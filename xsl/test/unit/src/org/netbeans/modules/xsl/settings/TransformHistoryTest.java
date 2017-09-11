/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
