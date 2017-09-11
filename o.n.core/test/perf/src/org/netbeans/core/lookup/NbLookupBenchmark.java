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


package org.netbeans.core.lookup;


import java.util.Collection;

import junit.framework.TestSuite;

import org.netbeans.performance.Benchmark;

import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


public class NbLookupBenchmark extends Benchmark {
    /** how many times objects in INSTANCES should be added in */
    private static Object[] ARGS = {
        new Integer (1)
    };


    public NbLookupBenchmark(java.lang.String testName) {
        super(testName, ARGS);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new TestSuite (NbLookupBenchmark.class));
    }
    
    /** Lookup which simulates instance lookup. */
    private AbstractLookup lookup;

    /** instances that we register */
    private static Object[] INSTANCES = new Object[] {
        new Integer (10), 
        new Object (),
        "Ahoj",
        new C4 (), new C3 (), new C2 (), new C1 ()
    };

    /** Fills the lookup with instances */
    protected void setUp () {
        Integer integer = (Integer)getArgument ();
        int cnt = integer.intValue ();
        
        boolean reverse = cnt < 0;
        if (reverse) cnt = -cnt;
        
        InstanceContent iContent = new InstanceContent();
        
        lookup = new AbstractLookup(iContent);
        
        while (cnt-- > 0) {
            for (int i = 0; i < INSTANCES.length; i++) {
                if (reverse) {
                    iContent.add (INSTANCES[INSTANCES.length - i - 1]);
                } else {
                    iContent.add (INSTANCES[i]);
                }
            }
        }
    }
    
    /** Clears the lookup.
     */
    protected void tearDown () {
        lookup = null;
    }
    
    /** Test to find the first registered object.
     */
    public void testInteger () {
        enum (Integer.class);
    }
    
    /** Test object.
     */
    public void testObject () {
        enum (Object.class);
    }
    
    /** Test string.
     */
    public void testString () {
        enum (String.class);
    }
    
    public void testC1 () {
        enum (C1.class);
    }
    
    public void testC2 () {
        enum (C2.class);
    }
    
    public void testC3 () {
        enum (C3.class);
    }
    
    public void testC4 () {
        enum (C4.class);
    }
    
    public void testI1 () {
        enum (I1.class);
    }
    
    public void testI2 () {
        enum (I2.class);
    }
    
    public void testI3 () {
        enum (I3.class);
    }
    
    public void testI4 () {
        enum (I4.class);
    }
        
        
        
    /** Enumerates over instances of given class.
     * @param clazz the class to find instances of
     */
    private void enum (Class clazz) {
        int cnt = getIterationCount ();
        
        while (cnt-- > 0) {
            Lookup.Result res = lookup.lookup (new Lookup.Template (clazz));

            Collection c = res.allInstances ();
        }
    }
    
    
    private static interface I1 {}
    private static interface I2 extends I1 {}
    private static interface I3 extends I1 {}
    private static interface I4 extends I2, I3 {}
    private static class C1 extends Object implements I2 {}
    private static class C2 extends C1 {}
    private static class C3 extends C2 implements I3 {}
    private static class C4 extends C3 implements I4 {}
}
