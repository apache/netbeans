/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
