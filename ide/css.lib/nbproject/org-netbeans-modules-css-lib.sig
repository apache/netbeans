#Signature file v4.1
#Version 2.3

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

CLSS public abstract interface java.lang.Runnable
 anno 0 java.lang.FunctionalInterface()
meth public abstract void run()

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

CLSS public abstract interface org.netbeans.api.lexer.TokenId
meth public abstract int ordinal()
meth public abstract java.lang.String name()
meth public abstract java.lang.String primaryCategory()

CLSS public abstract interface org.netbeans.modules.csl.api.Error
innr public abstract interface static Badging
meth public abstract boolean isLineError()
meth public abstract int getEndPosition()
meth public abstract int getStartPosition()
meth public abstract java.lang.Object[] getParameters()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getKey()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.csl.api.Severity getSeverity()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject getFile()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract org.netbeans.modules.csl.spi.ParserResult
cons protected init(org.netbeans.modules.parsing.api.Snapshot)
meth public abstract java.util.List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics()
supr org.netbeans.modules.parsing.spi.Parser$Result

CLSS public final !enum org.netbeans.modules.css.lib.api.CssColor
fld public final static org.netbeans.modules.css.lib.api.CssColor aliceblue
fld public final static org.netbeans.modules.css.lib.api.CssColor antiquewhite
fld public final static org.netbeans.modules.css.lib.api.CssColor aqua
fld public final static org.netbeans.modules.css.lib.api.CssColor aquamarine
fld public final static org.netbeans.modules.css.lib.api.CssColor azure
fld public final static org.netbeans.modules.css.lib.api.CssColor beige
fld public final static org.netbeans.modules.css.lib.api.CssColor bisque
fld public final static org.netbeans.modules.css.lib.api.CssColor black
fld public final static org.netbeans.modules.css.lib.api.CssColor blanchedalmond
fld public final static org.netbeans.modules.css.lib.api.CssColor blue
fld public final static org.netbeans.modules.css.lib.api.CssColor blueviolet
fld public final static org.netbeans.modules.css.lib.api.CssColor brown
fld public final static org.netbeans.modules.css.lib.api.CssColor burlywood
fld public final static org.netbeans.modules.css.lib.api.CssColor cadetblue
fld public final static org.netbeans.modules.css.lib.api.CssColor chartreuse
fld public final static org.netbeans.modules.css.lib.api.CssColor chocolate
fld public final static org.netbeans.modules.css.lib.api.CssColor coral
fld public final static org.netbeans.modules.css.lib.api.CssColor cornflowerblue
fld public final static org.netbeans.modules.css.lib.api.CssColor cornsilk
fld public final static org.netbeans.modules.css.lib.api.CssColor crimson
fld public final static org.netbeans.modules.css.lib.api.CssColor cyan
fld public final static org.netbeans.modules.css.lib.api.CssColor darkblue
fld public final static org.netbeans.modules.css.lib.api.CssColor darkcyan
fld public final static org.netbeans.modules.css.lib.api.CssColor darkgoldenrod
fld public final static org.netbeans.modules.css.lib.api.CssColor darkgray
fld public final static org.netbeans.modules.css.lib.api.CssColor darkgreen
fld public final static org.netbeans.modules.css.lib.api.CssColor darkgrey
fld public final static org.netbeans.modules.css.lib.api.CssColor darkkhaki
fld public final static org.netbeans.modules.css.lib.api.CssColor darkmagenta
fld public final static org.netbeans.modules.css.lib.api.CssColor darkolivegreen
fld public final static org.netbeans.modules.css.lib.api.CssColor darkorange
fld public final static org.netbeans.modules.css.lib.api.CssColor darkorchid
fld public final static org.netbeans.modules.css.lib.api.CssColor darkred
fld public final static org.netbeans.modules.css.lib.api.CssColor darksalmon
fld public final static org.netbeans.modules.css.lib.api.CssColor darkseagreen
fld public final static org.netbeans.modules.css.lib.api.CssColor darkslateblue
fld public final static org.netbeans.modules.css.lib.api.CssColor darkslategray
fld public final static org.netbeans.modules.css.lib.api.CssColor darkslategrey
fld public final static org.netbeans.modules.css.lib.api.CssColor darkturquoise
fld public final static org.netbeans.modules.css.lib.api.CssColor darkviolet
fld public final static org.netbeans.modules.css.lib.api.CssColor deeppink
fld public final static org.netbeans.modules.css.lib.api.CssColor deepskyblue
fld public final static org.netbeans.modules.css.lib.api.CssColor dimgray
fld public final static org.netbeans.modules.css.lib.api.CssColor dimgrey
fld public final static org.netbeans.modules.css.lib.api.CssColor dodgerblue
fld public final static org.netbeans.modules.css.lib.api.CssColor firebrick
fld public final static org.netbeans.modules.css.lib.api.CssColor floralwhite
fld public final static org.netbeans.modules.css.lib.api.CssColor forestgreen
fld public final static org.netbeans.modules.css.lib.api.CssColor fuchsia
fld public final static org.netbeans.modules.css.lib.api.CssColor gainsboro
fld public final static org.netbeans.modules.css.lib.api.CssColor ghostwhite
fld public final static org.netbeans.modules.css.lib.api.CssColor gold
fld public final static org.netbeans.modules.css.lib.api.CssColor goldenrod
fld public final static org.netbeans.modules.css.lib.api.CssColor gray
fld public final static org.netbeans.modules.css.lib.api.CssColor green
fld public final static org.netbeans.modules.css.lib.api.CssColor greenyellow
fld public final static org.netbeans.modules.css.lib.api.CssColor grey
fld public final static org.netbeans.modules.css.lib.api.CssColor honeydew
fld public final static org.netbeans.modules.css.lib.api.CssColor hotpink
fld public final static org.netbeans.modules.css.lib.api.CssColor indianred
fld public final static org.netbeans.modules.css.lib.api.CssColor indigo
fld public final static org.netbeans.modules.css.lib.api.CssColor ivory
fld public final static org.netbeans.modules.css.lib.api.CssColor khaki
fld public final static org.netbeans.modules.css.lib.api.CssColor lavender
fld public final static org.netbeans.modules.css.lib.api.CssColor lavenderblush
fld public final static org.netbeans.modules.css.lib.api.CssColor lawngreen
fld public final static org.netbeans.modules.css.lib.api.CssColor lemonchiffon
fld public final static org.netbeans.modules.css.lib.api.CssColor lightblue
fld public final static org.netbeans.modules.css.lib.api.CssColor lightcoral
fld public final static org.netbeans.modules.css.lib.api.CssColor lightcyan
fld public final static org.netbeans.modules.css.lib.api.CssColor lightgoldenrodyellow
fld public final static org.netbeans.modules.css.lib.api.CssColor lightgray
fld public final static org.netbeans.modules.css.lib.api.CssColor lightgreen
fld public final static org.netbeans.modules.css.lib.api.CssColor lightgrey
fld public final static org.netbeans.modules.css.lib.api.CssColor lightpink
fld public final static org.netbeans.modules.css.lib.api.CssColor lightsalmon
fld public final static org.netbeans.modules.css.lib.api.CssColor lightseagreen
fld public final static org.netbeans.modules.css.lib.api.CssColor lightskyblue
fld public final static org.netbeans.modules.css.lib.api.CssColor lightslategray
fld public final static org.netbeans.modules.css.lib.api.CssColor lightslategrey
fld public final static org.netbeans.modules.css.lib.api.CssColor lightsteelblue
fld public final static org.netbeans.modules.css.lib.api.CssColor lightyellow
fld public final static org.netbeans.modules.css.lib.api.CssColor lime
fld public final static org.netbeans.modules.css.lib.api.CssColor limegreen
fld public final static org.netbeans.modules.css.lib.api.CssColor linen
fld public final static org.netbeans.modules.css.lib.api.CssColor magenta
fld public final static org.netbeans.modules.css.lib.api.CssColor maroon
fld public final static org.netbeans.modules.css.lib.api.CssColor mediumaquamarine
fld public final static org.netbeans.modules.css.lib.api.CssColor mediumblue
fld public final static org.netbeans.modules.css.lib.api.CssColor mediumorchid
fld public final static org.netbeans.modules.css.lib.api.CssColor mediumpurple
fld public final static org.netbeans.modules.css.lib.api.CssColor mediumseagreen
fld public final static org.netbeans.modules.css.lib.api.CssColor mediumslateblue
fld public final static org.netbeans.modules.css.lib.api.CssColor mediumspringgreen
fld public final static org.netbeans.modules.css.lib.api.CssColor mediumturquoise
fld public final static org.netbeans.modules.css.lib.api.CssColor mediumvioletred
fld public final static org.netbeans.modules.css.lib.api.CssColor midnightblue
fld public final static org.netbeans.modules.css.lib.api.CssColor mintcream
fld public final static org.netbeans.modules.css.lib.api.CssColor mistyrose
fld public final static org.netbeans.modules.css.lib.api.CssColor moccasin
fld public final static org.netbeans.modules.css.lib.api.CssColor navajowhite
fld public final static org.netbeans.modules.css.lib.api.CssColor navy
fld public final static org.netbeans.modules.css.lib.api.CssColor oldlace
fld public final static org.netbeans.modules.css.lib.api.CssColor olive
fld public final static org.netbeans.modules.css.lib.api.CssColor olivedrab
fld public final static org.netbeans.modules.css.lib.api.CssColor orange
fld public final static org.netbeans.modules.css.lib.api.CssColor orangered
fld public final static org.netbeans.modules.css.lib.api.CssColor orchid
fld public final static org.netbeans.modules.css.lib.api.CssColor palegoldenrod
fld public final static org.netbeans.modules.css.lib.api.CssColor palegreen
fld public final static org.netbeans.modules.css.lib.api.CssColor paleturquoise
fld public final static org.netbeans.modules.css.lib.api.CssColor palevioletred
fld public final static org.netbeans.modules.css.lib.api.CssColor papayawhip
fld public final static org.netbeans.modules.css.lib.api.CssColor peachpuff
fld public final static org.netbeans.modules.css.lib.api.CssColor peru
fld public final static org.netbeans.modules.css.lib.api.CssColor pink
fld public final static org.netbeans.modules.css.lib.api.CssColor plum
fld public final static org.netbeans.modules.css.lib.api.CssColor powderblue
fld public final static org.netbeans.modules.css.lib.api.CssColor purple
fld public final static org.netbeans.modules.css.lib.api.CssColor red
fld public final static org.netbeans.modules.css.lib.api.CssColor rosybrown
fld public final static org.netbeans.modules.css.lib.api.CssColor royalblue
fld public final static org.netbeans.modules.css.lib.api.CssColor saddlebrown
fld public final static org.netbeans.modules.css.lib.api.CssColor salmon
fld public final static org.netbeans.modules.css.lib.api.CssColor sandybrown
fld public final static org.netbeans.modules.css.lib.api.CssColor seagreen
fld public final static org.netbeans.modules.css.lib.api.CssColor seashell
fld public final static org.netbeans.modules.css.lib.api.CssColor sienna
fld public final static org.netbeans.modules.css.lib.api.CssColor silver
fld public final static org.netbeans.modules.css.lib.api.CssColor skyblue
fld public final static org.netbeans.modules.css.lib.api.CssColor slateblue
fld public final static org.netbeans.modules.css.lib.api.CssColor slategray
fld public final static org.netbeans.modules.css.lib.api.CssColor slategrey
fld public final static org.netbeans.modules.css.lib.api.CssColor snow
fld public final static org.netbeans.modules.css.lib.api.CssColor springgreen
fld public final static org.netbeans.modules.css.lib.api.CssColor steelblue
fld public final static org.netbeans.modules.css.lib.api.CssColor tan
fld public final static org.netbeans.modules.css.lib.api.CssColor teal
fld public final static org.netbeans.modules.css.lib.api.CssColor thistle
fld public final static org.netbeans.modules.css.lib.api.CssColor tomato
fld public final static org.netbeans.modules.css.lib.api.CssColor turquoise
fld public final static org.netbeans.modules.css.lib.api.CssColor violet
fld public final static org.netbeans.modules.css.lib.api.CssColor wheat
fld public final static org.netbeans.modules.css.lib.api.CssColor white
fld public final static org.netbeans.modules.css.lib.api.CssColor whitesmoke
fld public final static org.netbeans.modules.css.lib.api.CssColor yellow
fld public final static org.netbeans.modules.css.lib.api.CssColor yellowgreen
meth public java.lang.String colorCode()
meth public static org.netbeans.modules.css.lib.api.CssColor getColor(java.lang.String)
meth public static org.netbeans.modules.css.lib.api.CssColor valueOf(java.lang.String)
meth public static org.netbeans.modules.css.lib.api.CssColor[] values()
supr java.lang.Enum<org.netbeans.modules.css.lib.api.CssColor>
hfds code,colorsMap

CLSS public abstract interface org.netbeans.modules.css.lib.api.CssModule
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getSpecificationURL()

CLSS public org.netbeans.modules.css.lib.api.CssParserFactory
cons public init()
meth public org.netbeans.modules.parsing.spi.Parser createParser(java.util.Collection<org.netbeans.modules.parsing.api.Snapshot>)
meth public static org.netbeans.modules.parsing.spi.ParserFactory getDefault()
supr org.netbeans.modules.parsing.spi.ParserFactory
hfds INSTANCE

CLSS public org.netbeans.modules.css.lib.api.CssParserResult
cons public init(org.netbeans.modules.parsing.api.Snapshot,org.netbeans.modules.css.lib.AbstractParseTreeNode,java.util.List<org.netbeans.modules.css.lib.api.ProblemDescription>)
fld public static boolean IN_UNIT_TESTS
meth protected void invalidate()
meth public <%0 extends java.lang.Object> void setProperty(java.lang.Class<{%%0}>,{%%0})
meth public <%0 extends java.lang.Object> {%%0} getProperty(java.lang.Class<{%%0}>)
meth public java.util.List<? extends org.netbeans.modules.css.lib.api.FilterableError> getDiagnostics()
meth public java.util.List<? extends org.netbeans.modules.css.lib.api.FilterableError> getDiagnostics(boolean)
meth public java.util.List<org.netbeans.modules.css.lib.api.ProblemDescription> getParserDiagnostics()
meth public org.netbeans.modules.css.lib.api.Node getParseTree()
supr org.netbeans.modules.csl.spi.ParserResult
hfds diagnostics,parseTree,properties

CLSS public final !enum org.netbeans.modules.css.lib.api.CssTokenId
fld public final static org.netbeans.modules.css.lib.api.CssTokenId A
fld public final static org.netbeans.modules.css.lib.api.CssTokenId ANGLE
fld public final static org.netbeans.modules.css.lib.api.CssTokenId AT_IDENT
fld public final static org.netbeans.modules.css.lib.api.CssTokenId AT_SIGN
fld public final static org.netbeans.modules.css.lib.api.CssTokenId B
fld public final static org.netbeans.modules.css.lib.api.CssTokenId BEGINS
fld public final static org.netbeans.modules.css.lib.api.CssTokenId BOTTOMCENTER_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId BOTTOMLEFTCORNER_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId BOTTOMLEFT_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId BOTTOMRIGHTCORNER_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId BOTTOMRIGHT_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId C
fld public final static org.netbeans.modules.css.lib.api.CssTokenId CDC
fld public final static org.netbeans.modules.css.lib.api.CssTokenId CDO
fld public final static org.netbeans.modules.css.lib.api.CssTokenId CHARSET_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId COLON
fld public final static org.netbeans.modules.css.lib.api.CssTokenId COMMA
fld public final static org.netbeans.modules.css.lib.api.CssTokenId COMMENT
fld public final static org.netbeans.modules.css.lib.api.CssTokenId CONTAINER_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId CONTAINS
fld public final static org.netbeans.modules.css.lib.api.CssTokenId COUNTER_STYLE_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId CP_DOTS
fld public final static org.netbeans.modules.css.lib.api.CssTokenId CP_EQ
fld public final static org.netbeans.modules.css.lib.api.CssTokenId CP_NOT_EQ
fld public final static org.netbeans.modules.css.lib.api.CssTokenId D
fld public final static org.netbeans.modules.css.lib.api.CssTokenId DASHMATCH
fld public final static org.netbeans.modules.css.lib.api.CssTokenId DCOLON
fld public final static org.netbeans.modules.css.lib.api.CssTokenId DIMENSION
fld public final static org.netbeans.modules.css.lib.api.CssTokenId DOT
fld public final static org.netbeans.modules.css.lib.api.CssTokenId E
fld public final static org.netbeans.modules.css.lib.api.CssTokenId EMS
fld public final static org.netbeans.modules.css.lib.api.CssTokenId ENDS
fld public final static org.netbeans.modules.css.lib.api.CssTokenId EOF
fld public final static org.netbeans.modules.css.lib.api.CssTokenId ERROR
fld public final static org.netbeans.modules.css.lib.api.CssTokenId ESCAPE
fld public final static org.netbeans.modules.css.lib.api.CssTokenId EXCLAMATION_MARK
fld public final static org.netbeans.modules.css.lib.api.CssTokenId EXS
fld public final static org.netbeans.modules.css.lib.api.CssTokenId F
fld public final static org.netbeans.modules.css.lib.api.CssTokenId FONT_FACE_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId FREQ
fld public final static org.netbeans.modules.css.lib.api.CssTokenId G
fld public final static org.netbeans.modules.css.lib.api.CssTokenId GEN
fld public final static org.netbeans.modules.css.lib.api.CssTokenId GREATER
fld public final static org.netbeans.modules.css.lib.api.CssTokenId GREATER_OR_EQ
fld public final static org.netbeans.modules.css.lib.api.CssTokenId H
fld public final static org.netbeans.modules.css.lib.api.CssTokenId HASH
fld public final static org.netbeans.modules.css.lib.api.CssTokenId HASH_SYMBOL
fld public final static org.netbeans.modules.css.lib.api.CssTokenId HEXCHAR
fld public final static org.netbeans.modules.css.lib.api.CssTokenId HEXCHAR_WILDCARD
fld public final static org.netbeans.modules.css.lib.api.CssTokenId I
fld public final static org.netbeans.modules.css.lib.api.CssTokenId IDENT
fld public final static org.netbeans.modules.css.lib.api.CssTokenId IMPORTANT_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId IMPORT_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId INCLUDES
fld public final static org.netbeans.modules.css.lib.api.CssTokenId INVALID
fld public final static org.netbeans.modules.css.lib.api.CssTokenId J
fld public final static org.netbeans.modules.css.lib.api.CssTokenId K
fld public final static org.netbeans.modules.css.lib.api.CssTokenId L
fld public final static org.netbeans.modules.css.lib.api.CssTokenId LAYER_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId LBRACE
fld public final static org.netbeans.modules.css.lib.api.CssTokenId LBRACKET
fld public final static org.netbeans.modules.css.lib.api.CssTokenId LEFTBOTTOM_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId LEFTMIDDLE_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId LEFTTOP_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId LENGTH
fld public final static org.netbeans.modules.css.lib.api.CssTokenId LESS
fld public final static org.netbeans.modules.css.lib.api.CssTokenId LESS_AND
fld public final static org.netbeans.modules.css.lib.api.CssTokenId LESS_JS_STRING
fld public final static org.netbeans.modules.css.lib.api.CssTokenId LESS_OR_EQ
fld public final static org.netbeans.modules.css.lib.api.CssTokenId LESS_REST
fld public final static org.netbeans.modules.css.lib.api.CssTokenId LINE_COMMENT
fld public final static org.netbeans.modules.css.lib.api.CssTokenId LPAREN
fld public final static org.netbeans.modules.css.lib.api.CssTokenId M
fld public final static org.netbeans.modules.css.lib.api.CssTokenId MEDIA_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId MINUS
fld public final static org.netbeans.modules.css.lib.api.CssTokenId MOZ_DOCUMENT_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId MOZ_DOMAIN
fld public final static org.netbeans.modules.css.lib.api.CssTokenId MOZ_REGEXP
fld public final static org.netbeans.modules.css.lib.api.CssTokenId MOZ_URL_PREFIX
fld public final static org.netbeans.modules.css.lib.api.CssTokenId N
fld public final static org.netbeans.modules.css.lib.api.CssTokenId NAME
fld public final static org.netbeans.modules.css.lib.api.CssTokenId NAMESPACE_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId NL
fld public final static org.netbeans.modules.css.lib.api.CssTokenId NMCHAR
fld public final static org.netbeans.modules.css.lib.api.CssTokenId NMSTART
fld public final static org.netbeans.modules.css.lib.api.CssTokenId NONASCII
fld public final static org.netbeans.modules.css.lib.api.CssTokenId NOT
fld public final static org.netbeans.modules.css.lib.api.CssTokenId NUMBER
fld public final static org.netbeans.modules.css.lib.api.CssTokenId O
fld public final static org.netbeans.modules.css.lib.api.CssTokenId OPEQ
fld public final static org.netbeans.modules.css.lib.api.CssTokenId P
fld public final static org.netbeans.modules.css.lib.api.CssTokenId PAGE_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId PERCENTAGE
fld public final static org.netbeans.modules.css.lib.api.CssTokenId PERCENTAGE_SYMBOL
fld public final static org.netbeans.modules.css.lib.api.CssTokenId PIPE
fld public final static org.netbeans.modules.css.lib.api.CssTokenId PLUS
fld public final static org.netbeans.modules.css.lib.api.CssTokenId Q
fld public final static org.netbeans.modules.css.lib.api.CssTokenId R
fld public final static org.netbeans.modules.css.lib.api.CssTokenId RBRACE
fld public final static org.netbeans.modules.css.lib.api.CssTokenId RBRACKET
fld public final static org.netbeans.modules.css.lib.api.CssTokenId REM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId RESOLUTION
fld public final static org.netbeans.modules.css.lib.api.CssTokenId RIGHTBOTTOM_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId RIGHTMIDDLE_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId RIGHTTOP_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId RPAREN
fld public final static org.netbeans.modules.css.lib.api.CssTokenId S
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_AT_ROOT
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_CONTENT
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_DEBUG
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_DEFAULT
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_EACH
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_ELSE
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_ELSEIF
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_ERROR
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_EXTEND
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_EXTEND_ONLY_SELECTOR
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_FOR
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_FORWARD
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_FUNCTION
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_GLOBAL
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_IF
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_INCLUDE
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_MIXIN
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_OPTIONAL
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_RETURN
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_USE
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_VAR
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_WARN
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SASS_WHILE
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SEMI
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SOLIDUS
fld public final static org.netbeans.modules.css.lib.api.CssTokenId STAR
fld public final static org.netbeans.modules.css.lib.api.CssTokenId STRING
fld public final static org.netbeans.modules.css.lib.api.CssTokenId SUPPORTS_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId T
fld public final static org.netbeans.modules.css.lib.api.CssTokenId TILDE
fld public final static org.netbeans.modules.css.lib.api.CssTokenId TIME
fld public final static org.netbeans.modules.css.lib.api.CssTokenId TOPCENTER_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId TOPLEFTCORNER_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId TOPLEFT_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId TOPRIGHTCORNER_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId TOPRIGHT_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId U
fld public final static org.netbeans.modules.css.lib.api.CssTokenId UNICODE
fld public final static org.netbeans.modules.css.lib.api.CssTokenId URANGE
fld public final static org.netbeans.modules.css.lib.api.CssTokenId URI
fld public final static org.netbeans.modules.css.lib.api.CssTokenId URL
fld public final static org.netbeans.modules.css.lib.api.CssTokenId V
fld public final static org.netbeans.modules.css.lib.api.CssTokenId VARIABLE
fld public final static org.netbeans.modules.css.lib.api.CssTokenId W
fld public final static org.netbeans.modules.css.lib.api.CssTokenId WEBKIT_KEYFRAMES_SYM
fld public final static org.netbeans.modules.css.lib.api.CssTokenId WS
fld public final static org.netbeans.modules.css.lib.api.CssTokenId X
fld public final static org.netbeans.modules.css.lib.api.CssTokenId Y
fld public final static org.netbeans.modules.css.lib.api.CssTokenId Z
intf org.netbeans.api.lexer.TokenId
meth public boolean matchesInput(java.lang.CharSequence)
meth public java.lang.String primaryCategory()
meth public org.netbeans.modules.css.lib.api.CssTokenIdCategory getTokenCategory()
meth public static org.netbeans.api.lexer.Language<org.netbeans.modules.css.lib.api.CssTokenId> language()
meth public static org.netbeans.modules.css.lib.api.CssTokenId forTokenTypeCode(int)
meth public static org.netbeans.modules.css.lib.api.CssTokenId valueOf(java.lang.String)
meth public static org.netbeans.modules.css.lib.api.CssTokenId[] values()
supr java.lang.Enum<org.netbeans.modules.css.lib.api.CssTokenId>
hfds code,codesMap,language,primaryCategory

CLSS public final !enum org.netbeans.modules.css.lib.api.CssTokenIdCategory
fld public final static org.netbeans.modules.css.lib.api.CssTokenIdCategory AT_RULE_SYMBOL
fld public final static org.netbeans.modules.css.lib.api.CssTokenIdCategory BRACES
fld public final static org.netbeans.modules.css.lib.api.CssTokenIdCategory COMMENTS
fld public final static org.netbeans.modules.css.lib.api.CssTokenIdCategory ERRORS
fld public final static org.netbeans.modules.css.lib.api.CssTokenIdCategory HASHES
fld public final static org.netbeans.modules.css.lib.api.CssTokenIdCategory IDENTIFIERS
fld public final static org.netbeans.modules.css.lib.api.CssTokenIdCategory KEYWORDS
fld public final static org.netbeans.modules.css.lib.api.CssTokenIdCategory NUMBERS
fld public final static org.netbeans.modules.css.lib.api.CssTokenIdCategory OPERATORS
fld public final static org.netbeans.modules.css.lib.api.CssTokenIdCategory OTHERS
fld public final static org.netbeans.modules.css.lib.api.CssTokenIdCategory SEPARATORS
fld public final static org.netbeans.modules.css.lib.api.CssTokenIdCategory STRINGS
fld public final static org.netbeans.modules.css.lib.api.CssTokenIdCategory URIS
fld public final static org.netbeans.modules.css.lib.api.CssTokenIdCategory WHITESPACES
meth public static org.netbeans.modules.css.lib.api.CssTokenIdCategory valueOf(java.lang.String)
meth public static org.netbeans.modules.css.lib.api.CssTokenIdCategory[] values()
supr java.lang.Enum<org.netbeans.modules.css.lib.api.CssTokenIdCategory>

CLSS public abstract interface org.netbeans.modules.css.lib.api.ErrorsProvider
meth public abstract java.util.List<? extends org.netbeans.modules.css.lib.api.FilterableError> getExtendedDiagnostics(org.netbeans.modules.css.lib.api.CssParserResult)

CLSS public abstract interface org.netbeans.modules.css.lib.api.FilterableError
innr public abstract interface static SetFilterAction
intf org.netbeans.modules.csl.api.Error
meth public abstract boolean isFiltered()
meth public abstract java.util.Collection<org.netbeans.modules.css.lib.api.FilterableError$SetFilterAction> getEnableFilterActions()
meth public abstract org.netbeans.modules.css.lib.api.FilterableError$SetFilterAction getDisableFilterAction()

CLSS public abstract interface static org.netbeans.modules.css.lib.api.FilterableError$SetFilterAction
 outer org.netbeans.modules.css.lib.api.FilterableError
intf java.lang.Runnable
meth public abstract java.lang.String getDisplayName()

CLSS public abstract interface org.netbeans.modules.css.lib.api.Node
meth public abstract int from()
meth public abstract int to()
meth public abstract java.lang.CharSequence image()
meth public abstract java.lang.String name()
meth public abstract java.util.List<org.netbeans.modules.css.lib.api.Node> children()
meth public abstract org.netbeans.modules.css.lib.api.Node parent()
meth public abstract org.netbeans.modules.css.lib.api.NodeType type()
meth public java.lang.String unescapedImage()

CLSS public final !enum org.netbeans.modules.css.lib.api.NodeType
fld public final static org.netbeans.modules.css.lib.api.NodeType atRuleId
fld public final static org.netbeans.modules.css.lib.api.NodeType at_rule
fld public final static org.netbeans.modules.css.lib.api.NodeType body
fld public final static org.netbeans.modules.css.lib.api.NodeType bodyItem
fld public final static org.netbeans.modules.css.lib.api.NodeType braceBlock
fld public final static org.netbeans.modules.css.lib.api.NodeType bracketBlock
fld public final static org.netbeans.modules.css.lib.api.NodeType charSet
fld public final static org.netbeans.modules.css.lib.api.NodeType charSetValue
fld public final static org.netbeans.modules.css.lib.api.NodeType combinator
fld public final static org.netbeans.modules.css.lib.api.NodeType componentValue
fld public final static org.netbeans.modules.css.lib.api.NodeType componentValueOuter
fld public final static org.netbeans.modules.css.lib.api.NodeType containerAtRule
fld public final static org.netbeans.modules.css.lib.api.NodeType containerCondition
fld public final static org.netbeans.modules.css.lib.api.NodeType containerName
fld public final static org.netbeans.modules.css.lib.api.NodeType containerQueryConjunction
fld public final static org.netbeans.modules.css.lib.api.NodeType containerQueryDisjunction
fld public final static org.netbeans.modules.css.lib.api.NodeType containerQueryInParens
fld public final static org.netbeans.modules.css.lib.api.NodeType containerQueryWithOperator
fld public final static org.netbeans.modules.css.lib.api.NodeType counterStyle
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_arg
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_args_list
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_expression
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_expression_atom
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_expression_list
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_expression_operator
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_math_expression
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_math_expression_atom
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_math_expressions
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_mixin_block
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_mixin_call
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_mixin_call_arg
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_mixin_call_args
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_mixin_declaration
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_mixin_name
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_propertyValue
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_term_symbol
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_variable
fld public final static org.netbeans.modules.css.lib.api.NodeType cp_variable_declaration
fld public final static org.netbeans.modules.css.lib.api.NodeType cssClass
fld public final static org.netbeans.modules.css.lib.api.NodeType cssId
fld public final static org.netbeans.modules.css.lib.api.NodeType declaration
fld public final static org.netbeans.modules.css.lib.api.NodeType declarations
fld public final static org.netbeans.modules.css.lib.api.NodeType elementName
fld public final static org.netbeans.modules.css.lib.api.NodeType elementSubsequent
fld public final static org.netbeans.modules.css.lib.api.NodeType error
fld public final static org.netbeans.modules.css.lib.api.NodeType esPred
fld public final static org.netbeans.modules.css.lib.api.NodeType expression
fld public final static org.netbeans.modules.css.lib.api.NodeType fnAttribute
fld public final static org.netbeans.modules.css.lib.api.NodeType fnAttributeName
fld public final static org.netbeans.modules.css.lib.api.NodeType fnAttributeValue
fld public final static org.netbeans.modules.css.lib.api.NodeType fnAttributes
fld public final static org.netbeans.modules.css.lib.api.NodeType fontFace
fld public final static org.netbeans.modules.css.lib.api.NodeType function
fld public final static org.netbeans.modules.css.lib.api.NodeType functionName
fld public final static org.netbeans.modules.css.lib.api.NodeType generic_at_rule
fld public final static org.netbeans.modules.css.lib.api.NodeType hexColor
fld public final static org.netbeans.modules.css.lib.api.NodeType importItem
fld public final static org.netbeans.modules.css.lib.api.NodeType importLayer
fld public final static org.netbeans.modules.css.lib.api.NodeType imports
fld public final static org.netbeans.modules.css.lib.api.NodeType invalidRule
fld public final static org.netbeans.modules.css.lib.api.NodeType key_and
fld public final static org.netbeans.modules.css.lib.api.NodeType key_only
fld public final static org.netbeans.modules.css.lib.api.NodeType key_or
fld public final static org.netbeans.modules.css.lib.api.NodeType layerAtRule
fld public final static org.netbeans.modules.css.lib.api.NodeType layerBlock
fld public final static org.netbeans.modules.css.lib.api.NodeType layerBody
fld public final static org.netbeans.modules.css.lib.api.NodeType layerName
fld public final static org.netbeans.modules.css.lib.api.NodeType layerStatement
fld public final static org.netbeans.modules.css.lib.api.NodeType less_condition
fld public final static org.netbeans.modules.css.lib.api.NodeType less_condition_operator
fld public final static org.netbeans.modules.css.lib.api.NodeType less_fn_name
fld public final static org.netbeans.modules.css.lib.api.NodeType less_function_in_condition
fld public final static org.netbeans.modules.css.lib.api.NodeType less_import_types
fld public final static org.netbeans.modules.css.lib.api.NodeType less_mixin_guarded
fld public final static org.netbeans.modules.css.lib.api.NodeType less_selector_interpolation
fld public final static org.netbeans.modules.css.lib.api.NodeType less_selector_interpolation_exp
fld public final static org.netbeans.modules.css.lib.api.NodeType less_when
fld public final static org.netbeans.modules.css.lib.api.NodeType margin
fld public final static org.netbeans.modules.css.lib.api.NodeType margin_sym
fld public final static org.netbeans.modules.css.lib.api.NodeType media
fld public final static org.netbeans.modules.css.lib.api.NodeType mediaBody
fld public final static org.netbeans.modules.css.lib.api.NodeType mediaBodyItem
fld public final static org.netbeans.modules.css.lib.api.NodeType mediaExpression
fld public final static org.netbeans.modules.css.lib.api.NodeType mediaFeature
fld public final static org.netbeans.modules.css.lib.api.NodeType mediaFeatureValue
fld public final static org.netbeans.modules.css.lib.api.NodeType mediaQuery
fld public final static org.netbeans.modules.css.lib.api.NodeType mediaQueryList
fld public final static org.netbeans.modules.css.lib.api.NodeType mediaQueryOperator
fld public final static org.netbeans.modules.css.lib.api.NodeType mediaType
fld public final static org.netbeans.modules.css.lib.api.NodeType moz_document
fld public final static org.netbeans.modules.css.lib.api.NodeType moz_document_function
fld public final static org.netbeans.modules.css.lib.api.NodeType namespace
fld public final static org.netbeans.modules.css.lib.api.NodeType namespacePrefix
fld public final static org.netbeans.modules.css.lib.api.NodeType namespacePrefixName
fld public final static org.netbeans.modules.css.lib.api.NodeType namespaces
fld public final static org.netbeans.modules.css.lib.api.NodeType operator
fld public final static org.netbeans.modules.css.lib.api.NodeType page
fld public final static org.netbeans.modules.css.lib.api.NodeType parenBlock
fld public final static org.netbeans.modules.css.lib.api.NodeType preservedToken
fld public final static org.netbeans.modules.css.lib.api.NodeType preservedTokenTopLevel
fld public final static org.netbeans.modules.css.lib.api.NodeType prio
fld public final static org.netbeans.modules.css.lib.api.NodeType property
fld public final static org.netbeans.modules.css.lib.api.NodeType propertyDeclaration
fld public final static org.netbeans.modules.css.lib.api.NodeType propertyValue
fld public final static org.netbeans.modules.css.lib.api.NodeType pseudo
fld public final static org.netbeans.modules.css.lib.api.NodeType pseudoPage
fld public final static org.netbeans.modules.css.lib.api.NodeType recovery
fld public final static org.netbeans.modules.css.lib.api.NodeType resourceIdentifier
fld public final static org.netbeans.modules.css.lib.api.NodeType root
fld public final static org.netbeans.modules.css.lib.api.NodeType rule
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_content
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_control
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_control_block
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_control_expression
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_debug
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_each
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_each_variables
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_else
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_error
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_extend
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_extend_only_selector
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_for
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_forward
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_forward_as
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_forward_hide
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_forward_show
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_forward_with
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_forward_with_declaration
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_function_declaration
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_function_name
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_function_return
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_if
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_interpolation_expression_var
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_map
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_map_name
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_map_pair
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_map_pairs
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_nested_properties
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_selector_interpolation_exp
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_use
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_use_as
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_use_with
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_use_with_declaration
fld public final static org.netbeans.modules.css.lib.api.NodeType sass_while
fld public final static org.netbeans.modules.css.lib.api.NodeType selector
fld public final static org.netbeans.modules.css.lib.api.NodeType selectorsGroup
fld public final static org.netbeans.modules.css.lib.api.NodeType simpleSelectorSequence
fld public final static org.netbeans.modules.css.lib.api.NodeType sizeFeature
fld public final static org.netbeans.modules.css.lib.api.NodeType sizeFeatureFixedValue
fld public final static org.netbeans.modules.css.lib.api.NodeType sizeFeatureName
fld public final static org.netbeans.modules.css.lib.api.NodeType sizeFeatureRangeBetweenGt
fld public final static org.netbeans.modules.css.lib.api.NodeType sizeFeatureRangeBetweenLt
fld public final static org.netbeans.modules.css.lib.api.NodeType sizeFeatureRangeSingle
fld public final static org.netbeans.modules.css.lib.api.NodeType sizeFeatureValue
fld public final static org.netbeans.modules.css.lib.api.NodeType slAttribute
fld public final static org.netbeans.modules.css.lib.api.NodeType slAttributeName
fld public final static org.netbeans.modules.css.lib.api.NodeType slAttributeValue
fld public final static org.netbeans.modules.css.lib.api.NodeType styleCondition
fld public final static org.netbeans.modules.css.lib.api.NodeType styleConditionWithOperator
fld public final static org.netbeans.modules.css.lib.api.NodeType styleFeature
fld public final static org.netbeans.modules.css.lib.api.NodeType styleInParens
fld public final static org.netbeans.modules.css.lib.api.NodeType styleQuery
fld public final static org.netbeans.modules.css.lib.api.NodeType styleQueryConjunction
fld public final static org.netbeans.modules.css.lib.api.NodeType styleQueryDisjunction
fld public final static org.netbeans.modules.css.lib.api.NodeType styleSheet
fld public final static org.netbeans.modules.css.lib.api.NodeType supportsAtRule
fld public final static org.netbeans.modules.css.lib.api.NodeType supportsCondition
fld public final static org.netbeans.modules.css.lib.api.NodeType supportsConjunction
fld public final static org.netbeans.modules.css.lib.api.NodeType supportsDecl
fld public final static org.netbeans.modules.css.lib.api.NodeType supportsDisjunction
fld public final static org.netbeans.modules.css.lib.api.NodeType supportsFeature
fld public final static org.netbeans.modules.css.lib.api.NodeType supportsInParens
fld public final static org.netbeans.modules.css.lib.api.NodeType supportsWithOperator
fld public final static org.netbeans.modules.css.lib.api.NodeType syncToDeclarationsRule
fld public final static org.netbeans.modules.css.lib.api.NodeType syncToFollow
fld public final static org.netbeans.modules.css.lib.api.NodeType syncTo_RBRACE
fld public final static org.netbeans.modules.css.lib.api.NodeType syncTo_SEMI
fld public final static org.netbeans.modules.css.lib.api.NodeType synpred1_Css3
fld public final static org.netbeans.modules.css.lib.api.NodeType synpred2_Css3
fld public final static org.netbeans.modules.css.lib.api.NodeType synpred3_Css3
fld public final static org.netbeans.modules.css.lib.api.NodeType term
fld public final static org.netbeans.modules.css.lib.api.NodeType token
fld public final static org.netbeans.modules.css.lib.api.NodeType typeSelector
fld public final static org.netbeans.modules.css.lib.api.NodeType unaryOperator
fld public final static org.netbeans.modules.css.lib.api.NodeType vendorAtRule
fld public final static org.netbeans.modules.css.lib.api.NodeType webkitKeyframeSelectors
fld public final static org.netbeans.modules.css.lib.api.NodeType webkitKeyframes
fld public final static org.netbeans.modules.css.lib.api.NodeType webkitKeyframesBlock
fld public final static org.netbeans.modules.css.lib.api.NodeType ws
meth public static org.netbeans.modules.css.lib.api.NodeType valueOf(java.lang.String)
meth public static org.netbeans.modules.css.lib.api.NodeType[] values()
supr java.lang.Enum<org.netbeans.modules.css.lib.api.NodeType>

CLSS public final org.netbeans.modules.css.lib.api.NodeUtil
meth public !varargs static boolean isOfType(org.netbeans.modules.css.lib.api.Node,org.netbeans.modules.css.lib.api.NodeType[])
meth public !varargs static java.util.List<org.netbeans.modules.css.lib.api.Node> getChildrenRecursivelyByType(org.netbeans.modules.css.lib.api.Node,org.netbeans.modules.css.lib.api.NodeType[])
meth public static boolean containsError(org.netbeans.modules.css.lib.api.Node)
meth public static boolean isSelectorNode(org.netbeans.modules.css.lib.api.Node)
meth public static int[] getRuleBodyRange(org.netbeans.modules.css.lib.api.Node)
meth public static int[] getTrimmedNodeRange(org.netbeans.modules.css.lib.api.Node)
meth public static java.lang.String encodeToString(org.netbeans.modules.css.lib.api.TreePath)
meth public static java.lang.String getElementId(org.netbeans.modules.css.lib.api.Node)
meth public static java.lang.String unescape(java.lang.CharSequence)
meth public static org.netbeans.modules.css.lib.api.CssTokenId getTokenNodeTokenId(org.netbeans.modules.css.lib.api.Node)
meth public static org.netbeans.modules.css.lib.api.Node findNodeAtOffset(org.netbeans.modules.css.lib.api.Node,int)
meth public static org.netbeans.modules.css.lib.api.Node findNonTokenNodeAtOffset(org.netbeans.modules.css.lib.api.Node,int)
meth public static org.netbeans.modules.css.lib.api.Node getAncestorByType(org.netbeans.modules.css.lib.api.Node,org.netbeans.modules.css.lib.api.NodeType)
meth public static org.netbeans.modules.css.lib.api.Node getChildByType(org.netbeans.modules.css.lib.api.Node,org.netbeans.modules.css.lib.api.NodeType)
meth public static org.netbeans.modules.css.lib.api.Node getChildTokenNode(org.netbeans.modules.css.lib.api.Node,org.netbeans.modules.css.lib.api.CssTokenId)
meth public static org.netbeans.modules.css.lib.api.Node getSibling(org.netbeans.modules.css.lib.api.Node,boolean)
meth public static org.netbeans.modules.css.lib.api.Node query(org.netbeans.modules.css.lib.api.Node,java.lang.String)
meth public static org.netbeans.modules.css.lib.api.Node query(org.netbeans.modules.css.lib.api.Node,java.lang.String,boolean)
meth public static org.netbeans.modules.css.lib.api.Node[] getChildrenByType(org.netbeans.modules.css.lib.api.Node,org.netbeans.modules.css.lib.api.NodeType)
meth public static void dumpTree(org.netbeans.modules.css.lib.api.Node)
meth public static void dumpTree(org.netbeans.modules.css.lib.api.Node,java.io.PrintWriter)
supr java.lang.Object
hfds ELEMENT_PATH_ELEMENTS_DELIMITER,ELEMENT_PATH_INDEX_DELIMITER,ESCAPE,INDENT

CLSS public abstract org.netbeans.modules.css.lib.api.NodeVisitor<%0 extends java.lang.Object>
cons public init()
cons public init({org.netbeans.modules.css.lib.api.NodeVisitor%0})
meth protected boolean isCancelled()
meth public abstract boolean visit(org.netbeans.modules.css.lib.api.Node)
meth public org.netbeans.modules.css.lib.api.Node visitChildren(org.netbeans.modules.css.lib.api.Node)
meth public static <%0 extends java.lang.Object> void visitChildren(org.netbeans.modules.css.lib.api.Node,java.util.Collection<org.netbeans.modules.css.lib.api.NodeVisitor<{%%0}>>)
meth public void cancel()
meth public void visitAncestors(org.netbeans.modules.css.lib.api.Node)
meth public {org.netbeans.modules.css.lib.api.NodeVisitor%0} getResult()
supr java.lang.Object
hfds cancelled,result

CLSS public final org.netbeans.modules.css.lib.api.ProblemDescription
cons public init(int,int,java.lang.String,java.lang.String,org.netbeans.modules.css.lib.api.ProblemDescription$Type)
innr public final static !enum Keys
innr public final static !enum Type
meth public boolean equals(java.lang.Object)
meth public int getFrom()
meth public int getTo()
meth public int hashCode()
meth public java.lang.String getDescription()
meth public java.lang.String getKey()
meth public java.lang.String toString()
meth public org.netbeans.modules.css.lib.api.ProblemDescription$Type getType()
supr java.lang.Object
hfds description,from,key,to,type

CLSS public final static !enum org.netbeans.modules.css.lib.api.ProblemDescription$Keys
 outer org.netbeans.modules.css.lib.api.ProblemDescription
fld public final static org.netbeans.modules.css.lib.api.ProblemDescription$Keys AST
fld public final static org.netbeans.modules.css.lib.api.ProblemDescription$Keys LEXING
fld public final static org.netbeans.modules.css.lib.api.ProblemDescription$Keys PARSING
meth public static org.netbeans.modules.css.lib.api.ProblemDescription$Keys valueOf(java.lang.String)
meth public static org.netbeans.modules.css.lib.api.ProblemDescription$Keys[] values()
supr java.lang.Enum<org.netbeans.modules.css.lib.api.ProblemDescription$Keys>

CLSS public final static !enum org.netbeans.modules.css.lib.api.ProblemDescription$Type
 outer org.netbeans.modules.css.lib.api.ProblemDescription
fld public final static org.netbeans.modules.css.lib.api.ProblemDescription$Type ERROR
fld public final static org.netbeans.modules.css.lib.api.ProblemDescription$Type FATAL
fld public final static org.netbeans.modules.css.lib.api.ProblemDescription$Type INFO
fld public final static org.netbeans.modules.css.lib.api.ProblemDescription$Type WARNING
meth public static org.netbeans.modules.css.lib.api.ProblemDescription$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.css.lib.api.ProblemDescription$Type[] values()
supr java.lang.Enum<org.netbeans.modules.css.lib.api.ProblemDescription$Type>

CLSS public org.netbeans.modules.css.lib.api.TreePath
cons public init(org.netbeans.modules.css.lib.api.Node)
cons public init(org.netbeans.modules.css.lib.api.Node,org.netbeans.modules.css.lib.api.Node)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.css.lib.api.Node> path()
meth public org.netbeans.modules.css.lib.api.Node first()
meth public org.netbeans.modules.css.lib.api.Node last()
supr java.lang.Object
hfds first,last

CLSS public org.netbeans.modules.css.lib.api.properties.FixedTextGrammarElement
cons public init(org.netbeans.modules.css.lib.api.properties.GroupGrammarElement,java.lang.CharSequence,java.lang.String)
meth public boolean accepts(org.netbeans.modules.css.lib.api.properties.Token)
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public void accept(org.netbeans.modules.css.lib.api.properties.GrammarElementVisitor)
supr org.netbeans.modules.css.lib.api.properties.ValueGrammarElement
hfds value

CLSS public abstract org.netbeans.modules.css.lib.api.properties.GrammarElement
cons public init(org.netbeans.modules.css.lib.api.properties.GroupGrammarElement,java.lang.String)
fld public final static char INVISIBLE_PROPERTY_PREFIX = '@'
meth public abstract void accept(org.netbeans.modules.css.lib.api.properties.GrammarElementVisitor)
meth public boolean equals(java.lang.Object)
meth public boolean isOptional()
meth public int getMaximumOccurances()
meth public int getMinimumOccurances()
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String getVisibleOrigin()
meth public java.lang.String origin()
meth public java.lang.String path()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.css.lib.api.properties.GrammarElement> elementsPath()
meth public org.netbeans.modules.css.lib.api.properties.GroupGrammarElement parent()
meth public static boolean isArtificialElementName(java.lang.CharSequence)
meth public void setMaximumOccurances(int)
meth public void setMinimumOccurances(int)
meth public void setName(java.lang.String)
supr java.lang.Object
hfds maximum_occurances,minimum_occurances,name,parent,path

CLSS public abstract org.netbeans.modules.css.lib.api.properties.GrammarElementVisitor
cons public init()
meth public boolean visit(org.netbeans.modules.css.lib.api.properties.FixedTextGrammarElement)
meth public boolean visit(org.netbeans.modules.css.lib.api.properties.GroupGrammarElement)
meth public boolean visit(org.netbeans.modules.css.lib.api.properties.UnitGrammarElement)
meth public final boolean visit(org.netbeans.modules.css.lib.api.properties.GrammarElement)
supr java.lang.Object

CLSS public org.netbeans.modules.css.lib.api.properties.GrammarParseTreeConvertor
cons public init()
supr java.lang.Object

CLSS public org.netbeans.modules.css.lib.api.properties.GrammarResolver
cons public init(org.netbeans.modules.css.lib.api.properties.GroupGrammarElement)
innr public final static !enum Feature
innr public final static !enum Log
meth public java.lang.Object getFeature(org.netbeans.modules.css.lib.api.properties.GrammarResolver$Feature)
meth public org.netbeans.modules.css.lib.api.properties.GrammarResolverResult resolve(java.lang.CharSequence)
meth public static org.netbeans.modules.css.lib.api.properties.GrammarResolverResult resolve(org.netbeans.modules.css.lib.api.properties.GroupGrammarElement,java.lang.CharSequence)
meth public static void setLogging(org.netbeans.modules.css.lib.api.properties.GrammarResolver$Log,boolean)
meth public void addGrammarResolverListener(org.netbeans.modules.css.lib.api.properties.GrammarResolverListener)
meth public void disableFeture(org.netbeans.modules.css.lib.api.properties.GrammarResolver$Feature)
meth public void enableFeature(org.netbeans.modules.css.lib.api.properties.GrammarResolver$Feature)
meth public void removeGrammarResolverListener(org.netbeans.modules.css.lib.api.properties.GrammarResolverListener)
meth public void setFeature(org.netbeans.modules.css.lib.api.properties.GrammarResolver$Feature,java.lang.Object)
supr java.lang.Object
hfds FEATURES,LISTENERS,LOG,LOGGER,LOGGERS,globalValues,grammar,lastResolved,resolvedSomething,resolvedTokens,tokenizer
hcls InputState

CLSS public final static !enum org.netbeans.modules.css.lib.api.properties.GrammarResolver$Feature
 outer org.netbeans.modules.css.lib.api.properties.GrammarResolver
fld public final static org.netbeans.modules.css.lib.api.properties.GrammarResolver$Feature keepAnonymousElementsInParseTree
meth public static org.netbeans.modules.css.lib.api.properties.GrammarResolver$Feature valueOf(java.lang.String)
meth public static org.netbeans.modules.css.lib.api.properties.GrammarResolver$Feature[] values()
supr java.lang.Enum<org.netbeans.modules.css.lib.api.properties.GrammarResolver$Feature>

CLSS public final static !enum org.netbeans.modules.css.lib.api.properties.GrammarResolver$Log
 outer org.netbeans.modules.css.lib.api.properties.GrammarResolver
fld public final static org.netbeans.modules.css.lib.api.properties.GrammarResolver$Log ALTERNATIVES
fld public final static org.netbeans.modules.css.lib.api.properties.GrammarResolver$Log DEFAULT
fld public final static org.netbeans.modules.css.lib.api.properties.GrammarResolver$Log VALUES
meth public static org.netbeans.modules.css.lib.api.properties.GrammarResolver$Log valueOf(java.lang.String)
meth public static org.netbeans.modules.css.lib.api.properties.GrammarResolver$Log[] values()
supr java.lang.Enum<org.netbeans.modules.css.lib.api.properties.GrammarResolver$Log>

CLSS public abstract interface org.netbeans.modules.css.lib.api.properties.GrammarResolverListener
meth public abstract void accepted(org.netbeans.modules.css.lib.api.properties.GroupGrammarElement)
meth public abstract void accepted(org.netbeans.modules.css.lib.api.properties.ValueGrammarElement,org.netbeans.modules.css.lib.api.properties.ResolvedToken)
meth public abstract void entering(org.netbeans.modules.css.lib.api.properties.GroupGrammarElement)
meth public abstract void entering(org.netbeans.modules.css.lib.api.properties.ValueGrammarElement)
meth public abstract void finished()
meth public abstract void rejected(org.netbeans.modules.css.lib.api.properties.GroupGrammarElement)
meth public abstract void rejected(org.netbeans.modules.css.lib.api.properties.ValueGrammarElement)
meth public abstract void ruleChoosen(org.netbeans.modules.css.lib.api.properties.GroupGrammarElement,org.netbeans.modules.css.lib.api.properties.GrammarElement)
meth public abstract void starting()

CLSS public final org.netbeans.modules.css.lib.api.properties.GrammarResolverResult
cons public init(org.netbeans.modules.css.lib.api.properties.Tokenizer,boolean,java.util.List<org.netbeans.modules.css.lib.api.properties.ResolvedToken>,java.util.Set<org.netbeans.modules.css.lib.api.properties.ValueGrammarElement>,org.netbeans.modules.css.lib.api.properties.Node)
meth public boolean success()
meth public java.util.List<org.netbeans.modules.css.lib.api.properties.ResolvedToken> resolved()
meth public java.util.List<org.netbeans.modules.css.lib.api.properties.Token> left()
meth public java.util.List<org.netbeans.modules.css.lib.api.properties.Token> tokens()
meth public java.util.Set<org.netbeans.modules.css.lib.api.properties.ValueGrammarElement> getAlternatives()
meth public org.netbeans.modules.css.lib.api.properties.Node getParseTree()
supr java.lang.Object
hfds alternatives,inputResolved,parseTreeRoot,resolvedTokens,tokenizer

CLSS public org.netbeans.modules.css.lib.api.properties.GroupGrammarElement
cons public init(org.netbeans.modules.css.lib.api.properties.GroupGrammarElement,int)
cons public init(org.netbeans.modules.css.lib.api.properties.GroupGrammarElement,int,java.lang.String)
innr public final static !enum Type
meth public boolean isVisible()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.css.lib.api.properties.GrammarElement> elements()
meth public java.util.List<org.netbeans.modules.css.lib.api.properties.GrammarElement> getAllPossibleValues()
meth public org.netbeans.modules.css.lib.api.properties.GroupGrammarElement$Type getType()
meth public void accept(org.netbeans.modules.css.lib.api.properties.GrammarElementVisitor)
meth public void addElement(org.netbeans.modules.css.lib.api.properties.GrammarElement)
meth public void setType(org.netbeans.modules.css.lib.api.properties.GroupGrammarElement$Type)
supr org.netbeans.modules.css.lib.api.properties.GrammarElement
hfds elements,index,type

CLSS public final static !enum org.netbeans.modules.css.lib.api.properties.GroupGrammarElement$Type
 outer org.netbeans.modules.css.lib.api.properties.GroupGrammarElement
fld public final static org.netbeans.modules.css.lib.api.properties.GroupGrammarElement$Type ALL
fld public final static org.netbeans.modules.css.lib.api.properties.GroupGrammarElement$Type COLLECTION
fld public final static org.netbeans.modules.css.lib.api.properties.GroupGrammarElement$Type LIST
fld public final static org.netbeans.modules.css.lib.api.properties.GroupGrammarElement$Type SET
meth public static org.netbeans.modules.css.lib.api.properties.GroupGrammarElement$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.css.lib.api.properties.GroupGrammarElement$Type[] values()
supr java.lang.Enum<org.netbeans.modules.css.lib.api.properties.GroupGrammarElement$Type>

CLSS public abstract interface org.netbeans.modules.css.lib.api.properties.GroupNode
intf org.netbeans.modules.css.lib.api.properties.Node
meth public abstract org.netbeans.modules.css.lib.api.properties.GrammarElement getGrammarElement()

CLSS public abstract interface org.netbeans.modules.css.lib.api.properties.Node
innr public abstract static AbstractNode
innr public static GroupNodeImpl
innr public static ResolvedTokenNode
meth public abstract java.lang.CharSequence image()
meth public abstract java.lang.String name()
meth public abstract java.util.Collection<org.netbeans.modules.css.lib.api.properties.Node> children()
meth public abstract void accept(org.netbeans.modules.css.lib.api.properties.NodeVisitor)
meth public abstract void accept(org.netbeans.modules.css.lib.api.properties.NodeVisitor2)

CLSS public abstract static org.netbeans.modules.css.lib.api.properties.Node$AbstractNode
 outer org.netbeans.modules.css.lib.api.properties.Node
cons public init()
intf org.netbeans.modules.css.lib.api.properties.Node
meth public void accept(org.netbeans.modules.css.lib.api.properties.NodeVisitor)
supr java.lang.Object

CLSS public static org.netbeans.modules.css.lib.api.properties.Node$GroupNodeImpl
 outer org.netbeans.modules.css.lib.api.properties.Node
cons public init(org.netbeans.modules.css.lib.api.properties.GrammarElement)
intf org.netbeans.modules.css.lib.api.properties.GroupNode
meth public <%0 extends org.netbeans.modules.css.lib.api.properties.Node$AbstractNode> boolean removeChild({%%0})
meth public <%0 extends org.netbeans.modules.css.lib.api.properties.Node$AbstractNode> {%%0} addChild({%%0})
meth public java.lang.CharSequence image()
meth public java.lang.String name()
meth public java.lang.String toString()
meth public java.util.Collection<org.netbeans.modules.css.lib.api.properties.Node> children()
meth public java.util.Collection<org.netbeans.modules.css.lib.api.properties.Node> modifiableChildren()
meth public org.netbeans.modules.css.lib.api.properties.GrammarElement getGrammarElement()
meth public void accept(org.netbeans.modules.css.lib.api.properties.NodeVisitor2)
supr org.netbeans.modules.css.lib.api.properties.Node$AbstractNode
hfds children,element

CLSS public static org.netbeans.modules.css.lib.api.properties.Node$ResolvedTokenNode
 outer org.netbeans.modules.css.lib.api.properties.Node
cons public init()
intf org.netbeans.modules.css.lib.api.properties.TokenNode
meth public java.lang.CharSequence image()
meth public java.lang.String name()
meth public java.lang.String toString()
meth public java.util.Collection<org.netbeans.modules.css.lib.api.properties.Node> children()
meth public org.netbeans.modules.css.lib.api.properties.ResolvedToken getResolvedToken()
meth public org.netbeans.modules.css.lib.api.properties.Token getToken()
meth public void accept(org.netbeans.modules.css.lib.api.properties.NodeVisitor2)
meth public void setResolvedToken(org.netbeans.modules.css.lib.api.properties.ResolvedToken)
supr org.netbeans.modules.css.lib.api.properties.Node$AbstractNode
hfds resolvedToken

CLSS public org.netbeans.modules.css.lib.api.properties.NodeUtil
meth public static void dump(org.netbeans.modules.css.lib.api.properties.Node,int,java.io.PrintWriter)
meth public static void dumpTree(org.netbeans.modules.css.lib.api.properties.Node)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.css.lib.api.properties.NodeVisitor
innr public abstract static Adapter
meth public abstract boolean visit(org.netbeans.modules.css.lib.api.properties.Node)
meth public abstract void unvisit(org.netbeans.modules.css.lib.api.properties.Node)

CLSS public abstract static org.netbeans.modules.css.lib.api.properties.NodeVisitor$Adapter
 outer org.netbeans.modules.css.lib.api.properties.NodeVisitor
cons public init()
intf org.netbeans.modules.css.lib.api.properties.NodeVisitor
meth public boolean visit(org.netbeans.modules.css.lib.api.properties.Node)
meth public void unvisit(org.netbeans.modules.css.lib.api.properties.Node)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.css.lib.api.properties.NodeVisitor2
cons public init()
meth public boolean visitGroupNode(org.netbeans.modules.css.lib.api.properties.GroupNode)
meth public void visitTokenNode(org.netbeans.modules.css.lib.api.properties.TokenNode)
supr java.lang.Object

CLSS public org.netbeans.modules.css.lib.api.properties.Properties
meth public static boolean isAggregatedProperty(org.openide.filesystems.FileObject,org.netbeans.modules.css.lib.api.properties.PropertyDefinition)
meth public static boolean isVendorSpecificProperty(org.netbeans.modules.css.lib.api.properties.PropertyDefinition)
meth public static boolean isVendorSpecificPropertyName(java.lang.String)
meth public static boolean isVisibleProperty(org.netbeans.modules.css.lib.api.properties.PropertyDefinition)
meth public static java.util.Collection<java.lang.String> getPropertyNames(org.openide.filesystems.FileObject)
meth public static java.util.Collection<org.netbeans.modules.css.lib.api.properties.PropertyDefinition> getPropertyDefinitions(org.openide.filesystems.FileObject)
meth public static java.util.Collection<org.netbeans.modules.css.lib.api.properties.PropertyDefinition> getPropertyDefinitions(org.openide.filesystems.FileObject,boolean)
meth public static org.netbeans.modules.css.lib.api.properties.PropertyDefinition getPropertyDefinition(java.lang.String)
meth public static org.netbeans.modules.css.lib.api.properties.PropertyDefinition getPropertyDefinition(java.lang.String,boolean)
supr java.lang.Object

CLSS public final !enum org.netbeans.modules.css.lib.api.properties.PropertyCategory
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory ALIGNMENT
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory ANIMATIONS
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory BACKGROUND
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory BOX
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory CHROME
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory COLORS
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory CONTAIN
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory CONTENT
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory DEFAULT
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory FIREFOX
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory FLEXIBLE_BOX_LAYOUT
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory FONTS
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory FRAGMENTATION
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory GRID
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory HYPERLINKS
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory IMAGES
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory INTERNET_EXPLORER
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory LINE
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory LISTS_AND_COUNTERS
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory MARQUEE
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory MULTI_COLUMN_LAYOUT
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory OPERA
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory PAGED_MEDIA
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory POSITIONING
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory RUBY
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory SAFARI
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory SIZING
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory SPEECH
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory TEXT
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory TRANSFORMATIONS_2D
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory TRANSFORMATIONS_3D
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory TRANSITIONS
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory UNKNOWN
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory USER_INTERFACE
fld public final static org.netbeans.modules.css.lib.api.properties.PropertyCategory WRITING_MODES
meth public java.lang.String getDisplayName()
meth public java.lang.String getLongDescription()
meth public java.lang.String getShortDescription()
meth public static org.netbeans.modules.css.lib.api.properties.PropertyCategory valueOf(java.lang.String)
meth public static org.netbeans.modules.css.lib.api.properties.PropertyCategory[] values()
supr java.lang.Enum<org.netbeans.modules.css.lib.api.properties.PropertyCategory>
hfds displayName,longDescription,shortDescription

CLSS public org.netbeans.modules.css.lib.api.properties.PropertyDefinition
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,org.netbeans.modules.css.lib.api.CssModule)
cons public init(java.lang.String,java.lang.String,org.netbeans.modules.css.lib.api.properties.PropertyCategory,org.netbeans.modules.css.lib.api.CssModule)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getGrammar()
meth public java.lang.String getName()
meth public org.netbeans.modules.css.lib.api.CssModule getCssModule()
meth public org.netbeans.modules.css.lib.api.properties.GroupGrammarElement getGrammarElement(org.openide.filesystems.FileObject)
meth public org.netbeans.modules.css.lib.api.properties.PropertyCategory getPropertyCategory()
supr java.lang.Object
hfds cssModule,grammar,name,propertyCategory,resolved

CLSS public abstract interface org.netbeans.modules.css.lib.api.properties.PropertyDefinitionProvider
innr public static Query
meth public abstract java.util.Collection<java.lang.String> getPropertyNames(org.openide.filesystems.FileObject)
meth public abstract org.netbeans.modules.css.lib.api.properties.PropertyDefinition getPropertyDefinition(java.lang.String)

CLSS public static org.netbeans.modules.css.lib.api.properties.PropertyDefinitionProvider$Query
 outer org.netbeans.modules.css.lib.api.properties.PropertyDefinitionProvider
cons public init()
meth public static java.util.Collection<java.lang.String> getPropertyNames(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.css.lib.api.properties.PropertyDefinition getPropertyDefinition(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.css.lib.api.properties.ResolvedProperty
cons public init(java.lang.String,java.lang.String)
cons public init(org.netbeans.modules.css.lib.api.properties.GrammarResolver,java.lang.String)
cons public init(org.netbeans.modules.css.lib.api.properties.GroupGrammarElement,java.lang.String)
cons public init(org.netbeans.modules.css.lib.api.properties.PropertyDefinition,java.lang.CharSequence)
cons public init(org.openide.filesystems.FileObject,org.netbeans.modules.css.lib.api.properties.PropertyDefinition,java.lang.CharSequence)
meth public boolean isResolved()
meth public java.util.List<org.netbeans.modules.css.lib.api.properties.ResolvedToken> getResolvedTokens()
meth public java.util.List<org.netbeans.modules.css.lib.api.properties.Token> getTokens()
meth public java.util.List<org.netbeans.modules.css.lib.api.properties.Token> getUnresolvedTokens()
meth public java.util.Set<org.netbeans.modules.css.lib.api.properties.ValueGrammarElement> getAlternatives()
meth public org.netbeans.modules.css.lib.api.properties.Node getParseTree()
meth public org.netbeans.modules.css.lib.api.properties.PropertyDefinition getPropertyDefinition()
meth public static org.netbeans.modules.css.lib.api.properties.ResolvedProperty resolve(org.openide.filesystems.FileObject,org.netbeans.modules.css.lib.api.properties.PropertyDefinition,java.lang.CharSequence)
supr java.lang.Object
hfds FILTER_COMMENTS_PATTERN,grammarResolverResult,propertyModel

CLSS public org.netbeans.modules.css.lib.api.properties.ResolvedToken
cons public init(org.netbeans.modules.css.lib.api.properties.Token,org.netbeans.modules.css.lib.api.properties.ValueGrammarElement)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.netbeans.modules.css.lib.api.properties.Token token()
meth public org.netbeans.modules.css.lib.api.properties.ValueGrammarElement getGrammarElement()
supr java.lang.Object
hfds grammarElement,token

CLSS public org.netbeans.modules.css.lib.api.properties.Token
cons public init(org.netbeans.modules.css.lib.api.CssTokenId,int,int,java.lang.CharSequence)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public int length()
meth public int offset()
meth public java.lang.CharSequence image()
meth public java.lang.String toString()
meth public org.netbeans.modules.css.lib.api.CssTokenId tokenId()
supr java.lang.Object
hfds length,offset,tokenId,tokenizerInput

CLSS public abstract org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
fld public final static java.util.Collection<org.netbeans.modules.css.lib.api.properties.TokenAcceptor> ACCEPTORS
fld public final static java.util.Map<java.lang.String,org.netbeans.modules.css.lib.api.properties.TokenAcceptor> ACCEPTORS_MAP
innr public abstract static NumberPostfixAcceptor
innr public abstract static TokenImageAcceptor
innr public static Angle
innr public static Anything
innr public static Date
innr public static Decibel
innr public static Flex
innr public static Frequency
innr public static HashColor
innr public static HashColorAplha
innr public static Identifier
innr public static Integer
innr public static Length
innr public static NonNegativeInteger
innr public static Number
innr public static Percentage
innr public static RelativeLength
innr public static Resolution
innr public static Semitones
innr public static StringAcceptor
innr public static Time
innr public static Urange
innr public static Uri
innr public static Variable
meth public abstract boolean accepts(org.netbeans.modules.css.lib.api.properties.Token)
meth public final java.lang.String id()
meth public static <%0 extends org.netbeans.modules.css.lib.api.properties.TokenAcceptor> {%%0} getAcceptor(java.lang.Class<{%%0}>)
meth public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor getAcceptor(java.lang.String)
supr java.lang.Object
hfds INSTANCES,id
hcls NonBrace

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Angle
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth protected java.util.List<java.lang.String> postfixes()
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor$NumberPostfixAcceptor
hfds POSTFIXES

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Anything
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth public boolean accepts(org.netbeans.modules.css.lib.api.properties.Token)
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Date
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth public boolean accepts(java.lang.String)
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor$TokenImageAcceptor

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Decibel
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth protected java.util.List<java.lang.String> postfixes()
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor$NumberPostfixAcceptor
hfds POSTFIXES

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Flex
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth protected java.util.List<java.lang.String> postfixes()
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor$NumberPostfixAcceptor
hfds POSTFIXES

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Frequency
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth protected java.util.List<java.lang.String> postfixes()
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor$NumberPostfixAcceptor
hfds POSTFIXES

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$HashColor
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth public boolean accepts(org.netbeans.modules.css.lib.api.properties.Token)
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$HashColorAplha
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth public boolean accepts(org.netbeans.modules.css.lib.api.properties.Token)
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Identifier
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth public boolean accepts(org.netbeans.modules.css.lib.api.properties.Token)
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Integer
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth public boolean accepts(java.lang.String)
meth public int getNumberValue(java.lang.String)
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor$TokenImageAcceptor

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Length
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth protected java.util.List<java.lang.String> postfixes()
meth public boolean accepts(java.lang.String)
meth public java.lang.Float getNumberValue(java.lang.CharSequence)
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor$NumberPostfixAcceptor
hfds POSTFIXES

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$NonNegativeInteger
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth public boolean accepts(java.lang.String)
meth public int getNumberValue(java.lang.String)
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor$TokenImageAcceptor

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Number
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth public boolean accepts(java.lang.String)
meth public java.lang.Float getNumberValue(java.lang.String)
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor$TokenImageAcceptor

CLSS public abstract static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$NumberPostfixAcceptor
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth protected abstract java.util.List<java.lang.String> postfixes()
meth public boolean accepts(java.lang.String)
meth public java.lang.CharSequence getPostfix(java.lang.CharSequence)
meth public java.lang.Float getNumberValue(java.lang.CharSequence)
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor$TokenImageAcceptor

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Percentage
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth protected java.util.List<java.lang.String> postfixes()
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor$NumberPostfixAcceptor
hfds POSTFIXES

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$RelativeLength
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth protected java.util.List<java.lang.String> postfixes()
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor$NumberPostfixAcceptor
hfds POSTFIXES

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Resolution
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth protected java.util.List<java.lang.String> postfixes()
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor$NumberPostfixAcceptor
hfds POSTFIXES

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Semitones
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth protected java.util.List<java.lang.String> postfixes()
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor$NumberPostfixAcceptor
hfds POSTFIXES

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$StringAcceptor
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth public boolean accepts(java.lang.String)
meth public java.lang.String getUnquotedValue(java.lang.String)
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor$TokenImageAcceptor

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Time
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth protected java.util.List<java.lang.String> postfixes()
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor$NumberPostfixAcceptor
hfds POSTFIXES

CLSS public abstract static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$TokenImageAcceptor
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth public abstract boolean accepts(java.lang.String)
meth public boolean accepts(org.netbeans.modules.css.lib.api.properties.Token)
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Urange
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth public boolean accepts(org.netbeans.modules.css.lib.api.properties.Token)
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor
hfds URANGE_TOKEN_IMAGE

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Uri
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth public boolean accepts(org.netbeans.modules.css.lib.api.properties.Token)
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor
hfds URL_TOKEN_IMAGE

CLSS public static org.netbeans.modules.css.lib.api.properties.TokenAcceptor$Variable
 outer org.netbeans.modules.css.lib.api.properties.TokenAcceptor
cons public init(java.lang.String)
meth public boolean accepts(org.netbeans.modules.css.lib.api.properties.Token)
supr org.netbeans.modules.css.lib.api.properties.TokenAcceptor

CLSS public abstract interface org.netbeans.modules.css.lib.api.properties.TokenNode
intf org.netbeans.modules.css.lib.api.properties.Node
meth public abstract org.netbeans.modules.css.lib.api.properties.ResolvedToken getResolvedToken()

CLSS public final org.netbeans.modules.css.lib.api.properties.Tokenizer
cons public init(java.lang.CharSequence)
meth public boolean moveNext()
meth public boolean movePrevious()
meth public int tokenIndex()
meth public int tokensCount()
meth public java.util.List<org.netbeans.modules.css.lib.api.properties.Token> tokensList()
meth public org.netbeans.modules.css.lib.api.properties.Token token()
meth public void move(int)
meth public void reset()
supr java.lang.Object
hfds currentToken,tokens

CLSS public org.netbeans.modules.css.lib.api.properties.UnitGrammarElement
cons public init(org.netbeans.modules.css.lib.api.properties.GroupGrammarElement,org.netbeans.modules.css.lib.api.properties.TokenAcceptor,java.lang.String)
meth public boolean accepts(org.netbeans.modules.css.lib.api.properties.Token)
meth public final java.lang.String getTokenAcceptorId()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public java.util.Collection<java.lang.String> getFixedValues()
 anno 0 java.lang.Deprecated()
meth public void accept(org.netbeans.modules.css.lib.api.properties.GrammarElementVisitor)
supr org.netbeans.modules.css.lib.api.properties.ValueGrammarElement
hfds name,tokenAcceptor

CLSS public abstract org.netbeans.modules.css.lib.api.properties.ValueGrammarElement
cons public init(org.netbeans.modules.css.lib.api.properties.GroupGrammarElement,java.lang.String)
meth public abstract boolean accepts(org.netbeans.modules.css.lib.api.properties.Token)
meth public abstract java.lang.String getValue()
meth public final boolean accepts(java.lang.CharSequence)
supr org.netbeans.modules.css.lib.api.properties.GrammarElement

CLSS public abstract org.netbeans.modules.parsing.spi.Parser
cons public init()
innr public abstract static Result
innr public final static !enum CancelReason
meth public abstract org.netbeans.modules.parsing.spi.Parser$Result getResult(org.netbeans.modules.parsing.api.Task) throws org.netbeans.modules.parsing.spi.ParseException
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void parse(org.netbeans.modules.parsing.api.Snapshot,org.netbeans.modules.parsing.api.Task,org.netbeans.modules.parsing.spi.SourceModificationEvent) throws org.netbeans.modules.parsing.spi.ParseException
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public void cancel()
 anno 0 java.lang.Deprecated()
meth public void cancel(org.netbeans.modules.parsing.spi.Parser$CancelReason,org.netbeans.modules.parsing.spi.SourceModificationEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hcls MyAccessor

CLSS public abstract static org.netbeans.modules.parsing.spi.Parser$Result
 outer org.netbeans.modules.parsing.spi.Parser
cons protected init(org.netbeans.modules.parsing.api.Snapshot)
meth protected abstract void invalidate()
meth protected boolean processingFinished()
meth public org.netbeans.modules.parsing.api.Snapshot getSnapshot()
supr java.lang.Object
hfds snapshot

CLSS public abstract org.netbeans.modules.parsing.spi.ParserFactory
cons public init()
meth public abstract org.netbeans.modules.parsing.spi.Parser createParser(java.util.Collection<org.netbeans.modules.parsing.api.Snapshot>)
supr java.lang.Object

