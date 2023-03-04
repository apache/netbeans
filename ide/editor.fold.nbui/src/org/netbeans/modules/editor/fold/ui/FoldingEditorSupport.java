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
package org.netbeans.modules.editor.fold.ui;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldStateChange;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseCaret;
import org.netbeans.modules.editor.lib2.caret.CaretFoldExpander;
import org.netbeans.spi.editor.fold.FoldHierarchyMonitor;
import org.openide.util.Exceptions;

/**
 * Provides adjustments to functions of editor component
 * based on folding operations.
 * This code was originally part of editor.lib, in BaseCaret class.
 * 
 * @author sdedic
 */
public class FoldingEditorSupport implements FoldHierarchyListener {
    private static final Logger LOG = Logger.getLogger(FoldingEditorSupport.class.getName());
    
    static {
        CaretFoldExpander.register(new CaretFoldExpanderImpl());
    }
    
    /**
     * Component where the folding takes place
     */
    private final JTextComponent component;
    
    /**
     * Fold hierarchy
     */
    private final FoldHierarchy foldHierarchy;
    
    FoldingEditorSupport(FoldHierarchy h, JTextComponent component) {
        this.component = component;
        this.foldHierarchy = h;
        component.putClientProperty("org.netbeans.api.fold.expander", new C());
        foldHierarchy.addFoldHierarchyListener(this);
    }
    
    private class C implements Runnable, Callable<Boolean> {
        private boolean res;
        private boolean sharp;
        
        public void run() {
            foldHierarchy.lock();
            try {
                int offset = component.getCaret().getDot();
                res = false;
                Fold f = FoldUtilities.findCollapsedFold(foldHierarchy, offset, offset);
                if (f != null) {
                    if (sharp) {
                        res = f.getStartOffset() < offset && f.getEndOffset() > offset;
                    } else {
                        res = f.getStartOffset() <= offset && f.getEndOffset() >= offset;
                    }
                    if (res) {
                        foldHierarchy.expand(f);
                    }
                }
            } finally {
                foldHierarchy.unlock();
            }
        }
        
        public boolean equals(Object whatever) {
            if (!(whatever instanceof Caret)) {
                return super.equals(whatever);
            }
            sharp = false;
            final Document doc = component.getDocument();
            doc.render(this);
            return res;
        }
        
        public Boolean call() {
            sharp = true;
            final Document doc = component.getDocument();
            doc.render(this);
            return res;
        }
    }
    
    public @Override void foldHierarchyChanged(final FoldHierarchyEvent evt) {
        final Caret c = component.getCaret();
//        if (!(c instanceof BaseCaret)) {
//            return;
//        }
//        final BaseCaret bc = (BaseCaret)c;
        if (c == null) {
            return;
        }
        int caretOffset = c.getDot();
        final int addedFoldCnt = evt.getAddedFoldCount();
        boolean scrollToView = false;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Received fold hierarchy change {1}, added folds: {0}", 
                    new Object[] { addedFoldCnt, evt.hashCode() }); // NOI18N
        }
        boolean expand = false;
        boolean includeEnd = false;
        int newPosition = -1;
        
        FoldHierarchy hierarchy = (FoldHierarchy) evt.getSource();
        if (addedFoldCnt > 0) {
            expand = true;
        } else {
            int startOffset = Integer.MAX_VALUE;
            // Set the caret's offset to the end of just collapsed fold if necessary
            if (evt.getAffectedStartOffset() <= caretOffset && evt.getAffectedEndOffset() >= caretOffset) {
                for (int i = 0; i < evt.getFoldStateChangeCount(); i++) {
                    FoldStateChange change = evt.getFoldStateChange(i);
                    if (change.isCollapsedChanged()) {
                        Fold fold = change.getFold();
                        if (fold.isCollapsed() && fold.getStartOffset() <= caretOffset && fold.getEndOffset() >= caretOffset) {
                            if (fold.getStartOffset() < startOffset) {
                                startOffset = fold.getStartOffset();
                                LOG.log(Level.FINER, "Moving caret from just collapsed fold {0} to offset {1}; evt=" + evt.hashCode(),
                                        new Object[] { fold, startOffset });
                            }
                        }
                    } else if (change.isStartOffsetChanged()) {
                        // schedule expand iff the caret is in the NEWLY included prefix of the fold
                        Fold fold = change.getFold();
                        int ostart = change.getOriginalStartOffset();
                        int nstart = fold.getStartOffset();
                        int nend = fold.getEndOffset();
                        int to = Math.max(ostart, nstart);
                        int from = Math.min(ostart, nstart);

                        boolean e = caretOffset >= from && caretOffset <= to && caretOffset >= nstart && caretOffset < nend;
                        if (e && LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINER, "Fold start extended over caret: {0}; evt= " + evt.hashCode(), fold);
                        }
                        expand |= e;
                    } else if (change.isEndOffsetChanged()) {
                        // ... the same check for suffix.
                        Fold fold = change.getFold();
                        int oend = change.getOriginalEndOffset();
                        int nend = fold.getEndOffset();
                        int nstart = fold.getStartOffset();
                        
                        int to = Math.max(oend, nend);
                        int from = Math.min(oend, nend);
                        
                        boolean e = caretOffset >= from && caretOffset <= to && caretOffset >= nstart && caretOffset <= nend;
                        expand |= e;
                        // the search for collapsed fold uses < not <= to compare fold end. Adjust caretOffset so the fold is found.
                        includeEnd = caretOffset == nend && (nend - nstart) > 1;

                        if (e && LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINER, "Fold end extended over caret: {0}, includeEnd = {1}; evt= " + evt.hashCode(), 
                                    new Object[] { fold, includeEnd });
                        }
                    }
                }
                if (startOffset != Integer.MAX_VALUE) {
                    newPosition = startOffset;
                    c.setDot(startOffset);
                    expand = false;
                }
            }
        }
        
        boolean wasExpanded = false;
        if (expand) {
            Fold collapsed = null;
            if (includeEnd) {
                // compensate so findCollapsedFold finds something. Also for newly created folds so text does not collapse immediately
                // after a caret.
                caretOffset--;
            }
            final int dot = caretOffset;
            while ((collapsed = FoldUtilities.findCollapsedFold(hierarchy, caretOffset, caretOffset)) != null) {
                boolean shouldExpand = false;
                
                EX: if (collapsed.getStartOffset() < caretOffset) {
                        if (collapsed.getEndOffset() > caretOffset) {
                            shouldExpand = true;
                        } else if (addedFoldCnt > 0) {
                            // shortcut: caret immediately following the collapsed fold
                            if (collapsed.getEndOffset() == caretOffset) {
                                shouldExpand = true;
                            }
                        }
                }
                if (shouldExpand) {
                    LOG.log(Level.FINER, "Expanding fold {0}; evt= " + evt.hashCode(), collapsed);
                    wasExpanded = true;
                    hierarchy.expand(collapsed);
                } else {
                    break;
                }
            }
            // prevent unneeded scrolling; the user may have scrolled out using mouse already
            // so scroll only if the added fold may affect Y axis. Actually it's unclear why
            // we should reveal the current position on fold events except when caret is positioned in now-collapsed fold
        }
        if (!wasExpanded) {
            // go through folds just created folds, if some of them is _immediately_ preceding the caret && there's just whitespace in between the caret
            // and fold end - expand it.
            Fold preceding = caretOffset > 0 ? FoldUtilities.findNearestFold(hierarchy, -caretOffset) : null;
            if (preceding != null) {
                int precEnd = preceding.getEndOffset();
                for (int i = 0; i < addedFoldCnt; i++) {
                    Fold f = evt.getAddedFold(i);
                    if (f.getStartOffset() > precEnd) {
                        // fail fast
                        break;
                    }
                    if (f == preceding && onlyWhitespacesBetween(f.getEndOffset(), caretOffset)) {
                        LOG.log(Level.FINER, "Expanding fold {0}; evt= " + evt.hashCode(), f);
                        wasExpanded = true;
                        hierarchy.expand(f);
                        break;
                    }
                }
                if (!wasExpanded) {
                    // go through changes and detect if the nearest preceding fold was expanded
                    for (int i = 0; i < evt.getFoldStateChangeCount(); i++) {
                        FoldStateChange change = evt.getFoldStateChange(i);
                        Fold f = change.getFold();
                        int so = f.getStartOffset();
                        if (so > precEnd) {
                            break;
                        }
                        if (change.isEndOffsetChanged() && 
                            f == preceding && f.isCollapsed() &&
                            onlyWhitespacesBetween(f.getEndOffset(), caretOffset) &&
                            // non empty content added to the fold:
                            !onlyWhitespacesBetween(change.getOriginalEndOffset(), caretOffset)) {
                            LOG.log(Level.FINER, "Expanding fold {0}; evt= " + evt.hashCode(), f);
                            wasExpanded = true;
                            hierarchy.expand(f);
                            break;
                        }
                    }
                }
            }
        }
        
        scrollToView = wasExpanded;
        
        final int newPositionF = newPosition;
        
        // Update caret's visual position
        // Post the caret update asynchronously since the fold hierarchy is updated before
        // the view hierarchy and the views so the dispatchUpdate() could be picking obsolete
        // view information.
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "Added folds: {0}, should scroll: {1}, new pos: {2}; evt= " + evt.hashCode(), 
                    new Object[] { addedFoldCnt, scrollToView, newPosition });
        }
        if (addedFoldCnt > 1 || scrollToView || newPosition >= 0) {
            final boolean scroll = scrollToView;
            SwingUtilities.invokeLater(new Runnable() {
                public @Override void run() {
                    LOG.fine("Updating after fold hierarchy change; evt= " + evt.hashCode()); // NOI18N
                    // see defect #227531; the caret may be uninstalled before the EDT executes this runnable.
                    if (component == null || component.getCaret() != c) {
                        return;
                    }
                    if (newPositionF >= 0) {
                        c.setDot(newPositionF);
                    } else {
                        /*
                        bc.refresh(addedFoldCnt > 1 && !scroll);
                        */
                    }
                }
            });
        }
    }
    
    private boolean onlyWhitespacesBetween(final int endOffset, final int dot) {
        // autoexpand a fold that was JUST CREATED, if there's no non-whitespace (not lexical, but actual) in between the
        // fold end and the caret:
        final String[] cnt = new String[1];
        final Document doc = component.getDocument();
        doc.render(new Runnable() {
            public void run() {
                int dl = doc.getLength();
                int from = Math.min(dl, 
                        Math.min(endOffset, dot)
                        );
                int to = Math.min(dl, 
                        Math.max(endOffset, dot
                        ));
                try {
                    cnt[0] = doc.getText(from, to - from);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        return (cnt[0] == null || cnt[0].trim().isEmpty());
    }

    @MimeRegistration(mimeType = "", service = FoldHierarchyMonitor.class)
    public static class F implements FoldHierarchyMonitor {
        @Override
        public void foldsAttached(FoldHierarchy h) {
            FoldingEditorSupport supp = new FoldingEditorSupport(h, h.getComponent());
            // stick as client property to prevent GC:
            h.getComponent().putClientProperty(F.class, supp);
        }
        
        static {
            FoldViewFactory.register();
        }
    }
}
