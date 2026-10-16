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
package org.netbeans.modules.project.ui.fallback;

import java.lang.ref.WeakReference;
import java.util.logging.Level;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNotSame;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import static org.netbeans.junit.NbTestCase.assertGC;
import org.netbeans.modules.projectapi.nb.NbProjectManagerAccessor;
import org.netbeans.modules.projectapi.nb.TimedWeakReference;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.test.MockLookup;

public class FallbackFactoryTest extends NbTestCase {
    static {
        // For easier testing.
        TimedWeakReference.TIMEOUT = 1000;
    }

    public FallbackFactoryTest(String name) {
        super(name);
    }

    private FileObject scratch;
    private FileObject goodproject;
    private FileObject goodproject2;
    private FileObject badproject;
    private FileObject mysteryproject;
    private FileObject justADir;
    private ProjectManager pm;

    protected @Override Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        goodproject = scratch.createFolder("good");
        goodproject.createFolder("testproject");
        goodproject2 = scratch.createFolder("good2");
        goodproject2.createFolder("testproject");
        badproject = scratch.createFolder("bad");
        badproject.createFolder("testproject").createData("broken");
        mysteryproject = scratch.createFolder("mystery");
        justADir = scratch.createFolder("justADir");
        MockLookup.setInstances(TestUtil.testProjectFactory());
        pm = ProjectManager.getDefault();
        NbProjectManagerAccessor.reset();
    }

    @Override
    protected void tearDown() throws Exception {
        scratch = null;
        goodproject = null;
        badproject = null;
        mysteryproject = null;
        pm = null;
        super.tearDown();
    }


    public void testIsFallbackProject() throws Exception {
        var nothing = pm.findProject(justADir);
        assertNull("No project is found for just a dir", nothing);
        var generic = FallbackFactory.findProjectOrFallback(justADir);
        assertNotNull("But one can ask for a fallback project", generic);

        var then = pm.findProject(justADir);
        assertSame("since then findProject works for just a dir", generic, then);

        var ref = new WeakReference<>(generic);
        generic = null;
        then = null;
        // give the references time to disappear
        Thread.sleep(TimedWeakReference.TIMEOUT);

        assertGC("The fallback project gets GCed when no longer used", ref);

        var nothingAgain = pm.findProject(justADir);
        assertNull("Since then, findProject again returns null", nothingAgain);
    }

    public void testFallbackProjectLookup() throws Exception {
        FileObject lookupDir = FileUtil.createFolder(FileUtil.getConfigRoot(), "Projects/org-netbeans-modules-project-fallback/Lookup");

        var nothing = pm.findProject(justADir);
        assertNull("No project is found for just a dir", nothing);
        var fallbackProject = FallbackFactory.findProjectOrFallback(justADir);
        assertNotNull("But one can ask for a fallback project", fallbackProject);

        var theProject = pm.findProject(justADir);
        assertSame("since then findProject works for just a dir", fallbackProject, theProject);

        Sources src = theProject.getLookup().lookup(Sources.class);
        assertNotNull("sources are provided", src);
        SourceGroup[] genSrc = src.getSourceGroups(Sources.TYPE_GENERIC);
        assertNotNull("They support the generic sources", genSrc);
        assertEquals("They support the generic sources", 1, genSrc.length);
        assertEquals("root is project root", theProject.getProjectDirectory(), genSrc[0].getRootFolder());

        MockLookupForProject notFoundInLookup = theProject.getLookup().lookup(MockLookupForProject.class);
        assertNull("No MockLookuptype found in project's lookup", notFoundInLookup);

        {
            // create a registration in the lookup
            FileObject mockLookupType = FileUtil.createData(lookupDir, MockLookupForProject.class.getName().replace(".", "-") + ".instance");
            MockLookupForProject found = FileUtil.getConfigObject(mockLookupType.getPath(), MockLookupForProject.class);
            assertNotNull("MockLookupForProject is registered in the lookup directory", found);
        }

        MockLookupForProject mockLookup = theProject.getLookup().lookup(MockLookupForProject.class);
        assertNotNull("MockLookupForProject also found in project's lookup", mockLookup);

        ActionProvider ap = theProject.getLookup().lookup(ActionProvider.class);
        assertNotNull("There are always actions for a project being merged together", ap);
        {
            var ap1 = new ActionProvider() {
                @Override
                public String[] getSupportedActions() {
                    return new String[] { "jedna", "dva" };
                }

                @Override
                public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
                    throw new UnsupportedOperationException(command);
                }

                @Override
                public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
                    return false;
                }
            };

            var ap2 = new ActionProvider() {
                @Override
                public String[] getSupportedActions() {
                    return new String[] { "odin", "dva" };
                }

                @Override
                public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
                    throw new UnsupportedOperationException(command);
                }

                @Override
                public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
                    return false;
                }
            };

            mockLookup.ic.add(ap1);

            String[] ap1Actions = theProject.getLookup().lookup(ActionProvider.class).getSupportedActions();
            assertEquals("Found two actions", 2, ap1Actions.length);

            mockLookup.ic.add(ap2);
            String[] bothActions = theProject.getLookup().lookup(ActionProvider.class).getSupportedActions();
            assertEquals("Found three actions", 3, bothActions.length);

            assertSame("Action provider instance stays the same", ap, theProject.getLookup().lookup(ActionProvider.class));

            ProjectInformation info = ProjectUtils.getInformation(theProject);
            assertNotNull("Cannot be null", info);
            assertEquals(theProject.getProjectDirectory().getNameExt(), info.getName());
            assertEquals("Folder " + justADir.getNameExt(), info.getDisplayName());
            assertNotNull("Icon is provided", info.getIcon());
            assertSame("Same icon as the group icon", info.getIcon(), genSrc[0].getIcon(true));
        }
    }

    public static final class MockLookupForProject implements org.netbeans.spi.project.LookupProvider {
        final InstanceContent ic = new InstanceContent();
        private final Lookup lkp = new AbstractLookup(ic);
        {
            ic.add(this);
        }


        @Override
        public Lookup createAdditionalLookup(Lookup baseContext) {
            return lkp;
        }
    }

    public void testFallbackProjectBecomesFileOwner() throws Exception {
        FileObject script = justADir.createData("Hello.java");
        FileObject nested = justADir.createFolder("sub").createData("Other.java");
        assertNull("Loose file has no owner before", FileOwnerQuery.getOwner(script));
        assertFalse("justADir is not a project", pm.isProject(justADir));

        Project fallback = FallbackFactory.findProjectOrFallback(justADir);

        assertNotNull("Project found", fallback);
        assertTrue("Now the directory is recognized as a project", pm.isProject(justADir));
        assertSame("but findProject returns the fallback", fallback, pm.findProject(justADir));
        assertEquals("Now the justADir files have an owner", fallback, FileOwnerQuery.getOwner(script));
        assertEquals("Nested files also have an owner", fallback, FileOwnerQuery.getOwner(nested));
    }

    public void testFallbackReplacedAfterClearNonProjectCacheUnderWriteAccess() throws Exception {
        Project fallback = FallbackFactory.findProjectOrFallback(justADir);
        Project real = ProjectManager.mutex().writeAccess((Mutex.ExceptionAction<Project>) () -> {
            // what project generators do: create metadata, clear cache, find project
            justADir.createFolder("testproject");
            pm.clearNonProjectCache();
            return pm.findProject(justADir);
        });
        Project outside = pm.findProject(justADir);
        assertNotNull("Project is found when mutex.writeAccess ends", outside);
        assertNotSame("Outside project replaces the fallback", fallback, outside);
        assertNotNull(real);
        assertNotSame("Real project replaces the fallback", fallback, real);
        assertSame("Outside and real are the same", outside, real);
    }

    public void testReplacedFallbackIsNoLongerValid() throws Exception {
        Project fallback = FallbackFactory.findProjectOrFallback(justADir);
        justADir.createFolder("testproject");
        pm.clearNonProjectCache();
        Project real = pm.findProject(justADir);
        assertNotSame(fallback, real);
        assertFalse("Replaced fallback should not be valid anymore", pm.isValid(fallback));
    }

}
