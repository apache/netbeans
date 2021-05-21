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
package org.netbeans.modules.payara.common.ui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Document filters for Payara instance forms.
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
