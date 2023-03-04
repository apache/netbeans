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

package org.netbeans.modules.csl.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;

/**
 * <p>The EditHistory object contains information about a set of edits that
 * have occurred in a given Document recently. This is typically used to
 * support <a href="../../../../../incremental-parsing.html">incremental parsing</a>.
 * The IDE infrastructure will hand a parser
 * its old parse tree along with an EditHistory. The EditHistory represents
 * edits made since the previous parse.  If the parser supports incremental
 * parsing, it can use the edit history to determine if it can parse just
 * a sub-portion of the buffer (for example, just the current method body)
 * and therefore do a lot less work. More importantly, it can record this
 * information in its ParserResult, and features that are driven off of the
 * parse tree can do a lot less work.
 * </p>
 * <p>
 * The EditHistory tracks edits accurately, so you can use the
 * {@link #convertOldToNew(int)} method to translate a pre-edits offsets
 * to a post-edits offsets.  However, the EditHistory maintains a couple
 * of attributes that are usually more interesting:
 * <ol>
 * <li> The offset</li>
 * <li> The original size</li>
 * <li> The edited size</li>
 * </ol>
 * These three parameters indicate that in the old document, the text between
 * <code>offset</code> and <code>offset+originalSize</code> has been modified,
 * and after the edits, this region corresponds to
 * <code>offset</code> to <code>offset+editedSize</code>. Put another way,
 * all document positions below <code>offset</code> are unaffected by the edits,
 * and all document positions above <code>offset+originalSize</code> are uniformly
 * shifted up by a delta of <code>editedSize-originalSize</code> (which can be negative,
 * when more text was deleted than added).
 * </p>
 * <p>
 * Here's how this works. Consider the following document:
 * <pre>
 *   Offsets:     0123456789ABCDEF
 *   Document:    Hello World!
 * </pre>
 * Let's apply 3 edits: removing the "e" character", the "r" character,
 * and inserting an extra space character in the middle. We now end up with:
 * <pre>
 *   Offsets:     0123456789ABCDEF
 *   Old Doc:     Hello World!
 *   New Doc:     Hllo  Wold!
 * </pre>
 * As you can see, some characters in the middle here have been edited.
 * The affected block is shown in bold as follows:
 * <pre>
 *   Offsets:     0123456789ABCDEF
 *   Old Doc:     H<b>ello Wor</b>ld!
 *   New Doc:     H<b>llo  Wo</b>ld!
 * </pre>
 * Therefore, in this document, the affected range begins at offset 1,
 * and in the original document the affected block had size 8, and in
 * the edited document the affected block size is 7. The delta is -1.
 * Incremental parsing clients can use this to traverse their data, and
 * seeing if it is in the affected region. If not, they can simply adjust
 * their offsets (by adding delta for offsets above the affected region,
 * and nothing for offsets below the affected region).
 * </p>
 * <p>For more information about incremental parsing, see the
 * <a href="../../../../../incremental-parsing.html">incremental updating</a>
 * document.</p>
 *
 * @author Tor Norbye
 */
public final class EditHistory implements DocumentListener, TokenHierarchyListener {
    private static final Object ADDED = new Object();
    private static final Object REMOVED = new Object();
    //private static final Object ADDED_AND_REMOVED = new Object();

    private int start = -1;
    private int originalEnd = -1;
    private int editedEnd = -1;
    private List<Edit> edits = new ArrayList<Edit>(4);
    private Map<TokenId,Object> tokenIds = new IdentityHashMap<TokenId,Object>(); // Really just want an IdentitySet!
    private int delta = 0;
    private boolean valid = true; // TODO mark it valid until there is a history event!

    EditHistory previous; // package protected only for tests
    private int version = -1;

    /**
     * The beginning position of the damaged region.
     */
    public int getStart() {
        return start;
    }

    /**
     * Check if the position is in the damaged region (inclusive)
     * @param pos The position
     * @return True iff the position is inside the damaged region
     */
    public boolean isInDamagedRegion(int pos) {
        if (start == -1) {
            return false;
        }
        return (pos >= start && pos <= editedEnd);
    }

    /**
     * Check if the range overlaps the damaged region (inclusive)
     * @param range The range
     * @return True iff the range overlaps the damaged region
     */
    public boolean isInDamagedRegion(OffsetRange range) {
        if (start == -1) {
            return false;
        }
        return range.getStart() < editedEnd && range.getEnd() > start;
    }

    /** Return whether this EditHistory is considered valid */
    public boolean isValid() {
        return valid;
    }

    /** Set whether this EditHistory is considered valid */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * The size of the edits. Could be negative (when more text has been
     * deleted than added). The key rule is that
     * <pre>
     *   oldText[i] = newText[i]   for i &lt; offset, and
     *   oldText[i] = newText[i+editedSize]  for i &gt;= offset
     * </pre>
     */
    public int getEditedSize() {
        return editedEnd - start;
    }

    /**
     * The end of the affected region, in the original document.
     * @return The offset of the end of the affected region in the original document
     */
    public int getOriginalEnd() {
        return originalEnd;
    }

    /**
     * The end of the affected region, in the edited document.
     * @return The offset of the end of the affected region in the edited document
     */
    public int getEditedEnd() {
        return editedEnd;
    }

    public int getSizeDelta() {
        return delta;
    }

    /**
     * The original size of the region that was damaged. The first character
     * after offset+originalSize before the edits, corresponds to the character
     * at offset+editedSize after the edits.
     */
    public int getOriginalSize() {
        return originalEnd - start;
    }

    /**
     * Return the version id of this edit history
     * @return The version number of this edit history
     */
    public int getVersion() {
        return version;
    }

    /** Return true iff the given token id was one of the modified (added or removed) tokens
     * in this edit history. If null is passed in, false will be returned.
     *
     * @param id The token id to be checked
     * @return True iff the given token id has appeared in this edit history
     */
    public boolean wasModified(@NullAllowed TokenId id) {
        if (id == null) {
            return false;
        }
        return tokenIds.containsKey(id);
    }

    /**
     * Convert a position before edits to a corresponding position after edits.
     * @param oldPos The position in the unedited document
     * @return The corresponding position after the edits
     */
    public int convertOriginalToEdited(int oldPos) {
        if (start == -1 || oldPos <= start) {
            return oldPos;
        }

        if (oldPos >= originalEnd) {
            return oldPos+delta;
        }

        // Perform more accurate translation:
        // Apply individual edits (which will usually just involve a couple of operations)

        List<Edit> list = edits;
        int len = list.size();
        if (len == 0) {
            return oldPos;
        }
        for (int i = 0; i < len; i++) {
            Edit edit = list.get(i);
            if (oldPos > edit.offset) {
                if (edit.insert) {
                    oldPos += edit.len;
                } else if (oldPos < edit.offset+edit.len) {
                    oldPos = edit.offset;
                } else {
                    oldPos -= edit.len;
                }
            }
        }

        if (oldPos < 0) {
            oldPos = 0;
        }

        return oldPos;
    }

    /**
     * Convert a position post-edits to a corresponding position pre-edits
     * @param newPos The position in the edited document
     * @return The corresponding position prior to the edits
     */
    public int convertEditedToOriginal(int newPos) {
        List<Edit> list = edits;
        int len = list.size();
        if (len == 0) {
            return newPos;
        }
        for (int i = len-1; i >= 0; i--) {
            Edit edit = list.get(i);
            if (edit.insert) {
                if (newPos > edit.offset) {
                    if (newPos < edit.offset+edit.len) {
                        // If it's anywhere INSIDE this block it was newly
                        // added by this insert - decide if I want to handle
                        // this differently.
                        newPos = edit.offset;
                    } else {
                        newPos -= edit.len;
                    }
                } // else: offset unaffected by the insert
            } else {
                // Remove
                if (newPos >= edit.offset) {
                    newPos += edit.len;
                }
            }
        }

        if (newPos < 0) {
            newPos = 0;
        }

        return newPos;
    }

    /**
     * Notify the EditHistory of a document edit (insert).
     */
    public void insertUpdate(final DocumentEvent e) {
        int pos = e.getOffset();
        int length = e.getLength();
        insertUpdate(pos, length);
    }

    private void insertUpdate(final int pos, final int length) {
        // TODO - synchronize?
        edits.add(new Edit(pos, length, true));

        if (start == -1) {
            start = pos;
            originalEnd = pos;
            editedEnd = pos+length;
            delta = length;
        } else {
            // Compute history backwards
            int original = convertEditedToOriginal(pos);
            if (original > originalEnd) {
                originalEnd = original;
            }
            if (pos < start) {
                start = pos;
            }
            if (pos > editedEnd) {
                editedEnd = pos+length;
            } else {
                editedEnd += length;
            }
            delta = getEditedSize()-getOriginalSize();
        }
    }

    /**
     * Notify the EditHistory of a document edit (remove).
     */
    public void removeUpdate(final DocumentEvent e) {
        int pos = e.getOffset();
        int length = e.getLength();

        removeUpdate(pos, length);
    }

    private void removeUpdate(final int pos, final int length) {
        // TODO - synchronize?
        edits.add(new Edit(pos, length, false));

        if (start == -1) {
            start = pos;
            originalEnd = pos+length;
            editedEnd = pos;
            delta = -length;
        } else {
            // TODO
            int original = convertEditedToOriginal(pos);
            if (original > originalEnd) {
                originalEnd = original;
            } else if (pos+length > editedEnd) {
                originalEnd += (pos+length-editedEnd);
            }

            if (pos > editedEnd) {
                editedEnd = pos;
            } else {
                editedEnd -= length;
                if (editedEnd < pos) {
                    editedEnd = pos;
                }
            }

            if (pos < start) {
                start = pos;
            }

            delta = getEditedSize()-getOriginalSize();
        }
    }

    /**
     * Notify the EditHistory of a document edit (change). Attribute changes
     * are not tracked by the EditHistory.
     */
    public void changedUpdate(DocumentEvent e) {
    }

    public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
        TokenHierarchyEventType type = evt.type();
        if (type == TokenHierarchyEventType.MODIFICATION) {
            changed(evt.tokenChange());
        } else if (type == TokenHierarchyEventType.REBUILD) {
            // Lexing has fundamentally changed, don't try to do anything incremental
            valid = false;
        }
    }

    public void changed(TokenChange change) {
        // Embedded changes
        int embeddedCount = change.embeddedChangeCount();
        for (int i = 0; i < embeddedCount; i++) {
            changed(change.embeddedChange(i)); // Recurse
        }

        if (change.removedTokenCount() > 0) {
            TokenSequence<?> removed = change.removedTokenSequence();
            if (removed != null) {
                removed.moveStart();
                while (removed.moveNext()) {
                    Token<?> token = removed.token();
                    if (token != null) {
                        TokenId id = token.id();
                        tokenIds.put(id, REMOVED);
                    }
                }
            }
        }

        if (change.addedTokenCount() > 0) {
            TokenSequence<?> current = change.currentTokenSequence();
            if (current != null) {
                current.moveIndex(change.index());
                for (int i = 0, n = change.addedTokenCount(); current.moveNext() && i < n; i++) {
                    Token<?> token = current.token();
                    if (token != null) {
                        TokenId id = token.id();
                        //if (tokenIds.containsKey(id)) {
                        //    tokenIds.put(id, ADDED_AND_REMOVED);
                        //} else {
                        tokenIds.put(id, ADDED);
                        //}
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "EditHistory(version=" + version + ", offset=" + start + ", originalSize=" + getOriginalSize() + ", editedSize=" + getEditedSize() + ", delta=" + delta + ")"; // NOI18N
    }

    /** Maximum number of previous edit histories to keep */
    private static final int MAX_KEEP = 15;

    public void add(@NonNull EditHistory history) {
        history.previous = this;
        history.version = version+1;

        // Chop off old history. We only need the most recent versions. Typically
        // we only need the most recent history, but in some cases (e.g. when documents
        // are edited during a parse job, the job gets split in two so we need to combine
        // the edit histories)
        if (history.version % MAX_KEEP == 0) {
            EditHistory curr = history;
            for (int i = 0; i < MAX_KEEP; i++) {
                curr = curr.previous;
                if (curr == null) {
                    return;
                }
            }
            curr.previous = null;
        }
    }

    @CheckForNull
    public static EditHistory getCombinedEdits(int lastVersion, @NonNull EditHistory mostRecent) {
        if (!mostRecent.isValid()) {
            return mostRecent;
        }

        if (mostRecent.previous == null || mostRecent.version == lastVersion) {
            return null;
        }
        if (mostRecent.previous.version == lastVersion) {
            return mostRecent;
        }

        // Combine edit histories back as far as the version calls for

        EditHistory current = mostRecent;
        List<EditHistory> histories = new ArrayList<EditHistory>();
        while (current.version != lastVersion) {
            histories.add(current);
            if (current.version == lastVersion) {
                break;
            }

            current = current.previous;
            if (current == null) {
                if (lastVersion == -1) {
                    // We're looking for history since the beginning
                    break;
                } else {
                    // Version not found!
                    return null;
                }
            }
        }

        // Process history from the beginning
        EditHistory result = new EditHistory();
        Collections.reverse(histories);
        for (EditHistory history : histories) {
            // TODO - I should be able to do this more intelligently!
            // I should be able to just merge the start/originalEnd/editedEnd
            // regions directly! On the other hand, edits here are typically going
            // to be small
            for (Edit edit : history.edits) {
                if (edit.insert) {
                    result.insertUpdate(edit.offset, edit.len);
                } else {
                    result.removeUpdate(edit.offset, edit.len);
                }
            }
            result.tokenIds.putAll(history.tokenIds);
        }

        return result;
    }

    /**
     * An Edit is a modification (insert/remove) we've been notified about from the document
     * since the last time we updated our "colorings" object.
     * The list of Edits lets me quickly compute the current position of an original
     * position in the "colorings" object. This is typically going to involve only a couple
     * of edits (since the colorings object is updated as soon as the user stops typing).
     * This is probably going to be more efficient than updating all the colorings offsets
     * every time the document is updated, since the colorings object can contain thousands
     * of ranges (e.g. for every highlight in the whole document) whereas asking for the
     * current positions is typically only done for the highlights visible on the screen.
     */
    private class Edit {
        private Edit(int offset, int len, boolean insert) {
            this.offset = offset;
            this.len = len;
            this.insert = insert;
        }

        private final int offset;
        private final int len;
        private final boolean insert; // true: insert, false: delete
    }

    /** This is just a helper for tests until I figure out why for unit tests,
     * the TokenHierarchyListener doesn't seem to get any changes from the getChange()
     * call on the TokenHierarchyListenerEvent
     */
    public void testHelperNotifyToken(boolean add, TokenId id) {
        if (add) {
            tokenIds.put(id, ADDED);
        } else {
            tokenIds.put(id, REMOVED);
        }
    }
}
