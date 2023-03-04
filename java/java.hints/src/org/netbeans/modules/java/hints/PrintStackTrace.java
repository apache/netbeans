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

package org.netbeans.modules.java.hints;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Jancura
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.PrintStackTrace", description = "#DESC_org.netbeans.modules.java.hints.PrintStackTrace", category="code_maturity", suppressWarnings="CallToPrintStackTrace")
public class PrintStackTrace {

    @TriggerPattern(value="$t.printStackTrace ()",
                    constraints=@ConstraintVariableType(variable="$t", type="java.lang.Throwable"))
    public static ErrorDescription checkPrintStackTrace (HintContext ctx) {
        TreePath                treePath = ctx.getPath ();
        CompilationInfo         compilationInfo = ctx.getInfo ();
        return ErrorDescriptionFactory.forName (
            ctx,
            treePath,
            NbBundle.getMessage (PrintStackTrace.class, "MSG_PrintStackTrace"),
        new FixImpl (
NbBundle.getMessage (
LoggerNotStaticFinal.class,
"MSG_PrintStackTrace_fix"
),
TreePathHandle.create (treePath, compilationInfo)
).toEditorFix());
    }

    private static final class FixImpl extends JavaFix {

        private final String    text;

        public FixImpl (
            String              text,
            TreePathHandle      loggerFieldHandle
        ) {
            super(loggerFieldHandle);
            this.text = text;
        }

        @Override
        public String getText () {
            return text;
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            Utilities.removeStatement(wc, tp.getParentPath ());
        }
    } // End of FixImpl class
}
