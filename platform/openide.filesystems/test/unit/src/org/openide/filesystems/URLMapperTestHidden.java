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

package org.openide.filesystems;


import java.io.*;
import java.net.URL;
import java.net.JarURLConnection;
import java.net.URLConnection;
import java.lang.Exception;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;

public class URLMapperTestHidden extends TestBaseHid {
    private FileObject root = null;

    protected String[] getResources (String testName) {
        return new String [] {
            "mynormaldir/mynormalfile.txt",
            "my ugly dir/my ugly file.txt"            
        };
    }

    protected void setUp() throws Exception {
        super.setUp();
        Repository.getDefault().addFileSystem(testedFS);
        root = testedFS.findResource(getResourcePrefix());                
    }

    protected void tearDown() throws Exception {
        Repository.getDefault().removeFileSystem(testedFS);        
        super.tearDown();
    }
    
    /** Creates new FileObjectTestHidden */
    public URLMapperTestHidden(String name) {
        super(name);
    }

    public void testIfReachable () throws Exception {        
        assertNotNull(root);
        implTestIfReachable(root);
                
        Enumeration en = root.getChildren(true);
        while (en.hasMoreElements()) {
            FileObject fileObject = (FileObject) en.nextElement();
            implTestIfReachable(fileObject);            
        }

    }
    
    public void testConversions () throws Exception {        
        assertNotNull(root);
        implTestConversions(root);
                

        Enumeration en = root.getChildren(true);
        while (en.hasMoreElements()) {
            FileObject fileObject = (FileObject) en.nextElement();
            implTestConversions(fileObject);            
        }

    }
    
    public void testForSlashes () throws Exception {        
        assertNotNull(root);
        implTestForSlashes(root);        
                
        Enumeration en = root.getChildren(true);
        while (en.hasMoreElements()) {
            FileObject fileObject = (FileObject) en.nextElement();
            implTestForSlashes(fileObject);                                
        }        
    }

    public void testForSlashes2 () throws Exception {        
        assertNotNull(root);
        if (testedFS.isReadOnly() || root.isReadOnly()) return;
                
        List testedFileObjects = new ArrayList(); 
        Enumeration en = root.getChildren(true);
        while (en.hasMoreElements()) {
            FileObject fileObject = (FileObject) en.nextElement();
            testedFileObjects.add(fileObject);
            implTestForSlashes(fileObject);                                
        }   
        
        FileObject[] directChildren = root.getChildren();
        for (int i = 0; i < directChildren.length; i++) {
            FileObject fo = directChildren[i];
            fo.delete();            
        }        

        for (int i = 0; i < testedFileObjects.size(); i++) {
            FileObject fo = (FileObject) testedFileObjects.get(i);
            assertFalse (fo.isValid());
            implTestForSlashes(fo);            
        }
    }
    
    public void testForSpaces () throws Exception {        
        assertNotNull(root);
        implTestForSpaces(root);
                
        Enumeration en = root.getChildren(true);
        while (en.hasMoreElements()) {
            FileObject fileObject = (FileObject) en.nextElement();
            implTestForSpaces(fileObject);                                            
        }        
    }
    

    private void implTestIfReachable(FileObject fo) throws Exception {
        URL urlFromMapper = URLMapper.findURL(fo, getURLType());        
        if (isNullURLExpected(urlFromMapper, fo)) return;
        
        assertNotNull(urlFromMapper);
        URLConnection fc = urlFromMapper.openConnection();
        
        
        if (fc instanceof JarURLConnection && fo.isFolder()) return; 
        InputStream ic = fc.getInputStream();
        try {
            assertNotNull(ic);
        } finally {
            if (ic != null) ic.close();
        }        
    }

    private boolean isNullURLExpected(URL urlFromMapper, FileObject fo) {
        boolean isNullExpected = false;
        if (urlFromMapper == null && getURLType() == URLMapper.EXTERNAL) {
            if (testedFS instanceof XMLFileSystem) {
                isNullExpected = true;   
            } else if (testedFS instanceof MultiFileSystem && FileUtil.toFile(fo) == null) {
                isNullExpected =  true;                 
            }
        }
        return isNullExpected;
    }

    private void implTestConversions (FileObject fo)  {        
        URL urlFromMapper = URLMapper.findURL(fo, getURLType());
        if (isNullURLExpected(urlFromMapper, fo)) return;
        
        assertNotNull(urlFromMapper);

        FileObject[] all = URLMapper.findFileObjects(urlFromMapper);
        List<FileObject> allList = Arrays.asList(all);
        assertTrue("found " + fo + " in " + allList + " from " + urlFromMapper, allList.contains(fo));
        assertEquals("findFileObject works too", fo, URLMapper.findFileObject(urlFromMapper));
    }

    
    protected int getURLType() {
        return URLMapper.EXTERNAL;
    }

    private void implTestForSlashes(FileObject fo) throws Exception{
        URL urlFromMapper = URLMapper.findURL(fo, getURLType());
        if (isNullURLExpected(urlFromMapper, fo)) return;
        
        assertNotNull(fo.getPath() + " from: " + fo.getFileSystem().toString(), urlFromMapper);
        String urlString = urlFromMapper.toExternalForm();


        /*test for last slash*/
        boolean isSlasLastIndex = (urlString.lastIndexOf('/') == (urlString.length()-1));
        assertTrue(urlString + ": last slash on unexpected position",(fo.isFolder()) ? isSlasLastIndex : !isSlasLastIndex);
    }

    private void implTestForSpaces(FileObject fo) {
        URL urlFromMapper = URLMapper.findURL(fo, getURLType());
        if (isNullURLExpected(urlFromMapper, fo)) return;
        
        assertNotNull(urlFromMapper);
        String urlString = urlFromMapper.toExternalForm();

        /*test for no spaces*/
        assertEquals(urlString + ": unexpected spaces",-1, urlFromMapper.toExternalForm().indexOf(' ') );
    }
}
