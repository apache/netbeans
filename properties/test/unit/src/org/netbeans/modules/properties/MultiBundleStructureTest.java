/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.properties;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import static org.junit.Assert.*;
import org.openide.filesystems.FileObject;

/**
 *
 * @author alex
 */
public class MultiBundleStructureTest extends NbTestCase {

    public MultiBundleStructureTest(String name) {
        super(name);
    }

    /**
     * Test of updateEntries method, of class MultiBundleStructure.
     */
    @Test
    public void testUpdateEntries() throws Exception {
        System.out.println("updateEntries");
        File propFile = new File(getWorkDir(), "foo.properties");
        propFile.createNewFile();
        DataObject propDO = DataObject.find(FileUtil.toFileObject(propFile));
        assertTrue(propDO instanceof PropertiesDataObject);
        PropertiesDataObject dataObject = (PropertiesDataObject) propDO;
        MultiBundleStructure instance = new MultiBundleStructure(dataObject);
        instance.updateEntries();
        assertEquals(instance.getEntryCount(), 1);
    }

    /**
     * Test of getNthEntry method, of class MultiBundleStructure.
     */
    @Test
    public void testGetNthEntry() throws Exception {
        System.out.println("getNthEntry");
        int index = 0;
        File propFile = new File(getWorkDir(), "foo.properties");
        propFile.createNewFile();
        DataObject propDO = DataObject.find(FileUtil.toFileObject(propFile));
        assertTrue(propDO instanceof PropertiesDataObject);
        PropertiesDataObject dataObject = (PropertiesDataObject) propDO;
        MultiBundleStructure instance = new MultiBundleStructure(dataObject);
        instance.updateEntries();
        PropertiesFileEntry result = instance.getNthEntry(index);
        assertEquals(dataObject.getName(),result.getName());
    }

    /**
     * Test of getEntryIndexByFileName method, of class MultiBundleStructure.
     */
    @Test
    public void testGetEntryIndexByFileName() throws Exception {
        System.out.println("getEntryIndexByFileName");
        String fileName = "foo";
        String ext = ".properties";
        File propFile = new File(getWorkDir(), fileName+ext);
        propFile.createNewFile();
        DataObject propDO = DataObject.find(FileUtil.toFileObject(propFile));
        assertTrue(propDO instanceof PropertiesDataObject);
        PropertiesDataObject dataObject = (PropertiesDataObject) propDO;
        MultiBundleStructure instance = new MultiBundleStructure(dataObject);
        instance.updateEntries();
        int expResult = 0;
        int result = instance.getEntryIndexByFileName(fileName);
        assertEquals(expResult, result);
    }

    /**
     * Test of getEntryByFileName method, of class MultiBundleStructure.
     */
    @Test
    public void testGetEntryByFileName() throws Exception {
        System.out.println("getEntryByFileName");
        String fileName1 = "foo";
        String fileName2 = "foo_ru";
        String ext = ".properties";
        File propFile = new File(getWorkDir(), fileName1+ext);
        propFile.createNewFile();
        File propFile2 = new File(getWorkDir(), fileName2+ext);
        propFile2.createNewFile();
        DataObject propDO = DataObject.find(FileUtil.toFileObject(propFile));
//        DataObject.find(FileUtil.toFileObject(propFile2));
        assertTrue(propDO instanceof PropertiesDataObject);
        PropertiesDataObject dataObject = (PropertiesDataObject) propDO;
        MultiBundleStructure instance = new MultiBundleStructure(dataObject);
        instance.updateEntries();
        PropertiesFileEntry result = instance.getEntryByFileName(fileName1);
        assertEquals(result.getFile().getName(), fileName1);
    }

    /**
     * Test of getEntryCount method, of class MultiBundleStructure.
     */
    @Test
    public void testGetEntryCount1() throws Exception{
        System.out.println("getEntryCount1");

        String fileName1 = "foo.properties";
        File propFile = new File(getWorkDir(), fileName1);
        propFile.createNewFile();
        String fileName2 = "foo_ru.properties";
        File propFile2 = new File(getWorkDir(), fileName2);
        propFile2.createNewFile();
        DataObject propDO = DataObject.find(FileUtil.toFileObject(propFile));
        assertTrue(propDO instanceof PropertiesDataObject);
        PropertiesDataObject dataObject = (PropertiesDataObject) propDO;
        MultiBundleStructure instance = new MultiBundleStructure(dataObject);
        instance.updateEntries();
        int expResult = 2;
        int result = instance.getEntryCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getEntryCount method, of class MultiBundleStructure.
     */
    @Test
    public void testGetEntryCount2() throws Exception{
        System.out.println("getEntryCount2");

        String fileName1 = "foo.properties";
        File propFile = new File(getWorkDir(), fileName1);
        propFile.createNewFile();
        String fileName2 = "foo_debug.properties";
        File propFile2 = new File(getWorkDir(), fileName2);
        propFile2.createNewFile();
        DataObject propDO = DataObject.find(FileUtil.toFileObject(propFile));
        assertTrue(propDO instanceof PropertiesDataObject);
        PropertiesDataObject dataObject = (PropertiesDataObject) propDO;
        MultiBundleStructure instance = new MultiBundleStructure(dataObject);
        instance.updateEntries();
        int expResult = 1;
        int result = instance.getEntryCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOpenSupport method, of class MultiBundleStructure.
     */
    @Test
    public void testGetOpenSupport() throws Exception{
        System.out.println("getOpenSupport");
        String fileName1 = "foo.properties";
        String fileName2 = "foo_ru.properties";
        File propFile = new File(getWorkDir(), fileName1);
        propFile.createNewFile();
        File propFile2 = new File(getWorkDir(), fileName2);
        propFile2.createNewFile();
        DataObject propDO1 = DataObject.find(FileUtil.toFileObject(propFile));
        DataObject propDO2 = DataObject.find(FileUtil.toFileObject(propFile));
        DataObject.find(FileUtil.toFileObject(propFile2));
        assertTrue(propDO1 instanceof PropertiesDataObject);
        PropertiesDataObject dataObject = (PropertiesDataObject) propDO1;
        MultiBundleStructure instance = (MultiBundleStructure) dataObject.getBundleStructure();
        MultiBundleStructure instance2 = (MultiBundleStructure) ((PropertiesDataObject)propDO2).getBundleStructure();
        //instances should be the same
        assertEquals(instance, instance2);
        instance.updateEntries();
        PropertiesOpen result = instance.getOpenSupport();
        assertNotNull(result);
    }

    /**
     * Test of getKeyCount method, of class MultiBundleStructure.
     */
    @Test
    public void testGetKeyCount() throws Exception {
        System.out.println("getKeyCount");
        File propFile = new File(getWorkDir(), "foo.properties");
        propFile.createNewFile();
        FileWriter wr = new FileWriter(propFile);
        wr.append("a=1\nb=2");
        wr.close();
        DataObject propDO = DataObject.find(FileUtil.toFileObject(propFile));
        assertTrue(propDO instanceof PropertiesDataObject);
        PropertiesDataObject dataObject = (PropertiesDataObject) propDO;
        MultiBundleStructure instance = new MultiBundleStructure(dataObject);
        instance.updateEntries();
        int expResult = 2;
        int result = instance.getKeyCount();
        assertEquals(expResult, result);
    }

    @Test
    public void test_200108_fix() throws Exception {
        System.out.println("deleting prop file with more then 2 locale");
        List<FileObject> fileObjects = new ArrayList<FileObject>();
        clearWorkDir();

        File folder = new File(getWorkDir(), "properties");
        folder.mkdir();
        FileObject folderObject = FileUtil.toFileObject(folder);

        File propFile = new File(folder, "boo.properties");
        propFile.createNewFile();

        FileObject toFileObject = FileUtil.toFileObject(propFile);
        fileObjects.add(toFileObject);
        PropertiesDataObject dataObject = (PropertiesDataObject) (DataObject.find(toFileObject));

        MultiBundleStructure instance = new MultiBundleStructure(dataObject);
        dataObject.setBundleStructure(instance);
        instance.updateEntries();
        Util.createLocaleFile(dataObject, "ar_EG", true);
        Util.createLocaleFile(dataObject, "ar_JO", true);
        int expResult = 3;
        int result = instance.getEntryCount();
        assertEquals(expResult, result);

        folderObject.delete();

        instance.updateEntries();
        expResult = 0;
        result = instance.getEntryCount();
        assertEquals(expResult, result);
    }
}
