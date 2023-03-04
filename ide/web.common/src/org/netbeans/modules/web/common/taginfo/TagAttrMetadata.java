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

package org.netbeans.modules.web.common.taginfo;

import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class TagAttrMetadata {
    private String name;
    private boolean optional;
    private Collection<AttrValueType> valueTypes;
    private String mimeType = "text/plain"; //NOI18N

    public TagAttrMetadata(String name, String mimeType){
        this(name, (AttrValueType)null);
        this.mimeType = mimeType;
    }

    public TagAttrMetadata(String name, AttrValueType valueType) {
        this(name, true, valueType);
    }

    public TagAttrMetadata(String name, boolean optional, AttrValueType valueType) {
        this(name, optional, Collections.singleton(valueType), null);
    }

    public TagAttrMetadata(String name, Collection<AttrValueType> valueTypes, String mimeType) {
        this(name, true, valueTypes, mimeType);
    }

    public TagAttrMetadata(String name, boolean optional, Collection<AttrValueType> valueTypes, String mimeType) {
        this.mimeType = mimeType;
        this.name = name;
        this.optional = optional;
        this.valueTypes = valueTypes;
    }

    public String getName() {
        return name;
    }

    public boolean isOptional() {
        return optional;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Collection<AttrValueType> getValueTypes() {
        return valueTypes;
    }
}
