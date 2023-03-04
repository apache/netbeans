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
 package org.netbeans.modules.java.openjdk.jtreg;

import java.util.ArrayList;
import java.util.List;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
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

@Hint(displayName = "#DN_WrongSourceVersion", description = "#DESC_WrongSourceVersion", category = "general", options=Options.NO_BATCH)
@Messages({
    "DN_WrongSourceVersion=Wrong -source",
    "DESC_WrongSourceVersion=Checks for hardcoded values for -source when --enable-preview"
})
public class WrongSourceVersion {

    @TriggerTreeKind(Kind.COMPILATION_UNIT)
    @Messages({
        "ERR_HardcodedSource=Hardcoded source version, should use ${jdk.version}"
    })
    public static List<ErrorDescription> computeWarning(HintContext ctx) {
        Result tags = TagParser.parseTags(ctx.getInfo());
        List<ErrorDescription> result = new ArrayList<>();
        for (Tag tag : tags.getTags()) {
            if (!"compile".equals(tag.getName())) {
                continue;
            }
            String[] params = tag.getValue().split("[\\s]+");
            boolean hasEnablePreview = Arrays.stream(params).anyMatch(s -> "--enable-preview".equals(s));
            if (hasEnablePreview) {
                for (int i = 0; i < params.length; i++) {
                    if ((params[i].equals("-source") || params[i].equals("--source")) && i + 1 < params.length) {
                        try {
                            Integer.parseInt(params[i + 1]);
                            int pos = tag.getValue().indexOf(params[i]);
                            int start = tag.getValue().indexOf(params[i + 1], pos) + tag.getTagEnd();
                            int end = start + params[i + 1].length();
                            ErrorDescription idealED = ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_HardcodedSource(), new UseJdkSource(ctx.getInfo(), ctx.getPath(), start, end).toEditorFix());

                            result.add(org.netbeans.spi.editor.hints.ErrorDescriptionFactory.createErrorDescription(idealED.getSeverity(), idealED.getDescription(), idealED.getFixes(), ctx.getInfo().getFileObject(), start, end));
                        } catch (NumberFormatException ex) {
                            //OK
                        }
                    }
                }
            }
        }
        return result;
    }

    private static final class UseJdkSource extends JavaFix {

        private final int start;
        private final int end;

        public UseJdkSource(CompilationInfo info, TreePath tp, int start, int end) {
            super(info, tp);
            this.start = start;
            this.end = end;
        }

        @Override
        @Messages("FIX_UseJdkVersion=Use ${jdk.version}")
        protected String getText() {
            return Bundle.FIX_AddRefOutput();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws IOException {
            ctx.getWorkingCopy().rewriteInComment(start, end - start, "${jdk.version}");
        }
    }

}
