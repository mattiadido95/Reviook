package it.unipi.dii.reviook_app.Controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import it.unipi.dii.reviook_app.Components.ListAuthor;
import it.unipi.dii.reviook_app.Components.ListBook;
import it.unipi.dii.reviook_app.Components.ListUser;
import it.unipi.dii.reviook_app.Data.Author;
import it.unipi.dii.reviook_app.Data.Book;

import it.unipi.dii.reviook_app.Data.Users;
import it.unipi.dii.reviook_app.Manager.SearchManager;
import it.unipi.dii.reviook_app.Manager.UserManager;
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
    private JFXListView<Users> usersList;

    @FXML
    private JFXListView<Author> authorsList;

    @FXML
    private JFXButton homeButton;

    @FXML
    private JFXButton profileButton;

    @FXML
    private TextField searchText;

    @FXML
    private ChoiceBox bookFilter;

    private ObservableList<String> availableChoices = FXCollections.observableArrayList("", "Test","Science", "Engineering", "Medicine", "Nonfiction", "Business & Investing", "Sports", "young", "graphic", "Outdoors & Nature", "Parenting & Families", "Computers & Internet", "mystery", "Law", "Health, Mind & Body", "comics", "romance", "Travel", "history", "fantasy", "Home & Garden", "crime", "children", "Horror", "Arts & Photography", "Literature & Fiction", "Biographies & Memoirs", "poetry", "Reference", "Professional & Technical", "biography", "Cooking, Food & Wine", "Teens", "Religion & Spirituality", "adult", "thriller", "Entertainment", "Gay & Lesbian");

    private SearchManager searchManager = new SearchManager();

    @FXML
    void initialize() {
        bookFilter.setItems(availableChoices);
    }

    @FXML
    void homeInterface(ActionEvent event) throws IOException {
        Session session = Session.getInstance();
        Parent homeInterface;
        if (session.getIsAuthor())
            homeInterface = FXMLLoader.load(getClass().getResource("/it/unipi/dii/reviook_app/fxml/author.fxml"));
        else
            homeInterface = FXMLLoader.load(getClass().getResource("/it/unipi/dii/reviook_app/fxml/user.fxml"));
        Stage actual_stage = (Stage) homeButton.getScene().getWindow();
        actual_stage.setScene(new Scene(homeInterface));
        actual_stage.setResizable(false);
        actual_stage.show();
    }

    @FXML
    void profileInterface() throws IOException {
        Session session = Session.getInstance();
        Parent userInterface;
        if (session.getIsAuthor()) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/it/unipi/dii/reviook_app/fxml/author.fxml"));
            userInterface = (Parent) fxmlLoader.load();
            AuthorInterfaceController controller = fxmlLoader.<AuthorInterfaceController>getController();
            // controller.setNickname(nickSelected);
        } else {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/it/unipi/dii/reviook_app/fxml/user.fxml"));
            userInterface = (Parent) fxmlLoader.load();
            UserInterfaceController controller = fxmlLoader.<UserInterfaceController>getController();
        }

        Stage actual_stage = (Stage) profileButton.getScene().getWindow();
        actual_stage.setScene(new Scene(userInterface));
        actual_stage.setResizable(false);
        actual_stage.show();
    }

    @FXML
    public void searchAction(ActionEvent actionEvent) {
        //TODO formattare meglio i risultati
        usersList.getItems().clear();
        bookList.getItems().clear();
        authorsList.getItems().clear();

        if (bookCheck.isSelected()) {

            bookList.setVisible(true);
            authorsList.setVisible(false);
            usersList.setVisible(false);

            String selectedChoice = (String) bookFilter.getSelectionModel().getSelectedItem();

            ObservableList<Book> obsBooksList = FXCollections.observableArrayList();
            obsBooksList.addAll(searchManager.searchBooks(searchText.getText(), selectedChoice));
            bookList.getItems().addAll(obsBooksList);
            bookList.setCellFactory(
                    new Callback<ListView<Book>, ListCell<Book>>() {
                        @Override
                        public ListCell<Book> call(ListView<Book> listView) {
                            return new ListBook();
                        }
                    });
            bookList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                        Book selectedCell = (Book) bookList.getSelectionModel().getSelectedItem();
                        try {
                            Parent bookInterface;
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/it/unipi/dii/reviook_app/fxml/bookDetail.fxml"));
                            bookInterface = (Parent) fxmlLoader.load();
                            BookDetailController bookController = fxmlLoader.getController();
                            bookController.setInfoBook(selectedCell);
                            Stage actual_stage = (Stage) profileButton.getScene().getWindow();
                            actual_stage.setScene(new Scene(bookInterface));
                            actual_stage.setResizable(false);
                            actual_stage.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        } else if (userCheck.isSelected()) {
            bookList.setVisible(false);
            authorsList.setVisible(false);
            usersList.setVisible(true);
            ObservableList<Users> obsUserList = FXCollections.observableArrayList();
            obsUserList.addAll(searchManager.searchUser(searchText.getText()));
            usersList.getItems().addAll(obsUserList);
            usersList.setCellFactory(new Callback<ListView<Users>, ListCell<Users>>() {
                @Override
                public ListCell<Users> call(ListView<Users> listView) {
                    return new ListUser();
                }
            });
            usersList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2 /*&& (mouseEvent.getTarget() instanceof Text)*/) {
                        Users selectedCell = (Users) usersList.getSelectionModel().getSelectedItem();
                        try {
                            Parent userInterface;
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/it/unipi/dii/reviook_app/fxml/user.fxml"));
                            userInterface = (Parent) fxmlLoader.load();
                            UserInterfaceController controller = fxmlLoader.<UserInterfaceController>getController();
                            controller.setNickname(selectedCell.getNickname());
                            Stage actual_stage = (Stage) profileButton.getScene().getWindow();
                            actual_stage.setScene(new Scene(userInterface));
                            actual_stage.setResizable(false);
                            actual_stage.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else if (authorCheck.isSelected()) {
            bookList.setVisible(false);
            authorsList.setVisible(true);
            usersList.setVisible(false);
            ObservableList<Author> obsUserList = FXCollections.observableArrayList();
            obsUserList.addAll(searchManager.searchAuthor(searchText.getText()));
            authorsList.getItems().addAll(obsUserList);
            authorsList.setCellFactory(new Callback<ListView<Author>, ListCell<Author>>() {
                @Override
                public ListCell<Author> call(ListView<Author> listView) {
                    return new ListAuthor();
                }
            });
            authorsList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2 /*&& (mouseEvent.getTarget() instanceof Text)*/) {
                        Author selectedCell = (Author) authorsList.getSelectionModel().getSelectedItem();
                        try {
                            Parent userInterface;
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/it/unipi/dii/reviook_app/fxml/author.fxml"));
                            userInterface = (Parent) fxmlLoader.load();
                            AuthorInterfaceController controller = fxmlLoader.<AuthorInterfaceController>getController();
                            controller.setNickname(selectedCell.getNickname());

                            Stage actual_stage = (Stage) profileButton.getScene().getWindow();
                            actual_stage.setScene(new Scene(userInterface));
                            actual_stage.setResizable(false);
                            actual_stage.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @FXML
    public void selectBookCheckAction(ActionEvent actionEvent) {
        bookCheck.setSelected(true);
        authorCheck.setSelected(false);
        userCheck.setSelected(false);
    }

    @FXML
    public void selectAuthorCheckAction(ActionEvent actionEvent) {
        bookCheck.setSelected(false);
        userCheck.setSelected(false);
        authorCheck.setSelected(true);
    }


    public void selectUserCheckAction(ActionEvent actionEvent) {
        bookCheck.setSelected(false);
        authorCheck.setSelected(false);
        userCheck.setSelected(true);
    }
}
