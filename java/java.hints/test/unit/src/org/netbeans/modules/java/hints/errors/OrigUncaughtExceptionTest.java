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
package org.netbeans.modules.java.hints.errors;

import java.io.File;
import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;

/**
 * Tests for Uncaught Exceptions (add throws, surround with try-catch)
 * @author Max Sauer
 */
public class OrigUncaughtExceptionTest extends HintsTestBase {

    public OrigUncaughtExceptionTest(String name) {
	super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.doSetUp("org/netbeans/modules/java/hints/resources/layer.xml");
    }
    
    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/UncaughtExceptionTest/";
    }
    
    /** Surround with try catch as this() parameter */
    public void testBug113448() throws Exception {
	performTestDoNotPerform("Test", 8, 22);
    }
    
    /** Surround with try catch as this() parameter, deeper in path */
    public void testBug113448b() throws Exception {
	performTestDoNotPerform("Test", 12, 33);
    }
    
    /** 
     * Surround with try catch as this() parameter
     * exception-throwing-method call
     */
    public void testBug113448c() throws Exception {
	performTestDoNotPerform("Test", 19, 33);
    }
    
    /**
     * Don't offer surround with t-c for fields 
     */ 
    public void testBug95535() throws Exception {
	performTestDoNotPerform("Test", 34, 21);
    }
    
    /**
     * Field access inside ctor
     */
    public void testBug113812() throws Exception {
	performTestDoNotPerform("Test", 31, 23);
    }
    
    /** Surround with try catch inside ctor */
    public void testInsideCtor() throws Exception {
	performTestDoNotPerform("Test", 16, 21);
    }
    
    /** Surround with try catch as this() parameter, 
     * but inside anonymous class. Should offer in this case.
     */
    public void testThisParamInsideAnonymous() throws Exception {
	performTestDoNotPerform("Test", 24, 30);
    }
    
    /**
     * Offer proper exception types for generic parametrized methods
     */ 
    public void testBug113380a() throws Exception {
	performTestDoNotPerform("TestBug113380", 13, 17);
    }
    
    public void testBug113380b() throws Exception {
	performTestDoNotPerform("TestBug113380", 14, 17);
    }

    /**
     * Surround with try-catch should be offered inside initializers
     * @throws java.lang.Exception
     */
    public void testSurroundWithTCInsideInitializer() throws Exception {
        performTestDoNotPerform("TestInitializer", 7, 17);
    }

    public void testBug88923() throws Exception {
        performTestDoNotPerform("TestBug88923", 8, 11);
    }
    
    public void testBug123850a() throws Exception {
        performTestDoNotPerform("TestBug123850a", 7, 18);
    }
    
    public void testBug123850b() throws Exception {
        performTestDoNotPerform("TestBug123850b", 7, 18);
    }
    
    public void testBug123850c() throws Exception {
        performTestDoNotPerform("TestBug123850c", 7, 18);
    }
    
    public void testBug123850d() throws Exception {
        performTestDoNotPerform("TestBug123850d", 7, 18);
    }
    
    public void testBug123850e() throws Exception {
        performTestDoNotPerform("TestBug123850e", 7, 18);
    }
    
    public void testBug123093() throws Exception {
        performTestDoNotPerform("TestBug123093", 11, 18);
    }
    
    /**
     * Duplicate entries should not be offered
     * (ie, two same exception thrown on same line, like:<br>
     * <code>new Filereader("").read();</code>
     */
    public void testDuplicateHintEntries() throws Exception {
        performTestDoNotPerform("TestDuplicate", 5, 15);
    }

    @Override
    public File getGoldenFile(String filename) {
        String goldenFileName = "org/netbeans/modules/java/hints/errors/UncaughtExceptionTest/" + filename;
        // golden files are in ${xtest.data}/goldenfiles/${classname}/...
        File goldenFile = new File(getDataDir() + "/goldenfiles/" + goldenFileName);
        if (goldenFile.exists()) {
            // Return if found, otherwise try to find golden file in deprecated
            // location. When deprecated part is removed, add assertTrue(goldenFile.exists())
            // instead of if clause.
            return goldenFile;
        }
        fail("cannot file golden file: " + goldenFile.getAbsolutePath());
        return null;
    }

    @Override
    protected boolean onlyMainResource() {
        return true;
    }
    
}
