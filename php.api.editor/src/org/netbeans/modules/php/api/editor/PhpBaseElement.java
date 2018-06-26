/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.api.editor;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Class representing a PHP element ({@link PhpType PHP type}, method, field etc.).
 */
public abstract class PhpBaseElement {

    private final String name;
    private final String fullyQualifiedName;
    private FileObject file;
    private final int offset;
    private final String description;
    private final PhpType type;


    protected PhpBaseElement(@NonNull String name, @NullAllowed PhpType type) {
        this(name, null, type, null, -1, null);
    }

    protected PhpBaseElement(@NonNull String name, @NullAllowed String fullyQualifiedName) {
        this(name, fullyQualifiedName, -1, null);
    }

    protected PhpBaseElement(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed FileObject file) {
        this(name, fullyQualifiedName, file, -1, null);
    }

    protected PhpBaseElement(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed String description) {
        this(name, fullyQualifiedName, -1, description);
    }

    protected PhpBaseElement(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset) {
        this(name, fullyQualifiedName, offset, null);
    }

    protected PhpBaseElement(@NonNull String name, @NullAllowed String fullyQualifiedName, int offset, @NullAllowed String description) {
        this(name, fullyQualifiedName, null, offset, description);
    }

    protected PhpBaseElement(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed FileObject file,
            int offset, @NullAllowed String description) {
        this(name, fullyQualifiedName, null, file, offset, description);
    }

    protected PhpBaseElement(@NonNull String name, @NullAllowed String fullyQualifiedName, @NullAllowed PhpType type,
            @NullAllowed FileObject file, int offset, @NullAllowed String description) {
        Parameters.notEmpty("name", name);

        this.name = name;
        this.fullyQualifiedName = fullyQualifiedName;
        this.type = type;
        this.file = file;
        this.offset = offset;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    @CheckForNull
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    @CheckForNull
    public PhpType getType() {
        return type;
    }

    @CheckForNull
    public FileObject getFile() {
        return file;
    }

    public int getOffset() {
        return offset;
    }

    @CheckForNull
    public String getDescription() {
        return description;
    }

    /**
     * @param file the file to set
     */
    public void setFile(FileObject file) {
        this.file = file;
    }

}
