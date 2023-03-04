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

package org.netbeans.modules.form.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.editor.guards.SimpleSection;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.modules.form.FormDataObject;
import org.netbeans.modules.nbform.FormEditorSupport;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.GuardedBlockHandler;
import org.netbeans.modules.refactoring.spi.GuardedBlockHandlerFactory;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileObject;

/**
 * Used by java refactoring to delegate changes in guarded blocks. Registered
 * in META-INF/services. Creates one GuardedBlockHandlerImpl instance per
 * refactoring (so it can handle more forms).
 * 
 * @author Tomas Pavek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.GuardedBlockHandlerFactory.class)
public class GuardedBlockHandlerFactoryImpl implements GuardedBlockHandlerFactory {
    
    public GuardedBlockHandlerFactoryImpl() {
    }
    
    @Override
    public GuardedBlockHandler createInstance(AbstractRefactoring refactoring) {
        RefactoringInfo refInfo = refactoring.getContext().lookup(RefactoringInfo.class);
        return new GuardedBlockHandlerImpl(refInfo);
    }

    // -----
    
    private static class GuardedBlockHandlerImpl implements GuardedBlockHandler {
        private RefactoringInfo refInfo;
        private Map<FileObject, GuardedBlockUpdate> guardedUpdates;

        private boolean first = true;

        public GuardedBlockHandlerImpl(RefactoringInfo refInfo) {
            this.refInfo = refInfo;
        }

        @Override
        public Problem handleChange(RefactoringElementImplementation proposedChange,
                                    Collection<RefactoringElementImplementation> replacements,
                                    Collection<Transaction> transactions) {
            if (refInfo == null) {
                return null; // unsupported
            }

            FileObject changedFile = proposedChange.getParentFile();
            if (!RefactoringInfo.isJavaFileOfForm(changedFile)) {
                // This guarded block does not belong to form.
                return null;
            }
            FormRefactoringUpdate update = refInfo.getUpdateForFile(changedFile);
            update.setGaurdedCodeChanging(true);

            boolean preloadForm = false;
            boolean canRegenerate = false;

            if (refInfo.containsOriginalFile(changedFile)) {
                // the change started in this form
                switch (refInfo.getChangeType()) {
                case VARIABLE_RENAME: // renaming field or local variable of initComponents
                case CLASS_RENAME: // renaming form class, need to regenarate use of MyForm.this
                case EVENT_HANDLER_RENAME: // renaming event handler - change the method and calls
                    preloadForm = true;
                    canRegenerate = true;
                    break;
                case CLASS_MOVE:
                    // don't preload the form here - it should be loaded and
                    // regenareted *after* moved to the new location
                    if (refInfo.getOriginalFiles().length == 1) {
                        canRegenerate = true;
                    } // otherwise it is very likely the change is caused by moving
                      // some other class used in this form - needs to be replaced
                      // without loading (and regenerating) the form
                }
            } else { // change originated in another class
                if (first) {
                    // add the preview element for the overall guarded block change
                    // (for direct form change it was added by our plugin)
                    replacements.add(update.getPreviewElement());
                    first = false;
                }
                // other changes may render the form unloadable (missing component
                // classes), will change the .form file directly...
            }

            // load the form in advance to be sure it can be loaded
            if (preloadForm && !update.prepareForm(true)) {
                return new Problem(true, "Error loading form. Cannot update generated code.");
            }

            if (!canRegenerate) { // guarded block gets changed but it is not safe to load the form
                // remember the change and modify the guarded block directly later
                ModificationResult.Difference diff = proposedChange.getLookup().lookup(ModificationResult.Difference.class);
                if (diff != null) {
                    GuardedBlockUpdate gbUpdate;
                    if (guardedUpdates == null) {
                        guardedUpdates = new HashMap<FileObject, GuardedBlockUpdate>();
                        gbUpdate = null;
                    } else {
                        gbUpdate = guardedUpdates.get(changedFile);
                    }
                    if (gbUpdate == null) {
                        FormDataObject formDataObject = update.getFormDataObject();
                        FormEditorSupport fes = (FormEditorSupport)formDataObject.getFormEditorSupport();
                        gbUpdate = new GuardedBlockUpdate(fes);
                        guardedUpdates.put(changedFile, gbUpdate);
                    }
                    gbUpdate.addChange(diff);
                    transactions.add(gbUpdate);
                }
            }

            // we must add some transaction or element (even if it can be redundant)
            // so it looks like we care about this guarded block change...
            transactions.add(update);

            return null;
        }
    }

    // -----

    /**
     * A transaction for updating guarded blocks directly with changes that came
     * from java refactoring. I.e. no regenerating by form editor.
     */
    private static class GuardedBlockUpdate implements Transaction {
        private FormEditorSupport formEditorSupport;
        private List<GuardedBlockInfo> guardedInfos; // there can be multiple guarded blocks affected

        GuardedBlockUpdate(FormEditorSupport fes) {
            this.formEditorSupport = fes;
            guardedInfos = new ArrayList<GuardedBlockInfo>(2);
            guardedInfos.add(new GuardedBlockInfo(fes.getInitComponentSection()));
            guardedInfos.add(new GuardedBlockInfo(fes.getVariablesSection()));
        }

        void addChange(ModificationResult.Difference diff) {
            for (GuardedBlockInfo block : guardedInfos) {
                if (block.containsPosition(diff)) {
                    block.addChange(diff);
                    break;
                }
            }
        }

        @Override
        public void commit() {
            for (GuardedBlockInfo block : guardedInfos) {
                String newText = block.getNewSectionText();
                if (newText != null) {
                    formEditorSupport.getGuardedSectionManager()
                        .findSimpleSection(block.getName())
                            .setText(newText);
                }
            }
        }

        @Override
        public void rollback() {
            // rollback not needed - should be reverted by java refactoring as a whole file
/*            for (GuardedBlockInfo block : guardedInfos) {
                formEditorSupport.getGuardedSectionManager()
                    .findSimpleSection(block.getName())
                        .setText(block.originalText);
            } */
        }
    }

    /**
     * Collects all changes for one guarded block.
     */
    private static class GuardedBlockInfo {
        private String blockName;
        private int originalPosition;
        private String originalText;

        /**
         * Represents one change in the guarded block.
         */
        private static class ChangeInfo implements Comparable<ChangeInfo> {
            private int startPos;
            private int length;
            private String newText;
            ChangeInfo(int startPos, int len, String newText) {
                this.startPos = startPos;
                this.length = len;
                this.newText = newText;
            }

            @Override
            public int compareTo(ChangeInfo ch) {
                return startPos - ch.startPos;
            }
        }

        private Set<ChangeInfo> changes = new TreeSet<ChangeInfo>();

        GuardedBlockInfo(SimpleSection section) {
            blockName = section.getName();
            originalPosition = section.getStartPosition().getOffset();
            originalText = section.getText();
        }

        boolean containsPosition(ModificationResult.Difference diff) {
            int pos = diff.getStartPosition().getOffset();
            return pos >= originalPosition && pos < originalPosition + originalText.length();
        }

        void addChange(ModificationResult.Difference diff) {
            changes.add(new ChangeInfo(
                    diff.getStartPosition().getOffset() - originalPosition,
                    diff.getOldText() != null ? diff.getOldText().length() : 0,
                    diff.getNewText()));
        }

        String getName() {
            return blockName;
        }

        String getNewSectionText() {
            if (changes.size() > 0) {
                StringBuilder buf = new StringBuilder();
                int lastOrigPos = 0;
                for (ChangeInfo change : changes) {
                    buf.append(originalText.substring(lastOrigPos, change.startPos));
                    if (change.newText != null) {
                        buf.append(change.newText);
                    }
                    lastOrigPos = change.startPos + change.length;
                }
                buf.append(originalText.substring(lastOrigPos));
                return buf.toString();
            } else {
                return null;
            }
        }
    }

}
