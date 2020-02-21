/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
