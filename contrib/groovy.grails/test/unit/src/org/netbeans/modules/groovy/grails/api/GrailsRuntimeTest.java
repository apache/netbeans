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

package org.netbeans.modules.groovy.grails.api;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.groovy.grails.settings.GrailsSettings;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hejl
 */
public class GrailsRuntimeTest extends NbTestCase {

    public GrailsRuntimeTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        super.tearDown();
    }

    public void testConfigured() throws IOException {
        final GrailsSettings settings = GrailsSettings.getInstance();
        final GrailsPlatform runtime = GrailsPlatform.getDefault();

        String path = getWorkDirPath();
        FileObject workDir = FileUtil.createFolder(FileUtil.normalizeFile(getWorkDir()));

        settings.setGrailsBase(path);
        assertFalse(runtime.isConfigured());

        FileObject dir = workDir.createFolder("bin");
        assertFalse(runtime.isConfigured());
        dir.createData(Utilities.isWindows() ? "grails.bat" : "grails");
        assertTrue(runtime.isConfigured());
    }

    public void testCommandDescriptor() throws IOException {
        Project project = new TestProject("test", FileUtil.toFileObject(
                FileUtil.normalizeFile(getWorkDir())));
        GrailsProjectConfig config = GrailsProjectConfig.forProject(project);

        GrailsPlatform.CommandDescriptor desc = GrailsPlatform.CommandDescriptor.forProject(
                "test", getWorkDir(), config, new String[]{}, null);

        assertEquals("test", desc.getName());
        assertEquals(getWorkDir(), desc.getDirectory());
        assertEquals(config, desc.getProjectConfig());
        assertEquals(new String[] {}, desc.getArguments());
        assertEquals(new Properties(), desc.getProps());

        String[] args = new String[] {"arg1", "arg2"};
        desc = GrailsPlatform.CommandDescriptor.forProject(
                "test", getWorkDir(), config, args, null);

        assertEquals("test", desc.getName());
        assertEquals(getWorkDir(), desc.getDirectory());
        assertEquals(config, desc.getProjectConfig());
        assertEquals(args, desc.getArguments());
        assertEquals(new Properties(), desc.getProps());

        Properties props = new Properties();
        props.setProperty("prop1", "value1");
        props.setProperty("prop2", "value2");

        desc = GrailsPlatform.CommandDescriptor.forProject(
                "test", getWorkDir(), config, args, props);

        assertEquals("test", desc.getName());
        assertEquals(getWorkDir(), desc.getDirectory());
        assertEquals(config, desc.getProjectConfig());
        assertEquals(args, desc.getArguments());
        assertEquals(props, desc.getProps());

        // test immutability
        desc.getArguments()[0] = "wrong";
        assertEquals(args, desc.getArguments());
        desc.getProps().setProperty("wrong", "wrong");
        assertEquals(props, desc.getProps());

        String[] correctArgs = args.clone();
        args[0] = "wrong";
        assertEquals(correctArgs, desc.getArguments());
        Properties correctProps = new Properties(props);
        props.setProperty("wrong", "wrong");
        assertEquals(correctProps, desc.getProps());
    }

    public void testVersion() {
        GrailsPlatform.Version version1 = new GrailsPlatform.Version(1, null, null, null, null);
        GrailsPlatform.Version version2 = new GrailsPlatform.Version(1, 0, 0, null, null);
        assertEquals(version1, version2);
        assertEquals(version1.hashCode(), version2.hashCode());
        assertEquals("1", version1.toString());
        assertEquals("1.0.0", version2.toString());

        version2 = new GrailsPlatform.Version(1, null, 0, null, "a");
        assertFalse(version1.equals(version2));
        assertEquals("1.0.0-a", version2.toString());

        version2 = new GrailsPlatform.Version(0, null, 0, null, "a");
        assertFalse(version1.equals(version2));
        assertEquals("0.0.0-a", version2.toString());

        version2 = new GrailsPlatform.Version(1, null, null, null, null);
        assertEquals(new GrailsPlatform.Version(1, 0, 0, 0, null), version2);
        assertEquals(GrailsPlatform.Version.VERSION_DEFAULT, version2);
        assertEquals("1", version2.toString());
    }

    public void testVersionParsing() {
        GrailsPlatform.Version version = GrailsPlatform.Version.valueOf("1.0");
        assertEquals(new GrailsPlatform.Version(1, 0, 0, 0, null), version);
        assertEquals("1.0", version.toString());

        version = GrailsPlatform.Version.valueOf("1.1");
        assertEquals(GrailsPlatform.Version.VERSION_1_1, version);
        assertEquals("1.1", version.toString());

        version = GrailsPlatform.Version.valueOf("2.0-a");
        assertEquals(new GrailsPlatform.Version(2, 0, 0, 0, "a"), version);
        assertEquals("2.0-a", version.toString());
    }

    public void testVersionCompare() {
        GrailsPlatform.Version version1 = GrailsPlatform.Version.valueOf("1.0");
        GrailsPlatform.Version version2 = GrailsPlatform.Version.valueOf("1.0");
        assertEquals(0, version1.compareTo(version2));

        version2 = GrailsPlatform.Version.valueOf("1.0-a");
        assertTrue(version1.compareTo(version2) < 0);
        assertTrue(version2.compareTo(version1) > 0);

        version2 = GrailsPlatform.Version.valueOf("2.0");
        assertTrue(version1.compareTo(version2) < 0);
        assertTrue(version2.compareTo(version1) > 0);
    }

    private static void assertEquals(String[] expected, String[] value) {
        assertEquals(expected.length, value.length);

        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], value[i]);
        }
    }

    private static void assertEquals(Properties expected, Properties value) {
        Set<String> valueNames = new HashSet<String>();
        for (Enumeration e = value.propertyNames(); e.hasMoreElements();) {
            valueNames.add(e.nextElement().toString());
        }

        for (Enumeration e = expected.propertyNames(); e.hasMoreElements();) {
            String propName = e.nextElement().toString();
            String propValue = expected.getProperty(propName);
            assertTrue(valueNames.remove(propName));
            assertEquals(expected.getProperty(propName), propValue);
        }

        assertTrue(valueNames.isEmpty());
    }
}
