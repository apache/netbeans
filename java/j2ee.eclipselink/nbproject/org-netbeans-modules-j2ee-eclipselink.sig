#Signature file v4.1
#Version 1.37

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
hfds name,ordinal

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

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
hfds serialVersionUID

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
hfds CAUSE_CAPTION,EMPTY_THROWABLE_ARRAY,NULL_CAUSE_MESSAGE,SELF_SUPPRESSION_MESSAGE,SUPPRESSED_CAPTION,SUPPRESSED_SENTINEL,UNASSIGNED_STACK,backtrace,cause,detailMessage,serialVersionUID,stackTrace,suppressedExceptions
hcls PrintStreamOrWriter,SentinelHolder,WrappedPrintStream,WrappedPrintWriter

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

CLSS public abstract interface !annotation javax.persistence.Access
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract javax.persistence.AccessType value()

CLSS public final !enum javax.persistence.AccessType
fld public final static javax.persistence.AccessType FIELD
fld public final static javax.persistence.AccessType PROPERTY
meth public static javax.persistence.AccessType valueOf(java.lang.String)
meth public static javax.persistence.AccessType[] values()
supr java.lang.Enum<javax.persistence.AccessType>

CLSS public abstract interface !annotation javax.persistence.AssociationOverride
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.persistence.ForeignKey foreignKey()
meth public abstract !hasdefault javax.persistence.JoinColumn[] joinColumns()
meth public abstract !hasdefault javax.persistence.JoinTable joinTable()
meth public abstract java.lang.String name()

CLSS public abstract interface !annotation javax.persistence.AssociationOverrides
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract javax.persistence.AssociationOverride[] value()

CLSS public abstract interface javax.persistence.AttributeConverter<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {javax.persistence.AttributeConverter%0} convertToEntityAttribute({javax.persistence.AttributeConverter%1})
meth public abstract {javax.persistence.AttributeConverter%1} convertToDatabaseColumn({javax.persistence.AttributeConverter%0})

CLSS public abstract interface javax.persistence.AttributeNode<%0 extends java.lang.Object>
meth public abstract java.lang.String getAttributeName()
meth public abstract java.util.Map<java.lang.Class,javax.persistence.Subgraph> getKeySubgraphs()
meth public abstract java.util.Map<java.lang.Class,javax.persistence.Subgraph> getSubgraphs()

CLSS public abstract interface !annotation javax.persistence.AttributeOverride
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String name()
meth public abstract javax.persistence.Column column()

CLSS public abstract interface !annotation javax.persistence.AttributeOverrides
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract javax.persistence.AttributeOverride[] value()

CLSS public abstract interface !annotation javax.persistence.Basic
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean optional()
meth public abstract !hasdefault javax.persistence.FetchType fetch()

CLSS public abstract interface javax.persistence.Cache
meth public abstract <%0 extends java.lang.Object> {%%0} unwrap(java.lang.Class<{%%0}>)
meth public abstract boolean contains(java.lang.Class,java.lang.Object)
meth public abstract void evict(java.lang.Class)
meth public abstract void evict(java.lang.Class,java.lang.Object)
meth public abstract void evictAll()

CLSS public final !enum javax.persistence.CacheRetrieveMode
fld public final static javax.persistence.CacheRetrieveMode BYPASS
fld public final static javax.persistence.CacheRetrieveMode USE
meth public static javax.persistence.CacheRetrieveMode valueOf(java.lang.String)
meth public static javax.persistence.CacheRetrieveMode[] values()
supr java.lang.Enum<javax.persistence.CacheRetrieveMode>

CLSS public final !enum javax.persistence.CacheStoreMode
fld public final static javax.persistence.CacheStoreMode BYPASS
fld public final static javax.persistence.CacheStoreMode REFRESH
fld public final static javax.persistence.CacheStoreMode USE
meth public static javax.persistence.CacheStoreMode valueOf(java.lang.String)
meth public static javax.persistence.CacheStoreMode[] values()
supr java.lang.Enum<javax.persistence.CacheStoreMode>

CLSS public abstract interface !annotation javax.persistence.Cacheable
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean value()

CLSS public final !enum javax.persistence.CascadeType
fld public final static javax.persistence.CascadeType ALL
fld public final static javax.persistence.CascadeType DETACH
fld public final static javax.persistence.CascadeType MERGE
fld public final static javax.persistence.CascadeType PERSIST
fld public final static javax.persistence.CascadeType REFRESH
fld public final static javax.persistence.CascadeType REMOVE
meth public static javax.persistence.CascadeType valueOf(java.lang.String)
meth public static javax.persistence.CascadeType[] values()
supr java.lang.Enum<javax.persistence.CascadeType>

CLSS public abstract interface !annotation javax.persistence.CollectionTable
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String catalog()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String schema()
meth public abstract !hasdefault javax.persistence.ForeignKey foreignKey()
meth public abstract !hasdefault javax.persistence.Index[] indexes()
meth public abstract !hasdefault javax.persistence.JoinColumn[] joinColumns()
meth public abstract !hasdefault javax.persistence.UniqueConstraint[] uniqueConstraints()

CLSS public abstract interface !annotation javax.persistence.Column
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean insertable()
meth public abstract !hasdefault boolean nullable()
meth public abstract !hasdefault boolean unique()
meth public abstract !hasdefault boolean updatable()
meth public abstract !hasdefault int length()
meth public abstract !hasdefault int precision()
meth public abstract !hasdefault int scale()
meth public abstract !hasdefault java.lang.String columnDefinition()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String table()

CLSS public abstract interface !annotation javax.persistence.ColumnResult
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class type()
meth public abstract java.lang.String name()

CLSS public final !enum javax.persistence.ConstraintMode
fld public final static javax.persistence.ConstraintMode CONSTRAINT
fld public final static javax.persistence.ConstraintMode NO_CONSTRAINT
fld public final static javax.persistence.ConstraintMode PROVIDER_DEFAULT
meth public static javax.persistence.ConstraintMode valueOf(java.lang.String)
meth public static javax.persistence.ConstraintMode[] values()
supr java.lang.Enum<javax.persistence.ConstraintMode>

CLSS public abstract interface !annotation javax.persistence.ConstructorResult
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class targetClass()
meth public abstract javax.persistence.ColumnResult[] columns()

CLSS public abstract interface !annotation javax.persistence.Convert
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean disableConversion()
meth public abstract !hasdefault java.lang.Class converter()
meth public abstract !hasdefault java.lang.String attributeName()

CLSS public abstract interface !annotation javax.persistence.Converter
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean autoApply()

CLSS public abstract interface !annotation javax.persistence.Converts
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD, TYPE])
intf java.lang.annotation.Annotation
meth public abstract javax.persistence.Convert[] value()

CLSS public abstract interface !annotation javax.persistence.DiscriminatorColumn
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int length()
meth public abstract !hasdefault java.lang.String columnDefinition()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault javax.persistence.DiscriminatorType discriminatorType()

CLSS public final !enum javax.persistence.DiscriminatorType
fld public final static javax.persistence.DiscriminatorType CHAR
fld public final static javax.persistence.DiscriminatorType INTEGER
fld public final static javax.persistence.DiscriminatorType STRING
meth public static javax.persistence.DiscriminatorType valueOf(java.lang.String)
meth public static javax.persistence.DiscriminatorType[] values()
supr java.lang.Enum<javax.persistence.DiscriminatorType>

CLSS public abstract interface !annotation javax.persistence.DiscriminatorValue
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation javax.persistence.ElementCollection
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class targetClass()
meth public abstract !hasdefault javax.persistence.FetchType fetch()

CLSS public abstract interface !annotation javax.persistence.Embeddable
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.persistence.Embedded
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.persistence.EmbeddedId
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.persistence.Entity
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String name()

CLSS public javax.persistence.EntityExistsException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr javax.persistence.PersistenceException

CLSS public abstract interface javax.persistence.EntityGraph<%0 extends java.lang.Object>
meth public abstract !varargs void addAttributeNodes(java.lang.String[])
meth public abstract !varargs void addAttributeNodes(javax.persistence.metamodel.Attribute<{javax.persistence.EntityGraph%0},?>[])
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<? extends {%%0}> addKeySubgraph(javax.persistence.metamodel.Attribute<{javax.persistence.EntityGraph%0},{%%0}>,java.lang.Class<? extends {%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<? extends {%%0}> addSubclassSubgraph(java.lang.Class<? extends {%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<? extends {%%0}> addSubgraph(javax.persistence.metamodel.Attribute<{javax.persistence.EntityGraph%0},{%%0}>,java.lang.Class<? extends {%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<{%%0}> addKeySubgraph(java.lang.String)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<{%%0}> addKeySubgraph(java.lang.String,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<{%%0}> addKeySubgraph(javax.persistence.metamodel.Attribute<{javax.persistence.EntityGraph%0},{%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<{%%0}> addSubgraph(java.lang.String)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<{%%0}> addSubgraph(java.lang.String,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<{%%0}> addSubgraph(javax.persistence.metamodel.Attribute<{javax.persistence.EntityGraph%0},{%%0}>)
meth public abstract java.lang.String getName()
meth public abstract java.util.List<javax.persistence.AttributeNode<?>> getAttributeNodes()

CLSS public abstract interface !annotation javax.persistence.EntityListeners
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class[] value()

CLSS public abstract interface javax.persistence.EntityManager
meth public abstract !varargs javax.persistence.StoredProcedureQuery createStoredProcedureQuery(java.lang.String,java.lang.Class[])
meth public abstract !varargs javax.persistence.StoredProcedureQuery createStoredProcedureQuery(java.lang.String,java.lang.String[])
meth public abstract <%0 extends java.lang.Object> java.util.List<javax.persistence.EntityGraph<? super {%%0}>> getEntityGraphs(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.EntityGraph<{%%0}> createEntityGraph(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.TypedQuery<{%%0}> createNamedQuery(java.lang.String,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.TypedQuery<{%%0}> createQuery(java.lang.String,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.TypedQuery<{%%0}> createQuery(javax.persistence.criteria.CriteriaQuery<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} find(java.lang.Class<{%%0}>,java.lang.Object)
meth public abstract <%0 extends java.lang.Object> {%%0} find(java.lang.Class<{%%0}>,java.lang.Object,java.util.Map<java.lang.String,java.lang.Object>)
meth public abstract <%0 extends java.lang.Object> {%%0} find(java.lang.Class<{%%0}>,java.lang.Object,javax.persistence.LockModeType)
meth public abstract <%0 extends java.lang.Object> {%%0} find(java.lang.Class<{%%0}>,java.lang.Object,javax.persistence.LockModeType,java.util.Map<java.lang.String,java.lang.Object>)
meth public abstract <%0 extends java.lang.Object> {%%0} getReference(java.lang.Class<{%%0}>,java.lang.Object)
meth public abstract <%0 extends java.lang.Object> {%%0} merge({%%0})
meth public abstract <%0 extends java.lang.Object> {%%0} unwrap(java.lang.Class<{%%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean isJoinedToTransaction()
meth public abstract boolean isOpen()
meth public abstract java.lang.Object getDelegate()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public abstract javax.persistence.EntityGraph<?> createEntityGraph(java.lang.String)
meth public abstract javax.persistence.EntityGraph<?> getEntityGraph(java.lang.String)
meth public abstract javax.persistence.EntityManagerFactory getEntityManagerFactory()
meth public abstract javax.persistence.EntityTransaction getTransaction()
meth public abstract javax.persistence.FlushModeType getFlushMode()
meth public abstract javax.persistence.LockModeType getLockMode(java.lang.Object)
meth public abstract javax.persistence.Query createNamedQuery(java.lang.String)
meth public abstract javax.persistence.Query createNativeQuery(java.lang.String)
meth public abstract javax.persistence.Query createNativeQuery(java.lang.String,java.lang.Class)
meth public abstract javax.persistence.Query createNativeQuery(java.lang.String,java.lang.String)
meth public abstract javax.persistence.Query createQuery(java.lang.String)
meth public abstract javax.persistence.Query createQuery(javax.persistence.criteria.CriteriaDelete)
meth public abstract javax.persistence.Query createQuery(javax.persistence.criteria.CriteriaUpdate)
meth public abstract javax.persistence.StoredProcedureQuery createNamedStoredProcedureQuery(java.lang.String)
meth public abstract javax.persistence.StoredProcedureQuery createStoredProcedureQuery(java.lang.String)
meth public abstract javax.persistence.criteria.CriteriaBuilder getCriteriaBuilder()
meth public abstract javax.persistence.metamodel.Metamodel getMetamodel()
meth public abstract void clear()
meth public abstract void close()
meth public abstract void detach(java.lang.Object)
meth public abstract void flush()
meth public abstract void joinTransaction()
meth public abstract void lock(java.lang.Object,javax.persistence.LockModeType)
meth public abstract void lock(java.lang.Object,javax.persistence.LockModeType,java.util.Map<java.lang.String,java.lang.Object>)
meth public abstract void persist(java.lang.Object)
meth public abstract void refresh(java.lang.Object)
meth public abstract void refresh(java.lang.Object,java.util.Map<java.lang.String,java.lang.Object>)
meth public abstract void refresh(java.lang.Object,javax.persistence.LockModeType)
meth public abstract void refresh(java.lang.Object,javax.persistence.LockModeType,java.util.Map<java.lang.String,java.lang.Object>)
meth public abstract void remove(java.lang.Object)
meth public abstract void setFlushMode(javax.persistence.FlushModeType)
meth public abstract void setProperty(java.lang.String,java.lang.Object)

CLSS public abstract interface javax.persistence.EntityManagerFactory
meth public abstract <%0 extends java.lang.Object> void addNamedEntityGraph(java.lang.String,javax.persistence.EntityGraph<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} unwrap(java.lang.Class<{%%0}>)
meth public abstract boolean isOpen()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public abstract javax.persistence.Cache getCache()
meth public abstract javax.persistence.EntityManager createEntityManager()
meth public abstract javax.persistence.EntityManager createEntityManager(java.util.Map)
meth public abstract javax.persistence.EntityManager createEntityManager(javax.persistence.SynchronizationType)
meth public abstract javax.persistence.EntityManager createEntityManager(javax.persistence.SynchronizationType,java.util.Map)
meth public abstract javax.persistence.PersistenceUnitUtil getPersistenceUnitUtil()
meth public abstract javax.persistence.criteria.CriteriaBuilder getCriteriaBuilder()
meth public abstract javax.persistence.metamodel.Metamodel getMetamodel()
meth public abstract void addNamedQuery(java.lang.String,javax.persistence.Query)
meth public abstract void close()

CLSS public javax.persistence.EntityNotFoundException
cons public init()
cons public init(java.lang.String)
supr javax.persistence.PersistenceException

CLSS public abstract interface !annotation javax.persistence.EntityResult
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String discriminatorColumn()
meth public abstract !hasdefault javax.persistence.FieldResult[] fields()
meth public abstract java.lang.Class entityClass()

CLSS public abstract interface javax.persistence.EntityTransaction
meth public abstract boolean getRollbackOnly()
meth public abstract boolean isActive()
meth public abstract void begin()
meth public abstract void commit()
meth public abstract void rollback()
meth public abstract void setRollbackOnly()

CLSS public final !enum javax.persistence.EnumType
fld public final static javax.persistence.EnumType ORDINAL
fld public final static javax.persistence.EnumType STRING
meth public static javax.persistence.EnumType valueOf(java.lang.String)
meth public static javax.persistence.EnumType[] values()
supr java.lang.Enum<javax.persistence.EnumType>

CLSS public abstract interface !annotation javax.persistence.Enumerated
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.persistence.EnumType value()

CLSS public abstract interface !annotation javax.persistence.ExcludeDefaultListeners
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.persistence.ExcludeSuperclassListeners
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public final !enum javax.persistence.FetchType
fld public final static javax.persistence.FetchType EAGER
fld public final static javax.persistence.FetchType LAZY
meth public static javax.persistence.FetchType valueOf(java.lang.String)
meth public static javax.persistence.FetchType[] values()
supr java.lang.Enum<javax.persistence.FetchType>

CLSS public abstract interface !annotation javax.persistence.FieldResult
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String column()
meth public abstract java.lang.String name()

CLSS public final !enum javax.persistence.FlushModeType
fld public final static javax.persistence.FlushModeType AUTO
fld public final static javax.persistence.FlushModeType COMMIT
meth public static javax.persistence.FlushModeType valueOf(java.lang.String)
meth public static javax.persistence.FlushModeType[] values()
supr java.lang.Enum<javax.persistence.FlushModeType>

CLSS public abstract interface !annotation javax.persistence.ForeignKey
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String foreignKeyDefinition()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault javax.persistence.ConstraintMode value()

CLSS public abstract interface !annotation javax.persistence.GeneratedValue
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String generator()
meth public abstract !hasdefault javax.persistence.GenerationType strategy()

CLSS public final !enum javax.persistence.GenerationType
fld public final static javax.persistence.GenerationType AUTO
fld public final static javax.persistence.GenerationType IDENTITY
fld public final static javax.persistence.GenerationType SEQUENCE
fld public final static javax.persistence.GenerationType TABLE
meth public static javax.persistence.GenerationType valueOf(java.lang.String)
meth public static javax.persistence.GenerationType[] values()
supr java.lang.Enum<javax.persistence.GenerationType>

CLSS public abstract interface !annotation javax.persistence.Id
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.persistence.IdClass
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class value()

CLSS public abstract interface !annotation javax.persistence.Index
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean unique()
meth public abstract !hasdefault java.lang.String name()
meth public abstract java.lang.String columnList()

CLSS public abstract interface !annotation javax.persistence.Inheritance
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.persistence.InheritanceType strategy()

CLSS public final !enum javax.persistence.InheritanceType
fld public final static javax.persistence.InheritanceType JOINED
fld public final static javax.persistence.InheritanceType SINGLE_TABLE
fld public final static javax.persistence.InheritanceType TABLE_PER_CLASS
meth public static javax.persistence.InheritanceType valueOf(java.lang.String)
meth public static javax.persistence.InheritanceType[] values()
supr java.lang.Enum<javax.persistence.InheritanceType>

CLSS public abstract interface !annotation javax.persistence.JoinColumn
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean insertable()
meth public abstract !hasdefault boolean nullable()
meth public abstract !hasdefault boolean unique()
meth public abstract !hasdefault boolean updatable()
meth public abstract !hasdefault java.lang.String columnDefinition()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String referencedColumnName()
meth public abstract !hasdefault java.lang.String table()
meth public abstract !hasdefault javax.persistence.ForeignKey foreignKey()

CLSS public abstract interface !annotation javax.persistence.JoinColumns
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.persistence.ForeignKey foreignKey()
meth public abstract javax.persistence.JoinColumn[] value()

CLSS public abstract interface !annotation javax.persistence.JoinTable
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String catalog()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String schema()
meth public abstract !hasdefault javax.persistence.ForeignKey foreignKey()
meth public abstract !hasdefault javax.persistence.ForeignKey inverseForeignKey()
meth public abstract !hasdefault javax.persistence.Index[] indexes()
meth public abstract !hasdefault javax.persistence.JoinColumn[] inverseJoinColumns()
meth public abstract !hasdefault javax.persistence.JoinColumn[] joinColumns()
meth public abstract !hasdefault javax.persistence.UniqueConstraint[] uniqueConstraints()

CLSS public abstract interface !annotation javax.persistence.Lob
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation

CLSS public final !enum javax.persistence.LockModeType
fld public final static javax.persistence.LockModeType NONE
fld public final static javax.persistence.LockModeType OPTIMISTIC
fld public final static javax.persistence.LockModeType OPTIMISTIC_FORCE_INCREMENT
fld public final static javax.persistence.LockModeType PESSIMISTIC_FORCE_INCREMENT
fld public final static javax.persistence.LockModeType PESSIMISTIC_READ
fld public final static javax.persistence.LockModeType PESSIMISTIC_WRITE
fld public final static javax.persistence.LockModeType READ
fld public final static javax.persistence.LockModeType WRITE
meth public static javax.persistence.LockModeType valueOf(java.lang.String)
meth public static javax.persistence.LockModeType[] values()
supr java.lang.Enum<javax.persistence.LockModeType>

CLSS public javax.persistence.LockTimeoutException
cons public init()
cons public init(java.lang.Object)
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,java.lang.Throwable,java.lang.Object)
cons public init(java.lang.Throwable)
meth public java.lang.Object getObject()
supr javax.persistence.PersistenceException
hfds entity

CLSS public abstract interface !annotation javax.persistence.ManyToMany
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class targetEntity()
meth public abstract !hasdefault java.lang.String mappedBy()
meth public abstract !hasdefault javax.persistence.CascadeType[] cascade()
meth public abstract !hasdefault javax.persistence.FetchType fetch()

CLSS public abstract interface !annotation javax.persistence.ManyToOne
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean optional()
meth public abstract !hasdefault java.lang.Class targetEntity()
meth public abstract !hasdefault javax.persistence.CascadeType[] cascade()
meth public abstract !hasdefault javax.persistence.FetchType fetch()

CLSS public abstract interface !annotation javax.persistence.MapKey
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String name()

CLSS public abstract interface !annotation javax.persistence.MapKeyClass
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class value()

CLSS public abstract interface !annotation javax.persistence.MapKeyColumn
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean insertable()
meth public abstract !hasdefault boolean nullable()
meth public abstract !hasdefault boolean unique()
meth public abstract !hasdefault boolean updatable()
meth public abstract !hasdefault int length()
meth public abstract !hasdefault int precision()
meth public abstract !hasdefault int scale()
meth public abstract !hasdefault java.lang.String columnDefinition()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String table()

CLSS public abstract interface !annotation javax.persistence.MapKeyEnumerated
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.persistence.EnumType value()

CLSS public abstract interface !annotation javax.persistence.MapKeyJoinColumn
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean insertable()
meth public abstract !hasdefault boolean nullable()
meth public abstract !hasdefault boolean unique()
meth public abstract !hasdefault boolean updatable()
meth public abstract !hasdefault java.lang.String columnDefinition()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String referencedColumnName()
meth public abstract !hasdefault java.lang.String table()
meth public abstract !hasdefault javax.persistence.ForeignKey foreignKey()

CLSS public abstract interface !annotation javax.persistence.MapKeyJoinColumns
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.persistence.ForeignKey foreignKey()
meth public abstract javax.persistence.MapKeyJoinColumn[] value()

CLSS public abstract interface !annotation javax.persistence.MapKeyTemporal
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract javax.persistence.TemporalType value()

CLSS public abstract interface !annotation javax.persistence.MappedSuperclass
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.persistence.MapsId
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface !annotation javax.persistence.NamedAttributeNode
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String keySubgraph()
meth public abstract !hasdefault java.lang.String subgraph()
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation javax.persistence.NamedEntityGraph
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean includeAllAttributes()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault javax.persistence.NamedAttributeNode[] attributeNodes()
meth public abstract !hasdefault javax.persistence.NamedSubgraph[] subclassSubgraphs()
meth public abstract !hasdefault javax.persistence.NamedSubgraph[] subgraphs()

CLSS public abstract interface !annotation javax.persistence.NamedEntityGraphs
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract javax.persistence.NamedEntityGraph[] value()

CLSS public abstract interface !annotation javax.persistence.NamedNativeQueries
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract javax.persistence.NamedNativeQuery[] value()

CLSS public abstract interface !annotation javax.persistence.NamedNativeQuery
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class resultClass()
meth public abstract !hasdefault java.lang.String resultSetMapping()
meth public abstract !hasdefault javax.persistence.QueryHint[] hints()
meth public abstract java.lang.String name()
meth public abstract java.lang.String query()

CLSS public abstract interface !annotation javax.persistence.NamedQueries
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract javax.persistence.NamedQuery[] value()

CLSS public abstract interface !annotation javax.persistence.NamedQuery
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.persistence.LockModeType lockMode()
meth public abstract !hasdefault javax.persistence.QueryHint[] hints()
meth public abstract java.lang.String name()
meth public abstract java.lang.String query()

CLSS public abstract interface !annotation javax.persistence.NamedStoredProcedureQueries
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract javax.persistence.NamedStoredProcedureQuery[] value()

CLSS public abstract interface !annotation javax.persistence.NamedStoredProcedureQuery
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class[] resultClasses()
meth public abstract !hasdefault java.lang.String[] resultSetMappings()
meth public abstract !hasdefault javax.persistence.QueryHint[] hints()
meth public abstract !hasdefault javax.persistence.StoredProcedureParameter[] parameters()
meth public abstract java.lang.String name()
meth public abstract java.lang.String procedureName()

CLSS public abstract interface !annotation javax.persistence.NamedSubgraph
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class type()
meth public abstract java.lang.String name()
meth public abstract javax.persistence.NamedAttributeNode[] attributeNodes()

CLSS public javax.persistence.NoResultException
cons public init()
cons public init(java.lang.String)
supr javax.persistence.PersistenceException

CLSS public javax.persistence.NonUniqueResultException
cons public init()
cons public init(java.lang.String)
supr javax.persistence.PersistenceException

CLSS public abstract interface !annotation javax.persistence.OneToMany
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean orphanRemoval()
meth public abstract !hasdefault java.lang.Class targetEntity()
meth public abstract !hasdefault java.lang.String mappedBy()
meth public abstract !hasdefault javax.persistence.CascadeType[] cascade()
meth public abstract !hasdefault javax.persistence.FetchType fetch()

CLSS public abstract interface !annotation javax.persistence.OneToOne
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean optional()
meth public abstract !hasdefault boolean orphanRemoval()
meth public abstract !hasdefault java.lang.Class targetEntity()
meth public abstract !hasdefault java.lang.String mappedBy()
meth public abstract !hasdefault javax.persistence.CascadeType[] cascade()
meth public abstract !hasdefault javax.persistence.FetchType fetch()

CLSS public javax.persistence.OptimisticLockException
cons public init()
cons public init(java.lang.Object)
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,java.lang.Throwable,java.lang.Object)
cons public init(java.lang.Throwable)
meth public java.lang.Object getEntity()
supr javax.persistence.PersistenceException
hfds entity

CLSS public abstract interface !annotation javax.persistence.OrderBy
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface !annotation javax.persistence.OrderColumn
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean insertable()
meth public abstract !hasdefault boolean nullable()
meth public abstract !hasdefault boolean updatable()
meth public abstract !hasdefault java.lang.String columnDefinition()
meth public abstract !hasdefault java.lang.String name()

CLSS public abstract interface javax.persistence.Parameter<%0 extends java.lang.Object>
meth public abstract java.lang.Class<{javax.persistence.Parameter%0}> getParameterType()
meth public abstract java.lang.Integer getPosition()
meth public abstract java.lang.String getName()

CLSS public final !enum javax.persistence.ParameterMode
fld public final static javax.persistence.ParameterMode IN
fld public final static javax.persistence.ParameterMode INOUT
fld public final static javax.persistence.ParameterMode OUT
fld public final static javax.persistence.ParameterMode REF_CURSOR
meth public static javax.persistence.ParameterMode valueOf(java.lang.String)
meth public static javax.persistence.ParameterMode[] values()
supr java.lang.Enum<javax.persistence.ParameterMode>

CLSS public javax.persistence.Persistence
cons public init()
fld protected final static java.util.Set<javax.persistence.spi.PersistenceProvider> providers
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PERSISTENCE_PROVIDER = "javax.persistence.spi.PeristenceProvider"
 anno 0 java.lang.Deprecated()
meth public static javax.persistence.EntityManagerFactory createEntityManagerFactory(java.lang.String)
meth public static javax.persistence.EntityManagerFactory createEntityManagerFactory(java.lang.String,java.util.Map)
meth public static javax.persistence.PersistenceUtil getPersistenceUtil()
meth public static void generateSchema(java.lang.String,java.util.Map)
supr java.lang.Object
hcls PersistenceUtilImpl

CLSS public abstract interface !annotation javax.persistence.PersistenceContext
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String unitName()
meth public abstract !hasdefault javax.persistence.PersistenceContextType type()
meth public abstract !hasdefault javax.persistence.PersistenceProperty[] properties()
meth public abstract !hasdefault javax.persistence.SynchronizationType synchronization()

CLSS public final !enum javax.persistence.PersistenceContextType
fld public final static javax.persistence.PersistenceContextType EXTENDED
fld public final static javax.persistence.PersistenceContextType TRANSACTION
meth public static javax.persistence.PersistenceContextType valueOf(java.lang.String)
meth public static javax.persistence.PersistenceContextType[] values()
supr java.lang.Enum<javax.persistence.PersistenceContextType>

CLSS public abstract interface !annotation javax.persistence.PersistenceContexts
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract javax.persistence.PersistenceContext[] value()

CLSS public javax.persistence.PersistenceException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public abstract interface !annotation javax.persistence.PersistenceProperty
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String name()
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation javax.persistence.PersistenceUnit
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String unitName()

CLSS public abstract interface javax.persistence.PersistenceUnitUtil
intf javax.persistence.PersistenceUtil
meth public abstract boolean isLoaded(java.lang.Object)
meth public abstract boolean isLoaded(java.lang.Object,java.lang.String)
meth public abstract java.lang.Object getIdentifier(java.lang.Object)

CLSS public abstract interface !annotation javax.persistence.PersistenceUnits
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract javax.persistence.PersistenceUnit[] value()

CLSS public abstract interface javax.persistence.PersistenceUtil
meth public abstract boolean isLoaded(java.lang.Object)
meth public abstract boolean isLoaded(java.lang.Object,java.lang.String)

CLSS public javax.persistence.PessimisticLockException
cons public init()
cons public init(java.lang.Object)
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,java.lang.Throwable,java.lang.Object)
cons public init(java.lang.Throwable)
meth public java.lang.Object getEntity()
supr javax.persistence.PersistenceException
hfds entity

CLSS public final !enum javax.persistence.PessimisticLockScope
fld public final static javax.persistence.PessimisticLockScope EXTENDED
fld public final static javax.persistence.PessimisticLockScope NORMAL
meth public static javax.persistence.PessimisticLockScope valueOf(java.lang.String)
meth public static javax.persistence.PessimisticLockScope[] values()
supr java.lang.Enum<javax.persistence.PessimisticLockScope>

CLSS public abstract interface !annotation javax.persistence.PostLoad
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.persistence.PostPersist
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.persistence.PostRemove
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.persistence.PostUpdate
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.persistence.PrePersist
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.persistence.PreRemove
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.persistence.PreUpdate
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.persistence.PrimaryKeyJoinColumn
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String columnDefinition()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String referencedColumnName()
meth public abstract !hasdefault javax.persistence.ForeignKey foreignKey()

CLSS public abstract interface !annotation javax.persistence.PrimaryKeyJoinColumns
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.persistence.ForeignKey foreignKey()
meth public abstract javax.persistence.PrimaryKeyJoinColumn[] value()

CLSS public abstract interface javax.persistence.Query
meth public abstract <%0 extends java.lang.Object> javax.persistence.Parameter<{%%0}> getParameter(int,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Parameter<{%%0}> getParameter(java.lang.String,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Query setParameter(javax.persistence.Parameter<{%%0}>,{%%0})
meth public abstract <%0 extends java.lang.Object> {%%0} getParameterValue(javax.persistence.Parameter<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} unwrap(java.lang.Class<{%%0}>)
meth public abstract boolean isBound(javax.persistence.Parameter<?>)
meth public abstract int executeUpdate()
meth public abstract int getFirstResult()
meth public abstract int getMaxResults()
meth public abstract java.lang.Object getParameterValue(int)
meth public abstract java.lang.Object getParameterValue(java.lang.String)
meth public abstract java.lang.Object getSingleResult()
meth public abstract java.util.List getResultList()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getHints()
meth public abstract java.util.Set<javax.persistence.Parameter<?>> getParameters()
meth public abstract javax.persistence.FlushModeType getFlushMode()
meth public abstract javax.persistence.LockModeType getLockMode()
meth public abstract javax.persistence.Parameter<?> getParameter(int)
meth public abstract javax.persistence.Parameter<?> getParameter(java.lang.String)
meth public abstract javax.persistence.Query setFirstResult(int)
meth public abstract javax.persistence.Query setFlushMode(javax.persistence.FlushModeType)
meth public abstract javax.persistence.Query setHint(java.lang.String,java.lang.Object)
meth public abstract javax.persistence.Query setLockMode(javax.persistence.LockModeType)
meth public abstract javax.persistence.Query setMaxResults(int)
meth public abstract javax.persistence.Query setParameter(int,java.lang.Object)
meth public abstract javax.persistence.Query setParameter(int,java.util.Calendar,javax.persistence.TemporalType)
meth public abstract javax.persistence.Query setParameter(int,java.util.Date,javax.persistence.TemporalType)
meth public abstract javax.persistence.Query setParameter(java.lang.String,java.lang.Object)
meth public abstract javax.persistence.Query setParameter(java.lang.String,java.util.Calendar,javax.persistence.TemporalType)
meth public abstract javax.persistence.Query setParameter(java.lang.String,java.util.Date,javax.persistence.TemporalType)
meth public abstract javax.persistence.Query setParameter(javax.persistence.Parameter<java.util.Calendar>,java.util.Calendar,javax.persistence.TemporalType)
meth public abstract javax.persistence.Query setParameter(javax.persistence.Parameter<java.util.Date>,java.util.Date,javax.persistence.TemporalType)

CLSS public abstract interface !annotation javax.persistence.QueryHint
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String name()
meth public abstract java.lang.String value()

CLSS public javax.persistence.QueryTimeoutException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,java.lang.Throwable,javax.persistence.Query)
cons public init(java.lang.Throwable)
cons public init(javax.persistence.Query)
meth public javax.persistence.Query getQuery()
supr javax.persistence.PersistenceException
hfds query

CLSS public javax.persistence.RollbackException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr javax.persistence.PersistenceException

CLSS public abstract interface !annotation javax.persistence.SecondaryTable
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String catalog()
meth public abstract !hasdefault java.lang.String schema()
meth public abstract !hasdefault javax.persistence.ForeignKey foreignKey()
meth public abstract !hasdefault javax.persistence.Index[] indexes()
meth public abstract !hasdefault javax.persistence.PrimaryKeyJoinColumn[] pkJoinColumns()
meth public abstract !hasdefault javax.persistence.UniqueConstraint[] uniqueConstraints()
meth public abstract java.lang.String name()

CLSS public abstract interface !annotation javax.persistence.SecondaryTables
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract javax.persistence.SecondaryTable[] value()

CLSS public abstract interface !annotation javax.persistence.SequenceGenerator
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int allocationSize()
meth public abstract !hasdefault int initialValue()
meth public abstract !hasdefault java.lang.String catalog()
meth public abstract !hasdefault java.lang.String schema()
meth public abstract !hasdefault java.lang.String sequenceName()
meth public abstract java.lang.String name()

CLSS public final !enum javax.persistence.SharedCacheMode
fld public final static javax.persistence.SharedCacheMode ALL
fld public final static javax.persistence.SharedCacheMode DISABLE_SELECTIVE
fld public final static javax.persistence.SharedCacheMode ENABLE_SELECTIVE
fld public final static javax.persistence.SharedCacheMode NONE
fld public final static javax.persistence.SharedCacheMode UNSPECIFIED
meth public static javax.persistence.SharedCacheMode valueOf(java.lang.String)
meth public static javax.persistence.SharedCacheMode[] values()
supr java.lang.Enum<javax.persistence.SharedCacheMode>

CLSS public abstract interface !annotation javax.persistence.SqlResultSetMapping
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.persistence.ColumnResult[] columns()
meth public abstract !hasdefault javax.persistence.ConstructorResult[] classes()
meth public abstract !hasdefault javax.persistence.EntityResult[] entities()
meth public abstract java.lang.String name()

CLSS public abstract interface !annotation javax.persistence.SqlResultSetMappings
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract javax.persistence.SqlResultSetMapping[] value()

CLSS public abstract interface !annotation javax.persistence.StoredProcedureParameter
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault javax.persistence.ParameterMode mode()
meth public abstract java.lang.Class type()

CLSS public abstract interface javax.persistence.StoredProcedureQuery
intf javax.persistence.Query
meth public abstract <%0 extends java.lang.Object> javax.persistence.StoredProcedureQuery setParameter(javax.persistence.Parameter<{%%0}>,{%%0})
meth public abstract boolean execute()
meth public abstract boolean hasMoreResults()
meth public abstract int executeUpdate()
meth public abstract int getUpdateCount()
meth public abstract java.lang.Object getOutputParameterValue(int)
meth public abstract java.lang.Object getOutputParameterValue(java.lang.String)
meth public abstract java.lang.Object getSingleResult()
meth public abstract java.util.List getResultList()
meth public abstract javax.persistence.StoredProcedureQuery registerStoredProcedureParameter(int,java.lang.Class,javax.persistence.ParameterMode)
meth public abstract javax.persistence.StoredProcedureQuery registerStoredProcedureParameter(java.lang.String,java.lang.Class,javax.persistence.ParameterMode)
meth public abstract javax.persistence.StoredProcedureQuery setFlushMode(javax.persistence.FlushModeType)
meth public abstract javax.persistence.StoredProcedureQuery setHint(java.lang.String,java.lang.Object)
meth public abstract javax.persistence.StoredProcedureQuery setParameter(int,java.lang.Object)
meth public abstract javax.persistence.StoredProcedureQuery setParameter(int,java.util.Calendar,javax.persistence.TemporalType)
meth public abstract javax.persistence.StoredProcedureQuery setParameter(int,java.util.Date,javax.persistence.TemporalType)
meth public abstract javax.persistence.StoredProcedureQuery setParameter(java.lang.String,java.lang.Object)
meth public abstract javax.persistence.StoredProcedureQuery setParameter(java.lang.String,java.util.Calendar,javax.persistence.TemporalType)
meth public abstract javax.persistence.StoredProcedureQuery setParameter(java.lang.String,java.util.Date,javax.persistence.TemporalType)
meth public abstract javax.persistence.StoredProcedureQuery setParameter(javax.persistence.Parameter<java.util.Calendar>,java.util.Calendar,javax.persistence.TemporalType)
meth public abstract javax.persistence.StoredProcedureQuery setParameter(javax.persistence.Parameter<java.util.Date>,java.util.Date,javax.persistence.TemporalType)

CLSS public abstract interface javax.persistence.Subgraph<%0 extends java.lang.Object>
meth public abstract !varargs void addAttributeNodes(java.lang.String[])
meth public abstract !varargs void addAttributeNodes(javax.persistence.metamodel.Attribute<{javax.persistence.Subgraph%0},?>[])
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<? extends {%%0}> addKeySubgraph(javax.persistence.metamodel.Attribute<{javax.persistence.Subgraph%0},{%%0}>,java.lang.Class<? extends {%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<? extends {%%0}> addSubgraph(javax.persistence.metamodel.Attribute<{javax.persistence.Subgraph%0},{%%0}>,java.lang.Class<? extends {%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<{%%0}> addKeySubgraph(java.lang.String)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<{%%0}> addKeySubgraph(java.lang.String,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<{%%0}> addKeySubgraph(javax.persistence.metamodel.Attribute<{javax.persistence.Subgraph%0},{%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<{%%0}> addSubgraph(java.lang.String)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<{%%0}> addSubgraph(java.lang.String,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> javax.persistence.Subgraph<{%%0}> addSubgraph(javax.persistence.metamodel.Attribute<{javax.persistence.Subgraph%0},{%%0}>)
meth public abstract java.lang.Class<{javax.persistence.Subgraph%0}> getClassType()
meth public abstract java.util.List<javax.persistence.AttributeNode<?>> getAttributeNodes()

CLSS public final !enum javax.persistence.SynchronizationType
fld public final static javax.persistence.SynchronizationType SYNCHRONIZED
fld public final static javax.persistence.SynchronizationType UNSYNCHRONIZED
meth public static javax.persistence.SynchronizationType valueOf(java.lang.String)
meth public static javax.persistence.SynchronizationType[] values()
supr java.lang.Enum<javax.persistence.SynchronizationType>

CLSS public abstract interface !annotation javax.persistence.Table
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String catalog()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String schema()
meth public abstract !hasdefault javax.persistence.Index[] indexes()
meth public abstract !hasdefault javax.persistence.UniqueConstraint[] uniqueConstraints()

CLSS public abstract interface !annotation javax.persistence.TableGenerator
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int allocationSize()
meth public abstract !hasdefault int initialValue()
meth public abstract !hasdefault java.lang.String catalog()
meth public abstract !hasdefault java.lang.String pkColumnName()
meth public abstract !hasdefault java.lang.String pkColumnValue()
meth public abstract !hasdefault java.lang.String schema()
meth public abstract !hasdefault java.lang.String table()
meth public abstract !hasdefault java.lang.String valueColumnName()
meth public abstract !hasdefault javax.persistence.Index[] indexes()
meth public abstract !hasdefault javax.persistence.UniqueConstraint[] uniqueConstraints()
meth public abstract java.lang.String name()

CLSS public abstract interface !annotation javax.persistence.Temporal
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract javax.persistence.TemporalType value()

CLSS public final !enum javax.persistence.TemporalType
fld public final static javax.persistence.TemporalType DATE
fld public final static javax.persistence.TemporalType TIME
fld public final static javax.persistence.TemporalType TIMESTAMP
meth public static javax.persistence.TemporalType valueOf(java.lang.String)
meth public static javax.persistence.TemporalType[] values()
supr java.lang.Enum<javax.persistence.TemporalType>

CLSS public javax.persistence.TransactionRequiredException
cons public init()
cons public init(java.lang.String)
supr javax.persistence.PersistenceException

CLSS public abstract interface !annotation javax.persistence.Transient
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation

CLSS public abstract interface javax.persistence.Tuple
meth public abstract <%0 extends java.lang.Object> {%%0} get(int,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} get(java.lang.String,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} get(javax.persistence.TupleElement<{%%0}>)
meth public abstract java.lang.Object get(int)
meth public abstract java.lang.Object get(java.lang.String)
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.List<javax.persistence.TupleElement<?>> getElements()

CLSS public abstract interface javax.persistence.TupleElement<%0 extends java.lang.Object>
meth public abstract java.lang.Class<? extends {javax.persistence.TupleElement%0}> getJavaType()
meth public abstract java.lang.String getAlias()

CLSS public abstract interface javax.persistence.TypedQuery<%0 extends java.lang.Object>
intf javax.persistence.Query
meth public abstract <%0 extends java.lang.Object> javax.persistence.TypedQuery<{javax.persistence.TypedQuery%0}> setParameter(javax.persistence.Parameter<{%%0}>,{%%0})
meth public abstract java.util.List<{javax.persistence.TypedQuery%0}> getResultList()
meth public abstract javax.persistence.TypedQuery<{javax.persistence.TypedQuery%0}> setFirstResult(int)
meth public abstract javax.persistence.TypedQuery<{javax.persistence.TypedQuery%0}> setFlushMode(javax.persistence.FlushModeType)
meth public abstract javax.persistence.TypedQuery<{javax.persistence.TypedQuery%0}> setHint(java.lang.String,java.lang.Object)
meth public abstract javax.persistence.TypedQuery<{javax.persistence.TypedQuery%0}> setLockMode(javax.persistence.LockModeType)
meth public abstract javax.persistence.TypedQuery<{javax.persistence.TypedQuery%0}> setMaxResults(int)
meth public abstract javax.persistence.TypedQuery<{javax.persistence.TypedQuery%0}> setParameter(int,java.lang.Object)
meth public abstract javax.persistence.TypedQuery<{javax.persistence.TypedQuery%0}> setParameter(int,java.util.Calendar,javax.persistence.TemporalType)
meth public abstract javax.persistence.TypedQuery<{javax.persistence.TypedQuery%0}> setParameter(int,java.util.Date,javax.persistence.TemporalType)
meth public abstract javax.persistence.TypedQuery<{javax.persistence.TypedQuery%0}> setParameter(java.lang.String,java.lang.Object)
meth public abstract javax.persistence.TypedQuery<{javax.persistence.TypedQuery%0}> setParameter(java.lang.String,java.util.Calendar,javax.persistence.TemporalType)
meth public abstract javax.persistence.TypedQuery<{javax.persistence.TypedQuery%0}> setParameter(java.lang.String,java.util.Date,javax.persistence.TemporalType)
meth public abstract javax.persistence.TypedQuery<{javax.persistence.TypedQuery%0}> setParameter(javax.persistence.Parameter<java.util.Calendar>,java.util.Calendar,javax.persistence.TemporalType)
meth public abstract javax.persistence.TypedQuery<{javax.persistence.TypedQuery%0}> setParameter(javax.persistence.Parameter<java.util.Date>,java.util.Date,javax.persistence.TemporalType)
meth public abstract {javax.persistence.TypedQuery%0} getSingleResult()

CLSS public abstract interface !annotation javax.persistence.UniqueConstraint
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String name()
meth public abstract java.lang.String[] columnNames()

CLSS public final !enum javax.persistence.ValidationMode
fld public final static javax.persistence.ValidationMode AUTO
fld public final static javax.persistence.ValidationMode CALLBACK
fld public final static javax.persistence.ValidationMode NONE
meth public static javax.persistence.ValidationMode valueOf(java.lang.String)
meth public static javax.persistence.ValidationMode[] values()
supr java.lang.Enum<javax.persistence.ValidationMode>

CLSS public abstract interface !annotation javax.persistence.Version
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD])
intf java.lang.annotation.Annotation

CLSS abstract interface javax.persistence.package-info

