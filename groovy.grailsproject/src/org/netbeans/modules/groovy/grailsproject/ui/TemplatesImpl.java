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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.ui;

import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.SourceCategoryType;
import org.netbeans.modules.groovy.grailsproject.ui.wizards.impl.GrailsArtifacts;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.filesystems.FileObject;

/**
 *
 * @author schmidtm
 * @author Martin Adamek
 */
public class TemplatesImpl implements PrivilegedTemplates, RecommendedTemplates  {

    public static final String GROOVY_CLASS = "Templates/Groovy/GroovyClass.groovy";
    public static final String GROOVY_SCRIPT = "Templates/Groovy/GroovyScript.groovy";
    public static final String GSP = "Templates/Groovy/_view.gsp";

    // These constants must be synchronized with template registrations IDs
    public static final String DOMAIN_CLASS = "Templates/Groovy/DomainClass";
    public static final String CONTROLLER = "Templates/Groovy/Controller";
    public static final String INTEGRATION_TEST = "Templates/Groovy/IntegrationTest";
    public static final String GANT_SCRIPT = "Templates/Groovy/GantScript";
    public static final String SERVICE = "Templates/Groovy/Service";
    public static final String TAG_LIB = "Templates/Groovy/TagLib";
    public static final String UNIT_TEST = "Templates/Groovy/UnitTest";

    private static final String FOLDER = "Templates/Other/Folder";
    private static final String PROPERTIES = "Templates/Other/properties.properties";
    private static final String SIMPLE_FILES = "simple-files";

    private static final String[] GENERAL_TEMPLATES = new String[] {
        GROOVY_CLASS,
        GROOVY_SCRIPT,
        FOLDER,
        PROPERTIES
    };

    private static final String[] SCRIPTS_TEMPLATES = new String[] {
        GANT_SCRIPT,
        GROOVY_SCRIPT,
        FOLDER,
        PROPERTIES
    };

    private static final String[] DOMAIN_TEMPLATES = new String[] {
        DOMAIN_CLASS,
        GROOVY_CLASS,
        GROOVY_SCRIPT,
        FOLDER,
        PROPERTIES
    };

    private static final String[] CONTROLLER_TEMPLATES = new String[] {
        CONTROLLER,
        GROOVY_CLASS,
        GROOVY_SCRIPT,
        FOLDER,
        PROPERTIES
    };

    private static final String[] TAGLIB_TEMPLATES = new String[] {
        TAG_LIB,
        GROOVY_CLASS,
        GROOVY_SCRIPT,
        FOLDER,
        PROPERTIES
    };

    private static final String[] INTEGRATION_TEST_TEMPLATES = new String[] {
        INTEGRATION_TEST,
        GROOVY_CLASS,
        GROOVY_SCRIPT,
        FOLDER,
        PROPERTIES
    };

    private static final String[] UNIT_TEST_TEMPLATES = new String[] {
        UNIT_TEST,
        GROOVY_CLASS,
        GROOVY_SCRIPT,
        FOLDER,
        PROPERTIES
    };

    private static final String[] SERVICE_TEMPLATES = new String[] {
        SERVICE,
        GROOVY_CLASS,
        GROOVY_SCRIPT,
        FOLDER,
        PROPERTIES
    };

    private static final String[] GSP_TEMPLATES = new String[] {
        GSP,
        FOLDER,
        PROPERTIES
    };

    private static final String[] I18N_TEMPLATES = new String[] {
        PROPERTIES
    };


    private final SourceCategoryType sourceCategory;


    public TemplatesImpl(GrailsProject project, SourceGroup sourceGroup) {
        FileObject projectDir = project.getProjectDirectory();
        FileObject rootFolder = sourceGroup.getRootFolder();

        sourceCategory = GrailsArtifacts.getCategoryTypeForFolder(projectDir, rootFolder, project.getSourceCategoriesFactory());
    }

    @Override
    public String[] getPrivilegedTemplates() {
        if (sourceCategory != null) {
            switch (sourceCategory) {
                case GRAILSAPP_CONTROLLERS: return CONTROLLER_TEMPLATES;
                case GRAILSAPP_DOMAIN: return DOMAIN_TEMPLATES;
                case GRAILSAPP_I18N: return I18N_TEMPLATES;
                case GRAILSAPP_SERVICES: return SERVICE_TEMPLATES;
                case GRAILSAPP_TAGLIB: return TAGLIB_TEMPLATES;
                case TEST_INTEGRATION: return INTEGRATION_TEST_TEMPLATES;
                case TEST_UNIT: return UNIT_TEST_TEMPLATES;
                case SCRIPTS: return SCRIPTS_TEMPLATES;

                case GRAILSAPP_UTILS:
                case GRAILSAPP_CONF:
                case SRC_GROOVY:
                    return GENERAL_TEMPLATES;

                case GRAILSAPP_VIEWS:
                case WEBAPP:
                    return GSP_TEMPLATES;

                case SRC_JAVA:
                    return new String[] {
                        "Templates/Classes/Class.java",
                        "Templates/Classes/Interface.java",
                        "Templates/Classes/Enum.java",
                        "Templates/Classes/AnnotationType.java",
                        "Templates/Classes/Exception.java",
                        "Templates/Classes/Package.java",
                    };
                default:
                    break;
            }
        }
        return new String[] {};
    }

    @Override
    public String[] getRecommendedTypes() {
        return new String[] { SIMPLE_FILES };
    }
}
