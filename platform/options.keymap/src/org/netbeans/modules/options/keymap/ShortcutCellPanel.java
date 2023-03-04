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
 * ShortcutCell.java
 *
 * Created on Oct 8, 2008, 10:35:16 AM
 */

package org.netbeans.modules.options.keymap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.UIResource;
import org.openide.util.Utilities;

/**
 * Panel representing one shortcut cell inside keymap table
 * @author Max Sauer
 */
public class ShortcutCellPanel extends javax.swing.JPanel implements Comparable, Popupable {
    
    private Popup popup;
    private final SpecialkeyPanel specialkeyList;

    PopupFactory factory = PopupFactory.getSharedInstance();
    /** Creates new form ShortcutCell */
    public ShortcutCellPanel() {
        initComponents();
        specialkeyList = new SpecialkeyPanel(this, scField);

        // close the popup when user clicks elsewhere
        changeButton.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                hidePopup();
            }
        });

        // close popup when escape pressed and return to editing state
        changeButton.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    //XXX: find a better way
                    JTable table = (JTable) scField.getParent().getParent();
                    final int editingRow = table.getEditingRow();
                    table.editCellAt(editingRow, 1);
                    table.setRowSelectionInterval(editingRow, editingRow);
                    scField.requestFocus();
                    
                    return;
                }
            }

        });

        //set a different icon for more button in edit mode, since it offers different popup
        scField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                changeButton.setText(""); // NOI18N
                changeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/options/keymap/more_opened.png")));
            }

            @Override
            public void focusLost(FocusEvent e) {
                changeButton.setIcon(null);
                changeButton.setText("..."); // NOI18N
            }
        });
        setFocusable(true);
    }

    ShortcutCellPanel(String displayedShortcut) {
        this();
        setText(displayedShortcut);
    }

    /**
     * Sets the shortcut text represenation for this table cell
     * @param shortcut
     */
    public void setText(String shortcut) {
        scField.setText(shortcut);
    }

    public void hidePopup() {
        if (popup != null) {
            popup.hide();
            popup = null;
        }
    }

    @Override
    public String toString() {
        return scField.getText();
    }

    @Override
    public void setSize(Dimension d) {
        super.setSize(d);
        int buttonWidth = changeButton.getPreferredSize().width;
        scField.setPreferredSize(new Dimension(d.width - buttonWidth, d.height));

        changeButton.setPreferredSize(new Dimension(buttonWidth, d.height));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Color c = getBackground();
        if (c instanceof UIResource) {
            // Nimbus LaF: if the color is a UIResource, it will paint
            // the leftover area with default JPanel color which is gray, which looks ugly
            super.setBackground(new Color(c.getRGB()));
        }
        super.paintComponent(g);
        super.setBackground(c);
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        // propagate potential alternate row color to the field's background
        if (scField != null) {
            scField.setBackground(bg);
        }
    }
    
    void setBgColor(Color col) {
        setBackground(col);
        // Nimbus LaF
        Color c = UIManager.getDefaults().getColor("control"); // NOI18N
        if (c == null) {
            c = new Color(204, 204, 204);
        }
        changeButton.setBackground(c);
    }

    void setFgCOlor(Color col, boolean selected) {
        Color def;
        
        if (selected) {
            // Nimbus laf
            def = UIManager.getDefaults().getColor("List[Selected].textForeground"); // NOI18N
        } else {
            // Nimbus laf
            def = UIManager.getDefaults().getColor("Label.foreground"); // NOI18N
        }
        if (def != null) {
            if (!(def instanceof UIResource)) {
                col = new ColorUIResource(def);
            } else {
                col = def;
            }
        }
        scField.setForeground(col);
    }

    public JButton getButton() {
        return changeButton;
    }

    public JTextField getTextField() {
        return scField;
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scField = new ShortcutTextField();
        changeButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(204, 204, 204));
        setPreferredSize(new java.awt.Dimension(134, 15));

        scField.setText(org.openide.util.NbBundle.getMessage(ShortcutCellPanel.class, "ShortcutCellPanel.scField.text")); // NOI18N
        scField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        changeButton.setBackground(new java.awt.Color(204, 204, 204));
        org.openide.awt.Mnemonics.setLocalizedText(changeButton, org.openide.util.NbBundle.getMessage(ShortcutCellPanel.class, "ShortcutCellPanel.changeButton.text")); // NOI18N
        changeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        changeButton.setMaximumSize(new java.awt.Dimension(25, 15));
        changeButton.setMinimumSize(new java.awt.Dimension(25, 15));
        changeButton.setPreferredSize(new java.awt.Dimension(25, 15));
        changeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(scField, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(changeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(scField, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(changeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void changeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeButtonActionPerformed
        JComponent tf = changeButton;
        Point p = new Point(tf.getX(), tf.getY());
        SwingUtilities.convertPointToScreen(p, this);
        //show special key popup
        if (popup == null) {
            changeButton.setText(""); // NOI18N
            changeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/options/keymap/more_closed.png")));
            if (Utilities.isUnix()) {
                // #156869 workaround, force HW for Linux
                popup = PopupFactory.getSharedInstance().getPopup(null, specialkeyList, p.x, p.y + tf.getHeight());
            } else {
                popup = factory.getPopup(this, specialkeyList, p.x, p.y + tf.getHeight());
            }
            popup.show();
        } else {
            changeButton.setText(""); // NOI18N
            changeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/options/keymap/more_opened.png")));
            hidePopup();
        }
    }//GEN-LAST:event_changeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton changeButton;
    private javax.swing.JTextField scField;
    // End of variables declaration//GEN-END:variables

    public int compareTo(Object o) {
        return this.toString().compareTo(o.toString());
    }

    public Popup getPopup() {
        return popup;
    }

    void setButtontext(String text) {
        changeButton.setText(text);
    }

}
