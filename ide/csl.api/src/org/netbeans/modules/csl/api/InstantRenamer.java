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

import java.util.Set;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.spi.ParserResult;


/**
 * Provide an implementation that will help with instant renaming (in place refactoring)
 *
 * @author <a href="mailto:tor.norbye@sun.com">Tor Norbye</a>
 */
public interface InstantRenamer {
    /**
     * Check whether instant-renaming is allowed for the symbol under the given caret offset,
     * and return true iff it is.
     * @param info The compilation context to be used for parse info
     * @param caretOffset The specific caret location we want to check
     * @param explanationRetValue An array of length 1 whose first element can be set
     *   to a short description string (explaining why renaming is not allowed) which
     *   may be displayed to the user.
     */
    boolean isRenameAllowed(@NonNull ParserResult info, int caretOffset, @NullAllowed String[] explanationRetValue);

    /**
     * Return a Set of regions that should be renamed if the element under the caret offset is
     * renamed.
     */
    @CheckForNull
    Set<OffsetRange> getRenameRegions(@NonNull ParserResult info, int caretOffset);
}
