package view;

import javafx.animation.PauseTransition;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Calculation;
import model.Controller;
import model.Project;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Optional;

class MiddlePane {
    private static final int MAX_CACHE_SIZE = 50;
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

    /**
     * creates the middle pane
     *
     * @return HBox
     */
    HBox get() {
        HBox middlePane = new HBox();
        VBox.setVgrow(middlePane, Priority.ALWAYS);
        middlePane.setId("middle-pane");

        ProjectList.refreshProjectList();
        updateSorting();

        // left side of the middle pane
        // scroll pane for row labels
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

        // project pane
        // pane to know the space the projects have
        Pane totalWidthSpacer = new Pane();

        // container for the projects
        projectsContainer = new HBox(CELL_GAP);
        projectsContainer.setFillHeight(true);

        // overlay two panes to enable virtual scrolling
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.TOP_LEFT);
        stackPane.getChildren().addAll(totalWidthSpacer, projectsContainer);

        // scroll pane for projects
        rightScroll = new ScrollPane(stackPane);
        rightScroll.setFitToHeight(true);
        rightScroll.setFitToWidth(false);
        HBox.setHgrow(rightScroll, Priority.ALWAYS);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        //synchronize scrolling of left and right scrollPane
        leftScroll.vvalueProperty().bindBidirectional(rightScroll.vvalueProperty());

        //update cells while scrolling right
        rightScroll.hvalueProperty().addListener(observable -> updateVisibleCells());

        //update cells while scrolling left
        rightScroll.widthProperty().addListener(observable -> updateVisibleCells());

        //update cells when projects get updated
        ProjectList.getSortedProjects().addListener((ListChangeListener<Project>) change -> {
            double totalWidth = ProjectList.getSortedProjects().size() * (CELL_WIDTH + CELL_GAP);
            totalWidthSpacer.setPrefWidth(totalWidth);
            totalWidthSpacer.setMinWidth(totalWidth);
            cellCache.clear();
            updateVisibleCells();
        });

        //right side of the middle pane
        VBox middleRightPane = new VBox(10);
        middleRightPane.setAlignment(Pos.TOP_RIGHT);
        middleRightPane.setMinWidth(250);
        middleRightPane.getChildren().add(statistics());
        middleRightPane.setStyle("-fx-border-color: lightgray; -fx-border-width: 1 1 1 1;");

        middlePane.getChildren().addAll(leftScroll, rightScroll, middleRightPane);
        ProjectList.refreshProjectList();
        return middlePane;
    }

    private VBox rowLabels() {
        rowLabels = new VBox();

        ProjectList.getProjectList().addListener((ListChangeListener<Project>) change -> {
            rowLabels.getChildren().clear();
            rowLabels.getChildren().addAll(
                    labelFactory.getLabel("Projekt-Nr.", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Version", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Adresse", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("PLZ", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Ort", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Bauherr", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Gebäudenutzung", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Art des Bauvorhaben", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Planstand", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Gerechnete Phasen", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Anzahl Wohnungen", LabelFactory.LabelType.TEXT, true, false),
                    //rowLabel()("Anzahl Nasszellen"),
                    labelFactory.getLabel("HNF inkl. Reduit in m²", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("GF in m²", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Grundstücksfläche in m²", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Umgebungsfläche in m²", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("SIA m³", LabelFactory.LabelType.TEXT, true, false),
                    //rowLabel()("unten"),
                    //rowLabel()("oben"),
                    labelFactory.getLabel("Fassaden-Typ", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Fenster-Typ", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Dach-Art", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Heizung", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Kühlung", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Lüftung WHG", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Lüftung TG", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("CO/NO Anlage", LabelFactory.LabelType.TEXT, true, false),
                    labelFactory.getLabel("Spezielles", LabelFactory.LabelType.TALL, true, false),
                    labelFactory.getLabel("", LabelFactory.LabelType.TEXT, true, false)
            );
            HBox noScroll = new HBox();
            noScroll.setMinHeight(15);

            for (int i = 0; i < ProjectList.getProjectList().getFirst().getCalculations().size(); i++) {
                String labelName = ProjectList.getProjectList().getFirst().getCalculations().get(i).getName();
                rowLabels.getChildren().add(labelFactory.getLabel(labelName, LabelFactory.LabelType.TEXT, true, false));
            }
            rowLabels.getChildren().add(noScroll);
        });

        return rowLabels;
    }

    private HBox statistics() {
        HBox outerPane = new HBox(10);
        outerPane.setPadding(new Insets(10));
        GridPane grid = new GridPane(10, 10);
        grid.getColumnConstraints().add(new ColumnConstraints(150));

        Label statisticsTitel = new Label("Statistik");
        statisticsTitel.setStyle(
                "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; "
        );
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

        double scrollPaneWidth = rightScroll.getWidth() - (rightScroll.getVbarPolicy() != ScrollPane.ScrollBarPolicy.NEVER ? SCROLLBAR_WIDTH : 0); // Scrollbar-Breite abziehen
        double totalWidth = ProjectList.getSortedProjects().size() * (CELL_WIDTH + CELL_GAP);

        // hvalue between 0 und 1 → calculate to Pixel
        double scrollOffset = rightScroll.getHvalue() * Math.max(0, totalWidth - scrollPaneWidth);

        // Welche Indizes sind sichtbar?
        int firstVisible = Math.max(0, (int) (scrollOffset / (CELL_WIDTH + CELL_GAP)));
        int lastVisible = Math.min(
                ProjectList.getSortedProjects().size() - 1,
                (int) ((scrollOffset + scrollPaneWidth) / (CELL_WIDTH + CELL_GAP))
        );

        projectsContainer.getChildren().clear();

        //left spacer: moves the visible cells to the correct position
        if (firstVisible > 0) {
            Pane leftSpacer = new Pane();
            leftSpacer.setPrefWidth(firstVisible * (CELL_WIDTH + CELL_GAP));
            leftSpacer.setMinWidth(leftSpacer.getPrefWidth());
            projectsContainer.getChildren().add(leftSpacer);
        }

        //render only visible cells, fetch them from cache
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

    /**
     * creates the visual elements for a project
     *
     * @param project the project to get rendered
     * @return project as VBox
     */
    private VBox projectCells(Project project) {
        VBox projectBox = new VBox();
        projectBox.setMinWidth(CELL_WIDTH);
        projectBox.setPrefWidth(CELL_WIDTH);
        projectBox.setMaxWidth(CELL_WIDTH);
        projectBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 1 0 0;");

        // Update sorting when the pinned property changes (from claude.ai)
        project.pinnedProperty().addListener(observable -> {
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(event -> updateSorting());
            pause.play();
        });

        projectBox.getChildren().addAll(
                projectHead(project),
                projectVersion(project),
                labelFactory.getLabel(project.getAddress(), LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(String.valueOf(project.getPlz()), LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(project.getLocation(), LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(project.getOwner(), LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(project.getPropertyType(), LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(project.getConstructionType(), LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(project.getDocumentPhase(), LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(project.getCalculationPhase() + " - 5", LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(project.getApartmentsNr(), LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(project.getHnf(), LabelFactory.LabelType.NUMBER, true, true),
                labelFactory.getLabel(project.getGf(), LabelFactory.LabelType.NUMBER, true, true),
                labelFactory.getLabel(project.getParcelSize(), LabelFactory.LabelType.NUMBER, true, true),
                labelFactory.getLabel(project.getLandscapedArea(), LabelFactory.LabelType.NUMBER, true, true),
                labelFactory.getLabel(project.getVolume(), LabelFactory.LabelType.NUMBER, true, true),
                labelFactory.getLabel(project.getFacadeType(), LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(project.getWindowType(), LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(project.getRoofType(), LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(project.getHeatingType(), LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(project.getCoolingType(), LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(project.getVentilationTypeApartments(), LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(project.getVentilationTypeUg(), LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(project.getCoNo(), LabelFactory.LabelType.TEXT, true, true),
                labelFactory.getLabel(project.getSpecial(), LabelFactory.LabelType.TALL, true, true),
                labelFactory.getLabel("", LabelFactory.LabelType.TEXT, true, true)
        );

        for (Calculation calc : project.getCalculations()) {
            projectBox.getChildren().add(labelFactory.getLabel(calc.getCalculation(), LabelFactory.LabelType.TEXT, true, true));
        }

        return projectBox;
    }

    /**
     * Wenn der Cache zu groß wird, werden die ältesten Einträge entfernt
     */
    private void evictCacheIfNeeded() {
        while (cellCache.size() > MAX_CACHE_SIZE) {
            Iterator<Integer> it = cellCache.keySet().iterator();
            if (it.hasNext()) {
                it.next();
                it.remove();
            }
        }
    }

    /**
     * sorts the project list (from claude.ai)
     */
    private void updateSorting() {
        ProjectList.getSortedProjects().setComparator(
                Comparator.comparing(Project::isPinned).reversed() // true first
                        .thenComparing(Project::getProjectNr) // project number
                        .thenComparing(Project::getVersion) // version
        );
    }


    /**
     * Creates a line for the project containing the project number, modify button and delete button.
     *
     * @param project the project
     * @return HBox
     */
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
                labelFactory.getLabel(project.getProjectNr(),
                        LabelFactory.LabelType.TEXT, false, true),
                spacerHead,
                modifyProjectButton(project),
                deleteProjectButton(project)
        );
        projectHead.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        return projectHead;
    }

    /**
     * Creates a line for the project containing the version number, next version button and focus checkbox.
     *
     * @param project the project
     * @return HBox
     */
    private HBox projectVersion(Project project) {
        Region spacerVersion = new Region();
        HBox.setHgrow(spacerVersion, Priority.ALWAYS);

        HBox projectVersion = new HBox(10);
        projectVersion.setPadding(new Insets(0, 10, 0, 0));
        projectVersion.setMinHeight(CELL_HEIGHT);
        projectVersion.setMaxHeight(CELL_HEIGHT);
        projectVersion.setPrefHeight(CELL_HEIGHT);
        projectVersion.setAlignment(Pos.CENTER_LEFT);
        projectVersion.getChildren().addAll(labelFactory.getLabel(
                        project.getVersion(),
                        LabelFactory.LabelType.TEXT, false, true),
                spacerVersion,
                nextVersionButton(project),
                focusContainer(project)
        );
        projectVersion.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        return projectVersion;
    }

    /**
     * Create a checkbox used to sort the project to the beginning.
     *
     * @param project the project
     * @return HBox
     */
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

    /**
     * Creates a button to create a new version of the project.
     *
     * @param project the project
     * @return Button
     */
    private Button nextVersionButton(Project project) {
        //adding next version of Project
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

    /**
     * Create a button to delete the project.
     *
     * @param project the project
     * @return Button
     */
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
            alert.setContentText("Löschen von Projekt Nr. " + project.getProjectNr() + " Version " + project.getVersion() + "?");
            Optional<ButtonType> buttonType = alert.showAndWait();
            if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
                String message = controller.deleteProject(project.getProjectNr(), project.getVersion());
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

    /**
     * Create a button to modify the project.
     *
     * @param project the project
     * @return Button
     */
    private Button modifyProjectButton(Project project) {
        //Modify-Project-Button, opens a new ProjectInputWindow
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
