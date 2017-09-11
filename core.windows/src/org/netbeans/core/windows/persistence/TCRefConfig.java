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
 * Class of reference of TopComponent in mode config properties for communication
 * with persistence management.
 * It keeps data which are read/written from/in .wstcref xml file.
 *
 * @author  Peter Zavadsky
 */
public class TCRefConfig {

    /** Reference to TopComponent by its unique Id. */
    public String tc_id;

    /** Is TopComponent opened. */
    public boolean opened;

    public String previousMode;
    /** tab index in the previous mode */
    public int previousIndex;
    
    /** True if this TopComponent is docked when the editor is maximized, 
     * false (default) if it should slide out */
    public boolean dockedInMaximizedMode;
    /** True (default) if this TopComponent is docked in the default mode, 
     * false if it is slided out */
    public boolean dockedInDefaultMode;
    /** True if this TopComponent is maximized when slided-in (covers the whole main window) */
    public boolean slidedInMaximized;

    /** Creates a new instance of TCRefConfig */
    public TCRefConfig() {
        tc_id = ""; // NOI18N
        dockedInMaximizedMode = false;
        dockedInDefaultMode = true;
        slidedInMaximized = false;
        previousIndex = -1;
    }
    
    @Override
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TCRefConfig) {
            TCRefConfig tcRefCfg = (TCRefConfig) obj;
            return (tc_id.equals(tcRefCfg.tc_id)
                   && (opened == tcRefCfg.opened)
                   && (dockedInMaximizedMode == tcRefCfg.dockedInMaximizedMode)
                   && (dockedInDefaultMode == tcRefCfg.dockedInDefaultMode)
                   && (slidedInMaximized == tcRefCfg.slidedInMaximized)
                   && (previousIndex == tcRefCfg.previousIndex));
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int hash = 17;
        hash = 37 * hash + tc_id.hashCode();
        hash = 37 * hash + (opened ? 0 : 1);
        hash = 37 * hash + (dockedInMaximizedMode ? 0 : 1);
        hash = 37 * hash + (dockedInDefaultMode ? 0 : 1);
        hash = 37 * hash + (slidedInMaximized ? 0 : 1);
        hash = 37 * hash + previousIndex;
        return hash;
    }
    
    @Override
    public String toString () {
        return "TCRefConfig: tc_id=" + tc_id + ", opened=" + opened 
                + ", maximizedMode=" + dockedInMaximizedMode
                + ", defaultMode=" + dockedInDefaultMode
                + ", slidedInMaximized=" + slidedInMaximized
                + ", previousMode=" + previousMode;
    }
    
}
