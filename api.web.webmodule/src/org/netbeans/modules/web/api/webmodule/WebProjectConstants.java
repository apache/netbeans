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

package org.netbeans.modules.web.api.webmodule;

/**
 * Constants useful for web-based projects.
 *
 * @author  Milan Kuchtiak
 */
public final class WebProjectConstants {

    private WebProjectConstants () {}

    /**
     * Document root root sources type (source folders for JSPs, HTML, ...).
     * See <code>org.netbeans.api.project.Sources</code>.
     */
    public static final String TYPE_DOC_ROOT="doc_root"; //NOI18N

    /**
     * WEB-INF sources type (source folders for TLD files, ...).
     * See <code>org.netbeans.api.project.Sources</code>.
     */
    public static final String TYPE_WEB_INF="web_inf"; //NOI18N
    
    /**
     * Standard command for redeploying a web project.
     * See <code>org.netbeans.api.project.ActionProvider</code>.
     */
    public static final String COMMAND_REDEPLOY = "redeploy" ; //NOI18N
    
    /**
     * Standard artifact type representing a WAR file.
     * See <code>org.netbeans.api.project.ant.AntArtifact</code>.
     */
    public static final String ARTIFACT_TYPE_WAR = "war"; // NOI18N
    
    /**
     * Standard artifact type representing a WAR file used for adding
     * Web module into a J2EE Application (ear project).
     * See <code>org.netbeans.api.project.ant.AntArtifact</code>.
     */
    public static final String ARTIFACT_TYPE_WAR_EAR_ARCHIVE = "j2ee_ear_archive"; //NOI18N
}
