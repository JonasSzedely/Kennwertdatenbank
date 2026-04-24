package excel;

import model.Calculation;
import model.ProjectCalculations;
import model.Project;
import model.ProjectValues;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class CreateExcel {

    private static final Locale SWISS_LOCALE = Locale.of("de", "CH");

    private final List<Project> projects;
    private final String filePath;

    public CreateExcel(List<Project> projects, String filePath) {
        this.projects = projects;
        this.filePath = filePath;
    }

    public void export() throws IOException {
        String fileName = "Kennwertdatenbank_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"))
                + ".xlsx";
        Path path = Paths.get(filePath).resolve(fileName);

        try (OutputStream output = Files.newOutputStream(path);
             Workbook wb = new Workbook(output, "Kennwertdatenbank", "1.0")) {

            Worksheet ws = wb.newWorksheet("Projekte");

            if (projects.isEmpty()) return;

            // Row labels in column 0 — dynamically from enum
            int row = 0;
            for (ProjectValues field : ProjectValues.values()) {
                ws.value(row++, 0, field.getLabel());
            }

            // Row labels for calculations
            List<Calculation> firstCalculations = new ProjectCalculations(projects.getFirst()).getCalculations();
            for (Calculation calc : firstCalculations) {
                ws.value(row++, 0, calc.getName());
            }

            // Project data, one column per project
            for (int col = 0; col < projects.size(); col++) {
                Project project = projects.get(col);
                row = 0;

                for (ProjectValues field : ProjectValues.values()) {
                    if (field == ProjectValues.CALCULATION_PHASE) {
                        int phase = project.get(field);
                        ws.value(row++, col + 1, phase + " - 5");
                    } else if (field == ProjectValues.APARTMENTS_NR) {
                        int apartments = project.get(field);
                        ws.value(row++, col + 1, apartments == 0 ? "" : String.format(SWISS_LOCALE, "%,d", apartments));
                    } else if (field.getType() == Integer.class) {
                        int value = project.get(field);
                        ws.value(row++, col + 1, value);
                    } else {
                        String value = project.get(field);
                        ws.value(row++, col + 1, value);
                    }
                }

                List<Calculation> calculations = new ProjectCalculations(project).getCalculations();
                for (Calculation calc : calculations) {
                    ws.value(row++, col + 1, calc.getCalculation());
                }
            }
        }
    }
}