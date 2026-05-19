package view.middlepane;

import api.DataService;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Calculation;
import model.Project;
import model.ProjectCalculations;
import model.ProjectValues;
import view.ProjectInputWindow;
import view.ProjectList;
import view.StageFactory;

import java.util.*;

public class MiddlePane {
    private static final int MAX_CACHE_SIZE = 50;

    // Handled by dedicated projectHead() / projectVersion() rows
    private static final ProjectValues[] HEAD_FIELDS = {
            ProjectValues.PROJECT_NR,
            ProjectValues.VERSION
    };

    // Not shown individually: BATHROOM_NR is hidden, volumes are merged into one row
    private static final ProjectValues[] SKIPPED_FIELDS = {
            ProjectValues.BATHROOM_NR,
            ProjectValues.VOLUME_UNDERGROUND,
            ProjectValues.VOLUME_ABOVE_GROUND
    };

    private final DataService service;
    private final LinkedHashMap<Project, VBox> cellCache = new LinkedHashMap<>();
    private final double CELL_WIDTH = 150;
    private final double CELL_HEIGHT = 30;
    private final double CELL_GAP = 0;
    private final int TOOL_TIP_TIME = 200;
    private final double BUTTON_SIZE = CELL_HEIGHT * 0.66;
    private final int SCROLLBAR_WIDTH = 15;
    private final LabelFactory labelFactory;
    private ScrollPane rightScroll;
    private HBox projectsContainer;
    private VBox rowLabels;

    public MiddlePane(DataService service) {
        this.service = service;
        this.labelFactory = new LabelFactory(CELL_HEIGHT, CELL_WIDTH);
    }

    public HBox get() {
        HBox middlePane = new HBox();
        VBox.setVgrow(middlePane, Priority.ALWAYS);
        middlePane.setId("middle-pane");

        updateSorting();

        ScrollPane leftScroll = new ScrollPane();
        leftScroll.setFitToHeight(true);
        leftScroll.setMinWidth(150);
        leftScroll.setMaxWidth(150);
        leftScroll.getStyleClass().add("no-scrollbar");
        leftScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox rowLabels = rowLabels();
        rowLabels.setNodeOrientation(javafx.geometry.NodeOrientation.LEFT_TO_RIGHT);
        rowLabels.prefWidthProperty().bind(leftScroll.widthProperty());
        leftScroll.setContent(rowLabels);

        Pane totalWidthSpacer = new Pane();

        projectsContainer = new HBox(CELL_GAP);
        projectsContainer.setFillHeight(true);

        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.TOP_LEFT);
        stackPane.getChildren().addAll(totalWidthSpacer, projectsContainer);

        rightScroll = new ScrollPane(stackPane);
        rightScroll.setFitToHeight(true);
        rightScroll.setFitToWidth(false);
        HBox.setHgrow(rightScroll, Priority.ALWAYS);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        leftScroll.vvalueProperty().bindBidirectional(rightScroll.vvalueProperty());
        rightScroll.hvalueProperty().addListener(observable -> updateVisibleCells());
        rightScroll.widthProperty().addListener(observable -> updateVisibleCells());


        ProjectList.getSortedProjects().addListener((ListChangeListener<Project>) change -> {
            double totalWidth = ProjectList.getSortedProjects().size() * (CELL_WIDTH + CELL_GAP);
            totalWidthSpacer.setPrefWidth(totalWidth);
            totalWidthSpacer.setMinWidth(totalWidth);

            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    cellCache.clear();
                    break;
                }
            }
            updateVisibleCells();
        });

        VBox middleRightPane = new VBox(10);
        middleRightPane.setAlignment(Pos.TOP_RIGHT);
        middleRightPane.setMinWidth(250);
        middleRightPane.getChildren().add(new Statistics().get());
        middleRightPane.setStyle("-fx-border-color: lightgray; -fx-border-width: 1 1 1 1;");

        middlePane.getChildren().addAll(leftScroll, rightScroll, middleRightPane);

        return middlePane;
    }

    private VBox rowLabels() {
        rowLabels = new VBox();

        ProjectList.getProjectList().addListener((ListChangeListener<Project>) change -> {
            fillRowLabels();
        });

        // fill at start
        fillRowLabels();

        return rowLabels;
    }

    private void fillRowLabels() {
        rowLabels.getChildren().clear();

        if (ProjectList.getProjectList().isEmpty()) {
            return;
        }

        for (ProjectValues field : HEAD_FIELDS) {
            rowLabels.getChildren().add(
                    labelFactory.getLabel(field.getLabel(), LabelFactory.LabelType.TEXT, true, false)
            );
        }

        for (ProjectValues field : ProjectValues.values()) {
            if (isHeadField(field) || isSkippedField(field)) continue;

            LabelFactory.LabelType labelType = (field == ProjectValues.SPECIAL)
                    ? LabelFactory.LabelType.TALL
                    : LabelFactory.LabelType.TEXT;

            rowLabels.getChildren().add(
                    labelFactory.getLabel(field.getLabel(), labelType, true, false)
            );

            if (field == ProjectValues.LANDSCAPED_AREA) {
                rowLabels.getChildren().add(
                        labelFactory.getLabel("SIA m³", LabelFactory.LabelType.TEXT, true, false)
                );
            }
        }

        rowLabels.getChildren().add(
                labelFactory.getLabel("", LabelFactory.LabelType.TEXT, true, false)
        );

        Project first = ProjectList.getProjectList().getFirst();
        List<Calculation> calculations = new ProjectCalculations(first).getCalculations();
        for (Calculation calculation : calculations) {
            rowLabels.getChildren().add(
                    labelFactory.getLabel(calculation.getName(), LabelFactory.LabelType.TEXT, true, false)
            );
        }

        HBox noScroll = new HBox();
        noScroll.setMinHeight(15);
        rowLabels.getChildren().add(noScroll);
    }

    private void updateVisibleCells() {
        if (rightScroll == null || projectsContainer == null) return;

        double scrollPaneWidth = rightScroll.getWidth()
                - (rightScroll.getVbarPolicy() != ScrollPane.ScrollBarPolicy.NEVER ? SCROLLBAR_WIDTH : 0);
        double totalWidth = ProjectList.getSortedProjects().size() * (CELL_WIDTH + CELL_GAP);
        double scrollOffset = rightScroll.getHvalue() * Math.max(0, totalWidth - scrollPaneWidth);

        int firstVisible = Math.max(0, (int) (scrollOffset / (CELL_WIDTH + CELL_GAP)));
        int lastVisible = Math.min(
                ProjectList.getSortedProjects().size() - 1,
                (int) ((scrollOffset + scrollPaneWidth) / (CELL_WIDTH + CELL_GAP))
        );

        projectsContainer.getChildren().clear();

        if (firstVisible > 0) {
            Pane leftSpacer = new Pane();
            leftSpacer.setPrefWidth(firstVisible * (CELL_WIDTH + CELL_GAP));
            leftSpacer.setMinWidth(leftSpacer.getPrefWidth());
            projectsContainer.getChildren().add(leftSpacer);
        }

        for (int i = firstVisible; i <= lastVisible; i++) {
            Project project = ProjectList.getSortedProjects().get(i);
            VBox cell = cellCache.get(project); // nach Project statt Index
            if (cell == null) {
                cell = projectCells(project);
                cellCache.put(project, cell);
                evictCacheIfNeeded();
            }
            projectsContainer.getChildren().add(cell);
        }
    }

    private VBox projectCells(Project project) {
        VBox projectBox = new VBox();
        projectBox.setMinWidth(CELL_WIDTH);
        projectBox.setPrefWidth(CELL_WIDTH);
        projectBox.setMaxWidth(CELL_WIDTH);
        projectBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 1 0 0;");

        // Dedicated head rows for PROJECT_NR and VERSION
        projectBox.getChildren().add(projectHead(project));
        projectBox.getChildren().add(projectVersion(project));

        // All remaining fields, dynamically from enum
        for (ProjectValues field : ProjectValues.values()) {
            if (isHeadField(field) || isSkippedField(field)) {
                continue;
            }

            LabelFactory.LabelType labelType = getLabelType(field);

            if (field == ProjectValues.CALCULATION_PHASE) {
                int phase = project.get(field);
                projectBox.getChildren().add(
                        labelFactory.getLabel(phase + " - 5", LabelFactory.LabelType.TEXT, true, true)
                );
            } else if (labelType == LabelFactory.LabelType.NUMBER) {
                int value = project.get(field);
                projectBox.getChildren().add(
                        labelFactory.getLabel(value, labelType, true, true)
                );
            } else {
                String value = project.get(field);
                projectBox.getChildren().add(
                        labelFactory.getLabel(value, labelType, true, true)
                );
            }

            if (field == ProjectValues.LANDSCAPED_AREA) {
                int volumeUG = project.get(ProjectValues.VOLUME_UNDERGROUND);
                int volumeAG = project.get(ProjectValues.VOLUME_ABOVE_GROUND);
                projectBox.getChildren().add(
                        labelFactory.getLabel(volumeUG + volumeAG, LabelFactory.LabelType.NUMBER, true, true)
                );
            }
        }

        // Empty separator row
        projectBox.getChildren().add(
                labelFactory.getLabel("", LabelFactory.LabelType.TEXT, true, true)
        );

        List<Calculation> calculations = new ProjectCalculations(project).getCalculations();
        for (Calculation calculation : calculations) {
            projectBox.getChildren().add(
                    labelFactory.getLabel(calculation.getCalculation(), LabelFactory.LabelType.TEXT, true, true)
            );
        }

        return projectBox;
    }

    /**
     * Returns the LabelType for a given field.
     * SPECIAL uses TALL, numeric fields use NUMBER, everything else TEXT.
     */
    private LabelFactory.LabelType getLabelType(ProjectValues field) {
        if (field == ProjectValues.SPECIAL) return LabelFactory.LabelType.TALL;
        if (field.getType() == Integer.class) return LabelFactory.LabelType.NUMBER;
        return LabelFactory.LabelType.TEXT;
    }

    private boolean isHeadField(ProjectValues field) {
        for (ProjectValues f : HEAD_FIELDS) {
            if (f == field) return true;
        }
        return false;
    }

    private boolean isSkippedField(ProjectValues field) {
        for (ProjectValues f : SKIPPED_FIELDS) {
            if (f == field) return true;
        }
        return false;
    }

    private void evictCacheIfNeeded() {
        while (cellCache.size() > MAX_CACHE_SIZE) {
            Iterator<Project> it = cellCache.keySet().iterator();
            if (it.hasNext()) {
                it.next();
                it.remove();
            }
        }
    }

    private void updateSorting() {
        ProjectList.getSortedProjects().setComparator(
                Comparator.comparing(Project::isPinned).reversed()
                        .thenComparing(p -> (int) p.get(ProjectValues.PROJECT_NR))
                        .thenComparing(p -> (int) p.get(ProjectValues.VERSION))
        );
    }

    private HBox projectHead(Project project) {
        Region spacerHead = new Region();
        HBox.setHgrow(spacerHead, Priority.ALWAYS);

        HBox projectHead = new HBox(10);
        projectHead.setPadding(new Insets(0, 10, 0, 0));
        projectHead.setMinHeight(CELL_HEIGHT);
        projectHead.setMaxHeight(CELL_HEIGHT);
        projectHead.setPrefHeight(CELL_HEIGHT);
        projectHead.setAlignment(Pos.CENTER_LEFT);
        projectHead.getChildren().addAll(
                labelFactory.getLabel((int) project.get(ProjectValues.PROJECT_NR),
                        LabelFactory.LabelType.TEXT, false, true),
                spacerHead,
                modifyProjectButton(project),
                deleteProjectButton(project)
        );
        projectHead.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        return projectHead;
    }

    private HBox projectVersion(Project project) {
        Region spacerVersion = new Region();
        HBox.setHgrow(spacerVersion, Priority.ALWAYS);

        HBox projectVersion = new HBox(10);
        projectVersion.setPadding(new Insets(0, 10, 0, 0));
        projectVersion.setMinHeight(CELL_HEIGHT);
        projectVersion.setMaxHeight(CELL_HEIGHT);
        projectVersion.setPrefHeight(CELL_HEIGHT);
        projectVersion.setAlignment(Pos.CENTER_LEFT);
        projectVersion.getChildren().addAll(
                labelFactory.getLabel((int) project.get(ProjectValues.VERSION),
                        LabelFactory.LabelType.TEXT, false, true),
                spacerVersion,
                nextVersionButton(project),
                focusContainer(project)
        );
        projectVersion.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        return projectVersion;
    }

    private HBox focusContainer(Project project) {
        CheckBox focus = new CheckBox();
        focus.setSelected(project.isPinned());
        focus.setTooltip(new Tooltip("Projekt als erstes anzeigen"));
        focus.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));
        focus.setPadding(new Insets(0));

        focus.selectedProperty().addListener((obs, oldVal, newVal) -> {
            project.setPinned(newVal);
            updateSorting();
        });

        HBox focusContainer = new HBox(focus);
        focusContainer.setAlignment(Pos.CENTER);
        focusContainer.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        focusContainer.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        focusContainer.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);

        return focusContainer;
    }

    private Button nextVersionButton(Project project) {
        Button nextVersionButton = new Button("+");
        nextVersionButton.setPadding(new Insets(0));
        service.onDbAvailableChanged(e -> {
            Platform.runLater(() -> nextVersionButton.setDisable(!service.isDBAvailable()));
        });

        nextVersionButton.setOnAction(event -> {
            ProjectInputWindow nextVersion = new ProjectInputWindow(service, project, ProjectInputWindow.Type.NEXT);
            try {
                Stage newStage = StageFactory.createStage("Neue Version");
                nextVersion.start(newStage);
                if (nextVersion.getAddButtonStatus()) {
                    ProjectList.refreshProjectList();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        nextVersionButton.setTooltip(new Tooltip("Neue Version erstellen"));
        nextVersionButton.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));
        nextVersionButton.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        nextVersionButton.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        nextVersionButton.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);

        return nextVersionButton;
    }

    private Button deleteProjectButton(Project project) {
        Button deleteProjectButton = new Button("\uD83D\uDDD1");
        service.onDbAvailableChanged(e -> {
            Platform.runLater(() -> deleteProjectButton.setDisable(!service.isDBAvailable()));
        });

        deleteProjectButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Projekt löschen");
            alert.setHeaderText("Bitte bestätigen:");
            alert.setContentText("Löschen von Projekt Nr. "
                    + project.get(ProjectValues.PROJECT_NR)
                    + " Version " + project.get(ProjectValues.VERSION) + "?");
            Optional<ButtonType> buttonType = alert.showAndWait();
            if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
                String message = service.deleteProject(
                        project.get(ProjectValues.PROJECT_NR),
                        project.get(ProjectValues.VERSION)
                );
                ProjectList.refreshProjectList();

                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Projekt löschen");
                confirmAlert.setHeaderText("Rückmeldung");
                confirmAlert.setContentText(message);
                confirmAlert.showAndWait();
            }
        });
        deleteProjectButton.setTooltip(new Tooltip("Projekt löschen"));
        deleteProjectButton.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));
        deleteProjectButton.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        deleteProjectButton.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        deleteProjectButton.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        deleteProjectButton.setPadding(new Insets(0));

        return deleteProjectButton;
    }

    private Button modifyProjectButton(Project project) {
        Button modifyProjectButton = new Button("⟲");
        service.onDbAvailableChanged(e -> {
            Platform.runLater(() -> modifyProjectButton.setDisable(!service.isDBAvailable()));
        });

        modifyProjectButton.setOnAction(event -> {
            ProjectInputWindow modify = new ProjectInputWindow(service, project, ProjectInputWindow.Type.MODIFY);
            try {
                Stage newStage = StageFactory.createStage("Projekt bearbeiten");
                modify.start(newStage);
                if (modify.getAddButtonStatus()) {
                    ProjectList.refreshProjectList();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        modifyProjectButton.setTooltip(new Tooltip("Projekt bearbeiten"));
        modifyProjectButton.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));
        modifyProjectButton.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        modifyProjectButton.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        modifyProjectButton.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        modifyProjectButton.setPadding(new Insets(0));

        return modifyProjectButton;
    }
}