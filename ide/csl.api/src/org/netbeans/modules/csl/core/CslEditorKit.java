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

package org.netbeans.modules.csl.core;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.TextAction;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.csl.api.CslActions;
import org.netbeans.modules.csl.api.InstantRenameAction;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SelectCodeElementAction;
import org.netbeans.modules.csl.api.ToggleBlockCommentAction;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.api.GoToMarkOccurrencesAction;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author vita
 */
public final class CslEditorKit extends NbEditorKit {

    private static final Logger LOG = Logger.getLogger(CslEditorKit.class.getName());
    
    public static EditorKit createEditorKitInstance(FileObject f) {
        String mimeType = detectMimeType(f);
        return mimeType != null ? new CslEditorKit(mimeType) : null;
    }

    public static org.netbeans.api.lexer.Language createLexerLanguageInstance(FileObject f) {
        String mimeType = detectMimeType(f);
        if (mimeType != null) {
            Language l = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
            if (l != null) {
                return l.getGsfLanguage().getLexerLanguage();
            }
        }
        return null;
    }

    // this is public only because of tests
    public CslEditorKit(String mimeType) {
        this.mimeType = mimeType;
    }
    
    private boolean cloned;

    // -----------------------------------------------------------------------
    // NbEditorKit implementation
    // -----------------------------------------------------------------------
    @Override
    public Object clone() {
        Object o = super.clone();
        ((CslEditorKit)o).cloned = true;
        return o;
    }
    
    public void applyContentType(String mimeType) {
        if (!cloned) {
            return;
        }
        EditorKit ek = MimeLookup.getLookup(mimeType).lookup(EditorKit.class);
        if (ek == null || ek.getClass() != getClass()) {
            return;
        }
        this.mimeType = mimeType;
    }
    

    @Override
    public String getContentType() {
        return mimeType;
    }

    @Override
    public Document createDefaultDocument() {
        return new GsfDocument(mimeType);
    }

    @Override
    public SyntaxSupport createSyntaxSupport(final BaseDocument doc) {
        return new ExtSyntaxSupport(doc) {
            @Override
            public int[] findMatchingBlock(int offset, boolean simpleSearch)
                    throws BadLocationException {
                // Do parenthesis matching, if applicable
                KeystrokeHandler bracketCompletion = UiUtils.getBracketCompletion(doc, offset);
                if (bracketCompletion != null) {
                    OffsetRange range = bracketCompletion.findMatching(getDocument(), offset/*, simpleSearch*/);
                    if (range == OffsetRange.NONE) {
                        return null;
                    } else {
                        return new int[] { range.getStart(), range.getEnd() };
                    }
                }

                return new int[0];
            }
        };
    }

    @Override
    protected void initDocument(BaseDocument doc) {
        // XXX This appears in JavaKit, not sure why, but doing it just in case.
        //do not ask why, fire bug in the IZ:
//        CodeTemplateManager.get(doc);
    }

    @Override
    protected Action[] createActions() {
        Action[] superActions = super.createActions();
        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
        ArrayList<Action> actions = new ArrayList<Action>(30);

        actions.add(new GsfDefaultKeyTypedAction());
        actions.add(new GsfInsertBreakAction());
        actions.add(new GsfDeleteCharAction(deletePrevCharAction, false));
        
        // The php needs to handle special cases of toggle comment. There has to be 
        // registered ToggleBlockCommentAction in PHP that handles these special cases,
        // but the current way, how the actions are registered, doesn't allow to overwrite the action
        // registered here.
        // See issue #204616. This hack can be removed, when issue #204616 will be done.
        if (!mimeType.equals("text/x-php5")) {
            actions.add(new ToggleBlockCommentAction());
        }
        actions.add(new GenerateFoldPopupAction());
        actions.add(new InstantRenameAction());
        actions.add(CslActions.createGoToDeclarationAction());
        actions.add(new GenericGenerateGoToPopupAction());
        actions.add(new SelectCodeElementAction(SelectCodeElementAction.selectNextElementAction, true));
        actions.add(new SelectCodeElementAction(SelectCodeElementAction.selectPreviousElementAction, false));

        if (language == null) {
            LOG.log(Level.WARNING, "Language missing for MIME type {0}", mimeType);
        } else if (language.hasOccurrencesFinder()) {
            actions.add(new GoToMarkOccurrencesAction(false));
            actions.add(new GoToMarkOccurrencesAction(true));
        }

        return TextAction.augmentList(superActions,
            actions.toArray(new Action[0]));
    }

    // -----------------------------------------------------------------------
    // Private implementation
    // -----------------------------------------------------------------------

    private volatile String mimeType;

    private static String detectMimeType(FileObject f) {
        String mimeType = f.getParent().getPath().substring("Editors/".length()); //NOI18N
        return MimePath.validate(mimeType) && mimeType.length() > 0 ? mimeType : null;
    }

    private static Action findAction(Action [] actions, String name) {
        for(Action a : actions) {
            Object nameObj = a.getValue(Action.NAME);
            if (nameObj instanceof String && name.equals(nameObj)) {
                return a;
            }
        }
        return null;
    }

    /**
     * Returns true if bracket completion is enabled in options.
     */
    private static boolean completionSettingEnabled() {
        //return ((Boolean)Settings.getValue(GsfEditorKit.class, JavaSettingsNames.PAIR_CHARACTERS_COMPLETION)).booleanValue();
        return true;
    }

    /**
     * @deprecated use {@link TypedTextInterceptor} instead
     */
    @Deprecated
    private final class GsfDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {
        private JTextComponent currentTarget;
        private String replacedText = null;

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            currentTarget = target;
            super.actionPerformed(evt, target);
            currentTarget = null;
        }

        @Override
        protected void insertString(BaseDocument doc, int dotPos, Caret caret, String str,
            boolean overwrite) throws BadLocationException {
            if (completionSettingEnabled()) {
                KeystrokeHandler bracketCompletion = UiUtils.getBracketCompletion(doc, dotPos);

                if (bracketCompletion != null) {
                    // TODO - check if we're in a comment etc. and if so, do nothing
                    boolean handled =
                        bracketCompletion.beforeCharInserted(doc, dotPos, currentTarget,
                            str.charAt(0));

                    if (!handled) {
                        super.insertString(doc, dotPos, caret, str, overwrite);
                        handled = bracketCompletion.afterCharInserted(doc, dotPos, currentTarget,
                                str.charAt(0));
                    }

                    return;
                }
            }

            super.insertString(doc, dotPos, caret, str, overwrite);
        }

        @Override
        protected void replaceSelection(JTextComponent target, int dotPos, Caret caret,
            String str, boolean overwrite) throws BadLocationException {
            if (str.equals("")) {
                return;
            }
            char insertedChar = str.charAt(0);
            Document document = target.getDocument();

            if (document instanceof BaseDocument) {
                BaseDocument doc = (BaseDocument)document;

                if (completionSettingEnabled()) {
                    KeystrokeHandler bracketCompletion = UiUtils.getBracketCompletion(doc, dotPos);

                    if (bracketCompletion != null) {
                        try {
                            int caretPosition = caret.getDot();

                            boolean handled =
                                bracketCompletion.beforeCharInserted(doc, caretPosition,
                                    target, insertedChar);

                            int p0 = Math.min(caret.getDot(), caret.getMark());
                            int p1 = Math.max(caret.getDot(), caret.getMark());

                            if (p0 != p1) {
                                doc.remove(p0, p1 - p0);
                            }

                            if (!handled) {
                                if ((str != null) && (str.length() > 0)) {
                                    doc.insertString(p0, str, null);
                                }

                                bracketCompletion.afterCharInserted(doc, caret.getDot() - 1,
                                    target, insertedChar);
                            }
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }

                        return;
                    }
                }
            }

            super.replaceSelection(target, dotPos, caret, str, overwrite);
        }
    }

    /**
     * @deprecated use {@link TypedBreakInterceptor} instead
     */
    @Deprecated
    private final class GsfInsertBreakAction extends InsertBreakAction {
        static final long serialVersionUID = -1506173310438326380L;

        @Override
        protected Object beforeBreak(JTextComponent target, BaseDocument doc, Caret caret) {
            if (completionSettingEnabled()) {
                KeystrokeHandler bracketCompletion = UiUtils.getBracketCompletion(doc, caret.getDot());

                if (bracketCompletion != null) {
                    try {
                        int newOffset = bracketCompletion.beforeBreak(doc, caret.getDot(), target);

                        if (newOffset >= 0) {
                            return Integer.valueOf(newOffset);
                        }
                    } catch (BadLocationException ble) {
                        Exceptions.printStackTrace(ble);
                    }
                }
            }

            // return Boolean.TRUE;
            return null;
        }

        @Override
        protected void afterBreak(JTextComponent target, BaseDocument doc, Caret caret,
            Object cookie) {
            if (completionSettingEnabled()) {
                if (cookie != null) {
                    if (cookie instanceof Integer) {
                        // integer
                        int dotPos = ((Integer)cookie).intValue();
                        if (dotPos != -1) {
                            caret.setDot(dotPos);
                        } else {
                            int nowDotPos = caret.getDot();
                            caret.setDot(nowDotPos + 1);
                        }
                    }
                }
            }
        }
    }

    /**
     * @deprecated use {@link DeletedTextInterceptor} instead
     */
    @Deprecated
    private final class GsfDeleteCharAction extends ExtDeleteCharAction {
        private JTextComponent currentTarget;

        public GsfDeleteCharAction(String nm, boolean nextChar) {
            super(nm, nextChar);
        }

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            currentTarget = target;
            super.actionPerformed(evt, target);
            currentTarget = null;
        }

        @Override
        protected void charBackspaced(BaseDocument doc, int dotPos, Caret caret, char ch)
            throws BadLocationException {
            if (completionSettingEnabled()) {
                KeystrokeHandler bracketCompletion = UiUtils.getBracketCompletion(doc, dotPos);

                if (bracketCompletion != null) {
                    boolean success = bracketCompletion.charBackspaced(doc, dotPos, currentTarget, ch);
                    return;
                }
            }
            super.charBackspaced(doc, dotPos, caret, ch);
        }
    }

    private final class GenericGenerateGoToPopupAction extends NbGenerateGoToPopupAction {
        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        }

        private void addAcceleretors(Action a, JMenuItem item, JTextComponent target) {
            // Try to get the accelerator
            Keymap km = target.getKeymap();

            if (km != null) {
                KeyStroke[] keys = km.getKeyStrokesForAction(a);

                if ((keys != null) && (keys.length > 0)) {
                    item.setAccelerator(keys[0]);
                } else if (a != null) {
                    KeyStroke ks = (KeyStroke)a.getValue(Action.ACCELERATOR_KEY);

                    if (ks != null) {
                        item.setAccelerator(ks);
                    }
                }
            }
        }

        private void addAction(JTextComponent target, JMenu menu, Action a) {
            if (a != null) {
                String actionName = (String)a.getValue(Action.NAME);
                JMenuItem item = null;

                if (a instanceof BaseAction) {
                    item = ((BaseAction)a).getPopupMenuItem(target);
                }

                if (item == null) {
                    // gets trimmed text that doesn' contain "go to"
                    String itemText = (String)a.getValue(ExtKit.TRIMMED_TEXT);

                    if (itemText == null) {
                        itemText = getItemText(target, actionName, a);
                    }

                    if (itemText != null) {
                        item = new JMenuItem(itemText);
                        Mnemonics.setLocalizedText(item, itemText);
                        item.addActionListener(a);
                        addAcceleretors(a, item, target);
                        item.setEnabled(a.isEnabled());

                        Object helpID = a.getValue("helpID"); // NOI18N

                        if (helpID instanceof String) {
                            item.putClientProperty("HelpID", helpID); // NOI18N
                        }
                    } else {
                        if (ExtKit.gotoSourceAction.equals(actionName)) {
                            item = new JMenuItem(NbBundle.getBundle(CslEditorKit.class)
                                                         .getString("goto_source_open_source_not_formatted")); //NOI18N
                            addAcceleretors(a, item, target);
                            item.setEnabled(false);
                        }
                    }
                }

                if (item != null) {
                    menu.add(item);
                }
            }
        }

        private void addAction(JTextComponent target, JMenu menu, String actionName) {
            BaseKit kit = Utilities.getKit(target);

            if (kit == null) {
                return;
            }

            Action a = kit.getActionByName(actionName);

            if (a != null) {
                addAction(target, menu, a);
            } else { // action-name is null, add the separator
                menu.addSeparator();
            }
        }

        private String getItemText(JTextComponent target, String actionName, Action a) {
            String itemText;

            if (a instanceof BaseAction) {
                itemText = ((BaseAction)a).getPopupMenuText(target);
            } else {
                itemText = actionName;
            }

            return itemText;
        }

        @Override
        public JMenuItem getPopupMenuItem(final JTextComponent target) {
            String menuText =
                NbBundle.getBundle(CslEditorKit.class).getString("generate-goto-popup"); //NOI18N
            JMenu jm = new JMenu(menuText);
            //addAction(target, jm, ExtKit.gotoSourceAction);
            addAction(target, jm, ExtKit.gotoDeclarationAction);
            //addAction(target, jm, gotoSuperImplementationAction);
            //addAction(target, jm, ExtKit.gotoAction);
            return jm;
        }
    }


}
