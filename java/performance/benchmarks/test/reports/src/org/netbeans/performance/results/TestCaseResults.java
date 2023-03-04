/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.performance.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** Wrapper for performance results related to one test case.
 *
 * @author  Radim Kubacki
 */
public class TestCaseResults implements Comparable {

    public static final int ORDER_FIRST = 1;
    public static final int ORDER_NEXT  = 2;


    public static void main (String [] av) {
        TestCaseResults r = new TestCaseResults("aa", 100, "ms", ORDER_FIRST, "ref suite");
        r.addValue(7);
        r.addValue(7);
        r.addValue(8);
        r.addValue(8);
        r.addValue(9);
        r.addValue(7);r.addValue(8);r.addValue(7);r.addValue(10);
//        r.addValue(131);
//        r.addValue(131);r.addValue(134);r.addValue(134);r.addValue(133);r.addValue(132);
        System.out.println("average "+r.getAverage());
        System.out.println("count "+r.getCount());
        System.out.println("stddev "+r.getStdDev());
        System.out.println("variance "+r.getVariance());
        TestCaseResults r2 = new TestCaseResults("aa", 100, "ms", ORDER_FIRST, "dummy suite");
        r2.addValue(9);
        r2.addValue(7);
        r2.addValue(11);
        r2.addValue(7);
        r2.addValue(8);
        r2.addValue(7);r2.addValue(8);r2.addValue(7);r2.addValue(7);
//        r2.addValue(131);
//        r2.addValue(133);r2.addValue(129);r2.addValue(129);r2.addValue(133);r2.addValue(131);
        System.out.println("average "+r2.getAverage());
        System.out.println("count "+r2.getCount());
        System.out.println("stddev "+r2.getStdDev());
        System.out.println("variance "+r2.getVariance());
        
        ttest(r, r2);
    }
    
    public static TTestValue ttest(TestCaseResults r1, TestCaseResults r2) {
        double sv1 = 0;	// 1st dataset sample variance
        double sv2 = 0;	// 2nd dataset sample variance
        
        // sample--not pop. variance is calculated below
        sv1 = (r1.getSumSquares() / r1.getCount() - r1.getAverage() * r1.getAverage()) *
        (r1.getCount() / (r1.getCount() - 1));
        sv2 = (r2.getSumSquares() / r2.getCount() - r2.getAverage() * r2.getAverage()) *
        (r2.getCount() / (r2.getCount() - 1));
        
    /*	Now it remains to be determined if the population variances can
        be pooled or not. To see this, an F-test can be applied to them.
        This decision is determined automatically, with an alpha level of
        1%, in order to not encumber the user with details.
     */
        
        double f = 0;
        double fAlpha = 0;
        
        if (sv1 > sv2) {
            f = sv1/sv2;
            fAlpha = fDist(r1.getCount() - 1, r2.getCount() - 1, f);
        }
        else {
            f = sv2/sv1;
            fAlpha = fDist(r2.getCount() - 1, r1.getCount() - 1, f);
        }
        
        double df = 0;		// t Test degrees of freedom
        double t = 0;		// t value
        String comment = new String();
        String comment1 = new String();
        
        if (fAlpha <= 0.005) {
            comment = "An F test on the sample variances indicates that they are " +
            "probably not from the same population (the variances " +
            "can't be pooled), at an alpha level of " + fAlpha + "." +
            "Thus, the t-test was set up for samples with unequal varainces. "+
            "(The degrees of freedom were adjusted.)";
            
            double svn1 = sv1 / r1.getCount();
            double svn2 = sv2 / r2.getCount();
            
            df = 	Math.pow(svn1 + svn2, 2) /
            (Math.pow(svn1, 2)/(r1.getCount() + 1)	+ Math.pow(svn2, 2)/(r2.getCount() + 1)) - 2;
            t = Math.abs(r1.getAverage() - r2.getAverage()) / Math.sqrt(sv1 / r1.getCount() + sv2 / r2.getCount());
        }
        else {
            comment = "An F test on the sample variances indicates that they could be " +
            "from the same population, (alpha level of 0.005)." +
            "Accordingly, the t-test was set up for samples with equal population variance.";
            df = 	r1.getCount() + r2.getCount() - 2;
            double sp = Math.sqrt( ((r1.getCount() - 1)*sv1 + (r2.getCount() - 1)*sv2) /
            (r1.getCount() + r2.getCount() - 2) );
            t = Math.abs(r1.getAverage() - r2.getAverage()) * Math.sqrt(r1.getCount() * r2.getCount() / (r1.getCount() + r2.getCount())) / sp;
        }
        
        double pVal = (t!=0)? stDist(df, t): 0.5;
        String pValComment = "" + pVal;
        
        if (pVal <= 0.01) {
            comment1 = "This probability indicates that there is a difference in sample means.\n";
            if (pVal <= 0.0001) {
                pValComment = "< 0.0001";
            }
        }
        else if (pVal <= 0.05) {
            comment1 = "This probability indicates that there may be a difference in sample means.\n";
        }
        else {
            comment1 = "There is not a significant difference in the sample means. " +
            "A difference could not be detected due to large variability, small sample size, " +
            "or both. Of course, it's possible that the samples really are from the same population!\n";
        }
        
        // a hack to take care of garbage data
        if ( (r1.getCount() == 0)||(r2.getCount() == 0) ) {
            comment1 = "There is a problem with the data. Valid delimiters are space, " +
            "comma, tab and newline.\n";
            comment = "\n";
        }
        else if (t == 0) {
            comment1 = "The means are the same for both samples.";
        }
        
        
        // convert variance to std. deviation for report
        sv1 = Math.sqrt(sv1);
        sv2 = Math.sqrt(sv2);
        
        
        // build the report
        String cs =
        "\nMean of first data set                   :\t" + r1.getAverage() +
        "\nStandard deviation of first data set     :\t" + sv1 +
        "\nNumber of observations in the first set  :\t" + r1.getCount() +
        "\nMean of second data set                  :\t" + r2.getAverage() +
        "\nStandard deviation of second data set    :\t" + sv2 +
        "\nNumber of observations in the second set :\t" + r2.getCount() +
        "\n\nDegrees of freedom   :\t" + df +
        "\nt Value (one-tailed) :\t" + t +
        "\nP(x>t)               :\t" + pValComment + "\n" +
        comment1 + comment;
        
        System.out.println(cs);
        
        return new TTestValue (pVal ,t, df, cs);
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
    
    Collection<Integer> values;

    /** flag whether computed values are valid */
    private transient boolean upToDate = false;

    /** computed average */
    private transient double avg;

    /** computed standard deviation */
    private transient double stddev;

    /** computed variance */
    private transient double var;
    
    private transient double sumSquares;
    
    private transient int n;
    
    private TTestValue tt;

    /** Creates a new instance of TestCaseResults */
    public TestCaseResults(String name, int threshold, String unit, int order, String suite) {
        if (name == null || unit == null)
            throw new IllegalArgumentException();
        
        this.name = name;
        this.unit = unit;
        this.threshold = threshold;
        this.order = order;
        this.suite = suite;
        values = new ArrayList<Integer> ();
    }
    
    /** Adds new value to set of measured results. */
    public synchronized void addValue(int val) {
        upToDate = false;
        values.add (new Integer (val));
    }
    
    public double getAverage () {
        compute();
        return avg;
    }
    
    public double getStdDev () {
        compute();
        return stddev;
    }
    
    public double getVariance () {
        compute();
        return var;
    }
    
    public int getCount () {
        compute();
        return n;
    }
    
    public double getSumSquares () {
        compute();
        return sumSquares;
    }
    
    public void setTTest(TTestValue tt) {
    	this.tt = tt;
    }
    
    public TTestValue getTTest() {
    	return tt;
    }
    
    /** updates mean and variances. */
    private synchronized void compute () {
        if (upToDate)
            return;
        
        Iterator it = values.iterator();
        n = values.size();
        avg = stddev = var = sumSquares = 0;
        while (it.hasNext()) {
            int val = ((Integer)it.next()).intValue();
            avg += val;
            sumSquares += val*val;
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
        if (n > 0) {
            avg = avg / n;
        }
        if (n > 1) {
            it = values.iterator();
            double ep = 0d;
            while (it.hasNext()) {
                int v = ((Integer)it.next()).intValue();
                ep += v - avg;
                var += (v - avg)*(v - avg);
            }
            var = (var - ep*ep/n)/(n-1);
            stddev = Math.sqrt(var);

        }
        upToDate = true;
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
    
    /**
     * Getter for property values.
     * @return Value of property values.
     */
    public java.util.Collection<Integer> getValues() {
        return values;
    }
    
    public int hashCode() {
        return name.hashCode() | unit.hashCode() | order | threshold;
    }    
    
    public boolean equals(Object obj) {
        if (!(obj instanceof TestCaseResults)) 
            return false;
        
        TestCaseResults o = (TestCaseResults)obj;
        return name.equals(o.name)
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
        TestCaseResults t = (TestCaseResults)o;
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
    
    private static double logGamma( double xx) {
        // An approximation to ln(gamma(x))
        // define some constants...
        int j;
        double stp = 2.506628274650;
        double cof[] = new double[6];
        cof[0]=76.18009173;
        cof[1]=-86.50532033;
        cof[2]=24.01409822;
        cof[3]=-1.231739516;
        cof[4]=0.120858003E-02;
        cof[5]=-0.536382E-05;
        
        double x = xx-1;
        double tmp = x + 5.5;
        tmp = (x + 0.5)*Math.log(tmp) - tmp;
        double ser = 1;
        for(j=0;j<6;j++){
            x++;
            ser = ser + cof[j]/x;
        }
        double retVal = tmp + Math.log(stp*ser);
        return retVal;
    }
    
    private static double gamma( double x) {
        // An approximation of gamma(x)
        double f = 10E99;
        double g = 1;
        if ( x > 0 ) {
            while (x < 3) {
                g = g * x;
                x = x + 1;
            }
//            f = (1 - (2/(7*Math.pow(x,2))) * (1 - 2/(3*Math.pow(x,2))))/(30*Math.pow(x,2));
            f = (1 - (2/(7*x*x)) * (1 - 2/(3*x*x)))/(30*x*x);
            f = (1-f)/(12*x) + x*(Math.log(x)-1);
            f = (Math.exp(f)/g)*Math.pow(2*Math.PI/x,0.5);
        }
        else {
            f = Double.POSITIVE_INFINITY;
        }
        return f;
    }
    
    private static double betacf(double a,double b,double x){
        // A continued fraction representation of the beta function
        int maxIterations = 50, m=1;
        double eps = 3E-5;
        double am = 1;
        double bm = 1;
        double az = 1;
        double qab = a+b;
        double qap = a+1;
        double qam = a-1;
        double bz = 1 - qab*x/qap;
        double aold = 0;
        double em, tem, d, ap, bp, app, bpp;
        while((m<maxIterations)&&(Math.abs(az-aold)>=eps*Math.abs(az))){
            em = m;
            tem = em+em;
            d = em*(b-m)*x/((qam + tem)*(a+tem));
            ap = az+d*am;
            bp = bz+d*bm;
            d = -(a+em)*(qab+em)*x/((a+tem)*(qap+tem));
            app = ap+d*az;
            bpp = bp+d*bz;
            aold = az;
            am = ap/bpp;
            bm = bp/bpp;
            az = app/bpp;
            bz = 1;
            m++;
        }
        return az;
    }
    
    private static double betai(double a, double b, double x) {
        // the incomplete beta function from 0 to x with parameters a, b
        // x must be in (0,1) (else returns error)
        Double er = new Double(0);
        double bt=0, beta=er.POSITIVE_INFINITY;
        if( x==0 || x==1 ){
            bt = 0; }
        else if((x>0)&&(x<1)) {
            bt = gamma(a+b)*Math.pow(x,a)*Math.pow(1-x,b)/(gamma(a)*gamma(b)); }
        if(x<(a+1)/(a+b+2)){
            beta = bt*betacf(a,b,x)/a; }
        else {
            beta = 1-bt*betacf(b,a,1-x)/b; }
        return beta;
    }
    
    private static double fDist(double v1, double v2, double f) {
                /* 	F distribution with v1, v2 deg. freedom
                        P(x>f)
                 */
        double p =	betai(v1/2, v2/2, v1/(v1 + v2*f));
        return p;
    }
    
    private static double student_c(double v) {
        // Coefficient appearing in Student's t distribution
        return Math.exp(logGamma( (v+1)/2)) / (Math.sqrt(Math.PI*v)*Math.exp(logGamma(v/2)));
    }
    
    private static double student_tDen(double v, double t) {
                /* 	Student's t density with v degrees of freedom
                        Requires gamma, student_c functions
                        Part of Bryan's Java math classes (c) 1997
                 */
        
        return student_c(v)*Math.pow( 1 + (t*t)/v, -0.5*(v+1) );
    }
    
    private static double stDist(double v, double t) {
        
                /* 	Student's t distribution with v degrees of freedom
                        Requires gamma, student_c functions
                        Part of Bryan's Java math classes (c) 1997
                        This only uses compound trapezoid, pending a good integration package
                        Returned value is P( x > t) for a r.v. x with v deg. freedom.
                        NOTE: With the gamma function supplied here, and the simple trapeziodal
                        sum used for integration, the accuracy is only about 5 decimal places.
                        Values below 0.00001 are returned as zero.
                 */
        
        double sm = 0.5;
        double u = 0;
        double sign = 1;
        double stepSize = t/5000;
        if ( t < 0) {
            sign = -1;
        }
        for (u = 0; u <= (sign * t) ; u = u + stepSize) {
            sm = sm + stepSize * student_tDen( v, u);
            // System.out.println("u "+u+" sm "+sm);
        }
        if ( sign < 0 ) {
            sm = 0.5 - sm;
        }
        else {
            sm = 1 - sm;
        }
        if (sm < 0) {
            sm = 0;		// do not allow probability less than zero from roundoff error
        }
        else if (sm > 1) {
            sm = 1;		// do not allow probability more than one from roundoff error
        }
        return  sm ;
    }
    
    public static class TTestValue {
    	/** P(x&gt;t). */
        private double p;
        /** t Value (one tailed). */
        private double t;
        
        /** Degree of freedom. */
        private double degree;

	/** Comment. */
        private String comment;
        
        public TTestValue (double p, double t, double degree, String c) {
            this.p = p;
            this.t = t;
            this.degree = degree;
	    comment = c;
        }
        
        public double getP() { return p; }
        
        public double getT() { return t; }
        
        public double getDF() { return degree; }
        
	public String getComment() { 
            String s = comment;
            int i = s.indexOf('>');
            while (i >= 0) {
                s = s.substring(0, i)+"&gt;"+s.substring(i+1);
                i = s.indexOf('>');
            }
            i = s.indexOf('<');
            while (i >= 0) {
                s = s.substring(0, i)+"&lt;"+s.substring(i+1);
                i = s.indexOf('<');
            }
            return s; 
        }
    }
}
