#Signature file v4.1
#Version 1.35

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

CLSS public abstract org.netbeans.core.network.proxy.pac.PacHelperMethods
cons public init()
intf org.netbeans.core.network.proxy.pac.PacHelperMethodsMicrosoft
intf org.netbeans.core.network.proxy.pac.PacHelperMethodsNetscape
meth public void alert(java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.netbeans.core.network.proxy.pac.PacHelperMethodsMicrosoft
meth public abstract boolean isInNetEx(java.lang.String,java.lang.String)
meth public abstract boolean isResolvableEx(java.lang.String)
meth public abstract java.lang.String dnsResolveEx(java.lang.String)
meth public abstract java.lang.String getClientVersion()
meth public abstract java.lang.String myIpAddressEx()
meth public abstract java.lang.String sortIpAddressList(java.lang.String)

CLSS public abstract interface org.netbeans.core.network.proxy.pac.PacHelperMethodsNetscape
meth public abstract !varargs boolean dateRange(java.lang.Object[])
meth public abstract !varargs boolean timeRange(java.lang.Object[])
meth public abstract !varargs boolean weekdayRange(java.lang.Object[])
meth public abstract boolean dnsDomainIs(java.lang.String,java.lang.String)
meth public abstract boolean isInNet(java.lang.String,java.lang.String,java.lang.String)
meth public abstract boolean isPlainHostName(java.lang.String)
meth public abstract boolean isResolvable(java.lang.String)
meth public abstract boolean localHostOrDomainIs(java.lang.String,java.lang.String)
meth public abstract boolean shExpMatch(java.lang.String,java.lang.String)
meth public abstract int dnsDomainLevels(java.lang.String)
meth public abstract java.lang.String dnsResolve(java.lang.String)
meth public abstract java.lang.String myIpAddress()

CLSS public final !enum org.netbeans.core.network.proxy.pac.PacJsEntryFunction
fld public final static org.netbeans.core.network.proxy.pac.PacJsEntryFunction IPV6_AWARE
fld public final static org.netbeans.core.network.proxy.pac.PacJsEntryFunction STANDARD
meth public java.lang.String getJsFunctionName()
meth public static org.netbeans.core.network.proxy.pac.PacJsEntryFunction valueOf(java.lang.String)
meth public static org.netbeans.core.network.proxy.pac.PacJsEntryFunction[] values()
supr java.lang.Enum<org.netbeans.core.network.proxy.pac.PacJsEntryFunction>
hfds jsFunctionName

CLSS public org.netbeans.core.network.proxy.pac.PacParsingException
cons public init(java.lang.Exception)
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
supr java.lang.Exception

CLSS public abstract interface org.netbeans.core.network.proxy.pac.PacScriptEvaluator
meth public abstract boolean usesCaching()
meth public abstract java.lang.String getEngineInfo()
meth public abstract java.lang.String getJsEntryFunction()
meth public abstract java.lang.String getPacScriptSource()
meth public abstract java.util.List<java.net.Proxy> findProxyForURL(java.net.URI) throws org.netbeans.core.network.proxy.pac.PacValidationException

CLSS public abstract interface org.netbeans.core.network.proxy.pac.PacScriptEvaluatorFactory
meth public abstract org.netbeans.core.network.proxy.pac.PacScriptEvaluator createPacScriptEvaluator(java.lang.String) throws org.netbeans.core.network.proxy.pac.PacParsingException
meth public abstract org.netbeans.core.network.proxy.pac.PacScriptEvaluator getNoOpEvaluator()

CLSS public org.netbeans.core.network.proxy.pac.PacScriptEvaluatorNoProxy
cons public init()
intf org.netbeans.core.network.proxy.pac.PacScriptEvaluator
meth public boolean usesCaching()
meth public java.lang.String getEngineInfo()
meth public java.lang.String getJsEntryFunction()
meth public java.lang.String getPacScriptSource()
meth public java.util.List<java.net.Proxy> findProxyForURL(java.net.URI)
supr java.lang.Object

CLSS public org.netbeans.core.network.proxy.pac.PacUtils
cons public init()
fld public final static int PRECOMPILED_GLOB_CACHE_MAX_ITEMS = 10
meth public static <%0 extends java.lang.Object> java.lang.String toSemiColonList(java.util.List<{%%0}>)
meth public static <%0 extends java.lang.Object> java.lang.String toSemiColonList(java.util.List<{%%0}>,java.util.function.Function<{%%0},java.lang.String>)
meth public static boolean ipPrefixMatch(java.net.InetAddress,java.lang.String)
meth public static java.lang.String toSemiColonListInetAddress(java.net.InetAddress[])
meth public static java.lang.String toSemiColonListInetAddress(java.util.List<java.net.InetAddress>)
meth public static java.lang.String toStrippedURLStr(java.net.URI)
meth public static java.util.regex.Pattern createRegexPatternFromGlob(java.lang.String)
supr java.lang.Object
hfds PRECOMPILED_GLOB_CACHE

CLSS public org.netbeans.core.network.proxy.pac.PacValidationException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
supr java.lang.Exception

CLSS public org.netbeans.core.network.proxy.pac.datetime.PacUtilsDateTime
cons public init()
fld public final static java.util.List<java.lang.String> MONTH_NAMES
fld public final static java.util.List<java.lang.String> WEEKDAY_NAMES
innr public static PacDateTimeInputException
meth public !varargs static boolean isInDateRange(java.util.Date,java.lang.Object[]) throws org.netbeans.core.network.proxy.pac.datetime.PacUtilsDateTime$PacDateTimeInputException
meth public !varargs static boolean isInTimeRange(java.util.Date,java.lang.Object[]) throws org.netbeans.core.network.proxy.pac.datetime.PacUtilsDateTime$PacDateTimeInputException
meth public !varargs static boolean isInWeekdayRange(java.util.Date,java.lang.Object[]) throws org.netbeans.core.network.proxy.pac.datetime.PacUtilsDateTime$PacDateTimeInputException
meth public !varargs static boolean usesGMT(java.lang.Object[])
meth public !varargs static int getNoOfParams(java.lang.Object[])
supr java.lang.Object
hfds UTC_TIME

CLSS public static org.netbeans.core.network.proxy.pac.datetime.PacUtilsDateTime$PacDateTimeInputException
 outer org.netbeans.core.network.proxy.pac.datetime.PacUtilsDateTime
cons public init(java.lang.String)
supr java.lang.Exception

CLSS abstract interface org.netbeans.core.network.proxy.pac.datetime.package-info

CLSS public org.netbeans.core.network.proxy.pac.impl.NbPacHelperMethods
cons public init()
fld public final static int DNS_TIMEOUT_MS = 4000
meth public !varargs boolean dateRange(java.lang.Object[])
meth public !varargs boolean timeRange(java.lang.Object[])
meth public !varargs boolean weekdayRange(java.lang.Object[])
meth public boolean dnsDomainIs(java.lang.String,java.lang.String)
meth public boolean isInNet(java.lang.String,java.lang.String,java.lang.String)
meth public boolean isInNetEx(java.lang.String,java.lang.String)
meth public boolean isPlainHostName(java.lang.String)
meth public boolean isResolvable(java.lang.String)
meth public boolean isResolvableEx(java.lang.String)
meth public boolean localHostOrDomainIs(java.lang.String,java.lang.String)
meth public boolean shExpMatch(java.lang.String,java.lang.String)
meth public int dnsDomainLevels(java.lang.String)
meth public java.lang.String dnsResolve(java.lang.String)
meth public java.lang.String dnsResolveEx(java.lang.String)
meth public java.lang.String getClientVersion()
meth public java.lang.String myIpAddress()
meth public java.lang.String myIpAddressEx()
meth public java.lang.String sortIpAddressList(java.lang.String)
meth public void alert(java.lang.String)
supr org.netbeans.core.network.proxy.pac.PacHelperMethods
hfds LOGGER

CLSS public org.netbeans.core.network.proxy.pac.impl.NbPacScriptEvaluator
cons public init(java.lang.String) throws org.netbeans.core.network.proxy.pac.PacParsingException
intf org.netbeans.core.network.proxy.pac.PacScriptEvaluator
meth public boolean usesCaching()
meth public java.lang.String getEngineInfo()
meth public java.lang.String getJsEntryFunction()
meth public java.lang.String getPacScriptSource()
meth public java.util.List<java.net.Proxy> findProxyForURL(java.net.URI) throws org.netbeans.core.network.proxy.pac.PacValidationException
meth public static javax.script.ScriptEngine newAllowedPacEngine(java.lang.String,java.lang.StringBuilder)
supr java.lang.Object
hfds LOGGER,PAC_DIRECT,PAC_HTTPS_FFEXT,PAC_HTTP_FFEXT,PAC_PROXY,PAC_SOCKS,PAC_SOCKS4_FFEXT,PAC_SOCKS5_FFEXT,canUseURLCaching,pacScriptSource,resultCache,scriptEngine
hcls PacScriptEngine

CLSS public org.netbeans.core.network.proxy.pac.impl.NbPacScriptEvaluatorFactory
cons public init()
intf org.netbeans.core.network.proxy.pac.PacScriptEvaluatorFactory
meth public org.netbeans.core.network.proxy.pac.PacScriptEvaluator createPacScriptEvaluator(java.lang.String) throws org.netbeans.core.network.proxy.pac.PacParsingException
meth public org.netbeans.core.network.proxy.pac.PacScriptEvaluator getNoOpEvaluator()
supr java.lang.Object

CLSS abstract interface org.netbeans.core.network.proxy.pac.impl.package-info

CLSS abstract interface org.netbeans.core.network.proxy.pac.package-info

CLSS public org.netbeans.core.network.utils.HostnameUtils
meth public static java.lang.String getNetworkHostname() throws org.netbeans.core.network.utils.NativeException
supr java.lang.Object

CLSS public org.netbeans.core.network.utils.IpAddressUtils
innr public final static !enum IpTypePreference
meth public static boolean isValidIpv4Address(java.lang.String)
meth public static boolean looksLikeIpv4Literal(java.lang.String)
meth public static boolean looksLikeIpv6Literal(java.lang.String)
meth public static java.lang.String removeDomain(java.lang.String)
meth public static java.net.InetAddress nameResolve(java.lang.String,int) throws java.lang.InterruptedException,java.net.UnknownHostException,java.util.concurrent.TimeoutException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.net.InetAddress nameResolve(java.lang.String,int,org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference) throws java.lang.InterruptedException,java.net.UnknownHostException,java.util.concurrent.TimeoutException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.net.InetAddress[] nameResolveArr(java.lang.String,int,org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference) throws java.lang.InterruptedException,java.net.UnknownHostException,java.util.concurrent.TimeoutException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static void removeLoopback(java.util.List<java.net.InetAddress>)
meth public static void sortIpAddresses(java.util.List<java.net.InetAddress>,boolean)
meth public static void sortIpAddressesShallow(java.util.List<java.net.InetAddress>,boolean)
supr java.lang.Object
hfds INSTANCE,IPV4_PATTERN,RP
hcls DnsTimeoutTask,InetAddressComparator

CLSS public final static !enum org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference
 outer org.netbeans.core.network.utils.IpAddressUtils
fld public final static org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference ANY_IPV4_PREF
fld public final static org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference ANY_IPV6_PREF
fld public final static org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference ANY_JDK_PREF
fld public final static org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference IPV4_ONLY
fld public final static org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference IPV6_ONLY
meth public static org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference valueOf(java.lang.String)
meth public static org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference[] values()
supr java.lang.Enum<org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference>

CLSS public org.netbeans.core.network.utils.LocalAddressUtils
meth public static boolean isSoftwareVirtualAdapter(java.net.NetworkInterface)
meth public static java.net.InetAddress getLocalHost() throws java.net.UnknownHostException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.net.InetAddress getLoopbackAddress(org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.net.InetAddress getMostLikelyLocalInetAddress(org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.net.InetAddress[] getLocalHostAddresses(org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference) throws java.net.UnknownHostException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.net.InetAddress[] getMostLikelyLocalInetAddresses(org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.List<java.net.InetAddress> getDatagramLocalInetAddress(org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.List<java.net.InetAddress> getPrioritizedLocalHostAddresses(org.netbeans.core.network.utils.IpAddressUtils$IpTypePreference)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static void refreshNetworkInfo(boolean)
meth public static void warmUp()
supr java.lang.Object
hfds C1,C2,C3,C4,LOCK,LOG,LOOPBACK_IPV4,LOOPBACK_IPV4_RAW,LOOPBACK_IPV6,LOOPBACK_IPV6_RAW,RP,SOMEADDR_IPV4,SOMEADDR_IPV4_RAW,SOMEADDR_IPV6,SOMEADDR_IPV6_RAW,fut1,fut2,fut3,fut4

CLSS public org.netbeans.core.network.utils.NativeException
cons public init(int)
cons public init(int,java.lang.String)
meth public int getErrorCode()
supr java.lang.Exception
hfds errorCode

CLSS public org.netbeans.core.network.utils.SimpleObjCache<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(int)
meth public int getCacheSize()
meth public void clear()
meth public {org.netbeans.core.network.utils.SimpleObjCache%1} get({org.netbeans.core.network.utils.SimpleObjCache%0})
meth public {org.netbeans.core.network.utils.SimpleObjCache%1} put({org.netbeans.core.network.utils.SimpleObjCache%0},{org.netbeans.core.network.utils.SimpleObjCache%1})
supr java.lang.Object
hfds map,maxSize
hcls ValueWrapper

CLSS abstract interface org.netbeans.core.network.utils.package-info

