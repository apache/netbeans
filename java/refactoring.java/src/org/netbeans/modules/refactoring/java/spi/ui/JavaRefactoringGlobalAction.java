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
package org.netbeans.modules.refactoring.java.spi.ui;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.List;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Actions;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 * @author Jan Becicka
 * @author Tim Boudreau
 */
final class JavaRefactoringGlobalAction extends NodeAction {
    private final JavaRefactoringActionDelegate delegate;
    public JavaRefactoringGlobalAction(String name, JavaRefactoringActionDelegate delegate) {
        setName(name);
        this.delegate = delegate;
    }

    @Override
    public final String getName() {
        return (String) getValue(Action.NAME);
    }

    protected void setName(String name) {
        putValue(Action.NAME, name);
    }

    protected void setMnemonic(char m) {
        putValue(Action.MNEMONIC_KEY, m);
    }

    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean enable(Lookup context) {
        return delegate.isEnabled(context);
    }

    public void performAction (Lookup context) {
        EditorCookie ec = getEditorCookie(context);
        if (ec != null) {
            new TextComponentRunnable(ec, delegate).run();
        }
    }


    protected Lookup getLookup(Node[] n) {
        InstanceContent ic = new InstanceContent();
        for (Node node:n) {
            ic.add(node);
        }
        Lookup result = new AbstractLookup (ic);
        EditorCookie tc = getEditorCookie(result);
        if (tc != null) {
            ic.add(tc);
        }
        ic.add(new Hashtable(0));
        return result;
    }


    static EditorCookie getEditorCookie(Lookup context) {
        EditorCookie ck = context.lookup (EditorCookie.class);
        if (ck == null) {
            Node n  = context.lookup (Node.class);
            if (n != null) {
                ck = n.getLookup().lookup (EditorCookie.class);
                if (ck != null) {
                    TopComponent activetc = TopComponent.getRegistry().getActivated();
                    if (!(activetc instanceof Pane)) {
                        ck = null;
                    }
                }
            }
        }
        return ck;
    }

    @Override
    public final void performAction(final Node[] activatedNodes) {
        performAction(getLookup(activatedNodes));
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return enable(getLookup(activatedNodes));
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextAction(actionContext);
    }

    protected class ContextAction implements Action, Presenter.Menu, Presenter.Popup, Presenter.Toolbar {

        Lookup context;

        public ContextAction(Lookup context) {
            this.context=context;
        }

        @Override
        public Object getValue(String arg0) {
            return JavaRefactoringGlobalAction.this.getValue(arg0);
        }

        @Override
        public void putValue(String arg0, Object arg1) {
            JavaRefactoringGlobalAction.this.putValue(arg0, arg1);
        }

        @Override
        public void setEnabled(boolean arg0) {
            JavaRefactoringGlobalAction.this.setEnabled(arg0);
        }

        @Override
        public boolean isEnabled() {
            return enable(context);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener arg0) {
            JavaRefactoringGlobalAction.this.addPropertyChangeListener(arg0);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener arg0) {
            JavaRefactoringGlobalAction.this.removePropertyChangeListener(arg0);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            JavaRefactoringGlobalAction.this.performAction(context);
        }
        @Override
        public JMenuItem getMenuPresenter() {
            if (isMethodOverridden(JavaRefactoringGlobalAction.this, "getMenuPresenter")) { // NOI18N

                return JavaRefactoringGlobalAction.this.getMenuPresenter();
            } else {
                return new Actions.MenuItem(this, true);
            }
        }

        @Override
        public JMenuItem getPopupPresenter() {
            if (isMethodOverridden(JavaRefactoringGlobalAction.this, "getPopupPresenter")) { // NOI18N

                return JavaRefactoringGlobalAction.this.getPopupPresenter();
            } else {
                return new Actions.MenuItem(this, false);
            }
        }

        @Override
        public Component getToolbarPresenter() {
            if (isMethodOverridden(JavaRefactoringGlobalAction.this, "getToolbarPresenter")) { // NOI18N

                return JavaRefactoringGlobalAction.this.getToolbarPresenter();
            } else {
                final JButton button = new JButton();
                Actions.connect(button, this);
                return button;
            }
        }

        private boolean isMethodOverridden(NodeAction d, String name) {
            try {
                Method m = d.getClass().getMethod(name, new Class[0]);

                return m.getDeclaringClass() != CallableSystemAction.class;
            } catch (java.lang.NoSuchMethodException ex) {
                ex.printStackTrace();
                throw new IllegalStateException("Error searching for method " + name + " in " + d); // NOI18N
            }
        }
    }

   private static final class TextComponentRunnable implements Runnable, CancellableTask <CompilationController> {
        private final JTextComponent textC;
        private final int caret;
        private final int start;
        private final int end;
        private RefactoringUI ui;
        private final JavaRefactoringActionDelegate delegate;

        public TextComponentRunnable(EditorCookie ec, JavaRefactoringActionDelegate delegate) {
            this.delegate = delegate;
            this.textC = NbDocument.findRecentEditorPane(ec);
            this.caret = textC.getCaretPosition();
            this.start = textC.getSelectionStart();
            this.end = textC.getSelectionEnd();
            assert caret != -1;
            assert start != -1;
            assert end != -1;
        }

        @Override
        public final void run() {
            try {
                JavaSource source = JavaSource.forDocument(textC.getDocument());
                source.runUserActionTask(this, false);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return;
            }
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (ui!=null) {
                UI.openRefactoringUI(ui, activetc);
            } else {
                DialogDescriptor.Message msg = new DialogDescriptor.Message (delegate.getErrorMessage(),
                        DialogDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
            }
        }

        @Override
        public void cancel() {
            //do nothing
        }

        @Override
        public void run(CompilationController cc) throws Exception {
            TreePath selectedElement = null;
            cc.toPhase(Phase.RESOLVED);
            selectedElement = cc.getTreeUtilities().pathFor(caret);
            //workaround for issue 89064
            if (selectedElement.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                List<? extends Tree> decls = cc.getCompilationUnit().getTypeDecls();
                if (!decls.isEmpty()) {
                    selectedElement = TreePath.getPath(cc.getCompilationUnit(), decls.get(0));
                }
            }
            ui = delegate.createRefactoringUI(TreePathHandle.create(selectedElement, cc), start, end, cc);
        }
    }
}
