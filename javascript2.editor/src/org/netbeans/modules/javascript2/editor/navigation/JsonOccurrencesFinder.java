/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.navigation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.parser.JsonParserResult;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.types.spi.ParserResult;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

/**
 *
 * @author Dusan Balek
 */
public class JsonOccurrencesFinder extends OccurrencesFinder<JsonParserResult> {

    private final AtomicBoolean cancelled = new AtomicBoolean();
    private int caretPosition;
    private Map<OffsetRange, ColoringAttributes> range2Attribs;    

    @Override
    public void setCaretPosition(int position) {
        caretPosition = position;
    }

    @Override
    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        return range2Attribs;
    }

    @Override
    public void run(JsonParserResult result, SchedulerEvent event) {
        range2Attribs = calculateOccurences(result, caretPosition, true, cancelled);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        cancelled.set(true);
    }

    @NonNull
    public static Set<OffsetRange> calculateOccurances(
            @NonNull final ParserResult result,
            final int caretPosition,
            final boolean includeQuotes) {
        final Map<OffsetRange,ColoringAttributes> occ = calculateOccurences(
                result,
                caretPosition,
                includeQuotes,
                new AtomicBoolean());
        return occ == null ?
                Collections.emptySet() :
                occ.keySet();
    }

    @CheckForNull
    private static Map<OffsetRange, ColoringAttributes> calculateOccurences(
            @NonNull final ParserResult result,
            final int caretPosition,
            boolean includeQuotes,
            @NonNull final AtomicBoolean cancelled) {
        if (cancelled.getAndSet(false)) {
            return null;
        }
        TokenHierarchy<?> th = result.getSnapshot().getTokenHierarchy();
        if (th == null) {
            return null;
        }
        TokenSequence<JsTokenId> ts = th.tokenSequence(JsTokenId.jsonLanguage());
        if (ts == null) {
            return null;
        }
        int offset = result.getSnapshot().getEmbeddedOffset(caretPosition);
        int delta = ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()){
            return null;
        }
        final Model model = Model.getModel(result, false);
        if (model == null) {
            return null;
        }
        Token<? extends JsTokenId> token = ts.token();
        JsTokenId tokenId = token.id();
        if (tokenId != JsTokenId.STRING && delta == 0 && ts.movePrevious()) {
            token = ts.token();
            tokenId = token.id();
        }
        ts.movePrevious();
        final Token<? extends JsTokenId> prevToken = LexUtilities.findPreviousNonWsNonComment(ts);
        final JsTokenId prevTokenId = prevToken.id();
        Set<OffsetRange> ranges = new HashSet<>();
        if (tokenId == JsTokenId.STRING && (prevTokenId == JsTokenId.BRACKET_LEFT_CURLY || prevTokenId == JsTokenId.OPERATOR_COMMA)) {
            CharSequence text = token.text();
            findRanges(model.getGlobalObject(), text.subSequence(1, text.length() - 1).toString(), includeQuotes, ranges);
        }
        final Map<OffsetRange, ColoringAttributes> res = new HashMap<>();
        if (cancelled.getAndSet(false)) {
            return null;
        }
        for (OffsetRange offsetRange : ranges) {
            res.put(ModelUtils.documentOffsetRange(result, offsetRange.getStart(), offsetRange.getEnd()), ColoringAttributes.MARK_OCCURRENCES);
        }
        return res;
    }

    private static void findRanges(JsObject object, String key, boolean includeQuotes, Set<OffsetRange> ranges) {
        for (Map.Entry<String, ? extends JsObject> entry : object.getProperties().entrySet()) {
            if (!entry.getValue().isAnonymous() && key.equals(entry.getKey())) {
                ranges.add(new OffsetRange(
                        (includeQuotes ? 0 : 1) + entry.getValue().getOffset(),
                        (includeQuotes ? 2 : 1) + entry.getValue().getOffset() + key.length()));
            }
            findRanges(entry.getValue(), key, includeQuotes, ranges);
        }
    }
}
