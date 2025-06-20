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

import org.junit.Test;
import static org.junit.Assert.*;
import static org.netbeans.modules.maven.execute.MavenCommandLineExecutor.fixTestReferences;
/**
 *
 * @author homberghp
 */
public class MavenCommandLineExecutorTest {

    public MavenCommandLineExecutorTest() {
    }

    @Test
    public void testFixTestReferences1(){
        String value="io.github.jristretto.ranges.InstantRangeTest#io.github.jristretto.ranges.InstantRangeTest.t04Meets"
                + "+io.github.jristretto.ranges.InstantRangeTest.t01Max,io.github.jristretto.ranges.IntegerRangeTest#t04Meets+t01Max";
        String expected= "io.github.jristretto.ranges.InstantRangeTest#t04Meets+t01Max,"
                + "io.github.jristretto.ranges.IntegerRangeTest#t04Meets+t01Max";
        String result=fixTestReferences(value);
        assertEquals(expected,result);

    }
}
