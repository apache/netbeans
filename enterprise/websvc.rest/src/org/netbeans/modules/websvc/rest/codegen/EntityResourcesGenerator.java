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
package org.netbeans.modules.websvc.rest.codegen;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.FacadeGenerator;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo.FieldInfo;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBeanModel;
import org.netbeans.modules.websvc.rest.codegen.model.GenericResourceBean;
import org.netbeans.modules.websvc.rest.model.api.RestConstants;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper.PersistenceUnit;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;

/**
 *
 * @author ads
 */
public abstract class EntityResourcesGenerator extends AbstractGenerator 
    implements FacadeGenerator 
{
    
    public static final String RESOURCE_FOLDER = "service";   //NOI18N

    public static final String CONTROLLER_FOLDER = "controller";      //NOI18N

    public static final String RESOURCE_SUFFIX = GenericResourceBean.RESOURCE_SUFFIX;

    protected static final String REST_FACADE_SUFFIX = "RESTFacade";            //NOI18N 

    private PersistenceUnit persistenceUnit;
    private String targetPackageName;
    private FileObject targetFolder;
    private FileObject resourceFolder;
    private String resourcePackageName;
    private FileObject controllerFolder;
    private String controllerPackageName;
    private EntityResourceBeanModel model;
    private Project project;
    private boolean jakartaNamespace;
    
    /** Creates a new instance of EntityRESTServicesCodeGenerator */
    public void initialize(EntityResourceBeanModel model, Project project,
            FileObject targetFolder, String targetPackageName,
            String resourcePackage, String controllerPackage,
            PersistenceUnit persistenceUnit) {
        this.model = model;
        this.project = project;
        this.persistenceUnit = persistenceUnit;
        this.targetFolder = targetFolder;
        this.targetPackageName = targetPackageName;

        if (resourcePackage == null) {
            this.resourcePackageName = targetPackageName + "." + RESOURCE_FOLDER;
        } else {
            this.resourcePackageName = resourcePackage;
        }

        if (controllerPackage == null) {
            this.controllerPackageName = targetPackageName + "." + CONTROLLER_FOLDER;
        } else {
            this.controllerPackageName = controllerPackage;
        }

        ClassPath cp = ClassPath.getClassPath(targetFolder, ClassPath.COMPILE);
        boolean jakartaRSPresent = cp.findResource("jakarta/ws/rs/GET.class") != null;
        boolean javaxRSPresent = cp.findResource("javax/ws/rs/GET.class") != null;
        jakartaNamespace = jakartaRSPresent || (! javaxRSPresent);
    }
    
    /**
     * Generates RESTful resources
     * @param pHandle ProgressHandle. May be null, e.g., if method is called in conjunction with other generators that have ProgressHandles
     * that are already running.
     * @return
     * @throws java.io.IOException
     */
    @Override
    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        if (pHandle != null) {
            initProgressReporting(pHandle);
        }

        createFolders();

        //Make necessary changes to the persistence.xml
        new PersistenceHelper(project).configure(model.getBuilder().getAllEntityNames(),
                !RestUtils.hasJTASupport(project));

        //
        //Delegate to J2eeEntityResourcesGenerator or SpringEntityResourceGenerator to
        // perform the rest of the persistence configuration.
        //
        configurePersistence();

        Set<String> fqnEntities = new HashSet<String>();
        for (EntityClassInfo info : model.getEntityInfos()) {
            if ( !fqnEntities.contains( info.getEntityFqn() )){
                fqnEntities.add( info.getEntityFqn() );
                Util.modifyEntity( info.getEntityFqn() , project);
            }
        }
        
        preGenerate(new ArrayList<String>( fqnEntities ));
        
        Util.generateRESTFacades(project, fqnEntities, model, resourceFolder, 
                getResourcePackageName(), this);

        finishProgressReporting();

        return new HashSet<FileObject>();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.persistence.wizard.fromdb.FacadeGenerator#generate(org.netbeans.api.project.Project, java.util.Map, org.openide.filesystems.FileObject, java.lang.String, java.lang.String, java.lang.String, boolean, boolean, boolean)
     */
    @Override
    public Set<FileObject> generate( Project project,
            Map<String, String> entityNames, FileObject targetFolder,
            String entityFQN, String idClass, String pkg, boolean hasRemote,
            boolean hasLocal, boolean overrideExisting ) throws IOException
    {
        final Set<FileObject> createdFiles = new HashSet<FileObject>();
        final String entitySimpleName = JavaIdentifiers.unqualify(entityFQN);
        
        // create the facade
        String resourceName = entitySimpleName + REST_FACADE_SUFFIX;
        reportProgress( resourceName );
        FileObject existingFO = targetFolder.getFileObject(resourceName, "java");
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
        
        if ( !generateInfrastracture(createdFiles, entityFQN, facade) ){
            return createdFiles;
        }
        
        generateResourceMethods( facade , entityFQN , idClass );
        
        return createdFiles;
    }
    
    protected List<String> getResourceImports( String entityFqn ) {
        List<String> result = new LinkedList<String>();
        result.add( entityFqn );
        result.add( URI.class.getCanonicalName() );
        return result;
    }

    protected boolean  generateInfrastracture( final Set<FileObject> createdFiles,
            final String entityFqn, final FileObject facade )
            throws IOException
    {
        Util.generatePrimaryKeyMethod(facade, entityFqn, model);
        return true;
    }

    protected void preGenerate( List<String> fqnEntities ) throws IOException {
    }
    
    protected ModifiersTree addRestMethodAnnotations( GenerationUtils genUtils,
            TreeMaker maker, RestGenerationOptions option,
            ModifiersTree modifiers )
    {
        String method = option.getRestMethod().getMethod(isJakartaNamespace());
        ModifiersTree modifiersTree = modifiers;
        if ( method != null ){
            modifiersTree = maker.addModifiersAnnotation(modifiers, 
                        genUtils.createAnnotation(method));
        }

        // add @Path annotation
        String uriPath = option.getRestMethod().getUriPath();
        if (uriPath != null) {
            ExpressionTree uriValue = maker.Literal(uriPath);
            modifiersTree =
                    maker.addModifiersAnnotation(modifiersTree,
                    genUtils.createAnnotation(
                            isJakartaNamespace() ? RestConstants.PATH_JAKARTA : RestConstants.PATH,
                            Collections.<ExpressionTree>singletonList(uriValue)));

        }
        
        // add @Override
        if ( option.getRestMethod().overrides() ){
            modifiersTree =
                maker.addModifiersAnnotation(modifiersTree,
                genUtils.createAnnotation(
                        Override.class.getCanonicalName()));
        }
        
        // add @Produces annotation
        modifiersTree = addMimeHandlerAnnotation(genUtils, maker,
                modifiersTree, isJakartaNamespace() ? RestConstants.PRODUCE_MIME_JAKARTA : RestConstants.PRODUCE_MIME, option.getProduces());
        
        // add @Consumes annotation
        modifiersTree = addMimeHandlerAnnotation(genUtils, maker,
                modifiersTree, isJakartaNamespace() ? RestConstants.CONSUME_MIME_JAKARTA : RestConstants.CONSUME_MIME, option.getConsumes());
        return modifiersTree;
    }
    
    protected ModifiersTree addResourceAnnotation(
            final String entityFQN, ClassTree classTree,
            GenerationUtils genUtils, TreeMaker maker )
    {
        // Add @Path annotation to REST resource class
        ExpressionTree resourcePath = maker.Literal(entityFQN.toLowerCase());
        ModifiersTree modifiersTree = classTree.getModifiers();
        modifiersTree =
                maker.addModifiersAnnotation(modifiersTree, 
                        genUtils.createAnnotation(RestConstants.PATH, 
                                Collections.<ExpressionTree>singletonList(
                                        resourcePath)));
        return modifiersTree;
    }
    
    protected List<RestGenerationOptions> getRestFacadeMethodOptions(
            String entityFQN, String idClass )
    {
        String paramArg;
        String idType = idClass;
        if ( Character.class.getCanonicalName().equals(idClass) ){
            paramArg = "id.charAt(0)";          // NOI18N
            idType = String.class.getCanonicalName();
        }
        else {
            paramArg = "id";                    // NOI18N
        }
        RestFacadeMethod[] methods = RestFacadeMethod.values();
        List<RestGenerationOptions> result = new ArrayList<RestGenerationOptions>( 
                methods.length);
        for (RestFacadeMethod method : methods) {
            RestGenerationOptions options = getGenerationOptions( method , 
                    entityFQN, paramArg , idType );
            result.add( options );
        }
        return result;
    }
    
    protected void generateRestMethod( TypeElement classElement,
            GenerationUtils genUtils, TreeMaker maker,
            List<Tree> members, RestGenerationOptions option )
    {
        ModifiersTree paramModifier = maker.Modifiers(
                Collections.<Modifier>emptySet());
        
        ModifiersTree modifiers ;
        if ( option.getRestMethod().getMethod(true) == null ){
            modifiers = genUtils.createModifiers(
                    Modifier.PRIVATE);
        }
        else {
            modifiers = genUtils.createModifiers(
                    Modifier.PUBLIC);
        }
        ModifiersTree modifiersTree = addRestMethodAnnotations(genUtils, maker,
                option, modifiers);

        // create arguments list
        List<VariableTree> vars = addRestArguments(classElement, genUtils,
                maker, option, paramModifier);

        Tree returnType = (option.getReturnType() == null || 
                option.getReturnType().equals("void"))?  //NOI18N
                                maker.PrimitiveType(TypeKind.VOID):
                                genUtils.createType(
                                        option.getReturnType(), 
                                        classElement);

        members.add(
                    maker.Method(
                    modifiersTree,
                    option.getRestMethod().getMethodName(),
                    returnType,
                    Collections.<TypeParameterTree>emptyList(),
                    vars,
                    Collections.<ExpressionTree>emptyList(),
                    "{"+option.getBody()+"}",               //NOI18N
                    null)
                );
    }
    
    protected List<VariableTree> addRestArguments( TypeElement classElement,
            GenerationUtils genUtils, TreeMaker maker,
            RestGenerationOptions option, ModifiersTree paramModifier )
    {
        List<VariableTree> vars = new ArrayList<VariableTree>();
        String[] paramNames = option.getParameterNames();
        int paramLength = paramNames == null ? 0 : option.getParameterNames().length ;

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
                                        isJakartaNamespace() ? RestConstants.PATH_PARAM_JAKARTA : RestConstants.PATH_PARAM,
                                        annArguments));
                }
                Tree paramTree = genUtils.createType(paramTypes[i], 
                        classElement);
                VariableTree var = maker.Variable(pathParamTree, 
                        paramNames[i], paramTree, null); 
                vars.add(var);

            }
        }
        return vars;
    }
    
    protected void createFolders(boolean createController) {
        FileObject sourceRootFolder = getSourceRootFolder(targetFolder, 
                targetPackageName);
        File sourceRootDir = FileUtil.toFile(sourceRootFolder);
        try {
            String resourceFolderPath = toFilePath(getResourcePackageName());
            resourceFolder = sourceRootFolder.getFileObject(resourceFolderPath);
            if (resourceFolder == null) {
                resourceFolder = FileUtil.createFolder(new File(sourceRootDir, 
                        resourceFolderPath));
            }

            if (createController) {
                String controllerFolderPath = toFilePath(controllerPackageName);
                controllerFolder = sourceRootFolder
                        .getFileObject(controllerFolderPath);
                if (controllerFolder == null) {
                    controllerFolder = FileUtil.createFolder(new File(
                            sourceRootDir, controllerFolderPath));
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    protected void configurePersistence() {
    }
    
    protected RestGenerationOptions getGenerationOptions(
            RestFacadeMethod method, String entityFQN, String paramArg,
            String idType )
    {
        boolean needPathSegment = false;
        EntityClassInfo entityInfo = model.getEntityInfo(entityFQN);
        if ( entityInfo!= null ){
            FieldInfo idFieldInfo = entityInfo.getIdFieldInfo();
            needPathSegment = idFieldInfo!= null && idFieldInfo.isEmbeddedId()&& 
                    idFieldInfo.getType()!= null;
        }
        String httpResponseType;
        String pathSegmentType;
        if (isJakartaNamespace()) {
            httpResponseType = RestConstants.HTTP_RESPONSE_JAKARTA;
            pathSegmentType = "jakarta.ws.rs.core.PathSegment";
        } else {
            httpResponseType = RestConstants.HTTP_RESPONSE;
            pathSegmentType = "javax.ws.rs.core.PathSegment";
        }
        RestGenerationOptions options = new RestGenerationOptions();
        switch ( method ){
            case CREATE:
                options.setRestMethod(RestFacadeMethod.CREATE);
                options.setReturnType(httpResponseType);
                options.setParameterNames(new String[]{"entity"}); //NOI18N
                options.setParameterTypes(new String[]{entityFQN});
                options.setConsumes(new String[]{Constants.MimeType.XML.value(), 
                        Constants.MimeType.JSON.value()}); 
                return options;
            case EDIT:
                options.setRestMethod(RestFacadeMethod.EDIT);
                options.setReturnType(httpResponseType);
                options.setParameterNames(new String[]{"entity"}); //NOI18N
                options.setParameterTypes(new String[]{entityFQN}); //NOI18N
                options.setConsumes(new String[]{Constants.MimeType.XML.value(), 
                        Constants.MimeType.JSON.value()}); 
                return options;
            case REMOVE:
                options.setRestMethod(RestFacadeMethod.REMOVE);
                options.setReturnType(httpResponseType);
                options.setParameterNames(new String[]{"id"}); //NOI18N
                if ( needPathSegment ){
                    options.setParameterTypes(new String[]{pathSegmentType}); //NOI18N
                }
                else {
                    options.setParameterTypes(new String[]{idType}); //NOI18N
                }
                options.setPathParams(new String[]{"id"}); //NOI18N
                return options;
            case FIND:
                options.setRestMethod(RestFacadeMethod.FIND);
                options.setReturnType(entityFQN);
                options.setProduces(new String[]{Constants.MimeType.XML.value(), 
                        Constants.MimeType.JSON.value()}); 
                options.setParameterNames(new String[]{"id"}); //NOI18N
                if ( needPathSegment ){
                    options.setParameterTypes(new String[]{pathSegmentType}); //NOI18N
                }
                else {
                    options.setParameterTypes(new String[]{idType}); //NOI18N
                }
                options.setPathParams(new String[]{"id"}); //NOI18N
                return options;
            case FIND_ALL:
                options.setRestMethod(RestFacadeMethod.FIND_ALL);
                options.setReturnType(List.class.getCanonicalName()
                        +"<" + entityFQN + ">");//NOI18N
                options.setProduces(new String[]{Constants.MimeType.XML.value(), 
                        Constants.MimeType.JSON.value()});
                return options;
            case FIND_RANGE:
                options.setRestMethod(RestFacadeMethod.FIND_RANGE);
                options.setReturnType(List.class.getCanonicalName()+
                        "<" + entityFQN + ">");//NOI18N
                options.setProduces(new String[]{Constants.MimeType.XML.value(), 
                        Constants.MimeType.JSON.value()}); 
                options.setParameterNames(new String[]{"max", "first"}); //NOI18N
                options.setParameterTypes(new String[]{Integer.class.getCanonicalName(), 
                        Integer.class.getCanonicalName()}); 
                options.setPathParams(new String[]{"max", "first"}); //NOI18N
                return options;
            case COUNT:
                options.setRestMethod(RestFacadeMethod.COUNT);
                options.setReturnType(String.class.getCanonicalName());
                options.setProduces(new String[]{Constants.MimeType.TEXT.value()}); 
                return options;
        }
        return null;
    }

    protected EntityClassInfo getEntityClassInfo(String className) {
        return model.getBuilder().getEntityClassInfo(className);
    }

    protected Project getProject(){
        return project;
    }
    
    protected EntityResourceBeanModel getModel(){
        return model;
    }
    
    protected FileObject getTargetFolder(){
        return targetFolder;
    }
    
    protected FileObject getResourceFolder(){
        return resourceFolder;
    }
    
    protected PersistenceUnit getPersistenceUnit(){
        return persistenceUnit;
    }

    protected String getResourcePackageName() {
        return resourcePackageName;
    }

    protected String getControllerPackageName() {
        return controllerPackageName;
    }
    
    protected FileObject getControllerFolder() {
        return controllerFolder;
    }
    
    protected int getEntitiesCount(){
        return getModel().getEntityInfos().size();
    }
    
    protected String getIdFieldToUriStmt(FieldInfo idField) {
        String getterName = getGetterName(idField);

        if (idField.isEmbeddedId()) {
            Collection<FieldInfo> fields = idField.getFieldInfos();
            StringBuilder stmt = new StringBuilder();
            int index = 0;

            for (FieldInfo f : fields) {
                if (index++ > 0) {
                    stmt.append(" + \",\" + ");             // NOI18N
                }
                stmt.append( "entity." );                   // NOI18N
                stmt.append( getterName );
                stmt.append( "()." );                       // NOI18N
                stmt.append( getGetterName(f) );
                stmt.append(  "()" );                       // NOI18N
            }

            return stmt.toString();
        } else {
            return "entity." + getterName + "()";           // NOI18N
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.rest.codegen.AbstractGenerator#getTotalWorkUnits()
     */
    @Override
    protected int getTotalWorkUnits() {
        return getEntitiesCount();
    }
    
    protected void createFolders() {
        createFolders( true );
    }

    protected boolean isJakartaNamespace() {
        return jakartaNamespace;
    }

    private void generateResourceMethods( FileObject fileObject , 
            final String entityFQN, final String idClass) throws IOException 
    {
        JavaSource javaSource = JavaSource.forFileObject( fileObject );
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            @Override
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree tree = workingCopy.getCompilationUnit();
                
                ClassTree classTree = (ClassTree)tree.getTypeDecls().get(0);
                TypeElement classElement = (TypeElement)workingCopy.getTrees().
                    getElement(TreePath.getPath( tree, classTree));
                
                List<String> imports = getResourceImports( entityFQN );
                JavaSourceHelper.addImports(workingCopy, imports.toArray(new String[0]));
                
                GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                TreeMaker maker = workingCopy.getTreeMaker();
                
                List<Tree> members = new ArrayList<Tree>(classTree.getMembers());
                // make empty CTOR
                MethodTree constructor = maker.Constructor(
                        genUtils.createModifiers(Modifier.PUBLIC),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        "{}");                                      //NOI18N
                members.add(constructor);
                
                List<RestGenerationOptions> restGenerationOptions = 
                    getRestFacadeMethodOptions(entityFQN, idClass);
                
                for(RestGenerationOptions option: restGenerationOptions) {
                    generateRestMethod(classElement, genUtils, maker, members,
                            option);
                }
                
                ModifiersTree modifiersTree = addResourceAnnotation(entityFQN,
                        classTree, genUtils, maker);
                
                // final step : generate new class tree
                List<Tree> implementsClause = new ArrayList<Tree>(
                        classTree.getImplementsClause());
                ClassTree newClassTree = maker.Class(
                        modifiersTree,
                        classTree.getSimpleName(),
                        classTree.getTypeParameters(),
                        classTree.getExtendsClause(),
                        implementsClause,
                        members);

                workingCopy.rewrite(classTree, newClassTree);
            }

        };
        javaSource.runModificationTask(task).commit();
    }
    
    private ModifiersTree addMimeHandlerAnnotation(GenerationUtils genUtils,
            TreeMaker maker, ModifiersTree modifiersTree, String handlerAnnotation, String[] mimes) {
        if (mimes == null) {
            return modifiersTree;
        }
        ExpressionTree annArguments;
        if (mimes.length == 1) {
            annArguments = mimeTypeTree(maker, mimes[0]);
        } else {
            List<ExpressionTree> mimeTypes = new ArrayList<ExpressionTree>();
            for (int i=0; i< mimes.length; i++) {
                mimeTypes.add(mimeTypeTree(maker, mimes[i]));
            }
            annArguments = maker.NewArray(null, 
                    Collections.<ExpressionTree>emptyList(), 
                    mimeTypes);
        }
        return maker.addModifiersAnnotation(modifiersTree,
                genUtils.createAnnotation(
                        handlerAnnotation, 
                        Collections.<ExpressionTree>singletonList(
                                annArguments)));
    }

    private ExpressionTree mimeTypeTree(TreeMaker maker, String mimeType) {
        Constants.MimeType type = Constants.MimeType.find(mimeType);
        ExpressionTree result;
        if (type == null) {
            result = maker.Literal(mimeType);
        } else {
            result = type.expressionTree(maker, isJakartaNamespace());
        }
        return result;
    }
    
    private String getGetterName(FieldInfo fieldInfo) {
        return Util.getGetterName(fieldInfo);
    }
    
    private String toFilePath(String packageName) {
        return packageName.replace('.', '/');
    }

    private FileObject getSourceRootFolder(FileObject packageFolder, 
            String packageName) 
    {
        String[] segments = packageName.split("\\.");       // NOI18N
        FileObject ret = packageFolder;
        for (int i = segments.length - 1; i >= 0; i--) {
            String segment = segments[i];

            if (segment.length() == 0) {
                return ret;
            }

            if (ret == null || !segments[i].equals(ret.getNameExt())) {
                throw new IllegalArgumentException("Unmatched folder: " + 
                        packageFolder.getPath() + " and package name: " + 
                        packageName);       // NOI18N
            }
            ret = ret.getParent();
        }
        return ret;
    }
    
}
