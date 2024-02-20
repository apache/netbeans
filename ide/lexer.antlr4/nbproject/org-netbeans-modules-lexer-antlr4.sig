#Signature file v4.1
#Version 1.5.0

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
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

CLSS public abstract interface java.util.function.Predicate<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract boolean test({java.util.function.Predicate%0})
meth public java.util.function.Predicate<{java.util.function.Predicate%0}> and(java.util.function.Predicate<? super {java.util.function.Predicate%0}>)
meth public java.util.function.Predicate<{java.util.function.Predicate%0}> negate()
meth public java.util.function.Predicate<{java.util.function.Predicate%0}> or(java.util.function.Predicate<? super {java.util.function.Predicate%0}>)
meth public static <%0 extends java.lang.Object> java.util.function.Predicate<{%%0}> isEqual(java.lang.Object)

CLSS public abstract interface org.netbeans.spi.lexer.Lexer<%0 extends org.netbeans.api.lexer.TokenId>
meth public abstract java.lang.Object state()
meth public abstract org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.Lexer%0}> nextToken()
meth public abstract void release()

CLSS public abstract org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge<%0 extends org.antlr.v4.runtime.Lexer, %1 extends org.netbeans.api.lexer.TokenId>
cons public init(org.netbeans.spi.lexer.LexerRestartInfo<{org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge%1}>,java.util.function.Function<org.antlr.v4.runtime.CharStream,{org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge%0}>)
fld protected final {org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge%0} lexer
innr public static LexerState
intf org.netbeans.spi.lexer.Lexer<{org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge%1}>
meth protected abstract org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge%1}> mapToken(org.antlr.v4.runtime.Token)
meth protected final org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge%1}> groupToken({org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge%1},int)
meth protected final org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge%1}> token({org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge%1})
meth protected java.lang.String flyweightText({org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge%1})
meth public final org.netbeans.api.lexer.Token<{org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge%1}> nextToken()
meth public java.lang.Object state()
meth public void release()
supr java.lang.Object
hfds FIXED_TOKEN_FACTORY,input,preFetchedToken,tokenFactory
hcls FixedToken

CLSS public static org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge$LexerState<%0 extends org.antlr.v4.runtime.Lexer>
 outer org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge
cons public init({org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge$LexerState%0})
meth public java.lang.String toString()
meth public void restore({org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge$LexerState%0})
supr java.lang.Object
hfds mode,modes,state

CLSS public final org.netbeans.spi.lexer.antlr4.AntlrTokenSequence
cons public init(org.antlr.v4.runtime.TokenSource)
fld public final static java.util.function.Predicate<org.antlr.v4.runtime.Token> DEFAULT_CHANNEL
innr public final static ChannelFilter
meth public boolean hasNext()
meth public boolean hasPrevious()
meth public boolean isEmpty()
meth public int getOffset()
meth public java.util.Optional<org.antlr.v4.runtime.Token> next()
meth public java.util.Optional<org.antlr.v4.runtime.Token> next(int)
meth public java.util.Optional<org.antlr.v4.runtime.Token> next(java.util.function.Predicate<org.antlr.v4.runtime.Token>)
meth public java.util.Optional<org.antlr.v4.runtime.Token> previous()
meth public java.util.Optional<org.antlr.v4.runtime.Token> previous(int)
meth public java.util.Optional<org.antlr.v4.runtime.Token> previous(java.util.function.Predicate<org.antlr.v4.runtime.Token>)
meth public void seekTo(int)
supr java.lang.Object
hfds cursor,cursorOffset,eofRead,readIndex,tokenList,tokens

CLSS public final static org.netbeans.spi.lexer.antlr4.AntlrTokenSequence$ChannelFilter
 outer org.netbeans.spi.lexer.antlr4.AntlrTokenSequence
cons public init(int)
intf java.util.function.Predicate<org.antlr.v4.runtime.Token>
meth public boolean test(org.antlr.v4.runtime.Token)
supr java.lang.Object
hfds channel

CLSS abstract interface org.netbeans.spi.lexer.antlr4.package-info

