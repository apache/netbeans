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

package org.netbeans.modules.xml.dtd.grammar;

import java.io.*;
import java.net.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;
import org.openide.xml.*;
import junit.framework.*;

/**
 * It tests if internal and external DTD is properly parsed.
 *
 * Warning: this test has knowledge for following resource files:
 * email.xml and email.dtd.
 *
 * @author Petr Kuzel
 */
public class DTDParserTest extends TestCase {
    
    public DTDParserTest(java.lang.String testName) {
        super(testName);
    }
    
    public void testParse() {        
//        try {
//            DTDParser parser = new DTDParser();
//            InputSource in = new InputSource();
//            URL url = getClass().getResource("email.xml");
//            in.setSystemId(url.toExternalForm());
//            in.setByteStream(url.openConnection().getInputStream());
//            DTDGrammar dtd = parser.parse(in);    
//            
//            assertTrue("Missing entity!", dtd.entities.contains("testExternalEntity"));
//            assertTrue("Missing notation!", dtd.notations.contains("testNotation"));
//            assertTrue("Missing element!", dtd.elementDecls.keySet().contains("testANYElement"));
//            assertTrue("Missing attribute!", dtd.attrDecls.keySet().contains("subject"));
//            
//            // ANY elements must contain all declared
//            Set all = (Set) dtd.elementDecls.get("testANYElement");
//            assertTrue("ANY must contain all declared!", all.containsAll(dtd.elementDecls.keySet()));
//
//            // EMPTY must be empty
//            assertTrue("EMPTY must be empty!", ((Set)dtd.elementDecls.get("attachment")).isEmpty());
//            
//            // #PCDATA mus be empty
//            assertTrue("#PCDATA must be empty!", ((Set)dtd.elementDecls.get("name")).isEmpty());
//
//        } catch (Exception ex) {
//            // Add your test code below by replacing the default call to fail.
//            ex.printStackTrace();
//            fail(ex.toString());
//        }                
    }
    
}
