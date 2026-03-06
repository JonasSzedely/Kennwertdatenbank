package userinterface;

import javafx.animation.PauseTransition;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import kennwertdatenbank.Calculation;
import kennwertdatenbank.Controller;
import kennwertdatenbank.Project;

import java.util.*;

public class MiddlePane {
    private static final int MAX_CACHE_SIZE = 50;
    private final Controller controller;
    private final LinkedHashMap<Integer, VBox> cellCache = new LinkedHashMap<>();
    private final Locale swissLocale = Locale.of("de", "CH");
    private final double CELL_WIDTH = 150;
    private final double CELL_HEIGHT = 30;
    private final double CELL_GAP = 0;
    private final int TOOL_TIP_TIME = 200;
    private final double BUTTON_SIZE = CELL_HEIGHT * 0.66;
    private final int SCROLLBAR_WIDTH = 15;
    private ScrollPane rightScroll;
    private HBox projectsContainer;
    private VBox rowLabels;

    public MiddlePane(Controller controller) {
        this.controller = controller;
    }

    /**
     * creates the middle pane
     *
     * @return HBox
     */
    public HBox get() {
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

        VBox rowLabels = displayRowLabels();
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

    private VBox displayRowLabels() {
        rowLabels = new VBox();

        ProjectList.getProjectList().addListener((ListChangeListener<Project>) change -> {
            rowLabels.getChildren().clear();
            rowLabels.getChildren().addAll(
                    rowLabel("Projekt-Nr."),
                    rowLabel("Version"),
                    rowLabel("Adresse"),
                    rowLabel("PLZ"),
                    rowLabel("Ort"),
                    rowLabel("Bauherr"),
                    rowLabel("Gebäudenutzungen"),
                    rowLabel("Art des Bauvorhaben"),
                    rowLabel("Planstand"),
                    rowLabel("Gerechnete Phasen"),
                    rowLabel("Anzahl Wohnungen"),
                    //rowLabel()("Anzahl Nasszellen"),
                    rowLabel("HNF in m²"),
                    rowLabel("GF in m²"),
                    rowLabel("SIA m³"),
                    //rowLabel()("unten"),
                    //rowLabel()("oben"),
                    rowLabel("Fassaden-Typ"),
                    rowLabel("Fenster-Typ"),
                    rowLabel("Dach-Typ"),
                    rowLabel("Heizungs-Typ"),
                    rowLabel("Kühlungs-Typ"),
                    rowLabel("Lüftungs-Typ Whg."),
                    rowLabel("Lüftungs-Typ TG"),
                    rowLabel("CO/NO-Anlage"),
                    rowLabelSpez("Sepzielles"),
                    rowLabel("")//,
            );
            HBox noScroll = new HBox();
            noScroll.setMinHeight(15);

            for (int i = 0; i < ProjectList.getProjectList().getFirst().getCalculations().size(); i++) {
                rowLabels.getChildren().add(rowLabel(ProjectList.getProjectList().getFirst().getCalculations().get(i).getName()));
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

    //methode von claude.ai generiert
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
                cell = createProjectCell(ProjectList.getSortedProjects().get(i));
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
    private VBox createProjectCell(Project project) {
        VBox projectBox = new VBox();
        projectBox.setMinWidth(CELL_WIDTH);
        projectBox.setPrefWidth(CELL_WIDTH);
        projectBox.setMaxWidth(CELL_WIDTH);
        projectBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 1 0 0;");

        //Modify-Project-Button, opens a new ProjectInputWindow
        Button modifyProjectButton = new Button("⟲");
        modifyProjectButton.setOnAction(event -> {
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

        //Delete-Project-Button, delete the project from the database
        Button deletProjectButton = new Button("\uD83D\uDDD1");
        deletProjectButton.setOnAction(event -> {
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
        deletProjectButton.setTooltip(new Tooltip("Projekt löschen"));
        deletProjectButton.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));
        deletProjectButton.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        deletProjectButton.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        deletProjectButton.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        deletProjectButton.setPadding(new Insets(0));

        Region spacerHead = new Region();
        HBox.setHgrow(spacerHead, Priority.ALWAYS);

        HBox projectHead = new HBox(10);
        projectHead.setPadding(new Insets(0, 10, 0, 0));
        projectHead.setMinHeight(CELL_HEIGHT);
        projectHead.setMaxHeight(CELL_HEIGHT);
        projectHead.setPrefHeight(CELL_HEIGHT);
        projectHead.setAlignment(Pos.CENTER_LEFT);
        projectHead.getChildren().addAll(projectLabelNoBorder(String.valueOf(project.getProjectNr())), spacerHead, modifyProjectButton, deletProjectButton);
        projectHead.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        //adding next version of Project
        Button nextVersionButton = new Button("+");
        nextVersionButton.setPadding(new Insets(0));
        nextVersionButton.setOnAction(event -> {
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

        //focus checkbox, sets the project to be focused and sorted first
        CheckBox focus = new CheckBox();
        focus.setTooltip(new Tooltip("Projekt als erstes anzeigen"));
        focus.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));
        focus.selectedProperty().bindBidirectional(project.pinnedProperty()); //code from claude.ai
        focus.setPadding(new Insets(0));

        HBox focusContainer = new HBox(focus);
        focusContainer.setAlignment(Pos.CENTER);
        focusContainer.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        focusContainer.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        focusContainer.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);

        // Update sorting when the pinned property changes (from claude.ai)
        project.pinnedProperty().addListener(observable -> {
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(event -> updateSorting());
            pause.play();
        });

        Region spacerVersion = new Region();
        HBox.setHgrow(spacerVersion, Priority.ALWAYS);

        HBox projectVersion = new HBox(10);
        projectVersion.setPadding(new Insets(0, 10, 0, 0));
        projectVersion.setMinHeight(CELL_HEIGHT);
        projectVersion.setMaxHeight(CELL_HEIGHT);
        projectVersion.setPrefHeight(CELL_HEIGHT);
        projectVersion.setAlignment(Pos.CENTER_LEFT);
        projectVersion.getChildren().addAll(projectLabelNoBorder(String.valueOf(project.getVersion())), spacerVersion, nextVersionButton, focusContainer);
        projectVersion.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        projectBox.getChildren().addAll(
                projectHead,
                projectVersion,
                projectLabel(project.getAddress()),
                projectLabel(String.valueOf(project.getPlz())),
                projectLabel(project.getLocation()),
                projectLabel(project.getOwner()),
                projectLabel(project.getPropertyType()),
                projectLabel(project.getConstructionType()),
                projectLabel(project.getDocumentPhase()),
                projectLabel(project.getCalculationPhase() + " - 5"),
                projectLabel(project.getApartmentsNr()),
                projectLabel(project.getHnf()),
                projectLabel(project.getGf()),
                projectLabel(project.getVolume()),
                projectLabel(project.getFacadeType()),
                projectLabel(project.getWindowType()),
                projectLabel(project.getRoofType()),
                projectLabel(project.getHeatingType()),
                projectLabel(project.getCoolingType()),
                projectLabel(project.getVentilationTypeApartments()),
                projectLabel(project.getVentilationTypeUg()),
                projectLabel(project.getCoNo()),
                projectLabelSpez(project.getSpecial()),
                projectLabel("")
        );

        for (Calculation calc : project.getCalculations().values()) {
            projectBox.getChildren().add(projectLabel(calc.getCalculation()));
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

    private Label rowLabel(String text) {
        Label label = new Label(text);
        label.setPadding(new Insets(0, 0, 0, 10));
        label.setMinHeight(CELL_HEIGHT);
        label.setMaxHeight(CELL_HEIGHT);
        label.setPrefHeight(CELL_HEIGHT);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        return label;
    }

    private TextFlow rowLabelSpez(String text) {
        Text newText = new Text(text);
        TextFlow label = new TextFlow(newText);
        label.setPadding(new Insets(5, 10, 0, 10));
        label.setMinHeight(CELL_HEIGHT * 2);
        label.setMaxHeight(CELL_HEIGHT * 2);
        label.setPrefHeight(CELL_HEIGHT * 2);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setTextAlignment(TextAlignment.LEFT);
        label.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        return label;
    }

    private Label projectLabel(String text) {
        Label label = new Label(text);
        label.setPadding(new Insets(5, 10, 0, 10));
        label.setMinHeight(CELL_HEIGHT);
        label.setMaxHeight(CELL_HEIGHT);
        label.setPrefHeight(CELL_HEIGHT);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_RIGHT);
        label.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        return label;
    }

    private TextFlow projectLabelSpez(String text) {
        Text newText = new Text(text);
        TextFlow label = new TextFlow(newText);
        label.setPadding(new Insets(5, 10, 0, 10));
        label.setMinHeight(CELL_HEIGHT * 2);
        label.setMaxHeight(CELL_HEIGHT * 2);
        label.setPrefHeight(CELL_HEIGHT * 2);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setTextAlignment(TextAlignment.RIGHT);
        label.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        return label;
    }

    private Label projectLabel(int number) {
        Label label = new Label(String.format(swissLocale, "%,d", number));
        label.setPadding(new Insets(5, 10, 0, 10));
        label.setMinHeight(CELL_HEIGHT);
        label.setMaxHeight(CELL_HEIGHT);
        label.setPrefHeight(CELL_HEIGHT);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_RIGHT);
        label.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        if (number == 0) {
            label.setText("");
        }
        return label;
    }

    private Label projectLabelNoBorder(String text) {
        Label label = new Label(text);
        label.setPadding(new Insets(5, 10, 0, 10));
        label.setMinHeight(CELL_HEIGHT);
        label.setMaxHeight(CELL_HEIGHT);
        label.setPrefHeight(CELL_HEIGHT);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_RIGHT);
        return label;
    }
}