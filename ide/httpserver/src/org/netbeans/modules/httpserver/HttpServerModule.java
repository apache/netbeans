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

package org.netbeans.modules.httpserver;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.threads.TaskQueue;
import org.openide.modules.ModuleInstall;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;

import static java.util.Arrays.asList;

/**
* Module installation class for Http Server
*
* @author Petr Jiricka
*/
public class HttpServerModule extends ModuleInstall implements Externalizable {
    private static final Logger LOG = Logger.getLogger(HttpServerModule.class.getName());

    // Names for Filter/Servlet Mapping
    private static final String SERVLET_MAPPER = "ServletMapper";   // NOI18N
    private static final String WRAPPER_SERVLET = "WrapperServlet"; // NOI18N
    private static final String ACCESS_FILTER = "AccessFilter";     // NOI18N

    /** listener that reloads context when systemClassLoader changes */
    private static ContextReloader reloader;

    private static Tomcat tomcat;
    private static boolean inSetRunning = false;

    /** Module is being closed. */
    @Override
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
                if ((tomcat != null) && (!HttpServerSettings.running)) {
                    // another thread is trying to start the server, wait for a while and then stop it if it's still bad
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {}
                    if ((tomcat != null) && (!HttpServerSettings.running)) {
                        try {
                            tomcat.stop();
                            tomcat.destroy();
                        } catch (LifecycleException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        tomcat = null;
                    }
                }
                if (tomcat == null) {
                    try {
                        buildServer();
                        tomcat.start();
                        httpserverSettings().runSuccess();
                        reloader.activate();
                    } catch (Exception e) {
                        if(tomcat != null) {
                            try {
                                tomcat.stop();
                                tomcat.destroy();
                            } catch (LifecycleException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            tomcat = null;
                        }
                        inSetRunning = false;
                        Logger.getLogger("global").log(Level.INFO, null, e);
                        httpserverSettings().runFailure(e);
                    } finally {
                        httpserverSettings().setStartStopMessages(true);
                    }
                }
            } finally {
                inSetRunning = false;
            }
        }
    }

    @Override
    public void uninstalled () {
        stopHTTPServer();
    }
    
    /** stops the HTTP server */
    @SuppressWarnings("deprecation")  // NOI18N
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
                
                if (tomcat != null) {
                    try {
                        tomcat.stop();
                        tomcat.destroy();
                    }
                    catch (LifecycleException | RuntimeException e) {
                    }
                    tomcat = null;
                    if (httpserverSettings().isStartStopMessages()) {
                        LOG.log(Level.INFO, "Internal HTTP server stopped"); // NOI18N
                    }
                }
            }
            finally {
                inSetRunning = false;
            }
        }
    }
    

    private static void buildServer() throws Exception {
        File httpwork = Places.getCacheSubdirectory("httpwork");
        File httpworkBase = Places.getCacheSubdirectory("httpwork-base");
        tomcat = new Tomcat();
        tomcat.setBaseDir(httpworkBase.getAbsolutePath());
        tomcat.setPort(httpserverSettings().getPort());
        tomcat.getServer().setUtilityThreads(1);
        tomcat.getConnector().setXpoweredBy(false);
        // The WrapperServlet expects to be able to use encoded slashes as
        // markers, so they need to be passed through
        tomcat.getConnector().setEncodedSolidusHandling("PASSTHROUGH");
	TaskQueue tq = new TaskQueue(10);
	ThreadPoolExecutor tf  = new ThreadPoolExecutor(0, 3, 60, TimeUnit.SECONDS, tq);
	tomcat.getConnector().getProtocolHandler().setExecutor(tf);

        Context ctx = tomcat.addContext("", httpwork.getAbsolutePath());

        if(ctx instanceof StandardContext) {
            ((StandardContext) ctx).setClearReferencesRmiTargets(false);
        }

        ctx.setResources(new StandardRoot() {
            @Override
            protected void registerURLStreamHandlerFactory() {
                // Tomcat tries to override the URLStreamHandlerFactory, this
                // collides with NetBeans usage and alternative registration
                // methods are only available on JDK 9+
                // So disable this here, we don't really need it.
            }
        });

        ctx.setSessionTimeout(30);

        initContext(ctx);

        reloader = new ContextReloader (tomcat, ctx);
    }

    private static void initContext(Context ctx) {

        ctx.setParentClassLoader(Lookup.getDefault().lookup(ClassLoader.class));

        for (String mapping : ctx.findServletMappings()) {
            ctx.removeServletMapping(mapping);
        }

        for (Container container : asList(ctx.findChildren())) {
            ctx.removeChild(container);
        }

        for(FilterMap fm: ctx.findFilterMaps()) {
            ctx.removeFilterMap(fm);
        }

        for(FilterDef fd: ctx.findFilterDefs()) {
            ctx.removeFilterDef(fd);
        }

        FilterDef filterDef = new FilterDef();
        filterDef.setFilter(new AccessFilter());
        filterDef.setFilterName(ACCESS_FILTER);
        FilterMap filterMap = new FilterMap();
        filterMap.addURLPattern("/*");
        filterMap.setFilterName(ACCESS_FILTER);
        ctx.addFilterDef(filterDef);
        ctx.addFilterMap(filterMap);

        Wrapper sw = ctx.createWrapper();
        sw.setServlet(new WrapperServlet());
        sw.setName(WRAPPER_SERVLET);
        ctx.addChild(sw);

        ctx.addServletMappingDecoded(httpserverSettings().getWrapperBaseURL() + "*", WRAPPER_SERVLET);

        // Originally the Apache Tomcat InvokerServlet took care of invoking
        // servlet. This servlet was removed from Tomcat. As a replacement
        // this servlet is used. The idea is, that this servlet serves as a
        // fallback servlet. When it is invoked, it looksup the right servlet,
        // registers it and redispatches the request.
        sw = ctx.createWrapper();
        sw.setServlet(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                String className = classFromPath((HttpServletRequest) request);
                LOG.log(Level.FINE, "Servlet class name: {0}", className);
                if (className != null) {
                    try {
                        Class<?> clazz = Class.forName(className, true, ctx.getParentClassLoader());
                        Tomcat.addServlet(ctx, className, (Servlet) clazz.getConstructor().newInstance());
                        ctx.addServletMappingDecoded("/servlet/" + className + "/*", className);
                        request.getRequestDispatcher(request.getRequestURI()).forward(request, response);
                    } catch (ReflectiveOperationException | SecurityException | IllegalArgumentException ex) {
                        LOG.log(Level.WARNING, null, ex);
                    }
                }
            }
        });
        sw.setName(SERVLET_MAPPER);
        ctx.addChild(sw);

        ctx.addServletMappingDecoded("/servlet/*", SERVLET_MAPPER);
    }

    private static String classFromPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            return null;
        }
        int startPos = pathInfo.startsWith("/") ? 1 : 0;
        int endPos = pathInfo.indexOf("/", startPos);
        if (endPos < 0) {
            endPos = pathInfo.length();
        }
        String className = pathInfo.substring(startPos, endPos);
        return className;
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

        private final Context ctx;

        private Lookup.Result<ClassLoader> res;

        public ContextReloader(Tomcat tc, Context ctx) {
            this.ctx = ctx;
        }

        /**
         * Starts to listen on class loader changes
         */
        public void activate() {
            res = Lookup.getDefault().lookup(new Lookup.Template<>(ClassLoader.class));
            res.addLookupListener(this);
        }

        /**
         * Stops listening.
         */
        public void deactivate() {
            if (res != null) {
                res.removeLookupListener(this);
                res = null;
            }
        }

        @Override
        public void resultChanged(LookupEvent evt) {
            RequestProcessor.getDefault().post(this);
        }

        @Override
        public void run() {
            initContext(ctx);
        }
        
    }
}

