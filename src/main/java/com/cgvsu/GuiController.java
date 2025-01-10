package com.cgvsu;

import com.cgvsu.deletevertex.DeleteVertex;
import com.cgvsu.math.typesMatrix.Matrix4f;
import com.cgvsu.math.typesVectors.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.ModelTransformer;
import com.cgvsu.model.TranslationModel;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objreader.ObjWriter;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.RenderEngine;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class GuiController {
    private static double[][] zBuffer;

    final private float TRANSLATION = 0.5F;



    //Поля для управления мышкой

    private double startX;
    private double startY;

    private Timeline timeline;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML
    private TabPane settingsTab;

    @FXML
    private Button showSettingsButton;

    private Model oldModel = null;

    private Model triangulatedModel = null;

    private boolean triangulated = false;

    private List<Model> models = new ArrayList<>();
    private Model curModel;


    private boolean polyGrid = false;
    private boolean fillTriangles = true;

    Color[] colors = new Color[]{Color.WHITE, Color.WHITE, Color.WHITE};


    //кнопки моделей
    public AnchorPane modelPane;
    private List<Button> addedButtonsModel = new ArrayList<>();
    //кнопки удаления моделей
    private List<Button> deletedButtonsModel = new ArrayList<>();

    @FXML
    private ColorPicker colorPicker;

    //кнопочки для камеры
    public AnchorPane cameraPane;
    private List<Button> addedButtonsCamera = new ArrayList<>();
    private List<Button> deletedButtonsCamera = new ArrayList<>();


    //Для перемещения изменения масштаба и тд модели
    public TextField Sx;
    public TextField Sy;
    public TextField Sz;
    public TextField Tx;
    public TextField Ty;
    public TextField Tz;
    public TextField Rx;
    public TextField Ry;
    public TextField Rz;
/*    public TextField Shxy;
    public TextField Shxz;
    public TextField Shyx;
    public TextField Shyz;
    public TextField Shzx;
    public TextField Shzy;*/
    public Button convert;

    //bpvtytybz

    //для добавления камеры
    public TextField eyeX;
    public TextField targetX;
    public TextField eyeY;
    public TextField targetY;
    public TextField eyeZ;
    public TextField targetZ;



    private List<Camera> camerasList = new ArrayList<>();
    private Camera curCamera;


    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        addNewCamera(new Vector3f(0, 0, 100), new Vector3f(0, 0, 0));
        curCamera = camerasList.get(0);
        addCameraButtons();

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();
            zBuffer = new double[(int) width][(int) height];
            for (double[] doubles : zBuffer) {
                Arrays.fill(doubles, Double.POSITIVE_INFINITY);
            }

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            curCamera.setAspectRatio((float) (width / height));
            canvas.setOnMousePressed(this::handleMousePressed);
            canvas.setOnMouseDragged(this::handleMouseDragged);
            canvas.setOnScroll(this::mouseCameraZoom);

            if (models != null) {
                for (Model model : models) {
                    canvas.getGraphicsContext2D().setStroke(Color.WHITE);
                    RenderEngine.render(canvas.getGraphicsContext2D(), curCamera, model, (int) width, (int) height,
                            zBuffer, polyGrid, fillTriangles, colors);
                }
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        // Determine the path to the target directory dynamically
        String relativePath = "src/main/resources/com/cgvsu/models";
        File initialDirectory = new File(System.getProperty("user.dir"), relativePath);

        fileChooser.setInitialDirectory(initialDirectory);

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            oldModel = ObjReader.read(fileContent);
            triangulatedModel = ObjReader.read(fileContent);
            // Триангуляция и расчёт нормалей
            triangulatedModel.triangulate();
            triangulatedModel.normalize();
            models.add(triangulatedModel);
            curModel = triangulatedModel;
            addModelButtons();
        } catch (IOException exception) {
            showError("Ошибка чтения файла", "Не удалось прочитать файл"+ exception.getMessage());
        }
        catch (Exception exception) {
            showError("Неизвестная ошибка", "Произошла неизвестная ошибка" + exception.getMessage());
        }
    }
    @FXML
    public void saveModel(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Объектные файлы", "*.obj"));
        File file = fileChooser.showSaveDialog(null);

        if (models != null) {
            if (file != null) {
                String filename = file.getAbsolutePath();
                ObjWriter.write(curModel, filename);  // Сохраняем модель

                System.out.println("Модель сохранена в файл: " + filename);
            } else {
                showError("Ошибка сохранения", "Введите имя");
            }
        }
        else {
            showError("Ошибка сохранения", "Нет модели");
        }
    }
    // Метод для отображения сообщения об ошибке
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void delvertex(ActionEvent actionEvent){
        // Создаем диалог для ввода списка вершин
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Удалить вершины");
        dialog.setHeaderText("Введите ID вершин для удаления (через запятую):");
        dialog.setContentText("Вершины:");

        // Отображаем диалог и ждем ответа
        String result = dialog.showAndWait().orElse("");

        if (!result.isEmpty()) {
            // Разделяем введенные данные на список вершин и удаляем лишние пробелы
            String[] verticesArray = result.split(",");
            List<Integer> verticesToDelete = new ArrayList<>();

            // Преобразуем строки в целые числа и добавляем в список
            for (String vertexStr : verticesArray) {
                try {
                    verticesToDelete.add(Integer.parseInt(vertexStr.trim()));
                } catch (NumberFormatException e) {
                    showError("Ошибка ввода", "Некоторые элементы не являются целыми числами.");
                    return; // Выход из метода, если был неправильный ввод
                }
            }
            boolean flag1 = askForFlag("Удалять нормали?");
            boolean flag2 = askForFlag("Удалять текстурные вершины?");

            // Создаем экземпляр DeleteVertex для удаления вершин
            DeleteVertex.deleteVertex(curModel, verticesToDelete, flag1, flag2);
            activeModelnull();
        } else {
            showError("Индексы отсутствуют", "Введите индексы вершин.");
        }
    }

    private void activeModelnull() {
        if (curModel.getVertices().isEmpty()) {
            models.remove(curModel);//удаляем из общ списка
            if (!models.isEmpty()) {
                curModel = models.get(models.size() - 1);
            }
        }
    }
    // Метод для запроса флажка true/false для каждой вершины
    private boolean askForFlag(String headerText) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Да", "Да", "Нет");
        dialog.setTitle("Выбор флажка");
        dialog.setHeaderText(headerText);
        dialog.setContentText("Выберите флажок:");

        // Отображаем диалог и ждем ответа
        String result = dialog.showAndWait().orElse("false");

        return result.equals("true");
    }

    public void togglePolygonalGrid(ActionEvent actionEvent) {
        polyGrid = !polyGrid;
    }

    public void toggleTriangleFill(ActionEvent actionEvent) {
         fillTriangles = !fillTriangles;
    }


    public void chooseModel(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Какую модель выбрать?");
        dialog.setHeaderText("Введите ID модели:");
        dialog.setContentText("Модель:");

        // Отображаем диалог и ждем ответа
        String result = dialog.showAndWait().orElse("");

        if (!result.isEmpty()) {
            try {
                curModel = models.get(Integer.parseInt(result) - 1);
            } catch (NumberFormatException e) {
                showError("Ошибка ввода", "Элемент не является целым числом.");
            }
            catch (IndexOutOfBoundsException e){
                showError("Ошибка выбора модели","Индекса такой модели нет");
            }
        }
    }

    public void DeleteModel(ActionEvent actionEvent) {
        models.remove(curModel);
        if (!models.isEmpty()) {
            curModel = models.get(models.size() - 1);
        } else {
            curModel = null;
        }
    }

    public void lightning(ActionEvent actionEvent) {
        curModel.isActiveLighting = !curModel.isActiveLighting;
        System.out.println("Lighting toggled: " + curModel.isActiveLighting);
        renderScene(); // Метод для обновления сцены после изменения.
    }

    public void renderScene() {
        if (curModel != null && curCamera != null) {
            RenderEngine.render(
                    canvas.getGraphicsContext2D(),
                    curCamera,
                    curModel,
                    (int) canvas.getWidth(),
                    (int) canvas.getHeight(),
                    zBuffer,
                    polyGrid,           // Глобальная переменная для сетки
                    fillTriangles,      // Глобальная переменная для заливки треугольников
                    colors              // Массив цветов
            );
        } else {
            System.out.println("Rendering skipped: No model or camera.");
        }
    }



    public void texture(ActionEvent actionEvent) {

        if(!curModel.isActiveTexture){
           curModel.pathTexture = texturePathPopup((Stage) canvas.getScene().getWindow());
        }
        toggleTexture();
        System.out.println(curModel.pathTexture);
    }

    public void toggleTexture(){
        curModel.isActiveTexture = !curModel.isActiveTexture;
    }

    public static String texturePathPopup(Stage stage) {
        FileChooser fileChooser = new FileChooser();


        fileChooser.setTitle("Select Texture");

        // Determine the path to the target directory dynamically
        String relativePath = "src/main/resources/com/cgvsu/textures";
        File initialDirectory = new File(System.getProperty("user.dir"), relativePath);

        // Set the initial directory for the FileChooser
        if (initialDirectory.exists()) {
            fileChooser.setInitialDirectory(initialDirectory);
        } else {
            System.out.println("Initial directory does not exist: " + initialDirectory.getAbsolutePath());
        }

        // Add filters for the file types you want to allow (e.g., image files)
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp");
        fileChooser.getExtensionFilters().add(imageFilter);


        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {

            return selectedFile.getAbsolutePath();
        } else {
            return null;
        }
    }





    public void addNewCamera(Vector3f cameraPos, Vector3f targetPos){
        camerasList.add(new Camera(cameraPos, targetPos, 1.0F, 1, 0.01F, 100));
        curCamera = camerasList.get(camerasList.size() - 1);

    }
    public void deleteCamera(int index){
        // Предполагается что индекс у камер будет начинаться с 1
        index -= 1;
        if (camerasList.size() == 1){
            return;
        }
        if (curCamera == camerasList.get(index)){
            curCamera = camerasList.get(index - 1);
        }
        camerasList.remove(index);
        cameraPane.getChildren().remove(addedButtonsCamera.get(index));//удаление кнопок с камерой
        cameraPane.getChildren().remove(deletedButtonsCamera.get(index));
        addedButtonsCamera.remove(index);//удаление ссылок на кнопки
        deletedButtonsCamera.remove(index);
        index = 1;
        for (Button button: addedButtonsCamera){
            button.setText("Камера " + index);
            //установка расположения кнопочек
            if (index != 1) {
                button.setLayoutY((!addedButtonsCamera.isEmpty()) ?
                        addedButtonsCamera.get(index - 2).getLayoutY() + 70 :
                        245);
                deletedButtonsCamera.get(index - 1).setLayoutY((!addedButtonsCamera.isEmpty()) ?
                        addedButtonsCamera.get(index - 2).getLayoutY() + 70 :
                        245);
            }
            index++;
        }
    }

    public void setCurCamera(int index){
        index -= 1;
        curCamera = camerasList.get(index);
    }

    @FXML
    public void mouseCameraZoom(ScrollEvent scrollEvent) {
        curCamera.mouseCameraZoom(scrollEvent.getDeltaY());
    }

    private void handleMousePressed(MouseEvent mouseEvent) {
        startX = mouseEvent.getX();
        startY = mouseEvent.getY();
    }

    @FXML
    public void mouseCameraOrbit(MouseEvent mouseEvent) {
        curCamera.mouseOrbit(startX - mouseEvent.getX(), startY - mouseEvent.getY());
        startX = mouseEvent.getX();
        startY = mouseEvent.getY();
    }

    @FXML
    public void mouseCameraMove(MouseEvent mouseEvent) {
        curCamera.mousePan(startX - mouseEvent.getX(), startY - mouseEvent.getY());
        startX = mouseEvent.getX();
        startY = mouseEvent.getY();
    }

    private void handleMouseDragged(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) { // Правая кнопка
            mouseCameraOrbit(mouseEvent);
        } else if (mouseEvent.getButton() == MouseButton.MIDDLE) { // Средняя кнопка
            mouseCameraMove(mouseEvent);
        }
    }

    public void toggleSettings(ActionEvent actionEvent) {
        settingsTab.setVisible(!settingsTab.isVisible());
        String arrow = (settingsTab.isVisible()) ? "<" : ">";
        showSettingsButton.setText(arrow);
        showSettingsButton.setTranslateX(-325 - showSettingsButton.getTranslateX());
    }


    public void addModelButtons() {
        Button addButton = new Button("Модель " + (addedButtonsModel.size() + 1));
        addButton.setLayoutY((addedButtonsModel.size() > 0) ?
               deletedButtonsModel.get(deletedButtonsModel.size()-1).getLayoutY() + 50 :
                20);
        addButton.setLayoutX(20);
        addedButtonsModel.add(addButton);



        modelPane.getChildren().add(addButton);


        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                setActiveModel(addedButtonsModel.indexOf(addButton));
                setActiveModelColor(addButton);

            }
        });
    }

    public void setActiveModelColor(Button addButton){
        for (Button button : addedButtonsModel) {
            if(button.equals(addButton)) button.setStyle("-fx-background-color: #c9c9c9");
            else button.setStyle("-fx-background-color: #aeaeeb");

        }
    }

    public void setActiveModel(int index){curModel = models.get(index);}

    public void handleColorPickerAction(){
        colors = new Color[]{colorPicker.getValue(), colorPicker.getValue(), colorPicker.getValue()}; //TODO aaaaaaaaaaaa
    }

    public void addCameraButtons() {
        Button addButton = new Button("Камера " + (addedButtonsCamera.size() + 1));
        addButton.setLayoutY((!addedButtonsCamera.isEmpty()) ?
                addedButtonsCamera.get(addedButtonsCamera.size() - 1).getLayoutY() + 70 :
                245);
        addButton.setLayoutX(20);
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setCurCamera(Integer.parseInt(addButton.getText().replace("Камера ", "")));
            }
        });
        addedButtonsCamera.add(addButton);

        Button deleteButton = new Button("Удалить");
        deleteButton.setLayoutY(addedButtonsCamera.get(addedButtonsCamera.size() - 1).getLayoutY());
        deleteButton.setLayoutX(addedButtonsCamera.get(addedButtonsCamera.size() - 1).getLayoutX() + 85);
        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                deleteCamera(Integer.parseInt(addButton.getText().replace("Камера ", "")));
            }
        });
        deletedButtonsCamera.add(deleteButton);



        cameraPane.getChildren().add(addButton);
        cameraPane.getChildren().add(deleteButton);

    }

    // тут добавляется камера
    public void createCamera(MouseEvent mouseEvent) {
        Vector3f pos = new Vector3f(Float.parseFloat(eyeX.getText()),
                Float.parseFloat(eyeY.getText()), Float.parseFloat(eyeZ.getText()));
        Vector3f targetPos = new Vector3f(Float.parseFloat(targetX.getText()),
                Float.parseFloat(targetY.getText()), Float.parseFloat(targetZ.getText()));
        addCameraButtons();
        addNewCamera(pos, targetPos);

    }
    //кнопочка преобразовать тут её функция при нажатии //TODO добавить правильный выбор моделей
    public void convert(MouseEvent mouseEvent) {
        if (Objects.equals(Tx.getText(), "") || Objects.equals(Ty.getText(), "") || Objects.equals(Tz.getText(), "")
        || Objects.equals(Sx.getText(), "") || Objects.equals(Sy.getText(), "") || Objects.equals(Sz.getText(), "")) {
            showError("Ошибка", "Введите необходимые данные!");
        } else {
            Matrix4f transposeMatrix = ModelTransformer.modelMatrix(
                    Double.parseDouble(Tx.getText()), Double.parseDouble(Ty.getText()), Double.parseDouble(Tz.getText()),
                    Double.parseDouble(Rx.getText()), Double.parseDouble(Ry.getText()), Double.parseDouble(Rz.getText()),
                    Double.parseDouble(Sx.getText()), Double.parseDouble(Sy.getText()), Double.parseDouble(Sz.getText()));
                    /*Double.parseDouble(Shxy.getText()), Double.parseDouble(Shxz.getText()), Double.parseDouble(Shyx.getText()),
                    Double.parseDouble(Shyz.getText()), Double.parseDouble(Shzx.getText()), Double.parseDouble(Shzy.getText()));*/
            TranslationModel.move(transposeMatrix, curModel);
            //System.out.println(transposeMatrix.toString());
            Tx.setText("0");
            Ty.setText("0");
            Tz.setText("0");

            Sx.setText("1");
            Sy.setText("1");
            Sz.setText("1");
            //маша новая аффиное преоброзование поворот
            Rx.setText("0");
            Ry.setText("0");
            Rz.setText("0");

       /*     Shxy.setText("0");
            Shxz.setText("0");
            Shyx.setText("0");
            Shyz.setText("0");
            Shzx.setText("0");
            Shzy.setText("0");*/

        }
    }


    //кнопочка перенести в начало координат
    public void MoveToTheOrigin(ActionEvent actionEvent) {
        Vector3f center = curModel.getCenter().multiplied(-1);
        Matrix4f transposeMatrix = ModelTransformer.translateMatrix(center.getX(), center.getY(), center.getZ());
        TranslationModel.move(transposeMatrix, curModel);
    }

    public void Rotate90x(ActionEvent actionEvent) {
        Matrix4f transposeMatrix = ModelTransformer.rotateMatrix(90, 0, 0);
        TranslationModel.move(transposeMatrix, curModel);
    }

    public void Rotate90y(ActionEvent actionEvent) {
        Matrix4f transposeMatrix = ModelTransformer.rotateMatrix(0, 90, 0);
        TranslationModel.move(transposeMatrix, curModel);
    }

    public void Rotate90z(ActionEvent actionEvent) {
        Matrix4f transposeMatrix = ModelTransformer.rotateMatrix(0, 0, 90);
        TranslationModel.move(transposeMatrix, curModel);
    }
    //Увеличить в 2 раза
    public void increase2(ActionEvent actionEvent) {
        Matrix4f transposeMatrix = ModelTransformer.scaleMatrix(2, 2, 2);
        TranslationModel.move(transposeMatrix, curModel);
    }
    //Уменьшить в 2 раза
    public void reduce2(ActionEvent actionEvent) {
        Matrix4f transposeMatrix = ModelTransformer.scaleMatrix(0.5, 0.5, 0.5);
        TranslationModel.move(transposeMatrix, curModel);
    }
}