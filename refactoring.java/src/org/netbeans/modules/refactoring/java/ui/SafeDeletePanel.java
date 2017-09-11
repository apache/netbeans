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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.refactoring.java.ui;
import com.sun.source.tree.Tree;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Collection;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.java.RefactoringModule;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * Subclass of CustomRefactoringPanel representing the
 * Safe Delete refactoring UI
 * @author Bharath Ravikumar
 */
public class SafeDeletePanel extends JPanel implements CustomRefactoringPanel {
    private final transient SafeDeleteRefactoring refactoring;
    private boolean regulardelete;
    private ChangeListener parent;
    
    /**
     * Creates new form RenamePanelName
     * @param refactoring The SafeDelete refactoring used by this panel
     * @param selectedElements A Collection of selected elements
     */
    public SafeDeletePanel(SafeDeleteRefactoring refactoring, boolean regulardelete, ChangeListener parent) {
        setName(NbBundle.getMessage(SafeDeletePanel.class,
                regulardelete ? "LBL_SafeDel_Delete" : "LBL_SafeDel")); // NOI18N
        this.refactoring = refactoring;
        this.regulardelete = regulardelete;
        this.parent = parent;
        initComponents();
    }
    
    private boolean initialized = false;
    private String methodDeclaringClass = null;
    
    String getMethodDeclaringClass() {
        return methodDeclaringClass;
    }
    /**
     * Initialization method. Creates appropriate labels in the panel.
     */
    @Override
    public void initialize() {
        //This is needed since the checkBox is gets disabled on a
        //repeated invocation of SafeDelete follwing removal of references
        //to the element
        searchInComments.setEnabled(true);
        
        if (initialized) {
            return;
        }
        
        final String labelText;
        
        Lookup lkp = refactoring.getRefactoringSource();
        NonRecursiveFolder folder = lkp.lookup(NonRecursiveFolder.class);
        Collection<? extends FileObject> files = lkp.lookupAll(FileObject.class);
        final Collection<? extends TreePathHandle> handles = lkp.lookupAll(TreePathHandle.class);
        if (folder != null) {
            String pkgName = folder.getFolder().getNameExt().replace('/', '.');
            labelText = NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDelPkg", pkgName);
        }else if (files.size()>1 && files.size() == handles.size()) {
            //delete multiple files
            if (regulardelete) {
                labelText = NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDel_RegularDelete",handles.size());
            } else {
                labelText = NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDel_Classes",handles.size());
            }
        } else if (handles.size()>1) {
            Tree.Kind kind = null;
            for (TreePathHandle handle : handles) {
                if(kind == null || handle.getKind() == kind) {
                    kind = handle.getKind();
                } else {
                    kind = null;
                    break;
                }
            }
            if(kind == null) {
                labelText = NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDel_Elements",handles.size());
            } else {
                switch(kind) {
                    case CLASS:
                        labelText = NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDel_Classes",handles.size());
                        break;
                    case METHOD:
                        labelText = NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDel_Methods",handles.size());
                        break;
                    case VARIABLE:
                        labelText = NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDel_Variables",handles.size());
                        break;
                    default:
                        labelText = NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDel_Elements",handles.size());
                        break;
                }
            }
        } else if (handles.size()==1) {
          JavaSource s = JavaSource.forFileObject(handles.iterator().next().getFileObject());
          final String[] name = new String[1];
          try {
              s.runUserActionTask(new CancellableTask<CompilationController>() {
                    @Override
                  public void cancel() {
                  }
                  
                    @Override
                  public void run(CompilationController parameter) throws Exception {
                      parameter.toPhase(Phase.RESOLVED);
                      Element resolvedElement = handles.iterator().next().resolveElement(parameter);
                      if (resolvedElement == null) {
                          throw new NullPointerException(
                                  "Please attach your {nb.userdir}/var/log/messages.log" + // NOI18N
                                  " to http://www.netbeans.org/issues/show_bug.cgi?id=115462" + // NOI18N
                                  "\nhandle: " + handles.iterator().next() + // NOI18N
                                  "\nclasspath: " + parameter.getClasspathInfo()); // NOI18N
                      }
                      if (resolvedElement.getKind() == ElementKind.CONSTRUCTOR) {
                          name[0] = resolvedElement.getEnclosingElement().getSimpleName().toString();
                      } else {
                          name[0] = resolvedElement.getSimpleName().toString();
                      }
                  }
              }, true);
          } catch (IOException ioe) {
              throw new RuntimeException(ioe);
          }
          if (regulardelete) {
              labelText = NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDel_RegularDeleteElement",name[0]);
          } else {
              labelText = NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDel_Element",name[0]);
          }
        } else {
            FileObject fileObject = files.iterator().next();
            boolean isSingleFolderSelected = (files != null && files.size() == 1 
                    && fileObject.isFolder());
            if (isSingleFolderSelected && !regulardelete) {
                String folderName = fileObject.getName();
                labelText = NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDelFolder", folderName);
            }else{
                labelText ="";
            }
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (regulardelete) {
                    safeDelete = new JCheckBox();
                    Mnemonics.setLocalizedText(safeDelete, NbBundle.getMessage(SafeDeletePanel.class, "LBL_SafeDelCheckBox"));
                    safeDelete.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(
                            SafeDeletePanel.class,
                            "SafeDeletePanel.safeDelete.AccessibleContext.accessibleDescription"));
                    safeDelete.setMargin(new java.awt.Insets(2, 14, 2, 2));
                    searchInComments.setEnabled(false);
                    safeDelete.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent evt) {
                            searchInComments.setEnabled(safeDelete.isSelected());
                            parent.stateChanged(null);
                        }
                    });

                    checkBoxes.add(safeDelete, BorderLayout.CENTER);
                }
                label.setText(labelText);
                validate();
            }
        });
        initialized = true;
    }

    @Override
    public boolean requestFocusInWindow() {
        if(safeDelete != null) {
            safeDelete.requestFocusInWindow();
        } else {
            searchInComments.requestFocusInWindow();
        }
        return true;
    }
    
    boolean isRegularDelete() {
        if (safeDelete!=null) {
            return !safeDelete.isSelected();
        }
        return false;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        checkBoxes = new javax.swing.JPanel();
        label = new javax.swing.JLabel();
        searchInComments = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        checkBoxes.setLayout(new java.awt.BorderLayout());

        label.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        checkBoxes.add(label, java.awt.BorderLayout.NORTH);

        searchInComments.setSelected(((Boolean) RefactoringModule.getOption("searchInComments.whereUsed", Boolean.FALSE)).booleanValue());
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/java/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(searchInComments, bundle.getString("LBL_SafeDelInComents")); // NOI18N
        searchInComments.setMargin(new java.awt.Insets(2, 14, 2, 2));
        searchInComments.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                searchInCommentsItemStateChanged(evt);
            }
        });
        checkBoxes.add(searchInComments, java.awt.BorderLayout.SOUTH);
        searchInComments.getAccessibleContext().setAccessibleDescription(searchInComments.getText());

        add(checkBoxes, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents
    
    private void searchInCommentsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_searchInCommentsItemStateChanged
        // used for change default value for deleteInComments check-box.
        // The value is persisted and then used as default in next IDE run.
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption("searchInComments.whereUsed", b); // NOI18N
        refactoring.setCheckInComments(b.booleanValue());
    }//GEN-LAST:event_searchInCommentsItemStateChanged
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JPanel checkBoxes;
    private javax.swing.JLabel label;
    private javax.swing.JCheckBox searchInComments;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JCheckBox safeDelete;
    
    @Override
    public Dimension getPreferredSize() {
        Dimension orig = super.getPreferredSize();
        return new Dimension(orig.width + 30 , orig.height + 30);
    }
    
    /**
     * Indicates whether the element usage must be checked in comments
     * before deleting each element.
     * @return Returns the isSelected() attribute of the
     * underlying check box that controls search in comments
     */
    public boolean isSearchInComments() {
        return searchInComments.isSelected();
    }
    
    @Override
    public Component getComponent() {
        return this;
    }
}

