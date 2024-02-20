#Signature file v4.1
#Version 2.9.0

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

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

CLSS public final org.netbeans.modules.java.completion.JavaCompletionTask<%0 extends java.lang.Object>
fld protected final int caretOffset
fld protected final java.util.concurrent.Callable<java.lang.Boolean> cancel
innr public abstract interface static ItemFactory
innr public abstract interface static LambdaItemFactory
innr public abstract interface static ModuleItemFactory
innr public abstract interface static RecordPatternItemFactory
innr public abstract interface static TypeCastableItemFactory
innr public final static !enum Options
meth protected void resolve(org.netbeans.api.java.source.CompilationController) throws java.io.IOException
meth public boolean hasAdditionalClasses()
meth public boolean hasAdditionalMembers()
meth public int getAnchorOffset()
meth public java.util.List<{org.netbeans.modules.java.completion.JavaCompletionTask%0}> getResults()
meth public static <%0 extends java.lang.Object> org.netbeans.modules.java.completion.JavaCompletionTask<{%%0}> create(int,org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory<{%%0}>,java.util.Set<org.netbeans.modules.java.completion.JavaCompletionTask$Options>,java.util.concurrent.Callable<java.lang.Boolean>)
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public void run(org.netbeans.modules.parsing.api.ResultIterator) throws java.lang.Exception
supr org.netbeans.modules.parsing.api.UserTask
hfds ABSTRACT_KEYWORD,ASSERT_KEYWORD,BLOCK_KEYWORDS,BOOLEAN_KEYWORD,BREAK_KEYWORD,BYTE_KEYWORD,CASE_KEYWORD,CATCH_KEYWORD,CHAR_KEYWORD,CLASS_BODY_KEYWORDS,CLASS_KEYWORD,COLON,CONTINUE_KEYWORD,DEFAULT_KEYWORD,DOUBLE_KEYWORD,DO_KEYWORD,ELSE_KEYWORD,EMPTY,ENUM_KEYWORD,ERROR,EXPORTS_KEYWORD,EXTENDS_KEYWORD,FALSE_KEYWORD,FINALLY_KEYWORD,FINAL_KEYWORD,FLOAT_KEYWORD,FOR_KEYWORD,IF_KEYWORD,IMPLEMENTS_KEYWORD,IMPORT_KEYWORD,INIT,INSTANCEOF_KEYWORD,INTERFACE_KEYWORD,INT_KEYWORD,JAVA_LANG_CLASS,JAVA_LANG_ITERABLE,JAVA_LANG_OBJECT,LONG_KEYWORD,MODULE_BODY_KEYWORDS,MODULE_KEYWORD,NATIVE_KEYWORD,NEW_KEYWORD,NON_SEALED_KEYWORD,NULL_KEYWORD,OPENS_KEYWORD,OPEN_KEYWORD,PACKAGE_KEYWORD,PERMITS_KEYWORD,PRIM_KEYWORDS,PRIVATE_KEYWORD,PROTECTED_KEYWORD,PROVIDES_KEYWORD,PUBLIC_KEYWORD,RECORD_KEYWORD,REQUIRES_KEYWORD,RETURN_KEYWORD,SEALED_KEYWORD,SEMI,SHORT_KEYWORD,SPACE,STATEMENT_KEYWORDS,STATEMENT_SPACE_KEYWORDS,STATIC_KEYWORD,STRICT_KEYWORD,SUPER_KEYWORD,SWITCH_KEYWORD,SYNCHRONIZED_KEYWORD,THIS_KEYWORD,THROWS_KEYWORD,THROW_KEYWORD,TO_KEYWORD,TRANSIENT_KEYWORD,TRANSITIVE_KEYWORD,TRUE_KEYWORD,TRY_KEYWORD,USES_KEYWORD,VAR_KEYWORD,VOID_KEYWORD,VOLATILE_KEYWORD,WHEN_KEYWORD,WHILE_KEYWORD,WITH_KEYWORD,YIELD_KEYWORD,anchorOffset,hasAdditionalClasses,hasAdditionalMembers,itemFactory,options,results

CLSS public abstract interface static org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory<%0 extends java.lang.Object>
 outer org.netbeans.modules.java.completion.JavaCompletionTask
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createAnnotationItem(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.TypeElement,javax.lang.model.type.DeclaredType,int,org.netbeans.api.java.source.support.ReferencesCount,boolean)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createArrayItem(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.type.ArrayType,int,org.netbeans.api.java.source.support.ReferencesCount,javax.lang.model.util.Elements)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createAttributeItem(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.ExecutableElement,javax.lang.model.type.ExecutableType,int,boolean)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createAttributeValueItem(org.netbeans.api.java.source.CompilationInfo,java.lang.String,java.lang.String,javax.lang.model.element.TypeElement,int,org.netbeans.api.java.source.support.ReferencesCount)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createChainedMembersItem(org.netbeans.api.java.source.CompilationInfo,java.util.List<? extends javax.lang.model.element.Element>,java.util.List<? extends javax.lang.model.type.TypeMirror>,int,boolean,boolean)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createDefaultConstructorItem(javax.lang.model.element.TypeElement,int,boolean)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createExecutableItem(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.ExecutableElement,javax.lang.model.type.ExecutableType,int,org.netbeans.api.java.source.support.ReferencesCount,boolean,boolean,boolean,boolean,boolean,int,boolean)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createGetterSetterMethodItem(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.VariableElement,javax.lang.model.type.TypeMirror,int,java.lang.String,boolean)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createInitializeAllConstructorItem(org.netbeans.api.java.source.CompilationInfo,boolean,java.lang.Iterable<? extends javax.lang.model.element.VariableElement>,javax.lang.model.element.ExecutableElement,javax.lang.model.element.TypeElement,int)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createKeywordItem(java.lang.String,java.lang.String,int,boolean)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createOverrideMethodItem(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.ExecutableElement,javax.lang.model.type.ExecutableType,int,boolean)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createPackageItem(java.lang.String,int,boolean)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createParametersItem(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.ExecutableElement,javax.lang.model.type.ExecutableType,int,boolean,int,java.lang.String)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createStaticMemberItem(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.type.DeclaredType,javax.lang.model.element.Element,javax.lang.model.type.TypeMirror,boolean,int,boolean,boolean)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createStaticMemberItem(org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>,java.lang.String,int,boolean,org.netbeans.api.java.source.support.ReferencesCount,org.netbeans.modules.parsing.api.Source)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createThisOrSuperConstructorItem(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.ExecutableElement,javax.lang.model.type.ExecutableType,int,boolean,java.lang.String)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createTypeItem(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.TypeElement,javax.lang.model.type.DeclaredType,int,org.netbeans.api.java.source.support.ReferencesCount,boolean,boolean,boolean,boolean,boolean,boolean)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createTypeItem(org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>,java.util.EnumSet<javax.lang.model.element.ElementKind>,int,org.netbeans.api.java.source.support.ReferencesCount,org.netbeans.modules.parsing.api.Source,boolean,boolean,boolean)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createTypeParameterItem(javax.lang.model.element.TypeParameterElement,int)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createVariableItem(org.netbeans.api.java.source.CompilationInfo,java.lang.String,int,boolean,boolean)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory%0} createVariableItem(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.VariableElement,javax.lang.model.type.TypeMirror,int,org.netbeans.api.java.source.support.ReferencesCount,boolean,boolean,boolean,int)

CLSS public abstract interface static org.netbeans.modules.java.completion.JavaCompletionTask$LambdaItemFactory<%0 extends java.lang.Object>
 outer org.netbeans.modules.java.completion.JavaCompletionTask
intf org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory<{org.netbeans.modules.java.completion.JavaCompletionTask$LambdaItemFactory%0}>
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$LambdaItemFactory%0} createLambdaItem(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.TypeElement,javax.lang.model.type.DeclaredType,int,boolean,boolean)

CLSS public abstract interface static org.netbeans.modules.java.completion.JavaCompletionTask$ModuleItemFactory<%0 extends java.lang.Object>
 outer org.netbeans.modules.java.completion.JavaCompletionTask
intf org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory<{org.netbeans.modules.java.completion.JavaCompletionTask$ModuleItemFactory%0}>
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$ModuleItemFactory%0} createModuleItem(java.lang.String,int)

CLSS public final static !enum org.netbeans.modules.java.completion.JavaCompletionTask$Options
 outer org.netbeans.modules.java.completion.JavaCompletionTask
fld public final static org.netbeans.modules.java.completion.JavaCompletionTask$Options ALL_COMPLETION
fld public final static org.netbeans.modules.java.completion.JavaCompletionTask$Options COMBINED_COMPLETION
fld public final static org.netbeans.modules.java.completion.JavaCompletionTask$Options SKIP_ACCESSIBILITY_CHECK
meth public static org.netbeans.modules.java.completion.JavaCompletionTask$Options valueOf(java.lang.String)
meth public static org.netbeans.modules.java.completion.JavaCompletionTask$Options[] values()
supr java.lang.Enum<org.netbeans.modules.java.completion.JavaCompletionTask$Options>

CLSS public abstract interface static org.netbeans.modules.java.completion.JavaCompletionTask$RecordPatternItemFactory<%0 extends java.lang.Object>
 outer org.netbeans.modules.java.completion.JavaCompletionTask
intf org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory<{org.netbeans.modules.java.completion.JavaCompletionTask$RecordPatternItemFactory%0}>
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$RecordPatternItemFactory%0} createRecordPatternItem(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.TypeElement,javax.lang.model.type.DeclaredType,int,org.netbeans.api.java.source.support.ReferencesCount,boolean,boolean,boolean)

CLSS public abstract interface static org.netbeans.modules.java.completion.JavaCompletionTask$TypeCastableItemFactory<%0 extends java.lang.Object>
 outer org.netbeans.modules.java.completion.JavaCompletionTask
intf org.netbeans.modules.java.completion.JavaCompletionTask$ItemFactory<{org.netbeans.modules.java.completion.JavaCompletionTask$TypeCastableItemFactory%0}>
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$TypeCastableItemFactory%0} createTypeCastableExecutableItem(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.ExecutableElement,javax.lang.model.type.ExecutableType,javax.lang.model.type.TypeMirror,int,org.netbeans.api.java.source.support.ReferencesCount,boolean,boolean,boolean,boolean,boolean,int,boolean)
meth public abstract {org.netbeans.modules.java.completion.JavaCompletionTask$TypeCastableItemFactory%0} createTypeCastableVariableItem(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.VariableElement,javax.lang.model.type.TypeMirror,javax.lang.model.type.TypeMirror,int,org.netbeans.api.java.source.support.ReferencesCount,boolean,boolean,boolean,int)

CLSS public final org.netbeans.modules.java.completion.JavaDocumentationTask<%0 extends java.lang.Object>
fld protected final int caretOffset
fld protected final java.util.concurrent.Callable<java.lang.Boolean> cancel
innr public abstract interface static DocumentationFactory
meth protected void resolve(org.netbeans.api.java.source.CompilationController) throws java.io.IOException
meth public static <%0 extends java.lang.Object> org.netbeans.modules.java.completion.JavaDocumentationTask<{%%0}> create(int,org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.Element>,org.netbeans.modules.java.completion.JavaDocumentationTask$DocumentationFactory<{%%0}>,java.util.concurrent.Callable<java.lang.Boolean>)
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public void run(org.netbeans.modules.parsing.api.ResultIterator) throws java.lang.Exception
meth public {org.netbeans.modules.java.completion.JavaDocumentationTask%0} getDocumentation()
supr org.netbeans.modules.parsing.api.UserTask
hfds documentation,element,factory

CLSS public abstract interface static org.netbeans.modules.java.completion.JavaDocumentationTask$DocumentationFactory<%0 extends java.lang.Object>
 outer org.netbeans.modules.java.completion.JavaDocumentationTask
meth public abstract {org.netbeans.modules.java.completion.JavaDocumentationTask$DocumentationFactory%0} create(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.Element,java.util.concurrent.Callable<java.lang.Boolean>)

CLSS public final org.netbeans.modules.java.completion.JavaTooltipTask
fld protected final int caretOffset
fld protected final java.util.concurrent.Callable<java.lang.Boolean> cancel
meth protected void resolve(org.netbeans.api.java.source.CompilationController) throws java.io.IOException
meth public int getActiveSignatureIndex()
meth public int getAnchorOffset()
meth public int getTooltipIndex()
meth public int getTooltipOffset()
meth public java.util.List<java.lang.String> getTooltipSignatures()
meth public java.util.List<java.util.List<java.lang.String>> getTooltipData()
meth public static org.netbeans.modules.java.completion.JavaTooltipTask create(int,java.util.concurrent.Callable<java.lang.Boolean>)
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public void run(org.netbeans.modules.parsing.api.ResultIterator) throws java.lang.Exception
supr org.netbeans.modules.parsing.api.UserTask
hfds INIT,SUPER_KEYWORD,THIS_KEYWORD,activeSignatureIndex,anchorOffset,toolTipData,toolTipIndex,toolTipOffset,toolTipSignatures

CLSS public final org.netbeans.modules.java.completion.Utilities
meth public static boolean isCaseSensitive()
meth public static boolean isExcludeMethods()
meth public static boolean isExcluded(java.lang.CharSequence)
meth public static boolean isShowDeprecatedMembers()
meth public static boolean isSubwordSensitive()
meth public static boolean startsWith(java.lang.String,java.lang.String)
meth public static boolean startsWithCamelCase(java.lang.String,java.lang.String)
meth public static java.util.List<java.lang.String> varNamesSuggestions(javax.lang.model.type.TypeMirror,javax.lang.model.element.ElementKind,java.util.Set<javax.lang.model.element.Modifier>,java.lang.String,java.lang.String,javax.lang.model.util.Types,javax.lang.model.util.Elements,java.lang.Iterable<? extends javax.lang.model.element.Element>,org.netbeans.api.java.source.CodeStyle)
meth public static void exclude(java.lang.CharSequence)
supr java.lang.Object
hfds COMPLETION_CASE_SENSITIVE,COMPLETION_CASE_SENSITIVE_DEFAULT,EMPTY,ERROR,JAVA_COMPLETION_BLACKLIST,JAVA_COMPLETION_BLACKLIST_DEFAULT,JAVA_COMPLETION_EXCLUDER_METHODS,JAVA_COMPLETION_EXCLUDER_METHODS_DEFAULT,JAVA_COMPLETION_SUBWORDS,JAVA_COMPLETION_SUBWORDS_DEFAULT,JAVA_COMPLETION_WHITELIST,SHOW_DEPRECATED_MEMBERS,SHOW_DEPRECATED_MEMBERS_DEFAULT,cachedCamelCasePattern,cachedPrefix,cachedSubwordsPattern,caseSensitive,excludeRef,includeRef,inited,javaCompletionExcluderMethods,javaCompletionSubwords,preferences,preferencesTracker,showDeprecatedMembers

CLSS public abstract org.netbeans.modules.parsing.api.Task
cons public init()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.api.UserTask
cons public init()
meth public abstract void run(org.netbeans.modules.parsing.api.ResultIterator) throws java.lang.Exception
supr org.netbeans.modules.parsing.api.Task

