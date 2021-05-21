/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.util;

import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;


/** Maps internal NetBeans resources such as repository objects to URLs.
* The mapping is delegated to an HTTP server module, which registers to do
* the mapping. It is also responsible for actually serving the individual data objects
* from the Repository and resources from the system classpath.
* @author Petr Jiricka
* @deprecated The <code>httpserver</code> module should provide a replacement for this API if necessary.
*/
@Deprecated
public abstract class HttpServer {
    /** regular server to be used */
    private static HttpServer.Impl registeredServer = null;

    private HttpServer() {
    }

    /** Returns a server implementation which is currently registered with the system.
    *  Server implementation obtained from Lookup has highest priority.
    *  'Normal' registered server has priority over a default registered server.
    *  If no server has been registered, internal error is
    */
    private static HttpServer.Impl getServer() throws UnknownHostException {
        Object o = Lookup.getDefault().lookup(HttpServer.Impl.class);

        if (o != null) {
            return (HttpServer.Impl) o;
        }

        if (registeredServer != null) {
            return registeredServer;
        } else {
            throw new UnknownHostException(NbBundle.getBundle(HttpServer.class).getString("MSG_NoServerRegistered"));
        }
    }

    /** Register the system HTTP server.
    * Typically this would be done in {@link org.openide.modules.ModuleInstall#installed}
    * or {@link org.openide.modules.ModuleInstall#restored}.
    * @param server the server to register
    * @throws SecurityException if there was already one registered
    * @deprecated As of 2.11 use Lookup instead of registering HTTP server
    */
    @Deprecated
    public static void registerServer(HttpServer.Impl server)
    throws SecurityException {
        if (registeredServer != null) {
            throw new SecurityException(NbBundle.getBundle(HttpServer.class).getString("SERVER_REGISTERED"));
        }

        registeredServer = server;
    }

    /** Deregister the system HTTP server.
    * Typically this would be done in {@link org.openide.modules.ModuleInstall#uninstalled}.
    * @param server the server to deregister
    * @throws SecurityException if the specified server was not the installed one
    * @deprecated As of 2.11 use Lookup instead of registering and derigistering HTTP server
    */
    @Deprecated
    public static void deregisterServer(HttpServer.Impl server)
    throws SecurityException {
        if (registeredServer == null) {
            return; // [PENDING] maybe remove this test and let it throw sec exc --jglick
        }

        if (registeredServer != server) {
            throw new SecurityException(NbBundle.getBundle(HttpServer.class).getString("SERVER_CANNOT_UNREGISTER"));
        } else {
            registeredServer = null;
        }
    }

    /** Map a file object to a URL.
    * Should ensure that the file object is accessible via the given URL.
    * @param fo the file object to represent
    * @return a URL providing access to it
    * @throws MalformedURLException for the usual reasons
    * @throws UnknownHostException for the usual reasons, or if there is no registered server
    * @deprecated Use {@link org.openide.filesystems.URLMapper} instead.
    */
    @Deprecated
    public static URL getRepositoryURL(FileObject fo) throws MalformedURLException, UnknownHostException {
        return getServer().getRepositoryURL(fo);
    }

    /** Map the repository root to a URL.
    * This URL should serve a page from which repository objects are accessible.
    * This means that it should serve a package-oriented view of the Repository, corresponding
    * to a merge of all files present in the root folders of visible file systems.
    * @return a URL
    * @throws MalformedURLException for the usual reasons
    * @throws UnknownHostException for the usual reasons, or if there is no registered server
    * @deprecated Assumes repository equals classpath.
    */
    @Deprecated
    public static URL getRepositoryRoot() throws MalformedURLException, UnknownHostException {
        return getServer().getRepositoryRoot();
    }

    /** Map a resource path to a URL.
    * Should ensure that the resource is accessible via the given URL.
    * @param resourcePath path of the resource in classloader format (e.g. <code>/some/path/resources/icon32.gif</code>)
    * @return a URL providing access to it
    * @see ClassLoader#getResource(java.lang.String)
    * @throws MalformedURLException for the usual reasons
    * @throws UnknownHostException for the usual reasons, or if there is no registered server
    * @deprecated Use {@link org.openide.filesystems.URLMapper} with a URL protocol <code>nbres</code>.
    */
    @Deprecated
    public static URL getResourceURL(String resourcePath)
    throws MalformedURLException, UnknownHostException {
        return getServer().getResourceURL(resourcePath);
    }

    /** Get URL root for a resource from system classpath.
    * @return the URL
    * @throws MalformedURLException for the usual reasons
    * @throws UnknownHostException for the usual reasons
    * @see HttpServer#getResourceURL
    * @deprecated Use {@link org.openide.filesystems.URLMapper} with a URL protocol <code>nbres</code>.
    */
    @Deprecated
    public static URL getResourceRoot() throws MalformedURLException, UnknownHostException {
        return getServer().getResourceRoot();
    }

    /** Requests the server to allow access to it from a given IP address.
    *  This can be useful if a module wishes another machine to be able to access
    *  the server, such as a machine running a deployment server.
    *  The server may or may not grant access to the IP address, for example
    *  if the user does not wish to grant access to the IP address.
    *  @param addr address for which access is requested
    *  @return <code>true</code> if access has been granted
    * @deprecated Should be replaced by an API in the <code>httpserver</code> module if still required.
    */
    @Deprecated
    public static boolean allowAccess(InetAddress addr)
    throws UnknownHostException {
        return getServer().allowAccess(addr);
    }

    /** Implementation of the HTTP server.
    * Must be implemented by classes which want to register as a server.
    * Implementations are obtained using Lookup.
    * <p>Such a server must be prepared to specially serve pages from
    * within the IDE, i.e. the Repository and the system class
    * loader. (It may also serve external pages, if desired.) It should
    * have a system option specifying at least the port number (<em>by
    * default, an unused port above 1000</em>), the host access
    * restrictions (<em>by default, only <code>localhost</code></em>),
    * and an toggle to disable it. It should provide URLs using the
    * protocol <code>http</code> so as not to need to register a new protocol
    * handler.
    * @deprecated Useful only for {@link HttpServer} which is itself deprecated.
    */
    @Deprecated
    public interface Impl {
        /** Get the URL for a file object.
        * @param fo the file object
        * @return the URL
        * @throws MalformedURLException for the usual reasons
        * @throws UnknownHostException for the usual reasons
        * @see HttpServer#getRepositoryURL
        */
        public URL getRepositoryURL(FileObject fo) throws MalformedURLException, UnknownHostException;

        /** Get the URL for the Repository. For this URL,
        * the implementation should display a page containing a list of links to subdirectories (packages).
        * @return the URL
        * @throws MalformedURLException for the usual reasons
        * @throws UnknownHostException for the usual reasons
        * @see HttpServer#getRepositoryRoot
        */
        public URL getRepositoryRoot() throws MalformedURLException, UnknownHostException;

        /** Get the URL for a resource from system classpath. The URL must comply to java naming conventions,
        * i.e. the URL must end with a fully qualified resource name.
        * @param resourcePath the resource path
        * @return the URL
        * @throws MalformedURLException for the usual reasons
        * @throws UnknownHostException for the usual reasons
        * @see HttpServer#getResourceURL
        */
        public URL getResourceURL(String resourcePath)
        throws MalformedURLException, UnknownHostException;

        /** Get URL root for a resource from system classpath.
        * @return the URL
        * @throws MalformedURLException for the usual reasons
        * @throws UnknownHostException for the usual reasons
        * @see HttpServer#getResourceURL
        */
        public URL getResourceRoot() throws MalformedURLException, UnknownHostException;

        /** Requests the server to allow access to it from a given IP address.
        *  This can be useful if a module wishes another machine to be able to access
        *  the server, such as a machine running a deployment server.
        *  The server may or may not grant access to the IP address, for example
        *  if the user does not wish to grant access to the IP address.
        *  @param addr address for which access is requested
        *  @return <code>true</code> if access has been granted
        */
        public boolean allowAccess(InetAddress addr) throws UnknownHostException;
    }
}
