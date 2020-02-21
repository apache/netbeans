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
package org.netbeans.modules.cnd.completion.templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessorFactory;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.completion.csm.CsmContext;
import org.netbeans.modules.cnd.completion.csm.CsmContextUtilities;
import org.netbeans.modules.cnd.completion.csm.CsmDeclarationResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.NbBundle;

/**
 *
 *
 */
public class CppCodeTemplateProcessor implements CodeTemplateProcessor {

    //
    public static final String ARRAY = "array"; //NOI18N
    public static final String ITERABLE = "iterable"; //NOI18N
    public static final String TYPE = "type"; //NOI18N
    public static final String CAST = "cast"; //NOI18N
    public static final String ITERABLE_ELEMENT_TYPE = "iterableElementType"; //NOI18N
    public static final String NEW_VAR_NAME = "newVarName"; //NOI18N
    public static final String RIGHT_SIDE_TYPE = "rightSideType"; //NOI18N

    private final CodeTemplateInsertRequest request;
    private CsmClass enclClass = null;
    private List<CsmVariable> locals = null;
    private static final String[] iterableTypes = new String[] {"list","map","vector"}; //NOI18N

    private CppCodeTemplateProcessor(CodeTemplateInsertRequest request) {
        this.request = request;
        for (CodeTemplateParameter param : request.getMasterParameters()) {
            if (CodeTemplateParameter.SELECTION_PARAMETER_NAME.equals(param.getName())) {
                initParsing();
                return;
            }
        }
    }

    @Override
    public void updateDefaultValues() {
        for (Object p : request.getMasterParameters()) {
            CodeTemplateParameter param = (CodeTemplateParameter)p;
            String value = getProposedValue(param);
            if (value != null && !value.equals(param.getValue())) {
                param.setValue(value);
            }
        }
    }

    @Override
    public void parameterValueChanged(CodeTemplateParameter masterParameter, boolean typingChange) {
        if (typingChange) {
            for (Object p : request.getMasterParameters()) {
                CodeTemplateParameter param = (CodeTemplateParameter)p;
                if (!param.isUserModified()) {
                    String value = getProposedValue(param);
                    if (value != null && !value.equals(param.getValue())) {
                        param.setValue(value);
                    }
                }
            }
        }
    }

    @Override
    public void release() {
    }

    private void initParsing() {
        JTextComponent c = request.getComponent();
        final int offset = c.getSelectionStart();
        final Document doc = c.getDocument();
        final AtomicBoolean cancel = new AtomicBoolean();
        ProgressUtils.runOffEventDispatchThread(new Runnable() {
            @Override
            public void run() {
                if (cancel.get()) {
                    return;
                }
                locals = new ArrayList<CsmVariable>();
                CsmFile file = CsmUtilities.getCsmFile(doc, false, false);
                if (file == null) {
                    return;
                }
                CsmContext context = new CsmContext(file, offset);
                CsmObject lastObj = CsmDeclarationResolver.findInnerFileObject(file, offset, context, null);
                for(CsmDeclaration var : CsmContextUtilities.findFunctionLocalVariables(context)) {
                    if (CsmKindUtilities.isVariable(var) ) {
                        locals.add((CsmVariable) var);
                    }
                }
                CsmContextUtilities.isInFunctionBody(context, offset);
                for(CsmDeclaration var : CsmContextUtilities.findFileLocalVariables(context)) {
                    if (CsmKindUtilities.isVariable(var) ) {
                        locals.add((CsmVariable) var);
                    }
                }
                CsmNamespace namespace = CsmContextUtilities.getNamespace(context);
                if (namespace != null) {
                    for(CsmDeclaration var : namespace.getDeclarations()) {
                        if (CsmKindUtilities.isVariable(var) ) {
                            locals.add((CsmVariable) var);
                        }
                    }
                }
                enclClass = CsmContextUtilities.getClass(context, true, false);
                if (enclClass != null) {
                    for(CsmDeclaration var : enclClass.getMembers()) {
                        if (CsmKindUtilities.isVariable(var) ) {
                            locals.add((CsmVariable) var);
                        }
                    }
                }
            }
        }, NbBundle.getMessage(CppCodeTemplateProcessor.class, "CPP-init"), cancel, false); //NOI18N
    }

    private String getProposedValue(CodeTemplateParameter param) {
        String name = null;
        for (Map.Entry<String, String> entry : param.getHints().entrySet()) {
            if (ARRAY.equals(entry.getKey())) {
                for(CsmVariable var : locals) {
                    if (isArray(var)) {
                        return var.getName().toString();
                    }
                }
            } else if (ITERABLE.equals(entry.getKey())) {
                for(CsmVariable var : locals) {
                    if (isIterable(var)) {
                        return var.getName().toString();
                    }
                }
            //} else if (TYPE.equals(entry.getKey())) {
            //} else if (ITERABLE_ELEMENT_TYPE.equals(entry.getKey())) {
            //} else if (RIGHT_SIDE_TYPE.equals(entry.getKey())) {
            //} else if (CAST.equals(entry.getKey())) {
            //} else if (NEW_VAR_NAME.equals(entry.getKey())) {
            }
        }
        return name;
    }

    private boolean isIterable(CsmVariable var) {
        CsmType type = var.getType();
        if (type != null) {
            CharSequence text = type.getText();
            for(String s : iterableTypes) {
                if (CharSequenceUtils.startsWith(text, s) ||
                    CharSequenceUtils.startsWith(text, "std::"+s)) { //NOI18N
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isArray(CsmVariable var) {
        CsmType type = var.getType();
        if (type != null) {
            if (type.getArrayDepth() == 1) {
                return true;
            }
        }
        return false;
    }

    public static final class Factory implements CodeTemplateProcessorFactory {

        @Override
        public CodeTemplateProcessor createProcessor(CodeTemplateInsertRequest request) {
            return new CppCodeTemplateProcessor(request);
        }
    }
}
