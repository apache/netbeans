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
package org.netbeans.modules.java.editor.fold;

import com.sun.source.tree.CompilationUnitTree;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.editor.base.fold.JavaElementFoldVisitor;
import org.netbeans.modules.java.editor.semantic.ScanningCancellableTask;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldInfo;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class JavaElementFoldManager implements FoldManager {
    
    private FoldOperation operation;
    private FileObject    file;
    private JavaElementFoldTask task;
    private boolean first = true;
    

    public static final FoldType INITIAL_COMMENT_FOLD_TYPE = FoldType.INITIAL_COMMENT; // NOI18N

    @NbBundle.Messages("FoldType_Imports=Imports")
    public static final FoldType IMPORTS_FOLD_TYPE = FoldType.create("import", Bundle.FoldType_Imports(), 
	     new org.netbeans.api.editor.fold.FoldTemplate(0, 0, "...")); // NOI18N
    
    @NbBundle.Messages("FoldType_Javadoc=Javadoc Comments")
    public static final FoldType JAVADOC_FOLD_TYPE = FoldType.DOCUMENTATION.derive("javadoc", Bundle.FoldType_Javadoc(), 
	     new org.netbeans.api.editor.fold.FoldTemplate(3, 2, "/**...*/")); // NOI18N

    @NbBundle.Messages("FoldType_Methods=Methods")
    public static final FoldType CODE_BLOCK_FOLD_TYPE = FoldType.MEMBER.derive("method", Bundle.FoldType_Methods(), 
	     new org.netbeans.api.editor.fold.FoldTemplate(1, 1, "{...}")); // NOI18N
    
    @NbBundle.Messages("FoldType_InnerClasses=Inner Classes")
    public static final FoldType INNERCLASS_TYPE = FoldType.NESTED.derive("innerclass", "Inner Classes", 
	     new org.netbeans.api.editor.fold.FoldTemplate(1, 1, "{...}")); // NOI18N
    
    protected static final class FoldTemplate {
        
        private FoldType type;
        
        private String description;
        
        private int startGuardedLength;
        
        private int endGuardedLength;
        
        protected FoldTemplate(FoldType type, String description,
        int startGuardedLength, int endGuardedLength) {
            this.type = type;
            this.description = description;
            this.startGuardedLength = startGuardedLength;
            this.endGuardedLength = endGuardedLength;
        }
        
        public FoldType getType() {
            return type;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getStartGuardedLength() {
            return startGuardedLength;
        }
        
        public int getEndGuardedLength() {
            return endGuardedLength;
        }

    }

    public void init(FoldOperation operation) {
        this.operation = operation;
    }

    public synchronized void initFolds(FoldHierarchyTransaction transaction) {
        Document doc = operation.getHierarchy().getComponent().getDocument();
        Object od = doc.getProperty(Document.StreamDescriptionProperty);
        
        if (od instanceof DataObject) {
            FileObject file = ((DataObject)od).getPrimaryFile();

            task = JavaElementFoldTask.getTask(file);
            task.setJavaElementFoldManager(JavaElementFoldManager.this, file);
        }
    }
    
    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        invalidate();
    }

    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        invalidate();
    }

    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    public void removeEmptyNotify(Fold emptyFold) {
        removeDamagedNotify(emptyFold);
    }

    public void removeDamagedNotify(Fold damagedFold) {
    }

    public void expandNotify(Fold expandedFold) {
    }

    public synchronized void release() {
        if (task != null)
            task.setJavaElementFoldManager(this, null);
        
        task         = null;
        file         = null;
    }
    
    private synchronized void invalidate() {
        if (task != null) {
            task.invalidate();
        }
    }
    
    static final class JavaElementFoldTask extends ScanningCancellableTask<CompilationInfo> {
        
        //XXX: this will hold JavaElementFoldTask as long as the FileObject exists:
        private static final Map<DataObject, JavaElementFoldTask> file2Task = new WeakHashMap<DataObject, JavaElementFoldTask>();
        
        private AtomicLong version = new AtomicLong(0);
        
        static JavaElementFoldTask getTask(FileObject file) {
            try {
                DataObject od = DataObject.find(file);
                synchronized (file2Task) {
                    JavaElementFoldTask task = file2Task.get(od);

                    if (task == null) {
                        file2Task.put(od,
                                task = new JavaElementFoldTask());
                    }
                    return task;
                }
            } catch (DataObjectNotFoundException ex) {
                Logger.getLogger(JavaElementFoldManager.class.getName()).log(Level.FINE, null, ex);
                return new JavaElementFoldTask();
            }
        }
        
        /**
         * All managers attched to this fold task
         */
        private Collection<Reference<JavaElementFoldManager>> managers = 
                new ArrayList<Reference<JavaElementFoldManager>>(2);
        
        void invalidate() {
            version.incrementAndGet();
        }

        synchronized void setJavaElementFoldManager(JavaElementFoldManager manager, FileObject file) {
            if (file == null) {
                for (Iterator<Reference<JavaElementFoldManager>> it = managers.iterator(); it.hasNext(); ) {
                    Reference<JavaElementFoldManager> ref = it.next();
                    JavaElementFoldManager fm = ref.get();
                    if (fm == null || fm == manager) {
                        it.remove();
                        break;
                    }
                }
            } else {
                managers.add(new WeakReference<JavaElementFoldManager>(manager));
                JavaElementFoldManagerTaskFactory.doRefresh(file);
            }
        }
        
        private synchronized Object findLiveManagers() {
            JavaElementFoldManager oneMgr = null;
            List<JavaElementFoldManager> result = null;
            
            for (Iterator<Reference<JavaElementFoldManager>> it = managers.iterator(); it.hasNext(); ) {
                Reference<JavaElementFoldManager> ref = it.next();
                JavaElementFoldManager fm = ref.get();
                if (fm == null) {
                    it.remove();
                    continue;
                }
                if (result != null) {
                    result.add(fm);
                } else if (oneMgr != null) {
                    result = new ArrayList<JavaElementFoldManager>(2);
                    result.add(oneMgr);
                    result.add(fm);
                } else {
                    oneMgr = fm;
                }
            }
            return result != null ? result : oneMgr;
        }
        
        public void run(final CompilationInfo info) {
            resume();
            
            final Object mgrs = findLiveManagers();            
            
            if (mgrs == null) {
                return ;
            }
            long startTime = System.currentTimeMillis();

            final CompilationUnitTree cu = info.getCompilationUnit();
            final Document doc = info.getSnapshot().getSource().getDocument(false);
            if (doc == null) {
                return;
            }
            
            final JavaElementFoldVisitor v = new JavaElementFoldVisitor(info, 
                    cu, info.getTrees().getSourcePositions(), doc, new JavaElementFoldVisitor.FoldCreator<FoldInfo>() {
                @Override
                public FoldInfo createImportsFold(int start, int end) {
                    return FoldInfo.range(start, end, IMPORTS_FOLD_TYPE);
                }

                @Override
                public FoldInfo createInnerClassFold(int start, int end) {
                    return FoldInfo.range(start, end, INNERCLASS_TYPE);
                }

                @Override
                public FoldInfo createCodeBlockFold(int start, int end) {
                    return FoldInfo.range(start, end, CODE_BLOCK_FOLD_TYPE);
                }

                @Override
                public FoldInfo createJavadocFold(int start, int end) {
                    return FoldInfo.range(start, end, JAVADOC_FOLD_TYPE);
                }

                @Override
                public FoldInfo createInitialCommentFold(int start, int end) {
                    return FoldInfo.range(start, end, INITIAL_COMMENT_FOLD_TYPE);
                }
            });
            
            scan(v, cu, null);
            
            final long stamp = version.get();
            
            if (v.isStopped() || isCancelled())
                return ;
            
            //check for initial fold:
            v.checkInitialFold();
            
            if (v.isStopped() || isCancelled())
                return ;
            
            if (mgrs instanceof JavaElementFoldManager) {
                SwingUtilities.invokeLater(
                        ((JavaElementFoldManager)mgrs).new CommitFolds(doc, v.getFolds(), v.getAnchors(), version, stamp)
                );
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    Collection<JavaElementFoldManager> jefms = (Collection<JavaElementFoldManager>)mgrs;
                    public void run() {
                        for (JavaElementFoldManager jefm : jefms) {
                            jefm.new CommitFolds(doc, v.getFolds(), v.getAnchors(), version, stamp).run();
                        }
                }});
            }
            
            long endTime = System.currentTimeMillis();
            
            Logger.getLogger("TIMER").log(Level.FINE, "Folds - 1",
                    new Object[] {info.getFileObject(), endTime - startTime});
        }
        
    }
    
    private class CommitFolds implements Runnable {
        
        private boolean insideRender;
        private Document doc;
        private List<FoldInfo> infos;
        private List<Integer> anchors;
        private long startTime;
        private AtomicLong version;
        private long stamp;
        
        public CommitFolds(Document doc, List<FoldInfo> infos, List<Integer> anchors, AtomicLong version, long stamp) {
            this.doc = doc;
            this.infos = infos;
            this.version = version;
            this.stamp = stamp;
            this.anchors = anchors;
        }
        
        private FoldInfo expanded(FoldInfo info) {
            FoldInfo ex = FoldInfo.range(info.getStart(), info.getEnd(), info.getType());
            if (info.getTemplate() != info.getType().getTemplate()) {
                ex = ex.withTemplate(info.getTemplate());
            }
            if (info.getDescriptionOverride() != null) {
                ex = ex.withDescription(info.getDescriptionOverride());
            }
            ex.attach(info.getExtraInfo());
            return ex.collapsed(false);
        }
        
        public void run() {
            int caretPos = -1;
            if (!insideRender) {
                startTime = System.currentTimeMillis();
                insideRender = true;
                
                // retain import & initial comment states
                operation.getHierarchy().getComponent().getDocument().render(this);
                
                return;
            }
            if (first) {
                JTextComponent c = operation.getHierarchy().getComponent();
                Object od = doc.getProperty(Document.StreamDescriptionProperty);
                if (od instanceof DataObject) {
                    DataObject d = (DataObject)od;
                    EditorCookie cake = d.getCookie(EditorCookie.class);
                    JEditorPane[] panes = cake.getOpenedPanes();
                    int idx = panes == null ? -1 : Arrays.asList(panes).indexOf(c);
                    if (idx != -1) {
                        caretPos = c.getCaret().getDot();
                    }
                }
            }
            operation.getHierarchy().lock();
            try {
                if (version.get() != stamp || operation.getHierarchy().getComponent().getDocument() != doc) {
                    return;
                }
                int expandIndex = -1;
                if (caretPos >= 0) {
                    for (int i = 0; i < anchors.size(); i++) {
                        int a = anchors.get(i);
                        if (a > caretPos) {
                            continue;
                        }
                        FoldInfo fi = infos.get(i);
                        if (a == caretPos) {
                            // do not expand comments if the pos is at the start, not within
                            FoldType ft = fi.getType();
                            if (ft.isKindOf(FoldType.INITIAL_COMMENT) || ft.isKindOf(FoldType.COMMENT) ||
                                ft.isKindOf(FoldType.DOCUMENTATION)) {
                                continue;
                            }
                        }
                        if (fi.getEnd() > caretPos) {
                            expandIndex = i;
                            break;
                        }
                    }
                }
                if (expandIndex != -1) {
                    infos = new ArrayList<FoldInfo>(infos);
                    infos.set(expandIndex, expanded(infos.get(expandIndex)));
                }
                Map<FoldInfo, Fold> folds = operation.update(infos, null, null);
                if (folds == null) {
                    // manager has been released.
                    return;
                }
                first = false;
            } catch (BadLocationException e) {
                Exceptions.printStackTrace(e);
            } finally {
                operation.getHierarchy().unlock();
            }
            
            long endTime = System.currentTimeMillis();
            
            Logger.getLogger("TIMER").log(Level.FINE, "Folds - 2",
                    new Object[] {file, endTime - startTime});
        }
    }
}
