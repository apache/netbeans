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
package org.netbeans.modules.gradle.loaders;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import static junit.framework.TestCase.assertNotNull;
import org.gradle.internal.impldep.com.google.common.collect.Streams;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.modules.gradle.api.BuildPropertiesSupport;
import org.netbeans.modules.gradle.api.BuildPropertiesSupport.PropertyKind;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.options.GradleExperimentalSettings;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.DummyInstalledFileLocator;
import org.openide.windows.IOProvider;

/**
 *
 * @author sdedic
 */
public class ExtensionPropertiesExtractorTest extends NbTestCase {

    public ExtensionPropertiesExtractorTest(String name) {
        super(name);
    }
    
    
    private static File getTestNBDestDir() {
        String destDir = System.getProperty("test.netbeans.dest.dir");
        // set in project.properties as test-unit-sys-prop.test.netbeans.dest.dir
        assertNotNull("test.netbeans.dest.dir property has to be set when running within binary distribution", destDir);
        return new File(destDir);
    }

    File destDirF;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // This is needed, otherwose the core window's startup code will redirect
        // System.out/err to the IOProvider, and its Trivial implementation will redirect
        // it back to System.err - loop is formed. Initialize IOProvider first, it gets
        // the real System.err/out references.
        IOProvider p = IOProvider.getDefault();
        System.setProperty("test.reload.sync", "true");
        
        destDirF = getTestNBDestDir();
        clearWorkDir();
    
        DummyInstalledFileLocator.registerDestDir(destDirF);
        GradleExperimentalSettings.getDefault().setOpenLazy(false);
    }

    FileObject projectDir;

    private Project makeProject(String subdir) throws Exception {
        FileObject src = FileUtil.toFileObject(getDataDir()).getFileObject(subdir);
        projectDir = FileUtil.copyFile(src, FileUtil.toFileObject(getWorkDir()), src.getNameExt());
        
        Project p = ProjectManager.getDefault().findProject(projectDir);
        assertNotNull(p);
        ProjectTrust.getDefault().trustProject(p);
        
        OpenProjects.getDefault().open(new Project[] { p }, true);
        OpenProjects.getDefault().openProjects().get();
        
        NbGradleProject.get(p).toQuality("Load data", NbGradleProject.Quality.FULL, false).toCompletableFuture().get();
        return p;
    }
    
    public void testSimpleExtensionProperty() throws Exception {
        Project p = makeProject("buildprops/micronaut");
        BuildPropertiesSupport support = BuildPropertiesSupport.get(p);
        assertNotNull(support);
        
        BuildPropertiesSupport.Property prop = support.findExtensionProperty("application", "mainClass");
        assertNotNull(prop);
        assertEquals(BuildPropertiesSupport.PropertyKind.PRIMITIVE, prop.getKind());
        assertEquals("com.example.Application", prop.getStringValue());
        assertEquals("java.lang.String", prop.getType());
        
        assertTrue(support.keys(prop).isEmpty());
        assertFalse(support.items(prop, null).iterator().hasNext());
        
        prop = support.findExtensionProperty("base", "distsDirectory");
        assertNotNull(prop);
        assertEquals(BuildPropertiesSupport.PropertyKind.STRUCTURE, prop.getKind());
        String s = prop.getStringValue();
        assertNotNull("Paths and directories are convertible to String", s);
        assertTrue(s.endsWith(Paths.get("micronaut", "build", "distributions").toString()));

        assertTrue(support.keys(prop).isEmpty());
        assertFalse(support.items(prop, null).iterator().hasNext());
    }
    
    public void testListExtensionProperty() throws Exception {
        Project p = makeProject("buildprops/micronaut");
        BuildPropertiesSupport support = BuildPropertiesSupport.get(p);
        assertNotNull(support);
        
        BuildPropertiesSupport.Property prop;
        
        prop = support.findExtensionProperty("nativeTest", "runtimeArgs");
        assertNotNull(prop);
        assertEquals(BuildPropertiesSupport.PropertyKind.LIST, prop.getKind());
        Iterable<BuildPropertiesSupport.Property> it = support.items(prop, null);
        assertNotNull(it);
        Iterator<BuildPropertiesSupport.Property> iter = it.iterator();
        assertTrue(iter.hasNext());
        
        assertTrue(support.keys(prop).isEmpty());
        
        BuildPropertiesSupport.Property item = iter.next();
        assertEquals(BuildPropertiesSupport.PropertyKind.PRIMITIVE, item.getKind());
        assertTrue(item.getStringValue().contains("--xml-output-dir"));
        assertTrue(iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());
    }
    
    public void testMapExtensionProperty() throws Exception {
        Project p = makeProject("buildprops/micronaut");
        BuildPropertiesSupport support = BuildPropertiesSupport.get(p);
        assertNotNull(support);
        
        BuildPropertiesSupport.Property prop;
        

        prop = support.findExtensionProperty("eclipse", "classpath.sourceSets");
        assertNotNull(prop);
        assertEquals(BuildPropertiesSupport.PropertyKind.MAP, prop.getKind());
        Collection<String> keys = support.keys(prop);
        assertNotNull(keys);
        assertEquals(2, keys.size());

        assertFalse(support.items(prop, null).iterator().hasNext());
        
        BuildPropertiesSupport.Property item;
        
        item = support.get(prop, "main", null);
        assertNotNull(item);
        item = support.get(prop, "test", null);
        assertNotNull(item);
    }
    
    public void testAddedListExtensionProperty() throws Exception {
        Project p = makeProject("buildprops/micronaut");
        BuildPropertiesSupport support = BuildPropertiesSupport.get(p);
        assertNotNull(support);
        
        BuildPropertiesSupport.Property prop;
        

        prop = support.findExtensionProperty("graalvmNative", "binaries.main.buildArgs");
        assertEquals(BuildPropertiesSupport.PropertyKind.LIST, prop.getKind());
        Iterable<BuildPropertiesSupport.Property> it = support.items(prop, null);
        assertNotNull(it);
        Iterator<BuildPropertiesSupport.Property> iter = it.iterator();
        assertTrue(iter.hasNext());
        
        List<String> l = Streams.stream(iter).map(BuildPropertiesSupport.Property::getStringValue).collect(Collectors.toList());
        assertEquals(Arrays.asList("x", "y", "z"), l);
    }

    public void testMapExtensionProperty2() throws Exception {
        Project p = makeProject("buildprops/micronaut");
        BuildPropertiesSupport support = BuildPropertiesSupport.get(p);
        assertNotNull(support);
        
        BuildPropertiesSupport.Property prop;
        

        prop = support.findExtensionProperty("graalvmNative", "binaries.main.systemProperties");
        assertNotNull(prop);
        assertEquals(BuildPropertiesSupport.PropertyKind.MAP, prop.getKind());
        Collection<String> keys = support.keys(prop);
        assertNotNull(keys);
        assertEquals(3, keys.size());

        assertFalse(support.items(prop, null).iterator().hasNext());
        
        BuildPropertiesSupport.Property item;
        
        item = support.get(prop, "prop1", null);
        assertNotNull(item);
        assertEquals("value1", item.getStringValue());
        item = support.get(prop, "prop2", null);
        assertNotNull(item);
        assertEquals("value2", item.getStringValue());
        item = support.get(prop, "prop;;3", null);
        assertNotNull(item);
        assertEquals("value3", item.getStringValue());
    }
    
    /**
     * Keys are ;; delimited, check various weird keys that needs to be encoded, if they decode back properly.
     * @throws Exception 
     */
    public void testWeirdKeysMapExtensionProperty() throws Exception {
        Project p = makeProject("buildprops/micronaut");
        BuildPropertiesSupport support = BuildPropertiesSupport.get(p);
        assertNotNull(support);
        
        BuildPropertiesSupport.Property prop;
        prop = support.findExtensionProperty("graalvmNative", "weirdKeys");
        prop = support.get(prop, "main", null);
        prop = support.findExtensionProperty("graalvmNative", null);
        BuildPropertiesSupport.Property prop2 = support.get(prop, "binaries", null);
        prop = support.findExtensionProperty("graalvmNative", "weirdKeys");
        
        
        Collection<String> keys = support.keys(prop);
        assertNotNull(keys);
        
        List<String> sorted = new ArrayList<>(keys);
        Collections.sort(sorted);
        
        assertEquals(Arrays.asList("normalKey", "semi;;key", "semi;\\;key", "semi;key"), sorted);
    }
    

    public void testSimpleTaskProperty() throws Exception {
        Project p = makeProject("buildprops/micronaut");
        BuildPropertiesSupport support = BuildPropertiesSupport.get(p);
        assertNotNull(support);
        
        BuildPropertiesSupport.Property prop = support.findTaskProperty("installDist", "caseSensitive");
        assertNotNull(prop);
        assertEquals(BuildPropertiesSupport.PropertyKind.PRIMITIVE, prop.getKind());
        assertEquals("true", prop.getStringValue());
        assertEquals("boolean", prop.getType());
        
        assertTrue(support.keys(prop).isEmpty());
        assertFalse(support.items(prop, null).iterator().hasNext());
        
        prop = support.findTaskProperty("nativeCompile", "options");
        assertNotNull(prop);
        assertEquals(BuildPropertiesSupport.PropertyKind.STRUCTURE, prop.getKind());
        
        assertFalse(support.keys(prop).isEmpty());
        assertFalse(support.items(prop, null).iterator().hasNext());

        prop = support.findTaskProperty("nativeTestCompile", "workingDirectory");
        assertNotNull(prop);
        assertEquals(BuildPropertiesSupport.PropertyKind.STRUCTURE, prop.getKind());
        String s = prop.getStringValue();
        assertNotNull("Paths and directories are convertible to String", s);
        assertTrue(s.endsWith(Paths.get("micronaut", "build", "native", "nativeTestCompile").toString()));

        assertTrue(support.keys(prop).isEmpty());
        assertFalse(support.items(prop, null).iterator().hasNext());
    }
    
    /**
     * Checks that a list property of a task is populated
     * @throws Exception 
     */
    public void testTaskListProperty() throws Exception {
        Project p = makeProject("buildprops/micronaut");
        BuildPropertiesSupport support = BuildPropertiesSupport.get(p);
        assertNotNull(support);
        
        BuildPropertiesSupport.Property prop = support.findTaskProperty("nativeCompile", "options.runtimeArgs");
        assertNotNull(prop);
        assertEquals(PropertyKind.LIST, prop.getKind());
        
        assertTrue(support.keys(prop).isEmpty());
        assertTrue(support.items(prop, null).iterator().hasNext());
    }
    
}
