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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.openide.awt;

import javax.swing.JTabbedPane;
import org.openide.util.Lookup;

/**
 * Factory class for TabbedPanes with closeable tabs.
 *
 * @author S. Aubrecht
 * @since 6.10
 */
public class TabbedPaneFactory {
    
    /**
     * Name of the property that is fired from the closeable tabbed pane
     * when the user clicks close button on a tab.
     */
    public static final String PROP_CLOSE = "close"; //NOI18N

    /**
     * To hide close button feature on specific tab, put value Boolean.TRUE
     * as a client property of your tab:<br>
     * <pre>
     * component.putClientProperty(TabbedPaneFactory.NO_CLOSE_BUTTON, Boolean.TRUE)
     * </pre>
     * @since 7.8
     */
    public static final String NO_CLOSE_BUTTON = "noCloseButton"; //NOI18N

    /**
     * @return TabbedPaneFactory instance from the global Lookup.
     */
    public static TabbedPaneFactory getDefault() {
        TabbedPaneFactory res = Lookup.getDefault().lookup( TabbedPaneFactory.class );
        if( null == res )
            return new TabbedPaneFactory();
        
        return res;
    }

    /**
     * The default implementation provides just the vanilla Swing JTabbedPane.
     * The actual implementation in core.windows provides a special subclass
     * with a small 'close' button in each tab.
     * @return A new TabbedPane instance.
     * @since 7.48
     */
    public JTabbedPane createTabbedPane() {
        return new JTabbedPane();
    }

    /**
     * Creates a special {@link JTabbedPane} that displays a small 'close' button in each tab.
     * When user clicks the close button a {@link java.beans.PropertyChangeEvent} is fired from the
     * tabbed pane. The property name is {@link #PROP_CLOSE} and the property
     * value is the inner component inside the clicked tab.
     * 
     * @return Special TabbedPane with closeable tabs.
     * @see TabbedPaneFactory#NO_CLOSE_BUTTON
     */
    public static JTabbedPane createCloseButtonTabbedPane() {
        return getDefault().createTabbedPane();
    }
}
