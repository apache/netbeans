/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

/*
 * Abbreviations.java
 *
 * Created on 1/2/03 4:04 PM
 */
package org.netbeans.jellytools.modules.editor;

import java.awt.Component;
import java.util.*;
import javax.swing.JButton;
import javax.swing.table.TableModel;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;

/**
 * Class implementing all necessary methods for handling "Abbreviations" NbDialog.
 *
 * @author  Jan Lahoda
 * @author Max Sauer
 * @version 1.1
 */
public class Abbreviations {
    
    private OptionsOperator optionOperator;
    
    /** Creates new Abbreviations that can handle it.
     */
    public Abbreviations(OptionsOperator operator) {
        this.optionOperator = operator;        
    }    
    
    private JButtonOperator newButton;
    private JButtonOperator removeButton;
    private JComboBoxOperator languageCombo;
    private JComboBoxOperator expandOnCombo;
    private JTableOperator templateTable;
    private JTabbedPaneOperator detailTabbedPane;

    public JTabbedPaneOperator getDetailTabbedPane() {
        if(detailTabbedPane==null) {
            detailTabbedPane = new JTabbedPaneOperator(optionOperator,1);
        }
        return detailTabbedPane;
    }

    public JComboBoxOperator getExpandOnCombo() {
        if(expandOnCombo==null) {
            expandOnCombo = new JComboBoxOperator(optionOperator,1);
        }
        return expandOnCombo;
    }

    public JComboBoxOperator getLanguageCombo() {
        if(languageCombo==null) {
            languageCombo = new JComboBoxOperator(optionOperator,0);
        }
        return languageCombo;
    }

    public JButtonOperator getNewButton() {
        if(newButton==null) {
            newButton = new JButtonOperator(optionOperator,new ComponentChooser() {

                public boolean checkComponent(Component component) {
                    if(component instanceof JButton) {                        
                        return ((JButton)component).getText().equals("New");
                    } 
                    return false;
                }

                public String getDescription() {
                    return "";
                }
            });
        }
        return newButton;
    }
    
    public JButtonOperator getRemoveButton() {
        if(removeButton==null) {
            removeButton = new JButtonOperator(optionOperator,new ComponentChooser() {

                public boolean checkComponent(Component component) {                    
                    if(component instanceof JButton) {               
                        JButton b = (JButton) component;                        
                        if(b.getText().equals("Remove")) {
                            return b.isEnabled();
                        } else return false;                        
                    } 
                    return false;                    
                }

                public String getDescription() {
                    return "";
                }
            });
        }
        return removeButton;
    }

    public JTableOperator getTemplateTable() {
        if(templateTable==null) {
            templateTable = new JTableOperator(optionOperator);
        }
        return templateTable;
    }
                
    public void addAbbreviation(String abbreviation, String expansion, String description) {
        getNewButton().push();
        NbDialogOperator dialogOperator = new NbDialogOperator("New Code Template");
        JTextFieldOperator abbrev = new JTextFieldOperator(dialogOperator);
        abbrev.typeText(abbreviation);        
        JButtonOperator ok = new JButtonOperator(abbrev.getWindowContainerOperator());        
        ok.push();
        JTabbedPaneOperator detailPanel = getDetailTabbedPane();
        detailPanel.selectPage("Expanded Text");
        JEditorPaneOperator expaneded = new JEditorPaneOperator(detailPanel.getWindowContainerOperator());
        expaneded.typeText(expansion);
        if(description!=null) {
            detailPanel.selectPage("Description");
            JEditorPaneOperator descript = new JEditorPaneOperator(detailPanel);
            descript.typeText(description);
            detailPanel.selectPage("Expanded Text");
        }                        
    }

    public boolean editAbbreviation(String abbreviationName, String newExpansion, String newDesc) {
        int row = getTemplateTable().findCellRow(abbreviationName);
        if (row == (-1)) {
            return false;
        }
        getTemplateTable().selectCell(row, 0);        
        JTabbedPaneOperator detailPanel = getDetailTabbedPane();
        detailPanel.selectPage("Expanded Text");        
        JEditorPaneOperator expaneded = new JEditorPaneOperator(detailPanel.getWindowContainerOperator());
        expaneded.clearText();
        expaneded.typeText(newExpansion);        
        if (newDesc != null) {
            detailPanel.selectPage("Description");            
            JEditorPaneOperator descript = new JEditorPaneOperator(detailPanel);
            descript.clearText();
            descript.typeText(newDesc);            
            detailPanel.selectPage("Expanded Text");
        }        
        getTemplateTable().selectCell(row, 0);  //changing focus        
        return true;
    }

     public void addOrEditAbbreviation(String abbreviationName, String newAbbreviationName, String newExpansion, String newDesc) {
        if (!editAbbreviation(abbreviationName, newExpansion, newDesc)) {
            addAbbreviation(newAbbreviationName, newExpansion, newDesc);
        }
    }

    public void ok() {
        optionOperator.ok();
    }
    
    public boolean removeAbbreviation(String abbreviation) {
        int row = getTemplateTable().findCellRow(abbreviation,
                new Operator.DefaultStringComparator(true, true));        
        if (row == (-1)) {
            System.out.println("Didn't find "+abbreviation);
            TableModel model = getTemplateTable().getModel();
            int rowCount = model.getRowCount();
            for (int cntr = 0; cntr < rowCount; cntr++) {
                System.out.print(model.getValueAt(cntr, 0)+" ");
            }
            System.out.println("");
            return false;
        }
        getTemplateTable().selectCell(row, 0);                
        getRemoveButton().push();        
        return true;
    }
    
    public Map listAbbreviations() {
        TableModel model = getTemplateTable().getModel();
        int rowCount = model.getRowCount();
        Map result = new HashMap();
        
        for (int cntr = 0; cntr < rowCount; cntr++) {
            result.put((String) model.getValueAt(cntr, 0), (String) model.getValueAt(cntr, 1));
        }
        
        return result;
    }
    
    public static Abbreviations invoke(String language) {
        OptionsOperator options = OptionsOperator.invoke();
        options.selectEditor();
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(options);
        jtpo.selectPage("Code Templates");                
        new EventTool().waitNoEvent(500);                
        Abbreviations abbreviations = new Abbreviations(options);
        abbreviations.getLanguageCombo().selectItem(language);               
        return abbreviations;
    }
            
    public static void addOrEditAbbreviation(String language, String abbreviationName, String newAbbreviationName, String newExpansion, String newDesc) {
        Abbreviations instance = invoke(language);        
        instance.addOrEditAbbreviation(abbreviationName, newAbbreviationName, newExpansion, newDesc);
        instance.ok();
    }
    
    public static boolean removeAbbreviation(String language, String abbreviation) {
        Abbreviations instance = invoke(language);
        boolean       result   = instance.removeAbbreviation(abbreviation);        
        
        instance.ok();
        return result;
    }
    
    public static boolean editAbbreviation(String language, String abbreviationName, String newExpansion, String newDesc) {
        Abbreviations instance = invoke(language);
        boolean       result   = instance.editAbbreviation(abbreviationName, newExpansion, newDesc);
        instance.ok();
        new EventTool().waitNoEvent(2000);
        return result;
    }
    
    public static Map listAbbreviations(String language) {
        Abbreviations instance = invoke(language);
        Map           result   = instance.listAbbreviations();
        
        instance.ok();        
        return result;
    }

}

