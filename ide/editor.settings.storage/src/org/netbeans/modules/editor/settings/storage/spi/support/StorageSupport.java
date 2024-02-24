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
package org.netbeans.modules.editor.settings.storage.spi.support;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import org.netbeans.core.options.keymap.api.KeyStrokeUtils;
import org.netbeans.modules.editor.settings.storage.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 * @deprecated Functionality of this Support can be obtained either from {@link KeyStrokeUtils} or from {@link Utilities}.
 */
@Deprecated
public final class StorageSupport {

    private static final Logger LOG = Logger.getLogger(StorageSupport.class.getName());
    private static Map<String, Integer> names;

    private StorageSupport() {

    }

    public static String getLocalizingBundleMessage(FileObject fo, String key, String defaultValue) {
        return Utils.getLocalizedName(fo, key, defaultValue, false);
    }

    /**
     * Converts a list of <code>KeyStroke</code>s to its textual representation. There
     * are two available formats for the textual representation:
     * <ul>
     * <li><b>Human readable</b> - this format encodes a <code>KeyStroke</code> to
     *   a string that looks like for example 'Ctrl+A' or 'Alt+Shift+M'.
     * <li><b>Emacs style</b> - this format encodes a <code>KeyStroke</code> to
     *   a string that's known from Emacs and that looks like for example 'C-A' or 'AS-M'.
     *   It uses methods from <code>org.openide.util.Utilities</code>, which take
     *   care of Mac OS specifics and use 'D' and 'O' wildcards for encoding 'Ctrl'
     *   and 'Alt' keys.
     * </ul>
     * @param keys The <code>KeyStrokes</code> to convert.
     * @param emacsStyle If <code>true</code> the returned string will be in so called
     *   Emacs style, ortherwise it will be in human readable format.
     * 
     * @return The textual representation of <code>KeyStroke</code>s passed in.
     * @since 1.16
     * @deprecated Use {@link KeyStrokeUtils#getKeyStrokesAsText} or {@link Utilities#keyToString(javax.swing.KeyStroke, boolean)}
     */
    public static String keyStrokesToString(Collection<? extends KeyStroke> keys, boolean emacsStyle) {
        if (!emacsStyle) {
            return KeyStrokeUtils.getKeyStrokesAsText(keys.toArray(new KeyStroke[0]), " "); // NOI18N
        }
        StringBuilder sb = new StringBuilder();

        for (Iterator<? extends KeyStroke> it = keys.iterator(); it.hasNext(); ) {
            KeyStroke keyStroke = it.next();
            sb.append(Utilities.keyToString(keyStroke, true));
            if (it.hasNext()) {
                sb.append('$'); //NOI18N
            }
        }

        return sb.toString();
    }

    /**
     * Converts a textual representation of key strokes to an array of <code>KeyStroke</code>
     * objects. Please see {@link #keyStrokesToString(Collection, boolean)}
     * ror details about the available formats.
     * 
     * @param key The textual representation of keystorkes to convert. Its format
     *   depends on the value of <code>emacsStyle</code> parameter.
     * @param emacsStyle If <code>true</code> the <code>key</code> string is expected to be
     *   in so called emacs format, ortherwise it will be in human readable format.
     * 
     * @return The <code>KeyStroke</code>s that were represented by the <code>key</code>
     *   text or <code>null</code> if the textual representation was malformed.
     * @since 1.16
     * @deprecated use {@link KeyStrokeUtils#getKeyStrokes(java.lang.String)} or {@link Utilities#stringToKeys(java.lang.String)}.
     * Note the differences in delimiter used by the two methods.
     */
    public static KeyStroke[] stringToKeyStrokes(String key, boolean emacsStyle) {
        assert key != null : "The parameter key must not be null"; //NOI18N

        if (emacsStyle) {
            return Utilities.stringToKeys(key.replaceAll("\\$", " "));
        }
        
        return KeyStrokeUtils.getKeyStrokes(key);
    }
    
}
