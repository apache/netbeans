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

package org.netbeans.modules.xml.text.navigator.base;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * A base implementation of XML Navigator UI.
 *
 * @author Samaresh
 * @version 1.0
 */
public abstract class AbstractXMLNavigatorContent extends javax.swing.JPanel
    implements ExplorerManager.Provider, PropertyChangeListener {
    
    protected ExplorerManager explorerManager;
    protected TreeView treeView;
    private final JPanel emptyPanel;
    private JLabel msgLabel;
    private Icon waitIcon;
    
    //Error messages that can be used in showError() call.
    public static String ERROR_NO_DATA_AVAILABLE    = "LBL_NotAvailable";       //NOI18N
    public static String ERROR_TOO_LARGE_DOCUMENT   = "LBL_TooLarge";           //NOI18N
    public static String ERROR_CANNOT_NAVIGATE      = "LBL_CannotNavigate";     //NOI18N
    
    /**
     * 
     */
    public AbstractXMLNavigatorContent() {
        explorerManager = new ExplorerManager();
        explorerManager.addPropertyChangeListener(this);
        treeView = new BeanTreeView();
        //init empty panel
        setBackground(UIManager.getColor("Tree.textBackground"));
        emptyPanel = new JPanel();
        emptyPanel.setBackground(UIManager.getColor("Tree.textBackground"));
        emptyPanel.setLayout(new BorderLayout());
        msgLabel = new JLabel();
        emptyPanel.add(msgLabel, BorderLayout.CENTER);
    }
       
    /**
     * 
     * @param dataObject 
     */
    public abstract void navigate(DataObject dataObject);
        
    /**
     * 
     */
    public void release() {
        removeAll();
        repaint();        
    }
    
    public void propertyChange(PropertyChangeEvent event) {        
    }
    
    public ExplorerManager getExplorerManager() {
	return explorerManager;
    }
    
    /**
     * 
     */
    public void showWaitNode() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               treeView.setRootVisible(true);
               explorerManager.setRootContext(new WaitNode());
            } 
        });
    }
    
    protected boolean isLoading() {
        return false;
    }
    
    public void showError(final String message) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    showError(message);
                }
            });
            return;
        }
        removeAll();
        msgLabel.setIcon(null);
        msgLabel.setForeground(Color.GRAY);
        msgLabel.setText(NbBundle.getMessage(AbstractXMLNavigatorContent.class, message));
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(emptyPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    
    public void showWaitPanel() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (!isLoading()) {
                        return;
                    }
                    showWaitPanel();
                }
                
            });
        }
        removeAll();
        if (waitIcon == null) {
            waitIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/xml/text/navigator/resources/wait.gif", false); //NOI18N
        }
        msgLabel.setIcon(waitIcon);
        msgLabel.setHorizontalAlignment(SwingConstants.LEFT);
        msgLabel.setForeground(Color.BLACK);
        msgLabel.setText(NbBundle.getMessage(AbstractXMLNavigatorContent.class, "LBL_Wait"));
        add(emptyPanel, BorderLayout.NORTH);
        revalidate();
        repaint();
    }
       
    private static class WaitNode extends AbstractNode {
        
        private Image waitIcon = ImageUtilities.loadImage("org/netbeans/modules/xml/text/navigator/resources/wait.gif"); // NOI18N
        
        WaitNode( ) {
            super( Children.LEAF );
        }
        
        @Override
        public Image getIcon(int type) {
             return waitIcon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @java.lang.Override
        public java.lang.String getDisplayName() {
            return NbBundle.getMessage(AbstractXMLNavigatorContent.class, "LBL_Wait");
        }
        
    }
    
  }


