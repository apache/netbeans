/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.team.commons;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.bugtracking.commons.TextUtils;

/**
 *
 * @author Tomas Stupka
 */
public final class LogUtils {
    /**
     * Metrics logger
     */
    private final static Logger METRICS_LOG = Logger.getLogger("org.netbeans.ui.metrics.bugtracking"); // NOI18N

    /**
     * The automatic refresh was set on or off.<br>
     * Parameters:
     * <ol>
     *  <li>connector name : String
     *  <li>is on : Boolean
     * </ol>
     */
    public static final String USG_BUGTRACKING_AUTOMATIC_REFRESH = "USG_BUGTRACKING_AUTOMATIC_REFRESH"; // NOI18N

    /**
     * A query was refreshed.<br>
     * Parameters:
     * <ol>
     *  <li>connector name : String
     *  <li>query name : String
     *  <li>issues count : Integer
     *  <li>is a team server query : Boolean
     *  <li>is a automatic refresh : Boolean
     * </ol>
     */
    public static final String USG_BUGTRACKING_QUERY             = "USG_BUGTRACKING_QUERY"; // NOI18N

    /**
     * Some bugtracking operation happened.<br>
     * Parameters:
     * <ol>
     *  <li>connector name : String
     *  <li>operation : String - ISSUE_EDIT, ISSUE_QUERY, COMMIT_HOOK
     * </ol>
     */
    private static final String USG_ISSUE_TRACKING = "USG_ISSUE_TRACKING"; // NOI18N

    /**
     * A repository was instantiated.<br>
     * Parameters:
     * <ol>
     *  <li>connector name : String
     *  <li>repository site : String - e.g. kenai.com, java.net, other
     * </ol>
     */
    private static final String USG_ISSUE_TRACKING_REPOSITORY = "USG_ISSUE_TRACKING_REPOSITORY"; // NOI18N
    
    private final static Set<String> loggedParams = new HashSet<String>(1); // to avoid logging same params more than once in a session

    public static void logQueryEvent(String connector, String name, int count, boolean isFromTeamServer, boolean isAutoRefresh) {
        name = obfuscateQueryName(name);
        logBugtrackingEvents(USG_BUGTRACKING_QUERY, new Object[] {connector, name, count, isFromTeamServer, isAutoRefresh} );
    }

    public static void logAutoRefreshEvent(String connector, String queryName, boolean isFromTeamServer, boolean on) {
        queryName = obfuscateQueryName(queryName);
        logBugtrackingEvents(USG_BUGTRACKING_AUTOMATIC_REFRESH, new Object[] {connector, queryName, isFromTeamServer, on} );
    }
    
    public static synchronized void logBugtrackingUsage(String connectorID, String operation) {
        if (connectorID == null || operation == null) {
            return;
        }
        String bugtrackingType = getBugtrackingType(connectorID);
        // log general bugtracking usage
        if (!checkMetricsKey(getParamString(USG_ISSUE_TRACKING, bugtrackingType, operation))) {
            // not logged in this session yet
            LogRecord rec = new LogRecord(Level.INFO, USG_ISSUE_TRACKING);
            rec.setParameters(new Object[] { bugtrackingType, operation });
            rec.setLoggerName(METRICS_LOG.getName());
            METRICS_LOG.log(rec);
        }
    }
    
    public static void logRepositoryUsage(String connectorID, String repositoryUrl) {
        if (connectorID == null || repositoryUrl == null) {
            return;
        }        
        String bugtrackingType = getBugtrackingType(connectorID);
        String knownRepositoryFor = LogUtils.getKnownRepositoryFor(repositoryUrl);        
        if (!checkMetricsKey(getParamString(USG_ISSUE_TRACKING_REPOSITORY, bugtrackingType, knownRepositoryFor))) {
            LogRecord rec = new LogRecord(Level.INFO, USG_ISSUE_TRACKING_REPOSITORY);
            rec.setParameters(new Object[] { getBugtrackingType(connectorID), knownRepositoryFor});
            rec.setLoggerName(METRICS_LOG.getName());
            METRICS_LOG.log(rec);
        }
    }
    
    private static boolean checkMetricsKey(String key) {
        synchronized (loggedParams) {
            if (loggedParams.contains(key)) {
                return true;
            } else {
                loggedParams.add(key);
            }
        }
        return false;
    }
    
    public static String getKnownRepositoryFor (String repositoryUrl) {
        repositoryUrl = repositoryUrl.toLowerCase();
        if (repositoryUrl.contains("github.com")) { //NOI18N
            return "GITHUB"; //NOI18N
        } else if (repositoryUrl.contains("gitorious.org")) { //NOI18N
            return "GITORIOUS"; //NOI18N
        } else if (repositoryUrl.contains("bitbucket.org")) { //NOI18N
            return "BITBUCKET"; //NOI18N
        } else if (repositoryUrl.contains("sourceforge.net")) { //NOI18N
            return "SOURCEFORGE"; //NOI18N
        } else if (repositoryUrl.contains("googlecode.com") //NOI18N
                || repositoryUrl.contains("code.google.com") //NOI18N
                || repositoryUrl.contains("googlesource.com")) { //NOI18N
            return "GOOGLECODE"; //NOI18N
        } else if (repositoryUrl.contains("kenai.com")) { //NOI18N
            return "KENAI"; //NOI18N
        } else if (repositoryUrl.contains("java.net")) { //NOI18N
            return "JAVANET"; //NOI18N
        } else if (repositoryUrl.contains("netbeans.org")) { //NOI18N
            return "NETBEANS"; //NOI18N
        } else if (repositoryUrl.contains("codeplex.com")) { //NOI18N
            return "CODEPLEX"; //NOI18N
        } else if (repositoryUrl.contains(".eclipse.org")) { //NOI18N
            return "ECLIPSE"; //NOI18N
        } else {
            return "OTHER"; //NOI18N
        }
    }        
    
    private static String getParamString(Object... parameters) {
        if (parameters == null || parameters.length == 0) {
            return ""; // NOI18N
        }
        if (parameters.length == 1) {
            return parameters[0].toString();
        }
        StringBuilder buf = new StringBuilder();
        for (Object p : parameters) {
            buf.append(p.toString());
        }
        return buf.toString();
    }

    public static String getBugtrackingType(String id) {
        // XXX hack: there's no clean way to determine the type of bugtracking
        // from RepositoryProvider (need BugtrackingConnector.getDisplayName)
        if (id.contains("bugzilla")) { // NOI18N
            return "Bugzilla"; // NOI18N
        }
        if (id.contains("jira")) { // NOI18N
            return "Jira"; // NOI18N
        }
        if (id.contains("odcs") || 
            id.contains("CloudDev")) { // NOI18N
            return "ODCS"; //NOI18N
        }
        return id;
    }

    public static String getPasswordLog(char[] psswd) {
        if(psswd == null) {
            return ""; // NOI18N
        }
        if("true".equals(System.getProperty("org.netbeans.modules.bugtracking.logPasswords", "false")) || // NOI18N
           "true".equals(System.getProperty("org.netbeans.modules.team.logPasswords", "false")))          // NOI18N
        {
            return new String(psswd); 
        }
        return "******"; // NOI18N
    }    
    
    /**
     * Logs bugtracking events
     *
     * @param key - the events key
     * @param parameters - the parameters for the given event
     */
    private static void logBugtrackingEvents(String key, Object[] parameters) {
        LogRecord rec = new LogRecord(Level.INFO, key);
        rec.setParameters(parameters);
        rec.setLoggerName(METRICS_LOG.getName());
        METRICS_LOG.log(rec);
    }    
    
    private static String obfuscateQueryName(String name) {
        if (name == null) {
            name = "Find Issues"; // NOI18N
        } else {
            name = TextUtils.getMD5(name);
        }
        return name;
    }
    
    
}
