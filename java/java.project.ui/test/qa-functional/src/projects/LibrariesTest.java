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
 * TestDefaultLibraries.java
 * NetBeans JUnit based test
 *
 * Created on 06 September 2004, 15:37
 */
package projects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import org.netbeans.jellytools.JellyTestCase;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
//import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.libraries.Library;

/**
 *
 */
public class LibrariesTest extends JellyTestCase {

    Map<String, Set<String>> librariesUrls;

    public LibrariesTest(java.lang.String testName) {
        super(testName);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(LibrariesTest.class).
                addTest("testDefaultLibrariesIgnoreOrder").
                enableModules(".*").clusters(".*"));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        librariesUrls = new HashMap<String, Set<String>>();

    }
    // -------------------------------------------------------------------------
    /**
     * This test fails only for golden files that differ from current state
     * in content (ignored are differnces related to lines order).
     * 
     */
    public void testDefaultLibrariesIgnoreOrder() throws IOException {
        init1();
        LibraryManager libMan = LibraryManager.getDefault();
        Library[] libs = libMan.getLibraries();
        String errorString = "";

        PrintWriter initMethodBuff = new PrintWriter(new FileWriter(new File(getWorkDir(), "testDefaultLibrariesIgnoreOrder.txt")));
        try {
            for (int i = 0; i < libs.length; i++) {

                // names of files are based on library name
                String baseName = libs[i].getName();

                Set<String> urls = new TreeSet<String>();
                addURLs(urls, libs[i].getContent("classpath"));
                addURLs(urls, libs[i].getContent("javadoc"));
                addURLs(urls, libs[i].getContent("src"));
                generateInitDefaultLibrariesIngoreOrder(initMethodBuff, urls, baseName);
                assertEquals("Following files differ other way than lines order:", librariesUrls.get(baseName), urls);

            }
        } finally {
            initMethodBuff.close();
        }
    }

    public void init1() {
        Set<String> set = new TreeSet<String>();
        set.add("jar:nbinst://org.netbeans.modules.websvc.jaxrpc16/modules/ext/jaxrpc16/FastInfoset.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.websvc.jaxrpc16/modules/ext/jaxrpc16/activation.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.websvc.jaxrpc16/modules/ext/jaxrpc16/jax-qname.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.websvc.jaxrpc16/modules/ext/jaxrpc16/jaxp-api.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.websvc.jaxrpc16/modules/ext/jaxrpc16/jaxrpc-api.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.websvc.jaxrpc16/modules/ext/jaxrpc16/jaxrpc-impl.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.websvc.jaxrpc16/modules/ext/jaxrpc16/jaxrpc-spi.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.websvc.jaxrpc16/modules/ext/jaxrpc16/jsr173_api.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.websvc.jaxrpc16/modules/ext/jaxrpc16/mail.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.websvc.jaxrpc16/modules/ext/jaxrpc16/relaxngDatatype.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.websvc.jaxrpc16/modules/ext/jaxrpc16/saaj-api.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.websvc.jaxrpc16/modules/ext/jaxrpc16/saaj-impl.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.websvc.jaxrpc16/modules/ext/jaxrpc16/xsdlib.jar!/");
        librariesUrls.put("jaxrpc16", set);
        set = new TreeSet<String>();
        set.add("jar:nbinst://org.netbeans.modules.form/modules/ext/AbsoluteLayout.jar!/");
        librariesUrls.put("absolutelayout", set);
        set = new TreeSet<String>();
        set.add("jar:nbinst://org.netbeans.modules.xml.jaxb.api/modules/ext/jaxb/api/jaxb-api.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.xml.jaxb.api/modules/ext/jaxb/activation.jar!/");
        set.add("jar:nbinst://org.netbeans.libs.jaxb/modules/ext/jaxb/jaxb-impl.jar!/");
        set.add("jar:nbinst://org.netbeans.libs.jaxb/modules/ext/jaxb/jaxb-xjc.jar!/");
        set.add("jar:nbinst:///modules/ext/jaxws22/api/jakarta.jws-api.jar!/");
        set.add("jar:nbinst:///modules/ext/jaxws22/api/jakarta.xml.soap-api.jar!/");
        set.add("jar:nbinst:///modules/ext/jaxws22/api/jakarta.xml.ws-api.jar!/");
        set.add("jar:nbinst:///modules/ext/jaxws22/FastInfoset.jar!/");
        set.add("jar:nbinst:///modules/ext/jaxws22/jaxws-rt.jar!/");
        set.add("jar:nbinst:///modules/ext/jaxws22/jaxws-tools.jar!/");
        set.add("jar:nbinst:///modules/ext/jaxws22/saaj-impl.jar!/");
        set.add("jar:nbinst:///modules/ext/jaxws22/stax-ex.jar!/");
        set.add("jar:nbinst:///modules/ext/jaxws22/streambuffer.jar!/");
        librariesUrls.put("jaxws20", set);
        set = new TreeSet<String>();
        set.add("jar:nbinst:///docs/org-netbeans-libs-javacapi.zip!/");
        set.add("jar:nbinst:///modules/ext/javac-api-nb-7.0-b07.jar!/");
        librariesUrls.put("javac-api", set);
        set = new TreeSet<String>();
        set.add("jar:nbinst:///docs/javaee-doc-api.jar!/");
        set.add("jar:nbinst://org.netbeans.libs.jstl/modules/ext/jstl-api.jar!/");
        set.add("jar:nbinst://org.netbeans.libs.jstl/modules/ext/jstl-impl.jar!/");
        librariesUrls.put("jstl", set);
        set = new TreeSet<String>();
        set.add("jar:nbinst://org.netbeans.modules.xml.jaxb.api/modules/ext/jaxb/api/jaxb-api.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.xml.jaxb.api/modules/ext/jaxb/activation.jar!/");
        set.add("jar:nbinst://org.netbeans.libs.jaxb/modules/ext/jaxb/jaxb-impl.jar!/");
        set.add("jar:nbinst://org.netbeans.libs.jaxb/modules/ext/jaxb/jaxb-xjc.jar!/");
        librariesUrls.put("jaxb20", set);
        set = new TreeSet<String>();
        set.add("jar:nbinst://org.netbeans.modules.swingapp/docs/appframework-0.30-doc.zip!/");
        set.add("jar:nbinst://org.netbeans.modules.swingapp/modules/ext/appframework-0.30.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.swingapp/modules/ext/swing-worker.jar!/");
        librariesUrls.put("swing-app-framework", set);
        set = new TreeSet<String>();
        set.add("jar:nbinst://org.netbeans.modules.junit/docs/junit-4.13.2-javadoc.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.junit/modules/ext/junit-4.13.2.jar!/");
        librariesUrls.put("junit", set);
        set = new TreeSet<String>();
        set.add("jar:nbinst://org.netbeans.modules.java.j2seproject/ant/extra/org-netbeans-modules-java-j2seproject-copylibstask.jar!/");
        librariesUrls.put("CopyLibs", set);
        set = new TreeSet<String>();
        set.add("jar:nbinst:///modules/ext/beansbinding-0.5.jar!/");
        librariesUrls.put("beans-binding", set);
        set = new TreeSet<String>();
        set.add("jar:nbinst:///docs/javaee-doc-api.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.web.jsf/modules/ext/jsf/commons-beanutils.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.web.jsf/modules/ext/jsf/commons-collections.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.web.jsf/modules/ext/jsf/commons-digester.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.web.jsf/modules/ext/jsf/commons-logging.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.web.jsf/modules/ext/jsf/jsf-api.jar!/");
        set.add("jar:nbinst://org.netbeans.modules.web.jsf/modules/ext/jsf/jsf-impl.jar!/");
        librariesUrls.put("jsf", set);

    }

    private void generateInitDefaultLibrariesIngoreOrder(PrintWriter buff, Set<String> urls, String baseName) {
        buff.println("set = new TreeSet<String>();");
        for (String url : urls) {
            buff.println("set.add(\"" + url + "\");");
        }
        buff.println("librariesUrls.put(\"" + baseName + "\", set);");
    }

    private void addURLs(Set<String> urls, Iterable<URL> sourceUrls) {
        for (URL url : sourceUrls) {
            urls.add(url.toExternalForm());
        }
    }

    /**
     * This test fails if golden files differ from current state even if there are
     * only differnces in lines order.
     */
//    public void testDefaultLibraries() {
//        
//        LibraryManager libMan = LibraryManager.getDefault();
//        Library [] libs = libMan.getLibraries();
//        
//        for (int i = 0; i < libs.length; i++) {
//            System.out.println("**********" + libs[i].getName() + "**********");
//            File refFile = null;
//            PrintStream ps = null;
//            // names of files are based on library name
//            String baseName = libs[i].getName();
//            String refFileName = baseName + "_ref.txt";
//            String goldenFileName = baseName + "_pass.txt";
//            String diffFileName = baseName + "_diff.txt";
//            
//            // write library data to ref file
//            try {
//                ps = new PrintStream(new java.io.FileOutputStream(new File(getWorkDir(), refFileName)));
//            } catch (Exception exc) {
//                fail(exc.getMessage());
//            }
//            
//            List listOfClasspaths = libs[i].getContent("classpath");
//            dumpList("", listOfClasspaths, ps);
//            List listOfJavadocs = libs[i].getContent("javadoc");
//            dumpList("", listOfJavadocs, ps);
//            List listOfSrcs = libs[i].getContent("src");
//            dumpList("", listOfSrcs, ps);
//            ps.close();
//            
//            try {
//                compareReferenceFiles(refFileName, goldenFileName, diffFileName);
//            } catch (Exception exc) {
//                fail(exc.getMessage());
//            }
//        }
//        
//    }
    public void __testCreateLibrary() {

        // learn hostname
        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhe) {
            fail("Cannot get hostname: " + uhe.getMessage()); // NOI18N
        }
        hostName = hostName.replace('-', '_');

        // load platforms.properties file
        InputStream is = this.getClass().getResourceAsStream("libraries.properties");
        Properties props = new Properties();
        try {
            props.load(is);
        } catch (java.io.IOException ioe) {
            fail("Cannot load platforms properties: " + ioe.getMessage()); // NOI18N
        }

        String[] libCp = getTokensAsArray(props.getProperty(hostName + "library1_cp"));
        String[] libSrc = getTokensAsArray(props.getProperty(hostName + "library1_src"));
        String[] libJdoc = getTokensAsArray(props.getProperty(hostName + "library1_jdoc"));

        TestProjectUtils.addLibrary(props.getProperty(hostName + "library1_name"),
                libCp, libSrc, libJdoc);
    }

    public void __testListDefaultLibraries() {
        listDefaultLibs("e:\\work\\libs\\");
    }
    // -------------------------------------------------------------------------
    /* This method is intended only for generation of golden files from Beta2 release build
     */
    private void listDefaultLibs(String folder) {

        PrintStream pw = null;
        LibraryManager libMan = LibraryManager.getDefault();
        Library[] libs = libMan.getLibraries();

        for (int i = 0; i < libs.length; i++) {
            try {
                pw = new PrintStream(new FileOutputStream(folder + libs[i].getName() + ".txt"));
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }

            System.out.println("Display name: " + libs[i].getDisplayName());
            System.out.println("Name: " + libs[i].getName());
            List listOfClasspaths = libs[i].getContent("classpath");
            dumpList("Classpath: ", listOfClasspaths, System.out);
            dumpList("", listOfClasspaths, pw);
            List listOfJavadocs = libs[i].getContent("javadoc");
            dumpList("Javadoc: ", listOfJavadocs, System.out);
            dumpList("", listOfJavadocs, pw);
            List listOfSrcs = libs[i].getContent("src");
            dumpList("Sources: ", listOfSrcs, System.out);
            dumpList("", listOfSrcs, pw);
            pw.close();

        }

    }

    public String[] getGoldenFileLines(String goldenFileName) {
        File goldenFile = null;
        BufferedReader reader = null;
        try {
            goldenFile = getGoldenFile(goldenFileName);
            reader = new BufferedReader(new FileReader(goldenFile));
        } catch (Exception e) {
            fail("Unable to open following golden file '" + goldenFile.toString() + "'"); // NOI18N
        }
        List<String> linesList = new ArrayList<String>();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (line.trim() != "") {
                    linesList.add(line);
                }
            }
            reader.close();
        } catch (Exception e) {
        }
        String[] lines = linesList.toArray(new String[0]);
        Arrays.sort(lines);
        return lines;
    }

    private void dumpList(String prefix, List list, PrintStream ps) {
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            ps.println(prefix + iter.next());
        }
    }

    private String[] getTokensAsArray(String str) {
        StringTokenizer st = new StringTokenizer(str, ",");
        String[] array = new String[st.countTokens()];
        int index = 0;
        while (st.hasMoreTokens()) {
            array[index++] = st.nextToken().trim();
        }
        return array;
    }
}
