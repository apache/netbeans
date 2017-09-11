/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
