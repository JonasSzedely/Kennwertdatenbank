package model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Objects;

/**
 * Represents a single version of a construction project.
 * <p>
 * A project consists of typed attributes defined by {@link ProjectAttributes} and
 * its costs per BKP code stored in {@link ProjectData}. A project is identified
 * by its project number and version.
 */
public class Project implements Comparable<Project> {

    private static final Logger LOG = LoggerFactory.getLogger(Project.class);

    /** Property name fired when the pinned state changes. */
    public static final String PROP_PINNED = "pinned";

    private static final Comparator<Project> ORDER = Comparator
            .comparing((Project p) -> p.<Integer>get(ProjectAttributes.PROJECT_NR))
            .thenComparing(p -> p.<Integer>get(ProjectAttributes.VERSION));

    private final EnumMap<ProjectAttributes, Object> attributes = new EnumMap<>(ProjectAttributes.class);
    private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    private ProjectData data;
    private boolean pinned = false;

    /**
     * Sets the value of an attribute.
     *
     * @param attribute the attribute to set
     * @param value     the new value, must match the type defined by {@code attribute}
     * @throws IllegalArgumentException if the value is {@code null} or has the wrong type
     */
    public void set(ProjectAttributes attribute, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Value for attribute " + attribute.getLabel() + " must not be null");
        }
        if (!attribute.getType().isInstance(value)) {
            throw new IllegalArgumentException("Input for attribute " + attribute.getLabel() + " of wrong type: "
                    + value.getClass().getSimpleName() + " instead of: " + attribute.getType().getSimpleName());
        }
        attributes.put(attribute, value);
    }

    /**
     * Returns the value of an attribute.
     *
     * @param attribute the attribute to read
     * @param <T>       the expected type, as defined by {@code attribute}
     * @return the value, or {@code null} if not set
     * @throws ClassCastException if {@code T} does not match the attribute type
     */
    @SuppressWarnings("unchecked")
    public <T> T get(ProjectAttributes attribute) {
        return (T) attribute.getType().cast(attributes.get(attribute));
    }

    /**
     * Sets the costs per BKP code.
     *
     * @param data the cost data
     */
    public void setData(ProjectData data) {
        this.data = data;
    }

    /**
     * Returns the costs per BKP code.
     *
     * @return the cost data, or {@code null} if not set
     */
    public ProjectData getData() {
        return data;
    }

    /**
     * Returns whether the project is pinned in the user interface.
     *
     * @return {@code true} if the project is pinned, {@code false} otherwise
     */
    public boolean isPinned() {
        return pinned;
    }

    /**
     * Registers a listener that is notified when a property changes.
     *
     * @param listener the listener to register
     * @see #PROP_PINNED
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(listener);
    }

    /**
     * Sets the pinned state and notifies registered listeners.
     *
     * @param value {@code true} to pin the project, {@code false} to unpin it
     */
    public void setPinned(boolean value) {
        boolean oldValue = pinned;
        pinned = value;
        LOG.debug("Project {} version {} pinned: {}",
                get(ProjectAttributes.PROJECT_NR), get(ProjectAttributes.VERSION), value);
        listeners.firePropertyChange(PROP_PINNED, oldValue, value);
    }

    /**
     * Orders projects by project number, then by version.
     *
     * @param o the project to compare with
     * @return a negative value, zero or a positive value if this project is
     *         less than, equal to or greater than {@code o}
     */
    @Override
    public int compareTo(Project o) {
        return ORDER.compare(this, o);
    }

    /**
     * Two projects are equal if they have the same project number and version.
     *
     * @param obj the object to compare with
     * @return {@code true} if both represent the same project version
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Project other
                && Objects.equals(get(ProjectAttributes.PROJECT_NR), other.get(ProjectAttributes.PROJECT_NR))
                && Objects.equals(get(ProjectAttributes.VERSION), other.get(ProjectAttributes.VERSION));
    }

    /**
     * Returns a hash code based on project number and version.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(get(ProjectAttributes.PROJECT_NR)) * 100
                + Objects.hashCode(get(ProjectAttributes.VERSION));
    }
}