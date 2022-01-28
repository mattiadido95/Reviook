package it.unipi.dii.reviook_app.entity;

import java.util.ArrayList;
import java.util.List;

public class Author extends User {
    private ArrayList<Book> writtenBook;
    private ArrayList<Book> published;

    public Author(String nickname) {
        super(nickname);
        this.writtenBook = new ArrayList<>();
        this.published = new ArrayList<>();
    }
    public Author(String id, String name, String surname, String nickname, String email, String password, ArrayList<String> listReviewID, Integer follower_count) {
        super(id, name, surname, nickname, email, password, listReviewID, follower_count);
        this.writtenBook = new ArrayList<>();
        this.published = new ArrayList<>();
    }
    public String setPublished(String title, String book_id) {
        Book b = new Book(title, book_id);
        b.setTitle((published.size() + 1) + ":" + b.getTitle());
        this.published.add(b);
        return b.getTitle();
    }
    public String getIdBookPublished(String title) {
        for (Book Books : this.published) {
            if (Books.getTitle().equals(title))
                return Books.getBook_id();
        }
        return null;
    }
    public ArrayList<Book> getPublished(){
        return this.published;
    }

    public void setWrittenBook(Book writtenBook) {
        this.writtenBook.add(writtenBook);
    }




    @Override
    public String toString() {
        return "Author{" +
                "writtenBook=" + writtenBook +
                ", userManager=" + userManager.toString() +
                '}';
    }
}
