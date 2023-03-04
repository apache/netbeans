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

package org.netbeans.modules.csl.api;

import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserResultTask;

/**
 * A CancellableTask which should analyze the given CompilationInfo
 * and produce a set of highlights.
 * 
 * @author Tor Norbye
 */
public abstract class SemanticAnalyzer<T extends Parser.Result> extends ParserResultTask<T> {
    
    /**
     * Return a set of highlights computed by the last call to
     * Note - there are a number of EnumSet constants in the ColoringAttributes
     * class you should use for many of the common combinations of coloring
     * attributes you want.
     * {@link #run}.
     * <p>
     * <b>NOTE</b>: The OffsetRanges should NOT be overlapping! (The unit test
     * infrastructure in GsfTestBase will check for this condition and fail
     * semantic highlighting tests if they violate this constraint. The test
     * is not performed at runtime.)
     */
    public abstract @NonNull Map<OffsetRange, Set<ColoringAttributes>> getHighlights();

    // Not yet implemented:
    /**
     * Provide a custom description of a set of attributes. This may be shown
     * by the IDE as a tooltip when the user hovers over a region with the given
     * attribute set.  Just return null if you want to get the default
     * descriptions (a comma separated list of the attribute names, such as
     * "Unused", or "Unused, Field".
     * (With the offset range information as well as the CompilationInfo you
     * can provide more accurate descriptions for the user if applicable).
     * 
     * @param attributes
     * @return A localized String for the user describing the given attributes,
     *   or null to get the default supplied descriptions.
     */
    //String describe(CompilationInfo info, OffsetRange range, Set<ColoringAttributes> attributes);
}
