#Signature file v4.1
#Version 1.34

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

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

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

CLSS public abstract interface org.netbeans.modules.db.metadata.model.api.Action<%0 extends java.lang.Object>
meth public abstract void run({org.netbeans.modules.db.metadata.model.api.Action%0})

CLSS public org.netbeans.modules.db.metadata.model.api.Catalog
meth public boolean isDefault()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.Schema> getSchemas()
meth public org.netbeans.modules.db.metadata.model.api.MetadataElement getParent()
meth public org.netbeans.modules.db.metadata.model.api.Schema getSchema(java.lang.String)
meth public org.netbeans.modules.db.metadata.model.api.Schema getSyntheticSchema()
meth public void refresh()
supr org.netbeans.modules.db.metadata.model.api.MetadataElement
hfds impl

CLSS public org.netbeans.modules.db.metadata.model.api.Column
meth public int getPosition()
meth public org.netbeans.modules.db.metadata.model.api.Tuple getParent()
supr org.netbeans.modules.db.metadata.model.api.Value
hfds impl

CLSS public org.netbeans.modules.db.metadata.model.api.ForeignKey
meth public java.lang.String getName()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.ForeignKeyColumn> getColumns()
meth public org.netbeans.modules.db.metadata.model.api.ForeignKeyColumn getColumn(java.lang.String)
meth public org.netbeans.modules.db.metadata.model.api.Table getParent()
supr org.netbeans.modules.db.metadata.model.api.MetadataElement
hfds impl

CLSS public org.netbeans.modules.db.metadata.model.api.ForeignKeyColumn
meth public int getPosition()
meth public java.lang.String getName()
meth public org.netbeans.modules.db.metadata.model.api.Column getReferredColumn()
meth public org.netbeans.modules.db.metadata.model.api.Column getReferringColumn()
meth public org.netbeans.modules.db.metadata.model.api.ForeignKey getParent()
supr org.netbeans.modules.db.metadata.model.api.MetadataElement
hfds impl

CLSS public org.netbeans.modules.db.metadata.model.api.Function
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.Column> getColumns()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.Parameter> getParameters()
meth public org.netbeans.modules.db.metadata.model.api.Column getColumn(java.lang.String)
meth public org.netbeans.modules.db.metadata.model.api.Parameter getParameter(java.lang.String)
meth public org.netbeans.modules.db.metadata.model.api.Schema getParent()
meth public org.netbeans.modules.db.metadata.model.api.Value getReturnValue()
meth public void refresh()
supr org.netbeans.modules.db.metadata.model.api.Tuple
hfds impl

CLSS public org.netbeans.modules.db.metadata.model.api.Index
innr public final static !enum IndexType
meth public boolean isUnique()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.IndexColumn> getColumns()
meth public org.netbeans.modules.db.metadata.model.api.Index$IndexType getIndexType()
meth public org.netbeans.modules.db.metadata.model.api.IndexColumn getColumn(java.lang.String)
meth public org.netbeans.modules.db.metadata.model.api.Table getParent()
supr org.netbeans.modules.db.metadata.model.api.MetadataElement
hfds impl

CLSS public final static !enum org.netbeans.modules.db.metadata.model.api.Index$IndexType
 outer org.netbeans.modules.db.metadata.model.api.Index
fld public final static org.netbeans.modules.db.metadata.model.api.Index$IndexType CLUSTERED
fld public final static org.netbeans.modules.db.metadata.model.api.Index$IndexType HASHED
fld public final static org.netbeans.modules.db.metadata.model.api.Index$IndexType OTHER
meth public static org.netbeans.modules.db.metadata.model.api.Index$IndexType valueOf(java.lang.String)
meth public static org.netbeans.modules.db.metadata.model.api.Index$IndexType[] values()
supr java.lang.Enum<org.netbeans.modules.db.metadata.model.api.Index$IndexType>

CLSS public org.netbeans.modules.db.metadata.model.api.IndexColumn
meth public int getPosition()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public org.netbeans.modules.db.metadata.model.api.Column getColumn()
meth public org.netbeans.modules.db.metadata.model.api.Index getParent()
meth public org.netbeans.modules.db.metadata.model.api.Ordering getOrdering()
supr org.netbeans.modules.db.metadata.model.api.MetadataElement
hfds impl

CLSS public org.netbeans.modules.db.metadata.model.api.Metadata
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.Catalog> getCatalogs()
meth public org.netbeans.modules.db.metadata.model.api.Catalog getCatalog(java.lang.String)
meth public org.netbeans.modules.db.metadata.model.api.Catalog getDefaultCatalog()
meth public org.netbeans.modules.db.metadata.model.api.Schema getDefaultSchema()
meth public void refresh()
supr java.lang.Object
hfds impl
hcls MetadataAccessorImpl

CLSS public abstract org.netbeans.modules.db.metadata.model.api.MetadataElement
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.db.metadata.model.api.MetadataElement getParent()
supr java.lang.Object

CLSS public org.netbeans.modules.db.metadata.model.api.MetadataElementHandle<%0 extends org.netbeans.modules.db.metadata.model.api.MetadataElement>
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public static <%0 extends org.netbeans.modules.db.metadata.model.api.MetadataElement> org.netbeans.modules.db.metadata.model.api.MetadataElementHandle<{%%0}> create({%%0})
meth public {org.netbeans.modules.db.metadata.model.api.MetadataElementHandle%0} resolve(org.netbeans.modules.db.metadata.model.api.Metadata)
supr java.lang.Object
hfds CATALOG,COLUMN,FOREIGN_KEY,FOREIGN_KEY_COLUMN,FUNCTION,INDEX,INDEX_COLUMN,PARAMETER,PROCEDURE,SCHEMA,TABLE,VIEW,kinds,names
hcls Kind

CLSS public org.netbeans.modules.db.metadata.model.api.MetadataException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public org.netbeans.modules.db.metadata.model.api.MetadataModel
meth public void runReadAction(org.netbeans.modules.db.metadata.model.api.Action<org.netbeans.modules.db.metadata.model.api.Metadata>) throws org.netbeans.modules.db.metadata.model.api.MetadataModelException
supr java.lang.Object
hfds impl

CLSS public org.netbeans.modules.db.metadata.model.api.MetadataModelException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public org.netbeans.modules.db.metadata.model.api.MetadataModels
meth public static org.netbeans.modules.db.metadata.model.api.MetadataModel createModel(java.sql.Connection,java.lang.String)
supr java.lang.Object

CLSS public final !enum org.netbeans.modules.db.metadata.model.api.Nullable
fld public final static org.netbeans.modules.db.metadata.model.api.Nullable NOT_NULLABLE
fld public final static org.netbeans.modules.db.metadata.model.api.Nullable NULLABLE
fld public final static org.netbeans.modules.db.metadata.model.api.Nullable UNKNOWN
meth public static org.netbeans.modules.db.metadata.model.api.Nullable valueOf(java.lang.String)
meth public static org.netbeans.modules.db.metadata.model.api.Nullable[] values()
supr java.lang.Enum<org.netbeans.modules.db.metadata.model.api.Nullable>

CLSS public final !enum org.netbeans.modules.db.metadata.model.api.Ordering
fld public final static org.netbeans.modules.db.metadata.model.api.Ordering ASCENDING
fld public final static org.netbeans.modules.db.metadata.model.api.Ordering DESCENDING
fld public final static org.netbeans.modules.db.metadata.model.api.Ordering NOT_SUPPORTED
meth public static org.netbeans.modules.db.metadata.model.api.Ordering valueOf(java.lang.String)
meth public static org.netbeans.modules.db.metadata.model.api.Ordering[] values()
supr java.lang.Enum<org.netbeans.modules.db.metadata.model.api.Ordering>

CLSS public org.netbeans.modules.db.metadata.model.api.Parameter
innr public final static !enum Direction
meth public int getOrdinalPosition()
meth public org.netbeans.modules.db.metadata.model.api.Parameter$Direction getDirection()
supr org.netbeans.modules.db.metadata.model.api.Value
hfds impl

CLSS public final static !enum org.netbeans.modules.db.metadata.model.api.Parameter$Direction
 outer org.netbeans.modules.db.metadata.model.api.Parameter
fld public final static org.netbeans.modules.db.metadata.model.api.Parameter$Direction IN
fld public final static org.netbeans.modules.db.metadata.model.api.Parameter$Direction INOUT
fld public final static org.netbeans.modules.db.metadata.model.api.Parameter$Direction OUT
meth public static org.netbeans.modules.db.metadata.model.api.Parameter$Direction valueOf(java.lang.String)
meth public static org.netbeans.modules.db.metadata.model.api.Parameter$Direction[] values()
supr java.lang.Enum<org.netbeans.modules.db.metadata.model.api.Parameter$Direction>

CLSS public org.netbeans.modules.db.metadata.model.api.PrimaryKey
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.Column> getColumns()
meth public org.netbeans.modules.db.metadata.model.api.Table getParent()
supr org.netbeans.modules.db.metadata.model.api.MetadataElement
hfds impl

CLSS public org.netbeans.modules.db.metadata.model.api.Procedure
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.Column> getColumns()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.Parameter> getParameters()
meth public org.netbeans.modules.db.metadata.model.api.Column getColumn(java.lang.String)
meth public org.netbeans.modules.db.metadata.model.api.Parameter getParameter(java.lang.String)
meth public org.netbeans.modules.db.metadata.model.api.Schema getParent()
meth public org.netbeans.modules.db.metadata.model.api.Value getReturnValue()
meth public void refresh()
supr org.netbeans.modules.db.metadata.model.api.Tuple
hfds impl

CLSS public final !enum org.netbeans.modules.db.metadata.model.api.SQLType
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType ARRAY
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType BIGINT
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType BINARY
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType BIT
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType BLOB
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType BOOLEAN
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType CHAR
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType CLOB
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType DATALINK
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType DATE
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType DECIMAL
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType DISTINCT
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType DOUBLE
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType FLOAT
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType INTEGER
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType JAVA_OBJECT
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType LONGNVARCHAR
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType LONGVARBINARY
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType LONGVARCHAR
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType NCHAR
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType NCLOB
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType NULL
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType NUMERIC
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType NVARCHAR
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType OTHER
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType REAL
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType REF
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType ROWID
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType SMALLINT
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType SQLXML
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType STRUCT
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType TIME
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType TIMESTAMP
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType TINYINT
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType VARBINARY
fld public final static org.netbeans.modules.db.metadata.model.api.SQLType VARCHAR
meth public static int getJavaSQLType(org.netbeans.modules.db.metadata.model.api.SQLType)
meth public static org.netbeans.modules.db.metadata.model.api.SQLType valueOf(java.lang.String)
meth public static org.netbeans.modules.db.metadata.model.api.SQLType[] values()
supr java.lang.Enum<org.netbeans.modules.db.metadata.model.api.SQLType>
hfds javasqltype

CLSS public org.netbeans.modules.db.metadata.model.api.Schema
meth public boolean isDefault()
meth public boolean isSynthetic()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.Function> getFunctions()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.Procedure> getProcedures()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.Table> getTables()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.View> getViews()
meth public org.netbeans.modules.db.metadata.model.api.Catalog getParent()
meth public org.netbeans.modules.db.metadata.model.api.Function getFunction(java.lang.String)
meth public org.netbeans.modules.db.metadata.model.api.Procedure getProcedure(java.lang.String)
meth public org.netbeans.modules.db.metadata.model.api.Table getTable(java.lang.String)
meth public org.netbeans.modules.db.metadata.model.api.View getView(java.lang.String)
meth public void refresh()
supr org.netbeans.modules.db.metadata.model.api.MetadataElement
hfds impl

CLSS public org.netbeans.modules.db.metadata.model.api.Table
meth public boolean isSystem()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.Column> getColumns()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.ForeignKey> getForeignKeys()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.Index> getIndexes()
meth public org.netbeans.modules.db.metadata.model.api.Column getColumn(java.lang.String)
meth public org.netbeans.modules.db.metadata.model.api.Index getIndex(java.lang.String)
meth public org.netbeans.modules.db.metadata.model.api.PrimaryKey getPrimaryKey()
meth public org.netbeans.modules.db.metadata.model.api.Schema getParent()
meth public void refresh()
supr org.netbeans.modules.db.metadata.model.api.Tuple
hfds impl

CLSS public abstract org.netbeans.modules.db.metadata.model.api.Tuple
cons public init()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.Column> getColumns()
meth public abstract org.netbeans.modules.db.metadata.model.api.Column getColumn(java.lang.String)
meth public abstract org.netbeans.modules.db.metadata.model.api.Schema getParent()
supr org.netbeans.modules.db.metadata.model.api.MetadataElement

CLSS public org.netbeans.modules.db.metadata.model.api.Value
meth public int getLength()
meth public int getPrecision()
meth public java.lang.String getName()
meth public java.lang.String getTypeName()
meth public java.lang.String toString()
meth public org.netbeans.modules.db.metadata.model.api.MetadataElement getParent()
meth public org.netbeans.modules.db.metadata.model.api.Nullable getNullable()
meth public org.netbeans.modules.db.metadata.model.api.SQLType getType()
meth public short getRadix()
meth public short getScale()
supr org.netbeans.modules.db.metadata.model.api.MetadataElement
hfds impl

CLSS public org.netbeans.modules.db.metadata.model.api.View
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.Column> getColumns()
meth public org.netbeans.modules.db.metadata.model.api.Column getColumn(java.lang.String)
meth public org.netbeans.modules.db.metadata.model.api.Schema getParent()
meth public void refresh()
supr org.netbeans.modules.db.metadata.model.api.Tuple
hfds impl

CLSS public abstract org.netbeans.modules.db.metadata.model.spi.CatalogImplementation
cons public init()
meth public abstract boolean isDefault()
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.Schema> getSchemas()
meth public abstract org.netbeans.modules.db.metadata.model.api.Schema getSchema(java.lang.String)
meth public abstract org.netbeans.modules.db.metadata.model.api.Schema getSyntheticSchema()
meth public abstract void refresh()
meth public final org.netbeans.modules.db.metadata.model.api.Catalog getCatalog()
supr java.lang.Object
hfds catalog

CLSS public abstract org.netbeans.modules.db.metadata.model.spi.ColumnImplementation
cons public init()
meth public abstract int getPosition()
meth public abstract org.netbeans.modules.db.metadata.model.api.Tuple getParent()
meth public final org.netbeans.modules.db.metadata.model.api.Column getColumn()
supr org.netbeans.modules.db.metadata.model.spi.ValueImplementation
hfds column

CLSS public abstract org.netbeans.modules.db.metadata.model.spi.ForeignKeyColumnImplementation
cons public init()
meth public abstract int getPosition()
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.db.metadata.model.api.Column getReferredColumn()
meth public abstract org.netbeans.modules.db.metadata.model.api.Column getReferringColumn()
meth public abstract org.netbeans.modules.db.metadata.model.api.ForeignKey getParent()
meth public final org.netbeans.modules.db.metadata.model.api.ForeignKeyColumn getForeignKeyColumn()
supr java.lang.Object
hfds column

CLSS public abstract org.netbeans.modules.db.metadata.model.spi.ForeignKeyImplementation
cons public init()
meth public abstract java.lang.String getInternalName()
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.ForeignKeyColumn> getColumns()
meth public abstract org.netbeans.modules.db.metadata.model.api.ForeignKeyColumn getColumn(java.lang.String)
meth public abstract org.netbeans.modules.db.metadata.model.api.Table getParent()
meth public final org.netbeans.modules.db.metadata.model.api.ForeignKey getForeignKey()
supr java.lang.Object
hfds key

CLSS public abstract org.netbeans.modules.db.metadata.model.spi.FunctionImplementation
cons public init()
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.Column> getColumns()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.Parameter> getParameters()
meth public abstract org.netbeans.modules.db.metadata.model.api.Column getColumn(java.lang.String)
meth public abstract org.netbeans.modules.db.metadata.model.api.Parameter getParameter(java.lang.String)
meth public abstract org.netbeans.modules.db.metadata.model.api.Schema getParent()
meth public abstract org.netbeans.modules.db.metadata.model.api.Value getReturnValue()
meth public abstract void refresh()
meth public final org.netbeans.modules.db.metadata.model.api.Function getFunction()
supr java.lang.Object
hfds table

CLSS public abstract org.netbeans.modules.db.metadata.model.spi.IndexColumnImplementation
cons public init()
meth public abstract int getPosition()
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.db.metadata.model.api.Column getColumn()
meth public abstract org.netbeans.modules.db.metadata.model.api.Index getParent()
meth public abstract org.netbeans.modules.db.metadata.model.api.Ordering getOrdering()
meth public final org.netbeans.modules.db.metadata.model.api.IndexColumn getIndexColumn()
supr java.lang.Object
hfds column

CLSS public abstract org.netbeans.modules.db.metadata.model.spi.IndexImplementation
cons public init()
meth public abstract boolean isUnique()
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.IndexColumn> getColumns()
meth public abstract org.netbeans.modules.db.metadata.model.api.Index$IndexType getIndexType()
meth public abstract org.netbeans.modules.db.metadata.model.api.IndexColumn getColumn(java.lang.String)
meth public abstract org.netbeans.modules.db.metadata.model.api.Table getParent()
meth public final org.netbeans.modules.db.metadata.model.api.Index getIndex()
supr java.lang.Object
hfds index

CLSS public abstract org.netbeans.modules.db.metadata.model.spi.MetadataImplementation
cons public init()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.Catalog> getCatalogs()
meth public abstract org.netbeans.modules.db.metadata.model.api.Catalog getCatalog(java.lang.String)
meth public abstract org.netbeans.modules.db.metadata.model.api.Catalog getDefaultCatalog()
meth public abstract org.netbeans.modules.db.metadata.model.api.Schema getDefaultSchema()
meth public abstract void refresh()
meth public final org.netbeans.modules.db.metadata.model.api.Metadata getMetadata()
supr java.lang.Object
hfds metadata

CLSS public abstract org.netbeans.modules.db.metadata.model.spi.ParameterImplementation
cons public init()
meth public abstract int getOrdinalPosition()
meth public abstract org.netbeans.modules.db.metadata.model.api.Parameter$Direction getDirection()
meth public final org.netbeans.modules.db.metadata.model.api.Parameter getParameter()
supr org.netbeans.modules.db.metadata.model.spi.ValueImplementation
hfds param

CLSS public abstract org.netbeans.modules.db.metadata.model.spi.PrimaryKeyImplementation
cons public init()
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.Column> getColumns()
meth public abstract org.netbeans.modules.db.metadata.model.api.Table getParent()
meth public final org.netbeans.modules.db.metadata.model.api.PrimaryKey getPrimaryKey()
supr java.lang.Object
hfds primaryKey

CLSS public abstract org.netbeans.modules.db.metadata.model.spi.ProcedureImplementation
cons public init()
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.Column> getColumns()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.Parameter> getParameters()
meth public abstract org.netbeans.modules.db.metadata.model.api.Column getColumn(java.lang.String)
meth public abstract org.netbeans.modules.db.metadata.model.api.Parameter getParameter(java.lang.String)
meth public abstract org.netbeans.modules.db.metadata.model.api.Schema getParent()
meth public abstract org.netbeans.modules.db.metadata.model.api.Value getReturnValue()
meth public abstract void refresh()
meth public final org.netbeans.modules.db.metadata.model.api.Procedure getProcedure()
supr java.lang.Object
hfds table

CLSS public abstract org.netbeans.modules.db.metadata.model.spi.SchemaImplementation
cons public init()
meth public abstract boolean isDefault()
meth public abstract boolean isSynthetic()
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.Procedure> getProcedures()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.Table> getTables()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.View> getViews()
meth public abstract org.netbeans.modules.db.metadata.model.api.Catalog getParent()
meth public abstract org.netbeans.modules.db.metadata.model.api.Procedure getProcedure(java.lang.String)
meth public abstract org.netbeans.modules.db.metadata.model.api.Table getTable(java.lang.String)
meth public abstract org.netbeans.modules.db.metadata.model.api.View getView(java.lang.String)
meth public abstract void refresh()
meth public final org.netbeans.modules.db.metadata.model.api.Schema getSchema()
meth public java.util.Collection<org.netbeans.modules.db.metadata.model.api.Function> getFunctions()
meth public org.netbeans.modules.db.metadata.model.api.Function getFunction(java.lang.String)
supr java.lang.Object
hfds schema

CLSS public abstract org.netbeans.modules.db.metadata.model.spi.TableImplementation
cons public init()
meth public abstract boolean isSystem()
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.Column> getColumns()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.ForeignKey> getForeignKeys()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.Index> getIndexes()
meth public abstract org.netbeans.modules.db.metadata.model.api.Column getColumn(java.lang.String)
meth public abstract org.netbeans.modules.db.metadata.model.api.ForeignKey getForeignKeyByInternalName(java.lang.String)
meth public abstract org.netbeans.modules.db.metadata.model.api.Index getIndex(java.lang.String)
meth public abstract org.netbeans.modules.db.metadata.model.api.PrimaryKey getPrimaryKey()
meth public abstract org.netbeans.modules.db.metadata.model.api.Schema getParent()
meth public abstract void refresh()
meth public final org.netbeans.modules.db.metadata.model.api.Table getTable()
supr java.lang.Object
hfds table

CLSS public abstract org.netbeans.modules.db.metadata.model.spi.ValueImplementation
cons public init()
meth public abstract int getLength()
meth public abstract int getPrecision()
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.db.metadata.model.api.MetadataElement getParent()
meth public abstract org.netbeans.modules.db.metadata.model.api.Nullable getNullable()
meth public abstract org.netbeans.modules.db.metadata.model.api.SQLType getType()
meth public abstract short getRadix()
meth public abstract short getScale()
meth public final org.netbeans.modules.db.metadata.model.api.Value getValue()
meth public java.lang.String getTypeName()
supr java.lang.Object
hfds value

CLSS public abstract org.netbeans.modules.db.metadata.model.spi.ViewImplementation
cons public init()
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection<org.netbeans.modules.db.metadata.model.api.Column> getColumns()
meth public abstract org.netbeans.modules.db.metadata.model.api.Column getColumn(java.lang.String)
meth public abstract org.netbeans.modules.db.metadata.model.api.Schema getParent()
meth public abstract void refresh()
meth public final org.netbeans.modules.db.metadata.model.api.View getView()
supr java.lang.Object
hfds table

