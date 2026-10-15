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

package org.netbeans.modules.editor.fold;

import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.event.DocumentEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * Fold maintainer that creates and updates custom folds.
 *
 * @author Dusan Balek, Miloslav Metelka
 * @version 1.00
 */

public final class CustomFoldManager implements FoldManager, Runnable {
    
    private static final Logger LOG = Logger.getLogger(CustomFoldManager.class.getName());
    
    public static final FoldType CUSTOM_FOLD_TYPE = new FoldType("custom-fold"); // NOI18N

    private FoldOperation operation;
    private Document doc;
    private GapObjectArray markArray = new GapObjectArray();
    private int minUpdateMarkOffset = Integer.MAX_VALUE;
    private int maxUpdateMarkOffset = -1;
    private List removedFoldList;
    private HashMap<String, Boolean> customFoldId = new HashMap<>();

    private static final RequestProcessor RP = new RequestProcessor(CustomFoldManager.class.getName(),
            1, false, false);
    private final RequestProcessor.Task task = RP.create(this);
    
    private final String tokenId;
    
    public CustomFoldManager() {
        this.tokenId = "comment";
    }

    public CustomFoldManager(String tokenId) {
        Parameters.notNull("tokenId", tokenId);
        this.tokenId = tokenId;
    }
    
    public void init(FoldOperation operation) {
        this.operation = operation;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Initialized: {0}", System.identityHashCode(this));
        }
    }
    
    private FoldOperation getOperation() {
        return operation;
    }

    public void initFolds(FoldHierarchyTransaction transaction) {
        doc = getOperation().getHierarchy().getComponent().getDocument();
        task.schedule(300);
    }

    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        processRemovedFolds(transaction);
        task.schedule(300);
    }

    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        processRemovedFolds(transaction);
        removeAffectedMarks(evt, transaction);
        task.schedule(300);
    }
    
    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }
    
    public void removeEmptyNotify(Fold emptyFold) {
        removeFoldNotify(emptyFold);
    }
    
    public void removeDamagedNotify(Fold damagedFold) {
        removeFoldNotify(damagedFold);
    }
    
    public void expandNotify(Fold expandedFold) {
        
    }

    public void release() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Released: {0}", System.identityHashCode(this));
        }
    }

    public void run() {
        if (operation.isReleased()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Update skipped, already relaesed: {0}", System.identityHashCode(this));
            }
            return;
        }
        doc.render(new Runnable() {
            public void run() {
                TokenHierarchy th = TokenHierarchy.get(doc);
                if (th != null && th.isActive()) {
                    FoldHierarchy hierarchy = getOperation().getHierarchy();
                    hierarchy.lock();
                    try {
                        if (operation.isReleased()) {
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.log(Level.FINE, "Update skipped, already relaesed: {0}", System.identityHashCode(this));
                            }
                            return;
                        }
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "Updating: {0}", System.identityHashCode(this));
                        }
                        FoldHierarchyTransaction transaction = getOperation().openTransaction();
                        try {
                            updateFolds(th.tokenSequence(), transaction);
                        } finally {
                            transaction.commit();
                        }
                    } finally {
                        hierarchy.unlock();
                    }
                }
            }
        });
    }
    
    private void removeFoldNotify(Fold removedFold) {
        if (removedFoldList == null) {
            removedFoldList = new ArrayList(3);
        }
        removedFoldList.add(removedFold);
    }
    
    private void removeAffectedMarks(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        int removeOffset = evt.getOffset();
        int markIndex = findMarkIndex(removeOffset);
        if (markIndex < getMarkCount()) {
            FoldMarkInfo mark;
            while (markIndex >= 0 && (mark = getMark(markIndex)).getOffset() == removeOffset) {
                mark.release(false, transaction);
                removeMark(markIndex);
                markIndex--;
            }
        }
    }
    
    private void processRemovedFolds(FoldHierarchyTransaction transaction) {
        if (removedFoldList != null) {
            for (int i = removedFoldList.size() - 1; i >= 0; i--) {
                Fold removedFold = (Fold)removedFoldList.get(i);
                FoldMarkInfo startMark = (FoldMarkInfo)getOperation().getExtraInfo(removedFold);
                if (startMark.getId() != null)
                    customFoldId.put(startMark.getId(), removedFold.isCollapsed()); // remember the last fold's state before remove
                FoldMarkInfo endMark = startMark.getPairMark(); // get prior releasing
                if (getOperation().isStartDamaged(removedFold)) { // start mark area was damaged
                    startMark.release(true, transaction); // forced remove
                }
                if (getOperation().isEndDamaged(removedFold)) {
                    endMark.release(true, transaction);
                }
            }
        }
        removedFoldList = null;
    }

    private void markUpdate(FoldMarkInfo mark) {
        markUpdate(mark.getOffset());
    }
    
    private void markUpdate(int offset) {
        if (offset < minUpdateMarkOffset) {
            minUpdateMarkOffset = offset;
        }
        if (offset > maxUpdateMarkOffset) {
            maxUpdateMarkOffset = offset;
        }
    }
    
    private FoldMarkInfo getMark(int index) {
        return (FoldMarkInfo)markArray.getItem(index);
    }
    
    private int getMarkCount() {
        return markArray.getItemCount();
    }
    
    private void removeMark(int index) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Removing mark from ind=" + index + ": " + getMark(index)); // NOI18N
        }
        markArray.remove(index, 1);
    }
    
    private void insertMark(int index, FoldMarkInfo mark) {
        markArray.insertItem(index, mark);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Inserted mark at ind=" + index + ": " + mark); // NOI18N
        }
    }

    private int findMarkIndex(int offset) {
        int markCount = getMarkCount();
        int low = 0;
        int high = markCount - 1;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            int midMarkOffset = getMark(mid).getOffset();
            
            if (midMarkOffset < offset) {
                low = mid + 1;
            } else if (midMarkOffset > offset) {
                high = mid - 1;
            } else {
                // mark starting exactly at the given offset found
                // If multiple -> find the one with highest index
                mid++;
                while (mid < markCount && getMark(mid).getOffset() == offset) {
                    mid++;
                }
                mid--;
                return mid;
            }
        }
        return low; // return higher index (e.g. for insert)
    }
    
    private List<FoldMarkInfo> getMarkList(TokenSequence seq) {
        List<FoldMarkInfo> markList = null;
        
        for(seq.moveStart(); seq.moveNext(); ) {
            Token token = seq.token();
            FoldMarkInfo info;
            try {
                info = scanToken(token);
            } catch (BadLocationException e) {
                LOG.log(Level.WARNING, null, e);
                info = null;
            }

            if (info != null) {
                if (markList == null) {
                    markList = new ArrayList<FoldMarkInfo>();
                }
                markList.add(info);
            }
        }

        return markList;
    }
    
    private void processTokenList(TokenSequence seq, FoldHierarchyTransaction transaction) {
        List<FoldMarkInfo> markList = getMarkList(seq);
        int markListSize;
        if (markList != null && ((markListSize = markList.size()) > 0)) {
            // Find the index for insertion
            int offset = ((FoldMarkInfo)markList.get(0)).getOffset();
            int arrayMarkIndex = findMarkIndex(offset);
            // Remember the corresponding mark in the array as well
            FoldMarkInfo arrayMark;
            int arrayMarkOffset;
            if (arrayMarkIndex < getMarkCount()) {
                arrayMark = getMark(arrayMarkIndex);
                arrayMarkOffset = arrayMark.getOffset();
            } else { // at last mark
                arrayMark = null;
                arrayMarkOffset = Integer.MAX_VALUE;
            }

            for (int i = 0; i < markListSize; i++) {
                FoldMarkInfo listMark = (FoldMarkInfo)markList.get(i);
                int listMarkOffset = listMark.getOffset();
                if (i == 0 || i == markListSize - 1) {
                    // Update the update-offsets by the first and last marks in the list
                    markUpdate(listMarkOffset);
                }
                while (listMarkOffset >= arrayMarkOffset) {
                    if (listMarkOffset == arrayMarkOffset) {
                        // At the same offset - likely the same mark
                        //   -> retain the collapsed state
                        listMark.setCollapsed(arrayMark.isCollapsed());
                    }
                    if (!arrayMark.isReleased()) { // make sure that the mark is released
                        arrayMark.release(false, transaction); 
                    }
                    removeMark(arrayMarkIndex);
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Removed dup mark from ind=" + arrayMarkIndex + ": " + arrayMark); // NOI18N
                    }
                    if (arrayMarkIndex < getMarkCount()) {
                        arrayMark = getMark(arrayMarkIndex);
                        arrayMarkOffset = arrayMark.getOffset();
                    } else { // no more marks
                        arrayMark = null;
                        arrayMarkOffset = Integer.MAX_VALUE;
                    }
                }
                // Insert the listmark
                insertMark(arrayMarkIndex, listMark);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Inserted mark at ind=" + arrayMarkIndex + ": " + listMark); // NOI18N
                }
                arrayMarkIndex++;
            }
        }
    }

    private void updateFolds(TokenSequence seq, FoldHierarchyTransaction transaction) {

        if (seq != null && !seq.isEmpty()) {
            processTokenList(seq, transaction);
        }

        if (maxUpdateMarkOffset == -1) { // no updates
            return;
        }
        
        // Find the first mark to update and init the prevMark and parentMark prior the loop
        int index = findMarkIndex(minUpdateMarkOffset);
        FoldMarkInfo prevMark;
        FoldMarkInfo parentMark;
        if (index == 0) { // start from begining
            prevMark = null;
            parentMark = null;
        } else {
            prevMark = getMark(index - 1);
            parentMark = prevMark.getParentMark();
        }
        
        // Iterate through the changed marks in the mark array 
        int markCount = getMarkCount();
        while (index < markCount) { // process the marks
            FoldMarkInfo mark = getMark(index);

            // If the mark was released then it must be removed
            if (mark.isReleased()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Removing released mark at ind=" + index + ": " + mark); // NOI18N
                }
                removeMark(index);
                markCount--;
                continue;
            }

            // Update mark's status (folds, parentMark etc.)
            if (mark.isStartMark()) { // starting a new fold
                if (prevMark == null || prevMark.isStartMark()) { // new level
                    mark.setParentMark(prevMark); // prevMark == null means root level
                    parentMark = prevMark;

                } // same level => parent to the parent of the prevMark

            } else { // end mark
                if (prevMark != null) {
                    if (prevMark.isStartMark()) { // closing nearest fold
                        prevMark.setEndMark(mark, false, transaction);

                    } else { // prevMark is end mark - closing its parent fold
                        if (parentMark != null) {
                            // mark's parent gets set as well
                            parentMark.setEndMark(mark, false, transaction);
                            parentMark = parentMark.getParentMark();

                        } else { // prevMark's parentMark is null (top level)
                            mark.makeSolitaire(false, transaction);
                        }
                    }
                    
                } else { // prevMark is null
                    mark.makeSolitaire(false, transaction);
                }
            }

            // Set parent mark of the mark
            mark.setParentMark(parentMark);

            
            prevMark = mark;
            index++;
        }

        minUpdateMarkOffset = Integer.MAX_VALUE;
        maxUpdateMarkOffset = -1;
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("MARKS DUMP:\n" + this); //NOI18N
        }
    }
    
    public @Override String toString() {
        StringBuffer sb = new StringBuffer();
        int markCount = getMarkCount();
        int markCountDigitCount = Integer.toString(markCount).length();
        for (int i = 0; i < markCount; i++) {
            sb.append("["); // NOI18N
            String iStr = Integer.toString(i);
            appendSpaces(sb, markCountDigitCount - iStr.length());
            sb.append(iStr);
            sb.append("]:"); // NOI18N
            FoldMarkInfo mark = getMark(i);
            
            // Add extra indent regarding the depth in hierarchy
            int indent = 0;
            FoldMarkInfo parentMark = mark.getParentMark();
            while (parentMark != null) {
                indent += 4;
                parentMark = parentMark.getParentMark();
            }
            appendSpaces(sb, indent);

            sb.append(mark);
            sb.append('\n');
        }
        return sb.toString();
    }
    
    private static void appendSpaces(StringBuffer sb, int spaces) {
        while (--spaces >= 0) {
            sb.append(' ');
        }
    }

    private static Pattern pattern = Pattern.compile(
            "(<\\s*editor-fold" +
            // id="x"[opt] defaultstate="y"[opt] desc="z"[opt] defaultstate="a"[opt]
            // id must be first, the rest of attributes in random order
            "(?:(?:\\s+id=\"(\\S*)\")?(?:\\s+defaultstate=\"(\\S*?)\")?(?:\\s+desc=\"([\\S \\t]*?)\")?(?:\\s+defaultstate=\"(\\S*?)\")?)" +
            "\\s*>)|(?:</\\s*editor-fold\\s*>)"); // NOI18N

    private FoldMarkInfo scanToken(Token token) throws BadLocationException {
        // ignore any token that is not comment
        if (token.id().primaryCategory() != null && token.id().primaryCategory().startsWith(tokenId)) { //NOI18N
            Matcher matcher = pattern.matcher(token.text());
            if (matcher.find()) {
                if (matcher.group(1) != null) { // fold's start mark found
                    boolean state;
                    if (matcher.group(3) != null) {
                        state = "collapsed".equals(matcher.group(3)); // remember the defaultstate // NOI18N
                    } else {
                        state = "collapsed".equals(matcher.group(5));
                    }
                    
                    if (matcher.group(2) != null) { // fold's id exists
                        Boolean collapsed = (Boolean)customFoldId.get(matcher.group(2));
                        if (collapsed != null)
                            state = collapsed; // fold's state is already known from the past
                        else
                            customFoldId.put(matcher.group(2), state);
                    }
                    return new FoldMarkInfo(true, token.offset(null), matcher.end(0), matcher.group(2), state, matcher.group(4)); // NOI18N
                } else { // fold's end mark found
                    return new FoldMarkInfo(false, token.offset(null), matcher.end(0), null, false, null);
                }
            }
        }
        return null;
    }

    private final class FoldMarkInfo {
        private boolean startMark;
        private Position pos;
        private int length;
        private String id;
        private boolean collapsed;
        private String description;

        /** Matching pair mark used for fold construction */
        private FoldMarkInfo pairMark;
        
        /** Parent mark defining nesting in the mark hierarchy. */
        private FoldMarkInfo parentMark;
        
        /**
         * Fold that corresponds to this mark (if it's start mark).
         * It can be null if this mark is end mark or if it currently
         * does not have the fold assigned.
         */
        private Fold fold;
        
        private boolean released;
        
        private FoldMarkInfo(boolean startMark, int offset,
                             int length, String id, boolean collapsed, String description)
        throws BadLocationException {

            this.startMark = startMark;
            this.pos = doc.createPosition(offset);
            this.length = length;
            this.id = id;
            this.collapsed = collapsed;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public boolean isStartMark() {
            return startMark;
        }

        public int getLength() {
            return length;
        }

        public int getOffset() {
            return pos.getOffset();
        }
        
        public int getEndOffset() {
            return getOffset() + getLength();
        }

        public boolean isCollapsed() {
            return (fold != null) ? fold.isCollapsed() : collapsed;
        }
        
        public boolean hasFold() {
            return (fold != null);
        }
        
        public void setCollapsed(boolean collapsed) {
            this.collapsed = collapsed;
        }
        
        public boolean isSolitaire() {
            return (pairMark == null);
        }
        
        public void makeSolitaire(boolean forced, FoldHierarchyTransaction transaction) {
            if (!isSolitaire()) {
                if (isStartMark()) {
                    setEndMark(null, forced, transaction);
                } else { // end mark
                    getPairMark().setEndMark(null, forced, transaction);
                }
            }
        }
        
        public boolean isReleased() {
            return released;
        }
        
        /**
         * Release this mark and mark for update.
         */
        public void release(boolean forced, FoldHierarchyTransaction transaction) {
            if (!released) {
                makeSolitaire(forced, transaction);
                released = true;
                markUpdate(this);
            }
        }
        
        public FoldMarkInfo getPairMark() {
            return pairMark;
        }
        
        private void setPairMark(FoldMarkInfo pairMark) {
            this.pairMark = pairMark;
        }

        public void setEndMark(FoldMarkInfo endMark, boolean forced,
        FoldHierarchyTransaction transaction) {
            if (!isStartMark()) {
                throw new IllegalStateException("Not start mark"); // NOI18N
            }
            if (pairMark == endMark) {
                return;
            }
            
            if (pairMark != null) { // is currently paired to an end mark
                releaseFold(forced, transaction);
                pairMark.setPairMark(null);
            }

            pairMark = endMark;
            if (endMark != null) {
                if (!endMark.isSolitaire()) { // make solitaire first
                    endMark.makeSolitaire(false, transaction); // not forced here
                }
                endMark.setPairMark(this);
                endMark.setParentMark(this.getParentMark());
                ensureFoldExists(transaction);
            }
        }
        
        public FoldMarkInfo getParentMark() {
            return parentMark;
        }
        
        public void setParentMark(FoldMarkInfo parentMark) {
            this.parentMark = parentMark;
        }
        
        private void releaseFold(boolean forced, FoldHierarchyTransaction transaction) {
            if (isSolitaire() || !isStartMark()) {
               throw new IllegalStateException();
            }

            if (fold != null) {
                setCollapsed(fold.isCollapsed()); // serialize the collapsed info
                if (!forced) {
                    getOperation().removeFromHierarchy(fold, transaction);
                }
                fold = null;
            }
        }

        public Fold getFold() {
            if (isSolitaire()) {
                return null;
            }
            if (!isStartMark()) {
                return pairMark.getFold();
            }
            return fold;
        }
        
        public void ensureFoldExists(FoldHierarchyTransaction transaction) {
            if (isSolitaire() || !isStartMark()) {
                throw new IllegalStateException();
            }

            if (fold == null) {
                try {
                    if (!startMark) {
                        throw new IllegalStateException("Not start mark: " + this); // NOI18N
                    }
                    if (pairMark == null) {
                        throw new IllegalStateException("No pairMark for mark:" + this); // NOI18N
                    }
                    int startOffset = getOffset();
                    int startGuardedLength = getLength();
                    int endGuardedLength = pairMark.getLength();
                    int endOffset = pairMark.getOffset() + endGuardedLength;
                    fold = getOperation().addToHierarchy(
                        CUSTOM_FOLD_TYPE, getDescription(), collapsed,
                        startOffset, endOffset,
                        startGuardedLength, endGuardedLength,
                        this,
                        transaction
                    );
                } catch (BadLocationException e) {
                    LOG.log(Level.WARNING, null, e);
                }
            }
        }
        
        public @Override String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(isStartMark() ? 'S' : 'E');  // NOI18N
            
            // Check whether this mark (or its pair) has fold
            if (hasFold() || (!isSolitaire() && getPairMark().hasFold())) {
                sb.append("F"); // NOI18N
                
                // Check fold's status
                if (isStartMark() && (isSolitaire()
                        || getOffset() != fold.getStartOffset()
                        || getPairMark().getEndOffset() != fold.getEndOffset())
                ) {
                    sb.append("!!<"); // NOI18N
                    sb.append(fold.getStartOffset());
                    sb.append(","); // NOI18N
                    sb.append(fold.getEndOffset());
                    sb.append(">!!"); // NOI18N
                }
            }

            // Append mark's internal status
            sb.append(" ("); // NOI18N
            sb.append("o="); // NOI18N
            sb.append(pos.getOffset());
            sb.append(", l="); // NOI18N
            sb.append(length);
            sb.append(", d='"); // NOI18N
            sb.append(description);
            sb.append('\'');
            if (getPairMark() != null) {
                sb.append(", <->"); // NOI18N
                sb.append(getPairMark().getOffset());
            }
            if (getParentMark() != null) {
                sb.append(", ^"); // NOI18N
                sb.append(getParentMark().getOffset());
            }
            sb.append(')');
            
            return sb.toString();
        }

    }
        
    public static final class Factory implements FoldManagerFactory {

        public FoldManager createFoldManager() {
            return new CustomFoldManager();
        }
    }
}
