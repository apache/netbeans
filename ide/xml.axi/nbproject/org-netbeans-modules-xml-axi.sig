#Signature file v4.1
#Version 1.56

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

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

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface javax.swing.event.UndoableEditListener
intf java.util.EventListener
meth public abstract void undoableEditHappened(javax.swing.event.UndoableEditEvent)

CLSS public abstract org.netbeans.modules.xml.axi.AXIComponent
cons public init(org.netbeans.modules.xml.axi.AXIModel)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.axi.AXIComponent)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.schema.model.SchemaComponent)
fld protected org.netbeans.modules.xml.axi.AXIComponent sharedComponent
innr public final static !enum ComponentType
intf java.beans.PropertyChangeListener
intf java.lang.Cloneable
meth protected boolean isInModel()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void appendChild(java.lang.String,org.netbeans.modules.xml.axi.AXIComponent)
meth protected void appendChildQuietly(org.netbeans.modules.xml.axi.AXIComponent,java.util.List<org.netbeans.modules.xml.axi.AXIComponent>)
meth protected void firePropertyChangeEvent(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void insertAtIndexQuietly(org.netbeans.modules.xml.axi.AXIComponent,java.util.List<org.netbeans.modules.xml.axi.AXIComponent>,int)
meth protected void removeChildQuietly(org.netbeans.modules.xml.axi.AXIComponent,java.util.List<org.netbeans.modules.xml.axi.AXIComponent>)
meth protected void setSharedComponent(org.netbeans.modules.xml.axi.AXIComponent)
meth public abstract void accept(org.netbeans.modules.xml.axi.visitor.AXIVisitor)
meth public boolean canVisitChildren()
meth public boolean isGlobal()
meth public boolean isReadOnly()
meth public boolean isShared()
meth public boolean supportsCardinality()
meth public final org.netbeans.modules.xml.schema.model.SchemaComponent getPeer()
meth public final void addChildAtIndex(org.netbeans.modules.xml.axi.AXIComponent,int)
meth public final void appendChild(org.netbeans.modules.xml.axi.AXIComponent)
meth public final void removeChild(org.netbeans.modules.xml.axi.AXIComponent)
meth public final void setPeer(org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public int getIndex()
meth public int getIndex(boolean)
meth public java.lang.String getDocumentation()
meth public java.lang.String getTargetNamespace()
meth public java.util.List<org.netbeans.modules.xml.axi.AXIComponent> getRefSet()
meth public java.util.List<org.netbeans.modules.xml.axi.AbstractElement> getChildElements()
meth public org.netbeans.modules.xml.axi.AXIComponent copy(org.netbeans.modules.xml.axi.AXIComponent)
meth public org.netbeans.modules.xml.axi.AXIComponent getOriginal()
meth public org.netbeans.modules.xml.axi.AXIComponent getSharedComponent()
meth public org.netbeans.modules.xml.axi.AXIComponent$ComponentType getComponentType()
meth public org.netbeans.modules.xml.axi.AXIModel getModel()
meth public org.netbeans.modules.xml.axi.ContentModel getContentModel()
meth public org.netbeans.modules.xml.axi.Element getParentElement()
meth public void addListener(org.netbeans.modules.xml.axi.AXIComponent)
meth public void insertAtIndex(java.lang.String,org.netbeans.modules.xml.axi.AXIComponent,int)
meth public void populateChildren(java.util.List<org.netbeans.modules.xml.axi.AXIComponent>)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removeAllChildren()
meth public void removeChild(java.lang.String,org.netbeans.modules.xml.axi.AXIComponent)
meth public void removeListener(org.netbeans.modules.xml.axi.AXIComponent)
supr org.netbeans.modules.xml.xam.AbstractComponent<org.netbeans.modules.xml.axi.AXIComponent>
hfds PROP_CHILD_ADDED,PROP_CHILD_REMOVED,listenerMap,pcs,peer

CLSS public final static !enum org.netbeans.modules.xml.axi.AXIComponent$ComponentType
 outer org.netbeans.modules.xml.axi.AXIComponent
fld public final static org.netbeans.modules.xml.axi.AXIComponent$ComponentType LOCAL
fld public final static org.netbeans.modules.xml.axi.AXIComponent$ComponentType PROXY
fld public final static org.netbeans.modules.xml.axi.AXIComponent$ComponentType REFERENCE
fld public final static org.netbeans.modules.xml.axi.AXIComponent$ComponentType SHARED
meth public static org.netbeans.modules.xml.axi.AXIComponent$ComponentType valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.axi.AXIComponent$ComponentType[] values()
supr java.lang.Enum<org.netbeans.modules.xml.axi.AXIComponent$ComponentType>

CLSS public org.netbeans.modules.xml.axi.AXIComponentFactory
meth public java.lang.String toString()
meth public long getComponentCount()
meth public org.netbeans.modules.xml.axi.AXIComponent copy(org.netbeans.modules.xml.axi.AXIComponent)
meth public org.netbeans.modules.xml.axi.AXIComponent createProxy(org.netbeans.modules.xml.axi.AXIComponent)
meth public org.netbeans.modules.xml.axi.AnyAttribute createAnyAttribute()
meth public org.netbeans.modules.xml.axi.AnyAttribute createAnyAttribute(org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public org.netbeans.modules.xml.axi.AnyElement createAnyElement()
meth public org.netbeans.modules.xml.axi.AnyElement createAnyElement(org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public org.netbeans.modules.xml.axi.Attribute createAttribute()
meth public org.netbeans.modules.xml.axi.Attribute createAttribute(org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public org.netbeans.modules.xml.axi.Attribute createAttributeReference(org.netbeans.modules.xml.axi.Attribute)
meth public org.netbeans.modules.xml.axi.Attribute createAttributeReference(org.netbeans.modules.xml.schema.model.SchemaComponent,org.netbeans.modules.xml.axi.Attribute)
meth public org.netbeans.modules.xml.axi.Compositor createAll()
meth public org.netbeans.modules.xml.axi.Compositor createAll(org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public org.netbeans.modules.xml.axi.Compositor createChoice()
meth public org.netbeans.modules.xml.axi.Compositor createChoice(org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public org.netbeans.modules.xml.axi.Compositor createCompositor(org.netbeans.modules.xml.axi.Compositor$CompositorType)
meth public org.netbeans.modules.xml.axi.Compositor createSequence()
meth public org.netbeans.modules.xml.axi.Compositor createSequence(org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public org.netbeans.modules.xml.axi.ContentModel createAttributeGroup()
meth public org.netbeans.modules.xml.axi.ContentModel createComplexType()
meth public org.netbeans.modules.xml.axi.ContentModel createContentModel(org.netbeans.modules.xml.axi.ContentModel$ContentModelType)
meth public org.netbeans.modules.xml.axi.ContentModel createContentModel(org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public org.netbeans.modules.xml.axi.ContentModel createGroup()
meth public org.netbeans.modules.xml.axi.Element createElement()
meth public org.netbeans.modules.xml.axi.Element createElement(org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public org.netbeans.modules.xml.axi.Element createElementReference(org.netbeans.modules.xml.axi.Element)
meth public org.netbeans.modules.xml.axi.Element createElementReference(org.netbeans.modules.xml.schema.model.SchemaComponent,org.netbeans.modules.xml.axi.Element)
meth public org.netbeans.modules.xml.axi.SchemaReference createImport()
meth public org.netbeans.modules.xml.axi.SchemaReference createInclude()
meth public org.netbeans.modules.xml.axi.SchemaReference createSchemaReference(org.netbeans.modules.xml.schema.model.SchemaComponent)
supr java.lang.Object
hfds attributeCount,compositorCount,contentModelCount,elementCount,model,proxyComponentCount
hcls AXICopier,ProxyComponentFactory

CLSS public abstract org.netbeans.modules.xml.axi.AXIContainer
cons public init(org.netbeans.modules.xml.axi.AXIModel)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.axi.AXIComponent)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.schema.model.SchemaComponent)
fld protected java.lang.String name
fld public final static java.lang.String PROP_NAME = "name"
meth public final java.util.List<org.netbeans.modules.xml.axi.AbstractAttribute> getAttributes()
meth public java.lang.String getName()
meth public org.netbeans.modules.xml.axi.Compositor getCompositor()
meth public void addAttribute(org.netbeans.modules.xml.axi.AbstractAttribute)
meth public void addCompositor(org.netbeans.modules.xml.axi.Compositor)
meth public void addElement(org.netbeans.modules.xml.axi.AbstractElement)
meth public void removeAttribute(org.netbeans.modules.xml.axi.AbstractAttribute)
meth public void removeCompositor(org.netbeans.modules.xml.axi.Compositor)
meth public void removeElement(org.netbeans.modules.xml.axi.AbstractElement)
meth public void setName(java.lang.String)
supr org.netbeans.modules.xml.axi.AXIComponent

CLSS public abstract org.netbeans.modules.xml.axi.AXIDocument
cons public init(org.netbeans.modules.xml.axi.AXIModel)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.schema.model.SchemaComponent)
fld public final static java.lang.String PROP_ATTRIBUTE_FORM_DEFAULT = "attributeFormDefault"
fld public final static java.lang.String PROP_ELEMENT_FORM_DEFAULT = "elementFormDefault"
fld public final static java.lang.String PROP_LANGUAGE = "language"
fld public final static java.lang.String PROP_SCHEMA_DESIGN_PATTERN = "schemaDesignPattern"
fld public final static java.lang.String PROP_TARGET_NAMESPACE = "targetNamespace"
fld public final static java.lang.String PROP_VERSION = "version"
meth public java.lang.String getLanguage()
meth public java.lang.String getTargetNamespace()
meth public java.lang.String getVersion()
meth public java.util.List<org.netbeans.modules.xml.axi.Attribute> getAttributes()
meth public java.util.List<org.netbeans.modules.xml.axi.ContentModel> getContentModels()
meth public java.util.List<org.netbeans.modules.xml.axi.Element> getElements()
meth public org.netbeans.modules.xml.axi.SchemaGenerator$Pattern getSchemaDesignPattern()
meth public org.netbeans.modules.xml.schema.model.Form getAttributeFormDefault()
meth public org.netbeans.modules.xml.schema.model.Form getElementFormDefault()
meth public void accept(org.netbeans.modules.xml.axi.visitor.AXIVisitor)
meth public void addContentModel(org.netbeans.modules.xml.axi.ContentModel)
meth public void addElement(org.netbeans.modules.xml.axi.Element)
meth public void removeContentModel(org.netbeans.modules.xml.axi.ContentModel)
meth public void removeElement(org.netbeans.modules.xml.axi.Element)
meth public void setAttributeFormDefault(org.netbeans.modules.xml.schema.model.Form)
meth public void setElementFormDefault(org.netbeans.modules.xml.schema.model.Form)
meth public void setLanguage(java.lang.String)
meth public void setSchemaDesignPattern(org.netbeans.modules.xml.axi.SchemaGenerator$Pattern)
meth public void setTargetNamespace(java.lang.String)
meth public void setVersion(java.lang.String)
supr org.netbeans.modules.xml.axi.AXIComponent
hfds attributeFormDefault,elementFormDefault,language,namespace,version

CLSS public abstract org.netbeans.modules.xml.axi.AXIModel
cons public init(org.netbeans.modules.xml.xam.ModelSource)
meth public abstract java.util.List<org.netbeans.modules.xml.axi.AXIModel> getReferencedModels()
meth public abstract org.netbeans.modules.xml.axi.SchemaGenerator$Pattern getSchemaDesignPattern()
meth public abstract void setSchemaDesignPattern(org.netbeans.modules.xml.axi.SchemaGenerator$Pattern)
meth public boolean canView(org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public boolean isReadOnly()
meth public org.netbeans.modules.xml.axi.AXIComponentFactory getComponentFactory()
meth public org.netbeans.modules.xml.axi.AXIDocument getRoot()
meth public org.netbeans.modules.xml.schema.model.SchemaModel getSchemaModel()
meth public org.netbeans.modules.xml.xam.ModelAccess getAccess()
meth public void addChildComponent(org.netbeans.modules.xml.xam.Component,org.netbeans.modules.xml.xam.Component,int)
meth public void removeChildComponent(org.netbeans.modules.xml.xam.Component)
supr org.netbeans.modules.xml.xam.AbstractModel<org.netbeans.modules.xml.axi.AXIComponent>
hfds factory,modelAccess,root

CLSS public org.netbeans.modules.xml.axi.AXIModelFactory
meth protected java.lang.Object getKey(org.netbeans.modules.xml.xam.ModelSource)
meth protected org.netbeans.modules.xml.axi.AXIModel createModel(org.netbeans.modules.xml.xam.ModelSource)
meth protected org.netbeans.modules.xml.axi.AXIModel getModel(org.netbeans.modules.xml.xam.ModelSource)
meth public org.netbeans.modules.xml.axi.AXIModel getModel(org.netbeans.modules.xml.schema.model.SchemaModel)
meth public static org.netbeans.modules.xml.axi.AXIModelFactory getDefault()
supr org.netbeans.modules.xml.xam.AbstractModelFactory<org.netbeans.modules.xml.axi.AXIModel>
hfds instance

CLSS public abstract interface org.netbeans.modules.xml.axi.AXIType
meth public abstract java.lang.String getName()
meth public abstract void accept(org.netbeans.modules.xml.axi.visitor.AXIVisitor)

CLSS public abstract org.netbeans.modules.xml.axi.AbstractAttribute
cons public init(org.netbeans.modules.xml.axi.AXIModel)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.axi.AXIComponent)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.schema.model.SchemaComponent)
fld public final static java.lang.String PROP_ATTRIBUTE = "attribute"
meth public abstract java.lang.String getName()
meth public abstract void accept(org.netbeans.modules.xml.axi.visitor.AXIVisitor)
supr org.netbeans.modules.xml.axi.AXIComponent

CLSS public abstract org.netbeans.modules.xml.axi.AbstractElement
cons public init(org.netbeans.modules.xml.axi.AXIModel)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.axi.AXIComponent)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.schema.model.SchemaComponent)
fld protected java.lang.String maxOccurs
fld protected java.lang.String minOccurs
fld public final static java.lang.String PROP_ELEMENT = "element"
fld public final static java.lang.String PROP_MAXOCCURS = "maxOccurs"
fld public final static java.lang.String PROP_MINOCCURS = "minOccurs"
meth public abstract void accept(org.netbeans.modules.xml.axi.visitor.AXIVisitor)
meth public boolean allowsFullMultiplicity()
meth public java.lang.String getMaxOccurs()
meth public java.lang.String getMinOccurs()
meth public void setMaxOccurs(java.lang.String)
meth public void setMinOccurs(java.lang.String)
supr org.netbeans.modules.xml.axi.AXIContainer

CLSS public org.netbeans.modules.xml.axi.AnyAttribute
cons public init(org.netbeans.modules.xml.axi.AXIModel)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.axi.AXIComponent)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.schema.model.SchemaComponent)
fld public final static java.lang.String PROP_NAMESPACE = "namespace"
fld public final static java.lang.String PROP_PROCESSCONTENTS = "processContents"
meth public java.lang.String getName()
meth public java.lang.String getTargetNamespace()
meth public java.lang.String toString()
meth public org.netbeans.modules.xml.schema.model.Any$ProcessContents getProcessContents()
meth public void accept(org.netbeans.modules.xml.axi.visitor.AXIVisitor)
meth public void setProcessContents(org.netbeans.modules.xml.schema.model.Any$ProcessContents)
meth public void setTargetNamespace(java.lang.String)
supr org.netbeans.modules.xml.axi.AbstractAttribute
hfds namespace,processContents

CLSS public org.netbeans.modules.xml.axi.AnyElement
cons public init(org.netbeans.modules.xml.axi.AXIModel)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.axi.AXIComponent)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.schema.model.SchemaComponent)
fld public final static java.lang.String PROP_NAMESPACE = "namespace"
fld public final static java.lang.String PROP_PROCESSCONTENTS = "processContents"
meth public java.lang.String getName()
meth public java.lang.String getTargetNamespace()
meth public java.lang.String toString()
meth public org.netbeans.modules.xml.schema.model.Any$ProcessContents getProcessContents()
meth public void accept(org.netbeans.modules.xml.axi.visitor.AXIVisitor)
meth public void setProcessContents(org.netbeans.modules.xml.schema.model.Any$ProcessContents)
meth public void setTargetNamespace(java.lang.String)
supr org.netbeans.modules.xml.axi.AbstractElement
hfds namespace,processContents

CLSS public abstract org.netbeans.modules.xml.axi.Attribute
cons public init(org.netbeans.modules.xml.axi.AXIModel)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.axi.AXIComponent)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.schema.model.SchemaComponent)
fld protected java.lang.String defaultValue
fld protected java.lang.String fixedValue
fld protected java.lang.String name
fld protected org.netbeans.modules.xml.axi.AXIType datatype
fld protected org.netbeans.modules.xml.schema.model.Attribute$Use use
fld protected org.netbeans.modules.xml.schema.model.Form form
fld public final static java.lang.String PROP_ATTRIBUTE_REF = "attributeRef"
fld public final static java.lang.String PROP_DEFAULT = "default"
fld public final static java.lang.String PROP_FIXED = "fixed"
fld public final static java.lang.String PROP_FORM = "form"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_TYPE = "type"
fld public final static java.lang.String PROP_USE = "use"
intf org.netbeans.modules.xml.axi.AXIType
meth public abstract boolean isReference()
meth public abstract java.lang.String getDefault()
meth public abstract java.lang.String getFixed()
meth public abstract org.netbeans.modules.xml.axi.AXIType getType()
meth public abstract org.netbeans.modules.xml.axi.Attribute getReferent()
meth public abstract org.netbeans.modules.xml.schema.model.Attribute$Use getUse()
meth public abstract org.netbeans.modules.xml.schema.model.Form getForm()
meth public abstract void setDefault(java.lang.String)
meth public abstract void setFixed(java.lang.String)
meth public abstract void setForm(org.netbeans.modules.xml.schema.model.Form)
meth public abstract void setName(java.lang.String)
meth public abstract void setType(org.netbeans.modules.xml.axi.AXIType)
meth public abstract void setUse(org.netbeans.modules.xml.schema.model.Attribute$Use)
meth public java.lang.String toString()
meth public void accept(org.netbeans.modules.xml.axi.visitor.AXIVisitor)
supr org.netbeans.modules.xml.axi.AbstractAttribute

CLSS public org.netbeans.modules.xml.axi.Compositor
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.axi.AXIComponent)
fld public final static java.lang.String PROP_COMPOSITOR = "compositor"
fld public final static java.lang.String PROP_MAXOCCURS = "maxOccurs"
fld public final static java.lang.String PROP_MINOCCURS = "minOccurs"
fld public final static java.lang.String PROP_TYPE = "type"
innr public final static !enum CompositorType
meth public boolean allowsFullMultiplicity()
meth public java.lang.String getMaxOccurs()
meth public java.lang.String getMinOccurs()
meth public java.lang.String toString()
meth public org.netbeans.modules.xml.axi.Compositor$CompositorType getType()
meth public void accept(org.netbeans.modules.xml.axi.visitor.AXIVisitor)
meth public void addCompositor(org.netbeans.modules.xml.axi.Compositor)
meth public void addElement(org.netbeans.modules.xml.axi.Element)
meth public void removeCompositor(org.netbeans.modules.xml.axi.Compositor)
meth public void removeElement(org.netbeans.modules.xml.axi.Element)
meth public void setMaxOccurs(java.lang.String)
meth public void setMinOccurs(java.lang.String)
meth public void setType(org.netbeans.modules.xml.axi.Compositor$CompositorType)
supr org.netbeans.modules.xml.axi.AXIComponent
hfds maxOccurs,minOccurs,type

CLSS public final static !enum org.netbeans.modules.xml.axi.Compositor$CompositorType
 outer org.netbeans.modules.xml.axi.Compositor
fld public final static org.netbeans.modules.xml.axi.Compositor$CompositorType ALL
fld public final static org.netbeans.modules.xml.axi.Compositor$CompositorType CHOICE
fld public final static org.netbeans.modules.xml.axi.Compositor$CompositorType SEQUENCE
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.axi.Compositor$CompositorType valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.axi.Compositor$CompositorType[] values()
supr java.lang.Enum<org.netbeans.modules.xml.axi.Compositor$CompositorType>

CLSS public org.netbeans.modules.xml.axi.ContentModel
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.axi.ContentModel$ContentModelType)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.schema.model.SchemaComponent)
fld public final static java.lang.String PROP_CONTENT_MODEL = "contentModel"
innr public final static !enum ContentModelType
intf org.netbeans.modules.xml.axi.AXIType
meth public java.lang.String toString()
meth public org.netbeans.modules.xml.axi.AXIComponent$ComponentType getComponentType()
meth public org.netbeans.modules.xml.axi.ContentModel$ContentModelType getType()
meth public void accept(org.netbeans.modules.xml.axi.visitor.AXIVisitor)
supr org.netbeans.modules.xml.axi.AXIContainer
hfds type

CLSS public final static !enum org.netbeans.modules.xml.axi.ContentModel$ContentModelType
 outer org.netbeans.modules.xml.axi.ContentModel
fld public final static org.netbeans.modules.xml.axi.ContentModel$ContentModelType ATTRIBUTE_GROUP
fld public final static org.netbeans.modules.xml.axi.ContentModel$ContentModelType COMPLEX_TYPE
fld public final static org.netbeans.modules.xml.axi.ContentModel$ContentModelType GROUP
meth public static org.netbeans.modules.xml.axi.ContentModel$ContentModelType valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.axi.ContentModel$ContentModelType[] values()
supr java.lang.Enum<org.netbeans.modules.xml.axi.ContentModel$ContentModelType>

CLSS public abstract org.netbeans.modules.xml.axi.Element
cons public init(org.netbeans.modules.xml.axi.AXIModel)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.axi.AXIComponent)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.schema.model.SchemaComponent)
fld protected boolean isAbstract
fld protected boolean isNillable
fld protected java.lang.String block
fld protected java.lang.String defaultValue
fld protected java.lang.String finalValue
fld protected java.lang.String fixedValue
fld protected org.netbeans.modules.xml.schema.model.Form form
fld public final static java.lang.String PROP_ABSTRACT = "abstract"
fld public final static java.lang.String PROP_BLOCK = "block"
fld public final static java.lang.String PROP_DEFAULT = "default"
fld public final static java.lang.String PROP_ELEMENT_REF = "elementRef"
fld public final static java.lang.String PROP_FINAL = "final"
fld public final static java.lang.String PROP_FIXED = "fixed"
fld public final static java.lang.String PROP_FORM = "form"
fld public final static java.lang.String PROP_NILLABLE = "nillable"
fld public final static java.lang.String PROP_TYPE = "type"
intf org.netbeans.modules.xml.axi.AXIType
meth public abstract boolean getAbstract()
meth public abstract boolean getNillable()
meth public abstract boolean isReference()
meth public abstract java.lang.String getBlock()
meth public abstract java.lang.String getDefault()
meth public abstract java.lang.String getFinal()
meth public abstract java.lang.String getFixed()
meth public abstract org.netbeans.modules.xml.axi.AXIType getType()
meth public abstract org.netbeans.modules.xml.axi.Element getReferent()
meth public abstract org.netbeans.modules.xml.schema.model.Form getForm()
meth public abstract void setAbstract(boolean)
meth public abstract void setBlock(java.lang.String)
meth public abstract void setDefault(java.lang.String)
meth public abstract void setFinal(java.lang.String)
meth public abstract void setFixed(java.lang.String)
meth public abstract void setForm(org.netbeans.modules.xml.schema.model.Form)
meth public abstract void setNillable(boolean)
meth public abstract void setType(org.netbeans.modules.xml.axi.AXIType)
meth public java.lang.Boolean isNillable()
meth public java.lang.String toString()
meth public void accept(org.netbeans.modules.xml.axi.visitor.AXIVisitor)
meth public void setNillable(java.lang.Boolean)
supr org.netbeans.modules.xml.axi.AbstractElement

CLSS public abstract org.netbeans.modules.xml.axi.SchemaGenerator
cons public init(org.netbeans.modules.xml.axi.SchemaGenerator$Mode)
fld public static org.netbeans.modules.xml.axi.SchemaGenerator$Pattern DEFAULT_DESIGN_PATTERN
innr public abstract interface static PrimitiveCart
innr public abstract interface static UniqueId
innr public final static !enum Mode
innr public final static !enum Pattern
meth protected void visitChildren(org.netbeans.modules.xml.axi.AXIComponent)
meth public abstract void transformSchema(org.netbeans.modules.xml.schema.model.SchemaModel) throws java.io.IOException
meth public abstract void updateSchema(org.netbeans.modules.xml.schema.model.SchemaModel) throws java.io.IOException,javax.swing.text.BadLocationException
meth public org.netbeans.modules.xml.axi.SchemaGenerator$Mode getMode()
meth public void visit(org.netbeans.modules.xml.axi.Attribute)
meth public void visit(org.netbeans.modules.xml.axi.Compositor)
meth public void visit(org.netbeans.modules.xml.axi.Element)
supr org.netbeans.modules.xml.axi.visitor.DefaultVisitor
hfds mode

CLSS public final static !enum org.netbeans.modules.xml.axi.SchemaGenerator$Mode
 outer org.netbeans.modules.xml.axi.SchemaGenerator
fld public final static org.netbeans.modules.xml.axi.SchemaGenerator$Mode TRANSFORM
fld public final static org.netbeans.modules.xml.axi.SchemaGenerator$Mode UPDATE
meth public static org.netbeans.modules.xml.axi.SchemaGenerator$Mode valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.axi.SchemaGenerator$Mode[] values()
supr java.lang.Enum<org.netbeans.modules.xml.axi.SchemaGenerator$Mode>

CLSS public final static !enum org.netbeans.modules.xml.axi.SchemaGenerator$Pattern
 outer org.netbeans.modules.xml.axi.SchemaGenerator
fld public final static org.netbeans.modules.xml.axi.SchemaGenerator$Pattern GARDEN_OF_EDEN
fld public final static org.netbeans.modules.xml.axi.SchemaGenerator$Pattern MIXED
fld public final static org.netbeans.modules.xml.axi.SchemaGenerator$Pattern RUSSIAN_DOLL
fld public final static org.netbeans.modules.xml.axi.SchemaGenerator$Pattern SALAMI_SLICE
fld public final static org.netbeans.modules.xml.axi.SchemaGenerator$Pattern VENITIAN_BLIND
meth public static org.netbeans.modules.xml.axi.SchemaGenerator$Pattern valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.axi.SchemaGenerator$Pattern[] values()
supr java.lang.Enum<org.netbeans.modules.xml.axi.SchemaGenerator$Pattern>

CLSS public abstract interface static org.netbeans.modules.xml.axi.SchemaGenerator$PrimitiveCart
 outer org.netbeans.modules.xml.axi.SchemaGenerator
meth public abstract java.util.Set<java.util.Map$Entry<org.netbeans.modules.xml.schema.model.SchemaComponent,org.netbeans.modules.xml.axi.datatype.Datatype>> getEntries()
meth public abstract org.netbeans.modules.xml.schema.model.GlobalSimpleType getDefaultPrimitive()
meth public abstract org.netbeans.modules.xml.schema.model.GlobalSimpleType getPrimitiveType(java.lang.String)
meth public abstract void add(org.netbeans.modules.xml.axi.datatype.Datatype,org.netbeans.modules.xml.schema.model.SchemaComponent)

CLSS public abstract interface static org.netbeans.modules.xml.axi.SchemaGenerator$UniqueId
 outer org.netbeans.modules.xml.axi.SchemaGenerator
meth public abstract int nextId()

CLSS public abstract org.netbeans.modules.xml.axi.SchemaGeneratorFactory
cons public init()
innr public final static !enum TransformHint
meth public abstract java.util.List<org.netbeans.modules.xml.axi.Element> findMasterGlobalElements(org.netbeans.modules.xml.axi.AXIModel)
meth public abstract org.netbeans.modules.xml.axi.SchemaGenerator$Pattern inferDesignPattern(org.netbeans.modules.xml.axi.AXIModel)
meth public abstract org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint canTransformSchema(org.netbeans.modules.xml.schema.model.SchemaModel,org.netbeans.modules.xml.axi.SchemaGenerator$Pattern,org.netbeans.modules.xml.axi.SchemaGenerator$Pattern)
meth public abstract org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint canTransformSchema(org.netbeans.modules.xml.schema.model.SchemaModel,org.netbeans.modules.xml.axi.SchemaGenerator$Pattern,org.netbeans.modules.xml.axi.SchemaGenerator$Pattern,java.util.List<org.netbeans.modules.xml.axi.Element>)
meth public abstract void transformSchema(org.netbeans.modules.xml.schema.model.SchemaModel,org.netbeans.modules.xml.axi.SchemaGenerator$Pattern) throws java.io.IOException
meth public abstract void updateSchema(org.netbeans.modules.xml.schema.model.SchemaModel,org.netbeans.modules.xml.axi.SchemaGenerator$Pattern) throws java.io.IOException,javax.swing.text.BadLocationException
meth public static org.netbeans.modules.xml.axi.SchemaGeneratorFactory getDefault()
supr java.lang.Object
hfds instance

CLSS public final static !enum org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint
 outer org.netbeans.modules.xml.axi.SchemaGeneratorFactory
fld public final static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint CANNOT_REMOVE_GLOBAL_ELEMENTS
fld public final static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint CANNOT_REMOVE_GLOBAL_ELEMENTS_AND_TYPES
fld public final static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint CANNOT_REMOVE_TYPES
fld public final static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint GLOBAL_ELEMENTS_HAVE_NO_CHILD_ATTRIBUTES
fld public final static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint GLOBAL_ELEMENTS_HAVE_NO_CHILD_ELEMENTS
fld public final static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint GLOBAL_ELEMENTS_HAVE_NO_CHILD_ELEMENTS_AND_ATTRIBUTES
fld public final static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint GLOBAL_ELEMENTS_HAVE_NO_GRAND_CHILDREN
fld public final static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint INVALID_SCHEMA
fld public final static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint NO_ATTRIBUTES
fld public final static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint NO_GLOBAL_ELEMENTS
fld public final static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint OK
fld public final static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint SAME_DESIGN_PATTERN
fld public final static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint WILL_REMOVE_GLOBAL_ELEMENTS
fld public final static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint WILL_REMOVE_GLOBAL_ELEMENTS_AND_TYPES
fld public final static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint WILL_REMOVE_TYPES
meth public static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint[] values()
supr java.lang.Enum<org.netbeans.modules.xml.axi.SchemaGeneratorFactory$TransformHint>

CLSS public org.netbeans.modules.xml.axi.SchemaReference
cons public init(org.netbeans.modules.xml.axi.AXIModel,boolean)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.axi.AXIComponent)
cons public init(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.schema.model.SchemaModelReference)
fld public final static java.lang.String PROP_SCHEMA_LOCATION = "schemaLocation"
fld public final static java.lang.String PROP_TARGET_NAMESPACE = "targetNamespace"
meth public boolean isImport()
meth public boolean isInclude()
meth public java.lang.String getSchemaLocation()
meth public java.lang.String getTargetNamespace()
meth public void accept(org.netbeans.modules.xml.axi.visitor.AXIVisitor)
meth public void setSchemaLocation(java.lang.String)
meth public void setTargetNamespace(java.lang.String)
supr org.netbeans.modules.xml.axi.AXIComponent
hfds include,schemaLocation,targetNamespace

CLSS public org.netbeans.modules.xml.axi.datatype.AnyType
cons public init()
supr org.netbeans.modules.xml.axi.datatype.StringBase

CLSS public org.netbeans.modules.xml.axi.datatype.AnyURIType
cons public init()
supr org.netbeans.modules.xml.axi.datatype.StringBase

CLSS public org.netbeans.modules.xml.axi.datatype.Base64BinaryType
cons public init()
supr org.netbeans.modules.xml.axi.datatype.BinaryBase

CLSS public abstract org.netbeans.modules.xml.axi.datatype.BinaryBase
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
fld protected boolean hasFacets
fld protected java.util.List<java.lang.String> enumerations
meth public boolean hasFacets()
meth public boolean isList()
meth public java.util.List<java.lang.Integer> getLengths()
meth public java.util.List<java.lang.Integer> getMaxLengths()
meth public java.util.List<java.lang.Integer> getMinLengths()
meth public java.util.List<java.lang.String> getEnumerations()
meth public java.util.List<java.lang.String> getPatterns()
meth public java.util.List<org.netbeans.modules.xml.axi.datatype.Datatype$Facet> getApplicableFacets()
meth public java.util.List<org.netbeans.modules.xml.schema.model.Whitespace$Treatment> getWhiteSpaces()
meth public org.netbeans.modules.xml.axi.datatype.Datatype$Kind getKind()
meth public void addEnumeration(java.lang.String)
meth public void addLength(int)
meth public void addMaxLength(int)
meth public void addMinLength(int)
meth public void addPattern(java.lang.String)
meth public void addWhitespace(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)
meth public void removeEnumeration(java.lang.String)
meth public void removeLength(java.lang.Number)
meth public void removeMaxLength(java.lang.Number)
meth public void removeMinLength(java.lang.Number)
meth public void removePattern(java.lang.String)
meth public void removeWhitespace(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)
meth public void setIsList(boolean)
supr org.netbeans.modules.xml.axi.datatype.Datatype
hfds applicableFacets,isList,kind,lengths,maxLengths,minLengths,patterns,whitespaces

CLSS public org.netbeans.modules.xml.axi.datatype.BooleanType
cons public init()
fld protected boolean hasFacets
meth public boolean hasFacets()
meth public boolean isList()
meth public java.util.List<java.lang.String> getPatterns()
meth public java.util.List<org.netbeans.modules.xml.axi.datatype.Datatype$Facet> getApplicableFacets()
meth public java.util.List<org.netbeans.modules.xml.schema.model.Whitespace$Treatment> getWhiteSpaces()
meth public org.netbeans.modules.xml.axi.datatype.Datatype$Kind getKind()
meth public void addPattern(java.lang.String)
meth public void addWhitespace(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)
meth public void setIsList(boolean)
supr org.netbeans.modules.xml.axi.datatype.Datatype
hfds applicableFacets,isList,kind,patterns,whitespaces

CLSS public org.netbeans.modules.xml.axi.datatype.ByteType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.ShortType

CLSS public org.netbeans.modules.xml.axi.datatype.CustomDatatype
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.modules.xml.axi.datatype.Datatype)
meth public boolean hasFacets()
meth public boolean isList()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.List<? extends java.lang.Number> getFractionDigits()
meth public java.util.List<? extends java.lang.Number> getLengths()
meth public java.util.List<? extends java.lang.Number> getMaxLengths()
meth public java.util.List<? extends java.lang.Number> getMinLengths()
meth public java.util.List<? extends java.lang.Number> getTotalDigits()
meth public java.util.List<? extends java.lang.String> getPatterns()
meth public java.util.List<?> getEnumerations()
meth public java.util.List<?> getMaxExclusives()
meth public java.util.List<?> getMaxInclusives()
meth public java.util.List<?> getMinExclusives()
meth public java.util.List<?> getMinInclusives()
meth public java.util.List<org.netbeans.modules.xml.axi.datatype.Datatype$Facet> getApplicableFacets()
meth public java.util.List<org.netbeans.modules.xml.schema.model.Whitespace$Treatment> getWhiteSpaces()
meth public org.netbeans.modules.xml.axi.datatype.Datatype getBase()
meth public org.netbeans.modules.xml.axi.datatype.Datatype$Kind getKind()
meth public void addEnumeration(java.lang.Object)
meth public void addFractionDigits(int)
meth public void addLength(int)
meth public void addMaxExclusive(java.lang.Object)
meth public void addMaxInclusive(java.lang.Object)
meth public void addMaxLength(int)
meth public void addMinExclusive(java.lang.Object)
meth public void addMinInclusive(java.lang.Object)
meth public void addMinLength(int)
meth public void addPattern(java.lang.String)
meth public void addTotalDigits(int)
meth public void addWhitespace(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)
meth public void setBase(org.netbeans.modules.xml.axi.datatype.Datatype)
meth public void setIsList(boolean)
meth public void setName(java.lang.String)
supr org.netbeans.modules.xml.axi.datatype.Datatype
hfds base,name

CLSS public abstract org.netbeans.modules.xml.axi.datatype.Datatype
cons public init()
innr public final static !enum Facet
innr public final static !enum Kind
intf org.netbeans.modules.xml.axi.AXIType
meth public abstract boolean hasFacets()
meth public abstract boolean isList()
meth public abstract java.util.List<org.netbeans.modules.xml.axi.datatype.Datatype$Facet> getApplicableFacets()
meth public abstract org.netbeans.modules.xml.axi.datatype.Datatype$Kind getKind()
meth public abstract void setIsList(boolean)
meth public boolean isPrimitive()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.List<? extends java.lang.Number> getFractionDigits()
meth public java.util.List<? extends java.lang.Number> getLengths()
meth public java.util.List<? extends java.lang.Number> getMaxLengths()
meth public java.util.List<? extends java.lang.Number> getMinLengths()
meth public java.util.List<? extends java.lang.Number> getTotalDigits()
meth public java.util.List<? extends java.lang.String> getPatterns()
meth public java.util.List<?> getEnumerations()
meth public java.util.List<?> getMaxExclusives()
meth public java.util.List<?> getMaxInclusives()
meth public java.util.List<?> getMinExclusives()
meth public java.util.List<?> getMinInclusives()
meth public java.util.List<org.netbeans.modules.xml.schema.model.Whitespace$Treatment> getWhiteSpaces()
meth public void accept(org.netbeans.modules.xml.axi.visitor.AXIVisitor)
meth public void addEnumeration(java.lang.Object)
meth public void addFractionDigits(int)
meth public void addLength(int)
meth public void addMaxExclusive(java.lang.Object)
meth public void addMaxInclusive(java.lang.Object)
meth public void addMaxLength(int)
meth public void addMinExclusive(java.lang.Object)
meth public void addMinInclusive(java.lang.Object)
meth public void addMinLength(int)
meth public void addPattern(java.lang.String)
meth public void addTotalDigits(int)
meth public void addWhitespace(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)
meth public void removeEnumeration(java.lang.Object)
meth public void removeFractionDigits(java.lang.Number)
meth public void removeLength(java.lang.Number)
meth public void removeMaxExclusive(java.lang.Object)
meth public void removeMaxInclusive(java.lang.Object)
meth public void removeMaxLength(java.lang.Number)
meth public void removeMinExclusive(java.lang.Object)
meth public void removeMinInclusive(java.lang.Object)
meth public void removeMinLength(java.lang.Number)
meth public void removePattern(java.lang.String)
meth public void removeTotalDigits(java.lang.Number)
meth public void removeWhitespace(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)
supr java.lang.Object
hfds enumerations

CLSS public final static !enum org.netbeans.modules.xml.axi.datatype.Datatype$Facet
 outer org.netbeans.modules.xml.axi.datatype.Datatype
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Facet ENUMERATION
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Facet FRACTIONDIGITS
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Facet LENGTH
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Facet MAXEXCLUSIVE
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Facet MAXINCLUSIVE
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Facet MAXLENGTH
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Facet MINEXCLUSIVE
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Facet MININCLUSIVE
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Facet MINLENGTH
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Facet PATTERN
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Facet TOTATDIGITS
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Facet WHITESPACE
meth public java.lang.Class<? extends org.netbeans.modules.xml.schema.model.SchemaComponent> getComponentType()
meth public java.lang.String getName()
meth public static org.netbeans.modules.xml.axi.datatype.Datatype$Facet valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.axi.datatype.Datatype$Facet[] values()
supr java.lang.Enum<org.netbeans.modules.xml.axi.datatype.Datatype$Facet>
hfds name,type

CLSS public final static !enum org.netbeans.modules.xml.axi.datatype.Datatype$Kind
 outer org.netbeans.modules.xml.axi.datatype.Datatype
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind ANYTYPE
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind ANYURI
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind BASE64_BINARY
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind BOOLEAN
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind BYTE
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind DATE
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind DATE_TIME
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind DECIMAL
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind DOUBLE
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind DURATION
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind ENTITIES
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind ENTITY
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind FLOAT
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind G_DAY
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind G_MONTH
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind G_MONTH_DAY
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind G_YEAR
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind G_YEAR_MONTH
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind HEX_BINARY
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind ID
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind IDREF
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind IDREFS
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind INT
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind INTEGER
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind LANGUAGE
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind LONG
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind NAME
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind NCNAME
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind NEGATIVE_INTEGER
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind NMTOKEN
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind NMTOKENS
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind NON_NEGATIVE_INTEGER
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind NON_POSITIVE_INTEGER
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind NORMALIZED_STRING
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind NOTATION
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind POSITIVE_INTEGER
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind QNAME
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind SHORT
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind STRING
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind TIME
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind TOKEN
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind UNION
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind UNSIGNED_BYTE
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind UNSIGNED_INT
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind UNSIGNED_LONG
fld public final static org.netbeans.modules.xml.axi.datatype.Datatype$Kind UNSIGNED_SHORT
meth public java.lang.String getName()
meth public static org.netbeans.modules.xml.axi.datatype.Datatype$Kind valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.axi.datatype.Datatype$Kind[] values()
supr java.lang.Enum<org.netbeans.modules.xml.axi.datatype.Datatype$Kind>
hfds name

CLSS public abstract org.netbeans.modules.xml.axi.datatype.DatatypeFactory
cons public init()
meth public abstract java.util.List<java.lang.Class<? extends org.netbeans.modules.xml.schema.model.SchemaComponent>> getApplicableSchemaFacets(org.netbeans.modules.xml.schema.model.SimpleType)
meth public abstract org.netbeans.modules.xml.axi.datatype.Datatype createPrimitive(java.lang.String)
meth public abstract org.netbeans.modules.xml.axi.datatype.Datatype getDatatype(org.netbeans.modules.xml.axi.AXIModel,org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public static org.netbeans.modules.xml.axi.datatype.DatatypeFactory getDefault()
supr java.lang.Object
hfds instance

CLSS public org.netbeans.modules.xml.axi.datatype.DateTimeType
cons public init()
supr org.netbeans.modules.xml.axi.datatype.TimeBase

CLSS public org.netbeans.modules.xml.axi.datatype.DateType
cons public init()
supr org.netbeans.modules.xml.axi.datatype.TimeBase

CLSS public org.netbeans.modules.xml.axi.datatype.DecimalType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.NumberBase

CLSS public org.netbeans.modules.xml.axi.datatype.DoubleType
cons public init()
supr org.netbeans.modules.xml.axi.datatype.NumberBase

CLSS public org.netbeans.modules.xml.axi.datatype.DurationType
cons public init()
supr org.netbeans.modules.xml.axi.datatype.TimeBase

CLSS public org.netbeans.modules.xml.axi.datatype.EntitiesType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.NameType

CLSS public org.netbeans.modules.xml.axi.datatype.EntityType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.NameType

CLSS public org.netbeans.modules.xml.axi.datatype.FloatType
cons public init()
supr org.netbeans.modules.xml.axi.datatype.NumberBase

CLSS public org.netbeans.modules.xml.axi.datatype.GDayType
cons public init()
supr org.netbeans.modules.xml.axi.datatype.TimeBase

CLSS public org.netbeans.modules.xml.axi.datatype.GMonthDayType
cons public init()
supr org.netbeans.modules.xml.axi.datatype.TimeBase

CLSS public org.netbeans.modules.xml.axi.datatype.GMonthType
cons public init()
supr org.netbeans.modules.xml.axi.datatype.TimeBase

CLSS public org.netbeans.modules.xml.axi.datatype.GYearMonthType
cons public init()
supr org.netbeans.modules.xml.axi.datatype.TimeBase

CLSS public org.netbeans.modules.xml.axi.datatype.GYearType
cons public init()
supr org.netbeans.modules.xml.axi.datatype.TimeBase

CLSS public org.netbeans.modules.xml.axi.datatype.HexBinaryType
cons public init()
supr org.netbeans.modules.xml.axi.datatype.BinaryBase

CLSS public org.netbeans.modules.xml.axi.datatype.IdRefType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.NameType

CLSS public org.netbeans.modules.xml.axi.datatype.IdRefsType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.NameType

CLSS public org.netbeans.modules.xml.axi.datatype.IdType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.NameType

CLSS public org.netbeans.modules.xml.axi.datatype.IntType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.LongType

CLSS public org.netbeans.modules.xml.axi.datatype.IntegerType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.DecimalType

CLSS public org.netbeans.modules.xml.axi.datatype.LanguageType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.TokenType

CLSS public org.netbeans.modules.xml.axi.datatype.LongType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.IntegerType

CLSS public org.netbeans.modules.xml.axi.datatype.NameType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.TokenType

CLSS public org.netbeans.modules.xml.axi.datatype.NcNameType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.NameType

CLSS public org.netbeans.modules.xml.axi.datatype.NegativeIntegerType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.IntegerType

CLSS public org.netbeans.modules.xml.axi.datatype.NmTokenType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.TokenType

CLSS public org.netbeans.modules.xml.axi.datatype.NmTokensType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.NmTokenType

CLSS public org.netbeans.modules.xml.axi.datatype.NonNegativeIntegerType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.IntegerType

CLSS public org.netbeans.modules.xml.axi.datatype.NonPositiveIntegerType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.IntegerType

CLSS public org.netbeans.modules.xml.axi.datatype.NormalizedStringType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.StringType

CLSS public org.netbeans.modules.xml.axi.datatype.NotationType
cons public init()
fld protected boolean hasFacets
meth public java.util.List<java.lang.Integer> getLengths()
meth public java.util.List<java.lang.Integer> getMaxLengths()
meth public java.util.List<java.lang.Integer> getMinLengths()
meth public java.util.List<java.lang.String> getEnumerations()
meth public java.util.List<java.lang.String> getPatterns()
meth public java.util.List<org.netbeans.modules.xml.schema.model.Whitespace$Treatment> getWhiteSpaces()
meth public void addEnumeration(java.lang.String)
meth public void addLength(int)
meth public void addMaxLength(int)
meth public void addMinLength(int)
meth public void addPattern(java.lang.String)
meth public void addWhitespace(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)
meth public void removeEnumeration(java.lang.String)
meth public void removeLength(int)
meth public void removeMaxLength(int)
meth public void removeMinLength(int)
meth public void removePattern(java.lang.String)
meth public void removeWhitespace(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)
supr org.netbeans.modules.xml.axi.datatype.StringBase
hfds enumerations,lengths,maxLengths,minLengths,patterns,whitespaces

CLSS public abstract org.netbeans.modules.xml.axi.datatype.NumberBase
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
fld public final static java.lang.Number UNBOUNDED_VALUE
fld public final static java.lang.String UNBOUNDED_STRING = "unbounded"
meth public boolean hasFacets()
meth public boolean isList()
meth public java.util.List<java.lang.Integer> getFractionDigits()
meth public java.util.List<java.lang.Integer> getTotalDigits()
meth public java.util.List<java.lang.Number> getEnumerations()
meth public java.util.List<java.lang.Number> getMaxExclusives()
meth public java.util.List<java.lang.Number> getMaxInclusives()
meth public java.util.List<java.lang.Number> getMinExclusives()
meth public java.util.List<java.lang.Number> getMinInclusives()
meth public java.util.List<java.lang.String> getPatterns()
meth public java.util.List<org.netbeans.modules.xml.axi.datatype.Datatype$Facet> getApplicableFacets()
meth public java.util.List<org.netbeans.modules.xml.schema.model.Whitespace$Treatment> getWhiteSpaces()
meth public org.netbeans.modules.xml.axi.datatype.Datatype$Kind getKind()
meth public static java.lang.Number toNumber(java.lang.String)
meth public static java.lang.String toXMLString(java.lang.Number)
meth public void addEnumeration(java.lang.Number)
meth public void addFractionDigits(int)
meth public void addMaxExclusive(java.lang.Number)
meth public void addMaxInclusive(java.lang.Number)
meth public void addMinExclusive(java.lang.Number)
meth public void addMinInclusive(java.lang.Number)
meth public void addPattern(java.lang.String)
meth public void addTotalDigits(int)
meth public void addWhitespace(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)
meth public void removeEnumeration(java.lang.Number)
meth public void removeFractionDigits(java.lang.Number)
meth public void removeMaxExclusive(java.lang.Number)
meth public void removeMaxInclusive(java.lang.Number)
meth public void removeMinExclusive(java.lang.Number)
meth public void removeMinInclusive(java.lang.Number)
meth public void removePattern(java.lang.String)
meth public void removeTotalDigits(java.lang.Number)
meth public void removeWhitespace(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)
meth public void setIsList(boolean)
supr org.netbeans.modules.xml.axi.datatype.Datatype
hfds applicableFacets,enumerations,fractionDigits,hasFacets,isList,kind,maxExclusives,maxInclusives,minExclusives,minInclusives,patterns,totalDigits,whitespaces

CLSS public org.netbeans.modules.xml.axi.datatype.PositiveIntegerType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.NonNegativeIntegerType

CLSS public org.netbeans.modules.xml.axi.datatype.QNameType
cons public init()
fld protected boolean hasFacets
fld protected java.util.List<java.lang.String> enumerations
meth public java.util.List<java.lang.Integer> getLengths()
meth public java.util.List<java.lang.Integer> getMaxLengths()
meth public java.util.List<java.lang.Integer> getMinLengths()
meth public java.util.List<java.lang.String> getEnumerations()
meth public java.util.List<java.lang.String> getPatterns()
meth public java.util.List<org.netbeans.modules.xml.schema.model.Whitespace$Treatment> getWhiteSpaces()
meth public void addEnumeration(java.lang.String)
meth public void addLength(int)
meth public void addMaxLength(int)
meth public void addMinLength(int)
meth public void addPattern(java.lang.String)
meth public void addWhitespace(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)
meth public void removeEnumeration(java.lang.String)
meth public void removeLength(int)
meth public void removeMaxLength(int)
meth public void removeMinLength(int)
meth public void removePattern(java.lang.String)
meth public void removeWhitespace(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)
supr org.netbeans.modules.xml.axi.datatype.StringBase
hfds lengths,maxLengths,minLengths,patterns,whitespaces

CLSS public org.netbeans.modules.xml.axi.datatype.ShortType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.IntType

CLSS public org.netbeans.modules.xml.axi.datatype.StringBase
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
meth public boolean hasFacets()
meth public boolean isList()
meth public java.util.List<java.lang.Integer> getLengths()
meth public java.util.List<java.lang.Integer> getMaxLengths()
meth public java.util.List<java.lang.Integer> getMinLengths()
meth public java.util.List<java.lang.String> getEnumerations()
meth public java.util.List<java.lang.String> getPatterns()
meth public java.util.List<org.netbeans.modules.xml.axi.datatype.Datatype$Facet> getApplicableFacets()
meth public java.util.List<org.netbeans.modules.xml.schema.model.Whitespace$Treatment> getWhiteSpaces()
meth public org.netbeans.modules.xml.axi.datatype.Datatype$Kind getKind()
meth public void addEnumeration(java.lang.String)
meth public void addLength(int)
meth public void addMaxLength(int)
meth public void addMinLength(int)
meth public void addPattern(java.lang.String)
meth public void addWhitespace(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)
meth public void removeEnumeration(java.lang.String)
meth public void removeLength(java.lang.Number)
meth public void removeMaxLength(java.lang.Number)
meth public void removeMinLength(java.lang.Number)
meth public void removePattern(java.lang.String)
meth public void removeWhitespace(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)
meth public void setIsList(boolean)
supr org.netbeans.modules.xml.axi.datatype.Datatype
hfds applicableFacets,enumerations,hasFacets,isList,kind,lengths,maxLengths,minLengths,name,patterns,whitespaces

CLSS public org.netbeans.modules.xml.axi.datatype.StringType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.StringBase

CLSS public abstract org.netbeans.modules.xml.axi.datatype.TimeBase
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
fld protected java.util.List<java.lang.String> enumerations
fld protected java.util.List<java.lang.String> maxExclusives
fld protected java.util.List<java.lang.String> maxInclusives
fld protected java.util.List<java.lang.String> minExclusives
fld protected java.util.List<java.lang.String> minInclusives
fld protected java.util.List<java.lang.String> patterns
fld protected java.util.List<org.netbeans.modules.xml.schema.model.Whitespace$Treatment> whitespaces
meth public boolean hasFacets()
meth public boolean isList()
meth public java.util.List<java.lang.String> getEnumerations()
meth public java.util.List<java.lang.String> getMaxExclusives()
meth public java.util.List<java.lang.String> getMaxInclusives()
meth public java.util.List<java.lang.String> getMinExclusives()
meth public java.util.List<java.lang.String> getMinInclusives()
meth public java.util.List<java.lang.String> getPatterns()
meth public java.util.List<org.netbeans.modules.xml.axi.datatype.Datatype$Facet> getApplicableFacets()
meth public java.util.List<org.netbeans.modules.xml.schema.model.Whitespace$Treatment> getWhiteSpaces()
meth public org.netbeans.modules.xml.axi.datatype.Datatype$Kind getKind()
meth public void addEnumeration(java.lang.String)
meth public void addMaxExclusive(java.lang.String)
meth public void addMaxInclusive(java.lang.String)
meth public void addMinExclusive(java.lang.String)
meth public void addMinInclusive(java.lang.String)
meth public void addPattern(java.lang.String)
meth public void addWhitespace(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)
meth public void removeEnumeration(java.lang.String)
meth public void removeMaxExclusive(java.lang.String)
meth public void removeMaxInclusive(java.lang.String)
meth public void removeMinExclusive(java.lang.String)
meth public void removeMinInclusive(java.lang.String)
meth public void removePattern(java.lang.String)
meth public void removeWhitespace(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)
meth public void setIsList(boolean)
supr org.netbeans.modules.xml.axi.datatype.Datatype
hfds applicableFacets,hasFacets,isList,kind

CLSS public org.netbeans.modules.xml.axi.datatype.TimeType
cons public init()
supr org.netbeans.modules.xml.axi.datatype.TimeBase

CLSS public org.netbeans.modules.xml.axi.datatype.TokenType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.NormalizedStringType

CLSS public org.netbeans.modules.xml.axi.datatype.UnionType
cons public init()
fld protected boolean hasFacets
meth public boolean hasFacets()
meth public boolean isList()
meth public java.util.List<org.netbeans.modules.xml.axi.datatype.Datatype$Facet> getApplicableFacets()
meth public java.util.List<org.netbeans.modules.xml.axi.datatype.Datatype> getMemberTypes()
meth public org.netbeans.modules.xml.axi.datatype.Datatype$Kind getKind()
meth public void addMemberType(org.netbeans.modules.xml.axi.datatype.Datatype)
meth public void setHasFacets(boolean)
meth public void setIsList(boolean)
supr org.netbeans.modules.xml.axi.datatype.Datatype
hfds applicableFacets,isList,kind,m

CLSS public org.netbeans.modules.xml.axi.datatype.UnsignedByteType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.NonNegativeIntegerType

CLSS public org.netbeans.modules.xml.axi.datatype.UnsignedIntType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.NonNegativeIntegerType

CLSS public org.netbeans.modules.xml.axi.datatype.UnsignedLongType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
supr org.netbeans.modules.xml.axi.datatype.NonNegativeIntegerType

CLSS public org.netbeans.modules.xml.axi.datatype.UnsignedShortType
cons public init()
cons public init(org.netbeans.modules.xml.axi.datatype.Datatype$Kind)
meth protected void initialize()
supr org.netbeans.modules.xml.axi.datatype.NonNegativeIntegerType

CLSS public org.netbeans.modules.xml.axi.visitor.AXINonCyclicVisitor
cons public init(org.netbeans.modules.xml.axi.AXIModel)
meth public boolean canVisit(org.netbeans.modules.xml.axi.Element)
meth public void expand(java.util.List<org.netbeans.modules.xml.axi.Element>)
meth public void expand(org.netbeans.modules.xml.axi.AXIDocument)
meth public void visit(org.netbeans.modules.xml.axi.Element)
meth public void visitChildren(org.netbeans.modules.xml.axi.Element)
supr org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor
hfds am,path,sm

CLSS public abstract interface org.netbeans.modules.xml.axi.visitor.AXIVisitor
meth public abstract void visit(org.netbeans.modules.xml.axi.AXIDocument)
meth public abstract void visit(org.netbeans.modules.xml.axi.AnyAttribute)
meth public abstract void visit(org.netbeans.modules.xml.axi.AnyElement)
meth public abstract void visit(org.netbeans.modules.xml.axi.Attribute)
meth public abstract void visit(org.netbeans.modules.xml.axi.Compositor)
meth public abstract void visit(org.netbeans.modules.xml.axi.ContentModel)
meth public abstract void visit(org.netbeans.modules.xml.axi.Element)
meth public abstract void visit(org.netbeans.modules.xml.axi.datatype.Datatype)

CLSS public abstract interface org.netbeans.modules.xml.axi.visitor.AXIVisitor2
intf org.netbeans.modules.xml.axi.visitor.AXIVisitor
meth public abstract void visit(org.netbeans.modules.xml.axi.SchemaReference)

CLSS public org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor
cons public init()
meth protected boolean canVisit(org.netbeans.modules.xml.axi.AXIComponent)
meth protected void visitChildren(org.netbeans.modules.xml.axi.AXIComponent)
meth public void visit(org.netbeans.modules.xml.axi.AXIDocument)
meth public void visit(org.netbeans.modules.xml.axi.AnyAttribute)
meth public void visit(org.netbeans.modules.xml.axi.AnyElement)
meth public void visit(org.netbeans.modules.xml.axi.Attribute)
meth public void visit(org.netbeans.modules.xml.axi.Compositor)
meth public void visit(org.netbeans.modules.xml.axi.ContentModel)
meth public void visit(org.netbeans.modules.xml.axi.Element)
supr org.netbeans.modules.xml.axi.visitor.DefaultVisitor
hfds pathToRoot

CLSS public abstract org.netbeans.modules.xml.axi.visitor.DefaultVisitor
cons public init()
intf org.netbeans.modules.xml.axi.visitor.AXIVisitor2
meth public void visit(org.netbeans.modules.xml.axi.AXIDocument)
meth public void visit(org.netbeans.modules.xml.axi.AnyAttribute)
meth public void visit(org.netbeans.modules.xml.axi.AnyElement)
meth public void visit(org.netbeans.modules.xml.axi.Attribute)
meth public void visit(org.netbeans.modules.xml.axi.Compositor)
meth public void visit(org.netbeans.modules.xml.axi.ContentModel)
meth public void visit(org.netbeans.modules.xml.axi.Element)
meth public void visit(org.netbeans.modules.xml.axi.SchemaReference)
meth public void visit(org.netbeans.modules.xml.axi.datatype.Datatype)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.axi.visitor.FindUsageVisitor
cons public init(org.netbeans.modules.xml.axi.AXIModel)
meth public org.netbeans.modules.xml.axi.impl.Preview findUsages(org.netbeans.modules.xml.axi.AXIDocument)
meth public org.netbeans.modules.xml.axi.impl.Preview findUsages(org.netbeans.modules.xml.axi.Element)
meth public void visit(org.netbeans.modules.xml.axi.Element)
supr org.netbeans.modules.xml.axi.visitor.AXINonCyclicVisitor
hfds p,usedBy

CLSS public org.netbeans.modules.xml.axi.visitor.PrintAXITreeVisitor
cons public init()
meth protected void visitChildren(org.netbeans.modules.xml.axi.AXIComponent)
supr org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor
hfds PRINT_TO_CONSOLE,depth

CLSS public abstract org.netbeans.modules.xml.xam.AbstractComponent<%0 extends org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.AbstractComponent%0}>>
cons public init(org.netbeans.modules.xml.xam.AbstractModel)
intf org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.AbstractComponent%0}>
meth protected <%0 extends {org.netbeans.modules.xml.xam.AbstractComponent%0}> {%%0} getChild(java.lang.Class<{%%0}>)
meth protected abstract void appendChildQuietly({org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}>)
meth protected abstract void insertAtIndexQuietly({org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}>,int)
meth protected abstract void populateChildren(java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}>)
meth protected abstract void removeChildQuietly({org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}>)
meth protected final boolean isChildrenInitialized()
meth protected void addAfter(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth protected void addBefore(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth protected void appendChild(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0})
meth protected void checkNullOrDuplicateChild({org.netbeans.modules.xml.xam.AbstractComponent%0})
meth protected void fireChildAdded()
meth protected void fireChildRemoved()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void fireValueChanged()
meth protected void insertAtIndex(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},int,java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>)
meth protected void setChild(java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>,java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth protected void setChild(java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>,java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>,boolean)
meth protected void setChildAfter(java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>,java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth protected void setChildBefore(java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>,java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth protected void setModel(org.netbeans.modules.xml.xam.AbstractModel)
meth protected void setParent({org.netbeans.modules.xml.xam.AbstractComponent%0})
meth protected void verifyWrite()
meth public <%0 extends {org.netbeans.modules.xml.xam.AbstractComponent%0}> java.util.List<{%%0}> getChildren(java.lang.Class<{%%0}>)
meth public boolean canPaste(org.netbeans.modules.xml.xam.Component)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public int getChildrenCount()
meth public java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}> getChildren()
meth public java.util.List<{org.netbeans.modules.xml.xam.AbstractComponent%0}> getChildren(java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.AbstractComponent%0}>>)
meth public org.netbeans.modules.xml.xam.AbstractModel getModel()
meth public void checkChildrenPopulated()
meth public void insertAtIndex(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0},int)
meth public void removeChild(java.lang.String,{org.netbeans.modules.xml.xam.AbstractComponent%0})
meth public void removeComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public {org.netbeans.modules.xml.xam.AbstractComponent%0} getParent()
supr java.lang.Object
hfds children,model,parent
hcls DelegateListener

CLSS public abstract org.netbeans.modules.xml.xam.AbstractModel<%0 extends org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.AbstractModel%0}>>
cons public init(org.netbeans.modules.xml.xam.ModelSource)
fld protected org.netbeans.modules.xml.xam.AbstractModel$ModelUndoableEditSupport ues
innr protected ModelUndoableEdit
innr protected ModelUndoableEditSupport
intf javax.swing.event.UndoableEditListener
intf org.netbeans.modules.xml.xam.Model<{org.netbeans.modules.xml.xam.AbstractModel%0}>
meth protected boolean needsSync()
meth protected javax.swing.undo.CompoundEdit createModelUndoableEdit()
meth protected void endTransaction(boolean)
meth protected void finishTransaction()
meth protected void refresh()
meth protected void setInSync(boolean)
meth protected void setInUndoRedo(boolean)
meth protected void setState(org.netbeans.modules.xml.xam.Model$State)
meth protected void syncCompleted()
meth protected void syncStarted()
meth protected void transactionCompleted()
meth protected void transactionStarted()
meth public abstract org.netbeans.modules.xml.xam.ModelAccess getAccess()
meth public boolean inSync()
meth public boolean inUndoRedo()
meth public boolean isAutoSyncActive()
meth public boolean isIntransaction()
meth public boolean startTransaction()
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
meth public boolean startedFiringEvents()
meth public org.netbeans.modules.xml.xam.Model$State getState()
meth public org.netbeans.modules.xml.xam.ModelSource getModelSource()
meth public void addComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void addUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public void endTransaction()
meth public void fireComponentChangedEvent(org.netbeans.modules.xml.xam.ComponentEvent)
meth public void firePropertyChangeEvent(java.beans.PropertyChangeEvent)
meth public void removeComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void removeUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public void rollbackTransaction()
meth public void setAutoSyncActive(boolean)
meth public void sync() throws java.io.IOException
meth public void undoableEditHappened(javax.swing.event.UndoableEditEvent)
meth public void validateWrite()
supr java.lang.Object
hfds RP,componentListeners,inSync,inUndoRedo,logger,pcs,savedUndoableEditListeners,source,status,transaction
hcls Transaction

CLSS public abstract org.netbeans.modules.xml.xam.AbstractModelFactory<%0 extends org.netbeans.modules.xml.xam.Model>
cons public init()
fld public final static int DELAY_DIRTY = 1000
fld public final static int DELAY_SYNCER = 2000
fld public final static java.lang.String MODEL_LOADED_PROPERTY = "modelLoaded"
meth protected abstract {org.netbeans.modules.xml.xam.AbstractModelFactory%0} createModel(org.netbeans.modules.xml.xam.ModelSource)
meth protected java.lang.Object getKey(org.netbeans.modules.xml.xam.ModelSource)
meth protected {org.netbeans.modules.xml.xam.AbstractModelFactory%0} getModel(org.netbeans.modules.xml.xam.ModelSource)
meth public java.util.List<{org.netbeans.modules.xml.xam.AbstractModelFactory%0}> getModels()
meth public static org.netbeans.modules.xml.xam.spi.ModelAccessProvider getAccessProvider()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public {org.netbeans.modules.xml.xam.AbstractModelFactory%0} createFreshModel(org.netbeans.modules.xml.xam.ModelSource)
supr java.lang.Object
hfds LOG,SYNCER,cachedModels,factories,propSupport

CLSS public abstract interface org.netbeans.modules.xml.xam.Component<%0 extends org.netbeans.modules.xml.xam.Component>
meth public abstract <%0 extends {org.netbeans.modules.xml.xam.Component%0}> java.util.List<{%%0}> getChildren(java.lang.Class<{%%0}>)
meth public abstract boolean canPaste(org.netbeans.modules.xml.xam.Component)
meth public abstract java.util.List<{org.netbeans.modules.xml.xam.Component%0}> getChildren()
meth public abstract java.util.List<{org.netbeans.modules.xml.xam.Component%0}> getChildren(java.util.Collection<java.lang.Class<? extends {org.netbeans.modules.xml.xam.Component%0}>>)
meth public abstract org.netbeans.modules.xml.xam.Component copy({org.netbeans.modules.xml.xam.Component%0})
meth public abstract org.netbeans.modules.xml.xam.Model getModel()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract {org.netbeans.modules.xml.xam.Component%0} getParent()

CLSS public abstract interface org.netbeans.modules.xml.xam.Model<%0 extends org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.Model%0}>>
fld public final static java.lang.String STATE_PROPERTY = "state"
innr public final static !enum State
intf org.netbeans.modules.xml.xam.Referenceable
meth public abstract boolean inSync()
meth public abstract boolean isIntransaction()
meth public abstract boolean startTransaction()
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
meth public abstract org.netbeans.modules.xml.xam.Model$State getState()
meth public abstract org.netbeans.modules.xml.xam.ModelSource getModelSource()
meth public abstract void addChildComponent(org.netbeans.modules.xml.xam.Component,org.netbeans.modules.xml.xam.Component,int)
meth public abstract void addComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void addUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public abstract void endTransaction()
meth public abstract void removeChildComponent(org.netbeans.modules.xml.xam.Component)
meth public abstract void removeComponentListener(org.netbeans.modules.xml.xam.ComponentListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void removeUndoableRefactorListener(javax.swing.event.UndoableEditListener)
meth public abstract void sync() throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.xml.xam.Referenceable

