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
/*
 * Contributors: Maxym Mykhalchuk
 */
package org.openide.awt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.util.Utilities;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.UIManager;


/**
 * Support class for setting button, menu, and label text strings with mnemonics.
 * @author Maxym Mykhalchuk
 * @since 3.37
 * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=26640">Issue #26640</a>
 */
public final class Mnemonics extends Object {
    /** Private constructor in order that this class is never instantiated. */
    private Mnemonics() {
    }

    /**
     * Actual setter of the text & mnemonics for the AbstractButton/JLabel or
     * their subclasses.
     * @param item AbstractButton/JLabel
     * @param text new label
     */
    private static void setLocalizedText2(Object item, String text) {
        // #17664. Handle null text also.
        // & in HTML should be ignored
        if (text == null) { // NOI18N
            setText(item, null);

            return;
        }

        int i = findMnemonicAmpersand(text);

        setMnemonicIndex(item, -1);
        if (i < 0) {
            // no '&' - don't set the mnemonic
            setText(item, text);
            setMnemonic(item, 0);
        } else {
            setText(item, text.substring(0, i) + text.substring(i + 1));
            //#67807 no mnemonics on macosx
            if (isAquaLF()) {
                setMnemonic(item, 0);
            } else {
                char ch = text.charAt(i + 1);
                if (text.startsWith("<html>")) { // NOI18N
                    // Workaround for JDK bug #6510775
                    setText(item, text.substring(0, i) + "<u>" + ch + "</u>" + text.substring(i + 2)); // NOI18N
                    i += 3; // just in case it gets fixed
                }
                if (((ch >= 'A') && (ch <= 'Z')) || ((ch >= 'a') && (ch <= 'z')) || ((ch >= '0') && (ch <= '9'))) {
                    // it's latin character or arabic digit,
                    // setting it as mnemonics
                    setMnemonic(item, ch);
                    
                    // If it's something like "Save &As", we need to set another
                    // mnemonic index (at least under 1.4 or later)
                    // see #29676
                    setMnemonicIndex(item, i);
                } else {
                    // it's non-latin, getting the latin correspondance
                    try {
                        int latinCode = getLatinKeycode(ch);
                        setMnemonic(item, latinCode);
                        setMnemonicIndex(item, i);
                    } catch (MissingResourceException e) {
                        Logger.getLogger(Mnemonics.class.getName()).info("Mapping from a non-Latin character '"+ch+ 
                                "' not found in a localized (branded) version of "+
                                "openide/awt/src/org/openide/awt/Mnemonics.properties - "+
                                "mnemonic cannot be assigned in "+text);
                    }
                }
            }
        }
    }

    /**
     * Sets the text for a menu item or other subclass of AbstractButton.
     * <p>Examples:</p>
     * <table cellspacing="2" cellpadding="3" border="1">
     *   <tr><th>Input String</th>                                   <th>View under JDK 1.4 or later</th></tr>
     *   <tr><td><code>Save &amp;As<code></td>                       <td>Save <u>A</u>s</td></tr>
     *   <tr><td><code>Rock &amp; Roll<code></td>                    <td>Rock &amp; Roll</td></tr>
     *   <tr><td><code>Drag &amp; &amp;Drop<code></td>               <td>Drag &amp; <u>D</u>rop</td></tr>
     *   <tr><td><code>&amp;&#1060;&#1072;&#1081;&#1083;</code></td> <td><u>&#1060;</u>&#1072;&#1081;&#1083;</td></tr>
     * </table>
     * @param item a button whose text will be changed
     * @param text new label
     */
    public static void setLocalizedText(AbstractButton item, String text) {
        setLocalizedText2(item, text);
    }

    /**
     * Sets the text for the label or other subclass of JLabel.
     * For details see {@link #setLocalizedText(AbstractButton, String)}.
     * @param item a label whose text will be set
     * @param text new label
     */
    public static void setLocalizedText(JLabel item, String text) {
        setLocalizedText2(item, text);
    }

    /**
     * Searches for an ampersand in a string which indicates a mnemonic.
     * Recognizes the following cases:
     * <ul>
     * <li>"Drag & Drop", "Ampersand ('&')" - don't have mnemonic ampersand.
     *      "&" is not found before " " (space), or if enclosed in "'"
     *     (single quotation marks).
     * <li>"&File", "Save &As..." - do have mnemonic ampersand.
     * <li>"Rock & Ro&ll", "Underline the '&' &character" - also do have
     *      mnemonic ampersand, but the second one.
     * <li>"&lt;html&gt;&lt;b&gt;R&amp;amp;D&lt;/b&gt; departmen&amp;t" - has mnemonic 
     *      ampersand before "t".
     *      Ampersands in HTML texts that are part of entity are ignored.
     * </ul>
     * @param text text to search
     * @return the position of mnemonic ampersand in text, or -1 if there is none
     */
    public static int findMnemonicAmpersand(String text) {
        int i = -1;
        boolean isHTML = text.startsWith("<html>");

        do {
            // searching for the next ampersand
            i = text.indexOf('&', i + 1);

            if ((i >= 0) && ((i + 1) < text.length())) {
                if (isHTML) {
                    boolean startsEntity = false;
                    for (int j = i + 1; j < text.length(); j++) {
                        char c = text.charAt(j);
                        if (c == ';') { 
                            startsEntity = true;
                            break;
                        }
                        if (!Character.isLetterOrDigit(c)) {
                            break;
                        }
                    }
                    if (!startsEntity) {
                        return i;
                    }
                }
                else {
                    // before ' '
                    if (text.charAt(i + 1) == ' ') {
                        continue;

                        // before ', and after '
                    } else if ((text.charAt(i + 1) == '\'') && (i > 0) && (text.charAt(i - 1) == '\'')) {
                        continue;
                    }

                    // ampersand is marking mnemonics
                    return i;
                }
            }
        } while (i >= 0);

        return -1;
    }

    /**
     * Gets the Latin symbol which corresponds
     * to some non-Latin symbol on the localized keyboard.
     * The search is done via lookup of Resource bundle
     * for pairs having the form (e.g.) <code>MNEMONIC_\u0424=A</code>.
     * @param localeChar non-Latin character or a punctuator to be used as mnemonic
     * @return character on latin keyboard, corresponding to the locale character,
     *         or the appropriate VK_*** code (if there's no latin character
     *         "under" the non-Latin one
     */
    private static int getLatinKeycode(char localeChar) throws MissingResourceException {
        // associated should be a latin character, arabic digit 
        // or an integer (KeyEvent.VK_***)
        String str = getBundle().getString("MNEMONIC_" + localeChar); // NOI18N

        if (str.length() == 1) {
            return str.charAt(0);
        } else {
            return Integer.parseInt(str);
        }
    }

    /**
     * Wrapper for the
     * <code>AbstractButton.setMnemonicIndex</code> or
     * <code>JLabel.setDisplayedMnemonicIndex</code> method.
     * @param item AbstractButton/JLabel or subclasses
     * @param index Index of the Character to underline under JDK1.4
     * @param latinCode Latin Character Keycode to underline under JDK1.3
     */
    private static void setMnemonicIndex(Object item, int index) {
        if (item instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) item;
            b.putClientProperty(PROP_DISPLAYED_MNEMONIC_INDEX, index);
            b.removePropertyChangeListener(PROP_DISPLAYED_MNEMONIC_INDEX, MNEMONIC_INDEX_LISTENER);
            b.setDisplayedMnemonicIndex(index);
            b.addPropertyChangeListener(PROP_DISPLAYED_MNEMONIC_INDEX, MNEMONIC_INDEX_LISTENER);
        } else if (item instanceof JLabel) {
            ((JLabel) item).setDisplayedMnemonicIndex(index);
        }
    }
    private static final String PROP_TEXT = "text"; // NOI18N
    private static final String PROP_MNEMONIC = "mnemonic"; // NOI18N
    private static final String PROP_DISPLAYED_MNEMONIC_INDEX = "displayedMnemonicIndex"; // NOI18N
    private static final PropertyChangeListener MNEMONIC_INDEX_LISTENER = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            AbstractButton b = (AbstractButton) evt.getSource();
            if (b.getDisplayedMnemonicIndex() == -1) {
                Integer mnemonic = (Integer) b.getClientProperty(PROP_MNEMONIC);
                Integer index = (Integer) b.getClientProperty(PROP_DISPLAYED_MNEMONIC_INDEX);
                if (mnemonic != null && index != null && Utilities.compareObjects(b.getText(), b.getClientProperty(PROP_TEXT))) {
                    b.setMnemonic(mnemonic);
                    b.setDisplayedMnemonicIndex(index);
                }
            }
        }
    };

    /**
     * Wrapper for AbstractButton/JLabel.setText
     * @param item AbstractButton/JLabel
     * @param text the text to set
     */
    private static void setText(Object item, String text) {
        if (item instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) item;
            b.putClientProperty(PROP_TEXT, text);
            b.setText(text);
        } else {
            ((JLabel) item).setText(text);
        }
    }

    /**
     * Wrapper for AbstractButton.setMnemonic and JLabel.setDisplayedMnemonic
     * @param item AbstractButton/JLabel
     * @param mnem Mnemonic char to set, latin [a-z,A-Z], digit [0-9], or any VK_ code
     */
    private static void setMnemonic(Object item, int mnem) {
        if (isAquaLF()) {
            // there shall be no mnemonics on macosx.
            //#55864
            return;
        }

        if ((mnem >= 'a') && (mnem <= 'z')) {
            mnem = mnem + ('A' - 'a');
        }

        if (item instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) item;
            b.putClientProperty(PROP_MNEMONIC, mnem);
            b.setMnemonic(mnem);
        } else {
            ((JLabel) item).setDisplayedMnemonic(mnem);
        }
    }

    /**
     * Getter for the used Resource bundle (org.openide.awt.Mnemonics).
     * Used to avoid calling </code>ResourceBundle.getBundle(...)</code>
     * many times in defferent places of the code.
     * Does no caching, it's simply an utility method.
     */
    private static ResourceBundle getBundle() {
        return ResourceBundle.getBundle("org.openide.awt.Mnemonics"); // NOI18N
    }
    
    static boolean isAquaLF() {
        return "Aqua".equals( UIManager.getLookAndFeel().getID() ); //NOI18N
    }
}
