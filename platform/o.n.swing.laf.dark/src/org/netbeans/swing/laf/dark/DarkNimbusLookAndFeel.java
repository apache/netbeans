/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.swing.laf.dark;

import java.awt.Color;
import javax.swing.UIDefaults;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import org.openide.util.NbBundle;

/**
 * Dark-themed Nimbus l&f
 * 
 */
public class DarkNimbusLookAndFeel extends NimbusLookAndFeel {

    @Override
    public String getName() {
        return NbBundle.getMessage(DarkNimbusLookAndFeel.class, "LBL_DARK_NIMBUS");
    }

    @Override
    public UIDefaults getDefaults() {
        UIDefaults res = super.getDefaults();
        res.put( "nb.dark.theme", Boolean.TRUE ); //NOI18N
        res.put( "nb.preferred.color.profile", "Norway Today"); //NOI18N
        return res;
    }
    
    @Override
    public Color getDerivedColor(String uiDefaultParentName, float hOffset, float sOffset, float bOffset, int aOffset, boolean uiResource) {
        float brightness = bOffset;
        if ((bOffset == -0.34509805f) && "nimbusBlueGrey".equals(uiDefaultParentName)) { //NOI18N
            //Match only for TreeHandle Color in Nimbus, workaround for #231953
            brightness = -bOffset; 
        }
        return super.getDerivedColor(uiDefaultParentName, hOffset, sOffset, brightness, aOffset, uiResource);
    }

    @Override
    public void initialize() {
        super.initialize();
        DarkNimbusTheme.install( this );
    }
}
