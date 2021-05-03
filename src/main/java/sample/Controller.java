package sample;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
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
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.fit.pdfdom.PDFToHTML;



public class Controller {

    @FXML private ListView mpaperSelectListView;
    @FXML private ListView paperList;
    @FXML private ListView mtopicSelectListView;
    @FXML private WebView mfontWebView;
    @FXML private ChoiceBox mpaperChoiceBox;
    @FXML private TableView topicTable;
    @FXML private AnchorPane bankPane;
    @FXML private AnchorPane modifyPaperPane;

    @FXML private ChoiceBox mtopicsChoiceBox;

    String filename= "Design_technology_paper_1__HL";

    ObservableList<Topic> topics = FXCollections.observableArrayList();
    private TableColumn<Topic, String> subtopicCol = new TableColumn("Subtopic");
    private TableColumn<Topic, String> numberCol = new TableColumn("#");
    private TableColumn<Topic, String> topicCol = new TableColumn("Topic");
    private File workingDirectory = new File(System.getProperty("user.dir"));
    private File f = new File(workingDirectory+"/ConvertedExams/");
    ObservableList<String> pathnames = FXCollections.observableArrayList(f.list());
    ListView lastSelectedList = null;
    public String currentPaper;

    public void initialize() throws IOException {
System.out.println(pathnames);
        modifyPaperPane.setVisible(false);
        bankPane.setVisible(true);
        subtopicCol.setSortable(false);
        numberCol.setSortable(false);
        topicCol.setSortable(false);

        topicTable.getColumns().addAll(numberCol, topicCol,subtopicCol);

        numberCol.setCellValueFactory(new PropertyValueFactory<Topic,String>("number"));
        topicCol.setCellValueFactory(new PropertyValueFactory<Topic,String>("topic"));
        subtopicCol.setCellValueFactory(new PropertyValueFactory<Topic,String>( "subtopic"));

        //open and read Json for any previously saved data.
        Gson gson = new Gson();
        try (Reader reader = new FileReader("topics.json")) {
            // Convert JSON File to Java Object
            ArrayList<Topic> imports = gson.fromJson(reader, new TypeToken<ArrayList<Topic>>() {}.getType());
            topics = FXCollections.observableArrayList(imports);
        } catch (IOException e) {
            e.printStackTrace();
        }

        topicTable.setItems(topics);

        paperList.setEditable(false);
        paperList.setItems(pathnames);

        setClick(); //lets you click the table instead of editing it.

        mtopicsChoiceBox.setItems(topics);
        mtopicsChoiceBox.setOnAction(event -> {
                //sets the topic question list on the paper view screen.
                setTopicQuestionsList(mtopicsChoiceBox.getSelectionModel().getSelectedIndex());
               //selects the right paper (this automatically loads the paper too).
                mpaperChoiceBox.getSelectionModel().select(topics.get(mtopicsChoiceBox.getSelectionModel().getSelectedIndex()).getQuestions().get(0).getPaper());
                //scrolls to the right part of the paper.
                scrollTo(mfontWebView,0, topics.get(mtopicsChoiceBox.getSelectionModel().getSelectedIndex()).getQuestions().get(0).getScollLocation());
        });

        mpaperChoiceBox.setItems(pathnames);
        mpaperChoiceBox.setOnAction(event -> {
            try {
                f = new File(workingDirectory+"/ConvertedExams/"+mpaperChoiceBox.getValue());
                String content = Files.asCharSource(f, Charsets.UTF_8).read();
                mfontWebView.getEngine().loadContent(content);
                setPaperQuestionsList(mpaperChoiceBox.getValue().toString());
                scrollTo(mfontWebView,0, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        mpaperSelectListView.setOnMouseClicked(event -> {
            lastSelectedList = mpaperSelectListView;
            String paper = mpaperChoiceBox.getValue().toString();
            int sp = mpaperSelectListView.getFocusModel().getFocusedItem().toString().indexOf(" ")-1;
            String questionN = mpaperSelectListView.getFocusModel().getFocusedItem().toString().substring(0,sp);
            for (Topic t: topics) {
                for (Question q: t.getQuestions()) {
                    if(q.getPaper().equals(paper)&&q.getQuestionNumber().equals(questionN)){
                        scrollTo(mfontWebView,0, q.getScollLocation());
                    }
                }
            }
        });

        mtopicSelectListView.setOnMouseClicked(event -> {
            lastSelectedList = mtopicSelectListView;
            int topic = mtopicsChoiceBox.getSelectionModel().getSelectedIndex();
            int questionN = mtopicSelectListView.getFocusModel().getFocusedIndex();
            String paper = topics.get(topic).getQuestions().get(questionN).getPaper();
            mpaperChoiceBox.getSelectionModel().select(pathnames.indexOf(paper));
            scrollTo(mfontWebView,0, topics.get(topic).getQuestions().get(questionN).getScollLocation());

            mfontWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    // new page has loaded, process:
                    scrollTo(mfontWebView,0, topics.get(topic).getQuestions().get(questionN).getScollLocation());
                }
            });
        });
    }

    public void AddNewPaper(ActionEvent actionEvent) throws IOException {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open exam paper");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF exam papers", "*.pdf"));
        File workingDirectory = new File(System.getProperty("user.dir"));
        fileChooser.setInitialDirectory(workingDirectory);
        File selectedFile = fileChooser.showOpenDialog((Stage) bankPane.getScene().getWindow());

        final FileChooser fileChooserms = new FileChooser();
        fileChooserms.setTitle("Open exam paper");
        fileChooserms.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF markscheme", "*.pdf"));
        fileChooser.setInitialDirectory(workingDirectory);
        File selectedmsFile = fileChooser.showOpenDialog((Stage) bankPane.getScene().getWindow());

        Dialog<Topic> dialog = new Dialog<>();
        dialog.setTitle("Paper details");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Label courseLabel = new Label("Course abbreviation:");
        TextField courseTextField = new TextField();
        Label yearLabel = new Label("Year:");
        TextField yearTextField = new TextField();
        Label sessionLabel = new Label("Session:");
        ChoiceBox sessionChoiceBox = new ChoiceBox();
        sessionChoiceBox.setItems(FXCollections.observableArrayList("May","Nov"));
        Label pLabel = new Label("Session:");
        ChoiceBox pChoiceBox = new ChoiceBox();
        pChoiceBox.setItems(FXCollections.observableArrayList("P1","P2","P3"));
        Label shLabel = new Label("SL or HL:");
        ChoiceBox shChoiceBox = new ChoiceBox();
        shChoiceBox.setItems(FXCollections.observableArrayList("SL","HL"));
        Label paperLabel = new Label("Selected paper:");
        TextField paperTextField = new TextField(selectedFile.getName());
        paperTextField.setEditable(false);
        paperTextField.setMinWidth(300);


        dialogPane.setContent(new VBox(8, courseLabel, courseTextField, yearLabel,yearTextField,sessionLabel,sessionChoiceBox,pLabel,pChoiceBox,shLabel,shChoiceBox,paperLabel,paperTextField));
        Platform.runLater(courseTextField::requestFocus);
        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btOk.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    if(!courseTextField.getText().equals("")&&!yearTextField.getText().equals("")&&!sessionChoiceBox.getSelectionModel().isEmpty()&&!pChoiceBox.getSelectionModel().isEmpty()&&!shChoiceBox.getSelectionModel().isEmpty()) {
                        if (selectedFile != null) {
                            currentPaper = courseTextField.getText()+yearTextField.getText()+"-"+sessionChoiceBox.getValue()+"-"+pChoiceBox.getValue()+shChoiceBox.getValue()+".html";
                            String currentMS = courseTextField.getText()+yearTextField.getText()+"-"+sessionChoiceBox.getValue()+"-"+pChoiceBox.getValue()+shChoiceBox.getValue()+".pdf";
                            PDFToHTML.start(selectedFile.toString(),workingDirectory+"/ConvertedExams/"+currentPaper );
                            try {
                                FileUtils.copyFile(selectedmsFile, new File(workingDirectory + "/Markschemes/" + currentMS));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }else{
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Incorrect input");
                        alert.setHeaderText(null);
                        alert.setContentText("Please check you have filled everything in.");
                        alert.showAndWait();
                        event.consume();

                    }
                });
        Optional<Topic> optionalResult = dialog.showAndWait();

        bankPane.setVisible(false);
        modifyPaperPane.setVisible(true);
        f = new File(workingDirectory+"/ConvertedExams/");
        String[] pathnamesString = f.list();
        pathnames = FXCollections.observableArrayList(pathnamesString);
        mpaperChoiceBox.setItems(FXCollections.observableArrayList(pathnames));
        mpaperChoiceBox.getSelectionModel().select(pathnames.indexOf(currentPaper));
        paperList.setItems(pathnames);

    }

    public void setPaperQuestionsList(String paperName){
        mpaperSelectListView.getItems().clear();
        for (Topic t: topics) {
            for (Question q: t.getQuestions()) {
                if(q.getPaper().equals(paperName)){
                    mpaperSelectListView.getItems().add(q);
                }
            }

        }
    }
    public void setTopicQuestionsList(int topicIndex){
        mtopicSelectListView.getItems().clear();
        for (Question question: topics.get(topicIndex).getQuestions()) {
            mtopicSelectListView.getItems().add(question);
        }
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
        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btOk.addEventFilter(
                ActionEvent.ACTION,
                event -> {
            if(!numberTextField.getText().equals("")&&!topicTextField.getText().equals("")&&!subtopicTextField.getText().equals("")) {
                    topics.add(new Topic(numberTextField.getText(), topicTextField.getText(), subtopicTextField.getText()));
            }else{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Incorrect input");
                alert.setHeaderText(null);
                alert.setContentText("All 3 boxes need filling. If there is no subtopic, type the same topic.");
                alert.showAndWait();
                event.consume();
            }
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
                if (!cell.getTableRow().isEmpty()) {
                    bankPane.setVisible(false);
                    modifyPaperPane.setVisible(true);
                    setTopicQuestionsList(cell.getIndex());
                    mtopicsChoiceBox.getSelectionModel().select(cell.getIndex());
                    mpaperChoiceBox.getSelectionModel().select(topics.get(cell.getIndex()).getQuestions().get(0).getPaper());
                    scrollTo(mfontWebView,0, topics.get(cell.getIndex()).getQuestions().get(0).getScollLocation());
                }
            });

            return cell;
        });

        numberCol.setCellFactory(tc -> {
            TableCell<Topic, String> cell = new TableCell<Topic, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.setOnMouseClicked(e -> {
                if (!cell.isEmpty()) {

                    String paper = topics.get(topicTable.getSelectionModel().getSelectedIndex()).getQuestions().get(0).getPaper();
                    mpaperChoiceBox.getSelectionModel().select(pathnames.indexOf(paper));
                    scrollTo(mfontWebView,0, topics.get(topicTable.getSelectionModel().getSelectedIndex()).getQuestions().get(0).getScollLocation());

                    mfontWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                        if (newState == Worker.State.SUCCEEDED) {
                            // new page has loaded, process:
                            scrollTo(mfontWebView,0, topics.get(topicTable.getSelectionModel().getSelectedIndex()).getQuestions().get(0).getScollLocation());
                        }
                    });
                }
            });

            return cell;
        });
        topicCol.setCellFactory(tc -> {
            TableCell<Topic, String> cell = new TableCell<Topic, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };
            cell.setOnMouseClicked(e -> {
                if (!cell.isEmpty()) {
                    System.out.println(topicTable.getSelectionModel().getSelectedIndex());
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

    public void paperClicked(MouseEvent mouseEvent) throws IOException {
        String selectedPaper = paperList.getSelectionModel().getSelectedItem().toString();
        bankPane.setVisible(false);
        modifyPaperPane.setVisible(true);
        f = new File(workingDirectory+"/ConvertedExams/");

        String[] pathnamesString = f.list();
        pathnames = FXCollections.observableArrayList(pathnamesString);
        mpaperChoiceBox.getSelectionModel().select(pathnames.indexOf(selectedPaper));
        mfontWebView.getEngine().setJavaScriptEnabled(true);
        f = new File(workingDirectory+"/ConvertedExams/"+selectedPaper);
        String content = Files.asCharSource(f, Charsets.UTF_8).read();
        mfontWebView.getEngine().loadContent(content);
        setPaperQuestionsList(mpaperChoiceBox.getValue().toString());
    }


    public void getPaperTopicsList(){
        Dialog<Topic> dialog = new Dialog<>();
        dialog.initModality(Modality.NONE);
        dialog.setTitle("Topics for this paper");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK);

        Label paperLabel = new Label("Question Paper:");
        Label paper = new Label(mpaperChoiceBox.getSelectionModel().getSelectedItem().toString());

        Label topicsLabel = new Label("Topics:");

        ArrayList<String> topicsListArray = new ArrayList<>();
        for (Topic t:topics) {
            for (Question q: t.getQuestions()) {
                if (q.getPaper().equals(mpaperChoiceBox.getSelectionModel().getSelectedItem().toString())){
                    topicsListArray.add(t.toString());
                }
            }
        }

        topicsListArray = removeDuplicates(topicsListArray);
        TextArea topicsList = new TextArea();
        for(String a : topicsListArray){
            topicsList.appendText(a + "\n");
        }

        dialogPane.setContent(new VBox(8, paperLabel, paper,topicsLabel,topicsList));

        Optional<Topic> optionalResult = dialog.showAndWait();
    }

    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {
        // Create a new LinkedHashSet
        Set<T> set = new LinkedHashSet<>();

        // Add the elements to set
        set.addAll(list);

        // Clear the list
        list.clear();

        // add the elements of set
        // with no duplicates to the list
        list.addAll(set);

        // return the list
        return list;
    }

    /**
     * Modify Paper
     */

    public void questionDialog(String QuestionS,ArrayList<Integer> topicIndex){
        Dialog<Topic> dialog = new Dialog<>();
        dialog.initModality(Modality.NONE);
        dialog.setTitle("Add new Question");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY);
        Label paperLabel = new Label("Question Paper:");
        Label paper = new Label(this.mpaperChoiceBox.getSelectionModel().getSelectedItem().toString());

        Label questionLabel = new Label("Question number:");
        TextField Question = new TextField(QuestionS);

        Label scrollLabel = new Label("Scroll position:");
        TextField scroll = new TextField();
        scroll.setText(Integer.toString(getVScrollValue(mfontWebView)));
        scroll.setEditable(false);
        Label scrollInfo = new Label("To edit this, scroll to the question you want to add on the PDF Viewer, then click apply.");
        Label scrollInfo2 = new Label("This is like an anchor so the program knows where the question is in the paper.");
        Label topicLabel = new Label("Topic/s:" );
        ListView topicList = new ListView();
        topicList.setMaxHeight(300);

        ObservableList<CheckBox> topicsCB = FXCollections.observableArrayList();
        int i=1;
        for (Topic t:topics) {
            CheckBox cb=new CheckBox(t.toString());
            for (int ti: topicIndex) {
                if (ti==i){
                    cb.setSelected(true);
                }
            }
            i++;
            topicsCB.add(cb);
        }
        topicList.setItems(topicsCB);

        ArrayList selectedTopics = new ArrayList();
        dialogPane.setContent(new VBox(8, paperLabel, paper, questionLabel,Question,scrollLabel,scrollInfo,scrollInfo2,scroll,topicLabel,topicList));
        Platform.runLater(Question::requestFocus);

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btOk.addEventFilter(
                ActionEvent.ACTION,
                event -> {

                    for (CheckBox cb: topicsCB) {
                        if(cb.isSelected()){
                            selectedTopics.add(cb.getText());
                        }
                    }
                    if(!Question.getText().equals("")&&!scroll.getText().equals("")&&!selectedTopics.isEmpty()) {
                        //delete the question from all topics.

                        ArrayList<Question> found = new ArrayList<>();
                        for (int ti : topicIndex) {
                            for (Question q : topics.get(ti-1).getQuestions()) {
                                if (q.getQuestionNumber().equals(QuestionS) && q.getPaper().equals(paper.getText())) {
                                    found.add(q);
                                }
                            }
                            topics.get(ti-1).questions.removeAll(found);
                        }


                        //Create the new question
                        for (Object item:selectedTopics) {
                            for (Topic t:topics){
                                if(item.equals(t.toString())){
                                    t.addQuestion(paper.getText(), Question.getText(),Integer.parseInt(scroll.getText()));
                                    Collections.sort(t.questions, new Sortbyquestion());
                                    //setTopicQuestionsList(mtopicsChoiceBox.getSelectionModel().getSelectedIndex());
                                    setPaperQuestionsList(mpaperChoiceBox.getValue().toString());

                                    try {
                                        saveObjects(new ActionEvent());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }else{
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Incorrect input");
                        alert.setHeaderText(null);
                        alert.setContentText("Make sure everything is filled in correctly.");
                        alert.showAndWait();
                        event.consume();
                    }
                }
        );

        final Button btApply = (Button) dialog.getDialogPane().lookupButton(ButtonType.APPLY);
        btApply.addEventFilter(
                ActionEvent.ACTION,
                event -> {

                    System.out.println("Scroll updated");
                    scroll.setText(Integer.toString(getVScrollValue(mfontWebView)));
                    event.consume();
                }
        );
        Optional<Topic> optionalResult = dialog.showAndWait();
    }

    public void ModifyQuestionBtn(ActionEvent actionEvent) {
        int sp = lastSelectedList.getFocusModel().getFocusedItem().toString().indexOf(" ")-1;
        String questionN = lastSelectedList.getFocusModel().getFocusedItem().toString().substring(0,sp);

        ArrayList<Integer> topicIndex = new ArrayList();
        int i = 0;
        for (Topic t: topics) {
            i++;
            for (Question q: t.getQuestions()) {
                if(q.getQuestionNumber().equals(questionN)){
                    topicIndex.add(i);
                }
            }
        }
        questionDialog(questionN,topicIndex) ;
    }

    public void addQuestion(ActionEvent actionEvent) {
        ArrayList<Integer> topicIndex = new ArrayList();
        questionDialog("",topicIndex) ;
    }

    /**
     * Each pane has a return button to return to the main pane.
     */
    public void returnBtn(ActionEvent actionEvent) throws IOException, UnsupportedFlavorException {
        bankPane.setVisible(true);
        modifyPaperPane.setVisible(false);
    }

    //Scrolling functions.

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
        final String string = (String) mfontWebView.getEngine().executeScript(SELECT_HTML);
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

//shows the mark scheme for the current paper
    public void ShowMS(ActionEvent actionEvent) throws IOException {
        String myFile = mpaperChoiceBox.getValue().toString().substring(0,mpaperChoiceBox.getValue().toString().length()-5)+".pdf";
        Desktop.getDesktop().open(new File(workingDirectory+"/Markschemes/"+myFile));
    }
}
