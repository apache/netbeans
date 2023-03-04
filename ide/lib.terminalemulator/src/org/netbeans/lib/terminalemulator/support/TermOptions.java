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
package org.netbeans.lib.terminalemulator.support;

import java.awt.Font;
import java.awt.Color;
import java.util.prefs.Preferences;
import javax.swing.UIManager;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

/**
 * Singleton "bean" to hold Term option properties.
 * 
 * Use TermOptions.getDefault() to get at the singleton.
 *
 * Uses Preferences as a backing store.
 * Uses "term." to distinguish keys from other properties.
 * The values are recovered from backing store on initial creation of
 * the singleton.
 */

public final class TermOptions {
    // Recommended boundary values. Noncompliance with these
    // values rules can cause an exception (e.g. setFontSize(0);)
    public static final int MIN_FONT_SIZE = 8;
    public static final int MAX_FONT_SIZE = 48;
    public static final int MIN_HISTORY_SIZE = 0;
    public static final int MAX_HISTORY_SIZE = 50000;
    public static final int MIN_TAB_SIZE = 1;
    public static final int MAX_TAB_SIZE = 16;
    
    private int fontSizeDefault;
    private Font fontDefault;
    private Color foregroundDefault;
    private Color backgroundDefault;
    private Color selectionBackgroundDefault;
    private int historySizeDefault;
    private int tabSizeDefault;
    private String selectByWordDelimitersDefault;
    private boolean clickToTypeDefault;
    private boolean scrollOnInputDefault;
    private boolean scrollOnOutputDefault;
    private boolean lineWrapDefault;
    private boolean ignoreKeymapDefault;
    private boolean altSendsEscapeDefault;

    // In case settings get shared uniqueify the key names with a prefix:
    private static final String PREFIX = "term.";	// NOI18N
    
    private static TermOptions DEFAULT;

    private boolean dirty = false;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Preferences preferences;

    private TermOptions() {
	resetToDefault();
    }

    // Copy constructor
    private TermOptions(TermOptions orig) {
        assign(orig);
    }

    public final void resetToDefault() {
        Font controlFont = UIManager.getFont("controlFont"); //NOI18N
        fontSizeDefault = (controlFont == null) ? 12 : controlFont.getSize();
        fontDefault = new Font("monospaced", Font.PLAIN, fontSizeDefault); //NOI18N
        foregroundDefault = getDefaultColorStandard();
        backgroundDefault = getDefaultColorBackground();
        selectionBackgroundDefault = getDefaultSelectionBackground();
        historySizeDefault = 5000;
        tabSizeDefault = 8;
        selectByWordDelimitersDefault = "! $^*();<>\\[]{}|\'\"`";    //NOI18N
        clickToTypeDefault = true;
        scrollOnInputDefault = true;
        scrollOnOutputDefault = true;
        lineWrapDefault = true;
        ignoreKeymapDefault = false;
	altSendsEscapeDefault = true;
        
        fontSize = fontSizeDefault;
        font = fontDefault;
        foreground = foregroundDefault;
        background = backgroundDefault;
        selectionBackground = selectionBackgroundDefault;
	historySize = historySizeDefault;
	tabSize = tabSizeDefault;
        selectByWordDelimiters = selectByWordDelimitersDefault;
	clickToType = clickToTypeDefault;
	scrollOnInput = scrollOnInputDefault;
	scrollOnOutput = scrollOnOutputDefault;
	lineWrap = lineWrapDefault;
        ignoreKeymap = ignoreKeymapDefault;
	altSendsEscape = altSendsEscapeDefault;
        markDirty();
    }

    private static Color getDefaultColorStandard() {
        Color out = UIManager.getColor("nb.output.foreground"); //NOI18N
        if (out == null) {
            out = UIManager.getColor("TextField.foreground"); //NOI18N
            if (out == null) {
                out = Color.BLACK;
            }
        }
        return out;
    }

    private static Color getDefaultColorBackground() {
        Color back = UIManager.getColor("nb.output.backgorund"); //NOI18N
        if (back == null) {
            back = UIManager.getColor("TextField.background"); //NOI18N
            if (back == null) {
                back = Color.WHITE;
            } else if ("Nimbus".equals( //NOI18N
                    UIManager.getLookAndFeel().getName())) {
                back = new Color(back.getRGB()); // #225829
            }
        }
        return back;
    }

    private static Color getDefaultSelectionBackground() {
	Color color = UIManager.getColor("TextArea.selectionBackground");// NOI18N
	if (color == null) {
	    // bug #185154
	    // Nimbus L&F doesn't define "TextArea.selectionBackground"
	    color = UIManager.getColor("textHighlight");// NOI18N
	}
        return color;
    }

    public static synchronized TermOptions getDefault(Preferences prefs) {
	if (DEFAULT == null) {
	    DEFAULT = new TermOptions();
	    DEFAULT.loadFrom(prefs);
	}
	return DEFAULT;
    }

    /**
     * Make a copy of 'this'.
     * @return A copy of 'this'.
     */
    public TermOptions makeCopy() {
        return new TermOptions(this);
    }

    /**
     * Assign the values in 'that' to 'this'.
     * @param that Object to copy values from.
     */
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public void assign (TermOptions that) {
        this.preferences = that.preferences;
	this.font= that.font;
	this.fontSize = that.fontSize;
	this.tabSize = that.tabSize;
	this.historySize = that.historySize;
	this.foreground = that.foreground;
	this.background = that.background;
	this.selectionBackground = that.selectionBackground;
        this.selectByWordDelimiters = that.selectByWordDelimiters;
	this.clickToType = that.clickToType;
	this.scrollOnInput = that.scrollOnInput;
	this.scrollOnOutput = that.scrollOnOutput;
	this.lineWrap = that.lineWrap;
        this.ignoreKeymap = that.ignoreKeymap;
        this.altSendsEscape = that.altSendsEscape;
	this.dirty = false;
        
	this.fontDefault= that.fontDefault;
	this.fontSizeDefault = that.fontSizeDefault;
	this.tabSizeDefault = that.tabSizeDefault;
	this.historySizeDefault = that.historySizeDefault;
	this.foregroundDefault = that.foregroundDefault;
	this.backgroundDefault = that.backgroundDefault;
	this.selectionBackgroundDefault = that.selectionBackgroundDefault;
        this.selectByWordDelimitersDefault = that.selectByWordDelimitersDefault;
	this.clickToTypeDefault = that.clickToTypeDefault;
	this.scrollOnInputDefault = that.scrollOnInputDefault;
	this.scrollOnOutputDefault = that.scrollOnOutputDefault;
	this.lineWrapDefault = that.lineWrapDefault;
        this.ignoreKeymapDefault = that.ignoreKeymapDefault;
        this.altSendsEscapeDefault = that.altSendsEscapeDefault;
	pcs.firePropertyChange(null, null, null);
    }

    public boolean isDirty() {
	return dirty;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
	pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
	pcs.removePropertyChangeListener(l);
    }

    void loadFrom(Preferences prefs) {
        if (prefs == null)
            return;
        preferences = prefs;
	String fontFamily = prefs.get(PREFIX + PROP_FONT_FAMILY, font.getFamily());
	int fontStyle = prefs.getInt(PREFIX + PROP_FONT_STYLE, font.getStyle());
        fontSize = prefs.getInt(PREFIX + PROP_FONT_SIZE, fontSize);

	tabSize = prefs.getInt(PREFIX + PROP_TAB_SIZE, tabSize);
	historySize = prefs.getInt(PREFIX + PROP_HISTORY_SIZE, historySize);
        
        selectByWordDelimiters = prefs.get(PREFIX + PROP_SELECT_BY_WORD_DELIMITERS, 
                                                selectByWordDelimiters);

	int foregroundRGB = prefs.getInt(PREFIX + PROP_FOREGROUND,
					 foreground.getRGB());
	foreground = new Color(foregroundRGB);
	int backgroundRGB = prefs.getInt(PREFIX + PROP_BACKGROUND,
					 background.getRGB());
	background = new Color(backgroundRGB);
	int selectionRGB = prefs.getInt(PREFIX + PROP_SELECTION_BACKGROUND,
						  selectionBackground.getRGB());
	selectionBackground = new Color(selectionRGB);
	clickToType = prefs.getBoolean(PREFIX + PROP_CLICK_TO_TYPE,
				       clickToType);
	scrollOnInput = prefs.getBoolean(PREFIX + PROP_SCROLL_ON_INPUT,
					 scrollOnInput);
	scrollOnOutput = prefs.getBoolean(PREFIX + PROP_SCROLL_ON_OUTPUT,
					  scrollOnOutput);
	lineWrap = prefs.getBoolean(PREFIX + PROP_LINE_WRAP,
				    lineWrap);
        
        ignoreKeymap = prefs.getBoolean(PREFIX + PROP_IGNORE_KEYMAP,
				    ignoreKeymap);
        altSendsEscape = prefs.getBoolean(PREFIX + PROP_ALT_SENDS_ESCAPE,
				    altSendsEscape);

	font = new Font(fontFamily, fontStyle, fontSize);

	// If 'fontfamily' isn't recognized Font.<init> will return
	// a "Dialog" font, per javadoc, which isn't fixed-width so
	// we need to fall back on Monospaced.
	if ("Dialog".equals(font.getFamily()))			// NOI18N
	    font = new Font("Monospaced", fontStyle, fontSize);// NOI18N
    }

    public void storeTo(Preferences prefs) {
        if (prefs == null)
            return;
	prefs.put(PREFIX + PROP_FONT_FAMILY, font.getFamily());
	prefs.putInt(PREFIX + PROP_FONT_STYLE, font.getStyle());
	prefs.putInt(PREFIX + PROP_FONT_SIZE, fontSize);
	prefs.putInt(PREFIX + PROP_TAB_SIZE, tabSize);
	prefs.putInt(PREFIX + PROP_HISTORY_SIZE, historySize);
	prefs.putInt(PREFIX + PROP_FOREGROUND, foreground.getRGB());
	prefs.putInt(PREFIX + PROP_BACKGROUND, background.getRGB());
	prefs.putInt(PREFIX + PROP_SELECTION_BACKGROUND,
		     selectionBackground.getRGB());
        prefs.put(PREFIX + PROP_SELECT_BY_WORD_DELIMITERS, selectByWordDelimiters);
	prefs.putBoolean(PREFIX + PROP_CLICK_TO_TYPE, clickToType);
	prefs.putBoolean(PREFIX + PROP_SCROLL_ON_INPUT, scrollOnInput);
	prefs.putBoolean(PREFIX + PROP_SCROLL_ON_OUTPUT, scrollOnOutput);
	prefs.putBoolean(PREFIX + PROP_LINE_WRAP, lineWrap);
        prefs.putBoolean(PREFIX + PROP_IGNORE_KEYMAP, ignoreKeymap);
        prefs.putBoolean(PREFIX + PROP_ALT_SENDS_ESCAPE, altSendsEscape);
    }


    /*
     * Font property
     */

    // we use PROP_FONT_SIZE and these two when we persist PROP_FONT
    private static final String PROP_FONT_STYLE = "fontStyle"; // NOI18N
    private static final String PROP_FONT_FAMILY = "fontFamily"; // NOI18N

    private Font font;

    public Font getFont() {
	return font;
    }

    public void setFont(Font font) {
        this.font = font;
        // recalculate fontSize as well.
        fontSize = this.font.getSize();
        markDirty();
    }

    /*
     * Font size property.
     */
    private static final String PROP_FONT_SIZE = "fontSize"; // NOI18N

    private int fontSize;

    public int getFontSize() {
	return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;

        // recalculate font as well.
        font = new Font(font.getFamily(),
                        font.getStyle(),
                        this.fontSize);
        markDirty();
    }

    /*
     * Foreground color property.
     */
    private static final String PROP_FOREGROUND = "foreground"; // NOI18N

    private Color foreground;

    public Color getForeground() {
	return foreground;
    } 
    public void setForeground(Color foreground) {
	this.foreground = foreground;
        markDirty();
    } 

    /*
     * Background color property.
     */
    private static final String PROP_BACKGROUND = "background"; // NOI18N

    private Color background;

    public Color getBackground() {
	return background;
    } 
    public void setBackground(Color background) {
	this.background = background;
        markDirty();
    } 

    /*
     * Selection background color property.
     */
    private static final String PROP_SELECTION_BACKGROUND =
	"selectionBackground"; // NOI18N

    private Color selectionBackground;

    public Color getSelectionBackground() {
	return selectionBackground;
    } 
    public void setSelectionBackground(Color selectionBackground) {
	this.selectionBackground = selectionBackground;
        markDirty();
    } 

    /*
     * History Size property.
     */
    private static final String PROP_HISTORY_SIZE = "historySize"; // NOI18N

    private int historySize;

    public int getHistorySize() {
	return historySize;
    } 
    public void setHistorySize(int historySize) {
        this.historySize = historySize;
        markDirty();
    } 

    /*
     * Tab Size property.
     */
    private static final String PROP_TAB_SIZE = "tabSize"; // NOI18N

    private int tabSize;

    public int getTabSize() {
	return tabSize;
    } 
    public void setTabSize(int tabSize) {
	this.tabSize = tabSize;
        markDirty();
    } 
    
    /*
     * Select-by-word-delimiters property
     */
    private static final String PROP_SELECT_BY_WORD_DELIMITERS = "selectByWordDelimiters";   // NOI18N
    
    private String selectByWordDelimiters;

    public String getSelectByWordDelimiters() {
        return selectByWordDelimiters;
    }

    public void setSelectByWordDelimiters(String selectByWordDelimiters) {
        this.selectByWordDelimiters = selectByWordDelimiters;
        markDirty();
    }
    
    /*
     * Click-to-type property.
     */
    private static final String PROP_CLICK_TO_TYPE = "clickToType"; // NOI18N

    private boolean clickToType;

    public boolean getClickToType() {
	return clickToType;
    } 
    public void setClickToType(boolean clickToType) {
	this.clickToType = clickToType;
        markDirty();
    } 

    /*
     * Scroll on input property.
     */
    private static final String PROP_SCROLL_ON_INPUT =
	"scrollOnInput"; // NOI18N

    private boolean scrollOnInput;

    public boolean getScrollOnInput() {
	return scrollOnInput;
    } 
    public void setScrollOnInput(boolean scrollOnInput) {
	this.scrollOnInput = scrollOnInput;
        markDirty();
    } 


    /*
     * Scroll on output property.
     */
    private static final String PROP_SCROLL_ON_OUTPUT =
	"scrollOnOutput"; // NOI18N

    private boolean scrollOnOutput;

    public boolean getScrollOnOutput() {
	return scrollOnOutput;
    } 
    public void setScrollOnOutput(boolean scrollOnOutput) {
	this.scrollOnOutput = scrollOnOutput;
        markDirty();
    } 

    /*
     * Line wrap property.
     */
    private static final String PROP_LINE_WRAP = "lineWrap"; // NOI18N

    private boolean lineWrap;

    public boolean getLineWrap() {
	return lineWrap;
    } 
    public void setLineWrap(boolean lineWrap) {
	this.lineWrap = lineWrap;
        markDirty();
    } 
    
    /*
     * Ignore keymap property.
     */
    private static final String PROP_IGNORE_KEYMAP = "ignoreKeymap"; // NOI18N

    private boolean ignoreKeymap;

    public boolean getIgnoreKeymap() {
	return ignoreKeymap;
    } 
    public void setIgnoreKeymap(boolean ignoreKeymap) {
	this.ignoreKeymap = ignoreKeymap;
        markDirty();
    } 

    /*
     * Alt sends ESC property
     */
    private static final String PROP_ALT_SENDS_ESCAPE = "altSendsEscape"; // NOI18N

    private boolean altSendsEscape;

    public boolean getAltSendsEscape() {
	return altSendsEscape;
    } 
    public void setAltSendsEscape(boolean altSendsEscape) {
	this.altSendsEscape = altSendsEscape;
        markDirty();
    } 

    private void markDirty() {
        pcs.firePropertyChange(null, null, null);
        if (preferences == null) {
            dirty = false;
            return;
        }
        dirty = !preferences.get(PREFIX + PROP_FONT_FAMILY, fontDefault.getFamily()).equals(font.getFamily())
                || preferences.getInt(PREFIX + PROP_FONT_STYLE, fontDefault.getStyle()) != font.getStyle()
                || preferences.getInt(PREFIX + PROP_FONT_SIZE, fontSizeDefault) != fontSize
                || preferences.getInt(PREFIX + PROP_TAB_SIZE, tabSizeDefault) != tabSize
                || preferences.getInt(PREFIX + PROP_HISTORY_SIZE, historySizeDefault) != historySize
                || preferences.getInt(PREFIX + PROP_FOREGROUND, foregroundDefault.getRGB()) != foreground.getRGB()
                || preferences.getInt(PREFIX + PROP_BACKGROUND, backgroundDefault.getRGB()) != background.getRGB()
                || preferences.getInt(PREFIX + PROP_SELECTION_BACKGROUND, selectionBackgroundDefault.getRGB()) != selectionBackground.getRGB()
                || !preferences.get(PREFIX + PROP_SELECT_BY_WORD_DELIMITERS, selectByWordDelimitersDefault).equals(selectByWordDelimiters)
                || preferences.getBoolean(PREFIX + PROP_CLICK_TO_TYPE, clickToTypeDefault) != clickToType
                || preferences.getBoolean(PREFIX + PROP_SCROLL_ON_INPUT, scrollOnInputDefault) != scrollOnInput
                || preferences.getBoolean(PREFIX + PROP_SCROLL_ON_OUTPUT, scrollOnOutputDefault) != scrollOnOutput
                || preferences.getBoolean(PREFIX + PROP_LINE_WRAP, lineWrapDefault) != lineWrap
                || preferences.getBoolean(PREFIX + PROP_IGNORE_KEYMAP, ignoreKeymapDefault) != ignoreKeymap
                || preferences.getBoolean(PREFIX + PROP_ALT_SENDS_ESCAPE, altSendsEscapeDefault) != altSendsEscape;
    }
}
