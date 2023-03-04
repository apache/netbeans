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
package org.netbeans.modules.refactoring.java.test;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.*;
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
import org.junit.Assert;

import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.lookup.*;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbPerformanceTest.PerformanceData;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Utilities methods.
 *
 * @author Pavel Flaska
 */
public class Utilities {

    /**
     * Prevent creation.
     */
    private Utilities() {
    }

    /**
     * Unzip the file <code>f</code> to folder <code>destDir</code>.
     *
     * @param f         file to unzip
     * @param destDir   destination directory
     */
    public static void unzip(File f, String destDir) {
        final int BUFFER = 2048;
        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = new FileInputStream(f);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    File dir = new File(destDir + '/' + entry.getName());
                    dir.mkdir();
                } else {
                    int count;
                    byte contents[] = new byte[BUFFER];
                    // write the files to the disk
                    FileOutputStream fos = new FileOutputStream(destDir + "/" + entry.getName());
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
     * Open project <code>projectName</code> located in <code>dir</code>
     * directory.
     *
     * @param projectName           project name to open
     * @param dir                   project's enclosing directory
     * @return file-object          representing project
     * @throws java.io.IOException  when project cannot be opened
     */
    public static FileObject openProject(String projectName, File dir) throws IOException {
        File projectsDir = FileUtil.normalizeFile(dir);
        FileObject projectsDirFO = FileUtil.toFileObject(projectsDir);
        FileObject projdir = projectsDirFO.getFileObject(projectName);
        Project p = ProjectManager.getDefault().findProject(projdir);
        OpenProjects.getDefault().open(new Project[]{p}, false);
        if (p == null) {
            throw new IOException("Project is not opened " + projectName);
        }
        return projdir;
    }

    public static ClassPath createEmptyPath() {
        return ClassPathSupport.createClassPath(Collections.<PathResourceImplementation>emptyList());
    }

    public static ClassPath createSourcePath(FileObject projectDir)
            throws IOException
    {
        final FileObject sourceRoot = projectDir.getFileObject("src");
        File root = FileUtil.toFile(sourceRoot);
        if (!root.exists()) {
            root.mkdirs();
        }
        return ClassPathSupport.createClassPath(new URL[]{root.toURI().toURL()});
    }

    public static String jEditProjectOpen() {

/* Temporary solution - download jEdit from internal location */

        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;
        int BUFFER = 2048;

        try {
            URL url = new URL("http://spbweb.russia.sun.com/~ok153203/jEdit41.zip");
            System.err.println("");
            File dir = new File(System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            out = new BufferedOutputStream(new FileOutputStream(dir.getAbsolutePath() + File.separator + "jEdit41.zip"));
            conn = url.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
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

        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = new FileInputStream(new File(System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + "jEdit41.zip"));
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(System.getProperty("nbjunit.workdir") + File.separator + ".." + File.separator + "data" + File.separator + entry.getName()).mkdirs();
                    continue;
                }
                int count;
                byte data[] = new byte[BUFFER];
                FileOutputStream fos = new FileOutputStream(System.getProperty("nbjunit.workdir") + File.separator + ".." + File.separator + "data" + File.separator + entry.getName());
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
            zis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + "jEdit41.zip";
    }

    /**
     * Copy file f1 to f2
     * @param f1 file 1
     * @param f2 file 2
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static void copyFile(java.io.File f1, java.io.File f2) throws java.io.FileNotFoundException, java.io.IOException{
        int data;
        java.io.InputStream fis = new java.io.BufferedInputStream(new java.io.FileInputStream(f1));
        java.io.OutputStream fos = new java.io.BufferedOutputStream(new java.io.FileOutputStream(f2));

        while((data=fis.read())!=-1){
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
        System.out.println("#####  Results for "+name+"   #####");
        System.out.print("#####        [");
        for(int i=1;i<=repeat;i++) {
            System.out.print(results[i]+"ms, ");
        }
        System.out.println("]");
        for (int i=1;i<=name.length()+27;i++) {
            System.out.print("#");
        }
        System.out.println();
        System.out.println();

        path=System.getProperty("nbjunit.workdir");
        File resGlobal=new File(path+File.separator+"allPerformance.xml");

        try {
            dbf=DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
         } catch (Exception ex) {
            ex.printStackTrace (  ) ;
        }

        if (!resGlobal.exists()) {
            try {
                resGlobal.createNewFile();
                out = new PrintStream(new FileOutputStream(resGlobal));
                out.print("<TestResults>\n");
                out.print("</TestResults>");
                out.close();
            } catch (IOException ex) {
            ex.printStackTrace (  ) ;
            }
         }

        try {
              allPerfDoc = db.parse(resGlobal);
            } catch (Exception ex) {
            ex.printStackTrace (  ) ;
            }

        testResultsTag = allPerfDoc.getDocumentElement();

        testTag=null;
        for (int i=0;i<allPerfDoc.getElementsByTagName("Test").getLength();i++) {
            if (("name=\""+name+"\"").equalsIgnoreCase( allPerfDoc.getElementsByTagName("Test").item(i).getAttributes().getNamedItem("name").toString() ) ) {
                testTag =(Element)allPerfDoc.getElementsByTagName("Test").item(i);
                break;
            }
        }

        if (testTag!=null) {
            for (int i=1;i<=repeat;i++) {
                perfDataTag=allPerfDoc.createElement("PerformanceData");
                if (i==1) {
                    perfDataTag.setAttribute("runOrder", "1");
                }
                    else {
                    perfDataTag.setAttribute("runOrder", "2");
                }
                perfDataTag.setAttribute("value", new Long(results[i]).toString());
                testTag.appendChild(perfDataTag);
            }
        }
        else {
            testTag=allPerfDoc.createElement("Test");
            testTag.setAttribute("name", name);
            testTag.setAttribute("unit", unit);
            testTag.setAttribute("results", pass);
            testTag.setAttribute("threshold", new Long(threshold).toString());
            testTag.setAttribute("classname", classname);
            for (int i=1;i<=repeat;i++) {
                perfDataTag=allPerfDoc.createElement("PerformanceData");
                if (i==1) {
                    perfDataTag.setAttribute("runOrder", "1");
                }
                    else {
                    perfDataTag.setAttribute("runOrder", "2");
                }
                perfDataTag.setAttribute("value", new Long(results[i]).toString());
                testTag.appendChild(perfDataTag);
            }
        }

            testSuiteTag=null;
            for (int i=0;i<allPerfDoc.getElementsByTagName("Suite").getLength();i++) {
                if (suite.equalsIgnoreCase(allPerfDoc.getElementsByTagName("Suite").item(i).getAttributes().getNamedItem("suitename").getNodeValue())) {
                    testSuiteTag =(Element)allPerfDoc.getElementsByTagName("Suite").item(i);
                    break;
                }
            }

            if (testSuiteTag==null) {
                testSuiteTag=allPerfDoc.createElement("Suite");
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

        Transformer tr=null;
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
        long[] result=new long[2];
        result[1]=pd.value;
        xmlTestResults(System.getProperty("nbjunit.workdir"), "Unit Tests Suite", pd.name, className, className, pd.unit, "passed", 120000 , result, 1);
    }
    
    public static class TestLkp extends ProxyLookup {

        private static TestLkp DEFAULT;

        public TestLkp() {
            Assert.assertNull(DEFAULT);
            DEFAULT = this;
            ClassLoader l = TestLkp.class.getClassLoader();
            this.setLookups(
                    new Lookup[] {
                        Lookups.metaInfServices(l),
                        Lookups.singleton(l)
                    }
            );
        }

        public static void setLookupsWrapper(Lookup... l) {
            DEFAULT.setLookups(l);
        }
    }

    public static interface ParameterSetter {
        void setParameters();
    }
    
    public static class MyHandler extends Handler {

        private Map<String, Long> map = new HashMap<String, Long>();

        @Override
        public void publish(LogRecord record) {
            Long data;
            if (record == null) {
                return;
            }
            for (Object o : record.getParameters()) {
                if (o instanceof Long) {
                    data = (Long) o;
                    map.put(record.getMessage(), data);
                }
            }
        }

        public Long get(String key) {
            return map.get(key);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
