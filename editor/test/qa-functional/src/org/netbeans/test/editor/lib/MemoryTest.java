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

package org.netbeans.test.editor.lib;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;

/**
 *
 * @author vita
 */
public class MemoryTest extends NbTestCase {

    public MemoryTest(String name) {
        super(name);
    }
    
    public void testNbEditorToolbarGCed() throws Exception {
        
        List<JToolBar> toolbars = new ArrayList<JToolBar>();
        Map<JToolBar, String> toolbar2filePath = new HashMap<JToolBar, String>();
        
        File dir = new File(getDataDir(), "org/netbeans/test/editor/data");
        for(File f : dir.listFiles()) {
            if (f.getName().equals("dummy.txt") 
                || f.getName().endsWith("GuiPanel.java") || f.getName().endsWith("GuiPanel.form")
                || f.getName().endsWith(".xsd")
            ) {
                continue;
            }
            
            Object [] cookies = findEditorCookies(f);
            
            // open the file
            if (cookies[0] instanceof EditCookie) {
                ((EditCookie) cookies[0]).edit();
            } else if (cookies[1] instanceof OpenCookie) {
                ((OpenCookie) cookies[1]).open();
            } else {
                fail(f.getAbsolutePath() + " has no EditCookie nor OpenCookie");
            }
            
            // wait for a while
            Thread.sleep(1000);
            
            if (cookies[2] instanceof EditorCookie) {
                List<JToolBar> list = new ArrayList<JToolBar>();
                collectToolbars(list, (EditorCookie) cookies[2]);
                
                toolbars.addAll(list);
                for(JToolBar toolbar : list) {
                    toolbar2filePath.put(toolbar, f.getAbsolutePath());
                }
            } else {
                fail(f.getAbsolutePath() + " has no EditorCookie");
            }
        }
        
        // open the dummy file
        File javaFile2 = new File(getDataDir(), "org/netbeans/test/editor/data/dummy.txt");
        Object [] cookies = findEditorCookies(javaFile2);
        
        if (cookies[0] instanceof EditCookie) {
            ((EditCookie) cookies[0]).edit();
        } else if (cookies[1] instanceof OpenCookie) {
            ((OpenCookie) cookies[1]).open();
        } else {
            fail(javaFile2.getAbsolutePath() + " has no EditCookie nor OpenCookie");
        }

        // test that all toolbars can be GCed
        for(int i = 0; i < toolbars.size(); i++) {
            JToolBar toolbar = toolbars.set(i, null);
            Reference<JToolBar> toolbarRef = new WeakReference<JToolBar>(toolbar);
            String filePath = toolbar2filePath.remove(toolbar);
            
            toolbar = null;
            assertGC("Can't GC toolbar for " + filePath, toolbarRef);
        }
    }
    
    public void testEditorComponentGCed() throws Exception {
        
        List<JEditorPane> panes = new ArrayList<JEditorPane>();
        Map<JEditorPane, String> pane2filePath = new HashMap<JEditorPane, String>();
        
        File dir = new File(getDataDir(), "org/netbeans/test/editor/data");
        for(File f : dir.listFiles()) {
            if (f.getName().equals("dummy.txt") 
                || f.getName().endsWith("GuiPanel.java") || f.getName().endsWith("GuiPanel.form")
                || f.getName().endsWith(".xsd")
            ) {
                continue;
            }
            
            Object [] cookies = findEditorCookies(f);
            
            // open the file
            if (cookies[0] instanceof EditCookie) {
                ((EditCookie) cookies[0]).edit();
            } else if (cookies[1] instanceof OpenCookie) {
                ((OpenCookie) cookies[1]).open();
            } else {
                fail(f.getAbsolutePath() + " has no EditCookie nor OpenCookie");
            }
            
            // wait for a while
            Thread.sleep(1000);
            
            if (cookies[2] instanceof EditorCookie) {
                List<JEditorPane> list = new ArrayList<JEditorPane>();
                collectPanes(list, (EditorCookie) cookies[2]);
                
                panes.addAll(list);
                for(JEditorPane pane : list) {
                    pane2filePath.put(pane, f.getAbsolutePath());
                }
            } else {
                fail(f.getAbsolutePath() + " has no EditorCookie");
            }
        }
        
        // open the dummy file
        File javaFile2 = new File(getDataDir(), "org/netbeans/test/editor/data/dummy.txt");
        Object [] cookies = findEditorCookies(javaFile2);
        
        if (cookies[0] instanceof EditCookie) {
            ((EditCookie) cookies[0]).edit();
        } else if (cookies[1] instanceof OpenCookie) {
            ((OpenCookie) cookies[1]).open();
        } else {
            fail(javaFile2.getAbsolutePath() + " has no EditCookie nor OpenCookie");
        }

        // test that all toolbars can be GCed
        for(int i = 0; i < panes.size(); i++) {
            JEditorPane pane = panes.set(i, null);
            Reference<JEditorPane> paneRef = new WeakReference<JEditorPane>(pane);
            String filePath = pane2filePath.remove(pane);
            
            pane = null;
            assertGC("Can't GC JEditorPane for " + filePath, paneRef);
        }
    }
    
    private static Object [] findEditorCookies(File f) throws IOException {
        //System.out.println("f = " + f.getPath());
        FileObject ff1 = FileUtil.toFileObject(f);
        //System.out.println("ff = " + ff1);
        DataObject dd1 = DataObject.find(ff1);
        //System.out.println("dd = " + dd1);
        return new Object [] {
            dd1.getCookie(EditCookie.class),
            dd1.getCookie(OpenCookie.class),
            dd1.getCookie(EditorCookie.class),
        };
    }
    
    private static void collectToolbars(final List<JToolBar> toolbars, final EditorCookie oc) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JEditorPane [] jeps = oc.getOpenedPanes();

                for(JEditorPane jep : jeps) {
                    EditorUI editorUI = Utilities.getEditorUI(jep);
                    assertNotNull(editorUI);

                    JToolBar toolbar = editorUI.getToolBarComponent();
                    assertNotNull(toolbar);
                    toolbars.add(toolbar);

                    TopComponent tc = findTopComponent(jep);
                    //System.out.println("tc = " + tc);
                    assertNotNull(tc);

                    boolean closed = tc.close();
                    assertTrue("Can't close TC", closed);
                }
            }
        });
    }
    
    private static void collectPanes(final List<JEditorPane> panes, final EditorCookie oc) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JEditorPane [] jeps = oc.getOpenedPanes();

                for(JEditorPane jep : jeps) {
                    panes.add(jep);

                    TopComponent tc = findTopComponent(jep);
                    //System.out.println("tc = " + tc);
                    assertNotNull(tc);

                    boolean closed = tc.close();
                    assertTrue("Can't close TC", closed);
                }
            }
        });
    }
    
    private static TopComponent findTopComponent(Component jep) {
        TopComponent topComponent = null;
        Component parent = jep;
        while (parent != null) {
            if (parent instanceof TopComponent) {
                topComponent = (TopComponent) parent;
            }
            parent = parent.getParent();
        }
        return topComponent;
    }
    
    public static Test suite() {
      return NbModuleSuite.create(
              NbModuleSuite.createConfiguration(MemoryTest.class).enableModules(".*").clusters(".*"));
   }
}
