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

package org.netbeans.performance.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** Wrapper for inforamtion about one performance test case.
 *
 * @author  Radim Kubacki
 */
public class TestResult implements Comparable {

    public static final int ORDER_FIRST = 1;
    public static final int ORDER_NEXT  = 2;

    /** updates mean and variances. */
    public static Statistics computeStatistics (Collection<Integer> values) {
        Statistics s = new Statistics();
        
        s.n = values.size();
        s.avg = s.stddev = s.var = s.sumSquares = 0;
        for (int val : values) {
            s.avg += val;
            s.sumSquares += val*val;
        }
        
//    ep = 0.0;
//    for (i = 2; i <= n; i++) {
//      s = ARGV[i] - mean;
//      ep += s;
//      variance = variance + s * s;
//    }
//    variance = (variance - ep*ep/n)/(n - 1);
//    stdev = sqrt(variance);
//    printf("stdev=%f\n", stdev);
//    printf("var=%f\n", variance);
//
        if (s.n > 0) {
            s.avg = s.avg / s.n;
        }
        if (s.n > 1) {
            double ep = 0d;
            for (int v: values) {
                ep += v - s.avg;
                s.var += (v - s.avg)*(v - s.avg);
            }
            s.var = (s.var - ep*ep/s.n)/(s.n-1);
            s.stddev = Math.sqrt(s.var);

        }
        return s;
    }
    
    
    /** Name of test case. */
    String name;
    /** Expected limit for result values. */
    int threshold;
    /** Measurement unit. */
    String unit;
    /** Order of test case in measured suite. */
    int order;
    
    private String suite;
    
    /** Creates a new instance of TestCaseResults */
    public TestResult(String name, int threshold, String unit, int order, String suite) {
        if (name == null || unit == null)
            throw new IllegalArgumentException();
        
        this.name = name;
        this.unit = unit;
        this.threshold = threshold;
        this.order = order;
        this.suite = (suite != null)? suite: "";
    }
    
    public static class Statistics {
        /** computed average */
        private double avg;
        
        /** computed standard deviation */
        private double stddev;
        
        /** computed variance */
        private double var;

        private double sumSquares;

        /** count of values */
        private int n;
        
        public double getAverage () {
            return avg;
        }
        
        public double getStdDev () {
            return stddev;
        }
        
        public double getVariance () {
            return var;
        }
        
        public int getCount () {
            return n;
        }
        
//        public double getSumSquares () {
//            return sumSquares;
//        }
    }
    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public java.lang.String getName() {
        return name;
    }
    
    /**
     * Getter for property threshold.
     * @return Value of property threshold.
     */
    public int getThreshold() {
        return threshold;
    }
    
    /**
     * Getter for property unit.
     * @return Value of property unit.
     */
    public java.lang.String getUnit() {
        return unit;
    }
    
    public int hashCode() {
        return name.hashCode() | unit.hashCode() | order | threshold;
    }    
    
    public boolean equals(Object obj) {
        if (!(obj instanceof TestResult)) 
            return false;
        
        TestResult o = (TestResult)obj;
        return suite.equals(o.suite)
          && name.equals(o.name)
          && threshold == o.threshold
          && unit.equals(o.unit)
          && order == o.order;
    }
    
    /**
     * Getter for property order.
     * @return Value of property order.
     */
    public int getOrder() {
        return order;
    }
    
    public int compareTo(Object o) {
        TestResult t = (TestResult)o;
        if (suite.equals(t.suite)) {
            if (name.equals(t.name)) {
                if (order == t.order) {
                    if (unit.equals(t.unit)) {
                        return threshold - t.threshold;
                    }
                    else {
                        return unit.compareTo(t.unit);
                    }
                }
                else {
                    return (order > t.order)? 1: -1;
                }
            }
            else {
                return name.compareTo(t.name);
            }
        }
        else {
            return suite.compareTo(t.suite);
        }
    }
    
}
