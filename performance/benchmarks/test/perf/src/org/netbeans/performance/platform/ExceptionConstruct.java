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

import org.netbeans.performance.Benchmark;

/**
 * The Benchmark measuring how long would it take to construct an instance
 * of Exception. Measured bacause the Exception contains native-filled
 * structure describing the shape of the thread stack in the moment
 * of constructing, which depends on stack depth in the time of constructing.
 * Uses a set of Integer arguments to select the call stack depth.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class ExceptionConstruct extends Benchmark {

    public ExceptionConstruct(String name) {
        super( name, new Integer[] {
            new Integer(1), new Integer(5), new Integer(10),
            new Integer(100), new Integer(1000 )
        });
    }

    private static final Object createObj( int depth ) {
	if( depth == 0 ) return new Object();
	return createObj( depth-1 );
    }

    /**
     * Pour into the call stack and then create an object.
     * Used as a reference to divide the time between recursive decline
     * and Exception creation.
     */
    public void testCreateObjectDeepInStack() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
    
        while( count-- > 0 ) {
            createObj( magnitude );
        }
    }

    private static final Object createExc( int depth ) {
	if( depth == 0 ) return new Exception();
	return createExc( depth-1 );
    }

    
    /**
     * Create an Exception deep in the call stack, filling its stack trace.
     */
    public void testCreateExceptionDeepInStack() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
    
        while( count-- > 0 ) {
            createExc( magnitude );
        }
    }

    private static final Object throwExc( int depth ) throws Exception {
	if( depth == 0 ) throw new Exception();
	return createExc( depth-1 );
    }

    
    /**
     * Create an Exception deep in the call stack and let it bubble up
     * throughout the whole stack.
     */
    public void testThrowExceptionDeepInStack() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
    
        while( count-- > 0 ) {
            try {
                createExc( magnitude );
            } catch( Exception e ) {}
        }
    }

    
    public static void main( String[] args ) {
	simpleRun( ExceptionConstruct.class );
    }

}
