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
