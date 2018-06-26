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

package org.netbeans.modules.groovy.support.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;

/**
 * Constants useful for Groovy-based projects.
 *
 * @author Martin Adamek
 */
public class GroovySources {

    /**
     * Location of Groovy file icon (16x16)
     */
    public static final String GROOVY_FILE_ICON_16x16 = "org/netbeans/modules/groovy/support/resources/GroovyFile16x16.png"; // NOI18N

    /**
     * Groovy package root sources type.
     * @see org.netbeans.api.project.Sources
     */
    public static final String SOURCES_TYPE_GROOVY = "groovy"; // NOI18N

    /**
     * Groovy sources in standard Grails folders, e.g. domain classes or controllers
     */
    public static final String SOURCES_TYPE_GRAILS = "grails"; // NOI18N

    /**
     * Groovy spources in non-standard Grails folders, e.g. jobs dir added by Quartz plugin
     */
    public static final String SOURCES_TYPE_GRAILS_UNKNOWN = "grails_unknown"; // NOI18N

    /**
     * Searches for all source groups that can contain Groovy sources, including Grails
     * default folders and also folders added to Grails by plugins etc...
     */
    public static List<SourceGroup> getGroovySourceGroups(Sources sources) {
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        result.addAll(Arrays.asList(sources.getSourceGroups(GroovySources.SOURCES_TYPE_GROOVY)));
        result.addAll(Arrays.asList(sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)));
        result.addAll(Arrays.asList(sources.getSourceGroups(GroovySources.SOURCES_TYPE_GRAILS)));
        result.addAll(Arrays.asList(sources.getSourceGroups(GroovySources.SOURCES_TYPE_GRAILS_UNKNOWN)));
        return result;
    }

}
