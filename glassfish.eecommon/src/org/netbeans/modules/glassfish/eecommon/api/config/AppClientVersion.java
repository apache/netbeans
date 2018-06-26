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
package org.netbeans.modules.glassfish.eecommon.api.config;


/**
 *  Enumerated types for Application Client Version
 *
 * @author Peter Williams
 */
public final class AppClientVersion extends J2EEBaseVersion {

    /** Represents application-client version 1.3
     */
    public static final AppClientVersion APP_CLIENT_1_3 = new AppClientVersion(
        "1.3", 1300,	// NOI18N
        "1.3", 1300	// NOI18N
        );

    /** Represents application-client version 1.4
     */
    public static final AppClientVersion APP_CLIENT_1_4 = new AppClientVersion(
        "1.4", 1400,	// NOI18N
        "1.4", 1400	// NOI18N
        );

    /** Represents application-client version 5.0
     */
    public static final AppClientVersion APP_CLIENT_5_0 = new AppClientVersion(
        "5.0", 5000,	// NOI18N
        "5.0", 5000	// NOI18N
        );


    /** Represents application-client version 6.0
     */
    public static final AppClientVersion APP_CLIENT_6_0 = new AppClientVersion(
        "6.0", 6000,	// NOI18N
        "6.0", 6000	// NOI18N
        );
    /** -----------------------------------------------------------------------
     *  Implementation
     */

    /** Creates a new instance of AppClientVersion 
     */
    private AppClientVersion(String version, int nv, String specVersion, int nsv) {
        super(version, nv, specVersion, nsv);
    }

    /** Comparator implementation that works only on AppClientVersion objects
     *
     *  @param obj AppClientVersion to compare with.
     *  @return -1, 0, or 1 if this version is less than, equal to, or greater
     *     than the version passed in as an argument.
     *  @throws ClassCastException if obj is not a AppClientVersion object.
     */
    public int compareTo(Object obj) {
        AppClientVersion target = (AppClientVersion) obj;
        return numericCompare(target);
    }

    public static AppClientVersion getAppClientVersion(String version) {
        AppClientVersion result = APP_CLIENT_6_0;

        if(APP_CLIENT_1_3.toString().equals(version)) {
            result = APP_CLIENT_1_3;
        } else if(APP_CLIENT_1_4.toString().equals(version)) {
            result = APP_CLIENT_1_4;
        } else if(APP_CLIENT_5_0.toString().equals(version)) {
            result = APP_CLIENT_5_0;
        }

        return result;
    }
}
