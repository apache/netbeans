/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
