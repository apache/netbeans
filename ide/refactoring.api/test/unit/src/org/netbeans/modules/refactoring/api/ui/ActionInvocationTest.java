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

package org.netbeans.modules.refactoring.api.ui;

import java.awt.Component;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.impl.ParametersPanel;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Becicka
 */
public class ActionInvocationTest extends NbTestCase {

    private static class DummyUI implements RefactoringUI {
        
        public enum Kind {
            MOVE,
            RENAME
        }
        
        private Lookup l;
        private Kind kind;

        public DummyUI(Lookup lookup, Kind kind) {
            Node node = lookup.lookup(Node.class);
            this.l = Lookups.singleton(node.getLookup().lookup(FileObject.class));
            this.kind = kind;
        }

        @Override
        public String getName() {
            return "dummy";
        }

        @Override
        public String getDescription() {
            return "dummy";
        }

        @Override
        public boolean isQuery() {
            return true;
        }

        @Override
        public CustomRefactoringPanel getPanel(ChangeListener parent) {
            return new CustomRefactoringPanel() {

                @Override
                public void initialize() {
                }

                @Override
                public Component getComponent() {
                    return new JPanel();
                }
            };
        }

        @Override
        public Problem setParameters() {
            return null;
        }

        @Override
        public Problem checkParameters() {
            return null;
        }

        @Override
        public boolean hasParameters() {
            return false;
        }

        @Override
        public AbstractRefactoring getRefactoring() {
            switch (kind) {
                case MOVE: return new MoveRefactoring(l);
                case RENAME: return new RenameRefactoring(l);
            }
            return null;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(DummyUI.class);
        }
    }
    private FileObject f;

    /** Creates a new instance of ActionInstantiationTest */
    public ActionInvocationTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws IOException, Exception {
        try {
            super.setUp();
            f = FileUtil.createData(FileUtil.toFileObject(getWorkDir()), "test.txt");

            OutputStream outputStream = f.getOutputStream();
            outputStream.write("test".getBytes());
            outputStream.close();
            
            assertEquals(DD.class, Lookup.getDefault().lookup(DialogDisplayer.class).getClass());
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void testRenameAction() throws InterruptedException, InvocationTargetException, IOException {
        DataObject testdo = DataObject.find(f);
        final Node node = testdo.getNodeDelegate();
        
        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        ic.add(node);
        final Action rename = RefactoringActionsFactory.renameAction().createContextAwareInstance(lookup);
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                if (rename.isEnabled()) {
                    rename.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
                    if (!((RenameRefactoring) DD.rui.getRefactoring()).getRefactoringSource().lookup(FileObject.class).equals(f))
                        fail("Rename dialog was opened with wrong data");
                } else {
                    fail("Action is not enabled.");
                }
            }
        });
    }
    
    public void testMoveAction() throws InterruptedException, InvocationTargetException, DataObjectNotFoundException, IOException {
        DataObject testdo = DataObject.find(f);
        final Node node = testdo.getNodeDelegate();

        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        ic.add(node);
        final Action move = RefactoringActionsFactory.moveAction().createContextAwareInstance(lookup);
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                if (move.isEnabled()) {
                    move.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
                    if (!((MoveRefactoring) DD.rui.getRefactoring()).getRefactoringSource().lookup(FileObject.class).equals(f))
                        fail("MoveClass was opened with wrong data");
                } else {
                    fail("Action is not enabled.");
                }
            }
        });
    }

    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (new DD ());
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider.class)
    public static class DefaultActionsProvider extends ActionsImplementationProvider {
        
        public DefaultActionsProvider() {
        }

        @Override
        public boolean canMove(Lookup lookup) {
            return true;
        }

        @Override
        public boolean canRename(Lookup lookup) {
            return true;
        }

        @Override
        public void doMove(Lookup lookup) {
            UI.openRefactoringUI(new DummyUI(lookup, DummyUI.Kind.MOVE));
        }

        @Override
        public void doRename(Lookup lookup) {
            UI.openRefactoringUI(new DummyUI(lookup, DummyUI.Kind.RENAME));
        }
        
    }

    /** Our own dialog displayer.
     */
    @org.openide.util.lookup.ServiceProvider(service=org.openide.DialogDisplayer.class, supersedes="org.netbeans.core.windows.services.DialogDisplayerImpl")
    public static final class DD extends org.openide.DialogDisplayer {
        public static Object[] options;
        public static RefactoringUI rui;
        private Object toReturn;
        
        @Override
        public java.awt.Dialog createDialog(org.openide.DialogDescriptor descriptor) {
            JDialog dialog = new JDialog() {
                @Override
                public void setVisible(boolean visible) {
                }
                
                @Override
                public void show() {
                }
            };
            toReturn = descriptor.getMessage();
            if (toReturn instanceof Component)
                dialog.getContentPane().add((Component) toReturn);
            
            if (toReturn instanceof ParametersPanel) {
                try {
                    java.lang.reflect.Field f = toReturn.getClass().getDeclaredField("rui");
                    f.setAccessible(true);
                    rui = (RefactoringUI) f.get(toReturn);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            
            return dialog;
        }
        
        @Override
        public Object notify(org.openide.NotifyDescriptor descriptor) {
            assertNull (options);
            assertNotNull(toReturn);
            options = descriptor.getOptions();
            Object r = toReturn;
            toReturn = null;
            return r;
        }
        
    } // end of DD
}
