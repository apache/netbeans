/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011-2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.breadcrumbs;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import org.netbeans.api.actions.Openable;
import org.netbeans.editor.JumpList;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsController;
import org.openide.awt.HtmlRenderer;
import org.openide.awt.HtmlRenderer.Renderer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ListView;
import org.openide.explorer.view.NodeRenderer;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author lahvac
 */
public class BreadCrumbComponent<T extends JLabel&Renderer> extends JComponent implements PropertyChangeListener {

    private final Image SEPARATOR = ImageUtilities.loadImage("org/netbeans/modules/editor/breadcrumbs/resources/separator.png");
    
    public BreadCrumbComponent() {
        setPreferredSize(new Dimension(0, COMPONENT_HEIGHT));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                expand(e);
            }
        });
    }

    private static final int USABLE_HEIGHT = 19;
    private static final int LEFT_SEPARATOR_INSET = 2;
    private static final int RIGHT_SEPARATOR_INSET = 10;
    private static final int ICON_TEXT_SEPARATOR = 5;
    private static final int START_INSET = 8;
    private static final int MAX_ROWS_IN_POP_UP = 20;
    public static final int COMPONENT_HEIGHT = USABLE_HEIGHT;

    private ExplorerManager seenManager;
    
    private PropertyChangeListener weakPCL;

    private ExplorerManager findManager() {
        ExplorerManager manager = ExplorerManager.find(this);

        if (seenManager != manager) {
            if (seenManager != null && weakPCL != null) {
                seenManager.removePropertyChangeListener(weakPCL);
                weakPCL = null;
            }
            if (manager != null) {
                weakPCL = WeakListeners.propertyChange(this, manager);
                manager.addPropertyChangeListener(weakPCL);
            }
            seenManager = manager;
        }

        assert manager != null;

        return manager;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        if (nodes == null) measurePrepaint();

        assert nodes != null;
        
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
            setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        
        int height = getHeight();
        
        if (nodes.length == 0) {
            g.drawImage(SEPARATOR, START_INSET, (height - SEPARATOR.getHeight(null)) / 2, null);
            return ;
        }
        
        int x = START_INSET;

        for (int i = 0; i < nodes.length; i++) {
            configureForNode(nodes[i]);
            Dimension preferred = renderer.getPreferredSize();
            int labelY = (height - preferred.height) / 2;
            g.translate(x, labelY);
            renderer.setSize(preferred);
            renderer.paint(g);
            g.translate(-x, -labelY);

            x += sizes[i];
            
            g.drawImage(SEPARATOR, x + LEFT_SEPARATOR_INSET, (height - SEPARATOR.getHeight(null)) / 2, null);

            x += LEFT_SEPARATOR_INSET + SEPARATOR.getWidth(null) + RIGHT_SEPARATOR_INSET;
        }
    }

    private final T renderer = (T) HtmlRenderer.createLabel();
    private Node[] nodes;
    private double[] sizes;
    private double height;

    private void measurePrepaint() {
        List<Node> path = computeNodePath();

        int i = 0;
        
        nodes = path.toArray(new Node[path.size()]);
        sizes = new double[path.size()];
        
        int xTotal = 0;
        
        height = /*XXX*/0;

        for (Node n : nodes) {
            configureForNode(n);
            Dimension preferedSize = renderer.getPreferredSize();
            xTotal += sizes[i] = preferedSize.width;
            
            height = Math.max(height, preferedSize.height);

            i++;
        }

        setPreferredSize(new Dimension((int) (xTotal + (nodes.length - 1) * (LEFT_SEPARATOR_INSET + SEPARATOR.getWidth(null) + RIGHT_SEPARATOR_INSET) + START_INSET), USABLE_HEIGHT/*(int) (height + 2 * INSET_HEIGHT)*/));
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                measurePrepaint();
                repaint();
            }
        });
        
    }

    private void expand(MouseEvent e) {
        int clickX = e.getPoint().x;
        int elemX = START_INSET;

        for (int i = 0; i < sizes.length; i++) {
            int startX = elemX;
            elemX += sizes[i];

            elemX += LEFT_SEPARATOR_INSET;
            
            if (clickX <= elemX) {
                //found:
                List<Node> path = computeNodePath();
                Node selected = path.get(i);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    open(selected);
                } else {
                    expand(startX, selected);
                }
                return ;
            }
            
            startX = elemX;
            elemX += SEPARATOR.getWidth(null);
            
            if (clickX <= elemX) {
                //found:
                List<Node> path = computeNodePath();
                expand(startX, path.get(i));
                return ;
            }
            
            elemX += RIGHT_SEPARATOR_INSET;
        }
    }

    private List<Node> computeNodePath() {
        ExplorerManager manager = findManager();
        List<Node> path = new ArrayList<Node>();
        Node sel = manager.getExploredContext();

        // see #223480; root context need not be the root of the node structure.
        Node stopAt = manager.getRootContext().getParentNode();
        while (sel != null && sel != stopAt) {
            path.add(sel);
            sel = sel.getParentNode();
        }

        path.remove(path.size() - 1); //XXX

        Collections.reverse(path);
        
        return path;
    }

    private void open(Node node) {
        Openable openable = node.getLookup().lookup(Openable.class);

        if (openable != null) {
            JumpList.checkAddEntry();
            openable.open();
        }
    }
    
    private void expand(int startX, final Node what) {
        if (what.getChildren().getNodesCount() == 0) return ;
        
        final ExplorerManager expandManager = new ExplorerManager();
        class Expanded extends JPanel implements ExplorerManager.Provider {
            public Expanded(LayoutManager layout) {
                super(layout);
            }
            @Override public ExplorerManager getExplorerManager() {
                return expandManager;
            }
        }
        final JPanel expanded = new Expanded(new BorderLayout());
        expanded.setBorder(new LineBorder(Color.BLACK, 1));
        
        final ListView listView = new ListView() {
            {
                int nodesCount = what.getChildren().getNodesCount();
                
                if (nodesCount >= MAX_ROWS_IN_POP_UP) {
                    list.setVisibleRowCount(MAX_ROWS_IN_POP_UP);
                } else {
                    list.setVisibleRowCount(nodesCount);
                    
                    NodeRenderer nr = new NodeRenderer();
                    int i = 0;
                    int width = getPreferredSize().width;
                    
                    for (Node n : what.getChildren().getNodes()) {
                        if (nr.getListCellRendererComponent(list, n, i, false, false).getPreferredSize().width > width) {
                            Dimension pref = getPreferredSize();
                            pref.height += getHorizontalScrollBar().getPreferredSize().height;
                            setPreferredSize(pref);
                            break;
                        }
                    }
                }
            }
        };
        listView.setPopupAllowed(false);
        expanded.add(listView, BorderLayout.CENTER);
        expandManager.setRootContext(what);
        
        Point place = new Point(startX, 0);
        
        SwingUtilities.convertPointToScreen(place, this);
        
        expanded.validate();
        
        final Popup popup = PopupFactory.getSharedInstance().getPopup(this, expanded, place.x, place.y - expanded.getPreferredSize().height);
        final AWTEventListener multicastListener = new AWTEventListener() {
            @Override public void eventDispatched(AWTEvent event) {
                    if (event instanceof MouseEvent && ((MouseEvent) event).getClickCount() > 0) {
                        Object source = event.getSource();
                        
                        while (source instanceof Component) {
                            if (source == expanded) return ; //accept
                            source = ((Component) source).getParent();
                        }
                        
                        popup.hide();
                        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                    }
            }
        };
        
        Toolkit.getDefaultToolkit().addAWTEventListener(multicastListener, AWTEvent.MOUSE_EVENT_MASK);
        
        expandManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    Node[] selected = expandManager.getSelectedNodes();
                    if (selected.length == 1) {
                        open(selected[0]);
                        popup.hide();
                        Toolkit.getDefaultToolkit().removeAWTEventListener(multicastListener);
                    }
                }
            }
        });
        
        popup.show();
    }

    private void configureForNode(Node node) {
        renderer.reset();
        
        Image nodeIcon = node.getIcon(BeanInfo.ICON_COLOR_16x16);
        Icon icon = nodeIcon != null && nodeIcon != BreadcrumbsController.NO_ICON ? ImageUtilities.image2Icon(nodeIcon) : null;
        int width = icon != null ? icon.getIconWidth() : 0;
        if (width > 0) {
            renderer.setIcon(icon);
            renderer.setIconTextGap(ICON_TEXT_SEPARATOR);
        } else {
            renderer.setIcon(null);
            renderer.setIconTextGap(0);
        }
        String html = node.getHtmlDisplayName();
        if (html != null) {
            renderer.setHtml(true);
            renderer.setText(html);
        } else {
            renderer.setHtml(false);
            renderer.setText(node.getDisplayName());
        }
        renderer.setFont(getFont());
    }
    
}
