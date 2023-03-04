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
package org.netbeans.modules.editor.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.lib.editor.util.swing.BlockCompare;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.PositionRegion;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.editor.lib2.document.DocumentInternalUtils;
import org.netbeans.modules.editor.lib2.document.ModRootElement;
import org.netbeans.spi.editor.document.OnSaveTask;
import org.openide.util.Exceptions;

/**
 * Task doing reformatting done at save.
 *
 * @author Miloslav Metelka
 */
public class ReformatBeforeSaveTask implements OnSaveTask {

    // -J-Dorg.netbeans.modules.editor.impl.ReformatAtSaveTask.level=FINE
    private static final Logger LOG = Logger.getLogger(ReformatBeforeSaveTask.class.getName());

    private final Document doc;

    private Reformat reformat;

    private boolean modifiedLinesOnly;

    private List<PositionRegion> guardedBlocks;

    private int guardedBlockIndex;

    private Position guardedBlockStartPos;

    private Position guardedBlockEndPos;
    
    private AtomicBoolean canceled = new AtomicBoolean();

    ReformatBeforeSaveTask(Document doc) {
        this.doc = doc;
    }

    @Override
    public void performTask() {
        if (reformat != null) {
            reformat();
        }
    }

    @Override
    public void runLocked(Runnable run) {
        Preferences prefs = MimeLookup.getLookup(DocumentUtilities.getMimeType(doc)).lookup(Preferences.class);
        if (prefs.getBoolean(SimpleValueNames.ON_SAVE_USE_GLOBAL_SETTINGS, Boolean.TRUE)) {
            prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        }
        String policy = prefs.get(SimpleValueNames.ON_SAVE_REFORMAT, "never"); //NOI18N
        if (!"never".equals(policy)) { //NOI18N
            modifiedLinesOnly = "modified-lines".equals(policy);
            reformat = Reformat.get(doc);
            reformat.lock();
            try {
                run.run();
            } finally {
                reformat.unlock();
            }
        } else {
            run.run();
        }
    }

    @Override
    public boolean cancel() {
        canceled.set(true);
        return true;
    }
    
    void reformat() {
        ModRootElement modRootElement = ModRootElement.get(doc);
        if (modRootElement != null) {
            boolean origEnabled = modRootElement.isEnabled();
            modRootElement.setEnabled(false);
            try {
                // Read all guarded blocks
                guardedBlocks = new GapList<PositionRegion>();
                if (doc instanceof GuardedDocument) {
                    MarkBlock block = ((GuardedDocument) doc).getGuardedBlockChain().getChain();
                    while (block != null) {
                        try {
                            guardedBlocks.add(new PositionRegion(doc, block.getStartOffset(), block.getEndOffset()));
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        block = block.getNext();
                    }

                }

                guardedBlockIndex = 0;
                fetchNextGuardedBlock();
                Element modRootOrDocElement = (modifiedLinesOnly)
                        ? modRootElement
                        : DocumentInternalUtils.customElement(doc, 0, doc.getLength());
                int modElementCount = modRootOrDocElement.getElementCount();
                LinkedList<PositionRegion> formatBlocks = new LinkedList<PositionRegion>();
                Element rootElement = doc.getDefaultRootElement();
                for (int i = 0; i < modElementCount; i++) {
                    if (canceled.get()) {
                        return;
                    }
                    Element modElement = modRootOrDocElement.getElement(i);
                    boolean modElementFinished;
                    boolean add;
                    int startOffset = modElement.getStartOffset();
                    int modElementEndOffset = modElement.getEndOffset();
                    int lineNumber = rootElement.getElementIndex(startOffset);
                    if (lineNumber >= 0) {
                        Element lineElement = rootElement.getElement(lineNumber);
                        startOffset = lineElement.getStartOffset();
                        if (lineElement.getEndOffset() >= modElementEndOffset) {
                            modElementEndOffset = lineElement.getEndOffset();
                        } else {
                            lineNumber = rootElement.getElementIndex(modElementEndOffset);
                            lineElement = rootElement.getElement(lineNumber);
                            modElementEndOffset = lineElement.getEndOffset();
                        }
                    }
                    int endOffset = modElementEndOffset;
                    do {
                        if (guardedBlockStartPos != null) {
                            int guardedStartOffset = guardedBlockStartPos.getOffset();
                            int guardedEndOffset = guardedBlockEndPos.getOffset();
                            BlockCompare blockCompare = BlockCompare.get(
                                    startOffset,
                                    endOffset,
                                    guardedStartOffset,
                                    guardedEndOffset);
                            if (blockCompare.before()) {
                                add = true;
                                modElementFinished = true;
                            } else if (blockCompare.after()) {
                                fetchNextGuardedBlock();
                                add = false;
                                modElementFinished = false;
                            } else if (blockCompare.equal()) {
                                fetchNextGuardedBlock();
                                add = false;
                                modElementFinished = true;
                            } else if (blockCompare.overlapStart()) {
                                endOffset = guardedStartOffset;
                                add = true;
                                modElementFinished = true;
                            } else if (blockCompare.overlapEnd()) {
                                // Skip part covered by guarded block
                                endOffset = guardedEndOffset;
                                fetchNextGuardedBlock();
                                add = false;
                                modElementFinished = false;
                            } else if (blockCompare.contains()) {
                                if (blockCompare.equalStart()) {
                                    startOffset = guardedEndOffset;
                                    add = true;
                                    modElementFinished = true;
                                } else {
                                    endOffset = guardedStartOffset;
                                    add = true;
                                    modElementFinished = false;
                                }
                            } else if (blockCompare.inside()) {
                                add = false;
                                modElementFinished = true;
                            } else {
                                LOG.info("Unexpected blockCompare=" + blockCompare);
                                add = false;
                                modElementFinished = true;
                            }
                        } else {
                            add = true;
                            modElementFinished = true;
                        }
                        if (add) {
                            try {
                                if (startOffset != endOffset) {
                                    PositionRegion last = formatBlocks.peek();
                                    if (last != null && startOffset <= last.getEndOffset()) {
                                        formatBlocks.remove();
                                        if (LOG.isLoggable(Level.FINE)) {
                                            LOG.fine("Reformat-at-save: remove block=" + last);
                                        }
                                        startOffset = last.getStartOffset();
                                    }
                                    PositionRegion block = new PositionRegion(doc, startOffset, endOffset);
                                    if (LOG.isLoggable(Level.FINE)) {
                                        LOG.fine("Reformat-at-save: add block=" + block);
                                    }
                                    formatBlocks.addFirst(block);
                                }
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        startOffset = endOffset;
                        endOffset = modElementEndOffset;
                    } while (!modElementFinished);
                }

                try {
                    for (PositionRegion block : formatBlocks) {
                        if (canceled.get()) {
                            return;
                        }
                        reformat.reformat(block.getStartOffset(), block.getEndOffset());
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }

            } finally {
                modRootElement.setEnabled(origEnabled);
            }
        }
    }

    private void fetchNextGuardedBlock() {
        if (guardedBlockIndex < guardedBlocks.size()) {
            PositionRegion guardedBlock = guardedBlocks.get(guardedBlockIndex++);
            guardedBlockStartPos = guardedBlock.getStartPosition();
            guardedBlockEndPos = guardedBlock.getEndPosition();
        } else {
            guardedBlockEndPos = guardedBlockStartPos = null;
        }
    }

    @MimeRegistration(mimeType="", service=OnSaveTask.Factory.class, position=500)
    public static final class FactoryImpl implements Factory {

        @Override
        public OnSaveTask createTask(Context context) {
            return new ReformatBeforeSaveTask(context.getDocument());
        }

    }

}
