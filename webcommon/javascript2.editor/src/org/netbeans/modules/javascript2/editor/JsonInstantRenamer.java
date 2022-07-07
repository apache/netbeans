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

import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.navigation.JsonOccurrencesFinder;
import org.netbeans.modules.javascript2.types.spi.ParserResult;

/**
 *
 * @author Tomas Zezula
 */
public final class JsonInstantRenamer implements InstantRenamer {
    public JsonInstantRenamer() {}

    @Override
    public boolean isRenameAllowed(
            @NonNull final org.netbeans.modules.csl.spi.ParserResult info,
            final int caretOffset,
            @NonNull final String[] explanationRetValue) {
        if (JsonOccurrencesFinder.calculateOccurances((ParserResult)info, caretOffset, false).isEmpty()) {
            return false;
        }
        explanationRetValue[0] = "JSON key";
        return true;
    }

    @Override
    public Set<OffsetRange> getRenameRegions(
            @NonNull final org.netbeans.modules.csl.spi.ParserResult info,
            final int caretOffset) {
        return JsonOccurrencesFinder.calculateOccurances((ParserResult)info, caretOffset, false);
    }
}
