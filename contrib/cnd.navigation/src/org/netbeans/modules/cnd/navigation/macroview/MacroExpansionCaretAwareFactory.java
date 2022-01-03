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
package org.netbeans.modules.cnd.navigation.macroview;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.modules.parsing.spi.support.CancelSupport;
import org.openide.filesystems.FileObject;

/**
 * Updates information in macro expansion view if caret position of main document changes and vice versa.
 *
 */
public final class MacroExpansionCaretAwareFactory extends IndexingAwareParserResultTask<Parser.Result> {
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.model.tasks"); //NOI18N
    private final CancelSupport cancel = CancelSupport.create(this);
    private AtomicBoolean canceled = new AtomicBoolean(false);
    
    public MacroExpansionCaretAwareFactory(String mimeType) {
        super(TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    @Override
    public void run(Parser.Result result, SchedulerEvent event) {
        synchronized (this) {
            canceled.set(true);
            canceled = new AtomicBoolean(false);
        }
        if (cancel.isCancelled()) {
            return;
        }
        if (!(event instanceof CursorMovedSchedulerEvent)) {
            return;
        }
        if (!MacroExpansionTopComponent.isMacroExpansionInitialized()) {
            return;
        }
        Document doc = result.getSnapshot().getSource().getDocument(false);
        if (!(doc instanceof StyledDocument)) {
            return;
        }
        CsmFile csmFile = CsmFileInfoQuery.getDefault().getCsmFile(result);
        if (csmFile == null) {
            csmFile = (CsmFile) doc.getProperty(CsmFile.class);
        }
        if (csmFile == null) {
            return;
        }
        if (canceled.get()) {
          return;
        }
        long time = 0;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "MacroExpansionCaretAwareFactory started"); //NOI18N
            time = System.currentTimeMillis();
        }
        runImpl((CursorMovedSchedulerEvent)event, csmFile, doc, canceled);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "MacroExpansionCaretAwareFactory finished for {0}ms", System.currentTimeMillis()-time); //NOI18N
        }
    }

    @Override
    public final void cancel() {
        synchronized(this) {
            canceled.set(true);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "MacroExpansionCaretAwareFactory cancelled"); //NOI18N
        }
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public int getPriority() {return 400;}

    private void runImpl(final CursorMovedSchedulerEvent event, final CsmFile csmFile, final Document doc, final AtomicBoolean canceled) {
       if (doc == null) {
            return;
        }
        Object obj = doc.getProperty(CsmMacroExpansion.USE_OWN_CARET_POSITION);
        if (obj != null) {
            if(!(Boolean) obj) {
                return;
            }
        }
        Runnable syncPositions = new Runnable() {
            @Override
            public void run() {
                if (!canceled.get()) {
                    syncRelatedDocumentCaretPosition(event, doc);
                }
            }
        };
        if (isMacroExpansionDoc(doc)) {
            SwingUtilities.invokeLater(syncPositions);
        } else {
            MacroExpansionViewUtils.updateView(doc, event.getCaretOffset(), csmFile, canceled, syncPositions);
        }
    }

    private static boolean isMacroExpansionDoc(Document doc) {
        return doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) != null;
    }

    private void syncRelatedDocumentCaretPosition(final CursorMovedSchedulerEvent event, final Document doc) {
        if (!MacroExpansionTopComponent.isSyncCaretAndContext()) {
            return;
        }
        if (doc != null) {
            Document doc2 = (Document) doc.getProperty(Document.class);
            if (doc2 != null) {
                JTextComponent comp2 = null;
                for(JTextComponent comp : EditorRegistry.componentList()) {
                    if (doc2.equals(comp.getDocument())) {
                        comp2 = comp;
                        break;
                    }
                }
                FileObject file2 = CsmUtilities.getFileObject(doc2);
                if (file2 != null && comp2 != null) {
                    int doc2CarretPosition = comp2.getCaretPosition();
                    int docCarretPosition = event.getCaretOffset();
                    int doc2CarretPositionFromDoc = MacroExpansionViewUtils.getDocumentOffset(doc2,
                            MacroExpansionViewUtils.getFileOffset(doc, docCarretPosition));
                    int docCarretPositionFromDoc2 = MacroExpansionViewUtils.getDocumentOffset(doc,
                            MacroExpansionViewUtils.getFileOffset(doc2, doc2CarretPosition));
                    if (doc2CarretPositionFromDoc >= 0 && doc2CarretPositionFromDoc < doc2.getLength()) {
                        JEditorPane ep = MacroExpansionViewUtils.getEditor(doc);
                        JEditorPane ep2 = MacroExpansionViewUtils.getEditor(doc2);
                        if (ep != null && ep2 != null && doc2CarretPosition != doc2CarretPositionFromDoc &&
                                docCarretPosition != docCarretPositionFromDoc2) {
                            ep2.setCaretPosition(doc2CarretPositionFromDoc);
                        }
                    }
                }
            }
        }
    }

    @MimeRegistrations({
        @MimeRegistration(mimeType = MIMENames.C_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = TaskFactory.class),
    })
    public static final class MacroExpansionCaretAwareFactoryImpl extends TaskFactory {
        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singletonList(new MacroExpansionCaretAwareFactory(snapshot.getMimeType()));
        }
    }
}
