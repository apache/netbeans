/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
    private static volatile int scaledMaxWidth = 0;
    
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
