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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.api.completion;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author schmidtm
 */
public class NewVarsCCTest extends GroovyTestBase {

    String TEST_BASE = "testfiles/completion/";
    String BASE = TEST_BASE + "newvars/";

    public NewVarsCCTest(String testName) {
        super(testName);
        Logger.getLogger(CompletionHandler.class.getName()).setLevel(Level.FINEST);
    }

    // uncomment this to have logging from GroovyLexer
    protected Level logLevel() {
        // enabling logging
        return Level.INFO;
        // we are only interested in a single logger, so we set its level in setUp(),
        // as returning Level.FINEST here would log from all loggers
    }

    protected @Override Map<String, ClassPath> createClassPathsForTest() {
        Map<String, ClassPath> map = super.createClassPathsForTest();
        map.put(ClassPath.SOURCE, ClassPathSupport.createClassPath(new FileObject[] {
            FileUtil.toFileObject(getDataFile("/testfiles/completion/newvars")) }));
        return map;
    }

    // test new-var suggestions based on identifiers

    public void testIdentifier1() throws Exception {
        checkCompletion(BASE + "Identifier1.groovy", "String str^", false);
    }

    public void testIdentifier2() throws Exception {
        checkCompletion(BASE + "Identifier1.groovy", "Long lo^", false);
    }

    public void testIdentifier3() throws Exception {
        checkCompletion(BASE + "Identifier2.groovy", "Boolean ^", false);
    }

    public void testIdentifier4() throws Exception {
        checkCompletion(BASE + "Identifier3.groovy", "StringBuffer ^", false);
    }

    // test field suggestions

    public void testCompletionField1_1() throws Exception {
        checkCompletion(BASE + "FieldCompletion.groovy", "    Identifier4 i^", false);
    }

    public void testCompletionField1_2() throws Exception {
        checkCompletion(BASE + "FieldCompletion.groovy", "    String ^", false);
    }

    public void testCompletionField1_3() throws Exception {
        checkCompletion(BASE + "FieldCompletion.groovy", "    private String ^", false);
    }

    // test primitve type suggestions

    public void testPrimitive1() throws Exception {
        checkCompletion(BASE + "Primitive1.groovy", "boolean ^", false);
    }

    public void testPrimitive2() throws Exception {
        checkCompletion(BASE + "Primitive1.groovy", "byte ^", false);
    }

    public void testPrimitive3() throws Exception {
        checkCompletion(BASE + "Primitive1.groovy", "char ^", false);
    }

    public void testPrimitive4() throws Exception {
        checkCompletion(BASE + "Primitive1.groovy", "double ^", false);
    }

    public void testPrimitive5() throws Exception {
        checkCompletion(BASE + "Primitive1.groovy", "float ^", false);
    }

    public void testPrimitive6() throws Exception {
        checkCompletion(BASE + "Primitive1.groovy", "int ^", false);
    }

    public void testPrimitive7() throws Exception {
        checkCompletion(BASE + "Primitive1.groovy", "long ^", false);
    }

    public void testPrimitive8() throws Exception {
        checkCompletion(BASE + "Primitive1.groovy", "short ^", false);
    }
}
