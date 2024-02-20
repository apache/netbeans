#Signature file v4.1
#Version 1.70

CLSS public abstract interface java.beans.BeanInfo
fld public final static int ICON_COLOR_16x16 = 1
fld public final static int ICON_COLOR_32x32 = 2
fld public final static int ICON_MONO_16x16 = 3
fld public final static int ICON_MONO_32x32 = 4
meth public abstract int getDefaultEventIndex()
meth public abstract int getDefaultPropertyIndex()
meth public abstract java.awt.Image getIcon(int)
meth public abstract java.beans.BeanDescriptor getBeanDescriptor()
meth public abstract java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public abstract java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public abstract java.beans.MethodDescriptor[] getMethodDescriptors()
meth public abstract java.beans.PropertyDescriptor[] getPropertyDescriptors()

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

CLSS public java.beans.SimpleBeanInfo
cons public init()
intf java.beans.BeanInfo
meth public int getDefaultEventIndex()
meth public int getDefaultPropertyIndex()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image loadImage(java.lang.String)
meth public java.beans.BeanDescriptor getBeanDescriptor()
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public java.beans.MethodDescriptor[] getMethodDescriptors()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.lang.Object

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

CLSS public abstract interface java.util.Iterator<%0 extends java.lang.Object>
meth public abstract boolean hasNext()
meth public abstract {java.util.Iterator%0} next()
meth public void forEachRemaining(java.util.function.Consumer<? super {java.util.Iterator%0}>)
meth public void remove()

CLSS public org.netbeans.modules.schema2beans.AttrProp
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,int,java.lang.String[],java.lang.String)
fld public final static int CDATA = 1
fld public final static int ENTITIES = 8
fld public final static int ENTITY = 7
fld public final static int ENUM = 2
fld public final static int FIXED = 768
fld public final static int ID = 4
fld public final static int IDREF = 5
fld public final static int IDREFS = 6
fld public final static int IMPLIED = 512
fld public final static int MASK_KIND = 255
fld public final static int MASK_OPTION = 3840
fld public final static int NMTOKEN = 3
fld public final static int NOTATION = 9
fld public final static int REQUIRED = 256
fld public final static int TRANSIENT = 4096
intf org.netbeans.modules.schema2beans.BaseAttribute
meth public boolean hasName(java.lang.String)
meth public boolean isComplete()
meth public boolean isEnum()
meth public boolean isFixed()
meth public boolean isTransient()
meth public int getInstance()
meth public int getOption()
meth public int getType()
meth public java.lang.String getDefaultValue()
meth public java.lang.String getDtdName()
meth public java.lang.String getJavaType()
meth public java.lang.String getName()
meth public java.lang.String getNamespace()
meth public java.lang.String getPropertyName()
meth public java.lang.String toString()
meth public java.lang.String typeAsString()
meth public java.lang.String[] getValues()
meth public java.util.List getExtraData()
meth public void addExtraData(java.lang.Object)
meth public void addValue(java.lang.String)
meth public void addValue(java.lang.String,java.lang.String)
meth public void checkEnum()
meth public void setDefaultValue(java.lang.String)
meth public void setEnum(boolean)
meth public void setJavaType(java.lang.String)
meth public void setName(java.lang.String)
meth public void validate()
supr java.lang.Object
hfds DONE,NEED_DEFVAL,NEED_ENUM,NEED_NAME,NEED_OPTION,NEED_TYPE,NEED_VALUE,defaultValue,dtdName,enumMode,extraData,javaType,kindValues,kinds,name,namespace,optionValues,options,propertyName,state,type,values

CLSS public abstract interface org.netbeans.modules.schema2beans.BaseAttribute
fld public final static int OPTION_FIXED = 768
fld public final static int OPTION_IMPLIED = 512
fld public final static int OPTION_REQUIRED = 256
fld public final static int TYPE_CDATA = 1
fld public final static int TYPE_ENTITIES = 8
fld public final static int TYPE_ENTITY = 7
fld public final static int TYPE_ENUM = 2
fld public final static int TYPE_ID = 4
fld public final static int TYPE_IDREF = 5
fld public final static int TYPE_IDREFS = 6
fld public final static int TYPE_NMTOKEN = 3
fld public final static int TYPE_NOTATION = 9
meth public abstract boolean hasName(java.lang.String)
meth public abstract boolean isEnum()
meth public abstract boolean isFixed()
meth public abstract boolean isTransient()
meth public abstract int getOption()
meth public abstract int getType()
meth public abstract java.lang.String getDefaultValue()
meth public abstract java.lang.String getDtdName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String[] getValues()

CLSS public abstract org.netbeans.modules.schema2beans.BaseBean
cons public init(java.util.Vector,org.netbeans.modules.schema2beans.Version)
fld protected org.netbeans.modules.schema2beans.DOMBinding binding
fld protected org.netbeans.modules.schema2beans.GraphManager graphManager
fld public final static int MERGE_COMPARE = 4
fld public final static int MERGE_INTERSECT = 1
fld public final static int MERGE_NONE = 0
fld public final static int MERGE_UNION = 2
fld public final static int MERGE_UPDATE = 3
innr public IterateChoiceProperties
intf java.lang.Cloneable
intf org.netbeans.modules.schema2beans.Bean
meth protected boolean hasDomNode()
meth protected int addValue(org.netbeans.modules.schema2beans.BeanProp,java.lang.Object)
meth protected int removeValue(org.netbeans.modules.schema2beans.BeanProp,java.lang.Object)
meth protected java.util.Iterator beanPropsIterator()
meth protected org.netbeans.modules.schema2beans.DOMBinding domBinding()
meth protected void addKnownValue(java.lang.String,java.lang.Object)
meth protected void buildPathName(java.lang.StringBuffer)
meth protected void copyProperty(org.netbeans.modules.schema2beans.BeanProp,org.netbeans.modules.schema2beans.BaseBean,int,java.lang.Object)
meth protected void init(java.util.Vector,org.netbeans.modules.schema2beans.Version)
meth protected void initPropertyTables(int)
meth protected void removeValue(org.netbeans.modules.schema2beans.BeanProp,int)
meth protected void setDomBinding(org.netbeans.modules.schema2beans.DOMBinding)
meth protected void setGraphManager(org.netbeans.modules.schema2beans.GraphManager)
meth protected void setValue(org.netbeans.modules.schema2beans.BeanProp,int,java.lang.Object)
meth public abstract void dump(java.lang.StringBuffer,java.lang.String)
meth public boolean hasName(java.lang.String)
meth public boolean isChoiceProperty()
meth public boolean isChoiceProperty(java.lang.String)
meth public boolean isEqualTo(java.lang.Object)
meth public boolean isNull(java.lang.String)
meth public boolean isNull(java.lang.String,int)
meth public boolean isRoot()
meth public int addValue(java.lang.String,java.lang.Object)
meth public int idToIndex(java.lang.String,int)
meth public int indexOf(java.lang.String,java.lang.Object)
meth public int indexToId(java.lang.String,int)
meth public int removeValue(java.lang.String,java.lang.Object)
meth public int size(java.lang.String)
meth public java.lang.Object clone()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.Object getValue(java.lang.String,int)
meth public java.lang.Object getValueById(java.lang.String,int)
meth public java.lang.Object[] getValues(java.lang.String)
meth public java.lang.Object[] knownValues(java.lang.String)
meth public java.lang.String _getXPathExpr()
meth public java.lang.String _getXPathExpr(java.lang.Object)
meth public java.lang.String dtdName()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String dumpDomNode()
meth public java.lang.String dumpDomNode(int)
meth public java.lang.String dumpDomNode(java.lang.String,int)
meth public java.lang.String fullName()
meth public java.lang.String getAttributeValue(java.lang.String)
meth public java.lang.String getAttributeValue(java.lang.String,int,java.lang.String)
meth public java.lang.String getAttributeValue(java.lang.String,java.lang.String)
meth public java.lang.String getDefaultNamespace()
meth public java.lang.String name()
meth public java.lang.String nameChild(java.lang.Object)
meth public java.lang.String nameChild(java.lang.Object,boolean,boolean)
meth public java.lang.String nameChild(java.lang.Object,boolean,boolean,boolean)
meth public java.lang.String nameSelf()
meth public java.lang.String toString()
meth public java.lang.String[] findAttributeValue(java.lang.String,java.lang.String)
meth public java.lang.String[] findPropertyValue(java.lang.String,java.lang.Object)
meth public java.lang.String[] findValue(java.lang.Object)
meth public java.lang.String[] getAttributeNames()
meth public java.lang.String[] getAttributeNames(java.lang.String)
meth public java.util.Iterator listChoiceProperties()
meth public org.netbeans.modules.schema2beans.BaseAttribute[] listAttributes()
meth public org.netbeans.modules.schema2beans.BaseAttribute[] listAttributes(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseBean newInstance(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseBean parent()
meth public org.netbeans.modules.schema2beans.BaseBean[] childBeans(boolean)
meth public org.netbeans.modules.schema2beans.BaseProperty getProperty()
meth public org.netbeans.modules.schema2beans.BaseProperty getProperty(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseProperty[] listChoiceProperties(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseProperty[] listProperties()
meth public org.netbeans.modules.schema2beans.Bean _getParent()
meth public org.netbeans.modules.schema2beans.Bean _getRoot()
meth public org.netbeans.modules.schema2beans.Bean propertyById(java.lang.String,int)
meth public org.netbeans.modules.schema2beans.BeanProp beanProp()
meth public org.netbeans.modules.schema2beans.BeanProp beanProp(int)
meth public org.netbeans.modules.schema2beans.BeanProp beanProp(java.lang.String)
meth public org.netbeans.modules.schema2beans.BeanProp[] beanProps()
meth public org.netbeans.modules.schema2beans.GraphManager graphManager()
meth public org.w3c.dom.Comment addComment(java.lang.String)
meth public org.w3c.dom.Comment[] comments()
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream,boolean) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream,boolean,org.xml.sax.EntityResolver) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream,boolean,org.xml.sax.EntityResolver,org.xml.sax.ErrorHandler) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public void _setChanged(boolean)
meth public void addBeanComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void changeDocType(java.lang.String,java.lang.String)
meth public void childBeans(boolean,java.util.List)
meth public void createAttribute(java.lang.String,java.lang.String,int,java.lang.String[],java.lang.String)
meth public void createAttribute(java.lang.String,java.lang.String,java.lang.String,int,java.lang.String[],java.lang.String)
meth public void createBean(org.w3c.dom.Node,org.netbeans.modules.schema2beans.GraphManager)
meth public void createProperty(java.lang.String,java.lang.String,int,java.lang.Class)
meth public void createProperty(java.lang.String,java.lang.String,java.lang.Class)
meth public void createRoot(java.lang.String,java.lang.String,int,java.lang.Class)
meth public void dumpAttributes(java.lang.String,int,java.lang.StringBuffer,java.lang.String)
meth public void dumpXml()
meth public void merge(org.netbeans.modules.schema2beans.BaseBean)
meth public void merge(org.netbeans.modules.schema2beans.BaseBean,int)
meth public void mergeUpdate(org.netbeans.modules.schema2beans.BaseBean)
meth public void reindent()
meth public void reindent(java.lang.String)
meth public void removeBeanComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void removeComment(org.w3c.dom.Comment)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void removeValue(java.lang.String,int)
meth public void setAttributeValue(java.lang.String,int,java.lang.String,java.lang.String)
meth public void setAttributeValue(java.lang.String,java.lang.String)
meth public void setAttributeValue(java.lang.String,java.lang.String,java.lang.String)
meth public void setDefaultNamespace(java.lang.String)
meth public void setValue(java.lang.String,int,java.lang.Object)
meth public void setValue(java.lang.String,java.lang.Object)
meth public void setValue(java.lang.String,java.lang.Object[])
meth public void setValueById(java.lang.String,int,java.lang.Object)
meth public void write(java.io.File) throws java.io.IOException
meth public void write(java.io.OutputStream) throws java.io.IOException
meth public void write(java.io.OutputStream,java.lang.String) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void write(java.io.Writer) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void write(java.io.Writer,java.lang.String) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void writeNoReindent(java.io.OutputStream) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void writeNode(java.io.Writer) throws java.io.IOException
supr java.lang.Object
hfds attrCache,changeListeners,comparators,defaultNamespace,isRoot,propByName,propByOrder,propertyOrder

CLSS public org.netbeans.modules.schema2beans.BaseBean$IterateChoiceProperties
 outer org.netbeans.modules.schema2beans.BaseBean
cons public init(org.netbeans.modules.schema2beans.BaseBean)
intf java.util.Iterator
meth public boolean hasNext()
meth public java.lang.Object next()
meth public void remove()
supr java.lang.Object
hfds groups,index

CLSS public abstract interface org.netbeans.modules.schema2beans.BaseProperty
fld public final static int INSTANCE_MANDATORY_ARRAY = 64
fld public final static int INSTANCE_MANDATORY_ELT = 32
fld public final static int INSTANCE_OPTIONAL_ARRAY = 48
fld public final static int INSTANCE_OPTIONAL_ELT = 16
innr public static VetoException
meth public abstract boolean hasName(java.lang.String)
meth public abstract boolean isBean()
meth public abstract boolean isChoiceProperty()
meth public abstract boolean isIndexed()
meth public abstract boolean isKey()
meth public abstract boolean isRoot()
meth public abstract int getInstanceType()
meth public abstract int size()
meth public abstract java.lang.Class getPropertyClass()
meth public abstract java.lang.String getDtdName()
meth public abstract java.lang.String getFullName()
meth public abstract java.lang.String getFullName(int)
meth public abstract java.lang.String getName()
meth public abstract java.lang.String[] getAttributeNames()
meth public abstract org.netbeans.modules.schema2beans.BaseAttribute[] getAttributes()
meth public abstract org.netbeans.modules.schema2beans.BaseBean getParent()
meth public abstract org.netbeans.modules.schema2beans.BaseProperty[] getChoiceProperties()

CLSS public static org.netbeans.modules.schema2beans.BaseProperty$VetoException
 outer org.netbeans.modules.schema2beans.BaseProperty
cons public init(java.beans.PropertyVetoException,java.lang.String)
meth public java.beans.PropertyVetoException getPropertyVetoException()
supr java.lang.RuntimeException
hfds pce

CLSS public abstract interface org.netbeans.modules.schema2beans.Bean
meth public abstract boolean hasName(java.lang.String)
meth public abstract boolean isRoot()
meth public abstract int addValue(java.lang.String,java.lang.Object)
meth public abstract int idToIndex(java.lang.String,int)
meth public abstract int indexToId(java.lang.String,int)
meth public abstract int removeValue(java.lang.String,java.lang.Object)
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract java.lang.Object getValue(java.lang.String,int)
meth public abstract java.lang.Object getValueById(java.lang.String,int)
meth public abstract java.lang.Object[] getValues(java.lang.String)
meth public abstract java.lang.String dtdName()
meth public abstract java.lang.String name()
meth public abstract org.netbeans.modules.schema2beans.BaseProperty getProperty()
meth public abstract org.netbeans.modules.schema2beans.BaseProperty getProperty(java.lang.String)
meth public abstract org.netbeans.modules.schema2beans.BaseProperty[] listProperties()
meth public abstract org.netbeans.modules.schema2beans.Bean _getParent()
meth public abstract org.netbeans.modules.schema2beans.Bean _getRoot()
meth public abstract org.netbeans.modules.schema2beans.Bean propertyById(java.lang.String,int)
meth public abstract org.netbeans.modules.schema2beans.BeanProp beanProp(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void childBeans(boolean,java.util.List)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setValue(java.lang.String,int,java.lang.Object)
meth public abstract void setValue(java.lang.String,java.lang.Object)
meth public abstract void setValueById(java.lang.String,int,java.lang.Object)

CLSS public org.netbeans.modules.schema2beans.BeanComparator
cons public init()
meth protected boolean hasKey()
meth public java.lang.Object compareProperty(java.lang.String,org.netbeans.modules.schema2beans.BaseBean,java.lang.Object,int,org.netbeans.modules.schema2beans.BaseBean,java.lang.Object,int)
meth public org.netbeans.modules.schema2beans.BaseBean compareBean(java.lang.String,org.netbeans.modules.schema2beans.BaseBean,org.netbeans.modules.schema2beans.BaseBean)
meth public static void enableComparatorsKey(boolean)
meth public void enableKey(boolean)
supr java.lang.Object
hfds hasKey,useComparatorsMddKeys,useMddKeys

CLSS public org.netbeans.modules.schema2beans.BeanProp
cons public init(org.netbeans.modules.schema2beans.BaseBean,java.lang.String,java.lang.String,int,java.lang.Class)
cons public init(org.netbeans.modules.schema2beans.BaseBean,java.lang.String,java.lang.String,int,java.lang.Class,boolean)
cons public init(org.netbeans.modules.schema2beans.BaseBean,java.lang.String,java.lang.String,int,java.lang.Class,boolean,int)
cons public init(org.netbeans.modules.schema2beans.BaseBean,java.lang.String,java.lang.String,int,java.lang.Class,int)
fld public int type
fld public java.lang.Class propClass
fld public java.lang.String beanName
fld public java.lang.String dtdName
intf org.netbeans.modules.schema2beans.BaseProperty
meth protected int bindingsSize()
meth protected int setElement(int,java.lang.Object,boolean)
meth protected java.lang.Object[] getObjectArray(int)
meth protected void addKnownValue(java.lang.Object)
meth public boolean hasName(java.lang.String)
meth public boolean isBean()
meth public boolean isChoiceProperty()
meth public boolean isIndexed()
meth public boolean isKey()
meth public boolean isRoot()
meth public int addValue(java.lang.Object)
meth public int getInstanceType()
meth public int getType()
meth public int idToIndex(int)
meth public int indexToId(int)
meth public int removeValue(java.lang.Object)
meth public int size()
meth public java.lang.Class getPropClass()
meth public java.lang.Class getPropertyClass()
meth public java.lang.Object getValue(int)
meth public java.lang.Object getValueById(int)
meth public java.lang.Object[] getValues()
meth public java.lang.Object[] knownValues()
meth public java.lang.String getAttributeValue(int,java.lang.String)
meth public java.lang.String getBeanName()
meth public java.lang.String getDtdName()
meth public java.lang.String getFullName()
meth public java.lang.String getFullName(int)
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.lang.String[] getAttributeNames()
meth public org.netbeans.modules.schema2beans.AttrProp getAttrProp(java.lang.String)
meth public org.netbeans.modules.schema2beans.AttrProp getAttrProp(java.lang.String,boolean)
meth public org.netbeans.modules.schema2beans.BaseAttribute[] getAttributes()
meth public org.netbeans.modules.schema2beans.BaseBean getBean()
meth public org.netbeans.modules.schema2beans.BaseBean getParent()
meth public org.netbeans.modules.schema2beans.BaseProperty[] getChoiceProperties()
meth public org.netbeans.modules.schema2beans.DOMBinding registerDomNode(org.w3c.dom.Node,org.netbeans.modules.schema2beans.DOMBinding,org.netbeans.modules.schema2beans.BaseBean) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public void addPCListener(java.beans.PropertyChangeListener)
meth public void addVCListener(java.beans.VetoableChangeListener)
meth public void createAttribute(java.lang.String,java.lang.String,int,java.lang.String[],java.lang.String)
meth public void createTransientAttribute(java.lang.String)
meth public void initialize()
meth public void removePCListener(java.beans.PropertyChangeListener)
meth public void removeVCListener(java.beans.VetoableChangeListener)
meth public void removeValue(int)
meth public void setAttributeValue(int,java.lang.String,java.lang.String)
meth public void setValue(int,java.lang.Object)
meth public void setValue(java.lang.Object[])
supr java.lang.Object
hfds OP_SETTER_ADD,OP_SETTER_REMOVE,OP_SETTER_SETARRAY,OP_SETTER_SETELT,attributes,bean,bindings,changeListeners,eventMgr,group,handleEvents,handleVetoEvents,isRoot,knownValues,order,vetoableListeners
hcls Action,EventMgr,GroupProp,InternalEvent

CLSS public org.netbeans.modules.schema2beans.Common
cons public init()
fld public final static int ATTLIST = 3
fld public final static int COMMENT = 1
fld public final static int ELEMENT = 2
fld public final static int MASK_INSTANCE = 240
fld public final static int MASK_PROP = 983040
fld public final static int MASK_SEQUENCE = 15
fld public final static int MASK_TYPE = 65280
fld public final static int MASK_USER = 65535
fld public final static int NONE = 0
fld public final static int NO_DEFAULT_VALUES = 2
fld public final static int SEQUENCE_AND = 1
fld public final static int SEQUENCE_OR = 2
fld public final static int TYPE_0_1 = 16
fld public final static int TYPE_0_N = 48
fld public final static int TYPE_1 = 32
fld public final static int TYPE_1_N = 64
fld public final static int TYPE_BEAN = 512
fld public final static int TYPE_BOOLEAN = 768
fld public final static int TYPE_BYTE = 1024
fld public final static int TYPE_CHAR = 1280
fld public final static int TYPE_COMMENT = 3840
fld public final static int TYPE_DOUBLE = 2560
fld public final static int TYPE_FLOAT = 2304
fld public final static int TYPE_INT = 1792
fld public final static int TYPE_KEY = 65536
fld public final static int TYPE_LONG = 2048
fld public final static int TYPE_SHORT = 1536
fld public final static int TYPE_SHOULD_NOT_BE_EMPTY = 131072
fld public final static int TYPE_STRING = 256
fld public final static int TYPE_VETOABLE = 1048576
fld public final static int USE_DEFAULT_VALUES = 1
fld public final static java.lang.String CLASS_BOOLEAN = "Boolean"
fld public final static java.lang.String CLASS_STRING = "String"
fld public final static java.lang.String DTD_EMPTY = "EMPTY"
fld public final static java.lang.String DTD_STRING = "#PCDATA"
fld public final static java.lang.String GENERATED_TAG = "Generated"
meth public static boolean isArray(int)
meth public static boolean isBean(int)
meth public static boolean isBoolean(int)
meth public static boolean isKey(int)
meth public static boolean isScalar(int)
meth public static boolean isSequenceOr(int)
meth public static boolean isString(int)
meth public static boolean isVetoable(int)
meth public static boolean shouldNotBeEmpty(int)
meth public static int widestInstance(int,int)
meth public static int wrapperToType(java.lang.String)
meth public static java.lang.Object defaultScalarValue(int)
meth public static java.lang.Object getComparableObject(java.lang.Object)
meth public static java.lang.String constName(java.lang.String)
meth public static java.lang.String convertName(java.lang.String)
meth public static java.lang.String convertNameInstance(java.lang.String)
meth public static java.lang.String dumpHex(java.lang.String)
meth public static java.lang.String getMessage(java.lang.String)
meth public static java.lang.String getMessage(java.lang.String,int)
meth public static java.lang.String getMessage(java.lang.String,java.lang.Object)
meth public static java.lang.String getMessage(java.lang.String,java.lang.Object,java.lang.Object)
meth public static java.lang.String getMessage(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object)
meth public static java.lang.String getMessage(java.lang.String,java.lang.Object[])
meth public static java.lang.String instanceToCommonString(int)
meth public static java.lang.String instanceToString(int)
meth public static java.lang.String scalarType(int)
meth public static java.lang.String typeToString(int)
meth public static java.lang.String wrapperClass(int)
meth public static java.lang.String wrapperGetMethod(int)
supr java.lang.Object
hfds rbName

CLSS public org.netbeans.modules.schema2beans.DDBeanInfo
cons public init()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo
hfds BEANINFO,properties,propertiesInited

CLSS public org.netbeans.modules.schema2beans.DDFactory
cons public init()
meth public static java.lang.String XmlToString(org.w3c.dom.Node)
meth public static java.lang.String XmlToString(org.w3c.dom.Node,int)
meth public static java.lang.String XmlToString(org.w3c.dom.Node,int,java.lang.String)
meth public static org.netbeans.modules.schema2beans.BaseBean create(java.io.InputStream,java.lang.String) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static void register(java.lang.String,java.lang.Class)
meth public static void register(java.lang.String,java.lang.String) throws java.lang.ClassNotFoundException
supr java.lang.Object
hfds beanClassMap,idCount

CLSS public org.netbeans.modules.schema2beans.DDLogFlags
cons public init()
fld public final static int ADDTYPE = 1
fld public final static int BEANCOMP = 21
fld public final static int BINDPROP = 13
fld public final static int BOUNDNODE = 10
fld public final static int CACHING = 18
fld public final static int CREATEATTR = 23
fld public final static int CREATECHG = 1
fld public final static int CREATEREM = 2
fld public final static int DBG_BLD = 13
fld public final static int DBG_DTD = 12
fld public final static int DBG_EVT = 14
fld public final static int DBG_REG = 16
fld public final static int DBG_UBN = 15
fld public final static int DDBEANED = 8
fld public final static int DDCREATE = 6
fld public final static int DDCREATED = 7
fld public final static int DELETENODE = 20
fld public final static int ELEMENT = 5
fld public final static int ENDDOC = 2
fld public final static int ENDELT = 4
fld public final static int ENDGRP = 7
fld public final static int EQUALS = 5
fld public final static int EXCEEDED = 14
fld public final static int FIND = 6
fld public final static int FINDATTR = 7
fld public final static int FINDCMP = 8
fld public final static int FINDNODE = 1
fld public final static int FINDPROP = 10
fld public final static int FNDATTR = 9
fld public final static int FNDPROP = 11
fld public final static int FOUNDNODE = 9
fld public final static int GETATTR = 24
fld public final static int GETIDXTEXT = 4
fld public final static int GETTEXT = 2
fld public final static int MAX_DBG_GROUP = 16
fld public final static int MERGE = 1
fld public final static int MERGEFOUND = 3
fld public final static int MERGENTFND = 4
fld public final static int MERGEPROP = 2
fld public final static int NEWBIND = 16
fld public final static int NONODE = 11
fld public final static int NOTELT = 12
fld public final static int NOTIFYCHG = 3
fld public final static int NOTIFYREM = 4
fld public final static int NOTIFYVETO = 6
fld public final static int PROPCOMP = 22
fld public final static int SETIDXTEXT = 5
fld public final static int SETTEXT = 3
fld public final static int SETVALUE = 15
fld public final static int STARTDOC = 1
fld public final static int STARTELT = 3
fld public final static int STARTGRP = 6
fld public final static int SYNCING = 19
fld public final static int SYNCNODES = 17
fld public final static int VETOABLE = 5
fld public java.lang.Object[] actionSets
fld public java.lang.String[] dbgNames
fld public static boolean debug
meth public static void setDebug(boolean)
supr java.lang.Object
hfds bldActionNames,dtdActionNames,evtActionNames,regActionNames,ubnActionNames

CLSS public org.netbeans.modules.schema2beans.DDParser
cons public init(org.netbeans.modules.schema2beans.BaseBean,java.lang.String)
innr public static DDLocation
intf java.util.Iterator
meth public boolean hasNext()
meth public java.lang.Object current()
meth public java.lang.Object next()
meth public org.netbeans.modules.schema2beans.DDParser$DDLocation getLocation()
meth public void remove()
supr java.lang.Object
hfds current,empty,parser,root,singleRoot
hcls PropParser

CLSS public static org.netbeans.modules.schema2beans.DDParser$DDLocation
 outer org.netbeans.modules.schema2beans.DDParser
cons public init(org.netbeans.modules.schema2beans.BaseBean)
cons public init(org.netbeans.modules.schema2beans.BaseBean,java.lang.String,int,int)
meth public boolean isNode()
meth public int getIndex()
meth public java.lang.Object getValue()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public org.netbeans.modules.schema2beans.BaseBean getRoot()
meth public void removeValue()
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds index,name,root,type

CLSS public org.netbeans.modules.schema2beans.DDRegistry
cons public init()
innr public ChangeTracer
innr public static DDChangeMarker
meth public boolean hasGraph(java.lang.String)
meth public boolean hasGraph(java.lang.String,org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor)
meth public boolean hasType(org.netbeans.modules.schema2beans.BaseBean,java.lang.String)
meth public java.lang.String dump()
meth public java.lang.String dumpAll()
meth public java.lang.String getID(org.netbeans.modules.schema2beans.BaseBean)
meth public java.lang.String getID(org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor)
meth public java.lang.String getName(java.lang.String)
meth public java.lang.String getName(org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor)
meth public long getTimestamp(org.netbeans.modules.schema2beans.BaseBean)
meth public org.netbeans.modules.schema2beans.BaseBean getRoot(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseBean[] getRoots(java.lang.String)
meth public org.netbeans.modules.schema2beans.DDRegistry$DDChangeMarker createChangeMarker()
meth public org.netbeans.modules.schema2beans.DDRegistryParser newParser(java.lang.String)
meth public org.netbeans.modules.schema2beans.DDRegistryParser newParser(org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor,java.lang.String)
meth public org.netbeans.modules.schema2beans.DDRegistryParser newParser(org.netbeans.modules.schema2beans.DDRegistryParser,java.lang.String)
meth public org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor newCursor(java.lang.String)
meth public org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor newCursor(org.netbeans.modules.schema2beans.BaseBean)
meth public org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor newCursor(org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor,java.lang.String)
meth public static java.lang.String createGraphName(java.lang.String)
meth public static java.lang.String getGraphName(java.lang.String)
meth public void addType(org.netbeans.modules.schema2beans.BaseBean,java.lang.String)
meth public void clearCache()
meth public void clearCache(org.netbeans.modules.schema2beans.BaseBean)
meth public void createEntry(org.netbeans.modules.schema2beans.BaseBean,java.lang.String,java.lang.String)
meth public void removeEntry(java.lang.String)
meth public void removeEntry(org.netbeans.modules.schema2beans.BaseBean)
meth public void renameEntry(java.lang.String,java.lang.String)
meth public void renameEntry(java.lang.String,java.lang.String,java.lang.String)
meth public void setTimestamp(org.netbeans.modules.schema2beans.BaseBean)
meth public void updateEntry(java.lang.String,org.netbeans.modules.schema2beans.BaseBean)
supr java.lang.Object
hfds changeTracer,scopes
hcls IterateParser,RegEntry

CLSS public org.netbeans.modules.schema2beans.DDRegistry$ChangeTracer
 outer org.netbeans.modules.schema2beans.DDRegistry
cons public init(org.netbeans.modules.schema2beans.DDRegistry,org.netbeans.modules.schema2beans.DDRegistry)
intf java.beans.PropertyChangeListener
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr java.lang.Object
hfds reg

CLSS public static org.netbeans.modules.schema2beans.DDRegistry$DDChangeMarker
 outer org.netbeans.modules.schema2beans.DDRegistry
meth public boolean hasChanged()
meth public int size()
meth public java.lang.String dump()
meth public java.lang.String toString()
meth public java.lang.StringBuffer dump(java.lang.StringBuffer,java.lang.String,long)
meth public void add(org.netbeans.modules.schema2beans.BaseBean)
meth public void add(org.netbeans.modules.schema2beans.DDRegistry$DDChangeMarker)
meth public void add(org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor)
meth public void remove(org.netbeans.modules.schema2beans.BaseBean)
meth public void remove(org.netbeans.modules.schema2beans.DDRegistry$DDChangeMarker)
meth public void remove(org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor)
meth public void resetTime()
supr java.lang.Object
hfds elts,reg,timestamp

CLSS public org.netbeans.modules.schema2beans.DDRegistryParser
cons public init(org.netbeans.modules.schema2beans.DDRegistry,java.lang.String)
cons public init(org.netbeans.modules.schema2beans.DDRegistry,org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor,java.lang.String)
cons public init(org.netbeans.modules.schema2beans.DDRegistry,org.netbeans.modules.schema2beans.DDRegistryParser,java.lang.String)
innr public static DDCursor
innr public static PathResolver
intf java.util.Iterator
meth public boolean hasNext()
meth public java.lang.Object current()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.Object next()
meth public org.netbeans.modules.schema2beans.DDParser$DDLocation getLocation()
meth public org.netbeans.modules.schema2beans.DDRegistry getRegistry()
meth public org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor getCursor()
meth public void remove()
supr java.lang.Object
hfds CURRENT_CURSOR,parentCursor,parentParser,parser,parserRoot,registry
hcls ParserSet

CLSS public static org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor
 outer org.netbeans.modules.schema2beans.DDRegistryParser
cons public init(org.netbeans.modules.schema2beans.DDRegistry,java.lang.String)
cons public init(org.netbeans.modules.schema2beans.DDRegistry,org.netbeans.modules.schema2beans.BaseBean)
cons public init(org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor,java.lang.String)
cons public init(org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor,org.netbeans.modules.schema2beans.BaseBean)
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String dump()
meth public java.lang.String toString()
meth public org.netbeans.modules.schema2beans.BaseBean getRoot()
meth public org.netbeans.modules.schema2beans.DDRegistry getRegistry()
meth public org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor getParent()
supr java.lang.Object
hfds parent,registry,root

CLSS public static org.netbeans.modules.schema2beans.DDRegistryParser$PathResolver
 outer org.netbeans.modules.schema2beans.DDRegistryParser
cons public init()
cons public init(org.netbeans.modules.schema2beans.DDRegistryParser$DDCursor,java.lang.String)
meth public java.lang.String toString()
supr java.lang.Object
hfds VALUE,VARBEGIN,VAREND,VAR_MODNAME,VAR_PTYPE,VAR_TYPE,VAR_UNAME,result

CLSS public org.netbeans.modules.schema2beans.DOMBinding
cons public init()
cons public init(org.w3c.dom.Node)
meth protected java.lang.Boolean nodeToBoolean(org.netbeans.modules.schema2beans.BeanProp)
meth protected java.lang.String findNamespace(java.lang.String)
meth public int getId()
meth public java.lang.String getDomValue(org.w3c.dom.Node)
meth public static java.lang.String calendarToString(java.util.Calendar)
supr java.lang.Object
hfds charArrayClass,id,node,pos,posDOM,prop
hcls BeanProperty,CacheAttr

CLSS public org.netbeans.modules.schema2beans.GraphManager
cons public init(org.netbeans.modules.schema2beans.BaseBean)
innr public abstract interface static Factory
innr public abstract interface static Writer
meth protected org.netbeans.modules.schema2beans.XMLUtil$DOMWriter getDOMWriter()
meth protected static void printLevel(java.io.OutputStream,int,java.lang.String,java.lang.String) throws java.io.IOException
meth protected static void printLevel(java.io.Writer,int,java.lang.String) throws java.io.IOException
meth protected static void printLevel(java.io.Writer,int,java.lang.String,java.lang.String) throws java.io.IOException
meth protected static void printLevel(java.lang.StringBuffer,int,java.lang.String)
meth protected static void printLevel(java.lang.StringBuffer,int,java.lang.String,java.lang.String)
meth protected void write(java.io.OutputStream,java.lang.String) throws java.io.IOException
meth protected void write(java.io.Writer) throws java.io.IOException
meth protected void write(java.io.Writer,java.lang.String) throws java.io.IOException
meth public boolean isAttribute(java.lang.String)
meth public int getPropertyIndex(java.lang.String)
meth public java.lang.Object defaultScalarValue(int)
meth public java.lang.String getAttributeName(java.lang.String)
meth public java.lang.String getKeyPropertyName(java.lang.String)
meth public java.lang.String getKeyPropertyName(java.lang.String,java.lang.String[],java.lang.String[])
meth public java.lang.String getKeyPropertyName(java.lang.String,java.lang.String[],java.lang.String[],boolean)
meth public org.netbeans.modules.schema2beans.BaseBean getBeanRoot()
meth public org.netbeans.modules.schema2beans.BaseBean getPropertyParent(java.lang.String)
meth public org.netbeans.modules.schema2beans.NodeFactory getNodeFactory()
meth public org.w3c.dom.Document getXmlDocument()
meth public static int getPropertyIndex(org.netbeans.modules.schema2beans.Bean,java.lang.String)
meth public static java.lang.String getKeyPropertyName(org.netbeans.modules.schema2beans.Bean,java.lang.String,java.lang.String[],java.lang.String[],boolean)
meth public static java.lang.String getPropertyName(java.lang.String)
meth public static java.lang.String getPropertyParentName(java.lang.String)
meth public static java.lang.String trimPropertyName(java.lang.String)
meth public static org.netbeans.modules.schema2beans.Bean getPropertyParent(org.netbeans.modules.schema2beans.Bean,java.lang.String)
meth public static org.w3c.dom.Document createXmlDocument(java.io.InputStream,boolean)
meth public static org.w3c.dom.Document createXmlDocument(java.io.InputStream,boolean,org.xml.sax.EntityResolver)
meth public static org.w3c.dom.Document createXmlDocument(org.xml.sax.InputSource,boolean) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.w3c.dom.Document createXmlDocument(org.xml.sax.InputSource,boolean,org.xml.sax.EntityResolver,org.xml.sax.ErrorHandler) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.w3c.dom.Node createRootElementNode(java.lang.String)
meth public static org.w3c.dom.Node getElementNode(java.lang.String,org.w3c.dom.Node)
meth public static void debug(boolean)
meth public static void setFactory(java.io.InputStream,org.netbeans.modules.schema2beans.GraphManager$Factory) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static void setFactory(java.io.InputStream,org.netbeans.modules.schema2beans.GraphManager$Factory,org.netbeans.modules.schema2beans.GraphManager$Writer) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public void completeRootBinding(org.netbeans.modules.schema2beans.BaseBean,org.w3c.dom.Node)
meth public void createRootBinding(org.netbeans.modules.schema2beans.BaseBean,org.netbeans.modules.schema2beans.BeanProp,org.w3c.dom.Node) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public void fillProperties(org.netbeans.modules.schema2beans.BeanProp[],org.w3c.dom.Node) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public void reindent(java.lang.String)
meth public void setDoctype(java.lang.String,java.lang.String)
meth public void setNodeFactory(org.w3c.dom.Document)
meth public void setWriteCData(boolean)
meth public void setWriter(org.netbeans.modules.schema2beans.GraphManager$Writer)
meth public void setXmlDocument(org.w3c.dom.Node)
meth public void write(java.io.Writer,org.w3c.dom.Node) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
supr java.lang.Object
hfds bindingsMap,docFactory,docTypePublic,docTypeSystem,docWriter,document,factory,factoryMap,root,writeCData,writerMap

CLSS public abstract interface static org.netbeans.modules.schema2beans.GraphManager$Factory
 outer org.netbeans.modules.schema2beans.GraphManager
meth public abstract org.w3c.dom.Document createDocument(java.io.InputStream,boolean)

CLSS public abstract interface static org.netbeans.modules.schema2beans.GraphManager$Writer
 outer org.netbeans.modules.schema2beans.GraphManager
meth public abstract void write(java.io.OutputStream,org.w3c.dom.Document)

CLSS public org.netbeans.modules.schema2beans.JavaBeansUtil
innr public abstract interface static BeanWriter
innr public abstract static IndentingBeanWriter
innr public static HtmlBeanWriter
innr public static XmlBeanWriter
meth public static boolean isJavaBeanType(java.lang.Class)
meth public static java.lang.Object convertValue(java.lang.Class,java.lang.String)
meth public static java.lang.Object dummyBean(java.lang.Class,int) throws java.beans.IntrospectionException
meth public static java.lang.Object dummyValue(java.lang.Class,int)
meth public static java.lang.Object readBean(java.lang.Class,java.io.InputStream) throws java.beans.IntrospectionException,java.io.IOException,java.lang.IllegalAccessException,java.lang.InstantiationException,java.lang.NoSuchMethodException,java.lang.reflect.InvocationTargetException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static java.lang.Object readBean(java.lang.Class,org.w3c.dom.Document) throws java.beans.IntrospectionException,java.lang.IllegalAccessException,java.lang.InstantiationException,java.lang.NoSuchMethodException,java.lang.reflect.InvocationTargetException
meth public static void copyBean(java.lang.Object,java.lang.Object) throws java.beans.IntrospectionException
meth public static void copyBean(java.lang.Object,java.lang.Object,java.util.Map) throws java.beans.IntrospectionException
meth public static void genReadType(java.io.Writer,java.lang.String) throws java.io.IOException
meth public static void genWriteType(java.io.Writer,java.lang.String) throws java.io.IOException
meth public static void readBean(java.lang.Object,java.io.InputStream) throws java.beans.IntrospectionException,java.io.IOException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static void readBean(java.lang.Object,org.w3c.dom.Document) throws java.beans.IntrospectionException
meth public static void readBean(java.lang.Object,org.w3c.dom.Node) throws java.beans.IntrospectionException
meth public static void readBean(java.lang.Object,org.xml.sax.InputSource,boolean,org.xml.sax.EntityResolver,org.xml.sax.ErrorHandler) throws java.beans.IntrospectionException,java.io.IOException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static void readBeanNoEntityResolver(java.lang.Object,java.io.InputStream) throws java.beans.IntrospectionException,java.io.IOException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static void writeBean(java.lang.Object,java.io.Writer) throws java.beans.IntrospectionException,java.io.IOException
meth public static void writeBean(java.lang.Object,org.netbeans.modules.schema2beans.JavaBeansUtil$BeanWriter) throws java.beans.IntrospectionException,java.io.IOException
meth public static void writeBean(java.lang.Object,org.netbeans.modules.schema2beans.JavaBeansUtil$BeanWriter,java.util.Map) throws java.beans.IntrospectionException,java.io.IOException
meth public static void writeBeanProperty(java.lang.Object,java.io.Writer,java.lang.String) throws java.beans.IntrospectionException,java.io.IOException
meth public static void writeBeanProperty(java.lang.Object,org.netbeans.modules.schema2beans.JavaBeansUtil$BeanWriter,java.lang.String) throws java.beans.IntrospectionException,java.io.IOException
meth public static void writeBeanProperty(java.lang.Object,org.netbeans.modules.schema2beans.JavaBeansUtil$BeanWriter,java.util.Map,java.lang.String) throws java.beans.IntrospectionException,java.io.IOException
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.schema2beans.JavaBeansUtil$BeanWriter
 outer org.netbeans.modules.schema2beans.JavaBeansUtil
meth public abstract void beginInnerNode() throws java.io.IOException
meth public abstract void beginPropertyName(java.lang.String) throws java.io.IOException
meth public abstract void endInnerNode() throws java.io.IOException
meth public abstract void endPropertyName(java.lang.String) throws java.io.IOException
meth public abstract void writeLeafObject(java.lang.Object) throws java.io.IOException

CLSS public static org.netbeans.modules.schema2beans.JavaBeansUtil$HtmlBeanWriter
 outer org.netbeans.modules.schema2beans.JavaBeansUtil
cons public init(java.io.Writer)
cons public init(java.io.Writer,java.lang.String)
cons public init(java.io.Writer,java.lang.String,java.lang.String)
fld protected java.io.Writer out
intf org.netbeans.modules.schema2beans.JavaBeansUtil$BeanWriter
meth public void beginInnerNode() throws java.io.IOException
meth public void beginPropertyName(java.lang.String) throws java.io.IOException
meth public void endInnerNode() throws java.io.IOException
meth public void endPropertyName(java.lang.String) throws java.io.IOException
meth public void writeLeafObject(java.lang.Object) throws java.io.IOException
supr org.netbeans.modules.schema2beans.JavaBeansUtil$IndentingBeanWriter

CLSS public abstract static org.netbeans.modules.schema2beans.JavaBeansUtil$IndentingBeanWriter
 outer org.netbeans.modules.schema2beans.JavaBeansUtil
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
fld protected int indentLevel
fld protected java.lang.String indent
fld protected java.lang.String indentBy
fld protected java.util.List indentions
intf org.netbeans.modules.schema2beans.JavaBeansUtil$BeanWriter
meth public void beginInnerNode() throws java.io.IOException
meth public void endInnerNode() throws java.io.IOException
supr java.lang.Object

CLSS public static org.netbeans.modules.schema2beans.JavaBeansUtil$XmlBeanWriter
 outer org.netbeans.modules.schema2beans.JavaBeansUtil
cons public init(java.io.Writer)
cons public init(java.io.Writer,java.lang.String)
cons public init(java.io.Writer,java.lang.String,java.lang.String)
fld protected java.io.Writer out
intf org.netbeans.modules.schema2beans.JavaBeansUtil$BeanWriter
meth public void beginInnerNode() throws java.io.IOException
meth public void beginPropertyName(java.lang.String) throws java.io.IOException
meth public void endInnerNode() throws java.io.IOException
meth public void endPropertyName(java.lang.String) throws java.io.IOException
meth public void writeLeafObject(java.lang.Object) throws java.io.IOException
supr org.netbeans.modules.schema2beans.JavaBeansUtil$IndentingBeanWriter

CLSS public org.netbeans.modules.schema2beans.NodeFactory
supr java.lang.Object
hfds factory

CLSS public org.netbeans.modules.schema2beans.NullEntityResolver
fld protected byte[] buf
fld protected java.lang.String dummyDTD
fld protected static org.netbeans.modules.schema2beans.NullEntityResolver theResolver
intf org.xml.sax.EntityResolver
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.schema2beans.NullEntityResolver newInstance()
supr java.lang.Object

CLSS public org.netbeans.modules.schema2beans.QName
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getLocalPart()
meth public java.lang.String getNamespaceURI()
meth public java.lang.String getPrefix()
meth public java.lang.String toString()
meth public static org.netbeans.modules.schema2beans.QName valueOf(java.lang.String)
supr java.lang.Object
hfds localPart,namespaceURI,prefix

CLSS public org.netbeans.modules.schema2beans.ReflectiveBeanProp
cons public init(org.netbeans.modules.schema2beans.BaseBean,java.lang.String,java.lang.String,int,java.lang.Class,boolean,java.lang.reflect.Method,java.lang.reflect.Method,java.lang.reflect.Method,java.lang.reflect.Method,java.lang.reflect.Method,java.lang.reflect.Method)
cons public init(org.netbeans.modules.schema2beans.BaseBean,java.lang.String,java.lang.String,int,java.lang.Class,java.lang.reflect.Method,java.lang.reflect.Method,java.lang.reflect.Method,java.lang.reflect.Method,java.lang.reflect.Method,java.lang.reflect.Method)
meth protected int bindingsSize()
meth protected int setElement(int,java.lang.Object,boolean)
meth protected java.lang.Object[] getObjectArray(int)
meth public int idToIndex(int)
meth public int indexToId(int)
meth public int removeValue(java.lang.Object)
meth public java.lang.Object getValue(int)
meth public java.lang.Object getValueById(int)
meth public java.lang.String getAttributeValue(int,java.lang.String)
meth public java.lang.String getFullName()
meth public java.lang.String getFullName(int)
meth public org.netbeans.modules.schema2beans.DOMBinding registerDomNode(org.w3c.dom.Node,org.netbeans.modules.schema2beans.DOMBinding,org.netbeans.modules.schema2beans.BaseBean) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public void removeValue(int)
meth public void setAttributeValue(int,java.lang.String,java.lang.String)
meth public void setValue(java.lang.Object[])
supr org.netbeans.modules.schema2beans.BeanProp
hfds adder,arrayReader,arrayWriter,reader,remover,writer

CLSS public abstract interface !annotation org.netbeans.modules.schema2beans.Schema2Beans
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE])
innr public abstract interface static !annotation Multiple
innr public final static !enum OutputType
innr public final static !enum SchemaType
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean attrProp()
meth public abstract !hasdefault boolean extendBaseBean()
meth public abstract !hasdefault boolean generateHasChanged()
meth public abstract !hasdefault boolean generateInterfaces()
meth public abstract !hasdefault boolean java5()
meth public abstract !hasdefault boolean removeUnreferencedNodes()
meth public abstract !hasdefault boolean standalone()
meth public abstract !hasdefault boolean useInterfaces()
meth public abstract !hasdefault boolean validate()
meth public abstract !hasdefault java.lang.String commonInterface()
meth public abstract !hasdefault java.lang.String docRoot()
meth public abstract !hasdefault java.lang.String mddFile()
meth public abstract !hasdefault java.lang.String[] finder()
meth public abstract java.lang.String schema()
meth public abstract org.netbeans.modules.schema2beans.Schema2Beans$OutputType outputType()
meth public abstract org.netbeans.modules.schema2beans.Schema2Beans$SchemaType schemaType()

CLSS public abstract interface static !annotation org.netbeans.modules.schema2beans.Schema2Beans$Multiple
 outer org.netbeans.modules.schema2beans.Schema2Beans
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE])
intf java.lang.annotation.Annotation
meth public abstract org.netbeans.modules.schema2beans.Schema2Beans[] value()

CLSS public final static !enum org.netbeans.modules.schema2beans.Schema2Beans$OutputType
 outer org.netbeans.modules.schema2beans.Schema2Beans
fld public final static org.netbeans.modules.schema2beans.Schema2Beans$OutputType JAVABEANS
fld public final static org.netbeans.modules.schema2beans.Schema2Beans$OutputType TRADITIONAL_BASEBEAN
meth public static org.netbeans.modules.schema2beans.Schema2Beans$OutputType valueOf(java.lang.String)
meth public static org.netbeans.modules.schema2beans.Schema2Beans$OutputType[] values()
supr java.lang.Enum<org.netbeans.modules.schema2beans.Schema2Beans$OutputType>

CLSS public final static !enum org.netbeans.modules.schema2beans.Schema2Beans$SchemaType
 outer org.netbeans.modules.schema2beans.Schema2Beans
fld public final static org.netbeans.modules.schema2beans.Schema2Beans$SchemaType DTD
fld public final static org.netbeans.modules.schema2beans.Schema2Beans$SchemaType XML_SCHEMA
meth public static org.netbeans.modules.schema2beans.Schema2Beans$SchemaType valueOf(java.lang.String)
meth public static org.netbeans.modules.schema2beans.Schema2Beans$SchemaType[] values()
supr java.lang.Enum<org.netbeans.modules.schema2beans.Schema2Beans$SchemaType>

CLSS public org.netbeans.modules.schema2beans.Schema2BeansException
cons public init(java.lang.String)
fld protected java.lang.String originalStackTrace
intf java.io.Serializable
meth public java.lang.String getOriginalStackTrace()
meth public void stashOriginalStackTrace()
supr java.lang.Exception

CLSS public org.netbeans.modules.schema2beans.Schema2BeansNestedException
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
fld protected java.lang.String message
fld protected java.lang.String stackTrace
fld protected java.lang.Throwable childThrowable
intf java.io.Serializable
meth protected void genStackTrace()
meth public java.lang.String getMessage()
meth public java.lang.Throwable getCause()
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
supr org.netbeans.modules.schema2beans.Schema2BeansException

CLSS public org.netbeans.modules.schema2beans.Schema2BeansRuntimeException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
fld protected java.lang.String message
fld protected java.lang.String stackTrace
fld protected java.lang.Throwable childThrowable
intf java.io.Serializable
meth protected void genStackTrace()
meth public java.lang.String getMessage()
meth public java.lang.Throwable getCause()
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
supr java.lang.RuntimeException

CLSS public org.netbeans.modules.schema2beans.Schema2BeansUtil
cons public init()
innr public static ReindentationListener
meth public static void mergeUnsupportedElements(org.netbeans.modules.schema2beans.BaseBean,org.netbeans.modules.schema2beans.BaseBean)
meth public static void write(org.netbeans.modules.schema2beans.BaseBean,java.io.OutputStream) throws java.io.IOException
meth public static void write(org.netbeans.modules.schema2beans.BaseBean,java.io.Writer) throws java.io.IOException
supr java.lang.Object

CLSS public static org.netbeans.modules.schema2beans.Schema2BeansUtil$ReindentationListener
 outer org.netbeans.modules.schema2beans.Schema2BeansUtil
cons public init()
intf java.beans.PropertyChangeListener
meth public java.lang.String getIndent()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void setIndent(java.lang.String)
supr java.lang.Object
hfds indent

CLSS public org.netbeans.modules.schema2beans.TraceLogger
cons public init()
fld public static int DEBUG
fld public static int MAXGROUP
fld public static int SVC_DD
fld public static java.io.PrintStream output
meth public static void error(java.lang.String)
meth public static void error(java.lang.Throwable)
meth public static void put(int,int,int,int,int)
meth public static void put(int,int,int,int,int,java.lang.Object)
supr java.lang.Object
hfds flags

CLSS public org.netbeans.modules.schema2beans.ValidateException
cons public init(java.lang.String,java.lang.String,java.lang.Object)
cons public init(java.lang.String,org.netbeans.modules.schema2beans.ValidateException$FailureType,java.lang.String,java.lang.Object)
fld protected java.lang.Object failedBean
fld protected java.lang.String failedPropertyName
fld protected org.netbeans.modules.schema2beans.ValidateException$FailureType failureType
innr public static FailureType
meth public java.lang.Object getFailedBean()
meth public java.lang.String getFailedPropertyName()
meth public org.netbeans.modules.schema2beans.ValidateException$FailureType getFailureType()
supr java.lang.Exception

CLSS public static org.netbeans.modules.schema2beans.ValidateException$FailureType
 outer org.netbeans.modules.schema2beans.ValidateException
fld public final static org.netbeans.modules.schema2beans.ValidateException$FailureType ALL_RESTRICTIONS
fld public final static org.netbeans.modules.schema2beans.ValidateException$FailureType DATA_RESTRICTION
fld public final static org.netbeans.modules.schema2beans.ValidateException$FailureType ENUM_RESTRICTION
fld public final static org.netbeans.modules.schema2beans.ValidateException$FailureType MUTUALLY_EXCLUSIVE
fld public final static org.netbeans.modules.schema2beans.ValidateException$FailureType NULL_VALUE
meth public java.lang.String toString()
supr java.lang.Object
hfds name

CLSS public org.netbeans.modules.schema2beans.Version
cons public init(int,int,int)
fld public final static int MAJVER = 5
fld public final static int MINVER = 0
fld public final static int PTCVER = 0
intf java.io.Serializable
meth public int getMajor()
meth public int getMinor()
meth public int getPatch()
meth public static java.lang.String getVersion()
supr java.lang.Object
hfds major,minor,patch

CLSS public abstract interface org.netbeans.modules.schema2beans.Wrapper
meth public abstract java.lang.String getWrapperValue()
meth public abstract void setWrapperValue(java.lang.String)

CLSS public org.netbeans.modules.schema2beans.XMLUtil
innr public static DOMWriter
meth protected static java.lang.String getDocTypeName(org.w3c.dom.Document)
meth protected static void printLevel(java.lang.StringBuffer,int,java.lang.String)
meth public static boolean isAttrContent(int)
meth public static boolean reindent(org.w3c.dom.Document,org.w3c.dom.Node,int,java.lang.String)
meth public static boolean shouldEscape(char)
meth public static boolean shouldEscape(java.lang.String)
meth public static org.xml.sax.Locator findLocationXPath(org.xml.sax.InputSource,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public static void printXML(java.io.Writer,char,boolean) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static void printXML(java.io.Writer,java.lang.String) throws java.io.IOException
meth public static void printXML(java.io.Writer,java.lang.String,boolean) throws java.io.IOException
meth public static void printXML(java.lang.StringBuffer,char,boolean)
 anno 0 java.lang.Deprecated()
meth public static void printXML(java.lang.StringBuffer,java.lang.String)
meth public static void printXML(java.lang.StringBuffer,java.lang.String,boolean)
meth public static void reindent(org.w3c.dom.Document,java.lang.String)
meth public static void writeXML(java.io.Writer,char,boolean) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static void writeXML(java.io.Writer,java.lang.String) throws java.io.IOException
meth public static void writeXML(java.io.Writer,java.lang.String,boolean) throws java.io.IOException
supr java.lang.Object
hcls XPathLocator

CLSS public static org.netbeans.modules.schema2beans.XMLUtil$DOMWriter
 outer org.netbeans.modules.schema2beans.XMLUtil
cons public init()
meth protected void printXML(java.lang.String,boolean) throws java.io.IOException
meth protected void write(java.lang.String,java.lang.String,java.lang.String,org.w3c.dom.NamedNodeMap) throws java.io.IOException
meth protected void write(org.w3c.dom.Comment) throws java.io.IOException
meth protected void write(org.w3c.dom.DocumentType) throws java.io.IOException
meth protected void write(org.w3c.dom.Entity) throws java.io.IOException
meth protected void write(org.w3c.dom.NamedNodeMap) throws java.io.IOException
meth protected void write(org.w3c.dom.ProcessingInstruction) throws java.io.IOException
meth public void setDocTypePublic(java.lang.String)
meth public void setDocTypeSystem(java.lang.String)
meth public void setWriteCData(boolean)
meth public void setWriter(java.io.Writer)
meth public void write(java.io.File,org.w3c.dom.Document) throws java.io.IOException
meth public void write(java.io.OutputStream,java.lang.String,org.w3c.dom.Document) throws java.io.IOException
meth public void write(java.io.OutputStream,org.w3c.dom.Document) throws java.io.IOException
meth public void write(org.w3c.dom.Document) throws java.io.IOException
meth public void write(org.w3c.dom.Document,java.lang.String) throws java.io.IOException
meth public void write(org.w3c.dom.Document,java.lang.String,boolean) throws java.io.IOException
meth public void write(org.w3c.dom.Node) throws java.io.IOException
supr java.lang.Object
hfds docTypePublic,docTypeSystem,out,writeCData

CLSS public abstract interface org.xml.sax.EntityResolver
meth public abstract org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException

