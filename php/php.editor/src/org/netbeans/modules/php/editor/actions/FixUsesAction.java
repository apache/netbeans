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
package org.netbeans.modules.php.editor.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.editor.MainMenuAction;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.actions.ImportData.ItemVariant;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.indent.CodeStyle;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@Messages({
    "FixUsesLabel=Fix Imports...",
    "LongName=Fix Imports in Current Namespace"
})
@EditorActionRegistration(
    name = FixUsesAction.ACTION_NAME,
    mimeType = FileUtils.PHP_MIME_TYPE,
    shortDescription = "Fixes use statements.",
    popupText = "#FixUsesLabel"
)
public class FixUsesAction extends BaseAction {

    static final String ACTION_NAME = "fix-uses"; //NOI18N
    private static final String PREFERENCES_NODE_KEY = FixUsesAction.class.getName();
    private static final String KEY_REMOVE_UNUSED_USES = "remove.unused.uses"; //NOI18N
    private static final boolean REMOVE_UNUSED_USES_DEFAULT = true;
    private static final long serialVersionUID = -8544670573081125944L;

    public FixUsesAction() {
        super(MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
    }

    @Override
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        if (target != null) {
            final int caretPosition = target.getCaretPosition();
            final AtomicBoolean cancel = new AtomicBoolean();
            final AtomicReference<ImportData> importData = new AtomicReference<>();
            final UserTask task = new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Result parserResult = resultIterator.getParserResult();
                    if (parserResult instanceof PHPParseResult) {
                        if (cancel.get()) {
                            return;
                        }

                        final ImportData data = computeUses((PHPParseResult) parserResult, caretPosition);

                        if (cancel.get()) {
                            return;
                        }
                        if (data.shouldShowUsesPanel) {
                            if (!cancel.get()) {
                                importData.set(data);
                            }
                        } else {
                            performFixUses((PHPParseResult) parserResult, data, data.getDefaultVariants(), isRemoveUnusedUses());
                        }
                    }
                }
            };

            BaseProgressUtils.runOffEventDispatchThread(new Runnable() {

                @Override
                public void run() {
                    try {
                        ParserManager.parse(Collections.singleton(Source.create(target.getDocument())), task);
                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }, Bundle.LongName(), cancel, false);

            if (importData.get() != null && !cancel.get()) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        showFixUsesDialog(target, importData.get());
                    }
                });
            }
        }
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(FixUsesAction.class).node(PREFERENCES_NODE_KEY);
    }

    private static boolean isRemoveUnusedUses() {
        return getPreferences().getBoolean(KEY_REMOVE_UNUSED_USES, REMOVE_UNUSED_USES_DEFAULT);
    }

    private static void setRemoveUnusedUses(final boolean removeUnusedUses) {
        getPreferences().putBoolean(KEY_REMOVE_UNUSED_USES, removeUnusedUses);
    }

    private static ImportData computeUses(final PHPParseResult parserResult, final int caretPosition) {
        Map<String, List<UsedNamespaceName>> filteredExistingNames = new UsedNamesCollector(parserResult, caretPosition).collectNames();
        Index index = parserResult.getModel().getIndexScope().getIndex();
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(parserResult, caretPosition);
        assert namespaceScope != null;
        ImportData importData = new ImportDataCreator(filteredExistingNames, index, namespaceScope.getNamespaceName(), createOptions(parserResult)).create();
        importData.caretPosition = caretPosition;
        return importData;
    }

    private static void performFixUses(
            final PHPParseResult parserResult,
            final ImportData importData,
            final List<ImportData.ItemVariant> selections,
            final boolean removeUnusedUses) {
        new FixUsesPerformer(parserResult, importData, selections, removeUnusedUses, createOptions(parserResult)).perform();
    }

    private static Options createOptions(final PHPParseResult parserResult) {
        Document document = parserResult.getSnapshot().getSource().getDocument(false);
        CodeStyle codeStyle = CodeStyle.get(document);
        return new Options(codeStyle, parserResult.getModel().getFileScope().getFileObject());
    }

    private static final RequestProcessor WORKER = new RequestProcessor(FixUsesAction.class.getName(), 1);

    @Messages({
        "LBL_Ok=Ok",
        "LBL_Cancel=Cancel"
    })
    private static void showFixUsesDialog(final JTextComponent target, final ImportData importData) {
    final FixDuplicateImportStmts panel = new FixDuplicateImportStmts();
        panel.initPanel(importData, isRemoveUnusedUses());
        final JButton ok = new JButton(Bundle.LBL_Ok());
        final JButton cancel = new JButton(Bundle.LBL_Cancel());
        final AtomicBoolean stop = new AtomicBoolean();
        DialogDescriptor dd = new DialogDescriptor(panel, Bundle.LongName(), true, new Object[] {ok, cancel}, ok, DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP, new ActionListener() {
                                          @Override
                                          public void actionPerformed(ActionEvent e) {
                                          }
                                      }, true);
        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ok.setEnabled(false);
                final List<ItemVariant> selections = panel.getSelections();
                final boolean removeUnusedUses = panel.getRemoveUnusedImports();
                WORKER.post(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            ParserManager.parse(Collections.singleton(Source.create(target.getDocument())), new UserTask() {

                                @Override
                                public void run(ResultIterator resultIterator) throws Exception {
                                    Result parserResult = resultIterator.getParserResult();
                                    if (parserResult instanceof PHPParseResult) {
                                        SwingUtilities.invokeLater(new Runnable() {

                                            @Override
                                            public void run() {
                                                cancel.setEnabled(false);
                                                ((JDialog) d).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                                            }
                                        });
                                        if (stop.get()) {
                                            return;
                                        }
                                        performFixUses((PHPParseResult) parserResult, importData, selections, removeUnusedUses);
                                    }
                                }
                            });
                        } catch (ParseException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        setRemoveUnusedUses(removeUnusedUses);
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                d.setVisible(false);
                            }
                        });
                    }
                });
            }
        });

        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stop.set(true);
                d.setVisible(false);
            }
        });

        d.setVisible(true);

        d.dispose();
    }

    public static final class GlobalAction extends MainMenuAction {
        public GlobalAction() {
            super();
            setMenu();
        }

        @Override
        protected String getMenuItemText() {
            return Bundle.FixUsesLabel();
        }

        @Override
        protected String getActionName() {
            return ACTION_NAME;
        }
    }

    public static class Options {

        private final boolean preferFullyQualifiedNames;
        private final boolean preferMultipleUseStatementsCombined;
        private final boolean preferGroupUses;
        private final boolean startUseWithNamespaceSeparator;
        private final boolean aliasesCapitalsOfNamespaces;
        private final boolean putInPSR12Order;
        private final boolean keepExistingUseTypeOrder;
        private final int blankLinesBetweenUseTypes;
        private final PhpVersion phpVersion;

        public static class Builder {

            private boolean preferFullyQualifiedNames = false;
            private boolean preferMultipleUseStatementsCombined = false;
            private boolean preferGroupUses = false;
            private boolean startUseWithNamespaceSeparator = false;
            private boolean aliasesCapitalsOfNamespaces = false;
            private boolean putInPSR12Order = false;
            private boolean keepExistingUseTypeOrder = false;
            private int blankLinesBetweenUseTypes = 0;
            private final PhpVersion phpVersion;

            public Builder(PhpVersion phpVersion) {
                this.phpVersion = phpVersion;
            }

            public Builder preferFullyQualifiedNames(boolean preferFullyQualifiedNames) {
                this.preferFullyQualifiedNames = preferFullyQualifiedNames;
                return this;
            }

            public Builder preferMultipleUseStatementsCombined(boolean preferMultipleUseStatementsCombined) {
                this.preferMultipleUseStatementsCombined = preferMultipleUseStatementsCombined;
                return this;
            }

            public Builder preferGroupUses(boolean preferGroupUses) {
                this.preferGroupUses = preferGroupUses;
                return this;
            }

            public Builder startUseWithNamespaceSeparator(boolean startUseWithNamespaceSeparator) {
                this.startUseWithNamespaceSeparator = startUseWithNamespaceSeparator;
                return this;
            }

            public Builder aliasesCapitalsOfNamespaces(boolean aliasesCapitalsOfNamespaces) {
                this.aliasesCapitalsOfNamespaces = aliasesCapitalsOfNamespaces;
                return this;
            }

            public Builder putInPSR12Order(boolean putInPSR12Order) {
                this.putInPSR12Order = putInPSR12Order;
                return this;
            }

            public Builder keepExistingUseTypeOrder(boolean keepExistingUseTypeOrder) {
                this.keepExistingUseTypeOrder = keepExistingUseTypeOrder;
                return this;
            }

            public Builder setBlankLinesBetweenUseTypes(int blankLinesBetweenUseTypes) {
                this.blankLinesBetweenUseTypes = blankLinesBetweenUseTypes;
                return this;
            }

            public Options build() {
                return new Options(this);
            }
        }

        private Options(Builder builder) {
            this.preferFullyQualifiedNames = builder.preferFullyQualifiedNames;
            this.preferMultipleUseStatementsCombined = builder.preferMultipleUseStatementsCombined;
            this.preferGroupUses = builder.preferGroupUses;
            this.startUseWithNamespaceSeparator = builder.startUseWithNamespaceSeparator;
            this.aliasesCapitalsOfNamespaces = builder.aliasesCapitalsOfNamespaces;
            this.putInPSR12Order = builder.putInPSR12Order;
            this.keepExistingUseTypeOrder = builder.keepExistingUseTypeOrder;
            this.blankLinesBetweenUseTypes = builder.blankLinesBetweenUseTypes;
            this.phpVersion = builder.phpVersion;
        }

        // for unit tests
        Options(
                boolean preferFullyQualifiedNames,
                boolean preferMultipleUseStatementsCombined,
                boolean preferGroupUses,
                boolean startUseWithNamespaceSeparator,
                boolean aliasesCapitalsOfNamespaces,
                boolean putInPSR12Order,
                boolean keepExistingUseTypeOrder,
                int blankLinesBetweenUseTypes,
                PhpVersion phpVersion) {
            this.preferFullyQualifiedNames = preferFullyQualifiedNames;
            this.preferMultipleUseStatementsCombined = preferMultipleUseStatementsCombined;
            this.preferGroupUses = preferGroupUses;
            this.startUseWithNamespaceSeparator = startUseWithNamespaceSeparator;
            this.aliasesCapitalsOfNamespaces = aliasesCapitalsOfNamespaces;
            this.putInPSR12Order = putInPSR12Order;
            this.keepExistingUseTypeOrder = keepExistingUseTypeOrder;
            this.blankLinesBetweenUseTypes = blankLinesBetweenUseTypes;
            this.phpVersion = phpVersion;
        }

        Options(
                boolean preferFullyQualifiedNames,
                boolean preferMultipleUseStatementsCombined,
                boolean preferGroupUses,
                boolean startUseWithNamespaceSeparator,
                boolean aliasesCapitalsOfNamespaces,
                PhpVersion phpVersion) {
            this(preferFullyQualifiedNames, preferMultipleUseStatementsCombined, preferGroupUses, startUseWithNamespaceSeparator, aliasesCapitalsOfNamespaces, false, false, 0, phpVersion);
        }

        // legacy, for unit tests
        Options(
                boolean preferFullyQualifiedNames,
                boolean preferMultipleUseStatementsCombined,
                boolean startUseWithNamespaceSeparator,
                boolean aliasesCapitalsOfNamespaces,
                boolean isPhp56OrGreater) {
            this(preferFullyQualifiedNames, preferMultipleUseStatementsCombined, false, startUseWithNamespaceSeparator, aliasesCapitalsOfNamespaces, false, false, 0,
                    isPhp56OrGreater ? PhpVersion.PHP_56 : PhpVersion.PHP_5);
        }

        // legacy, for unit tests
        Options(
                boolean preferFullyQualifiedNames,
                boolean preferMultipleUseStatementsCombined,
                boolean preferGroupUses,
                boolean startUseWithNamespaceSeparator,
                boolean aliasesCapitalsOfNamespaces,
                boolean isPhp56OrGreater) {
            this(preferFullyQualifiedNames, preferMultipleUseStatementsCombined, preferGroupUses, startUseWithNamespaceSeparator, aliasesCapitalsOfNamespaces, false, false, 0,
                    isPhp56OrGreater ? PhpVersion.PHP_56 : PhpVersion.PHP_5);
        }

        public Options(CodeStyle codeStyle, FileObject fileObject) {
            this.preferFullyQualifiedNames = codeStyle.preferFullyQualifiedNames();
            this.preferMultipleUseStatementsCombined = codeStyle.preferMultipleUseStatementsCombined();
            this.preferGroupUses = codeStyle.preferGroupUses();
            this.startUseWithNamespaceSeparator = codeStyle.startUseWithNamespaceSeparator();
            this.aliasesCapitalsOfNamespaces = codeStyle.aliasesFromCapitalsOfNamespaces();
            this.putInPSR12Order = codeStyle.putInPSR12Order();
            this.keepExistingUseTypeOrder = codeStyle.usesKeepExistingTypeOrder();
            this.blankLinesBetweenUseTypes = codeStyle.getBlankLinesBetweenUseTypes();
            this.phpVersion = CodeUtils.getPhpVersion(fileObject);
        }

        public boolean preferFullyQualifiedNames() {
            return preferFullyQualifiedNames;
        }

        public boolean preferMultipleUseStatementsCombined() {
            return preferMultipleUseStatementsCombined;
        }

        public boolean preferGroupUses() {
            return preferGroupUses;
        }

        public boolean startUseWithNamespaceSeparator() {
            return startUseWithNamespaceSeparator;
        }

        public boolean aliasesCapitalsOfNamespaces() {
            return aliasesCapitalsOfNamespaces;
        }

        public boolean putInPSR12Order() {
            return putInPSR12Order;
        }

        public boolean keepExistingUseTypeOrder() {
            return keepExistingUseTypeOrder;
        }

        public int getBlankLinesBetweenUseTypes() {
            return blankLinesBetweenUseTypes;
        }

        public PhpVersion getPhpVersion() {
            return phpVersion;
        }

    }

}
