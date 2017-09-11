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

package org.netbeans.core.api.multiview;

import java.util.TooManyListenersException;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.Lookup;

/** View element for multi view, provides the UI components to the multiview component.
 * Gets notified by the enclosing component about the changes in the lifecycle.
 *
 * @author  Milos Kleint
 */

// IS NOT PUBLIC, SHOULD NOT BE USED, ONLY IN EMERGENCY CASE!
final class MultiViewPerspectiveComponent {

    static {
        AccessorImpl.createAccesor();
    }
    
    private MultiViewElement element;
    
    MultiViewPerspectiveComponent(MultiViewElement elem) {
        element = elem;
    }
    
    // package private, access through Accessor
    MultiViewElement getElement() {
        return element;
    }

    /** Returns Swing visual representation of this multi view element. Should be relatively fast
     * and always return the same component.
     */
    public JComponent getVisualRepresentation () {
        return element.getVisualRepresentation();
    }
    
    /**
     * Returns the visual component with the multi view element's toolbar.Should be relatively fast as it's called
     * everytime the current perspective is switched.
     */
    public JComponent getToolbarRepresentation () {
        return element.getToolbarRepresentation();
    }
    
    /** Gets the actions which will appear in the popup menu of this component.
     * <p>Subclasses are encouraged to use add the default TopComponent actions to 
     * the array of their own. These are accessible by calling MultiViewElementCallback.createDefaultActions()
     * @return array of actions for this component
     */
    public Action[] getActions() {
        return element.getActions();
    }

    /**
     * Lookup for the MultiViewElement. Will become part of the TopComponent's lookup.
     * @return the lookup to use when the MultiViewElement is active.
     */
    public Lookup getLookup() {
        return element.getLookup();
    }
    
   
}
