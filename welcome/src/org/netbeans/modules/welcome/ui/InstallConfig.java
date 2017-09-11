/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
