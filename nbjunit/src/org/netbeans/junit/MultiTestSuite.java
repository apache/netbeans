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

package org.netbeans.junit;

import junit.framework.TestResult;

/**
 *
 * @author Alexander Pepin
 */
public abstract class MultiTestSuite extends NbTestSuite{
    
    /**
     * Creates a new instance of MultiTestSuite
     */
    public MultiTestSuite() {
        setName(this.getClass().getSimpleName());
    }
    
    /**
     * Constructs a MultiTestSuite with the given name.
     */
    public MultiTestSuite(String name){
        super(name);
    }
    
    /**
     * Factory method returns a new instance of a testcases.
     * Should return null if there are no more testcases to be executed.
     */
    protected abstract MultiTestCase nextTestCase();
    
    /**
     * Runs the tests and collects their result in a TestResult.
     */
    public void run(TestResult result) {
        if(isPrepared()){
            runAllTests(result);
            cleanit();
        }
        if(gotFailed())
            createFailLog(result);
    }
    
    /**
     * Creates all testcases and runs them.
     */
    protected void runAllTests(TestResult result){
        MultiTestCase testCase = null;
        while((testCase = nextTestCase()) != null){
            runTest(testCase, result);
        }
    }
    
    //stubs
    
    /**
     * The method is called before executing tests.
     * Can be overridden to perform preliminary actions.
     */
    public void prepare(){}
    /**
     * The method is called after executing tests.
     * Can be overridden to perform closing actions.
     */
    public void cleanup(){}
    
//Safe preparation and cleaning
    private Throwable err = null;
    
    private final boolean isPrepared(){
        boolean result = false;
        try{
            prepare();
            result = true;
        }catch(Throwable e){
            err = e;
            System.out.println("Exception occured while preparing for test "+getName()+": "+e.toString());
            e.printStackTrace();
        }
        return result;
    }
    
    private final void cleanit(){
        try{
            cleanup();
        }catch(Throwable e){
            err = e;
            System.out.println("Exception occured while cleaning after test "+getName()+": "+e.toString());
            e.printStackTrace();
        }
    }
    
    private final boolean gotFailed(){
        return err != null;
    }
    
    private final void createFailLog(TestResult result){
        //Create a new test case
        final String nameFailed = getName()+"FailLog";
        MultiTestCase dummy = new MultiTestCase(){
            public void execute(){}
        };
        dummy.setName(nameFailed);
        dummy.setError(err);
        runTest(dummy, result);
    }
    
}
