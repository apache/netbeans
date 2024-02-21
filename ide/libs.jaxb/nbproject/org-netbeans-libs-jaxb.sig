#Signature file v4.1
#Version 1.54

CLSS public final com.sun.codemodel.ClassType
fld public final static com.sun.codemodel.ClassType ANNOTATION_TYPE_DECL
fld public final static com.sun.codemodel.ClassType CLASS
fld public final static com.sun.codemodel.ClassType ENUM
fld public final static com.sun.codemodel.ClassType INTERFACE
supr java.lang.Object
hfds declarationToken

CLSS public abstract com.sun.codemodel.CodeWriter
cons public init()
fld protected java.lang.String encoding
meth public abstract java.io.OutputStream openBinary(com.sun.codemodel.JPackage,java.lang.String) throws java.io.IOException
meth public abstract void close() throws java.io.IOException
meth public java.io.Writer openSource(com.sun.codemodel.JPackage,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface com.sun.codemodel.JAnnotatable
meth public abstract <%0 extends com.sun.codemodel.JAnnotationWriter<? extends java.lang.annotation.Annotation>> {%%0} annotate2(java.lang.Class<{%%0}>)
meth public abstract boolean removeAnnotation(com.sun.codemodel.JAnnotationUse)
meth public abstract com.sun.codemodel.JAnnotationUse annotate(com.sun.codemodel.JClass)
meth public abstract com.sun.codemodel.JAnnotationUse annotate(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public abstract java.util.Collection<com.sun.codemodel.JAnnotationUse> annotations()

CLSS public final com.sun.codemodel.JAnnotationArrayMember
intf com.sun.codemodel.JAnnotatable
meth public <%0 extends com.sun.codemodel.JAnnotationWriter<? extends java.lang.annotation.Annotation>> {%%0} annotate2(java.lang.Class<{%%0}>)
meth public boolean removeAnnotation(com.sun.codemodel.JAnnotationUse)
meth public com.sun.codemodel.JAnnotationArrayMember param(boolean)
meth public com.sun.codemodel.JAnnotationArrayMember param(byte)
meth public com.sun.codemodel.JAnnotationArrayMember param(char)
meth public com.sun.codemodel.JAnnotationArrayMember param(com.sun.codemodel.JAnnotationUse)
 anno 0 java.lang.Deprecated()
meth public com.sun.codemodel.JAnnotationArrayMember param(com.sun.codemodel.JEnumConstant)
meth public com.sun.codemodel.JAnnotationArrayMember param(com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JAnnotationArrayMember param(com.sun.codemodel.JType)
meth public com.sun.codemodel.JAnnotationArrayMember param(double)
meth public com.sun.codemodel.JAnnotationArrayMember param(float)
meth public com.sun.codemodel.JAnnotationArrayMember param(int)
meth public com.sun.codemodel.JAnnotationArrayMember param(java.lang.Class<?>)
meth public com.sun.codemodel.JAnnotationArrayMember param(java.lang.Enum<?>)
meth public com.sun.codemodel.JAnnotationArrayMember param(java.lang.String)
meth public com.sun.codemodel.JAnnotationArrayMember param(long)
meth public com.sun.codemodel.JAnnotationArrayMember param(short)
meth public com.sun.codemodel.JAnnotationUse annotate(com.sun.codemodel.JClass)
meth public com.sun.codemodel.JAnnotationUse annotate(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public java.util.Collection<com.sun.codemodel.JAnnotationUse> annotations()
meth public void generate(com.sun.codemodel.JFormatter)
supr com.sun.codemodel.JAnnotationValue
hfds owner,values

CLSS public final com.sun.codemodel.JAnnotationUse
meth public com.sun.codemodel.JAnnotationArrayMember paramArray(java.lang.String)
meth public com.sun.codemodel.JAnnotationUse annotate(java.lang.Class<? extends java.lang.annotation.Annotation>)
 anno 0 java.lang.Deprecated()
meth public com.sun.codemodel.JAnnotationUse annotationParam(java.lang.String,java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public com.sun.codemodel.JAnnotationUse param(java.lang.String,boolean)
meth public com.sun.codemodel.JAnnotationUse param(java.lang.String,byte)
meth public com.sun.codemodel.JAnnotationUse param(java.lang.String,char)
meth public com.sun.codemodel.JAnnotationUse param(java.lang.String,com.sun.codemodel.JEnumConstant)
meth public com.sun.codemodel.JAnnotationUse param(java.lang.String,com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JAnnotationUse param(java.lang.String,com.sun.codemodel.JType)
meth public com.sun.codemodel.JAnnotationUse param(java.lang.String,double)
meth public com.sun.codemodel.JAnnotationUse param(java.lang.String,float)
meth public com.sun.codemodel.JAnnotationUse param(java.lang.String,int)
meth public com.sun.codemodel.JAnnotationUse param(java.lang.String,java.lang.Class<?>)
meth public com.sun.codemodel.JAnnotationUse param(java.lang.String,java.lang.Enum<?>)
meth public com.sun.codemodel.JAnnotationUse param(java.lang.String,java.lang.String)
meth public com.sun.codemodel.JAnnotationUse param(java.lang.String,long)
meth public com.sun.codemodel.JAnnotationUse param(java.lang.String,short)
meth public com.sun.codemodel.JClass getAnnotationClass()
meth public java.util.Map<java.lang.String,com.sun.codemodel.JAnnotationValue> getAnnotationMembers()
meth public void generate(com.sun.codemodel.JFormatter)
supr com.sun.codemodel.JAnnotationValue
hfds clazz,memberValues

CLSS public abstract com.sun.codemodel.JAnnotationValue
cons protected init()
intf com.sun.codemodel.JGenerable
supr java.lang.Object

CLSS public abstract interface com.sun.codemodel.JAnnotationWriter<%0 extends java.lang.annotation.Annotation>
meth public abstract com.sun.codemodel.JAnnotationUse getAnnotationUse()
meth public abstract java.lang.Class<{com.sun.codemodel.JAnnotationWriter%0}> getAnnotationType()

CLSS public final com.sun.codemodel.JArray
meth public com.sun.codemodel.JArray add(com.sun.codemodel.JExpression)
meth public void generate(com.sun.codemodel.JFormatter)
supr com.sun.codemodel.JExpressionImpl
hfds exprs,size,type

CLSS public final com.sun.codemodel.JArrayCompRef
intf com.sun.codemodel.JAssignmentTarget
meth public com.sun.codemodel.JExpression assign(com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JExpression assignPlus(com.sun.codemodel.JExpression)
meth public void generate(com.sun.codemodel.JFormatter)
supr com.sun.codemodel.JExpressionImpl
hfds array,index

CLSS public com.sun.codemodel.JAssignment
intf com.sun.codemodel.JStatement
meth public void generate(com.sun.codemodel.JFormatter)
meth public void state(com.sun.codemodel.JFormatter)
supr com.sun.codemodel.JExpressionImpl
hfds lhs,op,rhs

CLSS public abstract interface com.sun.codemodel.JAssignmentTarget
intf com.sun.codemodel.JExpression
intf com.sun.codemodel.JGenerable
meth public abstract com.sun.codemodel.JExpression assign(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression assignPlus(com.sun.codemodel.JExpression)

CLSS public final com.sun.codemodel.JBlock
cons public init()
cons public init(boolean,boolean)
intf com.sun.codemodel.JGenerable
intf com.sun.codemodel.JStatement
meth public boolean isEmpty()
meth public com.sun.codemodel.JBlock add(com.sun.codemodel.JStatement)
meth public com.sun.codemodel.JBlock assign(com.sun.codemodel.JAssignmentTarget,com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JBlock assignPlus(com.sun.codemodel.JAssignmentTarget,com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JBlock block()
meth public com.sun.codemodel.JConditional _if(com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JDoLoop _do(com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JForEach forEach(com.sun.codemodel.JType,java.lang.String,com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JForLoop _for()
meth public com.sun.codemodel.JInvocation invoke(com.sun.codemodel.JExpression,com.sun.codemodel.JMethod)
meth public com.sun.codemodel.JInvocation invoke(com.sun.codemodel.JExpression,java.lang.String)
meth public com.sun.codemodel.JInvocation invoke(com.sun.codemodel.JMethod)
meth public com.sun.codemodel.JInvocation invoke(java.lang.String)
meth public com.sun.codemodel.JInvocation staticInvoke(com.sun.codemodel.JClass,java.lang.String)
meth public com.sun.codemodel.JLabel label(java.lang.String)
meth public com.sun.codemodel.JStatement directStatement(java.lang.String)
meth public com.sun.codemodel.JSwitch _switch(com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JTryBlock _try()
meth public com.sun.codemodel.JVar decl(com.sun.codemodel.JType,java.lang.String)
meth public com.sun.codemodel.JVar decl(com.sun.codemodel.JType,java.lang.String,com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JVar decl(int,com.sun.codemodel.JType,java.lang.String,com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JWhileLoop _while(com.sun.codemodel.JExpression)
meth public int pos()
meth public int pos(int)
meth public java.util.List<java.lang.Object> getContents()
meth public void _break()
meth public void _break(com.sun.codemodel.JLabel)
meth public void _continue()
meth public void _continue(com.sun.codemodel.JLabel)
meth public void _return()
meth public void _return(com.sun.codemodel.JExpression)
meth public void _throw(com.sun.codemodel.JExpression)
meth public void generate(com.sun.codemodel.JFormatter)
meth public void state(com.sun.codemodel.JFormatter)
supr java.lang.Object
hfds bracesRequired,content,indentRequired,pos

CLSS public final com.sun.codemodel.JCase
intf com.sun.codemodel.JStatement
meth public com.sun.codemodel.JBlock body()
meth public com.sun.codemodel.JExpression label()
meth public void state(com.sun.codemodel.JFormatter)
supr java.lang.Object
hfds body,isDefaultCase,label

CLSS public final com.sun.codemodel.JCast
meth public void generate(com.sun.codemodel.JFormatter)
supr com.sun.codemodel.JExpressionImpl
hfds object,type

CLSS public com.sun.codemodel.JCatchBlock
intf com.sun.codemodel.JGenerable
meth public com.sun.codemodel.JBlock body()
meth public com.sun.codemodel.JVar param(java.lang.String)
meth public void generate(com.sun.codemodel.JFormatter)
supr java.lang.Object
hfds body,exception,var

CLSS public abstract com.sun.codemodel.JClass
cons protected init(com.sun.codemodel.JCodeModel)
fld protected final static com.sun.codemodel.JTypeVar[] EMPTY_ARRAY
meth protected abstract com.sun.codemodel.JClass substituteParams(com.sun.codemodel.JTypeVar[],java.util.List<com.sun.codemodel.JClass>)
meth public !varargs com.sun.codemodel.JClass narrow(com.sun.codemodel.JClass[])
meth public !varargs com.sun.codemodel.JClass narrow(java.lang.Class<?>[])
meth public abstract boolean isAbstract()
meth public abstract boolean isInterface()
meth public abstract com.sun.codemodel.JClass _extends()
meth public abstract com.sun.codemodel.JPackage _package()
meth public abstract java.lang.String name()
meth public abstract java.util.Iterator<com.sun.codemodel.JClass> _implements()
meth public com.sun.codemodel.JClass array()
meth public com.sun.codemodel.JClass boxify()
 anno 0 java.lang.Deprecated()
meth public com.sun.codemodel.JClass erasure()
meth public com.sun.codemodel.JClass narrow(com.sun.codemodel.JClass)
meth public com.sun.codemodel.JClass narrow(com.sun.codemodel.JType)
meth public com.sun.codemodel.JClass narrow(java.lang.Class<?>)
meth public com.sun.codemodel.JClass narrow(java.util.List<? extends com.sun.codemodel.JClass>)
meth public com.sun.codemodel.JClass outer()
meth public com.sun.codemodel.JPrimitiveType getPrimitiveType()
meth public com.sun.codemodel.JType unboxify()
meth public com.sun.codemodel.JTypeVar[] typeParams()
meth public final boolean isAssignableFrom(com.sun.codemodel.JClass)
meth public final boolean isParameterized()
meth public final com.sun.codemodel.JClass getBaseClass(com.sun.codemodel.JClass)
meth public final com.sun.codemodel.JClass getBaseClass(java.lang.Class<?>)
meth public final com.sun.codemodel.JClass wildcard()
meth public final com.sun.codemodel.JCodeModel owner()
meth public final com.sun.codemodel.JExpression dotclass()
meth public final com.sun.codemodel.JFieldRef staticRef(com.sun.codemodel.JVar)
meth public final com.sun.codemodel.JFieldRef staticRef(java.lang.String)
meth public final com.sun.codemodel.JInvocation staticInvoke(com.sun.codemodel.JMethod)
meth public final com.sun.codemodel.JInvocation staticInvoke(java.lang.String)
meth public java.lang.String toString()
meth public java.util.List<com.sun.codemodel.JClass> getTypeParameters()
meth public void generate(com.sun.codemodel.JFormatter)
supr com.sun.codemodel.JType
hfds _owner,arrayClass

CLSS public com.sun.codemodel.JClassAlreadyExistsException
cons public init(com.sun.codemodel.JDefinedClass)
meth public com.sun.codemodel.JDefinedClass getExistingClass()
supr java.lang.Exception
hfds existing,serialVersionUID

CLSS public abstract interface com.sun.codemodel.JClassContainer
meth public abstract boolean isClass()
meth public abstract boolean isPackage()
meth public abstract com.sun.codemodel.JClassContainer parentContainer()
meth public abstract com.sun.codemodel.JCodeModel owner()
meth public abstract com.sun.codemodel.JDefinedClass _annotationTypeDeclaration(java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public abstract com.sun.codemodel.JDefinedClass _class(int,java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public abstract com.sun.codemodel.JDefinedClass _class(int,java.lang.String,boolean) throws com.sun.codemodel.JClassAlreadyExistsException
 anno 0 java.lang.Deprecated()
meth public abstract com.sun.codemodel.JDefinedClass _class(int,java.lang.String,com.sun.codemodel.ClassType) throws com.sun.codemodel.JClassAlreadyExistsException
meth public abstract com.sun.codemodel.JDefinedClass _class(java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public abstract com.sun.codemodel.JDefinedClass _enum(java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public abstract com.sun.codemodel.JDefinedClass _interface(int,java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public abstract com.sun.codemodel.JDefinedClass _interface(java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public abstract com.sun.codemodel.JPackage getPackage()
meth public abstract java.util.Iterator<com.sun.codemodel.JDefinedClass> classes()

CLSS public final com.sun.codemodel.JCodeModel
cons public init()
fld protected final static boolean isCaseSensitiveFileSystem
fld public final com.sun.codemodel.JNullType NULL
fld public final com.sun.codemodel.JPrimitiveType BOOLEAN
fld public final com.sun.codemodel.JPrimitiveType BYTE
fld public final com.sun.codemodel.JPrimitiveType CHAR
fld public final com.sun.codemodel.JPrimitiveType DOUBLE
fld public final com.sun.codemodel.JPrimitiveType FLOAT
fld public final com.sun.codemodel.JPrimitiveType INT
fld public final com.sun.codemodel.JPrimitiveType LONG
fld public final com.sun.codemodel.JPrimitiveType SHORT
fld public final com.sun.codemodel.JPrimitiveType VOID
fld public final static java.util.Map<java.lang.Class<?>,java.lang.Class<?>> boxToPrimitive
fld public final static java.util.Map<java.lang.Class<?>,java.lang.Class<?>> primitiveToBox
meth public !varargs void _prepareModuleInfo(java.lang.String,java.lang.String[])
meth public !varargs void _updateModuleInfo(java.lang.String[])
meth public com.sun.codemodel.JClass directClass(java.lang.String)
meth public com.sun.codemodel.JClass ref(java.lang.Class<?>)
meth public com.sun.codemodel.JClass ref(java.lang.String)
meth public com.sun.codemodel.JClass wildcard()
meth public com.sun.codemodel.JDefinedClass _class(int,java.lang.String,com.sun.codemodel.ClassType) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDefinedClass _class(java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDefinedClass _class(java.lang.String,com.sun.codemodel.ClassType) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDefinedClass _getClass(java.lang.String)
meth public com.sun.codemodel.JDefinedClass anonymousClass(com.sun.codemodel.JClass)
meth public com.sun.codemodel.JDefinedClass anonymousClass(java.lang.Class<?>)
meth public com.sun.codemodel.JDefinedClass newAnonymousClass(com.sun.codemodel.JClass)
 anno 0 java.lang.Deprecated()
meth public com.sun.codemodel.JModule _getModuleInfo()
meth public com.sun.codemodel.JModule _moduleInfo(java.lang.String)
meth public com.sun.codemodel.JPackage _package(java.lang.String)
meth public com.sun.codemodel.JType _ref(java.lang.Class<?>)
meth public com.sun.codemodel.JType parseType(java.lang.String) throws java.lang.ClassNotFoundException
meth public final com.sun.codemodel.JPackage rootPackage()
meth public int countArtifacts()
meth public java.util.Iterator<com.sun.codemodel.JPackage> packages()
meth public void build(com.sun.codemodel.CodeWriter) throws java.io.IOException
meth public void build(com.sun.codemodel.CodeWriter,com.sun.codemodel.CodeWriter) throws java.io.IOException
meth public void build(java.io.File) throws java.io.IOException
meth public void build(java.io.File,java.io.File) throws java.io.IOException
meth public void build(java.io.File,java.io.File,java.io.PrintStream) throws java.io.IOException
meth public void build(java.io.File,java.io.PrintStream) throws java.io.IOException
supr java.lang.Object
hfds module,packages,refClasses,wildcard
hcls JReferencedClass,TypeNameParser

CLSS public com.sun.codemodel.JCommentPart
cons protected init()
meth protected void format(com.sun.codemodel.JFormatter,java.lang.String)
meth public boolean add(java.lang.Object)
meth public com.sun.codemodel.JCommentPart append(java.lang.Object)
supr java.util.ArrayList<java.lang.Object>
hfds serialVersionUID

CLSS public com.sun.codemodel.JConditional
intf com.sun.codemodel.JStatement
meth public com.sun.codemodel.JBlock _else()
meth public com.sun.codemodel.JBlock _then()
meth public com.sun.codemodel.JConditional _elseif(com.sun.codemodel.JExpression)
meth public void state(com.sun.codemodel.JFormatter)
supr java.lang.Object
hfds _else,_then,test

CLSS public abstract interface com.sun.codemodel.JDeclaration
meth public abstract void declare(com.sun.codemodel.JFormatter)

CLSS public com.sun.codemodel.JDefinedClass
fld public java.lang.Object metadata
intf com.sun.codemodel.JAnnotatable
intf com.sun.codemodel.JClassContainer
intf com.sun.codemodel.JDeclaration
intf com.sun.codemodel.JDocCommentable
intf com.sun.codemodel.JGenerifiable
meth protected com.sun.codemodel.JClass substituteParams(com.sun.codemodel.JTypeVar[],java.util.List<com.sun.codemodel.JClass>)
meth protected void declareBody(com.sun.codemodel.JFormatter)
meth public <%0 extends com.sun.codemodel.JAnnotationWriter<? extends java.lang.annotation.Annotation>> {%%0} annotate2(java.lang.Class<{%%0}>)
meth public boolean isAbstract()
meth public boolean isAnnotationTypeDeclaration()
meth public boolean isClass()
meth public boolean isHidden()
meth public boolean isInterface()
meth public boolean isPackage()
meth public boolean removeAnnotation(com.sun.codemodel.JAnnotationUse)
meth public com.sun.codemodel.ClassType getClassType()
meth public com.sun.codemodel.JAnnotationUse annotate(com.sun.codemodel.JClass)
meth public com.sun.codemodel.JAnnotationUse annotate(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public com.sun.codemodel.JBlock init()
meth public com.sun.codemodel.JBlock instanceInit()
meth public com.sun.codemodel.JClass _extends()
meth public com.sun.codemodel.JClass outer()
meth public com.sun.codemodel.JDefinedClass _annotationTypeDeclaration(java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDefinedClass _class(int,java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDefinedClass _class(int,java.lang.String,boolean) throws com.sun.codemodel.JClassAlreadyExistsException
 anno 0 java.lang.Deprecated()
meth public com.sun.codemodel.JDefinedClass _class(int,java.lang.String,com.sun.codemodel.ClassType) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDefinedClass _class(java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDefinedClass _enum(int,java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDefinedClass _enum(java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDefinedClass _extends(com.sun.codemodel.JClass)
meth public com.sun.codemodel.JDefinedClass _extends(java.lang.Class<?>)
meth public com.sun.codemodel.JDefinedClass _implements(com.sun.codemodel.JClass)
meth public com.sun.codemodel.JDefinedClass _implements(java.lang.Class<?>)
meth public com.sun.codemodel.JDefinedClass _interface(int,java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDefinedClass _interface(java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDocComment javadoc()
meth public com.sun.codemodel.JEnumConstant enumConstant(java.lang.String)
meth public com.sun.codemodel.JFieldVar field(int,com.sun.codemodel.JType,java.lang.String)
meth public com.sun.codemodel.JFieldVar field(int,com.sun.codemodel.JType,java.lang.String,com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JFieldVar field(int,java.lang.Class<?>,java.lang.String)
meth public com.sun.codemodel.JFieldVar field(int,java.lang.Class<?>,java.lang.String,com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JMethod constructor(int)
meth public com.sun.codemodel.JMethod getConstructor(com.sun.codemodel.JType[])
meth public com.sun.codemodel.JMethod getMethod(java.lang.String,com.sun.codemodel.JType[])
meth public com.sun.codemodel.JMethod method(int,com.sun.codemodel.JType,java.lang.String)
meth public com.sun.codemodel.JMethod method(int,java.lang.Class<?>,java.lang.String)
meth public com.sun.codemodel.JMods mods()
meth public com.sun.codemodel.JPackage getPackage()
meth public com.sun.codemodel.JTypeVar generify(java.lang.String)
meth public com.sun.codemodel.JTypeVar generify(java.lang.String,com.sun.codemodel.JClass)
meth public com.sun.codemodel.JTypeVar generify(java.lang.String,java.lang.Class<?>)
meth public com.sun.codemodel.JTypeVar[] typeParams()
meth public final boolean isAnonymous()
meth public final com.sun.codemodel.JClassContainer parentContainer()
meth public final com.sun.codemodel.JClass[] listClasses()
meth public final com.sun.codemodel.JPackage _package()
meth public final java.util.Iterator<com.sun.codemodel.JDefinedClass> classes()
meth public java.lang.String binaryName()
meth public java.lang.String fullName()
meth public java.lang.String name()
meth public java.util.Collection<com.sun.codemodel.JAnnotationUse> annotations()
meth public java.util.Collection<com.sun.codemodel.JMethod> methods()
meth public java.util.Iterator<com.sun.codemodel.JClass> _implements()
meth public java.util.Iterator<com.sun.codemodel.JMethod> constructors()
meth public java.util.Map<java.lang.String,com.sun.codemodel.JFieldVar> fields()
meth public void declare(com.sun.codemodel.JFormatter)
meth public void direct(java.lang.String)
meth public void hide()
meth public void removeField(com.sun.codemodel.JFieldVar)
supr com.sun.codemodel.JClass
hfds annotations,classType,classes,constructors,directBlock,enumConstantsByName,fields,generifiable,hideFile,init,instanceInit,interfaces,jdoc,methods,mods,name,outer,superClass

CLSS public com.sun.codemodel.JDoLoop
intf com.sun.codemodel.JStatement
meth public com.sun.codemodel.JBlock body()
meth public void state(com.sun.codemodel.JFormatter)
supr java.lang.Object
hfds body,test

CLSS public com.sun.codemodel.JDocComment
cons public init(com.sun.codemodel.JCodeModel)
intf com.sun.codemodel.JGenerable
meth public com.sun.codemodel.JCommentPart addDeprecated()
meth public com.sun.codemodel.JCommentPart addParam(com.sun.codemodel.JVar)
meth public com.sun.codemodel.JCommentPart addParam(java.lang.String)
meth public com.sun.codemodel.JCommentPart addReturn()
meth public com.sun.codemodel.JCommentPart addThrows(com.sun.codemodel.JClass)
meth public com.sun.codemodel.JCommentPart addThrows(java.lang.Class<? extends java.lang.Throwable>)
meth public com.sun.codemodel.JDocComment append(java.lang.Object)
meth public java.util.Map<java.lang.String,java.lang.String> addXdoclet(java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.String> addXdoclet(java.lang.String,java.lang.String,java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.String> addXdoclet(java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
meth public void generate(com.sun.codemodel.JFormatter)
supr com.sun.codemodel.JCommentPart
hfds INDENT,atDeprecated,atParams,atReturn,atThrows,atXdoclets,owner,serialVersionUID

CLSS public abstract interface com.sun.codemodel.JDocCommentable
meth public abstract com.sun.codemodel.JDocComment javadoc()

CLSS public final com.sun.codemodel.JEnumConstant
intf com.sun.codemodel.JAnnotatable
intf com.sun.codemodel.JDeclaration
intf com.sun.codemodel.JDocCommentable
meth public <%0 extends com.sun.codemodel.JAnnotationWriter<? extends java.lang.annotation.Annotation>> {%%0} annotate2(java.lang.Class<{%%0}>)
meth public boolean removeAnnotation(com.sun.codemodel.JAnnotationUse)
meth public com.sun.codemodel.JAnnotationUse annotate(com.sun.codemodel.JClass)
meth public com.sun.codemodel.JAnnotationUse annotate(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public com.sun.codemodel.JDocComment javadoc()
meth public com.sun.codemodel.JEnumConstant arg(com.sun.codemodel.JExpression)
meth public java.lang.String getName()
meth public java.util.Collection<com.sun.codemodel.JAnnotationUse> annotations()
meth public void declare(com.sun.codemodel.JFormatter)
meth public void generate(com.sun.codemodel.JFormatter)
supr com.sun.codemodel.JExpressionImpl
hfds annotations,args,jdoc,name,type

CLSS public com.sun.codemodel.JExportsDirective
meth public com.sun.codemodel.JFormatter generate(com.sun.codemodel.JFormatter)
meth public com.sun.codemodel.JModuleDirective$Type getType()
supr com.sun.codemodel.JModuleDirective

CLSS public abstract com.sun.codemodel.JExpr
fld public final static com.sun.codemodel.JExpression FALSE
fld public final static com.sun.codemodel.JExpression TRUE
meth public static com.sun.codemodel.JArray newArray(com.sun.codemodel.JType)
meth public static com.sun.codemodel.JArray newArray(com.sun.codemodel.JType,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JArray newArray(com.sun.codemodel.JType,int)
meth public static com.sun.codemodel.JArrayCompRef component(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JCast cast(com.sun.codemodel.JType,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression _null()
meth public static com.sun.codemodel.JExpression _super()
meth public static com.sun.codemodel.JExpression _this()
meth public static com.sun.codemodel.JExpression assign(com.sun.codemodel.JAssignmentTarget,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression assignPlus(com.sun.codemodel.JAssignmentTarget,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression direct(java.lang.String)
meth public static com.sun.codemodel.JExpression dotclass(com.sun.codemodel.JClass)
meth public static com.sun.codemodel.JExpression lit(boolean)
meth public static com.sun.codemodel.JExpression lit(char)
meth public static com.sun.codemodel.JExpression lit(double)
meth public static com.sun.codemodel.JExpression lit(float)
meth public static com.sun.codemodel.JExpression lit(int)
meth public static com.sun.codemodel.JExpression lit(java.lang.String)
meth public static com.sun.codemodel.JExpression lit(long)
meth public static com.sun.codemodel.JFieldRef ref(com.sun.codemodel.JExpression,com.sun.codemodel.JVar)
meth public static com.sun.codemodel.JFieldRef ref(com.sun.codemodel.JExpression,java.lang.String)
meth public static com.sun.codemodel.JFieldRef ref(java.lang.String)
meth public static com.sun.codemodel.JFieldRef refthis(java.lang.String)
meth public static com.sun.codemodel.JInvocation _new(com.sun.codemodel.JClass)
meth public static com.sun.codemodel.JInvocation _new(com.sun.codemodel.JType)
meth public static com.sun.codemodel.JInvocation invoke(com.sun.codemodel.JExpression,com.sun.codemodel.JMethod)
meth public static com.sun.codemodel.JInvocation invoke(com.sun.codemodel.JExpression,java.lang.String)
meth public static com.sun.codemodel.JInvocation invoke(com.sun.codemodel.JMethod)
meth public static com.sun.codemodel.JInvocation invoke(java.lang.String)
meth public static java.lang.String quotify(char,java.lang.String)
supr java.lang.Object
hfds __null,__super,__this,charEscape,charMacro

CLSS public abstract interface com.sun.codemodel.JExpression
intf com.sun.codemodel.JGenerable
meth public abstract com.sun.codemodel.JArrayCompRef component(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression _instanceof(com.sun.codemodel.JType)
meth public abstract com.sun.codemodel.JExpression band(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression bor(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression cand(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression complement()
meth public abstract com.sun.codemodel.JExpression cor(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression decr()
meth public abstract com.sun.codemodel.JExpression div(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression eq(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression gt(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression gte(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression incr()
meth public abstract com.sun.codemodel.JExpression lt(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression lte(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression minus()
meth public abstract com.sun.codemodel.JExpression minus(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression mod(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression mul(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression ne(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression not()
meth public abstract com.sun.codemodel.JExpression plus(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression shl(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression shr(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression shrz(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JExpression xor(com.sun.codemodel.JExpression)
meth public abstract com.sun.codemodel.JFieldRef ref(com.sun.codemodel.JVar)
meth public abstract com.sun.codemodel.JFieldRef ref(java.lang.String)
meth public abstract com.sun.codemodel.JInvocation invoke(com.sun.codemodel.JMethod)
meth public abstract com.sun.codemodel.JInvocation invoke(java.lang.String)

CLSS public abstract com.sun.codemodel.JExpressionImpl
cons protected init()
intf com.sun.codemodel.JExpression
meth public final com.sun.codemodel.JArrayCompRef component(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression _instanceof(com.sun.codemodel.JType)
meth public final com.sun.codemodel.JExpression band(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression bor(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression cand(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression complement()
meth public final com.sun.codemodel.JExpression cor(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression decr()
meth public final com.sun.codemodel.JExpression div(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression eq(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression gt(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression gte(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression incr()
meth public final com.sun.codemodel.JExpression lt(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression lte(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression minus()
meth public final com.sun.codemodel.JExpression minus(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression mod(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression mul(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression ne(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression not()
meth public final com.sun.codemodel.JExpression plus(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression shl(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression shr(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression shrz(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JExpression xor(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JFieldRef ref(com.sun.codemodel.JVar)
meth public final com.sun.codemodel.JFieldRef ref(java.lang.String)
meth public final com.sun.codemodel.JInvocation invoke(com.sun.codemodel.JMethod)
meth public final com.sun.codemodel.JInvocation invoke(java.lang.String)
supr java.lang.Object

CLSS public com.sun.codemodel.JFieldRef
intf com.sun.codemodel.JAssignmentTarget
meth public com.sun.codemodel.JExpression assign(com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JExpression assignPlus(com.sun.codemodel.JExpression)
meth public void generate(com.sun.codemodel.JFormatter)
supr com.sun.codemodel.JExpressionImpl
hfds explicitThis,name,object,var

CLSS public com.sun.codemodel.JFieldVar
intf com.sun.codemodel.JDocCommentable
meth public com.sun.codemodel.JDocComment javadoc()
meth public void declare(com.sun.codemodel.JFormatter)
meth public void name(java.lang.String)
supr com.sun.codemodel.JVar
hfds jdoc,owner

CLSS public final com.sun.codemodel.JForEach
cons public init(com.sun.codemodel.JType,java.lang.String,com.sun.codemodel.JExpression)
intf com.sun.codemodel.JStatement
meth public com.sun.codemodel.JBlock body()
meth public com.sun.codemodel.JVar var()
meth public void state(com.sun.codemodel.JFormatter)
supr java.lang.Object
hfds body,collection,loopVar,type,var

CLSS public com.sun.codemodel.JForLoop
cons protected init()
intf com.sun.codemodel.JStatement
meth public com.sun.codemodel.JBlock body()
meth public com.sun.codemodel.JVar init(com.sun.codemodel.JType,java.lang.String,com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JVar init(int,com.sun.codemodel.JType,java.lang.String,com.sun.codemodel.JExpression)
meth public void init(com.sun.codemodel.JVar,com.sun.codemodel.JExpression)
meth public void state(com.sun.codemodel.JFormatter)
meth public void test(com.sun.codemodel.JExpression)
meth public void update(com.sun.codemodel.JExpression)
supr java.lang.Object
hfds body,inits,test,updates

CLSS public final com.sun.codemodel.JFormatter
cons public init(java.io.PrintWriter)
cons public init(java.io.PrintWriter,java.lang.String)
cons public init(java.io.Writer)
meth public boolean isPrinting()
meth public com.sun.codemodel.JFormatter b(com.sun.codemodel.JVar)
meth public com.sun.codemodel.JFormatter d(com.sun.codemodel.JDeclaration)
meth public com.sun.codemodel.JFormatter g(com.sun.codemodel.JGenerable)
meth public com.sun.codemodel.JFormatter g(java.util.Collection<? extends com.sun.codemodel.JGenerable>)
meth public com.sun.codemodel.JFormatter i()
meth public com.sun.codemodel.JFormatter id(java.lang.String)
meth public com.sun.codemodel.JFormatter nl()
meth public com.sun.codemodel.JFormatter o()
meth public com.sun.codemodel.JFormatter p(char)
meth public com.sun.codemodel.JFormatter p(java.lang.String)
meth public com.sun.codemodel.JFormatter s(com.sun.codemodel.JStatement)
meth public com.sun.codemodel.JFormatter t(com.sun.codemodel.JClass)
meth public com.sun.codemodel.JFormatter t(com.sun.codemodel.JType)
meth public void close()
supr java.lang.Object
hfds CLOSE_TYPE_ARGS,atBeginningOfLine,collectedReferences,importedClasses,indentLevel,indentSpace,javaLang,lastChar,mode,pw
hcls Mode,ReferenceList

CLSS public abstract interface com.sun.codemodel.JGenerable
meth public abstract void generate(com.sun.codemodel.JFormatter)

CLSS public abstract interface com.sun.codemodel.JGenerifiable
meth public abstract com.sun.codemodel.JTypeVar generify(java.lang.String)
meth public abstract com.sun.codemodel.JTypeVar generify(java.lang.String,com.sun.codemodel.JClass)
meth public abstract com.sun.codemodel.JTypeVar generify(java.lang.String,java.lang.Class<?>)
meth public abstract com.sun.codemodel.JTypeVar[] typeParams()

CLSS public final com.sun.codemodel.JInvocation
intf com.sun.codemodel.JStatement
meth public com.sun.codemodel.JExpression[] listArgs()
meth public com.sun.codemodel.JInvocation arg(com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JInvocation arg(java.lang.String)
meth public void generate(com.sun.codemodel.JFormatter)
meth public void state(com.sun.codemodel.JFormatter)
supr com.sun.codemodel.JExpressionImpl
hfds args,isConstructor,method,name,object,type

CLSS public com.sun.codemodel.JJavaName
meth public static boolean isFullyQualifiedClassName(java.lang.String)
meth public static boolean isJavaIdentifier(java.lang.String)
meth public static boolean isJavaPackageName(java.lang.String)
meth public static java.lang.String getPluralForm(java.lang.String)
supr java.lang.Object
hfds TABLE,reservedKeywords
hcls Entry

CLSS public com.sun.codemodel.JLabel
intf com.sun.codemodel.JStatement
meth public void state(com.sun.codemodel.JFormatter)
supr java.lang.Object
hfds label

CLSS public com.sun.codemodel.JMethod
intf com.sun.codemodel.JAnnotatable
intf com.sun.codemodel.JDeclaration
intf com.sun.codemodel.JDocCommentable
intf com.sun.codemodel.JGenerifiable
meth protected com.sun.codemodel.JCodeModel owner()
meth public <%0 extends com.sun.codemodel.JAnnotationWriter<? extends java.lang.annotation.Annotation>> {%%0} annotate2(java.lang.Class<{%%0}>)
meth public boolean hasSignature(com.sun.codemodel.JType[])
meth public boolean hasVarArgs()
meth public boolean removeAnnotation(com.sun.codemodel.JAnnotationUse)
meth public com.sun.codemodel.JAnnotationUse annotate(com.sun.codemodel.JClass)
meth public com.sun.codemodel.JAnnotationUse annotate(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public com.sun.codemodel.JBlock body()
meth public com.sun.codemodel.JDocComment javadoc()
meth public com.sun.codemodel.JMethod _throws(com.sun.codemodel.JClass)
meth public com.sun.codemodel.JMethod _throws(java.lang.Class<? extends java.lang.Throwable>)
meth public com.sun.codemodel.JMods getMods()
 anno 0 java.lang.Deprecated()
meth public com.sun.codemodel.JMods mods()
meth public com.sun.codemodel.JType listVarParamType()
meth public com.sun.codemodel.JType type()
meth public com.sun.codemodel.JTypeVar generify(java.lang.String)
meth public com.sun.codemodel.JTypeVar generify(java.lang.String,com.sun.codemodel.JClass)
meth public com.sun.codemodel.JTypeVar generify(java.lang.String,java.lang.Class<?>)
meth public com.sun.codemodel.JTypeVar[] typeParams()
meth public com.sun.codemodel.JType[] listParamTypes()
meth public com.sun.codemodel.JVar listVarParam()
meth public com.sun.codemodel.JVar param(com.sun.codemodel.JType,java.lang.String)
meth public com.sun.codemodel.JVar param(int,com.sun.codemodel.JType,java.lang.String)
meth public com.sun.codemodel.JVar param(int,java.lang.Class<?>,java.lang.String)
meth public com.sun.codemodel.JVar param(java.lang.Class<?>,java.lang.String)
meth public com.sun.codemodel.JVar varParam(com.sun.codemodel.JType,java.lang.String)
meth public com.sun.codemodel.JVar varParam(java.lang.Class<?>,java.lang.String)
meth public com.sun.codemodel.JVar[] listParams()
meth public java.lang.String name()
meth public java.util.Collection<com.sun.codemodel.JAnnotationUse> annotations()
meth public java.util.List<com.sun.codemodel.JVar> params()
meth public void declare(com.sun.codemodel.JFormatter)
meth public void declareDefaultValue(com.sun.codemodel.JExpression)
meth public void name(java.lang.String)
meth public void type(com.sun.codemodel.JType)
supr java.lang.Object
hfds _throws,annotations,body,defaultValue,jdoc,mods,name,outer,params,type,varParam

CLSS public final com.sun.codemodel.JMod
fld public final static int ABSTRACT = 32
fld public final static int FINAL = 8
fld public final static int NATIVE = 64
fld public final static int NONE = 0
fld public final static int PRIVATE = 4
fld public final static int PROTECTED = 2
fld public final static int PUBLIC = 1
fld public final static int STATIC = 16
fld public final static int SYNCHRONIZED = 128
fld public final static int TRANSIENT = 256
fld public final static int VOLATILE = 512
supr java.lang.Object

CLSS public com.sun.codemodel.JMods
intf com.sun.codemodel.JGenerable
meth public boolean isAbstract()
meth public boolean isNative()
meth public boolean isSynchronized()
meth public int getValue()
meth public java.lang.String toString()
meth public void generate(com.sun.codemodel.JFormatter)
meth public void setFinal(boolean)
meth public void setPrivate()
meth public void setProtected()
meth public void setPublic()
meth public void setSynchronized(boolean)
supr java.lang.Object
hfds CLASS,FIELD,INTERFACE,METHOD,VAR,mods

CLSS public com.sun.codemodel.JModule
meth public !varargs void _requires(boolean,boolean,java.lang.String[])
meth public !varargs void _requires(java.lang.String[])
meth public com.sun.codemodel.JFormatter generate(com.sun.codemodel.JFormatter)
meth public java.lang.String name()
meth public void _exports(com.sun.codemodel.JPackage)
meth public void _exports(java.util.Collection<com.sun.codemodel.JPackage>,boolean)
meth public void _requires(java.lang.String)
meth public void _requires(java.lang.String,boolean,boolean)
supr java.lang.Object
hfds FILE_NAME,directives,name

CLSS public abstract com.sun.codemodel.JModuleDirective
fld protected final java.lang.String name
innr public final static !enum Type
meth public abstract com.sun.codemodel.JFormatter generate(com.sun.codemodel.JFormatter)
meth public abstract com.sun.codemodel.JModuleDirective$Type getType()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String name()
supr java.lang.Object

CLSS public final static !enum com.sun.codemodel.JModuleDirective$Type
 outer com.sun.codemodel.JModuleDirective
fld public final static com.sun.codemodel.JModuleDirective$Type ExportsDirective
fld public final static com.sun.codemodel.JModuleDirective$Type RequiresDirective
meth public static com.sun.codemodel.JModuleDirective$Type valueOf(java.lang.String)
meth public static com.sun.codemodel.JModuleDirective$Type[] values()
supr java.lang.Enum<com.sun.codemodel.JModuleDirective$Type>

CLSS public final com.sun.codemodel.JNullType
meth protected com.sun.codemodel.JClass substituteParams(com.sun.codemodel.JTypeVar[],java.util.List<com.sun.codemodel.JClass>)
meth public boolean isAbstract()
meth public boolean isInterface()
meth public com.sun.codemodel.JClass _extends()
meth public com.sun.codemodel.JPackage _package()
meth public java.lang.String fullName()
meth public java.lang.String name()
meth public java.util.Iterator<com.sun.codemodel.JClass> _implements()
supr com.sun.codemodel.JClass

CLSS public abstract com.sun.codemodel.JOp
meth public static com.sun.codemodel.JExpression _instanceof(com.sun.codemodel.JExpression,com.sun.codemodel.JType)
meth public static com.sun.codemodel.JExpression band(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression bor(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression cand(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression complement(com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression cond(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression cor(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression decr(com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression div(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression eq(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression gt(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression gte(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression incr(com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression lt(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression lte(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression minus(com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression minus(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression mod(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression mul(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression ne(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression not(com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression plus(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression shl(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression shr(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression shrz(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
meth public static com.sun.codemodel.JExpression xor(com.sun.codemodel.JExpression,com.sun.codemodel.JExpression)
supr java.lang.Object
hcls BinaryOp,TernaryOp,TightUnaryOp,UnaryOp

CLSS public final com.sun.codemodel.JPackage
intf com.sun.codemodel.JAnnotatable
intf com.sun.codemodel.JClassContainer
intf com.sun.codemodel.JDeclaration
intf com.sun.codemodel.JDocCommentable
intf com.sun.codemodel.JGenerable
intf java.lang.Comparable<com.sun.codemodel.JPackage>
meth public <%0 extends com.sun.codemodel.JAnnotationWriter<? extends java.lang.annotation.Annotation>> {%%0} annotate2(java.lang.Class<{%%0}>)
meth public boolean hasClasses()
meth public boolean hasResourceFile(java.lang.String)
meth public boolean isClass()
meth public boolean isDefined(java.lang.String)
meth public boolean isPackage()
meth public boolean removeAnnotation(com.sun.codemodel.JAnnotationUse)
meth public com.sun.codemodel.JAnnotationUse annotate(com.sun.codemodel.JClass)
meth public com.sun.codemodel.JAnnotationUse annotate(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public com.sun.codemodel.JClass ref(java.lang.String) throws java.lang.ClassNotFoundException
meth public com.sun.codemodel.JClassContainer parentContainer()
meth public com.sun.codemodel.JDefinedClass _annotationTypeDeclaration(java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDefinedClass _class(int,java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDefinedClass _class(int,java.lang.String,boolean) throws com.sun.codemodel.JClassAlreadyExistsException
 anno 0 java.lang.Deprecated()
meth public com.sun.codemodel.JDefinedClass _class(int,java.lang.String,com.sun.codemodel.ClassType) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDefinedClass _class(java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDefinedClass _enum(java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDefinedClass _getClass(java.lang.String)
meth public com.sun.codemodel.JDefinedClass _interface(int,java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDefinedClass _interface(java.lang.String) throws com.sun.codemodel.JClassAlreadyExistsException
meth public com.sun.codemodel.JDocComment javadoc()
meth public com.sun.codemodel.JPackage getPackage()
meth public com.sun.codemodel.JPackage parent()
meth public com.sun.codemodel.JPackage subPackage(java.lang.String)
meth public com.sun.codemodel.JResourceFile addResourceFile(com.sun.codemodel.JResourceFile)
meth public final boolean isUnnamed()
meth public final com.sun.codemodel.JCodeModel owner()
meth public int compareTo(com.sun.codemodel.JPackage)
meth public java.lang.String name()
meth public java.util.Collection<com.sun.codemodel.JAnnotationUse> annotations()
meth public java.util.Iterator<com.sun.codemodel.JDefinedClass> classes()
meth public java.util.Iterator<com.sun.codemodel.JResourceFile> propertyFiles()
meth public void declare(com.sun.codemodel.JFormatter)
meth public void generate(com.sun.codemodel.JFormatter)
meth public void remove(com.sun.codemodel.JClass)
supr java.lang.Object
hfds annotations,classes,jdoc,name,owner,resources,upperCaseClassMap

CLSS public final com.sun.codemodel.JPrimitiveType
meth public boolean isPrimitive()
meth public com.sun.codemodel.JClass array()
meth public com.sun.codemodel.JClass boxify()
meth public com.sun.codemodel.JClass getWrapperClass()
 anno 0 java.lang.Deprecated()
meth public com.sun.codemodel.JCodeModel owner()
meth public com.sun.codemodel.JExpression unwrap(com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JExpression wrap(com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JType unboxify()
 anno 0 java.lang.Deprecated()
meth public java.lang.String fullName()
meth public java.lang.String name()
meth public void generate(com.sun.codemodel.JFormatter)
supr com.sun.codemodel.JType
hfds arrayClass,owner,typeName,wrapperClass

CLSS public com.sun.codemodel.JRequiresDirective
meth protected void generateModifiers(com.sun.codemodel.JFormatter)
meth public com.sun.codemodel.JFormatter generate(com.sun.codemodel.JFormatter)
meth public com.sun.codemodel.JModuleDirective$Type getType()
supr com.sun.codemodel.JModuleDirective
hfds isPublic,isStatic

CLSS public abstract com.sun.codemodel.JResourceFile
cons protected init(java.lang.String)
meth protected abstract void build(java.io.OutputStream) throws java.io.IOException
meth protected boolean isResource()
meth public java.lang.String name()
supr java.lang.Object
hfds name

CLSS public abstract interface com.sun.codemodel.JStatement
meth public abstract void state(com.sun.codemodel.JFormatter)

CLSS public com.sun.codemodel.JStringLiteral
fld public final java.lang.String str
meth public void generate(com.sun.codemodel.JFormatter)
supr com.sun.codemodel.JExpressionImpl

CLSS public final com.sun.codemodel.JSwitch
intf com.sun.codemodel.JStatement
meth public com.sun.codemodel.JCase _case(com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JCase _default()
meth public com.sun.codemodel.JExpression test()
meth public java.util.Iterator<com.sun.codemodel.JCase> cases()
meth public void state(com.sun.codemodel.JFormatter)
supr java.lang.Object
hfds cases,defaultCase,test

CLSS public com.sun.codemodel.JTryBlock
intf com.sun.codemodel.JStatement
meth public com.sun.codemodel.JBlock _finally()
meth public com.sun.codemodel.JBlock body()
meth public com.sun.codemodel.JCatchBlock _catch(com.sun.codemodel.JClass)
meth public void state(com.sun.codemodel.JFormatter)
supr java.lang.Object
hfds _finally,body,catches

CLSS public abstract com.sun.codemodel.JType
cons protected init()
intf com.sun.codemodel.JGenerable
intf java.lang.Comparable<com.sun.codemodel.JType>
meth public abstract com.sun.codemodel.JClass array()
meth public abstract com.sun.codemodel.JClass boxify()
meth public abstract com.sun.codemodel.JCodeModel owner()
meth public abstract com.sun.codemodel.JType unboxify()
meth public abstract java.lang.String fullName()
meth public abstract java.lang.String name()
meth public boolean isArray()
meth public boolean isPrimitive()
meth public com.sun.codemodel.JType elementType()
meth public com.sun.codemodel.JType erasure()
meth public final boolean isReference()
meth public int compareTo(com.sun.codemodel.JType)
meth public java.lang.String binaryName()
meth public java.lang.String toString()
meth public static com.sun.codemodel.JPrimitiveType parse(com.sun.codemodel.JCodeModel,java.lang.String)
supr java.lang.Object

CLSS public final com.sun.codemodel.JTypeVar
intf com.sun.codemodel.JDeclaration
meth protected com.sun.codemodel.JClass substituteParams(com.sun.codemodel.JTypeVar[],java.util.List<com.sun.codemodel.JClass>)
meth public boolean isAbstract()
meth public boolean isInterface()
meth public com.sun.codemodel.JClass _extends()
meth public com.sun.codemodel.JPackage _package()
meth public com.sun.codemodel.JTypeVar bound(com.sun.codemodel.JClass)
meth public java.lang.String fullName()
meth public java.lang.String name()
meth public java.util.Iterator<com.sun.codemodel.JClass> _implements()
meth public void declare(com.sun.codemodel.JFormatter)
meth public void generate(com.sun.codemodel.JFormatter)
supr com.sun.codemodel.JClass
hfds bound,name

CLSS public com.sun.codemodel.JVar
intf com.sun.codemodel.JAnnotatable
intf com.sun.codemodel.JAssignmentTarget
intf com.sun.codemodel.JDeclaration
meth protected boolean isAnnotated()
meth public <%0 extends com.sun.codemodel.JAnnotationWriter<? extends java.lang.annotation.Annotation>> {%%0} annotate2(java.lang.Class<{%%0}>)
meth public boolean removeAnnotation(com.sun.codemodel.JAnnotationUse)
meth public com.sun.codemodel.JAnnotationUse annotate(com.sun.codemodel.JClass)
meth public com.sun.codemodel.JAnnotationUse annotate(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public com.sun.codemodel.JExpression assign(com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JExpression assignPlus(com.sun.codemodel.JExpression)
meth public com.sun.codemodel.JMods mods()
meth public com.sun.codemodel.JType type()
meth public com.sun.codemodel.JType type(com.sun.codemodel.JType)
meth public com.sun.codemodel.JVar init(com.sun.codemodel.JExpression)
meth public java.lang.String name()
meth public java.util.Collection<com.sun.codemodel.JAnnotationUse> annotations()
meth public void bind(com.sun.codemodel.JFormatter)
meth public void declare(com.sun.codemodel.JFormatter)
meth public void generate(com.sun.codemodel.JFormatter)
meth public void name(java.lang.String)
supr com.sun.codemodel.JExpressionImpl
hfds annotations,init,mods,name,type

CLSS public com.sun.codemodel.JWhileLoop
intf com.sun.codemodel.JStatement
meth public com.sun.codemodel.JBlock body()
meth public com.sun.codemodel.JExpression test()
meth public void state(com.sun.codemodel.JFormatter)
supr java.lang.Object
hfds body,test

CLSS public abstract interface com.sun.istack.Builder<%0 extends java.lang.Object>
meth public abstract {com.sun.istack.Builder%0} build()

CLSS public final com.sun.istack.ByteArrayDataSource
cons public init(byte[],int,java.lang.String)
cons public init(byte[],java.lang.String)
intf javax.activation.DataSource
meth public java.io.InputStream getInputStream()
meth public java.io.OutputStream getOutputStream()
meth public java.lang.String getContentType()
meth public java.lang.String getName()
supr java.lang.Object
hfds buf,contentType,len

CLSS public final com.sun.istack.FinalArrayList<%0 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(java.util.Collection<? extends {com.sun.istack.FinalArrayList%0}>)
supr java.util.ArrayList<{com.sun.istack.FinalArrayList%0}>

CLSS public com.sun.istack.FragmentContentHandler
cons public init()
cons public init(org.xml.sax.ContentHandler)
cons public init(org.xml.sax.XMLReader)
meth public void endDocument() throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
supr org.xml.sax.helpers.XMLFilterImpl

CLSS public abstract interface !annotation com.sun.istack.Interned
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD, METHOD, PARAMETER, LOCAL_VARIABLE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.sun.istack.NotNull
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD, METHOD, PARAMETER, LOCAL_VARIABLE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.sun.istack.Nullable
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD, METHOD, PARAMETER, LOCAL_VARIABLE])
intf java.lang.annotation.Annotation

CLSS public abstract interface com.sun.istack.Pool<%0 extends java.lang.Object>
innr public abstract static Impl
meth public abstract void recycle({com.sun.istack.Pool%0})
 anno 1 com.sun.istack.NotNull()
meth public abstract {com.sun.istack.Pool%0} take()
 anno 0 com.sun.istack.NotNull()

CLSS public abstract static com.sun.istack.Pool$Impl<%0 extends java.lang.Object>
 outer com.sun.istack.Pool
cons public init()
intf com.sun.istack.Pool<{com.sun.istack.Pool$Impl%0}>
meth protected abstract {com.sun.istack.Pool$Impl%0} create()
 anno 0 com.sun.istack.NotNull()
meth public final void recycle({com.sun.istack.Pool$Impl%0})
meth public final {com.sun.istack.Pool$Impl%0} take()
 anno 0 com.sun.istack.NotNull()
supr java.lang.Object
hfds queue

CLSS public com.sun.istack.SAXException2
cons public init(java.lang.Exception)
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
meth public java.lang.Throwable getCause()
supr org.xml.sax.SAXException

CLSS public com.sun.istack.SAXParseException2
cons public init(java.lang.String,java.lang.String,java.lang.String,int,int)
cons public init(java.lang.String,java.lang.String,java.lang.String,int,int,java.lang.Exception)
cons public init(java.lang.String,org.xml.sax.Locator)
cons public init(java.lang.String,org.xml.sax.Locator,java.lang.Exception)
meth public java.lang.Throwable getCause()
supr org.xml.sax.SAXParseException

CLSS public com.sun.istack.XMLStreamException2
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,javax.xml.stream.Location)
cons public init(java.lang.String,javax.xml.stream.Location,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public java.lang.Throwable getCause()
supr javax.xml.stream.XMLStreamException

CLSS public com.sun.istack.XMLStreamReaderToContentHandler
cons public init(javax.xml.stream.XMLStreamReader,org.xml.sax.ContentHandler,boolean,boolean)
cons public init(javax.xml.stream.XMLStreamReader,org.xml.sax.ContentHandler,boolean,boolean,java.lang.String[])
meth public void bridge() throws javax.xml.stream.XMLStreamException
supr java.lang.Object
hfds eagerQuit,fragment,inscopeNamespaces,saxHandler,staxStreamReader

CLSS public abstract interface com.sun.istack.localization.Localizable
fld public final static java.lang.String NOT_LOCALIZABLE = "\u0000"
meth public abstract java.lang.Object[] getArguments()
meth public abstract java.lang.String getKey()
meth public abstract java.lang.String getResourceBundleName()
meth public abstract java.util.ResourceBundle getResourceBundle(java.util.Locale)

CLSS public final com.sun.istack.localization.LocalizableMessage
cons public !varargs init(java.lang.String,com.sun.istack.localization.LocalizableMessageFactory$ResourceBundleSupplier,java.lang.String,java.lang.Object[])
cons public !varargs init(java.lang.String,java.lang.String,java.lang.Object[])
 anno 0 java.lang.Deprecated()
intf com.sun.istack.localization.Localizable
meth public java.lang.Object[] getArguments()
meth public java.lang.String getKey()
meth public java.lang.String getResourceBundleName()
meth public java.util.ResourceBundle getResourceBundle(java.util.Locale)
supr java.lang.Object
hfds _args,_bundlename,_key,_rbSupplier

CLSS public com.sun.istack.localization.LocalizableMessageFactory
cons public init(java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,com.sun.istack.localization.LocalizableMessageFactory$ResourceBundleSupplier)
innr public abstract interface static ResourceBundleSupplier
meth public !varargs com.sun.istack.localization.Localizable getMessage(java.lang.String,java.lang.Object[])
supr java.lang.Object
hfds _bundlename,_rbSupplier

CLSS public abstract interface static com.sun.istack.localization.LocalizableMessageFactory$ResourceBundleSupplier
 outer com.sun.istack.localization.LocalizableMessageFactory
meth public abstract java.util.ResourceBundle getResourceBundle(java.util.Locale)

CLSS public com.sun.istack.localization.Localizer
cons public init()
cons public init(java.util.Locale)
meth public java.lang.String localize(com.sun.istack.localization.Localizable)
meth public java.util.Locale getLocale()
supr java.lang.Object
hfds _locale,_resourceBundles

CLSS public final com.sun.istack.localization.NullLocalizable
cons public init(java.lang.String)
intf com.sun.istack.localization.Localizable
meth public java.lang.Object[] getArguments()
meth public java.lang.String getKey()
meth public java.lang.String getResourceBundleName()
meth public java.util.ResourceBundle getResourceBundle(java.util.Locale)
supr java.lang.Object
hfds msg

CLSS public com.sun.istack.logging.Logger
cons protected init(java.lang.String,java.lang.String)
meth public !varargs void entering(java.lang.Object[])
meth public <%0 extends java.lang.Throwable> {%%0} logException({%%0},boolean,java.util.logging.Level)
meth public <%0 extends java.lang.Throwable> {%%0} logException({%%0},java.lang.Throwable,java.util.logging.Level)
meth public <%0 extends java.lang.Throwable> {%%0} logException({%%0},java.util.logging.Level)
meth public <%0 extends java.lang.Throwable> {%%0} logSevereException({%%0})
meth public <%0 extends java.lang.Throwable> {%%0} logSevereException({%%0},boolean)
meth public <%0 extends java.lang.Throwable> {%%0} logSevereException({%%0},java.lang.Throwable)
meth public boolean isLoggable(java.util.logging.Level)
meth public boolean isMethodCallLoggable()
meth public static com.sun.istack.logging.Logger getLogger(java.lang.Class<?>)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public static com.sun.istack.logging.Logger getLogger(java.lang.String,java.lang.Class<?>)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public void config(java.lang.String)
meth public void config(java.lang.String,java.lang.Object[])
meth public void config(java.lang.String,java.lang.Throwable)
meth public void entering()
meth public void exiting()
meth public void exiting(java.lang.Object)
meth public void fine(java.lang.String)
meth public void fine(java.lang.String,java.lang.Throwable)
meth public void finer(java.lang.String)
meth public void finer(java.lang.String,java.lang.Object[])
meth public void finer(java.lang.String,java.lang.Throwable)
meth public void finest(java.lang.String)
meth public void finest(java.lang.String,java.lang.Object[])
meth public void finest(java.lang.String,java.lang.Throwable)
meth public void info(java.lang.String)
meth public void info(java.lang.String,java.lang.Object[])
meth public void info(java.lang.String,java.lang.Throwable)
meth public void log(java.util.logging.Level,java.lang.String)
meth public void log(java.util.logging.Level,java.lang.String,java.lang.Object)
meth public void log(java.util.logging.Level,java.lang.String,java.lang.Object[])
meth public void log(java.util.logging.Level,java.lang.String,java.lang.Throwable)
meth public void setLevel(java.util.logging.Level)
meth public void severe(java.lang.String)
meth public void severe(java.lang.String,java.lang.Object[])
meth public void severe(java.lang.String,java.lang.Throwable)
meth public void warning(java.lang.String)
meth public void warning(java.lang.String,java.lang.Object[])
meth public void warning(java.lang.String,java.lang.Throwable)
supr java.lang.Object
hfds METHOD_CALL_LEVEL_VALUE,ROOT_WS_PACKAGE,WS_LOGGING_SUBSYSTEM_NAME_ROOT,componentClassName,logger

CLSS public com.sun.istack.tools.DefaultAuthenticator
innr public abstract interface static Receiver
meth protected java.net.PasswordAuthentication getPasswordAuthentication()
meth public static com.sun.istack.tools.DefaultAuthenticator getAuthenticator()
meth public static void reset()
meth public void setAuth(java.io.File,com.sun.istack.tools.DefaultAuthenticator$Receiver)
meth public void setProxyAuth(java.lang.String)
supr java.net.Authenticator
hfds LOGGER,authInfo,counter,instance,proxyPasswd,proxyUser,systemAuthenticator
hcls AuthInfo,DefaultRImpl

CLSS public abstract interface static com.sun.istack.tools.DefaultAuthenticator$Receiver
 outer com.sun.istack.tools.DefaultAuthenticator
meth public abstract void onError(java.lang.Exception,org.xml.sax.Locator)
meth public abstract void onParsingError(java.lang.String,org.xml.sax.Locator)

CLSS public com.sun.istack.tools.MaskingClassLoader
cons public !varargs init(java.lang.ClassLoader,java.lang.String[])
cons public !varargs init(java.lang.String[])
cons public init(java.lang.ClassLoader,java.util.Collection<java.lang.String>)
cons public init(java.util.Collection<java.lang.String>)
meth protected java.lang.Class<?> loadClass(java.lang.String,boolean) throws java.lang.ClassNotFoundException
supr java.lang.ClassLoader
hfds masks

CLSS public com.sun.istack.tools.ParallelWorldClassLoader
cons public init(java.lang.ClassLoader,java.lang.String)
intf java.io.Closeable
meth protected java.lang.Class findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.net.URL findResource(java.lang.String)
meth protected java.util.Enumeration<java.net.URL> findResources(java.lang.String) throws java.io.IOException
meth public static java.net.URL toJarUrl(java.net.URL) throws java.lang.ClassNotFoundException,java.net.MalformedURLException
meth public void close() throws java.io.IOException
supr java.lang.ClassLoader
hfds jars,prefix

CLSS public abstract com.sun.istack.tools.ProtectedTask
hfds root
hcls AntElement

CLSS public abstract interface com.sun.tools.rngom.digested.DPatternVisitor<%0 extends java.lang.Object>
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onAttribute(com.sun.tools.rngom.digested.DAttributePattern)
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onChoice(com.sun.tools.rngom.digested.DChoicePattern)
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onData(com.sun.tools.rngom.digested.DDataPattern)
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onElement(com.sun.tools.rngom.digested.DElementPattern)
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onEmpty(com.sun.tools.rngom.digested.DEmptyPattern)
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onGrammar(com.sun.tools.rngom.digested.DGrammarPattern)
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onGroup(com.sun.tools.rngom.digested.DGroupPattern)
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onInterleave(com.sun.tools.rngom.digested.DInterleavePattern)
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onList(com.sun.tools.rngom.digested.DListPattern)
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onMixed(com.sun.tools.rngom.digested.DMixedPattern)
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onNotAllowed(com.sun.tools.rngom.digested.DNotAllowedPattern)
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onOneOrMore(com.sun.tools.rngom.digested.DOneOrMorePattern)
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onOptional(com.sun.tools.rngom.digested.DOptionalPattern)
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onRef(com.sun.tools.rngom.digested.DRefPattern)
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onText(com.sun.tools.rngom.digested.DTextPattern)
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onValue(com.sun.tools.rngom.digested.DValuePattern)
meth public abstract {com.sun.tools.rngom.digested.DPatternVisitor%0} onZeroOrMore(com.sun.tools.rngom.digested.DZeroOrMorePattern)

CLSS public com.sun.tools.rngom.digested.DPatternWalker
cons public init()
intf com.sun.tools.rngom.digested.DPatternVisitor<java.lang.Void>
meth protected java.lang.Void onContainer(com.sun.tools.rngom.digested.DContainerPattern)
meth protected java.lang.Void onUnary(com.sun.tools.rngom.digested.DUnaryPattern)
meth protected java.lang.Void onXmlToken(com.sun.tools.rngom.digested.DXmlTokenPattern)
meth public java.lang.Void onAttribute(com.sun.tools.rngom.digested.DAttributePattern)
meth public java.lang.Void onChoice(com.sun.tools.rngom.digested.DChoicePattern)
meth public java.lang.Void onData(com.sun.tools.rngom.digested.DDataPattern)
meth public java.lang.Void onElement(com.sun.tools.rngom.digested.DElementPattern)
meth public java.lang.Void onEmpty(com.sun.tools.rngom.digested.DEmptyPattern)
meth public java.lang.Void onGrammar(com.sun.tools.rngom.digested.DGrammarPattern)
meth public java.lang.Void onGroup(com.sun.tools.rngom.digested.DGroupPattern)
meth public java.lang.Void onInterleave(com.sun.tools.rngom.digested.DInterleavePattern)
meth public java.lang.Void onList(com.sun.tools.rngom.digested.DListPattern)
meth public java.lang.Void onMixed(com.sun.tools.rngom.digested.DMixedPattern)
meth public java.lang.Void onNotAllowed(com.sun.tools.rngom.digested.DNotAllowedPattern)
meth public java.lang.Void onOneOrMore(com.sun.tools.rngom.digested.DOneOrMorePattern)
meth public java.lang.Void onOptional(com.sun.tools.rngom.digested.DOptionalPattern)
meth public java.lang.Void onRef(com.sun.tools.rngom.digested.DRefPattern)
meth public java.lang.Void onText(com.sun.tools.rngom.digested.DTextPattern)
meth public java.lang.Void onValue(com.sun.tools.rngom.digested.DValuePattern)
meth public java.lang.Void onZeroOrMore(com.sun.tools.rngom.digested.DZeroOrMorePattern)
supr java.lang.Object

CLSS public com.sun.tools.xjc.AbortException
cons public init()
supr java.lang.RuntimeException

CLSS public com.sun.tools.xjc.BadCommandLineException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
meth public com.sun.tools.xjc.Options getOptions()
 anno 0 com.sun.istack.Nullable()
meth public void initOptions(com.sun.tools.xjc.Options)
supr java.lang.Exception
hfds options

CLSS public com.sun.tools.xjc.ConsoleErrorReporter
cons public init()
cons public init(java.io.OutputStream)
cons public init(java.io.PrintStream)
meth public boolean hadError()
meth public void error(org.xml.sax.SAXParseException)
meth public void fatalError(org.xml.sax.SAXParseException)
meth public void info(org.xml.sax.SAXParseException)
meth public void warning(org.xml.sax.SAXParseException)
supr com.sun.tools.xjc.ErrorReceiver
hfds hadError,output

CLSS public com.sun.tools.xjc.Driver
cons public init()
meth public static int run(java.lang.String[],com.sun.tools.xjc.XJCListener) throws com.sun.tools.xjc.BadCommandLineException
 anno 2 com.sun.istack.NotNull()
meth public static int run(java.lang.String[],java.io.PrintStream,java.io.PrintStream) throws java.lang.Exception
meth public static java.lang.String getBuildID()
meth public static void main(java.lang.String[]) throws java.lang.Exception
meth public static void usage(com.sun.tools.xjc.Options,boolean)
 anno 1 com.sun.istack.Nullable()
supr java.lang.Object
hfds SYSTEM_PROXY_PROPERTY
hcls Mode,OptionsEx,WeAreDone

CLSS public abstract com.sun.tools.xjc.ErrorReceiver
cons public init()
intf com.sun.tools.xjc.api.ErrorListener
intf org.xml.sax.ErrorHandler
meth protected final java.lang.String getLocationString(org.xml.sax.SAXParseException)
meth public abstract void error(org.xml.sax.SAXParseException)
meth public abstract void fatalError(org.xml.sax.SAXParseException)
meth public abstract void info(org.xml.sax.SAXParseException)
meth public abstract void warning(org.xml.sax.SAXParseException)
meth public final void debug(java.lang.String)
meth public final void error(java.lang.String,java.lang.Exception)
meth public final void error(org.xml.sax.Locator,java.lang.String)
meth public final void error(org.xml.sax.Locator,java.lang.String,java.lang.Exception)
meth public final void warning(org.xml.sax.Locator,java.lang.String)
meth public void error(java.lang.Exception)
meth public void pollAbort()
supr java.lang.Object

CLSS public final !enum com.sun.tools.xjc.Language
fld public final static com.sun.tools.xjc.Language DTD
fld public final static com.sun.tools.xjc.Language WSDL
fld public final static com.sun.tools.xjc.Language XMLSCHEMA
meth public static com.sun.tools.xjc.Language valueOf(java.lang.String)
meth public static com.sun.tools.xjc.Language[] values()
supr java.lang.Enum<com.sun.tools.xjc.Language>

CLSS public com.sun.tools.xjc.Messages
cons public init()
meth public !varargs static java.lang.String format(java.lang.String,java.lang.Object[])
supr java.lang.Object
hfds ADDON_USAGE,AT,BUILD_ID,COMPILING_SCHEMA,DATE_FORMAT,DEFAULT_PACKAGE_WARNING,DEFAULT_VERSION,DRIVER_PRIVATE_USAGE,DRIVER_PUBLIC_USAGE,ERROR_MSG,ERR_BINDING_FILE_NOT_SUPPORTED_FOR_RNC,ERR_NOT_A_BINDING_FILE,ERR_TOO_MANY_SCHEMA,EXPERIMENTAL_LANGUAGE_WARNING,FAILED_TO_GENERATE_CODE,FAILED_TO_LOAD,FAILED_TO_PARSE,FIELD_RENDERER_CONFLICT,FILE_PROLOG_COMMENT,FULLVERSION,ILLEGAL_PROXY,ILLEGAL_TARGET_VERSION,INFO_MSG,INVALID_JAVA_MODULE_NAME,LINE_X_OF_Y,MISSING_GRAMMAR,MISSING_MODE_OPERAND,MISSING_OPERAND,MISSING_PROXY,MISSING_PROXYFILE,MISSING_PROXYHOST,MISSING_PROXYPORT,NAME_CONVERTER_CONFLICT,NON_EXISTENT_DIR,NOT_A_FILE_NOR_URL,NOT_A_VALID_FILENAME,NO_SUCH_FILE,PARSE_FAILED,PARSING_SCHEMA,PLUGIN_LOAD_FAILURE,STACK_OVERFLOW,TIME_FORMAT,UNKNOWN_FILE,UNKNOWN_LOCATION,UNRECOGNIZED_MODE,UNRECOGNIZED_PARAMETER,UNSUPPORTED_ENCODING,VERSION,WARNING_MSG

CLSS public final com.sun.tools.xjc.ModelLoader
cons public init(com.sun.tools.xjc.Options,com.sun.codemodel.JCodeModel,com.sun.tools.xjc.ErrorReceiver)
meth public com.sun.tools.xjc.model.Model annotateXMLSchema(com.sun.xml.xsom.XSSchemaSet)
meth public com.sun.tools.xjc.reader.internalizer.DOMForest buildDOMForest(com.sun.tools.xjc.reader.internalizer.InternalizationLogic) throws org.xml.sax.SAXException
meth public com.sun.xml.xsom.XSSchemaSet createXSOM(com.sun.tools.xjc.reader.internalizer.DOMForest,com.sun.tools.xjc.reader.internalizer.SCDBasedBindingSet) throws org.xml.sax.SAXException
meth public com.sun.xml.xsom.XSSchemaSet loadXMLSchema() throws org.xml.sax.SAXException
meth public com.sun.xml.xsom.parser.XSOMParser createXSOMParser(com.sun.tools.xjc.reader.internalizer.DOMForest)
meth public com.sun.xml.xsom.parser.XSOMParser createXSOMParser(com.sun.xml.xsom.parser.XMLParser)
meth public static com.sun.tools.xjc.model.Model load(com.sun.tools.xjc.Options,com.sun.codemodel.JCodeModel,com.sun.tools.xjc.ErrorReceiver)
supr java.lang.Object
hfds codeModel,errorReceiver,opt,scdBasedBindingSet
hcls SpeculationChecker,SpeculationFailure,XMLSchemaParser

CLSS public com.sun.tools.xjc.Options
cons public init()
fld public boolean automaticNameConflictResolution
fld public boolean contentForWildcard
fld public boolean debugMode
fld public boolean disableXmlSecurity
fld public boolean enableIntrospection
fld public boolean noFileHeader
fld public boolean packageLevelAnnotations
fld public boolean quiet
fld public boolean readOnly
fld public boolean runtime14
fld public boolean strictCheck
fld public boolean verbose
fld public com.sun.tools.xjc.api.ClassNameAllocator classNameAllocator
fld public com.sun.tools.xjc.api.SpecVersion target
fld public final java.util.List<com.sun.tools.xjc.Plugin> activePlugins
fld public final java.util.List<java.net.URL> classpaths
fld public final java.util.Set<java.lang.String> pluginURIs
fld public final static int EXTENSION = 2
fld public final static int STRICT = 1
fld public int compatibilityMode
fld public java.io.File targetDir
fld public java.lang.String defaultPackage
fld public java.lang.String defaultPackage2
fld public java.lang.String encoding
fld public java.lang.String proxyAuth
fld public org.xml.sax.EntityResolver entityResolver
meth public boolean isExtensionMode()
meth public com.sun.codemodel.CodeWriter createCodeWriter() throws java.io.IOException
meth public com.sun.codemodel.CodeWriter createCodeWriter(com.sun.codemodel.CodeWriter)
meth public com.sun.tools.xjc.Language getSchemaLanguage()
meth public com.sun.tools.xjc.Language guessSchemaLanguage()
meth public com.sun.tools.xjc.generator.bean.field.FieldRendererFactory getFieldRendererFactory()
meth public com.sun.xml.bind.api.impl.NameConverter getNameConverter()
meth public int parseArgument(java.lang.String[],int) throws com.sun.tools.xjc.BadCommandLineException
meth public java.lang.ClassLoader getUserClassLoader(java.lang.ClassLoader)
meth public java.lang.String getModuleName()
meth public java.lang.String getPrologComment()
meth public java.lang.String requireArgument(java.lang.String,java.lang.String[],int) throws com.sun.tools.xjc.BadCommandLineException
meth public java.util.List<com.sun.tools.xjc.Plugin> getAllPlugins()
meth public org.xml.sax.InputSource[] getBindFiles()
meth public org.xml.sax.InputSource[] getGrammars()
meth public static java.lang.String getBuildID()
meth public static java.lang.String normalizeSystemId(java.lang.String)
meth public void addBindFile(java.io.File)
meth public void addBindFile(org.xml.sax.InputSource)
meth public void addBindFileRecursive(java.io.File)
meth public void addCatalog(java.io.File) throws java.io.IOException
meth public void addGrammar(java.io.File)
meth public void addGrammar(org.xml.sax.InputSource)
meth public void addGrammarRecursive(java.io.File)
meth public void parseArguments(java.lang.String[]) throws com.sun.tools.xjc.BadCommandLineException
meth public void scanEpisodeFile(java.io.File) throws com.sun.tools.xjc.BadCommandLineException
meth public void setFieldRendererFactory(com.sun.tools.xjc.generator.bean.field.FieldRendererFactory,com.sun.tools.xjc.Plugin) throws com.sun.tools.xjc.BadCommandLineException
meth public void setNameConverter(com.sun.xml.bind.api.impl.NameConverter,com.sun.tools.xjc.Plugin) throws com.sun.tools.xjc.BadCommandLineException
meth public void setSchemaLanguage(com.sun.tools.xjc.Language)
supr java.lang.Object
hfds allPlugins,bindFiles,catalogUrls,fieldRendererFactory,fieldRendererFactoryOwner,grammars,javaModule,logger,nameConverter,nameConverterOwner,pluginLoadFailure,proxyHost,proxyPort,schemaLanguage

CLSS public abstract com.sun.tools.xjc.Plugin
cons public init()
meth public abstract boolean run(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.Options,org.xml.sax.ErrorHandler) throws org.xml.sax.SAXException
meth public abstract java.lang.String getOptionName()
meth public abstract java.lang.String getUsage()
meth public boolean isCustomizationTagName(java.lang.String,java.lang.String)
meth public int parseArgument(com.sun.tools.xjc.Options,java.lang.String[],int) throws com.sun.tools.xjc.BadCommandLineException,java.io.IOException
meth public java.util.List<java.lang.String> getCustomizationURIs()
meth public void onActivated(com.sun.tools.xjc.Options) throws com.sun.tools.xjc.BadCommandLineException
meth public void postProcessModel(com.sun.tools.xjc.model.Model,org.xml.sax.ErrorHandler)
supr java.lang.Object

CLSS public final com.sun.tools.xjc.SchemaCache
cons public init(java.lang.String,java.lang.Class<?>)
cons public init(java.lang.String,java.lang.Class<?>,boolean)
meth public javax.xml.validation.ValidatorHandler newValidator()
supr java.lang.Object
hfds clazz,createResolver,resourceName,schema
hcls ResourceResolver

CLSS public com.sun.tools.xjc.XJC2Task

CLSS public com.sun.tools.xjc.XJCBase
hfds addexports,addmodules,addopens,addreads,bindingFiles,catalog,classpath,cmd,cmdLine,dependsSet,extension,failonerror,fork,limitmodules,modulepath,patchmodule,producesSet,producesSpecified,removeOldOutput,schemaFiles,specTarget,stackSize,upgrademodulepath,xmlCatalog
hcls AntProgressCodeWriter,ErrorReceiverImpl

CLSS public com.sun.tools.xjc.XJCFacade
cons public init()
meth public static java.lang.String parseVersion(java.lang.String)
meth public static void main(java.lang.String[]) throws java.lang.Throwable
supr java.lang.Object
hfds JDK_REQUIRED

CLSS public abstract com.sun.tools.xjc.XJCListener
cons public init()
intf com.sun.tools.xjc.api.ErrorListener
meth public boolean isCanceled()
meth public void compiled(com.sun.tools.xjc.outline.Outline)
meth public void generatedFile(java.lang.String)
meth public void generatedFile(java.lang.String,int,int)
meth public void message(java.lang.String)
supr java.lang.Object

CLSS public com.sun.tools.xjc.XJCTask
hfds source

CLSS public com.sun.tools.xjc.addon.at_generated.PluginImpl
cons public init()
meth public boolean run(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.Options,org.xml.sax.ErrorHandler)
meth public int parseArgument(com.sun.tools.xjc.Options,java.lang.String[],int) throws com.sun.tools.xjc.BadCommandLineException,java.io.IOException
meth public java.lang.String getOptionName()
meth public java.lang.String getUsage()
supr com.sun.tools.xjc.Plugin
hfds annotation,date,genAnnotation,noDate

CLSS public com.sun.tools.xjc.addon.code_injector.Const
cons public init()
fld public final static java.lang.String NS = "http://jaxb.dev.java.net/plugin/code-injector"
supr java.lang.Object

CLSS public com.sun.tools.xjc.addon.code_injector.PluginImpl
cons public init()
meth public boolean isCustomizationTagName(java.lang.String,java.lang.String)
meth public boolean run(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.Options,org.xml.sax.ErrorHandler)
 anno 1 com.sun.istack.NotNull()
meth public java.lang.String getOptionName()
meth public java.lang.String getUsage()
meth public java.util.List<java.lang.String> getCustomizationURIs()
supr com.sun.tools.xjc.Plugin

CLSS public com.sun.tools.xjc.addon.locator.SourceLocationAddOn
cons public init()
meth public boolean run(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.Options,org.xml.sax.ErrorHandler)
meth public int parseArgument(com.sun.tools.xjc.Options,java.lang.String[],int) throws com.sun.tools.xjc.BadCommandLineException,java.io.IOException
meth public java.lang.String getOptionName()
meth public java.lang.String getUsage()
supr com.sun.tools.xjc.Plugin
hfds fieldName

CLSS public com.sun.tools.xjc.addon.sync.SynchronizedMethodAddOn
cons public init()
meth public boolean run(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.Options,org.xml.sax.ErrorHandler)
meth public int parseArgument(com.sun.tools.xjc.Options,java.lang.String[],int) throws com.sun.tools.xjc.BadCommandLineException,java.io.IOException
meth public java.lang.String getOptionName()
meth public java.lang.String getUsage()
supr com.sun.tools.xjc.Plugin

CLSS public abstract interface com.sun.tools.xjc.api.ClassNameAllocator
meth public abstract java.lang.String assignClassName(java.lang.String,java.lang.String)

CLSS public abstract interface com.sun.tools.xjc.api.ErrorListener
intf com.sun.xml.bind.api.ErrorListener
meth public abstract void error(org.xml.sax.SAXParseException)
meth public abstract void fatalError(org.xml.sax.SAXParseException)
meth public abstract void info(org.xml.sax.SAXParseException)
meth public abstract void warning(org.xml.sax.SAXParseException)

CLSS public abstract interface com.sun.tools.xjc.api.J2SJAXBModel
intf com.sun.tools.xjc.api.JAXBModel
meth public abstract javax.xml.namespace.QName getXmlTypeName(com.sun.tools.xjc.api.Reference)
meth public abstract void generateEpisodeFile(javax.xml.transform.Result)
meth public abstract void generateSchema(javax.xml.bind.SchemaOutputResolver,com.sun.tools.xjc.api.ErrorListener) throws java.io.IOException

CLSS public abstract interface com.sun.tools.xjc.api.JAXBModel
meth public abstract java.util.List<java.lang.String> getClassList()

CLSS public abstract interface com.sun.tools.xjc.api.JavaCompiler
meth public abstract com.sun.tools.xjc.api.J2SJAXBModel bind(java.util.Collection<com.sun.tools.xjc.api.Reference>,java.util.Map<javax.xml.namespace.QName,com.sun.tools.xjc.api.Reference>,java.lang.String,javax.annotation.processing.ProcessingEnvironment)

CLSS public abstract interface com.sun.tools.xjc.api.Mapping
meth public abstract com.sun.tools.xjc.api.TypeAndAnnotation getType()
meth public abstract java.util.List<? extends com.sun.tools.xjc.api.Property> getWrapperStyleDrilldown()
meth public abstract javax.xml.namespace.QName getElement()

CLSS public abstract interface com.sun.tools.xjc.api.Property
meth public abstract com.sun.codemodel.JType type()
meth public abstract java.lang.String name()
meth public abstract javax.xml.namespace.QName elementName()
meth public abstract javax.xml.namespace.QName rawName()

CLSS public final com.sun.tools.xjc.api.Reference
cons public init(javax.lang.model.element.ExecutableElement)
cons public init(javax.lang.model.element.TypeElement,javax.annotation.processing.ProcessingEnvironment)
cons public init(javax.lang.model.element.VariableElement)
cons public init(javax.lang.model.type.TypeMirror,javax.lang.model.element.Element)
fld public final javax.lang.model.element.Element annotations
fld public final javax.lang.model.type.TypeMirror type
meth public boolean equals(java.lang.Object)
meth public int hashCode()
supr java.lang.Object

CLSS public abstract interface com.sun.tools.xjc.api.S2JJAXBModel
intf com.sun.tools.xjc.api.JAXBModel
meth public abstract com.sun.codemodel.JCodeModel generateCode(com.sun.tools.xjc.Plugin[],com.sun.tools.xjc.api.ErrorListener)
meth public abstract com.sun.tools.xjc.api.Mapping get(javax.xml.namespace.QName)
meth public abstract com.sun.tools.xjc.api.TypeAndAnnotation getJavaType(javax.xml.namespace.QName)
meth public abstract java.util.Collection<? extends com.sun.tools.xjc.api.Mapping> getMappings()
meth public abstract java.util.List<com.sun.codemodel.JClass> getAllObjectFactories()

CLSS public abstract interface com.sun.tools.xjc.api.SchemaCompiler
meth public abstract com.sun.tools.xjc.Options getOptions()
 anno 0 com.sun.istack.NotNull()
meth public abstract com.sun.tools.xjc.api.S2JJAXBModel bind()
meth public abstract org.xml.sax.ContentHandler getParserHandler(java.lang.String)
meth public abstract void forcePackageName(java.lang.String)
meth public abstract void parseSchema(java.lang.String,javax.xml.stream.XMLStreamReader) throws javax.xml.stream.XMLStreamException
meth public abstract void parseSchema(java.lang.String,org.w3c.dom.Element)
meth public abstract void parseSchema(org.xml.sax.InputSource)
meth public abstract void resetSchema()
meth public abstract void setClassNameAllocator(com.sun.tools.xjc.api.ClassNameAllocator)
meth public abstract void setDefaultPackageName(java.lang.String)
meth public abstract void setEntityResolver(org.xml.sax.EntityResolver)
meth public abstract void setErrorListener(com.sun.tools.xjc.api.ErrorListener)
meth public abstract void setTargetVersion(com.sun.tools.xjc.api.SpecVersion)

CLSS public final !enum com.sun.tools.xjc.api.SpecVersion
fld public final static com.sun.tools.xjc.api.SpecVersion LATEST
fld public final static com.sun.tools.xjc.api.SpecVersion V2_0
fld public final static com.sun.tools.xjc.api.SpecVersion V2_1
fld public final static com.sun.tools.xjc.api.SpecVersion V2_2
meth public boolean isLaterThan(com.sun.tools.xjc.api.SpecVersion)
meth public java.lang.String getVersion()
meth public static com.sun.tools.xjc.api.SpecVersion parse(java.lang.String)
meth public static com.sun.tools.xjc.api.SpecVersion valueOf(java.lang.String)
meth public static com.sun.tools.xjc.api.SpecVersion[] values()
supr java.lang.Enum<com.sun.tools.xjc.api.SpecVersion>

CLSS public abstract interface com.sun.tools.xjc.api.TypeAndAnnotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract com.sun.codemodel.JType getTypeClass()
meth public abstract void annotate(com.sun.codemodel.JAnnotatable)

CLSS public final com.sun.tools.xjc.api.XJC
cons public init()
meth public static com.sun.tools.xjc.api.SchemaCompiler createSchemaCompiler()
meth public static java.lang.String getDefaultPackageName(java.lang.String)
supr java.lang.Object

CLSS public final com.sun.tools.xjc.api.impl.s2j.PropertyImpl
fld protected final com.sun.codemodel.JCodeModel codeModel
fld protected final com.sun.tools.xjc.api.Mapping parent
fld protected final com.sun.tools.xjc.outline.FieldOutline fr
fld protected final javax.xml.namespace.QName elementName
intf com.sun.tools.xjc.api.Property
meth public final com.sun.codemodel.JType type()
meth public final java.lang.String name()
meth public final javax.xml.namespace.QName elementName()
meth public final javax.xml.namespace.QName rawName()
supr java.lang.Object

CLSS public final com.sun.tools.xjc.api.impl.s2j.SchemaCompilerImpl
cons public init()
fld protected com.sun.tools.xjc.reader.internalizer.DOMForest forest
 anno 0 com.sun.istack.NotNull()
fld protected final com.sun.tools.xjc.Options opts
intf com.sun.tools.xjc.api.SchemaCompiler
meth public com.sun.tools.xjc.Options getOptions()
 anno 0 com.sun.istack.NotNull()
meth public com.sun.tools.xjc.api.S2JJAXBModel bind()
meth public org.xml.sax.ContentHandler getParserHandler(java.lang.String)
meth public void error(org.xml.sax.SAXParseException)
meth public void fatalError(org.xml.sax.SAXParseException)
meth public void forcePackageName(java.lang.String)
meth public void info(org.xml.sax.SAXParseException)
meth public void parseSchema(java.lang.String,javax.xml.stream.XMLStreamReader) throws javax.xml.stream.XMLStreamException
meth public void parseSchema(java.lang.String,org.w3c.dom.Element)
meth public void parseSchema(org.xml.sax.InputSource)
meth public void resetSchema()
meth public void setClassNameAllocator(com.sun.tools.xjc.api.ClassNameAllocator)
meth public void setDefaultPackageName(java.lang.String)
meth public void setEntityResolver(org.xml.sax.EntityResolver)
meth public void setErrorListener(com.sun.tools.xjc.api.ErrorListener)
meth public void setTargetVersion(com.sun.tools.xjc.api.SpecVersion)
meth public void warning(org.xml.sax.SAXParseException)
supr com.sun.tools.xjc.ErrorReceiver
hfds NO_CORRECTNESS_CHECK,errorListener,hadError

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlAccessorOrderWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlAccessorOrder>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlAccessorOrderWriter value(javax.xml.bind.annotation.XmlAccessOrder)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlAccessorTypeWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlAccessorType>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlAccessorTypeWriter value(javax.xml.bind.annotation.XmlAccessType)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlAnyAttributeWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlAnyAttribute>

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlAnyElementWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlAnyElement>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlAnyElementWriter lax(boolean)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlAnyElementWriter value(com.sun.codemodel.JType)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlAnyElementWriter value(java.lang.Class)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlAttachmentRefWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlAttachmentRef>

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlAttributeWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlAttribute>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlAttributeWriter name(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlAttributeWriter namespace(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlAttributeWriter required(boolean)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlElementDeclWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlElementDecl>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementDeclWriter defaultValue(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementDeclWriter name(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementDeclWriter namespace(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementDeclWriter scope(com.sun.codemodel.JType)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementDeclWriter scope(java.lang.Class)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementDeclWriter substitutionHeadName(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementDeclWriter substitutionHeadNamespace(java.lang.String)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlElementRefWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlElementRef>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementRefWriter name(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementRefWriter namespace(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementRefWriter required(boolean)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementRefWriter type(com.sun.codemodel.JType)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementRefWriter type(java.lang.Class)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlElementRefsWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlElementRefs>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementRefWriter value()

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlElementWrapperWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlElementWrapper>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementWrapperWriter name(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementWrapperWriter namespace(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementWrapperWriter nillable(boolean)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementWrapperWriter required(boolean)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlElementWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlElement>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementWriter defaultValue(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementWriter name(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementWriter namespace(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementWriter nillable(boolean)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementWriter required(boolean)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementWriter type(com.sun.codemodel.JType)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementWriter type(java.lang.Class)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlElementsWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlElements>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlElementWriter value()

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlEnumValueWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlEnumValue>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlEnumValueWriter value(java.lang.String)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlEnumWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlEnum>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlEnumWriter value(com.sun.codemodel.JType)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlEnumWriter value(java.lang.Class)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlIDREFWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlIDREF>

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlIDWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlID>

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlInlineBinaryDataWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlInlineBinaryData>

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlJavaTypeAdapterWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlJavaTypeAdapterWriter type(com.sun.codemodel.JType)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlJavaTypeAdapterWriter type(java.lang.Class)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlJavaTypeAdapterWriter value(com.sun.codemodel.JType)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlJavaTypeAdapterWriter value(java.lang.Class)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlListWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlList>

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlMimeTypeWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlMimeType>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlMimeTypeWriter value(java.lang.String)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlMixedWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlMixed>

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlNsWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlNs>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlNsWriter namespaceURI(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlNsWriter prefix(java.lang.String)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlRegistryWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlRegistry>

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlRootElementWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlRootElement>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlRootElementWriter name(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlRootElementWriter namespace(java.lang.String)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlSchemaTypeWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlSchemaType>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlSchemaTypeWriter name(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlSchemaTypeWriter namespace(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlSchemaTypeWriter type(com.sun.codemodel.JType)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlSchemaTypeWriter type(java.lang.Class)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlSchemaTypesWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlSchemaTypes>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlSchemaTypeWriter value()

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlSchemaWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlSchema>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlNsWriter xmlns()
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlSchemaWriter attributeFormDefault(javax.xml.bind.annotation.XmlNsForm)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlSchemaWriter elementFormDefault(javax.xml.bind.annotation.XmlNsForm)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlSchemaWriter location(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlSchemaWriter namespace(java.lang.String)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlSeeAlsoWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlSeeAlso>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlSeeAlsoWriter value(com.sun.codemodel.JType)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlSeeAlsoWriter value(java.lang.Class)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlTransientWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlTransient>

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlTypeWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlType>
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlTypeWriter factoryClass(com.sun.codemodel.JType)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlTypeWriter factoryClass(java.lang.Class)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlTypeWriter factoryMethod(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlTypeWriter name(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlTypeWriter namespace(java.lang.String)
meth public abstract com.sun.tools.xjc.generator.annotation.spec.XmlTypeWriter propOrder(java.lang.String)

CLSS public abstract interface com.sun.tools.xjc.generator.annotation.spec.XmlValueWriter
intf com.sun.codemodel.JAnnotationWriter<javax.xml.bind.annotation.XmlValue>

CLSS public final com.sun.tools.xjc.generator.bean.BeanGenerator
intf com.sun.tools.xjc.outline.Outline
meth public com.sun.codemodel.JClass generateStaticClass(java.lang.Class,com.sun.codemodel.JPackage)
meth public com.sun.codemodel.JClassContainer getContainer(com.sun.tools.xjc.model.CClassInfoParent,com.sun.tools.xjc.outline.Aspect)
meth public com.sun.codemodel.JCodeModel getCodeModel()
meth public com.sun.tools.xjc.ErrorReceiver getErrorReceiver()
meth public com.sun.tools.xjc.generator.bean.ClassOutlineImpl getClazz(com.sun.tools.xjc.model.CClassInfo)
meth public com.sun.tools.xjc.generator.bean.PackageOutlineImpl getPackageContext(com.sun.codemodel.JPackage)
meth public com.sun.tools.xjc.model.Model getModel()
meth public com.sun.tools.xjc.outline.ElementOutline getElement(com.sun.tools.xjc.model.CElementInfo)
meth public com.sun.tools.xjc.outline.EnumOutline getEnum(com.sun.tools.xjc.model.CEnumLeafInfo)
meth public com.sun.tools.xjc.outline.FieldOutline getField(com.sun.tools.xjc.model.CPropertyInfo)
meth public com.sun.tools.xjc.util.CodeModelClassFactory getClassFactory()
meth public final com.sun.codemodel.JClass addRuntime(java.lang.Class)
meth public final com.sun.codemodel.JPackage[] getUsedPackages(com.sun.tools.xjc.outline.Aspect)
meth public final com.sun.codemodel.JType resolve(com.sun.tools.xjc.model.CTypeRef,com.sun.tools.xjc.outline.Aspect)
meth public final void generateAdapterIfNecessary(com.sun.tools.xjc.model.CPropertyInfo,com.sun.codemodel.JAnnotatable)
meth public java.lang.Iterable<? extends com.sun.tools.xjc.outline.PackageOutline> getAllPackageContexts()
meth public java.util.Collection<com.sun.tools.xjc.generator.bean.ClassOutlineImpl> getClasses()
meth public java.util.Collection<com.sun.tools.xjc.outline.EnumOutline> getEnums()
meth public static com.sun.tools.xjc.outline.Outline generate(com.sun.tools.xjc.model.Model,com.sun.tools.xjc.ErrorReceiver)
supr java.lang.Object
hfds JAXB_PACKAGE,classes,codeModel,codeModelClassFactory,elements,enums,errorReceiver,exposedContainerBuilder,fields,generatedRuntime,implContainerBuilder,model,packageContexts

CLSS public final com.sun.tools.xjc.generator.bean.ClassOutlineImpl
meth public com.sun.tools.xjc.generator.bean.BeanGenerator parent()
meth public com.sun.tools.xjc.generator.bean.MethodWriter createMethodWriter()
meth public com.sun.tools.xjc.generator.bean.PackageOutlineImpl _package()
supr com.sun.tools.xjc.outline.ClassOutline
hfds _parent

CLSS public final com.sun.tools.xjc.generator.bean.DualObjectFactoryGenerator
fld public final com.sun.tools.xjc.generator.bean.ObjectFactoryGenerator privateOFG
fld public final com.sun.tools.xjc.generator.bean.ObjectFactoryGenerator publicOFG
meth public com.sun.codemodel.JDefinedClass getObjectFactory()
supr com.sun.tools.xjc.generator.bean.ObjectFactoryGenerator

CLSS public abstract !enum com.sun.tools.xjc.generator.bean.ImplStructureStrategy
fld public final static com.sun.tools.xjc.generator.bean.ImplStructureStrategy BEAN_ONLY
fld public final static com.sun.tools.xjc.generator.bean.ImplStructureStrategy INTF_AND_IMPL
innr public final static Result
meth protected abstract com.sun.codemodel.JPackage getPackage(com.sun.codemodel.JPackage,com.sun.tools.xjc.outline.Aspect)
meth protected abstract com.sun.tools.xjc.generator.bean.ImplStructureStrategy$Result createClasses(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.model.CClassInfo)
meth protected abstract com.sun.tools.xjc.generator.bean.MethodWriter createMethodWriter(com.sun.tools.xjc.generator.bean.ClassOutlineImpl)
meth protected abstract void _extends(com.sun.tools.xjc.generator.bean.ClassOutlineImpl,com.sun.tools.xjc.generator.bean.ClassOutlineImpl)
meth public static com.sun.tools.xjc.generator.bean.ImplStructureStrategy valueOf(java.lang.String)
meth public static com.sun.tools.xjc.generator.bean.ImplStructureStrategy[] values()
supr java.lang.Enum<com.sun.tools.xjc.generator.bean.ImplStructureStrategy>

CLSS public final static com.sun.tools.xjc.generator.bean.ImplStructureStrategy$Result
 outer com.sun.tools.xjc.generator.bean.ImplStructureStrategy
cons public init(com.sun.codemodel.JDefinedClass,com.sun.codemodel.JDefinedClass)
fld public final com.sun.codemodel.JDefinedClass exposed
fld public final com.sun.codemodel.JDefinedClass implementation
supr java.lang.Object

CLSS public abstract com.sun.tools.xjc.generator.bean.MethodWriter
cons protected init(com.sun.tools.xjc.outline.ClassOutline)
fld protected final com.sun.codemodel.JCodeModel codeModel
meth public abstract com.sun.codemodel.JDocComment javadoc()
meth public abstract com.sun.codemodel.JMethod declareMethod(com.sun.codemodel.JType,java.lang.String)
meth public abstract com.sun.codemodel.JVar addParameter(com.sun.codemodel.JType,java.lang.String)
meth public final com.sun.codemodel.JMethod declareMethod(java.lang.Class,java.lang.String)
meth public final com.sun.codemodel.JVar addParameter(java.lang.Class,java.lang.String)
supr java.lang.Object

CLSS public abstract com.sun.tools.xjc.generator.bean.ObjectFactoryGenerator
cons public init()
meth public abstract com.sun.codemodel.JDefinedClass getObjectFactory()
supr java.lang.Object

CLSS public final com.sun.tools.xjc.generator.bean.PackageOutlineImpl
cons protected init(com.sun.tools.xjc.generator.bean.BeanGenerator,com.sun.tools.xjc.model.Model,com.sun.codemodel.JPackage)
intf com.sun.tools.xjc.outline.PackageOutline
meth public com.sun.codemodel.JDefinedClass objectFactory()
meth public com.sun.codemodel.JPackage _package()
meth public com.sun.tools.xjc.generator.bean.ObjectFactoryGenerator objectFactoryGenerator()
meth public java.lang.String getMostUsedNamespaceURI()
meth public java.util.Set<com.sun.tools.xjc.generator.bean.ClassOutlineImpl> getClasses()
meth public javax.xml.bind.annotation.XmlNsForm getAttributeFormDefault()
meth public javax.xml.bind.annotation.XmlNsForm getElementFormDefault()
meth public void calcDefaultValues()
supr java.lang.Object
hfds _model,_package,attributeFormDefault,classes,classesView,elementFormDefault,mostUsedNamespaceURI,objectFactoryGenerator,propUriCountMap,uriCountMap

CLSS public com.sun.tools.xjc.generator.bean.field.ContentListField
cons protected init(com.sun.tools.xjc.generator.bean.ClassOutlineImpl,com.sun.tools.xjc.model.CPropertyInfo,com.sun.codemodel.JClass)
fld protected com.sun.codemodel.JFieldVar field
fld protected final com.sun.codemodel.JClass listT
fld protected final com.sun.codemodel.JCodeModel codeModel
fld protected final com.sun.codemodel.JPrimitiveType primitiveType
fld protected final com.sun.codemodel.JType exposedType
fld protected final com.sun.codemodel.JType implType
fld protected final com.sun.tools.xjc.generator.bean.ClassOutlineImpl outline
fld protected final com.sun.tools.xjc.model.CPropertyInfo prop
meth protected com.sun.codemodel.JType getType(com.sun.tools.xjc.outline.Aspect)
meth protected final com.sun.codemodel.JClass getCoreListType()
meth protected final com.sun.codemodel.JExpression castToImplType(com.sun.codemodel.JExpression)
meth protected final com.sun.codemodel.JFieldVar generateField(com.sun.codemodel.JType)
meth protected final com.sun.tools.xjc.Options getOptions()
meth protected final java.util.List<java.lang.Object> listPossibleTypes(com.sun.tools.xjc.model.CPropertyInfo)
meth protected final void fixNullRef(com.sun.codemodel.JBlock)
meth protected final void generate()
meth protected void annotate(com.sun.codemodel.JAnnotatable)
meth public com.sun.codemodel.JType getRawType()
meth public com.sun.tools.xjc.outline.FieldAccessor create(com.sun.codemodel.JExpression)
meth public final com.sun.tools.xjc.model.CPropertyInfo getPropertyInfo()
meth public final com.sun.tools.xjc.outline.ClassOutline parent()
meth public void generateAccessors()
supr java.lang.Object
hfds $get,coreList
hcls Accessor

CLSS public com.sun.tools.xjc.generator.bean.field.DummyListField
cons protected init(com.sun.tools.xjc.generator.bean.ClassOutlineImpl,com.sun.tools.xjc.model.CPropertyInfo,com.sun.codemodel.JClass)
fld protected com.sun.codemodel.JFieldVar field
fld protected final com.sun.codemodel.JClass listT
fld protected final com.sun.codemodel.JCodeModel codeModel
fld protected final com.sun.codemodel.JPrimitiveType primitiveType
fld protected final com.sun.codemodel.JType exposedType
fld protected final com.sun.codemodel.JType implType
fld protected final com.sun.tools.xjc.generator.bean.ClassOutlineImpl outline
fld protected final com.sun.tools.xjc.model.CPropertyInfo prop
meth protected com.sun.codemodel.JType getType(com.sun.tools.xjc.outline.Aspect)
meth protected final com.sun.codemodel.JClass getCoreListType()
meth protected final com.sun.codemodel.JExpression castToImplType(com.sun.codemodel.JExpression)
meth protected final com.sun.codemodel.JFieldVar generateField(com.sun.codemodel.JType)
meth protected final com.sun.tools.xjc.Options getOptions()
meth protected final java.util.List<java.lang.Object> listPossibleTypes(com.sun.tools.xjc.model.CPropertyInfo)
meth protected final void fixNullRef(com.sun.codemodel.JBlock)
meth protected final void generate()
meth protected void annotate(com.sun.codemodel.JAnnotatable)
meth public com.sun.codemodel.JType getRawType()
meth public com.sun.tools.xjc.outline.FieldAccessor create(com.sun.codemodel.JExpression)
meth public final com.sun.tools.xjc.model.CPropertyInfo getPropertyInfo()
meth public final com.sun.tools.xjc.outline.ClassOutline parent()
meth public void generateAccessors()
supr java.lang.Object
hfds $get,coreList
hcls Accessor

CLSS public abstract interface com.sun.tools.xjc.generator.bean.field.FieldRenderer
meth public abstract com.sun.tools.xjc.outline.FieldOutline generate(com.sun.tools.xjc.generator.bean.ClassOutlineImpl,com.sun.tools.xjc.model.CPropertyInfo)

CLSS public com.sun.tools.xjc.generator.bean.field.FieldRendererFactory
cons public init()
meth public com.sun.tools.xjc.generator.bean.field.FieldRenderer getArray()
meth public com.sun.tools.xjc.generator.bean.field.FieldRenderer getConst(com.sun.tools.xjc.generator.bean.field.FieldRenderer)
meth public com.sun.tools.xjc.generator.bean.field.FieldRenderer getContentList(com.sun.codemodel.JClass)
meth public com.sun.tools.xjc.generator.bean.field.FieldRenderer getDefault()
meth public com.sun.tools.xjc.generator.bean.field.FieldRenderer getDummyList(com.sun.codemodel.JClass)
meth public com.sun.tools.xjc.generator.bean.field.FieldRenderer getList(com.sun.codemodel.JClass)
meth public com.sun.tools.xjc.generator.bean.field.FieldRenderer getRequiredUnboxed()
meth public com.sun.tools.xjc.generator.bean.field.FieldRenderer getSingle()
meth public com.sun.tools.xjc.generator.bean.field.FieldRenderer getSinglePrimitiveAccess()
supr java.lang.Object
hfds ARRAY,DEFAULT,REQUIRED_UNBOXED,SINGLE,SINGLE_PRIMITIVE_ACCESS

CLSS public final com.sun.tools.xjc.generator.bean.field.GenericFieldRenderer
cons public init(java.lang.Class)
intf com.sun.tools.xjc.generator.bean.field.FieldRenderer
meth public com.sun.tools.xjc.outline.FieldOutline generate(com.sun.tools.xjc.generator.bean.ClassOutlineImpl,com.sun.tools.xjc.model.CPropertyInfo)
supr java.lang.Object
hfds constructor

CLSS public com.sun.tools.xjc.generator.bean.field.IsSetField
cons protected init(com.sun.tools.xjc.generator.bean.ClassOutlineImpl,com.sun.tools.xjc.model.CPropertyInfo,com.sun.tools.xjc.outline.FieldOutline,boolean,boolean)
fld protected final com.sun.codemodel.JCodeModel codeModel
fld protected final com.sun.codemodel.JType exposedType
fld protected final com.sun.codemodel.JType implType
fld protected final com.sun.tools.xjc.generator.bean.ClassOutlineImpl outline
fld protected final com.sun.tools.xjc.model.CPropertyInfo prop
intf com.sun.tools.xjc.outline.FieldOutline
meth protected com.sun.codemodel.JType getType(com.sun.tools.xjc.outline.Aspect)
meth protected final com.sun.codemodel.JExpression castToImplType(com.sun.codemodel.JExpression)
meth protected final com.sun.codemodel.JFieldVar generateField(com.sun.codemodel.JType)
meth protected final com.sun.tools.xjc.Options getOptions()
meth protected final java.util.List<java.lang.Object> listPossibleTypes(com.sun.tools.xjc.model.CPropertyInfo)
meth protected void annotate(com.sun.codemodel.JAnnotatable)
meth public com.sun.codemodel.JType getRawType()
meth public com.sun.tools.xjc.outline.FieldAccessor create(com.sun.codemodel.JExpression)
meth public final com.sun.tools.xjc.model.CPropertyInfo getPropertyInfo()
meth public final com.sun.tools.xjc.outline.ClassOutline parent()
supr java.lang.Object
hfds core,generateIsSetMethod,generateUnSetMethod
hcls Accessor

CLSS public com.sun.tools.xjc.generator.bean.field.IsSetFieldRenderer
cons public init(com.sun.tools.xjc.generator.bean.field.FieldRenderer,boolean,boolean)
intf com.sun.tools.xjc.generator.bean.field.FieldRenderer
meth public com.sun.tools.xjc.outline.FieldOutline generate(com.sun.tools.xjc.generator.bean.ClassOutlineImpl,com.sun.tools.xjc.model.CPropertyInfo)
supr java.lang.Object
hfds core,generateIsSetMethod,generateUnSetMethod

CLSS public final !enum com.sun.tools.xjc.generator.bean.field.Messages
fld public final static com.sun.tools.xjc.generator.bean.field.Messages DEFAULT_GETTER_JAVADOC
fld public final static com.sun.tools.xjc.generator.bean.field.Messages DEFAULT_SETTER_JAVADOC
meth public !varargs java.lang.String format(java.lang.Object[])
meth public java.lang.String toString()
meth public static com.sun.tools.xjc.generator.bean.field.Messages valueOf(java.lang.String)
meth public static com.sun.tools.xjc.generator.bean.field.Messages[] values()
supr java.lang.Enum<com.sun.tools.xjc.generator.bean.field.Messages>
hfds rb

CLSS public com.sun.tools.xjc.generator.bean.field.NoExtendedContentField
cons protected init(com.sun.tools.xjc.generator.bean.ClassOutlineImpl,com.sun.tools.xjc.model.CPropertyInfo,com.sun.codemodel.JClass)
fld protected com.sun.codemodel.JFieldVar field
fld protected final com.sun.codemodel.JClass listT
fld protected final com.sun.codemodel.JCodeModel codeModel
fld protected final com.sun.codemodel.JPrimitiveType primitiveType
fld protected final com.sun.codemodel.JType exposedType
fld protected final com.sun.codemodel.JType implType
fld protected final com.sun.tools.xjc.generator.bean.ClassOutlineImpl outline
fld protected final com.sun.tools.xjc.model.CPropertyInfo prop
meth protected com.sun.codemodel.JType getType(com.sun.tools.xjc.outline.Aspect)
meth protected final com.sun.codemodel.JClass getCoreListType()
meth protected final com.sun.codemodel.JExpression castToImplType(com.sun.codemodel.JExpression)
meth protected final com.sun.codemodel.JFieldVar generateField(com.sun.codemodel.JType)
meth protected final com.sun.tools.xjc.Options getOptions()
meth protected final java.util.List<java.lang.Object> listPossibleTypes(com.sun.tools.xjc.model.CPropertyInfo)
meth protected final void fixNullRef(com.sun.codemodel.JBlock)
meth protected final void generate()
meth protected void annotate(com.sun.codemodel.JAnnotatable)
meth public com.sun.codemodel.JType getRawType()
meth public com.sun.tools.xjc.outline.FieldAccessor create(com.sun.codemodel.JExpression)
meth public final com.sun.tools.xjc.model.CPropertyInfo getPropertyInfo()
meth public final com.sun.tools.xjc.outline.ClassOutline parent()
meth public void generateAccessors()
supr java.lang.Object
hfds $get,coreList
hcls Accessor

CLSS public com.sun.tools.xjc.generator.bean.field.SingleField
cons protected init(com.sun.tools.xjc.generator.bean.ClassOutlineImpl,com.sun.tools.xjc.model.CPropertyInfo)
cons protected init(com.sun.tools.xjc.generator.bean.ClassOutlineImpl,com.sun.tools.xjc.model.CPropertyInfo,boolean)
fld protected final com.sun.codemodel.JCodeModel codeModel
fld protected final com.sun.codemodel.JType exposedType
fld protected final com.sun.codemodel.JType implType
fld protected final com.sun.tools.xjc.generator.bean.ClassOutlineImpl outline
fld protected final com.sun.tools.xjc.model.CPropertyInfo prop
innr protected Accessor
meth protected com.sun.codemodel.JFieldVar ref()
meth protected com.sun.codemodel.JType getType(com.sun.tools.xjc.outline.Aspect)
meth protected final com.sun.codemodel.JExpression castToImplType(com.sun.codemodel.JExpression)
meth protected final com.sun.codemodel.JFieldVar generateField(com.sun.codemodel.JType)
meth protected final com.sun.tools.xjc.Options getOptions()
meth protected final java.util.List<java.lang.Object> listPossibleTypes(com.sun.tools.xjc.model.CPropertyInfo)
meth protected final void createField()
meth protected java.lang.String getGetterMethod()
meth protected void annotate(com.sun.codemodel.JAnnotatable)
meth public com.sun.tools.xjc.outline.FieldAccessor create(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JType getFieldType()
meth public final com.sun.codemodel.JType getRawType()
meth public final com.sun.tools.xjc.model.CPropertyInfo getPropertyInfo()
meth public final com.sun.tools.xjc.outline.ClassOutline parent()
supr java.lang.Object

CLSS protected com.sun.tools.xjc.generator.bean.field.SingleField$Accessor
 outer com.sun.tools.xjc.generator.bean.field.SingleField
cons protected init(com.sun.tools.xjc.generator.bean.field.SingleField,com.sun.codemodel.JExpression)
fld protected final com.sun.codemodel.JExpression $target
fld protected final com.sun.codemodel.JFieldRef $ref
meth public com.sun.codemodel.JExpression hasSetValue()
meth public final com.sun.tools.xjc.model.CPropertyInfo getPropertyInfo()
meth public final com.sun.tools.xjc.outline.FieldOutline owner()
meth public final void fromRawValue(com.sun.codemodel.JBlock,java.lang.String,com.sun.codemodel.JExpression)
meth public final void toRawValue(com.sun.codemodel.JBlock,com.sun.codemodel.JVar)
meth public void unsetValues(com.sun.codemodel.JBlock)
supr java.lang.Object

CLSS public com.sun.tools.xjc.generator.bean.field.SinglePrimitiveAccessField
cons protected init(com.sun.tools.xjc.generator.bean.ClassOutlineImpl,com.sun.tools.xjc.model.CPropertyInfo)
supr com.sun.tools.xjc.generator.bean.field.SingleField

CLSS public com.sun.tools.xjc.generator.bean.field.UnboxedField
cons protected init(com.sun.tools.xjc.generator.bean.ClassOutlineImpl,com.sun.tools.xjc.model.CPropertyInfo)
fld protected final com.sun.codemodel.JCodeModel codeModel
fld protected final com.sun.codemodel.JType exposedType
fld protected final com.sun.codemodel.JType implType
fld protected final com.sun.tools.xjc.generator.bean.ClassOutlineImpl outline
fld protected final com.sun.tools.xjc.model.CPropertyInfo prop
innr protected abstract Accessor
meth protected com.sun.codemodel.JFieldVar ref()
meth protected com.sun.codemodel.JType getFieldType()
meth protected com.sun.codemodel.JType getType(com.sun.tools.xjc.outline.Aspect)
meth protected final com.sun.codemodel.JExpression castToImplType(com.sun.codemodel.JExpression)
meth protected final com.sun.codemodel.JFieldVar generateField(com.sun.codemodel.JType)
meth protected final com.sun.tools.xjc.Options getOptions()
meth protected final java.util.List<java.lang.Object> listPossibleTypes(com.sun.tools.xjc.model.CPropertyInfo)
meth protected final void createField()
meth protected java.lang.String getGetterMethod()
meth protected void annotate(com.sun.codemodel.JAnnotatable)
meth public com.sun.tools.xjc.outline.FieldAccessor create(com.sun.codemodel.JExpression)
meth public final com.sun.codemodel.JType getRawType()
meth public final com.sun.tools.xjc.model.CPropertyInfo getPropertyInfo()
meth public final com.sun.tools.xjc.outline.ClassOutline parent()
supr java.lang.Object
hfds ptype

CLSS public com.sun.tools.xjc.generator.bean.field.UntypedListField
cons protected init(com.sun.tools.xjc.generator.bean.ClassOutlineImpl,com.sun.tools.xjc.model.CPropertyInfo,com.sun.codemodel.JClass)
fld protected com.sun.codemodel.JFieldVar field
fld protected final com.sun.codemodel.JClass listT
fld protected final com.sun.codemodel.JCodeModel codeModel
fld protected final com.sun.codemodel.JPrimitiveType primitiveType
fld protected final com.sun.codemodel.JType exposedType
fld protected final com.sun.codemodel.JType implType
fld protected final com.sun.tools.xjc.generator.bean.ClassOutlineImpl outline
fld protected final com.sun.tools.xjc.model.CPropertyInfo prop
meth protected com.sun.codemodel.JType getType(com.sun.tools.xjc.outline.Aspect)
meth protected final com.sun.codemodel.JClass getCoreListType()
meth protected final com.sun.codemodel.JExpression castToImplType(com.sun.codemodel.JExpression)
meth protected final com.sun.codemodel.JFieldVar generateField(com.sun.codemodel.JType)
meth protected final com.sun.tools.xjc.Options getOptions()
meth protected final java.util.List<java.lang.Object> listPossibleTypes(com.sun.tools.xjc.model.CPropertyInfo)
meth protected final void fixNullRef(com.sun.codemodel.JBlock)
meth protected final void generate()
meth protected void annotate(com.sun.codemodel.JAnnotatable)
meth public com.sun.codemodel.JType getRawType()
meth public com.sun.tools.xjc.outline.FieldAccessor create(com.sun.codemodel.JExpression)
meth public final com.sun.tools.xjc.model.CPropertyInfo getPropertyInfo()
meth public final com.sun.tools.xjc.outline.ClassOutline parent()
meth public void generateAccessors()
supr java.lang.Object
hfds $get,coreList
hcls Accessor

CLSS public final com.sun.tools.xjc.generator.bean.field.UntypedListFieldRenderer
cons protected init(com.sun.codemodel.JClass)
cons protected init(com.sun.codemodel.JClass,boolean,boolean)
intf com.sun.tools.xjc.generator.bean.field.FieldRenderer
meth public com.sun.tools.xjc.outline.FieldOutline generate(com.sun.tools.xjc.generator.bean.ClassOutlineImpl,com.sun.tools.xjc.model.CPropertyInfo)
supr java.lang.Object
hfds content,coreList,dummy

CLSS public abstract interface com.sun.tools.xjc.generator.util.BlockReference
meth public abstract com.sun.codemodel.JBlock get(boolean)

CLSS public com.sun.tools.xjc.generator.util.ExistingBlockReference
cons public init(com.sun.codemodel.JBlock)
intf com.sun.tools.xjc.generator.util.BlockReference
meth public com.sun.codemodel.JBlock get(boolean)
supr java.lang.Object
hfds block

CLSS public abstract com.sun.tools.xjc.generator.util.LazyBlockReference
cons public init()
intf com.sun.tools.xjc.generator.util.BlockReference
meth protected abstract com.sun.codemodel.JBlock create()
meth public com.sun.codemodel.JBlock get(boolean)
supr java.lang.Object
hfds block

CLSS public abstract com.sun.tools.xjc.generator.util.WhitespaceNormalizer
cons public init()
fld public final static com.sun.tools.xjc.generator.util.WhitespaceNormalizer COLLAPSE
fld public final static com.sun.tools.xjc.generator.util.WhitespaceNormalizer PRESERVE
fld public final static com.sun.tools.xjc.generator.util.WhitespaceNormalizer REPLACE
meth public abstract com.sun.codemodel.JExpression generate(com.sun.codemodel.JCodeModel,com.sun.codemodel.JExpression)
meth public static com.sun.tools.xjc.generator.util.WhitespaceNormalizer parse(java.lang.String)
supr java.lang.Object

CLSS public com.sun.tools.xjc.model.AutoClassNameAllocator
cons public init(com.sun.tools.xjc.api.ClassNameAllocator)
intf com.sun.tools.xjc.api.ClassNameAllocator
meth public java.lang.String assignClassName(java.lang.String,java.lang.String)
supr java.lang.Object
hfds core,names

CLSS public final com.sun.tools.xjc.model.CAdapter
cons public init(com.sun.codemodel.JClass)
cons public init(java.lang.Class<? extends javax.xml.bind.annotation.adapters.XmlAdapter>,boolean)
meth public boolean isWhitespaceAdapter()
meth public com.sun.codemodel.JClass getAdapterClass(com.sun.tools.xjc.outline.Outline)
meth public java.lang.Class<? extends javax.xml.bind.annotation.adapters.XmlAdapter> getAdapterIfKnown()
supr com.sun.xml.bind.v2.model.core.Adapter<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
hfds adapterClass1,adapterClass2

CLSS public final com.sun.tools.xjc.model.CArrayInfo
cons public init(com.sun.tools.xjc.model.Model,com.sun.tools.xjc.model.CNonElement,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.model.CCustomizations)
intf com.sun.tools.xjc.model.CNonElement
intf com.sun.tools.xjc.model.CTypeInfo
intf com.sun.tools.xjc.model.nav.NType
intf com.sun.xml.bind.v2.model.core.ArrayInfo<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
meth public boolean isBoxedType()
meth public boolean isSimpleType()
meth public com.sun.codemodel.JExpression createConstant(com.sun.tools.xjc.outline.Outline,com.sun.xml.xsom.XmlString)
meth public com.sun.codemodel.JType toType(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.outline.Aspect)
meth public com.sun.tools.xjc.model.CCustomizations getCustomizations()
meth public com.sun.tools.xjc.model.CNonElement getInfo()
 anno 0 java.lang.Deprecated()
meth public com.sun.tools.xjc.model.CNonElement getItemType()
meth public com.sun.tools.xjc.model.nav.NType getType()
meth public final boolean canBeReferencedByIDREF()
meth public final boolean isCollection()
meth public final com.sun.tools.xjc.model.CAdapter getAdapterUse()
meth public final com.sun.xml.bind.v2.model.annotation.Locatable getUpstream()
meth public final com.sun.xml.bind.v2.model.core.ID idUse()
meth public final com.sun.xml.bind.v2.runtime.Location getLocation()
meth public final com.sun.xml.xsom.XSComponent getSchemaComponent()
meth public java.lang.String fullName()
meth public javax.activation.MimeType getExpectedMimeType()
meth public javax.xml.namespace.QName getTypeName()
meth public org.xml.sax.Locator getLocator()
supr java.lang.Object
hfds itemType,typeName

CLSS public final com.sun.tools.xjc.model.CAttributePropertyInfo
cons public init(java.lang.String,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.model.CCustomizations,org.xml.sax.Locator,javax.xml.namespace.QName,com.sun.tools.xjc.model.TypeUse,javax.xml.namespace.QName,boolean)
 anno 7 com.sun.istack.Nullable()
fld protected final com.sun.tools.xjc.model.TypeUse type
intf com.sun.xml.bind.v2.model.core.AttributePropertyInfo<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
meth public <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} accept(com.sun.tools.xjc.model.CPropertyVisitor2<{%%0},{%%1}>,{%%1})
meth public <%0 extends java.lang.Object> {%%0} accept(com.sun.tools.xjc.model.CPropertyVisitor<{%%0}>)
meth public boolean isOptionalPrimitive()
meth public boolean isRequired()
meth public boolean isUnboxable()
meth public final com.sun.tools.xjc.model.CAdapter getAdapter()
meth public final com.sun.tools.xjc.model.CNonElement getTarget()
meth public final com.sun.tools.xjc.model.CPropertyInfo getSource()
meth public final com.sun.xml.bind.v2.model.core.ID id()
meth public final com.sun.xml.bind.v2.model.core.PropertyKind kind()
meth public final java.util.List<? extends com.sun.tools.xjc.model.CTypeInfo> ref()
meth public final javax.activation.MimeType getExpectedMimeType()
meth public javax.xml.namespace.QName getSchemaType()
meth public javax.xml.namespace.QName getXmlName()
supr com.sun.tools.xjc.model.CPropertyInfo
hfds attName,isRequired

CLSS public abstract com.sun.tools.xjc.model.CBuiltinLeafInfo
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo ANYTYPE
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo BASE64_BYTE_ARRAY
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo BIG_DECIMAL
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo BIG_INTEGER
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo BOOLEAN
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo BYTE
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo CALENDAR
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo DATA_HANDLER
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo DOUBLE
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo DURATION
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo FLOAT
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo IMAGE
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo INT
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo LONG
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo QNAME
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo SHORT
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo STRING
fld public final static com.sun.tools.xjc.model.CBuiltinLeafInfo XML_SOURCE
fld public final static com.sun.tools.xjc.model.TypeUse BOOLEAN_ZERO_OR_ONE
fld public final static com.sun.tools.xjc.model.TypeUse HEXBIN_BYTE_ARRAY
fld public final static com.sun.tools.xjc.model.TypeUse ID
fld public final static com.sun.tools.xjc.model.TypeUse IDREF
fld public final static com.sun.tools.xjc.model.TypeUse NORMALIZED_STRING
fld public final static com.sun.tools.xjc.model.TypeUse STRING_LIST
fld public final static com.sun.tools.xjc.model.TypeUse TOKEN
fld public final static java.util.Map<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.CBuiltinLeafInfo> LEAVES
intf com.sun.tools.xjc.model.CNonElement
intf com.sun.xml.bind.v2.model.core.BuiltinLeafInfo<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
intf com.sun.xml.bind.v2.model.core.LeafInfo<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
intf com.sun.xml.bind.v2.runtime.Location
meth public boolean isSimpleType()
meth public com.sun.codemodel.JType toType(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.outline.Aspect)
meth public com.sun.tools.xjc.model.CNonElement getInfo()
 anno 0 java.lang.Deprecated()
meth public com.sun.tools.xjc.model.nav.NType getType()
meth public com.sun.xml.bind.v2.model.annotation.Locatable getUpstream()
meth public com.sun.xml.bind.v2.model.core.ID idUse()
meth public com.sun.xml.bind.v2.runtime.Location getLocation()
meth public final boolean canBeReferencedByIDREF()
meth public final boolean isCollection()
 anno 0 java.lang.Deprecated()
meth public final boolean isElement()
meth public final com.sun.tools.xjc.model.CAdapter getAdapterUse()
 anno 0 java.lang.Deprecated()
meth public final com.sun.tools.xjc.model.TypeUse makeAdapted(java.lang.Class<? extends javax.xml.bind.annotation.adapters.XmlAdapter>,boolean)
meth public final com.sun.tools.xjc.model.TypeUse makeCollection()
meth public final com.sun.tools.xjc.model.TypeUse makeMimeTyped(javax.activation.MimeType)
meth public final com.sun.xml.bind.v2.model.core.Element<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass> asElement()
meth public final com.sun.xml.xsom.XSComponent getSchemaComponent()
meth public final javax.xml.namespace.QName getElementName()
meth public final javax.xml.namespace.QName[] getTypeNames()
meth public javax.activation.MimeType getExpectedMimeType()
meth public javax.xml.namespace.QName getTypeName()
meth public org.xml.sax.Locator getLocator()
supr java.lang.Object
hfds id,type,typeName,typeNames
hcls Builtin,NoConstantBuiltin

CLSS public abstract interface com.sun.tools.xjc.model.CClass
intf com.sun.tools.xjc.model.CElement
intf com.sun.tools.xjc.model.CNonElement

CLSS public final com.sun.tools.xjc.model.CClassInfo
cons public init(com.sun.tools.xjc.model.Model,com.sun.codemodel.JCodeModel,java.lang.String,org.xml.sax.Locator,javax.xml.namespace.QName,javax.xml.namespace.QName,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.model.CCustomizations)
cons public init(com.sun.tools.xjc.model.Model,com.sun.codemodel.JPackage,java.lang.String,org.xml.sax.Locator,javax.xml.namespace.QName,javax.xml.namespace.QName,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.model.CCustomizations)
cons public init(com.sun.tools.xjc.model.Model,com.sun.tools.xjc.model.CClassInfoParent,java.lang.String,org.xml.sax.Locator,javax.xml.namespace.QName,javax.xml.namespace.QName,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.model.CCustomizations)
fld public final com.sun.tools.xjc.model.Model model
fld public final java.lang.String shortName
fld public java.lang.String javadoc
intf com.sun.tools.xjc.model.CClass
intf com.sun.tools.xjc.model.CClassInfoParent
intf com.sun.tools.xjc.model.CElement
intf com.sun.tools.xjc.model.nav.NClass
intf com.sun.xml.bind.v2.model.core.ClassInfo<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
meth public !varargs void addConstructor(java.lang.String[])
meth public boolean declaresAttributeWildcard()
meth public boolean hasAttributeWildcard()
meth public boolean hasProperties()
meth public boolean hasSubClasses()
meth public boolean hasValueProperty()
meth public boolean inheritsAttributeWildcard()
meth public boolean isAbstract()
meth public boolean isBoxedType()
meth public boolean isElement()
meth public boolean isFinal()
meth public boolean isOrdered()
meth public boolean isSimpleType()
meth public com.sun.codemodel.JExpression createConstant(com.sun.tools.xjc.outline.Outline,com.sun.xml.xsom.XmlString)
meth public com.sun.codemodel.JPackage getOwnerPackage()
meth public com.sun.tools.xjc.model.CClassInfo getBaseClass()
meth public com.sun.tools.xjc.model.CClassInfo getScope()
meth public com.sun.tools.xjc.model.CClassInfo getSubstitutionHead()
meth public com.sun.tools.xjc.model.CClassInfoParent parent()
meth public com.sun.tools.xjc.model.CClassRef getRefBaseClass()
meth public com.sun.tools.xjc.model.CCustomizations getCustomizations()
meth public com.sun.tools.xjc.model.CNonElement getInfo()
 anno 0 java.lang.Deprecated()
meth public com.sun.tools.xjc.model.CPropertyInfo getProperty(java.lang.String)
meth public com.sun.tools.xjc.model.nav.NClass getClazz()
meth public com.sun.xml.bind.v2.model.core.Element<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass> asElement()
meth public final <%0 extends java.lang.Object> {%%0} accept(com.sun.tools.xjc.model.CClassInfoParent$Visitor<{%%0}>)
meth public final boolean canBeReferencedByIDREF()
meth public final boolean isCollection()
meth public final com.sun.codemodel.JClass toType(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.outline.Aspect)
meth public final com.sun.tools.xjc.model.CAdapter getAdapterUse()
meth public final com.sun.tools.xjc.model.nav.NClass getType()
meth public final com.sun.xml.bind.v2.model.annotation.Locatable getUpstream()
meth public final com.sun.xml.bind.v2.model.core.ID idUse()
meth public final com.sun.xml.bind.v2.runtime.Location getLocation()
meth public final com.sun.xml.xsom.XSComponent getSchemaComponent()
meth public java.lang.String fullName()
meth public java.lang.String getName()
meth public java.lang.String getSqueezedName()
meth public java.lang.String getUserSpecifiedImplClass()
meth public java.lang.String toString()
meth public java.util.Collection<? extends com.sun.tools.xjc.model.Constructor> getConstructors()
meth public java.util.Iterator<com.sun.tools.xjc.model.CClassInfo> listSubclasses()
meth public java.util.List<com.sun.tools.xjc.model.CPropertyInfo> getProperties()
meth public javax.activation.MimeType getExpectedMimeType()
meth public javax.xml.namespace.QName getElementName()
meth public javax.xml.namespace.QName getTypeName()
meth public org.xml.sax.Locator getLocator()
meth public void _implements(com.sun.codemodel.JClass)
meth public void addProperty(com.sun.tools.xjc.model.CPropertyInfo)
meth public void hasAttributeWildcard(boolean)
meth public void setAbstract()
meth public void setBaseClass(com.sun.tools.xjc.model.CClass)
meth public void setOrdered(boolean)
meth public void setUserSpecifiedImplClass(java.lang.String)
supr java.lang.Object
hfds _implements,baseClass,calcSqueezedName,constructors,elementName,firstSubclass,hasAttributeWildcard,implClass,isOrdered,nextSibling,parent,properties,squeezedName,typeName

CLSS public abstract interface com.sun.tools.xjc.model.CClassInfoParent
innr public abstract interface static Visitor
innr public final static Package
meth public abstract <%0 extends java.lang.Object> {%%0} accept(com.sun.tools.xjc.model.CClassInfoParent$Visitor<{%%0}>)
meth public abstract com.sun.codemodel.JPackage getOwnerPackage()
meth public abstract java.lang.String fullName()

CLSS public final static com.sun.tools.xjc.model.CClassInfoParent$Package
 outer com.sun.tools.xjc.model.CClassInfoParent
cons public init(com.sun.codemodel.JPackage)
fld public final com.sun.codemodel.JPackage pkg
intf com.sun.tools.xjc.model.CClassInfoParent
meth public <%0 extends java.lang.Object> {%%0} accept(com.sun.tools.xjc.model.CClassInfoParent$Visitor<{%%0}>)
meth public com.sun.codemodel.JPackage getOwnerPackage()
meth public java.lang.String fullName()
supr java.lang.Object

CLSS public abstract interface static com.sun.tools.xjc.model.CClassInfoParent$Visitor<%0 extends java.lang.Object>
 outer com.sun.tools.xjc.model.CClassInfoParent
meth public abstract {com.sun.tools.xjc.model.CClassInfoParent$Visitor%0} onBean(com.sun.tools.xjc.model.CClassInfo)
meth public abstract {com.sun.tools.xjc.model.CClassInfoParent$Visitor%0} onElement(com.sun.tools.xjc.model.CElementInfo)
meth public abstract {com.sun.tools.xjc.model.CClassInfoParent$Visitor%0} onPackage(com.sun.codemodel.JPackage)

CLSS public final com.sun.tools.xjc.model.CClassRef
cons public init(com.sun.tools.xjc.model.Model,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.reader.xmlschema.bindinfo.BIClass,com.sun.tools.xjc.model.CCustomizations)
cons public init(com.sun.tools.xjc.model.Model,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.reader.xmlschema.bindinfo.BIEnum,com.sun.tools.xjc.model.CCustomizations)
intf com.sun.tools.xjc.model.CClass
intf com.sun.tools.xjc.model.CElement
intf com.sun.tools.xjc.model.nav.NClass
meth public boolean isAbstract()
meth public boolean isBoxedType()
meth public boolean isSimpleType()
meth public com.sun.codemodel.JClass toType(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.outline.Aspect)
meth public com.sun.codemodel.JExpression createConstant(com.sun.tools.xjc.outline.Outline,com.sun.xml.xsom.XmlString)
meth public com.sun.tools.xjc.model.CClassInfo getScope()
meth public com.sun.tools.xjc.model.CCustomizations getCustomizations()
meth public com.sun.tools.xjc.model.CElement getSubstitutionHead()
meth public com.sun.tools.xjc.model.CNonElement getInfo()
 anno 0 java.lang.Deprecated()
meth public com.sun.tools.xjc.model.nav.NType getType()
meth public final boolean canBeReferencedByIDREF()
meth public final boolean isCollection()
meth public final com.sun.tools.xjc.model.CAdapter getAdapterUse()
meth public final com.sun.xml.bind.v2.model.annotation.Locatable getUpstream()
meth public final com.sun.xml.bind.v2.model.core.ID idUse()
meth public final com.sun.xml.bind.v2.runtime.Location getLocation()
meth public final com.sun.xml.xsom.XSComponent getSchemaComponent()
meth public java.lang.String fullName()
meth public javax.activation.MimeType getExpectedMimeType()
meth public javax.xml.namespace.QName getElementName()
meth public javax.xml.namespace.QName getTypeName()
meth public org.xml.sax.Locator getLocator()
meth public void setAbstract()
supr java.lang.Object
hfds clazz,fullyQualifiedClassName

CLSS public abstract interface com.sun.tools.xjc.model.CCustomizable
meth public abstract com.sun.tools.xjc.model.CCustomizations getCustomizations()
meth public abstract com.sun.xml.xsom.XSComponent getSchemaComponent()
meth public abstract org.xml.sax.Locator getLocator()

CLSS public final com.sun.tools.xjc.model.CCustomizations
cons public init()
cons public init(java.util.Collection<? extends com.sun.tools.xjc.model.CPluginCustomization>)
fld public final static com.sun.tools.xjc.model.CCustomizations EMPTY
meth public boolean equals(java.lang.Object)
meth public com.sun.tools.xjc.model.CCustomizable getOwner()
meth public com.sun.tools.xjc.model.CPluginCustomization find(java.lang.String)
meth public com.sun.tools.xjc.model.CPluginCustomization find(java.lang.String,java.lang.String)
meth public int hashCode()
meth public static com.sun.tools.xjc.model.CCustomizations merge(com.sun.tools.xjc.model.CCustomizations,com.sun.tools.xjc.model.CCustomizations)
supr java.util.ArrayList<com.sun.tools.xjc.model.CPluginCustomization>
hfds next,owner

CLSS public abstract com.sun.tools.xjc.model.CDefaultValue
cons public init()
meth public abstract com.sun.codemodel.JExpression compute(com.sun.tools.xjc.outline.Outline)
meth public static com.sun.tools.xjc.model.CDefaultValue create(com.sun.tools.xjc.model.TypeUse,com.sun.xml.xsom.XmlString)
supr java.lang.Object

CLSS public abstract interface com.sun.tools.xjc.model.CElement
intf com.sun.tools.xjc.model.CTypeInfo
intf com.sun.xml.bind.v2.model.core.Element<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
meth public abstract boolean isAbstract()
meth public abstract void setAbstract()

CLSS public final com.sun.tools.xjc.model.CElementInfo
cons public init(com.sun.tools.xjc.model.Model,javax.xml.namespace.QName,com.sun.tools.xjc.model.CClassInfoParent,com.sun.tools.xjc.model.TypeUse,com.sun.xml.xsom.XmlString,com.sun.xml.xsom.XSElementDecl,com.sun.tools.xjc.model.CCustomizations,org.xml.sax.Locator)
cons public init(com.sun.tools.xjc.model.Model,javax.xml.namespace.QName,com.sun.tools.xjc.model.CClassInfoParent,java.lang.String,com.sun.tools.xjc.model.CCustomizations,org.xml.sax.Locator)
fld public final com.sun.tools.xjc.model.CClassInfoParent parent
intf com.sun.tools.xjc.model.CClassInfoParent
intf com.sun.tools.xjc.model.CElement
intf com.sun.tools.xjc.model.nav.NType
intf com.sun.xml.bind.v2.model.core.ElementInfo<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
meth public <%0 extends java.lang.Object> {%%0} accept(com.sun.tools.xjc.model.CClassInfoParent$Visitor<{%%0}>)
meth public boolean hasClass()
meth public boolean isAbstract()
meth public boolean isBoxedType()
meth public com.sun.codemodel.JExpression createConstant(com.sun.tools.xjc.outline.Outline,com.sun.xml.xsom.XmlString)
meth public com.sun.codemodel.JPackage getOwnerPackage()
meth public com.sun.codemodel.JType toType(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.outline.Aspect)
meth public com.sun.tools.xjc.model.CClassInfo getScope()
meth public com.sun.tools.xjc.model.CCustomizations getCustomizations()
meth public com.sun.tools.xjc.model.CElementInfo getSubstitutionHead()
meth public com.sun.tools.xjc.model.CElementPropertyInfo getProperty()
meth public com.sun.tools.xjc.model.CNonElement getContentType()
meth public com.sun.tools.xjc.model.nav.NType getContentInMemoryType()
meth public com.sun.tools.xjc.model.nav.NType getType()
meth public final boolean canBeReferencedByIDREF()
meth public final boolean isCollection()
meth public final com.sun.codemodel.JPackage _package()
meth public final com.sun.tools.xjc.model.CAdapter getAdapterUse()
meth public final com.sun.xml.bind.v2.model.annotation.Locatable getUpstream()
meth public final com.sun.xml.bind.v2.model.core.ID idUse()
meth public final com.sun.xml.bind.v2.runtime.Location getLocation()
meth public final com.sun.xml.xsom.XSComponent getSchemaComponent()
meth public final java.lang.String getDefaultValue()
meth public java.lang.String fullName()
meth public java.lang.String getSqueezedName()
meth public java.lang.String shortName()
meth public java.util.Collection<com.sun.tools.xjc.model.CElementInfo> getSubstitutionMembers()
meth public javax.activation.MimeType getExpectedMimeType()
meth public javax.xml.namespace.QName getElementName()
meth public org.xml.sax.Locator getLocator()
meth public void initContentType(com.sun.tools.xjc.model.TypeUse,com.sun.xml.xsom.XSElementDecl,com.sun.xml.xsom.XmlString)
 anno 2 com.sun.istack.Nullable()
meth public void setAbstract()
meth public void setSubstitutionHead(com.sun.tools.xjc.model.CElementInfo)
supr java.lang.Object
hfds className,model,property,squeezedName,substitutionHead,substitutionMembers,tagName,type

CLSS public final com.sun.tools.xjc.model.CElementPropertyInfo
cons public init(java.lang.String,com.sun.tools.xjc.model.CElementPropertyInfo$CollectionMode,com.sun.xml.bind.v2.model.core.ID,javax.activation.MimeType,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.model.CCustomizations,org.xml.sax.Locator,boolean)
innr public final static !enum CollectionMode
intf com.sun.xml.bind.v2.model.core.ElementPropertyInfo<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
meth public <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} accept(com.sun.tools.xjc.model.CPropertyVisitor2<{%%0},{%%1}>,{%%1})
meth public <%0 extends java.lang.Object> {%%0} accept(com.sun.tools.xjc.model.CPropertyVisitor<{%%0}>)
meth public boolean isCollectionNillable()
meth public boolean isCollectionRequired()
meth public boolean isOptionalPrimitive()
meth public boolean isRequired()
meth public boolean isUnboxable()
meth public boolean isValueList()
meth public com.sun.tools.xjc.model.CAdapter getAdapter()
meth public com.sun.xml.bind.v2.model.core.ID id()
meth public final com.sun.xml.bind.v2.model.core.PropertyKind kind()
meth public java.util.List<com.sun.tools.xjc.model.CNonElement> ref()
meth public java.util.List<com.sun.tools.xjc.model.CTypeRef> getTypes()
meth public javax.activation.MimeType getExpectedMimeType()
meth public javax.xml.namespace.QName collectElementNames(java.util.Map<javax.xml.namespace.QName,com.sun.tools.xjc.model.CPropertyInfo>)
meth public javax.xml.namespace.QName getSchemaType()
meth public javax.xml.namespace.QName getXmlName()
 anno 0 java.lang.Deprecated()
meth public void setAdapter(com.sun.tools.xjc.model.CAdapter)
supr com.sun.tools.xjc.model.CPropertyInfo
hfds adapter,expectedMimeType,id,isValueList,ref,required,types

CLSS public final static !enum com.sun.tools.xjc.model.CElementPropertyInfo$CollectionMode
 outer com.sun.tools.xjc.model.CElementPropertyInfo
fld public final static com.sun.tools.xjc.model.CElementPropertyInfo$CollectionMode NOT_REPEATED
fld public final static com.sun.tools.xjc.model.CElementPropertyInfo$CollectionMode REPEATED_ELEMENT
fld public final static com.sun.tools.xjc.model.CElementPropertyInfo$CollectionMode REPEATED_VALUE
meth public boolean isRepeated()
meth public static com.sun.tools.xjc.model.CElementPropertyInfo$CollectionMode valueOf(java.lang.String)
meth public static com.sun.tools.xjc.model.CElementPropertyInfo$CollectionMode[] values()
supr java.lang.Enum<com.sun.tools.xjc.model.CElementPropertyInfo$CollectionMode>
hfds col,val

CLSS public final com.sun.tools.xjc.model.CEnumConstant
cons public init(java.lang.String,java.lang.String,java.lang.String,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.model.CCustomizations,org.xml.sax.Locator)
fld public final java.lang.String javadoc
fld public final java.lang.String name
intf com.sun.tools.xjc.model.CCustomizable
intf com.sun.xml.bind.v2.model.core.EnumConstant<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
meth public com.sun.tools.xjc.model.CCustomizations getCustomizations()
meth public com.sun.tools.xjc.model.CEnumLeafInfo getEnclosingClass()
meth public com.sun.xml.xsom.XSComponent getSchemaComponent()
meth public java.lang.String getLexicalValue()
meth public java.lang.String getName()
meth public org.xml.sax.Locator getLocator()
supr java.lang.Object
hfds customizations,lexical,locator,parent,source

CLSS public final com.sun.tools.xjc.model.CEnumLeafInfo
cons public init(com.sun.tools.xjc.model.Model,javax.xml.namespace.QName,com.sun.tools.xjc.model.CClassInfoParent,java.lang.String,com.sun.tools.xjc.model.CNonElement,java.util.Collection<com.sun.tools.xjc.model.CEnumConstant>,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.model.CCustomizations,org.xml.sax.Locator)
fld public final com.sun.tools.xjc.model.CClassInfoParent parent
fld public final com.sun.tools.xjc.model.CNonElement base
fld public final com.sun.tools.xjc.model.Model model
fld public final java.lang.String shortName
fld public final java.util.Collection<com.sun.tools.xjc.model.CEnumConstant> members
fld public java.lang.String javadoc
intf com.sun.tools.xjc.model.CNonElement
intf com.sun.tools.xjc.model.nav.NClass
intf com.sun.xml.bind.v2.model.core.EnumLeafInfo<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
meth public boolean canBeReferencedByIDREF()
meth public boolean isAbstract()
meth public boolean isBoxedType()
meth public boolean isCollection()
 anno 0 java.lang.Deprecated()
meth public boolean isElement()
meth public boolean isPrimitive()
meth public boolean isSimpleType()
meth public boolean needsValueField()
meth public com.sun.codemodel.JClass toType(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.outline.Aspect)
meth public com.sun.codemodel.JExpression createConstant(com.sun.tools.xjc.outline.Outline,com.sun.xml.xsom.XmlString)
meth public com.sun.tools.xjc.model.CAdapter getAdapterUse()
 anno 0 java.lang.Deprecated()
meth public com.sun.tools.xjc.model.CCustomizations getCustomizations()
meth public com.sun.tools.xjc.model.CNonElement getInfo()
 anno 0 java.lang.Deprecated()
meth public com.sun.tools.xjc.model.nav.NClass getClazz()
meth public com.sun.tools.xjc.model.nav.NType getType()
meth public com.sun.xml.bind.v2.model.annotation.Locatable getUpstream()
meth public com.sun.xml.bind.v2.model.core.Element<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass> asElement()
meth public com.sun.xml.bind.v2.model.core.ID idUse()
meth public com.sun.xml.bind.v2.model.core.NonElement<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass> getBaseType()
meth public com.sun.xml.bind.v2.runtime.Location getLocation()
meth public com.sun.xml.xsom.XSComponent getSchemaComponent()
meth public java.lang.String fullName()
meth public java.util.Collection<com.sun.tools.xjc.model.CEnumConstant> getConstants()
meth public javax.activation.MimeType getExpectedMimeType()
meth public javax.xml.namespace.QName getElementName()
meth public javax.xml.namespace.QName getTypeName()
meth public org.xml.sax.Locator getLocator()
supr java.lang.Object
hfds customizations,source,sourceLocator,typeName

CLSS public abstract interface com.sun.tools.xjc.model.CNonElement
intf com.sun.tools.xjc.model.CTypeInfo
intf com.sun.tools.xjc.model.TypeUse
intf com.sun.xml.bind.v2.model.core.NonElement<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
meth public abstract boolean isCollection()
 anno 0 java.lang.Deprecated()
meth public abstract com.sun.tools.xjc.model.CAdapter getAdapterUse()
 anno 0 java.lang.Deprecated()
meth public abstract com.sun.tools.xjc.model.CNonElement getInfo()
 anno 0 java.lang.Deprecated()

CLSS public com.sun.tools.xjc.model.CPluginCustomization
cons public init(org.w3c.dom.Element,org.xml.sax.Locator)
fld public final org.w3c.dom.Element element
fld public final org.xml.sax.Locator locator
meth public boolean isAcknowledged()
meth public void markAsAcknowledged()
supr java.lang.Object
hfds acknowledged

CLSS public abstract com.sun.tools.xjc.model.CPropertyInfo
cons protected init(java.lang.String,boolean,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.model.CCustomizations,org.xml.sax.Locator)
fld public boolean inlineBinaryData
fld public com.sun.codemodel.JType baseType
fld public com.sun.tools.xjc.generator.bean.field.FieldRenderer realization
fld public com.sun.tools.xjc.model.CDefaultValue defaultValue
fld public final org.xml.sax.Locator locator
fld public java.lang.String javadoc
intf com.sun.tools.xjc.model.CCustomizable
intf com.sun.xml.bind.v2.model.core.PropertyInfo<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
meth protected static boolean needsExplicitTypeName(com.sun.tools.xjc.model.TypeUse,javax.xml.namespace.QName)
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} accept(com.sun.tools.xjc.model.CPropertyVisitor2<{%%0},{%%1}>,{%%1})
meth public abstract <%0 extends java.lang.Object> {%%0} accept(com.sun.tools.xjc.model.CPropertyVisitor<{%%0}>)
meth public abstract com.sun.tools.xjc.model.CAdapter getAdapter()
meth public abstract java.util.Collection<? extends com.sun.tools.xjc.model.CTypeInfo> ref()
meth public boolean inlineBinaryData()
meth public boolean isCollection()
meth public boolean isOptionalPrimitive()
meth public boolean isUnboxable()
meth public com.sun.tools.xjc.model.CCustomizations getCustomizations()
meth public com.sun.tools.xjc.model.CTypeInfo parent()
meth public final <%0 extends java.lang.annotation.Annotation> {%%0} readAnnotation(java.lang.Class<{%%0}>)
meth public final boolean hasAnnotation(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public final com.sun.xml.xsom.XSComponent getSchemaComponent()
meth public java.lang.String displayName()
meth public java.lang.String getName()
meth public java.lang.String getName(boolean)
meth public javax.xml.namespace.QName collectElementNames(java.util.Map<javax.xml.namespace.QName,com.sun.tools.xjc.model.CPropertyInfo>)
meth public org.xml.sax.Locator getLocator()
meth public void setName(boolean,java.lang.String)
supr java.lang.Object
hfds customizations,isCollection,parent,privateName,publicName,source

CLSS public abstract interface com.sun.tools.xjc.model.CPropertyVisitor<%0 extends java.lang.Object>
meth public abstract {com.sun.tools.xjc.model.CPropertyVisitor%0} onAttribute(com.sun.tools.xjc.model.CAttributePropertyInfo)
meth public abstract {com.sun.tools.xjc.model.CPropertyVisitor%0} onElement(com.sun.tools.xjc.model.CElementPropertyInfo)
meth public abstract {com.sun.tools.xjc.model.CPropertyVisitor%0} onReference(com.sun.tools.xjc.model.CReferencePropertyInfo)
meth public abstract {com.sun.tools.xjc.model.CPropertyVisitor%0} onValue(com.sun.tools.xjc.model.CValuePropertyInfo)

CLSS public abstract interface com.sun.tools.xjc.model.CPropertyVisitor2<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {com.sun.tools.xjc.model.CPropertyVisitor2%0} visit(com.sun.tools.xjc.model.CAttributePropertyInfo,{com.sun.tools.xjc.model.CPropertyVisitor2%1})
meth public abstract {com.sun.tools.xjc.model.CPropertyVisitor2%0} visit(com.sun.tools.xjc.model.CElementPropertyInfo,{com.sun.tools.xjc.model.CPropertyVisitor2%1})
meth public abstract {com.sun.tools.xjc.model.CPropertyVisitor2%0} visit(com.sun.tools.xjc.model.CReferencePropertyInfo,{com.sun.tools.xjc.model.CPropertyVisitor2%1})
meth public abstract {com.sun.tools.xjc.model.CPropertyVisitor2%0} visit(com.sun.tools.xjc.model.CValuePropertyInfo,{com.sun.tools.xjc.model.CPropertyVisitor2%1})

CLSS public final com.sun.tools.xjc.model.CReferencePropertyInfo
cons public init(java.lang.String,boolean,boolean,boolean,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.model.CCustomizations,org.xml.sax.Locator,boolean,boolean,boolean)
intf com.sun.xml.bind.v2.model.core.ReferencePropertyInfo<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
meth public <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} accept(com.sun.tools.xjc.model.CPropertyVisitor2<{%%0},{%%1}>,{%%1})
meth public <%0 extends java.lang.Object> {%%0} accept(com.sun.tools.xjc.model.CPropertyVisitor<{%%0}>)
meth public boolean isCollectionNillable()
meth public boolean isCollectionRequired()
meth public boolean isContent()
meth public boolean isDummy()
meth public boolean isMixed()
meth public boolean isMixedExtendedCust()
meth public boolean isOptionalPrimitive()
meth public boolean isRequired()
meth public boolean isUnboxable()
meth public com.sun.tools.xjc.model.CAdapter getAdapter()
meth public com.sun.tools.xjc.model.nav.NClass getDOMHandler()
meth public com.sun.xml.bind.v2.model.core.ID id()
meth public com.sun.xml.bind.v2.model.core.WildcardMode getWildcard()
meth public final com.sun.xml.bind.v2.model.core.PropertyKind kind()
meth public java.util.Set<? extends com.sun.tools.xjc.model.CTypeInfo> ref()
meth public java.util.Set<com.sun.tools.xjc.model.CElement> getElements()
meth public javax.activation.MimeType getExpectedMimeType()
meth public javax.xml.namespace.QName collectElementNames(java.util.Map<javax.xml.namespace.QName,com.sun.tools.xjc.model.CPropertyInfo>)
meth public javax.xml.namespace.QName getSchemaType()
meth public javax.xml.namespace.QName getXmlName()
 anno 0 java.lang.Deprecated()
meth public void setWildcard(com.sun.xml.bind.v2.model.core.WildcardMode)
supr com.sun.tools.xjc.model.CPropertyInfo
hfds content,dummy,elements,isMixed,isMixedExtendedCust,required,wildcard

CLSS public abstract interface com.sun.tools.xjc.model.CTypeInfo
intf com.sun.tools.xjc.model.CCustomizable
intf com.sun.xml.bind.v2.model.core.TypeInfo<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
meth public abstract com.sun.codemodel.JType toType(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.outline.Aspect)

CLSS public final com.sun.tools.xjc.model.CTypeRef
cons public init(com.sun.tools.xjc.model.CNonElement,com.sun.xml.xsom.XSElementDecl)
cons public init(com.sun.tools.xjc.model.CNonElement,javax.xml.namespace.QName,javax.xml.namespace.QName,boolean,com.sun.xml.xsom.XmlString)
fld public final com.sun.xml.xsom.XmlString defaultValue
intf com.sun.xml.bind.v2.model.core.TypeRef<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
meth public boolean isLeaf()
meth public boolean isNillable()
meth public com.sun.tools.xjc.model.CNonElement getTarget()
meth public com.sun.xml.bind.v2.model.core.PropertyInfo<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass> getSource()
meth public java.lang.String getDefaultValue()
meth public javax.xml.namespace.QName getTagName()
meth public javax.xml.namespace.QName getTypeName()
meth public static javax.xml.namespace.QName getSimpleTypeName(com.sun.xml.xsom.XSElementDecl)
supr java.lang.Object
hfds elementName,nillable,type,typeName

CLSS public final com.sun.tools.xjc.model.CValuePropertyInfo
cons public init(java.lang.String,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.model.CCustomizations,org.xml.sax.Locator,com.sun.tools.xjc.model.TypeUse,javax.xml.namespace.QName)
fld protected final com.sun.tools.xjc.model.TypeUse type
intf com.sun.xml.bind.v2.model.core.ValuePropertyInfo<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
meth public <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} accept(com.sun.tools.xjc.model.CPropertyVisitor2<{%%0},{%%1}>,{%%1})
meth public <%0 extends java.lang.Object> {%%0} accept(com.sun.tools.xjc.model.CPropertyVisitor<{%%0}>)
meth public final com.sun.tools.xjc.model.CAdapter getAdapter()
meth public final com.sun.tools.xjc.model.CNonElement getTarget()
meth public final com.sun.tools.xjc.model.CPropertyInfo getSource()
meth public final com.sun.xml.bind.v2.model.core.ID id()
meth public final com.sun.xml.bind.v2.model.core.PropertyKind kind()
meth public final java.util.List<? extends com.sun.tools.xjc.model.CTypeInfo> ref()
meth public final javax.activation.MimeType getExpectedMimeType()
meth public javax.xml.namespace.QName getSchemaType()
supr com.sun.tools.xjc.model.CPropertyInfo

CLSS public final com.sun.tools.xjc.model.CWildcardTypeInfo
fld public final static com.sun.tools.xjc.model.CWildcardTypeInfo INSTANCE
intf com.sun.tools.xjc.model.CTypeInfo
intf com.sun.xml.bind.v2.model.core.WildcardTypeInfo<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>
meth public com.sun.codemodel.JExpression createConstant(com.sun.tools.xjc.outline.Outline,com.sun.xml.xsom.XmlString)
meth public com.sun.codemodel.JType toType(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.outline.Aspect)
meth public com.sun.tools.xjc.model.CCustomizations getCustomizations()
meth public com.sun.tools.xjc.model.nav.NType getType()
meth public final boolean canBeReferencedByIDREF()
meth public final boolean isCollection()
meth public final com.sun.tools.xjc.model.CAdapter getAdapterUse()
meth public final com.sun.xml.bind.v2.model.annotation.Locatable getUpstream()
meth public final com.sun.xml.bind.v2.model.core.ID idUse()
meth public final com.sun.xml.bind.v2.runtime.Location getLocation()
meth public final com.sun.xml.xsom.XSComponent getSchemaComponent()
meth public javax.activation.MimeType getExpectedMimeType()
meth public org.xml.sax.Locator getLocator()
supr java.lang.Object

CLSS public com.sun.tools.xjc.model.Constructor
cons public init(java.lang.String[])
fld public final java.lang.String[] fields
supr java.lang.Object

CLSS public final com.sun.tools.xjc.model.Model
cons public init(com.sun.tools.xjc.Options,com.sun.codemodel.JCodeModel,com.sun.xml.bind.api.impl.NameConverter,com.sun.tools.xjc.api.ClassNameAllocator,com.sun.xml.xsom.XSSchemaSet)
fld public boolean serializable
fld public com.sun.codemodel.JClass rootClass
fld public com.sun.codemodel.JClass rootInterface
fld public com.sun.tools.xjc.generator.bean.ImplStructureStrategy strategy
fld public final com.sun.codemodel.JCodeModel codeModel
fld public final com.sun.tools.xjc.Options options
fld public final com.sun.tools.xjc.model.SymbolSpace defaultSymbolSpace
fld public final com.sun.xml.xsom.XSSchemaSet schemaComponent
fld public java.lang.Long serialVersionUID
intf com.sun.tools.xjc.model.CCustomizable
intf com.sun.xml.bind.v2.model.core.TypeInfoSet<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass,java.lang.Void,java.lang.Void>
meth public boolean isPackageLevelAnnotations()
meth public com.sun.tools.xjc.model.CBuiltinLeafInfo getAnyTypeInfo()
meth public com.sun.tools.xjc.model.CClassInfo getClassInfo(com.sun.tools.xjc.model.nav.NClass)
meth public com.sun.tools.xjc.model.CClassInfoParent$Package getPackage(com.sun.codemodel.JPackage)
meth public com.sun.tools.xjc.model.CCustomizations getCustomizations()
meth public com.sun.tools.xjc.model.CElementInfo getElementInfo(com.sun.tools.xjc.model.nav.NClass,javax.xml.namespace.QName)
meth public com.sun.tools.xjc.model.CNonElement getTypeInfo(com.sun.tools.xjc.model.nav.NType)
meth public com.sun.tools.xjc.model.CNonElement getTypeInfo(com.sun.xml.bind.v2.model.core.Ref<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass>)
meth public com.sun.tools.xjc.model.SymbolSpace getSymbolSpace(java.lang.String)
meth public com.sun.tools.xjc.outline.Outline generateCode(com.sun.tools.xjc.Options,com.sun.tools.xjc.ErrorReceiver)
meth public com.sun.xml.bind.v2.model.nav.Navigator<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass,java.lang.Void,java.lang.Void> getNavigator()
meth public com.sun.xml.xsom.XSComponent getSchemaComponent()
meth public final com.sun.xml.bind.api.impl.NameConverter getNameConverter()
meth public final java.util.Map<javax.xml.namespace.QName,com.sun.tools.xjc.model.CClassInfo> createTopLevelBindings()
meth public java.lang.Iterable<? extends com.sun.tools.xjc.model.CElementInfo> getAllElements()
meth public java.util.Map<com.sun.tools.xjc.model.nav.NClass,com.sun.tools.xjc.model.CClassInfo> beans()
meth public java.util.Map<com.sun.tools.xjc.model.nav.NClass,com.sun.tools.xjc.model.CEnumLeafInfo> enums()
meth public java.util.Map<com.sun.tools.xjc.model.nav.NType,? extends com.sun.tools.xjc.model.CArrayInfo> arrays()
meth public java.util.Map<com.sun.tools.xjc.model.nav.NType,? extends com.sun.tools.xjc.model.CBuiltinLeafInfo> builtins()
meth public java.util.Map<java.lang.String,java.lang.String> getSchemaLocations()
meth public java.util.Map<java.lang.String,java.lang.String> getXmlNs(java.lang.String)
meth public java.util.Map<javax.xml.namespace.QName,com.sun.tools.xjc.model.CElementInfo> getElementMappings(com.sun.tools.xjc.model.nav.NClass)
meth public java.util.Map<javax.xml.namespace.QName,com.sun.tools.xjc.model.TypeUse> typeUses()
meth public javax.xml.bind.annotation.XmlNsForm getAttributeFormDefault(java.lang.String)
meth public javax.xml.bind.annotation.XmlNsForm getElementFormDefault(java.lang.String)
meth public org.xml.sax.Locator getLocator()
meth public void dump(javax.xml.transform.Result)
meth public void setNameConverter(com.sun.xml.bind.api.impl.NameConverter)
meth public void setPackageLevelAnnotations(boolean)
supr java.lang.Object
hfds EMPTY_LOCATOR,allElements,allocator,beans,cache,customizations,elementMappings,enums,globalCustomizations,nameConverter,packageLevelAnnotations,symbolSpaces,typeUses

CLSS public final com.sun.tools.xjc.model.Multiplicity
fld public final java.math.BigInteger max
fld public final java.math.BigInteger min
fld public final static com.sun.tools.xjc.model.Multiplicity ONE
fld public final static com.sun.tools.xjc.model.Multiplicity OPTIONAL
fld public final static com.sun.tools.xjc.model.Multiplicity PLUS
fld public final static com.sun.tools.xjc.model.Multiplicity STAR
fld public final static com.sun.tools.xjc.model.Multiplicity ZERO
meth public boolean equals(java.lang.Object)
meth public boolean includes(com.sun.tools.xjc.model.Multiplicity)
meth public boolean isAtMostOnce()
meth public boolean isOptional()
meth public boolean isUnique()
meth public boolean isZero()
meth public com.sun.tools.xjc.model.Multiplicity makeOptional()
meth public com.sun.tools.xjc.model.Multiplicity makeRepeated()
meth public int hashCode()
meth public java.lang.String getMaxString()
meth public java.lang.String toString()
meth public static com.sun.tools.xjc.model.Multiplicity choice(com.sun.tools.xjc.model.Multiplicity,com.sun.tools.xjc.model.Multiplicity)
meth public static com.sun.tools.xjc.model.Multiplicity create(int,java.lang.Integer)
meth public static com.sun.tools.xjc.model.Multiplicity create(java.math.BigInteger,java.math.BigInteger)
meth public static com.sun.tools.xjc.model.Multiplicity group(com.sun.tools.xjc.model.Multiplicity,com.sun.tools.xjc.model.Multiplicity)
meth public static com.sun.tools.xjc.model.Multiplicity multiply(com.sun.tools.xjc.model.Multiplicity,com.sun.tools.xjc.model.Multiplicity)
meth public static com.sun.tools.xjc.model.Multiplicity oneOrMore(com.sun.tools.xjc.model.Multiplicity)
supr java.lang.Object

CLSS public abstract interface com.sun.tools.xjc.model.Populatable
meth public abstract void populate(com.sun.tools.xjc.model.Model,com.sun.tools.xjc.outline.Outline)

CLSS public com.sun.tools.xjc.model.SymbolSpace
cons public init(com.sun.codemodel.JCodeModel)
meth public com.sun.codemodel.JType getType()
meth public java.lang.String toString()
meth public void setType(com.sun.codemodel.JType)
supr java.lang.Object
hfds codeModel,type

CLSS public abstract interface com.sun.tools.xjc.model.TypeUse
meth public abstract boolean isCollection()
meth public abstract com.sun.codemodel.JExpression createConstant(com.sun.tools.xjc.outline.Outline,com.sun.xml.xsom.XmlString)
meth public abstract com.sun.tools.xjc.model.CAdapter getAdapterUse()
meth public abstract com.sun.tools.xjc.model.CNonElement getInfo()
meth public abstract com.sun.xml.bind.v2.model.core.ID idUse()
meth public abstract javax.activation.MimeType getExpectedMimeType()

CLSS public final com.sun.tools.xjc.model.TypeUseFactory
meth public static com.sun.tools.xjc.model.TypeUse adapt(com.sun.tools.xjc.model.TypeUse,com.sun.tools.xjc.model.CAdapter)
meth public static com.sun.tools.xjc.model.TypeUse adapt(com.sun.tools.xjc.model.TypeUse,java.lang.Class<? extends javax.xml.bind.annotation.adapters.XmlAdapter>,boolean)
meth public static com.sun.tools.xjc.model.TypeUse makeCollection(com.sun.tools.xjc.model.TypeUse)
meth public static com.sun.tools.xjc.model.TypeUse makeID(com.sun.tools.xjc.model.TypeUse,com.sun.xml.bind.v2.model.core.ID)
meth public static com.sun.tools.xjc.model.TypeUse makeMimeTyped(com.sun.tools.xjc.model.TypeUse,javax.activation.MimeType)
supr java.lang.Object

CLSS public com.sun.tools.xjc.model.nav.EagerNClass
cons public init(java.lang.Class)
intf com.sun.tools.xjc.model.nav.NClass
intf com.sun.tools.xjc.model.nav.NType
meth public boolean equals(java.lang.Object)
meth public boolean isAbstract()
meth public boolean isBoxedType()
meth public com.sun.codemodel.JClass toType(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.outline.Aspect)
meth public int hashCode()
meth public java.lang.String fullName()
supr java.lang.Object
hfds boxedTypes,c

CLSS public abstract interface com.sun.tools.xjc.model.nav.NClass
intf com.sun.tools.xjc.model.nav.NType
meth public abstract boolean isAbstract()
meth public abstract com.sun.codemodel.JClass toType(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.outline.Aspect)

CLSS public abstract interface com.sun.tools.xjc.model.nav.NType
meth public abstract boolean isBoxedType()
meth public abstract com.sun.codemodel.JType toType(com.sun.tools.xjc.outline.Outline,com.sun.tools.xjc.outline.Aspect)
meth public abstract java.lang.String fullName()

CLSS public final com.sun.tools.xjc.model.nav.NavigatorImpl
fld public final static com.sun.tools.xjc.model.nav.NavigatorImpl theInstance
intf com.sun.xml.bind.v2.model.nav.Navigator<com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass,java.lang.Void,java.lang.Void>
meth public !varargs static com.sun.tools.xjc.model.nav.NType createParameterizedType(com.sun.tools.xjc.model.nav.NClass,com.sun.tools.xjc.model.nav.NType[])
meth public !varargs static com.sun.tools.xjc.model.nav.NType createParameterizedType(java.lang.Class,com.sun.tools.xjc.model.nav.NType[])
meth public <%0 extends java.lang.Object> com.sun.tools.xjc.model.nav.NType erasure(com.sun.tools.xjc.model.nav.NType)
meth public boolean hasDefaultConstructor(com.sun.tools.xjc.model.nav.NClass)
meth public boolean isAbstract(com.sun.tools.xjc.model.nav.NClass)
meth public boolean isArray(com.sun.tools.xjc.model.nav.NType)
meth public boolean isArrayButNotByteArray(com.sun.tools.xjc.model.nav.NType)
meth public boolean isBridgeMethod(java.lang.Void)
meth public boolean isEnum(com.sun.tools.xjc.model.nav.NClass)
meth public boolean isFinal(com.sun.tools.xjc.model.nav.NClass)
meth public boolean isFinalMethod(java.lang.Void)
meth public boolean isInnerClass(com.sun.tools.xjc.model.nav.NClass)
meth public boolean isInterface(com.sun.tools.xjc.model.nav.NClass)
meth public boolean isOverriding(java.lang.Void,com.sun.tools.xjc.model.nav.NClass)
meth public boolean isParameterizedType(com.sun.tools.xjc.model.nav.NType)
meth public boolean isPrimitive(com.sun.tools.xjc.model.nav.NType)
meth public boolean isPublicField(java.lang.Void)
meth public boolean isPublicMethod(java.lang.Void)
meth public boolean isSameType(com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NType)
meth public boolean isStaticField(java.lang.Void)
meth public boolean isStaticMethod(java.lang.Void)
meth public boolean isSubClassOf(com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NType)
meth public boolean isTransient(java.lang.Void)
meth public com.sun.tools.xjc.model.nav.NClass asDecl(com.sun.tools.xjc.model.nav.NType)
meth public com.sun.tools.xjc.model.nav.NClass asDecl(java.lang.Class)
meth public com.sun.tools.xjc.model.nav.NClass getDeclaringClassForField(java.lang.Void)
meth public com.sun.tools.xjc.model.nav.NClass getDeclaringClassForMethod(java.lang.Void)
meth public com.sun.tools.xjc.model.nav.NClass getSuperClass(com.sun.tools.xjc.model.nav.NClass)
meth public com.sun.tools.xjc.model.nav.NClass loadObjectFactory(com.sun.tools.xjc.model.nav.NClass,java.lang.String)
meth public com.sun.tools.xjc.model.nav.NClass ref(com.sun.codemodel.JClass)
meth public com.sun.tools.xjc.model.nav.NClass ref(java.lang.Class)
meth public com.sun.tools.xjc.model.nav.NType getBaseClass(com.sun.tools.xjc.model.nav.NType,com.sun.tools.xjc.model.nav.NClass)
meth public com.sun.tools.xjc.model.nav.NType getComponentType(com.sun.tools.xjc.model.nav.NType)
meth public com.sun.tools.xjc.model.nav.NType getFieldType(java.lang.Void)
meth public com.sun.tools.xjc.model.nav.NType getPrimitive(java.lang.Class)
meth public com.sun.tools.xjc.model.nav.NType getReturnType(java.lang.Void)
meth public com.sun.tools.xjc.model.nav.NType getTypeArgument(com.sun.tools.xjc.model.nav.NType,int)
meth public com.sun.tools.xjc.model.nav.NType getVoidType()
meth public com.sun.tools.xjc.model.nav.NType use(com.sun.tools.xjc.model.nav.NClass)
meth public com.sun.tools.xjc.model.nav.NType[] getMethodParameters(java.lang.Void)
meth public com.sun.xml.bind.v2.runtime.Location getClassLocation(com.sun.tools.xjc.model.nav.NClass)
meth public com.sun.xml.bind.v2.runtime.Location getFieldLocation(java.lang.Void)
meth public com.sun.xml.bind.v2.runtime.Location getMethodLocation(java.lang.Void)
meth public final static com.sun.tools.xjc.model.nav.NType create(java.lang.reflect.Type)
meth public java.lang.String getClassName(com.sun.tools.xjc.model.nav.NClass)
meth public java.lang.String getClassShortName(com.sun.tools.xjc.model.nav.NClass)
meth public java.lang.String getFieldName(java.lang.Void)
meth public java.lang.String getMethodName(java.lang.Void)
meth public java.lang.String getPackageName(com.sun.tools.xjc.model.nav.NClass)
meth public java.lang.String getTypeName(com.sun.tools.xjc.model.nav.NType)
meth public java.lang.Void getDeclaredField(com.sun.tools.xjc.model.nav.NClass,java.lang.String)
meth public java.lang.Void[] getEnumConstants(com.sun.tools.xjc.model.nav.NClass)
meth public java.util.Collection<? extends java.lang.Void> getDeclaredFields(com.sun.tools.xjc.model.nav.NClass)
meth public java.util.Collection<? extends java.lang.Void> getDeclaredMethods(com.sun.tools.xjc.model.nav.NClass)
meth public static com.sun.tools.xjc.model.nav.NClass create(java.lang.Class)
supr java.lang.Object

CLSS public final !enum com.sun.tools.xjc.outline.Aspect
fld public final static com.sun.tools.xjc.outline.Aspect EXPOSED
fld public final static com.sun.tools.xjc.outline.Aspect IMPLEMENTATION
meth public static com.sun.tools.xjc.outline.Aspect valueOf(java.lang.String)
meth public static com.sun.tools.xjc.outline.Aspect[] values()
supr java.lang.Enum<com.sun.tools.xjc.outline.Aspect>

CLSS public abstract com.sun.tools.xjc.outline.ClassOutline
cons protected init(com.sun.tools.xjc.model.CClassInfo,com.sun.codemodel.JDefinedClass,com.sun.codemodel.JClass,com.sun.codemodel.JDefinedClass)
fld public final com.sun.codemodel.JClass implRef
 anno 0 com.sun.istack.NotNull()
fld public final com.sun.codemodel.JDefinedClass implClass
 anno 0 com.sun.istack.NotNull()
fld public final com.sun.codemodel.JDefinedClass ref
 anno 0 com.sun.istack.NotNull()
fld public final com.sun.tools.xjc.model.CClassInfo target
 anno 0 com.sun.istack.NotNull()
intf com.sun.tools.xjc.outline.CustomizableOutline
meth public abstract com.sun.tools.xjc.outline.Outline parent()
 anno 0 com.sun.istack.NotNull()
meth public com.sun.codemodel.JDefinedClass getImplClass()
meth public com.sun.tools.xjc.model.CCustomizable getTarget()
meth public com.sun.tools.xjc.outline.PackageOutline _package()
 anno 0 com.sun.istack.NotNull()
meth public final com.sun.tools.xjc.outline.ClassOutline getSuperClass()
meth public final com.sun.tools.xjc.outline.FieldOutline[] getDeclaredFields()
supr java.lang.Object

CLSS public abstract interface com.sun.tools.xjc.outline.CustomizableOutline
meth public abstract com.sun.codemodel.JDefinedClass getImplClass()
 anno 0 com.sun.istack.NotNull()
meth public abstract com.sun.tools.xjc.model.CCustomizable getTarget()
 anno 0 com.sun.istack.NotNull()

CLSS public abstract com.sun.tools.xjc.outline.ElementOutline
cons protected init(com.sun.tools.xjc.model.CElementInfo,com.sun.codemodel.JDefinedClass)
fld public final com.sun.codemodel.JDefinedClass implClass
fld public final com.sun.tools.xjc.model.CElementInfo target
intf com.sun.tools.xjc.outline.CustomizableOutline
meth public abstract com.sun.tools.xjc.outline.Outline parent()
meth public com.sun.codemodel.JDefinedClass getImplClass()
meth public com.sun.tools.xjc.model.CCustomizable getTarget()
meth public com.sun.tools.xjc.outline.PackageOutline _package()
supr java.lang.Object

CLSS public abstract com.sun.tools.xjc.outline.EnumConstantOutline
cons protected init(com.sun.tools.xjc.model.CEnumConstant,com.sun.codemodel.JEnumConstant)
fld public final com.sun.codemodel.JEnumConstant constRef
fld public final com.sun.tools.xjc.model.CEnumConstant target
supr java.lang.Object

CLSS public abstract com.sun.tools.xjc.outline.EnumOutline
cons protected init(com.sun.tools.xjc.model.CEnumLeafInfo,com.sun.codemodel.JDefinedClass)
fld public final com.sun.codemodel.JDefinedClass clazz
fld public final com.sun.tools.xjc.model.CEnumLeafInfo target
fld public final java.util.List<com.sun.tools.xjc.outline.EnumConstantOutline> constants
intf com.sun.tools.xjc.outline.CustomizableOutline
meth public abstract com.sun.tools.xjc.outline.Outline parent()
 anno 0 com.sun.istack.NotNull()
meth public com.sun.codemodel.JDefinedClass getImplClass()
meth public com.sun.tools.xjc.model.CCustomizable getTarget()
meth public com.sun.tools.xjc.outline.PackageOutline _package()
 anno 0 com.sun.istack.NotNull()
supr java.lang.Object

CLSS public abstract interface com.sun.tools.xjc.outline.FieldAccessor
meth public abstract com.sun.codemodel.JExpression hasSetValue()
meth public abstract com.sun.tools.xjc.model.CPropertyInfo getPropertyInfo()
meth public abstract com.sun.tools.xjc.outline.FieldOutline owner()
meth public abstract void fromRawValue(com.sun.codemodel.JBlock,java.lang.String,com.sun.codemodel.JExpression)
meth public abstract void toRawValue(com.sun.codemodel.JBlock,com.sun.codemodel.JVar)
meth public abstract void unsetValues(com.sun.codemodel.JBlock)

CLSS public abstract interface com.sun.tools.xjc.outline.FieldOutline
meth public abstract com.sun.codemodel.JType getRawType()
meth public abstract com.sun.tools.xjc.model.CPropertyInfo getPropertyInfo()
meth public abstract com.sun.tools.xjc.outline.ClassOutline parent()
meth public abstract com.sun.tools.xjc.outline.FieldAccessor create(com.sun.codemodel.JExpression)

CLSS public abstract interface com.sun.tools.xjc.outline.Outline
meth public abstract com.sun.codemodel.JClass addRuntime(java.lang.Class)
meth public abstract com.sun.codemodel.JClassContainer getContainer(com.sun.tools.xjc.model.CClassInfoParent,com.sun.tools.xjc.outline.Aspect)
meth public abstract com.sun.codemodel.JCodeModel getCodeModel()
meth public abstract com.sun.codemodel.JType resolve(com.sun.tools.xjc.model.CTypeRef,com.sun.tools.xjc.outline.Aspect)
meth public abstract com.sun.tools.xjc.ErrorReceiver getErrorReceiver()
meth public abstract com.sun.tools.xjc.model.Model getModel()
meth public abstract com.sun.tools.xjc.outline.ClassOutline getClazz(com.sun.tools.xjc.model.CClassInfo)
meth public abstract com.sun.tools.xjc.outline.ElementOutline getElement(com.sun.tools.xjc.model.CElementInfo)
meth public abstract com.sun.tools.xjc.outline.EnumOutline getEnum(com.sun.tools.xjc.model.CEnumLeafInfo)
meth public abstract com.sun.tools.xjc.outline.FieldOutline getField(com.sun.tools.xjc.model.CPropertyInfo)
meth public abstract com.sun.tools.xjc.outline.PackageOutline getPackageContext(com.sun.codemodel.JPackage)
meth public abstract com.sun.tools.xjc.util.CodeModelClassFactory getClassFactory()
meth public abstract java.lang.Iterable<? extends com.sun.tools.xjc.outline.PackageOutline> getAllPackageContexts()
meth public abstract java.util.Collection<? extends com.sun.tools.xjc.outline.ClassOutline> getClasses()
meth public abstract java.util.Collection<com.sun.tools.xjc.outline.EnumOutline> getEnums()

CLSS public abstract interface com.sun.tools.xjc.outline.PackageOutline
meth public abstract com.sun.codemodel.JDefinedClass objectFactory()
meth public abstract com.sun.codemodel.JPackage _package()
meth public abstract com.sun.tools.xjc.generator.bean.ObjectFactoryGenerator objectFactoryGenerator()
meth public abstract java.lang.String getMostUsedNamespaceURI()
meth public abstract java.util.Set<? extends com.sun.tools.xjc.outline.ClassOutline> getClasses()
meth public abstract javax.xml.bind.annotation.XmlNsForm getAttributeFormDefault()
meth public abstract javax.xml.bind.annotation.XmlNsForm getElementFormDefault()

CLSS public abstract com.sun.tools.xjc.reader.AbstractExtensionBindingChecker
cons public init(java.lang.String,com.sun.tools.xjc.Options,org.xml.sax.ErrorHandler)
fld protected final boolean allowExtensions
fld protected final java.lang.String schemaLanguage
fld protected final java.util.Set<java.lang.String> enabledExtensions
fld protected final org.xml.sax.helpers.NamespaceSupport nsSupport
meth protected final boolean isRecognizableExtension(java.lang.String)
meth protected final boolean isSupportedExtension(java.lang.String)
meth protected final org.xml.sax.SAXParseException error(java.lang.String) throws org.xml.sax.SAXException
meth protected final void checkAndEnable(java.lang.String) throws org.xml.sax.SAXException
meth protected final void verifyTagName(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth protected final void warning(java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr com.sun.tools.xjc.util.SubtreeCutter
hfds locator,options,recognizableExtensions

CLSS public com.sun.tools.xjc.reader.Const
cons public init()
fld public final static java.lang.String DTD = "DTD"
fld public final static java.lang.String EXPECTED_CONTENT_TYPES = "expectedContentTypes"
fld public final static java.lang.String JAXB_NSURI = "http://java.sun.com/xml/ns/jaxb"
fld public final static java.lang.String RELAXNG_URI = "http://relaxng.org/ns/structure/1.0"
fld public final static java.lang.String XJC_EXTENSION_URI = "http://java.sun.com/xml/ns/jaxb/xjc"
fld public final static java.lang.String XMLNS_URI = "http://www.w3.org/2000/xmlns/"
supr java.lang.Object

CLSS public final com.sun.tools.xjc.reader.ExtensionBindingChecker
cons public init(java.lang.String,com.sun.tools.xjc.Options,org.xml.sax.ErrorHandler)
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
supr com.sun.tools.xjc.reader.AbstractExtensionBindingChecker
hfds count

CLSS public final !enum com.sun.tools.xjc.reader.Messages
fld public final static com.sun.tools.xjc.reader.Messages DUPLICATE_ELEMENT
fld public final static com.sun.tools.xjc.reader.Messages DUPLICATE_PROPERTY
fld public final static com.sun.tools.xjc.reader.Messages ERR_CLASS_NOT_FOUND
fld public final static com.sun.tools.xjc.reader.Messages ERR_ILLEGAL_CUSTOMIZATION_TAGNAME
fld public final static com.sun.tools.xjc.reader.Messages ERR_PLUGIN_NOT_ENABLED
fld public final static com.sun.tools.xjc.reader.Messages ERR_RELEVANT_LOCATION
fld public final static com.sun.tools.xjc.reader.Messages ERR_SUPPORTED_EXTENSION_IGNORED
fld public final static com.sun.tools.xjc.reader.Messages ERR_UNDECLARED_PREFIX
fld public final static com.sun.tools.xjc.reader.Messages ERR_UNEXPECTED_EXTENSION_BINDING_PREFIXES
fld public final static com.sun.tools.xjc.reader.Messages ERR_UNSUPPORTED_EXTENSION
fld public final static com.sun.tools.xjc.reader.Messages ERR_VENDOR_EXTENSION_DISALLOWED_IN_STRICT_MODE
fld public final static com.sun.tools.xjc.reader.Messages PROPERTY_CLASS_IS_RESERVED
meth public !varargs java.lang.String format(java.lang.Object[])
meth public java.lang.String toString()
meth public static com.sun.tools.xjc.reader.Messages valueOf(java.lang.String)
meth public static com.sun.tools.xjc.reader.Messages[] values()
supr java.lang.Enum<com.sun.tools.xjc.reader.Messages>
hfds rb

CLSS public final com.sun.tools.xjc.reader.ModelChecker
cons public init()
meth public void check()
supr java.lang.Object
hfds errorReceiver,model

CLSS public final com.sun.tools.xjc.reader.RawTypeSet
cons public init(java.util.Set<com.sun.tools.xjc.reader.RawTypeSet$Ref>,com.sun.tools.xjc.model.Multiplicity)
fld public final com.sun.tools.xjc.model.Multiplicity mul
fld public final com.sun.tools.xjc.reader.RawTypeSet$Mode canBeTypeRefs
fld public final java.util.Set<com.sun.tools.xjc.reader.RawTypeSet$Ref> refs
innr public abstract static Ref
innr public final static !enum Mode
meth public boolean isRequired()
meth public com.sun.tools.xjc.model.CElementPropertyInfo$CollectionMode getCollectionMode()
meth public com.sun.xml.bind.v2.model.core.ID id()
meth public javax.activation.MimeType getExpectedMimeType()
meth public void addTo(com.sun.tools.xjc.model.CElementPropertyInfo)
meth public void addTo(com.sun.tools.xjc.model.CReferencePropertyInfo)
supr java.lang.Object
hfds collectionMode

CLSS public final static !enum com.sun.tools.xjc.reader.RawTypeSet$Mode
 outer com.sun.tools.xjc.reader.RawTypeSet
fld public final static com.sun.tools.xjc.reader.RawTypeSet$Mode CAN_BE_TYPEREF
fld public final static com.sun.tools.xjc.reader.RawTypeSet$Mode MUST_BE_REFERENCE
fld public final static com.sun.tools.xjc.reader.RawTypeSet$Mode SHOULD_BE_TYPEREF
meth public static com.sun.tools.xjc.reader.RawTypeSet$Mode valueOf(java.lang.String)
meth public static com.sun.tools.xjc.reader.RawTypeSet$Mode[] values()
supr java.lang.Enum<com.sun.tools.xjc.reader.RawTypeSet$Mode>
hfds rank

CLSS public abstract static com.sun.tools.xjc.reader.RawTypeSet$Ref
 outer com.sun.tools.xjc.reader.RawTypeSet
cons public init()
meth protected abstract boolean isListOfValues()
meth protected abstract com.sun.tools.xjc.model.CTypeRef toTypeRef(com.sun.tools.xjc.model.CElementPropertyInfo)
meth protected abstract com.sun.tools.xjc.reader.RawTypeSet$Mode canBeType(com.sun.tools.xjc.reader.RawTypeSet)
meth protected abstract com.sun.xml.bind.v2.model.core.ID id()
meth protected abstract void toElementRef(com.sun.tools.xjc.model.CReferencePropertyInfo)
meth protected javax.activation.MimeType getExpectedMimeType()
supr java.lang.Object

CLSS public final com.sun.tools.xjc.reader.Ring
meth public static <%0 extends java.lang.Object> void add(java.lang.Class<{%%0}>,{%%0})
meth public static <%0 extends java.lang.Object> void add({%%0})
meth public static <%0 extends java.lang.Object> {%%0} get(java.lang.Class<{%%0}>)
meth public static com.sun.tools.xjc.reader.Ring begin()
meth public static com.sun.tools.xjc.reader.Ring get()
meth public static void end(com.sun.tools.xjc.reader.Ring)
supr java.lang.Object
hfds components,instances

CLSS public com.sun.tools.xjc.reader.TypeUtil
cons public init()
meth public !varargs static com.sun.codemodel.JType getCommonBaseType(com.sun.codemodel.JCodeModel,com.sun.codemodel.JType[])
meth public static com.sun.codemodel.JType getCommonBaseType(com.sun.codemodel.JCodeModel,java.util.Collection<? extends com.sun.codemodel.JType>)
meth public static com.sun.codemodel.JType getType(com.sun.codemodel.JCodeModel,java.lang.String,com.sun.tools.xjc.ErrorReceiver,org.xml.sax.Locator)
supr java.lang.Object
hfds typeComparator

CLSS public com.sun.tools.xjc.reader.Util
cons public init()
meth public static java.lang.Object getFileOrURL(java.lang.String) throws java.io.IOException
meth public static java.lang.String escapeSpace(java.lang.String)
meth public static org.xml.sax.InputSource getInputSource(java.lang.String)
supr java.lang.Object

CLSS public com.sun.tools.xjc.reader.dtd.TDTDReader
cons protected init(com.sun.tools.xjc.ErrorReceiver,com.sun.tools.xjc.Options,org.xml.sax.InputSource)
meth protected !varargs final void error(org.xml.sax.Locator,java.lang.String,java.lang.Object[])
meth protected com.sun.tools.xjc.model.CPropertyInfo createAttribute(java.lang.String,java.lang.String,java.lang.String,java.lang.String[],short,java.lang.String) throws org.xml.sax.SAXException
meth public static com.sun.tools.xjc.model.Model parse(org.xml.sax.InputSource,org.xml.sax.InputSource,com.sun.tools.xjc.ErrorReceiver,com.sun.tools.xjc.Options)
meth public void attributeDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String[],short,java.lang.String) throws org.xml.sax.SAXException
meth public void childElement(java.lang.String,short) throws org.xml.sax.SAXException
meth public void connector(short) throws org.xml.sax.SAXException
meth public void endContentModel(java.lang.String,short) throws org.xml.sax.SAXException
meth public void endDTD() throws org.xml.sax.SAXException
meth public void endModelGroup(short) throws org.xml.sax.SAXException
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void startContentModel(java.lang.String,short) throws org.xml.sax.SAXException
meth public void startDTD(com.sun.xml.dtdparser.InputEntity) throws org.xml.sax.SAXException
meth public void startModelGroup() throws org.xml.sax.SAXException
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr com.sun.xml.dtdparser.DTDHandlerBase
hfds bindInfo,builtinConversions,classFactory,elements,entityResolver,errorReceiver,locator,model,modelGroups
hcls InterfaceAcceptor

CLSS public com.sun.tools.xjc.reader.dtd.bindinfo.BIAttribute
meth public com.sun.tools.xjc.reader.dtd.bindinfo.BIConversion getConversion()
meth public final com.sun.tools.xjc.generator.bean.field.FieldRenderer getRealization()
meth public final java.lang.String getPropertyName()
meth public final java.lang.String name()
supr java.lang.Object
hfds element,parent

CLSS public com.sun.tools.xjc.reader.dtd.bindinfo.BIConstructor
meth public org.xml.sax.Locator getSourceLocation()
meth public void createDeclaration(com.sun.tools.xjc.model.CClassInfo)
supr java.lang.Object
hfds dom,properties

CLSS public com.sun.tools.xjc.reader.dtd.bindinfo.BIContent
fld protected final com.sun.tools.xjc.reader.dtd.bindinfo.BIElement parent
fld protected final org.w3c.dom.Element element
meth public final com.sun.codemodel.JClass getType()
meth public final com.sun.tools.xjc.generator.bean.field.FieldRenderer getRealization()
meth public final java.lang.String getPropertyName()
supr java.lang.Object
hfds opts

CLSS public abstract interface com.sun.tools.xjc.reader.dtd.bindinfo.BIConversion
meth public abstract com.sun.tools.xjc.model.TypeUse getTransducer()
meth public abstract java.lang.String name()

CLSS public final com.sun.tools.xjc.reader.dtd.bindinfo.BIElement
fld public final com.sun.tools.xjc.model.CClassInfo clazz
meth public boolean isClass()
meth public boolean isRoot()
meth public com.sun.tools.xjc.reader.dtd.bindinfo.BIAttribute attribute(java.lang.String)
meth public com.sun.tools.xjc.reader.dtd.bindinfo.BIContent getRest()
meth public com.sun.tools.xjc.reader.dtd.bindinfo.BIConversion conversion(java.lang.String)
meth public com.sun.tools.xjc.reader.dtd.bindinfo.BIConversion getConversion()
meth public java.lang.String getClassName()
meth public java.lang.String name()
meth public java.util.List<com.sun.tools.xjc.reader.dtd.bindinfo.BIContent> getContents()
meth public org.xml.sax.Locator getLocation()
meth public org.xml.sax.Locator getSourceLocation()
meth public void declareConstructors(com.sun.tools.xjc.model.CClassInfo)
supr java.lang.Object
hfds attributes,className,constructors,contents,conversions,e,parent,rest

CLSS public final com.sun.tools.xjc.reader.dtd.bindinfo.BIEnumeration
intf com.sun.tools.xjc.reader.dtd.bindinfo.BIConversion
meth public com.sun.tools.xjc.model.TypeUse getTransducer()
meth public java.lang.String name()
supr java.lang.Object
hfds e,xducer

CLSS public final com.sun.tools.xjc.reader.dtd.bindinfo.BIInterface
meth public java.lang.String name()
meth public java.lang.String[] fields()
meth public java.lang.String[] members()
meth public org.xml.sax.Locator getSourceLocation()
supr java.lang.Object
hfds dom,fields,members,name

CLSS public com.sun.tools.xjc.reader.dtd.bindinfo.BIUserConversion
intf com.sun.tools.xjc.reader.dtd.bindinfo.BIConversion
meth public com.sun.tools.xjc.model.TypeUse getTransducer()
meth public java.lang.String name()
meth public org.xml.sax.Locator getSourceLocation()
supr java.lang.Object
hfds e,owner

CLSS public com.sun.tools.xjc.reader.dtd.bindinfo.BindInfo
cons public init(com.sun.tools.xjc.model.Model,org.w3c.dom.Document,com.sun.tools.xjc.ErrorReceiver)
cons public init(com.sun.tools.xjc.model.Model,org.xml.sax.InputSource,com.sun.tools.xjc.ErrorReceiver)
fld protected final com.sun.tools.xjc.ErrorReceiver errorReceiver
meth public com.sun.codemodel.JClass getSuperClass()
meth public com.sun.codemodel.JClass getSuperInterface()
meth public com.sun.codemodel.JPackage getTargetPackage()
meth public com.sun.tools.xjc.reader.dtd.bindinfo.BIConversion conversion(java.lang.String)
meth public com.sun.tools.xjc.reader.dtd.bindinfo.BIElement element(java.lang.String)
meth public java.lang.Long getSerialVersionUID()
meth public java.util.Collection<com.sun.tools.xjc.reader.dtd.bindinfo.BIElement> elements()
meth public java.util.Collection<com.sun.tools.xjc.reader.dtd.bindinfo.BIInterface> interfaces()
supr java.lang.Object
hfds XJC_NS,bindingFileSchema,classFactory,codeModel,conversions,defaultPackage,dom,elements,interfaces,model

CLSS public final com.sun.tools.xjc.reader.dtd.bindinfo.DOMUtil
cons public init()
meth public static java.lang.String getAttribute(org.w3c.dom.Element,java.lang.String,java.lang.String)
meth public static java.util.List<org.w3c.dom.Element> getChildElements(org.w3c.dom.Element)
meth public static java.util.List<org.w3c.dom.Element> getChildElements(org.w3c.dom.Element,java.lang.String)
meth public static org.w3c.dom.Element getElement(org.w3c.dom.Element,java.lang.String)
meth public static org.w3c.dom.Element getElement(org.w3c.dom.Element,java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public abstract com.sun.tools.xjc.reader.internalizer.AbstractReferenceFinderImpl
cons protected init(com.sun.tools.xjc.reader.internalizer.DOMForest)
fld protected final com.sun.tools.xjc.reader.internalizer.DOMForest parent
meth protected abstract java.lang.String findExternalResource(java.lang.String,java.lang.String,org.xml.sax.Attributes)
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.XMLFilterImpl
hfds locator

CLSS public final com.sun.tools.xjc.reader.internalizer.DOMForest
cons public init(com.sun.tools.xjc.reader.internalizer.InternalizationLogic,com.sun.tools.xjc.Options)
cons public init(javax.xml.parsers.SAXParserFactory,javax.xml.parsers.DocumentBuilder,com.sun.tools.xjc.reader.internalizer.InternalizationLogic)
fld protected final com.sun.tools.xjc.reader.internalizer.InternalizationLogic logic
fld public final com.sun.tools.xjc.reader.internalizer.LocatorTable locatorTable
fld public final java.util.Set<org.w3c.dom.Element> outerMostBindings
innr public abstract interface static Handler
meth public boolean checkSchemaCorrectness(com.sun.tools.xjc.ErrorReceiver)
meth public com.sun.tools.xjc.ErrorReceiver getErrorHandler()
meth public com.sun.tools.xjc.reader.internalizer.DOMForest$Handler getParserHandler(java.lang.String,boolean)
meth public com.sun.tools.xjc.reader.internalizer.SCDBasedBindingSet transform(boolean)
meth public com.sun.xml.xsom.parser.XMLParser createParser()
meth public java.lang.String getSystemId(org.w3c.dom.Document)
meth public java.lang.String[] listSystemIDs()
meth public java.util.Set<java.lang.String> getRootDocuments()
meth public javax.xml.transform.sax.SAXSource createSAXSource(java.lang.String)
 anno 0 com.sun.istack.NotNull()
meth public org.w3c.dom.Document get(java.lang.String)
meth public org.w3c.dom.Document getOneDocument()
meth public org.w3c.dom.Document parse(java.lang.String,boolean) throws java.io.IOException,org.xml.sax.SAXException
meth public org.w3c.dom.Document parse(java.lang.String,javax.xml.stream.XMLStreamReader,boolean) throws javax.xml.stream.XMLStreamException
meth public org.w3c.dom.Document parse(java.lang.String,org.xml.sax.InputSource,boolean) throws org.xml.sax.SAXException
meth public org.w3c.dom.Document parse(org.xml.sax.InputSource,boolean) throws org.xml.sax.SAXException
meth public org.w3c.dom.Document[] listDocuments()
meth public org.xml.sax.EntityResolver getEntityResolver()
meth public void dump(java.io.OutputStream) throws java.io.IOException
meth public void setEntityResolver(org.xml.sax.EntityResolver)
meth public void setErrorHandler(com.sun.tools.xjc.ErrorReceiver)
meth public void weakSchemaCorrectnessCheck(javax.xml.validation.SchemaFactory)
supr java.lang.Object
hfds core,documentBuilder,entityResolver,errorReceiver,options,parserFactory,rootDocuments
hcls HandlerImpl

CLSS public abstract interface static com.sun.tools.xjc.reader.internalizer.DOMForest$Handler
 outer com.sun.tools.xjc.reader.internalizer.DOMForest
intf org.xml.sax.ContentHandler
meth public abstract org.w3c.dom.Document getDocument()

CLSS public com.sun.tools.xjc.reader.internalizer.DOMForestScanner
cons public init(com.sun.tools.xjc.reader.internalizer.DOMForest)
meth public void scan(org.w3c.dom.Document,org.xml.sax.ContentHandler) throws org.xml.sax.SAXException
meth public void scan(org.w3c.dom.Element,org.xml.sax.ContentHandler) throws org.xml.sax.SAXException
supr java.lang.Object
hfds forest
hcls LocationResolver

CLSS public abstract interface com.sun.tools.xjc.reader.internalizer.InternalizationLogic
meth public abstract boolean checkIfValidTargetNode(com.sun.tools.xjc.reader.internalizer.DOMForest,org.w3c.dom.Element,org.w3c.dom.Element)
meth public abstract org.w3c.dom.Element refineTarget(org.w3c.dom.Element)
meth public abstract org.xml.sax.helpers.XMLFilterImpl createExternalReferenceFinder(com.sun.tools.xjc.reader.internalizer.DOMForest)

CLSS public final com.sun.tools.xjc.reader.internalizer.LocatorTable
cons public init()
meth public org.xml.sax.Locator getEndLocation(org.w3c.dom.Element)
meth public org.xml.sax.Locator getStartLocation(org.w3c.dom.Element)
meth public void storeEndLocation(org.w3c.dom.Element,org.xml.sax.Locator)
meth public void storeStartLocation(org.w3c.dom.Element,org.xml.sax.Locator)
supr java.lang.Object
hfds endLocations,startLocations

CLSS public final com.sun.tools.xjc.reader.internalizer.SCDBasedBindingSet
meth public void apply(com.sun.xml.xsom.XSSchemaSet,com.sun.tools.xjc.ErrorReceiver)
supr java.lang.Object
hfds errorReceiver,forest,loader,topLevel,unmarshaller
hcls Target

CLSS public com.sun.tools.xjc.reader.internalizer.VersionChecker
cons public init(org.xml.sax.ContentHandler,org.xml.sax.ErrorHandler,org.xml.sax.EntityResolver)
cons public init(org.xml.sax.XMLReader)
meth public void endDocument() throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.XMLFilterImpl
hfds VERSIONS,locator,rootTagStart,seenBindings,seenRoot,version

CLSS public final com.sun.tools.xjc.reader.relaxng.RELAXNGCompiler
cons public init(com.sun.tools.rngom.digested.DPattern,com.sun.codemodel.JCodeModel,com.sun.tools.xjc.Options)
meth public static com.sun.tools.xjc.model.Model build(com.sun.tools.rngom.digested.DPattern,com.sun.codemodel.JCodeModel,com.sun.tools.xjc.Options)
supr java.lang.Object
hfds bindQueue,classes,datatypes,defs,grammar,model,opts,pkg,typeUseBinder

CLSS public com.sun.tools.xjc.reader.relaxng.RELAXNGInternalizationLogic
cons public init()
intf com.sun.tools.xjc.reader.internalizer.InternalizationLogic
meth public boolean checkIfValidTargetNode(com.sun.tools.xjc.reader.internalizer.DOMForest,org.w3c.dom.Element,org.w3c.dom.Element)
meth public org.w3c.dom.Element refineTarget(org.w3c.dom.Element)
meth public org.xml.sax.helpers.XMLFilterImpl createExternalReferenceFinder(com.sun.tools.xjc.reader.internalizer.DOMForest)
supr java.lang.Object
hcls ReferenceFinder

CLSS public final com.sun.tools.xjc.reader.relaxng.RawTypeSetBuilder
cons public init(com.sun.tools.xjc.reader.relaxng.RELAXNGCompiler,com.sun.tools.xjc.model.Multiplicity)
meth public java.lang.Void onAttribute(com.sun.tools.rngom.digested.DAttributePattern)
meth public java.lang.Void onElement(com.sun.tools.rngom.digested.DElementPattern)
meth public java.lang.Void onOneOrMore(com.sun.tools.rngom.digested.DOneOrMorePattern)
meth public java.lang.Void onZeroOrMore(com.sun.tools.rngom.digested.DZeroOrMorePattern)
meth public static com.sun.tools.xjc.reader.RawTypeSet build(com.sun.tools.xjc.reader.relaxng.RELAXNGCompiler,com.sun.tools.rngom.digested.DPattern,com.sun.tools.xjc.model.Multiplicity)
supr com.sun.tools.rngom.digested.DPatternWalker
hfds compiler,mul,refs
hcls CClassInfoRef

CLSS public com.sun.tools.xjc.reader.xmlschema.BGMBuilder
cons protected init(java.lang.String,java.lang.String,boolean,com.sun.tools.xjc.generator.bean.field.FieldRendererFactory,java.util.List<com.sun.tools.xjc.Plugin>)
fld public final boolean inExtensionMode
fld public final com.sun.tools.xjc.generator.bean.field.FieldRendererFactory fieldRendererFactory
fld public final com.sun.tools.xjc.model.Model model
fld public final java.lang.String defaultPackage1
fld public final java.lang.String defaultPackage2
meth protected final com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDom getLocalDomCustomization(com.sun.xml.xsom.XSParticle)
meth public boolean isGenerateMixedExtensions()
meth public com.sun.tools.xjc.reader.xmlschema.ParticleBinder getParticleBinder()
 anno 0 com.sun.istack.NotNull()
meth public com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding getGlobalBinding()
 anno 0 com.sun.istack.NotNull()
meth public com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo getBindInfo(com.sun.xml.xsom.XSComponent)
meth public com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo getOrCreateBindInfo(com.sun.xml.xsom.XSComponent)
meth public com.sun.xml.bind.api.impl.NameConverter getNameConverter()
meth public java.lang.String deriveName(java.lang.String,com.sun.xml.xsom.XSComponent)
meth public java.util.Set<com.sun.xml.xsom.XSComponent> getReferer(com.sun.xml.xsom.XSType)
meth public javax.xml.transform.Transformer getIdentityTransformer()
meth public static com.sun.tools.xjc.model.Model build(com.sun.xml.xsom.XSSchemaSet,com.sun.codemodel.JCodeModel,com.sun.tools.xjc.ErrorReceiver,com.sun.tools.xjc.Options)
meth public static javax.xml.namespace.QName getName(com.sun.xml.xsom.XSDeclaration)
meth public void ying(com.sun.xml.xsom.XSComponent,com.sun.xml.xsom.XSComponent)
 anno 2 com.sun.istack.Nullable()
supr com.sun.tools.xjc.reader.xmlschema.BindingComponent
hfds activePlugins,emptyBindInfo,externalBindInfos,globalBinding,green,identityTransformer,particleBinder,purple,refFinder,toPurple

CLSS public final com.sun.tools.xjc.reader.xmlschema.BindGreen
cons public init()
fld protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder builder
fld protected final com.sun.tools.xjc.reader.xmlschema.ClassSelector selector
intf com.sun.xml.xsom.visitor.XSVisitor
meth protected final com.sun.tools.xjc.model.CClassInfo getCurrentBean()
meth protected final com.sun.xml.xsom.XSComponent getCurrentRoot()
meth protected final void createSimpleTypeProperty(com.sun.xml.xsom.XSSimpleType,java.lang.String)
meth public final void annotation(com.sun.xml.xsom.XSAnnotation)
meth public final void facet(com.sun.xml.xsom.XSFacet)
meth public final void identityConstraint(com.sun.xml.xsom.XSIdentityConstraint)
meth public final void notation(com.sun.xml.xsom.XSNotation)
meth public final void schema(com.sun.xml.xsom.XSSchema)
meth public final void xpath(com.sun.xml.xsom.XSXPath)
meth public void attContainer(com.sun.xml.xsom.XSAttContainer)
meth public void attGroupDecl(com.sun.xml.xsom.XSAttGroupDecl)
meth public void attributeDecl(com.sun.xml.xsom.XSAttributeDecl)
meth public void attributeUse(com.sun.xml.xsom.XSAttributeUse)
meth public void complexType(com.sun.xml.xsom.XSComplexType)
meth public void elementDecl(com.sun.xml.xsom.XSElementDecl)
meth public void empty(com.sun.xml.xsom.XSContentType)
meth public void modelGroup(com.sun.xml.xsom.XSModelGroup)
meth public void modelGroupDecl(com.sun.xml.xsom.XSModelGroupDecl)
meth public void particle(com.sun.xml.xsom.XSParticle)
meth public void simpleType(com.sun.xml.xsom.XSSimpleType)
meth public void wildcard(com.sun.xml.xsom.XSWildcard)
supr com.sun.tools.xjc.reader.xmlschema.BindingComponent
hfds ctBuilder

CLSS public com.sun.tools.xjc.reader.xmlschema.BindPurple
cons public init()
fld protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder builder
fld protected final com.sun.tools.xjc.reader.xmlschema.ClassSelector selector
intf com.sun.xml.xsom.visitor.XSVisitor
meth protected final com.sun.tools.xjc.model.CClassInfo getCurrentBean()
meth protected final com.sun.xml.xsom.XSComponent getCurrentRoot()
meth protected final void createSimpleTypeProperty(com.sun.xml.xsom.XSSimpleType,java.lang.String)
meth public final void annotation(com.sun.xml.xsom.XSAnnotation)
meth public final void facet(com.sun.xml.xsom.XSFacet)
meth public final void identityConstraint(com.sun.xml.xsom.XSIdentityConstraint)
meth public final void notation(com.sun.xml.xsom.XSNotation)
meth public final void schema(com.sun.xml.xsom.XSSchema)
meth public final void xpath(com.sun.xml.xsom.XSXPath)
meth public void attGroupDecl(com.sun.xml.xsom.XSAttGroupDecl)
meth public void attributeDecl(com.sun.xml.xsom.XSAttributeDecl)
meth public void attributeUse(com.sun.xml.xsom.XSAttributeUse)
meth public void complexType(com.sun.xml.xsom.XSComplexType)
meth public void elementDecl(com.sun.xml.xsom.XSElementDecl)
meth public void empty(com.sun.xml.xsom.XSContentType)
meth public void modelGroup(com.sun.xml.xsom.XSModelGroup)
meth public void modelGroupDecl(com.sun.xml.xsom.XSModelGroupDecl)
meth public void particle(com.sun.xml.xsom.XSParticle)
meth public void simpleType(com.sun.xml.xsom.XSSimpleType)
meth public void wildcard(com.sun.xml.xsom.XSWildcard)
supr com.sun.tools.xjc.reader.xmlschema.BindingComponent

CLSS public final com.sun.tools.xjc.reader.xmlschema.BindRed
cons public init()
fld protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder builder
fld protected final com.sun.tools.xjc.reader.xmlschema.ClassSelector selector
intf com.sun.xml.xsom.visitor.XSVisitor
meth protected final com.sun.tools.xjc.model.CClassInfo getCurrentBean()
meth protected final com.sun.xml.xsom.XSComponent getCurrentRoot()
meth protected final void createSimpleTypeProperty(com.sun.xml.xsom.XSSimpleType,java.lang.String)
meth public final void annotation(com.sun.xml.xsom.XSAnnotation)
meth public final void facet(com.sun.xml.xsom.XSFacet)
meth public final void identityConstraint(com.sun.xml.xsom.XSIdentityConstraint)
meth public final void notation(com.sun.xml.xsom.XSNotation)
meth public final void schema(com.sun.xml.xsom.XSSchema)
meth public final void xpath(com.sun.xml.xsom.XSXPath)
meth public void attGroupDecl(com.sun.xml.xsom.XSAttGroupDecl)
meth public void attributeDecl(com.sun.xml.xsom.XSAttributeDecl)
meth public void attributeUse(com.sun.xml.xsom.XSAttributeUse)
meth public void complexType(com.sun.xml.xsom.XSComplexType)
meth public void elementDecl(com.sun.xml.xsom.XSElementDecl)
meth public void empty(com.sun.xml.xsom.XSContentType)
meth public void modelGroup(com.sun.xml.xsom.XSModelGroup)
meth public void modelGroupDecl(com.sun.xml.xsom.XSModelGroupDecl)
meth public void particle(com.sun.xml.xsom.XSParticle)
meth public void simpleType(com.sun.xml.xsom.XSSimpleType)
meth public void wildcard(com.sun.xml.xsom.XSWildcard)
supr com.sun.tools.xjc.reader.xmlschema.BindingComponent
hfds ctBuilder

CLSS public final com.sun.tools.xjc.reader.xmlschema.BindYellow
cons public init()
fld protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder builder
fld protected final com.sun.tools.xjc.reader.xmlschema.ClassSelector selector
intf com.sun.xml.xsom.visitor.XSVisitor
meth protected final com.sun.tools.xjc.model.CClassInfo getCurrentBean()
meth protected final com.sun.xml.xsom.XSComponent getCurrentRoot()
meth protected final void createSimpleTypeProperty(com.sun.xml.xsom.XSSimpleType,java.lang.String)
meth public final void annotation(com.sun.xml.xsom.XSAnnotation)
meth public final void facet(com.sun.xml.xsom.XSFacet)
meth public final void identityConstraint(com.sun.xml.xsom.XSIdentityConstraint)
meth public final void notation(com.sun.xml.xsom.XSNotation)
meth public final void schema(com.sun.xml.xsom.XSSchema)
meth public final void xpath(com.sun.xml.xsom.XSXPath)
meth public void attGroupDecl(com.sun.xml.xsom.XSAttGroupDecl)
meth public void attributeDecl(com.sun.xml.xsom.XSAttributeDecl)
meth public void attributeUse(com.sun.xml.xsom.XSAttributeUse)
meth public void complexType(com.sun.xml.xsom.XSComplexType)
meth public void elementDecl(com.sun.xml.xsom.XSElementDecl)
meth public void empty(com.sun.xml.xsom.XSContentType)
meth public void modelGroup(com.sun.xml.xsom.XSModelGroup)
meth public void modelGroupDecl(com.sun.xml.xsom.XSModelGroupDecl)
meth public void particle(com.sun.xml.xsom.XSParticle)
meth public void simpleType(com.sun.xml.xsom.XSSimpleType)
meth public void wildcard(com.sun.xml.xsom.XSWildcard)
supr com.sun.tools.xjc.reader.xmlschema.BindingComponent

CLSS public abstract com.sun.tools.xjc.reader.xmlschema.BindingComponent
cons protected init()
meth protected final com.sun.tools.xjc.reader.xmlschema.ClassSelector getClassSelector()
meth protected final com.sun.tools.xjc.reader.xmlschema.ErrorReporter getErrorReporter()
supr java.lang.Object

CLSS public final com.sun.tools.xjc.reader.xmlschema.ClassSelector
cons public init()
meth public com.sun.codemodel.JPackage getPackage(java.lang.String)
meth public com.sun.tools.xjc.model.CClass bindToType(com.sun.xml.xsom.XSComplexType,com.sun.xml.xsom.XSComponent,boolean)
meth public com.sun.tools.xjc.model.CClassInfo getCurrentBean()
meth public com.sun.tools.xjc.model.CElement bindToType(com.sun.xml.xsom.XSElementDecl,com.sun.xml.xsom.XSComponent)
meth public com.sun.tools.xjc.model.CTypeInfo bindToType(com.sun.xml.xsom.XSComponent,com.sun.xml.xsom.XSComponent)
meth public com.sun.tools.xjc.model.TypeUse bindToType(com.sun.xml.xsom.XSType,com.sun.xml.xsom.XSComponent)
meth public com.sun.xml.xsom.XSComponent getCurrentRoot()
meth public final com.sun.tools.xjc.model.CClassInfoParent getClassScope()
meth public final com.sun.tools.xjc.model.CElement isBound(com.sun.xml.xsom.XSElementDecl,com.sun.xml.xsom.XSComponent)
meth public final void popClassScope()
meth public final void pushClassScope(com.sun.tools.xjc.model.CClassInfoParent)
meth public void executeTasks()
meth public void queueBuild(com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.model.CElement)
supr com.sun.tools.xjc.reader.xmlschema.BindingComponent
hfds bindMap,bindQueue,boundElements,builder,built,checkedPackageNames,classBinder,classScopes,currentBean,currentRoot,reservedClassNames
hcls Binding

CLSS public final com.sun.tools.xjc.reader.xmlschema.ErrorReporter
cons public init()
supr com.sun.tools.xjc.reader.xmlschema.BindingComponent
hfds errorReceiver

CLSS public final com.sun.tools.xjc.reader.xmlschema.ExpressionBuilder
intf com.sun.xml.xsom.visitor.XSTermFunction<com.sun.tools.xjc.reader.gbind.Expression>
meth public com.sun.tools.xjc.reader.gbind.Element elementDecl(com.sun.xml.xsom.XSElementDecl)
meth public com.sun.tools.xjc.reader.gbind.Expression modelGroup(com.sun.xml.xsom.XSModelGroup)
meth public com.sun.tools.xjc.reader.gbind.Expression modelGroupDecl(com.sun.xml.xsom.XSModelGroupDecl)
meth public com.sun.tools.xjc.reader.gbind.Expression particle(com.sun.xml.xsom.XSParticle)
meth public com.sun.tools.xjc.reader.gbind.Expression wildcard(com.sun.xml.xsom.XSWildcard)
meth public static com.sun.tools.xjc.reader.gbind.Expression createTree(com.sun.xml.xsom.XSParticle)
supr java.lang.Object
hfds current,decls,wildcard

CLSS public com.sun.tools.xjc.reader.xmlschema.Messages
cons public init()
fld public final static java.lang.String ERR_MULTIPLE_GLOBAL_BINDINGS = "ERR_MULTIPLE_GLOBAL_BINDINGS"
fld public final static java.lang.String ERR_MULTIPLE_GLOBAL_BINDINGS_OTHER = "ERR_MULTIPLE_GLOBAL_BINDINGS_OTHER"
fld public final static java.lang.String ERR_REFERENCE_TO_NONEXPORTED_CLASS = "ERR_REFERENCE_TO_NONEXPORTED_CLASS"
fld public final static java.lang.String ERR_REFERENCE_TO_NONEXPORTED_CLASS_MAP_FALSE = "ERR_REFERENCE_TO_NONEXPORTED_CLASS_MAP_FALSE"
fld public final static java.lang.String ERR_REFERENCE_TO_NONEXPORTED_CLASS_REFERER = "ERR_REFERENCE_TO_NONEXPORTED_CLASS_REFERER"
fld public final static java.lang.String ERR_UNACKNOWLEDGED_CUSTOMIZATION = "UnusedCustomizationChecker.UnacknolwedgedCustomization"
fld public final static java.lang.String ERR_UNACKNOWLEDGED_CUSTOMIZATION_LOCATION = "UnusedCustomizationChecker.UnacknolwedgedCustomization.Relevant"
fld public final static java.lang.String WARN_UNUSED_EXPECTED_CONTENT_TYPES = "UnusedCustomizationChecker.WarnUnusedExpectedContentTypes"
meth public !varargs static java.lang.String format(java.lang.String,java.lang.Object[])
supr java.lang.Object
hfds ERR_CANNOT_BE_TYPE_SAFE_ENUM,ERR_CANNOT_BE_TYPE_SAFE_ENUM_LOCATION,ERR_CANNOT_GENERATE_ENUM_NAME,ERR_CLASS_NAME_IS_REQUIRED,ERR_CONFLICT_BETWEEN_USERTYPE_AND_ACTUALTYPE_ATTUSE,ERR_CONFLICT_BETWEEN_USERTYPE_AND_ACTUALTYPE_ATTUSE_SOURCE,ERR_DATATYPE_ERROR,ERR_ENUM_MEMBER_NAME_COLLISION,ERR_ENUM_MEMBER_NAME_COLLISION_RELATED,ERR_ILLEGAL_EXPECTED_MIME_TYPE,ERR_INCORRECT_CLASS_NAME,ERR_INCORRECT_DEFAULT_VALUE,ERR_INCORRECT_FIXED_VALUE,ERR_INCORRECT_PACKAGE_NAME,ERR_MULTIPLE_SCHEMA_BINDINGS,ERR_MULTIPLE_SCHEMA_BINDINGS_LOCATION,ERR_NO_ENUM_FACET,ERR_NO_ENUM_NAME_AVAILABLE,ERR_RESERVED_CLASS_NAME,ERR_UNABLE_TO_GENERATE_NAME_FROM_MODELGROUP,ERR_UNNESTED_JAVATYPE_CUSTOMIZATION_ON_SIMPLETYPE,JAVADOC_HEADING,JAVADOC_LINE_UNKNOWN,JAVADOC_NIL_PROPERTY,JAVADOC_VALUEOBJECT_PROPERTY,MSG_COLLISION_INFO,MSG_FALLBACK_JAVADOC,MSG_LINE_X_OF_Y,MSG_UNKNOWN_FILE,WARN_DEFAULT_VALUE_PRIMITIVE_TYPE,WARN_ENUM_MEMBER_SIZE_CAP,WARN_NO_GLOBAL_ELEMENT

CLSS public final com.sun.tools.xjc.reader.xmlschema.MultiplicityCounter
fld public final static com.sun.tools.xjc.reader.xmlschema.MultiplicityCounter theInstance
intf com.sun.xml.xsom.visitor.XSTermFunction<com.sun.tools.xjc.model.Multiplicity>
meth public com.sun.tools.xjc.model.Multiplicity elementDecl(com.sun.xml.xsom.XSElementDecl)
meth public com.sun.tools.xjc.model.Multiplicity modelGroup(com.sun.xml.xsom.XSModelGroup)
meth public com.sun.tools.xjc.model.Multiplicity modelGroupDecl(com.sun.xml.xsom.XSModelGroupDecl)
meth public com.sun.tools.xjc.model.Multiplicity particle(com.sun.xml.xsom.XSParticle)
meth public com.sun.tools.xjc.model.Multiplicity wildcard(com.sun.xml.xsom.XSWildcard)
supr java.lang.Object

CLSS public abstract com.sun.tools.xjc.reader.xmlschema.ParticleBinder
cons protected init()
fld protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder builder
meth protected final <%0 extends com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration> {%%0} getLocalCustomization(com.sun.xml.xsom.XSParticle,java.lang.Class<{%%0}>)
meth protected final com.sun.tools.xjc.model.CClassInfo getCurrentBean()
meth protected final com.sun.tools.xjc.reader.xmlschema.ClassSelector getClassSelector()
meth protected final com.sun.tools.xjc.reader.xmlschema.ErrorReporter getErrorReporter()
meth protected final com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty getLocalPropCustomization(com.sun.xml.xsom.XSParticle)
meth protected final java.lang.String computeLabel(com.sun.xml.xsom.XSParticle)
meth protected final java.lang.String getSpecDefaultName(com.sun.xml.xsom.XSModelGroup,boolean) throws java.text.ParseException
meth protected final java.lang.String makeJavaName(boolean,java.lang.String)
meth protected final java.lang.String makeJavaName(com.sun.xml.xsom.XSParticle,java.lang.String)
meth public abstract boolean checkFallback(com.sun.xml.xsom.XSParticle)
meth public abstract void build(com.sun.xml.xsom.XSParticle,java.util.Collection<com.sun.xml.xsom.XSParticle>)
meth public final void build(com.sun.xml.xsom.XSParticle)
supr java.lang.Object

CLSS public com.sun.tools.xjc.reader.xmlschema.RawTypeSetBuilder
cons public init()
fld protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder builder
innr public final CElementInfoRef
innr public final static CClassRef
innr public final static WildcardRef
innr public final static XmlTypeRef
intf com.sun.xml.xsom.visitor.XSTermVisitor
meth public java.util.Set<com.sun.tools.xjc.reader.RawTypeSet$Ref> getRefs()
meth public static com.sun.tools.xjc.reader.RawTypeSet build(com.sun.xml.xsom.XSParticle,boolean)
meth public void elementDecl(com.sun.xml.xsom.XSElementDecl)
meth public void modelGroup(com.sun.xml.xsom.XSModelGroup)
meth public void modelGroupDecl(com.sun.xml.xsom.XSModelGroupDecl)
meth public void wildcard(com.sun.xml.xsom.XSWildcard)
supr java.lang.Object
hfds elementNames,refs

CLSS public final static com.sun.tools.xjc.reader.xmlschema.RawTypeSetBuilder$CClassRef
 outer com.sun.tools.xjc.reader.xmlschema.RawTypeSetBuilder
fld public final com.sun.tools.xjc.model.CClass target
fld public final com.sun.xml.xsom.XSElementDecl decl
meth protected boolean isListOfValues()
meth protected com.sun.tools.xjc.model.CTypeRef toTypeRef(com.sun.tools.xjc.model.CElementPropertyInfo)
meth protected com.sun.tools.xjc.reader.RawTypeSet$Mode canBeType(com.sun.tools.xjc.reader.RawTypeSet)
meth protected com.sun.xml.bind.v2.model.core.ID id()
meth protected void toElementRef(com.sun.tools.xjc.model.CReferencePropertyInfo)
supr com.sun.tools.xjc.reader.RawTypeSet$Ref

CLSS public final com.sun.tools.xjc.reader.xmlschema.RawTypeSetBuilder$CElementInfoRef
 outer com.sun.tools.xjc.reader.xmlschema.RawTypeSetBuilder
fld public final com.sun.tools.xjc.model.CElementInfo target
fld public final com.sun.xml.xsom.XSElementDecl decl
meth protected boolean isListOfValues()
meth protected com.sun.tools.xjc.model.CTypeRef toTypeRef(com.sun.tools.xjc.model.CElementPropertyInfo)
meth protected com.sun.tools.xjc.reader.RawTypeSet$Mode canBeType(com.sun.tools.xjc.reader.RawTypeSet)
meth protected com.sun.xml.bind.v2.model.core.ID id()
meth protected javax.activation.MimeType getExpectedMimeType()
meth protected void toElementRef(com.sun.tools.xjc.model.CReferencePropertyInfo)
supr com.sun.tools.xjc.reader.RawTypeSet$Ref

CLSS public final static com.sun.tools.xjc.reader.xmlschema.RawTypeSetBuilder$WildcardRef
 outer com.sun.tools.xjc.reader.xmlschema.RawTypeSetBuilder
meth protected boolean isListOfValues()
meth protected com.sun.tools.xjc.model.CTypeRef toTypeRef(com.sun.tools.xjc.model.CElementPropertyInfo)
meth protected com.sun.tools.xjc.reader.RawTypeSet$Mode canBeType(com.sun.tools.xjc.reader.RawTypeSet)
meth protected com.sun.xml.bind.v2.model.core.ID id()
meth protected void toElementRef(com.sun.tools.xjc.model.CReferencePropertyInfo)
supr com.sun.tools.xjc.reader.RawTypeSet$Ref
hfds mode

CLSS public final static com.sun.tools.xjc.reader.xmlschema.RawTypeSetBuilder$XmlTypeRef
 outer com.sun.tools.xjc.reader.xmlschema.RawTypeSetBuilder
cons public init(com.sun.xml.xsom.XSElementDecl)
meth protected boolean isListOfValues()
meth protected com.sun.tools.xjc.model.CTypeRef toTypeRef(com.sun.tools.xjc.model.CElementPropertyInfo)
meth protected com.sun.tools.xjc.reader.RawTypeSet$Mode canBeType(com.sun.tools.xjc.reader.RawTypeSet)
meth protected com.sun.xml.bind.v2.model.core.ID id()
meth protected javax.activation.MimeType getExpectedMimeType()
meth protected void toElementRef(com.sun.tools.xjc.model.CReferencePropertyInfo)
supr com.sun.tools.xjc.reader.RawTypeSet$Ref
hfds decl,target

CLSS public final com.sun.tools.xjc.reader.xmlschema.SimpleTypeBuilder
cons public init()
fld protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder builder
fld public final com.sun.xml.xsom.visitor.XSSimpleTypeFunction<com.sun.tools.xjc.model.TypeUse> composer
fld public final java.util.Stack<com.sun.xml.xsom.XSComponent> refererStack
fld public final static java.util.Map<java.lang.String,com.sun.tools.xjc.model.TypeUse> builtinConversions
meth public boolean isAcknowledgedXmimeContentTypes(com.sun.xml.xsom.XSComponent)
meth public com.sun.tools.xjc.model.TypeUse build(com.sun.xml.xsom.XSSimpleType)
meth public com.sun.tools.xjc.model.TypeUse buildDef(com.sun.xml.xsom.XSSimpleType)
meth public com.sun.xml.xsom.XSComponent getReferer()
meth public static boolean canBeMappedToTypeSafeEnum(com.sun.xml.xsom.XSSimpleType)
supr com.sun.tools.xjc.reader.xmlschema.BindingComponent
hfds INT_MAX,INT_MIN,LONG_MAX,LONG_MIN,acknowledgedXmimeContentTypes,builtinTypeSafeEnumCapableTypes,initiatingType,model,reportedEnumMemberSizeWarnings

CLSS public final com.sun.tools.xjc.reader.xmlschema.WildcardNameClassBuilder
intf com.sun.xml.xsom.visitor.XSWildcardFunction<com.sun.tools.rngom.nc.NameClass>
meth public com.sun.tools.rngom.nc.NameClass any(com.sun.xml.xsom.XSWildcard$Any)
meth public com.sun.tools.rngom.nc.NameClass other(com.sun.xml.xsom.XSWildcard$Other)
meth public com.sun.tools.rngom.nc.NameClass union(com.sun.xml.xsom.XSWildcard$Union)
meth public static com.sun.tools.rngom.nc.NameClass build(com.sun.xml.xsom.XSWildcard)
supr java.lang.Object
hfds theInstance

CLSS public com.sun.tools.xjc.reader.xmlschema.bindinfo.AnnotationParserFactoryImpl
cons public init(com.sun.tools.xjc.Options)
intf com.sun.xml.xsom.parser.AnnotationParserFactory
meth public com.sun.xml.xsom.parser.AnnotationParser create()
supr java.lang.Object
hfds options,validator
hcls ValidatorProtecter

CLSS public final com.sun.tools.xjc.reader.xmlschema.bindinfo.BIClass
cons protected init()
fld protected com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo parent
fld public final static javax.xml.namespace.QName NAME
intf com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration
meth protected final com.sun.codemodel.JCodeModel getCodeModel()
meth protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder getBuilder()
meth protected final com.sun.xml.xsom.XSComponent getOwner()
meth public final boolean isAcknowledged()
meth public java.lang.String getClassName()
 anno 0 com.sun.istack.Nullable()
meth public java.lang.String getExistingClassRef()
meth public java.lang.String getJavadoc()
meth public java.lang.String getRecursive()
meth public java.lang.String getUserSpecifiedImplClass()
meth public java.util.Collection<com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration> getChildren()
meth public javax.xml.namespace.QName getName()
meth public org.xml.sax.Locator getLocation()
meth public void markAsAcknowledged()
meth public void onSetOwner()
meth public void setParent(com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo)
supr java.lang.Object
hfds className,javadoc,recursive,ref,userSpecifiedImplClass

CLSS public abstract com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion
cons protected init()
cons public init(org.xml.sax.Locator)
 anno 0 java.lang.Deprecated()
fld protected com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo parent
fld public final static javax.xml.namespace.QName NAME
innr public final static Static
innr public static User
innr public static UserAdapter
intf com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration
meth protected final com.sun.codemodel.JCodeModel getCodeModel()
meth protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder getBuilder()
meth protected final com.sun.xml.xsom.XSComponent getOwner()
meth public abstract com.sun.tools.xjc.model.TypeUse getTypeUse(com.sun.xml.xsom.XSSimpleType)
meth public final boolean isAcknowledged()
meth public java.util.Collection<com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration> getChildren()
meth public javax.xml.namespace.QName getName()
meth public org.xml.sax.Locator getLocation()
meth public void markAsAcknowledged()
meth public void onSetOwner()
meth public void setParent(com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo)
supr java.lang.Object

CLSS public final static com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion$Static
 outer com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion
cons public init(org.xml.sax.Locator,com.sun.tools.xjc.model.TypeUse)
meth public com.sun.tools.xjc.model.TypeUse getTypeUse(com.sun.xml.xsom.XSSimpleType)
supr com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion
hfds transducer

CLSS public static com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion$User
 outer com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion
cons public init()
cons public init(org.xml.sax.Locator,java.lang.String,java.lang.String,com.sun.codemodel.JType)
fld public final static javax.xml.namespace.QName NAME
meth public com.sun.tools.xjc.model.TypeUse getTypeUse(com.sun.xml.xsom.XSSimpleType)
meth public javax.xml.namespace.QName getName()
supr com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion
hfds inMemoryType,knownBases,parseMethod,printMethod,type,typeUse

CLSS public static com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion$UserAdapter
 outer com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion
cons public init()
meth public com.sun.tools.xjc.model.TypeUse getTypeUse(com.sun.xml.xsom.XSSimpleType)
supr com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion
hfds adapter,type,typeUse

CLSS public abstract interface com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration
meth public abstract boolean isAcknowledged()
meth public abstract java.util.Collection<com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration> getChildren()
meth public abstract javax.xml.namespace.QName getName()
meth public abstract org.xml.sax.Locator getLocation()
meth public abstract void markAsAcknowledged()
meth public abstract void onSetOwner()
meth public abstract void setParent(com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo)

CLSS public com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDom
cons public init()
fld protected com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo parent
fld public final static javax.xml.namespace.QName NAME
intf com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration
meth protected final com.sun.codemodel.JCodeModel getCodeModel()
meth protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder getBuilder()
meth protected final com.sun.xml.xsom.XSComponent getOwner()
meth public final boolean isAcknowledged()
meth public final javax.xml.namespace.QName getName()
meth public java.util.Collection<com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration> getChildren()
meth public org.xml.sax.Locator getLocation()
meth public void markAsAcknowledged()
meth public void onSetOwner()
meth public void setParent(com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo)
supr java.lang.Object
hfds type

CLSS public final com.sun.tools.xjc.reader.xmlschema.bindinfo.BIEnum
cons public init()
fld protected com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo parent
fld public final java.lang.String javadoc
fld public final java.util.Map<java.lang.String,com.sun.tools.xjc.reader.xmlschema.bindinfo.BIEnumMember> members
fld public final static javax.xml.namespace.QName NAME
fld public java.lang.String className
fld public java.lang.String ref
intf com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration
meth protected final com.sun.codemodel.JCodeModel getCodeModel()
meth protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder getBuilder()
meth protected final com.sun.xml.xsom.XSComponent getOwner()
meth public boolean isMapped()
meth public final boolean isAcknowledged()
meth public java.util.Collection<com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration> getChildren()
meth public javax.xml.namespace.QName getName()
meth public org.xml.sax.Locator getLocation()
meth public void markAsAcknowledged()
meth public void onSetOwner()
meth public void setParent(com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo)
supr java.lang.Object
hfds map
hcls BIEnumMember2

CLSS public com.sun.tools.xjc.reader.xmlschema.bindinfo.BIEnumMember
cons protected init()
fld protected com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo parent
fld public final java.lang.String javadoc
fld public final java.lang.String name
fld public final static javax.xml.namespace.QName NAME
intf com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration
meth protected final com.sun.codemodel.JCodeModel getCodeModel()
meth protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder getBuilder()
meth protected final com.sun.xml.xsom.XSComponent getOwner()
meth public final boolean isAcknowledged()
meth public java.util.Collection<com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration> getChildren()
meth public javax.xml.namespace.QName getName()
meth public org.xml.sax.Locator getLocation()
meth public void markAsAcknowledged()
meth public void onSetOwner()
meth public void setParent(com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo)
supr java.lang.Object

CLSS public com.sun.tools.xjc.reader.xmlschema.bindinfo.BIFactoryMethod
cons public init()
fld protected com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo parent
fld public final static javax.xml.namespace.QName NAME
fld public java.lang.String name
intf com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration
meth protected final com.sun.codemodel.JCodeModel getCodeModel()
meth protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder getBuilder()
meth protected final com.sun.xml.xsom.XSComponent getOwner()
meth public final boolean isAcknowledged()
meth public final javax.xml.namespace.QName getName()
meth public java.util.Collection<com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration> getChildren()
meth public org.xml.sax.Locator getLocation()
meth public static void handle(com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.model.CPropertyInfo)
meth public void markAsAcknowledged()
meth public void onSetOwner()
meth public void setParent(com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo)
supr java.lang.Object

CLSS public final com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding
cons public init()
fld protected com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo parent
fld public com.sun.xml.bind.api.impl.NameConverter nameConverter
fld public final static javax.xml.namespace.QName NAME
intf com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration
meth protected final com.sun.codemodel.JCodeModel getCodeModel()
meth protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder getBuilder()
meth protected final com.sun.xml.xsom.XSComponent getOwner()
meth public boolean canBeMappedToTypeSafeEnum(com.sun.xml.xsom.XSDeclaration)
meth public boolean canBeMappedToTypeSafeEnum(java.lang.String,java.lang.String)
meth public boolean canBeMappedToTypeSafeEnum(javax.xml.namespace.QName)
meth public boolean isChoiceContentPropertyEnabled()
meth public boolean isEqual(com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding)
meth public boolean isGenerateElementClass()
meth public boolean isGenerateMixedExtensions()
meth public boolean isJavaNamingConventionEnabled()
meth public boolean isRestrictionFreshType()
meth public boolean isSimpleMode()
meth public boolean isSimpleTypeSubstitution()
meth public com.sun.codemodel.JDefinedClass getSuperClass()
meth public com.sun.codemodel.JDefinedClass getSuperInterface()
meth public com.sun.tools.xjc.generator.bean.ImplStructureStrategy getCodeGenerationStrategy()
meth public com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty getDefaultProperty()
meth public com.sun.tools.xjc.reader.xmlschema.bindinfo.BISerializable getSerializable()
meth public com.sun.tools.xjc.reader.xmlschema.bindinfo.EnumMemberMode getEnumMemberMode()
meth public com.sun.tools.xjc.reader.xmlschema.bindinfo.LocalScoping getFlattenClasses()
meth public final boolean isAcknowledged()
meth public int getDefaultEnumMemberSizeCap()
meth public java.util.Collection<com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration> getChildren()
meth public javax.xml.namespace.QName getName()
meth public org.xml.sax.Locator getLocation()
meth public void dispatchGlobalConversions(com.sun.xml.xsom.XSSchemaSet)
meth public void errorCheck()
meth public void markAsAcknowledged()
meth public void onSetOwner()
meth public void setParent(com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo)
supr java.lang.Object
hfds choiceContentProperty,codeGenerationStrategy,collectionType,defaultEnumMemberSizeCap,defaultProperty,enumBaseTypes,fixedAttributeAsConstantProperty,flattenClasses,generateElementClass,generateElementProperty,generateEnumMemberName,generateMixedExtensions,globalConversions,isJavaNamingConventionEnabled,noMarshaller,noUnmarshaller,noValidatingUnmarshaller,noValidator,optionalProperty,serializable,simpleMode,simpleTypeSubstitution,superClass,superInterface,treatRestrictionLikeNewType,typeSubstitution
hcls ClassNameAdapter,ClassNameBean,GlobalStandardConversion,GlobalVendorConversion,TypeSubstitutionElement,UnderscoreBinding

CLSS public com.sun.tools.xjc.reader.xmlschema.bindinfo.BIInlineBinaryData
cons public init()
fld protected com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo parent
fld public final static javax.xml.namespace.QName NAME
intf com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration
meth protected final com.sun.codemodel.JCodeModel getCodeModel()
meth protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder getBuilder()
meth protected final com.sun.xml.xsom.XSComponent getOwner()
meth public final boolean isAcknowledged()
meth public final javax.xml.namespace.QName getName()
meth public java.util.Collection<com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration> getChildren()
meth public org.xml.sax.Locator getLocation()
meth public static void handle(com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.model.CPropertyInfo)
meth public void markAsAcknowledged()
meth public void onSetOwner()
meth public void setParent(com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo)
supr java.lang.Object

CLSS public final com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty
cons protected init()
cons public init(org.xml.sax.Locator,java.lang.String,java.lang.String,com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty$BaseTypeBean,com.sun.tools.xjc.reader.xmlschema.bindinfo.CollectionTypeAttribute,java.lang.Boolean,com.sun.tools.xjc.reader.xmlschema.bindinfo.OptionalPropertyMode,java.lang.Boolean)
fld protected com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo parent
fld public final static javax.xml.namespace.QName NAME
intf com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration
meth protected com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty getDefault()
meth protected final com.sun.codemodel.JCodeModel getCodeModel()
meth protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder getBuilder()
meth protected final com.sun.xml.xsom.XSComponent getOwner()
meth public boolean isConstantProperty()
meth public com.sun.codemodel.JType getBaseType()
meth public com.sun.tools.xjc.model.CAttributePropertyInfo createAttributeProperty(com.sun.xml.xsom.XSAttributeUse,com.sun.tools.xjc.model.TypeUse)
meth public com.sun.tools.xjc.model.CElementPropertyInfo createElementProperty(java.lang.String,boolean,com.sun.xml.xsom.XSParticle,com.sun.tools.xjc.reader.RawTypeSet)
meth public com.sun.tools.xjc.model.CPropertyInfo createElementOrReferenceProperty(java.lang.String,boolean,com.sun.xml.xsom.XSParticle,com.sun.tools.xjc.reader.RawTypeSet)
meth public com.sun.tools.xjc.model.CReferencePropertyInfo createContentExtendedMixedReferenceProperty(java.lang.String,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.reader.RawTypeSet)
meth public com.sun.tools.xjc.model.CReferencePropertyInfo createDummyExtendedMixedReferenceProperty(java.lang.String,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.reader.RawTypeSet)
meth public com.sun.tools.xjc.model.CReferencePropertyInfo createReferenceProperty(java.lang.String,boolean,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.reader.RawTypeSet,boolean,boolean,boolean,boolean)
meth public com.sun.tools.xjc.model.CValuePropertyInfo createValueProperty(java.lang.String,boolean,com.sun.xml.xsom.XSComponent,com.sun.tools.xjc.model.TypeUse,javax.xml.namespace.QName)
meth public com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion getConv()
meth public com.sun.tools.xjc.reader.xmlschema.bindinfo.OptionalPropertyMode getOptionalPropertyMode()
meth public final boolean isAcknowledged()
meth public java.lang.String getJavadoc()
meth public java.lang.String getPropertyName(boolean)
meth public java.util.Collection<com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration> getChildren()
meth public javax.xml.namespace.QName getName()
meth public org.xml.sax.Locator getLocation()
meth public static com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty getCustomization(com.sun.xml.xsom.XSComponent)
meth public void markAsAcknowledged()
meth public void onSetOwner()
meth public void setParent(com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo)
supr java.lang.Object
hfds baseType,collectionType,defaultCustomizationFinder,generateElementProperty,generateFailFastSetterMethod,hasFixedValue,isConstantProperty,javadoc,name,optionalProperty
hcls BaseTypeBean

CLSS public final com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding
cons public init()
fld protected com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo parent
fld public boolean map
fld public final static javax.xml.namespace.QName NAME
innr public final static NamingRule
intf com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration
meth protected final com.sun.codemodel.JCodeModel getCodeModel()
meth protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder getBuilder()
meth protected final com.sun.xml.xsom.XSComponent getOwner()
meth public final boolean isAcknowledged()
meth public java.lang.String getJavadoc()
meth public java.lang.String getPackageName()
meth public java.lang.String mangleAnonymousTypeClassName(java.lang.String)
meth public java.lang.String mangleClassName(java.lang.String,com.sun.xml.xsom.XSComponent)
meth public java.util.Collection<com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration> getChildren()
meth public javax.xml.namespace.QName getName()
meth public org.xml.sax.Locator getLocation()
meth public void markAsAcknowledged()
meth public void onSetOwner()
meth public void setParent(com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo)
supr java.lang.Object
hfds defaultNamingRule,nameXmlTransform,packageInfo
hcls NameRules,PackageInfo

CLSS public final static com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding$NamingRule
 outer com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding
cons public init()
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String mangle(java.lang.String)
supr java.lang.Object
hfds prefix,suffix

CLSS public final com.sun.tools.xjc.reader.xmlschema.bindinfo.BISerializable
cons public init()
fld public java.lang.Long uid
supr java.lang.Object

CLSS public com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXDom
cons public init()
supr com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDom
hfds type

CLSS public final com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXPluginCustomization
cons public init(org.w3c.dom.Element,org.xml.sax.Locator)
fld protected com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo parent
fld public final org.w3c.dom.Element element
intf com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration
meth protected final com.sun.codemodel.JCodeModel getCodeModel()
meth protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder getBuilder()
meth protected final com.sun.xml.xsom.XSComponent getOwner()
meth public final boolean isAcknowledged()
meth public final javax.xml.namespace.QName getName()
meth public java.util.Collection<com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration> getChildren()
meth public org.xml.sax.Locator getLocation()
meth public void markAsAcknowledged()
meth public void onSetOwner()
meth public void setParent(com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo)
supr java.lang.Object
hfds name

CLSS public final com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXSubstitutable
cons public init()
fld protected com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo parent
fld public final static javax.xml.namespace.QName NAME
intf com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration
meth protected final com.sun.codemodel.JCodeModel getCodeModel()
meth protected final com.sun.tools.xjc.reader.xmlschema.BGMBuilder getBuilder()
meth protected final com.sun.xml.xsom.XSComponent getOwner()
meth public final boolean isAcknowledged()
meth public final javax.xml.namespace.QName getName()
meth public java.util.Collection<com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration> getChildren()
meth public org.xml.sax.Locator getLocation()
meth public void markAsAcknowledged()
meth public void onSetOwner()
meth public void setParent(com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo)
supr java.lang.Object

CLSS public final com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo
cons public init()
fld public final static com.sun.tools.xjc.SchemaCache bindingFileSchema
fld public final static com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo empty
intf java.lang.Iterable<com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration>
meth public <%0 extends com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration> {%%0} get(java.lang.Class<{%%0}>)
meth public boolean isPointless()
meth public com.sun.tools.xjc.model.CCustomizations toCustomizationList()
meth public com.sun.tools.xjc.reader.xmlschema.BGMBuilder getBuilder()
meth public com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration get(int)
meth public com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration[] getDecls()
meth public com.sun.xml.xsom.XSComponent getOwner()
meth public int size()
meth public java.lang.String getDocumentation()
meth public java.util.Iterator<com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration> iterator()
meth public org.xml.sax.Locator getSourceLocation()
meth public static javax.xml.bind.JAXBContext getCustomizationContext()
meth public static javax.xml.bind.Unmarshaller getCustomizationUnmarshaller()
meth public void absorb(com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo)
meth public void addDecl(com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration)
meth public void setOwner(com.sun.tools.xjc.reader.xmlschema.BGMBuilder,com.sun.xml.xsom.XSComponent)
supr java.lang.Object
hfds builder,customizationContext,decls,documentation,location,owner
hcls AppInfo,Documentation

CLSS public final !enum com.sun.tools.xjc.reader.xmlschema.bindinfo.EnumMemberMode
fld public final static com.sun.tools.xjc.reader.xmlschema.bindinfo.EnumMemberMode ERROR
fld public final static com.sun.tools.xjc.reader.xmlschema.bindinfo.EnumMemberMode GENERATE
fld public final static com.sun.tools.xjc.reader.xmlschema.bindinfo.EnumMemberMode SKIP
meth public com.sun.tools.xjc.reader.xmlschema.bindinfo.EnumMemberMode getModeWithEnum()
meth public static com.sun.tools.xjc.reader.xmlschema.bindinfo.EnumMemberMode valueOf(java.lang.String)
meth public static com.sun.tools.xjc.reader.xmlschema.bindinfo.EnumMemberMode[] values()
supr java.lang.Enum<com.sun.tools.xjc.reader.xmlschema.bindinfo.EnumMemberMode>

CLSS public com.sun.tools.xjc.reader.xmlschema.bindinfo.ForkingFilter
cons public init()
cons public init(org.xml.sax.ContentHandler)
meth public org.xml.sax.ContentHandler getSideHandler()
meth public org.xml.sax.Locator getDocumentLocator()
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startForking(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes,org.xml.sax.ContentHandler) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.XMLFilterImpl
hfds depth,loc,namespaces,side

CLSS public final !enum com.sun.tools.xjc.reader.xmlschema.bindinfo.LocalScoping
fld public final static com.sun.tools.xjc.reader.xmlschema.bindinfo.LocalScoping NESTED
fld public final static com.sun.tools.xjc.reader.xmlschema.bindinfo.LocalScoping TOPLEVEL
meth public static com.sun.tools.xjc.reader.xmlschema.bindinfo.LocalScoping valueOf(java.lang.String)
meth public static com.sun.tools.xjc.reader.xmlschema.bindinfo.LocalScoping[] values()
supr java.lang.Enum<com.sun.tools.xjc.reader.xmlschema.bindinfo.LocalScoping>

CLSS public final !enum com.sun.tools.xjc.reader.xmlschema.bindinfo.OptionalPropertyMode
fld public final static com.sun.tools.xjc.reader.xmlschema.bindinfo.OptionalPropertyMode ISSET
fld public final static com.sun.tools.xjc.reader.xmlschema.bindinfo.OptionalPropertyMode PRIMITIVE
fld public final static com.sun.tools.xjc.reader.xmlschema.bindinfo.OptionalPropertyMode WRAPPER
meth public static com.sun.tools.xjc.reader.xmlschema.bindinfo.OptionalPropertyMode valueOf(java.lang.String)
meth public static com.sun.tools.xjc.reader.xmlschema.bindinfo.OptionalPropertyMode[] values()
supr java.lang.Enum<com.sun.tools.xjc.reader.xmlschema.bindinfo.OptionalPropertyMode>

CLSS abstract interface com.sun.tools.xjc.reader.xmlschema.bindinfo.package-info

CLSS public final !enum com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode
fld public final static com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode FALLBACK_CONTENT
fld public final static com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode FALLBACK_EXTENSION
fld public final static com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode FALLBACK_REST
fld public final static com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode NORMAL
meth public static com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode valueOf(java.lang.String)
meth public static com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode[] values()
supr java.lang.Enum<com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode>

CLSS public final com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeFieldBuilder
cons public init()
meth protected com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode getBindingMode(com.sun.xml.xsom.XSComplexType)
meth public void build(com.sun.xml.xsom.XSComplexType)
meth public void recordBindingMode(com.sun.xml.xsom.XSComplexType,com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeBindingMode)
supr com.sun.tools.xjc.reader.xmlschema.BindingComponent
hfds complexTypeBindingModes,complexTypeBuilders

CLSS public com.sun.tools.xjc.reader.xmlschema.parser.CustomizationContextChecker
cons public init(org.xml.sax.ErrorHandler)
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.XMLFilterImpl
hfds elementNames,errorHandler,locator,prohibitedSchemaElementNames

CLSS public com.sun.tools.xjc.reader.xmlschema.parser.IncorrectNamespaceURIChecker
cons public init(org.xml.sax.ErrorHandler)
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.XMLFilterImpl
hfds errorHandler,isCustomizationUsed,isJAXBPrefixUsed,locator

CLSS public com.sun.tools.xjc.reader.xmlschema.parser.LSInputSAXWrapper
cons public init(org.xml.sax.InputSource)
intf org.w3c.dom.ls.LSInput
meth public boolean getCertifiedText()
meth public java.io.InputStream getByteStream()
meth public java.io.Reader getCharacterStream()
meth public java.lang.String getBaseURI()
meth public java.lang.String getEncoding()
meth public java.lang.String getPublicId()
meth public java.lang.String getStringData()
meth public java.lang.String getSystemId()
meth public void setBaseURI(java.lang.String)
meth public void setByteStream(java.io.InputStream)
meth public void setCertifiedText(boolean)
meth public void setCharacterStream(java.io.Reader)
meth public void setEncoding(java.lang.String)
meth public void setPublicId(java.lang.String)
meth public void setStringData(java.lang.String)
meth public void setSystemId(java.lang.String)
supr java.lang.Object
hfds core

CLSS public com.sun.tools.xjc.reader.xmlschema.parser.SchemaConstraintChecker
cons public init()
meth public static boolean check(org.xml.sax.InputSource[],com.sun.tools.xjc.ErrorReceiver,org.xml.sax.EntityResolver,boolean)
meth public static void main(java.lang.String[]) throws java.io.IOException
supr java.lang.Object

CLSS public com.sun.tools.xjc.reader.xmlschema.parser.XMLSchemaInternalizationLogic
cons public init()
intf com.sun.tools.xjc.reader.internalizer.InternalizationLogic
meth public boolean checkIfValidTargetNode(com.sun.tools.xjc.reader.internalizer.DOMForest,org.w3c.dom.Element,org.w3c.dom.Element)
meth public org.w3c.dom.Element refineTarget(org.w3c.dom.Element)
meth public org.xml.sax.helpers.XMLFilterImpl createExternalReferenceFinder(com.sun.tools.xjc.reader.internalizer.DOMForest)
supr java.lang.Object
hcls ReferenceFinder

CLSS public com.sun.tools.xjc.runtime.JAXBContextFactory
cons public init()
meth public static javax.xml.bind.JAXBContext createContext(java.lang.Class[],java.util.Map) throws javax.xml.bind.JAXBException
meth public static javax.xml.bind.JAXBContext createContext(java.lang.String,java.lang.ClassLoader,java.util.Map) throws javax.xml.bind.JAXBException
supr java.lang.Object
hfds DOT_OBJECT_FACTORY,IMPL_DOT_OBJECT_FACTORY

CLSS public com.sun.tools.xjc.runtime.ZeroOneBooleanAdapter
cons public init()
meth public java.lang.Boolean unmarshal(java.lang.String)
meth public java.lang.String marshal(java.lang.Boolean)
supr javax.xml.bind.annotation.adapters.XmlAdapter<java.lang.String,java.lang.Boolean>

CLSS public final com.sun.tools.xjc.util.CodeModelClassFactory
cons public init(com.sun.tools.xjc.ErrorReceiver)
meth public com.sun.codemodel.JDefinedClass createClass(com.sun.codemodel.JClassContainer,int,java.lang.String,org.xml.sax.Locator)
meth public com.sun.codemodel.JDefinedClass createClass(com.sun.codemodel.JClassContainer,int,java.lang.String,org.xml.sax.Locator,com.sun.codemodel.ClassType)
meth public com.sun.codemodel.JDefinedClass createClass(com.sun.codemodel.JClassContainer,java.lang.String,org.xml.sax.Locator)
meth public com.sun.codemodel.JDefinedClass createClass(com.sun.codemodel.JClassContainer,java.lang.String,org.xml.sax.Locator,com.sun.codemodel.ClassType)
meth public com.sun.codemodel.JDefinedClass createInterface(com.sun.codemodel.JClassContainer,int,java.lang.String,org.xml.sax.Locator)
meth public com.sun.codemodel.JDefinedClass createInterface(com.sun.codemodel.JClassContainer,java.lang.String,org.xml.sax.Locator)
supr java.lang.Object
hfds errorReceiver,ticketMaster

CLSS public com.sun.tools.xjc.util.DOMUtils
cons public init()
meth public static java.lang.String getElementText(org.w3c.dom.Element)
meth public static org.w3c.dom.Element getElement(org.w3c.dom.Document,java.lang.String)
meth public static org.w3c.dom.Element getElement(org.w3c.dom.Document,java.lang.String,java.lang.String)
meth public static org.w3c.dom.Element getElement(org.w3c.dom.Document,javax.xml.namespace.QName)
meth public static org.w3c.dom.Element getFirstChildElement(org.w3c.dom.Element,java.lang.String,java.lang.String)
meth public static org.w3c.dom.Element[] getChildElements(org.w3c.dom.Element)
meth public static org.w3c.dom.Element[] getChildElements(org.w3c.dom.Element,java.lang.String,java.lang.String)
meth public static org.w3c.dom.Element[] getElements(org.w3c.dom.NodeList)
supr java.lang.Object

CLSS public com.sun.tools.xjc.util.ErrorReceiverFilter
cons public init()
cons public init(com.sun.tools.xjc.api.ErrorListener)
meth public final boolean hadError()
meth public void error(org.xml.sax.SAXParseException)
meth public void fatalError(org.xml.sax.SAXParseException)
meth public void info(org.xml.sax.SAXParseException)
meth public void setErrorReceiver(com.sun.tools.xjc.api.ErrorListener)
meth public void warning(org.xml.sax.SAXParseException)
supr com.sun.tools.xjc.ErrorReceiver
hfds core,hadError

CLSS public com.sun.tools.xjc.util.ForkContentHandler
cons public init(org.xml.sax.ContentHandler,org.xml.sax.ContentHandler)
intf org.xml.sax.ContentHandler
meth public static org.xml.sax.ContentHandler create(org.xml.sax.ContentHandler[])
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr java.lang.Object
hfds lhs,rhs

CLSS public com.sun.tools.xjc.util.ForkEntityResolver
cons public init(org.xml.sax.EntityResolver,org.xml.sax.EntityResolver)
intf org.xml.sax.EntityResolver
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
supr java.lang.Object
hfds lhs,rhs

CLSS public com.sun.tools.xjc.util.MimeTypeRange
cons public init(java.lang.String) throws java.text.ParseException
fld public final float q
fld public final java.lang.String majorType
fld public final java.lang.String subType
fld public final java.util.Map<java.lang.String,java.lang.String> parameters
fld public final static com.sun.tools.xjc.util.MimeTypeRange ALL
meth public java.lang.String toString()
meth public javax.activation.MimeType toMimeType() throws javax.activation.MimeTypeParseException
meth public static com.sun.tools.xjc.util.MimeTypeRange merge(java.util.Collection<com.sun.tools.xjc.util.MimeTypeRange>)
meth public static java.util.List<com.sun.tools.xjc.util.MimeTypeRange> parseRanges(java.lang.String) throws java.text.ParseException
meth public static void main(java.lang.String[]) throws java.text.ParseException
supr java.lang.Object

CLSS public final com.sun.tools.xjc.util.NamespaceContextAdapter
cons public init(com.sun.xml.xsom.XmlString)
intf javax.xml.namespace.NamespaceContext
meth public java.lang.String getNamespaceURI(java.lang.String)
meth public java.lang.String getPrefix(java.lang.String)
meth public java.util.Iterator getPrefixes(java.lang.String)
supr java.lang.Object
hfds xstr

CLSS public com.sun.tools.xjc.util.NullStream
cons public init()
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream

CLSS public abstract com.sun.tools.xjc.util.ReadOnlyAdapter<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
meth public final {com.sun.tools.xjc.util.ReadOnlyAdapter%0} marshal({com.sun.tools.xjc.util.ReadOnlyAdapter%1})
supr javax.xml.bind.annotation.adapters.XmlAdapter<{com.sun.tools.xjc.util.ReadOnlyAdapter%0},{com.sun.tools.xjc.util.ReadOnlyAdapter%1}>

CLSS public final com.sun.tools.xjc.util.StringCutter
cons public init(java.lang.String,boolean)
meth public char peek()
meth public int length()
meth public java.lang.String next(java.lang.String) throws java.text.ParseException
meth public java.lang.String until(java.lang.String) throws java.text.ParseException
meth public void skip(java.lang.String) throws java.text.ParseException
supr java.lang.Object
hfds ignoreWhitespace,original,s

CLSS public abstract com.sun.tools.xjc.util.SubtreeCutter
cons public init()
meth public boolean isCutting()
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setContentHandler(org.xml.sax.ContentHandler)
meth public void startCutting()
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.XMLFilterImpl
hfds cutDepth,next,stub

CLSS public final com.sun.tools.xjc.util.Util
meth public static boolean equals(org.xml.sax.Locator,org.xml.sax.Locator)
meth public static java.lang.String getSystemProperty(java.lang.Class,java.lang.String)
meth public static java.lang.String getSystemProperty(java.lang.String)
supr java.lang.Object

CLSS public com.sun.tools.xjc.writer.SignatureWriter
meth public static void write(com.sun.tools.xjc.outline.Outline,java.io.Writer) throws java.io.IOException
supr java.lang.Object
hfds classSet,classes,indent,out

CLSS public abstract interface com.sun.xml.bind.AccessorFactory
meth public abstract com.sun.xml.bind.v2.runtime.reflect.Accessor createFieldAccessor(java.lang.Class,java.lang.reflect.Field,boolean) throws javax.xml.bind.JAXBException
meth public abstract com.sun.xml.bind.v2.runtime.reflect.Accessor createPropertyAccessor(java.lang.Class,java.lang.reflect.Method,java.lang.reflect.Method) throws javax.xml.bind.JAXBException

CLSS public com.sun.xml.bind.AccessorFactoryImpl
intf com.sun.xml.bind.InternalAccessorFactory
meth public com.sun.xml.bind.v2.runtime.reflect.Accessor createFieldAccessor(java.lang.Class,java.lang.reflect.Field,boolean)
meth public com.sun.xml.bind.v2.runtime.reflect.Accessor createFieldAccessor(java.lang.Class,java.lang.reflect.Field,boolean,boolean)
meth public com.sun.xml.bind.v2.runtime.reflect.Accessor createPropertyAccessor(java.lang.Class,java.lang.reflect.Method,java.lang.reflect.Method)
meth public static com.sun.xml.bind.AccessorFactoryImpl getInstance()
supr java.lang.Object
hfds instance

CLSS public final com.sun.xml.bind.AnyTypeAdapter
cons public init()
meth public java.lang.Object marshal(java.lang.Object)
meth public java.lang.Object unmarshal(java.lang.Object)
supr javax.xml.bind.annotation.adapters.XmlAdapter<java.lang.Object,java.lang.Object>

CLSS public abstract interface com.sun.xml.bind.CycleRecoverable
innr public abstract interface static Context
meth public abstract java.lang.Object onCycleDetected(com.sun.xml.bind.CycleRecoverable$Context)

CLSS public abstract interface static com.sun.xml.bind.CycleRecoverable$Context
 outer com.sun.xml.bind.CycleRecoverable
meth public abstract javax.xml.bind.Marshaller getMarshaller()

CLSS public final com.sun.xml.bind.DatatypeConverterImpl
 anno 0 java.lang.Deprecated()
cons protected init()
fld public final static javax.xml.bind.DatatypeConverterInterface theInstance
 anno 0 java.lang.Deprecated()
intf javax.xml.bind.DatatypeConverterInterface
meth public boolean parseBoolean(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public byte parseByte(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public byte[] parseBase64Binary(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public byte[] parseHexBinary(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public double parseDouble(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public float parseFloat(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public int parseInt(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public int parseUnsignedShort(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.String parseAnySimpleType(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.String parseString(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printAnySimpleType(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printBase64Binary(byte[])
 anno 0 java.lang.Deprecated()
meth public java.lang.String printBoolean(boolean)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printByte(byte)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printDate(java.util.Calendar)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printDateTime(java.util.Calendar)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printDecimal(java.math.BigDecimal)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printDouble(double)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printFloat(float)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printHexBinary(byte[])
 anno 0 java.lang.Deprecated()
meth public java.lang.String printInt(int)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printInteger(java.math.BigInteger)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printLong(long)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printQName(javax.xml.namespace.QName,javax.xml.namespace.NamespaceContext)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printShort(short)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printString(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printTime(java.util.Calendar)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printUnsignedInt(long)
 anno 0 java.lang.Deprecated()
meth public java.lang.String printUnsignedShort(int)
 anno 0 java.lang.Deprecated()
meth public java.math.BigDecimal parseDecimal(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.math.BigInteger parseInteger(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.util.Calendar parseDate(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.util.Calendar parseDateTime(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.util.Calendar parseTime(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public javax.xml.namespace.QName parseQName(java.lang.String,javax.xml.namespace.NamespaceContext)
 anno 0 java.lang.Deprecated()
meth public long parseLong(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public long parseUnsignedInt(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public short parseShort(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static byte _parseByte(java.lang.CharSequence)
meth public static byte encodeByte(int)
meth public static byte[] _parseBase64Binary(java.lang.String)
meth public static char encode(int)
meth public static double _parseDouble(java.lang.CharSequence)
meth public static float _parseFloat(java.lang.CharSequence)
meth public static int _parseInt(java.lang.CharSequence)
meth public static int _printBase64Binary(byte[],int,int,byte[],int)
meth public static int _printBase64Binary(byte[],int,int,char[],int)
meth public static java.lang.Boolean _parseBoolean(java.lang.CharSequence)
meth public static java.lang.String _printBase64Binary(byte[])
meth public static java.lang.String _printBase64Binary(byte[],int,int)
meth public static java.lang.String _printBoolean(boolean)
meth public static java.lang.String _printByte(byte)
meth public static java.lang.String _printDate(java.util.Calendar)
meth public static java.lang.String _printDateTime(java.util.Calendar)
meth public static java.lang.String _printDecimal(java.math.BigDecimal)
meth public static java.lang.String _printDouble(double)
meth public static java.lang.String _printFloat(float)
meth public static java.lang.String _printInt(int)
meth public static java.lang.String _printInteger(java.math.BigInteger)
meth public static java.lang.String _printLong(long)
meth public static java.lang.String _printQName(javax.xml.namespace.QName,javax.xml.namespace.NamespaceContext)
meth public static java.lang.String _printShort(short)
meth public static java.math.BigDecimal _parseDecimal(java.lang.CharSequence)
meth public static java.math.BigInteger _parseInteger(java.lang.CharSequence)
meth public static java.util.GregorianCalendar _parseDateTime(java.lang.CharSequence)
meth public static javax.xml.datatype.DatatypeFactory getDatatypeFactory()
meth public static javax.xml.namespace.QName _parseQName(java.lang.CharSequence,javax.xml.namespace.NamespaceContext)
meth public static long _parseLong(java.lang.CharSequence)
meth public static short _parseShort(java.lang.CharSequence)
meth public static void _printBase64Binary(byte[],int,int,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
supr java.lang.Object
hfds DF_CACHE,PADDING,decodeMap,encodeMap,hexCode
hcls CalendarFormatter

CLSS public abstract com.sun.xml.bind.IDResolver
cons public init()
meth public abstract java.util.concurrent.Callable<?> resolve(java.lang.String,java.lang.Class) throws org.xml.sax.SAXException
meth public abstract void bind(java.lang.String,java.lang.Object) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void startDocument(javax.xml.bind.ValidationEventHandler) throws org.xml.sax.SAXException
supr java.lang.Object

CLSS public abstract interface com.sun.xml.bind.InternalAccessorFactory
intf com.sun.xml.bind.AccessorFactory
meth public abstract com.sun.xml.bind.v2.runtime.reflect.Accessor createFieldAccessor(java.lang.Class,java.lang.reflect.Field,boolean,boolean) throws javax.xml.bind.JAXBException

CLSS public abstract interface com.sun.xml.bind.Locatable
meth public abstract org.xml.sax.Locator sourceLocation()

CLSS public final com.sun.xml.bind.Utils
meth public static java.lang.String getSystemProperty(java.lang.String)
meth public static java.util.logging.Logger getClassLogger()
supr java.lang.Object

CLSS public abstract interface com.sun.xml.bind.ValidationEventLocatorEx
intf javax.xml.bind.ValidationEventLocator
meth public abstract java.lang.String getFieldName()

CLSS public abstract com.sun.xml.bind.WhiteSpaceProcessor
cons public init()
meth protected static boolean isWhiteSpaceExceptSpace(char)
meth public static boolean isWhiteSpace(char)
meth public static boolean isWhiteSpace(java.lang.CharSequence)
meth public static java.lang.CharSequence collapse(java.lang.CharSequence)
meth public static java.lang.CharSequence replace(java.lang.CharSequence)
meth public static java.lang.CharSequence trim(java.lang.CharSequence)
meth public static java.lang.String collapse(java.lang.String)
meth public static java.lang.String replace(java.lang.String)
supr java.lang.Object

CLSS public abstract interface !annotation com.sun.xml.bind.XmlAccessorFactory
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, PACKAGE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<? extends com.sun.xml.bind.AccessorFactory> value()

CLSS public abstract interface !annotation com.sun.xml.bind.annotation.OverrideAnnotationOf
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface !annotation com.sun.xml.bind.annotation.XmlIsSet
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD, METHOD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation com.sun.xml.bind.annotation.XmlLocation
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD, METHOD])
intf java.lang.annotation.Annotation

CLSS public final com.sun.xml.bind.api.AccessorException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract com.sun.xml.bind.api.Bridge<%0 extends java.lang.Object>
cons protected init(com.sun.xml.bind.v2.runtime.JAXBContextImpl)
fld protected final com.sun.xml.bind.v2.runtime.JAXBContextImpl context
meth public abstract com.sun.xml.bind.api.TypeReference getTypeReference()
meth public abstract void marshal(javax.xml.bind.Marshaller,{com.sun.xml.bind.api.Bridge%0},java.io.OutputStream,javax.xml.namespace.NamespaceContext) throws javax.xml.bind.JAXBException
 anno 1 com.sun.istack.NotNull()
meth public abstract void marshal(javax.xml.bind.Marshaller,{com.sun.xml.bind.api.Bridge%0},javax.xml.stream.XMLStreamWriter) throws javax.xml.bind.JAXBException
 anno 1 com.sun.istack.NotNull()
meth public abstract void marshal(javax.xml.bind.Marshaller,{com.sun.xml.bind.api.Bridge%0},javax.xml.transform.Result) throws javax.xml.bind.JAXBException
 anno 1 com.sun.istack.NotNull()
meth public abstract void marshal(javax.xml.bind.Marshaller,{com.sun.xml.bind.api.Bridge%0},org.w3c.dom.Node) throws javax.xml.bind.JAXBException
 anno 1 com.sun.istack.NotNull()
meth public abstract void marshal(javax.xml.bind.Marshaller,{com.sun.xml.bind.api.Bridge%0},org.xml.sax.ContentHandler) throws javax.xml.bind.JAXBException
 anno 1 com.sun.istack.NotNull()
meth public abstract {com.sun.xml.bind.api.Bridge%0} unmarshal(javax.xml.bind.Unmarshaller,java.io.InputStream) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public abstract {com.sun.xml.bind.api.Bridge%0} unmarshal(javax.xml.bind.Unmarshaller,javax.xml.stream.XMLStreamReader) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public abstract {com.sun.xml.bind.api.Bridge%0} unmarshal(javax.xml.bind.Unmarshaller,javax.xml.transform.Source) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public abstract {com.sun.xml.bind.api.Bridge%0} unmarshal(javax.xml.bind.Unmarshaller,org.w3c.dom.Node) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public com.sun.xml.bind.api.JAXBRIContext getContext()
 anno 0 com.sun.istack.NotNull()
meth public final void marshal(com.sun.xml.bind.api.BridgeContext,{com.sun.xml.bind.api.Bridge%0},java.io.OutputStream,javax.xml.namespace.NamespaceContext) throws javax.xml.bind.JAXBException
 anno 1 com.sun.istack.NotNull()
meth public final void marshal(com.sun.xml.bind.api.BridgeContext,{com.sun.xml.bind.api.Bridge%0},javax.xml.stream.XMLStreamWriter) throws javax.xml.bind.JAXBException
 anno 1 com.sun.istack.NotNull()
meth public final void marshal(com.sun.xml.bind.api.BridgeContext,{com.sun.xml.bind.api.Bridge%0},javax.xml.transform.Result) throws javax.xml.bind.JAXBException
 anno 1 com.sun.istack.NotNull()
meth public final void marshal(com.sun.xml.bind.api.BridgeContext,{com.sun.xml.bind.api.Bridge%0},org.w3c.dom.Node) throws javax.xml.bind.JAXBException
 anno 1 com.sun.istack.NotNull()
meth public final void marshal(com.sun.xml.bind.api.BridgeContext,{com.sun.xml.bind.api.Bridge%0},org.xml.sax.ContentHandler) throws javax.xml.bind.JAXBException
 anno 1 com.sun.istack.NotNull()
meth public final void marshal({com.sun.xml.bind.api.Bridge%0},javax.xml.stream.XMLStreamWriter) throws javax.xml.bind.JAXBException
meth public final void marshal({com.sun.xml.bind.api.Bridge%0},javax.xml.stream.XMLStreamWriter,javax.xml.bind.attachment.AttachmentMarshaller) throws javax.xml.bind.JAXBException
meth public final void marshal({com.sun.xml.bind.api.Bridge%0},javax.xml.transform.Result) throws javax.xml.bind.JAXBException
meth public final void marshal({com.sun.xml.bind.api.Bridge%0},org.w3c.dom.Node) throws javax.xml.bind.JAXBException
meth public final void marshal({com.sun.xml.bind.api.Bridge%0},org.xml.sax.ContentHandler) throws javax.xml.bind.JAXBException
meth public final void marshal({com.sun.xml.bind.api.Bridge%0},org.xml.sax.ContentHandler,javax.xml.bind.attachment.AttachmentMarshaller) throws javax.xml.bind.JAXBException
meth public final {com.sun.xml.bind.api.Bridge%0} unmarshal(com.sun.xml.bind.api.BridgeContext,java.io.InputStream) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public final {com.sun.xml.bind.api.Bridge%0} unmarshal(com.sun.xml.bind.api.BridgeContext,javax.xml.stream.XMLStreamReader) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public final {com.sun.xml.bind.api.Bridge%0} unmarshal(com.sun.xml.bind.api.BridgeContext,javax.xml.transform.Source) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public final {com.sun.xml.bind.api.Bridge%0} unmarshal(com.sun.xml.bind.api.BridgeContext,org.w3c.dom.Node) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public final {com.sun.xml.bind.api.Bridge%0} unmarshal(java.io.InputStream) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public final {com.sun.xml.bind.api.Bridge%0} unmarshal(javax.xml.stream.XMLStreamReader) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public final {com.sun.xml.bind.api.Bridge%0} unmarshal(javax.xml.stream.XMLStreamReader,javax.xml.bind.attachment.AttachmentUnmarshaller) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.Nullable()
meth public final {com.sun.xml.bind.api.Bridge%0} unmarshal(javax.xml.transform.Source) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public final {com.sun.xml.bind.api.Bridge%0} unmarshal(javax.xml.transform.Source,javax.xml.bind.attachment.AttachmentUnmarshaller) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.Nullable()
meth public final {com.sun.xml.bind.api.Bridge%0} unmarshal(org.w3c.dom.Node) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public final {com.sun.xml.bind.api.Bridge%0} unmarshal(org.w3c.dom.Node,javax.xml.bind.attachment.AttachmentUnmarshaller) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.Nullable()
meth public void marshal({com.sun.xml.bind.api.Bridge%0},java.io.OutputStream,javax.xml.namespace.NamespaceContext) throws javax.xml.bind.JAXBException
meth public void marshal({com.sun.xml.bind.api.Bridge%0},java.io.OutputStream,javax.xml.namespace.NamespaceContext,javax.xml.bind.attachment.AttachmentMarshaller) throws javax.xml.bind.JAXBException
supr java.lang.Object

CLSS public abstract com.sun.xml.bind.api.BridgeContext
cons protected init()
meth public abstract javax.xml.bind.attachment.AttachmentMarshaller getAttachmentMarshaller()
meth public abstract javax.xml.bind.attachment.AttachmentUnmarshaller getAttachmentUnmarshaller()
meth public abstract void setAttachmentMarshaller(javax.xml.bind.attachment.AttachmentMarshaller)
meth public abstract void setAttachmentUnmarshaller(javax.xml.bind.attachment.AttachmentUnmarshaller)
meth public abstract void setErrorHandler(javax.xml.bind.ValidationEventHandler)
supr java.lang.Object

CLSS public abstract com.sun.xml.bind.api.ClassResolver
cons public init()
meth public abstract java.lang.Class<?> resolveElementName(java.lang.String,java.lang.String) throws java.lang.Exception
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
supr java.lang.Object

CLSS public com.sun.xml.bind.api.CompositeStructure
cons public init()
fld public com.sun.xml.bind.api.Bridge[] bridges
fld public java.lang.Object[] values
supr java.lang.Object

CLSS public abstract interface com.sun.xml.bind.api.ErrorListener
intf org.xml.sax.ErrorHandler
meth public abstract void error(org.xml.sax.SAXParseException)
meth public abstract void fatalError(org.xml.sax.SAXParseException)
meth public abstract void info(org.xml.sax.SAXParseException)
meth public abstract void warning(org.xml.sax.SAXParseException)

CLSS public abstract com.sun.xml.bind.api.JAXBRIContext
cons protected init()
fld public final static java.lang.String ANNOTATION_READER
fld public final static java.lang.String BACKUP_WITH_PARENT_NAMESPACE = "com.sun.xml.bind.backupWithParentNamespace"
fld public final static java.lang.String CANONICALIZATION_SUPPORT = "com.sun.xml.bind.c14n"
fld public final static java.lang.String DEFAULT_NAMESPACE_REMAP = "com.sun.xml.bind.defaultNamespaceRemap"
fld public final static java.lang.String DISABLE_XML_SECURITY = "com.sun.xml.bind.disableXmlSecurity"
fld public final static java.lang.String ENABLE_XOP = "com.sun.xml.bind.XOP"
fld public final static java.lang.String IMPROVED_XSI_TYPE_HANDLING = "com.sun.xml.bind.improvedXsiTypeHandling"
fld public final static java.lang.String MAX_ERRORS = "com.sun.xml.bind.maxErrorsCount"
fld public final static java.lang.String RETAIN_REFERENCE_TO_INFO = "retainReferenceToInfo"
fld public final static java.lang.String SUBCLASS_REPLACEMENTS = "com.sun.xml.bind.subclassReplacements"
fld public final static java.lang.String SUPRESS_ACCESSOR_WARNINGS = "supressAccessorWarnings"
fld public final static java.lang.String TREAT_EVERYTHING_NILLABLE = "com.sun.xml.bind.treatEverythingNillable"
fld public final static java.lang.String TYPE_REFERENCES = "com.sun.xml.bind.typeReferences"
fld public final static java.lang.String XMLACCESSORFACTORY_SUPPORT = "com.sun.xml.bind.XmlAccessorFactory"
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> com.sun.xml.bind.api.RawAccessor<{%%0},{%%1}> getElementPropertyAccessor(java.lang.Class<{%%0}>,java.lang.String,java.lang.String) throws javax.xml.bind.JAXBException
meth public abstract boolean hasSwaRef()
meth public abstract com.sun.xml.bind.api.Bridge createBridge(com.sun.xml.bind.api.TypeReference)
 anno 1 com.sun.istack.NotNull()
meth public abstract com.sun.xml.bind.api.BridgeContext createBridgeContext()
 anno 0 com.sun.istack.NotNull()
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet getRuntimeTypeInfoSet()
meth public abstract java.lang.String getBuildId()
 anno 0 com.sun.istack.NotNull()
meth public abstract java.util.List<java.lang.String> getKnownNamespaceURIs()
 anno 0 com.sun.istack.NotNull()
meth public abstract javax.xml.namespace.QName getElementName(java.lang.Class) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
meth public abstract javax.xml.namespace.QName getElementName(java.lang.Object) throws javax.xml.bind.JAXBException
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
meth public abstract javax.xml.namespace.QName getTypeName(com.sun.xml.bind.api.TypeReference)
 anno 1 com.sun.istack.NotNull()
meth public abstract void generateEpisode(javax.xml.transform.Result)
meth public abstract void generateSchema(javax.xml.bind.SchemaOutputResolver) throws java.io.IOException
 anno 1 com.sun.istack.NotNull()
meth public static com.sun.xml.bind.api.JAXBRIContext newInstance(java.lang.Class[],java.util.Collection<com.sun.xml.bind.api.TypeReference>,java.lang.String,boolean) throws javax.xml.bind.JAXBException
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.Nullable()
 anno 3 com.sun.istack.Nullable()
meth public static com.sun.xml.bind.api.JAXBRIContext newInstance(java.lang.Class[],java.util.Collection<com.sun.xml.bind.api.TypeReference>,java.util.Map<java.lang.Class,java.lang.Class>,java.lang.String,boolean,com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader) throws javax.xml.bind.JAXBException
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.Nullable()
 anno 3 com.sun.istack.Nullable()
 anno 4 com.sun.istack.Nullable()
 anno 6 com.sun.istack.Nullable()
meth public static com.sun.xml.bind.api.JAXBRIContext newInstance(java.lang.Class[],java.util.Collection<com.sun.xml.bind.api.TypeReference>,java.util.Map<java.lang.Class,java.lang.Class>,java.lang.String,boolean,com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader,boolean,boolean,boolean,boolean) throws javax.xml.bind.JAXBException
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.Nullable()
 anno 3 com.sun.istack.Nullable()
 anno 4 com.sun.istack.Nullable()
 anno 6 com.sun.istack.Nullable()
meth public static java.lang.String mangleNameToClassName(java.lang.String)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public static java.lang.String mangleNameToPropertyName(java.lang.String)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public static java.lang.String mangleNameToVariableName(java.lang.String)
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public static java.lang.reflect.Type getBaseType(java.lang.reflect.Type,java.lang.Class)
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
supr javax.xml.bind.JAXBContext

CLSS public abstract com.sun.xml.bind.api.RawAccessor<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
meth public abstract void set({com.sun.xml.bind.api.RawAccessor%0},{com.sun.xml.bind.api.RawAccessor%1}) throws com.sun.xml.bind.api.AccessorException
meth public abstract {com.sun.xml.bind.api.RawAccessor%1} get({com.sun.xml.bind.api.RawAccessor%0}) throws com.sun.xml.bind.api.AccessorException
supr java.lang.Object

CLSS public final com.sun.xml.bind.api.TypeReference
cons public !varargs init(javax.xml.namespace.QName,java.lang.reflect.Type,java.lang.annotation.Annotation[])
fld public final java.lang.annotation.Annotation[] annotations
fld public final java.lang.reflect.Type type
fld public final javax.xml.namespace.QName tagName
meth public <%0 extends java.lang.annotation.Annotation> {%%0} get(java.lang.Class<{%%0}>)
meth public boolean equals(java.lang.Object)
meth public com.sun.xml.bind.api.TypeReference toItemType()
meth public int hashCode()
supr java.lang.Object

CLSS public abstract interface com.sun.xml.bind.api.impl.NameConverter
fld public final static com.sun.xml.bind.api.impl.NameConverter jaxrpcCompatible
fld public final static com.sun.xml.bind.api.impl.NameConverter smart
fld public final static com.sun.xml.bind.api.impl.NameConverter standard
innr public static Standard
meth public abstract java.lang.String toClassName(java.lang.String)
meth public abstract java.lang.String toConstantName(java.lang.String)
meth public abstract java.lang.String toInterfaceName(java.lang.String)
meth public abstract java.lang.String toPackageName(java.lang.String)
meth public abstract java.lang.String toPropertyName(java.lang.String)
meth public abstract java.lang.String toVariableName(java.lang.String)

CLSS public static com.sun.xml.bind.api.impl.NameConverter$Standard
 outer com.sun.xml.bind.api.impl.NameConverter
cons public init()
fld protected final static int DIGIT = 3
fld protected final static int LOWER_LETTER = 1
fld protected final static int OTHER = 4
fld protected final static int OTHER_LETTER = 2
fld protected final static int UPPER_LETTER = 0
intf com.sun.xml.bind.api.impl.NameConverter
meth protected boolean isLetter(char)
meth protected boolean isPunct(char)
meth protected int classify(char)
meth protected java.lang.String toMixedCaseName(java.util.List<java.lang.String>,boolean)
meth protected java.lang.String toMixedCaseVariableName(java.lang.String[],boolean,boolean)
meth protected static boolean isDigit(char)
meth protected static boolean isLower(char)
meth protected static boolean isUpper(char)
meth public java.lang.String capitalize(java.lang.String)
meth public java.lang.String toClassName(java.lang.String)
meth public java.lang.String toConstantName(java.lang.String)
meth public java.lang.String toConstantName(java.util.List<java.lang.String>)
meth public java.lang.String toInterfaceName(java.lang.String)
meth public java.lang.String toPackageName(java.lang.String)
meth public java.lang.String toPropertyName(java.lang.String)
meth public java.lang.String toVariableName(java.lang.String)
meth public java.util.List<java.lang.String> toWordList(java.lang.String)
meth public static void escape(java.lang.StringBuilder,java.lang.String,int)
supr java.lang.Object

CLSS public abstract interface com.sun.xml.bind.marshaller.CharacterEscapeHandler
meth public abstract void escape(char[],int,int,boolean,java.io.Writer) throws java.io.IOException

CLSS public com.sun.xml.bind.marshaller.DataWriter
cons public init(java.io.Writer,java.lang.String)
cons public init(java.io.Writer,java.lang.String,com.sun.xml.bind.marshaller.CharacterEscapeHandler)
meth protected void writeXmlDecl(java.lang.String) throws java.io.IOException
meth public int getIndentStep()
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void reset()
meth public void setIndentStep(int)
meth public void setIndentStep(java.lang.String)
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
supr com.sun.xml.bind.marshaller.XMLWriter
hfds SEEN_DATA,SEEN_ELEMENT,SEEN_NOTHING,depth,indentStep,state,stateStack

CLSS public com.sun.xml.bind.marshaller.DumbEscapeHandler
fld public final static com.sun.xml.bind.marshaller.CharacterEscapeHandler theInstance
intf com.sun.xml.bind.marshaller.CharacterEscapeHandler
meth public void escape(char[],int,int,boolean,java.io.Writer) throws java.io.IOException
supr java.lang.Object

CLSS public com.sun.xml.bind.marshaller.Messages
cons public init()
fld public final static java.lang.String ASSERT_FAILED = "SAXMarshaller.AssertFailed"
fld public final static java.lang.String DOM_IMPL_DOESNT_SUPPORT_CREATELEMENTNS = "SAX2DOMEx.DomImplDoesntSupportCreateElementNs"
fld public final static java.lang.String ERR_DANGLING_IDREF = "SAXMarshaller.DanglingIDREF"
fld public final static java.lang.String ERR_MISSING_OBJECT = "SAXMarshaller.MissingObject"
fld public final static java.lang.String ERR_NOT_IDENTIFIABLE = "SAXMarshaller.NotIdentifiable"
fld public final static java.lang.String NOT_MARSHALLABLE = "MarshallerImpl.NotMarshallable"
fld public final static java.lang.String NULL_WRITER = "MarshallerImpl.NullWriterParam"
fld public final static java.lang.String UNSUPPORTED_ENCODING = "MarshallerImpl.UnsupportedEncoding"
fld public final static java.lang.String UNSUPPORTED_RESULT = "MarshallerImpl.UnsupportedResult"
meth public static java.lang.String format(java.lang.String)
meth public static java.lang.String format(java.lang.String,java.lang.Object)
meth public static java.lang.String format(java.lang.String,java.lang.Object,java.lang.Object)
meth public static java.lang.String format(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object)
supr java.lang.Object

CLSS public com.sun.xml.bind.marshaller.MinimumEscapeHandler
fld public final static com.sun.xml.bind.marshaller.CharacterEscapeHandler theInstance
intf com.sun.xml.bind.marshaller.CharacterEscapeHandler
meth public void escape(char[],int,int,boolean,java.io.Writer) throws java.io.IOException
supr java.lang.Object

CLSS public abstract com.sun.xml.bind.marshaller.NamespacePrefixMapper
cons public init()
meth public abstract java.lang.String getPreferredPrefix(java.lang.String,java.lang.String,boolean)
meth public java.lang.String[] getContextualNamespaceDecls()
meth public java.lang.String[] getPreDeclaredNamespaceUris()
meth public java.lang.String[] getPreDeclaredNamespaceUris2()
supr java.lang.Object
hfds EMPTY_STRING

CLSS public com.sun.xml.bind.marshaller.NioEscapeHandler
cons public init(java.lang.String)
intf com.sun.xml.bind.marshaller.CharacterEscapeHandler
meth public void escape(char[],int,int,boolean,java.io.Writer) throws java.io.IOException
supr java.lang.Object
hfds encoder

CLSS public com.sun.xml.bind.marshaller.NoEscapeHandler
cons public init()
fld public final static com.sun.xml.bind.marshaller.NoEscapeHandler theInstance
intf com.sun.xml.bind.marshaller.CharacterEscapeHandler
meth public void escape(char[],int,int,boolean,java.io.Writer) throws java.io.IOException
supr java.lang.Object

CLSS public com.sun.xml.bind.marshaller.SAX2DOMEx
cons public init() throws javax.xml.parsers.ParserConfigurationException
cons public init(javax.xml.parsers.DocumentBuilderFactory) throws javax.xml.parsers.ParserConfigurationException
cons public init(org.w3c.dom.Node)
cons public init(org.w3c.dom.Node,boolean)
fld protected final java.util.Stack<org.w3c.dom.Node> nodeStack
fld protected final org.w3c.dom.Document document
intf org.xml.sax.ContentHandler
meth protected org.w3c.dom.Text characters(java.lang.String)
meth protected void namespace(org.w3c.dom.Element,java.lang.String,java.lang.String)
meth public final org.w3c.dom.Element getCurrentElement()
meth public org.w3c.dom.Node getDOM()
meth public void characters(char[],int,int)
meth public void endDocument()
meth public void endElement(java.lang.String,java.lang.String,java.lang.String)
meth public void endPrefixMapping(java.lang.String)
meth public void ignorableWhitespace(char[],int,int)
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void skippedEntity(java.lang.String)
meth public void startDocument()
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes)
meth public void startPrefixMapping(java.lang.String,java.lang.String)
supr java.lang.Object
hfds isConsolidate,node,unprocessedNamespaces

CLSS public com.sun.xml.bind.marshaller.XMLWriter
cons public init(java.io.Writer,java.lang.String)
cons public init(java.io.Writer,java.lang.String,com.sun.xml.bind.marshaller.CharacterEscapeHandler)
meth protected final void write(char) throws java.io.IOException
meth protected final void write(java.lang.String) throws java.io.IOException
meth protected void writeXmlDecl(java.lang.String) throws java.io.IOException
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void characters(java.lang.String) throws org.xml.sax.SAXException
meth public void dataElement(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void dataElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void dataElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes,java.lang.String) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String) throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void flush() throws java.io.IOException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void reset()
meth public void setHeader(java.lang.String)
meth public void setOutput(java.io.Writer,java.lang.String)
meth public void setXmlDecl(boolean)
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String) throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.XMLFilterImpl
hfds EMPTY_ATTS,elementLevel,encoding,escapeHandler,header,locallyDeclaredPrefix,output,startTagIsClosed,writeXmlDecl

CLSS public com.sun.xml.bind.unmarshaller.DOMScanner
cons public init()
intf com.sun.xml.bind.unmarshaller.InfosetScanner
intf com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx
meth public com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx getLocator()
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.Object getCurrentElement()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
meth public javax.xml.bind.ValidationEventLocator getLocation()
meth public org.w3c.dom.Node getCurrentLocation()
meth public org.xml.sax.ContentHandler getContentHandler()
meth public void parse(org.w3c.dom.Element,org.xml.sax.ContentHandler) throws org.xml.sax.SAXException
meth public void parseWithContext(org.w3c.dom.Element,org.xml.sax.ContentHandler) throws org.xml.sax.SAXException
meth public void scan(java.lang.Object) throws org.xml.sax.SAXException
meth public void scan(org.w3c.dom.Document) throws org.xml.sax.SAXException
meth public void scan(org.w3c.dom.Element) throws org.xml.sax.SAXException
meth public void setContentHandler(org.xml.sax.ContentHandler)
meth public void setLocator(org.xml.sax.Locator)
meth public void visit(org.w3c.dom.Element) throws org.xml.sax.SAXException
supr java.lang.Object
hfds atts,currentNode,locator,receiver

CLSS public abstract interface com.sun.xml.bind.unmarshaller.InfosetScanner<%0 extends java.lang.Object>
meth public abstract com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx getLocator()
meth public abstract org.xml.sax.ContentHandler getContentHandler()
meth public abstract void scan({com.sun.xml.bind.unmarshaller.InfosetScanner%0}) throws org.xml.sax.SAXException
meth public abstract void setContentHandler(org.xml.sax.ContentHandler)
meth public abstract {com.sun.xml.bind.unmarshaller.InfosetScanner%0} getCurrentElement()

CLSS public com.sun.xml.bind.unmarshaller.Messages
cons public init()
fld public final static java.lang.String ILLEGAL_READER_STATE = "Unmarshaller.IllegalReaderState"
fld public final static java.lang.String NULL_READER = "Unmarshaller.NullReader"
fld public final static java.lang.String UNDEFINED_PREFIX = "Util.UndefinedPrefix"
fld public final static java.lang.String UNEXPECTED_ENTER_ATTRIBUTE = "ContentHandlerEx.UnexpectedEnterAttribute"
fld public final static java.lang.String UNEXPECTED_ENTER_ELEMENT = "ContentHandlerEx.UnexpectedEnterElement"
fld public final static java.lang.String UNEXPECTED_LEAVE_ATTRIBUTE = "ContentHandlerEx.UnexpectedLeaveAttribute"
fld public final static java.lang.String UNEXPECTED_LEAVE_CHILD = "ContentHandlerEx.UnexpectedLeaveChild"
fld public final static java.lang.String UNEXPECTED_LEAVE_ELEMENT = "ContentHandlerEx.UnexpectedLeaveElement"
fld public final static java.lang.String UNEXPECTED_ROOT_ELEMENT = "SAXUnmarshallerHandlerImpl.UnexpectedRootElement"
fld public final static java.lang.String UNEXPECTED_TEXT = "ContentHandlerEx.UnexpectedText"
meth public static java.lang.String format(java.lang.String)
meth public static java.lang.String format(java.lang.String,java.lang.Object)
meth public static java.lang.String format(java.lang.String,java.lang.Object,java.lang.Object)
meth public static java.lang.String format(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object)
meth public static java.lang.String format(java.lang.String,java.lang.Object[])
supr java.lang.Object

CLSS public abstract interface com.sun.xml.bind.unmarshaller.Patcher
meth public abstract void run() throws org.xml.sax.SAXException

CLSS public com.sun.xml.bind.util.AttributesImpl
cons public init()
cons public init(org.xml.sax.Attributes)
intf org.xml.sax.Attributes
meth public int getIndex(java.lang.String)
meth public int getIndex(java.lang.String,java.lang.String)
meth public int getIndexFast(java.lang.String,java.lang.String)
meth public int getLength()
meth public java.lang.String getLocalName(int)
meth public java.lang.String getQName(int)
meth public java.lang.String getType(int)
meth public java.lang.String getType(java.lang.String)
meth public java.lang.String getType(java.lang.String,java.lang.String)
meth public java.lang.String getURI(int)
meth public java.lang.String getValue(int)
meth public java.lang.String getValue(java.lang.String)
meth public java.lang.String getValue(java.lang.String,java.lang.String)
meth public void addAttribute(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public void clear()
meth public void removeAttribute(int)
meth public void setAttribute(int,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public void setAttributes(org.xml.sax.Attributes)
meth public void setLocalName(int,java.lang.String)
meth public void setQName(int,java.lang.String)
meth public void setType(int,java.lang.String)
meth public void setURI(int,java.lang.String)
meth public void setValue(int,java.lang.String)
supr java.lang.Object
hfds data,length

CLSS public com.sun.xml.bind.util.ValidationEventLocatorExImpl
cons public init(java.lang.Object,java.lang.String)
intf com.sun.xml.bind.ValidationEventLocatorEx
meth public java.lang.String getFieldName()
meth public java.lang.String toString()
supr javax.xml.bind.helpers.ValidationEventLocatorImpl
hfds fieldName

CLSS public com.sun.xml.bind.util.Which
cons public init()
meth public static java.lang.String which(java.lang.Class)
meth public static java.lang.String which(java.lang.String,java.lang.ClassLoader)
supr java.lang.Object

CLSS public final com.sun.xml.bind.v2.ClassFactory
cons public init()
meth public static <%0 extends java.lang.Object> java.lang.Class<? extends {%%0}> inferImplClass(java.lang.Class<{%%0}>,java.lang.Class[])
meth public static <%0 extends java.lang.Object> {%%0} create(java.lang.Class<{%%0}>)
meth public static <%0 extends java.lang.Object> {%%0} create0(java.lang.Class<{%%0}>) throws java.lang.IllegalAccessException,java.lang.InstantiationException,java.lang.reflect.InvocationTargetException
meth public static java.lang.Object create(java.lang.reflect.Method)
meth public static void cleanCache()
supr java.lang.Object
hfds emptyClass,emptyObject,logger,tls

CLSS public com.sun.xml.bind.v2.ContextFactory
cons public init()
fld public final static java.lang.String USE_JAXB_PROPERTIES = "_useJAXBProperties"
meth public static com.sun.xml.bind.api.JAXBRIContext createContext(java.lang.Class[],java.util.Collection<com.sun.xml.bind.api.TypeReference>,java.util.Map<java.lang.Class,java.lang.Class>,java.lang.String,boolean,com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader,boolean,boolean,boolean) throws javax.xml.bind.JAXBException
 anno 0 java.lang.Deprecated()
meth public static com.sun.xml.bind.api.JAXBRIContext createContext(java.lang.Class[],java.util.Collection<com.sun.xml.bind.api.TypeReference>,java.util.Map<java.lang.Class,java.lang.Class>,java.lang.String,boolean,com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader,boolean,boolean,boolean,boolean) throws javax.xml.bind.JAXBException
 anno 0 java.lang.Deprecated()
meth public static javax.xml.bind.JAXBContext createContext(java.lang.Class[],java.util.Map<java.lang.String,java.lang.Object>) throws javax.xml.bind.JAXBException
meth public static javax.xml.bind.JAXBContext createContext(java.lang.String,java.lang.ClassLoader,java.util.Map<java.lang.String,java.lang.Object>) throws javax.xml.bind.JAXBException
supr java.lang.Object

CLSS public com.sun.xml.bind.v2.JAXBContextFactory
cons public init()
intf javax.xml.bind.JAXBContextFactory
meth public javax.xml.bind.JAXBContext createContext(java.lang.Class<?>[],java.util.Map<java.lang.String,?>) throws javax.xml.bind.JAXBException
meth public javax.xml.bind.JAXBContext createContext(java.lang.String,java.lang.ClassLoader,java.util.Map<java.lang.String,?>) throws javax.xml.bind.JAXBException
supr java.lang.Object

CLSS public final !enum com.sun.xml.bind.v2.Messages
fld public final static com.sun.xml.bind.v2.Messages BROKEN_CONTEXTPATH
fld public final static com.sun.xml.bind.v2.Messages ERROR_LOADING_CLASS
fld public final static com.sun.xml.bind.v2.Messages ILLEGAL_ENTRY
fld public final static com.sun.xml.bind.v2.Messages INVALID_JAXP_IMPLEMENTATION
fld public final static com.sun.xml.bind.v2.Messages INVALID_PROPERTY_VALUE
fld public final static com.sun.xml.bind.v2.Messages INVALID_TYPE_IN_MAP
fld public final static com.sun.xml.bind.v2.Messages JAXP_EXTERNAL_ACCESS_CONFIGURED
fld public final static com.sun.xml.bind.v2.Messages JAXP_SUPPORTED_PROPERTY
fld public final static com.sun.xml.bind.v2.Messages JAXP_UNSUPPORTED_PROPERTY
fld public final static com.sun.xml.bind.v2.Messages JAXP_XML_SECURITY_DISABLED
fld public final static com.sun.xml.bind.v2.Messages NO_DEFAULT_CONSTRUCTOR_IN_INNER_CLASS
fld public final static com.sun.xml.bind.v2.Messages UNSUPPORTED_PROPERTY
meth public !varargs java.lang.String format(java.lang.Object[])
meth public java.lang.String toString()
meth public static com.sun.xml.bind.v2.Messages valueOf(java.lang.String)
meth public static com.sun.xml.bind.v2.Messages[] values()
supr java.lang.Enum<com.sun.xml.bind.v2.Messages>
hfds rb

CLSS public abstract com.sun.xml.bind.v2.TODO
cons public init()
meth public static void checkSpec()
meth public static void checkSpec(java.lang.String)
meth public static void prototype()
meth public static void prototype(java.lang.String)
meth public static void schemaGenerator(java.lang.String)
supr java.lang.Object

CLSS public abstract com.sun.xml.bind.v2.WellKnownNamespace
fld public final static java.lang.String JAXB = "http://java.sun.com/xml/ns/jaxb"
fld public final static java.lang.String SWA_URI = "http://ws-i.org/profiles/basic/1.1/xsd"
fld public final static java.lang.String XML_MIME_URI = "http://www.w3.org/2005/05/xmlmime"
fld public final static java.lang.String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String XOP = "http://www.w3.org/2004/08/xop/include"
supr java.lang.Object

CLSS public abstract com.sun.xml.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object>
cons public init()
intf com.sun.xml.bind.v2.model.annotation.AnnotationReader<{com.sun.xml.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl%0},{com.sun.xml.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl%1},{com.sun.xml.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl%2},{com.sun.xml.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl%3}>
meth protected abstract java.lang.String fullName({com.sun.xml.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl%3})
meth public boolean hasMethodAnnotation(java.lang.Class<? extends java.lang.annotation.Annotation>,java.lang.String,{com.sun.xml.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl%3},{com.sun.xml.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl%3},com.sun.xml.bind.v2.model.annotation.Locatable)
meth public final <%0 extends java.lang.annotation.Annotation> {%%0} getMethodAnnotation(java.lang.Class<{%%0}>,{com.sun.xml.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl%3},{com.sun.xml.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl%3},com.sun.xml.bind.v2.model.annotation.Locatable)
meth public final com.sun.xml.bind.v2.model.core.ErrorHandler getErrorHandler()
meth public void setErrorHandler(com.sun.xml.bind.v2.model.core.ErrorHandler)
supr java.lang.Object
hfds errorHandler

CLSS public abstract interface com.sun.xml.bind.v2.model.annotation.AnnotationReader<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object>
meth public abstract <%0 extends java.lang.annotation.Annotation> {%%0} getClassAnnotation(java.lang.Class<{%%0}>,{com.sun.xml.bind.v2.model.annotation.AnnotationReader%1},com.sun.xml.bind.v2.model.annotation.Locatable)
 anno 0 com.sun.istack.Nullable()
meth public abstract <%0 extends java.lang.annotation.Annotation> {%%0} getFieldAnnotation(java.lang.Class<{%%0}>,{com.sun.xml.bind.v2.model.annotation.AnnotationReader%2},com.sun.xml.bind.v2.model.annotation.Locatable)
meth public abstract <%0 extends java.lang.annotation.Annotation> {%%0} getMethodAnnotation(java.lang.Class<{%%0}>,{com.sun.xml.bind.v2.model.annotation.AnnotationReader%3},com.sun.xml.bind.v2.model.annotation.Locatable)
meth public abstract <%0 extends java.lang.annotation.Annotation> {%%0} getMethodAnnotation(java.lang.Class<{%%0}>,{com.sun.xml.bind.v2.model.annotation.AnnotationReader%3},{com.sun.xml.bind.v2.model.annotation.AnnotationReader%3},com.sun.xml.bind.v2.model.annotation.Locatable)
meth public abstract <%0 extends java.lang.annotation.Annotation> {%%0} getMethodParameterAnnotation(java.lang.Class<{%%0}>,{com.sun.xml.bind.v2.model.annotation.AnnotationReader%3},int,com.sun.xml.bind.v2.model.annotation.Locatable)
 anno 0 com.sun.istack.Nullable()
meth public abstract <%0 extends java.lang.annotation.Annotation> {%%0} getPackageAnnotation(java.lang.Class<{%%0}>,{com.sun.xml.bind.v2.model.annotation.AnnotationReader%1},com.sun.xml.bind.v2.model.annotation.Locatable)
 anno 0 com.sun.istack.Nullable()
meth public abstract boolean hasClassAnnotation({com.sun.xml.bind.v2.model.annotation.AnnotationReader%1},java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public abstract boolean hasFieldAnnotation(java.lang.Class<? extends java.lang.annotation.Annotation>,{com.sun.xml.bind.v2.model.annotation.AnnotationReader%2})
meth public abstract boolean hasMethodAnnotation(java.lang.Class<? extends java.lang.annotation.Annotation>,java.lang.String,{com.sun.xml.bind.v2.model.annotation.AnnotationReader%3},{com.sun.xml.bind.v2.model.annotation.AnnotationReader%3},com.sun.xml.bind.v2.model.annotation.Locatable)
meth public abstract boolean hasMethodAnnotation(java.lang.Class<? extends java.lang.annotation.Annotation>,{com.sun.xml.bind.v2.model.annotation.AnnotationReader%3})
meth public abstract java.lang.annotation.Annotation[] getAllFieldAnnotations({com.sun.xml.bind.v2.model.annotation.AnnotationReader%2},com.sun.xml.bind.v2.model.annotation.Locatable)
meth public abstract java.lang.annotation.Annotation[] getAllMethodAnnotations({com.sun.xml.bind.v2.model.annotation.AnnotationReader%3},com.sun.xml.bind.v2.model.annotation.Locatable)
meth public abstract void setErrorHandler(com.sun.xml.bind.v2.model.core.ErrorHandler)
meth public abstract {com.sun.xml.bind.v2.model.annotation.AnnotationReader%0} getClassValue(java.lang.annotation.Annotation,java.lang.String)
meth public abstract {com.sun.xml.bind.v2.model.annotation.AnnotationReader%0}[] getClassArrayValue(java.lang.annotation.Annotation,java.lang.String)

CLSS public abstract interface com.sun.xml.bind.v2.model.annotation.AnnotationSource
meth public abstract <%0 extends java.lang.annotation.Annotation> {%%0} readAnnotation(java.lang.Class<{%%0}>)
meth public abstract boolean hasAnnotation(java.lang.Class<? extends java.lang.annotation.Annotation>)

CLSS public com.sun.xml.bind.v2.model.annotation.ClassLocatable<%0 extends java.lang.Object>
cons public init(com.sun.xml.bind.v2.model.annotation.Locatable,{com.sun.xml.bind.v2.model.annotation.ClassLocatable%0},com.sun.xml.bind.v2.model.nav.Navigator<?,{com.sun.xml.bind.v2.model.annotation.ClassLocatable%0},?,?>)
intf com.sun.xml.bind.v2.model.annotation.Locatable
meth public com.sun.xml.bind.v2.model.annotation.Locatable getUpstream()
meth public com.sun.xml.bind.v2.runtime.Location getLocation()
supr java.lang.Object
hfds clazz,nav,upstream

CLSS public com.sun.xml.bind.v2.model.annotation.FieldLocatable<%0 extends java.lang.Object>
cons public init(com.sun.xml.bind.v2.model.annotation.Locatable,{com.sun.xml.bind.v2.model.annotation.FieldLocatable%0},com.sun.xml.bind.v2.model.nav.Navigator<?,?,{com.sun.xml.bind.v2.model.annotation.FieldLocatable%0},?>)
intf com.sun.xml.bind.v2.model.annotation.Locatable
meth public com.sun.xml.bind.v2.model.annotation.Locatable getUpstream()
meth public com.sun.xml.bind.v2.runtime.Location getLocation()
supr java.lang.Object
hfds field,nav,upstream

CLSS public abstract interface com.sun.xml.bind.v2.model.annotation.Locatable
meth public abstract com.sun.xml.bind.v2.model.annotation.Locatable getUpstream()
meth public abstract com.sun.xml.bind.v2.runtime.Location getLocation()

CLSS public com.sun.xml.bind.v2.model.annotation.LocatableAnnotation
intf com.sun.xml.bind.v2.model.annotation.Locatable
intf com.sun.xml.bind.v2.runtime.Location
intf java.lang.reflect.InvocationHandler
meth public com.sun.xml.bind.v2.model.annotation.Locatable getUpstream()
meth public com.sun.xml.bind.v2.runtime.Location getLocation()
meth public java.lang.Object invoke(java.lang.Object,java.lang.reflect.Method,java.lang.Object[]) throws java.lang.Throwable
meth public java.lang.String toString()
meth public static <%0 extends java.lang.annotation.Annotation> {%%0} create({%%0},com.sun.xml.bind.v2.model.annotation.Locatable)
supr java.lang.Object
hfds core,quicks,upstream

CLSS public com.sun.xml.bind.v2.model.annotation.MethodLocatable<%0 extends java.lang.Object>
cons public init(com.sun.xml.bind.v2.model.annotation.Locatable,{com.sun.xml.bind.v2.model.annotation.MethodLocatable%0},com.sun.xml.bind.v2.model.nav.Navigator<?,?,?,{com.sun.xml.bind.v2.model.annotation.MethodLocatable%0}>)
intf com.sun.xml.bind.v2.model.annotation.Locatable
meth public com.sun.xml.bind.v2.model.annotation.Locatable getUpstream()
meth public com.sun.xml.bind.v2.runtime.Location getLocation()
supr java.lang.Object
hfds method,nav,upstream

CLSS public abstract com.sun.xml.bind.v2.model.annotation.Quick
cons protected init(com.sun.xml.bind.v2.model.annotation.Locatable)
intf com.sun.xml.bind.v2.model.annotation.Locatable
intf com.sun.xml.bind.v2.runtime.Location
intf java.lang.annotation.Annotation
meth protected abstract com.sun.xml.bind.v2.model.annotation.Quick newInstance(com.sun.xml.bind.v2.model.annotation.Locatable,java.lang.annotation.Annotation)
meth protected abstract java.lang.annotation.Annotation getAnnotation()
meth public final com.sun.xml.bind.v2.model.annotation.Locatable getUpstream()
meth public final com.sun.xml.bind.v2.runtime.Location getLocation()
meth public final java.lang.String toString()
supr java.lang.Object
hfds upstream

CLSS public abstract interface com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader
intf com.sun.xml.bind.v2.model.annotation.AnnotationReader<java.lang.reflect.Type,java.lang.Class,java.lang.reflect.Field,java.lang.reflect.Method>

CLSS public final com.sun.xml.bind.v2.model.annotation.RuntimeInlineAnnotationReader
cons public init()
intf com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader
meth protected java.lang.String fullName(java.lang.reflect.Method)
meth public <%0 extends java.lang.annotation.Annotation> {%%0} getClassAnnotation(java.lang.Class<{%%0}>,java.lang.Class,com.sun.xml.bind.v2.model.annotation.Locatable)
meth public <%0 extends java.lang.annotation.Annotation> {%%0} getFieldAnnotation(java.lang.Class<{%%0}>,java.lang.reflect.Field,com.sun.xml.bind.v2.model.annotation.Locatable)
meth public <%0 extends java.lang.annotation.Annotation> {%%0} getMethodAnnotation(java.lang.Class<{%%0}>,java.lang.reflect.Method,com.sun.xml.bind.v2.model.annotation.Locatable)
meth public <%0 extends java.lang.annotation.Annotation> {%%0} getMethodParameterAnnotation(java.lang.Class<{%%0}>,java.lang.reflect.Method,int,com.sun.xml.bind.v2.model.annotation.Locatable)
meth public <%0 extends java.lang.annotation.Annotation> {%%0} getPackageAnnotation(java.lang.Class<{%%0}>,java.lang.Class,com.sun.xml.bind.v2.model.annotation.Locatable)
meth public boolean hasClassAnnotation(java.lang.Class,java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public boolean hasFieldAnnotation(java.lang.Class<? extends java.lang.annotation.Annotation>,java.lang.reflect.Field)
meth public boolean hasMethodAnnotation(java.lang.Class<? extends java.lang.annotation.Annotation>,java.lang.reflect.Method)
meth public java.lang.Class getClassValue(java.lang.annotation.Annotation,java.lang.String)
meth public java.lang.Class[] getClassArrayValue(java.lang.annotation.Annotation,java.lang.String)
meth public java.lang.annotation.Annotation[] getAllFieldAnnotations(java.lang.reflect.Field,com.sun.xml.bind.v2.model.annotation.Locatable)
meth public java.lang.annotation.Annotation[] getAllMethodAnnotations(java.lang.reflect.Method,com.sun.xml.bind.v2.model.annotation.Locatable)
supr com.sun.xml.bind.v2.model.annotation.AbstractInlineAnnotationReaderImpl<java.lang.reflect.Type,java.lang.Class,java.lang.reflect.Field,java.lang.reflect.Method>
hfds packageCache

CLSS public com.sun.xml.bind.v2.model.core.Adapter<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter,com.sun.xml.bind.v2.model.annotation.AnnotationReader<{com.sun.xml.bind.v2.model.core.Adapter%0},{com.sun.xml.bind.v2.model.core.Adapter%1},?,?>,com.sun.xml.bind.v2.model.nav.Navigator<{com.sun.xml.bind.v2.model.core.Adapter%0},{com.sun.xml.bind.v2.model.core.Adapter%1},?,?>)
cons public init({com.sun.xml.bind.v2.model.core.Adapter%1},com.sun.xml.bind.v2.model.nav.Navigator<{com.sun.xml.bind.v2.model.core.Adapter%0},{com.sun.xml.bind.v2.model.core.Adapter%1},?,?>)
fld public final {com.sun.xml.bind.v2.model.core.Adapter%0} customType
fld public final {com.sun.xml.bind.v2.model.core.Adapter%0} defaultType
fld public final {com.sun.xml.bind.v2.model.core.Adapter%1} adapterType
supr java.lang.Object

CLSS public abstract interface com.sun.xml.bind.v2.model.core.ArrayInfo<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.core.ArrayInfo%0},{com.sun.xml.bind.v2.model.core.ArrayInfo%1}>
meth public abstract com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.core.ArrayInfo%0},{com.sun.xml.bind.v2.model.core.ArrayInfo%1}> getItemType()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.AttributePropertyInfo<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.core.NonElementRef<{com.sun.xml.bind.v2.model.core.AttributePropertyInfo%0},{com.sun.xml.bind.v2.model.core.AttributePropertyInfo%1}>
intf com.sun.xml.bind.v2.model.core.PropertyInfo<{com.sun.xml.bind.v2.model.core.AttributePropertyInfo%0},{com.sun.xml.bind.v2.model.core.AttributePropertyInfo%1}>
meth public abstract boolean isRequired()
meth public abstract com.sun.xml.bind.v2.model.core.Adapter<{com.sun.xml.bind.v2.model.core.AttributePropertyInfo%0},{com.sun.xml.bind.v2.model.core.AttributePropertyInfo%1}> getAdapter()
meth public abstract com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.core.AttributePropertyInfo%0},{com.sun.xml.bind.v2.model.core.AttributePropertyInfo%1}> getTarget()
meth public abstract javax.xml.namespace.QName getXmlName()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.BuiltinLeafInfo<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.core.LeafInfo<{com.sun.xml.bind.v2.model.core.BuiltinLeafInfo%0},{com.sun.xml.bind.v2.model.core.BuiltinLeafInfo%1}>
meth public abstract javax.xml.namespace.QName getTypeName()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.ClassInfo<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.core.MaybeElement<{com.sun.xml.bind.v2.model.core.ClassInfo%0},{com.sun.xml.bind.v2.model.core.ClassInfo%1}>
meth public abstract boolean declaresAttributeWildcard()
meth public abstract boolean hasAttributeWildcard()
meth public abstract boolean hasProperties()
meth public abstract boolean hasSubClasses()
meth public abstract boolean hasValueProperty()
meth public abstract boolean inheritsAttributeWildcard()
meth public abstract boolean isAbstract()
meth public abstract boolean isFinal()
meth public abstract boolean isOrdered()
meth public abstract com.sun.xml.bind.v2.model.core.ClassInfo<{com.sun.xml.bind.v2.model.core.ClassInfo%0},{com.sun.xml.bind.v2.model.core.ClassInfo%1}> getBaseClass()
meth public abstract com.sun.xml.bind.v2.model.core.PropertyInfo<{com.sun.xml.bind.v2.model.core.ClassInfo%0},{com.sun.xml.bind.v2.model.core.ClassInfo%1}> getProperty(java.lang.String)
meth public abstract java.lang.String getName()
meth public abstract java.util.List<? extends com.sun.xml.bind.v2.model.core.PropertyInfo<{com.sun.xml.bind.v2.model.core.ClassInfo%0},{com.sun.xml.bind.v2.model.core.ClassInfo%1}>> getProperties()
meth public abstract {com.sun.xml.bind.v2.model.core.ClassInfo%1} getClazz()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.Element<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.core.TypeInfo<{com.sun.xml.bind.v2.model.core.Element%0},{com.sun.xml.bind.v2.model.core.Element%1}>
meth public abstract com.sun.xml.bind.v2.model.core.ClassInfo<{com.sun.xml.bind.v2.model.core.Element%0},{com.sun.xml.bind.v2.model.core.Element%1}> getScope()
meth public abstract com.sun.xml.bind.v2.model.core.Element<{com.sun.xml.bind.v2.model.core.Element%0},{com.sun.xml.bind.v2.model.core.Element%1}> getSubstitutionHead()
meth public abstract javax.xml.namespace.QName getElementName()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.ElementInfo<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.core.Element<{com.sun.xml.bind.v2.model.core.ElementInfo%0},{com.sun.xml.bind.v2.model.core.ElementInfo%1}>
meth public abstract com.sun.xml.bind.v2.model.core.ElementInfo<{com.sun.xml.bind.v2.model.core.ElementInfo%0},{com.sun.xml.bind.v2.model.core.ElementInfo%1}> getSubstitutionHead()
meth public abstract com.sun.xml.bind.v2.model.core.ElementPropertyInfo<{com.sun.xml.bind.v2.model.core.ElementInfo%0},{com.sun.xml.bind.v2.model.core.ElementInfo%1}> getProperty()
meth public abstract com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.core.ElementInfo%0},{com.sun.xml.bind.v2.model.core.ElementInfo%1}> getContentType()
meth public abstract java.util.Collection<? extends com.sun.xml.bind.v2.model.core.ElementInfo<{com.sun.xml.bind.v2.model.core.ElementInfo%0},{com.sun.xml.bind.v2.model.core.ElementInfo%1}>> getSubstitutionMembers()
meth public abstract {com.sun.xml.bind.v2.model.core.ElementInfo%0} getContentInMemoryType()
meth public abstract {com.sun.xml.bind.v2.model.core.ElementInfo%0} getType()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.ElementPropertyInfo<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.core.PropertyInfo<{com.sun.xml.bind.v2.model.core.ElementPropertyInfo%0},{com.sun.xml.bind.v2.model.core.ElementPropertyInfo%1}>
meth public abstract boolean isCollectionNillable()
meth public abstract boolean isCollectionRequired()
meth public abstract boolean isRequired()
meth public abstract boolean isValueList()
meth public abstract com.sun.xml.bind.v2.model.core.Adapter<{com.sun.xml.bind.v2.model.core.ElementPropertyInfo%0},{com.sun.xml.bind.v2.model.core.ElementPropertyInfo%1}> getAdapter()
meth public abstract java.util.List<? extends com.sun.xml.bind.v2.model.core.TypeRef<{com.sun.xml.bind.v2.model.core.ElementPropertyInfo%0},{com.sun.xml.bind.v2.model.core.ElementPropertyInfo%1}>> getTypes()
meth public abstract javax.xml.namespace.QName getXmlName()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.EnumConstant<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract com.sun.xml.bind.v2.model.core.EnumLeafInfo<{com.sun.xml.bind.v2.model.core.EnumConstant%0},{com.sun.xml.bind.v2.model.core.EnumConstant%1}> getEnclosingClass()
meth public abstract java.lang.String getLexicalValue()
meth public abstract java.lang.String getName()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.EnumLeafInfo<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.core.LeafInfo<{com.sun.xml.bind.v2.model.core.EnumLeafInfo%0},{com.sun.xml.bind.v2.model.core.EnumLeafInfo%1}>
meth public abstract com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.core.EnumLeafInfo%0},{com.sun.xml.bind.v2.model.core.EnumLeafInfo%1}> getBaseType()
meth public abstract java.lang.Iterable<? extends com.sun.xml.bind.v2.model.core.EnumConstant> getConstants()
meth public abstract {com.sun.xml.bind.v2.model.core.EnumLeafInfo%1} getClazz()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.ErrorHandler
meth public abstract void error(com.sun.xml.bind.v2.runtime.IllegalAnnotationException)

CLSS public final !enum com.sun.xml.bind.v2.model.core.ID
fld public final static com.sun.xml.bind.v2.model.core.ID ID
fld public final static com.sun.xml.bind.v2.model.core.ID IDREF
fld public final static com.sun.xml.bind.v2.model.core.ID NONE
meth public static com.sun.xml.bind.v2.model.core.ID valueOf(java.lang.String)
meth public static com.sun.xml.bind.v2.model.core.ID[] values()
supr java.lang.Enum<com.sun.xml.bind.v2.model.core.ID>

CLSS public abstract interface com.sun.xml.bind.v2.model.core.LeafInfo<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.core.MaybeElement<{com.sun.xml.bind.v2.model.core.LeafInfo%0},{com.sun.xml.bind.v2.model.core.LeafInfo%1}>

CLSS public abstract interface com.sun.xml.bind.v2.model.core.MapPropertyInfo<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.core.PropertyInfo<{com.sun.xml.bind.v2.model.core.MapPropertyInfo%0},{com.sun.xml.bind.v2.model.core.MapPropertyInfo%1}>
meth public abstract boolean isCollectionNillable()
meth public abstract com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.core.MapPropertyInfo%0},{com.sun.xml.bind.v2.model.core.MapPropertyInfo%1}> getKeyType()
meth public abstract com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.core.MapPropertyInfo%0},{com.sun.xml.bind.v2.model.core.MapPropertyInfo%1}> getValueType()
meth public abstract javax.xml.namespace.QName getXmlName()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.MaybeElement<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.core.MaybeElement%0},{com.sun.xml.bind.v2.model.core.MaybeElement%1}>
meth public abstract boolean isElement()
meth public abstract com.sun.xml.bind.v2.model.core.Element<{com.sun.xml.bind.v2.model.core.MaybeElement%0},{com.sun.xml.bind.v2.model.core.MaybeElement%1}> asElement()
meth public abstract javax.xml.namespace.QName getElementName()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.NonElement<%0 extends java.lang.Object, %1 extends java.lang.Object>
fld public final static javax.xml.namespace.QName ANYTYPE_NAME
intf com.sun.xml.bind.v2.model.core.TypeInfo<{com.sun.xml.bind.v2.model.core.NonElement%0},{com.sun.xml.bind.v2.model.core.NonElement%1}>
meth public abstract boolean isSimpleType()
meth public abstract javax.xml.namespace.QName getTypeName()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.NonElementRef<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.core.NonElementRef%0},{com.sun.xml.bind.v2.model.core.NonElementRef%1}> getTarget()
meth public abstract com.sun.xml.bind.v2.model.core.PropertyInfo<{com.sun.xml.bind.v2.model.core.NonElementRef%0},{com.sun.xml.bind.v2.model.core.NonElementRef%1}> getSource()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.PropertyInfo<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.annotation.AnnotationSource
meth public abstract boolean inlineBinaryData()
meth public abstract boolean isCollection()
meth public abstract com.sun.xml.bind.v2.model.core.Adapter<{com.sun.xml.bind.v2.model.core.PropertyInfo%0},{com.sun.xml.bind.v2.model.core.PropertyInfo%1}> getAdapter()
meth public abstract com.sun.xml.bind.v2.model.core.ID id()
meth public abstract com.sun.xml.bind.v2.model.core.PropertyKind kind()
meth public abstract com.sun.xml.bind.v2.model.core.TypeInfo<{com.sun.xml.bind.v2.model.core.PropertyInfo%0},{com.sun.xml.bind.v2.model.core.PropertyInfo%1}> parent()
meth public abstract java.lang.String displayName()
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection<? extends com.sun.xml.bind.v2.model.core.TypeInfo<{com.sun.xml.bind.v2.model.core.PropertyInfo%0},{com.sun.xml.bind.v2.model.core.PropertyInfo%1}>> ref()
meth public abstract javax.activation.MimeType getExpectedMimeType()
meth public abstract javax.xml.namespace.QName getSchemaType()
 anno 0 com.sun.istack.Nullable()

CLSS public final !enum com.sun.xml.bind.v2.model.core.PropertyKind
fld public final boolean canHaveXmlMimeType
fld public final boolean isOrdered
fld public final int propertyIndex
fld public final static com.sun.xml.bind.v2.model.core.PropertyKind ATTRIBUTE
fld public final static com.sun.xml.bind.v2.model.core.PropertyKind ELEMENT
fld public final static com.sun.xml.bind.v2.model.core.PropertyKind MAP
fld public final static com.sun.xml.bind.v2.model.core.PropertyKind REFERENCE
fld public final static com.sun.xml.bind.v2.model.core.PropertyKind VALUE
meth public static com.sun.xml.bind.v2.model.core.PropertyKind valueOf(java.lang.String)
meth public static com.sun.xml.bind.v2.model.core.PropertyKind[] values()
supr java.lang.Enum<com.sun.xml.bind.v2.model.core.PropertyKind>

CLSS public final com.sun.xml.bind.v2.model.core.Ref<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(com.sun.xml.bind.v2.model.annotation.AnnotationReader<{com.sun.xml.bind.v2.model.core.Ref%0},{com.sun.xml.bind.v2.model.core.Ref%1},?,?>,com.sun.xml.bind.v2.model.nav.Navigator<{com.sun.xml.bind.v2.model.core.Ref%0},{com.sun.xml.bind.v2.model.core.Ref%1},?,?>,{com.sun.xml.bind.v2.model.core.Ref%0},javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter,javax.xml.bind.annotation.XmlList)
cons public init(com.sun.xml.bind.v2.model.impl.ModelBuilderI<{com.sun.xml.bind.v2.model.core.Ref%0},{com.sun.xml.bind.v2.model.core.Ref%1},?,?>,{com.sun.xml.bind.v2.model.core.Ref%0},javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter,javax.xml.bind.annotation.XmlList)
cons public init({com.sun.xml.bind.v2.model.core.Ref%0})
cons public init({com.sun.xml.bind.v2.model.core.Ref%0},com.sun.xml.bind.v2.model.core.Adapter<{com.sun.xml.bind.v2.model.core.Ref%0},{com.sun.xml.bind.v2.model.core.Ref%1}>,boolean)
fld public final boolean valueList
fld public final com.sun.xml.bind.v2.model.core.Adapter<{com.sun.xml.bind.v2.model.core.Ref%0},{com.sun.xml.bind.v2.model.core.Ref%1}> adapter
fld public final {com.sun.xml.bind.v2.model.core.Ref%0} type
supr java.lang.Object

CLSS public abstract interface com.sun.xml.bind.v2.model.core.ReferencePropertyInfo<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.core.PropertyInfo<{com.sun.xml.bind.v2.model.core.ReferencePropertyInfo%0},{com.sun.xml.bind.v2.model.core.ReferencePropertyInfo%1}>
meth public abstract boolean isCollectionNillable()
meth public abstract boolean isCollectionRequired()
meth public abstract boolean isMixed()
meth public abstract boolean isRequired()
meth public abstract com.sun.xml.bind.v2.model.core.Adapter<{com.sun.xml.bind.v2.model.core.ReferencePropertyInfo%0},{com.sun.xml.bind.v2.model.core.ReferencePropertyInfo%1}> getAdapter()
meth public abstract com.sun.xml.bind.v2.model.core.WildcardMode getWildcard()
meth public abstract java.util.Collection<? extends com.sun.xml.bind.v2.model.core.TypeInfo<{com.sun.xml.bind.v2.model.core.ReferencePropertyInfo%0},{com.sun.xml.bind.v2.model.core.ReferencePropertyInfo%1}>> ref()
meth public abstract java.util.Set<? extends com.sun.xml.bind.v2.model.core.Element<{com.sun.xml.bind.v2.model.core.ReferencePropertyInfo%0},{com.sun.xml.bind.v2.model.core.ReferencePropertyInfo%1}>> getElements()
meth public abstract javax.xml.namespace.QName getXmlName()
meth public abstract {com.sun.xml.bind.v2.model.core.ReferencePropertyInfo%1} getDOMHandler()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.RegistryInfo<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract java.util.Set<com.sun.xml.bind.v2.model.core.TypeInfo<{com.sun.xml.bind.v2.model.core.RegistryInfo%0},{com.sun.xml.bind.v2.model.core.RegistryInfo%1}>> getReferences()
meth public abstract {com.sun.xml.bind.v2.model.core.RegistryInfo%1} getClazz()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.TypeInfo<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.annotation.Locatable
meth public abstract boolean canBeReferencedByIDREF()
meth public abstract {com.sun.xml.bind.v2.model.core.TypeInfo%0} getType()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.TypeInfoSet<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object>
meth public abstract com.sun.xml.bind.v2.model.core.ElementInfo<{com.sun.xml.bind.v2.model.core.TypeInfoSet%0},{com.sun.xml.bind.v2.model.core.TypeInfoSet%1}> getElementInfo({com.sun.xml.bind.v2.model.core.TypeInfoSet%1},javax.xml.namespace.QName)
meth public abstract com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.core.TypeInfoSet%0},{com.sun.xml.bind.v2.model.core.TypeInfoSet%1}> getAnyTypeInfo()
meth public abstract com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.core.TypeInfoSet%0},{com.sun.xml.bind.v2.model.core.TypeInfoSet%1}> getClassInfo({com.sun.xml.bind.v2.model.core.TypeInfoSet%1})
meth public abstract com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.core.TypeInfoSet%0},{com.sun.xml.bind.v2.model.core.TypeInfoSet%1}> getTypeInfo(com.sun.xml.bind.v2.model.core.Ref<{com.sun.xml.bind.v2.model.core.TypeInfoSet%0},{com.sun.xml.bind.v2.model.core.TypeInfoSet%1}>)
meth public abstract com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.core.TypeInfoSet%0},{com.sun.xml.bind.v2.model.core.TypeInfoSet%1}> getTypeInfo({com.sun.xml.bind.v2.model.core.TypeInfoSet%0})
meth public abstract com.sun.xml.bind.v2.model.nav.Navigator<{com.sun.xml.bind.v2.model.core.TypeInfoSet%0},{com.sun.xml.bind.v2.model.core.TypeInfoSet%1},{com.sun.xml.bind.v2.model.core.TypeInfoSet%2},{com.sun.xml.bind.v2.model.core.TypeInfoSet%3}> getNavigator()
meth public abstract java.lang.Iterable<? extends com.sun.xml.bind.v2.model.core.ElementInfo<{com.sun.xml.bind.v2.model.core.TypeInfoSet%0},{com.sun.xml.bind.v2.model.core.TypeInfoSet%1}>> getAllElements()
meth public abstract java.util.Map<? extends {com.sun.xml.bind.v2.model.core.TypeInfoSet%0},? extends com.sun.xml.bind.v2.model.core.ArrayInfo<{com.sun.xml.bind.v2.model.core.TypeInfoSet%0},{com.sun.xml.bind.v2.model.core.TypeInfoSet%1}>> arrays()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getSchemaLocations()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getXmlNs(java.lang.String)
meth public abstract java.util.Map<javax.xml.namespace.QName,? extends com.sun.xml.bind.v2.model.core.ElementInfo<{com.sun.xml.bind.v2.model.core.TypeInfoSet%0},{com.sun.xml.bind.v2.model.core.TypeInfoSet%1}>> getElementMappings({com.sun.xml.bind.v2.model.core.TypeInfoSet%1})
meth public abstract java.util.Map<{com.sun.xml.bind.v2.model.core.TypeInfoSet%0},? extends com.sun.xml.bind.v2.model.core.BuiltinLeafInfo<{com.sun.xml.bind.v2.model.core.TypeInfoSet%0},{com.sun.xml.bind.v2.model.core.TypeInfoSet%1}>> builtins()
meth public abstract java.util.Map<{com.sun.xml.bind.v2.model.core.TypeInfoSet%1},? extends com.sun.xml.bind.v2.model.core.ClassInfo<{com.sun.xml.bind.v2.model.core.TypeInfoSet%0},{com.sun.xml.bind.v2.model.core.TypeInfoSet%1}>> beans()
meth public abstract java.util.Map<{com.sun.xml.bind.v2.model.core.TypeInfoSet%1},? extends com.sun.xml.bind.v2.model.core.EnumLeafInfo<{com.sun.xml.bind.v2.model.core.TypeInfoSet%0},{com.sun.xml.bind.v2.model.core.TypeInfoSet%1}>> enums()
meth public abstract javax.xml.bind.annotation.XmlNsForm getAttributeFormDefault(java.lang.String)
meth public abstract javax.xml.bind.annotation.XmlNsForm getElementFormDefault(java.lang.String)
meth public abstract void dump(javax.xml.transform.Result) throws javax.xml.bind.JAXBException

CLSS public abstract interface com.sun.xml.bind.v2.model.core.TypeRef<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.core.NonElementRef<{com.sun.xml.bind.v2.model.core.TypeRef%0},{com.sun.xml.bind.v2.model.core.TypeRef%1}>
meth public abstract boolean isNillable()
meth public abstract java.lang.String getDefaultValue()
meth public abstract javax.xml.namespace.QName getTagName()

CLSS public abstract interface com.sun.xml.bind.v2.model.core.ValuePropertyInfo<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.core.NonElementRef<{com.sun.xml.bind.v2.model.core.ValuePropertyInfo%0},{com.sun.xml.bind.v2.model.core.ValuePropertyInfo%1}>
intf com.sun.xml.bind.v2.model.core.PropertyInfo<{com.sun.xml.bind.v2.model.core.ValuePropertyInfo%0},{com.sun.xml.bind.v2.model.core.ValuePropertyInfo%1}>
meth public abstract com.sun.xml.bind.v2.model.core.Adapter<{com.sun.xml.bind.v2.model.core.ValuePropertyInfo%0},{com.sun.xml.bind.v2.model.core.ValuePropertyInfo%1}> getAdapter()

CLSS public final !enum com.sun.xml.bind.v2.model.core.WildcardMode
fld public final boolean allowDom
fld public final boolean allowTypedObject
fld public final static com.sun.xml.bind.v2.model.core.WildcardMode LAX
fld public final static com.sun.xml.bind.v2.model.core.WildcardMode SKIP
fld public final static com.sun.xml.bind.v2.model.core.WildcardMode STRICT
meth public static com.sun.xml.bind.v2.model.core.WildcardMode valueOf(java.lang.String)
meth public static com.sun.xml.bind.v2.model.core.WildcardMode[] values()
supr java.lang.Enum<com.sun.xml.bind.v2.model.core.WildcardMode>

CLSS public abstract interface com.sun.xml.bind.v2.model.core.WildcardTypeInfo<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf com.sun.xml.bind.v2.model.core.TypeInfo<{com.sun.xml.bind.v2.model.core.WildcardTypeInfo%0},{com.sun.xml.bind.v2.model.core.WildcardTypeInfo%1}>

CLSS abstract interface com.sun.xml.bind.v2.model.core.package-info

CLSS public com.sun.xml.bind.v2.model.impl.ArrayInfoImpl<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object>
cons public init(com.sun.xml.bind.v2.model.impl.ModelBuilder<{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%3}>,com.sun.xml.bind.v2.model.annotation.Locatable,{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%0})
fld protected com.sun.xml.bind.v2.model.impl.ModelBuilder<{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%3}> builder
fld protected final com.sun.xml.bind.v2.model.impl.TypeInfoSetImpl<{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%3}> owner
intf com.sun.xml.bind.v2.model.core.ArrayInfo<{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%1}>
intf com.sun.xml.bind.v2.runtime.Location
meth protected final com.sun.xml.bind.v2.model.annotation.AnnotationReader<{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%3}> reader()
meth protected final com.sun.xml.bind.v2.model.nav.Navigator<{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%3}> nav()
meth protected final javax.xml.namespace.QName parseElementName({com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%1})
meth protected final javax.xml.namespace.QName parseTypeName({com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%1})
meth protected final javax.xml.namespace.QName parseTypeName({com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%1},javax.xml.bind.annotation.XmlType)
meth public boolean isSimpleType()
meth public com.sun.xml.bind.v2.model.annotation.Locatable getUpstream()
meth public com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%1}> getItemType()
meth public com.sun.xml.bind.v2.runtime.Location getLocation()
meth public final boolean canBeReferencedByIDREF()
meth public java.lang.String toString()
meth public javax.xml.namespace.QName getTypeName()
meth public {com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%0} getType()
supr com.sun.xml.bind.v2.model.impl.TypeInfoImpl<{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ArrayInfoImpl%3}>
hfds arrayType,itemType,typeName

CLSS public com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected !varargs init({com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl%0},javax.xml.namespace.QName[])
intf com.sun.xml.bind.v2.model.core.BuiltinLeafInfo<{com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl%0},{com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl%1}>
intf com.sun.xml.bind.v2.model.core.LeafInfo<{com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl%0},{com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl%1}>
intf com.sun.xml.bind.v2.runtime.Location
meth public boolean isSimpleType()
meth public com.sun.xml.bind.v2.model.annotation.Locatable getUpstream()
meth public com.sun.xml.bind.v2.runtime.Location getLocation()
meth public final boolean canBeReferencedByIDREF()
meth public final boolean isElement()
meth public final com.sun.xml.bind.v2.model.core.Element<{com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl%0},{com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl%1}> asElement()
meth public final javax.xml.namespace.QName getElementName()
meth public final javax.xml.namespace.QName[] getTypeNames()
meth public java.lang.String toString()
meth public javax.xml.namespace.QName getTypeName()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl<{%%0},{%%1}>> createLeaves(com.sun.xml.bind.v2.model.nav.Navigator<{%%0},{%%1},?,?>)
meth public {com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl%0} getType()
supr java.lang.Object<{com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl%0},{com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl%1}>
hfds typeNames

CLSS public com.sun.xml.bind.v2.model.impl.ClassInfoImpl<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object>
fld protected com.sun.xml.bind.v2.model.impl.ModelBuilder<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}> builder
fld protected com.sun.xml.bind.v2.model.impl.PropertySeed<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}> attributeWildcard
fld protected final com.sun.xml.bind.v2.model.impl.TypeInfoSetImpl<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}> owner
fld protected final {com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1} clazz
intf com.sun.xml.bind.v2.model.core.ClassInfo<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1}>
intf com.sun.xml.bind.v2.model.core.Element<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1}>
meth protected com.sun.xml.bind.v2.model.impl.AttributePropertyInfoImpl<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}> createAttributeProperty(com.sun.xml.bind.v2.model.impl.PropertySeed<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}>)
meth protected com.sun.xml.bind.v2.model.impl.ElementPropertyInfoImpl<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}> createElementProperty(com.sun.xml.bind.v2.model.impl.PropertySeed<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}>)
meth protected com.sun.xml.bind.v2.model.impl.MapPropertyInfoImpl<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}> createMapProperty(com.sun.xml.bind.v2.model.impl.PropertySeed<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}>)
meth protected com.sun.xml.bind.v2.model.impl.PropertySeed<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}> createAccessorSeed({com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3})
meth protected com.sun.xml.bind.v2.model.impl.PropertySeed<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}> createFieldSeed({com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2})
meth protected com.sun.xml.bind.v2.model.impl.ReferencePropertyInfoImpl<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}> createReferenceProperty(com.sun.xml.bind.v2.model.impl.PropertySeed<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}>)
meth protected com.sun.xml.bind.v2.model.impl.ValuePropertyInfoImpl<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}> createValueProperty(com.sun.xml.bind.v2.model.impl.PropertySeed<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}>)
meth protected final com.sun.xml.bind.v2.model.annotation.AnnotationReader<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}> reader()
meth protected final com.sun.xml.bind.v2.model.nav.Navigator<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}> nav()
meth protected final javax.xml.namespace.QName parseElementName({com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1})
meth protected final javax.xml.namespace.QName parseTypeName({com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1})
meth protected final javax.xml.namespace.QName parseTypeName({com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},javax.xml.bind.annotation.XmlType)
meth protected void checkFieldXmlLocation({com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2})
meth public <%0 extends java.lang.annotation.Annotation> {%%0} readAnnotation(java.lang.Class<{%%0}>)
meth public boolean canBeReferencedByIDREF()
meth public boolean hasProperties()
meth public boolean isAbstract()
meth public boolean isOrdered()
meth public com.sun.xml.bind.v2.model.annotation.Locatable getUpstream()
meth public com.sun.xml.bind.v2.model.core.Element<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1}> asElement()
meth public com.sun.xml.bind.v2.model.core.PropertyInfo<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1}> getProperty(java.lang.String)
meth public com.sun.xml.bind.v2.model.impl.ClassInfoImpl<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}> getBaseClass()
meth public com.sun.xml.bind.v2.model.impl.ClassInfoImpl<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}> getScope()
meth public com.sun.xml.bind.v2.runtime.Location getLocation()
meth public final boolean declaresAttributeWildcard()
meth public final boolean hasAttributeWildcard()
meth public final boolean hasSubClasses()
meth public final boolean hasValueProperty()
meth public final boolean inheritsAttributeWildcard()
meth public final boolean isElement()
meth public final boolean isFinal()
meth public final boolean isSimpleType()
meth public final com.sun.xml.bind.v2.model.core.Element<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1}> getSubstitutionHead()
meth public final java.lang.String getName()
meth public final javax.xml.namespace.QName getElementName()
meth public final javax.xml.namespace.QName getTypeName()
meth public final {com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0} getType()
meth public final {com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1} getClazz()
meth public java.lang.String toString()
meth public java.lang.reflect.Method getFactoryMethod()
meth public java.util.List<? extends com.sun.xml.bind.v2.model.core.PropertyInfo<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1}>> getProperties()
supr com.sun.xml.bind.v2.model.impl.TypeInfoImpl<{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%0},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%1},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%2},{com.sun.xml.bind.v2.model.impl.ClassInfoImpl%3}>
hfds ANNOTATION_NUMBER_MAP,DEFAULT_ORDER,EMPTY_ANNOTATIONS,SECONDARY_ANNOTATIONS,baseClass,baseClassComputed,elementName,factoryMethod,hasSubClasses,propOrder,properties,typeName
hcls ConflictException,DuplicateException,PropertyGroup,PropertySorter,SecondaryAnnotation

CLSS public abstract interface com.sun.xml.bind.v2.model.impl.DummyPropertyInfo<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object>
meth public abstract void addType(com.sun.xml.bind.v2.model.impl.PropertyInfoImpl<{com.sun.xml.bind.v2.model.impl.DummyPropertyInfo%0},{com.sun.xml.bind.v2.model.impl.DummyPropertyInfo%1},{com.sun.xml.bind.v2.model.impl.DummyPropertyInfo%2},{com.sun.xml.bind.v2.model.impl.DummyPropertyInfo%3}>)

CLSS public com.sun.xml.bind.v2.model.impl.ModelBuilder<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object>
cons public init(com.sun.xml.bind.v2.model.annotation.AnnotationReader<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1},{com.sun.xml.bind.v2.model.impl.ModelBuilder%2},{com.sun.xml.bind.v2.model.impl.ModelBuilder%3}>,com.sun.xml.bind.v2.model.nav.Navigator<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1},{com.sun.xml.bind.v2.model.impl.ModelBuilder%2},{com.sun.xml.bind.v2.model.impl.ModelBuilder%3}>,java.util.Map<{com.sun.xml.bind.v2.model.impl.ModelBuilder%1},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1}>,java.lang.String)
fld public boolean hasSwaRef
fld public final com.sun.xml.bind.v2.model.annotation.AnnotationReader<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1},{com.sun.xml.bind.v2.model.impl.ModelBuilder%2},{com.sun.xml.bind.v2.model.impl.ModelBuilder%3}> reader
fld public final com.sun.xml.bind.v2.model.nav.Navigator<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1},{com.sun.xml.bind.v2.model.impl.ModelBuilder%2},{com.sun.xml.bind.v2.model.impl.ModelBuilder%3}> nav
fld public final java.lang.String defaultNsUri
intf com.sun.xml.bind.v2.model.impl.ModelBuilderI<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1},{com.sun.xml.bind.v2.model.impl.ModelBuilder%2},{com.sun.xml.bind.v2.model.impl.ModelBuilder%3}>
meth protected com.sun.xml.bind.v2.model.impl.ArrayInfoImpl<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1},{com.sun.xml.bind.v2.model.impl.ModelBuilder%2},{com.sun.xml.bind.v2.model.impl.ModelBuilder%3}> createArrayInfo(com.sun.xml.bind.v2.model.annotation.Locatable,{com.sun.xml.bind.v2.model.impl.ModelBuilder%0})
meth protected com.sun.xml.bind.v2.model.impl.ClassInfoImpl<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1},{com.sun.xml.bind.v2.model.impl.ModelBuilder%2},{com.sun.xml.bind.v2.model.impl.ModelBuilder%3}> createClassInfo({com.sun.xml.bind.v2.model.impl.ModelBuilder%1},com.sun.xml.bind.v2.model.annotation.Locatable)
meth protected com.sun.xml.bind.v2.model.impl.ElementInfoImpl<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1},{com.sun.xml.bind.v2.model.impl.ModelBuilder%2},{com.sun.xml.bind.v2.model.impl.ModelBuilder%3}> createElementInfo(com.sun.xml.bind.v2.model.impl.RegistryInfoImpl<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1},{com.sun.xml.bind.v2.model.impl.ModelBuilder%2},{com.sun.xml.bind.v2.model.impl.ModelBuilder%3}>,{com.sun.xml.bind.v2.model.impl.ModelBuilder%3}) throws com.sun.xml.bind.v2.runtime.IllegalAnnotationException
meth protected com.sun.xml.bind.v2.model.impl.EnumLeafInfoImpl<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1},{com.sun.xml.bind.v2.model.impl.ModelBuilder%2},{com.sun.xml.bind.v2.model.impl.ModelBuilder%3}> createEnumLeafInfo({com.sun.xml.bind.v2.model.impl.ModelBuilder%1},com.sun.xml.bind.v2.model.annotation.Locatable)
meth protected com.sun.xml.bind.v2.model.impl.TypeInfoSetImpl<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1},{com.sun.xml.bind.v2.model.impl.ModelBuilder%2},{com.sun.xml.bind.v2.model.impl.ModelBuilder%3}> createTypeInfoSet()
meth public boolean isReplaced({com.sun.xml.bind.v2.model.impl.ModelBuilder%1})
meth public com.sun.xml.bind.v2.model.annotation.AnnotationReader<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1},{com.sun.xml.bind.v2.model.impl.ModelBuilder%2},{com.sun.xml.bind.v2.model.impl.ModelBuilder%3}> getReader()
meth public com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1}> getClassInfo({com.sun.xml.bind.v2.model.impl.ModelBuilder%1},boolean,com.sun.xml.bind.v2.model.annotation.Locatable)
meth public com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1}> getClassInfo({com.sun.xml.bind.v2.model.impl.ModelBuilder%1},com.sun.xml.bind.v2.model.annotation.Locatable)
meth public com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1}> getTypeInfo(com.sun.xml.bind.v2.model.core.Ref<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1}>)
meth public com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1}> getTypeInfo({com.sun.xml.bind.v2.model.impl.ModelBuilder%0},com.sun.xml.bind.v2.model.annotation.Locatable)
meth public com.sun.xml.bind.v2.model.core.RegistryInfo<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1}> addRegistry({com.sun.xml.bind.v2.model.impl.ModelBuilder%1},com.sun.xml.bind.v2.model.annotation.Locatable)
meth public com.sun.xml.bind.v2.model.core.RegistryInfo<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1}> getRegistry(java.lang.String)
meth public com.sun.xml.bind.v2.model.core.TypeInfoSet<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1},{com.sun.xml.bind.v2.model.impl.ModelBuilder%2},{com.sun.xml.bind.v2.model.impl.ModelBuilder%3}> link()
meth public com.sun.xml.bind.v2.model.nav.Navigator<{com.sun.xml.bind.v2.model.impl.ModelBuilder%0},{com.sun.xml.bind.v2.model.impl.ModelBuilder%1},{com.sun.xml.bind.v2.model.impl.ModelBuilder%2},{com.sun.xml.bind.v2.model.impl.ModelBuilder%3}> getNavigator()
meth public final void reportError(com.sun.xml.bind.v2.runtime.IllegalAnnotationException)
meth public void setErrorHandler(com.sun.xml.bind.v2.model.core.ErrorHandler)
supr java.lang.Object
hfds errorHandler,hadError,linked,logger,proxyErrorHandler,registries,subclassReplacements,typeInfoSet,typeNames

CLSS public abstract interface com.sun.xml.bind.v2.model.impl.ModelBuilderI<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object>
meth public abstract com.sun.xml.bind.v2.model.annotation.AnnotationReader<{com.sun.xml.bind.v2.model.impl.ModelBuilderI%0},{com.sun.xml.bind.v2.model.impl.ModelBuilderI%1},{com.sun.xml.bind.v2.model.impl.ModelBuilderI%2},{com.sun.xml.bind.v2.model.impl.ModelBuilderI%3}> getReader()
meth public abstract com.sun.xml.bind.v2.model.nav.Navigator<{com.sun.xml.bind.v2.model.impl.ModelBuilderI%0},{com.sun.xml.bind.v2.model.impl.ModelBuilderI%1},{com.sun.xml.bind.v2.model.impl.ModelBuilderI%2},{com.sun.xml.bind.v2.model.impl.ModelBuilderI%3}> getNavigator()

CLSS public abstract com.sun.xml.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl<%0 extends java.lang.Object>
fld public final static com.sun.xml.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl<java.lang.String> STRING
fld public final static java.lang.String MAP_ANYURI_TO_URI = "mapAnyUriToUri"
fld public final static java.lang.String USE_OLD_GMONTH_MAPPING = "jaxb.ri.useOldGmonthMapping"
fld public final static java.util.List<com.sun.xml.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl<?>> builtinBeanInfos
fld public final static java.util.Map<java.lang.reflect.Type,com.sun.xml.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl<?>> LEAVES
intf com.sun.xml.bind.v2.model.runtime.RuntimeBuiltinLeafInfo
intf com.sun.xml.bind.v2.runtime.Transducer<{com.sun.xml.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl%0}>
meth public boolean useNamespace()
meth public final com.sun.xml.bind.v2.runtime.Transducer getTransducer()
meth public final java.lang.Class getClazz()
meth public javax.xml.namespace.QName getTypeName({com.sun.xml.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl%0})
meth public void declareNamespace({com.sun.xml.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws com.sun.xml.bind.api.AccessorException
supr com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl<java.lang.reflect.Type,java.lang.Class>
hfds DATE,logger,xmlGregorianCalendarFieldRef,xmlGregorianCalendarFormatString
hcls PcdataImpl,StringImpl,StringImplImpl,UUIDImpl

CLSS public com.sun.xml.bind.v2.model.impl.RuntimeModelBuilder
cons public init(com.sun.xml.bind.v2.runtime.JAXBContextImpl,com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader,java.util.Map<java.lang.Class,java.lang.Class>,java.lang.String)
fld public final com.sun.xml.bind.v2.runtime.JAXBContextImpl context
 anno 0 com.sun.istack.Nullable()
meth protected com.sun.xml.bind.v2.model.core.TypeInfoSet createTypeInfoSet()
meth protected com.sun.xml.bind.v2.model.impl.RuntimeClassInfoImpl createClassInfo(java.lang.Class,com.sun.xml.bind.v2.model.annotation.Locatable)
meth protected com.sun.xml.bind.v2.model.impl.RuntimeEnumLeafInfoImpl createEnumLeafInfo(java.lang.Class,com.sun.xml.bind.v2.model.annotation.Locatable)
meth public com.sun.xml.bind.v2.model.impl.RuntimeArrayInfoImpl createArrayInfo(com.sun.xml.bind.v2.model.annotation.Locatable,java.lang.reflect.Type)
meth public com.sun.xml.bind.v2.model.runtime.RuntimeElementInfo createElementInfo(com.sun.xml.bind.v2.model.impl.RegistryInfoImpl<java.lang.reflect.Type,java.lang.Class,java.lang.reflect.Field,java.lang.reflect.Method>,java.lang.reflect.Method) throws com.sun.xml.bind.v2.runtime.IllegalAnnotationException
meth public com.sun.xml.bind.v2.model.runtime.RuntimeNonElement getClassInfo(java.lang.Class,boolean,com.sun.xml.bind.v2.model.annotation.Locatable)
meth public com.sun.xml.bind.v2.model.runtime.RuntimeNonElement getClassInfo(java.lang.Class,com.sun.xml.bind.v2.model.annotation.Locatable)
meth public com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet link()
meth public static com.sun.xml.bind.v2.runtime.Transducer createTransducer(com.sun.xml.bind.v2.model.runtime.RuntimeNonElementRef)
supr com.sun.xml.bind.v2.model.impl.ModelBuilder<java.lang.reflect.Type,java.lang.Class,java.lang.reflect.Field,java.lang.reflect.Method>
hcls IDTransducerImpl

CLSS public abstract interface com.sun.xml.bind.v2.model.nav.Navigator<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object>
meth public abstract <%0 extends java.lang.Object> {com.sun.xml.bind.v2.model.nav.Navigator%0} erasure({com.sun.xml.bind.v2.model.nav.Navigator%0})
meth public abstract boolean hasDefaultConstructor({com.sun.xml.bind.v2.model.nav.Navigator%1})
meth public abstract boolean isAbstract({com.sun.xml.bind.v2.model.nav.Navigator%1})
meth public abstract boolean isArray({com.sun.xml.bind.v2.model.nav.Navigator%0})
meth public abstract boolean isArrayButNotByteArray({com.sun.xml.bind.v2.model.nav.Navigator%0})
meth public abstract boolean isBridgeMethod({com.sun.xml.bind.v2.model.nav.Navigator%3})
meth public abstract boolean isEnum({com.sun.xml.bind.v2.model.nav.Navigator%1})
meth public abstract boolean isFinal({com.sun.xml.bind.v2.model.nav.Navigator%1})
meth public abstract boolean isFinalMethod({com.sun.xml.bind.v2.model.nav.Navigator%3})
meth public abstract boolean isInnerClass({com.sun.xml.bind.v2.model.nav.Navigator%1})
meth public abstract boolean isInterface({com.sun.xml.bind.v2.model.nav.Navigator%1})
meth public abstract boolean isOverriding({com.sun.xml.bind.v2.model.nav.Navigator%3},{com.sun.xml.bind.v2.model.nav.Navigator%1})
meth public abstract boolean isParameterizedType({com.sun.xml.bind.v2.model.nav.Navigator%0})
meth public abstract boolean isPrimitive({com.sun.xml.bind.v2.model.nav.Navigator%0})
meth public abstract boolean isPublicField({com.sun.xml.bind.v2.model.nav.Navigator%2})
meth public abstract boolean isPublicMethod({com.sun.xml.bind.v2.model.nav.Navigator%3})
meth public abstract boolean isSameType({com.sun.xml.bind.v2.model.nav.Navigator%0},{com.sun.xml.bind.v2.model.nav.Navigator%0})
meth public abstract boolean isStaticField({com.sun.xml.bind.v2.model.nav.Navigator%2})
meth public abstract boolean isStaticMethod({com.sun.xml.bind.v2.model.nav.Navigator%3})
meth public abstract boolean isSubClassOf({com.sun.xml.bind.v2.model.nav.Navigator%0},{com.sun.xml.bind.v2.model.nav.Navigator%0})
meth public abstract boolean isTransient({com.sun.xml.bind.v2.model.nav.Navigator%2})
meth public abstract com.sun.xml.bind.v2.runtime.Location getClassLocation({com.sun.xml.bind.v2.model.nav.Navigator%1})
meth public abstract com.sun.xml.bind.v2.runtime.Location getFieldLocation({com.sun.xml.bind.v2.model.nav.Navigator%2})
meth public abstract com.sun.xml.bind.v2.runtime.Location getMethodLocation({com.sun.xml.bind.v2.model.nav.Navigator%3})
meth public abstract java.lang.String getClassName({com.sun.xml.bind.v2.model.nav.Navigator%1})
meth public abstract java.lang.String getClassShortName({com.sun.xml.bind.v2.model.nav.Navigator%1})
meth public abstract java.lang.String getFieldName({com.sun.xml.bind.v2.model.nav.Navigator%2})
meth public abstract java.lang.String getMethodName({com.sun.xml.bind.v2.model.nav.Navigator%3})
meth public abstract java.lang.String getPackageName({com.sun.xml.bind.v2.model.nav.Navigator%1})
meth public abstract java.lang.String getTypeName({com.sun.xml.bind.v2.model.nav.Navigator%0})
meth public abstract java.util.Collection<? extends {com.sun.xml.bind.v2.model.nav.Navigator%2}> getDeclaredFields({com.sun.xml.bind.v2.model.nav.Navigator%1})
meth public abstract java.util.Collection<? extends {com.sun.xml.bind.v2.model.nav.Navigator%3}> getDeclaredMethods({com.sun.xml.bind.v2.model.nav.Navigator%1})
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%0} getBaseClass({com.sun.xml.bind.v2.model.nav.Navigator%0},{com.sun.xml.bind.v2.model.nav.Navigator%1})
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%0} getComponentType({com.sun.xml.bind.v2.model.nav.Navigator%0})
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%0} getFieldType({com.sun.xml.bind.v2.model.nav.Navigator%2})
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%0} getPrimitive(java.lang.Class)
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%0} getReturnType({com.sun.xml.bind.v2.model.nav.Navigator%3})
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%0} getTypeArgument({com.sun.xml.bind.v2.model.nav.Navigator%0},int)
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%0} getVoidType()
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%0} ref(java.lang.Class)
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%0} use({com.sun.xml.bind.v2.model.nav.Navigator%1})
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%0}[] getMethodParameters({com.sun.xml.bind.v2.model.nav.Navigator%3})
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%1} asDecl(java.lang.Class)
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%1} asDecl({com.sun.xml.bind.v2.model.nav.Navigator%0})
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%1} getDeclaringClassForField({com.sun.xml.bind.v2.model.nav.Navigator%2})
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%1} getDeclaringClassForMethod({com.sun.xml.bind.v2.model.nav.Navigator%3})
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%1} getSuperClass({com.sun.xml.bind.v2.model.nav.Navigator%1})
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%1} loadObjectFactory({com.sun.xml.bind.v2.model.nav.Navigator%1},java.lang.String)
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%2} getDeclaredField({com.sun.xml.bind.v2.model.nav.Navigator%1},java.lang.String)
meth public abstract {com.sun.xml.bind.v2.model.nav.Navigator%2}[] getEnumConstants({com.sun.xml.bind.v2.model.nav.Navigator%1})

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeArrayInfo
intf com.sun.xml.bind.v2.model.core.ArrayInfo<java.lang.reflect.Type,java.lang.Class>
intf com.sun.xml.bind.v2.model.runtime.RuntimeNonElement
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimeNonElement getItemType()
meth public abstract java.lang.Class getType()

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeAttributePropertyInfo
intf com.sun.xml.bind.v2.model.core.AttributePropertyInfo<java.lang.reflect.Type,java.lang.Class>
intf com.sun.xml.bind.v2.model.runtime.RuntimeNonElementRef
intf com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimeNonElement getTarget()

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeBuiltinLeafInfo
intf com.sun.xml.bind.v2.model.core.BuiltinLeafInfo<java.lang.reflect.Type,java.lang.Class>
intf com.sun.xml.bind.v2.model.runtime.RuntimeLeafInfo

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo
intf com.sun.xml.bind.v2.model.core.ClassInfo<java.lang.reflect.Type,java.lang.Class>
intf com.sun.xml.bind.v2.model.runtime.RuntimeNonElement
meth public abstract <%0 extends java.lang.Object> com.sun.xml.bind.v2.runtime.reflect.Accessor<{%%0},java.util.Map<javax.xml.namespace.QName,java.lang.String>> getAttributeWildcard()
meth public abstract <%0 extends java.lang.Object> com.sun.xml.bind.v2.runtime.reflect.Accessor<{%%0},org.xml.sax.Locator> getLocatorField()
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo getBaseClass()
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo getProperty(java.lang.String)
meth public abstract java.lang.reflect.Method getFactoryMethod()
meth public abstract java.util.List<? extends com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo> getProperties()

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeElement
intf com.sun.xml.bind.v2.model.core.Element<java.lang.reflect.Type,java.lang.Class>
intf com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeElementInfo
intf com.sun.xml.bind.v2.model.core.ElementInfo<java.lang.reflect.Type,java.lang.Class>
intf com.sun.xml.bind.v2.model.runtime.RuntimeElement
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo getScope()
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo getProperty()
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimeNonElement getContentType()
meth public abstract java.lang.Class<? extends javax.xml.bind.JAXBElement> getType()

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo
intf com.sun.xml.bind.v2.model.core.ElementPropertyInfo<java.lang.reflect.Type,java.lang.Class>
intf com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo
meth public abstract java.util.Collection<? extends com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo> ref()
meth public abstract java.util.List<? extends com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef> getTypes()

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeEnumLeafInfo
intf com.sun.xml.bind.v2.model.core.EnumLeafInfo<java.lang.reflect.Type,java.lang.Class>
intf com.sun.xml.bind.v2.model.runtime.RuntimeLeafInfo

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeLeafInfo
intf com.sun.xml.bind.v2.model.core.LeafInfo<java.lang.reflect.Type,java.lang.Class>
intf com.sun.xml.bind.v2.model.runtime.RuntimeNonElement
meth public abstract <%0 extends java.lang.Object> com.sun.xml.bind.v2.runtime.Transducer<{%%0}> getTransducer()
meth public abstract java.lang.Class getClazz()
meth public abstract javax.xml.namespace.QName[] getTypeNames()

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeMapPropertyInfo
intf com.sun.xml.bind.v2.model.core.MapPropertyInfo<java.lang.reflect.Type,java.lang.Class>
intf com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimeNonElement getKeyType()
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimeNonElement getValueType()

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeNonElement
intf com.sun.xml.bind.v2.model.core.NonElement<java.lang.reflect.Type,java.lang.Class>
intf com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo
meth public abstract <%0 extends java.lang.Object> com.sun.xml.bind.v2.runtime.Transducer<{%%0}> getTransducer()

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeNonElementRef
intf com.sun.xml.bind.v2.model.core.NonElementRef<java.lang.reflect.Type,java.lang.Class>
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimeNonElement getTarget()
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo getSource()
meth public abstract com.sun.xml.bind.v2.runtime.Transducer getTransducer()

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo
intf com.sun.xml.bind.v2.model.core.PropertyInfo<java.lang.reflect.Type,java.lang.Class>
meth public abstract boolean elementOnlyContent()
meth public abstract com.sun.xml.bind.v2.runtime.reflect.Accessor getAccessor()
meth public abstract java.lang.reflect.Type getIndividualType()
meth public abstract java.lang.reflect.Type getRawType()
meth public abstract java.util.Collection<? extends com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo> ref()

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeReferencePropertyInfo
intf com.sun.xml.bind.v2.model.core.ReferencePropertyInfo<java.lang.reflect.Type,java.lang.Class>
intf com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo
meth public abstract java.util.Set<? extends com.sun.xml.bind.v2.model.runtime.RuntimeElement> getElements()

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo
intf com.sun.xml.bind.v2.model.core.TypeInfo<java.lang.reflect.Type,java.lang.Class>

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet
intf com.sun.xml.bind.v2.model.core.TypeInfoSet<java.lang.reflect.Type,java.lang.Class,java.lang.reflect.Field,java.lang.reflect.Method>
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimeElementInfo getElementInfo(java.lang.Class,javax.xml.namespace.QName)
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimeNonElement getAnyTypeInfo()
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimeNonElement getClassInfo(java.lang.Class)
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimeNonElement getTypeInfo(java.lang.reflect.Type)
meth public abstract java.lang.Iterable<? extends com.sun.xml.bind.v2.model.runtime.RuntimeElementInfo> getAllElements()
meth public abstract java.util.Map<java.lang.Class,? extends com.sun.xml.bind.v2.model.runtime.RuntimeArrayInfo> arrays()
meth public abstract java.util.Map<java.lang.Class,? extends com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo> beans()
meth public abstract java.util.Map<java.lang.Class,? extends com.sun.xml.bind.v2.model.runtime.RuntimeEnumLeafInfo> enums()
meth public abstract java.util.Map<java.lang.reflect.Type,? extends com.sun.xml.bind.v2.model.runtime.RuntimeBuiltinLeafInfo> builtins()
meth public abstract java.util.Map<javax.xml.namespace.QName,? extends com.sun.xml.bind.v2.model.runtime.RuntimeElementInfo> getElementMappings(java.lang.Class)

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeTypeRef
intf com.sun.xml.bind.v2.model.core.TypeRef<java.lang.reflect.Type,java.lang.Class>
intf com.sun.xml.bind.v2.model.runtime.RuntimeNonElementRef
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimeNonElement getTarget()
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo getSource()

CLSS public abstract interface com.sun.xml.bind.v2.model.runtime.RuntimeValuePropertyInfo
intf com.sun.xml.bind.v2.model.core.ValuePropertyInfo<java.lang.reflect.Type,java.lang.Class>
intf com.sun.xml.bind.v2.model.runtime.RuntimeNonElementRef
intf com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimeNonElement getTarget()

CLSS abstract interface com.sun.xml.bind.v2.model.runtime.package-info

CLSS public final com.sun.xml.bind.v2.runtime.AssociationMap<%0 extends java.lang.Object>
cons public init()
meth public com.sun.xml.bind.v2.runtime.AssociationMap$Entry<{com.sun.xml.bind.v2.runtime.AssociationMap%0}> byElement(java.lang.Object)
meth public com.sun.xml.bind.v2.runtime.AssociationMap$Entry<{com.sun.xml.bind.v2.runtime.AssociationMap%0}> byPeer(java.lang.Object)
meth public java.lang.Object getInnerPeer({com.sun.xml.bind.v2.runtime.AssociationMap%0})
meth public java.lang.Object getOuterPeer({com.sun.xml.bind.v2.runtime.AssociationMap%0})
meth public void addInner({com.sun.xml.bind.v2.runtime.AssociationMap%0},java.lang.Object)
meth public void addOuter({com.sun.xml.bind.v2.runtime.AssociationMap%0},java.lang.Object)
meth public void addUsed({com.sun.xml.bind.v2.runtime.AssociationMap%0})
supr java.lang.Object
hfds byElement,byPeer,usedNodes
hcls Entry

CLSS public abstract interface com.sun.xml.bind.v2.runtime.AttributeAccessor<%0 extends java.lang.Object>
meth public abstract boolean isNilIncluded()

CLSS public com.sun.xml.bind.v2.runtime.BinderImpl<%0 extends java.lang.Object>
meth public <%0 extends java.lang.Object> javax.xml.bind.JAXBElement<{%%0}> unmarshal({com.sun.xml.bind.v2.runtime.BinderImpl%0},java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth public java.lang.Object getJAXBNode({com.sun.xml.bind.v2.runtime.BinderImpl%0})
meth public java.lang.Object getProperty(java.lang.String) throws javax.xml.bind.PropertyException
meth public java.lang.Object unmarshal({com.sun.xml.bind.v2.runtime.BinderImpl%0}) throws javax.xml.bind.JAXBException
meth public java.lang.Object updateJAXB({com.sun.xml.bind.v2.runtime.BinderImpl%0}) throws javax.xml.bind.JAXBException
meth public javax.xml.bind.ValidationEventHandler getEventHandler()
meth public javax.xml.validation.Schema getSchema()
meth public void marshal(java.lang.Object,{com.sun.xml.bind.v2.runtime.BinderImpl%0}) throws javax.xml.bind.JAXBException
meth public void setEventHandler(javax.xml.bind.ValidationEventHandler) throws javax.xml.bind.JAXBException
meth public void setProperty(java.lang.String,java.lang.Object) throws javax.xml.bind.PropertyException
meth public void setSchema(javax.xml.validation.Schema)
meth public {com.sun.xml.bind.v2.runtime.BinderImpl%0} getXMLNode(java.lang.Object)
meth public {com.sun.xml.bind.v2.runtime.BinderImpl%0} updateXML(java.lang.Object) throws javax.xml.bind.JAXBException
meth public {com.sun.xml.bind.v2.runtime.BinderImpl%0} updateXML(java.lang.Object,{com.sun.xml.bind.v2.runtime.BinderImpl%0}) throws javax.xml.bind.JAXBException
supr javax.xml.bind.Binder<{com.sun.xml.bind.v2.runtime.BinderImpl%0}>
hfds assoc,context,marshaller,scanner,unmarshaller

CLSS public final com.sun.xml.bind.v2.runtime.BridgeContextImpl
fld public final com.sun.xml.bind.v2.runtime.MarshallerImpl marshaller
fld public final com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl unmarshaller
meth public javax.xml.bind.attachment.AttachmentMarshaller getAttachmentMarshaller()
meth public javax.xml.bind.attachment.AttachmentUnmarshaller getAttachmentUnmarshaller()
meth public void setAttachmentMarshaller(javax.xml.bind.attachment.AttachmentMarshaller)
meth public void setAttachmentUnmarshaller(javax.xml.bind.attachment.AttachmentUnmarshaller)
meth public void setErrorHandler(javax.xml.bind.ValidationEventHandler)
supr com.sun.xml.bind.api.BridgeContext

CLSS public final com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl<%0 extends java.lang.Object>
fld public final com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl<? super {com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl%0}> superClazz
fld public final com.sun.xml.bind.v2.runtime.property.Property<{com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl%0}>[] properties
intf com.sun.xml.bind.v2.runtime.AttributeAccessor<{com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl%0}>
meth protected void link(com.sun.xml.bind.v2.runtime.JAXBContextImpl)
meth public boolean reset({com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl%0},com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext) throws org.xml.sax.SAXException
meth public com.sun.xml.bind.v2.runtime.Transducer<{com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl%0}> getTransducer()
meth public com.sun.xml.bind.v2.runtime.unmarshaller.Loader getLoader(com.sun.xml.bind.v2.runtime.JAXBContextImpl,boolean)
meth public java.lang.String getElementLocalName({com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl%0})
meth public java.lang.String getElementNamespaceURI({com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl%0})
meth public java.lang.String getId({com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws org.xml.sax.SAXException
meth public void serializeAttributes({com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void serializeBody({com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void serializeRoot({com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void serializeURIs({com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws org.xml.sax.SAXException
meth public void wrapUp()
meth public {com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl%0} createInstance(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext) throws java.lang.IllegalAccessException,java.lang.InstantiationException,java.lang.reflect.InvocationTargetException,org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.JaxBeanInfo<{com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl%0}>
hfds EMPTY_PROPERTIES,attributeProperties,ci,factoryMethod,idProperty,inheritedAttWildcard,loader,loaderWithTypeSubst,logger,retainPropertyInfo,tagName,uriProperties,xducer,xmlLocatorField

CLSS public com.sun.xml.bind.v2.runtime.CompositeStructureBeanInfo
cons public init(com.sun.xml.bind.v2.runtime.JAXBContextImpl)
meth public boolean reset(com.sun.xml.bind.api.CompositeStructure,com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext) throws org.xml.sax.SAXException
meth public com.sun.xml.bind.api.CompositeStructure createInstance(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext) throws java.lang.IllegalAccessException,java.lang.InstantiationException,java.lang.reflect.InvocationTargetException,org.xml.sax.SAXException
meth public com.sun.xml.bind.v2.runtime.Transducer<com.sun.xml.bind.api.CompositeStructure> getTransducer()
meth public com.sun.xml.bind.v2.runtime.unmarshaller.Loader getLoader(com.sun.xml.bind.v2.runtime.JAXBContextImpl,boolean)
meth public java.lang.String getElementLocalName(com.sun.xml.bind.api.CompositeStructure)
meth public java.lang.String getElementNamespaceURI(com.sun.xml.bind.api.CompositeStructure)
meth public java.lang.String getId(com.sun.xml.bind.api.CompositeStructure,com.sun.xml.bind.v2.runtime.XMLSerializer) throws org.xml.sax.SAXException
meth public void serializeAttributes(com.sun.xml.bind.api.CompositeStructure,com.sun.xml.bind.v2.runtime.XMLSerializer) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void serializeBody(com.sun.xml.bind.api.CompositeStructure,com.sun.xml.bind.v2.runtime.XMLSerializer) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void serializeRoot(com.sun.xml.bind.api.CompositeStructure,com.sun.xml.bind.v2.runtime.XMLSerializer) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void serializeURIs(com.sun.xml.bind.api.CompositeStructure,com.sun.xml.bind.v2.runtime.XMLSerializer) throws org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.JaxBeanInfo<com.sun.xml.bind.api.CompositeStructure>

CLSS public abstract com.sun.xml.bind.v2.runtime.Coordinator
cons public init()
intf javax.xml.bind.ValidationEventHandler
intf org.xml.sax.ErrorHandler
meth protected abstract javax.xml.bind.ValidationEventLocator getLocation()
meth protected final void popCoordinator()
meth protected final void pushCoordinator()
meth public <%0 extends javax.xml.bind.annotation.adapters.XmlAdapter> boolean containsAdapter(java.lang.Class<{%%0}>)
meth public final <%0 extends javax.xml.bind.annotation.adapters.XmlAdapter> {%%0} getAdapter(java.lang.Class<{%%0}>)
meth public final javax.xml.bind.annotation.adapters.XmlAdapter putAdapter(java.lang.Class<? extends javax.xml.bind.annotation.adapters.XmlAdapter>,javax.xml.bind.annotation.adapters.XmlAdapter)
meth public final void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public final void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public final void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public static com.sun.xml.bind.v2.runtime.Coordinator _getInstance()
supr java.lang.Object
hfds activeTable,adapters,old

CLSS public final com.sun.xml.bind.v2.runtime.ElementBeanInfoImpl
cons protected init(com.sun.xml.bind.v2.runtime.JAXBContextImpl)
fld public final java.lang.Class expectedType
meth public boolean reset(javax.xml.bind.JAXBElement,com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext)
meth public com.sun.xml.bind.v2.runtime.unmarshaller.Loader getLoader(com.sun.xml.bind.v2.runtime.JAXBContextImpl,boolean)
meth public final com.sun.xml.bind.v2.runtime.Transducer<javax.xml.bind.JAXBElement> getTransducer()
meth public final javax.xml.bind.JAXBElement createInstance(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext) throws java.lang.IllegalAccessException,java.lang.InstantiationException,java.lang.reflect.InvocationTargetException
meth public final javax.xml.bind.JAXBElement createInstanceFromValue(java.lang.Object) throws java.lang.IllegalAccessException,java.lang.InstantiationException,java.lang.reflect.InvocationTargetException
meth public java.lang.String getElementLocalName(javax.xml.bind.JAXBElement)
meth public java.lang.String getElementNamespaceURI(javax.xml.bind.JAXBElement)
meth public java.lang.String getId(javax.xml.bind.JAXBElement,com.sun.xml.bind.v2.runtime.XMLSerializer)
meth public void link(com.sun.xml.bind.v2.runtime.JAXBContextImpl)
meth public void serializeAttributes(javax.xml.bind.JAXBElement,com.sun.xml.bind.v2.runtime.XMLSerializer)
meth public void serializeBody(javax.xml.bind.JAXBElement,com.sun.xml.bind.v2.runtime.XMLSerializer) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void serializeRoot(javax.xml.bind.JAXBElement,com.sun.xml.bind.v2.runtime.XMLSerializer) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void serializeURIs(javax.xml.bind.JAXBElement,com.sun.xml.bind.v2.runtime.XMLSerializer)
meth public void wrapUp()
supr com.sun.xml.bind.v2.runtime.JaxBeanInfo<javax.xml.bind.JAXBElement>
hfds constructor,loader,property,scope,tagName
hcls IntercepterLoader

CLSS public abstract com.sun.xml.bind.v2.runtime.FilterTransducer<%0 extends java.lang.Object>
cons protected init(com.sun.xml.bind.v2.runtime.Transducer<{com.sun.xml.bind.v2.runtime.FilterTransducer%0}>)
fld protected final com.sun.xml.bind.v2.runtime.Transducer<{com.sun.xml.bind.v2.runtime.FilterTransducer%0}> core
intf com.sun.xml.bind.v2.runtime.Transducer<{com.sun.xml.bind.v2.runtime.FilterTransducer%0}>
meth public boolean useNamespace()
meth public java.lang.CharSequence print({com.sun.xml.bind.v2.runtime.FilterTransducer%0}) throws com.sun.xml.bind.api.AccessorException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public javax.xml.namespace.QName getTypeName({com.sun.xml.bind.v2.runtime.FilterTransducer%0})
meth public void declareNamespace({com.sun.xml.bind.v2.runtime.FilterTransducer%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws com.sun.xml.bind.api.AccessorException
meth public void writeLeafElement(com.sun.xml.bind.v2.runtime.XMLSerializer,com.sun.xml.bind.v2.runtime.Name,{com.sun.xml.bind.v2.runtime.FilterTransducer%0},java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void writeText(com.sun.xml.bind.v2.runtime.XMLSerializer,{com.sun.xml.bind.v2.runtime.FilterTransducer%0},java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public {com.sun.xml.bind.v2.runtime.FilterTransducer%0} parse(java.lang.CharSequence) throws com.sun.xml.bind.api.AccessorException,org.xml.sax.SAXException
supr java.lang.Object

CLSS public com.sun.xml.bind.v2.runtime.IllegalAnnotationException
cons public init(java.lang.String,com.sun.xml.bind.v2.model.annotation.Locatable)
cons public init(java.lang.String,com.sun.xml.bind.v2.model.annotation.Locatable,com.sun.xml.bind.v2.model.annotation.Locatable)
cons public init(java.lang.String,java.lang.Throwable,com.sun.xml.bind.v2.model.annotation.Locatable)
cons public init(java.lang.String,java.lang.annotation.Annotation)
cons public init(java.lang.String,java.lang.annotation.Annotation,com.sun.xml.bind.v2.model.annotation.Locatable)
cons public init(java.lang.String,java.lang.annotation.Annotation,java.lang.annotation.Annotation)
meth public java.lang.String toString()
meth public java.util.List<java.util.List<com.sun.xml.bind.v2.runtime.Location>> getSourcePos()
supr javax.xml.bind.JAXBException
hfds pos,serialVersionUID

CLSS public com.sun.xml.bind.v2.runtime.IllegalAnnotationsException
cons public init(java.util.List<com.sun.xml.bind.v2.runtime.IllegalAnnotationException>)
innr public static Builder
meth public java.lang.String toString()
meth public java.util.List<com.sun.xml.bind.v2.runtime.IllegalAnnotationException> getErrors()
supr javax.xml.bind.JAXBException
hfds errors,serialVersionUID

CLSS public static com.sun.xml.bind.v2.runtime.IllegalAnnotationsException$Builder
 outer com.sun.xml.bind.v2.runtime.IllegalAnnotationsException
cons public init()
intf com.sun.xml.bind.v2.model.core.ErrorHandler
meth public void check() throws com.sun.xml.bind.v2.runtime.IllegalAnnotationsException
meth public void error(com.sun.xml.bind.v2.runtime.IllegalAnnotationException)
supr java.lang.Object
hfds list

CLSS public com.sun.xml.bind.v2.runtime.InlineBinaryTransducer<%0 extends java.lang.Object>
cons public init(com.sun.xml.bind.v2.runtime.Transducer<{com.sun.xml.bind.v2.runtime.InlineBinaryTransducer%0}>)
meth public java.lang.CharSequence print({com.sun.xml.bind.v2.runtime.InlineBinaryTransducer%0}) throws com.sun.xml.bind.api.AccessorException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public void writeLeafElement(com.sun.xml.bind.v2.runtime.XMLSerializer,com.sun.xml.bind.v2.runtime.Name,{com.sun.xml.bind.v2.runtime.InlineBinaryTransducer%0},java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void writeText(com.sun.xml.bind.v2.runtime.XMLSerializer,{com.sun.xml.bind.v2.runtime.InlineBinaryTransducer%0},java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.FilterTransducer<{com.sun.xml.bind.v2.runtime.InlineBinaryTransducer%0}>

CLSS public final com.sun.xml.bind.v2.runtime.JAXBContextImpl
fld protected final boolean c14nSupport
fld protected java.util.Map<com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo,com.sun.xml.bind.v2.runtime.JaxBeanInfo> beanInfos
fld public com.sun.xml.bind.v2.runtime.NameBuilder nameBuilder
fld public final boolean allNillable
fld public final boolean disableSecurityProcessing
fld public final boolean fastBoot
fld public final boolean improvedXsiTypeHandling
fld public final boolean retainPropertyInfo
fld public final boolean supressAccessorWarnings
fld public final boolean xmlAccessorFactorySupport
fld public final com.sun.istack.Pool<javax.xml.bind.Marshaller> marshallerPool
fld public final com.sun.istack.Pool<javax.xml.bind.Unmarshaller> unmarshallerPool
fld public final com.sun.xml.bind.v2.runtime.NameList nameList
fld public final int maxErrorsCount
fld public java.lang.Boolean backupWithParentNamespace
innr public static JAXBContextBuilder
meth protected com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl getOrCreate(com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo)
meth protected com.sun.xml.bind.v2.runtime.JaxBeanInfo getOrCreate(com.sun.xml.bind.v2.model.runtime.RuntimeArrayInfo)
meth protected com.sun.xml.bind.v2.runtime.JaxBeanInfo getOrCreate(com.sun.xml.bind.v2.model.runtime.RuntimeEnumLeafInfo)
meth public <%0 extends java.lang.Object> javax.xml.bind.Binder<{%%0}> createBinder(java.lang.Class<{%%0}>)
meth public boolean hasSwaRef()
meth public com.sun.xml.bind.api.Bridge createBridge(com.sun.xml.bind.api.TypeReference)
meth public com.sun.xml.bind.api.BridgeContext createBridgeContext()
 anno 0 com.sun.istack.NotNull()
meth public com.sun.xml.bind.api.RawAccessor getElementPropertyAccessor(java.lang.Class,java.lang.String,java.lang.String) throws javax.xml.bind.JAXBException
meth public com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet getRuntimeTypeInfoSet()
meth public com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet getTypeInfoSet() throws com.sun.xml.bind.v2.runtime.IllegalAnnotationsException
meth public com.sun.xml.bind.v2.runtime.ElementBeanInfoImpl getElement(java.lang.Class,javax.xml.namespace.QName)
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl createAugmented(java.lang.Class<?>) throws javax.xml.bind.JAXBException
meth public com.sun.xml.bind.v2.runtime.JaxBeanInfo getGlobalType(javax.xml.namespace.QName)
meth public com.sun.xml.bind.v2.runtime.JaxBeanInfo getOrCreate(com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo)
meth public com.sun.xml.bind.v2.runtime.MarshallerImpl createMarshaller()
meth public com.sun.xml.bind.v2.runtime.output.Encoded[] getUTF8NameTable()
meth public com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl createUnmarshaller()
meth public final <%0 extends java.lang.Object> com.sun.xml.bind.v2.runtime.JaxBeanInfo<{%%0}> getBeanInfo(java.lang.Class<{%%0}>)
meth public final <%0 extends java.lang.Object> com.sun.xml.bind.v2.runtime.JaxBeanInfo<{%%0}> getBeanInfo(java.lang.Class<{%%0}>,boolean) throws javax.xml.bind.JAXBException
meth public final com.sun.xml.bind.v2.runtime.JaxBeanInfo getBeanInfo(java.lang.Object)
meth public final com.sun.xml.bind.v2.runtime.JaxBeanInfo getBeanInfo(java.lang.Object,boolean) throws javax.xml.bind.JAXBException
meth public final com.sun.xml.bind.v2.runtime.unmarshaller.Loader selectRootLoader(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName)
meth public int getNumberOfAttributeNames()
meth public int getNumberOfElementNames()
meth public int getNumberOfLocalNames()
meth public java.lang.String getBuildId()
meth public java.lang.String getNearestTypeName(javax.xml.namespace.QName)
meth public java.lang.String getXMIMEContentType(java.lang.Object)
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getKnownNamespaceURIs()
meth public java.util.Set<javax.xml.bind.annotation.XmlNs> getXmlNsSet()
meth public java.util.Set<javax.xml.namespace.QName> getValidRootNames()
meth public javax.xml.bind.Binder<org.w3c.dom.Node> createBinder()
meth public javax.xml.bind.JAXBIntrospector createJAXBIntrospector()
meth public javax.xml.bind.Validator createValidator()
meth public javax.xml.namespace.QName getElementName(java.lang.Class) throws javax.xml.bind.JAXBException
meth public javax.xml.namespace.QName getElementName(java.lang.Object) throws javax.xml.bind.JAXBException
meth public javax.xml.namespace.QName getTypeName(com.sun.xml.bind.api.TypeReference)
meth public static javax.xml.transform.sax.TransformerHandler createTransformerHandler(boolean)
meth public void generateEpisode(javax.xml.transform.Result)
meth public void generateSchema(javax.xml.bind.SchemaOutputResolver) throws java.io.IOException
supr com.sun.xml.bind.api.JAXBRIContext
hfds QNAME_COMPARATOR,annotationReader,beanInfoMap,bridges,classes,db,defaultNsUri,elements,hasSwaRef,rootMap,subclassReplacements,typeInfoSetCache,typeMap,utf8nameTable,xmlNsSet

CLSS public static com.sun.xml.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder
 outer com.sun.xml.bind.v2.runtime.JAXBContextImpl
cons public init()
cons public init(com.sun.xml.bind.v2.runtime.JAXBContextImpl)
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl build() throws javax.xml.bind.JAXBException
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder setAllNillable(boolean)
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder setAnnotationReader(com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader)
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder setBackupWithParentNamespace(java.lang.Boolean)
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder setC14NSupport(boolean)
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder setClasses(java.lang.Class[])
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder setDefaultNsUri(java.lang.String)
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder setDisableSecurityProcessing(boolean)
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder setImprovedXsiTypeHandling(boolean)
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder setMaxErrorsCount(int)
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder setRetainPropertyInfo(boolean)
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder setSubclassReplacements(java.util.Map<java.lang.Class,java.lang.Class>)
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder setSupressAccessorWarnings(boolean)
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder setTypeRefs(java.util.Collection<com.sun.xml.bind.api.TypeReference>)
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder setXmlAccessorFactorySupport(boolean)
supr java.lang.Object
hfds allNillable,annotationReader,backupWithParentNamespace,c14nSupport,classes,defaultNsUri,disableSecurityProcessing,improvedXsiTypeHandling,maxErrorsCount,retainPropertyInfo,subclassReplacements,supressAccessorWarnings,typeRefs,xmlAccessorFactorySupport

CLSS public abstract com.sun.xml.bind.v2.runtime.JaxBeanInfo<%0 extends java.lang.Object>
cons protected init(com.sun.xml.bind.v2.runtime.JAXBContextImpl,com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo,java.lang.Class<{com.sun.xml.bind.v2.runtime.JaxBeanInfo%0}>,boolean,boolean,boolean)
cons protected init(com.sun.xml.bind.v2.runtime.JAXBContextImpl,com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo,java.lang.Class<{com.sun.xml.bind.v2.runtime.JaxBeanInfo%0}>,javax.xml.namespace.QName,boolean,boolean,boolean)
cons protected init(com.sun.xml.bind.v2.runtime.JAXBContextImpl,com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo,java.lang.Class<{com.sun.xml.bind.v2.runtime.JaxBeanInfo%0}>,javax.xml.namespace.QName[],boolean,boolean,boolean)
fld protected boolean isNilIncluded
fld protected short flag
fld public final java.lang.Class<{com.sun.xml.bind.v2.runtime.JaxBeanInfo%0}> jaxbType
meth protected final void hasElementOnlyContentModel(boolean)
meth protected final void setLifecycleFlags()
meth protected void link(com.sun.xml.bind.v2.runtime.JAXBContextImpl)
meth public abstract boolean reset({com.sun.xml.bind.v2.runtime.JaxBeanInfo%0},com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext) throws org.xml.sax.SAXException
meth public abstract com.sun.xml.bind.v2.runtime.Transducer<{com.sun.xml.bind.v2.runtime.JaxBeanInfo%0}> getTransducer()
meth public abstract com.sun.xml.bind.v2.runtime.unmarshaller.Loader getLoader(com.sun.xml.bind.v2.runtime.JAXBContextImpl,boolean)
meth public abstract java.lang.String getElementLocalName({com.sun.xml.bind.v2.runtime.JaxBeanInfo%0})
meth public abstract java.lang.String getElementNamespaceURI({com.sun.xml.bind.v2.runtime.JaxBeanInfo%0})
meth public abstract java.lang.String getId({com.sun.xml.bind.v2.runtime.JaxBeanInfo%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws org.xml.sax.SAXException
meth public abstract void serializeAttributes({com.sun.xml.bind.v2.runtime.JaxBeanInfo%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public abstract void serializeBody({com.sun.xml.bind.v2.runtime.JaxBeanInfo%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public abstract void serializeRoot({com.sun.xml.bind.v2.runtime.JaxBeanInfo%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public abstract void serializeURIs({com.sun.xml.bind.v2.runtime.JaxBeanInfo%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws org.xml.sax.SAXException
meth public abstract {com.sun.xml.bind.v2.runtime.JaxBeanInfo%0} createInstance(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext) throws java.lang.IllegalAccessException,java.lang.InstantiationException,java.lang.reflect.InvocationTargetException,org.xml.sax.SAXException
meth public boolean isNilIncluded()
meth public boolean lookForLifecycleMethods()
meth public final boolean hasAfterMarshalMethod()
meth public final boolean hasAfterUnmarshalMethod()
meth public final boolean hasBeforeMarshalMethod()
meth public final boolean hasBeforeUnmarshalMethod()
meth public final boolean hasElementOnlyContentModel()
meth public final boolean isElement()
meth public final boolean isImmutable()
meth public final java.lang.Object getLifecycleMethods()
meth public final void invokeAfterUnmarshalMethod(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl,java.lang.Object,java.lang.Object) throws org.xml.sax.SAXException
meth public final void invokeBeforeUnmarshalMethod(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl,java.lang.Object,java.lang.Object) throws org.xml.sax.SAXException
meth public java.util.Collection<javax.xml.namespace.QName> getTypeNames()
meth public javax.xml.namespace.QName getTypeName({com.sun.xml.bind.v2.runtime.JaxBeanInfo%0})
 anno 1 com.sun.istack.NotNull()
meth public void wrapUp()
supr java.lang.Object
hfds FLAG_HAS_AFTER_MARSHAL_METHOD,FLAG_HAS_AFTER_UNMARSHAL_METHOD,FLAG_HAS_BEFORE_MARSHAL_METHOD,FLAG_HAS_BEFORE_UNMARSHAL_METHOD,FLAG_HAS_ELEMENT_ONLY_CONTENTMODEL,FLAG_HAS_LIFECYCLE_EVENTS,FLAG_IS_ELEMENT,FLAG_IS_IMMUTABLE,lcm,logger,marshalEventParams,typeName,unmarshalEventParams

CLSS public abstract interface com.sun.xml.bind.v2.runtime.Location
meth public abstract java.lang.String toString()

CLSS public final com.sun.xml.bind.v2.runtime.MarshallerImpl
cons public init(com.sun.xml.bind.v2.runtime.JAXBContextImpl,com.sun.xml.bind.v2.runtime.AssociationMap)
fld protected final com.sun.xml.bind.v2.runtime.XMLSerializer serializer
fld protected final static java.lang.String C14N = "com.sun.xml.bind.c14n"
fld protected final static java.lang.String ENCODING_HANDLER = "com.sun.xml.bind.characterEscapeHandler"
fld protected final static java.lang.String ENCODING_HANDLER2 = "com.sun.xml.bind.marshaller.CharacterEscapeHandler"
fld protected final static java.lang.String INDENT_STRING = "com.sun.xml.bind.indentString"
fld protected final static java.lang.String OBJECT_IDENTITY_CYCLE_DETECTION = "com.sun.xml.bind.objectIdentitityCycleDetection"
fld protected final static java.lang.String PREFIX_MAPPER = "com.sun.xml.bind.namespacePrefixMapper"
fld protected final static java.lang.String XMLDECLARATION = "com.sun.xml.bind.xmlDeclaration"
fld protected final static java.lang.String XML_HEADERS = "com.sun.xml.bind.xmlHeaders"
intf javax.xml.bind.ValidationEventHandler
meth protected com.sun.xml.bind.marshaller.CharacterEscapeHandler createEscapeHandler(java.lang.String)
meth protected final <%0 extends java.lang.Object> void write(com.sun.xml.bind.v2.runtime.Name,com.sun.xml.bind.v2.runtime.JaxBeanInfo<{%%0}>,{%%0},com.sun.xml.bind.v2.runtime.output.XmlOutput,java.lang.Runnable) throws javax.xml.bind.JAXBException
meth public <%0 extends javax.xml.bind.annotation.adapters.XmlAdapter> void setAdapter(java.lang.Class<{%%0}>,{%%0})
meth public <%0 extends javax.xml.bind.annotation.adapters.XmlAdapter> {%%0} getAdapter(java.lang.Class<{%%0}>)
meth public boolean handleEvent(javax.xml.bind.ValidationEvent)
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl getContext()
meth public com.sun.xml.bind.v2.runtime.output.XmlOutput createWriter(java.io.OutputStream) throws javax.xml.bind.JAXBException
meth public com.sun.xml.bind.v2.runtime.output.XmlOutput createWriter(java.io.OutputStream,java.lang.String) throws javax.xml.bind.JAXBException
meth public com.sun.xml.bind.v2.runtime.output.XmlOutput createWriter(java.io.Writer)
meth public com.sun.xml.bind.v2.runtime.output.XmlOutput createWriter(java.io.Writer,java.lang.String)
meth public java.lang.Object getProperty(java.lang.String) throws javax.xml.bind.PropertyException
meth public javax.xml.bind.Marshaller$Listener getListener()
meth public javax.xml.bind.attachment.AttachmentMarshaller getAttachmentMarshaller()
meth public javax.xml.validation.Schema getSchema()
meth public void marshal(java.lang.Object,com.sun.xml.bind.v2.runtime.output.XmlOutput) throws javax.xml.bind.JAXBException
meth public void marshal(java.lang.Object,java.io.OutputStream,javax.xml.namespace.NamespaceContext) throws javax.xml.bind.JAXBException
meth public void marshal(java.lang.Object,javax.xml.stream.XMLEventWriter) throws javax.xml.bind.JAXBException
meth public void marshal(java.lang.Object,javax.xml.stream.XMLStreamWriter) throws javax.xml.bind.JAXBException
meth public void marshal(java.lang.Object,javax.xml.transform.Result) throws javax.xml.bind.JAXBException
meth public void setAttachmentMarshaller(javax.xml.bind.attachment.AttachmentMarshaller)
meth public void setListener(javax.xml.bind.Marshaller$Listener)
meth public void setProperty(java.lang.String,java.lang.Object) throws javax.xml.bind.PropertyException
meth public void setSchema(javax.xml.validation.Schema)
supr javax.xml.bind.helpers.AbstractMarshallerImpl
hfds LOGGER,c14nSupport,context,escapeHandler,externalListener,header,indent,prefixMapper,schema,toBeClosed,toBeFlushed

CLSS public final com.sun.xml.bind.v2.runtime.MimeTypedTransducer<%0 extends java.lang.Object>
cons public init(com.sun.xml.bind.v2.runtime.Transducer<{com.sun.xml.bind.v2.runtime.MimeTypedTransducer%0}>,javax.activation.MimeType)
meth public java.lang.CharSequence print({com.sun.xml.bind.v2.runtime.MimeTypedTransducer%0}) throws com.sun.xml.bind.api.AccessorException
meth public void writeLeafElement(com.sun.xml.bind.v2.runtime.XMLSerializer,com.sun.xml.bind.v2.runtime.Name,{com.sun.xml.bind.v2.runtime.MimeTypedTransducer%0},java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void writeText(com.sun.xml.bind.v2.runtime.XMLSerializer,{com.sun.xml.bind.v2.runtime.MimeTypedTransducer%0},java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.FilterTransducer<{com.sun.xml.bind.v2.runtime.MimeTypedTransducer%0}>
hfds expectedMimeType

CLSS public final com.sun.xml.bind.v2.runtime.Name
fld public final boolean isAttribute
fld public final java.lang.String localName
fld public final java.lang.String nsUri
fld public final short localNameIndex
fld public final short nsUriIndex
fld public final short qNameIndex
intf java.lang.Comparable<com.sun.xml.bind.v2.runtime.Name>
meth public boolean equals(java.lang.String,java.lang.String)
meth public int compareTo(com.sun.xml.bind.v2.runtime.Name)
meth public java.lang.String toString()
meth public javax.xml.namespace.QName toQName()
supr java.lang.Object

CLSS public final com.sun.xml.bind.v2.runtime.NameBuilder
cons public init()
meth public com.sun.xml.bind.v2.runtime.Name createAttributeName(java.lang.String,java.lang.String)
meth public com.sun.xml.bind.v2.runtime.Name createAttributeName(javax.xml.namespace.QName)
meth public com.sun.xml.bind.v2.runtime.Name createElementName(java.lang.String,java.lang.String)
meth public com.sun.xml.bind.v2.runtime.Name createElementName(javax.xml.namespace.QName)
meth public com.sun.xml.bind.v2.runtime.NameList conclude()
supr java.lang.Object
hfds attributeQNameIndexMap,elementQNameIndexMap,localNameIndexMap,nonDefaultableNsUris,uriIndexMap

CLSS public final com.sun.xml.bind.v2.runtime.NameList
cons public init(java.lang.String[],boolean[],java.lang.String[],int,int)
fld public final boolean[] nsUriCannotBeDefaulted
fld public final int numberOfAttributeNames
fld public final int numberOfElementNames
fld public final java.lang.String[] localNames
fld public final java.lang.String[] namespaceURIs
supr java.lang.Object

CLSS public abstract interface com.sun.xml.bind.v2.runtime.NamespaceContext2
intf javax.xml.namespace.NamespaceContext
meth public abstract int force(java.lang.String,java.lang.String)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public abstract java.lang.String declareNamespace(java.lang.String,java.lang.String,boolean)

CLSS public com.sun.xml.bind.v2.runtime.RuntimeUtil
cons public init()
fld public final static java.util.Map<java.lang.Class,java.lang.Class> boxToPrimitive
fld public final static java.util.Map<java.lang.Class,java.lang.Class> primitiveToBox
innr public final static ToStringAdapter
supr java.lang.Object

CLSS public final static com.sun.xml.bind.v2.runtime.RuntimeUtil$ToStringAdapter
 outer com.sun.xml.bind.v2.runtime.RuntimeUtil
cons public init()
meth public java.lang.Object unmarshal(java.lang.String)
meth public java.lang.String marshal(java.lang.Object)
supr javax.xml.bind.annotation.adapters.XmlAdapter<java.lang.String,java.lang.Object>

CLSS public com.sun.xml.bind.v2.runtime.SchemaTypeTransducer<%0 extends java.lang.Object>
cons public init(com.sun.xml.bind.v2.runtime.Transducer<{com.sun.xml.bind.v2.runtime.SchemaTypeTransducer%0}>,javax.xml.namespace.QName)
meth public java.lang.CharSequence print({com.sun.xml.bind.v2.runtime.SchemaTypeTransducer%0}) throws com.sun.xml.bind.api.AccessorException
meth public void writeLeafElement(com.sun.xml.bind.v2.runtime.XMLSerializer,com.sun.xml.bind.v2.runtime.Name,{com.sun.xml.bind.v2.runtime.SchemaTypeTransducer%0},java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void writeText(com.sun.xml.bind.v2.runtime.XMLSerializer,{com.sun.xml.bind.v2.runtime.SchemaTypeTransducer%0},java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.FilterTransducer<{com.sun.xml.bind.v2.runtime.SchemaTypeTransducer%0}>
hfds schemaType

CLSS public final com.sun.xml.bind.v2.runtime.SwaRefAdapter
cons public init()
meth public java.lang.String marshal(javax.activation.DataHandler)
meth public javax.activation.DataHandler unmarshal(java.lang.String)
supr javax.xml.bind.annotation.adapters.XmlAdapter<java.lang.String,javax.activation.DataHandler>

CLSS public com.sun.xml.bind.v2.runtime.SwaRefAdapterMarker
cons public init()
meth public java.lang.String marshal(javax.activation.DataHandler) throws java.lang.Exception
meth public javax.activation.DataHandler unmarshal(java.lang.String) throws java.lang.Exception
supr javax.xml.bind.annotation.adapters.XmlAdapter<java.lang.String,javax.activation.DataHandler>

CLSS public abstract interface com.sun.xml.bind.v2.runtime.Transducer<%0 extends java.lang.Object>
meth public abstract boolean useNamespace()
meth public abstract java.lang.CharSequence print({com.sun.xml.bind.v2.runtime.Transducer%0}) throws com.sun.xml.bind.api.AccessorException
 anno 0 com.sun.istack.NotNull()
 anno 1 com.sun.istack.NotNull()
meth public abstract javax.xml.namespace.QName getTypeName({com.sun.xml.bind.v2.runtime.Transducer%0})
 anno 1 com.sun.istack.NotNull()
meth public abstract void declareNamespace({com.sun.xml.bind.v2.runtime.Transducer%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws com.sun.xml.bind.api.AccessorException
meth public abstract void writeLeafElement(com.sun.xml.bind.v2.runtime.XMLSerializer,com.sun.xml.bind.v2.runtime.Name,{com.sun.xml.bind.v2.runtime.Transducer%0},java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
 anno 3 com.sun.istack.NotNull()
meth public abstract void writeText(com.sun.xml.bind.v2.runtime.XMLSerializer,{com.sun.xml.bind.v2.runtime.Transducer%0},java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public abstract {com.sun.xml.bind.v2.runtime.Transducer%0} parse(java.lang.CharSequence) throws com.sun.xml.bind.api.AccessorException,org.xml.sax.SAXException

CLSS public final com.sun.xml.bind.v2.runtime.XMLSerializer
fld public final com.sun.xml.bind.v2.runtime.JAXBContextImpl grammar
fld public final com.sun.xml.bind.v2.runtime.NameList nameList
fld public final int[] knownUri2prefixIndexMap
fld public javax.xml.bind.attachment.AttachmentMarshaller attachmentMarshaller
meth protected javax.xml.bind.ValidationEventLocator getLocation()
meth public <%0 extends java.lang.Object> void writeDom({%%0},javax.xml.bind.annotation.DomHandler<{%%0},?>,java.lang.Object,java.lang.String) throws org.xml.sax.SAXException
meth public boolean getInlineBinaryFlag()
meth public boolean getObjectIdentityCycleDetection()
meth public boolean handleError(java.lang.Exception)
meth public boolean handleError(java.lang.Exception,java.lang.Object,java.lang.String)
meth public boolean handleEvent(javax.xml.bind.ValidationEvent)
meth public boolean setInlineBinaryFlag(boolean)
meth public com.sun.xml.bind.v2.runtime.NamespaceContext2 getNamespaceContext()
meth public com.sun.xml.bind.v2.runtime.property.Property getCurrentProperty()
meth public com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data getCachedBase64DataInstance()
meth public final void childAsSoleContent(java.lang.Object,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public final void childAsXsiType(java.lang.Object,java.lang.String,com.sun.xml.bind.v2.runtime.JaxBeanInfo,boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public final void reportError(java.lang.String,java.lang.Throwable) throws org.xml.sax.SAXException
meth public final void writeXsiNilTrue() throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public java.lang.String getXMIMEContentType()
meth public java.lang.String onID(java.lang.Object,java.lang.String)
meth public java.lang.String onIDREF(java.lang.Object) throws org.xml.sax.SAXException
meth public javax.activation.MimeType setExpectedMimeType(javax.activation.MimeType)
meth public javax.xml.bind.ValidationEventLocator getCurrentLocation(java.lang.String)
meth public javax.xml.namespace.QName getSchemaType()
meth public javax.xml.namespace.QName setSchemaType(javax.xml.namespace.QName)
meth public javax.xml.transform.Transformer getIdentityTransformer()
meth public static com.sun.xml.bind.v2.runtime.XMLSerializer getInstance()
meth public void addInscopeBinding(java.lang.String,java.lang.String)
meth public void attWildcardAsAttributes(java.util.Map<javax.xml.namespace.QName,java.lang.String>,java.lang.String) throws org.xml.sax.SAXException
meth public void attWildcardAsURIs(java.util.Map<javax.xml.namespace.QName,java.lang.String>,java.lang.String)
meth public void attribute(com.sun.xml.bind.v2.runtime.Name,java.lang.CharSequence) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public void attribute(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void childAsRoot(java.lang.Object) throws java.io.IOException,javax.xml.bind.JAXBException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void clearCurrentProperty()
meth public void close()
meth public void endAttributes() throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void endDocument() throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void endElement() throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void endNamespaceDecls(java.lang.Object) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public void errorMissingId(java.lang.Object) throws org.xml.sax.SAXException
meth public void leafElement(com.sun.xml.bind.v2.runtime.Name,com.sun.xml.bind.v2.runtime.output.Pcdata,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void leafElement(com.sun.xml.bind.v2.runtime.Name,int,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void leafElement(com.sun.xml.bind.v2.runtime.Name,java.lang.String,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void reportError(javax.xml.bind.ValidationEvent) throws org.xml.sax.SAXException
meth public void setObjectIdentityCycleDetection(boolean)
meth public void setPrefixMapper(com.sun.xml.bind.marshaller.NamespacePrefixMapper)
meth public void startDocument(com.sun.xml.bind.v2.runtime.output.XmlOutput,boolean,java.lang.String,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void startElement(com.sun.xml.bind.v2.runtime.Name,java.lang.Object)
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,java.lang.Object)
meth public void startElementForce(java.lang.String,java.lang.String,java.lang.String,java.lang.Object)
meth public void text(com.sun.xml.bind.v2.runtime.output.Pcdata,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void text(java.lang.String,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.Coordinator
hfds base64Data,contentHandlerAdapter,currentProperty,cycleDetectionStack,expectedMimeType,fragment,idReferencedObjects,identityTransformer,inlineBinaryFlag,intData,marshaller,noNsSchemaLocation,nsContext,nse,objectsWithId,out,schemaLocation,schemaType,seenRoot,textHasAlreadyPrinted

CLSS public com.sun.xml.bind.v2.runtime.output.C14nXmlOutput
cons public init(java.io.OutputStream,com.sun.xml.bind.v2.runtime.output.Encoded[],boolean,com.sun.xml.bind.marshaller.CharacterEscapeHandler)
meth protected void writeNsDecls(int) throws java.io.IOException
meth public void attribute(com.sun.xml.bind.v2.runtime.Name,java.lang.String) throws java.io.IOException
meth public void attribute(int,java.lang.String,java.lang.String) throws java.io.IOException
meth public void endStartTag() throws java.io.IOException
supr com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput
hfds len,namedAttributesAreOrdered,nsBuf,otherAttributes,staticAttributes
hcls DynamicAttribute,StaticAttribute

CLSS public final com.sun.xml.bind.v2.runtime.output.DOMOutput
cons public init(org.w3c.dom.Node,com.sun.xml.bind.v2.runtime.AssociationMap)
meth public void endStartTag() throws org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.output.SAXOutput
hfds assoc

CLSS public final com.sun.xml.bind.v2.runtime.output.Encoded
cons public init()
cons public init(java.lang.String)
fld public byte[] buf
fld public int len
meth public final void set(java.lang.String)
meth public final void setEscape(java.lang.String,boolean)
meth public final void write(com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput) throws java.io.IOException
meth public void append(char)
meth public void compact()
meth public void ensureSize(int)
supr java.lang.Object
hfds attributeEntities,entities

CLSS public final com.sun.xml.bind.v2.runtime.output.FastInfosetStreamWriterOutput
cons public init(com.sun.xml.fastinfoset.stax.StAXDocumentSerializer,com.sun.xml.bind.v2.runtime.JAXBContextImpl)
meth public void attribute(com.sun.xml.bind.v2.runtime.Name,java.lang.String) throws java.io.IOException
meth public void attribute(int,java.lang.String,java.lang.String) throws java.io.IOException
meth public void beginStartTag(com.sun.xml.bind.v2.runtime.Name) throws java.io.IOException
meth public void beginStartTag(int,java.lang.String) throws java.io.IOException
meth public void beginStartTagWithNamespaces(com.sun.xml.bind.v2.runtime.Name) throws java.io.IOException
meth public void endDocument(boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void endStartTag() throws java.io.IOException
meth public void endTag(com.sun.xml.bind.v2.runtime.Name) throws java.io.IOException
meth public void endTag(int,java.lang.String) throws java.io.IOException
meth public void startDocument(com.sun.xml.bind.v2.runtime.XMLSerializer,boolean,int[],com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void text(com.sun.xml.bind.v2.runtime.output.Pcdata,boolean) throws java.io.IOException
meth public void text(java.lang.String,boolean) throws java.io.IOException
supr com.sun.xml.bind.v2.runtime.output.XMLStreamWriterOutput
hfds fiout,localNames,tables
hcls AppData,TablesPerJAXBContext

CLSS public final com.sun.xml.bind.v2.runtime.output.ForkXmlOutput
cons public init(com.sun.xml.bind.v2.runtime.output.XmlOutput,com.sun.xml.bind.v2.runtime.output.XmlOutput)
meth public void attribute(com.sun.xml.bind.v2.runtime.Name,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public void attribute(int,java.lang.String,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public void beginStartTag(com.sun.xml.bind.v2.runtime.Name) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public void beginStartTag(int,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public void endDocument(boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void endStartTag() throws java.io.IOException,org.xml.sax.SAXException
meth public void endTag(com.sun.xml.bind.v2.runtime.Name) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void endTag(int,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void startDocument(com.sun.xml.bind.v2.runtime.XMLSerializer,boolean,int[],com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void text(com.sun.xml.bind.v2.runtime.output.Pcdata,boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void text(java.lang.String,boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.output.XmlOutputAbstractImpl
hfds lhs,rhs

CLSS public final com.sun.xml.bind.v2.runtime.output.IndentingUTF8XmlOutput
cons public init(java.io.OutputStream,java.lang.String,com.sun.xml.bind.v2.runtime.output.Encoded[],com.sun.xml.bind.marshaller.CharacterEscapeHandler)
meth public void beginStartTag(com.sun.xml.bind.v2.runtime.Name) throws java.io.IOException
meth public void beginStartTag(int,java.lang.String) throws java.io.IOException
meth public void endDocument(boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void endTag(com.sun.xml.bind.v2.runtime.Name) throws java.io.IOException
meth public void endTag(int,java.lang.String) throws java.io.IOException
meth public void text(com.sun.xml.bind.v2.runtime.output.Pcdata,boolean) throws java.io.IOException
meth public void text(java.lang.String,boolean) throws java.io.IOException
supr com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput
hfds depth,indent8,seenText,unitLen

CLSS public final com.sun.xml.bind.v2.runtime.output.MTOMXmlOutput
cons public init(com.sun.xml.bind.v2.runtime.output.XmlOutput)
meth public void attribute(com.sun.xml.bind.v2.runtime.Name,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public void attribute(int,java.lang.String,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public void beginStartTag(com.sun.xml.bind.v2.runtime.Name) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public void beginStartTag(int,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public void endDocument(boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void endStartTag() throws java.io.IOException,org.xml.sax.SAXException
meth public void endTag(com.sun.xml.bind.v2.runtime.Name) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void endTag(int,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void startDocument(com.sun.xml.bind.v2.runtime.XMLSerializer,boolean,int[],com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void text(com.sun.xml.bind.v2.runtime.output.Pcdata,boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void text(java.lang.String,boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.output.XmlOutputAbstractImpl
hfds localName,next,nsUri

CLSS public final com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl
cons public init(com.sun.xml.bind.v2.runtime.XMLSerializer)
fld public boolean collectionMode
innr public final Element
intf com.sun.xml.bind.v2.runtime.NamespaceContext2
meth public com.sun.xml.bind.marshaller.NamespacePrefixMapper getPrefixMapper()
meth public com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl$Element getCurrent()
meth public int count()
meth public int declareNsUri(java.lang.String,java.lang.String,boolean)
meth public int force(java.lang.String,java.lang.String)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.NotNull()
meth public int getPrefixIndex(java.lang.String)
meth public int put(java.lang.String,java.lang.String)
 anno 1 com.sun.istack.NotNull()
 anno 2 com.sun.istack.Nullable()
meth public java.lang.String declareNamespace(java.lang.String,java.lang.String,boolean)
meth public java.lang.String getNamespaceURI(int)
meth public java.lang.String getNamespaceURI(java.lang.String)
meth public java.lang.String getPrefix(int)
meth public java.lang.String getPrefix(java.lang.String)
meth public java.util.Iterator<java.lang.String> getPrefixes(java.lang.String)
meth public void reset()
meth public void setPrefixMapper(com.sun.xml.bind.marshaller.NamespacePrefixMapper)
supr java.lang.Object
hfds current,defaultNamespacePrefixMapper,nsUris,owner,prefixMapper,prefixes,size,top

CLSS public final com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl$Element
 outer com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl
fld public final com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl context
meth public boolean isRootElement()
meth public com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl$Element getParent()
meth public com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl$Element pop()
meth public com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl$Element push()
meth public final int count()
meth public final java.lang.String getNsUri(int)
meth public final java.lang.String getPrefix(int)
meth public int getBase()
meth public java.lang.Object getInnerPeer()
meth public java.lang.Object getOuterPeer()
meth public void endElement(com.sun.xml.bind.v2.runtime.output.XmlOutput) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void setTagName(com.sun.xml.bind.v2.runtime.Name,java.lang.Object)
meth public void setTagName(int,java.lang.String,java.lang.Object)
meth public void startElement(com.sun.xml.bind.v2.runtime.output.XmlOutput,java.lang.Object) throws java.io.IOException,javax.xml.stream.XMLStreamException
supr java.lang.Object
hfds baseIndex,defaultPrefixIndex,depth,elementLocalName,elementName,elementNamePrefix,innerPeer,next,oldDefaultNamespaceUriIndex,outerPeer,prev

CLSS public abstract com.sun.xml.bind.v2.runtime.output.Pcdata
cons public init()
intf java.lang.CharSequence
meth public abstract java.lang.String toString()
meth public abstract void writeTo(com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput) throws java.io.IOException
meth public void writeTo(char[],int)
supr java.lang.Object

CLSS public com.sun.xml.bind.v2.runtime.output.SAXOutput
cons public init(org.xml.sax.ContentHandler)
fld protected final org.xml.sax.ContentHandler out
meth public void attribute(int,java.lang.String,java.lang.String)
meth public void beginStartTag(int,java.lang.String)
meth public void endDocument(boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void endStartTag() throws org.xml.sax.SAXException
meth public void endTag(int,java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument(com.sun.xml.bind.v2.runtime.XMLSerializer,boolean,int[],com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void text(com.sun.xml.bind.v2.runtime.output.Pcdata,boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void text(java.lang.String,boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.output.XmlOutputAbstractImpl
hfds atts,buf,elementLocalName,elementNsUri,elementQName

CLSS public final com.sun.xml.bind.v2.runtime.output.StAXExStreamWriterOutput
cons public init(org.jvnet.staxex.XMLStreamWriterEx)
meth public void text(com.sun.xml.bind.v2.runtime.output.Pcdata,boolean) throws javax.xml.stream.XMLStreamException
supr com.sun.xml.bind.v2.runtime.output.XMLStreamWriterOutput
hfds out

CLSS public com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput
cons public init(java.io.OutputStream,com.sun.xml.bind.v2.runtime.output.Encoded[],com.sun.xml.bind.marshaller.CharacterEscapeHandler)
fld protected boolean closeStartTagPending
fld protected final byte[] octetBuffer
fld protected final java.io.OutputStream out
fld protected int octetBufferIndex
meth protected final void closeStartTag() throws java.io.IOException
meth protected final void flushBuffer() throws java.io.IOException
meth protected final void write(byte[]) throws java.io.IOException
meth protected final void write(byte[],int,int) throws java.io.IOException
meth protected final void writeNsDecl(int) throws java.io.IOException
meth protected void writeNsDecls(int) throws java.io.IOException
meth public final void text(int) throws java.io.IOException
meth public final void write(int) throws java.io.IOException
meth public void attribute(com.sun.xml.bind.v2.runtime.Name,java.lang.String) throws java.io.IOException
meth public void attribute(int,java.lang.String,java.lang.String) throws java.io.IOException
meth public void beginStartTag(com.sun.xml.bind.v2.runtime.Name) throws java.io.IOException
meth public void beginStartTag(int,java.lang.String) throws java.io.IOException
meth public void endDocument(boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void endStartTag() throws java.io.IOException
meth public void endTag(com.sun.xml.bind.v2.runtime.Name) throws java.io.IOException
meth public void endTag(int,java.lang.String) throws java.io.IOException
meth public void setHeader(java.lang.String)
meth public void startDocument(com.sun.xml.bind.v2.runtime.XMLSerializer,boolean,int[],com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void text(byte[],int) throws java.io.IOException
meth public void text(com.sun.xml.bind.v2.runtime.output.Pcdata,boolean) throws java.io.IOException
meth public void text(java.lang.String,boolean) throws java.io.IOException
supr com.sun.xml.bind.v2.runtime.output.XmlOutputAbstractImpl
hfds CLOSE_TAG,EMPTY_BYTE_ARRAY,EMPTY_TAG,EQUALS,XMLNS_COLON,XMLNS_EQUALS,XML_DECL,_CLOSE_TAG,_EMPTY_TAG,_EQUALS,_XMLNS_COLON,_XMLNS_EQUALS,_XML_DECL,escapeHandler,header,localNames,prefixCount,prefixes,textBuffer

CLSS public com.sun.xml.bind.v2.runtime.output.XMLEventWriterOutput
cons public init(javax.xml.stream.XMLEventWriter)
meth public void attribute(int,java.lang.String,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public void beginStartTag(int,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public void endDocument(boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void endStartTag() throws java.io.IOException,org.xml.sax.SAXException
meth public void endTag(int,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void startDocument(com.sun.xml.bind.v2.runtime.XMLSerializer,boolean,int[],com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void text(com.sun.xml.bind.v2.runtime.output.Pcdata,boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void text(java.lang.String,boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.output.XmlOutputAbstractImpl
hfds ef,out,sp

CLSS public com.sun.xml.bind.v2.runtime.output.XMLStreamWriterOutput
cons protected init(javax.xml.stream.XMLStreamWriter,com.sun.xml.bind.marshaller.CharacterEscapeHandler)
fld protected final char[] buf
meth public static com.sun.xml.bind.v2.runtime.output.XmlOutput create(javax.xml.stream.XMLStreamWriter,com.sun.xml.bind.v2.runtime.JAXBContextImpl,com.sun.xml.bind.marshaller.CharacterEscapeHandler)
meth public void attribute(int,java.lang.String,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public void beginStartTag(int,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public void endDocument(boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void endStartTag() throws java.io.IOException,org.xml.sax.SAXException
meth public void endTag(int,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void startDocument(com.sun.xml.bind.v2.runtime.XMLSerializer,boolean,int[],com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void text(com.sun.xml.bind.v2.runtime.output.Pcdata,boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void text(java.lang.String,boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.output.XmlOutputAbstractImpl
hfds FI_OUTPUT_CTOR,FI_STAX_WRITER_CLASS,STAXEX_OUTPUT_CTOR,STAXEX_WRITER_CLASS,escapeHandler,out,writerWrapper
hcls XmlStreamOutWriterAdapter

CLSS public abstract interface com.sun.xml.bind.v2.runtime.output.XmlOutput
meth public abstract void attribute(com.sun.xml.bind.v2.runtime.Name,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public abstract void attribute(int,java.lang.String,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public abstract void beginStartTag(com.sun.xml.bind.v2.runtime.Name) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public abstract void beginStartTag(int,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public abstract void endDocument(boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public abstract void endStartTag() throws java.io.IOException,org.xml.sax.SAXException
meth public abstract void endTag(com.sun.xml.bind.v2.runtime.Name) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public abstract void endTag(int,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public abstract void startDocument(com.sun.xml.bind.v2.runtime.XMLSerializer,boolean,int[],com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public abstract void text(com.sun.xml.bind.v2.runtime.output.Pcdata,boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public abstract void text(java.lang.String,boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException

CLSS public abstract com.sun.xml.bind.v2.runtime.output.XmlOutputAbstractImpl
cons public init()
fld protected com.sun.xml.bind.v2.runtime.XMLSerializer serializer
fld protected com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl nsContext
fld protected int[] nsUriIndex2prefixIndex
intf com.sun.xml.bind.v2.runtime.output.XmlOutput
meth public abstract void attribute(int,java.lang.String,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public abstract void beginStartTag(int,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public abstract void endStartTag() throws java.io.IOException,org.xml.sax.SAXException
meth public abstract void endTag(int,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void attribute(com.sun.xml.bind.v2.runtime.Name,java.lang.String) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public void beginStartTag(com.sun.xml.bind.v2.runtime.Name) throws java.io.IOException,javax.xml.stream.XMLStreamException
meth public void endDocument(boolean) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void endTag(com.sun.xml.bind.v2.runtime.Name) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void startDocument(com.sun.xml.bind.v2.runtime.XMLSerializer,boolean,int[],com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl) throws java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
supr java.lang.Object

CLSS public final com.sun.xml.bind.v2.runtime.property.AttributeProperty<%0 extends java.lang.Object>
cons public init(com.sun.xml.bind.v2.runtime.JAXBContextImpl,com.sun.xml.bind.v2.model.runtime.RuntimeAttributePropertyInfo)
fld protected final java.lang.String fieldName
fld public final com.sun.xml.bind.v2.runtime.Name attName
fld public final com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor<{com.sun.xml.bind.v2.runtime.property.AttributeProperty%0}> xacc
intf com.sun.xml.bind.v2.runtime.property.Property<{com.sun.xml.bind.v2.runtime.property.AttributeProperty%0}>
intf java.lang.Comparable<com.sun.xml.bind.v2.runtime.property.AttributeProperty>
meth public boolean hasSerializeURIAction()
meth public boolean isHiddenByOverride()
meth public com.sun.xml.bind.v2.model.core.PropertyKind getKind()
meth public com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo getInfo()
meth public com.sun.xml.bind.v2.runtime.reflect.Accessor getElementPropertyAccessor(java.lang.String,java.lang.String)
meth public int compareTo(com.sun.xml.bind.v2.runtime.property.AttributeProperty)
meth public java.lang.String getFieldName()
meth public java.lang.String getIdValue({com.sun.xml.bind.v2.runtime.property.AttributeProperty%0}) throws com.sun.xml.bind.api.AccessorException,org.xml.sax.SAXException
meth public void buildChildElementUnmarshallers(com.sun.xml.bind.v2.runtime.property.UnmarshallerChain,com.sun.xml.bind.v2.util.QNameMap<com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader>)
meth public void reset({com.sun.xml.bind.v2.runtime.property.AttributeProperty%0}) throws com.sun.xml.bind.api.AccessorException
meth public void serializeAttributes({com.sun.xml.bind.v2.runtime.property.AttributeProperty%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void serializeBody({com.sun.xml.bind.v2.runtime.property.AttributeProperty%0},com.sun.xml.bind.v2.runtime.XMLSerializer,java.lang.Object) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void serializeURIs({com.sun.xml.bind.v2.runtime.property.AttributeProperty%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws com.sun.xml.bind.api.AccessorException,org.xml.sax.SAXException
meth public void setHiddenByOverride(boolean)
meth public void wrapUp()
supr java.lang.Object<{com.sun.xml.bind.v2.runtime.property.AttributeProperty%0}>
hfds acc

CLSS public abstract interface com.sun.xml.bind.v2.runtime.property.Property<%0 extends java.lang.Object>
intf com.sun.xml.bind.v2.runtime.property.StructureLoaderBuilder
meth public abstract boolean hasSerializeURIAction()
meth public abstract boolean isHiddenByOverride()
meth public abstract com.sun.xml.bind.v2.model.core.PropertyKind getKind()
meth public abstract com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo getInfo()
meth public abstract com.sun.xml.bind.v2.runtime.reflect.Accessor getElementPropertyAccessor(java.lang.String,java.lang.String)
meth public abstract java.lang.String getFieldName()
meth public abstract java.lang.String getIdValue({com.sun.xml.bind.v2.runtime.property.Property%0}) throws com.sun.xml.bind.api.AccessorException,org.xml.sax.SAXException
meth public abstract void reset({com.sun.xml.bind.v2.runtime.property.Property%0}) throws com.sun.xml.bind.api.AccessorException
meth public abstract void serializeBody({com.sun.xml.bind.v2.runtime.property.Property%0},com.sun.xml.bind.v2.runtime.XMLSerializer,java.lang.Object) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public abstract void serializeURIs({com.sun.xml.bind.v2.runtime.property.Property%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws com.sun.xml.bind.api.AccessorException,org.xml.sax.SAXException
meth public abstract void setHiddenByOverride(boolean)
meth public abstract void wrapUp()

CLSS public abstract com.sun.xml.bind.v2.runtime.property.PropertyFactory
meth public static com.sun.xml.bind.v2.runtime.property.Property create(com.sun.xml.bind.v2.runtime.JAXBContextImpl,com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo)
supr java.lang.Object
hfds propImpls

CLSS public abstract interface com.sun.xml.bind.v2.runtime.property.StructureLoaderBuilder
fld public final static javax.xml.namespace.QName CATCH_ALL
fld public final static javax.xml.namespace.QName TEXT_HANDLER
meth public abstract void buildChildElementUnmarshallers(com.sun.xml.bind.v2.runtime.property.UnmarshallerChain,com.sun.xml.bind.v2.util.QNameMap<com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader>)

CLSS public final com.sun.xml.bind.v2.runtime.property.UnmarshallerChain
cons public init(com.sun.xml.bind.v2.runtime.JAXBContextImpl)
fld public final com.sun.xml.bind.v2.runtime.JAXBContextImpl context
meth public int allocateOffset()
meth public int getScopeSize()
supr java.lang.Object
hfds offset

CLSS public final com.sun.xml.bind.v2.runtime.property.ValueProperty<%0 extends java.lang.Object>
cons public init(com.sun.xml.bind.v2.runtime.JAXBContextImpl,com.sun.xml.bind.v2.model.runtime.RuntimeValuePropertyInfo)
fld protected final java.lang.String fieldName
intf com.sun.xml.bind.v2.runtime.property.Property<{com.sun.xml.bind.v2.runtime.property.ValueProperty%0}>
meth public boolean hasSerializeURIAction()
meth public boolean isHiddenByOverride()
meth public com.sun.xml.bind.v2.model.core.PropertyKind getKind()
meth public com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo getInfo()
meth public com.sun.xml.bind.v2.runtime.reflect.Accessor getElementPropertyAccessor(java.lang.String,java.lang.String)
meth public final void serializeBody({com.sun.xml.bind.v2.runtime.property.ValueProperty%0},com.sun.xml.bind.v2.runtime.XMLSerializer,java.lang.Object) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public java.lang.String getFieldName()
meth public java.lang.String getIdValue({com.sun.xml.bind.v2.runtime.property.ValueProperty%0}) throws com.sun.xml.bind.api.AccessorException,org.xml.sax.SAXException
meth public void buildChildElementUnmarshallers(com.sun.xml.bind.v2.runtime.property.UnmarshallerChain,com.sun.xml.bind.v2.util.QNameMap<com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader>)
meth public void reset({com.sun.xml.bind.v2.runtime.property.ValueProperty%0}) throws com.sun.xml.bind.api.AccessorException
meth public void serializeURIs({com.sun.xml.bind.v2.runtime.property.ValueProperty%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws com.sun.xml.bind.api.AccessorException,org.xml.sax.SAXException
meth public void setHiddenByOverride(boolean)
meth public void wrapUp()
supr java.lang.Object<{com.sun.xml.bind.v2.runtime.property.ValueProperty%0}>
hfds acc,xacc

CLSS public abstract com.sun.xml.bind.v2.runtime.reflect.Accessor<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init(java.lang.Class<{com.sun.xml.bind.v2.runtime.reflect.Accessor%1}>)
fld public final java.lang.Class<{com.sun.xml.bind.v2.runtime.reflect.Accessor%1}> valueType
fld public final static com.sun.xml.bind.v2.runtime.reflect.Accessor<javax.xml.bind.JAXBElement,java.lang.Object> JAXB_ELEMENT_VALUE
innr public final static ReadOnlyFieldReflection
innr public static FieldReflection
innr public static GetterOnlyReflection
innr public static GetterSetterReflection
innr public static SetterOnlyReflection
intf com.sun.xml.bind.v2.runtime.unmarshaller.Receiver
meth public abstract void set({com.sun.xml.bind.v2.runtime.reflect.Accessor%0},{com.sun.xml.bind.v2.runtime.reflect.Accessor%1}) throws com.sun.xml.bind.api.AccessorException
meth public abstract {com.sun.xml.bind.v2.runtime.reflect.Accessor%1} get({com.sun.xml.bind.v2.runtime.reflect.Accessor%0}) throws com.sun.xml.bind.api.AccessorException
meth public boolean isAbstractable(java.lang.Class)
meth public boolean isAdapted()
meth public boolean isValueTypeAbstractable()
meth public com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.Accessor%0},{com.sun.xml.bind.v2.runtime.reflect.Accessor%1}> optimize(com.sun.xml.bind.v2.runtime.JAXBContextImpl)
 anno 1 com.sun.istack.Nullable()
meth public final <%0 extends java.lang.Object> com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.Accessor%0},{%%0}> adapt(com.sun.xml.bind.v2.model.core.Adapter<java.lang.reflect.Type,java.lang.Class>)
meth public final <%0 extends java.lang.Object> com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.Accessor%0},{%%0}> adapt(java.lang.Class<{%%0}>,java.lang.Class<? extends javax.xml.bind.annotation.adapters.XmlAdapter<{%%0},{com.sun.xml.bind.v2.runtime.reflect.Accessor%1}>>)
meth public java.lang.Class<{com.sun.xml.bind.v2.runtime.reflect.Accessor%1}> getValueType()
meth public java.lang.Object getUnadapted({com.sun.xml.bind.v2.runtime.reflect.Accessor%0}) throws com.sun.xml.bind.api.AccessorException
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> com.sun.xml.bind.v2.runtime.reflect.Accessor<{%%0},{%%1}> getErrorInstance()
meth public void receive(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,java.lang.Object) throws org.xml.sax.SAXException
meth public void setUnadapted({com.sun.xml.bind.v2.runtime.reflect.Accessor%0},java.lang.Object) throws com.sun.xml.bind.api.AccessorException
supr java.lang.Object
hfds ERROR,accessWarned,nonAbstractableClasses,uninitializedValues

CLSS public static com.sun.xml.bind.v2.runtime.reflect.Accessor$FieldReflection<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer com.sun.xml.bind.v2.runtime.reflect.Accessor
cons public init(java.lang.reflect.Field)
cons public init(java.lang.reflect.Field,boolean)
fld public final java.lang.reflect.Field f
meth public com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.Accessor$FieldReflection%0},{com.sun.xml.bind.v2.runtime.reflect.Accessor$FieldReflection%1}> optimize(com.sun.xml.bind.v2.runtime.JAXBContextImpl)
meth public void set({com.sun.xml.bind.v2.runtime.reflect.Accessor$FieldReflection%0},{com.sun.xml.bind.v2.runtime.reflect.Accessor$FieldReflection%1})
meth public {com.sun.xml.bind.v2.runtime.reflect.Accessor$FieldReflection%1} get({com.sun.xml.bind.v2.runtime.reflect.Accessor$FieldReflection%0})
supr com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.Accessor$FieldReflection%0},{com.sun.xml.bind.v2.runtime.reflect.Accessor$FieldReflection%1}>
hfds logger

CLSS public static com.sun.xml.bind.v2.runtime.reflect.Accessor$GetterOnlyReflection<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer com.sun.xml.bind.v2.runtime.reflect.Accessor
cons public init(java.lang.reflect.Method)
meth public void set({com.sun.xml.bind.v2.runtime.reflect.Accessor$GetterOnlyReflection%0},{com.sun.xml.bind.v2.runtime.reflect.Accessor$GetterOnlyReflection%1}) throws com.sun.xml.bind.api.AccessorException
supr com.sun.xml.bind.v2.runtime.reflect.Accessor$GetterSetterReflection<{com.sun.xml.bind.v2.runtime.reflect.Accessor$GetterOnlyReflection%0},{com.sun.xml.bind.v2.runtime.reflect.Accessor$GetterOnlyReflection%1}>

CLSS public static com.sun.xml.bind.v2.runtime.reflect.Accessor$GetterSetterReflection<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer com.sun.xml.bind.v2.runtime.reflect.Accessor
cons public init(java.lang.reflect.Method,java.lang.reflect.Method)
fld public final java.lang.reflect.Method getter
fld public final java.lang.reflect.Method setter
meth public com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.Accessor$GetterSetterReflection%0},{com.sun.xml.bind.v2.runtime.reflect.Accessor$GetterSetterReflection%1}> optimize(com.sun.xml.bind.v2.runtime.JAXBContextImpl)
meth public void set({com.sun.xml.bind.v2.runtime.reflect.Accessor$GetterSetterReflection%0},{com.sun.xml.bind.v2.runtime.reflect.Accessor$GetterSetterReflection%1}) throws com.sun.xml.bind.api.AccessorException
meth public {com.sun.xml.bind.v2.runtime.reflect.Accessor$GetterSetterReflection%1} get({com.sun.xml.bind.v2.runtime.reflect.Accessor$GetterSetterReflection%0}) throws com.sun.xml.bind.api.AccessorException
supr com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.Accessor$GetterSetterReflection%0},{com.sun.xml.bind.v2.runtime.reflect.Accessor$GetterSetterReflection%1}>
hfds logger

CLSS public final static com.sun.xml.bind.v2.runtime.reflect.Accessor$ReadOnlyFieldReflection<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer com.sun.xml.bind.v2.runtime.reflect.Accessor
cons public init(java.lang.reflect.Field)
cons public init(java.lang.reflect.Field,boolean)
meth public com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.Accessor$ReadOnlyFieldReflection%0},{com.sun.xml.bind.v2.runtime.reflect.Accessor$ReadOnlyFieldReflection%1}> optimize(com.sun.xml.bind.v2.runtime.JAXBContextImpl)
meth public void set({com.sun.xml.bind.v2.runtime.reflect.Accessor$ReadOnlyFieldReflection%0},{com.sun.xml.bind.v2.runtime.reflect.Accessor$ReadOnlyFieldReflection%1})
supr com.sun.xml.bind.v2.runtime.reflect.Accessor$FieldReflection<{com.sun.xml.bind.v2.runtime.reflect.Accessor$ReadOnlyFieldReflection%0},{com.sun.xml.bind.v2.runtime.reflect.Accessor$ReadOnlyFieldReflection%1}>

CLSS public static com.sun.xml.bind.v2.runtime.reflect.Accessor$SetterOnlyReflection<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer com.sun.xml.bind.v2.runtime.reflect.Accessor
cons public init(java.lang.reflect.Method)
meth public {com.sun.xml.bind.v2.runtime.reflect.Accessor$SetterOnlyReflection%1} get({com.sun.xml.bind.v2.runtime.reflect.Accessor$SetterOnlyReflection%0}) throws com.sun.xml.bind.api.AccessorException
supr com.sun.xml.bind.v2.runtime.reflect.Accessor$GetterSetterReflection<{com.sun.xml.bind.v2.runtime.reflect.Accessor$SetterOnlyReflection%0},{com.sun.xml.bind.v2.runtime.reflect.Accessor$SetterOnlyReflection%1}>

CLSS public abstract com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor<%0 extends java.lang.Object>
cons public init()
meth public abstract java.lang.String print({com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor%0}) throws com.sun.xml.bind.api.AccessorException,org.xml.sax.SAXException
meth public void writeLeafElement(com.sun.xml.bind.v2.runtime.XMLSerializer,com.sun.xml.bind.v2.runtime.Name,{com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor%0},java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void writeText(com.sun.xml.bind.v2.runtime.XMLSerializer,{com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor%0},java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor<{com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor%0}>

CLSS public abstract interface com.sun.xml.bind.v2.runtime.reflect.ListIterator<%0 extends java.lang.Object>
meth public abstract boolean hasNext()
meth public abstract {com.sun.xml.bind.v2.runtime.reflect.ListIterator%0} next() throws javax.xml.bind.JAXBException,org.xml.sax.SAXException

CLSS public final com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object>
cons public init(com.sun.xml.bind.v2.runtime.Transducer<{com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl%2}>,com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl%0},{com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl%1}>,com.sun.xml.bind.v2.runtime.reflect.Lister<{com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl%0},{com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl%1},{com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl%2},{com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl%3}>)
meth public boolean hasValue({com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl%0}) throws com.sun.xml.bind.api.AccessorException
meth public boolean useNamespace()
meth public java.lang.String print({com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl%0}) throws com.sun.xml.bind.api.AccessorException,org.xml.sax.SAXException
meth public void declareNamespace({com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws com.sun.xml.bind.api.AccessorException,org.xml.sax.SAXException
meth public void parse({com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl%0},java.lang.CharSequence) throws com.sun.xml.bind.api.AccessorException,org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor<{com.sun.xml.bind.v2.runtime.reflect.ListTransducedAccessorImpl%0}>
hfds acc,lister,xducer

CLSS public abstract com.sun.xml.bind.v2.runtime.reflect.Lister<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object>
cons protected init()
fld public final static com.sun.xml.bind.v2.runtime.reflect.Lister ERROR
innr public final static CollectionLister
innr public final static IDREFSIterator
innr public final static Pack
meth public abstract com.sun.xml.bind.v2.runtime.reflect.ListIterator<{com.sun.xml.bind.v2.runtime.reflect.Lister%2}> iterator({com.sun.xml.bind.v2.runtime.reflect.Lister%1},com.sun.xml.bind.v2.runtime.XMLSerializer)
meth public abstract void addToPack({com.sun.xml.bind.v2.runtime.reflect.Lister%3},{com.sun.xml.bind.v2.runtime.reflect.Lister%2}) throws com.sun.xml.bind.api.AccessorException
meth public abstract void endPacking({com.sun.xml.bind.v2.runtime.reflect.Lister%3},{com.sun.xml.bind.v2.runtime.reflect.Lister%0},com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.Lister%0},{com.sun.xml.bind.v2.runtime.reflect.Lister%1}>) throws com.sun.xml.bind.api.AccessorException
meth public abstract void reset({com.sun.xml.bind.v2.runtime.reflect.Lister%0},com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.Lister%0},{com.sun.xml.bind.v2.runtime.reflect.Lister%1}>) throws com.sun.xml.bind.api.AccessorException
meth public abstract {com.sun.xml.bind.v2.runtime.reflect.Lister%3} startPacking({com.sun.xml.bind.v2.runtime.reflect.Lister%0},com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.Lister%0},{com.sun.xml.bind.v2.runtime.reflect.Lister%1}>) throws com.sun.xml.bind.api.AccessorException
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object> com.sun.xml.bind.v2.runtime.reflect.Lister<{%%0},{%%1},{%%2},{%%3}> create(java.lang.reflect.Type,com.sun.xml.bind.v2.model.core.ID,com.sun.xml.bind.v2.model.core.Adapter<java.lang.reflect.Type,java.lang.Class>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object> com.sun.xml.bind.v2.runtime.reflect.Lister<{%%0},{%%1},{%%2},{%%3}> getErrorInstance()
supr java.lang.Object
hfds COLLECTION_IMPL_CLASSES,EMPTY_ITERATOR,arrayListerCache,primitiveArrayListers
hcls ArrayLister,IDREFS

CLSS public final static com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister<%0 extends java.lang.Object, %1 extends java.util.Collection>
 outer com.sun.xml.bind.v2.runtime.reflect.Lister
cons public init(java.lang.Class<? extends {com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%1}>)
meth public com.sun.xml.bind.v2.runtime.reflect.ListIterator iterator({com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%1},com.sun.xml.bind.v2.runtime.XMLSerializer)
meth public void addToPack({com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%1},java.lang.Object)
meth public void endPacking({com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%1},{com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%0},com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%0},{com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%1}>) throws com.sun.xml.bind.api.AccessorException
meth public void reset({com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%0},com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%0},{com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%1}>) throws com.sun.xml.bind.api.AccessorException
meth public {com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%1} startPacking({com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%0},com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%0},{com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%1}>) throws com.sun.xml.bind.api.AccessorException
supr com.sun.xml.bind.v2.runtime.reflect.Lister<{com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%0},{com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%1},java.lang.Object,{com.sun.xml.bind.v2.runtime.reflect.Lister$CollectionLister%1}>
hfds implClass

CLSS public final static com.sun.xml.bind.v2.runtime.reflect.Lister$IDREFSIterator
 outer com.sun.xml.bind.v2.runtime.reflect.Lister
intf com.sun.xml.bind.v2.runtime.reflect.ListIterator<java.lang.String>
meth public boolean hasNext()
meth public java.lang.Object last()
meth public java.lang.String next() throws javax.xml.bind.JAXBException,org.xml.sax.SAXException
supr java.lang.Object
hfds context,i,last

CLSS public final static com.sun.xml.bind.v2.runtime.reflect.Lister$Pack<%0 extends java.lang.Object>
 outer com.sun.xml.bind.v2.runtime.reflect.Lister
cons public init(java.lang.Class<{com.sun.xml.bind.v2.runtime.reflect.Lister$Pack%0}>)
meth public {com.sun.xml.bind.v2.runtime.reflect.Lister$Pack%0}[] build()
supr java.util.ArrayList<{com.sun.xml.bind.v2.runtime.reflect.Lister$Pack%0}>
hfds itemType

CLSS public com.sun.xml.bind.v2.runtime.reflect.NullSafeAccessor<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object>
cons public init(com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.NullSafeAccessor%0},{com.sun.xml.bind.v2.runtime.reflect.NullSafeAccessor%1}>,com.sun.xml.bind.v2.runtime.reflect.Lister<{com.sun.xml.bind.v2.runtime.reflect.NullSafeAccessor%0},{com.sun.xml.bind.v2.runtime.reflect.NullSafeAccessor%1},?,{com.sun.xml.bind.v2.runtime.reflect.NullSafeAccessor%2}>)
meth public void set({com.sun.xml.bind.v2.runtime.reflect.NullSafeAccessor%0},{com.sun.xml.bind.v2.runtime.reflect.NullSafeAccessor%1}) throws com.sun.xml.bind.api.AccessorException
meth public {com.sun.xml.bind.v2.runtime.reflect.NullSafeAccessor%1} get({com.sun.xml.bind.v2.runtime.reflect.NullSafeAccessor%0}) throws com.sun.xml.bind.api.AccessorException
supr com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.NullSafeAccessor%0},{com.sun.xml.bind.v2.runtime.reflect.NullSafeAccessor%1}>
hfds core,lister

CLSS public abstract com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor<%0 extends java.lang.Object>
cons public init()
innr public static CompositeTransducedAccessorImpl
meth public abstract boolean hasValue({com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor%0}) throws com.sun.xml.bind.api.AccessorException
meth public abstract java.lang.CharSequence print({com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor%0}) throws com.sun.xml.bind.api.AccessorException,org.xml.sax.SAXException
 anno 0 com.sun.istack.Nullable()
 anno 1 com.sun.istack.NotNull()
meth public abstract void parse({com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor%0},java.lang.CharSequence) throws com.sun.xml.bind.api.AccessorException,org.xml.sax.SAXException
meth public abstract void writeLeafElement(com.sun.xml.bind.v2.runtime.XMLSerializer,com.sun.xml.bind.v2.runtime.Name,{com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor%0},java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public abstract void writeText(com.sun.xml.bind.v2.runtime.XMLSerializer,{com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor%0},java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public boolean useNamespace()
meth public static <%0 extends java.lang.Object> com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor<{%%0}> get(com.sun.xml.bind.v2.runtime.JAXBContextImpl,com.sun.xml.bind.v2.model.runtime.RuntimeNonElementRef)
meth public void declareNamespace({com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor%0},com.sun.xml.bind.v2.runtime.XMLSerializer) throws com.sun.xml.bind.api.AccessorException,org.xml.sax.SAXException
supr java.lang.Object
hcls CompositeContextDependentTransducedAccessorImpl,IDREFTransducedAccessorImpl

CLSS public static com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor$CompositeTransducedAccessorImpl<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor
cons public init(com.sun.xml.bind.v2.runtime.JAXBContextImpl,com.sun.xml.bind.v2.runtime.Transducer<{com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor$CompositeTransducedAccessorImpl%1}>,com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor$CompositeTransducedAccessorImpl%0},{com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor$CompositeTransducedAccessorImpl%1}>)
fld protected final com.sun.xml.bind.v2.runtime.Transducer<{com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor$CompositeTransducedAccessorImpl%1}> xducer
fld protected final com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor$CompositeTransducedAccessorImpl%0},{com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor$CompositeTransducedAccessorImpl%1}> acc
meth public boolean hasValue({com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor$CompositeTransducedAccessorImpl%0}) throws com.sun.xml.bind.api.AccessorException
meth public java.lang.CharSequence print({com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor$CompositeTransducedAccessorImpl%0}) throws com.sun.xml.bind.api.AccessorException
meth public void parse({com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor$CompositeTransducedAccessorImpl%0},java.lang.CharSequence) throws com.sun.xml.bind.api.AccessorException,org.xml.sax.SAXException
meth public void writeLeafElement(com.sun.xml.bind.v2.runtime.XMLSerializer,com.sun.xml.bind.v2.runtime.Name,{com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor$CompositeTransducedAccessorImpl%0},java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
meth public void writeText(com.sun.xml.bind.v2.runtime.XMLSerializer,{com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor$CompositeTransducedAccessorImpl%0},java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor<{com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor$CompositeTransducedAccessorImpl%0}>

CLSS public final com.sun.xml.bind.v2.runtime.reflect.opt.Const
cons public init()
fld public final static boolean default_value_boolean = false
fld public final static byte default_value_byte = 0
fld public final static char default_value_char = '\u0000'
fld public final static double default_value_double = 0.0
fld public final static float default_value_float = 0.0
fld public final static int default_value_int = 0
fld public final static long default_value_long = 0
fld public final static short default_value_short = 0
supr java.lang.Object

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.FieldAccessor_Boolean
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.FieldAccessor_Byte
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.FieldAccessor_Character
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.FieldAccessor_Double
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.FieldAccessor_Float
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.FieldAccessor_Integer
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.FieldAccessor_Long
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.FieldAccessor_Ref
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.FieldAccessor_Short
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.MethodAccessor_Boolean
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.MethodAccessor_Byte
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.MethodAccessor_Character
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.MethodAccessor_Double
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.MethodAccessor_Float
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.MethodAccessor_Integer
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.MethodAccessor_Long
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.MethodAccessor_Ref
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public com.sun.xml.bind.v2.runtime.reflect.opt.MethodAccessor_Short
cons public init()
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr com.sun.xml.bind.v2.runtime.reflect.Accessor

CLSS public final com.sun.xml.bind.v2.runtime.reflect.opt.TransducedAccessor_field_Boolean
cons public init()
meth public boolean hasValue(java.lang.Object)
meth public java.lang.String print(java.lang.Object)
meth public void parse(java.lang.Object,java.lang.CharSequence)
supr com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor

CLSS public final com.sun.xml.bind.v2.runtime.reflect.opt.TransducedAccessor_field_Byte
cons public init()
meth public boolean hasValue(java.lang.Object)
meth public java.lang.String print(java.lang.Object)
meth public void parse(java.lang.Object,java.lang.CharSequence)
supr com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor

CLSS public final com.sun.xml.bind.v2.runtime.reflect.opt.TransducedAccessor_field_Double
cons public init()
meth public boolean hasValue(java.lang.Object)
meth public java.lang.String print(java.lang.Object)
meth public void parse(java.lang.Object,java.lang.CharSequence)
supr com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor

CLSS public final com.sun.xml.bind.v2.runtime.reflect.opt.TransducedAccessor_field_Float
cons public init()
meth public boolean hasValue(java.lang.Object)
meth public java.lang.String print(java.lang.Object)
meth public void parse(java.lang.Object,java.lang.CharSequence)
supr com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor

CLSS public final com.sun.xml.bind.v2.runtime.reflect.opt.TransducedAccessor_field_Integer
cons public init()
meth public boolean hasValue(java.lang.Object)
meth public java.lang.String print(java.lang.Object)
meth public void parse(java.lang.Object,java.lang.CharSequence)
meth public void writeLeafElement(com.sun.xml.bind.v2.runtime.XMLSerializer,com.sun.xml.bind.v2.runtime.Name,java.lang.Object,java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor

CLSS public final com.sun.xml.bind.v2.runtime.reflect.opt.TransducedAccessor_field_Long
cons public init()
meth public boolean hasValue(java.lang.Object)
meth public java.lang.String print(java.lang.Object)
meth public void parse(java.lang.Object,java.lang.CharSequence)
supr com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor

CLSS public final com.sun.xml.bind.v2.runtime.reflect.opt.TransducedAccessor_field_Short
cons public init()
meth public boolean hasValue(java.lang.Object)
meth public java.lang.String print(java.lang.Object)
meth public void parse(java.lang.Object,java.lang.CharSequence)
supr com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor

CLSS public final com.sun.xml.bind.v2.runtime.reflect.opt.TransducedAccessor_method_Boolean
cons public init()
meth public boolean hasValue(java.lang.Object)
meth public java.lang.String print(java.lang.Object)
meth public void parse(java.lang.Object,java.lang.CharSequence)
supr com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor

CLSS public final com.sun.xml.bind.v2.runtime.reflect.opt.TransducedAccessor_method_Byte
cons public init()
meth public boolean hasValue(java.lang.Object)
meth public java.lang.String print(java.lang.Object)
meth public void parse(java.lang.Object,java.lang.CharSequence)
supr com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor

CLSS public final com.sun.xml.bind.v2.runtime.reflect.opt.TransducedAccessor_method_Double
cons public init()
meth public boolean hasValue(java.lang.Object)
meth public java.lang.String print(java.lang.Object)
meth public void parse(java.lang.Object,java.lang.CharSequence)
supr com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor

CLSS public final com.sun.xml.bind.v2.runtime.reflect.opt.TransducedAccessor_method_Float
cons public init()
meth public boolean hasValue(java.lang.Object)
meth public java.lang.String print(java.lang.Object)
meth public void parse(java.lang.Object,java.lang.CharSequence)
supr com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor

CLSS public final com.sun.xml.bind.v2.runtime.reflect.opt.TransducedAccessor_method_Integer
cons public init()
meth public boolean hasValue(java.lang.Object)
meth public java.lang.String print(java.lang.Object)
meth public void parse(java.lang.Object,java.lang.CharSequence)
meth public void writeLeafElement(com.sun.xml.bind.v2.runtime.XMLSerializer,com.sun.xml.bind.v2.runtime.Name,java.lang.Object,java.lang.String) throws com.sun.xml.bind.api.AccessorException,java.io.IOException,javax.xml.stream.XMLStreamException,org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor

CLSS public final com.sun.xml.bind.v2.runtime.reflect.opt.TransducedAccessor_method_Long
cons public init()
meth public boolean hasValue(java.lang.Object)
meth public java.lang.String print(java.lang.Object)
meth public void parse(java.lang.Object,java.lang.CharSequence)
supr com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor

CLSS public final com.sun.xml.bind.v2.runtime.reflect.opt.TransducedAccessor_method_Short
cons public init()
meth public boolean hasValue(java.lang.Object)
meth public java.lang.String print(java.lang.Object)
meth public void parse(java.lang.Object,java.lang.CharSequence)
supr com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor

CLSS public abstract interface com.sun.xml.bind.v2.runtime.unmarshaller.AttributesEx
intf org.xml.sax.Attributes
meth public abstract java.lang.CharSequence getData(int)
meth public abstract java.lang.CharSequence getData(java.lang.String,java.lang.String)

CLSS public final com.sun.xml.bind.v2.runtime.unmarshaller.AttributesExImpl
cons public init()
intf com.sun.xml.bind.v2.runtime.unmarshaller.AttributesEx
meth public java.lang.CharSequence getData(int)
meth public java.lang.CharSequence getData(java.lang.String,java.lang.String)
supr com.sun.xml.bind.util.AttributesImpl

CLSS public final com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data
cons public init()
meth public boolean hasData()
meth public byte[] get()
meth public byte[] getExact()
meth public char charAt(int)
meth public int getDataLen()
meth public int length()
meth public java.io.InputStream getInputStream() throws java.io.IOException
meth public java.lang.CharSequence subSequence(int,int)
meth public java.lang.String getMimeType()
meth public java.lang.String toString()
meth public javax.activation.DataHandler getDataHandler()
meth public void set(byte[],int,java.lang.String)
 anno 3 com.sun.istack.Nullable()
meth public void set(byte[],java.lang.String)
 anno 2 com.sun.istack.Nullable()
meth public void set(javax.activation.DataHandler)
meth public void writeTo(char[],int)
meth public void writeTo(com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput) throws java.io.IOException
meth public void writeTo(javax.xml.stream.XMLStreamWriter) throws java.io.IOException,javax.xml.stream.XMLStreamException
supr com.sun.xml.bind.v2.runtime.output.Pcdata
hfds data,dataHandler,dataLen,mimeType

CLSS public final com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader
cons public init(com.sun.xml.bind.v2.runtime.unmarshaller.Loader,com.sun.xml.bind.v2.runtime.unmarshaller.Receiver)
fld public final com.sun.xml.bind.v2.runtime.unmarshaller.Loader loader
fld public final com.sun.xml.bind.v2.runtime.unmarshaller.Receiver receiver
supr java.lang.Object

CLSS public final com.sun.xml.bind.v2.runtime.unmarshaller.DefaultValueLoaderDecorator
cons public init(com.sun.xml.bind.v2.runtime.unmarshaller.Loader,java.lang.String)
meth public void startElement(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.unmarshaller.Loader
hfds defaultValue,l

CLSS public final com.sun.xml.bind.v2.runtime.unmarshaller.Discarder
fld public final static com.sun.xml.bind.v2.runtime.unmarshaller.Loader INSTANCE
meth public void childElement(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName)
supr com.sun.xml.bind.v2.runtime.unmarshaller.Loader

CLSS public com.sun.xml.bind.v2.runtime.unmarshaller.DomLoader<%0 extends javax.xml.transform.Result>
cons public init(javax.xml.bind.annotation.DomHandler<?,{com.sun.xml.bind.v2.runtime.unmarshaller.DomLoader%0}>)
meth public void childElement(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public void leaveElement(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public void startElement(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public void text(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,java.lang.CharSequence) throws org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.unmarshaller.Loader
hfds dom
hcls State

CLSS public final com.sun.xml.bind.v2.runtime.unmarshaller.IntArrayData
cons public init()
cons public init(int[],int,int)
meth public char charAt(int)
meth public int length()
meth public java.lang.CharSequence subSequence(int,int)
meth public java.lang.String toString()
meth public void set(int[],int,int)
meth public void writeTo(com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput) throws java.io.IOException
supr com.sun.xml.bind.v2.runtime.output.Pcdata
hfds data,len,literal,start

CLSS public com.sun.xml.bind.v2.runtime.unmarshaller.IntData
cons public init()
meth public char charAt(int)
meth public int length()
meth public java.lang.CharSequence subSequence(int,int)
meth public java.lang.String toString()
meth public void reset(int)
meth public void writeTo(com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput) throws java.io.IOException
supr com.sun.xml.bind.v2.runtime.output.Pcdata
hfds data,length,sizeTable

CLSS public abstract interface com.sun.xml.bind.v2.runtime.unmarshaller.Intercepter
meth public abstract java.lang.Object intercept(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,java.lang.Object) throws org.xml.sax.SAXException

CLSS public final com.sun.xml.bind.v2.runtime.unmarshaller.InterningXmlVisitor
cons public init(com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor)
intf com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor
meth public com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext getContext()
meth public com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor$TextPredictor getPredictor()
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument(com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx,javax.xml.namespace.NamespaceContext) throws org.xml.sax.SAXException
meth public void startElement(com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void text(java.lang.CharSequence) throws org.xml.sax.SAXException
supr java.lang.Object
hfds attributes,next
hcls AttributesImpl

CLSS public com.sun.xml.bind.v2.runtime.unmarshaller.LeafPropertyLoader
cons public init(com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor)
meth public void text(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,java.lang.CharSequence) throws org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.unmarshaller.Loader
hfds xacc

CLSS public final com.sun.xml.bind.v2.runtime.unmarshaller.LeafPropertyXsiLoader
cons public init(com.sun.xml.bind.v2.runtime.unmarshaller.Loader,com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor,com.sun.xml.bind.v2.runtime.reflect.Accessor)
meth protected com.sun.xml.bind.v2.runtime.unmarshaller.Loader selectLoader(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public java.util.Collection<javax.xml.namespace.QName> getExpectedAttributes()
meth public java.util.Collection<javax.xml.namespace.QName> getExpectedChildElements()
meth public void startElement(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.unmarshaller.Loader
hfds acc,defaultLoader,xacc

CLSS public abstract com.sun.xml.bind.v2.runtime.unmarshaller.Loader
cons protected init()
cons protected init(boolean)
fld protected boolean expectText
meth protected final void fireAfterUnmarshal(com.sun.xml.bind.v2.runtime.JaxBeanInfo,java.lang.Object,com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State) throws org.xml.sax.SAXException
meth protected final void fireBeforeUnmarshal(com.sun.xml.bind.v2.runtime.JaxBeanInfo,java.lang.Object,com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State) throws org.xml.sax.SAXException
meth protected final void reportUnexpectedChildElement(com.sun.xml.bind.v2.runtime.unmarshaller.TagName,boolean) throws org.xml.sax.SAXException
meth protected static void handleGenericException(java.lang.Exception) throws org.xml.sax.SAXException
meth protected static void handleParseConversionException(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,java.lang.Exception) throws org.xml.sax.SAXException
meth protected static void reportError(java.lang.String,boolean) throws org.xml.sax.SAXException
meth public final boolean expectText()
meth public java.util.Collection<javax.xml.namespace.QName> getExpectedAttributes()
meth public java.util.Collection<javax.xml.namespace.QName> getExpectedChildElements()
meth public static void handleGenericError(java.lang.Error) throws org.xml.sax.SAXException
meth public static void handleGenericException(java.lang.Exception,boolean) throws org.xml.sax.SAXException
meth public static void reportError(java.lang.String,java.lang.Exception,boolean) throws org.xml.sax.SAXException
meth public void childElement(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public void leaveElement(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public void startElement(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public void text(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,java.lang.CharSequence) throws org.xml.sax.SAXException
supr java.lang.Object

CLSS public abstract interface com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx
innr public final static Snapshot
intf org.xml.sax.Locator
meth public abstract javax.xml.bind.ValidationEventLocator getLocation()

CLSS public final static com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx$Snapshot
 outer com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx
cons public init(com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx)
intf com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx
intf javax.xml.bind.ValidationEventLocator
meth public int getColumnNumber()
meth public int getLineNumber()
meth public int getOffset()
meth public java.lang.Object getObject()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
meth public java.net.URL getURL()
meth public javax.xml.bind.ValidationEventLocator getLocation()
meth public org.w3c.dom.Node getNode()
supr java.lang.Object
hfds columnNumber,lineNumber,node,object,offset,publicId,systemId,url

CLSS public abstract interface com.sun.xml.bind.v2.runtime.unmarshaller.Patcher
meth public abstract void run() throws org.xml.sax.SAXException

CLSS public abstract com.sun.xml.bind.v2.runtime.unmarshaller.ProxyLoader
cons public init()
meth protected abstract com.sun.xml.bind.v2.runtime.unmarshaller.Loader selectLoader(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public final void leaveElement(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName)
meth public final void startElement(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.unmarshaller.Loader

CLSS public abstract interface com.sun.xml.bind.v2.runtime.unmarshaller.Receiver
meth public abstract void receive(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,java.lang.Object) throws org.xml.sax.SAXException

CLSS public final com.sun.xml.bind.v2.runtime.unmarshaller.SAXConnector
cons public init(com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor,com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx)
intf javax.xml.bind.UnmarshallerHandler
meth public com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext getContext()
meth public final void characters(char[],int,int)
meth public final void ignorableWhitespace(char[],int,int)
meth public java.lang.Object getResult() throws javax.xml.bind.JAXBException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String)
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void skippedEntity(java.lang.String)
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
supr java.lang.Object
hfds buffer,context,loc,logger,next,predictor,tagName
hcls TagNameImpl

CLSS public final com.sun.xml.bind.v2.runtime.unmarshaller.Scope<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object>
fld public final com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext context
meth public boolean hasStarted()
meth public void add(com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.unmarshaller.Scope%0},{com.sun.xml.bind.v2.runtime.unmarshaller.Scope%1}>,com.sun.xml.bind.v2.runtime.reflect.Lister<{com.sun.xml.bind.v2.runtime.unmarshaller.Scope%0},{com.sun.xml.bind.v2.runtime.unmarshaller.Scope%1},{com.sun.xml.bind.v2.runtime.unmarshaller.Scope%2},{com.sun.xml.bind.v2.runtime.unmarshaller.Scope%3}>,{com.sun.xml.bind.v2.runtime.unmarshaller.Scope%2}) throws org.xml.sax.SAXException
meth public void finish() throws com.sun.xml.bind.api.AccessorException
meth public void reset()
meth public void start(com.sun.xml.bind.v2.runtime.reflect.Accessor<{com.sun.xml.bind.v2.runtime.unmarshaller.Scope%0},{com.sun.xml.bind.v2.runtime.unmarshaller.Scope%1}>,com.sun.xml.bind.v2.runtime.reflect.Lister<{com.sun.xml.bind.v2.runtime.unmarshaller.Scope%0},{com.sun.xml.bind.v2.runtime.unmarshaller.Scope%1},{com.sun.xml.bind.v2.runtime.unmarshaller.Scope%2},{com.sun.xml.bind.v2.runtime.unmarshaller.Scope%3}>) throws org.xml.sax.SAXException
supr java.lang.Object
hfds acc,bean,lister,pack

CLSS public final com.sun.xml.bind.v2.runtime.unmarshaller.StructureLoader
cons public init(com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl)
meth public com.sun.xml.bind.v2.runtime.JaxBeanInfo getBeanInfo()
meth public java.util.Collection<javax.xml.namespace.QName> getExpectedAttributes()
meth public java.util.Collection<javax.xml.namespace.QName> getExpectedChildElements()
meth public void childElement(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public void init(com.sun.xml.bind.v2.runtime.JAXBContextImpl,com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl,com.sun.xml.bind.v2.runtime.reflect.Accessor<?,java.util.Map<javax.xml.namespace.QName,java.lang.String>>)
meth public void leaveElement(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public void startElement(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public void text(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,java.lang.CharSequence) throws org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.unmarshaller.Loader
hfds EMPTY,attCatchAll,attUnmarshallers,beanInfo,catchAll,childUnmarshallers,frameSize,textHandler

CLSS public abstract com.sun.xml.bind.v2.runtime.unmarshaller.TagName
cons public init()
fld public java.lang.String local
fld public java.lang.String uri
fld public org.xml.sax.Attributes atts
meth public abstract java.lang.String getQname()
meth public final boolean matches(com.sun.xml.bind.v2.runtime.Name)
meth public final boolean matches(java.lang.String,java.lang.String)
meth public java.lang.String getPrefix()
meth public java.lang.String toString()
meth public javax.xml.namespace.QName createQName()
supr java.lang.Object

CLSS public com.sun.xml.bind.v2.runtime.unmarshaller.TextLoader
cons public init(com.sun.xml.bind.v2.runtime.Transducer)
meth public void text(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,java.lang.CharSequence) throws org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.unmarshaller.Loader
hfds xducer

CLSS public final com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl
cons public init(com.sun.xml.bind.v2.runtime.JAXBContextImpl,com.sun.xml.bind.v2.runtime.AssociationMap)
fld protected final com.sun.xml.bind.v2.runtime.JAXBContextImpl context
fld public final com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext coordinator
fld public final static java.lang.String FACTORY = "com.sun.xml.bind.ObjectFactory"
intf java.io.Closeable
intf javax.xml.bind.ValidationEventHandler
meth protected <%0 extends java.lang.Object> javax.xml.bind.JAXBElement<{%%0}> unmarshal(org.xml.sax.XMLReader,org.xml.sax.InputSource,java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth protected java.lang.Object unmarshal(org.xml.sax.XMLReader,org.xml.sax.InputSource) throws javax.xml.bind.JAXBException
meth protected org.xml.sax.XMLReader getXMLReader() throws javax.xml.bind.JAXBException
meth protected void finalize() throws java.lang.Throwable
meth public <%0 extends java.lang.Object> com.sun.xml.bind.v2.runtime.JaxBeanInfo<{%%0}> getBeanInfo(java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth public <%0 extends java.lang.Object> javax.xml.bind.JAXBElement<{%%0}> unmarshal(javax.xml.stream.XMLEventReader,java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth public <%0 extends java.lang.Object> javax.xml.bind.JAXBElement<{%%0}> unmarshal(javax.xml.stream.XMLStreamReader,java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth public <%0 extends java.lang.Object> javax.xml.bind.JAXBElement<{%%0}> unmarshal(javax.xml.transform.Source,java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth public <%0 extends java.lang.Object> javax.xml.bind.JAXBElement<{%%0}> unmarshal(org.w3c.dom.Node,java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth public <%0 extends javax.xml.bind.annotation.adapters.XmlAdapter> void setAdapter(java.lang.Class<{%%0}>,{%%0})
meth public <%0 extends javax.xml.bind.annotation.adapters.XmlAdapter> {%%0} getAdapter(java.lang.Class<{%%0}>)
meth public boolean handleEvent(javax.xml.bind.ValidationEvent)
meth public boolean isValidating()
meth public com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext getContext()
meth public final boolean hasEventHandler()
meth public final com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor createUnmarshallerHandler(com.sun.xml.bind.unmarshaller.InfosetScanner,boolean,com.sun.xml.bind.v2.runtime.JaxBeanInfo)
meth public final java.lang.Object unmarshal(javax.xml.transform.sax.SAXSource) throws javax.xml.bind.JAXBException
 anno 0 java.lang.Deprecated()
meth public final java.lang.Object unmarshal(org.w3c.dom.Node) throws javax.xml.bind.JAXBException
meth public final java.lang.Object unmarshal0(org.w3c.dom.Node,com.sun.xml.bind.v2.runtime.JaxBeanInfo) throws javax.xml.bind.JAXBException
meth public final javax.xml.bind.ValidationEventHandler getEventHandler()
meth public java.lang.Object getProperty(java.lang.String) throws javax.xml.bind.PropertyException
meth public java.lang.Object unmarshal(javax.xml.stream.XMLEventReader) throws javax.xml.bind.JAXBException
meth public java.lang.Object unmarshal(javax.xml.stream.XMLStreamReader) throws javax.xml.bind.JAXBException
meth public java.lang.Object unmarshal0(java.io.InputStream,com.sun.xml.bind.v2.runtime.JaxBeanInfo) throws javax.xml.bind.JAXBException
meth public java.lang.Object unmarshal0(javax.xml.stream.XMLStreamReader,com.sun.xml.bind.v2.runtime.JaxBeanInfo) throws javax.xml.bind.JAXBException
meth public java.lang.Object unmarshal0(javax.xml.transform.Source,com.sun.xml.bind.v2.runtime.JaxBeanInfo) throws javax.xml.bind.JAXBException
meth public javax.xml.bind.UnmarshalException createUnmarshalException(org.xml.sax.SAXException)
meth public javax.xml.bind.Unmarshaller$Listener getListener()
meth public javax.xml.bind.UnmarshallerHandler getUnmarshallerHandler()
meth public javax.xml.bind.attachment.AttachmentUnmarshaller getAttachmentUnmarshaller()
meth public javax.xml.validation.Schema getSchema()
meth public static boolean needsInterning(org.xml.sax.XMLReader)
meth public void close() throws java.io.IOException
meth public void setAttachmentUnmarshaller(javax.xml.bind.attachment.AttachmentUnmarshaller)
meth public void setListener(javax.xml.bind.Unmarshaller$Listener)
meth public void setProperty(java.lang.String,java.lang.Object) throws javax.xml.bind.PropertyException
meth public void setSchema(javax.xml.validation.Schema)
meth public void setValidating(boolean)
supr javax.xml.bind.helpers.AbstractUnmarshallerImpl
hfds attachmentUnmarshaller,dummyHandler,externalListener,idResolver,reader,schema

CLSS public final com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext
cons public init(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl,com.sun.xml.bind.v2.runtime.AssociationMap)
fld public com.sun.xml.bind.api.ClassResolver classResolver
 anno 0 com.sun.istack.Nullable()
fld public final com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl parent
fld public java.lang.ClassLoader classLoader
 anno 0 com.sun.istack.Nullable()
innr public final State
intf com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor
intf com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor$TextPredictor
intf javax.xml.bind.ValidationEventHandler
intf javax.xml.namespace.NamespaceContext
intf org.xml.sax.ErrorHandler
meth protected javax.xml.bind.ValidationEventLocator getLocation()
meth public boolean expectText()
 anno 0 java.lang.Deprecated()
meth public boolean handleEvent(javax.xml.bind.ValidationEvent)
meth public boolean shouldErrorBeReported() throws org.xml.sax.SAXException
meth public com.sun.xml.bind.v2.runtime.JAXBContextImpl getJAXBContext()
meth public com.sun.xml.bind.v2.runtime.unmarshaller.Loader selectRootLoader(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx getLocator()
meth public com.sun.xml.bind.v2.runtime.unmarshaller.Scope getScope(int)
meth public com.sun.xml.bind.v2.runtime.unmarshaller.StructureLoader getStructureLoader()
meth public com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext getContext()
meth public com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State getCurrentState()
meth public com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor$TextPredictor getPredictor()
 anno 0 java.lang.Deprecated()
meth public final void endElement(com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public java.lang.Object createInstance(com.sun.xml.bind.v2.runtime.JaxBeanInfo) throws org.xml.sax.SAXException
meth public java.lang.Object createInstance(java.lang.Class<?>) throws org.xml.sax.SAXException
meth public java.lang.Object getInnerPeer()
meth public java.lang.Object getOuterPeer()
meth public java.lang.Object getResult() throws javax.xml.bind.UnmarshalException
meth public java.lang.String addToIdTable(java.lang.String) throws org.xml.sax.SAXException
meth public java.lang.String getNamespaceURI(java.lang.String)
meth public java.lang.String getPrefix(java.lang.String)
meth public java.lang.String getXMIMEContentType()
meth public java.lang.String[] getAllDeclaredPrefixes()
meth public java.lang.String[] getNewlyDeclaredPrefixes()
meth public java.util.Collection<javax.xml.namespace.QName> getCurrentExpectedAttributes()
meth public java.util.Collection<javax.xml.namespace.QName> getCurrentExpectedElements()
meth public java.util.Iterator<java.lang.String> getPrefixes(java.lang.String)
meth public java.util.concurrent.Callable getObjectFromId(java.lang.String,java.lang.Class) throws org.xml.sax.SAXException
meth public static com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext getInstance()
meth public void addPatcher(com.sun.xml.bind.v2.runtime.unmarshaller.Patcher)
meth public void clearStates()
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String)
meth public void endScope(int) throws org.xml.sax.SAXException
meth public void errorUnresolvedIDREF(java.lang.Object,java.lang.String,com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx) throws org.xml.sax.SAXException
meth public void handleError(java.lang.Exception) throws org.xml.sax.SAXException
meth public void handleError(java.lang.Exception,boolean) throws org.xml.sax.SAXException
meth public void handleError(java.lang.String)
meth public void handleEvent(javax.xml.bind.ValidationEvent,boolean) throws org.xml.sax.SAXException
meth public void recordInnerPeer(java.lang.Object)
meth public void recordOuterPeer(java.lang.Object)
meth public void reset(com.sun.xml.bind.unmarshaller.InfosetScanner,boolean,com.sun.xml.bind.v2.runtime.JaxBeanInfo,com.sun.xml.bind.IDResolver)
meth public void setFactories(java.lang.Object)
meth public void startDocument(com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx,javax.xml.namespace.NamespaceContext) throws org.xml.sax.SAXException
meth public void startElement(com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String)
meth public void startScope(int)
meth public void text(java.lang.CharSequence) throws org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.Coordinator
hfds DEFAULT_ROOT_LOADER,DUMMY_INSTANCE,EXPECTED_TYPE_ROOT_LOADER,aborted,assoc,current,currentElement,environmentNamespaceContext,errorsCounter,expectedType,factories,idResolver,isInplaceMode,isUnmarshalInProgress,locator,logger,nsBind,nsLen,patchers,patchersLen,result,root,scanner,scopeTop,scopes
hcls DefaultRootLoader,ExpectedTypeRootLoader,Factory

CLSS public final com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State
 outer com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext
meth public boolean isMixed()
meth public boolean isNil()
meth public com.sun.xml.bind.v2.runtime.unmarshaller.Loader getLoader()
meth public com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext getContext()
meth public com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State getPrev()
meth public java.lang.Object getBackup()
meth public java.lang.Object getTarget()
meth public java.lang.String getElementDefaultValue()
meth public void setBackup(java.lang.Object)
meth public void setElementDefaultValue(java.lang.String)
meth public void setIntercepter(com.sun.xml.bind.v2.runtime.unmarshaller.Intercepter)
meth public void setLoader(com.sun.xml.bind.v2.runtime.unmarshaller.Loader)
meth public void setNil(boolean)
meth public void setReceiver(com.sun.xml.bind.v2.runtime.unmarshaller.Receiver)
meth public void setTarget(java.lang.Object)
supr java.lang.Object
hfds backup,elementDefaultValue,intercepter,loader,mixed,next,nil,numNsDecl,prev,receiver,target

CLSS public com.sun.xml.bind.v2.runtime.unmarshaller.ValuePropertyLoader
cons public init(com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor)
meth public void text(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,java.lang.CharSequence) throws org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.unmarshaller.Loader
hfds xacc

CLSS public final com.sun.xml.bind.v2.runtime.unmarshaller.WildcardLoader
cons public init(javax.xml.bind.annotation.DomHandler,com.sun.xml.bind.v2.model.core.WildcardMode)
meth protected com.sun.xml.bind.v2.runtime.unmarshaller.Loader selectLoader(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.unmarshaller.ProxyLoader
hfds dom,mode

CLSS public abstract interface com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor
innr public abstract interface static TextPredictor
meth public abstract com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext getContext()
meth public abstract com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor$TextPredictor getPredictor()
meth public abstract void endDocument() throws org.xml.sax.SAXException
meth public abstract void endElement(com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public abstract void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void startDocument(com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx,javax.xml.namespace.NamespaceContext) throws org.xml.sax.SAXException
meth public abstract void startElement(com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth public abstract void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void text(java.lang.CharSequence) throws org.xml.sax.SAXException

CLSS public abstract interface static com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor$TextPredictor
 outer com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor
meth public abstract boolean expectText()

CLSS public com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader
cons public init(com.sun.xml.bind.v2.runtime.unmarshaller.Loader)
innr public final static Array
innr public final static Single
meth protected com.sun.xml.bind.v2.runtime.unmarshaller.Loader selectLoader(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
meth protected void onNil(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State) throws org.xml.sax.SAXException
meth public java.util.Collection<javax.xml.namespace.QName> getExpectedAttributes()
meth public java.util.Collection<javax.xml.namespace.QName> getExpectedChildElements()
supr com.sun.xml.bind.v2.runtime.unmarshaller.ProxyLoader
hfds defaultLoader

CLSS public final static com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader$Array
 outer com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader
cons public init(com.sun.xml.bind.v2.runtime.unmarshaller.Loader)
meth protected void onNil(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State)
supr com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader

CLSS public final static com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader$Single
 outer com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader
cons public init(com.sun.xml.bind.v2.runtime.unmarshaller.Loader,com.sun.xml.bind.v2.runtime.reflect.Accessor)
meth protected void onNil(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State) throws org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader
hfds acc

CLSS public com.sun.xml.bind.v2.runtime.unmarshaller.XsiTypeLoader
cons public init(com.sun.xml.bind.v2.runtime.JaxBeanInfo)
meth public java.util.Collection<javax.xml.namespace.QName> getExpectedAttributes()
meth public void startElement(com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext$State,com.sun.xml.bind.v2.runtime.unmarshaller.TagName) throws org.xml.sax.SAXException
supr com.sun.xml.bind.v2.runtime.unmarshaller.Loader
hfds XsiTypeQNAME,defaultBeanInfo

CLSS public final com.sun.xml.bind.v2.schemagen.Util
meth public static boolean equal(java.lang.String,java.lang.String)
meth public static boolean equalsIgnoreCase(java.lang.String,java.lang.String)
meth public static java.lang.String escapeURI(java.lang.String)
meth public static java.lang.String getParentUriPath(java.lang.String)
meth public static java.lang.String normalizeUriPath(java.lang.String)
supr java.lang.Object

CLSS public final com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object>
cons public init(com.sun.xml.bind.v2.model.nav.Navigator<{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%0},{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%1},{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%2},{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%3}>,com.sun.xml.bind.v2.model.core.TypeInfoSet<{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%0},{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%1},{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%2},{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%3}>)
meth protected static java.lang.String relativize(java.lang.String,java.lang.String)
meth public java.lang.String toString()
meth public void add(com.sun.xml.bind.v2.model.core.ArrayInfo<{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%0},{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%1}>)
meth public void add(com.sun.xml.bind.v2.model.core.ClassInfo<{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%0},{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%1}>)
meth public void add(com.sun.xml.bind.v2.model.core.ElementInfo<{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%0},{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%1}>)
meth public void add(com.sun.xml.bind.v2.model.core.EnumLeafInfo<{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%0},{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%1}>)
meth public void add(javax.xml.namespace.QName,boolean,com.sun.xml.bind.v2.model.core.NonElement<{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%0},{com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator%1}>)
meth public void write(javax.xml.bind.SchemaOutputResolver,com.sun.xml.bind.api.ErrorListener) throws java.io.IOException
meth public void writeEpisodeFile(com.sun.xml.txw2.output.XmlSerializer)
supr java.lang.Object
hfds NAMESPACE_COMPARATOR,anyType,collisionChecker,errorListener,logger,namespaces,navigator,newline,stringType,types
hcls Namespace

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.episode.Bindings
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.episode.Bindings bindings()
meth public abstract com.sun.xml.bind.v2.schemagen.episode.Klass klass()
meth public abstract com.sun.xml.bind.v2.schemagen.episode.Klass typesafeEnumClass()
meth public abstract com.sun.xml.bind.v2.schemagen.episode.SchemaBindings schemaBindings()
meth public abstract void scd(java.lang.String)
meth public abstract void version(java.lang.String)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.episode.Klass
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract void ref(java.lang.String)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.episode.Package
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract void name(java.lang.String)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.episode.SchemaBindings
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.episode.Package _package()
meth public abstract void map(boolean)

CLSS abstract interface com.sun.xml.bind.v2.schemagen.episode.package-info

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Annotated id(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Annotation annotation()

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.Annotation
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Annotation id(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Appinfo appinfo()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Documentation documentation()

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.Any
intf com.sun.xml.bind.v2.schemagen.xmlschema.Occurs
intf com.sun.xml.bind.v2.schemagen.xmlschema.Wildcard
intf com.sun.xml.txw2.TypedXmlWriter

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.Appinfo
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Appinfo source(java.lang.String)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.AttrDecls
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.LocalAttribute attribute()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Wildcard anyAttribute()

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.AttributeType
intf com.sun.xml.bind.v2.schemagen.xmlschema.SimpleTypeHost
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.AttributeType type(javax.xml.namespace.QName)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.ComplexContent
intf com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ComplexContent mixed(boolean)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ComplexExtension extension()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ComplexRestriction restriction()

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.ComplexExtension
intf com.sun.xml.bind.v2.schemagen.xmlschema.AttrDecls
intf com.sun.xml.bind.v2.schemagen.xmlschema.ExtensionType
intf com.sun.xml.bind.v2.schemagen.xmlschema.TypeDefParticle
intf com.sun.xml.txw2.TypedXmlWriter

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.ComplexRestriction
intf com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.bind.v2.schemagen.xmlschema.AttrDecls
intf com.sun.xml.bind.v2.schemagen.xmlschema.TypeDefParticle
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ComplexRestriction base(javax.xml.namespace.QName)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.ComplexType
intf com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.bind.v2.schemagen.xmlschema.ComplexTypeModel
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ComplexType _abstract(boolean)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ComplexType _final(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ComplexType _final(java.lang.String[])
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ComplexType block(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ComplexType block(java.lang.String[])
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ComplexType name(java.lang.String)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.ComplexTypeHost
intf com.sun.xml.bind.v2.schemagen.xmlschema.TypeHost
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ComplexType complexType()

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.ComplexTypeModel
intf com.sun.xml.bind.v2.schemagen.xmlschema.AttrDecls
intf com.sun.xml.bind.v2.schemagen.xmlschema.TypeDefParticle
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ComplexContent complexContent()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ComplexTypeModel mixed(boolean)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.SimpleContent simpleContent()

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.ContentModelContainer
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Any any()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ExplicitGroup all()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ExplicitGroup choice()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ExplicitGroup sequence()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.LocalElement element()

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.Documentation
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Documentation lang(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Documentation source(java.lang.String)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.Element
intf com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.bind.v2.schemagen.xmlschema.ComplexTypeHost
intf com.sun.xml.bind.v2.schemagen.xmlschema.FixedOrDefault
intf com.sun.xml.bind.v2.schemagen.xmlschema.SimpleTypeHost
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Element block(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Element block(java.lang.String[])
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Element nillable(boolean)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Element type(javax.xml.namespace.QName)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.ExplicitGroup
intf com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.bind.v2.schemagen.xmlschema.NestedParticle
intf com.sun.xml.bind.v2.schemagen.xmlschema.Occurs
intf com.sun.xml.txw2.TypedXmlWriter

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.ExtensionType
intf com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ExtensionType base(javax.xml.namespace.QName)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.FixedOrDefault
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.FixedOrDefault _default(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.FixedOrDefault fixed(java.lang.String)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.Import
intf com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Import namespace(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Import schemaLocation(java.lang.String)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.List
intf com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.bind.v2.schemagen.xmlschema.SimpleTypeHost
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.List itemType(javax.xml.namespace.QName)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.LocalAttribute
intf com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.bind.v2.schemagen.xmlschema.AttributeType
intf com.sun.xml.bind.v2.schemagen.xmlschema.FixedOrDefault
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.LocalAttribute form(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.LocalAttribute name(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.LocalAttribute ref(javax.xml.namespace.QName)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.LocalAttribute use(java.lang.String)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.LocalElement
intf com.sun.xml.bind.v2.schemagen.xmlschema.Element
intf com.sun.xml.bind.v2.schemagen.xmlschema.Occurs
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.LocalElement form(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.LocalElement name(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.LocalElement ref(javax.xml.namespace.QName)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.NestedParticle
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Any any()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ExplicitGroup choice()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ExplicitGroup sequence()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.LocalElement element()

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.NoFixedFacet
intf com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.NoFixedFacet value(java.lang.String)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.Occurs
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Occurs maxOccurs(int)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Occurs maxOccurs(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Occurs minOccurs(int)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.Particle
intf com.sun.xml.bind.v2.schemagen.xmlschema.ContentModelContainer
intf com.sun.xml.bind.v2.schemagen.xmlschema.Occurs

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.Redefinable
intf com.sun.xml.bind.v2.schemagen.xmlschema.ComplexTypeHost
intf com.sun.xml.bind.v2.schemagen.xmlschema.SimpleTypeHost
intf com.sun.xml.txw2.TypedXmlWriter

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.Schema
intf com.sun.xml.bind.v2.schemagen.xmlschema.SchemaTop
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Annotation annotation()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Import _import()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Schema attributeFormDefault(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Schema blockDefault(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Schema blockDefault(java.lang.String[])
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Schema elementFormDefault(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Schema finalDefault(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Schema finalDefault(java.lang.String[])
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Schema id(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Schema lang(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Schema targetNamespace(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Schema version(java.lang.String)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.SchemaTop
intf com.sun.xml.bind.v2.schemagen.xmlschema.Redefinable
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelAttribute attribute()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelElement element()

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.SimpleContent
intf com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.SimpleExtension extension()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.SimpleRestriction restriction()

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.SimpleDerivation
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.List list()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.SimpleRestriction restriction()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Union union()

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.SimpleExtension
intf com.sun.xml.bind.v2.schemagen.xmlschema.AttrDecls
intf com.sun.xml.bind.v2.schemagen.xmlschema.ExtensionType
intf com.sun.xml.txw2.TypedXmlWriter

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.SimpleRestriction
intf com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.bind.v2.schemagen.xmlschema.AttrDecls
intf com.sun.xml.bind.v2.schemagen.xmlschema.SimpleRestrictionModel
intf com.sun.xml.txw2.TypedXmlWriter

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.SimpleRestrictionModel
intf com.sun.xml.bind.v2.schemagen.xmlschema.SimpleTypeHost
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.NoFixedFacet enumeration()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.SimpleRestrictionModel base(javax.xml.namespace.QName)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.SimpleType
intf com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.bind.v2.schemagen.xmlschema.SimpleDerivation
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.SimpleType _final(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.SimpleType _final(java.lang.String[])
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.SimpleType name(java.lang.String)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.SimpleTypeHost
intf com.sun.xml.bind.v2.schemagen.xmlschema.TypeHost
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.SimpleType simpleType()

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelAttribute
intf com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.bind.v2.schemagen.xmlschema.AttributeType
intf com.sun.xml.bind.v2.schemagen.xmlschema.FixedOrDefault
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelAttribute name(java.lang.String)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelElement
intf com.sun.xml.bind.v2.schemagen.xmlschema.Element
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelElement _abstract(boolean)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelElement _final(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelElement _final(java.lang.String[])
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelElement name(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelElement substitutionGroup(javax.xml.namespace.QName)

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.TypeDefParticle
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ExplicitGroup all()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ExplicitGroup choice()
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.ExplicitGroup sequence()

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.TypeHost
intf com.sun.xml.txw2.TypedXmlWriter

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.Union
intf com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.bind.v2.schemagen.xmlschema.SimpleTypeHost
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Union memberTypes(javax.xml.namespace.QName[])

CLSS public abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.Wildcard
intf com.sun.xml.bind.v2.schemagen.xmlschema.Annotated
intf com.sun.xml.txw2.TypedXmlWriter
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Wildcard namespace(java.lang.String)
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Wildcard namespace(java.lang.String[])
meth public abstract com.sun.xml.bind.v2.schemagen.xmlschema.Wildcard processContents(java.lang.String)

CLSS abstract interface com.sun.xml.bind.v2.schemagen.xmlschema.package-info

CLSS public final com.sun.xml.bind.v2.util.ByteArrayOutputStreamEx
cons public init()
cons public init(int)
meth public byte[] getBuffer()
meth public void readFrom(java.io.InputStream) throws java.io.IOException
meth public void set(com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data,java.lang.String)
supr java.io.ByteArrayOutputStream

CLSS public final com.sun.xml.bind.v2.util.CollisionCheckStack<%0 extends java.lang.Object>
cons public init()
meth public boolean findDuplicate({com.sun.xml.bind.v2.util.CollisionCheckStack%0})
meth public boolean getLatestPushResult()
meth public boolean getUseIdentity()
meth public boolean push({com.sun.xml.bind.v2.util.CollisionCheckStack%0})
meth public int size()
meth public java.lang.String getCycleString()
meth public void pushNocheck({com.sun.xml.bind.v2.util.CollisionCheckStack%0})
meth public void reset()
meth public void setUseIdentity(boolean)
meth public {com.sun.xml.bind.v2.util.CollisionCheckStack%0} get(int)
meth public {com.sun.xml.bind.v2.util.CollisionCheckStack%0} peek()
meth public {com.sun.xml.bind.v2.util.CollisionCheckStack%0} pop()
supr java.util.AbstractList<{com.sun.xml.bind.v2.util.CollisionCheckStack%0}>
hfds data,initialHash,latestPushResult,next,size,useIdentity

CLSS public final com.sun.xml.bind.v2.util.DataSourceSource
cons public init(javax.activation.DataHandler) throws javax.activation.MimeTypeParseException
cons public init(javax.activation.DataSource) throws javax.activation.MimeTypeParseException
meth public java.io.InputStream getInputStream()
meth public java.io.Reader getReader()
meth public javax.activation.DataSource getDataSource()
meth public void setInputStream(java.io.InputStream)
meth public void setReader(java.io.Reader)
supr javax.xml.transform.stream.StreamSource
hfds charset,is,r,source

CLSS public com.sun.xml.bind.v2.util.EditDistance
meth public static int editDistance(java.lang.String,java.lang.String)
meth public static java.lang.String findNearest(java.lang.String,java.lang.String[])
meth public static java.lang.String findNearest(java.lang.String,java.util.Collection<java.lang.String>)
supr java.lang.Object
hfds CACHE,a,b,back,cost

CLSS public com.sun.xml.bind.v2.util.FatalAdapter
cons public init(org.xml.sax.ErrorHandler)
intf org.xml.sax.ErrorHandler
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr java.lang.Object
hfds core

CLSS public final com.sun.xml.bind.v2.util.FlattenIterator<%0 extends java.lang.Object>
cons public init(java.lang.Iterable<? extends java.util.Map<?,? extends {com.sun.xml.bind.v2.util.FlattenIterator%0}>>)
intf java.util.Iterator<{com.sun.xml.bind.v2.util.FlattenIterator%0}>
meth public boolean hasNext()
meth public void remove()
meth public {com.sun.xml.bind.v2.util.FlattenIterator%0} next()
supr java.lang.Object
hfds child,next,parent

CLSS public final com.sun.xml.bind.v2.util.QNameMap<%0 extends java.lang.Object>
cons public init()
innr public final static Entry
meth public boolean containsKey(java.lang.String,java.lang.String)
meth public boolean isEmpty()
meth public com.sun.xml.bind.v2.util.QNameMap$Entry<{com.sun.xml.bind.v2.util.QNameMap%0}> getOne()
meth public com.sun.xml.bind.v2.util.QNameMap<{com.sun.xml.bind.v2.util.QNameMap%0}> putAll(com.sun.xml.bind.v2.util.QNameMap<? extends {com.sun.xml.bind.v2.util.QNameMap%0}>)
meth public int size()
meth public java.lang.String toString()
meth public java.util.Collection<javax.xml.namespace.QName> keySet()
meth public java.util.Set<com.sun.xml.bind.v2.util.QNameMap$Entry<{com.sun.xml.bind.v2.util.QNameMap%0}>> entrySet()
meth public void put(com.sun.xml.bind.v2.runtime.Name,{com.sun.xml.bind.v2.util.QNameMap%0})
meth public void put(java.lang.String,java.lang.String,{com.sun.xml.bind.v2.util.QNameMap%0})
meth public void put(javax.xml.namespace.QName,{com.sun.xml.bind.v2.util.QNameMap%0})
meth public {com.sun.xml.bind.v2.util.QNameMap%0} get(java.lang.String,java.lang.String)
meth public {com.sun.xml.bind.v2.util.QNameMap%0} get(javax.xml.namespace.QName)
supr java.lang.Object
hfds DEFAULT_INITIAL_CAPACITY,DEFAULT_LOAD_FACTOR,MAXIMUM_CAPACITY,entrySet,size,table,threshold
hcls EntryIterator,EntrySet,HashIterator

CLSS public final static com.sun.xml.bind.v2.util.QNameMap$Entry<%0 extends java.lang.Object>
 outer com.sun.xml.bind.v2.util.QNameMap
fld public final java.lang.String localName
fld public final java.lang.String nsUri
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public javax.xml.namespace.QName createQName()
meth public {com.sun.xml.bind.v2.util.QNameMap$Entry%0} getValue()
meth public {com.sun.xml.bind.v2.util.QNameMap$Entry%0} setValue({com.sun.xml.bind.v2.util.QNameMap$Entry%0})
supr java.lang.Object
hfds hash,next,value

CLSS public com.sun.xml.bind.v2.util.StackRecorder
cons public init()
supr java.lang.Throwable

CLSS public com.sun.xml.bind.v2.util.TypeCast
cons public init()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> checkedCast(java.util.Map<?,?>,java.lang.Class<{%%0}>,java.lang.Class<{%%1}>)
supr java.lang.Object

CLSS public com.sun.xml.bind.v2.util.XmlFactory
cons public init()
fld public final static java.lang.String ACCESS_EXTERNAL_DTD = "http://javax.xml.XMLConstants/property/accessExternalDTD"
fld public final static java.lang.String ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema"
meth public static javax.xml.parsers.DocumentBuilderFactory createDocumentBuilderFactory(boolean)
meth public static javax.xml.parsers.SAXParserFactory createParserFactory(boolean)
meth public static javax.xml.transform.TransformerFactory createTransformerFactory(boolean)
meth public static javax.xml.validation.SchemaFactory allowExternalAccess(javax.xml.validation.SchemaFactory,java.lang.String,boolean)
meth public static javax.xml.validation.SchemaFactory allowExternalDTDAccess(javax.xml.validation.SchemaFactory,java.lang.String,boolean)
meth public static javax.xml.validation.SchemaFactory createSchemaFactory(java.lang.String,boolean)
meth public static javax.xml.xpath.XPathFactory createXPathFactory(boolean)
supr java.lang.Object
hfds DISABLE_XML_SECURITY,LOGGER,XML_SECURITY_DISABLED

CLSS public abstract interface com.sun.xml.dtdparser.DTDEventListener
fld public final static short CHOICE = 0
fld public final static short CONTENT_MODEL_ANY = 1
fld public final static short CONTENT_MODEL_CHILDREN = 3
fld public final static short CONTENT_MODEL_EMPTY = 0
fld public final static short CONTENT_MODEL_MIXED = 2
fld public final static short OCCURENCE_ONCE = 3
fld public final static short OCCURENCE_ONE_OR_MORE = 1
fld public final static short OCCURENCE_ZERO_OR_MORE = 0
fld public final static short OCCURENCE_ZERO_OR_ONE = 2
fld public final static short SEQUENCE = 1
fld public final static short USE_FIXED = 2
fld public final static short USE_IMPLIED = 1
fld public final static short USE_NORMAL = 0
fld public final static short USE_REQUIRED = 3
intf java.util.EventListener
meth public abstract void attributeDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String[],short,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void characters(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void childElement(java.lang.String,short) throws org.xml.sax.SAXException
meth public abstract void comment(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void connector(short) throws org.xml.sax.SAXException
meth public abstract void endCDATA() throws org.xml.sax.SAXException
meth public abstract void endContentModel(java.lang.String,short) throws org.xml.sax.SAXException
meth public abstract void endDTD() throws org.xml.sax.SAXException
meth public abstract void endModelGroup(short) throws org.xml.sax.SAXException
meth public abstract void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void externalGeneralEntityDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void externalParameterEntityDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void internalGeneralEntityDecl(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void internalParameterEntityDecl(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void mixedElement(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void setDocumentLocator(org.xml.sax.Locator)
meth public abstract void startCDATA() throws org.xml.sax.SAXException
meth public abstract void startContentModel(java.lang.String,short) throws org.xml.sax.SAXException
meth public abstract void startDTD(com.sun.xml.dtdparser.InputEntity) throws org.xml.sax.SAXException
meth public abstract void startModelGroup() throws org.xml.sax.SAXException
meth public abstract void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException

CLSS public com.sun.xml.dtdparser.DTDHandlerBase
cons public init()
intf com.sun.xml.dtdparser.DTDEventListener
meth public void attributeDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String[],short,java.lang.String) throws org.xml.sax.SAXException
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void childElement(java.lang.String,short) throws org.xml.sax.SAXException
meth public void comment(java.lang.String) throws org.xml.sax.SAXException
meth public void connector(short) throws org.xml.sax.SAXException
meth public void endCDATA() throws org.xml.sax.SAXException
meth public void endContentModel(java.lang.String,short) throws org.xml.sax.SAXException
meth public void endDTD() throws org.xml.sax.SAXException
meth public void endModelGroup(short) throws org.xml.sax.SAXException
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void externalGeneralEntityDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void externalParameterEntityDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void internalGeneralEntityDecl(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void internalParameterEntityDecl(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void mixedElement(java.lang.String) throws org.xml.sax.SAXException
meth public void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void startCDATA() throws org.xml.sax.SAXException
meth public void startContentModel(java.lang.String,short) throws org.xml.sax.SAXException
meth public void startDTD(com.sun.xml.dtdparser.InputEntity) throws org.xml.sax.SAXException
meth public void startModelGroup() throws org.xml.sax.SAXException
meth public void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr java.lang.Object

CLSS public abstract interface com.sun.xml.txw2.TypedXmlWriter
meth public abstract <%0 extends com.sun.xml.txw2.TypedXmlWriter> {%%0} _cast(java.lang.Class<{%%0}>)
meth public abstract <%0 extends com.sun.xml.txw2.TypedXmlWriter> {%%0} _element(java.lang.Class<{%%0}>)
meth public abstract <%0 extends com.sun.xml.txw2.TypedXmlWriter> {%%0} _element(java.lang.String,java.lang.Class<{%%0}>)
meth public abstract <%0 extends com.sun.xml.txw2.TypedXmlWriter> {%%0} _element(java.lang.String,java.lang.String,java.lang.Class<{%%0}>)
meth public abstract <%0 extends com.sun.xml.txw2.TypedXmlWriter> {%%0} _element(javax.xml.namespace.QName,java.lang.Class<{%%0}>)
meth public abstract com.sun.xml.txw2.Document getDocument()
meth public abstract void _attribute(java.lang.String,java.lang.Object)
meth public abstract void _attribute(java.lang.String,java.lang.String,java.lang.Object)
meth public abstract void _attribute(javax.xml.namespace.QName,java.lang.Object)
meth public abstract void _cdata(java.lang.Object)
meth public abstract void _comment(java.lang.Object)
meth public abstract void _namespace(java.lang.String)
meth public abstract void _namespace(java.lang.String,boolean)
meth public abstract void _namespace(java.lang.String,java.lang.String)
meth public abstract void _pcdata(java.lang.Object)
meth public abstract void block()
meth public abstract void commit()
meth public abstract void commit(boolean)

CLSS public abstract interface !annotation com.sun.xml.txw2.annotation.XmlElement
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String ns()
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface !annotation com.sun.xml.txw2.annotation.XmlNamespace
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS public abstract interface com.sun.xml.xsom.parser.AnnotationParserFactory
meth public abstract com.sun.xml.xsom.parser.AnnotationParser create()

CLSS public abstract interface com.sun.xml.xsom.visitor.XSContentTypeVisitor
meth public abstract void empty(com.sun.xml.xsom.XSContentType)
meth public abstract void particle(com.sun.xml.xsom.XSParticle)
meth public abstract void simpleType(com.sun.xml.xsom.XSSimpleType)

CLSS public abstract interface com.sun.xml.xsom.visitor.XSTermFunction<%0 extends java.lang.Object>
meth public abstract {com.sun.xml.xsom.visitor.XSTermFunction%0} elementDecl(com.sun.xml.xsom.XSElementDecl)
meth public abstract {com.sun.xml.xsom.visitor.XSTermFunction%0} modelGroup(com.sun.xml.xsom.XSModelGroup)
meth public abstract {com.sun.xml.xsom.visitor.XSTermFunction%0} modelGroupDecl(com.sun.xml.xsom.XSModelGroupDecl)
meth public abstract {com.sun.xml.xsom.visitor.XSTermFunction%0} wildcard(com.sun.xml.xsom.XSWildcard)

CLSS public abstract interface com.sun.xml.xsom.visitor.XSTermVisitor
meth public abstract void elementDecl(com.sun.xml.xsom.XSElementDecl)
meth public abstract void modelGroup(com.sun.xml.xsom.XSModelGroup)
meth public abstract void modelGroupDecl(com.sun.xml.xsom.XSModelGroupDecl)
meth public abstract void wildcard(com.sun.xml.xsom.XSWildcard)

CLSS public abstract interface com.sun.xml.xsom.visitor.XSVisitor
intf com.sun.xml.xsom.visitor.XSContentTypeVisitor
intf com.sun.xml.xsom.visitor.XSTermVisitor
meth public abstract void annotation(com.sun.xml.xsom.XSAnnotation)
meth public abstract void attGroupDecl(com.sun.xml.xsom.XSAttGroupDecl)
meth public abstract void attributeDecl(com.sun.xml.xsom.XSAttributeDecl)
meth public abstract void attributeUse(com.sun.xml.xsom.XSAttributeUse)
meth public abstract void complexType(com.sun.xml.xsom.XSComplexType)
meth public abstract void facet(com.sun.xml.xsom.XSFacet)
meth public abstract void identityConstraint(com.sun.xml.xsom.XSIdentityConstraint)
meth public abstract void notation(com.sun.xml.xsom.XSNotation)
meth public abstract void schema(com.sun.xml.xsom.XSSchema)
meth public abstract void xpath(com.sun.xml.xsom.XSXPath)

CLSS public abstract interface com.sun.xml.xsom.visitor.XSWildcardFunction<%0 extends java.lang.Object>
meth public abstract {com.sun.xml.xsom.visitor.XSWildcardFunction%0} any(com.sun.xml.xsom.XSWildcard$Any)
meth public abstract {com.sun.xml.xsom.visitor.XSWildcardFunction%0} other(com.sun.xml.xsom.XSWildcard$Other)
meth public abstract {com.sun.xml.xsom.visitor.XSWildcardFunction%0} union(com.sun.xml.xsom.XSWildcard$Union)

CLSS public java.io.ByteArrayOutputStream
cons public init()
cons public init(int)
fld protected byte[] buf
fld protected int count
meth public byte[] toByteArray()
meth public int size()
meth public java.lang.String toString()
meth public java.lang.String toString(int)
 anno 0 java.lang.Deprecated()
meth public java.lang.String toString(java.lang.String) throws java.io.UnsupportedEncodingException
meth public void close() throws java.io.IOException
meth public void reset()
meth public void write(byte[],int,int)
meth public void write(int)
meth public void writeTo(java.io.OutputStream) throws java.io.IOException
supr java.io.OutputStream

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

CLSS public abstract java.io.OutputStream
cons public init()
intf java.io.Closeable
intf java.io.Flushable
meth public abstract void write(int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface java.lang.CharSequence
meth public abstract char charAt(int)
meth public abstract int length()
meth public abstract java.lang.CharSequence subSequence(int,int)
meth public abstract java.lang.String toString()
meth public java.util.stream.IntStream chars()
meth public java.util.stream.IntStream codePoints()

CLSS public abstract java.lang.ClassLoader
cons protected init()
cons protected init(java.lang.ClassLoader)
meth protected final java.lang.Class<?> defineClass(byte[],int,int)
 anno 0 java.lang.Deprecated()
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int)
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int,java.security.ProtectionDomain)
meth protected final java.lang.Class<?> defineClass(java.lang.String,java.nio.ByteBuffer,java.security.ProtectionDomain)
meth protected final java.lang.Class<?> findLoadedClass(java.lang.String)
meth protected final java.lang.Class<?> findSystemClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected final void resolveClass(java.lang.Class<?>)
meth protected final void setSigners(java.lang.Class<?>,java.lang.Object[])
meth protected java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Class<?> loadClass(java.lang.String,boolean) throws java.lang.ClassNotFoundException
meth protected java.lang.Object getClassLoadingLock(java.lang.String)
meth protected java.lang.Package definePackage(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.net.URL)
meth protected java.lang.Package getPackage(java.lang.String)
meth protected java.lang.Package[] getPackages()
meth protected java.lang.String findLibrary(java.lang.String)
meth protected java.net.URL findResource(java.lang.String)
meth protected java.util.Enumeration<java.net.URL> findResources(java.lang.String) throws java.io.IOException
meth protected static boolean registerAsParallelCapable()
meth public final java.lang.ClassLoader getParent()
meth public java.io.InputStream getResourceAsStream(java.lang.String)
meth public java.lang.Class<?> loadClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.net.URL getResource(java.lang.String)
meth public java.util.Enumeration<java.net.URL> getResources(java.lang.String) throws java.io.IOException
meth public static java.io.InputStream getSystemResourceAsStream(java.lang.String)
meth public static java.lang.ClassLoader getSystemClassLoader()
meth public static java.net.URL getSystemResource(java.lang.String)
meth public static java.util.Enumeration<java.net.URL> getSystemResources(java.lang.String) throws java.io.IOException
meth public void clearAssertionStatus()
meth public void setClassAssertionStatus(java.lang.String,boolean)
meth public void setDefaultAssertionStatus(boolean)
meth public void setPackageAssertionStatus(java.lang.String,boolean)
supr java.lang.Object

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected init(java.lang.String,int)
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} valueOf(java.lang.Class<{%%0}>,java.lang.String)
supr java.lang.Object

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

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

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public java.lang.Throwable
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
intf java.io.Serializable
meth public final java.lang.Throwable[] getSuppressed()
meth public final void addSuppressed(java.lang.Throwable)
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object

CLSS public abstract interface java.lang.annotation.Annotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation java.lang.annotation.Documented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Retention
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.RetentionPolicy value()

CLSS public abstract interface !annotation java.lang.annotation.Target
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.ElementType[] value()

CLSS public abstract interface java.lang.reflect.InvocationHandler
meth public abstract java.lang.Object invoke(java.lang.Object,java.lang.reflect.Method,java.lang.Object[]) throws java.lang.Throwable

CLSS public abstract java.net.Authenticator
cons public init()
innr public final static !enum RequestorType
meth protected final int getRequestingPort()
meth protected final java.lang.String getRequestingHost()
meth protected final java.lang.String getRequestingPrompt()
meth protected final java.lang.String getRequestingProtocol()
meth protected final java.lang.String getRequestingScheme()
meth protected final java.net.InetAddress getRequestingSite()
meth protected java.net.Authenticator$RequestorType getRequestorType()
meth protected java.net.PasswordAuthentication getPasswordAuthentication()
meth protected java.net.URL getRequestingURL()
meth public static java.net.PasswordAuthentication requestPasswordAuthentication(java.lang.String,java.net.InetAddress,int,java.lang.String,java.lang.String,java.lang.String)
meth public static java.net.PasswordAuthentication requestPasswordAuthentication(java.lang.String,java.net.InetAddress,int,java.lang.String,java.lang.String,java.lang.String,java.net.URL,java.net.Authenticator$RequestorType)
meth public static java.net.PasswordAuthentication requestPasswordAuthentication(java.net.InetAddress,int,java.lang.String,java.lang.String,java.lang.String)
meth public static void setDefault(java.net.Authenticator)
supr java.lang.Object

CLSS public abstract java.util.AbstractCollection<%0 extends java.lang.Object>
cons protected init()
intf java.util.Collection<{java.util.AbstractCollection%0}>
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract int size()
meth public abstract java.util.Iterator<{java.util.AbstractCollection%0}> iterator()
meth public boolean add({java.util.AbstractCollection%0})
meth public boolean addAll(java.util.Collection<? extends {java.util.AbstractCollection%0}>)
meth public boolean contains(java.lang.Object)
meth public boolean containsAll(java.util.Collection<?>)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean retainAll(java.util.Collection<?>)
meth public java.lang.Object[] toArray()
meth public java.lang.String toString()
meth public void clear()
supr java.lang.Object

CLSS public abstract java.util.AbstractList<%0 extends java.lang.Object>
cons protected init()
fld protected int modCount
intf java.util.List<{java.util.AbstractList%0}>
meth protected void removeRange(int,int)
meth public abstract {java.util.AbstractList%0} get(int)
meth public boolean add({java.util.AbstractList%0})
meth public boolean addAll(int,java.util.Collection<? extends {java.util.AbstractList%0}>)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public int indexOf(java.lang.Object)
meth public int lastIndexOf(java.lang.Object)
meth public java.util.Iterator<{java.util.AbstractList%0}> iterator()
meth public java.util.List<{java.util.AbstractList%0}> subList(int,int)
meth public java.util.ListIterator<{java.util.AbstractList%0}> listIterator()
meth public java.util.ListIterator<{java.util.AbstractList%0}> listIterator(int)
meth public void add(int,{java.util.AbstractList%0})
meth public void clear()
meth public {java.util.AbstractList%0} remove(int)
meth public {java.util.AbstractList%0} set(int,{java.util.AbstractList%0})
supr java.util.AbstractCollection<{java.util.AbstractList%0}>

CLSS public java.util.ArrayList<%0 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(java.util.Collection<? extends {java.util.ArrayList%0}>)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.List<{java.util.ArrayList%0}>
intf java.util.RandomAccess
meth protected void removeRange(int,int)
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public boolean add({java.util.ArrayList%0})
meth public boolean addAll(int,java.util.Collection<? extends {java.util.ArrayList%0}>)
meth public boolean addAll(java.util.Collection<? extends {java.util.ArrayList%0}>)
meth public boolean contains(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean removeIf(java.util.function.Predicate<? super {java.util.ArrayList%0}>)
meth public boolean retainAll(java.util.Collection<?>)
meth public int indexOf(java.lang.Object)
meth public int lastIndexOf(java.lang.Object)
meth public int size()
meth public java.lang.Object clone()
meth public java.lang.Object[] toArray()
meth public java.util.Iterator<{java.util.ArrayList%0}> iterator()
meth public java.util.List<{java.util.ArrayList%0}> subList(int,int)
meth public java.util.ListIterator<{java.util.ArrayList%0}> listIterator()
meth public java.util.ListIterator<{java.util.ArrayList%0}> listIterator(int)
meth public java.util.Spliterator<{java.util.ArrayList%0}> spliterator()
meth public void add(int,{java.util.ArrayList%0})
meth public void clear()
meth public void ensureCapacity(int)
meth public void forEach(java.util.function.Consumer<? super {java.util.ArrayList%0}>)
meth public void replaceAll(java.util.function.UnaryOperator<{java.util.ArrayList%0}>)
meth public void sort(java.util.Comparator<? super {java.util.ArrayList%0}>)
meth public void trimToSize()
meth public {java.util.ArrayList%0} get(int)
meth public {java.util.ArrayList%0} remove(int)
meth public {java.util.ArrayList%0} set(int,{java.util.ArrayList%0})
supr java.util.AbstractList<{java.util.ArrayList%0}>

CLSS public abstract interface java.util.Collection<%0 extends java.lang.Object>
intf java.lang.Iterable<{java.util.Collection%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Collection%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Collection%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Collection%0}> iterator()
meth public abstract void clear()
meth public boolean removeIf(java.util.function.Predicate<? super {java.util.Collection%0}>)
meth public java.util.Spliterator<{java.util.Collection%0}> spliterator()
meth public java.util.stream.Stream<{java.util.Collection%0}> parallelStream()
meth public java.util.stream.Stream<{java.util.Collection%0}> stream()

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface java.util.Iterator<%0 extends java.lang.Object>
meth public abstract boolean hasNext()
meth public abstract {java.util.Iterator%0} next()
meth public void forEachRemaining(java.util.function.Consumer<? super {java.util.Iterator%0}>)
meth public void remove()

CLSS public abstract interface java.util.List<%0 extends java.lang.Object>
intf java.util.Collection<{java.util.List%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.List%0})
meth public abstract boolean addAll(int,java.util.Collection<? extends {java.util.List%0}>)
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.List%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int indexOf(java.lang.Object)
meth public abstract int lastIndexOf(java.lang.Object)
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.List%0}> iterator()
meth public abstract java.util.List<{java.util.List%0}> subList(int,int)
meth public abstract java.util.ListIterator<{java.util.List%0}> listIterator()
meth public abstract java.util.ListIterator<{java.util.List%0}> listIterator(int)
meth public abstract void add(int,{java.util.List%0})
meth public abstract void clear()
meth public abstract {java.util.List%0} get(int)
meth public abstract {java.util.List%0} remove(int)
meth public abstract {java.util.List%0} set(int,{java.util.List%0})
meth public java.util.Spliterator<{java.util.List%0}> spliterator()
meth public void replaceAll(java.util.function.UnaryOperator<{java.util.List%0}>)
meth public void sort(java.util.Comparator<? super {java.util.List%0}>)

CLSS public abstract interface java.util.RandomAccess

CLSS public abstract interface javax.activation.DataSource
meth public abstract java.io.InputStream getInputStream() throws java.io.IOException
meth public abstract java.io.OutputStream getOutputStream() throws java.io.IOException
meth public abstract java.lang.String getContentType()
meth public abstract java.lang.String getName()

CLSS public abstract javax.xml.bind.Binder<%0 extends java.lang.Object>
cons public init()
meth public abstract <%0 extends java.lang.Object> javax.xml.bind.JAXBElement<{%%0}> unmarshal({javax.xml.bind.Binder%0},java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth public abstract java.lang.Object getJAXBNode({javax.xml.bind.Binder%0})
meth public abstract java.lang.Object getProperty(java.lang.String) throws javax.xml.bind.PropertyException
meth public abstract java.lang.Object unmarshal({javax.xml.bind.Binder%0}) throws javax.xml.bind.JAXBException
meth public abstract java.lang.Object updateJAXB({javax.xml.bind.Binder%0}) throws javax.xml.bind.JAXBException
meth public abstract javax.xml.bind.ValidationEventHandler getEventHandler() throws javax.xml.bind.JAXBException
meth public abstract javax.xml.validation.Schema getSchema()
meth public abstract void marshal(java.lang.Object,{javax.xml.bind.Binder%0}) throws javax.xml.bind.JAXBException
meth public abstract void setEventHandler(javax.xml.bind.ValidationEventHandler) throws javax.xml.bind.JAXBException
meth public abstract void setProperty(java.lang.String,java.lang.Object) throws javax.xml.bind.PropertyException
meth public abstract void setSchema(javax.xml.validation.Schema)
meth public abstract {javax.xml.bind.Binder%0} getXMLNode(java.lang.Object)
meth public abstract {javax.xml.bind.Binder%0} updateXML(java.lang.Object) throws javax.xml.bind.JAXBException
meth public abstract {javax.xml.bind.Binder%0} updateXML(java.lang.Object,{javax.xml.bind.Binder%0}) throws javax.xml.bind.JAXBException
supr java.lang.Object

CLSS public abstract interface javax.xml.bind.DatatypeConverterInterface
meth public abstract boolean parseBoolean(java.lang.String)
meth public abstract byte parseByte(java.lang.String)
meth public abstract byte[] parseBase64Binary(java.lang.String)
meth public abstract byte[] parseHexBinary(java.lang.String)
meth public abstract double parseDouble(java.lang.String)
meth public abstract float parseFloat(java.lang.String)
meth public abstract int parseInt(java.lang.String)
meth public abstract int parseUnsignedShort(java.lang.String)
meth public abstract java.lang.String parseAnySimpleType(java.lang.String)
meth public abstract java.lang.String parseString(java.lang.String)
meth public abstract java.lang.String printAnySimpleType(java.lang.String)
meth public abstract java.lang.String printBase64Binary(byte[])
meth public abstract java.lang.String printBoolean(boolean)
meth public abstract java.lang.String printByte(byte)
meth public abstract java.lang.String printDate(java.util.Calendar)
meth public abstract java.lang.String printDateTime(java.util.Calendar)
meth public abstract java.lang.String printDecimal(java.math.BigDecimal)
meth public abstract java.lang.String printDouble(double)
meth public abstract java.lang.String printFloat(float)
meth public abstract java.lang.String printHexBinary(byte[])
meth public abstract java.lang.String printInt(int)
meth public abstract java.lang.String printInteger(java.math.BigInteger)
meth public abstract java.lang.String printLong(long)
meth public abstract java.lang.String printQName(javax.xml.namespace.QName,javax.xml.namespace.NamespaceContext)
meth public abstract java.lang.String printShort(short)
meth public abstract java.lang.String printString(java.lang.String)
meth public abstract java.lang.String printTime(java.util.Calendar)
meth public abstract java.lang.String printUnsignedInt(long)
meth public abstract java.lang.String printUnsignedShort(int)
meth public abstract java.math.BigDecimal parseDecimal(java.lang.String)
meth public abstract java.math.BigInteger parseInteger(java.lang.String)
meth public abstract java.util.Calendar parseDate(java.lang.String)
meth public abstract java.util.Calendar parseDateTime(java.lang.String)
meth public abstract java.util.Calendar parseTime(java.lang.String)
meth public abstract javax.xml.namespace.QName parseQName(java.lang.String,javax.xml.namespace.NamespaceContext)
meth public abstract long parseLong(java.lang.String)
meth public abstract long parseUnsignedInt(java.lang.String)
meth public abstract short parseShort(java.lang.String)

CLSS public abstract javax.xml.bind.JAXBContext
cons protected init()
fld public final static java.lang.String JAXB_CONTEXT_FACTORY = "javax.xml.bind.JAXBContextFactory"
meth public !varargs static javax.xml.bind.JAXBContext newInstance(java.lang.Class<?>[]) throws javax.xml.bind.JAXBException
meth public <%0 extends java.lang.Object> javax.xml.bind.Binder<{%%0}> createBinder(java.lang.Class<{%%0}>)
meth public abstract javax.xml.bind.Marshaller createMarshaller() throws javax.xml.bind.JAXBException
meth public abstract javax.xml.bind.Unmarshaller createUnmarshaller() throws javax.xml.bind.JAXBException
meth public abstract javax.xml.bind.Validator createValidator() throws javax.xml.bind.JAXBException
 anno 0 java.lang.Deprecated()
meth public javax.xml.bind.Binder<org.w3c.dom.Node> createBinder()
meth public javax.xml.bind.JAXBIntrospector createJAXBIntrospector()
meth public static javax.xml.bind.JAXBContext newInstance(java.lang.Class<?>[],java.util.Map<java.lang.String,?>) throws javax.xml.bind.JAXBException
meth public static javax.xml.bind.JAXBContext newInstance(java.lang.String) throws javax.xml.bind.JAXBException
meth public static javax.xml.bind.JAXBContext newInstance(java.lang.String,java.lang.ClassLoader) throws javax.xml.bind.JAXBException
meth public static javax.xml.bind.JAXBContext newInstance(java.lang.String,java.lang.ClassLoader,java.util.Map<java.lang.String,?>) throws javax.xml.bind.JAXBException
meth public void generateSchema(javax.xml.bind.SchemaOutputResolver) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface javax.xml.bind.JAXBContextFactory
meth public abstract javax.xml.bind.JAXBContext createContext(java.lang.Class<?>[],java.util.Map<java.lang.String,?>) throws javax.xml.bind.JAXBException
meth public abstract javax.xml.bind.JAXBContext createContext(java.lang.String,java.lang.ClassLoader,java.util.Map<java.lang.String,?>) throws javax.xml.bind.JAXBException

CLSS public javax.xml.bind.JAXBException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public java.lang.String getErrorCode()
meth public java.lang.String toString()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable getLinkedException()
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setLinkedException(java.lang.Throwable)
supr java.lang.Exception
hfds errorCode,linkedException,serialVersionUID

CLSS public abstract interface javax.xml.bind.Marshaller
fld public final static java.lang.String JAXB_ENCODING = "jaxb.encoding"
fld public final static java.lang.String JAXB_FORMATTED_OUTPUT = "jaxb.formatted.output"
fld public final static java.lang.String JAXB_FRAGMENT = "jaxb.fragment"
fld public final static java.lang.String JAXB_NO_NAMESPACE_SCHEMA_LOCATION = "jaxb.noNamespaceSchemaLocation"
fld public final static java.lang.String JAXB_SCHEMA_LOCATION = "jaxb.schemaLocation"
innr public abstract static Listener
meth public abstract <%0 extends javax.xml.bind.annotation.adapters.XmlAdapter> void setAdapter(java.lang.Class<{%%0}>,{%%0})
meth public abstract <%0 extends javax.xml.bind.annotation.adapters.XmlAdapter> {%%0} getAdapter(java.lang.Class<{%%0}>)
meth public abstract java.lang.Object getProperty(java.lang.String) throws javax.xml.bind.PropertyException
meth public abstract javax.xml.bind.Marshaller$Listener getListener()
meth public abstract javax.xml.bind.ValidationEventHandler getEventHandler() throws javax.xml.bind.JAXBException
meth public abstract javax.xml.bind.attachment.AttachmentMarshaller getAttachmentMarshaller()
meth public abstract javax.xml.validation.Schema getSchema()
meth public abstract org.w3c.dom.Node getNode(java.lang.Object) throws javax.xml.bind.JAXBException
meth public abstract void marshal(java.lang.Object,java.io.File) throws javax.xml.bind.JAXBException
meth public abstract void marshal(java.lang.Object,java.io.OutputStream) throws javax.xml.bind.JAXBException
meth public abstract void marshal(java.lang.Object,java.io.Writer) throws javax.xml.bind.JAXBException
meth public abstract void marshal(java.lang.Object,javax.xml.stream.XMLEventWriter) throws javax.xml.bind.JAXBException
meth public abstract void marshal(java.lang.Object,javax.xml.stream.XMLStreamWriter) throws javax.xml.bind.JAXBException
meth public abstract void marshal(java.lang.Object,javax.xml.transform.Result) throws javax.xml.bind.JAXBException
meth public abstract void marshal(java.lang.Object,org.w3c.dom.Node) throws javax.xml.bind.JAXBException
meth public abstract void marshal(java.lang.Object,org.xml.sax.ContentHandler) throws javax.xml.bind.JAXBException
meth public abstract void setAdapter(javax.xml.bind.annotation.adapters.XmlAdapter)
meth public abstract void setAttachmentMarshaller(javax.xml.bind.attachment.AttachmentMarshaller)
meth public abstract void setEventHandler(javax.xml.bind.ValidationEventHandler) throws javax.xml.bind.JAXBException
meth public abstract void setListener(javax.xml.bind.Marshaller$Listener)
meth public abstract void setProperty(java.lang.String,java.lang.Object) throws javax.xml.bind.PropertyException
meth public abstract void setSchema(javax.xml.validation.Schema)

CLSS public abstract interface javax.xml.bind.Unmarshaller
innr public abstract static Listener
meth public abstract <%0 extends java.lang.Object> javax.xml.bind.JAXBElement<{%%0}> unmarshal(javax.xml.stream.XMLEventReader,java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth public abstract <%0 extends java.lang.Object> javax.xml.bind.JAXBElement<{%%0}> unmarshal(javax.xml.stream.XMLStreamReader,java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth public abstract <%0 extends java.lang.Object> javax.xml.bind.JAXBElement<{%%0}> unmarshal(javax.xml.transform.Source,java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth public abstract <%0 extends java.lang.Object> javax.xml.bind.JAXBElement<{%%0}> unmarshal(org.w3c.dom.Node,java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth public abstract <%0 extends javax.xml.bind.annotation.adapters.XmlAdapter> void setAdapter(java.lang.Class<{%%0}>,{%%0})
meth public abstract <%0 extends javax.xml.bind.annotation.adapters.XmlAdapter> {%%0} getAdapter(java.lang.Class<{%%0}>)
meth public abstract boolean isValidating() throws javax.xml.bind.JAXBException
meth public abstract java.lang.Object getProperty(java.lang.String) throws javax.xml.bind.PropertyException
meth public abstract java.lang.Object unmarshal(java.io.File) throws javax.xml.bind.JAXBException
meth public abstract java.lang.Object unmarshal(java.io.InputStream) throws javax.xml.bind.JAXBException
meth public abstract java.lang.Object unmarshal(java.io.Reader) throws javax.xml.bind.JAXBException
meth public abstract java.lang.Object unmarshal(java.net.URL) throws javax.xml.bind.JAXBException
meth public abstract java.lang.Object unmarshal(javax.xml.stream.XMLEventReader) throws javax.xml.bind.JAXBException
meth public abstract java.lang.Object unmarshal(javax.xml.stream.XMLStreamReader) throws javax.xml.bind.JAXBException
meth public abstract java.lang.Object unmarshal(javax.xml.transform.Source) throws javax.xml.bind.JAXBException
meth public abstract java.lang.Object unmarshal(org.w3c.dom.Node) throws javax.xml.bind.JAXBException
meth public abstract java.lang.Object unmarshal(org.xml.sax.InputSource) throws javax.xml.bind.JAXBException
meth public abstract javax.xml.bind.Unmarshaller$Listener getListener()
meth public abstract javax.xml.bind.UnmarshallerHandler getUnmarshallerHandler()
meth public abstract javax.xml.bind.ValidationEventHandler getEventHandler() throws javax.xml.bind.JAXBException
meth public abstract javax.xml.bind.attachment.AttachmentUnmarshaller getAttachmentUnmarshaller()
meth public abstract javax.xml.validation.Schema getSchema()
meth public abstract void setAdapter(javax.xml.bind.annotation.adapters.XmlAdapter)
meth public abstract void setAttachmentUnmarshaller(javax.xml.bind.attachment.AttachmentUnmarshaller)
meth public abstract void setEventHandler(javax.xml.bind.ValidationEventHandler) throws javax.xml.bind.JAXBException
meth public abstract void setListener(javax.xml.bind.Unmarshaller$Listener)
meth public abstract void setProperty(java.lang.String,java.lang.Object) throws javax.xml.bind.PropertyException
meth public abstract void setSchema(javax.xml.validation.Schema)
meth public abstract void setValidating(boolean) throws javax.xml.bind.JAXBException

CLSS public abstract interface javax.xml.bind.UnmarshallerHandler
intf org.xml.sax.ContentHandler
meth public abstract java.lang.Object getResult() throws javax.xml.bind.JAXBException

CLSS public abstract interface javax.xml.bind.ValidationEventHandler
meth public abstract boolean handleEvent(javax.xml.bind.ValidationEvent)

CLSS public abstract interface javax.xml.bind.ValidationEventLocator
meth public abstract int getColumnNumber()
meth public abstract int getLineNumber()
meth public abstract int getOffset()
meth public abstract java.lang.Object getObject()
meth public abstract java.net.URL getURL()
meth public abstract org.w3c.dom.Node getNode()

CLSS public abstract interface !annotation javax.xml.bind.annotation.XmlEnum
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<?> value()

CLSS public abstract interface !annotation javax.xml.bind.annotation.XmlRootElement
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String namespace()

CLSS public abstract interface !annotation javax.xml.bind.annotation.XmlSchema
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE])
fld public final static java.lang.String NO_LOCATION = "##generate"
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String location()
meth public abstract !hasdefault java.lang.String namespace()
meth public abstract !hasdefault javax.xml.bind.annotation.XmlNsForm attributeFormDefault()
meth public abstract !hasdefault javax.xml.bind.annotation.XmlNsForm elementFormDefault()
meth public abstract !hasdefault javax.xml.bind.annotation.XmlNs[] xmlns()

CLSS public abstract interface !annotation javax.xml.bind.annotation.XmlType
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
innr public final static DEFAULT
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class factoryClass()
meth public abstract !hasdefault java.lang.String factoryMethod()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String namespace()
meth public abstract !hasdefault java.lang.String[] propOrder()

CLSS public abstract javax.xml.bind.annotation.adapters.XmlAdapter<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init()
meth public abstract {javax.xml.bind.annotation.adapters.XmlAdapter%0} marshal({javax.xml.bind.annotation.adapters.XmlAdapter%1}) throws java.lang.Exception
meth public abstract {javax.xml.bind.annotation.adapters.XmlAdapter%1} unmarshal({javax.xml.bind.annotation.adapters.XmlAdapter%0}) throws java.lang.Exception
supr java.lang.Object

CLSS public abstract javax.xml.bind.helpers.AbstractMarshallerImpl
cons public init()
intf javax.xml.bind.Marshaller
meth protected boolean isFormattedOutput()
meth protected boolean isFragment()
meth protected java.lang.String getEncoding()
meth protected java.lang.String getJavaEncoding(java.lang.String) throws java.io.UnsupportedEncodingException
meth protected java.lang.String getNoNSSchemaLocation()
meth protected java.lang.String getSchemaLocation()
meth protected void setEncoding(java.lang.String)
meth protected void setFormattedOutput(boolean)
meth protected void setFragment(boolean)
meth protected void setNoNSSchemaLocation(java.lang.String)
meth protected void setSchemaLocation(java.lang.String)
meth public <%0 extends javax.xml.bind.annotation.adapters.XmlAdapter> void setAdapter(java.lang.Class<{%%0}>,{%%0})
meth public <%0 extends javax.xml.bind.annotation.adapters.XmlAdapter> {%%0} getAdapter(java.lang.Class<{%%0}>)
meth public final void marshal(java.lang.Object,java.io.OutputStream) throws javax.xml.bind.JAXBException
meth public final void marshal(java.lang.Object,java.io.Writer) throws javax.xml.bind.JAXBException
meth public final void marshal(java.lang.Object,org.w3c.dom.Node) throws javax.xml.bind.JAXBException
meth public final void marshal(java.lang.Object,org.xml.sax.ContentHandler) throws javax.xml.bind.JAXBException
meth public java.lang.Object getProperty(java.lang.String) throws javax.xml.bind.PropertyException
meth public javax.xml.bind.Marshaller$Listener getListener()
meth public javax.xml.bind.ValidationEventHandler getEventHandler() throws javax.xml.bind.JAXBException
meth public javax.xml.bind.attachment.AttachmentMarshaller getAttachmentMarshaller()
meth public javax.xml.validation.Schema getSchema()
meth public org.w3c.dom.Node getNode(java.lang.Object) throws javax.xml.bind.JAXBException
meth public void marshal(java.lang.Object,java.io.File) throws javax.xml.bind.JAXBException
meth public void marshal(java.lang.Object,javax.xml.stream.XMLEventWriter) throws javax.xml.bind.JAXBException
meth public void marshal(java.lang.Object,javax.xml.stream.XMLStreamWriter) throws javax.xml.bind.JAXBException
meth public void setAdapter(javax.xml.bind.annotation.adapters.XmlAdapter)
meth public void setAttachmentMarshaller(javax.xml.bind.attachment.AttachmentMarshaller)
meth public void setEventHandler(javax.xml.bind.ValidationEventHandler) throws javax.xml.bind.JAXBException
meth public void setListener(javax.xml.bind.Marshaller$Listener)
meth public void setProperty(java.lang.String,java.lang.Object) throws javax.xml.bind.PropertyException
meth public void setSchema(javax.xml.validation.Schema)
supr java.lang.Object
hfds aliases,encoding,eventHandler,formattedOutput,fragment,noNSSchemaLocation,schemaLocation

CLSS public abstract javax.xml.bind.helpers.AbstractUnmarshallerImpl
cons public init()
fld protected boolean validating
intf javax.xml.bind.Unmarshaller
meth protected abstract java.lang.Object unmarshal(org.xml.sax.XMLReader,org.xml.sax.InputSource) throws javax.xml.bind.JAXBException
meth protected javax.xml.bind.UnmarshalException createUnmarshalException(org.xml.sax.SAXException)
meth protected org.xml.sax.XMLReader getXMLReader() throws javax.xml.bind.JAXBException
meth public <%0 extends java.lang.Object> javax.xml.bind.JAXBElement<{%%0}> unmarshal(javax.xml.stream.XMLEventReader,java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth public <%0 extends java.lang.Object> javax.xml.bind.JAXBElement<{%%0}> unmarshal(javax.xml.stream.XMLStreamReader,java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth public <%0 extends java.lang.Object> javax.xml.bind.JAXBElement<{%%0}> unmarshal(javax.xml.transform.Source,java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth public <%0 extends java.lang.Object> javax.xml.bind.JAXBElement<{%%0}> unmarshal(org.w3c.dom.Node,java.lang.Class<{%%0}>) throws javax.xml.bind.JAXBException
meth public <%0 extends javax.xml.bind.annotation.adapters.XmlAdapter> void setAdapter(java.lang.Class<{%%0}>,{%%0})
meth public <%0 extends javax.xml.bind.annotation.adapters.XmlAdapter> {%%0} getAdapter(java.lang.Class<{%%0}>)
meth public boolean isValidating() throws javax.xml.bind.JAXBException
meth public final java.lang.Object unmarshal(java.io.File) throws javax.xml.bind.JAXBException
meth public final java.lang.Object unmarshal(java.io.InputStream) throws javax.xml.bind.JAXBException
meth public final java.lang.Object unmarshal(java.io.Reader) throws javax.xml.bind.JAXBException
meth public final java.lang.Object unmarshal(java.net.URL) throws javax.xml.bind.JAXBException
meth public final java.lang.Object unmarshal(org.xml.sax.InputSource) throws javax.xml.bind.JAXBException
meth public java.lang.Object getProperty(java.lang.String) throws javax.xml.bind.PropertyException
meth public java.lang.Object unmarshal(javax.xml.stream.XMLEventReader) throws javax.xml.bind.JAXBException
meth public java.lang.Object unmarshal(javax.xml.stream.XMLStreamReader) throws javax.xml.bind.JAXBException
meth public java.lang.Object unmarshal(javax.xml.transform.Source) throws javax.xml.bind.JAXBException
meth public javax.xml.bind.Unmarshaller$Listener getListener()
meth public javax.xml.bind.ValidationEventHandler getEventHandler() throws javax.xml.bind.JAXBException
meth public javax.xml.bind.attachment.AttachmentUnmarshaller getAttachmentUnmarshaller()
meth public javax.xml.validation.Schema getSchema()
meth public void setAdapter(javax.xml.bind.annotation.adapters.XmlAdapter)
meth public void setAttachmentUnmarshaller(javax.xml.bind.attachment.AttachmentUnmarshaller)
meth public void setEventHandler(javax.xml.bind.ValidationEventHandler) throws javax.xml.bind.JAXBException
meth public void setListener(javax.xml.bind.Unmarshaller$Listener)
meth public void setProperty(java.lang.String,java.lang.Object) throws javax.xml.bind.PropertyException
meth public void setSchema(javax.xml.validation.Schema)
meth public void setValidating(boolean) throws javax.xml.bind.JAXBException
supr java.lang.Object
hfds eventHandler,reader

CLSS public javax.xml.bind.helpers.ValidationEventLocatorImpl
cons public init()
cons public init(java.lang.Object)
cons public init(org.w3c.dom.Node)
cons public init(org.xml.sax.Locator)
cons public init(org.xml.sax.SAXParseException)
intf javax.xml.bind.ValidationEventLocator
meth public int getColumnNumber()
meth public int getLineNumber()
meth public int getOffset()
meth public java.lang.Object getObject()
meth public java.lang.String toString()
meth public java.net.URL getURL()
meth public org.w3c.dom.Node getNode()
meth public void setColumnNumber(int)
meth public void setLineNumber(int)
meth public void setNode(org.w3c.dom.Node)
meth public void setObject(java.lang.Object)
meth public void setOffset(int)
meth public void setURL(java.net.URL)
supr java.lang.Object
hfds columnNumber,lineNumber,node,object,offset,url

CLSS public abstract interface javax.xml.namespace.NamespaceContext
meth public abstract java.lang.String getNamespaceURI(java.lang.String)
meth public abstract java.lang.String getPrefix(java.lang.String)
meth public abstract java.util.Iterator getPrefixes(java.lang.String)

CLSS public javax.xml.stream.XMLStreamException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,javax.xml.stream.Location)
cons public init(java.lang.String,javax.xml.stream.Location,java.lang.Throwable)
cons public init(java.lang.Throwable)
fld protected java.lang.Throwable nested
fld protected javax.xml.stream.Location location
meth public java.lang.Throwable getNestedException()
meth public javax.xml.stream.Location getLocation()
supr java.lang.Exception

CLSS public abstract interface javax.xml.transform.Source
meth public abstract java.lang.String getSystemId()
meth public abstract void setSystemId(java.lang.String)

CLSS public javax.xml.transform.stream.StreamSource
cons public init()
cons public init(java.io.File)
cons public init(java.io.InputStream)
cons public init(java.io.InputStream,java.lang.String)
cons public init(java.io.Reader)
cons public init(java.io.Reader,java.lang.String)
cons public init(java.lang.String)
fld public final static java.lang.String FEATURE = "http://javax.xml.transform.stream.StreamSource/feature"
intf javax.xml.transform.Source
meth public java.io.InputStream getInputStream()
meth public java.io.Reader getReader()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
meth public void setInputStream(java.io.InputStream)
meth public void setPublicId(java.lang.String)
meth public void setReader(java.io.Reader)
meth public void setSystemId(java.io.File)
meth public void setSystemId(java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.w3c.dom.ls.LSInput
meth public abstract boolean getCertifiedText()
meth public abstract java.io.InputStream getByteStream()
meth public abstract java.io.Reader getCharacterStream()
meth public abstract java.lang.String getBaseURI()
meth public abstract java.lang.String getEncoding()
meth public abstract java.lang.String getPublicId()
meth public abstract java.lang.String getStringData()
meth public abstract java.lang.String getSystemId()
meth public abstract void setBaseURI(java.lang.String)
meth public abstract void setByteStream(java.io.InputStream)
meth public abstract void setCertifiedText(boolean)
meth public abstract void setCharacterStream(java.io.Reader)
meth public abstract void setEncoding(java.lang.String)
meth public abstract void setPublicId(java.lang.String)
meth public abstract void setStringData(java.lang.String)
meth public abstract void setSystemId(java.lang.String)

CLSS public abstract interface org.xml.sax.Attributes
meth public abstract int getIndex(java.lang.String)
meth public abstract int getIndex(java.lang.String,java.lang.String)
meth public abstract int getLength()
meth public abstract java.lang.String getLocalName(int)
meth public abstract java.lang.String getQName(int)
meth public abstract java.lang.String getType(int)
meth public abstract java.lang.String getType(java.lang.String)
meth public abstract java.lang.String getType(java.lang.String,java.lang.String)
meth public abstract java.lang.String getURI(int)
meth public abstract java.lang.String getValue(int)
meth public abstract java.lang.String getValue(java.lang.String)
meth public abstract java.lang.String getValue(java.lang.String,java.lang.String)

CLSS public abstract interface org.xml.sax.ContentHandler
meth public abstract void characters(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void endDocument() throws org.xml.sax.SAXException
meth public abstract void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void setDocumentLocator(org.xml.sax.Locator)
meth public abstract void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void startDocument() throws org.xml.sax.SAXException
meth public abstract void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public abstract void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.DTDHandler
meth public abstract void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.EntityResolver
meth public abstract org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.ErrorHandler
meth public abstract void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.Locator
meth public abstract int getColumnNumber()
meth public abstract int getLineNumber()
meth public abstract java.lang.String getPublicId()
meth public abstract java.lang.String getSystemId()

CLSS public org.xml.sax.SAXException
cons public init()
cons public init(java.lang.Exception)
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
meth public java.lang.Exception getException()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable getCause()
supr java.lang.Exception

CLSS public org.xml.sax.SAXParseException
cons public init(java.lang.String,java.lang.String,java.lang.String,int,int)
cons public init(java.lang.String,java.lang.String,java.lang.String,int,int,java.lang.Exception)
cons public init(java.lang.String,org.xml.sax.Locator)
cons public init(java.lang.String,org.xml.sax.Locator,java.lang.Exception)
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
meth public java.lang.String toString()
supr org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.XMLFilter
intf org.xml.sax.XMLReader
meth public abstract org.xml.sax.XMLReader getParent()
meth public abstract void setParent(org.xml.sax.XMLReader)

CLSS public abstract interface org.xml.sax.XMLReader
meth public abstract boolean getFeature(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public abstract java.lang.Object getProperty(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public abstract org.xml.sax.ContentHandler getContentHandler()
meth public abstract org.xml.sax.DTDHandler getDTDHandler()
meth public abstract org.xml.sax.EntityResolver getEntityResolver()
meth public abstract org.xml.sax.ErrorHandler getErrorHandler()
meth public abstract void parse(java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public abstract void parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public abstract void setContentHandler(org.xml.sax.ContentHandler)
meth public abstract void setDTDHandler(org.xml.sax.DTDHandler)
meth public abstract void setEntityResolver(org.xml.sax.EntityResolver)
meth public abstract void setErrorHandler(org.xml.sax.ErrorHandler)
meth public abstract void setFeature(java.lang.String,boolean) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public abstract void setProperty(java.lang.String,java.lang.Object) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException

CLSS public org.xml.sax.helpers.XMLFilterImpl
cons public init()
cons public init(org.xml.sax.XMLReader)
intf org.xml.sax.ContentHandler
intf org.xml.sax.DTDHandler
intf org.xml.sax.EntityResolver
intf org.xml.sax.ErrorHandler
intf org.xml.sax.XMLFilter
meth public boolean getFeature(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public java.lang.Object getProperty(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public org.xml.sax.ContentHandler getContentHandler()
meth public org.xml.sax.DTDHandler getDTDHandler()
meth public org.xml.sax.EntityResolver getEntityResolver()
meth public org.xml.sax.ErrorHandler getErrorHandler()
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public org.xml.sax.XMLReader getParent()
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void parse(java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setContentHandler(org.xml.sax.ContentHandler)
meth public void setDTDHandler(org.xml.sax.DTDHandler)
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void setEntityResolver(org.xml.sax.EntityResolver)
meth public void setErrorHandler(org.xml.sax.ErrorHandler)
meth public void setFeature(java.lang.String,boolean) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public void setParent(org.xml.sax.XMLReader)
meth public void setProperty(java.lang.String,java.lang.Object) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr java.lang.Object

