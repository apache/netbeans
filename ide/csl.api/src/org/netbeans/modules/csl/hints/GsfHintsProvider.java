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

package org.netbeans.modules.csl.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.core.CancelSupportImplementation;
import org.netbeans.modules.csl.core.SchedulerTaskCancelSupportImpl;
import org.netbeans.modules.csl.core.SpiSupportAccessor;
import org.netbeans.modules.csl.hints.infrastructure.GsfHintsManager;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.text.NbDocument;



/**
 * This class is based on JavaHintsFactory in Retouche's org.netbeans.modules.java.hints
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 * 
 * @author Jan Lahoda
 * @author leon chiver
 * @author Tor Norbye
 */
public final class GsfHintsProvider extends ParserResultTask<ParserResult> {
    
    public static final Logger LOG = Logger.getLogger(GsfHintsProvider.class.getName()); // NOI18N

    private final CancelSupportImplementation cancel = SchedulerTaskCancelSupportImpl.create(this);
    private FileObject file;
    
    /**
     * Tracks the HintsProvider being executed, so it can be cancelled.
     */
    private volatile HintsProvider pendingProvider;
    
    /**
     * Creates a new instance of GsfHintsProvider
     */
    GsfHintsProvider(FileObject file) {
        this.file = file;
    }
    
    private static final Map<org.netbeans.modules.csl.api.Severity, Severity> errorKind2Severity;
    
    static {
        errorKind2Severity = new EnumMap<org.netbeans.modules.csl.api.Severity, Severity>(org.netbeans.modules.csl.api.Severity.class);
        errorKind2Severity.put(org.netbeans.modules.csl.api.Severity.ERROR, Severity.ERROR);
        errorKind2Severity.put(org.netbeans.modules.csl.api.Severity.WARNING, Severity.WARNING);
        errorKind2Severity.put(org.netbeans.modules.csl.api.Severity.INFO, Severity.HINT);
//        errorKind2Severity.put(Error/*Diagnostic*/.Kind.NOTE, Severity.WARNING);
//        errorKind2Severity.put(Error/*Diagnostic*/.Kind.OTHER, Severity.WARNING);
    }
    
    List<ErrorDescription> computeErrors(Document doc, ParserResult result, List<? extends Error> errors, List<ErrorDescription> descs) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "errors = " + errors);
        }
        
        for (Error d : errors) {
            if (cancel.isCancelled()) {
                return null;
            }
            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "d = " + d);

                //Map<String, List<ErrorRule>> code2Rules = RulesManager.getInstance().getErrors();
            }
            
            //Map<String, List<ErrorRule>> code2Rules = RulesManager.getInstance().getErrors();
            
            //List<ErrorRule> rules = code2Rules.get(d.getKey());
            
            //if (LOG.isLoggable(Level.FINE)) {
                //LOG.log(Level.FINE, "code= " + d.getKey());
                //LOG.log(Level.FINE, "rules = " + rules);
            //}
            
            //int position = (int)d.getPosition();
            int astOffset = d.getStartPosition();
            int astEndOffset = d.getEndPosition();
            
            int position, endPosition;
            position = result.getSnapshot().getOriginalOffset(astOffset);
            if (position == -1) {
                continue;
            }
            endPosition = position+(astEndOffset-astOffset);
            
            LazyFixList ehm;
            
            //if (rules != null) {
            //    ehm = new CreatorBasedLazyFixList(info.getFileObject(), d.getKey(), (int)getPrefferedPosition(info, d), rules, data);
            //} else {
                ehm = ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList());
            //}
            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "ehm=" + ehm);
            }
            
            final String desc = d.getDisplayName();
            final Position[] range = getLine(d, doc, position, endPosition);
            
            if (cancel.isCancelled()) {
                return null;
            }
            
            if (range[0] == null || range[1] == null) {
                continue;
            }
            
            descs.add(ErrorDescriptionFactory.createErrorDescription(errorKind2Severity.get(d.getSeverity()), desc, ehm, doc, range[0], range[1]));
        }
        
        if (cancel.isCancelled()) {
            return null;
        }
        
        return descs;
    }
    
    public Document getDocument() {
        return DataLoadersBridge.getDefault().getDocument(file);
    }
    
    private Position[] getLine(final Error d, final Document doc, final int startOffset, final int endOffset) {
        if (doc == null) {
            return new Position[2];
        }
        final Position[][] ret = new Position[][] { new Position[2] };
        // line nunmber conversion + getText from the line should happen
        // under the document read-lock.
        doc.render(new Runnable() {
            public void run() {
                if (cancel.isCancelled()) {
                    return;
                }
                ret[0] = getLine0(d, doc, startOffset, endOffset);
            }
        });
        return ret[0];
    }
    
    private Position[] getLine0(Error d, final Document doc, int startOffset, int endOffset) {
        StyledDocument sdoc = (StyledDocument) doc;
        int lineNumber = NbDocument.findLineNumber(sdoc, startOffset);
        int lineOffset = NbDocument.findLineOffset(sdoc, lineNumber);
        String text = DataLoadersBridge.getDefault().getLine(doc, lineNumber);
        if (text == null) {
            return new Position[2];
        }
        
        if (d.isLineError()) {
            int column = 0;
            int length = text.length();
            
            while (column < text.length() && Character.isWhitespace(text.charAt(column))) {
                column++;
            }
            
            while (length > 0 && Character.isWhitespace(text.charAt(length - 1))) {
                length--;
            }
            
            startOffset = lineOffset + column;
            endOffset = lineOffset + length;
            if (startOffset > endOffset) {
                // Space only on the line
                startOffset = lineOffset;
            }
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "startOffset = " + startOffset );
            LOG.log(Level.FINE, "endOffset = " + endOffset );
        }
        
        final int startOffsetFinal = startOffset;
        final int endOffsetFinal = endOffset;
        final Position[] result = new Position[2];
        
        int len = doc.getLength();

        if (startOffsetFinal > len || endOffsetFinal > len) {
            if (!cancel.isCancelled() && LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, "document changed, but not canceled?" );
                LOG.log(Level.WARNING, "len = " + len );
                LOG.log(Level.WARNING, "startOffset = " + startOffsetFinal );
                LOG.log(Level.WARNING, "endOffset = " + endOffsetFinal );
            }
            cancel();

            return result;
        }

        try {
            result[0] = NbDocument.createPosition(doc, startOffsetFinal, Bias.Forward);
            result[1] = NbDocument.createPosition(doc, endOffsetFinal, Bias.Backward);
        } catch (BadLocationException e) {
            LOG.log(Level.WARNING, null, e);
        }
        
        return result;
    }


    @Override
    public void cancel() {
        final HintsProvider curProvider = this.pendingProvider;
        if (curProvider != null) {
            curProvider.cancel();
        }
    }

    private List<Error> processProviderErrors(
            final List<ErrorDescription> descriptions, 
            Snapshot topLevelSnapshot, final ParserResult r, final Language language) throws ParseException {
        
        HintsProvider provider = language.getHintsProvider();
        if (provider == null) {
            return null;
        }

        GsfHintsManager manager = language.getHintsManager();
        if (manager == null) {
            return null;
        }
        RuleContext ruleContext = manager.createRuleContext(r, language, -1, -1, -1);
        if (ruleContext == null) {
            return null;
        }
        List<Hint> hints = new ArrayList<Hint>();
        List<Error> errors = new ArrayList<Error>();
        try {
            synchronized (this) {
                pendingProvider = provider;
                if (cancel.isCancelled()) {
                    return errors;
                }
            }
            provider.computeErrors(manager, ruleContext, hints, errors);
        } finally {
            pendingProvider = null;
        }
        
        boolean allowDisableEmpty = true;
        for (int i = 0; i < hints.size(); i++) {
            Hint hint = hints.get(i);
            OffsetRange range = hint.getRange();
            if (range != null &&
                    range.getStart() >= 0 && range.getStart() <= topLevelSnapshot.getText().length() &&
                    range.getEnd() >= 0 && range.getEnd() <= topLevelSnapshot.getText().length() &&
                    range.getStart() <= range.getEnd()
            ) {
                ErrorDescription errorDesc = manager.createDescription(hint, ruleContext, allowDisableEmpty, i == hints.size()-1);
                descriptions.add(errorDesc);
            } else {
                String msg = provider + " supplied hint " + hint + " with invalid range " + range + //NOI18N
                        ", topLevelSnapshot.length=" + topLevelSnapshot.getText().length() +
                        ", file=" + topLevelSnapshot.getSource().getFileObject(); //NOI18N
//                                            assert false : msg;
                LOG.log(Level.FINE, msg);
            }
        }
        return errors;
    }
    
    private void refreshErrors(final ResultIterator resultIterator) throws ParseException {
        List<ErrorDescription> descs = new ArrayList<ErrorDescription>();
        Document doc = getDocument();
        processErrorsRecursive(resultIterator, doc, descs, resultIterator.getSnapshot());
        HintsController.setErrors(doc, GsfHintsFactory.LAYER_NAME, descs);
    }
    
    private void processErrorsRecursive(final ResultIterator resultIterator, 
            Document doc,
            final List<ErrorDescription> descriptions, 
            Snapshot topLevelSnapshot) throws ParseException {

            if (resultIterator == null) {
            return;
        }
        
        if (doc == null) {
            doc = getDocument();
        }
        
        for(Embedding e : resultIterator.getEmbeddings()) {
            try {
                if (cancel.isCancelled()) {
                    return;
                }
            } catch (Exception ex) {
                // should never happen, but log:
                LOG.log(Level.WARNING, "Unexpected error", ex);
            }

            processErrorsRecursive(resultIterator.getResultIterator(e), doc, descriptions, topLevelSnapshot);
        }
        if (!(resultIterator.getParserResult() instanceof ParserResult)) {
            return;
        }
        processErrors(resultIterator.getSnapshot(), (ParserResult)resultIterator.getParserResult(), 
                doc, descriptions, topLevelSnapshot);
    }
    
    void processErrors(final Snapshot snapshot, final ParserResult result,
            Document doc,
            final List<ErrorDescription> descriptions, 
            Snapshot topLevelSnapshot) throws ParseException {

        if (doc == null) {
            doc = getDocument();
        }
        
        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(snapshot.getMimeType());
        if (language == null) {
            return;
        }
        if(!(result instanceof ParserResult)) {
            return ;
        }

        ParserResult r = (ParserResult)result;
        List<? extends Error> errors = r.getDiagnostics();
        List<ErrorDescription> desc = new ArrayList<ErrorDescription>();
        List<Error> unhandled = processProviderErrors(descriptions, topLevelSnapshot, r, language);
        if (unhandled != null) {
            errors = unhandled;
        }
        // Process errors without codes
        desc = computeErrors(doc, r, errors, desc);
        if (desc == null) {
            //meaning: cancelled
            return;
        }

        descriptions.addAll(desc);
    }
    
    public @Override void run(ParserResult result, SchedulerEvent event) {
        final Document doc = getDocument();
        if (doc == null) {
            LOG.log(Level.INFO, "SemanticHighlighter: Cannot get document!");
            return ;
        }
        SpiSupportAccessor.getInstance().setCancelSupport(cancel);
        try {
            ParserManager.parse(Collections.singleton(result.getSnapshot().getSource()), new UserTask() {
                public @Override void run(ResultIterator resultIterator) throws ParseException {
                    refreshErrors(resultIterator);
                }

            });
        } catch (ParseException e) {
            LOG.log(Level.WARNING, null, e);
        } finally {
            SpiSupportAccessor.getInstance().removeCancelSupport(cancel);
        }
    }
    
    public static void refreshErrors() {
        
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }
}

