/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.diagnostics.clank.ui.tooltip;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.modules.cnd.diagnostics.clank.ui.views.ViewComponent;


/**
 * Factory that produces debugger views created from registered view models
 * (see {@link org.netbeans.spi.viewmodel.Model} and it's extension interfaces).
 * 
 * @since 2.34
 */
public class ViewFactory {
    
    private static ViewFactory vf;
    
    private ViewFactory() {}
    
    /**
     * Get the default implementation of view factory.
     * 
     * @return The view factory.
     */
    public static synchronized ViewFactory getDefault() {
        if (vf == null) {
            vf = new ViewFactory();
        }
        return vf;
    }
    

    /**
     * Create {@link JComponent} view from models registered under 'name' path.
     * @param icon The icon resource, possibly used by the button toolbar in the view
     * @param name Name of the view, under which are the models registered
     * @param helpID The helpID of the created TopComponent
     * @param propertiesHelpID The helpID of properties displayed in the view
     * @return Component containing the view created from registered models.
     */
    public JComponent createViewComponent(String icon, String name, String helpID, String propertiesHelpID) {
        JComponent c = new ViewComponent(icon, name, helpID, propertiesHelpID);
        return c;
    }
        
    
    /**
     * Create a tooltip with additional actions.
     * The tooltip can be expandable to a structured tooltip view, or pinnable as a pin watch.
     * @param toolTipText The text to display as a tooltip
     * @param expandable Description of an expanded tooltip,
     *                   or <code>null</code> when the tooltip is not expandable
     * @param pinnable Description of a pin watch created from this tooltip,
     *                 or <code>null</code> when the tooltip is not pinnable
     * @return A tooltip UI to be shown.
     * @since 2.54
     */
    public ToolTipUI createToolTip(String toolTipText,
                                   ToolTipUI.Expandable expandable,
                                   ToolTipUI.Pinnable pinnable, PropertyChangeListener l) {
        return new ToolTipUI(toolTipText, expandable, pinnable, l);
    }

}
