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
/*
 * SchemaParserTest.java
 * NetBeans JUnit based test
 *
 * Created on July 24, 2002, 11:44 PM
 */

package org.netbeans.modules.xml.wizard;

import org.netbeans.modules.xml.wizard.SchemaParser;
import java.net.URL;
import junit.framework.*;
import org.netbeans.junit.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 *
 * @author Petr Kuzel <petr.kuzel@sun.com>
 */
public class SchemaParserTest extends NbTestCase {
    
    public SchemaParserTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(SchemaParserTest.class);
        
        return suite;
    }
    
    /** Test of parse method, of class org.netbeans.modules.xml.wizard.SchemaParser. */
    public void testParse() {
        System.out.println("testParse");
        
        URL ns = getClass().getResource("data/schemaWithNS.xsd");
        SchemaParser parser = new SchemaParser();

        SchemaParser.SchemaInfo info = parser.parse(ns.toExternalForm());        
        assertTrue("root expected",info.roots.contains("root"));
        assertTrue("ns expected", "test:schemaWithNS".equals(info.namespace));
        assertTrue("unexpected root", info.roots.contains("seq1") == false);
    }
            
}
