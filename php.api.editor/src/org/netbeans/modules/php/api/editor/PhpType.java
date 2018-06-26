/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.api.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileObject;

/**
 * Base class for PHP type (class, interface, trait).
 * @since 0.28
 */
public abstract class PhpType extends PhpBaseElement {

    private final Collection<Field> fields = new LinkedList<>();
    private final Collection<Method> methods = new LinkedList<>();


    protected PhpType(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed String description) {
        super(name, fullyQualifiedName, description);
    }

    protected PhpType(@NonNull String name, @NullAllowed String fullyQualifiedName) {
        super(name, fullyQualifiedName);
    }

    protected PhpType(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset) {
        this(name, fullyQualifiedName, offset, null);
    }

    protected PhpType(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
        super(name, fullyQualifiedName, offset, description);
    }

    protected PhpType addField(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
        fields.add(new Field(name, fullyQualifiedName, offset, description));
        return this;
    }

    protected PhpType addField(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset) {
        return addField(name, fullyQualifiedName, offset, null);
    }

    protected PhpType addField(@NonNull String name, @NullAllowed String fullyQualifiedName) {
        return addField(name, fullyQualifiedName, -1, null);
    }

    protected PhpType addField(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed String description) {
        return addField(name, fullyQualifiedName, -1, description);
    }

    protected PhpType addField(@NonNull String name, @NullAllowed PhpType type, @NullAllowed FileObject file, int offset) {
        fields.add(new Field(name, type, file, offset));
        return this;
    }

    protected PhpType addMethod(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
        methods.add(new Method(name, fullyQualifiedName, offset, description));
        return this;
    }

    protected PhpType addMethod(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset) {
        return addMethod(name, fullyQualifiedName, offset, null);
    }

    protected PhpType addMethod(@NonNull String name, @NullAllowed String fullyQualifiedName) {
        return addMethod(name, fullyQualifiedName, -1, null);
    }

    protected PhpType addMethod(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed String description) {
        return addMethod(name, fullyQualifiedName, -1, description);
    }

    public Collection<Field> getFields() {
        return new ArrayList<>(fields);
    }

    public Collection<Method> getMethods() {
        return new ArrayList<>(methods);
    }

    /**
     * Class representing a PHP type field.
     */
    public final class Field extends PhpBaseElement {

        Field(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
            super(name, fullyQualifiedName, offset, description);
        }

        Field(@NonNull String name, @NullAllowed PhpType type, @NullAllowed FileObject file, int offset) {
            super(name, null, type, file, offset, null);
        }

        public PhpType getPhpType() {
            return PhpType.this;
        }

    }

    /**
     * Class representing a PHP type method.
     */
    public final class Method extends PhpBaseElement {

        Method(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
            super(name, fullyQualifiedName, offset, description);
        }

        public PhpType getPhpType() {
            return PhpType.this;
        }

    }

}
