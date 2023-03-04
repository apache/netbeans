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
package org.netbeans.qa.form;

import java.awt.Component;
import javax.swing.JLabel;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JLabelOperator;

/**
 *
 * @author adam.senk@oracle.com
 * 
 * This is class is workaround. Since JDK7 has JellyTools problem with some L&Fs.
 * Clicking on a category doesn't repaint the OptionsOperator-> it is impossible to change
 * option in selected category.
 */
public class OptionsForFormOperator extends OptionsOperator {
    
    /** Selects a category with given name.
     * @param name name of category to be selected
     */
    public void selectCategory(final String name) {
        new EventTool().waitNoEvent(300);  // prevent clicking on category button when panel not initialized
        final StringComparator comparator = this.getComparator();
        new JLabelOperator(this, new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                if(comp.getClass().getName().equals("org.netbeans.modules.options.OptionsPanel$CategoryButton")||// NOI18N
                        comp.getClass().getName().equals("org.netbeans.modules.options.OptionsPanel$NimbusCategoryButton")) { // NOI18N
                    if(((JLabel)comp).getText() != null) {
                        return comparator.equals(((JLabel)comp).getText(), name);
                    }
                }
                return false;
            }
            public String getDescription() {
                return "OptionsPanel$CategoryButton with text "+name; // NOI18N
            }
        }).clickMouse(2);
    }
}
    

