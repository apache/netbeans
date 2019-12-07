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

package org.netbeans;

import java.awt.AWTPermission;
import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LoggingPermission;
import org.openide.util.Lookup;
import org.openide.util.WeakSet;

/** NetBeans security manager implementation.
* @author Ales Novak, Jesse Glick
*/
public class TopSecurityManager extends SecurityManager {
    private static final boolean check = !Boolean.getBoolean("netbeans.security.nocheck"); // NOI18N
    private static final Logger LOG = Logger.getLogger(TopSecurityManager.class.getName());

    private Permission allPermission;
    
    /* JVMPI sometimes deadlocks sync getForeignClassLoader
        and Class.forName
    */
    private static final Class<?> classLoaderClass = ClassLoader.class;
    private static final Class<?> URLClass = URL.class;
    private static final Class<?> runtimePermissionClass = RuntimePermission.class;
    private static final Class<?> accessControllerClass = AccessController.class;
    private static final Class<?> awtPermissionClass = AWTPermission.class;
    private static SecurityManager fsSecManager;

    private static final List<SecurityManager> delegates = new ArrayList<SecurityManager>();
    /** Register a delegate security manager that can handle some checks for us.
     * Currently only checkExit and checkTopLevelWindow are supported.
     * @param sm the delegate to register
     * @throws SecurityException without RuntimePermission "TopSecurityManager.register"
     */
    public static void register(SecurityManager sm) throws SecurityException {
/*        if (check) {
            try {
                AccessController.checkPermission(new RuntimePermission("TopSecurityManager.register")); // NOI18N
            } catch (SecurityException se) {
                // Something is probably wrong; debug it better.
                ProtectionDomain pd = sm.getClass().getProtectionDomain();
                CodeSource cs = pd.getCodeSource();
                System.err.println("Code source of attempted secman: " + (cs != null ? cs.getLocation().toExternalForm() : "<none>")); // NOI18N
                System.err.println("Its permissions: " + pd); // NOI18N
                throw se;
            }
        }
*/
        synchronized (delegates) {
            if (delegates.contains(sm)) throw new SecurityException();
            delegates.add(sm);
            if (fsSecManager == null) {
                for (Lookup.Item<SecurityManager> item : Lookup.getDefault().lookupResult(SecurityManager.class).allItems()) {
                    if (item != null && "org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager".equals(item.getId())) {//NOI18N
                        fsSecManager = item.getInstance();
                        break;
                    }
                }
                assert fsSecManager != null;
            }            
        }
    }
    /** Unregister a delegate security manager.
     * @param sm the delegate to unregister
     * @throws SecurityException without RuntimePermission "TopSecurityManager.unregister"
     */
    public static void unregister(SecurityManager sm) throws SecurityException {
/*        if (check) {
            AccessController.checkPermission(new RuntimePermission("TopSecurityManager.unregister")); // NOI18N
        }
*/
        synchronized (delegates) {
            if (!delegates.contains(sm)) throw new SecurityException();
            delegates.remove(sm);
        }
    }

    /**
    * constructs new TopSecurityManager
    */
    public TopSecurityManager () {
        allPermission = new AllPermission();
    }

    public @Override void checkExit(int status) throws SecurityException {
        if (! check) {
            return;
        }
        
        synchronized (delegates) {
            Iterator<SecurityManager> it = delegates.iterator();
            while (it.hasNext()) {
                it.next().checkExit(status);
            }
        }
        
        PrivilegedCheck.checkExit(status, this);
    }

    SecurityManager getSecurityManager() {
        if (fsSecManager == null) {
            synchronized (delegates) {
                return fsSecManager;
            }        
        }
        return fsSecManager;
    }
    
    private void notifyDelete(String file) {
        SecurityManager s = getSecurityManager();
        if (s != null) {
            s.checkDelete(file);
        }
    }

    private void notifyRead(String file) {
        SecurityManager s = getSecurityManager();
        if (s != null) {
            s.checkRead(file);
        }
    }

    private void notifyWrite(String file) {
        SecurityManager s = getSecurityManager();
        if (s != null) {
            s.checkWrite(file);
        }
    }
    
    static boolean officialExit = false;
    static Class[] getStack() {
        SecurityManager s = System.getSecurityManager();
        TopSecurityManager t;
        if (s instanceof TopSecurityManager) {
            t = (TopSecurityManager)s;
        } else {
            t = new TopSecurityManager();
        }
        return t.getClassContext();
    }
    
    /** Can be called from core classes to exit the system.
     * Direct calls to System.exit will not be honored, for safety.
     * @param status the status code to exit with
     * @see "#20751"
     */
    public static void exit(int status) {
        if (officialExit) {
            return; // already inside a shutdown hook
        }
        officialExit = true;
        System.exit(status);
    }

    final void checkExitImpl(int status, AccessControlContext acc) throws SecurityException {             
        if (!officialExit) {
            throw new ExitSecurityException("Illegal attempt to exit early"); // NOI18N
        }

        super.checkExit(status);
    }

    public boolean checkTopLevelWindow(Object window) {
        return checkTopLevelWindow(new AWTPermission("showWindowWithoutWarningBanner"), window); // NOI18N
    }

    private boolean checkTopLevelWindow(Permission windowPermission, Object window) {
        synchronized (delegates) {
            for (SecurityManager sm : delegates) {
                sm.checkPermission(windowPermission, window);
            }
        }
        return true;
    }

    /* XXX probably unnecessary:
    // Hack against permissions of Launcher$AppLoader.
    public void checkPackageAccess(String pckg) {
        if (pckg == null) return;
        if (pckg.startsWith("sun.")) { // NOI18N
            if (inClazz("sun.misc.Launcher") || inClazz("java.lang.Class")) { // NOI18N
                return;
            }
        }
        super.checkPackageAccess(pckg);
    }

    private boolean inClazz(String s) {
        Class[] classes = getClassContext();
        int i = 0;
        for (; (i < classes.length) && (classes[i] == TopSecurityManager.class); i++);
        if (i == classes.length) {
            return false;
        }
        return classes[i].getName().startsWith(s);
    }
     */

    /** Performance - all props accessible */
    public @Override final void checkPropertyAccess(String x) {
        if ("netbeans.debug.exceptions".equals(x)) { // NOI18N
            // Get rid of this old system property.
            for (Class<?> c : getClassContext()) {
                if (c != TopSecurityManager.class &&
                        c != System.class &&
                        c != Boolean.class) {
                    String n = c.getName();
                    synchronized (warnedClassesNDE) {
                        if (warnedClassesNDE.add(n)) {
                            LOG.log(Level.WARNING, "use of system property netbeans.debug.exceptions has been obsoleted in favor of java.util.logging.Logger at {0}", findCallStackLine(n));
                        }
                    }
                    break;
                }
            }
        }
        if ("netbeans.home".equals(x) || "netbeans.user".equals(x)) { // NOI18N
            // Control access to this system property.
            for (Class<?> c : getClassContext()) {
                if (c != TopSecurityManager.class &&
                        c != System.class &&
                        c != Boolean.class) {
                    String n = c.getName();
                    boolean log;
                    synchronized (warnedClassesNH) {
                            log = warnedClassesNH.add(n);
                    }
                    if (log) {
                        LOG.log(Level.WARNING, "use of system property {0} has been obsoleted in favor of InstalledFileLocator/Places at {1}", new Object[] {x, findCallStackLine(n)});
                    }
                    break;
                }
            }
        }
    }
    private static String findCallStackLine(String callerClazz) {
        for (StackTraceElement line : Thread.currentThread().getStackTrace()) {
            if (line.getClassName().equals(callerClazz)) {
                return line.toString();
            }
        }
        return callerClazz;
    }
    private final Set<String> warnedClassesNDE = new HashSet<String>(25);
    private static final Set<String> warnedClassesNH = new HashSet<String>(25);
    static {
        // XXX cleaner would be to use @SuppressWarnings, but that has Retention(SOURCE), and not all these can use org.netbeans.api.annotations.common
        warnedClassesNH.add("org.openide.modules.Places");
        warnedClassesNH.add("org.netbeans.MainImpl"); // NOI18N
        warnedClassesNH.add("org.netbeans.MainImpl$BootClassLoader");
        warnedClassesNH.add("org.netbeans.CLIHandler");
        warnedClassesNH.add("org.netbeans.Stamps"); // NOI18N
        warnedClassesNH.add("org.netbeans.Clusters"); // NOI18N
        warnedClassesNH.add("org.netbeans.core.startup.InstalledFileLocatorImpl"); // NOI18N
        warnedClassesNH.add("org.netbeans.core.startup.CLIOptions");
        warnedClassesNH.add("org.netbeans.core.startup.preferences.RelPaths");
        warnedClassesNH.add("org.netbeans.core.startup.layers.BinaryFS");
        warnedClassesNH.add("org.netbeans.modules.netbinox.NetbinoxFactory");
        warnedClassesNH.add("org.netbeans.updater.UpdateTracking"); // NOI18N
        warnedClassesNH.add("org.netbeans.core.ui.ProductInformationPanel"); // #47429; NOI18N
        warnedClassesNH.add("org.netbeans.lib.uihandler.LogFormatter");
        warnedClassesNH.add("org.netbeans.modules.project.libraries.LibrariesStorage");
        warnedClassesNH.add("org.netbeans.modules.j2ee.sun.ide.j2ee.PluginProperties"); // AS bundle is not in any cluster
        warnedClassesNH.add("org.netbeans.modules.apisupport.project.universe.NbPlatform"); // defaultPlatformLocation
    }

    /* ----------------- private methods ------------- */

    /**
     * The method is empty. This is not "secure", but on the other hand,
     * it reduces performance penalty of startup about 10%
     */
    public @Override void checkRead(String file) {
        notifyRead(file);
    }
    
    public @Override void checkRead(FileDescriptor fd) {
    }

    
    public @Override void checkWrite(FileDescriptor fd) {
    }

    /** The method has awful performance in super class */
    public @Override void checkDelete(String file) {
        notifyDelete(file);
        try {
            checkPermission(allPermission);
            return;
        } catch (SecurityException e) {
            super.checkDelete(file);
        }
    }
           
    /** The method has awful performance in super class */
    public @Override void checkWrite(String file) {
        notifyWrite(file);
        try {
            checkPermission(allPermission);
            return;
        } catch (SecurityException e) {
            super.checkWrite(file);
        }
    }
    
    /** Checks connect */
    public @Override void checkConnect(String host, int port) {
        if (! check) {
            return;
        }
        
        try {
            checkPermission(allPermission);
            return;
        } catch (SecurityException e) {
        }
        
        try {
            super.checkConnect(host, port);
            return;
        } catch (SecurityException e) {
        }
        
        PrivilegedCheck.checkConnect(host, port, this);
    }
     
    final void checkConnectImpl(String host, int port) {
        Class<?> insecure = getInsecureClass();
        if (insecure != null) {  
            URL ctx = getClassURL(insecure);
            if (ctx != null) {
                try {
                    String fromHost = ctx.getHost();
                    InetAddress ia2 = InetAddress.getByName(host);
                    InetAddress ia3 = InetAddress.getByName(fromHost);
                    if (ia2.equals(ia3)) {
                        return;
                    }
                } catch (UnknownHostException e) { // ignore
                    e.printStackTrace();
                }
            }
            throw new SecurityException();
        }
    }

    public @Override void checkConnect(String s, int port, Object context) {
        checkConnect(s, port);
    }

    private final Set<Class<?>> warnedSunMisc = new WeakSet<>();
    private final Set<String> callerWhiteList = createCallerWhiteList();
    public void checkMemberAccess(Class<?> clazz, int which) {
        final String n = clazz.getName();
        if (n.startsWith("sun.misc")) { // NOI18N
            Class<?> caller = null;
            Class<?>[] arr = getClassContext();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == TopSecurityManager.class) {
                    continue;
                }
                if (arr[i] != Class.class) {
                    caller = arr[i];
                    break;
                }
            }
            StringBuilder msg = new StringBuilder();
            msg.append("Dangerous reflection access to ").append(n).append(" by ").append(caller).append(" detected!");
            if (caller != null && caller.getProtectionDomain() != null) {
                CodeSource cs = caller.getProtectionDomain().getCodeSource();
                msg.append("\ncode location: ").append(cs == null ? "null" : cs.getLocation());
            }
            if (caller != null && isDangerous(caller.getName(), n)) {
                throw new SecurityException(msg.toString());
            }
            Level l;
            if (caller != null && callerWhiteList.contains(caller.getName())) {
                l = Level.FINEST;
            } else {
                l = Level.FINE;
                assert (l = Level.INFO) != null;
            }
            if (!warnedSunMisc.add(caller)) {
                LOG.log(l, msg.toString());
                return; 
            }
            Exception ex = new Exception(msg.toString()); // NOI18N
            LOG.log(l, null, ex);
        }
    }
    
    /**
     * Create list of safe callers for {@link #checkMemberAccess(Class, int)}.
     */
    private static Set<String> createCallerWhiteList() {
        Set<String> wl = new HashSet<String>();
        wl.add("org.netbeans.core.output2.FileMapStorage");             //NOI18N
        wl.add("com.sun.tools.javac.util.CloseableURLClassLoader");     //NOI18N
        wl.add("java.lang.Thread$1");                                   //NOI18N
        wl.add("org.clank.support.NativeMemory");                       //NOI18N
        wl.add("org.apache.lucene.store.MMapDirectory$1");              //NOI18N
        wl.add("org.apache.lucene.util.Constants"); //#217037
        wl.add("org.apache.lucene.util.RamUsageEstimator");//#217037
        wl.add("com.google.gson.internal.UnsafeAllocator"); //#219464   //NOI18N
        wl.add("org.netbeans.modules.web.jspparser_ext.WebAppParseSupport$ParserClassLoader"); //#218690 // NOI18N
        return wl;
    }
    private static boolean isDangerous(String caller, String accessTo) {
        if ("com.sun.istack.tools.ProtectedTask".equals(caller)) { // NOI18N
            if ("sun.misc.ClassLoaderUtil".equals(accessTo)) { // NOI18N
                // calling ClassLoaderUtil is allowed
                return false;
            }
            return true;
        }
        return false;
    }
    
    public @Override void checkPermission(Permission perm) {
//        assert checkLogger(perm); //#178013 & JDK bug 1694855
        checkSetSecurityManager(perm);
        
        //
        // part of makeSwingUseSpecialClipboard that makes it work on
        // JDK 1.5
        //
        if (awtPermissionClass.isInstance(perm)) {
            if ("accessClipboard".equals (perm.getName ())) { // NOI18N
                ThreadLocal<Object> t;
                synchronized (TopSecurityManager.class) {
                    t = CLIPBOARD_FORBIDDEN;
                }
                if (t == null) {
                    return;
                }
                
                if (t.get () != null) {
                    t.set (this);
                    throw new SecurityException ();
                } else {
                    checkWhetherAccessedFromSwingTransfer ();
                }
            }
            if ("showWindowWithoutWarningBanner".equals(perm.getName())) { // NOI18N
                checkTopLevelWindow(perm, null);
            }
        }
        return;
    }
    
    public @Override void checkPermission(Permission perm, Object context) {
//        assert checkLogger(perm); //#178013 & JDK bug 1694855
        checkSetSecurityManager(perm);
        return;
    }

    private boolean checkLogger(Permission perm) {
        //Do not allow foreign code to replace NetBeans logger with its own
        //(particularly java.util.logging.FileLogger, which will deadlock)
        //see http://netbeans.org/bugzilla/show_bug.cgi?id=178013
        if (LoggingPermission.class.isInstance(perm)) {
            //This code will run every time a logger is created;  if this
            //proves too performance-degrading, replace the assertion test
            //with a system property so that mysterious logger-related deadlocks
            //can still be done, but leave it off by default
            Throwable t = new Exception().fillInStackTrace();
            for (StackTraceElement e : t.getStackTrace()) {
                //Currently no other reliable way to determine that the call
                //is to reset the logging infrastructure, not just create
                //a logger - see JDK bug 1694855
                if ("java.util.logging.LogManager".equals(e.getClassName()) && "reset".equals(e.getMethodName())) { //NOI18N
                    SecurityException se = new SecurityException("Illegal attempt to reset system logger"); //NOI18N
                    throw se;
                }
                if ("java.util.logging.LogManager".equals(e.getClassName()) && "readConfiguration".equals(e.getMethodName())) { //NOI18N
                    SecurityException se = new SecurityException("Illegal attempt to replace system logger configuration"); //NOI18N
                    throw se;
                }
            }
        }
        return true;
    }
    
    public static void install() {
        try {
            System.setSecurityManager(new TopSecurityManager());
        } catch (SecurityException ex) {
            LOG.log(Level.WARNING, "Cannot associated own security manager"); // NOI18N
            LOG.log(Level.INFO, "Cannot associated own security manager", ex); // NOI18N
        }
    }
    static void uninstall() {
        System.setSecurityManager(null);
    }
    
    /** Prohibits to set another SecurityManager */
    private void checkSetSecurityManager(Permission perm) {
        if (runtimePermissionClass.isInstance(perm)) {
            if (perm.getName().equals("setSecurityManager")) { // NOI18N - hardcoded in java.lang
                if (!check) {
                    return;
                }
                Class<?>[] arr = getClassContext();
                boolean seenJava = false;
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i].getName().equals("org.netbeans.TopSecurityManager")) { // NOI18N
                        if (seenJava) {
                            // if the change of security manager is called from my own
                            // class or the class loaded by other classloader, then it is likely ok
                            return;
                        } else {
                            continue;
                        }
                    }
                    if (arr[i] != System.class) {
                        // if there is a non-java class on stack, skip and throw exception
                        break;
                    }
                    seenJava = true;
                }
                throw new SecurityException();
            }
        }
    }

//    
//    public void checkMemberAccess(Class clazz, int which) {
//        if ((which == java.lang.reflect.Member.PUBLIC) ||
//                javax.swing.text.JTextComponent.class.isAssignableFrom(clazz)) {
//            return;
//        } else {
//            super.checkMemberAccess(clazz, which);
//        }
//    }
//
    private Class getInsecureClass() {

        Class<?>[] ctx = getClassContext();
        boolean firstACClass = false;

LOOP:   for (int i = 0; i < ctx.length; i++) {

            if (ctx[i] == accessControllerClass) {
                // privileged action is on the stack before an untrusted class loader
                // #3950
                if (firstACClass) {
                    return null;
                } else {
                    firstACClass = true;
                    continue LOOP;
                }
            } else if (ctx[i].getClassLoader() != null) {

                if (isSecureClass(ctx[i])) {
                    if (classLoaderClass.isAssignableFrom(ctx[i])) {
                        return null;
                    } else {
                        // OK process next one
                        continue LOOP;
                    }
                }

                return ctx[i];
            } else if (classLoaderClass.isAssignableFrom(ctx[i])) { // cloader == null
                return null; // foreign classloader wants to do work...
            }
        }

        return null;
    }

    /** Checks if the class is loaded through the nbfs URL */
    static boolean isSecureClass(final Class clazz) {
        URL source = getClassURL(clazz);
        if (source != null) {
            return isSecureProtocol(source.getProtocol());
        } else {
            return true;
        }
    }
    
    /** @return a protocol through which was the class loaded (file://...) or null
    */
    static URL getClassURL(Class clazz) {
        java.security.CodeSource cs = clazz.getProtectionDomain().getCodeSource();                                                     
        if (cs != null) {
            URL url = cs.getLocation();
            return url;
        } else { // PROXY CLASS?
            return null;
        }
    }

    static Field getUrlField(Class clazz) {
        if (urlField == null) {
            try {
                Field[] fds = clazz.getDeclaredFields();
                for (int i = 0; i < fds.length; i++) {
                    if (fds[i].getType() == URLClass) {
                        fds[i].setAccessible(true);
                        urlField = fds[i];
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return urlField;
    }

    private static Field urlField;

    /** @return Boolean.TRUE iff the string is a safe protocol (file, nbfs, ...) */
    static boolean isSecureProtocol(String protocol) {
        if (protocol.equals("http") || // NOI18N
            protocol.equals("ftp") || // NOI18N
            protocol.equals("rmi")) { // NOI18N
            return false;
        } else {
            return true;
        }
    }

    // Workaround for bug 
    // 
    // http://developer.java.sun.com/developer/bugParade/bugs/4818143.html
    //
    // sun.awt.datatransfer.ClipboardTransferable.getClipboardData() can hang
    // for very long time (maxlong == eternity).  We tries to avoid the hang by
    // access the system clipboard from a separate thread.  If the hang happens
    // the thread will wait for the system clipboard forever but not the whole
    // IDE.  See also NbClipboard
    
    private static ThreadLocal<Object> CLIPBOARD_FORBIDDEN;
    
    /** Convinces Swing components that they should use special clipboard
     * and not Toolkit.getSystemClipboard.
     *
     * @param clip clipboard to use
     */
    public static void makeSwingUseSpecialClipboard (java.awt.datatransfer.Clipboard clip) {
        try {
            synchronized (TopSecurityManager.class) {
                if (! (System.getSecurityManager() instanceof TopSecurityManager)) {
                    LOG.warning("Our manager has to be active: " + System.getSecurityManager());
                    return;
                } // NOI18N
                if (CLIPBOARD_FORBIDDEN != null) {
                    return;
                }
                CLIPBOARD_FORBIDDEN = new ThreadLocal<Object>();
                CLIPBOARD_FORBIDDEN.set (clip);
            }
            
            javax.swing.JComponent source = new javax.swing.JPanel ();
            javax.swing.TransferHandler.getPasteAction ().actionPerformed (
                new java.awt.event.ActionEvent (source, 0, "")
            );
            javax.swing.TransferHandler.getCopyAction ().actionPerformed (
                new java.awt.event.ActionEvent (source, 0, "")
            );
            javax.swing.TransferHandler.getCutAction ().actionPerformed (
                new java.awt.event.ActionEvent (source, 0, "")
            );
            Object forb = CLIPBOARD_FORBIDDEN.get ();
            CLIPBOARD_FORBIDDEN.set(null);
            if (! (forb instanceof TopSecurityManager) ) {
                System.err.println("Cannot install our clipboard to swing components, TopSecurityManager is not the security manager: " + forb); // NOI18N
                return;
            }

            Class<?> appContextClass = ClassLoader.getSystemClassLoader().loadClass("sun.awt.AppContext"); // NOI18N
            Method getAppContext = appContextClass.getMethod ("getAppContext"); // NOI18N
            Object appContext = getAppContext.invoke (null, new Object[0]);
            
            Class<?> actionClass = javax.swing.TransferHandler.getCopyAction ().getClass ();
            java.lang.reflect.Field sandboxKeyField = actionClass.getDeclaredField ("SandboxClipboardKey"); // NOI18N
            sandboxKeyField.setAccessible (true);
            Object value = sandboxKeyField.get (null);
            
            Method put = appContextClass.getMethod ("put", Object.class, Object.class); // NOI18N
            put.invoke (appContext, new Object[] { value, clip });
        } catch (ThreadDeath ex) {
            throw ex;
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (CLIPBOARD_FORBIDDEN != null) {
                CLIPBOARD_FORBIDDEN.set (null);
            }
        }
    }
    
    /** Throws exception if accessed from javax.swing.TransferHandler class
     */
    private void checkWhetherAccessedFromSwingTransfer () throws SecurityException {
        boolean throwExc = false;
        for (Class<?> c : getClassContext()) {
            if (c.getName().equals("org.netbeans.editor.BaseCaret")) { // NOI18N
                return;
            }
            if (c.getName().equals("javax.swing.TransferHandler$TransferAction")) { // NOI18N
                throwExc = true;
            }
        }
        if (throwExc) {
            throw new SecurityException("All swing access to clipboard should be redirected to ExClipboard"); // NOI18N
        }
    }


    private static final class PrivilegedCheck implements PrivilegedExceptionAction<Object> {
        int action;
        TopSecurityManager tsm;
        
        // exit
        int status;
        AccessControlContext acc;

        // connect
        String host;
        int port;
        
        
        public PrivilegedCheck(int action, TopSecurityManager tsm) {
            this.action = action;
            this.tsm = tsm;
            
            if (action == 0) {
                acc = AccessController.getContext();
            }
        }
        
        public Object run() throws Exception {
            switch (action) {
                case 0 : 
                    tsm.checkExitImpl(status, acc);
                    break;
                case 1 :
                    tsm.checkConnectImpl(host, port);
                    break;
                default :
            }
            return null;
        }
        
        static void checkExit(int status, TopSecurityManager tsm) {
            PrivilegedCheck pea = new PrivilegedCheck(0, tsm);
            pea.status = status;
            check(pea);
        }
        
        static void checkConnect(String host, int port, TopSecurityManager tsm) {
            PrivilegedCheck pea = new PrivilegedCheck(1, tsm);
            pea.host = host;
            pea.port = port;
            check(pea);
        }
        
        private static void check(PrivilegedCheck action) {
            try {
                AccessController.doPrivileged(action);
            } catch (PrivilegedActionException e) {
                Exception orig = e.getException();
                if (orig instanceof RuntimeException) {
                    throw ((RuntimeException) orig);
                }
                orig.printStackTrace();
            }
        }
    }
    
}
