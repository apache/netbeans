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

package org.netbeans.api.lexer;

import java.util.List;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author vita
 */
public class CustomTokenClassTest extends NbTestCase {

    public CustomTokenClassTest(String name) {
        super(name);
    }

    public void testCustomTokenClass() throws Exception {
        try {
            Token token = new CustomToken();
            fail("IllegalStateException expected from constructor of Token class.");
        } catch (IllegalStateException e) {
            // Expected ISE from Token's constructor.
        }
    }
    
    private static final class CustomToken<T extends TokenId> extends Token<T> {
        
        public T id() {
            return null;
        }

        public CharSequence text() {
            return null;
        }

        public boolean isCustomText() {
            return false;
        }

        public int length() {
            return 0;
        }

        public int offset(TokenHierarchy<?> tokenHierarchy) {
            return 0;
        }

        public boolean isFlyweight() {
            return false;
        }

        public boolean hasProperties() {
            return false;
        }

        public Object getProperty(Object key) {
            return false;
        }
        
        public PartType partType() {
            return null;
        }

        public boolean isRemoved() {
            return false;
        }

        public Token<T> joinToken() {
            return null;
        }

        public List<? extends Token<T>> joinedParts() {
            return null;
        }
        
    }

}
