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

package org.netbeans.modules.subversion.client.commands;

import org.netbeans.modules.subversion.client.AbstractCommandTestCase;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.subversion.client.SvnClientFactory;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public class PropertyTestHidden extends AbstractCommandTestCase {
    
    // XXX test ignored patterns
    
    public PropertyTestHidden(String testName) throws Exception {
        super(testName);
    }
            
    public void testPropertyGetFileNonProp() throws Exception {
        testPropertyGetFileNoProp("file");
    }

    public void testPropertyGetFileWithAtSignNoProp() throws Exception {
        testPropertyGetFileNoProp("@file");
        testPropertyGetFileNoProp("fi@le");
        testPropertyGetFileNoProp("file@");
    }

    public void testPropertyGetFileInDirNoProp() throws Exception {
        testPropertyGetFileNoProp("folder/file");
    }

    public void testPropertyGetFileWithAtSignInDirNoProp() throws Exception {
        testPropertyGetFileNoProp("folder/@file");
        testPropertyGetFileNoProp("folder/fi@le");
        testPropertyGetFileNoProp("folder/file@");
    }

    private void testPropertyGetFileNoProp(String filePath) throws Exception {
        createAndCommitParentFolders(filePath);
        File file = createFile(filePath);
        add(file);
        commit(file);
        
        ISVNClientAdapter c = getNbClient();        
        
        ISVNProperty p = c.propertyGet(file, "dil");
        assertNull(p);        
        p = c.propertyGet(getFileUrl(file), "dil");
        assertNull(p);
    }            
    
    public void testPropertyGetUrl() throws Exception {
        testPropertyGetUrl("file");
    }

    public void testPropertyGetUrlWithAtSign() throws Exception {
        testPropertyGetUrl("@file");
        testPropertyGetUrl("fi@le");
        testPropertyGetUrl("file@");
    }

    public void testPropertyGetUrlInDir() throws Exception {
        testPropertyGetUrl("folder/file");
    }

    public void testPropertyGetUrlWithAtSignInDir() throws Exception {
        testPropertyGetUrl("folder/@file");
        testPropertyGetUrl("folder/fi@le");
        testPropertyGetUrl("folder/file@");
    }

    private void testPropertyGetUrl(String filePath) throws Exception {
        createAndCommitParentFolders(filePath);
        File file = createFile(filePath);
        add(file);
                
        ISVNClientAdapter c = getNbClient();        
        c.propertySet(file, "p1", "v1", false);

        commit(file);
        
        assertPropertyStatus(SVNStatusKind.NORMAL, file);

        assertProperty(c, getFileUrl(file), "p1", "v1");                
    }            
    
    public void testPropertyListUrl() throws Exception {
        testPropertyListUrl("file");
    }

    public void testPropertyListUrlWithAtSign() throws Exception {
        testPropertyListUrl("@file");
        testPropertyListUrl("fi@le");
        testPropertyListUrl("file@");
    }

    public void testPropertyListUrlInDir() throws Exception {
        testPropertyListUrl("folder/file");
    }

    public void testPropertyListUrlWithAtSignInDir() throws Exception {
        testPropertyListUrl("folder/@file");
        testPropertyListUrl("folder/fi@le");
        testPropertyListUrl("folder/file@");
    }

    private void testPropertyListUrl(String filePath) throws Exception {
        createAndCommitParentFolders(filePath);
        File file = createFile(filePath);
        add(file);
        
        ISVNClientAdapter c = getNbClient();        
        c.propertySet(file, "p1", "v1", false);
        c.propertySet(file, "p2", "v2", false);
        c.propertySet(file, "p3", "v3", false);
        commit(file);
        
        assertPropertyStatus(SVNStatusKind.NORMAL, file);

        ISVNProperty[] props = c.getProperties(getFileUrl(file));
        Map<String, ISVNProperty> propMap = new HashMap<String, ISVNProperty>();
        for (ISVNProperty p : props) {
            propMap.put(p.getName(), p);                    
        }
        assertEquals(3, propMap.size());
        assertProperty("p1", "v1", propMap);        
        assertProperty("p2", "v2", propMap);        
        assertProperty("p3", "v3", propMap);        
    }          
    
    public void testPropertySetGetDel() throws Exception {
        testPropertySetGetDel("file");
    }

    public void testPropertySetGetDelWithAtSign() throws Exception {
        testPropertySetGetDel("@file");
        testPropertySetGetDel("fi@le");
        testPropertySetGetDel("file@");
    }

    public void testPropertySetGetDelInDir() throws Exception {
        testPropertySetGetDel("folder/file");
    }

    public void testPropertySetGetDelWithAtSignInDir() throws Exception {
        testPropertySetGetDel("folder/@file");
        testPropertySetGetDel("folder/fi@le");
        testPropertySetGetDel("folder/file@");
    }

    private void testPropertySetGetDel(String filePath) throws Exception {
        createAndCommitParentFolders(filePath);
        File file = createFile(filePath);
        add(file);
        commit(file);
        
        ISVNClientAdapter c = getNbClient();        
        c.propertySet(file, "p1", "v1", false);

        assertNotifiedFiles(file);
        
        assertPropertyStatus(SVNStatusKind.MODIFIED, file);

        assertProperty(c, file, "p1", "v1");
                
        clearNotifiedFiles();
        c.propertyDel(file, "p1", false);
        assertPropertyStatus(SVNStatusKind.NONE, file);
        assertNotifiedFiles(file);
    }            
    
    public void testPropertyListFile() throws Exception {
        testPropertyListFile("file");
    }

    public void testPropertyListFileWithAtSign() throws Exception {
        testPropertyListFile("@file");
        testPropertyListFile("fi@le");
        testPropertyListFile("file@");
    }

    public void testPropertyListFileInDir() throws Exception {
        testPropertyListFile("folder/file");
    }

    public void testPropertyListFileWithAtSignInDir() throws Exception {
        testPropertyListFile("folder/@file");
        testPropertyListFile("folder/fi@le");
        testPropertyListFile("folder/file@");
    }

    private void testPropertyListFile(String filePath) throws Exception {
        createAndCommitParentFolders(filePath);
        File file = createFile(filePath);
        add(file);
        commit(file);
        
        ISVNClientAdapter c = getNbClient();        
        c.propertySet(file, "p1", "v1", false);
        c.propertySet(file, "p2", "v2", false);
        c.propertySet(file, "p3", "v3", false);
        assertNotifiedFiles(file);
        
        assertPropertyStatus(SVNStatusKind.MODIFIED, file);

        ISVNProperty[] props = c.getProperties(file);
        Map<String, ISVNProperty> propMap = new HashMap<String, ISVNProperty>();
        for (ISVNProperty p : props) {
            propMap.put(p.getName(), p);                    
        }
        assertEquals(3, propMap.size());
        assertProperty("p1", "v1", propMap);        
        assertProperty("p2", "v2", propMap);        
        assertProperty("p3", "v3", propMap);        
    }            
            
    public void testPropertySetGetDelRecursivelly() throws Exception {                                                
        File folder = createFolder("folder");        
        File file = createFolder(folder, "file");        
        File folder1 = createFolder(folder, "folder1");        
        File file1 = createFolder(folder1, "file1");        
        
        add(folder);
        add(file);
        add(folder1);
        add(file1);
        commit(getWC());
        
        ISVNClientAdapter c = getNbClient();        
        c.propertySet(folder, "p1", "v1", true);
        assertNotifiedFiles(folder, file, folder1, file1);

        assertPropertyStatus(SVNStatusKind.MODIFIED, file);
        assertPropertyStatus(SVNStatusKind.MODIFIED, folder);
        assertPropertyStatus(SVNStatusKind.MODIFIED, folder1);
        assertPropertyStatus(SVNStatusKind.MODIFIED, file1);
        
        assertProperty(c, file, "p1", "v1");
        assertProperty(c, folder, "p1", "v1");
        assertProperty(c, folder1, "p1", "v1");
        assertProperty(c, file1, "p1", "v1");                
        
        clearNotifiedFiles();
        c.propertyDel(folder, "p1", true);
        assertPropertyStatus(SVNStatusKind.NONE, folder);
        assertPropertyStatus(SVNStatusKind.NONE, file);
        assertPropertyStatus(SVNStatusKind.NONE, folder1);
        assertPropertyStatus(SVNStatusKind.NONE, file1);
        assertNotifiedFiles(folder, file, folder1, file1);
    }    
                
    public void testPropertySetGetDelFile() throws Exception {                                                
        File file = createFile("file");        
        add(file);
        commit(file);
        File prop = createFile("prop");
        write(prop, 2);
        
        ISVNClientAdapter c = getNbClient();        
        c.propertySet(file, "p1", prop, false);
        assertNotifiedFiles(file);
        
        assertPropertyStatus(SVNStatusKind.MODIFIED, file);

        assertProperty(c, file, "p1", new byte[] {2});
        
        clearNotifiedFiles();
        c.propertyDel(file, "p1", true);
        assertPropertyStatus(SVNStatusKind.NONE, file);        
        assertNotifiedFiles(file);
    }            
            
    public void testPropertySetGetDelFileRecursivelly() throws Exception {                                                
        File folder = createFolder("folder");        
        File file = createFolder(folder, "file");        
        File folder1 = createFolder(folder, "folder1");        
        File file1 = createFolder(folder1, "file1");        
        
        add(folder);
        add(file);
        add(folder1);
        add(file1);
        commit(getWC());

        File prop = createFile("prop");
        write(prop, 2);
        
        ISVNClientAdapter c = getNbClient();        
        c.propertySet(folder, "p1", prop, true);
        assertNotifiedFiles(folder, file, folder1, file1);
        
        assertPropertyStatus(SVNStatusKind.MODIFIED, file);
        assertPropertyStatus(SVNStatusKind.MODIFIED, folder);
        assertPropertyStatus(SVNStatusKind.MODIFIED, folder1);
        assertPropertyStatus(SVNStatusKind.MODIFIED, file1);
        
        assertProperty(c, file, "p1", new byte[] {2});
        assertProperty(c, folder, "p1", new byte[] {2});
        assertProperty(c, folder1, "p1", new byte[] {2});
        assertProperty(c, file1, "p1", new byte[] {2});
        
        clearNotifiedFiles();
        c.propertyDel(folder, "p1", true);
        assertPropertyStatus(SVNStatusKind.NONE, folder);
        assertPropertyStatus(SVNStatusKind.NONE, file);
        assertPropertyStatus(SVNStatusKind.NONE, folder1);
        assertPropertyStatus(SVNStatusKind.NONE, file1);    
        assertNotifiedFiles(folder, file, folder1, file1);
    }
    
    public void testPropertySetNonRecursivelly() throws Exception {                                                
        File folder = createFolder("folder");        
        File file = createFolder(folder, "file");        
        File folder1 = createFolder(folder, "folder1");        
        File file1 = createFolder(folder1, "file1");        
        
        add(folder);
        add(file);
        add(folder1);
        add(file1);
        commit(getWC());
        
        ISVNClientAdapter c = getNbClient();        
        c.propertySet(folder, "p1", "v1", false);
        assertNotifiedFiles(folder);
        
        assertPropertyStatus(SVNStatusKind.MODIFIED, folder);
        assertPropertyStatus(SVNStatusKind.NONE, file);
        assertPropertyStatus(SVNStatusKind.NONE, folder1);
        assertPropertyStatus(SVNStatusKind.NONE, file1);
        
        assertProperty(c, folder, "p1", "v1");        
    }
    
    public void testPropertyDelNonRecursivelly() throws Exception {                                                
        File folder = createFolder("folder");        
        File file = createFolder(folder, "file");        
        File folder1 = createFolder(folder, "folder1");        
        File file1 = createFolder(folder1, "file1");        
        
        add(folder);
        add(file);
        add(folder1);
        add(file1);
        commit(getWC());

        ISVNClientAdapter c = getNbClient();        
        c.propertySet(folder, "p1", "v1", true);
        assertNotifiedFiles(folder, file, folder1, file1);
        
        assertPropertyStatus(SVNStatusKind.MODIFIED, file);
        assertPropertyStatus(SVNStatusKind.MODIFIED, folder);
        assertPropertyStatus(SVNStatusKind.MODIFIED, folder1);
        assertPropertyStatus(SVNStatusKind.MODIFIED, file1);
        
        assertProperty(c, file, "p1", "v1");
        assertProperty(c, folder, "p1", "v1");
        assertProperty(c, folder1, "p1", "v1");
        assertProperty(c, file1, "p1", "v1");
        
        clearNotifiedFiles();
        c.propertyDel(folder, "p1", false);
        assertPropertyStatus(SVNStatusKind.NONE, folder);
        assertPropertyStatus(SVNStatusKind.MODIFIED, file);
        assertPropertyStatus(SVNStatusKind.MODIFIED, folder1);
        assertPropertyStatus(SVNStatusKind.MODIFIED, file1);        
        assertNotifiedFiles(folder);
    }
    
    public void testPropertySetFileNonRecursivelly() throws Exception {                                                
        File folder = createFolder("folder");        
        File file = createFolder(folder, "file");        
        File folder1 = createFolder(folder, "folder1");        
        File file1 = createFolder(folder1, "file1");        
        
        add(folder);
        add(file);
        add(folder1);
        add(file1);
        commit(getWC());

        File prop = createFile("prop");
        write(prop, 2);
        
        ISVNClientAdapter c = getNbClient();        
        c.propertySet(folder, "p1", prop, false);
        assertNotifiedFiles(folder);
        
        assertPropertyStatus(SVNStatusKind.MODIFIED, folder);
        assertPropertyStatus(SVNStatusKind.NONE, file);
        assertPropertyStatus(SVNStatusKind.NONE, folder1);
        assertPropertyStatus(SVNStatusKind.NONE, file1);
        
        assertProperty(c, folder, "p1", new byte[] {2});        
    }
    
    public void testPropertyDelFileNonRecursivelly() throws Exception {                                                
        File folder = createFolder("folder");        
        File file = createFolder(folder, "file");        
        File folder1 = createFolder(folder, "folder1");        
        File file1 = createFolder(folder1, "file1");        
        
        add(folder);
        add(file);
        add(folder1);
        add(file1);
        commit(getWC());

        File prop = createFile("prop");
        write(prop, 2);
        
        ISVNClientAdapter c = getNbClient();        
        c.propertySet(folder, "p1", prop, true);
        assertNotifiedFiles(folder, file, folder1, file1);
        
        assertPropertyStatus(SVNStatusKind.MODIFIED, file);
        assertPropertyStatus(SVNStatusKind.MODIFIED, folder);
        assertPropertyStatus(SVNStatusKind.MODIFIED, folder1);
        assertPropertyStatus(SVNStatusKind.MODIFIED, file1);
        
        assertProperty(c, file, "p1", new byte[] {2});
        assertProperty(c, folder, "p1", new byte[] {2});
        assertProperty(c, folder1, "p1", new byte[] {2});
        assertProperty(c, file1, "p1", new byte[] {2});
        
        clearNotifiedFiles();
        c.propertyDel(folder, "p1", false);
        assertPropertyStatus(SVNStatusKind.NONE, folder);
        assertPropertyStatus(SVNStatusKind.MODIFIED, file);
        assertPropertyStatus(SVNStatusKind.MODIFIED, folder1);
        assertPropertyStatus(SVNStatusKind.MODIFIED, file1);        
        assertNotifiedFiles(folder);
    }
    
    public void testKWPropertySet () throws Exception {                                                
        File folder = createFolder("folder");        
        File file1 = createFile(folder, "file1");        
        
        add(folder);
        add(file1);
        commit(getWC());
 
        ISVNClientAdapter c = getNbClient();        
        c.propertySet(file1, "svn:keywords", "Id", false);
        assertNotifiedFiles(file1);
        
        assertPropertyStatus(SVNStatusKind.MODIFIED, file1);
        
        // add property for new file
        File file2 = createFile(folder, "file2");
        assertStatus(SVNStatusKind.UNVERSIONED, file2);
        c.addFile(file2);
        assertStatus(SVNStatusKind.ADDED, file2);
        
        c.propertySet(file2, "svn:keywords", "Id", false);
        assertNotifiedFiles(file2);
        
        assertPropertyStatus(SVNStatusKind.MODIFIED, file2);
        
    }
    
    public void testKWAutoPropSet () throws Exception {                                                
        File folder = createFolder("folder");        
        
        add(folder);
        commit(getWC());

        write(new File(SvnConfigFiles.getNBConfigPath(), "config"), "[miscellany]\n"
                + "enable-auto-props = yes\n"
                + "[auto-props]\n"
                + "*.java = svn:keywords=Id\n"
                + "");
        
        ISVNClientAdapter c = getNbClient();        
        File file1 = createFile(folder, "file1.java");
        assertStatus(SVNStatusKind.UNVERSIONED, file1);
        c.addFile(file1);
        assertStatus(SVNStatusKind.ADDED, file1);
        
        c.commit(new File[] { file1 }, "message", true);
    }

    private void assertProperty(String name, String value, Map<String, ISVNProperty> propMap) {
        ISVNProperty p = propMap.get(name);
        assertNotNull(p);
        assertEquals(value, p.getValue());
        assertEquals(value, new String(p.getData()));
    }
    
    private void assertProperty(ISVNClientAdapter c, File file, String prop, String val) throws SVNClientException {        
        ISVNProperty p = c.propertyGet(file, prop);
        assertEquals(val, new String(p.getData()));        
    }
    
    private void assertProperty(ISVNClientAdapter c, SVNUrl url, String prop, String val) throws SVNClientException {        
        ISVNProperty p = c.propertyGet(url, prop);
        assertEquals(val, new String(p.getData()));        
    }

    private void assertProperty(ISVNClientAdapter c, File file, String prop, byte[] data) throws SVNClientException {
        ISVNProperty p = c.propertyGet(file, prop);
        assertNotNull(p);
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], p.getData()[i]);                    
        }        
    }    
    
}
