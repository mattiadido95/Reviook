package it.unipi.dii.reviook_app.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.UUID;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import it.unipi.dii.reviook_app.entity.Author;
import it.unipi.dii.reviook_app.entity.Book;
import it.unipi.dii.reviook_app.entity.Genre;
import it.unipi.dii.reviook_app.manager.BookManager;
import it.unipi.dii.reviook_app.manager.SearchManager;
import it.unipi.dii.reviook_app.manager.UserManager;
import it.unipi.dii.reviook_app.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;


public class AddBookController {
    @FXML
    private JFXListView<Author> authorsList;

    @FXML
    private JFXListView<Genre> genreList;

    @FXML
    private JFXListView<String> genreTag;

    @FXML
    JFXButton backButton;

    @FXML
    private JFXListView<Author> listTagged;

    @FXML
    private TextField authorTag, titleBook, ISBN, numPage, URLImage;

    @FXML
    private TextArea description;

    @FXML
    private Text actionTarget;

    @FXML
    private ChoiceBox languageCode;

    @FXML
    private DatePicker publication_date;


    private Session session = Session.getInstance();
    private SearchManager searchManager = new SearchManager();
    private BookManager bookManager = new BookManager();
    private ObservableList<String> availableChoices = FXCollections.observableArrayList();

    @FXML
    int contatoreUsername = 0;
    int contatoreGener = 0;

    public void searchAction(ActionEvent actionEvent) {
        authorsList.getItems().clear();
        genreList.getItems().clear();
        genreList.setVisible(false);
        authorsList.setVisible(true);
        ObservableList<Author> obsUserList = FXCollections.observableArrayList();
        obsUserList.addAll(searchManager.searchAuthor(authorTag.getText()));
        authorsList.getItems().addAll(obsUserList);
        authorsList.setCellFactory(new Callback<ListView<Author>, ListCell<Author>>() {
            @Override
            public ListCell<Author> call(ListView<Author> listView) {
                return new ListCell<Author>() {
                    @Override
                    public void updateItem(Author item, boolean empty) {
                        super.updateItem(item, empty);
                        textProperty().unbind();
                        if (item != null)
                            setText(item.getNickname());
                        else
                            setText(null);
                    }
                };
            }
        });
        authorsList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                boolean check = false;
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2 /*&& (mouseEvent.getTarget() instanceof Text)*/) {
                    Author selectedCell = (Author) authorsList.getSelectionModel().getSelectedItem();
                    for (int i = 0; i < contatoreUsername; i++) {
                        if (listTagged.getItems().get(i).equals(selectedCell.getNickname())) {
                            check = true;
                            break;
                        } else check = false;
                    }
                    if (check)
                        return;
                    listTagged.getItems().addAll(selectedCell);
                    contatoreUsername++;
                }
            }
        });
        listTagged.setCellFactory(new Callback<ListView<Author>, ListCell<Author>>() {
            @Override
            public ListCell<Author> call(ListView<Author> listView) {
                return new ListCell<Author>() {
                    @Override
                    public void updateItem(Author item, boolean empty) {
                        super.updateItem(item, empty);
                        textProperty().unbind();
                        if (item != null)
                            setText(item.getNickname());
                        else
                            setText(null);
                    }
                };
            }
        });
        listTagged.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2 /*&& (mouseEvent.getTarget() instanceof Text)*/) {
                    Author removeCell = (Author) listTagged.getSelectionModel().getSelectedItem();
                    listTagged.getItems().remove(removeCell);
                    contatoreUsername--;
                }
            }
        });
    }

    @FXML
    public void searchActionGenres(ActionEvent actionEvent) {
        authorsList.getItems().clear();
        genreList.getItems().clear();
        genreList.setVisible(true);
        authorsList.setVisible(false);
        ObservableList<Genre> obsGenreList = FXCollections.observableArrayList();
        obsGenreList.addAll(searchManager.searchGenres());
        genreList.getItems().addAll(obsGenreList);
        genreList.setCellFactory(new Callback<ListView<Genre>, ListCell<Genre>>() {
            @Override
            public ListCell<Genre> call(ListView<Genre> listView) {
                return new ListCell<Genre>() {
                    @Override
                    public void updateItem(Genre item, boolean empty) {
                        super.updateItem(item, empty);
                        textProperty().unbind();
                        if (item != null)
                            setText(item.getType());
                        else
                            setText(null);
                    }
                };
            }
        });
        genreList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                boolean check = false;
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2 /*&& (mouseEvent.getTarget() instanceof Text)*/) {
                    String selectedCell = genreList.getSelectionModel().getSelectedItem().getType();
                    for (int i = 0; i < contatoreGener; i++) {
                        if (genreTag.getItems().get(i).equals(selectedCell)) {
                            check = true;
                            break;
                        } else check = false;
                    }
                    if (check)
                        return;
                    genreTag.getItems().addAll(selectedCell);
                    contatoreGener++;
                }
            }
        });
        genreTag.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2 /*&& (mouseEvent.getTarget() instanceof Text)*/) {
                    String removeCell = genreTag.getSelectionModel().getSelectedItem();
                    genreTag.getItems().remove(removeCell);
                    contatoreGener--;
                }
            }
        });
    }

    public void backFunction() throws IOException {
        Parent authorInterface;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/it/unipi/dii/reviook_app/fxml/author.fxml"));
        authorInterface = (Parent) fxmlLoader.load();
        AuthorInterfaceController authorInterfaceController = fxmlLoader.getController();
        authorInterfaceController.setAuthor(session.getLoggedAuthor());
        Stage actual_stage = (Stage) backButton.getScene().getWindow();
        actual_stage.setScene(new Scene(authorInterface));
        actual_stage.setResizable(false);
        actual_stage.show();
        actual_stage.centerOnScreen();
    }

    public void addBookFunction() throws IOException {
        ArrayList<String> authorCheck = new ArrayList<>();
        if (titleBook.getText().isEmpty()) {
            actionTarget.setText("You must enter the title");
            return;
        }
        if (ISBN.getText().isEmpty()) {
            actionTarget.setText("You must enter the ISBN");
            return;
        }
        if (genreTag.getItems().isEmpty()) {
            actionTarget.setText("You must enter the Genres");
            return;
        }
        String Title = titleBook.getText();
        String ISBN_ = ISBN.getText();
        LocalDate date = (publication_date.getValue() == null) ? LocalDate.now() : publication_date.getValue();
        String selectedChoice = languageCode.getSelectionModel().getSelectedItem() == null ? "" : languageCode.getSelectionModel().getSelectedItem().toString();
        String URL_image = URLImage.getText();
        Integer num_pages = numPage.getText().equals("") ? 0 : Integer.valueOf(numPage.getText());
        String Description = description.getText();
        ArrayList<String> Genre = new ArrayList<String>((ObservableList) genreTag.getItems());
        ArrayList<Author> AuthorTagged = new ArrayList<Author>((ObservableList) listTagged.getItems());
        for (Author a : AuthorTagged) {
            authorCheck.add(a.getNickname());
        }
        if (!authorCheck.contains(session.getLoggedAuthor().getNickname())) {
            // get author logged
            AuthorTagged.add(session.getLoggedAuthor());
        }
       /* ArrayList<DBObject> param = new ArrayList<DBObject>();
        for (int i = 0; i < AuthorTagged.size(); i++) {
            param.add(userManager.paramAuthor(AuthorTagged.get(i)));
        }*/
        if (bookManager.verifyISBN(ISBN_)) {
            actionTarget.setText("Existing ISBN");
            return;
        }
        String concat = ISBN_ + Title + session.getLoggedAuthor().getNickname();
        String id = UUID.nameUUIDFromBytes(concat.getBytes()).toString();
        Book newBook = new Book(num_pages, URL_image, selectedChoice, date, id, Title, ISBN_, Description, Genre, AuthorTagged);
        if(bookManager.addBookMongo(newBook)) {
            if (bookManager.addBookN4J(newBook)) {
                session.getLoggedAuthor().addToPublished(newBook);
            }else{
                bookManager.deleteBookMongo(newBook);
                actionTarget.setText("Error: unable to add book");
                return;
            }
        }else{
            actionTarget.setText("Error: unable to add book");
            return;
        }
        titleBook.clear();
        ISBN.clear();
        description.clear();
        genreTag.getItems().clear();
        listTagged.getItems().clear();
        backFunction();
    }

    @FXML
    void initialize() {
        availableChoices.addAll(searchManager.searchLanguageCode());
        languageCode.setItems(availableChoices);
    }

}
