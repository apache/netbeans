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
package org.netbeans.modules.css.editor.module.main;

import org.netbeans.modules.css.lib.CssTestBase;
import org.netbeans.modules.css.lib.api.properties.GrammarResolver;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author mfukala@netbeans.org
 */
public class ColorsModuleTest extends CssTestBase {

    public ColorsModuleTest(String name) {
        super(name);
    }

    public void testPropertyDescriptors() throws ParseException {
        PropertyDefinition color = Properties.getPropertyDefinition( "color");
        assertNotNull(color);

        assertNotNull(Properties.getPropertyDefinition( "@rgb"));
        assertNotNull(Properties.getPropertyDefinition( "@colors-list"));
        assertNotNull(Properties.getPropertyDefinition( "@system-color"));
    }

    public void testTextValues() {
        PropertyDefinition p = Properties.getPropertyDefinition( "color");
        assertTrue(new ResolvedProperty(p, "red").isResolved());
        assertTrue(new ResolvedProperty(p, "buttonface").isResolved());
    }

    public void testRGBValues() {
        PropertyDefinition p = Properties.getPropertyDefinition( "color");
        assertTrue(new ResolvedProperty(p, "rgb(10,20,30)").isResolved());
        assertTrue(new ResolvedProperty(p, "rgb(10%,20,30)").isResolved());
        assertFalse(new ResolvedProperty(p, "rgb(,20,30)").isResolved());
        assertFalse(new ResolvedProperty(p, "rgb(10,x,30)").isResolved());

    }

    public void testHashValues() {
        PropertyDefinition p = Properties.getPropertyDefinition( "color");
        assertTrue(new ResolvedProperty(p, "#ffaa00").isResolved());
        assertTrue(new ResolvedProperty(p, "#fb0").isResolved());
        assertTrue(new ResolvedProperty(p, "#ffaa0077").isResolved());
        assertTrue(new ResolvedProperty(p, "#fa07").isResolved());
        assertFalse(new ResolvedProperty(p, "#fx0").isResolved());
        assertFalse(new ResolvedProperty(p, "#fa001").isResolved());
        assertFalse(new ResolvedProperty(p, "#dummy1").isResolved());
        assertFalse(new ResolvedProperty(p, "#dummy123").isResolved());
        assertFalse(new ResolvedProperty(p, "#dffaa007712").isResolved());
    }

    public void testRGBaValues() {
        PropertyDefinition p = Properties.getPropertyDefinition( "color");
        assertTrue(new ResolvedProperty(p, "rgba(255,0,0,1)").isResolved());
        assertTrue(new ResolvedProperty(p, "rgba(100%,0%,0%,1)").isResolved());
        assertTrue(new ResolvedProperty(p, "rgba(0,0,255,0.5)").isResolved());
    }
    

    public void testHSLValues() {
        PropertyDefinition p = Properties.getPropertyDefinition( "color");
        assertTrue(new ResolvedProperty(p, "hsl(0, 100%, 50%)").isResolved());
        assertTrue(new ResolvedProperty(p, "hsl(120, 100%, 50%)").isResolved());
        assertTrue(new ResolvedProperty(p, "hsl(120, 100%, 25%)").isResolved());
        assertTrue(new ResolvedProperty(p, "hsl(120, 100%, 75%)").isResolved());
        assertTrue(new ResolvedProperty(p, "hsl(120, 75%, 75%)").isResolved());
        assertTrue(new ResolvedProperty(p, "hsl(120, 100%, 50%)").isResolved());
    }
    
    public void testHSLaValues() {
        PropertyDefinition p = Properties.getPropertyDefinition( "color");
        assertTrue(new ResolvedProperty(p, "hsla(120, 100%, 50%, 1)").isResolved());
        assertTrue(new ResolvedProperty(p, "hsla(240, 100%, 50%, 0.5)").isResolved());
        assertTrue(new ResolvedProperty(p, "hsla(30, 100%, 50%, 0.1)").isResolved());
    }
    
    public void testSpecialValues() {
        PropertyDefinition p = Properties.getPropertyDefinition( "color");
//        assertTrue(new PropertyValue(p, "inherit").success());
        assertTrue(new ResolvedProperty(p, "currentColor").isResolved());
        assertTrue(new ResolvedProperty(p, "transparent").isResolved());
    }
    
    public void testInheritInColor() throws ParseException {
        PropertyDefinition p = Properties.getPropertyDefinition( "color");
        PRINT_INFO_IN_ASSERT_RESOLVE = false;
        GrammarResolver.setLogging(GrammarResolver.Log.DEFAULT, false);
        assertResolve(p.getGrammarElement(null), "inherit");
        
//        assertCssCode("div { color: inherit }");
    }
    
    
}
