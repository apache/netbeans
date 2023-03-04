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
package org.netbeans.jellytools.modules.java.editor;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ListModel;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.spi.editor.codegen.CodeGenerator;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class GenerateCodeOperator {
                
    public static final String GENERATE_CONSTRUCTOR = Bundle.getString("org.netbeans.modules.java.editor.codegen.Bundle", "LBL_constructor"); //I18N

    public static final String GENERATE_GETTER = Bundle.getString("org.netbeans.modules.java.editor.codegen.Bundle", "LBL_getter"); //I18N
    
    public static final String GENERATE_SETTER = Bundle.getString("org.netbeans.modules.java.editor.codegen.Bundle", "LBL_setter"); //I18N
    
    public static final String GENERATE_GETTER_SETTER = Bundle.getString("org.netbeans.modules.java.editor.codegen.Bundle", "LBL_getter_and_setter"); //I18N
    
    public static final String GENERATE_EQUALS_HASHCODE = Bundle.getString("org.netbeans.modules.java.editor.codegen.Bundle", "LBL_equals_and_hashcode"); //I18N

    public static final String GENERATE_EQUALS = Bundle.getString("org.netbeans.modules.java.editor.codegen.Bundle", "LBL_equals"); //I18N

    public static final String GENERATE_HASHCODE = Bundle.getString("org.netbeans.modules.java.editor.codegen.Bundle", "LBL_hashcode"); //I18N
    
    public static final String OVERRIDE_METHOD = Bundle.getString("org.netbeans.modules.java.editor.codegen.Bundle", "LBL_override_method"); //I18N
            
    public static final String IMPLEMENT_METHOD = Bundle.getString("org.netbeans.modules.java.editor.codegen.Bundle", "LBL_implement_method"); //I18N
    
    public static final String DELEGATE_METHOD = Bundle.getString("org.netbeans.modules.java.editor.codegen.Bundle", "LBL_delegate_method"); //I18N
    /**
     * Opens requested code generation dialog
     * @param type Displayname of menu item
     * @param editor Operator of editor window where should be menu opened
     * @return true is item is found, false elsewhere
     */
    public static boolean openDialog(String type, EditorOperator editor) {
        new EventTool().waitNoEvent(1000);
        editor.pushKey(KeyEvent.VK_INSERT, KeyEvent.ALT_DOWN_MASK);
        JDialogOperator jdo = new JDialogOperator();
        new EventTool().waitNoEvent(1000);
        JListOperator list = new JListOperator(jdo);        
        ListModel lm = list.getModel();
        for (int i = 0; i < lm.getSize(); i++) {
            CodeGenerator cg  = (CodeGenerator) lm.getElementAt(i);
            if(cg.getDisplayName().equals(type)) {
                list.setSelectedIndex(i);
                jdo.pushKey(KeyEvent.VK_ENTER);
                new EventTool().waitNoEvent(1000);
                return true;
            }
        }
        return false;        
    }

    /**
     * Compares list of items provided in the Insert Code dialog with the list of expected items
     * @param editor Operator of editor window where should Insert Code should be caled
     * @param items Expected items
     * @return true if both list are the same, false otherwise
     */
    public static boolean containsItems(EditorOperator editor, String ... items) {
        Set<String> actItems = new HashSet<String>();
        List<String> expItems = Arrays.asList(items);
        editor.pushKey(KeyEvent.VK_INSERT, KeyEvent.ALT_DOWN_MASK);
        JDialogOperator jdo = new JDialogOperator();        
        JListOperator list = new JListOperator(jdo);
        ListModel lm = list.getModel();
        for (int i = 0; i < lm.getSize(); i++) {
            CodeGenerator cg  = (CodeGenerator) lm.getElementAt(i);
            actItems.add(cg.getDisplayName());
            if(!expItems.contains(cg.getDisplayName())) return false;
        }
        for (String string : expItems) {
            if(!actItems.contains(string)) return false;            
        }
        return true;       
    }

    /**
     * Check if Insertcode popup contains requested item
     * @param editor Operator of editor window where should Insert Code should be caled
     * @param items Expected items
     * @return true if all requested item are pressent, to exact match use {@link #containsItems(org.netbeans.jellytools.EditorOperator, java.lang.String[]) containsItems}
     */
    public static boolean checkItems(EditorOperator editor, String ... items) {        
        Set<String> expItems = new HashSet<String>(Arrays.asList(items));
        editor.pushKey(KeyEvent.VK_INSERT, KeyEvent.ALT_DOWN_MASK);
        JDialogOperator jdo = new JDialogOperator();
        JListOperator list = new JListOperator(jdo);
        ListModel lm = list.getModel();
        for (int i = 0; i < lm.getSize(); i++) {
            CodeGenerator cg  = (CodeGenerator) lm.getElementAt(i);
            expItems.remove(cg.getDisplayName());            
        }
        if(!expItems.isEmpty()) return false;
        return true;
    }

}
