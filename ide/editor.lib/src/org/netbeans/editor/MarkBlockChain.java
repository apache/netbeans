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

package org.netbeans.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.text.BadLocationException;

/**
* Support class for chain of MarkBlocks
*
* @author Miloslav Metelka
* @version 1.00
*/

public class MarkBlockChain {

    public static final String PROP_BLOCKS_CHANGED = "MarkBlockChain.PROP_BLOCKS_CHANGED"; //NOI18N
    
    /** Chain of all blocks */
    protected MarkBlock chain;

    /** Current block to make checks faster */
    protected MarkBlock currentBlock;

    /** Document for this block */
    protected BaseDocument doc;

    private final PropertyChangeSupport PCS = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener l) {
        PCS.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        PCS.removePropertyChangeListener(l);
    }
    
    /** Construct chain using regular base marks */
    public MarkBlockChain(BaseDocument doc) {
        this.doc = doc;
    }

    public final MarkBlock getChain() {
        return chain;
    }

    /** Tests whether the position range is partly or fully inside
    * some mark block from the chain.
    * @param startPos starting position of tested area
    * @param endPos ending position of tested area for removal or same
    *   as startPos when insert is made
    * @return relation of currentBlock to the given block
    */
    public synchronized int compareBlock(int startPos, int endPos) {
        if (currentBlock == null) {
            currentBlock = chain;
            if (currentBlock == null) {
                return MarkBlock.INVALID;
            }
        }

        int rel; // relation of block to particular mark block
        boolean afterPrev = false; // blk is after previous block
        boolean beforeNext = false; // blk is before next block
        boolean cont = false; // blk continued currentBlock in previous match
        MarkBlock contBlk = null;
        int contRel = 0;
        while (true) {
            rel = currentBlock.compare(startPos, endPos);
            if ((rel & MarkBlock.OVERLAP) != 0) {
                return rel;
            }

            if ((rel & MarkBlock.AFTER) != 0) { // after this mark block
                if (beforeNext) {
                    if (!cont || (rel & MarkBlock.CONTINUE) != 0) {
                        return rel;
                    } else { // continues with contBlk and this relation is pure after
                        currentBlock = contBlk;
                        return contRel;
                    }
                } else { // going from begining of chain
                    if (currentBlock.next != null) {
                        afterPrev = true;
                        cont = ((rel & MarkBlock.CONTINUE) != 0);
                        if (cont) {
                            contRel = rel;
                            contBlk = currentBlock;
                        }
                        currentBlock = currentBlock.next;
                    } else { // end of chain
                        return rel;
                    }
                }
            } else { // before this mark block
                if (afterPrev) {
                    if (!cont || (rel & MarkBlock.EXTEND) != 0) {
                        return rel;
                    } else {
                        currentBlock = contBlk;
                        return contRel;
                    }
                } else { // going from end of chain
                    if (currentBlock.prev != null) {
                        beforeNext = true;
                        cont = ((rel & MarkBlock.CONTINUE) != 0);
                        if (cont) {
                            contRel = rel;
                            contBlk = currentBlock;
                        }
                        currentBlock = currentBlock.prev;
                    } else { // begining of chain
                        return rel;
                    }
                }
            }
        }
    }

    public void removeEmptyBlocks() {
        try {
            int startPos = Integer.MAX_VALUE;
            int endPos = Integer.MIN_VALUE;
            
            MarkBlock blk = chain;
            while (blk != null) {
                if (blk.startMark.getOffset() == blk.endMark.getOffset()) { // empty block
                    if (startPos > blk.startMark.getOffset()) {
                        startPos = blk.startMark.getOffset();
                    }
                    if (endPos < blk.endMark.getOffset()) {
                        endPos = blk.endMark.getOffset();
                    }
                    blk = checkedRemove(blk); // remove current block and get the next one
                } else {
                    blk = blk.next;
                }
            }
            PCS.firePropertyChange(PROP_BLOCKS_CHANGED, startPos, endPos);
        } catch (InvalidMarkException e) {
            Utilities.annotateLoggable(e);
        }
    }

    protected MarkBlock createBlock(int startPos, int endPos)
    throws BadLocationException {
        return new MarkBlock(doc, createBlockStartMark(), createBlockEndMark(),
                             startPos, endPos);
    }

    protected Mark createBlockStartMark() {
        return new Mark();
    }

    protected Mark createBlockEndMark() {
        return new Mark();
    }
    
    private void removeCurrentIfEmpty() {
        try {
            while (currentBlock != null) {
                // For backward bias the following condition may become ">"
                if (currentBlock.startMark.getOffset() >= currentBlock.endMark.getOffset()) {
                    checkedRemove(currentBlock);
                    currentBlock = chain;
                } else { // not empty block
                    break;
                }
            }
        } catch (InvalidMarkException e) {
            Utilities.annotateLoggable(e);
        }
    }


    /** Add non-empty block to the chain of blocks
    * @param concat whether concatenate adjacent blocks
    */
    public synchronized void addBlock(int startPos, int endPos, boolean concat) {
        if (startPos == endPos) {
            return;
        }
        removeCurrentIfEmpty();
        try {
            int rel = compareBlock(startPos, endPos) & MarkBlock.IGNORE_EMPTY;
            if ((rel & MarkBlock.BEFORE) != 0) { // before currentBlock or continue_begin
                if (concat && rel == MarkBlock.CONTINUE_BEGIN) { // concatenate
                    currentBlock.startMark.move(doc, startPos);
                } else { // insert new block at begining
                    boolean first = (currentBlock == chain);
                    MarkBlock blk = currentBlock.insertChain(createBlock(startPos, endPos));
                    if (first) {
                        chain = blk;
                    }
                }
            } else if ((rel & MarkBlock.AFTER) != 0) { // after currentBlock or continue_end
                if (concat && rel == MarkBlock.CONTINUE_END) {
                    currentBlock.endMark.move(doc, endPos);
                } else { // add new block to the chain
                    currentBlock.addChain(createBlock(startPos, endPos));
                }
            } else { // overlap or invalid relation
                if (currentBlock == null) { // no current block
                    chain = createBlock(startPos, endPos);
                } else { // overlap
                    // the block is partly hit - extend it by positions
                    currentBlock.extendStart(startPos);
                    currentBlock.extendEnd(endPos);
                    // remove the blocks covered by startPos to endPos
                    MarkBlock blk = chain;
                    while (blk != null) {
                        if (blk != currentBlock) { // except self
                            if (currentBlock.extend(blk, concat)) { // if they overlapped
                                MarkBlock tempCurBlk = currentBlock;
                                blk = checkedRemove(blk); // will clear currentBlock
                                currentBlock = tempCurBlk;
                            } else { // didn't overlap, go to next
                                blk = blk.next;
                            }
                        } else {
                            blk = blk.next;
                        }
                    }
                }
            }
            PCS.firePropertyChange(PROP_BLOCKS_CHANGED, startPos, endPos);
        } catch (InvalidMarkException e) {
            Utilities.annotateLoggable(e);
        } catch (BadLocationException e) {
            Utilities.annotateLoggable(e);
        }
    }

    /** Remove non-empty block from area covered by blocks from chain */
    public void removeBlock(int startPos, int endPos) {
        if (startPos == endPos) {
            return;
        }
        try {
            synchronized (this) {
            int rel;
            while (((rel = compareBlock(startPos, endPos)) & MarkBlock.OVERLAP) != 0) {
                if ((rel & MarkBlock.THIS_EMPTY) != 0) { // currentBlock is empty
                    checkedRemove(currentBlock);
                } else {
                    switch (currentBlock.shrink(startPos, endPos)) {
                    case MarkBlock.INNER: // tested block inside currentBlock -> divide
                        int endMarkPos = currentBlock.endMark.getOffset();
                        currentBlock.endMark.move(doc, startPos);
                        currentBlock.addChain(createBlock(endPos, endMarkPos));
                        return;
                    case MarkBlock.INSIDE_BEGIN:
                    case MarkBlock.OVERLAP_BEGIN:
                        currentBlock.startMark.move(doc, endPos);
                        return;
                    case MarkBlock.INSIDE_END:
                    case MarkBlock.OVERLAP_END:
                        currentBlock.endMark.move(doc, startPos);
                        return;
                    default: // EXTEND
                        checkedRemove(currentBlock);
                        break;
                    }
                }
            }
            }
            PCS.firePropertyChange(PROP_BLOCKS_CHANGED, startPos, endPos);
        } catch (BadLocationException e) {
            Utilities.annotateLoggable(e);
        } catch (InvalidMarkException e) {
            Utilities.annotateLoggable(e);
        }
    }

    /** Removes mark block and possibly updates the chain.
    * @return next block after removed one
    */
    protected synchronized MarkBlock checkedRemove(MarkBlock blk) {
        boolean first = (blk == chain);
        blk = blk.removeChain();
        if (first) {
            chain = blk;
        }
        currentBlock = null; // make sure current block is cleared
        return blk;
    }
    
    synchronized int adjustToBlockHead(int pos) {
        int rel = compareBlock(pos, pos) & MarkBlock.IGNORE_EMPTY;
        if (rel == MarkBlock.INSIDE_BEGIN || rel == MarkBlock.INNER) {
            pos = currentBlock.getEndOffset();
        }
        return pos;
    }

    public synchronized int adjustToBlockEnd(int pos) {
        int rel = compareBlock(pos, pos) & MarkBlock.IGNORE_EMPTY;
        if (rel == MarkBlock.INSIDE_BEGIN || rel == MarkBlock.INNER) { // inside blk
            pos = currentBlock.getEndOffset();
        }
        return pos;
    }
    
    synchronized int adjustToPrevBlockEnd(int pos) {
        int rel = compareBlock(pos, pos) & MarkBlock.IGNORE_EMPTY;
        if ((rel & MarkBlock.AFTER) != 0) {
            pos = currentBlock.getEndOffset();
        } else {
            if (currentBlock != null) {
                MarkBlock prevBlk = currentBlock.getPrev();
                if (prevBlk != null) {
                    pos = prevBlk.getEndOffset();
                } else {
                    pos = -1;
                }
            } else {
                pos = -1;
            }
        }
        return pos;
    }

    /** Return the position adjusted to the start of the next mark-block.
    */
    public synchronized int adjustToNextBlockStart(int pos) {
        // !!! what about empty blocks
        int rel = compareBlock(pos, pos) & MarkBlock.IGNORE_EMPTY;
        if ((rel & MarkBlock.BEFORE) != 0) {
            pos = currentBlock.getStartOffset();
        } else { // after the block or inside
            if (currentBlock != null) {
                MarkBlock nextBlk = currentBlock.getNext();
                if (nextBlk != null) {
                    pos = nextBlk.getStartOffset();
                } else { // no next block
                    pos = -1;
                }
            } else { // no current block
                pos = -1;
            }
        }
        return pos;
    }

    public synchronized @Override String toString() {
        return "MarkBlockChain: currentBlock=" + currentBlock + "\nblock chain: " // NOI18N
               + (chain != null ? ("\n" + chain.toStringChain()) : " Empty"); // NOI18N
    }

}
