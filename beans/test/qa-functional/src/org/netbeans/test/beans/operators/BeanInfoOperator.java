/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.test.beans.operators;

import java.awt.Component;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.test.beans.BeansTestCase;
import org.netbeans.test.beans.BeansTestCase.NodeConverter;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

/**
 *
 * @author jprox
 */
public class BeanInfoOperator extends TopComponentOperator {
    
    private JTreeOperator tree;
    private PropertySheetOperator propertySheetOperator;   

    public BeanInfoOperator(String name) {
        this(name,0);
    }
    
    public BeanInfoOperator(String name, int index) {
        super(waitTopComponent(null, name, index, new BeanInfoDesignerSubchooser()));
    }
    
    public JTreeOperator getTreeOperator() {
        if(tree == null) {
            tree = new JTreeOperator(this);
        }
        return tree;
    }
    
    public PropertySheetOperator getPropertySheetOperator() {
        if(propertySheetOperator== null) {
            propertySheetOperator = new PropertySheetOperator(this);
        }
        return propertySheetOperator;
    }
    
    public void selectNode(String nodeName) {
	JTreeOperator tree = getTreeOperator();
	tree.selectPath(BeansTestCase.getTreePath(tree, nodeName, defaultConverter));
    }
    
    public void setPropertyValues(String nodeName, PropertyValue... propertyValues) {        
        this.selectNode(nodeName);
        PropertySheetOperator pso = this.getPropertySheetOperator();
        for (PropertyValue propertyValue : propertyValues) {
            new Property(pso, propertyValue.index).setValue(propertyValue.value);
        }                
        pso.updateUI();
    }
    
    public TreePath findPath(String node) {
        return BeansTestCase.getTreePath(getTreeOperator(), node,defaultConverter);
    }
    
    private static final class BeanInfoDesignerSubchooser implements ComponentChooser {

        @Override
        public boolean checkComponent(Component comp) {            
            return comp.getClass().getName().contains("BIEditorSupport$BeanInfoElement");
        }

        @Override
        public String getDescription() {
            return " org.netbeans.modules.beans.beaninfo.BIEditorSupport$BeanInfoElement";
        }
    }
    
    public static NodeConverter defaultConverter = new BeansTestCase.NodeConverter() {
        @Override
        public String getDisplayName(TreeNode node) {
            Node n = Visualizer.findNode(node);
            return n.getDisplayName();
        }
    };
    
    public static class PropertyValue {
        private int index;
        private String value;

        public PropertyValue(int index, String value) {
            this.index = index;
            this.value = value;
        }                
    }
    
}
