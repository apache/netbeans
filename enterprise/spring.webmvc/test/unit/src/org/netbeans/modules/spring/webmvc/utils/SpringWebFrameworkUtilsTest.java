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

package org.netbeans.modules.spring.webmvc.utils;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author John Baker
 */
public class SpringWebFrameworkUtilsTest extends NbTestCase {
    
    public SpringWebFrameworkUtilsTest(String testName) {
        super(testName);
    }         
    
    public void testDispatcherName_ValidPattern() throws Exception {
        assertTrue(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("Dispatcher"));
    }  
    
    public void testDispatcherName_ValidAmpersandPattern() throws Exception {
        assertTrue(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("Dispatcher&amp;"));
    } 
         
    public void testDispatcherName_NonWordUnicodeCharacterPattern() throws Exception {       
        assertFalse(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("あｂ３＿:え"));  // ^ is the invalid character
    }        
    
    public void testDispatcherName_ValidUnicodePattern() throws Exception {
        assertTrue(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("あおうえｂ３＿え"));
    }  
    
    public void testDispatcherName_InvalidPattern_LEFT_ANGLE_BRACKET() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("<Dispatcher"));
    }  
    
    public void testDispatcherName_InvalidPattern_RIGHT_ANGLE_BRACKET() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("Dispatcher>"));
    }  
    
    public void testDispatcherName_InvalidPattern_ASTERISK() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("Dispat*cher"));
    }  
    
    public void testDispatcherName_InvalidPattern_BACKSLASH() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("Dispatcher\\"));
    }  
    
    public void testDispatcherName_InvalidPattern_COLON() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("Dispatcher:"));
    }  
    
    public void testDispatcherName_InvalidPattern_DOUBLE_QUOTE() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("Dispatcher\""));
    }  
    
    public void testDispatcherName_InvalidPattern_FORWARD_SLASH() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("/Dispatcher"));
    }  
    
    public void testDispatcherName_InvalidPattern_PERCENT_SIGN() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("Dispa%tcher"));
    }  
    
    public void testDispatcherName_InvalidPattern_PIPE_CHAR() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("|Dispatcher"));
    }  
    
    public void testDispatcherName_ValidPattern_QUESTION_MARK() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid("Dispatc?her"));
    }  
        
    public void testDispatcherMapping_ExtensionSpacePattern() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("*.h tm"));
    }
    
    public void testDispatcherMapping_ExtensionNonWordPattern() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("*.h&tm"));
    }
    
    public void testDispatcherMapping_ServletSpacePattern() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("/a /*"));
    }
    
    public void testDispatcherMapping_PathSpacePattern() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherMappingPatternValid(" /"));
    }
    
    public void testDispatcherMapping_InvalidExtensionPattern() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherMappingPatternValid( "*."));
    }       
     
    public void testDispatcherMapping_InvalidPathPattern() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("/a"));
    }
    
    public void testDispatcherMapping_ValidPathPattern() throws Exception {
        assertTrue(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("/"));
    }
    
    public void testDispatcherMapping_InvalidDefaultServletPattern() throws Exception {
        assertFalse(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("/a*/"));
    }
    
    public void testDispatcherMapping_ValidDefaultServletPattern() throws Exception {
        assertTrue(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("/app/*"));
    }
        
    public void testDispatcherMapping_ValidPattern() throws Exception {
        assertTrue(SpringWebFrameworkUtils.isDispatcherMappingPatternValid("*.htm"));
    }

    public void testInstantiateDispatcherMapping() {
        assertEquals("index.html", SpringWebFrameworkUtils.instantiateDispatcherMapping("*.html", "index"));
        assertEquals("app/index", SpringWebFrameworkUtils.instantiateDispatcherMapping("/app/*", "index"));
        assertEquals("index", SpringWebFrameworkUtils.instantiateDispatcherMapping("/", "index"));
    }

    public void testGetSimpleDispatcherURL() {
        assertEquals("index.html", SpringWebFrameworkUtils.getSimpleDispatcherURL("index.html"));
        assertEquals("index", SpringWebFrameworkUtils.getSimpleDispatcherURL("app/index"));
        assertEquals("", SpringWebFrameworkUtils.getSimpleDispatcherURL("/"));
        assertEquals("", SpringWebFrameworkUtils.getSimpleDispatcherURL(""));
    }
}
