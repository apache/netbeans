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

/**
 *
 * @author mfukala@netbeans.org
 */
public class TextModuleTest extends CssModuleTestBase {

    public TextModuleTest(String testName) {
        super(testName);
    }
    
    public void testProperties() {
        assertPropertyValues("hanging-punctuation", "first force-end");
//        assertPropertyValues("hyphenate-limit-chars", "auto", "1", "1 2", "1 2 3");
        assertPropertyValues("text-align", "start", "center", "justify-all");
    }
    
//    public void testTextOverflow() {
//        assertPropertyValues("text-overflow-ellipsis", "\"one\"");
//        assertPropertyValues("text-overflow-ellipsis", "\"one\" \"two\"");
//        assertPropertyValues("text-overflow-ellipsis", "url(http://sg.sg)");
//        
//        assertPropertyValues("text-overflow-mode", "clip");
//        
//        assertPropertyValues("text-overflow", "clip");
//        assertPropertyValues("text-overflow", "ellipsis");
//        assertPropertyValues("text-overflow", "ellipsis-word");
//        
//        assertPropertyValues("text-overflow", "\"one\" url(htpp://sg.sg)");
//     
//    }
    
    public void testWord_OverflowWrap() {
        assertPropertyValues("word-wrap", "normal");
        assertPropertyValues("word-wrap", "break-word");
        assertPropertyValues("word-wrap", "inherit");
        
        assertPropertyValues("overflow-wrap", "normal");
        assertPropertyValues("overflow-wrap", "break-word");
        assertPropertyValues("overflow-wrap", "inherit");
        
   }
    
}
