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

package org.netbeans.modules.javadoc.search;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.LinkedList;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.LocalFileSystem;

/**
 *
 * @author Jan Pokorsky
 */
public final class SearchThreadJdk12Test extends NbTestCase {

    private LocalFileSystem fs;
    private static final String JDK14_INDEX_PATH = "docs_jdk14/api/index-files";
    private static final String JDK15_INDEX_PATH = "docs_jdk15/api/index-files";
    private static final String JDK7_INDEX_PATH = "docs_jdk7/api/index-files";
    private static final String JDK8_INDEX_PATH = "docs_jdk8/api/index-files";

    public SearchThreadJdk12Test(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        File dataFile = getDataDir();
        assertNotNull("missing data file", dataFile);
        fs = new LocalFileSystem();
        fs.setRootDirectory(dataFile);
    }

    public void testSearchInJDK14_Class() throws Exception {
        URL idxFolder = fs.findResource(JDK14_INDEX_PATH).getURL();
        
        String toFind = "DataFlavor";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 6, diiConsumer.l.size());
        
        // class DataFlavor
        DocIndexItem dii = diiConsumer.l.get(0);
        assertEquals("field", "DataFlavor", dii.getField());
        assertEquals("declaring class", "DataFlavor", dii.getDeclaringClass());
        assertEquals("remark", " - class java.awt.datatransfer.DataFlavor.", dii.getRemark());
        assertEquals("package", "java.awt.datatransfer.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/awt/datatransfer/DataFlavor.html"));
        // constructor
        dii = diiConsumer.l.get(1);
        assertEquals("field", "DataFlavor()", dii.getField());
        assertEquals("declaring class", "DataFlavor", dii.getDeclaringClass());
        assertEquals("remark", " - Constructor for class java.awt.datatransfer.DataFlavor", dii.getRemark());
        assertEquals("package", "java.awt.datatransfer.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/awt/datatransfer/DataFlavor.html#DataFlavor()"));
    }
    
    public void testSearchInJDK14_Interface() throws Exception {
        URL idxFolder = fs.findResource(JDK14_INDEX_PATH).getURL();
        
        String toFind = "DatabaseMetaData";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 1, diiConsumer.l.size());
        
        // DatabaseMetaData
        DocIndexItem dii = diiConsumer.l.get(0);
        assertEquals("field", "DatabaseMetaData", dii.getField());
        assertEquals("declaring class", "DatabaseMetaData", dii.getDeclaringClass());
        assertEquals("remark", " - interface java.sql.DatabaseMetaData.", dii.getRemark());
        assertEquals("package", "java.sql.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/sql/DatabaseMetaData.html"));
    }

    public void testSearchInJDK14_Exception() throws Exception {
        URL idxFolder = fs.findResource(JDK14_INDEX_PATH).getURL();
        
        String toFind = "DataFormatException";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 3, diiConsumer.l.size());
        
        // DataFormatException
        DocIndexItem dii = diiConsumer.l.get(0);
        assertEquals("field", "DataFormatException", dii.getField());
        assertEquals("declaring class", "DataFormatException", dii.getDeclaringClass());
        assertEquals("remark", " - exception java.util.zip.DataFormatException.", dii.getRemark());
        assertEquals("package", "java.util.zip.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/util/zip/DataFormatException.html"));
        
        // DataFormatException(String) - constructor
        dii = diiConsumer.l.get(2);
        assertEquals("field", "DataFormatException(String)", dii.getField());
        assertEquals("declaring class", "DataFormatException", dii.getDeclaringClass());
        assertEquals("remark", " - Constructor for class java.util.zip.DataFormatException", dii.getRemark());
        assertEquals("package", "java.util.zip.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/util/zip/DataFormatException.html#DataFormatException(java.lang.String)"));
    }

    public void testSearchInJDK14_Method() throws Exception {
        URL idxFolder = fs.findResource(JDK14_INDEX_PATH).getURL();
        
        String toFind = "damageLineRange";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 1, diiConsumer.l.size());
        
        // damageLineRange(int, int, Shape, Component)
        DocIndexItem dii = diiConsumer.l.get(0);
        assertEquals("field", "damageLineRange(int, int, Shape, Component)", dii.getField());
        assertEquals("declaring class", "PlainView", dii.getDeclaringClass());
        assertEquals("remark", " - Method in class javax.swing.text.PlainView", dii.getRemark());
        assertEquals("package", "javax.swing.text.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/javax/swing/text/PlainView.html#damageLineRange(int, int, java.awt.Shape, java.awt.Component)"));
    }

    public void testSearchInJDK14_Variables() throws Exception {
        URL idxFolder = fs.findResource(JDK14_INDEX_PATH).getURL();
        
        String toFind = "darkShadow";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 4, diiConsumer.l.size());
        
        // darkShadow
        DocIndexItem dii = diiConsumer.l.get(0);
        assertEquals("field", "darkShadow", dii.getField());
        assertEquals("declaring class", "BasicBorders.ButtonBorder", dii.getDeclaringClass());
        assertEquals("remark", " - Variable in class javax.swing.plaf.basic.BasicBorders.ButtonBorder", dii.getRemark());
        assertEquals("package", "javax.swing.plaf.basic.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/javax/swing/plaf/basic/BasicBorders.ButtonBorder.html#darkShadow"));
        
        // darkShadowColor - static variable
        dii = diiConsumer.l.get(3);
        assertEquals("field", "darkShadowColor", dii.getField());
        assertEquals("declaring class", "MetalSliderUI", dii.getDeclaringClass());
        assertEquals("remark", " - Static variable in class javax.swing.plaf.metal.MetalSliderUI", dii.getRemark());
        assertEquals("package", "javax.swing.plaf.metal.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/javax/swing/plaf/metal/MetalSliderUI.html#darkShadowColor"));
    }

    public void testSearchInJDK15_Class() throws Exception {
        URL idxFolder = fs.findResource(JDK15_INDEX_PATH).getURL();
        
        String toFind = "DataFlavor";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 6, diiConsumer.l.size());
        
        // class DataFlavor
        DocIndexItem dii = diiConsumer.l.get(0);
        assertEquals("field", "DataFlavor", dii.getField());
        assertEquals("declaring class", "java.awt.datatransfer", dii.getDeclaringClass());
        assertEquals("remark", " - Class in java.awt.datatransfer", dii.getRemark());
        assertEquals("package", "java.awt.datatransfer.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/awt/datatransfer/DataFlavor.html"));
        // constructor
        dii = diiConsumer.l.get(1);
        assertEquals("field", "DataFlavor()", dii.getField());
        assertEquals("declaring class", "DataFlavor", dii.getDeclaringClass());
        assertEquals("remark", " - Constructor for class java.awt.datatransfer.DataFlavor", dii.getRemark());
        assertEquals("package", "java.awt.datatransfer.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/awt/datatransfer/DataFlavor.html#DataFlavor()"));
    }

    public void testSearchInJDK15_GenericClass_54244() throws Exception {
        // see issue #54244
        URL idxFolder = fs.findResource(JDK15_INDEX_PATH).getURL();
        
        String toFind = "DemoHashMap";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 2, diiConsumer.l.size());
        
        // class DemoHashMap<K,V>
        DocIndexItem dii = diiConsumer.l.get(0);
        assertEquals("field", "DemoHashMap", dii.getField());
        assertEquals("declaring class", "java.util", dii.getDeclaringClass());
        assertEquals("remark", " - Class in java.util", dii.getRemark());
        assertEquals("package", "java.util.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/util/DemoHashMap.html"));
        // generic constructor DemoHashMap(Map<? extends K, ? extends V>)
        dii = diiConsumer.l.get(1);
        assertEquals("field", "DemoHashMap(Map<? extends K, ? extends V>)", dii.getField());
        assertEquals("declaring class", "DemoHashMap", dii.getDeclaringClass());
        assertEquals("remark", " - Constructor for class java.util.DemoHashMap", dii.getRemark());
        assertEquals("package", "java.util.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/util/DemoHashMap.html#DemoHashMap(java.util.Map)"));
    }

    public void testSearchInJDK15_Method() throws Exception {
        URL idxFolder = fs.findResource(JDK15_INDEX_PATH).getURL();
        
        String toFind = "damageLineRange";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 1, diiConsumer.l.size());
        
        // damageLineRange(int, int, Shape, Component)
        DocIndexItem dii = diiConsumer.l.get(0);
        assertEquals("field", "damageLineRange(int, int, Shape, Component)", dii.getField());
        assertEquals("declaring class", "PlainView", dii.getDeclaringClass());
        assertEquals("remark", " - Method in class javax.swing.text.PlainView", dii.getRemark());
        assertEquals("package", "javax.swing.text.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/javax/swing/text/PlainView.html#damageLineRange(int, int, java.awt.Shape, java.awt.Component)"));
    }

    public void testSearchInJDK15_Variables() throws Exception {
        URL idxFolder = fs.findResource(JDK15_INDEX_PATH).getURL();
        
        String toFind = "darkShadow";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 4, diiConsumer.l.size());
        
        // darkShadow
        DocIndexItem dii = diiConsumer.l.get(0);
        assertEquals("field", "darkShadow", dii.getField());
        assertEquals("declaring class", "BasicBorders.ButtonBorder", dii.getDeclaringClass());
        assertEquals("remark", " - Variable in class javax.swing.plaf.basic.BasicBorders.ButtonBorder", dii.getRemark());
        assertEquals("package", "javax.swing.plaf.basic.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/javax/swing/plaf/basic/BasicBorders.ButtonBorder.html#darkShadow"));
        
        // darkShadowColor - static variable
        dii = diiConsumer.l.get(3);
        assertEquals("field", "darkShadowColor", dii.getField());
        assertEquals("declaring class", "MetalSliderUI", dii.getDeclaringClass());
        assertEquals("remark", " - Static variable in class javax.swing.plaf.metal.MetalSliderUI", dii.getRemark());
        assertEquals("package", "javax.swing.plaf.metal.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/javax/swing/plaf/metal/MetalSliderUI.html#darkShadowColor"));
    }

    public void testSearchInJDK15_Exception() throws Exception {
        URL idxFolder = fs.findResource(JDK15_INDEX_PATH).getURL();
        
        String toFind = "DataFormatException";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 3, diiConsumer.l.size());
        
        // DataFormatException
        DocIndexItem dii = diiConsumer.l.get(0);
        assertEquals("field", "DataFormatException", dii.getField());
        assertEquals("declaring class", "java.util.zip", dii.getDeclaringClass());
        assertEquals("remark", " - Exception in java.util.zip", dii.getRemark());
        assertEquals("package", "java.util.zip.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/util/zip/DataFormatException.html"));
        
        // DataFormatException(String) - constructor
        dii = diiConsumer.l.get(2);
        assertEquals("field", "DataFormatException(String)", dii.getField());
        assertEquals("declaring class", "DataFormatException", dii.getDeclaringClass());
        assertEquals("remark", " - Constructor for exception java.util.zip.DataFormatException", dii.getRemark());
        assertEquals("package", "java.util.zip.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/util/zip/DataFormatException.html#DataFormatException(java.lang.String)"));
    }
    
    public void testSearchInJDK15_Interface() throws Exception {
        URL idxFolder = fs.findResource(JDK15_INDEX_PATH).getURL();
        
        String toFind = "DatabaseMetaData";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 1, diiConsumer.l.size());
        
        // DatabaseMetaData
        DocIndexItem dii = diiConsumer.l.get(0);
        assertEquals("field", "DatabaseMetaData", dii.getField());
        assertEquals("declaring class", "java.sql", dii.getDeclaringClass());
        assertEquals("remark", " - Interface in java.sql", dii.getRemark());
        assertEquals("package", "java.sql.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/sql/DatabaseMetaData.html"));
    }
    
    public void testSearchInJDK15_Enum() throws Exception {
        URL idxFolder = fs.findResource(JDK15_INDEX_PATH).getURL();
        
        String toFind = "DemoMemoryType";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 1, diiConsumer.l.size());
        
        // DemoMemoryType
        DocIndexItem dii = diiConsumer.l.get(0);
        assertEquals("field", "DemoMemoryType", dii.getField());
        assertEquals("declaring class", "java.lang.management", dii.getDeclaringClass());
        assertEquals("remark", " - Enum in java.lang.management", dii.getRemark());
        assertEquals("package", "java.lang.management.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/lang/management/DemoMemoryType.html"));
    }
    
    public void testSearchInJDK15_AnnotationType() throws Exception {
        URL idxFolder = fs.findResource(JDK15_INDEX_PATH).getURL();
        
        String toFind = "Deprecated";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 1, diiConsumer.l.size());
        
        // Deprecated
        DocIndexItem dii = diiConsumer.l.get(0);
        assertEquals("field", "Deprecated", dii.getField());
        assertEquals("declaring class", "java.lang", dii.getDeclaringClass());
        assertEquals("remark", " - Annotation Type in java.lang", dii.getRemark());
        assertEquals("package", "java.lang.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/lang/Deprecated.html"));
    }
    
    public void testSearchInJDK7_Class() throws Exception {
        URL idxFolder = fs.findResource(JDK7_INDEX_PATH).getURL();
        
        String toFind = "DataFlavor";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 6, diiConsumer.l.size());
        
        // class DataFlavor
        DocIndexItem dii = diiConsumer.l.get(0);
        assertEquals("field", "DataFlavor", dii.getField());
        assertEquals("declaring class", "java.awt.datatransfer", dii.getDeclaringClass());
        assertEquals("remark", " - Class in java.awt.datatransfer", dii.getRemark());
        assertEquals("package", "java.awt.datatransfer.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/awt/datatransfer/DataFlavor.html"));
        // constructor
        dii = diiConsumer.l.get(1);
        assertEquals("field", "DataFlavor()", dii.getField());
        assertEquals("declaring class", "DataFlavor", dii.getDeclaringClass());
        assertEquals("remark", " - Constructor for class java.awt.datatransfer.DataFlavor", dii.getRemark());
        assertEquals("package", "java.awt.datatransfer.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/awt/datatransfer/DataFlavor.html#DataFlavor()"));
    }
    
    public void testSearchInJDK8_Class() throws Exception {
        URL idxFolder = fs.findResource(JDK8_INDEX_PATH).getURL();
        
        String toFind = "DataFlavor";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 6, diiConsumer.l.size());
        
        // class DataFlavor
        DocIndexItem dii = diiConsumer.l.get(0);
        assertEquals("field", "DataFlavor", dii.getField());
        assertEquals("declaring class", "java.awt.datatransfer", dii.getDeclaringClass());
        assertEquals("remark", " - Class in java.awt.datatransfer", dii.getRemark());
        assertEquals("package", "java.awt.datatransfer.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/awt/datatransfer/DataFlavor.html"));
        // constructor
        dii = diiConsumer.l.get(1);
        assertEquals("field", "DataFlavor()", dii.getField());
        assertEquals("declaring class", "DataFlavor", dii.getDeclaringClass());
        assertEquals("remark", " - Constructor for class java.awt.datatransfer.DataFlavor", dii.getRemark());
        assertEquals("package", "java.awt.datatransfer.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/awt/datatransfer/DataFlavor.html#DataFlavor--"));
    }
    
    private static final class MyDocIndexItemConsumer implements IndexSearchThread.DocIndexItemConsumer {
        boolean isFinished = false;
        List<DocIndexItem> l = new LinkedList<DocIndexItem>();
            
        public @Override void addDocIndexItem(DocIndexItem dii) {
//            try {
//            System.out.println("dc: " + dii.getDeclaringClass() + ", field: " + dii.getField() +
//                    ", pkg: " + dii.getPackage() + ", remark: " + dii.getRemark() + ", url: " + dii.getURL().toString());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            l.add(dii);
        }

        public @Override void indexSearchThreadFinished(IndexSearchThread ist) {
//            System.out.println("-------------------------------");
            isFinished = true;
        }
    }
}
