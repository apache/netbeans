/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.httpserver;

import java.io.Externalizable;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.apache.tomcat.context.WebXmlReader;
import org.apache.tomcat.startup.EmbededTomcat;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.logging.TomcatLogger;
import org.apache.tomcat.service.PoolTcpConnector;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
* Module installation class for Http Server
*
* @author Petr Jiricka
*/
public class HttpServerModule extends ModuleInstall implements Externalizable {


    private static ContextManager server;
    
    /** listener that reloads context when systemClassLoader changes */
    private static ContextReloader reloader;
    
    private static Thread serverThread;
    private static boolean inSetRunning = false;

    /** Module is being closed. */
    public void close () {
        // stop the server, don't set the running status
        synchronized (HttpServerSettings.httpLock ()) {
            stopHTTPServer();
        }
    }

    /** initiates HTTPServer so it runs */
    static void initHTTPServer() {
        if (inSetRunning)
            return;
        synchronized (HttpServerSettings.httpLock()) {
            if (inSetRunning)
                return;
            inSetRunning = true;
            try {
                if ((serverThread != null) && (!httpserverSettings().running)) {
                    // another thread is trying to start the server, wait for a while and then stop it if it's still bad
                    try {
                        Thread.currentThread().sleep(2000);
                    } catch (InterruptedException e) {}
                    if ((serverThread != null) && (!httpserverSettings().running)) {
                        serverThread.stop();
                        serverThread = null;
                    }
                }
                if (serverThread == null) {
                    serverThread = new Thread("HTTPServer") { // NOI18N
                        public void run() {
                            try {
                                server = buildServer();
                                server.start();
                                httpserverSettings().runSuccess();
                                reloader.activate();
                                // this is not a debug message, this is a server startup message
                                if (httpserverSettings().isStartStopMessages())
                                    System.out.println(NbBundle.getMessage(HttpServerModule.class, "CTL_ServerStarted", new Object[] {new Integer(httpserverSettings().getPort())}));
                            } catch (ThreadDeath td) {
                                throw td;
                            } catch (Throwable ex) {
                                Logger.getLogger("global").log(Level.INFO, null, ex);
                                // couldn't start
                                serverThread = null;
                                inSetRunning = false;
                                httpserverSettings().runFailure(ex);
                            } finally {
                                httpserverSettings().setStartStopMessages(true);
                            }
                        }
                    };
                    serverThread.start();
                }
                // wait for the other thread to start the server
                try {
                    HttpServerSettings.httpLock().wait(HttpServerSettings.SERVER_STARTUP_TIMEOUT);
                } catch (Exception e) {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                }
            } finally {
                inSetRunning = false;
            }
        }
    }

    public void uninstalled () {
        stopHTTPServer();
    }
    
    /** stops the HTTP server */
    @SuppressWarnings("deprecation")
    static void stopHTTPServer() {
        if (inSetRunning)
            return;
        synchronized (HttpServerSettings.httpLock ()) {
            if (inSetRunning)
                return;
            inSetRunning = true;
            try {
                if (reloader != null) {
                    reloader.deactivate ();
                    reloader = null;
                }
                
                if ((serverThread != null) && (server != null)) {
                    try {
                        server.stop();
                        serverThread.join();
                    }
                    catch (InterruptedException e) {
                        serverThread.stop(); 
                        /* deprecated, but this really is the last resort,
                           only if everything else failed */
                    }
                    catch (Exception e) {
                        //e.printStackTrace();
                        serverThread.stop();
                        /* deprecated, but this really is the last resort,
                           only if everything else failed */
                    }
                    serverThread = null;
                    // this is not a debug message, this is a server shutdown message
                    if (httpserverSettings ().isStartStopMessages())
                        System.out.println(NbBundle.getBundle(HttpServerModule.class).
                                           getString("CTL_ServerStopped"));
                }
            }
            finally {
                inSetRunning = false;
            }
        }
    }
    
    
    private static ContextManager getContextManager(EmbededTomcat tc) {
        try {
            java.lang.reflect.Field fm = EmbededTomcat.class.getDeclaredField("contextM");   // NOI18N
            fm.setAccessible(true);
            return (ContextManager)fm.get(tc);
        }
        catch (NoSuchFieldException e) {
            return null;
        }
        catch (IllegalAccessException e) {
            return null;
        }
    }
    

    /** Removes WebXmlReader interceptor to avoid attempt 
     *  to load JspServlet that processes jsp file and produces confusing message
     */
    private static void removeWebXmlReader (EmbededTomcat tc) {
        try {
            java.lang.reflect.Field fm = EmbededTomcat.class.getDeclaredField("contextInt");   // NOI18N
            fm.setAccessible(true);
            Vector contextInt = (Vector)fm.get(tc);
            Iterator it = contextInt.iterator ();
            while (it.hasNext ()) {
                Object o = it.next ();
                if (o instanceof WebXmlReader) {
                    contextInt.remove (o);
                    break;
                }
            }
        }
        catch (NoSuchFieldException e) {
            return;
        }
        catch (IllegalAccessException e) {
            return;
        }
    }
    

    private static ContextManager buildServer() throws Exception {
        HttpServerSettings op = httpserverSettings ();

        NbLogger logger = new NbLogger();
        logger.setName("tc_log");    // NOI18N

        final EmbededTomcat tc=new EmbededTomcat();
        
        File wd = FileUtil.toFile (FileUtil.getConfigRoot());
        wd = new File(wd, "httpwork"); // NOI18N
        tc.setWorkDir(wd.getAbsolutePath());
        
        // install interceptors which need to be initialized BEFORE the default server interceptors
	NbLoaderInterceptor nbL =new NbLoaderInterceptor();
	tc.addContextInterceptor( nbL );
        
        // hack - force initialization of default interceptors, so our interceptor is after them
        tc.addApplicationAdapter(null);

        // install interceptors which need to be initialized AFTER the default server interceptors
	NbServletsInterceptor nbI =new NbServletsInterceptor();
	tc.addContextInterceptor( nbI );

        removeWebXmlReader (tc);
        
        ServletContext sctx;
        sctx=tc.addContext("", wd.toURI().toURL());  // NOI18N
        tc.initContext( sctx );
        //ctxt.getServletLoader().setParentLoader(TopManager.getDefault().systemClassLoader());
        
        tc.addEndpoint( op.getPort(), null, null);

        final ContextManager cm = getContextManager(tc);
        
        reloader = new ContextReloader (tc, cm, sctx);
        
        // reduce number of threads
        Enumeration e = cm.getConnectors ();
        while (e.hasMoreElements ()) {
            Object o = e.nextElement ();
            if (o instanceof PoolTcpConnector) {
                org.apache.tomcat.core.ServerConnector conn = (PoolTcpConnector)o;
                conn.setAttribute (PoolTcpConnector.MIN_SPARE_THREADS, "0");     // NOI18N
                conn.setAttribute (PoolTcpConnector.MAX_SPARE_THREADS, "1");     // NOI18N
                conn.setAttribute (PoolTcpConnector.MAX_THREADS, "3");           // NOI18N
            }
        }
        
        return cm;
        
    }

    private static class NbLogger extends TomcatLogger {
        public NbLogger() {
            super();
        }

        protected void realLog(String message) {
        }

        protected void realLog(String message, Throwable t) {
        }
    
        public void flush() {
        }
    }

    /** 
     * Obtains settings of this module
     */
    static HttpServerSettings httpserverSettings () {
        return HttpServerSettings.getDefault();
    }
    
    /** Listener for change of system class loader to reinitialize context 
     * running on HTTP server.
     * The purpose is to force usage of up-to-date classes even in the case 
     * of module reloading.
     */
    private static class ContextReloader implements LookupListener, Runnable {
        
        private ServletContext ide_ctx;
        
        private EmbededTomcat tc;
        
        private ContextManager cm;

	private Lookup.Result<ClassLoader> res;
        
        public ContextReloader (EmbededTomcat tc, ContextManager cm, ServletContext ctx) {
            ide_ctx = ctx;
            this.tc = tc;
            this.cm = cm;
        }
        
        /** Starts to listen on class loader changes */
        public void activate () {
            res = Lookup.getDefault().lookup(new Lookup.Template<ClassLoader> (ClassLoader.class));
            res.addLookupListener (this);
        }
        
        /** Stops listening. */
        public void deactivate () {
	    if (res != null) {
                res.removeLookupListener (this);
	        res = null;
	    }
        }
        
        public void resultChanged (LookupEvent evt) {
            RequestProcessor.getDefault ().post (this);
        }
        
        public void run () {
	    ClassLoader cl = (ClassLoader)res.allInstances ().iterator ().next ();
            cm.setParentClassLoader (cl);
            
            File wd = FileUtil.toFile (FileUtil.getConfigRoot());
            wd = new File(wd, "httpwork"); // NOI18N
            
            Enumeration e = cm.getContexts ();
            while (e.hasMoreElements ()) {
                Object o = e.nextElement ();
                if (o instanceof Context) {
                    Context ctx = (Context)o;
                    // PENDING why this is in loop?
                    tc.removeContext (ide_ctx);
                    try {
                        ide_ctx=tc.addContext ("", wd.toURI().toURL ());  // NOI18N
                    }
                    catch (MalformedURLException ex) {
                        // ErrorManager.getDefault ().log (ErrorManager.INFORMATIONAL, ex);
                    }
                    tc.initContext ( ide_ctx );
                }
            }
            
        }
        
    }
}

