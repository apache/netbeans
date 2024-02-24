#Signature file v4.1
#Version 1.86.0

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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.api.lexer.InputAttributes
cons public init()
meth public java.lang.Object getValue(org.netbeans.api.lexer.LanguagePath,java.lang.Object)
meth public void setValue(org.netbeans.api.lexer.Language<?>,java.lang.Object,java.lang.Object,boolean)
meth public void setValue(org.netbeans.api.lexer.LanguagePath,java.lang.Object,java.lang.Object,boolean)
supr java.lang.Object
hfds lp2attrs
hcls LPAttrs

CLSS public final org.netbeans.api.lexer.Language<%0 extends org.netbeans.api.lexer.TokenId>
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public int maxOrdinal()
meth public java.lang.String dumpInfo()
meth public java.lang.String mimeType()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> nonPrimaryTokenCategories({org.netbeans.api.lexer.Language%0})
meth public java.util.List<java.lang.String> tokenCategories({org.netbeans.api.lexer.Language%0})
meth public java.util.Set<java.lang.String> tokenCategories()
meth public java.util.Set<{org.netbeans.api.lexer.Language%0}> merge(java.util.Collection<{org.netbeans.api.lexer.Language%0}>,java.util.Collection<{org.netbeans.api.lexer.Language%0}>)
meth public java.util.Set<{org.netbeans.api.lexer.Language%0}> tokenCategoryMembers(java.lang.String)
meth public java.util.Set<{org.netbeans.api.lexer.Language%0}> tokenIds()
meth public static org.netbeans.api.lexer.Language<? extends org.netbeans.api.lexer.TokenId> find(java.lang.String)
meth public {org.netbeans.api.lexer.Language%0} tokenId(int)
meth public {org.netbeans.api.lexer.Language%0} tokenId(java.lang.String)
meth public {org.netbeans.api.lexer.Language%0} validTokenId(int)
meth public {org.netbeans.api.lexer.Language%0} validTokenId(java.lang.String)
supr java.lang.Object
hfds cat2ids,id,id2cats,id2nonPrimaryCats,idName2id,ids,indexedIds,languageHierarchy,languageOperation,languageRefList,maxOrdinal,mimeType
hcls Accessor

CLSS public final org.netbeans.api.lexer.LanguagePath
meth public boolean endsWith(org.netbeans.api.lexer.LanguagePath)
meth public int size()
meth public java.lang.String mimePath()
meth public java.lang.String toString()
meth public org.netbeans.api.lexer.Language<?> innerLanguage()
meth public org.netbeans.api.lexer.Language<?> language(int)
meth public org.netbeans.api.lexer.Language<?> topLanguage()
meth public org.netbeans.api.lexer.LanguagePath embedded(org.netbeans.api.lexer.Language<?>)
meth public org.netbeans.api.lexer.LanguagePath embedded(org.netbeans.api.lexer.LanguagePath)
meth public org.netbeans.api.lexer.LanguagePath parent()
meth public org.netbeans.api.lexer.LanguagePath subPath(int)
meth public org.netbeans.api.lexer.LanguagePath subPath(int,int)
meth public static org.netbeans.api.lexer.LanguagePath get(org.netbeans.api.lexer.Language<?>)
meth public static org.netbeans.api.lexer.LanguagePath get(org.netbeans.api.lexer.LanguagePath,org.netbeans.api.lexer.Language<?>)
supr java.lang.Object
hfds EMPTY,language2path,languages,mimePath,parent

CLSS public final !enum org.netbeans.api.lexer.PartType
fld public final static org.netbeans.api.lexer.PartType COMPLETE
fld public final static org.netbeans.api.lexer.PartType END
fld public final static org.netbeans.api.lexer.PartType MIDDLE
fld public final static org.netbeans.api.lexer.PartType START
meth public static org.netbeans.api.lexer.PartType valueOf(java.lang.String)
meth public static org.netbeans.api.lexer.PartType[] values()
supr java.lang.Enum<org.netbeans.api.lexer.PartType>

CLSS public abstract org.netbeans.api.lexer.Token<%0 extends org.netbeans.api.lexer.TokenId>
cons protected init()
meth public abstract boolean hasProperties()
meth public abstract boolean isCustomText()
meth public abstract boolean isFlyweight()
meth public abstract int length()
meth public abstract int offset(org.netbeans.api.lexer.TokenHierarchy<?>)
meth public abstract java.lang.CharSequence text()
meth public abstract java.lang.Object getProperty(java.lang.Object)
meth public abstract org.netbeans.api.lexer.PartType partType()
meth public abstract {org.netbeans.api.lexer.Token%0} id()
meth public boolean isRemoved()
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public java.util.List<? extends org.netbeans.api.lexer.Token<{org.netbeans.api.lexer.Token%0}>> joinedParts()
meth public org.netbeans.api.lexer.Token<{org.netbeans.api.lexer.Token%0}> joinToken()
supr java.lang.Object

CLSS public final org.netbeans.api.lexer.TokenChange<%0 extends org.netbeans.api.lexer.TokenId>
meth public boolean isBoundsChange()
meth public int addedTokenCount()
meth public int embeddedChangeCount()
meth public int index()
meth public int offset()
meth public int removedTokenCount()
meth public java.lang.String toString()
meth public org.netbeans.api.lexer.Language<{org.netbeans.api.lexer.TokenChange%0}> language()
meth public org.netbeans.api.lexer.LanguagePath languagePath()
meth public org.netbeans.api.lexer.TokenChange<?> embeddedChange(int)
meth public org.netbeans.api.lexer.TokenSequence<{org.netbeans.api.lexer.TokenChange%0}> currentTokenSequence()
meth public org.netbeans.api.lexer.TokenSequence<{org.netbeans.api.lexer.TokenChange%0}> removedTokenSequence()
supr java.lang.Object
hfds info

CLSS public final org.netbeans.api.lexer.TokenHierarchy<%0 extends java.lang.Object>
meth public <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.api.lexer.TokenSequence<{%%0}> tokenSequence(org.netbeans.api.lexer.Language<{%%0}>)
meth public boolean isActive()
meth public boolean isMutable()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.api.lexer.TokenSequence<?>> embeddedTokenSequences(int,boolean)
meth public java.util.List<org.netbeans.api.lexer.TokenSequence<?>> tokenSequenceList(org.netbeans.api.lexer.LanguagePath,int,int)
meth public java.util.Set<org.netbeans.api.lexer.LanguagePath> languagePaths()
meth public org.netbeans.api.lexer.TokenSequence<?> tokenSequence()
meth public static <%0 extends java.io.Reader, %1 extends org.netbeans.api.lexer.TokenId> org.netbeans.api.lexer.TokenHierarchy<{%%0}> create({%%0},org.netbeans.api.lexer.Language<{%%1}>,java.util.Set<{%%1}>,org.netbeans.api.lexer.InputAttributes)
meth public static <%0 extends java.lang.CharSequence, %1 extends org.netbeans.api.lexer.TokenId> org.netbeans.api.lexer.TokenHierarchy<{%%0}> create({%%0},boolean,org.netbeans.api.lexer.Language<{%%1}>,java.util.Set<{%%1}>,org.netbeans.api.lexer.InputAttributes)
meth public static <%0 extends java.lang.CharSequence> org.netbeans.api.lexer.TokenHierarchy<{%%0}> create({%%0},org.netbeans.api.lexer.Language<?>)
meth public static <%0 extends javax.swing.text.Document> org.netbeans.api.lexer.TokenHierarchy<{%%0}> get({%%0})
meth public void addTokenHierarchyListener(org.netbeans.api.lexer.TokenHierarchyListener)
meth public void removeTokenHierarchyListener(org.netbeans.api.lexer.TokenHierarchyListener)
meth public {org.netbeans.api.lexer.TokenHierarchy%0} inputSource()
supr java.lang.Object
hfds operation

CLSS public final org.netbeans.api.lexer.TokenHierarchyEvent
meth public <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.api.lexer.TokenChange<{%%0}> tokenChange(org.netbeans.api.lexer.Language<{%%0}>)
meth public int affectedEndOffset()
meth public int affectedStartOffset()
meth public int insertedLength()
meth public int modificationOffset()
meth public int removedLength()
meth public java.lang.String toString()
meth public org.netbeans.api.lexer.TokenChange<?> tokenChange()
meth public org.netbeans.api.lexer.TokenHierarchy<?> tokenHierarchy()
meth public org.netbeans.api.lexer.TokenHierarchyEventType type()
supr java.util.EventObject
hfds info

CLSS public final !enum org.netbeans.api.lexer.TokenHierarchyEventType
fld public final static org.netbeans.api.lexer.TokenHierarchyEventType ACTIVITY
fld public final static org.netbeans.api.lexer.TokenHierarchyEventType EMBEDDING_CREATED
fld public final static org.netbeans.api.lexer.TokenHierarchyEventType EMBEDDING_REMOVED
fld public final static org.netbeans.api.lexer.TokenHierarchyEventType LANGUAGE_PATHS
fld public final static org.netbeans.api.lexer.TokenHierarchyEventType MODIFICATION
fld public final static org.netbeans.api.lexer.TokenHierarchyEventType REBUILD
fld public final static org.netbeans.api.lexer.TokenHierarchyEventType RELEX
meth public static org.netbeans.api.lexer.TokenHierarchyEventType valueOf(java.lang.String)
meth public static org.netbeans.api.lexer.TokenHierarchyEventType[] values()
supr java.lang.Enum<org.netbeans.api.lexer.TokenHierarchyEventType>

CLSS public abstract interface org.netbeans.api.lexer.TokenHierarchyListener
intf java.util.EventListener
meth public abstract void tokenHierarchyChanged(org.netbeans.api.lexer.TokenHierarchyEvent)

CLSS public abstract interface org.netbeans.api.lexer.TokenId
meth public abstract int ordinal()
meth public abstract java.lang.String name()
meth public abstract java.lang.String primaryCategory()

CLSS public final org.netbeans.api.lexer.TokenSequence<%0 extends org.netbeans.api.lexer.TokenId>
meth public <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.api.lexer.TokenSequence<{%%0}> embedded(org.netbeans.api.lexer.Language<{%%0}>)
meth public <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.api.lexer.TokenSequence<{%%0}> embeddedJoined(org.netbeans.api.lexer.Language<{%%0}>)
meth public boolean createEmbedding(org.netbeans.api.lexer.Language<?>,int,int)
meth public boolean createEmbedding(org.netbeans.api.lexer.Language<?>,int,int,boolean)
meth public boolean isEmpty()
meth public boolean isValid()
meth public boolean moveNext()
meth public boolean movePrevious()
meth public boolean removeEmbedding(org.netbeans.api.lexer.Language<?>)
meth public int index()
meth public int move(int)
meth public int moveIndex(int)
meth public int offset()
meth public int tokenCount()
meth public java.lang.String toString()
meth public org.netbeans.api.lexer.Language<{org.netbeans.api.lexer.TokenSequence%0}> language()
meth public org.netbeans.api.lexer.LanguagePath languagePath()
meth public org.netbeans.api.lexer.Token<{org.netbeans.api.lexer.TokenSequence%0}> offsetToken()
meth public org.netbeans.api.lexer.Token<{org.netbeans.api.lexer.TokenSequence%0}> token()
meth public org.netbeans.api.lexer.TokenSequence<?> embedded()
meth public org.netbeans.api.lexer.TokenSequence<?> embeddedJoined()
meth public org.netbeans.api.lexer.TokenSequence<{org.netbeans.api.lexer.TokenSequence%0}> subSequence(int)
meth public org.netbeans.api.lexer.TokenSequence<{org.netbeans.api.lexer.TokenSequence%0}> subSequence(int,int)
meth public void moveEnd()
meth public void moveStart()
supr java.lang.Object
hfds embeddedTokenList,modCount,rootTokenList,token,tokenIndex,tokenList,tokenOffset

CLSS public final org.netbeans.api.lexer.TokenUtilities
meth public static boolean endsWith(java.lang.CharSequence,java.lang.CharSequence)
meth public static boolean equals(java.lang.CharSequence,java.lang.Object)
meth public static boolean startsWith(java.lang.CharSequence,java.lang.CharSequence)
meth public static boolean textEquals(java.lang.CharSequence,java.lang.CharSequence)
meth public static int indexOf(java.lang.CharSequence,int)
meth public static int indexOf(java.lang.CharSequence,int,int)
meth public static int indexOf(java.lang.CharSequence,java.lang.CharSequence)
meth public static int indexOf(java.lang.CharSequence,java.lang.CharSequence,int)
meth public static int lastIndexOf(java.lang.CharSequence,int)
meth public static int lastIndexOf(java.lang.CharSequence,int,int)
meth public static int lastIndexOf(java.lang.CharSequence,java.lang.CharSequence)
meth public static int lastIndexOf(java.lang.CharSequence,java.lang.CharSequence,int)
meth public static java.lang.CharSequence trim(java.lang.CharSequence)
meth public static java.lang.String debugText(java.lang.CharSequence)
supr java.lang.Object

CLSS public final !enum org.netbeans.spi.lexer.EmbeddingPresence
fld public final static org.netbeans.spi.lexer.EmbeddingPresence ALWAYS_QUERY
fld public final static org.netbeans.spi.lexer.EmbeddingPresence CACHED_FIRST_QUERY
fld public final static org.netbeans.spi.lexer.EmbeddingPresence NONE
meth public static org.netbeans.spi.lexer.EmbeddingPresence valueOf(java.lang.String)
meth public static org.netbeans.spi.lexer.EmbeddingPresence[] values()
supr java.lang.Enum<org.netbeans.spi.lexer.EmbeddingPresence>

CLSS public final org.netbeans.spi.lexer.LanguageEmbedding<%0 extends org.netbeans.api.lexer.TokenId>
meth public boolean joinSections()
meth public int endSkipLength()
meth public int startSkipLength()
meth public java.lang.String toString()
meth public org.netbeans.api.lexer.Language<{org.netbeans.spi.lexer.LanguageEmbedding%0}> language()
meth public static <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.spi.lexer.LanguageEmbedding<{%%0}> create(org.netbeans.api.lexer.Language<{%%0}>,int,int)
meth public static <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.spi.lexer.LanguageEmbedding<{%%0}> create(org.netbeans.api.lexer.Language<{%%0}>,int,int,boolean)
supr java.lang.Object
hfds endSkipLength,joinSections,language,startSkipLength

CLSS public abstract org.netbeans.spi.lexer.LanguageHierarchy<%0 extends org.netbeans.api.lexer.TokenId>
cons public init()
meth protected abstract java.lang.String mimeType()
meth protected abstract java.util.Collection<{org.netbeans.spi.lexer.LanguageHierarchy%0}> createTokenIds()
meth protected abstract org.netbeans.spi.lexer.Lexer<{org.netbeans.spi.lexer.LanguageHierarchy%0}> createLexer(org.netbeans.spi.lexer.LexerRestartInfo<{org.netbeans.spi.lexer.LanguageHierarchy%0}>)
meth protected boolean isRetainTokenText({org.netbeans.spi.lexer.LanguageHierarchy%0})
meth protected java.util.Map<java.lang.String,java.util.Collection<{org.netbeans.spi.lexer.LanguageHierarchy%0}>> createTokenCategories()
meth protected org.netbeans.spi.lexer.EmbeddingPresence embeddingPresence({org.netbeans.spi.lexer.LanguageHierarchy%0})
meth protected org.netbeans.spi.lexer.LanguageEmbedding<?> embedding(org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.LanguageHierarchy%0}>,org.netbeans.api.lexer.LanguagePath,org.netbeans.api.lexer.InputAttributes)
meth protected org.netbeans.spi.lexer.TokenValidator<{org.netbeans.spi.lexer.LanguageHierarchy%0}> createTokenValidator({org.netbeans.spi.lexer.LanguageHierarchy%0})
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final org.netbeans.api.lexer.Language<{org.netbeans.spi.lexer.LanguageHierarchy%0}> language()
meth public java.lang.String toString()
meth public static org.netbeans.api.lexer.TokenId newId(java.lang.String,int)
meth public static org.netbeans.api.lexer.TokenId newId(java.lang.String,int,java.lang.String)
supr java.lang.Object
hfds language
hcls Accessor

CLSS public abstract org.netbeans.spi.lexer.LanguageProvider
cons protected init()
fld public final static java.lang.String PROP_EMBEDDED_LANGUAGE = "LanguageProvider.PROP_EMBEDDED_LANGUAGE"
fld public final static java.lang.String PROP_LANGUAGE = "LanguageProvider.PROP_LANGUAGE"
meth protected final void firePropertyChange(java.lang.String)
meth public abstract org.netbeans.api.lexer.Language<?> findLanguage(java.lang.String)
meth public abstract org.netbeans.spi.lexer.LanguageEmbedding<?> findLanguageEmbedding(org.netbeans.api.lexer.Token<?>,org.netbeans.api.lexer.LanguagePath,org.netbeans.api.lexer.InputAttributes)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds pcs

CLSS public abstract interface org.netbeans.spi.lexer.Lexer<%0 extends org.netbeans.api.lexer.TokenId>
meth public abstract java.lang.Object state()
meth public abstract org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.Lexer%0}> nextToken()
meth public abstract void release()

CLSS public final org.netbeans.spi.lexer.LexerInput
fld public final static int EOF = -1
meth public boolean consumeNewline()
meth public int read()
meth public int readLength()
meth public int readLengthEOF()
meth public java.lang.CharSequence readText()
meth public java.lang.CharSequence readText(int,int)
meth public void backup(int)
supr java.lang.Object
hfds LOG,eof,loggable,operation,readText
hcls ReadText

CLSS public final org.netbeans.spi.lexer.LexerRestartInfo<%0 extends org.netbeans.api.lexer.TokenId>
meth public java.lang.Object getAttributeValue(java.lang.Object)
meth public java.lang.Object state()
meth public org.netbeans.api.lexer.InputAttributes inputAttributes()
meth public org.netbeans.api.lexer.LanguagePath languagePath()
meth public org.netbeans.spi.lexer.LexerInput input()
meth public org.netbeans.spi.lexer.TokenFactory<{org.netbeans.spi.lexer.LexerRestartInfo%0}> tokenFactory()
supr java.lang.Object
hfds input,inputAttributes,languagePath,state,tokenFactory

CLSS public abstract org.netbeans.spi.lexer.MutableTextInput<%0 extends java.lang.Object>
cons public init()
meth protected abstract boolean isReadLocked()
meth protected abstract boolean isWriteLocked()
meth protected abstract java.lang.CharSequence text()
meth protected abstract org.netbeans.api.lexer.InputAttributes inputAttributes()
meth protected abstract org.netbeans.api.lexer.Language<?> language()
meth protected abstract {org.netbeans.spi.lexer.MutableTextInput%0} inputSource()
meth public final org.netbeans.spi.lexer.TokenHierarchyControl<{org.netbeans.spi.lexer.MutableTextInput%0}> tokenHierarchyControl()
supr java.lang.Object
hfds thc

CLSS public final org.netbeans.spi.lexer.TokenFactory<%0 extends org.netbeans.api.lexer.TokenId>
fld public final static org.netbeans.api.lexer.Token SKIP_TOKEN
 anno 0 java.lang.Deprecated()
meth public boolean isSkipToken(org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.TokenFactory%0}>)
meth public org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.TokenFactory%0}> createCustomTextToken({org.netbeans.spi.lexer.TokenFactory%0},java.lang.CharSequence,int,org.netbeans.api.lexer.PartType)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.TokenFactory%0}> createPropertyToken({org.netbeans.spi.lexer.TokenFactory%0},int,org.netbeans.spi.lexer.TokenPropertyProvider<{org.netbeans.spi.lexer.TokenFactory%0}>)
meth public org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.TokenFactory%0}> createPropertyToken({org.netbeans.spi.lexer.TokenFactory%0},int,org.netbeans.spi.lexer.TokenPropertyProvider<{org.netbeans.spi.lexer.TokenFactory%0}>,org.netbeans.api.lexer.PartType)
meth public org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.TokenFactory%0}> createToken({org.netbeans.spi.lexer.TokenFactory%0})
meth public org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.TokenFactory%0}> createToken({org.netbeans.spi.lexer.TokenFactory%0},int)
meth public org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.TokenFactory%0}> createToken({org.netbeans.spi.lexer.TokenFactory%0},int,org.netbeans.api.lexer.PartType)
meth public org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.TokenFactory%0}> getFlyweightToken({org.netbeans.spi.lexer.TokenFactory%0},java.lang.String)
supr java.lang.Object
hfds operation

CLSS public final org.netbeans.spi.lexer.TokenHierarchyControl<%0 extends java.lang.Object>
meth public boolean isActive()
meth public org.netbeans.api.lexer.TokenHierarchy<{org.netbeans.spi.lexer.TokenHierarchyControl%0}> tokenHierarchy()
meth public void rebuild()
meth public void setActive(boolean)
meth public void textModified(int,int,java.lang.CharSequence,int)
supr java.lang.Object
hfds operation

CLSS public abstract interface org.netbeans.spi.lexer.TokenPropertyProvider<%0 extends org.netbeans.api.lexer.TokenId>
meth public abstract java.lang.Object getValue(org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.TokenPropertyProvider%0}>,java.lang.Object)

CLSS public abstract interface org.netbeans.spi.lexer.TokenValidator<%0 extends org.netbeans.api.lexer.TokenId>
meth public abstract org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.TokenValidator%0}> validateToken(org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.TokenValidator%0}>,org.netbeans.spi.lexer.TokenFactory<{org.netbeans.spi.lexer.TokenValidator%0}>,java.lang.CharSequence,int,int,java.lang.CharSequence,int,java.lang.CharSequence)

