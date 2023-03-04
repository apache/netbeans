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

package org.netbeans.modules.form;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import javax.swing.undo.UndoManager;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

import static org.netbeans.modules.form.CustomCodeData.*;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * GUI panel of the code customizer.
 * 
 * @author Tomas Pavek
 */

class CustomCodeView extends javax.swing.JPanel {

    private CustomCodeData codeData;
    private int lastLocalModifiers = -1;
    private int lastFieldModifiers = -1;

    private boolean changed;

    interface Listener {
        void componentExchanged(String compName);
        void renameInvoked();
        void declarationChanged();
    }

    private Listener controller;

    private DocumentL docListener;

    // flag to recognize user action on JComboBox from our calls
    private boolean ignoreComboAction;

    private static class EditBlockInfo {
        Position position;
        List<EditableLine> lines;
    }

    private static class GuardBlockInfo {
        Position position;
        String customizedCode;
        boolean customized;
    }

    private Map<CodeCategory, EditBlockInfo[]> editBlockInfos;
    private Map<CodeCategory, GuardBlockInfo[]> guardBlockInfos;

    private static final String UNDO_MANAGER_PROP = "undo-manager"; // from BaseDocument // NOI18N

    // -----

    CustomCodeView(Listener controller) {
        this.controller = controller;

        initComponents();

        variableCombo.setModel(new DefaultComboBoxModel(variableStrings));
        accessCombo.setModel(new DefaultComboBoxModel(accessStrings));

        // create gutter panels - let their layout share the component map so
        // they have the same width
        Map<Component, Position> positions = new HashMap<Component, Position>();
        initGutter = new JPanel();
        initGutter.setLayout(new GutterLayout(initCodeEditor, positions));
        declareGutter = new JPanel();
        declareGutter.setLayout(new GutterLayout(declareCodeEditor, positions));
        jScrollPane1.setRowHeaderView(initGutter);
        jScrollPane2.setRowHeaderView(declareGutter);
//        jScrollPane1.setBorder(null);
//        jScrollPane2.setBorder(null);
    }

    private javax.swing.JEditorPane createCodeEditorPane() {
        FormServices services = Lookup.getDefault().lookup(FormServices.class);
        return services.createCodeEditorPane();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        initCodeEditor.requestFocusInWindow();
    }

    boolean isChanged() {
        return changed;
    }

    void setComponentNames(String[] compNames) {
        componentCombo.setModel(new DefaultComboBoxModel(compNames));
    }

    void setCodeData(String componentName, CustomCodeData codeData, FileObject srcFile, int[] positions) {
        if (this.codeData != null) { // clean up
            initCodeEditor.getDocument().removeDocumentListener(docListener);
            declareCodeEditor.getDocument().removeDocumentListener(docListener);

            initGutter.removeAll();
            declareGutter.removeAll();

            revalidate();
            repaint();
        }

        if (editBlockInfos != null) {
            editBlockInfos.clear();
            guardBlockInfos.clear();
        }
        else {
            editBlockInfos = new HashMap<CodeCategory, EditBlockInfo[]>();
            guardBlockInfos = new HashMap<CodeCategory, GuardBlockInfo[]>();
        }

        FormUtils.setupEditorPane(initCodeEditor, srcFile, positions[0]);
        FormUtils.setupEditorPane(declareCodeEditor, srcFile, positions[1]);

        this.codeData = codeData;
        selectInComboBox(componentCombo, componentName);

        buildCodeView(CodeCategory.CREATE_AND_INIT);
        buildCodeView(CodeCategory.DECLARATION);

        Object um = initCodeEditor.getDocument().getProperty(UNDO_MANAGER_PROP);
        if (um instanceof UndoManager) {
            ((UndoManager)um).discardAllEdits();
        }
        um = declareCodeEditor.getDocument().getProperty(UNDO_MANAGER_PROP);
        if (um instanceof UndoManager) {
            ((UndoManager)um).discardAllEdits();
        }

        VariableDeclaration decl = codeData.getDeclarationData();
        boolean local = decl.local;
        for (int i=0; i < variableValues.length; i++) {
            if (variableValues[i] == local) {
                selectInComboBox(variableCombo, variableStrings[i]);
                break;
            }
        }
        int modifiers = decl.modifiers;
        int access = modifiers & (Modifier.PRIVATE|Modifier.PROTECTED|Modifier.PUBLIC);
        for (int i=0; i < accessValues.length; i++) {
            if (accessValues[i] == access) {
                selectInComboBox(accessCombo, accessStrings[i]);
                break;
            }
        }
        staticCheckBox.setSelected((modifiers & Modifier.STATIC) == Modifier.STATIC);
        finalCheckBox.setSelected((modifiers & Modifier.FINAL) == Modifier.FINAL);
        transientCheckBox.setSelected((modifiers & Modifier.TRANSIENT) == Modifier.TRANSIENT);
        volatileCheckBox.setSelected((modifiers & Modifier.VOLATILE) == Modifier.VOLATILE);
        accessCombo.setEnabled(!local);
        staticCheckBox.setEnabled(!local);
        transientCheckBox.setEnabled(!local);
        volatileCheckBox.setEnabled(!local);

        if (local)
            lastLocalModifiers = modifiers;
        else
            lastFieldModifiers = modifiers;

        changed = false;

        if (docListener == null)
            docListener = new DocumentL();
        initCodeEditor.getDocument().addDocumentListener(docListener);
        declareCodeEditor.getDocument().addDocumentListener(docListener);

        initCodeEditor.setCaretPosition(0);
        declareCodeEditor.setCaretPosition(0);
    }

    private void buildCodeView(CodeCategory category) {
        editBlockInfos.put(category, new EditBlockInfo[codeData.getEditableBlockCount(category)]);
        int gCount = codeData.getGuardedBlockCount(category);
        guardBlockInfos.put(category, new GuardBlockInfo[gCount]);

        try {
            for (int i=0; i < gCount; i++) {
                addEditableCode(category, i);
                addGuardedCode(category, i);
            }
            if (gCount > 0)
                addEditableCode(category, gCount);
        }
        catch (BadLocationException ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        getEditor(category).setEnabled(gCount > 0);
    }

    private void addEditableCode(CodeCategory category, int blockIndex)
        throws BadLocationException
    {
        Document doc = getDocument(category);
        EditableBlock eBlock = codeData.getEditableBlock(category, blockIndex);
        boolean lastBlock = blockIndex+1 == codeData.getEditableBlockCount(category);
        List<EditableLine> lineList = new LinkedList<EditableLine>();
        int startIndex = doc.getLength();
        boolean needLineEnd = false;

        CodeEntry[] entries = eBlock.getEntries();
        for (int i=0; i < entries.length; i++) {
            CodeEntry e = entries[i];
            String code = e.getCode();
            if (code == null)
                continue;

            // process lines of the code entry
            int lineStart = 0;
            int codeLength = code.length();
            for (int j=0; j < codeLength; j++) {
                char c = code.charAt(j);
                // TODO: filter out subsequent empty lines?
                if (c == '\n' || j+1 == codeLength) { // end of line
                    if (needLineEnd) // previous line not ended by '\n'
                        doc.insertString(doc.getLength(), "\n", null); // NOI18N

                    boolean lastLine = lastBlock && i+1 == entries.length && j+1 == codeLength;
                    needLineEnd = c != '\n' && !lastLine; // missing '\n' - will add it later when needed
                    int lineEnd = c == '\n' && lastLine ? j : j+1; // skip '\n' for very last line
                    int index = doc.getLength();
                    doc.insertString(index, code.substring(lineStart, lineEnd), null);
                    Position pos = NbDocument.createPosition(doc, index, Position.Bias.Backward);
                    lineList.add(new EditableLine(pos, eBlock, i, lineList));

                    lineStart = j + 1;
                }
            }
        }

        if (lineList.size() > 0) {
            if (needLineEnd) // last line of the block not ended by '\n'
                doc.insertString(doc.getLength(), "\n", null); // NOI18N
        }
        else { // no code in whole block - add one empty line
            int index = doc.getLength();
            if (!lastBlock)
                doc.insertString(index, "\n", null); // NOI18N
            Position pos = NbDocument.createPosition(doc, index, Position.Bias.Backward);
            lineList.add(new EditableLine(pos, eBlock, eBlock.getPreferredEntryIndex(), lineList));
        }

        updateGutterComponents(lineList, doc, startIndex, doc.getLength());

        EditBlockInfo eInfo = new EditBlockInfo();
        eInfo.position = lineList.get(0).getPosition();
        eInfo.lines = lineList;
        getEditInfos(category)[blockIndex] = eInfo;
    }

    private void addGuardedCode(CodeCategory category, int blockIndex)
        throws BadLocationException
    {
        StyledDocument doc = (StyledDocument) getDocument(category);
        GuardedBlock gBlock = codeData.getGuardedBlock(category, blockIndex);
        GuardBlockInfo gInfo = new GuardBlockInfo();
        int index = doc.getLength();
        if (gBlock.isCustomized()) {
            String code = gBlock.getCustomCode();
            doc.insertString(index, code, null);
            if (!code.endsWith("\n")) { // NOI18N
                doc.insertString(doc.getLength(), "\n", null); // NOI18N
            }
            int header = gBlock.getHeaderLength();
            int footer = gBlock.getFooterLength();
            NbDocument.markGuarded(doc, index, header);
            NbDocument.markGuarded(doc, doc.getLength() - footer, footer);
            gInfo.customized = true;
        }
        else {
            String code = gBlock.getDefaultCode();
            doc.insertString(index, code, null);
            if (!code.endsWith("\n")) { // NOI18N
                doc.insertString(doc.getLength(), "\n", null); // NOI18N
            }
            NbDocument.markGuarded(doc, index, doc.getLength()-index);
        }

        Position pos = NbDocument.createPosition(doc, index, Position.Bias.Forward);
        gInfo.position = pos;
        getGuardInfos(category)[blockIndex] = gInfo;

        if (gBlock.isCustomizable()) {
            String[] items = new String[] { NbBundle.getMessage(CustomCodeView.class, "CTL_GuardCombo_Default"), // NOI18N
                                            gBlock.getCustomEntry().getDisplayName() };
            JComboBox combo = new JComboBox(items);
//            combo.setBorder(null);
            if (gBlock.isCustomized()) {
                selectInComboBox(combo, items[1]);
                combo.setToolTipText(gBlock.getCustomEntry().getToolTipText());
            }
            else {
                selectInComboBox(combo, items[0]);
                combo.setToolTipText(NbBundle.getMessage(CustomCodeView.class, "CTL_GuardCombo_Default_Hint")); // NOI18N
            }
            combo.getAccessibleContext().setAccessibleName(gBlock.getCustomEntry().getName());
            combo.addActionListener(new GuardSwitchL(category, blockIndex));
            getGutter(doc).add(combo, pos);
        }
    }

    /**
     * Writes edited code back to the CustomCodeData structure.
     */
    CustomCodeData retreiveCodeData() {
        retreiveCodeData(CodeCategory.CREATE_AND_INIT);
        retreiveCodeData(CodeCategory.DECLARATION);

        VariableDeclaration decl = codeData.getDeclarationData();
        boolean local = variableValues[variableCombo.getSelectedIndex()];
        int modifiers;
        if (local != decl.local) {
            modifiers = local ? lastLocalModifiers : lastFieldModifiers;
            if (finalCheckBox.isSelected()) // only final makes sense for both local and field scope
                modifiers |= Modifier.FINAL;
            else
                modifiers &= ~Modifier.FINAL;
        }
        else {
            modifiers = accessValues[accessCombo.getSelectedIndex()];
            if (staticCheckBox.isSelected())
                modifiers |= Modifier.STATIC;
            if (finalCheckBox.isSelected())
                modifiers |= Modifier.FINAL;
            if (transientCheckBox.isSelected())
                modifiers |= Modifier.TRANSIENT;
            if (volatileCheckBox.isSelected())
                modifiers |= Modifier.VOLATILE;
            if (local)
                modifiers &= ~(Modifier.STATIC | Modifier.TRANSIENT | Modifier.VOLATILE);
        }
        decl.local = local;
        decl.modifiers = modifiers;

        return codeData;
    }

    private void retreiveCodeData(CodeCategory category) {
        int gCount = codeData.getGuardedBlockCount(category);
        for (int i=0; i < gCount; i++) {
            retreiveEditableBlock(category, i);
            retreiveGuardedBlock(category, i);
        }
        if (gCount > 0)
            retreiveEditableBlock(category, gCount);
    }

    private void retreiveEditableBlock(CodeCategory category, int index) {
        CodeEntry[] entries = codeData.getEditableBlock(category, index).getEntries();
        for (CodeEntry e : entries) {
            e.setCode(null);
        }

        int[] blockBounds = getEditBlockBounds(category, index);
        Document doc = getDocument(category);

        try {
            String allCode = doc.getText(blockBounds[0], blockBounds[1]-blockBounds[0]);
            if (allCode.trim().equals("")) // NOI18N
                return;

            StringBuilder buf = new StringBuilder();
            int selIndex = -1;
            EditableLine nextLine = null;
            Iterator<EditableLine> it = getEditInfos(category)[index].lines.iterator();
            while (it.hasNext() || nextLine != null) {
                EditableLine l = nextLine != null ? nextLine : it.next();
                int startPos = l.getPosition().getOffset();
                int endPos;
                if (it.hasNext()) {
                    nextLine = it.next();
                    endPos = nextLine.getPosition().getOffset();
                }
                else {
                    nextLine = null;
                    endPos = blockBounds[1];
                }
                buf.append(doc.getText(startPos, endPos-startPos));
                if (nextLine == null || nextLine.getSelectedIndex() != l.getSelectedIndex()) {
                    String code = buf.toString().trim();
                    if (!code.equals("")) // NOI18N
                        entries[l.getSelectedIndex()].setCode(code);
                    buf.delete(0, buf.length());
                }
            }
        }
        catch (BadLocationException ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }

    private void retreiveGuardedBlock(CodeCategory category, int index) {
        GuardedBlock gBlock = codeData.getGuardedBlock(category, index);
        if (!gBlock.isCustomizable())
            return;

        if (getGuardInfos(category)[index].customized) {
            Document doc = getDocument(category);
            int[] blockBounds = getGuardBlockBounds(category, index);
            try {
                int startPos = blockBounds[0] + gBlock.getHeaderLength();
                String code = doc.getText(startPos,
                                          blockBounds[1] - gBlock.getFooterLength() - startPos);
                gBlock.setCustomizedCode(code);
            }
            catch (BadLocationException ex) { // should not happen
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        else { // reset to default code
            gBlock.setCustomizedCode(null);
        }
    }

    private void selectInComboBox(JComboBox combo, Object item) {
        ignoreComboAction = true;
        combo.setSelectedItem(item);
        ignoreComboAction = false;
    }

    // -----
    // mapping methods

    private JTextComponent getEditor(CodeCategory category) {
        switch (category) {
            case CREATE_AND_INIT: return initCodeEditor;
            case DECLARATION: return declareCodeEditor;
        }
        return null;
    }

    private Document getDocument(CodeCategory category) {
        return getEditor(category).getDocument();
    }

    private EditBlockInfo[] getEditInfos(CodeCategory category) {
        return editBlockInfos.get(category);
    }

    private GuardBlockInfo[] getGuardInfos(CodeCategory category) {
        return guardBlockInfos.get(category);
    }

    private JPanel getGutter(Document doc) {
        if (doc == initCodeEditor.getDocument())
            return initGutter;
        if (doc == declareCodeEditor.getDocument())
            return declareGutter;
        return null;
    }

    private CodeCategory getCategoryForDocument(Document doc) {
        if (doc == initCodeEditor.getDocument())
            return CodeCategory.CREATE_AND_INIT;
        if (doc == declareCodeEditor.getDocument())
            return CodeCategory.DECLARATION;
        return null;
    }

    // -----

    private class EditableLine {
        private Position position;
        private JComboBox targetCombo;
        private List<EditableLine> linesInBlock;
        private CodeEntry[] codeEntries;

        EditableLine(Position pos, EditableBlock eBlock, int selIndex, List<EditableLine> lines) {
            position = pos;
            linesInBlock = lines;
            codeEntries = eBlock.getEntries();
            targetCombo = new JComboBox(codeEntries);
            setSelectedIndex(selIndex);
            targetCombo.getAccessibleContext().setAccessibleName(codeEntries[selIndex].getName());
            targetCombo.setToolTipText(codeEntries[selIndex].getToolTipText());
            targetCombo.addActionListener(new EditSwitchL());
        }

        Position getPosition() {
            return position;
        }

        Component getGutterComponent() {
            return targetCombo;
        }

        // if having visible combobox in gutter
        boolean isVisible() {
            return targetCombo.getParent() != null && targetCombo.isVisible();
        }

        int getSelectedIndex() {
            return targetCombo.getSelectedIndex();
        }

        void setSelectedIndex(int index) {
            selectInComboBox(targetCombo, targetCombo.getItemAt(index));
        }

        class EditSwitchL implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ignoreComboAction)
                    return; // not invoked by user, ignore

                changed = true;
                // go through all comboboxes in the group and correct selected index
                // according to the selection in this combobox (preceding comboboxes
                // can't have bigger index and subsequent can't have smaller index)
                int selectedIndex = targetCombo.getSelectedIndex();
                boolean preceding = true;
                for (EditableLine l : linesInBlock) {
                    if (l != EditableLine.this) {
                        if ((preceding && l.getSelectedIndex() > selectedIndex)
                            || (!preceding && l.getSelectedIndex() < selectedIndex))
                        {   // correct selected index
                            l.setSelectedIndex(selectedIndex);
                        }
                    }
                    else preceding = false;
                }
                targetCombo.setToolTipText(codeEntries[selectedIndex].getToolTipText());
            }
        }
    }

    private boolean updateGutterComponents(List<EditableLine> lines, Document doc,
                                           int startIndex, int endIndex)
    {
        String text;
        try {
            text = doc.getText(startIndex, endIndex-startIndex);
        }
        catch (BadLocationException ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return false;
        }

        boolean visibility = !text.trim().equals(""); // NOI18N
        int prevSelectedIndex = 0;
        boolean changed = false;
        Container gutter = getGutter(doc);
        for (EditableLine l : lines) {
            // make sure the selected index is correct (ascending in the group)
            if (l.getSelectedIndex() < prevSelectedIndex)
                l.setSelectedIndex(prevSelectedIndex);
            else
                prevSelectedIndex = l.getSelectedIndex();
            // add the component to the gutter if not there yet
            Component comp = l.getGutterComponent();
            if (comp.getParent() == null)
                gutter.add(comp, l.getPosition());
            // show/hide the component
            if (visibility != l.isVisible()) {
                comp.setVisible(visibility);
                changed = true;
            }
        }
        return changed;
    }

    // -----
    // document changes

    private class DocumentL implements DocumentListener {
        private boolean active = true;
        private Map<Document,Integer> lastDocLineCounts = new HashMap<Document,Integer>();

        @Override
        public void insertUpdate(DocumentEvent e) {
            if (active)
                contentChange(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if (active)
                contentChange(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }

        void setActive(boolean active) {
            this.active = active;
            if (active) {
                lastDocLineCounts.clear();
            }
        }

        private void contentChange(DocumentEvent e) {
            changed = true;

            Document doc = e.getDocument();
            CodeCategory category = getCategoryForDocument(doc);
            int eBlockIndex = getEditBlockIndex(category, e.getOffset());
            if (eBlockIndex < 0) {
                return;
            }

            List<EditableLine> lines = getEditInfos(category)[eBlockIndex].lines;
            int[] blockBounds = getEditBlockBounds(category, eBlockIndex);
            boolean repaint = false;

            Integer lastLineCount = lastDocLineCounts.get(doc);
            int lineCount = getLineCount(doc);
            if (lastLineCount == null || lastLineCount.intValue() != lineCount) {
                lastDocLineCounts.put(doc, Integer.valueOf(lineCount));
                updateLines(doc, blockBounds[0], blockBounds[1], lines,
                            codeData.getEditableBlock(category, eBlockIndex));
                repaint = true;
                // make sure our listener is invoked after position listeners update
                doc.removeDocumentListener(this);
                doc.addDocumentListener(this);
            }

            repaint |= updateGutterComponents(lines, doc, blockBounds[0], blockBounds[1]);

            if (repaint) {
                JPanel gutter = getGutter(doc);
                gutter.revalidate();
                gutter.repaint();
            }
//            ((BaseDocument)doc).resetUndoMerge();
        }
    }

    private void updateLines(Document doc, int startPos, int endPos,
                             List<EditableLine> lines, EditableBlock eBlock) {
        String text;
        try {
            text = doc.getText(startPos, endPos - startPos);
        } catch (BadLocationException ex) { // should not happen
            Exceptions.printStackTrace(ex);
            return;
        }
        ListIterator<EditableLine> lineIt = lines.listIterator();
        EditableLine line = null;
        int selIndex = -1;
        boolean onNewLine = true;
        for (int offset=startPos; offset < endPos; offset++) {
            boolean lineIsHere = false;
            if (line == null && lineIt.hasNext()) {
                line = lineIt.next();
            }
            EditableLine first = null; // in case of multiple collapsed lines stacked after deleting some lines
            while (line != null && line.getPosition().getOffset() == offset) {
                // there is a line mark at this position
                lineIsHere = true;
                if (onNewLine && first == null) {
                    first = line;
                    selIndex = line.getSelectedIndex();
                } else {
                    lineIt.remove();
                    Component comp = line.getGutterComponent();
                    comp.getParent().remove(comp);
                    if (first != null) {
                        selIndex = line.getSelectedIndex();
                        first.setSelectedIndex(selIndex);
                    }
                }
                line = lineIt.hasNext() ? lineIt.next() : null;
            }
            if (onNewLine && !lineIsHere) {
                if (selIndex < 0) {
                    selIndex = eBlock.getPreferredEntryIndex();
                }
                try {
                    Position pos = NbDocument.createPosition(doc, offset, Position.Bias.Backward);
                    if (line != null) {
                        lineIt.previous();
                    }
                    lineIt.add(new EditableLine(pos, eBlock, selIndex, lines));
                    if (line != null) {
                        lineIt.next();
                    }
                } catch (BadLocationException ex) { // should not happen
                    Exceptions.printStackTrace(ex);
                }
            }
            // is next position is on beginning of a new line?
            onNewLine = (text.charAt(offset-startPos) == '\n'); 
        }
    }

    private int[] getEditBlockBounds(CodeCategory category, int index) {
        int startIndex = getEditInfos(category)[index].position.getOffset();
        GuardBlockInfo[] gInfos = getGuardInfos(category);
        int endIndex = index < gInfos.length ?
                gInfos[index].position.getOffset() :
                getDocument(category).getLength();
        return new int[] { startIndex, endIndex };
    }

    private int getEditBlockIndex(CodeCategory category, int offset) {
        return getBlockIndex(category, offset, true);
    }

    private int getBlockIndex(CodeCategory category, int offset, boolean editable) {
        EditBlockInfo[] editInfos = getEditInfos(category);
        GuardBlockInfo[] guardInfos = getGuardInfos(category);
        // assuming editInfo.length == guardInfos.length + 1
        for (int i=0; i < guardInfos.length; i++) {
            int editPos = editInfos[i].position.getOffset();
            if (editPos > offset) // the offset lies in preceding guarded block
                return editable ? -1 : i-1;
            if (editPos == offset || guardInfos[i].position.getOffset() >= offset)
                return editable ? i : -1; // the offset lies in this editable block
        }
        // otherwise the offset is in the last editable block
        return editable ? editInfos.length-1 : -1;
    }

    // -----

    private class GuardSwitchL implements ActionListener {
        CodeCategory category;
        int blockIndex;

        GuardSwitchL(CodeCategory cat, int index) {
            category = cat;
            blockIndex = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (ignoreComboAction)
                return; // not invoked by user, ignore

            GuardedBlock gBlock = codeData.getGuardedBlock(category, blockIndex);
            GuardBlockInfo gInfo = getGuardInfos(category)[blockIndex];
            int[] blockBounds = getGuardBlockBounds(category, blockIndex);
            int startOffset = blockBounds[0];
            int endOffset = blockBounds[1];
            int gHead = gBlock.getHeaderLength();
            int gFoot = gBlock.getFooterLength();
            JTextComponent editor = getEditor(category);
            StyledDocument doc = (StyledDocument) editor.getDocument();

            changed = true;

            JComboBox combo = (JComboBox) e.getSource();
            try {
                docListener.setActive(false);
                if (combo.getSelectedIndex() == 1) { // changing from default to custom
                    NbDocument.unmarkGuarded(doc, startOffset, endOffset - startOffset);
                    // keep last '\n' so we don't destroy next editable block's position
                    doc.remove(startOffset, endOffset - startOffset - 1);
                    // insert the custom code into the document
                    String customCode = gBlock.getCustomCode();
                    int customLength = customCode.length();
                    if (gInfo.customizedCode != null) { // already was edited before
                        customCode = customCode.substring(0, gHead)
                                     + gInfo.customizedCode
                                     + customCode.substring(customLength - gFoot);
                        customLength = customCode.length();
                    }
                    if (customCode.endsWith("\n")) // NOI18N
                        customCode = customCode.substring(0, customLength-1);
                    doc.insertString(startOffset, customCode, null);
                    gInfo.customized = true;
                    // make guarded "header" and "footer", select the text in between
                    NbDocument.markGuarded(doc, startOffset, gHead);
                    NbDocument.markGuarded(doc, startOffset + customLength - gFoot, gFoot);
                    editor.setSelectionStart(startOffset + gHead);
                    editor.setSelectionEnd(startOffset + customLength - gFoot);
                    editor.requestFocus();
                    combo.setToolTipText(gBlock.getCustomEntry().getToolTipText());
                }
                else { // changing from custom to default
                    // remember the customized code
                    gInfo.customizedCode = doc.getText(startOffset + gHead,
                                                       endOffset - gFoot - gHead - startOffset);
                    NbDocument.unmarkGuarded(doc, endOffset - gFoot, gFoot);
                    NbDocument.unmarkGuarded(doc, startOffset, gHead);
                    // keep last '\n' so we don't destroy next editable block's position
                    doc.remove(startOffset, endOffset - startOffset - 1);
                    String defaultCode = gBlock.getDefaultCode();
                    if (defaultCode.endsWith("\n")) // NOI18N
                        defaultCode = defaultCode.substring(0, defaultCode.length()-1);
                    doc.insertString(startOffset, defaultCode, null);
                    gInfo.customized = false;
                    // make the whole text guarded, cancel selection
                    NbDocument.markGuarded(doc, startOffset, defaultCode.length()+1); // including '\n'
                    if (editor.getSelectionStart() >= startOffset && editor.getSelectionEnd() <= endOffset)
                        editor.setCaretPosition(startOffset);
                    combo.setToolTipText(NbBundle.getMessage(CustomCodeData.class, "CTL_GuardCombo_Default_Hint")); // NOI18N
                }
                // we must create a new Position - current was moved away by inserting new string on it
                gInfo.position = NbDocument.createPosition(doc, startOffset, Position.Bias.Forward);

                docListener.setActive(true);
            }
            catch (BadLocationException ex) { // should not happen
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

    private int[] getGuardBlockBounds(CodeCategory category, int index) {
        int startIndex = getGuardInfos(category)[index].position.getOffset();
        int endIndex = getEditInfos(category)[index+1].position.getOffset();
        return new int[] { startIndex, endIndex };
    }

    // -----

    private static class GutterLayout implements LayoutManager2 {

        private JTextComponent editor;
        private Map<Component, Position> positions;
        private int lineHeight = -1;

        private static final int LEFT_GAP = 2;
        private static final int RIGHT_GAP = 4;

        GutterLayout(JTextComponent editor, Map<Component, Position> positionMap) {
            this.editor = editor;
            this.positions = positionMap;
        }

        @Override
        public void addLayoutComponent(Component comp, Object constraints) {
            positions.put(comp, (Position)constraints);
        }

        @Override
        public void layoutContainer(Container parent) {
            StyledDocument doc = (StyledDocument)editor.getDocument();
            for (Component comp : parent.getComponents()) {
                Position pos = positions.get(comp);
                int line = findLineNumber(doc, pos.getOffset());
                Dimension prefSize = comp.getPreferredSize();
                int dy = lineHeight() - prefSize.height;
                dy = dy > 0 ? dy / 2 + 1 : 0;
                comp.setBounds(LEFT_GAP,
                               line * lineHeight() + dy,
                               parent.getWidth() - LEFT_GAP - RIGHT_GAP,
                               Math.min(prefSize.height, lineHeight()));
            }
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            positions.remove(comp);
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            int prefWidth = 0;
            for (Component comp : positions.keySet()) {
                Dimension prefSize = comp.getPreferredSize();
                if (prefSize.width > prefWidth)
                    prefWidth = prefSize.width;
            }
            return new Dimension(prefWidth + LEFT_GAP + RIGHT_GAP,
                                 editor.getPreferredSize().height);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public Dimension maximumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public float getLayoutAlignmentX(Container target) {
            return .5f;
        }

        @Override
        public float getLayoutAlignmentY(Container target) {
            return .5f;
        }

        @Override
        public void invalidateLayout(Container target) {
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        // -----

        private int lineHeight() {
            if (lineHeight < 0) {
                Element root = editor.getDocument().getDefaultRootElement();
                if (root.getElementCount()>0) {
                    Element elem = root.getElement(0);
                    try {
                        int y1 = editor.modelToView(elem.getStartOffset()).y;
                        int y2 = editor.modelToView(elem.getEndOffset()).y;
                        lineHeight = y2-y1;
                    } catch (BadLocationException blex) {
                        Logger.getLogger(CustomCodeView.class.getName()).log(Level.INFO, blex.getMessage(), blex);
                    }
                }
                if (lineHeight <= 0) {
                    // fallback
                    lineHeight = editor.getFontMetrics(editor.getFont()).getHeight();
                }
            }
            return lineHeight;
        }
    }

    private static int getLineCount(Document doc) {
        int length = doc.getLength();
        String text;
        int count = 0;
        try {
            text = doc.getText(0, length);
        } catch (BadLocationException ex) { // should not happen
            Exceptions.printStackTrace(ex);
            return -1;
        }
        for (int i=0; i < length; i++) {
            if (text.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

    private static int findLineNumber(Document doc, int offset) {
        int length = doc.getLength();
        String text;
        int count = 0;
        try {
            text = doc.getText(0, length);
        } catch (BadLocationException ex) { // should not happen
            Exceptions.printStackTrace(ex);
            return -1;
        }
        for (int i=0; i < length; i++) {
            if (i == offset) {
                return count;
            }
            if (text.charAt(i) == '\n') {
                count++;
                
            }
        }
        return offset==length ? count : -1;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JLabel initCodeLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        initCodeEditor = createCodeEditorPane();
        javax.swing.JLabel declarationCodeLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        declareCodeEditor = createCodeEditorPane();
        javax.swing.JLabel selectComponentLabel = new javax.swing.JLabel();
        componentCombo = new javax.swing.JComboBox();
        renameButton = new javax.swing.JButton();
        javax.swing.JLabel variableScopeLabel = new javax.swing.JLabel();
        variableCombo = new javax.swing.JComboBox();
        javax.swing.JLabel variableAccessLabel = new javax.swing.JLabel();
        accessCombo = new javax.swing.JComboBox();
        staticCheckBox = new javax.swing.JCheckBox();
        finalCheckBox = new javax.swing.JCheckBox();
        transientCheckBox = new javax.swing.JCheckBox();
        volatileCheckBox = new javax.swing.JCheckBox();

        FormListener formListener = new FormListener();

        initCodeLabel.setFont(initCodeLabel.getFont().deriveFont(initCodeLabel.getFont().getStyle() | java.awt.Font.BOLD));
        initCodeLabel.setLabelFor(initCodeEditor);
        org.openide.awt.Mnemonics.setLocalizedText(initCodeLabel, org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.initCodeLabel.text")); // NOI18N

        jScrollPane1.setViewportView(initCodeEditor);

        declarationCodeLabel.setFont(declarationCodeLabel.getFont().deriveFont(declarationCodeLabel.getFont().getStyle() | java.awt.Font.BOLD));
        declarationCodeLabel.setLabelFor(declareCodeEditor);
        org.openide.awt.Mnemonics.setLocalizedText(declarationCodeLabel, org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.declarationCodeLabel.text")); // NOI18N

        jScrollPane2.setViewportView(declareCodeEditor);

        selectComponentLabel.setLabelFor(componentCombo);
        org.openide.awt.Mnemonics.setLocalizedText(selectComponentLabel, org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.selectComponentLabel.text")); // NOI18N

        componentCombo.setToolTipText(org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.componentCombo.toolTipText")); // NOI18N
        componentCombo.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(renameButton, org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.renameButton.text")); // NOI18N
        renameButton.setToolTipText(org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.renameButton.toolTipText")); // NOI18N
        renameButton.addActionListener(formListener);

        variableScopeLabel.setLabelFor(variableCombo);
        org.openide.awt.Mnemonics.setLocalizedText(variableScopeLabel, org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.variableScopeLabel.text")); // NOI18N

        variableCombo.addActionListener(formListener);

        variableAccessLabel.setLabelFor(accessCombo);
        org.openide.awt.Mnemonics.setLocalizedText(variableAccessLabel, org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.variableAccessLabel.text")); // NOI18N

        accessCombo.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(staticCheckBox, "&static"); // NOI18N
        staticCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        staticCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        staticCheckBox.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(finalCheckBox, "&final"); // NOI18N
        finalCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        finalCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        finalCheckBox.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(transientCheckBox, "&transient"); // NOI18N
        transientCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        transientCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        transientCheckBox.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(volatileCheckBox, "v&olatile"); // NOI18N
        volatileCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        volatileCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        volatileCheckBox.addActionListener(formListener);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(variableScopeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(variableCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(variableAccessLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(accessCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(finalCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(staticCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(transientCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(volatileCheckBox))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
                    .addComponent(declarationCodeLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(selectComponentLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(componentCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(renameButton))
                    .addComponent(initCodeLabel, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {accessCombo, variableCombo});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectComponentLabel)
                    .addComponent(componentCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(renameButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(initCodeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                .addGap(11, 11, 11)
                .addComponent(declarationCodeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(variableScopeLabel)
                    .addComponent(variableCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(variableAccessLabel)
                    .addComponent(accessCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(staticCheckBox)
                    .addComponent(finalCheckBox)
                    .addComponent(transientCheckBox)
                    .addComponent(volatileCheckBox))
                .addContainerGap())
        );

        staticCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.staticCheckBox.accessibleDescription")); // NOI18N
        finalCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.finalCheckBox.accessibleDescription")); // NOI18N
        transientCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.transientCheckBox.accessibleDescription")); // NOI18N
        volatileCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.volatileCheckBox.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.accessibleDescription")); // NOI18N
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == componentCombo) {
                CustomCodeView.this.componentComboActionPerformed(evt);
            }
            else if (evt.getSource() == renameButton) {
                CustomCodeView.this.renameButtonActionPerformed(evt);
            }
            else if (evt.getSource() == variableCombo) {
                CustomCodeView.this.declControlActionPerformed(evt);
            }
            else if (evt.getSource() == accessCombo) {
                CustomCodeView.this.declControlActionPerformed(evt);
            }
            else if (evt.getSource() == staticCheckBox) {
                CustomCodeView.this.declControlActionPerformed(evt);
            }
            else if (evt.getSource() == finalCheckBox) {
                CustomCodeView.this.declControlActionPerformed(evt);
            }
            else if (evt.getSource() == transientCheckBox) {
                CustomCodeView.this.declControlActionPerformed(evt);
            }
            else if (evt.getSource() == volatileCheckBox) {
                CustomCodeView.this.declControlActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void declControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_declControlActionPerformed
        if (ignoreComboAction)
            return; // not invoked by user, ignore

        if (evt.getSource() == finalCheckBox) {
            if (finalCheckBox.isSelected()) {
                volatileCheckBox.setSelected(false);
            }
        } else if (evt.getSource() == volatileCheckBox) {
            if (volatileCheckBox.isSelected()) {
                finalCheckBox.setSelected(false);
            }
        }
        changed = true;
        controller.declarationChanged();
    }//GEN-LAST:event_declControlActionPerformed

    private void renameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameButtonActionPerformed
        controller.renameInvoked();
    }//GEN-LAST:event_renameButtonActionPerformed

    private void componentComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_componentComboActionPerformed
        if (ignoreComboAction)
            return; // not invoked by user, ignore

        controller.componentExchanged((String)componentCombo.getSelectedItem());
    }//GEN-LAST:event_componentComboActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox accessCombo;
    private javax.swing.JComboBox componentCombo;
    private javax.swing.JEditorPane declareCodeEditor;
    private javax.swing.JCheckBox finalCheckBox;
    private javax.swing.JEditorPane initCodeEditor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton renameButton;
    private javax.swing.JCheckBox staticCheckBox;
    private javax.swing.JCheckBox transientCheckBox;
    private javax.swing.JComboBox variableCombo;
    private javax.swing.JCheckBox volatileCheckBox;
    // End of variables declaration//GEN-END:variables
    private JPanel initGutter;
    private JPanel declareGutter;

    private static final boolean[] variableValues = { false, true };
    private static final String[] variableStrings = {
        NbBundle.getMessage(CustomCodeView.class, "CTL_VariableCombo_Field"), // NOI18N
        NbBundle.getMessage(CustomCodeView.class, "CTL_VariableCombo_Local") }; // NOI18N
    private static final int[] accessValues = { Modifier.PRIVATE, 0, Modifier.PROTECTED, Modifier.PUBLIC };
    private static final String[] accessStrings = {
        "private", // NOI18N
        NbBundle.getMessage(CustomCodeView.class, "CTL_AccessCombo_package_private"), // NOI18N
        "protected", // NOI18N
        "public" }; // NOI18N
}
