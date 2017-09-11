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

package org.netbeans.core.startup.preferences;

/**
 *
 * @author Radek Matous
 */
public final class Statistics {
    /** Creates a new instance of Statistics */
    
    public static final Statistics.TimeConsumer FLUSH =
            new Statistics.TimeConsumer("Flush");//NOI18N
    public static final Statistics.TimeConsumer LOAD =
            new Statistics.TimeConsumer("Load");//NOI18N
    public static final Statistics.TimeConsumer REMOVE_NODE =
            new Statistics.TimeConsumer("Remove node");//NOI18N
    public static final Statistics.TimeConsumer CHILDREN_NAMES =
            new Statistics.TimeConsumer("Children names");//NOI18N
    
    
    private Statistics() {}
    
    
    public static Statistics.StopWatch getStopWatch(Statistics.TimeConsumer consumer) {
        return new Statistics.StopWatch(consumer);
    }

    public static Statistics.StopWatch getStopWatch(Statistics.TimeConsumer consumer, boolean start) {
        Statistics.StopWatch retval = new Statistics.StopWatch(consumer);
        if (start) {
            retval.start();
        }
        return retval;
    }
    
    public static final class TimeConsumer {
        private int elapsedTime;
        private int numberOfCalls;
        private final String description;
        
        private TimeConsumer(final String description) {
            this.description = description;
        }
        
        public int getConsumedTime() {
            return elapsedTime;
        }
        
        public int getNumberOfCalls() {
            return numberOfCalls;
        }        
        
        public void reset() {
    	    elapsedTime = 0;
            numberOfCalls = 0;
        }

        public @Override String toString() {
            return description + ": " + numberOfCalls + " calls in " + elapsedTime + "ms";//NOI18N
        }
		
        private void incrementNumerOfCalls() {
            numberOfCalls++;
            
        }
    }
    
    public static final class StopWatch {
        private long startTime = 0;
        private final Statistics.TimeConsumer activity;
        
        
        /** Creates a new instance of ElapsedTime */
        private StopWatch(Statistics.TimeConsumer activity) {
            this.activity = activity;
        }
        
        
        public void start() {
            startTime = System.currentTimeMillis();
        }
        
        public void stop() {
            assert startTime != 0;
            activity.elapsedTime += (System.currentTimeMillis() - startTime);
            activity.incrementNumerOfCalls();
            startTime = 0;
        }
    }
}

