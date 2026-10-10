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
package org.netbeans.modules.php.blade.editor.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.antlr.v4.runtime.Token;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.blade.editor.EditorStringUtils;
import static org.netbeans.modules.php.blade.editor.parser.BladeParserResult.BladeStringReference;

/**
 *
 * @author bogdan
 */
public class BladeReferenceIdsCollection {

    private final Map<OffsetRange, BladeStringReference> referenceIds = new TreeMap<>();
    private final Map<String, List<OffsetRange>> includePathsOccurences = new HashMap<>();

    private final Map<String, OffsetRange> yieldIdsOccurences = new HashMap<>();
    private final Map<String, OffsetRange> stackIdsOccurences = new HashMap<>();

    public String sanitizeIdentifier(Token identifiableStringToken) {
        String rawReferenceId = identifiableStringToken.getText();
        return EditorStringUtils.stripSurroundingQuotes(rawReferenceId.trim());
    }

    public OffsetRange extractOffset(Token identifiableStringToken) {
        //TODO extract the whitespace fragment
        return new OffsetRange(identifiableStringToken.getStartIndex(),
                identifiableStringToken.getStopIndex() + 1);
    }

    public void addReferenceId(int type, String referenceId, OffsetRange range) {
        BladeStringReference reference = new BladeStringReference(type, referenceId);
        referenceIds.put(range, reference);
    }

    @CheckForNull
    public BladeStringReference findOccuredRefrence(int offset) {
        for (Map.Entry<OffsetRange, BladeStringReference> entry : referenceIds.entrySet()) {
            OffsetRange range = entry.getKey();

            if (offset < range.getStart()) {
                //excedeed the offset range
                break;
            }

            if (range.containsInclusive(offset)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public Map<OffsetRange, BladeStringReference> getReferenceIds() {
        return referenceIds;
    }

    public void markIncludeBladeOccurrence(String refName, OffsetRange or) {
        includePathsOccurences.computeIfAbsent(refName, s -> new ArrayList<>()).add(or);
    }

    public Map<String, List<OffsetRange>> getIncludePathsOccurences() {
        return includePathsOccurences;
    }

    public void addYieldOccurence(String identifier, OffsetRange range) {
        yieldIdsOccurences.putIfAbsent(identifier, range);
    }

    public Map<String, OffsetRange> getYieldIdOccurences() {
        return yieldIdsOccurences;
    }
    
    public void addStackOccurence(String identifier, OffsetRange range) {
        stackIdsOccurences.putIfAbsent(identifier, range);
    }
    
    public Map<String, OffsetRange> getStackIdOccurences() {
        return stackIdsOccurences;
    }
}
