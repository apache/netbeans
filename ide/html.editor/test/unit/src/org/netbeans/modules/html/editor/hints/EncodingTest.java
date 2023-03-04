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
package org.netbeans.modules.html.editor.hints;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;

/**
 *
 * @author marekfukala
 */
public class EncodingTest extends NbTestCase {
    
    public EncodingTest(String name) {
        super(name);
    }
    
    public void testPatterns() {
        String str = "Warning: Using \"windows-1252\" instead of the declared encoding \"iso-8859-1\".\n\n";
//        for(int i = 0; i < str.length(); i++) {
//            System.out.println(str.charAt(i) + " -> " + Integer.toHexString(str.charAt(i)));
//        }
        Error e = new DefaultError(null, null, str, null, -1, -1, Severity.WARNING);
        Encoding rule = new Encoding();
        
        assertTrue(rule.appliesTo(null, e));
    }
}
