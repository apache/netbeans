/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common.ui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Document filters for GlassFish instance forms.
 * <p/>
 * @author Tomas Kraus
 */
public class Filter {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Filter out characters that are not valid for network port numbers.
     */
    public static class PortNumber extends DocumentFilter {

        /**
         * Copy valid characters (decimal number digits) from {@link String}
         * into {@link StringBuilder}.
         * <p/>
         * @param src Source {@link String}.
         * @param dst Destination {@link StringBuilder}.
         * @return Destination {@link StringBuilder}.
         */
        private static StringBuilder copyValidChars(final String src,
                final StringBuilder dst) {
            for (int n = 0; n < src.length(); n++) {
                if (Character.isDigit(src.charAt(n))) {
                    dst.append(src.charAt(n));
                }
            }
            return dst;
        }

        /**
         * Invoked prior to insertion of text into the specified Document.
         * <p/>
         * @param fb     FilterBypass that can be used to mutate Document.
         * @param offset The offset into the document to insert
         *               the content >= 0.
         * @param string The string to insert.
         * @param attr   The attributes to associate with the inserted content.
         *               This may be <code>null</code> if there are
         *               no attributes.
         * @exception BadLocationException The given insert position is not a
         *                                  valid position within the document.
         */
        @Override
        public void insertString(final FilterBypass fb, final int offset,
                final String string, final AttributeSet attr)
                throws BadLocationException {
            if (string.length() > 0) {
                StringBuilder sb = copyValidChars(
                        string, new StringBuilder(string.length()));
                if (sb.length() > 0) {
                    super.insertString(fb, offset, sb.toString(), attr);
                }
            }
        }

        /**
         * Invoked prior to replacing a region of text in the specified
         * Document.
         * <p/>
         * @param fb     FilterBypass that can be used to mutate Document.
         * @param offset The offset into the document to replace
         *               the content >= 0.
         * @param length Length of text to delete
         * @param string Text to insert, <code>null</code> indicates no text
         *               to insert
         * @param attrs  The attributes to associate with the inserted content.
         *               This may be <code>null</code> if there are
         *               no attributes.
         * @exception BadLocationException The given insert position is not a
         *                                 valid position within the document.
         */
        @Override
        public void replace(final FilterBypass fb, final int offset,
                final int length, final String string, final AttributeSet attr)
                throws BadLocationException {
            if (string.length() > 0) {
                StringBuilder sb = copyValidChars(
                        string, new StringBuilder(string.length()));
                if (sb.length() > 0) {
                    super.replace(fb, offset, length, sb.toString(), attr);
                }
            }
        }
        
    }

}
