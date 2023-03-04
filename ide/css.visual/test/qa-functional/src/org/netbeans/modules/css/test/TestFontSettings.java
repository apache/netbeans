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
package org.netbeans.modules.css.test;

import java.util.List;
import java.util.Random;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.modules.css.test.operator.StyleBuilderOperator;
import org.netbeans.modules.css.test.operator.StyleBuilderOperator.FontPaneOperator;
import static org.netbeans.modules.css.test.operator.StyleBuilderOperator.Panes.FONT;

/**
 *
 * @author Jindrich Sedek
 */
public class TestFontSettings extends CSSTest{
    
    public TestFontSettings(String name) {
        super(name);
    }
    
    public void testSetFontFamily() throws Exception{
        openFile(newFileName);
        FontPaneOperator fontOper = initializeFontChanging();
        int familiesCount = getSize(fontOper.fontFamilies());
        StyleBuilderOperator.EditFontOperator fontOperator = fontOper.getEditFont();
        int startSize = getSize(fontOperator.currentFontFamilies());
        fontOperator.createNewFamily();
        int afterSize = getSize(fontOperator.currentFontFamilies());
        assertEquals("FONTS COUNT", startSize + 1, afterSize);
        fontOperator.deleteFamily();
        afterSize = getSize(fontOperator.currentFontFamilies());
        assertEquals("FONTS COUNT", startSize, afterSize);
        fontOperator.createNewFamily();
        afterSize = getSize(fontOperator.currentFontFamilies());
        assertEquals("FONTS COUNT", startSize + 1, afterSize);
        //-------ADD FONT------------//
        fontOperator.selectAvailable(0);
        addItem(fontOperator, 1);
        //-------ADD FAMILY----------//
        fontOperator.selectAvailable(1);
        addItem(fontOperator, 2);
        //-------ADD WEBFONT---------//
        fontOperator.selectAvailable(2);
        addItem(fontOperator, 3);
        //-------CHANGE ORDER--------//
        List<String> selectedItems = getItems(fontOperator.selected());
        fontOperator.selected().selectItem(0);
        fontOperator.down();
        fontOperator.selected().selectItem(2);
        fontOperator.up();
        List<String> afterChanges = getItems(fontOperator.selected());
        assertEquals("CHANGE OF ORDER", selectedItems.get(0), afterChanges.get(2));
        assertEquals("CHANGE OF ORDER", selectedItems.get(1), afterChanges.get(0));
        assertEquals("CHANGE OF ORDER", selectedItems.get(2), afterChanges.get(1));
        fontOperator.close();
        new EditorOperator(newFileName).setCaretPositionToLine(rootRuleLineNumber);
        assertEquals("ADDED FAMILY", familiesCount+1, getSize(fontOper.fontFamilies()));
        String selectedItem = fontOper.fontFamilies().getSelectedValue().toString();
        assertTrue("SELECTED", selectedItem.contains(afterChanges.get(0)));
        assertTrue("SELECTED", selectedItem.contains(afterChanges.get(1)));
        String rule = getRootRuleText();
        assertTrue("GENERATED", rule.contains(afterChanges.get(0)));
        assertTrue("GENERATED", rule.contains("font-family:"));
    }
    
    public void testChangeFontFamily(){
        openFile(newFileName);
        FontPaneOperator fontOper = initializeFontChanging();
        JListOperator fontFamilies = fontOper.fontFamilies();
        int familiesCount = getSize(fontFamilies);
        fontFamilies.selectItem(new Random().nextInt(familiesCount-1)+1);// IGNORE <NOT SET>
        waitUpdate();
        String selected = fontFamilies.getSelectedValue().toString();
        assertTrue("CHANGED FONT", getRootRuleText().contains(selected));
        assertTrue("CHANGED FONT", getRootRuleText().contains("font-family:"));
        fontFamilies.selectItem(0);// <NOT SET>
        assertFalse("CHANGED FONT", getRootRuleText().contains("font-family:"));
    }
    
    public void testChangeFontSize(){
        openFile(newFileName);
        FontPaneOperator fontOper = initializeFontChanging();
        JListOperator fontSizes = fontOper.fontSizes();
        fontSizes.selectItem("12");
        assertTrue(getRootRuleText().contains("font-size: "));
        assertTrue(getRootRuleText().contains("12"));
        JComboBoxOperator fontUnits = fontOper.fontSizeUnits();
        //        assertTrue(fontUnits.isEnabled());
        fontUnits.selectItem("mm");
        assertTrue(getRootRuleText(), getRootRuleText().contains("font-size: 12mm"));
        fontSizes.selectItem("large");
        fontUnits = fontOper.fontSizeUnits();
        //        assertFalse(fontUnits.isEnabled());
        assertTrue(getRootRuleText().contains("font-size: large"));
        fontSizes.selectItem(0);// <NOT SET>
        assertFalse(getRootRuleText().contains("font-size"));
    }
    
    public void testChangeFontWeight(){
        FontPaneOperator fontOper = initializeFontChanging();
        checkAtrribute("font-weight", fontOper.fontWeight());
    }
    
    public void testChangeFontStyle(){
        FontPaneOperator fontOper = initializeFontChanging();
        checkAtrribute("font-style", fontOper.fontStyle());
    }
    
    public void testChangeFontVariant(){
        FontPaneOperator fontOper = initializeFontChanging();
        checkAtrribute("font-variant", fontOper.fontVariant());
    }
    
    public void testChangeFontColor(){
        FontPaneOperator fontOper = initializeFontChanging();
        JComboBoxOperator operator = fontOper.fontColor();
        int size = getSize(operator);
        assertFalse("SOME ITEMS", size == 0);
        operator.selectItem("green");
        waitUpdate();
        assertTrue(getRootRuleText().contains("color: green"));
        //        JColorChooserOperator chooser = fontOper.showColorChooser();
        //        chooser.setColor(Color.RED);
        //        waitUpdate();
        //        assertTrue(getRootRuleText().contains("color: red"));
    }
    
    public void testDecoration(){
        FontPaneOperator fontOper = initializeFontChanging();
        fontOper.overline(true);
        waitUpdate();
        assertTrue(getRootRuleText().contains("text-decoration: overline"));
        fontOper.overline(false);
        waitUpdate();
        fontOper.underline(true);
        waitUpdate();
        assertTrue(getRootRuleText().contains("text-decoration: underline"));
        fontOper.underline(false);
        waitUpdate();
        fontOper.strikethrough(true);
        waitUpdate();
        assertTrue(getRootRuleText().contains("text-decoration: line-through"));
        fontOper.noDecoration(true);
        waitUpdate();
        assertTrue(getRootRuleText().contains("text-decoration: none"));
        assertFalse(getRootRuleText().contains("line-through"));
        assertFalse(getRootRuleText().contains("overline"));
    }
           
    private FontPaneOperator initializeFontChanging(){
        return initializeFontChanging("root");
    }

    private FontPaneOperator initializeFontChanging(String ruleName){
        EditorOperator eop = openFile(newFileName);
        eop.setCaretPositionToLine(rootRuleLineNumber);
        StyleBuilderOperator styleOper= new StyleBuilderOperator(ruleName).invokeBuilder();
        return (FontPaneOperator) styleOper.setPane(FONT);
    }
    
    private void addItem(StyleBuilderOperator.EditFontOperator fontOperator, int order){
        JListOperator fonts = fontOperator.fonts();
        int selected = new Random().nextInt(getSize(fonts)-1)+1;//IGNORE <NOT SET>
        fonts.selectItem(selected);
        String selectedItem = fonts.getSelectedValue().toString();
        fontOperator.add();
        assertEquals("ITEMS ADDED", order, getSize(fontOperator.selected()));
        assertEquals("ADDED ITEM", selectedItem, getItems(fontOperator.selected()).get(order-1));
    }
        
}
