#Signature file v4.1
#Version 1.39

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

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

CLSS public abstract java.util.AbstractMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init()
innr public static SimpleEntry
innr public static SimpleImmutableEntry
intf java.util.Map<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>> entrySet()
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.String toString()
meth public java.util.Collection<{java.util.AbstractMap%1}> values()
meth public java.util.Set<{java.util.AbstractMap%0}> keySet()
meth public void clear()
meth public void putAll(java.util.Map<? extends {java.util.AbstractMap%0},? extends {java.util.AbstractMap%1}>)
meth public {java.util.AbstractMap%1} get(java.lang.Object)
meth public {java.util.AbstractMap%1} put({java.util.AbstractMap%0},{java.util.AbstractMap%1})
meth public {java.util.AbstractMap%1} remove(java.lang.Object)
supr java.lang.Object

CLSS public java.util.HashMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(int,float)
cons public init(java.util.Map<? extends {java.util.HashMap%0},? extends {java.util.HashMap%1}>)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.Map<{java.util.HashMap%0},{java.util.HashMap%1}>
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object,java.lang.Object)
meth public boolean replace({java.util.HashMap%0},{java.util.HashMap%1},{java.util.HashMap%1})
meth public int size()
meth public java.lang.Object clone()
meth public java.util.Collection<{java.util.HashMap%1}> values()
meth public java.util.Set<java.util.Map$Entry<{java.util.HashMap%0},{java.util.HashMap%1}>> entrySet()
meth public java.util.Set<{java.util.HashMap%0}> keySet()
meth public void clear()
meth public void forEach(java.util.function.BiConsumer<? super {java.util.HashMap%0},? super {java.util.HashMap%1}>)
meth public void putAll(java.util.Map<? extends {java.util.HashMap%0},? extends {java.util.HashMap%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.HashMap%0},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} compute({java.util.HashMap%0},java.util.function.BiFunction<? super {java.util.HashMap%0},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} computeIfAbsent({java.util.HashMap%0},java.util.function.Function<? super {java.util.HashMap%0},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} computeIfPresent({java.util.HashMap%0},java.util.function.BiFunction<? super {java.util.HashMap%0},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} get(java.lang.Object)
meth public {java.util.HashMap%1} getOrDefault(java.lang.Object,{java.util.HashMap%1})
meth public {java.util.HashMap%1} merge({java.util.HashMap%0},{java.util.HashMap%1},java.util.function.BiFunction<? super {java.util.HashMap%1},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} put({java.util.HashMap%0},{java.util.HashMap%1})
meth public {java.util.HashMap%1} putIfAbsent({java.util.HashMap%0},{java.util.HashMap%1})
meth public {java.util.HashMap%1} remove(java.lang.Object)
meth public {java.util.HashMap%1} replace({java.util.HashMap%0},{java.util.HashMap%1})
supr java.util.AbstractMap<{java.util.HashMap%0},{java.util.HashMap%1}>

CLSS public abstract interface java.util.Map<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public abstract interface static Entry
meth public abstract boolean containsKey(java.lang.Object)
meth public abstract boolean containsValue(java.lang.Object)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.util.Collection<{java.util.Map%1}> values()
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.Map%0},{java.util.Map%1}>> entrySet()
meth public abstract java.util.Set<{java.util.Map%0}> keySet()
meth public abstract void clear()
meth public abstract void putAll(java.util.Map<? extends {java.util.Map%0},? extends {java.util.Map%1}>)
meth public abstract {java.util.Map%1} get(java.lang.Object)
meth public abstract {java.util.Map%1} put({java.util.Map%0},{java.util.Map%1})
meth public abstract {java.util.Map%1} remove(java.lang.Object)
meth public boolean remove(java.lang.Object,java.lang.Object)
meth public boolean replace({java.util.Map%0},{java.util.Map%1},{java.util.Map%1})
meth public void forEach(java.util.function.BiConsumer<? super {java.util.Map%0},? super {java.util.Map%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} compute({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfAbsent({java.util.Map%0},java.util.function.Function<? super {java.util.Map%0},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfPresent({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} getOrDefault(java.lang.Object,{java.util.Map%1})
meth public {java.util.Map%1} merge({java.util.Map%0},{java.util.Map%1},java.util.function.BiFunction<? super {java.util.Map%1},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} putIfAbsent({java.util.Map%0},{java.util.Map%1})
meth public {java.util.Map%1} replace({java.util.Map%0},{java.util.Map%1})

CLSS public abstract interface org.json.simple.JSONAware
meth public abstract java.lang.String toJSONString()

CLSS public org.json.simple.JSONObject
cons public init()
cons public init(java.util.Map)
intf java.util.Map
intf org.json.simple.JSONAware
intf org.json.simple.JSONStreamAware
meth public java.lang.String toJSONString()
meth public java.lang.String toString()
meth public static java.lang.String escape(java.lang.String)
meth public static java.lang.String toJSONString(java.util.Map)
meth public static java.lang.String toString(java.lang.String,java.lang.Object)
meth public static void writeJSONString(java.util.Map,java.io.Writer) throws java.io.IOException
meth public void writeJSONString(java.io.Writer) throws java.io.IOException
supr java.util.HashMap
hfds serialVersionUID

CLSS public abstract interface org.json.simple.JSONStreamAware
meth public abstract void writeJSONString(java.io.Writer) throws java.io.IOException

CLSS public org.netbeans.lib.v8debug.JSONReader
meth public static org.netbeans.lib.v8debug.V8Event getEvent(org.json.simple.JSONObject)
meth public static org.netbeans.lib.v8debug.V8Request getRequest(org.json.simple.JSONObject)
meth public static org.netbeans.lib.v8debug.V8Response getResponse(org.json.simple.JSONObject)
meth public static org.netbeans.lib.v8debug.V8Type getType(org.json.simple.JSONObject)
supr java.lang.Object

CLSS public org.netbeans.lib.v8debug.JSONWriter
meth public static org.json.simple.JSONObject store(org.netbeans.lib.v8debug.V8Event)
meth public static org.json.simple.JSONObject store(org.netbeans.lib.v8debug.V8Request)
meth public static org.json.simple.JSONObject store(org.netbeans.lib.v8debug.V8Response)
supr java.lang.Object

CLSS public final org.netbeans.lib.v8debug.PropertyBoolean
cons public init(java.lang.Boolean)
meth public boolean getValue()
meth public boolean getValueOr(boolean)
meth public boolean hasValue()
supr java.lang.Object
hfds b

CLSS public final org.netbeans.lib.v8debug.PropertyLong
cons public init(java.lang.Long)
meth public boolean hasValue()
meth public long getValue()
meth public long getValueOr(long)
supr java.lang.Object
hfds l

CLSS public abstract org.netbeans.lib.v8debug.V8Arguments
cons protected init()
supr java.lang.Object

CLSS public abstract org.netbeans.lib.v8debug.V8Body
cons protected init()
supr java.lang.Object

CLSS public final org.netbeans.lib.v8debug.V8Breakpoint
cons public init(org.netbeans.lib.v8debug.V8Breakpoint$Type,org.netbeans.lib.v8debug.PropertyLong,java.lang.String,long,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.PropertyLong,long,boolean,java.lang.String,long,org.netbeans.lib.v8debug.V8Breakpoint$ActualLocation[])
innr public final static !enum Type
innr public final static ActualLocation
meth public boolean isActive()
meth public java.lang.String getCondition()
meth public java.lang.String getScriptName()
meth public long getHitCount()
meth public long getIgnoreCount()
meth public long getNumber()
meth public org.netbeans.lib.v8debug.PropertyLong getColumn()
meth public org.netbeans.lib.v8debug.PropertyLong getGroupId()
meth public org.netbeans.lib.v8debug.PropertyLong getLine()
meth public org.netbeans.lib.v8debug.PropertyLong getScriptId()
meth public org.netbeans.lib.v8debug.V8Breakpoint$ActualLocation[] getActualLocations()
meth public org.netbeans.lib.v8debug.V8Breakpoint$Type getType()
supr java.lang.Object
hfds active,actualLocations,column,condition,groupId,hitCount,ignoreCount,line,number,scriptId,scriptName,type

CLSS public final static org.netbeans.lib.v8debug.V8Breakpoint$ActualLocation
 outer org.netbeans.lib.v8debug.V8Breakpoint
cons public init(long,long,java.lang.String)
cons public init(long,long,long)
meth public java.lang.String getScriptName()
meth public long getColumn()
meth public long getLine()
meth public org.netbeans.lib.v8debug.PropertyLong getScriptId()
supr java.lang.Object
hfds column,line,scriptId,scriptName

CLSS public final static !enum org.netbeans.lib.v8debug.V8Breakpoint$Type
 outer org.netbeans.lib.v8debug.V8Breakpoint
fld public final static org.netbeans.lib.v8debug.V8Breakpoint$Type function
fld public final static org.netbeans.lib.v8debug.V8Breakpoint$Type scriptId
fld public final static org.netbeans.lib.v8debug.V8Breakpoint$Type scriptName
fld public final static org.netbeans.lib.v8debug.V8Breakpoint$Type scriptRegExp
meth public static org.netbeans.lib.v8debug.V8Breakpoint$Type valueOf(java.lang.String)
meth public static org.netbeans.lib.v8debug.V8Breakpoint$Type[] values()
supr java.lang.Enum<org.netbeans.lib.v8debug.V8Breakpoint$Type>

CLSS public final !enum org.netbeans.lib.v8debug.V8Command
fld public final static org.netbeans.lib.v8debug.V8Command Backtrace
fld public final static org.netbeans.lib.v8debug.V8Command Changebreakpoint
fld public final static org.netbeans.lib.v8debug.V8Command Changelive
fld public final static org.netbeans.lib.v8debug.V8Command Clearbreakpoint
fld public final static org.netbeans.lib.v8debug.V8Command Clearbreakpointgroup
fld public final static org.netbeans.lib.v8debug.V8Command Continue
fld public final static org.netbeans.lib.v8debug.V8Command Disconnect
fld public final static org.netbeans.lib.v8debug.V8Command Evaluate
fld public final static org.netbeans.lib.v8debug.V8Command Flags
fld public final static org.netbeans.lib.v8debug.V8Command Frame
fld public final static org.netbeans.lib.v8debug.V8Command Gc
fld public final static org.netbeans.lib.v8debug.V8Command Listbreakpoints
fld public final static org.netbeans.lib.v8debug.V8Command Lookup
fld public final static org.netbeans.lib.v8debug.V8Command References
fld public final static org.netbeans.lib.v8debug.V8Command Restartframe
fld public final static org.netbeans.lib.v8debug.V8Command Scope
fld public final static org.netbeans.lib.v8debug.V8Command Scopes
fld public final static org.netbeans.lib.v8debug.V8Command Scripts
fld public final static org.netbeans.lib.v8debug.V8Command SetVariableValue
fld public final static org.netbeans.lib.v8debug.V8Command Setbreakpoint
fld public final static org.netbeans.lib.v8debug.V8Command Setexceptionbreak
fld public final static org.netbeans.lib.v8debug.V8Command Source
fld public final static org.netbeans.lib.v8debug.V8Command Suspend
fld public final static org.netbeans.lib.v8debug.V8Command Threads
fld public final static org.netbeans.lib.v8debug.V8Command V8flags
fld public final static org.netbeans.lib.v8debug.V8Command Version
meth public java.lang.String toString()
meth public static org.netbeans.lib.v8debug.V8Command fromString(java.lang.String)
meth public static org.netbeans.lib.v8debug.V8Command valueOf(java.lang.String)
meth public static org.netbeans.lib.v8debug.V8Command[] values()
supr java.lang.Enum<org.netbeans.lib.v8debug.V8Command>

CLSS public final org.netbeans.lib.v8debug.V8Event
cons public init(long,org.netbeans.lib.v8debug.V8Event$Kind,org.netbeans.lib.v8debug.V8Body,org.netbeans.lib.v8debug.vars.ReferencedValue[],java.lang.Boolean,java.lang.Boolean,java.lang.String)
innr public final static !enum Kind
meth public java.lang.String getErrorMessage()
meth public org.netbeans.lib.v8debug.PropertyBoolean getSuccess()
meth public org.netbeans.lib.v8debug.PropertyBoolean isRunning()
meth public org.netbeans.lib.v8debug.V8Body getBody()
meth public org.netbeans.lib.v8debug.V8Event$Kind getKind()
meth public org.netbeans.lib.v8debug.vars.ReferencedValue[] getReferencedValues()
meth public org.netbeans.lib.v8debug.vars.V8Value getReferencedValue(long)
supr org.netbeans.lib.v8debug.V8Packet
hfds body,errorMessage,eventKind,referencedValues,running,success,valuesByReferences

CLSS public final static !enum org.netbeans.lib.v8debug.V8Event$Kind
 outer org.netbeans.lib.v8debug.V8Event
fld public final static org.netbeans.lib.v8debug.V8Event$Kind AfterCompile
fld public final static org.netbeans.lib.v8debug.V8Event$Kind Break
fld public final static org.netbeans.lib.v8debug.V8Event$Kind CompileError
fld public final static org.netbeans.lib.v8debug.V8Event$Kind Exception
fld public final static org.netbeans.lib.v8debug.V8Event$Kind ScriptCollected
meth public java.lang.String toString()
meth public static org.netbeans.lib.v8debug.V8Event$Kind valueOf(java.lang.String)
meth public static org.netbeans.lib.v8debug.V8Event$Kind[] values()
supr java.lang.Enum<org.netbeans.lib.v8debug.V8Event$Kind>

CLSS public final !enum org.netbeans.lib.v8debug.V8ExceptionBreakType
fld public final static org.netbeans.lib.v8debug.V8ExceptionBreakType all
fld public final static org.netbeans.lib.v8debug.V8ExceptionBreakType uncaught
meth public static org.netbeans.lib.v8debug.V8ExceptionBreakType valueOf(java.lang.String)
meth public static org.netbeans.lib.v8debug.V8ExceptionBreakType[] values()
supr java.lang.Enum<org.netbeans.lib.v8debug.V8ExceptionBreakType>

CLSS public final org.netbeans.lib.v8debug.V8Frame
cons public init(java.lang.Long,org.netbeans.lib.v8debug.vars.ReferencedValue,org.netbeans.lib.v8debug.vars.ReferencedValue<org.netbeans.lib.v8debug.vars.V8Function>,long,boolean,boolean,boolean,java.util.Map<java.lang.String,org.netbeans.lib.v8debug.vars.ReferencedValue>,java.util.Map<java.lang.String,org.netbeans.lib.v8debug.vars.ReferencedValue>,long,long,long,java.lang.String,org.netbeans.lib.v8debug.V8Scope[],java.lang.String)
meth public boolean isAtReturn()
meth public boolean isConstructCall()
meth public boolean isDebuggerFrame()
meth public java.lang.String getSourceLineText()
meth public java.lang.String getText()
meth public java.util.Map<java.lang.String,org.netbeans.lib.v8debug.vars.ReferencedValue> getArgumentRefs()
meth public java.util.Map<java.lang.String,org.netbeans.lib.v8debug.vars.ReferencedValue> getLocalRefs()
meth public long getColumn()
meth public long getLine()
meth public long getPosition()
meth public long getScriptRef()
meth public org.netbeans.lib.v8debug.PropertyLong getIndex()
meth public org.netbeans.lib.v8debug.V8Scope[] getScopes()
meth public org.netbeans.lib.v8debug.vars.ReferencedValue getReceiver()
meth public org.netbeans.lib.v8debug.vars.ReferencedValue<org.netbeans.lib.v8debug.vars.V8Function> getFunction()
supr java.lang.Object
hfds argumentRefs,atReturn,column,constructCall,debuggerFrame,func,index,line,localRefs,position,receiver,scopes,scriptRef,sourceLineText,text

CLSS public abstract org.netbeans.lib.v8debug.V8Packet
cons protected init(long,org.netbeans.lib.v8debug.V8Type)
meth public long getSequence()
meth public org.netbeans.lib.v8debug.V8Type getType()
supr java.lang.Object
hfds SEQ,TYPE,sequence,type

CLSS public final org.netbeans.lib.v8debug.V8Request
cons public init(long,org.netbeans.lib.v8debug.V8Command,org.netbeans.lib.v8debug.V8Arguments)
meth public org.netbeans.lib.v8debug.V8Arguments getArguments()
meth public org.netbeans.lib.v8debug.V8Command getCommand()
meth public org.netbeans.lib.v8debug.V8Response createErrorResponse(long,boolean,java.lang.String)
meth public org.netbeans.lib.v8debug.V8Response createSuccessResponse(long,org.netbeans.lib.v8debug.V8Body,org.netbeans.lib.v8debug.vars.ReferencedValue[],boolean)
supr org.netbeans.lib.v8debug.V8Packet
hfds arguments,command

CLSS public final org.netbeans.lib.v8debug.V8Response
meth public boolean isRunning()
meth public boolean isSuccess()
meth public java.lang.String getErrorMessage()
meth public long getRequestSequence()
meth public org.netbeans.lib.v8debug.V8Body getBody()
meth public org.netbeans.lib.v8debug.V8Command getCommand()
meth public org.netbeans.lib.v8debug.vars.ReferencedValue[] getReferencedValues()
meth public org.netbeans.lib.v8debug.vars.V8Value getReferencedValue(long)
supr org.netbeans.lib.v8debug.V8Packet
hfds body,command,errorMessage,referencedValues,requestSequence,running,success,valuesByReferences

CLSS public final org.netbeans.lib.v8debug.V8Scope
cons public init(long,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.V8Scope$Type,org.netbeans.lib.v8debug.vars.ReferencedValue<org.netbeans.lib.v8debug.vars.V8Object>,java.lang.String)
innr public final static !enum Type
meth public java.lang.String getText()
meth public long getIndex()
meth public org.netbeans.lib.v8debug.PropertyLong getFrameIndex()
meth public org.netbeans.lib.v8debug.V8Scope$Type getType()
meth public org.netbeans.lib.v8debug.vars.ReferencedValue<org.netbeans.lib.v8debug.vars.V8Object> getObject()
supr java.lang.Object
hfds frameIndex,index,object,text,type

CLSS public final static !enum org.netbeans.lib.v8debug.V8Scope$Type
 outer org.netbeans.lib.v8debug.V8Scope
fld public final static org.netbeans.lib.v8debug.V8Scope$Type Block
fld public final static org.netbeans.lib.v8debug.V8Scope$Type Catch
fld public final static org.netbeans.lib.v8debug.V8Scope$Type Closure
fld public final static org.netbeans.lib.v8debug.V8Scope$Type Global
fld public final static org.netbeans.lib.v8debug.V8Scope$Type Local
fld public final static org.netbeans.lib.v8debug.V8Scope$Type Module
fld public final static org.netbeans.lib.v8debug.V8Scope$Type With
meth public static org.netbeans.lib.v8debug.V8Scope$Type valueOf(int)
meth public static org.netbeans.lib.v8debug.V8Scope$Type valueOf(java.lang.String)
meth public static org.netbeans.lib.v8debug.V8Scope$Type[] values()
supr java.lang.Enum<org.netbeans.lib.v8debug.V8Scope$Type>

CLSS public final org.netbeans.lib.v8debug.V8Script
cons public init(long,java.lang.String,long,long,long,long,java.lang.Object,java.lang.String,java.lang.String,java.lang.Long,org.netbeans.lib.v8debug.vars.ReferencedValue,java.lang.String,org.netbeans.lib.v8debug.V8Script$Type,org.netbeans.lib.v8debug.V8Script$CompilationType,org.netbeans.lib.v8debug.vars.ReferencedValue,org.netbeans.lib.v8debug.V8Script$EvalFromLocation)
innr public final static !enum CompilationType
innr public final static !enum Type
innr public final static EvalFromLocation
innr public final static Types
meth public java.lang.Object getData()
meth public java.lang.String getName()
meth public java.lang.String getSource()
meth public java.lang.String getSourceStart()
meth public long getColumnOffset()
meth public long getId()
meth public long getLineCount()
meth public long getLineOffset()
meth public org.netbeans.lib.v8debug.PropertyLong getSourceLength()
meth public org.netbeans.lib.v8debug.V8Script$CompilationType getCompilationType()
meth public org.netbeans.lib.v8debug.V8Script$EvalFromLocation getEvalFromLocation()
meth public org.netbeans.lib.v8debug.V8Script$Type getScriptType()
meth public org.netbeans.lib.v8debug.vars.ReferencedValue getContext()
meth public org.netbeans.lib.v8debug.vars.ReferencedValue getEvalFromScript()
supr org.netbeans.lib.v8debug.vars.V8Value
hfds columnOffset,compilationType,context,data,evalFromLocation,evalFromScript,id,lineCount,lineOffset,name,scriptType,source,sourceLength,sourceStart

CLSS public final static !enum org.netbeans.lib.v8debug.V8Script$CompilationType
 outer org.netbeans.lib.v8debug.V8Script
fld public final static org.netbeans.lib.v8debug.V8Script$CompilationType API
fld public final static org.netbeans.lib.v8debug.V8Script$CompilationType EVAL
meth public static org.netbeans.lib.v8debug.V8Script$CompilationType valueOf(int)
meth public static org.netbeans.lib.v8debug.V8Script$CompilationType valueOf(java.lang.String)
meth public static org.netbeans.lib.v8debug.V8Script$CompilationType[] values()
supr java.lang.Enum<org.netbeans.lib.v8debug.V8Script$CompilationType>

CLSS public final static org.netbeans.lib.v8debug.V8Script$EvalFromLocation
 outer org.netbeans.lib.v8debug.V8Script
cons public init(long,long)
meth public long getColumn()
meth public long getLine()
supr java.lang.Object
hfds column,line

CLSS public final static !enum org.netbeans.lib.v8debug.V8Script$Type
 outer org.netbeans.lib.v8debug.V8Script
fld public final static org.netbeans.lib.v8debug.V8Script$Type EXTENSION
fld public final static org.netbeans.lib.v8debug.V8Script$Type NATIVE
fld public final static org.netbeans.lib.v8debug.V8Script$Type NORMAL
meth public static org.netbeans.lib.v8debug.V8Script$Type valueOf(int)
meth public static org.netbeans.lib.v8debug.V8Script$Type valueOf(java.lang.String)
meth public static org.netbeans.lib.v8debug.V8Script$Type[] values()
supr java.lang.Enum<org.netbeans.lib.v8debug.V8Script$Type>

CLSS public final static org.netbeans.lib.v8debug.V8Script$Types
 outer org.netbeans.lib.v8debug.V8Script
cons public init(boolean,boolean,boolean)
cons public init(int)
meth public int getIntTypes()
meth public java.util.Set<org.netbeans.lib.v8debug.V8Script$Type> getTypes()
supr java.lang.Object
hfds type

CLSS public final org.netbeans.lib.v8debug.V8ScriptLocation
cons public init(long,java.lang.String,long,long,long)
meth public java.lang.String getName()
meth public long getColumn()
meth public long getId()
meth public long getLine()
meth public long getLineCount()
supr java.lang.Object
hfds column,id,line,lineCount,name

CLSS public final !enum org.netbeans.lib.v8debug.V8StepAction
fld public final static org.netbeans.lib.v8debug.V8StepAction in
fld public final static org.netbeans.lib.v8debug.V8StepAction next
fld public final static org.netbeans.lib.v8debug.V8StepAction out
meth public static org.netbeans.lib.v8debug.V8StepAction valueOf(java.lang.String)
meth public static org.netbeans.lib.v8debug.V8StepAction[] values()
supr java.lang.Enum<org.netbeans.lib.v8debug.V8StepAction>

CLSS public final !enum org.netbeans.lib.v8debug.V8Type
fld public final static org.netbeans.lib.v8debug.V8Type event
fld public final static org.netbeans.lib.v8debug.V8Type request
fld public final static org.netbeans.lib.v8debug.V8Type response
meth public static org.netbeans.lib.v8debug.V8Type valueOf(java.lang.String)
meth public static org.netbeans.lib.v8debug.V8Type[] values()
supr java.lang.Enum<org.netbeans.lib.v8debug.V8Type>

CLSS public final org.netbeans.lib.v8debug.commands.Backtrace
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.Boolean)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.Backtrace$Arguments
 outer org.netbeans.lib.v8debug.commands.Backtrace
cons public init(java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.Boolean)
meth public org.netbeans.lib.v8debug.PropertyBoolean isBottom()
meth public org.netbeans.lib.v8debug.PropertyBoolean isInlineRefs()
meth public org.netbeans.lib.v8debug.PropertyLong getFromFrame()
meth public org.netbeans.lib.v8debug.PropertyLong getToFrame()
supr org.netbeans.lib.v8debug.V8Arguments
hfds bottom,fromFrame,inlineRefs,toFrame

CLSS public final static org.netbeans.lib.v8debug.commands.Backtrace$ResponseBody
 outer org.netbeans.lib.v8debug.commands.Backtrace
cons public init(long,long,long,org.netbeans.lib.v8debug.V8Frame[])
meth public long getFromFrame()
meth public long getToFrame()
meth public long getTotalFrames()
meth public org.netbeans.lib.v8debug.V8Frame[] getFrames()
supr org.netbeans.lib.v8debug.V8Body
hfds frames,fromFrame,toFrame,totalFrames

CLSS public final org.netbeans.lib.v8debug.commands.ChangeBreakpoint
innr public final static Arguments
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,long,java.lang.Boolean,java.lang.String,java.lang.Long)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.ChangeBreakpoint$Arguments
 outer org.netbeans.lib.v8debug.commands.ChangeBreakpoint
cons public init(long,java.lang.Boolean,java.lang.String,java.lang.Long)
meth public java.lang.String getCondition()
meth public long getBreakpoint()
meth public org.netbeans.lib.v8debug.PropertyBoolean isEnabled()
meth public org.netbeans.lib.v8debug.PropertyLong getIgnoreCount()
supr org.netbeans.lib.v8debug.V8Arguments
hfds breakpoint,condition,enabled,ignoreCount

CLSS public final org.netbeans.lib.v8debug.commands.ChangeLive
innr public final static Arguments
innr public final static ChangeLog
innr public final static ResponseBody
innr public final static Result
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,long,java.lang.String)
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,long,java.lang.String,java.lang.Boolean)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.ChangeLive$Arguments
 outer org.netbeans.lib.v8debug.commands.ChangeLive
cons public init(long,java.lang.String,java.lang.Boolean)
meth public java.lang.String getNewSource()
meth public long getScriptId()
meth public org.netbeans.lib.v8debug.PropertyBoolean isPreviewOnly()
supr org.netbeans.lib.v8debug.V8Arguments
hfds newSource,previewOnly,scriptId

CLSS public final static org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog
 outer org.netbeans.lib.v8debug.commands.ChangeLive
cons public init(org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate[],java.lang.String[],java.lang.String[],org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$FunctionPatched,org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$PositionPatched[])
innr public final static BreakpointUpdate
innr public final static FunctionPatched
innr public final static PositionPatched
meth public java.lang.String[] getDroppedFrames()
meth public java.lang.String[] getNamesLinkedToOldScript()
meth public org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate[] getBreakpointsUpdate()
meth public org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$FunctionPatched getFunctionPatched()
meth public org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$PositionPatched[] getPatchedPositions()
supr java.lang.Object
hfds breakpointsUpdate,droppedFrames,functionPatched,namesLinkedToOldScript,patchedPositions

CLSS public final static org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate
 outer org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog
cons public init(org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate$Type,long,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate$Position,org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate$Position)
innr public final static !enum Type
innr public final static Position
meth public long getId()
meth public org.netbeans.lib.v8debug.PropertyLong getNewId()
meth public org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate$Position getNewPositions()
meth public org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate$Position getOldPositions()
meth public org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate$Type getType()
supr java.lang.Object
hfds id,newId,newPositions,oldPositions,type

CLSS public final static org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate$Position
 outer org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate
cons public init(long,long,long)
meth public long getColumn()
meth public long getLine()
meth public long getPosition()
supr java.lang.Object
hfds column,line,position

CLSS public final static !enum org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate$Type
 outer org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate
fld public final static org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate$Type CopiedToOld
fld public final static org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate$Type PositionChanged
meth public static org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate$Type fromString(java.lang.String)
meth public static org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate$Type valueOf(java.lang.String)
meth public static org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate$Type[] values()
supr java.lang.Enum<org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$BreakpointUpdate$Type>

CLSS public final static org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$FunctionPatched
 outer org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog
cons public init(java.lang.String,org.netbeans.lib.v8debug.PropertyBoolean)
meth public java.lang.String getFunction()
meth public org.netbeans.lib.v8debug.PropertyBoolean getFunctionInfoNotFound()
supr java.lang.Object
hfds function,functionInfoNotFound

CLSS public final static org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog$PositionPatched
 outer org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog
cons public init(java.lang.String,org.netbeans.lib.v8debug.PropertyBoolean)
meth public java.lang.String getName()
meth public org.netbeans.lib.v8debug.PropertyBoolean getInfoNotFound()
supr java.lang.Object
hfds infoNotFound,name

CLSS public final static org.netbeans.lib.v8debug.commands.ChangeLive$ResponseBody
 outer org.netbeans.lib.v8debug.commands.ChangeLive
cons public init(org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog,org.netbeans.lib.v8debug.commands.ChangeLive$Result,java.lang.Boolean)
meth public org.netbeans.lib.v8debug.PropertyBoolean getStepInRecommended()
meth public org.netbeans.lib.v8debug.commands.ChangeLive$ChangeLog getChangeLog()
meth public org.netbeans.lib.v8debug.commands.ChangeLive$Result getResult()
supr org.netbeans.lib.v8debug.V8Body
hfds changeLog,result,stepInRecommended

CLSS public final static org.netbeans.lib.v8debug.commands.ChangeLive$Result
 outer org.netbeans.lib.v8debug.commands.ChangeLive
cons public init(org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree,org.netbeans.lib.v8debug.commands.ChangeLive$Result$TextualDiff,boolean,java.lang.Boolean,java.lang.Boolean,java.lang.String)
innr public final static ChangeTree
innr public final static TextualDiff
meth public boolean isUpdated()
meth public java.lang.String getCreatedScriptName()
meth public org.netbeans.lib.v8debug.PropertyBoolean getStackModified()
meth public org.netbeans.lib.v8debug.PropertyBoolean getStackUpdateNeedsStepIn()
meth public org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree getChangeTree()
meth public org.netbeans.lib.v8debug.commands.ChangeLive$Result$TextualDiff getDiff()
supr java.lang.Object
hfds changeTree,createdScriptName,diff,stackModified,stackUpdateNeedsStepIn,updated

CLSS public final static org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree
 outer org.netbeans.lib.v8debug.commands.ChangeLive$Result
cons public init(java.lang.String,org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree$Positions,org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree$Positions,org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree$FunctionStatus,java.lang.String,org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree[],org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree[])
innr public final static !enum FunctionStatus
innr public final static Positions
meth public java.lang.String getName()
meth public java.lang.String getStatusExplanation()
meth public org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree$FunctionStatus getStatus()
meth public org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree$Positions getNewPositions()
meth public org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree$Positions getPositions()
meth public org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree[] getChildren()
meth public org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree[] getNewChildren()
supr java.lang.Object
hfds children,name,newChildren,newPositions,positions,status,statusExplanation

CLSS public final static !enum org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree$FunctionStatus
 outer org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree
fld public final static org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree$FunctionStatus Changed
fld public final static org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree$FunctionStatus Damaged
fld public final static org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree$FunctionStatus SourceChanged
fld public final static org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree$FunctionStatus Unchanged
meth public java.lang.String toString()
meth public static org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree$FunctionStatus fromString(java.lang.String)
meth public static org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree$FunctionStatus valueOf(java.lang.String)
meth public static org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree$FunctionStatus[] values()
supr java.lang.Enum<org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree$FunctionStatus>

CLSS public final static org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree$Positions
 outer org.netbeans.lib.v8debug.commands.ChangeLive$Result$ChangeTree
cons public init(long,long)
meth public long getEndPosition()
meth public long getStartPosition()
supr java.lang.Object
hfds endPosition,startPosition

CLSS public final static org.netbeans.lib.v8debug.commands.ChangeLive$Result$TextualDiff
 outer org.netbeans.lib.v8debug.commands.ChangeLive$Result
cons public init(long,long,long[])
meth public long getNewLength()
meth public long getOldLength()
meth public long[] getChunks()
supr java.lang.Object
hfds chunks,newLength,oldLength

CLSS public final org.netbeans.lib.v8debug.commands.ClearBreakpoint
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,long)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.ClearBreakpoint$Arguments
 outer org.netbeans.lib.v8debug.commands.ClearBreakpoint
cons public init(long)
meth public long getBreakpoint()
supr org.netbeans.lib.v8debug.V8Arguments
hfds breakpoint

CLSS public final static org.netbeans.lib.v8debug.commands.ClearBreakpoint$ResponseBody
 outer org.netbeans.lib.v8debug.commands.ClearBreakpoint
cons public init(long)
meth public long getBreakpoint()
supr org.netbeans.lib.v8debug.V8Body
hfds breakpoint

CLSS public final org.netbeans.lib.v8debug.commands.ClearBreakpointGroup
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,long)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.ClearBreakpointGroup$Arguments
 outer org.netbeans.lib.v8debug.commands.ClearBreakpointGroup
cons public init(long)
meth public long getGroupId()
supr org.netbeans.lib.v8debug.V8Arguments
hfds groupId

CLSS public final static org.netbeans.lib.v8debug.commands.ClearBreakpointGroup$ResponseBody
 outer org.netbeans.lib.v8debug.commands.ClearBreakpointGroup
cons public init(long[])
meth public long[] getBreakpointsCleared()
supr org.netbeans.lib.v8debug.V8Body
hfds breakpointsCleared

CLSS public final org.netbeans.lib.v8debug.commands.Continue
innr public final static Arguments
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long)
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,org.netbeans.lib.v8debug.V8StepAction)
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,org.netbeans.lib.v8debug.V8StepAction,long)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.Continue$Arguments
 outer org.netbeans.lib.v8debug.commands.Continue
cons public init(org.netbeans.lib.v8debug.V8StepAction)
cons public init(org.netbeans.lib.v8debug.V8StepAction,java.lang.Long)
meth public org.netbeans.lib.v8debug.PropertyLong getStepCount()
meth public org.netbeans.lib.v8debug.V8StepAction getStepAction()
supr org.netbeans.lib.v8debug.V8Arguments
hfds stepAction,stepCount

CLSS public final org.netbeans.lib.v8debug.commands.Evaluate
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,java.lang.String)
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,java.lang.String,java.lang.Long,java.lang.Boolean,java.lang.Boolean,org.netbeans.lib.v8debug.commands.Evaluate$Arguments$Context[])
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.Evaluate$Arguments
 outer org.netbeans.lib.v8debug.commands.Evaluate
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Long,java.lang.Boolean,java.lang.Boolean,org.netbeans.lib.v8debug.commands.Evaluate$Arguments$Context[])
innr public final static Context
meth public java.lang.String getExpression()
meth public org.netbeans.lib.v8debug.PropertyBoolean isDisableBreak()
meth public org.netbeans.lib.v8debug.PropertyBoolean isGlobal()
meth public org.netbeans.lib.v8debug.PropertyLong getFrame()
meth public org.netbeans.lib.v8debug.commands.Evaluate$Arguments$Context[] getAdditionalContext()
supr org.netbeans.lib.v8debug.V8Arguments
hfds additionalContext,disableBreak,expression,frame,global

CLSS public final static org.netbeans.lib.v8debug.commands.Evaluate$Arguments$Context
 outer org.netbeans.lib.v8debug.commands.Evaluate$Arguments
cons public init(java.lang.String,long)
meth public java.lang.String getName()
meth public long getHandle()
supr java.lang.Object
hfds handle,name

CLSS public final static org.netbeans.lib.v8debug.commands.Evaluate$ResponseBody
 outer org.netbeans.lib.v8debug.commands.Evaluate
cons public init(org.netbeans.lib.v8debug.vars.V8Value)
meth public org.netbeans.lib.v8debug.vars.V8Value getValue()
supr org.netbeans.lib.v8debug.V8Body
hfds value

CLSS public final org.netbeans.lib.v8debug.commands.Flags
fld public final static java.lang.String FLAG_BREAK_ON_CAUGHT_EXCEPTION = "breakOnCaughtException"
fld public final static java.lang.String FLAG_BREAK_ON_UNCAUGHT_EXCEPTION = "breakOnUncaughtException"
fld public final static java.lang.String FLAG_BREAK_POINTS_ACTIVE = "breakPointsActive"
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long)
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,java.lang.String,boolean)
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,java.util.Map<java.lang.String,java.lang.Boolean>)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.Flags$Arguments
 outer org.netbeans.lib.v8debug.commands.Flags
cons public init()
cons public init(java.lang.String,boolean)
cons public init(java.util.Map<java.lang.String,java.lang.Boolean>)
meth public java.util.Map<java.lang.String,java.lang.Boolean> getFlags()
supr org.netbeans.lib.v8debug.V8Arguments
hfds flags

CLSS public final static org.netbeans.lib.v8debug.commands.Flags$ResponseBody
 outer org.netbeans.lib.v8debug.commands.Flags
cons public init()
cons public init(java.util.Map<java.lang.String,java.lang.Boolean>)
meth public java.util.Map<java.lang.String,java.lang.Boolean> getFlags()
supr org.netbeans.lib.v8debug.V8Body
hfds flags

CLSS public final org.netbeans.lib.v8debug.commands.Frame
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,java.lang.Long)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.Frame$Arguments
 outer org.netbeans.lib.v8debug.commands.Frame
cons public init(java.lang.Long)
meth public org.netbeans.lib.v8debug.PropertyLong getFrameNumber()
supr org.netbeans.lib.v8debug.V8Arguments
hfds frameNumber

CLSS public final static org.netbeans.lib.v8debug.commands.Frame$ResponseBody
 outer org.netbeans.lib.v8debug.commands.Frame
cons public init(org.netbeans.lib.v8debug.V8Frame)
meth public org.netbeans.lib.v8debug.V8Frame getFrame()
supr org.netbeans.lib.v8debug.V8Body
hfds frame

CLSS public final org.netbeans.lib.v8debug.commands.GC
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long)
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,java.lang.String)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.GC$Arguments
 outer org.netbeans.lib.v8debug.commands.GC
cons public init(java.lang.String)
meth public java.lang.String getType()
supr org.netbeans.lib.v8debug.V8Arguments
hfds type

CLSS public final static org.netbeans.lib.v8debug.commands.GC$ResponseBody
 outer org.netbeans.lib.v8debug.commands.GC
cons public init(long,long)
meth public long getAfter()
meth public long getBefore()
supr org.netbeans.lib.v8debug.V8Body
hfds after,before

CLSS public final org.netbeans.lib.v8debug.commands.ListBreakpoints
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.ListBreakpoints$ResponseBody
 outer org.netbeans.lib.v8debug.commands.ListBreakpoints
cons public init(org.netbeans.lib.v8debug.V8Breakpoint[],boolean,boolean)
meth public boolean isBreakOnExceptions()
meth public boolean isBreakOnUncaughtExceptions()
meth public org.netbeans.lib.v8debug.V8Breakpoint[] getBreakpoints()
supr org.netbeans.lib.v8debug.V8Body
hfds breakOnExceptions,breakOnUncaughtExceptions,breakpoints

CLSS public final org.netbeans.lib.v8debug.commands.Lookup
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,long[],java.lang.Boolean)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.Lookup$Arguments
 outer org.netbeans.lib.v8debug.commands.Lookup
cons public init(long[],java.lang.Boolean)
meth public long[] getHandles()
meth public org.netbeans.lib.v8debug.PropertyBoolean isIncludeSource()
supr org.netbeans.lib.v8debug.V8Arguments
hfds handles,includeSource

CLSS public final static org.netbeans.lib.v8debug.commands.Lookup$ResponseBody
 outer org.netbeans.lib.v8debug.commands.Lookup
cons public init(java.util.Map<java.lang.Long,org.netbeans.lib.v8debug.vars.V8Value>)
meth public java.util.Map<java.lang.Long,org.netbeans.lib.v8debug.vars.V8Value> getValuesByHandle()
supr org.netbeans.lib.v8debug.V8Body
hfds valuesByHandle

CLSS public final org.netbeans.lib.v8debug.commands.References
innr public final static !enum Type
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,org.netbeans.lib.v8debug.commands.References$Type,long)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.References$Arguments
 outer org.netbeans.lib.v8debug.commands.References
cons public init(org.netbeans.lib.v8debug.commands.References$Type,long)
meth public long getHandle()
meth public org.netbeans.lib.v8debug.commands.References$Type getType()
supr org.netbeans.lib.v8debug.V8Arguments
hfds handle,type

CLSS public final static org.netbeans.lib.v8debug.commands.References$ResponseBody
 outer org.netbeans.lib.v8debug.commands.References
cons public init(org.netbeans.lib.v8debug.vars.V8Value[])
meth public org.netbeans.lib.v8debug.vars.V8Value[] getReferences()
supr org.netbeans.lib.v8debug.V8Body
hfds references

CLSS public final static !enum org.netbeans.lib.v8debug.commands.References$Type
 outer org.netbeans.lib.v8debug.commands.References
fld public final static org.netbeans.lib.v8debug.commands.References$Type constructedBy
fld public final static org.netbeans.lib.v8debug.commands.References$Type referencedBy
meth public static org.netbeans.lib.v8debug.commands.References$Type valueOf(java.lang.String)
meth public static org.netbeans.lib.v8debug.commands.References$Type[] values()
supr java.lang.Enum<org.netbeans.lib.v8debug.commands.References$Type>

CLSS public org.netbeans.lib.v8debug.commands.RestartFrame
fld public final static java.lang.String RESULT_STACK_UPDATE_NEEDS_STEP_IN = "stack_update_needs_step_in"
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long)
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,long)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.RestartFrame$Arguments
 outer org.netbeans.lib.v8debug.commands.RestartFrame
cons public init(java.lang.Long)
meth public org.netbeans.lib.v8debug.PropertyLong getFrame()
supr org.netbeans.lib.v8debug.V8Arguments
hfds frame

CLSS public final static org.netbeans.lib.v8debug.commands.RestartFrame$ResponseBody
 outer org.netbeans.lib.v8debug.commands.RestartFrame
cons public init(java.util.Map<java.lang.String,java.lang.Object>)
meth public java.util.Map<java.lang.String,java.lang.Object> getResult()
supr org.netbeans.lib.v8debug.V8Body
hfds result

CLSS public final org.netbeans.lib.v8debug.commands.Scope
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long)
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,long,java.lang.Long)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.Scope$Arguments
 outer org.netbeans.lib.v8debug.commands.Scope
cons public init(long)
cons public init(long,java.lang.Long)
meth public long getScopeNumber()
meth public org.netbeans.lib.v8debug.PropertyLong getFrameNumber()
supr org.netbeans.lib.v8debug.V8Arguments
hfds frameNumber,scopeNumber

CLSS public final static org.netbeans.lib.v8debug.commands.Scope$ResponseBody
 outer org.netbeans.lib.v8debug.commands.Scope
cons public init(org.netbeans.lib.v8debug.V8Scope)
meth public org.netbeans.lib.v8debug.V8Scope getScope()
supr org.netbeans.lib.v8debug.V8Body
hfds scope

CLSS public final org.netbeans.lib.v8debug.commands.Scopes
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long)
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,long)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.Scopes$Arguments
 outer org.netbeans.lib.v8debug.commands.Scopes
cons public init(java.lang.Long)
meth public org.netbeans.lib.v8debug.PropertyLong getFrameNumber()
supr org.netbeans.lib.v8debug.V8Arguments
hfds frameNumber

CLSS public final static org.netbeans.lib.v8debug.commands.Scopes$ResponseBody
 outer org.netbeans.lib.v8debug.commands.Scopes
cons public init(long,long,long,org.netbeans.lib.v8debug.V8Scope[])
meth public long getFromScope()
meth public long getToScope()
meth public long getTotalScopes()
meth public org.netbeans.lib.v8debug.V8Scope[] getScopes()
supr org.netbeans.lib.v8debug.V8Body
hfds fromScope,scopes,toScope,totalScopes

CLSS public final org.netbeans.lib.v8debug.commands.Scripts
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long)
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,org.netbeans.lib.v8debug.V8Script$Types,java.lang.Boolean)
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,org.netbeans.lib.v8debug.V8Script$Types,long[],java.lang.Boolean,java.lang.String)
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,org.netbeans.lib.v8debug.V8Script$Types,long[],java.lang.Boolean,long)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.Scripts$Arguments
 outer org.netbeans.lib.v8debug.commands.Scripts
cons public init(org.netbeans.lib.v8debug.V8Script$Types,long[],java.lang.Boolean,java.lang.String)
cons public init(org.netbeans.lib.v8debug.V8Script$Types,long[],java.lang.Boolean,long)
meth public java.lang.String getNameFilter()
meth public long[] getIds()
meth public org.netbeans.lib.v8debug.PropertyBoolean isIncludeSource()
meth public org.netbeans.lib.v8debug.PropertyLong getIdFilter()
meth public org.netbeans.lib.v8debug.V8Script$Types getTypes()
supr org.netbeans.lib.v8debug.V8Arguments
hfds idFilter,ids,includeSource,nameFilter,types

CLSS public final static org.netbeans.lib.v8debug.commands.Scripts$ResponseBody
 outer org.netbeans.lib.v8debug.commands.Scripts
cons public init(org.netbeans.lib.v8debug.V8Script[])
meth public org.netbeans.lib.v8debug.V8Script[] getScripts()
supr org.netbeans.lib.v8debug.V8Body
hfds scripts

CLSS public final org.netbeans.lib.v8debug.commands.SetBreakpoint
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,org.netbeans.lib.v8debug.V8Breakpoint$Type,java.lang.String,java.lang.Long,java.lang.Long)
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,org.netbeans.lib.v8debug.V8Breakpoint$Type,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,java.lang.Long,java.lang.Long)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.SetBreakpoint$Arguments
 outer org.netbeans.lib.v8debug.commands.SetBreakpoint
cons public init(org.netbeans.lib.v8debug.V8Breakpoint$Type,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,java.lang.Long,java.lang.Long)
meth public java.lang.String getCondition()
meth public java.lang.String getTarget()
meth public org.netbeans.lib.v8debug.PropertyBoolean isEnabled()
meth public org.netbeans.lib.v8debug.PropertyLong getColumn()
meth public org.netbeans.lib.v8debug.PropertyLong getGroupId()
meth public org.netbeans.lib.v8debug.PropertyLong getIgnoreCount()
meth public org.netbeans.lib.v8debug.PropertyLong getLine()
meth public org.netbeans.lib.v8debug.V8Breakpoint$Type getType()
supr org.netbeans.lib.v8debug.V8Arguments
hfds column,condition,enabled,groupId,ignoreCount,line,target,type

CLSS public final static org.netbeans.lib.v8debug.commands.SetBreakpoint$ResponseBody
 outer org.netbeans.lib.v8debug.commands.SetBreakpoint
cons public init(org.netbeans.lib.v8debug.V8Breakpoint$Type,long,java.lang.String,java.lang.Long,java.lang.Long,org.netbeans.lib.v8debug.V8Breakpoint$ActualLocation[])
meth public java.lang.String getScriptName()
meth public long getBreakpoint()
meth public org.netbeans.lib.v8debug.PropertyLong getColumn()
meth public org.netbeans.lib.v8debug.PropertyLong getLine()
meth public org.netbeans.lib.v8debug.V8Breakpoint$ActualLocation[] getActualLocations()
meth public org.netbeans.lib.v8debug.V8Breakpoint$Type getType()
supr org.netbeans.lib.v8debug.V8Body
hfds actualLocations,breakpoint,column,line,scriptName,type

CLSS public final org.netbeans.lib.v8debug.commands.SetExceptionBreak
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,org.netbeans.lib.v8debug.V8ExceptionBreakType,boolean)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.SetExceptionBreak$Arguments
 outer org.netbeans.lib.v8debug.commands.SetExceptionBreak
cons public init(org.netbeans.lib.v8debug.V8ExceptionBreakType,boolean)
meth public boolean isEnabled()
meth public org.netbeans.lib.v8debug.V8ExceptionBreakType getType()
supr org.netbeans.lib.v8debug.V8Arguments
hfds enabled,type

CLSS public final static org.netbeans.lib.v8debug.commands.SetExceptionBreak$ResponseBody
 outer org.netbeans.lib.v8debug.commands.SetExceptionBreak
cons public init(org.netbeans.lib.v8debug.V8ExceptionBreakType,boolean)
meth public boolean isEnabled()
meth public org.netbeans.lib.v8debug.V8ExceptionBreakType getType()
supr org.netbeans.lib.v8debug.V8Body
hfds enabled,type

CLSS public final org.netbeans.lib.v8debug.commands.SetVariableValue
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,java.lang.String,org.netbeans.lib.v8debug.vars.NewValue,long)
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,java.lang.String,org.netbeans.lib.v8debug.vars.NewValue,long,java.lang.Long)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.SetVariableValue$Arguments
 outer org.netbeans.lib.v8debug.commands.SetVariableValue
cons public init(java.lang.String,org.netbeans.lib.v8debug.vars.NewValue,long,java.lang.Long)
meth public java.lang.String getName()
meth public long getScopeNumber()
meth public org.netbeans.lib.v8debug.PropertyLong getScopeFrameNumber()
meth public org.netbeans.lib.v8debug.vars.NewValue getNewValue()
supr org.netbeans.lib.v8debug.V8Arguments
hfds name,newValue,scopeFrameNumber,scopeNumber

CLSS public final static org.netbeans.lib.v8debug.commands.SetVariableValue$ResponseBody
 outer org.netbeans.lib.v8debug.commands.SetVariableValue
cons public init(org.netbeans.lib.v8debug.vars.V8Value)
meth public org.netbeans.lib.v8debug.vars.V8Value getNewValue()
supr org.netbeans.lib.v8debug.V8Body
hfds newValue

CLSS public final org.netbeans.lib.v8debug.commands.Source
innr public final static Arguments
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,java.lang.Long,java.lang.Long,java.lang.Long)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.Source$Arguments
 outer org.netbeans.lib.v8debug.commands.Source
cons public init(java.lang.Long,java.lang.Long,java.lang.Long)
meth public org.netbeans.lib.v8debug.PropertyLong getFrame()
meth public org.netbeans.lib.v8debug.PropertyLong getFromLine()
meth public org.netbeans.lib.v8debug.PropertyLong getToLine()
supr org.netbeans.lib.v8debug.V8Arguments
hfds frame,fromLine,toLine

CLSS public final static org.netbeans.lib.v8debug.commands.Source$ResponseBody
 outer org.netbeans.lib.v8debug.commands.Source
cons public init(java.lang.String,long,long,long,long,long)
meth public java.lang.String getSource()
meth public long getFromLine()
meth public long getFromPosition()
meth public long getToLine()
meth public long getToPosition()
meth public long getTotalLines()
supr org.netbeans.lib.v8debug.V8Body
hfds fromLine,fromPosition,source,toLine,toPosition,totalLines

CLSS public final org.netbeans.lib.v8debug.commands.Suspend
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long)
supr java.lang.Object

CLSS public final org.netbeans.lib.v8debug.commands.Threads
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.Threads$ResponseBody
 outer org.netbeans.lib.v8debug.commands.Threads
cons public init(long,java.util.Map<java.lang.Long,java.lang.Boolean>)
meth public java.util.Map<java.lang.Long,java.lang.Boolean> getIds()
meth public long getNumThreads()
supr org.netbeans.lib.v8debug.V8Body
hfds ids,numThreads

CLSS public final org.netbeans.lib.v8debug.commands.V8Flags
innr public final static Arguments
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long,java.lang.String)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.V8Flags$Arguments
 outer org.netbeans.lib.v8debug.commands.V8Flags
cons public init(java.lang.String)
meth public java.lang.String getFlags()
supr org.netbeans.lib.v8debug.V8Arguments
hfds flags

CLSS public final org.netbeans.lib.v8debug.commands.Version
cons public init()
innr public final static ResponseBody
meth public static org.netbeans.lib.v8debug.V8Request createRequest(long)
supr java.lang.Object

CLSS public final static org.netbeans.lib.v8debug.commands.Version$ResponseBody
 outer org.netbeans.lib.v8debug.commands.Version
cons public init(java.lang.String)
meth public java.lang.String getVersion()
supr org.netbeans.lib.v8debug.V8Body
hfds version

CLSS public final org.netbeans.lib.v8debug.connection.ClientConnection
cons public init(java.lang.String,int) throws java.io.IOException
innr public abstract interface static Listener
meth public boolean isClosed()
meth public void addIOListener(org.netbeans.lib.v8debug.connection.IOListener)
meth public void close() throws java.io.IOException
meth public void removeIOListener(org.netbeans.lib.v8debug.connection.IOListener)
meth public void runEventLoop(org.netbeans.lib.v8debug.connection.ClientConnection$Listener) throws java.io.IOException
meth public void send(org.netbeans.lib.v8debug.V8Request) throws java.io.IOException
supr java.lang.Object
hfds LOG,buffer,containerFactory,ioListeners,outLock,server,serverIn,serverOut

CLSS public abstract interface static org.netbeans.lib.v8debug.connection.ClientConnection$Listener
 outer org.netbeans.lib.v8debug.connection.ClientConnection
meth public abstract void event(org.netbeans.lib.v8debug.V8Event)
meth public abstract void header(java.util.Map<java.lang.String,java.lang.String>)
meth public abstract void response(org.netbeans.lib.v8debug.V8Response)

CLSS public final org.netbeans.lib.v8debug.connection.HeaderProperties
fld public final static java.lang.String EMBEDDING_HOST = "Embedding-Host"
fld public final static java.lang.String PROTOCOL_VERSION = "Protocol-Version"
fld public final static java.lang.String TYPE = "Type"
fld public final static java.lang.String V8_VERSION = "V8-Version"
supr java.lang.Object

CLSS public abstract interface org.netbeans.lib.v8debug.connection.IOListener
meth public abstract void closed()
meth public abstract void received(java.lang.String)
meth public abstract void sent(java.lang.String)

CLSS public org.netbeans.lib.v8debug.connection.LinkedJSONObject
cons public init()
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.Object clone()
meth public java.lang.Object get(java.lang.Object)
meth public java.lang.Object put(java.lang.Object,java.lang.Object)
meth public java.lang.Object remove(java.lang.Object)
meth public java.util.Collection values()
meth public java.util.Set entrySet()
meth public java.util.Set keySet()
meth public void clear()
meth public void putAll(java.util.Map)
supr org.json.simple.JSONObject
hfds linkedMap

CLSS public final org.netbeans.lib.v8debug.connection.ServerConnection
cons public init() throws java.io.IOException
cons public init(int) throws java.io.IOException
innr public abstract interface static Listener
innr public final static ResponseProvider
meth public boolean isConnected()
meth public int getPort()
meth public void addIOListener(org.netbeans.lib.v8debug.connection.IOListener)
meth public void closeCurrentConnection() throws java.io.IOException
meth public void closeServer() throws java.io.IOException
meth public void removeIOListener(org.netbeans.lib.v8debug.connection.IOListener)
meth public void runConnectionLoop(java.util.Map<java.lang.String,java.lang.String>,org.netbeans.lib.v8debug.connection.ServerConnection$Listener) throws java.io.IOException
meth public void send(org.netbeans.lib.v8debug.V8Event) throws java.io.IOException
supr java.lang.Object
hfds LOG,SERVER_PROTOCOL_VERSION,buffer,clientIn,clientOut,containerFactory,currentSocket,ioListeners,outLock,server

CLSS public abstract interface static org.netbeans.lib.v8debug.connection.ServerConnection$Listener
 outer org.netbeans.lib.v8debug.connection.ServerConnection
meth public abstract org.netbeans.lib.v8debug.connection.ServerConnection$ResponseProvider request(org.netbeans.lib.v8debug.V8Request)

CLSS public final static org.netbeans.lib.v8debug.connection.ServerConnection$ResponseProvider
 outer org.netbeans.lib.v8debug.connection.ServerConnection
meth public static org.netbeans.lib.v8debug.connection.ServerConnection$ResponseProvider create(org.netbeans.lib.v8debug.V8Response)
meth public static org.netbeans.lib.v8debug.connection.ServerConnection$ResponseProvider createLazy()
meth public void setResponse(org.netbeans.lib.v8debug.V8Response) throws java.io.IOException
supr java.lang.Object
hfds response,sc

CLSS public org.netbeans.lib.v8debug.events.AfterCompileEventBody
cons public init(org.netbeans.lib.v8debug.V8Script)
meth public org.netbeans.lib.v8debug.V8Script getScript()
supr org.netbeans.lib.v8debug.V8Body
hfds script

CLSS public final org.netbeans.lib.v8debug.events.BreakEventBody
cons public init(java.lang.String,long,long,java.lang.String,org.netbeans.lib.v8debug.V8ScriptLocation,long[])
meth public java.lang.String getInvocationText()
meth public java.lang.String getSourceLineText()
meth public long getSourceColumn()
meth public long getSourceLine()
meth public long[] getBreakpoints()
meth public org.netbeans.lib.v8debug.V8ScriptLocation getScript()
supr org.netbeans.lib.v8debug.V8Body
hfds breakpoints,invocationText,script,sourceColumn,sourceLine,sourceLineText

CLSS public org.netbeans.lib.v8debug.events.CompileErrorEventBody
cons public init(org.netbeans.lib.v8debug.V8Script)
meth public org.netbeans.lib.v8debug.V8Script getScript()
supr org.netbeans.lib.v8debug.V8Body
hfds script

CLSS public final org.netbeans.lib.v8debug.events.ExceptionEventBody
cons public init(boolean,org.netbeans.lib.v8debug.vars.V8Value,long,long,java.lang.String,org.netbeans.lib.v8debug.V8Script)
meth public boolean isUncaught()
meth public java.lang.String getSourceLineText()
meth public long getSourceColumn()
meth public long getSourceLine()
meth public org.netbeans.lib.v8debug.V8Script getScript()
meth public org.netbeans.lib.v8debug.vars.V8Value getException()
supr org.netbeans.lib.v8debug.V8Body
hfds exception,script,sourceColumn,sourceLine,sourceLineText,uncaught

CLSS public org.netbeans.lib.v8debug.events.ScriptCollectedEventBody
cons public init(long)
meth public long getScriptId()
supr org.netbeans.lib.v8debug.V8Body
hfds scriptId

CLSS public final org.netbeans.lib.v8debug.vars.NamedHandle
cons public init(java.lang.String,long)
meth public java.lang.String getName()
meth public long getHandle()
supr java.lang.Object
hfds handle,name

CLSS public org.netbeans.lib.v8debug.vars.NewValue
cons public init(long)
cons public init(org.netbeans.lib.v8debug.vars.V8Value$Type,java.lang.String)
meth public java.lang.String getDescription()
meth public org.netbeans.lib.v8debug.PropertyLong getHandle()
meth public org.netbeans.lib.v8debug.vars.V8Value$Type getType()
supr java.lang.Object
hfds description,handle,type

CLSS public final org.netbeans.lib.v8debug.vars.ReferencedValue<%0 extends org.netbeans.lib.v8debug.vars.V8Value>
cons public init(long,{org.netbeans.lib.v8debug.vars.ReferencedValue%0})
meth public boolean hasValue()
meth public long getReference()
meth public {org.netbeans.lib.v8debug.vars.ReferencedValue%0} getValue()
supr java.lang.Object
hfds reference,value

CLSS public final org.netbeans.lib.v8debug.vars.V8Boolean
cons public init(long,boolean,java.lang.String)
meth public boolean getValue()
supr org.netbeans.lib.v8debug.vars.V8Value
hfds value

CLSS public final org.netbeans.lib.v8debug.vars.V8Function
cons public init(long,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.PropertyLong,java.lang.String,java.lang.String,java.lang.Boolean,java.lang.String,org.netbeans.lib.v8debug.PropertyLong,java.lang.Long,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.V8Scope[],java.util.Map<java.lang.String,org.netbeans.lib.v8debug.vars.V8Object$Property>,java.lang.String)
meth public java.lang.String getInferredName()
meth public java.lang.String getName()
meth public java.lang.String getSource()
meth public org.netbeans.lib.v8debug.PropertyBoolean isResolved()
meth public org.netbeans.lib.v8debug.PropertyLong getColumn()
meth public org.netbeans.lib.v8debug.PropertyLong getLine()
meth public org.netbeans.lib.v8debug.PropertyLong getPosition()
meth public org.netbeans.lib.v8debug.PropertyLong getScriptId()
meth public org.netbeans.lib.v8debug.PropertyLong getScriptRef()
meth public org.netbeans.lib.v8debug.V8Scope[] getScopes()
supr org.netbeans.lib.v8debug.vars.V8Object
hfds FUNCTION_CLASS_NAME,column,inferredName,line,name,position,resolved,scopes,scriptId,scriptRef,source

CLSS public final org.netbeans.lib.v8debug.vars.V8Generator
cons public init(long,java.lang.String,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.PropertyLong,java.util.Map<java.lang.String,org.netbeans.lib.v8debug.vars.V8Object$Property>,java.lang.String)
meth public org.netbeans.lib.v8debug.PropertyLong getFunctionHandle()
meth public org.netbeans.lib.v8debug.PropertyLong getReceiverHandle()
supr org.netbeans.lib.v8debug.vars.V8Object
hfds functionHandle,receiverHandle

CLSS public final org.netbeans.lib.v8debug.vars.V8Number
cons public init(long,double,java.lang.String)
cons public init(long,long,java.lang.String)
innr public final static !enum Kind
meth public double getDoubleValue()
meth public long getLongValue()
meth public org.netbeans.lib.v8debug.vars.V8Number$Kind getKind()
supr org.netbeans.lib.v8debug.vars.V8Value
hfds dvalue,lvalue,type

CLSS public final static !enum org.netbeans.lib.v8debug.vars.V8Number$Kind
 outer org.netbeans.lib.v8debug.vars.V8Number
fld public final static org.netbeans.lib.v8debug.vars.V8Number$Kind Double
fld public final static org.netbeans.lib.v8debug.vars.V8Number$Kind Long
meth public static org.netbeans.lib.v8debug.vars.V8Number$Kind valueOf(java.lang.String)
meth public static org.netbeans.lib.v8debug.vars.V8Number$Kind[] values()
supr java.lang.Enum<org.netbeans.lib.v8debug.vars.V8Number$Kind>

CLSS public org.netbeans.lib.v8debug.vars.V8Object
cons protected init(long,org.netbeans.lib.v8debug.vars.V8Value$Type,java.lang.String,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.PropertyLong,java.util.Map<java.lang.String,org.netbeans.lib.v8debug.vars.V8Object$Property>,java.lang.String)
cons public init(long,org.netbeans.lib.v8debug.vars.V8Value$Type,java.lang.String,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.PropertyLong,org.netbeans.lib.v8debug.PropertyLong,java.util.Map<java.lang.String,org.netbeans.lib.v8debug.vars.V8Object$Property>,org.netbeans.lib.v8debug.vars.V8Object$Array,java.lang.String)
innr public abstract interface static Array
innr public abstract interface static IndexIterator
innr public final static DefaultArray
innr public final static Property
meth public java.lang.String getClassName()
meth public java.util.Map<java.lang.String,org.netbeans.lib.v8debug.vars.V8Object$Property> getProperties()
meth public org.netbeans.lib.v8debug.PropertyLong getConstructorFunctionHandle()
meth public org.netbeans.lib.v8debug.PropertyLong getProtoObjectHandle()
meth public org.netbeans.lib.v8debug.PropertyLong getPrototypeObjectHandle()
meth public org.netbeans.lib.v8debug.vars.V8Object$Array getArray()
supr org.netbeans.lib.v8debug.vars.V8Value
hfds array,className,constructorFunctionHandle,properties,protoObjectHandle,prototypeObjectHandle

CLSS public abstract interface static org.netbeans.lib.v8debug.vars.V8Object$Array
 outer org.netbeans.lib.v8debug.vars.V8Object
meth public abstract boolean isContiguous()
meth public abstract long getLength()
meth public abstract long getReferenceAt(long)
meth public abstract long[] getContiguousReferences()
meth public abstract org.netbeans.lib.v8debug.vars.V8Object$IndexIterator getIndexIterator()

CLSS public final static org.netbeans.lib.v8debug.vars.V8Object$DefaultArray
 outer org.netbeans.lib.v8debug.vars.V8Object
cons public init()
cons public init(long[])
intf org.netbeans.lib.v8debug.vars.V8Object$Array
meth public boolean isContiguous()
meth public long getLength()
meth public long getReferenceAt(long)
meth public long[] getContiguousReferences()
meth public org.netbeans.lib.v8debug.vars.V8Object$IndexIterator getIndexIterator()
meth public void putReferenceAt(long,long)
supr java.lang.Object
hfds indexes,length,references
hcls DefaultIndexIterator

CLSS public abstract interface static org.netbeans.lib.v8debug.vars.V8Object$IndexIterator
 outer org.netbeans.lib.v8debug.vars.V8Object
meth public abstract boolean hasNextIndex()
meth public abstract long nextIndex()

CLSS public final static org.netbeans.lib.v8debug.vars.V8Object$Property
 outer org.netbeans.lib.v8debug.vars.V8Object
cons public init(java.lang.String,org.netbeans.lib.v8debug.vars.V8Object$Property$Type,int,long)
fld public final static int ATTR_ABSENT = 64
fld public final static int ATTR_DONT_DELETE = 4
fld public final static int ATTR_DONT_ENUM = 2
fld public final static int ATTR_DONT_SHOW = 50
fld public final static int ATTR_FROZEN = 5
fld public final static int ATTR_NONE = 0
fld public final static int ATTR_PRIVATE_SYMBOL = 32
fld public final static int ATTR_READ_ONLY = 1
fld public final static int ATTR_SEALED = 4
fld public final static int ATTR_STRING = 8
fld public final static int ATTR_SYMBOLIC = 16
innr public final static !enum Type
meth public int getAttributes()
meth public java.lang.String getName()
meth public long getReference()
meth public org.netbeans.lib.v8debug.vars.V8Object$Property$Type getType()
supr java.lang.Object
hfds attributes,name,reference,type

CLSS public final static !enum org.netbeans.lib.v8debug.vars.V8Object$Property$Type
 outer org.netbeans.lib.v8debug.vars.V8Object$Property
fld public final static org.netbeans.lib.v8debug.vars.V8Object$Property$Type Callbacks
fld public final static org.netbeans.lib.v8debug.vars.V8Object$Property$Type Constant
fld public final static org.netbeans.lib.v8debug.vars.V8Object$Property$Type Field
fld public final static org.netbeans.lib.v8debug.vars.V8Object$Property$Type Handler
fld public final static org.netbeans.lib.v8debug.vars.V8Object$Property$Type Interceptor
fld public final static org.netbeans.lib.v8debug.vars.V8Object$Property$Type Nonexistent
fld public final static org.netbeans.lib.v8debug.vars.V8Object$Property$Type Normal
fld public final static org.netbeans.lib.v8debug.vars.V8Object$Property$Type Transition
meth public static org.netbeans.lib.v8debug.vars.V8Object$Property$Type valueOf(java.lang.String)
meth public static org.netbeans.lib.v8debug.vars.V8Object$Property$Type[] values()
supr java.lang.Enum<org.netbeans.lib.v8debug.vars.V8Object$Property$Type>

CLSS public final org.netbeans.lib.v8debug.vars.V8ScriptValue
cons public init(long,org.netbeans.lib.v8debug.V8Script,java.lang.String)
meth public org.netbeans.lib.v8debug.V8Script getScript()
supr org.netbeans.lib.v8debug.vars.V8Value
hfds script

CLSS public final org.netbeans.lib.v8debug.vars.V8String
cons public init(long,java.lang.String,java.lang.String)
meth public java.lang.String getValue()
supr org.netbeans.lib.v8debug.vars.V8Value
hfds value

CLSS public org.netbeans.lib.v8debug.vars.V8Value
cons public init(long,org.netbeans.lib.v8debug.vars.V8Value$Type,java.lang.String)
innr public final static !enum Type
meth public java.lang.String getText()
meth public long getHandle()
meth public org.netbeans.lib.v8debug.vars.V8Value$Type getType()
supr java.lang.Object
hfds handle,text,type

CLSS public final static !enum org.netbeans.lib.v8debug.vars.V8Value$Type
 outer org.netbeans.lib.v8debug.vars.V8Value
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type Boolean
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type Context
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type Error
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type Frame
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type Function
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type Generator
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type Map
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type Null
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type Number
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type Object
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type Promise
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type Regexp
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type Script
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type Set
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type String
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type Symbol
fld public final static org.netbeans.lib.v8debug.vars.V8Value$Type Undefined
meth public java.lang.String toString()
meth public static org.netbeans.lib.v8debug.vars.V8Value$Type fromString(java.lang.String)
meth public static org.netbeans.lib.v8debug.vars.V8Value$Type valueOf(java.lang.String)
meth public static org.netbeans.lib.v8debug.vars.V8Value$Type[] values()
supr java.lang.Enum<org.netbeans.lib.v8debug.vars.V8Value$Type>

