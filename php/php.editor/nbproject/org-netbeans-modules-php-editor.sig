#Signature file v4.1
#Version 2.36.0

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

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

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface java_cup.runtime.Scanner
meth public abstract java_cup.runtime.Symbol next_token() throws java.lang.Exception

CLSS public abstract java_cup.runtime.lr_parser
cons public init()
cons public init(java_cup.runtime.Scanner)
cons public init(java_cup.runtime.Scanner,java_cup.runtime.SymbolFactory)
fld protected boolean _done_parsing
fld protected final static int _error_sync_size = 3
fld protected int lookahead_pos
fld protected int tos
fld protected java.util.Stack stack
fld protected java_cup.runtime.Symbol cur_token
fld protected java_cup.runtime.Symbol[] lookahead
fld protected short[][] action_tab
fld protected short[][] production_tab
fld protected short[][] reduce_tab
fld public java_cup.runtime.SymbolFactory symbolFactory
meth protected abstract void init_actions() throws java.lang.Exception
meth protected boolean advance_lookahead()
meth protected boolean error_recovery(boolean) throws java.lang.Exception
meth protected boolean find_recovery_config(boolean)
meth protected boolean shift_under_error()
meth protected boolean try_parse_ahead(boolean) throws java.lang.Exception
meth protected final short get_action(int,int)
meth protected final short get_reduce(int,int)
meth protected int error_sync_size()
meth protected java_cup.runtime.Symbol cur_err_token()
meth protected static short[][] unpackFromStrings(java.lang.String[])
meth protected void parse_lookahead(boolean) throws java.lang.Exception
meth protected void read_lookahead() throws java.lang.Exception
meth protected void restart_lookahead() throws java.lang.Exception
meth public abstract int EOF_sym()
meth public abstract int error_sym()
meth public abstract int start_production()
meth public abstract int start_state()
meth public abstract java_cup.runtime.Symbol do_action(int,java_cup.runtime.lr_parser,java.util.Stack,int) throws java.lang.Exception
meth public abstract short[][] action_table()
meth public abstract short[][] production_table()
meth public abstract short[][] reduce_table()
meth public java_cup.runtime.Scanner getScanner()
meth public java_cup.runtime.Symbol debug_parse() throws java.lang.Exception
meth public java_cup.runtime.Symbol parse() throws java.lang.Exception
meth public java_cup.runtime.Symbol scan() throws java.lang.Exception
meth public java_cup.runtime.SymbolFactory getSymbolFactory()
meth public void debug_message(java.lang.String)
meth public void debug_reduce(int,int,int)
meth public void debug_shift(java_cup.runtime.Symbol)
meth public void debug_stack()
meth public void done_parsing()
meth public void dump_stack()
meth public void report_error(java.lang.String,java.lang.Object)
meth public void report_fatal_error(java.lang.String,java.lang.Object) throws java.lang.Exception
meth public void setScanner(java_cup.runtime.Scanner)
meth public void syntax_error(java_cup.runtime.Symbol)
meth public void unrecovered_syntax_error(java_cup.runtime.Symbol) throws java.lang.Exception
meth public void user_init() throws java.lang.Exception
supr java.lang.Object
hfds _scanner

CLSS public abstract interface !annotation org.netbeans.api.annotations.common.SuppressWarnings
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String justification()
meth public abstract !hasdefault java.lang.String[] value()

CLSS public abstract interface org.netbeans.api.lexer.TokenId
meth public abstract int ordinal()
meth public abstract java.lang.String name()
meth public abstract java.lang.String primaryCategory()

CLSS public abstract interface org.netbeans.modules.csl.api.ElementHandle
innr public static UrlHandle
meth public abstract boolean signatureEquals(org.netbeans.modules.csl.api.ElementHandle)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getIn()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getMimeType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.ElementKind getKind()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getOffsetRange(org.netbeans.modules.csl.spi.ParserResult)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject getFileObject()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.csl.api.Error
innr public abstract interface static Badging
meth public abstract boolean isLineError()
meth public abstract int getEndPosition()
meth public abstract int getStartPosition()
meth public abstract java.lang.Object[] getParameters()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getKey()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.csl.api.Severity getSeverity()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject getFile()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface static org.netbeans.modules.csl.api.Error$Badging
 outer org.netbeans.modules.csl.api.Error
intf org.netbeans.modules.csl.api.Error
meth public abstract boolean showExplorerBadge()

CLSS public abstract org.netbeans.modules.csl.spi.ParserResult
cons protected init(org.netbeans.modules.parsing.api.Snapshot)
meth public abstract java.util.List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics()
supr org.netbeans.modules.parsing.spi.Parser$Result

CLSS public abstract org.netbeans.modules.parsing.spi.Parser
cons public init()
innr public abstract static Result
innr public final static !enum CancelReason
meth public abstract org.netbeans.modules.parsing.spi.Parser$Result getResult(org.netbeans.modules.parsing.api.Task) throws org.netbeans.modules.parsing.spi.ParseException
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void parse(org.netbeans.modules.parsing.api.Snapshot,org.netbeans.modules.parsing.api.Task,org.netbeans.modules.parsing.spi.SourceModificationEvent) throws org.netbeans.modules.parsing.spi.ParseException
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public void cancel()
 anno 0 java.lang.Deprecated()
meth public void cancel(org.netbeans.modules.parsing.spi.Parser$CancelReason,org.netbeans.modules.parsing.spi.SourceModificationEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hcls MyAccessor

CLSS public abstract static org.netbeans.modules.parsing.spi.Parser$Result
 outer org.netbeans.modules.parsing.spi.Parser
cons protected init(org.netbeans.modules.parsing.api.Snapshot)
meth protected abstract void invalidate()
meth protected boolean processingFinished()
meth public org.netbeans.modules.parsing.api.Snapshot getSnapshot()
supr java.lang.Object
hfds snapshot

CLSS public org.netbeans.modules.php.editor.api.AbstractElementQuery
cons public init(org.netbeans.modules.php.editor.api.ElementQuery$QueryScope)
intf org.netbeans.modules.php.editor.api.ElementQuery
meth public !varargs final org.netbeans.modules.php.editor.api.elements.PhpElement getAnyLast(java.lang.Class[])
meth public final <%0 extends org.netbeans.modules.php.editor.api.elements.PhpElement> java.util.Set<{%%0}> getElements(java.lang.Class<{%%0}>)
meth public final <%0 extends org.netbeans.modules.php.editor.api.elements.PhpElement> java.util.Set<{%%0}> getElements(java.lang.Class<{%%0}>,org.netbeans.modules.php.editor.api.NameKind)
meth public final <%0 extends org.netbeans.modules.php.editor.api.elements.PhpElement> {%%0} getLast(java.lang.Class<{%%0}>)
meth public final <%0 extends org.netbeans.modules.php.editor.api.elements.TypeMemberElement> java.util.Set<{%%0}> getElements(java.lang.Class<{%%0}>,org.netbeans.modules.php.editor.api.NameKind$Exact,org.netbeans.modules.php.editor.api.NameKind)
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.ClassElement> getClasses()
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.ClassElement> getClasses(org.netbeans.modules.php.editor.api.NameKind)
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.ConstantElement> getConstants()
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.ConstantElement> getConstants(org.netbeans.modules.php.editor.api.NameKind)
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.FieldElement> getFields(org.netbeans.modules.php.editor.api.NameKind$Exact,org.netbeans.modules.php.editor.api.NameKind)
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.FieldElement> getFields(org.netbeans.modules.php.editor.api.NameKind)
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.FunctionElement> getFunctions()
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.FunctionElement> getFunctions(org.netbeans.modules.php.editor.api.NameKind)
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.InterfaceElement> getInterfaces()
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.InterfaceElement> getInterfaces(org.netbeans.modules.php.editor.api.NameKind)
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getConstructors(org.netbeans.modules.php.editor.api.NameKind)
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getMethods(org.netbeans.modules.php.editor.api.NameKind$Exact,org.netbeans.modules.php.editor.api.NameKind)
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getMethods(org.netbeans.modules.php.editor.api.NameKind)
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.NamespaceElement> getNamespaces(org.netbeans.modules.php.editor.api.NameKind)
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeConstantElement> getTypeConstants(org.netbeans.modules.php.editor.api.NameKind$Exact,org.netbeans.modules.php.editor.api.NameKind)
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeConstantElement> getTypeConstants(org.netbeans.modules.php.editor.api.NameKind)
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeElement> getTypes(org.netbeans.modules.php.editor.api.NameKind)
meth public final java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeMemberElement> getTypeMembers(org.netbeans.modules.php.editor.api.NameKind$Exact,org.netbeans.modules.php.editor.api.NameKind)
meth public final void addElement(org.netbeans.modules.php.editor.api.elements.PhpElement)
meth public final void addElements(java.util.Set<? extends org.netbeans.modules.php.editor.api.elements.PhpElement>)
meth public java.util.LinkedList<org.netbeans.modules.php.editor.api.elements.PhpElement> getElements()
meth public java.util.Set<org.netbeans.modules.php.editor.api.elements.EnumCaseElement> getEnumCases(org.netbeans.modules.php.editor.api.NameKind$Exact,org.netbeans.modules.php.editor.api.NameKind)
meth public java.util.Set<org.netbeans.modules.php.editor.api.elements.EnumCaseElement> getEnumCases(org.netbeans.modules.php.editor.api.NameKind)
meth public java.util.Set<org.netbeans.modules.php.editor.api.elements.VariableElement> getTopLevelVariables(org.netbeans.modules.php.editor.api.NameKind)
meth public org.netbeans.modules.php.editor.api.ElementQuery$QueryScope getQueryScope()
supr java.lang.Object
hfds elements,queryScope

CLSS public final org.netbeans.modules.php.editor.api.AliasedName
cons public init(java.lang.String,org.netbeans.modules.php.editor.api.QualifiedName)
meth public java.lang.String getAliasName()
meth public org.netbeans.modules.php.editor.api.QualifiedName getRealName()
supr java.lang.Object
hfds aliasName,namespaceName

CLSS public abstract interface org.netbeans.modules.php.editor.api.ElementQuery
innr public abstract interface static File
innr public abstract interface static Index
innr public final static !enum QueryScope
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.ClassElement> getClasses()
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.ClassElement> getClasses(org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.ConstantElement> getConstants()
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.ConstantElement> getConstants(org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.EnumCaseElement> getEnumCases(org.netbeans.modules.php.editor.api.NameKind$Exact,org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.EnumCaseElement> getEnumCases(org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.FieldElement> getFields(org.netbeans.modules.php.editor.api.NameKind$Exact,org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.FieldElement> getFields(org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.FunctionElement> getFunctions()
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.FunctionElement> getFunctions(org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.InterfaceElement> getInterfaces()
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.InterfaceElement> getInterfaces(org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getConstructors(org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getMethods(org.netbeans.modules.php.editor.api.NameKind$Exact,org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getMethods(org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.NamespaceElement> getNamespaces(org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeConstantElement> getTypeConstants(org.netbeans.modules.php.editor.api.NameKind$Exact,org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeConstantElement> getTypeConstants(org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeElement> getTypes(org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeMemberElement> getTypeMembers(org.netbeans.modules.php.editor.api.NameKind$Exact,org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.VariableElement> getTopLevelVariables(org.netbeans.modules.php.editor.api.NameKind)
meth public abstract org.netbeans.modules.php.editor.api.ElementQuery$QueryScope getQueryScope()

CLSS public abstract interface static org.netbeans.modules.php.editor.api.ElementQuery$File
 outer org.netbeans.modules.php.editor.api.ElementQuery
intf org.netbeans.modules.php.editor.api.ElementQuery
meth public abstract java.net.URL getURL()
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.FieldElement> getDeclaredFields(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getDeclaredMethods(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeConstantElement> getDeclaredTypeConstants(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.VariableElement> getFunctionVariables(org.netbeans.modules.php.editor.api.elements.FunctionElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.VariableElement> getMethodVariables(org.netbeans.modules.php.editor.api.elements.MethodElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.VariableElement> getTopLevelVariables()
meth public abstract org.netbeans.modules.php.editor.parser.PHPParseResult getResult()
meth public abstract org.openide.filesystems.FileObject getFileObject()

CLSS public abstract interface static org.netbeans.modules.php.editor.api.ElementQuery$Index
 outer org.netbeans.modules.php.editor.api.ElementQuery
intf org.netbeans.modules.php.editor.api.ElementQuery
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.ClassElement> getAttributeClasses(org.netbeans.modules.php.editor.api.NameKind,java.util.Set<org.netbeans.modules.php.editor.api.AliasedName>,org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.ClassElement> getClasses(org.netbeans.modules.php.editor.api.NameKind,java.util.Set<org.netbeans.modules.php.editor.api.AliasedName>,org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.ClassElement> getDirectInheritedClasses(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.ClassElement> getInheritedClasses(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.ConstantElement> getConstants(org.netbeans.modules.php.editor.api.NameKind,java.util.Set<org.netbeans.modules.php.editor.api.AliasedName>,org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.EnumCaseElement> getAllEnumCases(org.netbeans.modules.php.editor.api.NameKind$Exact,org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.EnumCaseElement> getAllEnumCases(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.EnumCaseElement> getDeclaredEnumCases(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.EnumElement> getEnums(org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.EnumElement> getEnums(org.netbeans.modules.php.editor.api.NameKind,java.util.Set<org.netbeans.modules.php.editor.api.AliasedName>,org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.FieldElement> getAccessibleFields(org.netbeans.modules.php.editor.api.elements.TypeElement,org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.FieldElement> getAccessibleStaticFields(org.netbeans.modules.php.editor.api.elements.TypeElement,org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.FieldElement> getAlllFields(org.netbeans.modules.php.editor.api.NameKind$Exact,org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.FieldElement> getAlllFields(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.FieldElement> getDeclaredFields(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.FieldElement> getInheritedFields(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.FieldElement> getStaticInheritedFields(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.FunctionElement> getFunctions(org.netbeans.modules.php.editor.api.NameKind,java.util.Set<org.netbeans.modules.php.editor.api.AliasedName>,org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.InterfaceElement> getDirectInheritedInterfaces(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.InterfaceElement> getInheritedInterfaces(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.InterfaceElement> getInterfaces(org.netbeans.modules.php.editor.api.NameKind,java.util.Set<org.netbeans.modules.php.editor.api.AliasedName>,org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getAccessibleMagicMethods(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getAccessibleMethods(org.netbeans.modules.php.editor.api.elements.TypeElement,org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getAccessibleStaticMethods(org.netbeans.modules.php.editor.api.elements.TypeElement,org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getAllMethods(org.netbeans.modules.php.editor.api.NameKind$Exact,org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getAllMethods(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getAttributeClassConstructors(org.netbeans.modules.php.editor.api.NameKind,java.util.Set<org.netbeans.modules.php.editor.api.AliasedName>,org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getConstructors(org.netbeans.modules.php.editor.api.NameKind,java.util.Set<org.netbeans.modules.php.editor.api.AliasedName>,org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getConstructors(org.netbeans.modules.php.editor.api.elements.ClassElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getDeclaredConstructors(org.netbeans.modules.php.editor.api.elements.ClassElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getDeclaredMethods(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getInheritedMethods(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getStaticInheritedMethods(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.NamespaceElement> getNamespaces(org.netbeans.modules.php.editor.api.NameKind,java.util.Set<org.netbeans.modules.php.editor.api.AliasedName>,org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.PhpElement> getTopLevelElements(org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.PhpElement> getTopLevelElements(org.netbeans.modules.php.editor.api.NameKind,java.util.Set<org.netbeans.modules.php.editor.api.AliasedName>,org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TraitElement> getTraits(org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeConstantElement> getAccessibleMagicConstants(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeConstantElement> getAllTypeConstants(org.netbeans.modules.php.editor.api.NameKind$Exact,org.netbeans.modules.php.editor.api.NameKind)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeConstantElement> getAllTypeConstants(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeConstantElement> getDeclaredTypeConstants(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeConstantElement> getInheritedTypeConstants(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeElement> getDirectInheritedByTypes(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeElement> getDirectInheritedTypes(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeElement> getInheritedByTypes(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeElement> getInheritedTypes(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeElement> getTypes(org.netbeans.modules.php.editor.api.NameKind,java.util.Set<org.netbeans.modules.php.editor.api.AliasedName>,org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeMemberElement> getAccessibleMixinTypeMembers(org.netbeans.modules.php.editor.api.elements.TypeElement,org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeMemberElement> getAccessibleTypeMembers(org.netbeans.modules.php.editor.api.elements.TypeElement,org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeMemberElement> getAllTypeMembers(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeMemberElement> getDeclaredTypeMembers(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeMemberElement> getInheritedTypeMembers(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract java.util.Set<org.openide.filesystems.FileObject> getLocationsForIdentifiers(java.lang.String)
meth public abstract org.netbeans.modules.php.editor.api.elements.TreeElement<org.netbeans.modules.php.editor.api.elements.TypeElement> getInheritedByTypesAsTree(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract org.netbeans.modules.php.editor.api.elements.TreeElement<org.netbeans.modules.php.editor.api.elements.TypeElement> getInheritedByTypesAsTree(org.netbeans.modules.php.editor.api.elements.TypeElement,java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeElement>)
meth public abstract org.netbeans.modules.php.editor.api.elements.TreeElement<org.netbeans.modules.php.editor.api.elements.TypeElement> getInheritedTypesAsTree(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public abstract org.netbeans.modules.php.editor.api.elements.TreeElement<org.netbeans.modules.php.editor.api.elements.TypeElement> getInheritedTypesAsTree(org.netbeans.modules.php.editor.api.elements.TypeElement,java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeElement>)

CLSS public final static !enum org.netbeans.modules.php.editor.api.ElementQuery$QueryScope
 outer org.netbeans.modules.php.editor.api.ElementQuery
fld public final static org.netbeans.modules.php.editor.api.ElementQuery$QueryScope FILE_SCOPE
fld public final static org.netbeans.modules.php.editor.api.ElementQuery$QueryScope INDEX_SCOPE
fld public final static org.netbeans.modules.php.editor.api.ElementQuery$QueryScope VIRTUAL_SCOPE
meth public boolean isFileScope()
meth public boolean isIndexScope()
meth public boolean isVirtualScope()
meth public static org.netbeans.modules.php.editor.api.ElementQuery$QueryScope valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.api.ElementQuery$QueryScope[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.api.ElementQuery$QueryScope>

CLSS public final org.netbeans.modules.php.editor.api.ElementQueryFactory
meth public static org.netbeans.modules.php.editor.api.ElementQuery$File createFileQuery(org.netbeans.modules.php.editor.parser.PHPParseResult)
meth public static org.netbeans.modules.php.editor.api.ElementQuery$Index createIndexQuery(org.netbeans.modules.parsing.spi.indexing.support.QuerySupport)
meth public static org.netbeans.modules.php.editor.api.ElementQuery$Index getIndexQuery(org.netbeans.modules.csl.spi.ParserResult)
meth public static org.netbeans.modules.php.editor.api.ElementQuery$Index getIndexQuery(org.netbeans.modules.php.editor.parser.PHPParseResult)
supr java.lang.Object

CLSS public final org.netbeans.modules.php.editor.api.FileElementQuery
intf org.netbeans.modules.php.editor.api.ElementQuery$File
meth public java.net.URL getURL()
meth public java.util.Set<org.netbeans.modules.php.editor.api.elements.ConstantElement> createConstant(org.netbeans.modules.php.editor.api.elements.NamespaceElement,org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration)
meth public java.util.Set<org.netbeans.modules.php.editor.api.elements.FieldElement> create(org.netbeans.modules.php.editor.api.elements.TypeElement,org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration)
meth public java.util.Set<org.netbeans.modules.php.editor.api.elements.FieldElement> getDeclaredFields(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public java.util.Set<org.netbeans.modules.php.editor.api.elements.MethodElement> getDeclaredMethods(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeConstantElement> createTypeConstant(org.netbeans.modules.php.editor.api.elements.TypeElement,org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration)
meth public java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeConstantElement> getDeclaredTypeConstants(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public java.util.Set<org.netbeans.modules.php.editor.api.elements.VariableElement> getFunctionVariables(org.netbeans.modules.php.editor.api.elements.FunctionElement)
meth public java.util.Set<org.netbeans.modules.php.editor.api.elements.VariableElement> getMethodVariables(org.netbeans.modules.php.editor.api.elements.MethodElement)
meth public java.util.Set<org.netbeans.modules.php.editor.api.elements.VariableElement> getTopLevelVariables()
meth public org.netbeans.modules.php.editor.api.elements.ClassElement create(org.netbeans.modules.php.editor.api.elements.NamespaceElement,org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration)
meth public org.netbeans.modules.php.editor.api.elements.EnumCaseElement createEnumCase(org.netbeans.modules.php.editor.api.elements.TypeElement,org.netbeans.modules.php.editor.parser.astnodes.CaseDeclaration)
meth public org.netbeans.modules.php.editor.api.elements.EnumElement create(org.netbeans.modules.php.editor.api.elements.NamespaceElement,org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration)
meth public org.netbeans.modules.php.editor.api.elements.FieldElement create(org.netbeans.modules.php.editor.api.elements.TypeElement,org.netbeans.modules.php.editor.parser.astnodes.FieldAccess)
meth public org.netbeans.modules.php.editor.api.elements.FunctionElement create(org.netbeans.modules.php.editor.api.elements.NamespaceElement,org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration)
meth public org.netbeans.modules.php.editor.api.elements.InterfaceElement create(org.netbeans.modules.php.editor.api.elements.NamespaceElement,org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration)
meth public org.netbeans.modules.php.editor.api.elements.MethodElement create(org.netbeans.modules.php.editor.api.elements.TypeElement,org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration)
meth public org.netbeans.modules.php.editor.api.elements.NamespaceElement create(org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration)
meth public org.netbeans.modules.php.editor.api.elements.TraitElement create(org.netbeans.modules.php.editor.api.elements.NamespaceElement,org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration)
meth public org.netbeans.modules.php.editor.api.elements.TypeElement create(org.netbeans.modules.php.editor.api.elements.NamespaceElement,org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration)
meth public org.netbeans.modules.php.editor.api.elements.VariableElement createFunctionVariable(org.netbeans.modules.php.editor.api.elements.FunctionElement,org.netbeans.modules.php.editor.parser.astnodes.Variable)
meth public org.netbeans.modules.php.editor.api.elements.VariableElement createMethodVariable(org.netbeans.modules.php.editor.api.elements.MethodElement,org.netbeans.modules.php.editor.parser.astnodes.Variable)
meth public org.netbeans.modules.php.editor.api.elements.VariableElement createTopLevelVariable(org.netbeans.modules.php.editor.parser.astnodes.Variable)
meth public org.netbeans.modules.php.editor.parser.PHPParseResult getResult()
meth public org.openide.filesystems.FileObject getFileObject()
meth public static org.netbeans.modules.php.editor.api.FileElementQuery getInstance(org.netbeans.modules.php.editor.parser.PHPParseResult)
supr org.netbeans.modules.php.editor.api.AbstractElementQuery
hfds fields,fileObject,result,url,varMap
hcls VariableTypeResolver

CLSS public org.netbeans.modules.php.editor.api.NameKind
innr public final static CaseInsensitivePrefix
innr public final static Empty
innr public final static Exact
innr public final static Prefix
meth public boolean isCaseInsensitivePrefix()
meth public boolean isEmpty()
meth public boolean isExact()
meth public boolean isPrefix()
meth public boolean matchesName(org.netbeans.modules.php.editor.api.PhpElementKind,java.lang.String)
meth public boolean matchesName(org.netbeans.modules.php.editor.api.PhpElementKind,org.netbeans.modules.php.editor.api.QualifiedName)
meth public boolean matchesName(org.netbeans.modules.php.editor.api.elements.PhpElement)
meth public java.lang.String getQueryName()
meth public org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind getQueryKind()
meth public org.netbeans.modules.php.editor.api.QualifiedName getQuery()
meth public static boolean isCaseSensitive(org.netbeans.modules.php.editor.api.PhpElementKind)
meth public static boolean isDollared(org.netbeans.modules.php.editor.api.PhpElementKind)
meth public static org.netbeans.modules.php.editor.api.NameKind create(java.lang.String,org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind)
meth public static org.netbeans.modules.php.editor.api.NameKind create(org.netbeans.modules.php.editor.api.QualifiedName,org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind)
meth public static org.netbeans.modules.php.editor.api.NameKind$CaseInsensitivePrefix caseInsensitivePrefix(java.lang.String)
meth public static org.netbeans.modules.php.editor.api.NameKind$CaseInsensitivePrefix caseInsensitivePrefix(org.netbeans.modules.php.editor.api.QualifiedName)
meth public static org.netbeans.modules.php.editor.api.NameKind$Empty empty()
meth public static org.netbeans.modules.php.editor.api.NameKind$Exact exact(java.lang.String)
meth public static org.netbeans.modules.php.editor.api.NameKind$Exact exact(org.netbeans.modules.php.editor.api.QualifiedName)
meth public static org.netbeans.modules.php.editor.api.NameKind$Exact forElement(org.netbeans.modules.php.editor.api.elements.PhpElement)
meth public static org.netbeans.modules.php.editor.api.NameKind$Prefix prefix(java.lang.String)
meth public static org.netbeans.modules.php.editor.api.NameKind$Prefix prefix(org.netbeans.modules.php.editor.api.QualifiedName)
supr java.lang.Object
hfds query,queryKind

CLSS public final static org.netbeans.modules.php.editor.api.NameKind$CaseInsensitivePrefix
 outer org.netbeans.modules.php.editor.api.NameKind
supr org.netbeans.modules.php.editor.api.NameKind

CLSS public final static org.netbeans.modules.php.editor.api.NameKind$Empty
 outer org.netbeans.modules.php.editor.api.NameKind
supr org.netbeans.modules.php.editor.api.NameKind

CLSS public final static org.netbeans.modules.php.editor.api.NameKind$Exact
 outer org.netbeans.modules.php.editor.api.NameKind
supr org.netbeans.modules.php.editor.api.NameKind

CLSS public final static org.netbeans.modules.php.editor.api.NameKind$Prefix
 outer org.netbeans.modules.php.editor.api.NameKind
supr org.netbeans.modules.php.editor.api.NameKind

CLSS public final !enum org.netbeans.modules.php.editor.api.PhpElementKind
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind CLASS
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind CONSTANT
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind CONSTRUCTOR
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind EMPTY
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind ENUM
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind ENUM_CASE
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind FIELD
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind FUNCTION
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind GROUP_USE_STATEMENT
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind IFACE
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind INCLUDE
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind INDEX
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind METHOD
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind NAMESPACE_DECLARATION
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind PROGRAM
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind TRAIT
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind TRAIT_CONFLICT_RESOLUTION
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind TRAIT_METHOD_ALIAS
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind TYPE_CONSTANT
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind USE_ALIAS
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind USE_STATEMENT
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind VARIABLE
meth public final org.netbeans.modules.csl.api.ElementKind getElementKind()
meth public static org.netbeans.modules.php.editor.api.PhpElementKind valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.api.PhpElementKind[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.api.PhpElementKind>

CLSS public final org.netbeans.modules.php.editor.api.PhpModifiers
fld public final static int ALL_FLAGS = -1
fld public final static int NO_FLAGS = 0
fld public final static java.lang.String ABSTRACT_MODIFIER = "abstract"
fld public final static java.lang.String FINAL_MODIFIER = "final"
fld public final static java.lang.String READONLY_MODIFIER = "readonly"
fld public final static java.lang.String VISIBILITY_PRIVATE = "private"
fld public final static java.lang.String VISIBILITY_PROTECTED = "protected"
fld public final static java.lang.String VISIBILITY_PUBLIC = "public"
fld public final static java.lang.String VISIBILITY_VAR = "var"
meth public !varargs static org.netbeans.modules.php.editor.api.PhpModifiers fromBitMask(int[])
meth public boolean equals(java.lang.Object)
meth public boolean isAbstract()
meth public boolean isFinal()
meth public boolean isImplicitPublic()
meth public boolean isPrivate()
meth public boolean isProtected()
meth public boolean isPublic()
meth public boolean isReadonly()
meth public boolean isStatic()
meth public int hashCode()
meth public int toFlags()
meth public java.lang.String toString()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> toModifiers()
meth public org.netbeans.modules.php.editor.api.PhpModifiers setAbstract()
meth public org.netbeans.modules.php.editor.api.PhpModifiers setFinal()
meth public org.netbeans.modules.php.editor.api.PhpModifiers setImplicitPublic()
meth public org.netbeans.modules.php.editor.api.PhpModifiers setPrivate()
meth public org.netbeans.modules.php.editor.api.PhpModifiers setProtected()
meth public org.netbeans.modules.php.editor.api.PhpModifiers setPublic()
meth public org.netbeans.modules.php.editor.api.PhpModifiers setReadonly()
meth public org.netbeans.modules.php.editor.api.PhpModifiers setStatic()
meth public static org.netbeans.modules.php.editor.api.PhpModifiers noModifiers()
supr org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration$Modifier
hfds EMPTY,mod

CLSS public final org.netbeans.modules.php.editor.api.QualifiedName
meth public boolean equals(java.lang.Object)
meth public boolean isDefaultNamespace()
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String getNamespaceName()
meth public java.lang.String toString()
meth public java.lang.String toString(int)
meth public java.util.LinkedList<java.lang.String> getSegments()
meth public org.netbeans.modules.php.editor.api.QualifiedName append(java.lang.String)
meth public org.netbeans.modules.php.editor.api.QualifiedName append(org.netbeans.modules.php.editor.api.QualifiedName)
meth public org.netbeans.modules.php.editor.api.QualifiedName toFullyQualified()
meth public org.netbeans.modules.php.editor.api.QualifiedName toFullyQualified(org.netbeans.modules.php.editor.api.QualifiedName)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.php.editor.api.QualifiedName toFullyQualified(org.netbeans.modules.php.editor.model.NamespaceScope)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.php.editor.api.QualifiedName toName()
meth public org.netbeans.modules.php.editor.api.QualifiedName toNamespaceName()
meth public org.netbeans.modules.php.editor.api.QualifiedName toNamespaceName(boolean)
meth public org.netbeans.modules.php.editor.api.QualifiedName toNotFullyQualified()
meth public org.netbeans.modules.php.editor.api.QualifiedNameKind getKind()
meth public static java.util.List<org.netbeans.modules.php.editor.api.QualifiedName> create(org.netbeans.modules.php.editor.parser.astnodes.IntersectionType)
meth public static java.util.List<org.netbeans.modules.php.editor.api.QualifiedName> create(org.netbeans.modules.php.editor.parser.astnodes.UnionType)
meth public static org.netbeans.modules.php.editor.api.QualifiedName create(boolean,java.util.List<java.lang.String>)
meth public static org.netbeans.modules.php.editor.api.QualifiedName create(java.lang.String)
meth public static org.netbeans.modules.php.editor.api.QualifiedName create(org.netbeans.modules.php.editor.model.NamespaceScope)
meth public static org.netbeans.modules.php.editor.api.QualifiedName create(org.netbeans.modules.php.editor.parser.astnodes.Expression)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.editor.api.QualifiedName create(org.netbeans.modules.php.editor.parser.astnodes.NamespaceName)
meth public static org.netbeans.modules.php.editor.api.QualifiedName createForDefaultNamespaceName()
meth public static org.netbeans.modules.php.editor.api.QualifiedName createFullyQualified(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.php.editor.api.QualifiedName createUnqualifiedName(java.lang.String)
meth public static org.netbeans.modules.php.editor.api.QualifiedName createUnqualifiedName(org.netbeans.modules.php.editor.parser.astnodes.Identifier)
meth public static org.netbeans.modules.php.editor.api.QualifiedName createUnqualifiedNameInClassContext(java.lang.String,org.netbeans.modules.php.editor.model.ClassScope)
meth public static org.netbeans.modules.php.editor.api.QualifiedName createUnqualifiedNameInClassContext(org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.model.ClassScope)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.editor.api.QualifiedName createUnqualifiedNameInClassContext(org.netbeans.modules.php.editor.parser.astnodes.Identifier,org.netbeans.modules.php.editor.model.ClassScope)
meth public static org.netbeans.modules.php.editor.api.QualifiedName getPrefix(org.netbeans.modules.php.editor.api.QualifiedName,org.netbeans.modules.php.editor.api.QualifiedName,boolean)
meth public static org.netbeans.modules.php.editor.api.QualifiedName getSuffix(org.netbeans.modules.php.editor.api.QualifiedName,org.netbeans.modules.php.editor.api.QualifiedName,boolean)
supr java.lang.Object
hfds kind,segments

CLSS public final !enum org.netbeans.modules.php.editor.api.QualifiedNameKind
fld public final static org.netbeans.modules.php.editor.api.QualifiedNameKind FULLYQUALIFIED
fld public final static org.netbeans.modules.php.editor.api.QualifiedNameKind QUALIFIED
fld public final static org.netbeans.modules.php.editor.api.QualifiedNameKind UNQUALIFIED
meth public boolean isFullyQualified()
meth public boolean isQualified()
meth public boolean isUnqualified()
meth public static org.netbeans.modules.php.editor.api.QualifiedNameKind resolveKind(java.lang.String)
meth public static org.netbeans.modules.php.editor.api.QualifiedNameKind resolveKind(java.util.List<java.lang.String>)
meth public static org.netbeans.modules.php.editor.api.QualifiedNameKind resolveKind(org.netbeans.modules.php.editor.parser.astnodes.Identifier)
meth public static org.netbeans.modules.php.editor.api.QualifiedNameKind resolveKind(org.netbeans.modules.php.editor.parser.astnodes.NamespaceName)
meth public static org.netbeans.modules.php.editor.api.QualifiedNameKind valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.api.QualifiedNameKind[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.api.QualifiedNameKind>

CLSS public final org.netbeans.modules.php.editor.api.QuerySupportFactory
meth public static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport get(java.util.Collection<org.openide.filesystems.FileObject>)
meth public static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport get(org.netbeans.modules.csl.spi.ParserResult)
meth public static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport get(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport getDependent(org.openide.filesystems.FileObject)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.php.editor.api.elements.AbstractElementHandle
cons public init()
intf org.netbeans.modules.csl.api.ElementHandle
meth public boolean signatureEquals(org.netbeans.modules.csl.api.ElementHandle)
meth public java.lang.String getIn()
meth public java.lang.String getMimeType()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public org.netbeans.modules.csl.api.OffsetRange getOffsetRange(org.netbeans.modules.csl.spi.ParserResult)
meth public org.openide.filesystems.FileObject getFileObject()
supr java.lang.Object

CLSS public final org.netbeans.modules.php.editor.api.elements.AliasedClass
cons public init(org.netbeans.modules.php.editor.api.AliasedName,org.netbeans.modules.php.editor.api.elements.ClassElement)
intf org.netbeans.modules.php.editor.api.elements.ClassElement
meth public boolean isAbstract()
meth public boolean isAnonymous()
meth public boolean isAttribute()
meth public boolean isFinal()
meth public boolean isReadonly()
meth public java.util.Collection<org.netbeans.modules.php.editor.api.QualifiedName> getFQMixinClassNames()
meth public java.util.Collection<org.netbeans.modules.php.editor.api.QualifiedName> getPossibleFQSuperClassNames()
meth public java.util.Collection<org.netbeans.modules.php.editor.api.QualifiedName> getUsedTraits()
meth public org.netbeans.modules.php.editor.api.QualifiedName getSuperClassName()
supr org.netbeans.modules.php.editor.api.elements.AliasedType

CLSS public org.netbeans.modules.php.editor.api.elements.AliasedConstant
cons public init(org.netbeans.modules.php.editor.api.AliasedName,org.netbeans.modules.php.editor.api.elements.ConstantElement)
intf org.netbeans.modules.php.editor.api.elements.ConstantElement
meth protected final org.netbeans.modules.php.editor.api.elements.ConstantElement getRealConstant()
meth public java.lang.String getValue()
supr org.netbeans.modules.php.editor.api.elements.AliasedElement

CLSS public org.netbeans.modules.php.editor.api.elements.AliasedElement
cons public init(org.netbeans.modules.php.editor.api.AliasedName,org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement)
fld protected final org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement element
innr public final static !enum Trait
intf org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement
meth public final boolean isAliased()
meth public final boolean isDeprecated()
meth public final boolean isNameAliased()
meth public final boolean isNamespaceNameAliased()
meth public final boolean isPlatform()
meth public final boolean signatureEquals(org.netbeans.modules.csl.api.ElementHandle)
meth public final int getFlags()
meth public final int getOffset()
meth public final java.lang.String getFilenameUrl()
meth public final java.lang.String getIn()
meth public final java.lang.String getMimeType()
meth public final java.lang.String getName()
meth public final java.lang.String getName(org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait)
meth public final java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public final org.netbeans.modules.csl.api.ElementKind getKind()
meth public final org.netbeans.modules.csl.api.OffsetRange getOffsetRange(org.netbeans.modules.csl.spi.ParserResult)
meth public final org.netbeans.modules.php.editor.api.ElementQuery getElementQuery()
meth public final org.netbeans.modules.php.editor.api.PhpElementKind getPhpElementKind()
meth public final org.netbeans.modules.php.editor.api.PhpModifiers getPhpModifiers()
meth public final org.netbeans.modules.php.editor.api.QualifiedName getFullyQualifiedName()
meth public final org.netbeans.modules.php.editor.api.QualifiedName getFullyQualifiedName(org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait)
meth public final org.netbeans.modules.php.editor.api.QualifiedName getNamespaceName()
meth public final org.netbeans.modules.php.editor.api.QualifiedName getNamespaceName(org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait)
meth public final org.openide.filesystems.FileObject getFileObject()
meth public org.netbeans.modules.php.editor.api.AliasedName getAliasedName()
meth public org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait getTrait()
meth public void setTrait(org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait)
supr java.lang.Object
hfds aliasFqn,aliasedName,trait

CLSS public final static !enum org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait
 outer org.netbeans.modules.php.editor.api.elements.AliasedElement
fld public final static org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait ALIAS
fld public final static org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait ELEMENT
meth public static org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.api.elements.AliasedElement$Trait>

CLSS public org.netbeans.modules.php.editor.api.elements.AliasedEnum
cons public init(org.netbeans.modules.php.editor.api.AliasedName,org.netbeans.modules.php.editor.api.elements.EnumElement)
intf org.netbeans.modules.php.editor.api.elements.EnumElement
meth public java.util.Collection<org.netbeans.modules.php.editor.api.QualifiedName> getUsedTraits()
meth public org.netbeans.modules.php.editor.api.QualifiedName getBackingType()
supr org.netbeans.modules.php.editor.api.elements.AliasedType

CLSS public org.netbeans.modules.php.editor.api.elements.AliasedFunction
cons public init(org.netbeans.modules.php.editor.api.AliasedName,org.netbeans.modules.php.editor.api.elements.FunctionElement)
intf org.netbeans.modules.php.editor.api.elements.FunctionElement
meth protected final org.netbeans.modules.php.editor.api.elements.FunctionElement getRealFunction()
meth public boolean isAnonymous()
meth public boolean isReturnIntersectionType()
meth public boolean isReturnUnionType()
meth public java.lang.String asString(org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs)
meth public java.lang.String asString(org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs,org.netbeans.modules.php.editor.api.elements.TypeNameResolver)
meth public java.lang.String asString(org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs,org.netbeans.modules.php.editor.api.elements.TypeNameResolver,org.netbeans.modules.php.api.PhpVersion)
meth public java.lang.String getDeclaredReturnType()
meth public java.util.Collection<org.netbeans.modules.php.editor.api.elements.TypeResolver> getReturnTypes()
meth public java.util.List<org.netbeans.modules.php.editor.api.elements.ParameterElement> getParameters()
supr org.netbeans.modules.php.editor.api.elements.AliasedElement

CLSS public final org.netbeans.modules.php.editor.api.elements.AliasedInterface
cons public init(org.netbeans.modules.php.editor.api.AliasedName,org.netbeans.modules.php.editor.api.elements.InterfaceElement)
intf org.netbeans.modules.php.editor.api.elements.InterfaceElement
supr org.netbeans.modules.php.editor.api.elements.AliasedType

CLSS public org.netbeans.modules.php.editor.api.elements.AliasedNamespace
cons public init(org.netbeans.modules.php.editor.api.AliasedName,org.netbeans.modules.php.editor.api.elements.NamespaceElement)
intf org.netbeans.modules.php.editor.api.elements.NamespaceElement
supr org.netbeans.modules.php.editor.api.elements.AliasedElement

CLSS public org.netbeans.modules.php.editor.api.elements.AliasedTrait
cons public init(org.netbeans.modules.php.editor.api.AliasedName,org.netbeans.modules.php.editor.api.elements.TraitElement)
intf org.netbeans.modules.php.editor.api.elements.TraitElement
meth public java.util.Collection<org.netbeans.modules.php.editor.api.QualifiedName> getUsedTraits()
supr org.netbeans.modules.php.editor.api.elements.AliasedType

CLSS public org.netbeans.modules.php.editor.api.elements.AliasedType
cons protected init(org.netbeans.modules.php.editor.api.AliasedName,org.netbeans.modules.php.editor.api.elements.TypeElement)
intf org.netbeans.modules.php.editor.api.elements.TypeElement
meth protected final org.netbeans.modules.php.editor.api.elements.TypeElement getRealType()
meth public boolean isTraited()
meth public final boolean isClass()
meth public final boolean isEnum()
meth public final boolean isInterface()
meth public final boolean isTrait()
meth public final java.lang.String asString(org.netbeans.modules.php.editor.api.elements.TypeElement$PrintAs)
meth public final java.util.Set<org.netbeans.modules.php.editor.api.QualifiedName> getSuperInterfaces()
meth public java.util.Collection<org.netbeans.modules.php.editor.api.QualifiedName> getFQSuperInterfaceNames()
supr org.netbeans.modules.php.editor.api.elements.AliasedElement

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.BaseFunctionElement
innr public final static !enum PrintAs
intf org.netbeans.modules.php.editor.api.elements.PhpElement
meth public abstract boolean isReturnIntersectionType()
meth public abstract boolean isReturnUnionType()
meth public abstract java.lang.String asString(org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs)
meth public abstract java.lang.String asString(org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs,org.netbeans.modules.php.editor.api.elements.TypeNameResolver)
meth public abstract java.lang.String asString(org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs,org.netbeans.modules.php.editor.api.elements.TypeNameResolver,org.netbeans.modules.php.api.PhpVersion)
meth public abstract java.lang.String getDeclaredReturnType()
meth public abstract java.util.Collection<org.netbeans.modules.php.editor.api.elements.TypeResolver> getReturnTypes()
meth public abstract java.util.List<org.netbeans.modules.php.editor.api.elements.ParameterElement> getParameters()

CLSS public final static !enum org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs
 outer org.netbeans.modules.php.editor.api.elements.BaseFunctionElement
fld public final static org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs DeclarationWithEmptyBody
fld public final static org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs DeclarationWithParentCallInBody
fld public final static org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs DeclarationWithoutBody
fld public final static org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs NameAndParamsDeclaration
fld public final static org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs NameAndParamsInvocation
fld public final static org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs ReturnSemiTypes
fld public final static org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs ReturnTypes
meth public static org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.api.elements.BaseFunctionElement$PrintAs>

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.ClassElement
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind KIND
intf org.netbeans.modules.php.editor.api.elements.TraitedElement
meth public abstract boolean isAbstract()
meth public abstract boolean isAnonymous()
meth public abstract boolean isAttribute()
meth public abstract boolean isFinal()
meth public abstract boolean isReadonly()
meth public abstract java.util.Collection<org.netbeans.modules.php.editor.api.QualifiedName> getFQMixinClassNames()
meth public abstract java.util.Collection<org.netbeans.modules.php.editor.api.QualifiedName> getPossibleFQSuperClassNames()
meth public abstract org.netbeans.modules.php.editor.api.QualifiedName getSuperClassName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.ConstantElement
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind KIND
intf org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement
meth public abstract java.lang.String getValue()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract org.netbeans.modules.php.editor.api.elements.ElementFilter
cons public init()
meth public !varargs static org.netbeans.modules.php.editor.api.elements.ElementFilter allOf(org.netbeans.modules.php.editor.api.elements.ElementFilter[])
meth public !varargs static org.netbeans.modules.php.editor.api.elements.ElementFilter anyOf(org.netbeans.modules.php.editor.api.elements.ElementFilter[])
meth public !varargs static org.netbeans.modules.php.editor.api.elements.ElementFilter forFiles(org.openide.filesystems.FileObject[])
meth public <%0 extends org.netbeans.modules.php.editor.api.elements.PhpElement> java.util.Set<{%%0}> filter(java.util.Set<{%%0}>)
meth public <%0 extends org.netbeans.modules.php.editor.api.elements.PhpElement> java.util.Set<{%%0}> filter({%%0})
meth public <%0 extends org.netbeans.modules.php.editor.api.elements.PhpElement> java.util.Set<{%%0}> prefer(java.util.Set<{%%0}>)
meth public <%0 extends org.netbeans.modules.php.editor.api.elements.PhpElement> java.util.Set<{%%0}> reverseFilter(java.util.Set<{%%0}>)
meth public abstract boolean isAccepted(org.netbeans.modules.php.editor.api.elements.PhpElement)
meth public static <%0 extends org.netbeans.modules.php.editor.api.elements.PhpElement> org.netbeans.modules.php.editor.api.elements.ElementFilter forExcludedElements(java.util.Collection<{%%0}>)
meth public static <%0 extends org.netbeans.modules.php.editor.api.elements.PhpElement> org.netbeans.modules.php.editor.api.elements.ElementFilter forInstanceOf(java.lang.Class<{%%0}>)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter allOf(java.util.Collection<org.netbeans.modules.php.editor.api.elements.ElementFilter>)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter anyOf(java.util.Collection<org.netbeans.modules.php.editor.api.elements.ElementFilter>)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forAllOfFlags(int)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forAnyOfFlags(int)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forConstructor()
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forDeprecated(boolean)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forEqualTypes(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forExcludedNames(java.util.Collection<java.lang.String>,org.netbeans.modules.php.editor.api.PhpElementKind)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forIncludedNames(java.util.Collection<java.lang.String>,org.netbeans.modules.php.editor.api.PhpElementKind)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forKind(org.netbeans.modules.php.editor.api.PhpElementKind)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forMembersOfClass()
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forMembersOfInterface()
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forMembersOfType(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forMembersOfTypeName(org.netbeans.modules.php.editor.api.NameKind)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forMembersOfTypeName(org.netbeans.modules.php.editor.api.elements.TypeElement)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forMembersOfTypes(java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeElement>)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forName(org.netbeans.modules.php.editor.api.NameKind)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forOffset(int)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forPrivateModifiers(boolean)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forPublicModifiers(boolean)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forStaticModifiers(boolean)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forSuperClassName(org.netbeans.modules.php.editor.api.QualifiedName)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forSuperInterfaceName(org.netbeans.modules.php.editor.api.QualifiedName)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forSuperInterfaceNames(java.util.Set<org.netbeans.modules.php.editor.api.QualifiedName>)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forTypesFromNamespace(org.netbeans.modules.php.editor.api.QualifiedName)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forTypesFromNamespaces(java.util.Set<org.netbeans.modules.php.editor.api.QualifiedName>)
meth public static org.netbeans.modules.php.editor.api.elements.ElementFilter forVirtualExtensions()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.php.editor.api.elements.ElementTransformation<%0 extends org.netbeans.modules.php.editor.api.elements.PhpElement>
cons public init()
meth public abstract {org.netbeans.modules.php.editor.api.elements.ElementTransformation%0} transform(org.netbeans.modules.php.editor.api.elements.PhpElement)
meth public final <%0 extends org.netbeans.modules.php.editor.api.elements.PhpElement> java.util.Set<{org.netbeans.modules.php.editor.api.elements.ElementTransformation%0}> transform(java.util.Set<{%%0}>)
meth public static org.netbeans.modules.php.editor.api.elements.ElementTransformation<org.netbeans.modules.php.editor.api.elements.TypeElement> toMemberTypes()
meth public static org.netbeans.modules.php.editor.api.elements.ElementTransformation<org.netbeans.modules.php.editor.api.elements.VariableElement> fieldsToVariables()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.EnumCaseElement
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind KIND
intf org.netbeans.modules.php.editor.api.elements.TypeMemberElement
meth public abstract boolean isBacked()
meth public abstract java.lang.String getValue()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.EnumElement
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind KIND
intf org.netbeans.modules.php.editor.api.elements.TraitedElement
meth public abstract org.netbeans.modules.php.editor.api.QualifiedName getBackingType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.FieldElement
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind KIND
intf org.netbeans.modules.php.editor.api.elements.TypeMemberElement
intf org.netbeans.modules.php.editor.api.elements.TypedInstanceElement
meth public abstract boolean isAnnotation()
meth public abstract boolean isIntersectionType()
meth public abstract boolean isUnionType()
meth public abstract java.lang.String getDeclaredType()
meth public abstract java.lang.String getName(boolean)

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement
intf org.netbeans.modules.php.editor.api.elements.PhpElement
meth public abstract boolean isAliased()
meth public abstract org.netbeans.modules.php.editor.api.QualifiedName getFullyQualifiedName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.php.editor.api.QualifiedName getNamespaceName()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.FunctionElement
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind KIND
intf org.netbeans.modules.php.editor.api.elements.BaseFunctionElement
intf org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement
meth public abstract boolean isAnonymous()

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.InterfaceElement
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind KIND
intf org.netbeans.modules.php.editor.api.elements.TypeElement

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.MethodElement
fld public final static java.lang.String CONSTRUCTOR_NAME = "__construct"
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind KIND
intf org.netbeans.modules.php.editor.api.elements.BaseFunctionElement
intf org.netbeans.modules.php.editor.api.elements.TypeMemberElement
meth public abstract boolean isConstructor()
meth public abstract boolean isMagic()

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.NamespaceElement
fld public final static java.lang.String DEFAULT_NAMESPACE_NAME = ""
fld public final static java.lang.String NAMESPACE_SEPARATOR = "\u005c"
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind KIND
intf org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.ParameterElement
innr public final static !enum OutputType
meth public abstract boolean hasDeclaredType()
meth public abstract boolean isIntersectionType()
meth public abstract boolean isMandatory()
meth public abstract boolean isReference()
meth public abstract boolean isUnionType()
meth public abstract boolean isVariadic()
meth public abstract int getModifier()
meth public abstract int getOffset()
meth public abstract java.lang.String asString(org.netbeans.modules.php.editor.api.elements.ParameterElement$OutputType)
meth public abstract java.lang.String asString(org.netbeans.modules.php.editor.api.elements.ParameterElement$OutputType,org.netbeans.modules.php.editor.api.elements.TypeNameResolver)
meth public abstract java.lang.String asString(org.netbeans.modules.php.editor.api.elements.ParameterElement$OutputType,org.netbeans.modules.php.editor.api.elements.TypeNameResolver,org.netbeans.modules.php.api.PhpVersion)
meth public abstract java.lang.String getDeclaredType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getDefaultValue()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getPhpdocType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeResolver> getTypes()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getOffsetRange()

CLSS public final static !enum org.netbeans.modules.php.editor.api.elements.ParameterElement$OutputType
 outer org.netbeans.modules.php.editor.api.elements.ParameterElement
fld public final static org.netbeans.modules.php.editor.api.elements.ParameterElement$OutputType COMPLETE_DECLARATION
fld public final static org.netbeans.modules.php.editor.api.elements.ParameterElement$OutputType COMPLETE_DECLARATION_WITH_MODIFIER
fld public final static org.netbeans.modules.php.editor.api.elements.ParameterElement$OutputType SHORTEN_DECLARATION
fld public final static org.netbeans.modules.php.editor.api.elements.ParameterElement$OutputType SHORTEN_DECLARATION_WITH_MODIFIER
fld public final static org.netbeans.modules.php.editor.api.elements.ParameterElement$OutputType SIMPLE_NAME
meth public static org.netbeans.modules.php.editor.api.elements.ParameterElement$OutputType valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.api.elements.ParameterElement$OutputType[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.api.elements.ParameterElement$OutputType>

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.PhpElement
intf org.netbeans.modules.csl.api.ElementHandle
meth public abstract boolean isDeprecated()
meth public abstract boolean isPlatform()
meth public abstract int getFlags()
meth public abstract int getOffset()
meth public abstract java.lang.String getFilenameUrl()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.php.editor.api.ElementQuery getElementQuery()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.php.editor.api.PhpElementKind getPhpElementKind()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.php.editor.api.PhpModifiers getPhpModifiers()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.TraitElement
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind KIND
intf org.netbeans.modules.php.editor.api.elements.TraitedElement

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.TraitedElement
intf org.netbeans.modules.php.editor.api.elements.TypeElement
meth public abstract java.util.Collection<org.netbeans.modules.php.editor.api.QualifiedName> getUsedTraits()

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.TreeElement<%0 extends org.netbeans.modules.php.editor.api.elements.PhpElement>
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TreeElement<{org.netbeans.modules.php.editor.api.elements.TreeElement%0}>> children()
meth public abstract {org.netbeans.modules.php.editor.api.elements.TreeElement%0} getElement()

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.TypeConstantElement
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind KIND
intf org.netbeans.modules.php.editor.api.elements.TypeMemberElement
meth public abstract boolean isMagic()
meth public abstract java.lang.String getDeclaredType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getValue()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.TypeElement
innr public final static !enum PrintAs
intf org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement
meth public abstract boolean isClass()
meth public abstract boolean isEnum()
meth public abstract boolean isInterface()
meth public abstract boolean isTrait()
meth public abstract boolean isTraited()
meth public abstract java.lang.String asString(org.netbeans.modules.php.editor.api.elements.TypeElement$PrintAs)
meth public abstract java.util.Collection<org.netbeans.modules.php.editor.api.QualifiedName> getFQSuperInterfaceNames()
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.QualifiedName> getSuperInterfaces()

CLSS public final static !enum org.netbeans.modules.php.editor.api.elements.TypeElement$PrintAs
 outer org.netbeans.modules.php.editor.api.elements.TypeElement
fld public final static org.netbeans.modules.php.editor.api.elements.TypeElement$PrintAs NameAndSuperTypes
fld public final static org.netbeans.modules.php.editor.api.elements.TypeElement$PrintAs SuperTypes
meth public static org.netbeans.modules.php.editor.api.elements.TypeElement$PrintAs valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.api.elements.TypeElement$PrintAs[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.api.elements.TypeElement$PrintAs>

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.TypeMemberElement
intf org.netbeans.modules.php.editor.api.elements.PhpElement
meth public abstract boolean isAbstract()
meth public abstract boolean isFinal()
meth public abstract boolean isPrivate()
meth public abstract boolean isProtected()
meth public abstract boolean isPublic()
meth public abstract boolean isStatic()
meth public abstract org.netbeans.modules.php.editor.api.elements.TypeElement getType()

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.TypeNameResolver
meth public abstract org.netbeans.modules.php.editor.api.QualifiedName resolve(org.netbeans.modules.php.editor.api.QualifiedName)

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.TypeResolver
meth public abstract boolean canBeResolved()
meth public abstract boolean isNullableType()
meth public abstract boolean isResolved()
meth public abstract java.lang.String getRawTypeName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.php.editor.api.QualifiedName getTypeName(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.TypedInstanceElement
intf org.netbeans.modules.php.editor.api.elements.PhpElement
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeResolver> getInstanceFQTypes()
meth public abstract java.util.Set<org.netbeans.modules.php.editor.api.elements.TypeResolver> getInstanceTypes()

CLSS public abstract interface org.netbeans.modules.php.editor.api.elements.VariableElement
fld public final static org.netbeans.modules.php.editor.api.PhpElementKind KIND
intf org.netbeans.modules.php.editor.api.elements.PhpElement
intf org.netbeans.modules.php.editor.api.elements.TypedInstanceElement
meth public abstract java.lang.String getName(boolean)

CLSS public org.netbeans.modules.php.editor.lexer.DocumentorColoringScanner
cons public init(java.io.InputStream)
cons public init(java.io.Reader)
fld public final static int ST_HTML_TAG = 6
fld public final static int ST_IN_TAG = 2
fld public final static int ST_NO_TAG = 4
fld public final static int YYEOF = -1
fld public final static int YYINITIAL = 0
innr public LexerState
meth public final char yycharat(int)
meth public final int yylength()
meth public final int yystate()
meth public final java.lang.String yytext()
meth public final void yybegin(int)
meth public final void yyclose() throws java.io.IOException
meth public final void yyreset(java.io.Reader)
meth public int getTokenLength()
meth public org.netbeans.modules.php.editor.lexer.DocumentorColoringScanner$LexerState getState()
meth public org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId nextToken() throws java.io.IOException
meth public void setState(org.netbeans.modules.php.editor.lexer.DocumentorColoringScanner$LexerState)
meth public void yypushback(int)
supr java.lang.Object
hfds ZZ_ACTION,ZZ_ACTION_PACKED_0,ZZ_ATTRIBUTE,ZZ_ATTRIBUTE_PACKED_0,ZZ_BUFFERSIZE,ZZ_CMAP,ZZ_CMAP_PACKED,ZZ_ERROR_MSG,ZZ_LEXSTATE,ZZ_NO_MATCH,ZZ_PUSHBACK_2BIG,ZZ_ROWMAP,ZZ_ROWMAP_PACKED_0,ZZ_TRANS,ZZ_TRANS_PACKED_0,ZZ_UNKNOWN_ERROR,input,yychar,yycolumn,yyline,zzAtBOL,zzAtEOF,zzBuffer,zzCurrentPos,zzEndRead,zzLexicalState,zzMarkedPos,zzPushbackPos,zzReader,zzStartRead,zzState

CLSS public org.netbeans.modules.php.editor.lexer.DocumentorColoringScanner$LexerState
 outer org.netbeans.modules.php.editor.lexer.DocumentorColoringScanner
supr java.lang.Object
hfds zzLexicalState,zzState

CLSS public final org.netbeans.modules.php.editor.lexer.GSFPHPLexer
intf org.netbeans.spi.lexer.Lexer<org.netbeans.modules.php.editor.lexer.PHPTokenId>
meth public java.lang.Object state()
meth public org.netbeans.api.lexer.Token<org.netbeans.modules.php.editor.lexer.PHPTokenId> nextToken()
meth public static org.netbeans.modules.php.editor.lexer.GSFPHPLexer create(org.netbeans.spi.lexer.LexerRestartInfo<org.netbeans.modules.php.editor.lexer.PHPTokenId>,boolean)
meth public void release()
supr java.lang.Object
hfds scanner,tokenFactory

CLSS public final org.netbeans.modules.php.editor.lexer.LexUtilities
innr public final static !enum LineBalance
meth public !varargs static boolean textEquals(java.lang.CharSequence,char[])
meth public static boolean isCommentOnlyLine(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth public static boolean isPHPOperator(org.netbeans.modules.php.editor.lexer.PHPTokenId)
meth public static char getTokenChar(org.netbeans.editor.BaseDocument,int)
meth public static int findStartTokenOfExpression(org.netbeans.api.lexer.TokenSequence)
meth public static int getLineBalance(org.netbeans.editor.BaseDocument,int,org.netbeans.api.lexer.TokenId,org.netbeans.api.lexer.TokenId,org.netbeans.modules.php.editor.lexer.LexUtilities$LineBalance)
meth public static int getTokenBalance(org.netbeans.editor.BaseDocument,char,char,int) throws javax.swing.text.BadLocationException
meth public static org.netbeans.api.lexer.Token<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId> findEndOfLine(org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId>)
meth public static org.netbeans.api.lexer.Token<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId> findNext(org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId>,java.util.List<org.netbeans.modules.php.editor.lexer.PHPTokenId>)
meth public static org.netbeans.api.lexer.Token<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId> findNextToken(org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId>,java.util.List<org.netbeans.modules.php.editor.lexer.PHPTokenId>)
meth public static org.netbeans.api.lexer.Token<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId> findPrevious(org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId>,java.util.List<org.netbeans.modules.php.editor.lexer.PHPTokenId>)
meth public static org.netbeans.api.lexer.Token<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId> findPreviousToken(org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId>,java.util.List<org.netbeans.modules.php.editor.lexer.PHPTokenId>)
meth public static org.netbeans.api.lexer.Token<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId> getToken(org.netbeans.editor.BaseDocument,int)
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.api.lexer.TokenId> getMostEmbeddedTokenSequence(javax.swing.text.Document,int,boolean)
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId> getPositionedSequence(org.netbeans.editor.BaseDocument,int)
meth public static org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.php.editor.lexer.PHPTokenId> getPHPTokenSequence(javax.swing.text.Document,int)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.php.editor.lexer.PHPTokenId> getPHPTokenSequence(org.netbeans.api.lexer.TokenHierarchy<?>,int)
meth public static org.netbeans.modules.csl.api.OffsetRange findBegin(org.netbeans.editor.BaseDocument,org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId>)
meth public static org.netbeans.modules.csl.api.OffsetRange findBwd(org.netbeans.editor.BaseDocument,org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId>,org.netbeans.modules.php.editor.lexer.PHPTokenId,char,org.netbeans.modules.php.editor.lexer.PHPTokenId,char)
meth public static org.netbeans.modules.csl.api.OffsetRange findBwdAlternativeSyntax(org.netbeans.editor.BaseDocument,org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId>,org.netbeans.api.lexer.Token<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId>)
meth public static org.netbeans.modules.csl.api.OffsetRange findFwd(org.netbeans.editor.BaseDocument,org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId>,org.netbeans.modules.php.editor.lexer.PHPTokenId,char,org.netbeans.modules.php.editor.lexer.PHPTokenId,char)
meth public static org.netbeans.modules.csl.api.OffsetRange findFwdAlternativeSyntax(org.netbeans.editor.BaseDocument,org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId>,org.netbeans.api.lexer.Token<? extends org.netbeans.modules.php.editor.lexer.PHPTokenId>)
supr java.lang.Object

CLSS public final static !enum org.netbeans.modules.php.editor.lexer.LexUtilities$LineBalance
 outer org.netbeans.modules.php.editor.lexer.LexUtilities
fld public final static org.netbeans.modules.php.editor.lexer.LexUtilities$LineBalance DOWN_FIRST
fld public final static org.netbeans.modules.php.editor.lexer.LexUtilities$LineBalance PLAIN
fld public final static org.netbeans.modules.php.editor.lexer.LexUtilities$LineBalance UP_FIRST
meth public static org.netbeans.modules.php.editor.lexer.LexUtilities$LineBalance valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.lexer.LexUtilities$LineBalance[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.lexer.LexUtilities$LineBalance>

CLSS public org.netbeans.modules.php.editor.lexer.PHP5ColoringLexer
cons public init(java.io.InputStream)
cons public init(java.io.Reader)
cons public init(org.netbeans.spi.lexer.LexerRestartInfo,boolean,boolean,boolean)
fld public final static int ST_HALTED_COMPILER = 40
fld public final static int ST_PHP_BACKQUOTE = 6
fld public final static int ST_PHP_COMMENT = 32
fld public final static int ST_PHP_DOC_COMMENT = 34
fld public final static int ST_PHP_DOUBLE_QUOTES = 4
fld public final static int ST_PHP_END_HEREDOC = 16
fld public final static int ST_PHP_END_NOWDOC = 22
fld public final static int ST_PHP_HEREDOC = 12
fld public final static int ST_PHP_HIGHLIGHTING_ERROR = 38
fld public final static int ST_PHP_IN_SCRIPTING = 2
fld public final static int ST_PHP_LINE_COMMENT = 36
fld public final static int ST_PHP_LOOKING_FOR_CONSTANT_NAME = 28
fld public final static int ST_PHP_LOOKING_FOR_FUNCTION_NAME = 26
fld public final static int ST_PHP_LOOKING_FOR_PARAMETER_NAME = 44
fld public final static int ST_PHP_LOOKING_FOR_PROPERTY = 24
fld public final static int ST_PHP_LOOKING_FOR_STATIC_PROPERTY = 10
fld public final static int ST_PHP_LOOKING_FOR_TRUE_FALSE_NULL = 42
fld public final static int ST_PHP_NOWDOC = 18
fld public final static int ST_PHP_QUOTES_AFTER_VARIABLE = 8
fld public final static int ST_PHP_START_HEREDOC = 14
fld public final static int ST_PHP_START_NOWDOC = 20
fld public final static int ST_PHP_VAR_OFFSET = 30
fld public final static int YYEOF = -1
fld public final static int YYINITIAL = 0
innr public final static LexerState
meth protected boolean isHeredocState(int)
meth protected int getZZEndRead()
meth protected int getZZLexicalState()
meth protected int getZZMarkedPos()
meth protected int getZZPushBackPosition()
meth protected int getZZStartRead()
meth protected void popState()
meth protected void pushBack(int)
meth protected void pushState(int)
meth public char[] getZZBuffer()
meth public final char yycharat(int)
meth public final int yylength()
meth public final int yystate()
meth public final java.lang.String yytext()
meth public final void yybegin(int)
meth public final void yyclose() throws java.io.IOException
meth public final void yyreset(java.io.Reader)
meth public int[] getParamenters()
meth public org.netbeans.modules.php.editor.lexer.PHP5ColoringLexer$LexerState getState()
meth public org.netbeans.modules.php.editor.lexer.PHPTokenId nextToken() throws java.io.IOException
meth public void setState(org.netbeans.modules.php.editor.lexer.PHP5ColoringLexer$LexerState)
meth public void yypushback(int)
supr java.lang.Object
hfds ZZ_ACTION,ZZ_ACTION_PACKED_0,ZZ_ATTRIBUTE,ZZ_ATTRIBUTE_PACKED_0,ZZ_BUFFERSIZE,ZZ_CMAP,ZZ_CMAP_PACKED,ZZ_ERROR_MSG,ZZ_LEXSTATE,ZZ_NO_MATCH,ZZ_PUSHBACK_2BIG,ZZ_ROWMAP,ZZ_ROWMAP_PACKED_0,ZZ_TRANS,ZZ_TRANS_PACKED_0,ZZ_UNKNOWN_ERROR,aspTagsAllowed,braceBalanceInConst,bracketBalanceInConst,heredoc,heredocStack,input,isInConst,parenBalanceInConst,parenBalanceInScripting,shortTagsAllowed,stack,yychar,yycolumn,yyline,zzAtBOL,zzAtEOF,zzBuffer,zzCurrentPos,zzEndRead,zzLexicalState,zzMarkedPos,zzPushbackPos,zzReader,zzStartRead,zzState

CLSS public final static org.netbeans.modules.php.editor.lexer.PHP5ColoringLexer$LexerState
 outer org.netbeans.modules.php.editor.lexer.PHP5ColoringLexer
meth public boolean equals(java.lang.Object)
meth public int hashCode()
supr java.lang.Object
hfds braceBalanceInConst,bracketBalanceInConst,heredoc,heredocStack,parenBalanceInConst,parenBalanceInScripting,stack,zzLexicalState,zzState

CLSS public final org.netbeans.modules.php.editor.lexer.PHPDocCommentLexer
cons public init(org.netbeans.spi.lexer.LexerRestartInfo<org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId>)
intf org.netbeans.spi.lexer.Lexer<org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId>
meth public java.lang.Object state()
meth public java.util.prefs.Preferences getDocscanPreferences()
meth public org.netbeans.api.lexer.Token<org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId> nextToken()
meth public void release()
supr java.lang.Object
hfds scanner,tokenFactory

CLSS public final !enum org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId
fld public final static java.lang.String MIME_TYPE = "text/x-php-doccomment"
fld public final static org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId PHPDOC_ANNOTATION
fld public final static org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId PHPDOC_COMMENT
fld public final static org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId PHPDOC_HTML_TAG
intf org.netbeans.api.lexer.TokenId
meth public java.lang.String fixedText()
meth public java.lang.String primaryCategory()
meth public static org.netbeans.api.lexer.Language<org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId> language()
meth public static org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId>
hfds LANGUAGE,fixedText,primaryCategory

CLSS public final !enum org.netbeans.modules.php.editor.lexer.PHPTokenId
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHPDOC_COMMENT
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHPDOC_COMMENT_END
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHPDOC_COMMENT_START
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_ABSTRACT
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_ARRAY
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_AS
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_ATTRIBUTE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_BREAK
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_CALLABLE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_CASE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_CASTING
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_CATCH
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_CLASS
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_CLONE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_CLOSETAG
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_COMMENT
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_COMMENT_END
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_COMMENT_START
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_CONST
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_CONSTANT_ENCAPSED_STRING
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_CONTINUE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_CURLY_CLOSE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_CURLY_OPEN
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_DECLARE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_DEFAULT
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_DIE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_DO
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_ECHO
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_ELSE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_ELSEIF
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_EMPTY
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_ENCAPSED_AND_WHITESPACE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_ENDDECLARE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_ENDFOR
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_ENDFOREACH
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_ENDIF
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_ENDSWITCH
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_ENDWHILE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_ENUM
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_EVAL
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_EXIT
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_EXTENDS
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_FALSE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_FINAL
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_FINALLY
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_FN
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_FOR
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_FOREACH
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_FUNCTION
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_GLOBAL
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_GOTO
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_HALT_COMPILER
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_HEREDOC_TAG_END
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_HEREDOC_TAG_START
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_IF
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_IMPLEMENTS
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_INCLUDE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_INCLUDE_ONCE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_INSTANCEOF
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_INSTEADOF
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_INTERFACE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_ISSET
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_ITERABLE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_LINE_COMMENT
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_LIST
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_LOGICAL_AND
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_LOGICAL_OR
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_LOGICAL_XOR
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_MATCH
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_NAMESPACE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_NEW
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_NOT
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_NOWDOC_TAG_END
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_NOWDOC_TAG_START
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_NS_SEPARATOR
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_NULL
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_NULLSAFE_OBJECT_OPERATOR
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_NUMBER
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_OBJECT_OPERATOR
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_OPENTAG
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_OPERATOR
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_PAAMAYIM_NEKUDOTAYIM
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_PARENT
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_PRINT
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_PRIVATE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_PROTECTED
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_PUBLIC
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_READONLY
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_REQUIRE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_REQUIRE_ONCE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_RETURN
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_SELF
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_SEMICOLON
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_STATIC
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_STRING
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_SWITCH
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_TEXTUAL_OPERATOR
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_THROW
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_TOKEN
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_TRAIT
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_TRUE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_TRY
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_TYPE_BOOL
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_TYPE_FLOAT
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_TYPE_INT
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_TYPE_MIXED
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_TYPE_NEVER
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_TYPE_OBJECT
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_TYPE_STRING
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_TYPE_VOID
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_UNSET
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_USE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_VAR
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_VARIABLE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_VAR_COMMENT
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_WHILE
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_YIELD
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP_YIELD_FROM
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP__CLASS__
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP__DIR__
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP__FILE__
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP__FUNCTION__
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP__LINE__
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP__METHOD__
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP__NAMESPACE__
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId PHP__TRAIT__
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId T_INLINE_HTML
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId T_OPEN_TAG_WITH_ECHO
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId UNKNOWN_TOKEN
fld public final static org.netbeans.modules.php.editor.lexer.PHPTokenId WHITESPACE
intf org.netbeans.api.lexer.TokenId
meth public java.lang.String fixedText()
meth public java.lang.String primaryCategory()
meth public static org.netbeans.api.lexer.Language<org.netbeans.modules.php.editor.lexer.PHPTokenId> language()
meth public static org.netbeans.api.lexer.Language<org.netbeans.modules.php.editor.lexer.PHPTokenId> languageInPHP()
meth public static org.netbeans.modules.php.editor.lexer.PHPTokenId valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.lexer.PHPTokenId[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.lexer.PHPTokenId>
hfds LANGUAGE_HIERARCHY,fixedText,primaryCategory
hcls PHPLanguageHierarchy

CLSS public final org.netbeans.modules.php.editor.lexer.PHPTopLexer
intf org.netbeans.spi.lexer.Lexer<org.netbeans.modules.php.editor.lexer.PHPTopTokenId>
meth public java.lang.Object state()
meth public org.netbeans.api.lexer.Token<org.netbeans.modules.php.editor.lexer.PHPTopTokenId> nextToken()
meth public static org.netbeans.modules.php.editor.lexer.PHPTopLexer create(org.netbeans.spi.lexer.LexerRestartInfo<org.netbeans.modules.php.editor.lexer.PHPTopTokenId>)
meth public void release()
supr java.lang.Object
hfds scanner,tokenFactory
hcls PHPTopColoringLexer,State

CLSS public final !enum org.netbeans.modules.php.editor.lexer.PHPTopTokenId
fld public final static org.netbeans.modules.php.editor.lexer.PHPTopTokenId T_HTML
fld public final static org.netbeans.modules.php.editor.lexer.PHPTopTokenId T_PHP
fld public final static org.netbeans.modules.php.editor.lexer.PHPTopTokenId T_PHP_CLOSE_DELIMITER
fld public final static org.netbeans.modules.php.editor.lexer.PHPTopTokenId T_PHP_OPEN_DELIMITER
intf org.netbeans.api.lexer.TokenId
meth public java.lang.String fixedText()
meth public java.lang.String primaryCategory()
meth public static org.netbeans.api.lexer.Language<org.netbeans.modules.php.editor.lexer.PHPTopTokenId> language()
meth public static org.netbeans.modules.php.editor.lexer.PHPTopTokenId valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.lexer.PHPTopTokenId[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.lexer.PHPTopTokenId>
hfds LANGUAGE,fixedText,primaryCategory

CLSS public abstract interface org.netbeans.modules.php.editor.model.ArrowFunctionScope
intf org.netbeans.modules.php.editor.model.FunctionScope

CLSS public abstract interface org.netbeans.modules.php.editor.model.CaseElement
intf org.netbeans.modules.php.editor.model.ClassMemberElement
intf org.netbeans.modules.php.editor.model.ModelElement
meth public abstract boolean isBacked()
meth public abstract java.lang.String getValue()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.php.editor.model.ClassConstantElement
intf org.netbeans.modules.php.editor.model.ClassMemberElement
intf org.netbeans.modules.php.editor.model.ConstantElement
meth public abstract java.lang.String getDeclaredType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getValue()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.php.editor.model.ClassMemberElement
intf org.netbeans.modules.php.editor.model.ModelElement

CLSS public abstract interface org.netbeans.modules.php.editor.model.ClassScope
intf org.netbeans.modules.php.editor.api.elements.ClassElement
intf org.netbeans.modules.php.editor.model.TraitedScope
intf org.netbeans.modules.php.editor.model.TypeScope
intf org.netbeans.modules.php.editor.model.VariableScope
meth public abstract java.lang.String getDefaultConstructorIndexSignature()
meth public abstract java.util.Collection<? extends java.lang.String> getSuperClassNames()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.ClassScope> getSuperClasses()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.FieldElement> getDeclaredFields()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.FieldElement> getFields()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.FieldElement> getInheritedFields()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.MethodScope> getDeclaredConstructors()

CLSS public abstract interface org.netbeans.modules.php.editor.model.CodeMarker
meth public abstract boolean containsInclusive(int)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.CodeMarker> getAllMarkers()
meth public abstract void highlight(org.netbeans.modules.php.editor.model.OccurrenceHighlighter)

CLSS public abstract interface org.netbeans.modules.php.editor.model.ConstantElement
intf org.netbeans.modules.php.editor.model.ModelElement
meth public abstract java.lang.String getValue()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.php.editor.model.EnumScope
intf org.netbeans.modules.php.editor.api.elements.EnumElement
intf org.netbeans.modules.php.editor.model.TraitedScope
intf org.netbeans.modules.php.editor.model.TypeScope
intf org.netbeans.modules.php.editor.model.VariableScope
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.CaseElement> getDeclaredEnumCases()

CLSS public abstract interface org.netbeans.modules.php.editor.model.FieldElement
intf org.netbeans.modules.php.editor.model.ClassMemberElement
intf org.netbeans.modules.php.editor.model.TypeAssignments
meth public abstract boolean isAnnotation()
meth public abstract java.lang.String getDefaultType()
meth public abstract java.util.Collection<? extends java.lang.String> getDefaultTypeNames()

CLSS public abstract interface org.netbeans.modules.php.editor.model.FileScope
intf org.netbeans.modules.php.editor.model.Scope
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.NamespaceScope> getDeclaredNamespaces()
meth public abstract org.netbeans.modules.php.editor.model.IndexScope getIndexScope()
meth public abstract org.netbeans.modules.php.editor.model.NamespaceScope getDefaultDeclaredNamespace()

CLSS public final org.netbeans.modules.php.editor.model.FindUsageSupport
meth public java.util.Collection<org.netbeans.modules.php.editor.api.elements.MethodElement> overridingMethods()
meth public java.util.Collection<org.netbeans.modules.php.editor.api.elements.TypeElement> directSubclasses()
meth public java.util.Collection<org.netbeans.modules.php.editor.api.elements.TypeElement> subclasses()
meth public java.util.Collection<org.netbeans.modules.php.editor.model.Occurence> occurences(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.Set<org.openide.filesystems.FileObject> inFiles()
meth public org.netbeans.modules.php.editor.model.ModelElement elementToFind()
meth public static org.netbeans.modules.php.editor.model.FindUsageSupport getInstance(org.netbeans.modules.php.editor.api.ElementQuery$Index,org.netbeans.modules.php.editor.model.ModelElement)
supr java.lang.Object
hfds element,files,index

CLSS public abstract interface org.netbeans.modules.php.editor.model.FunctionScope
intf org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement
intf org.netbeans.modules.php.editor.model.Scope
intf org.netbeans.modules.php.editor.model.VariableScope
meth public abstract boolean isAnonymous()
meth public abstract boolean isReturnIntersectionType()
meth public abstract boolean isReturnUnionType()
meth public abstract java.lang.String getDeclaredReturnType()
meth public abstract java.util.Collection<? extends java.lang.String> getReturnTypeNames()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.TypeScope> getReturnTypes()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.TypeScope> getReturnTypes(boolean,java.util.Collection<? extends org.netbeans.modules.php.editor.model.TypeScope>)
meth public abstract java.util.List<? extends java.lang.String> getParameterNames()
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.api.elements.ParameterElement> getParameters()

CLSS public abstract interface org.netbeans.modules.php.editor.model.GroupUseScope
intf org.netbeans.modules.php.editor.model.Scope
meth public abstract java.util.List<org.netbeans.modules.php.editor.model.UseScope> getUseScopes()
meth public abstract org.netbeans.modules.php.editor.model.UseScope$Type getType()

CLSS public abstract interface org.netbeans.modules.php.editor.model.IncludeElement
intf org.netbeans.modules.php.editor.model.ModelElement
meth public abstract org.netbeans.modules.csl.api.OffsetRange getReferenceSpanRange()

CLSS public abstract interface org.netbeans.modules.php.editor.model.IndexScope
intf org.netbeans.modules.php.editor.model.Scope
meth public abstract !varargs java.util.List<? extends org.netbeans.modules.php.editor.model.FieldElement> findFields(org.netbeans.modules.php.editor.model.ClassScope,int[])
meth public abstract !varargs java.util.List<? extends org.netbeans.modules.php.editor.model.FieldElement> findFields(org.netbeans.modules.php.editor.model.ClassScope,java.lang.String,int[])
meth public abstract !varargs java.util.List<? extends org.netbeans.modules.php.editor.model.FieldElement> findFields(org.netbeans.modules.php.editor.model.TraitScope,int[])
meth public abstract !varargs java.util.List<? extends org.netbeans.modules.php.editor.model.FieldElement> findFields(org.netbeans.modules.php.editor.model.TraitScope,java.lang.String,int[])
meth public abstract !varargs java.util.List<? extends org.netbeans.modules.php.editor.model.MethodScope> findMethods(org.netbeans.modules.php.editor.model.TypeScope,java.lang.String,int[])
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.CaseElement> findEnumCases(org.netbeans.modules.php.editor.model.TypeScope)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.CaseElement> findEnumCases(org.netbeans.modules.php.editor.model.TypeScope,java.lang.String)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.ClassConstantElement> findClassConstants(org.netbeans.modules.php.editor.model.TypeScope)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.ClassConstantElement> findClassConstants(org.netbeans.modules.php.editor.model.TypeScope,java.lang.String)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.ClassConstantElement> findInheritedClassConstants(org.netbeans.modules.php.editor.model.ClassScope,java.lang.String)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.ClassScope> findClasses(org.netbeans.modules.php.editor.api.QualifiedName)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.ConstantElement> findConstants(org.netbeans.modules.php.editor.api.QualifiedName)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.EnumScope> findEnums(org.netbeans.modules.php.editor.api.QualifiedName)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.FieldElement> findInheritedFields(org.netbeans.modules.php.editor.model.ClassScope,java.lang.String)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.FunctionScope> findFunctions(org.netbeans.modules.php.editor.api.QualifiedName)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.InterfaceScope> findInterfaces(org.netbeans.modules.php.editor.api.QualifiedName)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.MethodScope> findInheritedMethods(org.netbeans.modules.php.editor.model.TypeScope,java.lang.String)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.MethodScope> findMethods(org.netbeans.modules.php.editor.model.TypeScope)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.TraitScope> findTraits(org.netbeans.modules.php.editor.api.QualifiedName)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.TypeScope> findTypes(org.netbeans.modules.php.editor.api.QualifiedName)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.VariableName> findVariables(java.lang.String)
meth public abstract org.netbeans.modules.php.editor.api.ElementQuery$Index getIndex()

CLSS public abstract interface org.netbeans.modules.php.editor.model.InterfaceScope
intf org.netbeans.modules.php.editor.api.elements.InterfaceElement
intf org.netbeans.modules.php.editor.model.TypeScope
intf org.netbeans.modules.php.editor.model.VariableScope

CLSS public abstract interface org.netbeans.modules.php.editor.model.MethodScope
intf org.netbeans.modules.php.editor.model.ClassMemberElement
intf org.netbeans.modules.php.editor.model.FunctionScope
intf org.netbeans.modules.php.editor.model.VariableScope
meth public abstract boolean isConstructor()
meth public abstract boolean isInitiator()
meth public abstract boolean isMagic()
meth public abstract java.lang.String getClassSkeleton()
meth public abstract java.lang.String getConstructorIndexSignature()
meth public abstract java.lang.String getInterfaceSkeleton()
meth public abstract org.netbeans.modules.php.editor.model.TypeScope getTypeScope()

CLSS public final org.netbeans.modules.php.editor.model.Model
innr public abstract static !enum Type
meth public java.util.List<org.netbeans.modules.php.api.editor.PhpBaseElement> getExtendedElements()
meth public org.netbeans.modules.php.editor.model.FileScope getFileScope()
meth public org.netbeans.modules.php.editor.model.IndexScope getIndexScope()
meth public org.netbeans.modules.php.editor.model.ModelElement findDeclaration(org.netbeans.modules.php.editor.api.elements.PhpElement)
meth public org.netbeans.modules.php.editor.model.OccurencesSupport getOccurencesSupport(int)
meth public org.netbeans.modules.php.editor.model.OccurencesSupport getOccurencesSupport(org.netbeans.modules.csl.api.OffsetRange)
meth public org.netbeans.modules.php.editor.model.ParameterInfoSupport getParameterInfoSupport(int)
meth public org.netbeans.modules.php.editor.model.VariableScope getVariableScope(int)
meth public org.netbeans.modules.php.editor.model.VariableScope getVariableScopeForNamedElement(int)
supr java.lang.Object
hfds LOGGER,info,modelVisitor,occurencesSupport

CLSS public abstract static !enum org.netbeans.modules.php.editor.model.Model$Type
 outer org.netbeans.modules.php.editor.model.Model
fld public final static org.netbeans.modules.php.editor.model.Model$Type COMMON
fld public final static org.netbeans.modules.php.editor.model.Model$Type EXTENDED
meth public abstract void process(org.netbeans.modules.php.editor.model.Model)
meth public static org.netbeans.modules.php.editor.model.Model$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.model.Model$Type[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.model.Model$Type>

CLSS public abstract interface org.netbeans.modules.php.editor.model.ModelElement
intf org.netbeans.modules.php.editor.api.elements.PhpElement
meth public abstract org.netbeans.modules.csl.api.ElementHandle getPHPElement()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getNameRange()
meth public abstract org.netbeans.modules.php.editor.api.QualifiedName getNamespaceName()
meth public abstract org.netbeans.modules.php.editor.model.Scope getInScope()
meth public abstract org.openide.util.Union2<java.lang.String,org.openide.filesystems.FileObject> getFile()
meth public abstract void addSelfToIndex(org.netbeans.modules.parsing.spi.indexing.support.IndexDocument)

CLSS public final org.netbeans.modules.php.editor.model.ModelFactory
meth public static org.netbeans.modules.php.editor.model.Model getModel(org.netbeans.modules.php.editor.parser.PHPParseResult)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final org.netbeans.modules.php.editor.model.ModelUtils
innr public abstract interface static ElementFilter
meth public !varargs static <%0 extends org.netbeans.modules.php.editor.model.ModelElement> java.util.Collection<? extends {%%0}> merge(java.util.Collection<? extends {%%0}>[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public !varargs static <%0 extends org.netbeans.modules.php.editor.model.ModelElement> java.util.List<? extends {%%0}> filter(java.util.Collection<{%%0}>,java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public !varargs static <%0 extends org.netbeans.modules.php.editor.model.ModelElement> java.util.List<? extends {%%0}> filter(java.util.Collection<{%%0}>,org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind,java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public !varargs static <%0 extends org.netbeans.modules.php.editor.model.ModelElement> {%%0} getFirst(java.util.Collection<{%%0}>,java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public !varargs static <%0 extends org.netbeans.modules.php.editor.model.ModelElement> {%%0} getFirst(java.util.Collection<{%%0}>,org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind,java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public !varargs static boolean nameKindMatch(java.lang.String,org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind,java.lang.String[])
meth public static <%0 extends java.lang.Object> {%%0} getFirst(java.util.Collection<? extends {%%0}>)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static <%0 extends org.netbeans.modules.php.editor.model.ModelElement> java.util.List<? extends {%%0}> filter(java.util.Collection<? extends {%%0}>,org.netbeans.modules.php.editor.model.ModelUtils$ElementFilter<{%%0}>)
meth public static <%0 extends org.netbeans.modules.php.editor.model.ModelElement> java.util.List<? extends {%%0}> filter(java.util.Collection<? extends {%%0}>,org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static <%0 extends org.netbeans.modules.php.editor.model.ModelElement> java.util.List<? extends {%%0}> filter(java.util.Collection<{%%0}>,org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind,org.netbeans.modules.php.editor.api.QualifiedName)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static <%0 extends org.netbeans.modules.php.editor.model.ModelElement> java.util.List<? extends {%%0}> filter(java.util.Collection<{%%0}>,org.netbeans.modules.php.editor.api.QualifiedName)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static <%0 extends org.netbeans.modules.php.editor.model.ModelElement> {%%0} getFirst(java.util.Collection<? extends {%%0}>,org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static <%0 extends org.netbeans.modules.php.editor.model.ModelElement> {%%0} getLast(java.util.List<? extends {%%0}>)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static boolean isAnonymousFunction(org.netbeans.modules.php.editor.model.Scope)
meth public static java.lang.String getCamelCaseName(org.netbeans.modules.php.editor.model.ModelElement)
meth public static java.lang.String toCamelCase(java.lang.String)
meth public static java.util.Collection<? extends org.netbeans.modules.php.editor.model.ClassScope> getDeclaredClasses(org.netbeans.modules.php.editor.model.FileScope)
meth public static java.util.Collection<? extends org.netbeans.modules.php.editor.model.ConstantElement> getDeclaredConstants(org.netbeans.modules.php.editor.model.FileScope)
meth public static java.util.Collection<? extends org.netbeans.modules.php.editor.model.EnumScope> getDeclaredEnums(org.netbeans.modules.php.editor.model.FileScope)
meth public static java.util.Collection<? extends org.netbeans.modules.php.editor.model.FunctionScope> getDeclaredFunctions(org.netbeans.modules.php.editor.model.FileScope)
meth public static java.util.Collection<? extends org.netbeans.modules.php.editor.model.InterfaceScope> getDeclaredInterfaces(org.netbeans.modules.php.editor.model.FileScope)
meth public static java.util.Collection<? extends org.netbeans.modules.php.editor.model.TraitScope> getDeclaredTraits(org.netbeans.modules.php.editor.model.FileScope)
meth public static java.util.Collection<? extends org.netbeans.modules.php.editor.model.TypeScope> getDeclaredTypes(org.netbeans.modules.php.editor.model.FileScope)
meth public static java.util.Collection<? extends org.netbeans.modules.php.editor.model.TypeScope> resolveType(org.netbeans.modules.php.editor.model.Model,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.Collection<? extends org.netbeans.modules.php.editor.model.TypeScope> resolveType(org.netbeans.modules.php.editor.model.Model,org.netbeans.modules.php.editor.parser.astnodes.Assignment)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.Collection<? extends org.netbeans.modules.php.editor.model.TypeScope> resolveType(org.netbeans.modules.php.editor.model.Model,org.netbeans.modules.php.editor.parser.astnodes.StaticDispatch)
meth public static java.util.Collection<? extends org.netbeans.modules.php.editor.model.TypeScope> resolveType(org.netbeans.modules.php.editor.model.Model,org.netbeans.modules.php.editor.parser.astnodes.VariableBase)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.Collection<? extends org.netbeans.modules.php.editor.model.TypeScope> resolveType(org.netbeans.modules.php.editor.model.Model,org.netbeans.modules.php.editor.parser.astnodes.VariableBase,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.Collection<? extends org.netbeans.modules.php.editor.model.TypeScope> resolveTypeAfterReferenceToken(org.netbeans.modules.php.editor.model.Model,org.netbeans.api.lexer.TokenSequence<org.netbeans.modules.php.editor.lexer.PHPTokenId>,int,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.Collection<? extends org.netbeans.modules.php.editor.model.VariableName> getDeclaredVariables(org.netbeans.modules.php.editor.model.FileScope)
meth public static java.util.List<? extends org.netbeans.modules.php.editor.model.ModelElement> getElements(org.netbeans.modules.php.editor.model.Scope,boolean)
meth public static java.util.Set<org.netbeans.modules.php.editor.api.AliasedName> getAliasedNames(org.netbeans.modules.php.editor.model.Model,int)
meth public static org.netbeans.modules.php.editor.model.ClassScope getClassScope(org.netbeans.modules.php.editor.model.ModelElement)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.editor.model.FileScope getFileScope(org.netbeans.modules.php.editor.model.ModelElement)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.editor.model.FileScope getFileScope(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.editor.model.FileScope getFileScope(org.openide.filesystems.FileObject,int)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.editor.model.IndexScope getIndexScope(org.netbeans.modules.php.editor.model.ModelElement)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.php.editor.model.Model getModel(org.netbeans.modules.parsing.api.Source)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.editor.model.Model getModel(org.netbeans.modules.parsing.api.Source,int)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.editor.model.NamespaceScope getNamespaceScope(org.netbeans.modules.php.editor.model.FileScope,int)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.editor.model.NamespaceScope getNamespaceScope(org.netbeans.modules.php.editor.model.ModelElement)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.editor.model.NamespaceScope getNamespaceScope(org.netbeans.modules.php.editor.parser.PHPParseResult,int)
meth public static org.netbeans.modules.php.editor.model.NamespaceScope getNamespaceScope(org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration,org.netbeans.modules.php.editor.model.FileScope)
meth public static org.netbeans.modules.php.editor.model.TypeScope getTypeScope(org.netbeans.modules.php.editor.model.ModelElement)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object
hfds LOGGER,RP
hcls NamespaceDeclarationVisitor

CLSS public abstract interface static org.netbeans.modules.php.editor.model.ModelUtils$ElementFilter<%0 extends org.netbeans.modules.php.editor.model.ModelElement>
 outer org.netbeans.modules.php.editor.model.ModelUtils
meth public abstract boolean isAccepted({org.netbeans.modules.php.editor.model.ModelUtils$ElementFilter%0})

CLSS public abstract interface org.netbeans.modules.php.editor.model.NamespaceScope
intf org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement
intf org.netbeans.modules.php.editor.model.VariableScope
meth public abstract boolean isDefaultNamespace()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.ClassScope> getDeclaredClasses()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.ConstantElement> getDeclaredConstants()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.EnumScope> getDeclaredEnums()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.FunctionScope> getDeclaredFunctions()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.GroupUseScope> getDeclaredGroupUses()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.InterfaceScope> getDeclaredInterfaces()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.TraitScope> getDeclaredTraits()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.TypeScope> getDeclaredTypes()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.UseScope> getAllDeclaredSingleUses()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.UseScope> getDeclaredSingleUses()
meth public abstract org.netbeans.modules.php.editor.api.QualifiedName getQualifiedName()
meth public abstract org.netbeans.modules.php.editor.model.FileScope getFileScope()

CLSS public abstract interface org.netbeans.modules.php.editor.model.Occurence
innr public final static !enum Accuracy
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.api.elements.PhpElement> getAllDeclarations()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.api.elements.PhpElement> gotoDeclarations()
meth public abstract java.util.Collection<org.netbeans.modules.php.editor.model.Occurence> getAllOccurences()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getOccurenceRange()
meth public abstract org.netbeans.modules.php.editor.api.PhpElementKind getKind()
meth public abstract org.netbeans.modules.php.editor.model.Occurence$Accuracy degreeOfAccuracy()

CLSS public final static !enum org.netbeans.modules.php.editor.model.Occurence$Accuracy
 outer org.netbeans.modules.php.editor.model.Occurence
fld public final static org.netbeans.modules.php.editor.model.Occurence$Accuracy EXACT
fld public final static org.netbeans.modules.php.editor.model.Occurence$Accuracy EXACT_TYPE
fld public final static org.netbeans.modules.php.editor.model.Occurence$Accuracy MORE
fld public final static org.netbeans.modules.php.editor.model.Occurence$Accuracy MORE_MEMBERS
fld public final static org.netbeans.modules.php.editor.model.Occurence$Accuracy MORE_TYPES
fld public final static org.netbeans.modules.php.editor.model.Occurence$Accuracy NO
fld public final static org.netbeans.modules.php.editor.model.Occurence$Accuracy UNIQUE
meth public static org.netbeans.modules.php.editor.model.Occurence$Accuracy valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.model.Occurence$Accuracy[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.model.Occurence$Accuracy>

CLSS public final org.netbeans.modules.php.editor.model.OccurencesSupport
meth public org.netbeans.modules.php.editor.model.CodeMarker getCodeMarker()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.php.editor.model.Occurence getOccurence()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object
hfds codeMarker,modelVisitor,occurence,offset

CLSS public abstract interface org.netbeans.modules.php.editor.model.OccurrenceHighlighter
fld public final static org.netbeans.modules.php.editor.model.OccurrenceHighlighter NONE
meth public abstract java.util.Set<org.netbeans.modules.csl.api.OffsetRange> getRanges()
meth public abstract void add(org.netbeans.modules.csl.api.OffsetRange)

CLSS public abstract interface org.netbeans.modules.php.editor.model.Parameter
meth public abstract boolean hasRawType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract boolean isIntersectionType()
meth public abstract boolean isMandatory()
meth public abstract boolean isReference()
meth public abstract boolean isUnionType()
meth public abstract boolean isVariadic()
meth public abstract int getModifier()
meth public abstract java.lang.String getDeclaredType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getDefaultValue()
meth public abstract java.lang.String getIndexSignature()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getPhpdocType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.util.List<org.netbeans.modules.php.editor.api.QualifiedName> getTypes()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getOffsetRange()

CLSS public org.netbeans.modules.php.editor.model.ParameterInfoSupport
meth public org.netbeans.modules.csl.api.ParameterInfo getParameterInfo()
supr java.lang.Object
hfds CTX_DELIMITERS,modelVisitor,offset
hcls State

CLSS public abstract interface org.netbeans.modules.php.editor.model.Scope
intf org.netbeans.modules.php.editor.model.ModelElement
meth public abstract java.lang.String getNormalizedName()
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.ModelElement> getElements()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getBlockRange()

CLSS public abstract interface org.netbeans.modules.php.editor.model.TraitScope
intf org.netbeans.modules.php.editor.api.elements.TraitElement
intf org.netbeans.modules.php.editor.model.TraitedScope
intf org.netbeans.modules.php.editor.model.TypeScope
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.FieldElement> getDeclaredFields()

CLSS public abstract interface org.netbeans.modules.php.editor.model.TraitedScope
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.TraitScope> getTraits()

CLSS public abstract interface org.netbeans.modules.php.editor.model.TypeAssignments
meth public abstract java.util.Collection<? extends java.lang.String> getTypeNames(int)
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.TypeScope> getArrayAccessTypes(int)
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.TypeScope> getFieldTypes(org.netbeans.modules.php.editor.model.FieldElement,int)
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.TypeScope> getTypes(int)

CLSS public abstract interface org.netbeans.modules.php.editor.model.TypeScope
intf org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement
intf org.netbeans.modules.php.editor.api.elements.TypeElement
intf org.netbeans.modules.php.editor.model.Scope
meth public abstract boolean isSubTypeOf(org.netbeans.modules.php.editor.model.TypeScope)
meth public abstract boolean isSuperTypeOf(org.netbeans.modules.php.editor.model.TypeScope)
meth public abstract java.lang.String getIndexSignature()
meth public abstract java.util.Collection<? extends java.lang.String> getSuperInterfaceNames()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.ClassConstantElement> getDeclaredConstants()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.ClassConstantElement> getInheritedConstants()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.InterfaceScope> getSuperInterfaceScopes()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.MethodScope> getDeclaredMethods()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.MethodScope> getInheritedMethods()
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.MethodScope> getMethods()

CLSS public abstract interface org.netbeans.modules.php.editor.model.UseAliasElement
intf org.netbeans.modules.php.editor.model.ModelElement

CLSS public abstract interface org.netbeans.modules.php.editor.model.UseScope
innr public final static !enum Type
intf org.netbeans.modules.php.editor.model.Scope
meth public abstract boolean isPartOfGroupUse()
meth public abstract org.netbeans.modules.php.editor.api.AliasedName getAliasedName()
meth public abstract org.netbeans.modules.php.editor.model.UseAliasElement getAliasElement()
meth public abstract org.netbeans.modules.php.editor.model.UseScope$Type getType()

CLSS public final static !enum org.netbeans.modules.php.editor.model.UseScope$Type
 outer org.netbeans.modules.php.editor.model.UseScope
fld public final static org.netbeans.modules.php.editor.model.UseScope$Type CONST
fld public final static org.netbeans.modules.php.editor.model.UseScope$Type FUNCTION
fld public final static org.netbeans.modules.php.editor.model.UseScope$Type TYPE
meth public java.lang.String toString()
meth public static org.netbeans.modules.php.editor.model.UseScope$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.model.UseScope$Type[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.model.UseScope$Type>
hfds type

CLSS public abstract interface org.netbeans.modules.php.editor.model.VariableName
intf org.netbeans.modules.php.editor.model.ModelElement
intf org.netbeans.modules.php.editor.model.TypeAssignments
meth public abstract boolean isGloballyVisible()
meth public abstract boolean representsThis()
meth public abstract org.openide.filesystems.FileObject getRealFileObject()

CLSS public abstract interface org.netbeans.modules.php.editor.model.VariableScope
intf org.netbeans.modules.php.editor.model.Scope
meth public abstract java.util.Collection<? extends org.netbeans.modules.php.editor.model.VariableName> getDeclaredVariables()

CLSS public final org.netbeans.modules.php.editor.model.VariableScopeFinder
innr public abstract interface static ScopeRangeAcceptor
innr public abstract interface static VariableScopeWrapper
meth public org.netbeans.modules.php.editor.model.VariableScope find(java.util.List<? extends org.netbeans.modules.php.editor.model.ModelElement>,int,org.netbeans.modules.php.editor.model.VariableScopeFinder$ScopeRangeAcceptor)
meth public org.netbeans.modules.php.editor.model.VariableScope find(org.netbeans.modules.php.editor.model.Scope,int,org.netbeans.modules.php.editor.model.VariableScopeFinder$ScopeRangeAcceptor)
meth public org.netbeans.modules.php.editor.model.VariableScope findNearestVarScope(org.netbeans.modules.php.editor.model.Scope,int,org.netbeans.modules.php.editor.model.VariableScope)
meth public static org.netbeans.modules.php.editor.model.VariableScopeFinder create()
supr java.lang.Object
hfds LOGGER
hcls VariableScopeWrapperImpl

CLSS public abstract interface static org.netbeans.modules.php.editor.model.VariableScopeFinder$ScopeRangeAcceptor
 outer org.netbeans.modules.php.editor.model.VariableScopeFinder
fld public final static org.netbeans.modules.php.editor.model.VariableScopeFinder$ScopeRangeAcceptor BLOCK
fld public final static org.netbeans.modules.php.editor.model.VariableScopeFinder$ScopeRangeAcceptor NAME_START_BLOCK_END
meth public abstract boolean accept(org.netbeans.modules.php.editor.model.VariableScopeFinder$VariableScopeWrapper,int)
meth public abstract boolean overlaps(org.netbeans.modules.php.editor.model.VariableScopeFinder$VariableScopeWrapper,org.netbeans.modules.php.editor.model.VariableScopeFinder$VariableScopeWrapper)

CLSS public abstract interface static org.netbeans.modules.php.editor.model.VariableScopeFinder$VariableScopeWrapper
 outer org.netbeans.modules.php.editor.model.VariableScopeFinder
fld public final static org.netbeans.modules.php.editor.model.VariableScopeFinder$VariableScopeWrapper NONE
meth public abstract boolean containsRange(org.netbeans.modules.php.editor.model.VariableScopeFinder$VariableScopeWrapper)
meth public abstract boolean overlaps(org.netbeans.modules.php.editor.model.VariableScopeFinder$VariableScopeWrapper)
meth public abstract java.util.List<? extends org.netbeans.modules.php.editor.model.ModelElement> getElements()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getBlockRange()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getNameRange()
meth public abstract org.netbeans.modules.php.editor.model.VariableScope getVariableScope()

CLSS public org.netbeans.modules.php.editor.parser.ASTPHP5Parser
cons public init()
cons public init(java_cup.runtime.Scanner)
cons public init(java_cup.runtime.Scanner,java_cup.runtime.SymbolFactory)
fld protected final static java.lang.Integer ABSTRACT
fld protected final static java.lang.Integer FINAL
fld protected final static java.lang.Integer IMPLICIT_PUBLIC
fld protected final static java.lang.Integer PRIVATE
fld protected final static java.lang.Integer PROTECTED
fld protected final static java.lang.Integer PUBLIC
fld protected final static java.lang.Integer READONLY
fld protected final static java.lang.Integer STATIC
fld protected final static short[][] _action_table
fld protected final static short[][] _production_table
fld protected final static short[][] _reduce_table
fld protected java.lang.Object action_obj
meth protected boolean error_recovery(boolean) throws java.lang.Exception
meth protected int error_sync_size()
meth protected void init_actions()
meth public int EOF_sym()
meth public int error_sym()
meth public int incrementAndGetAnonymousClassCounter()
meth public int start_production()
meth public int start_state()
meth public java.lang.String getFileName()
meth public java_cup.runtime.Symbol do_action(int,java_cup.runtime.lr_parser,java.util.Stack,int) throws java.lang.Exception
meth public org.netbeans.modules.php.editor.parser.ParserErrorHandler getErrorHandler()
meth public org.netbeans.modules.php.editor.parser.astnodes.VariableBase createDispatch(org.netbeans.modules.php.editor.parser.ASTPHP5Parser$Access,org.netbeans.modules.php.editor.parser.astnodes.VariableBase,org.netbeans.modules.php.editor.parser.astnodes.Expression,int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>,int,java.util.List,java.util.List)
meth public org.netbeans.modules.php.editor.parser.astnodes.VariableBase createDispatch(org.netbeans.modules.php.editor.parser.astnodes.VariableBase,org.netbeans.modules.php.editor.parser.astnodes.VariableBase,java.util.List,org.netbeans.modules.php.editor.parser.ASTPHP5Parser$Access)
meth public org.netbeans.modules.php.editor.parser.astnodes.VariableBase createDispatch(org.netbeans.modules.php.editor.parser.astnodes.VariableBase,org.netbeans.modules.php.editor.parser.astnodes.VariableBase,org.netbeans.modules.php.editor.parser.ASTPHP5Parser$Access)
meth public org.netbeans.modules.php.editor.parser.astnodes.VariableBase createDispatch(org.netbeans.modules.php.editor.parser.astnodes.VariableBase,org.openide.util.Pair<org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.ASTPHP5Parser$Access>,java.util.List)
meth public org.openide.util.Pair<org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.ASTPHP5Parser$Access> createDispatchProperty(org.netbeans.modules.php.editor.parser.ASTPHP5Parser$Access,org.netbeans.modules.php.editor.parser.astnodes.Expression,int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>,int,java.util.List)
meth public short[][] action_table()
meth public short[][] production_table()
meth public short[][] reduce_table()
meth public void report_error(java.lang.String,java.lang.Object)
meth public void report_fatal_error(java.lang.String,java.lang.Object) throws java.lang.Exception
meth public void setErrorHandler(org.netbeans.modules.php.editor.parser.ParserErrorHandler)
meth public void setFileName(java.lang.String)
meth public void syntax_error(java_cup.runtime.Symbol)
supr java_cup.runtime.lr_parser
hfds anonymousClassCounter,defaultStrategy,errorHandler,errorStrategy,fileName
hcls Access,DefaultErrorStrategy,ErrorStrategy

CLSS public org.netbeans.modules.php.editor.parser.ASTPHP5Scanner
cons public init(java.io.InputStream)
cons public init(java.io.Reader)
cons public init(java.io.Reader,boolean,boolean)
fld protected int commentStartPosition
fld public final static int ST_BACKQUOTE = 6
fld public final static int ST_COMMENT = 26
fld public final static int ST_DOCBLOCK = 28
fld public final static int ST_DOUBLE_QUOTES = 4
fld public final static int ST_END_HEREDOC = 12
fld public final static int ST_END_NOWDOC = 18
fld public final static int ST_HALTED_COMPILER = 34
fld public final static int ST_HEREDOC = 8
fld public final static int ST_IN_SCRIPTING = 2
fld public final static int ST_IN_SHORT_ECHO = 32
fld public final static int ST_LOOKING_FOR_PROPERTY = 20
fld public final static int ST_LOOKING_FOR_VARNAME = 22
fld public final static int ST_NOWDOC = 14
fld public final static int ST_ONE_LINE_COMMENT = 30
fld public final static int ST_START_HEREDOC = 10
fld public final static int ST_START_NOWDOC = 16
fld public final static int ST_VAR_OFFSET = 24
fld public final static int YYEOF = -1
fld public final static int YYINITIAL = 0
intf java_cup.runtime.Scanner
meth protected int getTokenLength()
meth protected int getTokenStartPosition()
meth protected void addComment(org.netbeans.modules.php.editor.parser.astnodes.Comment$Type)
meth public boolean isEndedPhp()
meth public boolean useAspTagsAsPhp()
meth public final char yycharat(int)
meth public final int yylength()
meth public final int yystate()
meth public final java.lang.String yytext()
meth public final void yybegin(int)
meth public final void yyclose() throws java.io.IOException
meth public final void yyreset(java.io.Reader)
meth public int getCurlyBalance()
meth public int getCurrentLine()
meth public int getLength()
meth public int getState()
meth public int getWhitespaceEndPosition()
meth public int[] getParamenters()
meth public java.util.List getCommentList()
meth public java_cup.runtime.Symbol next_token() throws java.io.IOException
meth public void reset(java.io.Reader)
meth public void reset(java.io.Reader,char[],int[])
meth public void resetCommentList()
meth public void setInScriptingState()
meth public void setState(int)
meth public void setUseAspTagsAsPhp(boolean)
meth public void yypushback(int)
supr java.lang.Object
hfds ZZ_ACTION,ZZ_ACTION_PACKED_0,ZZ_ATTRIBUTE,ZZ_ATTRIBUTE_PACKED_0,ZZ_BUFFERSIZE,ZZ_CMAP,ZZ_CMAP_PACKED,ZZ_ERROR_MSG,ZZ_LEXSTATE,ZZ_NO_MATCH,ZZ_PUSHBACK_2BIG,ZZ_ROWMAP,ZZ_ROWMAP_PACKED_0,ZZ_TRANS,ZZ_TRANS_PACKED_0,ZZ_UNKNOWN_ERROR,asp_tags,bracket,comment,commentList,docParser,heredoc,heredocBody,heredocBodyLength,heredocBodyStart,heredocStack,isEndedPhp,nowdoc,nowdocBody,nowdocBodyLength,nowdocBodyStart,short_tags_allowed,stack,varParser,whitespaceEndPosition,yy_old_buffer,yy_old_pushbackPos,yychar,yycolumn,yyline,zzAtBOL,zzAtEOF,zzBuffer,zzCurrentPos,zzEndRead,zzLexicalState,zzMarkedPos,zzPushbackPos,zzReader,zzStartRead,zzState
hcls HeredocInfo

CLSS public abstract interface org.netbeans.modules.php.editor.parser.ASTPHP5Symbols
fld public final static int EOF = 0
fld public final static int T_ABSTRACT = 145
fld public final static int T_AMPERSAND_NOT_FOLLOWED_BY_VAR_OR_VARARG = 167
fld public final static int T_AND_EQUAL = 97
fld public final static int T_ARRAY = 59
fld public final static int T_ARRAY_CAST = 133
fld public final static int T_AS = 25
fld public final static int T_AT = 137
fld public final static int T_ATTRIBUTE = 165
fld public final static int T_BACKQUATE = 155
fld public final static int T_BOOLEAN_AND = 105
fld public final static int T_BOOLEAN_OR = 104
fld public final static int T_BOOL_CAST = 135
fld public final static int T_BREAK = 31
fld public final static int T_CALLABLE = 60
fld public final static int T_CASE = 29
fld public final static int T_CATCH = 41
fld public final static int T_CLASS = 51
fld public final static int T_CLASS_C = 61
fld public final static int T_CLONE = 24
fld public final static int T_CLOSE_PARENTHESE = 151
fld public final static int T_CLOSE_RECT = 139
fld public final static int T_COALESCE = 163
fld public final static int T_COALESCE_EQUAL = 164
fld public final static int T_COMMA = 85
fld public final static int T_CONCAT_EQUAL = 95
fld public final static int T_CONST = 36
fld public final static int T_CONSTANT_ENCAPSED_STRING = 12
fld public final static int T_CONTINUE = 32
fld public final static int T_CURLY_CLOSE = 72
fld public final static int T_CURLY_OPEN = 71
fld public final static int T_CURLY_OPEN_WITH_DOLAR = 70
fld public final static int T_DEC = 129
fld public final static int T_DECLARE = 21
fld public final static int T_DEFAULT = 30
fld public final static int T_DEFINE = 79
fld public final static int T_DIR = 76
fld public final static int T_DIV = 123
fld public final static int T_DIV_EQUAL = 94
fld public final static int T_DNUMBER = 5
fld public final static int T_DO = 14
fld public final static int T_DOLLAR = 153
fld public final static int T_DOLLAR_OPEN_CURLY_BRACES = 69
fld public final static int T_DOUBLE_ARROW = 57
fld public final static int T_DOUBLE_CAST = 131
fld public final static int T_ECHO = 13
fld public final static int T_ELLIPSIS = 162
fld public final static int T_ELSE = 143
fld public final static int T_ELSEIF = 142
fld public final static int T_EMPTY = 49
fld public final static int T_ENCAPSED_AND_WHITESPACE = 11
fld public final static int T_ENDDECLARE = 22
fld public final static int T_ENDFOR = 18
fld public final static int T_ENDFOREACH = 20
fld public final static int T_ENDIF = 141
fld public final static int T_ENDSWITCH = 27
fld public final static int T_ENDWHILE = 16
fld public final static int T_END_HEREDOC = 68
fld public final static int T_END_NOWDOC = 157
fld public final static int T_ENUM = 168
fld public final static int T_EQUAL = 90
fld public final static int T_EVAL = 82
fld public final static int T_EXIT = 2
fld public final static int T_EXTENDS = 53
fld public final static int T_FILE = 66
fld public final static int T_FINAL = 146
fld public final static int T_FINALLY = 43
fld public final static int T_FN = 34
fld public final static int T_FOR = 17
fld public final static int T_FOREACH = 19
fld public final static int T_FUNCTION = 35
fld public final static int T_FUNC_C = 64
fld public final static int T_GLOBAL = 45
fld public final static int T_GOTO = 33
fld public final static int T_HALT_COMPILER = 50
fld public final static int T_IF = 3
fld public final static int T_IMPLEMENTS = 54
fld public final static int T_INC = 128
fld public final static int T_INCLUDE = 80
fld public final static int T_INCLUDE_ONCE = 81
fld public final static int T_INLINE_HTML = 10
fld public final static int T_INSTANCEOF = 23
fld public final static int T_INSTEADOF = 159
fld public final static int T_INTERFACE = 52
fld public final static int T_INT_CAST = 130
fld public final static int T_ISSET = 48
fld public final static int T_IS_EQUAL = 109
fld public final static int T_IS_GREATER_OR_EQUAL = 114
fld public final static int T_IS_IDENTICAL = 111
fld public final static int T_IS_NOT_EQUAL = 110
fld public final static int T_IS_NOT_IDENTICAL = 112
fld public final static int T_IS_SMALLER_OR_EQUAL = 113
fld public final static int T_KOVA = 107
fld public final static int T_LGREATER = 117
fld public final static int T_LINE = 65
fld public final static int T_LIST = 58
fld public final static int T_LNUMBER = 4
fld public final static int T_LOGICAL_AND = 88
fld public final static int T_LOGICAL_OR = 86
fld public final static int T_LOGICAL_XOR = 87
fld public final static int T_MATCH = 28
fld public final static int T_METHOD_C = 63
fld public final static int T_MINUS = 121
fld public final static int T_MINUS_EQUAL = 92
fld public final static int T_MOD_EQUAL = 96
fld public final static int T_MUL_EQUAL = 93
fld public final static int T_NAMESPACE = 74
fld public final static int T_NAME_FULLY_QUALIFIED = 171
fld public final static int T_NAME_QUALIFIED = 170
fld public final static int T_NAME_RELATIVE = 169
fld public final static int T_NEKUDA = 127
fld public final static int T_NEKUDOTAIM = 152
fld public final static int T_NEW = 140
fld public final static int T_NOT = 125
fld public final static int T_NS_C = 75
fld public final static int T_NS_SEPARATOR = 77
fld public final static int T_NULLSAFE_OBJECT_OPERATOR = 56
fld public final static int T_NUM_STRING = 9
fld public final static int T_OBJECT_CAST = 134
fld public final static int T_OBJECT_OPERATOR = 55
fld public final static int T_OPEN_PARENTHESE = 150
fld public final static int T_OPEN_RECT = 138
fld public final static int T_OR = 106
fld public final static int T_OR_EQUAL = 98
fld public final static int T_PAAMAYIM_NEKUDOTAYIM = 73
fld public final static int T_PLUS = 120
fld public final static int T_PLUS_EQUAL = 91
fld public final static int T_POW = 160
fld public final static int T_POW_EQUAL = 161
fld public final static int T_PRECENT = 124
fld public final static int T_PRINT = 89
fld public final static int T_PRIVATE = 147
fld public final static int T_PROTECTED = 148
fld public final static int T_PUBLIC = 149
fld public final static int T_QUATE = 154
fld public final static int T_QUESTION_MARK = 102
fld public final static int T_READONLY = 166
fld public final static int T_REFERENCE = 108
fld public final static int T_REQUIRE = 83
fld public final static int T_REQUIRE_ONCE = 84
fld public final static int T_RETURN = 37
fld public final static int T_RGREATER = 116
fld public final static int T_SEMICOLON = 103
fld public final static int T_SL = 118
fld public final static int T_SL_EQUAL = 100
fld public final static int T_SPACESHIP = 115
fld public final static int T_SR = 119
fld public final static int T_SR_EQUAL = 101
fld public final static int T_START_HEREDOC = 67
fld public final static int T_START_NOWDOC = 156
fld public final static int T_STATIC = 144
fld public final static int T_STRING = 6
fld public final static int T_STRING_CAST = 132
fld public final static int T_STRING_VARNAME = 7
fld public final static int T_SWITCH = 26
fld public final static int T_THROW = 42
fld public final static int T_TILDA = 126
fld public final static int T_TIMES = 122
fld public final static int T_TRAIT = 158
fld public final static int T_TRAIT_C = 62
fld public final static int T_TRY = 40
fld public final static int T_UNSET = 47
fld public final static int T_UNSET_CAST = 136
fld public final static int T_USE = 44
fld public final static int T_VAR = 46
fld public final static int T_VARIABLE = 8
fld public final static int T_VAR_COMMENT = 78
fld public final static int T_WHILE = 15
fld public final static int T_XOR_EQUAL = 99
fld public final static int T_YIELD = 38
fld public final static int T_YIELD_FROM = 39
fld public final static int error = 1

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable1
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable10
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable11
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable12
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable13
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable14
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable15
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable16
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable17
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable18
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable19
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable2
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable20
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable3
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable4
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable5
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable6
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable7
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable8
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.EncodedActionTable9
cons protected init()
fld protected final java.lang.StringBuilder sb
meth public java.lang.String getTableData()
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.GSFPHPError
cons public init(java.lang.String,org.openide.filesystems.FileObject,int,int,org.netbeans.modules.csl.api.Severity,java.lang.Object[])
intf org.netbeans.modules.csl.api.Error$Badging
meth public boolean isLineError()
meth public boolean showExplorerBadge()
meth public int getEndPosition()
meth public int getStartPosition()
meth public java.lang.Object[] getParameters()
meth public java.lang.String getDescription()
meth public java.lang.String getDisplayName()
meth public java.lang.String getKey()
meth public org.netbeans.modules.csl.api.Severity getSeverity()
meth public org.openide.filesystems.FileObject getFile()
supr java.lang.Object
hfds SILENT_ERROR_BADGE,displayName,endPosition,file,parameters,severity,startPosition

CLSS public org.netbeans.modules.php.editor.parser.GSFPHPParser
cons public init()
fld public final static boolean PARSE_BIG_FILES
fld public final static int BIG_FILE_SIZE
innr public abstract interface static SanitizedPart
innr public final static !enum Sanitize
innr public static Context
innr public static SanitizedPartImpl
intf java.beans.PropertyChangeListener
meth protected boolean sanitizeCurly(org.netbeans.modules.php.editor.parser.GSFPHPParser$Context)
meth protected boolean sanitizeRequireAndInclude(org.netbeans.modules.php.editor.parser.GSFPHPParser$Context,int,int)
meth protected org.netbeans.modules.php.editor.parser.PHPParseResult parseBuffer(org.netbeans.modules.php.editor.parser.GSFPHPParser$Context,org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize,org.netbeans.modules.php.editor.parser.PHP5ErrorHandler) throws java.lang.Exception
meth public org.netbeans.modules.parsing.spi.Parser$Result getResult(org.netbeans.modules.parsing.api.Task) throws org.netbeans.modules.parsing.spi.ParseException
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void cancel(org.netbeans.modules.parsing.spi.Parser$CancelReason,org.netbeans.modules.parsing.spi.SourceModificationEvent)
meth public void parse(org.netbeans.modules.parsing.api.Snapshot,org.netbeans.modules.parsing.api.Task,org.netbeans.modules.parsing.spi.SourceModificationEvent) throws org.netbeans.modules.parsing.spi.ParseException
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr org.netbeans.modules.parsing.spi.Parser
hfds LOGGER,REGISTERED_PHP_EXTENSIONS,aspTags,changeSupport,projectPropertiesListenerAdded,result,shortTags,unitTestCaretPosition
hcls SnapshotSourceHolder,SourceHolder,StringSourceHolder

CLSS public static org.netbeans.modules.php.editor.parser.GSFPHPParser$Context
 outer org.netbeans.modules.php.editor.parser.GSFPHPParser
cons public init(org.netbeans.modules.parsing.api.Snapshot,int)
meth public int getCaretOffset()
meth public java.lang.String getBaseSource()
meth public java.lang.String getSanitizedSource()
meth public java.lang.String toString()
meth public org.netbeans.modules.parsing.api.Snapshot getSnapshot()
meth public org.netbeans.modules.php.editor.parser.GSFPHPParser$SanitizedPart getSanitizedPart()
meth public void setSanitizedPart(org.netbeans.modules.php.editor.parser.GSFPHPParser$SanitizedPart)
supr java.lang.Object
hfds caretOffset,sanitizedPart,snapshot,sourceHolder

CLSS public final static !enum org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize
 outer org.netbeans.modules.php.editor.parser.GSFPHPParser
fld public final static org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize BLOCK_START
fld public final static org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize EDITED_DOT
fld public final static org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize EDITED_LINE
fld public final static org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize ERROR_DOT
fld public final static org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize ERROR_LINE
fld public final static org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize MISSING_CURLY
fld public final static org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize NEVER
fld public final static org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize NONE
fld public final static org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize REQUIRE_FUNCTION_INCOMPLETE
fld public final static org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize SYNTAX_ERROR_BLOCK
fld public final static org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize SYNTAX_ERROR_CURRENT
fld public final static org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize SYNTAX_ERROR_PREVIOUS
fld public final static org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize SYNTAX_ERROR_PREVIOUS_LINE
meth public static org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.GSFPHPParser$Sanitize>

CLSS public abstract interface static org.netbeans.modules.php.editor.parser.GSFPHPParser$SanitizedPart
 outer org.netbeans.modules.php.editor.parser.GSFPHPParser
fld public final static org.netbeans.modules.php.editor.parser.GSFPHPParser$SanitizedPart NONE
meth public abstract java.lang.String getText()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getOffsetRange()

CLSS public static org.netbeans.modules.php.editor.parser.GSFPHPParser$SanitizedPartImpl
 outer org.netbeans.modules.php.editor.parser.GSFPHPParser
cons public init(org.netbeans.modules.csl.api.OffsetRange,java.lang.String)
intf org.netbeans.modules.php.editor.parser.GSFPHPParser$SanitizedPart
meth public java.lang.String getText()
meth public org.netbeans.modules.csl.api.OffsetRange getOffsetRange()
supr java.lang.Object
hfds offsetRange,text

CLSS public abstract interface org.netbeans.modules.php.editor.parser.PHP5ErrorHandler
innr public static FatalError
innr public static SyntaxError
intf org.netbeans.modules.php.editor.parser.ParserErrorHandler
meth public abstract java.util.List<org.netbeans.modules.csl.api.Error> displayFatalError()
meth public abstract java.util.List<org.netbeans.modules.csl.api.Error> displaySyntaxErrors(org.netbeans.modules.php.editor.parser.astnodes.Program)
meth public abstract java.util.List<org.netbeans.modules.php.editor.parser.PHP5ErrorHandler$SyntaxError> getSyntaxErrors()
meth public abstract void disableHandling()

CLSS public static org.netbeans.modules.php.editor.parser.PHP5ErrorHandler$FatalError
 outer org.netbeans.modules.php.editor.parser.PHP5ErrorHandler
meth public boolean isLineError()
supr org.netbeans.modules.php.editor.parser.GSFPHPError

CLSS public static org.netbeans.modules.php.editor.parser.PHP5ErrorHandler$SyntaxError
 outer org.netbeans.modules.php.editor.parser.PHP5ErrorHandler
cons public init(short[],java_cup.runtime.Symbol,java_cup.runtime.Symbol,org.netbeans.modules.php.editor.parser.PHP5ErrorHandler$SyntaxError$Type)
innr public abstract static !enum Type
meth public boolean generateExtraInfo()
meth public java.lang.String getMessageHeader()
meth public java_cup.runtime.Symbol getCurrentToken()
meth public java_cup.runtime.Symbol getPreviousToken()
meth public org.netbeans.modules.csl.api.Severity getSeverity()
meth public short[] getExpectedTokens()
supr java.lang.Object
hfds currentToken,expectedTokens,previousToken,type

CLSS public abstract static !enum org.netbeans.modules.php.editor.parser.PHP5ErrorHandler$SyntaxError$Type
 outer org.netbeans.modules.php.editor.parser.PHP5ErrorHandler$SyntaxError
fld public final static org.netbeans.modules.php.editor.parser.PHP5ErrorHandler$SyntaxError$Type FIRST_VALID_ERROR
fld public final static org.netbeans.modules.php.editor.parser.PHP5ErrorHandler$SyntaxError$Type POSSIBLE_ERROR
meth public abstract java.lang.String getMessageHeader()
meth public abstract org.netbeans.modules.csl.api.Severity getSeverity()
meth public static org.netbeans.modules.php.editor.parser.PHP5ErrorHandler$SyntaxError$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.PHP5ErrorHandler$SyntaxError$Type[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.PHP5ErrorHandler$SyntaxError$Type>

CLSS public org.netbeans.modules.php.editor.parser.PHP5ErrorHandlerImpl
cons public init(org.netbeans.modules.php.editor.parser.GSFPHPParser$Context)
intf org.netbeans.modules.php.editor.parser.PHP5ErrorHandler
meth public java.util.List<org.netbeans.modules.csl.api.Error> displayFatalError()
meth public java.util.List<org.netbeans.modules.csl.api.Error> displaySyntaxErrors(org.netbeans.modules.php.editor.parser.astnodes.Program)
meth public java.util.List<org.netbeans.modules.php.editor.parser.PHP5ErrorHandler$SyntaxError> getSyntaxErrors()
meth public void disableHandling()
meth public void handleError(org.netbeans.modules.php.editor.parser.ParserErrorHandler$Type,short[],java_cup.runtime.Symbol,java_cup.runtime.Symbol)
supr java.lang.Object
hfds context,handleErrors,syntaxErrors
hcls SyntaxErrorLogger,TokenWrapper

CLSS public org.netbeans.modules.php.editor.parser.PHPDocCommentParser
cons public init()
meth public org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock parse(int,int,java.lang.String)
supr java.lang.Object
hfds LINE_PARSERS,LINE_PARSERS_LOCK,PHP_DOC_VAR_TYPE_TAGS,pattern
hcls LineParsersListener,ParametersExtractor,ParametersExtractorImpl

CLSS public org.netbeans.modules.php.editor.parser.PHPParseResult
cons public init(org.netbeans.modules.parsing.api.Snapshot,org.netbeans.modules.php.editor.parser.astnodes.Program)
meth protected void invalidate()
meth public java.util.List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics()
meth public org.netbeans.modules.csl.api.OffsetRange getErrorRange()
meth public org.netbeans.modules.php.editor.model.Model getModel()
meth public org.netbeans.modules.php.editor.model.Model getModel(boolean)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.modules.php.editor.model.Model getModel(org.netbeans.modules.php.editor.model.Model$Type)
meth public org.netbeans.modules.php.editor.parser.astnodes.Program getProgram()
meth public void setErrors(java.util.List<org.netbeans.modules.csl.api.Error>)
supr org.netbeans.modules.csl.spi.ParserResult
hfds errors,model,root

CLSS public org.netbeans.modules.php.editor.parser.PHPVarCommentParser
cons public init()
supr java.lang.Object
hfds PHPDOCTAG

CLSS public abstract interface org.netbeans.modules.php.editor.parser.ParserErrorHandler
innr public final static !enum Type
meth public abstract void handleError(org.netbeans.modules.php.editor.parser.ParserErrorHandler$Type,short[],java_cup.runtime.Symbol,java_cup.runtime.Symbol)

CLSS public final static !enum org.netbeans.modules.php.editor.parser.ParserErrorHandler$Type
 outer org.netbeans.modules.php.editor.parser.ParserErrorHandler
fld public final static org.netbeans.modules.php.editor.parser.ParserErrorHandler$Type FATAL_PARSER_ERROR
fld public final static org.netbeans.modules.php.editor.parser.ParserErrorHandler$Type SYNTAX_ERROR
meth public static org.netbeans.modules.php.editor.parser.ParserErrorHandler$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.ParserErrorHandler$Type[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.ParserErrorHandler$Type>

CLSS public abstract interface org.netbeans.modules.php.editor.parser.PhpAnnotationType
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getName()
meth public abstract java.util.Map<org.netbeans.modules.csl.api.OffsetRange,java.lang.String> getTypes()

CLSS public org.netbeans.modules.php.editor.parser.UnknownAnnotationLine
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
intf org.netbeans.modules.php.spi.annotation.AnnotationParsedLine
meth public boolean startsWithAnnotation()
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public java.util.Map<org.netbeans.modules.csl.api.OffsetRange,java.lang.String> getTypes()
supr java.lang.Object
hfds description,name

CLSS public org.netbeans.modules.php.editor.parser.UnusedUsesCollector
cons public init(org.netbeans.modules.php.editor.parser.PHPParseResult)
innr public final static UnusedOffsetRanges
meth public java.util.Collection<org.netbeans.modules.php.editor.parser.UnusedUsesCollector$UnusedOffsetRanges> collect()
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.NamespaceName)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Program)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.UseStatement)
supr org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor
hfds NAMESPACE_SEPARATOR,parserResult,unusedUsesOffsetRanges

CLSS public final static org.netbeans.modules.php.editor.parser.UnusedUsesCollector$UnusedOffsetRanges
 outer org.netbeans.modules.php.editor.parser.UnusedUsesCollector
meth public org.netbeans.modules.csl.api.OffsetRange getRangeToReplace()
meth public org.netbeans.modules.csl.api.OffsetRange getRangeToVisualise()
supr java.lang.Object
hfds rangeToReplace,rangeToVisualise

CLSS public final org.netbeans.modules.php.editor.parser.Utils
meth public static int getRowEnd(java.lang.String,int)
meth public static int getRowStart(java.lang.String,int)
meth public static java.lang.String getASTScannerTokenName(int)
meth public static java.lang.String getRepeatingChars(char,int)
meth public static java.lang.String getSpaces(int)
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ASTError
cons public init(int,int)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ASTErrorExpression
cons public init(int,int)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression

CLSS public abstract org.netbeans.modules.php.editor.parser.astnodes.ASTNode
cons public init(int,int)
meth public abstract void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
meth public final int getEndOffset()
meth public final int getStartOffset()
meth public final void setSourceRange(int,int)
supr java.lang.Object
hfds endOffset,startOffset

CLSS public org.netbeans.modules.php.editor.parser.astnodes.AnonymousObjectVariable
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.CloneExpression)
meth public java.lang.String toString()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Variable

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.VariableBase,org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension getDimension()
meth public org.netbeans.modules.php.editor.parser.astnodes.VariableBase getName()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Variable
hfds dimension

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation
cons public init(int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.ArrayElement>,org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation$Type)
innr public abstract static !enum Type
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.ArrayElement> getElements()
meth public org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation$Type getType()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds elements,type

CLSS public abstract static !enum org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation$Type
 outer org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation
fld public final static org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation$Type NEW
fld public final static org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation$Type OLD
meth public static org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation$Type[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation$Type>

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension$Type)
innr public final static !enum Type
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension$Type getType()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getIndex()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds index,type

CLSS public final static !enum org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension$Type
 outer org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension
fld public final static org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension$Type VARIABLE_ARRAY
fld public final static org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension$Type VARIABLE_HASHTABLE
meth public static org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension$Type[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension$Type>

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ArrayElement
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getKey()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getValue()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.ASTNode
hfds key,value

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration
cons public init(int,int,java.util.List,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Expression,boolean,boolean)
intf org.netbeans.modules.php.editor.parser.astnodes.Attributed
meth public boolean isAttributed()
meth public boolean isReference()
meth public boolean isStatic()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute> getAttributes()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.FormalParameter> getFormalParameters()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getReturnType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration create(org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute>)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds attributes,expression,formalParameters,isReference,isStatic,returnType

CLSS public org.netbeans.modules.php.editor.parser.astnodes.Assignment
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.VariableBase,org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type,org.netbeans.modules.php.editor.parser.astnodes.Expression)
innr public final static !enum Type
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type getOperator()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getRightHandSide()
meth public org.netbeans.modules.php.editor.parser.astnodes.VariableBase getLeftHandSide()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds leftHandSide,operator,rightHandSide

CLSS public final static !enum org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type
 outer org.netbeans.modules.php.editor.parser.astnodes.Assignment
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type AND_EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type COALESCE_EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type CONCAT_EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type DIV_EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type MINUS_EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type MOD_EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type MUL_EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type OR_EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type PLUS_EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type POW_EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type SL_EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type SR_EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type XOR_EQUAL
meth public java.lang.String toString()
meth public static org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.Assignment$Type>
hfds operator

CLSS public org.netbeans.modules.php.editor.parser.astnodes.Attribute
cons public init(int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.AttributeDeclaration>)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.AttributeDeclaration> getAttributeDeclarations()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds attributeDeclarations

CLSS public org.netbeans.modules.php.editor.parser.astnodes.AttributeDeclaration
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>)
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getParameters()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getAttributeName()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds attributeName,parameters

CLSS public abstract interface org.netbeans.modules.php.editor.parser.astnodes.Attributed
meth public abstract boolean isAttributed()
meth public abstract java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute> getAttributes()

CLSS public org.netbeans.modules.php.editor.parser.astnodes.BackTickExpression
cons public init(int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression[])
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getExpressions()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds expressions

CLSS public org.netbeans.modules.php.editor.parser.astnodes.Block
cons public init(int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Statement>)
cons public init(int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Statement>,boolean)
meth public boolean isCurly()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Statement> getStatements()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds isCurly,statements

CLSS public abstract org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration
cons public init(int,int,int)
cons public init(int,int,int,boolean)
cons public init(int,int,int,boolean,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute>)
innr public static Modifier
intf org.netbeans.modules.php.editor.parser.astnodes.Attributed
meth public boolean isAttributed()
meth public int getModifier()
meth public java.lang.String getModifierString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute> getAttributes()
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds attributes,modifier

CLSS public static org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration$Modifier
 outer org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration
cons public init()
fld public final static int ABSTRACT = 1024
fld public final static int FINAL = 16
fld public final static int IMPLICIT_PUBLIC = 32
fld public final static int PRIVATE = 2
fld public final static int PROTECTED = 4
fld public final static int PUBLIC = 1
fld public final static int READONLY = 64
fld public final static int STATIC = 8
meth public static boolean isAbstract(int)
meth public static boolean isFinal(int)
meth public static boolean isImplicitPublic(int)
meth public static boolean isPrivate(int)
meth public static boolean isProtected(int)
meth public static boolean isPublic(int)
meth public static boolean isReadonly(int)
meth public static boolean isStatic(int)
meth public static boolean isVisibilityModifier(int)
meth public static java.lang.String toString(int)
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.astnodes.BreakStatement
cons public init(int,int)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds expression

CLSS public org.netbeans.modules.php.editor.parser.astnodes.CaseDeclaration
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Identifier,org.netbeans.modules.php.editor.parser.astnodes.Expression)
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getInitializer()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.php.editor.parser.astnodes.Identifier getName()
meth public static org.netbeans.modules.php.editor.parser.astnodes.CaseDeclaration create(org.netbeans.modules.php.editor.parser.astnodes.CaseDeclaration,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute>)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration
hfds initializer,name

CLSS public org.netbeans.modules.php.editor.parser.astnodes.CastExpression
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.CastExpression$Type)
innr public final static !enum Type
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.CastExpression$Type getCastingType()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds castingType,expression

CLSS public final static !enum org.netbeans.modules.php.editor.parser.astnodes.CastExpression$Type
 outer org.netbeans.modules.php.editor.parser.astnodes.CastExpression
fld public final static org.netbeans.modules.php.editor.parser.astnodes.CastExpression$Type ARRAY
fld public final static org.netbeans.modules.php.editor.parser.astnodes.CastExpression$Type BOOL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.CastExpression$Type INT
fld public final static org.netbeans.modules.php.editor.parser.astnodes.CastExpression$Type OBJECT
fld public final static org.netbeans.modules.php.editor.parser.astnodes.CastExpression$Type REAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.CastExpression$Type STRING
fld public final static org.netbeans.modules.php.editor.parser.astnodes.CastExpression$Type UNSET
meth public static org.netbeans.modules.php.editor.parser.astnodes.CastExpression$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.CastExpression$Type[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.CastExpression$Type>

CLSS public org.netbeans.modules.php.editor.parser.astnodes.CatchClause
cons public init(int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>,org.netbeans.modules.php.editor.parser.astnodes.Variable,org.netbeans.modules.php.editor.parser.astnodes.Block)
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getClassNames()
meth public org.netbeans.modules.php.editor.parser.astnodes.Block getBody()
meth public org.netbeans.modules.php.editor.parser.astnodes.Variable getVariable()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds body,classNames,variable

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration
cons public init(int,int,java.util.Map<org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration$Modifier,java.util.Set<org.netbeans.modules.csl.api.OffsetRange>>,org.netbeans.modules.php.editor.parser.astnodes.Identifier,org.netbeans.modules.php.editor.parser.astnodes.Expression,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>,org.netbeans.modules.php.editor.parser.astnodes.Block)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration$Modifier,org.netbeans.modules.php.editor.parser.astnodes.Identifier,org.netbeans.modules.php.editor.parser.astnodes.Expression,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>,org.netbeans.modules.php.editor.parser.astnodes.Block)
innr public final static !enum Modifier
meth public java.lang.String toString()
meth public java.util.Map<org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration$Modifier,java.util.Set<org.netbeans.modules.csl.api.OffsetRange>> getModifiers()
meth public org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration$Modifier getModifier()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getSuperClass()
meth public static org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration create(org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute>)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration
hfds modifier,modifiers,superClass

CLSS public final static !enum org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration$Modifier
 outer org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration
fld public final static org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration$Modifier ABSTRACT
fld public final static org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration$Modifier FINAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration$Modifier NONE
fld public final static org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration$Modifier READONLY
meth public java.lang.String toString()
meth public static org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration$Modifier valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration$Modifier[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration$Modifier>

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.ClassName,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>)
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
intf org.netbeans.modules.php.editor.parser.astnodes.Attributed
meth public boolean isAnonymous()
meth public boolean isAttributed()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute> getAttributes()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> ctorParams()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getInterfaces()
meth public org.netbeans.modules.php.editor.parser.astnodes.Block getBody()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.php.editor.parser.astnodes.ClassName getClassName()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getSuperClass()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation anonymous(java.lang.String,int,int,int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>,org.netbeans.modules.php.editor.parser.astnodes.Expression,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>,org.netbeans.modules.php.editor.parser.astnodes.Block)
meth public static org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation anonymous(java.lang.String,int,int,int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>,org.netbeans.modules.php.editor.parser.astnodes.Expression,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>,org.netbeans.modules.php.editor.parser.astnodes.Block,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute>)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds attributes,body,classCounter,className,classStartOffset,ctorParams,fileName,interfaces,superClass

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ClassName
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getName()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.ASTNode
hfds name

CLSS public org.netbeans.modules.php.editor.parser.astnodes.CloneExpression
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds expression

CLSS public org.netbeans.modules.php.editor.parser.astnodes.Comment
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Comment$Type)
innr public final static !enum Type
meth public org.netbeans.modules.php.editor.parser.astnodes.Comment$Type getCommentType()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.ASTNode
hfds commentType

CLSS public final static !enum org.netbeans.modules.php.editor.parser.astnodes.Comment$Type
 outer org.netbeans.modules.php.editor.parser.astnodes.Comment
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Comment$Type TYPE_MULTILINE
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Comment$Type TYPE_PHPDOC
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Comment$Type TYPE_SINGLE_LINE
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Comment$Type TYPE_VARTYPE
meth public java.lang.String toString()
meth public static org.netbeans.modules.php.editor.parser.astnodes.Comment$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.Comment$Type[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.Comment$Type>
hfds text

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression$OperatorType,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Expression)
innr public abstract static !enum OperatorType
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression$OperatorType getOperator()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getCondition()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getIfFalse()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getIfTrue()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds condition,ifFalse,ifTrue,operator

CLSS public abstract static !enum org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression$OperatorType
 outer org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression
fld public final static org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression$OperatorType COALESCE
fld public final static org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression$OperatorType ELVIS
fld public final static org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression$OperatorType QUESTION_MARK
meth public abstract boolean isOperatorToken(org.netbeans.api.lexer.Token<org.netbeans.modules.php.editor.lexer.PHPTokenId>)
meth public boolean isShortened()
meth public java.lang.String toString()
meth public static org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression$OperatorType valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression$OperatorType[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression$OperatorType>
hfds operatorSign,shortened

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration
cons public init(int,int,int,java.util.List,boolean)
meth public boolean isGlobal()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getInitializers()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Identifier> getNames()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getConstType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration create(int,int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,java.util.List,boolean)
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration create(org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute>)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration
hfds constType,initializers,isGlobal,names

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ConstantVariable
cons public init(org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Variable

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ContinueStatement
cons public init(int,int)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds expression

CLSS public org.netbeans.modules.php.editor.parser.astnodes.DeclareStatement
cons public init(int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Identifier>,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>,org.netbeans.modules.php.editor.parser.astnodes.Statement)
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getDirectiveValues()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Identifier> getDirectiveNames()
meth public org.netbeans.modules.php.editor.parser.astnodes.Statement getBody()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds body,directiveNames,directiveValues

CLSS public org.netbeans.modules.php.editor.parser.astnodes.DereferencableVariable
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.VariableBase
hfds expression

CLSS public org.netbeans.modules.php.editor.parser.astnodes.DereferencedArrayAccess
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.VariableBase,org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension getDimension()
meth public org.netbeans.modules.php.editor.parser.astnodes.VariableBase getMember()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Dispatch
hfds dimension

CLSS public abstract org.netbeans.modules.php.editor.parser.astnodes.Dispatch
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.VariableBase)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.VariableBase,boolean)
meth public abstract org.netbeans.modules.php.editor.parser.astnodes.VariableBase getMember()
meth public boolean isNullsafe()
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.VariableBase getDispatcher()
supr org.netbeans.modules.php.editor.parser.astnodes.VariableBase
hfds dispatcher,isNullsafe

CLSS public org.netbeans.modules.php.editor.parser.astnodes.DoStatement
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Statement)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getCondition()
meth public org.netbeans.modules.php.editor.parser.astnodes.Statement getBody()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds body,condition

CLSS public org.netbeans.modules.php.editor.parser.astnodes.EchoStatement
cons public init(int,int,java.util.List<java.lang.Exception>)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getExpressions()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds expressions

CLSS public org.netbeans.modules.php.editor.parser.astnodes.EmptyStatement
cons public init(int,int)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement

CLSS public org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Identifier,org.netbeans.modules.php.editor.parser.astnodes.Expression,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>,org.netbeans.modules.php.editor.parser.astnodes.Block)
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getBackingType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration create(org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute>)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration
hfds backingType

CLSS public abstract org.netbeans.modules.php.editor.parser.astnodes.Expression
cons public init(int,int)
supr org.netbeans.modules.php.editor.parser.astnodes.ASTNode

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ExpressionArrayAccess
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension getDimension()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.VariableBase
hfds dimension,expression

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds expression

CLSS public org.netbeans.modules.php.editor.parser.astnodes.FieldAccess
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.VariableBase,org.netbeans.modules.php.editor.parser.astnodes.Variable,boolean)
meth public org.netbeans.modules.php.editor.parser.astnodes.Variable getField()
meth public org.netbeans.modules.php.editor.parser.astnodes.VariableBase getMember()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Dispatch
hfds field

CLSS public org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration
cons public init(int,int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,java.util.List)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration> getFields()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getFieldType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression[] getInitialValues()
meth public org.netbeans.modules.php.editor.parser.astnodes.Variable[] getVariableNames()
meth public static org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration create(org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute>)
meth public static org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration create(org.netbeans.modules.php.editor.parser.astnodes.FormalParameter)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration
hfds fieldType,fields

CLSS public org.netbeans.modules.php.editor.parser.astnodes.FinallyClause
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Block)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Block getBody()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds body

CLSS public org.netbeans.modules.php.editor.parser.astnodes.FirstClassCallableArg
cons public init(int,int)
meth public java.lang.String toString()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Statement)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Statement)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getKey()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getValue()
meth public org.netbeans.modules.php.editor.parser.astnodes.Statement getStatement()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds expression,key,statement,value

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ForStatement
cons public init(int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>,org.netbeans.modules.php.editor.parser.astnodes.Statement)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getConditions()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getInitializers()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getUpdaters()
meth public org.netbeans.modules.php.editor.parser.astnodes.Statement getBody()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds body,conditions,initializers,updaters

CLSS public org.netbeans.modules.php.editor.parser.astnodes.FormalParameter
cons public init(int,int,java.lang.Integer,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Expression)
cons public init(int,int,java.lang.Integer,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Expression)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Expression)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Expression)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Reference)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Reference,org.netbeans.modules.php.editor.parser.astnodes.Expression)
intf org.netbeans.modules.php.editor.parser.astnodes.Attributed
meth public boolean isAttributed()
meth public boolean isIntersectionType()
meth public boolean isMandatory()
meth public boolean isNullableType()
meth public boolean isOptional()
meth public boolean isReference()
meth public boolean isUnionType()
meth public boolean isVariadic()
meth public int getModifier()
meth public java.lang.String getModifierString()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute> getAttributes()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getDefaultValue()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getParameterName()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getParameterType()
meth public static org.netbeans.modules.php.editor.parser.astnodes.FormalParameter create(org.netbeans.modules.php.editor.parser.astnodes.FormalParameter,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute>)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.ASTNode
hfds attributes,defaultValue,modifier,parameterName,parameterType

CLSS public org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Identifier,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.FormalParameter>,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Block,boolean)
intf org.netbeans.modules.php.editor.parser.astnodes.Attributed
meth public boolean isAttributed()
meth public boolean isReference()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute> getAttributes()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.FormalParameter> getFormalParameters()
meth public org.netbeans.modules.php.editor.parser.astnodes.Block getBody()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getReturnType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.php.editor.parser.astnodes.Identifier getFunctionName()
meth public static org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration create(org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute>)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds attributes,body,formalParameters,isReference,name,returnType

CLSS public org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.FunctionName,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getParameters()
meth public org.netbeans.modules.php.editor.parser.astnodes.FunctionName getFunctionName()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.VariableBase
hfds functionName,parameters

CLSS public org.netbeans.modules.php.editor.parser.astnodes.FunctionName
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getName()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.ASTNode
hfds name

CLSS public org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement
cons public init(int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Variable>)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Variable> getVariables()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds variables

CLSS public org.netbeans.modules.php.editor.parser.astnodes.GotoLabel
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Identifier)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Identifier getName()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds name

CLSS public org.netbeans.modules.php.editor.parser.astnodes.GotoStatement
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Identifier)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Identifier getLabel()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds label

CLSS public org.netbeans.modules.php.editor.parser.astnodes.GroupUseStatementPart
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.NamespaceName,java.util.List)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart> getItems()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.php.editor.parser.astnodes.NamespaceName getBaseNamespaceName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.UseStatementPart
hfds baseNamespaceName,items

CLSS public org.netbeans.modules.php.editor.parser.astnodes.HaltCompiler
cons public init(int,int)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement

CLSS public org.netbeans.modules.php.editor.parser.astnodes.Identifier
cons public init(int,int,java.lang.String)
cons public init(int,int,java.lang.String,boolean)
meth public boolean isKeyword()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds isKeyword,name

CLSS public org.netbeans.modules.php.editor.parser.astnodes.IfStatement
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Statement,org.netbeans.modules.php.editor.parser.astnodes.Statement)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getCondition()
meth public org.netbeans.modules.php.editor.parser.astnodes.Statement getFalseStatement()
meth public org.netbeans.modules.php.editor.parser.astnodes.Statement getTrueStatement()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds condition,falseStatement,trueStatement

CLSS public org.netbeans.modules.php.editor.parser.astnodes.IgnoreError
cons public init(int,int)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds expression

CLSS public org.netbeans.modules.php.editor.parser.astnodes.InLineHtml
cons public init(int,int)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement

CLSS public org.netbeans.modules.php.editor.parser.astnodes.Include
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Include$Type)
innr public final static !enum Type
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public org.netbeans.modules.php.editor.parser.astnodes.Include$Type getIncludeType()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds expression,includeType

CLSS public final static !enum org.netbeans.modules.php.editor.parser.astnodes.Include$Type
 outer org.netbeans.modules.php.editor.parser.astnodes.Include
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Include$Type INCLUDE
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Include$Type INCLUDE_ONCE
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Include$Type REQUIRE
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Include$Type REQUIRE_ONCE
meth public static org.netbeans.modules.php.editor.parser.astnodes.Include$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.Include$Type[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.Include$Type>

CLSS public org.netbeans.modules.php.editor.parser.astnodes.InfixExpression
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType,org.netbeans.modules.php.editor.parser.astnodes.Expression)
innr public final static !enum OperatorType
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getLeft()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getRight()
meth public org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType getOperator()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds left,operator,right

CLSS public final static !enum org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType
 outer org.netbeans.modules.php.editor.parser.astnodes.InfixExpression
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType AND
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType BOOL_AND
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType BOOL_OR
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType CONCAT
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType DIV
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType IS_EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType IS_GREATER_OR_EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType IS_IDENTICAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType IS_NOT_EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType IS_NOT_IDENTICAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType IS_SMALLER_OR_EQUAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType LGREATER
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType MINUS
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType MOD
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType MUL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType OR
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType PLUS
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType POW
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType RGREATER
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType SL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType SPACESHIP
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType SR
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType STRING_AND
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType STRING_OR
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType STRING_XOR
fld public final static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType XOR
meth public java.lang.String toString()
meth public static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.InfixExpression$OperatorType>
hfds operatorSign

CLSS public org.netbeans.modules.php.editor.parser.astnodes.InstanceOfExpression
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.ClassName)
meth public final org.netbeans.modules.php.editor.parser.astnodes.ClassName getClassName()
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds className,expression

CLSS public org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Identifier,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>,org.netbeans.modules.php.editor.parser.astnodes.Block)
meth public static org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration create(org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute>)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration

CLSS public org.netbeans.modules.php.editor.parser.astnodes.IntersectionType
cons public init(int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getTypes()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds types

CLSS public org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration
cons public init(int,int,java.util.List,org.netbeans.modules.php.editor.parser.astnodes.Expression,java.util.List,org.netbeans.modules.php.editor.parser.astnodes.Block,boolean,boolean)
intf org.netbeans.modules.php.editor.parser.astnodes.Attributed
meth public boolean isAttributed()
meth public boolean isReference()
meth public boolean isStatic()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute> getAttributes()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getLexicalVariables()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.FormalParameter> getFormalParameters()
meth public org.netbeans.modules.php.editor.parser.astnodes.Block getBody()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getReturnType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration create(org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute>)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
meth public void addAttributes(java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute>)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds attributes,body,formalParameters,isReference,isStatic,lexicalVariables,returnType

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ListVariable
cons public init(int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.ArrayElement>,org.netbeans.modules.php.editor.parser.astnodes.ListVariable$SyntaxType)
innr public abstract static !enum SyntaxType
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.ArrayElement> getElements()
meth public org.netbeans.modules.php.editor.parser.astnodes.ListVariable$SyntaxType getSyntaxType()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.VariableBase
hfds elements,syntaxType

CLSS public abstract static !enum org.netbeans.modules.php.editor.parser.astnodes.ListVariable$SyntaxType
 outer org.netbeans.modules.php.editor.parser.astnodes.ListVariable
fld public final static org.netbeans.modules.php.editor.parser.astnodes.ListVariable$SyntaxType NEW
fld public final static org.netbeans.modules.php.editor.parser.astnodes.ListVariable$SyntaxType OLD
meth public static org.netbeans.modules.php.editor.parser.astnodes.ListVariable$SyntaxType valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.ListVariable$SyntaxType[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.ListVariable$SyntaxType>

CLSS public org.netbeans.modules.php.editor.parser.astnodes.MatchArm
cons public init(int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>,org.netbeans.modules.php.editor.parser.astnodes.Expression,boolean)
meth public boolean isDefault()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getConditions()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds conditions,expression,isDefault

CLSS public org.netbeans.modules.php.editor.parser.astnodes.MatchExpression
cons public init(int,int,org.netbeans.modules.csl.api.OffsetRange,org.netbeans.modules.php.editor.parser.astnodes.Expression,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.MatchArm>)
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.MatchArm> getMatchArms()
meth public org.netbeans.modules.csl.api.OffsetRange getBlockRange()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds blockRange,expression,matchArms

CLSS public org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration
cons public init(int,int,int,org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration)
cons public init(int,int,int,org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration,boolean)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration getFunction()
meth public static org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration create(org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute>)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration
hfds function

CLSS public org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.VariableBase,org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation,boolean)
meth public org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation getMember()
meth public org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation getMethod()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Dispatch
hfds method

CLSS public org.netbeans.modules.php.editor.parser.astnodes.NamedArgument
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Identifier,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public org.netbeans.modules.php.editor.parser.astnodes.Identifier getParameterName()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds expression,parameterName

CLSS public org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.NamespaceName,org.netbeans.modules.php.editor.parser.astnodes.Block,boolean)
meth public boolean isBracketed()
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Block getBody()
meth public org.netbeans.modules.php.editor.parser.astnodes.NamespaceName getName()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
meth public void addStatement(org.netbeans.modules.php.editor.parser.astnodes.Statement)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds body,bracketed,name

CLSS public org.netbeans.modules.php.editor.parser.astnodes.NamespaceName
cons public init(int,int,java.util.List,boolean,boolean)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Identifier[],boolean,boolean)
fld protected java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Identifier> segments
meth public boolean isCurrent()
meth public boolean isGlobal()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Identifier> getSegments()
meth public static org.netbeans.modules.php.editor.parser.astnodes.NamespaceName create(int,int,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds current,global

CLSS public org.netbeans.modules.php.editor.parser.astnodes.NullableType
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getType()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds type

CLSS public org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock
cons public init(int,int,java.lang.String)
cons public init(int,int,java.lang.String,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag>)
meth public java.lang.String getDescription()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag> getTags()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Comment
hfds description,tags

CLSS public org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag
cons public init(int,int,org.netbeans.modules.php.spi.annotation.AnnotationParsedLine,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode>,org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag>,java.lang.String)
cons public init(int,int,org.netbeans.modules.php.spi.annotation.AnnotationParsedLine,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode>,org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag>,java.lang.String,boolean)
meth public boolean isStatic()
meth public java.lang.String getDocumentation()
meth public java.lang.String getReturnType()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag> getParameters()
meth public org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode getMethodName()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag
hfds isStatic,name,params,returnType
hcls CommentExtractor,CommentExtractorImpl

CLSS public org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode
cons public init(int,int,java.lang.String)
meth public java.lang.String getValue()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.ASTNode
hfds value

CLSS public org.netbeans.modules.php.editor.parser.astnodes.PHPDocStaticAccessType
cons public init(int,int,java.lang.String,org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode,org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode)
meth public org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode getClassName()
meth public org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode getConstant()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode
hfds className,constant

CLSS public org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag
cons public init(int,int,org.netbeans.modules.php.spi.annotation.AnnotationParsedLine,java.lang.String)
innr public final static !enum Type
meth public java.lang.String getDocumentation()
meth public java.lang.String getValue()
meth public org.netbeans.modules.php.spi.annotation.AnnotationParsedLine getKind()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.ASTNode
hfds type,value

CLSS public final static !enum org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag$Type
 outer org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag
fld public final static org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag$Type DEPRECATED
fld public final static org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag$Type GLOBAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag$Type METHOD
fld public final static org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag$Type MIXIN
fld public final static org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag$Type PARAM
fld public final static org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag$Type PROPERTY
fld public final static org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag$Type PROPERTY_READ
fld public final static org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag$Type PROPERTY_WRITE
fld public final static org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag$Type RETURN
fld public final static org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag$Type VAR
intf org.netbeans.modules.php.spi.annotation.AnnotationParsedLine
meth public boolean startsWithAnnotation()
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public java.util.Map<org.netbeans.modules.csl.api.OffsetRange,java.lang.String> getTypes()
meth public static org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag$Type[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag$Type>
hfds name

CLSS public org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode
cons public init(int,int,java.lang.String,boolean)
meth public boolean isArray()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode
hfds array

CLSS public org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag
cons public init(int,int,org.netbeans.modules.php.spi.annotation.AnnotationParsedLine,java.lang.String,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode>)
fld protected java.lang.String documentation
meth public java.lang.String getDocumentation()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode> getTypes()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag
hfds types

CLSS public org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag
cons public init(int,int,org.netbeans.modules.php.spi.annotation.AnnotationParsedLine,java.lang.String,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode>,org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode)
meth public java.lang.String getDocumentation()
meth public org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode getVariable()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag
hfds variable

CLSS public org.netbeans.modules.php.editor.parser.astnodes.PHPVarComment
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag)
meth public org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag getVariable()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Comment
hfds variable

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression
cons public init(int,int)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds expression

CLSS public org.netbeans.modules.php.editor.parser.astnodes.PostfixExpression
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.VariableBase,org.netbeans.modules.php.editor.parser.astnodes.PostfixExpression$Operator)
innr public final static !enum Operator
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.PostfixExpression$Operator getOperator()
meth public org.netbeans.modules.php.editor.parser.astnodes.VariableBase getVariable()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds operator,variable

CLSS public final static !enum org.netbeans.modules.php.editor.parser.astnodes.PostfixExpression$Operator
 outer org.netbeans.modules.php.editor.parser.astnodes.PostfixExpression
fld public final static org.netbeans.modules.php.editor.parser.astnodes.PostfixExpression$Operator DEC
fld public final static org.netbeans.modules.php.editor.parser.astnodes.PostfixExpression$Operator INC
meth public java.lang.String toString()
meth public static org.netbeans.modules.php.editor.parser.astnodes.PostfixExpression$Operator valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.PostfixExpression$Operator[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.PostfixExpression$Operator>
hfds operatorSign

CLSS public org.netbeans.modules.php.editor.parser.astnodes.PrefixExpression
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.VariableBase,org.netbeans.modules.php.editor.parser.astnodes.PrefixExpression$Operator)
innr public final static !enum Operator
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.PrefixExpression$Operator getOperator()
meth public org.netbeans.modules.php.editor.parser.astnodes.VariableBase getVariable()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds operator,variable

CLSS public final static !enum org.netbeans.modules.php.editor.parser.astnodes.PrefixExpression$Operator
 outer org.netbeans.modules.php.editor.parser.astnodes.PrefixExpression
fld public final static org.netbeans.modules.php.editor.parser.astnodes.PrefixExpression$Operator DEC
fld public final static org.netbeans.modules.php.editor.parser.astnodes.PrefixExpression$Operator INC
meth public java.lang.String toString()
meth public static org.netbeans.modules.php.editor.parser.astnodes.PrefixExpression$Operator valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.PrefixExpression$Operator[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.PrefixExpression$Operator>
hfds operatorSign

CLSS public org.netbeans.modules.php.editor.parser.astnodes.Program
cons public init(int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Statement>,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Comment>)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Comment> getComments()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Statement> getStatements()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.ASTNode
hfds comments,statements

CLSS public org.netbeans.modules.php.editor.parser.astnodes.Quote
cons public init(int,int,java.util.List<java.lang.Exception>,org.netbeans.modules.php.editor.parser.astnodes.Quote$Type)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression[],org.netbeans.modules.php.editor.parser.astnodes.Quote$Type)
innr public final static !enum Type
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getExpressions()
meth public org.netbeans.modules.php.editor.parser.astnodes.Quote$Type getQuoteType()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds expressions,quoteType

CLSS public final static !enum org.netbeans.modules.php.editor.parser.astnodes.Quote$Type
 outer org.netbeans.modules.php.editor.parser.astnodes.Quote
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Quote$Type HEREDOC
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Quote$Type QUOTE
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Quote$Type SINGLE
meth public static org.netbeans.modules.php.editor.parser.astnodes.Quote$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.Quote$Type[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.Quote$Type>

CLSS public org.netbeans.modules.php.editor.parser.astnodes.Reference
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.VariableBase)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Variadic)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds expression

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ReflectionVariable
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Variable

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement
cons public init(int,int)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds expression

CLSS public org.netbeans.modules.php.editor.parser.astnodes.Scalar
cons public init(int,int,java.lang.String,org.netbeans.modules.php.editor.parser.astnodes.Scalar$Type)
innr public final static !enum Type
meth public java.lang.String getStringValue()
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Scalar$Type getScalarType()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds scalarType,stringValue

CLSS public final static !enum org.netbeans.modules.php.editor.parser.astnodes.Scalar$Type
 outer org.netbeans.modules.php.editor.parser.astnodes.Scalar
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Scalar$Type INT
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Scalar$Type REAL
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Scalar$Type STRING
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Scalar$Type SYSTEM
fld public final static org.netbeans.modules.php.editor.parser.astnodes.Scalar$Type UNKNOWN
meth public static org.netbeans.modules.php.editor.parser.astnodes.Scalar$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.Scalar$Type[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.Scalar$Type>

CLSS public org.netbeans.modules.php.editor.parser.astnodes.SimpleASTNode
cons public init(int,int,java.lang.String)
cons public init(int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.SimpleASTNode>,java.lang.String)
meth public int getEndOffset()
meth public int getStartOffset()
meth public java.lang.String getKind()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.SimpleASTNode> getChildren()
supr java.lang.Object
hfds children,endOffset,kind,startOffset

CLSS public org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Variable,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getFieldType()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getValue()
meth public org.netbeans.modules.php.editor.parser.astnodes.Variable getName()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.ASTNode
hfds fieldType,name,value

CLSS public org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.NamespaceName,org.netbeans.modules.php.editor.parser.astnodes.Identifier)
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.UseStatement$Type,org.netbeans.modules.php.editor.parser.astnodes.NamespaceName,org.netbeans.modules.php.editor.parser.astnodes.Identifier)
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Identifier getAlias()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.php.editor.parser.astnodes.NamespaceName getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.php.editor.parser.astnodes.UseStatement$Type getType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.UseStatementPart
hfds alias,name,type

CLSS public abstract org.netbeans.modules.php.editor.parser.astnodes.Statement
cons public init(int,int)
supr org.netbeans.modules.php.editor.parser.astnodes.ASTNode

CLSS public org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Expression)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Expression,boolean)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Identifier)
meth public boolean isDynamicName()
meth public org.netbeans.modules.php.editor.parser.astnodes.ASTNode getMember()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getConstant()
meth public org.netbeans.modules.php.editor.parser.astnodes.Identifier getConstantName()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.StaticDispatch
hfds constant,isDynamicName

CLSS public abstract org.netbeans.modules.php.editor.parser.astnodes.StaticDispatch
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public abstract org.netbeans.modules.php.editor.parser.astnodes.ASTNode getMember()
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getDispatcher()
supr org.netbeans.modules.php.editor.parser.astnodes.VariableBase
hfds dispatcher

CLSS public org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Variable)
meth public org.netbeans.modules.php.editor.parser.astnodes.ASTNode getMember()
meth public org.netbeans.modules.php.editor.parser.astnodes.Variable getField()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.StaticDispatch
hfds field

CLSS public org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation)
meth public org.netbeans.modules.php.editor.parser.astnodes.ASTNode getMember()
meth public org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation getMethod()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.StaticDispatch
hfds method

CLSS public org.netbeans.modules.php.editor.parser.astnodes.StaticStatement
cons public init(int,int,java.util.List<java.lang.Exception>)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getExpressions()
meth public org.netbeans.modules.php.editor.parser.astnodes.Variable[] getVariables()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds expressions

CLSS public org.netbeans.modules.php.editor.parser.astnodes.SwitchCase
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Statement>,boolean)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Statement[],boolean)
meth public boolean isDefault()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Statement> getActions()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getValue()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds actions,isDefault,value

CLSS public org.netbeans.modules.php.editor.parser.astnodes.SwitchStatement
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Block)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Block getBody()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds body,expression

CLSS public org.netbeans.modules.php.editor.parser.astnodes.ThrowExpression
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds expression

CLSS public org.netbeans.modules.php.editor.parser.astnodes.TraitConflictResolutionDeclaration
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Identifier,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getSuppressedTraitNames()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getPreferredTraitName()
meth public org.netbeans.modules.php.editor.parser.astnodes.Identifier getMethodName()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds methodName,preferredTraitName,suppressedTraitNames

CLSS public org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Identifier,org.netbeans.modules.php.editor.parser.astnodes.Block)
meth public static org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration create(org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute>)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration

CLSS public org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Identifier,org.netbeans.modules.php.editor.parser.astnodes.Identifier,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration$Modifier)
innr public final static !enum Modifier
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getTraitName()
meth public org.netbeans.modules.php.editor.parser.astnodes.Identifier getNewMethodName()
meth public org.netbeans.modules.php.editor.parser.astnodes.Identifier getOldMethodName()
meth public org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration$Modifier getModifier()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds modifier,newMethodName,oldMethodName,traitName

CLSS public final static !enum org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration$Modifier
 outer org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration
fld public final static org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration$Modifier PRIVATE
fld public final static org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration$Modifier PROTECTED
fld public final static org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration$Modifier PUBLIC
meth public static org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration$Modifier valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration$Modifier[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration$Modifier>

CLSS public org.netbeans.modules.php.editor.parser.astnodes.TryStatement
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Block,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.CatchClause>,org.netbeans.modules.php.editor.parser.astnodes.FinallyClause)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.CatchClause> getCatchClauses()
meth public org.netbeans.modules.php.editor.parser.astnodes.Block getBody()
meth public org.netbeans.modules.php.editor.parser.astnodes.FinallyClause getFinallyClause()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds catchClauses,finallyClause,tryStatement

CLSS public abstract org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Identifier,org.netbeans.modules.php.editor.parser.astnodes.Expression[],org.netbeans.modules.php.editor.parser.astnodes.Block)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Identifier,org.netbeans.modules.php.editor.parser.astnodes.Expression[],org.netbeans.modules.php.editor.parser.astnodes.Block,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute>)
intf org.netbeans.modules.php.editor.parser.astnodes.Attributed
meth public boolean isAttributed()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Attribute> getAttributes()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getInterfaces()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getInterfaes()
 anno 0 java.lang.Deprecated()
meth public org.netbeans.modules.php.editor.parser.astnodes.Block getBody()
meth public org.netbeans.modules.php.editor.parser.astnodes.Identifier getName()
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds attributes,body,interfaces,name

CLSS public org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation$Operator)
innr public final static !enum Operator
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation$Operator getOperator()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds expression,operator

CLSS public final static !enum org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation$Operator
 outer org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation
fld public final static org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation$Operator MINUS
fld public final static org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation$Operator NOT
fld public final static org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation$Operator PLUS
fld public final static org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation$Operator TILDA
meth public java.lang.String toString()
meth public static org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation$Operator valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation$Operator[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation$Operator>
hfds operatorSign

CLSS public org.netbeans.modules.php.editor.parser.astnodes.UnionType
cons public init(int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression>)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.Expression> getTypes()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds types

CLSS public org.netbeans.modules.php.editor.parser.astnodes.UnpackableArrayElement
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.ArrayElement

CLSS public org.netbeans.modules.php.editor.parser.astnodes.UseStatement
cons public init(int,int,java.util.List)
cons public init(int,int,java.util.List,org.netbeans.modules.php.editor.parser.astnodes.UseStatement$Type)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart[])
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart[],org.netbeans.modules.php.editor.parser.astnodes.UseStatement$Type)
innr public final static !enum Type
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.UseStatementPart> getParts()
meth public org.netbeans.modules.php.editor.parser.astnodes.UseStatement$Type getType()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds parts,type

CLSS public final static !enum org.netbeans.modules.php.editor.parser.astnodes.UseStatement$Type
 outer org.netbeans.modules.php.editor.parser.astnodes.UseStatement
fld public final static org.netbeans.modules.php.editor.parser.astnodes.UseStatement$Type CONST
fld public final static org.netbeans.modules.php.editor.parser.astnodes.UseStatement$Type FUNCTION
fld public final static org.netbeans.modules.php.editor.parser.astnodes.UseStatement$Type TYPE
meth public java.lang.String toString()
meth public static org.netbeans.modules.php.editor.parser.astnodes.UseStatement$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.php.editor.parser.astnodes.UseStatement$Type[] values()
supr java.lang.Enum<org.netbeans.modules.php.editor.parser.astnodes.UseStatement$Type>
hfds type

CLSS public abstract org.netbeans.modules.php.editor.parser.astnodes.UseStatementPart
cons public init(int,int)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.ASTNode

CLSS public org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatement
cons public init(int,int,java.util.List<org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatementPart>,org.netbeans.modules.php.editor.parser.astnodes.Block)
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatementPart> getParts()
meth public org.netbeans.modules.php.editor.parser.astnodes.Block getBody()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds body,parts

CLSS public org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatementPart
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.NamespaceName)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.NamespaceName getName()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.ASTNode
hfds name

CLSS public org.netbeans.modules.php.editor.parser.astnodes.Variable
cons protected init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
cons protected init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,boolean)
cons public init(int,int,java.lang.String)
meth public boolean isDollared()
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getName()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.VariableBase
hfds isDollared,name

CLSS public org.netbeans.modules.php.editor.parser.astnodes.VariableBase
cons public init(int,int)
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression

CLSS public org.netbeans.modules.php.editor.parser.astnodes.Variadic
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpression()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds expression

CLSS public abstract interface org.netbeans.modules.php.editor.parser.astnodes.Visitor
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ASTError)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ASTErrorExpression)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ASTNode)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.AnonymousObjectVariable)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ArrayElement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.Assignment)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.Attribute)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.AttributeDeclaration)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.BackTickExpression)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.Block)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.BreakStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.CaseDeclaration)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.CastExpression)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.CatchClause)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ClassName)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.CloneExpression)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.Comment)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ConstantVariable)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ContinueStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.DeclareStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.DereferencableVariable)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.DereferencedArrayAccess)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.DoStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.EchoStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.EmptyStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ExpressionArrayAccess)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.FieldAccess)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.FinallyClause)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.FirstClassCallableArg)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ForStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.FormalParameter)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.FunctionName)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.GotoLabel)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.GotoStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.GroupUseStatementPart)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.HaltCompiler)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.Identifier)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.IfStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.IgnoreError)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.InLineHtml)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.Include)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.InfixExpression)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.InstanceOfExpression)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.IntersectionType)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ListVariable)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.MatchArm)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.MatchExpression)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.NamedArgument)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.NamespaceName)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.NullableType)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocStaticAccessType)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPVarComment)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.PostfixExpression)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.PrefixExpression)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.Program)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.Quote)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.Reference)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ReflectionVariable)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.Scalar)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.StaticStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.SwitchCase)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.SwitchStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.ThrowExpression)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.TraitConflictResolutionDeclaration)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.TryStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.UnionType)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.UnpackableArrayElement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.UseStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatementPart)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.Variable)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.Variadic)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.WhileStatement)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.YieldExpression)
meth public abstract void visit(org.netbeans.modules.php.editor.parser.astnodes.YieldFromExpression)

CLSS public org.netbeans.modules.php.editor.parser.astnodes.WhileStatement
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Statement)
meth public java.lang.String toString()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getCondition()
meth public org.netbeans.modules.php.editor.parser.astnodes.Statement getBody()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Statement
hfds body,condition

CLSS public org.netbeans.modules.php.editor.parser.astnodes.YieldExpression
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getKey()
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getValue()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds key,value

CLSS public org.netbeans.modules.php.editor.parser.astnodes.YieldFromExpression
cons public init(int,int,org.netbeans.modules.php.editor.parser.astnodes.Expression)
meth public org.netbeans.modules.php.editor.parser.astnodes.Expression getExpr()
meth public void accept(org.netbeans.modules.php.editor.parser.astnodes.Visitor)
supr org.netbeans.modules.php.editor.parser.astnodes.Expression
hfds expr

CLSS public org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor
cons public init()
meth protected void addToPath(org.netbeans.modules.php.editor.parser.astnodes.ASTNode)
meth protected void removeFromPath()
meth public java.util.List<org.netbeans.modules.php.editor.parser.astnodes.ASTNode> getPath()
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ASTError)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ASTErrorExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ASTNode)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.AnonymousObjectVariable)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ArrayElement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Assignment)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Attribute)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.AttributeDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.BackTickExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Block)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.BreakStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.CaseDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.CastExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.CatchClause)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ClassName)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.CloneExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Comment)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ConstantVariable)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ContinueStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.DeclareStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.DoStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.EchoStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.EmptyStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FieldAccess)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FinallyClause)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ForStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FormalParameter)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FunctionName)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.GroupUseStatementPart)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Identifier)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.IfStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.IgnoreError)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.InLineHtml)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Include)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.InfixExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.InstanceOfExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.IntersectionType)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ListVariable)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.MatchArm)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.MatchExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.NamedArgument)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PostfixExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PrefixExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Program)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Quote)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Reference)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ReflectionVariable)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Scalar)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.StaticStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.SwitchCase)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.SwitchStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ThrowExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.TraitConflictResolutionDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.TryStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.UnpackableArrayElement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.UseStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatementPart)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Variable)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.WhileStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.YieldExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.YieldFromExpression)
supr org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor
hfds path

CLSS public org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor
cons public init()
intf org.netbeans.modules.php.editor.parser.astnodes.Visitor
meth public void scan(java.lang.Iterable<? extends org.netbeans.modules.php.editor.parser.astnodes.ASTNode>)
meth public void scan(org.netbeans.modules.php.editor.parser.astnodes.ASTNode)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ASTError)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ASTErrorExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ASTNode)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.AnonymousObjectVariable)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ArrayElement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Assignment)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Attribute)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.AttributeDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.BackTickExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Block)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.BreakStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.CaseDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.CastExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.CatchClause)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ClassName)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.CloneExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Comment)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ConstantVariable)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ContinueStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.DeclareStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.DereferencableVariable)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.DereferencedArrayAccess)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.DoStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.EchoStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.EmptyStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ExpressionArrayAccess)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FieldAccess)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FinallyClause)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FirstClassCallableArg)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ForStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FormalParameter)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FunctionName)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.GotoLabel)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.GotoStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.GroupUseStatementPart)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.HaltCompiler)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Identifier)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.IfStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.IgnoreError)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.InLineHtml)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Include)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.InfixExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.InstanceOfExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.IntersectionType)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ListVariable)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.MatchArm)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.MatchExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.NamedArgument)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.NamespaceName)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.NullableType)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocStaticAccessType)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PHPVarComment)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PostfixExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.PrefixExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Program)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Quote)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Reference)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ReflectionVariable)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Scalar)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.StaticStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.SwitchCase)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.SwitchStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ThrowExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.TraitConflictResolutionDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.TryStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.UnionType)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.UnpackableArrayElement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.UseStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatementPart)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Variable)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Variadic)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.WhileStatement)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.YieldExpression)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.YieldFromExpression)
supr java.lang.Object

CLSS public org.netbeans.modules.php.editor.parser.astnodes.visitors.PhpElementVisitor
cons protected init(org.netbeans.modules.php.editor.parser.PHPParseResult)
meth public final org.netbeans.modules.php.editor.api.ElementQuery$File toElementQuery()
meth public org.netbeans.modules.php.editor.api.FileElementQuery getElementQuery()
meth public static org.netbeans.modules.php.editor.api.ElementQuery$File createElementQuery(org.netbeans.modules.php.editor.parser.PHPParseResult)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FieldAccess)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration)
meth public void visit(org.netbeans.modules.php.editor.parser.astnodes.Variable)
supr org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor
hfds elementQuery

CLSS public abstract interface org.netbeans.modules.php.spi.annotation.AnnotationParsedLine
innr public final static ParsedLine
meth public abstract boolean startsWithAnnotation()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getName()
meth public abstract java.util.Map<org.netbeans.modules.csl.api.OffsetRange,java.lang.String> getTypes()

CLSS public abstract interface org.netbeans.spi.lexer.Lexer<%0 extends org.netbeans.api.lexer.TokenId>
meth public abstract java.lang.Object state()
meth public abstract org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.Lexer%0}> nextToken()
meth public abstract void release()

