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
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
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

package org.netbeans.spi.settings;

import org.openide.util.Lookup;

/** Convertor allows to read/write objects in own format and notify about
 * object changes.
 *
 * @author  Jan Pokorsky
 */
public abstract class Convertor {

    /** Subclasses can implement own storing format.
     * @param w stream into which inst is written
     * @param inst the setting object to be written
     * @exception IOException if the object cannot be written
     */
    public abstract void write (java.io.Writer w, Object inst) throws java.io.IOException;

    /** Subclasses have to be able to read format implemented by {@link #write}.
     * @param r stream containing stored object
     * @return the read setting object
     * @exception IOException if the object cannot be read
     * @exception ClassNotFoundException if the object class cannot be resolved
     */
    public abstract Object read (java.io.Reader r) throws java.io.IOException, ClassNotFoundException;
    
    /** register {@link Saver saver}; convertor can provide own policy notifing
     * the saver about changes of setting object. (e.g. register property
     * change listener)
     * @param inst setting object
     * @param s saver implementation
     */
    public abstract void registerSaver (Object inst, Saver s);
    
    /** unregister {@link Saver saver}
     * @param inst setting object
     * @param s saver implementation
     * @see #registerSaver
     */
    public abstract void unregisterSaver (Object inst, Saver s);
    
    /** get a context associated with the reader <code>r</code>. It can contain
     * various info like a file location of the read object etc.
     * @param r stream containing stored object
     * @return a context associated with the reader
     * @since 1.2
     */
    protected static org.openide.util.Lookup findContext(java.io.Reader r) {
        if (r instanceof Lookup.Provider) {
            return ((Lookup.Provider) r).getLookup();
        } else {
            return Lookup.EMPTY;
        }
    }
    
    /** get a context associated with the writer <code>w</code>. It can contain
     * various info like a file location of the written object etc.
     * @param w stream into which inst is written
     * @return a context associated with the reader
     * @since 1.2
     */
    protected static org.openide.util.Lookup findContext(java.io.Writer w) {
        if (w instanceof Lookup.Provider) {
            return ((Lookup.Provider) w).getLookup();
        } else {
            return Lookup.EMPTY;
        }
    }
    
}
