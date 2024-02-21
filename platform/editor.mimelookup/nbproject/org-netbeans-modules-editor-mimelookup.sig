#Signature file v4.1
#Version 1.63

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

CLSS public final org.netbeans.api.editor.mimelookup.MimeLookup
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
 anno 0 java.lang.Deprecated()
meth public <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.api.editor.mimelookup.MimeLookup childLookup(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.api.editor.mimelookup.MimeLookup getMimeLookup(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static org.openide.util.Lookup getLookup(java.lang.String)
meth public static org.openide.util.Lookup getLookup(org.netbeans.api.editor.mimelookup.MimePath)
supr org.openide.util.Lookup
hfds mimePath,mimePathLookup

CLSS public final org.netbeans.api.editor.mimelookup.MimePath
fld public final static org.netbeans.api.editor.mimelookup.MimePath EMPTY
meth public int size()
meth public java.lang.String getInheritedType()
meth public java.lang.String getMimeType(int)
meth public java.lang.String getPath()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.api.editor.mimelookup.MimePath> getIncludedPaths()
meth public org.netbeans.api.editor.mimelookup.MimePath getPrefix(int)
meth public static boolean validate(java.lang.CharSequence)
meth public static boolean validate(java.lang.CharSequence,java.lang.CharSequence)
meth public static org.netbeans.api.editor.mimelookup.MimePath get(java.lang.String)
meth public static org.netbeans.api.editor.mimelookup.MimePath get(org.netbeans.api.editor.mimelookup.MimePath,java.lang.String)
meth public static org.netbeans.api.editor.mimelookup.MimePath parse(java.lang.String)
supr java.lang.Object
hfds LOCK,LOOKUP_LOCK,LRU,MAX_LRU_SIZE,REG_NAME_PATTERN,WELL_KNOWN_TYPES,lookup,mimePaths,mimeType,mimeType2mimePathRef,path,string2mimePath
hcls AccessorImpl

CLSS public abstract interface !annotation org.netbeans.api.editor.mimelookup.MimeRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract java.lang.Class<?> service()
meth public abstract java.lang.String mimeType()

CLSS public abstract interface !annotation org.netbeans.api.editor.mimelookup.MimeRegistrations
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract org.netbeans.api.editor.mimelookup.MimeRegistration[] value()

CLSS public abstract interface org.netbeans.spi.editor.mimelookup.Class2LayerFolder<%0 extends java.lang.Object>
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.Class<{org.netbeans.spi.editor.mimelookup.Class2LayerFolder%0}> getClazz()
meth public abstract java.lang.String getLayerFolderName()
meth public abstract org.netbeans.spi.editor.mimelookup.InstanceProvider<{org.netbeans.spi.editor.mimelookup.Class2LayerFolder%0}> getInstanceProvider()

CLSS public abstract interface org.netbeans.spi.editor.mimelookup.InstanceProvider<%0 extends java.lang.Object>
meth public abstract {org.netbeans.spi.editor.mimelookup.InstanceProvider%0} createInstance(java.util.List<org.openide.filesystems.FileObject>)

CLSS public abstract interface org.netbeans.spi.editor.mimelookup.MimeDataProvider
meth public abstract org.openide.util.Lookup getLookup(org.netbeans.api.editor.mimelookup.MimePath)

CLSS public abstract interface !annotation org.netbeans.spi.editor.mimelookup.MimeLocation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass()
meth public abstract java.lang.String subfolderName()

CLSS public abstract interface org.netbeans.spi.editor.mimelookup.MimeLookupInitializer
 anno 0 java.lang.Deprecated()
meth public abstract org.openide.util.Lookup lookup()
meth public abstract org.openide.util.Lookup$Result<org.netbeans.spi.editor.mimelookup.MimeLookupInitializer> child(java.lang.String)

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

