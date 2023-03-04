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
package org.netbeans.modules.websvc.rest.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.modules.websvc.rest.model.api.RestApplicationModel;
import org.netbeans.modules.websvc.rest.model.api.RestApplications;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import java.io.IOException;
import java.util.Collections;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.spi.MiscUtilities;
import org.netbeans.modules.websvc.rest.spi.RestSupport;


/**
 * @author ads
 *
 */
class RestScanTask {

    RestScanTask( RestConfigurationEditorAwareTaskFactory factory,
            FileObject fileObject, CompilationInfo info )
    {
        this.factory = factory;
        this.fileObject = fileObject;
        this.info = info;
        hints = new LinkedList<ErrorDescription>();
        stop = new AtomicBoolean( false );
    }

    void run(){
       final Project project = FileOwnerQuery.getOwner(fileObject);
       if ( project == null ){
           return;
       }
       WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
       if( webModule == null ){
           return;
       }
       final RestSupport support = project.getLookup().lookup(RestSupport.class);
       if ( support ==null || !support.isEESpecWithJaxRS() ){
           return;
       }
       
       boolean needConfiguration = true;
       try {
            if (support.isRestSupportOn()) {
                if (MiscUtilities.getApplicationPathFromDD(support.getWebApp()) != null
                        || !support.getRestApplications().isEmpty())
                {
                    needConfiguration = false;
                }
            }
            RestServicesModel servicesModel = support.getRestServicesModel();
            if (servicesModel != null) {
                if ( needConfiguration ){
                    configureRest(project, servicesModel);
                }
                else {
                    checkApplicationConfiguration(project, support, servicesModel);
                }
            }
       }
       catch(IOException e ){
           // Ignore all exceptions , just returns without assigned hints
       }
    }

    private void checkApplicationConfiguration( final Project project,
            final RestSupport support, RestServicesModel servicesModel )
            throws MetadataModelException, IOException
    {
        RestApplicationModel applicationModel = support.getRestApplicationsModel();
        List<RestApplication> applications = null;
        if ( isCancelled() ){
            return ;
        }
        if (applicationModel != null) {
            applications = applicationModel
                    .runReadAction(new MetadataModelAction<RestApplications, 
                            List<RestApplication>>()
                    {
                        @Override
                        public List<RestApplication> run(
                                RestApplications metadata )
                                throws IOException
                        {
                            if ( isCancelled() ){
                                return null;
                            }
                            return metadata.getRestApplications();
                        }
                    });
        }
        else {
            return;
        }
                
        if ( applications== null || applications.isEmpty() ||isCancelled()){
            return;
        }
        final List<RestApplication> restApps = applications;
        servicesModel.runReadAction(
                new MetadataModelAction<RestServicesMetadata, Void>()
        {

            @Override
            public Void run( RestServicesMetadata metadata )
                    throws Exception
            {
                if ( isCancelled() ){
                    return null;
                }
                doCheckApplicationConfiguration(project,support, restApps,
                        metadata);
                return null;
            }
        });
    }

    private void configureRest( final Project project,
            RestServicesModel servicesModel ) throws MetadataModelException,
            IOException
    {
        servicesModel.runReadAction(
                new MetadataModelAction<RestServicesMetadata, Void>()
        {

            @Override
            public Void run( RestServicesMetadata metadata )
                    throws Exception
            {
                if ( isCancelled() ){
                    return null;
                }
                doConfigureRest(project, metadata);
                return null;
            }
        });
    }

    void stop() {
        stop.set(true);
    }

    Collection<? extends ErrorDescription> getHints() {
        return hints;
    }
    
    private void doCheckApplicationConfiguration( Project project, RestSupport support,
            List<RestApplication>  applications, RestServicesMetadata metadata) 
    {
        List<TypeElement> restResources = getRestResources(metadata);
        for (TypeElement typeElement : restResources) {
            String fqn = typeElement.getQualifiedName().toString();
            if ( isCancelled() ){
                return; 
            }
            if ( !MiscUtilities.hasApplicationResourceClass(support, fqn ) ){
                ClassTree tree = info.getTrees().getTree(typeElement);
                List<Integer> position = getElementPosition(info, tree);
                
                Fix fix = new ApplicationConfigurationFix(project,
                                fileObject, factory,  fqn, info.getClasspathInfo());
                List<Fix> fixes = Collections.singletonList(fix);
                ErrorDescription description = ErrorDescriptionFactory
                        .createErrorDescription(Severity.HINT,
                                NbBundle.getMessage(RestScanTask.class,
                                        "TXT_NoApplicationResource",    // NOI18N
                                        fqn ), fixes , 
                                        info.getFileObject(), position.get(0),
                                        position.get(1));
                hints.add(description);
            }
        }
    }
    
    private void doConfigureRest( Project project, RestServicesMetadata metadata ) 
    {
        List<TypeElement> rest = getRestResources(metadata);
        if ( rest.isEmpty() ){
            return;
        }
        ClassTree tree = info.getTrees().getTree(rest.get(0));
        List<Integer> position = getElementPosition(info, tree);
        
        ErrorDescription description = ErrorDescriptionFactory
                .createErrorDescription(Severity.WARNING,
                        NbBundle.getMessage(RestScanTask.class,
                                "TXT_NoRestConfiguration"), // NOI18N
                        RestConfigHint.getConfigHints(project,
                                fileObject, factory, info.getClasspathInfo()), 
                                info.getFileObject(), position.get(0),
                                position.get(1));
        hints.add(description);
    }
    
    private List<TypeElement> getRestResources(RestServicesMetadata metadata) 
    {
        List<? extends TypeElement> types = info.getTopLevelElements();
        if ( isCancelled()){
            return Collections.emptyList();
        }
        Set<String> restFqns = new HashSet<String>();
        if (isCancelled()) {
            return Collections.emptyList();
        }
        RestServices services = metadata.getRoot();
        RestServiceDescription[] descriptions = services
                .getRestServiceDescription();
        for (RestServiceDescription description : descriptions) {
            restFqns.add(description.getClassName());
        }

        if ( isCancelled()){
            return Collections.emptyList();
        }
        List<TypeElement> rest = new ArrayList<TypeElement>(
                types.size());
        for (TypeElement typeElement : types) {
            if (restFqns.contains(typeElement.getQualifiedName().toString())){
                rest.add(typeElement);
            }
        }
        return rest;
    }
    
    private boolean isCancelled(){
        return stop.get();
    }
    
    static List<Integer> getElementPosition(CompilationInfo info, Tree tree){
        SourcePositions srcPos = info.getTrees().getSourcePositions();
        
        int startOffset = (int) srcPos.getStartPosition(info.getCompilationUnit(), tree);
        int endOffset = (int) srcPos.getEndPosition(info.getCompilationUnit(), tree);
        
        Tree startTree = null;
        
        if (TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind())){
            startTree = ((ClassTree)tree).getModifiers();
            
        } else if (tree.getKind() == Tree.Kind.METHOD){
            startTree = ((MethodTree)tree).getReturnType();
        } else if (tree.getKind() == Tree.Kind.VARIABLE){
            startTree = ((VariableTree)tree).getType();
        }
        
        if (startTree != null){
            int searchStart = (int) srcPos.getEndPosition(info.getCompilationUnit(),
                    startTree);
            
            TokenSequence<?> tokenSequence = info.getTreeUtilities().tokensFor(tree);
            
            if (tokenSequence != null){
                boolean eob = false;
                tokenSequence.move(searchStart);
                
                do{
                    eob = !tokenSequence.moveNext();
                }
                while (!eob && tokenSequence.token().id() != JavaTokenId.IDENTIFIER);
                
                if (!eob){
                    Token<?> identifier = tokenSequence.token();
                    startOffset = identifier.offset(info.getTokenHierarchy());
                    endOffset = startOffset + identifier.length();
                }
            }
        }
        
        List<Integer> result = new ArrayList<Integer>(2);
        result.add(startOffset);
        result.add(endOffset );
        return result;
    }
    
    private FileObject fileObject;
    private RestConfigurationEditorAwareTaskFactory factory;
    private CompilationInfo info;
    private List<ErrorDescription> hints;
    private AtomicBoolean stop;
}
