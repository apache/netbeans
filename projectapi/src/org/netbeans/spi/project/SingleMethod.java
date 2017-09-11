/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.spi.project;

import org.openide.filesystems.FileObject;

/**
 * Structure representing an identification of a single method/function
 * in a file.
 *
 * @since 1.19
 */
public final class SingleMethod {

    private FileObject file;
    private String methodName;

    /**
     * Creates a new instance holding the specified identification
     * of a method/function in a file.
     *
     * @param file file to be kept in the object
     * @param methodName name of a method inside the file
     * @exception  java.lang.IllegalArgumentException
     *             if the file or method name is {@code null}
     * @since 1.19
     */
    public SingleMethod(FileObject file, String methodName) {
        super();
        if (file == null) {
            throw new IllegalArgumentException("file is <null>");
        }
        if (methodName == null) {
            throw new IllegalArgumentException("methodName is <null>");
        }
        this.file = file;
        this.methodName = methodName;
    }

    /**
     * Returns the file identification.
     *
     * @return file held by this object
     * @since 1.19
     */
    public FileObject getFile() {
        return file;
    }

    /**
     * Returns name of a method/function within the file.
     *
     * @return method/function name held by this object
     * @since 1.19
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Standard command for running single method/function
     *
     * @since 1.19
     */
    public static final String COMMAND_RUN_SINGLE_METHOD = "run.single.method";

    /**
     * Standard command for running single method/function in debugger
     *
     * @since 1.19
     */
    public static final String COMMAND_DEBUG_SINGLE_METHOD = "debug.single.method";

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || (obj.getClass() != SingleMethod.class)) {
            return false;
        }
        SingleMethod other = (SingleMethod) obj;
        return other.file.equals(file) && other.methodName.equals(methodName);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.file.hashCode();
        hash = 29 * hash + this.methodName.hashCode();
        return hash;
    }
}
