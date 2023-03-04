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
