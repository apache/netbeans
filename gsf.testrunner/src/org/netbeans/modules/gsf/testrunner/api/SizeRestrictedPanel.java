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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004 Sun
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

package org.netbeans.modules.gsf.testrunner.api;

import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.JPanel;

/**
 * Panel whose dimension can be restricted by its preferred size.
 * It is supposed to be used in containers using <code>BoxLayout</code>.
 *
 * @see  javax.swing.BoxLayout  BoxLayout
 * @author  Marian Petras
 */
public class SizeRestrictedPanel extends JPanel {

    /** whether the panel's width is restricted */
    private final boolean widthRestriction;
    /** whether the panel's height is restricted */
    private final boolean heightRestriction;
    
    /**
     * Creates a panel with flow layout, restricted in both directions.
     */
    public SizeRestrictedPanel() {
        this(true, true);
    }
    
    /**
     * Creates a panel with flow layout, with width and/or height restricted.
     *
     * @param  widthRestriction  whether the panel's width should be restricted
     * @param  heightRestriction whether the panel's height should be restricted
     */
    public SizeRestrictedPanel(boolean widthRestriction,
                               boolean heightRestriction) {
        super();
        this.widthRestriction = widthRestriction;
        this.heightRestriction = heightRestriction;
    }
    
    /**
     * Creates a panel with the specified layout manager and with size
     * restricted in both directions.
     *
     * @param  layoutMgr  layout manager for this panel
     */
    public SizeRestrictedPanel(LayoutManager layoutMgr) {
        this(layoutMgr, true, true);
    }
    
    /**
     * Creates a panel with the specified layout manager and with width and/or
     * height restricted.
     *
     * @param  layoutMgr  layout manager for this panel
     * @param  widthRestriction  whether the panel's width should be restricted
     * @param  heightRestriction whether the panel's height should be restricted
     */
    public SizeRestrictedPanel(LayoutManager layoutMgr,
                               boolean widthRestriction,
                               boolean heightRestriction) {
        super(layoutMgr);
        this.widthRestriction = widthRestriction;
        this.heightRestriction = heightRestriction;
    }
    
    /**
     * Returns maximum size of this panel.
     * The maximum size can be restricted in width, height or in both
     * directions, depending on parameters passed to the constructor.
     *
     * @return  dimension returned from original <code>getMaximumSize()</code>
     *          and then modified according to restrictions specified
     *          by the constructor's parameters
     */
    public Dimension getMaximumSize() {
        if (widthRestriction && heightRestriction) {    //both true
            return getPreferredSize();
        }
        if (widthRestriction == heightRestriction) {    //both false
            return super.getMaximumSize();
        }
        
        Dimension maximumSize = super.getMaximumSize();
        if (widthRestriction) {
            maximumSize.width = getPreferredSize().width;
        } else {
            maximumSize.height = getPreferredSize().height;
        }
        return maximumSize;
    }
    
}
