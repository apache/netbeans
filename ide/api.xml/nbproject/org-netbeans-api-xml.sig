#Signature file v4.1
#Version 1.67

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

CLSS public final org.netbeans.api.xml.parsers.DocumentInputSource
cons public init(javax.swing.text.Document)
meth public final void setCharacterStream(java.io.Reader)
meth public java.io.Reader getCharacterStream()
meth public java.lang.String getSystemId()
meth public java.lang.String toString()
supr org.xml.sax.InputSource
hfds LOG,doc

CLSS public org.netbeans.api.xml.parsers.SAXEntityParser
cons public init(org.xml.sax.XMLReader)
cons public init(org.xml.sax.XMLReader,boolean)
intf org.xml.sax.XMLReader
meth protected boolean propagateException(org.xml.sax.SAXParseException)
meth protected org.xml.sax.InputSource wrapInputSource(org.xml.sax.InputSource)
meth public boolean getFeature(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public java.lang.Object getProperty(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public org.xml.sax.ContentHandler getContentHandler()
meth public org.xml.sax.DTDHandler getDTDHandler()
meth public org.xml.sax.EntityResolver getEntityResolver()
meth public org.xml.sax.ErrorHandler getErrorHandler()
meth public void parse(java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public void setContentHandler(org.xml.sax.ContentHandler)
meth public void setDTDHandler(org.xml.sax.DTDHandler)
meth public void setEntityResolver(org.xml.sax.EntityResolver)
meth public void setErrorHandler(org.xml.sax.ErrorHandler)
meth public void setFeature(java.lang.String,boolean) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public void setProperty(java.lang.String,java.lang.Object) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
supr java.lang.Object
hfds FAKE_PUBLIC_ID,FAKE_SYSTEM_ID,RANDOM,generalEntity,peer,used
hcls EH,ER

CLSS public abstract org.netbeans.api.xml.services.UserCatalog
cons public init()
meth public java.util.Iterator getPublicIDs()
meth public javax.xml.transform.URIResolver getURIResolver()
meth public org.xml.sax.EntityResolver getEntityResolver()
meth public static org.netbeans.api.xml.services.UserCatalog getDefault()
supr java.lang.Object

CLSS public org.xml.sax.InputSource
cons public init()
cons public init(java.io.InputStream)
cons public init(java.io.Reader)
cons public init(java.lang.String)
meth public java.io.InputStream getByteStream()
meth public java.io.Reader getCharacterStream()
meth public java.lang.String getEncoding()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
meth public void setByteStream(java.io.InputStream)
meth public void setCharacterStream(java.io.Reader)
meth public void setEncoding(java.lang.String)
meth public void setPublicId(java.lang.String)
meth public void setSystemId(java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.xml.sax.XMLReader
meth public abstract boolean getFeature(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public abstract java.lang.Object getProperty(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public abstract org.xml.sax.ContentHandler getContentHandler()
meth public abstract org.xml.sax.DTDHandler getDTDHandler()
meth public abstract org.xml.sax.EntityResolver getEntityResolver()
meth public abstract org.xml.sax.ErrorHandler getErrorHandler()
meth public abstract void parse(java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public abstract void parse(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public abstract void setContentHandler(org.xml.sax.ContentHandler)
meth public abstract void setDTDHandler(org.xml.sax.DTDHandler)
meth public abstract void setEntityResolver(org.xml.sax.EntityResolver)
meth public abstract void setErrorHandler(org.xml.sax.ErrorHandler)
meth public abstract void setFeature(java.lang.String,boolean) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public abstract void setProperty(java.lang.String,java.lang.Object) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException

