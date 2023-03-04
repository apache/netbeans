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
import org.netbeans.modules.css.test.operator.StyleBuilderOperator;
import static org.netbeans.modules.css.test.operator.StyleBuilderOperator.Panes.*;
import org.netbeans.modules.css.test.operator.StyleBuilderOperator.BackgroundPaneOperator;

/**
 *
 * @author Jindrich Sedek
 */
public class TestBackgroundSettings extends CSSTest {
    
    /** Creates new CSS Test */
    public TestBackgroundSettings(String testName) {
        super(testName);
    }
    
    public void testBGColor(){
//        BackgroundPaneOperator backgroundPane = initializeBackgroundChanging();
//        ColorSelectionField colorPanel = backgroundPane.getColor();
//        //--------INSERT ONCE--------//
//        colorPanel.setColorString("red");
//        waitUpdate();
//        assertTrue("INSERTING", getRootRuleText().contains("background-color: red"));
//        //--------  UPDATE   --------//
//        colorPanel.setColorString("green");
//        waitUpdate();
//        assertTrue("UPDATING", getRootRuleText().contains("background-color: green"));
//        //-------- REMOVE -----------//
//        colorPanel.setColorString("");//<NOT SET>
//        waitUpdate();
//        assertFalse("REMOVING", getRootRuleText().contains("background-color"));
    }
    
    public void testTile(){
        BackgroundPaneOperator paneOperator = initializeBackgroundChanging();
        checkAtrribute("background-repeat", paneOperator.getTile());
    }

    public void testScroll(){
        BackgroundPaneOperator paneOperator = initializeBackgroundChanging();
        checkAtrribute("background-attachment", paneOperator.getScroll());
    }

    public void testHPosition(){
        BackgroundPaneOperator paneOperator = initializeBackgroundChanging();
        checkAtrribute("background-position", paneOperator.getHPosition(), true);
    }

    public void testVPosition(){
    }

    private BackgroundPaneOperator initializeBackgroundChanging(){
        EditorOperator eop = openFile(newFileName);
        eop.setVisible(true);
        eop.setCaretPositionToLine(rootRuleLineNumber);
        StyleBuilderOperator styleOper= new StyleBuilderOperator("root").invokeBuilder();
        return (BackgroundPaneOperator) styleOper.setPane(BACKGROUND);
    }
    
}
