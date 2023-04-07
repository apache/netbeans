#Signature file v4.1
#Version 0.8

CLSS public abstract interface java.lang.Cloneable

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

CLSS public abstract interface org.jcodings.ApplyAllCaseFoldFunction
meth public abstract void apply(int,int[],int,java.lang.Object)

CLSS public abstract org.jcodings.CanBeTrailTableEncoding
cons protected init(java.lang.String,int,int,int[],int[][],short[],boolean[])
fld protected final boolean[] CanBeTrailTable
meth public boolean isReverseMatchAllowed(byte[],int,int)
meth public int leftAdjustCharHead(byte[],int,int,int)
supr org.jcodings.MultiByteEncoding

CLSS public org.jcodings.CaseFoldCodeItem
cons public init(int,int,int[])
fld public final int byteLen
fld public final int codeLen
fld public final int[] code
supr java.lang.Object
hfds ENC_MAX_COMP_CASE_FOLD_CODE_LEN

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

CLSS public org.jcodings.CodeRange
cons public init()
meth public static boolean isInCodeRange(int[],int)
meth public static boolean isInCodeRange(int[],int,int)
supr java.lang.Object

CLSS public abstract interface org.jcodings.Config
fld public final static boolean USE_CRNL_AS_LINE_TERMINATOR = false
fld public final static boolean USE_UNICODE_ALL_LINE_TERMINATORS = false
fld public final static boolean USE_UNICODE_CASE_FOLD_TURKISH_AZERI = false
fld public final static boolean USE_UNICODE_PROPERTIES = true
fld public final static boolean VANILLA = false
fld public final static int ENC_CASE_FOLD_DEFAULT = 1073741824
fld public final static int ENC_CASE_FOLD_MIN = 1073741824
fld public final static int ENC_CASE_FOLD_TURKISH_AZERI = 1048576
fld public final static int ENC_CODE_TO_MBC_MAXLEN = 7
fld public final static int ENC_GET_CASE_FOLD_CODES_MAX_NUM = 13
fld public final static int ENC_MAX_COMP_CASE_FOLD_CODE_LEN = 3
fld public final static int ENC_MBC_CASE_FOLD_MAXLEN = 18
fld public final static int INTERNAL_ENC_CASE_FOLD_MULTI_CHAR = 1073741824

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
meth public final int mbcodeStartPosition()
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
supr java.lang.Object
hfds charset,count,hashCode,index,isAsciiCompatible,isDummy,isFixedWidth,isSingleByte,name

CLSS public org.jcodings.EncodingDB
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
fld protected final static org.jcodings.CaseFoldCodeItem[] EMPTY_FOLD_CODES
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
meth public int length(byte)
meth public int mbcCaseFold(int,byte[],org.jcodings.IntHolder,int,byte[])
meth public int propertyNameToCType(byte[],int,int)
meth public int strCodeAt(byte[],int,int,int)
meth public int strLength(byte[],int,int)
meth public org.jcodings.CaseFoldCodeItem[] caseFoldCodesByString(int,byte[],int,int)
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
cons protected init(java.lang.String,short[],byte[],int)
fld protected final byte[] LowerCaseTable
fld protected final static org.jcodings.CaseFoldCodeItem[] EMPTY_FOLD_CODES
fld protected int codeSize
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

