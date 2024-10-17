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

package org.netbeans.modules.editor.fold.ui;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldStateChange;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.view.EditorView;
import org.netbeans.modules.editor.lib2.view.EditorViewFactory;
import org.netbeans.modules.editor.lib2.view.EditorViewFactoryChange;
import org.netbeans.modules.editor.lib2.view.ViewUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * View factory creating views for collapsed folds.
 *
 * @author Miloslav Metelka
 */

@SuppressWarnings("ClassWithMultipleLoggers")
public final class FoldViewFactory extends EditorViewFactory implements FoldHierarchyListener, LookupListener, PreferenceChangeListener {

    /**
     * Component's client property which can be set to see all folds expanded regardless 
     * of their real state - used for tooltip fold preview pane.
     */
    static final String DISPLAY_ALL_FOLDS_EXPANDED_PROPERTY = "display-all-folds-expanded"; // NOI18N

    // -J-Dorg.netbeans.editor.view.change.level=FINE
    static final Logger CHANGE_LOG = Logger.getLogger("org.netbeans.editor.view.change"); // NOI18N

    // -J-Dorg.netbeans.modules.editor.fold.FoldViewFactory.level=FINE
    private static final Logger LOG = Logger.getLogger(FoldViewFactory.class.getName());

    public static void register() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Fold view factory registered");
        }
        EditorViewFactory.registerFactory(new FoldFactory());
    }

    private FoldHierarchy foldHierarchy;

    private boolean foldHierarchyLocked;

    private Fold fold;

    private int foldStartOffset;

    private Iterator<Fold> collapsedFoldIterator;

    private boolean displayAllFoldsExpanded;
    
    /**
     * Quick optimization to avoid traversing through the fold hierarchy
     * if no collapsed folds were encountered yet for the component.
     * It scans the fold hierarchy initially and then only the changed folds
     * upon fold changes notifications. Once the flag becomes true the regular
     * collapsed fold iterator is used.
     */
    private boolean collapsedFoldEncountered;
    
    /**
     * Composite Color settings from MIME lookup
     */
    private FontColorSettings   colorSettings;

    /**
     * Lookup results for color settings, being listened for changes.
     */
    private Lookup.Result<?>    colorSource;
    
    private Preferences         prefs;
    
    private int viewFlags = 0;
    
    private final FoldHierarchyListener weakL;

    public FoldViewFactory(View documentView) {
        super(documentView);
        foldHierarchy = FoldHierarchy.get(textComponent());
        // the view factory may get eventually GCed, but the FoldHierarchy can survive, snce it is tied to the component.
        weakL = WeakListeners.create(FoldHierarchyListener.class, this, foldHierarchy);
        foldHierarchy.addFoldHierarchyListener(weakL);
        // Go through folds and search for collapsed fold.
        foldHierarchy.lock();
        try {
            @SuppressWarnings("unchecked")
            Iterator<Fold> it = FoldUtilities.collapsedFoldIterator(foldHierarchy, 0, Integer.MAX_VALUE);
            collapsedFoldEncountered = it.hasNext();
        } finally {
            foldHierarchy.unlock();
        }

        displayAllFoldsExpanded = Boolean.TRUE.equals(textComponent().getClientProperty(DISPLAY_ALL_FOLDS_EXPANDED_PROPERTY));
        
        String mime = DocumentUtilities.getMimeType(document());
        
        Lookup lkp = MimeLookup.getLookup(mime);
        colorSource = lkp.lookupResult(FontColorSettings.class);
        colorSource.addLookupListener(WeakListeners.create(LookupListener.class, this, colorSource));
        colorSettings = (FontColorSettings)colorSource.allInstances().iterator().next();
        prefs = lkp.lookup(Preferences.class);
        prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, prefs));
        
        initViewFlags();
    }
    
    private void initViewFlags() {
        viewFlags =
                (prefs.getBoolean(FoldUtilitiesImpl.PREF_CONTENT_PREVIEW, true) ? 1 : 0) |
                (prefs.getBoolean(FoldUtilitiesImpl.PREF_CONTENT_SUMMARY, true) ? 2 : 0);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        refreshColors();
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        String k = evt.getKey();
        if (FoldUtilitiesImpl.PREF_CONTENT_PREVIEW.equals(k) ||
            FoldUtilitiesImpl.PREF_CONTENT_SUMMARY.equals(k)) {
            initViewFlags();
            final Document d = document();
            if (d != null) {
                d.render(new Runnable() {
                    @Override
                    public void run() {
                        int end = d.getLength();
                        fireEvent(EditorViewFactoryChange.createList(0, end, EditorViewFactoryChange.Type.CHARACTER_CHANGE));
                    }
                });
            }
        }
    }

    private void refreshColors() {
        colorSettings = (FontColorSettings)colorSource.allInstances().iterator().next();
        final Document d = document();
        if (d != null) {
            d.render(new Runnable() {
                @Override
                public void run() {
                    int end = d.getLength();
                    fireEvent(EditorViewFactoryChange.createList(0, end, EditorViewFactoryChange.Type.CHARACTER_CHANGE));
                }
            });
        }
    }

    @Override
    public void restart(int startOffset, int endOffset, boolean createViews) {
        if (collapsedFoldEncountered) {
            foldHierarchy.lock(); // this.finish() always called in try-finally
            foldHierarchyLocked = true;
            @SuppressWarnings("unchecked")
            Iterator<Fold> it = FoldUtilities.collapsedFoldIterator(foldHierarchy, startOffset, Integer.MAX_VALUE);
            collapsedFoldIterator = it;
            foldStartOffset = -1; // Make a next call to updateFoldInfo() to fetch a fold
        }
    }

    private void updateFoldInfo(int offset) {
        if (foldStartOffset < offset) { // offset already inside or above current fold
            while (collapsedFoldIterator.hasNext()) { // fetch next fold that is above (or right at) offset
                fold = collapsedFoldIterator.next();
                foldStartOffset = fold.getStartOffset();
                // avoid issue #229852; the length might be 0, if text was deleted and the fold collapsed to nothing &&
                // the fold hierarchy was not refreshed yet (the hierarchy locks itself in post update)
                int l = fold.getEndOffset() - foldStartOffset;
                if (foldStartOffset >= offset && l > 0) {
                    return;
                }
            }
            fold = null;
            foldStartOffset = Integer.MAX_VALUE;
        }
    }

    @Override
    public int nextViewStartOffset(int offset) {
        if (!displayAllFoldsExpanded && collapsedFoldEncountered) {
            updateFoldInfo(offset);
            return foldStartOffset;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public EditorView createView(int startOffset, int limitOffset, boolean forcedLimit,
    EditorView origView, int nextOrigViewOffset) {
        assert (startOffset == foldStartOffset) : "startOffset=" + startOffset + " != foldStartOffset=" + foldStartOffset; // NOI18N
        if (fold.getEndOffset() <= limitOffset || !forcedLimit) {
            return new FoldView(textComponent(), fold, colorSettings, viewFlags);
        } else {
            return null;
        }
    }
    
    @Override
    public int viewEndOffset(int startOffset, int limitOffset, boolean forcedLimit) {
        int foldEndOffset = fold.getEndOffset();
        if (foldEndOffset <= limitOffset) {
            return foldEndOffset;
        } else {
            return -1;
        }
    }

    @Override
    public void continueCreation(int startOffset, int endOffset) {
    }

    @Override
    public void finishCreation() {
        fold = null;
        collapsedFoldIterator = null;
        if (foldHierarchyLocked) {
            foldHierarchy.unlock();
        }
    }

    @Override
    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
        boolean collapsedAdded = false;
        boolean changedToCollapsed = false;
        // Check if any collapsed fold was added or a collapsed/expanded state changed
        for (int i = evt.getAddedFoldCount() - 1; i >= 0; i--) {
            if (evt.getAddedFold(i).isCollapsed()) {
                collapsedAdded = true;
                break;
            }
        }
        if (!collapsedAdded) {
            for (int i = evt.getFoldStateChangeCount() - 1; i >= 0; i--) {
                FoldStateChange foldStateChange = evt.getFoldStateChange(i);
                if (foldStateChange.isCollapsedChanged() && foldStateChange.getFold().isCollapsed()) {
                    changedToCollapsed = true;
                    break;
                }
            }
        }
        collapsedFoldEncountered |= collapsedAdded || changedToCollapsed;
        JTextComponent comp = textComponent();
        if (collapsedFoldEncountered && comp != null) {
            // [TODO] there could be more detailed inspection done among folds
            // of what really changed and what are in fact the same folds as before the change
            // possibly performed even on the Fold API level.
            int startOffset = evt.getAffectedStartOffset();
            int endOffset = evt.getAffectedEndOffset();
            if (CHANGE_LOG.isLoggable(Level.FINE)) {
                ViewUtils.log(CHANGE_LOG, "CHANGE in FoldViewFactory: <" + // NOI18N
                        startOffset + "," + endOffset + ">\n"); // NOI18N
            }
            if (collapsedAdded) {
                // this hint covers a specific case when a fold is created as *initially* collapsed
                // to maintain caret position on screen, if the caret was visible. 
                comp.putClientProperty("editorcaret.updateRetainsVisibleOnce", Boolean.TRUE);
            }
            fireEvent(EditorViewFactoryChange.createList(startOffset, endOffset,
                    EditorViewFactoryChange.Type.PARAGRAPH_CHANGE));
        }
    }

    public static final class FoldFactory implements EditorViewFactory.Factory {

        @Override
        public EditorViewFactory createEditorViewFactory(View documentView) {
            return new FoldViewFactory(documentView);
        }

        @Override
        public int weight() {
            return 100;
        }

    }

}
