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

package org.netbeans.modules.xml.xdm.perf;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * This test class is meant to be used for obtaining performance numbers on XDM.
 * NOTE: The numbers may vary on each run and on each machine. Also please note
 * the following before running these tests.
 *
 * 1. Do not run testReadUsingSyntaxParser and testReadUsingXDM together.
 * 2. Avoid insertString for each line. This is BAD. See usage for 'readLine'
 * and 'insertEachLine'.
 *
 * @author Samaresh
 */
public class XDMPerfNumberTest extends TestCase {
    
    /**
     * Performance numbers are run on this giant schema.
     */
    static final String SCHEMA_FILE = "J1_TravelItinerary.xsd";
        
    /**
     * Line separator.
     */
    static String lineSeparator = System.getProperty("line.separator");
    
    /**
     * boolean flag, to indicate whether to call insertString() for each line
     * or not. Applicable only when 'readLine' is true. When true, the performance
     * is really BAD.
     */
    boolean insertEachLine = false;
    
    /**
     * boolean flag, to indicate whether to read line-by-line or
     * character-by-character. This flag does NOT make any major
     * difference.
     */
    boolean readLine = false;
    
    public XDMPerfNumberTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
//        Disabled as referenced files were partly not donated by oracle to apache
//        suite.addTest(new XDMPerfNumberTest("testReadUsingDOM"));
//        suite.addTest(new XDMPerfNumberTest("testReadUsingSyntaxParser"));
//        suite.addTest(new XDMPerfNumberTest("testReadUsingXDM"));        
        return suite;
    }
            
    public void testReadUsingDOM() throws Exception {
        System.out.println("testReadUsingDOM");
        long start = System.currentTimeMillis();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream is = getClass().getResourceAsStream(SCHEMA_FILE);
        org.w3c.dom.Document document = builder.parse(is);
        this.assertNotNull("DOM model didn't get created!!!", document);
        long end = System.currentTimeMillis();
        System.out.println("Time taken to parse using DOM: " + (end-start) + "ms.\n");
    }
        
    public void testReadUsingSyntaxParser() throws Exception {
        System.out.println("testReadUsingSyntaxParser");
        java.net.URL url = getClass().getResource(SCHEMA_FILE);            
        // prepare document
        BaseDocument basedoc = new BaseDocument(true, "text/xml"); //NOI18N
        insertStringInDocument(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8), basedoc);
        long start = System.currentTimeMillis();
        SyntaxSupport sup = basedoc.getSyntaxSupport();
        TokenItem ti = sup.getTokenChain(0);
        while(ti != null) {
            ti = ti.getNext();
        }
        long end = System.currentTimeMillis();
        System.out.println("Time taken to parse using Syntax Parser: " + (end-start) + "ms.\n");
        this.assertNotNull("Syntax parser model didn't get created!!!", sup);
    }    
    
    public void testReadUsingXDM() throws Exception {
        long start = System.currentTimeMillis();
        javax.swing.text.Document sd = new BaseDocument(true, "text/xml"); //NOI18N
        XDMModel model = null;

        InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(SCHEMA_FILE), StandardCharsets.UTF_8);
        insertStringInDocument(reader, sd);

        start = System.currentTimeMillis();
        Lookup lookup = Lookups.singleton(sd);
        ModelSource ms = new ModelSource(lookup, true);
        model = new XDMModel(ms);
        model.sync();
        this.assertNotNull("XDM model didn't get created!!!", model);
        long end = System.currentTimeMillis();
        System.out.println("Time taken to parse using XDM: " + (end-start) + "ms.\n");
    }
    
    
    /**
     * Reads data in a buffer reader and then accumulates them in a
     * string buffer
     */
    private void insertStringInDocument(InputStreamReader inputStreamReader,
            javax.swing.text.Document document) {
        
        BufferedReader reader = null;
        System.out.println("insertStringInDocument() called...");
        long start = System.currentTimeMillis();
        try {
            reader = new BufferedReader(inputStreamReader);
            StringBuffer sbuf = new StringBuffer();
            if(readLine) {
                insertLines(reader, document);
            } else {
                insertCharacters(reader, document);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Time taken in insertStringInDocument(): " + (end-start) + "ms.");        
    }
    
    
    /**
     * Inserts text into the swing document line by line.
     * If 'insertEachLine' is true, inserts them directly, otherwise
     * keeps them in a buffer and inserts at the end.
     * The former is BAD. Do NOT insert each line.
     */
    private void insertLines(BufferedReader reader,
            javax.swing.text.Document document) throws Exception {
        StringBuffer buffer = new StringBuffer();
        String line = null;
        while ((line = reader.readLine()) != null) {
            //do not ever insert each line into the doc. This is a killer.
            if(insertEachLine)
                document.insertString(document.getLength(), line+lineSeparator, null);
            else
                buffer.append(line+lineSeparator);
        }
        if(!insertEachLine) {
            document.insertString(0, buffer.toString(), null);
        }
    }
    
    /**
     * Inserts text into the swing document character by character.
     */
    private void insertCharacters(BufferedReader reader,
            javax.swing.text.Document document) throws Exception {
        StringBuffer buffer = new StringBuffer();
        int c = 0;
        while((c = reader.read()) != -1) {
            buffer.append((char)c);
        }
        
        //finally one insertString
        document.insertString(0, buffer.toString(), null);
    }
}
