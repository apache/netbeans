#Signature file v4.1
#Version 1.48

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

CLSS public final org.netbeans.modules.spellchecker.api.LocaleQuery
meth public static java.util.Locale findLocale(org.openide.filesystems.FileObject)
supr java.lang.Object

CLSS public org.netbeans.modules.spellchecker.api.Spellchecker
cons public init()
meth public static void register(javax.swing.text.JTextComponent)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.spellchecker.spi.LocaleQueryImplementation
meth public abstract java.util.Locale findLocale(org.openide.filesystems.FileObject)

CLSS public abstract interface org.netbeans.modules.spellchecker.spi.dictionary.Dictionary
meth public abstract java.util.List<java.lang.String> findProposals(java.lang.CharSequence)
meth public abstract java.util.List<java.lang.String> findValidWordsForPrefix(java.lang.CharSequence)
meth public abstract org.netbeans.modules.spellchecker.spi.dictionary.ValidityType validateWord(java.lang.CharSequence)

CLSS public abstract interface org.netbeans.modules.spellchecker.spi.dictionary.DictionaryProvider
meth public abstract org.netbeans.modules.spellchecker.spi.dictionary.Dictionary getDictionary(java.util.Locale)

CLSS public final !enum org.netbeans.modules.spellchecker.spi.dictionary.ValidityType
fld public final static org.netbeans.modules.spellchecker.spi.dictionary.ValidityType BLACKLISTED
fld public final static org.netbeans.modules.spellchecker.spi.dictionary.ValidityType INVALID
fld public final static org.netbeans.modules.spellchecker.spi.dictionary.ValidityType PREFIX_OF_VALID
fld public final static org.netbeans.modules.spellchecker.spi.dictionary.ValidityType VALID
meth public static org.netbeans.modules.spellchecker.spi.dictionary.ValidityType valueOf(java.lang.String)
meth public static org.netbeans.modules.spellchecker.spi.dictionary.ValidityType[] values()
supr java.lang.Enum<org.netbeans.modules.spellchecker.spi.dictionary.ValidityType>

CLSS public abstract interface org.netbeans.modules.spellchecker.spi.language.TokenList
meth public abstract boolean nextWord()
meth public abstract int getCurrentWordStartOffset()
meth public abstract java.lang.CharSequence getCurrentWordText()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void setStartOffset(int)

CLSS public abstract interface org.netbeans.modules.spellchecker.spi.language.TokenListProvider
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="TokenListProvider")
meth public abstract org.netbeans.modules.spellchecker.spi.language.TokenList findTokenList(javax.swing.text.Document)

CLSS public org.netbeans.modules.spellchecker.spi.language.support.MultiTokenList
cons public init()
meth public static org.netbeans.modules.spellchecker.spi.language.TokenList create(java.util.List<org.netbeans.modules.spellchecker.spi.language.TokenList>)
supr java.lang.Object
hcls MultiTokenListImpl

CLSS public abstract interface !annotation org.netbeans.spi.editor.mimelookup.MimeLocation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass()
meth public abstract java.lang.String subfolderName()

