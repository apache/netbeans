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
package org.netbeans.modules.php.project.annotations;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.openide.util.NbPreferences;

public final class UserAnnotations {

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "annotations"; // NOI18N

    private static final UserAnnotations INSTANCE = new UserAnnotations(NbPreferences.forModule(UserAnnotations.class).node(PREFERENCES_PATH), ""); // NOI18N

    // common tag key - [<prefix>]tag.<index>.<attribute>
    private static final String TAG_KEY = "%stag.%d.%s"; // NOI18N

    // tag attributes
    private static final String ATTR_TYPES = "types"; // NOI18N
    private static final String ATTR_NAME = "name"; // NOI18N
    private static final String ATTR_INSERT_TEMPLATE = "insertTemplate"; // NOI18N
    private static final String ATTR_DOCUMENTATION = "documentation"; // NOI18N

    // value delimiter
    private static final String DELIMITER = ","; // NOI18N

    private final Preferences preferences;
    private final String tagKeyPrefix;


    public UserAnnotations(Preferences preferences, String tagKeyPrefix) {
        assert preferences != null;
        this.preferences = preferences;
        this.tagKeyPrefix = tagKeyPrefix;
    }

    public static UserAnnotations getGlobal() {
        return INSTANCE;
    }

    public static UserAnnotations forProject(Project project) {
        return new UserAnnotations(ProjectUtils.getPreferences(project, UserAnnotations.class, true), "annotation."); // NOI18N
    }

    public List<UserAnnotationTag> getAnnotations() {
        List<UserAnnotationTag> annotations = new LinkedList<>();
        int i = 0;
        for (;;) {
            String types = preferences.get(getTypesKey(i), null);
            if (types == null) {
                return annotations;
            }
            String name = preferences.get(getNameKey(i), null);
            String insertTemplate = preferences.get(getInsertTemplateKey(i), null);
            String documentation = preferences.get(getDocumentationKey(i), null);
            annotations.add(new UserAnnotationTag(unmarshallTypes(types), name, insertTemplate, documentation));
            i++;
        }
    }

    public void setAnnotations(List<UserAnnotationTag> annotations) {
        clearAnnotations();
        int i = 0;
        for (UserAnnotationTag annotation : annotations) {
            preferences.put(getTypesKey(i), marshallTypes(annotation.getTypes()));
            preferences.put(getNameKey(i), annotation.getName());
            preferences.put(getInsertTemplateKey(i), annotation.getInsertTemplate());
            preferences.put(getDocumentationKey(i), annotation.getDocumentation());
            i++;
        }
    }

    // for unit tests
    void clearAnnotations() {
        int i = 0;
        for (;;) {
            String type = preferences.get(getTypesKey(i), null);
            if (type == null) {
                return;
            }
            preferences.remove(getTypesKey(i));
            preferences.remove(getNameKey(i));
            preferences.remove(getInsertTemplateKey(i));
            preferences.remove(getDocumentationKey(i));
            i++;
        }
    }

    private String getTypesKey(int i) {
        return getKey(i, ATTR_TYPES);
    }

    private String getNameKey(int i) {
        return getKey(i, ATTR_NAME);
    }

    private String getInsertTemplateKey(int i) {
        return getKey(i, ATTR_INSERT_TEMPLATE);
    }

    private String getDocumentationKey(int i) {
        return getKey(i, ATTR_DOCUMENTATION);
    }

    private String getKey(int i, String attr) {
        return String.format(TAG_KEY, tagKeyPrefix, i, attr);
    }

    // for unit tests
    String marshallTypes(EnumSet<UserAnnotationTag.Type> types) {
        ArrayList<String> list = new ArrayList<>(types.size());
        for (UserAnnotationTag.Type type : types) {
            list.add(type.name());
        }
        return StringUtils.implode(list, DELIMITER);
    }

    // for unit tests
    EnumSet<UserAnnotationTag.Type> unmarshallTypes(String types) {
        List<String> list = StringUtils.explode(types, DELIMITER);
        EnumSet<UserAnnotationTag.Type> result = EnumSet.noneOf(UserAnnotationTag.Type.class);
        for (String type : list) {
            result.add(UserAnnotationTag.Type.valueOf(type));
        }
        return result;
    }

}
