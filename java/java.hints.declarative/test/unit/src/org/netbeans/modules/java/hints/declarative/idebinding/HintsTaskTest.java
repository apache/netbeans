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
package org.netbeans.modules.java.hints.declarative.idebinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintTokenId;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser;
import org.netbeans.modules.java.hints.spiimpl.TestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class HintsTaskTest extends TestBase {
    
    public HintsTaskTest(String name) {
        super(name);
    }
    
    public void test206116WarnAboutRemovingNonVoidExpression() throws Exception {
        prepareTest("test/Test.java", "");
        
        FileObject hint = sourceRoot.createData("test.hint");
        String code = "Math.min(0, 1) => ;;";
        
        TestUtilities.copyStringToFile(hint, code);
        
        TokenHierarchy<?> h = TokenHierarchy.create(code, DeclarativeHintTokenId.language());
        DeclarativeHintsParser.Result res = new DeclarativeHintsParser().parse(hint, code, h.tokenSequence(DeclarativeHintTokenId.language()));
        List<ErrorDescription> errorInstances = HintsTask.computeErrors(res, code, hint);
        List<String> errors = new ArrayList<>();
        
        for (ErrorDescription ed : errorInstances) {
            errors.add(ed.toString());
        }
        
        assertEquals(Arrays.asList("0:0-0:15:warning:ERR_RemoveExpression"), errors);
    }
    
    public void test206116NoWarningAboutRemovingVoidExpression() throws Exception {
        prepareTest("test/Test.java", "");
        
        FileObject hint = sourceRoot.createData("test.hint");
        String code = "System.err.println() => ;;";
        
        TestUtilities.copyStringToFile(hint, code);
        
        TokenHierarchy<?> h = TokenHierarchy.create(code, DeclarativeHintTokenId.language());
        DeclarativeHintsParser.Result res = new DeclarativeHintsParser().parse(hint, code, h.tokenSequence(DeclarativeHintTokenId.language()));
        List<ErrorDescription> errorInstances = HintsTask.computeErrors(res, code, hint);
        List<String> errors = new ArrayList<>();
        
        for (ErrorDescription ed : errorInstances) {
            errors.add(ed.toString());
        }
        
        assertEquals(Collections.emptyList(), errors);
    }
    
    public void test206116NoWarningAboutRemovingNonExpressions() throws Exception {
        prepareTest("test/Test.java", "");
        
        FileObject hint = sourceRoot.createData("test.hint");
        String code = "Math.min(0, 1); => ;;";
        
        TestUtilities.copyStringToFile(hint, code);
        
        TokenHierarchy<?> h = TokenHierarchy.create(code, DeclarativeHintTokenId.language());
        DeclarativeHintsParser.Result res = new DeclarativeHintsParser().parse(hint, code, h.tokenSequence(DeclarativeHintTokenId.language()));
        List<ErrorDescription> errorInstances = HintsTask.computeErrors(res, code, hint);
        List<String> errors = new ArrayList<>();
        
        for (ErrorDescription ed : errorInstances) {
            errors.add(ed.toString());
        }
        
        assertEquals(Collections.emptyList(), errors);
    }
    
    public void test206116NoWarningAboutRemovingUnattributable() throws Exception {
        prepareTest("test/Test.java", "");
        
        FileObject hint = sourceRoot.createData("test.hint");
        String code = "Math.doesNotExist(0, 1) => ;;";
        
        TestUtilities.copyStringToFile(hint, code);
        
        TokenHierarchy<?> h = TokenHierarchy.create(code, DeclarativeHintTokenId.language());
        DeclarativeHintsParser.Result res = new DeclarativeHintsParser().parse(hint, code, h.tokenSequence(DeclarativeHintTokenId.language()));
        List<ErrorDescription> errorInstances = HintsTask.computeErrors(res, code, hint);
        List<String> errors = new ArrayList<>();
        
        for (ErrorDescription ed : errorInstances) {
            errors.add(ed.toString());
        }
        
        assertFalse(new HashSet<>(errors).contains("ERR_RemoveExpression"));
    }

    public void testTypeConditions() throws Exception {
        prepareTest("test/Test.java", "");

        FileObject hint = sourceRoot.createData("test.hint");
        String code = "$1.length() :: $1 instanceof java.lang.String;;";

        TestUtilities.copyStringToFile(hint, code);

        TokenHierarchy<?> h = TokenHierarchy.create(code, DeclarativeHintTokenId.language());
        DeclarativeHintsParser.Result res = new DeclarativeHintsParser().parse(hint, code, h.tokenSequence(DeclarativeHintTokenId.language()));
        List<ErrorDescription> errorInstances = HintsTask.computeErrors(res, code, hint);
        List<String> errors = new ArrayList<>();

        for (ErrorDescription ed : errorInstances) {
            errors.add(ed.toString());
        }

        assertEquals(Collections.emptyList(), errors);
    }

    static {
        NbBundle.setBranding("test");
    }
}
