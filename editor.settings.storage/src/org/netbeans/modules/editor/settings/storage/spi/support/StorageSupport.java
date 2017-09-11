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
     * 
     * <li><b>Human readable</b> - this format encodes a <code>KeyStroke</code> to
     *   a string that looks like for example 'Ctrl+A' or 'Alt+Shift+M'.
     * <li><b>Emacs style</b> - this format encodes a <code>KeyStroke</code> to
     *   a string that's known from Emacs and that looks like for example 'C-A' or 'AS-M'.
     *   It uses methods from <code>org.openide.util.Utilities</code>, which take
     *   care of Mac OS specifics and use 'D' and 'O' wildcards for encoding 'Ctrl'
     *   and 'Alt' keys.
     * 
     * @param keys The <code>KeyStrokes</code> to convert.
     * @param emacsStyle If <code>true</code> the returned string will be in so called
     *   Emacs style, ortherwise it will be in human readable format.
     * 
     * @return The textual representation of <code>KeyStroke</code>s passed in.
     * @since 1.16
     * @deprecated Use {@link KeyStrokeUtils#getKeyStrokesAsText} or {@link Utilities#keyToString(javax.swing.KeyStroke[], java.lang.String, boolean)}
     */
    public static String keyStrokesToString(Collection<? extends KeyStroke> keys, boolean emacsStyle) {
        if (!emacsStyle) {
            return KeyStrokeUtils.getKeyStrokesAsText(keys.toArray(new KeyStroke[keys.size()]), " "); // NOI18N
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
     * objects. Please see {@link #keyStrokesToString(Collection<KeyStroke>, boolean)}
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
