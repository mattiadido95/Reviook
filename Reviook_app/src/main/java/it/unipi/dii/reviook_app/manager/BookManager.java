package it.unipi.dii.reviook_app.manager;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.reviook_app.entity.*;
import it.unipi.dii.reviook_app.MongoDriver;
import it.unipi.dii.reviook_app.Neo4jDriver;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;

import java.util.UUID;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Sorts.orderBy;
import static org.neo4j.driver.Values.parameters;


public class BookManager {
    private static MongoDriver md;
    private static Neo4jDriver nd;
    private it.unipi.dii.reviook_app.Session session = it.unipi.dii.reviook_app.Session.getInstance();
    private UserManager userManager;

    private static final String usersCollection = "users";
    private static final String authorCollection = "authors";
    private static final String bookCollection = "books";

    public BookManager() {
        this.md = MongoDriver.getInstance();
        this.nd = Neo4jDriver.getInstance();
        this.userManager = new UserManager();
    }

    public boolean removeBookFromList(String idBook, String Relation, String username, String Type) {
        try (Session session = nd.getDriver().session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (n:" + Type + "{username: '" + username + "' })-[r:" + Relation + "]->" +
                        "(c : Book{id: '" + idBook + "'}) " +
                        "DELETE r");
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean verifyISBN(String ISBN) {
        MongoCollection<Document> book = md.getCollection(bookCollection);
        try (MongoCursor<Document> cursor = book.find(eq("isbn", ISBN)).iterator()) {
            while (cursor.hasNext()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addBookMongo(Book newBook) {
        InsertOneResult result = null;
        //TODO controllare se il formato dei campi inseriti corrisponde a quelli di mongo
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        ArrayList<DBObject> authorsObj = new ArrayList<>();
        for (Author a : newBook.getAuthors()) {
            DBObject author = new BasicDBObject();
            author.put("author_name", (String) a.getName());
            author.put("author_role", ""); // TODO da togliere
            author.put("author_id", (String) a.getId());
            authorsObj.add(author);
        }

        //MONGO DB
        ArrayList<Review> reviews = new ArrayList<Review>();
        Document doc = new Document("language_code", newBook.getLanguage_code())
                .append("isbn", newBook.getIsbn())
                .append("description", newBook.getDescription())
                .append("num_pages", newBook.getNum_pages())
                .append("publication_day", newBook.getPublication_day())
                .append("publication_month", newBook.getPublication_month())
                .append("publication_year", newBook.getPublication_year())
                .append("image_url", newBook.getImage_url())
                .append("book_id", newBook.getBook_id())
                .append("title", newBook.getTitle())
                .append("average_rating", 0.0)
                .append("ratings_count", 0)
                .append("genres", newBook.getGenres())
                .append("authors", authorsObj)
                .append("reviews", newBook.getReviews());
        try {
            result = md.getCollection(bookCollection).insertOne(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result != null)
            return result.wasAcknowledged();
        return false;
    }

    public boolean addBookN4J(Book newBook) {
        Boolean result;
        try (Session session = nd.getDriver().session()) {
            result = session.writeTransaction((TransactionWork<Boolean>) tx -> {
                tx.run("CREATE (ee: Book { id : $id, title: $title})", parameters("id", newBook.getBook_id(), "title", newBook.getTitle()));
                for (int i = 0; i < newBook.getAuthors().size(); i++) {
                    tx.run("MATCH (dd:Author),(ee: Book) WHERE dd.id = '" + newBook.getAuthors().get(i).getId() + "' AND ee.id='" + newBook.getBook_id() + "'" +
                            "CREATE (dd)-[:WROTE]->(ee)");
                }
                return true;
            });
        }
        if (!result) {
            // can't add book into N4J --- delete book from MongoDB
            deleteBook(newBook.getBook_id());
        }
        return result;
    }

    public boolean addReviewToBook(String reviewText, Integer ratingBook, String book_id) {
        MongoCollection<Document> book = md.getCollection(bookCollection);
        Document newReview = new Document();
        String reviewID = UUID.randomUUID().toString();
        UpdateResult addReview, rateUpdated;
        LocalDateTime now = LocalDateTime.now();
        Date date = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        newReview.append("date_updated", date);
        newReview.append("review_id", reviewID);
        newReview.append("likes", 0);
        newReview.append("rating", ratingBook);
        newReview.append("review_text", reviewText);
        if (session.getLoggedUser() != null) {
            newReview.append("user_id", session.getLoggedUser().getId());
            newReview.append("username", session.getLoggedUser().getNickname());
        } else {
            newReview.append("user_id", session.getLoggedAuthor().getId());
            newReview.append("username", session.getLoggedAuthor().getNickname());
        }
        Bson getBook = eq("book_id", book_id);
        DBObject elem = new BasicDBObject("reviews", new BasicDBObject(newReview));
        DBObject insertReview = new BasicDBObject("$push", elem);
        addReview = book.updateOne(getBook, (Bson) insertReview);

        Book bookToUpdate = getBookByID(book_id);
        Double newRating = updateRating(bookToUpdate.getReviews());
        rateUpdated = book.updateOne(getBook, Updates.set("average_rating", newRating));

        if (addReview.getModifiedCount() == 1) {
            if (rateUpdated.getModifiedCount() == 1) {
                if (session.getLoggedUser() != null) {
                    //add that book to read list
                    if(userManager.readAdd("User", session.getLoggedUser().getNickname(), book_id)) {
                        return true;
                    }else{
                        //delete review
                        deleteReview(reviewID,book_id);
                        //update rating
                        newRating = updateRating(bookToUpdate.getReviews());
                        book.updateOne(getBook, Updates.set("average_rating", newRating));
                    }
                } else {
                    //add that book to read list
                    if(userManager.readAdd("Author", session.getLoggedAuthor().getNickname(), book_id)) {
                        return true;
                    }else{
                        //delete review
                        deleteReview(reviewID,book_id);
                        //update rating
                        newRating = updateRating(bookToUpdate.getReviews());
                        book.updateOne(getBook, Updates.set("average_rating", newRating));
                    }
                }
            }else{
                deleteReview(reviewID,book_id);
            }
        }
        return false;
    }

    public void editReview(String reviewText, Integer ratingBook, String book_id, String review_id) {
        MongoCollection<Document> books = md.getCollection(bookCollection);
        Bson getBook = eq("book_id", book_id);
        Bson getReview = eq("reviews.review_id", review_id);
        UpdateResult updateResult = books.updateOne(getReview, Updates.set("reviews.$.review_text", reviewText));
        UpdateResult updateResult2 = books.updateOne(getReview, Updates.set("reviews.$.rating", ratingBook));
        LocalDateTime now = LocalDateTime.now();
        Date date = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        UpdateResult updateResult3 = books.updateOne(getReview, Updates.set("reviews.$.date_updated", date));
        Book bookToUpdate = getBookByID(book_id);
        Double newRating = updateRating(bookToUpdate.getReviews());
        UpdateResult updateResult4 = books.updateOne(getBook, Updates.set("average_rating", newRating));
    }

    public boolean deleteReview(String review_id, String book_id) {
        MongoCollection<Document> books = md.getCollection(bookCollection);
        Bson getBook = eq("book_id", book_id);
        UpdateResult updateResult = books.updateOne(getBook, Updates.pull("reviews", new Document("review_id", review_id)));
        Book bookToUpdate = getBookByID(book_id);
        if (bookToUpdate == null) {
            return true;
        }
        Double newRating = updateRating(bookToUpdate.getReviews());
        UpdateResult updateResult2 = books.updateOne(getBook, Updates.set("average_rating", newRating));
        // TODO funziona solo per i like miei a mie review, altrimenti non funziona
        removeLikeReview(review_id, book_id);
        if (updateResult.getModifiedCount() == 1 && updateResult2.getModifiedCount() == 1) {
            return true;
        }
        return false;
    }

    public Book getBookByID(String book_id) {
        MongoCollection<Document> books = md.getCollection(bookCollection);
        Document book = new Document();
        try (MongoCursor<Document> cursor = books.find(eq("book_id", book_id)).iterator()) {
            if (!cursor.hasNext()) {
                return null;
            }
            book = cursor.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Author> authorsLis = new ArrayList<>();
        ArrayList<Review> reviewsList = new ArrayList<>();
        ArrayList<Document> authors = (ArrayList<Document>) book.get("authors");
        ArrayList<Document> reviews = (ArrayList<Document>) book.get("reviews");
        ArrayList<String> genres = (ArrayList<String>) book.get("genres");

        for (Document r : reviews) {
            reviewsList.add(new Review(
                    r.getString("username"),
                    r.getString("review_id"),
                    r.get("date_updated") == null ? "" : r.get("date_updated").toString(),
                    r.get("likes") == null ? r.getInteger("helpful") : r.getInteger("likes"),
                    r.getString("user_id"),
                    r.get("rating").toString(),
                    r.getString("review_text")
            ));
        }
        for (Document a : authors) {
            Author author = new Author(
                    a.getString("author_id"),
                    a.getString("author_name"),
                    "",
                    "",
                    "",
                    null,
                    0
            );
            authorsLis.add(author);
        }

        Book outputBook = new Book(
                book.get("isbn") == null ? null : book.getString("isbn"),
                book.get("language_code") == null ? null : book.getString("language_code"),
                book.get("asin") == null ? null : book.getString("asin"),
                book.get("average_rating").toString().equals("") ? Double.valueOf(0) : Double.valueOf(book.get("average_rating").toString()),
                book.get("description") == null ? null : book.getString("description"),
                book.get("num_pages") == null ? null : book.getInteger("num_pages"),
                book.get("publication_day") == null ? null : book.getInteger("publication_day"),
                book.get("publication_month") == null ? null : book.getInteger("publication_month"),
                book.get("publication_year") == null ? null : book.getInteger("publication_year"),
                book.get("image_url") == null ? null : book.getString("image_url"),
                book.getString("book_id"),
                book.getInteger("ratings_count"),
                book.getString("title"),
                authorsLis,
                genres,
                reviewsList
        );
        return outputBook;
    }

    public Double updateRating(ArrayList<Review> reviews) {
        Double ratingSum = 0.0;
        if (reviews.size() > 0) {
            for (Review r : reviews) {
                ratingSum += Double.parseDouble(r.getRating());
            }
            return ratingSum / reviews.size();
        } else {
            return ratingSum;
        }
    }

    public void addLikeReview(String reviewID, String book_id) {
        if (session.getLoggedAuthor() != null) {
            MongoCollection<Document> authors = md.getCollection(authorCollection);
            Bson getAuthor = eq("author_id", session.getLoggedAuthor().getId());
            DBObject elem = new BasicDBObject("liked_review", reviewID);
            DBObject insertRevID = new BasicDBObject("$push", elem);
            authors.updateOne(getAuthor, (Bson) insertRevID);
            //increment like review counter
            MongoCollection<Document> books = md.getCollection(bookCollection);
            Bson getBook = eq("book_id", book_id);
            Bson getReview = eq("reviews.review_id", reviewID);
            UpdateResult updateResult = books.updateOne(getReview, Updates.inc("reviews.$.likes", 1));
        } else if (session.getLoggedUser() != null) {
            MongoCollection<Document> users = md.getCollection(usersCollection);
            Bson getUser = eq("user_id", session.getLoggedUser().getId());
            DBObject elem = new BasicDBObject("liked_review", reviewID);
            DBObject insertRevID = new BasicDBObject("$push", elem);
            users.updateOne(getUser, (Bson) insertRevID);
            //increment like review counter
            MongoCollection<Document> books = md.getCollection(bookCollection);
            Bson getBook = eq("book_id", book_id);
            Bson getReview = eq("reviews.review_id", reviewID);
            UpdateResult updateResult = books.updateOne(getReview, Updates.inc("reviews.$.likes", 1));
        }

    }

    public void removeLikeReview(String reviewID, String book_id) {
        if (session.getLoggedAuthor() != null) {
            MongoCollection<Document> authors = md.getCollection(authorCollection);
            Bson getAuthor = eq("author_id", session.getLoggedAuthor().getId());
            authors.updateOne(getAuthor, Updates.pull("liked_review", reviewID));
            //decrement like review counter
            MongoCollection<Document> books = md.getCollection(bookCollection);
            Bson getBook = eq("book_id", book_id);
            Bson getReview = eq("reviews.review_id", reviewID);
            UpdateResult updateResult = books.updateOne(getReview, Updates.inc("reviews.$.likes", -1));
        } else if (session.getLoggedUser() != null) {
            MongoCollection<Document> users = md.getCollection(usersCollection);
            Bson getUser = eq("user_id", session.getLoggedUser().getId());
            users.updateOne(getUser, Updates.pull("liked_review", reviewID));
            //increment like review counter
            MongoCollection<Document> books = md.getCollection(bookCollection);
            Bson getBook = eq("book_id", book_id);
            Bson getReview = eq("reviews.review_id", reviewID);
            UpdateResult updateResult = books.updateOne(getReview, Updates.inc("reviews.$.likes", -1));
        }

    }

    public boolean foundMyBook(String id_book, String id_author) {
        MongoCollection<Document> book = md.getCollection(bookCollection);
        Bson match = match(in("book_id", id_book));
        Bson project = project(fields(include("authors.author_id")));
        try (MongoCursor<Document> result = book.aggregate(Arrays.asList(match, project)).iterator();) {
            if (result.hasNext()) {
                ArrayList<Document> myAuthor = (ArrayList<Document>) result.next().get("authors");
                for (int i = 0; i < myAuthor.size(); i++) {
                    if (myAuthor.get(i).getString("author_id").equals(id_author))
                        return true;
                }
            }
        }
        return false;
    }

    public static boolean deleteBook(String book_id) {
        MongoCollection<Document> books = md.getCollection(bookCollection);
        DeleteResult deleteResult = books.deleteOne(eq("book_id", book_id));
        if (deleteResult.getDeletedCount() == 1) {
            try (Session session = nd.getDriver().session()) {
                session.writeTransaction((TransactionWork<Boolean>) tx -> {
                    tx.run("MATCH (n : Book { id: '" + book_id + "'}) DETACH DELETE n");
                    return true;
                });
            }
            return true;
        }
        return false;
    }

    //ANALYTICS ==========================================================================================================

    public ArrayList<Book> similarBooks(String book_id) {
        ArrayList<Book> suggestion = new ArrayList<>();
        ArrayList<Book> queryResult = new ArrayList<>();

        try (Session session = nd.getDriver().session()) {
            suggestion = session.readTransaction((TransactionWork<ArrayList<Book>>) tx -> {
                Result result = tx.run("MATCH (b1:Book)<-[:WROTE]-(a:Author)-[:WROTE]->(b2:Book) " +
                        "WHERE b1.id = '" + book_id + "' AND b1<>b2 " +
                        "RETURN DISTINCT b2.id,b2.title");
                while (result.hasNext()) {
                    Record r = result.next();
                    queryResult.add(new Book(r.get("b2.title").asString(), r.get("b2.id").asString()));
                }
                return queryResult;
            });
        }
        return suggestion;
    }

    public ArrayList<Author> similarAuthors(String book_id) {
        ArrayList<Author> suggestion;
        ArrayList<Author> queryResult = new ArrayList<>();

        try (Session session = nd.getDriver().session()) {
            suggestion = (ArrayList<Author>) session.readTransaction((TransactionWork<ArrayList<Author>>) tx -> {
                Result result = tx.run("MATCH (b1:Book)<-[:WROTE]-(a1:Author)-[:WROTE]->(b2:Book)<-[:WROTE]-(a2:Author) " +
                        "WHERE b1.id = '" + book_id + "' AND b1<>b2 AND a1<>a2 " +
                        "RETURN DISTINCT a2.id,a2.name,a2.username");
                while (result.hasNext()) {
                    Record r = result.next();
                    queryResult.add(new Author(r.get("a2.id").asString(), r.get("a2.name").asString(), r.get("a2.username").asString(), "", "", new ArrayList<>(), 0));
                }
                return queryResult;
            });
        }
        return suggestion;
    }

    public ArrayList<RankingObject> topBooks(String type, Integer limit) {
        ArrayList<RankingObject> books;
        ArrayList<RankingObject> queryResult = new ArrayList<>();

        try (Session session = nd.getDriver().session()) {
            books = (ArrayList<RankingObject>) session.readTransaction((TransactionWork<ArrayList<RankingObject>>) tx -> {
                Result result = tx.run("MATCH (b:Book)<-[r:" + type + "]-() " +
                        "RETURN b.id,b.title,count(r) as count " +
                        "ORDER BY count DESC " +
                        "LIMIT " + limit + "");
                while (result.hasNext()) {
                    Record r = result.next();
                    queryResult.add(new RankingObject(r.get("b.title").asString(), Integer.valueOf(r.get("count").toString())));
                }
                return queryResult;
            });
        }
        return books;
    }

    public ArrayList<Genre> searchRankBook(Integer year) {
        MongoCollection<Document> bookGenres = md.getCollection(bookCollection);

        ArrayList<Genre> genres = new ArrayList<>();
        Bson match = match(in("publication_year", year));
        Bson unwind = unwind("$genres");
        Bson group = group("$genres", sum("counter", 1));

        try (MongoCursor<Document> result = bookGenres.aggregate(Arrays.asList(match, unwind, group)).iterator()) {

            while (result.hasNext()) {
                Document y = result.next();
                genres.add(new Genre(y.getString("_id"), Double.valueOf(y.get("counter").toString())));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return genres;
    }

    public ArrayList<RankingObject> rankReview() {
        MongoCollection<Document> book = md.getCollection(bookCollection);
        ArrayList<RankingObject> users = new ArrayList<>();

        Bson unwindReviews = unwind("$reviews");
        //project likes : ($likes != null) $likes : $helpful
        Bson projectLikes = new Document("$project",
                new Document("reviews.username", 1L)
                        .append("reviews.likes",
                                new Document("$ifNull", Arrays.asList("$reviews.likes", "$reviews.helpful"))));
        Bson groupUsername = group("$reviews.username", sum("reviews_number", 1), avg("average_likes", "$reviews.likes"));
        Bson matchGreaterThan200 = match(gte("reviews_number", 200));
        Bson sort = sort(orderBy(descending("average_likes")));
        Bson limit = limit(100);

        try (MongoCursor<Document> result = book.aggregate(Arrays.asList(unwindReviews, projectLikes, groupUsername, matchGreaterThan200, sort, limit)).iterator()) {

            while (result.hasNext()) {
                Document document = result.next();
                users.add(new RankingObject(document.getString("_id"),
                        document.getInteger("reviews_number"),
                        document.getDouble("average_likes")));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return users;

    }

    //==================================================================================================================

}
