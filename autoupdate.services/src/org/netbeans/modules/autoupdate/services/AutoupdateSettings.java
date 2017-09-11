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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.services;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateSettings {

    private static final String PROP_OPEN_CONNECTION_TIMEOUT = "plugin.manager.connection.timeout"; // NOI18N
    public static final int DEFAULT_OPEN_CONNECTION_TIMEOUT = 30000;
    private static final String PROP_LAST_CHECK = "lastCheckTime"; // NOI18N
    
    private static final Logger err = Logger.getLogger (AutoupdateSettings.class.getName ());
    
    private AutoupdateSettings () {
    }

    public static void setLastCheck (Date lastCheck) {
        err.log (Level.FINER, "Set the last check to " + lastCheck);
        if (lastCheck != null) {
            getPreferences().putLong (PROP_LAST_CHECK, lastCheck.getTime ());
        } else {
            getPreferences().remove (PROP_LAST_CHECK);
        }
    }
    
    public static void setLastCheck (String updateProviderName, Date lastCheck) {
        err.log (Level.FINER, "Set the last check to " + lastCheck);
        if (lastCheck != null) {
            getPreferences().putLong (updateProviderName+"_"+PROP_LAST_CHECK, lastCheck.getTime ());
        } else {
            getPreferences().remove (updateProviderName+"_"+PROP_LAST_CHECK);//NOI18N
        }
    }
    
    
    private static Preferences getPreferences () {
        return NbPreferences.root ().node ("/org/netbeans/modules/autoupdate");
    }    
    
    public static int getOpenConnectionTimeout () {
        return getPreferences ().getInt (PROP_OPEN_CONNECTION_TIMEOUT, DEFAULT_OPEN_CONNECTION_TIMEOUT);
    }
}
