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
package org.openide.filesystems.xmlfs;

import java.io.*;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.filesystems.localfs.LocalFSTest;
import org.openide.filesystems.data.SerialData;

/**
 * Base class for testing XMLFileSystem.
 */
public class XMLFSTest extends ReadOnlyFSTest {

    public static final String PACKAGE = "org/openide/filesystems/data/";
    private static final String MF_NAME = "mf-layer";
    private static final String HEADER = "<?xml version=\"1.0\"?>";//\n<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.0//EN\" \"http://www.netbeans.org/dtds/filesystem-1_0.dtd\">";
    private static final String FOLDER_START1 = "  <folder name=\"org\">\n    <folder name=\"openide\">\n      <folder name=\"filesystems\">\n        <folder name=\"data";
    private static final String FOLDER_START2 = "\">";
    private static final String FOLDER_END = "        </folder>\n      </folder>\n    </folder>\n  </folder>\n";
    private static final int FOLDER_INDENT  = FOLDER_END.indexOf('<') / 2;
    private static final String INDENT_STEP  = "  ";
    
    /** Root folder for this test */
    protected File tmp;
    /** Working folder */
    protected File destFolder;
    /** Tested XMLFS */
    protected XMLFileSystem xmlfs;
    
    /** Creates new XMLFSGenerator */
    public XMLFSTest(String name) {
        super(name);
    }

    /** Set up given number of FileObjects */
    protected FileObject[] setUpFileObjects(int foCount) throws Exception {
        tmp = createTempFolder();
        destFolder = LocalFSTest.createFiles(foCount, 0, tmp);
        File xmlbase = generateXMLFile(destFolder, new ResourceComposer(LocalFSTest.RES_NAME, LocalFSTest.RES_EXT, foCount, 0));
        xmlfs = new XMLFileSystem();
        xmlfs.setXmlUrl(xmlbase.toURL(), false);
        
        FileObject pkg = xmlfs.findResource(PACKAGE);
        return pkg.getChildren();
    }
    
    /** Disposes given FileObjects */
    protected void tearDownFileObjects(FileObject[] fos) throws Exception {
        destFolder = null;
        delete(tmp);
        tmp = null;
    }
    
    /** Generates an XML file that describes a filesystem structure.
     * @param folder - where to place the file
     * @param composer a factory that assemblies resource strings
     */
    public static final File generateXMLFile(File folder, ResourceComposer composer) throws Exception {
        String name = MF_NAME + '-' + String.valueOf(composer.getFileBase());
        File dest = new File(folder, name.concat(".xml"));
        
        OutputStream os = new FileOutputStream(dest);
        Writer writer = new OutputStreamWriter(os);
        writer.write(generate(composer));
        writer.close();
        os.close();
        
        return dest;
    }
    
    /** Generates an XML file that describes a filesystem structure.
     * @return a String that is an xml document describing a filesystem
     */
    public static String generate(ResourceComposer composer) throws Exception {
        StringBuffer buffer = new StringBuffer(50000);
        buffer.append(HEADER).append('\n');
        buffer.append("<filesystem>").append('\n');
        generateFolder(buffer, composer);
        buffer.append("</filesystem>").append('\n');
        
        return buffer.toString();
    }
    
    /** Generates an XML description of a folder inside a filesystem structure.
     * @param buffer - where to place the description
     */
    private static final void generateFolder(StringBuffer buffer, ResourceComposer composer) throws Exception {
        buffer.append(FOLDER_START1);
        int base = composer.getFileBase();
        if (base != 0) {
            buffer.append(base);
        }
        buffer.append(FOLDER_START2);
        generateFiles(buffer, composer);
        buffer.append(FOLDER_END);
    }

    /** Generates an XML description of files inside a folder structure.
     * @param buffer - where to place the description
     */
    private static final void generateFiles(StringBuffer buffer, ResourceComposer composer) throws Exception {
        int base = composer.getFileBase();
        int fileNo = composer.getFileCount();
        for (int i = 0; i < fileNo; i++) {
            composer.setFileBase(base + i);
            generateOneFile(buffer, composer);
        }
    }
    
    /** Generates an XML description of a file inside a folder structure.
     * @param buffer - where to place the description
     */
    private static void generateOneFile(StringBuffer buffer, ResourceComposer composer) throws Exception {
        buffer.append('\n');
        addFileHeader(buffer, composer);
        generateAttributes(buffer, composer.getPaddingSize());
        addFileEnd(buffer);
    }
    
    /** Generates an XML description of attributes inside a file description.
     * @param buffer - where to place the description
     */
    private static void generateAttributes(StringBuffer buffer, int paddingSize) throws Exception {
        generateSerialAttr(buffer);
        for (int i = 0; i < 5; i++) {
            generateStringAttr(buffer, i, paddingSize);
        }
    }
    
    /** Generates a serial attribute inside a file description.
     * @param buffer - where to place the description
     */
    private static void generateSerialAttr(StringBuffer buffer) throws Exception {
        addIndent(buffer, FOLDER_INDENT + 2);
        buffer.append("<attr name=\"NetBeansAttrSerial\" serialvalue=\"").append(SerialData.getSerialDataString()).append("\"/>");
        buffer.append('\n');
    }
    
    /** Generates i-th String attribute inside a file description.
     * @param buffer - where to place the description
     */
    private static void generateStringAttr(StringBuffer buffer, int i, int paddingSize) {
        addIndent(buffer, FOLDER_INDENT + 2);
        buffer.append("<attr name=\"key_");
        Utilities.appendNDigits(i, paddingSize, buffer);
        buffer.append("\" stringvalue=\"val_");
        Utilities.appendNDigits(i, paddingSize, buffer);
        buffer.append("\"/>");
        buffer.append('\n');
    }
    
    /** Generates file end inside a folder description.
     * @param buffer - where to place the description
     */
    private static void addFileEnd(StringBuffer buffer) {
        addIndent(buffer, FOLDER_INDENT + 1);
        buffer.append("</file>");
        buffer.append('\n');
    }
    
    /** Generates file start inside a folder description.
     * @param buffer - where to place the description
     */
    private static void addFileHeader(StringBuffer buffer, ResourceComposer composer) {
        addIndent(buffer, FOLDER_INDENT + 1);
        buffer.append("<file name=\"");
        composer.assemblyResourceString(buffer);
        buffer.append("\" url=\"");
        composer.assemblyResourceString(buffer);
        buffer.append("\">").append('\n');
    }
    
    /** Adds indent
     * @param buffer - where to place the description
     */
    private static void addIndent(StringBuffer buffer, int howMuch) {
        for (int i = 0; i < howMuch; i++) {
            buffer.append(INDENT_STEP);
        }
    }
    
    /** Assemblies resource string */
    public static final class ResourceComposer {
        private final int paddingSize;
        private int fileBase;
        private final int foCount;
        private String resName;
        private String resExt;
        
        /** new ResourceComposer */
        public ResourceComposer(String resName, String resExt, int foCount, int fileBase) {
            this.foCount = foCount;
            this.paddingSize = Utilities.expPaddingSize(foCount + fileBase - 1);
            this.fileBase = fileBase;
            this.resName = resName;
            this.resExt = resExt;
        }
        
        /** getter for paddingSize */
        protected final int getPaddingSize() {
            return paddingSize;
        }
        
        /** getter for fileBase */
        protected final int getFileBase() {
            return fileBase;
        }
        
        /** setter for fileBase */
        protected final void setFileBase(int newBase) {
            fileBase = newBase;
        }
        
        /** getter for file count */
        protected final int getFileCount() {
            return foCount;
        }
        
        /** Assembly fileBase (e.g. 13) with name (e.g. JavaSrc) and ext (e.g. .java) into sbuffer.
         * Do not forget to take into account paddingSize.
         * Result could be e.g. JavaSrc0675.java, with paddingSize 4 and fileBase 675.
         */
        public void assemblyResourceString(StringBuffer sbuffer) {
            sbuffer.append(resName);
            Utilities.appendNDigits(getFileBase(), getPaddingSize(), sbuffer);
            sbuffer.append(resExt);
        }
    }

    /*
     public static void main(String[] args) throws Exception {
        XMLFSTest xmlfstest = new XMLFSTest("first test");
        xmlfstest.setUpFileObjects(500);
    }
    */    
}
