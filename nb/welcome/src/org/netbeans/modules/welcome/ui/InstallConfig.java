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

package org.netbeans.modules.welcome.ui;

import java.util.HashSet;
import java.util.Set;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 * Scan for installed top-level module kits.
 * 
 * @author S. Aubrecht
 */
class InstallConfig {
    private boolean ergonomics = false;
    private boolean somePacksDisabled = false;
    private Set<String> enabledPackNames = new HashSet<String>(10);
    private Set<String> availablePackNames = new HashSet<String>(10);

    private static final String javaSEPackName = "org.netbeans.modules.java.kit"; //NOI18N

    private static final String ergonomicsPackName = "org.netbeans.modules.ide.ergonomics"; //NOI18N
    
    private static final String[] packNames = new String[] {
        //java se
        "org.netbeans.modules.java.kit", //NOI18N
        //java web & ee
        "org.netbeans.modules.j2ee.kit", //NOI18N
        //java me
        "org.netbeans.modules.mobility.kit", //NOI18N
        //java ruby
        "org.netbeans.modules.ruby.kit", //NOI18N
        //c/c++
        "org.netbeans.modules.cnd.kit", //NOI18N
        //php
        "org.netbeans.modules.php.kit", //NOI18N
        //groovy
        "org.netbeans.modules.groovy.kit", //NOI18N
    };


    private InstallConfig() {
        for( ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class) ) {

            ergonomics = ergonomics || isErgonomicsPack(mi);

            if( !isPack(mi) )
                continue;

            if( mi.isEnabled() ) {
                enabledPackNames.add(mi.getCodeNameBase());
            } else {
                somePacksDisabled = true;
            }
            availablePackNames.add(mi.getCodeNameBase());
        }
    }

    private static InstallConfig theInstance;

    public static InstallConfig getDefault() {
        if( null == theInstance )
            theInstance = new InstallConfig();
        return theInstance;
    }

    public boolean isErgonomicsEnabled() {
        return ergonomics;
    }

    public boolean somePacksDisabled() {
        return somePacksDisabled;
    }

    public void setSomePacksDisabled(boolean somePacksDisabled) {
        this.somePacksDisabled = somePacksDisabled;
    }


    private boolean isPack( ModuleInfo mi ) {
        String moduleName = mi.getCodeNameBase();
        for( String pn : packNames ) {
            if( moduleName.startsWith(pn) )
                return true;
        }
        return false;
    }

    private boolean isErgonomicsPack( ModuleInfo mi ) {
        String moduleName = mi.getCodeNameBase();
        return moduleName.startsWith(ergonomicsPackName) && mi.isEnabled();
    }

    private boolean isPackEnabled(String prefName) {
        for( String name : enabledPackNames ) {
            if( name.contains(prefName) )
                return true;
        }
        return false;
    }
}
