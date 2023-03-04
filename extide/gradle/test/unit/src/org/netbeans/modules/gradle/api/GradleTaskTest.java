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
package org.netbeans.modules.gradle.api;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lkishalmi
 */
public class GradleTaskTest {
    

    /**
     * Test of getGroup method, of class GradleTask.
     */
    @Test
    public void testGetGroup1() {
        GradleTask instance = new GradleTask(":run", "application", "run", "");
        String expResult = "application";
        String result = instance.getGroup();
        assertEquals(expResult, result);
    }

    /**
     * Test of getGroup method, of class GradleTask.
     */
    @Test
    public void testGetGroup2() {
        GradleTask instance = new GradleTask(":run", null, "run", "");
        String expResult = GradleBaseProject.PRIVATE_TASK_GROUP;
        String result = instance.getGroup();
        assertEquals(expResult, result);
    }

    /**
     * Test of isPrivate method, of class GradleTask.
     */
    @Test
    public void testIsPrivate() {
        GradleTask instance = new GradleTask(":run", null, "run", "");
        assertTrue(instance.isPrivate());
    }

    /**
     * Test of matches method, of class GradleTask.
     */
    @Test
    public void testMatches() {
        String abbrev = "ru";
        GradleTask instance = new GradleTask(":run", null, "run", "");
        assertTrue(instance.matches(abbrev));
    }

    /**
     * Test of abbrevMatch method, of class GradleTask.
     */
    @Test
    public void testAbbrevMatchEmpty() {
        assertTrue(GradleTask.abbrevMatch("", "run"));
    }
    
    @Test
    public void testAbbrevMatchLong() {
        assertFalse(GradleTask.abbrevMatch("runTest", "run"));
    }
    
    @Test
    public void testAbbrevMatchSimple() {
        assertTrue(GradleTask.abbrevMatch("te", "test"));
    }
    
    @Test
    public void testAbbrevMatchSimpleNeg() {
        assertFalse(GradleTask.abbrevMatch("bu", "test"));
    }

    @Test
    public void testAbbrevMatchComplex1() {
        assertTrue(GradleTask.abbrevMatch("ja", "jacocoTestCoverageVerification"));
    }
    
    @Test
    public void testAbbrevMatchComplex2() {
        assertTrue(GradleTask.abbrevMatch("jaTeCV", "jacocoTestCoverageVerification"));
    }
    
    @Test
    public void testAbbrevMatchComplex3() {
        assertTrue(GradleTask.abbrevMatch("jTC", "jacocoTestCoverageVerification"));
    }
    
    @Test
    public void testAbbrevMatchComplex4() {
        assertTrue(GradleTask.abbrevMatch("aD8R", "assembleDev8Release"));
    }

    @Test
    public void testAbbrevMatchComplex5() {
        assertFalse(GradleTask.abbrevMatch("aDR", "assembleDev8Release"));
    }
    
    @Test
    public void testAbbrevMatchComplex6() {
        assertFalse(GradleTask.abbrevMatch("jTCVerofovation", "jacocoTestCoverageVerification"));
    }
    
    @Test
    public void testAbbrevMatchDumb1() {
        assertFalse(GradleTask.abbrevMatch("${", "assembleDev8Release"));
    }
    
    @Test
    public void testAbbrevMatchDumb2() {
        assertFalse(GradleTask.abbrevMatch(".", "assembleDev8Release"));
    }
    
    @Test
    public void testAbbrevMatchDumb3() {
        assertFalse(GradleTask.abbrevMatch(" ", "assembleDev8Release"));
    }
    
}
