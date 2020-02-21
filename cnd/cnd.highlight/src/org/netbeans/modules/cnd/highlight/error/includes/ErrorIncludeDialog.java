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

package org.netbeans.modules.cnd.highlight.error.includes;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnitInterface;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.Dwarf.CompilationUnitIterator;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 */
public class ErrorIncludeDialog extends JPanel implements CsmModelListener {
    private static final boolean TRACE_ERROR_STATISTIC = Boolean.getBoolean("cnd.highlight.trace.statistic"); // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(ErrorIncludeDialog.class.getName(), 1);
    private CsmProject baseProject;
    private Dialog parent;
    private AtomicBoolean closed = new AtomicBoolean(false);

    public ErrorIncludeDialog(Set<CsmUID<CsmFile>> set) {
        createComponents(set);
        setPreferredSize(new Dimension(NbPreferences.forModule(ErrorIncludeDialog.class).getInt("dialogSizeW", 500), // NOI18N
                                       NbPreferences.forModule(ErrorIncludeDialog.class).getInt("dialogSizeH", 240))); // NOI18N
        setMinimumSize(new Dimension(320, 240));
        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if (e.getChangeFlags() == HierarchyEvent.SHOWING_CHANGED) {
                    if (!e.getChanged().isVisible()){
                        closed.set(true);
                        leftList.setModel(new DefaultListModel());
                        rightList.setModel(new DefaultListModel());
                        model = null;
                        baseProject = null;
                        parent = null;
                        if (searchBase != null) {
                            searchBase.clear();
                        }
                        CsmListeners.getDefault().removeModelListener(ErrorIncludeDialog.this);
                        NbPreferences.forModule(ErrorIncludeDialog.class).putInt("dialogSizeW", getSize().width); // NOI18N
                        NbPreferences.forModule(ErrorIncludeDialog.class).putInt("dialogSizeH", getSize().height); // NOI18N
                    }
                }
            }
        });
    }

    @Override
    public void projectOpened(CsmProject project) {
    }

    @Override
    public void projectClosed(CsmProject project) {
        if (project == baseProject) {
            if (parent !=  null) {
                parent.setVisible(false);
            }
        }
    }

    @Override
    public void modelChanged(CsmChangeEvent e) {
    }
    
    public static void showErrorIncludeDialog(Set<CsmUID<CsmFile>> set) {
        ErrorIncludeDialog errors = new ErrorIncludeDialog(set);
        DialogDescriptor descriptor = new DialogDescriptor(errors, i18n("ErrorIncludeDialog_Title"), // NOI18N
                false, new Object[]{DialogDescriptor.CLOSED_OPTION}, DialogDescriptor.CLOSED_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setVisible(true);
        errors.parent = dlg;
        CsmListeners.getDefault().addModelListener(errors);
    }
    
    private void createComponents(final Set<CsmUID<CsmFile>> set) {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(createIncludesPane(), c);
        getAccessibleContext().setAccessibleName(i18n("ErrorIncludeDialog_AccessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(i18n("ErrorIncludeDialog_AccessibleDescription")); // NOI18N
        DefaultListModel wait = new DefaultListModel();
        wait.add(0, i18n("MSG_Loading")); // NOI18N
        leftList.setModel(wait);
        RP.post(new Runnable(){
           @Override
             public void run() {
                 computeList(set);
             }
        });
    }
    
    private void computeList(Set<CsmUID<CsmFile>> set) {
        Set<CsmFile> files = new HashSet<>();
        if (set != null) {
            for (CsmUID<CsmFile> fileUID : set) {
                CsmFile csmFile = fileUID.getObject();
                assert csmFile != null;
                if (csmFile != null) {
                    files.add(csmFile);
                }
            }
        }

        List<CsmInclude> includes = new ArrayList<>();
        List<CsmErrorDirective> errors = new ArrayList<>();
        CsmFileInfoQuery fiq = CsmFileInfoQuery.getDefault();
        for(CsmFile file:files){
            Collection<CsmErrorDirective> fileErrors = file.getErrors();
            boolean hasFailed = !fileErrors.isEmpty();
            errors.addAll(fileErrors);
            for(CsmInclude incl : fiq.getBrokenIncludes(file)) {
                if (incl.getIncludeFile() == null){
                    includes.add(incl);
                    hasFailed = true;
                }
            }
            if (baseProject == null){
                baseProject = file.getProject();
            }
            if (!hasFailed && TRACE_ERROR_STATISTIC) {
                System.out.println("File marked as failed does not contain failed directives:"); // NOI18N
                System.out.println("  "+file.getAbsolutePath()); // NOI18N
            }
        }
        if (closed.get()) {
            return;
        }
        if (baseProject != null && TRACE_ERROR_STATISTIC) {
            checkHighlightModel(files);
        }
        if (closed.get()) {
            return;
        }
        ErrorIncludesModel aModel = new ErrorIncludesModel(includes, errors);
        if (closed.get()) {
            return;
        }
        model = aModel;
        if (TRACE_ERROR_STATISTIC) {
            printStatistic();
        }
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                if (closed.get()) {
                    return;
                }
                leftList.setModel(model);
                addListeners();
            }
        });
    }

    private void checkHighlightModel(Set<CsmFile> files){
        CsmProject aBaseProject = baseProject;
        if (aBaseProject == null) {
            return;
        }
        CsmFileInfoQuery fiq = CsmFileInfoQuery.getDefault();
        for(Object f : aBaseProject.getSourceFiles()){
            CsmFile file = (CsmFile) f;
            boolean failed = fiq.hasBrokenIncludes(file);
            if (failed){
                if (!files.contains(file)) {
                    System.out.println("Project source file is failed and not found in highlight"); // NOI18N
                    System.out.println("  "+file.getAbsolutePath()); // NOI18N
                }
            } else {
                if (files.contains(file)) {
                    System.out.println("Project source file is not failed and found in highlight"); // NOI18N
                    System.out.println("  "+file.getAbsolutePath()); // NOI18N
                }
            }
        }
        for(Object f :aBaseProject.getHeaderFiles()){
            CsmFile file = (CsmFile) f;
            boolean failed = fiq.hasBrokenIncludes(file);
            if (failed){
                if (!files.contains(file)) {
                    System.out.println("Project header file is failed and not found in highlight"); // NOI18N
                    System.out.println("  "+file.getAbsolutePath()); // NOI18N
                }
            } else {
                if (files.contains(file)) {
                    System.out.println("Project header file is not failed and found in highlight"); // NOI18N
                    System.out.println("  "+file.getAbsolutePath()); // NOI18N
                }
            }
        }
    }
    
    private void printStatistic(){
        CsmProject aBaseProject = baseProject;
        if (aBaseProject == null) {
            return;
        }
        int files = 0;
        int directives = 0;
        int failedDirectives = 0;
        int failedFiles = 0;
        for(Object f : aBaseProject.getSourceFiles()){
            CsmFile file = (CsmFile) f;
            files++;
            boolean failed = false;
            for (CsmInclude directive : file.getIncludes()){
                if (directive.getIncludeFile()==null){
                    failedDirectives++;
                    failed = true;
                }
                directives++;
            }
            if (failed){
                failedFiles++;
            }
        }
        for(Object f :aBaseProject.getHeaderFiles()){
            CsmFile file = (CsmFile) f;
            files++;
            boolean failed = false;
            for (CsmInclude directive : file.getIncludes()){
                if (directive.getIncludeFile()==null){
                    failedDirectives++;
                    failed = true;
                }
                directives++;
            }
            if (failed){
                failedFiles++;
            }
        }
        System.out.println("*Model #includes statistic*"); // NOI18N
        System.out.println("  Amount of #includes:"+directives); // NOI18N
        System.out.println("  Failed    #includes:"+failedDirectives); // NOI18N
        System.out.println("  Amount     of files:"+files); // NOI18N
        System.out.println("  Failed        files:"+failedFiles); // NOI18N
        if (directives>0) {
            double metric = 100.0 * (directives-failedDirectives) / directives;
            System.out.println("  Resolve #include Accuracy:"+metric+"%"); // NOI18N
            metric = 100.0 * (files-failedFiles) / files;
            System.out.println("  File-based       Accuracy:"+metric+"%"); // NOI18N
        }
        Object o = aBaseProject.getPlatformProject();
        if (o instanceof NativeProject){
            files = 0;
            NativeProject nativeProject = (NativeProject) o;
            for(NativeFileItem item : nativeProject.getAllFiles()){
                if (!item.isExcluded()) {
                    switch(item.getLanguage()){
                        case C:
                        case CPP:
                        case C_HEADER:
                            files++;
                            break;
                        default:
                            break;
                    }
                }
            }
            System.out.println("*Details for project statistic*"); // NOI18N
            System.out.println("  Amount of native project files:"+files); // NOI18N
            ErrorIncludesModel aModel = model;
            if (aModel != null) {
                System.out.println("  Failed   highlight   #includes:"+aModel.getFailedIncludesSize()); // NOI18N
                System.out.println("  Failed   highlight      files:"+aModel.getFailedFilesSize()); // NOI18N
            }
        }
    }
    
    private ErrorIncludesModel model;
    private JList leftList;
    private JList rightList;
    private JEditorPane guessList;
    private Map<String,List<FileObject>> searchBase;
    private JComponent createIncludesPane(/*List<CsmInclude> includes*/) {
        leftList = new JList();
        leftList.setBorder(BorderFactory.createEmptyBorder());
        leftList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        rightList = new JList();
        rightList.setBorder(BorderFactory.createEmptyBorder());
        rightList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        guessList = new JEditorPane();
        guessList.setContentType("text/html");  // NOI18N
        guessList.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        guessList.setEditable(false);
        
        JSplitPane pane = new JSplitPane();
        pane.setResizeWeight(0.5);
        pane.setOneTouchExpandable(true);
        
        JPanel p;
        JLabel l;
        GridBagConstraints c;
        
        JScrollPane leftScroller = new JScrollPane(leftList);
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        l = new JLabel();
        l.setLabelFor(leftList);
        Mnemonics.setLocalizedText(l, i18n("ErrorIncludeDialog_TitleInclides")); // NOI18N
        l.setToolTipText(i18n("ErrorIncludeDialog_AccessibleNameInclides")); // NOI18N
        l.getAccessibleContext().setAccessibleName(i18n("ErrorIncludeDialog_AccessibleNameInclides")); // NOI18N
        l.getAccessibleContext().setAccessibleDescription(i18n("ErrorIncludeDialog_AccessibleDescriptionInclides")); // NOI18N
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.SOUTHWEST;
        c.insets = new Insets(5, 6, 5, 5);
        p.add(l, c);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(1, 6, 5, 5);
        p.add(leftScroller, c);
        pane.setLeftComponent(p);
        
        JSplitPane vertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        vertical.setResizeWeight(0.5);
        vertical.setOneTouchExpandable(true);
        
        JScrollPane rightTopScroller = new JScrollPane(rightList);
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        l = new JLabel();
        l.setLabelFor(rightList);
        Mnemonics.setLocalizedText(l, i18n("ErrorIncludeDialog_TitleFiles"));// NOI18N
        l.setToolTipText(i18n("ErrorIncludeDialog_AccessibleNameFiles")); // NOI18N
        l.getAccessibleContext().setAccessibleName(i18n("ErrorIncludeDialog_AccessibleNameFiles")); // NOI18N
        l.getAccessibleContext().setAccessibleDescription(i18n("ErrorIncludeDialog_AccessibleDescriptionFiles")); // NOI18N
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.SOUTHWEST;
        c.insets = new Insets(5, 6, 5, 5);
        p.add(l, c);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(1, 6, 5, 5);
        p.add(rightTopScroller, c);
        vertical.setTopComponent(p);
        
        JScrollPane rightBottomScroller = new JScrollPane(guessList);
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        l = new JLabel();
        l.setLabelFor(guessList);
        Mnemonics.setLocalizedText(l, i18n("ErrorIncludeDialog_TitleGuess"));// NOI18N
        l.setToolTipText(i18n("ErrorIncludeDialog_AccessibleNameGuess")); // NOI18N
        l.getAccessibleContext().setAccessibleName(i18n("ErrorIncludeDialog_AccessibleNameGuess")); // NOI18N
        l.getAccessibleContext().setAccessibleDescription(i18n("ErrorIncludeDialog_AccessibleDescriptionGuess")); // NOI18N
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.SOUTHWEST;
        c.insets = new Insets(5, 6, 5, 5);
        p.add(l, c);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(1, 6, 5, 5);
        p.add(rightBottomScroller, c);
        vertical.setBottomComponent(p);
        
        pane.setRightComponent(vertical);

        vertical.addHierarchyListener(new HierarchyListener() {
            private boolean show;
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) == HierarchyEvent.SHOWING_CHANGED){
                    JSplitPane p = (JSplitPane)e.getSource();
                    if (show) {
                        int l1 = p.getTopComponent().getHeight();
                        int l2 = p.getBottomComponent().getHeight();
                        if (l1 > 0 && l2 > 0) {
                            NbPreferences.forModule(ErrorIncludeDialog.class).putDouble("verticalDivider", ((double)l1)/(l1+l2)); // NOI18N
                        }
                    } else {
                        p.setDividerLocation(NbPreferences.forModule(ErrorIncludeDialog.class).getDouble("verticalDivider", 0.65)); // NOI18N
                        show = true;
                    }
                }
            }
        });

        pane.addHierarchyListener(new HierarchyListener() {
            private boolean show;
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) == HierarchyEvent.SHOWING_CHANGED){
                    JSplitPane p = (JSplitPane)e.getSource();
                    if (show) {
                        int l1 = p.getLeftComponent().getWidth();
                        int l2 = p.getRightComponent().getWidth();
                        if (l1 > 0 && l2 > 0) {
                            NbPreferences.forModule(ErrorIncludeDialog.class).putDouble("horisontalDivider", ((double)l1)/(l1+l2)); // NOI18N
                        }
                    } else {
                        p.setDividerLocation(NbPreferences.forModule(ErrorIncludeDialog.class).getDouble("horisontalDivider", 0.35)); // NOI18N
                        show = true;
                    }
                }
            }
        });
        
        return pane;
    }
    
    private void addListeners(){
        leftList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selected = leftList.getSelectedIndex();
                    if (selected >=0){
                        List<CsmOffsetable> errors;
                        if (baseProject != null && baseProject.isValid()) {
                            errors = model.getElementList(selected);
                        } else {
                            errors = Collections.<CsmOffsetable>emptyList();
                        }
                        ErrorFilesModel m = new ErrorFilesModel(errors);
                        rightList.setModel(m);
                        if (errors.size()>0) {
                            rightList.setSelectedIndex(0);
                            rightList.invalidate();
                            rightList.repaint();
                            CsmOffsetable error = m.getFailedDirective(0);
                            guessList.setText(NbBundle.getMessage(ErrorIncludeDialog.class, "MSG_Loading"));
                            guess(error, (String)model.getElementAt(selected));
                        } else {
                            guessList.setText("");
                        }
                    }
                }
            }
        });
        leftList.setSelectedIndex(0);
        
        rightList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selected = rightList.getSelectedIndex();
                    if (selected >=0 && baseProject != null && baseProject.isValid()){
                        ErrorFilesModel m = (ErrorFilesModel)rightList.getModel();
                        CsmOffsetable error = m.getFailedDirective(selected);
                        guessList.setText(NbBundle.getMessage(ErrorIncludeDialog.class, "MSG_Loading"));
                        guess(error, (String)model.getElementAt(leftList.getSelectedIndex()));
                    } else {
                        guessList.setText("");
                    }
                }
            }
        });
        
        leftList.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                int selected = rightList.getSelectedIndex();
                if (e.getClickCount()==2 && selected >= 0){
                    openElement(selected);
                }
            }
        });
        
        leftList.addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
            }
            @Override
            public void keyPressed(KeyEvent e) {
            }
            @Override
            public void keyReleased(KeyEvent e) {
                int selected = rightList.getSelectedIndex();
                if (e.getModifiers()==0 && e.getKeyCode()==KeyEvent.VK_SPACE && selected >= 0){
                    openElement(selected);
                }
            }
        });
        
        rightList.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                int selected = rightList.getSelectedIndex();
                if (e.getClickCount()==2 && selected >= 0){
                    openElement(selected);
                }
            }
        });
        
        rightList.addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
            }
            @Override
            public void keyPressed(KeyEvent e) {
            }
            @Override
            public void keyReleased(KeyEvent e) {
                int selected = rightList.getSelectedIndex();
                if (e.getModifiers()==0 && e.getKeyCode()==KeyEvent.VK_SPACE && selected >= 0){
                    openElement(selected);
                }
            }
        });
    }
    
    private void openElement(final int selected){
        if (baseProject != null && baseProject.isValid()) {
            ListModel lm = rightList.getModel();
            if (lm instanceof ErrorFilesModel) {
                ErrorFilesModel m = (ErrorFilesModel)lm;
                final CsmOffsetable error = m.getFailedDirective(selected);
                Runnable run = new Runnable() {

                    @Override
                    public void run() {
                        CsmUtilities.openSource(error);
                    }
                };
                CsmModelAccessor.getModel().enqueue(run, "Open include"); //NOI18N
            }
        }
    }
    
    private RequestProcessor.Task guessTask = null;
    private void guess(final CsmOffsetable error, final String item) {
        Runnable worker = new Runnable() {

            @Override
            public void run() {
                final StringBuilder buf = new StringBuilder();
                String found = item;
                boolean handleIncludeError = CsmKindUtilities.isInclude(error);
                if (handleIncludeError) {
                    if (searchBase == null) {
                        searchBase = search(error);
                    }
                    found = found.replace("<", ""); // NOI18N
                    found = found.replace(">", ""); // NOI18N
                    found = found.replace("\"", ""); // NOI18N
                    found = found.replace("\\", "/"); // NOI18N
                    if (found.indexOf('/') >= 0) {
                        found = found.substring(found.lastIndexOf('/') + 1);
                    }
                    List<FileObject> result = searchBase.get(found);
                    if (result != null) {
                        for (Iterator<FileObject> it = result.iterator(); it.hasNext();) {
                            FileObject elem = it.next();
                            buf.append(elem.getPath()).append("\n<br>"); // NOI18N
                        }
                    }
                } else {
                    buf.append(error.getText()).append("\n<br>"); // NOI18N
                }
                
                CsmFile file = error.getContainingFile();
                if (file != null) {
                    if (handleIncludeError) {
                        getObjectFile(found, file.getAbsolutePath().toString(), buf);
                    }
                    if (file.isHeaderFile()) {
                        List<CsmInclude> list = Collections.emptyList();
                        if (handleIncludeError) {
                            list = CsmFileInfoQuery.getDefault().getIncludeStack(file);
                        } else if (CsmKindUtilities.isErrorDirective(error)) {
                            list = CsmFileInfoQuery.getDefault().getIncludeStack((CsmErrorDirective)error);
                        }
                        if (!list.isEmpty()) {
                            buf.append(i18n("PathToHeader"));  // NOI18N
                            file = list.get(0).getContainingFile();
                            for (CsmInclude inc : list) {
                                buf.append("\n<br>&nbsp;&nbsp;&nbsp;&nbsp;");  // NOI18N
                                buf.append(inc.getContainingFile().getAbsolutePath());
                                buf.append(i18n("PathToHeaderLine", inc.getStartPosition().getLine()));  // NOI18N
                            }
                            buf.append("\n<br>"); // NOI18N
                        } else {
                            file = null;
                        }
                    }
                    List<IncludePath> list = CsmFileInfoQuery.getDefault().getUserIncludePaths(file);
                    if (list.size() > 0) {
                        buf.append(i18n("SourceUserPaths"));  // NOI18N
                        for (IncludePath fsPath : list) {
                            buf.append("\n<br>&nbsp;&nbsp;&nbsp;&nbsp;");  // NOI18N
                            FileObject fo = fsPath.getFSPath().getFileObject();
                            if (fo != null && fo.isValid() && fo.isFolder()) {
                                buf.append(fsPath.getFSPath().getPath());
                            } else if (fo != null && fo.isValid() && fo.isData()) {
                                buf.append("<font color='green'>");  // NOI18N
                                buf.append(fsPath.getFSPath().getPath());
                                buf.append("</font>");  // NOI18N
                            } else {
                                buf.append("<font color='red'>");  // NOI18N
                                buf.append(fsPath.getFSPath().getPath());
                                buf.append("</font>");  // NOI18N
                            }
                        }
                        buf.append("\n<br>"); // NOI18N
                    }
                    list = CsmFileInfoQuery.getDefault().getSystemIncludePaths(file);
                    if (list.size() > 0) {
                        buf.append(i18n("SourceSystemPaths"));  // NOI18N
                        for (IncludePath fsPath : list) {
                            buf.append("\n<br>&nbsp;&nbsp;&nbsp;&nbsp;");  // NOI18N
                            FileObject fo = fsPath.getFSPath().getFileObject();
                            if (fo != null && fo.isValid() && fo.isFolder()) {
                                buf.append(fsPath.getFSPath().getPath());
                            } else if (fo != null && fo.isValid() && fo.isData()) {
                                buf.append("<font color='green'>");  // NOI18N
                                buf.append(fsPath.getFSPath().getPath());
                                buf.append("</font>");  // NOI18N
                            } else {
                                buf.append("<font color='red'>");  // NOI18N
                                buf.append(fsPath.getFSPath().getPath());
                                buf.append("</font>");  // NOI18N
                            }
                        }
                        buf.append("\n<br>"); // NOI18N
                    }
                }
                //System.err.println(guessList.getText());
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        guessList.setText("<html><head></head><body>" + buf.toString() + "</body></html>"); // NOI18N
                    }
                });
            }
        };
        if (guessTask != null) {
            guessTask.cancel();
            guessTask = null;
        }
        guessTask = RP.post(worker);
    }
    
    private void getObjectFile(String searchFor, String in, StringBuilder buf){
        String source = in.replace("<",""); // NOI18N
        source = source.replace(">",""); // NOI18N
        source = source.replace("\"",""); // NOI18N
        source = source.replace("\\","/"); // NOI18N
        if(source.indexOf('/')>=0){
            source = source.substring(source.lastIndexOf('/')+1);
        }
        if (source.lastIndexOf('.')>0){
            source = source.substring(0,source.lastIndexOf('.'))+".o";  // NOI18N
            List<FileObject> result = searchBase.get(source);
            if (result != null){
                for (Iterator<FileObject> it = result.iterator(); it.hasNext();) {
                    FileObject elem = it.next();
                    buf.append(elem.getPath()).append("\n<br>"); // NOI18N
                    String path = trace(searchFor, elem, in);
                    if (path != null){
                        buf.append(path).append("\n<br>"); // NOI18N
                    }
                }
            }
        }
    }
    
    private String trace(String found, FileObject objFileName, String unit){
        if (FileSystemProvider.getExecutionEnvironment(objFileName).isRemote()) {
            //TODO check remote
            return null;
        }
        Dwarf dump = null;
        try {
            dump = new Dwarf(objFileName.getPath());
            CompilationUnitIterator units = dump.iteratorCompilationUnits();
            if (units.hasNext()){
                CompilationUnitInterface cu = units.next();
                String fullName = getRightName(cu.getSourceFileAbsolutePath());
                if (unit.equals(fullName)){
                    if (cu instanceof CompilationUnit) {
                        List<String> includes = ((CompilationUnit)cu).getStatementList().getPathsForFile(found);
                        if (includes.size()>0){
                            String path = getRightName(cu.getCompilationDir());
                            String message = i18n("HeaderFromBinary");  // NOI18N
                            return MessageFormat.format(message, new Object[]{
                                path,fullName,includes.get(0)
                            });
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            // Skip exception
        } catch (WrongFileFormatException ex) {
            // Skip exception
        } catch (IOException ex) {
            // Skip exception
            //ex.printStackTrace();
        } catch (Throwable ex) {
            // Skip exception
            //ex.printStackTrace();
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        return null;
    }

    private static final String CYG_DRIVE_UNIX = "/cygdrive/"; // NOI18N
    private static final String CYG_DRIVE_WIN = "\\cygdrive\\"; // NOI18N
    private String fixFileName(String fileName) {
        if (fileName != null && Utilities.isWindows()) {
            if (fileName.startsWith(CYG_DRIVE_UNIX)) {
                fileName = fileName.substring(CYG_DRIVE_UNIX.length()); // NOI18N
                fileName = "" + Character.toUpperCase(fileName.charAt(0)) + ':' + fileName.substring(1); // NOI18N
            } else {
                int i = fileName.indexOf(CYG_DRIVE_WIN);
                if (i > 0) {
                    fileName = fileName.substring(i+CYG_DRIVE_UNIX.length());
                    fileName = "" + Character.toUpperCase(fileName.charAt(0)) + ':' + fileName.substring(1); // NOI18N
                }
            }
            fileName = fileName.replace('/', '\\');
        }
        return fileName;
    }
    
    private String getRightName(String fullName){
        fullName = CndFileUtils.normalizeAbsolutePath(fullName);
        fullName = fixFileName(fullName);
        return fullName;
    }

    private Map<String,List<FileObject>> search(CsmOffsetable ppDirective){
        CsmProject prj = ppDirective.getContainingFile().getProject();
        HashSet<FileObject> set = new HashSet<>();
        for (Iterator<CsmFile> it = prj.getSourceFiles().iterator(); it.hasNext();){
            CsmFile file = it.next();
            FileObject fo = file.getFileObject();
            if (fo != null && fo.isValid()) {
                FileObject aParent = fo.getParent();
                if (aParent != null && aParent.isValid()) {
                    set.add(aParent);
                }
            }
        }
        for (Iterator<CsmFile> it = prj.getHeaderFiles().iterator(); it.hasNext();){
            CsmFile file = it.next();
            FileObject fo = file.getFileObject();
            if (fo != null && fo.isValid()) {
                FileObject aParent = fo.getParent();
                if (aParent != null && aParent.isValid()) {
                    set.add(aParent);
                }
            }
        }
        HashMap<String,List<FileObject>> map = new HashMap<>();
        for (Iterator<FileObject> it = set.iterator(); it.hasNext();){
            FileObject d = it.next();
            if (d.isValid() && d.isFolder() && d.canRead()){
                FileObject[] ff = d.getChildren();
                if (ff != null) {
                    for (int i = 0; i < ff.length; i++) {
                        if (ff[i].isValid() && ff[i].isData()) {
                            List<FileObject> l = map.get(ff[i].getNameExt());
                            if (l == null){
                                l = new ArrayList<>();
                                map.put(ff[i].getNameExt(),l);
                            }
                            l.add(ff[i]);
                        }
                    }
                }
            }
        }
        return map;
    }
    
    private static String i18n(String id) {
        return NbBundle.getMessage(ErrorIncludeDialog.class,id);
    }

    private static String i18n(String id, int line) {
        return NbBundle.getMessage(ErrorIncludeDialog.class,id,""+line);
    }

}
