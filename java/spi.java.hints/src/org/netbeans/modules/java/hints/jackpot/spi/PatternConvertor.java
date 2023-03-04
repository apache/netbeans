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

package org.netbeans.modules.java.hints.jackpot.spi;

import org.netbeans.modules.java.hints.providers.spi.HintDescriptionFactory;
import org.netbeans.spi.java.hints.HintContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.hints.providers.spi.HintDescription.Worker;
import org.netbeans.modules.java.hints.providers.spi.Trigger.PatternDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.openide.util.Lookup;

/**XXX: big hack?
 *
 * @author lahvac
 */
public abstract class PatternConvertor {

    protected abstract @CheckForNull Iterable<? extends HintDescription> parseString(@NonNull String code);

    public static @CheckForNull Iterable<? extends HintDescription> create(@NonNull String code) {
        Collection<String> patterns = new ArrayList<>();
        
        //XXX:
        if (code.contains(";;")) {
            PatternConvertor c = Lookup.getDefault().lookup(PatternConvertor.class);

            if (c != null) {
                return c.parseString(code);
            }

            for (String s : code.split(";;")) {
                s = s.trim();
                if (s.isEmpty()) {
                    continue;
                }
                
                patterns.add(s);
            }
        } else {
            patterns.add(code);
        }

        Collection<HintDescription> result = new ArrayList<>(patterns.size());

        for (String pattern : patterns) {
            PatternDescription pd = PatternDescription.create(pattern, Collections.<String, String>emptyMap());

            HintDescription desc = HintDescriptionFactory.create()
    //                                                     .setDisplayName("Pattern Matches")
                                                         .setTrigger(pd)
                                                         .setWorker(new WorkerImpl())
                                                         .produce();
            
            result.add(desc);
        }

        return result;
    }

    private static final class WorkerImpl implements Worker {

        @Override
        public Collection<? extends ErrorDescription> createErrors(HintContext ctx) {
            ErrorDescription ed = ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), "Found pattern occurrence");

            return Collections.singleton(ed);
        }
        
    }
}
