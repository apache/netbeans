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

package org.netbeans.modules.java.hints.perf;

import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint.Options;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class InitialCapacity {

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.InitialCapacity.collections", description = "#DESC_org.netbeans.modules.java.hints.perf.InitialCapacity.collections", category="performance", enabled=false, suppressWarnings="CollectionWithoutInitialCapacity", options=Options.QUERY)
    @TriggerPatterns({
        @TriggerPattern(value="new java.util.ArrayDeque()"),
        @TriggerPattern(value="new java.util.ArrayDeque<$T$>()"),
        @TriggerPattern(value="new java.util.ArrayList()"),
        @TriggerPattern(value="new java.util.ArrayList<$T$>()"),
        @TriggerPattern(value="new java.util.BitSet()"),
        @TriggerPattern(value="new java.util.BitSet<$T$>()"),
        @TriggerPattern(value="new java.util.concurrent.ConcurrentHashMap()"),
        @TriggerPattern(value="new java.util.concurrent.ConcurrentHashMap<$T$>()"),
        @TriggerPattern(value="new java.util.HashMap()"),
        @TriggerPattern(value="new java.util.HashMap<$T$>()"),
        @TriggerPattern(value="new java.util.HashSet()"),
        @TriggerPattern(value="new java.util.HashSet<$T$>()"),
        @TriggerPattern(value="new java.util.Hashtable()"),
        @TriggerPattern(value="new java.util.Hashtable<$T$>()"),
        @TriggerPattern(value="new java.util.IdentityHashMap()"),
        @TriggerPattern(value="new java.util.IdentityHashMap<$T$>()"),
        @TriggerPattern(value="new java.util.LinkedHashMap()"),
        @TriggerPattern(value="new java.util.LinkedHashMap<$T$>()"),
        @TriggerPattern(value="new java.util.LinkedHashSet()"),
        @TriggerPattern(value="new java.util.LinkedHashSet<$T$>()"),
        @TriggerPattern(value="new java.util.Vector()"),
        @TriggerPattern(value="new java.util.Vector<$T$>()")
    })
    public static ErrorDescription collections(HintContext ctx) {
        String displayName = NbBundle.getMessage(InitialCapacity.class, "ERR_InitialCapacity_collections");
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.InitialCapacity.stringBuilder", description = "#DESC_org.netbeans.modules.java.hints.perf.InitialCapacity.stringBuilder", category="performance", enabled=false, suppressWarnings="StringBufferWithoutInitialCapacity", options=Options.QUERY)
    @TriggerPatterns({
        @TriggerPattern(value="new java.lang.StringBuffer()"),
        @TriggerPattern(value="new java.lang.StringBuilder()")
    })
    public static ErrorDescription stringBuilder(HintContext ctx) {
        String displayName = NbBundle.getMessage(InitialCapacity.class, "ERR_InitialCapacity_stringBuilder");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }
}
