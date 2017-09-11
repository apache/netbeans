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

package org.netbeans.core.spi.multiview;

import java.awt.Image;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.util.HelpCtx;

/** Description of multi view element. Implementations should be lightweight
 * and fast. Creating heavyweight {@link org.netbeans.core.spi.multiview.MultiViewElement} instances asociated with
 * Swing visual representation should be done lazily in {@link #createElement} methods.
 * The implementing class should be serializable. For performance reasons,
 * don't include the element into serialization of the description. That one will be handled
 * separately when necessary.
 *
 * @author  Dafe Simonek, Milos Kleint
 */
@MimeLocation(subfolderName="MultiView")
public interface MultiViewDescription {
    
    /** Gets persistence type of multi view element, the TopComponent will decide
     * on it's onw persistenceType based on the sum of all it's elements.
     * {@link org.openide.windows.TopComponent#PERSISTENCE_ALWAYS} has higher priority than {@link org.openide.windows.TopComponent#PERSISTENCE_ONLY_OPENED}
     * and {@link org.openide.windows.TopComponent#PERSISTENCE_NEVER} has lowest priority.
     * The {@link org.openide.windows.TopComponent} will be stored only if at least one element requesting persistence
     * was made visible.
     */
    public int getPersistenceType();

    /** 
     * Gets localized display name of multi view element. Will be placed on the Element's toggle button.
     *@return localized display name
     */
    public String getDisplayName();
    
    /** 
     * Icon for the MultiViewDescription's multiview component. Will be shown as TopComponent's icon
     * when this element is selected.
     * @return the icon of multi view element, or null
     */
    public @CheckForNull Image getIcon();

    /** Get the help context of multi view element.
    */
    public HelpCtx getHelpCtx ();
    
    /**
     * A Description's contribution 
     * to unique {@link org.openide.windows.TopComponent}'s Id returned by <code>getID</code>. Returned value is used as starting
     * value for creating unique {@link org.openide.windows.TopComponent} ID for whole enclosing multi view
     * component.
     * Value should be preferably unique, but need not be.
     */
    public String preferredID();
    
    /** Creates and returns asociated multi view element. it is called just once during the lifecycle of the 
     * multiview component.
     * @return the multiview element associated with this description
     */
    public MultiViewElement createElement ();
    
}
