/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.db.sql.editor.completion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.explorer.test.api.SQLIdentifiersTestUtilities;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class SelectCompletionQueryTest extends NbTestCase {

    private final boolean stdout;

    public SelectCompletionQueryTest(String testName) {
        this(testName, false);
    }

    public SelectCompletionQueryTest(String testName, boolean stdout) {
        super(testName);
        this.stdout = stdout;
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        // Find a way to add the tests automatically (java.util.zip?).

        suite.addTest(new SelectCompletionQueryTest("selectAll"));
        suite.addTest(new SelectCompletionQueryTest("selectAllWhenSyntheticSchema"));
        suite.addTest(new SelectCompletionQueryTest("selectSimple"));
        suite.addTest(new SelectCompletionQueryTest("selectQualTable"));
        suite.addTest(new SelectCompletionQueryTest("selectQualColumn"));
        suite.addTest(new SelectCompletionQueryTest("selectQualColumnWhenTableInDefaultSchema"));
        suite.addTest(new SelectCompletionQueryTest("selectQualColumnWhenTableInNonDefaultSchema"));
        suite.addTest(new SelectCompletionQueryTest("selectQualColumnWhenTableInSyntheticSchema"));
        suite.addTest(new SelectCompletionQueryTest("selectDoubleQualColumn"));
        suite.addTest(new SelectCompletionQueryTest("selectTripleQualColumn"));
        suite.addTest(new SelectCompletionQueryTest("selectViews"));

        suite.addTest(new SelectCompletionQueryTest("selectAllFrom"));
        suite.addTest(new SelectCompletionQueryTest("selectAllWhenFromClauseEmpty"));
        suite.addTest(new SelectCompletionQueryTest("selectAllFromTableInSyntheticSchema"));
        suite.addTest(new SelectCompletionQueryTest("selectSimpleFrom"));
        suite.addTest(new SelectCompletionQueryTest("selectQualTableFromNonDefaultSchema"));
        suite.addTest(new SelectCompletionQueryTest("selectQualTableFromTableInSyntheticSchema"));
        suite.addTest(new SelectCompletionQueryTest("selectQualColumnFromTableInNonDefaultSchema"));
        suite.addTest(new SelectCompletionQueryTest("selectQualColumnFromQualTableInDefaultSchema"));
        suite.addTest(new SelectCompletionQueryTest("selectQualColumnFromTableNotInFromClause"));
        suite.addTest(new SelectCompletionQueryTest("selectQualColumnFromUnqualTableInDefaultSchema"));
        suite.addTest(new SelectCompletionQueryTest("selectDoubleQualColumnFromQualTableInNonDefaultSchema"));

        suite.addTest(new SelectCompletionQueryTest("selectQuote"));
        suite.addTest(new SelectCompletionQueryTest("selectAllFromQuoted"));
        suite.addTest(new SelectCompletionQueryTest("selectQuotedQualTable"));
        suite.addTest(new SelectCompletionQueryTest("selectQuotedQualColumn"));

        suite.addTest(new SelectCompletionQueryTest("fromAll"));
        suite.addTest(new SelectCompletionQueryTest("fromSimple"));
        suite.addTest(new SelectCompletionQueryTest("fromQualTable"));
        suite.addTest(new SelectCompletionQueryTest("fromJoinCondition"));
        suite.addTest(new SelectCompletionQueryTest("fromJoinConditionAlias"));

        suite.addTest(new SelectCompletionQueryTest("whereAll"));
        suite.addTest(new SelectCompletionQueryTest("whereSimple"));
        suite.addTest(new SelectCompletionQueryTest("whereQualTable"));

        suite.addTest(new SelectCompletionQueryTest("groupBySimple"));
        suite.addTest(new SelectCompletionQueryTest("orderBySimple"));

        suite.addTest(new SelectCompletionQueryTest("selectSubquery"));

        suite.addTest(new SelectCompletionQueryTest("script"));

        suite.addTest(new SelectCompletionQueryTest("dropTableAll"));
        suite.addTest(new SelectCompletionQueryTest("dropTableSimple"));

        // #200367: CC breaks with use of DECLARE/SET
        suite.addTest(new SelectCompletionQueryTest("selectAfterDeclare"));
        suite.addTest(new SelectCompletionQueryTest("selectAfterSet"));

        // #200368: CC does not work with fully qualified names (catalog, schema)
        //          table names are not completed
        suite.addTest(new SelectCompletionQueryTest("selectQualifiedIdentifierFrom"));

        return suite;
    }

    @Override
    public void runTest() throws Exception {
        testCompletion();
    }

    public void testCompletion() throws Exception {
        StringBuilder sqlData = new StringBuilder();
        List<String> modelData = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(SelectCompletionQueryTest.class.getResource(getName() + ".test").openStream(), "utf-8"));
        try {
            boolean separatorRead = false;
            for (String line; (line = reader.readLine()) != null;) {
                if (line.startsWith("#") || line.trim().length() == 0) {
                    continue;
                }
                if (line.equals("--")) {
                    separatorRead = true;
                } else {
                    if (separatorRead) {
                        modelData.add(line);
                    } else {
                        sqlData.append(line);
                    }
                }
            }
        } finally {
            reader.close();
        }
        String sql = sqlData.toString();
        Metadata metadata = TestMetadata.create(modelData);
        if (stdout) {
            performTest(sql, metadata, System.out);
        } else {
            File result = new File(getWorkDir(), getName() + ".result");
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(result), "utf-8"));
            try {
                performTest(sql, metadata, writer);
            } finally {
                writer.close();
            }
            File pass = new File(getWorkDir(), getName() + ".pass");
            InputStream input = SelectCompletionQueryTest.class.getResource(getName() + ".pass").openStream();
            try {
                copyStream(input, pass);
            } finally {
                input.close();
            }
            assertFile(getName(), result, pass, null);
        }
    }

    private static void performTest(String sql, Metadata metadata, Appendable output) throws Exception {
        int caretOffset = sql.indexOf('|');
        if (caretOffset >= 0) {
            sql = sql.replace("|", "");
        } else {
            throw new IllegalArgumentException();
        }
        SQLCompletionQuery query = new SQLCompletionQuery(null);
        SQLCompletionEnv env = SQLCompletionEnv.forScript(sql, caretOffset);
        for (SQLCompletionItem item : query.doQuery(env, metadata, SQLIdentifiersTestUtilities.createNonASCIIQuoter("\""))) {
            output.append(item.toString());
            output.append('\n');
        }
    }

    private static void copyStream(InputStream input, File dest) throws IOException {
        OutputStream output = new FileOutputStream(dest);
        try {
            FileUtil.copy(input, output);
        } finally {
            output.close();
        }
    }

    @Override
    public String toString() {
        return getName() + "(" + getClass().getName() + ")";
    }
}
