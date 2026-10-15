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

package org.netbeans.modules.java.api.common.project.ui.customizer;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.TypeElement;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.awt.Mnemonics;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;


/** Browses and allows to choose a project's main class.
 *
 * @author  Jiri Rechtacek
 */
public class MainClassChooser extends JPanel {

    private ChangeListener changeListener;
    private String dialogSubtitle = null;
    private Collection<ElementHandle<TypeElement>> possibleMainClasses;
            
    /** Creates new form MainClassChooser */
    public MainClassChooser (FileObject[] sourcesRoots) {
        this (sourcesRoots, null, null);
    }
    
    public MainClassChooser (
            final @NonNull FileObject[] sourcesRoots,
            final @NullAllowed String subtitle) {
        this (sourcesRoots, subtitle, null);
    }

    public MainClassChooser (
            final @NonNull FileObject[] sourcesRoots,
            final @NullAllowed String subtitle,
            final @NullAllowed String mainClass) {
        Parameters.notNull("sourceRoots", sourcesRoots);    //NOI18N
        dialogSubtitle = subtitle;
        initComponents();
        jMainClassList.setCellRenderer(new MainClassRenderer());
        initClassesView();
        initClassesModel(sourcesRoots, mainClass);
        scanningLabel.setVisible(false);
    }
    
    public MainClassChooser (final Collection<ElementHandle<TypeElement>> mainClassesInFile) {
        this(mainClassesInFile, null);
    }
    
    public MainClassChooser (final Collection<ElementHandle<TypeElement>> mainClassesInFile, final String subtitle) {
        assert mainClassesInFile != null;
        dialogSubtitle = subtitle;
        this.initComponents();
        jMainClassList.setCellRenderer(new MainClassRenderer());
        initClassesView();
        initClassesModel (mainClassesInFile);
    }
    
    private void initClassesView () {
        possibleMainClasses = null;
        jMainClassList.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        jMainClassList.setListData (getWarmupList ());
        jMainClassList.addListSelectionListener (new ListSelectionListener () {
            @Override
            public void valueChanged (ListSelectionEvent evt) {
                if (changeListener != null) {
                    changeListener.stateChanged (new ChangeEvent (evt));
                }
            }
        });
        // support for double click to finish dialog with selected class
        jMainClassList.addMouseListener (new MouseListener () {
            @Override
            public void mouseClicked (MouseEvent e) {
                if (MouseUtils.isDoubleClick (e)) {
                    if (getSelectedMainClass () != null) {
                        if (changeListener != null) {
                            changeListener.stateChanged (new ChangeEvent (e));
                        }
                    }
                }
            }
            @Override
            public void mousePressed (MouseEvent e) {}
            @Override
            public void mouseReleased (MouseEvent e) {}
            @Override
            public void mouseEntered (MouseEvent e) {}
            @Override
            public void mouseExited (MouseEvent e) {}
        });
        if (dialogSubtitle != null) {
            Mnemonics.setLocalizedText (jLabel1, dialogSubtitle);
        }
    }
    
    private class SearchTask implements Task<CompilationController> {
        final FileObject[] sourcesRoots;
        final String mainClass;
        final AtomicBoolean stillEnabled;
        boolean incomplete;

        public SearchTask(FileObject[] sourcesRoots, String mainClass, AtomicBoolean flag, boolean incomplete) {
            this.sourcesRoots = sourcesRoots;
            this.mainClass = mainClass;
            this.stillEnabled = flag;
            this.incomplete = incomplete;
        }
        
        @Override
        public void run(CompilationController parameter) throws Exception {
            // TODO-PERF: main class search may even work without attribution
            parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            possibleMainClasses = SourceUtils.getMainClasses(sourcesRoots);
            if (possibleMainClasses.isEmpty()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        jMainClassList.setListData(new String[]{NbBundle.getMessage(MainClassChooser.class, "LBL_ChooseMainClass_NO_CLASSES_NODE")}); // NOI18N
                        scanningLabel.setVisible(incomplete);
                    }
                });
            } else {
                final List<ElementHandle<TypeElement>> sortedMainClasses = new ArrayList<ElementHandle<TypeElement>>(possibleMainClasses);
                // #46861, sort name of classes
                sortedMainClasses.sort(new MainClassComparator());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        jMainClassList.setListData(sortedMainClasses.toArray());
                        int index = getMainClassIndex(sortedMainClasses, mainClass);
                        jMainClassList.setSelectedIndex(index);                                
                        scanningLabel.setVisible(incomplete);
                    }
                });
            }
        }
    }
    
    private void initClassesModel (
            final FileObject[] sourcesRoots,
            final String mainClass) {
        final ClasspathInfo cpInfo = ClasspathInfo.create(ClassPathSupport.createClassPath(new URL[0]),
                                                          ClassPathSupport.createClassPath(new URL[0]),
                                                          ClassPathSupport.createClassPath(new URL[0]));
        final JavaSource dummyJs = JavaSource.create(cpInfo);
        final AtomicBoolean flag = new AtomicBoolean(true);
        
        SearchTask afterScan = new SearchTask(sourcesRoots, mainClass, flag, false) {

            @Override
            public void run(CompilationController parameter) throws Exception {
                stillEnabled.set(false);
                super.run(parameter);
            }
        };
        
        try {
            Future<Void> task = dummyJs.runWhenScanFinished(afterScan, true);
            if (task.isDone()) {
                // scanning done, info updated & up-to-date
                return;
            }
            task.cancel(true);
            dummyJs.runUserActionTask(new SearchTask(sourcesRoots, mainClass, flag, true) {
                @Override
                public void run(CompilationController parameter) throws Exception {
                    if (!stillEnabled.getAndSet(false)) {
                        // do not execute if the after-scan task was already executed
                        return;
                    }
                    super.run(parameter);
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void initClassesModel (final Collection<ElementHandle<TypeElement>> mainClasses) {
        final List<ElementHandle<TypeElement>> sortedMainClasses = new ArrayList<ElementHandle<TypeElement>>(mainClasses);
        sortedMainClasses.sort(new MainClassComparator());
        possibleMainClasses = mainClasses;
        jMainClassList.setListData(sortedMainClasses.toArray());
        jMainClassList.setSelectedIndex (0);
        scanningLabel.setVisible(false);
    }
    
    private Object[] getWarmupList () {        
//        return JMManager.getManager().isScanInProgress() ?
//            new Object[] {NbBundle.getMessage (MainClassChooser.class, "LBL_ChooseMainClass_SCANNING_MESSAGE")}:
//            new Object[] {NbBundle.getMessage (MainClassChooser.class, "LBL_ChooseMainClass_WARMUP_MESSAGE")}; // NOI18N
          return new Object[] {NbBundle.getMessage (MainClassChooser.class, "LBL_ChooseMainClass_WARMUP_MESSAGE")};
    }
    
    @SuppressWarnings("element-type-mismatch")
    private boolean isValidMainClassName (Object value) {
        return (possibleMainClasses != null) && (possibleMainClasses.contains (value));
    }


    /** Returns the selected main class.
     *
     * @return a binary name of class or null if no class with the main method is selected
     */    
    @SuppressWarnings("unchecked")
    public String getSelectedMainClass () {
        ElementHandle<TypeElement> te = null;
        if (isValidMainClassName (jMainClassList.getSelectedValue ())) {
            te = (ElementHandle<TypeElement>)jMainClassList.getSelectedValue();
        }
        return te == null ? null : te.getBinaryName();
    }
    
    public void addChangeListener (ChangeListener l) {
        changeListener = l;
    }
    
    public void removeChangeListener (ChangeListener l) {
        changeListener = null;
    }
    
    // Used only from unit tests to suppress check of main method. If value
    // is different from null it will be returned instead.
    public static Boolean unitTestingSupport_hasMainMethodResult = null;
    
    /** Checks if given file object contains the main method.
     *
     * @param classFO file object represents java 
     * @return false if parameter is null or doesn't contain SourceCookie
     * or SourceCookie doesn't contain the main method
     */    
    public static boolean hasMainMethod (FileObject classFO) {
        return CommonProjectUtils.hasMainMethod (classFO);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jMainClassList = new javax.swing.JList();
        scanningLabel = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(380, 300));
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(jMainClassList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getBundle(MainClassChooser.class).getString("CTL_AvaialableMainClasses")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 2, 12);
        add(jLabel1, gridBagConstraints);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 200));
        jScrollPane1.setViewportView(jMainClassList);
        jMainClassList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(MainClassChooser.class).getString("AD_jMainClassList")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(jScrollPane1, gridBagConstraints);

        scanningLabel.setFont(new java.awt.Font("Dialog", 2, 12)); // NOI18N
        scanningLabel.setText(org.openide.util.NbBundle.getBundle(MainClassChooser.class).getString("LBL_ChooseMainClass_SCANNING_MESSAGE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(scanningLabel, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(MainClassChooser.class).getString("AD_MainClassChooser")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jMainClassList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel scanningLabel;
    // End of variables declaration//GEN-END:variables

    private static int getMainClassIndex(final List<? extends ElementHandle<TypeElement>> handles, final String mainClass) {
        if (mainClass != null) {
            final Iterator<? extends ElementHandle<TypeElement>> it = handles.iterator();
            for (int index=0; it.hasNext() ;index++) {
                if (mainClass.equals(it.next().getQualifiedName())) {
                    return index;
                }
            }
        }
        return 0;
    }

    private static final class MainClassRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String displayName;
            if (value instanceof String) {
                displayName = (String) value;
            } if (value instanceof ElementHandle) {
                displayName = ((ElementHandle)value).getQualifiedName();
            } else {
                displayName = value.toString ();
            }
            return super.getListCellRendererComponent (list, displayName, index, isSelected, cellHasFocus);
        }
    }
    
    private static class MainClassComparator implements Comparator<ElementHandle> {
            
        public int compare(ElementHandle arg0, ElementHandle arg1) {
            return arg0.getQualifiedName().compareTo(arg1.getQualifiedName());
        }
    }

}
