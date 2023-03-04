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

package org.netbeans.modules.java.hints.declarative.debugging;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintTokenId;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser.HintTextDescription;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintsParser.Result;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
public class HintWrapper {

    public final String spec;
    public final Result res;
    public final HintTextDescription desc;

    private HintWrapper(String spec, Result res, HintTextDescription desc) {
        this.spec = spec;
        this.res = res;
        this.desc = desc;
    }

    public static Collection<? extends HintWrapper> parse(@NonNull FileObject file, String spec) {
        TokenHierarchy<?> h = TokenHierarchy.create(spec, DeclarativeHintTokenId.language());
        TokenSequence<DeclarativeHintTokenId> ts = h.tokenSequence(DeclarativeHintTokenId.language());
        List<HintWrapper> result = new LinkedList<>();
        Result parsed = new DeclarativeHintsParser().parse(file, spec, ts);

        for (HintTextDescription d : parsed.hints) {
            result.add(new HintWrapper(spec, parsed, d));
        }

        return result;
    }

}
