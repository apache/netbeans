#Signature file v4.1
#Version 1.30

CLSS public final com.dd.plist.ASCIIPropertyListParser
fld public final static char ARRAY_BEGIN_TOKEN = '('
fld public final static char ARRAY_END_TOKEN = ')'
fld public final static char ARRAY_ITEM_DELIMITER_TOKEN = ','
fld public final static char COMMENT_BEGIN_TOKEN = '/'
fld public final static char DATA_BEGIN_TOKEN = '<'
fld public final static char DATA_END_TOKEN = '>'
fld public final static char DATA_GSBOOL_BEGIN_TOKEN = 'B'
fld public final static char DATA_GSBOOL_FALSE_TOKEN = 'N'
fld public final static char DATA_GSBOOL_TRUE_TOKEN = 'Y'
fld public final static char DATA_GSDATE_BEGIN_TOKEN = 'D'
fld public final static char DATA_GSINT_BEGIN_TOKEN = 'I'
fld public final static char DATA_GSOBJECT_BEGIN_TOKEN = '*'
fld public final static char DATA_GSREAL_BEGIN_TOKEN = 'R'
fld public final static char DATE_APPLE_DATE_TIME_DELIMITER = 'T'
fld public final static char DATE_APPLE_END_TOKEN = 'Z'
fld public final static char DATE_DATE_FIELD_DELIMITER = '-'
fld public final static char DATE_GS_DATE_TIME_DELIMITER = ' '
fld public final static char DATE_TIME_FIELD_DELIMITER = ':'
fld public final static char DICTIONARY_ASSIGN_TOKEN = '='
fld public final static char DICTIONARY_BEGIN_TOKEN = '{'
fld public final static char DICTIONARY_END_TOKEN = '}'
fld public final static char DICTIONARY_ITEM_DELIMITER_TOKEN = ';'
fld public final static char MULTILINE_COMMENT_END_TOKEN = '/'
fld public final static char MULTILINE_COMMENT_SECOND_TOKEN = '*'
fld public final static char QUOTEDSTRING_BEGIN_TOKEN = '\u0022'
fld public final static char QUOTEDSTRING_END_TOKEN = '\u0022'
fld public final static char QUOTEDSTRING_ESCAPE_TOKEN = '\u005c'
fld public final static char SINGLELINE_COMMENT_SECOND_TOKEN = '/'
fld public final static char WHITESPACE_CARRIAGE_RETURN = '\r'
fld public final static char WHITESPACE_NEWLINE = '\n'
fld public final static char WHITESPACE_SPACE = ' '
fld public final static char WHITESPACE_TAB = '\u0009'
meth public com.dd.plist.NSObject parse() throws java.text.ParseException
meth public static com.dd.plist.NSObject parse(byte[]) throws java.text.ParseException
meth public static com.dd.plist.NSObject parse(byte[],java.lang.String) throws java.io.UnsupportedEncodingException,java.text.ParseException
meth public static com.dd.plist.NSObject parse(java.io.File) throws java.io.IOException,java.text.ParseException
meth public static com.dd.plist.NSObject parse(java.io.File,java.lang.String) throws java.io.IOException,java.text.ParseException
meth public static com.dd.plist.NSObject parse(java.io.InputStream) throws java.io.IOException,java.text.ParseException
meth public static com.dd.plist.NSObject parse(java.io.InputStream,java.lang.String) throws java.io.IOException,java.text.ParseException
supr java.lang.Object
hfds data,index

CLSS public com.dd.plist.Base64
fld public final static int DECODE = 0
fld public final static int DONT_GUNZIP = 4
fld public final static int DO_BREAK_LINES = 8
fld public final static int ENCODE = 1
fld public final static int GZIP = 2
fld public final static int NO_OPTIONS = 0
fld public final static int ORDERED = 32
fld public final static int URL_SAFE = 16
innr public static B64InputStream
innr public static B64OutputStream
meth public static byte[] decode(byte[]) throws java.io.IOException
meth public static byte[] decode(byte[],int,int,int) throws java.io.IOException
meth public static byte[] decode(java.lang.String) throws java.io.IOException
meth public static byte[] decode(java.lang.String,int) throws java.io.IOException
meth public static byte[] decodeFromFile(java.lang.String) throws java.io.IOException
meth public static byte[] encodeBytesToBytes(byte[])
meth public static byte[] encodeBytesToBytes(byte[],int,int,int) throws java.io.IOException
meth public static java.lang.Object decodeToObject(java.lang.String) throws java.io.IOException,java.lang.ClassNotFoundException
meth public static java.lang.Object decodeToObject(java.lang.String,int,java.lang.ClassLoader) throws java.io.IOException,java.lang.ClassNotFoundException
meth public static java.lang.String encodeBytes(byte[])
meth public static java.lang.String encodeBytes(byte[],int) throws java.io.IOException
meth public static java.lang.String encodeBytes(byte[],int,int)
meth public static java.lang.String encodeBytes(byte[],int,int,int) throws java.io.IOException
meth public static java.lang.String encodeFromFile(java.lang.String) throws java.io.IOException
meth public static java.lang.String encodeObject(java.io.Serializable) throws java.io.IOException
meth public static java.lang.String encodeObject(java.io.Serializable,int) throws java.io.IOException
meth public static void decodeFileToFile(java.lang.String,java.lang.String) throws java.io.IOException
meth public static void decodeToFile(java.lang.String,java.lang.String) throws java.io.IOException
meth public static void encode(java.nio.ByteBuffer,java.nio.ByteBuffer)
meth public static void encode(java.nio.ByteBuffer,java.nio.CharBuffer)
meth public static void encodeFileToFile(java.lang.String,java.lang.String) throws java.io.IOException
meth public static void encodeToFile(byte[],java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds EQUALS_SIGN,EQUALS_SIGN_ENC,MAX_LINE_LENGTH,NEW_LINE,PREFERRED_ENCODING,WHITE_SPACE_ENC,_ORDERED_ALPHABET,_ORDERED_DECODABET,_STANDARD_ALPHABET,_STANDARD_DECODABET,_URL_SAFE_ALPHABET,_URL_SAFE_DECODABET

CLSS public static com.dd.plist.Base64$B64InputStream
 outer com.dd.plist.Base64
cons public init(java.io.InputStream)
cons public init(java.io.InputStream,int)
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
supr java.io.FilterInputStream
hfds breakLines,buffer,bufferLength,decodabet,encode,lineLength,numSigBytes,options,position

CLSS public static com.dd.plist.Base64$B64OutputStream
 outer com.dd.plist.Base64
cons public init(java.io.OutputStream)
cons public init(java.io.OutputStream,int)
meth public void close() throws java.io.IOException
meth public void flushBase64() throws java.io.IOException
meth public void resumeEncoding()
meth public void suspendEncoding() throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.FilterOutputStream
hfds b4,breakLines,buffer,bufferLength,decodabet,encode,lineLength,options,position,suspendEncoding

CLSS public final com.dd.plist.BinaryPropertyListParser
meth public static byte[] copyOfRange(byte[],int,int)
meth public static com.dd.plist.NSObject parse(byte[]) throws com.dd.plist.PropertyListFormatException,java.io.UnsupportedEncodingException
meth public static com.dd.plist.NSObject parse(java.io.File) throws com.dd.plist.PropertyListFormatException,java.io.IOException
meth public static com.dd.plist.NSObject parse(java.io.InputStream) throws com.dd.plist.PropertyListFormatException,java.io.IOException
meth public static double parseDouble(byte[])
meth public static double parseDouble(byte[],int,int)
meth public static long parseLong(byte[])
meth public static long parseLong(byte[],int,int)
meth public static long parseUnsignedInt(byte[])
meth public static long parseUnsignedInt(byte[],int,int)
supr java.lang.Object
hfds bytes,majorVersion,minorVersion,objectRefSize,offsetTable

CLSS public final com.dd.plist.BinaryPropertyListWriter
meth public static byte[] writeToArray(com.dd.plist.NSObject) throws java.io.IOException
meth public static void write(java.io.File,com.dd.plist.NSObject) throws java.io.IOException
meth public static void write(java.io.OutputStream,com.dd.plist.NSObject) throws java.io.IOException
supr java.lang.Object
hfds VERSION_00,VERSION_10,VERSION_15,VERSION_20,count,idMap,idSizeInBytes,out,version

CLSS public com.dd.plist.NSArray
cons public !varargs init(com.dd.plist.NSObject[])
cons public init(int)
meth protected void toASCII(java.lang.StringBuilder,int)
meth protected void toASCIIGnuStep(java.lang.StringBuilder,int)
meth public !varargs com.dd.plist.NSObject[] objectsAtIndexes(int[])
meth public boolean containsObject(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public com.dd.plist.NSArray clone()
meth public com.dd.plist.NSObject lastObject()
meth public com.dd.plist.NSObject objectAtIndex(int)
meth public com.dd.plist.NSObject[] getArray()
meth public int count()
meth public int hashCode()
meth public int indexOfIdenticalObject(java.lang.Object)
meth public int indexOfObject(java.lang.Object)
meth public java.lang.String toASCIIPropertyList()
meth public java.lang.String toGnuStepASCIIPropertyList()
meth public void remove(int)
meth public void setValue(int,java.lang.Object)
supr com.dd.plist.NSObject
hfds array

CLSS public com.dd.plist.NSData
cons public init(byte[])
cons public init(java.io.File) throws java.io.IOException
cons public init(java.lang.String) throws java.io.IOException
meth protected void toASCII(java.lang.StringBuilder,int)
meth protected void toASCIIGnuStep(java.lang.StringBuilder,int)
meth public boolean equals(java.lang.Object)
meth public byte[] bytes()
meth public com.dd.plist.NSData clone()
meth public int hashCode()
meth public int length()
meth public java.lang.String getBase64EncodedData()
meth public void getBytes(java.nio.ByteBuffer,int)
meth public void getBytes(java.nio.ByteBuffer,int,int)
supr com.dd.plist.NSObject
hfds bytes

CLSS public com.dd.plist.NSDate
cons public init(byte[])
cons public init(byte[],int,int)
cons public init(java.lang.String) throws java.text.ParseException
cons public init(java.util.Date)
meth protected void toASCII(java.lang.StringBuilder,int)
meth protected void toASCIIGnuStep(java.lang.StringBuilder,int)
meth public boolean equals(java.lang.Object)
meth public com.dd.plist.NSDate clone()
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.Date getDate()
meth public void toBinary(com.dd.plist.BinaryPropertyListWriter) throws java.io.IOException
supr com.dd.plist.NSObject
hfds EPOCH,date,sdfDefault,sdfGnuStep

CLSS public com.dd.plist.NSDictionary
cons public init()
intf java.util.Map<java.lang.String,com.dd.plist.NSObject>
meth protected void toASCII(java.lang.StringBuilder,int)
meth protected void toASCIIGnuStep(java.lang.StringBuilder,int)
meth public boolean containsKey(java.lang.Object)
meth public boolean containsKey(java.lang.String)
meth public boolean containsValue(boolean)
meth public boolean containsValue(byte[])
meth public boolean containsValue(com.dd.plist.NSObject)
meth public boolean containsValue(double)
meth public boolean containsValue(java.lang.Object)
meth public boolean containsValue(java.lang.String)
meth public boolean containsValue(java.util.Date)
meth public boolean containsValue(long)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public com.dd.plist.NSDictionary clone()
meth public com.dd.plist.NSObject get(java.lang.Object)
meth public com.dd.plist.NSObject objectForKey(java.lang.String)
meth public com.dd.plist.NSObject put(java.lang.String,com.dd.plist.NSObject)
meth public com.dd.plist.NSObject put(java.lang.String,java.lang.Object)
meth public com.dd.plist.NSObject remove(java.lang.Object)
meth public com.dd.plist.NSObject remove(java.lang.String)
meth public int count()
meth public int hashCode()
meth public int size()
meth public java.lang.String toASCIIPropertyList()
meth public java.lang.String toGnuStepASCIIPropertyList()
meth public java.lang.String[] allKeys()
meth public java.util.Collection<com.dd.plist.NSObject> values()
meth public java.util.HashMap<java.lang.String,com.dd.plist.NSObject> getHashMap()
meth public java.util.Set<java.lang.String> keySet()
meth public java.util.Set<java.util.Map$Entry<java.lang.String,com.dd.plist.NSObject>> entrySet()
meth public void clear()
meth public void putAll(java.util.Map<? extends java.lang.String,? extends com.dd.plist.NSObject>)
supr com.dd.plist.NSObject
hfds dict

CLSS public com.dd.plist.NSNumber
cons public init(boolean)
cons public init(byte[],int)
cons public init(byte[],int,int,int)
cons public init(double)
cons public init(int)
cons public init(java.lang.String)
cons public init(long)
fld public final static int BOOLEAN = 2
fld public final static int INTEGER = 0
fld public final static int REAL = 1
intf java.lang.Comparable<java.lang.Object>
meth protected void toASCII(java.lang.StringBuilder,int)
meth protected void toASCIIGnuStep(java.lang.StringBuilder,int)
meth public boolean boolValue()
meth public boolean equals(java.lang.Object)
meth public boolean isBoolean()
meth public boolean isInteger()
meth public boolean isReal()
meth public com.dd.plist.NSNumber clone()
meth public double doubleValue()
meth public float floatValue()
meth public int compareTo(java.lang.Object)
meth public int hashCode()
meth public int intValue()
meth public int type()
meth public java.lang.String stringValue()
meth public java.lang.String toString()
meth public long longValue()
supr com.dd.plist.NSObject
hfds boolValue,doubleValue,longValue,type

CLSS public abstract com.dd.plist.NSObject
cons public init()
intf java.lang.Cloneable
meth protected abstract void toASCII(java.lang.StringBuilder,int)
meth protected abstract void toASCIIGnuStep(java.lang.StringBuilder,int)
meth public <%0 extends java.lang.Object> {%%0} toJavaObject(java.lang.Class<{%%0}>)
meth public abstract com.dd.plist.NSObject clone()
meth public java.lang.Object toJavaObject()
meth public java.lang.String toXMLPropertyList()
meth public static com.dd.plist.NSObject fromJavaObject(java.lang.Object)
supr java.lang.Object
hfds ASCII_LINE_LENGTH,INDENT,NEWLINE

CLSS public com.dd.plist.NSSet
cons public !varargs init(boolean,com.dd.plist.NSObject[])
cons public !varargs init(com.dd.plist.NSObject[])
cons public init()
cons public init(boolean)
meth protected void toASCII(java.lang.StringBuilder,int)
meth protected void toASCIIGnuStep(java.lang.StringBuilder,int)
meth public boolean containsObject(com.dd.plist.NSObject)
meth public boolean equals(java.lang.Object)
meth public boolean intersectsSet(com.dd.plist.NSSet)
meth public boolean isSubsetOfSet(com.dd.plist.NSSet)
meth public com.dd.plist.NSObject anyObject()
meth public com.dd.plist.NSObject member(com.dd.plist.NSObject)
meth public com.dd.plist.NSObject[] allObjects()
meth public com.dd.plist.NSSet clone()
meth public int count()
meth public int hashCode()
meth public java.util.Iterator<com.dd.plist.NSObject> objectIterator()
meth public void addObject(com.dd.plist.NSObject)
meth public void removeObject(com.dd.plist.NSObject)
supr com.dd.plist.NSObject
hfds ordered,set

CLSS public com.dd.plist.NSString
cons public init(byte[],int,int,java.lang.String) throws java.io.UnsupportedEncodingException
cons public init(byte[],java.lang.String) throws java.io.UnsupportedEncodingException
cons public init(java.lang.String)
intf java.lang.Comparable<java.lang.Object>
meth protected void toASCII(java.lang.StringBuilder,int)
meth protected void toASCIIGnuStep(java.lang.StringBuilder,int)
meth public boolean boolValue()
meth public boolean equals(java.lang.Object)
meth public com.dd.plist.NSString clone()
meth public double doubleValue()
meth public float floatValue()
meth public int compareTo(java.lang.Object)
meth public int hashCode()
meth public int intValue()
meth public java.lang.String getContent()
meth public java.lang.String toString()
meth public void append(com.dd.plist.NSString)
meth public void append(java.lang.String)
meth public void prepend(com.dd.plist.NSString)
meth public void prepend(java.lang.String)
meth public void setContent(java.lang.String)
meth public void toBinary(com.dd.plist.BinaryPropertyListWriter) throws java.io.IOException
supr com.dd.plist.NSObject
hfds asciiEncoder,content,utf16beEncoder,utf8Encoder

CLSS public com.dd.plist.PropertyListFormatException
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public com.dd.plist.PropertyListParser
cons protected init()
meth protected static byte[] readAll(java.io.InputStream) throws java.io.IOException
meth public static com.dd.plist.NSObject parse(byte[]) throws com.dd.plist.PropertyListFormatException,java.io.IOException,java.text.ParseException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static com.dd.plist.NSObject parse(java.io.File) throws com.dd.plist.PropertyListFormatException,java.io.IOException,java.text.ParseException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static com.dd.plist.NSObject parse(java.io.InputStream) throws com.dd.plist.PropertyListFormatException,java.io.IOException,java.text.ParseException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static com.dd.plist.NSObject parse(java.lang.String) throws com.dd.plist.PropertyListFormatException,java.io.IOException,java.text.ParseException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static void convertToASCII(java.io.File,java.io.File) throws com.dd.plist.PropertyListFormatException,java.io.IOException,java.text.ParseException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static void convertToBinary(java.io.File,java.io.File) throws com.dd.plist.PropertyListFormatException,java.io.IOException,java.text.ParseException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static void convertToGnuStepASCII(java.io.File,java.io.File) throws com.dd.plist.PropertyListFormatException,java.io.IOException,java.text.ParseException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static void convertToXml(java.io.File,java.io.File) throws com.dd.plist.PropertyListFormatException,java.io.IOException,java.text.ParseException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static void saveAsASCII(com.dd.plist.NSArray,java.io.File) throws java.io.IOException
meth public static void saveAsASCII(com.dd.plist.NSDictionary,java.io.File) throws java.io.IOException
meth public static void saveAsBinary(com.dd.plist.NSObject,java.io.File) throws java.io.IOException
meth public static void saveAsBinary(com.dd.plist.NSObject,java.io.OutputStream) throws java.io.IOException
meth public static void saveAsGnuStepASCII(com.dd.plist.NSArray,java.io.File) throws java.io.IOException
meth public static void saveAsGnuStepASCII(com.dd.plist.NSDictionary,java.io.File) throws java.io.IOException
meth public static void saveAsXML(com.dd.plist.NSObject,java.io.File) throws java.io.IOException
meth public static void saveAsXML(com.dd.plist.NSObject,java.io.OutputStream) throws java.io.IOException
supr java.lang.Object
hfds READ_BUFFER_LENGTH,TYPE_ASCII,TYPE_BINARY,TYPE_ERROR_BLANK,TYPE_ERROR_UNKNOWN,TYPE_XML

CLSS public com.dd.plist.UID
cons public init(java.lang.String,byte[])
meth protected void toASCII(java.lang.StringBuilder,int)
meth protected void toASCIIGnuStep(java.lang.StringBuilder,int)
meth public byte[] getBytes()
meth public com.dd.plist.UID clone()
meth public java.lang.String getName()
supr com.dd.plist.NSObject
hfds bytes,name

CLSS public com.dd.plist.XMLPropertyListParser
cons public init()
meth public static com.dd.plist.NSObject parse(byte[]) throws com.dd.plist.PropertyListFormatException,java.io.IOException,java.text.ParseException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static com.dd.plist.NSObject parse(java.io.File) throws com.dd.plist.PropertyListFormatException,java.io.IOException,java.text.ParseException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static com.dd.plist.NSObject parse(java.io.InputStream) throws com.dd.plist.PropertyListFormatException,java.io.IOException,java.text.ParseException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public static com.dd.plist.NSObject parse(org.w3c.dom.Document) throws com.dd.plist.PropertyListFormatException,java.io.IOException,java.text.ParseException
meth public static javax.xml.parsers.DocumentBuilder getDocBuilder() throws javax.xml.parsers.ParserConfigurationException
supr java.lang.Object
hfds FACTORY
hcls PlistDtdResolver

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public java.io.FilterInputStream
cons protected init(java.io.InputStream)
fld protected volatile java.io.InputStream in
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.io.InputStream

CLSS public java.io.FilterOutputStream
cons public init(java.io.OutputStream)
fld protected java.io.OutputStream out
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

CLSS public abstract java.io.InputStream
cons public init()
intf java.io.Closeable
meth public abstract int read() throws java.io.IOException
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.lang.Object

CLSS public abstract java.io.OutputStream
cons public init()
intf java.io.Closeable
intf java.io.Flushable
meth public abstract void write(int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

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

CLSS public abstract interface java.util.Map<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public abstract interface static Entry
meth public abstract boolean containsKey(java.lang.Object)
meth public abstract boolean containsValue(java.lang.Object)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.util.Collection<{java.util.Map%1}> values()
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.Map%0},{java.util.Map%1}>> entrySet()
meth public abstract java.util.Set<{java.util.Map%0}> keySet()
meth public abstract void clear()
meth public abstract void putAll(java.util.Map<? extends {java.util.Map%0},? extends {java.util.Map%1}>)
meth public abstract {java.util.Map%1} get(java.lang.Object)
meth public abstract {java.util.Map%1} put({java.util.Map%0},{java.util.Map%1})
meth public abstract {java.util.Map%1} remove(java.lang.Object)
meth public boolean remove(java.lang.Object,java.lang.Object)
meth public boolean replace({java.util.Map%0},{java.util.Map%1},{java.util.Map%1})
meth public void forEach(java.util.function.BiConsumer<? super {java.util.Map%0},? super {java.util.Map%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} compute({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfAbsent({java.util.Map%0},java.util.function.Function<? super {java.util.Map%0},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfPresent({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} getOrDefault(java.lang.Object,{java.util.Map%1})
meth public {java.util.Map%1} merge({java.util.Map%0},{java.util.Map%1},java.util.function.BiFunction<? super {java.util.Map%1},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} putIfAbsent({java.util.Map%0},{java.util.Map%1})
meth public {java.util.Map%1} replace({java.util.Map%0},{java.util.Map%1})

