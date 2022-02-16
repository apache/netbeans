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

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_SnippetDoc", description = "#DESC_SnippetDoc", category = "general")
@Messages({
    "DN_SnippetDoc=snippet",
    "DESC_SnippetDoc=snippet"
})
public class SnippetDoc {

    @TriggerPattern(value = "$//@highlight")
    @Messages("ERR_SnippetDoc=snippet")
    public static ErrorDescription computeWarning(HintContext ctx) {
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_SnippetDoc());
    }

}
