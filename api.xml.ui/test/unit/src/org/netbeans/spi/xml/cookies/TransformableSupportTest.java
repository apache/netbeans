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
            if (msg.getLevel() >= msg.WARNING_LEVEL) {
                warnings++;
            }
        }
        public int getWarnings() {
            return warnings;
        }
        
    }
        
}
