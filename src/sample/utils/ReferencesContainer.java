package sample.utils;

import sample.services.TaskService;

public class ReferencesContainer {
    private static ReferencesContainer ourInstance = new ReferencesContainer();

    public static ReferencesContainer getInstance() {
        return ourInstance;
    }

    TaskService taskService = new TaskService();

    private ReferencesContainer() {}

    public TaskService getTaskService() {
        return taskService;
    }
}
