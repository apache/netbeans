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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.CodeTemplateDescription;
import org.netbeans.api.editor.settings.CodeTemplateSettings;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.lib.editor.codetemplates.spi.*;
import org.netbeans.lib.editor.codetemplates.storage.CodeTemplateSettingsImpl;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * Code template allows the client to paste itself into the given
 * text component.
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateManagerOperation
    implements LookupListener, Runnable
{
    private static final Logger LOG = Logger.getLogger(CodeTemplateManagerOperation.class.getName());
    
    private static final Map<MimePath, CodeTemplateManagerOperation> mime2operation = 
            new WeakHashMap<MimePath, CodeTemplateManagerOperation>(8);
    
    private static final KeyStroke DEFAULT_EXPANSION_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
    
    public static synchronized CodeTemplateManager getManager(Document doc) {
        String mimeType = (String)doc.getProperty("mimeType"); //NOI18N
        return get(MimePath.parse(mimeType)).getManager();
    }

    public static synchronized CodeTemplateManagerOperation get(Document document, int offset) {
        MimePath mimePath = getFullMimePath(document, offset);
        if (mimePath != null) {
            return CodeTemplateManagerOperation.get(mimePath);
        } else {
            return null;
        }
    }
    
    public static synchronized CodeTemplateManagerOperation get(MimePath mimePath) {
        CodeTemplateManagerOperation operation = mime2operation.get(mimePath);
        if (operation == null) {
            operation = new CodeTemplateManagerOperation(mimePath);
            mime2operation.put(mimePath, operation);
        }
        return operation;
    }
    
    private final CodeTemplateManager manager;
    private final String mimePath;
    private final Lookup.Result<CodeTemplateSettings> ctslr;
    private final EventListenerList listenerList = new EventListenerList();

    private boolean loaded = false;
    private Map<String, CodeTemplate> abbrev2template = Collections.<String, CodeTemplate>emptyMap();
    private List<CodeTemplate> sortedTemplatesByAbbrev = Collections.<CodeTemplate>emptyList();
    private List<CodeTemplate> sortedTemplatesByParametrizedText = Collections.<CodeTemplate>emptyList();
    private List<CodeTemplate> selectionTemplates = Collections.<CodeTemplate>emptyList();
    private KeyStroke expansionKey = DEFAULT_EXPANSION_KEY;
    private String expansionKeyText = getExpandKeyStrokeText(expansionKey);

    // Do not store mimePath in a private field, it would break the WeakHashMap cache
    private CodeTemplateManagerOperation(MimePath mimePath) {
        this.mimePath = mimePath.getPath();
        this.manager = CodeTemplateApiPackageAccessor.get().createCodeTemplateManager(this);
        assert manager != null : "Can't creat CodeTemplateManager"; //NOI18N
        
        this.ctslr = MimeLookup.getLookup(mimePath).lookupResult(CodeTemplateSettings.class);
        this.ctslr.addLookupListener(WeakListeners.create(LookupListener.class, this, this.ctslr));
        
        // Compute descriptions asynchronously
        RequestProcessor.getDefault().post(this);
    }
    
    public String getMimePath() {
        return mimePath;
    }
    
    public CodeTemplateManager getManager() {
        return manager;
    }
    
    public Collection<? extends CodeTemplate> getCodeTemplates() {
        return sortedTemplatesByAbbrev;
    }
    
    public Collection<? extends CodeTemplate> findSelectionTemplates() {
        return selectionTemplates;
    }
    
    public CodeTemplate findByAbbreviation(String abbreviation) {
        return abbrev2template.get(abbreviation);
    }
    
    public Collection<? extends CodeTemplate> findByAbbreviationPrefix(String prefix, boolean ignoreCase) {
        List<CodeTemplate> result = new ArrayList<CodeTemplate>();
        
        int low = 0;
	int high = sortedTemplatesByAbbrev.size() - 1;
	while (low <= high) {
	    int mid = (low + high) >> 1;
	    CodeTemplate t = sortedTemplatesByAbbrev.get(mid);
	    int cmp = compareTextIgnoreCase(t.getAbbreviation(), prefix);

	    if (cmp < 0) {
		low = mid + 1;
            } else if (cmp > 0) {
		high = mid - 1;
            } else {
                low = mid;
		break;
            }
	}
        
        // Go back whether prefix matches the name
        int i = low - 1;
        while (i >= 0) {
            CodeTemplate t = sortedTemplatesByAbbrev.get(i);
            int mp = matchPrefix(t.getAbbreviation(), prefix);
            if (mp == MATCH_NO) { // not matched
                break;
            } else if (mp == MATCH_IGNORE_CASE) { // matched when ignoring case
                if (ignoreCase) { // do not add if exact match required
                    result.add(t);
                }
            } else { // matched exactly
                result.add(t);
            }
            i--;
        }
        
        i = low;
        while (i < sortedTemplatesByAbbrev.size()) {
            CodeTemplate t = sortedTemplatesByAbbrev.get(i);
            int mp = matchPrefix(t.getAbbreviation(), prefix);
            if (mp == MATCH_NO) { // not matched
                break;
            } else if (mp == MATCH_IGNORE_CASE) { // matched when ignoring case
                if (ignoreCase) { // do not add if exact match required
                    result.add(t);
                }
            } else { // matched exactly
                result.add(t);
            }
            i++;
        }
        
        return result;
    }
    
    public Collection<? extends CodeTemplate> findByParametrizedText(String prefix, boolean ignoreCase) {
        List<CodeTemplate> result = new ArrayList<CodeTemplate>();
        
        int low = 0;
	int high = sortedTemplatesByParametrizedText.size() - 1;
	while (low <= high) {
	    int mid = (low + high) >> 1;
	    CodeTemplate t = sortedTemplatesByParametrizedText.get(mid);
	    int cmp = compareTextIgnoreCase(t.getParametrizedText(), prefix);

	    if (cmp < 0) {
		low = mid + 1;
            } else if (cmp > 0) {
		high = mid - 1;
            } else {
                low = mid;
		break;
            }
	}
        
        // Go back whether prefix matches the name
        int i = low - 1;
        while (i >= 0) {
            CodeTemplate t = sortedTemplatesByParametrizedText.get(i);
            int mp = matchPrefix(t.getParametrizedText(), prefix);
            if (mp == MATCH_NO) { // not matched
                break;
            } else if (mp == MATCH_IGNORE_CASE) { // matched when ignoring case
                if (ignoreCase) { // do not add if exact match required
                    result.add(t);
                }
            } else { // matched exactly
                result.add(t);
            }
            i--;
        }
        
        i = low;
        while (i < sortedTemplatesByParametrizedText.size()) {
            CodeTemplate t = sortedTemplatesByParametrizedText.get(i);
            int mp = matchPrefix(t.getParametrizedText(), prefix);
            if (mp == MATCH_NO) { // not matched
                break;
            } else if (mp == MATCH_IGNORE_CASE) { // matched when ignoring case
                if (ignoreCase) { // do not add if exact match required
                    result.add(t);
                }
            } else { // matched exactly
                result.add(t);
            }
            i++;
        }
        
        return result;
    }
    
    public static Collection<? extends CodeTemplateFilter> getTemplateFilters(Document doc, int startOffset, int endOffset) {
        MimePath mimeType = getFullMimePath(doc, startOffset);
        Collection<? extends CodeTemplateFilter.Factory> filterFactories = 
            MimeLookup.getLookup(mimeType).lookupAll(CodeTemplateFilter.Factory.class);
        
        List<CodeTemplateFilter> result = new ArrayList<CodeTemplateFilter>(filterFactories.size());
        for (CodeTemplateFilter.Factory factory : filterFactories) {
            result.add(factory.createFilter(doc, startOffset, endOffset));
        }
        return result;
    }

    public static void insert(CodeTemplate codeTemplate, JTextComponent component) {
        String mimePath = CodeTemplateApiPackageAccessor.get().getCodeTemplateMimePath(codeTemplate);
        Collection<? extends CodeTemplateProcessorFactory> processorFactories = 
            MimeLookup.getLookup(mimePath).lookupAll(CodeTemplateProcessorFactory.class);
        
        CodeTemplateInsertHandler handler = new CodeTemplateInsertHandler(
                codeTemplate, component, processorFactories, CodeTemplateSettingsImpl.get(MimePath.parse(mimePath)).getOnExpandAction());
        handler.processTemplate();
    }
    
    /**
     * Match text against the given prefix.
     *
     * @param text text to be compared with the prefix.
     * @param prefix text to be matched as a prefix of the text parameter.
     * @return one of <code>MATCH_NO</code>, <code>MATCH_IGNORE_CASE</code>
     *  or <code>MATCH</code>
     */
    private static final int MATCH_NO = 0;
    private static final int MATCH_IGNORE_CASE = 1;
    private static final int MATCH = 2;
    private static int matchPrefix(CharSequence text, CharSequence prefix) {
        boolean matchCase = true;
        int prefixLength = prefix.length();
        if (prefixLength > text.length()) { // prefix longer than text
            return MATCH_NO;
        }
        int i;
        for (i = 0; i < prefixLength; i++) {
            char ch1 = text.charAt(i);
            char ch2 = prefix.charAt(i);
            if (ch1 != ch2) {
                matchCase = false;
                if (Character.toLowerCase(ch1) != Character.toLowerCase(ch2)) {
                    break;
                }
            }
        }
        if (i == prefixLength) { // compared all
            return matchCase ? MATCH : MATCH_IGNORE_CASE;
        } else { // not compared all => not matched
            return MATCH_NO;
        }
    }
    
    private static int compareTextIgnoreCase(CharSequence text1, CharSequence text2) {
        int len = Math.min(text1.length(), text2.length());
        for (int i = 0; i < len; i++) {
            char ch1 = Character.toLowerCase(text1.charAt(i));
            char ch2 = Character.toLowerCase(text2.charAt(i));
            if (ch1 != ch2) {
                return ch1 - ch2;
            }
        }
        return text1.length() - text2.length();
    }
    
    public boolean isLoaded() {
        synchronized (listenerList) {
            return loaded;
        }
    }
    
    public void registerLoadedListener(ChangeListener listener) {
        synchronized (listenerList) {
            if (!isLoaded()) {
                // not yet loaded
                listenerList.add(ChangeListener.class, listener);
                return;
            }
        }

        // already loaded
        listener.stateChanged(new ChangeEvent(manager));
    }
    
    public void waitLoaded() {
        synchronized (listenerList) {
            while(!isLoaded()) {
                try {
                    listenerList.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted when waiting to load code templates"); //NOI18N
                }
            }
        }
    }
    
    private void fireStateChanged(ChangeEvent evt) {
        Object[] listeners;
        synchronized (listenerList) {
            listeners = listenerList.getListenerList();
        }
        for (int i = 0; i < listeners.length; i += 2) {
            if (ChangeListener.class == listeners[i]) {
                ((ChangeListener)listeners[i + 1]).stateChanged(evt);
            }
        }
    }
    
    public void run() {
        rebuildCodeTemplates();
    }
    
    private static void processCodeTemplateDescriptions(
        CodeTemplateManagerOperation operation,
        Collection<? extends CodeTemplateDescription> ctds,
        Map<String, CodeTemplate> codeTemplatesMap,
        List<CodeTemplate> codeTemplatesWithSelection
    ) {
        for (CodeTemplateDescription ctd : ctds) {
            CodeTemplate ct = CodeTemplateApiPackageAccessor.get().createCodeTemplate(
                operation, 
                ctd.getAbbreviation(),
                ctd.getDescription(), 
                ctd.getParametrizedText(),
                ctd.getContexts(),
                ctd.getMimePath()
            );
            
            codeTemplatesMap.put(ct.getAbbreviation(), ct);
            if (ct.getParametrizedText().toLowerCase().indexOf("${selection") > -1) { //NOI18N
                codeTemplatesWithSelection.add(ct);
            }
        }
    }
    
    private void rebuildCodeTemplates() {
        Collection<? extends CodeTemplateSettings> allCts = ctslr.allInstances();
        CodeTemplateSettings cts = allCts.isEmpty() ? null : allCts.iterator().next();
        
        Map<String, CodeTemplate> map = new HashMap<String, CodeTemplate>();
        List<CodeTemplate> templatesWithSelection = new ArrayList<CodeTemplate>();
        KeyStroke keyStroke = DEFAULT_EXPANSION_KEY;
        
        if (cts != null) {
            // Load templates
            Collection<? extends CodeTemplateDescription> ctds = cts.getCodeTemplateDescriptions();
            processCodeTemplateDescriptions(this, ctds, map, templatesWithSelection);
            
            // Load expansion key
            keyStroke = patchExpansionKey(cts.getExpandKey());
        } else {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.warning("Can't find CodeTemplateSettings for '" + mimePath + "'"); //NOI18N
            }
        }

        List<CodeTemplate> byAbbrev = new ArrayList<CodeTemplate>(map.values());
        byAbbrev.sort(CodeTemplateComparator.BY_ABBREVIATION_IGNORE_CASE);

        List<CodeTemplate> byText = new ArrayList<CodeTemplate>(map.values());
        byText.sort(CodeTemplateComparator.BY_PARAMETRIZED_TEXT_IGNORE_CASE);
        
        templatesWithSelection.sort(CodeTemplateComparator.BY_PARAMETRIZED_TEXT_IGNORE_CASE);

        boolean fire = false;

        synchronized(listenerList) {
            fire = abbrev2template == null;

            abbrev2template = Collections.unmodifiableMap(map);
            sortedTemplatesByAbbrev = Collections.unmodifiableList(byAbbrev);
            sortedTemplatesByParametrizedText = Collections.unmodifiableList(byText);
            selectionTemplates = Collections.unmodifiableList(templatesWithSelection);
            expansionKey = keyStroke;
            expansionKeyText = getExpandKeyStrokeText(keyStroke);
            
            loaded = true;
            listenerList.notifyAll();
        }

        if (fire) {
            fireStateChanged(new ChangeEvent(manager));
        }
    }
    
    public KeyStroke getExpansionKey() {
        return expansionKey;
    }

    public String getExpandKeyStrokeText() {
        return expansionKeyText;
    }
    
    private static String getExpandKeyStrokeText(KeyStroke keyStroke) {
        String expandKeyStrokeText;
        if (keyStroke.equals(KeyStroke.getKeyStroke(' '))) { //NOI18N
            expandKeyStrokeText = "SPACE"; // NOI18N
        } else if (keyStroke.equals(KeyStroke.getKeyStroke(new Character(' '), InputEvent.SHIFT_MASK))) { //NOI18N
            expandKeyStrokeText = "Shift-SPACE"; // NOI18N
        } else if (keyStroke.equals(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0))) {
            expandKeyStrokeText = "TAB"; // NOI18N
        } else if (keyStroke.equals(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0))) {
            expandKeyStrokeText = "ENTER"; // NOI18N
        } else {
            expandKeyStrokeText = keyStroke.toString();
        }
        return expandKeyStrokeText;
    }
    
    private static KeyStroke patchExpansionKey(KeyStroke eks) {
	// Patch the keyPressed => keyTyped to prevent insertion of expand chars into editor
        if (eks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0))) {
            eks = KeyStroke.getKeyStroke(' ');
        } else if (eks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.SHIFT_MASK))) {
            eks = KeyStroke.getKeyStroke(new Character(' '), InputEvent.SHIFT_MASK);
        } else if (eks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0))) {
        } else if (eks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0))) {
        }
	return eks;
    }
    
    public void resultChanged(LookupEvent ev) {
        rebuildCodeTemplates();
    }
    
    private static MimePath getFullMimePath(Document document, int offset) {
        String langPath = null;

        if (document instanceof AbstractDocument) {
            AbstractDocument adoc = (AbstractDocument)document;
            adoc.readLock();
            try {
                List<TokenSequence<?>> list = TokenHierarchy.get(document).embeddedTokenSequences(offset, true);
                if (list.size() > 1) {
                    langPath = list.get(list.size() - 1).languagePath().mimePath();
                }
            } finally {
                adoc.readUnlock();
            }
        }

        if (langPath == null) {
            langPath = NbEditorUtilities.getMimeType(document);
        }

        if (langPath != null) {
            return MimePath.parse(langPath);
        } else {
            return null;
        }
    }
}
