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

package org.netbeans.spi.editor.errorstripe;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.editor.errorstripe.apimodule.SPIAccessor;

/**UpToDateStatus provider.
 *
 * TODO: better javadoc.
 *
 * @author Jan Lahoda
 */
public abstract class UpToDateStatusProvider {

    static {
        SPIAccessor.DEFAULT = new SPIAccessorImpl();
    }

    /**Name of property which should be fired when the up-to-date status changes.
     */
    public static final String PROP_UP_TO_DATE = "upToDate"; // NOI18N

    private PropertyChangeSupport pcs;
    
    /** Creates a new instance of MarkProvider */
    public UpToDateStatusProvider() {
        pcs = new PropertyChangeSupport(this);
    }
    
    /**Report whether the current annotations attached to the documents are up-to-date
     * (the meaning of up-to-date is left on the provider).
     *
     * If a provider does not provide this information, it should
     * always return {@link UpToDateStatus#UP_TO_DATE_OK} value.
     *
     * @return a value of the {@link UpToDateStatus} enum.
     *
     * @see UpToDateStatus#UP_TO_DATE_OK
     * @see UpToDateStatus#UP_TO_DATE_PROCESSING
     * @see UpToDateStatus#UP_TO_DATE_DIRTY
     *
     */
    public abstract UpToDateStatus getUpToDate();
    
    /**Register a {@link PropertyChangeListener}.
     *
     * @param l listener to register
     */
    /*package private*/ final void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    /**Unregister a {@link PropertyChangeListener}.
     *
     * @param l listener to register
     */
    /*package private*/ final void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    /**Fire property change event to all registered listener. Subclasses should call
     * this method when they need to fire the {@link java.beans.PropertyChangeEvent}
     * because property {@link #PROP_UP_TO_DATE} have changed.
     *
     * @param name name of the property ({@link #PROP_UP_TO_DATE})
     * @param old  previous value of the property or null if unknown
     * @param nue  current value of the property or null if unknown
     */
    protected final void firePropertyChange(String name, Object old, Object nue) {
        pcs.firePropertyChange(name, old, nue);
    }
    
}
