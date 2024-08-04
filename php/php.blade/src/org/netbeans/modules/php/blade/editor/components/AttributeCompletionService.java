/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.blade.editor.components;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.php.blade.editor.components.annotation.Attribute;
import org.netbeans.modules.php.blade.editor.components.annotation.AttributeRegister;

/**
 *
 * @author bhaidu
 */
@AttributeRegister({
    @Attribute(name = "class"), // NOI18N
    @Attribute(name = "id"), // NOI18N
    @Attribute(name = "title"), // NOI18N
})
public class AttributeCompletionService {

    public Collection<String> queryComponents(String prefix) {
        Collection<String> results = new ArrayList<>();

        for (Attribute attrName : getAttributes()) {
            if (attrName.name().startsWith(prefix)){
                results.add(attrName.name());
            }
        }

        return results;
    }

    public Attribute[] getAttributes() {
        AttributeRegister attributeRegister = this.getClass().getAnnotation(AttributeRegister.class);
        return attributeRegister.value();
    }
}
