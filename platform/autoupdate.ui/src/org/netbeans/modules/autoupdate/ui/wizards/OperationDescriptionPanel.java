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

package org.netbeans.modules.autoupdate.ui.wizards;

import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import org.netbeans.modules.autoupdate.ui.Utilities;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jiri Rechtacek
 */
public class OperationDescriptionPanel extends javax.swing.JPanel {
    private String tpPrimaryTitleText;
    private String tpPrimaryPluginsText;
    private String tpDependingTitleText;
    private String tpDependingPluginsText;
    private static String ACD_TEXT_HTML;
    /** Creates new form OperationDescriptionPanel */
    public OperationDescriptionPanel (String primary, String primaryU, String depending, String dependingU, boolean hasRequired) {
        assert EventQueue.isDispatchThread();
        this.tpPrimaryTitleText = primary;
        this.tpPrimaryPluginsText = primaryU;
        this.tpDependingTitleText = depending;
        this.tpDependingPluginsText = dependingU;
        customInitComponents (hasRequired);
    }
    
    // XXX: cannot be designed by mattise
    private void customInitComponents (boolean hasRequired) {
        ACD_TEXT_HTML = NbBundle.getMessage (OperationDescriptionPanel.class, "ACD_Text_Html");
        tpPrimaryTitle = new javax.swing.JTextPane();
        tpPrimaryPlugins = new javax.swing.JTextPane();
        tpDependingTitle = new javax.swing.JTextPane();
        tpDependingPlugins = new javax.swing.JTextPane();

        tpPrimaryTitle.setContentType("text/html"); // NOI18N
        tpPrimaryTitle.setEditable(false);
        tpPrimaryTitle.setOpaque (false);
        tpPrimaryTitle.getAccessibleContext ().setAccessibleName (tpPrimaryTitleText);
        tpPrimaryTitle.getAccessibleContext ().setAccessibleName (ACD_TEXT_HTML);
        tpPrimaryTitle.setBackground( new Color(0,0,0,0) );
        tpPrimaryTitle.putClientProperty( JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE );

        tpPrimaryPlugins.setContentType ("text/html"); // NOI18N
        tpPrimaryPlugins.setEditable(false);
        tpPrimaryPlugins.setOpaque (false);
        tpPrimaryPlugins.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hlevt) {
                if (EventType.ACTIVATED == hlevt.getEventType()) {
                    if (hlevt.getURL () != null) {
                        Utilities.showURL(hlevt.getURL());
                    }
                }
            }
        });
        tpPrimaryPlugins.getAccessibleContext ().setAccessibleName (tpPrimaryPluginsText);
        tpPrimaryPlugins.getAccessibleContext ().setAccessibleName (ACD_TEXT_HTML);
        tpPrimaryPlugins.setBackground( new Color(0,0,0,0) );
        tpPrimaryPlugins.putClientProperty( JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE );


        tpDependingTitle.setContentType ("text/html"); // NOI18N
        tpDependingTitle.setEditable(false);
        tpDependingTitle.setOpaque (false);
        tpDependingTitle.getAccessibleContext ().setAccessibleName (tpDependingTitleText);
        tpDependingTitle.getAccessibleContext ().setAccessibleName (ACD_TEXT_HTML);
        tpDependingTitle.setBackground( new Color(0,0,0,0) );
        tpDependingTitle.putClientProperty( JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE );

        tpDependingPlugins.setContentType ("text/html"); // NOI18N
        tpDependingPlugins.setEditable(false);
        tpDependingPlugins.setOpaque (false);
        tpDependingPlugins.getAccessibleContext ().setAccessibleName (tpDependingPluginsText);
        tpDependingPlugins.getAccessibleContext ().setAccessibleName (ACD_TEXT_HTML);
        tpDependingPlugins.setBackground( new Color(0,0,0,0) );
        tpDependingPlugins.putClientProperty( JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE );

        tpPrimaryTitle.setText(tpPrimaryTitleText);
        tpPrimaryPlugins.setText(tpPrimaryPluginsText);
        tpDependingTitle.setText(tpDependingTitleText);
        tpDependingPlugins.setText(tpDependingPluginsText);
        boolean hasPrimary = tpPrimaryPluginsText.length () > 0 || tpPrimaryTitleText.length () > 0;
        
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup (getHorizontalGroup (layout, hasPrimary, hasRequired));
        layout.setVerticalGroup (getVerticalGroup (layout, hasPrimary, hasRequired));
        
        tpPrimaryPlugins.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hlevt) {
                if (EventType.ACTIVATED == hlevt.getEventType()) {
                    if (hlevt.getURL () != null) {
                        Utilities.showURL(hlevt.getURL());
                    }
                }
            }
        });
        tpDependingPlugins.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hlevt) {
                if (EventType.ACTIVATED == hlevt.getEventType()) {
                    if (hlevt.getURL () != null) {
                        Utilities.showURL(hlevt.getURL());
                    }
                }
            }
        });
        tpPrimaryTitle.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hlevt) {
                if (EventType.ACTIVATED == hlevt.getEventType()) {
                    if (hlevt.getURL () != null) {
                        Utilities.showURL(hlevt.getURL());
                    }
                }
            }
        });
        tpDependingTitle.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hlevt) {
                if (EventType.ACTIVATED == hlevt.getEventType()) {
                    if (hlevt.getURL () != null) {
                        Utilities.showURL(hlevt.getURL());
                    }
                }
            }
        });
    }
    
    private GroupLayout.ParallelGroup getVerticalGroup (GroupLayout layout, boolean hasPrimary, boolean hasRequired) {
        GroupLayout.ParallelGroup res = layout.createParallelGroup (/* XXX huh? GroupLayout.PREFERRED_SIZE*/);
        GroupLayout.SequentialGroup seq = layout.createSequentialGroup ();
        if (hasPrimary) {
            seq.addComponent (tpPrimaryTitle, GroupLayout.DEFAULT_SIZE, 40, 40)
                .addPreferredGap (LayoutStyle.ComponentPlacement.RELATED)
                .addComponent (tpPrimaryPlugins, GroupLayout.PREFERRED_SIZE, tpPrimaryPlugins.getPreferredSize ().height, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap (LayoutStyle.ComponentPlacement.RELATED)
                .addGap (0, 30, 30);
        }
        if (hasRequired) {
            seq.addComponent (tpDependingTitle, GroupLayout.DEFAULT_SIZE, 80, 80)
                    .addPreferredGap (LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent (tpDependingPlugins, GroupLayout.PREFERRED_SIZE, tpDependingPlugins.getPreferredSize ().height, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap (LayoutStyle.ComponentPlacement.RELATED);
        }
        res.addGroup (seq);
        return res;
    }
    
    private GroupLayout.ParallelGroup getHorizontalGroup (GroupLayout layout, boolean hasPrimary, boolean hasRequired) {
        GroupLayout.ParallelGroup res = layout.createParallelGroup (Alignment.LEADING);
        if (hasPrimary) {
            res.addGroup (Alignment.TRAILING, layout.createSequentialGroup ()
                    .addGroup (layout.createParallelGroup (Alignment.TRAILING)
                    .addGroup (Alignment.LEADING, layout.createSequentialGroup ()
                    .addGap (49, 49, 49)
                    .addComponent (tpPrimaryPlugins, GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE))
                    .addGroup (Alignment.LEADING, layout.createSequentialGroup ()
                    .addContainerGap ()
                    .addComponent (tpPrimaryTitle, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)))
                    .addContainerGap ());
        }
        if (hasRequired) {
            res.addGroup (Alignment.TRAILING, layout.createSequentialGroup ()
                    .addGroup (layout.createParallelGroup (Alignment.TRAILING)
                    .addGroup (Alignment.LEADING, layout.createSequentialGroup ()
                    .addGap (49, 49, 49)
                    .addComponent (tpDependingPlugins, GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE))
                    .addGroup (Alignment.LEADING, layout.createSequentialGroup ()
                    .addContainerGap ()
                    .addComponent (tpDependingTitle, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)))
                    .addContainerGap ());
        }
        return res;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tpPrimaryTitle = new javax.swing.JTextPane();
        tpPrimaryPlugins = new javax.swing.JTextPane();
        tpDependingTitle = new javax.swing.JTextPane();
        tpDependingPlugins = new javax.swing.JTextPane();

        tpPrimaryTitle.setContentType("text/html"); // NOI18N
        tpPrimaryTitle.setEditable(false);

        tpPrimaryPlugins.setContentType("text/html"); // NOI18N
        tpPrimaryPlugins.setEditable(false);

        tpDependingTitle.setContentType("text/html"); // NOI18N
        tpDependingTitle.setEditable(false);

        tpDependingPlugins.setContentType("text/html"); // NOI18N
        tpDependingPlugins.setEditable(false);

        tpPrimaryTitle.setText(tpPrimaryTitleText);
        tpPrimaryPlugins.setText(tpPrimaryPluginsText);
        tpDependingTitle.setText(tpDependingTitleText);
        tpDependingPlugins.setText(tpDependingPluginsText);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addComponent(tpDependingPlugins, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tpDependingTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addComponent(tpPrimaryPlugins, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tpPrimaryTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(tpPrimaryTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tpPrimaryPlugins)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tpDependingTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tpDependingPlugins, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(90, 90, 90))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane tpDependingPlugins;
    private javax.swing.JTextPane tpDependingTitle;
    private javax.swing.JTextPane tpPrimaryPlugins;
    private javax.swing.JTextPane tpPrimaryTitle;
    // End of variables declaration//GEN-END:variables
    
}
