/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.debugger.jpda.models.AbstractObjectVariable;
import org.netbeans.modules.debugger.jpda.models.ShortenedStrings;
import org.netbeans.modules.debugger.jpda.models.ShortenedStrings.StringInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
class BigStringCustomEditor extends JPanel implements ActionListener {
    
    static final int MAX_STRING_LENGTH;

    private final StringInfo shortenedInfo;
    private final String fullString;

    static {
        int maxStringLength = AbstractObjectVariable.MAX_STRING_LENGTH;
        String javaV = System.getProperty("java.version");
        if (javaV.startsWith("1.8.0")) {
            String update = "";
            for (int i = "1.8.0_".length(); i < javaV.length(); i++) {
                char c = javaV.charAt(i);
                if (Character.isDigit(c)) {
                    update += c;
                } else {
                    break;
                }
            }
            int updateNo = 0;
            if (!update.isEmpty()) {
                try {
                    updateNo = Integer.parseInt(update);
                } catch (NumberFormatException nfex) {}
            }
            if (updateNo < 60) {
                // Memory problem on JDK 8, fixed in update 60 (https://bugs.openjdk.java.net/browse/JDK-8072775):
                maxStringLength = 1000;
            }
        }
        MAX_STRING_LENGTH = maxStringLength;
    }
    
    private BigStringCustomEditor(Component delegateCustomEditor, StringInfo shortenedInfo, int preferredShortLength) {
        this.shortenedInfo = shortenedInfo;
        this.fullString = null;
        int shortLength;
        if (preferredShortLength >= 0) {
            shortLength = preferredShortLength;
        } else {
            shortLength = shortenedInfo.getShortLength();
        }
        int fullLength = shortenedInfo.getLength();
        init(delegateCustomEditor, shortLength, fullLength);
    }
    
    private BigStringCustomEditor(Component delegateCustomEditor,
                                  String shortString, String fullString) {
        this.shortenedInfo = null;
        this.fullString = fullString;
        init(delegateCustomEditor, shortString.length(), fullString.length());
    }
    
    static BigStringCustomEditor createIfBig(PropertyEditor propertyEditor, String value) {
        ShortenedStrings.StringInfo shortenedInfo = ShortenedStrings.getShortenedInfo(value);
        if (shortenedInfo != null) {
            if (!(shortenedInfo.getShortLength() > MAX_STRING_LENGTH)) {
                return new BigStringCustomEditor(propertyEditor.getCustomEditor(), shortenedInfo, -1);
            } else {
                String shortText = value.substring(0, MAX_STRING_LENGTH) + "...";
                propertyEditor.setValue(shortText);
                return new BigStringCustomEditor(propertyEditor.getCustomEditor(), shortenedInfo, shortText.length() - 3);
            }
        } else if (value.length() > MAX_STRING_LENGTH) {
            String shortText = value.substring(0, MAX_STRING_LENGTH) + "...";
            propertyEditor.setValue(shortText);
            return new BigStringCustomEditor(propertyEditor.getCustomEditor(), shortText, value);
        } else {
            return null;
        }
    }
    
    private void init(Component contentComponent, int shortLength, int fullLength) {
        setLayout(new java.awt.BorderLayout());
        add(contentComponent, BorderLayout.CENTER);
        add(createSavePanel(shortLength, fullLength), BorderLayout.SOUTH);
    }
    
    @NbBundle.Messages({"# {0} - number of displayed characters,",
                        "# {1} - total number of characters in the string,",
                       "MSG_TooLargeString=The String value is too large. Displaying {0} out of {1} characters."})
    private JPanel createSavePanel(int shortLength, int fullLength) {
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.BorderLayout(5, 5));
        panel.add(new JLabel(Bundle.MSG_TooLargeString(shortLength, fullLength)), BorderLayout.LINE_START);
        panel.add(createSaveButton(), BorderLayout.LINE_END);
        panel.setBorder(new EmptyBorder(10, 12, 0, 11));
        return panel;
    }
    
    @NbBundle.Messages("BTN_SaveValueToFile=&Save Value to File")
    private JButton createSaveButton() {
        JButton button = new JButton();
        Mnemonics.setLocalizedText(button, Bundle.BTN_SaveValueToFile());
        button.addActionListener(this);
        return button;
    }

    @Override
    @NbBundle.Messages("DLG_SaveToFile=Save to File")
    public void actionPerformed(ActionEvent e) {
        // Save
        FileChooserBuilder fchb = new FileChooserBuilder(BigStringCustomEditor.class);
        fchb.setTitle(Bundle.DLG_SaveToFile());
        final File f = fchb.showSaveDialog();
        if (f != null) {
            new RequestProcessor(BigStringCustomEditor.class).post(new Runnable() {
                @Override
                public void run() {
                    save(f);
                }
            });
        }
    }
    
    private void save(File f) {
        Reader r;
        if (shortenedInfo != null) {
            r = shortenedInfo.getContent();
        } else {
            r = new StringReader(fullString);
        }
        Writer w = null;
        try {
            w = new FileWriter(f);
            final char[] BUFFER = new char[65536];
            int len;
            while ((len = r.read(BUFFER)) > 0) {
                w.write(BUFFER, 0, len);
            }
        } catch (IOException ioex) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(ioex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException ex) {}
            }
            try {
                r.close();
            } catch (IOException ex) {}
        }
    }
}
