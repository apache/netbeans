/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual.spi;

import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.openide.nodes.Node.PropertySet;

/**
 * This interface provides information about a remote component.
 * 
 * @author Martin Entlicher
 */
public interface ComponentInfo {
    
    /**
     * Provides the display name of the component.
     * @return The component display name.
     */
    String getDisplayName();

    /**
     * Return a variant of the component display name containing HTML markup
     * conforming to the limited subset of font-markup HTML supported by
     * the lightweight HTML renderer <code>org.openide.awt.HtmlRenderer</code>
     * (font color, bold, italic and strike-through supported; font
     * colors can be UIManager color keys if they are prefixed with
     * a ! character, i.e. <samp>&lt;font color='!controlShadow'&gt;</samp>).
     * Enclosing <samp>&lt;html&gt;</samp> tags are not needed. If returning non-null, HTML
     * markup characters that should be literally rendered must be
     * escaped (<samp>&gt;</samp> becomes <samp>&amp;gt;</samp> and so forth).
     * <p><strong>This method should return either an HTML display name
     * or null; it should not return the non-HTML display name.</strong>
     *
     * @see org.openide.awt.HtmlRenderer
     * @return a String containing conformant HTML markup which
     *  represents the display name, or <code>null</code>.
     */
    String getHtmlDisplayName();

    /**
     * Provides the actions that are available on the component.
     * @param context
     * @return 
     */
    Action[] getActions(boolean context);

    /**
     * Get the component bounds relative to it's parent component.
     * @return The component bounds.
     */
    Rectangle getBounds();

    /**
     * Get the component bounds relative to the window.
     * @return The component bounds.
     */
    Rectangle getWindowBounds();

    /**
     * Get property sets of the component.
     * @return The property sets.
     */
    PropertySet[] getPropertySets();

    /**
     * Get the list of sub-components.
     * @return The sub-components.
     */
    ComponentInfo[] getSubComponents();

    /**
     * Retrieves a component occupying the given position
     * @param x Horizontal position
     * @param y Vertical position
     * @return Returns a subcomponent residing at the given position or this component itself
     */
    ComponentInfo findAt(int x, int y);
    
    /**
     * Add a property change listener to listen on changes in component properties.
     * 
     * @param propertyChangeListener The property change listener
     */
    void addPropertyChangeListener(PropertyChangeListener propertyChangeListener);

    /**
     * Remove a property change listener.
     * 
     * @param propertyChangeListener The property change listener
     */
    void removePropertyChangeListener(PropertyChangeListener propertyChangeListener);

}
