/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.usages;

import com.sun.tools.javac.api.ClassNamesForFileOraculum;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lahvac
 */
public class ClassNamesForFileOraculumImplTest {

    public ClassNamesForFileOraculumImplTest() {
    }

    @Test
    public void testDivineSources() {
        TestJavaFileObject fo1 = new TestJavaFileObject();
        TestJavaFileObject fo2 = new TestJavaFileObject();
        Map<JavaFileObject, List<String>> fo2FQNs = new HashMap<JavaFileObject, List<String>>();

        fo2FQNs.put(fo1, Arrays.asList("a.b.c.Class1"));
        fo2FQNs.put(fo2, Arrays.asList("e.f.g"));

        ClassNamesForFileOraculum oraculum = new ClassNamesForFileOraculumImpl(fo2FQNs);

        assertArrayEquals(new JavaFileObject[] {fo1}, oraculum.divineSources("a.b.c"));
        assertNull(oraculum.divineSources("a.b"));
        assertNull(oraculum.divineSources("e.f.g"));
    }

    private static final class TestJavaFileObject extends SimpleJavaFileObject {

        public TestJavaFileObject() {
            super(URI.create("test://test.java"), Kind.SOURCE);
        }

    }

}