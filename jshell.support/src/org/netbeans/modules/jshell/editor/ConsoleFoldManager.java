/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.editor;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.jshell.model.ConsoleContents;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldInfo;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public class ConsoleFoldManager implements FoldManager {
    private static final Logger LOG = Logger.getLogger(ConsoleFoldManager.class.getName());
    
    private static Map<DataObject, FoldTask>    fileTaskMap = new WeakHashMap<>();
    
    private FoldTask    parserTask;
    
    private FoldOperation   operation;

    @Override
    public void init(FoldOperation operation) {
        this.operation = operation;
    }

    @Override
    public void initFolds(FoldHierarchyTransaction transaction) {
        Document doc = operation.getHierarchy().getComponent().getDocument();
        Object od = doc.getProperty(Document.StreamDescriptionProperty);
        
        if (od instanceof DataObject) {
            FileObject file = ((DataObject)od).getPrimaryFile();

            parserTask = FoldTask.getTask(file);
            parserTask.updateFoldManager(this, file);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        invalidate();
    }

    @Override
    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        invalidate();
    }

    @Override
    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    @Override
    public void removeEmptyNotify(Fold epmtyFold) {
    }

    @Override
    public void removeDamagedNotify(Fold damagedFold) {
    }

    @Override
    public void expandNotify(Fold expandedFold) {
    }

    @Override
    public void release() {
        if (parserTask != null) {
            parserTask.updateFoldManager(this, null);
        }
    }
    
    private void invalidate() {
        if (parserTask != null) {
            parserTask.invalidate();
        }
    }
    
    private void update(List<FoldInfo> infos) {
        FoldOperation op = this.operation;
        if (op == null) {
            return;
        }
        Document d = operation.getHierarchy().getComponent().getDocument();
        d.render(() -> {
            operation.getHierarchy().lock();
            try {
                op.update(infos, null, null);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                operation.getHierarchy().unlock();
            }
        });
    }
    
    static final class FoldTask extends ParserResultTask<ConsoleContents> {
        private final AtomicInteger version = new AtomicInteger();
        private final List<Reference<ConsoleFoldManager>>  managers = new ArrayList<>();
        
        void updateFoldManager(ConsoleFoldManager mgr, FileObject f) {
            if (f == null) {
                for (Iterator<Reference<ConsoleFoldManager>> it = managers.iterator(); it.hasNext(); ) {
                    Reference<ConsoleFoldManager> ref = it.next();
                    ConsoleFoldManager fm = ref.get();
                    if (fm == null || fm == mgr) {
                        it.remove();
                        break;
                    }
                }
            } else {
                managers.add(new WeakReference<>(mgr));
            }
        }
        
        static FoldTask getTask(FileObject f) {
            if (f == null) {
                return null;
            }
            try {
                DataObject d = DataObject.find(f);
                synchronized (fileTaskMap) {
                    FoldTask ft = fileTaskMap.get(d);
                    if (ft != null) {
                        return ft;
                    }
                    ft = new FoldTask();
                    fileTaskMap.put(d, ft);
                    return ft;
                }
            } catch (DataObjectNotFoundException ex) {
                return new FoldTask();
            }
        }
        
        private synchronized List<ConsoleFoldManager> findLiveManagers() {
            List<ConsoleFoldManager> result = new ArrayList<>();
            
            for (Iterator<Reference<ConsoleFoldManager>> it = managers.iterator(); it.hasNext(); ) {
                Reference<ConsoleFoldManager> ref = it.next();
                ConsoleFoldManager fm = ref.get();
                if (fm == null) {
                    it.remove();
                    continue;
                }
                if (result != null) {
                    result.add(fm);
                }
            }
            return result;
        }
        
        void invalidate() {
            version.incrementAndGet();
        }

        @Override
        public void run(ConsoleContents result, SchedulerEvent event) {
            Document d = result.getSnapshot().getSource().getDocument(false);
            if (d == null) {
                return;
            }
            List<FoldInfo>  infos = new ArrayList<>();
            for (ConsoleSection s : result.getSectionModel().getSections()) {
                if (s == result.getInputSection()) {
                    continue;
                }
                
                int so = s.getStart();
                int eo = s.getEnd() - 1;
                
                switch (s.getType()) {
                    case JAVA:
                    case JAVA_INCOMPLETE:
                    case COMMAND:
                    default:
                        break;
                
                    case MESSAGE:
                        // the section may contain version information:
                        if (!processMessageSection(s, d, infos)) {
                            infos.add(FoldInfo.range(so, eo, ConsoleFoldsProvider.MESSAGE));
                        }
                        break;
                    case OUTPUT:
                        infos.add(FoldInfo.range(so, eo, ConsoleFoldsProvider.OUTPUT));
                        break;
                }
            }
            for (ConsoleFoldManager mgr : findLiveManagers()) {
                mgr.update(infos);
            }
        }
        
        private static final ResourceBundle infoBundle = NbBundle.getBundle("org/netbeans/modules/jshell/tool/Bundle"); // NOI18N
        
        private static final Pattern SYSTEM_INFO_PATTERN;
        private static final Pattern JAVA_RUNTIME_PATTERN;
        private static final Pattern CLASSPATH_PATTERN;
        
        static {
            SYSTEM_INFO_PATTERN =Pattern.compile(
                    "^.*" +
                    Pattern.quote(infoBundle.getString("MSG_SystemInformation")),
                    Pattern.MULTILINE);
            
            String s = infoBundle.getString("MSG_JavaVersion");
            int index = s.indexOf("{0}");
            if (index > 0) {
                s = Pattern.quote(s.substring(0, index)) + 
                        "(.*)" + 
                        Pattern.quote(s.substring(index + 3));
            } else {
                s = Pattern.quote(s);
            }
            JAVA_RUNTIME_PATTERN =Pattern.compile(
                    "^.*" + s,
                    Pattern.MULTILINE);
            
            
            CLASSPATH_PATTERN =Pattern.compile(
                    "^.*" +
                    Pattern.quote(infoBundle.getString("MSG_Classpath")) +
                    ".*$",
                    Pattern.MULTILINE);
        }

        @NbBundle.Messages({
            "# {0} - java version",
            "FoldDesc_SystemInfo_1=Java version: {0}",
            "FoldDesc_SystemInfo_2=System information",
        })
        private boolean processMessageSection(ConsoleSection section, Document d, List<FoldInfo> infos) {
            if (d == null) {
                // someone has closed the document
                return true;
            }
            String contents = section.getContents(d);
            Matcher m = JAVA_RUNTIME_PATTERN.matcher(contents);
            if (!m.find()) {
                return false;
            }
            String desc;
            
            if (m.groupCount() > 0) {
                desc = Bundle.FoldDesc_SystemInfo_1(m.group(1));
            } else {
                desc = Bundle.FoldDesc_SystemInfo_2();
            }
            FoldInfo initInfo = FoldInfo.range(section.getStart(), section.getEnd() - 1, ConsoleFoldsProvider.INITIAL_INFO);
            infos.add(initInfo.withDescription(desc));
            
            // try to find classpath info
            m = CLASSPATH_PATTERN.matcher(contents);
            if (!m.find()) {
                return true;
            }
            int end = m.end() + 1;
            int start = end;
            String[] lines = contents.substring(end).split("\\n *");
            int lcount = 0;
            for (String l : lines) {
                String l2 = l.trim();
                if (l2.isEmpty()) {
                    break;
                }
                end += l.length() + 1; // newline
                lcount++;
            }
            if (lcount < CLASSPATH_ITEMS_THRESHOLD) {
                return true;
            }
            
            infos.add(
                FoldInfo.range(
                        section.offsetFromContents(start), 
                        section.offsetFromContents(end - 1), 
                        ConsoleFoldsProvider.CLASSPATH_INFO
                )
            );
            return true;
        }
        private static final int CLASSPATH_ITEMS_THRESHOLD = 4;

        @Override
        public int getPriority() {
            return 100;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
        }

        @Override
        public void cancel() {
        }
        
    }

    @MimeRegistration(mimeType = "text/x-repl", service = FoldManagerFactory.class, position = 250)
    public static class Factory implements FoldManagerFactory {
        @Override
        public FoldManager createFoldManager() {
            return new ConsoleFoldManager();
        }
    }
    
    @MimeRegistration(mimeType = "text/x-repl", service = TaskFactory.class)
    public static class FoldTaskFactory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            FoldTask ft = FoldTask.getTask(snapshot.getSource().getFileObject());
            if (ft == null) {
                return Collections.emptyList();
            } else {
                return Collections.singleton(ft);
            }
        }
        
    }
}
