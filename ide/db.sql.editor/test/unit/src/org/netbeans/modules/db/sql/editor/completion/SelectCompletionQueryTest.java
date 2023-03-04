/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        List<String> modelData = new ArrayList<>();
        try (InputStream is = SelectCompletionQueryTest.class.getResourceAsStream(getName() + ".test");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
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
        }
        String sql = sqlData.toString();
        Metadata metadata = TestMetadata.create(modelData);
        if (stdout) {
            performTest(sql, metadata, System.out);
        } else {
            File result = new File(getWorkDir(), getName() + ".result");
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(result), "utf-8"))) {
                performTest(sql, metadata, writer);
            }
            File pass = new File(getWorkDir(), getName() + ".pass");
            try (InputStream input = SelectCompletionQueryTest.class.getResourceAsStream(getName() + ".pass")) {
                createReferenceFile(input, pass);
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

    private static void createReferenceFile(InputStream is, File dest) throws IOException {
        try (   InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader reader = new BufferedReader(isr);
                OutputStream os = new FileOutputStream(dest);
                OutputStreamWriter osr = new OutputStreamWriter(os, "utf-8");
                BufferedWriter writer = new BufferedWriter(osr)) {
            for (String line; (line = reader.readLine()) != null;) {
                if (line.startsWith("#") || line.trim().length() == 0) {
                    continue;
                }
                writer.write(line);
                writer.write("\n");
            }
        }
    }

    @Override
    public String toString() {
        return getName() + "(" + getClass().getName() + ")";
    }
}
