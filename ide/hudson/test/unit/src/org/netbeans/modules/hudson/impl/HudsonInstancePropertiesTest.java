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
package org.netbeans.modules.hudson.impl;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.netbeans.modules.hudson.impl.HudsonInstanceProperties.join;
import static org.netbeans.modules.hudson.impl.HudsonInstanceProperties.split;

/**
 *
 * @author jhavlin
 */
public class HudsonInstancePropertiesTest {

    @Test
    public void testSplit() {
        assertEquals(list(), split(""));
        assertEquals(list("a"), split("a"));
        assertEquals(list("a", "b"), split("a/b"));
        assertEquals(list("a", "b", "ccc"), split("a/b/ccc"));
        assertEquals(
                list("a", "f1/a", "f1/b", "f2/ccc", "d"),
                split("a/f1//a/f1//b/f2//ccc/d"));
        assertEquals(
                list("a/b/c/d/e/f", "a1/b2/c3/d4/e5"),
                split("a//b//c//d//e//f/a1//b2//c3//d4//e5"));
    }

    @Test
    public void testJoin() {
        assertEquals("", join(list()));
        assertEquals("a", join(list("a")));
        assertEquals("a/b", join(list("a", "b")));
        assertEquals("a/b/ccc", join(list("a", "b", "ccc")));
        assertEquals(
                "a/f1//a/f1//b/f2//ccc/d",
                join(list("a", "f1/a", "f1/b", "f2/ccc", "d")));
        assertEquals(
                "a//b//c//d//e//f/a1//b2//c3//d4//e5",
                join(list("a/b/c/d/e/f", "a1/b2/c3/d4/e5")));
    }

    @Test(expected = AssertionError.class)
    public void testJoinChecksSlashStart() {
        join(list("a", "b", "/c", "d"));
    }

    @Test(expected = AssertionError.class)
    public void testJoinChecksSlashEnd() {
        join(list("a", "b", "c/", "d"));
    }

    @Test
    public void testSplitAndJoin() {
        List<String> list = list("job1", "folder1/job1", "folder2/job2", "j3");
        assertEquals(list, split(join(list)));
    }

    @Test
    public void testJoinAndSplit() {
        String string = "job1/folder1//job1/folder2//job2/j3";
        assertEquals(string, join(split(string)));
    }

    private List<String> list(String... items) {
        return Arrays.asList(items);
    }
}
