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

package org.netbeans.lib.lexer.token;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.WrapTokenId;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 * Token with associated properties. It may also act as a token part but without
 * a reference to a complete token e.g. suitable for java's incomplete block comment.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class PropertyToken<T extends TokenId> extends DefaultToken<T> {

    private final TokenPropertyProvider<T> propertyProvider; // 28 bytes (24-super + 4)
    
    public PropertyToken(WrapTokenId<T> wid, int length, TokenPropertyProvider<T> propertyProvider, PartType partType) {
        super(wid, length);
        assert (partType != null);
        this.propertyProvider = (propertyProvider != null)
                ? PartTypePropertyProvider.createDelegating(partType, propertyProvider)
                : PartTypePropertyProvider.<T>get(partType);
    }

    @Override
    public boolean hasProperties() {
        return (propertyProvider != null);
    }

    @Override
    public Object getProperty(Object key) {
        return (propertyProvider != null) ? propertyProvider.getValue(this, key) : null;
    }

    @Override
    public PartType partType() {
        return (PartType) getProperty(PartType.class);
    }

    @Override
    protected String dumpInfoTokenType() {
        return "ProT"; // NOI18N
    }

}
