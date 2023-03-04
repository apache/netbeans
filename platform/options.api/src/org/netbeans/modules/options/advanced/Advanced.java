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

package org.netbeans.modules.options.advanced;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * Contains information about Advanced Panel, and creates a new
 * instance of it.
 *
 * @author Jan Jancura
 */
public final class Advanced extends OptionsCategory {

    private OptionsPanelController controller;
    
    private static String loc (String key) {
        return NbBundle.getMessage (Advanced.class, key);
    }

    private static Icon icon;
    
    @Override
    public Icon getIcon () {
        if (icon == null)
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/options/resources/advanced.png", false);
        return icon;
    }
    
    public String getCategoryName () {
        return loc ("CTL_Advanced_Options");
    }

    public String getTitle () {
        return loc ("CTL_Advanced_Options_Title");
    }
    
    public String getDescription () {
        return loc ("CTL_Advanced_Options_Description");
    }

    public OptionsPanelController create () {
        if (controller == null) {
            controller = new AdvancedPanelController(OptionsDisplayer.ADVANCED);
        }
        return controller;
    }  
}
