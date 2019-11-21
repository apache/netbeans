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
package org.netbeans.modules.websvc.rest;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WildcardTree;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.modules.websvc.rest.model.api.RestApplicationModel;
import org.netbeans.modules.websvc.rest.model.api.RestApplications;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.rest.model.api.RestProviderDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.spi.MiscUtilities;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 * This class contains everything related to generation of Application subclass which used
 * to be defined directly in RestSupport or one of its subclasses. I tried to move
 * it here as a logical piece of functionality. The methods itself in this class
 * were never reviewed - I just moved them from somewhere else. See also
 * WebXmlUpdater class which has similar role for everything related to web.xml update.
 */
public class ApplicationSubclassGenerator {

    static final String GET_REST_RESOURCE_CLASSES = "getRestResourceClasses";//NOI18N

    private RequestProcessor.Task refreshTask = null;
    private static RequestProcessor RP = new RequestProcessor(ApplicationSubclassGenerator.class);

    private RestSupport restSupport;

    public ApplicationSubclassGenerator(RestSupport restSupport) {
        this.restSupport = restSupport;
    }

    public void refreshApplicationSubclass() {
        getRefreshTask().schedule(1000);
    }
    
    private synchronized RequestProcessor.Task getRefreshTask() {
        if (refreshTask == null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        doReconfigure();
                    }
                    catch(IOException e ){
                        Logger.getLogger(RestSupport.class.getName()).log(
                                Level.INFO, e.getLocalizedMessage(), e);
                    }
                }
            };
            refreshTask = RP.create(runnable);
        }
        return refreshTask;
    }

    private void doReconfigure() throws IOException {
        RestApplicationModel restAppModel = restSupport.getRestApplicationsModel();
        RestServicesModel model = restSupport.getRestServicesModel();
        String clazz = null;
        Collection<String> classNames = Collections.emptyList();
        try {
            clazz = restAppModel.runReadAction(
                    new MetadataModelAction<RestApplications, String>() {
                @Override
                public String run(final RestApplications metadata)
                        throws IOException {
                    List<RestApplication> applications =
                            metadata.getRestApplications();
                    if (applications != null
                            && !applications.isEmpty()) {
                        RestApplication application =
                                applications.get(0);
                        String clazz = application.
                                getApplicationClass();
                        return clazz;
                    }
                    return null;
                }
            });

            if (clazz != null) {
                classNames = model.runReadAction(new MetadataModelAction<RestServicesMetadata, Collection<String>>() {
                    @Override
                    public Collection<String> run(RestServicesMetadata metadata) throws Exception {
                        Collection<String> classes = new TreeSet<String>();
                        RestServices services = metadata.getRoot();
                        for (RestServiceDescription description : services.getRestServiceDescription()) {
                            // ignore REST services for which we do not have sources (#216168, #229168):
                            if (description.getFile() != null) {
                                classes.add(description.getClassName());
                            }
                        }
                        for (RestProviderDescription provider : services.getProviders()) {
                            // ignore REST providers for which we do not have sources (#216168, #229168):
                            if (provider.getFile() != null) {
                                classes.add(provider.getClassName());
                            }
                        }
                        return classes;
                    }
                });
            }

        } catch (MetadataModelException ex) {
            Logger.getLogger(RestSupport.class.getName()).log(
                    Level.INFO, ex.getLocalizedMessage(), ex);
        }
        if (clazz != null) {
            reconfigApplicationClass(clazz, classNames);
        }
    }

    protected void reconfigApplicationClass(String appClassFqn, final Collection<String> classNames) throws IOException {
        JavaSource javaSource = MiscPrivateUtilities.getJavaSourceFromClassName(restSupport.getProject(), appClassFqn);
        if ( javaSource == null ){
            return;
        }
        ModificationResult res = javaSource.runModificationTask( new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                CompilationUnitTree tree = workingCopy.getCompilationUnit();
                for (Tree typeDeclaration : tree.getTypeDecls()){
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDeclaration.getKind())){
                        MethodTree getClasses = null;
                        MethodTree restResources = null;
                        MethodTree restResources2 = null;
                        ClassTree classTree = (ClassTree) typeDeclaration;
                        List<? extends Tree> members = classTree.getMembers();
                        for (Tree member : members) {
                            if ( member.getKind().equals(Tree.Kind.METHOD)){
                                MethodTree method = (MethodTree)member;
                                String name = method.getName().toString();
                                if ( name.equals(RestConstants.GET_CLASSES)){
                                    getClasses = method;
                                }
                                else if ( name.equals(GET_REST_RESOURCE_CLASSES)){
                                    restResources = method;
                                } else if ( name.equals(RestConstants.GET_REST_RESOURCE_CLASSES2)){
                                    restResources2 = method;
                                }
                            }
                        }
                        TreeMaker maker = workingCopy.getTreeMaker();
                        ClassTree modified = classTree;

                        if (getClasses != null && restResources != null) {
                            // this is old code generator replaced in NB 7.3.1
                            // as part of EE7 upgrade:
                            modified = removeResourcesMethod( restResources,
                                    maker, modified);
                            modified = createMethodsOlderVersion(classNames,
                                    maker, modified, workingCopy);
                        } else {
                            if (restResources2 != null) {
                                modified = removeResourcesMethod( restResources2,
                                        maker, modified);
                                modified = createMethods(classNames, getClasses,
                                        maker, modified, workingCopy);
                            }
                        }

                        workingCopy.rewrite(classTree, modified);
                    }
                }
            }

        });
        res.commit();

        Collection<FileObject> files = javaSource.getFileObjects();
        if ( files.isEmpty() ){
            return;
        }
        FileObject fileObject = files.iterator().next();
        DataObject dataObject = DataObject.find(fileObject);
        if ( dataObject!= null){
            SaveCookie cookie = dataObject.getLookup().lookup(SaveCookie.class);
            if ( cookie!= null ){
                cookie.save();
            }
        }
    }

    private ClassTree removeResourcesMethod( MethodTree restResources,
            TreeMaker maker, ClassTree modified )
    {
        return maker.removeClassMember(modified, restResources);
    }

    private ClassTree createMethods(Collection<String> classNames, MethodTree getClasses,
            TreeMaker maker, ClassTree modified,
            CompilationController controller) throws IOException
    {
        WildcardTree wildCard = maker.Wildcard(Tree.Kind.UNBOUNDED_WILDCARD,
                null);
        ParameterizedTypeTree wildClass = maker.ParameterizedType(
                maker.QualIdent(Class.class.getCanonicalName()),
                Collections.singletonList(wildCard));
        ParameterizedTypeTree wildSet = maker.ParameterizedType(
                maker.QualIdent(Set.class.getCanonicalName()),
                Collections.singletonList(wildClass));

        String methodBody = MiscPrivateUtilities.collectRestResources(classNames, restSupport, false);
        
        return MiscUtilities.createAddResourceClasses(maker, modified, controller, methodBody, false);
    }

    private ClassTree createMethodsOlderVersion(Collection<String> classNames,
            TreeMaker maker,ClassTree modified,
            CompilationController controller) throws IOException
    {
        WildcardTree wildCard = maker.Wildcard(Tree.Kind.UNBOUNDED_WILDCARD,
                null);
        ParameterizedTypeTree wildClass = maker.ParameterizedType(
                maker.QualIdent(Class.class.getCanonicalName()),
                Collections.singletonList(wildCard));
        ParameterizedTypeTree wildSet = maker.ParameterizedType(
                maker.QualIdent(Set.class.getCanonicalName()),
                Collections.singletonList(wildClass));
        //StringBuilder builder = new StringBuilder();
        String methodBody = MiscPrivateUtilities.collectRestResources(classNames, restSupport, true);
        ModifiersTree modifiersTree = maker.Modifiers(EnumSet
                .of(Modifier.PRIVATE));
        MethodTree methodTree = maker.Method(modifiersTree,
                GET_REST_RESOURCE_CLASSES, wildSet,
                Collections.<TypeParameterTree> emptyList(),
                Collections.<VariableTree> emptyList(),
                Collections.<ExpressionTree> emptyList(), methodBody,
                null);
        modified = maker.addClassMember(modified, methodTree);
        return modified;
    }

    public static String getApplicationPathFromAnnotations(RestSupport restSupport, final String applPathFromDD) {
        List<RestApplication> restApplications = restSupport.getRestApplications();
        if (applPathFromDD == null) {
            if (restApplications.isEmpty()) {
                return null;
            } else {
                return restSupport.getApplicationPathFromDialog(restApplications);
            }
        } else {
            if (restApplications.isEmpty()) {
                return applPathFromDD;
            } else {
                boolean found = false;
                for (RestApplication appl: restApplications) {
                    if (applPathFromDD.equals(appl.getApplicationPath())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    restApplications.add(new RestApplication() {
                        public String getApplicationPath() {
                            return applPathFromDD;
                        }

                        public String getApplicationClass() {
                            return "web.xml"; //NOI18N
                        }
                    });
                }
                return restSupport.getApplicationPathFromDialog(restApplications);
            }
        }
    }


}
