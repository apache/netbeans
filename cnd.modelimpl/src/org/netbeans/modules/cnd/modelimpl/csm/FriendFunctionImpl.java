/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 *
 */
public final class FriendFunctionImpl extends FunctionImpl<CsmFriendFunction> implements CsmFriendFunction {
    private final CsmUID<CsmClass> friendClassUID;
    private SpecializationDescriptor specializationDesctiptor;
    
    protected FriendFunctionImpl(CharSequence name, CharSequence rawName, CsmScope scope, CsmClass cls, boolean _static,  FunctionImpl.CV_RL _const, CsmFile file, int startOffset, int endOffset, boolean global) {
        super(name, rawName, scope, _static, _const, file, startOffset, endOffset, global);
        friendClassUID = UIDs.get(cls);
    }
    
    public static FriendFunctionImpl create(AST ast, final CsmFile file, FileContent fileContent, CsmClass cls, CsmScope scope, boolean global) throws AstRendererException {
        
        int startOffset = getStartOffset(ast);
        int endOffset = getEndOffset(ast);
        
        NameHolder nameHolder = NameHolder.createFunctionName(ast);
        CharSequence name = QualifiedNameCache.getManager().getString(nameHolder.getName());
        if (name.length() == 0) {
            AstRendererException.throwAstRendererException((FileImpl) file, ast, startOffset, "Empty function name."); // NOI18N
        }
        CharSequence rawName = initRawName(ast);
        
        boolean _static = AstRenderer.FunctionRenderer.isStatic(ast, file, fileContent, name);
        FunctionImpl.CV_RL _const = AstRenderer.FunctionRenderer.isConst(ast);

        scope = AstRenderer.FunctionRenderer.getScope(scope, file, _static, false);

        FriendFunctionImpl friendFunctionImpl = new FriendFunctionImpl(name, rawName, scope, cls, _static, _const, file, startOffset, endOffset, global);        
        temporaryRepositoryRegistration(ast, global, friendFunctionImpl);
        
        StringBuilder clsTemplateSuffix = new StringBuilder();
        TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, friendFunctionImpl, clsTemplateSuffix, global);
        CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);
        
        friendFunctionImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
        friendFunctionImpl.setSpecializationDesctiptor(SpecializationDescriptor.createIfNeeded(ast, file, scope, global));
        friendFunctionImpl.setReturnType(AstRenderer.FunctionRenderer.createReturnType(ast, friendFunctionImpl, file));
        friendFunctionImpl.setParameters(AstRenderer.FunctionRenderer.createParameters(ast, friendFunctionImpl, file, fileContent), 
                AstRenderer.FunctionRenderer.isVoidParameter(ast));
        
        postObjectCreateRegistration(global, friendFunctionImpl);
        nameHolder.addReference(fileContent, friendFunctionImpl);
        return friendFunctionImpl;
    }
    
    protected void setSpecializationDesctiptor(SpecializationDescriptor specializationDesctiptor) {
        this.specializationDesctiptor = specializationDesctiptor;        
    }
    
    
    @Override
    public CsmFunction getReferencedFunction() {
        CsmFunction fun = this;
        if (CsmKindUtilities.isFunctionDeclaration(this)){
            fun = getDefinition();
            if (fun == null){
                fun = this;
            }
        }
        return fun;
    }
    
    @Override
    public CsmClass getContainingClass() {
        CsmClass cls = null;
        cls = UIDCsmConverter.UIDtoClass(friendClassUID);
        assert (friendClassUID != null) : "null object for UID " + friendClassUID;
        return cls;
    }

    public List<CsmSpecializationParameter> getSpecializationParameters() {
        return (specializationDesctiptor != null) ? specializationDesctiptor.getSpecializationParameters() : Collections.<CsmSpecializationParameter>emptyList();
    }
    
    @Override
    public Kind getKind() {
        return Kind.FUNCTION_FRIEND;
    }
    
    public static class FriendFunctionBuilder extends FunctionBuilder {

        public FriendFunctionBuilder(SimpleDeclarationBuilder builder) {
            super(builder);
        }
    
        @Override
        public CsmScope getScope() {
            CsmScope scope = super.getScope();
            while (CsmKindUtilities.isClass(scope)) {
               CsmScope newScope = ((CsmClass)scope).getScope(); 
               if (newScope != null) {
                   scope = newScope;
               } else {
                   break;
               }
            }
            return AstRenderer.FunctionRenderer.getScope(scope, getFile(), isStatic(), false);
        } 
        
        public CsmClass getCls() {
            return (CsmClass) super.getScope();
        } 
        
        @Override
        public FunctionImpl create() {
            FriendFunctionImpl fun = new FriendFunctionImpl(getName(), getRawName(), getScope(), getCls(), isStatic(), FunctionImpl.CV_RL.isConst(isConst()), getFile(), getStartOffset(), getEndOffset(), true);
            init(fun);
            return fun;
        }
        
    }          

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        UIDObjectFactory.getDefaultFactory().writeUID(friendClassUID, output);
        PersistentUtils.writeSpecializationDescriptor(specializationDesctiptor, output);
    }
    
    public FriendFunctionImpl(RepositoryDataInput input) throws IOException {
        super(input);
        friendClassUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        this.specializationDesctiptor = PersistentUtils.readSpecializationDescriptor(input);
    }       
}
