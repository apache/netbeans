/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 ** Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.test.refactoring.operators;

import java.awt.Component;
import java.awt.Container;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import javax.swing.*;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JScrollPaneOperator;
import org.netbeans.jemmy.operators.JSplitPaneOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.modules.refactoring.java.ui.tree.FileTreeElement;
import org.netbeans.modules.refactoring.spi.impl.CheckNode;
import org.netbeans.modules.refactoring.spi.impl.RefactoringPanelContainer;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jiri.Prox@oracle.com, Marian.Mirilovic@oracle.com
 */
public class RefactoringResultOperator extends TopComponentOperator {

    private JButtonOperator _btRefresh;
    private JButtonOperator _btPrevious;
    private JButtonOperator _btNext;
    private JButtonOperator _btCancel;
    private JButtonOperator _btDoRefactor;
    private JToggleButtonOperator _tbtCollapse;
    private JToggleButtonOperator _tbtLogical;
    private JToggleButtonOperator _tbtPhysical;
    private JTabbedPaneOperator _tabpResults;

    private RefactoringResultOperator() {
        super(Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "LBL_Usages"));
    }

    private RefactoringResultOperator(String windowTitle) {
        super(windowTitle);
    }

    public static RefactoringResultOperator getFindUsagesResult() {
        return new RefactoringResultOperator(Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "LBL_Usages"));
    }

    public static RefactoringResultOperator getPreview() {
        return new RefactoringResultOperator(Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "LBL_Refactoring"));
    }

    public JTabbedPaneOperator getTabbedPane() {
        if (_tabpResults == null) {
            _tabpResults = new JTabbedPaneOperator(this);
        }
        return _tabpResults;
    }

    public int getTabCount() {
        getTabbedPane();
        if (_tabpResults == null) {
            return 0;
        }
        return _tabpResults.getTabCount();
    }

    public void selectTab(String name) {
        getTabbedPane();
        _tabpResults.selectPage(name);
    }

    public JPanel getRefactoringPanel() {
        Component source = this.getSource();
        Component[] components = ((JComponent) source).getComponents();
        JComponent content = (JComponent) components[0];

        if ("org.netbeans.modules.refactoring.spi.impl.RefactoringPanel".equals(content.getClass().getName())) {
            return (JPanel) content;
        }
        if ("org.netbeans.modules.refactoring.spi.impl.RefactoringPanelContainer".equals(content.getClass().getName())) {
            return ((RefactoringPanelContainer) content).getCurrentPanel();
        }
        if (content instanceof JTabbedPane) {
            JTabbedPane tab = (JTabbedPane) content;
            return (JPanel) tab.getSelectedComponent();
        }
        throw new IllegalArgumentException("Wrong structure " + content.getClass().getName());

    }

    private ContainerOperator getDoRefactoringCancelPanel() {
        JSplitPane splitPane = JSplitPaneOperator.findJSplitPane(getRefactoringPanel());
        JComponent leftComponent = (JComponent) splitPane.getLeftComponent();
        return new ContainerOperator((Container) leftComponent.getComponent(0));
    }

    public JTree getPreviewTree() {
        ContainerOperator ct = new ContainerOperator(getRefactoringPanel());
        JSplitPaneOperator splitPane = new JSplitPaneOperator(ct);
        JComponent leftComponent = (JComponent) splitPane.getLeftComponent();
        JScrollPane jScrollPane = JScrollPaneOperator.findJScrollPane(leftComponent);
        javax.swing.JViewport viewport = jScrollPane.getViewport();
        return (JTree) viewport.getComponent(0);
    }

    public ContainerOperator getToolbar() {
		return new OverflowToolbarOperator(this);
    }

    private JButtonOperator getToolbarButton(String buttonTooltip) {
		AbstractButton ab = ((OverflowToolbarOperator)getToolbar()).getButton(buttonTooltip);
		return new JButtonOperator((JButton) ab);
    }
    
    private JToggleButtonOperator getToolbarToogleButton(String buttonTooltip) {
		AbstractButton ab = ((OverflowToolbarOperator)getToolbar()).getButton(buttonTooltip);
		return new JToggleButtonOperator((JToggleButton) ab);
    }
    
    private JButtonOperator getRefresh() {
        if (_btRefresh == null) {
            _btRefresh = getToolbarButton(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "HINT_refresh")); // Refresh the refactoring data
        }
        return _btRefresh;
    }

    private JToggleButtonOperator getCollapse() {
        if (_tbtCollapse == null) {
            _tbtCollapse = getToolbarToogleButton(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "HINT_expandAll")); // Expand all nodes in the tree
        }
        return _tbtCollapse;
    }

    private JToggleButtonOperator getLogical() {
        if (_tbtLogical == null) {
            _tbtLogical = getToolbarToogleButton(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "HINT_logicalView")); // Show Logical View
        }
        return _tbtLogical;
    }

    private JToggleButtonOperator getPhysical() {
        if (_tbtPhysical == null) {
            _tbtPhysical = getToolbarToogleButton(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "HINT_physicalView")); // Show Physical View
			
		}
        return _tbtPhysical;
    }

    private JButtonOperator getPrev() {
        if (_btPrevious == null) {
            _btPrevious = getToolbarButton(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "HINT_prevMatch")); // Previous Occurence - Ctrl+Comma
        }
        return _btPrevious;
    }

    private JButtonOperator getNext() {
        if (_btNext == null) {
            _btNext = getToolbarButton(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "HINT_nextMatch")); // Next Occurence - Ctrl+Period
        }
        return _btNext;
    }

    private JButtonOperator getDoRefactor() {
        if (_btDoRefactor == null) {
            _btDoRefactor = new JButtonOperator(getDoRefactoringCancelPanel(), 0);
        }
        return _btDoRefactor;
    }

    private JButtonOperator getCancel() {
        if (_btCancel == null) {
            _btCancel = new JButtonOperator(getDoRefactoringCancelPanel(), 1);
        }
        return _btCancel;
    }

    public void doRefactoring() {
        getDoRefactor().pushNoBlock();
    }
    
    public void collapse() {
        getCollapse().pushNoBlock();
    }
    
    public void refresh() {
        getRefresh().pushNoBlock();
    }

    public void logical() {
        getLogical().push();
    }
    
    public boolean logicalIsSelected() {
        return getLogical().isSelected();
    }

    public void physical() {
        getPhysical().push();
    }
    
    public boolean physicalIsSelected() {
        return getPhysical().isSelected();
    }
    
    public void previous() {
        getPrev().push();
    }

    public void next() {
        getNext().push();
    }

    public void cancel() {
        getCancel().pushNoBlock();
    }

    public Set<FileObject> getInvolvedFiles() {
        return browseForFileObjects(getPreviewTree().getModel());
    }

    private Set<FileObject> browseForFileObjects(javax.swing.tree.TreeModel model) {
        Queue<CheckNode> q = new LinkedList<CheckNode>();
        q.add((CheckNode) model.getRoot());
        Set<FileObject> result = new HashSet<FileObject>();
        while (!q.isEmpty()) {
            CheckNode node = q.remove();
            Object uo = node.getUserObject();
            if (uo instanceof FileTreeElement) {
                FileTreeElement fileTreeElement = (FileTreeElement) uo;
                Object userObject = fileTreeElement.getUserObject();
                if (userObject instanceof FileObject) {
                    result.add((FileObject) userObject);
                } else {
                    throw new IllegalArgumentException("Object of type FileObject was expected, but got " + userObject.getClass().getName());
                }

            }
            for (int i = 0; i < model.getChildCount(node); i++) {
                q.add((CheckNode) model.getChild(node, i));
            }
        }
        return result;
    }

    /** Chooser which can be used to find a org.openide.awt.Toolbar component or
     * count a number of such components in given container.
     */
    private static class ToolbarChooser implements ComponentChooser {
        private String toolbarName;
        private StringComparator comparator;
        private int count = 0;
        
        /** Use this to find org.openide.awt.Toolbar component with given name. */
        public ToolbarChooser(String toolbarName, StringComparator comparator) {
            this.toolbarName = toolbarName;
            this.comparator = comparator;
        }
        
        /** Use this to count org.openide.awt.Toolbar components in given container. */
        public ToolbarChooser() {
            this.comparator = null;
        }
        
        @Override
        public boolean checkComponent(Component comp) {
            if(comp instanceof javax.swing.JToolBar) {
                count++;
                if(comparator != null) {
                    return comparator.equals(((javax.swing.JToolBar)comp).getName(), toolbarName);
                } else {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public String getDescription() {
            return "javax.swing.JToolBar";
        }
        
        public int getCount() {
            return count;
        }
    }
    
    /** Chooser which can be used to find a component with given tooltip,
     * for example a toolbar button.
     */
    private static class ToolbarButtonChooser implements ComponentChooser {
        private String buttonTooltip;
        private StringComparator comparator;
        
        public ToolbarButtonChooser(String buttonTooltip, StringComparator comparator) {
            this.buttonTooltip = buttonTooltip;
            this.comparator = comparator;
        }
        
        @Override
        public boolean checkComponent(Component comp) {
            return comparator.equals(((JComponent)comp).getToolTipText(), buttonTooltip);
        }
        
        @Override
        public String getDescription() {
            return "Toolbar button with tooltip \""+buttonTooltip+"\".";
        }
    }
    
}
