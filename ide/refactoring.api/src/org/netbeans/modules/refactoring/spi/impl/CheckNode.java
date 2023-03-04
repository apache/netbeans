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
package org.netbeans.modules.refactoring.spi.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.tree.*;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.openide.text.PositionBounds;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.ExpandableTreeElement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;

/**
 * @author Pavel Flaska
 */
public final class CheckNode extends DefaultMutableTreeNode {

    public static final int SINGLE_SELECTION = 0;
    public static final int DIG_IN_SELECTION = 4;
  
    private int selectionMode;
    private boolean isSelected;
    private boolean isQuery;

    private String nodeLabel;
    private Icon icon;
    
    private boolean disabled = false;
    private boolean needsRefresh = false;
    private static Icon found = ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/api/resources/found_item_orange.png", false);
    
    public CheckNode(Object userObject, String nodeLabel, Icon icon, boolean isQuery) {
        super(userObject, !(userObject instanceof RefactoringElement) || userObject instanceof ExpandableTreeElement);
        this.isSelected = true;
        setSelectionMode(DIG_IN_SELECTION);
        this.isQuery = isQuery;
        this.nodeLabel = nodeLabel;
        this.icon = icon;
        if (userObject instanceof TreeElement) {
            if (((TreeElement)userObject).getUserObject() instanceof RefactoringElement) {
                RefactoringElement ree = (RefactoringElement) ((TreeElement)userObject).getUserObject();
                int s = ree.getStatus();
                
                PositionBounds bounds = getPosition();
                if (isQuery && bounds != null) {
                    int line = 0;
                    try {
                        line = bounds.getBegin().getLine() + 1;
                    } catch (IOException ioe) {
                    }


                    this.nodeLabel = "<font color='!controlShadow'>" + getLineString(line,4) + "</font>" + nodeLabel; //NOI18N
                    if (this.icon==null) {
                        this.icon = found;
                    }
                }
                
                if (s==RefactoringElement.GUARDED || s==RefactoringElement.READ_ONLY) {
                    isSelected = false;
                    disabled = true;
                    this.nodeLabel = "[<font color=#CC0000>" // NOI18N
                            + NbBundle.getMessage(CheckNode.class, s==RefactoringElement.GUARDED?"LBL_InGuardedBlock":"LBL_InReadOnlyFile")
                            + "</font>]" + this.nodeLabel; // NOI18N
                }
            }
        }

        if (userObject instanceof ExpandableTreeElement) {
            add(new CheckNode("Please wait", "Please wait...", null, isQuery));
        }
    }
    
    String getLabel() {
        return nodeLabel;
    }

    void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }
    
    Icon getIcon() {
        return icon;
    }
    
    public void setDisabled() {
        disabled = true;
        isSelected = false;
        removeAllChildren();
    }
    
    boolean isDisabled() {
        return disabled;
    }

    void setNeedsRefresh() {
        needsRefresh = true;
        setDisabled();
    }
    
    boolean needsRefresh() {
        return needsRefresh;
    }
    
    public void setSelectionMode(int mode) {
        selectionMode = mode;
    }

    public int getSelectionMode() {
        return selectionMode;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        if (userObject instanceof TreeElement) {
            Object ob = ((TreeElement) userObject).getUserObject();
            if (ob instanceof RefactoringElement) {
                    ((RefactoringElement) ob).setEnabled(isSelected);
            }
        }
        if ((selectionMode == DIG_IN_SELECTION) && (children != null)) {
            Enumeration e = children.elements();      
            while (e.hasMoreElements()) {
                CheckNode node = (CheckNode)e.nextElement();
                node.setSelected(isSelected);
            }
        }
    }

    public boolean isSelected() {
        if (userObject instanceof TreeElement) {
            Object ob = ((TreeElement) userObject).getUserObject();
            if (ob instanceof RefactoringElement) {
                return ((RefactoringElement) ob).isEnabled() &&
                        !((((RefactoringElement) ob).getStatus() == RefactoringElement.GUARDED) || (((RefactoringElement) ob).getStatus() == RefactoringElement.READ_ONLY));
            }
        }
        return isSelected;
    }
    
    public PositionBounds getPosition() {
        if (userObject instanceof TreeElement) {
            Object re = ((TreeElement) userObject).getUserObject();
            if (re instanceof RefactoringElement)
                return ((RefactoringElement) re).getPosition();
        }
        return null;
    }
    
    private String tooltip;
    public String getToolTip() {
        if (tooltip==null) {
            if (userObject instanceof TreeElement) {
                Object re = ((TreeElement) userObject).getUserObject();
                if (re instanceof RefactoringElement) {
                    RefactoringElement ree = (RefactoringElement) re;
                    PositionBounds bounds = getPosition();
                    FileObject file = ree.getParentFile();
                    if (bounds != null && file!=null) {
                        int line;
                        try {
                            line = bounds.getBegin().getLine() + 1;
                        } catch (IOException ioe) {
                            return null;
                        }
                        tooltip = FileUtil.getFileDisplayName(file) + ':' + line;
                    }
                }
            }
        }
        return tooltip;
    }

    private String getLineString(int line, int size) {
        String l = Integer.toString(line);
        int length = size-l.length();
        for (int i=0; i < length*2; i++) {
            l = "&nbsp;" + l; //NOI18N
        }
        return l + ":&nbsp;&nbsp;"; //NOI18N
    }
    
    private static final RequestProcessor WORKER = new RequestProcessor(CheckNode.class.getName(), 1, true, false);
    private boolean childrenFilled;
    
    public synchronized void ensureChildrenFilled(final DefaultTreeModel model) {
        if (!childrenFilled) {
            childrenFilled = true;

            if (userObject instanceof ExpandableTreeElement) {
                WORKER.post(new Runnable() {
                    @Override public void run() {
                        final List<TreeElement> subelements = new ArrayList<TreeElement>();
                        
                        for (TreeElement el : ((ExpandableTreeElement) userObject)) {
                            subelements.add(el);
                        }

                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() {
                                for (TreeElement el : subelements) {
                                    insert(new CheckNode(el, el.getText(/*XXX:*/true), el.getIcon(), isQuery), getChildCount() - 1);
                                }
                                int[] added = new int[getChildCount() - 1];
                                for (int i = 0; i < added.length; i++) {
                                    added[i] = i;
                                }
                                model.nodesWereInserted(CheckNode.this, added);
                                int childCount = getChildCount();
                                Object last = getChildAt(childCount - 1);
                                int index = model.getIndexOfChild(CheckNode.this, last);
                                remove(index); //remove the please wait node
                                model.nodesWereRemoved(CheckNode.this, new int[] {index}, new Object[] {last});
                            }
                        });
                    }
                });
            }
        }
    }
}
