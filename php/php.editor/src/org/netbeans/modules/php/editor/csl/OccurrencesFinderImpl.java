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

package org.netbeans.modules.php.editor.csl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.CodeMarker;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.Occurence;
import org.netbeans.modules.php.editor.model.Occurence.Accuracy;
import org.netbeans.modules.php.editor.model.OccurencesSupport;
import org.netbeans.modules.php.editor.model.OccurrenceHighlighter;
import org.netbeans.modules.php.editor.options.MarkOccurencesSettings;
import org.netbeans.modules.php.editor.parser.PHPParseResult;

/**
 *
 * @todo Put task cancel support in reasonable places
 *
 * @author Radek Matous
 */
public class OccurrencesFinderImpl extends OccurrencesFinder {
    private Map<OffsetRange, ColoringAttributes> range2Attribs = Collections.emptyMap();
    private int caretPosition;
    private volatile boolean cancelled;

    @Override
    public void setCaretPosition(int position) {
        this.caretPosition = position;
    }

    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField") // field holds immutable map
    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        return range2Attribs;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public void run(Result result, SchedulerEvent event) {
        if (cancelled) {
            cancelled = false;
            return;
        }

        Preferences node = MarkOccurencesSettings.getCurrentNode();
        Map<OffsetRange, ColoringAttributes> localRange2Attribs = new HashMap<>();
        if (node.getBoolean(MarkOccurencesSettings.ON_OFF, true)) {
            for (OffsetRange r : compute((ParserResult) result, caretPosition)) {
                localRange2Attribs.put(r, ColoringAttributes.MARK_OCCURRENCES);
            }
        }

        if (cancelled) {
            cancelled = false;
            return;
        }

        if (!localRange2Attribs.isEmpty()) {
            // store the new occurrences if any were found
            range2Attribs = Collections.unmodifiableMap(localRange2Attribs);
        } else if (!node.getBoolean(MarkOccurencesSettings.KEEP_MARKS, true)) {
            // clear occurrences if "Keep Marks" is not selected. If "Keep Marks"
            // is enabled, the old ranges must be retained.
            range2Attribs = Collections.emptyMap();
        }
    }

    private Collection<OffsetRange> compute(final ParserResult parameter, final int offset) {
        final PHPParseResult parseResult = (PHPParseResult) parameter;
        Set<OffsetRange> result = new TreeSet<>(new Comparator<OffsetRange>() {
            @Override
            public int compare(OffsetRange o1, OffsetRange o2) {
                return o1.compareTo(o2);
            }
        });
        final TokenHierarchy<?> tokenHierarchy = parseResult.getSnapshot().getTokenHierarchy();
        TokenSequence<PHPTokenId> tokenSequence = tokenHierarchy != null ? LexUtilities.getPHPTokenSequence(tokenHierarchy, offset) : null;
        if (cancelled) {
            return Collections.EMPTY_LIST;
        }
        OffsetRange referenceSpan = tokenSequence != null ? DeclarationFinderImpl.getReferenceSpan(tokenSequence, offset, parseResult.getModel()) : OffsetRange.NONE;
        if (!referenceSpan.equals(OffsetRange.NONE)) {
            result.addAll(getOccurrences(parseResult.getModel(), referenceSpan));
        } else {
            OccurrenceHighlighter highlighter = OccurrenceHighlighter.NONE;
            OffsetRange referenceSpanForCodeMarkers = tokenSequence != null ? getReferenceSpanForCodeMarkers(tokenSequence, offset) : OffsetRange.NONE;
            if (!referenceSpanForCodeMarkers.equals(OffsetRange.NONE)) {
                highlighter = getCodeMarkersHighlighter(parseResult.getModel(), referenceSpanForCodeMarkers);
            }
            result.addAll(highlighter.getRanges());
        }
        return result;
    }

    private Collection<OffsetRange> getOccurrences(Model model, OffsetRange referenceSpan) {
        Collection<OffsetRange> result = new TreeSet<>();
        OccurencesSupport occurencesSupport = model.getOccurencesSupport(referenceSpan);
        if (cancelled) {
            return Collections.EMPTY_LIST;
        }
        Occurence caretOccurence = occurencesSupport.getOccurence();
        if (cancelled) {
            return Collections.EMPTY_LIST;
        }
        if (caretOccurence != null) {
            final EnumSet<Accuracy> handledAccuracyFlags = EnumSet.<Occurence.Accuracy>of(
                    Accuracy.EXACT, Accuracy.EXACT_TYPE, Accuracy.MORE, Accuracy.MORE_TYPES,
                    Accuracy.UNIQUE,  Accuracy.MORE_MEMBERS);
            if (handledAccuracyFlags.contains(caretOccurence.degreeOfAccuracy())) {
                PhpElementKind kind = caretOccurence.getKind();
                if (!kind.equals(PhpElementKind.INCLUDE)) {
                    Collection<Occurence> allOccurences = caretOccurence.getAllOccurences();
                    for (Occurence occurence : allOccurences) {
                        if (handledAccuracyFlags.contains(caretOccurence.degreeOfAccuracy())) {
                            result.add(occurence.getOccurenceRange());
                        }
                    }
                }
            }
        }
        return result;
    }

    private OccurrenceHighlighter getCodeMarkersHighlighter(Model model, OffsetRange referenceSpanForCodeMarkers) {
        OccurrenceHighlighter highlighter = new OccurrenceHighlighterImpl();
        OccurencesSupport occurencesSupport = model.getOccurencesSupport(referenceSpanForCodeMarkers);
        if (cancelled) {
            return highlighter;
        }
        CodeMarker codeMarker = occurencesSupport.getCodeMarker();
        if (cancelled) {
            return highlighter;
        }
        if (codeMarker != null) {
            Collection<? extends CodeMarker> allMarkers = codeMarker.getAllMarkers();
            for (CodeMarker marker : allMarkers) {
                marker.highlight(highlighter);
            }
        }
        return highlighter;
    }

    private static OffsetRange getReferenceSpanForCodeMarkers(TokenSequence<PHPTokenId> ts, final int caretOffset) {
        ts.move(caretOffset);
        if (ts.moveNext()) {
            Token<PHPTokenId> token = ts.token();
            PHPTokenId id = token.id();
            if (id.equals(PHPTokenId.PHP_FUNCTION) || id.equals(PHPTokenId.PHP_RETURN)) {
                return new OffsetRange(ts.offset(), ts.offset() + token.length());
            }
        }
        return OffsetRange.NONE;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    private static final class OccurrenceHighlighterImpl implements OccurrenceHighlighter {
        private Set<OffsetRange> offsetRanges = new TreeSet<>();

        @Override
        public void add(OffsetRange offsetRange) {
            offsetRanges.add(offsetRange);
        }

        @Override
        public Set<OffsetRange> getRanges() {
            return new TreeSet<>(offsetRanges);
        }
    }
}
