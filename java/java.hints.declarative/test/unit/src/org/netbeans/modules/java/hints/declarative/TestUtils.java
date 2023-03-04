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

package org.netbeans.modules.java.hints.declarative;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;

import static org.junit.Assert.*;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings.GlobalSettingsProvider;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class TestUtils {

    public static void assertNextTokenEquals(TokenSequence<?> ts, TokenId id, String text) {
        assertNextTokenEquals(ts, id, PartType.COMPLETE, text);
    }

    public static void assertNextTokenEquals(TokenSequence<?> ts, TokenId id, PartType pt, String text) {
        assertTrue(ts.moveNext());

        Token<?> t = ts.token();

        assertNotNull(t);
        assertEquals(id, t.id());
        assertEquals(pt, t.partType());
        assertEquals(text, t.text().toString());
    }
    
    @ServiceProvider(service=MimeDataProvider.class)
    public static class HintGlobalPreferencesMimeProviderImpl implements MimeDataProvider {

        private final Lookup L = Lookups.fixed(new GlobalSettingsProvider());
        
        @Override
        public Lookup getLookup(MimePath mimePath) {
            if ("text/x-java".equals(mimePath.getPath())) {
                return L;
            }
            return Lookup.EMPTY;
        }
        
    }

}
