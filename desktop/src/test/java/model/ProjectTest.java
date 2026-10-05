package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectTest {
    private Project project;

    @BeforeEach
    public void setup() {
        project = new Project();
    }

    @Test
    @DisplayName("Setting a correct value in a Project and getting it back.")
    public void getValueSuccessfully() {
        project.set(ProjectValues.PROJECT_NR, 12345);
        assertEquals((Integer) 12345, project.get(ProjectValues.PROJECT_NR));
    }

    @Test
    @DisplayName("Setting a wrong value in a Project and getting an Exception")
    public void setWrongValue() {
        assertThrows(IllegalArgumentException.class, () -> project.set(ProjectValues.PROJECT_NR, "String"));
    }

    @Test
    @DisplayName("Getting a field that has not been set.")
    public void getNotYetSetField() {
        assertNull(project.get(ProjectValues.PROJECT_NR));
    }

    @Test
    @DisplayName("Adding a ProjectData object")
    public void setData() {
        ProjectData data = new ProjectData();
        project.setData(data);
        assertEquals(data, project.getData());
    }

    @Test
    @DisplayName("Getting a ProjectData object that has not been set.")
    public void getData() {
        assertNull(project.getData());
    }

    @Test
    @DisplayName("Setting pinned to true, isPinned returns true.")
    public void testIsPinnedTrue() {
        project.setPinned(true);
        assertTrue(project.isPinned());
    }

    @Test
    @DisplayName("Not setting pinned to true, isPinned returns false.")
    public void testIsPinnedFalse() {
        assertFalse(project.isPinned());
    }

    @Test
    @DisplayName("Testing if the Listener fires when setPinned is called.")
    public void testSetPinnedListener() {
        AtomicBoolean fired = new AtomicBoolean(false);
        project.addPropertyChangeListener(event -> {
            if (event.getPropertyName().equals(Project.PROP_PINNED)) {
                fired.set(true);
            }
        });
        project.setPinned(true);
        assertTrue(fired.get());
    }

    @Test
    @DisplayName("Testing if two projects compare correctly.")
    public void testCompareMethod() {
        Project p1 = new Project();
        Project p2 = new Project();

        p1.set(ProjectValues.PROJECT_NR, 12345);
        p1.set(ProjectValues.VERSION, 1);

        p2.set(ProjectValues.PROJECT_NR, 12345);
        p2.set(ProjectValues.VERSION, 1);

        assertEquals(0, p1.compareTo(p2));
        assertEquals(0, p2.compareTo(p1));

        p2.set(ProjectValues.PROJECT_NR, 12345);
        p2.set(ProjectValues.VERSION, 2);

        assertEquals(-1, p1.compareTo(p2));
        assertEquals(1, p2.compareTo(p1));

        p1.set(ProjectValues.PROJECT_NR, 12346);
        p1.set(ProjectValues.VERSION, 1);

        assertEquals(99, p1.compareTo(p2));
        assertEquals(-99, p2.compareTo(p1));
    }

}
