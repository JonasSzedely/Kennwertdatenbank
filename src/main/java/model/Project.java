package model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EnumMap;

public class Project implements Comparable<Project> {
    private final EnumMap<ProjectValues, Object> values = new EnumMap<>(ProjectValues.class);
    private ProjectData data;
    private boolean pinned = false;
    private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    public static final String PROP_PINNED = "pinned";

    public void set(ProjectValues field, Object value) {
        if (field.getType().isInstance(value)) {
            values.put(field, value);
        } else {
            throw new IllegalArgumentException("Input for field " + field.getLabel() + " of wrong type: " + value.getClass() + " instead of: " + field.getType());
        }
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

    public boolean isPinned() {
        return pinned;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(listener);
    }

    public void setPinned(boolean value) {
        boolean oldValue = pinned;
        pinned = value;
        listeners.firePropertyChange(PROP_PINNED, oldValue, value);
    }

    @Override
    public int compareTo(Project o) {
        return this.hashCode() - o.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Project other
                && this.<Integer>get(ProjectValues.PROJECT_NR).equals(other.<Integer>get(ProjectValues.PROJECT_NR))
                && this.<Integer>get(ProjectValues.VERSION).equals(other.<Integer>get(ProjectValues.VERSION));
    }

    @Override
    public int hashCode() {
        return this.<Integer>get(ProjectValues.PROJECT_NR) * 100 + this.<Integer>get(ProjectValues.VERSION);
    }
}
