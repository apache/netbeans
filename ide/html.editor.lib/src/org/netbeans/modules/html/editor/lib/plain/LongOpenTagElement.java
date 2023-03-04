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
package org.netbeans.modules.html.editor.lib.plain;

import java.util.List;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;

/**
 * Used for unusually long open tags.
 * 
 * For an example take a look the following bug:
 * https://netbeans.org/bugzilla/show_bug.cgi?id=228101
 * 
 * Note: this class wastes one "short" fied which is unused, 
 * but this will not hurt as instances of this class will be pretty rare.
 * 
 * @author marekfukala
 */
public class LongOpenTagElement extends OpenTagElement {

    private int length;
    
    public LongOpenTagElement(CharSequence document, int from, int length, byte nameLen, List<Attribute> attribs, boolean isEmpty) {
        super(document, from, (short)0, nameLen, attribs, isEmpty);
        assert length >=0 : "element length must be positive!"; //NOI18N
        this.length = length;
    }

    @Override
    public int to() {
        return from() + length;
    }
    
}
