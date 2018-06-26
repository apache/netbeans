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

package org.netbeans.modules.j2ee.sun.validation.util;

import java.io.File;

import junit.framework.*;

import org.netbeans.modules.j2ee.sun.validation.Constants;
import org.netbeans.modules.j2ee.sun.validation.util.Utils;


/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class BundleReaderTest extends TestCase{
    /* A class implementation comment can go here. */

    public BundleReaderTest(String name){
        super(name);
    }


    public static void main(String args[]){
        junit.textui.TestRunner.run(suite());
    }


    public void testGetValue() {
        String str = BundleReader.getValue("non_existing_key");         //NOI18N
        assertTrue(str.equals("non_existing_key"));                     //NOI18N
        str = BundleReader.getValue("MSG_NumberConstraint_Failure");    //NOI18N
        assertTrue(!str.equals("MSG_NumberConstraint_Failure"));        //NOI18N
    }


    public void testCreate() {
        String bundleFile = "org/netbeans/modules/" +                   //NOI18N
            "j2ee/sun/validation/Bundle.properties";                    //NOI18N
        Utils utils = new Utils();
        boolean fileExists = utils.fileExists(bundleFile);
        String str = 
            BundleReader.getValue("MSG_NumberConstraint_Failure");      //NOI18N
        
        if(fileExists){
            assertTrue(!str.equals("MSG_NumberConstraint_Failure"));    //NOI18N
        } else {
            assertTrue(str.equals("MSG_NumberConstraint_Failure"));     //NOI18N
        }
    }


    /**
     * Define suite of all the Tests to run.
     */
    public static Test suite(){
        TestSuite suite = new TestSuite(BundleReaderTest.class);
        return suite;
    }


    /**
     * Initialize; allocate any resources needed to perform Tests.
     */
    protected void setUp() {
    }


    /**
     * Free all the resources initilized/allocated to perform Tests.
     */
    protected void tearDown() {
    }


    private void nyi() {
        ///fail("Not yet implemented");                                 //NOI18N
    }
}
