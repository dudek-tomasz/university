package sample;

import com.sun.xml.internal.bind.v2.TODO;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.Background;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import sample.enums.TaskPriority;
import sample.enums.TaskState;
import sample.services.TaskService;
import sample.utils.ReferencesContainer;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class MainController implements Initializable {
    private ScrollPane toDo;
    private static String Separator = "~";
    TaskService taskService;
    ObservableList <Task>tmpTaskList=FXCollections.observableArrayList();

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ObjectProperty<ListCell<Task>> dragSource = new SimpleObjectProperty<>();
    private final ObjectProperty<ListView<Task>> dragListViewSource = new SimpleObjectProperty<>();

    @FXML
    private ListView<Task> todoTaskList;

    @FXML
    private ListView<Task> inProgressTaskList;

    @FXML
    private ListView<Task> doneTaskList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        taskService = ReferencesContainer.getInstance().getTaskService();
        refreshLists();

        taskService.getTaskList().addListener((ListChangeListener<Task>) c -> {
            refreshLists();
        });

        todoTaskList.setCellFactory(lv -> createListCell());


        inProgressTaskList.setCellFactory(lv -> createListCell());
        doneTaskList.setCellFactory(lv -> createListCell());

    }

    private ListCell<Task> createListCell() {
        ListCell<Task> cell = new ListCell<Task>() {
            @Override
            public void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                setText(task != null ? task.getTitle() : "");
                setStyle("-fx-control-inner-background: " + "none" + ";");
                if (task != null) {
                    setTooltip(createTooltip(task));

                    if (task.getPriority() == TaskPriority.HIGH) {
                        setStyle("-fx-control-inner-background: " + "derive(red, 50%)" + ";");
                    }
                }
            }
        };

        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem();
        editItem.setText("Edit");
        editItem.setOnAction(event -> {
            System.out.println("EDIT");
            editTask(cell.getItem());
        });
        MenuItem deleteItem = new MenuItem();
        deleteItem.setText("Delete");
        deleteItem.setOnAction(event -> taskService.deleteTask(cell.getItem()));
        contextMenu.getItems().addAll(editItem, deleteItem);

        cell.setContextMenu(contextMenu);

        cell.setOnMouseEntered(event -> {
            cell.setTooltip(createTooltip(cell.getItem()));
        });

        cell.setOnDragDetected(event -> {
            if (!cell.isEmpty()) {
                Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.putString(cell.getItem().getTitle());
                db.setContent(cc);
                dragSource.set(cell);
                dragListViewSource.set(cell.getListView());
            }
        });

        cell.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();

            if (db.hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
        });

        cell.setOnDragDone(event -> {
            refreshLists();
        });

        cell.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString() && dragSource.get() != null && dragListViewSource.get() != cell.getListView()) {

                ListCell<Task> dragSourceCell = dragSource.get();
                if (cell.getListView() == doneTaskList) {
                    dragSourceCell.getItem().setTaskState(TaskState.DONE);

                } else if (cell.getListView() == inProgressTaskList) {
                    dragSourceCell.getItem().setTaskState(TaskState.IN_PROGRESS);
                } else if (cell.getListView() == todoTaskList) {
                    dragSourceCell.getItem().setTaskState(TaskState.TODO);
                }

                event.setDropCompleted(true);
                dragSource.set(null);
                refreshLists();
            } else {
                event.setDropCompleted(false);
            }
        });


        return cell;
    }

    public void menuClose() {
        System.exit(0);
    }

    public void menuAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText("Kanban in JavaFX ver 1.0");
        alert.setContentText("Application created by Tomasz Dudek");

        alert.showAndWait();
    }

    public void menuSave() {
        Stage secondaryStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Saving Kanban");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(secondaryStage);
        try {
            // write object to file
            BufferedWriter fos = Files.newBufferedWriter(Paths.get(file.getPath()));

            for (Task task : taskService.getTaskList()) {
                String row = task.getTaskState() + Separator +
                        task.getTitle() + Separator +
                        task.getPriority() + Separator +
                        task.getDate() + Separator +
                        task.getTaskText() + Separator;
                fos.write(row + "\n");

            }
            fos.flush();
            fos.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void menuOpen() {
        BufferedReader fileReader=null;
        Stage secondaryStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Kanban");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(secondaryStage);
        if(file.canExecute()){taskService.getTaskList().clear();}

        try {


                        // read object from file
            fileReader = new BufferedReader(new FileReader(file.getPath()));
            if(fileReader.lines()==null){throw new IOException();}
            for (String x : fileReader.lines().collect(Collectors.toList())) {
                String[] splitString = x.split(Separator);
                Task task = new Task();

                if (splitString.length != 5) {
                    System.out.println("length");
                    throw new IOException();
                }

                if (!splitString[0].contentEquals("TODO") && !splitString[0].contentEquals("IN_PROGRESS") && !splitString[0].contentEquals("DONE")) {
                    System.out.println("state");

                    throw new IOException();
                } else if (splitString[0].contentEquals("TODO")) task.setTaskState(TaskState.TODO);
                else if (splitString[0].contentEquals("IN_PROGRESS")) task.setTaskState(TaskState.IN_PROGRESS);
                else if (splitString[0].contentEquals("DONE")) task.setTaskState(TaskState.DONE);

                task.setTitle(splitString[1]);

                if (!splitString[2].contentEquals("LOW") && !splitString[2].contentEquals("NORMAL") && !splitString[2].contentEquals("HIGH")) {
                    throw new IOException();
                } else if (splitString[2].contentEquals("LOW")) task.setPriority(TaskPriority.LOW);
                else if (splitString[2].contentEquals("NORMAL")) task.setPriority(TaskPriority.NORMAL);
                else if (splitString[2].contentEquals("HIGH")) task.setPriority(TaskPriority.HIGH);

                task.setTaskText(splitString[4]);
                taskService.addTask(task);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Nieprawidłowy format lub plik jest pusty");
            alert.showAndWait();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void menuImport() {
        BufferedReader fileReader=null;
        Stage secondaryStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Kanban");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(secondaryStage);

        try {
            // read object from file
            fileReader = new BufferedReader(new FileReader(file.getPath()));
            for (String x : fileReader.lines().collect(Collectors.toList())) {
                String[] splitString = x.split(Separator);
                Task task = new Task();

                if (splitString.length != 5) {
                    System.out.println("length");
                    throw new IOException();
                }

                if (!splitString[0].contentEquals("TODO") && !splitString[0].contentEquals("IN_PROGRESS") && !splitString[0].contentEquals("DONE")) {
                    System.out.println("state");

                    throw new IOException();
                } else if (splitString[0].contentEquals("TODO")) task.setTaskState(TaskState.TODO);
                else if (splitString[0].contentEquals("IN_PROGRESS")) task.setTaskState(TaskState.IN_PROGRESS);
                else if (splitString[0].contentEquals("DONE")) task.setTaskState(TaskState.DONE);

                task.setTitle(splitString[1]);

                if (!splitString[2].contentEquals("LOW") && !splitString[2].contentEquals("NORMAL") && !splitString[2].contentEquals("HIGH")) {
                    throw new IOException();
                } else if (splitString[2].contentEquals("LOW")) task.setPriority(TaskPriority.LOW);
                else if (splitString[2].contentEquals("NORMAL")) task.setPriority(TaskPriority.NORMAL);
                else if (splitString[2].contentEquals("HIGH")) task.setPriority(TaskPriority.HIGH);

                task.setTaskText(splitString[4]);
                taskService.addTask(task);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Nieprawidłowy format");
            alert.showAndWait();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void menuExport() {

        for (Task task:taskService.getTaskList()){
            tmpTaskList.add(task);
        }
        BufferedReader fileReader=null;
        Stage secondaryStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Kanban");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(secondaryStage);
        try {
            // read object from file
            fileReader = new BufferedReader(new FileReader(file.getPath()));
            for (String x : fileReader.lines().collect(Collectors.toList())) {
                String[] splitString = x.split(Separator);
                Task task = new Task();

                if (splitString.length != 5) {
                    System.out.println("length");
                    throw new IOException();
                }

                if (!splitString[0].contentEquals("TODO") && !splitString[0].contentEquals("IN_PROGRESS") && !splitString[0].contentEquals("DONE")) {
                    System.out.println("state");

                    throw new IOException();
                } else if (splitString[0].contentEquals("TODO")) task.setTaskState(TaskState.TODO);
                else if (splitString[0].contentEquals("IN_PROGRESS")) task.setTaskState(TaskState.IN_PROGRESS);
                else if (splitString[0].contentEquals("DONE")) task.setTaskState(TaskState.DONE);

                task.setTitle(splitString[1]);

                if (!splitString[2].contentEquals("LOW") && !splitString[2].contentEquals("NORMAL") && !splitString[2].contentEquals("HIGH")) {
                    throw new IOException();
                } else if (splitString[2].contentEquals("LOW")) task.setPriority(TaskPriority.LOW);
                else if (splitString[2].contentEquals("NORMAL")) task.setPriority(TaskPriority.NORMAL);
                else if (splitString[2].contentEquals("HIGH")) task.setPriority(TaskPriority.HIGH);

                task.setTaskText(splitString[4]);
                taskService.addTask(task);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Nieprawidłowy format");
            alert.showAndWait();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            // write object to file
            BufferedWriter fos = Files.newBufferedWriter(Paths.get(file.getPath()));

            for (Task task : taskService.getTaskList()) {
                String row = task.getTaskState() + Separator +
                        task.getTitle() + Separator +
                        task.getPriority() + Separator +
                        task.getDate() + Separator +
                        task.getTaskText() + Separator;
                fos.write(row + "\n");

            }
            fos.flush();
            fos.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        taskService.getTaskList().clear();
        for (Task task:tmpTaskList) {
            taskService.addTask(task);
        }
        tmpTaskList.clear();
    }

    public void addNewTaskButton() throws Exception {
        Stage secondaryStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("newtask.fxml"));
//            Parent root = FXMLLoader.load(AddNewTaskController.class.getResource("newtask.fxml"));
        Parent root = loader.load();
        ((AddNewTaskController) loader.getController()).setStage(secondaryStage);
        secondaryStage.setTitle("Add new task");
        secondaryStage.setScene(new Scene(root, 417, 400));
        secondaryStage.show();


    }

    public void refreshLists() {
        ObservableList<Task> toList = FXCollections.observableArrayList();
        ObservableList<Task> inProgressList = FXCollections.observableArrayList();
        ObservableList<Task> doneList = FXCollections.observableArrayList();

        taskService.getTaskList().forEach(task -> {
            switch (task.getTaskState()) {
                case TODO:
                    toList.add(task);
                    break;
                case IN_PROGRESS:
                    inProgressList.add(task);
                    break;
                case DONE:
                    doneList.add(task);
                    break;
            }
        });

        todoTaskList.setItems(toList);
        inProgressTaskList.setItems(inProgressList);
        doneTaskList.setItems(doneList);
    }



    public void onDragOver(DragEvent dragEvent) {
        if (dragSource.get() != null) {
            dragEvent.acceptTransferModes(TransferMode.MOVE);
        }
    }

    public void onDragDropped(DragEvent dragEvent) {
        System.out.println("onDragDropped");
        ListCell<Task> dragSourceCell = dragSource.get();

        if (dragEvent.getTarget() == todoTaskList) {
            dragSourceCell.getItem().setTaskState(TaskState.TODO);
        } else if (dragEvent.getTarget() == inProgressTaskList) {
            dragSourceCell.getItem().setTaskState(TaskState.IN_PROGRESS);
        } else if (dragEvent.getTarget() == doneTaskList) {
            dragSourceCell.getItem().setTaskState(TaskState.DONE);
        } else {
            dragEvent.setDropCompleted(false);
        }
        dragEvent.setDropCompleted(true);
    }

    public void onDragDone(DragEvent dragEvent) {
        refreshLists();
    }

    private Tooltip createTooltip(Task task) {
        if (task == null) {
            return new Tooltip("");
        }
        StringBuffer text = new StringBuffer();

        text.append("Title: ");
        text.append(task.getTitle());
        text.append("\n\r");

        text.append("Priority: ");
        text.append(task.getPriority());
        text.append("\n\r");

        text.append("State: ");
        text.append(task.getTaskState().toString());
        text.append("\n\r");

        text.append("Expire date: ");
        text.append(task.getDate() != null ? dateFormat.format(task.getDate()) : "");
        text.append("\n\r");

        text.append("Task text: ");
        text.append(task.getTaskText());
        text.append("\n\r");

        Tooltip tooltip = new Tooltip();
        tooltip.setText(text.toString());
        return tooltip;
    }


    private void editTask(Task task) {
        Stage secondaryStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("newtask.fxml"));
//            Parent root = FXMLLoader.load(AddNewTaskController.class.getResource("newtask.fxml"));
        Parent root = null;
        try {
            root = loader.load();
            AddNewTaskController controller = (AddNewTaskController) loader.getController();
            controller.setStage(secondaryStage);
            controller.setEdit(true);
            controller.setTask(task);
            secondaryStage.setTitle("Edit task");
            secondaryStage.setScene(new Scene(root, 417, 400));
            secondaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}