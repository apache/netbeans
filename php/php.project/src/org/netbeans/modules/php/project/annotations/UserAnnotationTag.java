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

import java.util.EnumSet;
import org.netbeans.modules.php.spi.annotation.AnnotationCompletionTag;
import org.openide.util.NbBundle;

/**
 * Tag for user annotation.
 */
public class UserAnnotationTag extends AnnotationCompletionTag {

    @NbBundle.Messages({
        "UserAnnotationTag.type.function.title=Function",
        "UserAnnotationTag.type.type.title=Class/Interface",
        "UserAnnotationTag.type.field.title=Field",
        "UserAnnotationTag.type.method.title=Method"
    })
    public static enum Type {
        FUNCTION(Bundle.UserAnnotationTag_type_function_title()),
        TYPE(Bundle.UserAnnotationTag_type_type_title()),
        FIELD(Bundle.UserAnnotationTag_type_field_title()),
        METHOD(Bundle.UserAnnotationTag_type_method_title());

        private final String title;


        private Type(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

    }

    private final EnumSet<Type> types;


    public UserAnnotationTag(EnumSet<Type> types, String name, String insertTemplate, String documentation) {
        super(name, insertTemplate, documentation);

        assert types != null;
        this.types = types;
    }

    public EnumSet<Type> getTypes() {
        return types;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + types.hashCode();
        return hash + super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserAnnotationTag other = (UserAnnotationTag) obj;
        if (!types.equals(other.types)) {
            return false;
        }
        return super.equals(obj);
    }

}
