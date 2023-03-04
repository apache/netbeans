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
package org.netbeans.test.ide;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class InstallationCompletenessTest extends NbTestCase {

    public static final String[] NON_PRODUCTION_MODULES = {"org.netbeans.modules.timers"};
    public static final String DISTRO_PROPERTY = "netbeans.distribution";
    public static final String JAVACARD_PREFIX = "org.netbeans.modules.javacard";
    public static final String EXPECTED_INCLUDES_PROPERTY = "expected.includes";
    public static final String EXPECTED_EXCLUDES_PROPERTY = "expected.excludes";

    private Set<String> expectedIncludes = new HashSet<String>();
    private Set<String> expectedExcludes = new HashSet<String>();
    
    public enum Type {

        JAVASE("base,javase,websvccommon,extide"),
        JAVAEE("base,javase,websvccommon,javaee,webcommon,extide"),
        PHP("base,php,websvccommon,webcommon,extide"),
        CPP("base,cpp"),
        FULL("base,javase,javaee,php,cpp,webcommon,websvccommon,full,extide");
        private String parts;

        private Type(String parts) {
            this.parts = parts;
        }

        public String getParts() {
            return parts;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public InstallationCompletenessTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); //To change body of generated methods, choose Tools | Templates.
    }

    public static Test suite() throws IOException {
        // disable 'slowness detection'
        System.setProperty("org.netbeans.core.TimeableEventQueue.quantum", "100000");
        NbTestSuite s = new NbTestSuite();
        s.addTest(
                NbModuleSuite.createConfiguration(
                InstallationCompletenessTest.class).gui(false).clusters(".*").enableModules(".*").
                honorAutoloadEager(true).
                addTest("testInstalledKits")
                .suite());
        return s;
    }

    public void testInstalledKits() throws Exception {
        String distro = System.getProperty(DISTRO_PROPERTY);
        assertNotNull("Distribution not set, please set by setting property:" + DISTRO_PROPERTY, distro);
        Set<String> kitsGolden = getModulesForDistro(distro, new File(getDataDir(), "kits.properties"));
        if(readExpectedParams()){
            kitsGolden.addAll(expectedIncludes);
            kitsGolden.removeAll(expectedExcludes);
            listExpectedParams();
        }
        Set<String> redundantKits = new HashSet<String>();
        UpdateManager um = UpdateManager.getDefault();
        List<UpdateUnit> l = um.getUpdateUnits(UpdateManager.TYPE.KIT_MODULE);
        for (UpdateUnit updateUnit : l) {
            String kitName = updateUnit.getCodeName();
            if (kitsGolden.contains(kitName)) {
                kitsGolden.remove(kitName);
                System.out.println("OK - IDE contains:" + updateUnit.getCodeName());
            } else {
                redundantKits.add(kitName);
                System.out.println("REDUNDANT - IDE contains:" + kitName);
            }
        }
        for (String missing : kitsGolden) {
            System.out.println("MISSING - IDE does not contain:" + missing);
        }
        assertTrue("Some modules are missing:\n" + setToString(kitsGolden), kitsGolden.isEmpty());
        assertTrue("Some modules are redundant:\n" + setToString(redundantKits), redundantKits.isEmpty());
    }

    private String setToString(Set<String> set){
        String resStr = "";
        for (String s : set) {
            resStr = resStr.concat(s + "\n");
        }
        return resStr;
    }
    
    private boolean readExpectedParams(){
        String excludes = System.getProperty(EXPECTED_EXCLUDES_PROPERTY);
        String includes = System.getProperty(EXPECTED_INCLUDES_PROPERTY);
        if(excludes != null) expectedExcludes.addAll(Arrays.asList(excludes.split(":")));
        if(includes != null) expectedIncludes.addAll(Arrays.asList(includes.split(":")));
        return !expectedExcludes.isEmpty() || !expectedIncludes.isEmpty();
    }
    
    private void listExpectedParams(){
        System.out.println("Expected includes:");
        for (String in : expectedIncludes) {
            System.out.println(in);
        }
        System.out.println();
        System.out.println("Expected excludes:");
        for (String ex : expectedExcludes) {
            System.out.println(ex);
        }
        System.out.println();
    }
    
    private  Set<String> getModulesForDistro(String distro, File f) {
        Set<String> result = new HashSet<String>();
        Type distroT = Type.valueOf(distro.toUpperCase());
        Properties p = readProps(f);
        assertNotNull("Distro golden files are correctly read", p);
        for (String kit : p.stringPropertyNames()) {
            if (distroT.getParts().contains(p.getProperty(kit))) {
                result.add(kit);
            }
        }
        return filterProductionSpecific(filterPlatformSpecific(result));
    }

    private Set<String> filterPlatformSpecific(Set<String> set){
        if (!org.openide.util.Utilities.isWindows()){
            //filter javacard modules on non-win platforms
            Set<String> toRemove = new HashSet<String>();
            for (String kit : set) {
                if(kit.startsWith(JAVACARD_PREFIX)) {
                    toRemove.add(kit);
                }
            }
            set.removeAll(toRemove);
        }
        return set;
    }
    
    private Set<String> filterProductionSpecific(Set<String> set){
        if (!isDevBuild()){
            // remove o.n.m.timers for Beta, RCs and FCS builds
            set.removeAll(Arrays.asList(NON_PRODUCTION_MODULES));
        } 
        return set;
    }
    
    private boolean isDevBuild(){
        return NbBundle.getBundle("org.netbeans.core.startup.Bundle").
                getString("currentVersion").contains("Dev");
    }

    private Properties readProps(File f) {
        Properties props = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            props.load(fis);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                }
            }
        }
        return props;
    }
}