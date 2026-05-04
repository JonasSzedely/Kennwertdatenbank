package view;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import model.Controller;
import model.Project;
import model.ProjectValues;
import org.controlsfx.control.ToggleSwitch;

class Filters {
    private final Controller controller;
    private final int TOOL_TIP_TIME = 200;
    private RangeFilter sumFilter;
    private RangeFilter apartmentNrFilter;
    private RangeFilter volumeFilter;

    public Filters(Controller controller) {
        this.controller = controller;
    }

    /**
     * creates the filters
     *
     * @return HBox
     */
    public HBox get() {
        HBox outerPane = new HBox();
        outerPane.setAlignment(Pos.CENTER);
        outerPane.setPadding(new Insets(20, 0, 20, 0));

        GridPane filterBox = new GridPane(10, 10);
        filterBox.getColumnConstraints().addAll(
                new ColumnConstraints(100),
                new ColumnConstraints(100),
                new ColumnConstraints(2),
                new ColumnConstraints(100),
                new ColumnConstraints(100),
                new ColumnConstraints(2),
                new ColumnConstraints(100),
                new ColumnConstraints(100),
                new ColumnConstraints(2),
                new ColumnConstraints(100),
                new ColumnConstraints(100));

        //filter project number
        TextField filterProjectNr = new TextField();
        filterProjectNr.setPromptText("Projektnummer");
        filterBox.add(filterProjectNr, 0, 0);
        filterProjectNr.setTooltip(new Tooltip("Nach Projektnummer filtern"));
        filterProjectNr.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));

        //filter construction Type
        ComboBox<String> constructionTypeFilter = new ComboBox<>();
        String[] constructionType = ProjectValues.CONSTRUCTION_TYPE.getOptions().split("\\|");
        ObservableList<String> constructionTypeList = FXCollections.observableArrayList(constructionType);
        constructionTypeList.addFirst("Alle Bauvorhaben");
        constructionTypeFilter.setItems(constructionTypeList);
        constructionTypeFilter.setPromptText("Bauvorhaben");
        filterBox.add(constructionTypeFilter, 0, 2);
        constructionTypeFilter.setTooltip(new Tooltip("Nach Bauvorhaben Art filtern"));
        constructionTypeFilter.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));

        //filter project type
        ComboBox<String> projectTypeFilter = new ComboBox<>();
        String[] projectTypes = ProjectValues.PROPERTY_TYPE.getOptions().split("\\|");
        ObservableList<String> projectTypeList = FXCollections.observableArrayList(projectTypes);
        projectTypeList.addFirst("Alle Gebäudenutzer");
        projectTypeFilter.setItems(projectTypeList);
        projectTypeFilter.setPromptText("Gebäudenutzer");
        filterBox.add(projectTypeFilter, 0, 1);
        projectTypeFilter.setTooltip(new Tooltip("Nach Gebäudenutzer filtern"));
        projectTypeFilter.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));



        Separator verticalLine1 = new Separator(Orientation.VERTICAL);
        filterBox.add(verticalLine1, 2, 0);
        GridPane.setRowSpan(verticalLine1, 3);
        GridPane.setFillHeight(verticalLine1, true);

        //filter versions (all/newest)
        ToggleSwitch versionFilter = new ToggleSwitch();
        versionFilter.setAlignment(Pos.CENTER_RIGHT);
        versionFilter.setMinWidth(99);
        versionFilter.setPrefWidth(99);
        versionFilter.setMaxWidth(99);
        versionFilter.setSelected(false);
        versionFilter.setText("vollständig");
        filterBox.add(versionFilter, 1, 0);
        versionFilter.setTooltip(new Tooltip("Nach Versionen filtern"));
        versionFilter.getTooltip().setShowDelay(Duration.millis(TOOL_TIP_TIME));

        sumFilter = new RangeFilter(
                "Bausumme",
                "Reset",
                project -> project.getData().getTotalCost(),
                controller::getMinTotalCost,
                controller::getMaxTotalCost
        );

        filterBox.add(sumFilter.getTitelLabel(), 3, 0);
        filterBox.add(sumFilter.getResetButton(), 4, 0);
        filterBox.add(sumFilter.getSlider(), 3, 1);
        GridPane.setColumnSpan(sumFilter.getSlider(), 2);
        filterBox.add(sumFilter.getMinTextField(), 3, 2);
        filterBox.add(sumFilter.getMaxTextField(), 4, 2);

        Separator verticalLine2 = new Separator(Orientation.VERTICAL);
        filterBox.add(verticalLine2, 5, 0);
        GridPane.setRowSpan(verticalLine2, 3);
        GridPane.setFillHeight(verticalLine2, true);

        apartmentNrFilter = new RangeFilter(
                "Wohnungen",
                "Reset",
                project -> project.get(ProjectValues.APARTMENTS_NR),
                controller::getMinApartments,
                controller::getMaxApartments
        );

        filterBox.add(apartmentNrFilter.getTitelLabel(), 6, 0);
        filterBox.add(apartmentNrFilter.getResetButton(), 7, 0);
        filterBox.add(apartmentNrFilter.getSlider(), 6, 1);
        GridPane.setColumnSpan(apartmentNrFilter.getSlider(), 2);
        filterBox.add(apartmentNrFilter.getMinTextField(), 6, 2);
        filterBox.add(apartmentNrFilter.getMaxTextField(), 7, 2);

        Separator verticalLine3 = new Separator(Orientation.VERTICAL);
        filterBox.add(verticalLine3, 8, 0);
        GridPane.setRowSpan(verticalLine3, 3);
        GridPane.setFillHeight(verticalLine3, true);

        volumeFilter = new RangeFilter(
                "Volumen",
                "Reset",
                project -> (int) project.get(ProjectValues.VOLUME_UNDERGROUND)
                        + (int) project.get(ProjectValues.VOLUME_ABOVE_GROUND),
                controller::getMinVolume,
                controller::getMaxVolume
        );

        filterBox.add(volumeFilter.getTitelLabel(), 9, 0);
        filterBox.add(volumeFilter.getResetButton(), 10, 0);
        filterBox.add(volumeFilter.getSlider(), 9, 1);
        GridPane.setColumnSpan(volumeFilter.getSlider(), 2);
        filterBox.add(volumeFilter.getMinTextField(), 9, 2);
        filterBox.add(volumeFilter.getMaxTextField(), 10, 2);

        //listener for filter
        sumFilter.setOnFilterChanged(() -> updateFilter(filterProjectNr, versionFilter, constructionTypeFilter, projectTypeFilter));
        apartmentNrFilter.setOnFilterChanged(() -> updateFilter(filterProjectNr, versionFilter, constructionTypeFilter, projectTypeFilter));
        volumeFilter.setOnFilterChanged(() -> updateFilter(filterProjectNr, versionFilter, constructionTypeFilter, projectTypeFilter));

        filterProjectNr.setOnAction(event -> updateFilter(filterProjectNr, versionFilter, constructionTypeFilter, projectTypeFilter));

        ProjectList.getProjectList().addListener((ListChangeListener<Project>) change -> {
            sumFilter.setRange();
            apartmentNrFilter.setRange();
            volumeFilter.setRange();
        });

        versionFilter.selectedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                versionFilter.setText("neuste");
            } else {
                versionFilter.setText("vollständig");
            }
            updateFilter(filterProjectNr, versionFilter, constructionTypeFilter, projectTypeFilter);
        });

        constructionTypeFilter.setOnAction(event -> updateFilter(filterProjectNr, versionFilter, constructionTypeFilter, projectTypeFilter));

        projectTypeFilter.setOnAction(event -> updateFilter(filterProjectNr, versionFilter, constructionTypeFilter, projectTypeFilter));

        outerPane.getChildren().addAll(filterBox);
        return outerPane;
    }

    /**
     * filter projects
     * @param filterProjectNr        input filter for project number
     * @param versionFilter          input filter for version
     * @param constructionTypeFilter input filter for construction type
     * @param projectTypeFilter      input filter for project type
     */
    private void updateFilter(TextField filterProjectNr,
                              ToggleSwitch versionFilter,
                              ComboBox<String> constructionTypeFilter,
                              ComboBox<String> projectTypeFilter) {

        ProjectList.setPredicate(project -> {
            String selectedConstructionType = constructionTypeFilter.getSelectionModel().getSelectedItem();
            String selectedProjectType = projectTypeFilter.getSelectionModel().getSelectedItem();

            boolean matchSumFilter = sumFilter.getPredicate().test(project);
            boolean matchApartmentFilter = apartmentNrFilter.getPredicate().test(project);
            boolean matchVolumeFilter = volumeFilter.getPredicate().test(project);

            if (isEmpty(filterProjectNr)
                    && (selectedConstructionType == null || selectedConstructionType.equals("Alle Bauvorhaben"))
                    && (selectedProjectType == null || selectedProjectType.equals("Alle Gebäudenutzer"))
                    && !versionFilter.isSelected()) {
                return matchSumFilter && matchApartmentFilter && matchVolumeFilter;
            }

            int projectNr = project.get(ProjectValues.PROJECT_NR);
            int version = project.get(ProjectValues.VERSION);
            String constructionType = project.get(ProjectValues.CONSTRUCTION_TYPE);
            String propertyType = project.get(ProjectValues.PROPERTY_TYPE);

            boolean matchProjectNrFilter = isEmpty(filterProjectNr)
                    || String.valueOf(projectNr).contains(filterProjectNr.getText().trim());
            boolean matchConstructionType = selectedConstructionType == null
                    || selectedConstructionType.equals("Alle Bauvorhaben")
                    || constructionType.contains(selectedConstructionType);
            boolean matchProjectType = selectedProjectType == null
                    || selectedProjectType.equals("Alle Gebäudenutzer")
                    || propertyType.contains(selectedProjectType);

            int baseKey = projectNr * 100;
            int currentKey = baseKey + version;
            boolean matchVersionFilter = !versionFilter.isSelected()
                    || ProjectList.getTreeMap().subMap(currentKey + 1, baseKey + 100).isEmpty();

            return matchProjectNrFilter
                    && matchVersionFilter
                    && matchVolumeFilter
                    && matchConstructionType
                    && matchProjectType
                    && matchSumFilter
                    && matchApartmentFilter;
        });
    }

    private boolean isEmpty(TextField textField) {
        return textField.getText() == null || textField.getText().trim().isEmpty();
    }
}