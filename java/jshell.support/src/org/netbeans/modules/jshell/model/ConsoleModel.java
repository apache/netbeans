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
package org.netbeans.modules.jshell.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Position;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import org.netbeans.lib.nbjshell.SnippetWrapping;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.GuardedException;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.jshell.parsing.JShellParser;
import org.netbeans.modules.jshell.parsing.ModelAccessor;
import org.netbeans.modules.jshell.parsing.ShellAccessBridge;
import org.netbeans.modules.jshell.parsing.SnippetRegistry;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 * .
 * The Model is in two states: WRITE and EXECUTE. When the command is going to be executed by JShell,
 * the model switches to EXECUTE state and the document becomes r/o. Former input section is added
 * to the scrollback and any SnippetEvents which come form the JShell will be linked to that section.
 * 
 * Maintains a collection of sections from the scrollback, as the scrollback cannot change. Input section
 * may be invalidated.
 * <p/>
 * The last known editable ConsoleSection is served from {@link #getInputSection}. It may be null in the 
 * case that the JShell is executing the command and therefore there is no input section to write into. 
 * After document is modified and before the input section is revalidated, the old input section is served.
 * Use {@link #getInputEndOffset()} to get the current end offset or -1 if no active input section.
 * 
 * <p/>
 * <b>Threading model:</b> all updates must be done either in JSHell evaluator thread, or when the JShell evaluator
 * thread does not evaluate user code. Calls from Parsing API may perform immediate updates provided that the
 * evaluator does not evaluate user code.
 * 
 * @author sdedic
 */
public class ConsoleModel {
    private static Logger LOG = Logger.getLogger(ConsoleModel.class.getName());
    
    private volatile boolean valid;

    private final ShellAccessBridge   shellBridge;
    
    /**
     * The document for console contents
     */
    private final LineDocument document;

    /**
     * Start of the unprocessed text
     */
    private int processed;

    /**
     * Track possible changes, moves atomically as the input is being typed.
     */
    private Position inputEndPos = null;
    
    private volatile ConsoleSection executingSection;

    /**
     * Sections which were parsed out of the document, or seen previously
     */
    private List<ConsoleSection> scrollbackSections = new ArrayList<>();
    
    /**
     * Last section, which may be appended to, if further input comes and
     * fits into that section. This section may be even input one,
     * if executing: a trailing whitespace is appended to it.
     */
    private ConsoleSection lastSection = null;

    /**
     * Separate input section, which is writable and will be reparsed frequently
     */
    private ConsoleSection inputSection;
    
    private final RequestProcessor evaluator;
    
    /**
     * Position of the progress indicator. The model will ignore changes past the progress
     * position.
     */
    private int progressPos = -1;
    
    /**
     * True, if the shell is now executing a command.
     */
    private volatile boolean executing;

    public synchronized int getInputEndOffset() {
        Position p = inputEndPos;
        return p == null ? document.getLength() : p.getOffset();
    }
    
    private Position inputOffset;
    
    private boolean writingResponse;
    
    private volatile boolean inputValid = false;
    
    private RequestProcessor.Task inputTask;
    
    private ConsoleModel(ConsoleModel orig, ConsoleSection replaceInput) {
        this.scrollbackSections = new ArrayList<>(orig.scrollbackSections);
        if (orig.lastSection != null) {
            // we can freeze the section in the scrollback
            scrollbackSections.add(orig.lastSection);
        }
        if (replaceInput != null) {
            this.inputSection = replaceInput;
        } else {
            this.inputSection = orig.inputSection;
        }
        this.executingSection = orig.executingSection;
        this.document = orig.document;
        this.evaluator = orig.evaluator;
        this.progressPos = orig.progressPos;
        this.sections = new HashMap<>(orig.sections);
        this.snippets = new HashMap<>(orig.snippets);
        this.inputValid = true;
        this.shellBridge = orig.shellBridge;
    }
    
    public boolean isWritingResponse() {
        return writingResponse;
    }
    
    public int getWritablePos() {
        ConsoleSection s = getInputSection();
        return s == null ? document.getLength() + 1 : s.getPartBegin();
    }

    public int getInputOffset() {
        ConsoleSection s = getInputSection();
        return isExecute() || s == null ? -1 : s.getStart();
    }
    
    private int getScrollbackEnd() {
        if (isExecute()) {
            if (executingSection == null) {
                if (lastSection != null) {
                   return lastSection.getEnd();
                }
                ConsoleSection s = getInputSection();
                if (s != null) {
                    return s.getStart();
                }
            }
            return document.getLength();
        }
        ConsoleSection s = getInputSection();
        if (inputOffset == null) {
            return s != null ? s.getStart() : document.getLength();
        } else {
            return inputOffset.getOffset();
        }
    }
    
    private volatile List<ConsoleListener>   listeners = Collections.emptyList();
    
    public synchronized void addConsoleListener(ConsoleListener l) {
        List<ConsoleListener> ll = new ArrayList<>(listeners);
        ll.add(l);
        listeners = ll;
    }
    
    public synchronized void removeConsoleListener(ConsoleListener l) {
        List<ConsoleListener> ll = new ArrayList<>(listeners);
        ll.remove(l);
        listeners = ll;
    }
    
    public void setProgressPos(int pos) {
        this.progressPos = pos;
    }
    
    public boolean isExecute() {
        return executing;
    }
    
    public synchronized ConsoleSection getExecutingSection() {
        return executingSection;
    }
    
    public synchronized ConsoleSection getLastInputSection() {
        return isExecute() ? executingSection : getInputSection();
    }
    
    public void updateIfIdle() {
        synchronized (this) {
            if (isExecute()) {
                return;
            }
        }
        processInputSection(false);
    }
    
    public ConsoleSection   parseInputSection(CharSequence snap) {
        InputReader rdr = new InputReader(snap);
        rdr.run();
        return rdr.newSection;
    }
    
    /**
     * Returns the input section information. If the data is inaccurate,
     * tries to refresh them before returning from the call. The call may block,
     * do not hold any lock neither to the ConsoleModel or write/atomic lock to the underlying
     * Document.
     * @return the input section
     */
    public ConsoleSection processInputSection(boolean force) {
        synchronized (this) {
            if (!shouldRefresh() && !force) {
                return getInputSection();
            }
        }
        refreshInput(force, true).waitFinished();
        return inputSection;
    }
    
    /**
     * Returns input section in a non-blocking manner. Returns the last known
     * state of the input section. Some document changes may not be processed 
     * yet, as the processing must be serialized off EDT to the JShell
     * thread. Use {@link #processInputSection} to get the fresh data.
     * 
     * @return the input section
     */
    public ConsoleSection getInputSection() {
        ConsoleSection i;
        synchronized (this) {
            if (isExecute()) {
                return null;
            }
        } 
        if (shouldRefresh()) {
            // in evaluator, the refresh happens immediately
            refreshInput(false, false);
        }
        return inputSection;
    }
    
    /**
     * True, if the thread itself is refreshing the model.
     */
    private boolean isRefreshPending() {
        return refreshPending.get();
    }
    
    private synchronized boolean shouldRefresh() {
        return !inputValid && inputTask == null;
    }
    
    private Task refreshInput(boolean force, boolean now) {
        Task t;
        boolean wait;
        synchronized (this) {
            boolean rp = isRefreshPending();
            if (rp || executing) {
                 // cannot refresh during execution. Must not refresh if the
                 // thread itself is the refresh one.
                 return Task.EMPTY;
            }
            // reset the valid flag
            inputValid = false;
            boolean sched = inputTask == null;
            if (inputTask != null) {
                if (!force) {
                    return inputTask;
                }
                if (inputTask.cancel()) {
                    inputTask.schedule(now ? 0 : 200);
                    sched = true;
                }
            }
            if (sched) {
                InputReader r = new InputReader();
                inputTask = evaluator.post(r, now ? 0 : 200);
                r.myTask = inputTask;
            }
            t = inputTask;
            wait = now && !rp && evaluator.isRequestProcessorThread();
        }
        if (wait) {
            t.waitFinished();
        }
        return t;
    }
    
    private synchronized void clearInputTask(Task t) {
        if (t == inputTask) {
            inputTask = null;
        }
    }
    
    /**
     * If true, the refresh task is running. Input section is then to be returned
     * immediately, no wait on the refresh task to prevent self-deadlock.
     * Set and reset by the InputReader only.
     */
    private ThreadLocal<Boolean> refreshPending = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };
    
    private class InputReader extends EventBuffer implements Runnable {
        private int stage;
        private long docSerial;
        private CharSequence contents;
        private ConsoleSection newSection;
        private int inputStart;
        private long endSerial;
        private Position endPos;
        private int stalledInput;
        private Task myTask;
        private CharSequence processSnapshot;
        
        private List<ConsoleSection>    updateSections;
        
        private InputReader(CharSequence snapshot) {
            this.processSnapshot = snapshot;
        }
        
        private InputReader() {
        }
        
        @Override
        public void run() {
            switch (stage++) {
                case 0: 
                    LOG.log(Level.FINER, "InputReader starting");
                    doIt();
                    break;
                case 1:
                    try {
                        readContents();
                        if (contents != null) {
                            parseInput();
                            propagateResults();
                        }
                    } finally {
                        clearInputTask(myTask);
                    }
                    break;
            }
        }
        
        public void doIt() {
            // stage 0, read the document and serial
            synchronized (ConsoleModel.this) {
                refreshPending.set(true);
            }
            try {
                // stage 1
                if (processSnapshot != null) {
                    // do not use document lock, get the contents from the snapshot.
                    run();
                } else {
                    document.render(this);
                }
            } finally {
                synchronized (ConsoleModel.this) {
                    refreshPending.set(false);
                }
                stage = 0;
            }
        }
        
        private void readContents() {
            stalledInput = getInputOffset();
            if (isExecute() /* || stalledInput == -1 */) {
                return;
            }
            int is = getScrollbackEnd();
            if (stalledInput >= 0 && stalledInput < is) {
                inputStart = lastSection != null ? lastSection.getStart() : stalledInput;
                LOG.log(Level.FINER, "Detected stale input. Know input at {0} while anchor moved to {1}. LastSection = {2}, inputStart = {3}", new Object[] {
                    stalledInput, is, lastSection, inputStart
                });
            } else {
                inputStart = is;
            }
            try {
                if (processSnapshot != null) {
                    contents = processSnapshot.subSequence(inputStart, processSnapshot.length()).toString();
                    // intentionally do not fetch document's serial. the results will not update at the end
                } else {
                    contents = DocumentUtilities.getText(document, inputStart, document.getLength() - inputStart);
                    docSerial = DocumentUtilities.getDocumentVersion(document);
                }
            } catch (BadLocationException ex) {
            }
        }
        
        private void getPositionAndSerial() {
            if (newSection != null) {
                try {
                    if (newSection.getEnd() <= document.getLength()) {
                        endPos = document.createPosition(newSection.getEnd(), Position.Bias.Forward);
                    }
                } catch (BadLocationException ex) {
                }
            }
            endSerial = DocumentUtilities.getDocumentVersion(document);
        }
        
        private void propagateResults() {
            synchronized (ConsoleModel.this) {
                runUpdate();
            }
        }
        
        protected void doUpdates() {
            getPositionAndSerial();
            if (endSerial != docSerial) {
                LOG.log(Level.FINER, "Input has changed, discarding....");
                discardSection(newSection);
                return;
            }
            
            inputEndPos = endPos;
            inputValid = true;
            if (newSection != null) {
                try {
                    inputOffset = document.createPosition(newSection.getStart(), Position.Bias.Forward);
                } catch (BadLocationException ex) {
                    // should not happen, running inside readlock.
                }
            }
            if (updateSections != null) {
                for (ConsoleSection s : updateSections) {
                    if (lastSection != null && 
                        lastSection.getStart() > s.getStart()) {
                        continue;
                    }
                    addOrUpdate(s);
                }
            }
        }

        private void parseInput() {
            TokenHierarchy th;
            TokenSequence seq;
            int limit = contents.length();
            int initPos = 0;
            if (processSnapshot == null) {
                th = TokenHierarchy.get(getDocument()); 
                seq = th.tokenSequence();
                seq.move(inputStart);
                limit += inputStart;
            } else {
                th = TokenHierarchy.create(contents, Language.find("text/x-repl"));
                seq = th.tokenSequence();
                seq.move(0);
                initPos = inputStart;
            }
            
            JShellParser parser2 = new JShellParser(shellBridge, seq, initPos, limit);
            parser2.execute();
            
            List<ConsoleSection> newSections = new ArrayList<>(parser2.sections());
            if (newSections.isEmpty()) {
                return;
            }
            LOG.log(Level.FINER, "Read sections: {0}", newSections);
            
            int iindex = newSections.size() - 1;
            ConsoleSection newInput = newSections.get(iindex);
            if (!newInput.getType().input) {
                LOG.log(Level.FINER, "Last section was not input - bail out");
                return;
            }
            this.updateSections = newSections;
            this.newSection = newInput;
        }
    }
    
    private synchronized void discardSection(ConsoleSection section) {
        if (section == null) {
            return;
        }
        Collection<SnippetHandle> snips = snippets.remove(section);
        if (snips != null) {
            sections.keySet().removeAll(snips.stream().filter((org.netbeans.modules.jshell.model.SnippetHandle h) -> h.getSnippet() != null).map((org.netbeans.modules.jshell.model.SnippetHandle h) -> h.getSnippet()).collect(Collectors.toList())
            );
        }
    }
    
    private static final RequestProcessor RP = new RequestProcessor(ConsoleModel.class);
    
    private void notifyUpdated(ConsoleSection s) {
        ConsoleEvent e = new ConsoleEvent(this, s);
        listeners.stream().forEach(l -> l.sectionUpdated(e));
    }
    
    private void notifyUpdated(List<ConsoleSection> s) {
        ConsoleEvent e = new ConsoleEvent(this, s);
        listeners.stream().forEach(l -> l.sectionUpdated(e));
    }
    
    public Document getDocument() {
        return document;
    }
    
    private void notifyCreated(List<ConsoleSection> s) {
        ConsoleEvent e = new ConsoleEvent(this, s);
        listeners.stream().forEach(l -> l.sectionCreated(e));
    }
    
    private synchronized ConsoleSection getOpenSection() {
        if (lastSection != null) {
            return lastSection;
        } else if (executingSection != null) {
            return executingSection;
        } 
        return inputSection;
    }
    
    public synchronized ConsoleSection getLastSection() {
        ConsoleSection s = getOpenSection();
        if (s != null) {
            return s;
        }
        if (!scrollbackSections.isEmpty()) {
            return scrollbackSections.get(scrollbackSections.size() - 1);
        }
        return null;
    }
    
    private abstract class EventBuffer  {
        private List<ConsoleSection>    created;
        private List<ConsoleSection>    updated;
        ConsoleSection  prevInput;
        
        protected abstract void doUpdates();

        public void runUpdate() {
            List<ConsoleSection> myCreated;
            List<ConsoleSection> myUpdated;
            ConsoleSection executing = executingSection;
            boolean wasInput = executing == null && inputSection != null;
            boolean myInput;
            boolean same = true;
            
            synchronized (ConsoleModel.this) {
                created = new ArrayList<>();
                updated = new ArrayList<>();
                try {
                    doUpdates();
                } finally {
                    myCreated = created;
                    myUpdated = updated;
                    myInput = inputSection != null;
                    created = null;
                    updated = null;
                    // discard only the input, hopefully the preceding [output/message] section
                    // has no snippets
                    discardSection(prevInput);
                }
            }
            same &= myUpdated.isEmpty() && myCreated.isEmpty() && (myInput != wasInput);
            if (same) {
                return;
            }
            invalidate();
            RP.post(() -> {
                if (!myUpdated.isEmpty()) {
                    notifyUpdated(myUpdated);
                }
                if (!myCreated.isEmpty()) {
                    notifyCreated(myCreated);
                }
                if (!wasInput && myInput) {
                    if (executing != null) {
                        ConsoleEvent e = new ConsoleEvent(ConsoleModel.this, executing, wasInput);
                        listeners.stream().forEach(t -> t.executing(e));
                    }
                }
            });
        }

        protected void addOrUpdate(ConsoleSection s) {
            if (s.getType().input) {
                if (isExecute() || inputSection == null) {
                    created.add(s);
                } else {
                    updated.add(s);
                    prevInput = inputSection;
                }
                setInputSection(s);
            } else {
                int start = s.getStart();

                if (lastSection != null) {
                    if (start >= lastSection.getStart() && start <= lastSection.getEnd()) {
                        updated.add(s);
                    } else if (start < lastSection.getEnd()) {
                        throw new IllegalStateException();
                    } else {
                        scrollbackSections.add(lastSection);
                    }
                    lastSection = s;
                } else {
                    if (!scrollbackSections.isEmpty()) {
                        ConsoleSection last = scrollbackSections.get(scrollbackSections.size() - 1);
                        if (last.getEnd() > start) {
                            throw new IllegalStateException();
                        }
                    }
                    lastSection = s;
                    created.add(s);
                }
            }
        }

    }
    
    /**
     * Informs that text of the document has been changed.
     * 
     * @param start start of the change
     * @param end end of the change
     */
    public void textAppended(int end) {
        ConsoleSection last;
        int start;
        
        // we should have always a last section
        synchronized (this) {
            last = lastSection;
            if (last != null) {
                start = last.getStart();
            } else if (executingSection != null) {
                start = executingSection.getEnd();
            } else if (inputSection != null) {
                start = inputSection.getStart();
            } else {
                start = processed;
            }
        }
        
        TokenHierarchy th = TokenHierarchy.get(getDocument());
        TokenSequence seq = th.tokenSequence();
        assert seq != null;
        seq.move(start);
        JShellParser parser2 = new JShellParser(shellBridge, seq, 0, document.getLength());

        parser2.execute();
        List<ConsoleSection> sections = parser2.sections();
        if (sections.isEmpty()) {
            return;
        }
        
        synchronized(ConsoleModel.this) {
            new EventBuffer() {
                @Override
                protected void doUpdates() {
                    for (ConsoleSection s : sections) {
                        if (lastSection != null &&
                            s.getStart() < lastSection.getStart()) {
                            continue;
                        }
                        addOrUpdate(s);
                    }
                }
            }.runUpdate();
        }
        invalidate();
    }
    
    private synchronized void setInputSection(ConsoleSection s) {
        executingSection = null;
        if (s != null) {
            executing = false;
        }
        this.inputSection = s;
        inputValid = true;
        invalidate();
    }
    
    private void change(DocumentEvent e) {
        int s = e.getOffset();
        int l = e.getLength();
        ConsoleSection i = getInputSection();
        if (isExecute() /* || (i != null && s < i.getStart()) */) {
            if (progressPos != -1 && s >= progressPos) {
                return;
            }
            textAppended(document.getLength() + 1);
        } else if (inputSection != null && inputSection.getStart() < s) {
            refreshInput(true, false);
        }
    }
    
    private synchronized void invalidate() {
        allSections = null;
    }
    
    private List<ConsoleSection> allSections = null;
    
    public synchronized List<ConsoleSection> getSections() {
        if (allSections != null) {
            return allSections;
        }
        List<ConsoleSection> res = new ArrayList<>(scrollbackSections);
        if (lastSection != null) {
            res.add(lastSection);
        }
        ConsoleSection is = getInputSection();
        if (is != null) {
            res.add(is);
        }
        allSections = res;
        return res;
    }
    
    /**
     * Prepares for the JShell input execution.
     * Locks the document (moves writing pointer at the end), moves the current
     * input section to the scrollback
     * 
     */
    void beforeExecution(boolean external) {
        Task t = null;
        ConsoleSection is = null;
        while (true) {
            // wait after all refreshes are complete, block furthe refreshes by setting up executing flag
            synchronized (this) {
                assert !isExecute();
                t = inputTask;
            }
            if (t != null) {
                t.waitFinished();
            }
            is = getInputSection();
            synchronized (this) {
                if (inputTask == null) {
                    // no refresh is pending, change mode
                    executingSection = is;
                    executing = true;
                    break;
                }
            }
        }
        synchronized (this) {
            ConsoleSection finIs = is;
            if (finIs != null) {
                if (executingSection != null) {
                    // the input will be added to the scrollback; if something is still
                    // buffered in the lastSection, add it first:
                    if (lastSection != null) {
                        scrollbackSections.add(lastSection);
                    }
                    lastSection = null;
                    scrollbackSections.add(executingSection);
                }
            }
            if (is != null) {
                RP.post(() -> { 
                    // notify that the scrollback has been changed.
                    notifyUpdated(finIs); 

                    ConsoleEvent e = new ConsoleEvent(this, finIs, true);
                    listeners.stream().forEach(l -> l.executing(e));
                });
            }
        }
    }
    
    synchronized void afterExecution() {
//        if (lastSection != null) {
//            scrollbackSections.add(lastSection);
//        }
        ConsoleSection s = executingSection;
        if (s != null) {
            RP.post(() -> { 
                // execution finished.
                ConsoleEvent e = new ConsoleEvent(this, s, false);
                listeners.stream().forEach(l -> l.executing(e));
            });
        }
        executingSection = null;
        if (inputSection == null) {
            // create an input section forcefully
        }
    }
    
    /**
     * Provides mapping between snippets and individual input sections
     */
    private Map<Snippet, SnippetHandle> sections = new HashMap<>();
    private Map<ConsoleSection, List<SnippetHandle>> snippets = new HashMap<>();
    
    private class DocL implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            change(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            //change(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {}
    }
    
    private class DocFilter extends DocumentFilter {

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (!isValid()) {
                super.replace(fb, offset, length, text, attrs);
                return;
            }
            bypass = fb;
            int wr = getWritablePos();
            if (offset >= wr) {
                super.replace(fb, offset, length, text, attrs);
                return;
            }
            int endPos = offset + length;
            if (endPos < wr) {
                return;
            }
            int remainder = offset + length - wr;
            int prefix = wr - offset;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (!isValid()) {
                super.insertString(fb, offset, string, attr);
                return;
            }
            bypass = fb;
            if (offset >= getWritablePos()) {
                super.insertString(fb, offset, string, attr);
            } else {
                throw new GuardedException(string, offset);
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            if (!isValid()) {
                super.remove(fb, offset, length);
                return;
            }
            bypass = fb;
            if (offset >= getWritablePos() || length == 0) {
                super.remove(fb, offset, length);
            } else {
                throw new GuardedException(null, offset);
            }
        }
        
    }
    
    // initial bypass impl just writes to the document.
    private DocumentFilter.FilterBypass bypass = new DocumentFilter.FilterBypass() {
        @Override
        public Document getDocument() {
            return document;
        }

        @Override
        public void remove(int offset, int length) throws BadLocationException {
            if (bypass != this) {
                bypass.remove(offset, length);
            } else {
                document.remove(offset, length);
            }
        }

        @Override
        public void insertString(int offset, String string, AttributeSet attr) throws BadLocationException {
            if (bypass != this) {
                bypass.insertString(offset, string, attr);
            } else {
                document.insertString(offset, string, attr);
            }
        }

        @Override
        public void replace(int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
            if (bypass != this) {
                bypass.replace(offset, length, string, attrs);
            } else {
                document.remove(offset, length); 
                document.insertString(offset, string, attrs);
            }
        }
    };
    
    public static ConsoleModel get(Document d) {
        return (ConsoleModel)d.getProperty(ConsoleModel.class);
    }

    static ConsoleModel create(Document d, ShellAccessBridge shellBridge, RequestProcessor evaluator) {
        LineDocument ld = LineDocumentUtils.as(d, LineDocument.class);
        
        if (ld == null) {
            return null;
        }
        ConsoleModel mdl;
        synchronized (d) {
            mdl = (ConsoleModel)ld.getProperty(ConsoleModel.class);
            if (mdl != null) {
                return mdl;
            }
            mdl= new ConsoleModel(ld, evaluator, shellBridge);
            d.putProperty(ConsoleModel.class, mdl);
            mdl.init();
        }
        return mdl;
    }

    public ConsoleModel(LineDocument document, RequestProcessor evaluator, ShellAccessBridge shellBridge) {
        this.document = document;
        this.evaluator = evaluator;
        this.shellBridge = shellBridge;
    }
    
    private DocFilter f;
    private DocL l;
    
    private void init() {
        AbstractDocument ad = LineDocumentUtils.asRequired(document, AbstractDocument.class);
        this.valid = true;
        ad.setDocumentFilter(new DocFilter());
        try {
            // initialize the bypass:
            ad.replace(0, 0, "", null);
        } catch (BadLocationException ex) {
        }
        org.netbeans.lib.editor.util.swing.DocumentUtilities.addPriorityDocumentListener(document,
                l = new DocL(), DocumentListenerPriority.CARET_UPDATE);
    }
    
    public String getInputText() {
        int wr = getWritablePos();
        if (wr != -1) {
            String[] res = new String[1];
            document.render(() -> {
                try {
                    res[0] = document.getText(wr, document.getLength() - wr);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
            if (res[0] != null) {
                return res[0];
            }
        }
        return "";
    }
    
    public void insertResponseString(int offset, String text, AttributeSet attrs) throws BadLocationException {
        boolean saveResponse = writingResponse;
        try {
            writingResponse = true;
            getProtectionBypass().insertString(offset, text, null);   
        } finally {
            writingResponse = saveResponse;
        }
    }
    
    public void writeToShellDocument(String text) {
        AtomicLockDocument ald = LineDocumentUtils.asRequired(
                document, AtomicLockDocument.class);
        final boolean saveResponse = writingResponse;       
        try {
            ald.runAtomic(()-> {
                writingResponse = true;
                try {
                    int offset = getInputOffset();
                    if (offset == -1) {
                        offset = document.getLength();
                    }
                    getProtectionBypass().insertString(offset, text, null);
                    textAppended(offset);
                } catch (BadLocationException ex) {
                } finally {
                    writingResponse = saveResponse;
                }
            });
        } finally {
        }
    }
    
    public DocumentFilter.FilterBypass getProtectionBypass() {
        return bypass;
    }
    
    public List<String> history() {
        return scrollbackSections.stream().filter(s -> s.getType().input).
                map(s -> s.getContents(document)).collect(Collectors.toList());
    }
    
    static class ModelAccImpl extends ModelAccessor {

        @Override
        public ConsoleModel createModel(LineDocument document, RequestProcessor evaluator, ShellAccessBridge shellBridge) {
            return ConsoleModel.create(document, shellBridge, evaluator);
        }

        @Override
        public SnippetHandle createHandle(SnippetRegistry r, ConsoleSection s, Rng[] fragments, SnippetWrapping wrap, boolean transientSnippet) {
            return new SnippetHandle(r, s, fragments, wrap, transientSnippet);
        }

        @Override
        public void setFile(SnippetHandle h, FileObject f) {
            h.setFile(f);
        }

        @Override
        public ConsoleContents copyModel(ShellSession session, ConsoleModel m,Snapshot snapshot) {
            ConsoleContents c = new ConsoleContents(session, m.snapshot(snapshot.getText()), snapshot);
            return c;
        }
        
        @Override
        public void installSnippets(ConsoleContents contents, ConsoleSection s, List<SnippetHandle> snippets) {
            contents.installSnippetHandles(s, snippets);
        }

        @Override
        public void extendSection(ConsoleSection section, int start, int end, List<Rng> ranges, List<Rng> snippets) {
            if (ranges == null || ranges.isEmpty()) {
                section.extendWithPart(start, end);
            } else {
                section.extendToWithRanges(ranges);
            }
            if (snippets != null && snippets.size() > 1) {
                section.setSnippetRanges(snippets);
            }
        }

        @Override
        public void setSectionComplete(ConsoleSection target, boolean complete) {
            target.setComplete(complete);
        }

//        @Override
        public void beforeExecution(ConsoleModel model) {
            model.beforeExecution(false);
        }
        
        public void execute(ConsoleModel model, boolean external, Runnable c, Supplier<String> prompt)  {
            model.beforeExecution(external);
            try {
                c.run();
            } finally {
                model.ensureInputSectionAvailable(prompt);
                model.afterExecution();
            }
        }

//        @Override
        public void afterExecution(ConsoleModel model) {
            model.afterExecution();
        }
    }
    
    public boolean isValid() {
        return valid;
    }
    
    /**
     * Detaches the console model from the document and the JShell. The model
     * will not restrict or observe the document
     */
    public void detach() {
        synchronized (this) {
            if (!valid) {
                return;
            }
            valid = false;
        }
        document.putProperty(ConsoleModel.class, null);
        document.removeDocumentListener(l);
        ConsoleEvent ev = new ConsoleEvent(this, Collections.emptyList());
        listeners.stream().forEach(l -> l.closed(ev));
    }
    
    static {
        ModelAccessor.impl(new ModelAccImpl());
    }

    void ensureInputSectionAvailable(Supplier<String> promptSupplier) {
        ConsoleSection s = processInputSection(true);
        if (s != null) {
            return;
        }
        String promptText = "\n" + promptSupplier.get(); // NOI18N
        writeToShellDocument(promptText);
    }


    ConsoleModel snapshot(CharSequence s) {
       ConsoleSection input = parseInputSection(s);
       synchronized (this) {
           return new ConsoleModel(this, input);
       }
    }
    
    public static void initModel() {
        
    }
}
