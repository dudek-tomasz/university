package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import sample.enums.TaskPriority;
import sample.enums.TaskState;
import sample.services.TaskService;
import sample.utils.ReferencesContainer;

import java.net.URL;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AddNewTaskController implements Initializable {

    Stage stage;

    boolean isEdit = false;

    Task task;

    @FXML
    ComboBox priorityComboBox;

    TaskService taskService;

    @FXML
    private TextArea taskTextField;
    @FXML
    private TextField titleTextField;
    @FXML
    private DatePicker datePicker;

    @FXML Button addButton;
    @FXML Button editButton;


    public AddNewTaskController() {
        this.taskService = ReferencesContainer.getInstance().getTaskService();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setEdit(boolean edit) {
        isEdit = edit;

        if(isEdit){
            addButton.setVisible(false);
        } else {
            editButton.setVisible(true);
        }
    }

    public void setTask(Task task) {
        this.task = task;

        taskTextField.setText(task.getTaskText());
        titleTextField.setText(task.getTitle());
        if(task.getDate() != null){
            datePicker.setValue(task.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        if(task.getPriority() != null){
            priorityComboBox.setValue(task.getPriority().toString());
        }
    }

    public void cancel(){
        stage.close();
    }

    public void addTask(){
        Task task = new Task();
        task.setTitle(titleTextField.getText());

        task.setPriority(TaskPriority.valueOf((String)priorityComboBox.getValue()));

        task.setTaskText(taskTextField.getText());
        task.setDate(Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        task.setTaskState(TaskState.TODO);
        taskService.addTask(task);
        stage.close();
}

    public void editTask(){
        task.setTitle(titleTextField.getText());
        task.setPriority(TaskPriority.valueOf((String)priorityComboBox.getValue()));
        task.setTaskText(taskTextField.getText());
        if(datePicker.getValue() != null){
            task.setDate(Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        }
        taskService.deleteTask(task);
        taskService.addTask(task);
        stage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<TaskPriority> taskPriorities1 = Arrays.asList(TaskPriority.values());

        ObservableList<String> priorityList = FXCollections.observableArrayList();
        priorityList.addAll(taskPriorities1.stream().map(Enum::toString).collect(Collectors.toList()));

        priorityComboBox.setItems(priorityList);
        priorityComboBox.setValue(TaskPriority.NORMAL.toString());
    }
}
