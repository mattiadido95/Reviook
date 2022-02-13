package it.unipi.dii.reviook_app.controllers;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import it.unipi.dii.reviook_app.components.ListAuthor;
import it.unipi.dii.reviook_app.components.ListBook;
import it.unipi.dii.reviook_app.components.ListUser;
import it.unipi.dii.reviook_app.entity.Author;
import it.unipi.dii.reviook_app.entity.Book;

import it.unipi.dii.reviook_app.entity.Genre;
import it.unipi.dii.reviook_app.entity.User;
import it.unipi.dii.reviook_app.manager.SearchManager;
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
import javafx.stage.Stage;
import javafx.util.Callback;

public class SearchInterfaceController {
    @FXML
    private ResourceBundle resources;

    @FXML
    private CheckBox authorCheck;

    @FXML
    private CheckBox userCheck;

    @FXML
    private CheckBox bookCheck;

    @FXML
    private JFXListView<Book> bookList;

    @FXML
    private JFXListView<User> usersList;

    @FXML
    private JFXListView<Author> authorsList;

    @FXML
    private JFXButton homeButton;

    @FXML
    private TextField searchText;

    @FXML
    private ChoiceBox bookFilter;

    private SearchManager searchManager = new SearchManager();

    private Session session = Session.getInstance();

    private ObservableList<Genre> availableChoices = FXCollections.observableArrayList();
    private ObservableList<Book> obsBooksList = FXCollections.observableArrayList();
    private ObservableList<User> obsUserList = FXCollections.observableArrayList();
    private ObservableList<Author> obsAuthorList = FXCollections.observableArrayList();

    private void clearList() {
        obsBooksList.clear();
        obsUserList.clear();
        obsAuthorList.clear();
    }

    @FXML
    void initialize() {
        availableChoices.add(new Genre(""));
        availableChoices.addAll(searchManager.searchGenres());
        bookFilter.setItems(availableChoices);
        bookFilter.setVisible(false);

        //set jfxlist
        bookList.setItems(obsBooksList);
        usersList.setItems(obsUserList);
        authorsList.setItems(obsAuthorList);

        if (checkCacheValid()) {
            //inizializzo le liste se ho la cache piena
            if (session.getCache().getSearchType() != null && session.getCache().getSearchType().equals("book") && session.getCache().getSearchedBooks() != null) {
//            System.out.println("book: "+session.getCache().getSearchedBooks().size());
                obsBooksList.addAll(session.getCache().getSearchedBooks());
                addCustomFactory("book");
                bookCheck.setSelected(true);
                authorCheck.setSelected(false);
                userCheck.setSelected(false);
                bookList.setVisible(true);
                usersList.setVisible(false);
                authorsList.setVisible(false);
            } else if (session.getCache().getSearchType() != null && session.getCache().getSearchType().equals("user") && session.getCache().getSearchedUsers() != null) {
//            System.out.println("user: "+session.getCache().getSearchedUsers().size());
                obsUserList.addAll(session.getCache().getSearchedUsers());
                addCustomFactory("user");
                bookCheck.setSelected(false);
                authorCheck.setSelected(false);
                userCheck.setSelected(true);
                usersList.setVisible(true);
                authorsList.setVisible(false);
                bookList.setVisible(false);
            } else if (session.getCache().getSearchType() != null && session.getCache().getSearchType().equals("author") && session.getCache().getSearchedAuthors() != null) {
//            System.out.println("author: "+session.getCache().getSearchedAuthors().size());
                obsAuthorList.addAll(session.getCache().getSearchedAuthors());
                addCustomFactory("author");
                bookCheck.setSelected(false);
                userCheck.setSelected(false);
                authorCheck.setSelected(true);
                authorsList.setVisible(true);
                bookList.setVisible(false);
                usersList.setVisible(false);
            } else {
                clearList();
            }
        }
    }

    @FXML
    void homeInterface(ActionEvent event) throws IOException {
        Session session = Session.getInstance();
        Parent homeInterface;
        if (session.getIsAuthor()) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/it/unipi/dii/reviook_app/fxml/author.fxml"));
            homeInterface = (Parent) fxmlLoader.load();
            AuthorInterfaceController authorInterfaceController = fxmlLoader.getController();
            authorInterfaceController.setAuthor(session.getLoggedAuthor());
        } else {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/it/unipi/dii/reviook_app/fxml/user.fxml"));
            homeInterface = (Parent) fxmlLoader.load();
            UserInterfaceController userInterfaceController = fxmlLoader.getController();
            userInterfaceController.setUser(session.getLoggedUser());
        }
        Stage actual_stage = (Stage) homeButton.getScene().getWindow();
        actual_stage.setScene(new Scene(homeInterface));
        actual_stage.setResizable(false);
        actual_stage.show();
        actual_stage.centerOnScreen();
    }

    @FXML
    public void searchAction(ActionEvent actionEvent) {
        clearList();

        if (bookCheck.isSelected()) {
            bookList.setVisible(true);
            authorsList.setVisible(false);
            usersList.setVisible(false);
            String selectedChoice = bookFilter.getSelectionModel().getSelectedItem() == null ? "" : bookFilter.getSelectionModel().getSelectedItem().toString();
            ArrayList<Book> list = searchManager.searchBooks(searchText.getText(), selectedChoice);
            session.getCache().setSearchedBooks(list);
            session.getCache().setLastUpdate(new Date());
            obsBooksList.addAll(list);
            addCustomFactory("book");
        } else if (userCheck.isSelected()) {
            bookList.setVisible(false);
            authorsList.setVisible(false);
            usersList.setVisible(true);
            ArrayList<User> list = searchManager.searchUser(searchText.getText());
            session.getCache().setSearchedUsers(list);
            session.getCache().setLastUpdate(new Date());
            obsUserList.addAll(list);
            addCustomFactory("user");
        } else if (authorCheck.isSelected()) {
            bookList.setVisible(false);
            authorsList.setVisible(true);
            usersList.setVisible(false);
            ArrayList<Author> list = searchManager.searchAuthor(searchText.getText());
            session.getCache().setSearchedAuthors(list);
            session.getCache().setLastUpdate(new Date());
            obsAuthorList.addAll(list);
            addCustomFactory("author");
        }
    }

    @FXML
    public void selectBookCheckAction(ActionEvent actionEvent) {
        bookCheck.setSelected(true);
        authorCheck.setSelected(false);
        userCheck.setSelected(false);
        bookFilter.setVisible(true);
        session.getCache().setSearchType("book");
        if (session.getCache().getSearchedBooks() != null && checkCacheValid()) {
            obsBooksList.addAll(session.getCache().getSearchedBooks());
            addCustomFactory("book");
            bookList.setVisible(true);
            usersList.setVisible(false);
            authorsList.setVisible(false);
        }
    }

    @FXML
    public void selectAuthorCheckAction(ActionEvent actionEvent) {
        bookCheck.setSelected(false);
        userCheck.setSelected(false);
        authorCheck.setSelected(true);
        bookFilter.setVisible(false);
        session.getCache().setSearchType("author");
        if (session.getCache().getSearchedAuthors() != null && checkCacheValid()) {
            obsAuthorList.addAll(session.getCache().getSearchedAuthors());
            addCustomFactory("author");
            bookList.setVisible(false);
            usersList.setVisible(false);
            authorsList.setVisible(true);
        }
    }

    @FXML
    public void selectUserCheckAction(ActionEvent actionEvent) {
        bookCheck.setSelected(false);
        authorCheck.setSelected(false);
        userCheck.setSelected(true);
        bookFilter.setVisible(false);
        session.getCache().setSearchType("user");
        if (session.getCache().getSearchedUsers() != null && checkCacheValid()) {
            obsUserList.addAll(session.getCache().getSearchedUsers());
            addCustomFactory("user");
            bookList.setVisible(false);
            usersList.setVisible(true);
            authorsList.setVisible(false);
        }
    }

    private void addCustomFactory(String type) {
        if (type.equals("book")) {
            this.bookList.setCellFactory(new Callback<ListView<Book>, ListCell<Book>>() {
                @Override
                public ListCell<Book> call(ListView<Book> listView) {
                    return new ListBook();
                }
            });
            this.bookList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                        Book selectedCell = (Book) bookList.getSelectionModel().getSelectedItem();
                        if (selectedCell == null){
                            return;
                        }
                        try {
                            Parent bookInterface;
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/it/unipi/dii/reviook_app/fxml/bookDetail.fxml"));
                            bookInterface = (Parent) fxmlLoader.load();
                            BookDetailController bookController = fxmlLoader.getController();
                            bookController.setInfoBook(selectedCell, false);
                            Stage actual_stage = (Stage) homeButton.getScene().getWindow();
                            actual_stage.setScene(new Scene(bookInterface));
                            actual_stage.setResizable(false);
                            actual_stage.show();
                            actual_stage.centerOnScreen();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else if (type.equals("user")) {
            this.usersList.setCellFactory(new Callback<ListView<User>, ListCell<User>>() {
                @Override
                public ListCell<User> call(ListView<User> listView) {
                    return new ListUser();
                }
            });
            this.usersList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2 /*&& (mouseEvent.getTarget() instanceof Text)*/) {
                        User selectedCell = (User) usersList.getSelectionModel().getSelectedItem();
                        if (selectedCell == null){
                            return;
                        }
                        try {
                            Parent userInterface;
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/it/unipi/dii/reviook_app/fxml/user.fxml"));
                            userInterface = (Parent) fxmlLoader.load();
                            UserInterfaceController controller = fxmlLoader.<UserInterfaceController>getController();
                            controller.setUser(selectedCell);
                            Stage actual_stage = (Stage) homeButton.getScene().getWindow();
                            actual_stage.setScene(new Scene(userInterface));
                            actual_stage.setResizable(false);
                            actual_stage.show();
                            actual_stage.centerOnScreen();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else if (type.equals("author")) {
            this.authorsList.setCellFactory(new Callback<ListView<Author>, ListCell<Author>>() {
                @Override
                public ListCell<Author> call(ListView<Author> listView) {
                    return new ListAuthor();
                }
            });
            this.authorsList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                        Author selectedCell = (Author) authorsList.getSelectionModel().getSelectedItem();
                        if (selectedCell == null){
                            return;
                        }
                        try {
                            Parent userInterface;
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/it/unipi/dii/reviook_app/fxml/author.fxml"));
                            userInterface = (Parent) fxmlLoader.load();
                            AuthorInterfaceController controller = fxmlLoader.<AuthorInterfaceController>getController();
                            controller.setAuthor(selectedCell);
                            Stage actual_stage = (Stage) homeButton.getScene().getWindow();
                            actual_stage.setScene(new Scene(userInterface));
                            actual_stage.setResizable(false);
                            actual_stage.show();
                            actual_stage.centerOnScreen();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private boolean checkCacheValid() {
        Date timeNow = new Date();
        long difference_In_Time = timeNow.getTime() - session.getCache().getLastUpdate().getTime();
        long difference_In_Minutes = (difference_In_Time / (1000 * 60)) % 60;
        if (difference_In_Minutes >= 10) {
            session.getCache().ClearCache();
            clearList();
            return false;
        }
        return true;
    }
}
