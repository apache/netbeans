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
package org.netbeans.api.search.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import static java.awt.event.HierarchyEvent.DISPLAYABILITY_CHANGED;
import java.awt.event.HierarchyListener;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JComboBox;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.search.RegexpUtil;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.modules.search.FindDialogMemory;
import org.netbeans.modules.search.ListComboBoxModel;
import org.netbeans.modules.search.ui.PatternChangeListener;
import org.netbeans.modules.search.ui.TextFieldFocusListener;
import org.netbeans.modules.search.ui.UiUtils;
import org.openide.util.Exceptions;

/**
 * Component controller for specifying file name pattern.
 *
 * Use {@link ComponentUtils} to create instances of this class.
 *
 * @author jhavlin
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class FileNameController extends ComponentController<JComboBox> {

    private FileNamePatternWatcher fileNamePatternWatcher;
    private JTextComponent fileNamePatternEditor;
    private boolean regexp = false;
    /**
     * When set to {@link true}, changes of file name pattern are ignored. This
     * is needed when the text in the file name pattern is programatically (i.e.
     * not by the user) set to "(all files)" and when this text is cleared (when
     * the text field gets focus).
     */
    private boolean ignoreFileNamePatternChanges;
    private boolean patternValid;
    private Color defaultColor;

    FileNameController(JComboBox jComboBox) {
        super(jComboBox);
        init();
    }

    private void init() {

        Component cboxEditorComp = component.getEditor().getEditorComponent();
        fileNamePatternEditor = (JTextComponent) cboxEditorComp;
        fileNamePatternWatcher =
                new FileNamePatternWatcher(fileNamePatternEditor);
        fileNamePatternEditor.addFocusListener(fileNamePatternWatcher);
        fileNamePatternEditor.addHierarchyListener(fileNamePatternWatcher);
        fileNamePatternEditor.getDocument().addDocumentListener(
                new FileNameChangeListener());
        defaultColor = component.getForeground();
        component.setEditable(true);
        List<String> entries = FindDialogMemory.getDefault().getFileNamePatterns();
        if (!entries.isEmpty()) {
            component.setModel(new ListComboBoxModel<String>(entries, true));
        }
    }

    /**
     * Get pattern for matching file names.
     */
    public String getFileNamePattern() {
        if (isAllFilesInfoDisplayed()) {
            return "";                                                  //NOI18N
        } else {
            return fileNamePatternEditor.getText();
        }
    }

    /**
     * Set file name pattern.
     */
    public void setFileNamePattern(String pattern) {
        component.setSelectedItem(pattern);
    }

    /**
     * Tells whether the contained expression should be interpreted as a simple
     * pattern or a regular expression pattern.
     *
     * @return True if the contained string stands for a regula expressiong,
     * false if it stands for a simple pattern.
     */
    public boolean isRegularExpression() {
        return this.regexp;
    }

    /**
     * Tell whether gray (all files) text is currently displayed.
     */
    public boolean isAllFilesInfoDisplayed() {
        return fileNamePatternWatcher.infoDisplayed;
    }

    /**
     * Sets whether the contained expression should be interpreted as a simple
     * pattern or a regular expression pattern.
     *
     * @param regularExpression True if the contained string is a regular
     * expression, false if it is a simple pattern.
     */
    public void setRegularExpression(boolean regularExpression) {
        this.regexp = regularExpression;
        setFileNamePatternToolTip();
        patternChanged();
    }

    /**
     * Display (all files) text.
     */
    public void displayAllFilesInfo() {
        fileNamePatternWatcher.displayInfo();
    }

    /**
     * Hide (all files) text.
     */
    public void hideAllFilesInfo() {
        fileNamePatternWatcher.hideInfo();
    }

    private void setFileNamePatternToolTip() {
        component.setToolTipText(UiUtils.getFileNamePatternsExample(regexp));
    }

    /**
     * Extension of the {@code TextFieldFocusListener} - besides selecting of
     * all text upon focus gain, it displays &quot;(no files)&quot; if no file
     * name pattern is specified.
     *
     * @author Marian Petras
     */
    private final class FileNamePatternWatcher extends TextFieldFocusListener
            implements HierarchyListener {

        private final JTextComponent txtComp;
        private final Document doc;
        private Color foregroundColor;
        private String infoText;
        private boolean infoDisplayed;
        private final Logger watcherLogger =
                Logger.getLogger(this.getClass().getName());

        private FileNamePatternWatcher(JTextComponent txtComp) {
            this.txtComp = txtComp;
            doc = txtComp.getDocument();
        }

        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            if ((e.getComponent() != txtComp)
                    || ((e.getChangeFlags() & DISPLAYABILITY_CHANGED) == 0)
                    || !txtComp.isDisplayable()) {
                return;
            }

            watcherLogger.finer("componentShown()");                    //NOI18N
            if (foregroundColor == null) {
                foregroundColor = txtComp.getForeground();
            }
            if ((doc.getLength() == 0) && !txtComp.isFocusOwner()) {
                displayInfo();
            }
        }

        @Override
        public void focusGained(FocusEvent e) {

            /*
             * Order of method calls hideInfo() and super.focusGained(e) is
             * important! See bug #113202.
             */

            if (infoDisplayed) {
                hideInfo();
            }
            super.focusGained(e);   //selects all text
        }

        @Override
        public void focusLost(FocusEvent e) {
            super.focusLost(e);     //does nothing
            if (isEmptyText()) {
                displayInfo();
            }
        }

        private boolean isEmptyText() {
            int length = doc.getLength();
            if (length == 0) {
                return true;
            }

            String text;
            try {
                text = doc.getText(0, length);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                text = null;
            }
            return (text != null) && (text.trim().length() == 0);
        }

        private void displayInfo() {
            assert (isEmptyText() && !txtComp.isFocusOwner());
            watcherLogger.finer("displayInfo()");                       //NOI18N

            try {
                txtComp.setForeground(txtComp.getDisabledTextColor());

                ignoreFileNamePatternChanges = true;
                doc.remove(0, doc.getLength());
                doc.insertString(0, getInfoText(), null);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                ignoreFileNamePatternChanges = false;
                infoDisplayed = true;
            }
        }

        private void hideInfo() {
            watcherLogger.finer("hideInfo()");                          //NOI18N

            txtComp.setEnabled(true);
            try {
                ignoreFileNamePatternChanges = true;
                if (doc.getText(0, doc.getLength()).equals(getInfoText())) {
                    doc.remove(0, doc.getLength());
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                ignoreFileNamePatternChanges = false;
                txtComp.setForeground(foregroundColor);
                infoDisplayed = false;
            }
        }

        private String getInfoText() {
            if (infoText == null) {
                infoText = UiUtils.getText(
                        "BasicSearchForm.cboxFileNamePattern.allFiles");//NOI18N
            }
            return infoText;
        }
    }

    private final class FileNameChangeListener extends PatternChangeListener {

        @Override
        public void handleComboBoxChange(String text) {
            patternChanged();
        }
    }

    private void patternChanged() {
        if (!ignoreFileNamePatternChanges) {
            updateFileNamePatternColor();
            fireChange();
        }
    }

    /**
     * Sets proper color of file pattern.
     */
    private void updateFileNamePatternColor() {

        boolean wasInvalid = patternValid;
        String pattern = getFileNamePattern();

        if (pattern == null || pattern.isEmpty()) {
            patternValid = true;
        } else {
            try {
                Pattern p = RegexpUtil.makeFileNamePattern(
                        SearchScopeOptions.create(getFileNamePattern(), regexp));
                if (p == null) {
                    patternValid = false;
                } else {
                    patternValid = true;
                }
            } catch (PatternSyntaxException e) {
                patternValid = false;
            }
        }
        if (patternValid != wasInvalid && !isAllFilesInfoDisplayed()) {
            fileNamePatternEditor.setForeground(
                    patternValid ? defaultColor : UiUtils.getErrorTextColor());
        }
    }
}
