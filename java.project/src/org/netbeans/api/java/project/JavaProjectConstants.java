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

package org.netbeans.api.java.project;

/**
 * Constants useful for Java-based projects.
 * @author Jesse Glick
 */
public class JavaProjectConstants {

    private JavaProjectConstants() {}

    /**
     * Java package root sources type.
     * @see org.netbeans.api.project.Sources
     */
    public static final String SOURCES_TYPE_JAVA = "java"; // NOI18N

    /**
     * Package root sources type for resources, if these are not put together with Java sources.
     * @see org.netbeans.api.project.Sources
     * @since org.netbeans.modules.java.project/1 1.11
     */
    public static final String SOURCES_TYPE_RESOURCES = "resources"; // NOI18N

    /**
     * Java module root sources type.
     * @see org.netbeans.api.project.Sources
     */
    public static final String SOURCES_TYPE_MODULES = "modules"; // NOI18N

    /**
     * Hint for <code>SourceGroupModifier</code> to create a <code>SourceGroup</code>
     * for main project codebase.
     * @see org.netbeans.api.project.SourceGroupModifier
     * @since org.netbeans.modules.java.project/1 1.24
     */
    public static final String SOURCES_HINT_MAIN = "main"; //NOI18N

    /**
     * Hint for <code>SourceGroupModifier</code> to create a <code>SourceGroup</code>
     * for project's tests.
     * @see org.netbeans.api.project.SourceGroupModifier
     * @since org.netbeans.modules.java.project/1 1.24
     */
    public static final String SOURCES_HINT_TEST = "test"; //NOI18N

    /**
     * Standard artifact type representing a JAR file, presumably
     * used as a Java library of some kind.
     * @see org.netbeans.api.project.ant.AntArtifact
     */
    public static final String ARTIFACT_TYPE_JAR = "jar"; // NOI18N
    
    
    /**
     * Standard artifact type representing a folder containing classes, presumably
     * used as a Java library of some kind.
     * @see org.netbeans.api.project.ant.AntArtifact
     * @since org.netbeans.modules.java.project/1 1.4
     */
    public static final String ARTIFACT_TYPE_FOLDER = "folder"; //NOI18N

    /**
     * Standard command for running Javadoc on a project.
     * @see org.netbeans.spi.project.ActionProvider
     */
    public static final String COMMAND_JAVADOC = "javadoc"; // NOI18N
    
    /** 
     * Standard command for reloading a class in a foreign VM and continuing debugging.
     * @see org.netbeans.spi.project.ActionProvider
     */
    public static final String COMMAND_DEBUG_FIX = "debug.fix"; // NOI18N
    
}
