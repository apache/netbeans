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

package org.netbeans.core.startup.layers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.core.startup.layers.ModuleLayeredFileSystem;
import org.netbeans.core.startup.layers.SystemFileSystem;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Tulach
 */
public class AttributeChangeIsNotifiedTest extends NbTestCase {
    
    public AttributeChangeIsNotifiedTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        
        File u = new File(getWorkDir(), "userdir");
        File uc = new File(u, "config");
        uc.mkdirs();
        System.setProperty("netbeans.user", u.toString());
        File h = new File(getWorkDir(), "nb/installdir");
        new File(h, "config").mkdirs();
        System.setProperty("netbeans.home", h.toString());
        
        File f = FileUtil.toFile(FileUtil.getConfigRoot());
        
        assertEquals("Root is really on disk", uc, f);
        
    }

    protected ModuleLayeredFileSystem getTheLayer(SystemFileSystem sfs) {
        return sfs.getUserLayer();
    }

    public void testChangeOfAnAttributeInLayerIsFired() throws Exception {
        doChangeOfAnAttributeInLayerIsFired(getTheLayer((SystemFileSystem)FileUtil.getConfigRoot().getFileSystem()));
    }
    
    private void doChangeOfAnAttributeInLayerIsFired(ModuleLayeredFileSystem fs) throws Exception {
        File f1 = changeOfAnAttributeInLayerIsFiredgenerateLayer("Folder", "java.awt.List");
        File f2 = changeOfAnAttributeInLayerIsFiredgenerateLayer("Folder", "java.awt.Button");
        File f3 = changeOfAnAttributeInLayerIsFiredgenerateLayer("NoChange", "nochange");

        {
            List<URL> list = new ArrayList<URL>();
            list.add(Utilities.toURI(f1).toURL());
            list.add(Utilities.toURI(f3).toURL());
            fs.setURLs (list);
        }
        
        FileObject file = FileUtil.getConfigFile("Folder/empty.xml");
        assertNotNull("File found in layer", file);
        
        FSListener l = new FSListener();
        file.addFileChangeListener(l);
        
        FileObject nochange = FileUtil.getConfigFile("NoChange/empty.xml");
        assertNotNull("File found in layer", nochange);
        FSListener no = new FSListener();
        nochange.addFileChangeListener(no);
        
        assertAttr("The first value is list", file, "value", "java.awt.List");
        assertAttr("Imutable value is nochange", nochange, "value", "nochange");
        
        {
            List<URL> list = new ArrayList<URL>();
            list.add(Utilities.toURI(f2).toURL());
            list.add(Utilities.toURI(f3).toURL());
            fs.setURLs (list);
        }
        String v2 = (String) file.getAttribute("value");
        assertEquals("The second value is button", "java.awt.Button", v2);
        
        assertEquals("One change: " + l.events, 1, l.events.size());
        
        if (!(l.events.get(0) instanceof FileAttributeEvent)) {
            fail("Wrong event: " + l.events);
        }
        
        assertAttr("Imutable value is still nochange", nochange, "value", "nochange");
        assertEquals("No change in this attribute: "  + no.events, 0, no.events.size());
    }    
    
    private static void assertAttr(String msg, FileObject fo, String attr, String value) throws IOException {
        Object v = fo.getAttribute(attr);
        assertEquals(msg + "[" + fo + "]", value, v);
    }

    int cnt;
    private File changeOfAnAttributeInLayerIsFiredgenerateLayer(String folderName, String string) throws IOException {
        File f = new File(getWorkDir(), "layer" + (cnt++) + ".xml");
        FileWriter w = new FileWriter(f);
        w.write(
            "<filesystem>" +
            "<folder name='" + folderName + "'>" +
            "  <file name='empty.xml' >" +
            "    <attr name='value' stringvalue='" + string + "' />" +
            "  </file>" +
            "</folder>" +
            "</filesystem>"
        );
        w.close();
        return f;
    }
    
    private static class FSListener implements FileChangeListener {
        public List<FileEvent> events = new ArrayList<FileEvent>();
        public List<FileEvent> change = new ArrayList<FileEvent>();
        
        
        public void fileRenamed(FileRenameEvent fe) {
            events.add(fe);
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            events.add(fe);
        }

        public void fileFolderCreated(FileEvent fe) {
            events.add(fe);
        }

        public void fileDeleted(FileEvent fe) {
            events.add(fe);
        }

        public void fileDataCreated(FileEvent fe) {
            events.add(fe);
        }

        public void fileChanged(FileEvent fe) {
            change.add(fe);
        }
        
    }
}
