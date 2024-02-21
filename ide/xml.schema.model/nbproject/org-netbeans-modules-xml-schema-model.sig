#Signature file v4.1
#Version 1.55.0

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

CLSS public abstract interface org.netbeans.modules.xml.schema.model.All
fld public final static java.lang.String ELEMENT_PROPERTY = "element"
fld public final static java.lang.String ELEMENT_REFERENCE_PROPERTY = "elementReference"
fld public final static java.lang.String MIN_OCCURS_PROPERTY = "minOccurs"
intf org.netbeans.modules.xml.schema.model.ComplexExtensionDefinition
intf org.netbeans.modules.xml.schema.model.ComplexTypeDefinition
intf org.netbeans.modules.xml.schema.model.LocalGroupDefinition
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract boolean allowsFullMultiplicity()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.ElementReference> getElementReferences()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.LocalElement> getElements()
meth public abstract org.netbeans.modules.xml.schema.model.Occur$ZeroOne getMinOccurs()
meth public abstract org.netbeans.modules.xml.schema.model.Occur$ZeroOne getMinOccursDefault()
meth public abstract org.netbeans.modules.xml.schema.model.Occur$ZeroOne getMinOccursEffective()
meth public abstract void addElement(org.netbeans.modules.xml.schema.model.LocalElement)
meth public abstract void addElementReference(org.netbeans.modules.xml.schema.model.ElementReference)
meth public abstract void removeElement(org.netbeans.modules.xml.schema.model.LocalElement)
meth public abstract void removeElementReference(org.netbeans.modules.xml.schema.model.ElementReference)
meth public abstract void setMinOccurs(org.netbeans.modules.xml.schema.model.Occur$ZeroOne)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Annotation
fld public final static java.lang.String APPINFO_PROPERTY = "appinfo"
fld public final static java.lang.String DOCUMENTATION_PROPERTY = "documentation"
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.AppInfo> getAppInfos()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.Documentation> getDocumentationElements()
meth public abstract void addAppInfo(org.netbeans.modules.xml.schema.model.AppInfo)
meth public abstract void addDocumentation(org.netbeans.modules.xml.schema.model.Documentation)
meth public abstract void removeAppInfo(org.netbeans.modules.xml.schema.model.AppInfo)
meth public abstract void removeDocumentation(org.netbeans.modules.xml.schema.model.Documentation)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Any
fld public final static java.lang.String NAMESPACE_PROPERTY = "namespace"
fld public final static java.lang.String PROCESS_CONTENTS_PROPERTY = "processContents"
innr public final static !enum ProcessContents
meth public abstract java.lang.String getNameSpaceEffective()
meth public abstract java.lang.String getNamespace()
meth public abstract java.lang.String getNamespaceDefault()
meth public abstract org.netbeans.modules.xml.schema.model.Any$ProcessContents getProcessContents()
meth public abstract org.netbeans.modules.xml.schema.model.Any$ProcessContents getProcessContentsDefault()
meth public abstract org.netbeans.modules.xml.schema.model.Any$ProcessContents getProcessContentsEffective()
meth public abstract void setNamespace(java.lang.String)
meth public abstract void setProcessContents(org.netbeans.modules.xml.schema.model.Any$ProcessContents)

CLSS public final static !enum org.netbeans.modules.xml.schema.model.Any$ProcessContents
 outer org.netbeans.modules.xml.schema.model.Any
fld public final static org.netbeans.modules.xml.schema.model.Any$ProcessContents LAX
fld public final static org.netbeans.modules.xml.schema.model.Any$ProcessContents SKIP
fld public final static org.netbeans.modules.xml.schema.model.Any$ProcessContents STRICT
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.schema.model.Any$ProcessContents valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.schema.model.Any$ProcessContents[] values()
supr java.lang.Enum<org.netbeans.modules.xml.schema.model.Any$ProcessContents>
hfds value

CLSS public abstract interface org.netbeans.modules.xml.schema.model.AnyAttribute
intf org.netbeans.modules.xml.schema.model.Any
intf org.netbeans.modules.xml.schema.model.SchemaComponent

CLSS public abstract interface org.netbeans.modules.xml.schema.model.AnyElement
fld public final static java.lang.String MAX_OCCURS_PROPERTY = "maxOccurs"
fld public final static java.lang.String MIN_OCCURS_PROPERTY = "minOccurs"
intf org.netbeans.modules.xml.schema.model.Any
intf org.netbeans.modules.xml.schema.model.SchemaComponent
intf org.netbeans.modules.xml.schema.model.SequenceDefinition
meth public abstract int getMinOccursDefault()
meth public abstract int getMinOccursEffective()
meth public abstract java.lang.Integer getMinOccurs()
meth public abstract java.lang.String getMaxOccurs()
meth public abstract java.lang.String getMaxOccursDefault()
meth public abstract java.lang.String getMaxOccursEffective()
meth public abstract void setMaxOccurs(java.lang.String)
meth public abstract void setMinOccurs(java.lang.Integer)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.AppInfo
fld public final static java.lang.String CONTENT_PROPERTY = "content"
fld public final static java.lang.String SOURCE_PROPERTY = "source"
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract java.lang.String getContentFragment()
meth public abstract java.lang.String getURI()
meth public abstract org.w3c.dom.Element getAppInfoElement()
meth public abstract void setAppInfoElement(org.w3c.dom.Element)
meth public abstract void setContentFragment(java.lang.String) throws java.io.IOException
meth public abstract void setURI(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Attribute
fld public final static java.lang.String DEFAULT_PROPERTY = "default"
fld public final static java.lang.String FIXED_PROPERTY = "fixed"
fld public final static java.lang.String INLINE_TYPE_PROPERTY = "inlineType"
fld public final static java.lang.String TYPE_PROPERTY = "type"
innr public final static !enum Use
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract java.lang.String getDefault()
meth public abstract java.lang.String getFixed()
meth public abstract void setDefault(java.lang.String)
meth public abstract void setFixed(java.lang.String)

CLSS public final static !enum org.netbeans.modules.xml.schema.model.Attribute$Use
 outer org.netbeans.modules.xml.schema.model.Attribute
fld public final static org.netbeans.modules.xml.schema.model.Attribute$Use OPTIONAL
fld public final static org.netbeans.modules.xml.schema.model.Attribute$Use PROHIBITED
fld public final static org.netbeans.modules.xml.schema.model.Attribute$Use REQUIRED
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.schema.model.Attribute$Use valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.schema.model.Attribute$Use[] values()
supr java.lang.Enum<org.netbeans.modules.xml.schema.model.Attribute$Use>
hfds value

CLSS public abstract interface org.netbeans.modules.xml.schema.model.AttributeGroupReference
fld public final static java.lang.String GROUP_PROPERTY = "group"
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalAttributeGroup> getGroup()
meth public abstract void setGroup(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalAttributeGroup>)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.AttributeReference
fld public final static java.lang.String FORM_PROPERTY = "form"
fld public final static java.lang.String REF_PROPERTY = "ref"
fld public final static java.lang.String USE_PROPERTY = "use"
intf org.netbeans.modules.xml.schema.model.Attribute
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract org.netbeans.modules.xml.schema.model.Attribute$Use getUse()
meth public abstract org.netbeans.modules.xml.schema.model.Attribute$Use getUseDefault()
meth public abstract org.netbeans.modules.xml.schema.model.Attribute$Use getUseEffective()
meth public abstract org.netbeans.modules.xml.schema.model.Form getForm()
meth public abstract org.netbeans.modules.xml.schema.model.Form getFormDefault()
meth public abstract org.netbeans.modules.xml.schema.model.Form getFormEffective()
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalAttribute> getRef()
meth public abstract void setForm(org.netbeans.modules.xml.schema.model.Form)
meth public abstract void setRef(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalAttribute>)
meth public abstract void setUse(org.netbeans.modules.xml.schema.model.Attribute$Use)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.BoundaryFacet
fld public final static java.lang.String FIXED_PROPERTY = "fixed"
fld public final static java.lang.String VALUE_PROPERTY = "value"
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract boolean getFixedDefault()
meth public abstract boolean getFixedEffective()
meth public abstract java.lang.Boolean isFixed()
meth public abstract java.lang.String getValue()
meth public abstract void setFixed(java.lang.Boolean)
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Cardinality
meth public abstract int getMinOccursDefault()
meth public abstract int getMinOccursEffective()
meth public abstract java.lang.Integer getMinOccurs()
meth public abstract java.lang.String getMaxOccurs()
meth public abstract java.lang.String getMaxOccursDefault()
meth public abstract java.lang.String getMaxOccursEffective()
meth public abstract void setMaxOccurs(java.lang.String)
meth public abstract void setMinOccurs(java.lang.Integer)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Choice
fld public final static java.lang.String ANY_PROPERTY = "any"
fld public final static java.lang.String CHOICE_PROPERTY = "choice"
fld public final static java.lang.String ELEMENT_REFERENCE_PROPERTY = "elementReference"
fld public final static java.lang.String GROUP_REF_PROPERTY = "groupReference"
fld public final static java.lang.String LOCAL_ELEMENT_PROPERTY = "localElememnt"
fld public final static java.lang.String MAX_OCCURS_PROPERTY = "maxOccurs"
fld public final static java.lang.String MIN_OCCURS_PROPERTY = "minOccurs"
fld public final static java.lang.String SEQUENCE_PROPERTY = "sequence"
intf org.netbeans.modules.xml.schema.model.ComplexExtensionDefinition
intf org.netbeans.modules.xml.schema.model.ComplexTypeDefinition
intf org.netbeans.modules.xml.schema.model.LocalGroupDefinition
intf org.netbeans.modules.xml.schema.model.SchemaComponent
intf org.netbeans.modules.xml.schema.model.SequenceDefinition
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.AnyElement> getAnys()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.Choice> getChoices()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.ElementReference> getElementReferences()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.GroupReference> getGroupReferences()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.LocalElement> getLocalElements()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.Sequence> getSequences()
meth public abstract org.netbeans.modules.xml.schema.model.Cardinality getCardinality()
meth public abstract void addAny(org.netbeans.modules.xml.schema.model.AnyElement)
meth public abstract void addChoice(org.netbeans.modules.xml.schema.model.Choice)
meth public abstract void addElementReference(org.netbeans.modules.xml.schema.model.ElementReference)
meth public abstract void addGroupReference(org.netbeans.modules.xml.schema.model.GroupReference)
meth public abstract void addLocalElement(org.netbeans.modules.xml.schema.model.LocalElement)
meth public abstract void addSequence(org.netbeans.modules.xml.schema.model.Sequence)
meth public abstract void removeAny(org.netbeans.modules.xml.schema.model.AnyElement)
meth public abstract void removeChoice(org.netbeans.modules.xml.schema.model.Choice)
meth public abstract void removeElementReference(org.netbeans.modules.xml.schema.model.ElementReference)
meth public abstract void removeGroupReference(org.netbeans.modules.xml.schema.model.GroupReference)
meth public abstract void removeLocalElement(org.netbeans.modules.xml.schema.model.LocalElement)
meth public abstract void removeSequence(org.netbeans.modules.xml.schema.model.Sequence)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.ComplexContent
fld public final static java.lang.String LOCAL_DEFINITION_PROPERTY = "localDefinition"
fld public final static java.lang.String MIXED_PROPERTY = "mixed"
intf org.netbeans.modules.xml.schema.model.ComplexTypeDefinition
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract boolean getMixedDefault()
meth public abstract boolean getMixedEffective()
meth public abstract java.lang.Boolean isMixed()
meth public abstract org.netbeans.modules.xml.schema.model.ComplexContentDefinition getLocalDefinition()
meth public abstract void setLocalDefinition(org.netbeans.modules.xml.schema.model.ComplexContentDefinition)
meth public abstract void setMixed(java.lang.Boolean)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.ComplexContentDefinition
intf org.netbeans.modules.xml.schema.model.SchemaComponent

CLSS public abstract interface org.netbeans.modules.xml.schema.model.ComplexContentRestriction
fld public final static java.lang.String BASE_PROPERTY = "base"
fld public final static java.lang.String DEFINITION_CHANGED_PROPERTY = "definition"
intf org.netbeans.modules.xml.schema.model.ComplexContentDefinition
intf org.netbeans.modules.xml.schema.model.LocalAttributeContainer
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract org.netbeans.modules.xml.schema.model.ComplexTypeDefinition getDefinition()
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalComplexType> getBase()
meth public abstract void setBase(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalComplexType>)
meth public abstract void setDefinition(org.netbeans.modules.xml.schema.model.ComplexTypeDefinition)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.ComplexExtension
fld public final static java.lang.String LOCAL_DEFINITION_PROPERTY = "localDefinition"
intf org.netbeans.modules.xml.schema.model.ComplexContentDefinition
intf org.netbeans.modules.xml.schema.model.Extension
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract org.netbeans.modules.xml.schema.model.ComplexExtensionDefinition getLocalDefinition()
meth public abstract void setLocalDefinition(org.netbeans.modules.xml.schema.model.ComplexExtensionDefinition)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.ComplexExtensionDefinition
intf org.netbeans.modules.xml.schema.model.SchemaComponent

CLSS public abstract interface org.netbeans.modules.xml.schema.model.ComplexType
fld public final static java.lang.String DEFINITION_PROPERTY = "definition"
fld public final static java.lang.String MIXED_PROPERTY = "mixed"
intf org.netbeans.modules.xml.schema.model.LocalAttributeContainer
meth public abstract boolean getMixedDefault()
meth public abstract boolean getMixedEffective()
meth public abstract java.lang.Boolean isMixed()
meth public abstract org.netbeans.modules.xml.schema.model.ComplexTypeDefinition getDefinition()
meth public abstract void setDefinition(org.netbeans.modules.xml.schema.model.ComplexTypeDefinition)
meth public abstract void setMixed(java.lang.Boolean)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.ComplexTypeDefinition
intf org.netbeans.modules.xml.schema.model.SchemaComponent

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Constraint
fld public final static java.lang.String FIELD_PROPERTY = "field"
fld public final static java.lang.String SELECTOR_PROPERTY = "selector"
intf org.netbeans.modules.xml.schema.model.NameableSchemaComponent
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.Field> getFields()
meth public abstract org.netbeans.modules.xml.schema.model.Selector getSelector()
meth public abstract void addField(org.netbeans.modules.xml.schema.model.Field)
meth public abstract void deleteField(org.netbeans.modules.xml.schema.model.Field)
meth public abstract void setSelector(org.netbeans.modules.xml.schema.model.Selector)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Derivation
innr public final static !enum Type

CLSS public final static !enum org.netbeans.modules.xml.schema.model.Derivation$Type
 outer org.netbeans.modules.xml.schema.model.Derivation
fld public final static org.netbeans.modules.xml.schema.model.Derivation$Type ALL
fld public final static org.netbeans.modules.xml.schema.model.Derivation$Type EMPTY
fld public final static org.netbeans.modules.xml.schema.model.Derivation$Type EXTENSION
fld public final static org.netbeans.modules.xml.schema.model.Derivation$Type LIST
fld public final static org.netbeans.modules.xml.schema.model.Derivation$Type RESTRICTION
fld public final static org.netbeans.modules.xml.schema.model.Derivation$Type SUBSTITUTION
fld public final static org.netbeans.modules.xml.schema.model.Derivation$Type UNION
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.schema.model.Derivation$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.schema.model.Derivation$Type[] values()
supr java.lang.Enum<org.netbeans.modules.xml.schema.model.Derivation$Type>
hfds value

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Documentation
fld public final static java.lang.String CONTENT_PROPERTY = "content"
fld public final static java.lang.String LANGUAGE_PROPERTY = "language"
fld public final static java.lang.String SOURCE_PROPERTY = "source"
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract java.lang.String getContent()
meth public abstract java.lang.String getContentFragment()
meth public abstract java.lang.String getLanguage()
meth public abstract java.lang.String getSource()
meth public abstract org.w3c.dom.Element getDocumentationElement()
meth public abstract void setContent(java.lang.String)
meth public abstract void setContentFragment(java.lang.String) throws java.io.IOException
meth public abstract void setDocumentationElement(org.w3c.dom.Element)
meth public abstract void setLanguage(java.lang.String)
meth public abstract void setSource(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Element
fld public final static java.lang.String BLOCK_PROPERTY = "block"
fld public final static java.lang.String CONSTRAINT_PROPERTY = "constraint"
fld public final static java.lang.String DEFAULT_PROPERTY = "default"
fld public final static java.lang.String FIXED_PROPERTY = "fixed"
fld public final static java.lang.String NILLABLE_PROPERTY = "nillable"
fld public final static java.lang.String REF_PROPERTY = "ref"
innr public final static !enum Block
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract boolean getNillableDefault()
meth public abstract boolean getNillableEffective()
meth public abstract java.lang.Boolean isNillable()
meth public abstract java.lang.String getDefault()
meth public abstract java.lang.String getFixed()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.Constraint> getConstraints()
meth public abstract void addConstraint(org.netbeans.modules.xml.schema.model.Constraint)
meth public abstract void removeConstraint(org.netbeans.modules.xml.schema.model.Constraint)
meth public abstract void setDefault(java.lang.String)
meth public abstract void setFixed(java.lang.String)
meth public abstract void setNillable(java.lang.Boolean)

CLSS public final static !enum org.netbeans.modules.xml.schema.model.Element$Block
 outer org.netbeans.modules.xml.schema.model.Element
fld public final static org.netbeans.modules.xml.schema.model.Element$Block ALL
fld public final static org.netbeans.modules.xml.schema.model.Element$Block EMPTY
fld public final static org.netbeans.modules.xml.schema.model.Element$Block EXTENSION
fld public final static org.netbeans.modules.xml.schema.model.Element$Block RESTRICTION
fld public final static org.netbeans.modules.xml.schema.model.Element$Block SUBSTITUTION
intf org.netbeans.modules.xml.schema.model.Derivation
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.schema.model.Element$Block valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.schema.model.Element$Block[] values()
supr java.lang.Enum<org.netbeans.modules.xml.schema.model.Element$Block>
hfds value

CLSS public abstract interface org.netbeans.modules.xml.schema.model.ElementReference
fld public final static java.lang.String FORM_PROPERTY = "form"
fld public final static java.lang.String MAX_OCCURS_PROPERTY = "maxOccurs"
fld public final static java.lang.String MIN_OCCURS_PROPERTY = "minOccurs"
intf org.netbeans.modules.xml.schema.model.Element
intf org.netbeans.modules.xml.schema.model.SchemaComponent
intf org.netbeans.modules.xml.schema.model.SequenceDefinition
meth public abstract boolean allowsFullMultiplicity()
meth public abstract int getMinOccursDefault()
meth public abstract int getMinOccursEffective()
meth public abstract java.lang.Integer getMinOccurs()
meth public abstract java.lang.String getMaxOccurs()
meth public abstract java.lang.String getMaxOccursDefault()
meth public abstract java.lang.String getMaxOccursEffective()
meth public abstract org.netbeans.modules.xml.schema.model.Form getForm()
meth public abstract org.netbeans.modules.xml.schema.model.Form getFormDefault()
meth public abstract org.netbeans.modules.xml.schema.model.Form getFormEffective()
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalElement> getRef()
meth public abstract void setForm(org.netbeans.modules.xml.schema.model.Form)
meth public abstract void setMaxOccurs(java.lang.String)
meth public abstract void setMinOccurs(java.lang.Integer)
meth public abstract void setRef(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalElement>)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Enumeration
fld public final static java.lang.String VALUE_PROPERTY = "value"
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract java.lang.String getValue()
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Extension
fld public final static java.lang.String BASE_PROPERTY = "base"
intf org.netbeans.modules.xml.schema.model.LocalAttributeContainer
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalType> getBase()
meth public abstract void setBase(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalType>)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Field
fld public final static java.lang.String XPATH_PROPERTY = "xPath"
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract java.lang.String getXPath()
meth public abstract void setXPath(java.lang.String)

CLSS public final !enum org.netbeans.modules.xml.schema.model.Form
fld public final static org.netbeans.modules.xml.schema.model.Form QUALIFIED
fld public final static org.netbeans.modules.xml.schema.model.Form UNQUALIFIED
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.schema.model.Form valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.schema.model.Form[] values()
supr java.lang.Enum<org.netbeans.modules.xml.schema.model.Form>
hfds value

CLSS public abstract interface org.netbeans.modules.xml.schema.model.FractionDigits
intf org.netbeans.modules.xml.schema.model.LengthFacet

CLSS public abstract interface org.netbeans.modules.xml.schema.model.GlobalAttribute
intf org.netbeans.modules.xml.schema.model.Attribute
intf org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent
meth public abstract org.netbeans.modules.xml.schema.model.LocalSimpleType getInlineType()
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalSimpleType> getType()
meth public abstract void setInlineType(org.netbeans.modules.xml.schema.model.LocalSimpleType)
meth public abstract void setType(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalSimpleType>)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.GlobalAttributeGroup
intf org.netbeans.modules.xml.schema.model.LocalAttributeContainer
intf org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent

CLSS public abstract interface org.netbeans.modules.xml.schema.model.GlobalComplexType
fld public final static java.lang.String ABSTRACT_PROPERTY = "abstract"
fld public final static java.lang.String BLOCK_PROPERTY = "block"
fld public final static java.lang.String FINAL_PROPERTY = "final"
innr public final static !enum Block
innr public final static !enum Final
intf org.netbeans.modules.xml.schema.model.ComplexType
intf org.netbeans.modules.xml.schema.model.GlobalType
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract boolean getAbstractDefault()
meth public abstract boolean getAbstractEffective()
meth public abstract java.lang.Boolean isAbstract()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.GlobalComplexType$Block> getBlock()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.GlobalComplexType$Block> getBlockDefault()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.GlobalComplexType$Block> getBlockEffective()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.GlobalComplexType$Final> getFinal()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.GlobalComplexType$Final> getFinalDefault()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.GlobalComplexType$Final> getFinalEffective()
meth public abstract void setAbstract(java.lang.Boolean)
meth public abstract void setBlock(java.util.Set<org.netbeans.modules.xml.schema.model.GlobalComplexType$Block>)
meth public abstract void setFinal(java.util.Set<org.netbeans.modules.xml.schema.model.GlobalComplexType$Final>)

CLSS public final static !enum org.netbeans.modules.xml.schema.model.GlobalComplexType$Block
 outer org.netbeans.modules.xml.schema.model.GlobalComplexType
fld public final static org.netbeans.modules.xml.schema.model.GlobalComplexType$Block ALL
fld public final static org.netbeans.modules.xml.schema.model.GlobalComplexType$Block EMPTY
fld public final static org.netbeans.modules.xml.schema.model.GlobalComplexType$Block EXTENSION
fld public final static org.netbeans.modules.xml.schema.model.GlobalComplexType$Block RESTRICTION
intf org.netbeans.modules.xml.schema.model.Derivation
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.schema.model.GlobalComplexType$Block valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.schema.model.GlobalComplexType$Block[] values()
supr java.lang.Enum<org.netbeans.modules.xml.schema.model.GlobalComplexType$Block>
hfds value

CLSS public final static !enum org.netbeans.modules.xml.schema.model.GlobalComplexType$Final
 outer org.netbeans.modules.xml.schema.model.GlobalComplexType
fld public final static org.netbeans.modules.xml.schema.model.GlobalComplexType$Final ALL
fld public final static org.netbeans.modules.xml.schema.model.GlobalComplexType$Final EMPTY
fld public final static org.netbeans.modules.xml.schema.model.GlobalComplexType$Final EXTENSION
fld public final static org.netbeans.modules.xml.schema.model.GlobalComplexType$Final RESTRICTION
intf org.netbeans.modules.xml.schema.model.Derivation
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.schema.model.GlobalComplexType$Final valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.schema.model.GlobalComplexType$Final[] values()
supr java.lang.Enum<org.netbeans.modules.xml.schema.model.GlobalComplexType$Final>
hfds value

CLSS public abstract interface org.netbeans.modules.xml.schema.model.GlobalElement
fld public final static java.lang.String ABSTRACT_PROPERTY = "abstract"
fld public final static java.lang.String FINAL_PROPERTY = "final"
fld public final static java.lang.String SUBSTITUTION_GROUP_PROPERTY = "substitutionGroup"
innr public final static !enum Final
intf org.netbeans.modules.xml.schema.model.Element
intf org.netbeans.modules.xml.schema.model.NameableSchemaComponent
intf org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent
intf org.netbeans.modules.xml.schema.model.TypeContainer
meth public abstract boolean getAbstractDefault()
meth public abstract boolean getAbstractEffective()
meth public abstract java.lang.Boolean isAbstract()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.Element$Block> getBlock()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.Element$Block> getBlockDefault()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.Element$Block> getBlockEffective()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.GlobalElement$Final> getFinal()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.GlobalElement$Final> getFinalDefault()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.GlobalElement$Final> getFinalEffective()
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalElement> getSubstitutionGroup()
meth public abstract void setAbstract(java.lang.Boolean)
meth public abstract void setBlock(java.util.Set<org.netbeans.modules.xml.schema.model.Element$Block>)
meth public abstract void setFinal(java.util.Set<org.netbeans.modules.xml.schema.model.GlobalElement$Final>)
meth public abstract void setSubstitutionGroup(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalElement>)

CLSS public final static !enum org.netbeans.modules.xml.schema.model.GlobalElement$Final
 outer org.netbeans.modules.xml.schema.model.GlobalElement
fld public final static org.netbeans.modules.xml.schema.model.GlobalElement$Final ALL
fld public final static org.netbeans.modules.xml.schema.model.GlobalElement$Final EMPTY
fld public final static org.netbeans.modules.xml.schema.model.GlobalElement$Final EXTENSION
fld public final static org.netbeans.modules.xml.schema.model.GlobalElement$Final RESTRICTION
intf org.netbeans.modules.xml.schema.model.Derivation
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.schema.model.GlobalElement$Final valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.schema.model.GlobalElement$Final[] values()
supr java.lang.Enum<org.netbeans.modules.xml.schema.model.GlobalElement$Final>
hfds value

CLSS public abstract interface org.netbeans.modules.xml.schema.model.GlobalGroup
fld public final static java.lang.String DEFINITION_PROPERTY = "definition"
intf org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract org.netbeans.modules.xml.schema.model.LocalGroupDefinition getDefinition()
meth public abstract void setDefinition(org.netbeans.modules.xml.schema.model.LocalGroupDefinition)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.GlobalSimpleType
fld public final static java.lang.String FINAL_PROPERTY = "final"
innr public final static !enum Final
intf org.netbeans.modules.xml.schema.model.GlobalType
intf org.netbeans.modules.xml.schema.model.SimpleType
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.GlobalSimpleType$Final> getFinal()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.GlobalSimpleType$Final> getFinalDefault()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.GlobalSimpleType$Final> getFinalEffective()
meth public abstract void setFinal(java.util.Set<org.netbeans.modules.xml.schema.model.GlobalSimpleType$Final>)

CLSS public final static !enum org.netbeans.modules.xml.schema.model.GlobalSimpleType$Final
 outer org.netbeans.modules.xml.schema.model.GlobalSimpleType
fld public final static org.netbeans.modules.xml.schema.model.GlobalSimpleType$Final ALL
fld public final static org.netbeans.modules.xml.schema.model.GlobalSimpleType$Final EMPTY
fld public final static org.netbeans.modules.xml.schema.model.GlobalSimpleType$Final LIST
fld public final static org.netbeans.modules.xml.schema.model.GlobalSimpleType$Final RESTRICTION
fld public final static org.netbeans.modules.xml.schema.model.GlobalSimpleType$Final UNION
intf org.netbeans.modules.xml.schema.model.Derivation
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.schema.model.GlobalSimpleType$Final valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.schema.model.GlobalSimpleType$Final[] values()
supr java.lang.Enum<org.netbeans.modules.xml.schema.model.GlobalSimpleType$Final>
hfds value

CLSS public abstract interface org.netbeans.modules.xml.schema.model.GlobalType
intf org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent
intf org.netbeans.modules.xml.schema.model.SchemaComponent

CLSS public abstract interface org.netbeans.modules.xml.schema.model.GroupReference
fld public final static java.lang.String MAX_OCCURS_PROPERTY = "maxOccurs"
fld public final static java.lang.String MIN_OCCURS_PROPERTY = "minOccurs"
fld public final static java.lang.String REF_PROPERTY = "ref"
intf org.netbeans.modules.xml.schema.model.ComplexExtensionDefinition
intf org.netbeans.modules.xml.schema.model.ComplexTypeDefinition
intf org.netbeans.modules.xml.schema.model.SchemaComponent
intf org.netbeans.modules.xml.schema.model.SequenceDefinition
meth public abstract int getMinOccursDefault()
meth public abstract int getMinOccursEffective()
meth public abstract java.lang.Integer getMinOccurs()
meth public abstract java.lang.String getMaxOccurs()
meth public abstract java.lang.String getMaxOccursDefault()
meth public abstract java.lang.String getMaxOccursEffective()
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalGroup> getRef()
meth public abstract void setMaxOccurs(java.lang.String)
meth public abstract void setMinOccurs(java.lang.Integer)
meth public abstract void setRef(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalGroup>)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Import
fld public final static java.lang.String NAMESPACE_PROPERTY = "namespace"
intf org.netbeans.modules.xml.schema.model.SchemaModelReference
meth public abstract java.lang.String getNamespace()
meth public abstract void setNamespace(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Include
intf org.netbeans.modules.xml.schema.model.SchemaModelReference

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Key
intf org.netbeans.modules.xml.schema.model.Constraint
intf org.netbeans.modules.xml.schema.model.SchemaComponent

CLSS public abstract interface org.netbeans.modules.xml.schema.model.KeyRef
fld public final static java.lang.String REFERER_PROPERTY = "referer"
intf org.netbeans.modules.xml.schema.model.Constraint
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract org.netbeans.modules.xml.schema.model.Constraint getReferer()
meth public abstract void setReferer(org.netbeans.modules.xml.schema.model.Constraint)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Length
intf org.netbeans.modules.xml.schema.model.LengthFacet

CLSS public abstract interface org.netbeans.modules.xml.schema.model.LengthFacet
fld public final static java.lang.String FIXED_PROPERTY = "fixed"
fld public final static java.lang.String VALUE_PROPERTY = "value"
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract boolean getFixedDefault()
meth public abstract boolean getFixedEffective()
meth public abstract int getValue()
meth public abstract java.lang.Boolean isFixed()
meth public abstract void setFixed(java.lang.Boolean)
meth public abstract void setValue(int)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.List
fld public final static java.lang.String INLINE_TYPE_PROPERTY = "inlineType"
fld public final static java.lang.String TYPE_PROPERTY = "type"
intf org.netbeans.modules.xml.schema.model.SchemaComponent
intf org.netbeans.modules.xml.schema.model.SimpleTypeDefinition
meth public abstract org.netbeans.modules.xml.schema.model.LocalSimpleType getInlineType()
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalSimpleType> getType()
meth public abstract void setInlineType(org.netbeans.modules.xml.schema.model.LocalSimpleType)
meth public abstract void setType(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalSimpleType>)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.LocalAttribute
fld public final static java.lang.String FORM_PROPERTY = "form"
fld public final static java.lang.String REF_PROPERTY = "ref"
fld public final static java.lang.String USE_PROPERTY = "use"
intf org.netbeans.modules.xml.schema.model.Attribute
intf org.netbeans.modules.xml.schema.model.NameableSchemaComponent
intf org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent
meth public abstract org.netbeans.modules.xml.schema.model.Attribute$Use getUse()
meth public abstract org.netbeans.modules.xml.schema.model.Attribute$Use getUseDefault()
meth public abstract org.netbeans.modules.xml.schema.model.Attribute$Use getUseEffective()
meth public abstract org.netbeans.modules.xml.schema.model.Form getForm()
meth public abstract org.netbeans.modules.xml.schema.model.Form getFormDefault()
meth public abstract org.netbeans.modules.xml.schema.model.Form getFormEffective()
meth public abstract org.netbeans.modules.xml.schema.model.LocalSimpleType getInlineType()
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalSimpleType> getType()
meth public abstract void setForm(org.netbeans.modules.xml.schema.model.Form)
meth public abstract void setInlineType(org.netbeans.modules.xml.schema.model.LocalSimpleType)
meth public abstract void setType(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalSimpleType>)
meth public abstract void setUse(org.netbeans.modules.xml.schema.model.Attribute$Use)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.LocalAttributeContainer
fld public final static java.lang.String ANY_ATTRIBUTE_PROPERTY = "anyAttribute"
fld public final static java.lang.String ATTRIBUTE_GROUP_REFERENCE_PROPERTY = "attributeGroupReferences"
fld public final static java.lang.String LOCAL_ATTRIBUTE_PROPERTY = "localAttribute"
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.AttributeGroupReference> getAttributeGroupReferences()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.AttributeReference> getAttributeReferences()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.LocalAttribute> getLocalAttributes()
meth public abstract org.netbeans.modules.xml.schema.model.AnyAttribute getAnyAttribute()
meth public abstract void addAttributeGroupReference(org.netbeans.modules.xml.schema.model.AttributeGroupReference)
meth public abstract void addAttributeReference(org.netbeans.modules.xml.schema.model.AttributeReference)
meth public abstract void addLocalAttribute(org.netbeans.modules.xml.schema.model.LocalAttribute)
meth public abstract void removeAttributeGroupReference(org.netbeans.modules.xml.schema.model.AttributeGroupReference)
meth public abstract void removeAttributeReference(org.netbeans.modules.xml.schema.model.AttributeReference)
meth public abstract void removeLocalAttribute(org.netbeans.modules.xml.schema.model.LocalAttribute)
meth public abstract void setAnyAttribute(org.netbeans.modules.xml.schema.model.AnyAttribute)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.LocalComplexType
intf org.netbeans.modules.xml.schema.model.ComplexType
intf org.netbeans.modules.xml.schema.model.LocalType
intf org.netbeans.modules.xml.schema.model.SchemaComponent

CLSS public abstract interface org.netbeans.modules.xml.schema.model.LocalElement
fld public final static java.lang.String FORM_PROPERTY = "form"
fld public final static java.lang.String MAX_OCCURS_PROPERTY = "maxOccurs"
fld public final static java.lang.String MIN_OCCURS_PROPERTY = "minOccurs"
intf org.netbeans.modules.xml.schema.model.Element
intf org.netbeans.modules.xml.schema.model.NameableSchemaComponent
intf org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent
intf org.netbeans.modules.xml.schema.model.SchemaComponent
intf org.netbeans.modules.xml.schema.model.SequenceDefinition
intf org.netbeans.modules.xml.schema.model.TypeContainer
meth public abstract boolean allowsFullMultiplicity()
meth public abstract int getMinOccursDefault()
meth public abstract int getMinOccursEffective()
meth public abstract java.lang.Integer getMinOccurs()
meth public abstract java.lang.String getMaxOccurs()
meth public abstract java.lang.String getMaxOccursDefault()
meth public abstract java.lang.String getMaxOccursEffective()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.Element$Block> getBlock()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.Element$Block> getBlockDefault()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.Element$Block> getBlockEffective()
meth public abstract org.netbeans.modules.xml.schema.model.Form getForm()
meth public abstract org.netbeans.modules.xml.schema.model.Form getFormDefault()
meth public abstract org.netbeans.modules.xml.schema.model.Form getFormEffective()
meth public abstract void setBlock(java.util.Set<org.netbeans.modules.xml.schema.model.Element$Block>)
meth public abstract void setForm(org.netbeans.modules.xml.schema.model.Form)
meth public abstract void setMaxOccurs(java.lang.String)
meth public abstract void setMinOccurs(java.lang.Integer)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.LocalGroupDefinition
intf org.netbeans.modules.xml.schema.model.SchemaComponent

CLSS public abstract interface org.netbeans.modules.xml.schema.model.LocalSimpleType
intf org.netbeans.modules.xml.schema.model.LocalType
intf org.netbeans.modules.xml.schema.model.SchemaComponent
intf org.netbeans.modules.xml.schema.model.SimpleType

CLSS public abstract interface org.netbeans.modules.xml.schema.model.LocalType
intf org.netbeans.modules.xml.schema.model.SchemaComponent

CLSS public abstract interface org.netbeans.modules.xml.schema.model.MaxExclusive
intf org.netbeans.modules.xml.schema.model.BoundaryFacet

CLSS public abstract interface org.netbeans.modules.xml.schema.model.MaxInclusive
intf org.netbeans.modules.xml.schema.model.BoundaryFacet

CLSS public abstract interface org.netbeans.modules.xml.schema.model.MaxLength
intf org.netbeans.modules.xml.schema.model.LengthFacet

CLSS public abstract interface org.netbeans.modules.xml.schema.model.MinExclusive
intf org.netbeans.modules.xml.schema.model.BoundaryFacet

CLSS public abstract interface org.netbeans.modules.xml.schema.model.MinInclusive
intf org.netbeans.modules.xml.schema.model.BoundaryFacet

CLSS public abstract interface org.netbeans.modules.xml.schema.model.MinLength
intf org.netbeans.modules.xml.schema.model.LengthFacet

CLSS public abstract interface org.netbeans.modules.xml.schema.model.NameableSchemaComponent
intf org.netbeans.modules.xml.schema.model.SchemaComponent
intf org.netbeans.modules.xml.xam.Nameable<org.netbeans.modules.xml.schema.model.SchemaComponent>
meth public abstract org.netbeans.modules.xml.schema.model.SchemaModel getModel()

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Notation
fld public final static java.lang.String PUBLIC_PROPERTY = "public"
fld public final static java.lang.String SYSTEM_PROPERTY = "system"
intf org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract java.lang.String getPublicIdentifier()
meth public abstract java.lang.String getSystemIdentifier()
meth public abstract void setPublicIdentifier(java.lang.String)
meth public abstract void setSystemIdentifier(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Occur
innr public final static !enum ZeroOne

CLSS public final static !enum org.netbeans.modules.xml.schema.model.Occur$ZeroOne
 outer org.netbeans.modules.xml.schema.model.Occur
fld public final static org.netbeans.modules.xml.schema.model.Occur$ZeroOne ONE
fld public final static org.netbeans.modules.xml.schema.model.Occur$ZeroOne ZERO
intf org.netbeans.modules.xml.schema.model.Occur
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.schema.model.Occur$ZeroOne valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.schema.model.Occur$ZeroOne valueOfNumeric(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.xml.schema.model.Occur$ZeroOne[] values()
supr java.lang.Enum<org.netbeans.modules.xml.schema.model.Occur$ZeroOne>

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Pattern
fld public final static java.lang.String VALUE_PROPERTY = "value"
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract java.lang.String getValue()
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Redefine
fld public final static java.lang.String ATTRIBUTE_GROUP_PROPERTY = "attributeGroup"
fld public final static java.lang.String COMPLEX_TYPE_PROPERTY = "complexType"
fld public final static java.lang.String GROUP_DEFINITION_PROPERTY = "groupDefinition"
fld public final static java.lang.String SIMPLE_TYPE_PROPERTY = "simpleType"
intf org.netbeans.modules.xml.schema.model.SchemaModelReference
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.GlobalAttributeGroup> getAttributeGroups()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.GlobalComplexType> getComplexTypes()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.GlobalGroup> getGroupDefinitions()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.GlobalSimpleType> getSimpleTypes()
meth public abstract void addAttributeGroup(org.netbeans.modules.xml.schema.model.GlobalAttributeGroup)
meth public abstract void addComplexType(org.netbeans.modules.xml.schema.model.GlobalComplexType)
meth public abstract void addGroupDefinition(org.netbeans.modules.xml.schema.model.GlobalGroup)
meth public abstract void addSimpleType(org.netbeans.modules.xml.schema.model.GlobalSimpleType)
meth public abstract void removeAttributeGroup(org.netbeans.modules.xml.schema.model.GlobalAttributeGroup)
meth public abstract void removeComplexType(org.netbeans.modules.xml.schema.model.GlobalComplexType)
meth public abstract void removeGroupDefinition(org.netbeans.modules.xml.schema.model.GlobalGroup)
meth public abstract void removeSimpleType(org.netbeans.modules.xml.schema.model.GlobalSimpleType)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent
intf org.netbeans.modules.xml.schema.model.NameableSchemaComponent
intf org.netbeans.modules.xml.xam.NamedReferenceable<org.netbeans.modules.xml.schema.model.SchemaComponent>
meth public abstract org.netbeans.modules.xml.schema.model.SchemaModel getModel()

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Schema
fld public final static java.lang.String ATTRIBUTES_PROPERTY = "attributes"
fld public final static java.lang.String ATTRIBUTE_FORM_DEFAULT_PROPERTY = "attributeFormDefault"
fld public final static java.lang.String ATTRIBUTE_GROUPS_PROPERTY = "attributeGroups"
fld public final static java.lang.String BLOCK_DEFAULT_PROPERTY = "blockDefault"
fld public final static java.lang.String COMPLEX_TYPES_PROPERTY = "complexTypes"
fld public final static java.lang.String ELEMENTS_PROPERTY = "elements"
fld public final static java.lang.String ELEMENT_FORM_DEFAULT_PROPERTY = "elementFormDefault"
fld public final static java.lang.String FINAL_DEFAULT_PROPERTY = "finalDefault"
fld public final static java.lang.String GROUPS_PROPERTY = "groups"
fld public final static java.lang.String LANGUAGE_PROPERTY = "language"
fld public final static java.lang.String NOTATIONS_PROPERTY = "notations"
fld public final static java.lang.String SCHEMA_REFERENCES_PROPERTY = "schemaReferences"
fld public final static java.lang.String SIMPLE_TYPES_PROPERTY = "simpleTypes"
fld public final static java.lang.String TARGET_NAMESPACE_PROPERTY = "targetNamespace"
fld public final static java.lang.String VERSION_PROPERTY = "version"
innr public final static !enum Block
innr public final static !enum Final
intf org.netbeans.modules.xml.schema.model.SchemaComponent
intf org.netbeans.modules.xml.xam.EmbeddableRoot
meth public abstract java.lang.String getLanguage()
meth public abstract java.lang.String getTargetNamespace()
meth public abstract java.lang.String getVersion()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.GlobalAttribute> getAttributes()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.GlobalAttributeGroup> getAttributeGroups()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.GlobalComplexType> getComplexTypes()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.GlobalElement> findAllGlobalElements()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.GlobalElement> getElements()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.GlobalGroup> getGroups()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.GlobalSimpleType> getSimpleTypes()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.GlobalType> findAllGlobalTypes()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.Import> getImports()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.Include> getIncludes()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.Notation> getNotations()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.Redefine> getRedefines()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.SchemaModelReference> getSchemaReferences()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getPrefixes()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.Schema$Block> getBlockDefault()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.Schema$Block> getBlockDefaultDefault()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.Schema$Block> getBlockDefaultEffective()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.Schema$Final> getFinalDefault()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.Schema$Final> getFinalDefaultDefault()
meth public abstract java.util.Set<org.netbeans.modules.xml.schema.model.Schema$Final> getFinalDefaultEffective()
meth public abstract org.netbeans.modules.xml.schema.model.Form getAttributeFormDefault()
meth public abstract org.netbeans.modules.xml.schema.model.Form getAttributeFormDefaultDefault()
meth public abstract org.netbeans.modules.xml.schema.model.Form getAttributeFormDefaultEffective()
meth public abstract org.netbeans.modules.xml.schema.model.Form getElementFormDefault()
meth public abstract org.netbeans.modules.xml.schema.model.Form getElementFormDefaultDefault()
meth public abstract org.netbeans.modules.xml.schema.model.Form getElementFormDefaultEffective()
meth public abstract void addAttribute(org.netbeans.modules.xml.schema.model.GlobalAttribute)
meth public abstract void addAttributeGroup(org.netbeans.modules.xml.schema.model.GlobalAttributeGroup)
meth public abstract void addComplexType(org.netbeans.modules.xml.schema.model.GlobalComplexType)
meth public abstract void addElement(org.netbeans.modules.xml.schema.model.GlobalElement)
meth public abstract void addExternalReference(org.netbeans.modules.xml.schema.model.SchemaModelReference)
meth public abstract void addGroup(org.netbeans.modules.xml.schema.model.GlobalGroup)
meth public abstract void addNotation(org.netbeans.modules.xml.schema.model.Notation)
meth public abstract void addPrefix(java.lang.String,java.lang.String)
meth public abstract void addSimpleType(org.netbeans.modules.xml.schema.model.GlobalSimpleType)
meth public abstract void removeAttribute(org.netbeans.modules.xml.schema.model.GlobalAttribute)
meth public abstract void removeAttributeGroup(org.netbeans.modules.xml.schema.model.GlobalAttributeGroup)
meth public abstract void removeComplexType(org.netbeans.modules.xml.schema.model.GlobalComplexType)
meth public abstract void removeElement(org.netbeans.modules.xml.schema.model.GlobalElement)
meth public abstract void removeExternalReference(org.netbeans.modules.xml.schema.model.SchemaModelReference)
meth public abstract void removeGroup(org.netbeans.modules.xml.schema.model.GlobalGroup)
meth public abstract void removeNotation(org.netbeans.modules.xml.schema.model.Notation)
meth public abstract void removePrefix(java.lang.String)
meth public abstract void removeSimpleType(org.netbeans.modules.xml.schema.model.GlobalSimpleType)
meth public abstract void setAttributeFormDefault(org.netbeans.modules.xml.schema.model.Form)
meth public abstract void setBlockDefault(java.util.Set<org.netbeans.modules.xml.schema.model.Schema$Block>)
meth public abstract void setElementFormDefault(org.netbeans.modules.xml.schema.model.Form)
meth public abstract void setFinalDefault(java.util.Set<org.netbeans.modules.xml.schema.model.Schema$Final>)
meth public abstract void setLanguage(java.lang.String)
meth public abstract void setTargetNamespace(java.lang.String)
meth public abstract void setVersion(java.lang.String)

CLSS public final static !enum org.netbeans.modules.xml.schema.model.Schema$Block
 outer org.netbeans.modules.xml.schema.model.Schema
fld public final static org.netbeans.modules.xml.schema.model.Schema$Block ALL
fld public final static org.netbeans.modules.xml.schema.model.Schema$Block EMPTY
fld public final static org.netbeans.modules.xml.schema.model.Schema$Block EXTENSION
fld public final static org.netbeans.modules.xml.schema.model.Schema$Block RESTRICTION
fld public final static org.netbeans.modules.xml.schema.model.Schema$Block SUBSTITUTION
intf org.netbeans.modules.xml.schema.model.Derivation
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.schema.model.Schema$Block valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.schema.model.Schema$Block[] values()
supr java.lang.Enum<org.netbeans.modules.xml.schema.model.Schema$Block>
hfds value

CLSS public final static !enum org.netbeans.modules.xml.schema.model.Schema$Final
 outer org.netbeans.modules.xml.schema.model.Schema
fld public final static org.netbeans.modules.xml.schema.model.Schema$Final ALL
fld public final static org.netbeans.modules.xml.schema.model.Schema$Final EMPTY
fld public final static org.netbeans.modules.xml.schema.model.Schema$Final EXTENSION
fld public final static org.netbeans.modules.xml.schema.model.Schema$Final LIST
fld public final static org.netbeans.modules.xml.schema.model.Schema$Final RESTRICTION
fld public final static org.netbeans.modules.xml.schema.model.Schema$Final UNION
intf org.netbeans.modules.xml.schema.model.Derivation
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.schema.model.Schema$Final valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.schema.model.Schema$Final[] values()
supr java.lang.Enum<org.netbeans.modules.xml.schema.model.Schema$Final>
hfds value

CLSS public abstract interface org.netbeans.modules.xml.schema.model.SchemaComponent
fld public final static java.lang.String ANNOTATION_PROPERTY = "annotation"
fld public final static java.lang.String ID_PROPERTY = "id"
intf org.netbeans.modules.xml.xam.dom.DocumentComponent<org.netbeans.modules.xml.schema.model.SchemaComponent>
meth public abstract <%0 extends org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent> org.netbeans.modules.xml.xam.dom.NamedComponentReference<{%%0}> createReferenceTo({%%0},java.lang.Class<{%%0}>)
meth public abstract boolean fromSameModel(org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public abstract java.lang.Class<? extends org.netbeans.modules.xml.schema.model.SchemaComponent> getComponentType()
meth public abstract java.lang.String getAnyAttribute(javax.xml.namespace.QName)
meth public abstract java.lang.String getId()
meth public abstract org.netbeans.modules.xml.schema.model.Annotation getAnnotation()
meth public abstract org.netbeans.modules.xml.schema.model.SchemaModel getModel()
meth public abstract void accept(org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor)
meth public abstract void setAnnotation(org.netbeans.modules.xml.schema.model.Annotation)
meth public abstract void setAnyAttribute(javax.xml.namespace.QName,java.lang.String)
meth public abstract void setId(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.SchemaComponentFactory
intf org.netbeans.modules.xml.xam.dom.ComponentFactory<org.netbeans.modules.xml.schema.model.SchemaComponent>
meth public abstract <%0 extends org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent> org.netbeans.modules.xml.xam.dom.NamedComponentReference<{%%0}> createGlobalReference({%%0},java.lang.Class<{%%0}>,org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public abstract org.netbeans.modules.xml.schema.model.All createAll()
meth public abstract org.netbeans.modules.xml.schema.model.Annotation createAnnotation()
meth public abstract org.netbeans.modules.xml.schema.model.AnyAttribute createAnyAttribute()
meth public abstract org.netbeans.modules.xml.schema.model.AnyElement createAny()
meth public abstract org.netbeans.modules.xml.schema.model.AppInfo createAppInfo()
meth public abstract org.netbeans.modules.xml.schema.model.AttributeGroupReference createAttributeGroupReference()
meth public abstract org.netbeans.modules.xml.schema.model.AttributeReference createAttributeReference()
meth public abstract org.netbeans.modules.xml.schema.model.Choice createChoice()
meth public abstract org.netbeans.modules.xml.schema.model.ComplexContent createComplexContent()
meth public abstract org.netbeans.modules.xml.schema.model.ComplexContentRestriction createComplexContentRestriction()
meth public abstract org.netbeans.modules.xml.schema.model.ComplexExtension createComplexExtension()
meth public abstract org.netbeans.modules.xml.schema.model.Documentation createDocumentation()
meth public abstract org.netbeans.modules.xml.schema.model.ElementReference createElementReference()
meth public abstract org.netbeans.modules.xml.schema.model.Enumeration createEnumeration()
meth public abstract org.netbeans.modules.xml.schema.model.Field createField()
meth public abstract org.netbeans.modules.xml.schema.model.FractionDigits createFractionDigits()
meth public abstract org.netbeans.modules.xml.schema.model.GlobalAttribute createGlobalAttribute()
meth public abstract org.netbeans.modules.xml.schema.model.GlobalAttributeGroup createGlobalAttributeGroup()
meth public abstract org.netbeans.modules.xml.schema.model.GlobalComplexType createGlobalComplexType()
meth public abstract org.netbeans.modules.xml.schema.model.GlobalElement createGlobalElement()
meth public abstract org.netbeans.modules.xml.schema.model.GlobalGroup createGroupDefinition()
meth public abstract org.netbeans.modules.xml.schema.model.GlobalSimpleType createGlobalSimpleType()
meth public abstract org.netbeans.modules.xml.schema.model.GroupReference createGroupReference()
meth public abstract org.netbeans.modules.xml.schema.model.Import createImport()
meth public abstract org.netbeans.modules.xml.schema.model.Include createInclude()
meth public abstract org.netbeans.modules.xml.schema.model.Key createKey()
meth public abstract org.netbeans.modules.xml.schema.model.KeyRef createKeyRef()
meth public abstract org.netbeans.modules.xml.schema.model.Length createLength()
meth public abstract org.netbeans.modules.xml.schema.model.List createList()
meth public abstract org.netbeans.modules.xml.schema.model.LocalAttribute createLocalAttribute()
meth public abstract org.netbeans.modules.xml.schema.model.LocalComplexType createLocalComplexType()
meth public abstract org.netbeans.modules.xml.schema.model.LocalElement createLocalElement()
meth public abstract org.netbeans.modules.xml.schema.model.LocalSimpleType createLocalSimpleType()
meth public abstract org.netbeans.modules.xml.schema.model.MaxExclusive createMaxExclusive()
meth public abstract org.netbeans.modules.xml.schema.model.MaxInclusive createMaxInclusive()
meth public abstract org.netbeans.modules.xml.schema.model.MaxLength createMaxLength()
meth public abstract org.netbeans.modules.xml.schema.model.MinExclusive createMinExclusive()
meth public abstract org.netbeans.modules.xml.schema.model.MinInclusive createMinInclusive()
meth public abstract org.netbeans.modules.xml.schema.model.MinLength createMinLength()
meth public abstract org.netbeans.modules.xml.schema.model.Notation createNotation()
meth public abstract org.netbeans.modules.xml.schema.model.Pattern createPattern()
meth public abstract org.netbeans.modules.xml.schema.model.Redefine createRedefine()
meth public abstract org.netbeans.modules.xml.schema.model.Schema createSchema()
meth public abstract org.netbeans.modules.xml.schema.model.Selector createSelector()
meth public abstract org.netbeans.modules.xml.schema.model.Sequence createSequence()
meth public abstract org.netbeans.modules.xml.schema.model.SimpleContent createSimpleContent()
meth public abstract org.netbeans.modules.xml.schema.model.SimpleContentRestriction createSimpleContentRestriction()
meth public abstract org.netbeans.modules.xml.schema.model.SimpleExtension createSimpleExtension()
meth public abstract org.netbeans.modules.xml.schema.model.SimpleTypeRestriction createSimpleTypeRestriction()
meth public abstract org.netbeans.modules.xml.schema.model.TotalDigits createTotalDigits()
meth public abstract org.netbeans.modules.xml.schema.model.Union createUnion()
meth public abstract org.netbeans.modules.xml.schema.model.Unique createUnique()
meth public abstract org.netbeans.modules.xml.schema.model.Whitespace createWhitespace()

CLSS public org.netbeans.modules.xml.schema.model.SchemaComponentReference<%0 extends org.netbeans.modules.xml.schema.model.SchemaComponent>
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public static <%0 extends org.netbeans.modules.xml.schema.model.SchemaComponent> org.netbeans.modules.xml.schema.model.SchemaComponentReference<{%%0}> create({%%0})
meth public {org.netbeans.modules.xml.schema.model.SchemaComponentReference%0} get()
supr java.lang.Object
hfds component

CLSS public abstract interface org.netbeans.modules.xml.schema.model.SchemaModel
intf org.netbeans.modules.xml.xam.Referenceable
intf org.netbeans.modules.xml.xam.dom.DocumentModel<org.netbeans.modules.xml.schema.model.SchemaComponent>
meth public abstract <%0 extends org.netbeans.modules.xml.xam.NamedReferenceable> {%%0} findByNameAndType(java.lang.String,java.lang.Class<{%%0}>)
meth public abstract <%0 extends org.netbeans.modules.xml.xam.NamedReferenceable> {%%0} resolve(java.lang.String,java.lang.String,java.lang.Class<{%%0}>)
meth public abstract boolean isEmbedded()
meth public abstract java.lang.String getEffectiveNamespace(org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.Schema> findSchemas(java.lang.String)
meth public abstract org.netbeans.modules.xml.schema.model.Schema getSchema()
meth public abstract org.netbeans.modules.xml.schema.model.SchemaComponentFactory getFactory()

CLSS public org.netbeans.modules.xml.schema.model.SchemaModelFactory
meth protected org.netbeans.modules.xml.schema.model.SchemaModel createModel(org.netbeans.modules.xml.xam.ModelSource)
meth public org.netbeans.modules.xml.schema.model.SchemaModel createEmbeddedSchemaModel(org.netbeans.modules.xml.xam.dom.DocumentModel,org.w3c.dom.Element)
meth public org.netbeans.modules.xml.schema.model.SchemaModel getModel(org.netbeans.modules.xml.xam.ModelSource)
meth public org.netbeans.modules.xml.schema.model.SchemaModel getPrimitiveTypesModel()
meth public static org.netbeans.modules.xml.schema.model.SchemaModelFactory getDefault()
supr org.netbeans.modules.xml.xam.AbstractModelFactory<org.netbeans.modules.xml.schema.model.SchemaModel>
hfds primitiveTypesSchema,schemaModelFactory

CLSS public abstract interface org.netbeans.modules.xml.schema.model.SchemaModelReference
fld public final static java.lang.String SCHEMA_LOCATION_PROPERTY = "schemaLocation"
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract java.lang.String getSchemaLocation()
meth public abstract org.netbeans.modules.xml.schema.model.SchemaModel resolveReferencedModel() throws org.netbeans.modules.xml.xam.locator.CatalogModelException
meth public abstract void setSchemaLocation(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Selector
fld public final static java.lang.String XPATH_PROPERTY = "xPath"
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract java.lang.String getXPath()
meth public abstract void setXPath(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Sequence
fld public final static java.lang.String CONTENT_PROPERTY = "content"
fld public final static java.lang.String MAX_OCCURS_PROPERTY = "maxOccurs"
fld public final static java.lang.String MIN_OCCURS_PROPERTY = "minOccurs"
intf org.netbeans.modules.xml.schema.model.ComplexExtensionDefinition
intf org.netbeans.modules.xml.schema.model.ComplexTypeDefinition
intf org.netbeans.modules.xml.schema.model.LocalGroupDefinition
intf org.netbeans.modules.xml.schema.model.SchemaComponent
intf org.netbeans.modules.xml.schema.model.SequenceDefinition
meth public abstract java.util.List<org.netbeans.modules.xml.schema.model.SequenceDefinition> getContent()
meth public abstract org.netbeans.modules.xml.schema.model.Cardinality getCardinality()
meth public abstract void addContent(org.netbeans.modules.xml.schema.model.SequenceDefinition,int)
meth public abstract void appendContent(org.netbeans.modules.xml.schema.model.SequenceDefinition)
meth public abstract void removeContent(org.netbeans.modules.xml.schema.model.SequenceDefinition)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.SequenceDefinition
intf org.netbeans.modules.xml.schema.model.SchemaComponent

CLSS public abstract interface org.netbeans.modules.xml.schema.model.SimpleContent
fld public final static java.lang.String LOCAL_DEFINITION_PROPERTY = "restriction"
intf org.netbeans.modules.xml.schema.model.ComplexTypeDefinition
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract org.netbeans.modules.xml.schema.model.SimpleContentDefinition getLocalDefinition()
meth public abstract void setLocalDefinition(org.netbeans.modules.xml.schema.model.SimpleContentDefinition)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.SimpleContentDefinition
intf org.netbeans.modules.xml.schema.model.SchemaComponent

CLSS public abstract interface org.netbeans.modules.xml.schema.model.SimpleContentRestriction
intf org.netbeans.modules.xml.schema.model.LocalAttributeContainer
intf org.netbeans.modules.xml.schema.model.SimpleContentDefinition
intf org.netbeans.modules.xml.schema.model.SimpleRestriction
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalType> getBase()
meth public abstract void setBase(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalType>)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.SimpleExtension
intf org.netbeans.modules.xml.schema.model.Extension
intf org.netbeans.modules.xml.schema.model.SchemaComponent
intf org.netbeans.modules.xml.schema.model.SimpleContentDefinition

CLSS public abstract interface org.netbeans.modules.xml.schema.model.SimpleRestriction
fld public final static java.lang.String BASE_PROPERTY = "base"
fld public final static java.lang.String ENUMERATION_PROPERTY = "enumerations"
fld public final static java.lang.String FRACTION_DIGITS_PROPERTY = "fractionDigits"
fld public final static java.lang.String INLINETYPE_PROPERTY = "inlinetype"
fld public final static java.lang.String LENGTH_PROPERTY = "lengths"
fld public final static java.lang.String MAX_EXCLUSIVE_PROPERTY = "maxExclusives"
fld public final static java.lang.String MAX_INCLUSIVE_PROPERTY = "maxInclusives"
fld public final static java.lang.String MAX_LENGTH_PROPERTY = "maxLengths"
fld public final static java.lang.String MIN_EXCLUSIVE_PROPERTY = "minExclusives"
fld public final static java.lang.String MIN_INCLUSIVE_PROPERTY = "minInclusives"
fld public final static java.lang.String MIN_LENGTH_PROPERTY = "minLengths"
fld public final static java.lang.String PATTERN_PROPERTY = "patterns"
fld public final static java.lang.String TOTAL_DIGITS_PROPERTY = "totalDigits"
fld public final static java.lang.String WHITESPACE_PROPERTY = "whitespaces"
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.Enumeration> getEnumerations()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.FractionDigits> getFractionDigits()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.Length> getLengths()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.MaxExclusive> getMaxExclusives()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.MaxInclusive> getMaxInclusives()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.MaxLength> getMaxLengths()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.MinExclusive> getMinExclusives()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.MinInclusive> getMinInclusives()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.MinLength> getMinLengths()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.Pattern> getPatterns()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.TotalDigits> getTotalDigits()
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.Whitespace> getWhitespaces()
meth public abstract org.netbeans.modules.xml.schema.model.LocalSimpleType getInlineType()
meth public abstract void addEnumeration(org.netbeans.modules.xml.schema.model.Enumeration)
meth public abstract void addFractionDigits(org.netbeans.modules.xml.schema.model.FractionDigits)
meth public abstract void addLength(org.netbeans.modules.xml.schema.model.Length)
meth public abstract void addMaxExclusive(org.netbeans.modules.xml.schema.model.MaxExclusive)
meth public abstract void addMaxInclusive(org.netbeans.modules.xml.schema.model.MaxInclusive)
meth public abstract void addMaxLength(org.netbeans.modules.xml.schema.model.MaxLength)
meth public abstract void addMinExclusive(org.netbeans.modules.xml.schema.model.MinExclusive)
meth public abstract void addMinInclusive(org.netbeans.modules.xml.schema.model.MinInclusive)
meth public abstract void addMinLength(org.netbeans.modules.xml.schema.model.MinLength)
meth public abstract void addPattern(org.netbeans.modules.xml.schema.model.Pattern)
meth public abstract void addTotalDigit(org.netbeans.modules.xml.schema.model.TotalDigits)
meth public abstract void addWhitespace(org.netbeans.modules.xml.schema.model.Whitespace)
meth public abstract void removeEnumeration(org.netbeans.modules.xml.schema.model.Enumeration)
meth public abstract void removeFractionDigits(org.netbeans.modules.xml.schema.model.FractionDigits)
meth public abstract void removeLength(org.netbeans.modules.xml.schema.model.Length)
meth public abstract void removeMaxExclusive(org.netbeans.modules.xml.schema.model.MaxExclusive)
meth public abstract void removeMaxInclusive(org.netbeans.modules.xml.schema.model.MaxInclusive)
meth public abstract void removeMaxLength(org.netbeans.modules.xml.schema.model.MaxLength)
meth public abstract void removeMinExclusive(org.netbeans.modules.xml.schema.model.MinExclusive)
meth public abstract void removeMinInclusive(org.netbeans.modules.xml.schema.model.MinInclusive)
meth public abstract void removeMinLength(org.netbeans.modules.xml.schema.model.MinLength)
meth public abstract void removePattern(org.netbeans.modules.xml.schema.model.Pattern)
meth public abstract void removeTotalDigit(org.netbeans.modules.xml.schema.model.TotalDigits)
meth public abstract void removeWhitespace(org.netbeans.modules.xml.schema.model.Whitespace)
meth public abstract void setInlineType(org.netbeans.modules.xml.schema.model.LocalSimpleType)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.SimpleType
fld public final static java.lang.String DEFINITION_PROPERTY = "definition"
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract org.netbeans.modules.xml.schema.model.SimpleTypeDefinition getDefinition()
meth public abstract void setDefinition(org.netbeans.modules.xml.schema.model.SimpleTypeDefinition)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.SimpleTypeDefinition
intf org.netbeans.modules.xml.schema.model.SchemaComponent

CLSS public abstract interface org.netbeans.modules.xml.schema.model.SimpleTypeRestriction
intf org.netbeans.modules.xml.schema.model.SchemaComponent
intf org.netbeans.modules.xml.schema.model.SimpleRestriction
intf org.netbeans.modules.xml.schema.model.SimpleTypeDefinition
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalSimpleType> getBase()
meth public abstract void setBase(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalSimpleType>)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.TotalDigits
intf org.netbeans.modules.xml.schema.model.LengthFacet

CLSS public abstract interface org.netbeans.modules.xml.schema.model.TypeContainer
fld public final static java.lang.String INLINE_TYPE_PROPERTY = "inlineType"
fld public final static java.lang.String TYPE_PROPERTY = "type"
meth public abstract org.netbeans.modules.xml.schema.model.LocalType getInlineType()
meth public abstract org.netbeans.modules.xml.xam.dom.NamedComponentReference<? extends org.netbeans.modules.xml.schema.model.GlobalType> getType()
meth public abstract void setInlineType(org.netbeans.modules.xml.schema.model.LocalType)
meth public abstract void setType(org.netbeans.modules.xml.xam.dom.NamedComponentReference<? extends org.netbeans.modules.xml.schema.model.GlobalType>)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Union
fld public final static java.lang.String INLINE_TYPE_PROPERTY = "inline_type"
fld public final static java.lang.String MEMBER_TYPES_PROPERTY = "memberTypes"
intf org.netbeans.modules.xml.schema.model.SchemaComponent
intf org.netbeans.modules.xml.schema.model.SimpleTypeDefinition
meth public abstract java.util.Collection<org.netbeans.modules.xml.schema.model.LocalSimpleType> getInlineTypes()
meth public abstract java.util.List<org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalSimpleType>> getMemberTypes()
meth public abstract void addInlineType(org.netbeans.modules.xml.schema.model.LocalSimpleType)
meth public abstract void addMemberType(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalSimpleType>)
meth public abstract void removeInlineType(org.netbeans.modules.xml.schema.model.LocalSimpleType)
meth public abstract void removeMemberType(org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalSimpleType>)
meth public abstract void setMemberTypes(java.util.List<org.netbeans.modules.xml.xam.dom.NamedComponentReference<org.netbeans.modules.xml.schema.model.GlobalSimpleType>>)

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Unique
intf org.netbeans.modules.xml.schema.model.Constraint
intf org.netbeans.modules.xml.schema.model.SchemaComponent

CLSS public abstract interface org.netbeans.modules.xml.schema.model.Whitespace
fld public final static java.lang.String FIXED_PROPERTY = "fixed"
fld public final static java.lang.String VALUE_PROPERTY = "value"
innr public final static !enum Treatment
intf org.netbeans.modules.xml.schema.model.SchemaComponent
meth public abstract boolean getFixedDefault()
meth public abstract boolean getFixedEffective()
meth public abstract java.lang.Boolean isFixed()
meth public abstract org.netbeans.modules.xml.schema.model.Whitespace$Treatment getValue()
meth public abstract void setFixed(java.lang.Boolean)
meth public abstract void setValue(org.netbeans.modules.xml.schema.model.Whitespace$Treatment)

CLSS public final static !enum org.netbeans.modules.xml.schema.model.Whitespace$Treatment
 outer org.netbeans.modules.xml.schema.model.Whitespace
fld public final static org.netbeans.modules.xml.schema.model.Whitespace$Treatment COLLAPSE
fld public final static org.netbeans.modules.xml.schema.model.Whitespace$Treatment PRESERVE
fld public final static org.netbeans.modules.xml.schema.model.Whitespace$Treatment REPLACE
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.schema.model.Whitespace$Treatment valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.schema.model.Whitespace$Treatment[] values()
supr java.lang.Enum<org.netbeans.modules.xml.schema.model.Whitespace$Treatment>
hfds value

CLSS public org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor
cons public init()
intf org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor
meth protected void visitChildren(org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public void visit(org.netbeans.modules.xml.schema.model.All)
meth public void visit(org.netbeans.modules.xml.schema.model.Annotation)
meth public void visit(org.netbeans.modules.xml.schema.model.AnyAttribute)
meth public void visit(org.netbeans.modules.xml.schema.model.AnyElement)
meth public void visit(org.netbeans.modules.xml.schema.model.AppInfo)
meth public void visit(org.netbeans.modules.xml.schema.model.AttributeGroupReference)
meth public void visit(org.netbeans.modules.xml.schema.model.AttributeReference)
meth public void visit(org.netbeans.modules.xml.schema.model.Choice)
meth public void visit(org.netbeans.modules.xml.schema.model.ComplexContent)
meth public void visit(org.netbeans.modules.xml.schema.model.ComplexContentRestriction)
meth public void visit(org.netbeans.modules.xml.schema.model.ComplexExtension)
meth public void visit(org.netbeans.modules.xml.schema.model.Documentation)
meth public void visit(org.netbeans.modules.xml.schema.model.ElementReference)
meth public void visit(org.netbeans.modules.xml.schema.model.Enumeration)
meth public void visit(org.netbeans.modules.xml.schema.model.Field)
meth public void visit(org.netbeans.modules.xml.schema.model.FractionDigits)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalAttribute)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalAttributeGroup)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalComplexType)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalElement)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalGroup)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalSimpleType)
meth public void visit(org.netbeans.modules.xml.schema.model.GroupReference)
meth public void visit(org.netbeans.modules.xml.schema.model.Import)
meth public void visit(org.netbeans.modules.xml.schema.model.Include)
meth public void visit(org.netbeans.modules.xml.schema.model.Key)
meth public void visit(org.netbeans.modules.xml.schema.model.KeyRef)
meth public void visit(org.netbeans.modules.xml.schema.model.Length)
meth public void visit(org.netbeans.modules.xml.schema.model.List)
meth public void visit(org.netbeans.modules.xml.schema.model.LocalAttribute)
meth public void visit(org.netbeans.modules.xml.schema.model.LocalComplexType)
meth public void visit(org.netbeans.modules.xml.schema.model.LocalElement)
meth public void visit(org.netbeans.modules.xml.schema.model.LocalSimpleType)
meth public void visit(org.netbeans.modules.xml.schema.model.MaxExclusive)
meth public void visit(org.netbeans.modules.xml.schema.model.MaxInclusive)
meth public void visit(org.netbeans.modules.xml.schema.model.MaxLength)
meth public void visit(org.netbeans.modules.xml.schema.model.MinExclusive)
meth public void visit(org.netbeans.modules.xml.schema.model.MinInclusive)
meth public void visit(org.netbeans.modules.xml.schema.model.MinLength)
meth public void visit(org.netbeans.modules.xml.schema.model.Notation)
meth public void visit(org.netbeans.modules.xml.schema.model.Pattern)
meth public void visit(org.netbeans.modules.xml.schema.model.Redefine)
meth public void visit(org.netbeans.modules.xml.schema.model.Schema)
meth public void visit(org.netbeans.modules.xml.schema.model.Selector)
meth public void visit(org.netbeans.modules.xml.schema.model.Sequence)
meth public void visit(org.netbeans.modules.xml.schema.model.SimpleContent)
meth public void visit(org.netbeans.modules.xml.schema.model.SimpleContentRestriction)
meth public void visit(org.netbeans.modules.xml.schema.model.SimpleExtension)
meth public void visit(org.netbeans.modules.xml.schema.model.SimpleTypeRestriction)
meth public void visit(org.netbeans.modules.xml.schema.model.TotalDigits)
meth public void visit(org.netbeans.modules.xml.schema.model.Union)
meth public void visit(org.netbeans.modules.xml.schema.model.Unique)
meth public void visit(org.netbeans.modules.xml.schema.model.Whitespace)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor
cons public init()
intf org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor
meth public void visit(org.netbeans.modules.xml.schema.model.All)
meth public void visit(org.netbeans.modules.xml.schema.model.Annotation)
meth public void visit(org.netbeans.modules.xml.schema.model.AnyAttribute)
meth public void visit(org.netbeans.modules.xml.schema.model.AnyElement)
meth public void visit(org.netbeans.modules.xml.schema.model.AppInfo)
meth public void visit(org.netbeans.modules.xml.schema.model.AttributeGroupReference)
meth public void visit(org.netbeans.modules.xml.schema.model.AttributeReference)
meth public void visit(org.netbeans.modules.xml.schema.model.Choice)
meth public void visit(org.netbeans.modules.xml.schema.model.ComplexContent)
meth public void visit(org.netbeans.modules.xml.schema.model.ComplexContentRestriction)
meth public void visit(org.netbeans.modules.xml.schema.model.ComplexExtension)
meth public void visit(org.netbeans.modules.xml.schema.model.Documentation)
meth public void visit(org.netbeans.modules.xml.schema.model.ElementReference)
meth public void visit(org.netbeans.modules.xml.schema.model.Enumeration)
meth public void visit(org.netbeans.modules.xml.schema.model.Field)
meth public void visit(org.netbeans.modules.xml.schema.model.FractionDigits)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalAttribute)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalAttributeGroup)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalComplexType)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalElement)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalGroup)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalSimpleType)
meth public void visit(org.netbeans.modules.xml.schema.model.GroupReference)
meth public void visit(org.netbeans.modules.xml.schema.model.Import)
meth public void visit(org.netbeans.modules.xml.schema.model.Include)
meth public void visit(org.netbeans.modules.xml.schema.model.Key)
meth public void visit(org.netbeans.modules.xml.schema.model.KeyRef)
meth public void visit(org.netbeans.modules.xml.schema.model.Length)
meth public void visit(org.netbeans.modules.xml.schema.model.List)
meth public void visit(org.netbeans.modules.xml.schema.model.LocalAttribute)
meth public void visit(org.netbeans.modules.xml.schema.model.LocalComplexType)
meth public void visit(org.netbeans.modules.xml.schema.model.LocalElement)
meth public void visit(org.netbeans.modules.xml.schema.model.LocalSimpleType)
meth public void visit(org.netbeans.modules.xml.schema.model.MaxExclusive)
meth public void visit(org.netbeans.modules.xml.schema.model.MaxInclusive)
meth public void visit(org.netbeans.modules.xml.schema.model.MaxLength)
meth public void visit(org.netbeans.modules.xml.schema.model.MinExclusive)
meth public void visit(org.netbeans.modules.xml.schema.model.MinInclusive)
meth public void visit(org.netbeans.modules.xml.schema.model.MinLength)
meth public void visit(org.netbeans.modules.xml.schema.model.Notation)
meth public void visit(org.netbeans.modules.xml.schema.model.Pattern)
meth public void visit(org.netbeans.modules.xml.schema.model.Redefine)
meth public void visit(org.netbeans.modules.xml.schema.model.Schema)
meth public void visit(org.netbeans.modules.xml.schema.model.Selector)
meth public void visit(org.netbeans.modules.xml.schema.model.Sequence)
meth public void visit(org.netbeans.modules.xml.schema.model.SimpleContent)
meth public void visit(org.netbeans.modules.xml.schema.model.SimpleContentRestriction)
meth public void visit(org.netbeans.modules.xml.schema.model.SimpleExtension)
meth public void visit(org.netbeans.modules.xml.schema.model.SimpleTypeRestriction)
meth public void visit(org.netbeans.modules.xml.schema.model.TotalDigits)
meth public void visit(org.netbeans.modules.xml.schema.model.Union)
meth public void visit(org.netbeans.modules.xml.schema.model.Unique)
meth public void visit(org.netbeans.modules.xml.schema.model.Whitespace)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.schema.model.visitor.FindGlobalReferenceVisitor<%0 extends org.netbeans.modules.xml.xam.NamedReferenceable>
cons public init()
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalAttribute)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalAttributeGroup)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalComplexType)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalElement)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalGroup)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalSimpleType)
meth public void visit(org.netbeans.modules.xml.schema.model.Notation)
meth public void visit(org.netbeans.modules.xml.schema.model.Schema)
meth public {org.netbeans.modules.xml.schema.model.visitor.FindGlobalReferenceVisitor%0} find(java.lang.Class<{org.netbeans.modules.xml.schema.model.visitor.FindGlobalReferenceVisitor%0}>,java.lang.String,org.netbeans.modules.xml.schema.model.Schema)
supr org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor
hfds elementType,found,localName,refType,schema

CLSS public org.netbeans.modules.xml.schema.model.visitor.FindReferredConstraintVisitor
cons public init()
meth protected void visitChildren(org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public org.netbeans.modules.xml.schema.model.Constraint findReferredConstraint(org.netbeans.modules.xml.schema.model.SchemaComponent,java.lang.String)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalElement)
meth public void visit(org.netbeans.modules.xml.schema.model.LocalElement)
supr org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor
hfds constraint,found,name

CLSS public org.netbeans.modules.xml.schema.model.visitor.FindSchemaComponentFromDOM
cons public init()
meth protected void visitChildren(org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public java.lang.String getXPathForComponent(org.netbeans.modules.xml.schema.model.SchemaComponent,org.netbeans.modules.xml.schema.model.SchemaComponent)
meth public org.netbeans.modules.xml.schema.model.SchemaComponent findComponent(org.netbeans.modules.xml.schema.model.SchemaComponent,java.lang.String)
meth public org.netbeans.modules.xml.schema.model.SchemaComponent findComponent(org.netbeans.modules.xml.schema.model.SchemaComponent,org.w3c.dom.Element)
meth public static <%0 extends org.netbeans.modules.xml.schema.model.SchemaComponent> {%%0} find(java.lang.Class<{%%0}>,org.netbeans.modules.xml.schema.model.SchemaComponent,java.lang.String)
supr org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor
hfds result,xmlNode

CLSS public final org.netbeans.modules.xml.schema.model.visitor.FindSubstitutions
meth public static java.util.Set<org.netbeans.modules.xml.schema.model.GlobalElement> resolveSubstitutions(org.netbeans.modules.xml.schema.model.SchemaModel,java.lang.String,java.lang.String)
supr java.lang.Object
hcls Visitor

CLSS public org.netbeans.modules.xml.schema.model.visitor.FindUsageVisitor
cons public init()
meth public org.netbeans.modules.xml.schema.model.visitor.Preview findUsages(java.util.Collection<org.netbeans.modules.xml.schema.model.Schema>,org.netbeans.modules.xml.xam.NamedReferenceable<org.netbeans.modules.xml.schema.model.SchemaComponent>)
meth public void visit(org.netbeans.modules.xml.schema.model.AttributeGroupReference)
meth public void visit(org.netbeans.modules.xml.schema.model.AttributeReference)
meth public void visit(org.netbeans.modules.xml.schema.model.ComplexContentRestriction)
meth public void visit(org.netbeans.modules.xml.schema.model.ComplexExtension)
meth public void visit(org.netbeans.modules.xml.schema.model.ElementReference)
meth public void visit(org.netbeans.modules.xml.schema.model.GlobalElement)
meth public void visit(org.netbeans.modules.xml.schema.model.GroupReference)
meth public void visit(org.netbeans.modules.xml.schema.model.List)
meth public void visit(org.netbeans.modules.xml.schema.model.LocalAttribute)
meth public void visit(org.netbeans.modules.xml.schema.model.LocalElement)
meth public void visit(org.netbeans.modules.xml.schema.model.SimpleExtension)
meth public void visit(org.netbeans.modules.xml.schema.model.SimpleTypeRestriction)
meth public void visit(org.netbeans.modules.xml.schema.model.Union)
supr org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor
hfds globalSchemaComponent,preview

CLSS public abstract interface org.netbeans.modules.xml.schema.model.visitor.Preview
meth public abstract java.util.Map<org.netbeans.modules.xml.schema.model.SchemaComponent,java.util.List<org.netbeans.modules.xml.schema.model.SchemaComponent>> getUsages()

CLSS public org.netbeans.modules.xml.schema.model.visitor.PreviewImpl
cons public init()
intf org.netbeans.modules.xml.schema.model.visitor.Preview
meth public java.util.Map<org.netbeans.modules.xml.schema.model.SchemaComponent,java.util.List<org.netbeans.modules.xml.schema.model.SchemaComponent>> getUsages()
supr java.lang.Object
hfds usages

CLSS public abstract interface org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor
meth public abstract void visit(org.netbeans.modules.xml.schema.model.All)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Annotation)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.AnyAttribute)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.AnyElement)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.AppInfo)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.AttributeGroupReference)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.AttributeReference)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Choice)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.ComplexContent)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.ComplexContentRestriction)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.ComplexExtension)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Documentation)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.ElementReference)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Enumeration)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Field)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.FractionDigits)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.GlobalAttribute)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.GlobalAttributeGroup)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.GlobalComplexType)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.GlobalElement)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.GlobalGroup)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.GlobalSimpleType)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.GroupReference)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Import)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Include)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Key)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.KeyRef)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Length)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.List)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.LocalAttribute)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.LocalComplexType)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.LocalElement)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.LocalSimpleType)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.MaxExclusive)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.MaxInclusive)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.MaxLength)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.MinExclusive)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.MinInclusive)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.MinLength)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Notation)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Pattern)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Redefine)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Schema)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Selector)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Sequence)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.SimpleContent)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.SimpleContentRestriction)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.SimpleExtension)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.SimpleTypeRestriction)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.TotalDigits)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Union)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Unique)
meth public abstract void visit(org.netbeans.modules.xml.schema.model.Whitespace)

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

CLSS public abstract interface org.netbeans.modules.xml.xam.EmbeddableRoot
innr public abstract interface static ForeignParent
meth public abstract org.netbeans.modules.xml.xam.Component getForeignParent()
meth public abstract void setForeignParent(org.netbeans.modules.xml.xam.Component)

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

CLSS public abstract interface org.netbeans.modules.xml.xam.Nameable<%0 extends org.netbeans.modules.xml.xam.Component>
intf org.netbeans.modules.xml.xam.Named<{org.netbeans.modules.xml.xam.Nameable%0}>
meth public abstract void setName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.xml.xam.Named<%0 extends org.netbeans.modules.xml.xam.Component>
fld public final static java.lang.String NAME_PROPERTY = "name"
intf org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.Named%0}>
meth public abstract java.lang.String getName()

CLSS public abstract interface org.netbeans.modules.xml.xam.NamedReferenceable<%0 extends org.netbeans.modules.xml.xam.Component>
intf org.netbeans.modules.xml.xam.Named<{org.netbeans.modules.xml.xam.NamedReferenceable%0}>
intf org.netbeans.modules.xml.xam.Referenceable

CLSS public abstract interface org.netbeans.modules.xml.xam.Referenceable

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.ComponentFactory<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.ComponentFactory%0}>>
meth public abstract {org.netbeans.modules.xml.xam.dom.ComponentFactory%0} create(org.w3c.dom.Element,{org.netbeans.modules.xml.xam.dom.ComponentFactory%0})

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.DocumentComponent<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent>
fld public final static java.lang.String TEXT_CONTENT_PROPERTY = "textContent"
intf org.netbeans.modules.xml.xam.Component<{org.netbeans.modules.xml.xam.dom.DocumentComponent%0}>
meth public abstract boolean isInDocumentModel()
meth public abstract boolean referencesSameNode(org.w3c.dom.Node)
meth public abstract int findAttributePosition(java.lang.String)
meth public abstract int findPosition()
meth public abstract java.lang.String getAttribute(org.netbeans.modules.xml.xam.dom.Attribute)
meth public abstract org.w3c.dom.Element getPeer()
meth public abstract void setAttribute(java.lang.String,org.netbeans.modules.xml.xam.dom.Attribute,java.lang.Object)
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentComponent%0} findChildComponent(org.w3c.dom.Element)

CLSS public abstract interface org.netbeans.modules.xml.xam.dom.DocumentModel<%0 extends org.netbeans.modules.xml.xam.dom.DocumentComponent<{org.netbeans.modules.xml.xam.dom.DocumentModel%0}>>
intf org.netbeans.modules.xml.xam.Model<{org.netbeans.modules.xml.xam.dom.DocumentModel%0}>
meth public abstract boolean areSameNodes(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract java.lang.String getXPathExpression(org.netbeans.modules.xml.xam.dom.DocumentComponent)
meth public abstract org.netbeans.modules.xml.xam.dom.DocumentComponent findComponent(int)
meth public abstract org.w3c.dom.Document getDocument()
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentModel%0} createComponent({org.netbeans.modules.xml.xam.dom.DocumentModel%0},org.w3c.dom.Element)
meth public abstract {org.netbeans.modules.xml.xam.dom.DocumentModel%0} getRootComponent()

