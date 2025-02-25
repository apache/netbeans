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
package org.netbeans.modules.refactoring.spi.impl;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.api.actions.Openable;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * This listener controls click and double click on the CheckNodes. In addition
 * to it provides support for keyboard node checking/unchecking and opening
 * document.
 *
 * todo (#pf): Improve behaviour and comments.
 *
 * @author  Pavel Flaska
 */
class CheckNodeListener implements MouseListener, KeyListener {

    private final boolean isQuery;

    public CheckNodeListener(boolean isQuery) {
        this.isQuery = isQuery;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // todo (#pf): we need to solve problem between click and double
        // click - click should be possible only on the check box area
        // and double click should be bordered by title text.
        // we need a test how to detect where the mouse pointer is
        JTree tree = (JTree) e.getSource();
        Point p = e.getPoint();
        int x = e.getX();
        int y = e.getY();
        int row = tree.getRowForLocation(x, y);
        TreePath path = tree.getPathForRow(row);

        // if path exists and mouse is clicked exactly once
        if (path != null) {
            CheckNode node = (CheckNode) path.getLastPathComponent();
            if (isQuery) {
                if (e.getClickCount() == 2) {
                    Object o = node.getUserObject();
                    if (o instanceof Openable) {
                        ((Openable) o).open();
                    } else if (o instanceof TreeElement) {
                        o = ((TreeElement) o).getUserObject();
                        if (o instanceof RefactoringElement || o instanceof FileObject || o instanceof Openable) {
                            if(!findInSource(node)) {
                                if (tree.isCollapsed(row)) {
                                    tree.expandRow(row);
                                } else {
                                    tree.collapseRow(row);
                                }
                            }
                        } else {
                            if (tree.isCollapsed(row)) {
                                tree.expandRow(row);
                            } else {
                                tree.collapseRow(row);
                            }
                        }
                    } else {
                        if (tree.isCollapsed(row)) {
                            tree.expandRow(row);
                        } else {
                            tree.collapseRow(row);
                        }
                    }
                } else if (e.getClickCount() == 1) {
                    Object o = node.getUserObject();
                    if (o instanceof TreeElement) {
                        o = ((TreeElement) o).getUserObject();
                        if (o instanceof RefactoringElement) {
                                openDiff(node);
                        }
                    }
                }
            } else {
                Rectangle chRect = CheckRenderer.getCheckBoxRectangle();
                Rectangle rowRect = tree.getPathBounds(path);
                chRect.setLocation(chRect.x + rowRect.x, chRect.y + rowRect.y);
                if (e.getClickCount() == 1 && chRect.contains(p) && !node.isDisabled()) {
                    boolean isSelected = !(node.isSelected());
                    node.setSelected(isSelected);
                    Object o = node.getUserObject();
                    if (o instanceof TreeElement) {
                        o = ((TreeElement) o).getUserObject();
                        if (o instanceof RefactoringElement) {
                                openDiff(node);
                            }
                        }
                    ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                    if (row == 0) {
                        tree.revalidate();
                    }
                    tree.repaint();
                } // double click, open the document
                else if (e.getClickCount() == 2 && chRect.contains(p) == false) {
                    Object o = node.getUserObject();
                    if (o instanceof TreeElement) {
                        o = ((TreeElement) o).getUserObject();
                        if (o instanceof RefactoringElement || o instanceof FileObject) {
                            findInSource(node);
                        } 
                    } else if (o instanceof Openable) {
                        ((Openable) o).open();
                    } else {
                        if (tree.isCollapsed(row)) {
                            tree.expandRow(row);
                        } else {
                            tree.collapseRow(row);
                        }
                    }
                } else if (e.getClickCount() == 1 && chRect.contains(p) == false) {
                    Object o = node.getUserObject();
                    if (o instanceof TreeElement) {
                        o = ((TreeElement) o).getUserObject();
                        if (o instanceof RefactoringElement) {
                            openDiff(node);
                        }
//                        else if (o instanceof FileObject) {
//                            tree.expandPath(path);
//                            TreePath pathForRow = tree.getPathForRow(row+1);
//                            CheckNode lastPathComponent = (CheckNode) pathForRow.getLastPathComponent();
//                            Object userObject = lastPathComponent.getUserObject();
//                            if (userObject instanceof TreeElement) {
//                                Object refElement = ((TreeElement) userObject).getUserObject();
//                                if (refElement instanceof RefactoringElement)
//                                    openDiff(lastPathComponent);
//                            }
//                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN) {
            JTree tree = (JTree) e.getSource();
            int row = tree.getSelectionRows()[0];
            TreePath path = tree.getSelectionPath();
            if (path != null) {
                CheckNode node = (CheckNode) path.getLastPathComponent();

                Object o = node.getUserObject();
                if (o instanceof TreeElement) {
                    o = ((TreeElement) o).getUserObject();
                    if (o instanceof RefactoringElement) {
                        openDiff(node);
                    }
//                    else if (o instanceof FileObject) {
//                        tree.expandPath(path);
//                        TreePath pathForRow = tree.getPathForRow(row + 1);
//                        CheckNode lastPathComponent = (CheckNode) pathForRow.getLastPathComponent();
//                        Object userObject = lastPathComponent.getUserObject();
//                        if (userObject instanceof TreeElement) {
//                            Object refElement = ((TreeElement) userObject).getUserObject();
//                            if (refElement instanceof RefactoringElement) {
//                                openDiff(lastPathComponent);
//                            }
//                        }
//                    }
                }
            }
        }
    }


    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent event) {
        JTree tree = (JTree) event.getSource();
        int x = event.getX();
        int y = event.getY();

        int row = tree.getRowForLocation(x, y);
        TreePath path = tree.getPathForRow(row);

        // if path exists and mouse is clicked exactly once
        if (path == null) {
            return;
        }
        CheckNode node = (CheckNode) path.getLastPathComponent();

        if ( !SwingUtilities.isRightMouseButton(event)) {
            return;
        }
        Object o = node.getUserObject();

        if ( !(o instanceof TreeElement)) {
            return;
        }
        o = ((TreeElement) o).getUserObject();

        if (o instanceof RefactoringElement) {
            showPopup(((RefactoringElement) o).getLookup().lookupAll(Action.class), tree, x, y);
        }
    }

    private void showPopup(Collection<? extends Action> actions, Component c, int x, int y) {
        if (actions.isEmpty()) {
            return;
        }
        JPopupMenu menu = new JPopupMenu();

        for (Action a:actions) {
            menu.add(a);
        }
        menu.show(c, x, y);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_SPACE) {
            JTree tree = (JTree) e.getSource();
            TreePath path = tree.getSelectionPath();
            if (path != null) {
                CheckNode node = (CheckNode) path.getLastPathComponent();
                node.setSelected(!node.isSelected());
                tree.repaint();
                e.consume();
            }
        } else {
            // Enter key was pressed, find the reference in document
            if (keyCode == KeyEvent.VK_ENTER) {
                JTree tree = (JTree) e.getSource();
                TreePath path = tree.getSelectionPath();
                if (path != null) {
                    CheckNode node = (CheckNode) path.getLastPathComponent();
                    findInSource(node);
                }
            }
        }

    }

    static boolean findInSource(CheckNode node) {
        Object o = node.getUserObject();
        if (o instanceof TreeElement) {
            if (o instanceof Openable) {
                ((Openable) o).open();
                return true;
            } else {
            o = ((TreeElement) o).getUserObject();
            if (o instanceof RefactoringElement) {
                APIAccessor.DEFAULT.getRefactoringElementImplementation((RefactoringElement) o).openInEditor();
                return true;
            } else if (o instanceof Openable) {
                ((Openable) o).open();
                return true;
            } else if (o instanceof FileObject) {
                try {
                    OpenCookie oc = DataObject.find((FileObject) o).getCookie(OpenCookie.class);
                    if (oc != null) {
                        oc.open();
                        return true;
                    }
                } catch (DataObjectNotFoundException ex) {
                    //ignore, unknown file, do nothing
                }
            }
            }
        }
        return false;
    }

    static void openDiff(CheckNode node) {
        Object o = node.getUserObject();
        if (o instanceof TreeElement) {
            o = ((TreeElement) o).getUserObject();
            if (o instanceof RefactoringElement) {
                APIAccessor.DEFAULT.getRefactoringElementImplementation((RefactoringElement) o).showPreview();
            }
        }
    }

    static void selectNextPrev(final boolean next, boolean isQuery, JTree tree) {
        int[] rows = tree.getSelectionRows();
        int newRow = rows == null || rows.length == 0 ? 0 : rows[0];
        int maxcount = tree.getRowCount();
        CheckNode node;
        do {
            if (next) {
                newRow++;
                if (newRow >= maxcount) {
                    newRow = 0;
                }
            } else {
                newRow--;
                if (newRow < 0) {
                    newRow = maxcount - 1;
                }
            }
            TreePath path = tree.getPathForRow(newRow);
            node = (CheckNode) path.getLastPathComponent();
            if (!node.isLeaf()) {
                tree.expandRow(newRow);
                maxcount = tree.getRowCount();
            }
        } while (!node.isLeaf());
        tree.setSelectionRow(newRow);
        verticalScrollRowToVisible(tree, newRow);
        CheckNodeListener.openDiff(node);
    }

    /**
     * Analog to {@link javax.swing.JTree#scrollRowToVisible(int)} but scrolls only vertically.
     */
    private static void verticalScrollRowToVisible(JTree tree, int row) {
        TreePath path = tree.getPathForRow(row);
        if (path != null) {
            tree.makeVisible(path);
            Rectangle bounds = tree.getPathBounds(path);
            bounds.setLocation(0, (int) bounds.getY());
            tree.scrollRectToVisible(bounds);
        }
    }

} // end CheckNodeListener
