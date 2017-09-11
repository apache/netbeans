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
