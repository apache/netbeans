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

package org.openide.util;

import org.openide.utildata.UtilClass;
import org.netbeans.performance.Benchmark;
import java.util.ResourceBundle;

public class NbBundleTest extends Benchmark {

    public NbBundleTest(String name) {
        super( name, new Integer[] {
            new Integer(1), new Integer(10), new Integer(100), new Integer(1000)
        });
    }

    private String[] keys;

    protected void setUp() {
        int count = getIterationCount();
        int param = ((Integer)getArgument()).intValue();
        keys = new String[param];
        for( int i=0; i<param; i++ ) {
            keys[i] = "MSG_BundleTest_" + i;
        }
    }
    
    protected void tearDown() {
        keys=null;
    }
        
    public void testGetMessageUsingClass() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();

        while( count-- > 0 ) {
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                NbBundle.getMessage( UtilClass.class, keys[number] );
            }
        }
    }    

    public void testGetMessageUsingClassFullBrand() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
	NbBundle.setBranding("brand1");

        while( count-- > 0 ) {
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                NbBundle.getMessage( UtilClass.class, keys[number] );
            }
        }
	NbBundle.setBranding(null);
    }    

    public void testGetMessageUsingEmptyBrand() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
	NbBundle.setBranding("brand2");

        while( count-- > 0 ) {
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                NbBundle.getMessage( UtilClass.class, keys[number] );
            }
        }
	
	NbBundle.setBranding(null);
    }    

    private ResourceBundle bundle;
    private synchronized ResourceBundle getBundle() {
        if( bundle == null ) {
            bundle = NbBundle.getBundle( UtilClass.class );
        }
        return bundle;
    }
    
    private synchronized void clearBundle() {
        bundle = null;
    }
    
    public void testGetMessageUsingLazyCache() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();

        while( count-- > 0 ) {
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                getBundle().getString( keys[number] );
            }
            clearBundle();
        }
    }    

    public void testGetMessageUsingCachedBundle() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();

        while( count-- > 0 ) {
            ResourceBundle bundle = NbBundle.getBundle( UtilClass.class );
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                bundle.getString( keys[number] );
            }
        }
    }

    public void testGetMessageUsingCachedBundleFullBrand() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
	NbBundle.setBranding("brand1");

        while( count-- > 0 ) {
            ResourceBundle bundle = NbBundle.getBundle( UtilClass.class );
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                bundle.getString( keys[number] );
            }
        }
	NbBundle.setBranding(null);
    }


    public void testGetMessageUsingCachedBundleEmptyBrand() throws Exception {
        int count = getIterationCount();
        int magnitude = ((Integer)getArgument()).intValue();
	NbBundle.setBranding("brand2");

        while( count-- > 0 ) {
            ResourceBundle bundle = NbBundle.getBundle( UtilClass.class );
            // do the stuff here, 
            for( int number = 0; number < magnitude; number++ ) {
                bundle.getString( keys[number] );
            }
        }
	NbBundle.setBranding(null);
    }
    
    public static void main(String[] args) {
	simpleRun( NbBundleTest.class );
    }
}
