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

package org.netbeans.editor.ext;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.Caret;
import javax.swing.text.Keymap;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.api.editor.NavigationHistory;
import org.netbeans.api.editor.EditorActionNames;
import org.netbeans.api.editor.caret.CaretInfo;
import org.netbeans.api.editor.caret.EditorCaret;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
* Extended kit offering advanced functionality
*
* @author Miloslav Metelka
* @version 1.00
*/
public class ExtKit extends BaseKit {

    /** This action is searched and executed when the popup menu should
    * be displayed to build the popup menu.
    */
    public static final String buildPopupMenuAction = "build-popup-menu"; // NOI18N

    /** Show popup menu.
    */
    public static final String showPopupMenuAction = "show-popup-menu"; // NOI18N

    /** This action is searched and executed when the tool-tip should
    * be displayed by tool-tip support to build the tool-tip.
    */
    public static final String buildToolTipAction = "build-tool-tip"; // NOI18N

    /** Open find dialog action - this action is defined in view package, but
     * its name is defined here for clarity
     * @deprecated Without any replacement.
     */
    @Deprecated
    public static final String findAction = "find"; // NOI18N

    /** Open replace dialog action - this action is defined in view package, but
    * its name is defined here for clarity
    */
    public static final String replaceAction = "replace"; // NOI18N

    /** Open goto dialog action - this action is defined in view package, but
    * its name is defined here for clarity
    */
    public static final String gotoAction = "goto"; // NOI18N

    /** Goto declaration depending on the context under the caret */
    public static final String gotoDeclarationAction = EditorActionNames.gotoDeclaration; // NOI18N

    /** Goto source depending on the context under the caret */
    public static final String gotoSourceAction = "goto-source"; // NOI18N

    public static final String gotoSuperImplementationAction = "goto-super-implementation"; // NOI18N

    /** Goto help depending on the context under the caret */
    public static final String gotoHelpAction = "goto-help"; // NOI18N

    /** Match brace */
    public static final String matchBraceAction = "match-brace"; // NOI18N

    /** Select the text to the matching bracket */
    public static final String selectionMatchBraceAction = "selection-match-brace"; // NOI18N

    /** Toggle the case for the first character of the word under caret */
    public static final String toggleCaseIdentifierBeginAction = "toggle-case-identifier-begin"; // NOI18N

    /** Advanced code selection technique
     * @deprecated this action name is not actively used by ExtKit and will be removed in future releases.
     */
    @Deprecated
    public static final String codeSelectAction = "code-select"; // NOI18N

    /** Action used when escape is pressed. By default it hides popup-menu
     * @deprecated this action name is not actively used by ExtKit and will be removed in future releases.
     */
    @Deprecated
    public static final String escapeAction = "escape"; // NOI18N

    /** Find the completion help and show it in the completion pane. */
    public static final String completionShowAction = "completion-show"; // NOI18N
    public static final String allCompletionShowAction = "all-completion-show"; // NOI18N

    /** Show documentation popup panel */
    public static final String documentationShowAction = "documentation-show"; // NOI18N
    
    /** Show completion tooltip */
    public static final String completionTooltipShowAction = "tooltip-show"; // NOI18N

    /** Comment out the selected block */
    public static final String commentAction = "comment"; // NOI18N

    /** Uncomment the selected block */
    public static final String uncommentAction = "uncomment"; // NOI18N
    
    /** Comment/Uncomment the selected block */
    public static final String toggleCommentAction = "toggle-comment"; // NOI18N
    
    /** Toggle the toolbar */
    public static final String toggleToolbarAction = EditorActionNames.toggleToolbar;
   
    /** Trimmed text for go to submenu*/
    public static final String TRIMMED_TEXT = "trimmed-text";    //NOI18N

    private static final String editorBundleHash = "org.netbeans.editor.Bundle#";

    /** Whether editor popup menu creation should be dumped to console */
    private static final boolean debugPopupMenu
            = Boolean.getBoolean("netbeans.debug.editor.popup.menu"); // NOI18N
    
    public ExtKit() {
    }

    public @Override SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new ExtSyntaxSupport(doc);
    }

// XXX: remove
//    public Completion createCompletion(ExtEditorUI extEditorUI) {
//        return null;
//    }
//    
//    public CompletionJavaDoc createCompletionJavaDoc(ExtEditorUI extEditorUI) {
//        return null;
//    }

    private boolean noExtEditorUIClass = false;
    protected @Override EditorUI createEditorUI() {
        if (!noExtEditorUIClass) {
            try {
                ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
                Class extEditorUIClass = loader.loadClass("org.netbeans.editor.ext.ExtEditorUI"); //NOI18N
                return (EditorUI) extEditorUIClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                noExtEditorUIClass = true;
            }
        }
        return new EditorUI();
    }

    protected @Override Action[] createActions() {
        ArrayList<Action> actions = new ArrayList<Action>();

        actions.add(new ExtDefaultKeyTypedAction());
// XXX: remove
//        if (!ExtCaret.NO_HIGHLIGHT_BRACE_LAYER) {
//            actions.add(new MatchBraceAction(matchBraceAction, false));
//            actions.add(new MatchBraceAction(selectionMatchBraceAction, true));
//        }
        actions.add(new CommentAction()); // to make ctrl-shift-T in Netbeans55 profile work
        actions.add(new UncommentAction()); // to make ctrl-shift-D in Netbeans55 profile work
                
        return TextAction.augmentList(super.createActions(), actions.toArray(new Action[0]));
    }
    
    /**
     * Action that is localized in org.netbeans.editor package.
     * <br>
     * <code>BaseKit.class</code> is used as a bundle class.
     */
    private abstract static class BaseKitLocalizedAction extends BaseAction {

        public BaseKitLocalizedAction() {
            super();
        }

        public BaseKitLocalizedAction(int updateMask) {
            super(updateMask);
        }

        public BaseKitLocalizedAction(String name) {
            super(name);
        }
        
        public BaseKitLocalizedAction(String name, int updateMask) {
            super(name, updateMask);
        }
        
        protected @Override Class getShortDescriptionBundleClass() {
            return BaseKit.class;
        }
        
    }

    /** Called before the popup menu is shown to possibly rebuild
    * the popup menu.
    */
    @EditorActionRegistration(
            name = buildPopupMenuAction,
            shortDescription = editorBundleHash + buildPopupMenuAction,
            noKeyBinding = true
    )
    public static class BuildPopupMenuAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =4257043398248915291L;

        public BuildPopupMenuAction() {
            super(buildPopupMenuAction, NO_RECORDING);
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }
        
        public BuildPopupMenuAction(Map attrs) { // Create action without wrapper (extra properties in constructor)
            this();
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (debugPopupMenu) {
                    /*DEBUG*/System.err.println("POPUP CREATION START <<<<<"); // NOI18N
                }
                JPopupMenu pm = buildPopupMenu(target);
                if (debugPopupMenu) {
                    /*DEBUG*/System.err.println("POPUP CREATION END >>>>>"); // NOI18N
                }
                Utilities.getEditorUI(target).setPopupMenu(pm);
            }
        }
        
        protected JPopupMenu createPopupMenu(JTextComponent target) {
            return new JPopupMenu();
        }

        protected JPopupMenu buildPopupMenu(JTextComponent target) {
            JPopupMenu pm = createPopupMenu(target);

            EditorUI ui = Utilities.getEditorUI(target);
            String settingName = ui == null || ui.hasExtComponent()
                    ? "popup-menu-action-name-list" //NOI18N
                    : "dialog-popup-menu-action-name-list"; //NOI18N
            
            Preferences prefs = MimeLookup.getLookup(DocumentUtilities.getMimeType(target)).lookup(Preferences.class);
            String actionNames = prefs.get(settingName, null);

            if (actionNames != null) {
                for(StringTokenizer t = new StringTokenizer(actionNames, ","); t.hasMoreTokens(); ) {
                    String action = t.nextToken().trim();
                    addAction(target, pm, action);
                }
            }
            
            return pm;
        }

        /** Add the action to the popup menu. This method is called
         * for each action-name found in the action-name-list. It should
         * add the appopriate menu item to the popup menu.
         * @param target target component for which the menu is being
         *  constructed.
         * @param popupMenu popup menu to which this method should add
         *  the item corresponding to the action-name.
         * @param actionName name of the action to add. The real action
         *  can be retrieved from the kit by calling <tt>getActionByName()</tt>.
         */
        protected void addAction(JTextComponent target, JPopupMenu popupMenu,
        String actionName) {
            Action a = Utilities.getKit(target).getActionByName(actionName);
            if (a != null) {
                JMenuItem item = null;
                if (a instanceof BaseAction) {
                    item = ((BaseAction)a).getPopupMenuItem(target);
                }
                if (item == null) {
                    String itemText = getItemText(target, actionName, a);
                    if (itemText != null) {
                        item = new JMenuItem(itemText);
                        item.addActionListener(a);
                        // Try to get the accelerator
                        Keymap km = target.getKeymap();
                        if (km != null) {
                            KeyStroke[] keys = km.getKeyStrokesForAction(a);
                            if (keys != null && keys.length > 0) {
                                item.setAccelerator(keys[0]);
                            }
                        }
                        item.setEnabled(a.isEnabled());
                        Object helpID = a.getValue ("helpID"); // NOI18N
                        if (helpID instanceof String)
                            item.putClientProperty ("HelpID", helpID); // NOI18N
                    }
                }

                if (item != null) {
                    debugPopupMenuItem(item, a);
                    popupMenu.add(item);
                }

            } else if (actionName == null){ // action-name is null, add the separator
                if (popupMenu.getComponentCount()>0){
                    debugPopupMenuItem(null, null);
                    popupMenu.addSeparator();
                }
            }
        }
        
        protected final void debugPopupMenuItem(JMenuItem item, Action action) {
            if (debugPopupMenu) {
                StringBuilder sb = new StringBuilder("POPUP: "); // NOI18N
                if (item != null) {
                    sb.append('"'); //NOI18N
                    sb.append(item.getText());
                    sb.append('"'); //NOI18N
                    if (!item.isVisible()) {
                        sb.append(", INVISIBLE"); // NOI18N
                    }
                    if (action != null) {
                        sb.append(", action="); // NOI18N
                        sb.append(action.getClass().getName());
                    }
                    
                } else { // null item means separator
                    sb.append("--Separator--"); // NOI18N
                }

                /*DEBUG*/System.err.println(sb.toString());
            }

        }

        protected String getItemText(JTextComponent target, String actionName, Action a) {
            String itemText;
            if (a instanceof BaseAction) {
                itemText = ((BaseAction)a).getPopupMenuText(target);
            } else {
                itemText = (String) a.getValue("popupText");
                if (itemText == null) {
                    itemText = (String) a.getValue("menuText");
                    if (itemText == null) {
                        itemText = actionName;
                    }
                }
            }
            return itemText;
        }

    }

    /** Show the popup menu.
    */
    @EditorActionRegistration(
            name = "org.openide.actions.PopupAction",
            shortDescription = editorBundleHash + showPopupMenuAction,
            noKeyBinding = true
    )
    public static class ShowPopupMenuAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =4257043398248915291L;

        public ShowPopupMenuAction() {
            super(NO_RECORDING);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                try {
                    int dotPos = target.getCaret().getDot();
                    Rectangle r = target.getUI().modelToView(target, dotPos);
                    if (r != null) {
                        EditorUI eui = Utilities.getEditorUI(target);
                        if (eui != null) {
                            eui.showPopupMenu(r.x, r.y + r.height);
                        }
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }

    }

    @EditorActionRegistration(
            name = buildToolTipAction,
            shortDescription = editorBundleHash + buildToolTipAction
    )
    public static class BuildToolTipAction extends BaseAction {

        static final long serialVersionUID =-2701131863705941250L;

        public BuildToolTipAction() {
            super(buildToolTipAction, NO_RECORDING);
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }

        public BuildToolTipAction(Map attrs) { // Create action without wrapper (extra properties in constructor)
            this();
        }

        protected String buildText(JTextComponent target) {
            ToolTipSupport tts = Utilities.getEditorUI(target).getToolTipSupport();
            return  (tts != null)
                ? target.getToolTipText(tts.getLastMouseEvent())
                : target.getToolTipText();
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                ToolTipSupport tts = Utilities.getEditorUI(target).getToolTipSupport();
                if (tts != null) {
                    tts.setToolTipText(buildText(target));
                }
            }
        }

    }

    @EditorActionRegistration(
            name = gotoAction,
            shortDescription = editorBundleHash + "goto_trimmed"
    )
    public static class GotoAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =8425585413146373256L;

        public GotoAction() {
            super(gotoAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
            String name = NbBundle.getBundle(BaseKit.class).getString("goto_trimmed");
            putValue(TRIMMED_TEXT, name);
            putValue(POPUP_MENU_TEXT, name);
        }


        /** This method is called by the dialog support
        * to translate the line offset to the document position. This
        * can be changed for example for the diff operations.
        * @param doc document to operate over
        * @param lineOffset the line offset to convert to position
        * @return document offset that corresponds to the row-start
        *  of the line with the line-number equal to (lineOffset + 1).
        */
        protected int getOffsetFromLine(BaseDocument doc, int lineOffset) {
            return Utilities.getRowStartFromLineOffset(doc, lineOffset);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                new GotoDialogSupport().showGotoDialog(new KeyEventBlocker(target, false));
            }
        }

    }

    /**
     * Action to go to the declaration of the variable under the caret.
     * @deprecated this action is no longer used. It is reimplemented in editor.actions module.
     */
    @Deprecated
    public static class GotoDeclarationAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =-6440495023918097760L;

        public GotoDeclarationAction() {
            super(gotoDeclarationAction,
                  ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET
                  | SAVE_POSITION
                 );
            String name = NbBundle.getBundle(BaseKit.class).getString("goto-declaration-trimmed");
            putValue(TRIMMED_TEXT, name);  //NOI18N            
            putValue(POPUP_MENU_TEXT, name);  //NOI18N            
        }

        public boolean gotoDeclaration(JTextComponent target) {
            BaseDocument doc = Utilities.getDocument(target);
            if (doc == null)
                return false;
            try {
                Caret caret = target.getCaret();
                int dotPos = caret.getDot();
                int[] idBlk = Utilities.getIdentifierBlock(doc, dotPos);
                ExtSyntaxSupport extSup = (ExtSyntaxSupport)doc.getSyntaxSupport();
                if (idBlk != null) {
                    int decPos = extSup.findDeclarationPosition(doc.getText(idBlk), idBlk[1]);
                    if (decPos >= 0) {
                        caret.setDot(decPos);
                        return true;
                    }
                }
            } catch (BadLocationException e) {
            }
            return false;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                gotoDeclaration(target); // try to go to the declaration position
            }
        }
    }

    @EditorActionRegistration(
            name = toggleCaseIdentifierBeginAction,
            shortDescription = editorBundleHash + toggleCaseIdentifierBeginAction
    )
    public static class ToggleCaseIdentifierBeginAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =584392193824931979L;

        public ToggleCaseIdentifierBeginAction() {
            super(ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                Caret caret = target.getCaret();
                BaseDocument doc = (BaseDocument) target.getDocument();
                if(caret instanceof EditorCaret) {
                    EditorCaret editorCaret = (EditorCaret) caret;
                    boolean beeped = false;
                    for (CaretInfo caretInfo : editorCaret.getSortedCarets()) {
                        try {
                            int[] idBlk = Utilities.getIdentifierBlock(doc, caretInfo.getDot());
                            if (idBlk != null) {
                                Utilities.changeCase(doc, idBlk[0], 1, Utilities.CASE_SWITCH);
                            }
                        } catch (BadLocationException e) {
                            if(!beeped) {
                                target.getToolkit().beep();
                                beeped = true;
                            }
                        }
                    }
                } else {
                    try {
                        int[] idBlk = Utilities.getIdentifierBlock(doc, caret.getDot());
                        if (idBlk != null) {
                            Utilities.changeCase(doc, idBlk[0], 1, Utilities.CASE_SWITCH);
                        }
                    } catch (BadLocationException e) {
                        target.getToolkit().beep();
                    }
                }
            }
        }
    }

    /**
     * This action does nothing.
     * 
     * @deprecated Please use Braces Matching SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-bracesmatching@/overview-summary.html">Editor Braces Matching</a>.
     */
    @Deprecated
    public static class MatchBraceAction extends BaseKitLocalizedAction {
// XXX: remove
//        boolean select;

        static final long serialVersionUID =-184887499045886231L;

        public MatchBraceAction(String name, boolean select) {
            super(name, 0);
// XXX: remove
//            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
// XXX: remove
//            if (target != null) {
//                try {
//                    Caret caret = target.getCaret();
//                    BaseDocument doc = Utilities.getDocument(target);
//                    int dotPos = caret.getDot();
//                    ExtSyntaxSupport sup = (ExtSyntaxSupport)doc.getSyntaxSupport();
//                    int[] matchBlk = null;
//                    int setDotOffset = 0;
//                    if(caret instanceof ExtCaret) {
//                        int how = ((ExtCaret)caret).getMatchBraceOffset();
//                        if(dotPos > 0 && (how == ExtCaret.MATCH_BRACE_BEFORE
//                                        || how == ExtCaret.MATCH_BRACE_EITHER)) {
//                            matchBlk = sup.findMatchingBlock(dotPos - 1, false);
//                        }
//                        if(matchBlk == null && (how == ExtCaret.MATCH_BRACE_AFTER
//                                    || how == ExtCaret.MATCH_BRACE_EITHER)) {
//                            matchBlk = sup.findMatchingBlock(dotPos, false);
//                            if(how == ExtCaret.MATCH_BRACE_AFTER) {
//                                // back it up so caret is on the match
//                                setDotOffset = -1;
//                            }
//                        }
//                    } else if(dotPos > 0) {
//                        matchBlk = sup.findMatchingBlock(dotPos - 1, false);
//                    }
//                    if (matchBlk != null) {
//                        if (select) {
//                            caret.moveDot(matchBlk[1]);
//                        } else {
//                            caret.setDot(matchBlk[1] + setDotOffset);
//                        }
//                    }
//                } catch (BadLocationException e) {
//                    target.getToolkit().beep();
//                }
//            }
        }
    }

    /**
     * @deprecated this action is deprecated and will be removed in future releases.
     */
    @Deprecated
    public static class CodeSelectAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =4033474080778585860L;

        public CodeSelectAction() {
            super(codeSelectAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
/*            if (target != null) {
                BaseDocument doc = (BaseDocument)target.getDocument();
                SyntaxSupport sup = doc.getSyntaxSupport();
                Caret caret = target.getCaret();
                try {
                    int bracketPos = sup.findUnmatchedBracket(caret.getDot(), sup.getRightBrackets());
                    if (bracketPos >= 0) {
                        caret.setDot(bracketPos);
                        while (true) {
                          int bolPos = Utilities.getRowStart(doc, bracketPos);
                          boolean isWSC = sup.isCommentOrWhitespace(bolPos, bracketPos);
                          if (isWSC) { // get previous line end
                            
                          }
                        }
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
*/
        }
    }

    /** Prefix maker adds the prefix before the identifier under cursor.
    * The prefix is not added if it's already present. The prefix to be
    * added is specified in the constructor of the action together
    * with the prefix group. If there's already any prefix from the prefix
    * group at the begining of the identifier, that prefix is replaced
    * by the actual prefix.
    */
    public static class PrefixMakerAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =-2305157963664484920L;

        private String prefix;

        private String[] prefixGroup;

        public PrefixMakerAction(String name, String prefix, String[] prefixGroup) {
            super(name);
            this.prefix = prefix;
            this.prefixGroup = prefixGroup;
            
            // [PENDING] This should be done in a better way
            String iconRes = null;
            if ("get".equals(prefix)) { // NOI18N
                iconRes = "org/netbeans/modules/editor/resources/var_get.gif"; // NOI18N
            } else if ("set".equals(prefix)) { // NOI18N
                iconRes = "org/netbeans/modules/editor/resources/var_set.gif"; // NOI18N
            } else if ("is".equals(prefix)) { // NOI18N
                iconRes = "org/netbeans/modules/editor/resources/var_is.gif"; // NOI18N
            }
            if (iconRes != null) {
                putValue(BaseAction.ICON_RESOURCE_PROPERTY, iconRes);
            }
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                BaseDocument doc = (BaseDocument)target.getDocument();
                int dotPos = target.getCaret().getDot();
                try {
                    // look for identifier around caret
                    int[] block = org.netbeans.editor.Utilities.getIdentifierBlock(doc, dotPos);

                    // If there is no identifier around, warn user
                    if (block == null) {
                        target.getToolkit().beep();
                        return;
                    }

                    // Get the identifier to operate on
                    CharSequence identifier = DocumentUtilities.getText(doc, block[0], block[1] - block[0]);

                    // Handle the case we already have the work done - e.g. if we got called over 'getValue'
                    if (CharSequenceUtilities.startsWith(identifier, prefix) && 
                            Character.isUpperCase(identifier.charAt(prefix.length()))) return;

                    // Handle the case we have other type of known xEr: eg isRunning -> getRunning
                    for (int i=0; i<prefixGroup.length; i++) {
                        String actPref = prefixGroup[i];
                        if (CharSequenceUtilities.startsWith(identifier, actPref)
                                && identifier.length() > actPref.length()
                                && Character.isUpperCase(identifier.charAt(actPref.length()))
                           ) {
                            doc.remove(block[0], actPref.length());
                            doc.insertString(block[0], prefix, null);
                            return;
                        }
                    }

                    // Upcase the first letter
                    Utilities.changeCase(doc, block[0], 1, Utilities.CASE_UPPER);
                    // Prepend the prefix before it
                    doc.insertString(block[0], prefix, null);
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class CommentAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =-1422954906554289179L;

        private ToggleCommentAction delegateAction;

        private CommentAction() {
            this(null);
        }
        
        public CommentAction(String lineCommentString) {
            super(commentAction);
            this.delegateAction = lineCommentString != null ? new ToggleCommentAction(lineCommentString) : null;
            putValue(BaseAction.ICON_RESOURCE_PROPERTY,
                "org/netbeans/modules/editor/resources/comment.png"); // NOI18N
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            BaseAction action = null;
            
            if (delegateAction != null) {
                action = delegateAction;
            } else {
                BaseKit kit = Utilities.getKit(target);
                Action a = kit == null ? null : kit.getActionByName(toggleCommentAction);
                if (a instanceof BaseAction) {
                    action = (BaseAction) a;
                }
            }

            if (action instanceof ToggleCommentAction) {
                ((ToggleCommentAction) action).commentUncomment(evt, target, Boolean.TRUE);
            } else {
                if (action != null) {
                    action.putValue("force-comment", Boolean.TRUE); // NOI18N
                    try {
                        action.actionPerformed(evt, target);
                    } finally {
                        action.putValue("force-comment", null); // NOI18N
                    }
                } else {
                    target.getToolkit().beep();
                }
            }
        }
    } // End of CommentAction class

    public static class UncommentAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =-7005758666529862034L;

        private ToggleCommentAction delegateAction;

        private UncommentAction() {
            this(null);
        }
        
        public UncommentAction(String lineCommentString) {
            super(uncommentAction);
            this.delegateAction = lineCommentString != null ? new ToggleCommentAction(lineCommentString) : null;
            putValue(BaseAction.ICON_RESOURCE_PROPERTY,
                "org/netbeans/modules/editor/resources/uncomment.png"); // NOI18N
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            BaseAction action = null;
            
            if (delegateAction != null) {
                action = delegateAction;
            } else {
                BaseKit kit = Utilities.getKit(target);
                Action a = kit == null ? null : kit.getActionByName(toggleCommentAction);
                if (a instanceof BaseAction) {
                    action = (BaseAction) a;
                }
            }

            if (action instanceof ToggleCommentAction) {
                ((ToggleCommentAction) action).commentUncomment(evt, target, Boolean.FALSE);
            } else {
                if (action != null) {
                    action.putValue("force-uncomment", Boolean.TRUE); // NOI18N
                    try {
                        action.actionPerformed(evt, target);
                    } finally {
                        action.putValue("force-uncomment", null); // NOI18N
                    }
                } else {
                    target.getToolkit().beep();
                }
            }
        }
    } // End of UncommentAction class

    /**
     * @since 1.16
     */
    public static class ToggleCommentAction extends BaseAction {

        static final long serialVersionUID = -1L;

        private final String lineCommentString;
        private final int lineCommentStringLen;
        
        public ToggleCommentAction(String lineCommentString) {
            super(toggleCommentAction);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(ToggleCommentAction.class, "ToggleCommentAction_shortDescription")); //NOI18N
            
            assert lineCommentString != null : "The lineCommentString parameter must not be null."; //NOI18N
            this.lineCommentString = lineCommentString;
            this.lineCommentStringLen = lineCommentString.length();
            
            putValue(BaseAction.ICON_RESOURCE_PROPERTY, "org/netbeans/modules/editor/resources/comment.png"); // NOI18N
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            commentUncomment(evt, target, null);
        }
        
        private void commentUncomment(ActionEvent evt, final JTextComponent target, final Boolean forceComment) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                final Caret caret = target.getCaret();
                final BaseDocument doc = (BaseDocument)target.getDocument();
                doc.runAtomicAsUser (new Runnable () {
                    public void run () {
                        if(caret instanceof EditorCaret) {                            
                            EditorCaret editorCaret = (EditorCaret) caret;
                            boolean beeped = false;
                            for (CaretInfo caretInfo : editorCaret.getSortedCarets()) {
                                try {
                                    int startPos;
                                    int endPos;

                                    if (caretInfo.isSelectionShowing()) {
                                        int start = Math.min(caretInfo.getDot(), caretInfo.getMark());
                                        int end = Math.max(caretInfo.getDot(), caretInfo.getMark());
                                        startPos = Utilities.getRowStart(doc, start);
                                        endPos = end;
                                        if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                                            endPos--;
                                        }
                                        endPos = Utilities.getRowEnd(doc, endPos);
                                    } else { // selection not visible
                                        startPos = Utilities.getRowStart(doc, caretInfo.getDot());
                                        endPos = Utilities.getRowEnd(doc, caretInfo.getDot());
                                    }

                                    int lineCount = Utilities.getRowCount(doc, startPos, endPos);
                                    boolean comment = forceComment != null ? forceComment : !allComments(doc, startPos, lineCount);

                                    if (comment) {
                                        comment(doc, startPos, lineCount);
                                    } else {
                                        uncomment(doc, startPos, lineCount);
                                    }
                                    // TODO:
//                                    NavigationHistory.getEdits().markWaypoint(target, startPos, false, true);
                                } catch (BadLocationException e) {
                                    if(!beeped) {
                                        target.getToolkit().beep();
                                        beeped = true;
                                    }
                                }
                            }
                        } else {
                            try {
                                int startPos;
                                int endPos;

                                if (Utilities.isSelectionShowing(caret)) {
                                    startPos = Utilities.getRowStart(doc, target.getSelectionStart());
                                    endPos = target.getSelectionEnd();
                                    if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                                        endPos--;
                                    }
                                    endPos = Utilities.getRowEnd(doc, endPos);
                                } else { // selection not visible
                                    startPos = Utilities.getRowStart(doc, caret.getDot());
                                    endPos = Utilities.getRowEnd(doc, caret.getDot());
                                }

                                int lineCount = Utilities.getRowCount(doc, startPos, endPos);
                                boolean comment = forceComment != null ? forceComment : !allComments(doc, startPos, lineCount);

                                if (comment) {
                                    comment(doc, startPos, lineCount);
                                } else {
                                    uncomment(doc, startPos, lineCount);
                                }
                                NavigationHistory.getEdits().markWaypoint(target, startPos, false, true);
                            } catch (BadLocationException e) {
                                target.getToolkit().beep();
                            }
                        }
                    }
                });
            }
        }
        
        private boolean allComments(BaseDocument doc, int startOffset, int lineCount) throws BadLocationException {
            for (int offset = startOffset; lineCount > 0; lineCount--) {
                int firstNonWhitePos = Utilities.getRowFirstNonWhite(doc, offset);
                if (firstNonWhitePos == -1) {
                    return false;
                }
                
                if (Utilities.getRowEnd(doc, firstNonWhitePos) - firstNonWhitePos < lineCommentStringLen) {
                    return false;
                }
                
                CharSequence maybeLineComment = DocumentUtilities.getText(doc, firstNonWhitePos, lineCommentStringLen);
                if (!CharSequenceUtilities.textEquals(maybeLineComment, lineCommentString)) {
                    return false;
                }
                
                offset = Utilities.getRowStart(doc, offset, +1);
            }
            return true;
        }
        
        private void comment(BaseDocument doc, int startOffset, int lineCount) throws BadLocationException {
            for (int offset = startOffset; lineCount > 0; lineCount--) {
                doc.insertString(offset, lineCommentString, null); // NOI18N
                offset = Utilities.getRowStart(doc, offset, +1);
            }
        }
        
        private void uncomment(BaseDocument doc, int startOffset, int lineCount) throws BadLocationException {
            for (int offset = startOffset; lineCount > 0; lineCount--) {
                // Get the first non-whitespace char on the current line
                int firstNonWhitePos = Utilities.getRowFirstNonWhite(doc, offset);

                // If there is any, check wheter it's the line-comment-chars and remove them
                if (firstNonWhitePos != -1) {
                    if (Utilities.getRowEnd(doc, firstNonWhitePos) - firstNonWhitePos >= lineCommentStringLen) {
                        CharSequence maybeLineComment = DocumentUtilities.getText(doc, firstNonWhitePos, lineCommentStringLen);
                        if (CharSequenceUtilities.textEquals(maybeLineComment, lineCommentString)) {
                            doc.remove(firstNonWhitePos, lineCommentStringLen);
                        }
                    }
                }

                offset = Utilities.getRowStart(doc, offset, +1);
            }
        }
        
    } // End of CommentUncommentAction class

    /** Executed when the Escape key is pressed. By default it hides
    * the popup menu if visible.
    */
    public static class EscapeAction extends BaseKitLocalizedAction {

        public EscapeAction() {
            super(escapeAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Utilities.getEditorUI(target).hidePopupMenu();
            }
        }
    }


    /** 
     * @deprecated Please do not subclass this class. Use Typing Hooks instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
//    @EditorActionRegistration(
//            name = defaultKeyTypedAction,
//            shortDescription = editorBundleHash + defaultKeyTypedAction
//    )
    @Deprecated
    public static class ExtDefaultKeyTypedAction extends DefaultKeyTypedAction {

        static final long serialVersionUID =5273032708909044812L;

        public @Override void actionPerformed(ActionEvent evt, JTextComponent target) {
            String cmd = evt.getActionCommand();
            int mod = evt.getModifiers();

            // Dirty fix for Completion shortcut on Unix !!!
            if (cmd != null && cmd.equals(" ") && (mod == ActionEvent.CTRL_MASK)) { // NOI18N
                // Ctrl + SPACE
            } else {
                Caret caret = target.getCaret();
                if (caret instanceof ExtCaret) {
                    ((ExtCaret)caret).requestMatchBraceUpdateSync(); // synced bracket update
                }
                super.actionPerformed(evt, target);
            }

            if ((target != null) && (evt != null)) {
                if ((cmd != null) && (cmd.length() == 1)) {
                    // Check whether char that should reindent the line was inserted
                    checkIndentHotChars(target, cmd);

                    // Check the completion
                    checkCompletion(target, cmd);
                }
            }
        }

        // --------------------------------------------------------------------
        // SPI
        // --------------------------------------------------------------------

        /** 
         * Check the characters that should cause reindenting the line. 
         * 
         * @deprecated Please use <a href="@org-netbeans-modules-editor-indent-support@/org/netbeans/modules/editor/indent/spi/support/AutomatedIndenting.html">AutomatedIndentig</a>
         *   or Typing Hooks instead, for details see
         *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
         */
        protected void checkIndentHotChars(JTextComponent target, String typedText) {
        }


        /** 
         * Check and possibly popup, hide or refresh the completion 
         * @deprecated Please use Editor Code Completion API instead, for details see
         *   <a href="@org-netbeans-modules-editor-completion@/overview-summary.html">Editor Code Completion</a>.
         */
        protected void checkCompletion(JTextComponent target, String typedText) {
        }
    }

    /** 
     * @deprecated Please use Editor Code Completion API instead, for details see
     *   <a href="@org-netbeans-modules-editor-completion@/overview-summary.html">Editor Code Completion</a>.
     */
    @Deprecated
    @EditorActionRegistration(
            name = completionShowAction,
            shortDescription = editorBundleHash + completionShowAction
    )
    public static class CompletionShowAction extends BaseKitLocalizedAction {

        static final long serialVersionUID =1050644925893851146L;

        public CompletionShowAction() {
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        }

    }

    /** 
     * @deprecated Please use Editor Code Completion API instead, for details see
     *   <a href="@org-netbeans-modules-editor-completion@/overview-summary.html">Editor Code Completion</a>.
     */
    @Deprecated
    @EditorActionRegistration(
            name = allCompletionShowAction,
            shortDescription = editorBundleHash + allCompletionShowAction
    )
    public static class AllCompletionShowAction extends BaseKitLocalizedAction {

        public AllCompletionShowAction() {
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        }

    }

    /** 
     * @deprecated Please use Editor Code Completion API instead, for details see
     *   <a href="@org-netbeans-modules-editor-completion@/overview-summary.html">Editor Code Completion</a>.
     */
    @Deprecated
    @EditorActionRegistration(
            name = documentationShowAction,
            shortDescription = editorBundleHash + documentationShowAction
    )
    public static class DocumentationShowAction extends BaseKitLocalizedAction {

        public DocumentationShowAction() {
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        }

    }

    /** 
     * @deprecated Please use Editor Code Completion API instead, for details see
     *   <a href="@org-netbeans-modules-editor-completion@/overview-summary.html">Editor Code Completion</a>.
     */
    @Deprecated
    @EditorActionRegistration(
            name = completionTooltipShowAction,
            shortDescription = editorBundleHash + completionTooltipShowAction
    )
    public static class CompletionTooltipShowAction extends BaseKitLocalizedAction {

        public CompletionTooltipShowAction() {
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        }

    }

    /** 
     * @deprecated Please do not subclass this class. Use Typing Hooks instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    @Deprecated
    public static class ExtDeleteCharAction extends DeleteCharAction {

    public ExtDeleteCharAction(String nm, boolean nextChar) {
      super(nm, nextChar);
    }
    
  }


}
