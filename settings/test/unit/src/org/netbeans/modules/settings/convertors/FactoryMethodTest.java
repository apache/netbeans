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
package org.netbeans.modules.settings.convertors;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.AnnotationProcessorTestUtils;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FactoryMethodTest extends NbTestCase {
    private File src;
    private File dst;

    public FactoryMethodTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        src = new File(getWorkDir(), "src");
        src.mkdirs();

        dst = new File(getWorkDir(), "dst");
        dst.mkdirs();
    }
    
    
    
    public void testMethodOfGivenNameMustExists() throws IOException {
        AnnotationProcessorTestUtils.makeSource(src, "tst.Bean", 
            "import org.netbeans.api.settings.FactoryMethod;",
            "@FactoryMethod(\"create\")",
            "public class Bean {",
            "}"
        );
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(src, null, dst, null, os);
        
        assertFalse("Compilation fails", res);
        String out = new String(os.toByteArray(), "UTF-8");
        
        if (out.indexOf(" Method named create was not found") == -1) {
            fail("Should warn about missing method\n" + out);
        }
    }

    public void testMethodOfGivenNameMustHaveNoArguments() throws IOException {
        AnnotationProcessorTestUtils.makeSource(src, "tst.Bean", 
            "import org.netbeans.api.settings.FactoryMethod;",
            "@FactoryMethod(\"create\")",
            "public class Bean {",
            "  public static Bean create(boolean x) { return null; }",
            "}"
        );
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(src, null, dst, null, os);
        
        assertFalse("Compilation fails", res);
        String out = new String(os.toByteArray(), "UTF-8");
        
        if (out.indexOf("no parameters") == -1) {
            fail("Should warn about arguments of the method\n" + out);
        }
    }
    public void testMethodOfGivenNameMustBeStatic() throws IOException {
        AnnotationProcessorTestUtils.makeSource(src, "tst.Bean", 
            "import org.netbeans.api.settings.FactoryMethod;",
            "@FactoryMethod(\"create\")",
            "public class Bean {",
            "  public Bean create() { return null; }",
            "}"
        );
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(src, null, dst, null, os);
        
        assertFalse("Compilation fails", res);
        String out = new String(os.toByteArray(), "UTF-8");
        
        if (out.indexOf("has to be static") == -1) {
            fail("Should warn method not being static\n" + out);
        }
    }
    
    public void testMethodOfGivenNameMustHaveTheSameReturnType() throws IOException {
        AnnotationProcessorTestUtils.makeSource(src, "tst.Bean", 
            "import org.netbeans.api.settings.FactoryMethod;",
            "@FactoryMethod(\"create\")",
            "public class Bean {",
            "  public static String create() { return null; }",
            "}"
        );
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(src, null, dst, null, os);
        
        assertFalse("Compilation fails", res);
        String out = new String(os.toByteArray(), "UTF-8");
        
        if (out.indexOf("must return Bean") == -1) {
            fail("Should about wrong return type\n" + out);
        }
    }
}
