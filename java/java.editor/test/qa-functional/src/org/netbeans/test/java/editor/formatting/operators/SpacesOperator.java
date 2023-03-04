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
