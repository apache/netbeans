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

package org.netbeans.modules.bugzilla.util;

import org.eclipse.mylyn.internal.bugzilla.core.IBugzillaConstants;

/**
 *
 * @author Tomas Stupka
 */
public interface BugzillaConstants {
    public static final String URL_ADVANCED_BUG_LIST = IBugzillaConstants.URL_BUGLIST + "?query_format=advanced"; //NOI18N
    public static final String URL_BUG_IDS = IBugzillaConstants.URL_BUGLIST + "?bug_id="; //NOI18N
    public static final String URL_SHOW_BUG = IBugzillaConstants.URL_GET_SHOW_BUG;
    public static final String QUERY_COLUMN_LIST = "&columnlist=bug_severity%2Cpriority%2Cbug_status%2Cresolution%2Cshort_desc%2Cchangeddate"; //NOI18N

    public static final String MY_ISSUES_PARAMETERS_FORMAT =
            "&product={0}" +                                                    // NOI18N
            "&bug_status=UNCONFIRMED" +                                         // NOI18N
            "&bug_status=NEW" +                                                 // NOI18N
            "&bug_status=ASSIGNED" +                                            // NOI18N
            "&bug_status=REOPENED" +                                            // NOI18N
            "&emailassigned_to1=1" +                                            // NOI18N
            "&emailreporter1=1" +                                               // NOI18N
            "&emailtype1=exact" +                                               // NOI18N
            "&email1={1}";                                                      // NOI18N

    public static final String NB_MY_ISSUES_PARAMETERS_FORMAT =
            "&product={0}" +                                                    // NOI18N
            "&bug_status=NEW" +                                                 // NOI18N
            "&bug_status=STARTED" +                                             // NOI18N
            "&bug_status=REOPENED" +                                            // NOI18N
            "&emailassigned_to1=1" +                                            // NOI18N
            "&emailreporter1=1" +                                               // NOI18N
            "&emailtype1=exact" +                                               // NOI18N
            "&email1={1}";                                                      // NOI18N

    public static final String ALL_ISSUES_PARAMETERS =
            "&product={0}" +                                                    // NOI18N
            "&bug_status=UNCONFIRMED" +                                         // NOI18N
            "&bug_status=NEW" +                                                 // NOI18N
            "&bug_status=ASSIGNED" +                                            // NOI18N
            "&bug_status=REOPENED";                                             // NOI18N

    public static final String DEFAULT_STATUS_PARAMETERS =
            "&bug_status=NEW" +                                                 // NOI18N
            "&bug_status=ASSIGNED" +                                            // NOI18N
            "&bug_status=REOPENED";                                             // NOI18N

    public static final String DEFAULT_NB_STATUS_PARAMETERS =
            "&bug_status=NEW" +                                                 // NOI18N
            "&bug_status=STARTED" +                                             // NOI18N
            "&bug_status=REOPENED";                                             // NOI18N

}
