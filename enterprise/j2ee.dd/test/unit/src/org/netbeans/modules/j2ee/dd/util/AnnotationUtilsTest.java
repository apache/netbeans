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

package org.netbeans.modules.j2ee.dd.util;

import org.netbeans.junit.NbTestCase;

/**
 * Test for {@link AnnotationUtils}.
 * @author Tomas Mysik
 */
public class AnnotationUtilsTest extends NbTestCase {
    
    public AnnotationUtilsTest(String name) {
        super(name);
    }
    
    public void testSetterNameToPropertyName() throws Exception {
        // correct setters
        assertEquals("property name should be ok", "time", AnnotationUtils.setterNameToPropertyName("setTime"));
        assertEquals("property name should be ok", "longTermPlan", AnnotationUtils.setterNameToPropertyName("setLongTermPlan"));
        assertEquals("property name should be ok", "a", AnnotationUtils.setterNameToPropertyName("setA"));
        
        // incorrect setters
        assertNull("property name should be null", AnnotationUtils.setterNameToPropertyName("isTest"));
        assertNull("property name should be null", AnnotationUtils.setterNameToPropertyName("testMe"));
        assertNull("property name should be null", AnnotationUtils.setterNameToPropertyName("getShortTermPlan"));
        assertNull("property name should be null", AnnotationUtils.setterNameToPropertyName("is"));
        assertNull("property name should be null", AnnotationUtils.setterNameToPropertyName("se"));
        assertNull("property name should be null", AnnotationUtils.setterNameToPropertyName("get"));
        assertNull("property name should be null", AnnotationUtils.setterNameToPropertyName("set"));
    }
}
