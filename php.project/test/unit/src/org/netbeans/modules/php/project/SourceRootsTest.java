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

package org.netbeans.modules.php.project;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.NbTestCase;

public class SourceRootsTest extends NbTestCase {

    public SourceRootsTest(String name) {
        super(name);
    }

    public void testSourceRootsNames() {
        List<String> paths = Arrays.asList(
                makeOsDependentPath("/project1/test")
        );
        List<String> names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(1, names.size());
        assertEquals("", names.get(0));

        paths = Arrays.asList(
                (String) null
        );
        names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(1, names.size());
        assertEquals("", names.get(0));

        paths = Arrays.asList(
                (String) null,
                (String) null
        );
        names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(2, names.size());
        assertEquals("", names.get(0));
        assertEquals("", names.get(1));

        paths = Arrays.asList(
                (String) null,
                (String) null,
                makeOsDependentPath("/project1/test")
        );
        names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(3, names.size());
        assertEquals("", names.get(0));
        assertEquals("", names.get(1));
        assertEquals("test", names.get(2));

        paths = Arrays.asList(
                makeOsDependentPath("/project1/test1"),
                makeOsDependentPath("/project1/test2")
        );
        names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(2, names.size());
        assertEquals("test1", names.get(0));
        assertEquals("test2", names.get(1));

        paths = Arrays.asList(
                makeOsDependentPath("/project1/bundle1/test"),
                makeOsDependentPath("/project1/bundle2/test")
        );
        names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(names.toString(), 2, names.size());
        assertEquals("bundle1", names.get(0));
        assertEquals("bundle2", names.get(1));

        paths = Arrays.asList(
                makeOsDependentPath("/project1/bundle1/test"),
                makeOsDependentPath("/project1/bundle2/test"),
                makeOsDependentPath("/tmp/test")
        );
        names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(3, names.size());
        assertEquals("bundle1", names.get(0));
        assertEquals("bundle2", names.get(1));
        assertEquals("tmp", names.get(2));

        paths = Arrays.asList(
                makeOsDependentPath("/project1/bundle1/test"),
                makeOsDependentPath("/project1/bundle2/test"),
                makeOsDependentPath("/project1/alltests")
        );
        names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(3, names.size());
        assertEquals("bundle1", names.get(0));
        assertEquals("bundle2", names.get(1));
        assertEquals("alltests", names.get(2));
    }

    public void testEmptySourceRootsNames() {
        List<String> names = SourceRoots.getPureSourceRootsNames(Collections.<String>emptyList());
        assertTrue(names.isEmpty());
    }

    public void testInvalidSourceRootsNames() {
        List<String> paths = Arrays.asList(
                makeOsDependentPath("/project1/bundle1/test"),
                makeOsDependentPath("/project1/bundle1/test")
        );
        List<String> names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(names.toString(), 2, names.size());
        assertEquals(makeOsDependentPath("/project1/bundle1/test"), names.get(0));
        assertEquals(makeOsDependentPath("/project1/bundle1/test"), names.get(1));

        paths = Arrays.asList(
                makeOsDependentPath("/project1/bundle1/test"),
                makeOsDependentPath("/project1/bundle1/test"),
                makeOsDependentPath("/tmp/test")
        );
        names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(names.toString(), 3, names.size());
        assertEquals(makeOsDependentPath("/project1/bundle1/test"), names.get(0));
        assertEquals(makeOsDependentPath("/project1/bundle1/test"), names.get(1));
        assertEquals(makeOsDependentPath("/tmp/test"), names.get(2));
    }

    private String makeOsDependentPath(String path) {
        return path.replace('/', File.separatorChar);
    }

}
