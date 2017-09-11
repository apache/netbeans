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
    public final static String LINE_BREAK_METATAG = "|";
    
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
