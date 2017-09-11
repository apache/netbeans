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

package org.netbeans.modules.bugtracking.commons;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public final class AutoupdateSupport {

    private static final String CHECK_UPDATES       = ".check_updates";         // NOI18N
    private final static Logger LOG = Logger.getLogger(AutoupdateSupport.class.getName());
    
    private Map<String, Long> lastChecks = null;
    private Set<String> loggedUrls;
    private final Callback callback;
    
    private final String cnb;
    private final String pluginName;
    
    public AutoupdateSupport(Callback callback, String cnb, String pluginName) { 
        this.callback = callback;
        this.cnb = cnb;
        this.pluginName = pluginName;
    }

    boolean getCheckUpdates() {
        return Support.getInstance().getPreferences().getBoolean(cnb + CHECK_UPDATES, true);
    }

    void setCheckUpdates(boolean b) {
        Support.getInstance().getPreferences().putBoolean(cnb + CHECK_UPDATES, b);
    }

    String getPluginName() {
        return pluginName;
    }

    public interface Callback {
        String getServerVersion(String url);
        boolean isSupportedVersion(String version);
        boolean checkIfShouldDownload(String desc);
    }
    
    /**
     * Checks if the remote repository has a version higher then actually
     * supported and if an update is available on the UC.
     *
     * @param url the repository to check the version for
     */
    @NbBundle.Messages({"CTL_Yes=Yes",
                        "# {0} - stands for an issue tracking plugins name available for download - e.g. Bugzilla of JIRA", "CTL_AutoupdateTitle=Newer {0} plugin version available"})
    public void checkAndNotify(String url) {
        LOG.log(Level.FINEST, "{0} AutoupdateSupport.checkAndNotify start", pluginName); // NOI18N
        
        IDEServices ideServices = Support.getInstance().getIDEServices();
        if(ideServices == null || !ideServices.providesPluginUpdate()) {
            return;
        }
        try {
            if(wasCheckedToday(getLastCheck(url))) {
                return;
            }
            if(!getCheckUpdates()) {
                return;
            }
            
            String serverVersion = callback.getServerVersion(url);
            if(serverVersion != null && !callback.isSupportedVersion(serverVersion)) {
                
                boolean alreadyLogged = loggedUrls != null && loggedUrls.contains(url);
                if(!alreadyLogged) {
                    LOG.log(Level.INFO,
                             "{0} repository [{1}] has version {2}. ", // NOI18N
                             new Object[] {pluginName, url, serverVersion});
                    if(loggedUrls == null) {
                        loggedUrls = new HashSet<String>();
                    } 
                    loggedUrls.add(url);
                }
            
                IDEServices.Plugin plugin = checkNewPluginAvailable();
                if(plugin != null) {
                    AutoupdatePanel panel = new AutoupdatePanel(this);
                    if(Util.show(
                            panel,
                            Bundle.CTL_AutoupdateTitle(pluginName),
                            Bundle.CTL_Yes()))
                    {
                        plugin.installOrUpdate();
                    }
                }
            }
        } finally {
            LOG.log(Level.FINEST, "{0} AutoupdateSupport.checkAndNotify finish", pluginName); // NOI18N
        }
    }

    public IDEServices.Plugin checkNewPluginAvailable() {
        IDEServices ideServices = Support.getInstance().getIDEServices();
        IDEServices.Plugin plugin = ideServices.getPluginUpdates(cnb, pluginName);
        if(plugin != null) {
            if(callback.checkIfShouldDownload(plugin.getDescription())){
                return plugin;
            } 
        }
        return null;
    }

    boolean wasCheckedToday(long lastCheck) {
        if (lastCheck < 0) {
            return false;
        }
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, c.get(Calendar.SECOND) * -1);
        c.add(Calendar.MINUTE, c.get(Calendar.MINUTE) * -1);
        c.add(Calendar.HOUR, c.get(Calendar.HOUR) * -1);
        return lastCheck > c.getTime().getTime();
    }

    private long getLastCheck(String url) {
        if(lastChecks == null) {
            lastChecks = new HashMap<String, Long>(1);
        }
        Long l = lastChecks.get(url);
        if(l == null) {
            lastChecks.put(url, System.currentTimeMillis());
            return -1;
        }
        return l;
    }
}
