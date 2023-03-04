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
 * DTDParserTest.java
 * NetBeans JUnit based test
 *
 * Created on April 10, 2002, 9:06 AM
 */

package org.netbeans.modules.xml.wizard;

import org.netbeans.modules.xml.wizard.impl.DTDParser;
import java.io.*;
import java.net.URL;
import java.util.*;
import junit.framework.*;
import org.netbeans.junit.*;
import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;
import org.openide.xml.*;
import org.openide.util.Lookup;
import org.netbeans.modules.xml.tree.ModuleEntityResolver;

/**
 * Must be executed in IDE with mounted NetBEans XML Catalog.
 *
 * @author Petr Kuzel
 */
public class DTDParserTest extends NbTestCase {
    
    public DTDParserTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(DTDParserTest.class);
        
        return suite;
    }
    
    /** Test of parse method, of class org.netbeans.modules.xml.wizard.DTDParser. */
    public void testParse() {        
        URL dtd = getClass().getResource("data/dtd.dtd");
        InputSource in = new InputSource(dtd.toExternalForm());
        Set roots = new DTDParser().parse(in);        
        assertTrue("Expected decl not found!", roots.contains("root"));
    }    
    
}
