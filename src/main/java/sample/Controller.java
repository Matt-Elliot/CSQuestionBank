package sample;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.javafx.charts.Legend;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.scene.control.TextField;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.awt.*;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.stage.StageStyle;
import org.antlr.v4.runtime.misc.Triple;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.pdfdom.PDFToHTML;


public class Controller {

    @FXML private TableView topicTable;
    @FXML private TableView paperTable;
    @FXML private TextArea cliboardtxt;
    @FXML private TextArea scrollPositionTxtBox;
    @FXML private TextArea paperTxtBox;
    @FXML private WebView fontWebView;
    @FXML private AnchorPane bankPane;
    @FXML private AnchorPane modifyPaperPane;
    @FXML private AnchorPane questionPane;
    @FXML private TextArea mscrollPositionTxtBox;

    String filename= "Design_technology_paper_1__HL";

    ObservableList<Topic> topics = FXCollections.observableArrayList();
    private TableColumn<Topic, String> subtopicCol = new TableColumn("Subtopic");

    public void initialize() throws IOException {


        //open Json
        Gson gson = new Gson();
        try (Reader reader = new FileReader("topics.json")) {
            // Convert JSON File to Java Object
            ArrayList<Topic> imports = gson.fromJson(reader, new TypeToken<ArrayList<Topic>>() {}.getType());
            topics = FXCollections.observableArrayList(imports);
        } catch (IOException e) {
            e.printStackTrace();
        }

        paperTxtBox.setText(filename);

        topicTable.setEditable(false);
        TableColumn numberCol = new TableColumn("#");
        TableColumn topicCol = new TableColumn("Topic");

        topicTable.getColumns().addAll(numberCol, topicCol,subtopicCol);

        numberCol.setCellValueFactory(
                new PropertyValueFactory<Topic,String>("number"));
        numberCol.setCellFactory(TextFieldTableCell.forTableColumn());
        numberCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Topic, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Topic, String> t) {
                        ((Topic) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setNumber(t.getNewValue());
                    }
                }
        );
        topicCol.setCellValueFactory(
                new PropertyValueFactory<Topic,String>("topic"));
        topicCol.setCellFactory(TextFieldTableCell.forTableColumn());
        topicCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Topic, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Topic, String> t) {
                        ((Topic) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setTopic(t.getNewValue());
                    }
                }
        );
        subtopicCol.setCellValueFactory(
                new PropertyValueFactory<Topic,String>( "subtopic"));

        topicTable.setItems(topics);
        setClick(); //lets you click the table.

        paperTable.setEditable(true); //This needs finishing
    }

    /**
     * Question Bank
     */

    public void ModifyPaper(ActionEvent actionEvent) {
    }

    public void AddNewPaper(ActionEvent actionEvent) {
    }

    public void addNewTopic(ActionEvent actionEvent) {
        Dialog<Topic> dialog = new Dialog<>();
        dialog.setTitle("Add new topic");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField numberTextField = new TextField("number");
        TextField topicTextField = new TextField("topic");
        TextField subtopicTextField = new TextField("subtopic");

        dialogPane.setContent(new VBox(8, numberTextField, topicTextField, subtopicTextField));
        Platform.runLater(numberTextField::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if(!numberTextField.getText().equals("")&&!topicTextField.getText().equals("")&&!subtopicTextField.getText().equals("")) {
                if (button == ButtonType.OK) {
                    topics.add(new Topic(numberTextField.getText(), topicTextField.getText(), subtopicTextField.getText()));
                }
            }else{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Incorrect input");
                alert.setHeaderText(null);
                alert.setContentText("All 3 boxes need filling. If there is no subtopic, type the same topic.");
                alert.showAndWait();
            }
            return null;
        });
        Optional<Topic> optionalResult = dialog.showAndWait();
    }

    public void setEdit(){
        subtopicCol.setCellFactory(TextFieldTableCell.forTableColumn());
        subtopicCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Topic, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Topic, String> t) {
                        ((Topic) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setSubtopic(t.getNewValue());
                    }
                }
        );
    }

    public void setClick() {
        subtopicCol.setCellFactory(tc -> {
            TableCell<Topic, String> cell = new TableCell<Topic, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.setOnMouseClicked(e -> {
                if (!cell.isEmpty()) {
                    String userId = cell.getText();
                    System.out.println(userId);
                }
            });

            return cell;
        });
    }

    public void EnableEdit(ActionEvent actionEvent) {
        if(topicTable.isEditable()) {
            topicTable.setEditable(false);
            setClick();
        }else{
            topicTable.setEditable(true);
            setEdit();
        }
    }

    public void saveObjects(ActionEvent actionEvent) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("topics.json")) {
            gson.toJson(topics, writer);
            System.out.println("Saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    /**
     * Modify Paper
     */
    public void deleteBtn(ActionEvent actionEvent) {
    }

    public void addBtn(ActionEvent actionEvent) {
    }

    /**
     * Questions pane
     */
    public void previousBtn(ActionEvent actionEvent) {
    }

    public void nextBtn(ActionEvent actionEvent) {
    }


//Helper functions

    /**
     * Each pane has a return button to return to the main pane.
     */
    public void returnBtn(ActionEvent actionEvent) throws IOException, UnsupportedFlavorException {
        bankPane.setVisible(true);
        questionPane.setVisible(false);
        modifyPaperPane.setVisible(false);
    }

    //Scrolling functions.
    public void scroll() {
        scrollTo(fontWebView,0,Integer.parseInt(scrollPositionTxtBox.getText()));
    }

    public void updateScroll(ScrollEvent scrollEvent) {
    scrollPositionTxtBox.setText(Integer.toString(getVScrollValue(fontWebView)));
    mscrollPositionTxtBox.setText(Integer.toString(getVScrollValue(fontWebView)));
}

    /**
     * Scrolls to the specified position.
     * @param view web view that shall be scrolled
     * @param x horizontal scroll value
     * @param y vertical scroll value
     */
    public void scrollTo(WebView view, int x, int y) {
        view.getEngine().executeScript("window.scrollTo(" + x + ", " + y + ")");
    }

    /**
     * Returns the vertical scroll value, i.e. thumb position.
     * This is equivalent to {@link javafx.scene.control.ScrollBar#getValue().
     * @param view
     * @return vertical scroll value
     */
    public int getVScrollValue(WebView view) {
        return (Integer) view.getEngine().executeScript("document.body.scrollTop");
    }

    /**
     * Returns the horizontal scroll value, i.e. thumb position.
     * This is equivalent to {@link javafx.scene.control.ScrollBar#getValue()}.
     * @param view
     * @return horizontal scroll value
     */
    public int getHScrollValue(WebView view) {
        return (Integer) view.getEngine().executeScript("document.body.scrollLeft");
    }

    /**
     * Returns the maximum vertical scroll value.
     * This is equivalent to {@link javafx.scene.control.ScrollBar#getMax()}.
     * @param view
     * @return vertical scroll max
     */
    public int getVScrollMax(WebView view) {
        return (Integer) view.getEngine().executeScript("document.body.scrollWidth");
    }

    /**
     * Returns the maximum horizontal scroll value.
     * This is equivalent to {@link javafx.scene.control.ScrollBar#getMax()}.
     * @param view
     * @return horizontal scroll max
     */
    public int getHScrollMax(WebView view) {
        return (Integer) view.getEngine().executeScript("document.body.scrollHeight");
    }

    //copying text
    /**
     * This button will copy what is selected to the clipboard and strip it of HTML tags.
     * If text is copied directly (Ctrl+c), there are no spaces between words.
     */
    public void CopySelection(ActionEvent actionEvent) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        final String regex = "[</div>]*[\\n ]*<[^>]+>";
        final String string = (String) fontWebView.getEngine().executeScript(SELECT_HTML);
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(string);

        final StringSelection result = new StringSelection(matcher.replaceAll(" "));
        clipboard.setContents(result,result);
    }

    /**
     * This section retrieves the HTML of the selection.
     */
    public static final String SELECT_HTML
            = "(getSelectionHTML = function () {\n"
            + "      var userSelection;\n"
            + "      if (window.getSelection) {\n"
            + "        // W3C Ranges\n"
            + "        userSelection = window.getSelection ();\n"
            + "        // Get the range:\n"
            + "        if (userSelection.getRangeAt)\n"
            + "          var range = userSelection.getRangeAt (0);\n"
            + "        else {\n"
            + "          var range = document.createRange ();\n"
            + "          range.setStart (userSelection.anchorNode, userSelection.anchorOffset);\n"
            + "          range.setEnd (userSelection.focusNode, userSelection.focusOffset);\n"
            + "        }\n"
            + "        // And the HTML:\n"
            + "        var clonedSelection = range.cloneContents ();\n"
            + "        var div = document.createElement ('div');\n"
            + "        div.appendChild (clonedSelection);\n"
            + "        return div.innerHTML;\n"
            + "      } else if (document.selection) {\n"
            + "        // Explorer selection, return the HTML\n"
            + "        userSelection = document.selection.createRange ();\n"
            + "        return userSelection.htmlText;\n"
            + "      } else {\n"
            + "        return '';\n"
            + "      }\n"
            + "    })()";




}
