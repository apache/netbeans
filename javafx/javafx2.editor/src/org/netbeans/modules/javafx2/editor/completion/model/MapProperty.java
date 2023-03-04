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
package org.netbeans.modules.javafx2.editor.completion.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Map property. Map is characterized by having unconstrained 
 * property number of keys and values.
 * 
 * @author sdedic
 */
public class MapProperty extends PropertyValue {
    /**
     * Textual contents of values in the map.
     */
    private Map<String, CharSequence> valueContents = Collections.emptyMap();

    MapProperty(String propertyName) {
        super(propertyName);
    }

    void setValues(Map<String, CharSequence> vals) {
        this.valueContents = vals;
    }
    
    public Map<String, CharSequence> getValueMap() {
        return Collections.unmodifiableMap(valueContents);
    }

    @Override
    public void accept(FxNodeVisitor v) {
        v.visitMapProperty(this);
    }
    
    
}
