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
package org.netbeans.modules.openide.nodes;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jan Horvath <jhorvath@netbeans.org>
 */
public class PERegistrationSupportTest extends NbTestCase {
    
    public PERegistrationSupportTest(String name) {
        super(name);
    }
    
    public void doTest(String type) throws ClassNotFoundException {
        Class<?> cls = NodesRegistrationSupport.getClassFromCanonicalName(type);
        assertEquals(type, cls.getCanonicalName());
    }
    
    public void testPrimitives() throws ClassNotFoundException {
        String[] classNames = {"int", "boolean", "float", "short", "char"};
        for (int i = 0; i < classNames.length; i++) {
            doTest(classNames[i]);
        }
    }
    
    public void testArrays() throws ClassNotFoundException {
        String[] classNames = {"int[][][]", "boolean[]", "java.lang.String[]"};
        for (int i = 0; i < classNames.length; i++) {
            doTest(classNames[i]);
        }
    }
    
    public void testTypes() throws ClassNotFoundException {
        String[] classNames = {"java.lang.String", "java.lang.Integer"};
        for (int i = 0; i < classNames.length; i++) {
            doTest(classNames[i]);
        }
    }
    
    
}
