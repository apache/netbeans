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

package org.netbeans.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FontMetrics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * Status bar support.
 * <br>
 * By default the status bar is hidden and a global IDE's status bar is used.
 * It is only visible if the editor window is undocked.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class StatusBar implements PropertyChangeListener, DocumentListener {

    /**
     * Besides line|column display also caret offset in status bar.
     */
    // -J-Dorg.netbeans.editor.caret.offset.level=FINE
    private static final Logger CARET_OFFSET_LOG = Logger.getLogger("org.netbeans.editor.caret.offset");
    
    public static final String CELL_MAIN = "main"; // NOI18N

    public static final String CELL_POSITION = "position"; // NOI18N

    public static final String CELL_TYPING_MODE = "typing-mode"; // NOI18N

    public static final String INSERT_LOCALE = "status-bar-insert"; // NOI18N

    public static final String OVERWRITE_LOCALE = "status-bar-overwrite"; // NOI18N

    private static final String[] POS_MAX_STRINGS = new String[] { "99999:999/9999:9999" }; // NOI18N

    private static final String[] POS_MAX_STRINGS_OFFSET = new String[] { "99999:999/9999:9999 <99999999>" }; // NOI18N

    private static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);

    static final Border CELL_BORDER = 
    BorderFactory.createCompoundBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,UIManager.getDefaults().getColor("control")),   // NOI18N
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,1,UIManager.getDefaults().getColor("controlHighlight")),   // NOI18N
                BorderFactory.createLineBorder(UIManager.getDefaults().getColor("controlDkShadow"))   // NOI18N
            )
        ),
        BorderFactory.createEmptyBorder(0, 2, 0, 2)
    );

    private static Map<String,JLabel> cellName2GlobalCell = new HashMap<String, JLabel>();

    public static void setGlobalCell(String cellName, JLabel globalCell) {
        cellName2GlobalCell.put(cellName, globalCell);
    }

    protected EditorUI editorUI;

    /** The status bar panel into which the cells are added. */
    private JPanel panel;

    private boolean visible;

    private List<JLabel> cellList = new ArrayList<>();

    private Caret caret;

    private CaretListener caretL;

    private int caretDelay;

    private boolean overwriteModeDisplayed;

    private String insText;

    private String ovrText;
    
    private String caretPositionLocaleString;
    private String insertModeLocaleString;
    private String overwriteModeLocaleString;

    private Preferences prefs = null;
    private final PreferenceChangeListener prefsListener = new PreferenceChangeListener() {
        public void preferenceChange(PreferenceChangeEvent evt) {
            // #50073
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    refreshPanel();
                }
            });
            
            if (evt == null || SimpleValueNames.STATUS_BAR_CARET_DELAY.equals(evt.getKey())) {
                caretDelay = prefs.getInt(SimpleValueNames.STATUS_BAR_CARET_DELAY, EditorPreferencesDefaults.defaultStatusBarCaretDelay);
                if (caretL != null) {
                    caretL.setDelay(caretDelay);
                }
            }
        }
    };
    private PreferenceChangeListener weakListener = null;
    
    static final long serialVersionUID =-6266183959929157349L;

    public StatusBar(EditorUI editorUI) {
        this.editorUI = editorUI;

        caretDelay = 10;
        caretL = new CaretListener(caretDelay);
        ResourceBundle bundle = NbBundle.getBundle(BaseKit.class);
        insText = bundle.getString(INSERT_LOCALE);
        ovrText = bundle.getString(OVERWRITE_LOCALE);
        caretPositionLocaleString = bundle.getString("status-bar-caret-position"); //NOI18N
        insertModeLocaleString = bundle.getString("status-bar-insert-mode"); //NOI18N
        overwriteModeLocaleString = bundle.getString("status-bar-overwrite-mode"); //NOI18N

        synchronized (editorUI.getComponentLock()) {
            // if component already installed in EditorUI simulate installation
            JTextComponent component = editorUI.getComponent();
            if (component != null) {
                propertyChange(new PropertyChangeEvent(editorUI, EditorUI.COMPONENT_PROPERTY, null, component));
            }

            editorUI.addPropertyChangeListener(this);
        }
    }

    private void documentUndo(DocumentEvent evt) {
        Utilities.runInEventDispatchThread(new Runnable() {
            public void run() {
                // Clear the main cell
                setText(CELL_MAIN, "");
            }
        });
    }

    public void insertUpdate(DocumentEvent evt) {
        if (evt.getType() == DocumentEvent.EventType.REMOVE) { // undo
            documentUndo(evt);
        }
    }

    public void removeUpdate(DocumentEvent evt) {
        if (evt.getType() == DocumentEvent.EventType.INSERT) { // undo
            documentUndo(evt);
        }
    }

    public void changedUpdate(DocumentEvent evt) {
    }


    protected JPanel createPanel() {
        return new JPanel(new GridBagLayout());
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean v) {
        if (v != visible) {
            visible = v;

            if (panel != null || visible) {
                if (visible) { // need to refresh first
                    refreshPanel();
                }
                // fix for issue 13842
                if (SwingUtilities.isEventDispatchThread()) {
                    getPanel().setVisible(visible);
                } else {
                    SwingUtilities.invokeLater(
                        new Runnable() {
                            public void run() {
                                getPanel().setVisible(visible);
                            }
                        }
                    );
                }
            }
        }
    }

    /**
     * Update values into global cells.
     * @since 1.37
     */
    public void updateGlobal() {
        // Update all cell mappings
        for (Map.Entry<String,JLabel> e : cellName2GlobalCell.entrySet()) {
            if (CELL_MAIN.equals(e.getKey())) { // Do not sync main cell into global panel
                continue;
            }
            String s = getText(e.getKey());
            e.getValue().setText(s);
        }
    }

    public final JPanel getPanel() {
        if (panel == null) {
            panel = createPanel();
            initPanel();
        }
        return panel;
    }

    protected void initPanel() {
        JLabel cell = addCell(CELL_POSITION,
                CARET_OFFSET_LOG.isLoggable(Level.FINE) ? POS_MAX_STRINGS_OFFSET : POS_MAX_STRINGS);
        cell.setHorizontalAlignment(SwingConstants.CENTER);
        cell.addMouseListener(new MouseAdapter() {
            public @Override void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTextComponent jtc = editorUI.getComponent();
                    if (jtc != null) {
                        BaseKit kit = Utilities.getKit(jtc);
                        if (kit != null) {
                            Action a = kit.getActionByName(org.netbeans.editor.ext.ExtKit.gotoAction);
                            if (a != null) {
                                a.actionPerformed(new ActionEvent(jtc, 0, null));
                            }
                        }
                    }
                }
            }            
        });
        addCell(CELL_TYPING_MODE, new String[] { insText, ovrText }).setHorizontalAlignment(
            SwingConstants.CENTER);
        setText(CELL_TYPING_MODE, insText);
        addCell(CELL_MAIN, null);
        
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if (EditorUI.COMPONENT_PROPERTY.equals(propName)) {
            if (prefs != null && weakListener != null) {
                prefs.removePreferenceChangeListener(weakListener);
            }
            
            JTextComponent component = (JTextComponent)evt.getNewValue();
            if (component != null) { // just installed
                component.addPropertyChangeListener(this);

                caret = component.getCaret();
                if (caret != null) {
                    caret.addChangeListener(caretL);
                }
                
                Document doc = component.getDocument();
                if (doc != null) {
                    doc.addDocumentListener(this);
                }

                String mimeType = org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(component);
                prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
                weakListener = WeakListeners.create(PreferenceChangeListener.class, prefsListener, prefs);
                prefs.addPreferenceChangeListener(weakListener);
                prefsListener.preferenceChange(null);
                
                refreshPanel();

            } else { // just deinstalled
                component = (JTextComponent)evt.getOldValue();

                component.removePropertyChangeListener(this);

                caret = component.getCaret();
                if (caret != null) {
                    caret.removeChangeListener(caretL);
                }

                Document doc = component.getDocument();
                if (doc != null) {
                    doc.removeDocumentListener(this);
                }
            }

        } else if ("caret".equals(propName)) { // NOI18N
            if (caret != null) {
                caret.removeChangeListener(caretL);
            }

            caret = (Caret)evt.getNewValue();
            if (caret != null) {
                caret.addChangeListener(caretL);
            }
        } else if ("document".equals(propName)) { // NOI18N
            Document old = (Document)evt.getOldValue();
            Document cur = (Document)evt.getNewValue();
            if (old != null) {
                old.removeDocumentListener(this);
            }
            if (cur != null) {
                cur.addDocumentListener(this);
            }
        }

        // Refresh the panel after each property-change
        if (EditorUI.OVERWRITE_MODE_PROPERTY.equals(propName)) {
            caretL.actionPerformed(null); // refresh immediately

        } else { // not overwrite mode change
            caretL.stateChanged(null);
        }
    }

    /** #86272: Applies given coloring on cell in special way, stick with default
     * LF color for status bar cells when foreColor or backColor of Coloring is null
     */
    private void applyColoring(Cell cell, Coloring coloring) {
        coloring.apply(cell);
        if (coloring.getForeColor() == null) {
            cell.setForeground(cell.getDefaultForeground());
        }
        if (coloring.getBackColor() == null) {
            cell.setBackground(cell.getDefaultBackground());
        }
    }

    public int getCellCount() {
        return cellList.size();
    }

    public JLabel addCell(String name, String[] widestStrings) {
        return addCell(-1, name, widestStrings);
    }

    public JLabel addCell(int i, String name, String[] widestStrings) {
        Cell c = new Cell(name, widestStrings);
        addCellImpl(i, c);
        return c;
    }

    public void addCustomCell(int i, JLabel c) {
        addCellImpl(i, c);
    }

    private void addCellImpl(int i, JLabel c) {
        synchronized (cellList) {
            List<JLabel> newCellList = new ArrayList<>(cellList);
            int cnt = newCellList.size();
            if (i < 0 || i > cnt) {
                i = cnt;
            }
            newCellList.add(i, c);

            cellList = newCellList;
            
            updateCellBorders(i);
        }

        refreshPanel();
    }

    /** Manages cell borders so that left, right and inner cells have properly
     * assigned borders for various LFs. Borders are special, installed by
     * core into UIManager maps. */
    private void updateCellBorders(int addedIndex) {
        int cellCount = getCellCount();
        Border innerBorder = (Border)UIManager.get("Nb.Editor.Status.innerBorder"); //NOI18N
        Border leftBorder = (Border)UIManager.get("Nb.Editor.Status.leftBorder"); //NOI18N
        Border rightBorder = (Border)UIManager.get("Nb.Editor.Status.rightBorder"); //NOI18N
        Border onlyOneBorder = (Border)UIManager.get("Nb.Editor.Status.onlyOneBorder"); //NOI18N
        if ((innerBorder == null) || (leftBorder == null) || (rightBorder == null)
            || onlyOneBorder == null) {
            // don't modify borders at all if some is not available 
            return;
        }
        if (cellCount == 1) {
            // only one cell
            cellList.get(0).setBorder(onlyOneBorder);
            return;
        }
        if (addedIndex == 0) {
            // added as first, updates second
            cellList.get(0).setBorder(leftBorder);
            JLabel second = cellList.get(1);
            second.setBorder(cellCount == 2 ? rightBorder : innerBorder);
        } else if (addedIndex == cellCount - 1) {
            // added as last, updates previous
            cellList.get(cellCount - 1).setBorder(rightBorder);
            JLabel previous = cellList.get(cellCount - 2);
            previous.setBorder(cellCount == 2 ? leftBorder : innerBorder);
        } else {
            // cell added inside
            cellList.get(addedIndex).setBorder(innerBorder);
        }
    }

    public JLabel getCellByName(String name) {
        Iterator<JLabel> i = cellList.iterator();
        while (i.hasNext()) {
            JLabel c = i.next();
            if (name.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }

    public String getText(String cellName) {
        JLabel cell = getCellByName(cellName);
        return (cell != null) ? cell.getText() : null;
    }

    public void setText(String cellName, String text) {
        setText(cellName, text, null);
    }

    private static Coloring getColoring(String mimeType, String highlight) {
        FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
        AttributeSet attribs = fcs == null ? null : fcs.getFontColors(highlight);
        return attribs == null ? null : Coloring.fromAttributeSet(attribs);
    }
    
    public void setBoldText(String cellName, String text) {
        JTextComponent jtc = editorUI.getComponent();
        setText(cellName, text, jtc != null ? 
            getColoring(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(jtc), FontColorNames.STATUS_BAR_BOLD_COLORING) :
            null);
    }

    public void setText(String cellName, String text, Coloring extraColoring) {
        setText(cellName, text, extraColoring, 1);
    }

    public void setText(String cellName, String text, Coloring extraColoring, int importance) {
        JLabel cell = getCellByName(cellName);
        if (cell != null) {
            cell.setText(text);
            if (visible) {
                JTextComponent jtc = editorUI.getComponent();
                Coloring c = jtc != null ? getColoring(
                    org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(jtc),
                    FontColorNames.STATUS_BAR_COLORING
                ) : null;

                if (c != null && extraColoring != null) {
                    c = extraColoring.apply(c);
                } else if (c == null) {
                    c = extraColoring;
                }
                if (CELL_POSITION.equals(cellName)){
                    cell.setToolTipText(caretPositionLocaleString);
                } else if (CELL_TYPING_MODE.equals(cellName)) {
                    cell.setToolTipText(insText.equals(text)? insertModeLocaleString : overwriteModeLocaleString);
                } else {
                    cell.setToolTipText(text == null || text.length() == 0 ? null : text);
                }

                if (c != null && cell instanceof Cell) {
                    applyColoring((Cell) cell, c);
                }
            } else { // Status bar not visible => use global status bar if possible
                JLabel globalCell = cellName2GlobalCell.get(cellName);
                if (globalCell != null) {
                    if (CELL_MAIN.equals(cellName)) {
                        globalCell.putClientProperty("importance", importance);
                    }
                    globalCell.setText(text);
                }
            }
        }
    }

    /**
     * Set text into main cell with a given importance.
     *
     * @param text non-null text to be displayed.
     * @param importance positive integer having
     */
    public void setText(String text, int importance) {
        JTextComponent jtc = editorUI.getComponent();
        setText(CELL_MAIN, text, jtc != null ? getColoring(
            org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(jtc), FontColorNames.STATUS_BAR_BOLD_COLORING) : null,
            importance);
    }

    /* Refresh the whole panel by removing all the components
    * and adding only those that appear in the cell-list.
    */
    private void refreshPanel() {
        if (isVisible()) { // refresh only if visible
            // Apply coloring to all cells
            Iterator<JLabel> it = cellList.iterator();
            while (it.hasNext()) {
                JLabel c = it.next();
                JTextComponent jtc = editorUI.getComponent();
                if (c instanceof Cell && jtc != null /*#141362 Check editorUI.getComponent() not null*/) {
                    Coloring col = getColoring(
                        org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(jtc),
                        FontColorNames.STATUS_BAR_COLORING
                    );
                    if (col != null) {
                        applyColoring((Cell) c, col);
                    }
                }
            }

            // Layout cells
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = GridBagConstraints.RELATIVE;
            gc.gridwidth = 1;
            gc.gridheight = 1;

            it = cellList.iterator();
            while (it.hasNext()) {
                JLabel c = it.next();
                boolean main = CELL_MAIN.equals(c.getName());
                if (main) {
                    gc.fill = GridBagConstraints.HORIZONTAL;
                    gc.weightx = 1.0;
                }
                getPanel().add(c, gc);
                if (main) {
                    gc.fill = GridBagConstraints.NONE;
                    gc.weightx = 0;
                }
            }
        }
    }

    class CaretListener implements ChangeListener, ActionListener {

        Timer timer;

        CaretListener(int delay) {
            timer = new Timer(delay, new WeakTimerListener(this));
            timer.setRepeats(false);
        }

        void setDelay(int delay) {
            timer.setInitialDelay(delay);
        }

        public void stateChanged(ChangeEvent evt) {
            timer.restart();
        }

        public void actionPerformed(ActionEvent evt) {
            Caret c = caret;
            JTextComponent component = editorUI.getComponent();

            // Also check whether the component is last focused since when undocking an editor
            // all the components' carets fire this listener and so invalid component's
            // data would get displayed in the global status bar.
            if (component != null && component == EditorRegistry.lastFocusedComponent()) {
                if (c != null) {
                    BaseDocument doc = Utilities.getDocument(component);
                    if (doc != null && doc.getDefaultRootElement().getElementCount()>0) {
                        int pos = c.getDot();
                        String s = Utilities.debugPosition(doc, pos, ":");
                        if (CARET_OFFSET_LOG.isLoggable(Level.FINE)) { // Possibly add caret offset info
                            s += " <" + pos + ">"; // NOI18N
                        }
                        int countOfSelectedChars = component.getSelectionEnd() - component.getSelectionStart();
                        final boolean hasSelection = countOfSelectedChars > 0;
                        if (hasSelection) {
                            try {
                                //count of selected lines
                                int lineEnd = Utilities.getLineOffset(doc, component.getSelectionEnd());
                                int lineStart = Utilities.getLineOffset(doc, component.getSelectionStart());
                                s += "/" + (lineEnd - lineStart + 1);
                            } catch (BadLocationException ex) {
                            }
                            //count of selected characters
                            s += ":" + countOfSelectedChars;
                        }
                        //rows:cols/countRows:countCols
                        setText(CELL_POSITION, s);
                    }
                }

                Boolean b = (Boolean)editorUI.getProperty(EditorUI.OVERWRITE_MODE_PROPERTY);
                boolean om = (b != null && b.booleanValue());
                if (om != overwriteModeDisplayed) {
                    overwriteModeDisplayed = om;
                    setText(CELL_TYPING_MODE, overwriteModeDisplayed ? ovrText : insText);
                }
            }
        }

    }

    static class Cell extends JLabel {

        Dimension maxDimension;

        String[] widestStrings;
        
        private final Color defaultBackground;
        
        private final Color defaultForeground;

        static final long serialVersionUID =-2554600362177165648L;

        Cell(String name, String[] widestStrings) {
            setName(name);
            setBorder(CELL_BORDER);
            setOpaque(true);
            this.widestStrings = widestStrings;
            this.defaultBackground = getBackground();
            this.defaultForeground = getForeground();
            updateSize();
        }
        
        private void updateSize() {
            Font f = getFont();
            if (maxDimension == null) {
                maxDimension = new Dimension();
            }
            if (f != null) {
                Border b = getBorder();
                Insets ins = (b != null) ? b.getBorderInsets(this) : NULL_INSETS;
                FontMetrics fm = getFontMetrics(f);
                String text = this.getText();
                int mw = text == null ? 0 : fm.stringWidth(text);
                maxDimension.height = fm.getHeight() + ins.top + ins.bottom;
                if (widestStrings != null) {
                    for (int i = 0; i < widestStrings.length; i++) {
                        String widestString = widestStrings[i];
                        if (widestString == null){
                            continue;
                        }
                        mw = Math.max(mw, fm.stringWidth(widestString));
                    }
                }
                maxDimension.width = mw + ins.left + ins.right;
            }
        }

        public @Override Dimension getPreferredSize() {
            if (maxDimension == null) {
                maxDimension = new Dimension();
            }
            return new Dimension(maxDimension);
        }
        
        public @Override Dimension getMinimumSize(){
            if (maxDimension == null) {
                maxDimension = new Dimension();
            }
            return new Dimension(maxDimension);
        }

        public @Override void setFont(Font f) {
            super.setFont(f);
            updateSize();
        }

        @Override
        public void setBorder(Border border) {
            super.setBorder(border);
            updateSize();
        }

        /** Returns default foreground color of the cell, for current LF and theme.  
         * @return Default foreground color 
         */
        public Color getDefaultForeground () {
            Color color = (Color) UIManager.get("Label.foreground"); //NOI18N
            return color != null ? color : defaultForeground;
        }
        
        /** Returns default background color of the cell, for current LF and theme.  
         * @return Default background color 
         */
        public Color getDefaultBackground () {
            Color color = UIManager.getColor("NbEditorStatusBar.background"); //NOI18N
            if( null == color )
                color = (Color) UIManager.get("Label.background"); //NOI18N
            return color != null ? color : defaultBackground;
        }

    }

    public static final class StatusBarFactory implements SideBarFactory {
        
        public JComponent createSideBar(JTextComponent target) {
            return Utilities.getEditorUI(target).getStatusBar().getPanel();
        }
        
        
    }
}
