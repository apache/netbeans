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

package org.netbeans.spi.navigator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.swing.JComponent;
import org.openide.util.Lookup;

/** Navigation related view description.
 *
 * Implementors of this interface will be plugged into Navigator UI.
 * @see Registration
 * @author Dafe Simonek
 */
public interface NavigatorPanel {

    /** Name of the view which will be shown in navigator UI.
     *
     * @return Displayable name of the view
     */
    public String getDisplayName ();

    /** Description of the view, explaining main purpose of the view.
     * <em>Currently unused.</em>
     * 
     * @return String description of the view.
     */
    public String getDisplayHint ();
    
    /** JComponent representation of this view. System will ask
     * multiple times and it is strongly recommended to
     * return the same JComponent instance each call for performance
     * reasons.<p>
     *
     * This method is always called in event dispatch thread. 
     * 
     * @return JComponent representation of this view.
     */
    public JComponent getComponent ();
    
    /** Called when this panel's component is about to being displayed.
     * Right place to attach listeners to current navigation data context,
     * as clients are responsible for listening to context changes when active
     * (in the time between panelActivated - panelDeactivated calls). 
     *
     * This method is always called in event dispatch thread. 
     *
     * @param context Lookup instance representing current context to take
     * data from
     */
    public void panelActivated (Lookup context);
    
    /** Called when this panel's component is about to being hidden.
     * Right place to detach, remove listeners from data context, that 
     * were added in panelActivated impl.
     *
     * This method is always called in event dispatch thread. 
     */
    public void panelDeactivated ();
    
    
    /** Returns Lookup that will be integrated into Lookup of Navigator UI
     * TopComponent. Allows clients to achieve several things:<p></p>
     * 
     * 1) Specify activated Node of Navigator UI TopComponent when this panel is active.<br></br>
     * If returned lookup contains Nodes, they will act as activated nodes of 
     * Navigator UI TopComponent (navigator window).<br></br>
     * 
     * Method may return null, signalizing that default mechanism should be enabled.
     * Default mechanism chooses first Node from Utilities.actionsGlobalContext()
     * (current system-wide activated Node) as activated Node for Navigator's TopComponent.
     * <p></p>
     * 
     * 2) Enable support for Toolbar or main menu global actions in NavigatorPanel.<br></br>
     * ActionMap contained in this Lookup will be part of Utilities.actionsGlobalContext()
     * when Navigator UI TopComponent (navigator window) is active in the system. 
     *   
     * @return Lookup instance or null
     */
    public Lookup getLookup ();

    /**
     * Registers a navigator panel.
     * @since 1.22
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    @interface Registration {

        /**
         * MIME type to register under.
         * For example: {@code text/javascript}
         */
        String mimeType();

        /**
         * Optional position for this panel among others of the same type.
         */
        int position() default Integer.MAX_VALUE;

        /**
         * Label for this view to be used in a switcher UI.
         * Will be replaced with {@link NavigatorPanel#getDisplayName}
         * if and when the panel is actually shown.
         * May use {@code pkg.Bundle#key} or {@code #key} syntax.
         */
        String displayName();

    }
    
    /**
     * Used in case multiple registrations are needed in one place.
     * @since 1.22
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    @interface Registrations {
        Registration[] value();
    }

}
