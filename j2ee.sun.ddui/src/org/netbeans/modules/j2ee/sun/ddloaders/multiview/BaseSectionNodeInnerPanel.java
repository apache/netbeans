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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview;

import java.awt.Dimension;
import java.awt.Font;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;


/**
 *
 * @author Peter Williams
 */
public class BaseSectionNodeInnerPanel extends SectionNodeInnerPanel {

    public final ResourceBundle commonBundle = ResourceBundle.getBundle(
       "org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle"); // NOI18N
    
    public final ResourceBundle customizerBundle = ResourceBundle.getBundle(
       "org.netbeans.modules.j2ee.sun.share.configbean.customizers.Bundle"); // NOI18N

    
    // Current descriptor version.
    protected final ASDDVersion version;
    
    // true if AS 8.0+ fields are visible.
    protected final boolean as80FeaturesVisible;
    
    // true if AS 8.1+ fields are visible.
    protected final boolean as81FeaturesVisible;

    // true if AS 9.0+ fields are visible.
    protected final boolean as90FeaturesVisible;
    

    public BaseSectionNodeInnerPanel(SectionNodeView sectionNodeView, final ASDDVersion version) {
        super(sectionNodeView);
        
        this.version = version;
        this.as80FeaturesVisible = ASDDVersion.SUN_APPSERVER_8_0.compareTo(version) <= 0;
        this.as81FeaturesVisible = ASDDVersion.SUN_APPSERVER_8_1.compareTo(version) <= 0;
        this.as90FeaturesVisible = ASDDVersion.SUN_APPSERVER_9_0.compareTo(version) <= 0;
    }
    
    // ------------------------------------------------------------------------
    //
    //  Abstract methods from SectionNodeInnerPanel
    //
    // ------------------------------------------------------------------------
    public void setValue(JComponent source, Object value) {
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

    public JComponent getErrorComponent(String errorId) {
        return null;
    }

    
    // ------------------------------------------------------------------------
    //
    //  Nicer default sizing behavior.
    //
    // ------------------------------------------------------------------------
    /** Return reasonable maximum size.  Usage of GridBagLayout + stretch fields
     *  in this panel cause the default maximum size behavior to too wide.
     */
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(getScaledMaxWidth(), super.getMaximumSize().height);
    }
    
    /** Return correct preferred size.  Multiline JLabels cause the default
     *  preferred size behavior to too wide.
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getMinimumSize().width, super.getPreferredSize().height);
    }
    
    /** MAXWIDTH is arbitrarily set to 600 pixels and this is good for the default
     *  fontsize of 12.  For larger font sizes (e.g. --fontsize 16 24, etc.) this
     *  needs to be scaled upwards (IZ 115372).  There is probably a better way
     *  to do this, but this works for now.
     */
    private volatile static int scaledMaxWidth = 0;
    
    protected int getScaledMaxWidth() {
        int smw = scaledMaxWidth;
        if(smw == 0) {
            smw = scaledWidth(CustomSectionNodePanel.MAX_WIDTH);
            scaledMaxWidth = smw;
        }
        return smw;
    }
    
    private int scaledWidth(int width) {
        Font f = getFont();
        if(f != null) {
            int fs = f.getSize();
            if(fs > 12) {
                width = width * fs / 12;
            }
        }
        return width;
    }
}
