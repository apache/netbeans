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
package org.netbeans.modules.cnd.navigation.callgraph;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.callgraph.api.Function;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.text.PositionBounds;
import org.openide.util.NbBundle;

/**
 *
 */
public class VariableImpl implements Function {
    private static final Map<CsmDeclaration.Kind, CsmDeclaration.Kind> preferredIcons = new HashMap<CsmDeclaration.Kind, CsmDeclaration.Kind>();
    
    static {
        preferredIcons.put(CsmDeclaration.Kind.VARIABLE, CsmDeclaration.Kind.VARIABLE_DEFINITION);
    }
    
    private final CsmOffsetableDeclaration variable;
    private String htmlDisplayName = ""; // NOI18N
    private String scopeName = null; // NOI18N
    private final PositionBounds positions;

    public VariableImpl(CsmOffsetableDeclaration variable) {
        this.variable = variable;
        positions = CsmUtilities.createPositionBounds(variable);
    }
    
    @Override
    public String getName() {
        return variable.getName().toString();
    }
    
    @Override
    public String getScopeName() {
        if (scopeName == null) {
            scopeName = "";
            try {
                if (CsmKindUtilities.isField(variable)) {
                    CsmClass cls = ((CsmField) variable).getContainingClass();
                    if (cls != null && cls.getName().length() > 0) {
                        scopeName = cls.getName().toString()+"::"; // NOI18N
                    }
                }
            } catch (AssertionError ex) {
                ex.printStackTrace(System.err);
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            }
        }
        return scopeName;
    }
    
    public CsmOffsetableDeclaration getVariable() {
        return variable;
    }
    
    @Override
    public String getHtmlDisplayName() {
        if (htmlDisplayName.length() == 0) {
            htmlDisplayName = createHtmlDisplayName();
        }
        return htmlDisplayName;
    }
    
    private String createHtmlDisplayName() {
        String displayName = variable.getName().toString();
        
        try {
            if (CsmKindUtilities.isField(variable)) {
                    CsmClass cls = ((CsmField) variable).getContainingClass();
                    if (cls != null && cls.getName().length() > 0) {
                        String name = CsmDisplayUtilities.htmlize(cls.getName().toString());
                        String in = NbBundle.getMessage(CallImpl.class, "LBL_inClass"); // NOI18N
                        return displayName + "<font color=\'!textInactiveText\'>  " + in + " " + name; // NOI18N
                    }
                }
        } catch (AssertionError ex) {
            ex.printStackTrace(System.err);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        
        return displayName;
    }
    
    @Override
    public String getDescription() {
        return getScopeName()+getName();
    }
    
    @Override
    public Image getIcon() {
        try {
            return CsmImageLoader.getImage(variable, preferredIcons);
        } catch (AssertionError ex) {
            ex.printStackTrace(System.err);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        return null;
    }
    
    @Override
    public void open() {
        CsmUtilities.openSource(positions);
    }
   
    @Override
    public boolean equals(Object obj) {
        if (variable != null) {
            if (obj instanceof VariableImpl) {
                return variable.equals(((VariableImpl) obj).getVariable());
            }
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        if (variable != null) {
            return variable.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
    
    @Override
    public boolean isVurtual() {
        return false;
    }
    
    @Override
    public Kind kind() {
        if (CsmKindUtilities.isVariable(variable)) {
            if (CsmKindUtilities.isFunctionPointerType(((CsmVariable)variable).getType())) {
                return Kind.FUNCTION_POINTER;
            }
        }
        return Kind.VARIABLE;
    }
}
