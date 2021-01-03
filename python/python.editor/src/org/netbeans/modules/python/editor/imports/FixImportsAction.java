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
package org.netbeans.modules.python.editor.imports;

import org.netbeans.modules.python.source.ImportEntry;
import org.netbeans.modules.python.source.ImportManager;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.PythonParserResult;
import org.netbeans.modules.python.source.CodeStyle.ImportCleanupStyle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Handle imports
 *
 * @todo Sort import choices to the top
 * @todo Pick defaults
 * @todo Update the import model
 * @todo Sort the imports
 * @todo Clean up whitespace
 * @todo Combine import statements
 * @todo Find class references somehow
 * @todo Make remove unused into a combo where you can choose whether to comment out
 *   or remove
 * @todo Make the unused computation properly remove "import as " and "import from" clauses
 *   where the particular name isn't used.
 * @todo Compute the normal block of imports (located at the beginning of Module, possibly
 *   following a Str. Compute all the imports included in it. These should be removed
 *   and replaced by a completely sorted section.
 * @todo Worry about non-top-level modules (right now I only use the basename, which
 *   isn't right)
 * @todo When import-rewriting make sure I split imports as necessary - or perhaps not?
 * @todo Remove duplicate imports and remove froms when included by a whole packge import
 *   unless it's used as a rename!
 *
 */
public class FixImportsAction extends BaseAction {
    private static final String PREFS_KEY = FixImportsAction.class.getName();
    // TODO - use document style instead!
    private static final String KEY_REMOVE_UNUSED_IMPORTS = "removeUnusedImports"; // NOI18N
    
    public FixImportsAction() {
        super("fix-imports", 0); // NOI18N
    }

    @Override
    public Class getShortDescriptionBundleClass() {
        return FixImportsAction.class;
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target.getCaret() == null) {
            return;
        }

        FileObject fo = GsfUtilities.findFileObject(target);
        BaseDocument doc = (BaseDocument)target.getDocument();

        if (fo != null) {
            // Cleanup import section: Remove newlines
            // Sort imports alphabetically
            // Split multi-imports into single splits
            // Look for missing imports: Take ALL calls,
            // and ensure we have imports for all of them.
            // (This means I need to have a complete index of all the builtins)
            // Combine multiple imports (from X import A,, from X import B,  etc. into single list)
            // Move imports that I think may be unused to the end - or just comment them out?

            // For imports: Gather imports from everywhere... move others into the same section
            PythonParserResult info = null;

            Source model = Source.create(fo);
            if (model != null) {
                final PythonParserResult[] infoHolder = new PythonParserResult[1];
                try {
                    ParserManager.parse(Collections.singleton(model), new UserTask() {

                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            infoHolder[0] = (PythonParserResult) resultIterator.getParserResult();
                        }
                    });
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
                info = infoHolder[0];
            }
            if (info != null && PythonAstUtils.getRoot(info) != null) {
                boolean shouldShowImportsPanel = false;

                boolean fixImports = false;
                String[] selections = null;
                boolean removeUnusedImports;
                Preferences prefs = NbPreferences.forModule(FixImportsAction.class).node(PREFS_KEY);

                List<String> ambiguousSymbols = new ArrayList<>();
                Set<ImportEntry> unused = new HashSet<>();
                Set<ImportEntry> duplicates = new HashSet<>();
                Map<String, String> defaultLists = new HashMap<>();
                Map<String, List<String>> alternatives = new HashMap<>();

                ImportManager manager = new ImportManager(info, doc);
                boolean ambiguous = manager.computeImports(ambiguousSymbols, defaultLists, alternatives, unused, duplicates);
                if (ambiguousSymbols.size() > 0) {
                    int size = ambiguousSymbols.size();

                    String[] names = new String[size];
                    String[][] variants = new String[size][];
                    Icon[][] icons = new Icon[size][];
                    String[] defaults = new String[size];

                    int index = 0;
                    for (String name : ambiguousSymbols) {
                        names[index] = name;
                        List<String> list = alternatives.get(name);
                        if (list != null && list.size() > 0) {
                            variants[index] = list.toArray(new String[list.size()]);
                            String deflt = defaultLists.get(name);
                            if (deflt == null) {
                                deflt = list.get(0);
                            }
                            defaults[index] = deflt;
                        } else {
                            variants[index] = new String[1];
                            variants[index][0] = NbBundle.getMessage(FixImportsAction.class, "FixDupImportStmts_CannotResolve"); //NOI18N
                            defaults[index] = variants[index][0];
                            icons[index] = new Icon[1];
                            icons[index][0] = ImageUtilities.loadImageIcon("org/netbeans/modules/python/editor/imports/error-glyph.gif", false);//NOI18N
                        }

                        index++;
                    }
                    assert index == names.length;

                    shouldShowImportsPanel = ambiguous;
                    if (shouldShowImportsPanel) {
                        FixDuplicateImportStmts panel = new FixDuplicateImportStmts();

                        panel.initPanel(names, variants, icons, defaults,
                                prefs.getBoolean(KEY_REMOVE_UNUSED_IMPORTS, true));

                        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(FixImportsAction.class, "FixDupImportStmts_Title")); //NOI18N
                        Dialog d = DialogDisplayer.getDefault().createDialog(dd);

                        d.setVisible(true);
                        d.setVisible(false);
                        d.dispose();
                        fixImports = dd.getValue() == DialogDescriptor.OK_OPTION;
                        selections = panel.getSelections();
                        removeUnusedImports = panel.getRemoveUnusedImports();

                        boolean haveUnresolved = false;
                        for (String selection : selections) {
                            if (selection != null && selection.startsWith("<html>")) { // NOI18N
                                haveUnresolved = true;
                                break;
                            }
                        }

                        // Don't try to remove unused imports if we have unresolved imports - they
                        // could be providing symbols for our unresolved calls/classes somehow
                        if (haveUnresolved) {
                            unused = Collections.emptySet();
                        }
                    } else {
                        fixImports = true;
                        selections = defaults;
                        removeUnusedImports = prefs.getBoolean(KEY_REMOVE_UNUSED_IMPORTS, true);
                    }
                } else {
                    removeUnusedImports = prefs.getBoolean(KEY_REMOVE_UNUSED_IMPORTS, true);
                    // Just clean up imports
                    fixImports = true;
                    selections = null;
                }

                if (fixImports) {
                    if (shouldShowImportsPanel) {
                        prefs.putBoolean(KEY_REMOVE_UNUSED_IMPORTS, removeUnusedImports);
                    }

                    if (!removeUnusedImports) {
                        unused = Collections.emptySet();
                    } else {
                        manager.setCleanup(ImportCleanupStyle.DELETE);
                    }

                    boolean someImportsWereRemoved = unused.size() > 0;

                    manager.apply(null, selections, unused, duplicates);
                    boolean nothingToImport = ambiguousSymbols.size() == 0;

                    if (!shouldShowImportsPanel) {
                        String statusText;
                        if (nothingToImport && !someImportsWereRemoved) {
                            Toolkit.getDefaultToolkit().beep();
                            statusText = NbBundle.getMessage(FixImportsAction.class, "MSG_NothingToFix"); //NOI18N
                        } else if (nothingToImport && someImportsWereRemoved) {
                            statusText = NbBundle.getMessage(FixImportsAction.class, "MSG_UnusedImportsRemoved"); //NOI18N
                        } else {
                            statusText = NbBundle.getMessage(FixImportsAction.class, "MSG_ImportsFixed"); //NOI18N
                        }
                        StatusDisplayer.getDefault().setStatusText(statusText);
                    }

                }

            } else {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(FixImportsAction.class, "MSG_CannotFixImports"));
            }
        }
    }
}
