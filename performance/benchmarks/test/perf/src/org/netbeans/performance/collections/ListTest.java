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

package org.netbeans.performance.collections;

import org.netbeans.performance.MultiInstanceIntArgBenchmark;
import java.util.*;

public abstract class ListTest extends MultiInstanceIntArgBenchmark {

    /** Creates new FSTest */
    public ListTest(String name) {
        super( name, new Integer[] { i(1), i(5), i(10), i(100), i(1000) });
    }

    protected int getMaxIterationCount() {
        /* 50MBs / size of a filled list */
        int param = ((Integer)getArgument()).intValue();
        int itemSize = 20;
        int size = param*itemSize + 200;
        return 50000000/size;
    }

    protected final Object createInstance() {
	// create list instance
	List inst = createList( getIntArg() );
	
	//fill it with data for ToArray test
	if( "testToArray".equals( getName() ) ) {
	    int size = getIntArg();
            while( size-- > 0 ) inst.add( null );
	}
	
	return inst;
    }
    
    protected abstract List createList( int size );
    
    public void testAppend() throws Exception {
        int count = getIterationCount();
        int magnitude = getIntArg();

        while( count-- > 0 ) {
            // do the stuff here, 
            List act = (List)instances[count];
            for( int number = 0; number < magnitude; number++ ) {
                act.add( null );
            }
        }
    }    

    public void testToArray() throws Exception {
        int count = getIterationCount();
        int magnitude = getIntArg();

        while( count-- > 0 ) {
            // do the stuff here, 
            List act = (List)instances[count];
	    Object[] arr = act.toArray();
        }
    }    

}
