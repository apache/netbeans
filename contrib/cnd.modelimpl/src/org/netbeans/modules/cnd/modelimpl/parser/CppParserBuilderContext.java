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
package org.netbeans.modules.cnd.modelimpl.parser;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl.ClassBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.CsmObjectBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.EnumImpl.EnumBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceDefinitionImpl.NamespaceBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.SimpleDeclarationBuilder;

/**
 */
public class CppParserBuilderContext {
    
    List<CsmObjectBuilder> builders = new ArrayList<>();
 
    public void push(CsmObjectBuilder builder) {
        builders.add(builder);
    }

    public void pop() {
        builders.remove(builders.size() - 1);
    }

    public CsmObjectBuilder top() {
        if(!builders.isEmpty()) {
            return builders.get(builders.size() - 1);
        } else {
            return null;
        }
    }

    public CsmObjectBuilder top(int i) {
        if(!builders.isEmpty() && builders.size() > i) {
            return builders.get(builders.size() - 1 - i);
        } else {
            return null;
        }
    }
    
    public EnumBuilder getEnumBuilder() {
        CsmObjectBuilder builder = top();
        assert builder instanceof EnumBuilder;
        EnumBuilder enumBuilder = (EnumBuilder)builder;        
        return enumBuilder;
    }

    public ClassBuilder getClassBuilder() {
        CsmObjectBuilder builder = top();
        assert builder instanceof ClassBuilder : "top " + top();
        ClassBuilder classBuilder = (ClassBuilder)builder;        
        return classBuilder;
    }
    
    public NamespaceBuilder getNamespaceBuilder() {
        CsmObjectBuilder builder = top();
        assert builder instanceof NamespaceBuilder;
        NamespaceBuilder nsBuilder = (NamespaceBuilder)builder;        
        return nsBuilder;
    }

    public NamespaceBuilder getNamespaceBuilderIfExist() {
        CsmObjectBuilder builder = top();
        if(builder instanceof NamespaceBuilder) {
            NamespaceBuilder nsBuilder = (NamespaceBuilder)builder;        
            return nsBuilder;
        }
        return null;
    }

    public SimpleDeclarationBuilder getSimpleDeclarationBuilderIfExist() {
        CsmObjectBuilder builder = top();
        if(builder instanceof SimpleDeclarationBuilder) {
            SimpleDeclarationBuilder sdBuilder = (SimpleDeclarationBuilder)builder;        
            return sdBuilder;
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        StringBuilder prefix = new StringBuilder();
        for (int i = builders.size()-1; i >= 0; i--) {
            CsmObjectBuilder bldr = builders.get(i);
            sb.append("\n"); //NOI18N
            sb.append(prefix);
            sb.append("->"); //NOI18N
            sb.append(bldr);
            prefix.append("  "); //NOI18N
        }
        return sb.toString();
    }
}
