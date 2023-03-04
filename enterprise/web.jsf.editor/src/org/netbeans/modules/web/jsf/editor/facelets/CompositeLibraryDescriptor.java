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
package org.netbeans.modules.web.jsf.editor.facelets;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.modules.web.jsfapi.api.Tag;

/**
 * Composite library descriptor. The first library has the precedence.
 *
 * @author marekfukala
 */
public class CompositeLibraryDescriptor implements LibraryDescriptor {

    private LibraryDescriptor first, second;

    public CompositeLibraryDescriptor(LibraryDescriptor first, LibraryDescriptor second) {
        this.first = first;
        this.second = second;
        
        if(first.getNamespace() != null 
                && second.getNamespace() != null 
                && !first.getNamespace().equals(second.getNamespace())) {
            //both must have the same namespace if there's any
            throw new IllegalArgumentException(String.format("Both libraries must declare the same namespace if there's any. "
                    + "Currently the first one declares %s and the second %s!", first.getNamespace(), second.getNamespace())); //NOI18N
        }
        
    }
    
    @Override
    public String getNamespace() {
        return first.getNamespace() != null ? first.getNamespace() : second.getNamespace();
    }
    
    @Override
    public String getPrefix() {
        return first.getPrefix() != null ? first.getPrefix() : second.getPrefix();
    }

     @Override
    public Map<String, Tag> getTags() {
        //merge
        Map<String, Tag> s = first.getTags();
        Map<String, Tag> t = second.getTags();

        Map<String, Tag> result = new HashMap<>();

        Collection<String> allTagNames = new HashSet<>();
        allTagNames.addAll(s.keySet());
        allTagNames.addAll(t.keySet());
        for(String tagName : allTagNames) {
            result.put(tagName, new ProxyTag(s.get(tagName), t.get(tagName)));
        }

        return result;
    }

    
}
