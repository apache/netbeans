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

import org.netbeans.modules.parsing.spi.ParseException;

/**
 * @author mfukala@netbeans.org
 */
public class MediaQueriesModuleTest extends CssModuleTestBase {

    public MediaQueriesModuleTest(String testName) {
        super(testName);
    }
    
   public void testGetMediaFeatures() {
       assertTrue(MediaQueriesModule.getMediaFeatures().contains("color"));
       assertTrue(MediaQueriesModule.getMediaFeatures().contains("min-color"));
       assertTrue(MediaQueriesModule.getMediaFeatures().contains("max-color"));
       
       assertTrue(MediaQueriesModule.getMediaFeatures().contains("aspect-ratio"));
       assertTrue(MediaQueriesModule.getMediaFeatures().contains("max-aspect-ratio"));
       assertTrue(MediaQueriesModule.getMediaFeatures().contains("min-aspect-ratio"));
       
       assertTrue(MediaQueriesModule.getMediaFeatures().contains("scan"));
       assertFalse(MediaQueriesModule.getMediaFeatures().contains("min-scan"));
       assertFalse(MediaQueriesModule.getMediaFeatures().contains("max-scan"));
   }
   
   public void testMediaAtRuleCompletion() throws ParseException {
        checkCC("|", arr("@media"), Match.CONTAINS);
        checkCC("@med|", arr("@media"), Match.EXACT);
   }
   
   public void testMediaTypesCompletion() throws ParseException {
        checkCC("@media |", arr("all", "screen"), Match.CONTAINS);
        checkCC("@media sc|", arr("screen"), Match.EXACT);
        checkCC("@media screen|", arr("screen"), Match.EXACT);
        
        checkCC("@media screen,| ", arr("print"), Match.CONTAINS);
        checkCC("@media screen, | ", arr("print"), Match.CONTAINS);
        checkCC("@media screen,pri| ", arr("print"), Match.EXACT);
        checkCC("@media screen, pri| ", arr("print"), Match.EXACT);
       
        checkCC("@media screen and (color),| ", arr("print"), Match.CONTAINS);
        checkCC("@media screen and (color), | ", arr("print"), Match.CONTAINS);
        checkCC("@media screen and (color),pri| ", arr("print"), Match.EXACT);
        checkCC("@media screen and (color), pri| ", arr("print"), Match.EXACT);
       
   }
   
   public void testMediaFeaturesCompletion() throws ParseException {
        checkCC("@media (|", arr("device-width", "min-device-width"), Match.CONTAINS);
        checkCC("@media (dev|", arr("device-width"), Match.CONTAINS);
   }
   
}
