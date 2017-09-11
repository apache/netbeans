/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 * Main menu action, like Edit/Go to Source, Edit/Go to Line..., 
 * View/Show Editor Toolbar, View/Show Line Numbers
 * This is the action implements Presenter.Menu and delegates on specific actions like 
 * ExtKit.toggleToolbarAction or ExtKit.gotoSuperImplementationAction
 *
 * @author  Martin Roskanin
 */
public abstract class MainMenuAction implements Presenter.Menu, ChangeListener, LookupListener {

    public static final Icon BLANK_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/editor/resources/empty.gif", false);
    public boolean menuInitialized = false;
    /** icon of the action, null means no icon */
    private final Icon forcedIcon;
    /** true when icon of original action should be ignored */
    private final boolean forceIcon;

    private Lookup.Result<KeyBindingSettings> kbs = null;
    private Lookup.Result<ActionMap> globalActionMap = null;

    private JMenuItem menuPresenter = null;
    
    /** Creates a new instance of ShowLineNumbersAction */
    public MainMenuAction() {
        // force no icon
        this(true, null);
    }
    
    public MainMenuAction (boolean forceIcon, Icon forcedIcon) {
        this.forceIcon = forceIcon;
        this.forcedIcon = forcedIcon;
    }
    
    public void resultChanged(org.openide.util.LookupEvent ev){
        postSetMenu();
    }
    
    public void stateChanged(ChangeEvent e)    {
        postSetMenu();
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;        
    }

    public String getName() {
        return getMenuItemText();
    }

    /** Returns focused editor component */
    private static JTextComponent getComponent(){
        return Utilities.getFocusedComponent();
    }

    /** Returns the action by given name */
    protected static Action getActionByName(String actionName){
        BaseKit bKit = getKit();
        if (bKit!=null){
            Action action = bKit.getActionByName(actionName);
            return action;
        }
        return null;
    }
    
    /** Adds accelerators to given JMenuItem taken from the action */
    protected static void addAccelerators(Action a, JMenuItem item, JTextComponent target){
        if (target == null || a==null || item==null) return;
        
        // get accelerators from kitAction
        Action kitAction = getActionByName((String)a.getValue(Action.NAME));
        if (kitAction!=null) a = kitAction;
        // Try to get the accelerator, TopComponent action could be obsoleted
        Keymap km = target.getKeymap();

        if (km != null) {
            KeyStroke[] keys = km.getKeyStrokesForAction(a);
            KeyStroke itemAccelerator = item.getAccelerator();
            
            if (keys != null && keys.length > 0) {
                if (itemAccelerator==null || !itemAccelerator.equals(keys[0])){
                    item.setAccelerator(keys[0]);
                }
            }else{
                if (itemAccelerator!=null && kitAction!=null){
                    item.setAccelerator(null);
                }
            }
        }
    }
    
    /** Gets the editor kit */
    private static BaseKit getKit(){
        JTextComponent component = getComponent();
        return (component == null) ? null : Utilities.getKit(component);
    }
    
    public boolean isEnabled() {
        return false;
    }
    
    /** If there is no kit sensitive action, some global kit action can be returned
     * by subclasses. Returning null by default */
    protected Action getGlobalKitAction(){
        return null;
    }
    
    protected final ActionMap getContextActionMap() {
        if (globalActionMap == null) {
            globalActionMap = org.openide.util.Utilities.actionsGlobalContext().lookupResult(ActionMap.class);
            globalActionMap.addLookupListener(WeakListeners.create(LookupListener.class, this, globalActionMap));
        }

        Collection<? extends ActionMap> am = globalActionMap.allInstances();
        return am.size() > 0 ? am.iterator().next() : null;
    }
    
    protected final void postSetMenu() {
        Utilities.runInEventDispatchThread(new Runnable() {
            @Override
            public void run() {
                setMenu();
            }
        });
    }

    private static final RequestProcessor RP = new RequestProcessor(MainMenuAction.class.getName(), 1, false, false);
    private static boolean IS_SET_POST_SET_MENU_LISTENER = false;
    /** Sets the state of JMenuItem. Should be called from subclasses constructors
     * after their initialization is done.
     */
    protected void setMenu() {
        // needs to listen on Registry - resultChanged event is fired before
        // TopComponent is really focused - this causes problems in getComponent method
        if (!IS_SET_POST_SET_MENU_LISTENER) {
            EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    postSetMenu();
                }
            });
            IS_SET_POST_SET_MENU_LISTENER = true;
        }
    
        if (kbs == null) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    kbs = MimeLookup.getLookup(MimePath.EMPTY).lookupResult(KeyBindingSettings.class);
                    kbs.addLookupListener(WeakListeners.create(LookupListener.class, MainMenuAction.this, kbs));
                    kbs.allInstances(); 
                }
            });
        }

        ActionMap am = getContextActionMap();
        Action action = null;
        if (am != null) {
            action = am.get(getActionName());
        }

        if (action == null){
            action = getGlobalKitAction();
        }
        
        JMenuItem presenter = getMenuPresenter();
        assert presenter != null : "Got null return from getMenuPresenter in " + this;
        Action presenterAction = presenter.getAction();
        if (presenterAction == null){
            if (action != null) {
                presenter.setAction(action);
                presenter.setToolTipText(null); /* bugfix #62872 */ 
                menuInitialized = false;
            }
        }else{
            if ((action!=null && !action.equals(presenterAction))){
                presenter.setAction(action);
                presenter.setToolTipText(null); /* bugfix #62872 */
                menuInitialized = false;
            }
        }
        
        if (!menuInitialized){
            Mnemonics.setLocalizedText(presenter, getMenuItemText());
            menuInitialized = true;
        }
        
        presenter.setEnabled(action != null);
        JTextComponent comp = Utilities.getFocusedComponent();
        if (comp != null && comp instanceof JEditorPane){
            addAccelerators(action, presenter, comp);
        } else {
            presenter.setAccelerator(getDefaultAccelerator());
        }
        
        if (forceIcon) {
            presenter.setIcon(forcedIcon);
        }
    }
    
    /** Get the text of the menu item */
    protected abstract String getMenuItemText();
    
    /** Get the action name */
    protected abstract String getActionName();

    public JMenuItem getMenuPresenter() {
        if (menuPresenter == null) {
            menuPresenter = new JMenuItem() {
                public @Override void setText(String text) {
                    // never change the text of this menu item to anything else
                    // then getMenuItemText()
                    if (getText() == null || getText().length() == 0) {
                        super.setText(text);
                    }
                }
            };
            Mnemonics.setLocalizedText(menuPresenter, getMenuItemText());
        }
        return menuPresenter;
    }

    /** Get default accelerator */
    protected KeyStroke getDefaultAccelerator(){
//        Lookup ml = MimeLookup.getLookup(MimePath.get("text/x-java")); //NOI18N
//        KeyBindingSettings kbs = (KeyBindingSettings) ml.lookup(KeyBindingSettings.class);
//        if (kbs != null){
//            List lst = kbs.getKeyBindings();
//            if (lst != null){
//                for (int i=0; i<lst.size(); i++){
//                    MultiKeyBinding mkb = (MultiKeyBinding)lst.get(i);
//                    String an = mkb.getActionName();
//                    if (an != null && an.equals(getActionName())){
//                        if (mkb.getKeyStrokeCount() == 1){// we do not support multi KB in mnemonics
//                            return mkb.getKeyStroke(0);
//                        }
//                    }
//                }
//            }
//        }
        return null;
    }
    
    public static class ShowToolBarAction extends MainMenuAction{

        private static JCheckBoxMenuItem SHOW_TOOLBAR_MENU = null;
        private Action delegate = null;
        
        public ShowToolBarAction(){
            super(false, null);
        }

        protected @Override void setMenu(){
            super.setMenu();
            JTextComponent c = getComponent();
            MimePath mimePath = c == null ? MimePath.EMPTY : MimePath.parse(DocumentUtilities.getMimeType(c));
            Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
            boolean visible = prefs.getBoolean(SimpleValueNames.TOOLBAR_VISIBLE_PROP, EditorPreferencesDefaults.defaultToolbarVisible);
            SHOW_TOOLBAR_MENU.setState(visible);
        }

        public @Override JMenuItem getMenuPresenter() {
            if (SHOW_TOOLBAR_MENU == null) {
                SHOW_TOOLBAR_MENU = new JCheckBoxMenuItem() {
                    public @Override void setText(String text) {
                        // never change the text of this menu item to anything else
                        // then getMenuItemText()
                        if (getText() == null || getText().length() == 0) {
                            super.setText(text);
                        }
                    }
                };
                Mnemonics.setLocalizedText(SHOW_TOOLBAR_MENU, getMenuItemText());
                setMenu();
            }
            return SHOW_TOOLBAR_MENU;
        }

        protected String getMenuItemText(){
            return NbBundle.getBundle(MainMenuAction.class).getString(
                "show_editor_toolbar_main_menu_view_item"); //NOI18N
        }
        
        protected String getActionName() {
            return ExtKit.toggleToolbarAction;
        }        
        
        protected @Override Action getGlobalKitAction() {
            if (delegate == null) {
                delegate = new NbEditorKit.ToggleToolbarAction();
            }
            return delegate;
        }
    }
    
    
    public static class ShowLineNumbersAction extends MainMenuAction{

        private static JCheckBoxMenuItem SHOW_LINE_MENU = null;
        private Action delegate = null;
        
        public ShowLineNumbersAction(){
            super(false, null);
        }
        
        protected @Override void setMenu(){
            super.setMenu();
            JTextComponent c = getComponent();
            MimePath mimePath = c == null ? MimePath.EMPTY : MimePath.parse(DocumentUtilities.getMimeType(c));
            Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
            boolean visible = prefs.getBoolean(SimpleValueNames.LINE_NUMBER_VISIBLE, EditorPreferencesDefaults.defaultLineNumberVisible);
            SHOW_LINE_MENU.setState(visible);
        }
        
        protected String getMenuItemText(){
            return NbBundle.getBundle(MainMenuAction.class).getString(
                "show_line_numbers_main_menu_view_item"); //NOI18N
        }
        
        public @Override String getName() {
            return getMenuItemText();
        }   
        
        public @Override JMenuItem getMenuPresenter() {
            if (SHOW_LINE_MENU == null) {
                SHOW_LINE_MENU  = new JCheckBoxMenuItem() {
                    public @Override void setText(String text) {
                        // never change the text of this menu item to anything else
                        // then getMenuItemText()
                        if (getText() == null || getText().length() == 0) {
                            super.setText(text);
                        }
                    }
                };
                Mnemonics.setLocalizedText(SHOW_LINE_MENU, getMenuItemText());
                setMenu();
            }
            return SHOW_LINE_MENU;
        }
        
        protected String getActionName() {
            return ExtKit.toggleLineNumbersAction;
        }
        
        protected @Override Action getGlobalKitAction() {
            if (delegate == null) {
                delegate = new NbEditorKit.NbToggleLineNumbersAction();
            }
            return delegate;
        }
    }
    
    
    public static class GoToSourceAction extends MainMenuAction{
        public GoToSourceAction(){
            super(true, null);
            postSetMenu();
        }
        
        protected String getMenuItemText(){
            return NbBundle.getBundle(MainMenuAction.class).getString(
                "goto_source_main_menu_edit_item"); //NOI18N
        }
        
        protected String getActionName() {
            return ExtKit.gotoSourceAction;
        }
    } // End of GoToSourceAction class

    
    public static class GoToSuperAction extends MainMenuAction{
        public GoToSuperAction(){
            super(true, null);
            postSetMenu();
        }
        
        protected String getMenuItemText(){
            return NbBundle.getBundle(MainMenuAction.class).getString(
                "goto_super_implementation_main_menu_edit_item"); //NOI18N
        }
        
        protected String getActionName() {
            return ExtKit.gotoSuperImplementationAction;
        }
    } // End of GoToSuperAction class

    public static class GoToDeclarationAction extends MainMenuAction{
        public GoToDeclarationAction(){
            super();
            postSetMenu();
        }
        
        protected String getMenuItemText(){
            return NbBundle.getBundle(MainMenuAction.class).getString(
                "goto_declaration_main_menu_edit_item"); //NOI18N
        }

        protected String getActionName() {
            return ExtKit.gotoDeclarationAction;
        }
    } // End of GoToDeclarationAction class

    /** Back action in Go To main menu, wrapper for BaseKit.jumpListPrevAction
     */ 
    public static final class JumpBackAction extends MainMenuAction {
        public JumpBackAction () {
            super(true, null);
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(JumpBackAction.class).getString(
                "jump_back_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return BaseKit.jumpListPrevAction;
        }
    } // End of JumpBackAction class
    
    /** Forward action in Go To main menu, wrapper for BaseKit.jumpListNextAction
     */ 
    public static final class JumpForwardAction extends MainMenuAction {
        public JumpForwardAction () {
            super(true, null);
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(JumpForwardAction.class).getString(
                "jump_forward_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return BaseKit.jumpListNextAction;
        }
    } // End of JumpForwardAction class

    /** Reformat Code action in Source main menu, wrapper for BaseKit.formatAction
     */ 
    public static final class FormatAction extends MainMenuAction {
        public FormatAction () {
            super();
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(FormatAction.class).getString(
                "format_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return BaseKit.formatAction;
        }

        @Override
        protected Action getGlobalKitAction() {
            return FileUtil.getConfigObject("Editors/private/GlobalFormatAction.instance", Action.class);
        }

        @Override
        protected KeyStroke getDefaultAccelerator(){
            Lookup ml = MimeLookup.getLookup(MimePath.EMPTY); //NOI18N
            KeyBindingSettings kbs = (KeyBindingSettings) ml.lookup(KeyBindingSettings.class);
            if (kbs != null){
                List lst = kbs.getKeyBindings();
                if (lst != null){
                    for (int i=0; i<lst.size(); i++){
                        MultiKeyBinding mkb = (MultiKeyBinding)lst.get(i);
                        String an = mkb.getActionName();
                        if (an != null && an.equals(getActionName())){
                            if (mkb.getKeyStrokeCount() == 1){// we do not support multi KB in mnemonics
                                return mkb.getKeyStroke(0);
                            }
                        }
                    }
                }
            }
            return null;
        }
    } // end of FormatAction
    
    /** Shift Left action in Source main menu, wrapper for BaseKit.shiftLineLeftAction
     */ 
    public static final class ShiftLineLeftAction extends MainMenuAction {
        public ShiftLineLeftAction () {
            super();
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(ShiftLineLeftAction.class).getString(
                "shift_line_left_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return BaseKit.shiftLineLeftAction;
        }
    } // End of ShiftLineLeftAction class
    
    /** Shift Right action in Source main menu, wrapper for BaseKit.shiftLineRightAction
     */ 
    public static final class ShiftLineRightAction extends MainMenuAction {
        public ShiftLineRightAction () {
            super();
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(ShiftLineRightAction.class).getString(
                "shift_line_right_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return BaseKit.shiftLineRightAction;
        }
    } // End of ShiftLineRightAction class
    
    /** Comment action in Source main menu, wrapper for ExtKit.commentAction
     */ 
    public static final class CommentAction extends MainMenuAction {
        public CommentAction () {
            super();
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(CommentAction.class).getString(
                "comment_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return ExtKit.commentAction;
        }
        
    } // End of CommentAction class
    
    /** Uncomment action in Source main menu, wrapper for ExtKit.uncommentAction
     */ 
    public static final class UncommentAction extends MainMenuAction {
        public UncommentAction () {
            super();
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(UncommentAction.class).getString(
                "uncomment_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return ExtKit.uncommentAction;
        }
        
    } // End of UncommentAction class
    
    /** Uncomment action in Source main menu, wrapper for ExtKit.uncommentAction
     */ 
    public static final class ToggleCommentAction extends MainMenuAction {
        public ToggleCommentAction () {
            super();
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(ToggleCommentAction.class).getString(
                "toggle_comment_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return ExtKit.toggleCommentAction;
        }
        
    } // End of UncommentAction class
    
    /** Insert Next Matching Word action in Source main menu, wrapper for BaseKit.wordMatchNextAction
     */ 
    public static final class WordMatchNextAction extends MainMenuAction {
        public WordMatchNextAction () {
            super();
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(WordMatchNextAction.class).getString(
                "word_match_next_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return BaseKit.wordMatchNextAction;
        }
    } // End of WordMatchNextAction class

    /** Insert Previous Matching Word action in Source main menu, wrapper for BaseKit.wordMatchPrevAction
     */ 
    public static final class WordMatchPrevAction extends MainMenuAction {
        public WordMatchPrevAction () {
            super();
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(WordMatchPrevAction.class).getString(
                "word_match_previous_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return BaseKit.wordMatchPrevAction;
        }
    } // End of WordMatchPrevAction class

    /** Find Next action in Edit main menu, wrapper for BaseKit.findNextAction
     */ 
    public static final class FindNextAction extends MainMenuAction {
        public FindNextAction () {
            super(true, null);
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(FindNextAction.class).getString(
                "find_next_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return BaseKit.findNextAction;
        }
    } // End of FindNextAction class
    
    
    /** Find Previous action in Edit main menu, wrapper for BaseKit.findPreviousAction
     */ 
    public static final class FindPreviousAction extends MainMenuAction {
        public FindPreviousAction () {
            super(true, null);
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(FindNextAction.class).getString(
                "find_previous_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return BaseKit.findPreviousAction;
        }
    } // End of FindPreviousAction class

    /** Find Selection action in Edit main menu, wrapper for BaseKit.findSelectionAction
     */ 
    public static final class FindSelectionAction extends MainMenuAction {
        public FindSelectionAction () {
            super(true, null);
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(FindNextAction.class).getString(
                "find_selection_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return BaseKit.findSelectionAction;
        }

        @Override
        public boolean isEnabled() {
            JTextComponent focused = EditorRegistry.focusedComponent();
            return focused != null;
        }
    } // End of FindSelectionAction class

    /** Start Macro Recording action in View main menu, wrapper for BaseKit.startMacroRecordingAction
     */ 
    public static final class StartMacroRecordingAction extends MainMenuAction {
        public StartMacroRecordingAction () {
            super(true, null);
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(StartMacroRecordingAction.class).getString(
                "start_macro_recording_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return BaseKit.startMacroRecordingAction;
        }
    } // End of StartMacroRecordingAction class
    
   
    public static final class SelectAllAction extends MainMenuAction {
        public SelectAllAction() {
            super(true, null);
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(SelectAllAction.class).getString(
                "select_all_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return BaseKit.selectAllAction;
        }
    } // End of SelectAll class

    public static final class SelectIdentifierAction extends MainMenuAction {
        public SelectIdentifierAction() {
            super(true, null);
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(SelectIdentifierAction.class).getString(
                "select_identifier_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return BaseKit.selectIdentifierAction;
        }
    } // End of SelectIdentifierAction class

    /** Stop Macro Recording action in View main menu, wrapper for BaseKit.stopMacroRecordingAction
     */ 
    public static final class StopMacroRecordingAction extends MainMenuAction {
        public StopMacroRecordingAction () {
            super(true, null);
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(StopMacroRecordingAction.class).getString(
                "stop_macro_recording_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return BaseKit.stopMacroRecordingAction;
        }
    } // End of StopMacroRecordingAction class
            
    /** Remove Trailing Spaces action in View main menu, wrapper for BaseKit.removeTrailingSpaces
     */ 
    public static final class RemoveTrailingSpacesAction extends MainMenuAction {
        public RemoveTrailingSpacesAction() {
            super(true, null);
            postSetMenu();
        }

        protected String getMenuItemText () {
            return NbBundle.getBundle(RemoveTrailingSpacesAction.class).getString(
                "remove_trailing_spaces_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return BaseKit.removeTrailingSpacesAction;
        }
    } // End of RemoveTrailingSpacesAction class

    /** Paste Formatted action in Edit main menu, wrapper for BaseKit.pasteFormattedAction
     */ 
    public static final class PasteFormattedAction extends MainMenuAction {
        public PasteFormattedAction() {
            super(true, null);
            postSetMenu();
        }

        protected String getMenuItemText () {
            return NbBundle.getBundle(PasteFormattedAction.class).getString(
                "paste_formatted_main_menu_item"); //NOI18N
        }

        protected String getActionName () {
            return BaseKit.pasteFormatedAction;
        }
    } // End of PasteFormattedAction class
    
}
    
