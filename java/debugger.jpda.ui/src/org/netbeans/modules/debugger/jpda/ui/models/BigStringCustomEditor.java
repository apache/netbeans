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
import org.netbeans.modules.debugger.jpda.models.ShortenedStrings.StringInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
class BigStringCustomEditor extends JPanel implements ActionListener {

    private final StringInfo shortenedInfo;
    private final String fullString;

    private BigStringCustomEditor(Component delegateCustomEditor, StringInfo shortenedInfo, int preferredShortLength) {
        this.shortenedInfo = shortenedInfo;
        this.fullString = null;
        int shortLength;
        if (preferredShortLength >= 0) {
            shortLength = preferredShortLength;
        } else {
            shortLength = shortenedInfo.getShortendLength();
        }
        int fullLength = shortenedInfo.getLength();
        init(delegateCustomEditor, shortLength, fullLength);
    }

    static BigStringCustomEditor createIfBig(PropertyEditor propertyEditor, StringInfo shortenedInfo) {
        if (shortenedInfo != null) {
            if (!(shortenedInfo.getShortendLength() > AbstractObjectVariable.MAX_STRING_LENGTH)) {
                return new BigStringCustomEditor(propertyEditor.getCustomEditor(), shortenedInfo, -1);
            } else {
                propertyEditor.setValue(shortenedInfo.getShortendString());
                return new BigStringCustomEditor(propertyEditor.getCustomEditor(), shortenedInfo, shortenedInfo.getShortendLength() - 3);
            }
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
            new RequestProcessor(BigStringCustomEditor.class).post(() -> {
                save(f);
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
        try (Writer w = new FileWriter(f); r) {
            r.transferTo(w);
        } catch (IOException ioex) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(ioex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }
}
