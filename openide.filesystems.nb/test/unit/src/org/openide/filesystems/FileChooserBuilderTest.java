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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.openide.filesystems;

import java.awt.Component;
import java.awt.Dialog;
import javax.swing.UIManager;
import javax.swing.JRootPane;
import java.util.concurrent.atomic.AtomicReference;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;
import java.awt.Container;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.RootPaneContainer;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.netbeans.junit.NbTestCase;
import org.openide.util.RequestProcessor;
import static org.junit.Assert.*;
import org.netbeans.junit.RandomlyFails;

/**
 * @author tim
 */
public class FileChooserBuilderTest extends NbTestCase {

    public FileChooserBuilderTest(String name) {
        super(name);
    }

    /**
     * Test of setDirectoriesOnly method, of class FileChooserBuilder.
     */
    public void testSetDirectoriesOnly() {
        FileChooserBuilder instance = new FileChooserBuilder("x");
        boolean dirsOnly = instance.createFileChooser().getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY;
        assertFalse(dirsOnly);
        instance.setDirectoriesOnly(true);
        dirsOnly = instance.createFileChooser().getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY;
        assertTrue(dirsOnly);
    }

    /**
     * Test of setFilesOnly method, of class FileChooserBuilder.
     */
    public void testSetFilesOnly() {
        FileChooserBuilder instance = new FileChooserBuilder("y");
        boolean filesOnly = instance.createFileChooser().getFileSelectionMode() == JFileChooser.FILES_ONLY;
        assertFalse(filesOnly);
        instance.setFilesOnly(true);
        filesOnly = instance.createFileChooser().getFileSelectionMode() == JFileChooser.FILES_ONLY;
        assertTrue(filesOnly);
    }

    /**
     * Test of setTitle method, of class FileChooserBuilder.
     */
    public void testSetTitle() {
        FileChooserBuilder instance = new FileChooserBuilder("a");
        assertNull(instance.createFileChooser().getDialogTitle());
        instance.setTitle("foo");
        assertEquals("foo", instance.createFileChooser().getDialogTitle());
    }

    /**
     * Test of setApproveText method, of class FileChooserBuilder.
     */
    public void testSetApproveText() {
        FileChooserBuilder instance = new FileChooserBuilder("b");
        assertNull(instance.createFileChooser().getDialogTitle());
        instance.setApproveText("bar");
        assertEquals("bar", instance.createFileChooser().getApproveButtonText());
    }

    /**
     * Test of setFileFilter method, of class FileChooserBuilder.
     */
    public void testSetFileFilter() {
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File f) {
                return true;
            }

            @Override
            public String getDescription() {
                return "X";
            }
        };
        FileChooserBuilder instance = new FileChooserBuilder("c");
        instance.setFileFilter(filter);
        assertEquals(filter, instance.createFileChooser().getFileFilter());
    }

    /**
     * Test of setDefaultWorkingDirectory method, of class FileChooserBuilder.
     */
    public void testSetDefaultWorkingDirectory() throws IOException {
        FileChooserBuilder instance = new FileChooserBuilder("d");
        File dir = getWorkDir();
        assertTrue("tmpdir is not sane", dir.exists() && dir.isDirectory());
        instance.setDefaultWorkingDirectory(dir);
        assertEquals(dir, instance.createFileChooser().getCurrentDirectory());
    }

    /**
     * Test of setFileHiding method, of class FileChooserBuilder.
     */
    public void testSetFileHiding() {
        FileChooserBuilder instance = new FileChooserBuilder("e");
        assertFalse(instance.createFileChooser().isFileHidingEnabled());
        instance.setFileHiding(true);
        assertTrue(instance.createFileChooser().isFileHidingEnabled());
    }

    /**
     * Test of setControlButtonsAreShown method, of class FileChooserBuilder.
     */
    public void testSetControlButtonsAreShown() {
        FileChooserBuilder instance = new FileChooserBuilder("f");
        assertTrue(instance.createFileChooser().getControlButtonsAreShown());
        instance.setControlButtonsAreShown(false);
        assertFalse(instance.createFileChooser().getControlButtonsAreShown());
    }

    /**
     * Test of setAccessibleDescription method, of class FileChooserBuilder.
     */
    public void testSetAccessibleDescription() {
        FileChooserBuilder instance = new FileChooserBuilder("g");
        String desc = "desc";
        instance.setAccessibleDescription(desc);
        assertEquals(desc, instance.createFileChooser().getAccessibleContext().getAccessibleDescription());
    }

    /**
     * Test of createFileChooser method, of class FileChooserBuilder.
     */
    public void testCreateFileChooser() {
        FileChooserBuilder instance = new FileChooserBuilder("h");
        assertNotNull(instance.createFileChooser());
    }

    public void testSetSelectionApprover() throws Exception {
        FileChooserBuilder instance = new FileChooserBuilder("i");
        File tmp = new File(System.getProperty("java.io.tmpdir"));
        assertTrue ("Environment is insane", tmp.exists() && tmp.isDirectory());
        File sel = new File("tmp" + System.currentTimeMillis());
        if (!sel.exists()) {
            assertTrue (sel.createNewFile());
        }
        instance.setDefaultWorkingDirectory(tmp);
        SA sa = new SA();
        instance.setSelectionApprover(sa);
        JFileChooser ch = instance.createFileChooser();
        ch.setSelectedFile(sel);
        ch.approveSelection();
        sa.assertApproveInvoked(sel);
    }

    public void testAddFileFilter() {
        FileChooserBuilder instance = new FileChooserBuilder("j");
        FF one = new FF ("a");
        FF two = new FF ("b");
        instance.addFileFilter(one);
        instance.addFileFilter(two);
        JFileChooser ch = instance.createFileChooser();
        Set<FileFilter> ff = new HashSet<FileFilter>(Arrays.asList(one, two));
        Set<FileFilter> actual = new HashSet<FileFilter>(Arrays.asList(ch.getChoosableFileFilters()));
        assertTrue (actual.containsAll(ff));
        //actual should also contain JFileChooser.getAcceptAllFileFilter()
        assertEquals (ff.size() + 1, actual.size());
    }

    public void testSetAcceptAllFileFilterUsed() {
        FileChooserBuilder instance = new FileChooserBuilder("k");
        assertTrue(instance.createFileChooser().isAcceptAllFileFilterUsed());
        instance.setAcceptAllFileFilterUsed(false);
        assertFalse(instance.createFileChooser().isAcceptAllFileFilterUsed());
    }

    private static final class FF extends FileFilter {
        private String x;
        FF(String x) {
            this.x = x;
        }

        @Override
        public boolean accept(File f) {
            return f.getName().endsWith(x);
        }

        @Override
        public String getDescription() {
            return x;
        }

    }

    private static final class SA implements FileChooserBuilder.SelectionApprover {
        private File[] selection;
        @Override
        public boolean approve(File[] selection) {
            this.selection = selection;
            return true;
        }

        void assertApproveInvoked(File selected) {
            assertNotNull ("approve method called", selection);
            assertEquals("One selected file", 1, selection.length);
            assertEquals("It is the one", selected, selection[0]);
        }
    }

    private static AbstractButton findDefaultButton(Container c, String txt) {
        if (c instanceof RootPaneContainer) {
            JRootPane root = ((RootPaneContainer) c).getRootPane();
            if (root == null) {
                return null;
            }
            AbstractButton btn = root.getDefaultButton();
            if (btn == null) {
                //Metal L&F does not set default button for JFileChooser
                Container parent = c;
                while (parent.getParent() != null && !(parent instanceof Dialog)) {
                    parent = parent.getParent();
                }
                if (parent instanceof Dialog) {
                    return findFileChooserAcceptButton ((Dialog) parent, txt);
                }
            } else {
                return btn;
            }
        }
        return null;
    }

    private static AbstractButton findFileChooserAcceptButton(Dialog dlg, String txt) {
        for (Component c : dlg.getComponents()) {
            if (c instanceof Container) {
                AbstractButton result = scanForButton((Container) c, txt);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static AbstractButton scanForButton(Container container, String txt) {
        assertNotNull (container);
        assertNotNull (txt);
        if (container instanceof AbstractButton) {
            if (txt.equals(((AbstractButton) container).getText())) {
                return ((AbstractButton) container);
            }
        } else {
            for (Component c : container.getComponents()) {
                if (c instanceof Container) {
                    AbstractButton b = scanForButton ((Container) c, txt);
                    if (b != null) {
                        return b;
                    }
                }
            }
        }
        return null;
    }

    @RandomlyFails // NB-Core-Build #8038: Button is visible
    public void testForceUseOfDefaultWorkingDirectory() throws InterruptedException, IOException, InvocationTargetException, UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        FileChooserBuilder instance = new FileChooserBuilder("i").setApproveText("__OK");
        instance.setDirectoriesOnly(true);
        final File toDir = getWorkDir();
        final File selDir = new File(toDir, "sel" + System.currentTimeMillis());
        if (!selDir.exists()) {
            assertTrue(selDir.mkdirs());
        }

        final JFileChooser ch = instance.createFileChooser();
        assertEquals ("__OK", ch.getApproveButtonText());
        final CountDownLatch showLatch = new CountDownLatch(1);
        ch.addAncestorListener (new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                if (ch.isShowing()) {
                    ch.removeAncestorListener(this);
                    showLatch.countDown();
                }
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {

            }

            @Override
            public void ancestorMoved(AncestorEvent event) {

            }

        });

        final AtomicReference<Object> chooserRes = new AtomicReference<Object>();
        RequestProcessor.Task task = RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                Object r = ch.showOpenDialog(null);
                chooserRes.set(r);
            }

        });


        showLatch.await();
        EventQueue.invokeAndWait (new Runnable() {
            @Override
            public void run() {
                ch.setCurrentDirectory(toDir);
            }
        });
        EventQueue.invokeAndWait (new Runnable() {
            @Override
            public void run() {
                ch.setSelectedFile (selDir);
            }
        });
        assertTrue ("Button is visible: " + ch, ch.isShowing());
        final AtomicReference<AbstractButton> btn = new AtomicReference<AbstractButton>();
        EventQueue.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                AbstractButton defButton = findDefaultButton(ch.getTopLevelAncestor(), ch.getApproveButtonText());
                btn.set(defButton);
            }

        });
        assertNotNull("have a button", btn.get());
        assertTrue(btn.get().isEnabled());
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                AbstractButton defButton = btn.get();
                defButton.doClick();
            }
        });

        task.waitFinished();
        assertEquals(JFileChooser.APPROVE_OPTION, chooserRes.get());

        assertEquals(toDir, ch.getCurrentDirectory());

        instance = new FileChooserBuilder("i");
        assertEquals("Directory not retained", toDir, instance.createFileChooser().getCurrentDirectory());

        File userHome = new File(System.getProperty("user.home"));
        assertTrue("Environment not sane", userHome.exists() && userHome.isDirectory());
        instance.forceUseOfDefaultWorkingDirectory(true).setDefaultWorkingDirectory(userHome);

        assertEquals(userHome, instance.createFileChooser().getCurrentDirectory());
    }
}
