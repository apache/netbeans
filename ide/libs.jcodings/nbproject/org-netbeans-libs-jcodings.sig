#Signature file v4.1
#Version 1.2

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

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

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

CLSS public abstract java.nio.charset.Charset
cons protected init(java.lang.String,java.lang.String[])
intf java.lang.Comparable<java.nio.charset.Charset>
meth public abstract boolean contains(java.nio.charset.Charset)
meth public abstract java.nio.charset.CharsetDecoder newDecoder()
meth public abstract java.nio.charset.CharsetEncoder newEncoder()
meth public boolean canEncode()
meth public final boolean equals(java.lang.Object)
meth public final boolean isRegistered()
meth public final int compareTo(java.nio.charset.Charset)
meth public final int hashCode()
meth public final java.lang.String name()
meth public final java.lang.String toString()
meth public final java.nio.ByteBuffer encode(java.lang.String)
meth public final java.nio.ByteBuffer encode(java.nio.CharBuffer)
meth public final java.nio.CharBuffer decode(java.nio.ByteBuffer)
meth public final java.util.Set<java.lang.String> aliases()
meth public java.lang.String displayName()
meth public java.lang.String displayName(java.util.Locale)
meth public static boolean isSupported(java.lang.String)
meth public static java.nio.charset.Charset defaultCharset()
meth public static java.nio.charset.Charset forName(java.lang.String)
meth public static java.util.SortedMap<java.lang.String,java.nio.charset.Charset> availableCharsets()
supr java.lang.Object

CLSS public abstract java.nio.charset.spi.CharsetProvider
cons protected init()
meth public abstract java.nio.charset.Charset charsetForName(java.lang.String)
meth public abstract java.util.Iterator<java.nio.charset.Charset> charsets()
supr java.lang.Object

CLSS public abstract interface java.util.Iterator<%0 extends java.lang.Object>
meth public abstract boolean hasNext()
meth public abstract {java.util.Iterator%0} next()
meth public void forEachRemaining(java.util.function.Consumer<? super {java.util.Iterator%0}>)
meth public void remove()

CLSS public abstract interface org.jcodings.ApplyAllCaseFoldFunction
meth public abstract void apply(int,int[],int,java.lang.Object)

CLSS public abstract org.jcodings.CanBeTrailTableEncoding
cons protected init(java.lang.String,int,int,int[],int[][],short[],boolean[])
fld protected final boolean[] CanBeTrailTable
meth public boolean isReverseMatchAllowed(byte[],int,int)
meth public int leftAdjustCharHead(byte[],int,int,int)
supr org.jcodings.MultiByteEncoding

CLSS public final org.jcodings.CaseFoldCodeItem
fld public final int byteLen
fld public final int[] code
fld public final static org.jcodings.CaseFoldCodeItem[] EMPTY_FOLD_CODES
meth public static org.jcodings.CaseFoldCodeItem create(int,int)
meth public static org.jcodings.CaseFoldCodeItem create(int,int,int)
meth public static org.jcodings.CaseFoldCodeItem create(int,int,int,int)
supr java.lang.Object

CLSS public abstract org.jcodings.CaseFoldMapEncoding
cons protected init(java.lang.String,short[],byte[],int[][])
cons protected init(java.lang.String,short[],byte[],int[][],boolean)
fld protected final boolean foldFlag
fld protected final int[][] CaseFoldMap
meth protected final int applyAllCaseFoldWithMap(int,int[][],boolean,int,org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
meth protected final org.jcodings.CaseFoldCodeItem[] getCaseFoldCodesByStringWithMap(int,int[][],boolean,int,byte[],int,int)
meth public boolean isCodeCType(int,int)
meth public org.jcodings.CaseFoldCodeItem[] caseFoldCodesByString(int,byte[],int,int)
meth public void applyAllCaseFold(int,org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
supr org.jcodings.SingleByteEncoding
hfds SS

CLSS public final org.jcodings.CodeRange
cons public init()
meth public static boolean isInCodeRange(int[],int)
meth public static boolean isInCodeRange(int[],int,int)
supr java.lang.Object

CLSS public abstract interface org.jcodings.Config
fld public final static boolean USE_CRNL_AS_LINE_TERMINATOR = false
fld public final static boolean USE_UNICODE_ALL_LINE_TERMINATORS = false
fld public final static boolean USE_UNICODE_CASE_FOLD_TURKISH_AZERI = false
fld public final static boolean USE_UNICODE_PROPERTIES = true
fld public final static int CASE_ASCII_ONLY = 4194304
fld public final static int CASE_DOWNCASE = 16384
fld public final static int CASE_DOWN_SPECIAL = 131072
fld public final static int CASE_FOLD = 524288
fld public final static int CASE_FOLD_LITHUANIAN = 2097152
fld public final static int CASE_FOLD_TURKISH_AZERI = 1048576
fld public final static int CASE_IS_TITLECASE = 8388608
fld public final static int CASE_MODIFIED = 262144
fld public final static int CASE_SPECIALS = 8617984
fld public final static int CASE_SPECIAL_OFFSET = 3
fld public final static int CASE_TITLECASE = 32768
fld public final static int CASE_UPCASE = 8192
fld public final static int CASE_UP_SPECIAL = 65536
fld public final static int CodePointMask = 7
fld public final static int CodePointMaskWidth = 3
fld public final static int ENC_CASE_FOLD_DEFAULT = 1073741824
fld public final static int ENC_CASE_FOLD_MIN = 1073741824
fld public final static int ENC_CODE_TO_MBC_MAXLEN = 7
fld public final static int ENC_GET_CASE_FOLD_CODES_MAX_NUM = 13
fld public final static int ENC_MAX_COMP_CASE_FOLD_CODE_LEN = 3
fld public final static int ENC_MBC_CASE_FOLD_MAXLEN = 18
fld public final static int INTERNAL_ENC_CASE_FOLD_MULTI_CHAR = 1073741824
fld public final static int SpecialIndexMask = 8184
fld public final static int SpecialIndexShift = 3
fld public final static int SpecialIndexWidth = 10
fld public final static int SpecialsLengthOffset = 25
fld public final static int UNICODE_EMOJI_VERSION_MAJOR = 13
fld public final static int UNICODE_EMOJI_VERSION_MINOR = 1
fld public final static int UNICODE_VERSION_MAJOR = 13
fld public final static int UNICODE_VERSION_MINOR = 0
fld public final static int UNICODE_VERSION_TEENY = 0
fld public final static java.lang.String UNICODE_EMOJI_VERSION_STRING = "13.1"
fld public final static java.lang.String UNICODE_VERSION_STRING = "13.0.0"

CLSS public abstract org.jcodings.Encoding
cons protected init(java.lang.String,int,int)
fld protected boolean isUTF8
fld protected boolean isUnicode
fld protected final int maxLength
fld protected final int minLength
fld public final static byte NEW_LINE = 10
fld public final static int CHAR_INVALID = -1
intf java.lang.Cloneable
meth protected final void setDummy()
meth protected final void setName(byte[])
meth protected final void setName(java.lang.String)
meth public abstract boolean isCodeCType(int,int)
meth public abstract boolean isNewLine(byte[],int,int)
meth public abstract boolean isReverseMatchAllowed(byte[],int,int)
meth public abstract int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
meth public abstract int codeToMbc(int,byte[],int)
meth public abstract int codeToMbcLength(int)
meth public abstract int leftAdjustCharHead(byte[],int,int,int)
meth public abstract int length(byte)
meth public abstract int length(byte[],int,int)
meth public abstract int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public abstract int mbcToCode(byte[],int,int)
meth public abstract int propertyNameToCType(byte[],int,int)
meth public abstract int strCodeAt(byte[],int,int,int)
meth public abstract int strLength(byte[],int,int)
meth public abstract int[] ctypeCodeRange(int,org.jcodings.IntHolder)
meth public abstract org.jcodings.CaseFoldCodeItem[] caseFoldCodesByString(int,byte[],int,int)
meth public abstract void applyAllCaseFold(int,org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
meth public boolean isMbcCrnl(byte[],int,int)
meth public byte[] toLowerCaseTable()
meth public final boolean equals(java.lang.Object)
meth public final boolean isAlnum(int)
meth public final boolean isAlpha(int)
meth public final boolean isAsciiCompatible()
meth public final boolean isBlank(int)
meth public final boolean isCntrl(int)
meth public final boolean isDigit(int)
meth public final boolean isDummy()
meth public final boolean isFixedWidth()
meth public final boolean isGraph(int)
meth public final boolean isLower(int)
meth public final boolean isMbcHead(byte[],int,int)
meth public final boolean isMbcWord(byte[],int,int)
meth public final boolean isNewLine(int)
meth public final boolean isPrint(int)
meth public final boolean isPunct(int)
meth public final boolean isSbWord(int)
meth public final boolean isSingleByte()
meth public final boolean isSpace(int)
meth public final boolean isUTF8()
meth public final boolean isUnicode()
meth public final boolean isUpper(int)
meth public final boolean isWord(int)
meth public final boolean isXDigit(int)
meth public final byte[] getName()
meth public final int getIndex()
meth public final int hashCode()
meth public final int maxLength()
meth public final int maxLengthDistance()
 anno 0 java.lang.Deprecated()
meth public final int mbcodeStartPosition()
 anno 0 java.lang.Deprecated()
meth public final int minLength()
meth public final int prevCharHead(byte[],int,int,int)
meth public final int rightAdjustCharHead(byte[],int,int,int)
meth public final int rightAdjustCharHeadWithPrev(byte[],int,int,int,org.jcodings.IntHolder)
meth public final int step(byte[],int,int,int)
meth public final int stepBack(byte[],int,int,int,int)
meth public final int strByteLengthNull(byte[],int,int)
meth public final int strLengthNull(byte[],int,int)
meth public final int strNCmp(byte[],int,int,byte[],int,int)
meth public final int xdigitVal(int)
meth public final java.lang.String toString()
meth public java.lang.String getCharsetName()
meth public java.nio.charset.Charset getCharset()
meth public static boolean isAscii(byte)
meth public static boolean isAscii(int)
meth public static boolean isMbcAscii(byte)
meth public static boolean isWordGraphPrint(int)
meth public static byte asciiToLower(int)
meth public static byte asciiToUpper(int)
meth public static int digitVal(int)
meth public static int odigitVal(int)
meth public static org.jcodings.Encoding load(java.lang.String)
meth public static org.jcodings.Encoding load(java.lang.String,java.lang.String)
supr java.lang.Object
hfds charset,count,hashCode,index,isAsciiCompatible,isDummy,isFixedWidth,isSingleByte,name,stringName

CLSS public final org.jcodings.EncodingDB
cons public init()
innr public final static Entry
meth public final static org.jcodings.util.CaseInsensitiveBytesHash<org.jcodings.EncodingDB$Entry> getAliases()
meth public final static org.jcodings.util.CaseInsensitiveBytesHash<org.jcodings.EncodingDB$Entry> getEncodings()
meth public static org.jcodings.EncodingDB$Entry dummy(byte[])
meth public static void alias(java.lang.String,java.lang.String)
meth public static void declare(java.lang.String,java.lang.String)
meth public static void dummy(java.lang.String)
meth public static void dummy_unicode(java.lang.String)
meth public static void replicate(java.lang.String,java.lang.String)
meth public static void set_base(java.lang.String,java.lang.String)
supr java.lang.Object
hfds aliases,ascii,encodings

CLSS public final static org.jcodings.EncodingDB$Entry
 outer org.jcodings.EncodingDB
meth public boolean isDummy()
meth public int getIndex()
meth public int hashCode()
meth public java.lang.String getEncodingClass()
meth public org.jcodings.Encoding getEncoding()
meth public org.jcodings.EncodingDB$Entry getBase()
supr java.lang.Object
hfds base,count,encoding,encodingClass,index,isDummy,name

CLSS public abstract org.jcodings.EucEncoding
cons protected init(java.lang.String,int,int,int[],int[][],short[])
meth protected abstract boolean isLead(int)
meth public int leftAdjustCharHead(byte[],int,int,int)
supr org.jcodings.MultiByteEncoding

CLSS public abstract org.jcodings.ISOEncoding
cons protected init(java.lang.String,short[],byte[],int[][])
cons protected init(java.lang.String,short[],byte[],int[][],boolean)
fld public static int SHARP_s
meth public boolean isCodeCType(int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public java.lang.String getCharsetName()
supr org.jcodings.CaseFoldMapEncoding

CLSS public org.jcodings.IntHolder
cons public init()
fld public int value
supr java.lang.Object

CLSS public abstract org.jcodings.MultiByteEncoding
cons protected init(java.lang.String,int,int,int[],int[][],short[])
fld protected final int[] EncLen
fld protected final int[] TransZero
fld protected final int[][] Trans
fld protected final static int A = -1
fld protected final static int F = -2
meth protected final boolean isCodeCTypeInternal(int,int)
meth protected final boolean mb2IsCodeCType(int,int)
meth protected final boolean mb4IsCodeCType(int,int)
meth protected final int asciiMbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth protected final int lengthForTwoUptoFour(byte[],int,int,int,int)
meth protected final int mb2CodeToMbc(int,byte[],int)
meth protected final int mb2CodeToMbcLength(int)
meth protected final int mb4CodeToMbc(int,byte[],int)
meth protected final int mb4CodeToMbcLength(int)
meth protected final int mbnMbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth protected final int mbnMbcToCode(byte[],int,int)
meth protected final int missing(int)
meth protected final int missing(int,int)
meth protected final int safeLengthForUptoFour(byte[],int,int)
meth protected final int safeLengthForUptoThree(byte[],int,int)
meth protected final int safeLengthForUptoTwo(byte[],int,int)
meth protected final org.jcodings.CaseFoldCodeItem[] asciiCaseFoldCodesByString(int,byte[],int,int)
meth protected final void asciiApplyAllCaseFold(int,org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
meth public boolean isNewLine(byte[],int,int)
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
meth public int length(byte)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int propertyNameToCType(byte[],int,int)
meth public int strCodeAt(byte[],int,int,int)
meth public int strLength(byte[],int,int)
meth public org.jcodings.CaseFoldCodeItem[] caseFoldCodesByString(int,byte[],int,int)
meth public static boolean isInRange(int,int,int)
meth public void applyAllCaseFold(int,org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
supr org.jcodings.Encoding

CLSS public final org.jcodings.ObjPtr<%0 extends java.lang.Object>
cons public init()
cons public init({org.jcodings.ObjPtr%0})
fld public {org.jcodings.ObjPtr%0} p
supr java.lang.Object
hfds NULL

CLSS public final org.jcodings.Ptr
cons public init()
cons public init(int)
fld public final static org.jcodings.Ptr NULL
fld public int p
supr java.lang.Object

CLSS public abstract org.jcodings.SingleByteEncoding
cons protected init(java.lang.String,short[],byte[])
fld protected final byte[] LowerCaseTable
fld public final static int MAX_BYTE = 255
meth protected final boolean isCodeCTypeInternal(int,int)
meth protected final int asciiMbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth protected final org.jcodings.CaseFoldCodeItem[] asciiCaseFoldCodesByString(int,byte[],int,int)
meth protected final void asciiApplyAllCaseFold(int,org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
meth public boolean isNewLine(byte[],int,int)
meth public final boolean isReverseMatchAllowed(byte[],int,int)
meth public final int codeToMbc(int,byte[],int)
meth public final int leftAdjustCharHead(byte[],int,int,int)
meth public final int strLength(byte[],int,int)
meth public final int[] ctypeCodeRange(int,org.jcodings.IntHolder)
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
meth public int codeToMbcLength(int)
meth public int length(byte)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int propertyNameToCType(byte[],int,int)
meth public int strCodeAt(byte[],int,int,int)
meth public org.jcodings.CaseFoldCodeItem[] caseFoldCodesByString(int,byte[],int,int)
meth public void applyAllCaseFold(int,org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
supr org.jcodings.Encoding

CLSS public org.jcodings.ascii.AsciiTables
cons public init()
fld public final static byte[] ToLowerCaseTable
fld public final static byte[] ToUpperCaseTable
fld public final static int[][] LowerMap
fld public final static short[] AsciiCtypeTable
supr java.lang.Object

CLSS public abstract interface org.jcodings.constants.CharacterType
fld public final static int ALNUM = 13
fld public final static int ALPHA = 1
fld public final static int ASCII = 14
fld public final static int BIT_ALNUM = 8192
fld public final static int BIT_ALPHA = 2
fld public final static int BIT_ASCII = 16384
fld public final static int BIT_BLANK = 4
fld public final static int BIT_CNTRL = 8
fld public final static int BIT_DIGIT = 16
fld public final static int BIT_GRAPH = 32
fld public final static int BIT_LOWER = 64
fld public final static int BIT_NEWLINE = 1
fld public final static int BIT_PRINT = 128
fld public final static int BIT_PUNCT = 256
fld public final static int BIT_SPACE = 512
fld public final static int BIT_UPPER = 1024
fld public final static int BIT_WORD = 4096
fld public final static int BIT_XDIGIT = 2048
fld public final static int BLANK = 2
fld public final static int CNTRL = 3
fld public final static int DIGIT = 4
fld public final static int GRAPH = 5
fld public final static int LOWER = 6
fld public final static int MAX_STD_CTYPE = 14
fld public final static int NEWLINE = 0
fld public final static int PRINT = 7
fld public final static int PUNCT = 8
fld public final static int SPACE = 9
fld public final static int UPPER = 10
fld public final static int WORD = 12
fld public final static int XDIGIT = 11

CLSS public org.jcodings.constants.PosixBracket
cons public init()
fld public final static byte[][] PBSNamesLower
fld public final static int[] PBSValues
fld public final static org.jcodings.util.CaseInsensitiveBytesHash<java.lang.Integer> PBSTableUpper
supr java.lang.Object

CLSS public org.jcodings.exception.CharacterPropertyException
cons public init(java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,byte[],int,int)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(org.jcodings.exception.EncodingError)
cons public init(org.jcodings.exception.EncodingError,byte[],int,int)
cons public init(org.jcodings.exception.EncodingError,java.lang.String)
supr org.jcodings.exception.EncodingException

CLSS public final !enum org.jcodings.exception.EncodingError
fld public final static org.jcodings.exception.EncodingError ERR_COULD_NOT_REPLICATE
fld public final static org.jcodings.exception.EncodingError ERR_ENCODING_ALIAS_ALREADY_REGISTERED
fld public final static org.jcodings.exception.EncodingError ERR_ENCODING_ALREADY_REGISTERED
fld public final static org.jcodings.exception.EncodingError ERR_ENCODING_CLASS_DEF_NOT_FOUND
fld public final static org.jcodings.exception.EncodingError ERR_ENCODING_LOAD_ERROR
fld public final static org.jcodings.exception.EncodingError ERR_ENCODING_REPLICA_ALREADY_REGISTERED
fld public final static org.jcodings.exception.EncodingError ERR_INVALID_CHAR_PROPERTY_NAME
fld public final static org.jcodings.exception.EncodingError ERR_INVALID_CODE_POINT_VALUE
fld public final static org.jcodings.exception.EncodingError ERR_NO_SUCH_ENCODNG
fld public final static org.jcodings.exception.EncodingError ERR_TOO_BIG_WIDE_CHAR_VALUE
fld public final static org.jcodings.exception.EncodingError ERR_TOO_LONG_WIDE_CHAR_VALUE
fld public final static org.jcodings.exception.EncodingError ERR_TRANSCODER_ALREADY_REGISTERED
fld public final static org.jcodings.exception.EncodingError ERR_TRANSCODER_CLASS_DEF_NOT_FOUND
fld public final static org.jcodings.exception.EncodingError ERR_TRANSCODER_LOAD_ERROR
fld public final static org.jcodings.exception.EncodingError ERR_TYPE_BUG
meth public int getCode()
meth public java.lang.String getMessage()
meth public static org.jcodings.exception.EncodingError fromCode(int)
meth public static org.jcodings.exception.EncodingError valueOf(java.lang.String)
meth public static org.jcodings.exception.EncodingError[] values()
supr java.lang.Enum<org.jcodings.exception.EncodingError>
hfds CODE_TO_ERROR,code,message

CLSS public org.jcodings.exception.EncodingException
cons public init(java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,byte[],int,int)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(org.jcodings.exception.EncodingError)
cons public init(org.jcodings.exception.EncodingError,byte[],int,int)
cons public init(org.jcodings.exception.EncodingError,java.lang.String)
meth public org.jcodings.exception.EncodingError getError()
supr org.jcodings.exception.JCodingsException
hfds error

CLSS public abstract interface org.jcodings.exception.ErrorCodes
fld public final static int ERR_CHAR_CLASS_VALUE_AT_END_OF_RANGE = -110
fld public final static int ERR_CHAR_CLASS_VALUE_AT_START_OF_RANGE = -111
fld public final static int ERR_CONTROL_CODE_SYNTAX = -109
fld public final static int ERR_COULD_NOT_REPLICATE = -1006
fld public final static int ERR_DEFAULT_ENCODING_IS_NOT_SET = -21
fld public final static int ERR_EMPTY_CHAR_CLASS = -102
fld public final static int ERR_EMPTY_GROUP_NAME = -214
fld public final static int ERR_EMPTY_RANGE_IN_CHAR_CLASS = -203
fld public final static int ERR_ENCODING_ALIAS_ALREADY_REGISTERED = -1003
fld public final static int ERR_ENCODING_ALREADY_REGISTERED = -1002
fld public final static int ERR_ENCODING_CLASS_DEF_NOT_FOUND = -1000
fld public final static int ERR_ENCODING_LOAD_ERROR = -1001
fld public final static int ERR_ENCODING_REPLICA_ALREADY_REGISTERED = -1004
fld public final static int ERR_END_PATTERN_AT_CONTROL = -106
fld public final static int ERR_END_PATTERN_AT_ESCAPE = -104
fld public final static int ERR_END_PATTERN_AT_LEFT_BRACE = -100
fld public final static int ERR_END_PATTERN_AT_LEFT_BRACKET = -101
fld public final static int ERR_END_PATTERN_AT_META = -105
fld public final static int ERR_END_PATTERN_IN_GROUP = -118
fld public final static int ERR_END_PATTERN_WITH_UNMATCHED_PARENTHESIS = -117
fld public final static int ERR_GROUP_NUMBER_OVER_FOR_CAPTURE_HISTORY = -222
fld public final static int ERR_INVALID_ARGUMENT = -30
fld public final static int ERR_INVALID_BACKREF = -208
fld public final static int ERR_INVALID_CHAR_IN_GROUP_NAME = -216
fld public final static int ERR_INVALID_CHAR_PROPERTY_NAME = -223
fld public final static int ERR_INVALID_CODE_POINT_VALUE = -400
fld public final static int ERR_INVALID_COMBINATION_OF_OPTIONS = -403
fld public final static int ERR_INVALID_CONDITION_PATTERN = -124
fld public final static int ERR_INVALID_GROUP_NAME = -215
fld public final static int ERR_INVALID_LOOK_BEHIND_PATTERN = -122
fld public final static int ERR_INVALID_POSIX_BRACKET_TYPE = -121
fld public final static int ERR_INVALID_REPEAT_RANGE_PATTERN = -123
fld public final static int ERR_INVALID_WIDE_CHAR_VALUE = -400
fld public final static int ERR_MATCH_STACK_LIMIT_OVER = -15
fld public final static int ERR_MEMORY = -5
fld public final static int ERR_META_CODE_SYNTAX = -108
fld public final static int ERR_MISMATCH_CODE_LENGTH_IN_CLASS_RANGE = -204
fld public final static int ERR_MULTIPLEX_DEFINED_NAME = -219
fld public final static int ERR_MULTIPLEX_DEFINITION_NAME_CALL = -220
fld public final static int ERR_NESTED_REPEAT_OPERATOR = -115
fld public final static int ERR_NEVER_ENDING_RECURSION = -221
fld public final static int ERR_NOT_SUPPORTED_ENCODING_COMBINATION = -402
fld public final static int ERR_NO_SUCH_ENCODNG = -1005
fld public final static int ERR_NUMBERED_BACKREF_OR_CALL_NOT_ALLOWED = -209
fld public final static int ERR_PARSER_BUG = -11
fld public final static int ERR_PREMATURE_END_OF_CHAR_CLASS = -103
fld public final static int ERR_SPECIFIED_ENCODING_CANT_CONVERT_TO_WIDE_CHAR = -22
fld public final static int ERR_STACK_BUG = -12
fld public final static int ERR_TARGET_OF_REPEAT_OPERATOR_INVALID = -114
fld public final static int ERR_TARGET_OF_REPEAT_OPERATOR_NOT_SPECIFIED = -113
fld public final static int ERR_TOO_BIG_BACKREF_NUMBER = -207
fld public final static int ERR_TOO_BIG_NUMBER = -200
fld public final static int ERR_TOO_BIG_NUMBER_FOR_REPEAT_RANGE = -201
fld public final static int ERR_TOO_BIG_WIDE_CHAR_VALUE = -401
fld public final static int ERR_TOO_LONG_WIDE_CHAR_VALUE = -212
fld public final static int ERR_TOO_MANY_CAPTURE_GROUPS = -224
fld public final static int ERR_TOO_MANY_MULTI_BYTE_RANGES = -205
fld public final static int ERR_TOO_SHORT_DIGITS = -210
fld public final static int ERR_TOO_SHORT_MULTI_BYTE_STRING = -206
fld public final static int ERR_TRANSCODER_ALREADY_REGISTERED = -1007
fld public final static int ERR_TRANSCODER_CLASS_DEF_NOT_FOUND = -1008
fld public final static int ERR_TRANSCODER_LOAD_ERROR = -1009
fld public final static int ERR_TYPE_BUG = -6
fld public final static int ERR_UNDEFINED_BYTECODE = -13
fld public final static int ERR_UNDEFINED_GROUP_OPTION = -119
fld public final static int ERR_UNDEFINED_GROUP_REFERENCE = -218
fld public final static int ERR_UNDEFINED_NAME_REFERENCE = -217
fld public final static int ERR_UNEXPECTED_BYTECODE = -14
fld public final static int ERR_UNMATCHED_CLOSE_PARENTHESIS = -116
fld public final static int ERR_UNMATCHED_RANGE_SPECIFIER_IN_CHAR_CLASS = -112
fld public final static int ERR_UPPER_SMALLER_THAN_LOWER_IN_REPEAT_RANGE = -202
fld public final static int MISMATCH = -1
fld public final static int NORMAL = 0
fld public final static int NO_SUPPORT_CONFIG = -2

CLSS public abstract interface org.jcodings.exception.ErrorMessages
fld public final static java.lang.String ERR_COULD_NOT_REPLICATE = "could not replicate <%n> encoding"
fld public final static java.lang.String ERR_ENCODING_ALIAS_ALREADY_REGISTERED = "encoding alias already registerd <%n>"
fld public final static java.lang.String ERR_ENCODING_ALREADY_REGISTERED = "encoding already registerd <%n>"
fld public final static java.lang.String ERR_ENCODING_CLASS_DEF_NOT_FOUND = "encoding class <%n> not found"
fld public final static java.lang.String ERR_ENCODING_LOAD_ERROR = "problem loading encoding <%n>"
fld public final static java.lang.String ERR_ENCODING_REPLICA_ALREADY_REGISTERED = "encoding replica already registerd <%n>"
fld public final static java.lang.String ERR_ILLEGAL_CHARACTER = "illegal character"
fld public final static java.lang.String ERR_INVALID_CHAR_PROPERTY_NAME = "invalid character property name <%n>"
fld public final static java.lang.String ERR_INVALID_CODE_POINT_VALUE = "invalid code point value"
fld public final static java.lang.String ERR_NO_SUCH_ENCODNG = "no such encoding <%n>"
fld public final static java.lang.String ERR_TOO_BIG_WIDE_CHAR_VALUE = "too big wide-char value"
fld public final static java.lang.String ERR_TOO_LONG_WIDE_CHAR_VALUE = "too long wide-char value"
fld public final static java.lang.String ERR_TRANSCODER_ALREADY_REGISTERED = "transcoder from <%n> has been already registered"
fld public final static java.lang.String ERR_TRANSCODER_CLASS_DEF_NOT_FOUND = "transcoder class <%n> not found"
fld public final static java.lang.String ERR_TRANSCODER_LOAD_ERROR = "problem loading transcoder <%n>"
fld public final static java.lang.String ERR_TYPE_BUG = "undefined type (bug)"

CLSS public org.jcodings.exception.InternalException
cons public init(java.lang.String)
cons public init(java.lang.String,byte[],int,int)
cons public init(java.lang.String,java.lang.String)
supr org.jcodings.exception.JCodingsException
hfds serialVersionUID

CLSS public org.jcodings.exception.JCodingsException
cons public init(java.lang.String)
cons public init(java.lang.String,byte[],int,int)
cons public init(java.lang.String,java.lang.String)
supr java.lang.RuntimeException

CLSS public org.jcodings.exception.TranscoderException
cons public init(java.lang.String)
cons public init(java.lang.String,byte[],int,int)
cons public init(java.lang.String,java.lang.String)
supr org.jcodings.exception.JCodingsException

CLSS public final org.jcodings.specific.ASCIIEncoding
cons protected init()
fld public final static org.jcodings.specific.ASCIIEncoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public final byte[] toLowerCaseTable()
meth public java.lang.String getCharsetName()
supr org.jcodings.SingleByteEncoding

CLSS public final org.jcodings.specific.BIG5Encoding
cons protected init()
fld public final static org.jcodings.specific.BIG5Encoding INSTANCE
meth public java.lang.String getCharsetName()
supr org.jcodings.specific.BaseBIG5Encoding
hfds BIG5,Big5EncLen

CLSS public abstract org.jcodings.specific.BaseBIG5Encoding
cons protected init(java.lang.String,int[],int)
meth public boolean isCodeCType(int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
supr org.jcodings.CanBeTrailTableEncoding
hfds BIG5Trans,BIG5_CAN_BE_TRAIL_TABLE,TransBase

CLSS public final org.jcodings.specific.Big5HKSCSEncoding
cons protected init()
fld public final static org.jcodings.specific.Big5HKSCSEncoding INSTANCE
supr org.jcodings.specific.BaseBIG5Encoding
hfds Big5HKSCSEncLen

CLSS public final org.jcodings.specific.Big5UAOEncoding
cons protected init()
fld public final static org.jcodings.specific.Big5UAOEncoding INSTANCE
supr org.jcodings.specific.BaseBIG5Encoding
hfds Big5UAOEncLen

CLSS public final org.jcodings.specific.CESU8Encoding
cons protected init()
fld public final static org.jcodings.specific.CESU8Encoding INSTANCE
meth public boolean isNewLine(byte[],int,int)
meth public boolean isReverseMatchAllowed(byte[],int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int leftAdjustCharHead(byte[],int,int,int)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
meth public java.lang.String getCharsetName()
supr org.jcodings.unicode.UnicodeEncoding
hfds CESU8EncLen,CESU8Trans,INVALID_CODE_FE,INVALID_CODE_FF,USE_INVALID_CODE_SCHEME,VALID_CODE_LIMIT

CLSS public final org.jcodings.specific.CP949Encoding
cons protected init()
fld public final static org.jcodings.specific.CP949Encoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
meth public java.lang.String getCharsetName()
supr org.jcodings.CanBeTrailTableEncoding
hfds CP949,CP949EncLen,CP949Trans,CP949_CAN_BE_TRAIL_TABLE

CLSS public final org.jcodings.specific.EUCJPEncoding
cons protected init()
fld public final static org.jcodings.specific.EUCJPEncoding INSTANCE
meth protected boolean isLead(int)
meth public boolean isCodeCType(int,int)
meth public boolean isReverseMatchAllowed(byte[],int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int propertyNameToCType(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
meth public java.lang.String getCharsetName()
supr org.jcodings.EucEncoding
hfds EUCJPTrans,EUC_JP

CLSS public org.jcodings.specific.EUCKREncoding
cons protected init()
cons protected init(java.lang.String)
fld public final static org.jcodings.specific.EUCKREncoding INSTANCE
meth protected boolean isLead(int)
meth public boolean isCodeCType(int,int)
meth public boolean isReverseMatchAllowed(byte[],int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
supr org.jcodings.EucEncoding
hfds EUCKREncLen,EUCKRTrans

CLSS public final org.jcodings.specific.EUCTWEncoding
cons protected init()
fld public final static org.jcodings.specific.EUCTWEncoding INSTANCE
meth protected boolean isLead(int)
meth public boolean isCodeCType(int,int)
meth public boolean isReverseMatchAllowed(byte[],int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
meth public java.lang.String getCharsetName()
supr org.jcodings.EucEncoding
hfds EUCTWEncLen,EUCTWTrans,EUC_TW

CLSS public final org.jcodings.specific.EmacsMuleEncoding
cons protected init()
fld public final static org.jcodings.specific.EmacsMuleEncoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public boolean isReverseMatchAllowed(byte[],int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int leftAdjustCharHead(byte[],int,int,int)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
supr org.jcodings.MultiByteEncoding
hfds EmacsMuleEncLen,EmacsMuleTrans

CLSS public final org.jcodings.specific.GB18030Encoding
cons protected init()
fld public final static org.jcodings.specific.GB18030Encoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public boolean isReverseMatchAllowed(byte[],int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int leftAdjustCharHead(byte[],int,int,int)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
meth public java.lang.String getCharsetName()
supr org.jcodings.MultiByteEncoding
hfds C1,C2,C4,CM,GB18030,GB18030Trans,GB18030_MAP
hcls State

CLSS public final org.jcodings.specific.GB2312Encoding
cons protected init()
fld public final static org.jcodings.specific.GB2312Encoding INSTANCE
supr org.jcodings.specific.EUCKREncoding

CLSS public final org.jcodings.specific.GBKEncoding
cons protected init()
fld public final static org.jcodings.specific.GBKEncoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
meth public java.lang.String getCharsetName()
supr org.jcodings.CanBeTrailTableEncoding
hfds GBK,GBKEncLen,GBKTrans,GBK_CAN_BE_TRAIL_TABLE

CLSS public final org.jcodings.specific.ISO8859_10Encoding
cons protected init()
fld public final static org.jcodings.specific.ISO8859_10Encoding INSTANCE
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
supr org.jcodings.ISOEncoding
hfds ISO8859_10CaseFoldMap,ISO8859_10CtypeTable,ISO8859_10ToLowerCaseTable

CLSS public final org.jcodings.specific.ISO8859_11Encoding
cons protected init()
fld public final static org.jcodings.specific.ISO8859_11Encoding INSTANCE
meth public final byte[] toLowerCaseTable()
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public org.jcodings.CaseFoldCodeItem[] caseFoldCodesByString(int,byte[],int,int)
meth public void applyAllCaseFold(int,org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
supr org.jcodings.ISOEncoding
hfds ISO8859_11CtypeTable

CLSS public final org.jcodings.specific.ISO8859_13Encoding
cons protected init()
fld public final static org.jcodings.specific.ISO8859_13Encoding INSTANCE
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
supr org.jcodings.ISOEncoding
hfds ISO8859_13CaseFoldMap,ISO8859_13CtypeTable,ISO8859_13ToLowerCaseTable

CLSS public final org.jcodings.specific.ISO8859_14Encoding
cons protected init()
fld public final static org.jcodings.specific.ISO8859_14Encoding INSTANCE
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
supr org.jcodings.ISOEncoding
hfds ISO8859_14CaseFoldMap,ISO8859_14CtypeTable,ISO8859_14ToLowerCaseTable

CLSS public final org.jcodings.specific.ISO8859_15Encoding
cons protected init()
fld public final static org.jcodings.specific.ISO8859_15Encoding INSTANCE
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
supr org.jcodings.ISOEncoding
hfds ISO8859_15CaseFoldMap,ISO8859_15CtypeTable,ISO8859_15ToLowerCaseTable

CLSS public final org.jcodings.specific.ISO8859_16Encoding
cons protected init()
fld public final static org.jcodings.specific.ISO8859_16Encoding INSTANCE
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
supr org.jcodings.ISOEncoding
hfds ISO8859_16CaseFoldMap,ISO8859_16CtypeTable,ISO8859_16ToLowerCaseTable

CLSS public final org.jcodings.specific.ISO8859_1Encoding
cons protected init()
fld public final static org.jcodings.specific.ISO8859_1Encoding INSTANCE
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
meth public org.jcodings.CaseFoldCodeItem[] caseFoldCodesByString(int,byte[],int,int)
meth public void applyAllCaseFold(int,org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
supr org.jcodings.ISOEncoding
hfds ISO8859_1CaseFoldMap,ISO8859_1CtypeTable,ISO8859_1ToLowerCaseTable,ISO8859_1ToUpperCaseTable

CLSS public final org.jcodings.specific.ISO8859_2Encoding
cons protected init()
fld public final static org.jcodings.specific.ISO8859_2Encoding INSTANCE
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
supr org.jcodings.ISOEncoding
hfds ISO8859_2CaseFoldMap,ISO8859_2CtypeTable,ISO8859_2ToLowerCaseTable

CLSS public final org.jcodings.specific.ISO8859_3Encoding
cons protected init()
fld public final static org.jcodings.specific.ISO8859_3Encoding INSTANCE
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
supr org.jcodings.ISOEncoding
hfds DOTLESS_i,ISO8859_3CaseFoldMap,ISO8859_3CtypeTable,ISO8859_3ToLowerCaseTable,I_WITH_DOT_ABOVE

CLSS public final org.jcodings.specific.ISO8859_4Encoding
cons protected init()
fld public final static org.jcodings.specific.ISO8859_4Encoding INSTANCE
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
supr org.jcodings.ISOEncoding
hfds ISO8859_4CaseFoldMap,ISO8859_4CtypeTable,ISO8859_4ToLowerCaseTable

CLSS public final org.jcodings.specific.ISO8859_5Encoding
cons protected init()
fld public final static org.jcodings.specific.ISO8859_5Encoding INSTANCE
meth public final byte[] toLowerCaseTable()
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
supr org.jcodings.ISOEncoding
hfds ISO8859_5CaseFoldMap,ISO8859_5CtypeTable,ISO8859_5ToLowerCaseTable

CLSS public final org.jcodings.specific.ISO8859_6Encoding
cons protected init()
fld public final static org.jcodings.specific.ISO8859_6Encoding INSTANCE
meth public final byte[] toLowerCaseTable()
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public org.jcodings.CaseFoldCodeItem[] caseFoldCodesByString(int,byte[],int,int)
meth public void applyAllCaseFold(int,org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
supr org.jcodings.ISOEncoding
hfds ISO8859_6CtypeTable

CLSS public final org.jcodings.specific.ISO8859_7Encoding
cons protected init()
fld public final static org.jcodings.specific.ISO8859_7Encoding INSTANCE
meth public final byte[] toLowerCaseTable()
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
supr org.jcodings.ISOEncoding
hfds ISO8859_7CaseFoldMap,ISO8859_7CtypeTable,ISO8859_7ToLowerCaseTable

CLSS public final org.jcodings.specific.ISO8859_8Encoding
cons protected init()
fld public final static org.jcodings.specific.ISO8859_8Encoding INSTANCE
meth public final byte[] toLowerCaseTable()
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public org.jcodings.CaseFoldCodeItem[] caseFoldCodesByString(int,byte[],int,int)
meth public void applyAllCaseFold(int,org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
supr org.jcodings.ISOEncoding
hfds ISO8859_8CtypeTable

CLSS public final org.jcodings.specific.ISO8859_9Encoding
cons protected init()
fld public final static org.jcodings.specific.ISO8859_9Encoding INSTANCE
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
supr org.jcodings.ISOEncoding
hfds DOTLESS_i,ISO8859_9CaseFoldMap,ISO8859_9CtypeTable,ISO8859_9ToLowerCaseTable,I_WITH_DOT_ABOVE

CLSS public final org.jcodings.specific.KOI8Encoding
cons protected init()
fld public final static org.jcodings.specific.KOI8Encoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
supr org.jcodings.CaseFoldMapEncoding
hfds ENC_CASE_FOLD_ASCII_CASE,KOI8_CaseFoldMap,KOI8_CtypeTable,KOI8_ToLowerCaseTable,ONIGENC_CASE_FOLD_NONASCII_CASE

CLSS public final org.jcodings.specific.KOI8REncoding
cons protected init()
fld public final static org.jcodings.specific.KOI8REncoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
supr org.jcodings.CaseFoldMapEncoding
hfds KOI8R_CaseFoldMap,KOI8R_CtypeTable,KOI8R_ToLowerCaseTable

CLSS public final org.jcodings.specific.KOI8UEncoding
cons protected init()
fld public final static org.jcodings.specific.KOI8UEncoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
supr org.jcodings.CaseFoldMapEncoding
hfds KOI8U_CaseFoldMap,KOI8U_CtypeTable,KOI8U_ToLowerCaseTable

CLSS public final org.jcodings.specific.NonStrictEUCJPEncoding
cons protected init()
fld public final static org.jcodings.specific.NonStrictEUCJPEncoding INSTANCE
meth protected boolean isLead(int)
meth public boolean isCodeCType(int,int)
meth public boolean isReverseMatchAllowed(byte[],int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int propertyNameToCType(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
supr org.jcodings.EucEncoding

CLSS public final org.jcodings.specific.NonStrictSJISEncoding
cons protected init()
fld public final static org.jcodings.specific.NonStrictSJISEncoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int propertyNameToCType(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
meth public java.lang.String getCharsetName()
supr org.jcodings.CanBeTrailTableEncoding

CLSS public final org.jcodings.specific.NonStrictUTF8Encoding
cons protected init()
fld public final static org.jcodings.specific.NonStrictUTF8Encoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public boolean isNewLine(byte[],int,int)
meth public boolean isReverseMatchAllowed(byte[],int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int leftAdjustCharHead(byte[],int,int,int)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
meth public java.lang.String getCharsetName()
supr org.jcodings.unicode.UnicodeEncoding
hfds UTF8EncLen

CLSS public final org.jcodings.specific.SJISEncoding
cons protected init()
fld public final static org.jcodings.specific.SJISEncoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int propertyNameToCType(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
meth public java.lang.String getCharsetName()
supr org.jcodings.CanBeTrailTableEncoding
hfds SjisTrans

CLSS public final org.jcodings.specific.USASCIIEncoding
cons protected init()
fld public final static org.jcodings.specific.USASCIIEncoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public final byte[] toLowerCaseTable()
meth public int length(byte[],int,int)
meth public java.lang.String getCharsetName()
supr org.jcodings.SingleByteEncoding

CLSS public final org.jcodings.specific.UTF16BEEncoding
cons protected init()
fld public final static org.jcodings.specific.UTF16BEEncoding INSTANCE
meth public boolean isNewLine(byte[],int,int)
meth public boolean isReverseMatchAllowed(byte[],int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int leftAdjustCharHead(byte[],int,int,int)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
supr org.jcodings.unicode.UnicodeEncoding
hfds UTF16EncLen

CLSS public final org.jcodings.specific.UTF16LEEncoding
cons protected init()
fld public final static org.jcodings.specific.UTF16LEEncoding INSTANCE
meth public boolean isNewLine(byte[],int,int)
meth public boolean isReverseMatchAllowed(byte[],int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int leftAdjustCharHead(byte[],int,int,int)
meth public int length(byte)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
supr org.jcodings.unicode.UnicodeEncoding

CLSS public final org.jcodings.specific.UTF32BEEncoding
cons protected init()
fld public static org.jcodings.specific.UTF32BEEncoding INSTANCE
meth public boolean isNewLine(byte[],int,int)
meth public int codeToMbc(int,byte[],int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
supr org.jcodings.unicode.FixedWidthUnicodeEncoding

CLSS public final org.jcodings.specific.UTF32LEEncoding
cons protected init()
fld public static org.jcodings.specific.UTF32LEEncoding INSTANCE
meth public boolean isNewLine(byte[],int,int)
meth public int codeToMbc(int,byte[],int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
supr org.jcodings.unicode.FixedWidthUnicodeEncoding

CLSS public final org.jcodings.specific.UTF8Encoding
cons protected init()
fld public final static org.jcodings.specific.UTF8Encoding INSTANCE
meth public boolean isNewLine(byte[],int,int)
meth public boolean isReverseMatchAllowed(byte[],int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int leftAdjustCharHead(byte[],int,int,int)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
meth public java.lang.String getCharsetName()
supr org.jcodings.unicode.UnicodeEncoding
hfds UTF8EncLen,UTF8Trans

CLSS public final org.jcodings.specific.Windows_1250Encoding
cons protected init()
fld public final static org.jcodings.specific.Windows_1250Encoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
supr org.jcodings.CaseFoldMapEncoding
hfds CP1250_CaseFoldMap,CP1250_CtypeTable,CP1250_ToLowerCaseTable

CLSS public final org.jcodings.specific.Windows_1251Encoding
cons protected init()
fld public final static org.jcodings.specific.Windows_1251Encoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
supr org.jcodings.CaseFoldMapEncoding
hfds CP1251_CaseFoldMap,CP1251_CtypeTable,CP1251_ToLowerCaseTable

CLSS public final org.jcodings.specific.Windows_1252Encoding
cons protected init()
fld public final static org.jcodings.specific.Windows_1252Encoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
supr org.jcodings.CaseFoldMapEncoding
hfds CP1252_CaseFoldMap,CP1252_CtypeTable,CP1252_ToLowerCaseTable

CLSS public final org.jcodings.specific.Windows_1253Encoding
cons protected init()
fld public final static org.jcodings.specific.Windows_1253Encoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
supr org.jcodings.CaseFoldMapEncoding
hfds CP1253_CaseFoldMap,CP1253_CtypeTable,CP1253_ToLowerCaseTable

CLSS public final org.jcodings.specific.Windows_1254Encoding
cons protected init()
fld public final static org.jcodings.specific.Windows_1254Encoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
supr org.jcodings.CaseFoldMapEncoding
hfds CP1254_CaseFoldMap,CP1254_CtypeTable,CP1254_ToLowerCaseTable,DOTLESS_i,I_WITH_DOT_ABOVE

CLSS public final org.jcodings.specific.Windows_1257Encoding
cons protected init()
fld public final static org.jcodings.specific.Windows_1257Encoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
supr org.jcodings.CaseFoldMapEncoding
hfds CP1257_CaseFoldMap,CP1257_CtypeTable,CP1257_ToLowerCaseTable,DOTLESS_i,I_WITH_DOT_ABOVE

CLSS public final org.jcodings.specific.Windows_31JEncoding
cons protected init()
fld public final static org.jcodings.specific.Windows_31JEncoding INSTANCE
meth public boolean isCodeCType(int,int)
meth public int codeToMbc(int,byte[],int)
meth public int codeToMbcLength(int)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int propertyNameToCType(byte[],int,int)
meth public int[] ctypeCodeRange(int,org.jcodings.IntHolder)
meth public java.lang.String getCharsetName()
supr org.jcodings.CanBeTrailTableEncoding

CLSS public org.jcodings.spi.Charsets
cons public init()
meth public java.nio.charset.Charset charsetForName(java.lang.String)
meth public java.util.Iterator<java.nio.charset.Charset> charsets()
supr java.nio.charset.spi.CharsetProvider
hfds charsets

CLSS public org.jcodings.spi.ISO_8859_16
fld public final static org.jcodings.spi.ISO_8859_16 INSTANCE
meth public boolean contains(java.nio.charset.Charset)
meth public java.nio.charset.CharsetDecoder newDecoder()
meth public java.nio.charset.CharsetEncoder newEncoder()
supr java.nio.charset.Charset
hcls Decoder,Encoder

CLSS public final !enum org.jcodings.transcode.AsciiCompatibility
fld public final static org.jcodings.transcode.AsciiCompatibility CONVERTER
fld public final static org.jcodings.transcode.AsciiCompatibility DECODER
fld public final static org.jcodings.transcode.AsciiCompatibility ENCODER
meth public boolean isConverter()
meth public boolean isDecoder()
meth public boolean isEncoder()
meth public static org.jcodings.transcode.AsciiCompatibility valueOf(java.lang.String)
meth public static org.jcodings.transcode.AsciiCompatibility[] values()
supr java.lang.Enum<org.jcodings.transcode.AsciiCompatibility>

CLSS public final org.jcodings.transcode.EConv
fld public byte[] destination
fld public byte[] replacementEncoding
fld public byte[] replacementString
fld public byte[] source
fld public final org.jcodings.transcode.EConv$LastError lastError
fld public int numTranscoders
fld public int replacementLength
fld public org.jcodings.Encoding destinationEncoding
fld public org.jcodings.Encoding sourceEncoding
fld public org.jcodings.transcode.EConv$EConvElement[] elements
fld public org.jcodings.transcode.Transcoding lastTranscoding
innr public final static EConvElement
innr public final static LastError
intf org.jcodings.transcode.EConvFlags
meth public boolean addConverter(byte[],byte[],int)
meth public boolean equals(java.lang.Object)
meth public byte[] encodingToInsertOutput()
meth public int insertOutput(byte[],int,int,byte[])
meth public int makeReplacement()
meth public int putbackable()
meth public int setReplacement(byte[],int,int,byte[])
meth public java.lang.String toString()
meth public java.lang.String toStringFull()
meth public org.jcodings.transcode.EConvResult convert(byte[],org.jcodings.Ptr,int,byte[],org.jcodings.Ptr,int,int)
meth public void binmode()
meth public void close()
meth public void putback(byte[],int,int)
supr java.lang.Object
hfds NULL_POINTER,NULL_STRING,flags,inBuf,numFinished,started

CLSS public final static org.jcodings.transcode.EConv$EConvElement
 outer org.jcodings.transcode.EConv
fld public final org.jcodings.transcode.Transcoding transcoding
meth public java.lang.String toString()
supr java.lang.Object
hfds lastResult

CLSS public final static org.jcodings.transcode.EConv$LastError
 outer org.jcodings.transcode.EConv
cons public init()
meth public byte[] getDestination()
meth public byte[] getErrorBytes()
meth public byte[] getSource()
meth public int getErrorBytesLength()
meth public int getErrorBytesP()
meth public int getReadAgainLength()
meth public java.lang.String toString()
meth public org.jcodings.transcode.EConvResult getResult()
meth public org.jcodings.transcode.Transcoding getErrorTranscoding()
supr java.lang.Object
hfds destination,errorBytes,errorBytesLength,errorBytesP,errorTranscoding,readAgainLength,result,source

CLSS public abstract interface org.jcodings.transcode.EConvFlags
fld public final static int AFTER_OUTPUT = 131072
fld public final static int CRLF_NEWLINE_DECORATOR = 4096
fld public final static int CR_NEWLINE_DECORATOR = 8192
fld public final static int DECORATOR_MASK = 65280
fld public final static int ERROR_HANDLER_MASK = 255
fld public final static int INVALID_MASK = 15
fld public final static int INVALID_REPLACE = 2
fld public final static int MAX_ECFLAGS_DECORATORS = 32
fld public final static int NEWLINE_DECORATOR_MASK = 16128
fld public final static int NEWLINE_DECORATOR_READ_MASK = 3840
fld public final static int NEWLINE_DECORATOR_WRITE_MASK = 12288
fld public final static int PARTIAL_INPUT = 65536
fld public final static int STATEFUL_DECORATOR_MASK = 15728640
fld public final static int UNDEF_HEX_CHARREF = 48
fld public final static int UNDEF_MASK = 240
fld public final static int UNDEF_REPLACE = 32
fld public final static int UNIVERSAL_NEWLINE_DECORATOR = 256
fld public final static int XML_ATTR_CONTENT_DECORATOR = 32768
fld public final static int XML_ATTR_QUOTE_DECORATOR = 1048576
fld public final static int XML_TEXT_DECORATOR = 16384

CLSS public final !enum org.jcodings.transcode.EConvResult
fld public final static org.jcodings.transcode.EConvResult AfterOutput
fld public final static org.jcodings.transcode.EConvResult DestinationBufferFull
fld public final static org.jcodings.transcode.EConvResult Finished
fld public final static org.jcodings.transcode.EConvResult IncompleteInput
fld public final static org.jcodings.transcode.EConvResult InvalidByteSequence
fld public final static org.jcodings.transcode.EConvResult SourceBufferEmpty
fld public final static org.jcodings.transcode.EConvResult UndefinedConversion
meth public boolean isAfterOutput()
meth public boolean isDestinationBufferFull()
meth public boolean isFinished()
meth public boolean isIncompleteInput()
meth public boolean isInvalidByteSequence()
meth public boolean isSourceBufferEmpty()
meth public boolean isUndefinedConversion()
meth public java.lang.String symbolicName()
meth public static org.jcodings.transcode.EConvResult valueOf(java.lang.String)
meth public static org.jcodings.transcode.EConvResult[] values()
supr java.lang.Enum<org.jcodings.transcode.EConvResult>
hfds symbolicName

CLSS public org.jcodings.transcode.TranscodeFunctions
cons public init()
fld public final static byte G0_ASCII = 0
fld public final static byte G0_JISX0201_KATAKANA = 3
fld public final static byte G0_JISX0208_1978 = 1
fld public final static byte G0_JISX0208_1983 = 2
fld public final static byte[] tbl0208
fld public final static int BE = 1
fld public final static int EMACS_MULE_LEADING_CODE_JISX0208_1978 = 144
fld public final static int EMACS_MULE_LEADING_CODE_JISX0208_1983 = 146
fld public final static int LE = 2
fld public final static int from_UTF_16BE_D8toDB_00toFF
fld public final static int from_UTF_16LE_00toFF_D8toDB
fld public final static int iso2022jp_decoder_jisx0208_rest
fld public final static int iso2022jp_kddi_decoder_jisx0208_rest
meth public static int UTF8MAC_BL_ACTION(int,byte)
meth public static int UTF8MAC_BL_MAX_BYTE(int)
meth public static int UTF8MAC_BL_MIN_BYTE(int)
meth public static int UTF8MAC_BL_OFFSET(int,int)
meth public static int escapeXmlAttrQuoteFinish(byte[],byte[],int,int)
meth public static int escapeXmlAttrQuoteInit(byte[])
meth public static int finishCp50220Encoder(byte[],byte[],int,int)
meth public static int finishIso2022jpEncoder(byte[],byte[],int,int)
meth public static int finishIso2022jpKddiEncoder(byte[],byte[],int,int)
meth public static int fromUtf8MacFinish(byte[],byte[],int,int)
meth public static int fromUtf8MacInit(byte[])
meth public static int funSiCp50221Decoder(byte[],byte[],int,int)
meth public static int funSiFromUTF16(byte[],byte[],int,int)
meth public static int funSiFromUTF32(byte[],byte[],int,int)
meth public static int funSiIso2022jpKddiDecoder(byte[],byte[],int,int)
meth public static int funSiIso50220jpDecoder(byte[],byte[],int,int)
meth public static int funSioFromGB18030(byte[],byte[],int,int,int,byte[],int,int)
meth public static int funSioToGB18030(byte[],byte[],int,int,int,byte[],int,int)
meth public static int funSoCp50220Encoder(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoCp50221Decoder(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoCp5022xEncoder(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoEscapeXmlAttrQuote(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoEucjp2Sjis(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoEucjpToStatelessIso2022jp(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoFromCESU8(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoFromGB18030(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoFromUTF16(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoFromUTF16BE(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoFromUTF16LE(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoFromUTF32(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoFromUTF32BE(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoFromUTF32LE(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoFromUtf8Mac(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoIso2022jpDecoder(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoIso2022jpEncoder(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoIso2022jpKddiDecoder(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoIso2022jpKddiEncoder(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoSjis2Eucjp(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoStatelessIso2022jpToEucjp(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoToCESU8(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoToGB18030(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoToUTF16(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoToUTF16BE(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoToUTF16LE(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoToUTF32(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoToUTF32BE(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoToUTF32LE(byte[],byte[],int,int,byte[],int,int)
meth public static int funSoUniversalNewline(byte[],byte[],int,int,byte[],int,int)
meth public static int iso2022jpEncoderResetSequenceSize(byte[])
meth public static int iso2022jpInit(byte[])
meth public static int iso2022jpKddiEncoderResetSequence_size(byte[])
meth public static int iso2022jpKddiInit(byte[])
meth public static int universalNewlineFinish(byte[],byte[],int,int)
meth public static int universalNewlineInit(byte[])
supr java.lang.Object
hfds ESCAPE_END,ESCAPE_NORMAL,MET_CR,MET_CRLF,MET_LF,NEWLINE_JUST_AFTER_CR,NEWLINE_NORMAL,STATUS_BUF_SIZE,TOTAL_BUF_SIZE,from_utf8_mac_nfc2

CLSS public org.jcodings.transcode.TranscodeTableSupport
cons public init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int WORDINDEX_SHIFT_BITS = 2
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
meth public static byte makeSTR1LEN(int)
meth public static int INFO2WORDINDEX(int)
meth public static int WORDINDEX2INFO(int)
meth public static int funsio(int)
meth public static int g4(int,int,int,int)
meth public static int getBT0(int)
meth public static int getBT1(int)
meth public static int getBT2(int)
meth public static int getBT3(int)
meth public static int makeSTR1(int)
meth public static int o1(int)
meth public static int o2(int,int)
meth public static int o2FUNii(int,int)
meth public static int o3(int,int,int)
meth public static int o4(int,int,int,int)
supr java.lang.Object

CLSS public abstract org.jcodings.transcode.Transcoder
cons protected init(byte[],byte[],int,java.lang.String,int,int,int,org.jcodings.transcode.AsciiCompatibility,int)
cons protected init(java.lang.String,java.lang.String,int,java.lang.String,int,int,int,org.jcodings.transcode.AsciiCompatibility,int)
fld public final int inputUnitLength
fld public final int maxInput
fld public final int maxOutput
fld public final org.jcodings.transcode.AsciiCompatibility compatibility
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
meth public boolean hasFinish()
meth public boolean hasStateInit()
meth public byte[] getDestination()
meth public byte[] getSource()
meth public final org.jcodings.transcode.Transcoding transcoding(int)
meth public int finish(byte[],byte[],int,int)
meth public int infoToInfo(byte[],int)
meth public int infoToOutput(byte[],int,byte[],int,int)
meth public int resetSize(byte[])
meth public int resetState(byte[],byte[],int,int)
meth public int startInfoToOutput(byte[],byte[],int,int,int,byte[],int,int)
meth public int startToInfo(byte[],byte[],int,int)
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
meth public int stateFinish(byte[])
meth public int stateInit(byte[])
meth public java.lang.String toString()
meth public java.lang.String toStringFull()
meth public static org.jcodings.transcode.Transcoder load(java.lang.String)
supr java.lang.Object
hfds byteArray,byteArrayHash,destination,hashCode,intArray,source,stateSize,treeStart,wordArrayHash
hcls GenericTranscoderEntry

CLSS public org.jcodings.transcode.TranscoderDB
cons public init()
fld public final static org.jcodings.util.CaseInsensitiveBytesHash<org.jcodings.util.CaseInsensitiveBytesHash<org.jcodings.transcode.TranscoderDB$Entry>> transcoders
innr public abstract interface static SearchPathCallback
innr public final static Entry
intf org.jcodings.transcode.EConvFlags
meth public static int decoratorNames(int,byte[][])
meth public static int searchPath(byte[],byte[],org.jcodings.transcode.TranscoderDB$SearchPathCallback)
meth public static org.jcodings.transcode.EConv alloc(int)
meth public static org.jcodings.transcode.EConv open(byte[],byte[],int)
meth public static org.jcodings.transcode.EConv open(java.lang.String,java.lang.String,int)
meth public static org.jcodings.transcode.TranscoderDB$Entry getEntry(byte[],byte[])
supr java.lang.Object
hcls SearchPathQueue

CLSS public final static org.jcodings.transcode.TranscoderDB$Entry
 outer org.jcodings.transcode.TranscoderDB
meth public byte[] getDestination()
meth public byte[] getSource()
meth public org.jcodings.transcode.Transcoder getTranscoder()
supr java.lang.Object
hfds destination,source,transcoder,transcoderClass

CLSS public abstract interface static org.jcodings.transcode.TranscoderDB$SearchPathCallback
 outer org.jcodings.transcode.TranscoderDB
meth public abstract void call(byte[],byte[],int)

CLSS public org.jcodings.transcode.Transcoding
cons public init(org.jcodings.transcode.Transcoder,int)
fld public final org.jcodings.transcode.Transcoder transcoder
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
meth public java.lang.String toString()
meth public static byte getBT0(int)
meth public static byte getBT1(int)
meth public static byte getBT2(int)
meth public static byte getBT3(int)
meth public static byte getGB4bt0(int)
meth public static byte getGB4bt1(int)
meth public static byte getGB4bt2(int)
meth public static byte getGB4bt3(int)
meth public static int BL_ACTION(org.jcodings.transcode.Transcoding,byte)
meth public static int BL_MAX_BYTE(org.jcodings.transcode.Transcoding)
meth public static int BL_MIN_BYTE(org.jcodings.transcode.Transcoding)
meth public static int BL_OFFSET(org.jcodings.transcode.Transcoding,int)
meth public static int WORDINDEX2INFO(int)
supr java.lang.Object
hfds CALL_FUN_IO,CALL_FUN_SIO,CALL_FUN_SO,CLEANUP,FINISHED,FINISH_FUNC,FOLLOW_BYTE,FOLLOW_INFO,FOUR_BYTE_0,FOUR_BYTE_1,FOUR_BYTE_2,FOUR_BYTE_3,GB_FOUR_BYTE_0,GB_FOUR_BYTE_1,GB_FOUR_BYTE_2,GB_FOUR_BYTE_3,NEXTBYTE,NOMAP_TRANSFER,ONE_BYTE_1,READ_MORE,REPORT_INCOMPLETE,REPORT_INVALID,REPORT_UNDEF,RESUME_AFTER_OUTPUT,RESUME_CALL_FUN_SIO,RESUME_CALL_FUN_SO,RESUME_FINISH_WRITEBUF,RESUME_NOMAP,RESUME_STRING,RESUME_TRANSFER_WRITEBUF,SELECT_TABLE,START,STRING,SUSPEND,TRANSFER_WRITEBUF,TWO_BYTE_1,TWO_BYTE_2,WORDINDEX_SHIFT_BITS,charStart,charStartBytes,flags,inBytes,inCharStart,inP,inPos,nextByte,nextInfo,nextTable,outputIndex,readAgainLength,readBuf,recognizedLength,resumePosition,state,suspendResult,writeBuf,writeBuffLen,writeBuffOff

CLSS public org.jcodings.transcode.specific.Cp50220_decoder_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToInfo(byte[],byte[],int,int)
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
meth public int stateFinish(byte[])
meth public int stateInit(byte[])
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.Cp50220_encoder_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public boolean hasFinish()
meth public int finish(byte[],byte[],int,int)
meth public int resetSize(byte[])
meth public int resetState(byte[],byte[],int,int)
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
meth public int stateFinish(byte[])
meth public int stateInit(byte[])
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.Cp50221_decoder_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToInfo(byte[],byte[],int,int)
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
meth public int stateFinish(byte[])
meth public int stateInit(byte[])
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.Cp50221_encoder_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public boolean hasFinish()
meth public int finish(byte[],byte[],int,int)
meth public int resetSize(byte[])
meth public int resetState(byte[],byte[],int,int)
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
meth public int stateFinish(byte[])
meth public int stateInit(byte[])
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.Escape_xml_attr_quote_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public boolean hasFinish()
meth public int finish(byte[],byte[],int,int)
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
meth public int stateFinish(byte[])
meth public int stateInit(byte[])
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.Eucjp2sjis_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.Eucjp_to_stateless_iso2022jp_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.From_CESU_8_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.From_GB18030_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startInfoToOutput(byte[],byte[],int,int,int,byte[],int,int)
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.From_UTF8_MAC_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public boolean hasFinish()
meth public int finish(byte[],byte[],int,int)
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
meth public int stateFinish(byte[])
meth public int stateInit(byte[])
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.From_UTF_16BE_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.From_UTF_16LE_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.From_UTF_16_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public boolean hasStateInit()
meth public int startToInfo(byte[],byte[],int,int)
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
meth public int stateInit(byte[])
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.From_UTF_32BE_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.From_UTF_32LE_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.From_UTF_32_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public boolean hasStateInit()
meth public int startToInfo(byte[],byte[],int,int)
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
meth public int stateInit(byte[])
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.Iso2022jp_decoder_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToInfo(byte[],byte[],int,int)
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
meth public int stateFinish(byte[])
meth public int stateInit(byte[])
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.Iso2022jp_encoder_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public boolean hasFinish()
meth public int finish(byte[],byte[],int,int)
meth public int resetSize(byte[])
meth public int resetState(byte[],byte[],int,int)
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
meth public int stateFinish(byte[])
meth public int stateInit(byte[])
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.Iso2022jp_kddi_decoder_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToInfo(byte[],byte[],int,int)
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
meth public int stateFinish(byte[])
meth public int stateInit(byte[])
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.Iso2022jp_kddi_encoder_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public boolean hasFinish()
meth public int finish(byte[],byte[],int,int)
meth public int resetSize(byte[])
meth public int resetState(byte[],byte[],int,int)
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
meth public int stateFinish(byte[])
meth public int stateInit(byte[])
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.Sjis2eucjp_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.Stateless_iso2022jp_to_eucjp_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.To_CESU_8_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.To_GB18030_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startInfoToOutput(byte[],byte[],int,int,int,byte[],int,int)
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.To_UTF_16BE_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.To_UTF_16LE_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.To_UTF_16_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public boolean hasStateInit()
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
meth public int stateInit(byte[])
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.To_UTF_32BE_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.To_UTF_32LE_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.To_UTF_32_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public boolean hasStateInit()
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
meth public int stateInit(byte[])
supr org.jcodings.transcode.Transcoder

CLSS public org.jcodings.transcode.specific.Universal_newline_Transcoder
cons protected init()
fld public final static int FOURbt = 6
fld public final static int FUNii = 11
fld public final static int FUNio = 14
fld public final static int FUNsi = 13
fld public final static int FUNsio = 19
fld public final static int FUNso = 15
fld public final static int GB4bt = 18
fld public final static int INVALID = 7
fld public final static int LAST = 28
fld public final static int NOMAP = 1
fld public final static int NOMAP_RESUME_1 = 29
fld public final static int ONEbt = 2
fld public final static int STR1 = 17
fld public final static int THREEbt = 5
fld public final static int TWObt = 3
fld public final static int UNDEF = 9
fld public final static int ZERObt = 10
fld public final static int ZeroXResume_1 = 30
fld public final static int ZeroXResume_2 = 31
fld public final static org.jcodings.transcode.Transcoder INSTANCE
meth public boolean hasFinish()
meth public int finish(byte[],byte[],int,int)
meth public int resetSize(byte[])
meth public int resetState(byte[],byte[],int,int)
meth public int startToOutput(byte[],byte[],int,int,byte[],int,int)
meth public int stateFinish(byte[])
meth public int stateInit(byte[])
supr org.jcodings.transcode.Transcoder
hfds universal_newline

CLSS public abstract org.jcodings.unicode.FixedWidthUnicodeEncoding
cons protected init(java.lang.String,int)
fld protected final int shift
meth public final boolean isReverseMatchAllowed(byte[],int,int)
meth public final int codeToMbcLength(int)
meth public final int leftAdjustCharHead(byte[],int,int,int)
meth public final int length(byte)
meth public final int strCodeAt(byte[],int,int,int)
meth public final int strLength(byte[],int,int)
meth public final int[] ctypeCodeRange(int,org.jcodings.IntHolder)
meth public int length(byte[],int,int)
supr org.jcodings.unicode.UnicodeEncoding

CLSS public final !enum org.jcodings.unicode.UnicodeCodeRange
fld public final static org.jcodings.unicode.UnicodeCodeRange ADLAM
fld public final static org.jcodings.unicode.UnicodeCodeRange ADLM
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_10_0
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_11_0
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_12_0
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_12_1
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_13_0
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_1_1
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_2_0
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_2_1
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_3_0
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_3_1
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_3_2
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_4_0
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_4_1
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_5_0
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_5_1
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_5_2
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_6_0
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_6_1
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_6_2
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_6_3
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_7_0
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_8_0
fld public final static org.jcodings.unicode.UnicodeCodeRange AGE_9_0
fld public final static org.jcodings.unicode.UnicodeCodeRange AGHB
fld public final static org.jcodings.unicode.UnicodeCodeRange AHEX
fld public final static org.jcodings.unicode.UnicodeCodeRange AHOM
fld public final static org.jcodings.unicode.UnicodeCodeRange ALNUM
fld public final static org.jcodings.unicode.UnicodeCodeRange ALPHA
fld public final static org.jcodings.unicode.UnicodeCodeRange ALPHABETIC
fld public final static org.jcodings.unicode.UnicodeCodeRange ANATOLIANHIEROGLYPHS
fld public final static org.jcodings.unicode.UnicodeCodeRange ANY
fld public final static org.jcodings.unicode.UnicodeCodeRange ARAB
fld public final static org.jcodings.unicode.UnicodeCodeRange ARABIC
fld public final static org.jcodings.unicode.UnicodeCodeRange ARMENIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange ARMI
fld public final static org.jcodings.unicode.UnicodeCodeRange ARMN
fld public final static org.jcodings.unicode.UnicodeCodeRange ASCII
fld public final static org.jcodings.unicode.UnicodeCodeRange ASCIIHEXDIGIT
fld public final static org.jcodings.unicode.UnicodeCodeRange ASSIGNED
fld public final static org.jcodings.unicode.UnicodeCodeRange AVESTAN
fld public final static org.jcodings.unicode.UnicodeCodeRange AVST
fld public final static org.jcodings.unicode.UnicodeCodeRange BALI
fld public final static org.jcodings.unicode.UnicodeCodeRange BALINESE
fld public final static org.jcodings.unicode.UnicodeCodeRange BAMU
fld public final static org.jcodings.unicode.UnicodeCodeRange BAMUM
fld public final static org.jcodings.unicode.UnicodeCodeRange BASS
fld public final static org.jcodings.unicode.UnicodeCodeRange BASSAVAH
fld public final static org.jcodings.unicode.UnicodeCodeRange BATAK
fld public final static org.jcodings.unicode.UnicodeCodeRange BATK
fld public final static org.jcodings.unicode.UnicodeCodeRange BENG
fld public final static org.jcodings.unicode.UnicodeCodeRange BENGALI
fld public final static org.jcodings.unicode.UnicodeCodeRange BHAIKSUKI
fld public final static org.jcodings.unicode.UnicodeCodeRange BHKS
fld public final static org.jcodings.unicode.UnicodeCodeRange BIDIC
fld public final static org.jcodings.unicode.UnicodeCodeRange BIDICONTROL
fld public final static org.jcodings.unicode.UnicodeCodeRange BLANK
fld public final static org.jcodings.unicode.UnicodeCodeRange BOPO
fld public final static org.jcodings.unicode.UnicodeCodeRange BOPOMOFO
fld public final static org.jcodings.unicode.UnicodeCodeRange BRAH
fld public final static org.jcodings.unicode.UnicodeCodeRange BRAHMI
fld public final static org.jcodings.unicode.UnicodeCodeRange BRAI
fld public final static org.jcodings.unicode.UnicodeCodeRange BRAILLE
fld public final static org.jcodings.unicode.UnicodeCodeRange BUGI
fld public final static org.jcodings.unicode.UnicodeCodeRange BUGINESE
fld public final static org.jcodings.unicode.UnicodeCodeRange BUHD
fld public final static org.jcodings.unicode.UnicodeCodeRange BUHID
fld public final static org.jcodings.unicode.UnicodeCodeRange C
fld public final static org.jcodings.unicode.UnicodeCodeRange CAKM
fld public final static org.jcodings.unicode.UnicodeCodeRange CANADIANABORIGINAL
fld public final static org.jcodings.unicode.UnicodeCodeRange CANS
fld public final static org.jcodings.unicode.UnicodeCodeRange CARI
fld public final static org.jcodings.unicode.UnicodeCodeRange CARIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange CASED
fld public final static org.jcodings.unicode.UnicodeCodeRange CASEDLETTER
fld public final static org.jcodings.unicode.UnicodeCodeRange CASEIGNORABLE
fld public final static org.jcodings.unicode.UnicodeCodeRange CAUCASIANALBANIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange CC
fld public final static org.jcodings.unicode.UnicodeCodeRange CF
fld public final static org.jcodings.unicode.UnicodeCodeRange CHAKMA
fld public final static org.jcodings.unicode.UnicodeCodeRange CHAM
fld public final static org.jcodings.unicode.UnicodeCodeRange CHANGESWHENCASEFOLDED
fld public final static org.jcodings.unicode.UnicodeCodeRange CHANGESWHENCASEMAPPED
fld public final static org.jcodings.unicode.UnicodeCodeRange CHANGESWHENLOWERCASED
fld public final static org.jcodings.unicode.UnicodeCodeRange CHANGESWHENTITLECASED
fld public final static org.jcodings.unicode.UnicodeCodeRange CHANGESWHENUPPERCASED
fld public final static org.jcodings.unicode.UnicodeCodeRange CHER
fld public final static org.jcodings.unicode.UnicodeCodeRange CHEROKEE
fld public final static org.jcodings.unicode.UnicodeCodeRange CHORASMIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange CHRS
fld public final static org.jcodings.unicode.UnicodeCodeRange CI
fld public final static org.jcodings.unicode.UnicodeCodeRange CLOSEPUNCTUATION
fld public final static org.jcodings.unicode.UnicodeCodeRange CN
fld public final static org.jcodings.unicode.UnicodeCodeRange CNTRL
fld public final static org.jcodings.unicode.UnicodeCodeRange CO
fld public final static org.jcodings.unicode.UnicodeCodeRange COMBININGMARK
fld public final static org.jcodings.unicode.UnicodeCodeRange COMMON
fld public final static org.jcodings.unicode.UnicodeCodeRange CONNECTORPUNCTUATION
fld public final static org.jcodings.unicode.UnicodeCodeRange CONTROL
fld public final static org.jcodings.unicode.UnicodeCodeRange COPT
fld public final static org.jcodings.unicode.UnicodeCodeRange COPTIC
fld public final static org.jcodings.unicode.UnicodeCodeRange CPRT
fld public final static org.jcodings.unicode.UnicodeCodeRange CS
fld public final static org.jcodings.unicode.UnicodeCodeRange CUNEIFORM
fld public final static org.jcodings.unicode.UnicodeCodeRange CURRENCYSYMBOL
fld public final static org.jcodings.unicode.UnicodeCodeRange CWCF
fld public final static org.jcodings.unicode.UnicodeCodeRange CWCM
fld public final static org.jcodings.unicode.UnicodeCodeRange CWL
fld public final static org.jcodings.unicode.UnicodeCodeRange CWT
fld public final static org.jcodings.unicode.UnicodeCodeRange CWU
fld public final static org.jcodings.unicode.UnicodeCodeRange CYPRIOT
fld public final static org.jcodings.unicode.UnicodeCodeRange CYRILLIC
fld public final static org.jcodings.unicode.UnicodeCodeRange CYRL
fld public final static org.jcodings.unicode.UnicodeCodeRange DASH
fld public final static org.jcodings.unicode.UnicodeCodeRange DASHPUNCTUATION
fld public final static org.jcodings.unicode.UnicodeCodeRange DECIMALNUMBER
fld public final static org.jcodings.unicode.UnicodeCodeRange DEFAULTIGNORABLECODEPOINT
fld public final static org.jcodings.unicode.UnicodeCodeRange DEP
fld public final static org.jcodings.unicode.UnicodeCodeRange DEPRECATED
fld public final static org.jcodings.unicode.UnicodeCodeRange DESERET
fld public final static org.jcodings.unicode.UnicodeCodeRange DEVA
fld public final static org.jcodings.unicode.UnicodeCodeRange DEVANAGARI
fld public final static org.jcodings.unicode.UnicodeCodeRange DI
fld public final static org.jcodings.unicode.UnicodeCodeRange DIA
fld public final static org.jcodings.unicode.UnicodeCodeRange DIACRITIC
fld public final static org.jcodings.unicode.UnicodeCodeRange DIAK
fld public final static org.jcodings.unicode.UnicodeCodeRange DIGIT
fld public final static org.jcodings.unicode.UnicodeCodeRange DIVESAKURU
fld public final static org.jcodings.unicode.UnicodeCodeRange DOGR
fld public final static org.jcodings.unicode.UnicodeCodeRange DOGRA
fld public final static org.jcodings.unicode.UnicodeCodeRange DSRT
fld public final static org.jcodings.unicode.UnicodeCodeRange DUPL
fld public final static org.jcodings.unicode.UnicodeCodeRange DUPLOYAN
fld public final static org.jcodings.unicode.UnicodeCodeRange EBASE
fld public final static org.jcodings.unicode.UnicodeCodeRange ECOMP
fld public final static org.jcodings.unicode.UnicodeCodeRange EGYP
fld public final static org.jcodings.unicode.UnicodeCodeRange EGYPTIANHIEROGLYPHS
fld public final static org.jcodings.unicode.UnicodeCodeRange ELBA
fld public final static org.jcodings.unicode.UnicodeCodeRange ELBASAN
fld public final static org.jcodings.unicode.UnicodeCodeRange ELYM
fld public final static org.jcodings.unicode.UnicodeCodeRange ELYMAIC
fld public final static org.jcodings.unicode.UnicodeCodeRange EMOD
fld public final static org.jcodings.unicode.UnicodeCodeRange EMOJI
fld public final static org.jcodings.unicode.UnicodeCodeRange EMOJICOMPONENT
fld public final static org.jcodings.unicode.UnicodeCodeRange EMOJIMODIFIER
fld public final static org.jcodings.unicode.UnicodeCodeRange EMOJIMODIFIERBASE
fld public final static org.jcodings.unicode.UnicodeCodeRange EMOJIPRESENTATION
fld public final static org.jcodings.unicode.UnicodeCodeRange ENCLOSINGMARK
fld public final static org.jcodings.unicode.UnicodeCodeRange EPRES
fld public final static org.jcodings.unicode.UnicodeCodeRange ETHI
fld public final static org.jcodings.unicode.UnicodeCodeRange ETHIOPIC
fld public final static org.jcodings.unicode.UnicodeCodeRange EXT
fld public final static org.jcodings.unicode.UnicodeCodeRange EXTENDEDPICTOGRAPHIC
fld public final static org.jcodings.unicode.UnicodeCodeRange EXTENDER
fld public final static org.jcodings.unicode.UnicodeCodeRange EXTPICT
fld public final static org.jcodings.unicode.UnicodeCodeRange FINALPUNCTUATION
fld public final static org.jcodings.unicode.UnicodeCodeRange FORMAT
fld public final static org.jcodings.unicode.UnicodeCodeRange GEOR
fld public final static org.jcodings.unicode.UnicodeCodeRange GEORGIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange GLAG
fld public final static org.jcodings.unicode.UnicodeCodeRange GLAGOLITIC
fld public final static org.jcodings.unicode.UnicodeCodeRange GONG
fld public final static org.jcodings.unicode.UnicodeCodeRange GONM
fld public final static org.jcodings.unicode.UnicodeCodeRange GOTH
fld public final static org.jcodings.unicode.UnicodeCodeRange GOTHIC
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAN
fld public final static org.jcodings.unicode.UnicodeCodeRange GRANTHA
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPH
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPHEMEBASE
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPHEMECLUSTERBREAK_CONTROL
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPHEMECLUSTERBREAK_CR
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPHEMECLUSTERBREAK_EXTEND
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPHEMECLUSTERBREAK_L
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPHEMECLUSTERBREAK_LF
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPHEMECLUSTERBREAK_LV
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPHEMECLUSTERBREAK_LVT
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPHEMECLUSTERBREAK_PREPEND
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPHEMECLUSTERBREAK_REGIONALINDICATOR
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPHEMECLUSTERBREAK_SPACINGMARK
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPHEMECLUSTERBREAK_T
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPHEMECLUSTERBREAK_V
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPHEMECLUSTERBREAK_ZWJ
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPHEMEEXTEND
fld public final static org.jcodings.unicode.UnicodeCodeRange GRAPHEMELINK
fld public final static org.jcodings.unicode.UnicodeCodeRange GRBASE
fld public final static org.jcodings.unicode.UnicodeCodeRange GREEK
fld public final static org.jcodings.unicode.UnicodeCodeRange GREK
fld public final static org.jcodings.unicode.UnicodeCodeRange GREXT
fld public final static org.jcodings.unicode.UnicodeCodeRange GRLINK
fld public final static org.jcodings.unicode.UnicodeCodeRange GUJARATI
fld public final static org.jcodings.unicode.UnicodeCodeRange GUJR
fld public final static org.jcodings.unicode.UnicodeCodeRange GUNJALAGONDI
fld public final static org.jcodings.unicode.UnicodeCodeRange GURMUKHI
fld public final static org.jcodings.unicode.UnicodeCodeRange GURU
fld public final static org.jcodings.unicode.UnicodeCodeRange HAN
fld public final static org.jcodings.unicode.UnicodeCodeRange HANG
fld public final static org.jcodings.unicode.UnicodeCodeRange HANGUL
fld public final static org.jcodings.unicode.UnicodeCodeRange HANI
fld public final static org.jcodings.unicode.UnicodeCodeRange HANIFIROHINGYA
fld public final static org.jcodings.unicode.UnicodeCodeRange HANO
fld public final static org.jcodings.unicode.UnicodeCodeRange HANUNOO
fld public final static org.jcodings.unicode.UnicodeCodeRange HATR
fld public final static org.jcodings.unicode.UnicodeCodeRange HATRAN
fld public final static org.jcodings.unicode.UnicodeCodeRange HEBR
fld public final static org.jcodings.unicode.UnicodeCodeRange HEBREW
fld public final static org.jcodings.unicode.UnicodeCodeRange HEX
fld public final static org.jcodings.unicode.UnicodeCodeRange HEXDIGIT
fld public final static org.jcodings.unicode.UnicodeCodeRange HIRA
fld public final static org.jcodings.unicode.UnicodeCodeRange HIRAGANA
fld public final static org.jcodings.unicode.UnicodeCodeRange HLUW
fld public final static org.jcodings.unicode.UnicodeCodeRange HMNG
fld public final static org.jcodings.unicode.UnicodeCodeRange HMNP
fld public final static org.jcodings.unicode.UnicodeCodeRange HUNG
fld public final static org.jcodings.unicode.UnicodeCodeRange HYPHEN
fld public final static org.jcodings.unicode.UnicodeCodeRange IDC
fld public final static org.jcodings.unicode.UnicodeCodeRange IDCONTINUE
fld public final static org.jcodings.unicode.UnicodeCodeRange IDEO
fld public final static org.jcodings.unicode.UnicodeCodeRange IDEOGRAPHIC
fld public final static org.jcodings.unicode.UnicodeCodeRange IDS
fld public final static org.jcodings.unicode.UnicodeCodeRange IDSB
fld public final static org.jcodings.unicode.UnicodeCodeRange IDSBINARYOPERATOR
fld public final static org.jcodings.unicode.UnicodeCodeRange IDST
fld public final static org.jcodings.unicode.UnicodeCodeRange IDSTART
fld public final static org.jcodings.unicode.UnicodeCodeRange IDSTRINARYOPERATOR
fld public final static org.jcodings.unicode.UnicodeCodeRange IMPERIALARAMAIC
fld public final static org.jcodings.unicode.UnicodeCodeRange INADLAM
fld public final static org.jcodings.unicode.UnicodeCodeRange INAEGEANNUMBERS
fld public final static org.jcodings.unicode.UnicodeCodeRange INAHOM
fld public final static org.jcodings.unicode.UnicodeCodeRange INALCHEMICALSYMBOLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INALPHABETICPRESENTATIONFORMS
fld public final static org.jcodings.unicode.UnicodeCodeRange INANATOLIANHIEROGLYPHS
fld public final static org.jcodings.unicode.UnicodeCodeRange INANCIENTGREEKMUSICALNOTATION
fld public final static org.jcodings.unicode.UnicodeCodeRange INANCIENTGREEKNUMBERS
fld public final static org.jcodings.unicode.UnicodeCodeRange INANCIENTSYMBOLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INARABIC
fld public final static org.jcodings.unicode.UnicodeCodeRange INARABICEXTENDEDA
fld public final static org.jcodings.unicode.UnicodeCodeRange INARABICMATHEMATICALALPHABETICSYMBOLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INARABICPRESENTATIONFORMSA
fld public final static org.jcodings.unicode.UnicodeCodeRange INARABICPRESENTATIONFORMSB
fld public final static org.jcodings.unicode.UnicodeCodeRange INARABICSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INARMENIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INARROWS
fld public final static org.jcodings.unicode.UnicodeCodeRange INAVESTAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INBALINESE
fld public final static org.jcodings.unicode.UnicodeCodeRange INBAMUM
fld public final static org.jcodings.unicode.UnicodeCodeRange INBAMUMSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INBASICLATIN
fld public final static org.jcodings.unicode.UnicodeCodeRange INBASSAVAH
fld public final static org.jcodings.unicode.UnicodeCodeRange INBATAK
fld public final static org.jcodings.unicode.UnicodeCodeRange INBENGALI
fld public final static org.jcodings.unicode.UnicodeCodeRange INBHAIKSUKI
fld public final static org.jcodings.unicode.UnicodeCodeRange INBLOCKELEMENTS
fld public final static org.jcodings.unicode.UnicodeCodeRange INBOPOMOFO
fld public final static org.jcodings.unicode.UnicodeCodeRange INBOPOMOFOEXTENDED
fld public final static org.jcodings.unicode.UnicodeCodeRange INBOXDRAWING
fld public final static org.jcodings.unicode.UnicodeCodeRange INBRAHMI
fld public final static org.jcodings.unicode.UnicodeCodeRange INBRAILLEPATTERNS
fld public final static org.jcodings.unicode.UnicodeCodeRange INBUGINESE
fld public final static org.jcodings.unicode.UnicodeCodeRange INBUHID
fld public final static org.jcodings.unicode.UnicodeCodeRange INBYZANTINEMUSICALSYMBOLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INCARIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INCAUCASIANALBANIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INCHAKMA
fld public final static org.jcodings.unicode.UnicodeCodeRange INCHAM
fld public final static org.jcodings.unicode.UnicodeCodeRange INCHEROKEE
fld public final static org.jcodings.unicode.UnicodeCodeRange INCHEROKEESUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INCHESSSYMBOLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INCHORASMIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INCJKCOMPATIBILITY
fld public final static org.jcodings.unicode.UnicodeCodeRange INCJKCOMPATIBILITYFORMS
fld public final static org.jcodings.unicode.UnicodeCodeRange INCJKCOMPATIBILITYIDEOGRAPHS
fld public final static org.jcodings.unicode.UnicodeCodeRange INCJKCOMPATIBILITYIDEOGRAPHSSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INCJKRADICALSSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INCJKSTROKES
fld public final static org.jcodings.unicode.UnicodeCodeRange INCJKSYMBOLSANDPUNCTUATION
fld public final static org.jcodings.unicode.UnicodeCodeRange INCJKUNIFIEDIDEOGRAPHS
fld public final static org.jcodings.unicode.UnicodeCodeRange INCJKUNIFIEDIDEOGRAPHSEXTENSIONA
fld public final static org.jcodings.unicode.UnicodeCodeRange INCJKUNIFIEDIDEOGRAPHSEXTENSIONB
fld public final static org.jcodings.unicode.UnicodeCodeRange INCJKUNIFIEDIDEOGRAPHSEXTENSIONC
fld public final static org.jcodings.unicode.UnicodeCodeRange INCJKUNIFIEDIDEOGRAPHSEXTENSIOND
fld public final static org.jcodings.unicode.UnicodeCodeRange INCJKUNIFIEDIDEOGRAPHSEXTENSIONE
fld public final static org.jcodings.unicode.UnicodeCodeRange INCJKUNIFIEDIDEOGRAPHSEXTENSIONF
fld public final static org.jcodings.unicode.UnicodeCodeRange INCJKUNIFIEDIDEOGRAPHSEXTENSIONG
fld public final static org.jcodings.unicode.UnicodeCodeRange INCOMBININGDIACRITICALMARKS
fld public final static org.jcodings.unicode.UnicodeCodeRange INCOMBININGDIACRITICALMARKSEXTENDED
fld public final static org.jcodings.unicode.UnicodeCodeRange INCOMBININGDIACRITICALMARKSFORSYMBOLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INCOMBININGDIACRITICALMARKSSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INCOMBININGHALFMARKS
fld public final static org.jcodings.unicode.UnicodeCodeRange INCOMMONINDICNUMBERFORMS
fld public final static org.jcodings.unicode.UnicodeCodeRange INCONTROLPICTURES
fld public final static org.jcodings.unicode.UnicodeCodeRange INCOPTIC
fld public final static org.jcodings.unicode.UnicodeCodeRange INCOPTICEPACTNUMBERS
fld public final static org.jcodings.unicode.UnicodeCodeRange INCOUNTINGRODNUMERALS
fld public final static org.jcodings.unicode.UnicodeCodeRange INCUNEIFORM
fld public final static org.jcodings.unicode.UnicodeCodeRange INCUNEIFORMNUMBERSANDPUNCTUATION
fld public final static org.jcodings.unicode.UnicodeCodeRange INCURRENCYSYMBOLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INCYPRIOTSYLLABARY
fld public final static org.jcodings.unicode.UnicodeCodeRange INCYRILLIC
fld public final static org.jcodings.unicode.UnicodeCodeRange INCYRILLICEXTENDEDA
fld public final static org.jcodings.unicode.UnicodeCodeRange INCYRILLICEXTENDEDB
fld public final static org.jcodings.unicode.UnicodeCodeRange INCYRILLICEXTENDEDC
fld public final static org.jcodings.unicode.UnicodeCodeRange INCYRILLICSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INDESERET
fld public final static org.jcodings.unicode.UnicodeCodeRange INDEVANAGARI
fld public final static org.jcodings.unicode.UnicodeCodeRange INDEVANAGARIEXTENDED
fld public final static org.jcodings.unicode.UnicodeCodeRange INDINGBATS
fld public final static org.jcodings.unicode.UnicodeCodeRange INDIVESAKURU
fld public final static org.jcodings.unicode.UnicodeCodeRange INDOGRA
fld public final static org.jcodings.unicode.UnicodeCodeRange INDOMINOTILES
fld public final static org.jcodings.unicode.UnicodeCodeRange INDUPLOYAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INEARLYDYNASTICCUNEIFORM
fld public final static org.jcodings.unicode.UnicodeCodeRange INEGYPTIANHIEROGLYPHFORMATCONTROLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INEGYPTIANHIEROGLYPHS
fld public final static org.jcodings.unicode.UnicodeCodeRange INELBASAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INELYMAIC
fld public final static org.jcodings.unicode.UnicodeCodeRange INEMOTICONS
fld public final static org.jcodings.unicode.UnicodeCodeRange INENCLOSEDALPHANUMERICS
fld public final static org.jcodings.unicode.UnicodeCodeRange INENCLOSEDALPHANUMERICSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INENCLOSEDCJKLETTERSANDMONTHS
fld public final static org.jcodings.unicode.UnicodeCodeRange INENCLOSEDIDEOGRAPHICSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INETHIOPIC
fld public final static org.jcodings.unicode.UnicodeCodeRange INETHIOPICEXTENDED
fld public final static org.jcodings.unicode.UnicodeCodeRange INETHIOPICEXTENDEDA
fld public final static org.jcodings.unicode.UnicodeCodeRange INETHIOPICSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INGENERALPUNCTUATION
fld public final static org.jcodings.unicode.UnicodeCodeRange INGEOMETRICSHAPES
fld public final static org.jcodings.unicode.UnicodeCodeRange INGEOMETRICSHAPESEXTENDED
fld public final static org.jcodings.unicode.UnicodeCodeRange INGEORGIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INGEORGIANEXTENDED
fld public final static org.jcodings.unicode.UnicodeCodeRange INGEORGIANSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INGLAGOLITIC
fld public final static org.jcodings.unicode.UnicodeCodeRange INGLAGOLITICSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INGOTHIC
fld public final static org.jcodings.unicode.UnicodeCodeRange INGRANTHA
fld public final static org.jcodings.unicode.UnicodeCodeRange INGREEKANDCOPTIC
fld public final static org.jcodings.unicode.UnicodeCodeRange INGREEKEXTENDED
fld public final static org.jcodings.unicode.UnicodeCodeRange INGUJARATI
fld public final static org.jcodings.unicode.UnicodeCodeRange INGUNJALAGONDI
fld public final static org.jcodings.unicode.UnicodeCodeRange INGURMUKHI
fld public final static org.jcodings.unicode.UnicodeCodeRange INHALFWIDTHANDFULLWIDTHFORMS
fld public final static org.jcodings.unicode.UnicodeCodeRange INHANGULCOMPATIBILITYJAMO
fld public final static org.jcodings.unicode.UnicodeCodeRange INHANGULJAMO
fld public final static org.jcodings.unicode.UnicodeCodeRange INHANGULJAMOEXTENDEDA
fld public final static org.jcodings.unicode.UnicodeCodeRange INHANGULJAMOEXTENDEDB
fld public final static org.jcodings.unicode.UnicodeCodeRange INHANGULSYLLABLES
fld public final static org.jcodings.unicode.UnicodeCodeRange INHANIFIROHINGYA
fld public final static org.jcodings.unicode.UnicodeCodeRange INHANUNOO
fld public final static org.jcodings.unicode.UnicodeCodeRange INHATRAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INHEBREW
fld public final static org.jcodings.unicode.UnicodeCodeRange INHERITED
fld public final static org.jcodings.unicode.UnicodeCodeRange INHIGHPRIVATEUSESURROGATES
fld public final static org.jcodings.unicode.UnicodeCodeRange INHIGHSURROGATES
fld public final static org.jcodings.unicode.UnicodeCodeRange INHIRAGANA
fld public final static org.jcodings.unicode.UnicodeCodeRange INIDEOGRAPHICDESCRIPTIONCHARACTERS
fld public final static org.jcodings.unicode.UnicodeCodeRange INIDEOGRAPHICSYMBOLSANDPUNCTUATION
fld public final static org.jcodings.unicode.UnicodeCodeRange INIMPERIALARAMAIC
fld public final static org.jcodings.unicode.UnicodeCodeRange ININDICSIYAQNUMBERS
fld public final static org.jcodings.unicode.UnicodeCodeRange ININSCRIPTIONALPAHLAVI
fld public final static org.jcodings.unicode.UnicodeCodeRange ININSCRIPTIONALPARTHIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INIPAEXTENSIONS
fld public final static org.jcodings.unicode.UnicodeCodeRange INITIALPUNCTUATION
fld public final static org.jcodings.unicode.UnicodeCodeRange INJAVANESE
fld public final static org.jcodings.unicode.UnicodeCodeRange INKAITHI
fld public final static org.jcodings.unicode.UnicodeCodeRange INKANAEXTENDEDA
fld public final static org.jcodings.unicode.UnicodeCodeRange INKANASUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INKANBUN
fld public final static org.jcodings.unicode.UnicodeCodeRange INKANGXIRADICALS
fld public final static org.jcodings.unicode.UnicodeCodeRange INKANNADA
fld public final static org.jcodings.unicode.UnicodeCodeRange INKATAKANA
fld public final static org.jcodings.unicode.UnicodeCodeRange INKATAKANAPHONETICEXTENSIONS
fld public final static org.jcodings.unicode.UnicodeCodeRange INKAYAHLI
fld public final static org.jcodings.unicode.UnicodeCodeRange INKHAROSHTHI
fld public final static org.jcodings.unicode.UnicodeCodeRange INKHITANSMALLSCRIPT
fld public final static org.jcodings.unicode.UnicodeCodeRange INKHMER
fld public final static org.jcodings.unicode.UnicodeCodeRange INKHMERSYMBOLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INKHOJKI
fld public final static org.jcodings.unicode.UnicodeCodeRange INKHUDAWADI
fld public final static org.jcodings.unicode.UnicodeCodeRange INLAO
fld public final static org.jcodings.unicode.UnicodeCodeRange INLATIN1SUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INLATINEXTENDEDA
fld public final static org.jcodings.unicode.UnicodeCodeRange INLATINEXTENDEDADDITIONAL
fld public final static org.jcodings.unicode.UnicodeCodeRange INLATINEXTENDEDB
fld public final static org.jcodings.unicode.UnicodeCodeRange INLATINEXTENDEDC
fld public final static org.jcodings.unicode.UnicodeCodeRange INLATINEXTENDEDD
fld public final static org.jcodings.unicode.UnicodeCodeRange INLATINEXTENDEDE
fld public final static org.jcodings.unicode.UnicodeCodeRange INLEPCHA
fld public final static org.jcodings.unicode.UnicodeCodeRange INLETTERLIKESYMBOLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INLIMBU
fld public final static org.jcodings.unicode.UnicodeCodeRange INLINEARA
fld public final static org.jcodings.unicode.UnicodeCodeRange INLINEARBIDEOGRAMS
fld public final static org.jcodings.unicode.UnicodeCodeRange INLINEARBSYLLABARY
fld public final static org.jcodings.unicode.UnicodeCodeRange INLISU
fld public final static org.jcodings.unicode.UnicodeCodeRange INLISUSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INLOWSURROGATES
fld public final static org.jcodings.unicode.UnicodeCodeRange INLYCIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INLYDIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INMAHAJANI
fld public final static org.jcodings.unicode.UnicodeCodeRange INMAHJONGTILES
fld public final static org.jcodings.unicode.UnicodeCodeRange INMAKASAR
fld public final static org.jcodings.unicode.UnicodeCodeRange INMALAYALAM
fld public final static org.jcodings.unicode.UnicodeCodeRange INMANDAIC
fld public final static org.jcodings.unicode.UnicodeCodeRange INMANICHAEAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INMARCHEN
fld public final static org.jcodings.unicode.UnicodeCodeRange INMASARAMGONDI
fld public final static org.jcodings.unicode.UnicodeCodeRange INMATHEMATICALALPHANUMERICSYMBOLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INMATHEMATICALOPERATORS
fld public final static org.jcodings.unicode.UnicodeCodeRange INMAYANNUMERALS
fld public final static org.jcodings.unicode.UnicodeCodeRange INMEDEFAIDRIN
fld public final static org.jcodings.unicode.UnicodeCodeRange INMEETEIMAYEK
fld public final static org.jcodings.unicode.UnicodeCodeRange INMEETEIMAYEKEXTENSIONS
fld public final static org.jcodings.unicode.UnicodeCodeRange INMENDEKIKAKUI
fld public final static org.jcodings.unicode.UnicodeCodeRange INMEROITICCURSIVE
fld public final static org.jcodings.unicode.UnicodeCodeRange INMEROITICHIEROGLYPHS
fld public final static org.jcodings.unicode.UnicodeCodeRange INMIAO
fld public final static org.jcodings.unicode.UnicodeCodeRange INMISCELLANEOUSMATHEMATICALSYMBOLSA
fld public final static org.jcodings.unicode.UnicodeCodeRange INMISCELLANEOUSMATHEMATICALSYMBOLSB
fld public final static org.jcodings.unicode.UnicodeCodeRange INMISCELLANEOUSSYMBOLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INMISCELLANEOUSSYMBOLSANDARROWS
fld public final static org.jcodings.unicode.UnicodeCodeRange INMISCELLANEOUSSYMBOLSANDPICTOGRAPHS
fld public final static org.jcodings.unicode.UnicodeCodeRange INMISCELLANEOUSTECHNICAL
fld public final static org.jcodings.unicode.UnicodeCodeRange INMODI
fld public final static org.jcodings.unicode.UnicodeCodeRange INMODIFIERTONELETTERS
fld public final static org.jcodings.unicode.UnicodeCodeRange INMONGOLIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INMONGOLIANSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INMRO
fld public final static org.jcodings.unicode.UnicodeCodeRange INMULTANI
fld public final static org.jcodings.unicode.UnicodeCodeRange INMUSICALSYMBOLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INMYANMAR
fld public final static org.jcodings.unicode.UnicodeCodeRange INMYANMAREXTENDEDA
fld public final static org.jcodings.unicode.UnicodeCodeRange INMYANMAREXTENDEDB
fld public final static org.jcodings.unicode.UnicodeCodeRange INNABATAEAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INNANDINAGARI
fld public final static org.jcodings.unicode.UnicodeCodeRange INNEWA
fld public final static org.jcodings.unicode.UnicodeCodeRange INNEWTAILUE
fld public final static org.jcodings.unicode.UnicodeCodeRange INNKO
fld public final static org.jcodings.unicode.UnicodeCodeRange INNOBLOCK
fld public final static org.jcodings.unicode.UnicodeCodeRange INNUMBERFORMS
fld public final static org.jcodings.unicode.UnicodeCodeRange INNUSHU
fld public final static org.jcodings.unicode.UnicodeCodeRange INNYIAKENGPUACHUEHMONG
fld public final static org.jcodings.unicode.UnicodeCodeRange INOGHAM
fld public final static org.jcodings.unicode.UnicodeCodeRange INOLCHIKI
fld public final static org.jcodings.unicode.UnicodeCodeRange INOLDHUNGARIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INOLDITALIC
fld public final static org.jcodings.unicode.UnicodeCodeRange INOLDNORTHARABIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INOLDPERMIC
fld public final static org.jcodings.unicode.UnicodeCodeRange INOLDPERSIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INOLDSOGDIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INOLDSOUTHARABIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INOLDTURKIC
fld public final static org.jcodings.unicode.UnicodeCodeRange INOPTICALCHARACTERRECOGNITION
fld public final static org.jcodings.unicode.UnicodeCodeRange INORIYA
fld public final static org.jcodings.unicode.UnicodeCodeRange INORNAMENTALDINGBATS
fld public final static org.jcodings.unicode.UnicodeCodeRange INOSAGE
fld public final static org.jcodings.unicode.UnicodeCodeRange INOSMANYA
fld public final static org.jcodings.unicode.UnicodeCodeRange INOTTOMANSIYAQNUMBERS
fld public final static org.jcodings.unicode.UnicodeCodeRange INPAHAWHHMONG
fld public final static org.jcodings.unicode.UnicodeCodeRange INPALMYRENE
fld public final static org.jcodings.unicode.UnicodeCodeRange INPAUCINHAU
fld public final static org.jcodings.unicode.UnicodeCodeRange INPHAGSPA
fld public final static org.jcodings.unicode.UnicodeCodeRange INPHAISTOSDISC
fld public final static org.jcodings.unicode.UnicodeCodeRange INPHOENICIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INPHONETICEXTENSIONS
fld public final static org.jcodings.unicode.UnicodeCodeRange INPHONETICEXTENSIONSSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INPLAYINGCARDS
fld public final static org.jcodings.unicode.UnicodeCodeRange INPRIVATEUSEAREA
fld public final static org.jcodings.unicode.UnicodeCodeRange INPSALTERPAHLAVI
fld public final static org.jcodings.unicode.UnicodeCodeRange INREJANG
fld public final static org.jcodings.unicode.UnicodeCodeRange INRUMINUMERALSYMBOLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INRUNIC
fld public final static org.jcodings.unicode.UnicodeCodeRange INSAMARITAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INSAURASHTRA
fld public final static org.jcodings.unicode.UnicodeCodeRange INSCRIPTIONALPAHLAVI
fld public final static org.jcodings.unicode.UnicodeCodeRange INSCRIPTIONALPARTHIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INSHARADA
fld public final static org.jcodings.unicode.UnicodeCodeRange INSHAVIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INSHORTHANDFORMATCONTROLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INSIDDHAM
fld public final static org.jcodings.unicode.UnicodeCodeRange INSINHALA
fld public final static org.jcodings.unicode.UnicodeCodeRange INSINHALAARCHAICNUMBERS
fld public final static org.jcodings.unicode.UnicodeCodeRange INSMALLFORMVARIANTS
fld public final static org.jcodings.unicode.UnicodeCodeRange INSMALLKANAEXTENSION
fld public final static org.jcodings.unicode.UnicodeCodeRange INSOGDIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INSORASOMPENG
fld public final static org.jcodings.unicode.UnicodeCodeRange INSOYOMBO
fld public final static org.jcodings.unicode.UnicodeCodeRange INSPACINGMODIFIERLETTERS
fld public final static org.jcodings.unicode.UnicodeCodeRange INSPECIALS
fld public final static org.jcodings.unicode.UnicodeCodeRange INSUNDANESE
fld public final static org.jcodings.unicode.UnicodeCodeRange INSUNDANESESUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INSUPERSCRIPTSANDSUBSCRIPTS
fld public final static org.jcodings.unicode.UnicodeCodeRange INSUPPLEMENTALARROWSA
fld public final static org.jcodings.unicode.UnicodeCodeRange INSUPPLEMENTALARROWSB
fld public final static org.jcodings.unicode.UnicodeCodeRange INSUPPLEMENTALARROWSC
fld public final static org.jcodings.unicode.UnicodeCodeRange INSUPPLEMENTALMATHEMATICALOPERATORS
fld public final static org.jcodings.unicode.UnicodeCodeRange INSUPPLEMENTALPUNCTUATION
fld public final static org.jcodings.unicode.UnicodeCodeRange INSUPPLEMENTALSYMBOLSANDPICTOGRAPHS
fld public final static org.jcodings.unicode.UnicodeCodeRange INSUPPLEMENTARYPRIVATEUSEAREAA
fld public final static org.jcodings.unicode.UnicodeCodeRange INSUPPLEMENTARYPRIVATEUSEAREAB
fld public final static org.jcodings.unicode.UnicodeCodeRange INSUTTONSIGNWRITING
fld public final static org.jcodings.unicode.UnicodeCodeRange INSYLOTINAGRI
fld public final static org.jcodings.unicode.UnicodeCodeRange INSYMBOLSANDPICTOGRAPHSEXTENDEDA
fld public final static org.jcodings.unicode.UnicodeCodeRange INSYMBOLSFORLEGACYCOMPUTING
fld public final static org.jcodings.unicode.UnicodeCodeRange INSYRIAC
fld public final static org.jcodings.unicode.UnicodeCodeRange INSYRIACSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INTAGALOG
fld public final static org.jcodings.unicode.UnicodeCodeRange INTAGBANWA
fld public final static org.jcodings.unicode.UnicodeCodeRange INTAGS
fld public final static org.jcodings.unicode.UnicodeCodeRange INTAILE
fld public final static org.jcodings.unicode.UnicodeCodeRange INTAITHAM
fld public final static org.jcodings.unicode.UnicodeCodeRange INTAIVIET
fld public final static org.jcodings.unicode.UnicodeCodeRange INTAIXUANJINGSYMBOLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INTAKRI
fld public final static org.jcodings.unicode.UnicodeCodeRange INTAMIL
fld public final static org.jcodings.unicode.UnicodeCodeRange INTAMILSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INTANGUT
fld public final static org.jcodings.unicode.UnicodeCodeRange INTANGUTCOMPONENTS
fld public final static org.jcodings.unicode.UnicodeCodeRange INTANGUTSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INTELUGU
fld public final static org.jcodings.unicode.UnicodeCodeRange INTHAANA
fld public final static org.jcodings.unicode.UnicodeCodeRange INTHAI
fld public final static org.jcodings.unicode.UnicodeCodeRange INTIBETAN
fld public final static org.jcodings.unicode.UnicodeCodeRange INTIFINAGH
fld public final static org.jcodings.unicode.UnicodeCodeRange INTIRHUTA
fld public final static org.jcodings.unicode.UnicodeCodeRange INTRANSPORTANDMAPSYMBOLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INUGARITIC
fld public final static org.jcodings.unicode.UnicodeCodeRange INUNIFIEDCANADIANABORIGINALSYLLABICS
fld public final static org.jcodings.unicode.UnicodeCodeRange INUNIFIEDCANADIANABORIGINALSYLLABICSEXTENDED
fld public final static org.jcodings.unicode.UnicodeCodeRange INVAI
fld public final static org.jcodings.unicode.UnicodeCodeRange INVARIATIONSELECTORS
fld public final static org.jcodings.unicode.UnicodeCodeRange INVARIATIONSELECTORSSUPPLEMENT
fld public final static org.jcodings.unicode.UnicodeCodeRange INVEDICEXTENSIONS
fld public final static org.jcodings.unicode.UnicodeCodeRange INVERTICALFORMS
fld public final static org.jcodings.unicode.UnicodeCodeRange INWANCHO
fld public final static org.jcodings.unicode.UnicodeCodeRange INWARANGCITI
fld public final static org.jcodings.unicode.UnicodeCodeRange INYEZIDI
fld public final static org.jcodings.unicode.UnicodeCodeRange INYIJINGHEXAGRAMSYMBOLS
fld public final static org.jcodings.unicode.UnicodeCodeRange INYIRADICALS
fld public final static org.jcodings.unicode.UnicodeCodeRange INYISYLLABLES
fld public final static org.jcodings.unicode.UnicodeCodeRange INZANABAZARSQUARE
fld public final static org.jcodings.unicode.UnicodeCodeRange ITAL
fld public final static org.jcodings.unicode.UnicodeCodeRange JAVA
fld public final static org.jcodings.unicode.UnicodeCodeRange JAVANESE
fld public final static org.jcodings.unicode.UnicodeCodeRange JOINC
fld public final static org.jcodings.unicode.UnicodeCodeRange JOINCONTROL
fld public final static org.jcodings.unicode.UnicodeCodeRange KAITHI
fld public final static org.jcodings.unicode.UnicodeCodeRange KALI
fld public final static org.jcodings.unicode.UnicodeCodeRange KANA
fld public final static org.jcodings.unicode.UnicodeCodeRange KANNADA
fld public final static org.jcodings.unicode.UnicodeCodeRange KATAKANA
fld public final static org.jcodings.unicode.UnicodeCodeRange KAYAHLI
fld public final static org.jcodings.unicode.UnicodeCodeRange KHAR
fld public final static org.jcodings.unicode.UnicodeCodeRange KHAROSHTHI
fld public final static org.jcodings.unicode.UnicodeCodeRange KHITANSMALLSCRIPT
fld public final static org.jcodings.unicode.UnicodeCodeRange KHMER
fld public final static org.jcodings.unicode.UnicodeCodeRange KHMR
fld public final static org.jcodings.unicode.UnicodeCodeRange KHOJ
fld public final static org.jcodings.unicode.UnicodeCodeRange KHOJKI
fld public final static org.jcodings.unicode.UnicodeCodeRange KHUDAWADI
fld public final static org.jcodings.unicode.UnicodeCodeRange KITS
fld public final static org.jcodings.unicode.UnicodeCodeRange KNDA
fld public final static org.jcodings.unicode.UnicodeCodeRange KTHI
fld public final static org.jcodings.unicode.UnicodeCodeRange L
fld public final static org.jcodings.unicode.UnicodeCodeRange LANA
fld public final static org.jcodings.unicode.UnicodeCodeRange LAO
fld public final static org.jcodings.unicode.UnicodeCodeRange LAOO
fld public final static org.jcodings.unicode.UnicodeCodeRange LATIN
fld public final static org.jcodings.unicode.UnicodeCodeRange LATN
fld public final static org.jcodings.unicode.UnicodeCodeRange LC
fld public final static org.jcodings.unicode.UnicodeCodeRange LEPC
fld public final static org.jcodings.unicode.UnicodeCodeRange LEPCHA
fld public final static org.jcodings.unicode.UnicodeCodeRange LETTER
fld public final static org.jcodings.unicode.UnicodeCodeRange LETTERNUMBER
fld public final static org.jcodings.unicode.UnicodeCodeRange LIMB
fld public final static org.jcodings.unicode.UnicodeCodeRange LIMBU
fld public final static org.jcodings.unicode.UnicodeCodeRange LINA
fld public final static org.jcodings.unicode.UnicodeCodeRange LINB
fld public final static org.jcodings.unicode.UnicodeCodeRange LINEARA
fld public final static org.jcodings.unicode.UnicodeCodeRange LINEARB
fld public final static org.jcodings.unicode.UnicodeCodeRange LINESEPARATOR
fld public final static org.jcodings.unicode.UnicodeCodeRange LISU
fld public final static org.jcodings.unicode.UnicodeCodeRange LL
fld public final static org.jcodings.unicode.UnicodeCodeRange LM
fld public final static org.jcodings.unicode.UnicodeCodeRange LO
fld public final static org.jcodings.unicode.UnicodeCodeRange LOE
fld public final static org.jcodings.unicode.UnicodeCodeRange LOGICALORDEREXCEPTION
fld public final static org.jcodings.unicode.UnicodeCodeRange LOWER
fld public final static org.jcodings.unicode.UnicodeCodeRange LOWERCASE
fld public final static org.jcodings.unicode.UnicodeCodeRange LOWERCASELETTER
fld public final static org.jcodings.unicode.UnicodeCodeRange LT
fld public final static org.jcodings.unicode.UnicodeCodeRange LU
fld public final static org.jcodings.unicode.UnicodeCodeRange LYCI
fld public final static org.jcodings.unicode.UnicodeCodeRange LYCIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange LYDI
fld public final static org.jcodings.unicode.UnicodeCodeRange LYDIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange M
fld public final static org.jcodings.unicode.UnicodeCodeRange MAHAJANI
fld public final static org.jcodings.unicode.UnicodeCodeRange MAHJ
fld public final static org.jcodings.unicode.UnicodeCodeRange MAKA
fld public final static org.jcodings.unicode.UnicodeCodeRange MAKASAR
fld public final static org.jcodings.unicode.UnicodeCodeRange MALAYALAM
fld public final static org.jcodings.unicode.UnicodeCodeRange MAND
fld public final static org.jcodings.unicode.UnicodeCodeRange MANDAIC
fld public final static org.jcodings.unicode.UnicodeCodeRange MANI
fld public final static org.jcodings.unicode.UnicodeCodeRange MANICHAEAN
fld public final static org.jcodings.unicode.UnicodeCodeRange MARC
fld public final static org.jcodings.unicode.UnicodeCodeRange MARCHEN
fld public final static org.jcodings.unicode.UnicodeCodeRange MARK
fld public final static org.jcodings.unicode.UnicodeCodeRange MASARAMGONDI
fld public final static org.jcodings.unicode.UnicodeCodeRange MATH
fld public final static org.jcodings.unicode.UnicodeCodeRange MATHSYMBOL
fld public final static org.jcodings.unicode.UnicodeCodeRange MC
fld public final static org.jcodings.unicode.UnicodeCodeRange ME
fld public final static org.jcodings.unicode.UnicodeCodeRange MEDEFAIDRIN
fld public final static org.jcodings.unicode.UnicodeCodeRange MEDF
fld public final static org.jcodings.unicode.UnicodeCodeRange MEETEIMAYEK
fld public final static org.jcodings.unicode.UnicodeCodeRange MEND
fld public final static org.jcodings.unicode.UnicodeCodeRange MENDEKIKAKUI
fld public final static org.jcodings.unicode.UnicodeCodeRange MERC
fld public final static org.jcodings.unicode.UnicodeCodeRange MERO
fld public final static org.jcodings.unicode.UnicodeCodeRange MEROITICCURSIVE
fld public final static org.jcodings.unicode.UnicodeCodeRange MEROITICHIEROGLYPHS
fld public final static org.jcodings.unicode.UnicodeCodeRange MIAO
fld public final static org.jcodings.unicode.UnicodeCodeRange MLYM
fld public final static org.jcodings.unicode.UnicodeCodeRange MN
fld public final static org.jcodings.unicode.UnicodeCodeRange MODI
fld public final static org.jcodings.unicode.UnicodeCodeRange MODIFIERLETTER
fld public final static org.jcodings.unicode.UnicodeCodeRange MODIFIERSYMBOL
fld public final static org.jcodings.unicode.UnicodeCodeRange MONG
fld public final static org.jcodings.unicode.UnicodeCodeRange MONGOLIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange MRO
fld public final static org.jcodings.unicode.UnicodeCodeRange MROO
fld public final static org.jcodings.unicode.UnicodeCodeRange MTEI
fld public final static org.jcodings.unicode.UnicodeCodeRange MULT
fld public final static org.jcodings.unicode.UnicodeCodeRange MULTANI
fld public final static org.jcodings.unicode.UnicodeCodeRange MYANMAR
fld public final static org.jcodings.unicode.UnicodeCodeRange MYMR
fld public final static org.jcodings.unicode.UnicodeCodeRange N
fld public final static org.jcodings.unicode.UnicodeCodeRange NABATAEAN
fld public final static org.jcodings.unicode.UnicodeCodeRange NAND
fld public final static org.jcodings.unicode.UnicodeCodeRange NANDINAGARI
fld public final static org.jcodings.unicode.UnicodeCodeRange NARB
fld public final static org.jcodings.unicode.UnicodeCodeRange NBAT
fld public final static org.jcodings.unicode.UnicodeCodeRange NCHAR
fld public final static org.jcodings.unicode.UnicodeCodeRange ND
fld public final static org.jcodings.unicode.UnicodeCodeRange NEWA
fld public final static org.jcodings.unicode.UnicodeCodeRange NEWLINE
fld public final static org.jcodings.unicode.UnicodeCodeRange NEWTAILUE
fld public final static org.jcodings.unicode.UnicodeCodeRange NKO
fld public final static org.jcodings.unicode.UnicodeCodeRange NKOO
fld public final static org.jcodings.unicode.UnicodeCodeRange NL
fld public final static org.jcodings.unicode.UnicodeCodeRange NO
fld public final static org.jcodings.unicode.UnicodeCodeRange NONCHARACTERCODEPOINT
fld public final static org.jcodings.unicode.UnicodeCodeRange NONSPACINGMARK
fld public final static org.jcodings.unicode.UnicodeCodeRange NSHU
fld public final static org.jcodings.unicode.UnicodeCodeRange NUMBER
fld public final static org.jcodings.unicode.UnicodeCodeRange NUSHU
fld public final static org.jcodings.unicode.UnicodeCodeRange NYIAKENGPUACHUEHMONG
fld public final static org.jcodings.unicode.UnicodeCodeRange OALPHA
fld public final static org.jcodings.unicode.UnicodeCodeRange ODI
fld public final static org.jcodings.unicode.UnicodeCodeRange OGAM
fld public final static org.jcodings.unicode.UnicodeCodeRange OGHAM
fld public final static org.jcodings.unicode.UnicodeCodeRange OGREXT
fld public final static org.jcodings.unicode.UnicodeCodeRange OIDC
fld public final static org.jcodings.unicode.UnicodeCodeRange OIDS
fld public final static org.jcodings.unicode.UnicodeCodeRange OLCHIKI
fld public final static org.jcodings.unicode.UnicodeCodeRange OLCK
fld public final static org.jcodings.unicode.UnicodeCodeRange OLDHUNGARIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange OLDITALIC
fld public final static org.jcodings.unicode.UnicodeCodeRange OLDNORTHARABIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange OLDPERMIC
fld public final static org.jcodings.unicode.UnicodeCodeRange OLDPERSIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange OLDSOGDIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange OLDSOUTHARABIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange OLDTURKIC
fld public final static org.jcodings.unicode.UnicodeCodeRange OLOWER
fld public final static org.jcodings.unicode.UnicodeCodeRange OMATH
fld public final static org.jcodings.unicode.UnicodeCodeRange OPENPUNCTUATION
fld public final static org.jcodings.unicode.UnicodeCodeRange ORIYA
fld public final static org.jcodings.unicode.UnicodeCodeRange ORKH
fld public final static org.jcodings.unicode.UnicodeCodeRange ORYA
fld public final static org.jcodings.unicode.UnicodeCodeRange OSAGE
fld public final static org.jcodings.unicode.UnicodeCodeRange OSGE
fld public final static org.jcodings.unicode.UnicodeCodeRange OSMA
fld public final static org.jcodings.unicode.UnicodeCodeRange OSMANYA
fld public final static org.jcodings.unicode.UnicodeCodeRange OTHER
fld public final static org.jcodings.unicode.UnicodeCodeRange OTHERALPHABETIC
fld public final static org.jcodings.unicode.UnicodeCodeRange OTHERDEFAULTIGNORABLECODEPOINT
fld public final static org.jcodings.unicode.UnicodeCodeRange OTHERGRAPHEMEEXTEND
fld public final static org.jcodings.unicode.UnicodeCodeRange OTHERIDCONTINUE
fld public final static org.jcodings.unicode.UnicodeCodeRange OTHERIDSTART
fld public final static org.jcodings.unicode.UnicodeCodeRange OTHERLETTER
fld public final static org.jcodings.unicode.UnicodeCodeRange OTHERLOWERCASE
fld public final static org.jcodings.unicode.UnicodeCodeRange OTHERMATH
fld public final static org.jcodings.unicode.UnicodeCodeRange OTHERNUMBER
fld public final static org.jcodings.unicode.UnicodeCodeRange OTHERPUNCTUATION
fld public final static org.jcodings.unicode.UnicodeCodeRange OTHERSYMBOL
fld public final static org.jcodings.unicode.UnicodeCodeRange OTHERUPPERCASE
fld public final static org.jcodings.unicode.UnicodeCodeRange OUPPER
fld public final static org.jcodings.unicode.UnicodeCodeRange P
fld public final static org.jcodings.unicode.UnicodeCodeRange PAHAWHHMONG
fld public final static org.jcodings.unicode.UnicodeCodeRange PALM
fld public final static org.jcodings.unicode.UnicodeCodeRange PALMYRENE
fld public final static org.jcodings.unicode.UnicodeCodeRange PARAGRAPHSEPARATOR
fld public final static org.jcodings.unicode.UnicodeCodeRange PATSYN
fld public final static org.jcodings.unicode.UnicodeCodeRange PATTERNSYNTAX
fld public final static org.jcodings.unicode.UnicodeCodeRange PATTERNWHITESPACE
fld public final static org.jcodings.unicode.UnicodeCodeRange PATWS
fld public final static org.jcodings.unicode.UnicodeCodeRange PAUC
fld public final static org.jcodings.unicode.UnicodeCodeRange PAUCINHAU
fld public final static org.jcodings.unicode.UnicodeCodeRange PC
fld public final static org.jcodings.unicode.UnicodeCodeRange PCM
fld public final static org.jcodings.unicode.UnicodeCodeRange PD
fld public final static org.jcodings.unicode.UnicodeCodeRange PE
fld public final static org.jcodings.unicode.UnicodeCodeRange PERM
fld public final static org.jcodings.unicode.UnicodeCodeRange PF
fld public final static org.jcodings.unicode.UnicodeCodeRange PHAG
fld public final static org.jcodings.unicode.UnicodeCodeRange PHAGSPA
fld public final static org.jcodings.unicode.UnicodeCodeRange PHLI
fld public final static org.jcodings.unicode.UnicodeCodeRange PHLP
fld public final static org.jcodings.unicode.UnicodeCodeRange PHNX
fld public final static org.jcodings.unicode.UnicodeCodeRange PHOENICIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange PI
fld public final static org.jcodings.unicode.UnicodeCodeRange PLRD
fld public final static org.jcodings.unicode.UnicodeCodeRange PO
fld public final static org.jcodings.unicode.UnicodeCodeRange PREPENDEDCONCATENATIONMARK
fld public final static org.jcodings.unicode.UnicodeCodeRange PRINT
fld public final static org.jcodings.unicode.UnicodeCodeRange PRIVATEUSE
fld public final static org.jcodings.unicode.UnicodeCodeRange PRTI
fld public final static org.jcodings.unicode.UnicodeCodeRange PS
fld public final static org.jcodings.unicode.UnicodeCodeRange PSALTERPAHLAVI
fld public final static org.jcodings.unicode.UnicodeCodeRange PUNCT
fld public final static org.jcodings.unicode.UnicodeCodeRange PUNCTUATION
fld public final static org.jcodings.unicode.UnicodeCodeRange QAAC
fld public final static org.jcodings.unicode.UnicodeCodeRange QAAI
fld public final static org.jcodings.unicode.UnicodeCodeRange QMARK
fld public final static org.jcodings.unicode.UnicodeCodeRange QUOTATIONMARK
fld public final static org.jcodings.unicode.UnicodeCodeRange RADICAL
fld public final static org.jcodings.unicode.UnicodeCodeRange REGIONALINDICATOR
fld public final static org.jcodings.unicode.UnicodeCodeRange REJANG
fld public final static org.jcodings.unicode.UnicodeCodeRange RI
fld public final static org.jcodings.unicode.UnicodeCodeRange RJNG
fld public final static org.jcodings.unicode.UnicodeCodeRange ROHG
fld public final static org.jcodings.unicode.UnicodeCodeRange RUNIC
fld public final static org.jcodings.unicode.UnicodeCodeRange RUNR
fld public final static org.jcodings.unicode.UnicodeCodeRange S
fld public final static org.jcodings.unicode.UnicodeCodeRange SAMARITAN
fld public final static org.jcodings.unicode.UnicodeCodeRange SAMR
fld public final static org.jcodings.unicode.UnicodeCodeRange SARB
fld public final static org.jcodings.unicode.UnicodeCodeRange SAUR
fld public final static org.jcodings.unicode.UnicodeCodeRange SAURASHTRA
fld public final static org.jcodings.unicode.UnicodeCodeRange SC
fld public final static org.jcodings.unicode.UnicodeCodeRange SD
fld public final static org.jcodings.unicode.UnicodeCodeRange SENTENCETERMINAL
fld public final static org.jcodings.unicode.UnicodeCodeRange SEPARATOR
fld public final static org.jcodings.unicode.UnicodeCodeRange SGNW
fld public final static org.jcodings.unicode.UnicodeCodeRange SHARADA
fld public final static org.jcodings.unicode.UnicodeCodeRange SHAVIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange SHAW
fld public final static org.jcodings.unicode.UnicodeCodeRange SHRD
fld public final static org.jcodings.unicode.UnicodeCodeRange SIDD
fld public final static org.jcodings.unicode.UnicodeCodeRange SIDDHAM
fld public final static org.jcodings.unicode.UnicodeCodeRange SIGNWRITING
fld public final static org.jcodings.unicode.UnicodeCodeRange SIND
fld public final static org.jcodings.unicode.UnicodeCodeRange SINH
fld public final static org.jcodings.unicode.UnicodeCodeRange SINHALA
fld public final static org.jcodings.unicode.UnicodeCodeRange SK
fld public final static org.jcodings.unicode.UnicodeCodeRange SM
fld public final static org.jcodings.unicode.UnicodeCodeRange SO
fld public final static org.jcodings.unicode.UnicodeCodeRange SOFTDOTTED
fld public final static org.jcodings.unicode.UnicodeCodeRange SOGD
fld public final static org.jcodings.unicode.UnicodeCodeRange SOGDIAN
fld public final static org.jcodings.unicode.UnicodeCodeRange SOGO
fld public final static org.jcodings.unicode.UnicodeCodeRange SORA
fld public final static org.jcodings.unicode.UnicodeCodeRange SORASOMPENG
fld public final static org.jcodings.unicode.UnicodeCodeRange SOYO
fld public final static org.jcodings.unicode.UnicodeCodeRange SOYOMBO
fld public final static org.jcodings.unicode.UnicodeCodeRange SPACE
fld public final static org.jcodings.unicode.UnicodeCodeRange SPACESEPARATOR
fld public final static org.jcodings.unicode.UnicodeCodeRange SPACINGMARK
fld public final static org.jcodings.unicode.UnicodeCodeRange STERM
fld public final static org.jcodings.unicode.UnicodeCodeRange SUND
fld public final static org.jcodings.unicode.UnicodeCodeRange SUNDANESE
fld public final static org.jcodings.unicode.UnicodeCodeRange SURROGATE
fld public final static org.jcodings.unicode.UnicodeCodeRange SYLO
fld public final static org.jcodings.unicode.UnicodeCodeRange SYLOTINAGRI
fld public final static org.jcodings.unicode.UnicodeCodeRange SYMBOL
fld public final static org.jcodings.unicode.UnicodeCodeRange SYRC
fld public final static org.jcodings.unicode.UnicodeCodeRange SYRIAC
fld public final static org.jcodings.unicode.UnicodeCodeRange TAGALOG
fld public final static org.jcodings.unicode.UnicodeCodeRange TAGB
fld public final static org.jcodings.unicode.UnicodeCodeRange TAGBANWA
fld public final static org.jcodings.unicode.UnicodeCodeRange TAILE
fld public final static org.jcodings.unicode.UnicodeCodeRange TAITHAM
fld public final static org.jcodings.unicode.UnicodeCodeRange TAIVIET
fld public final static org.jcodings.unicode.UnicodeCodeRange TAKR
fld public final static org.jcodings.unicode.UnicodeCodeRange TAKRI
fld public final static org.jcodings.unicode.UnicodeCodeRange TALE
fld public final static org.jcodings.unicode.UnicodeCodeRange TALU
fld public final static org.jcodings.unicode.UnicodeCodeRange TAMIL
fld public final static org.jcodings.unicode.UnicodeCodeRange TAML
fld public final static org.jcodings.unicode.UnicodeCodeRange TANG
fld public final static org.jcodings.unicode.UnicodeCodeRange TANGUT
fld public final static org.jcodings.unicode.UnicodeCodeRange TAVT
fld public final static org.jcodings.unicode.UnicodeCodeRange TELU
fld public final static org.jcodings.unicode.UnicodeCodeRange TELUGU
fld public final static org.jcodings.unicode.UnicodeCodeRange TERM
fld public final static org.jcodings.unicode.UnicodeCodeRange TERMINALPUNCTUATION
fld public final static org.jcodings.unicode.UnicodeCodeRange TFNG
fld public final static org.jcodings.unicode.UnicodeCodeRange TGLG
fld public final static org.jcodings.unicode.UnicodeCodeRange THAA
fld public final static org.jcodings.unicode.UnicodeCodeRange THAANA
fld public final static org.jcodings.unicode.UnicodeCodeRange THAI
fld public final static org.jcodings.unicode.UnicodeCodeRange TIBETAN
fld public final static org.jcodings.unicode.UnicodeCodeRange TIBT
fld public final static org.jcodings.unicode.UnicodeCodeRange TIFINAGH
fld public final static org.jcodings.unicode.UnicodeCodeRange TIRH
fld public final static org.jcodings.unicode.UnicodeCodeRange TIRHUTA
fld public final static org.jcodings.unicode.UnicodeCodeRange TITLECASELETTER
fld public final static org.jcodings.unicode.UnicodeCodeRange UGAR
fld public final static org.jcodings.unicode.UnicodeCodeRange UGARITIC
fld public final static org.jcodings.unicode.UnicodeCodeRange UIDEO
fld public final static org.jcodings.unicode.UnicodeCodeRange UNASSIGNED
fld public final static org.jcodings.unicode.UnicodeCodeRange UNIFIEDIDEOGRAPH
fld public final static org.jcodings.unicode.UnicodeCodeRange UNKNOWN
fld public final static org.jcodings.unicode.UnicodeCodeRange UPPER
fld public final static org.jcodings.unicode.UnicodeCodeRange UPPERCASE
fld public final static org.jcodings.unicode.UnicodeCodeRange UPPERCASELETTER
fld public final static org.jcodings.unicode.UnicodeCodeRange VAI
fld public final static org.jcodings.unicode.UnicodeCodeRange VAII
fld public final static org.jcodings.unicode.UnicodeCodeRange VARIATIONSELECTOR
fld public final static org.jcodings.unicode.UnicodeCodeRange VS
fld public final static org.jcodings.unicode.UnicodeCodeRange WANCHO
fld public final static org.jcodings.unicode.UnicodeCodeRange WARA
fld public final static org.jcodings.unicode.UnicodeCodeRange WARANGCITI
fld public final static org.jcodings.unicode.UnicodeCodeRange WCHO
fld public final static org.jcodings.unicode.UnicodeCodeRange WHITESPACE
fld public final static org.jcodings.unicode.UnicodeCodeRange WORD
fld public final static org.jcodings.unicode.UnicodeCodeRange WSPACE
fld public final static org.jcodings.unicode.UnicodeCodeRange XDIGIT
fld public final static org.jcodings.unicode.UnicodeCodeRange XIDC
fld public final static org.jcodings.unicode.UnicodeCodeRange XIDCONTINUE
fld public final static org.jcodings.unicode.UnicodeCodeRange XIDS
fld public final static org.jcodings.unicode.UnicodeCodeRange XIDSTART
fld public final static org.jcodings.unicode.UnicodeCodeRange XPEO
fld public final static org.jcodings.unicode.UnicodeCodeRange XPOSIXPUNCT
fld public final static org.jcodings.unicode.UnicodeCodeRange XSUX
fld public final static org.jcodings.unicode.UnicodeCodeRange YEZI
fld public final static org.jcodings.unicode.UnicodeCodeRange YEZIDI
fld public final static org.jcodings.unicode.UnicodeCodeRange YI
fld public final static org.jcodings.unicode.UnicodeCodeRange YIII
fld public final static org.jcodings.unicode.UnicodeCodeRange Z
fld public final static org.jcodings.unicode.UnicodeCodeRange ZANABAZARSQUARE
fld public final static org.jcodings.unicode.UnicodeCodeRange ZANB
fld public final static org.jcodings.unicode.UnicodeCodeRange ZINH
fld public final static org.jcodings.unicode.UnicodeCodeRange ZL
fld public final static org.jcodings.unicode.UnicodeCodeRange ZP
fld public final static org.jcodings.unicode.UnicodeCodeRange ZS
fld public final static org.jcodings.unicode.UnicodeCodeRange ZYYY
fld public final static org.jcodings.unicode.UnicodeCodeRange ZZZZ
meth public boolean contains(int)
meth public int getCType()
meth public static org.jcodings.unicode.UnicodeCodeRange valueOf(java.lang.String)
meth public static org.jcodings.unicode.UnicodeCodeRange[] values()
supr java.lang.Enum<org.jcodings.unicode.UnicodeCodeRange>
hfds CodeRangeTable,MAX_WORD_LENGTH,name,range,table

CLSS public abstract org.jcodings.unicode.UnicodeEncoding
cons protected init(java.lang.String,int,int,int[])
cons protected init(java.lang.String,int,int,int[],int[][])
meth protected final int[] ctypeCodeRange(int)
meth public boolean isCodeCType(int,int)
meth public final int caseMap(org.jcodings.IntHolder,byte[],org.jcodings.IntHolder,int,byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int propertyNameToCType(byte[],int,int)
meth public java.lang.String getCharsetName()
meth public org.jcodings.CaseFoldCodeItem[] caseFoldCodesByString(int,byte[],int,int)
meth public static boolean isInCodeRange(org.jcodings.unicode.UnicodeCodeRange,int)
meth public void applyAllCaseFold(int,org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
supr org.jcodings.MultiByteEncoding
hfds CASE_MAPPING_SLACK,DOTLESS_i,DOT_ABOVE,I_WITH_DOT_ABOVE,PROPERTY_NAME_MAX_SIZE,UNICODE_ISO_8859_1_CTypeTable
hcls CTypeName,CaseFold,CaseMappingSpecials,CaseUnfold11,CaseUnfold12,CaseUnfold13,CodeList

CLSS public org.jcodings.util.ArrayReader
cons public init()
meth public static byte[] readByteArray(java.lang.String)
meth public static int[] readIntArray(java.lang.String)
meth public static int[][] readNestedIntArray(java.lang.String)
meth public static java.io.DataInputStream openStream(java.lang.String)
supr java.lang.Object

CLSS public final org.jcodings.util.BytesHash<%0 extends java.lang.Object>
cons public init()
cons public init(int)
innr public final static BytesHashEntry
meth protected void init()
meth public static int hashCode(byte[],int,int)
meth public void putDirect(byte[],int,int,{org.jcodings.util.BytesHash%0})
meth public void putDirect(byte[],{org.jcodings.util.BytesHash%0})
meth public {org.jcodings.util.BytesHash%0} delete(byte[])
meth public {org.jcodings.util.BytesHash%0} delete(byte[],int,int)
meth public {org.jcodings.util.BytesHash%0} get(byte[])
meth public {org.jcodings.util.BytesHash%0} get(byte[],int,int)
meth public {org.jcodings.util.BytesHash%0} put(byte[],int,int,{org.jcodings.util.BytesHash%0})
meth public {org.jcodings.util.BytesHash%0} put(byte[],{org.jcodings.util.BytesHash%0})
supr org.jcodings.util.Hash<{org.jcodings.util.BytesHash%0}>

CLSS public final static org.jcodings.util.BytesHash$BytesHashEntry<%0 extends java.lang.Object>
 outer org.jcodings.util.BytesHash
cons public init()
cons public init(int,org.jcodings.util.Hash$HashEntry<{org.jcodings.util.BytesHash$BytesHashEntry%0}>,{org.jcodings.util.BytesHash$BytesHashEntry%0},byte[],int,int,org.jcodings.util.Hash$HashEntry<{org.jcodings.util.BytesHash$BytesHashEntry%0}>)
fld public final byte[] bytes
fld public final int end
fld public final int p
meth public boolean equals(byte[],int,int)
supr org.jcodings.util.Hash$HashEntry<{org.jcodings.util.BytesHash$BytesHashEntry%0}>

CLSS public final org.jcodings.util.CaseInsensitiveBytesHash<%0 extends java.lang.Object>
cons public init()
cons public init(int)
innr public CaseInsensitiveBytesHashEntryIterator
innr public final static CaseInsensitiveBytesHashEntry
meth protected void init()
meth public org.jcodings.util.CaseInsensitiveBytesHash$CaseInsensitiveBytesHashEntryIterator entryIterator()
meth public static boolean caseInsensitiveEquals(byte[],byte[])
meth public static boolean caseInsensitiveEquals(byte[],int,int,byte[],int,int)
meth public static int hashCode(byte[],int,int)
meth public void putDirect(byte[],int,int,{org.jcodings.util.CaseInsensitiveBytesHash%0})
meth public void putDirect(byte[],{org.jcodings.util.CaseInsensitiveBytesHash%0})
meth public {org.jcodings.util.CaseInsensitiveBytesHash%0} delete(byte[])
meth public {org.jcodings.util.CaseInsensitiveBytesHash%0} delete(byte[],int,int)
meth public {org.jcodings.util.CaseInsensitiveBytesHash%0} get(byte[])
meth public {org.jcodings.util.CaseInsensitiveBytesHash%0} get(byte[],int,int)
meth public {org.jcodings.util.CaseInsensitiveBytesHash%0} put(byte[],int,int,{org.jcodings.util.CaseInsensitiveBytesHash%0})
meth public {org.jcodings.util.CaseInsensitiveBytesHash%0} put(byte[],{org.jcodings.util.CaseInsensitiveBytesHash%0})
supr org.jcodings.util.Hash<{org.jcodings.util.CaseInsensitiveBytesHash%0}>

CLSS public final static org.jcodings.util.CaseInsensitiveBytesHash$CaseInsensitiveBytesHashEntry<%0 extends java.lang.Object>
 outer org.jcodings.util.CaseInsensitiveBytesHash
cons public init()
cons public init(int,org.jcodings.util.Hash$HashEntry<{org.jcodings.util.CaseInsensitiveBytesHash$CaseInsensitiveBytesHashEntry%0}>,{org.jcodings.util.CaseInsensitiveBytesHash$CaseInsensitiveBytesHashEntry%0},byte[],int,int,org.jcodings.util.Hash$HashEntry<{org.jcodings.util.CaseInsensitiveBytesHash$CaseInsensitiveBytesHashEntry%0}>)
fld public final byte[] bytes
fld public final int end
fld public final int p
meth public boolean equals(byte[],int,int)
supr org.jcodings.util.Hash$HashEntry<{org.jcodings.util.CaseInsensitiveBytesHash$CaseInsensitiveBytesHashEntry%0}>

CLSS public org.jcodings.util.CaseInsensitiveBytesHash$CaseInsensitiveBytesHashEntryIterator
 outer org.jcodings.util.CaseInsensitiveBytesHash
cons public init(org.jcodings.util.CaseInsensitiveBytesHash)
meth public org.jcodings.util.CaseInsensitiveBytesHash$CaseInsensitiveBytesHashEntry<{org.jcodings.util.CaseInsensitiveBytesHash%0}> next()
supr org.jcodings.util.Hash$HashEntryIterator

CLSS public abstract org.jcodings.util.Hash<%0 extends java.lang.Object>
cons public init()
cons public init(int)
fld protected int size
fld protected org.jcodings.util.Hash$HashEntry<{org.jcodings.util.Hash%0}> head
fld protected org.jcodings.util.Hash$HashEntry<{org.jcodings.util.Hash%0}>[] table
innr public HashEntryIterator
innr public HashIterator
innr public static HashEntry
intf java.lang.Iterable<{org.jcodings.util.Hash%0}>
meth protected abstract void init()
meth protected final void checkResize()
meth protected final void resize(int)
meth protected static int bucketIndex(int,int)
meth protected static int hashValue(int)
meth public final int size()
meth public java.util.Iterator<{org.jcodings.util.Hash%0}> iterator()
meth public org.jcodings.util.Hash$HashEntryIterator entryIterator()
supr java.lang.Object
hfds HASH_SIGN_BIT_MASK,INITIAL_CAPACITY,MAXIMUM_CAPACITY,MIN_CAPA,PRIMES

CLSS public static org.jcodings.util.Hash$HashEntry<%0 extends java.lang.Object>
 outer org.jcodings.util.Hash
fld protected org.jcodings.util.Hash$HashEntry<{org.jcodings.util.Hash$HashEntry%0}> after
fld protected org.jcodings.util.Hash$HashEntry<{org.jcodings.util.Hash$HashEntry%0}> before
fld protected org.jcodings.util.Hash$HashEntry<{org.jcodings.util.Hash$HashEntry%0}> next
fld public {org.jcodings.util.Hash$HashEntry%0} value
meth public int getHash()
supr java.lang.Object
hfds hash

CLSS public org.jcodings.util.Hash$HashEntryIterator
 outer org.jcodings.util.Hash
cons public init(org.jcodings.util.Hash)
intf java.lang.Iterable<org.jcodings.util.Hash$HashEntry<{org.jcodings.util.Hash%0}>>
intf java.util.Iterator<org.jcodings.util.Hash$HashEntry<{org.jcodings.util.Hash%0}>>
meth public boolean hasNext()
meth public java.util.Iterator<org.jcodings.util.Hash$HashEntry<{org.jcodings.util.Hash%0}>> iterator()
meth public org.jcodings.util.Hash$HashEntry<{org.jcodings.util.Hash%0}> next()
meth public void remove()
supr java.lang.Object
hfds next

CLSS public org.jcodings.util.Hash$HashIterator
 outer org.jcodings.util.Hash
cons public init(org.jcodings.util.Hash)
intf java.util.Iterator<{org.jcodings.util.Hash%0}>
meth public boolean hasNext()
meth public void remove()
meth public {org.jcodings.util.Hash%0} next()
supr java.lang.Object
hfds next

CLSS public final org.jcodings.util.IntArrayHash<%0 extends java.lang.Object>
cons public init()
cons public init(int)
innr public final static IntArrayHashEntry
meth protected void init()
meth public !varargs {org.jcodings.util.IntArrayHash%0} delete(int[])
meth public !varargs {org.jcodings.util.IntArrayHash%0} get(int[])
meth public void putDirect(int[],{org.jcodings.util.IntArrayHash%0})
meth public {org.jcodings.util.IntArrayHash%0} put(int[],{org.jcodings.util.IntArrayHash%0})
supr org.jcodings.util.Hash<{org.jcodings.util.IntArrayHash%0}>

CLSS public final static org.jcodings.util.IntArrayHash$IntArrayHashEntry<%0 extends java.lang.Object>
 outer org.jcodings.util.IntArrayHash
cons public init()
cons public init(int,org.jcodings.util.Hash$HashEntry<{org.jcodings.util.IntArrayHash$IntArrayHashEntry%0}>,{org.jcodings.util.IntArrayHash$IntArrayHashEntry%0},int[],org.jcodings.util.Hash$HashEntry<{org.jcodings.util.IntArrayHash$IntArrayHashEntry%0}>)
fld public final int[] key
meth public boolean equals(int[])
supr org.jcodings.util.Hash$HashEntry<{org.jcodings.util.IntArrayHash$IntArrayHashEntry%0}>

CLSS public org.jcodings.util.IntHash<%0 extends java.lang.Object>
cons public init()
cons public init(int)
innr public final static IntHashEntry
meth protected void init()
meth public void putDirect(int,{org.jcodings.util.IntHash%0})
meth public {org.jcodings.util.IntHash%0} delete(int)
meth public {org.jcodings.util.IntHash%0} get(int)
meth public {org.jcodings.util.IntHash%0} put(int,{org.jcodings.util.IntHash%0})
supr org.jcodings.util.Hash<{org.jcodings.util.IntHash%0}>

CLSS public final static org.jcodings.util.IntHash$IntHashEntry<%0 extends java.lang.Object>
 outer org.jcodings.util.IntHash
cons public init()
cons public init(int,org.jcodings.util.Hash$HashEntry<{org.jcodings.util.IntHash$IntHashEntry%0}>,{org.jcodings.util.IntHash$IntHashEntry%0},org.jcodings.util.Hash$HashEntry<{org.jcodings.util.IntHash$IntHashEntry%0}>)
supr org.jcodings.util.Hash$HashEntry<{org.jcodings.util.IntHash$IntHashEntry%0}>

CLSS public org.jcodings.util.Macros
cons public init()
fld public final static int MBCLEN_INVALID = -1
meth public static boolean MBCLEN_CHARFOUND_P(int)
meth public static boolean MBCLEN_INVALID_P(int)
meth public static boolean MBCLEN_NEEDMORE_P(int)
meth public static boolean UNICODE_VALID_CODEPOINT_P(int)
meth public static boolean UTF16_IS_SURROGATE(int)
meth public static boolean UTF16_IS_SURROGATE_FIRST(int)
meth public static boolean UTF16_IS_SURROGATE_SECOND(int)
meth public static int CONSTRUCT_MBCLEN_CHARFOUND(int)
meth public static int CONSTRUCT_MBCLEN_INVALID()
meth public static int CONSTRUCT_MBCLEN_NEEDMORE(int)
meth public static int MBCLEN_CHARFOUND_LEN(int)
meth public static int MBCLEN_NEEDMORE_LEN(int)
supr java.lang.Object

CLSS public final org.jcodings.util.ObjHash<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
innr public final static ObjHashEntry
meth protected void init()
meth public void putDirect({org.jcodings.util.ObjHash%0},{org.jcodings.util.ObjHash%1})
meth public {org.jcodings.util.ObjHash%1} delete({org.jcodings.util.ObjHash%0})
meth public {org.jcodings.util.ObjHash%1} get({org.jcodings.util.ObjHash%0})
meth public {org.jcodings.util.ObjHash%1} put({org.jcodings.util.ObjHash%0},{org.jcodings.util.ObjHash%1})
supr org.jcodings.util.Hash<{org.jcodings.util.ObjHash%1}>

CLSS public final static org.jcodings.util.ObjHash$ObjHashEntry<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.jcodings.util.ObjHash
cons public init()
cons public init(int,org.jcodings.util.Hash$HashEntry<{org.jcodings.util.ObjHash$ObjHashEntry%1}>,{org.jcodings.util.ObjHash$ObjHashEntry%1},{org.jcodings.util.ObjHash$ObjHashEntry%0},org.jcodings.util.Hash$HashEntry<{org.jcodings.util.ObjHash$ObjHashEntry%1}>)
fld public final {org.jcodings.util.ObjHash$ObjHashEntry%0} key
meth public boolean equals(java.lang.Object)
supr org.jcodings.util.Hash$HashEntry<{org.jcodings.util.ObjHash$ObjHashEntry%1}>

