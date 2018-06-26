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
