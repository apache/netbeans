/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package tests;

import java.io.*;
import java.net.URL;
import junit.framework.*;
import org.netbeans.junit.*;

import org.netbeans.modules.schema2beans.*;
import org.netbeans.modules.schema2beansdev.*;
import org.netbeans.modules.schema2beansdev.beangraph.*;
import org.openide.util.Utilities;

public class MainTest extends NbTestCase {

    public MainTest(java.lang.String testName) {
        super(testName);
    }

    public void testPurchaseOrder() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestPurchaseOrder", true, true, true);
    }
    
    public void testInvoice() throws IOException, Schema2BeansException, InterruptedException {
        GenBeans.Config config = new GenBeans.Config();
        config.setOutputType(GenBeans.Config.OUTPUT_JAVABEANS);
        config.setAttributesAsProperties(true);
        config.setGenerateValidate(true);
        config.setProcessComments(true);
        config.setProcessDocType(true);
        BeanGraph bg = new BeanGraph();
        SchemaTypeMappingType stm = new SchemaTypeMappingType();
        stm.setSchemaTypeNamespace("http://www.w3.org/2001/XMLSchema");
        stm.setSchemaTypeName("integer");
        stm.setJavaType("int");
        bg.addSchemaTypeMapping(stm);
        config.addReadBeanGraphs(bg);

        generalTest("TestInvoice", true, config);
    }

    public void testBookXMLSchema() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestBookXMLSchema", true, false, false);
    }

    public void testBook() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestBook");
    }

    public void testDupInternalNames() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestDupInternalNames", true, true, true);
    }

    public void testEvents() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestEvents");
    }

    public void testMerge() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestMerge");
    }

    public void testAttr() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestAttr", false, false, true);
    }

    public void testMdd() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestMdd");
    }

    public void testValid() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestValid");
    }

    public void testFind() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestFind");
    }

    // 228467 - tests.MainTest.testVeto started to fail and needs to be fixed; commenting out for now:
    public void DISABLEtestVeto() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestVeto");
    }

    public void testContrivedApp() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestContrivedApp");
    }

    public void testEncoding() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestEncoding");
    }

    public void testExceptions() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestExceptions");
    }

    public void testEmpty() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestEmpty");
    }

    public void testNamespace() throws IOException, Schema2BeansException, InterruptedException {
        GenBeans.Config config = new GenBeans.Config();
        config.setOutputType(GenBeans.Config.OUTPUT_JAVABEANS);
        config.setAttributesAsProperties(true);
        config.setRemoveUnreferencedNodes(true);
        config.setGenerateValidate(true);
        generalTest("TestNamespace", true, config);
    }

    public void testExtensionSample() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestExtensionSample", true, true, true);
    }

    public void testExtension() throws IOException, Schema2BeansException, InterruptedException {
        GenBeans.Config config = new GenBeans.Config();
        config.setOutputType(GenBeans.Config.OUTPUT_JAVABEANS);
        config.setAttributesAsProperties(true);
        config.setGenerateValidate(true);
        config.setProcessComments(true);
        config.setProcessDocType(true);
        config.setGenerateCommonInterface("CommonBean");
        generalTest("TestExtension", true, config);
    }

    public void testExtension2() throws IOException, Schema2BeansException, InterruptedException {
        GenBeans.Config config = new GenBeans.Config();
        config.buyPremium();
        config.setRespectExtension(true);
        config.setOutputType(GenBeans.Config.OUTPUT_JAVABEANS);
        config.setAttributesAsProperties(true);
        config.setGenerateCommonInterface("CommonBean");
        generalTest("TestExtension2", true, config);
    }

    public void testWebApp() throws IOException, Schema2BeansException, InterruptedException {
        GenBeans.Config config = new GenBeans.Config();
        config.setOutputType(GenBeans.Config.OUTPUT_JAVABEANS);
        config.setAttributesAsProperties(true);
        config.setGenerateCommonInterface("CommonBean");
        config.setRemoveUnreferencedNodes(true);
        generalTest("TestWebApp", true, config);
    }

    public void testWebAppDelegator() throws IOException, Schema2BeansException, InterruptedException {
        GenBeans.Config config = new GenBeans.Config();
        config.buyPremium();
        config.setGenerateDelegator(true);
        config.setGenerateHasChanged(true);
        generalTest("TestWebAppDelegator", true, config);
    }

    public void testWebAppDelegatorBaseBean() throws IOException, Schema2BeansException, InterruptedException {
        GenBeans.Config config = new GenBeans.Config();
        config.buyPremium();
        config.setGenerateDelegator(true);
        config.setOutputType(GenBeans.Config.OUTPUT_TRADITIONAL_BASEBEAN);
        generalTest("TestWebAppDelegatorBaseBean", true, config);
    }

    public void testFinalWebApp() throws IOException, Schema2BeansException, InterruptedException {
        GenBeans.Config config = new GenBeans.Config();
        config.setOutputType(GenBeans.Config.OUTPUT_JAVABEANS);
        config.setAttributesAsProperties(true);
        config.setGenerateCommonInterface("CommonBean");
        config.setGenerateValidate(true);
        config.setProcessComments(true);
        config.setProcessDocType(true);
        config.addReadBeanGraphFiles(new File(getDataDir(), "TestFinalWebAppBeanGraph.xml"));
        generalTest("TestFinalWebApp", true, config);
    }

    public void testBadNames() throws IOException, Schema2BeansException, InterruptedException {
        GenBeans.Config config = new GenBeans.Config();
        config.buyPremium();
        config.setGenerateCommonInterface(null);
        config.setOutputType(GenBeans.Config.OUTPUT_TRADITIONAL_BASEBEAN);
        generalTest("TestBadNames", true, config);
    }

    public void testPositions() throws IOException, Schema2BeansException, InterruptedException {
        GenBeans.Config config = new GenBeans.Config();
        config.buyPremium();
        config.setKeepElementPositions(true);
        generalTest("TestPositions", false, config);
    }

    public void testOr() throws IOException, Schema2BeansException, InterruptedException {
        GenBeans.Config config = new GenBeans.Config();
        config.setOutputType(GenBeans.Config.OUTPUT_JAVABEANS);
        config.setGenerateValidate(true);
        generalTest("TestOr", false, config);
    }

    public void testGroupUnbounded() throws IOException, Schema2BeansException, InterruptedException {
        GenBeans.Config config = new GenBeans.Config();
        config.setOutputType(GenBeans.Config.OUTPUT_JAVABEANS);
        config.setAttributesAsProperties(true);
        config.setGenerateValidate(true);
        generalTest("TestGroupUnbounded", true, config);
    }

    public void testApplication1_4() throws IOException, Schema2BeansException, InterruptedException {
        generalTest("TestApplication1_4", true, true, true);
    }

    public void testInclude() throws IOException, Schema2BeansException, InterruptedException {
        GenBeans.Config config = new GenBeans.Config();
        config.setDocRoot("root");
        generalTest("TestIncludeMain", true, config, false);
    }

    public void testChameleonInclude() throws IOException, Schema2BeansException, InterruptedException {
        GenBeans.Config config = new GenBeans.Config();
        generalTest("TestChameleonIncludeMain", true, config, false);
    }    

    public void testMergeExtendBaseBean() throws IOException, Schema2BeansException, InterruptedException {
        GenBeans.Config config = new GenBeans.Config();
        config.setOutputType(GenBeans.Config.OUTPUT_JAVABEANS);
        config.setAttributesAsProperties(true);
        config.setGenerateValidate(true);
        config.setGenerateCommonInterface("CommonBean");
        config.setExtendBaseBean(true);
        config.setUseInterfaces(true);
        config.setDumpToString(true);
        generalTest("TestMergeExtendBaseBean", false, config);
    }

    public void testBeanWrapper() throws IOException, Schema2BeansException, InterruptedException {
        String testName = "TestBeanWrapper";
        try {
            System.out.println(": Starting "+testName);
            File workDir = getWorkDir();
            System.out.println("workDir="+workDir.toString());
            File schemaFile;
            File beanTreeFile = File.createTempFile("beanTree", "txt");
            InputStream dtdIn;
            InputStream mddIn;

            GenBeans.Config config = new GenBeans.Config();
            config.setOutputType(GenBeans.Config.OUTPUT_JAVABEANS);
            config.setAttributesAsProperties(true);
            config.setGenerateCommonInterface("CommonBean");
            config.setGenerateInterfaces(true);
            config.setGeneratePropertyEvents(true);
            config.setAuto(true);
            config.setStandalone(false);
            schemaFile = new File(getDataDir(), "simple.xsd");
            dtdIn = new FileInputStream(schemaFile);
            config.setFileIn(dtdIn);
            config.setInputURI(schemaFile.toString());
            config.setMddIn(null);
            config.setSchemaType(GenBeans.Config.XML_SCHEMA);
            config.setRootDir(new File(workDir.toString()));
            String simpleBeanGraph = getDataDir() + File.pathSeparator + "simpleBeanGraph.xml";
            config.setWriteBeanGraphFile(new File(simpleBeanGraph));
            ref("Calling GenBeans.doIt");
            GenBeans.doIt(config);
            beanTreeFile.delete();

            config = new GenBeans.Config();
            config.setOutputType(GenBeans.Config.OUTPUT_JAVABEANS);
            config.setAttributesAsProperties(true);
            config.setGenerateCommonInterface("CommonBean");
            config.setGenerateInterfaces(true);
            config.setGeneratePropertyEvents(true);
            config.setAuto(true);
            config.setStandalone(false);
            schemaFile = new File(getDataDir(), "TestNamespace.xsd");
            dtdIn = new FileInputStream(schemaFile);
            config.setFileIn(dtdIn);
            config.setInputURI(schemaFile.toString());
            //mddIn = new FileInputStream(new File(getDataDir(), "TestBeanWrapper.mdd"));
            //config.setMddIn(mddIn);
            config.setSchemaType(GenBeans.Config.XML_SCHEMA);
            config.setRootDir(workDir);
            config.setDumpBeanTree(beanTreeFile);
            config.addReadBeanGraphFiles(new File(simpleBeanGraph));
            ref("Calling GenBeans.doIt");
            GenBeans.doIt(config);

            ref("Bean Tree:");
            ref(beanTreeFile);

            ref("Compiling");
            String cmd = getJdkHome() + "javac -nowarn -classpath "+workDir.toString()+File.pathSeparator+getDataDir().toString()+File.pathSeparator+theClassPath+" "+getDataDir().toString()+"/"+testName+".java";
            int result = runCommandToSystemOut(cmd);
            ref("Finished compiling: "+result);

            cmd = getJdkHome() + "java -classpath "+workDir.toString()+File.pathSeparator+getDataDir().toString()+File.pathSeparator+theClassPath+" "+testName+" "+getDataDir().toString()+"/";
            result = runCommand(cmd);
            ref("Finished running "+testName+": "+result);

            System.out.println("Finished.\n");
        } catch (Exception e) {
            ref(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void generalTest(String testName) throws IOException, Schema2BeansException, InterruptedException {
        generalTest(testName, false, false, false);
    }

    public void generalTest(String testName, boolean xmlSchema,
                            boolean pureJavaBeans, boolean attrProp) throws IOException, Schema2BeansException, InterruptedException {
        GenBeans.Config config = new GenBeans.Config();
        if (pureJavaBeans) {
            config.setOutputType(GenBeans.Config.OUTPUT_JAVABEANS);
        }
        if (attrProp) {
            config.setAttributesAsProperties(true);
        }
        generalTest(testName, xmlSchema, config);
    }

    public void generalTest(String testName, boolean xmlSchema,
                            GenBeans.Config config) throws IOException, Schema2BeansException, InterruptedException {
        generalTest(testName, xmlSchema, config, true);
    }

    public void generalTest(String testName, boolean xmlSchema,
                            GenBeans.Config config, boolean testCompile) throws IOException, Schema2BeansException, InterruptedException {
        String testOnly = System.getProperty("MainTest.testOnly");
        if (testOnly != null && !testOnly.equals(testName))
            return;
        try {
            System.out.println(": Starting "+testName);
            File workDir = getWorkDir();

            //File diffCommandFile = new File(workDir, "diffCommand");
            //Writer cmdOut = new FileWriter(diffCommandFile);
            //cmdOut.write("ediff "+workDir.toString());
            //cmdOut.close();

            config.setAuto(true);
            config.setStandalone(false);
            //config.setTraceParse(true);
            //config.setTraceGen(true);
            //config.setThrowErrors(true);
            System.out.println("workDir="+workDir.toString());
            File schemaFile;
            if (xmlSchema) {
                schemaFile = new File(getDataDir(), testName+".xsd");
                InputStream dtdIn = new FileInputStream(schemaFile);
                config.setFileIn(dtdIn);
                config.setSchemaType(GenBeans.Config.XML_SCHEMA);
            } else {
                schemaFile = new File(getDataDir(), testName+".dtd");
                InputStream dtdIn = new FileInputStream(schemaFile);
                config.setFileIn(dtdIn);
                config.setSchemaType(GenBeans.Config.DTD);
            }
            try {
                InputStream mddIn = new FileInputStream(new File(getDataDir(), testName+".mdd"));
                System.out.println("Found mdd file");
                config.setMddIn(mddIn);
            } catch (FileNotFoundException e) {
                // It's okay if there is no mdd file present
            }
            config.setInputURI(schemaFile.toString());
            config.setRootDir(workDir);
            //config.setPackagePath();
            //config.setMessageOut(getRef());
            //if (testName.equals("TestBookXMLSchema")) {
            //    config.setTraceParse(true);
            //    config.setTraceGen(true);
            //}
            File beanTreeFile = File.createTempFile("beanTree", "txt");
            config.setDumpBeanTree(beanTreeFile);
            ref("Calling GenBeans.doIt");
            GenBeans.doIt(config);

            ref("Bean Tree:");
            ref(beanTreeFile);
            beanTreeFile.delete();
            
            if (testCompile) {
                ref("Compiling");
                String cmd = getJdkHome() + "javac -nowarn -classpath "+workDir.toString()+File.pathSeparator+getDataDir().toString()+File.pathSeparator+theClassPath+" "+getDataDir().toString()+"/"+testName+".java";
                int result = runCommandToSystemOut(cmd);
                ref("Finished compiling: "+result);

                //runCommand("ls -l "+getDataDir());
                cmd = getJdkHome() + "java -classpath "+workDir.toString()+File.pathSeparator+getDataDir().toString()+File.pathSeparator+theClassPath+" "+testName+" "+getDataDir().toString()+"/";
                result = runCommand(cmd);
                ref("Finished running "+testName+": "+result);
            }
            System.out.println("Finished.\n");
        } catch (Exception e) {
            ref(e.getMessage());
            e.printStackTrace();
        }
    }

    private int runCommand(String cmd) throws java.io.IOException, java.lang.InterruptedException {
        System.out.println(cmd);
        Process proc = Runtime.getRuntime().exec(cmd);
        Writer out = new BufferedWriter(new OutputStreamWriter(getRef(), "UTF-8"));
        Thread outThread = new Thread(new InputMonitor("out: ", proc.getInputStream(), out));
        outThread.start();
        Thread errThread = new Thread(new InputMonitor("err: ", proc.getErrorStream(), out));
        errThread.start();

        int result = proc.waitFor();
        out.flush();

        // Wait upto 32s for that thread to finish before going on.
        outThread.join(32000);
        errThread.join(32000);
        return result;
    }
    
    private int runCommandToSystemOut(String cmd) throws java.io.IOException, java.lang.InterruptedException {
        System.out.println(cmd);
        Process proc = Runtime.getRuntime().exec(cmd);
        int result = proc.waitFor();
        return result;
    }

    static class InputMonitor implements Runnable {
        private String prefix;
        private InputStream is;
        private Writer out;
        
        public InputMonitor(String prefix, InputStream is, Writer out) {
            this.prefix = prefix;
            this.is = is;
            this.out = out;
        }

        public void run() {
            try {
                int c;
                boolean freshLine = true;
                while ((c = is.read()) != -1) {
                    if (freshLine) {
                        out.write(prefix);
                        freshLine = false;
                    }
                    char ch = (char)c;
                    if (ch == '\n')
                        freshLine = true;
                    out.write(ch);
                }
                out.flush();
            } catch (java.io.IOException e) {
                try {
                    out.write(e.getMessage());
                } catch (java.io.IOException e2) {
                    // try only once.
                }
            }
        }
    }

    //protected File dataDir;
    protected String theClassPath = "";
    
    protected void setUp() {
        // when running this code inside IDE, getResource method returns URL in NBFS
        // format, so we need to convert it to filename
        // when running this code inside code mode, nothing happens        
        //String dataDirName = NbTestCase.convertNBFSURL(MainTest.class.getResource("data"));
        //dataDir = new File(dataDirName);
        //System.out.println("dataDirName="+dataDirName);

        theClassPath += classPathEntryFromURL(org.netbeans.modules.schema2beans.BaseBean.class);
        theClassPath += File.pathSeparator + classPathEntryFromURL(org.openide.filesystems.FileObject.class);
        theClassPath += File.pathSeparator + classPathEntryFromURL(org.openide.util.Lookup.class);
        //theClassPath += File.pathSeparator + classPathEntryFromURL(javax.xml.namespace.QName.class);
        //theClassPath += File.pathSeparator + classPathEntryFromURL(org.w3c.dom.Node.class);
        //theClassPath += File.pathSeparator + classPathEntryFromURL(javax.xml.parsers.DocumentBuilderFactory.newInstance().getClass());
        //theClassPath += File.pathSeparator + classPathEntryFromURL(org.w3c.dom.ranges.DocumentRange.class);
        System.out.println("classpath="+theClassPath);
    }

    private String classPathEntryFromURL(Class cls) {
        String shortName = cls.getName().substring(1+cls.getName().lastIndexOf('.'));
        URL url = cls.getResource(shortName + ".class");
        String file = url.getFile();
        if (url.getProtocol().equals("jar")) {
            // example: file = 'jar:/usr/local/j2sdkee1.3.1/lib/j2ee.jar!/org/w3c/dom/Node.class'
            String jarFile = file.substring(file.indexOf(':')+1);
            jarFile = jarFile.substring(0, jarFile.indexOf('!'));
            return jarFile;
        } else if (url.getProtocol().equals("file")) {
            // example: file='/home/cliffwd/cvs/dublin/nb_all/schema2beans/rt/src/org/netbeans/modules/schema2beans/GenBeans.class'
            String result = file.substring(0, file.length() - cls.getName().length() - 6);
            return result;
        } else {
            return file;
        }
    }
    
    protected void tearDown() {
        compareReferenceFiles();
    }

    // XXX: temporarily overriding compareReferenceFiles() to dump differences as
    // I do not know what problem there is on javaee continual tester as there is
    // no access to diff files
    public void compareReferenceFiles(String testFilename, String goldenFilename, String diffFilename) {
        try {
            File goldenFile = getGoldenFile(goldenFilename);
            File testFile = new File(getWorkDir(),testFilename);
            File diffFile = new File(getWorkDir(),diffFilename);
            String message = "Files differ";
            if(System.getProperty("xtest.home") == null) {
                // show location of diff file only when run without XTest (run file in IDE)
                message += "; check "+diffFile;
            }
            try {
            assertFile(message, testFile, goldenFile, diffFile);
            } catch (AssertionFileFailedError e) {
                BufferedReader diffFileReader = new BufferedReader(new FileReader(diffFile));
                StringBuffer diff = new StringBuffer();
                try {
                    String ss = diffFileReader.readLine();
                    while (ss != null) {
                        diff.append(ss+"\n");
                        ss = diffFileReader.readLine();
                    }
                } finally {
                    diffFileReader.close();
                }
                throw new AssertionFileFailedError("DIFF: "+diffFile.toString()+"\n"+diff.toString(), e.getDiffFile());

            }
        } catch (IOException ioe) {
            fail("Could not obtain working direcory");
        }
    }


    public void ref(File f) throws IOException {
        Reader r = new FileReader(f);
        char buf[] = new char[1024];
        StringBuffer s = new StringBuffer();
        int len;
        while ((len = r.read(buf, 0, 1024)) > 0) {
            s.append(buf, 0, len);
        }
        r.close();
        ref(s.toString());
    }
    
    private String getJdkHome(){
        if (Utilities.isMac())
            return System.getProperty("java.home") + File.separator + "bin" + File.separator;
        else
            return System.getProperty("java.home") + File.separator + ".." + File.separator + "bin" + File.separator;

    }
}
