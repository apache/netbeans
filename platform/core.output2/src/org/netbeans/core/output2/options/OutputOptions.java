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
package org.netbeans.core.output2.options;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import org.netbeans.core.output2.Controller;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.util.NbPreferences;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOColors;

/**
 *
 * @author jhavlin
 */
public class OutputOptions {

    public enum LinkStyle {
        NONE, UNDERLINE
    }

    private static OutputOptions DEFAULT = null;
    private boolean initialized = false;
    private static final Logger LOG = Logger.getLogger(
            OutputOptions.class.getName());
    private static AtomicBoolean saveScheduled = new AtomicBoolean(false);
    private static final String PREFIX = "output.settings.";            //NOI18N
    public static final String PROP_FONT = "font";                      //NOI18N
    private static final String PROP_FONT_FAMILY = "font.family";       //NOI18N
    private static final String PROP_FONT_SIZE = "font.size";           //NOI18N
    private static final String PROP_FONT_STYLE = "font.style";         //NOI18N
    public static final String PROP_COLOR_STANDARD = "color.standard";  //NOI18N
    public static final String PROP_COLOR_ERROR = "color.error";        //NOI18N
    public static final String PROP_COLOR_INPUT = "color.input";        //NOI18N
    public static final String PROP_COLOR_LINK = "color.link";          //NOI18N
    public static final String PROP_COLOR_LINK_IMPORTANT =
            "color.link.important";                                     //NOI18N
    public static final String PROP_COLOR_BACKGROUND =
            "color.backgorund";                                         //NOI18N
    public static final String PROP_COLOR_WARNING = "color.warning";    //NOI18N
    public static final String PROP_COLOR_FAILURE = "color.failure";    //NOI18N
    public static final String PROP_COLOR_SUCCESS = "color.success";    //NOI18N
    public static final String PROP_COLOR_DEBUG = "color.debug";        //NOI18N
    public static final String PROP_STYLE_LINK = "style.link";          //NOI18N
    public static final String PROP_FONT_SIZE_WRAP = "font.size.wrap";  //NOI18N
    static final String PROP_INITIALIZED = "initialized";       //NOI18N
    private static final int MIN_FONT_SIZE = 3;
    private static final int MAX_FONT_SIZE = 72;
    private static Font defaultFont = null;
    private Font font = null;
    private Font fontWrapped = null; // font for wrapped mode
    private Color colorStandard;
    private Color colorError;
    private Color colorInput;
    private Color colorLink;
    private Color colorLinkImportant;
    private Color colorBackground;
    private Color colorWarning;
    private Color colorFailure;
    private Color colorSuccess;
    private Color colorDebug;
    private LinkStyle linkStyle = LinkStyle.UNDERLINE;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean defaultFontType = false;

    private OutputOptions(boolean initFromDisk) {
        resetToDefault();
        if (!initFromDisk) {
            return;
        }
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                loadFrom(NbPreferences.forModule(Controller.class));
            }
        });
    }

    final void resetToDefault() {
        setDefaultFont();
        setDefaultColors();
        setLinkStyle(LinkStyle.UNDERLINE);
    }

    /**
     * Determine if options were modified by the user in order to trigger activation/deactivation of the Apply button.
     */
    boolean isChanged() {
        Preferences preferences = NbPreferences.forModule(Controller.class);
        if (!getFont().getFamily().equals(preferences.get(PREFIX + PROP_FONT_FAMILY, getDefaultFont().getFamily()))) {
            return true;
        }
        if (getFont().getSize() != preferences.getInt(PREFIX + PROP_FONT_SIZE, getDefaultFont().getSize())) {
            return true;
        }
        if (getFont().getStyle() != preferences.getInt(PREFIX + PROP_FONT_STYLE, getDefaultFont().getStyle())) {
            return true;
        }
        if (!getLinkStyle().name().equals(preferences.get(PREFIX + PROP_STYLE_LINK, "UNDERLINE"))) { //NOI18N
            return true;
        }
        if (getColorStandard().getRGB() != preferences.getInt(PREFIX + PROP_COLOR_STANDARD, getDefaultColorStandard().getRGB())) {
            return true;
        }
        if (getColorError().getRGB() != preferences.getInt(PREFIX + PROP_COLOR_ERROR, getDefaultColorError().getRGB())) {
            return true;
        }
        if (getColorInput().getRGB() != preferences.getInt(PREFIX + PROP_COLOR_INPUT, getDefaultColorInput().getRGB())) {
            return true;
        }
        if (getColorBackground().getRGB() != preferences.getInt(PREFIX + PROP_COLOR_BACKGROUND, getDefaultColorBackground().getRGB())) {
            return true;
        }
        if (getColorLink().getRGB() != preferences.getInt(PREFIX + PROP_COLOR_LINK, getDefaultColorLink().getRGB())) {
            return true;
        }
        if (getColorLinkImportant().getRGB() != preferences.getInt(PREFIX + PROP_COLOR_LINK_IMPORTANT, getDefaultColorLinkImportant().getRGB())) {
            return true;
        }
        if (getColorDebug().getRGB() != preferences.getInt(PREFIX + PROP_COLOR_DEBUG, getDefaultColorDebug().getRGB())) {
            return true;
        }
        if (getColorWarning().getRGB() != preferences.getInt(PREFIX + PROP_COLOR_WARNING, getDefaultColorWarning().getRGB())) {
            return true;
        }
        if (getColorFailure().getRGB() != preferences.getInt(PREFIX + PROP_COLOR_FAILURE, getDefaultColorFailure().getRGB())) {
            return true;
        }
        return getColorSuccess().getRGB() != preferences.getInt(PREFIX + PROP_COLOR_SUCCESS, getDefaultColorSuccess().getRGB());
    }

    public void loadFrom(Preferences preferences) {
        assert !EventQueue.isDispatchThread();
        final OutputOptions diskData = new OutputOptions(false);
        String fontFamily = preferences.get(PREFIX + PROP_FONT_FAMILY,
                getDefaultFont().getFamily());
        int fontSize = preferences.getInt(PREFIX + PROP_FONT_SIZE,
                getDefaultFont().getSize());
        int fontStyle = preferences.getInt(PREFIX + PROP_FONT_STYLE,
                getDefaultFont().getStyle());
        diskData.setFont(new Font(fontFamily, fontStyle, fontSize));
        int fontSizeWrapped = preferences.getInt(PREFIX + PROP_FONT_SIZE_WRAP,
                getDefaultFont().getSize());
        diskData.setFontForWrappedMode(
                getDefaultFont().deriveFont((float) fontSizeWrapped));
        loadColors(preferences, diskData);
        String linkStyleStr = preferences.get(PREFIX + PROP_STYLE_LINK,
                "UNDERLINE");                                           //NOI18N
        try {
            diskData.setLinkStyle(LinkStyle.valueOf(linkStyleStr));
        } catch (Exception e) {
            LOG.log(Level.INFO, "Invalid link style {0}", linkStyleStr);//NOI18N
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                assign(diskData);
                synchronized (OutputOptions.this) {
                    initialized = true;
                }
                pcs.firePropertyChange(PROP_INITIALIZED, false, true);
            }
        });
    }

    private void loadColors(final Preferences preferences,
            final OutputOptions diskData) {
        int rgbStandard = preferences.getInt(PREFIX + PROP_COLOR_STANDARD,
                getDefaultColorStandard().getRGB());
        diskData.setColorStandard(new Color(rgbStandard));
        int rgbError = preferences.getInt(PREFIX + PROP_COLOR_ERROR,
                getDefaultColorError().getRGB());
        diskData.setColorError(new Color(rgbError));
        int rgbInput = preferences.getInt(PREFIX + PROP_COLOR_INPUT,
                getDefaultColorInput().getRGB());
        diskData.setColorInput(new Color(rgbInput));
        int rgbBackground = preferences.getInt(PREFIX + PROP_COLOR_BACKGROUND,
                getDefaultColorBackground().getRGB());
        diskData.setColorBackground(new Color(rgbBackground));
        int rgbLink = preferences.getInt(PREFIX + PROP_COLOR_LINK,
                getDefaultColorLink().getRGB());
        diskData.setColorLink(new Color(rgbLink));
        int rgbLinkImportant = preferences.getInt(
                PREFIX + PROP_COLOR_LINK_IMPORTANT,
                getDefaultColorLinkImportant().getRGB());
        diskData.setColorLinkImportant(new Color(rgbLinkImportant));
        int rgbDebug = preferences.getInt(
                PREFIX + PROP_COLOR_DEBUG,
                getDefaultColorDebug().getRGB());
        diskData.setColorDebug(new Color(rgbDebug));
        int rgbWarning = preferences.getInt(
                PREFIX + PROP_COLOR_WARNING,
                getDefaultColorWarning().getRGB());
        diskData.setColorWarning(new Color(rgbWarning));
        int rgbFailure = preferences.getInt(
                PREFIX + PROP_COLOR_FAILURE,
                getDefaultColorFailure().getRGB());
        diskData.setColorFailure(new Color(rgbFailure));
        int rgbSuccess = preferences.getInt(
                PREFIX + PROP_COLOR_SUCCESS,
                getDefaultColorSuccess().getRGB());
        diskData.setColorSuccess(new Color(rgbSuccess));
    }

    public void saveTo(Preferences preferences) {
        assert !EventQueue.isDispatchThread();

        saveColorsTo(preferences);
        saveFontsTo(preferences);

        try {
            preferences.flush();
        } catch (BackingStoreException ex) {
            LOG.log(Level.INFO, null, ex);
        }
    }

    /**
     * Save color settings. Store only customized values, so changing Look and
     * Feel behaves correctly for output window.
     */
    private void saveColorsTo(Preferences p) {
        saveIfNotDefault(p, PROP_COLOR_STANDARD,
                getColorStandard(), getDefaultColorStandard());
        saveIfNotDefault(p, PROP_COLOR_ERROR,
                getColorError(), getDefaultColorError());
        saveIfNotDefault(p, PROP_COLOR_INPUT,
                getColorInput(), getDefaultColorInput());
        saveIfNotDefault(p, PROP_COLOR_BACKGROUND,
                getColorBackground(), getDefaultColorBackground());
        saveIfNotDefault(p, PROP_COLOR_LINK,
                getColorLink(), getDefaultColorLink());
        saveIfNotDefault(p, PROP_COLOR_WARNING,
                getColorWarning(), getDefaultColorWarning());
        saveIfNotDefault(p, PROP_COLOR_FAILURE,
                getColorFailure(), getDefaultColorFailure());
        saveIfNotDefault(p, PROP_COLOR_SUCCESS,
                getColorSuccess(), getDefaultColorSuccess());
        saveIfNotDefault(p, PROP_COLOR_DEBUG,
                getColorDebug(), getDefaultColorDebug());
        saveIfNotDefault(p, PROP_COLOR_LINK_IMPORTANT,
                getColorLinkImportant(), getDefaultColorLinkImportant());
    }

    /**
     * Save the color only if it set to a customize value. If it is set to
     * default value, make sure that the key is removed from the properties
     * object.
     */
    private void saveIfNotDefault(Preferences preferences, String key,
            Color value, Color dflt) {
        if (value == null || dflt.getRGB() == value.getRGB()) {
            preferences.remove(PREFIX + key);
        } else {
            preferences.putInt(PREFIX + key, value.getRGB());
        }
    }

    /**
     * Save font settings. Save allways, changing L&F shouldn't affect font
     * settings.
     */
    private void saveFontsTo(Preferences preferences) {
        preferences.putInt(PREFIX + PROP_FONT_SIZE, getFont().getSize());
        preferences.putInt(PREFIX + PROP_FONT_STYLE, getFont().getStyle());
        preferences.putInt(PREFIX + PROP_FONT_SIZE_WRAP,
                getFontForWrappedMode().getSize());
        preferences.put(PREFIX + PROP_FONT_FAMILY, getFont().getFamily());
        preferences.put(PREFIX + PROP_STYLE_LINK, getLinkStyle().name());
    }

    private void setDefaultColors() {
        setColorStandard(getDefaultColorStandard());
        setColorError(getDefaultColorError());
        setColorInput(getDefaultColorInput());
        setColorLink(getDefaultColorLink());
        setColorLinkImportant(getDefaultColorLinkImportant());
        setColorBackground(getDefaultColorBackground());
        setColorWarning(getDefaultColorWarning());
        setColorFailure(getDefaultColorFailure());
        setColorSuccess(getDefaultColorSuccess());
        setColorDebug(getDefaultColorDebug());
    }

    private void setDefaultFont() {
        setFont(getDefaultFont());
        setFontForWrappedMode(getDefaultFont());
    }

    public static Font getDefaultFont() {
        if (defaultFont == null) {
            int size = UIManager.getInt("uiFontSize");                  //NOI18N
            if (size < MIN_FONT_SIZE) {
                size = UIManager.getInt("customFontSize");              //NOI18N
            }
            if (size < MIN_FONT_SIZE) {
                Font f = (Font) UIManager.get("controlFont");           //NOI18N
                if (f != null) {
                    size = f.getSize();
                }
            }
            if (size < MIN_FONT_SIZE) {
                size = 11;
            }
            defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, size);     //NOI18N
        }
        return defaultFont;
    }

    public Font getFont() {
        return font;
    }

    public Font getFontForWrappedMode() {
        return fontWrapped;
    }

    public Font getFont(boolean wrapped) {
        return wrapped ? getFontForWrappedMode() : getFont();
    }

    public Color getColorStandard() {
        return colorStandard;
    }

    public Color getColorError() {
        return colorError;
    }

    public Color getColorInput() {
        return colorInput;
    }

    public Color getColorLink() {
        return colorLink;
    }

    public Color getColorLinkImportant() {
        return colorLinkImportant;
    }

    public Color getColorBackground() {
        return colorBackground;
    }

    public Color getColorWarning() {
        return colorWarning;
    }

    public Color getColorFailure() {
        return colorFailure;
    }

    public Color getColorSuccess() {
        return colorSuccess;
    }

    public Color getColorDebug() {
        return colorDebug;
    }

    public LinkStyle getLinkStyle() {
        return linkStyle;
    }

    /**
     * Set font for standard mode.
     */
    public void setFont(Font font) {
        Font fontToSet = checkFontToSet(font);
        if (!fontToSet.equals(this.font)) {
            Font oldFont = this.font;
            this.font = fontToSet;
            defaultFontType = checkDefaultFontType();
            pcs.firePropertyChange(PROP_FONT, oldFont, fontToSet);
        }
    }

    private void setFontForWrappedMode(Font font) {
        Font fontToSet = checkFontToSet(font);
        if (!fontToSet.equals(this.fontWrapped)) {
            int oldFontSize = this.fontWrapped != null
                    ? this.fontWrapped.getSize() : 0;
            this.fontWrapped = fontToSet;
            pcs.firePropertyChange(PROP_FONT_SIZE_WRAP, oldFontSize,
                    fontToSet.getSize());
        }
    }

    private Font checkFontToSet(Font font) {
        Font checkedFont = font == null ? getDefaultFont() : font;
        if (checkedFont.getSize() < MIN_FONT_SIZE) {
            checkedFont = checkedFont.deriveFont((float) MIN_FONT_SIZE);
        } else if (checkedFont.getSize() > MAX_FONT_SIZE) {
            checkedFont = checkedFont.deriveFont((float) MAX_FONT_SIZE);
        }
        return checkedFont;
    }

    /**
     * Set font size for one of modes. If standard mode uses the same font as
     * wrapped mode, set the same font size for both modes.
     *
     * @param wrapped If true, the size is set for wrapped mode, if false, size
     * is set for standard mode. If standard mode uses the same font as wrapped
     * mode, sizes for both modes are modified.
     */
    public void setFontSize(boolean wrapped, int fontSize) {
        if (getFont() != null && (!wrapped || isDefaultFontType())) {
            if (fontSize != getFont().getSize()) {
                setFont(getFont().deriveFont((float) fontSize));
            }
        }
        if (getFontForWrappedMode() != null
                && (wrapped || isDefaultFontType())) {
            setFontForWrappedMode(
                    getFontForWrappedMode().deriveFont((float) fontSize));
        }
    }

    /**
     * Check if currently used font (for not wrapped mode) is of the same type
     * (family, style) as font for wrapped mode (the default font).
     */
    public boolean isDefaultFontType() {
        return defaultFontType;
    }

    /**
     * Check if the font currently used for standard mode is of the same type as
     * font for wrapped mode (default, monospaced).
     */
    private boolean checkDefaultFontType() {
        Font defFont = getDefaultFont();
        return defFont.getName().equals(font.getName())
                && defFont.getStyle() == font.getStyle();
    }

    public void setColorStandard(Color colorStandard) {
        Parameters.notNull("colorStandard", colorStandard);             //NOI18N
        if (!colorStandard.equals(this.colorStandard)) {
            Color oldColorStandard = this.colorStandard;
            this.colorStandard = colorStandard;
            pcs.firePropertyChange(PROP_COLOR_STANDARD, oldColorStandard,
                    colorStandard);
        }
    }

    public void setColorError(Color colorError) {
        Parameters.notNull("colorError", colorError);                   //NOI18N
        if (!colorError.equals(this.colorError)) {
            Color oldColorError = this.colorError;
            this.colorError = colorError;
            pcs.firePropertyChange(PROP_COLOR_ERROR, oldColorError, colorError);
        }
    }

    public void setColorInput(Color colorInput) {
        Parameters.notNull("colorError", colorInput);                   //NOI18N
        if (!colorInput.equals(this.colorInput)) {
            Color oldColorInput = this.colorInput;
            this.colorInput = colorInput;
            pcs.firePropertyChange(PROP_COLOR_INPUT, oldColorInput, colorInput);
        }
    }

    public void setColorLink(Color colorLink) {
        Parameters.notNull("colorLink", colorLink);                     //NOI18N
        if (!colorLink.equals(this.colorLink)) {
            Color oldColorLink = this.colorLink;
            this.colorLink = colorLink;
            pcs.firePropertyChange(PROP_COLOR_LINK, oldColorLink, colorLink);
        }
    }

    public void setColorLinkImportant(Color colorLinkImportant) {
        Parameters.notNull("colorLinkImportant", colorLinkImportant);   //NOI18N
        if (!colorLinkImportant.equals(this.colorLinkImportant)) {
            Color oldColorLinkImportant = this.colorLinkImportant;
            this.colorLinkImportant = colorLinkImportant;
            pcs.firePropertyChange(PROP_COLOR_LINK_IMPORTANT,
                    oldColorLinkImportant, colorLinkImportant);
        }
    }

    public void setColorBackground(Color colorBackground) {
        Parameters.notNull("colorBackground", colorBackground);         //NOI18N
        if (!colorBackground.equals(this.colorBackground)) {
            Color oldColorBackground = this.colorBackground;
            this.colorBackground = colorBackground;
            pcs.firePropertyChange(PROP_COLOR_BACKGROUND, oldColorBackground,
                    colorBackground);
        }
    }

    public void setColorWarning(Color colorWarning) {
        Parameters.notNull("colorWarning", colorWarning);               //NOI18N
        if (!colorWarning.equals(this.colorWarning)) {
            Color oldColorWarning = this.colorWarning;
            this.colorWarning = colorWarning;
            pcs.firePropertyChange(PROP_COLOR_WARNING, oldColorWarning,
                    colorWarning);
        }
    }

    public void setColorFailure(Color colorFailure) {
        Parameters.notNull("colorFailure", colorFailure);               //NOI18N
        if (!colorFailure.equals(this.colorFailure)) {
            Color oldColorFailure = this.colorFailure;
            this.colorFailure = colorFailure;
            pcs.firePropertyChange(PROP_COLOR_FAILURE, oldColorFailure,
                    colorFailure);
        }
    }

    public void setColorSuccess(Color colorSuccess) {
        Parameters.notNull("colorSuccess", colorSuccess);               //NOI18N
        if (!colorSuccess.equals(this.colorSuccess)) {
            Color oldColorSuccess = this.colorSuccess;
            this.colorSuccess = colorSuccess;
            pcs.firePropertyChange(PROP_COLOR_SUCCESS, oldColorSuccess,
                    colorSuccess);
        }
    }

    public void setColorDebug(Color colorDebug) {
        Parameters.notNull("colorDebug", colorDebug);                   //NOI18N
        if (!colorDebug.equals(this.colorDebug)) {
            Color oldColorDebug = this.colorDebug;
            this.colorDebug = colorDebug;
            pcs.firePropertyChange(PROP_COLOR_DEBUG, oldColorDebug,
                    colorDebug);
        }
    }

    public void setLinkStyle(LinkStyle linkStyle) {
        Parameters.notNull("linkStyle", linkStyle);                     //NOI18N
        if (linkStyle != this.linkStyle) {
            LinkStyle oldLinkStyle = this.linkStyle;
            this.linkStyle = linkStyle;
            pcs.firePropertyChange(PROP_STYLE_LINK, oldLinkStyle, linkStyle);
        }
    }

    public static synchronized OutputOptions getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new OutputOptions(true);
        }
        return DEFAULT;
    }

    public void addPropertyChangeListener(
            PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(
            PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Create a copy of this object, with the same options values, but with
     * separate set of listeners.
     */
    public synchronized OutputOptions makeCopy() {
        final OutputOptions copy = new OutputOptions(false);
        copy.font = font;
        copy.fontWrapped = fontWrapped;
        copy.colorStandard = this.colorStandard;
        copy.colorError = this.colorError;
        copy.colorInput = this.colorInput;
        copy.colorBackground = this.colorBackground;
        copy.colorLink = this.colorLink;
        copy.colorLinkImportant = this.colorLinkImportant;
        copy.colorWarning = this.colorWarning;
        copy.colorFailure = this.colorFailure;
        copy.colorSuccess = this.colorSuccess;
        copy.colorDebug = this.colorDebug;
        copy.initialized = initialized;
        copy.linkStyle = linkStyle;
        if (!initialized) {
            PropertyChangeListener l = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(PROP_INITIALIZED)) {
                        copy.assign(OutputOptions.this);
                        synchronized (copy) {
                            copy.initialized = true;
                        }
                        copy.pcs.firePropertyChange(PROP_INITIALIZED,
                                false, true);
                        OutputOptions.this.removePropertyChangeListener(this);
                    }
                }
            };
            OutputOptions.this.addPropertyChangeListener(l);
        } else {
            copy.initialized = true;
        }
        return copy;
    }

    /**
     * @return True if this object has been initialized (with data from
     * persistent storage), false otherwise.
     */
    public synchronized boolean isInitialized() {
        return initialized;
    }

    /**
     * Assign values from another object.
     */
    public void assign(OutputOptions outputOptions) {
        this.setFont(outputOptions.getFont());
        this.setFontForWrappedMode(outputOptions.getFontForWrappedMode());
        this.setColorStandard(outputOptions.getColorStandard());
        this.setColorError(outputOptions.getColorError());
        this.setColorInput(outputOptions.getColorInput());
        this.setColorLink(outputOptions.getColorLink());
        this.setColorLinkImportant(outputOptions.getColorLinkImportant());
        this.setColorBackground(outputOptions.getColorBackground());
        this.setColorDebug(outputOptions.getColorDebug());
        this.setColorWarning(outputOptions.getColorWarning());
        this.setColorFailure(outputOptions.getColorFailure());
        this.setColorSuccess(outputOptions.getColorSuccess());
        this.setLinkStyle(outputOptions.getLinkStyle());
    }

    static Color getDefaultColorStandard() {
        Color out = UIManager.getColor("nb.output.foreground");         //NOI18N
        if (out == null) {
            out = UIManager.getColor("TextField.foreground");           //NOI18N
            if (out == null) {
                out = Color.BLACK;
            }
        }
        return out;
    }

    static Color getDefaultColorBackground() {
        Color back = UIManager.getColor("nb.output.backgorund");        //NOI18N
        if (back == null) {
            back = UIManager.getColor("TextField.background");          //NOI18N
            if (back == null) {
                back = Color.WHITE;
            } else if ("Nimbus".equals( //NOI18N
                    UIManager.getLookAndFeel().getName())) {
                back = new Color(back.getRGB()); // #225829
            }
        }
        return back;
    }

    static Color getDefaultColorError() {
        Color err = UIManager.getColor("nb.output.err.foreground");     //NOI18N
        if (err == null) {
            err = LFCustoms.shiftColor(Color.red);
        }
        return err;
    }

    static Color getDefaultColorInput() {
        Color input = UIManager.getColor("nb.output.input");            //NOI18N
        if (input == null) {
            input = getDefaultColorStandard();
        }
        return input;
    }

    static Color getDefaultColorLink() {
        Color hyperlink = UIManager.getColor(
                "nb.output.link.foreground");                           //NOI18N
        if (hyperlink == null) {
            hyperlink = LFCustoms.shiftColor(Color.blue);
        }
        return hyperlink;
    }

    static Color getDefaultColorLinkImportant() {
        Color hyperlinkImp = UIManager.getColor(
                "nb.output.link.foreground.important");                 //NOI18N
        if (hyperlinkImp == null) {
            return getDefaultColorLink();
        } else {
            return hyperlinkImp;
        }
    }

    static Color getDefaultColorWarning() {
        Color c = UIManager.getColor("nb.output.warning.foreground");
        if (c == null) {
            c = ensureContrastingColor(Color.ORANGE, getDefaultColorBackground());
        }
        return c;
    }

    static Color getDefaultColorFailure() {
        Color c = UIManager.getColor("nb.output.failure.foreground");
        if (c == null) {
            c = ensureContrastingColor(Color.RED, getDefaultColorBackground());
        }
        return c;
    }

    static Color getDefaultColorSuccess() {
        Color c = UIManager.getColor("nb.output.success.foreground");
        if (c == null) {
            c = ensureContrastingColor(Color.GREEN.darker().darker(),
                    getDefaultColorBackground());
        }
        return c;
    }

    static Color getDefaultColorDebug() {
        Color c = UIManager.getColor("nb.output.debug.foreground");
        if (c == null) {
            c = ensureContrastingColor(Color.GRAY, getDefaultColorBackground());
        }
        return c;
    }

    /* From openide.awt/HtmlLabelUI.
     (int pos, String s, Graphics g, int x,
     int y, int w, int h, Font f, Color defaultColor, int style,
     boolean paint, Color background) {  */
    static Color ensureContrastingColor(Color fg, Color bg) {
        if (bg == null) {
            if (isNimbus()) {
                bg = UIManager.getColor("Tree.background"); //NOI18N
                if (null == bg) {
                    bg = Color.WHITE;
                }
            } else {
                bg = UIManager.getColor("text"); //NOI18N

                if (bg == null) {
                    bg = Color.WHITE;
                }
            }
        }
        if (fg == null) {
            if (isNimbus()) {
                fg = UIManager.getColor("Tree.foreground"); //NOI18N
                if (null == fg) {
                    fg = Color.BLACK;
                }
            } else {
                fg = UIManager.getColor("textText"); //NOI18N
                if (fg == null) {
                    fg = Color.BLACK;
                }
            }
        }

        if (Color.BLACK.equals(fg) && Color.WHITE.equals(fg)) {
            return fg;
        }

        boolean replace = fg.equals(bg);
        int dif = 0;

        if (!replace) {
            dif = difference(fg, bg);
            replace = dif < 60;
        }

        if (replace) {
            int lum = luminance(bg);
            boolean darker = lum >= 128;

            if (darker) {
                fg = fg.darker();
            } else {
                fg = fg.brighter();
            }
        }

        return fg;
    }

    private static int difference(Color a, Color b) {
        return Math.abs(luminance(a) - luminance(b));
    }

    private static int luminance(Color c) {
        return (299 * c.getRed() + 587 * c.getGreen() + 114 * c.getBlue()) / 1000;
    }

    static boolean isNimbus() {
        return "Nimbus".equals(UIManager.getLookAndFeel().getID());     //NOI18N
    }

    public Color getColorForType(IOColors.OutputType type) {
        switch (type) {
            case OUTPUT:
                return getColorStandard();
            case ERROR:
                return getColorError();
            case INPUT:
                return getColorInput();
            case HYPERLINK:
                return getColorLink();
            case HYPERLINK_IMPORTANT:
                return getColorLinkImportant();
            case LOG_DEBUG:
                return getColorDebug();
            case LOG_WARNING:
                return getColorWarning();
            case LOG_FAILURE:
                return getColorFailure();
            case LOG_SUCCESS:
                return getColorSuccess();
            default:
                return getColorStandard();
        }
    }

    /**
     * Save default options to persistent storage, in background.
     */
    public static void storeDefault() {
        if (saveScheduled.compareAndSet(false, true)) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    OutputOptions.getDefault().saveTo(
                            NbPreferences.forModule(Controller.class));
                    saveScheduled.set(false);
                }
            }, 100);
        }
    }
}
