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

package org.netbeans.junit;

import junit.extensions.TestDecorator;
import java.io.*;
import org.netbeans.junit.diff.*;

/**
 * A Decorator for Tests. Use TestDecorator as the base class
 * for defining new test decorators. Test decorator subclasses
 * can be introduced to add behaviour before or after a test
 * is run.
 *
 */

import junit.framework.*;

/** NetBeans extension to JUnit's TestDecorator class. Tests created
 * with the help of this class can use method assertFile and can be
 * filtered.
 */
public class NbTestDecorator extends TestDecorator implements NbTest {
	
/**
 */
	public NbTestDecorator(Test test) {
		super(test);
	}

        /**
         * Sets active filter.
         * @param filter Filter to be set as active for current test, null will reset filtering.
         */
        public void setFilter(Filter filter) {
            //System.out.println("NbTestDecorator.setFilter()");
            if (fTest instanceof NbTest) {
                //System.out.println("NbTestDecorator.setFilter(): test="+fTest+" filter="+filter);
                ((NbTest)fTest).setFilter(filter);
            }
        }
        
        /**
         * Returns expected fail message.
         * @return expected fail message if it's expected this test fail, null otherwise.
         */
        public String getExpectedFail() {
            if (fTest instanceof NbTest) {
                return ((NbTest)fTest).getExpectedFail();
            } else {
                return null;
            }
        }
        
        /**
         * Checks if a test isn't filtered out by the active filter.
         */
        public boolean canRun() {
            //System.out.println("NbTestDecorator.canRun()");
            if (fTest instanceof NbTest) {
                //System.out.println("fTest is NbTest");
                return ((NbTest)fTest).canRun();
            } else {
                // standard JUnit tests can always run
                return true;
            }
        }        

        
        
        // additional asserts !!!!
        // these methods only wraps functionality from asserts from NbTestCase,
        // because java does not have multiinheritance :-)
        // please see more documentatino in this file.
        
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(String message, String test, String pass, String diff, Diff externalDiff) {
            NbTestCase.assertFile(message,test,pass,diff,externalDiff);
        }
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(String test, String pass, String diff, Diff externalDiff) {
            NbTestCase.assertFile(test, pass, diff, externalDiff);
        }
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(String message, String test, String pass, String diff) {
            NbTestCase.assertFile(message, test, pass, diff);
        }
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(String test, String pass, String diff) {
            NbTestCase.assertFile(test, pass, diff);
        }
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(String test, String pass) {
            NbTestCase.assertFile(test, pass);
        }
        
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(String message, File test, File pass, File diff, Diff externalDiff) {
            NbTestCase.assertFile(message,test,pass,diff,externalDiff);
        }
        
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(File test, File pass, File diff, Diff externalDiff) {
            NbTestCase.assertFile(test, pass, diff, externalDiff);
        }
        
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(String message, File test, File pass, File diff) {
            NbTestCase.assertFile(message, test, pass, diff);
        }
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(File test, File pass, File diff) {
            NbTestCase.assertFile(test,pass,diff);
        }
        
/** for description, see this method in NbTestCase class
 */
        static public void assertFile(File test, File pass) {
            NbTestCase.assertFile(test, pass);
        }
        
        
}
