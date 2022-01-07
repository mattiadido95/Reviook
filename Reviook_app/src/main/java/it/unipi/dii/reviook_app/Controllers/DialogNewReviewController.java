package it.unipi.dii.reviook_app.Controllers;

import com.jfoenix.controls.JFXButton;
import it.unipi.dii.reviook_app.Data.Users;
import it.unipi.dii.reviook_app.Manager.UserManager;
import it.unipi.dii.reviook_app.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DialogNewReviewController {
    @FXML
    public TextArea newReviewText;

    @FXML
    public JFXButton exitButton;

    @FXML
    public JFXButton addButton;

    @FXML
    private ChoiceBox<Integer> bookStars;

    private String book_id;

    UserManager userManager = new UserManager();


    @FXML
    public void setBook_id(String id) {
        this.book_id = id;
    }

    @FXML

    public void exitDialogAction(ActionEvent actionEvent) {
        Stage actual_stage = (Stage) exitButton.getScene().getWindow();
        actual_stage.close();
    }

    @FXML
    public void addReviewAction(ActionEvent actionEvent) {
        String reviewText = newReviewText.getText();
        Integer ratingBook = bookStars.getValue() != null ? bookStars.getValue() : 0;
        String book_id = this.book_id;
        userManager.AddReviewToBook(reviewText, ratingBook, book_id);
    }

    @FXML
    void initialize() {
        ObservableList<Integer> choice = FXCollections.observableArrayList(1, 2, 3, 4, 5);
        bookStars.setItems(choice);
    }
}
