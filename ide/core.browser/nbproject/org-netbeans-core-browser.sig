#Signature file v4.1
#Version 1.51.0

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

CLSS public abstract org.netbeans.core.browser.api.EmbeddedBrowserFactory
cons public init()
fld public final static java.lang.String PROP_ENABLED = "enabled"
meth public abstract boolean isEnabled()
meth public abstract org.netbeans.core.browser.api.WebBrowser createEmbeddedBrowser()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static org.netbeans.core.browser.api.EmbeddedBrowserFactory getDefault()
supr java.lang.Object

CLSS public abstract org.netbeans.core.browser.api.WebBrowser
cons public init()
fld public final static java.lang.String PROP_BACKWARD = "backward"
fld public final static java.lang.String PROP_FORWARD = "forward"
fld public final static java.lang.String PROP_HISTORY = "history"
fld public final static java.lang.String PROP_LOADING = "loading"
fld public final static java.lang.String PROP_STATUS_MESSAGE = "statusMessage"
fld public final static java.lang.String PROP_TITLE = "title"
fld public final static java.lang.String PROP_URL = "url"
meth public abstract boolean isBackward()
meth public abstract boolean isForward()
meth public abstract boolean isHistory()
meth public abstract java.awt.Component getComponent()
meth public abstract java.lang.Object executeJavaScript(java.lang.String)
meth public abstract java.lang.String getStatusMessage()
meth public abstract java.lang.String getTitle()
meth public abstract java.lang.String getURL()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getCookie(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.openide.util.Lookup getLookup()
meth public abstract org.w3c.dom.Document getDocument()
meth public abstract void addCookie(java.util.Map<java.lang.String,java.lang.String>)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addWebBrowserListener(org.netbeans.core.browser.api.WebBrowserListener)
meth public abstract void backward()
meth public abstract void deleteCookie(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void dispose()
meth public abstract void forward()
meth public abstract void reloadDocument()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removeWebBrowserListener(org.netbeans.core.browser.api.WebBrowserListener)
meth public abstract void setContent(java.lang.String)
meth public abstract void setURL(java.lang.String)
meth public abstract void showHistory()
meth public abstract void stopLoading()
supr java.lang.Object

CLSS public abstract org.netbeans.core.browser.api.WebBrowserEvent
cons public init()
fld public final static int WBE_KEY_EVENT = 5
fld public final static int WBE_LOADING_ENDED = 3
fld public final static int WBE_LOADING_STARTED = 2
fld public final static int WBE_LOADING_STARTING = 1
fld public final static int WBE_MOUSE_EVENT = 4
meth public abstract int getType()
meth public abstract java.awt.AWTEvent getAWTEvent()
meth public abstract java.lang.String getURL()
meth public abstract org.netbeans.core.browser.api.WebBrowser getWebBrowser()
meth public abstract org.w3c.dom.Node getNode()
meth public abstract void cancel()
supr java.lang.Object

CLSS public abstract interface org.netbeans.core.browser.api.WebBrowserListener
meth public abstract void onDispatchEvent(org.netbeans.core.browser.api.WebBrowserEvent)

