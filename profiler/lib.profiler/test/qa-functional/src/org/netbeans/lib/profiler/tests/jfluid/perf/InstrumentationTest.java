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

/*
 * InstrumentationTest.java
 * JUnit based test
 *
 * Created on November 7, 2006, 2:14 PM
 */
package org.netbeans.lib.profiler.tests.jfluid.perf;

import junit.framework.*;
import org.netbeans.junit.NbPerformanceTest;
import org.netbeans.lib.profiler.ProfilerEngineSettings;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
import org.netbeans.lib.profiler.instrumentation.Instrumentor;
import org.netbeans.lib.profiler.tests.jfluid.CommonProfilerTestCase;
import org.netbeans.lib.profiler.wireprotocol.InstrumentMethodGroupResponse;
import org.netbeans.lib.profiler.wireprotocol.RootClassLoadedCommand;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import junit.textui.TestRunner;
import org.netbeans.junit.NbModuleSuite;


/**
 *
 * @author ehucka
 */
public class InstrumentationTest extends CommonProfilerTestCase implements NbPerformanceTest {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    ArrayList<NbPerformanceTest.PerformanceData> data = new ArrayList<>();
    ProfilerEngineSettings settings;
    String[] classNames;
    byte[][] classesBytes;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public InstrumentationTest(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(InstrumentationTest.class).addTest(
            "testJ2SE",
            "testJaxb",
            "testJaxbNoGettersEmpties",
            "testSimple",
            "testSimpleNoEmpties",
            "testSimpleNoGetters").enableModules(".*").clusters(".*").gui(false));
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public NbPerformanceTest.PerformanceData[] getPerformanceData() {
        return data.toArray(new NbPerformanceTest.PerformanceData[0]);
    }

    public void reportPerformance(String name, long value, String unit) {
        NbPerformanceTest.PerformanceData d = new NbPerformanceTest.PerformanceData();
        d.name = name;
        d.value = value;
        d.unit = unit;
        d.threshold = NbPerformanceTest.PerformanceData.NO_THRESHOLD;
        data.add(d);
    }

    public void testJ2SE() {
        try {
            String jarPath = "/perfdata/j2se-simple.jar";
            File f = new File(getDataDir(), jarPath);
            assertTrue("Instrumented jar file doesn't exist.", f.exists());
            initTest(f.getAbsolutePath());
            startInstrumentationTest(f.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    public void testJaxb() {
        try {
            String jarPath = "/perfdata/jaxb-xjc.jar";
            File f = new File(getDataDir(), jarPath);
            assertTrue("Instrumented jar file doesn't exist.", f.exists());
            initTest(f.getAbsolutePath());
            startInstrumentationTest(f.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    public void testJaxbNoGettersEmpties() {
        try {
            String jarPath = "/perfdata/jaxb-xjc.jar";
            File f = new File(getDataDir(), jarPath);
            assertTrue("Instrumented jar file doesn't exist.", f.exists());
            initTest(f.getAbsolutePath());
            settings.setInstrumentGetterSetterMethods(false);
            settings.setInstrumentEmptyMethods(false);
            startInstrumentationTest(f.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    public void testSimple() {
        try {
            String jarPath = "/perfdata/oneclass.jar";
            File f = new File(getDataDir(), jarPath);
            assertTrue("Instrumented jar file doesn't exist.", f.exists());
            initTest(f.getAbsolutePath());
            startInstrumentationTest(f.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    public void testSimpleNoEmpties() {
        try {
            String jarPath = "/perfdata/oneclass.jar";
            File f = new File(getDataDir(), jarPath);
            assertTrue("Instrumented jar file doesn't exist.", f.exists());
            initTest(f.getAbsolutePath());
            settings.setInstrumentEmptyMethods(false);
            startInstrumentationTest(f.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    public void testSimpleNoGetters() {
        try {
            String jarPath = "/perfdata/oneclass.jar";
            File f = new File(getDataDir(), jarPath);
            assertTrue("Instrumented jar file doesn't exist.", f.exists());
            initTest(f.getAbsolutePath());
            settings.setInstrumentGetterSetterMethods(false);
            startInstrumentationTest(f.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    protected void setClasses(String jarPath) throws Exception {
        ArrayList<String> names = new ArrayList<>(16);
        ArrayList<byte[]> bytes = new ArrayList<>(16);
        JarFile file = new JarFile(jarPath);
        Enumeration<JarEntry> entries = file.entries();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        int read = 0;
        byte[] buffer = new byte[1024];

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            if (entry.getName().endsWith(".class")) {
                String nm = entry.getName();
                nm = nm.substring(0, nm.lastIndexOf('.'));
                names.add(nm);

                BufferedInputStream bis = new BufferedInputStream(file.getInputStream(entry));

                while ((read = bis.read(buffer)) > -1) {
                    bos.write(buffer, 0, read);
                }

                bis.close();
                bytes.add(bos.toByteArray());
                bos.reset();
            }
        }

        classNames = names.toArray(new String[0]);
        classesBytes = bytes.toArray(new byte[0][]);
    }

    protected void setRootMethods(String jarFile) throws Exception {
        JarFile file = new JarFile(jarFile);
        HashSet<String> list = new HashSet<>(8);

        for (Enumeration<JarEntry> entries = file.entries(); entries.hasMoreElements();) {
            JarEntry entry = entries.nextElement();

            if (entry.getName().endsWith(".class")) {
                String name = entry.getName();
                int idx = name.lastIndexOf('/');
                String packageName = (idx == -1) ? name : name.substring(0, idx);
                packageName = packageName.replace('/', '.');
                list.add(packageName);
            }
        }

        ClientUtils.SourceCodeSelection[] ret = new ClientUtils.SourceCodeSelection[list.size()];
        String[] cls = list.toArray(new String[0]);

        for (int i = 0; i < list.size(); i++) {
            ret[i] = new ClientUtils.SourceCodeSelection(cls[i] + ".", "", ""); //NOI18N
        }

        settings.setInstrumentationRootMethods(ret);
    }

    protected boolean checkBytes(String className, byte[] bytes) {
        String clnm = className.replace('.', '/');
        int clindex = -1;

        for (int i = 0; i < classNames.length; i++) {
            if (classNames[i].equals(clnm)) {
                clindex = i;

                break;
            }
        }

        if (clindex == -1) {
            throw new IllegalStateException("Class " + className + " has not original.");
        }

        byte[] origbytes = classesBytes[clindex];

        if (origbytes.length != bytes.length) {
            return false;
        }

        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != origbytes[i]) {
                return false;
            }
        }

        return true;
    }

    protected ProfilerEngineSettings initTest(String pathToJar)
                                       throws Exception {
        settings = new ProfilerEngineSettings();
        settings.setPortNo(5140);
        settings.setSeparateConsole(false);
        settings.setCPUProfilingType(CommonConstants.CPU_INSTR_FULL);
        settings.setInstrScheme(CommonConstants.INSTRSCHEME_TOTAL);
        settings.setInstrumentEmptyMethods(true);
        settings.setInstrumentGetterSetterMethods(true);
        settings.setInstrumentMethodInvoke(true);
        settings.setInstrumentSpawnedThreads(true);
        settings.setJVMArgs("");

        setRootMethods(pathToJar);

        setTargetVM(settings);
        //setClassPath(settings);
        setProfilerHome(settings);

        setStatus(STATUS_NONE);

        return settings;
    }

    protected void startInstrumentationTest(String jarFile)
                                     throws Exception {
        ProfilingSessionStatus status = new ProfilingSessionStatus();
        status.targetJDKVersionString = settings.getTargetJDKVersionString();

        PrintStream oldOutStream = System.out;
        PrintStream oldErrStream = System.err;
        System.setOut(getLogStream());
        System.setErr(getLogStream());

        Instrumentor instr = new Instrumentor(status, settings);
        instr.setStatusInfoFromSourceCodeSelection(settings.getInstrumentationRootMethods());
        status.currentInstrType = CommonConstants.INSTR_RECURSIVE_FULL;

        setClasses(jarFile);

        int[] loadersIDs = new int[classNames.length];

        for (int i = 0; i < classNames.length; i++) {
            loadersIDs[i] = 20;
        }

        int[] parentloadersIDs = new int[classNames.length];

        for (int i = 0; i < classNames.length; i++) {
            parentloadersIDs[i] = 0;
        }

        int[] superClasses = new int[classNames.length];
        int[][] ifaces = new int[classNames.length][];
        
        RootClassLoadedCommand cmd = new RootClassLoadedCommand(classNames, loadersIDs, classesBytes, superClasses, ifaces,
                                                                classNames.length, parentloadersIDs);
        log("Start instrumenting ...");

        InstrumentMethodGroupResponse resp = null;
        long time = System.currentTimeMillis();
        resp = instr.createInitialInstrumentMethodGroupResponse(cmd);
        time = System.currentTimeMillis() - time;
        ref("Number of Classes: " + classNames.length);

        byte[][] clbytes = resp.getReplacementClassFileBytes();
        ref("Instrumented Classes: " + resp.getBase().getNClasses());
        ref("Instrumented Methods: " + resp.getBase().getNMethods());

        if (resp.getErrorMessage() != null) {
            log("Error Message: " + resp.getErrorMessage());
        }

        String[] clnames = resp.getMethodClasses();
        byte[][] bts = resp.getReplacementClassFileBytes();
        boolean comp = false;

        for (int i = 0; i < clnames.length; i++) {
            if (checkBytes(clnames[i], bts[i])) {
                log("Equals bytes: " + clnames[i]);
            }
        }

        System.setOut(oldOutStream);
        System.setErr(oldErrStream);
        reportPerformance(getName(), time, "ms");
    }
}
