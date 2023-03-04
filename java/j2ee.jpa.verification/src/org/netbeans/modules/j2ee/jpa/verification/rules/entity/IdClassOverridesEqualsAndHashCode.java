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

package org.netbeans.modules.j2ee.jpa.verification.rules.entity;

import com.sun.source.tree.Tree;
import java.io.IOException;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.IdClass;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.MappedSuperclass;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
@Hint(id = "o.n.m.j2ee.jpa.verification.IdClassOverridesEqualsAndHashCode",
        displayName = "#MSG_IdClassDoesNotOverrideEquals",
        description = "#MSG_IdClassDoesNotOverrideEquals",
        category = "javaee/jpa",
        enabled = true,
        severity = Severity.ERROR,
        suppressWarnings = "IdClassOverridesEqualsAndHashCode")
//@NbBundle.Messages({
//    "IdClassOverridesEqualsAndHashCode.display.name=Verify entity have defined promary key",
//    "IdClassOverridesEqualsAndHashCode.desc=Id is required for entities"})
public class IdClassOverridesEqualsAndHashCode {
    
    @TriggerPattern(value = JPAAnnotations.ID_CLASS)//NOI18N
    public static ErrorDescription apply(HintContext hc){
        
        if (hc.isCanceled() || (hc.getPath().getLeaf().getKind() != Tree.Kind.IDENTIFIER || hc.getPath().getParentPath().getLeaf().getKind() != Tree.Kind.ANNOTATION)) {//NOI18N
            return null;//we pass only if it is an annotation
        }

        final JPAProblemContext ctx = ModelUtils.getOrCreateCachedContext(hc);
        
        if (ctx == null || hc.isCanceled()) {
            return null;
        }
        boolean hasEquals = false;
        boolean hasHashCode = false;
        
        final IdClass[] idclass = {null};
         try {
            MetadataModel<EntityMappingsMetadata> model = ModelUtils.getModel(hc.getInfo().getFileObject());
            model.runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
                @Override
                public Void run(EntityMappingsMetadata metadata) {
                    if(ctx.getModelElement() instanceof Entity) {
                        idclass[0] = ((Entity) ctx.getModelElement()).getIdClass();

                    } else if (ctx.getModelElement() instanceof MappedSuperclass) {
                        idclass[0] = ((MappedSuperclass) ctx.getModelElement()).getIdClass();
                    } 
                    return null;
                }
            });
        } catch (IOException ex) {
        }
       

        
        if(idclass[0] == null) {
            return null;
        }
        String className = idclass[0].getClass2();
        // this may happen when the id class is not (yet) defined
        if (className == null) {
            return null;
        }
        
        TypeElement subject = hc.getInfo().getElements().getTypeElement(className);
        
        if(subject == null) {
            return null;
        }
        
        for (ExecutableElement method : ElementFilter.methodsIn(subject.getEnclosedElements())){
            String methodName = method.getSimpleName().toString();
            
            if ("equals".equals(methodName) //NOI18N
                    && method.getParameters().size() == 1){
                
                if ("java.lang.Object".equals(method.getParameters().get(0).asType().toString())){ //NOI18N
                    hasEquals = true;
                }
            }
            else{
                if ("hashCode".equals(methodName) && method.getParameters().size() == 0){ //NOI18N
                    hasHashCode = true;
                }
            }
            
            if (hasHashCode && hasEquals){
                return null;
            }
        }
        
         return ErrorDescriptionFactory.forTree(
                    hc,
                    hc.getPath().getParentPath(),
                    NbBundle.getMessage(IdDefinedInHierarchy.class, "MSG_IdClassDoesNotOverrideEquals"));       
 
    }
}
