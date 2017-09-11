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

package threaddemo.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import threaddemo.locking.RWLock;

/**
 * Really, a file.
 * Mutator methods (rename, delete, create*Phadhail) cannot be called
 * from within a listener callback, or generally without read access.
 * You *can* add/remove listeners from within a listener callback however,
 * or in fact at any other time (without even a lock).
 * Methods from java.lang.Object (toString, hashCode, equals) can be called at any time.
 * @author Jesse Glick
 */
public interface Phadhail {
    
    /** will be simple file name */
    String getName();
    
    /** will be full path */
    String getPath();
    
    /** rename (within parent) */
    void rename(String nue) throws IOException;
    
    /** will be true if a directory */
    boolean hasChildren();
    
    /**
     * Get a list of child files.
     * caller cannot mutate list, and it might not be thread-safe
     * implementor cannot change list after creation (i.e. size & identity of elements)
     * it is expected that once the list is obtained, asking for elements is fast and nonblocking
     * (and then the read lock is not required)
     */
    List<Phadhail> getChildren();
    
    /** delete this phadhail (must not have children) */
    void delete() throws IOException;
    
    /** make a new phadhail without children */
    Phadhail createLeafPhadhail(String name) throws IOException;
    
    /** make a new phadhail with children */
    Phadhail createContainerPhadhail(String name) throws IOException;
    
    /** read */
    InputStream getInputStream() throws IOException;
    
    /** write (note: in this simple model, no locks here) */
    OutputStream getOutputStream() throws IOException;
    
    /** add a listener */
    void addPhadhailListener(PhadhailListener l);
    
    /** remove a listener */
    void removePhadhailListener(PhadhailListener l);
    
    /**
     * Get a lock appropriate for locking operations from another thread.
     * Should be a single lock for a whole tree of phadhails.
     * Model methods should automatically acquire the relevant lock for you;
     * the view need not bother, unless it needs to do an atomic operation.
     */
    RWLock lock();
    
}
