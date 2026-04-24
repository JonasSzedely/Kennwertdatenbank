package model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.EnumMap;

public class Project {
    private final EnumMap<ProjectValues, Object> values = new EnumMap<>(ProjectValues.class);
    private ProjectData data = new ProjectData();
    private BooleanProperty pinned = new SimpleBooleanProperty(false);

    public void set(ProjectValues field, Object value) {
        values.put(field, value);
    }

    public <T> T get(ProjectValues field) {
        return (T) field.getType().cast(values.get(field));
    }

    public void setData(ProjectData data) {
        this.data = data;
    }

    public ProjectData getData() {
        return data;
    }

    public BooleanProperty pinnedProperty() {
        return pinned;
    }

    public boolean isPinned() {
        return pinned.get();
    }
}
