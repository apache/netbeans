/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.spi.xml.cookies;

import java.io.*;
import java.net.*;

import junit.framework.*;
import org.netbeans.junit.*;

import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import org.netbeans.api.xml.cookies.*;
import org.netbeans.spi.xml.cookies.*;

/**
 *
 * @author Libor Kramolis
 */
public class TransformableSupportTest extends NbTestCase {

    public TransformableSupportTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(TransformableSupportTest.class);
        return suite;
    }
    
    
    public void testTransform () {
        assertTrue ("Correct XML and correct XSLT must pass!",               transform ("data/doc.xml", "data/doc2xhtml.xsl"));
        assertTrue ("Incorrect XML and correct XSLT must not pass!",  false==transform ("data/InvalidDocument.xml", "data/doc2xhtml.xsl"));
        assertTrue ("Correct XML and incorrect XSLT must not pass!",  false==transform ("data/doc.xml", "data/InvalidDocument.xml"));
        assertTrue ("Incrrect XML and incorrect XSLT must not pass!", false==transform ("data/InvalidDocument.xml", "data/InvalidDocument.xml"));
    }
    
    private boolean transform (String xml, String xslt) {
        URL xmlURL = getClass().getResource(xml);
        URL xsltURL = getClass().getResource(xslt);
        Source xmlSource = new SAXSource (new InputSource (xmlURL.toExternalForm()));
        Source xsltSource = new SAXSource (new InputSource (xsltURL.toExternalForm()));
        Result outputResult = new StreamResult (new StringWriter());
        
        TransformableSupport support = new TransformableSupport (xmlSource);
        Observer observer = new Observer(); // not yet used
        boolean exceptionThrown = false;
        try {
            support.transform (xsltSource, outputResult, null);
        } catch (TransformerException exc) {
            System.err.println("!!! " + exc);
            exceptionThrown = true;
        }
        
        System.out.println(xml + " & " + xslt + " => " + ( exceptionThrown ? "WRONG" : "OK" ));
        return exceptionThrown==false;
    }
    

    //
    // class Observer
    //
    
    private static class Observer implements CookieObserver {
        private int receives;
        private int warnings;
        
        public void receive(CookieMessage msg) {
            receives++;
            if (msg.getLevel() >= CookieMessage.WARNING_LEVEL) {
                warnings++;
            }
        }
        public int getWarnings() {
            return warnings;
        }
        
    }
        
}
