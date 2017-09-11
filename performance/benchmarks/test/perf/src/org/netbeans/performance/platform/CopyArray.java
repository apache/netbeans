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
 * Benchmark measuring how efficiently is the System.arraycopy implemented,
 * how big overhead does it have on small arrays.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class CopyArray extends Benchmark {

    public CopyArray(String name) {
        super( name, new Integer[] {
            new Integer(1), new Integer(5), new Integer(10),
            new Integer(100), new Integer(1000)
        });
    }

    private Object[] src, dest;
    private byte[] src_b, dest_b;

    private String[] dest_s;
    private Object[] src_s;

    protected void setUp() {
        int magnitude = ((Integer)getArgument()).intValue();
	src = new Object[magnitude];
	dest = new Object[magnitude];
	src_b = new byte[magnitude];
	dest_b = new byte[magnitude];

	src_s = new Object[magnitude];
	dest_s = new String[magnitude];
    }

    protected int getMaxIterationCount() {
	return Integer.MAX_VALUE;
    }


    /**
     */
    public void testCopyNativeObjects() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();

        while( count-- > 0 ) {
	    System.arraycopy(src, 0, dest, 0, magnitude);
        }
    }

    /**
     */
    public void testCopyNativeMixed() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();

        while( count-- > 0 ) {
	    System.arraycopy(src_s, 0, dest_s, 0, magnitude);
        }
    }
    
    /**
     */
    public void testCopyNativeBytes() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();

        while( count-- > 0 ) {
	    System.arraycopy(src_b, 0, dest_b, 0, magnitude);
        }
    }


    /**
     */
    public void testCopyLoopObjects() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
    
        while( count-- > 0 ) {
	    for( int i=0; i<magnitude; i++ ) dest[i] = src[i];
        }
    }

    /**
     */
    public void testCopyLoopBytes() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
    
        while( count-- > 0 ) {
	    for( int i=0; i<magnitude; i++ ) dest_b[i] = src_b[i];
        }
    }

    /**
     */
    public void testCopyLoopMixed() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
    
        while( count-- > 0 ) {
	    for( int i=0; i<magnitude; i++ ) dest_s[i] = (String) src_s[i];
        }
    }

    public static void main( String[] args ) {
	simpleRun( CopyArray.class );
    }

}
