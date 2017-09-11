/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript2.debug.sources;

import java.io.IOException;
import static junit.framework.Assert.*;
import org.junit.Test;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Entlicher
 */
public class SourceFSTest {
    
    @Test
    public void testFilesCreation() throws IOException {
        SourceFS fs = new SourceFS();
        checkFileCreation(fs, "simpleName", "Simple Content");
        checkFileCreation(fs, "simpleName.js", "Simple JS Content");
        checkFileCreation(fs, "<eval>.js", "Eval");
        checkFileCreation(fs, "a/b/c/d.js", "ABCD");
        checkFileCreation(fs, "/e/f/g/h.js", "Absolute ABCD");
        checkFileCreation(fs, "a//bb.js", "Two slashes file");
        checkFileCreation(fs, "6911ca99//Users/someone/tools/scripts/script.js#15:15<eval>@1.js", "Wild eval file");
        assertEquals("Simple JS Content", fs.findResource("simpleName.js").asText());
        assertEquals("ABCD", fs.findResource("a/b/c/d.js").asText());
        assertEquals("Wild eval file", fs.findResource("6911ca99//Users/someone/tools/scripts/script.js#15:15<eval>@1.js").asText());
    }
    
    private FileObject checkFileCreation(SourceFS fs, String name, String content) throws IOException {
        FileObject fo = fs.createFile(name, new SourceFilesCache.StringContent(content));
        assertNotNull(name, fo);
        assertEquals(content, fo.asText());
        return fo;
    }
    
}
