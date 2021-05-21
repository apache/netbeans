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
 package org.netbeans.modules.java.openjdk.jtreg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.openjdk.jtreg.TagParser.Result;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_MissingRefOutputHint", description = "#DESC_MissingRefOutputHint", category = "general", options=Options.NO_BATCH)
@Messages({
    "DN_MissingRefOutputHint=Missing Reference Output",
    "DESC_MissingRefOutputHint=Checks for missing reference output in jtreg @compile tags."
})
public class MissingRefOutputHint {

    @TriggerTreeKind(Kind.COMPILATION_UNIT)
    @Messages({
        "ERR_NoRef=Reference output missing",
        "ERR_RefFileMissing=Reference output file is missing",
    })
    public static List<ErrorDescription> computeWarning(HintContext ctx) {
        Result tags = TagParser.parseTags(ctx.getInfo());
        List<ErrorDescription> result = new ArrayList<>();
        for (Tag tag : tags.getTags()) {
            if (!"compile".equals(tag.getName())) {
                continue;
            }
            if (tag.getValue().startsWith("/")) {
                String firstParam = tag.getValue().split("[\\s]+", 2)[0];
                boolean hasFail = false;
                boolean hasRef  = false;
                int pos = tag.getTagEnd();
                for (String opt : firstParam.split("/")) {
                    if ("fail".equals(opt)) {
                        hasFail = true;
                    } else if (opt.startsWith("ref=")) {
                        hasRef = true;
                        String fileName = opt.substring(4);
                        if (ctx.getInfo().getFileObject().getParent().getFileObject(fileName) == null) {
                            ErrorDescription idealED = ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_RefFileMissing(), new CreateRefFileFixImpl(ctx.getInfo(), ctx.getPath(), fileName).toEditorFix());

                            result.add(org.netbeans.spi.editor.hints.ErrorDescriptionFactory.createErrorDescription(idealED.getSeverity(), idealED.getDescription(), idealED.getFixes(), ctx.getInfo().getFileObject(), pos + 4, pos + fileName.length() + 4));
                        }
                    }
                    pos += opt.length() + 1;
                }
                if (hasFail && !hasRef) {
                    ErrorDescription idealED = ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_NoRef(), new AddRefFixImpl(ctx.getInfo(), ctx.getPath(), tag.getTagEnd() + firstParam.length()).toEditorFix());

                    result.add(org.netbeans.spi.editor.hints.ErrorDescriptionFactory.createErrorDescription(idealED.getSeverity(), idealED.getDescription(), idealED.getFixes(), ctx.getInfo().getFileObject(), tag.getTagStart(), tag.getTagEnd()));
                }
            }
        }
        return result;
    }

    private static final class AddRefFixImpl extends JavaFix {

        private final int pos;

        public AddRefFixImpl(CompilationInfo info, TreePath tp, int pos) {
            super(info, tp);
            this.pos = pos;
        }

        @Override
        @Messages("FIX_AddRefOutput=Add /ref output")
        protected String getText() {
            return Bundle.FIX_AddRefOutput();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws IOException {
            String fileName = ctx.getWorkingCopy().getFileObject().getName() + ".out";

            ctx.getWorkingCopy().rewriteInComment(pos, 0, "/ref=" + fileName);
            if (ctx.getWorkingCopy().getFileObject().getParent().getFileObject(fileName) == null) {
                ctx.getWorkingCopy().getFileObject().getParent().createData(fileName);
            }
        }
    }

    private static final class CreateRefFileFixImpl extends JavaFix {

        private final String fileName;

        public CreateRefFileFixImpl(CompilationInfo info, TreePath tp, String fileName) {
            super(info, tp);
            this.fileName = fileName;
        }

        @Override
        @Messages("FIX_CreateRefOutput=Create reference output file")
        protected String getText() {
            return Bundle.FIX_CreateRefOutput();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws IOException {
            ctx.getWorkingCopy().getFileObject().getParent().createData(fileName);
        }

    }
}
