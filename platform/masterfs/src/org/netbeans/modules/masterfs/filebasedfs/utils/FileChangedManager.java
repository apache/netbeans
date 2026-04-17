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
package org.netbeans.modules.masterfs.filebasedfs.utils;

import java.io.File;
import java.security.Permission;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenSupport;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Radek Matous
 */
@ServiceProviders({
    @ServiceProvider(service=SecurityManager.class),
    @ServiceProvider(service=FileChangedManager.class)
})
public class FileChangedManager extends SecurityManager {
    private static final Logger LOG = Logger.getLogger(FileChangedManager.class.getName());
    private static final boolean isFine;
    private static final boolean isFiner;
    static {
        isFine = LOG.isLoggable(Level.FINE);
        isFiner = LOG.isLoggable(Level.FINER);
    }
    private static  FileChangedManager INSTANCE;
    private static final int CREATE_HINT = 2;
    private static final int DELETE_HINT = 1;
    private static final int AMBIGOUS_HINT = 3;

    private final ConcurrentHashMap<Integer,Integer> hints = new ConcurrentHashMap<Integer,Integer>();
    private long shrinkTime = System.currentTimeMillis();
    private static volatile long ioTime = -1;
    private static volatile int ioLoad;
    private static final AtomicInteger priorityIO = new AtomicInteger();
    private static final ThreadLocal<Integer> IDLE_IO = new ThreadLocal<Integer>();
    private static final ThreadLocal<Runnable> IDLE_CALL = new ThreadLocal<Runnable>();
    private static final ThreadLocal<AtomicBoolean> IDLE_ON = new ThreadLocal<AtomicBoolean>();
    
    public FileChangedManager() {
        if (isFine) {
            LOG.fine("Initializing FileChangedManager");
        }
    }
    
    public static synchronized FileChangedManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = Lookup.getDefault().lookup(FileChangedManager.class);
            if (INSTANCE == null) {
                // for test purposes
                INSTANCE = new FileChangedManager();
            }
        }
        return INSTANCE;
    }

    static void assertNoLock() {
        assert !Thread.holdsLock(IDLE_CALL);
        assert !Thread.holdsLock(IDLE_IO);
        assert !Thread.holdsLock(IDLE_ON);
    }

    @Override
    public void checkPermission(Permission perm) {
    }
    
    @Override
    public void checkDelete(String file) {
        put(file, false);
    }

    @Override
    public void checkWrite(String file) {
        put(file, true);
    }

    @Override
    public void checkRead(String file) {
        pingIO(1);
    }

    @Override
    public void checkRead(String file, Object context) {
        pingIO(1);
    }
        
    public boolean impeachExistence(File f, boolean expectedExixts) {
        Integer hint = remove(getKey(f));
        boolean retval = (hint == null) ? false : true;
        if (retval) {
            if (hint == AMBIGOUS_HINT) {
                return true;
            } else {
                retval = (expectedExixts != toState(hint));
            }
        }
        return retval;
    }    

    public boolean exists(File file) {
        long time = 0;
        assert (time = System.currentTimeMillis()) >= Long.MIN_VALUE;
        boolean retval = file.exists();
        if (time > 0) {
            time = System.currentTimeMillis() - time;
            if (time > 500) {
                Level l;
                String msg;
                if (isIdleIO()) {
                    l = Level.FINE;
                    msg = "{0} new File(\"{1}\").exists() in I/O mode";
                } else {
                    l = Level.WARNING;
                    msg = "{0} ms in new File(\"{1}\").exists()";
                }
                LOG.log(l, msg, new Object[]{time, file});
            }
        }
        Integer id = getKey(file);
        remove(id);
        put(id, retval);
        return retval;
    }

    public static <T> T priorityIO(Callable<T> callable) throws Exception {
        try {
            priorityIO.incrementAndGet();
            return callable.call();
        } finally {
            priorityIO.decrementAndGet();
        }
    }

    static boolean isIdleIO() {
        return IDLE_IO.get() != null;
    }

    /**
     * If invoked in {@link #idleIO(int, java.lang.Runnable,
     * java.lang.Runnable, java.util.concurrent.atomic.AtomicBoolean) idleIO},
     * wait until all priority IOs are completed and start {@link Runnable}
     * {@code run}. The {@code run} will not be blocked in FileChangedManager
     * even if another priority IO is started (this would cause deadlocks, see
     * bug 246893).
     */
    public static void waitNowAndRun(Runnable run) {
        Integer storedMaxLoad = IDLE_IO.get();
        if (storedMaxLoad != null) {
            try {
                waitIOLoadLowerThan(storedMaxLoad);
            } catch (InterruptedException ex) {
                if (isFine) {
                    LOG.log(Level.FINE, "Interrupted {0}", ex.getMessage());
                }
            }
            IDLE_IO.set(null);
            try {
                run.run();
            } finally {
                IDLE_IO.set(storedMaxLoad);
            }
        } else {
            run.run();
        }
    }

    public static void idleIO(int maximumLoad, Runnable r, Runnable goingToSleep, AtomicBoolean goOn) {
        Integer prev = IDLE_IO.get();
        Runnable pGoing = IDLE_CALL.get();
        AtomicBoolean pGoOn = IDLE_ON.get();
        int prevMax = prev == null ? 0 : prev;
        try {
            IDLE_IO.set(Math.max(maximumLoad, prevMax));
            IDLE_CALL.set(goingToSleep);
            IDLE_ON.set(goOn);
            r.run();
        } finally {
            IDLE_IO.set(prev);
            IDLE_CALL.set(pGoing);
            IDLE_ON.set(pGoOn);
        }
    }

    public static void waitIOLoadLowerThan(int load) throws InterruptedException {
        boolean checkClassLoading = true;
        for (;;) {
            AtomicBoolean goOn = IDLE_ON.get();
            if (goOn != null && !goOn.get()) {
                final String msg = "Interrupting manually"; // NOI18N
                if (isFine) LOG.fine(msg);
                throw new InterruptedException(msg);
            }
            int l = pingIO(0);
            if (l < load && priorityIO.get() == 0) {
                return;
            }
            if (checkClassLoading) { // Check class loading only once.
                checkClassLoading = false;
                if (isClassLoading()) {
                    return;
                }
            }
            if (ChildrenSupport.isLock() || Thread.holdsLock(NamingFactory.class)) {
                return;
            }
            Runnable goingToSleep = IDLE_CALL.get();
            if (goingToSleep != null) {
                goingToSleep.run();
            }
            synchronized (IDLE_IO) {
                IDLE_IO.wait(100);
            }
        }
    }

    private static int pingIO(int inc) {
        long ms = System.currentTimeMillis();
        boolean change = false;
        while (ioTime < ms) {
            ioTime += 100;
            ioLoad /= 2;
            change = true;
            if (ioLoad == 0) {
                ioTime = ms + 100;
                break;
            }
        }
        if (change) {
            synchronized (IDLE_IO) {
                IDLE_IO.notifyAll();
            }
        }
        if (inc == 0) {
            return ioLoad;
        }

        Integer maxLoad = IDLE_IO.get();
        if (maxLoad != null) {
            try {
                waitIOLoadLowerThan(maxLoad);
            } catch (InterruptedException ex) {
                if (isFine) LOG.log(Level.FINE, "Interrupted {0}", ex.getMessage());
            }
        } else {
            ioLoad += inc;
            if (isFiner) LOG.log(Level.FINER, "I/O load: {0} (+{1})", new Object[] { ioLoad, inc });
        }
        return ioLoad;
    }

    
    private Integer put(int id, boolean state) {
        pingIO(2);
        if (hints.size() > 150000) { // Clear cache if it gets too big. #242998
            shrink();
        }
        shrinkTime = System.currentTimeMillis();
        int val = toValue(state);
        Integer retval = hints.putIfAbsent(id,val);
        if (retval != null) {
            if (retval != AMBIGOUS_HINT && retval != val) {
                hints.put(id,AMBIGOUS_HINT);
            } 
        }                
        return retval;
    }
    
    private int toValue(boolean state) {
        return state ? CREATE_HINT : DELETE_HINT;
    }
    
    private boolean toState(int value) {
        switch(value) {
            case DELETE_HINT:
                return false;
            case CREATE_HINT:
                return true;
        }  
        return false;
    }
    
    private void shrink() {
        hints.keySet().clear();
    }
    
    private Integer remove(int id) {
        long now = System.currentTimeMillis();
        if ((now - shrinkTime) > 5000) {
            int size = hints.size();
            if (size > 1500) {
                shrink();
            }
            shrinkTime = now;
        }
        return hints.remove(id);
    }                
    
    private static int getKey(File f) {
        return NamingFactory.createID(f).value();
    }
    private static int getKey(String f) {
        return getKey(new File(f));
    }  

    private Integer put(String f, boolean value) {
        return put(getKey(f), value);
    }
    
    private static boolean isClassLoading() {
        StackTraceElement[] arr = new Throwable().getStackTrace();
        for (StackTraceElement e : arr) {
            if (
                e.getClassName().startsWith("org.netbeans.JarClassLoader")
                ||
                e.getClassName().startsWith("org.netbeans.ProxyClassLoader")
                ||
                e.getClassName().equals("java.lang.ClassLoader")
            ) {
                return true;
            }
        }
        return false;
    }
}
