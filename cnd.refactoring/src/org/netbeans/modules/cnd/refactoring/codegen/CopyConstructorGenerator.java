/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.refactoring.codegen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.GeneratorUtils;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 */
public class CopyConstructorGenerator implements CodeGenerator {

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CsmContext path = context.lookup(CsmContext.class);
            if (component == null || path == null) {
                return ret;
            }
            CsmClass typeElement = path.getEnclosingClass();
            if (typeElement == null) {
                return ret;
            }
            List<CsmObject> pathList = path.getPath();
            CsmObject last = pathList.get(pathList.size()-1);
            if (!(CsmKindUtilities.isClass(last) || CsmKindUtilities.isField(last))) {
                return ret;
            }
            final List<Pair<CsmField,ConstructorGenerator.Inited>> fields = new ArrayList<>();
            final List<CsmConstructor> constructors = new ArrayList<>();
            final Map<CsmClass,CsmConstructor> inheritedConstructors = new HashMap<>();
            CsmCacheManager.enter();
            try {
                GeneratorUtils.scanForFieldsAndConstructors(typeElement, fields, constructors);
                for(CsmConstructor c : constructors) {
                    if (isCopyConstructor(typeElement, c)) {
                        return ret;
                    }
                }
                // check base class
                for (CsmInheritance csmInheritance : typeElement.getBaseClasses()) {
                    CsmClass baseClass = CsmInheritanceUtilities.getCsmClass(csmInheritance);
                    if (baseClass != null) {
                        List<CsmConstructor> list = new ArrayList<>();
                        for (CsmMember member : baseClass.getMembers()) {
                            if (CsmKindUtilities.isConstructor(member) &&
                                CsmInheritanceUtilities.matchVisibility(member, CsmVisibility.PROTECTED) &&
                                isCopyConstructor(baseClass, (CsmConstructor) member)) {
                                inheritedConstructors.put(baseClass, (CsmConstructor)member);
                                break;
                            }
                        }
                    }
                }
            } finally {
                CsmCacheManager.leave();
            }
            int mayAndMust = inheritedConstructors.size();
            for (Pair<CsmField,ConstructorGenerator.Inited> variableElement : fields) {
                switch(variableElement.second()) {
                    case may:
                    case must:
                        mayAndMust++;
                        break;
                }
            }
            
            if (mayAndMust > 0) {
                ret.add(new CopyConstructorGenerator(component, path, typeElement, fields, inheritedConstructors));
            }
            return ret;
        }
        
        private boolean isCopyConstructor(CsmClass cls, CsmConstructor constructor) {
            Collection<CsmParameter> parameters = constructor.getParameters();
            if (parameters.size() == 1) {
                CsmParameter p = parameters.iterator().next();
                CsmType paramType = p.getType();
                if (paramType.isReference()) {
                    if (cls.equals(paramType.getClassifier())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    private final JTextComponent component;
    private final CsmContext contextPath;
    private final CsmClass type;
    private final List<Pair<CsmField,ConstructorGenerator.Inited>> fields;
    private final Map<CsmClass,CsmConstructor> inheritedConstructors;

    public CopyConstructorGenerator(JTextComponent component, CsmContext contextPath, CsmClass type, List<Pair<CsmField,ConstructorGenerator.Inited>> fields, Map<CsmClass,CsmConstructor> inheritedConstructors) {
        this.component = component;
        this.contextPath = contextPath;
        this.type = type;
        this.fields = fields;
        this.inheritedConstructors = inheritedConstructors;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ConstructorGenerator.class, "LBL_copy_constructor"); //NOI18N
    }

    @Override
    public void invoke() {
        UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, CsmRefactoringUtils.GENERATE_TRACKING, "CONSTRUCTOR"); // NOI18N
        ArrayList<CsmField> fld = new ArrayList<>();
        for (Pair<CsmField,ConstructorGenerator.Inited> variableElement : fields) {
            switch(variableElement.second()) {
                case may:
                case must:
                    fld.add(variableElement.first());
            }
        }
        ArrayList<CsmConstructor> constructors = new ArrayList<>(inheritedConstructors.values());
        GeneratorUtils.generateCopyConstructor(contextPath,  type, constructors, fld);
    }
}
