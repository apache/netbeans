/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.helper;

import org.netbeans.installer.utils.ResourceUtils;

public enum DetailedStatus {
    INSTALLED_SUCCESSFULLY,
    INSTALLED_WITH_WARNINGS,
    FAILED_TO_INSTALL,
    UNINSTALLED_SUCCESSFULLY,
    UNINSTALLED_WITH_WARNINGS,
    FAILED_TO_UNINSTALL;
    
    public String toString() {
        switch (this) {
            case INSTALLED_SUCCESSFULLY:
                return INSTALLED_SUCCESSFULLY_STRING;
            case INSTALLED_WITH_WARNINGS:
                return INSTALLED_WITH_WARNINGS_STRING;
            case FAILED_TO_INSTALL:
                return FAILED_TO_INSTALL_STRING;
            case UNINSTALLED_SUCCESSFULLY:
                return UNINSTALLED_SUCCESSFULLY_STRING;
            case UNINSTALLED_WITH_WARNINGS:
                return UNINSTALLED_WITH_WARNINGS_STRING;
            case FAILED_TO_UNINSTALL:
                return FAILED_TO_UNINSTALL_STRING;
            default:
                return null;
        }
    }
    private static final String INSTALLED_SUCCESSFULLY_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.install.succes");//NOI18N
private static final String INSTALLED_WITH_WARNINGS_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.install.warning");//NOI18N
private static final String FAILED_TO_INSTALL_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.install.error");//NOI18N

private static final String UNINSTALLED_SUCCESSFULLY_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.uninstall.success");//NOI18N
private static final String UNINSTALLED_WITH_WARNINGS_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.uninstall.warning");//NOI18N
private static final String FAILED_TO_UNINSTALL_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.uninstall.error");//NOI18N
}
