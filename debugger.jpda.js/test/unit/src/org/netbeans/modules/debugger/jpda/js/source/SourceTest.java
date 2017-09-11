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
package org.netbeans.modules.debugger.jpda.js.source;

import java.io.OutputStream;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class SourceTest extends NbTestCase {
   public static final String AVATAR_PREFIX = "(function (exports, require, module, __filename, __dirname) {";
   public static final String AVATAR_SUFFIX = "\n});";
  
    public SourceTest(String name) {
        super(name);
    }

    public void testAvatarJSPrefix() throws Exception {
        String js = 
            "(function() {\n"
          + "  alert('Hello');\n"
          + ")();\n";
        
        FileObject fo = FileUtil.createMemoryFileSystem().getRoot().createData("test.js");
        final OutputStream os = fo.getOutputStream();
        os.write(js.getBytes("UTF-8"));
        os.close();
        
        String wrap = AVATAR_PREFIX + js + AVATAR_SUFFIX;
        
        int shift = Source.getContentLineShift(fo.toURL(), wrap);
        assertEquals("No shift at all", 0, shift);
    }
    
    public void testTwoLinesPrefix() throws Exception {
        String js = 
            "(function() {\n"
          + "  alert('Hello');\n"
          + ")();\n";
        
        FileObject fo = FileUtil.createMemoryFileSystem().getRoot().createData("test.js");
        final OutputStream os = fo.getOutputStream();
        os.write(js.getBytes("UTF-8"));
        os.close();
        
        String wrap = "// Written by Martin\n// Tested by Jarda\n" + js;
        
        int shift = Source.getContentLineShift(fo.toURL(), wrap);
        assertEquals("Two lines shift", 2, shift);
        
    }
}
