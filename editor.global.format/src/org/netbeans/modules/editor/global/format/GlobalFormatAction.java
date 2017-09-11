/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.global.format;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.lib.editor.util.swing.PositionRegion;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.refactoring.spi.BackupFacility.Handle;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.UserQuestionException;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

public final class GlobalFormatAction extends AbstractAction {

    private static final RequestProcessor WORKER = new RequestProcessor(GlobalFormatAction.class.getName(), 1, false, false);
    
    public GlobalFormatAction() {
        putValue(NAME, BaseKit.formatAction);
    }

    @Override
    @Messages({"BTN_OK=OK",
               "BTN_Cancel=Cancel",
               "CAP_Reformat=Format Recursively"
    })
    public void actionPerformed(ActionEvent e) {
        final JButton ok = new JButton(Bundle.BTN_OK());
        JButton cancelButton = new JButton(Bundle.BTN_Cancel());
        final ProgressHandle handle = ProgressHandleFactory.createHandle("Format");
        final ConfirmationPanel panel = new ConfirmationPanel(handle);
        DialogDescriptor nd = new DialogDescriptor(panel, Bundle.CAP_Reformat(), true, new Object[] {ok, cancelButton}, ok, DialogDescriptor.DEFAULT_ALIGN, null, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {}
        });
        final Dialog[] d = new Dialog[1];
        final AtomicBoolean cancel = new AtomicBoolean();
        final AtomicBoolean started = new AtomicBoolean();
        ok.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                ok.setEnabled(false);
                panel.started();
                started.set(true);
                WORKER.post(new Runnable() {
                    @Override public void run() {
                        try {
                            doFormat(handle, cancel);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override public void run() {
                                    d[0].setVisible(false);
                                    d[0].dispose();
                                }
                            });
                        }
                    }
                });
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                cancel.set(true);
                if (!started.get()) {
                    d[0].setVisible(false);
                    d[0].dispose();
                }
            }
        });
        handle.start(1);//need to "start" the progress, so that the progressbars have reasonable size
        d[0] = DialogDisplayer.getDefault().createDialog(nd);
        d[0].setVisible(true);
    }

    @Messages({"LBL_Preparing=Preparing...", "#{0} - the name of the file being formatted", "LBL_Formatting=Formatting: {0}", "LBL_BulkFormatting=Formatting"})
    private static void doFormat(ProgressHandle handle, AtomicBoolean cancel) throws IOException {
        try {
            handle.switchToIndeterminate();
            handle.progress(Bundle.LBL_Preparing());

            Set<String> sourceIds = new HashSet<String>();

            for (PathRecognizer pr : Lookup.getDefault().lookupAll(PathRecognizer.class)) {
                Set<String> ids = pr.getSourcePathIds();

                if (ids == null) continue;

                sourceIds.addAll(ids);
            }

            Lookup context = Utilities.actionsGlobalContext();
            List<FileObject> toFormat = new ArrayList<FileObject>();
            Set<FileObject> nonRecursiveRoots = new HashSet<FileObject>();

            for (NonRecursiveFolder f : context.lookupAll(NonRecursiveFolder.class)) {
                if (cancel.get()) return;
                nonRecursiveRoots.add(f.getFolder());
                for (FileObject c : f.getFolder().getChildren()) {
                    if (!c.isData()) continue;
                    toFormat.add(c);
                }
            }

            for (FileObject r : context.lookupAll(FileObject.class)) {
                if (cancel.get()) return;
                if (nonRecursiveRoots.contains(r)) continue;
                addRecursivelly(r, toFormat, sourceIds, null, null, cancel);
            }

            for (SourceGroup sg : context.lookupAll(SourceGroup.class)) {
                if (cancel.get()) return;
                addRecursivelly(sg.getRootFolder(), toFormat, sourceIds, null, sg, cancel);
            }

            for (DataObject d : context.lookupAll(DataObject.class)) {
                if (cancel.get()) return;
                if (nonRecursiveRoots.contains(d.getPrimaryFile())) continue;
                addRecursivelly(d.getPrimaryFile(), toFormat, sourceIds, null, null, cancel);
            }

            Collection<? extends Project> projects = context.lookupAll(Project.class);

            if (!projects.isEmpty()) {
                Map<Project, Map<FileObject, ClassPath>> sourceRoots = new IdentityHashMap<Project, Map<FileObject, ClassPath>>();

                for (String id : sourceIds) {
                    for (ClassPath sCP : GlobalPathRegistry.getDefault().getPaths(id)) {
                        for (FileObject root : sCP.getRoots()) {
                            if (cancel.get()) return;
                            Project owner = FileOwnerQuery.getOwner(root);

                            if (owner != null) {
                                Map<FileObject, ClassPath> projectSources = sourceRoots.get(owner);

                                if (projectSources == null) {
                                    sourceRoots.put(owner, projectSources = new HashMap<FileObject, ClassPath>());
                                }

                                projectSources.put(root, sCP);
                            }
                        }
                    }
                }

                for (Project prj : projects) {
                    Map<FileObject, ClassPath> roots = sourceRoots.get(prj);

                    if (roots != null) {
                        for (Entry<FileObject, ClassPath> e : roots.entrySet()) {
                            if (cancel.get()) return;
                            addRecursivelly(e.getKey(), toFormat, Collections.<String>emptySet(), e.getValue(), null, cancel);
                        }
                    } else {
                        for (SourceGroup sg : ProjectUtils.getSources(prj).getSourceGroups(Sources.TYPE_GENERIC)) {
                            if (cancel.get()) return;
                            addRecursivelly(sg.getRootFolder(), toFormat, sourceIds, null, sg, cancel);
                        }
                    }
                }
            }

            if (cancel.get()) return;
            
            BackupFacility bf = BackupFacility.getDefault();
            final Handle backup = bf.backup(toFormat);

            handle.switchToDeterminate(toFormat.size());

            int done = 0;

            for (FileObject current : toFormat) {
                if (cancel.get()) break ;
                try {
                    DataObject d = DataObject.find(current);
                    EditorCookie ec = d.getLookup().lookup(EditorCookie.class);

                    if (ec == null) continue;

                    handle.progress(Bundle.LBL_Formatting(FileUtil.getFileDisplayName(current)));

                    StyledDocument sd;
                    try {
                        sd = ec.openDocument();
                    } catch (UserQuestionException uqe) {
                        uqe.confirmed();
                        sd = ec.openDocument();
                    }
                    final StyledDocument doc = sd;
                    final Reformat reformat = Reformat.get(doc);

                    reformat.lock();

                    try {
                        NbDocument.runAtomic(doc, new Runnable() {
                            @Override public void run() {
                                try {
                                    reformat(reformat, doc, 0, doc.getLength(), new AtomicBoolean());
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        });
                    } finally {
                        reformat.unlock();
                    }

                    ec.saveDocument();
                } catch (UserQuestionException uqe) {
                    uqe.confirmed();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    handle.progress(++done);
                }
            }

            RefactoringSession session = RefactoringSession.create(Bundle.LBL_BulkFormatting());
            FormatRefactoring refactoring = new FormatRefactoring(new Transaction() {
                private boolean first = true;
                @Override
                public void commit() {
                    if (first) {
                        first = false;
                    } else {
                        try {
                            backup.restore();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }

                @Override
                public void rollback() {
                    try {
                        backup.restore();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });

            refactoring.prepare(session);
            session.doRefactoring(false);
        } finally {
        handle.finish();
        }
    }

    private static void addRecursivelly(FileObject top, List<FileObject> into, Set<String> sourceIds, ClassPath sourceCP, SourceGroup sg, AtomicBoolean cancel) {
        List<FileObject> todo = new LinkedList<FileObject>();
        Iterator<String> sIDIter = sourceIds.iterator();
        
        while (sourceCP == null && sIDIter.hasNext()) {
            if (cancel.get()) return;
            sourceCP = ClassPath.getClassPath(top, sIDIter.next());
        }

        todo.add(top);

        while (!todo.isEmpty()) {
            if (cancel.get()) return;
            
            FileObject current = todo.remove(0);

            if (!VisibilityQuery.getDefault().isVisible(current)) continue;
            if (sourceCP != null && !sourceCP.contains(current)) continue;
            if (sg != null && !sg.contains(current)) continue;

            if (current.isData()) {
                into.add(current);
            }

            todo.addAll(Arrays.asList(current.getChildren()));
        }
    }

    //TODO: copied from org.netbeans.editor.ActionFactory:
    static void reformat(Reformat formatter, Document doc, int startPos, int endPos, AtomicBoolean canceled) throws BadLocationException {
        final GuardedDocument gdoc = (doc instanceof GuardedDocument)
                ? (GuardedDocument) doc : null;

        int pos = startPos;
        if (gdoc != null) {
            pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
        }

        LinkedList<PositionRegion> regions = new LinkedList<PositionRegion>();
        while (pos < endPos) {
            int stopPos = endPos;
            if (gdoc != null) { // adjust to start of the next guarded block
                stopPos = gdoc.getGuardedBlockChain().adjustToNextBlockStart(pos);
                if (stopPos == -1 || stopPos > endPos) {
                    stopPos = endPos;
                }
            }

            if (pos < stopPos) {
                regions.addFirst(new PositionRegion(doc, pos, stopPos));
                pos = stopPos;
            } else {
                pos++; //ensure to make progress
            }

            if (gdoc != null) { // adjust to end of current block
                pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
            }
        }

        if (canceled.get()) return;
        // Once we start formatting, the task can't be canceled

        for (PositionRegion region : regions) {
            formatter.reformat(region.getStartOffset(), region.getEndOffset());
        }
    }

    static final class FormatRefactoring extends AbstractRefactoring {
        private final Transaction transaction;
        public FormatRefactoring(Transaction transaction) {
            super(Lookup.EMPTY);
            this.transaction = transaction;
        }
    }

    static final class FormatRefactoringPlugin implements RefactoringPlugin {
        private final FormatRefactoring refactoring;
        public FormatRefactoringPlugin(FormatRefactoring refactoring) {
            this.refactoring = refactoring;
        }
        @Override public Problem preCheck() { return null; }
        @Override public Problem checkParameters() { return null; }
        @Override public Problem fastCheckParameters() { return null; }
        @Override public void cancelRequest() {}
        @Override public Problem prepare(RefactoringElementsBag refactoringElements) {
            refactoringElements.registerTransaction(refactoring.transaction);
            return null;
        }
    }

    @ServiceProvider(service=RefactoringPluginFactory.class)
    public static final class FormatRefactoringPluginFactory implements RefactoringPluginFactory {
        @Override public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
            if (refactoring instanceof FormatRefactoring) {
                return new FormatRefactoringPlugin((FormatRefactoring) refactoring);
            }
            return null;
        }

    }
}
