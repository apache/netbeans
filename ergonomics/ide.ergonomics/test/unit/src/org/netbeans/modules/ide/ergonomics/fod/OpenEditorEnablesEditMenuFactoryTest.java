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

package org.netbeans.modules.ide.ergonomics.fod;

import java.awt.EventQueue;
import javax.swing.JEditorPane;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.StyledDocument;
import junit.framework.Test;
import org.netbeans.api.actions.Openable;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataObject;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class OpenEditorEnablesEditMenuFactoryTest extends NbTestCase {
    private static final Logger LOG = Logger.getLogger(OpenEditorEnablesEditMenuFactoryTest.class.getName());

    public OpenEditorEnablesEditMenuFactoryTest(String name) {
        super(name);
    }

    public static Test suite() {
        LOG.fine("Creating suite");
        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().addTest(OpenEditorEnablesEditMenuFactoryTest.class).
            gui(true).
            clusters("ergonomics.*").
            clusters(".*").
            enableModules("ide[0-9]*", ".*").
            honorAutoloadEager(true)
        );
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        URL u = OpenEditorEnablesEditMenuFactoryTest.class.getResource("default.xml");
        assertNotNull("Default layer found", u);
        XMLFileSystem xml = new XMLFileSystem(u);
        FileObject fo = xml.findResource("Menu/Edit_hidden");
        assertNotNull("File found", fo);
        
        FileObject e = FileUtil.getConfigFile("Menu/Edit");
        assertNull("Default layer is on and Edit is hidden: " + Arrays.toString(FileUtil.getConfigFile("Menu").getChildren()), e);
    }

    public void testIfProjectFactoryInstalled() throws Exception {
        LOG.info("testIfProjectFactoryInstalled started");
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Mode m = WindowManager.getDefault().findMode("editor");
                assertNotNull("Editor mode found", m);
                TopComponent[] arr = WindowManager.getDefault().getOpenedTopComponents(m);
                assertEquals("No component is open", 0, arr.length);
            }
        });
        LOG.info("Back from AWT");
        FileObject folder = FileUtil.toFileObject(getWorkDir());
        FileObject txt = folder.createData("text.txt");
        LOG.log(Level.INFO, "file object found {0}", txt);
        final DataObject obj = DataObject.find(txt);
        LOG.log(Level.INFO, "data object found {0}", obj);
        Openable open = obj.getLookup().lookup(Openable.class);
        assertNotNull("Can be opened: " + obj, open);
        LOG.log(Level.INFO, "cookie found{0}", open);
        open.open();
        LOG.info("opened");

        final EditorCookie ec = obj.getLookup().lookup(EditorCookie.class);
        assertNotNull("EditorCookie found", ec);
        LOG.log(Level.INFO, "editor cookie: {0}", ec);
        StyledDocument doc = ec.openDocument();
        assertNotNull("Document loaded", doc);
        LOG.log(Level.INFO, "document {0}", doc);

        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                JEditorPane[] earr = ec.getOpenedPanes();
                LOG.log(Level.INFO, "panes {0}", Arrays.toString(earr));
                assertNotNull("panes found", earr);
                assertEquals("One item", 1, earr.length);
                Mode m = WindowManager.getDefault().findMode("editor");
                LOG.log(Level.INFO, "editor mode: {0}", m);
                assertNotNull("Editor mode found", m);
                TopComponent[] arr = WindowManager.getDefault().getOpenedTopComponents(m);
                LOG.log(Level.INFO, "opened editor components: {0}", Arrays.toString(arr));
                assertEquals("One component is open", 1, arr.length);
            }
        });
        LOG.info("waitFinished");
        FoDLayersProvider.getInstance().waitFinished();

        FileObject fo = null;
        for (int i = 0; i < 100; i++) {
            fo = FileUtil.getConfigFile("Menu/Edit");
            LOG.log(Level.INFO, "round #{0} found edit menu: {1}", new Object[]{i, fo});
            if (fo != null) break;
            Thread.sleep(100);
        }
        LOG.info("final check");
        assertNotNull(
            "Default layer is off and Edit is visible: " +
            Arrays.toString(FileUtil.getConfigFile("Menu").getChildren()),
            fo
        );
    }
}
