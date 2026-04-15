package excel;

import model.Calculation;
import model.Project;
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

    private static final String[] ROW_LABELS = {
            "Projekt-Nr.",
            "Version",
            "Adresse",
            "PLZ",
            "Ort",
            "Bauherr",
            "Gebäudenutzungen",
            "Art des Bauvorhaben",
            "Planstand",
            "Gerechnete Phasen",
            "Anzahl Wohnungen",
            "HNF in m²",
            "GF in m²",
            "SIA m³",
            "Fassaden-Typ",
            "Fenster-Typ",
            "Dach-Typ",
            "Heizungs-Typ",
            "Kühlungs-Typ",
            "Lüftungs-Typ Whg.",
            "Lüftungs-Typ TG",
            "CO/NO-Anlage",
            "Spezielles"
    };

    private final List<Project> projects;
    private final String filePath;

    public CreateExcel(List<Project> projects, String filePath) {
        this.projects = projects;
        this.filePath = filePath;
    }

    public void export() throws IOException {
        String fileName = "Kennwertdatenbank_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")) + ".xlsx";
        Path path = Paths.get(filePath).resolve(fileName);
        try (OutputStream output = Files.newOutputStream(path);
             Workbook wb = new Workbook(output, "Kennwertdatenbank", "1.0")) {
            Worksheet ws = wb.newWorksheet("Projekte");


            if (!projects.isEmpty()) {
                // Row labels in column 0
                int row = 0;
                for (String str : ROW_LABELS) {
                    ws.value(row++, 0, str);
                }
                // Row labels in column 0 for calculations
                for (Calculation calc : projects.getFirst().getCalculations()) {
                    ws.value(row++, 0, calc.getName());
                }

                // Project data, one column per project
                for (int col = 0; col < projects.size(); col++) {
                    Project project = projects.get(col);
                    row = 0;

                    ws.value(row++, col + 1, project.getProjectNr());
                    ws.value(row++, col + 1, project.getVersion());
                    ws.value(row++, col + 1, project.getAddress());
                    ws.value(row++, col + 1, project.getPlz());
                    ws.value(row++, col + 1, project.getLocation());
                    ws.value(row++, col + 1, project.getOwner());
                    ws.value(row++, col + 1, project.getPropertyType());
                    ws.value(row++, col + 1, project.getConstructionType());
                    ws.value(row++, col + 1, project.getDocumentPhase());
                    ws.value(row++, col + 1, project.getCalculationPhase() + " - 5");
                    ws.value(row++, col + 1, project.getApartmentsNr() == 0 ? "" : String.format(SWISS_LOCALE, "%,d", project.getApartmentsNr()));
                    ws.value(row++, col + 1, project.getHnf());
                    ws.value(row++, col + 1, project.getGf());
                    ws.value(row++, col + 1, project.getVolume());
                    ws.value(row++, col + 1, project.getFacadeType());
                    ws.value(row++, col + 1, project.getWindowType());
                    ws.value(row++, col + 1, project.getRoofType());
                    ws.value(row++, col + 1, project.getHeatingType());
                    ws.value(row++, col + 1, project.getCoolingType());
                    ws.value(row++, col + 1, project.getVentilationTypeApartments());
                    ws.value(row++, col + 1, project.getVentilationTypeUg());
                    ws.value(row++, col + 1, project.getCoNo());
                    ws.value(row++, col + 1, project.getSpecial());

                    for (Calculation calc : project.getCalculations()) {
                        ws.value(row++, col + 1, calc.getCalculation());
                    }
                }
            }
        }
    }
}