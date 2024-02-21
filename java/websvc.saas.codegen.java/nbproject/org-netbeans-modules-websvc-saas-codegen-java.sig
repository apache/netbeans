#Signature file v4.1
#Version 1.53

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public abstract interface org.netbeans.api.java.source.CancellableTask<%0 extends java.lang.Object>
intf org.netbeans.api.java.source.Task<{org.netbeans.api.java.source.CancellableTask%0}>
meth public abstract void cancel()

CLSS public abstract interface org.netbeans.api.java.source.Task<%0 extends java.lang.Object>
meth public abstract void run({org.netbeans.api.java.source.Task%0}) throws java.lang.Exception

CLSS public abstract org.netbeans.modules.websvc.saas.codegen.SaasClientAuthenticationGenerator
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,org.netbeans.api.project.Project)
meth public abstract java.lang.String getLoginBody(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract java.lang.String getLogoutBody()
meth public abstract java.lang.String getPostAuthenticationCode()
meth public abstract java.lang.String getPreAuthenticationCode()
meth public abstract java.lang.String getSignParamUsage(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,java.lang.String)
meth public abstract java.lang.String getTokenBody(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract void createAuthenticatorClass() throws java.io.IOException
meth public abstract void createAuthorizationClasses() throws java.io.IOException
meth public abstract void modifyAuthenticationClass() throws java.io.IOException
meth public abstract void modifyAuthenticationClass(java.lang.String,java.lang.Object[],java.lang.Object,java.lang.String,java.lang.String[],java.lang.Object[],java.lang.Object[],java.lang.String) throws java.io.IOException
meth public java.lang.String getAuthenticationProfile()
meth public java.lang.String getLoginArguments()
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getAuthenticatorMethodParameters()
meth public org.netbeans.api.project.Project getProject()
meth public org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType getDropFileType()
meth public org.netbeans.modules.websvc.saas.codegen.Constants$SaasAuthenticationType getAuthenticationType() throws java.io.IOException
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean getBean()
meth public org.openide.filesystems.FileObject getSaasServiceFolder() throws java.io.IOException
meth public void setAuthenticationProfile(java.lang.String)
meth public void setAuthenticatorMethodParameters(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth public void setDropFileType(org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType)
meth public void setLoginArguments(java.lang.String)
meth public void setSaasServiceFolder(org.openide.filesystems.FileObject) throws java.io.IOException
supr java.lang.Object
hfds authMethodParams,authProfile,bean,dropFileType,loginArgs,project,serviceFolder

CLSS public abstract org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator
cons public init()
fld public final static java.lang.String COMMENT_END_OF_HTTP_MEHTOD_GET = "TODO return proper representation object"
fld public final static java.lang.String CONVERTER_FOLDER = "converter"
fld public final static java.lang.String CONVERTER_SUFFIX = "Converter"
fld public final static java.lang.String GENERIC_REF_CONVERTER = "GenericRefConverter"
fld public final static java.lang.String GENERIC_REF_CONVERTER_TEMPLATE = "Templates/SaaSServices/RefConverter.java"
fld public final static java.lang.String INDENT = "        "
fld public final static java.lang.String INDENT_2 = "             "
fld public final static java.lang.String RESOURCE_SUFFIX = "Resource"
fld public final static java.lang.String REST_CONNECTION = "RestConnection"
fld public final static java.lang.String REST_CONNECTION_PACKAGE = "org.netbeans.saas"
fld public final static java.lang.String REST_CONNECTION_TEMPLATE = "Templates/SaaSServices/RestConnection.java"
fld public final static java.lang.String REST_RESPONSE = "RestResponse"
fld public final static java.lang.String REST_RESPONSE_TEMPLATE = "Templates/SaaSServices/RestResponse.java"
fld public final static java.lang.String SAAS_SERVICES = "SaaSServices"
fld public final static java.lang.String TEMPLATES_SAAS = "Templates/SaaSServices/"
fld public final static java.lang.String VAR_NAMES_RESULT_DECL = "RestResponse result"
intf org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider
meth protected abstract java.lang.String getCustomMethodBody() throws java.io.IOException
meth protected boolean isInBlock(javax.swing.text.Document)
meth protected int insert(java.lang.String,int,int,javax.swing.text.Document,boolean) throws javax.swing.text.BadLocationException
meth protected java.lang.Object[] getParamValues(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected java.lang.String findNewName(java.lang.String,java.lang.String)
meth protected java.lang.String getParameterName(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo)
meth protected java.lang.String getParameterName(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo,boolean,boolean)
meth protected java.lang.String getParameterName(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo,boolean,boolean,boolean)
meth protected java.lang.String getResultPattern()
meth protected java.lang.String getVariableName(java.lang.String)
meth protected java.lang.String getVariableName(java.lang.String,boolean,boolean,boolean)
meth protected java.lang.String[] getGetParamNames(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected java.lang.String[] getGetParamTypes(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected java.lang.String[] getParamNames(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected java.lang.String[] getParamTypeNames(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> renameParameterNames(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected javax.swing.text.Document getTargetDocument()
meth protected org.netbeans.api.progress.ProgressHandle getProgressHandle()
meth protected org.netbeans.api.project.Project getProject()
meth protected org.openide.filesystems.FileObject getTargetFile()
meth protected org.openide.filesystems.FileObject getTargetFolder()
meth protected void addVariablePattern(java.lang.String,int)
meth protected void clearVariablePatterns()
meth protected void insert(java.lang.String,boolean) throws javax.swing.text.BadLocationException
meth protected void preGenerate() throws java.io.IOException
meth protected void reformat(javax.swing.text.Document,int,int) throws javax.swing.text.BadLocationException
meth protected void updateVariableNames(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected void updateVariableNamesForWS(java.util.List<? extends org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter>)
meth public abstract boolean canAccept(org.netbeans.modules.websvc.saas.model.SaasMethod,javax.swing.text.Document)
meth public int getEndPosition()
meth public int getPrecedence()
meth public int getStartPosition()
meth public int getTotalWorkUnits()
meth public java.lang.String getVariableDecl(org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter)
meth public java.lang.String getVariableDecl(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo)
meth public java.util.Set<org.openide.filesystems.FileObject> generate() throws java.io.IOException
meth public org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType getDropFileType()
meth public org.netbeans.modules.websvc.saas.codegen.model.SaasBean getBean()
meth public void copyFile(java.lang.String,java.io.File) throws java.io.IOException
meth public void finishProgressReporting()
meth public void init(org.netbeans.modules.websvc.saas.model.SaasMethod,javax.swing.text.Document) throws java.io.IOException
meth public void initProgressReporting(org.netbeans.api.progress.ProgressHandle)
meth public void initProgressReporting(org.netbeans.api.progress.ProgressHandle,boolean)
meth public void reportProgress(java.lang.String)
meth public void setBean(org.netbeans.modules.websvc.saas.codegen.model.SaasBean)
meth public void setDropFileType(org.netbeans.modules.websvc.saas.codegen.Constants$DropFileType)
meth public void setDropLocation(javax.swing.text.JTextComponent)
meth public void setEndPosition(int)
meth public void setPrecedence(int)
meth public void setStartPosition(int)
supr java.lang.Object
hfds bean,destDir,dropFileType,end,nFinder,pHandle,precedence,project,start,targetDocument,targetFile,totalWorkUnits,workUnits

CLSS public org.netbeans.modules.websvc.saas.codegen.java.CustomClientPojoCodeGenerator
cons public init()
fld public final static java.lang.String VAR_NAMES_RESULT_DECL = "RestResponse result"
meth protected java.lang.String getCustomMethodBody() throws java.io.IOException
meth protected java.lang.String getHeaderOrParameterDeclaration(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected java.lang.String getHeaderOrParameterDeclaration(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,java.lang.String)
meth protected java.lang.String getLoginArguments()
meth protected java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getAuthenticatorMethodParameters()
meth protected java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getServiceMethodParameters()
meth protected org.netbeans.api.java.source.JavaSource getTargetSource()
meth protected void addImportsToTargetFile() throws java.io.IOException
meth protected void createRestConnectionFile(org.netbeans.api.project.Project) throws java.io.IOException
meth protected void insertSaasServiceAccessCode(boolean) throws java.io.IOException
meth protected void preGenerate() throws java.io.IOException
meth public boolean canAccept(org.netbeans.modules.websvc.saas.model.SaasMethod,javax.swing.text.Document)
meth public java.util.Set<org.openide.filesystems.FileObject> generate() throws java.io.IOException
meth public org.netbeans.modules.websvc.saas.codegen.SaasClientAuthenticationGenerator getAuthenticationGenerator()
meth public org.netbeans.modules.websvc.saas.codegen.model.CustomClientSaasBean getBean()
meth public org.openide.filesystems.FileObject getSaasServiceFolder() throws java.io.IOException
meth public static java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getAuthenticatorMethodParametersForWeb()
meth public static java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getServiceMethodParametersForWeb(org.netbeans.modules.websvc.saas.codegen.model.CustomClientSaasBean)
meth public void init(org.netbeans.modules.websvc.saas.model.SaasMethod,javax.swing.text.Document) throws java.io.IOException
meth public void init(org.netbeans.modules.websvc.saas.model.SaasMethod,org.netbeans.modules.websvc.saas.codegen.model.CustomClientSaasBean,javax.swing.text.Document) throws java.io.IOException
supr org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator
hfds authGen,serviceFolder,targetSource

CLSS public org.netbeans.modules.websvc.saas.codegen.java.RestClientPojoCodeGenerator
cons public init()
meth protected java.lang.String getCustomMethodBody() throws java.io.IOException
meth protected java.lang.String getFixedParameterDeclaration()
meth protected java.lang.String getHeaderOrParameterDeclaration(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>)
meth protected java.lang.String getHeaderOrParameterDeclaration(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,java.lang.String)
meth protected java.lang.String getLoginArguments()
meth protected java.lang.String getServiceMethodBody() throws java.io.IOException
meth protected java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getAuthenticatorMethodParameters()
meth protected java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getServiceMethodParameters()
meth protected org.netbeans.api.java.source.JavaSource getTargetSource()
meth protected void addImportsToSaasService() throws java.io.IOException
meth protected void addImportsToTargetFile() throws java.io.IOException
meth protected void addJaxbLib() throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth protected void addSaasServiceMethod() throws java.io.IOException
meth protected void createRestConnectionFile(org.netbeans.api.project.Project) throws java.io.IOException
meth protected void insertSaasServiceAccessCode(boolean) throws java.io.IOException
meth protected void preGenerate() throws java.io.IOException
meth public boolean canAccept(org.netbeans.modules.websvc.saas.model.SaasMethod,javax.swing.text.Document)
meth public java.util.Set<org.openide.filesystems.FileObject> generate() throws java.io.IOException
meth public org.netbeans.api.java.source.JavaSource getSaasServiceSource()
meth public org.netbeans.modules.websvc.saas.codegen.SaasClientAuthenticationGenerator getAuthenticationGenerator()
meth public org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo findParameter(java.lang.String)
meth public org.netbeans.modules.websvc.saas.codegen.model.RestClientSaasBean getBean()
meth public org.openide.filesystems.FileObject getSaasServiceFolder() throws java.io.IOException
meth public void createSaasServiceClass() throws java.io.IOException
meth public void init(org.netbeans.modules.websvc.saas.model.SaasMethod,javax.swing.text.Document) throws java.io.IOException
meth public void init(org.netbeans.modules.websvc.saas.model.SaasMethod,org.netbeans.modules.websvc.saas.codegen.model.RestClientSaasBean,javax.swing.text.Document) throws java.io.IOException
supr org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator
hfds authGen,saasServiceFile,saasServiceJS,serviceFolder,targetSource

CLSS public org.netbeans.modules.websvc.saas.codegen.java.SaasClientJavaAuthenticationGenerator
cons public init(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,org.netbeans.api.project.Project)
meth public java.lang.String getLoginBody(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,java.lang.String,java.lang.String) throws java.io.IOException
meth public java.lang.String getLogoutBody()
meth public java.lang.String getPostAuthenticationCode()
meth public java.lang.String getPreAuthenticationCode()
meth public java.lang.String getSignParamUsage(java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,java.lang.String)
meth public java.lang.String getTokenBody(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public void createAuthenticatorClass() throws java.io.IOException
meth public void createAuthorizationClasses() throws java.io.IOException
meth public void modifyAuthenticationClass() throws java.io.IOException
meth public void modifyAuthenticationClass(java.lang.String,java.lang.Object[],java.lang.Object,java.lang.String,java.lang.String[],java.lang.Object[],java.lang.Object[],java.lang.String) throws java.io.IOException
supr org.netbeans.modules.websvc.saas.codegen.SaasClientAuthenticationGenerator
hfds callbackFile,callbackJS,loginFile,loginJS,saasAuthFile,saasAuthJS

CLSS public org.netbeans.modules.websvc.saas.codegen.java.SoapClientPojoCodeGenerator
cons public init()
fld public final static java.lang.String HEADERS = "com.sun.xml.ws.api.message.Headers"
fld public final static java.lang.String HINT_INIT_ARGUMENTS = " // TODO initialize WS operation arguments here\n"
fld public final static java.lang.String QNAME = "javax.xml.namespace.QName"
fld public final static java.lang.String SET_HEADER_PARAMS = "setHeaderParameters"
fld public final static java.lang.String SET_HEADER_PARAMS_CALL = "setHeaderParameters(port); \n"
fld public final static java.lang.String VAR_NAMES_PORT = "port"
fld public final static java.lang.String VAR_NAMES_SERVICE = "service"
fld public final static java.lang.String WS_BINDING_PROVIDER = "com.sun.xml.ws.developer.WSBindingProvider"
meth protected java.lang.String getCustomMethodBody() throws java.io.IOException
meth protected java.lang.String getJavaInvocationBody(org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation,boolean,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth protected java.lang.String getWSInvocationCode(org.netbeans.modules.websvc.saas.codegen.model.SoapClientOperationInfo) throws java.io.IOException
meth protected static java.lang.String findProperServiceFieldName(java.util.Set)
meth protected static java.lang.String resolveInitValue(java.lang.String)
meth protected static java.lang.String resolveInitValue(org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo)
meth protected static java.lang.String resolveResponseType(java.lang.String)
meth protected void insertSaasServiceAccessCode(boolean) throws java.io.IOException
meth protected void preGenerate() throws java.io.IOException
meth public boolean canAccept(org.netbeans.modules.websvc.saas.model.SaasMethod,javax.swing.text.Document)
meth public java.util.Set<org.openide.filesystems.FileObject> generate() throws java.io.IOException
meth public org.netbeans.modules.websvc.saas.codegen.model.SoapClientSaasBean getBean()
meth public void init(org.netbeans.modules.websvc.saas.model.SaasMethod,javax.swing.text.Document) throws java.io.IOException
supr org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator
hfds INVOKE_JAXRPC_BODY,JAVA_OUT,JAVA_PORT_DEF,JAVA_RESULT,JAVA_SERVICE_DEF,JAVA_STATIC_STUB_ASYNC_CALLBACK,JAVA_STATIC_STUB_ASYNC_POLLING,JAVA_VOID

CLSS public org.netbeans.modules.websvc.saas.codegen.java.WadlSaasEx
cons public init(org.netbeans.modules.websvc.saas.model.WadlSaas)
meth public java.util.List<org.openide.filesystems.FileObject> getJaxbSourceJars()
meth public java.util.List<org.openide.filesystems.FileObject> getLibraryJars()
supr java.lang.Object
hfds jaxbJars,jaxbSourceJars,wadlSaas

CLSS public abstract org.netbeans.modules.websvc.saas.codegen.java.support.AbstractTask<%0 extends java.lang.Object>
cons public init()
intf org.netbeans.api.java.source.CancellableTask<{org.netbeans.modules.websvc.saas.codegen.java.support.AbstractTask%0}>
meth public void cancel()
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper
cons public init()
meth public static boolean annotationHasAttributeValue(javax.lang.model.element.AnnotationMirror,java.lang.String)
meth public static boolean annotationHasAttributeValue(javax.lang.model.element.AnnotationMirror,java.lang.String,java.lang.String)
meth public static boolean isContainsConstuctor(org.netbeans.api.java.source.JavaSource,java.lang.String[],java.lang.Object[])
meth public static boolean isContainsMethod(org.netbeans.api.java.source.JavaSource,java.lang.String,java.lang.String[],java.lang.Object[])
meth public static boolean isEntity(org.netbeans.api.java.source.JavaSource)
meth public static boolean isInjectionTarget(org.netbeans.api.java.source.CompilationController) throws java.io.IOException
meth public static boolean isInjectionTarget(org.netbeans.api.java.source.CompilationController,javax.lang.model.element.TypeElement)
meth public static boolean isOfAnnotationType(javax.lang.model.element.AnnotationMirror,java.lang.String)
meth public static com.sun.source.tree.AssignmentTree createAssignmentTree(org.netbeans.api.java.source.WorkingCopy,java.lang.String,java.lang.Object)
meth public static com.sun.source.tree.ClassTree addConstructor(org.netbeans.api.java.source.WorkingCopy,com.sun.source.tree.ClassTree,javax.lang.model.element.Modifier[],java.lang.String[],java.lang.Object[],java.lang.String,java.lang.String)
meth public static com.sun.source.tree.ClassTree addField(org.netbeans.api.java.source.WorkingCopy,com.sun.source.tree.ClassTree,javax.lang.model.element.Modifier[],java.lang.String[],java.lang.Object[],java.lang.String,java.lang.Object)
meth public static com.sun.source.tree.ClassTree addField(org.netbeans.api.java.source.WorkingCopy,com.sun.source.tree.ClassTree,javax.lang.model.element.Modifier[],java.lang.String[],java.lang.Object[],java.lang.String,java.lang.Object,java.lang.Object)
meth public static com.sun.source.tree.ClassTree addMethod(org.netbeans.api.java.source.WorkingCopy,com.sun.source.tree.ClassTree,javax.lang.model.element.Modifier[],java.lang.String[],java.lang.Object[],java.lang.String,java.lang.Object,java.lang.String[],java.lang.Object[],java.lang.Object[],java.lang.Object[],java.lang.Object[],java.lang.String,java.lang.String)
meth public static com.sun.source.tree.ClassTree addMethod(org.netbeans.api.java.source.WorkingCopy,com.sun.source.tree.ClassTree,javax.lang.model.element.Modifier[],java.lang.String[],java.lang.Object[],java.lang.String,java.lang.Object,java.lang.String[],java.lang.Object[],java.lang.Object[],java.lang.Object[],java.lang.String,java.lang.String)
meth public static com.sun.source.tree.ClassTree findPublicTopLevelClass(org.netbeans.api.java.source.CompilationController) throws java.io.IOException
meth public static com.sun.source.tree.ClassTree getClassTree(org.netbeans.api.java.source.CompilationController,javax.lang.model.element.TypeElement)
meth public static com.sun.source.tree.ClassTree getTopLevelClassTree(org.netbeans.api.java.source.CompilationController)
meth public static com.sun.source.tree.MethodTree getDefaultConstructor(org.netbeans.api.java.source.CompilationController)
meth public static com.sun.source.tree.MethodTree getMethodByName(org.netbeans.api.java.source.CompilationController,java.lang.String)
meth public static com.sun.source.tree.Tree createIdentifierTree(org.netbeans.api.java.source.WorkingCopy,java.lang.String)
meth public static com.sun.source.tree.Tree createParameterizedTypeTree(org.netbeans.api.java.source.WorkingCopy,java.lang.String,java.lang.String[])
meth public static com.sun.source.tree.VariableTree getField(org.netbeans.api.java.source.CompilationController,java.lang.String)
meth public static java.lang.String createFieldSignature(com.sun.source.tree.VariableTree)
meth public static java.lang.String createFieldSignature(java.lang.Object,java.lang.String)
meth public static java.lang.String createMethodSignature(com.sun.source.tree.MethodTree)
meth public static java.lang.String createMethodSignature(com.sun.source.tree.MethodTree,boolean)
meth public static java.lang.String createMethodSignature(java.lang.String,java.lang.Object[])
meth public static java.lang.String findUri(org.netbeans.api.java.source.JavaSource)
meth public static java.lang.String getClassName(org.netbeans.api.java.source.JavaSource) throws java.io.IOException
meth public static java.lang.String getClassNameQuietly(org.netbeans.api.java.source.JavaSource)
meth public static java.lang.String getClassType(org.netbeans.api.java.source.JavaSource) throws java.io.IOException
meth public static java.lang.String getIdFieldName(org.netbeans.api.java.source.JavaSource)
meth public static java.lang.String getPackageName(org.netbeans.api.java.source.JavaSource)
meth public static java.lang.String getShortTypeName(java.lang.Object)
meth public static java.lang.String getValueFromAnnotation(java.lang.String)
meth public static java.lang.String getValueFromAnnotation(javax.lang.model.element.AnnotationMirror)
meth public static java.util.Collection<java.lang.String> getAnnotationValuesForAllMethods(org.netbeans.api.java.source.JavaSource,java.lang.String)
meth public static java.util.Collection<java.lang.String> getImports(org.netbeans.api.java.source.CompilationController)
meth public static java.util.List<? extends com.sun.source.tree.Tree> getAllTree(org.netbeans.api.java.source.JavaSource)
meth public static java.util.List<? extends javax.lang.model.element.AnnotationMirror> getClassAnnotations(org.netbeans.api.java.source.JavaSource)
meth public static java.util.List<com.sun.source.tree.MethodTree> getAllConstuctors(org.netbeans.api.java.source.JavaSource)
meth public static java.util.List<com.sun.source.tree.MethodTree> getAllMethods(org.netbeans.api.java.source.JavaSource)
meth public static java.util.List<com.sun.source.tree.VariableTree> getAllFields(org.netbeans.api.java.source.JavaSource)
meth public static java.util.List<org.netbeans.api.java.source.JavaSource> getEntityClasses(org.netbeans.api.project.Project)
meth public static java.util.List<org.netbeans.api.java.source.JavaSource> getJavaSources(org.netbeans.api.project.Project)
meth public static javax.lang.model.element.AnnotationMirror findAnnotation(java.util.List<? extends javax.lang.model.element.AnnotationMirror>,java.lang.String)
meth public static javax.lang.model.element.ExecutableElement getLongestContructor(javax.lang.model.element.TypeElement)
meth public static javax.lang.model.element.ExecutableElement getLongestContructor(org.netbeans.api.java.source.JavaSource) throws java.io.IOException
meth public static javax.lang.model.element.TypeElement getTopLevelClassElement(org.netbeans.api.java.source.CompilationController)
meth public static javax.lang.model.element.TypeElement getTypeElement(org.netbeans.api.java.source.CompilationController,com.sun.source.tree.ClassTree)
meth public static javax.lang.model.element.TypeElement getTypeElement(org.netbeans.api.java.source.CompilationController,com.sun.source.util.TreePath)
meth public static javax.lang.model.element.TypeElement getTypeElement(org.netbeans.api.java.source.JavaSource) throws java.io.IOException
meth public static javax.lang.model.element.TypeElement getXmlRepresentationClass(javax.lang.model.element.TypeElement,java.lang.String)
meth public static long[] getPosition(org.netbeans.api.java.source.JavaSource,java.lang.String)
meth public static org.netbeans.api.java.source.JavaSource createJavaSource(java.lang.String,org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
meth public static org.netbeans.api.java.source.JavaSource createJavaSource(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
meth public static org.netbeans.api.java.source.JavaSource forTypeElement(javax.lang.model.element.TypeElement,org.netbeans.api.project.Project) throws java.io.IOException
meth public static void addClassAnnotation(org.netbeans.api.java.source.WorkingCopy,java.lang.String[],java.lang.Object[])
meth public static void addConstants(org.netbeans.api.java.source.WorkingCopy,java.lang.String[],java.lang.Object[],java.lang.Object[])
meth public static void addFields(org.netbeans.api.java.source.WorkingCopy,java.lang.String[],java.lang.Object[])
meth public static void addFields(org.netbeans.api.java.source.WorkingCopy,java.lang.String[],java.lang.Object[],java.lang.Object[])
meth public static void addFields(org.netbeans.api.java.source.WorkingCopy,java.lang.String[],java.lang.Object[],java.lang.Object[],javax.lang.model.element.Modifier[])
meth public static void addImports(org.netbeans.api.java.source.WorkingCopy,java.lang.String[])
meth public static void getAvailableConstructorSignature(org.netbeans.api.java.source.JavaSource,java.util.Map<java.lang.String,java.lang.String>)
meth public static void getAvailableFieldSignature(org.netbeans.api.java.source.JavaSource,java.util.Map<java.lang.String,java.lang.String>)
meth public static void getAvailableMethodSignature(org.netbeans.api.java.source.JavaSource,java.util.Map<java.lang.String,java.lang.String>)
meth public static void replaceFieldValue(org.netbeans.api.java.source.WorkingCopy,com.sun.source.tree.VariableTree,java.lang.String)
meth public static void replaceMethodBody(org.netbeans.api.java.source.WorkingCopy,com.sun.source.tree.MethodTree,java.lang.String)
meth public static void saveSource(org.openide.filesystems.FileObject[]) throws java.io.IOException
supr java.lang.Object
hfds CLASS_TEMPLATE,INTERFACE_TEMPLATE,JAVA_EXT

CLSS public org.netbeans.modules.websvc.saas.codegen.java.support.JavaUtil
cons public init()
fld public final static javax.lang.model.element.Modifier[] PRIVATE
fld public final static javax.lang.model.element.Modifier[] PRIVATE_STATIC
fld public final static javax.lang.model.element.Modifier[] PROTECTED
fld public final static javax.lang.model.element.Modifier[] PUBLIC
fld public final static javax.lang.model.element.Modifier[] PUBLIC_STATIC
fld public final static javax.lang.model.element.Modifier[] PUBLIC_STATIC_FINAL
meth public static boolean isJava(org.openide.loaders.DataObject)
meth public static boolean isProjectOpened(org.netbeans.api.project.Project)
meth public static java.lang.Class getType(org.netbeans.api.project.Project,java.lang.String)
meth public static org.netbeans.api.project.SourceGroup getClassSourceGroup(org.netbeans.api.project.Project,java.lang.String)
meth public static org.netbeans.api.project.SourceGroup[] getSourceGroups(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.websvc.saas.codegen.java.support.SoapClientJavaOperationInfo[] toJaxwsOperationInfos(org.netbeans.modules.websvc.saas.model.WsdlSaasMethod,org.netbeans.api.project.Project)
meth public static void addClientJars(org.netbeans.modules.websvc.saas.codegen.model.SaasBean,org.netbeans.api.project.Project,org.openide.filesystems.FileObject) throws java.io.IOException
meth public static void addImportsToSource(org.netbeans.api.java.source.JavaSource,java.util.List<java.lang.String>) throws java.io.IOException
meth public static void addInputParamField(org.netbeans.api.java.source.JavaSource,org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo,java.lang.String[],java.lang.Object[]) throws java.io.IOException
meth public static void addInputParamFields(org.netbeans.api.java.source.JavaSource,java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>) throws java.io.IOException
meth public static void addInputParamFields(org.netbeans.api.java.source.JavaSource,java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo>,javax.lang.model.element.Modifier[]) throws java.io.IOException
meth public static void addJaxbLib(org.netbeans.api.project.Project) throws java.io.IOException
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds JAXB_LIB

CLSS public org.netbeans.modules.websvc.saas.codegen.java.support.LibrariesHelper
cons public init()
fld public final static java.lang.String PATH_LIBRARIES = "lib"
fld public final static java.lang.String WEBSERVICE_CLIENTS_SUB_DIR = "webservice_clients"
meth public static java.util.List<java.lang.String> getDefaultJaxRpcClientJars(org.netbeans.modules.websvc.saas.model.WsdlSaas,java.lang.String)
meth public static java.util.List<java.lang.String> getDefaultJaxWsClientJars(org.netbeans.modules.websvc.saas.model.WsdlSaas,java.lang.String)
meth public static org.openide.filesystems.FileObject getProjectLibraryDirectory(org.netbeans.api.project.Project) throws java.io.IOException
meth public static org.openide.filesystems.FileObject getSourceRoot(org.netbeans.api.project.Project)
meth public static org.openide.filesystems.FileObject getWebServiceClientLibraryDir(org.netbeans.api.project.Project) throws java.io.IOException
meth public static void addArchiveRefsToProject(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.util.List<java.lang.String>)
meth public static void addArchiveRefsToProject(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.util.List<java.lang.String>,java.lang.String)
meth public static void addArchivesToProject(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.util.List<org.openide.filesystems.FileObject>)
meth public static void addArchivesToProject(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.util.List<org.openide.filesystems.FileObject>,java.lang.String)
meth public static void addClientJars(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,org.netbeans.modules.websvc.saas.model.WadlSaas)
meth public static void addDefaultJaxRpcClientJars(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,org.netbeans.modules.websvc.saas.model.WsdlSaas)
meth public static void addDefaultJaxWsClientJars(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,org.netbeans.modules.websvc.saas.model.WsdlSaas)
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.codegen.java.support.SoapClientJavaOperationInfo
cons public init(org.netbeans.modules.websvc.saas.model.WsdlSaasMethod,org.netbeans.api.project.Project)
meth public java.lang.Class getType(org.netbeans.api.project.Project,java.lang.String)
meth public void initWsdlModelInfo()
supr org.netbeans.modules.websvc.saas.codegen.model.SoapClientOperationInfo

CLSS public org.netbeans.modules.websvc.saas.codegen.java.support.SourceGroupSupport
meth public static boolean isFolderWritable(org.netbeans.api.project.SourceGroup,java.lang.String)
meth public static boolean isValidPackageName(java.lang.String)
meth public static java.lang.String getClassName(java.lang.String)
meth public static java.lang.String getPackageForFolder(org.netbeans.api.project.SourceGroup,org.openide.filesystems.FileObject)
meth public static java.lang.String getPackageName(java.lang.String)
meth public static java.lang.String packageForFolder(org.openide.filesystems.FileObject)
meth public static java.util.List<org.netbeans.api.java.classpath.ClassPath> gerClassPath(org.netbeans.api.project.Project)
meth public static org.netbeans.api.java.source.JavaSource getJavaSourceFromClassName(java.lang.String,org.netbeans.api.project.Project) throws java.io.IOException
meth public static org.netbeans.api.project.SourceGroup findSourceGroupForFile(org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
meth public static org.netbeans.api.project.SourceGroup findSourceGroupForFile(org.netbeans.api.project.SourceGroup[],org.openide.filesystems.FileObject)
meth public static org.netbeans.api.project.SourceGroup[] getJavaSourceGroups(org.netbeans.api.project.Project)
meth public static org.openide.filesystems.FileObject findJavaSourceFile(org.netbeans.api.project.Project,java.lang.String)
meth public static org.openide.filesystems.FileObject findSourceRoot(org.netbeans.api.project.Project)
meth public static org.openide.filesystems.FileObject getFileObjectFromClassName(java.lang.String,org.netbeans.api.project.Project) throws java.io.IOException
meth public static org.openide.filesystems.FileObject getFolderForPackage(org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth public static org.openide.filesystems.FileObject getFolderForPackage(org.netbeans.api.project.SourceGroup,java.lang.String,boolean) throws java.io.IOException
supr java.lang.Object

CLSS public org.netbeans.modules.websvc.saas.codegen.java.support.Xsd2Java
cons public init(org.openide.filesystems.FileObject,java.lang.String)
fld public final static java.lang.String JAXB_SCRIPT_PATH = "modules/ext/build-jaxb.xml"
fld public final static java.lang.String PROP_JAXB_BASE = "jaxb.base"
fld public final static java.lang.String PROP_JAXB_JAR = "jaxb.jar"
fld public final static java.lang.String PROP_JAXB_SRC_JAR = "jaxb.src.jar"
fld public final static java.lang.String PROP_PACKAGE_NAME = "packageName"
fld public final static java.lang.String PROP_USER_BUILD_PROPERTIES = "user.build.properties.file"
fld public final static java.lang.String PROP_XSD_FILE = "xsd.file"
meth public boolean compile() throws java.io.IOException
meth public boolean recompile() throws java.io.IOException
meth public org.openide.filesystems.FileObject getJaxbFolder()
meth public org.openide.filesystems.FileObject getJaxbJarFile()
meth public org.openide.filesystems.FileObject getJaxbSourceJarFile()
meth public org.openide.filesystems.FileObject getOrCreateJaxbFolder() throws java.io.IOException
supr java.lang.Object
hfds jaxbFolder,jaxbJarPath,jaxbSourceJarPath,packageName,xsdFile

CLSS public org.netbeans.modules.websvc.saas.codegen.model.SoapClientOperationInfo
cons public init(org.netbeans.modules.websvc.saas.model.WsdlSaasMethod,org.netbeans.api.project.Project)
meth public boolean isRPCEncoded()
meth public boolean needsSoapHandler()
meth public java.lang.Class getType(org.netbeans.api.project.Project,java.lang.String)
meth public java.lang.Class[] getInputParameterTypes()
meth public java.lang.String getCategoryName()
meth public java.lang.String getOperationName()
meth public java.lang.String getOutputType()
meth public java.lang.String getPortName()
meth public java.lang.String getServiceName()
meth public java.lang.String getWsdlLocation()
meth public java.lang.String getWsdlURL()
meth public java.lang.String[] getInputParameterNames()
meth public java.util.List<org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter> getOutputParameters()
meth public java.util.List<org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo> getSoapHeaderParameters()
meth public org.netbeans.api.project.Project getProject()
meth public org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation getOperation()
meth public org.netbeans.modules.websvc.jaxwsmodelapi.WSPort getPort()
meth public org.netbeans.modules.websvc.jaxwsmodelapi.WSService getService()
meth public org.netbeans.modules.websvc.saas.model.WsdlSaasMethod getMethod()
meth public org.netbeans.modules.xml.wsdl.model.WSDLModel getXamWsdlModel()
meth public static java.lang.String getParamType(org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter)
meth public static org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation findOperationByName(org.netbeans.modules.websvc.jaxwsmodelapi.WSPort,java.lang.String)
meth public void initWsdlModelInfo()
supr java.lang.Object
hfds categoryName,headerParams,method,operation,operationName,port,portName,project,service,serviceName,webServiceData,wsdlUrl

CLSS public abstract interface org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider
meth public abstract boolean canAccept(org.netbeans.modules.websvc.saas.model.SaasMethod,javax.swing.text.Document)
meth public abstract int getPrecedence()
meth public abstract java.util.Set<org.openide.filesystems.FileObject> generate() throws java.io.IOException
meth public abstract void init(org.netbeans.modules.websvc.saas.model.SaasMethod,javax.swing.text.Document) throws java.io.IOException

