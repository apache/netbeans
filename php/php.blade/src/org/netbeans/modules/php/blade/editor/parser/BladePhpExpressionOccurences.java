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

import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 *
 * @author bogdan
 */
public class BladePhpExpressionOccurences {

    private final Set<OffsetRange> phpRawInlineExpressionLocations = new TreeSet<>();
    private final Set<OffsetRange> phpInlineExpressionLocations = new TreeSet<>();
    private final Set<OffsetRange> phpOutputExpressionLocations = new TreeSet<>();
    private final Set<OffsetRange> phpForeachExpressionLocations = new TreeSet<>();

    public void markPhpInlineExpressionOccurence(OffsetRange range) {
        phpInlineExpressionLocations.add(range);
    }

    public void markPhpOutputExpressionOccurence(OffsetRange range) {
        phpOutputExpressionLocations.add(range);
    }

    public void markPhpForeachExpressionOccurence(OffsetRange range) {
        phpForeachExpressionLocations.add(range);
    }

    public void markPhpRawInlineExpressionOccurence(OffsetRange range) {
        phpRawInlineExpressionLocations.add(range);
    }

    public OffsetRange findPhpExpressionLocation(int offset) {

        //OUTPUT
        for (OffsetRange range : phpOutputExpressionLocations) {

            if (offset < range.getStart()) {
                //excedeed the offset range
                break;
            }

            if (range.containsInclusive(offset)) {
                return range;
            }
        }

        for (OffsetRange range : phpInlineExpressionLocations) {

            if (offset < range.getStart()) {
                //excedeed the offset range
                break;
            }

            if (range.containsInclusive(offset)) {
                return range;
            }
        }

        for (OffsetRange range : phpRawInlineExpressionLocations) {

            if (offset < range.getStart()) {
                //excedeed the offset range
                break;
            }

            if (range.containsInclusive(offset)) {
                return range;
            }
        }

        //FOREACH
        for (OffsetRange range : phpForeachExpressionLocations) {

            if (offset < range.getStart()) {
                //excedeed the offset range
                break;
            }

            if (range.containsInclusive(offset)) {
                return range;
            }
        }

        return null;
    }

    public Set<OffsetRange> getPhpInlineOccurences() {
        return phpInlineExpressionLocations;
    }

    public Set<OffsetRange> getPhpRawInlineOccurences() {
        return phpRawInlineExpressionLocations;
    }

    public Set<OffsetRange> getPhpOutputOccurences() {
        return phpOutputExpressionLocations;
    }

    public Set<OffsetRange> getPhpForeachOccurences() {
        return phpForeachExpressionLocations;
    }
}
