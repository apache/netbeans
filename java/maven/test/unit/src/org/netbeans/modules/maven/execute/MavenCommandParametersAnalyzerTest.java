/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.maven.execute;

import java.util.Arrays;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import org.junit.Test;

/**
 *
 * @author Benjamin Asbach (asbachb.netbeans@impl.it)
 */
public class MavenCommandParametersAnalyzerTest {

    @Test
    public void defaultIsSingleThreaded() {
        assertFalse(isMultiThreadedMaven(""));
    }
 
    @Test
    public void validateMultiThreaded1() {
        assertTrue(isMultiThreadedMaven("-T 10"));
    }
 
    @Test
    public void validateMultiThreaded2() {
        assertTrue(isMultiThreadedMaven("-T 2"));
    }
 
    @Test
    public void validateMultiThreaded3() {
        assertTrue(isMultiThreadedMaven("-Dmaven.test.skip=true -T 10"));
    }
 
    @Test
    public void validateMultiThreaded4() {
        assertTrue(isMultiThreadedMaven("-Dmaven.test.skip=true -T 2"));
    }
 
    @Test
    public void validateMultiThreaded5() {
        assertTrue(isMultiThreadedMaven("-Dmaven.test.skip=true -T 10 -Dmaven.test.skip=true "));
    }
 
    @Test
    public void validateMultiThreaded6() {
        assertTrue(isMultiThreadedMaven("-Dmaven.test.skip=true -T 2 -Dmaven.test.skip=true "));
    }
 
    @Test
    public void validateMultiThreaded7() {
        assertTrue(isMultiThreadedMaven("-T 10 -Dmaven.test.skip=true"));
    }
 
    @Test
    public void validateMultiThreaded8() {
        assertTrue(isMultiThreadedMaven("-T 2 -Dmaven.test.skip=true"));
    }
 
    @Test
    public void validateMultiThreaded9() {
        assertTrue(isMultiThreadedMaven("--threads 10"));
    }
 
    @Test
    public void validateMultiThreaded10() {
        assertTrue(isMultiThreadedMaven("--threads 2"));
    }
 
    @Test
    public void validateMultiThreaded11() {
        assertTrue(isMultiThreadedMaven("-Dmaven.test.skip=true --threads 10"));
    }
 
    @Test
    public void validateMultiThreaded12() {
        assertTrue(isMultiThreadedMaven("-Dmaven.test.skip=true --threads 2"));
    }
 
    @Test
    public void validateMultiThreaded13() {
        assertTrue(isMultiThreadedMaven("-Dmaven.test.skip=true --threads 10 -Dmaven.test.skip=true "));
    }
 
    @Test
    public void validateMultiThreaded14() {
        assertTrue(isMultiThreadedMaven("-Dmaven.test.skip=true --threads 2 -Dmaven.test.skip=true "));
    }
 
    @Test
    public void validateMultiThreaded15() {
        assertTrue(isMultiThreadedMaven("--threads 10 -Dmaven.test.skip=true"));
    }
 
    @Test
    public void validateMultiThreaded16() {
        assertTrue(isMultiThreadedMaven("--threads 2 -Dmaven.test.skip=true"));
    }

    @Test
    public void validateSingleThreaded1() {
        assertFalse(isMultiThreadedMaven("--threads 1"));
    }

    @Test
    public void validateSingleThreaded2() {
        assertFalse(isMultiThreadedMaven("-T 1"));
    }

    private boolean isMultiThreadedMaven(String params) {
        return MavenCommandLineExecutor.isMultiThreadedMaven(Arrays.asList(params.split(" ")));
    }
}
