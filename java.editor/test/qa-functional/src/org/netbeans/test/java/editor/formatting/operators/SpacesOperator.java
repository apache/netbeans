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

package org.netbeans.test.java.editor.formatting.operators;

import java.awt.Component;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.openide.util.Exceptions;

/**
 *
 * @author jprox
 */
public class SpacesOperator  extends FormattingPanelOperator {

    private static final Map<String, Boolean> defaultValues = new HashMap<>();

    static boolean isModfied() {
        return !defaultValues.isEmpty();
    }
    
    public SpacesOperator(FormattingOptionsOperator formattingOperator) {
        super(formattingOperator, "Java", "Spaces");
        switchToPanel();
    }

    @Override
    public List<OperatorGetter> getAllOperatorGetters() {
        throw new UnsupportedOperationException("Restore default values is managed differently."); 
    }
    
    @Override
    public void restoreDefaultsValues() {                
        switchToPanel();
        for (String string : defaultValues.keySet()) {
            setValue(string, defaultValues.get(string));
        }
        defaultValues.clear();
        new EventTool().waitNoEvent(250);  //Timeout to propagate UI changes correctly
        
    }    
    
    private JTreeOperator jto;
    
    private JTreeOperator getTreeOperator() {
        if(jto==null) {
            jto = new JTreeOperator(this.formattingOperator);
        }
        return jto;
    }
    
    private Object[] findPath(JTreeOperator jto,String[] path) {
        Object[] resPath = new Object[path.length+1];
        Object act = jto.getRoot();
        
        resPath[0] = act;
        L1: for (int i = 0; i < path.length; i++) {
            String string = path[i];
            Object[] childs = jto.getChildren(act);
            for (Object child : childs) {
                if(child instanceof String) {
                    if(string.equals(child)) {                        
                        resPath[i+1] = child;                                
                        act=child;
                        continue L1;
                    }
                } else if(child instanceof DefaultMutableTreeNode) {
                    Object item = ((DefaultMutableTreeNode) child).getUserObject();
                    if(string.equals(item.toString())) {
                        resPath[i+1] = child;                                
                        act=child;
                        continue L1;
                    }                                                        
                } else throw new IllegalArgumentException("Invalid node type");                                    
            }            
        }    
        return resPath;        
    }
    
    private boolean getNodeState(DefaultMutableTreeNode node) {
        try {
            Object item = node.getUserObject();
            Field declaredField = item.getClass().getDeclaredField("value");
            declaredField.setAccessible(true);
            boolean state = declaredField.getBoolean(item);    
            return state;
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            throw new IllegalArgumentException("Cannot acces field",ex);
        }
    }
        
    public boolean setValue(String path, boolean value) {
        TreePath tp = new TreePath(this.findPath(getTreeOperator(), path.split("\\|")));
        boolean actState = getNodeState((DefaultMutableTreeNode) tp.getLastPathComponent());
        if(actState!=value) {
            if(!defaultValues.containsKey(path)) {
                defaultValues.put(path,actState);
            }
            jto.selectPath(tp);        
        } else {
            System.out.println("Setting same state!");
        }
        return actState;
    }

}
