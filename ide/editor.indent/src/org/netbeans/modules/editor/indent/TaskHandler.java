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

package org.netbeans.modules.editor.indent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.editor.indent.spi.ReformatTask;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;

/**
 * Indentation and code reformatting services for a swing text document.
 *
 * @author Miloslav Metelka
 */
public final class TaskHandler {
    
    // -J-Dorg.netbeans.modules.editor.indent.TaskHandler.level=FINE
    private static final Logger LOG = Logger.getLogger(TaskHandler.class.getName());
    
    private final boolean indent;
    
    private final Document doc;

    private List<MimeItem> items;

    /**
     * Start position of the currently formatted chunk.
     */
    private Position startPos;

    /**
     * End position of the currently formatted chunk.
     */
    private Position endPos;
    
    private Position caretPos;
    
    private final Set<Object> existingFactories = new HashSet<Object>();

    private Lookup lookup = null;
    

    TaskHandler(boolean indent, Document doc) {
        this.indent = indent;
        this.doc = doc;
    }

    public Lookup getLookup() {
        return lookup;
    }

    public boolean isIndent() {
        return indent;
    }

    public Document document() {
        return doc;
    }
    
    public int caretOffset() {
        return caretPos.getOffset();
    }

    public void setCaretOffset(int offset) throws BadLocationException {
        caretPos = doc.createPosition(offset);
    }
    
    public Position startPos() {
        return startPos;
    }
    
    public Position endPos() {
        return endPos;
    }

    void setGlobalBounds(Position startPos, Position endPos) {
        assert (startPos.getOffset() <= endPos.getOffset())
                : "startPos=" + startPos.getOffset() + " < endPos=" + endPos.getOffset();
        this.startPos = startPos;
        this.endPos = endPos;
    }

    boolean collectTasks() {
        TokenHierarchy<?> th = TokenHierarchy.get(document());
        Set<LanguagePath> languagePathSet = Collections.emptySet();
        if (doc instanceof AbstractDocument) {
            AbstractDocument adoc = (AbstractDocument)doc;
            adoc.readLock();
            try {
                languagePathSet = th.languagePaths();
                List<LanguagePath> languagePaths = new ArrayList<LanguagePath>(languagePathSet);
                languagePaths.sort(LanguagePathSizeComparator.ASCENDING);
                for (LanguagePath lp : languagePaths) {
                    addItem(MimePath.parse(lp.mimePath()), lp);
                }
            } finally {
                adoc.readUnlock();
            }
        }
        
        if (languagePathSet.isEmpty()) {
            addItem(MimePath.parse(docMimeType()), null);
        }

        // XXX: HACK TODO PENDING WORKAROUND
        // Temporary Workaround: the HTML formatter clobbers the Ruby formatter's
        // work so make sure the Ruby formatter gets to work last in RHTML files
        //
        // The problem is that both html and ruby formatters have language paths
        // of the same lenght and therefore their ordering is undefined.
        // This will be solved in the infrastructure by segmenting the formatted
        // area by the language paths. And calling each formatter task only
        // with the segments that belong to it.
        if (items != null && "application/x-httpd-eruby".equals(docMimeType())) { //NOI18N
            // Copy list, except for Ruby element, which we then add at the end
            List<MimeItem> newItems = new ArrayList<MimeItem>(items.size());
            MimeItem rubyItem = null;
            for (MimeItem item : items) {
                if (item.mimePath().getPath().endsWith("text/x-ruby")) { // NOI18N
                    rubyItem = item;
                } else {
                    newItems.add(item);
                }
            }
            if (rubyItem != null) {
                newItems.add(rubyItem);
            }
            items = newItems;
        }

        // current PHP formatter must run after HTML formatter
        if (items != null && "text/x-php5".equals(docMimeType())) { //NOI18N
            // Copy list, except for Ruby element, which we then add at the end
            List<MimeItem> newItems = new ArrayList<MimeItem>(items.size());
            MimeItem phpItem = null;
            for (MimeItem item : items) {
                if (item.mimePath().getPath().endsWith("text/x-php5")) { // NOI18N
                    phpItem = item;
                } else {
                    newItems.add(item);
                }
            }
            if (phpItem != null) {
                newItems.add(phpItem);
            }
            items = newItems;
        }

        // XXX : Hack to get javascript formatter running as last one
        if (items != null && items.size() > 1 && "text/javascript".equals(docMimeType())) { //NOI18N
            // Copy list, except for JS element, which we then add at the end
            List<MimeItem> newItems = new ArrayList<MimeItem>(items.size());
            MimeItem jsItem = null;
            for (MimeItem item : items) {
                if (item.mimePath().getPath().endsWith("text/javascript")) { // NOI18N
                    jsItem = item;
                } else {
                    newItems.add(item);
                }
            }
            if (jsItem != null) {
                newItems.add(jsItem);
            }
            items = newItems;
        }
        
        // XXX: HACK TODO PENDING WORKAROUND
        // A hotfix for #116022: the jsp formatter must be called first and the html formatter second
        if (items != null && "text/x-jsp".equals(docMimeType()) || "text/x-tag".equals(docMimeType())) { //NOI18N
            List<MimeItem> newItems = new ArrayList<MimeItem>(items.size());
            MimeItem htmlItem = null;
            MimeItem jspItem = null;
            for (MimeItem item : items) {
                if (item.mimePath().getPath().endsWith("text/html")) { //NOI18N
                    htmlItem = item;
                } else if (item.mimePath().getPath().endsWith("text/x-jsp") //NOI18N
                        || item.mimePath().getPath().endsWith("text/x-tag")) { //NOI18N
                    jspItem = item;
                } else {
                    newItems.add(item);
                }
            }

            if (htmlItem != null) {
                newItems.add(0, htmlItem);
            }

            if (jspItem != null) {
                newItems.add(0, jspItem);
            }

            items = newItems;
        }

        if (items != null) {
            List<Lookup> lookups = new ArrayList<Lookup>();
            for (MimeItem mi : items) {
                Lookup l = mi.getLookup();
                if (l != null) {
                    lookups.add(l);
                }
            }
            if (lookups.size() > 0) {
                lookup = new ProxyLookup(lookups.toArray(new Lookup[0]));
            }
        }

        if (lookup == null) {
            lookup = Lookup.EMPTY;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Collected items: "); //NOI18N
            if (items != null) {
                for (MimeItem mi : items) {
                    LOG.fine("  Item: " + mi); //NOI18N
                }
            }
            LOG.fine("-----------------"); //NOI18N
        }
        
        return (items != null);
    }
    
    void lock() {
        if (items != null) {
            int i = 0;
            try {
                for (; i < items.size(); i++) {
                    MimeItem item = items.get(i);
                    item.lock();
                }
            } finally {
                if (i < items.size()) { // Locking of i-th item has failed
                    // Unlock the <0,i-1> items that are already locked
                    // Assuming that the unlock() for already locked items will pass
                    while (--i >= 0) {
                        MimeItem item = items.get(i);
                        item.unlock();
                    }
                }
            }
        }
    }

    void unlock() {
        if (items != null) {
            for (MimeItem item : items) {
                item.unlock();
            }
        }
    }

    boolean hasFactories() {
        String mimeType = docMimeType();
        return (mimeType != null && new MimeItem(this, MimePath.get(mimeType), null).hasFactories());
    }

    boolean hasItems() {
        return (items != null);
    }

    void runTasks() throws BadLocationException {
        // Run top-level task and possibly embedded tasks according to the context
        if (items == null) // Do nothing for no items
            return;

        // Start with the doc's mime type's task
        for (MimeItem item : items) {
            item.runTask();
        }
    }

    private boolean addItem(MimePath mimePath, LanguagePath languagePath) {
        MimeItem item = new MimeItem(this, mimePath, languagePath);
        if (item.createTask(existingFactories)) {
            if (items == null) {
                items = new ArrayList<MimeItem>();
            }
            items.add(item);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Adding MimeItem: " + item); //NOI18N
            }
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Collect language paths used within the given token sequence
     * 
     * @param ts non-null token sequence (or subsequence). <code>ts.moveNext()</code>
     * is called first on it.
     * @return collection of language paths present in the given token sequence.
     */
    private Collection<LanguagePath> getActiveEmbeddedPaths(TokenSequence ts) {
        Collection<LanguagePath> lps = new HashSet<LanguagePath>();
        lps.add(ts.languagePath());
        List<TokenSequence<?>> tsStack = null;
        while (true) {
            while (ts.moveNext()) {
                TokenSequence<?> eTS = ts.embedded();
                if (eTS != null) {
                    tsStack.add(ts);
                    ts = eTS;
                    lps.add(ts.languagePath());
                }
            }
            if (tsStack != null && tsStack.size() > 0) {
                ts = tsStack.get(tsStack.size() - 1);
                tsStack.remove(tsStack.size() - 1);
            } else {
                break;
            }
        }
        return lps;
    }

    private String docMimeType() {
        return (String)document().getProperty("mimeType"); //NOI18N
    }
        
    /**
     * Item that services indentation/reformatting for a single mime-path.
     */
    public static final class MimeItem {
        
        private final TaskHandler handler;
        
        private final MimePath mimePath;
        
        private final LanguagePath languagePath;
        
        private IndentTask indentTask;
        
        private ReformatTask reformatTask;
        
        private ExtraLock extraLock;
        
        private Context context;

        MimeItem(TaskHandler handler, MimePath mimePath, LanguagePath languagePath) {
            this.handler = handler;
            this.mimePath = mimePath;
            this.languagePath = languagePath;
        }

        public MimePath mimePath() {
            return mimePath;
        }
        
        public LanguagePath languagePath() {
            return languagePath;
        }

        public Context context() {
            if (context == null) {
                context = IndentSpiPackageAccessor.get().createContext(this);
            }
            return context;
        }
        
        public TaskHandler handler() {
            return handler;
        }
        
        boolean hasFactories() {
            Lookup lookup = MimeLookup.getLookup(mimePath);
            return handler().isIndent()
                    ? (lookup.lookup(IndentTask.Factory.class) != null)
                    : (lookup.lookup(ReformatTask.Factory.class) != null);
        }
        
        public List<Context.Region> indentRegions() {
            Document doc = handler.document();
            List<Context.Region> indentRegions = new ArrayList<Context.Region>();
            AbstractDocument adoc = null;
            if (doc instanceof AbstractDocument) {
                adoc = (AbstractDocument) doc;
                adoc.readLock();
            }
            try {
                int startOffset = handler.startPos().getOffset();
                int endOffset = handler.endPos().getOffset();
                if (endOffset > doc.getLength())
                    endOffset = Integer.MAX_VALUE;
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("indentRegions: startOffset=" + startOffset + ", endOffset=" + endOffset + '\n'); //NOI18N
                }
                if (languagePath != null && startOffset < endOffset) {
                    List<TokenSequence<?>> tsl = TokenHierarchy.get(doc).tokenSequenceList(languagePath,
                            startOffset, endOffset);
                    for (TokenSequence<?> ts : tsl) {
                        ts.moveStart();
                        if (ts.moveNext()) { // At least one token
                            int regionStartOffset = ts.offset();
                            ts.moveEnd(); // At least one token exists
                            ts.movePrevious();
                            int regionEndOffset = ts.offset() + ts.token().length();
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("  Region[" + indentRegions.size() + // NOI18N
                                        "]: startOffset=" + regionStartOffset + ", endOffset=" + regionEndOffset + '\n'); //NOI18N
                            }
                            // Only within global boundaries
                            if (regionStartOffset <= endOffset && regionEndOffset >= startOffset) {
                                regionStartOffset = Math.max(regionStartOffset, startOffset);
                                regionEndOffset = Math.min(regionEndOffset, endOffset);
                                MutablePositionRegion region = new MutablePositionRegion(
                                        doc.createPosition(regionStartOffset),
                                        doc.createPosition(regionEndOffset)
                                );
                                indentRegions.add(IndentSpiPackageAccessor.get().createContextRegion(region));
                            }
                        }
                    }
                } else { // used when no token hierarchy exists
                    MutablePositionRegion wholeDocRegion = new MutablePositionRegion(handler.startPos,
                            handler.endPos);
                    indentRegions.add(IndentSpiPackageAccessor.get().createContextRegion(wholeDocRegion));
                }
                
                // Filter out guarded regions
//                if (indentRegions.size() > 0 && doc instanceof GuardedDocument) {
//                    MutablePositionRegion region = IndentSpiPackageAccessor.get().positionRegion(indentRegions.get(0));
//                    int regionStartOffset = region.getStartOffset();
//                    GuardedDocument gdoc = (GuardedDocument)doc;
//                    int gbStartOffset = guardedBlocks.adjustToBlockEnd(region.getEndOffset());
//                    MarkBlockChain guardedBlocks = gdoc.getGuardedBlockChain();
//                    if (guardedBlocks != null && guardedBlocks.getChain() != null) {
//                        int gbStartOffset = guardedBlocks.adjustToNextBlockStart(indentRegions.getStartOffset());
//                        int regionIndex = 0;
//                        while (regionIndex < indentRegions.size()) { // indentRegions can be mutated dynamically
//                            MutablePositionRegion region = IndentSpiPackageAccessor.get().positionRegion(indentRegions.get(regionIndex));
//                            int gbStartOffset = guardedBlocks.adjustToNextBlockStart(region.getStartOffset());
//                            int gbEndOffset = guardedBlocks.adjustToBlockEnd(region.getEndOffset());
//
//                            while (pos < endPosition.getOffset()) {
//                                int stopPos = endPosition.getOffset();
//                                if (gdoc != null) { // adjust to start of the next guarded block
//                                    stopPos = gdoc.getGuardedBlockChain().adjustToNextBlockStart(pos);
//                                    if (stopPos == -1 || stopPos > endPosition.getOffset()) {
//                                        stopPos = endPosition.getOffset();
//                                    }
//                                }
//
//                                if (pos < stopPos) {
//                                    int reformattedLen = formatter.reformat(doc, pos, stopPos);
//                                    pos = pos + reformattedLen;
//                                } else {
//                                    pos++; //ensure to make progress
//                                }
//
//                                if (gdoc != null) { // adjust to end of current block
//                                    pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
//                                }
//                            }
//                        }
//                    }
//                }
            } catch (BadLocationException e) {
                Exceptions.printStackTrace(e);
                indentRegions = Collections.emptyList();
            } finally {
                if (adoc != null) {
                    adoc.readUnlock();
                }
            }
            return indentRegions;
        }
        
        boolean createTask(Set<Object> existingFactories) {
            Lookup lookup = MimeLookup.getLookup(mimePath);
            if (!handler.isIndent()) { // Attempt reformat task first
                ReformatTask.Factory factory = lookup.lookup(ReformatTask.Factory.class);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("'" + mimePath.getPath() + "' supplied ReformatTask.Factory: " + factory); //NOI18N
                }
                if (factory != null && (reformatTask = factory.createTask(context())) != null
                && !existingFactories.contains(factory)) {
                    extraLock = reformatTask.reformatLock();
                    existingFactories.add(factory);
                    return true;
                }
            }
            
            if (handler.isIndent() || reformatTask == null) { // Possibly fallback to reindent for reformatting
                IndentTask.Factory factory = lookup.lookup(IndentTask.Factory.class);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("'" + mimePath.getPath() + "' supplied IndentTask.Factory: " + factory); //NOI18N
                }
                if (factory != null && (indentTask = factory.createTask(context())) != null
                && !existingFactories.contains(factory)) {
                    extraLock = indentTask.indentLock();
                    existingFactories.add(factory);
                    return true;
                }
            }
            return false;
        }
        
        void lock() {
            if (extraLock != null)
                extraLock.lock();
        }
        
        void runTask() throws BadLocationException {
            if (indentTask != null) {
                indentTask.reindent();
            } else {
                reformatTask.reformat();
            }
        }
        
        void unlock() {
            if (extraLock != null)
                extraLock.unlock();
        }
        
        public @Override String toString() {
            return mimePath + ": " + ((indentTask != null) ? "IT: " + indentTask : "RT: " + reformatTask); //NOI18N
        }

        private Lookup getLookup() {
            if (indentTask instanceof Lookup.Provider) {
                return ((Lookup.Provider)indentTask).getLookup();
            } else if (reformatTask instanceof Lookup.Provider) {
                return ((Lookup.Provider)reformatTask).getLookup();
            } else {
                return null;
            }
        }
    }
    
    private static final class LanguagePathSizeComparator implements Comparator<LanguagePath> {
        
        static final LanguagePathSizeComparator ASCENDING = new LanguagePathSizeComparator(false);

        private final boolean reverse;
        
        public LanguagePathSizeComparator(boolean reverse) {
            this.reverse = reverse;
        }
        
        @Override
        public int compare(LanguagePath lp1, LanguagePath lp2) {
            int result = lp1.size() - lp2.size();
            if(result == 0) {
                for(int i = 0; i < lp1.size(); i++) {
                    String mime1 = lp1.language(i).mimeType();
                    String mime2 = lp2.language(i).mimeType();
                    // Ensure the formatter/indenter for javascript is invoked
                    // after the CSS identer. When the two mimetypes are
                    // encountered at the same level,
                    if(mime1.equals("text/css") && mime2.equals("text/javascript")) {
                        result = 1;
                        break;
                    } else if (mime1.equals("text/javascript") && mime2.equals("text/css")) {
                        result = -1;
                        break;
                    } else {
                        result = mime1.compareTo(mime2);
                        if(result != 0) {
                            break;
                        }
                    }
                }
            }
            return reverse ? (-1 * result) : result;
        }
    } // End of MimePathSizeComparator class
    
}
