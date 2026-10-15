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

package org.netbeans.swing.plaf;

import java.awt.Color;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;

/** Look and feel customizations interface.
 * For various look and feels, there is a need to customize colors,
 * borders etc. to provide 'native-like' UI.
 * Implementers of this interface should install and uninstall custom
 * UI elements into UIManager on request.
 *
 * There are three types of customization possible:
 * <ol>
 * <li>Guaranteeing values code expects to be non-null but the look and feel (i.e. GTK) may not provide.</li>
 * <li>Customizing values that are already present (such as changing the default font for labels)</li>
 * <li>Adding values used by custom components which are not part of Swing, but use UIDefaults to fetch their colors,
 *     fonts, uis, borders, etc.</li>
 * </ol>
 * Each type of customization is a separate method on this interface.  On startup, first the customizations for
 * all look and feels are run, then the customizations for the specific look and feel that is currently set.
 * <p>
 * A non-standard look and feel that wishes to provide some custom colors for NetBeans window system, etc., can
 * do so by placing an instance of its implementation of LFCustoms into UIDefaults under the key
 * &quot;Nb.[return value of the custom look and feel's getID() method]LFCustoms&quot;.
 * <p>
 * Given that all this class does is return some keys and values, in the future it may be replaced by an
 * XML file similar to <a href="https://netbeans.apache.org/projects/ui/themes/themes">theme files</a>.
 * <p>
 * This class defines a number of relatively self-explanatory UIManager keys for things used in various parts
 * of NetBeans.
 *
 * @author  Dafe Simonek, Tim Boudreau
 */
public abstract class LFCustoms {
    private Object[] lfKeysAndValues = null;
    private Object[] appKeysAndValues = null;
    private Object[] guaranteedKeysAndValues = null;
    protected static final String WORKPLACE_FILL = "nb_workplace_fill"; //NOI18N

    /**
     * convert color
     *
     * @since 1.27
     * @param color as integer value
     * @return hexadecimal value
     */
    public static String getHexString(int color) {
            String result = Integer.toHexString(color).toUpperCase();
            if (result.length() == 1) {
                    return '0'+result;
            }
            return result;
    }

    /** cached window text  foreground color as html code */
    private static String textFgColorHTML = "";

    /** @return  window text foreground color as html code
     * @since 1.27
     */
    public static String getTextFgColorHTML() {
        synchronized(LFCustoms.class) {
            if (textFgColorHTML.isEmpty()) {
                    Object o = UIManager.getLookAndFeelDefaults().get("windowText");
                    if( null == o )
                        o = UIManager.getLookAndFeelDefaults().get("Tree.foreground");
                    if (o instanceof Color) {
                            Color resource = (Color)o;
                            textFgColorHTML = "<font color=#" + getHexString(resource.getRed()) + getHexString(resource.getGreen()) + getHexString(resource.getBlue())+">";
                    } else {
                            textFgColorHTML = "<font color=#000000>";
                            Logger.getLogger(LFCustoms.class.getName()).warning("BUG: getTextFgColorHTML: color isn't available");
                    }
            }
            return textFgColorHTML;
        }
    }

    /** cached window text foreground color */
    private static Color textFgColor = null;

    /** @return window text foreground color
     * @since 1.27
     */
    public static Color getTextFgColor() {
        synchronized(LFCustoms.class) {
            if (textFgColor == null) {
                Object o = UIManager.getLookAndFeelDefaults().get("windowText");
                if( null == o )
                    o = UIManager.getLookAndFeelDefaults().get("Tree.foreground");
                if (o instanceof Color) {
                    textFgColor = (Color) o;
                } else {
                    textFgColor = Color.BLACK;
                    Logger.getLogger(LFCustoms.class.getName()).warning("BUG: getTextFgColor: color isn't available");
                }
            }
            return textFgColor;
        }
    }

    /** shift color value */
    private static final int shiftValue = 64;

    /** convert color component
     * @return brighter  color component
     * @since 1.27
     */
    private static int brighter (int color) {
        return Math.min(color + shiftValue, 255);
    }

    /** convert color component
     *  @return  darker  color component
     * @since 1.27
     */
    private static int darker(int color) {
        return Math.max( color - shiftValue, 0 );
    }

    /** convert color to brighter one if window foreground text color is bright color
     * or convert color to darker one if window foreground text color is dark color
     * @param color to shift
     * @return converted color
     * @since 1.27
     */
    public static Color shiftColor(Color color) {
        Color textColor = getTextFgColor();
        if ((textColor.getRed() > 127) || (textColor.getGreen() > 127) || (textColor.getBlue() > 127)) {
            return new Color(brighter(color.getRed()), brighter(color.getGreen()), brighter(color.getBlue()));
        }
        return new Color(darker(color.getRed()), darker(color.getGreen()), darker(color.getBlue()));
    }

    /** get foreground text color from AttributeSet
     *  or get window foreground text color if AttributeSet doesn't define foreground text color
     * @param a attributeset to get color from
     * @return  foreground text color
     * @since 1.27
     */
    public static Color getForeground(AttributeSet a) {
        Color fg = (Color) a.getAttribute(javax.swing.text.StyleConstants.Foreground);
        if (fg == null) {
            fg = getTextFgColor();
        }
        return fg;
    }

    //TODO: A nice idea would be to replace these classes with XML files - minor rewrite of NbTheme to do it

    /** Fetch and cache keys and values */
    Object[] getLookAndFeelCustomizationKeysAndValues () {
        if (lfKeysAndValues == null) {
            //System.err.println (getClass() + " getLfKeysAndValues");
            lfKeysAndValues = createLookAndFeelCustomizationKeysAndValues();
        }
        return lfKeysAndValues;
    }

    /** Fetch and cache keys and values */
    Object[] getApplicationSpecificKeysAndValues () {
        if (appKeysAndValues == null) {
            //System.err.println (getClass() + " getAppSpecificKeysAndValues");
            appKeysAndValues = createApplicationSpecificKeysAndValues();
        }
        return appKeysAndValues;
    }

    /** Fetch and cache keys and values */
    Object[] getGuaranteedKeysAndValues () {
        if (guaranteedKeysAndValues == null) {
            //System.err.println (getClass() + " getGuaranteedKeysAndValues");
            guaranteedKeysAndValues = createGuaranteedKeysAndValues();
        }
        return guaranteedKeysAndValues;
    }

    /**
     * Get all keys this LFCustoms installs in UIManager.  This is used to 
     * delete unneeded elements from UIManager if the look and feel is changed
     * on the fly (for example, the user switches Windows from Classic to XP
     * look).
     */
    Object[] allKeys() {
        Object[] additional = additionalKeys();
        int size = additional == null ? 0 : additional.length;
        if (appKeysAndValues != null) {
            size += appKeysAndValues.length / 2;
        }
        if (guaranteedKeysAndValues != null) {
            size += guaranteedKeysAndValues.length / 2;
        }
        if (lfKeysAndValues != null) {
            size += (lfKeysAndValues.length / 2);
        }
        Object[] result = new Object [size];

        int ct = 0;
        if (lfKeysAndValues != null) {
            //may be null, if the flag to not customize was set
            for (int i=0; i < lfKeysAndValues.length; i+=2) {
                result[ct++] = lfKeysAndValues[i];
            }
        }
        if (guaranteedKeysAndValues != null) {
            for (int i=0; i < guaranteedKeysAndValues.length; i+=2) {
                result[ct++] = guaranteedKeysAndValues[i];
            }
        }
        if (appKeysAndValues != null) {
            for (int i=0; i < appKeysAndValues.length; i+=2) {
                result[ct++] = appKeysAndValues[i];
            }
        }
        if (additional != null) {
            for (int i=0; i < additional.length; i++) {
                result[ct++] = additional[i];
            }
        }
        return result;
    }
    
    /**
     * LFCustoms implementations which use UIBootstrapValue.Lazy should return
     * any keys that it will install here, so they can be merged into the list
     * of things to clear on L&amp;F change.
     *
     * @return an array of objects or null.
     */
    protected Object[] additionalKeys() {
        return null;
    }

    /** Dispose the value part of all arrays - no need to hold onto lazy value instances
     * or GuaranteedValue instances - they should disappear once dereferenced.  We only
     * need the keys to uninstall the customizations later.
     */
    void disposeValues() {
        if (lfKeysAndValues != null) {
            //may be null, if the flag to not customize was set
            disposeValues (lfKeysAndValues);
        }
        disposeValues (appKeysAndValues);
        disposeValues (guaranteedKeysAndValues);
    }

    /** Null every other element of an array */
    private void disposeValues (Object[] arr) {
        for (int i=1; i < arr.length; i+=2) {
            arr[i] = null;
        }
    }

    /**
     * Create any objects to put into UIDefaults to <strong>replace</strong> values normally supplied by the look
     * and feel, to customize application appearance.
     *
     * @return An array of key-value pairs to put into UIDefaults
     */
    public Object[] createLookAndFeelCustomizationKeysAndValues () {
        return new Object[0];
    }

    /**
     * Create any objects to put into UIDefaults for custom components which use UIManager to find values, UIs, etc.
     *
     * @return An array of key-value pairs to put into UIDefaults
     */
    public Object[] createApplicationSpecificKeysAndValues () {
        return new Object[0];
    }

    /**
     * Provide UIDefaults entries for things which components rely on being non-null, but which may be null on some
     * look and feels or some versions of the look and feel in question.  For example, if you have a component that
     * sets its background by calling <code>UIManager.get(&quot;controlShadow&quot;)</code>, you need to guarantee
     * that this will be non-null when fetched from UIManager - but look and feels do not guarantee this.  The typical
     * pattern here is to put into UIManager an instance of <code>GuaranteedValue</code>, i.e.
     * <code>new GuaranteedValue ("controlShadow", Color.GRAY)</code> which will take on the value already present
     * if it's there, and provide a fallback if it's not.
     *
     * @see org.netbeans.swing.plaf.util.GuaranteedValue
     * @return An array of key-value pairs to put into UIDefaults
     *
     **/
    public Object[] createGuaranteedKeysAndValues () {
        return new Object[0];
    }

    /** Integer value which LFCustoms will <i>read</i>.  On startup, if a
     * custom font size is specified, the core will put this into UIDefaults.
     * We then read it out if present and use it to set up a custom font size. */
    protected static final String CUSTOM_FONT_SIZE = "customFontSize"; //NOI18N
    
    //Default font size - some classes use this to handle creating appropriate
    //custom fonts based on this value
    protected static final String DEFAULT_FONT_SIZE = "nbDefaultFontSize"; //NOI18N

    //Color Profile
    protected static final String EDITOR_PREFERRED_COLOR_PROFILE = "nb.preferred.color.profile"; //NOI18N

    //Editor
    protected static final String EDITOR_STATUS_LEFT_BORDER = "Nb.Editor.Status.leftBorder"; //NOI18N
    protected static final String EDITOR_STATUS_INNER_BORDER = "Nb.Editor.Status.innerBorder"; //NOI18N
    protected static final String EDITOR_STATUS_RIGHT_BORDER = "Nb.Editor.Status.rightBorder"; //NOI18N
    protected static final String EDITOR_STATUS_ONLYONEBORDER = "Nb.Editor.Status.onlyOneBorder"; //NOI18N
    protected static final String EDITOR_TABSCOMPONENT_BORDER = "Nb.Editor.TabsComponent.border"; //NOI18N
    protected static final String EDITOR_TOOLBAR_BORDER = "Nb.Editor.Toolbar.border"; //NOI18N
    protected static final String EDITOR_ERRORSTRIPE_SCROLLBAR_INSETS = "Nb.Editor.ErrorStripe.ScrollBar.Insets"; //NOI18N

    //Explorer
    protected static final String EXPLORER_STATUS_BORDER = "Nb.Explorer.Status.border"; //NOI18N
    protected static final String EXPLORER_FOLDER_ICON = "Nb.Explorer.Folder.icon"; //NOI18N
    protected static final String EXPLORER_FOLDER_OPENED_ICON = "Nb.Explorer.Folder.openedIcon"; //NOI18N

    //Winsys
    protected static final String DESKTOP_BORDER = "Nb.Desktop.border"; //NOI18N
    public static final String SCROLLPANE_BORDER = "Nb.ScrollPane.border"; //NOI18N
    protected static final String TOOLBAR_UI = "Nb.Toolbar.ui"; //NOI18N
    protected static final String DESKTOP_BACKGROUND = "Nb.Desktop.background"; //NOI18N
    public static final String SCROLLPANE_BORDER_COLOR = "Nb.ScrollPane.Border.color"; //NOI18N

    //Output window
    protected static final String OUTPUT_SELECTION_BACKGROUND = "nb.output.selectionBackground"; //NOI18N
    protected static final String OUTPUT_HYPERLINK_FOREGROUND = "nb.hyperlink.foreground"; //NOI18N
    protected static final String OUTPUT_BACKGROUND = "nb.output.background"; //NOI18N
    protected static final String OUTPUT_FOREGROUND = "nb.output.foreground"; //NOI18N

    //QuickSearch
    protected static final String QUICKSEARCH_BORDER = "nb.quicksearch.border"; //NOI18N

    //Property sheet
    protected static final String PROPSHEET_ALTERNATE_ROW_COLOR = "Tree.altbackground"; //NOI18N
    protected static final String PROPSHEET_SET_BACKGROUND = "PropSheet.setBackground"; //NOI18N
    protected static final String PROPSHEET_SELECTED_SET_BACKGROUND = "PropSheet.selectedSetBackground"; //NOI18N
    protected static final String PROPSHEET_SET_FOREGROUND = "PropSheet.setForeground"; //NOI18N
    protected static final String PROPSHEET_SELECTED_SET_FOREGROUND = "PropSheet.selectedSetForeground"; //NOI18N
    protected static final String PROPSHEET_DISABLED_FOREGROUND = "PropSheet.disabledForeground"; //NOI18N
    protected static final String PROPSHEET_SELECTION_BACKGROUND = "PropSheet.selectionBackground"; //NOI18N
    protected static final String PROPSHEET_SELECTION_FOREGROUND = "PropSheet.selectionForeground"; //NOI18N
    protected static final String PROPSHEET_BUTTON_FOREGROUND = "PropSheet.customButtonForeground"; //NOI18N
    protected static final String PROPSHEET_BUTTON_COLOR = "netbeans.ps.buttonColor"; //NOI18N
    protected static final String PROPSHEET_BACKGROUND = "netbeans.ps.background"; //NOI18N

    protected static final String PROPSHEET_ICON_MARGIN = "netbeans.ps.iconmargin"; //NOI18N //Integer
    protected static final String PROPSHEET_ROWHEIGHT = "netbeans.ps.rowheight"; //NOI18N

    //General
    protected static final String ERROR_FOREGROUND = "nb.errorForeground"; //NOI18N
    protected static final String WARNING_FOREGROUND = "nb.warningForeground"; //NOI18N

    //Tab control
    protected static final String EDITOR_TABBED_CONTAINER_UI = "TabbedContainerUI"; //NOI18N
    protected static final String EDITOR_TAB_DISPLAYER_UI = "EditorTabDisplayerUI"; //NOI18N
    protected static final String VIEW_TAB_DISPLAYER_UI = "ViewTabDisplayerUI"; //NOI18N
    protected static final String SLIDING_TAB_DISPLAYER_UI = "SlidingTabDisplayerUI"; //NOI18N
    protected static final String SLIDING_TAB_BUTTON_UI = "IndexButtonUI";
    protected static final String SLIDING_BUTTON_UI = "SlidingButtonUI"; //NOI18N

    //Tab control colors - see org.netbeans.swing.plaf.DefaultTabbedContainerUI
    protected static final String EDITOR_TAB_CONTENT_BORDER = "TabbedContainer.editor.contentBorder"; //NOI18N
    protected static final String EDITOR_TAB_TABS_BORDER = "TabbedContainer.editor.tabsBorder"; //NOI18N
    protected static final String EDITOR_TAB_OUTER_BORDER = "TabbedContainer.editor.outerBorder"; //NOI18N

    //Tab control colors - see org.netbeans.swing.plaf.DefaultTabbedContainerUI
    protected static final String VIEW_TAB_CONTENT_BORDER = "TabbedContainer.view.contentBorder"; //NOI18N
    protected static final String VIEW_TAB_TABS_BORDER = "TabbedContainer.view.tabsBorder"; //NOI18N
    protected static final String VIEW_TAB_OUTER_BORDER = "TabbedContainer.view.outerBorder"; //NOI18N

    //Tab control colors - see org.netbeans.swing.plaf.DefaultTabbedContainerUI
    protected static final String SLIDING_TAB_CONTENT_BORDER = "TabbedContainer.sliding.contentBorder"; //NOI18N
    protected static final String SLIDING_TAB_TABS_BORDER = "TabbedContainer.sliding.tabsBorder"; //NOI18N
    protected static final String SLIDING_TAB_OUTER_BORDER = "TabbedContainer.sliding.outerBorder"; //NOI18N


    //Tab control borders
    protected static final String TAB_ACTIVE_SELECTION_BACKGROUND = "TabRenderer.selectedActivatedBackground"; //NOI18N
    protected static final String TAB_ACTIVE_SELECTION_FOREGROUND = "TabRenderer.selectedActivatedForeground"; //NOI18N
    protected static final String TAB_SELECTION_FOREGROUND = "TabRenderer.selectedForeground"; //NOI18N
    protected static final String TAB_SELECTION_BACKGROUND = "TabRenderer.selectedBackground"; //NOI18N

    protected static final String EXPLORER_MINISTATUSBAR_BORDER = "nb.explorer.ministatusbar.border"; //NOI18N

    protected static final String DESKTOP_SPLITPANE_BORDER = "nb.desktop.splitpane.border"; //NOI18N

    //Enables lazy loading of defaults for the property sheet - since it's not actually shown on startup
    //anymore, no need to install its keys and values on startup
    protected static final String PROPERTYSHEET_BOOTSTRAP = "nb.propertysheet";

    /** For JSplitPane.VERTICAL_SPLIT */
    protected static final String SPLIT_PANE_DIVIDER_SIZE_VERTICAL   = "Nb.SplitPane.dividerSize.vertical";
    /** For JSplitPane.HORIZONTAL_SPLIT */
    protected static final String SPLIT_PANE_DIVIDER_SIZE_HORIZONTAL = "Nb.SplitPane.dividerSize.horizontal";

    // keys used to store theme values in UIDefaults
    public static final String CONTROLFONT = "controlFont"; // NOI18N
    public static final String SYSTEMFONT = "systemFont"; //NOI18N
    public static final String USERFONT = "userFont"; //NOI18N
    public static final String MENUFONT = "menuFont"; //NOI18N
    public static final String WINDOWTITLEFONT = "windowTitleFont"; //NOI18N
    public static final String SUBFONT = "subFont"; //NOI18N
    public static final String LISTFONT = "List.font"; //NOI18N
    public static final String TREEFONT = "Tree.font"; //NOI18N
    public static final String PANELFONT = "Panel.font"; //NOI18N  
    public static final String SPINNERFONT = "Spinner.font"; //NOI18N  
    
    // keys used by the progressbar api module.
    public static final String PROGRESS_CANCEL_BUTTON_ICON = "nb.progress.cancel.icon";
    public static final String PROGRESS_CANCEL_BUTTON_ROLLOVER_ICON = "nb.progress.cancel.icon.mouseover";
    public static final String PROGRESS_CANCEL_BUTTON_PRESSED_ICON = "nb.progress.cancel.icon.pressed";

    /**
     * Keys used by the options dialog module.
     */
    public static final String OPTIONS_USE_UI_DEFAULT_COLORS = "nb.options.useUIDefaultsColors";
    public static final String OPTIONS_CATEGORIES_SEPARATOR_COLOR = "nb.options.categories.separatorColor";
    public static final String OPTIONS_CATEGORIES_BUTTON_USE_NIMBUS = "nb.options.categories.button.useNimbusCategoryButton";

    /**
     * FileChooser
     */
    public static final String FILECHOOSER_SHORTCUTS_FILESFUNCTION = "FileChooser.shortcuts.filesFunction";
    public static final String FILECHOOSER_SHORTCUTS_PANEL_FACTORY = "FileChooser.shortcuts.panel.factory";
    public static final String FILECHOOSER_FAVORITES_ENABLED = "FileChooser.favorites.enabled";
}
