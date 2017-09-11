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


package org.netbeans.core.windows;


import org.netbeans.core.windows.persistence.GroupConfig;
import org.netbeans.core.windows.persistence.ModeConfig;
import org.netbeans.core.windows.persistence.TCRefConfig;
import org.netbeans.core.windows.persistence.WindowManagerConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Factory which can produce winsys configuration (<code>WindowManagerConfig</code>).
 * Used as a fallback configuration in case of persistence problems.
 *
 * @author  Peter Zavadsky
 */
abstract class ConfigFactory {

    // For shorten names.
    private static final int VERTICAL   = Constants.VERTICAL;
    private static final int HORIZONTAL = Constants.HORIZONTAL;
    
    /** Creates a new instance of Defaults */
    private ConfigFactory() {
    }
    
    
    public static WindowManagerConfig createDefaultConfig() {
        WindowManagerConfig wmc = new WindowManagerConfig();
        wmc.xJoined = 100;
        wmc.yJoined = 100;
        wmc.widthJoined = 800;
        wmc.heightJoined = 600;
        wmc.relativeXJoined = -1;
        wmc.relativeYJoined = -1;
        wmc.relativeWidthJoined = -1;
        wmc.relativeHeightJoined = -1;
        wmc.centeredHorizontallyJoined = false;
        wmc.centeredVerticallyJoined = false;
        wmc.maximizeIfWidthBelowJoined = -1;
        wmc.maximizeIfHeightBelowJoined = -1;
        
        wmc.editorAreaState = Constants.EDITOR_AREA_JOINED;
        wmc.editorAreaConstraints = createDefaultEditorAreaConstraints();
        wmc.editorAreaBounds = null;
        wmc.editorAreaRelativeBounds = null;
        
        wmc.screenSize = new Dimension(1024, 750);
        wmc.activeModeName = "editor"; // NOI18N
        wmc.editorMaximizedModeName = "";
        wmc.viewMaximizedModeName = "";
        wmc.toolbarConfiguration = "Standard"; // NOI18N
        wmc.preferredToolbarIconSize = 24;
        wmc.modes = createDefaultModeConfigs();
        wmc.groups = createDefaultGroupConfigs();
        return wmc;
    }
    
    /** @return list of <code>Item</code>S as constraints */
    private static SplitConstraint[] createDefaultEditorAreaConstraints() {
        return new SplitConstraint[] {
            new SplitConstraint(HORIZONTAL, 1, 0.75D),
            new SplitConstraint(VERTICAL,   0, 0.75D),
            new SplitConstraint(HORIZONTAL, 0, 0.75D)
        };
    }
    
    /** @return list of <code>ModeConfig</code>S. */
    private static ModeConfig[] createDefaultModeConfigs() {
        List<ModeConfig> l = new ArrayList<ModeConfig>();
        l.add(createDefaultExplorerModeConfig());
        l.add(createDefaultPropertiesModeConfig());
        l.add(createDefaultEditorModeConfig());
        l.add(createDefaultOutputModeConfig());
        l.add(createDefaultFormModeConfig());
        return l.toArray(new ModeConfig[0]);
    }
    
    private static ModeConfig createDefaultExplorerModeConfig() {
        ModeConfig mc = new ModeConfig();
        mc.name = "explorer"; // NOI18N
        mc.bounds = null;
        mc.relativeBounds = null;
        mc.frameState = -1;
        mc.state = Constants.MODE_STATE_JOINED;
        mc.kind = Constants.MODE_KIND_VIEW;
        mc.constraints = createDefaultExplorerConstraints();
        mc.selectedTopComponentID = "runtime"; // NOI18N
        mc.permanent = true;
        mc.tcRefConfigs = createDefaultExplorerTCRefConfigs();
        return mc;
    }

    /** @return list of <code>Item</code>S */
    private static SplitConstraint[] createDefaultExplorerConstraints() {
        return new SplitConstraint[] {
            new SplitConstraint(HORIZONTAL, 0, 0.30D),
            new SplitConstraint(VERTICAL,   0, 0.70D)
        };
    }
    
    private static TCRefConfig[] createDefaultExplorerTCRefConfigs() {
        List<TCRefConfig> tcRefConfigs = new ArrayList<TCRefConfig>();
        tcRefConfigs.add(createDefaultRuntimeTCRefConfig());
        return tcRefConfigs.toArray(new TCRefConfig[0]);
    }

    private static TCRefConfig createDefaultRuntimeTCRefConfig() {
        TCRefConfig tcrc = new TCRefConfig();
        tcrc.tc_id = "runtime"; // NOI18N
        tcrc.opened = true;
        return tcrc;
    }
    
    private static ModeConfig createDefaultPropertiesModeConfig() {
        ModeConfig mc = new ModeConfig();
        mc.name = "properties"; // NOI18N
        mc.bounds = null;
        mc.relativeBounds = null;
        mc.frameState = -1;
        mc.state = Constants.MODE_STATE_JOINED;
        mc.kind = Constants.MODE_KIND_VIEW;
        mc.constraints = createDefaultPropertiesConstraints();
        mc.selectedTopComponentID = "properties"; // NOI18N
        mc.permanent = true;
        mc.tcRefConfigs = createDefaultPropertiesTCRefConfigs();
        return mc;
    }
    
    /** @return list of <code>Item</code>S */
    private static SplitConstraint[] createDefaultPropertiesConstraints() {
        return new SplitConstraint[] {
            new SplitConstraint(HORIZONTAL, 0, 0.30D),
            new SplitConstraint(VERTICAL,   1, 0.30D)
        };
    }
    
    private static TCRefConfig[] createDefaultPropertiesTCRefConfigs() {
        List<TCRefConfig> tcRefConfigs = new ArrayList<TCRefConfig>();
        tcRefConfigs.add(createDefaultPropertiesTCRefConfig());
        return tcRefConfigs.toArray(new TCRefConfig[0]);
    }

    private static TCRefConfig createDefaultPropertiesTCRefConfig() {
        TCRefConfig tcrc = new TCRefConfig();
        tcrc.tc_id = "properties"; // NOI18N
        tcrc.opened = true;
        return tcrc;
    }
    
    private static ModeConfig createDefaultEditorModeConfig() {
        ModeConfig mc = new ModeConfig();
        mc.name = "editor";
        mc.bounds = null;
        mc.relativeBounds = null;
        mc.frameState = -1;
        mc.state = Constants.MODE_STATE_JOINED;
        mc.kind = Constants.MODE_KIND_EDITOR;
        mc.constraints = createDefaultEditorConstraints();
        mc.selectedTopComponentID = null;
        mc.permanent = true;
        mc.tcRefConfigs = createDefaultEditorTCRefConfigs();
        return mc;
    }

    /** @return list of <code>Item</code>S */
    private static SplitConstraint[] createDefaultEditorConstraints() {
        return new SplitConstraint[] {};
    }
    
    private static TCRefConfig[] createDefaultEditorTCRefConfigs() {
        List<TCRefConfig> tcRefConfigs = new ArrayList<TCRefConfig>();
        tcRefConfigs.add(createDefaultWelcomeTCRefConfig());
        return tcRefConfigs.toArray(new TCRefConfig[0]);
    }

    private static TCRefConfig createDefaultWelcomeTCRefConfig() {
        TCRefConfig tcrc = new TCRefConfig();
        tcrc.tc_id = "Welcome"; // NOI18N
        tcrc.opened = true;
        return tcrc;
    }

    private static ModeConfig createDefaultOutputModeConfig() {
        ModeConfig mc = new ModeConfig();
        mc.name = "output";
        mc.bounds = null;
        mc.relativeBounds = null;
        mc.frameState = -1;
        mc.state = Constants.MODE_STATE_JOINED;
        mc.kind = Constants.MODE_KIND_VIEW;
        mc.constraints = createDefaultOutputConstraints();
        mc.selectedTopComponentID = null;
        mc.permanent = true;
        mc.tcRefConfigs = createDefaultOutputTCRefConfigs();
        return mc;
    }
    
    /** @return list of <code>Item</code>S */
    private static SplitConstraint[] createDefaultOutputConstraints() {
        return new SplitConstraint[] {
            new SplitConstraint(HORIZONTAL, 1, 0.70D),
            new SplitConstraint(VERTICAL,   1, 0.20D),
            new SplitConstraint(HORIZONTAL, 0, 0.80D)
        };
    }
    
    private static TCRefConfig[] createDefaultOutputTCRefConfigs() {
        List<TCRefConfig> tcRefConfigs = new ArrayList<TCRefConfig>();
        tcRefConfigs.add(createDefaultOutputTCRefConfig());
        return tcRefConfigs.toArray(new TCRefConfig[0]);
    }

    private static TCRefConfig createDefaultOutputTCRefConfig() {
        TCRefConfig tcrc = new TCRefConfig();
        tcrc.tc_id = "output"; // NOI18N
        tcrc.opened = true;
        return tcrc;
    }
    
    private static ModeConfig createDefaultFormModeConfig() {
        ModeConfig mc = new ModeConfig();
        mc.name = "Form";
        mc.bounds = null;
        mc.relativeBounds = null;
        mc.frameState = -1;
        mc.state = Constants.MODE_STATE_JOINED;
        mc.kind = Constants.MODE_KIND_VIEW;
        mc.constraints = createDefaultFormConstraints();
        mc.selectedTopComponentID = "ComponentInspector"; // NOI18N
        mc.permanent = true;
        mc.tcRefConfigs = createDefaultFormTCRefConfigs();
        return mc;
    }

    /** @return list of <code>Item</code>S */
    private static SplitConstraint[] createDefaultFormConstraints() {
        return new SplitConstraint[] {
            new SplitConstraint(HORIZONTAL, 1, 0.70D),
            new SplitConstraint(VERTICAL,   0, 0.80D),
            new SplitConstraint(HORIZONTAL, 1, 0.50D)
        };
    }
    
    private static TCRefConfig[] createDefaultFormTCRefConfigs() {
        List<TCRefConfig> tcRefConfigs = new ArrayList<TCRefConfig>();
        tcRefConfigs.add(createDefaultComponentInspectorTCRefConfig());
        tcRefConfigs.add(createDefaultComponentPaletteTCRefConfig());
        return tcRefConfigs.toArray(new TCRefConfig[0]);
    }

    private static TCRefConfig createDefaultComponentInspectorTCRefConfig() {
        TCRefConfig tcrc = new TCRefConfig();
        tcrc.tc_id = "ComponentInspector"; // NOI18N
        tcrc.opened = true;
        return tcrc;
    }

    private static TCRefConfig createDefaultComponentPaletteTCRefConfig() {
        TCRefConfig tcrc = new TCRefConfig();
        tcrc.tc_id = "CommonPalette"; // NOI18N
        tcrc.opened = true;
        tcrc.dockedInMaximizedMode = true;
        return tcrc;
    }
    

    /** @return list of <code>GroupConfig</code>S. */
    private static GroupConfig[] createDefaultGroupConfigs() {
        List<GroupConfig> l = new ArrayList<GroupConfig>();
        // TODO
        return l.toArray(new GroupConfig[0]);
    }


}
