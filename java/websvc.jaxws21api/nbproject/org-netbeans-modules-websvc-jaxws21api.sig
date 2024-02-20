#Signature file v4.1
#Version 1.55

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface !annotation java.lang.annotation.Inherited
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Repeatable
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> value()

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

CLSS public abstract java.security.BasicPermission
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public boolean implies(java.security.Permission)
meth public int hashCode()
meth public java.lang.String getActions()
meth public java.security.PermissionCollection newPermissionCollection()
supr java.security.Permission

CLSS public abstract interface java.security.Guard
meth public abstract void checkGuard(java.lang.Object)

CLSS public abstract java.security.Permission
cons public init(java.lang.String)
intf java.io.Serializable
intf java.security.Guard
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean implies(java.security.Permission)
meth public abstract int hashCode()
meth public abstract java.lang.String getActions()
meth public final java.lang.String getName()
meth public java.lang.String toString()
meth public java.security.PermissionCollection newPermissionCollection()
meth public void checkGuard(java.lang.Object)
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

CLSS public abstract interface java.util.concurrent.Future<%0 extends java.lang.Object>
meth public abstract boolean cancel(boolean)
meth public abstract boolean isCancelled()
meth public abstract boolean isDone()
meth public abstract {java.util.concurrent.Future%0} get() throws java.lang.InterruptedException,java.util.concurrent.ExecutionException
meth public abstract {java.util.concurrent.Future%0} get(long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException,java.util.concurrent.TimeoutException

CLSS public abstract interface !annotation javax.jws.HandlerChain
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String name()
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.String file()

CLSS public abstract interface !annotation javax.jws.Oneway
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.jws.WebMethod
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean exclude()
meth public abstract !hasdefault java.lang.String action()
meth public abstract !hasdefault java.lang.String operationName()

CLSS public abstract interface !annotation javax.jws.WebParam
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
innr public final static !enum Mode
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean header()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String partName()
meth public abstract !hasdefault java.lang.String targetNamespace()
meth public abstract !hasdefault javax.jws.WebParam$Mode mode()

CLSS public final static !enum javax.jws.WebParam$Mode
 outer javax.jws.WebParam
fld public final static javax.jws.WebParam$Mode IN
fld public final static javax.jws.WebParam$Mode INOUT
fld public final static javax.jws.WebParam$Mode OUT
meth public static javax.jws.WebParam$Mode valueOf(java.lang.String)
meth public static javax.jws.WebParam$Mode[] values()
supr java.lang.Enum<javax.jws.WebParam$Mode>

CLSS public abstract interface !annotation javax.jws.WebResult
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean header()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String partName()
meth public abstract !hasdefault java.lang.String targetNamespace()

CLSS public abstract interface !annotation javax.jws.WebService
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String endpointInterface()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String portName()
meth public abstract !hasdefault java.lang.String serviceName()
meth public abstract !hasdefault java.lang.String targetNamespace()
meth public abstract !hasdefault java.lang.String wsdlLocation()

CLSS public abstract interface !annotation javax.jws.soap.InitParam
 anno 0 java.lang.Deprecated()
intf java.lang.annotation.Annotation
meth public abstract java.lang.String name()
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation javax.jws.soap.SOAPBinding
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
innr public final static !enum ParameterStyle
innr public final static !enum Style
innr public final static !enum Use
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.jws.soap.SOAPBinding$ParameterStyle parameterStyle()
meth public abstract !hasdefault javax.jws.soap.SOAPBinding$Style style()
meth public abstract !hasdefault javax.jws.soap.SOAPBinding$Use use()

CLSS public final static !enum javax.jws.soap.SOAPBinding$ParameterStyle
 outer javax.jws.soap.SOAPBinding
fld public final static javax.jws.soap.SOAPBinding$ParameterStyle BARE
fld public final static javax.jws.soap.SOAPBinding$ParameterStyle WRAPPED
meth public static javax.jws.soap.SOAPBinding$ParameterStyle valueOf(java.lang.String)
meth public static javax.jws.soap.SOAPBinding$ParameterStyle[] values()
supr java.lang.Enum<javax.jws.soap.SOAPBinding$ParameterStyle>

CLSS public final static !enum javax.jws.soap.SOAPBinding$Style
 outer javax.jws.soap.SOAPBinding
fld public final static javax.jws.soap.SOAPBinding$Style DOCUMENT
fld public final static javax.jws.soap.SOAPBinding$Style RPC
meth public static javax.jws.soap.SOAPBinding$Style valueOf(java.lang.String)
meth public static javax.jws.soap.SOAPBinding$Style[] values()
supr java.lang.Enum<javax.jws.soap.SOAPBinding$Style>

CLSS public final static !enum javax.jws.soap.SOAPBinding$Use
 outer javax.jws.soap.SOAPBinding
fld public final static javax.jws.soap.SOAPBinding$Use ENCODED
fld public final static javax.jws.soap.SOAPBinding$Use LITERAL
meth public static javax.jws.soap.SOAPBinding$Use valueOf(java.lang.String)
meth public static javax.jws.soap.SOAPBinding$Use[] values()
supr java.lang.Enum<javax.jws.soap.SOAPBinding$Use>

CLSS public abstract interface !annotation javax.jws.soap.SOAPMessageHandler
 anno 0 java.lang.Deprecated()
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String[] headers()
meth public abstract !hasdefault java.lang.String[] roles()
meth public abstract !hasdefault javax.jws.soap.InitParam[] initParams()
meth public abstract java.lang.String className()

CLSS public abstract interface !annotation javax.jws.soap.SOAPMessageHandlers
 anno 0 java.lang.Deprecated()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract javax.jws.soap.SOAPMessageHandler[] value()

CLSS public abstract interface !annotation javax.xml.bind.annotation.XmlRootElement
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String namespace()

CLSS public abstract interface !annotation javax.xml.bind.annotation.XmlSchema
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE])
fld public final static java.lang.String NO_LOCATION = "##generate"
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String location()
meth public abstract !hasdefault java.lang.String namespace()
meth public abstract !hasdefault javax.xml.bind.annotation.XmlNsForm attributeFormDefault()
meth public abstract !hasdefault javax.xml.bind.annotation.XmlNsForm elementFormDefault()
meth public abstract !hasdefault javax.xml.bind.annotation.XmlNs[] xmlns()

CLSS public abstract interface !annotation javax.xml.bind.annotation.XmlTransient
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD, METHOD, TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.xml.bind.annotation.XmlType
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
innr public final static DEFAULT
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class factoryClass()
meth public abstract !hasdefault java.lang.String factoryMethod()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String namespace()
meth public abstract !hasdefault java.lang.String[] propOrder()

CLSS public abstract javax.xml.soap.AttachmentPart
cons public init()
meth public abstract byte[] getRawContentBytes() throws javax.xml.soap.SOAPException
meth public abstract int getSize() throws javax.xml.soap.SOAPException
meth public abstract java.io.InputStream getBase64Content() throws javax.xml.soap.SOAPException
meth public abstract java.io.InputStream getRawContent() throws javax.xml.soap.SOAPException
meth public abstract java.lang.Object getContent() throws javax.xml.soap.SOAPException
meth public abstract java.lang.String[] getMimeHeader(java.lang.String)
meth public abstract java.util.Iterator<javax.xml.soap.MimeHeader> getAllMimeHeaders()
meth public abstract java.util.Iterator<javax.xml.soap.MimeHeader> getMatchingMimeHeaders(java.lang.String[])
meth public abstract java.util.Iterator<javax.xml.soap.MimeHeader> getNonMatchingMimeHeaders(java.lang.String[])
meth public abstract javax.activation.DataHandler getDataHandler() throws javax.xml.soap.SOAPException
meth public abstract void addMimeHeader(java.lang.String,java.lang.String)
meth public abstract void clearContent()
meth public abstract void removeAllMimeHeaders()
meth public abstract void removeMimeHeader(java.lang.String)
meth public abstract void setBase64Content(java.io.InputStream,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract void setContent(java.lang.Object,java.lang.String)
meth public abstract void setDataHandler(javax.activation.DataHandler)
meth public abstract void setMimeHeader(java.lang.String,java.lang.String)
meth public abstract void setRawContent(java.io.InputStream,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract void setRawContentBytes(byte[],int,int,java.lang.String) throws javax.xml.soap.SOAPException
meth public java.lang.String getContentId()
meth public java.lang.String getContentLocation()
meth public java.lang.String getContentType()
meth public void setContentId(java.lang.String)
meth public void setContentLocation(java.lang.String)
meth public void setContentType(java.lang.String)
supr java.lang.Object

CLSS public abstract interface javax.xml.soap.Detail
intf javax.xml.soap.SOAPFaultElement
meth public abstract java.util.Iterator<javax.xml.soap.DetailEntry> getDetailEntries()
meth public abstract javax.xml.soap.DetailEntry addDetailEntry(javax.xml.namespace.QName) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.DetailEntry addDetailEntry(javax.xml.soap.Name) throws javax.xml.soap.SOAPException

CLSS public abstract interface javax.xml.soap.DetailEntry
intf javax.xml.soap.SOAPElement

CLSS public abstract javax.xml.soap.MessageFactory
cons public init()
meth public abstract javax.xml.soap.SOAPMessage createMessage() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPMessage createMessage(javax.xml.soap.MimeHeaders,java.io.InputStream) throws java.io.IOException,javax.xml.soap.SOAPException
meth public static javax.xml.soap.MessageFactory newInstance() throws javax.xml.soap.SOAPException
meth public static javax.xml.soap.MessageFactory newInstance(java.lang.String) throws javax.xml.soap.SOAPException
supr java.lang.Object
hfds DEFAULT_MESSAGE_FACTORY

CLSS public javax.xml.soap.MimeHeader
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String getValue()
supr java.lang.Object
hfds name,value

CLSS public javax.xml.soap.MimeHeaders
cons public init()
meth public java.lang.String[] getHeader(java.lang.String)
meth public java.util.Iterator<javax.xml.soap.MimeHeader> getAllHeaders()
meth public java.util.Iterator<javax.xml.soap.MimeHeader> getMatchingHeaders(java.lang.String[])
meth public java.util.Iterator<javax.xml.soap.MimeHeader> getNonMatchingHeaders(java.lang.String[])
meth public void addHeader(java.lang.String,java.lang.String)
meth public void removeAllHeaders()
meth public void removeHeader(java.lang.String)
meth public void setHeader(java.lang.String,java.lang.String)
supr java.lang.Object
hfds headers
hcls MatchingIterator

CLSS public abstract interface javax.xml.soap.Name
meth public abstract java.lang.String getLocalName()
meth public abstract java.lang.String getPrefix()
meth public abstract java.lang.String getQualifiedName()
meth public abstract java.lang.String getURI()

CLSS public abstract interface javax.xml.soap.Node
intf org.w3c.dom.Node
meth public abstract java.lang.String getValue()
meth public abstract javax.xml.soap.SOAPElement getParentElement()
meth public abstract void detachNode()
meth public abstract void recycleNode()
meth public abstract void setParentElement(javax.xml.soap.SOAPElement) throws javax.xml.soap.SOAPException
meth public abstract void setValue(java.lang.String)

CLSS public abstract javax.xml.soap.SAAJMetaFactory
cons protected init()
meth protected abstract javax.xml.soap.MessageFactory newMessageFactory(java.lang.String) throws javax.xml.soap.SOAPException
meth protected abstract javax.xml.soap.SOAPFactory newSOAPFactory(java.lang.String) throws javax.xml.soap.SOAPException
supr java.lang.Object
hfds DEFAULT_META_FACTORY_CLASS,META_FACTORY_DEPRECATED_CLASS_PROPERTY

CLSS public javax.xml.soap.SAAJResult
cons public init() throws javax.xml.soap.SOAPException
cons public init(java.lang.String) throws javax.xml.soap.SOAPException
cons public init(javax.xml.soap.SOAPElement)
cons public init(javax.xml.soap.SOAPMessage)
meth public javax.xml.soap.Node getResult()
supr javax.xml.transform.dom.DOMResult

CLSS public abstract interface javax.xml.soap.SOAPBody
intf javax.xml.soap.SOAPElement
meth public abstract boolean hasFault()
meth public abstract javax.xml.soap.SOAPBodyElement addBodyElement(javax.xml.namespace.QName) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPBodyElement addBodyElement(javax.xml.soap.Name) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPBodyElement addDocument(org.w3c.dom.Document) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPFault addFault() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPFault addFault(javax.xml.namespace.QName,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPFault addFault(javax.xml.namespace.QName,java.lang.String,java.util.Locale) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPFault addFault(javax.xml.soap.Name,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPFault addFault(javax.xml.soap.Name,java.lang.String,java.util.Locale) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPFault getFault()
meth public abstract org.w3c.dom.Document extractContentAsDocument() throws javax.xml.soap.SOAPException

CLSS public abstract interface javax.xml.soap.SOAPBodyElement
intf javax.xml.soap.SOAPElement

CLSS public abstract javax.xml.soap.SOAPConnection
cons public init()
meth public abstract javax.xml.soap.SOAPMessage call(javax.xml.soap.SOAPMessage,java.lang.Object) throws javax.xml.soap.SOAPException
meth public abstract void close() throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPMessage get(java.lang.Object) throws javax.xml.soap.SOAPException
supr java.lang.Object

CLSS public abstract javax.xml.soap.SOAPConnectionFactory
cons public init()
meth public abstract javax.xml.soap.SOAPConnection createConnection() throws javax.xml.soap.SOAPException
meth public static javax.xml.soap.SOAPConnectionFactory newInstance() throws javax.xml.soap.SOAPException
supr java.lang.Object
hfds DEFAULT_SOAP_CONNECTION_FACTORY

CLSS public abstract interface javax.xml.soap.SOAPConstants
fld public final static java.lang.String DEFAULT_SOAP_PROTOCOL = "SOAP 1.1 Protocol"
fld public final static java.lang.String DYNAMIC_SOAP_PROTOCOL = "Dynamic Protocol"
fld public final static java.lang.String SOAP_1_1_CONTENT_TYPE = "text/xml"
fld public final static java.lang.String SOAP_1_1_PROTOCOL = "SOAP 1.1 Protocol"
fld public final static java.lang.String SOAP_1_2_CONTENT_TYPE = "application/soap+xml"
fld public final static java.lang.String SOAP_1_2_PROTOCOL = "SOAP 1.2 Protocol"
fld public final static java.lang.String SOAP_ENV_PREFIX = "env"
fld public final static java.lang.String URI_NS_SOAP_1_1_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/"
fld public final static java.lang.String URI_NS_SOAP_1_2_ENCODING = "http://www.w3.org/2003/05/soap-encoding"
fld public final static java.lang.String URI_NS_SOAP_1_2_ENVELOPE = "http://www.w3.org/2003/05/soap-envelope"
fld public final static java.lang.String URI_NS_SOAP_ENCODING = "http://schemas.xmlsoap.org/soap/encoding/"
fld public final static java.lang.String URI_NS_SOAP_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/"
fld public final static java.lang.String URI_SOAP_1_2_ROLE_NEXT = "http://www.w3.org/2003/05/soap-envelope/role/next"
fld public final static java.lang.String URI_SOAP_1_2_ROLE_NONE = "http://www.w3.org/2003/05/soap-envelope/role/none"
fld public final static java.lang.String URI_SOAP_1_2_ROLE_ULTIMATE_RECEIVER = "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver"
fld public final static java.lang.String URI_SOAP_ACTOR_NEXT = "http://schemas.xmlsoap.org/soap/actor/next"
fld public final static javax.xml.namespace.QName SOAP_DATAENCODINGUNKNOWN_FAULT
fld public final static javax.xml.namespace.QName SOAP_MUSTUNDERSTAND_FAULT
fld public final static javax.xml.namespace.QName SOAP_RECEIVER_FAULT
fld public final static javax.xml.namespace.QName SOAP_SENDER_FAULT
fld public final static javax.xml.namespace.QName SOAP_VERSIONMISMATCH_FAULT

CLSS public abstract interface javax.xml.soap.SOAPElement
intf javax.xml.soap.Node
intf org.w3c.dom.Element
meth public abstract boolean removeAttribute(javax.xml.namespace.QName)
meth public abstract boolean removeAttribute(javax.xml.soap.Name)
meth public abstract boolean removeNamespaceDeclaration(java.lang.String)
meth public abstract java.lang.String getAttributeValue(javax.xml.namespace.QName)
meth public abstract java.lang.String getAttributeValue(javax.xml.soap.Name)
meth public abstract java.lang.String getEncodingStyle()
meth public abstract java.lang.String getNamespaceURI(java.lang.String)
meth public abstract java.util.Iterator<java.lang.String> getNamespacePrefixes()
meth public abstract java.util.Iterator<java.lang.String> getVisibleNamespacePrefixes()
meth public abstract java.util.Iterator<javax.xml.namespace.QName> getAllAttributesAsQNames()
meth public abstract java.util.Iterator<javax.xml.soap.Name> getAllAttributes()
meth public abstract java.util.Iterator<javax.xml.soap.Node> getChildElements()
meth public abstract java.util.Iterator<javax.xml.soap.Node> getChildElements(javax.xml.namespace.QName)
meth public abstract java.util.Iterator<javax.xml.soap.Node> getChildElements(javax.xml.soap.Name)
meth public abstract javax.xml.namespace.QName createQName(java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.namespace.QName getElementQName()
meth public abstract javax.xml.soap.Name getElementName()
meth public abstract javax.xml.soap.SOAPElement addAttribute(javax.xml.namespace.QName,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addAttribute(javax.xml.soap.Name,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addChildElement(java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addChildElement(java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addChildElement(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addChildElement(javax.xml.namespace.QName) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addChildElement(javax.xml.soap.Name) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addChildElement(javax.xml.soap.SOAPElement) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addNamespaceDeclaration(java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement addTextNode(java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement setElementQName(javax.xml.namespace.QName) throws javax.xml.soap.SOAPException
meth public abstract void removeContents()
meth public abstract void setEncodingStyle(java.lang.String) throws javax.xml.soap.SOAPException

CLSS public javax.xml.soap.SOAPElementFactory
meth public javax.xml.soap.SOAPElement create(java.lang.String) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPElement create(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPElement create(javax.xml.soap.Name) throws javax.xml.soap.SOAPException
meth public static javax.xml.soap.SOAPElementFactory newInstance() throws javax.xml.soap.SOAPException
supr java.lang.Object
hfds soapFactory

CLSS public abstract interface javax.xml.soap.SOAPEnvelope
intf javax.xml.soap.SOAPElement
meth public abstract javax.xml.soap.Name createName(java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.Name createName(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPBody addBody() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPBody getBody() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPHeader addHeader() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPHeader getHeader() throws javax.xml.soap.SOAPException

CLSS public javax.xml.soap.SOAPException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public java.lang.String getMessage()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
supr java.lang.Exception
hfds cause

CLSS public abstract javax.xml.soap.SOAPFactory
cons public init()
meth public abstract javax.xml.soap.Detail createDetail() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.Name createName(java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.Name createName(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement createElement(java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement createElement(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPElement createElement(javax.xml.soap.Name) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPFault createFault() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPFault createFault(java.lang.String,javax.xml.namespace.QName) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPElement createElement(javax.xml.namespace.QName) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPElement createElement(org.w3c.dom.Element) throws javax.xml.soap.SOAPException
meth public static javax.xml.soap.SOAPFactory newInstance() throws javax.xml.soap.SOAPException
meth public static javax.xml.soap.SOAPFactory newInstance(java.lang.String) throws javax.xml.soap.SOAPException
supr java.lang.Object
hfds DEFAULT_SOAP_FACTORY

CLSS public abstract interface javax.xml.soap.SOAPFault
intf javax.xml.soap.SOAPBodyElement
meth public abstract boolean hasDetail()
meth public abstract java.lang.String getFaultActor()
meth public abstract java.lang.String getFaultCode()
meth public abstract java.lang.String getFaultNode()
meth public abstract java.lang.String getFaultReasonText(java.util.Locale) throws javax.xml.soap.SOAPException
meth public abstract java.lang.String getFaultRole()
meth public abstract java.lang.String getFaultString()
meth public abstract java.util.Iterator<java.lang.String> getFaultReasonTexts() throws javax.xml.soap.SOAPException
meth public abstract java.util.Iterator<java.util.Locale> getFaultReasonLocales() throws javax.xml.soap.SOAPException
meth public abstract java.util.Iterator<javax.xml.namespace.QName> getFaultSubcodes()
meth public abstract java.util.Locale getFaultStringLocale()
meth public abstract javax.xml.namespace.QName getFaultCodeAsQName()
meth public abstract javax.xml.soap.Detail addDetail() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.Detail getDetail()
meth public abstract javax.xml.soap.Name getFaultCodeAsName()
meth public abstract void addFaultReasonText(java.lang.String,java.util.Locale) throws javax.xml.soap.SOAPException
meth public abstract void appendFaultSubcode(javax.xml.namespace.QName) throws javax.xml.soap.SOAPException
meth public abstract void removeAllFaultSubcodes()
meth public abstract void setFaultActor(java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract void setFaultCode(java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract void setFaultCode(javax.xml.namespace.QName) throws javax.xml.soap.SOAPException
meth public abstract void setFaultCode(javax.xml.soap.Name) throws javax.xml.soap.SOAPException
meth public abstract void setFaultNode(java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract void setFaultRole(java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract void setFaultString(java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract void setFaultString(java.lang.String,java.util.Locale) throws javax.xml.soap.SOAPException

CLSS public abstract interface javax.xml.soap.SOAPFaultElement
intf javax.xml.soap.SOAPElement

CLSS public abstract interface javax.xml.soap.SOAPHeader
intf javax.xml.soap.SOAPElement
meth public abstract java.util.Iterator<javax.xml.soap.SOAPHeaderElement> examineAllHeaderElements()
meth public abstract java.util.Iterator<javax.xml.soap.SOAPHeaderElement> examineHeaderElements(java.lang.String)
meth public abstract java.util.Iterator<javax.xml.soap.SOAPHeaderElement> examineMustUnderstandHeaderElements(java.lang.String)
meth public abstract java.util.Iterator<javax.xml.soap.SOAPHeaderElement> extractAllHeaderElements()
meth public abstract java.util.Iterator<javax.xml.soap.SOAPHeaderElement> extractHeaderElements(java.lang.String)
meth public abstract javax.xml.soap.SOAPHeaderElement addHeaderElement(javax.xml.namespace.QName) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPHeaderElement addHeaderElement(javax.xml.soap.Name) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPHeaderElement addNotUnderstoodHeaderElement(javax.xml.namespace.QName) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPHeaderElement addUpgradeHeaderElement(java.lang.String) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPHeaderElement addUpgradeHeaderElement(java.lang.String[]) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.SOAPHeaderElement addUpgradeHeaderElement(java.util.Iterator<java.lang.String>) throws javax.xml.soap.SOAPException

CLSS public abstract interface javax.xml.soap.SOAPHeaderElement
intf javax.xml.soap.SOAPElement
meth public abstract boolean getMustUnderstand()
meth public abstract boolean getRelay()
meth public abstract java.lang.String getActor()
meth public abstract java.lang.String getRole()
meth public abstract void setActor(java.lang.String)
meth public abstract void setMustUnderstand(boolean)
meth public abstract void setRelay(boolean) throws javax.xml.soap.SOAPException
meth public abstract void setRole(java.lang.String) throws javax.xml.soap.SOAPException

CLSS public abstract javax.xml.soap.SOAPMessage
cons public init()
fld public final static java.lang.String CHARACTER_SET_ENCODING = "javax.xml.soap.character-set-encoding"
fld public final static java.lang.String WRITE_XML_DECLARATION = "javax.xml.soap.write-xml-declaration"
meth public abstract boolean saveRequired()
meth public abstract int countAttachments()
meth public abstract java.lang.String getContentDescription()
meth public abstract java.util.Iterator<javax.xml.soap.AttachmentPart> getAttachments()
meth public abstract java.util.Iterator<javax.xml.soap.AttachmentPart> getAttachments(javax.xml.soap.MimeHeaders)
meth public abstract javax.xml.soap.AttachmentPart createAttachmentPart()
meth public abstract javax.xml.soap.AttachmentPart getAttachment(javax.xml.soap.SOAPElement) throws javax.xml.soap.SOAPException
meth public abstract javax.xml.soap.MimeHeaders getMimeHeaders()
meth public abstract javax.xml.soap.SOAPPart getSOAPPart()
meth public abstract void addAttachmentPart(javax.xml.soap.AttachmentPart)
meth public abstract void removeAllAttachments()
meth public abstract void removeAttachments(javax.xml.soap.MimeHeaders)
meth public abstract void saveChanges() throws javax.xml.soap.SOAPException
meth public abstract void setContentDescription(java.lang.String)
meth public abstract void writeTo(java.io.OutputStream) throws java.io.IOException,javax.xml.soap.SOAPException
meth public java.lang.Object getProperty(java.lang.String) throws javax.xml.soap.SOAPException
meth public javax.xml.soap.AttachmentPart createAttachmentPart(java.lang.Object,java.lang.String)
meth public javax.xml.soap.AttachmentPart createAttachmentPart(javax.activation.DataHandler)
meth public javax.xml.soap.SOAPBody getSOAPBody() throws javax.xml.soap.SOAPException
meth public javax.xml.soap.SOAPHeader getSOAPHeader() throws javax.xml.soap.SOAPException
meth public void setProperty(java.lang.String,java.lang.Object) throws javax.xml.soap.SOAPException
supr java.lang.Object

CLSS public abstract javax.xml.soap.SOAPPart
cons public init()
intf javax.xml.soap.Node
intf org.w3c.dom.Document
meth public abstract java.lang.String[] getMimeHeader(java.lang.String)
meth public abstract java.util.Iterator<javax.xml.soap.MimeHeader> getAllMimeHeaders()
meth public abstract java.util.Iterator<javax.xml.soap.MimeHeader> getMatchingMimeHeaders(java.lang.String[])
meth public abstract java.util.Iterator<javax.xml.soap.MimeHeader> getNonMatchingMimeHeaders(java.lang.String[])
meth public abstract javax.xml.soap.SOAPEnvelope getEnvelope() throws javax.xml.soap.SOAPException
meth public abstract javax.xml.transform.Source getContent() throws javax.xml.soap.SOAPException
meth public abstract void addMimeHeader(java.lang.String,java.lang.String)
meth public abstract void removeAllMimeHeaders()
meth public abstract void removeMimeHeader(java.lang.String)
meth public abstract void setContent(javax.xml.transform.Source) throws javax.xml.soap.SOAPException
meth public abstract void setMimeHeader(java.lang.String,java.lang.String)
meth public java.lang.String getContentId()
meth public java.lang.String getContentLocation()
meth public void setContentId(java.lang.String)
meth public void setContentLocation(java.lang.String)
supr java.lang.Object

CLSS public abstract interface javax.xml.soap.Text
intf javax.xml.soap.Node
intf org.w3c.dom.Text
meth public abstract boolean isComment()

CLSS public abstract interface javax.xml.transform.Result
fld public final static java.lang.String PI_DISABLE_OUTPUT_ESCAPING = "javax.xml.transform.disable-output-escaping"
fld public final static java.lang.String PI_ENABLE_OUTPUT_ESCAPING = "javax.xml.transform.enable-output-escaping"
meth public abstract java.lang.String getSystemId()
meth public abstract void setSystemId(java.lang.String)

CLSS public javax.xml.transform.dom.DOMResult
cons public init()
cons public init(org.w3c.dom.Node)
cons public init(org.w3c.dom.Node,java.lang.String)
cons public init(org.w3c.dom.Node,org.w3c.dom.Node)
cons public init(org.w3c.dom.Node,org.w3c.dom.Node,java.lang.String)
fld public final static java.lang.String FEATURE = "http://javax.xml.transform.dom.DOMResult/feature"
intf javax.xml.transform.Result
meth public java.lang.String getSystemId()
meth public org.w3c.dom.Node getNextSibling()
meth public org.w3c.dom.Node getNode()
meth public void setNextSibling(org.w3c.dom.Node)
meth public void setNode(org.w3c.dom.Node)
meth public void setSystemId(java.lang.String)
supr java.lang.Object

CLSS public abstract interface !annotation javax.xml.ws.Action
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String input()
meth public abstract !hasdefault java.lang.String output()
meth public abstract !hasdefault javax.xml.ws.FaultAction[] fault()

CLSS public abstract interface javax.xml.ws.AsyncHandler<%0 extends java.lang.Object>
meth public abstract void handleResponse(javax.xml.ws.Response<{javax.xml.ws.AsyncHandler%0}>)

CLSS public abstract interface javax.xml.ws.Binding
meth public abstract java.lang.String getBindingID()
meth public abstract java.util.List<javax.xml.ws.handler.Handler> getHandlerChain()
meth public abstract void setHandlerChain(java.util.List<javax.xml.ws.handler.Handler>)

CLSS public abstract interface javax.xml.ws.BindingProvider
fld public final static java.lang.String ENDPOINT_ADDRESS_PROPERTY = "javax.xml.ws.service.endpoint.address"
fld public final static java.lang.String PASSWORD_PROPERTY = "javax.xml.ws.security.auth.password"
fld public final static java.lang.String SESSION_MAINTAIN_PROPERTY = "javax.xml.ws.session.maintain"
fld public final static java.lang.String SOAPACTION_URI_PROPERTY = "javax.xml.ws.soap.http.soapaction.uri"
fld public final static java.lang.String SOAPACTION_USE_PROPERTY = "javax.xml.ws.soap.http.soapaction.use"
fld public final static java.lang.String USERNAME_PROPERTY = "javax.xml.ws.security.auth.username"
meth public abstract <%0 extends javax.xml.ws.EndpointReference> {%%0} getEndpointReference(java.lang.Class<{%%0}>)
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getRequestContext()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getResponseContext()
meth public abstract javax.xml.ws.Binding getBinding()
meth public abstract javax.xml.ws.EndpointReference getEndpointReference()

CLSS public abstract interface !annotation javax.xml.ws.BindingType
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface javax.xml.ws.Dispatch<%0 extends java.lang.Object>
intf javax.xml.ws.BindingProvider
meth public abstract java.util.concurrent.Future<?> invokeAsync({javax.xml.ws.Dispatch%0},javax.xml.ws.AsyncHandler<{javax.xml.ws.Dispatch%0}>)
meth public abstract javax.xml.ws.Response<{javax.xml.ws.Dispatch%0}> invokeAsync({javax.xml.ws.Dispatch%0})
meth public abstract void invokeOneWay({javax.xml.ws.Dispatch%0})
meth public abstract {javax.xml.ws.Dispatch%0} invoke({javax.xml.ws.Dispatch%0})

CLSS public abstract javax.xml.ws.Endpoint
cons public init()
fld public final static java.lang.String WSDL_PORT = "javax.xml.ws.wsdl.port"
fld public final static java.lang.String WSDL_SERVICE = "javax.xml.ws.wsdl.service"
meth public !varargs static javax.xml.ws.Endpoint create(java.lang.Object,javax.xml.ws.WebServiceFeature[])
meth public !varargs static javax.xml.ws.Endpoint create(java.lang.String,java.lang.Object,javax.xml.ws.WebServiceFeature[])
meth public !varargs static javax.xml.ws.Endpoint publish(java.lang.String,java.lang.Object,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs <%0 extends javax.xml.ws.EndpointReference> {%%0} getEndpointReference(java.lang.Class<{%%0}>,org.w3c.dom.Element[])
meth public abstract !varargs javax.xml.ws.EndpointReference getEndpointReference(org.w3c.dom.Element[])
meth public abstract boolean isPublished()
meth public abstract java.lang.Object getImplementor()
meth public abstract java.util.List<javax.xml.transform.Source> getMetadata()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public abstract java.util.concurrent.Executor getExecutor()
meth public abstract javax.xml.ws.Binding getBinding()
meth public abstract void publish(java.lang.Object)
meth public abstract void publish(java.lang.String)
meth public abstract void setExecutor(java.util.concurrent.Executor)
meth public abstract void setMetadata(java.util.List<javax.xml.transform.Source>)
meth public abstract void setProperties(java.util.Map<java.lang.String,java.lang.Object>)
meth public abstract void stop()
meth public static javax.xml.ws.Endpoint create(java.lang.Object)
meth public static javax.xml.ws.Endpoint create(java.lang.String,java.lang.Object)
meth public static javax.xml.ws.Endpoint publish(java.lang.String,java.lang.Object)
meth public void publish(javax.xml.ws.spi.http.HttpContext)
meth public void setEndpointContext(javax.xml.ws.EndpointContext)
supr java.lang.Object

CLSS public abstract javax.xml.ws.EndpointContext
cons public init()
meth public abstract java.util.Set<javax.xml.ws.Endpoint> getEndpoints()
supr java.lang.Object

CLSS public abstract javax.xml.ws.EndpointReference
cons protected init()
meth public !varargs <%0 extends java.lang.Object> {%%0} getPort(java.lang.Class<{%%0}>,javax.xml.ws.WebServiceFeature[])
meth public abstract void writeTo(javax.xml.transform.Result)
meth public java.lang.String toString()
meth public static javax.xml.ws.EndpointReference readFrom(javax.xml.transform.Source)
supr java.lang.Object

CLSS public abstract interface !annotation javax.xml.ws.FaultAction
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String value()
meth public abstract java.lang.Class<? extends java.lang.Exception> className()

CLSS public final javax.xml.ws.Holder<%0 extends java.lang.Object>
cons public init()
cons public init({javax.xml.ws.Holder%0})
fld public {javax.xml.ws.Holder%0} value
intf java.io.Serializable
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract interface javax.xml.ws.LogicalMessage
meth public abstract java.lang.Object getPayload(javax.xml.bind.JAXBContext)
meth public abstract javax.xml.transform.Source getPayload()
meth public abstract void setPayload(java.lang.Object,javax.xml.bind.JAXBContext)
meth public abstract void setPayload(javax.xml.transform.Source)

CLSS public javax.xml.ws.ProtocolException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr javax.xml.ws.WebServiceException

CLSS public abstract interface javax.xml.ws.Provider<%0 extends java.lang.Object>
meth public abstract {javax.xml.ws.Provider%0} invoke({javax.xml.ws.Provider%0})

CLSS public abstract interface !annotation javax.xml.ws.RequestWrapper
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String className()
meth public abstract !hasdefault java.lang.String localName()
meth public abstract !hasdefault java.lang.String partName()
meth public abstract !hasdefault java.lang.String targetNamespace()

CLSS public abstract interface !annotation javax.xml.ws.RespectBinding
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
 anno 0 javax.xml.ws.spi.WebServiceFeatureAnnotation(java.lang.Class<? extends javax.xml.ws.WebServiceFeature> bean=class javax.xml.ws.RespectBindingFeature, java.lang.String id="javax.xml.ws.RespectBindingFeature")
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean enabled()

CLSS public final javax.xml.ws.RespectBindingFeature
cons public init()
cons public init(boolean)
fld public final static java.lang.String ID = "javax.xml.ws.RespectBindingFeature"
meth public java.lang.String getID()
supr javax.xml.ws.WebServiceFeature

CLSS public abstract interface javax.xml.ws.Response<%0 extends java.lang.Object>
intf java.util.concurrent.Future<{javax.xml.ws.Response%0}>
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getContext()

CLSS public abstract interface !annotation javax.xml.ws.ResponseWrapper
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String className()
meth public abstract !hasdefault java.lang.String localName()
meth public abstract !hasdefault java.lang.String partName()
meth public abstract !hasdefault java.lang.String targetNamespace()

CLSS public javax.xml.ws.Service
cons protected !varargs init(java.net.URL,javax.xml.namespace.QName,javax.xml.ws.WebServiceFeature[])
cons protected init(java.net.URL,javax.xml.namespace.QName)
innr public final static !enum Mode
meth public !varargs <%0 extends java.lang.Object> javax.xml.ws.Dispatch<{%%0}> createDispatch(javax.xml.namespace.QName,java.lang.Class<{%%0}>,javax.xml.ws.Service$Mode,javax.xml.ws.WebServiceFeature[])
meth public !varargs <%0 extends java.lang.Object> javax.xml.ws.Dispatch<{%%0}> createDispatch(javax.xml.ws.EndpointReference,java.lang.Class<{%%0}>,javax.xml.ws.Service$Mode,javax.xml.ws.WebServiceFeature[])
meth public !varargs <%0 extends java.lang.Object> {%%0} getPort(java.lang.Class<{%%0}>,javax.xml.ws.WebServiceFeature[])
meth public !varargs <%0 extends java.lang.Object> {%%0} getPort(javax.xml.namespace.QName,java.lang.Class<{%%0}>,javax.xml.ws.WebServiceFeature[])
meth public !varargs <%0 extends java.lang.Object> {%%0} getPort(javax.xml.ws.EndpointReference,java.lang.Class<{%%0}>,javax.xml.ws.WebServiceFeature[])
meth public !varargs javax.xml.ws.Dispatch<java.lang.Object> createDispatch(javax.xml.namespace.QName,javax.xml.bind.JAXBContext,javax.xml.ws.Service$Mode,javax.xml.ws.WebServiceFeature[])
meth public !varargs javax.xml.ws.Dispatch<java.lang.Object> createDispatch(javax.xml.ws.EndpointReference,javax.xml.bind.JAXBContext,javax.xml.ws.Service$Mode,javax.xml.ws.WebServiceFeature[])
meth public !varargs static javax.xml.ws.Service create(java.net.URL,javax.xml.namespace.QName,javax.xml.ws.WebServiceFeature[])
meth public !varargs static javax.xml.ws.Service create(javax.xml.namespace.QName,javax.xml.ws.WebServiceFeature[])
meth public <%0 extends java.lang.Object> javax.xml.ws.Dispatch<{%%0}> createDispatch(javax.xml.namespace.QName,java.lang.Class<{%%0}>,javax.xml.ws.Service$Mode)
meth public <%0 extends java.lang.Object> {%%0} getPort(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} getPort(javax.xml.namespace.QName,java.lang.Class<{%%0}>)
meth public java.net.URL getWSDLDocumentLocation()
meth public java.util.Iterator<javax.xml.namespace.QName> getPorts()
meth public java.util.concurrent.Executor getExecutor()
meth public javax.xml.namespace.QName getServiceName()
meth public javax.xml.ws.Dispatch<java.lang.Object> createDispatch(javax.xml.namespace.QName,javax.xml.bind.JAXBContext,javax.xml.ws.Service$Mode)
meth public javax.xml.ws.handler.HandlerResolver getHandlerResolver()
meth public static javax.xml.ws.Service create(java.net.URL,javax.xml.namespace.QName)
meth public static javax.xml.ws.Service create(javax.xml.namespace.QName)
meth public void addPort(javax.xml.namespace.QName,java.lang.String,java.lang.String)
meth public void setExecutor(java.util.concurrent.Executor)
meth public void setHandlerResolver(javax.xml.ws.handler.HandlerResolver)
supr java.lang.Object
hfds delegate

CLSS public final static !enum javax.xml.ws.Service$Mode
 outer javax.xml.ws.Service
fld public final static javax.xml.ws.Service$Mode MESSAGE
fld public final static javax.xml.ws.Service$Mode PAYLOAD
meth public static javax.xml.ws.Service$Mode valueOf(java.lang.String)
meth public static javax.xml.ws.Service$Mode[] values()
supr java.lang.Enum<javax.xml.ws.Service$Mode>

CLSS public abstract interface !annotation javax.xml.ws.ServiceMode
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Inherited()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.xml.ws.Service$Mode value()

CLSS public abstract interface !annotation javax.xml.ws.WebEndpoint
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String name()

CLSS public abstract interface !annotation javax.xml.ws.WebFault
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String faultBean()
meth public abstract !hasdefault java.lang.String messageName()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String targetNamespace()

CLSS public abstract interface !annotation javax.xml.ws.WebServiceClient
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String targetNamespace()
meth public abstract !hasdefault java.lang.String wsdlLocation()

CLSS public abstract interface javax.xml.ws.WebServiceContext
meth public abstract !varargs <%0 extends javax.xml.ws.EndpointReference> {%%0} getEndpointReference(java.lang.Class<{%%0}>,org.w3c.dom.Element[])
meth public abstract !varargs javax.xml.ws.EndpointReference getEndpointReference(org.w3c.dom.Element[])
meth public abstract boolean isUserInRole(java.lang.String)
meth public abstract java.security.Principal getUserPrincipal()
meth public abstract javax.xml.ws.handler.MessageContext getMessageContext()

CLSS public javax.xml.ws.WebServiceException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public abstract javax.xml.ws.WebServiceFeature
cons protected init()
fld protected boolean enabled
meth public abstract java.lang.String getID()
meth public boolean isEnabled()
supr java.lang.Object

CLSS public final javax.xml.ws.WebServicePermission
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
supr java.security.BasicPermission
hfds serialVersionUID

CLSS public abstract interface !annotation javax.xml.ws.WebServiceProvider
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String portName()
meth public abstract !hasdefault java.lang.String serviceName()
meth public abstract !hasdefault java.lang.String targetNamespace()
meth public abstract !hasdefault java.lang.String wsdlLocation()

CLSS public abstract interface !annotation javax.xml.ws.WebServiceRef
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Repeatable(java.lang.Class<? extends java.lang.annotation.Annotation> value=class javax.xml.ws.WebServiceRefs)
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends javax.xml.ws.Service> value()
meth public abstract !hasdefault java.lang.Class<?> type()
meth public abstract !hasdefault java.lang.String lookup()
meth public abstract !hasdefault java.lang.String mappedName()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String wsdlLocation()

CLSS public abstract interface !annotation javax.xml.ws.WebServiceRefs
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract javax.xml.ws.WebServiceRef[] value()

CLSS public abstract interface javax.xml.ws.handler.Handler<%0 extends javax.xml.ws.handler.MessageContext>
meth public abstract boolean handleFault({javax.xml.ws.handler.Handler%0})
meth public abstract boolean handleMessage({javax.xml.ws.handler.Handler%0})
meth public abstract void close(javax.xml.ws.handler.MessageContext)

CLSS public abstract interface javax.xml.ws.handler.HandlerResolver
meth public abstract java.util.List<javax.xml.ws.handler.Handler> getHandlerChain(javax.xml.ws.handler.PortInfo)

CLSS public abstract interface javax.xml.ws.handler.LogicalHandler<%0 extends javax.xml.ws.handler.LogicalMessageContext>
intf javax.xml.ws.handler.Handler<{javax.xml.ws.handler.LogicalHandler%0}>

CLSS public abstract interface javax.xml.ws.handler.LogicalMessageContext
intf javax.xml.ws.handler.MessageContext
meth public abstract javax.xml.ws.LogicalMessage getMessage()

CLSS public abstract interface javax.xml.ws.handler.MessageContext
fld public final static java.lang.String HTTP_REQUEST_HEADERS = "javax.xml.ws.http.request.headers"
fld public final static java.lang.String HTTP_REQUEST_METHOD = "javax.xml.ws.http.request.method"
fld public final static java.lang.String HTTP_RESPONSE_CODE = "javax.xml.ws.http.response.code"
fld public final static java.lang.String HTTP_RESPONSE_HEADERS = "javax.xml.ws.http.response.headers"
fld public final static java.lang.String INBOUND_MESSAGE_ATTACHMENTS = "javax.xml.ws.binding.attachments.inbound"
fld public final static java.lang.String MESSAGE_OUTBOUND_PROPERTY = "javax.xml.ws.handler.message.outbound"
fld public final static java.lang.String OUTBOUND_MESSAGE_ATTACHMENTS = "javax.xml.ws.binding.attachments.outbound"
fld public final static java.lang.String PATH_INFO = "javax.xml.ws.http.request.pathinfo"
fld public final static java.lang.String QUERY_STRING = "javax.xml.ws.http.request.querystring"
fld public final static java.lang.String REFERENCE_PARAMETERS = "javax.xml.ws.reference.parameters"
fld public final static java.lang.String SERVLET_CONTEXT = "javax.xml.ws.servlet.context"
fld public final static java.lang.String SERVLET_REQUEST = "javax.xml.ws.servlet.request"
fld public final static java.lang.String SERVLET_RESPONSE = "javax.xml.ws.servlet.response"
fld public final static java.lang.String WSDL_DESCRIPTION = "javax.xml.ws.wsdl.description"
fld public final static java.lang.String WSDL_INTERFACE = "javax.xml.ws.wsdl.interface"
fld public final static java.lang.String WSDL_OPERATION = "javax.xml.ws.wsdl.operation"
fld public final static java.lang.String WSDL_PORT = "javax.xml.ws.wsdl.port"
fld public final static java.lang.String WSDL_SERVICE = "javax.xml.ws.wsdl.service"
innr public final static !enum Scope
intf java.util.Map<java.lang.String,java.lang.Object>
meth public abstract javax.xml.ws.handler.MessageContext$Scope getScope(java.lang.String)
meth public abstract void setScope(java.lang.String,javax.xml.ws.handler.MessageContext$Scope)

CLSS public final static !enum javax.xml.ws.handler.MessageContext$Scope
 outer javax.xml.ws.handler.MessageContext
fld public final static javax.xml.ws.handler.MessageContext$Scope APPLICATION
fld public final static javax.xml.ws.handler.MessageContext$Scope HANDLER
meth public static javax.xml.ws.handler.MessageContext$Scope valueOf(java.lang.String)
meth public static javax.xml.ws.handler.MessageContext$Scope[] values()
supr java.lang.Enum<javax.xml.ws.handler.MessageContext$Scope>

CLSS public abstract interface javax.xml.ws.handler.PortInfo
meth public abstract java.lang.String getBindingID()
meth public abstract javax.xml.namespace.QName getPortName()
meth public abstract javax.xml.namespace.QName getServiceName()

CLSS public abstract interface javax.xml.ws.handler.soap.SOAPHandler<%0 extends javax.xml.ws.handler.soap.SOAPMessageContext>
intf javax.xml.ws.handler.Handler<{javax.xml.ws.handler.soap.SOAPHandler%0}>
meth public abstract java.util.Set<javax.xml.namespace.QName> getHeaders()

CLSS public abstract interface javax.xml.ws.handler.soap.SOAPMessageContext
intf javax.xml.ws.handler.MessageContext
meth public abstract java.lang.Object[] getHeaders(javax.xml.namespace.QName,javax.xml.bind.JAXBContext,boolean)
meth public abstract java.util.Set<java.lang.String> getRoles()
meth public abstract javax.xml.soap.SOAPMessage getMessage()
meth public abstract void setMessage(javax.xml.soap.SOAPMessage)

CLSS public abstract interface javax.xml.ws.http.HTTPBinding
fld public final static java.lang.String HTTP_BINDING = "http://www.w3.org/2004/08/wsdl/http"
intf javax.xml.ws.Binding

CLSS public javax.xml.ws.http.HTTPException
cons public init(int)
meth public int getStatusCode()
supr javax.xml.ws.ProtocolException
hfds statusCode

CLSS public abstract interface !annotation javax.xml.ws.soap.Addressing
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
 anno 0 javax.xml.ws.spi.WebServiceFeatureAnnotation(java.lang.Class<? extends javax.xml.ws.WebServiceFeature> bean=class javax.xml.ws.soap.AddressingFeature, java.lang.String id="http://www.w3.org/2005/08/addressing/module")
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean required()
meth public abstract !hasdefault javax.xml.ws.soap.AddressingFeature$Responses responses()

CLSS public final javax.xml.ws.soap.AddressingFeature
cons public init()
cons public init(boolean)
cons public init(boolean,boolean)
cons public init(boolean,boolean,javax.xml.ws.soap.AddressingFeature$Responses)
fld protected boolean required
fld public final static java.lang.String ID = "http://www.w3.org/2005/08/addressing/module"
innr public final static !enum Responses
meth public boolean isRequired()
meth public java.lang.String getID()
meth public javax.xml.ws.soap.AddressingFeature$Responses getResponses()
supr javax.xml.ws.WebServiceFeature
hfds responses

CLSS public final static !enum javax.xml.ws.soap.AddressingFeature$Responses
 outer javax.xml.ws.soap.AddressingFeature
fld public final static javax.xml.ws.soap.AddressingFeature$Responses ALL
fld public final static javax.xml.ws.soap.AddressingFeature$Responses ANONYMOUS
fld public final static javax.xml.ws.soap.AddressingFeature$Responses NON_ANONYMOUS
meth public static javax.xml.ws.soap.AddressingFeature$Responses valueOf(java.lang.String)
meth public static javax.xml.ws.soap.AddressingFeature$Responses[] values()
supr java.lang.Enum<javax.xml.ws.soap.AddressingFeature$Responses>

CLSS public abstract interface !annotation javax.xml.ws.soap.MTOM
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, FIELD])
 anno 0 javax.xml.ws.spi.WebServiceFeatureAnnotation(java.lang.Class<? extends javax.xml.ws.WebServiceFeature> bean=class javax.xml.ws.soap.MTOMFeature, java.lang.String id="http://www.w3.org/2004/08/soap/features/http-optimization")
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault int threshold()

CLSS public final javax.xml.ws.soap.MTOMFeature
cons public init()
cons public init(boolean)
cons public init(boolean,int)
cons public init(int)
fld protected int threshold
fld public final static java.lang.String ID = "http://www.w3.org/2004/08/soap/features/http-optimization"
meth public int getThreshold()
meth public java.lang.String getID()
supr javax.xml.ws.WebServiceFeature

CLSS public abstract interface javax.xml.ws.soap.SOAPBinding
fld public final static java.lang.String SOAP11HTTP_BINDING = "http://schemas.xmlsoap.org/wsdl/soap/http"
fld public final static java.lang.String SOAP11HTTP_MTOM_BINDING = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true"
fld public final static java.lang.String SOAP12HTTP_BINDING = "http://www.w3.org/2003/05/soap/bindings/HTTP/"
fld public final static java.lang.String SOAP12HTTP_MTOM_BINDING = "http://www.w3.org/2003/05/soap/bindings/HTTP/?mtom=true"
intf javax.xml.ws.Binding
meth public abstract boolean isMTOMEnabled()
meth public abstract java.util.Set<java.lang.String> getRoles()
meth public abstract javax.xml.soap.MessageFactory getMessageFactory()
meth public abstract javax.xml.soap.SOAPFactory getSOAPFactory()
meth public abstract void setMTOMEnabled(boolean)
meth public abstract void setRoles(java.util.Set<java.lang.String>)

CLSS public javax.xml.ws.soap.SOAPFaultException
cons public init(javax.xml.soap.SOAPFault)
meth public javax.xml.soap.SOAPFault getFault()
supr javax.xml.ws.ProtocolException
hfds fault

CLSS public abstract javax.xml.ws.spi.Invoker
cons public init()
meth public abstract !varargs java.lang.Object invoke(java.lang.reflect.Method,java.lang.Object[]) throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
meth public abstract void inject(javax.xml.ws.WebServiceContext) throws java.lang.IllegalAccessException,java.lang.reflect.InvocationTargetException
supr java.lang.Object

CLSS public abstract javax.xml.ws.spi.Provider
cons protected init()
meth public !varargs javax.xml.ws.Endpoint createAndPublishEndpoint(java.lang.String,java.lang.Object,javax.xml.ws.WebServiceFeature[])
meth public !varargs javax.xml.ws.Endpoint createEndpoint(java.lang.String,java.lang.Class<?>,javax.xml.ws.spi.Invoker,javax.xml.ws.WebServiceFeature[])
meth public !varargs javax.xml.ws.Endpoint createEndpoint(java.lang.String,java.lang.Object,javax.xml.ws.WebServiceFeature[])
meth public !varargs javax.xml.ws.spi.ServiceDelegate createServiceDelegate(java.net.URL,javax.xml.namespace.QName,java.lang.Class<? extends javax.xml.ws.Service>,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs <%0 extends java.lang.Object> {%%0} getPort(javax.xml.ws.EndpointReference,java.lang.Class<{%%0}>,javax.xml.ws.WebServiceFeature[])
meth public abstract javax.xml.ws.Endpoint createAndPublishEndpoint(java.lang.String,java.lang.Object)
meth public abstract javax.xml.ws.Endpoint createEndpoint(java.lang.String,java.lang.Object)
meth public abstract javax.xml.ws.EndpointReference readEndpointReference(javax.xml.transform.Source)
meth public abstract javax.xml.ws.spi.ServiceDelegate createServiceDelegate(java.net.URL,javax.xml.namespace.QName,java.lang.Class<? extends javax.xml.ws.Service>)
meth public abstract javax.xml.ws.wsaddressing.W3CEndpointReference createW3CEndpointReference(java.lang.String,javax.xml.namespace.QName,javax.xml.namespace.QName,java.util.List<org.w3c.dom.Element>,java.lang.String,java.util.List<org.w3c.dom.Element>)
meth public javax.xml.ws.wsaddressing.W3CEndpointReference createW3CEndpointReference(java.lang.String,javax.xml.namespace.QName,javax.xml.namespace.QName,javax.xml.namespace.QName,java.util.List<org.w3c.dom.Element>,java.lang.String,java.util.List<org.w3c.dom.Element>,java.util.List<org.w3c.dom.Element>,java.util.Map<javax.xml.namespace.QName,java.lang.String>)
meth public static javax.xml.ws.spi.Provider provider()
supr java.lang.Object
hfds DEFAULT_JAXWSPROVIDER

CLSS public abstract javax.xml.ws.spi.ServiceDelegate
cons protected init()
meth public abstract !varargs <%0 extends java.lang.Object> javax.xml.ws.Dispatch<{%%0}> createDispatch(javax.xml.namespace.QName,java.lang.Class<{%%0}>,javax.xml.ws.Service$Mode,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs <%0 extends java.lang.Object> javax.xml.ws.Dispatch<{%%0}> createDispatch(javax.xml.ws.EndpointReference,java.lang.Class<{%%0}>,javax.xml.ws.Service$Mode,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs <%0 extends java.lang.Object> {%%0} getPort(java.lang.Class<{%%0}>,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs <%0 extends java.lang.Object> {%%0} getPort(javax.xml.namespace.QName,java.lang.Class<{%%0}>,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs <%0 extends java.lang.Object> {%%0} getPort(javax.xml.ws.EndpointReference,java.lang.Class<{%%0}>,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs javax.xml.ws.Dispatch<java.lang.Object> createDispatch(javax.xml.namespace.QName,javax.xml.bind.JAXBContext,javax.xml.ws.Service$Mode,javax.xml.ws.WebServiceFeature[])
meth public abstract !varargs javax.xml.ws.Dispatch<java.lang.Object> createDispatch(javax.xml.ws.EndpointReference,javax.xml.bind.JAXBContext,javax.xml.ws.Service$Mode,javax.xml.ws.WebServiceFeature[])
meth public abstract <%0 extends java.lang.Object> javax.xml.ws.Dispatch<{%%0}> createDispatch(javax.xml.namespace.QName,java.lang.Class<{%%0}>,javax.xml.ws.Service$Mode)
meth public abstract <%0 extends java.lang.Object> {%%0} getPort(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} getPort(javax.xml.namespace.QName,java.lang.Class<{%%0}>)
meth public abstract java.net.URL getWSDLDocumentLocation()
meth public abstract java.util.Iterator<javax.xml.namespace.QName> getPorts()
meth public abstract java.util.concurrent.Executor getExecutor()
meth public abstract javax.xml.namespace.QName getServiceName()
meth public abstract javax.xml.ws.Dispatch<java.lang.Object> createDispatch(javax.xml.namespace.QName,javax.xml.bind.JAXBContext,javax.xml.ws.Service$Mode)
meth public abstract javax.xml.ws.handler.HandlerResolver getHandlerResolver()
meth public abstract void addPort(javax.xml.namespace.QName,java.lang.String,java.lang.String)
meth public abstract void setExecutor(java.util.concurrent.Executor)
meth public abstract void setHandlerResolver(javax.xml.ws.handler.HandlerResolver)
supr java.lang.Object

CLSS public abstract interface !annotation javax.xml.ws.spi.WebServiceFeatureAnnotation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<? extends javax.xml.ws.WebServiceFeature> bean()
meth public abstract java.lang.String id()

CLSS public abstract javax.xml.ws.spi.http.HttpContext
cons public init()
fld protected javax.xml.ws.spi.http.HttpHandler handler
meth public abstract java.lang.Object getAttribute(java.lang.String)
meth public abstract java.lang.String getPath()
meth public abstract java.util.Set<java.lang.String> getAttributeNames()
meth public void setHandler(javax.xml.ws.spi.http.HttpHandler)
supr java.lang.Object

CLSS public abstract javax.xml.ws.spi.http.HttpExchange
cons public init()
fld public final static java.lang.String REQUEST_CIPHER_SUITE = "javax.xml.ws.spi.http.request.cipher.suite"
fld public final static java.lang.String REQUEST_KEY_SIZE = "javax.xml.ws.spi.http.request.key.size"
fld public final static java.lang.String REQUEST_X509CERTIFICATE = "javax.xml.ws.spi.http.request.cert.X509Certificate"
meth public abstract boolean isUserInRole(java.lang.String)
meth public abstract java.io.InputStream getRequestBody() throws java.io.IOException
meth public abstract java.io.OutputStream getResponseBody() throws java.io.IOException
meth public abstract java.lang.Object getAttribute(java.lang.String)
meth public abstract java.lang.String getContextPath()
meth public abstract java.lang.String getPathInfo()
meth public abstract java.lang.String getProtocol()
meth public abstract java.lang.String getQueryString()
meth public abstract java.lang.String getRequestHeader(java.lang.String)
meth public abstract java.lang.String getRequestMethod()
meth public abstract java.lang.String getRequestURI()
meth public abstract java.lang.String getScheme()
meth public abstract java.net.InetSocketAddress getLocalAddress()
meth public abstract java.net.InetSocketAddress getRemoteAddress()
meth public abstract java.security.Principal getUserPrincipal()
meth public abstract java.util.Map<java.lang.String,java.util.List<java.lang.String>> getRequestHeaders()
meth public abstract java.util.Map<java.lang.String,java.util.List<java.lang.String>> getResponseHeaders()
meth public abstract java.util.Set<java.lang.String> getAttributeNames()
meth public abstract javax.xml.ws.spi.http.HttpContext getHttpContext()
meth public abstract void addResponseHeader(java.lang.String,java.lang.String)
meth public abstract void close() throws java.io.IOException
meth public abstract void setStatus(int)
supr java.lang.Object

CLSS public abstract javax.xml.ws.spi.http.HttpHandler
cons public init()
meth public abstract void handle(javax.xml.ws.spi.http.HttpExchange) throws java.io.IOException
supr java.lang.Object

CLSS public final javax.xml.ws.wsaddressing.W3CEndpointReference
cons protected init()
cons public init(javax.xml.transform.Source)
fld protected final static java.lang.String NS = "http://www.w3.org/2005/08/addressing"
meth public void writeTo(javax.xml.transform.Result)
supr javax.xml.ws.EndpointReference
hfds address,attributes,elements,metadata,referenceParameters,w3cjc
hcls Address,Elements

CLSS public final javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder
cons public init()
meth public javax.xml.ws.wsaddressing.W3CEndpointReference build()
meth public javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder address(java.lang.String)
meth public javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder attribute(javax.xml.namespace.QName,java.lang.String)
meth public javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder element(org.w3c.dom.Element)
meth public javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder endpointName(javax.xml.namespace.QName)
meth public javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder interfaceName(javax.xml.namespace.QName)
meth public javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder metadata(org.w3c.dom.Element)
meth public javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder referenceParameter(org.w3c.dom.Element)
meth public javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder serviceName(javax.xml.namespace.QName)
meth public javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder wsdlDocumentLocation(java.lang.String)
supr java.lang.Object
hfds address,attributes,elements,endpointName,interfaceName,metadata,referenceParameters,serviceName,wsdlDocumentLocation

CLSS abstract interface javax.xml.ws.wsaddressing.package-info

CLSS public abstract interface org.w3c.dom.CharacterData
intf org.w3c.dom.Node
meth public abstract int getLength()
meth public abstract java.lang.String getData()
meth public abstract java.lang.String substringData(int,int)
meth public abstract void appendData(java.lang.String)
meth public abstract void deleteData(int,int)
meth public abstract void insertData(int,java.lang.String)
meth public abstract void replaceData(int,int,java.lang.String)
meth public abstract void setData(java.lang.String)

CLSS public abstract interface org.w3c.dom.Document
intf org.w3c.dom.Node
meth public abstract boolean getStrictErrorChecking()
meth public abstract boolean getXmlStandalone()
meth public abstract java.lang.String getDocumentURI()
meth public abstract java.lang.String getInputEncoding()
meth public abstract java.lang.String getXmlEncoding()
meth public abstract java.lang.String getXmlVersion()
meth public abstract org.w3c.dom.Attr createAttribute(java.lang.String)
meth public abstract org.w3c.dom.Attr createAttributeNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.CDATASection createCDATASection(java.lang.String)
meth public abstract org.w3c.dom.Comment createComment(java.lang.String)
meth public abstract org.w3c.dom.DOMConfiguration getDomConfig()
meth public abstract org.w3c.dom.DOMImplementation getImplementation()
meth public abstract org.w3c.dom.DocumentFragment createDocumentFragment()
meth public abstract org.w3c.dom.DocumentType getDoctype()
meth public abstract org.w3c.dom.Element createElement(java.lang.String)
meth public abstract org.w3c.dom.Element createElementNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.Element getDocumentElement()
meth public abstract org.w3c.dom.Element getElementById(java.lang.String)
meth public abstract org.w3c.dom.EntityReference createEntityReference(java.lang.String)
meth public abstract org.w3c.dom.Node adoptNode(org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node importNode(org.w3c.dom.Node,boolean)
meth public abstract org.w3c.dom.Node renameNode(org.w3c.dom.Node,java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public abstract org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.ProcessingInstruction createProcessingInstruction(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.Text createTextNode(java.lang.String)
meth public abstract void normalizeDocument()
meth public abstract void setDocumentURI(java.lang.String)
meth public abstract void setStrictErrorChecking(boolean)
meth public abstract void setXmlStandalone(boolean)
meth public abstract void setXmlVersion(java.lang.String)

CLSS public abstract interface org.w3c.dom.Element
intf org.w3c.dom.Node
meth public abstract boolean hasAttribute(java.lang.String)
meth public abstract boolean hasAttributeNS(java.lang.String,java.lang.String)
meth public abstract java.lang.String getAttribute(java.lang.String)
meth public abstract java.lang.String getAttributeNS(java.lang.String,java.lang.String)
meth public abstract java.lang.String getTagName()
meth public abstract org.w3c.dom.Attr getAttributeNode(java.lang.String)
meth public abstract org.w3c.dom.Attr getAttributeNodeNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr)
meth public abstract org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr)
meth public abstract org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr)
meth public abstract org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public abstract org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.TypeInfo getSchemaTypeInfo()
meth public abstract void removeAttribute(java.lang.String)
meth public abstract void removeAttributeNS(java.lang.String,java.lang.String)
meth public abstract void setAttribute(java.lang.String,java.lang.String)
meth public abstract void setAttributeNS(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void setIdAttribute(java.lang.String,boolean)
meth public abstract void setIdAttributeNS(java.lang.String,java.lang.String,boolean)
meth public abstract void setIdAttributeNode(org.w3c.dom.Attr,boolean)

CLSS public abstract interface org.w3c.dom.Node
fld public final static short ATTRIBUTE_NODE = 2
fld public final static short CDATA_SECTION_NODE = 4
fld public final static short COMMENT_NODE = 8
fld public final static short DOCUMENT_FRAGMENT_NODE = 11
fld public final static short DOCUMENT_NODE = 9
fld public final static short DOCUMENT_POSITION_CONTAINED_BY = 16
fld public final static short DOCUMENT_POSITION_CONTAINS = 8
fld public final static short DOCUMENT_POSITION_DISCONNECTED = 1
fld public final static short DOCUMENT_POSITION_FOLLOWING = 4
fld public final static short DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC = 32
fld public final static short DOCUMENT_POSITION_PRECEDING = 2
fld public final static short DOCUMENT_TYPE_NODE = 10
fld public final static short ELEMENT_NODE = 1
fld public final static short ENTITY_NODE = 6
fld public final static short ENTITY_REFERENCE_NODE = 5
fld public final static short NOTATION_NODE = 12
fld public final static short PROCESSING_INSTRUCTION_NODE = 7
fld public final static short TEXT_NODE = 3
meth public abstract boolean hasAttributes()
meth public abstract boolean hasChildNodes()
meth public abstract boolean isDefaultNamespace(java.lang.String)
meth public abstract boolean isEqualNode(org.w3c.dom.Node)
meth public abstract boolean isSameNode(org.w3c.dom.Node)
meth public abstract boolean isSupported(java.lang.String,java.lang.String)
meth public abstract java.lang.Object getFeature(java.lang.String,java.lang.String)
meth public abstract java.lang.Object getUserData(java.lang.String)
meth public abstract java.lang.Object setUserData(java.lang.String,java.lang.Object,org.w3c.dom.UserDataHandler)
meth public abstract java.lang.String getBaseURI()
meth public abstract java.lang.String getLocalName()
meth public abstract java.lang.String getNamespaceURI()
meth public abstract java.lang.String getNodeName()
meth public abstract java.lang.String getNodeValue()
meth public abstract java.lang.String getPrefix()
meth public abstract java.lang.String getTextContent()
meth public abstract java.lang.String lookupNamespaceURI(java.lang.String)
meth public abstract java.lang.String lookupPrefix(java.lang.String)
meth public abstract org.w3c.dom.Document getOwnerDocument()
meth public abstract org.w3c.dom.NamedNodeMap getAttributes()
meth public abstract org.w3c.dom.Node appendChild(org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node cloneNode(boolean)
meth public abstract org.w3c.dom.Node getFirstChild()
meth public abstract org.w3c.dom.Node getLastChild()
meth public abstract org.w3c.dom.Node getNextSibling()
meth public abstract org.w3c.dom.Node getParentNode()
meth public abstract org.w3c.dom.Node getPreviousSibling()
meth public abstract org.w3c.dom.Node insertBefore(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node removeChild(org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node replaceChild(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract org.w3c.dom.NodeList getChildNodes()
meth public abstract short compareDocumentPosition(org.w3c.dom.Node)
meth public abstract short getNodeType()
meth public abstract void normalize()
meth public abstract void setNodeValue(java.lang.String)
meth public abstract void setPrefix(java.lang.String)
meth public abstract void setTextContent(java.lang.String)

CLSS public abstract interface org.w3c.dom.Text
intf org.w3c.dom.CharacterData
meth public abstract boolean isElementContentWhitespace()
meth public abstract java.lang.String getWholeText()
meth public abstract org.w3c.dom.Text replaceWholeText(java.lang.String)
meth public abstract org.w3c.dom.Text splitText(int)

