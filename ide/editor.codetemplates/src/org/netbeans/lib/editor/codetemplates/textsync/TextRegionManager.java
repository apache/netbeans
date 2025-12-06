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

package org.netbeans.lib.editor.codetemplates.textsync;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TextAction;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.lib.editor.util.swing.BlockCompare;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;

/**
 * Maintain the same text in the selected regions of the text document.
 *
 * @author Miloslav Metelka
 */
public final class TextRegionManager {
    
    // -J-Dorg.netbeans.lib.editor.codetemplates.textsync.TextRegionManager.level=FINE
    static final Logger LOG = Logger.getLogger(TextRegionManager.class.getName());

    private static final int INVALID_TEXT_SYNC = -1; // The given text sync group will be deactivated
    private static final int SAME_TEXT_SYNC = -2; // The same text sync as was previously active will be activated

    public static synchronized TextRegionManager reserve(JTextComponent component) {
        if (component == null)
            throw new IllegalArgumentException("component cannot be null"); // NOI18N
        Document doc = component.getDocument();
        TextRegionManager manager = get(doc, true);
        if (manager == null) {
            manager = get(doc, true);
        }
        JTextComponent activeComponent = manager.component();
        // Check if the mgr does not already manage other component
        if (activeComponent == null) {
            manager.setComponent(component);
        } else if (activeComponent != component) { // Active component
            if (manager.isActive()) { // edit groups exist
                manager = null;
            } else { // no edit groups -> reassign
                manager.setComponent(component);
            }
        }
        return manager;
    }

    public static TextRegionManager get(Document doc, boolean forceCreation) {
        TextRegionManager manager = (TextRegionManager)doc.getProperty(TextRegionManager.class);
        if (manager == null && forceCreation) {
            manager = new TextRegionManager(doc);
            doc.putProperty(TextRegionManager.class, manager);
        }
        return manager;
    }

    private WeakReference<JTextComponent> componentRef;
    
    private Document doc;

    private TextRegion<?> rootRegion;
    
    private EventListenerList listenerList = new EventListenerList();

    private GapList<TextSyncGroup<?>> editGroups;
    
    /**
     * Text sync for which the document modifications are being replicated
     * across the respective regions.
     */
    private TextSync activeTextSync;
    
    /**
     * Bounds of the activeTextSync prior last modification (during insert/removeUpdate()).
     */
    private int masterRegionStartOffset;
    private int masterRegionEndOffset;
    
    private int ignoreDocModifications;

    private boolean forceSyncByMaster;
    
    private final Highlighting highlighting = new Highlighting(this);

    private boolean overridingKeys;

    private ActionMap origActionMap;
    private ActionMap overrideActionMap;

    TextRegionManager(Document doc) {
        this.doc = doc;
        this.rootRegion = new TextRegion<Void>();
        this.editGroups = new GapList<TextSyncGroup<?>>(2);
    }

    /**
     * Add a new group to the bottom of the edit groups stack and start its editing.
     * <br/>
     * This method should only be called from AWT thread.
     *
     * @param textSyncGroup
     * @param textSyncIndex
     */
    public void addGroup(TextSyncGroup<?> group, int offsetShift) throws BadLocationException {
        if (group == null)
            throw new IllegalArgumentException("textSyncGroup cannot be null"); // NOI18N
        if (group.textRegionManager() != null)
            throw new IllegalArgumentException("textSyncGroup=" + group + // NOI18N
                    " already assigned to textRegionManager=" + group.textRegionManager()); // NOI18N
        activate();
        editGroups.add(group);
        addGroupUpdate(group, offsetShift);
    }

    public void activateGroup(TextSyncGroup<?> group) {
        activateTextSync(null, group, findEditableTextSyncIndex(group, 0, +1, true, false), true);
    }

    public void stopGroupEditing(TextSyncGroup group) {
        int groupIndex = editGroups.indexOf(group);
        if (groupIndex >= 0) {
            releaseLastGroups(editGroups.size() - groupIndex);
        }
    }

    public void stopSyncEditing() {
        releaseLastGroups(editGroups.size()); // release all groups
    }


    public TextSync activeTextSync() {
        return activeTextSync;
    }

    TextSyncGroup<?> activeGroup() {
        return (activeTextSync != null) ? activeTextSync.group() : null;
    }

    TextSyncGroup<?> lastGroup() {
        return (editGroups.size() > 0) ? editGroups.get(editGroups.size() - 1) : null;
    }

    public void addTextRegionManagerListener(TextRegionManagerListener l) {
        listenerList.add(TextRegionManagerListener.class, l);
    }

    public void removeTextRegionManagerListener(TextRegionManagerListener l) {
        listenerList.remove(TextRegionManagerListener.class, l);
    }

    private TextRegionManagerEvent createEvent(boolean focusChange,
            List<TextSyncGroup<?>> removedGroups, TextSync previousTextSync
    ) {
        return new TextRegionManagerEvent(this, focusChange, removedGroups, previousTextSync);
    }

    private void fireEvent(TextRegionManagerEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 1; i < listeners.length; i += 2) {
            ((TextRegionManagerListener)listeners[i]).stateChanged(evt);
        }
    }

    public JTextComponent component() {
        return (componentRef != null) ? componentRef.get() : null;
    }

    private void setComponent(JTextComponent component) {
        this.componentRef = (component != null) ? new WeakReference<JTextComponent>(component) : null;
    }

    public Document document() {
        return doc;
    }

    void markIgnoreDocModifications() {
        ignoreDocModifications++;
    }

    /**
     * Add a sync group to the manager of text regions which will cause
     * the regions to be updated by the changes performed in the document.
     * <br/>
     * This method should be called under document's readlock to ensure
     * that the document will not be modified during execution of this method.
     * 
     * @param textSyncGroup non-null text sync group.
     * @param offsetShift shift added to the offsets contained in the text regions
     *  in the group being added. The resulting offsets will be turned into positions.
     * @throws javax.swing.text.BadLocationException
     */
    private void addGroupUpdate(TextSyncGroup<?> textSyncGroup, int offsetShift) throws BadLocationException {
        if (textSyncGroup.textRegionManager() != null)
            throw new IllegalArgumentException("TextSyncGroup=" + textSyncGroup // NOI18N
                    + " already assigned to " + textSyncGroup.textRegionManager()); // NOI18N

        TextRegion<?> lastAdded = null;
        try {
            for (TextSync textSync : textSyncGroup.textSyncsModifiable()) {
                for (TextRegion<?> textRegion : textSync.regions()) {
                    Position startPos = doc.createPosition(textRegion.startOffset() + offsetShift);
                    Position endPos = doc.createPosition(textRegion.endOffset() + offsetShift);
                    textRegion.setStartPos(startPos);
                    textRegion.setEndPos(endPos);
                    addRegion(rootRegion, textRegion);
                    lastAdded = textRegion;
                }
            }
            lastAdded = null; // All were added
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("ADD textSyncGroup: " + textSyncGroup + '\n');
            }
        } finally {
            removeAddedSoFar(textSyncGroup, lastAdded);
        }
        textSyncGroup.setTextRegionManager(this);
    }
    
    private void removeAddedSoFar(TextSyncGroup<?> textSyncGroup, TextRegion<?> lastAdded) {
        // Created as a method (instead of a cycle in finally { } due to a crashing javac during compilation
        while (lastAdded != null) { // Remove what was added so far
            for (TextSync textSync : textSyncGroup.textSyncsModifiable()) {
                for (TextRegion<?> textRegion : textSync.regions()) {
                    removeRegionFromParent(textRegion);
                    if (textRegion == lastAdded) {
                        return;
                    }
                }
            }
        }
    }

    private void removeGroupUpdate(TextSyncGroup<?> textSyncGroup) {
        textSyncGroup.setTextRegionManager(null);
        for (TextSync textSync : textSyncGroup.textSyncsModifiable()) {
            for (TextRegion<?> textRegion : textSync.regions()) {
                removeRegionFromParent(textRegion);
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("REMOVE textSyncGroup: " + textSyncGroup + '\n');
        }
    }
    
    void setActiveTextSync(TextSync textSync) {
        if (textSync.masterRegion() == null)
            throw new IllegalArgumentException("masterRegion expected to be non-null"); // NOI18N
        this.activeTextSync = textSync;
        updateMasterRegionBounds();
    }

    int findEditableTextSyncIndex(TextSyncGroup<?> group, int textSyncIndex,
            int direction, boolean cycle, boolean skipCaretMarkers
    ) {
        int tsCount = group.textSyncsModifiable().size();
        if (tsCount == 0) { // Cannot sync without sync items
            return -1;
        }

        int startTextSyncIndex = -1;
        do { // Check whether the index is valid
            if (textSyncIndex >= tsCount) {
                if (cycle)
                    textSyncIndex = 0;
                else
                    break;
            } else if (textSyncIndex < 0) {
                if (cycle)
                    textSyncIndex = tsCount - 1;
                else
                    break;
            }
            if (startTextSyncIndex == -1)
                startTextSyncIndex = textSyncIndex;

            TextSync textSync = group.textSyncs().get(textSyncIndex);
            if (textSync.isEditable() || (!skipCaretMarkers && textSync.isCaretMarker())) {
                return textSyncIndex;
            }
            textSyncIndex += direction;
        } while (textSyncIndex != startTextSyncIndex);
        return -1;
    }

    /**
     * Activate the particular textSyncIndex.
     *
     * @param textSyncIndex index of the text sync to be activated.
     */
    private void activateTextSync(List<TextSyncGroup<?>> removedGroups,
            TextSyncGroup<?> group, int textSyncIndex, boolean selectText
    ) {
        TextSync previousTextSync = activeTextSync;
        boolean removeGroup = false;
        if (textSyncIndex == INVALID_TEXT_SYNC) { // May be result of finding findEditableTextSyncIndex() returning -1
            removeGroup = true;
        }
        if (group != null) {
            if (textSyncIndex == SAME_TEXT_SYNC) {
                textSyncIndex = group.activeTextSyncIndex();
            }
            group.setActiveTextSyncIndex(textSyncIndex);
            activeTextSync = group.activeTextSync();
            if (activeTextSync.isCaretMarker()) {
                // Place the caret at the marker and release this group
                int offset = activeTextSync.regions().get(0).startOffset();
                JTextComponent component = component();
                if (component != null) {
                    component.setCaretPosition(offset);
                }
                removeGroup = true;
            }
        } else {
            activeTextSync = null;
        }
        while (removeGroup && group != null) {
            removeGroup = false;
            selectText = false;
            int groupIndex = editGroups.indexOf(group);
            assert (groupIndex >= 0);
            removedGroups = removeLastGroups(removedGroups, editGroups.size() - groupIndex);
            // Use previous group
            if (groupIndex > 0) {
                group = editGroups.get(groupIndex - 1);
                textSyncIndex = group.activeTextSyncIndex();
                activeTextSync = group.activeTextSync();
            } else { // Was first group
                group = null;
                activeTextSync = null;
            }
        }

        if (group != null) {
            JTextComponent component = component();
            if (component != null) {
                setActiveTextSync(activeTextSync);
                ((BaseTextUI)component.getUI()).getEditorUI().getWordMatch().clear();
                if (selectText) {
                    TextRegion activeRegion = activeTextSync.masterRegion();
                    component.select(activeRegion.startOffset(), activeRegion.endOffset());
                }

                if (!overridingKeys) {
                    overridingKeys = true;
                    // Not adding listener permanently in constructor since
                    // it grabbed the TAB key from code-template expansion
                    component.addKeyListener(OverrideKeysListener.INSTANCE);
                    ActionMap [] maps = OverrideAction.installOverrideActionMap(component);
                    origActionMap = maps[0];
                    overrideActionMap = maps[1];
                }
            }
        }
        // Fire focus change event
        TextRegionManagerEvent evt = createEvent(true, removedGroups, previousTextSync);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Firing event - focusing of activeTextSync:\n" + activeTextSync + // NOI18N
                    "previousTextSync=" + previousTextSync + ", removedGroups=" + removedGroups + '\n'); // NOI18N
        }
        fireEvent(evt);
        if (removedGroups != null) {
            removeGroupsUpdate();
        }
        highlighting.requestRepaint();
    }

    private void releaseLastGroups(int count) {
        if (editGroups.size() > 0) {
            List<TextSyncGroup<?>> removedGroups = removeLastGroups(null, count);
            activateTextSync(removedGroups, lastGroup(), SAME_TEXT_SYNC, false); // Will fire event
        }
    }

    private List<TextSyncGroup<?>> removeLastGroups(List<TextSyncGroup<?>> removedGroups, int count) {
        assert (count >= 0 && count <= editGroups.size());
        int groupIndex = editGroups.size() - count;
        if (removedGroups == null) {
            removedGroups = new GapList<TextSyncGroup<?>>(count);
        }
        while (--count >= 0) {
            TextSyncGroup<?> group = editGroups.remove(groupIndex + count);
            removeGroupUpdate(group);
            removedGroups.add(0, group);
        }
        return removedGroups;
    }

    private void removeGroupsUpdate() {
        if (editGroups.size() == 0) {
            JTextComponent component = component();
            if (doc instanceof BaseDocument) {
                BaseDocument bdoc = (BaseDocument) doc;
                // Add the listener to allow doc syncing modifications
                // The listener is never removed (since this object is a property of the document)
                bdoc.removePostModificationDocumentListener(DocListener.INSTANCE);
                bdoc.removeUpdateDocumentListener(UpdateDocListener.INSTANCE);
            }
            activeTextSync = null;
            componentRef = null;

            if (overridingKeys) {
                overridingKeys = false;
                component.removeKeyListener(OverrideKeysListener.INSTANCE);

                // check if the action map is still our overrideActionMap
                if (overrideActionMap != component.getActionMap()) {
                    LOG.warning("The action map got tampered with! component=" //NOI18
                        + component.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(component)) //NOI18N
                        + "; doc=" + component.getDocument()); //NOI18N
                } else {
                    component.setActionMap(origActionMap);
                }

                overrideActionMap.clear();
                origActionMap = null;
                overrideActionMap = null;
            }
        }
    }

    void activeTextSyncModified() {
        TextRegionManagerEvent evt = createEvent(false, Collections.<TextSyncGroup<?>>emptyList(), activeTextSync);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Firing event - mod of activeTextSync=" + activeTextSync + '\n'); // NOI18N
        }
        fireEvent(evt);
        highlighting.requestRepaint();
    }

    boolean enterAction() {
        TextSync textSync = activeTextSync();
        if (textSync != null) {
            TextRegion<?> master = textSync.validMasterRegion();
            JTextComponent component = component();
            if (master.startOffset() <= component.getCaretPosition() && component.getCaretPosition() <= master.endOffset()) {
                TextSyncGroup<?> group = textSync.group();
                activateTextSync(null, group,
                        findEditableTextSyncIndex(group, group.activeTextSyncIndex() + 1, +1, false, false),
                        true);
                return true;
            }
            releaseLastGroups(1);
        } else {
            // #145443 - I'm not sure why this is called when there is no active
            // TextSync, but apparently it happens under certain circumstances. So
            // let's try closing all active groups, which will terminate the special editing mode.
            releaseLastGroups(editGroups.size());
        }
        return false;
    }

    void escapeAction() {
        releaseLastGroups(1);
    }

    void tabAction() {
        TextSync textSync = activeTextSync();
        if (textSync != null) {
            TextSyncGroup<?> group = textSync.group();
            int index = findEditableTextSyncIndex(group, group.activeTextSyncIndex() + 1, +1, true, true);
            if (index != -1) {
                activateTextSync(null, group, index, true);
            } // otherwise stay on current text sync
        }
    }

    void shiftTabAction() {
        TextSync textSync = activeTextSync();
        if (textSync != null) {
            TextSyncGroup<?> group = textSync.group();
            int index = findEditableTextSyncIndex(group, group.activeTextSyncIndex() - 1, -1, true, true);
            if (index != -1) {
                activateTextSync(null, group, index, true);
            } // otherwise stay on current text sync
        }
    }


    boolean isActive() {
        return !editGroups.isEmpty();
    }

    List<TextRegion<?>> regions() { // For tests only
        return rootRegion.regions();
    }

    void insertUpdate(DocumentEvent evt) {
        if (ignoreDocModifications == 0) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("TextRegionManager.insertUpdate(): evt:" + DocumentUtilities.appendEvent(null, evt) + "\n");
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("BEFORE-INSERT-UPDATE REGIONS: " + this);
                }
            }
            if (activeTextSync != null) {
                ignoreDocModifications++;
                try {
                    int offset = evt.getOffset();
                    int insertLength = evt.getLength();
                    String insertText = DocumentUtilities.getModificationText(evt);
                    if (insertText == null) {
                        try {
                            insertText = doc.getText(offset, insertLength);
                        } catch (BadLocationException e) {
                            throw new IllegalStateException(e); // Should never happen
                        }
                    }
                    boolean syncSuccess = false;
                    if (offset > masterRegionStartOffset) {
                        if (offset <= masterRegionEndOffset) { // Within master region
                            TextRegion<?> master = activeTextSync.validMasterRegion();
                            int relOffset = offset - master.startOffset();
                            if (relOffset <= 0) {
                                // See #146105 - the undo will cause the master's start position
                                // to be above the insertion point => offset < 0
                                stopSyncEditing();
                                return;
                            }
                            beforeDocumentModification();
                            boolean oldTM = DocumentUtilities.isTypingModification(doc);
                            DocumentUtilities.setTypingModification(doc, false);
                            try {
                                if (forceSyncByMaster) {
                                    forceSyncByMaster = false;
                                    syncByMaster(activeTextSync);
                                } else {
                                    for (TextRegion<?> region : activeTextSync.regions()) {
                                        if (region != master) {
                                            doc.insertString(region.startOffset() + relOffset, insertText, null);
                                        }
                                    }
                                }
                            } finally {
                                DocumentUtilities.setTypingModification(doc, oldTM);
                                afterDocumentModification();
                            }
                            syncSuccess = true;
                        }

                    } else if (offset == masterRegionStartOffset) { // Insert at begining of master region
                        // This will require fixing of regions' start positions.
                        // In adition adjacent regions may need to be fixed too if they were
                        // ending at the begining of the region which start position was fixed.
                        TextRegion<?> master = activeTextSync.validMasterRegion();
                        fixRegionStartOffset(master, offset);
                        beforeDocumentModification();
                        boolean oldTM = DocumentUtilities.isTypingModification(doc);
                        DocumentUtilities.setTypingModification(doc, false);
                        try {
                            if (forceSyncByMaster) {
                                forceSyncByMaster = false;
                                syncByMaster(activeTextSync);
                            } else { // Sync by inserting the right text
                                for (TextRegion<?> region : activeTextSync.regions()) {
                                    if (region != master) {
                                        int startOffset = region.startOffset();
                                        doc.insertString(startOffset, insertText, null);
                                        fixRegionStartOffset(region, startOffset);
                                    }
                                }
                            }
                        } finally {
                            DocumentUtilities.setTypingModification(doc, oldTM);
                            afterDocumentModification();
                        }
                        syncSuccess = true;

                    } // otherwise below master region

                    if (syncSuccess) {
                        activeTextSyncModified();
                    } else { // Not synced successfully
                        // In case this was typing modification the synced editing should end
                        if (DocumentUtilities.isTypingModification(doc)) {
                            stopSyncEditing();
                        }
                    }
                } catch (BadLocationException e) {
                    LOG.log(Level.WARNING, "Unexpected exception during synchronization", e); // NOI18N
                } finally {
                    assert (ignoreDocModifications > 0);
                    ignoreDocModifications--;
                }
            } else { // activeTextSync == null
                if (DocumentUtilities.isTypingModification(doc)) {
                    stopSyncEditing();
                }
            }
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("AFTER-INSERT-UPDATE REGIONS: " + this);
            }
        } // else controlled doc modification to be ignored
        updateMasterRegionBounds();
    }
    
    void removeUpdate(DocumentEvent evt) {
        if (ignoreDocModifications == 0) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("TextRegionManager.removeUpdate(): evt:" + DocumentUtilities.appendEvent(null, evt) + "\n");
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("BEFORE-REMOVE-UPDATE REGIONS: " + this);
                }
            }
            if (activeTextSync != null) {
                ignoreDocModifications++;
                try {
                    int offset = evt.getOffset();
                    int removeLength = evt.getLength();
                    if (offset >= masterRegionStartOffset && offset + removeLength <= masterRegionEndOffset) {
                        TextRegion<?> master = activeTextSync.validMasterRegion();
                        int relOffset = offset - master.startOffset();
                        beforeDocumentModification();
                        boolean oldTM = DocumentUtilities.isTypingModification(doc);
                        DocumentUtilities.setTypingModification(doc, false);
                        try {
                            // In case of JTextComponent.replaceSelection()
                            // the DOC_REPLACE_SELECTION_PROPERTY is expected to be set to TRUE
                            // by surrounding editor code. It must be treated specially
                            // since the replaceSelection() remembers mod-offset as int
                            // and reuses it for insert so any post-modifications
                            // (that could generally precede the mod-offset would be fatal
                            // and result into an insertion at incorrect offset.
                            // At the time of subsequent insert the slaves must be re-synced
                            // by the contents of the master.
                            if (Boolean.TRUE.equals(doc.getProperty(BaseKit.DOC_REPLACE_SELECTION_PROPERTY))) {
                                forceSyncByMaster = true;
                            } else {
                                for (TextRegion<?> region : activeTextSync.regions()) {
                                    if (region != master) {
                                        doc.remove(region.startOffset() + relOffset, removeLength);
                                    }
                                }
                                activeTextSyncModified();
                            }
                        } catch (BadLocationException e) {
                            stopSyncEditing();
                            LOG.log(Level.WARNING, "Unexpected exception during synchronization", e); // NOI18N
                        } finally {
                            DocumentUtilities.setTypingModification(doc, oldTM);
                            afterDocumentModification();
                        }
                    } else { // Not synced successfully
                        if (DocumentUtilities.isTypingModification(doc)) {
                            stopSyncEditing();
                        }
                    }
                } finally {
                    assert (ignoreDocModifications > 0);
                    ignoreDocModifications--;
                }
            } else {
                if (DocumentUtilities.isTypingModification(doc)) {
                    stopSyncEditing();
                }
            }
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("AFTER-REMOVE-UPDATE REGIONS: " + this);
            }
        } // else controlled doc modification to be ignored
        updateMasterRegionBounds();
    }

    /**
     * Called before the actual removal in document is done.
     *
     * @param evt
     */
    void removeUpdateUpdate(DocumentEvent evt) {
        // Check whether removal would affect any contained text regions
        // and if so remove their associated text sync groups.
        int minGroupIndex = Integer.MAX_VALUE;
        int removeStartOffset = evt.getOffset();
        int removeEndOffset = removeStartOffset + evt.getLength();
        List<TextRegion<?>> regions = rootRegion.regions();
        int index = findRegionInsertIndex(regions, removeStartOffset);
        if (index > 0) { // Check whether region at index-1 is not affected by the removal
            TextRegion<?> region = regions.get(index - 1);
            minGroupIndex = findMinGroupIndex(minGroupIndex, region, removeStartOffset, removeEndOffset);
        }
        for (;index < regions.size(); index++) {
            TextRegion<?> region = regions.get(index);
            if (region.startOffset() >= removeEndOffset) {
                break;
            }
            minGroupIndex = findMinGroupIndex(minGroupIndex, region, removeStartOffset, removeEndOffset);
        }
        if (minGroupIndex != Integer.MAX_VALUE) {
            int removeCount = editGroups.size() - minGroupIndex;
            if (LOG.isLoggable(Level.FINE)) {
                StringBuilder sb = new StringBuilder(100);
                sb.append("removeUpdateUpdate(): Text remove <").append(removeStartOffset);
                sb.append(",").append(removeEndOffset).append(">.\n  Removing GROUPS <");
                sb.append(minGroupIndex).append(",").append(editGroups.size()).append(">\n");
                LOG.fine(sb.toString());
            }
            releaseLastGroups(removeCount);
        }
    }

    private int findMinGroupIndex(int minGroupIndex, TextRegion<?> region, int removeStartOffset, int removeEndOffset) {
        BlockCompare bc = BlockCompare.get(removeStartOffset, removeEndOffset, region.startOffset(), region.endOffset());
        TextSyncGroup<?> group = region.textSync().group();
        boolean scanChildren = false;
        if (!bc.emptyY()) {
            if (bc.overlap() || bc.containsStrict()) {
                if (LOG.isLoggable(Level.FINE)) {
                    StringBuilder sb = new StringBuilder(100);
                    sb.append("removeUpdateUpdate(): Text remove <").append(removeStartOffset);
                    sb.append(",").append(removeEndOffset).append(">.\n  Conflicting region ").append(region).append('\n');
                    LOG.fine(sb.toString());
                }
                minGroupIndex = Math.min(minGroupIndex, editGroups.indexOf(group));
                scanChildren = true;
            } else {
                scanChildren = bc.inside();
            }
            if (scanChildren) {
                List<TextRegion<?>> childRegions = region.regions();
                if (childRegions != null) {
                    for (TextRegion<?> childRegion : childRegions) {
                        minGroupIndex = findMinGroupIndex(minGroupIndex, childRegion, removeStartOffset, removeEndOffset);
                    }
                }
            }
        }
        return minGroupIndex;
    }

    private void beforeDocumentModification() {
        doc.putProperty("abbrev-ignore-modification", Boolean.TRUE); // NOI18N
    }
    
    private void afterDocumentModification() {
        doc.putProperty("abbrev-ignore-modification", Boolean.FALSE); // NOI18N
    }
    
    private void activate() {
        if (editGroups.size() == 0) {
            if (doc instanceof BaseDocument) {
                BaseDocument bdoc = (BaseDocument)doc;
                // Add the listener to allow doc syncing modifications
                // The listener is never removed (since this object is a property of the document)
                bdoc.addPostModificationDocumentListener(DocListener.INSTANCE);
                bdoc.addUpdateDocumentListener(UpdateDocListener.INSTANCE);
            }
        }
    }

    void syncByMaster(TextSync textSync) {
        beforeDocumentModification();
        ignoreDocModifications++;
        boolean oldTM = DocumentUtilities.isTypingModification(doc);
        DocumentUtilities.setTypingModification(doc, false);
        try {
            TextRegion<?> masterRegion = textSync.validMasterRegion();
            CharSequence docText = DocumentUtilities.getText(doc);
            CharSequence masterRegionText = docText.subSequence(
                    masterRegion.startOffset(), masterRegion.endOffset());
            String masterRegionString = null;
            for (TextRegion<?> region : textSync.regionsModifiable()) {
                if (region == masterRegion)
                    continue;
                int regionStartOffset = region.startOffset();
                int regionEndOffset = region.endOffset();
                CharSequence regionText = docText.subSequence(regionStartOffset, regionEndOffset);
                if (!CharSequenceUtilities.textEquals(masterRegionText, regionText)) {
                    // Must re-insert
                    if (masterRegionString == null)
                        masterRegionString = masterRegionText.toString();
                    doc.remove(regionStartOffset, regionEndOffset - regionStartOffset);
                    doc.insertString(regionStartOffset, masterRegionString, null);
                    fixRegionStartOffset(region, regionStartOffset);
                }
            }
        } catch (BadLocationException e) {
           LOG.log(Level.WARNING, "Invalid offset", e); // NOI18N 
        } finally {
            DocumentUtilities.setTypingModification(doc, oldTM);
            assert (ignoreDocModifications > 0);
            ignoreDocModifications--;
            afterDocumentModification();
        }
        updateMasterRegionBounds();
    }
    
    void setText(TextSync textSync, String text) {
        beforeDocumentModification();
        ignoreDocModifications++;
        boolean oldTM = DocumentUtilities.isTypingModification(doc);
        DocumentUtilities.setTypingModification(doc, false);
        try {
            CharSequence docText = DocumentUtilities.getText(doc);
            for (TextRegion<?> region : textSync.regionsModifiable()) {
                int regionStartOffset = region.startOffset();
                int regionEndOffset = region.endOffset();
                CharSequence regionText = docText.subSequence(regionStartOffset, regionEndOffset);
                if (!CharSequenceUtilities.textEquals(text, regionText)) {
                    // Must re-insert
                    doc.remove(regionStartOffset, regionEndOffset - regionStartOffset);
                    doc.insertString(regionStartOffset, text, null);
                    fixRegionStartOffset(region, regionStartOffset);
                }
            }
        } catch (BadLocationException e) {
           LOG.log(Level.WARNING, "Invalid offset", e); // NOI18N 
        } finally {
            DocumentUtilities.setTypingModification(doc, oldTM);
            assert (ignoreDocModifications > 0);
            ignoreDocModifications--;
            afterDocumentModification();
        }
        updateMasterRegionBounds();
    }
    
    private void fixRegionStartOffset(TextRegion<?> region, int offset) throws BadLocationException {
        assert (!isRoot(region)) : "Cannot fix root document's start offset"; // NOI18N
        TextRegion<?> parent = region.parent();
        if (parent == null) { // Give warning for null parent since the code that follows will fail
            LOG.warning("Region with null parent:\n" + region + "\nRegions:\n" + this + "\n\n");
        }
        // Fix previous regions' offsets
        List<TextRegion<?>> regions = parent.regions();
        int index = findRegionIndex(regions, region) - 1;
        Position pos = doc.createPosition(offset);

        region.setStartPos(pos);
        while (index >= 0) {
            region = regions.get(index);
            if (region.endOffset() > offset)
                region.setEndPos(pos);
            else
                break;
            if (region.startOffset() > offset)
                region.setStartPos(pos);
            else
                break;
        }
        if (index < 0) { // Fixed first region -> check parent
            if (!isRoot(parent) && parent.startOffset() > offset) {
                parent.setStartPos(pos);
                fixRegionStartOffset(parent, offset);
            }
        }
    }
    
    private boolean isRoot(TextRegion region) {
        return (region == rootRegion);
    }
    
    private void updateMasterRegionBounds() {
        if (activeTextSync != null) {
            masterRegionStartOffset = activeTextSync.masterRegion().startOffset();
            masterRegionEndOffset = activeTextSync.masterRegion().endOffset();
        }
    }
    
    /**
     * Get index at which the region's start offset is greater than the given offset.
     * 
     * @param regions regions in which to search.
     * @param offset >=0 offset 
     * @return index >=0 index at which the region's start offset is greater than the given offset.
     */
    static int findRegionInsertIndex(List<TextRegion<?>> regions, int offset) {
        int low = 0;
        int high = regions.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            TextRegion<?> midRegion = regions.get(mid);
            int midStartOffset = midRegion.startOffset();

            if (midStartOffset < offset) {
                low = mid + 1;
            } else if (midStartOffset > offset) {
                high = mid - 1;
            } else {
                // Region starting exactly at startOffset found -> get index above it
                low = mid + 1;
                while (low < regions.size() && regions.get(low).startOffset() == offset) {
                    low++;
                }
                break;
            }
        }
        return low;
    }
    
    static int findRegionIndex(List<TextRegion<?>> regions, TextRegion<?> region) {
        int low = 0;
        int high = regions.size() - 1;
        int offset = region.startOffset();
        while (low <= high) {
            int mid = (low + high) / 2;
            TextRegion<?> midRegion = regions.get(mid);
            int midStartOffset = midRegion.startOffset();

            if (midStartOffset < offset) {
                low = mid + 1;
            } else if (midStartOffset > offset) {
                high = mid - 1;
            } else {
                // Region starting exactly at startOffset found
                if (midRegion == region)
                    return mid;
                // Search backward in adjacent regions
                low = mid - 1;
                while (low >= 0) {
                    midRegion = regions.get(low);
                    if (midRegion == region)
                        return low;
                    if (midRegion.startOffset() != offset)
                        break;
                    low--;
                }
                // Search forward in adjacent regions
                low = mid + 1;
                while (low < regions.size()) {
                    midRegion = regions.get(low);
                    if (midRegion == region)
                        return low;
                    if (midRegion.startOffset() != offset) {
                        break; // a 'break' below will break the outer loop
                    }
                    low++;
                }
            }
        }
        throw new IllegalStateException("Region: " + region + " not found by binary search:\n" + // NOI18N
                dumpRegions(null, regions, 4)); // NOI18N
    }

    static void addRegion(TextRegion<?> parent, TextRegion<?> region) {
        if (region.parent() != null)
            throw new IllegalArgumentException("Region:" + region + " already added."); // NOI18N
        List<TextRegion<?>> regions = parent.validRegions();
        int regionStartOffset = region.startOffset();
        int regionEndOffset = region.endOffset();
        int insertIndex = findRegionInsertIndex(regions, regionStartOffset);
        // Check the regions containment and overlapping
        // Prefer containment of existing regions into the one being inserted
        // since the inserted one's positions will likely be produced later
        // so possible undo of removals would retain containment.
        int endConsumeIndex = insertIndex; // >0 if region-param consumes existing regions
        while (endConsumeIndex < regions.size()) {
            TextRegion<?> consumeCandidate = regions.get(endConsumeIndex);
            if (regionEndOffset < consumeCandidate.endOffset()) { // region-param does not fully contain consumeCandidate
                if (regionEndOffset <= consumeCandidate.startOffset()) { // Region and consumeCandidate do not overlap
                    break;
                } else {
                    throw new IllegalArgumentException("Inserted region " + region + // NOI18N
                            " overlaps with region " + consumeCandidate + // NOI18N
                            " at index=" + endConsumeIndex // NOI18N
                        );
                }
            } // otherwise Region fully contains consumeCandidate
            endConsumeIndex++;
        }
        while (insertIndex > 0) {
            TextRegion<?> prev = regions.get(insertIndex - 1);
            int prevEndOffset;
            if (regionStartOffset == prev.startOffset()) { // region-param eats prev?
                if (regionEndOffset < (prevEndOffset = prev.endOffset())) { // region-param inside prev
                    if (regionStartOffset != regionEndOffset) { // region-param is non-empty 
                        addRegion(prev, region);
                        return;
                    } else { // Region will be inserted right before this region
                        insertIndex--;
                        endConsumeIndex = insertIndex;
                        break;
                    }
                } // Region consumes prev - continue
            } else { // startOffset > prevStartOffset
                if (regionStartOffset >= (prevEndOffset = prev.endOffset())) { // Region does not overlap prev
                    break;
                } else if (regionEndOffset <= prevEndOffset) { // Region nests into prev
                    addRegion(prev, region);
                    return;
                } else {
                    throw new IllegalArgumentException("Inserted region " + region + // NOI18N
                            " overlaps with region " + prev + // NOI18N
                            " at index=" + (insertIndex - 1)); // NOI18N
                }
            }
            insertIndex--;
        }
        if (endConsumeIndex - insertIndex > 0) { // Do consume
            GapList<TextRegion<?>> regionsGL = (GapList<TextRegion<?>>)regions;
            TextRegion<?>[] consumedRegions = new TextRegion<?>[endConsumeIndex - insertIndex];
            regionsGL.copyElements(insertIndex, endConsumeIndex, consumedRegions, 0);
            regionsGL.remove(insertIndex, consumedRegions.length);
            region.initRegions(consumedRegions);
            for (TextRegion<?> r : consumedRegions)
                r.setParent(region);
        }
        regions.add(insertIndex, region);
        region.setParent(parent);
    }
    
    static void removeRegionFromParent(TextRegion<?> region) {
        TextRegion<?> parent = region.parent();
        List<TextRegion<?>> regions = parent.regions();
        int index = findRegionIndex(regions, region);
        regions.remove(index);
        region.setParent(null);
        // Move possible children to the regions
        List<TextRegion<?>> children = region.regions();
        if (children != null) {
            for (TextRegion<?> child : children) {
                regions.add(index++, child);
                child.setParent(parent);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("Managed regions:\n");
        dumpRegions(sb, rootRegion.regions(), 4);
        sb.append("Managed groups:\n");
        for (TextSyncGroup<?> group : editGroups) {
            sb.append("  ").append(group).append('\n');
        }
        if (activeTextSync != null) {
            sb.append("Active textSync: ").append(activeTextSync).append('\n');
        }
        sb.append('\n');
        return sb.toString();
    }
    
    private static StringBuilder dumpRegions(StringBuilder sb, List<TextRegion<?>> regions, int indent) {
        if (sb == null)
            sb = new StringBuilder(100);
        if (regions == null)
            return sb;
        for (TextRegion<?> region : regions) {
            ArrayUtilities.appendSpaces(sb, indent);
            sb.append(region).append('\n');
            dumpRegions(sb, region.regions(), indent + 4);
        }
        return sb;
    }
    
    private static final class DocListener implements DocumentListener {

        static final DocListener INSTANCE = new DocListener();

        public void insertUpdate(DocumentEvent e) {
            TextRegionManager.get(e.getDocument(), false).insertUpdate(e);
        }

        public void removeUpdate(DocumentEvent e) {
            TextRegionManager.get(e.getDocument(), false).removeUpdate(e);
        }

        public void changedUpdate(DocumentEvent e) {
        }

    }

    private static final class UpdateDocListener implements DocumentListener {

        static final UpdateDocListener INSTANCE = new UpdateDocListener();

        public void insertUpdate(DocumentEvent e) {
        }

        public void removeUpdate(DocumentEvent e) {
            TextRegionManager.get(e.getDocument(), false).removeUpdateUpdate(e);
        }

        public void changedUpdate(DocumentEvent e) {
        }

    }

    private static final class DocChangeListener implements PropertyChangeListener {

        static final DocChangeListener INSTANCE = new DocChangeListener();

        public void propertyChange(PropertyChangeEvent evt) {
            if ("document".equals(evt.getPropertyName())) {
                TextRegionManager manager = TextRegionManager.get(((JTextComponent)evt.getSource()).getDocument(), false);
                if (manager != null) {
                    manager.stopSyncEditing();
                }
            }
        }

    }

    private static final class OverrideAction extends TextAction {

        private static final String ORIGINAL_ACTION_PROPERTY = "original-action"; // NOI18N

        private static final int TAB = 1;
        private static final int SHIFT_TAB = 2;
        private static final int ENTER = 3;

        public static ActionMap [] installOverrideActionMap(JTextComponent component) {
            ActionMap origActionMap = component.getActionMap();
            ActionMap actionMap = new ActionMap();
            OverrideAction[] actions = new OverrideAction[]{
                new OverrideAction(TAB),
                new OverrideAction(SHIFT_TAB),
                new OverrideAction(ENTER),
            };

            // Install the actions into new action map
            for (OverrideAction action : actions) {
                Object actionKey = (String) action.getValue(Action.NAME);
                assert (actionKey != null);
                // Translate to the real key in the action map
                actionKey = action.findActionKey(component);
                if (actionKey != null) { // == null may happen during unit tests
                    Action origAction = origActionMap.get(actionKey);
                    action.putValue(ORIGINAL_ACTION_PROPERTY, origAction);
                    actionMap.put(actionKey, action);
                }
            }
            actionMap.setParent(origActionMap);
            // Install the new action map and return the original action map
            component.setActionMap(actionMap);
            return new ActionMap [] { origActionMap, actionMap };
        }

        private static String actionType2Name(int actionType) {
            switch (actionType) {
                case TAB:
                    return BaseKit.insertTabAction;
                case SHIFT_TAB:
                    return BaseKit.removeTabAction;
                case ENTER:
                    return DefaultEditorKit.insertBreakAction;
                default:
                    throw new IllegalArgumentException();
            }
        }
        private final int actionType;

        private OverrideAction(int actionType) {
            super(actionType2Name(actionType));
            this.actionType = actionType;
        }

        private TextRegionManager textRegionManager(ActionEvent evt) {
            JTextComponent component = getTextComponent(evt);
            if (component != null) {
                return TextRegionManager.get(component.getDocument(), false);
            }
            return null;
        }

        public void actionPerformed(ActionEvent evt) {
            TextRegionManager manager = textRegionManager(evt);
            if (manager != null) {
                switch (actionType) {
                    case TAB:
                        manager.tabAction();
                        break;
                    case SHIFT_TAB:
                        manager.shiftTabAction();
                        break;
                    case ENTER:
                        if (!manager.enterAction()) {
                            Action original = (Action)getValue(ORIGINAL_ACTION_PROPERTY);
                            if (original != null)
                                original.actionPerformed(evt);
                        }
                        break;
                }
            }
        }

        Object findActionKey(JTextComponent component) {
            KeyStroke keyStroke;
            switch (actionType) {
                case TAB:
                    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
                    break;
                case SHIFT_TAB:
                    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK);
                    break;
                case ENTER:
                    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            // Assume the 'a' character will trigger defaultKeyTypedAction
            Object key = component.getInputMap().get(keyStroke);
            return key;
        }
    }

    private static final class OverrideKeysListener implements KeyListener {

        static OverrideKeysListener INSTANCE = new OverrideKeysListener();

        public void keyPressed(KeyEvent evt) {
            TextRegionManager manager = textRegionManager(evt);
            if (manager == null || !manager.isActive() || evt.isConsumed())
                return;

            KeyStroke evtKeyStroke = KeyStroke.getKeyStrokeForEvent(evt);
            if (KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0) == evtKeyStroke) {
                manager.escapeAction();
                evt.consume();

//            } else if (KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0) == evtKeyStroke) {
//                if (editing.enterAction())
//                    evt.consume();
//
//            } else if (KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0) == evtKeyStroke) {
//                if (editing.tabAction())
//                    evt.consume();
//
//            } else if (KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK) == evtKeyStroke) {
//                if (editing.shiftTabAction())
//                    evt.consume();
//
            }
        }

        public void keyTyped(KeyEvent evt) {
        }

        public void keyReleased(KeyEvent evt) {
        }

        private TextRegionManager textRegionManager(KeyEvent evt) {
            return TextRegionManager.get(((JTextComponent)evt.getSource()).getDocument(), false);
        }

    }

    private static final class Highlighting {

        private static final String BAG_DOC_PROPERTY = TextRegionManager.class.getName() + "-OffsetsBag"; // NOI18N

        private TextRegionManager textRegionManager;

        private AttributeSet attribs = null;
        private AttributeSet attribsLeft = null;
        private AttributeSet attribsRight = null;
        private AttributeSet attribsMiddle = null;
        private AttributeSet attribsAll = null;

        Highlighting(TextRegionManager textRegionManager) {
            this.textRegionManager = textRegionManager;
        }

        void requestRepaint() {
            Document doc = textRegionManager.document();
            TextSync activeTextSync = textRegionManager.activeTextSync();
            TextRegion masterRegion;
            if (activeTextSync != null && (masterRegion = activeTextSync.masterRegion()) != null) {
                // Compute attributes
                if (attribs == null) {
                    attribs = getSyncedTextBlocksHighlight();
                    Color foreground = (Color) attribs.getAttribute(StyleConstants.Foreground);
                    Color background = (Color) attribs.getAttribute(StyleConstants.Background);
                    attribsLeft = createAttribs(
                            StyleConstants.Background, background,
                            EditorStyleConstants.LeftBorderLineColor, foreground,
                            EditorStyleConstants.TopBorderLineColor, foreground,
                            EditorStyleConstants.BottomBorderLineColor, foreground);
                    attribsRight = createAttribs(
                            StyleConstants.Background, background,
                            EditorStyleConstants.RightBorderLineColor, foreground,
                            EditorStyleConstants.TopBorderLineColor, foreground,
                            EditorStyleConstants.BottomBorderLineColor, foreground);
                    attribsMiddle = createAttribs(
                            StyleConstants.Background, background,
                            EditorStyleConstants.TopBorderLineColor, foreground,
                            EditorStyleConstants.BottomBorderLineColor, foreground);
                    attribsAll = createAttribs(
                            StyleConstants.Background, background,
                            EditorStyleConstants.LeftBorderLineColor, foreground,
                            EditorStyleConstants.RightBorderLineColor, foreground,
                            EditorStyleConstants.TopBorderLineColor, foreground,
                            EditorStyleConstants.BottomBorderLineColor, foreground);
                }

                OffsetsBag nue = new OffsetsBag(doc);
                try {

                    int startOffset = masterRegion.startOffset();
                    int endOffset = masterRegion.endOffset();
                    int startLine = LineDocumentUtils.getLineIndex((BaseDocument) doc, startOffset);
                    int endLine = LineDocumentUtils.getLineIndex((BaseDocument) doc, endOffset);

                    for (int i = startLine; i <= endLine; i++) {
                        int s = Math.max(LineDocumentUtils.getLineStartFromIndex((BaseDocument) doc, i), startOffset);
                        int e = Math.min(LineDocumentUtils.getLineEndOffset((BaseDocument) doc, s), endOffset);
                        int size = e - s;

                        if (size == 1) {
                            nue.addHighlight(s, e, attribsAll);
                        } else if (size > 1) {
                            nue.addHighlight(s, s + 1, attribsLeft);
                            nue.addHighlight(e - 1, e, attribsRight);
                            if (size > 2) {
                                nue.addHighlight(s + 1, e - 1, attribsMiddle);
                            }
                        }
                    }
                } catch (BadLocationException ble) {
                    LOG.log(Level.WARNING, null, ble);
                }

                OffsetsBag bag = getBag(doc);
                bag.setHighlights(nue);

            } else { // No active text sync
                OffsetsBag bag = getBag(doc);
                bag.clear();
                attribs = null;
            }
        }

        private static synchronized OffsetsBag getBag(Document doc) {
            OffsetsBag bag = (OffsetsBag) doc.getProperty(BAG_DOC_PROPERTY);
            if (bag == null) {
                bag = new OffsetsBag(doc);
                doc.putProperty(BAG_DOC_PROPERTY, bag);
            }
            return bag;
        }

        private static AttributeSet getSyncedTextBlocksHighlight() {
            FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
            AttributeSet as = fcs.getFontColors("synchronized-text-blocks-ext"); //NOI18N
            return as == null ? SimpleAttributeSet.EMPTY : as;
        }

        private static AttributeSet createAttribs(Object... keyValuePairs) {
            assert keyValuePairs.length % 2 == 0 : "There must be even number of prameters. " +
                    "They are key-value pairs of attributes that will be inserted into the set.";

            List<Object> list = new ArrayList<Object>(keyValuePairs.length);

            for (int i = keyValuePairs.length / 2 - 1; i >= 0; i--) {
                Object attrKey = keyValuePairs[2 * i];
                Object attrValue = keyValuePairs[2 * i + 1];

                if (attrKey != null && attrValue != null) {
                    list.add(attrKey);
                    list.add(attrValue);
                }
            }

            return AttributesUtilities.createImmutable(list.toArray());
        }

        public static final class HLFactory implements HighlightsLayerFactory {

            public HighlightsLayer[] createLayers(Context context) {
                return new HighlightsLayer[]{
                            HighlightsLayer.create(
                            "org.netbeans.lib.editor.codetemplates.CodeTemplateParametersHighlights", //NOI18N
                            ZOrder.SHOW_OFF_RACK.forPosition(490),
                            true,
                            getBag(context.getDocument()))
                        };
            }
        } // End of HLFactory class

    }

}
