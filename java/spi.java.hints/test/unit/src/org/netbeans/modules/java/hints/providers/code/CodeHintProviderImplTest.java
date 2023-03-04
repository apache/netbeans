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

package org.netbeans.modules.java.hints.providers.code;

import com.sun.source.tree.Tree.Kind;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.HintContext;

import static org.junit.Assert.*;

import org.netbeans.modules.java.hints.providers.code.CodeHintProviderImpl.WorkerImpl;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 *
 * @author lahvac
 */
@Hint(displayName="foo", description="bar", id="hintPattern", category="general")
public class CodeHintProviderImplTest {

    public CodeHintProviderImplTest() {
    }

    @Test
    public void testComputeHints() throws Exception {
        Map<HintMetadata, ? extends Collection<? extends HintDescription>> hints = new CodeHintProviderImpl().computeHints();

        Set<String> golden = new HashSet<String>(Arrays.asList(
            "$1.toURL():public static org.netbeans.spi.editor.hints.ErrorDescription org.netbeans.modules.java.hints.providers.code.CodeHintProviderImplTest.hintPattern1(org.netbeans.spi.java.hints.HintContext)",
            "[METHOD_INVOCATION]:public static org.netbeans.spi.editor.hints.ErrorDescription org.netbeans.modules.java.hints.providers.code.CodeHintProviderImplTest.hintPattern2(org.netbeans.spi.java.hints.HintContext)"
        ));

        for (Collection<? extends HintDescription> hds : hints.values()) {
            for (HintDescription d : hds) {
                golden.remove(toString(d));
            }
        }

        assertTrue(golden.toString(), golden.isEmpty());
    }

    private static String toString(HintDescription hd) throws Exception {
        StringBuilder sb = new StringBuilder();

        sb.append(hd.getTrigger());
        sb.append(":");
        
        //TODO: constraints
        sb.append(((WorkerImpl) hd.getWorker()).getMethod().toGenericString());

        return sb.toString();
    }

    @TriggerPattern(value="$1.toURL()", constraints=@ConstraintVariableType(variable="$1", type="java.io.File"))
    public static ErrorDescription hintPattern1(HintContext ctx) {
        return null;
    }

    @TriggerTreeKind(Kind.METHOD_INVOCATION)
    public static ErrorDescription hintPattern2(HintContext ctx) {
        return null;
    }

}