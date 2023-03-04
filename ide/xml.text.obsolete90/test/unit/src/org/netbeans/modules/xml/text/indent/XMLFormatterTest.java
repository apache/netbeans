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

package org.netbeans.modules.xml.text.indent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import junit.framework.*;
import java.io.IOException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.text.syntax.XMLKit;


/**
 * Formatting related tests based on old formatter. See XMLFormatter.
 * @author Tomasz Slota
 */
public class XMLFormatterTest extends NbTestCase {
    /**
     * Used for testing dynamic indentation ("smart enter")
     */
    public static final String LINE_BREAK_METATAG = "|";
    
    public XMLFormatterTest(String testName) {
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
        suite.addTest(new XMLFormatterTest("testReformatSample1"));
        suite.addTest(new XMLFormatterTest("testReformatSample2"));        
        return suite;
    }

    public void testReformatSample1() {
        testReformat("web.xml");
    }
    
    public void testReformatSample2(){
        testReformat("netbeans_build.xml");
    }
    
    private String extractCRMetatag(String text){
        int indexOfMetatag = text.indexOf(LINE_BREAK_METATAG);
        
        if (indexOfMetatag == -1){
            return text;
        }
        
        String firstPart = text.substring(0, indexOfMetatag);
        String secondPart = text.substring(indexOfMetatag + LINE_BREAK_METATAG.length());
        
        if (secondPart.indexOf(LINE_BREAK_METATAG) > -1){
            throw new IllegalArgumentException("text contains more than one line break tag");
        }
        
        return firstPart + secondPart;
    }
    
    private void testReformat(String testFileName) {
        System.out.println("testReformat(" + testFileName + ")");
        XMLFormatter formatter = new XMLFormatter(XMLKit.class);
        BaseDocument doc = createEmptyBaseDocument();
         
        try{
            String txtRawXML = readStringFromFile(new File(new File(
                getTestFilesDir(), "testReformat"), testFileName));
                    
            doc.insertString(0, txtRawXML, null);
            formatter.reformat(doc, 0, doc.getLength(), false);
            getRef().print(doc.getText(0, doc.getLength()));
        }
        catch (Exception e){
            fail(e.getMessage()); // should never happen
        }
        
        compareReferenceFiles();
    }

    private String readStringFromFile(File file) throws IOException {
        StringBuffer buff = new StringBuffer();
        
        BufferedReader rdr = new BufferedReader(new FileReader(file));
        
        String line;
        
        try{
            while ((line = rdr.readLine()) != null){
                buff.append(line + "\n");
            }
        } finally{
            rdr.close();
        }
        
        return buff.toString();
    }
    
    private File getTestFilesDir(){
        return new File(new File(getDataDir(), "input"), "XMLFormatterTest");
    }
    
    private BaseDocument createEmptyBaseDocument(){
        return new BaseDocument(true, "text/xml"); //NOI18N
    }
}
