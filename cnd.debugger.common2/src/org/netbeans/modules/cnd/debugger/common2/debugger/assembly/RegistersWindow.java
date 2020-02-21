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

package org.netbeans.modules.cnd.debugger.common2.debugger.assembly;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.accessibility.AccessibleContext;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.openide.util.HelpCtx;

import org.netbeans.spi.viewmodel.Models;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.openide.util.NbPreferences;


public final class RegistersWindow extends TopComponent
    implements ActionListener
{

    private static final String preferredID = "RegistersWindow";	// NOI18N
    private static RegistersWindow DEFAULT;

    private transient JComponent tree = null;
    private final String name;
    private final String view_name;
    private NativeDebugger debugger = null;
    private DefaultTableModel dataModel;
    private JMenuItem menuItemHide;
    private JPopupMenu popup;
    private JPanel hp;
    private JTable tab;
    private JTextArea ta;
    private JScrollPane ta_sp;
    private JScrollPane tab_sp;
    private List<String> previous_regs;
    private List<String> current_regs;
    private List<String> hidden_regs;
    private PopupListener popupListener;
    private boolean needInitData=true;
    private boolean seen_sparc_regs=false;
    private String selected_text = null;
    private int selected_area_start = 0;
    private int selected_area_end = 0;
    private int view_model = 1; // 0 - JTextArea, 1 - JTable
    
    public static synchronized RegistersWindow getDefault() {
        if (DEFAULT == null) {
            DEFAULT = (RegistersWindow) WindowManager.getDefault().findTopComponent(preferredID);

            if (DEFAULT == null) {
                DEFAULT = new RegistersWindow();
            }
        }
        return DEFAULT;
    }
    
    public RegistersWindow() {
	name = Catalog.get("TITLE_RegistersWindow");    // NOI18N
	view_name = Catalog.get("TITLE_RegistersView"); // NOI18N
	super.setName(name);
	final String iconDir = "org/netbeans/modules/cnd/debugger/common2/icons/";//NOI18N
	setIcon(org.openide.util.ImageUtilities.loadImage
	    (iconDir + "registers.png")); // NOI18N
    }

    @Override
    protected String preferredID() {
        return this.getClass().getName();
    }

    @Override
    protected void componentHidden () {
	if (debugger != null) {
	    debugger.registerRegistersWindow(null);
	}
        super.componentHidden();
    }

    @Override
    protected void componentShowing () {
        super.componentShowing ();
	connectToDebugger(NativeDebuggerManager.get().currentDebugger());
        needInitData=true;
        updateWindow();
    }

    @Override
    protected void componentClosed () {
	if (debugger != null) {
	    debugger.registerRegistersWindow(null);
	}
        super.componentClosed();
    }

    private void connectToDebugger (NativeDebugger debugger) {
	this.debugger = debugger;
	if (debugger == null) {
            return;
        }
	debugger.registerRegistersWindow(this);
    }

    @Override
    public void requestActive() {
        super.requestActive();
        tab.requestFocusInWindow();
    }
    
    private class RegisterTableModel extends DefaultTableModel {
        public RegisterTableModel(Object[] columns) {
            super(columns, 0);
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            debugger.assignRegisterValue(getValueAt(row, 0).toString(), aValue.toString());
        }

    }
    
    private void updateWindow() {
        assert SwingUtilities.isEventDispatchThread();
        
        if (tree == null) {
	    // Temporary (the real name will come from the engine)
            String symbol = "\"main()\"";		// NOI18N
            ta = new JTextArea();
            ta_sp = new JScrollPane(ta);
            // Create table
            Object[] columnNames = {
		Catalog.get("LBL_NameCol"),		// NOI18N
		Catalog.get("LBL_ValueCol")		// NOI18N
	    };
            
            dataModel = new RegisterTableModel(columnNames);
            tab = new JTable(dataModel);
	    // You need to set preferredWidth on all columns for it to take
	    tab.getColumnModel().getColumn(0).setPreferredWidth(40);
	    tab.getColumnModel().getColumn(1).setPreferredWidth(300);
            tab.setGridColor(Color.LIGHT_GRAY);
            tab_sp = new JScrollPane(tab);
            
            previous_regs = new ArrayList<String>();
            current_regs = new ArrayList<String>();
            hidden_regs = new ArrayList<String>();
            setLayout (new BorderLayout ());
            tree = Models.createView (Models.EMPTY_MODEL);
            // Models.setModelsToView (tree, Models.createCompoundModel(regs1));
            tree.setName (view_name);
            
            ta.setEditable(false);
            ta.setWrapStyleWord(false);
            Font f = ta.getFont();
            ta.setFont(new Font("Monospaced", f.getStyle(), f.getSize())); //NOI18N
            f = tab.getFont();
            tab.setFont(new Font("Monospaced", f.getStyle(), f.getSize())); //NOI18N
            
            BorderLayout bl = new BorderLayout();
            bl.setHgap(118);
            hp = new JPanel(bl);
            JLabel hp_name = new JLabel(Catalog.get("RegisterName")); // NOI18N
            JLabel hp_value = new JLabel(Catalog.get("RegisterValue"));     // NOI18N
            hp_name.setToolTipText(Catalog.get("LBL_RegisterName"));   // NOI18N
            hp_value.setToolTipText(Catalog.get("LBL_RegisterValue")); // NOI18N
            hp.add(hp_name, BorderLayout.WEST);
            hp.add(hp_value, BorderLayout.CENTER);
            hp.setToolTipText(Catalog.get("LBL_RegisterName"));		// NOI18N
            
            if (view_model == 0) {
		tree.add(hp, BorderLayout.NORTH);
		tree.add(ta_sp, BorderLayout.CENTER);
            } else {
		tree.add(tab_sp, BorderLayout.CENTER);
            }
            // OLD tree.add(cp, BorderLayout.SOUTH);
            AccessibleContext ac = tree.getAccessibleContext();
            ac.setAccessibleDescription(Catalog.get("ACSD_RegisterView")); // NOI18N
            ac.setAccessibleName(Catalog.get("TITLE_RegistersView")); // NOI18N
            add (tree, "Center");  //NOI18N
            
            //Create the popup menu.
            popup = new JPopupMenu();
            popup.add(new ShowAllRegistersAction());
            menuItemHide = new JMenuItem(new HideSelectedRegistersAction());
            popup.add(menuItemHide);
            //TODO: finish bz#129094 - Allow user to select register values presentation
            //popup.add(new DataPresentationMenu());
            //popup.addSeparator();
            //popup.add(new ShowDynamicHelpPageAction());
            // popup.addSeparator();
            // popup.add(new ChangeViewAction());
            //Add listener
            popupListener = new PopupListener(popup);
          //if (view_model == 0) {
            ta.addMouseListener(popupListener);
          //} else {
            tab.addMouseListener(popupListener);
          //}
            ta.setText(null);
            ta.setCaretPosition(0);

            //Hide section registers
            hidden_regs.add("cs");	// NOI18N
            hidden_regs.add("ds");	// NOI18N
            hidden_regs.add("es");	// NOI18N
            hidden_regs.add("fs");	// NOI18N
            hidden_regs.add("gs");	// NOI18N
            hidden_regs.add("ss");	// NOI18N
        }
        if (needInitData) {
            // Init page
            needInitData=false;
            // initRegs();
        }
        //hsbv = ta_sp.getHorizontalScrollBar().getValue();
        //vsbv = ta_sp.getVerticalScrollBar().getValue();
        int carpos = ta.getCaretPosition();
        ta.setText(null);
        // Clean Table
        dataModel.setRowCount(0);
       	for (int i = 0; i < current_regs.size(); i++) {
       	    String s = current_regs.get(i);
            String[] names = getRegisterNames(s);
            if (names == null) {
                continue;
            }
            
            String regname = names[0].trim();
            int m = 0;
            if (!hidden_regs.contains(regname)) {
                ta.append(s);
                // Update Table
                Object [] row = { "", "" }; // NOI18N
       	        int n=0;
       	        for (m = 0; m < s.length(); m++) {
       	            if (s.charAt(m) == ' ') {
                        continue;
                    }
       	            if (s.charAt(m) == '\t') {
                        continue;
                    }
       	            for (n=m+1; n < s.length(); n++) {
       	                if (s.charAt(n) == ' ') {
                            break;
                        }
       	                if (s.charAt(n) == '\t') {
                            break;
                        }
       	            }
       	            break;
       	        }
       	        if ((m < s.length()) && (m < n)) { 
                    row[0]=s.substring(m, n);
       	            //m=n;
       	            for (++n; n < s.length(); n++) {
       	                if (s.charAt(n) == ' ') {
                            continue;
                        }
       	                if (s.charAt(n) == '\t') {
                            continue;
                        }
       	                break;
       	            }
		    if (n > s.length())  {
                        n = s.length();
                    }
                    row[1]=s.substring(n, s.length());
                }
       	        dataModel.addRow(row);
       	    }
       	    if (i < previous_regs.size()) {
       	        previous_regs.set(i, s);
       	    } else {
       	        previous_regs.add(s);
       	    }
        }
        try {
            ta.setCaretPosition(carpos);
        } catch (java.lang.IllegalArgumentException e) {
            // bad position carpos
            ta.setCaretPosition(0);
        }
        //ta_sp.getHorizontalScrollBar().setValue(hsbv);
        //ta_sp.getVerticalScrollBar().setValue(vsbv);
        invalidate();
        // Update view
        dataModel.fireTableDataChanged();
    }
    
    private class DataPresentationMenu extends JMenu {

        private DataPresentationMenu() {
            super(Catalog.get("Reg_ACT_Data_Presentation"));//NOI18N
            String currentFormat = Disassembly.PREFS.get(Disassembly.REGISTER_DATA_REPRESENTATION_PREF_FORMAT_KEY,
                    Disassembly.DATA_REPRESENTATION.hexadecimal.toString());
            ActionListener jmiActionListener = new MenuItemActionListener();
            for (Disassembly.DATA_REPRESENTATION f : Disassembly.DATA_REPRESENTATION.values()) {
                JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(f.name());
                menuItem.putClientProperty(Disassembly.REGISTER_DATA_REPRESENTATION_PREF_FORMAT_KEY,
                        f.toString());
                menuItem.addActionListener(jmiActionListener);
                menuItem.setSelected(currentFormat.equals(f.toString()));
                add(menuItem);
            }
        }       
        
    }    
    
        // Innerclasses ------------------------------------------------------------
    private  class MenuItemActionListener implements ActionListener {
        @Override
        public void actionPerformed( ActionEvent e ) {
            if ( e.getSource() instanceof JMenuItem ) {
                JMenuItem jmi = (JMenuItem)e.getSource();
		String regWindowDataRepersentation = (String)jmi.getClientProperty(Disassembly.REGISTER_DATA_REPRESENTATION_PREF_FORMAT_KEY );
                Disassembly.PREFS.put(Disassembly.REGISTER_DATA_REPRESENTATION_PREF_FORMAT_KEY, regWindowDataRepersentation);
                updateWindow();
            }
            
        }
        
    }

    private class ShowAllRegistersAction extends AbstractAction {
        public ShowAllRegistersAction() {
            super(Catalog.get("Reg_ACT_Show_All_Registers"), 	// NOI18N
                null);
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            ShowAllRegisters();
        }
    }

    private class HideSelectedRegistersAction extends AbstractAction {
        public HideSelectedRegistersAction() {
            super(Catalog.get("Reg_ACT_Hide"), 			// NOI18N
                null);
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            if (view_model == 0) {
                selected_text = ta.getSelectedText();
            } else {
                if (tab.getSelectedRowCount() <= 0) {
                    return;
                }
                int[] rows = tab.getSelectedRows();
                selected_text = "";
                for (int i=0; i < rows.length; i++) {
                    selected_text += tab.getValueAt(rows[i], 0) +
				     "\t " + // NOI18N
				     tab.getValueAt(rows[i], 1) +
				     " \n"; // NOI18N
                }
            }
            HideSelectedRegisters(selected_text);
        }
    }

    /* LATER
    class ShowDynamicHelpPageAction extends AbstractAction {
        public ShowDynamicHelpPageAction() {
            super(Catalog.get("Reg_ACT_More_Info"),
                new ImageIcon("help.gif"));
        }
        public void actionPerformed(ActionEvent ev) {
//System.out.println("ShowDynamicHelpPageAction.ActionPerformed(More Info)");
            ShowDynamicHelpPage();
        }
    }

    class ChangeViewAction extends AbstractAction {
        public ChangeViewAction() {
            super("Change View", 
                new ImageIcon("help.gif"));
        }
        public void actionPerformed(ActionEvent ev) {
//System.out.println("ChangeViewAction.ActionPerformed(Change View)");
            ChangeView();
        }
    }
    */
    
    private class PopupListener extends MouseAdapter
                           implements ActionListener, 
                                      PopupMenuListener {
        final JPopupMenu popup;

        PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        private void selectCurrentLine(int x, int y) {
            // This is just temporary placeholder code
            int ln = ta.getLineCount(); 
            if (ln > 0) {
                try {
                    selected_area_start = ta.getLineStartOffset(ln);
                    selected_area_end = ta.getLineEndOffset(ln);
                    ta.select(selected_area_start, selected_area_end);
                } catch (javax.swing.text.BadLocationException e) {
                    //selected_area_start = 0;
                    //selected_area_end = 0;
                }
            }
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                boolean selected = false;
                if (view_model == 0) {
                    selected_text = ta.getSelectedText();
                    if (selected_text != null) {
                        selected = true;
                    }
                } else {
                    if (tab.getSelectedRowCount() > 0)  {
                        selected = true;
                    }
                }
                if (selected) {
                    menuItemHide.setEnabled(true);
                } else {
                    menuItemHide.setEnabled(false);
                }
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
        }
        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }
        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }

    @Override
    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
        
    @Override
    public String getName () {
        return name;
    }
    
    @Override
    public String getToolTipText () {
        return view_name;
    }
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
	// super.actionPerformed(actionEvent);
    }

    public void updateData(List<String> regs) {
        current_regs.clear();
        current_regs.addAll(regs);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateWindow();
            }
        });
    }
    
    private String[] getRegisterNames(String regs) {
        if (regs == null) {
            return null;
        }
        ArrayList<String> result = new ArrayList<String>();
        int i, j, k, l;
        String s, regname;
        
        l = regs.length();
       	for (i = 0; i < l; i++) {
       	    k = regs.indexOf('\n', i);
       	    if (k < 0) {
                k = l - 1;
            }
       	    if (k <= i) {
                break;
            }
       	    s = regs.substring(i, k + 1);
            i = k;
            regname = null;
       	    for (j=0, k=0; j < s.length(); j++) {
       	        if (s.charAt(j) != ' ') {
       	            if (k == 0) {
       	                k = s.indexOf(' ', j);
       	                if (k < 0) {
       	                    k = s.length();
       	                }
       	                if (k < j) {
                            break;
                        }
       	                regname = s.substring(j, k);
       	                j = k;
                    } else {
       	                k = s.indexOf('\n', j);
       	                if (k < j) {
                            break;
                        }
       	                break;
                    }
       	        }
            }
       	    if (regname != null) {
                result.add(regname);
            }
        }
        return result.toArray(new String[result.size()]);
    }
    
    protected void HideSelectedRegisters(String regs) {
        String[] selectedRegisterNames = getRegisterNames(regs);
        if (selectedRegisterNames == null) {
            return;
        }
        for (int i = 0;  i < selectedRegisterNames.length; i++) {
            String regname = selectedRegisterNames[i].trim();
            if (!hidden_regs.contains(regname)) {
                hidden_regs.add(regname);
            }
        }
        updateWindow();
    }

    protected void ShowAllRegisters() {
        hidden_regs.clear();
        updateWindow();
    }

    protected void ChangeView() {
        view_model++;
        if (view_model > 1) {
            view_model = 0;
        }
        if (view_model == 0) {
            tree.remove(tab_sp);
            tree.add(hp, BorderLayout.NORTH);
            tree.add(ta_sp, BorderLayout.CENTER);
        } else {
            tree.remove(hp);
            tree.remove(ta_sp);
            tree.add(tab_sp, BorderLayout.CENTER);
        }
        updateWindow();
    }
    
    @Override
    public HelpCtx getHelpCtx() {
	return new HelpCtx("RegistersWindow");
    }

}
