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
/*
 * DDTablePanel.java -- synopsis.
 *
 */

package org.netbeans.modules.j2ee.sun.ide.editors.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.lang.ref.*;

import org.openide.explorer.propertysheet.editors.*;
import org.openide.*;
import org.openide.util.*;

/**
 * Panel containing a JTable for display of Deployment
 * Descriptor array values, and an "add" and "remove" button.
 *
 * @author Joe Warzecha 
 */
//
// 29-may-2001
//	Changes for bug 4457984. Added an ancestor listener to check
//	for the user cancelling. Changed the call to the addRowAt so
//	the newly created row is passed to the next level, and called
//	newElementCancelled in the case where the user cancels the creation
//	of a new row. (joecorto)
//	
public class DDTablePanel extends JPanel 
			  implements EnhancedCustomPropertyEditor {

    static final ResourceBundle bundle = NbBundle.getBundle
                                                	(DDTablePanel.class);

    private DDTableModel model;

    private JScrollPane scrollPane;
    private JTable	tab;
    private JButton 	addButton;
    private JButton     editButton;
    private JButton 	removeButton;
    private int         sortCol;
    private Dialog      editDialog; 
			    
    public DDTablePanel (DDTableModel m, String [] toolTips) {
	initComponents (m, toolTips, true, true, true, 500, 70, 12);
    }

    public DDTablePanel (DDTableModel m, String [] toolTips, int insetVal) {
	initComponents (m, toolTips, true, true, true, 500, 70, insetVal);
    }

    public DDTablePanel (DDTableModel m, String[] toolTips, boolean allowAdd,
			 boolean allowRemove) {
        initComponents (m, toolTips, allowAdd, allowRemove, true, 500, 70, 12);
    }

    public DDTablePanel (DDTableModel m) {
        initComponents (m, new String [0], false, false, false, 500, 70, 12);
    }
    
    public DDTablePanel (DDTableModel m, String [] toolTips, int width, int height){
        initComponents (m, toolTips, true, true, true, width, height, 12);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents (DDTableModel m, String [] toolTips,
				 boolean allowAdd, boolean allowRemove,
				 boolean allowEdit, int width, int height,
				 int insetVal) {
	JPanel      buttonPanel;

	model = m;
	sortCol = -1;
	
	setBorder (new EmptyBorder (insetVal, insetVal, insetVal, insetVal));
	setLayout (new BorderLayout ());

	scrollPane = new javax.swing.JScrollPane ();
	tab = new JTable (model);
	tab.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);

	scrollPane.setViewportView (tab);
	add (scrollPane, BorderLayout.CENTER);

	buttonPanel = new JPanel ();
	buttonPanel.setLayout (new FlowLayout ());


	addButton = new JButton ();
//	addButton.setText (bundle.getString ("CTL_Add"));
    org.openide.awt.Mnemonics.setLocalizedText(addButton, bundle.getString ("CTL_Add")); // NOI18N	
    addButton.addActionListener (new ActionListener () {
	    public void actionPerformed (ActionEvent evt) {
	        addButtonActionPerformed (evt);
	    }
	});
	buttonPanel.add (addButton);
	addButton.setVisible(allowAdd);
        
        editButton = new JButton ();
//	editButton.setText (bundle.getString ("CTL_Edit"));
    org.openide.awt.Mnemonics.setLocalizedText(editButton, bundle.getString ("CTL_Edit")); // NOI18N	
	editButton.addActionListener (new ActionListener () {
	    public void actionPerformed (ActionEvent evt) {
		editButtonActionPerformed (evt);
	    }
	});
	buttonPanel.add (editButton);

	removeButton = new JButton ();
//	removeButton.setText (bundle.getString ("CTL_Remove"));
    org.openide.awt.Mnemonics.setLocalizedText(removeButton, bundle.getString ("CTL_Remove")); // NOI18N	
	removeButton.addActionListener (new ActionListener () {
	    public void actionPerformed (ActionEvent evt) {
	        removeButtonActionPerformed (evt);
	    }
	});
	buttonPanel.add (removeButton);
	removeButton.setEnabled(false);
	removeButton.setVisible(allowRemove);

	add (buttonPanel, BorderLayout.SOUTH);
	
	if (model instanceof SortableDDTableModel) {
	    addMouseListenerToHeader((SortableDDTableModel)model);
	}

	setHeaderToolTips (toolTips);

	if ((allowRemove) || (allowEdit)) {
	    tab.getSelectionModel().addListSelectionListener(
	         new ListSelectionListener() {
	             public void valueChanged(ListSelectionEvent e) {
		         if ((!e.getValueIsAdjusting()) &&
					(tab.getRowCount () > 0)) {
		             boolean rowSelected = tab.getSelectedRow() != -1;
		             removeButton.setEnabled(rowSelected);
			     editButton.setEnabled(rowSelected);
		         }
	             }
	         }
            );
	}

	if (allowEdit) {
	    tab.addMouseListener(new MouseAdapter() {
	        public void mouseClicked(MouseEvent me) {
	            if (me.getClickCount() == 2) {
		        int row = tab.rowAtPoint(me.getPoint());
		        if (row != -1) {
		            tab.setRowSelectionInterval(row,row);
		            editButtonActionPerformed(null);          
		        }
	            }
	        }
	    });
	}

	editButton.setEnabled(false);
	editButton.setVisible (allowEdit);
	int tableWidth = initColumnSizes();
	if (tableWidth > width) {
	    width = tableWidth;
	}
	tab.setPreferredScrollableViewportSize (new Dimension(width, height));
	this.addAncestorListener(new AListener()); // BUG 4457984
        initAccessibility();
    } 

    private void initAccessibility(){
        getAccessibleContext().setAccessibleDescription(bundle.getString ("ACSD_DDTablePanel"));
        addButton.getAccessibleContext().setAccessibleDescription(bundle.getString ("ACSD_add"));
        editButton.getAccessibleContext().setAccessibleDescription(bundle.getString ("ACSD_edit"));
        removeButton.getAccessibleContext().setAccessibleDescription(bundle.getString ("ACSD_remove"));
        tab.getAccessibleContext().setAccessibleDescription(bundle.getString ("ACSD_DDTable"));
        tab.getAccessibleContext().setAccessibleName(model.getModelName());
    }

    public void addListSelectionListener (ListSelectionListener l) {
	tab.getSelectionModel ().addListSelectionListener (l);
    }

    public void addVerticalScrollBarAdjustmentListener (AdjustmentListener l) {
	scrollPane.getVerticalScrollBar ().addAdjustmentListener (l);
    }

    public void setVerticalScrollBarValue (int val) {
	int curVal = scrollPane.getVerticalScrollBar ().getValue ();
	if (curVal != val) {
	    scrollPane.getVerticalScrollBar ().setValue (val);
	}
    }
    
    public void linkLabel(JLabel label)
    {
        label.setLabelFor(tab);
    }

    public Color getHeaderColor () {
	if (tab.getColumnCount () < 1) {
	    return Color.black;
	}

	TableColumn c = tab.getColumnModel ().getColumn (0);
	TableCellRenderer cellR = c.getHeaderRenderer ();
	Component comp = cellR.getTableCellRendererComponent 
				        (tab, c.getHeaderValue (), false, 
					 false, -1, 0);
	return comp.getForeground ();
    }

    public int getSelectedRow () {
	return tab.getSelectedRow ();
    }

    public void setSelectedRow (int row) {
	if (row == -1) {
	    tab.clearSelection ();
	} else {
	    tab.addRowSelectionInterval (row, row);
	}
    }

    private void setHeaderToolTips (String [] toolTips) {

	for (int i = 0; i < model.getColumnCount(); i++) {
	    TableColumn c = tab.getColumnModel ().getColumn (i);
	    TableCellRenderer cellR = c.getHeaderRenderer ();
	    
	    DefaultTableCellRenderer label = new DefaultTableCellRenderer() {
                 public Component getTableCellRendererComponent (JTable table,
                                                                 Object value,
                         		                   boolean isSelected,
                                                             boolean hasFocus, 
					                  int row, int column)
                 {
                      return this;
		 }
	    };
            label.setHorizontalAlignment(JLabel.CENTER);
	    c.setHeaderRenderer (label);
	    cellR = label;

	    Component comp = cellR.getTableCellRendererComponent 
				        (tab, c.getHeaderValue (), false, 
					 false, -1, i);
	    if (comp instanceof JComponent
			&& i < toolTips.length) {
    		JComponent jComp = (JComponent) comp;
    		jComp.setToolTipText (toolTips [i]);
	    } 
      	}
    }

    private int maxSize(int col) {
        Component c = null;
	int max = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
	    c = tab.getDefaultRenderer(model.getColumnClass(col)).
                             getTableCellRendererComponent(
                                 tab, model.getValueAt(i,col),
                                 false, false, i, col);
	    if (c.getPreferredSize().width > max) {
	        max = c.getPreferredSize().width;
	    }
        }
	return max;
    }			    

    private int initColumnSizes() {
	int totWidth = tab.getIntercellSpacing().width * model.getColumnCount();
        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn column = tab.getColumnModel().getColumn(i);

            Component comp = column.getHeaderRenderer().
                             getTableCellRendererComponent(
                                 null, column.getHeaderValue(), 
                                 false, false, 0, 0);
	    /* 
	     * Note the +6 is so that there will be some "white" space
	     * on each side of the text in the header.
	     */
            int headerWidth = comp.getPreferredSize().width + 6;
            int cellWidth = maxSize(i);
	    totWidth += Math.max (headerWidth, cellWidth);
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
	return totWidth;
    } 			    

    public void setCellEditor (int col, TableCellEditor cEdit) {
	tab.getColumnModel ().getColumn (col).setCellEditor (cEdit);
    }   

    private void addButtonActionPerformed(ActionEvent evt) {
	editRow(tab.getSelectedRow(), true);
    }

    private void closeEditDialog() {
        editDialog.setVisible(false);
        editDialog.dispose();
	editDialog = null;
    }			    

    public void editSelectedRow () {
	editRow (tab.getSelectedRow (), false);
    }

    private void editRow(final int row, final boolean isNew) {
        final DDTableModelEditor editor = model.getEditor();
	//
	// Changed how new is done for bug 4457984.
	//
	final Object obj = (isNew?model.makeNewElement():model.getValueAt(row));
	editor.setValue(obj);
	JPanel p = new JPanel();
	JPanel editorPanel = editor.getPanel ();        
	HelpCtx hCtx = null; // HelpCtx.findHelp (editorPanel);        
	if (hCtx != null) {
	    HelpCtx.setHelpIDString (p, hCtx.getHelpID ());
	}
	p.add(BorderLayout.NORTH, editorPanel);
        p.getAccessibleContext().setAccessibleDescription(editorPanel.getAccessibleContext().getAccessibleDescription());
	String editType = isNew?bundle.getString("TTL_ADD"):
	                        bundle.getString("TTL_EDIT");
	String title = MessageFormat.format(bundle.getString("TTL_DIALOG"),
					    new Object[] {
	                                        editType, 
						model.getModelName()
                                            });

	if (p.getAccessibleContext().getAccessibleDescription () == null) {
	    p.getAccessibleContext().setAccessibleDescription (title);
	}

        DialogDescriptor dd = new DialogDescriptor(p, title, 
            true, NotifyDescriptor.OK_CANCEL_OPTION, null, 
            DialogDescriptor.BOTTOM_ALIGN, null, 
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
		   if (evt.getSource() != NotifyDescriptor.OK_OPTION) {
		       if (isNew) {
			    //
			    // BUG 4457984 - the new was cancelled, so
			    // tell the model. This allows the app server
			    // api to be told.
			    //
			    model.newElementCancelled(obj);
		       }
		   } else {
                       java.util.List errors = 
                           model.isValueValid
					(editor.getValue (), isNew?-1:row);
                       if (! errors.isEmpty()) {
                           Component errorComp = getErrorComponent (("MSG_TableErrors"),errors);// NOI18N
                           errorComp.getAccessibleContext().setAccessibleDescription(getA11yText(errors));
                           NotifyDescriptor d = new NotifyDescriptor(errorComp,
                                         MessageFormat.format(
                                            bundle.getString("TTL_DIALOG"),
                                            new Object[] { 
				              bundle.getString("TTL_ERROR"),
						model.getModelName()}),
					 NotifyDescriptor.OK_CANCEL_OPTION,
					 NotifyDescriptor.ERROR_MESSAGE,
				     new Object[]{NotifyDescriptor.OK_OPTION},
					 null);
	                    DialogDisplayer.getDefault().notify(d);
			    return;
                       } else {
			   if (!isNew) {
			       if (! model.isEditValid 
						(editor.getValue (), row)) {
				   return;
			       }

			       int[] rows = tab.getSelectedRows();
			       tab.clearSelection();
			       model.setValueAt(row, editor.getValue());
			       if (rows != null) {
				   for (int i = 0; i < rows.length; i++) {
				       tab.setRowSelectionInterval(rows[i], 
								   rows[i]);
				   }
			       }
			   } else {
			       //
			       // BUG 4457984 - pass the row created above
			       // to the next level so that it can store
			       // the original row created.
			       //
			       model.addRowAt(row >= model.getRowCount() ? model.getRowCount()-1 :
                                              row, obj, editor.getValue());
			   } 
			}
                   }
		   closeEditDialog(); 
                }
            });
        dd.setClosingOptions(new Object[0]);
        // XXX workaround for issuezilla 12542
        JButton cancel = new JButton(bundle.getString("LBL_Cancel"));
        cancel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Cancel"));
        cancel.setVerifyInputWhenFocusTarget(false);
        dd.setOptions(new Object[] { 
                              NotifyDescriptor.OK_OPTION,
                              cancel});
        editDialog = DialogDisplayer.getDefault().createDialog(dd);
        editDialog.setVisible(true);
    }
    			    

    private void editButtonActionPerformed (ActionEvent evt) {
        editRow(tab.getSelectedRow(), false);
    }

    private void removeButtonActionPerformed (ActionEvent evt) {
	int row = tab.getSelectedRow ();
	java.util.List errors = model.canRemoveRow (row);
	if (! (errors.isEmpty ())) {
	    Component c = getErrorComponent ("MSG_RemoveWarning", errors);	// NOI18N
            NotifyDescriptor.Confirmation confirmDesc =
                           new NotifyDescriptor.Confirmation (c, 
					bundle.getString ("CTL_Remove") + 
					" " + model.getModelName () + " " +	// NOI18N
					bundle.getString ("TTL_WARNING"));
            Object [] options = new Object [] { bundle.getString ("CTL_Remove"),
                           		    NotifyDescriptor.CANCEL_OPTION };
            confirmDesc.setOptions (options);

            Object o = DialogDisplayer.getDefault().notify(confirmDesc);
            if (o == NotifyDescriptor.CANCEL_OPTION) {
                return ;
            }
	}

	tab.removeRowSelectionInterval(row,row);

	//
	// The following is needed to make sure those rows that have
	// associated app server properties are set up so the plug-in
	// knows they are being deleted. Creating the editor causes
	// the model subclasses to call the plug-in making sure it
	// has loaded its information about these rows (See EjbRefAppSrvEditor
	// and ejb/EditableDDRef).
	//
        DDTableModelEditor editor = model.getEditor();
	editor.setValue(model.getValueAt(row));
	model.removeRowAt (row);
    }

    private Component getErrorComponent 
				(String summaryString, java.util.List errors) {
	if (errors.size () == 1) {
	    return new MultiLineField ((String) errors.get (0));
	}

	MessageArea msg = new MessageArea ();
	Object [] o = new Object [] { model.getModelName() };
	msg.setText (MessageFormat.format 
				(bundle.getString (summaryString), o));
	Iterator iter = errors.iterator ();
	while (iter.hasNext ()) {
	    msg.appendBulletItem ((String) iter.next ());
	}
	return msg;
    }

    private String getA11yText(java.util.List errors) {
        Object [] o = new Object [] { model.getModelName() };
        StringBuffer buf =new StringBuffer(MessageFormat.format(bundle.getString("MSG_TableErrors"),o));
        for (int i=0;i<errors.size();i++) {
            buf.append("\n"+(String)errors.get(i)); //NOI18N
        }
        return buf.toString();
    }
    
    /**
     * Add listener for table header clicks. This invokes sort on the clicked
     * column.
     */
    private void addMouseListenerToHeader(final SortableDDTableModel model) { 
        MouseAdapter listMouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tab.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX()); 
                int column = tab.convertColumnIndexToModel(viewColumn); 
                if (e.getClickCount() == 1 && column != -1) {
                    model.setSortColumn(sortCol = column); 
                }
            }
        };
        JTableHeader th = tab.getTableHeader(); 
        th.addMouseListener(listMouseListener); 
    }

    public Object getPropertyValue () throws IllegalStateException {
        getPropertyValueCalled = true;
	return model.getValue ();
    }

    boolean getPropertyValueCalled = false;

    //
    // Added to fix bug 4457984 - check for a cancel.
    //
    class AListener implements AncestorListener {
	public void ancestorAdded(AncestorEvent event) {
	}
	public void ancestorMoved(AncestorEvent event) {
	}
	public void ancestorRemoved(AncestorEvent event) {
	    if (!DDTablePanel.this.isShowing()) {
	    	if (getPropertyValueCalled) {
		    // User pressed OK
		} else {
		    // User cancelled
		    DDTablePanel.this.model.editsCancelled();
		}
	    }
	}
    }
}
