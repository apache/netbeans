/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * 
 * Portions Copyrighted 2007-2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class OverrideWeakerAccessTest extends ErrorHintsTestBase {
    
    public OverrideWeakerAccessTest(String testName) {
        super(testName);
    }

    public void testSimple() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test { String |toString() { return null; } }",
                       Bundle.FIX_ChangeModifiers("toString", "public"),
                       "package test; public class Test { public String toString() { return null; } }");
    }
    
    public void testInterface() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test implements Runnable { void |run() { } }",
                       Bundle.FIX_ChangeModifiers("run", "public"),
                       "package test; public class Test implements Runnable { public void run() { } }");
    }
    
    public void testProtected() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test extends T { private void |run() { } } class T { protected void run() {} }",
                       Bundle.FIX_ChangeModifiers("run", "protected"),
                       "package test; public class Test extends T { protected void run() { } } class T { protected void run() {} }");
    }
    
    public void testDefault() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test extends T { private void |run() { } } class T { void run() {} }",
                       Bundle.FIX_DefaultAccess("run"),
                       "package test; public class Test extends T { void run() { } } class T { void run() {} }");
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new OverrideWeakerAccess().run(info, null, pos, path, null);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

}
