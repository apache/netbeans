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
package org.netbeans.performance.benchmarks;

import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

/**
 *
 * @author  Petr Nejedly
 * @version 0.9
 */
public class Benchmark extends Assert implements Test {

    /** the reporter that will be notified about results of measurements **/
    private static Reporter reporter;

    /** The name the the actually tested method */
    private String name;

    /** The name of the actually running Benchmark class */
    private String className;
    
    private static final Object[] emptyObjectArray = new Object[0];
    private static final Class[] emptyClassArray = new Class[0];
    
    /** Tells whether we'return doing sharp measurement or some preliminary
     * autocalibration runs.
     */
    private boolean realRun = false;
    
    /** How many iterations to perform in the test method */
    private int iterations;
    
    /** The actual value of the argument */
    private Object argument;
    
    /** The full set of arguments used for this test */
    private Object[] arguments;
    
    /** ptr to BenchmarkSuite for DataManagers */
    private BenchmarkSuite bsuite;
    
    /** Creates new Benchmark without arguments for given test method
     * @param name the name fo the testing method
     */    
    public Benchmark( String name ) {
        this( name, new Object[] { new Object() {
            public String toString() {
                return "";
            }
        }} );
    }

    /** Creates new Benchmark for given test method with given set of arguments
     * @param name the name fo the testing method
     * @param args the array of objects describing arguments to testing method
     */    
    public Benchmark( String name, Object[] args ) {
        this.name = name;
        className = getClass().getName();
        arguments = args; // should we clone it?
    }
    
    // things to override by the implementation of a particular Benchmark

    /** This method is called before the actual test method to allow
     * the benchmark to prepare accordingly to informations available
     * through {@link #getIterationCount}, {@link #getArgument} and {@link #getName}.
     * This method can use assertions to signal failure of the test.
     * @throws Exception This method can throw any exception which is treated as a error in the testing code
     * or testing enviroment.
     */
    protected void setUp() throws Exception {
    }

    /** This method is called after every finished test method.
     * It is intended to be used to free all the resources allocated
     * during {@link #setUp} or the test itself.
     * This method can use assertions to signal failure of the test.
     * @throws Exception This method can throw any exception which is treated as a error in the testing code
     * or testing enviroment.
     */
    protected void tearDown() throws Exception {
    }
    
    /** This method can be overriden by a benchmark that can be resource
     * intensive when set up for more iterations to limit the number
     * of iterations.
     * @return the maximal iteration count the benchmark is able to handle
     *  without loss of precision due swapping, gc()ing and so on.
     * Its return value can depend on values returned by {@link #getName}
     * and {@link #getParameter}.
     */
    protected int getMaxIterationCount() {
        return 50000;
    }
    
    // things to call from a particular benchmark

    /** How many iterations to perform.
     * @return the iteration count the benchmark should perform or
     * the {@link #setUp} should prepare the benchmark for.
     */
    protected final int getIterationCount() {
        return iterations;
    }
    
    /** Which test is to be performed.
     * @return the name of the test method that will be performed.
     * Benchmark writers could use this information during the
     * {@link #setUp} to prepare different conditions for different tests.
     */
    protected final String getName() {
        return name;
    }
    
    /** For which argument the test runs
     * @return the object describing the argument.
     * It will be one of the objects specified in {@link #Benchmark(String,Object[])}
     * constructor or {@link #setArgumentArray(Object[])}
     * It will be <CODE>null</CODE> for tests that didn't specify any argument.
     */    
    protected final Object getArgument() {
        return argument;
    }
    
    /** Sets the set of arguments for this test.
     * @param args the array of objects describing arguments to testing method
     */
    protected final void setArgumentArray( Object[] args ) {
        arguments = args; //do clone ??
    }
    
    
    // the rest is implemetation
    /** How many tests should this Test perform
     * @return the number of tests this Test should perform during
     * {@link #run} method
     */
    public int countTestCases() {
        return arguments.length;
    }

    public final void run( TestResult result ) {
        try {
            Method testMethod = getClass().getMethod( name, emptyClassArray );
            for( int a=0; a<arguments.length; a++ ) {
                
                result.startTest( this );
                
                try {
                    doOneArgument( testMethod, arguments[a] );
                } catch( AssertionFailedError err ) {
                    result.addFailure( this, err );
                } catch( ThreadDeath td ) {
                    throw td;                  // need to propagate this
                } catch( Throwable t ) {
                    result.addError( this, t );
                }
                
                result.endTest( this );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
    
    private void doOneArgument( Method testMethod, Object argument ) throws Exception {
            setArgument( argument );
            checkSetUpData();
            
            // class loading and so on...
            realRun = false;
            doOneMeasurement( testMethod, 1 );
            
            // Do the adaptive measurement, the test have to take at least 200ms
            // to be taken representative enough
            long time = 0;
            int iters = 1;
            int maxIters = getMaxIterationCount();
            
            for( ;; ) {
                realRun = false;
                for( ;; ) {
                    time = doOneMeasurement( testMethod, iters );
                    if( time >= 300 ) break;
                    if( 2*iters > maxIters ) break; // fuse
                    iters *= 2;
                }
                
                if( 2*iters > maxIters ) break; // additional round won't help
                
                // A check it the calibrated iteration count is sufficient
                // when running with realRun == true
                realRun = true;
                time = doOneMeasurement( testMethod, iters );
		if( time > 200 ) break;
		iters *= 2;
            }
            
            // do the real measurement
            realRun = true;
            for( int run = 0; run < 3; run++ ) {
                time = doOneMeasurement( testMethod, iters );
                reportSample( ((float)time) / 1000 / iters );
            }
    }
    
    private void reportSample( float time ) {
        getReporter().addSample( className, name, argument, time );
    }
    
    private static Reporter getReporter() {
        if( reporter == null ) reporter = new PlainReporter();
        return reporter;
    }
    
    /** Set the Reporter to be used by the Benchmark
     */
    public static void setReporter( Reporter rep ) {
        reporter = rep;
    }
    
    /** Helper method to be called from possible main that will take care
     * of wrapping the test with TestSuite an running all the test methods
     * of the testClass. It will also flush the reporter at the end.
     */
    protected static void simpleRun( Class testClass ) {
        junit.textui.TestRunner.run( new TestSuite( testClass ) );
        getReporter().flush();
    }
    
    private void checkSetUpData() throws Exception {
        if (this instanceof DataManager) {
            bsuite.setUpDataFor(this);
        }
    }
    
    final void setSuite(BenchmarkSuite bs) {
        this.bsuite = bs;
    }
        
    private long doOneMeasurement( Method testMethod, int iterations ) throws Exception {
        setIterationCount( iterations );
        setUp();
        cooling();

        long time = System.currentTimeMillis();
        testMethod.invoke( this, emptyObjectArray );
        time = System.currentTimeMillis() - time;

        tearDown();
        return time;
    }

    private void setIterationCount( int count ) {
        iterations = count; 
    }

    void setArgument( Object arg ) {
        argument = arg;
    }
    
    private void cooling() {
        if( !realRun ) return;
        System.gc();
        System.gc();
        try {
            Thread.sleep( 500 );
        } catch( InterruptedException exc ) {}
        System.gc();
        try {
            Thread.sleep( 300 );
        } catch( InterruptedException exc ) {}
    }
}
