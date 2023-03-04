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

