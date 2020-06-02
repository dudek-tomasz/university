package sample.services;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import sample.Task;
import sample.enums.TaskPriority;
import sample.enums.TaskState;


public class TaskService{

    ObservableList<Task> taskList = FXCollections.observableArrayList();

    public TaskService() {
        Task task = new Task();
        task.setTitle("11111");
        task.setPriority(TaskPriority.NORMAL);



        Task task2 = new Task();
        task2.setTitle("22222");
        task2.setPriority(TaskPriority.HIGH);
        task2.setTaskState(TaskState.DONE);

        Task task3 = new Task();
        task3.setTitle("33333");
        task3.setPriority(TaskPriority.HIGH);
        task3.setTaskState(TaskState.IN_PROGRESS);

        taskList.add(task);
        taskList.add(task2);
        taskList.add(task3);
    }

    public void addTask(Task task){
        taskList.add(task);
    }

    public void deleteTask(Task task){
        taskList.remove(task);
    }

    public ObservableList<Task> getTaskList() {
        return taskList;
    }

//    public void setListenerToTaskList(ListChangeListener lcs){
//        taskList.addListener(lcs);
//    }


}
