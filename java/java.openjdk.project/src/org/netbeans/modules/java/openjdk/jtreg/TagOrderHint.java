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
import java.util.Comparator;
import java.util.List;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.openjdk.jtreg.TagParser.Result;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_TagOrderHint", description = "#DESC_TagOrderHint", category = "general")
@Messages({
    "DN_TagOrderHint=Tag Order",
    "DESC_TagOrderHint=Checks jtreg tag order"
})
public class TagOrderHint {

    @TriggerTreeKind(Kind.COMPILATION_UNIT)
    @Messages("ERR_TagOrderHint=Incorrect tag order")
    public static ErrorDescription computeWarning(HintContext ctx) {
        Result tags = TagParser.parseTags(ctx.getInfo());
        List<Tag> sorted = sortTags(tags);

        if (!tags.getTags().equals(sorted)) {
            ErrorDescription idealED = ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_TagOrderHint(), new FixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix());
            List<Tag> test = tags.getName2Tag().get("test");

            return org.netbeans.spi.editor.hints.ErrorDescriptionFactory.createErrorDescription(idealED.getSeverity(), idealED.getDescription(), idealED.getFixes(), ctx.getInfo().getFileObject(), test.get(0).getTagStart(), test.get(0).getTagEnd());
        }

        return null;
    }

    private static List<Tag> sortTags(Result tags) {
        List<Tag> sorted = new ArrayList<>(tags.getTags());
        sorted.sort(new Comparator<Tag>() {
            @Override public int compare(Tag o1, Tag o2) {
                int pos1 = TagParser.RECOMMENDED_TAGS_ORDER.indexOf(o1.getName());
                int pos2 = TagParser.RECOMMENDED_TAGS_ORDER.indexOf(o2.getName());

                if (pos1 < 0) pos1 = Integer.MAX_VALUE;
                if (pos2 < 0) pos2 = Integer.MAX_VALUE;

                return pos1 - pos2;
            }
        });
        return sorted;
    }

    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        @Messages("FIX_TagOrderHint=Fix tag order")
        protected String getText() {
            return Bundle.FIX_TagOrderHint();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            List<Tag> sorted = sortTags(TagParser.parseTags(ctx.getWorkingCopy()));

            StringBuilder newText = new StringBuilder();
            int min = Integer.MAX_VALUE;
            int max = 0;

            for (Tag t : sorted) {
                min = Math.min(min, t.getStart());
                max = Math.max(max, t.getEnd());

                newText.append(ctx.getWorkingCopy().getText().substring(t.getStart(), t.getEnd()));
            }

            ctx.getWorkingCopy().rewriteInComment(min, max - min, newText.toString());
        }
    }
}
