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


package org.netbeans.core.windows.persistence;





/**
 * Class of group config properties for communication with persistence management.
 * It keeps data which are read/written from/in .wsgrp xml file.
 *
 * @author  Peter Zavadsky
 */
public class GroupConfig {

    /** Unique name of group. */
    public String name;

    /** Is group opened or not. */
    public boolean opened;


    /** Array of TCGroupConfigs */
    public TCGroupConfig[] tcGroupConfigs;

    /** Creates a new instance of GroupConfig */
    public GroupConfig() {
        name = ""; // NOI18N
        tcGroupConfigs = new TCGroupConfig[0];
    }
    
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GroupConfig)) {
            return false;
        }
        GroupConfig groupCfg = (GroupConfig) obj;
        if (!name.equals(groupCfg.name)) {
            return false;
        }
        if (opened != groupCfg.opened) {
            return false;
        }
        //Order of tcGroupConfigs array is NOT defined
        if (tcGroupConfigs.length != groupCfg.tcGroupConfigs.length) {
            return false;
        }
        for (int i = 0; i < tcGroupConfigs.length; i++) {
            TCGroupConfig tcGroupCfg = null;
            for (int j = 0; j < groupCfg.tcGroupConfigs.length; j++) {
                if (tcGroupConfigs[i].tc_id.equals(groupCfg.tcGroupConfigs[j].tc_id)) {
                    tcGroupCfg = groupCfg.tcGroupConfigs[j];
                    break;
                }
            }
            if (tcGroupCfg == null) {
                return false;
            }
            if (!tcGroupConfigs[i].equals(tcGroupCfg)) {
                return false;
            }
        }
        return true;
    }
    
    public int hashCode() {
        int hash = 17;
        hash = 37 * hash + name.hashCode();
        hash = 37 * hash + (opened ? 0 : 1);
        for (int i = 0; i < tcGroupConfigs.length; i++) {
            hash = 37 * hash + tcGroupConfigs[i].hashCode();
        }
        return hash;
    }
    
}
