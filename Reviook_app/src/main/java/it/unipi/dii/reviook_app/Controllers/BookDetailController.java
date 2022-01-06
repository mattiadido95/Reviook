package it.unipi.dii.reviook_app.Controllers;

import com.jfoenix.controls.JFXButton;
import it.unipi.dii.reviook_app.Components.ListElem;
import it.unipi.dii.reviook_app.Data.Author;
import it.unipi.dii.reviook_app.Data.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.*;

import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.util.Callback;


public class BookDetailController {
    @FXML
    private ResourceBundle resources;

    @FXML
    private ImageView imageContainer;

    @FXML
    private URL location;

    @FXML
    private JFXButton searchButton;

    @FXML
    private JFXButton addReviewButton;

    @FXML
    private ListView listView;

    @FXML
    private Text bookAuthor;

    @FXML
    private Text bookCategories;

    @FXML
    private Text bookDescription;

    @FXML
    private Text bookTitle;

    private String title, author, categories, description, img_url;

    private List<String> stringList = new ArrayList<>(5);

    private ObservableList observableList = FXCollections.observableArrayList();

    public void setListView() {

    }

    @FXML
    void searchInterface(ActionEvent event) throws IOException {
        Parent searchInterface = FXMLLoader.load(getClass().getResource("/it/unipi/dii/reviook_app/fxml/search.fxml"));
        Stage actual_stage = (Stage) searchButton.getScene().getWindow();
        actual_stage.setScene(new Scene(searchInterface));
        actual_stage.setResizable(false);
        actual_stage.show();
    }

    @FXML
    public void addReviewAction(ActionEvent actionEvent) {
    }

    public void setInfoBook(Book bookSelected) {
        // BOOK TITLE
        this.title = bookSelected.getTitle();
        bookTitle.setText(this.title);
        // AUTHORS LIST
        ArrayList<String> authors = bookSelected.getAuthors();
        this.author = authors.get(0); // TODO per ora prendo solo il primo ma poi andranno elencati tutti
        bookAuthor.setText(this.author);
        // CATEGORIES LIST
        ArrayList<String> genres = bookSelected.getGenres();
        categories = genres.get(0); // TODO per ora prendo solo il primo ma poi andranno elencati tutti
        bookCategories.setText(categories);
        // BOOK DESCRIPTION
        this.description = bookSelected.getDescription();
        bookDescription.setText(this.description);
        // IMG BOOK
        this.img_url = bookSelected.getImage_url();
        File file = new File(this.img_url);
        Image image = new Image(this.img_url);
        imageContainer.setImage(image);
    }

    @FXML
    void initialize() {
        setListView();
    }
}

