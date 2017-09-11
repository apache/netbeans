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
* <p/>
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
