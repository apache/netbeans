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

package org.netbeans.modules.progress.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.openide.util.Mutex;

/**
 * @author mkleint
 */
public class PopupPane extends JScrollPane {
    private JPanel view;
    private Set<ListComponent> listComponents;
    private ListComponent selected;
    
    public PopupPane() {
        listComponents = new HashSet<ListComponent>();
        view = new JPanel();
        GridLayout grid = new GridLayout(0, 1);
        grid.setHgap(0);
        grid.setVgap(0);
        view.setLayout(grid);
        view.setBorder(BorderFactory.createEmptyBorder());
        setName("progresspopup"); //NOI18N
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        setViewportView(view);
        setFocusable(true);
        setRequestFocusEnabled(true);

        Action down = new MoveDownAction();
        getActionMap().put("Move-Down", down);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "Move-Down");
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "Move-Down");
        
        Action up = new MoveUpAction();
        getActionMap().put("Move-Up", up);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "Move-Up");
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "Move-Up");
        Action cancel = new CancelAction();
        getActionMap().put("Cancel-Task", cancel);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Cancel-Task");
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Cancel-Task");
        
        Action select = new SelectAction();
        getActionMap().put("select-task", select);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "select-task");
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "select-task");
        
        
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }
    
    public void addListComponent(final ListComponent lst) {
        Mutex.EVENT.readAccess(new Runnable() {            
            public @Override void run() {
                listComponents.add(lst);
                if (view.getComponentCount() > 0) {
                    JComponent previous = (JComponent)view.getComponent(view.getComponentCount() - 1);
                    previous.setBorder(new BottomLineBorder());
                }
                lst.setBorder(BorderFactory.createEmptyBorder());
                view.add(lst);
                if (listComponents.size() > 3) {
                    setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                } else {
                    setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                }
            }
        });
    }
    
    public void removeListComponent(final InternalHandle handle) {
        Mutex.EVENT.readAccess(new Runnable() {            
            public @Override void run() {
                Iterator<ListComponent> it = listComponents.iterator();
                while (it.hasNext()) {
                    ListComponent comp = it.next();
                    if (comp.getHandle() == handle) {
                        view.remove(comp);
                        it.remove();
                        break;
                    }
                }
                if (view.getComponentCount() > 0) {
                    // remove bottom border from last component
                    JComponent last = (JComponent)view.getComponent(view.getComponentCount() - 1);
                    last.setBorder(null);
                }
                if (listComponents.size() > 3) {
                    setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                } else {
                    setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                }
            }
        });
    }

    public @Override Dimension getPreferredSize() {
        int count = view.getComponentCount();
        int height = count > 0 ? view.getComponent(0).getPreferredSize().height : 0;
        int offset = count > 3 ? height * 3 + 5 : (count * height) + 5;
        // 22 is the width of the additional scrollbar
        return new Dimension(count >3 ? ListComponent.ITEM_WIDTH + 22 
                                      : ListComponent.ITEM_WIDTH + 2, offset);
    }

    /**
     * bold font is now used not only for explicitly selected items, but for any 
     * change in currently selected task.
     */
    public void updateBoldFont(final InternalHandle handle) {
        Mutex.EVENT.readAccess(new Runnable() {            
            public @Override void run() {
                for (ListComponent comp : listComponents) {
                    comp.markAsActive(handle == comp.getHandle());
                }
            }
        });
    }
    
    
    
    private static class BottomLineBorder implements Border {
        private Insets ins = new Insets(0, 0, 1, 0);
        private Color col;
        
        public BottomLineBorder () {
            col = UIManager.getColor("Separator.foreground"); // NOI18N
            if (col == null) {
                col = new Color(221, 229, 248);
            }
        }
        
        public @Override Insets getBorderInsets(Component c) {
            return ins;
        }

        public @Override boolean isBorderOpaque() {
            return false;
        }

        public @Override void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
           Color old = g.getColor();
           g.setColor(col);
           g.drawRect(x, y + height - 2,  width, 1);
           g.setColor(old);
        }
    }
    
    private int findIndex(Component comp) {
        Component[] comps = view.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] == comp) {
                return i;
            }
        }
        return -1;
    }

    public @Override void requestFocus() {
//#63666 - don't focus any of the tasks explicitly, wait for user action.
//        if (view.getComponentCount() > 1) {
//            if (selected == null || !selected.isDisplayable()) {
//                selected = (ListComponent)view.getComponent(0);
//            }
//            selected.requestFocus();
//        } else {
            super.requestFocus();
//        }
    }
    
    private class MoveDownAction extends AbstractAction {
        
        MoveDownAction() {
        }
         
        public @Override void actionPerformed(ActionEvent actionEvent) {
            int index = -1;
            if (selected != null) {
                index = findIndex(selected);
            }
            index = index + 1;
            if (index >= PopupPane.this.view.getComponentCount()) {
                index = 0;
            }
            selected = (ListComponent)PopupPane.this.view.getComponent(index);
            selected.requestFocus();
        }
        
    }
    
    private class MoveUpAction extends AbstractAction {
        
        MoveUpAction() {
        }
         
        public @Override void actionPerformed(ActionEvent actionEvent) {
            int index = PopupPane.this.view.getComponentCount();
            if (selected != null) {
                index = findIndex(selected);
//                selected.setBackground(new Color(249, 249, 249));
            }
            index = index - 1;
            if (index < 0) {
                index = PopupPane.this.view.getComponentCount() - 1;
            }
            selected = (ListComponent)PopupPane.this.view.getComponent(index);
            selected.requestFocus();
//            selected.setBackground(selectBgColor);
//            selected.scrollRectToVisible(selected.getBounds());
        }
    }
    
    private class CancelAction extends AbstractAction {
        public CancelAction () {}
        
        public @Override void actionPerformed(ActionEvent actionEvent) {
            if (selected != null) {
                Action act = selected.getCancelAction();
                if (act != null) {
                    act.actionPerformed(actionEvent);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }
    }

    private class SelectAction extends AbstractAction {
        public SelectAction () {}
        
        public @Override void actionPerformed(ActionEvent actionEvent) {
            if (selected != null) {
                selected.getHandle().requestExplicitSelection();
            }
        }
    }
    
}
