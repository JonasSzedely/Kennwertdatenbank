package userinterface;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kennwertdatenbank.Project;
import org.openpdf.text.*;
import org.openpdf.text.pdf.*;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    /** Sorted list of projects to display in the PDF */
    private SortedList<Project> sortedProjects;

    /** Form elements mapping */
    private HashMap<String, Form> forms;

    /** Form validation listeners */
    private ArrayList<FormListener> formListeners;

    /** Button to trigger PDF creation */
    private final Button addButton = new Button("PDF erstellen");

    /** Flag indicating if the add button was used */
    private boolean addButtonUsed = false;

    /** Header background color (blue) */
    private static final Color HEADER_COLOR = Color.decode("#052048");

    /** Header height in points */
    private static final float HEADER_HEIGHT = 80f;

    /** Logo dimensions */
    private static final int LOGO_WIDTH = 60;
    private static final int LOGO_HEIGHT = 60;

    /** Fixed column width in points (200px ≈ 150 points) */
    private static final float COLUMN_WIDTH = 120f;

    /** Normal cell height */
    private static final float CELL_HEIGHT = 20f;

    /** Special cell height for column to fit 65 characters */
    private static final float SPECIAL_CELL_HEIGHT = 30f;

    /** Maximum projects to export */
    private static final int MAX_PROJECTS_TO_EXPORT = 8;

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
     * @throws Exception if GUI initialization fails
     */
    @Override
    public void start(Stage stage) throws Exception {
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
                new ColumnConstraints(150)
        );

        forms = new HashMap<>();
        formListeners = new ArrayList<>();

        // Form configuration: name;label;placeholder;errorMessage;type;maxLength(optional)
        String[] formsArray = {
                "title;Überschrift;Projektname eingeben;Bitte Überschrift eingeben;text;45",
                "path;Speicherort (Ordner);C:\\Users\\Name\\Downloads;Bitte Ordner-Pfad eingeben!;path"
        };

        // Create form elements based on configuration
        for (int i = 0; i < formsArray.length; i++) {
            String[] parts = formsArray[i].split(";");
            String name = parts[0];

            if (parts[4].equals("text")) {
                forms.put(name, new InputForm(parts[1], parts[2], parts[3]));
                formListeners.add(new FormListener(
                        (InputForm) forms.get(name),
                        name,
                        Integer.parseInt(parts[5])
                ));
            } else if (parts[4].equals("path")) {
                forms.put(name, new InputForm(parts[1], parts[2], parts[3]));
                formListeners.add(new FormListener((InputForm) forms.get(name), name));
            }

            gridPane.add(forms.get(name).getLabel(), 0, i);
            gridPane.add(forms.get(name).getInputField(), 1, i);
            gridPane.add(forms.get(name).getInvalidLabel(), 2, i);
        }

        gridPane.add(addButton, 0, formsArray.length);
        GridPane.setColumnSpan(addButton, 2);

        addButton.setOnAction(event -> {
            if (validate()) {
                String message = createPDF(
                        sortedProjects,
                        forms.get("title").getInput(),
                        forms.get("path").getInput().replaceAll("\"", "")
                );

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("PDF erstellt");
                alert.setHeaderText("Rückmeldung");
                alert.setContentText(message + "\nDrücken Sie OK um den Vorgang zu beenden.");
                alert.showAndWait();
                addButtonUsed = true;
                stage.close();
            }
        });

        outerPane.getChildren().addAll(title, gridPane);
        Scene scene = new Scene(outerPane);

        // Apply CSS styling if available
        var cssResource = getClass().getResource("/style.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.out.println("CSS file not found!");
        }

        stage.setScene(scene);
        stage.setMinWidth(500);
        stage.setMinHeight(300);
        stage.showAndWait();
    }



//starting from this line, everything is "vibecoded" 100% produced with claude.ai and DeepSeek
//ToDo rework the code where necessary



    /**
     * Creates the PDF document with project data.
     * Only exports the first MAX_PROJECTS_TO_EXPORT projects.
     *
     * @param sortedProjects projects to include in the table
     * @param title document title for header
     * @param dirPath directory path to save the PDF
     * @return status message about creation result
     */
    private String createPDF(SortedList<Project> sortedProjects, String title, String dirPath) {
        // Use landscape orientation for more columns
        Document document = new Document(PageSize.A4.rotate());

        // Set margins: left, right, top, bottom
        document.setMargins(20, 20, HEADER_HEIGHT + 20, 40);

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
            HeaderFooter headerFooter = new HeaderFooter(title);
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

        // First header row: project numbers - WICHTIG: Diese wird auf jeder Seite wiederholt
        addLabelCell(table, "Projekt Nummer", labelFont);
        for (Project project : projects) {
            addHeaderCell(table, String.valueOf(project.getProjectNr()), headerFont);
        }

        // Add project attribute rows with German labels
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

        addAttributeRow(table, "Baukosten Total", projects, labelFont, cellFont,
                p -> String.valueOf(p.getData().getTotalCost()));

        // Add calculation rows
        if (!projects.isEmpty()) {
            int calcCount = projects.get(0).getCalculations().size();
            for (int i = 0; i < calcCount; i++) {
                final int index = i;
                String calcName = projects.get(0).getCalculations().get(i).getName();
                addAttributeRow(table, calcName, projects, labelFont, cellFont,
                        p -> p.getCalculations().get(index).getCalculation());
            }
        }

        return table;
    }

    /**
     * Sets fixed column widths for all columns.
     *
     * @param table table to configure
     * @param projectCount number of projects (data columns)
     */
    private void setFixedColumnWidths(PdfPTable table, int projectCount) {
        try {
            // Calculate available width
            float availableWidth = PageSize.A4.rotate().getWidth() - 40;

            // Calculate total columns
            int totalColumns = 1 + projectCount;

            // Calculate width per column (ensure it fits)
            float widthPerColumn = Math.min(COLUMN_WIDTH, availableWidth / totalColumns);

            // Ensure minimum width
            widthPerColumn = Math.max(widthPerColumn, 80f);

            float[] columnWidths = new float[totalColumns];

            // First column (labels)
            columnWidths[0] = widthPerColumn;

            // All data columns get same width
            for (int i = 1; i < columnWidths.length; i++) {
                columnWidths[i] = widthPerColumn;
            }

            table.setWidths(columnWidths);

            System.out.println("Table with " + projectCount + " projects: " +
                    totalColumns + " columns, " + widthPerColumn + " points each");

        } catch (DocumentException e) {
            System.err.println("Error setting column widths: " + e.getMessage());
            // Fallback: use percentage
            table.setWidthPercentage(100);
        }
    }

    /**
     * Adds an attribute row to the table for all projects.
     *
     * @param table table to add row to
     * @param label row label
     * @param projects projects to get values from
     * @param labelFont font for label cell
     * @param cellFont font for data cells
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
     * @param table table to add row to
     * @param label row label
     * @param projects projects to get values from
     * @param labelFont font for label cell
     * @param cellFont font for data cells
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
     * Adds a label cell to the table (left column with gray background).
     *
     * @param table table to add cell to
     * @param text cell text
     * @param font cell font
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
     * @param text cell text
     * @param font cell font
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
     * @param text cell text
     * @param font cell font
     */
    private void addDataCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
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
     * @param text cell text
     * @param font cell font
     */
    private void addSpecialDataCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_TOP); // Align text to top for multi-line
        cell.setPadding(4);

        // Enable text wrapping and set fixed height
        cell.setNoWrap(false); // Allow text wrapping
        cell.setFixedHeight(SPECIAL_CELL_HEIGHT); // Fixed height for 65 characters

        table.addCell(cell);
    }

    /**
     * Inner class for handling PDF header and footer.
     * Manages logo, title, date, and page numbering.
     */
    class HeaderFooter extends PdfPageEventHelper {
        private String title;
        private Image logo;
        private static final float LOGO_WIDTH = 60f;
        private static final float LOGO_HEIGHT = 60f;
        private static final float PADDING = 20f;

        /**
         * Creates a new HeaderFooter with specified title.
         *
         * @param title document title to display in header
         */
        public HeaderFooter(String title) {
            this.title = title;

            try {
                // Load logo from classpath resources
                ClassLoader classLoader = getClass().getClassLoader();
                java.net.URL resource = classLoader.getResource("imag_logo.png");

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
         * @param writer PDF writer
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

                float headerTop = page.getHeight();
                float headerBottom = page.getHeight() - HEADER_HEIGHT;
                float headerCenterY = headerBottom + (HEADER_HEIGHT / 2);

                // 1. LEFT TEXT: "Kennwert\n    Datenbank" vertically centered
                Font leftTextFont = new Font(Font.HELVETICA, 14, Font.BOLD, Color.WHITE);

                // First line: "Kennwert"
                Phrase line1 = new Phrase("Kennwert", leftTextFont);

                // Second line: "    Datenbank" (indented)
                Phrase line2 = new Phrase("    Datenbank", leftTextFont);

                // Calculate line height
                float lineHeight = 16f;

                // Y position for vertical centering (for 2 lines)
                float textStartY = headerCenterY + (lineHeight / 2);

                // Draw first line
                ColumnText.showTextAligned(
                        canvas,
                        Element.ALIGN_LEFT,
                        line1,
                        PADDING,
                        textStartY,
                        0
                );

                // Draw second line (below first)
                ColumnText.showTextAligned(
                        canvas,
                        Element.ALIGN_LEFT,
                        line2,
                        PADDING,
                        textStartY - lineHeight,
                        0
                );

                // 2. LOGO: Right side, vertically centered
                float logoWidth = 0;
                if (logo != null) {
                    try {
                        float logoY = headerCenterY - (logo.getScaledHeight() / 2);
                        float logoX = page.getWidth() - PADDING - logo.getScaledWidth();
                        logoWidth = logo.getScaledWidth();

                        logo.setAbsolutePosition(logoX, logoY);
                        canvas.addImage(logo);

                    } catch (Exception e) {
                        System.err.println("Error drawing logo: " + e.getMessage());
                    }
                }

                // 3. TITLE: Vertically centered and horizontally centered between left text and logo
                Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, Color.WHITE);

                // Calculate available space for title
                float leftBoundary = PADDING + 150;  // After left text
                float rightBoundary = page.getWidth() - PADDING - logoWidth - 10;  // Before logo (with spacing)

                // Calculate center between leftBoundary and rightBoundary
                float titleCenterX = leftBoundary + (rightBoundary - leftBoundary) / 2;

                // Draw title centered at calculated position
                ColumnText.showTextAligned(
                        canvas,
                        Element.ALIGN_CENTER,
                        new Phrase(title, titleFont),
                        titleCenterX,
                        headerCenterY,
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
            formListener.setInvalidLabel(inputIsValid);
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
}