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

package org.netbeans.spi.editor.hints;

import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * A list of fixes that allows lazy computation of the fixes for an error.
 *
 * @author Jan Lahoda
 */
public interface LazyFixList {

    /**
     * PropertyChangeEvent with this name is fired when the list
     * of the fixes is changed.
     */
    public static final String PROP_FIXES = "fixes";

    /**
     * PropertyChangeEvent with this name is fired when the list
     * of the fixes is computed - no more changes are to be expeted
     * after this.
     */
    public static final String PROP_COMPUTED = "computed";

    /**
     * The registered PropertyChangeListener will recieve events
     * with names PROP_COMPUTED and PROP_FIXES.
     * @param l the listener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Allows to unregister a PropertyChangeListener.
     * @param l the listener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Should return false if there will not be any fixes in the list for sure.
     * Should return true otherwise.
     * Should run very fast - should not try to actualy compute the fixes.
     * 
     * @return false if this list will never contain any fixes, true otherwise.
     */
    public boolean probablyContainsFixes();

    /**
     * Getter for the current list of fixes.
     * @return the list of fixes
     */
    public List<Fix> getFixes();
    
    /**
     * Returns true if the list of fixes will not changed anymore (it is computed).
     *
     * @return true if the list of fixes is computed.
     */
    public boolean isComputed();
    
}
