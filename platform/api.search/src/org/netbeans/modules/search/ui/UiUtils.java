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
package org.netbeans.modules.search.ui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import org.netbeans.modules.search.BasicSearchProvider;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**
 *
 * @author jhavlin
 */
public class UiUtils {

    private static Color ERROR_COLOR = null;
    public static final String HTML_LINK_PREFIX =
            "<html><u><a href=\"#\">";                                  //NOI18N
    public static final String HTML_LINK_SUFFIX = "</a></u></html>";    //NOI18N

    public static final Transferable DISABLE_TRANSFER = new Transferable() {
        private final DataFlavor[] NO_FLAVOR = new DataFlavor[0];

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return NO_FLAVOR;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return false;
        }

        @Override
        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            return null;
        }
    };

    public static Color getErrorTextColor() {

        assert EventQueue.isDispatchThread();

        if (ERROR_COLOR == null) {
            ERROR_COLOR = UIManager.getDefaults().getColor(
                    "TextField.errorForeground");                       //NOI18N
            if (ERROR_COLOR == null) {
                ERROR_COLOR = Color.RED;
            }
        }
        return ERROR_COLOR;
    }

    public static String getText(String bundleKey) {
        return NbBundle.getMessage(BasicSearchProvider.class, bundleKey);
    }

    public static String getHtmlLink(String key) {
        return HTML_LINK_PREFIX + getText(key) + HTML_LINK_SUFFIX;
    }

    /**
     * Convenience method for setting localized text and mnemonics of buttons.
     */
    public static void lclz(AbstractButton obj, String key) {
        Mnemonics.setLocalizedText(obj, getText(key));
    }

    /**
     * Convenience method for setting localized text and mnemonics of labels
     */
    public static void lclz(JLabel obj, String key) {
        Mnemonics.setLocalizedText(obj, getText(key));
    }

    /**
     * Escape HTML special characters in string.
     */
    public static String escapeHtml(String s) {
        if (s == null) {
            return null;
        } else {
            try {
                return XMLUtil.toElementContent(s);
            } catch (CharConversionException cce) {
                return s;
            }
        }
    }

    /**
     * Get an example of file name patterns.
     *
     * @param regexp True to get example or regular expression pattern, false to
     * get example of standard pattern.
     */
    public static String getFileNamePatternsExample(boolean regexp) {
        if (regexp) {
            String separator = ("\\".equals(File.separator)) //NOI18N
                    ? "\\\\" //NOI18N
                    : File.separator;
            return NbBundle.getMessage(BasicSearchProvider.class,
                    "BasicSearchForm.cboxFileNamePattern.example.re", //NOI18N
                    separator);
        } else {
            return NbBundle.getMessage(BasicSearchProvider.class,
                    "BasicSearchForm.cboxFileNamePattern.example");     //NOI18N
        }
    }
}
