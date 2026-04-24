package pdf;

import javafx.collections.transformation.SortedList;
import model.ProjectCalculations;
import model.Project;
import model.ProjectValues;
import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.*;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PDFCreator {

    private SortedList<Project> sortedProjects;
    private boolean addButtonUsed = false;

    private final Color HEADER_COLOR = Color.decode("#052048");
    private final float HEADER_HEIGHT = 60f;
    private final int LOGO_HEIGHT = (int) (HEADER_HEIGHT * 0.8);
    private final int LOGO_WIDTH = LOGO_HEIGHT * 2;
    private final float COLUMN_WIDTH = 89f;
    private final float CELL_HEIGHT = 15f;
    private final float SPECIAL_CELL_HEIGHT = 30f;
    private final int LABEL_PADDING = 3;
    private int MAX_PROJECTS_TO_EXPORT = 8;

    private String title;
    private String path;
    private Image logo;
    private final float PADDING = 20f;
    private final float TITLE_HEIGHT = 14f;
    private final float TITLE_OFFSET = 0.3f;

    public PDFCreator() {
    }

    public String create(SortedList<Project> sortedProjects, String title, String path) {
        this.sortedProjects = sortedProjects;
        this.title = title;
        this.path = path;
        Document document = new Document(PageSize.A3);
        document.setMargins(20, 20, HEADER_HEIGHT + 20, 40);

        try {
            if (sortedProjects.isEmpty()) {
                return "Fehler: Keine Projekte vorhanden.";
            }

            String projectNr = title.substring(0, 5);
            String filename = projectNr + " Kennwertdatenbank.pdf";

            File dir = new File(path);
            File file = new File(dir, filename);
            String fullPath = file.getAbsolutePath();

            if (!dir.exists()) {
                dir.mkdirs();
            }

            if (file.exists() && !file.canWrite()) {
                throw new IOException("Datei ist schreibgeschützt oder geöffnet: " + fullPath);
            }

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            HeaderFooter headerFooter = new HeaderFooter();
            writer.setPageEvent(headerFooter);

            document.open();

            List<Project> projectsToExport = getProjectsToExport(sortedProjects);

            if (!projectsToExport.isEmpty()) {
                PdfPTable table = createProjectTable(projectsToExport);
                table.setHeaderRows(1);
                document.add(table);
            } else {
                document.add(new Paragraph("Keine Projekte vorhanden."));
            }

            document.close();

            String resultMessage = "PDF erfolgreich erstellt: " + fullPath;

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

    private PdfPTable createProjectTable(List<Project> projects) throws DocumentException {
        int totalColumns = 1 + projects.size();

        PdfPTable table = new PdfPTable(totalColumns);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20f);

        setFixedColumnWidths(table, projects.size());

        Font labelFont = new Font(Font.HELVETICA, 8, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 7, Font.BOLD);
        Font cellFont = new Font(Font.HELVETICA, 7, Font.NORMAL);

        // Header row: project numbers
        addLabelCell(table, ProjectValues.PROJECT_NR.getLabel(), labelFont);
        for (Project project : projects) {
            int projectNr = project.get(ProjectValues.PROJECT_NR);
            addHeaderCell(table, String.valueOf(projectNr), headerFont);
        }

        // Attribute rows — dynamically from enum, skipping PROJECT_NR (already in header)
        for (ProjectValues field : ProjectValues.values()) {
            if (field == ProjectValues.PROJECT_NR) continue;

            if (field == ProjectValues.SPECIAL) {
                addAttributeRowWithSpecialHeight(table, field.getLabel(), projects, labelFont, cellFont,
                        p -> {
                            Object val = p.get(field);
                            return val != null ? String.valueOf(val) : "";
                        });
            } else {
                addAttributeRow(table, field.getLabel(), projects, labelFont, cellFont,
                        p -> {
                            Object val = p.get(field);
                            return val != null ? String.valueOf(val) : "";
                        });
            }
        }

        // Calculation rows
        if (!projects.isEmpty()) {
            List<model.Calculation> calculations = new ProjectCalculations(projects.getFirst()).getCalculations();
            for (int i = 0; i < calculations.size(); i++) {
                final int index = i;
                String calcName = calculations.get(i).getName();
                addAttributeRow(table, calcName, projects, labelFont, cellFont,
                        p -> new ProjectCalculations(p).getCalculations().get(index).getCalculation());
            }
        }

        return table;
    }

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

    private void addAttributeRow(PdfPTable table, String label, List<Project> projects,
                                 Font labelFont, Font cellFont, ProjectValueExtractor extractor) {
        addLabelCell(table, label, labelFont);
        for (Project project : projects) {
            addDataCell(table, extractor.getValue(project), cellFont);
        }
    }

    private void addAttributeRowWithSpecialHeight(PdfPTable table, String label, List<Project> projects,
                                                  Font labelFont, Font cellFont, ProjectValueExtractor extractor) {
        addLabelCell(table, label, labelFont);
        for (Project project : projects) {
            addSpecialDataCell(table, extractor.getValue(project), cellFont);
        }
    }

    @FunctionalInterface
    interface ProjectValueExtractor {
        String getValue(Project project);
    }

    private void addLabelCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(200, 200, 200));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(LABEL_PADDING);
        cell.setFixedHeight(CELL_HEIGHT);
        table.addCell(cell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(230, 230, 230));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(LABEL_PADDING);
        cell.setFixedHeight(CELL_HEIGHT);
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(LABEL_PADDING);
        cell.setFixedHeight(CELL_HEIGHT);
        table.addCell(cell);
    }

    private void addSpecialDataCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setPadding(LABEL_PADDING);
        cell.setNoWrap(false);
        cell.setFixedHeight(SPECIAL_CELL_HEIGHT);
        table.addCell(cell);
    }

    class HeaderFooter extends PdfPageEventHelper {

        public HeaderFooter() {
            try {
                ClassLoader classLoader = getClass().getClassLoader();
                java.net.URL resource = classLoader.getResource("imag_logo_cropped.png");
                if (resource != null) {
                    logo = Image.getInstance(resource);
                    logo.scaleToFit(LOGO_WIDTH, LOGO_HEIGHT);
                } else {
                    System.err.println("Logo not found in classpath!");
                    logo = null;
                }
            } catch (Exception e) {
                System.err.println("Error loading logo: " + e.getMessage());
                logo = null;
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                Rectangle page = document.getPageSize();
                PdfContentByte canvas = writer.getDirectContent();

                canvas.saveState();
                canvas.setColorFill(HEADER_COLOR);
                canvas.rectangle(0, page.getHeight() - HEADER_HEIGHT, page.getWidth(), HEADER_HEIGHT);
                canvas.fill();
                canvas.restoreState();

                float headerBottom = page.getHeight() - HEADER_HEIGHT;
                float headerCenterY = headerBottom + (HEADER_HEIGHT / 2);

                Font leftTextFont = new Font(Font.HELVETICA, TITLE_HEIGHT, Font.BOLD, Color.WHITE);

                ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                        new Phrase("Kennwert", leftTextFont),
                        PADDING,
                        headerCenterY + TITLE_HEIGHT / 2 - (TITLE_HEIGHT * TITLE_OFFSET), 0);

                ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                        new Phrase("    Datenbank", leftTextFont),
                        PADDING,
                        headerCenterY - TITLE_HEIGHT / 2 - (TITLE_HEIGHT * TITLE_OFFSET), 0);

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

                Font titleFont = new Font(Font.HELVETICA, TITLE_HEIGHT, Font.BOLD, Color.WHITE);
                float leftBoundary = PADDING + 150;
                float rightBoundary = page.getWidth() - PADDING - LOGO_WIDTH - 10;
                float titleCenterX = leftBoundary + (rightBoundary - leftBoundary) / 2;

                ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                        new Phrase(title, titleFont),
                        titleCenterX,
                        headerCenterY - (TITLE_HEIGHT * TITLE_OFFSET), 0);

                String currentDate = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
                ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                        new Phrase(currentDate, new Font(Font.HELVETICA, 10, Font.NORMAL)),
                        PADDING, PADDING, 0);

                ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                        new Phrase(String.format("Seite %d", writer.getPageNumber()),
                                new Font(Font.HELVETICA, 10, Font.NORMAL)),
                        page.getWidth() - PADDING, PADDING, 0);

            } catch (Exception e) {
                System.err.println("Error in onEndPage: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}