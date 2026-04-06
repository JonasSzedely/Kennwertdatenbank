package view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

class SoftwareVersion {
    public static String get() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File("package.json");
            JsonNode root = mapper.readTree(file);
            return root.get("version").asText();
        } catch (IOException e) {
            System.err.println("Version konnte nicht gelesen werden: " + e.getMessage());
            return "unbekannt";
        }
    }
}