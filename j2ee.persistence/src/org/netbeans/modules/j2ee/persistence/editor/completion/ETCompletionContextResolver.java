/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.persistence.editor.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.eclipse.persistence.jpa.jpql.parser.DefaultJPQLGrammar;
import org.eclipse.persistence.jpa.jpql.tools.ContentAssistProposals;
import org.eclipse.persistence.jpa.jpql.tools.DefaultJPQLQueryHelper;
import org.eclipse.persistence.jpa.jpql.tools.spi.IEntity;
import org.eclipse.persistence.jpa.jpql.tools.spi.IMapping;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.jpql.ManagedTypeProvider;
import org.netbeans.modules.j2ee.persistence.spi.jpql.Query;

/**
 *
 * @author Sergey Petrov
 */
public class ETCompletionContextResolver implements CompletionContextResolver {
    
    private static final Logger LOGGER = Logger.getLogger(ETCompletionContextResolver.class.getName());
    
    @Override
    public List resolve(JPACodeCompletionProvider.Context ctx) {
        
        List<JPACompletionItem> result = new ResultItemsFilterList(ctx);
        
        //parse the annotation
        String methodName = ctx.getMethod() == null ? null : ctx.getMethod().getMethodName();
        String annotationName = null;
        CCParser.CC parsedNN = null;
        CCParser.NNAttr nnattr = null;
        if (methodName == null){
            parsedNN = ctx.getParsedAnnotation();
            if (parsedNN == null) {
                return result;
            }

            nnattr = parsedNN.getAttributeForOffset(ctx.getCompletionOffset());
            if(nnattr == null) {
                return result;
            }

            annotationName = parsedNN.getName();
            if(annotationName == null) {
                return result;
            }
        }
        if(CCParser.CREATE_NAMEDQUERY.equals(methodName)) {
            result = completecreateNamedQueryparameters(ctx, result);
        } else if (CCParser.CREATE_QUERY.equals(methodName)){
            completeJPQLContext(ctx, ctx.getMethod(), result);
        } else if("NamedQuery".equals(annotationName)){//NOI18N
            completeJPQLContext(ctx, parsedNN, nnattr, result);
        }
        
        
        return result;
    }

    private List<JPACompletionItem> completecreateNamedQueryparameters(JPACodeCompletionProvider.Context ctx, List<JPACompletionItem> results) {
        Project prj = FileOwnerQuery.getOwner(ctx.getFileObject());
        EntityClassScopeProvider provider = (EntityClassScopeProvider) prj.getLookup().lookup(EntityClassScopeProvider.class);
        EntityClassScope ecs = null;
        Entity[] entities = null;
        if (provider != null) {
            ecs = provider.findEntityClassScope(ctx.getFileObject());
        }
        if (ecs != null) {
            try {
                entities = ecs.getEntityMappingsModel(false).runReadAction(new MetadataModelAction<EntityMappingsMetadata, Entity[]>() {
                   @Override
                    public Entity[] run(EntityMappingsMetadata metadata) throws Exception {
                        return metadata.getRoot().getEntity();
                    }
                });
            } catch (MetadataModelException ex) {
            } catch (IOException ex) {
            }
        }
        if(entities != null) {
            for (Entity entity : entities) {
                for(NamedQuery nq:entity.getNamedQuery()){
                    results.add(new JPACompletionItem.NamedQueryNameItem(nq.getName(), entity.getName(), nq.getQuery(), ctx.getMethod().isWithQ(), ctx.getMethod().getValueOffset()));
                }
            }
        }
        return results;
    }
    
     private List completeJPQLContext(JPACodeCompletionProvider.Context ctx, CCParser.CC nn, CCParser.NNAttr nnattr, List<JPACompletionItem> results) {
        String completedMember = nnattr.getName();

        if ("query".equals(completedMember)) { // NOI18N
            String completedValue = nnattr.getValue().toString() == null ? "" : nnattr.getValue().toString();
            DefaultJPQLQueryHelper helper = new DefaultJPQLQueryHelper(DefaultJPQLGrammar.instance());

            Project project = FileOwnerQuery.getOwner(ctx.getFileObject());
            helper.setQuery(new Query(null, completedValue, new ManagedTypeProvider(project, ctx.getEntityMappings(), ctx.getController().getElements())));
            int offset = ctx.getCompletionOffset() - nnattr.getValueOffset() - (nnattr.isValueQuoted() ? 1 : 0);
            ContentAssistProposals buildContentAssistProposals = null;
            try{
                buildContentAssistProposals = ((offset<=completedValue.length()) ? helper.buildContentAssistProposals(offset) : null);
            } catch (NullPointerException ex) {
                //al npe from 3rd party lib shouldn't affect nb much, see #222208
                LOGGER.log(Level.INFO, "exception in eclipsleink", ex);//NOI18N
            }
            
            if(buildContentAssistProposals!=null && buildContentAssistProposals.hasProposals()){
                for (String var : buildContentAssistProposals.identificationVariables()) {
                    results.add(new JPACompletionItem.JPQLElementItem(var, nnattr.isValueQuoted(), nnattr.getValueOffset(), offset, nnattr.getValue().toString(), buildContentAssistProposals));
                }
                for (IMapping mapping : buildContentAssistProposals.mappings()) {
                    results.add(new JPACompletionItem.JPQLElementItem(mapping.getName(), nnattr.isValueQuoted(), nnattr.getValueOffset(), offset, nnattr.getValue().toString(), buildContentAssistProposals));
                }
                for (IEntity entity : buildContentAssistProposals.abstractSchemaTypes()) {
                    results.add(new JPACompletionItem.JPQLElementItem(entity.getName(), nnattr.isValueQuoted(), nnattr.getValueOffset(), offset, nnattr.getValue().toString(), buildContentAssistProposals));
                }
                for (String ids : buildContentAssistProposals.identifiers()) {
                    results.add(new JPACompletionItem.JPQLElementItem(ids, nnattr.isValueQuoted(), nnattr.getValueOffset(), offset, nnattr.getValue().toString(), buildContentAssistProposals));
                }
            }
        }
        
        return results;
    }
     private List completeJPQLContext(JPACodeCompletionProvider.Context ctx, CCParser.MD method, List<JPACompletionItem> results) {

            String completedValue = method.getValue();
            if(completedValue == null) {
                return results;
            }//do not support case if "" isn't typed yet (there should be quite a lot of general java items, avoid mixing
            DefaultJPQLQueryHelper helper = new DefaultJPQLQueryHelper(DefaultJPQLGrammar.instance());
            completedValue = org.netbeans.modules.j2ee.persistence.editor.completion.Utils.unquote(completedValue);

            Project project = FileOwnerQuery.getOwner(ctx.getFileObject());
            helper.setQuery(new Query(null, completedValue, new ManagedTypeProvider(project, ctx.getEntityMappings(), ctx.getController().getElements())));
            int offset = ctx.getCompletionOffset() - method.getValueOffset() - (method.isWithQ() ? 1 : 0);
            ContentAssistProposals buildContentAssistProposals = helper.buildContentAssistProposals(offset);

            if(buildContentAssistProposals!=null && buildContentAssistProposals.hasProposals()){
                for (String var : buildContentAssistProposals.identificationVariables()) {
                    if(var!=null && var.length()>0) {
                        results.add(new JPACompletionItem.JPQLElementItem(var, true, method.getValueOffset(), offset, completedValue, buildContentAssistProposals));
                    }
                }
                for (IMapping mapping : buildContentAssistProposals.mappings()) {
                    if(mapping.getName()!=null && mapping.getName().length()>0) {
                        results.add(new JPACompletionItem.JPQLElementItem(mapping.getName(), true, method.getValueOffset(), offset, completedValue, buildContentAssistProposals));
                    }
                }
                for (IEntity entity : buildContentAssistProposals.abstractSchemaTypes()) {
                    if(entity.getName()!=null && entity.getName().length()>0) {
                        results.add(new JPACompletionItem.JPQLElementItem(entity.getName(), true, method.getValueOffset(), offset, completedValue, buildContentAssistProposals));
                    }
                }
                for (String ids : buildContentAssistProposals.identifiers()) {
                    if(ids!=null && ids.length()>0) {
                        results.add(new JPACompletionItem.JPQLElementItem(ids, true, method.getValueOffset(), offset, completedValue, buildContentAssistProposals));
                    }
                }
            }
        
        return results;
    }
    private static final class ResultItemsFilterList extends ArrayList {
        private JPACodeCompletionProvider.Context ctx;
        public ResultItemsFilterList(JPACodeCompletionProvider.Context ctx) {
            super();
            this.ctx = ctx;
        }
        
        @Override
        public boolean add(Object o) {
            if(!(o instanceof JPACompletionItem)) {
                return false;
            }
            
            JPACompletionItem ri = (JPACompletionItem)o;
            //check if the pretext corresponds to the result item text
            try {
                String preText = ctx.getBaseDocument().getText(ri.getSubstituteOffset(), ctx.getCompletionOffset() - ri.getSubstituteOffset());
                if(!ri.canFilter() || ri.getItemText().startsWith(preText)) {
                    return super.add(ri);
                }
            }catch(BadLocationException ble) {
                //ignore
            }
            return false;
        }
    }
    

    private static final boolean DEBUG = Boolean.getBoolean("debug." + ETCompletionContextResolver.class.getName());
}
