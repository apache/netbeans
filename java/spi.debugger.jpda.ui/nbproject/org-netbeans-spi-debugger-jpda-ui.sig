#Signature file v4.1
#Version 3.26

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

CLSS public abstract interface org.netbeans.spi.debugger.jpda.BreakpointStratifier
meth public abstract void stratify(org.netbeans.api.debugger.jpda.JPDABreakpoint)

CLSS public abstract org.netbeans.spi.debugger.jpda.BreakpointsClassFilter
cons public init()
innr public abstract interface static !annotation Registration
innr public static ClassNames
meth public abstract org.netbeans.spi.debugger.jpda.BreakpointsClassFilter$ClassNames filterClassNames(org.netbeans.spi.debugger.jpda.BreakpointsClassFilter$ClassNames,org.netbeans.api.debugger.jpda.JPDABreakpoint)
supr java.lang.Object
hcls ContextAware

CLSS public static org.netbeans.spi.debugger.jpda.BreakpointsClassFilter$ClassNames
 outer org.netbeans.spi.debugger.jpda.BreakpointsClassFilter
cons public init(java.lang.String[],java.lang.String[])
meth public java.lang.String[] getClassNames()
meth public java.lang.String[] getExcludedClassNames()
supr java.lang.Object
hfds classNames,excludedClassNames

CLSS public abstract interface static !annotation org.netbeans.spi.debugger.jpda.BreakpointsClassFilter$Registration
 outer org.netbeans.spi.debugger.jpda.BreakpointsClassFilter
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String path()

CLSS public abstract org.netbeans.spi.debugger.jpda.EditorContext
cons public init()
fld public final static java.lang.String BREAKPOINT_ANNOTATION_TYPE = "Breakpoint"
fld public final static java.lang.String CALL_STACK_FRAME_ANNOTATION_TYPE = "CallSite"
fld public final static java.lang.String CLASS_BREAKPOINT_ANNOTATION_TYPE = "ClassBreakpoint"
fld public final static java.lang.String CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = "CondBreakpoint"
fld public final static java.lang.String CURRENT_EXPRESSION_CURRENT_LINE_ANNOTATION_TYPE = "CurrentExpressionLine"
fld public final static java.lang.String CURRENT_EXPRESSION_SECONDARY_LINE_ANNOTATION_TYPE = "CurrentExpression"
fld public final static java.lang.String CURRENT_LAST_OPERATION_ANNOTATION_TYPE = "LastOperation"
fld public final static java.lang.String CURRENT_LINE_ANNOTATION_TYPE = "CurrentPC"
fld public final static java.lang.String CURRENT_OUT_OPERATION_ANNOTATION_TYPE = "StepOutOperation"
fld public final static java.lang.String DISABLED_BREAKPOINT_ANNOTATION_TYPE = "DisabledBreakpoint"
fld public final static java.lang.String DISABLED_CLASS_BREAKPOINT_ANNOTATION_TYPE = "DisabledClassBreakpoint"
fld public final static java.lang.String DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = "DisabledCondBreakpoint"
fld public final static java.lang.String DISABLED_FIELD_BREAKPOINT_ANNOTATION_TYPE = "DisabledFieldBreakpoint"
fld public final static java.lang.String DISABLED_METHOD_BREAKPOINT_ANNOTATION_TYPE = "DisabledMethodBreakpoint"
fld public final static java.lang.String FIELD_BREAKPOINT_ANNOTATION_TYPE = "FieldBreakpoint"
fld public final static java.lang.String METHOD_BREAKPOINT_ANNOTATION_TYPE = "MethodBreakpoint"
fld public final static java.lang.String OTHER_THREAD_ANNOTATION_TYPE = "OtherThread"
fld public final static java.lang.String PROP_LINE_NUMBER = "lineNumber"
innr public abstract interface static !annotation Registration
innr public abstract interface static BytecodeProvider
innr public final static MethodArgument
innr public final static Operation
innr public final static Position
meth protected final org.netbeans.spi.debugger.jpda.EditorContext$Operation createMethodOperation(org.netbeans.spi.debugger.jpda.EditorContext$Position,org.netbeans.spi.debugger.jpda.EditorContext$Position,org.netbeans.spi.debugger.jpda.EditorContext$Position,org.netbeans.spi.debugger.jpda.EditorContext$Position,java.lang.String,java.lang.String,int)
meth protected final org.netbeans.spi.debugger.jpda.EditorContext$Operation createMethodOperation(org.netbeans.spi.debugger.jpda.EditorContext$Position,org.netbeans.spi.debugger.jpda.EditorContext$Position,org.netbeans.spi.debugger.jpda.EditorContext$Position,org.netbeans.spi.debugger.jpda.EditorContext$Position,java.lang.String,java.lang.String,int,boolean)
meth protected final org.netbeans.spi.debugger.jpda.EditorContext$Position createPosition(int,int,int)
meth protected final void addNextOperationTo(org.netbeans.spi.debugger.jpda.EditorContext$Operation,org.netbeans.spi.debugger.jpda.EditorContext$Operation)
meth public abstract boolean showSource(java.lang.String,int,java.lang.Object)
meth public abstract int getCurrentLineNumber()
meth public abstract int getFieldLineNumber(java.lang.String,java.lang.String,java.lang.String)
meth public abstract int getLineNumber(java.lang.Object,java.lang.Object)
meth public abstract java.lang.Object annotate(java.lang.String,int,java.lang.String,java.lang.Object)
meth public abstract java.lang.String getClassName(java.lang.String,int)
meth public abstract java.lang.String getCurrentClassName()
meth public abstract java.lang.String getCurrentFieldName()
meth public abstract java.lang.String getCurrentMethodName()
meth public abstract java.lang.String getCurrentURL()
meth public abstract java.lang.String getSelectedIdentifier()
meth public abstract java.lang.String getSelectedMethodName()
meth public abstract java.lang.String[] getImports(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public abstract void createTimeStamp(java.lang.Object)
meth public abstract void disposeTimeStamp(java.lang.Object)
meth public abstract void removeAnnotation(java.lang.Object)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public abstract void updateTimeStamp(java.lang.Object,java.lang.String)
meth public int getMethodLineNumber(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.Object annotate(java.lang.String,int,int,java.lang.String,java.lang.Object)
meth public java.lang.Object annotate(java.lang.String,int,java.lang.String,java.lang.Object,org.netbeans.api.debugger.jpda.JPDAThread)
meth public java.lang.String[] getCurrentMethodDeclaration()
meth public org.netbeans.spi.debugger.jpda.EditorContext$MethodArgument[] getArguments(java.lang.String,int)
meth public org.netbeans.spi.debugger.jpda.EditorContext$MethodArgument[] getArguments(java.lang.String,org.netbeans.spi.debugger.jpda.EditorContext$Operation)
meth public org.netbeans.spi.debugger.jpda.EditorContext$Operation[] getOperations(java.lang.String,int,org.netbeans.spi.debugger.jpda.EditorContext$BytecodeProvider)
supr java.lang.Object
hcls ContextAware

CLSS public abstract interface static org.netbeans.spi.debugger.jpda.EditorContext$BytecodeProvider
 outer org.netbeans.spi.debugger.jpda.EditorContext
meth public abstract byte[] byteCodes()
meth public abstract byte[] constantPool()
meth public abstract int[] indexAtLines(int,int)

CLSS public final static org.netbeans.spi.debugger.jpda.EditorContext$MethodArgument
 outer org.netbeans.spi.debugger.jpda.EditorContext
cons public init(java.lang.String,java.lang.String,org.netbeans.spi.debugger.jpda.EditorContext$Position,org.netbeans.spi.debugger.jpda.EditorContext$Position)
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public org.netbeans.spi.debugger.jpda.EditorContext$Position getEndPosition()
meth public org.netbeans.spi.debugger.jpda.EditorContext$Position getStartPosition()
supr java.lang.Object
hfds endPos,name,startPos,type

CLSS public final static org.netbeans.spi.debugger.jpda.EditorContext$Operation
 outer org.netbeans.spi.debugger.jpda.EditorContext
meth public boolean equals(java.lang.Object)
meth public boolean isNative()
meth public int getBytecodeIndex()
meth public int hashCode()
meth public java.lang.String getMethodClassType()
meth public java.lang.String getMethodName()
meth public java.util.List<org.netbeans.spi.debugger.jpda.EditorContext$Operation> getNextOperations()
meth public org.netbeans.api.debugger.jpda.Variable getReturnValue()
meth public org.netbeans.spi.debugger.jpda.EditorContext$Position getEndPosition()
meth public org.netbeans.spi.debugger.jpda.EditorContext$Position getMethodEndPosition()
meth public org.netbeans.spi.debugger.jpda.EditorContext$Position getMethodStartPosition()
meth public org.netbeans.spi.debugger.jpda.EditorContext$Position getStartPosition()
meth public void setReturnValue(org.netbeans.api.debugger.jpda.Variable)
supr java.lang.Object
hfds bytecodeIndex,endPosition,isNative,methodClassType,methodDescriptor,methodEndPosition,methodName,methodStartPosition,nextOperations,returnValue,startPosition

CLSS public final static org.netbeans.spi.debugger.jpda.EditorContext$Position
 outer org.netbeans.spi.debugger.jpda.EditorContext
meth public boolean equals(java.lang.Object)
meth public int getColumn()
meth public int getLine()
meth public int getOffset()
meth public int hashCode()
supr java.lang.Object
hfds column,line,offset

CLSS public abstract interface static !annotation org.netbeans.spi.debugger.jpda.EditorContext$Registration
 outer org.netbeans.spi.debugger.jpda.EditorContext
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String path()

CLSS public abstract interface org.netbeans.spi.debugger.jpda.Evaluator<%0 extends java.lang.Object>
innr public abstract interface static !annotation Registration
innr public final static Context
innr public final static Expression
innr public final static Result
meth public abstract org.netbeans.spi.debugger.jpda.Evaluator$Result evaluate(org.netbeans.spi.debugger.jpda.Evaluator$Expression<{org.netbeans.spi.debugger.jpda.Evaluator%0}>,org.netbeans.spi.debugger.jpda.Evaluator$Context) throws org.netbeans.api.debugger.jpda.InvalidExpressionException

CLSS public final static org.netbeans.spi.debugger.jpda.Evaluator$Context
 outer org.netbeans.spi.debugger.jpda.Evaluator
cons public init(org.openide.util.Lookup)
meth public com.sun.jdi.ObjectReference getContextObject()
meth public com.sun.jdi.StackFrame getStackFrame()
meth public int getStackDepth()
meth public org.netbeans.api.debugger.jpda.CallStackFrame getCallStackFrame()
meth public org.netbeans.api.debugger.jpda.ObjectVariable getContextVariable()
meth public void notifyMethodToBeInvoked()
supr java.lang.Object
hfds callStackFrame,contextObject,contextVariable,methodToBeInvokedNotifier,stackDepth,stackFrame

CLSS public final static org.netbeans.spi.debugger.jpda.Evaluator$Expression<%0 extends java.lang.Object>
 outer org.netbeans.spi.debugger.jpda.Evaluator
cons public init(java.lang.String)
meth public java.lang.String getExpression()
meth public void setPreprocessedObject({org.netbeans.spi.debugger.jpda.Evaluator$Expression%0})
meth public {org.netbeans.spi.debugger.jpda.Evaluator$Expression%0} getPreprocessedObject()
supr java.lang.Object
hfds expression,preprocessed

CLSS public abstract interface static !annotation org.netbeans.spi.debugger.jpda.Evaluator$Registration
 outer org.netbeans.spi.debugger.jpda.Evaluator
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String language()

CLSS public final static org.netbeans.spi.debugger.jpda.Evaluator$Result
 outer org.netbeans.spi.debugger.jpda.Evaluator
cons public init(com.sun.jdi.Value)
cons public init(org.netbeans.api.debugger.jpda.Variable)
meth public com.sun.jdi.Value getValue()
meth public org.netbeans.api.debugger.jpda.Variable getVariable()
supr java.lang.Object
hfds v,var

CLSS public abstract org.netbeans.spi.debugger.jpda.SmartSteppingCallback
cons public init()
innr public abstract interface static !annotation Registration
innr public final static StopOrStep
meth public abstract boolean stopHere(org.netbeans.spi.debugger.ContextProvider,org.netbeans.api.debugger.jpda.JPDAThread,org.netbeans.api.debugger.jpda.SmartSteppingFilter)
meth public abstract void initFilter(org.netbeans.api.debugger.jpda.SmartSteppingFilter)
meth public org.netbeans.spi.debugger.jpda.SmartSteppingCallback$StopOrStep stopAt(org.netbeans.spi.debugger.ContextProvider,org.netbeans.api.debugger.jpda.CallStackFrame,org.netbeans.api.debugger.jpda.SmartSteppingFilter)
supr java.lang.Object
hcls ContextAware

CLSS public abstract interface static !annotation org.netbeans.spi.debugger.jpda.SmartSteppingCallback$Registration
 outer org.netbeans.spi.debugger.jpda.SmartSteppingCallback
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String path()

CLSS public final static org.netbeans.spi.debugger.jpda.SmartSteppingCallback$StopOrStep
 outer org.netbeans.spi.debugger.jpda.SmartSteppingCallback
meth public boolean equals(java.lang.Object)
meth public boolean isStop()
meth public int getStepDepth()
meth public int getStepSize()
meth public int hashCode()
meth public java.lang.String toString()
meth public static org.netbeans.spi.debugger.jpda.SmartSteppingCallback$StopOrStep skip()
meth public static org.netbeans.spi.debugger.jpda.SmartSteppingCallback$StopOrStep step(int,int)
meth public static org.netbeans.spi.debugger.jpda.SmartSteppingCallback$StopOrStep stop()
supr java.lang.Object
hfds SKIP,STOP,stepDepth,stepSize,stop

CLSS public abstract org.netbeans.spi.debugger.jpda.SourcePathProvider
cons public init()
fld public final static java.lang.String PROP_SOURCE_ROOTS = "sourceRoots"
innr public abstract interface static !annotation Registration
meth public abstract java.lang.String getRelativePath(java.lang.String,char,boolean)
meth public abstract java.lang.String getURL(java.lang.String,boolean)
meth public abstract java.lang.String[] getOriginalSourceRoots()
meth public abstract java.lang.String[] getSourceRoots()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setSourceRoots(java.lang.String[])
meth public java.lang.String getSourceRoot(java.lang.String)
supr java.lang.Object
hcls ContextAware

CLSS public abstract interface static !annotation org.netbeans.spi.debugger.jpda.SourcePathProvider$Registration
 outer org.netbeans.spi.debugger.jpda.SourcePathProvider
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String path()

CLSS public abstract org.netbeans.spi.debugger.jpda.VariablesFilter
cons public init()
innr public abstract interface static !annotation Registration
meth public abstract boolean isLeaf(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract boolean isReadOnly(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract int getChildrenCount(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object getValueAt(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.Object[] getChildren(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getDisplayName(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getIconBase(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String getShortDescription(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract java.lang.String[] getSupportedAncestors()
meth public abstract java.lang.String[] getSupportedTypes()
meth public abstract javax.swing.Action[] getActions(org.netbeans.spi.viewmodel.NodeActionsProvider,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void performDefaultAction(org.netbeans.spi.viewmodel.NodeActionsProvider,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public abstract void setValueAt(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
supr java.lang.Object
hcls ContextAware

CLSS public abstract interface static !annotation org.netbeans.spi.debugger.jpda.VariablesFilter$Registration
 outer org.netbeans.spi.debugger.jpda.VariablesFilter
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String path()

CLSS public abstract org.netbeans.spi.debugger.jpda.VariablesFilterAdapter
cons public init()
meth public abstract java.lang.String[] getSupportedAncestors()
meth public abstract java.lang.String[] getSupportedTypes()
meth public boolean isLeaf(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public boolean isReadOnly(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public int getChildrenCount(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.Object getValueAt(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.Object[] getChildren(org.netbeans.spi.viewmodel.TreeModel,org.netbeans.api.debugger.jpda.Variable,int,int) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getDisplayName(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getIconBase(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public java.lang.String getShortDescription(org.netbeans.spi.viewmodel.NodeModel,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public javax.swing.Action[] getActions(org.netbeans.spi.viewmodel.NodeActionsProvider,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void performDefaultAction(org.netbeans.spi.viewmodel.NodeActionsProvider,org.netbeans.api.debugger.jpda.Variable) throws org.netbeans.spi.viewmodel.UnknownTypeException
meth public void setValueAt(org.netbeans.spi.viewmodel.TableModel,org.netbeans.api.debugger.jpda.Variable,java.lang.String,java.lang.Object) throws org.netbeans.spi.viewmodel.UnknownTypeException
supr org.netbeans.spi.debugger.jpda.VariablesFilter

