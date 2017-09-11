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

package org.netbeans.performance.platform;

import org.netbeans.performance.*;
import java.util.*;
import java.lang.reflect.Method;

/**
 * The benchmark comparing time to construct and run an anonymous Runnable
 * versus time to construct a single special Runable working over a Method.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class RunnableMethod extends Benchmark {

    public RunnableMethod(String name) {
        super( name, new Integer[] {
            new Integer(1), new Integer(100), new Integer( 10000 )
        });
    }
    
    /** Create new instance of unnamed Runnable implemented as independent
     * inner class for every round.
     */
    public void testUsingRealRunnable() {
        int count = getIterationCount();
        final int workload = ((Integer)getArgument()).intValue();
        
        while( count-- > 0 ) {
            new Runnable() {
                public void run() {
                    for( int i=0; i < workload; i++ );
                }
            }.run();
        }
    }
    
    
    /** Create new instance of unnamed Runnable implemented as a direct caller
     * to a worker method for every round.
     */
    public void testUsingDelegatingRunnable() {
        int count = getIterationCount();
        final int workload = ((Integer)getArgument()).intValue();
        
        while( count-- > 0 ) {
            new Runnable() {
                public void run() {
                    workerMethod( workload );
                }
            }.run();
        }
    }
    
    /** Create a new instance of MethodRunner - a class that will delegate
     * its task to passed Method - for every round. 
     */
    public void testUsingMethodRunner() throws Exception {
        int count = getIterationCount();
        int workload = ((Integer)getArgument()).intValue();
        
        while( count-- > 0 ) {
            Method mtd = getClass().getDeclaredMethod( "workerMethod", new Class[] { Integer.TYPE } );
            mtd.setAccessible(true);
            new MethodRunner( mtd, this, new Object[] { new Integer(workload) } ).run();
        }
    }

    /** Create a new instance of MethodRunner - a class that will delegate
     * its task to passed Method - for every round, but looking up the Method
     * only once.  
     */
    public void testUsingMethodRunnerWithCachedMethod() throws Exception {
        int count = getIterationCount();
        int workload = ((Integer)getArgument()).intValue();
        Method mtd = getClass().getDeclaredMethod( "workerMethod", new Class[] { Integer.TYPE } );
        mtd.setAccessible(true);
        
        while( count-- > 0 ) {
            new MethodRunner( mtd, this, new Object[] { new Integer(workload) } ).run();
        }
    }

    /** Create a new instance of MethodRunner - a class that will delegate
     * its task to passed Method - for every round, but looking up the Method
     * and creating argument parring array only once.  
     */
    public void testUsingMethodRunnerWithCachedParams() throws Exception {
        int count = getIterationCount();
        int workload = ((Integer)getArgument()).intValue();
        Method mtd = getClass().getDeclaredMethod( "workerMethod", new Class[] { Integer.TYPE } );
        mtd.setAccessible(true);
        Object[] params = new Object[] { new Integer(workload) };
        
        while( count-- > 0 ) {
            new MethodRunner( mtd, this, params ).run();
        }
    }

    /** Create just one instance of MethodRunner - a class that will delegate
     * its task to passed Method - and call this single instance for every
     * round.  
     */
    public void testUsingCachedMethodRunner() throws Exception {
        int count = getIterationCount();
        int workload = ((Integer)getArgument()).intValue();
        Method mtd = getClass().getDeclaredMethod( "workerMethod", new Class[] { Integer.TYPE } );
        mtd.setAccessible(true);
        Object[] params = new Object[] { new Integer(workload) };
        MethodRunner mr = new MethodRunner( mtd, this, params );

        while( count-- > 0 ) {
            mr.run();
        }
    }

    public static void main( String[] args ) {
	simpleRun( RunnableMethod.class );
    }    
    
    /* ----------------------------- */
    private void workerMethod( int workload ) {
        for( int i=0; i < workload; i++ );
    }

    private static final class MethodRunner implements Runnable {
        private Method mtd;
        private Object obj;
        private Object[] args;
        
        public MethodRunner( Method mtd, Object obj, Object[] args ) {
            this.mtd = mtd;
            this.obj = obj;
            this.args = args;
        }
        
        public void run() {
            try {
                mtd.invoke( obj, args );
            } catch( Throwable t ) {
                t.printStackTrace();
                junit.framework.Assert.fail( "Exception: " + t.getMessage() );
            }
        }
    }
}
