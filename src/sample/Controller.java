package sample;


import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class Controller {
    @FXML
    private TextField firstFilePath;
    @FXML
    private TextField secondFilePath;
    @FXML
    private TextArea firstMatrixArea;
    @FXML
    private TextArea secondMatrixArea;
    @FXML
    private Button firstFileButton;
    @FXML
    private Button secondFileButton;
    @FXML
    private Button firstVisualizeButton;
    @FXML
    private Button secondVisualizeButton;


    @FXML
    void initialize() {
        setAreasColors(Color.RED);
    }


    @FXML
    void saveInFile() throws IOException {
        if (firstMatrixArea.getText().isBlank() || secondMatrixArea.getText().isBlank()) return;
        File file = findFile();
        if (file == null) return;
        FileWriter fw = new FileWriter(file);
        if (firstMatrixArea.getBorder().getStrokes().get(0).getBottomStroke().equals(Color.RED))
            fw.write("Graphs represented by the matrices below aren't isomorphic\n");
        else
            fw.write("Graphs represented by the matrices below are isomorphic\n");
        fw.write("\nFirst matrix:\n" + firstMatrixArea.getText());
        fw.write("\nSecond matrix:\n" + secondMatrixArea.getText());
        fw.flush();
        fw.close();
    }

    @FXML
    void enterFilePath(KeyEvent event) throws IOException {
        String filePath;
        if (event.getSource().equals(firstFilePath)) {
            filePath = firstFilePath.getText();
            if (isValidFilePath(filePath).length() == 0)
                setDataInArea(1, new File(filePath));
            else {
                firstMatrixArea.clear();
                firstMatrixArea.setPromptText(isValidFilePath(filePath));
                setAreasColors(Color.RED);
            }
        } else if (event.getSource().equals(secondFilePath)) {
            filePath = secondFilePath.getText();
            if (isValidFilePath(filePath).length() == 0)
                setDataInArea(2, new File(filePath));
            else {
                secondMatrixArea.clear();
                secondMatrixArea.setPromptText(isValidFilePath(filePath));
                setAreasColors(Color.RED);
            }
        }
    }

    private String isValidFilePath(String filePath) throws IOException {
        if (filePath.length() < 4)
            return "File path length must be more then 4";
        File file = new File(filePath);
        if (!file.exists())
            return "File doesn't exists";
        if (!filePath.endsWith(".txt"))
            return "This program works only with .txt filed";
        if (readFile(file).isBlank())
            return "File is empty";
        return "";
    }

    private void setAreasColors(Color color) {
        firstMatrixArea.setBorder(new Border(new BorderStroke(color, BorderStrokeStyle.SOLID,
                new CornerRadii(2), new BorderWidths(2))));
        secondMatrixArea.setBorder(new Border(new BorderStroke(color, BorderStrokeStyle.SOLID,
                new CornerRadii(2), new BorderWidths(2))));
    }

    private void setDataInArea(int areaNumber, File file) throws IOException {
        switch (areaNumber) {
            case 1:
                firstMatrixArea.setText(readFile(file));
                break;
            case 2:
                secondMatrixArea.setText(readFile(file));
                break;
        }
        checkTextAreasColors();
    }


    public void browseFile(ActionEvent event) throws IOException {
        File file = findFile();
        if (file == null) return;
        if (event.getSource().equals(firstFileButton)) {
            firstFilePath.setText(file.getPath());
            setDataInArea(1, file);
        } else {
            secondFilePath.setText(file.getPath());
            setDataInArea(2, file);
        }
    }

    private void checkTextAreasColors() {
        if (firstMatrixArea.getText().isBlank() || secondMatrixArea.getText().isBlank()) {
            setAreasColors(Color.RED);
            return;
        }
        LinkedHashMap<Integer, ArrayList<Integer>> firstMatrix = createAdjacencyMatrix(firstMatrixArea.getText());
        LinkedHashMap<Integer, ArrayList<Integer>> secondMatrix = createAdjacencyMatrix(secondMatrixArea.getText());
        if (firstMatrix.size() != secondMatrix.size()) {
            setAreasColors(Color.RED);
            return;
        }
        List<Integer> firstMatrixKeys = new ArrayList<>(firstMatrix.keySet());
        List<Integer> secondMatrixKeys = new ArrayList<>(secondMatrix.keySet());
        boolean isIsomorphic = true;
        for (Map.Entry<Integer, ArrayList<Integer>> firstMatrixRow : firstMatrix.entrySet()) {
            for (int i = 0; i < firstMatrixRow.getValue().size(); i++) {
                if (!firstMatrixRow.getValue().get(i).equals(secondMatrix.get(firstMatrixRow.getKey()).get(secondMatrixKeys.indexOf(firstMatrixKeys.get(i)))))
                    isIsomorphic = false;
            }
        }
        if (isIsomorphic)
            setAreasColors(Color.LIGHTGREEN);
    }

    private LinkedHashMap<Integer, ArrayList<Integer>> createAdjacencyMatrix(String data) {
        LinkedHashMap<Integer, ArrayList<Integer>> adjacencyMatrix = new LinkedHashMap<>();
        for (String row : data.split("\n")) {
            ArrayList<Integer> rowArray = new ArrayList<>();
            String[] rowValues = row.split(" ");
            for (int i = 1; i < rowValues.length; i++) {
                if (!rowValues[i].equals("\\s+"))
                    rowArray.add(Integer.parseInt(rowValues[i]));
            }
            adjacencyMatrix.put(Integer.parseInt(rowValues[0]), rowArray);
        }
        return adjacencyMatrix;
    }

    public void visualize(ActionEvent event) {
        if (event.getSource().equals(firstVisualizeButton) && !firstMatrixArea.getText().isBlank()) {
            LinkedHashMap<Integer, ArrayList<Integer>> matrix = createAdjacencyMatrix(firstMatrixArea.getText());
            Group graph = createGraph(matrix);
            createScene(-200, -100, "First graph", graph);
        } else if (event.getSource().equals(secondVisualizeButton) && !secondMatrixArea.getText().isBlank()) {
            LinkedHashMap<Integer, ArrayList<Integer>> matrix = createAdjacencyMatrix(secondMatrixArea.getText());
            Group graph = createGraph(matrix);
            createScene(200, 100, "Second graph", graph);
        }
    }

    private File findFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text document", "*txt"));
        return fileChooser.showOpenDialog(Main.primaryStage);
    }

    private String readFile(File file) throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader(file));
        StringBuilder fileData = new StringBuilder();
        String currentLine;
        while ((currentLine = bf.readLine()) != null) {
            fileData.append(currentLine);
            fileData.append("\n");
        }
        bf.close();
        return fileData.toString();
    }

    private void createScene(int xOffset, int yOffset, String title, Group graph) {
        Scene visualizeScene = new Scene(graph, 550, 550);
        Stage newWindow = new Stage();
        newWindow.setTitle(title);
        newWindow.setScene(visualizeScene);
        newWindow.initModality(Modality.NONE);
        newWindow.initOwner(Main.primaryStage);
        newWindow.setX(Main.primaryStage.getX() + xOffset);
        newWindow.setY(Main.primaryStage.getY() + yOffset);
        newWindow.setResizable(false);
        newWindow.show();
    }

    private void createVertex(Group graph, double angle, int vertexValue) {
        double vertexX_Coordinate = 300 + 120 * Math.cos(angle);
        double vertexY_Coordinate = 300 + 120 * Math.sin(angle);
        Circle vertexCircle = new Circle(vertexX_Coordinate, vertexY_Coordinate, 30, null);
        vertexCircle.setStroke(Color.RED);
        vertexCircle.setStrokeWidth(3);
        Text vertexValueText = new Text(vertexX_Coordinate - 4, vertexY_Coordinate + 4, Integer.toString(vertexValue));
        vertexValueText.setStroke(Color.RED);
        graph.getChildren().addAll(vertexValueText, vertexCircle);
    }

    private void createLoopEdge(Group graph, double angle, int loopValue) {
        Arc arc = new Arc(300 + 150 * Math.cos(angle), 300 + 150 * Math.sin(angle),
                30, 30, -120 - Math.toDegrees(angle), 240);
        arc.setFill(null);
        arc.setStroke(Color.BLACK);
        arc.setStrokeWidth(2);
        Text arcValue = new Text(300 + 190 * Math.cos(angle),
                300 + 190 * Math.sin(angle), Integer.toString(loopValue));
        graph.getChildren().addAll(arc, arcValue);
    }

    private void createLineEdge(Group graph, int currentVertexIndex, int adjacencyIndex, double angle, int edgeValue) {
        double sourceX = 300 + 90 * Math.cos(currentVertexIndex * angle);
        double sourceY = 300 + 90 * Math.sin(currentVertexIndex * angle);
        double terminalX = 300 + 90 * Math.cos(adjacencyIndex * angle);
        double terminalY = 300 + 90 * Math.sin(adjacencyIndex * angle);
        Line lineEdge = new Line(sourceX, sourceY, terminalX, terminalY);
        Text edgeValueText = new Text((sourceX + terminalX) / 2 - 2,
                (sourceY + terminalY) / 2 - 2, Integer.toString(edgeValue));
        graph.getChildren().addAll(lineEdge, edgeValueText);
    }


    private Group createGraph(LinkedHashMap<Integer, ArrayList<Integer>> adjacencyMatrix) {
        Group graph = new Group();
        List<Integer> keys = new ArrayList<>(adjacencyMatrix.keySet());
        double angle = Math.toRadians((double) 360 / adjacencyMatrix.size());
        for (Integer key : keys) {
            double currentVertexAngle = keys.indexOf(key) * angle;
            createVertex(graph, currentVertexAngle, key);
            if (adjacencyMatrix.get(key).get(keys.indexOf(key)) != 0)
                createLoopEdge(graph, currentVertexAngle, adjacencyMatrix.get(key).get(keys.indexOf(key)));
            int currentAdjacencyIndex = 0;
            for (int adjacencyValue : adjacencyMatrix.get(key)) {
                if (adjacencyValue != 0 && currentAdjacencyIndex != keys.indexOf(key)) {
                    createLineEdge(graph, keys.indexOf(key), currentAdjacencyIndex, angle, adjacencyValue);
                }
                currentAdjacencyIndex++;
            }
        }
        return graph;
    }

    @FXML
    void showInfoAboutAuthor() {
        Alert authorInfo = new Alert(Alert.AlertType.INFORMATION, "Kovalenko Vladislav 951008");
        authorInfo.setTitle("Author");
        authorInfo.setHeaderText("Author information");
        authorInfo.showAndWait();
    }

    @FXML
    void showInfoAboutProgram() {
        Alert programInfo = new Alert(Alert.AlertType.INFORMATION);
        programInfo.setTitle("Program");
        programInfo.setHeaderText("Program information");
        programInfo.setContentText("This program shows green borders in data areas, when both\ngraphs are isomorphic," +
                " and red borders, if something went\nwrong with files or matrices aren't isomorphic");
        programInfo.showAndWait();
    }
}

