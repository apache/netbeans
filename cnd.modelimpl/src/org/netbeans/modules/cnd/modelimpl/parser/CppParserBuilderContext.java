/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
