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

package org.netbeans.modules.refactoring.java.ui;

import java.awt.EventQueue;
import java.awt.datatransfer.Transferable;
import java.util.*;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.plugins.InstantRefactoringPerformer;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

import static org.netbeans.modules.refactoring.java.ui.Bundle.*;


/**
 *
 * @author Jan Becicka
 */
@NbBundle.Messages({"WARN_CannotPerformHere=Cannot perform rename here."})
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider.class, position=100)
public class RefactoringActionsProvider extends ActionsImplementationProvider{

    private static boolean INSTANT = Boolean.getBoolean("org.netbeans.modules.java.refactoring.instantRename");
    
    /** Creates a new instance of RefactoringActionsProvider */
    public RefactoringActionsProvider() {
    }
    
    @Override
    public void doRename(final Lookup lookup) {
        final EditorCookie ec = lookup.lookup(EditorCookie.class);
        if(INSTANT && RefactoringUtils.isFromEditor(ec)) {
            invokeInstantRename(lookup, ec);
        } else {
            doFullRename(lookup);
        }
    }
    
    private void invokeInstantRename(final Lookup lookup, final EditorCookie ec) {
        try {
            JEditorPane target = ec.getOpenedPanes()[0];
            final int caret = target.getCaretPosition();
            String ident = Utilities.getIdentifier(Utilities.getDocument(target), caret);
            
            if (ident == null) {
                Utilities.setStatusBoldText(target, WARN_CannotPerformHere());
                return;
            }
            
            DataObject od = (DataObject) target.getDocument().getProperty(Document.StreamDescriptionProperty);
            JavaSource js = od != null ? JavaSource.forFileObject(od.getPrimaryFile()) : null;

            if (js == null) {
                Utilities.setStatusBoldText(target, WARN_CannotPerformHere());
                return;
            }
            InstantRefactoringUI ui = InstantRefactoringUIImpl.create(js, caret);
            
            if (ui != null) {
                if (ui.getRegions().isEmpty() || ui.getKeyStroke() == null) {
                    doFullRename(lookup);
                } else {
                    doInstantRename(target, js, caret, ui);
                }
            } else {
                Utilities.setStatusBoldText(target, WARN_CannotPerformHere());
            }
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    public static void doInstantRename(JTextComponent target, JavaSource js, int caretOffset, InstantRefactoringUI ui) throws BadLocationException {
        new InstantRefactoringPerformer(target, caretOffset, ui);
    }

    private void doFullRename(final Lookup lookup) {
        final Runnable task = ContextAnalyzer.createTask(lookup, RenameRefactoringUI.factory(lookup));
        if(!EventQueue.isDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                
                @Override
                public void run() {
                    UIUtilities.runWhenScanFinished(task, getActionName(RefactoringActionsFactory.renameAction()));
                }
            });
        } else {
            UIUtilities.runWhenScanFinished(task, getActionName(RefactoringActionsFactory.renameAction()));
        }
    }
    
    static String getActionName(Action action) {
        String arg = (String) action.getValue(Action.NAME);
        arg = arg.replace("&", ""); // NOI18N
        return arg.replace("...", ""); // NOI18N
    }


    /**
     * returns true if exactly one refactorable file is selected
     */
    @Override
    public boolean canRename(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();
        TreePathHandle tph = n.getLookup().lookup(TreePathHandle.class);
        if (tph != null) {
            return JavaRefactoringUtils.isRefactorable(tph.getFileObject());
        }
        DataObject dob = n.getCookie(DataObject.class);
        if (dob==null) {
            return false;
        }
        FileObject fo = dob.getPrimaryFile();
        if (JavaRefactoringUtils.isRefactorable(fo)) { //NOI18N
            return true;
        }
        if ((dob instanceof DataFolder) && 
                RefactoringUtils.isFileInOpenProject(fo) && 
                JavaRefactoringUtils.isOnSourceClasspath(fo) &&
                !RefactoringUtils.isClasspathRoot(fo)) {
            return true;
        }
        return false;
    }
    
    @Override
    public void doCopy(final Lookup lookup) {
        Runnable task = ContextAnalyzer.createTask(lookup, CopyClassRefactoringUI.factory(lookup));
        UIUtilities.runWhenScanFinished(task, getActionName(RefactoringActionsFactory.copyAction()));
    }

    /**
     * returns true if there is at least one java file in the selection
     * and all java files are refactorable
     */
    @Override
    public boolean canCopy(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        FileObject fo = getTarget(lookup);
        if (fo != null) {
            if (!fo.isFolder()) {
                return false;
            }
            if (!JavaRefactoringUtils.isOnSourceClasspath(fo) || RefactoringUtils.isIndexedSourceLauncherFile(fo)) {
                return false;
            }
        }
        boolean result = true;
        boolean hasJava = false;
        for (Node n:nodes) {
            DataObject dob = n.getLookup().lookup(DataObject.class);
            if (dob == null || dob.getPrimaryFile().isFolder()) {
                result = false;
                break;
            }
            if (RefactoringUtils.isJavaFile(dob.getPrimaryFile())) {
                hasJava = true;
                if(ClassPath.getClassPath(dob.getPrimaryFile(), ClassPath.SOURCE) == null) {
                    result = false;
                    break;
                }
            }
        }
        return result && hasJava;
    }    

    @Override
    public boolean canFindUsages(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();
        TreePathHandle handle;
        if ((handle = n.getLookup().lookup(TreePathHandle.class)) != null && handle.getFileObject() != null) {
            return true;
        }
        DataObject dob = n.getLookup().lookup(DataObject.class);
        if ((dob!=null) && RefactoringUtils.isJavaFile(dob.getPrimaryFile()) && !"package-info".equals(dob.getName())) { //NOI18N
            return true;
        }
        return false;
    }

    @Override
    public void doFindUsages(Lookup lookup) {
        Runnable task = ContextAnalyzer.createTask(lookup, WhereUsedQueryUI.factory());
        task.run();
    }

    /**
     * Returns true if all selected file are refactorable java files
     **/

    @Override
    public boolean canDelete(Lookup lookup) {
        if (SourceUtils.isScanInProgress()) {
            return false;
        }
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        //We live with a 2 pass validation of the selected nodes for now since
        //the code will become unreadable if we attempt to implement all checks
        //in one pass.
        if (isSelectionHeterogeneous(nodes)) {
            return false;
        }
        for (Node n:nodes) {
            TreePathHandle tph = n.getLookup().lookup(TreePathHandle.class);
            if (tph != null) {
                return JavaRefactoringUtils.isRefactorable(tph.getFileObject());
            }
            DataObject dataObject = n.getCookie(DataObject.class);
            if (dataObject == null){
                return false;
            }
            FileObject fileObject = dataObject.getPrimaryFile();
            if (isRefactorableFolder(dataObject)){
                return true;
            }
            if (!JavaRefactoringUtils.isRefactorable(fileObject)) {
                return false;
            }
        }
        return !nodes.isEmpty();
    }

    @Override
    public void doDelete(final Lookup lookup) {
        Runnable task = ContextAnalyzer.createTask(lookup, SafeDeleteUI.factory(lookup));
        UIUtilities.runWhenScanFinished(task, getActionName(RefactoringActionsFactory.safeDeleteAction()));
    }
    
    public static FileObject getTarget(Lookup look) {
        ExplorerContext drop = look.lookup(ExplorerContext.class);
        if (drop==null) {
            return null;
        }
        Node n = drop.getTargetNode();
        if (n==null) {
            return null;
        }
        DataObject dob = n.getCookie(DataObject.class);
        if (dob!=null) {
            return dob.getPrimaryFile();
        }
        return null;
    }
    
    public static PasteType getPaste(Lookup look) {
        ExplorerContext drop = look.lookup(ExplorerContext.class);
        if (drop==null) {
            return null;
        }
        Transferable orig = drop.getTransferable();
        if (orig==null) {
            return null;
        }
        Node n = drop.getTargetNode();
        if (n==null) {
            return null;
        }
        PasteType[] pt = n.getPasteTypes(orig);
        if (pt.length==1) {
            return null;
        }
        return pt[1];
    }
    
    static String getName(Lookup look) {
        ExplorerContext ren = look.lookup(ExplorerContext.class); 
        if (ren==null) {
            return null;
        }
        return ren.getNewName(); //NOI18N
    }
    
    /**
     * returns true if there is at least one java file in the selection
     * and all java files are refactorable
     */
    @Override
    public boolean canMove(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<Node>(lookup.lookupAll(Node.class));
        ExplorerContext drop = lookup.lookup(ExplorerContext.class);
        FileObject fo = getTarget(lookup);
        if (fo != null) {
            if (!fo.isFolder()) {
                return false;
            }
            if (!JavaRefactoringUtils.isOnSourceClasspath(fo) || RefactoringUtils.isIndexedSourceLauncherFile(fo)) {
                return false;
            }
            
            //it is drag and drop
            Set<DataFolder> folders = new HashSet<DataFolder>();
            boolean jdoFound = false;
            for (Node n:nodes) {
                DataObject dob = n.getCookie(DataObject.class);
                if (dob==null) {
                    return false;
                }
                if (!JavaRefactoringUtils.isOnSourceClasspath(dob.getPrimaryFile())
                        || RefactoringUtils.isIndexedSourceLauncherFile(dob.getPrimaryFile())) {
                    return false;
                }
                if (dob instanceof DataFolder) {
                    if (FileUtil.getRelativePath(dob.getPrimaryFile(), fo)!=null) {
                        return false;
                    }
                    folders.add((DataFolder)dob);
                } else if (RefactoringUtils.isJavaFile(dob.getPrimaryFile())) {
                    jdoFound = true;
                }
            }
            if (jdoFound) {
                return true;
            }
            for (DataFolder fold:folders) {
                for (Enumeration<DataObject> e = (fold).children(true); e.hasMoreElements();) {
                    if (RefactoringUtils.isJavaFile(e.nextElement().getPrimaryFile())) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            //regular invokation
            boolean result = false;
            nodesloop:
            for (Node n:nodes) {
                DataObject dob = n.getCookie(DataObject.class);
                if (dob==null) {
                    return false;
                }
                if (dob instanceof DataFolder) {
                    if (drop==null) {
                        return false;
                    } else {
                        //Ctrl-X
                        if (!JavaRefactoringUtils.isOnSourceClasspath(dob.getPrimaryFile())
                                || RefactoringUtils.isClasspathRoot(dob.getPrimaryFile())
                                || RefactoringUtils.isIndexedSourceLauncherFile(dob.getPrimaryFile())) {
                            return false;
                        } else {
                            LinkedList<DataFolder> folders = new LinkedList<DataFolder>();
                            folders.add((DataFolder) dob);
                            while (!folders.isEmpty()) {
                                DataFolder fold = folders.remove();
                                for (Enumeration<DataObject> e = fold.children(true); e.hasMoreElements();) {
                                    if (RefactoringUtils.isJavaFile(e.nextElement().getPrimaryFile())) {
                                        result = true;
                                        continue nodesloop;
                                    } else if (e instanceof DataFolder) {
                                        folders.add((DataFolder) e);
                                    }
                                }
                            }
                        }
                    }
                }
                if (!JavaRefactoringUtils.isOnSourceClasspath(dob.getPrimaryFile())
                        || RefactoringUtils.isIndexedSourceLauncherFile(dob.getPrimaryFile())) {
                    return false;
                }
                if (RefactoringUtils.isJavaFile(dob.getPrimaryFile())) {
                    result = true;
                }
            }
            return result;
        }
    }

    @Override
    public void doMove(final Lookup lookup) {
        Runnable task = ContextAnalyzer.createTask(lookup, MoveClassUI.factory(lookup));
        UIUtilities.runWhenScanFinished(task, getActionName(RefactoringActionsFactory.moveAction()));
    }
    private static boolean isSelectionHeterogeneous(Collection<? extends Node> nodes) {
        boolean folderSelected = false;
        boolean nonFolderNodeSelected = false;
        for (Node node : nodes) {
            DataObject dataObject = node.getCookie(DataObject.class);
            if (dataObject == null) {
                continue;
            }
            if (isRefactorableFolder(dataObject)) {
                if (folderSelected || nonFolderNodeSelected) {
                    return true;
                } else {
                    folderSelected = true;
                }
            } else {
                nonFolderNodeSelected = true;
            }
        }

        return false;
    }

    private static boolean isRefactorableFolder(DataObject dataObject) {
        FileObject fileObject = dataObject.getPrimaryFile();
        if (/*
                 * #159628
                 */!Boolean.TRUE.equals(fileObject.getAttribute("isRemoteAndSlow"))) { // NOI18N
            FileObject[] children = fileObject.getChildren();
            if (children == null || children.length <= 0) {
                return false;
            }
        }

        return (dataObject instanceof DataFolder)
                && RefactoringUtils.isFileInOpenProject(fileObject)
                && JavaRefactoringUtils.isOnSourceClasspath(fileObject)
                && !RefactoringUtils.isClasspathRoot(fileObject);
    }


}
