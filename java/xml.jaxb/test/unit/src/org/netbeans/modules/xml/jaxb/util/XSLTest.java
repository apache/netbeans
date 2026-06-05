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

package org.netbeans.modules.xml.jaxb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.junit.NbTestCase;


/**
 *
 * @author gmpatil
 */
public class XSLTest extends NbTestCase {
    private static final String CONFIG_FILE1 = "ConfigFile1.xml"; //NOI18N
    private static final String CONFIG_FILE2 = "ConfigFile2.xml"; //NOI18N
    private static final String CONFIG_FILE3 = "ConfigFile3.xml"; //NOI18N
    private static final String CONFIG_EMPTY_CAT = "ConfigFileEmptyCatalog.xml"; //NOI18N
    private static final String BUILD_FILE1 = "BuildFile1.xml";   //NOI18N
    private static final String BUILD_FILE2 = "BuildFile2.xml"; //NOI18N
    private static final String BUILD_FILE3 = "BuildFile3.xml"; //NOI18N
    private static final String BUILD_EMPTY_CAT = "BuildFileEmptyCatalog.xml"; //NOI18N

    private static final String XSL_FILE =
            "/org/netbeans/modules/xml/jaxb/resources/JAXBBuild.xsl"; //NOI18N
    private static final String TEMP_BUILD_FILE = "jaxb_build" ; //NOI18N

    public XSLTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
    }

    @Override
    public void tearDown() throws Exception {
    }

    private InputStream getFromClasspath(String filePath){
        return this.getClass().getResourceAsStream(filePath);
    }

    private InputStream getDatafile(String filename) throws FileNotFoundException{
        String dataFilename = "/org/netbeans/modules/xml/jaxb/util/" + filename;
        return new FileInputStream(new File(getDataDir(), dataFilename));
    }

    private String getString(InputStream stream) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line = "";
        while (line != null){
            line = br.readLine();
            if (line != null){
                sb.append(line);
            }
        }

        return sb.toString();
    }

    private void compareStream(InputStream file1, InputStream file2) throws IOException{
        String str1 = getString(file1);
        //System.out.println("Str1:" + str1 + ":Str1");
        String str2 = getString(file2);
        //System.out.println("Str2:" + str2 + ":Str2");
        //System.out.println("Length:" + str1.length() + ":" + str2.length());
        assertEquals(str1, str2);
    }

    private void transformConfig2Build(String configFile, String buildFile){
        try {
            Source xmlSource = new StreamSource(getDatafile(configFile));
            Source xslSource = new StreamSource(getFromClasspath(XSL_FILE));
            File tmpFile = java.io.File.createTempFile(TEMP_BUILD_FILE, ".xml");
            //System.out.println("tmpFile:" + tmpFile.getAbsolutePath());
            tmpFile.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
                Result result = new StreamResult(fos);
                TransformerFactory fact = TransformerFactory.newInstance();
                fact.setAttribute("indent-number", 4); //NOI18N
                Transformer xformer = fact.newTransformer(xslSource);
                xformer.setOutputProperty(OutputKeys.INDENT, "yes"); //NOI18N
                xformer.setOutputProperty(OutputKeys.METHOD, "xml"); //NOI18N
                xformer.transform(xmlSource, result);
            }
            // Compare.
            compareStream(getDatafile(buildFile), new FileInputStream(tmpFile));
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
            fail("TransformerConfigurationException");
        } catch (TransformerException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
            fail("TransformerException");
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
            fail("IOException");
        }
    }

    /**
     * Test the XSL style sheet.
     **/
    public void testXformConfig2BuildWithPkg(){
        transformConfig2Build(CONFIG_FILE1, BUILD_FILE1);
        System.out.println("testXformConfig2BuildWithPkg done.");
    }

    /**
     * Test the XSL style sheet.
     **/
    public void testXformConfig2BuildWithoutPkg(){
        transformConfig2Build(CONFIG_FILE2, BUILD_FILE2);
        System.out.println("testXformConfig2BuildWithoutPkg done.");
    }

    /**
     * Test the XSL style sheet.
     **/
    public void testXformConfig2BuildEmptySchema(){
        transformConfig2Build(CONFIG_FILE3, BUILD_FILE3);
        System.out.println("testXformConfig2BuildEmptySchema done.");
    }

    /**
     * Test the XSL style sheet.
     **/
    public void testXformConfig2BuildEmptyCatalog(){
        transformConfig2Build(CONFIG_EMPTY_CAT, BUILD_EMPTY_CAT);
        System.out.println("testXformConfig2BuildEmptyCatalog done.");
    }

}
