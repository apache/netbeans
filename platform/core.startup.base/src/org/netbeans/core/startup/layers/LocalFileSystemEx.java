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

package org.netbeans.core.startup.layers;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.*;

/** Extends LocalFileSystem by useful features. It is used as
 * delegates being part of SystemFileSystem.
 *
 * @author  Vita Stejskal
 */
public final class LocalFileSystemEx extends LocalFileSystem {

    /** name -> FileObject */
    private static final HashMap<String,FileObject> allLocks = new HashMap<String,FileObject> (7);
    private static final HashSet<String> pLocks = new HashSet<String> (7);
//    private static HashMap allThreads = new HashMap (7);
    private static final Logger LOGGER = Logger.getLogger(LocalFileSystemEx.class.getName());

    public static String[] getLocks() {
        /* Returns union of allLocks and pLocks. As side effect
         * it removes invalid locks from pLocks.
         */
        LinkedList<String> l = new LinkedList<String>();
        Set<String> pLocksCopy = new HashSet<String>();
        synchronized (allLocks) {
            l.addAll(allLocks.keySet());
            pLocksCopy.addAll(pLocks);
        }
        // must be called outside of synchronized block becuase it may lock (#133616)
        Set<String> invalid = getInvalid(pLocksCopy);
        pLocksCopy.removeAll(invalid);
        l.addAll(pLocksCopy);
        synchronized (allLocks) {
            pLocks.removeAll(invalid);
        }
        return l.toArray(new String[0]);
    }

    public static boolean hasLocks() {
        /* Returns true if either allLocks or pLocks are not empty. As side effect
         * it removes invalid locks from pLocks.
         */
        Set<String> pLocksCopy = new HashSet<String>();
        boolean allLocksEmpty;
        synchronized (allLocks) {
            allLocksEmpty = allLocks.isEmpty();
            pLocksCopy.addAll(pLocks);
        }
        // must be called outside of synchronized block becuase it may lock (#133616)
        Set<String> invalid = getInvalid(pLocksCopy);
        pLocksCopy.removeAll(invalid);
        synchronized (allLocks) {
            pLocks.removeAll(invalid);
        }
        return !allLocksEmpty || !pLocksCopy.isEmpty();
    }
    
    public static void potentialLock (String name) {
        synchronized (allLocks) {
            pLocks.add (name);
        }
    }
    
    public static void potentialLock (String o, String n) {
        synchronized (allLocks) {
            if (pLocks.remove (o)) {
                pLocks.add (n);
            }
        }
    }

    private static Set<String> getInvalid (Set<String> names) {
        LOGGER.finest("133616 - checking invalid");
        Set<String> invalid = new HashSet<String>();
        Iterator<String> i = names.iterator();
        while (i.hasNext ()) {
            String name = i.next();
            FileObject fo = FileUtil.getConfigFile(name);
            if (null == fo || !fo.isLocked()) {
                // file lock recorded in potentialLock has been used
                // in operation which masked file as hidden and nothing
                // was actually locked
                invalid.add(name);
            }
        }
        return invalid;
    }

    /** Creates new LocalFileSystemEx */
    public LocalFileSystemEx () {
        this( false );
    }
    
    /**
     * @since 1.8
     */
    LocalFileSystemEx( boolean supportRemoveWritablesAttr ) {
        if( supportRemoveWritablesAttr ) {
            attr = new DelegatingAttributes( attr );
        }
    }

    protected @Override void lock(String name) throws IOException {
        LOGGER.finest("133616 - in lock");
        super.lock (name);
        synchronized (allLocks) {
            FileObject fo = findResource (name);
            allLocks.put (name, fo);
            pLocks.remove (name);
//            allThreads.put (name, new Throwable ("LocalFileSystemEx.lock() is locking file: " + name));
        }
    }    
    
    protected @Override void unlock(String name) {
        synchronized (allLocks) {
            if (allLocks.containsKey (name)) {
                allLocks.remove (name);
//                allThreads.remove (name);
            } else {
                FileObject fo = findResource (name);
                if (fo != null) {
                    for (Map.Entry<String,FileObject> entry : allLocks.entrySet()) {
                        if (fo.equals (entry.getValue ())) {
                            allLocks.remove (entry.getKey ());
//                            allThreads.remove (entry.getKey ());
                            break;
                        }
                    }
                } else {
                    Logger.getLogger(LocalFileSystemEx.class.getName()).log(Level.WARNING, null,
                                      new Throwable("Can\'t unlock file " + name +
                                                    ", it\'s lock was not found or it wasn\'t locked."));
                }
            }
        }
        super.unlock (name);
    }
    
    private class DelegatingAttributes implements AbstractFileSystem.Attr {
        
        private AbstractFileSystem.Attr a;
        
        public DelegatingAttributes( AbstractFileSystem.Attr a ) {
            this.a = a;
        }

        public Object readAttribute(String name, String attrName) {
            if( "removeWritables".equals( attrName ) ) { // NOI18N
                return new WritableRemover( name );
            }
            return a.readAttribute( name, attrName );
        }

        public void writeAttribute(String name, String attrName, Object value) throws IOException {
            a.writeAttribute( name, attrName, value );
        }

        public Enumeration<String> attributes(String name) {
            return a.attributes( name );
        }

        public void renameAttributes(String oldName, String newName) {
            a.renameAttributes(oldName, newName);
        }

        public void deleteAttributes(String name) {
            a.deleteAttributes( name );
        }
    }

    private class WritableRemover implements Callable<Void> {
        private String name;
        public WritableRemover( String name ) {
            this.name = name;
        }
        
        @Override public Void call() throws IOException {
            FileObject fo = findResource( name );
            if( null != fo ) {
                fo.delete();
            }
            return null;
        }
        
    }
}
