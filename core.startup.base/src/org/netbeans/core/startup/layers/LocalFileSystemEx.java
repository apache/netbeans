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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
        return l.toArray(new String[l.size()]);
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

    private static Set<String> getInvalid (Set names) {
        LOGGER.finest("133616 - checking invalid");
        HashSet<String> invalid = new HashSet<String>();
        Iterator i = names.iterator ();
        while (i.hasNext ()) {
            String name = (String) i.next ();
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
