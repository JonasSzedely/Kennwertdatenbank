package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectTest {
    private Project project;

    @BeforeEach
    public void setup() {
        project = new Project();
    }

    private static Project createProject(int projectNr, int version) {
        Project p = new Project();
        p.set(ProjectAttributes.PROJECT_NR, projectNr);
        p.set(ProjectAttributes.VERSION, version);
        return p;
    }

    // --- set / get ---

    @Test
    @DisplayName("Setting a correct value in a Project and getting it back.")
    public void getValueSuccessfully() {
        project.set(ProjectAttributes.PROJECT_NR, 12345);
        assertEquals((Integer) 12345, project.get(ProjectAttributes.PROJECT_NR));
    }

    @Test
    @DisplayName("Setting a wrong value in a Project and getting an Exception")
    public void setWrongValue() {
        assertThrows(IllegalArgumentException.class, () -> project.set(ProjectAttributes.PROJECT_NR, "String"));
    }

    @Test
    @DisplayName("Setting null throws an IllegalArgumentException.")
    public void setNullValue() {
        assertThrows(IllegalArgumentException.class, () -> project.set(ProjectAttributes.PROJECT_NR, null));
    }

    @Test
    @DisplayName("Getting a field that has not been set.")
    public void getNotYetSetField() {
        assertNull(project.get(ProjectAttributes.PROJECT_NR));
    }

    // --- data ---

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

    // --- pinned ---

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
    @DisplayName("Listener does not fire when the pinned state does not change.")
    public void testSetPinnedListenerNoChange() {
        AtomicBoolean fired = new AtomicBoolean(false);
        project.addPropertyChangeListener(_ -> fired.set(true));
        project.setPinned(false);
        assertFalse(fired.get());
    }

    // --- compareTo ---

    @Test
    @DisplayName("Testing if two projects compare correctly.")
    public void testCompareMethod() {
        Project p1 = createProject(12345, 1);
        Project p2 = createProject(12345, 1);

        assertEquals(0, p1.compareTo(p2));
        assertEquals(0, p2.compareTo(p1));

        p2.set(ProjectAttributes.VERSION, 2);

        assertTrue(p1.compareTo(p2) < 0);
        assertTrue(p2.compareTo(p1) > 0);

        p1.set(ProjectAttributes.PROJECT_NR, 12346);

        assertTrue(p1.compareTo(p2) > 0);
        assertTrue(p2.compareTo(p1) < 0);
    }

    @Test
    @DisplayName("Projects are ordered by number first, then by version.")
    public void testCompareNumberBeforeVersion() {
        Project p1 = createProject(10000, 32);
        Project p2 = createProject(10001, 1);

        assertTrue(p1.compareTo(p2) < 0);
        assertTrue(p2.compareTo(p1) > 0);
    }

    @Test
    @DisplayName("TreeSet keeps all distinct project versions.")
    public void testTreeSetKeepsAllProjects() {
        TreeSet<Project> set = new TreeSet<>();
        set.add(createProject(10000, 32));
        set.add(createProject(10001, 1));
        set.add(createProject(10000, 1));

        assertEquals(3, set.size());
        assertEquals((Integer) 1, set.first().get(ProjectAttributes.VERSION));
        assertEquals((Integer) 10001, set.last().get(ProjectAttributes.PROJECT_NR));
    }

    // --- equals / hashCode ---

    @Test
    @DisplayName("Projects with same number and version are equal.")
    public void testEquals() {
        Project p1 = createProject(12345, 1);
        Project p2 = createProject(12345, 1);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    @DisplayName("Projects with different version are not equal.")
    public void testNotEquals() {
        assertNotEquals(createProject(12345, 1), createProject(12345, 2));
    }

    @Test
    @DisplayName("equals and hashCode work without project number and version.")
    public void testEqualsWithoutValues() {
        Project other = new Project();

        assertDoesNotThrow(() -> project.hashCode());
        assertEquals(project, other);
    }
}