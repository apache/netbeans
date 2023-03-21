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

package org.netbeans.editor;

import java.awt.event.ActionEvent;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.modules.editor.lib2.actions.MacroRecording;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * This is the parent of majority of the actions. It implements
 * the necessary resetting depending of what is required
 * by constructor of target action.
 * The other thing implemented here is macro recording.
 * <br>
 * Property "noIconInMenu" can be set to inform menu items not to use action's icon.
 * <br>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class BaseAction extends TextAction {

    /** Text of the menu item in popup menu for this action */
    public static final String POPUP_MENU_TEXT = "PopupMenuText"; // NOI18N

    /** Prefix for the name of the key for description in locale support */
    public static final String LOCALE_DESC_PREFIX = "desc-"; // NOI18N

    /** Prefix for the name of the key for popup description in locale support */
    public static final String LOCALE_POPUP_PREFIX = "popup-"; // NOI18N

    /** Resource for the icon */
    public static final String ICON_RESOURCE_PROPERTY = "IconResource"; // NOI18N

    /** Remove the selected text at the action begining */
    public static final int SELECTION_REMOVE = 1;

    /** Reset magic caret position */
    public static final int MAGIC_POSITION_RESET = 2;

    /** 
     * Reset abbreviation accounting to empty string.
     * @deprecated Not used anymore.
     */
    @Deprecated
    public static final int ABBREV_RESET = 4;

    /** Prevents adding the new undoable edit to the old one when the next
    * document change occurs.
    */
    public static final int UNDO_MERGE_RESET = 8;

    /** Reset word-match table */
    public static final int WORD_MATCH_RESET = 16;

    /** Clear status bar text */
    public static final int CLEAR_STATUS_TEXT = 32;

    /** The action will not be recorded if in macro recording */
    public static final int NO_RECORDING = 64;

    /** Save current position in the jump list */
    public static final int SAVE_POSITION = 128;

    /** The name of Action property. If the action has property NO_KEYBINDING set to true, it won't
     *  be listed in editor keybindings customizer list.
     */
    public static final String NO_KEYBINDING = "no-keybinding"; //NOI18N

    /** logger for reporting invoked actions */
    private static Logger UILOG = Logger.getLogger("org.netbeans.ui.actions.editor"); // NOI18N
    
    /**
     * Whether invoked actions not logged by default, such as caret moves, should be logged too.
     * -J-Dorg.netbeans.editor.ui.actions.logging.detailed=true
     */
    private static final boolean UI_LOG_DETAILED = Boolean.getBoolean("org.netbeans.editor.ui.actions.logging.detailed");
    
    /** Bit mask of what should be updated when the action is performed before
    * the action's real task is invoked.
    */
    protected int updateMask;

    private static boolean recording;

    static final long serialVersionUID =-4255521122272110786L;

    public BaseAction() {
        this(null);
    }

    public BaseAction(int updateMask) {
        this(null, updateMask);
    }

    public BaseAction(String name) {
        this(name, 0);
    }

    public BaseAction(String name, int updateMask) {
        super(name);
        this.updateMask = updateMask;
    }
    
    /** Find a value in resource bundles.
     * @deprecated this method is deprecated like the LocaleSupport which it uses by default.
     *   It should be replaced by implementing {@link #getShortDescriptionBundleClass()}
     */
    @Deprecated
    protected Object findValue(String key){
        return LocaleSupport.getString(key);
    }
    
    public @Override Object getValue(String key){
        Object obj = super.getValue(key);

        if (obj == null){
            obj = createDefaultValue(key);
            if (obj != null) {
                putValue(key, obj);
            }
        }

        return obj;
    }

    @Override
    public void putValue(String key, Object value) {
        super.putValue(key, value);
        if (Action.NAME.equals(key) && value instanceof String) {
            actionNameUpdate((String)value);
        }
    }

    /**
     * Called by {@link #putValue(String,Object)} when {@link Action#NAME} property
     * is set to a non-null String value. This allows a "polymorphic" action (with
     * Action.NAME-specific behavior) to update certain properties (e.g. an icon)
     * according to the name that was set.
     *
     * @param actionName non-null action's name (value of Action.NAME property).
     * @since 1.34
     */
    protected void actionNameUpdate(String actionName) {
    }

    /**
     * This method is called when there is no value for the particular key.
     * <br>
     * If the returned value is non-null it is remembered
     * by {@link #putValue(String, Object)} so in that case this method
     * is only called once.
     *
     * <p>
     * <b>Note:</b> When overriding this method <code>super</code> implementation
     * should always be called.
     *
     * @param key key for which the default value should be found.
     * @return default value or null if the default value does not exist
     *  for the given key.
     */
    protected Object createDefaultValue(String key) {
        Object ret = null;
        if (SHORT_DESCRIPTION.equals(key)) {
            Class bundleClass = getShortDescriptionBundleClass();
            if (bundleClass != null) {
                // The bundle key is just the action's name
                String bundleKey = (String)getValue(Action.NAME);
                try {
                    ret = NbBundle.getBundle(bundleClass).getString(bundleKey);
                } catch (MissingResourceException mre) {
                    MissingResourceException mre2 = new MissingResourceException("Can't find SHORT_DESCRIPTION for " + this //NOI18N
                            + "; bundleClass=" + bundleClass + "; bundleKey=" + bundleKey, bundleClass.getName(), bundleKey); //NOI18N
                    mre2.initCause(mre);
                    throw mre2;
                }
            } else { // default to slow deprecated findValue()
                // getDefaultShortDescription() is only called once for non-null ret value
                ret = getDefaultShortDescription();
            }

        } else if (POPUP_MENU_TEXT.equals(key)){
            String bundleKey = LOCALE_POPUP_PREFIX + getValue(Action.NAME);
            ret = findValue(bundleKey);
            if (ret == null){
                ret = getValue(SHORT_DESCRIPTION);
            }
        }
        return ret;
    }
    
    /**
     * Get the class in a package where resource bundle for localization
     * of the short description of this action resides.
     * <br>
     * By default this method returns null.
     */
    protected Class getShortDescriptionBundleClass() {
        return null;
    }
    
    /**
     * Get the default value for {@link Action#SHORT_DESCRIPTION} property.
     * <br>
     * If this method returns non-empty value it will only be called once
     * (its result will be remembered).
     *
     * @return value that will be use as result for
     *  <code>Action.getValue(Action.SHORT_DESCRIPTION)</code>.
     */
    protected Object getDefaultShortDescription() {
        String actionName = (String)getValue(Action.NAME);
        String localizerKey = LOCALE_DESC_PREFIX + actionName;
        Object obj = findValue(localizerKey);
        if (obj==null){
            obj = findValue(actionName);
            if (obj==null) obj = actionName;
        }
        return obj;
    }

// XXX: remove
//    /** This method is called once after the action is constructed
//    * and then each time the settings are changed.
//    * @param evt event describing the changed setting name. It's null
//    *   if it's called after the action construction.
//    * @param kitClass class of the kit that created the actions
//    */
//    protected void settingsChange(SettingsChangeEvent evt, Class kitClass) {
//    }

    /** This method is made final here as there's an important
    * processing that must be done before the real action
    * functionality is performed. It can include the following:
    * 1. Updating of the target component depending on the update
    *    mask given in action constructor.
    * 2. Possible macro recoding when the macro recording
    *    is turned on.
    * The real action functionality should be done in
    * the method actionPerformed(ActionEvent evt, JTextComponent target)
    * which must be redefined by the target action.
    */
    public final void actionPerformed(final ActionEvent evt) {
        final JTextComponent target = getTextComponent(evt);

        // #146657 - Only perform the action if the document is BaseDocument's instance
        // #147899 - NPE
        if (target == null || !(target.getDocument() instanceof BaseDocument)) {
            return;
        }
                              
        if(0 == (updateMask & NO_RECORDING) ) {
            MacroRecording.get().recordAction(this, evt, target);
        }
        

        updateComponent(target);

        if (UILOG.isLoggable(Level.FINE)) {
            String actionName = getValue(NAME) != null ? getValue(NAME).toString().toLowerCase() : null;
            if (actionName != null &&
                !"default-typed".equals(actionName) && //NOI18N
                -1 == actionName.indexOf("build-tool-tip") &&//NOI18N
                -1 == actionName.indexOf("build-popup-menu") &&//NOI18N
                -1 == actionName.indexOf("-kit-install") && //NOI18N
                (UI_LOG_DETAILED || (
                    -1 == actionName.indexOf("caret") && //NOI18N
                    -1 == actionName.indexOf("delete") && //NOI18N
                    -1 == actionName.indexOf("undo") &&//NOI18N
                    -1 == actionName.indexOf("redo") &&//NOI18N
                    -1 == actionName.indexOf("selection") && //NOI18N
                    -1 == actionName.indexOf("page-up") &&//NOI18N
                    -1 == actionName.indexOf("page-down") //NOI18N
                ))
            ) {
                LogRecord r = new LogRecord(Level.FINE, "UI_ACTION_EDITOR"); // NOI18N
                r.setResourceBundle(NbBundle.getBundle(BaseAction.class));
                if (evt != null) {
                    r.setParameters(new Object[] { evt, evt.toString(), this, toString(), getValue(NAME) });
                } else {
                    r.setParameters(new Object[] { "no-ActionEvent", "no-ActionEvent", this, toString(), getValue(NAME) }); //NOI18N
                }
                r.setLoggerName(UILOG.getName());
                UILOG.log(r);
            }
        }
        
        if (asynchonous()) {
            RequestProcessor.getDefault().post(new Runnable () {
                public void run() {
                    actionPerformed(evt, target);
                }
            });
        } else {
            actionPerformed(evt, target);
        }
    }
    
    /**
     * Use MacroRecording from editor.lib2
     */
    @Deprecated
    boolean startRecording( JTextComponent target ) {
        boolean b = MacroRecording.get().startRecording();
        if (b) {
            recording = true;
            Utilities.setStatusText( target,
                NbBundle.getBundle(BaseAction.class).getString( "macro-recording" ) );
        }
        return b;
    }
    
    /**
     * Use MacroRecording from editor.lib2
     */
    @Deprecated
    String stopRecording( JTextComponent target ) {
        String s = MacroRecording.get().stopRecording();
        if (s == null) {
            return s;
        }
        recording = false;
        Utilities.setStatusText( target, "" ); // NOI18N
        return s;
    }
    
    /** The target method that performs the real action functionality.
    * @param evt action event describing the action that occured
    * @param target target component where the action occured. It's retrieved
    *   by the TextAction.getTextComponent(evt).
    */
    public abstract void actionPerformed(ActionEvent evt, JTextComponent target);

    protected boolean asynchonous() {
        return false;
    }

    public JMenuItem getPopupMenuItem(JTextComponent target) {
        return null;
    }

    public String getPopupMenuText(JTextComponent target) {
        String txt = (String)getValue(POPUP_MENU_TEXT);
        if (txt == null) {
            txt = (String)getValue(NAME);
        }
        return txt;
    }

    /** Update the component according to the update mask specified
    * in the constructor of the action.
    * @param target target component to be updated.
    */
    public void updateComponent(JTextComponent target) {
        updateComponent(target, this.updateMask);
    }

    /** Update the component according to the given update mask
    * @param target target component to be updated.
    * @param updateMask mask that specifies what will be updated
    */
    public void updateComponent(JTextComponent target, int updateMask) {
        if (target != null && target.getDocument() instanceof BaseDocument) {
            BaseDocument doc = (BaseDocument)target.getDocument();
            boolean writeLocked = false;

            try {
                // remove selected text
                if ((updateMask & SELECTION_REMOVE) != 0) {
                    writeLocked = true;
                    doc.extWriteLock();
                    Caret caret = target.getCaret();
                    if (caret != null && Utilities.isSelectionShowing(caret)) {
                        int dot = caret.getDot();
                        int markPos = caret.getMark();
                        if (dot < markPos) { // swap positions
                            int tmpPos = dot;
                            dot = markPos;
                            markPos = tmpPos;
                        }
                        try {
                            target.getDocument().remove(markPos, dot - markPos);
                        } catch (BadLocationException e) {
                            Utilities.annotateLoggable(e);
                        }
                    }
                }

                // reset magic caret position
                if ((updateMask & MAGIC_POSITION_RESET) != 0) {
                    if (target.getCaret() != null)
                        target.getCaret().setMagicCaretPosition(null);
                }

                // reset merging of undoable edits
                if ((updateMask & UNDO_MERGE_RESET) != 0) {
                    doc.resetUndoMerge();
                }

                // reset word matching
                if ((updateMask & WORD_MATCH_RESET) != 0) {
                    ((BaseTextUI)target.getUI()).getEditorUI().getWordMatch().clear();
                }

                // Clear status bar text
                if (!recording && (updateMask & CLEAR_STATUS_TEXT) != 0) {
                    Utilities.clearStatusText(target);
                }

                // Save current caret position in the jump-list
                if ((updateMask & SAVE_POSITION) != 0) {
                    JumpList.checkAddEntry(target);
                }

            } finally {
                if (writeLocked) {
                    doc.extWriteUnlock();
                }
            }
        }
    }

}
