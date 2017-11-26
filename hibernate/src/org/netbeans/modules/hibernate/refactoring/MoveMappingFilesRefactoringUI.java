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
package org.netbeans.modules.hibernate.refactoring;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.explorer.view.NodeRenderer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * 
 * @author Dongmei Cao
 */
final class MoveMappingFilesRefactoringUI implements RefactoringUI, RefactoringUIBypass {

    private MoveRefactoring refactoring;
    private FileObject[] mappingFileObjects;
    private MovePanel panel;
    private boolean disable;
    private FileObject targetFolder;
    private PasteType pasteType;

    /**
     * 
     * @param pageFileObject 
     * @param targetFolder 
     * @param pasteType 
     */
    public MoveMappingFilesRefactoringUI(FileObject[] mappingFileObjects, FileObject targetFolder, PasteType pasteType) {
        refactoring = new MoveRefactoring(Lookups.fixed((Object[]) mappingFileObjects));
        this.disable = targetFolder != null;
        this.targetFolder = targetFolder;
        this.mappingFileObjects = mappingFileObjects;
        this.pasteType = pasteType;
    }

    public String getName() {
        return NbBundle.getMessage(MoveMappingFilesRefactoringUI.class, "LBL_Move"); // NOI18N
    }

    public String getDescription() {
        return getName();
    }

    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        
        if(panel==null) {
        String pkgName = null;
        if (targetFolder != null) {
            ClassPath cp = ClassPath.getClassPath(targetFolder, ClassPath.SOURCE);
            if (cp != null) {
                pkgName = cp.getResourceName(targetFolder, '.', false);
            }
        }
        
        String headline = NbBundle.getMessage(MoveMappingFilesRefactoringUI.class, "DSC_MoveMappingFiles");
        if(mappingFileObjects.length == 1 ) {
            headline = NbBundle.getMessage(MoveMappingFilesRefactoringUI.class, "DSC_MoveOneMappingFile", mappingFileObjects[0].getName());
        }
        panel = new MovePanel(parent,
                pkgName != null ? pkgName : getDOPackageName(((FileObject) mappingFileObjects[0]).getParent()),
                headline );
        }
        return panel;
    }

    private static String getDOPackageName(FileObject f) {
        ClassPath cp = ClassPath.getClassPath(f, ClassPath.SOURCE);
        if (cp != null) {
            return cp.getResourceName(f, '.', false);
        } else {
            return f.getName();
        }
    }

    public boolean hasParameters() {
        return true;
    }

    public Problem checkParameters() {
        return setParameters(true);
    }

    public Problem setParameters() {
        return setParameters(false);
    }

    private Problem setParameters(boolean checkOnly) {
        if (panel == null) {
            return null;
        }

        URL url = URLMapper.findURL(panel.getRootFolder(), URLMapper.EXTERNAL);
        try {
            refactoring.setTarget(Lookups.singleton(new URL(url.toExternalForm() + URLEncoder.encode(panel.getPackageName().replace('.', '/'), "utf-8")))); // NOI18N
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        if (checkOnly) {
            return refactoring.fastCheckParameters();
        } else {
            return refactoring.checkParameters();
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(MoveMappingFilesRefactoringUI.class);
    }

    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public boolean isRefactoringBypassRequired() {
        return !panel.isUpdateReferences();
    }

    public void doRefactoringBypass() throws IOException {
        pasteType.paste();
    }

    private final Vector getNodes() {
        Vector<Node> result = new Vector<Node>(mappingFileObjects.length);
        LinkedList<FileObject> q = new LinkedList<FileObject>(Arrays.asList(mappingFileObjects));
        while (!q.isEmpty()) {
            FileObject f = q.removeFirst();
            if (!VisibilityQuery.getDefault().isVisible(f)) {
                continue;
            }
            DataObject d = null;
            try {
                d = DataObject.find(f);
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            }
            if (d instanceof DataFolder) {
                for (DataObject o : ((DataFolder) d).getChildren()) {
                    q.addLast(o.getPrimaryFile());
                }
            } else {
                result.add(d.getNodeDelegate());
            }
        }
        return result;
    }

    class MovePanel extends MoveMappingFilePanel {

        public MovePanel(final ChangeListener parent, String startPackage, String headLine) {
            super(parent, startPackage, headLine, targetFolder != null ? targetFolder : (FileObject) mappingFileObjects[0]);
            
            // Add a list to list the selected mapping files
            if (mappingFileObjects.length > 1) {
                setCombosEnabled(!disable);
                JList list = new JList(getNodes());
                list.setCellRenderer(new NodeRenderer());
                list.setVisibleRowCount(5);
                JScrollPane pane = new JScrollPane(list);
                bottomPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
                bottomPanel.setLayout(new BorderLayout());
                bottomPanel.add(pane, BorderLayout.CENTER);
                JLabel listOf = new JLabel();
                Mnemonics.setLocalizedText(listOf, NbBundle.getMessage(MovePanel.class, "LBL_ListOfMappingFiles"));
                bottomPanel.add(listOf, BorderLayout.NORTH);
            }
        }
    }
}
