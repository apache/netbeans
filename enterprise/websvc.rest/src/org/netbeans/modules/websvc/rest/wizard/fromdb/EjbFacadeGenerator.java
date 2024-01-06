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

package org.netbeans.modules.websvc.rest.wizard.fromdb;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.persistence.action.EntityManagerGenerator;
import org.netbeans.modules.j2ee.persistence.action.GenerationOptions;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTAInjectableInEJB;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.FacadeGenerator;
import org.netbeans.modules.websvc.rest.codegen.RestGenerationOptions;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo.FieldInfo;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBeanModel;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkuchtiak
 */
public class EjbFacadeGenerator implements FacadeGenerator {
    
    private static final Logger LOGGER = Logger.getLogger(EjbFacadeGenerator.class.getName());
    
    private static final String REST_FACADE_SUFFIX = FACADE_SUFFIX+"REST"; //NOI18N
    private static final String FACADE_ABSTRACT = "AbstractFacade"; //NOI18N
    private static final String FACADE_REMOTE_SUFFIX = REST_FACADE_SUFFIX + "Remote"; //NOI18N
    private static final String FACADE_LOCAL_SUFFIX = REST_FACADE_SUFFIX + "Local"; //NOI18N
    private static final String EJB_LOCAL = "javax.ejb.Local"; //NOI18N
    private static final String EJB_REMOTE = "javax.ejb.Remote"; //NOI18N
    private static final String EJB_STATELESS = "javax.ejb.Stateless"; //NOI18N
    private static final String EJB_LOCAL_JAKARTA = "jakarta.ejb.Local"; //NOI18N
    private static final String EJB_REMOTE_JAKARTA = "jakarta.ejb.Remote"; //NOI18N
    private static final String EJB_STATELESS_JAKARTA = "jakarta.ejb.Stateless"; //NOI18N
    
    private EntityResourceBeanModel model;
    
    public EjbFacadeGenerator(){
    }
    
    public EjbFacadeGenerator(EntityResourceBeanModel model){
        this.model = model;
    }

    /**
     * Generates the facade and the loca/remote interface(s) for thhe given
     * entity class.
     * <i>Package private visibility for tests</i>.
     * @param targetFolder the folder where the facade and interfaces are generated.
     * @param entityClass the FQN of the entity class for which the facade is generated.
     * @param pkg the package prefix for the generated facede.
     * @param hasRemote specifies whether a remote interface is generated.
     * @param hasLocal specifies whether a local interface is generated.
     * @param strategyClass the entity manager lookup strategy.
     *
     * @return a set containing the generated files.
     */
    @Override
    public Set<FileObject> generate(final Project project,
            final Map<String, String> entityNames,
            final FileObject targetFolder,
            final String entityFQN,
            final String idClass,
            final String pkg, 
            final boolean hasRemote,
            final boolean hasLocal,
            boolean overrideExisting) throws IOException {

        ClassPath cp = ClassPath.getClassPath(targetFolder, ClassPath.COMPILE);

        final boolean javaxPersistenceAvailable = cp != null &&
                cp.findResource("javax/persistence/EntityManager.class") != null;

        final boolean jakartaPersistenceAvailable = cp != null &&
                cp.findResource("jakarta/persistence/EntityManager.class") != null;

        final boolean jakartaNamespace = jakartaPersistenceAvailable || (!javaxPersistenceAvailable);

        final Set<FileObject> createdFiles = new HashSet<FileObject>();
        final String entitySimpleName = JavaIdentifiers.unqualify(entityFQN);
        final String variableName = entitySimpleName.toLowerCase().charAt(0) + 
            entitySimpleName.substring(1);

        //create the abstract facade class
        final String afName = pkg + "." + FACADE_ABSTRACT;
        FileObject afFO = targetFolder.getFileObject(FACADE_ABSTRACT, "java");
        if (afFO == null) {
            afFO = GenerationUtils.createClass(targetFolder, FACADE_ABSTRACT, null);
            createdFiles.add(afFO);

            JavaSource source = JavaSource.forFileObject(afFO);
            source.runModificationTask(new Task<WorkingCopy>(){
                @Override
                public void run(WorkingCopy workingCopy) throws Exception {
                    workingCopy.toPhase(Phase.RESOLVED);
                    ClassTree classTree = SourceUtils.getPublicTopLevelTree(
                            workingCopy);
                    assert classTree != null;
                    TreeMaker maker = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(
                            workingCopy);
                    TreePath classTreePath = workingCopy.getTrees().getPath(
                            workingCopy.getCompilationUnit(), classTree);
                    TypeElement classElement = (TypeElement)workingCopy.getTrees().
                        getElement(classTreePath);

                    String genericsTypeName = "T";      //NOI18N
                    List<GenerationOptions> methodOptions = 
                        getAbstractFacadeMethodOptions(entityNames, 
                                genericsTypeName, "entity", jakartaNamespace); //NOI18N
                    List<Tree> members = new ArrayList<>();
                    String entityClassVar = "entityClass";                                              //NOI18N
                    Tree classObjectTree = genUtils.createType("java.lang.Class<" + 
                            genericsTypeName + ">", classElement);     //NOI18N
                    members.add(maker.Variable(genUtils.createModifiers(Modifier.PRIVATE),
                            entityClassVar,classObjectTree,null));
                    members.add(maker.Constructor(
                            genUtils.createModifiers(Modifier.PUBLIC),
                            Collections.EMPTY_LIST,
                            Arrays.asList(new VariableTree[]{genUtils.
                                    createVariable(entityClassVar,classObjectTree)}),
                            Collections.EMPTY_LIST,
                            "{this." + entityClassVar + " = " + entityClassVar + ";}"));    //NOI18N
                    for(GenerationOptions option: methodOptions) {
                        Tree returnType = (option.getReturnType() == null || 
                                option.getReturnType().equals("void"))?  //NOI18N
                                                maker.PrimitiveType(TypeKind.VOID):
                                                genUtils.createType(option.getReturnType(), 
                                                        classElement);
                        List<VariableTree> vars = option.getParameterName() == 
                            null ? Collections.EMPTY_LIST :
                                Arrays.asList(new VariableTree[] {
                                        genUtils.createVariable(
                                                option.getParameterName(),
                                                genUtils.createType(
                                                        option.getParameterType(), 
                                                        classElement))
                        });

                        if (option.getOperation() == null){
                            members.add(maker.Method(
                                    maker.Modifiers(option.getModifiers()),
                                    option.getMethodName(),
                                    returnType,
                                    Collections.EMPTY_LIST,
                                    vars,
                                    (List<ExpressionTree>)Collections.EMPTY_LIST,
                                    (BlockTree)null,
                                 null));
                        } else {
                            members.add(maker.Method(
                                    maker.Modifiers(option.getModifiers()),
                                    option.getMethodName(),
                                    returnType,
                                    (List<TypeParameterTree>)Collections.EMPTY_LIST,
                                    vars,
                                    (List<ExpressionTree>)Collections.EMPTY_LIST,
                                    "{" + option.getCallLines("getEntityManager()", 
                                            entityClassVar, project!=null ? 
                                                    PersistenceUtils.getJPAVersion(project) : 
                                                        Persistence.VERSION_1_0) + "}", //NOI18N
                                    null));
                        }
                    }

                    ClassTree newClassTree = maker.Class(
                            maker.Modifiers(EnumSet.of(Modifier.PUBLIC, 
                                    Modifier.ABSTRACT)),
                            classTree.getSimpleName(),
                            Arrays.asList(maker.TypeParameter(genericsTypeName, 
                                    Collections.EMPTY_LIST)),
                            null,
                            Collections.EMPTY_LIST,
                            members);

                    workingCopy.rewrite(classTree, newClassTree);
                }
            }).commit();

        }

        // create the facade
        FileObject existingFO = targetFolder.getFileObject(entitySimpleName + 
                REST_FACADE_SUFFIX, "java");
        if (existingFO != null) {
            if (overrideExisting) {
                existingFO.delete();
            } else {
                throw new IOException("file alerady exists exception: "+existingFO);
            }
        }
        final FileObject facade = GenerationUtils.createClass(targetFolder, 
                entitySimpleName + REST_FACADE_SUFFIX, null);
        createdFiles.add(facade);

        // create the interfaces
        final String localInterfaceFQN = pkg + "." + 
            getUniqueClassName(entitySimpleName + FACADE_LOCAL_SUFFIX, targetFolder);
        final String remoteInterfaceFQN = pkg + "." + 
            getUniqueClassName(entitySimpleName + FACADE_REMOTE_SUFFIX, targetFolder);

        List<GenerationOptions> intfOptions = getAbstractFacadeMethodOptions(
                entityNames, entityFQN, variableName, jakartaNamespace);
        if (hasLocal) {
            FileObject local = createInterface(
                    JavaIdentifiers.unqualify(localInterfaceFQN),
                    jakartaNamespace ? EJB_LOCAL_JAKARTA : EJB_LOCAL,
                    targetFolder
            );
            addMethodToInterface(intfOptions, local);
            createdFiles.add(local);
        }
        if (hasRemote) {
            FileObject remote = createInterface(
                    JavaIdentifiers.unqualify(remoteInterfaceFQN),
                    jakartaNamespace ? EJB_REMOTE_JAKARTA : EJB_REMOTE,
                    targetFolder
            );
            addMethodToInterface(intfOptions, remote);
            createdFiles.add(remote);
        }
        
        if ( model != null ) {
            Util.generatePrimaryKeyMethod(facade, entityFQN, model);
        }
        
        final FileObject abstractFacadeFO = afFO;
        
        // add the @stateless annotation
        // add implements and extends clauses to the facade
        final Task<WorkingCopy> modificationTask = new Task<WorkingCopy>(){
            @Override
            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(Phase.RESOLVED);
                TypeElement classElement = SourceUtils.getPublicTopLevelElement(wc);
                ClassTree classTree = wc.getTrees().getTree(classElement);
                assert classTree != null;
                GenerationUtils genUtils = GenerationUtils.newInstance(wc);
                TreeMaker maker = wc.getTreeMaker();

                List<Tree> implementsClause = new ArrayList<Tree>(
                        classTree.getImplementsClause());
                if (hasLocal) {
                    implementsClause.add(genUtils.createType(localInterfaceFQN, 
                            classElement));
                }
                if (hasRemote){
                    implementsClause.add(genUtils.createType(remoteInterfaceFQN, 
                            classElement));
                }
                
                List<Tree> members = new ArrayList<Tree>(classTree.getMembers());
                MethodTree constructor = maker.Constructor(
                        genUtils.createModifiers(Modifier.PUBLIC),
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        "{super(" + entitySimpleName + ".class);}");            //NOI18N
                members.add(constructor);

                List<RestGenerationOptions> restGenerationOptions = 
                    getRestFacadeMethodOptions(entityFQN, idClass, jakartaNamespace);

                ModifiersTree publicModifiers = genUtils.createModifiers(
                        Modifier.PUBLIC);
                ModifiersTree paramModifier = maker.Modifiers(
                        Collections.<Modifier>emptySet());
                for(RestGenerationOptions option: restGenerationOptions) {

                    ModifiersTree modifiersTree =
                            maker.addModifiersAnnotation(publicModifiers, 
                                    genUtils.createAnnotation(
                                            option.getRestMethod().getMethod(jakartaNamespace)));

                     // add @Path annotation
                    String uriPath = option.getRestMethod().getUriPath();
                    if (uriPath != null) {
                        ExpressionTree annArgument = maker.Literal(uriPath);
                        modifiersTree =
                                maker.addModifiersAnnotation(modifiersTree,
                                genUtils.createAnnotation(
                                        jakartaNamespace ? RestConstants.PATH_JAKARTA : RestConstants.PATH,
                                        Collections.<ExpressionTree>singletonList(annArgument))
                                );

                    }
                    
                    if ( option.getRestMethod().overrides() ){
                        modifiersTree =
                            maker.addModifiersAnnotation(modifiersTree,
                                    genUtils.createAnnotation(
                                            Override.class.getCanonicalName()));
                    }
                    // add @Produces annotation
                    String[] produces = option.getProduces();
                    if (produces != null) {
                        ExpressionTree annArguments;
                        if (produces.length == 1) {
                            annArguments = mimeTypeTree(maker, produces[0], jakartaNamespace);
                        } else {
                            List<ExpressionTree> mimeTypes = new ArrayList<ExpressionTree>();
                            for (int i=0; i< produces.length; i++) {
                                mimeTypes.add(mimeTypeTree(maker, produces[i], jakartaNamespace));
                            }
                            annArguments = maker.NewArray(null,
                                    Collections.<ExpressionTree>emptyList(), 
                                    mimeTypes);
                        }
                        modifiersTree =
                                maker.addModifiersAnnotation(modifiersTree,
                                        genUtils.createAnnotation(
                                                jakartaNamespace ? RestConstants.PRODUCE_MIME_JAKARTA : RestConstants.PRODUCE_MIME,
                                                Collections.<ExpressionTree>singletonList(annArguments))
                                );
                    }
                    // add @Consumes annotation
                    String[] consumes = option.getConsumes();
                    if (consumes != null) {
                        ExpressionTree annArguments;
                        if (consumes.length == 1) {
                            annArguments = mimeTypeTree(maker, consumes[0], jakartaNamespace);
                        } else {
                            List<ExpressionTree> mimeTypes = new ArrayList<ExpressionTree>();
                            for (int i=0; i< consumes.length; i++) {
                                mimeTypes.add(mimeTypeTree(maker, consumes[i], jakartaNamespace));
                            }
                            annArguments = maker.NewArray(null, 
                                    Collections.<ExpressionTree>emptyList(), mimeTypes);
                        }
                        modifiersTree =
                                maker.addModifiersAnnotation(modifiersTree,
                                        genUtils.createAnnotation(
                                                jakartaNamespace ? RestConstants.CONSUME_MIME_JAKARTA : RestConstants.CONSUME_MIME,
                                                Collections.<ExpressionTree>singletonList(annArguments)));
                    }

                    // create arguments list
                    List<VariableTree> vars = new ArrayList<VariableTree>();
                    String[] paramNames = option.getParameterNames();
                    int paramLength = paramNames == null ? 0 : 
                        option.getParameterNames().length ;

                    if (paramLength > 0) {
                        String[] paramTypes = option.getParameterTypes();
                        String[] pathParams = option.getPathParams();
                        
                        for (int i = 0; i<paramLength; i++) {
                            ModifiersTree pathParamTree = paramModifier;
                            if (pathParams != null && pathParams[i] != null) {
                                List<ExpressionTree> annArguments = 
                                    Collections.<ExpressionTree>singletonList(
                                            maker.Literal(pathParams[i]));
                                pathParamTree =
                                    maker.addModifiersAnnotation(paramModifier, 
                                            genUtils.createAnnotation(
                                                    jakartaNamespace ? RestConstants.PATH_PARAM_JAKARTA : RestConstants.PATH_PARAM,
                                                    annArguments));
                            }
                            Tree paramTree = genUtils.createType(paramTypes[i], 
                                    classElement);
                            VariableTree var = maker.Variable(pathParamTree, 
                                    paramNames[i], paramTree, null); //NOI18N
                            vars.add(var);

                        }
                    }

                    Tree returnType = (option.getReturnType() == null || 
                            option.getReturnType().equals("void"))?  //NOI18N
                                            maker.PrimitiveType(TypeKind.VOID):
                                            genUtils.createType(option.getReturnType(), 
                                                    classElement);

                    members.add(
                                maker.Method(
                                modifiersTree,
                                option.getRestMethod().getMethodName(),
                                returnType,
                                Collections.EMPTY_LIST,
                                vars,
                                (List<ExpressionTree>)Collections.EMPTY_LIST,
                                "{"+option.getBody()+"}", //NOI18N
                                null)
                            );

                }

                ModifiersTree modifiersTree = maker.addModifiersAnnotation(
                        classTree.getModifiers(),
                        genUtils.createAnnotation(jakartaNamespace ? EJB_STATELESS_JAKARTA : EJB_STATELESS)
                );

                ExpressionTree annArgument = maker.Literal(entityFQN.toLowerCase());
                modifiersTree =
                        maker.addModifiersAnnotation(modifiersTree, 
                                genUtils.createAnnotation(
                                        jakartaNamespace ? RestConstants.PATH_JAKARTA : RestConstants.PATH,
                                        Collections.<ExpressionTree>singletonList(annArgument)));
                               

                TypeElement abstractFacadeElement = wc.getElements().getTypeElement(afName);
                TypeElement entityElement = wc.getElements().getTypeElement(entityFQN);
                if (abstractFacadeElement == null) {
                    LOGGER.log(Level.SEVERE, "TypeElement not found for {0}", afName);
                    if (abstractFacadeFO == null) {
                        LOGGER.log(Level.SEVERE, "AbstractFacade FileObject is null");
                    } else {
                        LOGGER.log(Level.SEVERE, "AbstractFacade:path={0},valid={1},canRead={2},", new Object[]{
                            abstractFacadeFO.getPath(), abstractFacadeFO.isValid(), abstractFacadeFO.canRead()});
                    }
                }
                ClassTree newClassTree = maker.Class(
                        modifiersTree,
                        classTree.getSimpleName(),
                        classTree.getTypeParameters(),
                        maker.Type(wc.getTypes().getDeclaredType(
                            abstractFacadeElement,
                            entityElement.asType())),
                        implementsClause,
                        members);

                wc.rewrite(classTree, newClassTree);
            }
        };

        try {
        JavaSource.forFileObject(facade).runWhenScanFinished( new Task<CompilationController>(){

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                JavaSource.forFileObject(facade).runModificationTask(modificationTask).commit();
            }
            
            }, true).get();
        }
        catch( InterruptedException e ){
            Logger.getLogger(EjbFacadeGenerator.class.getCanonicalName()).
                log(Level.INFO, null ,e );
        }
        catch( ExecutionException e ){
            Logger.getLogger(EjbFacadeGenerator.class.getCanonicalName()).
                log(Level.INFO, null ,e );
        }
        
        
        // generate methods for the facade
        EntityManagerGenerator generator = new EntityManagerGenerator(facade, entityFQN);
        List<GenerationOptions> methodOptions = getMethodOptions(entityFQN, variableName, jakartaNamespace);
        for (GenerationOptions each : methodOptions){
            generator.generate(each, ContainerManagedJTAInjectableInEJB.class);
        }
        modifyEntityManager( methodOptions, facade);

        return createdFiles;
    }

    private ExpressionTree mimeTypeTree(TreeMaker maker, String mimeType, boolean jakartaNamespace) {
        String mediaTypeMember = null;
        if (mimeType.equals("application/xml")) { // NOI18N
            mediaTypeMember = "APPLICATION_XML"; // NOI18N
        } else if (mimeType.equals("application/json")) { // NOI18N
            mediaTypeMember = "APPLICATION_JSON"; // NOI18N
        } else if (mimeType.equals("text/plain")) { // NOI18N
            mediaTypeMember = "TEXT_PLAIN"; // NOI18N
        }
        ExpressionTree result;
        if (mediaTypeMember == null) {
            result = maker.Literal(mimeType);
        } else {
            // Use a field of MediaType class if possible
            ExpressionTree typeTree = maker.QualIdent(
                    jakartaNamespace ? "jakarta.ws.rs.core.MediaType" : "javax.ws.rs.core.MediaType"); // NOI18N
            result = maker.MemberSelect(typeTree, mediaTypeMember);
        }
        return result;
    }

    private void modifyEntityManager( List<GenerationOptions> methodOptions ,
            FileObject fileObject)  throws IOException 
    {
        final Set<String> methodNames = new HashSet<String>();
        for( GenerationOptions opt : methodOptions ){
            methodNames.add( opt.getMethodName());
        }
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            @Override
            public void run(WorkingCopy workingCopy) throws Exception {
                
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                for (Tree typeDeclaration : cut.getTypeDecls()){
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDeclaration
                            .getKind()))
                    {
                        ClassTree clazz = (ClassTree) typeDeclaration;
                        TreePath path = workingCopy.getTrees().getPath(cut,
                                clazz);
                        Element element = workingCopy.getTrees().getElement(
                                path);
                        List<ExecutableElement> methods = ElementFilter
                                .methodsIn(element.getEnclosedElements());
                        for (ExecutableElement method : methods) {
                            if ( methodNames.contains(method.getSimpleName().
                                    toString()) )
                            {
                                MethodTree methodTree = workingCopy.getTrees().
                                    getTree( method );
                                Set<Modifier> modifiers = method.getModifiers();
                                AnnotationTree annotation = make.Annotation(
                                        make.QualIdent(Override.class.getCanonicalName()),
                                        Collections.<ExpressionTree>emptyList());
                                ModifiersTree newModifs = make.Modifiers(
                                        modifiers, 
                                        Collections.singletonList(annotation));
                                workingCopy.rewrite(methodTree.getModifiers(), 
                                        newModifs);
                            }
                        }
                    }
                }
            }
        };
        
        JavaSource.forFileObject(fileObject).runModificationTask(task).commit();
    }

    private List<GenerationOptions> getAbstractFacadeMethodOptions(Map<String, 
            String> entityNames, String entityFQN, String variableName,
            boolean jakartaNamespace)
    {

        GenerationOptions getEMOptions = new GenerationOptions();
        getEMOptions.setMethodName("getEntityManager"); //NOI18N
        if (jakartaNamespace) {
            getEMOptions.setReturnType("jakarta.persistence.EntityManager");//NOI18N
        } else {
            getEMOptions.setReturnType("javax.persistence.EntityManager");//NOI18N
        }
        getEMOptions.setModifiers(EnumSet.of(Modifier.PROTECTED, Modifier.ABSTRACT));

        //implemented methods
        GenerationOptions createOptions = new GenerationOptions();
        createOptions.setMethodName("create"); //NOI18N
        createOptions.setOperation(GenerationOptions.Operation.PERSIST);
        createOptions.setReturnType("void");//NOI18N
        createOptions.setParameterName(variableName);
        createOptions.setParameterType(entityFQN);

        GenerationOptions editOptions = new GenerationOptions();
        editOptions.setMethodName("edit");//NOI18N
        editOptions.setOperation(GenerationOptions.Operation.MERGE);
        editOptions.setReturnType("void");//NOI18N
        editOptions.setParameterName(variableName);
        editOptions.setParameterType(entityFQN);

        GenerationOptions destroyOptions = new GenerationOptions();
        destroyOptions.setMethodName("remove");//NOI18N
        destroyOptions.setOperation(GenerationOptions.Operation.REMOVE);
        destroyOptions.setReturnType("void");//NOI18N
        destroyOptions.setParameterName(variableName);
        destroyOptions.setParameterType(entityFQN);

        GenerationOptions findOptions = new GenerationOptions();
        findOptions.setMethodName("find");//NOI18N
        findOptions.setOperation(GenerationOptions.Operation.FIND);
        findOptions.setReturnType(entityFQN);//NOI18N
        findOptions.setParameterName("id");//NOI18N
        findOptions.setParameterType("Object");//NOI18N

        GenerationOptions findAllOptions = new GenerationOptions();
        findAllOptions.setMethodName("findAll");//NOI18N
        findAllOptions.setOperation(GenerationOptions.Operation.FIND_ALL);
        findAllOptions.setReturnType("java.util.List<" + entityFQN + ">");//NOI18N
        findAllOptions.setQueryAttribute(getEntityName(entityNames, entityFQN));

        GenerationOptions findSubOptions = new GenerationOptions();
        findSubOptions.setMethodName("findRange");//NOI18N
        findSubOptions.setOperation(GenerationOptions.Operation.FIND_SUBSET);
        findSubOptions.setReturnType("java.util.List<" + entityFQN + ">");//NOI18N
        findSubOptions.setQueryAttribute(getEntityName(entityNames, entityFQN));
        findSubOptions.setParameterName("range");//NOI18N
        findSubOptions.setParameterType("int[]");//NOI18N

        GenerationOptions countOptions = new GenerationOptions();
        countOptions.setMethodName("count");//NOI18N
        countOptions.setOperation(GenerationOptions.Operation.COUNT);
        countOptions.setReturnType("int");//NOI18N
        countOptions.setQueryAttribute(getEntityName(entityNames, entityFQN));

        return Arrays.<GenerationOptions>asList(getEMOptions, createOptions, 
                editOptions, destroyOptions, findOptions, findAllOptions, 
                    findSubOptions, countOptions);
    }

    /**
     * @return the options representing the methods for a facade, i.e. create/edit/
     * find/remove/findAll.
     */
    private List<GenerationOptions> getMethodOptions(String entityFQN, 
            String variableName, boolean jakartaNamespace)
    {

        GenerationOptions getEMOptions = new GenerationOptions();
        getEMOptions.setMethodName("getEntityManager"); //NOI18N
        getEMOptions.setOperation(GenerationOptions.Operation.GET_EM);
        if (jakartaNamespace) {
            getEMOptions.setReturnType("jakarta.persistence.EntityManager");//NOI18N
        } else {
            getEMOptions.setReturnType("javax.persistence.EntityManager");//NOI18N
        }
        getEMOptions.setModifiers(EnumSet.of(Modifier.PROTECTED));

        return Arrays.<GenerationOptions>asList(getEMOptions);
    }

    /**
     *@return the name for the given <code>entityFQN</code>.
     */
    private String getEntityName(Map<String, String> entityNames, String entityFQN){
        String result = entityNames.get(entityFQN);
        return result != null ? result : JavaIdentifiers.unqualify(entityFQN);
    }

    /**
     * Creates an interface with the given <code>name</code>, annotated with an annotation
     * of the given <code>annotationType</code>. <i>Package private visibility just because of tests</i>.
     *
     * @param name the name for the interface
     * @param annotationType the FQN of the annotation
     * @param targetFolder the folder to which the interface is generated
     *
     * @return the generated interface.
     */
    private FileObject createInterface(String name, final String annotationType, 
            FileObject targetFolder) throws IOException 
    {
        FileObject sourceFile = GenerationUtils.createInterface(targetFolder, name, null);
        JavaSource source = JavaSource.forFileObject(sourceFile);
        ModificationResult result = source.runModificationTask(new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = SourceUtils.getPublicTopLevelTree(workingCopy);
                assert clazz != null;
                GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                TreeMaker make = workingCopy.getTreeMaker();
                AnnotationTree annotations = genUtils.createAnnotation(annotationType);
                ModifiersTree modifiers = make.Modifiers(clazz.getModifiers(), 
                        Collections.<AnnotationTree>singletonList(annotations));
                ClassTree modifiedClass = make.Class(modifiers, 
                        clazz.getSimpleName(), clazz.getTypeParameters(), 
                            clazz.getExtendsClause(), 
                                Collections.<ExpressionTree>emptyList(), 
                                    Collections.<Tree>emptyList());
                workingCopy.rewrite(clazz, modifiedClass);
            }
        });
        result.commit();
        return source.getFileObjects().iterator().next();
    }

    private String getUniqueClassName(String candidateName, FileObject targetFolder){
        return FileUtil.findFreeFileName(targetFolder, candidateName, "java"); //NOI18N
    }
    /**
     * Adds a method to the given interface.
     *
     * @param name the name of the method.
     * @param returnType the return type of the method.
     * @param parameterName the name of the parameter for the method.
     * @param parameterType the FQN type of the parameter.
     * @param target the target interface.
     */
    private void addMethodToInterface(final List<GenerationOptions> options, 
            final FileObject target) throws IOException 
    {

        JavaSource source = JavaSource.forFileObject(target);
        ModificationResult result = source.runModificationTask(new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);
                GenerationUtils utils = GenerationUtils.newInstance(copy);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(copy);
                assert typeElement != null;
                ClassTree original = copy.getTrees().getTree(typeElement);
                ClassTree modifiedClass = original;
                TreeMaker make = copy.getTreeMaker();
                for (GenerationOptions each : options) {
                    if (each.getModifiers().size() == 1 && 
                            each.getModifiers().contains(Modifier.PUBLIC))
                    {
                        MethodTree method = make.Method(make.Modifiers(
                                Collections.<Modifier>emptySet()),
                                each.getMethodName(), 
                                utils.createType(each.getReturnType(), typeElement),
                                Collections.<TypeParameterTree>emptyList(), 
                                getParameterList(each, make, utils, typeElement),
                                Collections.<ExpressionTree>emptyList(), 
                                (BlockTree) null, null);
                        modifiedClass = make.addClassMember(modifiedClass, method);
                    }
                }
                copy.rewrite(original, modifiedClass);
            }
        });
        result.commit();
    }

    private List<VariableTree> getParameterList(GenerationOptions options, 
            TreeMaker make, GenerationUtils utils, TypeElement scope)
    {
        if (options.getParameterName() == null){
            return Collections.<VariableTree>emptyList();
        }
        VariableTree vt = make.Variable(make.Modifiers(
                Collections.<Modifier>emptySet()),
                options.getParameterName(), utils.createType(
                        options.getParameterType(), scope), null);
        return Collections.<VariableTree>singletonList(vt);
    }
    
    private List<RestGenerationOptions> getRestFacadeMethodOptions(
            String entityFQN, String idClass, boolean jakartaNamespace)
    {
        final String pathSegmentType;
        if(jakartaNamespace) {
            pathSegmentType = "jakarta.ws.rs.core.PathSegment";
        } else {
            pathSegmentType = "javax.ws.rs.core.PathSegment";
        }

        String paramArg = "java.lang.Character".equals(idClass) ? 
                "id.charAt(0)" : "id"; //NOI18N
        String idType = "id".equals(paramArg) ? idClass : "java.lang.String"; //NOI18N
        
        boolean needPathSegment = false;
        if ( model!= null ){
            EntityClassInfo entityInfo = model.getEntityInfo(entityFQN);
            if ( entityInfo!= null ){
                FieldInfo idFieldInfo = entityInfo.getIdFieldInfo();
                needPathSegment = idFieldInfo!=null && idFieldInfo.isEmbeddedId() 
                        && idFieldInfo.getType()!= null;
            }
        }

        RestGenerationOptions createOptions = new RestGenerationOptions();
        createOptions.setRestMethod(Operation.CREATE);
        createOptions.setReturnType("void"); //NOI18N
        createOptions.setParameterNames(new String[]{"entity"}); //NOI18N
        createOptions.setParameterTypes(new String[]{entityFQN});
        createOptions.setConsumes(new String[]{"application/xml", 
                "application/json"}); //NOI18N
        createOptions.setBody("super.create(entity);"); //NOI18N

        RestGenerationOptions editOptions = new RestGenerationOptions();
        editOptions.setRestMethod(Operation.EDIT);
        editOptions.setReturnType("void");//NOI18N
        editOptions.setParameterNames(new String[]{"id", "entity"}); //NOI18N
        editOptions.setPathParams(new String[]{"id", null}); //NOI18N
        if ( needPathSegment ){
            editOptions.setParameterTypes(new String[]{pathSegmentType, entityFQN}); // NOI18N
        }
        else {
            editOptions.setParameterTypes(new String[]{idType, entityFQN}); 
        }
        //editOptions.setParameterTypes(new String[]{entityFQN}); //NOI18N
        editOptions.setConsumes(new String[]{"application/xml", 
                "application/json"}); //NOI18N
        editOptions.setBody("super.edit(entity);"); //NOI18N

        RestGenerationOptions destroyOptions = new RestGenerationOptions();
        destroyOptions.setRestMethod(Operation.REMOVE);
        destroyOptions.setReturnType("void");//NOI18N
        destroyOptions.setParameterNames(new String[]{"id"}); //NOI18N
        destroyOptions.setPathParams(new String[]{"id"}); //NOI18N
        StringBuilder builder = new StringBuilder();
        if ( needPathSegment ){
            destroyOptions.setParameterTypes(new String[]{pathSegmentType}); // NOI18N
            builder.append(idType);
            builder.append(" key=getPrimaryKey(id);\n");
            paramArg = "key";
        }
        else {
            destroyOptions.setParameterTypes(new String[]{idType}); 
        }
        StringBuilder removeBody = new StringBuilder(builder);
        removeBody.append("super.remove(super.find(");             //NOI18N
        removeBody.append(paramArg);
        removeBody.append("));");                                  //NOI18N
        destroyOptions.setBody(removeBody.toString()); 

        RestGenerationOptions findOptions = new RestGenerationOptions();
        findOptions.setRestMethod(Operation.FIND);
        findOptions.setReturnType(entityFQN);//NOI18N
        findOptions.setProduces(new String[]{"application/xml", "application/json"}); //NOI18N
        findOptions.setPathParams(new String[]{"id"}); //NOI18N
        findOptions.setParameterNames(new String[]{"id"}); //NOI18N
        if ( needPathSegment ){
            findOptions.setParameterTypes(new String[]{pathSegmentType}); // NOI18N
        }
        else {
            findOptions.setParameterTypes(new String[]{idType});     
        }
        StringBuilder findBody = new StringBuilder(builder);
        findBody.append("return super.find(");                  //NOI18N
        findBody.append(paramArg);
        findBody.append(");");                                  //NOI18N
        findOptions.setBody( findBody.toString()); 

        RestGenerationOptions findAllOptions = new RestGenerationOptions();
        findAllOptions.setRestMethod(Operation.FIND_ALL);
        findAllOptions.setReturnType("java.util.List<" + entityFQN + ">");//NOI18N
        findAllOptions.setProduces(new String[]{"application/xml", "application/json"});
        findAllOptions.setBody("return super.findAll();");

        RestGenerationOptions findSubOptions = new RestGenerationOptions();
        findSubOptions.setRestMethod(Operation.FIND_RANGE);
        findSubOptions.setReturnType("java.util.List<" + entityFQN + ">");//NOI18N
        findSubOptions.setProduces(new String[]{"application/xml", 
                "application/json"}); //NOI18N
        findSubOptions.setParameterNames(new String[]{"from", "to"}); //NOI18N
        findSubOptions.setParameterTypes(new String[]{"java.lang.Integer", 
                "java.lang.Integer"}); //NOI18N
        findSubOptions.setPathParams(new String[]{"from", "to"}); //NOI18N
        findSubOptions.setBody("return super.findRange(new int[] {from, to});"); //NOI18N

        RestGenerationOptions countOptions = new RestGenerationOptions();
        countOptions.setRestMethod(Operation.COUNT);
        countOptions.setReturnType("java.lang.String");//NOI18N
        countOptions.setProduces(new String[]{"text/plain"}); //NOI18N
        countOptions.setBody("return String.valueOf(super.count());"); //NOI18N

        return Arrays.<RestGenerationOptions>asList(
                createOptions,
                editOptions,
                destroyOptions,
                findOptions,
                findAllOptions,
                findSubOptions,
                countOptions);
    }


}
