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

package org.netbeans.tax.dom;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.xml.tax.parser.ParserLoader;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.netbeans.tax.*;
import org.netbeans.tax.io.*;
import org.openide.xml.XMLUtil;
import org.apache.tools.ant.AntClassLoader;

/**
 * Sorry, complicated test setup. It requires OpenIDE and Ant libs.
 * There is also hardcoded path to isolated jars.
 *
 * @author Petr Kuzel
 */
public class WrapperTest extends NbTestCase {

    /**
     * Can anybody resolve it dynamically at runtime?
     */
    private static String AUTOLOAD_PREFIX = 
        System.getProperty("netbeans.test.xml.autoloadLibrariesPath", 
            "/jungle/prj/netbeans/40/nb_all/xml/netbeans/modules/autoload/ext/"
        );
    
    public WrapperTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(WrapperTest.class);
        
        return suite;
    }
    
    /** Test of wrap method, of class org.netbeans.tax.dom.Wrapper. */
    public void testWrap() throws Exception {
        System.out.println("testWrap");

        URL prototype = getClass().getResource("data/Prototype.xml");
        InputSource in = new InputSource(prototype.toExternalForm());
        ByteArrayOutputStream out;
        
        // prepare golden serialized XML
        
        Document  goldenDocument = XMLUtil.parse(in, false, false, null, null);
        out = new ByteArrayOutputStream(2000); 
        XMLUtil.write(goldenDocument, out, "UTF-8");
        String golden = out.toString("UTF-8");
        
        // prepare the same for tax        
        in = new InputSource(prototype.toExternalForm());
        in.setCharacterStream(new InputStreamReader(prototype.openStream(), "UTF8"));
        //ClassLoader loader = ParserLoader.getInstance();
        AntClassLoader loader = new AntClassLoader(getClass().getClassLoader(), true);
        String path = AUTOLOAD_PREFIX + "xerces2.jar";
        if (new File(path).exists() == false) {
            throw new IllegalStateException("Xerces file not found! " + path);
        };        
        loader.addPathElement(path);
        
        String taxpath = AUTOLOAD_PREFIX + "tax.jar";
        if (new File(taxpath).exists() == false) {
            throw new IllegalStateException("TAX file not found! " + taxpath);
        };                
        loader.addPathElement(taxpath);

        loader.forceLoadClass("org.netbeans.tax.io.XNIBuilder");
        loader.forceLoadClass("org.netbeans.tax.io.XNIBuilder$XMLBuilder");
        loader.forceLoadClass("org.netbeans.tax.io.XNIBuilder$DTDStopException");
        loader.forceLoadClass("org.netbeans.tax.io.XNIBuilder$DTDEntityResolver");
        loader.forceLoadClass("org.netbeans.tax.io.XNIBuilder$1");
        loader.addLoaderPackageRoot("org.apache.xerces");
        
        
        Class builderClass = loader.loadClass("org.netbeans.tax.io.XNIBuilder");
        Constructor builderConstructor = builderClass.getConstructor(new Class[] {
            Class.class, 
            InputSource.class, 
            EntityResolver.class, 
            TreeStreamBuilderErrorHandler.class
        });
        TreeBuilder builder = (TreeBuilder) builderConstructor.newInstance(new Object[] {
            TreeDocument.class,
            in,
            null,
            new TreeStreamBuilderErrorHandler() {
                public void message(int type, SAXParseException e) {
                    e.printStackTrace();
                }
            }
        });
        TreeDocumentRoot taxDocument = builder.buildDocument();
        Document wrappedDocument = Wrapper.wrap(taxDocument);
        
        out = new ByteArrayOutputStream(2000); 
        XMLUtil.write(wrappedDocument, out, "UTF-8");
        String serializedWrapped = out.toString("UTF-8");

        if (golden.equals(serializedWrapped) == false) {            
            System.out.println("Golden:\n" + golden);            
            System.out.println("====\nWrapped TAX:\n" + serializedWrapped);
            String serializedTax = Convertors.treeToString(taxDocument);
            System.out.println("====\nSerilized TAX:\n" + serializedTax);
            System.out.println("====");
            assertTrue("Serialized documents are different!", false);
        }
        
    }
    
    
}
