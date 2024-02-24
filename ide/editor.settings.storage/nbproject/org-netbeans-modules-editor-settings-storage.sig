#Signature file v4.1
#Version 1.75.0

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

CLSS public abstract org.netbeans.modules.editor.settings.storage.api.EditorSettings
cons public init()
fld public final static java.lang.String PROP_CURRENT_FONT_COLOR_PROFILE = "currentFontColorProfile"
fld public final static java.lang.String PROP_CURRENT_KEY_MAP_PROFILE = "currentKeyMapProfile"
fld public final static java.lang.String PROP_DEFAULT_FONT_COLORS = "defaultFontColors"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROP_EDITOR_FONT_COLORS
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROP_MIME_TYPES = "mime-types"
meth public abstract boolean isCustomFontColorProfile(java.lang.String)
meth public abstract boolean isCustomKeymapProfile(java.lang.String)
meth public abstract java.lang.String getCurrentFontColorProfile()
meth public abstract java.lang.String getCurrentKeyMapProfile()
meth public abstract java.lang.String getLanguageName(java.lang.String)
meth public abstract java.util.Collection<javax.swing.text.AttributeSet> getDefaultFontColorDefaults(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Collection<javax.swing.text.AttributeSet> getDefaultFontColors(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Map<java.lang.String,javax.swing.text.AttributeSet> getAnnotationDefaults(java.lang.String)
meth public abstract java.util.Map<java.lang.String,javax.swing.text.AttributeSet> getAnnotations(java.lang.String)
meth public abstract java.util.Map<java.lang.String,javax.swing.text.AttributeSet> getHighlightingDefaults(java.lang.String)
meth public abstract java.util.Map<java.lang.String,javax.swing.text.AttributeSet> getHighlightings(java.lang.String)
meth public abstract java.util.Set<java.lang.String> getAllMimeTypes()
meth public abstract java.util.Set<java.lang.String> getFontColorProfiles()
meth public abstract java.util.Set<java.lang.String> getKeyMapProfiles()
meth public abstract java.util.Set<java.lang.String> getMimeTypes()
meth public abstract org.netbeans.modules.editor.settings.storage.api.FontColorSettingsFactory getFontColorSettings(java.lang.String[])
meth public abstract org.netbeans.modules.editor.settings.storage.api.KeyBindingSettingsFactory getKeyBindingSettings(java.lang.String[])
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public abstract void setAnnotations(java.lang.String,java.util.Map<java.lang.String,javax.swing.text.AttributeSet>)
meth public abstract void setCurrentFontColorProfile(java.lang.String)
meth public abstract void setCurrentKeyMapProfile(java.lang.String)
meth public abstract void setDefaultFontColors(java.lang.String,java.util.Collection<javax.swing.text.AttributeSet>)
 anno 0 java.lang.Deprecated()
meth public abstract void setHighlightings(java.lang.String,java.util.Map<java.lang.String,javax.swing.text.AttributeSet>)
meth public static org.netbeans.modules.editor.settings.storage.api.EditorSettings getDefault()
supr java.lang.Object

CLSS public final org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage<%0 extends java.lang.Object, %1 extends java.lang.Object>
fld public final static java.lang.String PROP_DATA = "EditorSettingsStorage.PROP_DATA"
meth public java.util.Map<{org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage%0},{org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage%1}> load(org.netbeans.api.editor.mimelookup.MimePath,java.lang.String,boolean) throws java.io.IOException
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage<{%%0},{%%1}> find(java.lang.String)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage<{%%0},{%%1}> get(java.lang.String)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void delete(org.netbeans.api.editor.mimelookup.MimePath,java.lang.String,boolean) throws java.io.IOException
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void save(org.netbeans.api.editor.mimelookup.MimePath,java.lang.String,boolean,java.util.Map<{org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage%0},{org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage%1}>) throws java.io.IOException
supr java.lang.Object
hfds PCS,RP,storageImpl

CLSS public abstract org.netbeans.modules.editor.settings.storage.api.FontColorSettingsFactory
cons public init()
meth public abstract java.util.Collection<javax.swing.text.AttributeSet> getAllFontColorDefaults(java.lang.String)
meth public abstract java.util.Collection<javax.swing.text.AttributeSet> getAllFontColors(java.lang.String)
meth public abstract void setAllFontColors(java.lang.String,java.util.Collection<javax.swing.text.AttributeSet>)
meth public abstract void setAllFontColorsDefaults(java.lang.String,java.util.Collection<javax.swing.text.AttributeSet>)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.editor.settings.storage.api.KeyBindingSettingsFactory
cons public init()
meth public abstract java.util.List<org.netbeans.api.editor.settings.MultiKeyBinding> getKeyBindingDefaults(java.lang.String)
meth public abstract java.util.List<org.netbeans.api.editor.settings.MultiKeyBinding> getKeyBindings()
meth public abstract java.util.List<org.netbeans.api.editor.settings.MultiKeyBinding> getKeyBindings(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setKeyBindings(java.lang.String,java.util.List<org.netbeans.api.editor.settings.MultiKeyBinding>)
supr java.lang.Object

CLSS public final org.netbeans.modules.editor.settings.storage.api.MemoryPreferences
meth public boolean isDirty(java.util.prefs.Preferences)
meth public java.util.prefs.Preferences getPreferences()
meth public static org.netbeans.modules.editor.settings.storage.api.MemoryPreferences get(java.lang.Object,java.util.prefs.Preferences)
meth public static org.netbeans.modules.editor.settings.storage.api.MemoryPreferences getWithInherited(java.lang.Object,java.util.prefs.Preferences,java.util.prefs.Preferences)
meth public void destroy()
meth public void runWithoutEvents(java.lang.Runnable)
supr java.lang.Object
hfds prefInstance

CLSS public abstract interface org.netbeans.modules.editor.settings.storage.api.OverridePreferences
meth public abstract boolean isOverriden(java.lang.String)

CLSS public abstract interface org.netbeans.modules.editor.settings.storage.spi.StorageDescription<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract boolean isUsingProfiles()
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getLegacyFileName()
meth public abstract java.lang.String getMimeType()
meth public abstract org.netbeans.modules.editor.settings.storage.spi.StorageReader<{org.netbeans.modules.editor.settings.storage.spi.StorageDescription%0},{org.netbeans.modules.editor.settings.storage.spi.StorageDescription%1}> createReader(org.openide.filesystems.FileObject,java.lang.String)
meth public abstract org.netbeans.modules.editor.settings.storage.spi.StorageWriter<{org.netbeans.modules.editor.settings.storage.spi.StorageDescription%0},{org.netbeans.modules.editor.settings.storage.spi.StorageDescription%1}> createWriter(org.openide.filesystems.FileObject,java.lang.String)

CLSS public abstract org.netbeans.modules.editor.settings.storage.spi.StorageFilter<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init(java.lang.String)
meth protected final void notifyChanges()
meth public abstract void afterLoad(java.util.Map<{org.netbeans.modules.editor.settings.storage.spi.StorageFilter%0},{org.netbeans.modules.editor.settings.storage.spi.StorageFilter%1}>,org.netbeans.api.editor.mimelookup.MimePath,java.lang.String,boolean) throws java.io.IOException
meth public abstract void beforeSave(java.util.Map<{org.netbeans.modules.editor.settings.storage.spi.StorageFilter%0},{org.netbeans.modules.editor.settings.storage.spi.StorageFilter%1}>,org.netbeans.api.editor.mimelookup.MimePath,java.lang.String,boolean) throws java.io.IOException
supr java.lang.Object
hfds LOG,notificationCallback,storageDescriptionId

CLSS public abstract org.netbeans.modules.editor.settings.storage.spi.StorageReader<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init(org.openide.filesystems.FileObject,java.lang.String)
intf org.xml.sax.ext.LexicalHandler
meth protected final boolean isDefaultProfile()
meth protected final boolean isModuleFile()
meth protected final java.lang.String getMimePath()
meth protected final org.openide.filesystems.FileObject getProcessedFile()
meth public abstract java.util.Map<{org.netbeans.modules.editor.settings.storage.spi.StorageReader%0},{org.netbeans.modules.editor.settings.storage.spi.StorageReader%1}> getAdded()
meth public abstract java.util.Set<{org.netbeans.modules.editor.settings.storage.spi.StorageReader%0}> getRemoved()
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String)
meth public void comment(char[],int,int) throws org.xml.sax.SAXException
meth public void endCDATA() throws org.xml.sax.SAXException
meth public void endDTD() throws org.xml.sax.SAXException
meth public void endEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void startCDATA() throws org.xml.sax.SAXException
meth public void startDTD(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void startEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.DefaultHandler
hfds LOG,file,isDefaultProfile,isModuleFile,mimePath
hcls SpiPackageAccessorImpl

CLSS public abstract org.netbeans.modules.editor.settings.storage.spi.StorageWriter<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init()
meth protected final java.util.Map<? extends {org.netbeans.modules.editor.settings.storage.spi.StorageWriter%0},? extends {org.netbeans.modules.editor.settings.storage.spi.StorageWriter%1}> getAdded()
meth protected final java.util.Set<? extends {org.netbeans.modules.editor.settings.storage.spi.StorageWriter%0}> getRemoved()
meth public abstract org.w3c.dom.Document getDocument()
meth public final void setAdded(java.util.Map<? extends {org.netbeans.modules.editor.settings.storage.spi.StorageWriter%0},? extends {org.netbeans.modules.editor.settings.storage.spi.StorageWriter%1}>)
meth public final void setRemoved(java.util.Set<? extends {org.netbeans.modules.editor.settings.storage.spi.StorageWriter%0}>)
supr java.lang.Object
hfds added,removed

CLSS public final org.netbeans.modules.editor.settings.storage.spi.TypedValue
cons public init(java.lang.String,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getApiCategory()
meth public java.lang.String getJavaType()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public void setApiCategory(java.lang.String)
meth public void setJavaType(java.lang.String)
supr java.lang.Object
hfds apiCategory,javaType,value

CLSS public final org.netbeans.modules.editor.settings.storage.spi.support.StorageSupport
 anno 0 java.lang.Deprecated()
meth public static java.lang.String getLocalizingBundleMessage(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
meth public static java.lang.String keyStrokesToString(java.util.Collection<? extends javax.swing.KeyStroke>,boolean)
meth public static javax.swing.KeyStroke[] stringToKeyStrokes(java.lang.String,boolean)
supr java.lang.Object
hfds LOG,names

CLSS public abstract interface org.xml.sax.ContentHandler
meth public abstract void characters(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void endDocument() throws org.xml.sax.SAXException
meth public abstract void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void setDocumentLocator(org.xml.sax.Locator)
meth public abstract void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void startDocument() throws org.xml.sax.SAXException
meth public abstract void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public abstract void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.DTDHandler
meth public abstract void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.EntityResolver
meth public abstract org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.ErrorHandler
meth public abstract void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.ext.LexicalHandler
meth public abstract void comment(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void endCDATA() throws org.xml.sax.SAXException
meth public abstract void endDTD() throws org.xml.sax.SAXException
meth public abstract void endEntity(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void startCDATA() throws org.xml.sax.SAXException
meth public abstract void startDTD(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void startEntity(java.lang.String) throws org.xml.sax.SAXException

CLSS public org.xml.sax.helpers.DefaultHandler
cons public init()
intf org.xml.sax.ContentHandler
intf org.xml.sax.DTDHandler
intf org.xml.sax.EntityResolver
intf org.xml.sax.ErrorHandler
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr java.lang.Object

