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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.modules.css.test.operator.StyleBuilderOperator;
import static org.netbeans.modules.css.test.operator.StyleBuilderOperator.Panes.*;
import org.netbeans.modules.css.test.operator.StyleBuilderOperator.FontPaneOperator;

/**
 *
 * @author Jindrich Sedek
 */
public class TestIssues extends CSSTest {
    
    /** Creates new CSS Test */
    public TestIssues(String testName) {
        super(testName);
    }
    
    public void test105562(){/*move end bracket, 105574 end semicolon should be added*/
        String insertion = "h2{font-size: 10px   }\n";
        EditorOperator eop = openFile(newFileName);
        eop.setCaretPositionToLine(1);
        eop.insert(insertion);
        eop.setCaretPositionToLine(1);
        waitUpdate();
        StyleBuilderOperator styleOper= new StyleBuilderOperator("h2").invokeBuilder();
        FontPaneOperator fontPane = (FontPaneOperator) styleOper.setPane(FONT);
        JListOperator fontFamilies = fontPane.fontFamilies();
        fontFamilies.selectItem(3);
        waitUpdate();
        String selected = fontFamilies.getSelectedValue().toString();
        String text = eop.getText();
        assertFalse("END BRACKET IS MOVED",text.contains(insertion));
        String rule = text.substring(0, text.indexOf('}'));
        assertTrue("SEMICOLON ADDED", rule.contains("font-size: 10px;"));
        assertTrue("FONT FAMILY SOULD BE GENERATED INSIDE RULE",rule.contains("font-family: "+selected));
    }     
    
    public void test105568(){
        String insertion = "h1{\ntext-decoration   : overline;\n}";
        EditorOperator eop = openFile(newFileName);
        eop.setCaretPositionToLine(1);
        eop.insert(insertion);
        eop.setCaretPositionToLine(1);
        StyleBuilderOperator styleOper= new StyleBuilderOperator("h1");
        waitUpdate();
        FontPaneOperator fontPane = (FontPaneOperator) styleOper.setPane(FONT);
        assertTrue(fontPane.isOverline());
    }
    
}
