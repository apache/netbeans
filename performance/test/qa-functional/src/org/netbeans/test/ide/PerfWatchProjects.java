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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.test.ide;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.Log;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapFactory;
import org.netbeans.lib.profiler.heap.HeapSummary;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.view.TreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class PerfWatchProjects {
    private static final Logger LOG = Logger.getLogger(PerfWatchProjects.class.getName());
    private static String path=null;

    
    private PerfWatchProjects() {
    }

    public static void initialize() throws Exception {
        Log.enableInstances(Logger.getLogger("TIMER"), "Project", Level.FINEST);

    }

    private static void cleanWellKnownStaticFields() throws Exception {
        Object o;

//        resetJTreeUIs(Frame.getFrames());

        tryCloseNavigator();

        StringSelection ss = new StringSelection("");
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
//      Toolkit.getDefaultToolkit().getSystemSelection().setContents(ss, ss);
//      fix for Issue 146901
        Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemSelection();
        if (clipBoard != null) {
            clipBoard.setContents(ss, ss);
        }
        Clipboard cc = Lookup.getDefault().lookup(Clipboard.class);
        Assert.assertNotNull("There is a clipboard in lookup", cc);
        cc.setContents(ss, ss);

        for (Frame f : Frame.getFrames()) {
            clearInstanceField( f, "java.awt.Container", "dispatcher" );
        }

// XXX: uncommented because of the csl.api & related changes
        JFrame empty = new JFrame("Clear");
        empty.getContentPane().setLayout(new FlowLayout());
        empty.getContentPane().add(new JEditorPane());
        empty.pack();
        empty.setVisible(true);
        empty.requestFocusInWindow();
// --------------------------------------------------------


        clearField("sun.awt.im.InputContext", "previousInputMethod");
        clearField("sun.awt.im.InputContext", "inputMethodWindowContext");
        clearField("sun.awt.im.CompositionAreaHandler", "compositionAreaOwner");
//        clearField("sun.awt.AppContext", "mainAppContext");
//        clearField("org.netbeans.modules.beans.BeanPanel", "INSTANCE");
        clearField("java.awt.KeyboardFocusManager", "focusedWindow");
        clearField("java.awt.KeyboardFocusManager", "activeWindow");
        clearField("java.awt.KeyboardFocusManager", "focusOwner");
        clearField("java.awt.KeyboardFocusManager", "permanentFocusOwner");
        clearField("java.awt.KeyboardFocusManager", "newFocusOwner");
        clearField("java.awt.KeyboardFocusManager", "currentFocusCycleRoot");
//        clearField("org.netbeans.jemmy.EventTool", "listenerSet");
        clearField("sun.awt.X11.XKeyboardFocusManagerPeer", "currentFocusOwner");
        clearField("sun.awt.X11.XKeyboardFocusManagerPeer", "currentFocusedWindow");
//        clearField("org.netbeans.modules.java.navigation.CaretListeningFactory", "INSATNCE");
//        clearField("org.netbeans.modules.editor.hints.HintsUI", "INSTANCE");
//        clearField("org.netbeans.modules.websvc.core.ProjectWebServiceView", "views");
//        clearField("org.netbeans.api.java.source.support.OpenedEditors", "DEFAULT");
//        clearField("org.netbeans.spi.palette.PaletteSwitch", "theInstance");
//        clearField("org.netbeans.core.NbMainExplorer$MainTab", "lastActivated");
//        clearField("org.netbeans.core.NbMainExplorer$MainTab", "DEFAULT");
/*
        o = getFieldValue("org.netbeans.api.java.source.JavaSource", "toRemove");
        if (o instanceof Collection) {
            Collection c = (Collection) o;
            c.clear();
        }
        o = getFieldValue("org.netbeans.api.java.source.JavaSource", "requests");
        if (o instanceof Collection) {
            Collection c = (Collection) o;
            c.clear();
        }
*/
        clearField("sun.awt.im.InputContext", "previousInputMethod");
        clearField("sun.awt.im.InputContext", "inputMethodWindowContext");
    }


    public static void assertTextDocuments() throws Exception {
        closeTopComponents();
        Thread.sleep(2000);
                TopComponent tc = new TopComponent();
                tc.setLayout(new FlowLayout());
                tc.add(new JTextArea());
                tc.open();
                tc.requestVisible();
                tc.requestActive();
                String jVMversion = System.getProperty("java.specification.version");
                System.out.println("Java.specification.version="+jVMversion);
                if (!("1.8".equals(jVMversion))) {
                    try {
                        System.out.println("Cleaning well known static fields");
                        cleanWellKnownStaticFields();
                    } catch (Exception ex) {
                        throw new IllegalStateException(ex);
                    }
                }
                System.setProperty("assertgc.paths", "0");
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException exc) {
                    Exceptions.printStackTrace(exc);
                }
                try {
                    Log.assertInstances("Are all documents GCed?", "TextDocument");
                } catch (AssertionFailedError afe) {
                    throw afe;
                } finally {
                    dumpHeap(null);
                }
    }

    public static void assertProjects() throws Exception {
        Object o;

        OpenProjects.getDefault().close(
            OpenProjects.getDefault().getOpenProjects()
        );
        Project p = new Project() {
            public FileObject getProjectDirectory() {
                return FileUtil.getConfigRoot();
            }

            public Lookup getLookup() {
                return Lookup.EMPTY;
            }
        };
        try {
            OpenProjects.getDefault().open(new Project[] { p }, false);
        } catch (AssertionError ae) {
            System.out.println("Excepton during creation of fake project:");
            ae.printStackTrace();
        }
        try {
            OpenProjects.getDefault().setMainProject(p);
        } catch (AssertionError ae) {
            System.out.println("Excepton during setting of fake project as main:");
            ae.printStackTrace();
        }

        for (int i = 0; i < 10; i++) {
            EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
            }
        });
        }

             try {
                    cleanWellKnownStaticFields();
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
                if (Boolean.getBoolean("ignore.random.failures")) {
                    // remove the if we don't care about random failures
                    // reported as #
                    //removeTreeView(Frame.getFrames());
                }

                System.setProperty("assertgc.paths", "0");
                try {
                    Log.assertInstances("Checking if all projects are really garbage collected", "Project");
                } catch (AssertionFailedError t) {
                    Logger.getLogger(PerfWatchProjects.class.getName()).warning(t.getMessage());
                    if (!Boolean.getBoolean("ignore.random.failures")) {
                        throw t;
                    }
                } finally {
                    dumpHeap(null);
                }
    }
    
    static void analyzeHeapDump(String path,boolean isDocument) {
        File f = new File(path);
        Heap h = null;
        try {
            h = HeapFactory.createHeap(f);
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        HeapSummary hs = h.getSummary();
        if (isDocument) {
            Assert.assertTrue("!!! The heap consumption is too big: "+hs.getTotalLiveBytes(), hs.getTotalLiveBytes()<127000000);
            Assert.assertTrue("!!! The number of instances is too big: "+hs.getTotalLiveInstances(), hs.getTotalLiveInstances()<1825000);
            Assert.assertTrue("!!! The number of classes is too big: "+h.getAllClasses().size(), h.getAllClasses().size()<22300);
        } else {
            Assert.assertTrue("!!! The heap consumption is too big: "+hs.getTotalLiveBytes(), hs.getTotalLiveBytes()<125000000);
            Assert.assertTrue("!!! The number of instances is too big: "+hs.getTotalLiveInstances(), hs.getTotalLiveInstances()<1530000);
            Assert.assertTrue("!!! The number of classes is too big: "+h.getAllClasses().size(), h.getAllClasses().size()<22400);
        }
    }

    private static int printTreeView(Component[] arr) throws Exception {
        int cnt = 0;
        StringBuilder str = new StringBuilder();
        for (Component c : arr) {
            if (c instanceof TreeView) {
                Set<?> set = (Set<?>) getField(TreeView.class, "visHolder").get(c);
                if (!set.isEmpty()) {
                    cnt += set.size();
                    str.append("visHolder for TreeView in '" + c.getParent().getName() + "':");
                    for (Object o : set) {
                        str.append(o);
                    }
                }
                continue;
            }
            if (c instanceof Container) {
                Container o = (Container)c;
                cnt += printTreeView(o.getComponents());
            }
        }
        if (str.length() > 0) {
            Logger.getLogger(PerfWatchProjects.class.getName()).warning(str.toString());
        }
        return cnt;
    }

    private static void removeTreeView(Component[] arr) throws Exception {
        for (Component c : arr) {
            if (c instanceof TreeView) {
                Set<?> set = (Set<?>) getField(TreeView.class, "visHolder").get(c);
                set.clear();
                continue;
            }
            if (c instanceof Container) {
                Container o = (Container)c;
                removeTreeView(o.getComponents());
            }
        }
    }
    private static void resetJTreeUIs(Component[] arr) {
        for (Component c : arr) {
            if (c instanceof JTree) {
                JTree jt = (JTree)c;
                jt.updateUI();
            }
            if (c instanceof Container) {
                Container o = (Container)c;
                resetJTreeUIs(o.getComponents());
            }
        }
    }

    /**
     * #124061 workaround - close navigator before tests
     */
    private static void tryCloseNavigator() throws Exception {
        for (TopComponent c : new ArrayList<TopComponent>(TopComponent.getRegistry().getOpened())) {
            LOG.fine("Processing TC " + c.getDisplayName() + "class " + c.getClass().getName());
            if (c.getClass().getName().equals("org.netbeans.modules.navigator.NavigatorTC")) {
                final TopComponent navigator = c;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        navigator.close();
                    }
                });
                LOG.fine("tryCloseNavigator: Navigator closed, OK!");
                break;
            }
        }
        clearField("org.netbeans.modules.navigator.NavigatorTC", "instance");
        clearField("org.netbeans.modules.navigator.ProviderRegistry","instance");
    }

    private static Object clearField(String clazz, String... name) throws Exception {
        return clearInstanceField(null, clazz, name);
    }
    private static Object getFieldValue(String clazz, String... name) throws Exception {
        Object ret = null;
        for (int i = 0; i < name.length; i++) {
            Field f = i == 0 ? getField(clazz, name[0]) : getField(ret.getClass(), name[i]);
            ret = f.get(ret);
        }
        return ret;
    }


    private static Field getField(String clazz, String name) throws NoSuchFieldException, ClassNotFoundException {
        ClassLoader l = Thread.currentThread().getContextClassLoader();
        if (l == null) {
            l = PerfWatchProjects.class.getClassLoader();
        }
        Class<?> c = Class.forName(clazz, true, l);
        return getField(c, name);
    }
    private static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        Field f = clazz.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    private static Object clearInstanceField(Object obj, String clazz, String... name) throws Exception {
        Object ret = obj;
        for (int i = 0; i < name.length; i++) {
            Field f;
            try {
                f = i == 0 ? getField(clazz, name[0]) : getField(ret.getClass(), name[i]);
            } catch (NoSuchFieldException ex) {
                LOG.log(Level.WARNING, "Cannot get " + name[i]);
                continue;
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.WARNING, "Cannot class " + clazz);
                continue;
            }
            Object now = ret;
            ret = f.get(now);
            for (int tryHarder = 0;; tryHarder++) {
                f.set(now, null);
                if (f.get(now) == null) {
                    break;
                }
                if (tryHarder == 10) {
                    Assert.fail("Field is really cleared " + f + " but was: " + f.get(now));
                }
                Thread.sleep(100);
            }
            if (ret == null) {
                LOG.info("Getting " + f + " from " + now + " returned null");
                break;
            }
        }
        return ret;
    }

    private static void dumpHeap(String path) {
        System.out.println("DUMPING HEAP");
        Method m = null;
        Class c = null;
        HotSpotDiagnosticMXBean hdmxb=null;
        try {
            c = Class.forName("sun.management.ManagementFactoryHelper");//NOI18N
        } catch (ClassNotFoundException exc) {
            System.out.println(exc.getMessage());
            try {
                c = Class.forName("sun.management.ManagementFactory");//NOI18N
            } catch (ClassNotFoundException ex1) {
                System.out.println(ex1.getMessage());
            }
        }
        if (c!=null) {
            try {
                m= c.getMethod("getDiagnosticMXBean");//NOI18N
            } catch (NoSuchMethodException exc) {
                System.out.println(exc.getMessage());
            } catch (SecurityException exc) {
                System.out.println(exc.getMessage());
            }
        }
        if (m!=null) {
            try {
                hdmxb= (HotSpotDiagnosticMXBean)m.invoke(null);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            long i = System.currentTimeMillis();
            if (path==null || "".equals(path)) {
                path=System.getProperty("nbjunit.workdir")+File.separator+"Heapdump"+i+".hprof";
            }
            System.out.println("Creating heap dump, target directory="+path);
            try {
                hdmxb.dumpHeap(path, true);
                PerfWatchProjects.setPath(path);
                System.out.println("Heap dump successfully created in: "+path);
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        }
    }
    
    private static void closeTopComponents() {
        for (TopComponent tc : new ArrayList<TopComponent>(TopComponent.getRegistry().getOpened())) {
            final EditorCookie ec = tc.getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                ec.close();
            }
        }
        System.out.println("closed all ... hopefully");
    }

    public static void waitScanFinished() {
        try {
            class Wait implements Runnable {

                boolean initialized;
                boolean ok;

                public void run() {
                    if (initialized) {
                        ok = true;
                        return;
                    }
                    initialized = true;
                    boolean canceled = ScanDialog.runWhenScanFinished(this, "tests");
                    Assert.assertFalse("Dialog really finished", canceled);
                    Assert.assertTrue("Runnable run", ok);
                }
            }
            Wait wait = new Wait();
            SwingUtilities.invokeAndWait(wait);
        } catch (Exception ex) {
            throw (AssertionFailedError)new AssertionFailedError().initCause(ex);
        }
    }
    
    public static void setPath(String path) {
        PerfWatchProjects.path=path;
    }
    
    /**
     * @return the path
     */
    public static String getPath() {
        return path;
    }
}
