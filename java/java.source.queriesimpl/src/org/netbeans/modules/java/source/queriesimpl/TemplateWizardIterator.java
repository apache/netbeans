/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.source.queriesimpl;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 * Special template wizard iterator for BeanForm template - requires to
 * specify superclass additionally.
 *
 * @author Tomas Pavek, Jan Stola
 */

class TemplateWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {
    private transient WizardDescriptor wiz;
    private transient WizardDescriptor.Panel superclassPanel;
    private transient boolean superclassPanelCurrent;
    private transient WizardDescriptor.InstantiatingIterator delegateIterator;

    private boolean specifySuperclass;

    public static TemplateWizardIterator createForSuperclass() {
        return new TemplateWizardIterator(true);
    }

    public static TemplateWizardIterator create() {
        return new TemplateWizardIterator(false);
    }

    public TemplateWizardIterator(boolean specifySuperclass) {
        delegateIterator = JavaTemplates.createJavaTemplateIterator();
        this.specifySuperclass = specifySuperclass;
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        wiz = wizard;
        delegateIterator.initialize(wizard);
        superclassPanelCurrent = false;
        if (superclassPanel == null && specifySuperclass) {
            superclassPanel = new SuperclassWizardPanel();
            
            ResourceBundle bundle = NbBundle.getBundle(TemplateWizardIterator.class);
            JComponent comp = (JComponent)delegateIterator.current().getComponent();
            String[] contentData = (String[])comp.getClientProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
            String[] newContentData = new String[contentData.length+1];
            System.arraycopy(contentData, 0, newContentData, 0, contentData.length);
            newContentData[contentData.length] = bundle.getString("CTL_SuperclassTitle"); // NOI18N
            comp.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, newContentData); // NOI18N
        }    
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        delegateIterator.uninitialize(wizard);
        superclassPanel = null;
    }

    @Override
    public Set instantiate() throws IOException, IllegalArgumentException {
        Set<FileObject> set = delegateIterator.instantiate();
        FileObject template = set.iterator().next();
        if (wiz instanceof TemplateWizard) {
            Logger logger = Logger.getLogger("org.netbeans.ui.metrics.form"); // NOI18N
            LogRecord rec = new LogRecord(Level.INFO, "USG_FORM_CREATED"); // NOI18N
            rec.setLoggerName(logger.getName());
            rec.setParameters(new Object[] { ((TemplateWizard)wiz).getTemplate().getName() });
            logger.log(rec);
        }

        if (specifySuperclass) {
            final String className = template.getName();
            final String superclassName = 
                    ((SuperclassWizardPanel) superclassPanel).getSuperclassName();           
            JavaSource js = JavaSource.forFileObject(template);
            js.runModificationTask(new CancellableTask<WorkingCopy>() {
                @Override
                public void cancel() {
                }
                @Override
                public void run(WorkingCopy wcopy) throws Exception {
                    wcopy.toPhase(JavaSource.Phase.RESOLVED);
   
                    for (Tree t: wcopy.getCompilationUnit().getTypeDecls()) {
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind()) && className.equals(((ClassTree) t).getSimpleName().toString())) {
                            ClassTree orig = (ClassTree) t;
                            TreeMaker maker = wcopy.getTreeMaker();
                            TypeElement superclassElm = wcopy.getElements().getTypeElement(superclassName);
                            ExpressionTree extendsTree = superclassElm != null
                                ? maker.QualIdent(superclassElm)
                                : maker.Identifier(superclassName);
                            ClassTree copy = maker.setExtends(orig, extendsTree);
                            wcopy.rewrite(orig, copy);
                            break;
                        }
                    }
                }
            }).commit();
        }

        template.setAttribute("justCreatedByNewWizard", Boolean.TRUE); // NOI18N
        
        return set;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return superclassPanelCurrent ? superclassPanel : delegateIterator.current();
    }

    @Override
    public boolean hasNext() {
        return delegateIterator.hasNext() || (!superclassPanelCurrent && superclassPanel != null);
    }
    
    @Override
    public boolean hasPrevious() {
        return superclassPanelCurrent ? true : delegateIterator.hasPrevious();
    }
    
    @Override
    public void nextPanel() {
        if (delegateIterator.hasNext()) {
            delegateIterator.nextPanel();
        } else {
            if (superclassPanelCurrent || superclassPanel == null) {
                throw new NoSuchElementException();
            } else {
                superclassPanelCurrent = true;
            }
        }
    }
    
    @Override
    public void previousPanel() {
        if (superclassPanelCurrent) {
            superclassPanelCurrent = false;
        } else {
            delegateIterator.previousPanel();
        }
    }
    
    @Override
    public void addChangeListener(ChangeListener l) {
        delegateIterator.addChangeListener(l);
    }
    
    @Override
    public String name() {
        return superclassPanelCurrent ? "" : delegateIterator.name(); // NOI18N
    }
    
    @Override
    public void removeChangeListener(ChangeListener l) {
        delegateIterator.removeChangeListener(l);
    }

    // ---------

    static class SuperclassWizardPanel implements WizardDescriptor.FinishablePanel {

        private SuperclassPanel panelUI;

        String getSuperclassName() {
            String name = panelUI != null ?
                          panelUI.superclassTextField.getText() : null;
            return name != null && !"".equals(name) ? name : "java.lang.Object"; // NOI18N
        }

        @Override
        public Component getComponent() {
            if (panelUI == null)
                panelUI = new SuperclassPanel();
            return panelUI;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void readSettings(Object settings) {
        }

        @Override
        public void storeSettings(Object settings) {
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public org.openide.util.HelpCtx getHelp () {
            return new org.openide.util.HelpCtx("gui.creatingforms"); // NOI18N
        }
        
        @Override
        public boolean isFinishPanel() {
            return true;
        }
        
    }

    // -------

    static class SuperclassPanel extends javax.swing.JPanel {

        SuperclassPanel() {
            ResourceBundle bundle = NbBundle.getBundle(TemplateWizardIterator.class);
            setName(bundle.getString("CTL_SuperclassTitle")); // NOI18N
            putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, 1); //NOI18N
            getAccessibleContext()
                .setAccessibleDescription(bundle.getString("ACSD_SuperclassPanel")); // NOI18N

            setLayout(new GridBagLayout());
            setBorder(new javax.swing.border.EmptyBorder(8, 8, 8, 8));

            label1 = new JLabel();
            superclassTextField = new JTextField();

            label1.setLabelFor(superclassTextField);
            label1.setText(bundle.getString("CTL_SuperclassName")); // NOI18N
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(0, 0, 0, 12);
            add(label1, gridBagConstraints);

            superclassTextField.setText("java.lang.Object"); // NOI18N
            superclassTextField.setToolTipText(bundle.getString("CTL_SuperclassName_Hint")); // NOI18N
            superclassTextField.getAccessibleContext()
                .setAccessibleDescription(bundle.getString("ACSD_SuperclassTextField"));  // NOI18N
            superclassTextField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent evt) {
                    superclassTextField.selectAll();
                }
            });

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            add(superclassTextField, gridBagConstraints);
        }

        @Override
        public void addNotify() {
            super.addNotify();
            superclassTextField.requestFocus();
        }

        private JLabel label1;
        private JTextField superclassTextField;
    }
}
