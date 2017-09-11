/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
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

package org.netbeans.projectopener;

import junit.framework.TestCase;
import junit.framework.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Milan Kubec
 */
public class ArgsHandlerTest extends TestCase {
    
    public ArgsHandlerTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of getArgValue method, of class org.netbeans.projectopener.ArgsHandler.
     */
    public void testGetArgValue() {
        
        System.out.println("getArgValue");
        
        String prjURLString = "http://www.someurl.com/TheProject.zip";
        String nbVersionString = "1.2.3";
        String mainPrjNameString = "TheMainProject";
        String args[] = new String[] { "-projecturl", prjURLString, 
                                       "-minversion", nbVersionString, 
                                       "-mainproject", mainPrjNameString, 
                                       "-showgui", "-otherarg" };
        List<String> list = new ArrayList<String>();
        list.add("showgui");
        list.add("otherarg");
        
        ArgsHandler handler = new ArgsHandler(args);
        assertEquals(prjURLString, handler.getArgValue("projecturl"));
        assertEquals(nbVersionString, handler.getArgValue("minversion"));
        assertEquals(mainPrjNameString, handler.getArgValue("mainproject"));
        assertEquals(list, handler.getAdditionalArgs());
        
        String args2[] = new String[] { "-projecturl", 
                                        "-minversion", nbVersionString, 
                                        "-showgui" };
        list.clear();
        list.add("showgui");
        
        handler = new ArgsHandler(args2);
        assertEquals(null, handler.getArgValue("projecturl"));
        assertEquals(nbVersionString, handler.getArgValue("minversion"));
        assertEquals(null, handler.getArgValue("mainproject"));
        assertEquals(list, handler.getAdditionalArgs());
        
    }

    /**
     * Test of getAdditionalArgs method, of class org.netbeans.projectopener.ArgsHandler.
     */
    // public void testGetAdditionalArgs() { }
    
}
