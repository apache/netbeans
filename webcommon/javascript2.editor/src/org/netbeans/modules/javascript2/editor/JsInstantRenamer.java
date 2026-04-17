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
package org.netbeans.modules.javascript2.editor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.model.api.Occurrence;
import org.netbeans.modules.javascript2.editor.navigation.OccurrencesSupport;
import org.netbeans.modules.javascript2.editor.navigation.OccurrencesFinderImpl;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.javascript2.model.api.Model;

/**
 *
 * @author Petr Pisl
 */
public class JsInstantRenamer implements InstantRenamer {

    @Override
    public boolean isRenameAllowed(ParserResult info, int caretOffset, String[] explanationRetValue) {
        JsParserResult jsInfo = (JsParserResult)info;
        OccurrencesSupport os = new OccurrencesSupport(Model.getModel(jsInfo, false));
        Occurrence occurrence = os.getOccurrence(jsInfo.getSnapshot().getEmbeddedOffset(caretOffset));
        return occurrence != null;
    }

    @Override
    public Set<OffsetRange> getRenameRegions(ParserResult info, int caretOffset) {
        if (info instanceof JsParserResult pResult) {
            Set<OffsetRange> findOccurrenceRanges = OccurrencesFinderImpl.findOccurrenceRanges(pResult, info.getSnapshot().getEmbeddedOffset(caretOffset));
            HashSet<OffsetRange> sourceRanges = new HashSet<>(findOccurrenceRanges.size());
            for (OffsetRange range : findOccurrenceRanges) {
                sourceRanges.add(LexUtilities.getLexerOffsets(pResult, range));
            }
            return sourceRanges;
        } else {
            return Collections.emptySet();
        }

    }

}
