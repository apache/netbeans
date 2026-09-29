package org.netbeans.modules.ide.ergonomics.fod;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ProjectInformationProvider;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 * Tests the Sources contract before and after activation of a feature project.
 * Module activation and notification UI are deliberately outside this unit test.
 */
public class FeatureProjectSourcesTest extends NbTestCase {

    // Keep the ergonomics test independent of the Java project API.
    private static final String SOURCES_TYPE_JAVA = "java";

    private FileObject projectDirectory;
    private Project project;
    private Sources sources;
    private RecordingNotificationDisplayer notifications;

    public FeatureProjectSourcesTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        notifications = new RecordingNotificationDisplayer();
        ProjectInformationProvider information = p -> p.getLookup().lookup(ProjectInformation.class);
        // Do not discover installed IDE services or enable any modules.
        MockLookup.setLookup(Lookups.fixed(notifications, information, getClass().getClassLoader()));
        projectDirectory = FileUtil.createMemoryFileSystem().getRoot().createFolder("project");
        FeatureInfo feature = FeatureInfo.create("test", null,
                getClass().getResource("FeatureProjectSourcesTest.properties"));
        project = createFeatureProject(projectDirectory, feature);
        sources = ProjectUtils.getSources(project);
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            MockLookup.setLookup(Lookup.EMPTY);
        } finally {
            super.tearDown();
        }
    }

    public void testGenericSourcesContainProjectDirectory() {
        SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);

        assertEquals("An inactive project must retain its generic source root", 1, groups.length);
        assertEquals("The generic root is the project directory",
                projectDirectory, groups[0].getRootFolder());
    }

    public void testGenericSourcesDoNotRequestInitialization() {
        var found = sources.getSourceGroups(Sources.TYPE_GENERIC);
        assertEquals("found one", 1, found.length);

        assertEquals("Generic sources are available without enabling project support",
                0, notifications.count);
    }

    public void testMissingJavaSourcesRequestInitializationOnlyOnce() {
        SourceGroup[] firstQuery = sources.getSourceGroups(SOURCES_TYPE_JAVA);
        assertEquals("No java sources", 0, firstQuery.length);

        SourceGroup[] secondQuery = sources.getSourceGroups(SOURCES_TYPE_JAVA);
        assertEquals("Still no java sources", 0, secondQuery.length);

        assertEquals("Repeated queries must share one initialization notification",
                1, notifications.count);
        assertNotNull("The notification offers an action to open the project",
                notifications.action);
    }

    public void testSourcesDelegateAfterAssociation() throws Exception {
        TestProject realProject = new TestProject(projectDirectory);
        associate(project, realProject);

        assertEquals("The retained Sources instance delegates generic groups",
                realProject.genericGroup,
                sources.getSourceGroups(Sources.TYPE_GENERIC)[0]);
        assertEquals("The retained Sources instance delegates Java groups",
                realProject.javaGroup,
                sources.getSourceGroups(SOURCES_TYPE_JAVA)[0]);
        assertEquals("Unknown source types remain empty", 0,
                sources.getSourceGroups("unknown").length);
        assertEquals("An initialized project needs no notification", 0, notifications.count);
    }

    public void testAssociationNotifiesSourcesListeners() throws Exception {
        assertEquals("There are no Java source groups before activation", 0,
                sources.getSourceGroups(SOURCES_TYPE_JAVA).length);
        ChangeCounter listener = new ChangeCounter();
        sources.addChangeListener(listener);

        associate(project, new TestProject(projectDirectory));

        assertEquals("Java source groups become available after activation", 1,
                sources.getSourceGroups(SOURCES_TYPE_JAVA).length);
        assertTrue("Changing the source groups on association must notify listeners",
                listener.count > 0);
    }

    public void testDelegateChangesReachRegisteredListeners() throws Exception {
        ChangeCounter listener = new ChangeCounter();
        sources.addChangeListener(listener);
        TestProject realProject = new TestProject(projectDirectory);
        listener.count = 0;
        associate(project, realProject);

        FileObject replacementRoot = projectDirectory.createFolder("generated");
        realProject.setJavaRoot(replacementRoot);

        SourceGroup[] javaSources = sources.getSourceGroups(SOURCES_TYPE_JAVA);
        assertNotNull("There are Java sources in " + project, javaSources);
        assertEquals("There are non-empty Java sources in " + project, 1, javaSources.length);
        assertEquals("The retained Sources instance sees the new Java root",
                replacementRoot, javaSources[0].getRootFolder());
        assertTrue("Changes from the real Sources must reach previously registered listeners",
                listener.count > 0);

        sources.removeChangeListener(listener);
        listener.count = 0;
        realProject.setJavaRoot(projectDirectory.createFolder("other"));
        assertEquals("Removed listeners must no longer receive changes", 0, listener.count);
    }

    /**
     * Construct the private placeholder without starting the module system.
     * Keep reflection at this boundary; assertions use the public Sources API.
     */
    private static Project createFeatureProject(FileObject directory, FeatureInfo feature)
            throws ReflectiveOperationException {
        Class<? extends Project> type = Class.forName(
                FeatureProjectFactory.class.getName() + "$FeatureNonProject").asSubclass(Project.class);
        Constructor<? extends Project> constructor = type.getDeclaredConstructor(
                FileObject.class, FeatureInfo.class, ProjectState.class, List.class);
        constructor.setAccessible(true);
        ProjectState state = new ProjectState() {
            @Override
            public void markModified() {
                fail("A Sources query must not modify the project");
            }

            @Override
            public void notifyDeleted() {
                fail("This unit test must not activate the project through ProjectManager");
            }
        };
        return constructor.newInstance(directory, feature, state, Collections.emptyList());
    }

    /** Simulate the handover performed by FeatureNonProject.switchToReal(). */
    private static void associate(Project placeholder, Project realProject)
            throws ReflectiveOperationException {
        var delegate = placeholder.getLookup().lookup(FeatureProjectFactory.FeatureDelegate.class);
        assertNotNull("The placeholder supplies its delegate in the lookup", delegate);
        delegate.associate(realProject);
    }

    private static final class ChangeCounter implements ChangeListener {
        int count;

        @Override
        public void stateChanged(ChangeEvent event) {
            count++;
        }
    }

    private static final class RecordingNotificationDisplayer extends NotificationDisplayer {
        private int count;
        private ActionListener action;

        @Override
        public Notification notify(String title, Icon icon, String details,
                ActionListener action, Priority priority) {
            count++;
            this.action = action;
            return new Notification() {
                @Override
                public void clear() {
                }
            };
        }

        @Override
        public Notification notify(String title, Icon icon, JComponent balloonDetails,
                JComponent popupDetails, Priority priority) {
            throw new AssertionError("Expected a text notification with an opening action");
        }
    }

    private static final class TestProject implements Project, Sources {
        private final FileObject directory;
        private final Lookup lookup;
        private final ChangeSupport changes = new ChangeSupport(this);
        private final SourceGroup genericGroup;
        private SourceGroup javaGroup;

        TestProject(FileObject directory) throws IOException {
            this.directory = directory;
            lookup = Lookups.singleton(this);
            genericGroup = GenericSources.group(this, directory, "generic", "Project", null, null);
            setJavaRoot(directory.createFolder("src"));
        }

        void setJavaRoot(FileObject root) {
            javaGroup = GenericSources.group(this, root, "java", "Java Sources", null, null);
            changes.fireChange();
        }

        @Override
        public FileObject getProjectDirectory() {
            return directory;
        }

        @Override
        public Lookup getLookup() {
            return lookup;
        }

        @Override
        public SourceGroup[] getSourceGroups(String type) {
            if (Sources.TYPE_GENERIC.equals(type)) {
                return new SourceGroup[] { genericGroup };
            }
            if (SOURCES_TYPE_JAVA.equals(type)) {
                return new SourceGroup[] { javaGroup };
            }
            return new SourceGroup[0];
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            changes.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            changes.removeChangeListener(listener);
        }
    }
}
