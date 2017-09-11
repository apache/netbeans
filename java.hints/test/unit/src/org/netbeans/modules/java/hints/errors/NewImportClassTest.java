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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author lahvac
 */
public class NewImportClassTest extends ErrorHintsTestBase {

    public NewImportClassTest(String name) {
        super(name);
    }
    
    public void testImportHint200742a() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.util.Atomi|cBoolean;\n" +
                       "public class Test { AtomicBoolean b; }\n",
                       Bundle.Change_to_import_X(AtomicBoolean.class.getName(), ""),
                       ("package test;\n" +
                        "import java.util.concurrent.atomic.AtomicBoolean;\n" +
                        "public class Test { AtomicBoolean b; }\n").replaceAll("[ \t\n]+", " "));
    }

    public void testImportHint200742b() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "import java.lang.Map;\n" +
                       "import java.lang.Ma|p.Entry;\n" +
                       "public class Test { Map b; }\n",
                       Bundle.Change_to_import_X(Map.class.getName() + ".Entry", ""),
                       ("package test;\n" +
                        "import java.lang.Map;\n" +
                        "import java.util.Map.Entry;\n" +
                        "public class Test { Map b; }\n").replaceAll("[ \t\n]+", " "));
    }
    
    @Override
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception {
        return new ImportClass().run(info, null, pos, path, new Data<Void>());
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }
    
}
