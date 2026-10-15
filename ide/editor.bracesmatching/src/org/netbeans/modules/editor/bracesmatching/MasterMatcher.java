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
package org.netbeans.modules.editor.bracesmatching;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Vita Stejskal
 */
public final class MasterMatcher {

    private static final Logger LOG = Logger.getLogger(MasterMatcher.class.getName());
    
    public static final String PROP_SEARCH_DIRECTION = "nbeditor-bracesMatching-searchDirection"; //NOI18N
    public static final String D_BACKWARD = "backward-preferred"; //NOI18N
    public static final String D_FORWARD = "forward-preferred"; //NOI18N

    public static final String PROP_CARET_BIAS = "nbeditor-bracesMatching-caretBias"; //NOI18N
    public static final String B_BACKWARD = "backward"; //NOI18N
    public static final String B_FORWARD = "forward"; //NOI18N
    
    public static final String PROP_MAX_BACKWARD_LOOKAHEAD = "nbeditor-bracesMatching-maxBackwardLookahead"; //NOI18N
    public static final String PROP_MAX_FORWARD_LOOKAHEAD = "nbeditor-bracesMatching-maxForwardLookahead"; //NOI18N
    private static final int DEFAULT_MAX_LOOKAHEAD = 1;
    private static final int MAX_MAX_LOOKAHEAD = 256;

    // Just for debugging
    public static final String PROP_SHOW_SEARCH_PARAMETERS = "debug-showSearchParameters-dont-ever-use-it-or-you-will-die"; //NOI18N
    private static final AttributeSet CARET_BIAS_HIGHLIGHT = AttributesUtilities.createImmutable(StyleConstants.Underline, Color.BLACK);
    private static final AttributeSet MAX_LOOKAHEAD_HIGHLIGHT = AttributesUtilities.createImmutable(EditorStyleConstants.WaveUnderlineColor, Color.BLUE);
    
    private List<MatchListener> matchListeners = new LinkedList<MatchListener>();
    
    public void addMatchListener(MatchListener l) {
        synchronized (LOCK) {
            matchListeners.add(l);
        }
    }
    
    public void removeMatchListener(MatchListener l) {
        synchronized (LOCK) {
            matchListeners.remove(l);
        }
    }
    
    public static synchronized MasterMatcher get(JTextComponent component) {
        MasterMatcher mm = (MasterMatcher) component.getClientProperty(MasterMatcher.class);
        if (mm == null) {
            mm = new MasterMatcher(component);
            component.putClientProperty(MasterMatcher.class, mm);
        }
        return mm;
    }
    
    public static boolean isTaskCanceled() {
        Result threadTask = THREAD_RESULTS.get(Thread.currentThread());
        assert threadTask != null : "MatcherContext.isTaskCanceled() should only be called from the matcher task's thread"; //NOI18N
        return threadTask.isCanceled();
    }
    
    /* test */ public static void markTestThread() {
        MasterMatcher mm = new MasterMatcher(null);
        THREAD_RESULTS.put(Thread.currentThread(), mm.new Result(null, -1, null, null, -1, -1));
    }
    
    public void highlight(
        Document document,
        int caretOffset, 
        OffsetsBag highlights, 
        AttributeSet matchedColoring, 
        AttributeSet mismatchedColoring,
        AttributeSet matchedMulticharColoring,
        AttributeSet mismatchedMulticharColoring
    ) {
        assert document != null : "The document parameter must not be null"; //NOI18N
        assert highlights != null : "The highlights parameter must not be null"; //NOI18N
        assert matchedColoring != null : "The matchedColoring parameter must not be null"; //NOI18N
        assert mismatchedColoring != null : "The mismatchedColoring parameter must not be null"; //NOI18N
        assert matchedMulticharColoring != null : "The matchedMulticharColoring parameter must not be null"; //NOI18N
        assert mismatchedMulticharColoring != null : "The mismatchedMulticharColoring parameter must not be null"; //NOI18N
        assert caretOffset >= 0 : "The caretOffset parameter must be >= 0"; //NOI18N
        
        fireMatchCleared();
        synchronized (LOCK) {
            Object allowedSearchDirection = getAllowedDirection();
            Object caretBias = getCaretBias();
            int maxBwdLookahead = getMaxLookahead(true);
            int maxFwdLookahead = getMaxLookahead(false);
            
            if (task != null) {
                // a task is running, perhaps just add a new job to it
                if (lastResult.getCaretOffset() == caretOffset && 
                    lastResult.getAllowedDirection() == allowedSearchDirection &&
                    lastResult.getCaretBias() == caretBias &&
                    lastResult.getMaxBwdLookahead() == maxBwdLookahead &&
                    lastResult.getMaxFwdLookahead() == maxBwdLookahead
                ) {
                    lastResult.addHighlightingJob(
                            highlights,
                            matchedColoring, mismatchedColoring,
                            matchedMulticharColoring, mismatchedMulticharColoring
                    );
                } else {
                    // Different request, cancel the current task
                    lastResult.cancel();
                    task = null;
                }
            }

            if (task == null) {
                // Remember the last request
                lastResult = new Result(document, caretOffset, allowedSearchDirection, caretBias, maxBwdLookahead, maxFwdLookahead);
                lastResult.addHighlightingJob(
                        highlights,
                        matchedColoring, mismatchedColoring,
                        matchedMulticharColoring, mismatchedMulticharColoring
                );

                // Fire up a new task
                task = PR.post(lastResult);
            }
        }
    }
    
    public void navigate(
        Document document,
        int caretOffset, 
        Caret caret,
        boolean select
    ) {
        assert document != null : "The document parameter must not be null"; //NOI18N
        assert caret != null : "The caret parameter must not be null"; //NOI18N
        assert caretOffset >= 0 : "The caretOffset parameter must be >= 0"; //NOI18N
        
        RequestProcessor.Task waitFor = null;
        
        synchronized (LOCK) {
            Object allowedSearchDirection = getAllowedDirection();
            Object caretBias = getCaretBias();
            int maxBwdLookahead = getMaxLookahead(true);
            int maxFwdLookahead = getMaxLookahead(false);

            boolean documentLocked = DocumentUtilities.isReadLocked(document);
            
            if (task != null) {
                // a task is running, perhaps just add a new job to it
                if (!documentLocked &&
                    lastResult.getCaretOffset() == caretOffset &&
                    lastResult.getAllowedDirection() == allowedSearchDirection &&
                    lastResult.getCaretBias() == caretBias &&
                    lastResult.getMaxBwdLookahead() == maxBwdLookahead &&
                    lastResult.getMaxFwdLookahead() == maxBwdLookahead
                ) {
                    lastResult.addNavigationJob(caret, select);
                    waitFor = task;
                } else {
                    // Different request, cancel the current task
                    lastResult.cancel();
                    task = null;
                }
            }

            if (task == null) {
                // Remember the last request
                lastResult = new Result(document, caretOffset, allowedSearchDirection, caretBias, maxBwdLookahead, maxFwdLookahead);
                lastResult.addNavigationJob(caret, select);

                if (documentLocked) {
                    // To prevent deadlocks as in #110500 we will run the task synchronously
                    lastResult.run();
                } else {
                    // Fire up a new task
                    task = PR.post(lastResult);
                    waitFor = task;
                }
            }
        }
        
        if (waitFor != null) {
            waitFor.waitFinished();
        }
    }
    
    private static final RequestProcessor PR = new RequestProcessor("EditorBracesMatching", 5, true); //NOI18N
    // package private just for tests
    /* package */ static final Map<Thread, Result> THREAD_RESULTS = Collections.synchronizedMap(new HashMap<Thread, Result>());

    private final String LOCK = new String("MasterMatcher.LOCK"); //NOI18N

    private final JTextComponent component;
    
    private RequestProcessor.Task task = null;
    private Result lastResult = null;
    
    private MasterMatcher(JTextComponent component) {
        this.component = component;
        if (component != null) {
            component.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("document".equals(evt.getPropertyName())) {
                        synchronized (LOCK) {
                            // Cancel any pending task and clear the lastResult
                            if (task != null) {
                                task.cancel();
                                task = null;
                            }
                            if (lastResult != null) {
                                lastResult.cancel();
                                lastResult = null; // Prevent memory leak upon document change
                            }
                        }
                    }
                }
            });
        }
    }

    private Object getAllowedDirection() {
        Object allowedDirection = component.getClientProperty(PROP_SEARCH_DIRECTION);
        return allowedDirection != null ? allowedDirection : D_BACKWARD;
    }

    private Object getCaretBias() {
        Object caretBias = component.getClientProperty(PROP_CARET_BIAS);
        return caretBias != null ? caretBias : B_BACKWARD;
    }

    private int getMaxLookahead(boolean backward) {
        String propName = backward ? PROP_MAX_BACKWARD_LOOKAHEAD : PROP_MAX_FORWARD_LOOKAHEAD;
        int maxLookahead = DEFAULT_MAX_LOOKAHEAD;
        Object value = component.getClientProperty(propName);
        if (value instanceof Integer) {
            maxLookahead = ((Integer) value).intValue();
        } else if (value != null) {
            try {
                maxLookahead = Integer.valueOf(value.toString());
            } catch (NumberFormatException nfe) {
                LOG.log(Level.WARNING, "Can't parse the value of " + propName + ": '" + value + "'", nfe); //NOI18N
            }
        }
        
        if (maxLookahead >= 0 && maxLookahead <= MAX_MAX_LOOKAHEAD) {
            return maxLookahead;
        } else {
            LOG.warning("Invalid value of " + propName + ": " + maxLookahead); //NOI18N
            return MAX_MAX_LOOKAHEAD;
        }
    }
    
    private static void highlightAreas(
        int [] origin, 
        int [] matches,
        OffsetsBag highlights, 
        AttributeSet matchedColoring, 
        AttributeSet mismatchedColoring,
        int maxOffset
    ) {
        // Remove all existing highlights
        highlights.clear();

        if (matches != null && matches.length >= 2) {
            // Highlight the matched origin
            placeHighlights(origin, true, highlights, matchedColoring, maxOffset);
            // Highlight all the matches
            placeHighlights(matches, false, highlights, matchedColoring, maxOffset);
        } else if (origin != null && origin.length >= 2) {
            // Highlight the mismatched origin
            placeHighlights(origin, true, highlights, mismatchedColoring, maxOffset);
        }
    }
    
    private Position[] toPositions(JTextComponent c, int[] offsets) throws BadLocationException {
        if (offsets == null) {
            return null;
        }
        Position[] ret = new Position[offsets.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = c.getDocument().createPosition(offsets[i]);
        }
        return ret;
    }
    
    private void fireMatchesHighlighted(Position[] origin, Position[] matches, BracesMatcher.ContextLocator locator) {
        MatchListener[] ll;
        synchronized (LOCK) {
            if (matchListeners.isEmpty()) {
                return;
            }
            ll = matchListeners.toArray(new MatchListener[0]);
        }
        if (ll.length == 0) {
            return;
        }
        MatchEvent evt = new MatchEvent(component, locator, this);
        evt.setHighlights(origin, matches);
        for (int i = 0; i < ll.length; i++) {
            MatchListener matchListener = ll[i];
            matchListener.matchHighlighted(evt);
        }
    }
    
    private void fireMatchCleared() {
        MatchListener[] ll;
        synchronized (LOCK) {
            if (matchListeners.isEmpty()) {
                return;
            }
            ll = matchListeners.toArray(new MatchListener[0]);
        }
        MatchEvent evt = new MatchEvent(component, null, this);
        for (int i = 0; i < ll.length; i++) {
            MatchListener matchListener = ll[i];
            matchListener.matchCleared(evt);
        }
    }
    
    private static void placeHighlights(
        int [] offsets, 
        boolean skipFirst,
        OffsetsBag highlights, 
        AttributeSet coloring,
        int max
    ) {
        int startIdx;
        
        if (skipFirst && offsets.length > 2) {
            startIdx = 2;
        } else {
            startIdx = 0;
        }
        
        // Highlight all the matches
        for(int i = startIdx; i < offsets.length; i += 2) {
            try {
                int from = Math.min(offsets[i], max);
                int to = Math.min(offsets[i+1], max);
                if (from == to) {
                    return;
                }
                highlights.addHighlight(from, to, coloring);
            } catch (Throwable t) {
                // ignore, most likely invalid offsets supplied from a custom BracesMatcher,
                // unfortunately here it's too late to know who supplied them (#167478)
                LOG.log(Level.FINE, null, t);
            }
        }
    }

    private static boolean isMultiChar(int [] offsets, boolean skipFirst) {
        if (offsets != null) {
            int startIdx;

            if (skipFirst && offsets.length > 2) {
                startIdx = 1;
            } else {
                startIdx = 0;
            }

            // Highlight all the matches
            for(int i = startIdx; i < offsets.length / 2; i++) {
                if (offsets[i * 2 + 1] - offsets[i * 2] > 1) {
                    return true;
                }
            }
        }

        return false;
    }

    // when navigating: set the dot after or before the matching area, depending on the caret bias
    // when selecting: see #123091 for details
    private static void navigateAreas(
        int [] origin, 
        int [] matches,
        int caretOffset,
        Object caretBias,
        Caret caret,
        boolean select
    ) {
        if (matches != null && matches.length >= 2) {
            int newDotBackwardIdx = -1;
            int newDotForwardIdx = -1;
            
            for(int i = 0; i < matches.length / 2; i++) {
                if (matches[i * 2] <= origin[0] && 
                    (newDotBackwardIdx == -1 || matches[i * 2] > matches[newDotBackwardIdx * 2])
                ) {
                    newDotBackwardIdx = i;
                }
                
                if (matches[i * 2] >= origin[1] && 
                    (newDotForwardIdx == -1 || matches[i * 2] < matches[newDotForwardIdx * 2])
                ) {
                    newDotForwardIdx = i;
                }
            }
            
            if (newDotBackwardIdx != -1) {
                if (select) {
                    int set, move;
                    
                    if (caretOffset < origin[1]) {
                        set = origin[0];
                        move = matches[2 * newDotBackwardIdx + 1];
                    } else {
                        set = origin[1];
                        move = matches[2 * newDotBackwardIdx];
                    }

                    if (caret.getDot() == caret.getMark()) { // || (move <= caret.getMark() && caret.getMark() <= set)
                        caret.setDot(set);
                    }
                    caret.moveDot(move);
                } else {
                    if (B_BACKWARD.equalsIgnoreCase(caretBias.toString())) {
                        caret.setDot(matches[2 * newDotBackwardIdx + 1]);
                    } else {
                        caret.setDot(matches[2 * newDotBackwardIdx]);
                    }
                }
            } else if (newDotForwardIdx != -1) {
                if (select) {
                    int set, move;

                    if (caretOffset > origin[0]) {
                        set = origin[1];
                        move = matches[2 * newDotForwardIdx];
                    } else {
                        set = origin[0];
                        move = matches[2 * newDotForwardIdx + 1];
                    }
                    
                    if (caret.getDot() == caret.getMark()) { //  || (set <= caret.getMark() && caret.getMark() <= move)
                        caret.setDot(set);
                    }
                    caret.moveDot(move);
                } else {
                    if (B_BACKWARD.equalsIgnoreCase(caretBias.toString())) {
                        caret.setDot(matches[2 * newDotForwardIdx + 1]);
                    } else {
                        caret.setDot(matches[2 * newDotForwardIdx]);
                    }
                }
            }
        }
    }

    private static Collection<? extends BracesMatcherFactory> findFactories(final Document document,
            final int offset, final boolean backward
    ) {
        final MimePath[] mimePath = { null };
        document.render(new Runnable() {
            public void run() {
                TokenHierarchy<? extends Document> th = TokenHierarchy.get(document);
                if (th.isActive()) {
                    List<TokenSequence<?>> sequences = th.embeddedTokenSequences(offset, backward);
                    if (!sequences.isEmpty()) {
                        String path = sequences.get(sequences.size() - 1).languagePath().mimePath();
                        mimePath[0] = MimePath.parse(path);
                    }
                } else {
                    String mimeType = (String) document.getProperty("mimeType"); //NOI18N
                    mimePath[0] = mimeType != null ? MimePath.parse(mimeType) : MimePath.EMPTY;
                }
            }
        });
        Collection<? extends BracesMatcherFactory> factories = mimePath[0] == null ?
            Collections.<BracesMatcherFactory>emptyList() :
            MimeLookup.getLookup(mimePath[0]).lookupAll(BracesMatcherFactory.class);
        
//        System.out.println("@@@ '" + (mimePath == null ? "null" : mimePath.getPath()) + "', offset = " + offset + ", backward = " + backward + " -> {");
//        for(BracesMatcherFactory f : factories) {
//            System.out.println("@@@    " + f);
//        }
//        System.out.println("@@@ } --------------");
        
        return factories;
    }
    
    private void scheduleMatchHighlighted(Result r, int[] origin, int[] matches, BracesMatcher.ContextLocator locator, Document d) throws BadLocationException {
        PR.post(new Firer(r, toPositions(component, origin), toPositions(component, matches), locator), 200);
    }
    
    private final class Firer implements Runnable {
        private Result myResult;
        private Position[] origin;
        private Position[] matches;
        private BracesMatcher.ContextLocator locator;

        public Firer(Result myResult, Position[] origin, Position[] matches, BracesMatcher.ContextLocator locator) {
            this.myResult = myResult;
            this.origin = origin;
            this.matches = matches;
            this.locator = locator;
        }
        
        
        
        public void run() {
            if (lastResult != myResult) {
                return;
            }
            
            fireMatchesHighlighted(origin, matches, locator);
        }
    }
    
    private final class Result implements Runnable {

        private final Document document;
        private final int caretOffset;
        private final Object allowedDirection;
        private final Object caretBias;
        private final int maxBwdLookahead;
        private final int maxFwdLookahead;

//        private boolean inDocumentRender = false;
        private volatile boolean canceled = false;
        
        private final List<Object []> highlightingJobs = new ArrayList<Object []>();
        private final List<Object []> navigationJobs = new ArrayList<Object []>();
        
        public Result(
            Document document, 
            int caretOffset, 
            Object allowedDirection,
            Object caretBias,
            int maxBwdLookahead,
            int maxFwdLookahead
        ) {
            this.document = document;
            this.caretOffset = caretOffset;
            this.allowedDirection = allowedDirection;
            this.caretBias = caretBias;
            this.maxBwdLookahead = maxBwdLookahead;
            this.maxFwdLookahead = maxFwdLookahead;
        }
        
        // Must be called under the MasterMatcher.LOCK
        public void addHighlightingJob(
            OffsetsBag highlights,
            AttributeSet matchedColoring,
            AttributeSet mismatchedColoring,
            AttributeSet matchedMulticharColoring,
            AttributeSet mismatchedMulticharColoring
        ) {
            highlightingJobs.add(new Object[] {
                highlights,
                matchedColoring,
                mismatchedColoring,
                matchedMulticharColoring,
                mismatchedMulticharColoring
            });
        }

        // Must be called under the MasterMatcher.LOCK
        public void addNavigationJob(Caret caret, boolean select) {
            navigationJobs.add(new Object [] { caret, select });
        }
        
        public int getCaretOffset() {
            return caretOffset;
        }
        
        public Object getAllowedDirection() {
            return allowedDirection;
        }
        
        public Object getCaretBias() {
            return caretBias;
        }
        
        public int getMaxBwdLookahead() {
            return maxBwdLookahead;
        }
        
        public int getMaxFwdLookahead() {
            return maxFwdLookahead;
        }
        
        public boolean isCanceled() {
            return canceled;
        }
        
        public void cancel() {
            this.canceled = true;
        }
        
        // ------------------------------------------------
        // Runnable implementation
        // ------------------------------------------------
        
        public void run() {
            THREAD_RESULTS.put(Thread.currentThread(), this);
            try {
                _run();
            } finally {
                THREAD_RESULTS.remove(Thread.currentThread());
            }
        }
        
        private void _run() {
//            // Read lock the document
//            if (!inDocumentRender) {
//                inDocumentRender = true;
//                THREAD_RESULTS.put(Thread.currentThread(), this);
//                try {
//                    document.render(this);
//                } catch (ThreadDeath t) {
//                    throw t;
//                } catch (Error t) {
//                    // ignore, can happen when the task is interrupted
//                } finally {
//                    THREAD_RESULTS.remove(Thread.currentThread());
//                }
//                return;
//            }

            if (canceled) {
                return;
            }
            
            if (caretOffset > document.getLength()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Invalid offset, braces matching request ignored. " + //NOI18N
                        "Offset = " + caretOffset + //NOI18N
                        ", doc.getLength() = " + document.getLength()); //NOI18N
                }
                return;
            }
            
            int [] origin = null;
            int [] matches = null;
            BracesMatcher.ContextLocator locator = null;
            try {
                // Find the original area
                BracesMatcher [] matcher = new BracesMatcher[1];

//                System.out.println("!!! ------------------- finding Origin ---------------------");
                if (D_BACKWARD.equalsIgnoreCase(allowedDirection.toString())) {
                    origin = findOrigin(true, matcher);
                    if (origin == null && !canceled) {
                        origin = findOrigin(false, matcher);
                    }
                } else if (D_FORWARD.equalsIgnoreCase(allowedDirection.toString())) {
                    origin = findOrigin(false, matcher);
                    if (origin == null && !canceled) {
                        origin = findOrigin(true, matcher);
                    }
                }
//                System.out.println("!!! --------------------------------------------------------");
                
                if (origin != null && !canceled) {
                    // Find matching areas
                    matches = matcher[0].findMatches();
                    if (matches != null) {
                        // #231842 - ignore invalid results, log
                        if (matches.length == 0) {
                            matches = null;
                        } else if (matches.length % 2 != 0) {
                            if (LOG.isLoggable(Level.WARNING)) {
                                LOG.log(Level.WARNING, "Invalid match found by matcher {0}: {1}",
                                        new Object[] {
                                            matcher[0],
                                            Arrays.asList(matches)
                                });
                            }
                            matches = null;
                        } else if (matcher[0] instanceof BracesMatcher.ContextLocator) {
                            locator = ((BracesMatcher.ContextLocator)matcher[0]);
                        }
                    }
                }
            } catch (ThreadDeath td) {
                throw td;

            } catch (Throwable e) {
                // catch everything including assertions and other Errors (#159491)
                for(Throwable t = e; t != null; t = t.getCause()) {
                    if (t instanceof InterruptedException) {
                        // We were interrupted, no results
                        return;
                    }
                }

                // since we are not running under document lock (see #131284) there can be exceptions
                LOG.log(Level.FINE, null, e);
                return;
            }

            // Show the results
            synchronized (LOCK) {
                // If the task was cancelled, we must exit immediately
                if (canceled) {
                    return;
                }

                // Signal that the task is done. No more jobs will be added to it.
                MasterMatcher.this.task = null;
            }

            final int [] _origin = origin;
            final int [] _matches = matches;
            final BracesMatcher.ContextLocator _locator = locator;
            
            document.render(new Runnable() {
                public void run() {
                    try {
                        for (Object[] job : highlightingJobs) {
                            AttributeSet matchedColoring;
                            AttributeSet mismatchedColoring;

                            if (isMultiChar(_origin, true) || isMultiChar(_matches, false)) {
                                matchedColoring = (AttributeSet) job[3];
                                mismatchedColoring = (AttributeSet) job[4];
                            } else {
                                matchedColoring = (AttributeSet) job[1];
                                mismatchedColoring = (AttributeSet) job[2];
                            }

                            highlightAreas(_origin, _matches, (OffsetsBag) job[0], matchedColoring, mismatchedColoring, document.getLength());
                            if (Boolean.valueOf((String) component.getClientProperty(PROP_SHOW_SEARCH_PARAMETERS))) {
                                showSearchParameters((OffsetsBag) job[0]);
                            }
                        }
                        scheduleMatchHighlighted(Result.this, _origin, _matches, _locator, document);

                        for(Object [] job : navigationJobs) {
                            navigateAreas(_origin, _matches, caretOffset, caretBias, (Caret) job[0], (Boolean) job[1]);
                        }
                    } catch (Exception e) {
                        // the results were not computed under document lock and may be out of sync
                        // with the document, just ignore the exception and remove any highlights
                        LOG.log(Level.FINE, null, e);

                        // clear everything, we probably screwed it up
                        for (Object[] job : highlightingJobs) {
                            ((OffsetsBag) job[0]).clear();
                        }
                    }
                }
            });
        }
        
        private int [] findOrigin(
            boolean backward, 
            BracesMatcher [] matcher
        ) throws InterruptedException {
            Element paragraph = DocumentUtilities.getParagraphElement(document, caretOffset);
            
            int adjustedCaretOffset = caretOffset;
            int lookahead = 0;
            if (backward) {
                int maxLookahead = maxBwdLookahead;
                if (B_FORWARD.equalsIgnoreCase(caretBias.toString())) {
                    if (adjustedCaretOffset < paragraph.getEndOffset() - 1) {
                        adjustedCaretOffset++;
                        maxLookahead++;
                    }
                } else {
                    if (maxLookahead == 0) {
                        maxLookahead = 1;
                    }
                }

                lookahead = adjustedCaretOffset - paragraph.getStartOffset();
                if (lookahead > maxLookahead) {
                    lookahead = maxLookahead;
                }
            } else {
                int maxLookahead = maxFwdLookahead;
                if (B_BACKWARD.equalsIgnoreCase(caretBias.toString())) {
                    if (adjustedCaretOffset > paragraph.getStartOffset()) {
                        adjustedCaretOffset--;
                        maxLookahead++;
                    }
                } else {
                    if (maxLookahead == 0) {
                        maxLookahead = 1;
                    }
                }
                
                lookahead = paragraph.getEndOffset() - 1 - adjustedCaretOffset;
                if (lookahead > maxLookahead) {
                    lookahead = maxLookahead;
                }
            }
            
            Collection<? extends BracesMatcherFactory> factories = Collections.<BracesMatcherFactory>emptyList();
            
            if (lookahead > 0) {
                factories = findFactories(document, adjustedCaretOffset, backward);
            }
            final MatcherContext context;
            if (!factories.isEmpty()) {
                context = SpiAccessor.get().createCaretContext(
                    document, 
                    adjustedCaretOffset, 
                    backward, 
                    lookahead
                );
            } else {
                context = null;
            }

            Iterator<? extends BracesMatcherFactory> matcherIt = factories.iterator();
            boolean tryAgain;
            do {
                // Find the first provider that accepts the context
                while (matcherIt.hasNext()) {
                    matcher[0] = matcherIt.next().createMatcher(context);
                    if (matcher[0] != null) {
                        break;
                    }
                }
                
                if (matcher[0] != null) {
                    tryAgain = false;
                    // Find the original area
                    int [] origin = null;
                    try {
                        origin = matcher[0].findOrigin();
                    } catch (BadLocationException ble) {
                        // since we are not running under document lock (see #131284) there can be exceptions
                        LOG.log(Level.FINE, null, ble);
                    }

                    // Check the original area for consistency
                    if (origin != null) {
                        if (origin.length == 0) {
                            origin = null;
                            // special hack for CSL. CSL subverts the backward-compatible LegacyEssMatcher bridge
                            // in CslEditorKit - it implements bracematcher bridge through ExtSyntaxSupport.
                            // In case the language does not even contain bracket match handlers, CSL will return empty array
                            // to indicate the Legacy bridge should be ignored
                            tryAgain = matcher[0] instanceof LegacyEssMatcher;
                            continue;
                        } else if (origin.length % 2 != 0) {
                            if (LOG.isLoggable(Level.WARNING)) {
                                LOG.warning("Invalid BracesMatcher implementation, " + //NOI18N
                                    "findOrigin() should return nothing or offset pairs. " + //NOI18N
                                    "Offending BracesMatcher: " + matcher[0]); //NOI18N
                            }
                            origin = null;
                        } else {
                            for(int i = 0; i < origin.length / 2; i++) {
                                if (origin[2 * i] < 0 || origin[2 * i + 1] > document.getLength() || origin[2 * i] > origin[2 * i + 1]) {
                                    if (LOG.isLoggable(Level.WARNING)) {
                                        LOG.warning("Invalid origin offsets [" + origin[2 * i] + ", " + origin[2 * i + 1] + "]. " + //NOI18N
                                            "Offending BracesMatcher: " + matcher[0]); //NOI18N
                                    }
                                    origin = null;
                                    break;
                                }
                            }
                        }

                        if (origin != null) {
                            if (backward) {
                                if (origin[1] < caretOffset - lookahead || origin[0] > caretOffset) {
                                    if (LOG.isLoggable(Level.WARNING)) {
                                        LOG.warning("Origin offsets out of range, " + //NOI18N
                                            "origin = [" + origin[0] + ", " + origin[1] + "], " + //NOI18N
                                            "caretOffset = " + caretOffset + //NOI18N
                                            ", lookahead = " + lookahead + //NOI18N
                                            ", searching backwards. " + //NOI18N
                                            "Offending BracesMatcher: " + matcher[0]); //NOI18N
                                    }
                                    origin = null;
                                }
                            } else {
                                if ((origin[1] < caretOffset || origin[0] > caretOffset + lookahead)) {
                                    if (LOG.isLoggable(Level.WARNING)) {
                                        LOG.warning("Origin offsets out of range, " + //NOI18N
                                            "origin = [" + origin[0] + ", " + origin[1] + "], " + //NOI18N
                                            "caretOffset = " + caretOffset + //NOI18N
                                            ", lookahead = " + lookahead + //NOI18N
                                            ", searching forward. " + //NOI18N
                                            "Offending BracesMatcher: " + matcher[0]); //NOI18N
                                    }
                                    origin = null;
                                }
                            }
                        }
                    }

                    if (LOG.isLoggable(Level.FINE)) {
                        if (origin != null) {
                            LOG.fine("[" + origin[0] + ", " + origin[1] + "] for caret = " + caretOffset + ", lookahead = " + (backward ? "-" : "") + lookahead); //NOI18N
                        } else {
                            LOG.fine("[null] for caret = " + caretOffset + ", lookahead = " + (backward ? "-" : "") + lookahead); //NOI18N
                        }
                    }

                    return origin;
                } else {
                    return null;
                }
            } while (tryAgain && matcherIt.hasNext());
            return null;
        }
        
        private void showSearchParameters(OffsetsBag bag) {
            Element paragraph = DocumentUtilities.getParagraphElement(document, caretOffset);
            
            // Show caret bias
            if (B_BACKWARD.equalsIgnoreCase(caretBias.toString())) {
                if (caretOffset > paragraph.getStartOffset()) {
                    bag.addHighlight(caretOffset - 1, caretOffset, CARET_BIAS_HIGHLIGHT);
                }
            } else {
                if (caretOffset < paragraph.getEndOffset() - 1) {
                    bag.addHighlight(caretOffset, caretOffset + 1, CARET_BIAS_HIGHLIGHT);
                }
            }
            
            // Show lookahead
            int bwdLookahead = Math.min(maxBwdLookahead, caretOffset - paragraph.getStartOffset());
            int fwdLookahead = Math.min(maxFwdLookahead, paragraph.getEndOffset() - 1 - caretOffset);
            bag.addHighlight(caretOffset - bwdLookahead, caretOffset, MAX_LOOKAHEAD_HIGHLIGHT);
            bag.addHighlight(caretOffset, caretOffset + fwdLookahead, MAX_LOOKAHEAD_HIGHLIGHT);
        }
    } // End of Result class
    
}
