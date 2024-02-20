#Signature file v4.1
#Version 1.48.0

CLSS public abstract interface com.sun.org.apache.commons.logging.Log
meth public abstract boolean isDebugEnabled()
meth public abstract boolean isErrorEnabled()
meth public abstract boolean isFatalEnabled()
meth public abstract boolean isInfoEnabled()
meth public abstract boolean isTraceEnabled()
meth public abstract boolean isWarnEnabled()
meth public abstract void debug(java.lang.Object)
meth public abstract void debug(java.lang.Object,java.lang.Throwable)
meth public abstract void error(java.lang.Object)
meth public abstract void error(java.lang.Object,java.lang.Throwable)
meth public abstract void fatal(java.lang.Object)
meth public abstract void fatal(java.lang.Object,java.lang.Throwable)
meth public abstract void info(java.lang.Object)
meth public abstract void info(java.lang.Object,java.lang.Throwable)
meth public abstract void trace(java.lang.Object)
meth public abstract void trace(java.lang.Object,java.lang.Throwable)
meth public abstract void warn(java.lang.Object)
meth public abstract void warn(java.lang.Object,java.lang.Throwable)

CLSS public com.sun.org.apache.commons.logging.LogConfigurationException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
fld protected java.lang.Throwable cause
meth public java.lang.Throwable getCause()
supr java.lang.RuntimeException

CLSS public abstract com.sun.org.apache.commons.logging.LogFactory
cons protected init()
fld protected final static java.lang.String SERVICE_ID = "META-INF/services/com.sun.org.apache.commons.logging.LogFactory"
fld protected static java.util.Hashtable factories
fld public final static java.lang.String FACTORY_DEFAULT = "com.sun.org.apache.commons.logging.impl.LogFactoryImpl"
fld public final static java.lang.String FACTORY_PROPERTIES = "commons-logging.properties"
fld public final static java.lang.String FACTORY_PROPERTY = "com.sun.org.apache.commons.logging.LogFactory"
meth protected static com.sun.org.apache.commons.logging.LogFactory newFactory(java.lang.String,java.lang.ClassLoader)
meth protected static java.lang.ClassLoader getContextClassLoader()
meth public abstract com.sun.org.apache.commons.logging.Log getInstance(java.lang.Class)
meth public abstract com.sun.org.apache.commons.logging.Log getInstance(java.lang.String)
meth public abstract java.lang.Object getAttribute(java.lang.String)
meth public abstract java.lang.String[] getAttributeNames()
meth public abstract void release()
meth public abstract void removeAttribute(java.lang.String)
meth public abstract void setAttribute(java.lang.String,java.lang.Object)
meth public static com.sun.org.apache.commons.logging.Log getLog(java.lang.Class)
meth public static com.sun.org.apache.commons.logging.Log getLog(java.lang.String)
meth public static com.sun.org.apache.commons.logging.LogFactory getFactory()
meth public static void release(java.lang.ClassLoader)
meth public static void releaseAll()
supr java.lang.Object
hfds class$com$sun$org$apache$commons$logging$LogFactory,class$java$lang$Thread

CLSS public com.sun.org.apache.commons.logging.LogSource
fld protected static boolean jdk14IsAvailable
fld protected static boolean log4jIsAvailable
fld protected static java.lang.reflect.Constructor logImplctor
fld protected static java.util.Hashtable logs
meth public static com.sun.org.apache.commons.logging.Log getInstance(java.lang.Class)
meth public static com.sun.org.apache.commons.logging.Log getInstance(java.lang.String)
meth public static com.sun.org.apache.commons.logging.Log makeNewLogInstance(java.lang.String)
meth public static java.lang.String[] getLogNames()
meth public static void setLogImplementation(java.lang.Class) throws java.lang.NoSuchMethodException
meth public static void setLogImplementation(java.lang.String) throws java.lang.ClassNotFoundException,java.lang.NoSuchMethodException
supr java.lang.Object

CLSS public com.sun.org.apache.commons.logging.impl.Jdk14Logger
cons public init(java.lang.String)
fld protected java.lang.String name
fld protected java.util.logging.Logger logger
intf com.sun.org.apache.commons.logging.Log
intf java.io.Serializable
meth public boolean isDebugEnabled()
meth public boolean isErrorEnabled()
meth public boolean isFatalEnabled()
meth public boolean isInfoEnabled()
meth public boolean isTraceEnabled()
meth public boolean isWarnEnabled()
meth public java.util.logging.Logger getLogger()
meth public void debug(java.lang.Object)
meth public void debug(java.lang.Object,java.lang.Throwable)
meth public void error(java.lang.Object)
meth public void error(java.lang.Object,java.lang.Throwable)
meth public void fatal(java.lang.Object)
meth public void fatal(java.lang.Object,java.lang.Throwable)
meth public void info(java.lang.Object)
meth public void info(java.lang.Object,java.lang.Throwable)
meth public void trace(java.lang.Object)
meth public void trace(java.lang.Object,java.lang.Throwable)
meth public void warn(java.lang.Object)
meth public void warn(java.lang.Object,java.lang.Throwable)
supr java.lang.Object

CLSS public com.sun.org.apache.commons.logging.impl.LogFactoryImpl
cons public init()
fld protected final static java.lang.String LOG_PROPERTY_OLD = "com.sun.org.apache.commons.logging.log"
fld protected java.lang.Class[] logConstructorSignature
fld protected java.lang.Class[] logMethodSignature
fld protected java.lang.reflect.Constructor logConstructor
fld protected java.lang.reflect.Method logMethod
fld protected java.util.Hashtable attributes
fld protected java.util.Hashtable instances
fld public final static java.lang.String LOG_PROPERTY = "com.sun.org.apache.commons.logging.Log"
meth protected boolean isJdk13LumberjackAvailable()
meth protected boolean isJdk14Available()
meth protected boolean isLog4JAvailable()
meth protected com.sun.org.apache.commons.logging.Log newInstance(java.lang.String)
meth protected java.lang.String getLogClassName()
meth protected java.lang.reflect.Constructor getLogConstructor()
meth public com.sun.org.apache.commons.logging.Log getInstance(java.lang.Class)
meth public com.sun.org.apache.commons.logging.Log getInstance(java.lang.String)
meth public java.lang.Object getAttribute(java.lang.String)
meth public java.lang.String[] getAttributeNames()
meth public void release()
meth public void removeAttribute(java.lang.String)
meth public void setAttribute(java.lang.String,java.lang.Object)
supr com.sun.org.apache.commons.logging.LogFactory
hfds LOG_INTERFACE,class$com$sun$org$apache$commons$logging$LogFactory,class$java$lang$String,logClassName

CLSS public com.sun.org.apache.commons.logging.impl.NoOpLog
cons public init()
cons public init(java.lang.String)
intf com.sun.org.apache.commons.logging.Log
intf java.io.Serializable
meth public final boolean isDebugEnabled()
meth public final boolean isErrorEnabled()
meth public final boolean isFatalEnabled()
meth public final boolean isInfoEnabled()
meth public final boolean isTraceEnabled()
meth public final boolean isWarnEnabled()
meth public void debug(java.lang.Object)
meth public void debug(java.lang.Object,java.lang.Throwable)
meth public void error(java.lang.Object)
meth public void error(java.lang.Object,java.lang.Throwable)
meth public void fatal(java.lang.Object)
meth public void fatal(java.lang.Object,java.lang.Throwable)
meth public void info(java.lang.Object)
meth public void info(java.lang.Object,java.lang.Throwable)
meth public void trace(java.lang.Object)
meth public void trace(java.lang.Object,java.lang.Throwable)
meth public void warn(java.lang.Object)
meth public void warn(java.lang.Object,java.lang.Throwable)
supr java.lang.Object

CLSS public com.sun.org.apache.commons.logging.impl.SimpleLog
cons public init(java.lang.String)
fld protected final static java.lang.String DEFAULT_DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss:SSS zzz"
fld protected final static java.lang.String systemPrefix = "com.sun.org.apache.commons.logging.simplelog."
fld protected final static java.util.Properties simpleLogProps
fld protected int currentLogLevel
fld protected java.lang.String logName
fld protected static boolean showDateTime
fld protected static boolean showLogName
fld protected static boolean showShortName
fld protected static java.lang.String dateTimeFormat
fld protected static java.text.DateFormat dateFormatter
fld public final static int LOG_LEVEL_ALL = 0
fld public final static int LOG_LEVEL_DEBUG = 2
fld public final static int LOG_LEVEL_ERROR = 5
fld public final static int LOG_LEVEL_FATAL = 6
fld public final static int LOG_LEVEL_INFO = 3
fld public final static int LOG_LEVEL_OFF = 7
fld public final static int LOG_LEVEL_TRACE = 1
fld public final static int LOG_LEVEL_WARN = 4
intf com.sun.org.apache.commons.logging.Log
intf java.io.Serializable
meth protected boolean isLevelEnabled(int)
meth protected void log(int,java.lang.Object,java.lang.Throwable)
meth protected void write(java.lang.StringBuffer)
meth public final boolean isDebugEnabled()
meth public final boolean isErrorEnabled()
meth public final boolean isFatalEnabled()
meth public final boolean isInfoEnabled()
meth public final boolean isTraceEnabled()
meth public final boolean isWarnEnabled()
meth public final void debug(java.lang.Object)
meth public final void debug(java.lang.Object,java.lang.Throwable)
meth public final void error(java.lang.Object)
meth public final void error(java.lang.Object,java.lang.Throwable)
meth public final void fatal(java.lang.Object)
meth public final void fatal(java.lang.Object,java.lang.Throwable)
meth public final void info(java.lang.Object)
meth public final void info(java.lang.Object,java.lang.Throwable)
meth public final void trace(java.lang.Object)
meth public final void trace(java.lang.Object,java.lang.Throwable)
meth public final void warn(java.lang.Object)
meth public final void warn(java.lang.Object,java.lang.Throwable)
meth public int getLevel()
meth public void setLevel(int)
supr java.lang.Object
hfds class$com$sun$org$apache$commons$logging$impl$SimpleLog,class$java$lang$Thread,shortLogName

CLSS public abstract interface java.io.Serializable

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

