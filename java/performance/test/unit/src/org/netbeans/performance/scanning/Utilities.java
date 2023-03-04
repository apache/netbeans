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
package org.netbeans.performance.scanning;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.zip.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.openide.filesystems.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbPerformanceTest.PerformanceData;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utilities methods.
 *
 * @author Pavel Flaska
 */
public class Utilities {

    public static final Map<String, String> PROJECTS = new HashMap<>(4);

    static {
        PROJECTS.put("jEdit", "http://hg.netbeans.org/binaries/BBD005CDF8785223376257BD3E211C7C51A821E7-jEdit41.zip");
        PROJECTS.put("mediawiki-1.14.0", "https://netbeans.org/projects/performance/downloads/download/Mediawiki-1_FitnessViaSamples.14.0-nbproject.zip");
        PROJECTS.put("tomcat6", "http://hg.netbeans.org/binaries/70CE8459CA39C3A49A2722C449117CE5DCFBA56A-tomcat6.zip");
        PROJECTS.put("FrankioskiProject", "http://jupiter.uk.oracle.com/wiki/pub/NbQE/TestingProjects/BigWebProject.zip");
    }

    /**
     * Prevent creation.
     */
    private Utilities() {
    }

    /**
     * Unzip the file <code>f</code> to folder <code>destDir</code>.
     *
     * @param f file to unzip
     * @param destDir destination directory
     */
    public static void unzip(File f, File destDir) {
        final int BUFFER = 2048;
        try {
            BufferedOutputStream dest;
            FileInputStream fis = new FileInputStream(f);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    File dir = new File(destDir, entry.getName());
                    dir.mkdir();
                } else {
                    int count;
                    byte contents[] = new byte[BUFFER];
                    // write the files to the disk
                    FileOutputStream fos = new FileOutputStream(new File(destDir, entry.getName()));
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = zis.read(contents, 0, BUFFER)) != -1) {
                        dest.write(contents, 0, count);
                    }
                    dest.flush();
                    dest.close();
                }
            }
            zis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Open projects with given names located in given folder.
     *
     * @param projectNames project names to open
     * @param dir project's enclosing directory
     * @throws java.io.IOException when project cannot be opened
     */
    public static void openProjects(File dir, String... projectNames) throws IOException {
        List<Project> projects = new ArrayList<>(projectNames.length);
        for (String projectName : projectNames) {
            File projectsDir = FileUtil.normalizeFile(dir);
            FileObject projectsDirFO = FileUtil.toFileObject(projectsDir);
            FileObject projdir = projectsDirFO.getFileObject(projectName);
            FileObject nbproject = projdir.getFileObject("nbproject");
            if (nbproject.getFileObject("private") != null) {
                for (FileObject ch : nbproject.getFileObject("private").getChildren()) {
                    ch.delete();
                }
            }
            Project p = ProjectManager.getDefault().findProject(projdir);
            if (p == null) {
                throw new IOException("Project is not found " + projectName);
            }
            projects.add(p);
        }
        OpenProjects.getDefault().open(projects.toArray(new Project[0]), false);
    }

    public static void projectDownload(String projectUrl, File projectZip) throws Exception {

        /* Temporary solution - download jEdit from internal location */
        OutputStream out = null;
        URLConnection conn;
        InputStream in = null;

        try {
            URL url = new URL(projectUrl);
            if (!projectZip.getParentFile().exists()) {
                projectZip.getParentFile().mkdirs();
            }
            out = new BufferedOutputStream(new FileOutputStream(projectZip));
            conn = url.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * Downloads project by its name from pre-defined location and unzips it to
     * given workdir.
     *
     * @param projectName project name
     * @param workdir folder to unzip project
     * @throws java.lang.Exception
     */
    public static void projectDownloadAndUnzip(String projectName, File workdir) throws Exception {
        String projectUrl = Utilities.PROJECTS.get(projectName);
        File projectsDir = new File(System.getProperty("nbjunit.workdir"), "tmpdir");
        File projectZip = new File(projectsDir, projectName + ".zip");
        if (!projectZip.exists() || projectZip.length()<1) {
            projectDownload(projectUrl, projectZip);
        }
        unzip(projectZip, workdir);
    }

    /**
     * Copy file f1 to f2
     *
     * @param f1 file 1
     * @param f2 file 2
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static void copyFile(java.io.File f1, java.io.File f2) throws java.io.FileNotFoundException, java.io.IOException {
        int data;
        java.io.InputStream fis = new java.io.BufferedInputStream(new java.io.FileInputStream(f1));
        java.io.OutputStream fos = new java.io.BufferedOutputStream(new java.io.FileOutputStream(f2));

        while ((data = fis.read()) != -1) {
            fos.write(data);
        }
    }

    public static void xmlTestResults(String path, String suite, String name, String classname, String sname, String unit, String pass, long threshold, long[] results, int repeat) {

        DocumentBuilderFactory dbf = null;
        DocumentBuilder db = null;
        Document allPerfDoc = null;
        Element testResultsTag, testTag, perfDataTag, testSuiteTag = null;

        PrintStream out = System.out;

        System.out.println();
        System.out.println("#####  Results for " + name + "   #####");
        System.out.print("#####        [");
        for (int i = 1; i <= repeat; i++) {
            System.out.print(results[i] + "ms, ");
        }
        System.out.println("]");
        for (int i = 1; i <= name.length() + 27; i++) {
            System.out.print("#");
        }
        System.out.println();
        System.out.println();

        path = System.getProperty("nbjunit.workdir");
        File resGlobal = new File(path + File.separator + "allPerformance.xml");

        try {
            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!resGlobal.exists()) {
            try {
                resGlobal.createNewFile();
                out = new PrintStream(new FileOutputStream(resGlobal));
                out.print("<TestResults>\n");
                out.print("</TestResults>");
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        try {
            allPerfDoc = db.parse(resGlobal);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        testResultsTag = allPerfDoc.getDocumentElement();
        String buildNumber = System.getProperty("org.netbeans.performance.buildnumber");
        if (buildNumber != null) {
            testResultsTag.setAttribute("buildnumber", buildNumber);
        }

        testTag = null;
        for (int i = 0; i < allPerfDoc.getElementsByTagName("Test").getLength(); i++) {
            if (("name=\"" + name + "\"").equalsIgnoreCase(allPerfDoc.getElementsByTagName("Test").item(i).getAttributes().getNamedItem("name").toString())) {
                testTag = (Element) allPerfDoc.getElementsByTagName("Test").item(i);
                break;
            }
        }

        if (testTag != null) {
            for (int i = 1; i <= repeat; i++) {
                perfDataTag = allPerfDoc.createElement("PerformanceData");
                if (i == 1) {
                    perfDataTag.setAttribute("runOrder", "1");
                } else {
                    perfDataTag.setAttribute("runOrder", "2");
                }
                perfDataTag.setAttribute("value", new Long(results[i]).toString());
                testTag.appendChild(perfDataTag);
            }
        } else {
            testTag = allPerfDoc.createElement("Test");
            testTag.setAttribute("name", name);
            testTag.setAttribute("unit", unit);
            testTag.setAttribute("results", pass);
            testTag.setAttribute("threshold", new Long(threshold).toString());
            testTag.setAttribute("classname", classname);
            for (int i = 1; i <= repeat; i++) {
                perfDataTag = allPerfDoc.createElement("PerformanceData");
                if (i == 1) {
                    perfDataTag.setAttribute("runOrder", "1");
                } else {
                    perfDataTag.setAttribute("runOrder", "2");
                }
                perfDataTag.setAttribute("value", new Long(results[i]).toString());
                testTag.appendChild(perfDataTag);
            }
        }

        testSuiteTag = null;
        for (int i = 0; i < allPerfDoc.getElementsByTagName("Suite").getLength(); i++) {
            if (suite.equalsIgnoreCase(allPerfDoc.getElementsByTagName("Suite").item(i).getAttributes().getNamedItem("suitename").getNodeValue())) {
                testSuiteTag = (Element) allPerfDoc.getElementsByTagName("Suite").item(i);
                break;
            }
        }

        if (testSuiteTag == null) {
            testSuiteTag = allPerfDoc.createElement("Suite");
            testSuiteTag.setAttribute("name", sname);
            testSuiteTag.setAttribute("suitename", suite);
            testSuiteTag.appendChild(testTag);
        } else {
            testSuiteTag.appendChild(testTag);
        }

        testResultsTag.appendChild(testSuiteTag);

        try {
            out = new PrintStream(new FileOutputStream(resGlobal));
        } catch (FileNotFoundException ex) {
        }

        Transformer tr = null;
        try {
            tr = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException ex) {
        }

        tr.setOutputProperty(OutputKeys.INDENT, "no");
        tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        DOMSource docSrc = new DOMSource(allPerfDoc);
        StreamResult result = new StreamResult(out);

        try {
            tr.transform(docSrc, result);
        } catch (TransformerException ex) {
        }
        out.close();
    }

    public static void processUnitTestsResults(String className, PerformanceData pd) {
        String suiteClassName = System.getProperty("suitename", className);
        String suiteName = System.getProperty("suite", className);
        processUnitTestsResults(className, suiteName, suiteClassName, pd);
    }

    public static void processUnitTestsResults(String className, String suiteName, String suiteClassName, PerformanceData pd) {
        long[] result = new long[2];
        result[1] = pd.value;
        xmlTestResults(System.getProperty("nbjunit.workdir"), suiteName, pd.name, className, suiteClassName, pd.unit, "passed", pd.threshold, result, 1);
    }

    /**
     * Request RepositoryUpdater to refresh all indexes.
     */
    public static void refreshIndexes() {
        RepositoryUpdater.getDefault().refreshAll(false, false, false, null, new Object[0]);
    }

    /**
     * Sets new cache folder to reset indexed values.
     *
     * @param cacheFolder new cache folder
     */
    public static void setCacheFolder(File cacheFolder) {
        CacheFolder.setCacheFolder(FileUtil.toFileObject(cacheFolder));
    }

    /**
     * Wait until scanning is finished.
     *
     * @param projectDir File pointing to project root or root of several
     * projects
     * @throws java.lang.Exception
     */
    public static void waitScanningFinished(File projectDir) throws Exception {
        JavaSource src = JavaSource.create(ClasspathInfo.create(projectDir));
        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override()
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
            }
        }, false).get();
    }

    static class ReadingHandler extends Handler {

        private boolean read = false;

        @Override
        public void publish(LogRecord record) {
            if ("MSG_CACHED_INPUT_STREAM".equals(record.getMessage())) {
                read = true;
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public boolean wasRead() {
            return read;
        }
    }
}
