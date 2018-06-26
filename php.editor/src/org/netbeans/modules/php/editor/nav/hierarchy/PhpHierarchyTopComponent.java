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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.php.editor.nav.hierarchy;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.model.Model;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Union2;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays PHP class hierarchy.
 * @author Radek Matous
 */
@org.netbeans.api.annotations.common.SuppressWarnings({"SE_NO_SUITABLE_CONSTRUCTOR_FOR_EXTERNALIZATION"})
final class PhpHierarchyTopComponent extends TopComponent implements PropertyChangeListener {

    private static PhpHierarchyTopComponent instance;
    /* path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/php/editor/resources/subtypehierarchy.gif"; // NOI18N
    private static final String PREFERRED_ID = "PhpHierarchyTopComponent"; // NOI18N
    private JComponent last = null;
    private RequestProcessor requestProcessor;
    private Task task;

    private PhpHierarchyTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(getClass(), "CTL_HierarchyTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_HierarchyTopComponent")); // NOI18N
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty("KeepNonPersistentTCInModelWhenClosed", Boolean.TRUE); // NOI18N
    }

    private void setModel(Model model) {
        FileObject fileObject = model.getFileScope().getFileObject();
        setName(fileObject == null ? "?" : fileObject.getNameExt() + " - " + NbBundle.getMessage(getClass(), "CTL_HierarchyTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_HierarchyTopComponent")); // NOI18N
        if (!(last instanceof ClassHierarchyPanel)) {
            removeAll();
            ClassHierarchyPanel panel = new ClassHierarchyPanel(true);
            add(panel, BorderLayout.CENTER);
            validate();
            last = panel;
        }
        ((ClassHierarchyPanel) last).setModel(model);
    }

    @Override
    public void requestActive() {
        super.requestActive();
        if (last != null) {
            last.requestFocusInWindow();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jButton1.setBackground(new JTextArea().getBackground());
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(PhpHierarchyTopComponent.class, "NoViewAvailable")); // NOI18N
        jButton1.setBorderPainted(false);
        jButton1.setEnabled(false);
        add(jButton1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized PhpHierarchyTopComponent getDefault() {
        if (instance == null) {
            instance = new PhpHierarchyTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the HierarchyTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized PhpHierarchyTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(PhpHierarchyTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof PhpHierarchyTopComponent) {
            return (PhpHierarchyTopComponent) win;
        }
        Logger.getLogger(PhpHierarchyTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID + // NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    @Override
    public void componentOpened() {
        TopComponent.getRegistry().addPropertyChangeListener(this);
    }

    @Override
    public void componentClosed() {
        removeAll();
        initComponents();
        last = null;
        TopComponent.getRegistry().removePropertyChangeListener(this);
    }

    /* replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (isOpened() && evt.getPropertyName() == null
                || evt.getPropertyName().equals(TopComponent.Registry.PROP_ACTIVATED_NODES)) {
            PhpHierarchyTopComponent view = PhpHierarchyTopComponent.findInstance();
            if (view != null && view.isOpened()) {
                refresh();
            }
        }
    }

    void refresh() {
        synchronized (this) {
            if (requestProcessor == null) {
                requestProcessor = new RequestProcessor("RefreshHierarchyScheduler"); //NOI18N
                task = requestProcessor.create(new Runnable() {

                    @Override
                    public void run() {
                        boolean indexing = IndexingManager.getDefault().isIndexing();
                        if (!indexing) {
                            try {
                                final List<Union2<Document, FileObject>> fromNode = new ArrayList<>();
                                SwingUtilities.invokeAndWait(new Runnable() {

                                    @Override
                                    public void run() {
                                        final Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
                                        if (activatedNodes.length > 0) {
                                            EditorCookie c = activatedNodes[0].getLookup().lookup(EditorCookie.class);
                                            if (ShowPhpClassHierarchyAction.isFromEditor(c)) {
                                                Union2<Document, FileObject> first = Union2.createFirst(c.getOpenedPanes()[0].getDocument());
                                                fromNode.add(first);
                                            } else {
                                                DataObject dobj = activatedNodes[0].getLookup().lookup(DataObject.class);
                                                FileObject fo = (dobj != null) ? dobj.getPrimaryFile() : null;
                                                if (fo != null && fo.isData() && FileUtils.isPhpFile(fo)) {
                                                    Union2<Document, FileObject> second = Union2.createSecond(fo);
                                                    fromNode.add(second);
                                                }
                                            }
                                        }
                                    }
                                });
                                if (fromNode.size() > 0) {
                                    final Model model = ShowPhpClassHierarchyAction.getModel(fromNode.get(0));
                                    if (model != null) {
                                        SwingUtilities.invokeLater(new Runnable() {

                                            @Override
                                            public void run() {
                                                PhpHierarchyTopComponent.this.setModel(model);
                                            }
                                        });
                                    }
                                }
                            } catch (InterruptedException | InvocationTargetException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        } else {
                            task.cancel();
                            task.schedule(500);
                        }
                    }
                });
            }
        }
        task.cancel();
        task.schedule(500);
    }

    static final class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return PhpHierarchyTopComponent.getDefault();
        }
    }
}
