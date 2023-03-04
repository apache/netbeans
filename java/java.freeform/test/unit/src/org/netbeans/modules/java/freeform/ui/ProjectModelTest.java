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

package org.netbeans.modules.java.freeform.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.freeform.JavaProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;

/**
 * Tests for ProjectModel.
 *
 * @author David Konecny
 */
public class ProjectModelTest extends NbTestCase {
    
    public ProjectModelTest(String testName) {
        super(testName);
    }
    
    public void testCreateEmptyModel() throws Exception {
        File baseFolder = new File(getWorkDir(), "somefolder/");
        File nbProjectFolder = new File(getWorkDir(), "nbprojfolder/");
        Map<String,String> p = new HashMap<String,String>();
        p.put("key", "value");
        PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(null, PropertyUtils.fixedPropertyProvider(p));
        ProjectModel pm = ProjectModel.createEmptyModel(baseFolder, nbProjectFolder, evaluator);
        assertNotNull(pm);
        assertEquals("Base folder incorrect", baseFolder, pm.getBaseFolder());
        assertEquals("NB project folder incorrect", nbProjectFolder, pm.getNBProjectFolder());
        assertEquals("Evaluator incorrect", evaluator, pm.getEvaluator());
        assertEquals("Evaluator is different", 1, pm.getEvaluator().getProperties().size());
    }

    // tests: addSourceFolder, removeSourceFolder, setSourceLevel, getCompilationUnit
    public void testBasicFunctionality() throws Exception {
        ProjectModel pm = createEmptyProjectModel();
        pm.setSourceLevel("custom_source_level");
        JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
        sf.location = "loc1";
        sf.label = "label_loc1";
        sf.type = "java";
        pm.addSourceFolder(sf, false);
        assertEquals("Number of source folders does not match", 1, pm.getSourceFoldersCount());
        assertEquals("Number of comp units does not match", 1, pm.getJavaCompilationUnits().size());
        assertEquals("Number of comp unit keys does not match", 1, pm.createCompilationUnitKeys().size());
        List<ProjectModel.CompilationUnitKey> keys = generateKeys(new Object[]{"loc1"}, new String[]{"label_loc1"});
        assertKeyEquals(keys, pm.createCompilationUnitKeys());
        JavaProjectGenerator.JavaCompilationUnit cu1 = pm.getJavaCompilationUnits().get(0);
        JavaProjectGenerator.JavaCompilationUnit cu2 = pm.getCompilationUnit(keys.get(0), false);
        assertEquals("Must be the same instance", cu1, cu2);

        sf = new JavaProjectGenerator.SourceFolder();
        sf.location = "loc2";
        sf.label = "label_loc2";
        sf.type = "java";
        pm.addSourceFolder(sf, false);
        assertEquals("Number of source folders does not match", 2, pm.getSourceFoldersCount());
        assertEquals("Number of comp units does not match", 2, pm.getJavaCompilationUnits().size());
        assertEquals("Number of comp unit keys does not match", 2, pm.createCompilationUnitKeys().size());
        keys = generateKeys(new Object[]{"loc1", "loc2"}, new String[]{"label_loc1", "label_loc2"});
        assertKeyEquals(keys, pm.createCompilationUnitKeys());
        cu1 = pm.getJavaCompilationUnits().get(0);
        cu2 = pm.getCompilationUnit(keys.get(0), false);
        assertEquals("Must be the same instance", cu1, cu2);
        cu1 = pm.getJavaCompilationUnits().get(1);
        cu2 = pm.getCompilationUnit(keys.get(1), false);
        assertEquals("Must be the same instance", cu1, cu2);
        
        assertEquals("Source level does not match", "custom_source_level", 
            pm.getJavaCompilationUnits().get(0).sourceLevel);
        assertEquals("Source level does not match", "custom_source_level", 
            pm.getJavaCompilationUnits().get(1).sourceLevel);
        pm.setSourceLevel("jdk15");
        assertEquals("Source level does not match", "jdk15", 
            pm.getJavaCompilationUnits().get(0).sourceLevel);
        assertEquals("Source level does not match", "jdk15", 
            pm.getJavaCompilationUnits().get(1).sourceLevel);
        
        pm.removeSourceFolder(0);
        assertEquals("Number of source folders does not match", 1, pm.getSourceFoldersCount());
        assertEquals("Number of comp units does not match", 1, pm.getJavaCompilationUnits().size());
        assertEquals("Number of comp unit keys does not match", 1, pm.createCompilationUnitKeys().size());
        keys = generateKeys(new Object[]{"loc2"}, new String[]{"label_loc2"});
        assertKeyEquals(keys, pm.createCompilationUnitKeys());
    }

    public void testAdvancedAddSourceFolder() throws Exception {
        ProjectModel pm = createEmptyProjectModel();
        pm.setSourceLevel("jdk1x");
        JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
        sf.location = "loc1";
        sf.label = "label_loc1";
        sf.type = "java";
        pm.addSourceFolder(sf, false);
        assertEquals("Number of source folders does not match", 1, pm.getSourceFoldersCount());
        assertEquals("Number of comp units does not match", 1, pm.getJavaCompilationUnits().size());
        assertEquals("Number of comp unit keys does not match", 1, pm.createCompilationUnitKeys().size());
        List keys = generateKeys(new Object[]{"loc1"}, new String[]{"label_loc1"});
        assertKeyEquals(keys, pm.createCompilationUnitKeys());

        sf = new JavaProjectGenerator.SourceFolder();
        sf.location = "loc2";
        sf.label = "label_loc2";
        sf.type = "java";
        pm.addSourceFolder(sf, false);
        assertEquals("Number of source folders does not match", 2, pm.getSourceFoldersCount());
        assertEquals("Number of comp units does not match", 2, pm.getJavaCompilationUnits().size());
        assertEquals("Number of comp unit keys does not match", 2, pm.createCompilationUnitKeys().size());
        keys = generateKeys(new Object[]{"loc1", "loc2"}, new String[]{"label_loc1", "label_loc2"});
        assertKeyEquals(keys, pm.createCompilationUnitKeys());

        sf = new JavaProjectGenerator.SourceFolder();
        sf.location = "locWEB";
        sf.label = "label_locWEB";
        sf.type = "web";
        pm.addSourceFolder(sf, false);
        assertEquals("Number of source folders does not match", 3, pm.getSourceFoldersCount());
        assertEquals("Number of comp units does not match", 2, pm.getJavaCompilationUnits().size());
        assertEquals("Number of comp unit keys does not match", 2, pm.createCompilationUnitKeys().size());
        keys = generateKeys(new Object[]{"loc1", "loc2"}, new String[]{"label_loc1", "label_loc2"});
        assertKeyEquals(keys, pm.createCompilationUnitKeys());

        pm.removeSourceFolder(2);
        assertEquals("Number of source folders does not match", 2, pm.getSourceFoldersCount());
        assertEquals("Number of comp units does not match", 2, pm.getJavaCompilationUnits().size());
        assertEquals("Number of comp unit keys does not match", 2, pm.createCompilationUnitKeys().size());
        keys = generateKeys(new Object[]{"loc1", "loc2"}, new String[]{"label_loc1", "label_loc2"});
        assertKeyEquals(keys, pm.createCompilationUnitKeys());
        
        pm.updateCompilationUnits(false);
        assertEquals("Number of source folders does not match", 2, pm.getSourceFoldersCount());
        assertEquals("Number of comp units does not match", 1, pm.getJavaCompilationUnits().size());
        assertEquals("Number of comp unit keys does not match", 1, pm.createCompilationUnitKeys().size());
        keys = generateKeys(new Object[]{Arrays.asList("loc1", "loc2")}, new String[]{null});
        assertKeyEquals(keys, pm.createCompilationUnitKeys());
        
        sf = new JavaProjectGenerator.SourceFolder();
        sf.location = "loc3";
        sf.label = "label_loc3";
        sf.type = "java";
        pm.addSourceFolder(sf, false);
        assertEquals("Number of source folders does not match", 3, pm.getSourceFoldersCount());
        assertEquals("Number of comp units does not match", 1, pm.getJavaCompilationUnits().size());
        assertEquals("Number of comp unit keys does not match", 1, pm.createCompilationUnitKeys().size());
        keys = generateKeys(new Object[]{Arrays.asList("loc1", "loc2", "loc3")}, new String[]{null});
        assertKeyEquals(keys, pm.createCompilationUnitKeys());
        
        sf = new JavaProjectGenerator.SourceFolder();
        sf.location = "locWEB";
        sf.label = "label_locWEB";
        sf.type = "web";
        pm.addSourceFolder(sf, false);
        assertEquals("Number of source folders does not match", 4, pm.getSourceFoldersCount());
        assertEquals("Number of comp units does not match", 1, pm.getJavaCompilationUnits().size());
        assertEquals("Number of comp unit keys does not match", 1, pm.createCompilationUnitKeys().size());
        keys = generateKeys(new Object[]{Arrays.asList("loc1", "loc2", "loc3")}, new String[]{null});
        assertKeyEquals(keys, pm.createCompilationUnitKeys());
        
        pm.removeSourceFolder(0);
        pm.removeSourceFolder(0);
        assertEquals("Number of source folders does not match", 2, pm.getSourceFoldersCount());
        assertEquals("Number of comp units does not match", 1, pm.getJavaCompilationUnits().size());
        assertEquals("Number of comp unit keys does not match", 1, pm.createCompilationUnitKeys().size());
        keys = generateKeys(new Object[]{"loc3"}, new String[]{"label_loc3"});
        assertKeyEquals(keys, pm.createCompilationUnitKeys());
    }

    public void testCreateCompilationUnitKeys() throws Exception {
        List<JavaProjectGenerator.SourceFolder> sources;
        List<JavaProjectGenerator.JavaCompilationUnit> units;
        List<ProjectModel.CompilationUnitKey> keys;
        List<ProjectModel.CompilationUnitKey> createdKeys;
        
        ProjectModel pm = createEmptyProjectModel();
        
        // case: some source folders; no comp unit
        // expected result: one key for each source folder
        sources = generateSources(new String[]{"src1", "src2", "src3"});
        units = new ArrayList<JavaProjectGenerator.JavaCompilationUnit>();
        keys = generateKeys(new Object[]{"src1", "src2", "src3"}, new String[]{"src1", "src2", "src3"});
        pm.setSourceFolders(sources);
        pm.setJavaCompilationUnits(units);
        createdKeys = pm.createCompilationUnitKeys();
        assertKeyEquals(keys, createdKeys);
        
        // case: one source folder; one comp unit for the source
        // expected result: one key
        sources = generateSources(new String[]{"src1"});
        units = generateUnits(new Object[]{"src1"});
        keys = generateKeys(new Object[]{"src1"}, new String[]{"src1"});
        pm.setSourceFolders(sources);
        pm.setJavaCompilationUnits(units);
        createdKeys = pm.createCompilationUnitKeys();
        assertKeyEquals(keys, createdKeys);
        
        // case: two source folders; two comp unit for the sources
        // expected result: two key
        sources = generateSources(new String[]{"src1", "src2"});
        units = generateUnits(new Object[]{"src1", "src2"});
        keys = generateKeys(new Object[]{"src1", "src2"}, new String[]{"src1", "src2"});
        pm.setSourceFolders(sources);
        pm.setJavaCompilationUnits(units);
        createdKeys = pm.createCompilationUnitKeys();
        assertKeyEquals(keys, createdKeys);
        
        // case: two source folders; one comp unit for both sources
        // expected result: one key with null as location
        sources = generateSources(new String[]{"src1", "src2"});
        units = generateUnits(new Object[]{Arrays.asList("src1", "src2")});
        keys = generateKeys(new Object[]{Arrays.asList("src1", "src2")}, new String[]{null});
        pm.setSourceFolders(sources);
        pm.setJavaCompilationUnits(units);
        createdKeys = pm.createCompilationUnitKeys();
        assertKeyEquals(keys, createdKeys);
        
        // case: mixed source folders; mixed comp units
        sources = generateSources(new String[]{"src1", "src2"});
        units = generateUnits(new Object[]{"src3", "src4"});
        // XXX: impl dependency: the result will first contain comp units and then source folders:
        keys = generateKeys(new Object[]{"src3", "src4", "src1", "src2"}, new String[]{null, null, "src1", "src2"});
        pm.setSourceFolders(sources);
        pm.setJavaCompilationUnits(units);
        createdKeys = pm.createCompilationUnitKeys();
        assertKeyEquals(keys, createdKeys);
        
        sources = generateSources(new String[]{"src1", "src2"});
        units = generateUnits(new Object[]{Arrays.asList("src2", "src3")});
        // XXX: impl dependency: the result will first contain comp units and then source folders:
        keys = generateKeys(new Object[]{Arrays.asList("src2", "src3"), "src1"}, new String[]{null, "src1"});
        pm.setSourceFolders(sources);
        pm.setJavaCompilationUnits(units);
        createdKeys = pm.createCompilationUnitKeys();
        assertKeyEquals(keys, createdKeys);
    }
    
    public void testUpdateCompilationUnits() throws Exception {
        List sources;
        List units = new ArrayList();
        List expectedUnits;
        
        ProjectModel pm = createEmptyProjectModel();
        
        sources = generateSources(new String[]{"src1", "src2"});
        JavaProjectGenerator.JavaCompilationUnit cu = new JavaProjectGenerator.JavaCompilationUnit();
        cu.packageRoots = Collections.singletonList("src1");
        JavaProjectGenerator.JavaCompilationUnit.CP cp = new JavaProjectGenerator.JavaCompilationUnit.CP();
        cp.classpath = "cp1"+File.pathSeparatorChar+"cp2";
        cp.mode = "compile";
        cu.classpath = Collections.singletonList(cp);
        cu.output = Arrays.asList(new String[]{"out1", "out2"});
        units.add(cu);
        cu = new JavaProjectGenerator.JavaCompilationUnit();
        cu.packageRoots = Collections.singletonList("src2");
        cp = new JavaProjectGenerator.JavaCompilationUnit.CP();
        cp.classpath = "cp2"+File.pathSeparatorChar+"cp3";
        cp.mode = "compile";
        cu.classpath = Collections.singletonList(cp);
        cu.output = Arrays.asList(new String[]{"out3"});
        units.add(cu);
        pm.setSourceFolders(sources);
        pm.setJavaCompilationUnits(units);
        pm.setSourceLevel("S_L_14");
        pm.updateCompilationUnits(false);
        assertEquals("Compilation units has to be merged into one", 1, units.size());
        cu = (JavaProjectGenerator.JavaCompilationUnit)units.get(0);
        assertEquals("Compilation unit has to have two package roots", 2, cu.packageRoots.size());
        assertTrue("Missing expected package root: src1", cu.packageRoots.contains("src1"));
        assertTrue("Missing expected package root: src2", cu.packageRoots.contains("src2"));
        assertEquals("Compilation unit has to have three classpath items", 
            "cp1"+File.pathSeparatorChar+"cp2"+File.pathSeparatorChar+"cp3", 
            cu.classpath.get(0).classpath);
        assertEquals("Compilation unit has to have three output items", 3, cu.output.size());
        assertTrue("Missing expected package root: out1", cu.output.contains("out1"));
        assertTrue("Missing expected package root: out2", cu.output.contains("out2"));
        assertTrue("Missing expected package root: out2", cu.output.contains("out2"));
        assertTrue("Missing expected source level: S_L_14", cu.sourceLevel.equals("S_L_14"));
        
        pm.setSourceFolders(sources);
        pm.setJavaCompilationUnits(units);
        pm.setSourceLevel("S_L_15");
        pm.updateCompilationUnits(true);
        assertEquals("Compilation units has to be cloned into two", 2, units.size());
        cu = (JavaProjectGenerator.JavaCompilationUnit)units.get(0);
        assertEquals("Compilation unit has to have one package root", 1, cu.packageRoots.size());
        assertTrue("Missing expected package root", cu.packageRoots.contains("src1"));
        assertEquals("Compilation unit has to have three classpath items", 
            "cp1"+File.pathSeparatorChar+"cp2"+File.pathSeparatorChar+"cp3", 
            cu.classpath.get(0).classpath);
        assertEquals("Compilation unit has to have three output items", 3, cu.output.size());
        assertTrue("Missing expected package root: out1", cu.output.contains("out1"));
        assertTrue("Missing expected package root: out2", cu.output.contains("out2"));
        assertTrue("Missing expected package root: out2", cu.output.contains("out2"));
        assertTrue("Missing expected source level: S_L_14", cu.sourceLevel.equals("S_L_15"));
        cu = (JavaProjectGenerator.JavaCompilationUnit)units.get(1);
        assertEquals("Compilation unit has to have one package root", 1, cu.packageRoots.size());
        assertTrue("Missing expected package root", cu.packageRoots.contains("src2"));
        assertEquals("Compilation unit has to have three classpath items", 
            "cp1"+File.pathSeparatorChar+"cp2"+File.pathSeparatorChar+"cp3", 
            cu.classpath.get(0).classpath);
        assertEquals("Compilation unit has to have three output items", 3, cu.output.size());
        assertTrue("Missing expected package root: out1", cu.output.contains("out1"));
        assertTrue("Missing expected package root: out2", cu.output.contains("out2"));
        assertTrue("Missing expected package root: out2", cu.output.contains("out2"));
        assertTrue("Missing expected source level: S_L_14", cu.sourceLevel.equals("S_L_15"));
        
    }
    
    public void testUpdatePrincipalSourceFolders() throws Exception {
        ProjectModel pm = createEmptyProjectModel();
        List<JavaProjectGenerator.SourceFolder> l = pm.getSourceFolders();
        // base folder and proj folder are different
        List<JavaProjectGenerator.SourceFolder> l2 = pm.updatePrincipalSourceFolders(l, true);
        assertEquals("Principal source for base directory must be added", 1, l2.size());
        l2 = pm.updatePrincipalSourceFolders(l, false);
        assertEquals("There are no external java source folders", 0, l2.size());
        
        pm = createEmptyProjectModel();
        JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
        sf.location = "..\\something";
        sf.label = "something";
        sf.type = "java";
        l = pm.getSourceFolders();
        pm.addSourceFolder(sf, false);
        l2 = pm.updatePrincipalSourceFolders(l, false);
        assertEquals("One principal source must be added", 2, l2.size());
        
        pm = createEmptyProjectModel();
        JavaProjectGenerator.SourceFolder sf2 = new JavaProjectGenerator.SourceFolder();
        sf2.location = "..\\something2";
        sf2.label = "something2";
        sf2.type = "java";
        pm.addSourceFolder(sf, false);
        pm.addSourceFolder(sf2, false);
        pm.removeSourceFolder(0);
        pm.removeSourceFolder(0);
        pm.addSourceFolder(sf, false);
        pm.addSourceFolder(sf2, false);
        pm.removeSourceFolder(0);
        pm.addSourceFolder(sf, false);
        l = pm.getSourceFolders();
        l2 = pm.updatePrincipalSourceFolders(l, false);
        assertEquals("Two principal sources must be added", 4, l2.size());
        JavaProjectGenerator.SourceFolder addedSF = l2.get(2);
        assertEquals("Added principal source must have the same label", addedSF.label, sf.label);
        assertEquals("Added principal source must have the same location", addedSF.location, sf.location);
        assertNull("Added principal source must have type==null", addedSF.type);
        addedSF = l2.get(3);
        assertEquals("Added principal source must have the same label", addedSF.label, sf2.label);
        assertEquals("Added principal source must have the same location", addedSF.location, sf2.location);
        assertNull("Added principal source must have type==null", addedSF.type);
        pm.removeSourceFolder(0);
        l = pm.getSourceFolders();
        l2 = pm.updatePrincipalSourceFolders(l, false);
        assertEquals("One principal source must be removed", 2, l2.size());

        JavaProjectGenerator.SourceFolder sf2_ = new JavaProjectGenerator.SourceFolder();
        sf2_.location = "..\\something2";
        sf2_.label = "something2";
        sf2_.type = null;
        pm = createEmptyProjectModel();
        pm.addSourceFolder(sf2, false);
        pm.addSourceFolder(sf2_, false);
        l = pm.getSourceFolders();
        l2 = pm.updatePrincipalSourceFolders(l, false);
        assertEquals("No principal sources added in this case because it already exist", l.size(), l2.size());
    }
    
    private List<JavaProjectGenerator.SourceFolder> generateSources(String[] locations) {
        List<JavaProjectGenerator.SourceFolder> l = new ArrayList<JavaProjectGenerator.SourceFolder>(locations.length);
        for (String loc : locations) {
            JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
            sf.location = loc;
            sf.label = loc;
            sf.type = "java";
            l.add(sf);
        }
        return l;
    }

    /**
     * @param locations can be either String or List instance
     */
    private List generateUnits(Object[] locations) {
        List l = new ArrayList(locations.length);
        for (int i=0; i<locations.length; i++) {
            JavaProjectGenerator.JavaCompilationUnit cu = new JavaProjectGenerator.JavaCompilationUnit();
            if (locations[i] instanceof List) { // XXX use Union2
                cu.packageRoots = (List)locations[i];
            } else {
                cu.packageRoots = Collections.singletonList((String) locations[i]);
            }
            l.add(cu);
        }
        return l;
    }
    
    /**
     * @param locations can be either String or List instance
     */
    private List<ProjectModel.CompilationUnitKey> generateKeys(Object[] locations, String[] labels) {
        List<ProjectModel.CompilationUnitKey> l = new ArrayList<ProjectModel.CompilationUnitKey>(locations.length);
        for (int i=0; i<locations.length; i++) {
            ProjectModel.CompilationUnitKey key = new ProjectModel.CompilationUnitKey();
            if (locations[i] instanceof List) {
                key.locations = (List)locations[i];
            } else {
                key.locations = Collections.singletonList((String) locations[i]);
            }
            key.label = labels[i];
            l.add(key);
        }
        return l;
    }
    
    private void assertKeyEquals(List<ProjectModel.CompilationUnitKey> l1, List<ProjectModel.CompilationUnitKey> l2) throws Exception {
        String param = "Keys do not match: Expected: "+l1+" Result:"+l2; // NOI18N
        assertEquals(param,  l1, l2);
        Iterator<ProjectModel.CompilationUnitKey> i1 = l1.iterator();
        Iterator<ProjectModel.CompilationUnitKey> i2 = l2.iterator();
        while (i1.hasNext()) {
            ProjectModel.CompilationUnitKey k1 = i1.next();
            ProjectModel.CompilationUnitKey k2 = i2.next();
            assertEquals(param, k1.label, k2.label);
        }
    }

    private ProjectModel createEmptyProjectModel() throws Exception {
        File baseFolder = new File(getWorkDir(), "somefolder/");
        File nbProjectFolder = new File(getWorkDir(), "nbprojfolder/");
        Map<String,String> p = new HashMap<String,String>();
        p.put("key", "value");
        PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(null, PropertyUtils.fixedPropertyProvider(p));
        return ProjectModel.createEmptyModel(baseFolder, nbProjectFolder, evaluator);
    }
    
}
