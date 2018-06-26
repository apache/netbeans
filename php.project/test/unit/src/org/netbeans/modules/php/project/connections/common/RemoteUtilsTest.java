/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.connections.common;

import org.netbeans.junit.NbTestCase;

public class RemoteUtilsTest extends NbTestCase {

    public RemoteUtilsTest(String name) {
        super(name);
    }

    public void testSanitizeDirectoryPath() {
        assertEquals("/dir", RemoteUtils.sanitizeDirectoryPath("/dir/"));
        assertEquals("/dir", RemoteUtils.sanitizeDirectoryPath("/dir//"));
        assertEquals("/dir", RemoteUtils.sanitizeDirectoryPath("/dir/////"));
        assertEquals("/", RemoteUtils.sanitizeDirectoryPath("/"));
    }

    public void testSanitizeUploadDirectory() {
        assertEquals("/dir", RemoteUtils.sanitizeUploadDirectory("/dir", true));
        assertEquals("/dir", RemoteUtils.sanitizeUploadDirectory("/dir", false));
        assertEquals("/", RemoteUtils.sanitizeUploadDirectory("/", false));
        assertEquals("", RemoteUtils.sanitizeUploadDirectory("/", true));
        assertEquals("/", RemoteUtils.sanitizeUploadDirectory(null, false));
        assertEquals("/", RemoteUtils.sanitizeUploadDirectory("", false));
        assertEquals("", RemoteUtils.sanitizeUploadDirectory(null, true));
        assertEquals("", RemoteUtils.sanitizeUploadDirectory("", true));
    }

    public void testParentPathForFiles() {
        assertEquals(null, RemoteUtils.getParentPath("a"));
        assertEquals("a", RemoteUtils.getParentPath("a/b"));
        assertEquals("a/b", RemoteUtils.getParentPath("a/b/c"));
        assertEquals("/", RemoteUtils.getParentPath("/a"));
        assertEquals("/a", RemoteUtils.getParentPath("/a/b"));
        assertEquals("/a/b", RemoteUtils.getParentPath("/a/b/c"));
    }

    public void testParentPathForFolders() {
        assertEquals(null, RemoteUtils.getParentPath("/"));
        assertEquals(null, RemoteUtils.getParentPath("a/"));
        assertEquals("a", RemoteUtils.getParentPath("a/b/"));
        assertEquals("a/b", RemoteUtils.getParentPath("a/b/c/"));
        assertEquals("/", RemoteUtils.getParentPath("/a/"));
        assertEquals("/a", RemoteUtils.getParentPath("/a/b/"));
        assertEquals("/a/b", RemoteUtils.getParentPath("/a/b/c/"));
    }

    public void testNameForFiles() {
        assertEquals("a", RemoteUtils.getName("a"));
        assertEquals("b", RemoteUtils.getName("a/b"));
        assertEquals("c", RemoteUtils.getName("a/b/c"));
        assertEquals("a", RemoteUtils.getName("/a"));
        assertEquals("b", RemoteUtils.getName("/a/b"));
        assertEquals("c", RemoteUtils.getName("/a/b/c"));
    }

    public void testNameForFolders() {
        assertEquals("/", RemoteUtils.getName("/"));
        assertEquals("a", RemoteUtils.getName("a/"));
        assertEquals("b", RemoteUtils.getName("a/b/"));
        assertEquals("c", RemoteUtils.getName("a/b/c/"));
        assertEquals("a", RemoteUtils.getName("/a/"));
        assertEquals("b", RemoteUtils.getName("/a/b/"));
        assertEquals("c", RemoteUtils.getName("/a/b/c/"));
    }

}
