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
package org.netbeans.modules.subversion.client;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLKeyException;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClientFactory.ConnectionType;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.Cancellable;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 *
 * @author Tomas Stupka 
 */
public class SvnClientInvocationHandler implements InvocationHandler {    

    private static final Logger LOG = Logger.getLogger(SvnClientInvocationHandler.class.getName());
    
    protected static final String GET_SINGLE_STATUS = "getSingleStatus"; // NOI18N
    protected static final String GET_STATUS = "getStatus"; // NOI18N
    protected static final String GET_INFO_FROM_WORKING_COPY = "getInfoFromWorkingCopy"; // NOI18N
    protected static final String CANCEL_OPERATION = "cancel"; //NOI18N
    private static final String DISPOSE_METHOD = "dispose"; //NOI18N
    private static final String CHECKOUT_METHOD = "checkout"; //NOI18N
    private static final Set<String> ADMINISTRATIVE_METHODS = new HashSet<>(Arrays.asList(
        "addConflictResolutionCallback", //NOI18N
        "addNotifyListener", //NOI18N
        "addPasswordCallback", //NOI18N
        "cancelOperation", //NOI18N
        "canCommitAcrossWC", //NOI18N
        "dispose", //NOI18N
        "getAdminDirectoryName", //NOI18N
        "getNotificationHandler", //NOI18N
        "getPostCommitError", //NOI18N
        "getSvnUrl", //NOI18N
        "isAdminDirectory", //NOI18N
        "isThreadsafe", //NOI18N
        "removeNotifyListener", //NOI18N
        "setConfigDirectory", //NOI18N
        "setProgressListener", //NOI18N
        "setPassword", //NOI18N
        "setUsername", //NOI18N
        "statusReturnsRemoteInfo", //NOI18N
        "suggestMergeSources", //NOI18N
        CANCEL_OPERATION,
        DISPOSE_METHOD
    ));
    private static final Set<String> READ_ONLY_METHODS = new HashSet<>(Arrays.asList(new String[] {
        "annotate", //NOI18N
        "createPatch", //NOI18N
        "diff", //NOI18N
        "diffSummarize", //NOI18N
        "doImport", //NOI18N - does nothing with WC
        "doExport", //NOI18N - does nothing with WC
        "getContent", //NOI18N
        "getDirEntry", //NOI18N
        "getIgnoredPatterns", //NOI18N
        "getInfo", //NOI18N
        "getInfoFromWorkingCopy", //NOI18N
        "getKeywords", //NOI18N
        "getList", //NOI18N
        "getListWithLocks", //NOI18N
        "getLogMessages", //NOI18N
        "getMergeInfo", //NOI18N
        "getMergeinfoLog", //NOI18N
        "getProperties", //NOI18N
        "getPropertiesIncludingInherited", //NOI18N
        "getRevProperties", //NOI18N
        "getRevProperty", //NOI18N
        "getSingleStatus", //NOI18N
        "getStatus", //NOI18N
        "propertyGet" //NOI18N
    }));
    
    private final ISVNClientAdapter adapter;
    private final SvnClientDescriptor desc;
    private final Cancellable cancellable;
    private SvnProgressSupport support;
    private final int handledExceptions;
    private static boolean metricsAlreadyLogged = false;
    private final ConnectionType connectionType;
    private volatile boolean disposed;
    private static final Map<String, Mutex> locks = new HashMap<>(5);
    private static final ConfigFiles SENSITIVE_CONFIG_FILES = new ConfigFiles();
    private static final boolean KEEP_SERVERS_FILE = Boolean.getBoolean("versioning.subversion.keepServersFile");
    
    public SvnClientInvocationHandler (ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions, SvnClientFactory.ConnectionType connType) {
        
        assert adapter  != null;
        assert desc     != null;
        
        this.adapter = adapter;
        this.desc = desc;
        this.support = support;
        this.handledExceptions = handledExceptions;
        this.cancellable = new Cancellable() {
            @Override
            public boolean cancel() {
                try {
                    SvnClientInvocationHandler.this.adapter.cancelOperation();
                } catch (SVNClientException ex) {
                    Subversion.LOG.log(Level.SEVERE, null, ex);
                    return false;
                }
                return true;
            }
        };
        this.connectionType = connType;
    }

    private static String print(Object[] args) {
        if (args == null || args.length == 0) {
            return "no parameters"; //NOI18N
        } else {
            StringBuilder sb = new StringBuilder();
            for(Object a : args) {
                sb.append("\n  "); //NOI18N
                if (a == null) {
                    sb.append("null"); //NOI18N
                } else {
                    sb.append(a.toString());
                    sb.append(" : "); //NOI18N
                    sb.append(a.getClass().getName());
                }
                sb.append("\n"); //NOI18N
            }
            return sb.toString();
        }
    }

    /**
     * @see InvocationHandler#invoke(Object proxy, Method method, Object[] args)
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                
        boolean fsReadOnlyAction = isFSWrittingCommand(method);

        try {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "~~~ SVN: invoking ''{0}'' with {1}", new Object[]{method.getName(), print(args)}); //NOI18N
                //new Throwable("~~~ SVN: invoking '" + method.getName() + "'").printStackTrace();
            }

            if (DISPOSE_METHOD.equals(method.getName())) {
                disposed = true;
            }
            Mutex mutex = getLock(method, args);
            if (mutex == null) {
                return invokeMethod(method, args);
            } else {
                Mutex.ExceptionAction<Object> action = new Mutex.ExceptionAction<Object>() {
                    @Override
                    public Object run () throws Exception {
                        return invokeMethod(method, args);
                    }
                };
                try {
                    if (isReadMethod(method)) {
                        return mutex.readAccess(action);
                    } else {
                        return mutex.writeAccess(action);
                    }
                } catch (MutexException ex) {
                    throw ex.getException();
                }
            }
        } catch (Exception e) {
            try {
                if(handleException((SvnClient) proxy, e, method.getName()) ) {
                    return invoke(proxy, method, args);
                } else {
                    // some action canceled by user message 
                    throw new SVNClientException(SvnClientExceptionHandler.ACTION_CANCELED_BY_USER); 
                }                
            } catch (InvocationTargetException ite) {
                Throwable t = ite.getTargetException();
                if(t instanceof SVNClientException) {
                    throw t;
                }
                throw ite;
            } catch (SSLKeyException ex) {
                if(ex.getCause() instanceof InvalidKeyException) {
                    InvalidKeyException ike = (InvalidKeyException) ex.getCause();
                    if(ike.getMessage().equalsIgnoreCase("illegal key size or default parameters")) { // NOI18N
                        SvnClientExceptionHandler.handleInvalidKeyException(ike);
                    }
                    return null; 
                }
                throw ex;
            } catch (Throwable t) {
                if(t instanceof InterruptedException) {
                    throw new SVNClientException(SvnClientExceptionHandler.ACTION_CANCELED_BY_USER);                     
                } 
                if(t instanceof SVNClientException) {
                    Throwable c = t.getCause();
                    if(c instanceof IOException) {
                        c = c.getCause();
                        if(c instanceof InterruptedException) {                    
                            throw new SVNClientException(SvnClientExceptionHandler.ACTION_CANCELED_BY_USER);                     
                        } 
                    }
                }
                Throwable c = t.getCause();
                if(c != null) {
                    // c.getMessage() could return null here, it is a general Throwable object => isOperationCancelled throws a NPE
                    String exMessage = c.getMessage();
                    if(c instanceof InterruptedException || (exMessage != null && SvnClientExceptionHandler.isOperationCancelled(exMessage))) {
                        throw new SVNClientException(SvnClientExceptionHandler.ACTION_CANCELED_BY_USER);
                    }
                }
                if(support != null && support.isCanceled()) {
                    // action has been canceled, level info should be fine
                    Subversion.LOG.log(Level.FINE, null, t);
                    // who knows what might have happened ...
                    throw new SVNClientException(SvnClientExceptionHandler.ACTION_CANCELED_BY_USER);
                }
                throw t;
            }
        } finally {
            // whatever command was invoked, whatever the result is - 
            // call refresh for all files notified by the client adapter
            if (fsReadOnlyAction) {
                Subversion.getInstance().getRefreshHandler().refresh();
            }
        }
    }

    private boolean isFSWrittingCommand(final Method method) {
        // list here all operations that can potentially modify files on the disk
        return !method.getName().equals("update") &&           // NOI18N
               !method.getName().equals("revert") &&           // NOI18N
               !method.getName().equals("switchToUrl") &&      // NOI18N
               !method.getName().equals("remove") &&           // NOI18N
               !method.getName().equals("mkdir") &&            // NOI18N
               !method.getName().equals("checkout") &&         // NOI18N
               !method.getName().equals("copy") &&             // NOI18N
               !method.getName().equals("move") &&             // NOI18N
               !method.getName().equals("merge");              // NOI18N
    }

    private void logClientInvoked() {
        if(metricsAlreadyLogged) {
            return;
        }
        try {
            SvnClientFactory.checkClientAvailable();
        } catch (SVNClientException e) {
            return;
        }
        String client = null;
        if(SvnClientFactory.isCLI()) {
            client = "CLI";
        } else if(SvnClientFactory.isJavaHl()) {
            client = "JAVAHL";
        } else if(SvnClientFactory.isSvnKit()) {
            client = "SVNKIT";
        } else {
            Subversion.LOG.warning("Unknown client type!");            
        }
        if(client != null) {
            Utils.logVCSClientEvent("SVN", client);   
        }
        metricsAlreadyLogged = true;
    }
    
    private boolean parallelizable (Method method) {
        String methodName = method.getName();
        return isClientAdministrativMethod(methodName)
                || isCancelCommand(method)
                || methodName.equals(GET_SINGLE_STATUS)
                || methodName.equals(GET_INFO_FROM_WORKING_COPY)
                || methodName.equals(GET_STATUS)
                || "getIgnoredPatterns".equals(methodName); //NOI18N
    }

    private Mutex getLock (Method method, Object[] args) {
        if (EventQueue.isDispatchThread() && parallelizable(method)
                || args == null || isClientAdministrativMethod(method.getName())) {
            return null;
        } else {
            File root = null;
            for (Object o : args) {
                if (o instanceof File) {
                    File f = (File) o;
                    root = getRoot(method.getName(), f);
                } else if (o instanceof File[]) {
                    for (File f : (File[]) o) {
                        root = getRoot(method.getName(), f);
                        if (root != null) {
                            break;
                        }
                    }
                }
                if (root != null) {
                    break;
                }
            }
            if (root != null) {
                return getLock(root.getAbsolutePath());
            }
        }
        return null;
    }

    private static File getRoot (String methodName, File f) {
        if (CHECKOUT_METHOD.equals(methodName)) {
            return f;
        } else {
            return Subversion.getInstance().getTopmostManagedAncestor(f);
        }
    }

    private Mutex getLock (String key) {
        synchronized (locks) {
            Mutex mutex = locks.get(key);
            if (mutex == null) {
                mutex = new Mutex();
                locks.put(key, mutex);
            }
            return mutex;
        }
    }

    private boolean isClientAdministrativMethod (String name) {
        return ADMINISTRATIVE_METHODS.contains(name);
    }

    private boolean isReadMethod (Method method) {
        return READ_ONLY_METHODS.contains(method.getName());
    }

    protected boolean isCancelCommand (final Method method) {
        String methodName = method.getName();
        return Cancellable.class.isAssignableFrom(method.getDeclaringClass())
                && methodName.equals(CANCEL_OPERATION);
    }
    
    protected Object invokeMethod(Method proxyMethod, Object[] args)
    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        return handle(proxyMethod, args);    
    }

    protected Object handle(final Method proxyMethod, final Object[] args) 
    throws SecurityException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException 
    {
        Object ret;

        Class[] parameters = proxyMethod.getParameterTypes();
        Class declaringClass = proxyMethod.getDeclaringClass();

        // escaping SVNUrl argument values - #146041
        // for javahl this is the place to escape those args, for cmd see SvnCommand
        if (args != null) {
            for (int i = 0; i < args.length; ++i) {
                Object arg = args[i];
                if (arg instanceof SVNUrl) {
                    try {
                        args[i] = SvnUtils.decodeAndEncodeUrl((SVNUrl) arg);
                    } catch (MalformedURLException ex) {
                        Subversion.LOG.log(Level.INFO, "Url: " + arg, ex);
                    }
                }
            }
        }

        if( ISVNClientAdapter.class.isAssignableFrom(declaringClass) ) {
            // Cliet Adapter
            if(support != null) {
                support.setCancellableDelegate(cancellable);
            }            
            File serversConfigFile = null;
            try {
                // save the proxy settings into the svn servers file                
                if(desc != null && desc.getSvnUrl() != null) {
                    synchronized (SENSITIVE_CONFIG_FILES) {
                        // prepare a config file. If the return value is not null
                        // it means the file should be deleted eventually because it 
                        // contains sensitive private data.
                        serversConfigFile = SvnConfigFiles.getInstance().storeSvnServersSettings(desc.getSvnUrl(), connectionType);
                        if (serversConfigFile != null) {
                            SENSITIVE_CONFIG_FILES.add(serversConfigFile);
                        }
                    }
                    if (!parallelizable(proxyMethod) && !"getInfo".equals(proxyMethod.getName())) { //NOI18N
                        // all svn actions running against a remote repository (commit, update, diff)
                        String url = desc.getSvnUrl().toString();
                        if (url.startsWith("file://")) { // NOI18N
                            // null means LOCAL
                            url = null;
                        }
                        Utils.logVCSExternalRepository("SVN", url); //NOI18N
                    }
                }
                logClientInvoked();
                ret = adapter.getClass().getMethod(proxyMethod.getName(), parameters).invoke(adapter, args);
            } finally {
                if (serversConfigFile != null) {
                    SENSITIVE_CONFIG_FILES.decrease(serversConfigFile);
                }
            }
            if(support != null) {
                support.setCancellableDelegate(null);
            }
        } else if( Cancellable.class.isAssignableFrom(declaringClass) ) { 
            // Cancellable
            ret = cancellable.getClass().getMethod(proxyMethod.getName(), parameters).invoke(cancellable, args);
        } else if( SvnClientDescriptor.class.isAssignableFrom(declaringClass) ) {            
            // Client Descriptor
            if(desc != null) {
                ret = desc.getClass().getMethod(proxyMethod.getName(), parameters).invoke(desc, args);    
            } else {
                // when there is no descriptor, then why has the method been called
                throw new NoSuchMethodException(proxyMethod.getName());
            }            
        } else {
            // try to take care for hashCode, equals & co. -> fallback to clientadapter
            ret = adapter.getClass().getMethod(proxyMethod.getName(), parameters).invoke(adapter, args);
        }                
        
        return ret;
    }

    private boolean handleException(SvnClient client, Throwable t, String methodName) throws Throwable {
        if( t instanceof InvocationTargetException ) {
            t = ((InvocationTargetException) t).getCause();            
        } 
        if( !(t instanceof SVNClientException) ) {
            throw t;
        }

        SvnClientExceptionHandler eh = new SvnClientExceptionHandler((SVNClientException) t, adapter, client, desc, handledExceptions, connectionType);
        eh.setMethod(methodName);
        return eh.handleException();        
    }

    @Override
    protected void finalize () throws Throwable {
        if (!disposed) {
            try {
                adapter.dispose();
            } catch (Throwable t) {
                // 
            }
        }
        super.finalize();
    }

    private static class ConfigFiles extends HashMap<File, Integer> {

        public synchronized void add (File file) {
            Integer currentCounter = get(file);
            if (currentCounter == null) {
                currentCounter = 0;
            }
            currentCounter++;
            put(file, currentCounter);
        }

        public synchronized void decrease (File file) {
            Integer currentCounter = get(file);
            if (currentCounter == null) {
                currentCounter = 1;
            }
            currentCounter--;
            if (currentCounter == 0) {
                remove(file);
                if (!KEEP_SERVERS_FILE) {
                    file.delete();
                }
            } else {
                put(file, currentCounter);
            }
        }
    }
    
}

