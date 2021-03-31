package sample;

import com.sun.javafx.charts.Legend;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    ObservableList<Topic> topics = FXCollections.observableArrayList(
    new Topic("1.1",	"Systems in organizations","Planning and system installation"),
    new Topic("1.1",	"Systems in organizations","User focus"),
    new Topic("1.1",	"Systems in organizations","System backup"),
    new Topic("1.1",	"Systems in organizations","Software development"));

    public void initialize() throws IOException {
        //PDFToHTML.start(filename+".pdf", filename+".html");
        paperTxtBox.setText(filename);

       // topics.add(new Topic("1.1",	"Systems in organizations","Planning and system installation"));
        //topics.add(new Topic("1.1",	"Systems in organizations","User focus"));
       // topics.add(new Topic("1.1",	"Systems in organizations","System backup"));
       // topics.add(new Topic("1.1",	"Systems in organizations","Software development"));
        topicTable.setEditable(false);
        TableColumn numberCol = new TableColumn("#");
        TableColumn topicCol = new TableColumn("Topic");
        TableColumn subtopicCol = new TableColumn("Subtopic");

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
                new PropertyValueFactory<Topic,String>("subtopic"));
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

        topicTable.setItems(topics);





        //fontWebView.getEngine().setJavaScriptEnabled(true);
        //File f = new File(filename+".html");
        //String content = Files.asCharSource(f, Charsets.UTF_8).read();
        //fontWebView.getEngine().loadContent(content);


        paperTable.setEditable(true);
    }

    public void scroll() {
        scrollTo(fontWebView,0,Integer.parseInt(scrollPositionTxtBox.getText()));
    }

    public static StringBuilder scrollWebView(int xPos, int yPos) {
        StringBuilder script = new StringBuilder().append("<html>");
        script.append("<head>");
        script.append("   <script language=\"javascript\" type=\"text/javascript\">");
        script.append("       function toBottom(){");
        script.append("           window.scrollTo(" + xPos + ", " + yPos + ");");
        script.append("       }");
        script.append("   </script>");
        script.append("</head>");
        script.append("<body onload='toBottom()'>");
        return script;
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
     * Each pane has a return button
     */
    public void returnBtn(ActionEvent actionEvent) throws IOException, UnsupportedFlavorException {
        bankPane.setVisible(true);
        questionPane.setVisible(false);
        modifyPaperPane.setVisible(false);
    }

    /**
     * Modify Paper pane buttons
     */
    public void deleteBtn(ActionEvent actionEvent) {
    }

    public void addBtn(ActionEvent actionEvent) {
    }

    /**
     * Questions pane buttons
     */
    public void previousBtn(ActionEvent actionEvent) {
    }

    public void nextBtn(ActionEvent actionEvent) {
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

    public void CopySelection(ActionEvent actionEvent) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        final String regex = "[</div>]*[\\n ]*<[^>]+>";
        final String string = (String) fontWebView.getEngine().executeScript(SELECT_HTML);
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(string);

        final StringSelection result = new StringSelection(matcher.replaceAll(" "));
        clipboard.setContents(result,result);
    }

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
            if (button == ButtonType.OK) {
                topics.add(new Topic(numberTextField.getText(), topicTextField.getText(), subtopicTextField.getText()));
            }
            return null;
        });
        Optional<Topic> optionalResult = dialog.showAndWait();

    }

    public void EnableEdit(ActionEvent actionEvent) {
        topicTable.setEditable(true);
    }
}
