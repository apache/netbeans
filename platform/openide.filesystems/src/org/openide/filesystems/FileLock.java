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
package org.openide.filesystems;

import java.util.logging.Level;

/** Represents an acquired lock on a <code>FileObject</code>.
* Typical usage includes locking the file in the editor on first
* modification, and then using this object to ensure exclusive access when
* overwriting the file (saving) by using {@link FileObject#getOutputStream}.
* Also used for renames, deletes, &amp;c.
* <p>Note that such locks are only used to protect against concurrent write accesses,
* and are not used for read operations (i.e. they are <em>not</em> write-one-read-many locks).
* Normally this is sufficient protection. If you really need an atomic read, you may
* simply lock the file, perform the read, and unlock it when done. The file will still
* be protected against writes, although the read operation did not request a lock.
* <p>
* The {@code FileLock} implements {@link AutoCloseable}, so it can be created within
* try-with-resources resource clause and the lock will be released at the end of the try block.
* 
* @see FileObject
* @since 9.2 implements {@code AutoCloseable} interface.
* @author Petr Hamernik, Jaroslav Tulach, Ian Formanek
* @version 0.16, Jun 5, 1997
*
*/
public class FileLock implements AutoCloseable {
    // ========================= NONE file lock =====================================

    /** Constant that can be used in filesystems that do not support locking.
     * Represents a lock which is never valid.
    */
    public static final FileLock NONE = new FileLock() {

        /** @return false always. */
        @Override
        public boolean isValid() {
            return false;
        }
    };

    /** Determines if lock is locked or if it was released. */
    private boolean locked = true;
    protected Throwable lockedBy;

    public FileLock() {
        assert (lockedBy = new Throwable("Locked by:")) != null;  //NOI18N
    }

    // ===============================================================================
    //  This part of code could be used for monitoring of closing file streams.

    /*  public static java.util.HashMap locks = new java.util.HashMap();
      public FileLock() {
        locks.put(this, new Exception()); int size = locks.size();
        System.out.println ("locks:"+(size-1)+" => "+size);
      }
      public void releaseLock() {
        locked = false; locks.remove(this); int size = locks.size();
        System.out.println ("locks:"+(size+1)+" => "+size);
      } */

    //  End of the debug part
    // ============================================================================
    //  Begin of the original part

    /** Release this lock.
    * In typical usage this method will be called in a <code>finally</code> clause.
    */
    public void releaseLock() {
        locked = false;
    }

    /**
     * Releases the lock. Equivalent to {@link #releaseLock} call.
     */
    @Override
    public void close() {
        releaseLock();
    }
    
    //  End of the original part
    // ============================================================================

    /** Test whether this lock is still active, or released.
    * @return <code>true</code> if lock is still active
    */
    public boolean isValid() {
        return locked;
    }

    /** Finalize this object. Calls {@link #releaseLock} to release the lock if the program
    * for some reason failed to.
    */
    @Override
    public void finalize() {
        if(isValid()) {
            releaseLock();
            boolean assertOn = false;
            assert assertOn = true;
            if (assertOn) {
                StreamPool.LOG.log(Level.SEVERE, 
                    "Not released lock for file: " + toString() + " (trapped in finalizer)", lockedBy); // NOI18N
            }
        }
    }
}
