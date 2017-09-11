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

package threaddemo.data;

import java.io.IOException;
import javax.swing.event.ChangeListener;
import org.w3c.dom.Document;
import threaddemo.locking.RWLock;

/**
 * Cookie for an object with a DOM tree.
 * @author Jesse Glick
 */
public interface DomProvider {

    /**
     * Prepare for parsing. If the DOM tree is not already
     * available, parsing will be initiated. To receive notification
     * of completion, attach a listener.
     */
    void start();

    /**
     * Get the parsed document (blocking as needed).
     * @throws IOException if it cannot be read or parsed
     */
    Document getDocument() throws IOException;
    
    /**
     * Set the parsed document.
     * @throws IOException if it cannot be written
     */
    void setDocument(Document d) throws IOException;
    
    /**
     * True if the parse is finished and OK (does not block except for lock).
     */
    boolean isReady();
    
    /**
     * Listen for changes in status.
     */
    void addChangeListener(ChangeListener l);

    /**
     * Stop listening for changes in status.
     */
    void removeChangeListener(ChangeListener l);
    
    /**
     * Lock on which to lock while doing things.
     */
    RWLock lock();
    
    /**
     * Do an isolated block of operations to the document (must be in the write lock).
     * During this block you may not call any other methods of this interface which
     * require the lock (in read or write mode), or this method itself; you may
     * only adjust the document using DOM mutations.
     * Changes will be fired, and any underlying storage recreated, only when the
     * block is finished (possibly with an error). Does not roll back partial blocks.
     */
    void isolatingChange(Runnable r);

}
