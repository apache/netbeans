/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.junit.ide;

import java.io.*;
import java.util.Collection;
import junit.framework.Test;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests in this file check JDK compliance with FX support.
 * 
 * @author Petr Somol
 */
public class JDKSetupTest extends NbTestCase {
    
    /**
     * Tests in this file are meant to test end-user JDK configuration.
     * Note that the required configuration may not be available
     * during NetBeans Hudson build process. To prevent unintended
     * failures of the build process all of the following tests
     * can be disabled here.
     */
    private static final boolean DISABLE_ALL_TESTS = true;
    
    private static String BUILD_SCRIPT_FILE = "JDKAntJSTest.xml";
    private static String JS_MESSAGE = "JavaScript has been successfully called.";
    private static String TEST_RESULT = "TEST RESULT: ";
    
    private static enum OSType {WINDOWS, MAC, UNIX}
    
    private static boolean versionChecked = false;
    private static boolean JDKpre16 = false;
    private static boolean JDKpre7u6 = false;
    private static boolean JDK7u4to5 = false;
    private static OSType  JDKOSType  = OSType.WINDOWS;
    
    public JDKSetupTest(String testName) {
        super(testName);
    }

    /** Set up. */
    protected @Override void setUp() throws IOException {
        this.clearWorkDir();
        System.out.println("\nJDKFXSETUPTEST  "+getName()+"  JDKFXSETUPTEST");
    }

    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.emptyConfiguration()
            .addTest(JDKSetupTest.class,
                "testJDKVersion",
                "testFXSDKinJDK",
                "testAntJavaScriptSupport"
            )
        .enableModules(".*").clusters(".*"));
    }

    /**
     * Tests JDK minimum version necessary for JavaFX 2.0+
     */
    public void testJDKVersion() {
        if(!DISABLE_ALL_TESTS) {
            checkJDKVersion();
            assertTrue("JDK version could not be determined.", versionChecked);
            assertFalse("Detected JDK version lower than 1.6. JavaFX 2.0+ requires JDK version grater or equal to 1.6.", JDKpre16);
        }
    }    
    
    /**
     * Tests the presence of FX SDK components in pre-specified locations
     * inside Mac JDK 7u4 or all-system JDK 7u6+.
     */
    public void testFXSDKinJDK() {
        if(!DISABLE_ALL_TESTS) {
            checkJDKVersion();
            assertTrue("JDK version could not be determined.", versionChecked);
            JavaPlatform platform = JavaPlatform.getDefault();
            Collection<FileObject> roots = platform.getInstallFolders();
            if(!JDKpre16 && JDKOSType == OSType.MAC && JDK7u4to5) {
                // Mac JDK 7u4 has new FX SDK directory structure but misses webstart and browser plugins
                assertTrue(fileExists(roots, "lib/ant-javafx.jar"));
                assertTrue(fileExists(roots, "jre/lib/jfxrt.jar"));
                System.out.println(TEST_RESULT + "JDK directory structure OK (WebStart and browser plugins assumed missing).");
            } else {
                if(!JDKpre16 && !JDKpre7u6) {
                    // JDK 7u6 and above has new FX SDK directory structure
                    FileObject javawsFO = platform.findTool("javaws");
                    assertNotNull(javawsFO);
                    assertTrue(fileExists(roots, "bin/javaws") || fileExists(roots, "bin/javaws.exe"));
                    assertTrue(fileExists(roots, "lib/ant-javafx.jar"));
                    assertTrue(fileExists(roots, "jre/lib/jfxrt.jar"));
                    assertTrue(fileExists(roots, "jre/lib/deploy.jar"));
                    assertTrue(fileExists(roots, "jre/lib/javaws.jar"));
                    assertTrue(fileExists(roots, "jre/lib/plugin.jar"));
                    System.out.println(TEST_RESULT + "JDK directory structure OK.");
                } else {
                    // FX SDK not inside JDK, but JDK should contain WebStart
                    //FileObject javawsFO = platform.findTool("javaws");
                    //assertNotNull(javawsFO);
                    //assertTrue(fileExists(roots, "bin/javaws") || fileExists(roots, "bin/javaws.exe"));
                    System.out.println(TEST_RESULT + "JDK directory structure OK.");
                }
            }
        }
    }
    
    /**
     * Tests whether JavaScript code can be executed from Ant build script.
     * FX Project build scripts rely on JavaScript to pass parameters to
     * <fx:jar> and <fx:deploy> tasks in FX SDK.
     */
    public void testAntJavaScriptSupport() {
        if(!DISABLE_ALL_TESTS) {
            checkJDKVersion();
            assertTrue("JDK version could not be determined.", versionChecked);
            try {
                FileObject buildScript = createAntTestScript();
                assertTrue(buildScript.isData());
                String commandLine = (JDKOSType == OSType.WINDOWS ? "cmd /c " : "")
                        + "ant -buildfile " + buildScript.getPath();
                System.out.println("Executing " + commandLine);
                Process proc = Runtime.getRuntime().exec(commandLine);
                BufferedReader bri = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                BufferedReader bre = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                boolean scriptExecuted = false;
                String line;
                while ((line = bri.readLine()) != null) {
                    System.out.println("Log: " + line);
                    if(line.contains(JS_MESSAGE)) {
                        scriptExecuted = true;
                    }
                }
                bri.close();
                while ((line = bre.readLine()) != null) {
                    System.out.println("Log: " + line);
                    if(line.contains(JS_MESSAGE)) {
                        scriptExecuted = true;
                    }
                }
                bre.close();
                proc.waitFor();
                assertTrue("JavaScript execution from Ant failed.", scriptExecuted);
                System.out.println(TEST_RESULT + "JavaScript is callable from Ant script.");
            }
            catch (Exception err) {
                fail("Exception thrown while creating or executing Ant build script; " + err.getMessage());
            }
        }
    }

    /**
     * Verifies existence of file located relatively as specified by relPath
     * under any of the directories in roots collection
     * 
     * @param roots directories in which to search for relative file location
     * @param relPath relative path to evaluated file
     * @return true if file exists in any of the root directories
     * @throws IllegalArgumentException 
     */
    public static boolean fileExists(Collection<? extends FileObject> roots, String relPath) throws IllegalArgumentException {
        for(FileObject root : roots) {
            FileObject file = root.getFileObject(relPath);
            if(file != null) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks version of default JavaPlatform JDK and sets
     * JDKpre6, JDK7u4to5, JDKpre7u6, JDKOSType and versionChecked
     */
    public void checkJDKVersion() {
        if(!versionChecked) {
            JavaPlatform platform = JavaPlatform.getDefault();
            FileObject javaFO = platform.findTool("java");
            try {
                String commandLine = javaFO.getPath() + " -version";
                System.out.println("Executing " + commandLine);
                Process proc = Runtime.getRuntime().exec(commandLine);
                BufferedReader bri = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                BufferedReader bre = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                String line;
                while ((line = bri.readLine()) != null) {
                    System.out.println("Log: " + line);
                    checkLineForVersion(line);
                }
                bri.close();
                while ((line = bre.readLine()) != null) {
                    System.out.println("Log: " + line);
                    checkLineForVersion(line);
                }
                bre.close();
                String sysName = System.getProperty("os.name");
                JDKOSType = OSType.UNIX;
                if(sysName.toLowerCase().contains("win")) {
                    JDKOSType = OSType.WINDOWS;
                } else {
                    if(sysName.toLowerCase().contains("mac")) {
                        JDKOSType = OSType.MAC;
                    }
                }
                proc.waitFor();
                if(!JDKpre16 && !JDKpre7u6) {
                    System.out.println(TEST_RESULT + "Found JDK 7u6 or later.\nFX SDK assumed integrated into JDK directory structure.");
                } else {
                    if(!JDKpre16 && JDKOSType == OSType.MAC && JDK7u4to5) {
                        System.out.println(TEST_RESULT + "Found Mac JDK 7u4 or 7u5.\nFX SDK assumed integrated into JDK directory structure.\nWebStart and FX browser plugins assumed to be missing.");
                    } else {
                        System.out.println(TEST_RESULT + "Found JDK without integrated FX SDK.");
                    }
                }
                versionChecked = true;
            }
            catch (Exception err) {
                System.err.println("Exception thrown while executing java.exe; " + err.getMessage());
            }
        }
    }

    /**
     * Evaluates line contents to determine java version
     * @param line 
     */
    private void checkLineForVersion(String line) {
        if(line.contains("java version")) {
            if(line.contains("\"1.5") || line.contains("\"1.4") || line.contains("\"1.3")
                    || line.contains("\"1.2") || line.contains("\"1.1") || line.contains("\"1.0")) {
                JDKpre16 = true;
            } else {
                if(line.contains("\"1.7.0_04") || line.contains("\"1.7.0_05")) {
                    JDK7u4to5 = true;
                    JDKpre7u6 = true;
                } else {
                    if(line.contains("\"1.6") || line.contains("\"1.7.0\"") || line.contains("\"1.7.0_01")
                            || line.contains("\"1.7.0_02") || line.contains("\"1.7.0_03")) {
                        JDKpre7u6 = true;
                    }
                }
            }
        }
    }

    /** 
     * Creates Ant build script with a target that calls JavaScript
     * @return FileObject of the created build script file
     */
    public FileObject createAntTestScript() throws IOException {
        String dir = this.getWorkDirPath();
        assertNotNull(dir);
        File dirF = new File(dir);
        if(!dirF.exists()) {
            dirF.mkdirs();
        }
        assertTrue(dirF.exists());
        FileObject dirFO = FileUtil.toFileObject(dirF);
        FileObject buildFile = dirFO.createData(BUILD_SCRIPT_FILE);
        FileLock lock = buildFile.lock();
        try {
            OutputStream os = buildFile.getOutputStream(lock);
            PrintWriter writer = new PrintWriter(os);
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<project name=\"TestJavaScript\" default=\"testjs\" basedir=\".\">");
            writer.println("    <description>");
            writer.println("        Ant build script to test presence of JavaScript engine");
            writer.println("    </description>");
            writer.println("    <target name=\"testjs\">");
            writer.println("        <script language=\"javascript\">");
            writer.println("            <![CDATA[");
            writer.println("                println(\"" + JS_MESSAGE + "\");");
            writer.println("            ]]>");
            writer.println("        </script>");
            writer.println("    </target>");
            writer.println("</project>");
            writer.flush();
            writer.close();
            os.close();
        } finally {
            lock.releaseLock();
        }
        return buildFile;
    }

}
