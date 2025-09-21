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
package org.netbeans.modules.java.hints.jdk;

import org.netbeans.modules.java.hints.Feature;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 *
 * @author mjayan
 */
@NbBundle.Messages({
    "DN_CanUseVT=Can use Virtual Threads",
    "DESC_CanUseVT=Can use virtual threads if workload is not CPU bound or number of concurrent tasks is high",
    "ERR_CanUseVT=Can Use Virtual Thread Executor",
    "ERR_CanUseThreadPerTask=Can Use Executors.newThreadPerTaskExecutor"
})
@Hint(displayName = "#DN_CanUseVT", description = "#DESC_CanUseVT", category = "suggestions", hintKind = Hint.Kind.INSPECTION, severity = Severity.HINT,
        minSourceVersion = "19")
public class CanUseVT {

    @TriggerPatterns({
        @TriggerPattern(value = "java.util.concurrent.Executors.newFixedThreadPool($size)", constraints = @ConstraintVariableType(variable = "$size", type = "int")),
        @TriggerPattern(value = "java.util.concurrent.Executors.newFixedThreadPool($size, $factory)", constraints = {
            @ConstraintVariableType(variable = "$factory", type = "java.util.concurrent.ThreadFactory"),
            @ConstraintVariableType(variable = "$size", type = "int")}),
        @TriggerPattern(value = "java.util.concurrent.Executors.newCachedThreadPool()"),
        @TriggerPattern(value = "java.util.concurrent.Executors.newCachedThreadPool($factory)", constraints
                = @ConstraintVariableType(variable = "$factory", type = "java.util.concurrent.ThreadFactory"))})
    public static ErrorDescription compute(HintContext ctx) {
        if (!Feature.VIRTUAL_THREADS.isEnabled(ctx.getInfo())) {
            return null;
        }
        if (ctx.getVariables().get("$factory") != null) {
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_CanUseThreadPerTask(), new Fix[0]);
        } else {
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_CanUseVT(), new Fix[0]);
        }
    }
}
