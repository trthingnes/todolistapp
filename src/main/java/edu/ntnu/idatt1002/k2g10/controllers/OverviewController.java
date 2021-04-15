package edu.ntnu.idatt1002.k2g10.controllers;

import com.jfoenix.controls.JFXTextField;
import edu.ntnu.idatt1002.k2g10.Session;
import edu.ntnu.idatt1002.k2g10.factory.PopupWindowFactory;
import edu.ntnu.idatt1002.k2g10.factory.TableColumnFactory;
import edu.ntnu.idatt1002.k2g10.models.Task;
import javafx.collections.FXCollections;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for Overview view.
 *
 * @author tobiasth
 */
public class OverviewController {
    @FXML
    private JFXTextField searchField;
    @FXML
    private ListView<String> categoryList;
    @FXML
    private TableView<Task> taskList;
    @FXML
    private VBox taskDetailPanel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label emailLabel;

    private final List<Task> displayedTasks = new ArrayList<>();
    private final List<String> displayedCategories = new ArrayList<>();

    /**
     * Runs when the view is loaded.
     */
    @FXML
    public void initialize() {
        // Fill user info
        usernameLabel.setText(Session.getActiveUser().getUsername());
        emailLabel.setText(Session.getActiveUser().getEmail());

        // Initialize task list
        TableColumnFactory<Task, String> columnFactory = new TableColumnFactory<>();
        TableColumn<Task, String> titleColumn = columnFactory.getTableColumn("Title", "title");
        TableColumn<Task, String> priorityColumn = columnFactory.getTableColumn("Priority", "priority");
        TableColumn<Task, String> categoryColumn = columnFactory.getTableColumn("Category", "category");
        taskList.getColumns().addAll(List.of(titleColumn, priorityColumn, categoryColumn));


        // Link task list table to the task list.
        taskList.getSelectionModel().selectedItemProperty().addListener((task) -> showTaskDetails());
        refreshAndFilterTaskList();

        // Link category list view to category list.
        refreshCategoryList();

        // Make detail panel grow to fill right menu
        taskDetailPanel.setPrefHeight(Double.MAX_VALUE);
    }

    @FXML
    public void showAddTask() throws IOException {
        Stage popupWindow = PopupWindowFactory.getPopupWindow("add-task");
        popupWindow.setTitle("Add new task");
        popupWindow.showAndWait();

        refreshAndFilterTaskList();

        taskList.refresh();
    }

    @FXML
    public void refreshAndFilterTaskList() {
        String query = searchField.getText().toLowerCase(Locale.ROOT);

        List<Task> matchingTasks = new ArrayList<>();
        if(!query.isBlank()) {
            for(Task task : Session.getActiveUser().getTaskList().getTasks()) {
                if(task.getTitle().toLowerCase(Locale.ROOT).contains(query) ||
                        task.getDescription().toLowerCase(Locale.ROOT).contains(query) ||
                        task.getCategory().getTitle().toLowerCase(Locale.ROOT).contains(query)
                ) {
                    matchingTasks.add(task);
                }
            }
        }
        else {
            matchingTasks = Session.getActiveUser().getTaskList().getTasks();
        }

        displayedTasks.clear();
        displayedTasks.addAll(matchingTasks);
        taskList.setItems(FXCollections.observableList(displayedTasks));
    }

    @FXML
    public void showAddCategory() throws IOException {
        Stage popupWindow = PopupWindowFactory.getPopupWindow("add-category");
        popupWindow.showAndWait();

        refreshCategoryList();
        showTaskDetails();
    }

    private void refreshCategoryList() {
        displayedCategories.clear();
        displayedCategories.addAll(Session.getActiveUser().getTaskList().getCategories()
                .stream()
                .map(c -> String.format("%s %s", c.getIcon(), c.getTitle()))
                .sorted()
                .collect(Collectors.toList()));

        categoryList.setItems(FXCollections.observableList(displayedCategories));
    }

    private void showTaskDetails()  {
        Task selectedTask = taskList.getSelectionModel().getSelectedItem();

        try {
            Objects.requireNonNull(selectedTask);
            taskDetailPanel.getChildren().clear();
            taskDetailPanel.getChildren().add(new DetailTaskBox(selectedTask).getContainer());
        } catch (IOException | NullPointerException ignored) {}
    }

    @FXML
    public void showSettings() throws IOException {
        Stage popupWindow = PopupWindowFactory.getPopupWindow("settings");
        popupWindow.show();
    }

    @FXML
    public void logout() throws IOException {
        Session.setLocation("login");
    }
}