package userinterface;

import javafx.application.Application;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kennwertdatenbank.Project;
import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.*;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * PDF creation interface for generating project reports.
 * Provides a GUI for selecting output location and title,
 * then creates a PDF document with project data in table format.
 *
 * @version 1.0
 */
public class CreatePDF extends Application {

    /**
     * Sorted list of projects to display in the PDF
     */
    private final SortedList<Project> sortedProjects;
    /**
     * Header background color (blue)
     */
    private final Color HEADER_COLOR = Color.decode("#052048");
    /**
     * Header height in points
     */
    private final float HEADER_HEIGHT = 60f;
    /**
     * Logo dimensions
     */
    private final int LOGO_HEIGHT = (int) (HEADER_HEIGHT * 0.8);
    private final int LOGO_WIDTH = LOGO_HEIGHT * 2;
    /**
     * Fixed column width in points (200px ≈ 150 points)
     */
    private final float COLUMN_WIDTH = 89f;
    /**
     * Normal cell height
     */
    private final float CELL_HEIGHT = 20f;
    /**
     * Special cell height for column to fit 65 characters
     */
    private final float SPECIAL_CELL_HEIGHT = 30f;
    private final float PADDING = 20f;
    private final float TITLE_HEIGHT = 14f;
    private final float TITLE_OFFSET = 0.3f;
    /**
     * Form elements mapping
     */
    private HashMap<String, Form> forms;
    /**
     * Form validation listeners
     */
    private ArrayList<FormListener> formListeners;
    /**
     * Flag indicating if the add button was used
     */
    private boolean addButtonUsed = false;
    /**
     * Maximum projects to export
     */
    private int MAX_PROJECTS_TO_EXPORT = 8;
    private String title;
    private Image logo;

    /**
     * Constructor initializing the PDF creator with project data.
     *
     * @param sortedProjects sorted list of projects to include in PDF
     */
    public CreatePDF(SortedList<Project> sortedProjects) {
        this.sortedProjects = sortedProjects;
    }

    /**
     * Starts the PDF creation GUI.
     *
     * @param stage primary stage for the GUI
     */
    @Override
    public void start(Stage stage) {
        stage.setTitle("PDF Creator");

        VBox outerPane = new VBox(10);
        outerPane.setPadding(new Insets(20));
        outerPane.setAlignment(Pos.CENTER);
        outerPane.setStyle("-fx-background-color: white");

        Label title = new Label();
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        title.setText("Neues PDF erstellen");

        GridPane gridPane = new GridPane(10, 10);
        gridPane.setPadding(new Insets(20, 20, 20, 20));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.getColumnConstraints().addAll(
                new ColumnConstraints(150),
                new ColumnConstraints(200),
                new ColumnConstraints(200)
        );

        forms = new HashMap<>();
        formListeners = new ArrayList<>();

        // Form configuration: name;label;placeholder;errorMessage;type;maxLength(optional)
        String[] formsArray = {
                "title;Überschrift;Projektname eingeben;Bitte Überschrift eingeben;format;^[0-9]{5};5;45",
                "path;Speicherort (Ordner);C:\\Users\\Name\\Downloads;Bitte Ordner-Pfad eingeben!;text;260",
                "nOfProj; Anzahl Projekte (1-8);8;Bitte Ganzzahl eingeben (1-8);format;^[1-8]{1}$;1;1"
        };

        // Create form elements based on configuration
        int i;
        for (i = 0; i < formsArray.length; i++) {
            String[] parts = formsArray[i].split(";");
            String name = parts[0];

            if (parts[4].equals("format")) {
                forms.put(name, new InputForm(parts[1], parts[2], parts[3]));
                formListeners.add(new FormListener((InputForm) forms.get(name), name, parts[5], Integer.parseInt(parts[6]), Integer.parseInt(parts[7])
                ));
            } else if (parts[4].equals("text")) {
                forms.put(name, new InputForm(parts[1], parts[2], parts[3]));
                formListeners.add(new FormListener((InputForm) forms.get(name), name, Integer.parseInt(parts[5])));
            }

            gridPane.add(forms.get(name).getLabel(), 0, i);
            gridPane.add(forms.get(name).getInputField(), 1, i);
            gridPane.add(forms.get(name).getInvalidLabel(), 2, i);
        }

        String infoTextDefault = "Die Projektdatei wir nach der Projektnummer in der Überschrift benannt.\nEs werden die ersten 1-8 gefilterten Projekte als PDF exportiert.";
        Label infoText = new Label(infoTextDefault);
        forms.get("nOfProj").getInputField().focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (formListeners.get(2).isValid()) {
                infoText.setText("Die Projektdatei wir nach der Projektnummer in der Überschrift benannt.\nEs werden die ersten " + forms.get("nOfProj").getInput() + " Projekte als PDF exportiert.");
            } else {
                infoText.setText(infoTextDefault);
            }
        }));
        gridPane.add(infoText, 0, i);
        GridPane.setColumnSpan(infoText, 3);

        Button addButton = new Button("PDF erstellen");
        gridPane.add(addButton, 0, formsArray.length + 1);
        addButton.prefWidthProperty().bind(gridPane.widthProperty());
        GridPane.setColumnSpan(addButton, 2);

        addButton.setOnAction(event -> {
            if (validate()) {
                MAX_PROJECTS_TO_EXPORT = Integer.parseInt(forms.get("nOfProj").getInput());
                String pdfPath = forms.get("path").getInput().replaceAll("\"", "");
                String message = createPDF(
                        sortedProjects,
                        forms.get("title").getInput(),
                        pdfPath
                );

                Hyperlink textLink = new Hyperlink("Ordner öffnen.");
                textLink.setOnAction(newEvent -> getHostServices().showDocument(pdfPath));

                Label label = new Label("Daten erfolgreich exportiert. ");

                HBox content = new HBox(label, textLink);
                content.setAlignment(Pos.CENTER_LEFT);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("PDF erstellen");
                alert.setHeaderText(null);
                alert.getDialogPane().setContent(content);
                alert.showAndWait();

                addButtonUsed = true;
                stage.close();
            }
        });

        outerPane.getChildren().addAll(title, gridPane);
        Scene scene = new Scene(outerPane);

        // Apply CSS styling if available
        URL cssResource = getClass().getResource("/style.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.out.println("CSS file not found!");
        }

        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(300);
        stage.showAndWait();
    }


//starting from this line, everything is "vibecoded" 100% produced with claude.ai
//ToDo rework the code where necessary


    /**
     * Creates the PDF document with project data.
     * Only exports the first MAX_PROJECTS_TO_EXPORT projects.
     *
     * @param sortedProjects projects to include in the table
     * @param title          document title for header
     * @param dirPath        directory path to save the PDF
     * @return status message about creation result
     */
    private String createPDF(SortedList<Project> sortedProjects, String title, String dirPath) {
        // Use landscape orientation for more columns
        Document document = new Document(PageSize.A3);

        // Set margins: left, right, top, bottom
        document.setMargins(20, 20, HEADER_HEIGHT + 20, 40);

        this.title = title;

        try {
            if (sortedProjects.isEmpty()) {
                return "Fehler: Keine Projekte vorhanden.";
            }

            // Generate filename from first 5 characters of title
            String projectNr = title.substring(0, 5);
            String filename = projectNr + " Kennwertdatenbank.pdf";

            // Build full file path
            File dir = new File(dirPath);
            File file = new File(dir, filename);
            String fullPath = file.getAbsolutePath();

            // Create directory if it doesn't exist
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Check if file is writeable
            if (file.exists() && !file.canWrite()) {
                throw new IOException("Datei ist schreibgeschützt oder geöffnet: " + fullPath);
            }

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

            // Add header/footer event handler
            HeaderFooter headerFooter = new HeaderFooter();
            writer.setPageEvent(headerFooter);

            document.open();

            // Get only the first MAX_PROJECTS_TO_EXPORT projects
            List<Project> projectsToExport = getProjectsToExport(sortedProjects);

            if (!projectsToExport.isEmpty()) {
                // Create table with header row repeating on each page
                PdfPTable table = createProjectTable(projectsToExport);

                // Set the table to repeat the header row on each new page
                table.setHeaderRows(1); // WICHTIG: Erste Zeile wird auf jeder Seite wiederholt

                document.add(table);
            } else {
                document.add(new Paragraph("Keine Projekte vorhanden."));
            }

            document.close();

            // Create result message
            String resultMessage = "PDF erfolgreich erstellt: " + fullPath;

            // Add warning if projects were omitted
            if (sortedProjects.size() > MAX_PROJECTS_TO_EXPORT) {
                int omittedProjects = sortedProjects.size() - MAX_PROJECTS_TO_EXPORT;
                resultMessage += "\n\nHinweis: Es wurden nur die ersten " + MAX_PROJECTS_TO_EXPORT +
                        " Projekte exportiert.\n" + omittedProjects +
                        " weitere Projekte wurden nicht exportiert.";
            }

            return resultMessage;

        } catch (IOException e) {
            return "IO-Fehler: " + e.getMessage() +
                    "\nPrüfe: 1) PDF geschlossen? 2) Verzeichnis existiert? 3) Schreibrechte?";
        } catch (Exception e) {
            return "Fehler beim Erstellen des PDFs: " + e.getMessage();
        }
    }

    /**
     * Gets the projects to export (first MAX_PROJECTS_TO_EXPORT projects).
     *
     * @param sortedProjects all sorted projects
     * @return list of projects to export (max MAX_PROJECTS_TO_EXPORT)
     */
    private List<Project> getProjectsToExport(SortedList<Project> sortedProjects) {
        List<Project> projectsToExport = new ArrayList<>();
        int limit = Math.min(sortedProjects.size(), MAX_PROJECTS_TO_EXPORT);

        for (int i = 0; i < limit; i++) {
            projectsToExport.add(sortedProjects.get(i));
        }

        System.out.println("Exporting " + projectsToExport.size() + " of " +
                sortedProjects.size() + " total projects.");

        return projectsToExport;
    }

    /**
     * Creates a PDF table with project data.
     *
     * @param projects projects to display in the table
     * @return configured PdfPTable with project data
     * @throws DocumentException if table creation fails
     */
    private PdfPTable createProjectTable(List<Project> projects) throws DocumentException {
        // Columns: 1 for labels + 1 per project
        int totalColumns = 1 + projects.size();

        PdfPTable table = new PdfPTable(totalColumns);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20f); // Add spacing after header

        // Set fixed column widths
        setFixedColumnWidths(table, projects.size());

        // Fonts for different table sections
        Font labelFont = new Font(Font.HELVETICA, 8, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 7, Font.BOLD);
        Font cellFont = new Font(Font.HELVETICA, 7, Font.NORMAL);

        // First header row: project numbers
        addLabelCell(table, "Projekt Nummer", labelFont);
        for (Project project : projects) {
            addHeaderCell(table, String.valueOf(project.getProjectNr()), headerFont);
        }

        // Add project attribute rows
        addAttributeRow(table, "Version", projects, labelFont, cellFont,
                p -> String.valueOf(p.getVersion()));
        addAttributeRow(table, "Adresse", projects, labelFont, cellFont,
                p -> p.getAddress());
        addAttributeRow(table, "PLZ", projects, labelFont, cellFont,
                p -> String.valueOf(p.getPlz()));
        addAttributeRow(table, "Ort", projects, labelFont, cellFont,
                p -> p.getLocation());
        addAttributeRow(table, "Eigentümer", projects, labelFont, cellFont,
                p -> p.getOwner());
        addAttributeRow(table, "Gebäudeart", projects, labelFont, cellFont,
                p -> p.getPropertyType());
        addAttributeRow(table, "Bauweise", projects, labelFont, cellFont,
                p -> p.getConstructionType());
        addAttributeRow(table, "Dokument Phase", projects, labelFont, cellFont,
                p -> String.valueOf(p.getDocumentPhase()));
        addAttributeRow(table, "Kalkulation Phase", projects, labelFont, cellFont,
                p -> String.valueOf(p.getCalculationPhase()));
        addAttributeRow(table, "Wohnungen", projects, labelFont, cellFont,
                p -> String.valueOf(p.getApartmentsNr()));
        addAttributeRow(table, "Badezimmer", projects, labelFont, cellFont,
                p -> String.valueOf(p.getBathroomNr()));
        addAttributeRow(table, "HNF", projects, labelFont, cellFont,
                p -> String.valueOf(p.getHnf()));
        addAttributeRow(table, "GF", projects, labelFont, cellFont,
                p -> String.valueOf(p.getGf()));
        addAttributeRow(table, "Volumen UG", projects, labelFont, cellFont,
                p -> String.valueOf(p.getVolumeUnderground()));
        addAttributeRow(table, "Volumen OG", projects, labelFont, cellFont,
                p -> String.valueOf(p.getVolumeAboveGround()));
        addAttributeRow(table, "Volumen Total", projects, labelFont, cellFont,
                p -> String.valueOf(p.getVolume()));
        addAttributeRow(table, "Fassadenfläche", projects, labelFont, cellFont,
                p -> String.valueOf(p.getFacadeArea()));
        addAttributeRow(table, "Fensterfläche", projects, labelFont, cellFont,
                p -> String.valueOf(p.getWindowArea()));
        addAttributeRow(table, "Fassadentyp", projects, labelFont, cellFont,
                p -> p.getFacadeType());
        addAttributeRow(table, "Fenstertyp", projects, labelFont, cellFont,
                p -> p.getWindowType());
        addAttributeRow(table, "Dachtyp", projects, labelFont, cellFont,
                p -> p.getRoofType());
        addAttributeRow(table, "Heizungstyp", projects, labelFont, cellFont,
                p -> p.getHeatingType());
        addAttributeRow(table, "Kühlungstyp", projects, labelFont, cellFont,
                p -> p.getCoolingType());
        addAttributeRow(table, "Lüftung WHG", projects, labelFont, cellFont,
                p -> p.getVentilationTypeApartments());
        addAttributeRow(table, "Lüftung UG", projects, labelFont, cellFont,
                p -> p.getVentilationTypeUg());
        addAttributeRow(table, "Co No", projects, labelFont, cellFont,
                p -> p.getCoNo());

        // Special handling for "Besonderes" column with fixed height and text wrapping
        addAttributeRowWithSpecialHeight(table, "Besonderes", projects, labelFont, cellFont,
                p -> p.getSpecial());


        // Add calculation rows
        if (!projects.isEmpty()) {
            int calcCount = projects.getFirst().getCalculations().size();
            for (int i = 0; i < calcCount; i++) {
                final int index = i;
                String calcName = projects.getFirst().getCalculations().get(i).getName();
                addAttributeRow(table, calcName, projects, labelFont, cellFont,
                        p -> p.getCalculations().get(index).getCalculation());
            }
        }
        return table;
    }

    /**
     * Sets fixed column widths for all columns.
     *
     * @param table        table to configure
     * @param projectCount number of projects (data columns)
     */
    private void setFixedColumnWidths(PdfPTable table, int projectCount) {
        int totalColumns = 1 + projectCount;
        float[] columnWidths = new float[totalColumns];

        for (int i = 0; i < totalColumns; i++) {
            columnWidths[i] = COLUMN_WIDTH;
        }

        try {
            table.setTotalWidth(columnWidths);
            table.setLockedWidth(true);
        } catch (DocumentException e) {
            System.err.println("Error setting column widths: " + e.getMessage());
        }
    }

    /**
     * Adds an attribute row to the table for all projects.
     *
     * @param table     table to add row to
     * @param label     row label
     * @param projects  projects to get values from
     * @param labelFont font for label cell
     * @param cellFont  font for data cells
     * @param extractor function to extract value from project
     */
    private void addAttributeRow(PdfPTable table, String label, List<Project> projects,
                                 Font labelFont, Font cellFont, ProjectValueExtractor extractor) {
        addLabelCell(table, label, labelFont);
        for (Project project : projects) {
            String value = extractor.getValue(project);
            addDataCell(table, value, cellFont);
        }
    }

    /**
     * Adds an attribute row with special height for "Besonderes" column.
     * Fixed height ensures 65 characters can fit with text wrapping.
     *
     * @param table     table to add row to
     * @param label     row label
     * @param projects  projects to get values from
     * @param labelFont font for label cell
     * @param cellFont  font for data cells
     * @param extractor function to extract value from project
     */
    private void addAttributeRowWithSpecialHeight(PdfPTable table, String label, List<Project> projects,
                                                  Font labelFont, Font cellFont, ProjectValueExtractor extractor) {
        addLabelCell(table, label, labelFont);
        for (Project project : projects) {
            String value = extractor.getValue(project);
            addSpecialDataCell(table, value, cellFont);
        }
    }

    /**
     * Adds a label cell to the table (left column with gray background).
     *
     * @param table table to add cell to
     * @param text  cell text
     * @param font  cell font
     */
    private void addLabelCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(200, 200, 200));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setFixedHeight(CELL_HEIGHT); // Consistent height for label cells
        table.addCell(cell);
    }

    /**
     * Adds a header cell to the table (project number headers).
     *
     * @param table table to add cell to
     * @param text  cell text
     * @param font  cell font
     */
    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(230, 230, 230));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setFixedHeight(CELL_HEIGHT); // Consistent height for header cells
        table.addCell(cell);
    }

    /**
     * Adds a data cell to the table (project attribute values).
     *
     * @param table table to add cell to
     * @param text  cell text
     * @param font  cell font
     */
    private void addDataCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        cell.setFixedHeight(CELL_HEIGHT); // Consistent height for regular cells
        table.addCell(cell);
    }

    /**
     * Adds a special data cell with fixed height for text wrapping.
     * Specifically for "Besonderes" column to fit 65 characters.
     *
     * @param table table to add cell to
     * @param text  cell text
     * @param font  cell font
     */
    private void addSpecialDataCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_TOP); // Align text to top for multi-line
        cell.setPadding(4);

        // Enable text wrapping and set fixed height
        cell.setNoWrap(false); // Allow text wrapping
        cell.setFixedHeight(SPECIAL_CELL_HEIGHT); // Fixed height for 65 characters

        table.addCell(cell);
    }

    /**
     * Validates all form inputs.
     *
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validate() {
        boolean inputIsValid = false;
        for (FormListener formListener : formListeners) {
            formListener.validate();
            inputIsValid = formListener.isValid();
            formListener.setInvalidLabel(!inputIsValid);
        }
        return inputIsValid;
    }

    /**
     * Returns whether the add button was used.
     *
     * @return true if PDF was created, false otherwise
     */
    public boolean getAddButtonStatus() {
        return addButtonUsed;
    }

    /**
     * Functional interface for extracting values from Project objects.
     */
    @FunctionalInterface
    interface ProjectValueExtractor {
        /**
         * Extracts a string value from a Project.
         *
         * @param project project to extract value from
         * @return extracted value as string
         */
        String getValue(Project project);
    }

    /**
     * Inner class for handling PDF header and footer.
     * Manages logo, title, date, and page numbering.
     */
    class HeaderFooter extends PdfPageEventHelper {

        /**
         * Creates a new HeaderFooter with specified title.
         */
        public HeaderFooter() {
            try {
                // Load logo from classpath resources
                ClassLoader classLoader = getClass().getClassLoader();
                java.net.URL resource = classLoader.getResource("imag_logo_cropped.png");

                if (resource != null) {
                    logo = Image.getInstance(resource);
                    logo.scaleToFit(LOGO_WIDTH, LOGO_HEIGHT);
                    System.out.println("Logo loaded: " + logo.getScaledWidth() + "x" + logo.getScaledHeight());
                } else {
                    System.err.println("Logo not found in classpath!");
                    logo = null;
                }
            } catch (Exception e) {
                System.err.println("Error loading logo: " + e.getMessage());
                logo = null;
            }
        }

        /**
         * Called at the end of each page to draw header and footer.
         *
         * @param writer   PDF writer
         * @param document PDF document
         */
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                Rectangle page = document.getPageSize();
                PdfContentByte canvas = writer.getDirectContent();

                // Draw header background
                canvas.saveState();
                canvas.setColorFill(HEADER_COLOR);
                canvas.rectangle(0, page.getHeight() - HEADER_HEIGHT,
                        page.getWidth(), HEADER_HEIGHT);
                canvas.fill();
                canvas.restoreState();

                float headerBottom = page.getHeight() - HEADER_HEIGHT;
                float headerCenterY = headerBottom + (HEADER_HEIGHT / 2);

                // 1. LEFT TEXT: "Kennwert\n    Datenbank" vertically centered
                Font leftTextFont = new Font(Font.HELVETICA, TITLE_HEIGHT, Font.BOLD, Color.WHITE);

                // First line: "Kennwert"
                Phrase line1 = new Phrase("Kennwert", leftTextFont);

                // Second line: "    Datenbank" (indented)
                Phrase line2 = new Phrase("    Datenbank", leftTextFont);

                // Draw first line
                ColumnText.showTextAligned(
                        canvas,
                        Element.ALIGN_LEFT,
                        line1,
                        PADDING,
                        headerCenterY + TITLE_HEIGHT / 2 - (TITLE_HEIGHT * TITLE_OFFSET),
                        0
                );

                // Draw second line (below first)
                ColumnText.showTextAligned(
                        canvas,
                        Element.ALIGN_LEFT,
                        line2,
                        PADDING,
                        headerCenterY - TITLE_HEIGHT / 2 - (TITLE_HEIGHT * TITLE_OFFSET),
                        0
                );

                // 2. LOGO: Right side, vertically centered
                if (logo != null) {
                    try {
                        float logoY = headerCenterY - (logo.getScaledHeight() / 2);
                        float logoX = page.getWidth() - PADDING - logo.getScaledWidth();

                        logo.setAbsolutePosition(logoX, logoY);
                        canvas.addImage(logo);

                    } catch (Exception e) {
                        System.err.println("Error drawing logo: " + e.getMessage());
                    }
                }

                // 3. TITLE: Vertically centered and horizontally centered between left text and logo
                Font titleFont = new Font(Font.HELVETICA, TITLE_HEIGHT, Font.BOLD, Color.WHITE);

                // Calculate available space for title
                float leftBoundary = PADDING + 150;  // After left text
                float rightBoundary = page.getWidth() - PADDING - LOGO_WIDTH - 10;  // Before logo (with spacing)

                // Calculate center between leftBoundary and rightBoundary
                float titleCenterX = leftBoundary + (rightBoundary - leftBoundary) / 2;

                // Draw title centered at calculated position
                ColumnText.showTextAligned(
                        canvas,
                        Element.ALIGN_CENTER,
                        new Phrase(title, titleFont),
                        titleCenterX,
                        headerCenterY - (TITLE_HEIGHT * TITLE_OFFSET),
                        0
                );

                // 4. DATE: Bottom left (same vertical alignment as page number)
                String currentDate = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
                Font dateFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

                ColumnText.showTextAligned(
                        canvas,
                        Element.ALIGN_LEFT,
                        new Phrase(currentDate, dateFont),
                        PADDING,
                        PADDING,
                        0
                );

                // 5. PAGE NUMBER: Bottom right (German text)
                Font pageFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
                ColumnText.showTextAligned(
                        canvas,
                        Element.ALIGN_RIGHT,
                        new Phrase(String.format("Seite %d", writer.getPageNumber()), pageFont),
                        page.getWidth() - PADDING,
                        PADDING,
                        0
                );

            } catch (Exception e) {
                System.err.println("Error in onEndPage: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}