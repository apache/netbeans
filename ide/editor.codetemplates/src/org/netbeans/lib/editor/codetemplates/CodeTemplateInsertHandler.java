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

package org.netbeans.lib.editor.codetemplates;

import static java.lang.Integer.MAX_VALUE;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.GuardedException;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessorFactory;
import org.netbeans.lib.editor.codetemplates.storage.CodeTemplateSettingsImpl.OnExpandAction;
import org.netbeans.lib.editor.codetemplates.textsync.TextRegion;
import org.netbeans.lib.editor.codetemplates.textsync.TextRegionManager;
import org.netbeans.lib.editor.codetemplates.textsync.TextRegionManagerEvent;
import org.netbeans.lib.editor.codetemplates.textsync.TextRegionManagerListener;
import org.netbeans.lib.editor.codetemplates.textsync.TextSync;
import org.netbeans.lib.editor.codetemplates.textsync.TextSyncGroup;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.CharacterConversions;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.api.Reformat;

/**
 * Code template allows the client to paste itself into the given
 * text component.
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateInsertHandler implements TextRegionManagerListener, Runnable {

    // -J-Dorg.netbeans.lib.editor.codetemplates.CodeTemplateInsertHandler.level=FINE
    private static final Logger LOG = Logger.getLogger(CodeTemplateInsertHandler.class.getName());
    /** logger for timers/counters */
    private static final Logger TIMERS = Logger.getLogger("TIMER"); // NOI18N

    /**
     * Property holding active code template processor during template expanding.
     * Note: this property is checked for non-null value in java/source and gsf.api modules.
     */
    private static final Object CT_HANDLER_DOC_PROPERTY = "code-template-insert-handler"; // NOI18N
    
    private final CodeTemplate codeTemplate;
    
    private final JTextComponent component;
    
    private final List<CodeTemplateProcessor> processors;
    
    private String parametrizedText;
    
    private ParametrizedTextParser parametrizedTextParser;

    private String insertText;
    
    private List<CodeTemplateParameter> allParameters;
    
    private List<CodeTemplateParameter> masterParameters;
    
    private CodeTemplateInsertRequest request;
    
    private boolean inserted;
    
    private boolean released;
    
    private TextRegion completeTextRegion;

    private String completeInsertString;

    private Reformat formatter;
    private Indent indenter;   
    
    private TextSyncGroup textSyncGroup;
    
    private boolean completionInvoked;

    public CodeTemplateInsertHandler(
        CodeTemplate codeTemplate,
        JTextComponent component, 
        Collection<? extends CodeTemplateProcessorFactory> processorFactories,
        OnExpandAction onExpandAction
    ) {
        this.codeTemplate = codeTemplate;
        this.component = component;

        completeTextRegion = new TextRegion();
        TextSync completeTextSync = new TextSync(completeTextRegion);
        textSyncGroup = new TextSyncGroup(completeTextSync);

        this.request = CodeTemplateSpiPackageAccessor.get().createInsertRequest(this);

        setParametrizedText(codeTemplate.getParametrizedText());

        processors = new ArrayList<>();
        for (CodeTemplateProcessorFactory factory : processorFactories) {
            processors.add(factory.createProcessor(this.request));
        }
        
        for (CodeTemplateParameter parameter : masterParameters) {
            if (CodeTemplateParameter.NO_FORMAT_PARAMETER_NAME.equals(parameter.getName()) && onExpandAction != OnExpandAction.NOOP) {
                onExpandAction = OnExpandAction.INDENT;
                break;
            }
            if (CodeTemplateParameter.NO_INDENT_PARAMETER_NAME.equals(parameter.getName())) {
                onExpandAction = OnExpandAction.NOOP;
                break;
            }
        }
        switch (onExpandAction) {
            case FORMAT:
                formatter = Reformat.get(component.getDocument());
                break;
            case INDENT:
                indenter = Indent.get(component.getDocument());
                break;
        }

        if (TIMERS.isLoggable(Level.FINE)) {
            LogRecord rec = new LogRecord(Level.FINE, "CodeTemplateInsertHandler"); // NOI18N
            rec.setParameters(new Object[] { this });
            TIMERS.log(rec);
        }
    }
    
    public CodeTemplate getCodeTemplate() {
        return codeTemplate;
    }
    
    public JTextComponent getComponent() {
        return component;
    }
    
    public CodeTemplateInsertRequest getRequest() {
        return request;
    }
    
    public synchronized boolean isInserted() {
        return inserted;
    }
    
    public synchronized boolean isReleased() {
        return released;
    }
    
    public String getParametrizedText() {
        return parametrizedText;
    }
    
    public void setParametrizedText(String parametrizedText) {
        int idx = 0;
        while(idx < parametrizedText.length() && Character.isWhitespace(parametrizedText.charAt(idx))) {
            idx++;
        }
        this.parametrizedText = CharacterConversions.lineSeparatorToLineFeed(idx > 0 ? parametrizedText.substring(idx) : parametrizedText);
        parseParametrizedText();
    }

    public int getInsertOffset() {
        return completeTextRegion.startOffset();
    }

    public String getInsertText() {
        if (inserted) {
            try {
                int startOffset = getInsertOffset();
                Document doc = component.getDocument();
                return doc.getText(startOffset, completeTextRegion.endOffset() - startOffset);
            } catch (BadLocationException e) {
                LOG.log(Level.WARNING, "Invalid offset", e); // NOI18N
                return "";
            }

        } else { // not inserted yet
            checkInsertTextBuilt();
            return insertText;
        }
    }
    
    public List<? extends CodeTemplateParameter> getAllParameters() {
        return Collections.unmodifiableList(allParameters);
    }

    public List<? extends CodeTemplateParameter> getMasterParameters() {
        return Collections.unmodifiableList(masterParameters);
    }
    
    public void processTemplate() {
        // Update default values by all processors
        for (CodeTemplateProcessor processor : processors) {
            processor.updateDefaultValues();
        }
        // Insert the template into document
        insertTemplate();
    }

    void checkInsertTextBuilt() {
        if (insertText == null) {
            insertText = buildInsertText();
        }
    }
    
    void resetCachedInsertText() {
        insertText = null;
    }
    
    public void insertTemplate() {
        TextRegionManager trm = TextRegionManager.reserve(component);
        if (trm == null) // Already occupied for another component over the same document
            return;

        Document doc = component.getDocument();
        doc.putProperty(CT_HANDLER_DOC_PROPERTY, this);
        // Build insert string outside of the atomic lock
        completeInsertString = getInsertText();

        if (formatter != null)
            formatter.lock();
        if (indenter != null)
            indenter.lock();
        try {
            if (doc instanceof BaseDocument) {
                ((BaseDocument) doc).runAtomicAsUser(this);
            } else { // Otherwise run without atomic locking
                this.run();
            }
        } finally {
            if (formatter != null) {
                formatter.unlock();
                formatter = null;
            }
            if (indenter != null) {
                indenter.unlock();
                indenter = null;
            }
            completeInsertString = null;
        }
    }

    boolean isCompletionInvoked() {
        return completionInvoked;
    }

    @Override
    public void run() {
        boolean success = false;
        try {
            Document doc = component.getDocument();
            BaseDocument bdoc = (doc instanceof BaseDocument)
                    ? (BaseDocument) doc
                    : null;

            // First check if there is a caret selection and if so remove it
            Caret caret = component.getCaret();
            Position pos;
            if (Utilities.isSelectionShowing(caret)) {
                int removeOffset = component.getSelectionStart();
                int removeLength = component.getSelectionEnd() - removeOffset;
                pos = doc.createPosition(removeOffset);
                // Removal can cause vars of outer tepmlate to get updated
                // so removeOffset needs to be remembered as position
                doc.remove(removeOffset, removeLength);
            } else { // No selection
                pos = doc.createPosition(caret.getDot());
            }

            // insert the complete text
            completeTextRegion.updateBounds(null, 
                    TextRegion.createFixedPosition(completeInsertString.length()));


            doc.insertString(pos.getOffset(), completeInsertString, null);
            // Positions at offset 0 do not move - swing anomally :-( so do Math.max()
            pos = doc.createPosition(Math.max(pos.getOffset() - completeInsertString.length(), 0));
            // #132615
            // Insert a special undoable-edit marker that - once undone will release CT editing.
            if (bdoc != null) {
                bdoc.addUndoableEdit(new TemplateInsertUndoEdit(doc));
            }
            
            TextRegion<?> caretTextRegion = null;
            
            List<CodeTemplateParameter> prioritizedMasterParameters = prioritizeParameters(masterParameters);
            
            // Go through all master parameters and create region infos for them
            for (CodeTemplateParameter master : prioritizedMasterParameters) {
                CodeTemplateParameterImpl masterImpl = CodeTemplateParameterImpl.get(master);
                if (CodeTemplateParameter.CURSOR_PARAMETER_NAME.equals(master.getName())) {
                    // Add explicit ${cursor} as last into text sync group to jump to it by TAB as last param
                    caretTextRegion = masterImpl.textRegion();
                } else {
                    textSyncGroup.addTextSync(masterImpl.textRegion().textSync());
                }
            }
            
            if (caretTextRegion == null) { // no specific ${cursor} parameter
                Position caretFixedPos = TextRegion.createFixedPosition(completeInsertString.length());
                caretTextRegion = new TextRegion(caretFixedPos, caretFixedPos);
                TextSync caretTextSync = new TextSync(caretTextRegion);
                caretTextSync.setCaretMarker(true);
            }
            textSyncGroup.addTextSync(caretTextRegion.textSync());
            
            // For nested template expanding or when without parameters
            // just update the caret position and release
            textSyncGroup.setClientInfo(this);
            TextRegionManager trm = textRegionManager();
            trm.addGroup(textSyncGroup, pos.getOffset());
            // Add the listener before reformat() so that the possible releasing gets catched
            trm.addTextRegionManagerListener(this);
            // Mark inserted - before reformat (otherwise ISE - the parameters' text could not be changed)
            this.inserted = true;
            
            if (bdoc != null) {
                component.setCaretPosition(caretTextRegion.startOffset());
                if (formatter != null)
                    formatter.reformat(pos.getOffset(), pos.getOffset() + completeInsertString.length());
                if (indenter != null)
                    indenter.reindent(pos.getOffset(), pos.getOffset() + completeInsertString.length());
            }

            if (!released) {
                trm.activateGroup(textSyncGroup);
            }
            success = true;
            
        } catch (GuardedException ge) {
            LOG.log(Level.FINE, null, ge); // NOI18N
        } catch (BadLocationException e) {
            LOG.log(Level.WARNING, "Invalid offset", e); // NOI18N
        } finally {
            resetCachedInsertText();
            if (!success) {
                this.inserted = false;
                release();
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("CodeTemplateInsertHandler.insertTemplate()\n"); // NOI18N
            LOG.fine(toStringDetail());
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(textRegionManager().toString() + "\n");
            }
        }
    }
    
    private TextRegionManager textRegionManager() {
        return TextRegionManager.get(component.getDocument(), true);
    }
    
    public String getDocParameterValue(CodeTemplateParameterImpl paramImpl) {
        TextRegion textRegion = paramImpl.textRegion();
        int offset = textRegion.startOffset();
        int len = textRegion.endOffset() - offset;
        String parameterText;
        try {
            parameterText = component.getDocument().getText(offset, len);
        } catch (BadLocationException e) {
            LOG.log(Level.WARNING, "Invalid offset", e); // NOI18N
            parameterText = ""; //NOI18N
        }
        return parameterText;
    }
    
    public void setDocMasterParameterValue(CodeTemplateParameterImpl paramImpl, String newValue) {
        assert (!paramImpl.isSlave()); // assert master parameter
        TextRegion textRegion = paramImpl.textRegion();
        int offset = textRegion.startOffset();
        int length = textRegion.endOffset() - offset;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("CodeTemplateInsertHandler.setMasterParameterValue(): parameter-name=" + paramImpl.getName() + // NOI18N
                    ", offset=" + offset + // NOI18N
                    ", length=" + length + ", newValue=\"" + newValue + "\"\n"); // NOI18N
        }
        try {
            Document doc = component.getDocument();
            CharSequence parameterText = DocumentUtilities.getText(doc, offset, length);
            if (!CharSequenceUtilities.textEquals(parameterText, newValue)) {
                textRegion.textSync().setText(newValue);
                notifyParameterUpdate(paramImpl.getParameter(), false);
            }
        } catch (BadLocationException e) {
            LOG.log(Level.WARNING, "Invalid offset", e); // NOI18N
        }
    }
    
    private void notifyParameterUpdate(CodeTemplateParameter parameter, boolean typingChange) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("CodeTemplateInsertHandler.notifyParameterUpdate() CALLED for " + parameter.getName() + "\n"); // NOI18N
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(textRegionManager().toString() + "\n");
            }
        }
        // Notify all processors about parameter's change
        for (CodeTemplateProcessor processor : processors) {
            processor.parameterValueChanged(parameter, typingChange);
        }
    }
    
    private void parseParametrizedText() {
        allParameters = new ArrayList<>(2);
        masterParameters = new ArrayList<>(2);
        parametrizedTextParser = new ParametrizedTextParser(this, parametrizedText);
        parametrizedTextParser.parse();
    }
    
    void notifyParameterParsed(CodeTemplateParameterImpl paramImpl) {
        allParameters.add(paramImpl.getParameter());
        // Check whether a corresponding master parameter already exists
        for (CodeTemplateParameter master : masterParameters) {
            if (master.getName().equals(paramImpl.getName())) {
                paramImpl.markSlave(master);
                CodeTemplateParameterImpl masterImpl = CodeTemplateParameterImpl.get(master);
                TextSync textSync = masterImpl.textRegion().textSync();
                textSync.addRegion(paramImpl.textRegion());
                return;
            }
        }
        // Make it master
        masterParameters.add(paramImpl.getParameter());
        TextSync textSync = new TextSync(paramImpl.textRegion());
        if (paramImpl.isEditable())
            textSync.setEditable(true);
        if (CodeTemplateParameter.CURSOR_PARAMETER_NAME.equals(paramImpl.getName()))
            textSync.setCaretMarker(true);
        textSync.setCompletionInvoke(paramImpl.isCompletionInvoke());
    }

    @Override
    public void stateChanged(TextRegionManagerEvent evt) {
        completionInvoked = false;
        TextRegionManager trm = evt.textRegionManager();
        if (evt.isFocusChange()) {
            if (shouldOpenCompletionAfter(evt)) {
                SwingUtilities.invokeLater(Completion.get()::showCompletion);
                completionInvoked = true;
            }
            List<TextSyncGroup<CodeTemplateInsertHandler>> removedGroups = evt.<CodeTemplateInsertHandler>removedGroups();
            for (int i = removedGroups.size() - 1; i >= 0; i--) {
                CodeTemplateInsertHandler handler = removedGroups.get(i).clientInfo();
                if (handler == this) {
                    release();
                    break;
                }
            }

            if (!removedGroups.isEmpty()) {
                TextSync textSync = trm.activeTextSync();
                if (textSync != null) {
                    TextSyncGroup<CodeTemplateInsertHandler> activeGroup = textSync.<CodeTemplateInsertHandler>group();
                    CodeTemplateInsertHandler activeHandler = activeGroup.clientInfo();
                    if (activeHandler == this) {
                        textSync.syncByMaster();
                        CodeTemplateParameterImpl activeMasterImpl = textSync.<CodeTemplateParameterImpl>masterRegion().clientInfo();
                        activeMasterImpl.markUserModified();
                        component.getDocument().putProperty(CT_HANDLER_DOC_PROPERTY, this);
                    }
                } else { // No active text sync - all released
                    component.getDocument().putProperty(CT_HANDLER_DOC_PROPERTY, null);
                }
            }

        } else { // Modification change
            TextSync activeTextSync = trm.activeTextSync();
            CodeTemplateParameterImpl activeMasterImpl = activeTextSync.<CodeTemplateParameterImpl>masterRegion().clientInfo();
            if (activeMasterImpl != null) {
                activeMasterImpl.markUserModified();
                notifyParameterUpdate(activeMasterImpl.getParameter(), true);
            }
        }
    }

    private static boolean shouldOpenCompletionAfter(TextRegionManagerEvent evt) {
        TextSync activeTextSync = evt.activeTextSync();
        if (activeTextSync != null && activeTextSync.isCompletionInvoke()) {
            return true;
        }
        List<TextSyncGroup<Object>> removed = evt.removedGroups();
        if (removed.isEmpty() || evt.previousTextSync() != null) {
            return false;
        }
        TextSync last = removed.get(removed.size()-1).activeTextSync();
        return last != null && last.isCaretMarker() && last.isCompletionInvoke();
    }

    void release() {
        synchronized (this) {
            if (released) {
                return;
            }
            this.released = true;
        }

        TextRegionManager trm = textRegionManager();
        if (textSyncGroup.textRegionManager() == trm) {
            trm.stopGroupEditing(textSyncGroup);
        }
        trm.removeTextRegionManagerListener(this);
        if (LOG.isLoggable(Level.FINE)) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.INFO, "", new Exception());
            }
            LOG.fine("CodeTemplateInsertHandler.release() CALLED\n");
            LOG.fine(toStringDetail());
        }

        // Notify processors
        for (CodeTemplateProcessor processor : processors) {
            processor.release();
        }
    }

    private String buildInsertText() {
        return parametrizedTextParser.buildInsertText(allParameters);
    }

    @Override
    public String toString() {
        return "Abbrev: \"" + codeTemplate.getAbbreviation() + "\"";
    }

    String toStringDetail() {
        StringBuilder sb = new StringBuilder();
        for (CodeTemplateParameter param : allParameters) {
            CodeTemplateParameterImpl paramImpl = CodeTemplateParameterImpl.get(param);
            sb.append("  ").append(paramImpl.getName()).append(":");
            sb.append(paramImpl.textRegion());
            if (!paramImpl.isSlave()) {
                sb.append(" Master");
            }
            sb.append('\n');
        }
        return sb.toString();
    }
    /**
     * #181703 - Allow prioritizing parameters in a code-template.
     * Package private for testing
     */
    static List<CodeTemplateParameter> prioritizeParameters(List<CodeTemplateParameter> params) {

        List<CodeTemplateParameter> result = new ArrayList<>(params);
        result.sort(new Comparator<CodeTemplateParameter>() {
            @Override
            public int compare(CodeTemplateParameter p1, CodeTemplateParameter p2) {
                return getPrio(p1) - getPrio(p2);
            }

            private int getPrio(CodeTemplateParameter templateParam) throws NumberFormatException {
                if (null == templateParam) {
                    return MAX_VALUE;
                }
                String value = templateParam.getHints().get(CodeTemplateParameter.ORDERING_HINT_NAME);
                if (null != value) {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        // ignore
                        return MAX_VALUE;
                    }
                }
                return MAX_VALUE;
            }
        });
        return result;
    }

    private static final class TemplateInsertUndoEdit extends AbstractUndoableEdit {
        
        private final Document doc;

        private boolean inactive;
        
        TemplateInsertUndoEdit(Document doc) {
            assert (doc != null);
            this.doc = doc;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            if (!inactive) {
                inactive = true;
                CodeTemplateInsertHandler handler = (CodeTemplateInsertHandler) doc.getProperty(CT_HANDLER_DOC_PROPERTY);
                if (handler != null) {
                    handler.release();
                }
            }
        }

    }

}
