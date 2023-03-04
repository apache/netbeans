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
package org.netbeans.modules.css.lib.api.properties;

import org.netbeans.modules.css.lib.CssTestBase;
import org.netbeans.modules.css.lib.properties.GrammarParseTreeBuilder;

/**
 *
 * @author marekfukala
 */
public class ResolvedPropertyTest extends CssTestBase {

    public ResolvedPropertyTest(String testName) {
        super(testName);
    }

    public void testBorderColor() {
        PropertyDefinition pm = Properties.getPropertyDefinition( "border-color");
        assertNotNull(pm);

        ResolvedProperty rp = new ResolvedProperty(pm, "red green #ffaabb");

        assertTrue(rp.isResolved());

//        dumpTree(rp.getParseTree());
        
    }
    
    public void testBorder() {
        PropertyDefinition pm = Properties.getPropertyDefinition( "border");
        assertNotNull(pm);

        GrammarParseTreeBuilder.DEBUG = true;
        ResolvedProperty rp = new ResolvedProperty(pm, "red solid");

        assertTrue(rp.isResolved());

//        dumpTree(rp.getParseTree());
        
    }
    
    public void testBackgroundPosition() {
        PropertyDefinition pm = Properties.getPropertyDefinition( "background-position");
        assertNotNull(pm);

        ResolvedProperty rp = new ResolvedProperty(pm, "left      top");

        assertTrue(rp.isResolved());

//        dumpTree(rp.getParseTree());
        
    }
    
    public void testNegativeZIndex() {
        PropertyDefinition pm = Properties.getPropertyDefinition( "z-index");
        assertNotNull(pm);

        ResolvedProperty rp = new ResolvedProperty(pm, "-1 ");
//        dumpTree(rp.getParseTree());
//        for(Token t : rp.getTokens()) {
//            System.out.println(t);
//        }

        assertTrue(rp.isResolved());
    }
    
    public void testNegativeLeftValue() {
        PropertyDefinition pm = Properties.getPropertyDefinition( "left");
        assertNotNull(pm);

        ResolvedProperty rp = new ResolvedProperty(pm, "-10% ");
//        dumpTree(rp.getParseTree());
//        for(Token t : rp.getTokens()) {
//            System.out.println(t);
//        }

        assertTrue(rp.isResolved());
    }
    
    
}
