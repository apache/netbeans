#Signature file v4.1
#Version 1.54

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface org.netbeans.modules.css.live.LiveUpdater
 anno 0 java.lang.Deprecated()
meth public abstract boolean update(javax.swing.text.Document)

CLSS public abstract interface org.netbeans.modules.css.model.api.AtRule
intf org.netbeans.modules.css.model.api.Element
meth public abstract org.netbeans.modules.css.model.api.Element getElement()
meth public abstract void setElement(org.netbeans.modules.css.model.api.Element)

CLSS public abstract interface org.netbeans.modules.css.model.api.AtRuleId
intf org.netbeans.modules.css.model.api.PlainElement

CLSS public abstract interface org.netbeans.modules.css.model.api.Body
intf org.netbeans.modules.css.model.api.Element
meth public abstract boolean removeFontFace(org.netbeans.modules.css.model.api.FontFace)
meth public abstract boolean removeGenericAtRule(org.netbeans.modules.css.model.api.GenericAtRule)
meth public abstract boolean removeMedia(org.netbeans.modules.css.model.api.Media)
meth public abstract boolean removeMozDocument(org.netbeans.modules.css.model.api.MozDocument)
meth public abstract boolean removePage(org.netbeans.modules.css.model.api.Page)
meth public abstract boolean removeRule(org.netbeans.modules.css.model.api.Rule)
meth public abstract boolean removeWebkitKeyFrames(org.netbeans.modules.css.model.api.WebkitKeyframes)
meth public abstract java.util.List<org.netbeans.modules.css.model.api.BodyItem> getBodyItems()
meth public abstract java.util.List<org.netbeans.modules.css.model.api.FontFace> getFontFaces()
meth public abstract java.util.List<org.netbeans.modules.css.model.api.GenericAtRule> getGenericAtRules()
meth public abstract java.util.List<org.netbeans.modules.css.model.api.Media> getMedias()
meth public abstract java.util.List<org.netbeans.modules.css.model.api.MozDocument> getMozDocuments()
meth public abstract java.util.List<org.netbeans.modules.css.model.api.Page> getPages()
meth public abstract java.util.List<org.netbeans.modules.css.model.api.Rule> getRules()
meth public abstract java.util.List<org.netbeans.modules.css.model.api.WebkitKeyframes> getWebkitKeyFrames()
meth public abstract void addFontFace(org.netbeans.modules.css.model.api.FontFace)
meth public abstract void addGenericAtRule(org.netbeans.modules.css.model.api.GenericAtRule)
meth public abstract void addMedia(org.netbeans.modules.css.model.api.Media)
meth public abstract void addMozDocument(org.netbeans.modules.css.model.api.MozDocument)
meth public abstract void addPage(org.netbeans.modules.css.model.api.Page)
meth public abstract void addRule(org.netbeans.modules.css.model.api.Rule)
meth public abstract void addWebkitKeyFrames(org.netbeans.modules.css.model.api.WebkitKeyframes)

CLSS public abstract interface org.netbeans.modules.css.model.api.BodyItem
intf org.netbeans.modules.css.model.api.Element
meth public abstract org.netbeans.modules.css.model.api.Element getElement()
meth public abstract void setElement(org.netbeans.modules.css.model.api.Element)

CLSS public abstract interface org.netbeans.modules.css.model.api.CharSet
intf org.netbeans.modules.css.model.api.Element
meth public abstract org.netbeans.modules.css.model.api.CharSetValue getCharSetValue()
meth public abstract void setCharSetValue(org.netbeans.modules.css.model.api.CharSetValue)

CLSS public abstract interface org.netbeans.modules.css.model.api.CharSetValue
intf org.netbeans.modules.css.model.api.PlainElement

CLSS public abstract interface org.netbeans.modules.css.model.api.Declaration
intf org.netbeans.modules.css.model.api.Element
meth public abstract org.netbeans.modules.css.model.api.PropertyDeclaration getPropertyDeclaration()
meth public abstract void setPropertyDeclaration(org.netbeans.modules.css.model.api.PropertyDeclaration)

CLSS public abstract interface org.netbeans.modules.css.model.api.Declarations
intf org.netbeans.modules.css.model.api.Element
meth public abstract boolean removeDeclaration(org.netbeans.modules.css.model.api.Declaration)
meth public abstract java.util.List<org.netbeans.modules.css.model.api.Declaration> getDeclarations()
meth public abstract void addDeclaration(org.netbeans.modules.css.model.api.Declaration)

CLSS public abstract interface org.netbeans.modules.css.model.api.Element
meth public abstract boolean isValid()
meth public abstract boolean removeElement(org.netbeans.modules.css.model.api.Element)
meth public abstract int addElement(org.netbeans.modules.css.model.api.Element)
meth public abstract int getElementIndex(org.netbeans.modules.css.model.api.Element)
meth public abstract int getElementsCount()
meth public abstract int getEndOffset()
meth public abstract int getStartOffset()
meth public abstract java.util.Iterator<org.netbeans.modules.css.model.api.Element> childrenIterator()
meth public abstract org.netbeans.modules.css.model.api.Element getElementAt(int)
meth public abstract org.netbeans.modules.css.model.api.Element getParent()
meth public abstract org.netbeans.modules.css.model.api.Element removeElement(int)
meth public abstract org.netbeans.modules.css.model.api.Element setElementAt(int,org.netbeans.modules.css.model.api.Element)
meth public abstract org.netbeans.modules.css.model.api.ElementHandle getElementHandle()
meth public abstract org.netbeans.modules.css.model.api.Model getModel()
meth public abstract void accept(org.netbeans.modules.css.model.api.ModelVisitor)
meth public abstract void addElementListener(org.netbeans.modules.css.model.api.ElementListener)
meth public abstract void insertElement(int,org.netbeans.modules.css.model.api.Element)
meth public abstract void removeElementListener(org.netbeans.modules.css.model.api.ElementListener)
meth public abstract void setParent(org.netbeans.modules.css.model.api.Element)

CLSS public abstract interface org.netbeans.modules.css.model.api.ElementFactory
meth public abstract !varargs org.netbeans.modules.css.model.api.Declarations createDeclarations(org.netbeans.modules.css.model.api.PropertyDeclaration[])
meth public abstract !varargs org.netbeans.modules.css.model.api.MediaBody createMediaBody(org.netbeans.modules.css.model.api.Page[])
meth public abstract !varargs org.netbeans.modules.css.model.api.MediaBody createMediaBody(org.netbeans.modules.css.model.api.Rule[])
meth public abstract !varargs org.netbeans.modules.css.model.api.MediaQuery createMediaQuery(org.netbeans.modules.css.model.api.MediaQueryOperator,org.netbeans.modules.css.model.api.MediaType,org.netbeans.modules.css.model.api.MediaExpression[])
meth public abstract !varargs org.netbeans.modules.css.model.api.MediaQueryList createMediaQueryList(org.netbeans.modules.css.model.api.MediaQuery[])
meth public abstract !varargs org.netbeans.modules.css.model.api.SelectorsGroup createSelectorsGroup(org.netbeans.modules.css.model.api.Selector[])
meth public abstract org.netbeans.modules.css.model.api.AtRule createAtRule()
meth public abstract org.netbeans.modules.css.model.api.AtRuleId createAtRuleId()
meth public abstract org.netbeans.modules.css.model.api.AtRuleId createAtRuleId(java.lang.CharSequence)
meth public abstract org.netbeans.modules.css.model.api.Body createBody()
meth public abstract org.netbeans.modules.css.model.api.BodyItem createBodyItem()
meth public abstract org.netbeans.modules.css.model.api.CharSet createCharSet()
meth public abstract org.netbeans.modules.css.model.api.CharSetValue createCharSetValue()
meth public abstract org.netbeans.modules.css.model.api.Declaration createDeclaration()
meth public abstract org.netbeans.modules.css.model.api.Declarations createDeclarations()
meth public abstract org.netbeans.modules.css.model.api.Expression createExpression()
meth public abstract org.netbeans.modules.css.model.api.Expression createExpression(java.lang.CharSequence)
meth public abstract org.netbeans.modules.css.model.api.FontFace createFontFace()
meth public abstract org.netbeans.modules.css.model.api.FontFace createFontFace(org.netbeans.modules.css.model.api.Declarations)
meth public abstract org.netbeans.modules.css.model.api.GenericAtRule createGenericAtRule()
meth public abstract org.netbeans.modules.css.model.api.ImportItem createImportItem()
meth public abstract org.netbeans.modules.css.model.api.Imports createImports()
meth public abstract org.netbeans.modules.css.model.api.Media createMedia()
meth public abstract org.netbeans.modules.css.model.api.Media createMedia(org.netbeans.modules.css.model.api.MediaQueryList,org.netbeans.modules.css.model.api.MediaBody)
meth public abstract org.netbeans.modules.css.model.api.MediaBody createMediaBody()
meth public abstract org.netbeans.modules.css.model.api.MediaExpression createMediaExpression()
meth public abstract org.netbeans.modules.css.model.api.MediaExpression createMediaExpression(org.netbeans.modules.css.model.api.MediaFeature,org.netbeans.modules.css.model.api.MediaFeatureValue)
meth public abstract org.netbeans.modules.css.model.api.MediaFeature createMediaFeature()
meth public abstract org.netbeans.modules.css.model.api.MediaFeature createMediaFeature(java.lang.CharSequence)
meth public abstract org.netbeans.modules.css.model.api.MediaFeatureValue createMediaFeatureValue()
meth public abstract org.netbeans.modules.css.model.api.MediaFeatureValue createMediaFeatureValue(org.netbeans.modules.css.model.api.Expression)
meth public abstract org.netbeans.modules.css.model.api.MediaQuery createMediaQuery()
meth public abstract org.netbeans.modules.css.model.api.MediaQueryList createMediaQueryList()
meth public abstract org.netbeans.modules.css.model.api.MediaQueryOperator createMediaQueryOperator()
meth public abstract org.netbeans.modules.css.model.api.MediaQueryOperator createMediaQueryOperator(java.lang.CharSequence)
meth public abstract org.netbeans.modules.css.model.api.MediaType createMediaType()
meth public abstract org.netbeans.modules.css.model.api.MediaType createMediaType(java.lang.CharSequence)
meth public abstract org.netbeans.modules.css.model.api.MozDocument createMozDocument()
meth public abstract org.netbeans.modules.css.model.api.MozDocumentFunction createMozDocumentFunction()
meth public abstract org.netbeans.modules.css.model.api.Namespace createNamespace()
meth public abstract org.netbeans.modules.css.model.api.NamespacePrefixName createNamespacePrefixName()
meth public abstract org.netbeans.modules.css.model.api.Namespaces createNamespaces()
meth public abstract org.netbeans.modules.css.model.api.Page createPage()
meth public abstract org.netbeans.modules.css.model.api.Page createPage(java.lang.CharSequence)
meth public abstract org.netbeans.modules.css.model.api.PlainElement createPlainElement()
meth public abstract org.netbeans.modules.css.model.api.PlainElement createPlainElement(java.lang.CharSequence)
meth public abstract org.netbeans.modules.css.model.api.Prio createPrio()
meth public abstract org.netbeans.modules.css.model.api.Property createProperty()
meth public abstract org.netbeans.modules.css.model.api.Property createProperty(java.lang.CharSequence)
meth public abstract org.netbeans.modules.css.model.api.PropertyDeclaration createPropertyDeclaration()
meth public abstract org.netbeans.modules.css.model.api.PropertyDeclaration createPropertyDeclaration(org.netbeans.modules.css.model.api.Property,org.netbeans.modules.css.model.api.PropertyValue,boolean)
meth public abstract org.netbeans.modules.css.model.api.PropertyValue createPropertyValue()
meth public abstract org.netbeans.modules.css.model.api.PropertyValue createPropertyValue(org.netbeans.modules.css.model.api.Expression)
meth public abstract org.netbeans.modules.css.model.api.ResourceIdentifier createResourceIdentifier()
meth public abstract org.netbeans.modules.css.model.api.Rule createRule()
meth public abstract org.netbeans.modules.css.model.api.Rule createRule(org.netbeans.modules.css.model.api.SelectorsGroup,org.netbeans.modules.css.model.api.Declarations)
meth public abstract org.netbeans.modules.css.model.api.Selector createSelector()
meth public abstract org.netbeans.modules.css.model.api.Selector createSelector(java.lang.CharSequence)
meth public abstract org.netbeans.modules.css.model.api.SelectorsGroup createSelectorsGroup()
meth public abstract org.netbeans.modules.css.model.api.StyleSheet createStyleSheet()
meth public abstract org.netbeans.modules.css.model.api.VendorAtRule createVendorAtRule()
meth public abstract org.netbeans.modules.css.model.api.WebkitKeyframeSelectors createWebkitKeyframeSelectors()
meth public abstract org.netbeans.modules.css.model.api.WebkitKeyframes createWebkitKeyFrames()
meth public abstract org.netbeans.modules.css.model.api.WebkitKeyframesBlock createWebkitKeyFramesBlock()

CLSS public abstract interface org.netbeans.modules.css.model.api.ElementHandle
meth public abstract org.netbeans.modules.css.model.api.Element resolve(org.netbeans.modules.css.model.api.Model)

CLSS public abstract interface org.netbeans.modules.css.model.api.ElementListener
innr public abstract interface static Event
meth public abstract void elementChanged(org.netbeans.modules.css.model.api.ElementListener$Event)

CLSS public abstract interface static org.netbeans.modules.css.model.api.ElementListener$Event
 outer org.netbeans.modules.css.model.api.ElementListener

CLSS public abstract interface org.netbeans.modules.css.model.api.Expression
intf org.netbeans.modules.css.model.api.PlainElement

CLSS public abstract interface org.netbeans.modules.css.model.api.FontFace
intf org.netbeans.modules.css.model.api.Element
meth public abstract org.netbeans.modules.css.model.api.Declarations getDeclarations()
meth public abstract void setDeclarations(org.netbeans.modules.css.model.api.Declarations)

CLSS public abstract interface org.netbeans.modules.css.model.api.GenericAtRule
intf org.netbeans.modules.css.model.api.PlainElement

CLSS public abstract interface org.netbeans.modules.css.model.api.ImportItem
intf org.netbeans.modules.css.model.api.Element
meth public abstract org.netbeans.modules.css.model.api.MediaQueryList getMediaQueryList()
meth public abstract org.netbeans.modules.css.model.api.ResourceIdentifier getResourceIdentifier()
meth public abstract void setMediaQueryList(org.netbeans.modules.css.model.api.MediaQueryList)
meth public abstract void setResourceIdentifier(org.netbeans.modules.css.model.api.ResourceIdentifier)

CLSS public abstract interface org.netbeans.modules.css.model.api.Imports
intf org.netbeans.modules.css.model.api.Element
meth public abstract java.util.List<org.netbeans.modules.css.model.api.ImportItem> getImportItems()
meth public abstract void addImportItem(org.netbeans.modules.css.model.api.ImportItem)

CLSS public abstract interface org.netbeans.modules.css.model.api.Media
intf org.netbeans.modules.css.model.api.Element
meth public abstract org.netbeans.modules.css.model.api.MediaBody getMediaBody()
meth public abstract org.netbeans.modules.css.model.api.MediaQueryList getMediaQueryList()
meth public abstract void setMediaBody(org.netbeans.modules.css.model.api.MediaBody)
meth public abstract void setMediaQueryList(org.netbeans.modules.css.model.api.MediaQueryList)

CLSS public abstract interface org.netbeans.modules.css.model.api.MediaBody
intf org.netbeans.modules.css.model.api.Element
meth public abstract java.util.List<org.netbeans.modules.css.model.api.Page> getPages()
meth public abstract java.util.List<org.netbeans.modules.css.model.api.Rule> getRules()
meth public abstract void addPage(org.netbeans.modules.css.model.api.Page)
meth public abstract void addRule(org.netbeans.modules.css.model.api.Rule)

CLSS public abstract interface org.netbeans.modules.css.model.api.MediaExpression
intf org.netbeans.modules.css.model.api.Element
meth public abstract org.netbeans.modules.css.model.api.MediaFeature getMediaFeature()
meth public abstract org.netbeans.modules.css.model.api.MediaFeatureValue getMediaFeatureValue()
meth public abstract void setMediaFeature(org.netbeans.modules.css.model.api.MediaFeature)
meth public abstract void setMediaFeatureValue(org.netbeans.modules.css.model.api.MediaFeatureValue)

CLSS public abstract interface org.netbeans.modules.css.model.api.MediaFeature
intf org.netbeans.modules.css.model.api.PlainElement

CLSS public abstract interface org.netbeans.modules.css.model.api.MediaFeatureValue
intf org.netbeans.modules.css.model.api.Element
meth public abstract org.netbeans.modules.css.model.api.Expression getExpression()
meth public abstract void setExpression(org.netbeans.modules.css.model.api.Expression)

CLSS public abstract interface org.netbeans.modules.css.model.api.MediaQuery
intf org.netbeans.modules.css.model.api.Element
meth public abstract java.util.Collection<org.netbeans.modules.css.model.api.MediaExpression> getMediaExpressions()
meth public abstract org.netbeans.modules.css.model.api.MediaQueryOperator getMediaQueryOperator()
meth public abstract org.netbeans.modules.css.model.api.MediaType getMediaType()
meth public abstract void addMediaExpression(org.netbeans.modules.css.model.api.MediaExpression)
meth public abstract void setMediaQueryOperator(org.netbeans.modules.css.model.api.MediaQueryOperator)
meth public abstract void setMediaType(org.netbeans.modules.css.model.api.MediaType)

CLSS public abstract interface org.netbeans.modules.css.model.api.MediaQueryList
intf org.netbeans.modules.css.model.api.Element
meth public abstract java.util.List<org.netbeans.modules.css.model.api.MediaQuery> getMediaQueries()
meth public abstract void addMediaQuery(org.netbeans.modules.css.model.api.MediaQuery)

CLSS public abstract interface org.netbeans.modules.css.model.api.MediaQueryOperator
intf org.netbeans.modules.css.model.api.PlainElement

CLSS public abstract interface org.netbeans.modules.css.model.api.MediaType
intf org.netbeans.modules.css.model.api.PlainElement

CLSS public final org.netbeans.modules.css.model.api.Model
fld public final static java.lang.String CHANGES_APPLIED_TO_DOCUMENT = "changes.applied"
fld public final static java.lang.String MODEL_WRITE_TASK_FINISHED = "model.write.task.finished"
fld public final static java.lang.String NO_CHANGES_APPLIED_TO_DOCUMENT = "no.changes.to.apply"
innr public abstract interface static ModelTask
intf java.beans.PropertyChangeListener
meth public boolean applyChanges() throws java.io.IOException,javax.swing.text.BadLocationException
meth public boolean canApplyChanges()
meth public int getSerialNumber()
meth public java.lang.CharSequence getElementSource(org.netbeans.modules.css.model.api.Element)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.CharSequence getModelSource()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.CharSequence getOriginalSource()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String toString()
meth public org.netbeans.api.diff.Difference[] getModelSourceDiff() throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.css.model.api.ElementFactory getElementFactory()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.util.Lookup getLookup()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.css.model.api.Model createModel(org.netbeans.modules.css.lib.api.CssParserResult)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.css.model.api.Model getModel(org.netbeans.modules.css.lib.api.CssParserResult)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void runReadTask(org.netbeans.modules.css.model.api.Model$ModelTask)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void runWriteTask(org.netbeans.modules.css.model.api.Model$ModelTask)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds DIRECT_OFFSET_CONVERTOR,ELEMENT_FACTORY,LOGGER,MODEL_LOOKUP,MODEL_MUTEX,changesApplied,dataObject,documentLookup,editorCookie,globalModelSerialNumber,modelSerialNumber,support
hcls DocumentLookup,OffsetConvertor,SnapshotOffsetConvertor

CLSS public abstract interface static org.netbeans.modules.css.model.api.Model$ModelTask
 outer org.netbeans.modules.css.model.api.Model
meth public abstract void run(org.netbeans.modules.css.model.api.StyleSheet)

CLSS public org.netbeans.modules.css.model.api.ModelUtils
cons public init(org.netbeans.modules.css.model.api.Model)
meth public org.netbeans.modules.css.model.api.Body getBody()
meth public org.netbeans.modules.css.model.api.Media findMatchingMedia(org.netbeans.modules.css.model.api.Model,org.netbeans.modules.css.model.api.Media)
meth public org.netbeans.modules.css.model.api.PropertyDeclaration createPropertyDeclaration(java.lang.String)
meth public org.netbeans.modules.css.model.api.Rule createRule(java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>)
meth public org.netbeans.modules.css.model.api.Rule findMatchingRule(org.netbeans.modules.css.model.api.Model,org.netbeans.modules.css.model.api.Rule)
supr java.lang.Object
hfds factory,model,styleSheet
hcls MediaRefModelVisitor,ResolveMediaRefModelVisitor,ResolveRuleRefModelVisitor,RuleRefModelVisitor

CLSS public abstract interface org.netbeans.modules.css.model.api.ModelVisitor
innr public static Adapter
meth public abstract void visitMedia(org.netbeans.modules.css.model.api.Media)
meth public abstract void visitRule(org.netbeans.modules.css.model.api.Rule)

CLSS public static org.netbeans.modules.css.model.api.ModelVisitor$Adapter
 outer org.netbeans.modules.css.model.api.ModelVisitor
cons public init()
intf org.netbeans.modules.css.model.api.ModelVisitor
meth public void visitMedia(org.netbeans.modules.css.model.api.Media)
meth public void visitRule(org.netbeans.modules.css.model.api.Rule)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.css.model.api.MozDocument
intf org.netbeans.modules.css.model.api.Element
meth public abstract java.util.List<org.netbeans.modules.css.model.api.MozDocumentFunction> getRestrictions()
meth public abstract org.netbeans.modules.css.model.api.Body getBody()

CLSS public abstract interface org.netbeans.modules.css.model.api.MozDocumentFunction
intf org.netbeans.modules.css.model.api.PlainElement

CLSS public abstract interface org.netbeans.modules.css.model.api.Namespace
intf org.netbeans.modules.css.model.api.Element
meth public abstract org.netbeans.modules.css.model.api.NamespacePrefixName getNamespacePrefixName()
meth public abstract org.netbeans.modules.css.model.api.ResourceIdentifier getResourceIdentifier()
meth public abstract void setNamespacesPrefixName(org.netbeans.modules.css.model.api.NamespacePrefixName)
meth public abstract void setResourceIdentifier(org.netbeans.modules.css.model.api.ResourceIdentifier)

CLSS public abstract interface org.netbeans.modules.css.model.api.NamespacePrefixName
intf org.netbeans.modules.css.model.api.PlainElement

CLSS public abstract interface org.netbeans.modules.css.model.api.Namespaces
intf org.netbeans.modules.css.model.api.Element
meth public abstract java.util.List<org.netbeans.modules.css.model.api.Namespace> getNamespaces()
meth public abstract void addNamespace(org.netbeans.modules.css.model.api.Namespace)

CLSS public abstract interface org.netbeans.modules.css.model.api.Page
intf org.netbeans.modules.css.model.api.PlainElement

CLSS public abstract interface org.netbeans.modules.css.model.api.PlainElement
intf org.netbeans.modules.css.model.api.Element
meth public abstract java.lang.CharSequence getContent()
meth public abstract void setContent(java.lang.CharSequence)

CLSS public abstract interface org.netbeans.modules.css.model.api.Prio
intf org.netbeans.modules.css.model.api.PlainElement

CLSS public abstract interface org.netbeans.modules.css.model.api.Property
intf org.netbeans.modules.css.model.api.PlainElement

CLSS public abstract interface org.netbeans.modules.css.model.api.PropertyDeclaration
intf org.netbeans.modules.css.model.api.Element
meth public abstract org.netbeans.modules.css.lib.api.properties.ResolvedProperty getResolvedProperty()
meth public abstract org.netbeans.modules.css.model.api.Prio getPrio()
meth public abstract org.netbeans.modules.css.model.api.Property getProperty()
meth public abstract org.netbeans.modules.css.model.api.PropertyValue getPropertyValue()
meth public abstract void setPrio(org.netbeans.modules.css.model.api.Prio)
meth public abstract void setProperty(org.netbeans.modules.css.model.api.Property)
meth public abstract void setPropertyValue(org.netbeans.modules.css.model.api.PropertyValue)

CLSS public abstract interface org.netbeans.modules.css.model.api.PropertyValue
intf org.netbeans.modules.css.model.api.Element
meth public abstract org.netbeans.modules.css.model.api.Expression getExpression()
meth public abstract void setExpression(org.netbeans.modules.css.model.api.Expression)

CLSS public abstract interface org.netbeans.modules.css.model.api.ResourceIdentifier
intf org.netbeans.modules.css.model.api.PlainElement

CLSS public abstract interface org.netbeans.modules.css.model.api.Rule
intf org.netbeans.modules.css.model.api.Element
meth public abstract org.netbeans.modules.css.model.api.Declarations getDeclarations()
meth public abstract org.netbeans.modules.css.model.api.SelectorsGroup getSelectorsGroup()
meth public abstract void setDeclarations(org.netbeans.modules.css.model.api.Declarations)
meth public abstract void setSelectorsGroup(org.netbeans.modules.css.model.api.SelectorsGroup)

CLSS public abstract interface org.netbeans.modules.css.model.api.Selector
intf org.netbeans.modules.css.model.api.PlainElement

CLSS public abstract interface org.netbeans.modules.css.model.api.SelectorsGroup
intf org.netbeans.modules.css.model.api.Element
meth public abstract java.util.List<org.netbeans.modules.css.model.api.Selector> getSelectors()
meth public abstract void addSelector(org.netbeans.modules.css.model.api.Selector)

CLSS public abstract interface org.netbeans.modules.css.model.api.StyleSheet
intf org.netbeans.modules.css.model.api.Element
meth public abstract org.netbeans.modules.css.model.api.Body getBody()
meth public abstract org.netbeans.modules.css.model.api.CharSet getCharSet()
meth public abstract org.netbeans.modules.css.model.api.Imports getImports()
meth public abstract org.netbeans.modules.css.model.api.Namespaces getNamespaces()
meth public abstract void setBody(org.netbeans.modules.css.model.api.Body)
meth public abstract void setCharSet(org.netbeans.modules.css.model.api.CharSet)
meth public abstract void setImports(org.netbeans.modules.css.model.api.Imports)
meth public abstract void setNamespaces(org.netbeans.modules.css.model.api.Namespaces)

CLSS public abstract interface org.netbeans.modules.css.model.api.TokenElement
intf org.netbeans.modules.css.model.api.PlainElement
meth public abstract java.lang.CharSequence getContent()
meth public abstract void setContent(java.lang.CharSequence)

CLSS public abstract interface org.netbeans.modules.css.model.api.VendorAtRule
intf org.netbeans.modules.css.model.api.Element
meth public abstract org.netbeans.modules.css.model.api.Element getElement()
meth public abstract void setElement(org.netbeans.modules.css.model.api.Element)

CLSS public abstract interface org.netbeans.modules.css.model.api.WebkitKeyframeSelectors
intf org.netbeans.modules.css.model.api.PlainElement

CLSS public abstract interface org.netbeans.modules.css.model.api.WebkitKeyframes
intf org.netbeans.modules.css.model.api.Element
meth public abstract java.util.List<org.netbeans.modules.css.model.api.WebkitKeyframesBlock> getKeyFramesBlocks()
meth public abstract org.netbeans.modules.css.model.api.AtRuleId getAtRuleId()

CLSS public abstract interface org.netbeans.modules.css.model.api.WebkitKeyframesBlock
intf org.netbeans.modules.css.model.api.Element
meth public abstract org.netbeans.modules.css.model.api.Declarations getDeclarations()
meth public abstract org.netbeans.modules.css.model.api.WebkitKeyframeSelectors getSelectors()

