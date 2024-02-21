#Signature file v4.1
#Version 1.23

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

CLSS public abstract org.netbeans.modules.csl.spi.ParserResult
cons protected init(org.netbeans.modules.parsing.api.Snapshot)
meth public abstract java.util.List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics()
supr org.netbeans.modules.parsing.spi.Parser$Result

CLSS public abstract interface org.netbeans.modules.javascript2.types.api.DeclarationScope
meth public abstract java.util.Collection<? extends org.netbeans.modules.javascript2.types.api.DeclarationScope> getChildrenScopes()
meth public abstract org.netbeans.modules.javascript2.types.api.DeclarationScope getParentScope()
meth public abstract void addDeclaredScope(org.netbeans.modules.javascript2.types.api.DeclarationScope)

CLSS public final org.netbeans.modules.javascript2.types.api.Identifier
cons public init(java.lang.String,int)
cons public init(java.lang.String,org.netbeans.modules.csl.api.OffsetRange)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public org.netbeans.modules.csl.api.OffsetRange getOffsetRange()
supr java.lang.Object
hfds name,offsetRange

CLSS public abstract interface org.netbeans.modules.javascript2.types.api.Type
fld public final static java.lang.String ARRAY = "Array"
fld public final static java.lang.String BOOLEAN = "Boolean"
fld public final static java.lang.String FUNCTION = "Function"
fld public final static java.lang.String INFINITY = "Infinity"
fld public final static java.lang.String NAN = "NaN"
fld public final static java.lang.String NULL = "null"
fld public final static java.lang.String NUMBER = "Number"
fld public final static java.lang.String OBJECT = "Object"
fld public final static java.lang.String REGEXP = "RegExp"
fld public final static java.lang.String STRING = "String"
fld public final static java.lang.String UNDEFINED = "undefined"
fld public final static java.lang.String UNRESOLVED = "unresolved"
meth public abstract int getOffset()
meth public abstract java.lang.String getType()

CLSS public final org.netbeans.modules.javascript2.types.api.TypeUsage
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,boolean)
intf org.netbeans.modules.javascript2.types.api.Type
meth public boolean equals(java.lang.Object)
meth public boolean isResolved()
meth public int getOffset()
meth public int hashCode()
meth public java.lang.String getType()
meth public java.lang.String toString()
supr java.lang.Object
hfds offset,resolved,type

CLSS public abstract org.netbeans.modules.javascript2.types.spi.ParserResult
cons public init(org.netbeans.modules.parsing.api.Snapshot)
intf org.openide.util.Lookup$Provider
supr org.netbeans.modules.csl.spi.ParserResult

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

CLSS public abstract org.openide.util.Lookup
cons public init()
fld public final static org.openide.util.Lookup EMPTY
innr public abstract interface static Provider
innr public abstract static Item
innr public abstract static Result
innr public final static Template
meth public <%0 extends java.lang.Object> java.util.Collection<? extends {%%0}> lookupAll(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookupResult(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public static org.openide.util.Lookup getDefault()
supr java.lang.Object
hfds LOG,defaultLookup,defaultLookupProvider
hcls DefLookup,Empty

CLSS public abstract interface static org.openide.util.Lookup$Provider
 outer org.openide.util.Lookup
meth public abstract org.openide.util.Lookup getLookup()

