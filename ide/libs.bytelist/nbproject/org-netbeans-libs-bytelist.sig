#Signature file v4.1
#Version 0.34.0

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.CharSequence
meth public abstract char charAt(int)
meth public abstract int length()
meth public abstract java.lang.CharSequence subSequence(int,int)
meth public abstract java.lang.String toString()
meth public java.util.stream.IntStream chars()
meth public java.util.stream.IntStream codePoints()

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

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

CLSS public final org.jruby.util.ByteList
cons public init()
cons public init(byte[])
cons public init(byte[],boolean)
cons public init(byte[],int,int)
cons public init(byte[],int,int,boolean)
cons public init(byte[],int,int,org.jcodings.Encoding,boolean)
cons public init(byte[],org.jcodings.Encoding)
cons public init(byte[],org.jcodings.Encoding,boolean)
cons public init(int)
cons public init(org.jruby.util.ByteList)
cons public init(org.jruby.util.ByteList,boolean)
 anno 0 java.lang.Deprecated()
cons public init(org.jruby.util.ByteList,int,int)
fld public byte[] bytes
 anno 0 java.lang.Deprecated()
fld public final static byte[] NULL_ARRAY
fld public final static org.jruby.util.ByteList EMPTY_BYTELIST
fld public int begin
 anno 0 java.lang.Deprecated()
fld public int realSize
 anno 0 java.lang.Deprecated()
fld public org.jcodings.Encoding encoding
 anno 0 java.lang.Deprecated()
intf java.io.Serializable
intf java.lang.CharSequence
intf java.lang.Comparable
meth public boolean endsWith(org.jruby.util.ByteList)
meth public boolean equal(org.jruby.util.ByteList)
meth public boolean equals(java.lang.Object)
meth public boolean sample_equals(java.lang.Object)
meth public boolean startsWith(org.jruby.util.ByteList)
meth public boolean startsWith(org.jruby.util.ByteList,int)
meth public byte[] bytes()
meth public byte[] unsafeBytes()
meth public char charAt(int)
meth public final byte[] getUnsafeBytes()
meth public final int getBegin()
meth public final int getRealSize()
meth public final int realSize()
meth public final org.jcodings.Encoding getEncoding()
meth public final void realSize(int)
meth public final void setBegin(int)
meth public final void setEncoding(org.jcodings.Encoding)
meth public final void setRealSize(int)
meth public final void setUnsafeBytes(byte[])
meth public int begin()
meth public int caseInsensitiveCmp(org.jruby.util.ByteList)
meth public int cmp(org.jruby.util.ByteList)
meth public int compareTo(java.lang.Object)
meth public int get(int)
meth public int getEnc(int)
meth public int hashCode()
meth public int indexOf(int)
meth public int indexOf(int,int)
meth public int indexOf(org.jruby.util.ByteList)
meth public int indexOf(org.jruby.util.ByteList,int)
meth public int lastIndexOf(int)
meth public int lastIndexOf(int,int)
meth public int lastIndexOf(org.jruby.util.ByteList)
meth public int lastIndexOf(org.jruby.util.ByteList,int)
meth public int length()
meth public int lengthEnc()
meth public java.lang.CharSequence subSequence(int,int)
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public org.jruby.util.ByteList append(byte)
meth public org.jruby.util.ByteList append(int)
meth public org.jruby.util.ByteList append(java.io.InputStream,int) throws java.io.IOException
meth public org.jruby.util.ByteList dup()
meth public org.jruby.util.ByteList dup(int)
meth public org.jruby.util.ByteList makeShared(int,int)
meth public org.jruby.util.ByteList shallowDup()
meth public static byte[] encode(java.lang.CharSequence,java.lang.String)
meth public static byte[] plain(char[])
meth public static byte[] plain(java.lang.CharSequence)
meth public static char[] plain(byte[])
meth public static char[] plain(byte[],int,int)
meth public static int memcmp(byte[],int,byte[],int,int)
meth public static int memcmp(byte[],int,int,byte[],int,int)
meth public static java.lang.String decode(byte[],int,int,java.lang.String)
meth public static java.lang.String decode(byte[],java.lang.String)
meth public static org.jcodings.Encoding safeEncoding(org.jcodings.Encoding)
meth public static org.jruby.util.ByteList create(java.lang.CharSequence)
meth public void append(byte[])
meth public void append(byte[],int,int)
meth public void append(java.nio.ByteBuffer,int)
meth public void append(org.jruby.util.ByteList)
meth public void append(org.jruby.util.ByteList,int,int)
meth public void delete(int,int)
meth public void ensure(int)
meth public void fill(int,int)
meth public void insert(int,int)
meth public void invalidate()
meth public void length(int)
meth public void prepend(byte)
meth public void realloc(int)
meth public void replace(byte[])
meth public void replace(int,int,byte[])
meth public void replace(int,int,byte[],int,int)
meth public void replace(int,int,org.jruby.util.ByteList)
meth public void set(int,int)
meth public void unsafeReplace(int,int,byte[])
meth public void unsafeReplace(int,int,byte[],int,int)
meth public void unsafeReplace(int,int,org.jruby.util.ByteList)
meth public void unshare()
meth public void unshare(int)
meth public void view(int,int)
supr java.lang.Object
hfds DEFAULT_SIZE,charsetsByAlias,hash,serialVersionUID,stringValue

