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
package org.netbeans.modules.javafx2.editor.css;

import org.netbeans.modules.css.lib.CssTestBase;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;

/**
 *
 * @author marekfukala, petrsomol
 */
public class JavaFXCSSModuleTest extends CssTestBase {

    public JavaFXCSSModuleTest(String testName) {
        super(testName);
    }

    public void testFxBackgroundColorRadialGradient() {
        PropertyDefinition pd = Properties.getPropertyDefinition("-fx-background-color");
        assertNotNull(pd);
        
        assertResolve(pd, "radial-gradient(center 50% 50%, radius 80%, derive(-fx-bubble-fill,20%), derive(-fx-bubble-fill,-30%))");
    }

    public void testFxBackgroundColorLadder() {
        PropertyDefinition pd = Properties.getPropertyDefinition("-fx-background-color");
        assertNotNull(pd);
        
        assertResolve(pd, "ladder("
            + "-fx-background,"
            + "-fx-light-text-color 45%,"
            + "-fx-dark-text-color  46%,"
            + "-fx-dark-text-color  59%,"
            + "-fx-mid-text-color   60%)");
    }

    public void testFxBackgroundInsets() {
        PropertyDefinition pd = Properties.getPropertyDefinition("-fx-background-insets");
        assertNotNull(pd);
        
        assertResolve(pd, "0 0 -1 0, 0, 1, 2");
        assertResolve(pd, "-1.4, 0, 1, 2");
        assertResolve(pd, "0, 0 0 1 0, 1 1 2 1, 2 2 3 2, 3 3 4 3");
    }
    
    public void testFxBorderColor() {
        PropertyDefinition pd = Properties.getPropertyDefinition("-fx-border-color");
        assertNotNull(pd);
        
        assertResolve(pd, "derive(-fx-base, 80%) "
                + "linear-gradient(to bottom, derive(-fx-base,80%) 20%, derive(-fx-base,-10%) 90%)"
                + "derive(-fx-base, 10%) "
                + "linear-gradient(to bottom, derive(-fx-base,80%) 20%, derive(-fx-base,-10%) 90%),        "
                + "transparent -fx-table-header-border-color -fx-table-header-border-color -fx-table-header-border-color");
    }
    
    public void testFxBorderColorHash() {
        PropertyDefinition pd = Properties.getPropertyDefinition("-fx-border-color");
        assertNotNull(pd);
        
        assertResolve(pd, "#f9d900");
        assertResolve(pd, "#f9d900aa");
    }
    
    public void testFxBorderStyleSegments() {
        PropertyDefinition pd = Properties.getPropertyDefinition("-fx-border-style");
        assertNotNull(pd);
        
        assertResolve(pd, "segments(0.166667em, 0.166667em)");
    }

    public void testFxEffectDropshadow() {
        PropertyDefinition pd = Properties.getPropertyDefinition("-fx-effect");
        assertNotNull(pd);
        
        assertResolve(pd, "dropshadow( three-pass-box , rgba(0,0,0,0.6) , 8, 0.0 , 0 , 0 )");
    }

    public void testFxEffectInnershadow() {
        PropertyDefinition pd = Properties.getPropertyDefinition("-fx-effect");
        assertNotNull(pd);
        
        assertResolve(pd, "innershadow( two-pass-box , rgba(0,0,0,0.6) , 4, 0.0 , 0 , 1 )");
    }

    public void testFxFont() {
        PropertyDefinition pd = Properties.getPropertyDefinition("-fx-font");
        assertNotNull(pd);
        
        assertResolve(pd, "bold 36px \"Amble\"");
    }
    
    public void testFxPadding() {
        PropertyDefinition pd = Properties.getPropertyDefinition("-fx-padding");
        assertNotNull(pd);
        
        assertResolve(pd, "0.166667em 0.833333em 0.25em 0.833333em");
    }

    public void testFxShape() {
        PropertyDefinition pd = Properties.getPropertyDefinition("-fx-shape");
        assertNotNull(pd);
        
        assertResolve(pd, "\"M0,0H9L0,9Z\"");
    }

    public void testLinearGradient() {
        PropertyDefinition pd = Properties.getPropertyDefinition("@fx-linear-gradient");
        assertNotNull(pd);
        
        assertResolve(pd, "linear-gradient(red, blue)");
        assertResolve(pd, "linear-gradient(red, blue, green)");
        
        assertResolve(pd, "linear-gradient(repeat, red, blue)");
        assertResolve(pd, "linear-gradient(reflect, red, blue)");
        
        assertResolve(pd, "linear-gradient(to left, red, blue)");
        assertResolve(pd, "linear-gradient(to top, red, blue)");
        assertResolve(pd, "linear-gradient(to left top, red, blue)");
        assertResolve(pd, "linear-gradient(to right bottom, red, blue)");
        
        assertResolve(pd, "linear-gradient(from 1% 2% to 3% 4%, red, blue)");
        assertResolve(pd, "linear-gradient(from 1cm 2cm to 3cm 4cm, red, blue)");
        assertResolve(pd, "linear-gradient(from 1% 2% to 3cm 4cm, red, blue)");
        
        assertResolve(pd, "linear-gradient( from 0% 0% to 50% 50%, #fcf7b6, #a59c31)");
    
        assertResolve(pd, "linear-gradient(to right, derive(-fx-base,-30%), derive(-fx-base,-60%))");
        assertResolve(pd, "linear-gradient(to right, derive(-fx-base,65%) 2%, derive(-fx-base,-20%) 95%)");
        assertResolve(pd, "linear-gradient(to right, rgba(0,0,0,0.3) 0%, rgba(0,0,0,0) 10%, rgba(0,0,0,0) 90%, rgba(0,0,0,0.3) 100%)");        
    }

}
