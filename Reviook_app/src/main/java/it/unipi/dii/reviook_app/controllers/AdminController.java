package it.unipi.dii.reviook_app.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXListView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import it.unipi.dii.reviook_app.Session;
import it.unipi.dii.reviook_app.components.*;
import it.unipi.dii.reviook_app.entity.*;
import it.unipi.dii.reviook_app.manager.AdminManager;
import it.unipi.dii.reviook_app.manager.BookManager;
import it.unipi.dii.reviook_app.manager.SearchManager;
import it.unipi.dii.reviook_app.manager.UserManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

public class AdminController {
    @FXML

    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private JFXCheckBox authorOption;

    @FXML
    private JFXCheckBox bookOption;

    @FXML
    private JFXCheckBox reviewOption;

    @FXML
    private JFXButton deleteElemButton;

    @FXML
    private JFXButton searchButton;

    @FXML
    private Text actionTarget;

    @FXML
    private JFXButton logoutButton;

    @FXML
    private JFXCheckBox userOption;

    @FXML
    private JFXListView<Report> bookList;

    @FXML
    private JFXListView<Report> reviewList;

    @FXML
    private JFXListView<User> usersList;

    @FXML
    private JFXListView<Author> authorsList;

    @FXML
    private TextField usernameField;

    @FXML
    private Text nameTitle;

    @FXML
    private Text description;

    @FXML
    private Text reviewText;

    @FXML
    private Text follower;

    @FXML
    private Text username;

    @FXML
    private JFXButton addAdminButton;

    private String selectedBookID = null;

    private SearchManager searchManager = new SearchManager();
    private BookManager bookManager = new BookManager();
    private UserManager userManager = new UserManager();
    private AdminManager adminManager = new AdminManager();
    private Session session = Session.getInstance();

    ObservableList<Report> obsBooksList = FXCollections.observableArrayList();
    ObservableList<User> obsUserList = FXCollections.observableArrayList();
    ObservableList<Author> obsAuthorList = FXCollections.observableArrayList();
    ObservableList<Report> obsListReview = FXCollections.observableArrayList();

    @FXML
    void logoutActon(ActionEvent event) throws IOException {
        // TODO va invalidata la sessione
        session.clear();
        Parent loginInterface = FXMLLoader.load(getClass().getResource("/it/unipi/dii/reviook_app/fxml/login.fxml"));
        Stage actual_stage = (Stage) logoutButton.getScene().getWindow();
        actual_stage.setScene(new Scene(loginInterface));
        actual_stage.setResizable(false);
        actual_stage.show();
        actual_stage.centerOnScreen();
    }

    void resetRightDetail() {
        nameTitle.setText("-");
        username.setText("-");
        description.setText("-");
        follower.setText("-");
        reviewText.setText("-");
    }

    @FXML
    void deleteElemAction(ActionEvent event) {
        actionTarget.setText("");
        if (bookOption.isSelected()) {
            Report selectedBook = (Report) bookList.getSelectionModel().getSelectedItem();
            bookManager.deleteBook(selectedBook.getBook_id());
            if (!adminManager.deleteReport(selectedBook))
                actionTarget.setText("Error: unable to remove Book");
            obsBooksList.remove(selectedBook);
            addCustomFactory("book");
            resetRightDetail();
        } else if (userOption.isSelected()) {
            User selectedUser = (User) usersList.getSelectionModel().getSelectedItem();
            if (userManager.deleteUserMongo(selectedUser, "user")) {
                if (!userManager.deleteUserN4J(selectedUser, "user"))
                    actionTarget.setText("Error: unable to register");
            } else {
                actionTarget.setText("Error: unable to register");
            }
            obsUserList.remove(selectedUser);
            addCustomFactory("user");
            resetRightDetail();
        } else if (authorOption.isSelected()) {
            Author selectedAuthor = (Author) authorsList.getSelectionModel().getSelectedItem();
            if (userManager.deleteUserMongo(selectedAuthor, "author")) {
                if (!userManager.deleteUserN4J(selectedAuthor, "author"))
                    actionTarget.setText("Error: unable to register");
            } else {
                actionTarget.setText("Error: unable to register");
            }
            obsAuthorList.remove(selectedAuthor);
            addCustomFactory("author");
            resetRightDetail();
        } else if (reviewOption.isSelected()) {
            Report selectedReview = (Report) reviewList.getSelectionModel().getSelectedItem();
            if (!adminManager.deleteReport(selectedReview))
                actionTarget.setText("Error: unable to remove Review");
            bookManager.deleteReview(selectedReview.getReview_id(), selectedReview.getBook_id());
            obsListReview.remove(selectedReview);
            addCustomFactory("review");
            resetRightDetail();
        }
    }

    @FXML
    void addAdminAction(ActionEvent event) throws IOException {
        Parent newAdminInterface = FXMLLoader.load(getClass().getResource("/it/unipi/dii/reviook_app/fxml/newAdmin.fxml"));
        Stage actual_stage = (Stage) addAdminButton.getScene().getWindow();
        actual_stage.setScene(new Scene(newAdminInterface));
        actual_stage.setResizable(false);
        actual_stage.show();
        actual_stage.centerOnScreen();
    }

    @FXML
    void unReport() {
        if (bookOption.isSelected()) {
            Report selectedBook = (Report) bookList.getSelectionModel().getSelectedItem();
            int index = bookList.getSelectionModel().getSelectedIndex();
            if (!adminManager.deleteReport(selectedBook))
                actionTarget.setText("Error: unable to remove bookReport");
            obsBooksList.remove(selectedBook);
            addCustomFactory("book");
            resetRightDetail();
        } else if (reviewOption.isSelected()) {
            Report selectedReview = (Report) reviewList.getSelectionModel().getSelectedItem();
            if (!adminManager.deleteReport(selectedReview))
                actionTarget.setText("Error: unable to remove reviewReport");
            obsListReview.remove(selectedReview);
            addCustomFactory("review");
            resetRightDetail();
        }
    }

//    void deleteReviewAction() {
//        Review selectedReview = (Review) reviewList.getSelectionModel().getSelectedItem();
//        if (selectedReview != null && selectedBookID != null) {
//            bookManager.deleteReview(selectedReview.getReview_id(), selectedBookID);
//            Book book = bookManager.getBookByID(this.selectedBookID); // query to update review
//            ObservableList<Review> obsListReview = FXCollections.observableArrayList();
//            obsListReview.setAll(book.getReviews());
//            this.reviewsListView.getItems().clear();
//            this.reviewsListView.setItems(obsListReview);
//        }
//    }

    @FXML
    void searchAction() {
        clearList();
        if (bookOption.isSelected()) {
            ArrayList<Report> list = adminManager.loadBookReported();
            obsBooksList.addAll(list);
            bookList.setVisible(true);
            authorsList.setVisible(false);
            usersList.setVisible(false);
            reviewList.setVisible(false);
            addCustomFactory("book");
        } else if (userOption.isSelected()) {
            ArrayList<User> list = searchManager.searchUser(usernameField.getText());
            obsUserList.addAll(list);
            bookList.setVisible(false);
            authorsList.setVisible(false);
            usersList.setVisible(true);
            reviewList.setVisible(false);
            addCustomFactory("user");
        } else if (authorOption.isSelected()) {
            ArrayList<Author> list = searchManager.searchAuthor(usernameField.getText());
            obsAuthorList.addAll(list);
            bookList.setVisible(false);
            authorsList.setVisible(true);
            usersList.setVisible(false);
            reviewList.setVisible(false);
            addCustomFactory("author");
        } else if (reviewOption.isSelected()) {
            ArrayList<Report> listRev = adminManager.loadReviewReported();
            obsListReview.addAll(listRev);
            usersList.setVisible(false);
            authorsList.setVisible(false);
            bookList.setVisible(false);
            reviewList.setVisible(true);
            addCustomFactory("review");
        }
    }

    private void addCustomFactory(String type) {
        if (type.equals("book")) {
            this.bookList.setCellFactory(new Callback<ListView<Report>, ListCell<Report>>() {
                @Override
                public ListCell<Report> call(ListView<Report> listView) {
                    return new ListReport();
                }
            });
            this.bookList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                        Report selectedBook = (Report) bookList.getSelectionModel().getSelectedItem();
                        if (selectedBook != null) {
                            selectedBookID = selectedBook.getBook_id();
                            nameTitle.setText(selectedBook.getTitle());
                            ArrayList<Author> authors = selectedBook.getAuthors();
                            ArrayList<String> authorsName = new ArrayList<>();
                            for (Author a : authors) {
                                authorsName.add(a.getName());
                            }
                            String author = String.join(", ", authorsName);
                            username.setText(author);
                            description.setText(selectedBook.getDescription());
                            follower.setText("-");
                            reviewText.setText("-");
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
                    if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                        User selectedUser = (User) usersList.getSelectionModel().getSelectedItem();
                        if (selectedUser != null) {
                            nameTitle.setText(selectedUser.getName());
                            username.setText(selectedUser.getNickname());
                            follower.setText(((Integer) selectedUser.getFollowerCount()).toString());
                            description.setText("-");
                            reviewText.setText("-");
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
                        Author selectedAuthor = (Author) authorsList.getSelectionModel().getSelectedItem();
                        if (selectedAuthor != null) {
                            nameTitle.setText(selectedAuthor.getName());
                            username.setText(selectedAuthor.getNickname());
                            follower.setText(((Integer) selectedAuthor.getFollowerCount()).toString());
                            description.setText("-");
                            reviewText.setText("-");
                        }
                    }
                }
            });
        } else if (type.equals("review")) {
            reviewList.setCellFactory(new Callback<ListView<Report>, javafx.scene.control.ListCell<Report>>() {
                @Override
                public ListCell<Report> call(ListView<Report> listView) {
                    return new ListReport();
                }
            });
            this.reviewList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                        Report selectedRev = (Report) reviewList.getSelectionModel().getSelectedItem();
                        if (selectedRev != null) {
                            reviewText.setText(selectedRev.getReview_text());
                            username.setText(selectedRev.getUsername());
                            description.setText("-");
                            follower.setText("-");
                            nameTitle.setText("-");
                        }
                    }
                }
            });
        }
    }

    @FXML
    public void selectBookCheckAction(ActionEvent actionEvent) {
        bookOption.setSelected(true);
        authorOption.setSelected(false);
        userOption.setSelected(false);
        reviewOption.setSelected(false);
    }

    @FXML
    public void selectAuthorCheckAction(ActionEvent actionEvent) {
        bookOption.setSelected(false);
        userOption.setSelected(false);
        authorOption.setSelected(true);
        reviewOption.setSelected(false);
    }

    @FXML
    public void selectUserCheckAction(ActionEvent actionEvent) {
        bookOption.setSelected(false);
        authorOption.setSelected(false);
        userOption.setSelected(true);
        reviewOption.setSelected(false);
    }

    @FXML
    public void selectReviewCheckAction(ActionEvent actionEvent) {
        bookOption.setSelected(false);
        authorOption.setSelected(false);
        userOption.setSelected(false);
        reviewOption.setSelected(true);
    }

    //TODO clear all obs list
    private void clearList() {
        obsListReview.clear();
        obsBooksList.clear();
        obsUserList.clear();
        obsAuthorList.clear();
    }

    @FXML
    void initialize() {
        clearList();
        reviewList.setItems(obsListReview);
        usersList.setItems(obsUserList);
        authorsList.setItems(obsAuthorList);
        bookList.setItems(obsBooksList);
    }
}
