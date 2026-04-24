package view;

import javafx.animation.PauseTransition;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.*;

import java.util.*;

class MiddlePane {
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

    private final Controller controller;
    private final LinkedHashMap<Integer, VBox> cellCache = new LinkedHashMap<>();
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

    MiddlePane(Controller controller) {
        this.controller = controller;
        this.labelFactory = new LabelFactory(CELL_HEIGHT, CELL_WIDTH);
    }

    HBox get() {
        HBox middlePane = new HBox();
        VBox.setVgrow(middlePane, Priority.ALWAYS);
        middlePane.setId("middle-pane");

        //ProjectList.refreshProjectList();
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
            cellCache.clear();
            updateVisibleCells();
        });

        VBox middleRightPane = new VBox(10);
        middleRightPane.setAlignment(Pos.TOP_RIGHT);
        middleRightPane.setMinWidth(250);
        middleRightPane.getChildren().add(statistics());
        middleRightPane.setStyle("-fx-border-color: lightgray; -fx-border-width: 1 1 1 1;");

        middlePane.getChildren().addAll(leftScroll, rightScroll, middleRightPane);
        //ProjectList.refreshProjectList();
        return middlePane;
    }

    private VBox rowLabels() {
        rowLabels = new VBox();

        ProjectList.getProjectList().addListener((ListChangeListener<Project>) change -> {
            fillRowLabels();
        });

        // Einmalig beim Start befüllen
        fillRowLabels();

        return rowLabels;
    }

    private void fillRowLabels() {
        rowLabels.getChildren().clear();

        if (ProjectList.getProjectList().isEmpty()) return;

        for (ProjectValues field : HEAD_FIELDS) {
            rowLabels.getChildren().add(
                    labelFactory.getLabel(field.getLabel(), LabelFactory.LabelType.TEXT, true, false)
            );
        }

        for (ProjectValues field : ProjectValues.values()) {
            if (isHeadField(field) || isSkippedField(field)) continue;

            // Immer TEXT — Labels sind immer Strings, nie Numbers
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

    private HBox statistics() {
        HBox outerPane = new HBox(10);
        outerPane.setPadding(new Insets(10));
        GridPane grid = new GridPane(10, 10);
        grid.getColumnConstraints().add(new ColumnConstraints(150));

        Label statisticsTitel = new Label("Statistik");
        statisticsTitel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label nrOfProjects = new Label("Anzahl Projekte:");
        Label nrOfProjectsValue = new Label(String.valueOf(ProjectList.getProjectList().size()));
        Label averageRatioUG = new Label("⌀ Verhältnis UG/OG:");
        Label averageRatioUGValue = new Label(String.format("%.2f", controller.getAverageRatioUG()));
        Label averageWindowRatio = new Label("⌀ Anteil Fenster/Fassade:");
        Label averageWindowRatioValue = new Label(controller.getAverageWindowRatio() + " %");

        grid.add(statisticsTitel, 0, 0);
        grid.add(nrOfProjects, 0, 1);
        grid.add(nrOfProjectsValue, 1, 1);
        grid.add(averageRatioUG, 0, 2);
        grid.add(averageRatioUGValue, 1, 2);
        grid.add(averageWindowRatio, 0, 3);
        grid.add(averageWindowRatioValue, 1, 3);

        ProjectList.getProjectList().addListener((ListChangeListener<Project>) change -> {
            nrOfProjectsValue.setText(String.valueOf(ProjectList.getProjectList().size()));
            averageRatioUGValue.setText(String.format("%.2f", controller.getAverageRatioUG()));
        });

        outerPane.getChildren().add(grid);
        return outerPane;
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
            VBox cell = cellCache.get(i);
            if (cell == null) {
                cell = projectCells(ProjectList.getSortedProjects().get(i));
                cellCache.put(i, cell);
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

        project.pinnedProperty().addListener(observable -> {
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(event -> updateSorting());
            pause.play();
        });

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
            Iterator<Integer> it = cellCache.keySet().iterator();
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
        focus.setTooltip(new Tooltip("Projekt als erstes anzeigen"));
        focus.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));
        focus.selectedProperty().bindBidirectional(project.pinnedProperty());
        focus.setPadding(new Insets(0));

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
        nextVersionButton.setOnAction(event -> {
            if (!controller.isDatabaseAvailable()) {
                NoDBConnection.show();
                return;
            }
            ProjectInputWindow nextVersion = new ProjectInputWindow(controller, project, ProjectInputWindow.Type.NEXT);
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
        deleteProjectButton.setOnAction(event -> {
            if (!controller.isDatabaseAvailable()) {
                NoDBConnection.show();
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Projekt löschen");
            alert.setHeaderText("Bitte bestätigen:");
            alert.setContentText("Löschen von Projekt Nr. "
                    + project.get(ProjectValues.PROJECT_NR)
                    + " Version " + project.get(ProjectValues.VERSION) + "?");
            Optional<ButtonType> buttonType = alert.showAndWait();
            if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
                String message = controller.deleteProject(
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
        modifyProjectButton.setOnAction(event -> {
            if (!controller.isDatabaseAvailable()) {
                NoDBConnection.show();
                return;
            }
            ProjectInputWindow modify = new ProjectInputWindow(controller, project, ProjectInputWindow.Type.MODIFY);
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