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
 * Benchmark measuring how long it takes to set the name of the Thread.
 * Yarda's concenr was to not slow down the request processor by
 * pooling threads and changing the thread's name frequently.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class SetThreadName extends Benchmark {

    public SetThreadName(String name) {
        super( name );
	t.start();
	t2.start();
    }

    protected int getMaxIterationCount() {
	return Integer.MAX_VALUE;
    }

    Thread t = new Thread() {
	public void run() {
	    try {
		Thread.sleep(1000000000);
	    } catch (InterruptedException e) {
	    }
	}
    };

    static class Thread2 extends Thread {
	int prio;
	
	public void run() {
	    try {
		Thread.sleep(1000000000);
	    } catch (InterruptedException e) {
	    }
	}
	
	public void setPrio(int p) {
	    if (prio == p) return;
	    setPriority(p);
	    prio = p;
	}
    }

    Thread2 t2 = new Thread2();
    

    /**
     */
    public void testSetThreadName() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    t.setName("Thread #" + count);
        }
    }

    /**
     */
    public void testSetFixedName() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    t.setName("Thread #A");
	    t.setName("Thread #B");
        }
    }

    /**
     */
    public void testCreateName() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    String s = "Thread #" + count;
        }
    }

    /**
     */
    public void testSetPriority() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    t.setPriority(4+(count%3));
        }
    }

    /**
     */
    public void testSetFixedPriority() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    t.setPriority(5);
        }
    }

    /**
     */
    public void testSetSimilarPriority() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    t2.setPrio(4+((count/100)%3));
        }
    }

    
    public static void main( String[] args ) {
	simpleRun( SetThreadName.class );
    }

}
