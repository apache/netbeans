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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.AssertionFailedError;

/** Checks that we can do proper logging.
 *
 * @author Jaroslav Tulach
 */
public class AssertInstancesTest extends NbTestCase {
    private static Object hold;
    
    private Logger LOG;
    
    public AssertInstancesTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("test." + getName());
    }

    public void testCannotGC() throws Throwable {
        String s = new String("Ahoj");
        hold = s;
        
        Log.enableInstances(LOG, null, Level.FINEST);
        LOG.log(Level.FINE, "Text", s);
        
        try {
            Log.assertInstances("Cannot GC");
        } catch (AssertionFailedError ex) {
            // ok
            return;
        }
        
        fail("The unreleased reference shall be spotted");
    }

    public void testCannotGCWithAWrongAndRightName() throws Throwable {
        String s = new String("Ahoj");
        hold = s;

        Log.enableInstances(LOG, "NoText", Level.FINEST);
        Log.enableInstances(LOG, "3rdText", Level.FINEST);


        LOG.log(Level.FINE, "3rdText", s);
        LOG.log(Level.FINE, "NoText", new String("OK"));

        Log.assertInstances("OK, GC as nothing holds OK", "NoText");
        try {
            Log.assertInstances("Cannot GC", "3rdText");
        } catch (AssertionFailedError ex) {
            // ok
            return;
        }
        fail("The unreleased reference shall be spotted");
    }
    
    public void testCannotGCWithNameOfMessages() throws Throwable {
        String s = new String("Ahoj");
        hold = s;
        
        Log.enableInstances(LOG, "Text", Level.FINEST);
        LOG.log(Level.FINE, "Text", s);
        
        try {
            Log.assertInstances("Cannot GC");
        } catch (AssertionFailedError ex) {
            // ok
            return;
        }
        
        fail("The unreleased reference shall be spotted");
    }
    
    public void testCannotGCButOKAsNotTrackedMessage() throws Throwable {
        String s = new String("Ahoj");
        hold = s;
        
        Log.enableInstances(LOG, "StrangeMessages", Level.FINEST);
        LOG.log(Level.FINE, "Text", s);
        LOG.log(Level.FINE, "StrangeMessages", new String("some not hold instance"));
        
        Log.assertInstances("Can GC because the object is not tracked");
    }
    
    
    public void testCanGC() throws Throwable {
        String s = new String("Ahoj");
        
        Log.enableInstances(LOG, null, Level.FINEST);
        LOG.log(Level.FINE, "Text", s);
        
        s = null;
        
        Log.assertInstances("Can GC without problems");
    }

    public void testFailIfNoMessage() throws Throwable {
        String s = new String("Ahoj");
        hold = s;
        
        Log.enableInstances(LOG, "StrangeMessages", Level.FINEST);
        
        try {
            Log.assertInstances("Fails as there is no message");
        } catch (AssertionFailedError ex) {
            return;
        }
        
        fail("Shall fails as there is no logged object");
    }
    
}
