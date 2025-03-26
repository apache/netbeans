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
public class MavenDaemonCommandParametersAnalyzerTest {

    @Test
    public void defaultIsSingleThreaded() {
        assertTrue(checkisMultiThreadedMvnd(""));
    }

    @Test
    public void validateMultiThreaded1() {
        assertTrue(checkisMultiThreadedMvnd("-T 10"));
    }
 
    @Test
    public void validateMultiThreaded3() {
        assertTrue(checkisMultiThreadedMvnd("-Dmaven.test.skip=true -T 10"));
    }
 
    @Test
    public void validateMultiThreaded5() {
        assertTrue(checkisMultiThreadedMvnd("-Dmaven.test.skip=true -T 10 -Dmaven.test.skip=true "));
    }
 
    @Test
    public void validateMultiThreaded7() {
        assertTrue(checkisMultiThreadedMvnd("-T 10 -Dmaven.test.skip=true"));
    }


    @Test
    public void validateMultiThreaded2() {
        assertTrue(checkisMultiThreadedMvnd("-T 2"));
    }
 
    @Test
    public void validateMultiThreaded4() {
        assertTrue(checkisMultiThreadedMvnd("-Dmaven.test.skip=true -T 2"));
    }
 
    @Test
    public void validateMultiThreaded6() {
        assertTrue(checkisMultiThreadedMvnd("-Dmaven.test.skip=true -T 2 -Dmaven.test.skip=true "));
    }
 
    @Test
    public void validateMultiThreaded8() {
        assertTrue(checkisMultiThreadedMvnd("-T 2 -Dmaven.test.skip=true"));
    }

 
    @Test
    public void validateMultiThreaded9() {
        assertTrue(checkisMultiThreadedMvnd("--threads 10"));
    }
 
    @Test
    public void validateMultiThreaded11() {
        assertTrue(checkisMultiThreadedMvnd("-Dmaven.test.skip=true --threads 10"));
    }
 
    @Test
    public void validateMultiThreaded13() {
        assertTrue(checkisMultiThreadedMvnd("-Dmaven.test.skip=true --threads 10 -Dmaven.test.skip=true "));
    }
 
    @Test
    public void validateMultiThreaded15() {
        assertTrue(checkisMultiThreadedMvnd("--threads 10 -Dmaven.test.skip=true"));
    }
 
    @Test
    public void validateMultiThreaded10() {
        assertTrue(checkisMultiThreadedMvnd("--threads 2"));
    }
 
    @Test
    public void validateMultiThreaded12() {
        assertTrue(checkisMultiThreadedMvnd("-Dmaven.test.skip=true --threads 2"));
    }
 
    @Test
    public void validateMultiThreaded14() {
        assertTrue(checkisMultiThreadedMvnd("-Dmaven.test.skip=true --threads 2 -Dmaven.test.skip=true "));
    }
 
    @Test
    public void validateMultiThreaded16() {
        assertTrue(checkisMultiThreadedMvnd("--threads 2 -Dmaven.test.skip=true"));
    }

    @Test
    public void validateMultiThreaded17() {
        assertTrue(checkisMultiThreadedMvnd("-Dmvnd.threads=2 -Dmaven.test.skip=true"));
    }

    @Test
    public void validateSingleThreaded1() {
        assertFalse(checkisMultiThreadedMvnd("--threads 1"));
    }

    @Test
    public void validateSingleThreaded3() {
        assertFalse(checkisMultiThreadedMvnd("-Dmaven.test.skip=true --threads 1"));
    }

    @Test
    public void validateSingleThreaded5() {
        assertFalse(checkisMultiThreadedMvnd("--threads 1 -Dmaven.test.skip=true"));
    }

    @Test
    public void validateSingleThreaded2() {
        assertFalse(checkisMultiThreadedMvnd("-T 1"));
    }

    @Test
    public void validateSingleThreaded4() {
        assertFalse(checkisMultiThreadedMvnd("-Dmaven.test.skip=true -T 1"));
    }

    @Test
    public void validateSingleThreaded6() {
        assertFalse(checkisMultiThreadedMvnd("-T 1 -Dmaven.test.skip=true"));
    }

    @Test
    public void validateSingleThreaded7() {
        assertFalse(checkisMultiThreadedMvnd("-Dmvnd.threads=1"));
    }

    @Test
    public void validateSingleThreaded8() {
        assertTrue(checkisMultiThreadedMvnd("-Dmvnd.threads=foo -Dmaven.test.skip=true"));
    }

    private boolean checkisMultiThreadedMvnd(String params) {
        return MavenCommandLineExecutor.isMultiThreadedMvnd(Arrays.asList(params.split(" ")));
    }
}
