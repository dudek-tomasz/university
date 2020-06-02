package sample;

import sample.enums.TaskPriority;
import sample.enums.TaskState;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {
    private String title;
    private TaskPriority priority;
    private Date date;
    private TaskState taskState = TaskState.TODO;
    private String taskText;


    public void setTitle(String title) {
        this.title = title;
    }

    public void setTaskText(String taskText) {
        this.taskText = taskText;
    }


    public String getTask() {
        return taskText;
    }

    public String getTitle() {
        return title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTaskText() {
        return taskText;
    }

    public TaskState getTaskState() {
        return taskState;
    }

    public void setTaskState(TaskState taskState) {
        this.taskState = taskState;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }
}
