
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
 * TabbedWidget.java
 *
 * Created on March 28, 2007, 2:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import javax.swing.AbstractAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.websvc.design.view.layout.TableLayout;

/**
 *
 * @author Ajit Bhate
 */
public class TabbedPaneWidget extends Widget {
    
    private static final Color TAB_BORDER_COLOR = new Color(169, 197, 235);
    private static final Color SELECTED_TAB_COLOR = Color.WHITE;
    private static final Color TAB_COLOR = new Color(232,232,232);
        
    private Widget tabs;
    private Widget contentWidget;
    
    private ButtonWidget selectedTab;
    private Widget selectedTabComponent;
    
    /**
     *
     * @param scene
     */
    public TabbedPaneWidget(Scene scene) {
        super(scene);
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 1));
        tabs = new Widget(scene);
        addChild(tabs);
        contentWidget = new Widget(scene);
        contentWidget.setLayout(LayoutFactory.createOverlayLayout());
        contentWidget.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 1, 1, TAB_BORDER_COLOR));
        addChild(contentWidget);
    }
    
    /**
     * Adds given TabWidget to this TabbedPaneWidget
     * @param tabWidget the TabWidget to be added.
     */
    public void addTab(TabWidget tabWidget) {
        addTab(tabWidget.getTitle(),tabWidget.getIcon(),tabWidget.getComponentWidget());
    }

    /**
     *
     * @param tabTitle
     * @param tabIcon
     * @param tabComponent
     */
    public void addTab(String tabTitle, Image tabIcon, final Widget tabComponent) {
        contentWidget.addChild(tabComponent);
        tabComponent.setVisible(false);
        final ButtonWidget tab = new ButtonWidget(getScene(), tabIcon, tabTitle) {
            protected boolean isAimingAllowed() {
                return false;
            }
        };
        tab.setBorder(new TabBorder(this,tab));
        tab.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                if(selectedTabComponent != tabComponent) {
                    if(selectedTab!=null)
                        selectedTab.setLabelFont(getScene().getFont());
                    selectedTab = tab;
                    selectedTab.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
                    if(selectedTabComponent!=null)
                        selectedTabComponent.setVisible(false);
                    selectedTabComponent = tabComponent;
                    selectedTabComponent.setVisible(true);
                    contentWidget.revalidate(true);
                }
            }
        });
        tabs.addChild(tab);
        tabs.setLayout(new TableLayout(tabs.getChildren().size(), 0, 0, 20));
        if(selectedTab==null) {
            selectedTab = tab;
            selectedTab.setLabelFont(getScene().getFont().deriveFont(Font.BOLD));
            selectedTabComponent = tabComponent;
            selectedTabComponent.setVisible(true);
        }
    }
    
    private static class TabBorder implements Border {
        private static final int radius = 2;
        private Insets insets = new Insets(2*radius, 3*radius, radius, 3*radius);
        private final TabbedPaneWidget tabbedPane;
        private final Widget tab;
        
        public TabBorder(TabbedPaneWidget tabbedPane,Widget tab) {
            this.tabbedPane=tabbedPane;
            this.tab=tab;
        }
        
        public Insets getInsets() {
            return insets;
        }
        
        public void paint(Graphics2D g2, Rectangle rect) {
            Paint oldPaint = g2.getPaint();
            
            Arc2D arc = new Arc2D.Double(rect.x - radius + 0.5f, rect.y + rect.height - radius *2 +0.5f,
                    radius*2, radius*2, -90, 90, Arc2D.OPEN);
            GeneralPath gp = new GeneralPath(arc);
            arc = new Arc2D.Double(rect.x+radius+0.5f, rect.y+0.5f,
                    radius*4, radius*4, 180, -90, Arc2D.OPEN);
            gp.append(arc,true);
            arc = new Arc2D.Double(rect.x + rect.width - radius*6 +1f, rect.y+0.5f,
                    radius*4, radius*4, 90, -90, Arc2D.OPEN);
            gp.append(arc,true);
            arc = new Arc2D.Double(rect.x + rect.width - radius*2 +1f, rect.y + rect.height - radius*2 +0.5f,
                    radius*2, radius*2, 180, 90, Arc2D.OPEN);
            gp.append(arc,true);
            if (tabbedPane.selectedTab==tab) {
                g2.setPaint(SELECTED_TAB_COLOR);
                g2.fill(gp);
            } else {
                g2.setPaint(TAB_COLOR);
                g2.fill(gp);
                gp.closePath();
            }
            g2.setPaint(TAB_BORDER_COLOR);
            if (tab.getState().isFocused()) {
                Stroke s = g2.getStroke ();
                g2.setStroke (new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_MITER, new float[] {2,2}, 0));
                g2.drawRect (rect.x+radius*3, rect.y+radius*2,rect.width-radius*6,rect.height-radius*4);
                g2.setStroke (s);
            }
            g2.draw(gp);
            
            g2.setPaint(oldPaint);
        }
        
        public boolean isOpaque() {
            return true;
        }
        
    }
}
