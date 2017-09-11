/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
