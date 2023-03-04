/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.spi.java.project.classpath.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.project.support.ant.AntBasedTestUtil;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.TestUtil;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * Tests for {@link ProjectClassPathImplementation}.
 * @author Tomas Zezula
 */
public class ProjectClassPathImplementationTest extends NbTestCase {
    
    private static final String PROP_NAME_1 = "classpath1"; //NOI18N
    private static final String PROP_NAME_2 = "classpath2"; //NOI18N
    
    public ProjectClassPathImplementationTest(String testName) {
        super(testName);
        FileUtil.class.getClassLoader().setClassAssertionStatus(FileUtil.class.getName(), false);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject[] cpRoots1;
    private FileObject[] cpRoots2;
    private AntProjectHelper helper;
    private PropertyEvaluator evaluator;
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(new SimpleFileOwnerQueryImplementation(), AntBasedTestUtil.testAntBasedProjectType());
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj"); //NOI18N
        cpRoots1 = new FileObject[2];
        cpRoots1[0] = scratch.createFolder("cpRoot1"); //NOI18N
        cpRoots1[1] = scratch.createFolder("cpRoot2"); //NOI18N
        cpRoots2 = new FileObject[2];
        cpRoots2[0] = scratch.createFolder("cpRoot3"); //NOI18N
        cpRoots2[1] = scratch.createFolder("cpRoot4"); //NOI18N
        helper = ProjectGenerator.createProject(projdir, "test"); //NOI18N                
        evaluator = helper.getStandardPropertyEvaluator();
        setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots1, cpRoots2});
    }
    
    public void testBootClassPathImplementation () throws Exception {
        ClassPathImplementation cpImpl = ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                FileUtil.toFile(helper.getProjectDirectory()), evaluator, new String[] {PROP_NAME_1, PROP_NAME_2});
        ClassPath cp = ClassPathFactory.createClassPath(cpImpl);
        FileObject[] fo = cp.getRoots();
        List<FileObject> expected = new ArrayList<FileObject>();
        expected.addAll(Arrays.asList(cpRoots1));
        expected.addAll(Arrays.asList(cpRoots2));
        assertEquals ("Wrong ClassPath roots",expected, Arrays.asList(fo));   //NOI18N
        cpRoots1 = new FileObject[] {cpRoots1[0]};
        setClassPath(new String[] {PROP_NAME_1}, new FileObject[][]{cpRoots1});
        fo = cp.getRoots();
        expected = new ArrayList<FileObject>();
        expected.addAll(Arrays.asList(cpRoots1));
        expected.addAll(Arrays.asList(cpRoots2));
        assertEquals ("Wrong ClassPath roots",expected, Arrays.asList(fo));   //NOI18N
        cpRoots2 = new FileObject[] {cpRoots2[0]};
        setClassPath(new String[] {PROP_NAME_2}, new FileObject[][]{cpRoots2});
        fo = cp.getRoots();
        expected = new ArrayList<FileObject>();
        expected.addAll(Arrays.asList(cpRoots1));
        expected.addAll(Arrays.asList(cpRoots2));
        assertEquals ("Wrong ClassPath roots",expected, Arrays.asList(fo));   //NOI18N
    }        
    
    public void testProjectClassPathEvents () throws Exception {
        final ClassPathImplementation cpImpl = ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                FileUtil.toFile(helper.getProjectDirectory()), evaluator, new String[] {PROP_NAME_1, PROP_NAME_2});
        final CPImplListener listener = new CPImplListener ();
        cpImpl.addPropertyChangeListener (listener);
        
        //Properties changed 4 times, the last state of properties equals to the initial value => 0 events
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots1, cpRoots2});
                setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots1, cpRoots2});
                setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots1, cpRoots2});
                setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots1, cpRoots2});
            }
        });        
        int eventCount = listener.reset();
        assertEquals(0, eventCount);
        
        //Properties changed 4 times, the last state of properties doesn't equal to the initial value => 1 event
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots2, cpRoots1});
                setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots2, cpRoots1});
                setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots2, cpRoots1});
                setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots2, cpRoots1});
            }
        });        
        eventCount = listener.reset();
        assertEquals(1, eventCount);
        
        //Properties changed 4 times, the last state of properties equals to the initial value => 0 event
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots1, cpRoots2});
                setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots1, cpRoots2});
                setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots2, cpRoots1});
                setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots2, cpRoots1});
            }
        });        
        eventCount = listener.reset();
        assertEquals(0, eventCount);
        
        //Properties changed 4 times, the last state of properties doesn't equal to the initial value => 1 event
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots2, cpRoots1});
                setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots1, cpRoots1});
                setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots1, cpRoots2});
                setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots1, cpRoots2});
            }
        });        
        eventCount = listener.reset();
        assertEquals(1, eventCount);
        
        //Properties changed 4 times outside mutex, none of new state of properties equal to the previous value => 4 events
        setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots2, cpRoots1});
        setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots1, cpRoots2});
        setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots2, cpRoots1});
        setClassPath(new String[] {PROP_NAME_1, PROP_NAME_2}, new FileObject[][] {cpRoots1, cpRoots2});
        eventCount = listener.reset();
        assertEquals(4, eventCount);
        
        cpImpl.removePropertyChangeListener(listener);
    }

    public void testBrokenRelativePath_Issue270362() throws Exception {
        setClassPath(new String[] {PROP_NAME_1}, new String[] {
            "/opencv/build/java/x86/../../../../../MyLibrary/opencv-248.jar"
        });
        ClassPathImplementation cpImpl = ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                FileUtil.toFile(helper.getProjectDirectory()),
                evaluator,
                new String[] {PROP_NAME_1});
        ClassPathFactory.createClassPath(cpImpl);
        setClassPath(new String[] {PROP_NAME_1}, new String[] {
            "/Users/tom/NetBeansProjects/bixby~simulink2j/TestSimulink/../../../../../net/10.0.0.111/tank/jag/NetBeansProjects/bixby~map-matching/dist/ClothoidMap.jar"
        });
        cpImpl = ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                FileUtil.toFile(helper.getProjectDirectory()),
                evaluator,
                new String[] {PROP_NAME_1});
        ClassPathFactory.createClassPath(cpImpl);
    }

    // XXX should test that changes are actually fired when appropriate

    private void setClassPath (String[] propNames, FileObject[][] cpRoots) {
        final String[] paths = new String[cpRoots.length];
        for (int i=0; i<cpRoots.length; i++) {
            paths[i] = toPath(cpRoots[i]);
        }
        setClassPath(propNames, paths);
    }

    private void setClassPath (String[] propNames, String[] paths) {
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        for (int i=0; i< propNames.length; i++) {
            props.setProperty (propNames[i],paths[i]);
        }
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
    }
    
    
    private static String toPath (FileObject[] cpRoots) {
        StringBuffer result = new StringBuffer ();
        for (int i=0; i<cpRoots.length; i++) {
            if (i>0) {
                result.append(':'); //NOI18N
            }
            File f = FileUtil.toFile (cpRoots[i]);
            result.append (f.getAbsolutePath());
        }
        return result.toString();
    }
    
    private static final class CPImplListener implements PropertyChangeListener {
        
        final AtomicInteger eventCounter = new AtomicInteger ();
        
        public void propertyChange(PropertyChangeEvent evt) {
            eventCounter.incrementAndGet();
        }
        
        public int reset () {
            return this.eventCounter.getAndSet(0);
        }
        
    }
    
}
