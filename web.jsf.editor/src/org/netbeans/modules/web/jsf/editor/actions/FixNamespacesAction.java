/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.editor.BaseAction;
import static org.netbeans.editor.BaseAction.MAGIC_POSITION_RESET;
import static org.netbeans.editor.BaseAction.UNDO_MERGE_RESET;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.jsf.editor.actions.ImportData.VariantItem;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * Invokes fixing of namespaces for the current Facelet file.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@NbBundle.Messages({
    "FixNamespacesLabel=Fix Namespaces...",
    "FixNamespacesLabelLongName=Fix namespaces in the current file"
})
@EditorActionRegistration(
        name = FixNamespacesAction.ACTION_NAME,
        mimeType = HtmlKit.HTML_MIME_TYPE,
        shortDescription = "#FixNamespacesLabelLongName",
        popupText = "#FixNamespacesLabel")
public class FixNamespacesAction extends BaseAction {

    private static final long serialVersionUID = 1L;
    private static final RequestProcessor RP = new RequestProcessor(FixNamespacesAction.class);
    private static final String PREFERENCES_NODE_KEY = FixNamespacesAction.class.getName();
    private static final String KEY_REMOVE_UNUSED_NS = "remove.unused.namespaces"; //NOI18N
    private static final boolean REMOVE_UNUSED_NS_DEFAULT = true;

    static final String ACTION_NAME = "fix-imports"; //NOI18N

    public FixNamespacesAction() {
        super(MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
    }

    @Override
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        if (target != null) {
            final AtomicBoolean cancel = new AtomicBoolean();
            final AtomicReference<ImportData> importData = new AtomicReference<>();
            final UserTask task = new UserTask() {
                @Override
                public void run(ResultIterator ri) throws Exception {
                    Parser.Result parserResult = getHtmlParserResult(ri);
                    if (parserResult instanceof HtmlParserResult) {
                        HtmlParserResult htmlParserResult = (HtmlParserResult) parserResult;
                        if (cancel.get()) {
                            return;
                        }

                        final ImportData data = computeNamespaces(htmlParserResult);
                        if (cancel.get()) {
                            return;
                        }
                        if (data.shouldShowNamespacesPanel) {
                            if (!cancel.get()) {
                                importData.set(data);
                            }
                        } else {
                            performFixNamespaces(htmlParserResult, data, data.getDefaultVariants(), isRemoveUnusedNs());
                        }
                    }
                }
            };

            ProgressUtils.runOffEventDispatchThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ParserManager.parse(Collections.singleton(Source.create(target.getDocument())), task);
                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }, Bundle.FixNamespacesLabelLongName(), cancel, false);

            if (importData.get() != null && !cancel.get()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        showFixNamespacesDialog(target, importData.get());
                    }
                });
            }
        }
    }

    private static Parser.Result getHtmlParserResult(ResultIterator ri) throws ParseException {
        ResultIterator resultIterator = WebUtils.getResultIterator(ri, "text/html");
        if (resultIterator == null) {
            return null;
        } else {
            return resultIterator.getParserResult();
        }
    }

    private static void performFixNamespaces(
            final HtmlParserResult parserResult,
            final ImportData importData,
            final List<VariantItem> selections,
            final boolean removeUnused) {
        new FixNamespacesPerformer(parserResult, importData, selections, removeUnused).perform();
    }

    private ImportData computeNamespaces(HtmlParserResult parserResult) {
        NamespaceProcessor namespaceProcessor = new NamespaceProcessor(parserResult);
        return namespaceProcessor.computeImportData();
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(FixNamespacesAction.class).node(PREFERENCES_NODE_KEY);
    }

    private static boolean isRemoveUnusedNs() {
        return getPreferences().getBoolean(KEY_REMOVE_UNUSED_NS, REMOVE_UNUSED_NS_DEFAULT);
    }

    private static void setRemoveUnusedNs(final boolean removeUnusedUses) {
        getPreferences().putBoolean(KEY_REMOVE_UNUSED_NS, removeUnusedUses);
    }

    @NbBundle.Messages({
        "LBL_Ok=Ok",
        "LBL_Cancel=Cancel"
    })
    private static void showFixNamespacesDialog(final JTextComponent target, final ImportData importData) {
        final FixDuplicateImportStmts panel = new FixDuplicateImportStmts();
        panel.initPanel(importData, isRemoveUnusedNs());
        final JButton ok = new JButton(Bundle.LBL_Ok());
        final JButton cancel = new JButton(Bundle.LBL_Cancel());
        final AtomicBoolean stop = new AtomicBoolean();
        DialogDescriptor dd = new DialogDescriptor(panel, Bundle.FixNamespacesLabelLongName(), true, new Object[]{ok, cancel}, ok, DialogDescriptor.DEFAULT_ALIGN, HelpCtx.DEFAULT_HELP, null, true);
        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ok.setEnabled(false);
                final List<VariantItem> selections = panel.getSelections();
                final boolean removeUnusedNamespaces = panel.getRemoveUnusedNamespaces();
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ParserManager.parse(Collections.singleton(Source.create(target.getDocument())), new UserTask() {
                                @Override
                                public void run(ResultIterator resultIterator) throws Exception {
                                    Parser.Result parserResult = getHtmlParserResult(resultIterator);
                                    if (parserResult instanceof HtmlParserResult) {
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
                                        performFixNamespaces((HtmlParserResult) parserResult, importData, selections, removeUnusedNamespaces);
                                    }
                                }
                            });
                        } catch (ParseException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        setRemoveUnusedNs(removeUnusedNamespaces);
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

}
