/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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


