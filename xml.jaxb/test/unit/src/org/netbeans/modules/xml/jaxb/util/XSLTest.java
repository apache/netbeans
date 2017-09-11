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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xml.jaxb.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
    private static final String CONFIG_FILE1 = "/data/ConfigFile1.xml"; //NOI18N
    private static final String CONFIG_FILE2 = "/data/ConfigFile2.xml"; //NOI18N    
    private static final String CONFIG_FILE3 = "/data/ConfigFile3.xml"; //NOI18N        
    private static final String CONFIG_EMPTY_CAT = 
            "/data/ConfigFileEmptyCatalog.xml"; //NOI18N        
    private static final String BUILD_FILE1 = "/data/BuildFile1.xml";   //NOI18N
    private static final String BUILD_FILE2 = "/data/BuildFile2.xml"; //NOI18N 
    private static final String BUILD_FILE3 = "/data/BuildFile3.xml"; //NOI18N     
    private static final String BUILD_EMPTY_CAT = 
            "/data/BuildFileEmptyCatalog.xml"; //NOI18N     
    
    private static final String XSL_FILE = 
            "/org/netbeans/modules/xml/jaxb/resources/JAXBBuild.xsl"; //NOI18N
    private static final String TEMP_BUILD_FILE = "jaxb_build" ; //NOI18N
    
    public XSLTest(String testName) {
        super(testName);
    }
    
    public void setUp() throws Exception {
    }
    
    public void tearDown() throws Exception {
    }
    
    private InputStream getInputStream(String filePath){
        return this.getClass().getResourceAsStream(filePath);
    }
    
    private String getString(InputStream stream) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuffer sb = new StringBuffer();
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
        boolean ret = false;
        String str1 = getString(file1);
        //System.out.println("Str1:" + str1 + ":Str1");
        String str2 = getString(file2);
        //System.out.println("Str2:" + str2 + ":Str2");        
        //System.out.println("Length:" + str1.length() + ":" + str2.length());                                
        assertEquals(str1, str2);
    }
    
    private void transformConfig2Build(String configFile, String buildFile){
        try {
            Source xmlSource = new StreamSource(getInputStream(configFile));
            Source xslSource = new StreamSource(getInputStream(XSL_FILE));
            File tmpFile = java.io.File.createTempFile(TEMP_BUILD_FILE, ".xml");
            //System.out.println("tmpFile:" + tmpFile.getAbsolutePath());
            tmpFile.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tmpFile);
            Result result = new StreamResult(fos);
            TransformerFactory fact = TransformerFactory.newInstance();
            fact.setAttribute("indent-number", 4); //NOI18N
            Transformer xformer = fact.newTransformer(xslSource);
            xformer.setOutputProperty(OutputKeys.INDENT, "yes"); //NOI18N
            xformer.setOutputProperty(OutputKeys.METHOD, "xml"); //NOI18N
            xformer.transform(xmlSource, result);
            // Compare.
            fos.close();
            compareStream(getInputStream(buildFile), new FileInputStream(tmpFile));
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
