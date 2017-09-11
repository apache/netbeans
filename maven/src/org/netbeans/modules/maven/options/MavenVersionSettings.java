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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.options;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Preferences class for externalizing the hardwired plugin versions to
 * allow changes by advanced users?
 * @author mkleint
 */
public final class MavenVersionSettings {
    private static final MavenVersionSettings INSTANCE = new MavenVersionSettings();
    
    public static final String VERSION_COMPILER = "maven-compiler-plugin"; //NOI18N
    public static final String VERSION_RESOURCES = "maven-resources-plugin"; //NOI18N
    
    public static MavenVersionSettings getDefault() {
        return INSTANCE;
    }
    
    protected final Preferences getPreferences() {
        return NbPreferences.root().node("org/netbeans/modules/maven/pluginVersions"); //NOI18N
    }
    
    protected final String putProperty(String key, String value) {
        String retval = getProperty(key);
        if (value != null) {
            getPreferences().put(key, value);
        } else {
            getPreferences().remove(key);
        }
        return retval;
    }

    protected final String getProperty(String key) {
        return getPreferences().get(key, null);
    }    
    
    private MavenVersionSettings() {
    }
    
    public String getVersion(String plugin) {
        String toRet = getProperty(plugin);
        if (toRet == null) {
            // XXX these should rather read the most recent version from the repository index
            if (VERSION_RESOURCES.equals(plugin)) {
                toRet = "2.4.3"; //NOI18N
            }
            else if (VERSION_COMPILER.equals(plugin)) {
                toRet = "2.3.2"; //NOI18N
            }
        }
        if (toRet == null) {
            toRet = "RELEASE"; // this is wrong for 2.1
        }
        return toRet;
    }
    
}
