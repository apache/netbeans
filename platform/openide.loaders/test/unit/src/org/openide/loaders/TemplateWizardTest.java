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

package org.openide.loaders;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
/** Checks the testable behaviour of TemplateWizard
 * @author Jaroslav Tulach, Jiri Rechtacek
 */
public class TemplateWizardTest extends NbTestCase {
    
    public TemplateWizardTest (String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        // set up user directory
        clearWorkDir();
        File wd = new File(getWorkDir(), "config");
        wd.mkdirs();
        System.setProperty("netbeans.user", wd.toString());
        FileObject fo = FileUtil.getConfigRoot ();
        FileUtil.createFolder (fo, "Templates");
    }
    
    protected void tearDown() {
        System.getProperties().remove("netbeans.user");
    }

    /** Does getIterator honours DataObject's cookies?
     */
    public void testGetIteratorHonoursDataObjectsCookies () throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        DataObject obj;
        Loader l = Loader.findObject (Loader.class, true);
        try {
            AddLoaderManuallyHid.addRemoveLoader (l, true);
            obj = DataObject.find (fs.getRoot ());
        } finally {
            AddLoaderManuallyHid.addRemoveLoader (l, false);
        }
        
        TemplateWizard.Iterator it = TemplateWizard.getIterator (obj);
        
        assertEquals ("Iterator obtained from the object's cookie", obj, it);
    }
    
    public void testIteratorBridge() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject fo = fs.getRoot().createData("x");
        final FileObject a = fs.getRoot().createData("a");
        final FileObject b = fs.getRoot().createData("b");
        final FileObject c = fs.getRoot().createData("c");
        final FileObject d = fs.getRoot().createData("d");
        fo.setAttribute("instantiatingIterator", new WizardDescriptor.InstantiatingIterator() {
            public Set instantiate() throws IOException {
                return new LinkedHashSet(Arrays.asList(new FileObject[] {
                    d,
                    c,
                    a,
                    b,
                }));
            }
            public void removeChangeListener(ChangeListener l) {}
            public void addChangeListener(ChangeListener l) {}
            public void uninitialize(WizardDescriptor wizard) {}
            public void initialize(WizardDescriptor wizard) {}
            public void previousPanel() {}
            public void nextPanel() {}
            public String name() {return null;}
            public boolean hasPrevious() {return false;}
            public boolean hasNext() {return false;}
            public WizardDescriptor.Panel current() {return null;}
        });
        System.out.println("natural order:" + new HashSet(Arrays.asList(new DataObject[] {
            DataObject.find(d),
            DataObject.find(c),
            DataObject.find(a),
            DataObject.find(b),
        })));
        assertEquals("order preserved (#64760)", Arrays.asList(new DataObject[] {
            DataObject.find(d),
            DataObject.find(c),
            DataObject.find(a),
            DataObject.find(b),
        }), new ArrayList(TemplateWizard.getIterator(DataObject.find(fo)).instantiate(new TemplateWizard())));
    }
    
    private static class DO extends DataFolder implements TemplateWizard.Iterator {
        public DO (FileObject fo) throws DataObjectExistsException {
            super (fo);

            getCookieSet ().add (this);
        }

        //
        // Dummy implementation of wizard iterator
        //

        public void addChangeListener(ChangeListener l) {
        }
        public WizardDescriptor.Panel<WizardDescriptor> current() {
            return null;
        }
        public boolean hasNext() {
            return false;
        }
        public boolean hasPrevious() {
            return false;
        }
        public void initialize(TemplateWizard wiz) {
        }
        public Set instantiate(TemplateWizard wiz) throws IOException {
            throw new IOException ();
        }
        public String name() {
            return "";
        }
        public void nextPanel() {
        }
        public void previousPanel() {
        }
        public void removeChangeListener(ChangeListener l) {
        }
        public void uninitialize(TemplateWizard wiz) {
        }
    } // end of DO
    private static class Loader extends UniFileLoader {
        public Loader () {
            super (DO.class.getName ());
        }

        protected FileObject findPrimaryFile (FileObject fo) {
            if (fo.isFolder ()) {
                return fo;
            } else {
                return null;
            }
        }

        protected MultiDataObject createMultiObject (FileObject fo) throws IOException {
            return new DO (fo);
        }
    } // end of Loader
    
    public void testCanCallInstantiateFromNonEventDispatchThread() throws Exception {
        TemplateWizard tw = new TemplateWizard();
        MockServices.setServices(CancelDD.class);
        Set<DataObject> ret = tw.instantiate();
        assertNull("Selection was cancelled", ret);
    }

    @RandomlyFails // NB-Core-Build #1639 (NPE in SunGraphics2D.addRenderingHints from HtmlLabelUI.calcPreferredSize);
                   // NB-Core-Build #1644 (CCE: javax.swing.KeyStroke from TreeMap.compare in JTextField.<init>)
    public void testNextOnIterImpl () throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                doNextOnIterImpl (false);
            }
        });
    }

    @RandomlyFails // NB-Core-Build #1429
    public void testNextOnIterImplWithNotification () throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                doNextOnIterImpl (true);
            }
        });
    }
    
    /**
     * When creating file on Config FS, the resulting object must be created
     * on the config FS and not on the underlying OS filesystem visible through
     * FileSystem APIs.
     */
    public void testWizardConfigLocation() throws Exception {
        final TemplateWizard tw = new TemplateWizard();
        FileObject cf = FileUtil.getConfigRoot().createFolder("target"); // NOI18N
        DataFolder target = DataFolder.findFolder(cf);
        tw.setTargetFolder(target);
        
        // folder does not have a default action -> instantiate does not open an editor
        FileObject f = FileUtil.getConfigFile("Templates").createFolder("bubu"); // NOI18N
        tw.setTemplate(DataObject.find(f));

        tw.initialize();
        final TemplateWizardIterImpl iter = tw.getIterImpl ();
        iter.first();
        // initialize the target
        iter.nextPanel();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                // this is what is called by UI on Finish
                tw.updateState();
                iter.getIterator().current().storeSettings(tw);
            }
        });
        tw.setValue(TemplateWizard.FINISH_OPTION);
        Set<DataObject> result = iter.instantiate();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        FileObject tf = result.iterator().next().getPrimaryFile();
        
        // check that the object is really created on config fs
        assertTrue(FileUtil.isParentOf(cf, tf));
    }

    /**
     * Test deferred TargetFolder creation
     */
    public void testDeferredTargetFolderCreation() throws Exception {
        final TemplateWizard tw = new TemplateWizard();
        final String TARGET = "deferredTarget"; // NOI18N
        tw.setTargetFolderLazy(() -> {
            FileObject fo = null;
            try {
                fo = FileUtil.getConfigRoot().createFolder(TARGET);
            } catch(IOException ex) {
                assertTrue("createFolder(TARGET) exception", false);
            }
            return DataFolder.findFolder(fo);
        });
        assertNull(FileUtil.getConfigRoot().getFileObject(TARGET));
        // should lazily create target
        DataFolder targetFolder = tw.getTargetFolder();
        FileObject foNew = FileUtil.getConfigRoot().getFileObject(TARGET);
        assertNotNull(foNew);
        assertFalse(foNew.isVirtual());
        assertEquals(targetFolder.getName(), TARGET);
    }

    /**
     * Test *no* deferred TargetFolder creation
     */
    public void testNoDeferredTargetFolderCreation() throws Exception {
        final TemplateWizard tw = new TemplateWizard();
        final String TARGET1 = "target1"; // NOI18N
        final String TARGET2 = "target2"; // NOI18N

        FileObject cf = FileUtil.getConfigRoot().createFolder(TARGET1);
        DataFolder target = DataFolder.findFolder(cf);
        tw.setTargetFolder(target);
        tw.setTargetFolderLazy(() -> {
            FileObject fo = null;
            try {
                fo = FileUtil.getConfigRoot().createFolder(TARGET2);
            } catch(IOException ex) {
                assertTrue("createFolder(TARGET2) exception", false);
            }
            return DataFolder.findFolder(fo);
        });
        DataFolder targetFolder = tw.getTargetFolder();

        // TARGET2 should not be created
        assertNull(FileUtil.getConfigRoot().getFileObject(TARGET2));
    }
    
    private void doNextOnIterImpl (boolean notify) {
        TemplateWizard wizard = new TemplateWizard ();
        wizard.initialize ();
        TemplateWizardIterImpl iter = wizard.getIterImpl ();
        assertEquals ("IterImpl returns template chooser.", wizard.templateChooser (), iter.current ());
        final WizardDescriptor.Panel[] arr = {new P(1), new P(2)};
        class I extends WizardDescriptor.ArrayIterator<WizardDescriptor> implements TemplateWizard.Iterator {
            public I () {
                super (arr);
            }
            public Set<DataObject> instantiate (TemplateWizard wiz) throws IOException {
                throw new IOException ();
            }
            public void initialize(TemplateWizard wiz) {}
            public void uninitialize(TemplateWizard wiz) {}
        }
        
        I newIter = new I ();
        WizardDescriptor.Panel oldPanel = iter.current ();
        iter.setIterator (newIter, notify);
        iter.nextPanel ();
        assertEquals ("IterImpl returns the first panel of newly delegated iterator, ", arr[0], iter.current ());
        iter.previousPanel ();
        assertEquals ("IterImpl returns the first panel of old iterator on previous, ", oldPanel, iter.current ());
    }
    
    public static class P implements WizardDescriptor.Panel {
        int index;
        public P (int i) {
            index = i;
        }
        
        public void removeChangeListener (ChangeListener l) {
        }

        public void addChangeListener (ChangeListener l) {
        }

        public void storeSettings (Object settings) {
        }

        public void readSettings (Object settings) {
        }

        public boolean isValid () {
            return true;
        }

        public HelpCtx getHelp () {
            return null;
        }

        public Component getComponent () {
            return new JPanel ();
        }
        
        public String toString () {
            return Integer.toString (index);
        }
        
    }

    public static final class CancelDD extends DialogDisplayer {
        @Override
        public Object notify(NotifyDescriptor descriptor) {
            return NotifyDescriptor.CANCEL_OPTION;
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}


