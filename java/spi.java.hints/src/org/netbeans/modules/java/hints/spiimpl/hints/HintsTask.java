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

package org.netbeans.modules.java.hints.spiimpl.hints;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.java.hints.providers.spi.PositionRefresherHelper;
import org.netbeans.modules.java.hints.providers.spi.PositionRefresherHelper.DocumentVersion;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.java.source.PositionRefProvider;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.spi.editor.hints.Context;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.editor.hints.settings.FileHintPreferences;
import org.openide.filesystems.FileObject;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class HintsTask implements CancellableTask<CompilationInfo> {

    public static final String KEY_HINTS = HintsInvoker.class.getName() + "-hints";
    public static final String KEY_SUGGESTIONS = HintsInvoker.class.getName() + "-suggestions";

    private static final Logger TIMER = Logger.getLogger("TIMER");
    private static final Logger TIMER_EDITOR = Logger.getLogger("TIMER.editor");
    private static final Logger TIMER_CARET = Logger.getLogger("TIMER.caret");

    private final AtomicBoolean cancel = new AtomicBoolean();

    private final boolean caretAware;

    public HintsTask(boolean caretAware) {
        this.caretAware = caretAware;
    }
    
    @Override
    public void run(CompilationInfo info) {
        cancel.set(false);

        if (org.netbeans.modules.java.hints.spiimpl.Utilities.disableErrors(info.getFileObject()).contains(Severity.VERIFIER)) {
            return;
        }

        Document doc = info.getSnapshot().getSource().getDocument(false);
        FileObject f = info.getSnapshot().getSource().getFileObject();
        if (f != null) {
            // hints use TreePathHandles to persist info for fixes, whcih in turn assume CloneableEditorSupport is
            // available on the document's DataObject. Since the document is opened, it would have to be opened through
            // the editor/open cookie, so there should not be a performance penalty in asking for them:
            PositionRefProvider prp;
            try {
                prp = PositionRefProvider.get(f);
                if (prp == null) {
                    return;
                }
                prp.createPosition(0, Position.Bias.Forward);
            } catch (IOException | IllegalArgumentException ex) {
                // the position provider is not working properly; bail out. Hints would fail
                // unexpectedly on creating TPHs, trying to open or save files etc.
                return;
            }
        }
        long startTime = System.currentTimeMillis();

        int caret = CaretAwareJavaSourceTaskFactory.getLastPosition(info.getFileObject());
        HintsSettings settings = HintsSettings.getSettingsFor(info.getFileObject());
        HintsInvoker inv = caretAware ? new HintsInvoker(settings, caret, cancel) : new HintsInvoker(settings, cancel);
        List<ErrorDescription> result = inv.computeHints(info);

        if (result == null || cancel.get()) {
            return;
        }

        HintsController.setErrors(info.getFileObject(), caretAware ? KEY_SUGGESTIONS : KEY_HINTS, result);

        if (caretAware) {
            SuggestionsPositionRefresherHelper.setVersion(doc, caret);
        } else {
            HintPositionRefresherHelper.setVersion(doc);
        }

        long endTime = System.currentTimeMillis();
        
        TIMER.log(Level.FINE, "{1}ms Hints Task: " + (caretAware ? " - Caret Aware" : "") + " {0}", new Object[] {info.getFileObject(), endTime - startTime});

        Logger l = caretAware ? TIMER_CARET : TIMER_EDITOR;

        for (Entry<String, Long> e : inv.getTimeLog().entrySet()) {
            l.log(Level.FINE, "{1}ms {0}", new Object[] {e.getKey(), e.getValue()});
        }
    }

    @Override
    public void cancel() {
        cancel.set(true);
    }


    @ServiceProvider(service=JavaSourceTaskFactory.class)
    public static final class FactoryImpl extends EditorAwareJavaSourceTaskFactory implements ChangeListener {

        public FactoryImpl() {
            super(Phase.RESOLVED, Priority.LOW, TaskIndexingMode.ALLOWED_DURING_SCAN);
            FileHintPreferences.addChangeListener(WeakListeners.change(this, HintsSettings.class));
        }

        @Override
        protected CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new HintsTask(false);
        }

	@Override
	public void stateChanged(ChangeEvent e) {
	    for (FileObject file : getFileObjects()) {
		reschedule(file);
	    }
	}
        
    }

    @ServiceProvider(service=JavaSourceTaskFactory.class)
    public static final class CaretFactoryImpl extends CaretAwareJavaSourceTaskFactory implements ChangeListener {

        public CaretFactoryImpl() {
            super(Phase.RESOLVED, Priority.LOW);
            FileHintPreferences.addChangeListener(WeakListeners.change(this, HintsSettings.class));
        }

        @Override
        protected CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new HintsTask(true);
        }

	@Override
	public void stateChanged(ChangeEvent e) {
	    for (FileObject file : getFileObjects()) {
		reschedule(file);
	    }
	}

    }

    @MimeRegistration(mimeType="text/x-java", service=PositionRefresherHelper.class)
    public static final class HintPositionRefresherHelper extends PositionRefresherHelper<DocumentVersion> {

        public HintPositionRefresherHelper() {
            super(KEY_HINTS);
        }

        @Override
        protected boolean isUpToDate(Context context, Document doc, DocumentVersion oldVersion) {
            return true;
        }

        @Override
        public List<ErrorDescription> getErrorDescriptionsAt(CompilationInfo info, Context context, Document doc) throws BadLocationException {
            int rowStart = LineDocumentUtils.getLineStart((BaseDocument) doc, context.getPosition());
            int rowEnd = LineDocumentUtils.getLineEnd((BaseDocument) doc, context.getPosition());

            return new HintsInvoker(HintsSettings.getSettingsFor(info.getFileObject()), rowStart, rowEnd, context.getCancel()).computeHints(info);
        }

        private static void setVersion(Document doc) {
            for (PositionRefresherHelper<?> h : MimeLookup.getLookup("text/x-java").lookupAll(PositionRefresherHelper.class)) {
                if (h instanceof HintPositionRefresherHelper hp) {
                    hp.setVersion(doc, new DocumentVersion(doc));
                }
            }
        }

    }

    @MimeRegistration(mimeType="text/x-java", service=PositionRefresherHelper.class)
    public static final class SuggestionsPositionRefresherHelper extends PositionRefresherHelper<SuggestionsDocumentVersion> {

        public SuggestionsPositionRefresherHelper() {
            super(KEY_SUGGESTIONS);
        }

        @Override
        protected boolean isUpToDate(Context context, Document doc, SuggestionsDocumentVersion oldVersion) {
            return oldVersion.suggestionsCaret == context.getPosition();
        }

        @Override
        public List<ErrorDescription> getErrorDescriptionsAt(CompilationInfo info, Context context, Document doc) throws BadLocationException {
            return new HintsInvoker(HintsSettings.getSettingsFor(info.getFileObject()), context.getPosition(), context.getCancel()).computeHints(info);
        }

        private static void setVersion(Document doc, int caret) {
            for (PositionRefresherHelper<?> h : MimeLookup.getLookup("text/x-java").lookupAll(PositionRefresherHelper.class)) {
                if (h instanceof SuggestionsPositionRefresherHelper sp) {
                    sp.setVersion(doc, new SuggestionsDocumentVersion(doc, caret));
                }
            }
        }
    }

    private static class SuggestionsDocumentVersion extends DocumentVersion {

        private final int suggestionsCaret;
        
        public SuggestionsDocumentVersion(Document doc, int suggestionsCaret) {
            super(doc);
            this.suggestionsCaret = suggestionsCaret;
        }
    }

}
