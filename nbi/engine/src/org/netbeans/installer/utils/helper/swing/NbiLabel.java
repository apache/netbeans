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
package org.netbeans.installer.utils.helper.swing;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JLabel;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiLabel extends JLabel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private boolean collapsePaths;

    private String text;

    public NbiLabel() {
        super();

        setText(null);
        collapsePaths = false;
    }

    public NbiLabel(final boolean collapsePaths) {
        this();
        this.collapsePaths = collapsePaths;
        if (collapsePaths) {
            addComponentListener(new ComponentAdapter() {

                @Override
                public void componentResized(ComponentEvent e) {
                    String shortenedString = shortenString(text);
                    NbiLabel.super.setText(shortenedString);
                    NbiLabel.super.setToolTipText(StringUtils.stripMnemonic(shortenedString));
                }
            });
        }
    }

    public void clearText() {
        setText(null);
    }

    @Override
    public void setText(final String text) {
        if ((text == null) || text.equals("")) {
            this.text = DEFAULT_TEXT;

            super.setText(DEFAULT_TEXT);
            super.setDisplayedMnemonic(DEFAULT_MNEMONIC);
            super.setToolTipText(DEFAULT_TOOLTIP_TEXT);
        } else {
            this.text = text;
            if (collapsePaths) {
                String shortenedString = shortenString(text);
                super.setText(shortenedString);
                super.setToolTipText(StringUtils.stripMnemonic(shortenedString));
            } else {
                super.setText(StringUtils.stripMnemonic(text));
                super.setToolTipText(StringUtils.stripMnemonic(text));
            }

            if (!SystemUtils.isMacOS()) {
                super.setDisplayedMnemonic(StringUtils.fetchMnemonic(text));
            }
        }
    }

    private String shortenString(final String text) {
        final String string = StringUtils.stripMnemonic(text);
        final String separator = SystemUtils.getFileSeparator();
        final int boundsWidth = getBounds().width;
        final int lastIndex = string.lastIndexOf(separator);
        int stringWidth = getStringBounds(getGraphics(), text).width;
        int index = string.lastIndexOf(separator, lastIndex - 1);
        // we should continue while there is at least one separator
        // (lastIndex > -1), there is a previous separator (index > -1) and
        // the rendered string width exceeds the bounds
        // (stringWidth > boundsWidth)
        // note: if there are no separators in the string, it will not be
        // shortened at all and the default shortening procedure will take
        // place, also if collapsing a path does not help completely, additional
        // shortening will be performed by the default procedure
        String shortenedString = string;
        while ((lastIndex != -1) && (index != -1) && (stringWidth > boundsWidth)) {
            shortenedString = StringUtils.replace(string, "...", index + 1, lastIndex);
            stringWidth = getStringBounds(getGraphics(), shortenedString).width;
            index = string.lastIndexOf(separator, index - 1);
        }
        return shortenedString;
    }

    private Rectangle getStringBounds(Graphics graphics, String text) {
        return getFontMetrics(
                getFont()).getStringBounds(text, graphics).getBounds();
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TEXT =
            " "; // NOI18N
    
    public static final String DEFAULT_TOOLTIP_TEXT =
            null;
    
    public static final char DEFAULT_MNEMONIC =
            '\u0000'; // NOMAGI
}
