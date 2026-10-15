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

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import static javax.swing.plaf.metal.MetalLookAndFeel.getCurrentTheme;
import static javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme;
import org.openide.util.NbBundle;

/**
 * Dark-themed Metal look and feel.
 * 
 */
public class DarkMetalLookAndFeel extends MetalLookAndFeel {

    @Override
    public String getName() {
        return NbBundle.getMessage(DarkMetalLookAndFeel.class, "LBL_DARK_METAL");
    }

    @Override
    protected void createDefaultTheme() {
        super.createDefaultTheme();
        if( !(getCurrentTheme() instanceof DarkMetalTheme) )
            setCurrentTheme( new DarkMetalTheme() );
    }

    @Override
    public UIDefaults getDefaults() {
//        if( !(getCurrentTheme() instanceof DarkMetalTheme) )
//            setCurrentTheme( new DarkMetalTheme() );
        UIDefaults defaults = super.getDefaults(); //To change body of generated methods, choose Tools | Templates.
//        new DarkMetalTheme().addCustomEntriesToTable( defaults );
        return defaults;
    }

    @Override
    public void initialize() {
        super.initialize(); //To change body of generated methods, choose Tools | Templates.
//        setCurrentTheme( new DarkMetalTheme() );
//        new DarkMetalTheme().addCustomEntriesToTable( UIManager.getDefaults() );
    }


}
