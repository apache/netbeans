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
package org.netbeans.modules.xsl.settings;

import java.util.*;
import java.io.*;
import java.rmi.MarshalledObject;

import junit.framework.*;
import org.netbeans.junit.*;

import org.openide.filesystems.FileObject;

import org.netbeans.modules.xsl.utils.TransformUtil;

/**
 *
 * @author Libor Kramolis
 */
public class TransformHistoryTest extends NbTestCase {

    public TransformHistoryTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(TransformHistoryTest.class);
        
        return suite;
    }
    
    
    public void testIt () {
        System.out.println("testIt");
        
        TransformHistory history = new TransformHistory();
        
        // current max number of items in history is 5!
        assertTrue ("[1/5][1/5] OK!", checkHistory (history, 1, 1, "in.xml", "trans.xsl", "out.put"));
        assertTrue ("[1/5][1/5] OK!", checkHistory (history, 1, 1, "in.xml", null, null));
        assertTrue ("[2/5][1/5] OK!", checkHistory (history, 2, 1, "in2.xml", null, "out2.put"));
        assertTrue ("[3/5][1/5] OK!", checkHistory (history, 3, 1, "in3.xml", null, "out3.put"));
        assertTrue ("[4/5][1/5] OK!", checkHistory (history, 4, 1, "in4.xml", null, "out4.put"));
        assertTrue ("[5/5][1/5] OK!", checkHistory (history, 5, 1, "in5.xml", null, "out5.put"));
        assertTrue ("[6/5][1/5] OK!", checkHistory (history, 5, 1, "in6.xml", null, "out6.put"));
        assertTrue ("Output for in6.xml is out6.put!", "out6.put".equals (history.getXMLOutput("in6.xml")));
        assertTrue ("Output for in.xml is null!", (history.getXMLOutput("in.xml") == null));
    }
    
    
    private boolean checkHistory (TransformHistory history, int xi, int ti, String xml, String xsl, String output) {
        // modify history
        history.setOverwriteOutput (!history.isOverwriteOutput()); // negate
        history.setProcessOutput ((history.getProcessOutput()+1)%3); // rotate
        if ( xml != null ) {
            history.addXML (xml, output);
        }
        if ( xsl != null ) {
            history.addXSL (xsl, output);
        }

        // test number of XMLs
        if ( history.getXMLs().length != xi ) {
            System.out.println("    history.getXMLs().length: " + history.getXMLs().length);
            return false;
        }
        // test number of XSLs
        if ( history.getXSLs().length != ti ) {
            System.out.println("    history.getXSLs().length: " + history.getXSLs().length);
            return false;
        }
        
        // (de)marshal
        TransformHistory newHistory = null;
        try {
            MarshalledObject marshalled = new MarshalledObject (history);
            newHistory = (TransformHistory) marshalled.get();
        } catch (Exception exc) {
            System.err.println("!!! " + exc);
            return false;
        }
        
        // test if equals
        return (history.equals (newHistory));
    }
    
}
