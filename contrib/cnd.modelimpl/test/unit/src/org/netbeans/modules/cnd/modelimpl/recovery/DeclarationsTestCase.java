/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.modelimpl.recovery;

import java.io.File;
import org.junit.Test;
import org.netbeans.junit.Manager;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.cnd.modelimpl.recovery.base.Diff;
import org.netbeans.modules.cnd.modelimpl.recovery.base.Diffs;
import org.netbeans.modules.cnd.modelimpl.recovery.base.Golden;
import org.netbeans.modules.cnd.modelimpl.recovery.base.Grammar;
import org.netbeans.modules.cnd.modelimpl.recovery.base.Grammars;
import org.netbeans.modules.cnd.modelimpl.recovery.base.RecoveryTestCaseBase;

/**
 *
 */
@RandomlyFails
public class DeclarationsTestCase  extends RecoveryTestCaseBase {
    private static final String SOURCE = "undefined_type.cc";
    public DeclarationsTestCase(String testName, Grammar gramma, Diff diff, Golden golden) {
        super(testName, gramma, diff, golden);
    }

    @Override
    protected File getTestCaseDataDir() {
        return Manager.normalizeFile(new File(getDataDir(), "common/recovery/declarations"));
    }

    @Grammar(newGrammar = false)
    @Golden
    @Test
    public void A_Golden() throws Exception {
        implTest(SOURCE);
    }

    @Grammar(newGrammar = true)
    @Diff(file=SOURCE)
    @Test
    public void definedParaneterType() throws Exception {
        implTest(SOURCE);
    }
    
    @Grammars({
        @Grammar(newGrammar = false),
        @Grammar(newGrammar = true)
    })
    @Diffs({
        @Diff(file=SOURCE, line = 2, column = 9, length = 3, insert = "string"),
        @Diff(file=SOURCE, line = 2, column = 9, length = 3, insert = "string<int>"),
        @Diff(file=SOURCE, line = 2, column = 9, length = 3, insert = "vector<string>"),
        @Diff(file=SOURCE, line = 2, column = 9, length = 3, insert = "std::string"),
        @Diff(file=SOURCE, line = 2, column = 9, length = 3, insert = "class string"),
        @Diff(file=SOURCE, line = 2, column = 9, length = 3, insert = "::string"),
    })
    @Test
    public void undefinedParameterType1() throws Exception {
        implTest(SOURCE);
    }

    @Grammars({
        @Grammar(newGrammar = false),
        @Grammar(newGrammar = true)
    })
    @Diffs({
        @Diff(file=SOURCE, line = 2, column = 1, length = 3, insert = "string"),
        @Diff(file=SOURCE, line = 2, column = 1, length = 3, insert = "string<int>"),
        @Diff(file=SOURCE, line = 2, column = 1, length = 3, insert = "vector<string>"),
        @Diff(file=SOURCE, line = 2, column = 1, length = 3, insert = "std::string"),
        @Diff(file=SOURCE, line = 2, column = 1, length = 3, insert = "class string"),
        @Diff(file=SOURCE, line = 2, column = 1, length = 3, insert = "::string"),
    })
    @Test
    public void undefinedReturnType1() throws Exception {
        implTest(SOURCE);
    }
}