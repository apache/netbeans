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
package org.netbeans.modules.parsing.lucene;

import java.io.IOException;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.lucene.index.FilteredTermsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.QueryVisitor;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.automaton.Automata;
import org.apache.lucene.util.automaton.CompiledAutomaton;
import org.apache.lucene.util.automaton.Operations;


public class RegexpFilter extends MultiTermQuery {

    private static final BitSet SPECIAL_CHARS = new BitSet(126);
    static {
        final char[] specials = new char[]{'{', '}', '[', ']', '(', ')', '\\', '.', '*', '?', '+'}; //NOI18N
        for (char c : specials) {
            SPECIAL_CHARS.set(c);
        }
    }
    private static final BitSet QUANTIFIER_CHARS = new BitSet(126);
    static {
        final char[] specials = new char[]{'{', '*', '?'}; //NOI18N
        for (char c : specials) {
            QUANTIFIER_CHARS.set(c);
        }
    }
    private final CompiledAutomaton startPrefix;
    private final Pattern pattern;

    public RegexpFilter(final String fieldName, final String regexp, final boolean caseSensitive) {
        super(fieldName, MultiTermQuery.CONSTANT_SCORE_BLENDED_REWRITE);
        this.pattern = caseSensitive ? Pattern.compile(regexp) : Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
        this.startPrefix = new CompiledAutomaton(Operations.concatenate(List.of(Automata.makeString(getStartText(regexp)), Automata.makeAnyString())));
    }

    @Override
    protected TermsEnum getTermsEnum(Terms terms, AttributeSource atts) throws IOException {
        return new FilteredTermsEnum(startPrefix.getTermsEnum(terms), false) {
            @Override
            protected FilteredTermsEnum.AcceptStatus accept(BytesRef term) throws IOException {
                if (pattern.matcher(term.utf8ToString()).matches()) {
                    return FilteredTermsEnum.AcceptStatus.YES;
                }
                return FilteredTermsEnum.AcceptStatus.NO;
            }

            @Override
            protected BytesRef nextSeekTerm(BytesRef currentTerm) throws IOException {
                if(currentTerm == null || currentTerm == tenum.term()) {
                    return tenum.next();
                } else {
                    throw new IllegalStateException("TermsEnum does not enable seeking");
                }
            }
        };
    }

    @Override
    public void visit(QueryVisitor visitor) {
        if (visitor.acceptField(field)) {
            visitor.visitLeaf(this);
        }
    }

    static String getStartText(final String regexp) {
        final StringBuilder startBuilder = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < regexp.length(); i++) {
            char c = regexp.charAt(i);
            if ((!quoted) && i < (regexp.length() - 1)) {
                char lookAhead = regexp.charAt(i + 1);
                if (QUANTIFIER_CHARS.get(lookAhead)) {
                    break;
                }
            }
            if (c == '\\' && (i + 1) < regexp.length()) {
                //NOI18N
                char cn = regexp.charAt(i + 1);
                if (!quoted && cn == 'Q') {
                    //NOI18N
                    quoted = true;
                    i++;
                    continue;
                } else if (cn == 'E') {
                    //NOI18N
                    quoted = false;
                    i++;
                    continue;
                }
            } else if (!quoted && (c == '^' || c == '$')) {
                //NOI18N
                continue;
            }
            if (!quoted && SPECIAL_CHARS.get(c)) {
                break;
            }
            startBuilder.append(c);
        }
        return startBuilder.toString();
    }

    @Override
    public String toString(String field) {
        StringBuilder buffer = new StringBuilder();
        if (this.getField() != null) {
            buffer.append(this.getField());
            buffer.append(":");
        }
        buffer.append('/');
        buffer.append(pattern.toString());
        buffer.append('/');
        return buffer.toString();
    }

}
