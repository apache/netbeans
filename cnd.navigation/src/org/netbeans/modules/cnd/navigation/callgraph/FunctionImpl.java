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
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.callgraph.api.Function;
import org.netbeans.modules.cnd.callgraph.api.ui.CallGraphPreferences;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.text.PositionBounds;
import org.openide.util.NbBundle;

public class FunctionImpl implements Function {

    private static final Map<CsmDeclaration.Kind, CsmDeclaration.Kind> preferredIcons = new HashMap<CsmDeclaration.Kind, CsmDeclaration.Kind>();

    static {
        preferredIcons.put(CsmDeclaration.Kind.FUNCTION, CsmDeclaration.Kind.FUNCTION_DEFINITION);
        preferredIcons.put(CsmDeclaration.Kind.FUNCTION_FRIEND, CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION);
    }

    private final CsmFunction function;
    private String htmlDisplayName = ""; // NOI18N
    private String scopeName = null; // NOI18N
    private final CsmFunction cachedFunctionDefinition;
    private final CsmFunction cachedFunctionDeclaration;
    private final boolean isVirtual;
    private final PositionBounds positions;

    public FunctionImpl(CsmFunction function) {
        this.function = function;
        cachedFunctionDefinition = initDefinition();
        cachedFunctionDeclaration = initDeclaration();
        positions = CsmUtilities.createPositionBounds(cachedFunctionDefinition);
        isVirtual = initVirtual();
    }

    public CsmFunction getDeclaration() {
        return cachedFunctionDeclaration;
    }

    private CsmFunction initDeclaration() {
        if (CsmKindUtilities.isFunctionDefinition(function)) {
            CsmFunction f = ((CsmFunctionDefinition) function).getDeclaration();
            if (f != null) {
                return f;
            }
        }
        return function;
    }

    public CsmFunction getDefinition() {
        return cachedFunctionDefinition;
    }

    private CsmFunction initDefinition() {
        if (CsmKindUtilities.isFunctionDeclaration(function)) {
            CsmFunction f = function.getDefinition();
            if (f != null) {
                return f;
            }
        }
        return function;
    }
    
    @Override
    public String getName() {
        return function.getName().toString();
    }

    @Override
    public String getScopeName() {
        if (scopeName == null) {
            scopeName = "";
            try {
                CsmFunction f = getDeclaration();
                if (CsmKindUtilities.isClassMember(f)) {
                    CsmClass cls = ((CsmMember) f).getContainingClass();
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

    @Override
    public String getHtmlDisplayName() {
        if (htmlDisplayName.length() == 0) {
            htmlDisplayName = createHtmlDisplayName();
        }
        return htmlDisplayName;
    }

    @Override
    public boolean isVurtual() {
        return isVirtual;
    }

    private boolean initVirtual() {
        try {
            CsmFunction f = getDeclaration();
            if (CsmKindUtilities.isClassMember(f)) {
                CsmClass cls = ((CsmMember) f).getContainingClass();
                if (cls != null && cls.getName().length() > 0) {
                    return CsmKindUtilities.isMethod(f) && CsmVirtualInfoQuery.getDefault().isVirtual((CsmMethod)f);
                }
            }
        } catch (AssertionError ex) {
            ex.printStackTrace(System.err);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        return false;
    }

    private String createHtmlDisplayName() {
        String displayName;
        if (CallGraphPreferences.isShowParameters()) {
            displayName = function.getSignature().toString();
        } else {
            displayName = function.getName().toString();
        }
        displayName = CsmDisplayUtilities.htmlize(displayName);
        if (scopeName == null) {
            scopeName = "";
        }
        try {
            CsmFunction f = getDeclaration();
            if (CsmKindUtilities.isClassMember(f)) {
                CsmClass cls = ((CsmMember) f).getContainingClass();
                if (cls != null && cls.getName().length() > 0) {
                    String name = CsmDisplayUtilities.htmlize(cls.getName().toString()); // NOI18N
                    String in = NbBundle.getMessage(CallImpl.class, "LBL_inClass"); // NOI18N
                    if (isVurtual()){
                        displayName ="<i>"+displayName+"</i>"; // NOI18N
                    }
                    scopeName = cls.getName().toString()+"::"; // NOI18N
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
        String scope = getScopeName();
        return scope+function.getSignature().toString();
    }

    @Override
    public Image getIcon() {
        try {
            return CsmImageLoader.getImage(getDefinition(), preferredIcons);
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
        CsmFunction f = getDefinition();
        if (f != null) {
            if (obj instanceof FunctionImpl) {
                return f.equals(((FunctionImpl) obj).getDefinition());
            }
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        CsmFunction f = getDefinition();
        if (f != null) {
            return f.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
    
    @Override
    public Kind kind() {
        return Kind.FUNCTION;
    }
}
