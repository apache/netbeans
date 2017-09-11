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
package org.netbeans.modules.java.source.queries.spi;

import java.net.URL;
import java.util.Collection;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.source.queries.api.QueryException;
import org.netbeans.modules.java.source.queries.api.Queries;
import org.netbeans.modules.java.source.queries.api.Updates;

/**
 * SPI for {@link Queries} and {@link Updates}.
 * The instance is created by implementor of {@link QueriesController}
 * and passed to {@link QueriesController.Context}
 * @author Tomas Zezula
 */
public interface ModelOperations {

    /**
     * Returns the fully qualified names of top level classes in the
     * compilation unit represented by file for which the {@link ModelOperations}
     * was created
     * @return a collection of fully qualified names
     * @throws QueryException in case of failure
     */
    @NonNull
    Collection<? extends String> getTopLevelClasses() throws QueryException;

    /**
     * Returns the fully qualified name of the super class for
     * for given class.
     * @param cls the class fully qualified name
     * @return the fully qualified name of the super class or null
     * when the class has no super type, eg j.l.Object, error type
     * @throws QueryException in case of failure
     */
    @CheckForNull
    String getSuperClass(@NonNull String cls) throws QueryException;

    /**
     * Returns the fully qualified names of the implemented interfaces for
     * for given class.
     * @param cls the class fully qualified name
     * @return the fully qualified names of the implemented interfaces
     * @throws QueryException in case of failure
     */
    @NonNull
    public Collection<? extends String> getInterfaces(@NonNull String cls) throws QueryException;

    /**
     * Returns names of the methods complying to given types.
     * @param cls the fully qualified name of the class to look up methods in
     * @param useRawTypes if true the erasure is done before comparison
     * @param returnType the return type of the method specified by
     * the class canonical name, @see Class#getCanonicalName (null represents
     * any type). If useRawTypes is false parameterized type is allowed.
     * @param parameterTypes the types of the method parameters specified by
     * the class canonical name, @see Class#getCanonicalName
     * (null represent any type). If useRawTypes is false parameterized types
     * are allowed.
     * @return a collection of method names
     * @throws QueryException in case of failure
     */
    @NonNull
    Collection<? extends String> getMethodNames(
            @NonNull String cls,
            boolean useRawTypes,
            @NullAllowed String returnType,
            @NullAllowed String[] parameterTypes) throws QueryException;


    /**
     * Returns names of the fields complying to type.
     * @param cls the fully qualified name of the class to look up methods in
     * @param useRawTypes if true the erasure is done before comparison
     * @param type the type of the field specified by the class canonical name,
     * @see Class#getCanonicalName (null represents any type). If useRawTypes is
     * false parameterized type is allowed.
     * @return a collection of field names
     * @throws QueryException in case of failure
     */
    @NonNull
    Collection<? extends String> getFieldNames(
            @NonNull String cls,
            boolean useRawTypes,
            @NullAllowed String type) throws QueryException;


    /**
     * Returns a method start and end position.
     * @param cls the fully qualified name of the class to look up methods in
     * @param methodName name of the method
     * @param useRawTypes if true the erasure is done before comparison
     * @param returnType the return type of the method specified by
     * the class canonical name, @see Class#getCanonicalName. If useRawTypes is
     * false parameterized type is allowed.
     * @param parameterTypes the types of the method parameters specified by
     * the class canonical name, @see Class#getCanonicalName. If useRawTypes is
     * false parameterized types are allowed.
     * @return a two elements int array containing method start and end position,
     * or null if cannot be found
     * @throws QueryException in case of failure
     */
    @CheckForNull
    int[] getMethodSpan(
            @NonNull String cls,
            @NonNull String methodName,
            boolean useRawTypes,
            @NonNull String returnType,
            @NonNull String... parameterTypes) throws QueryException;

    /**
     * Returns a binary name as specified by JLS §13.1 for given
     * fully qualified name.
     * @param cls the fully qualified name of the class
     * @return the binary name or null if cls cannot be resolved
     * @throws QueryException in case of failure
     */
    @CheckForNull
    String getClassBinaryName(@NonNull String cls) throws QueryException;


    /**
     * Adds required imports into the parts of java source specified by given ranges.
     * @param ranges the source ranges where the fully qualified named should be
     * changed into simple names and the fully qualified names should be imported
     * @throws QueryException in case of failure
     */
    void fixImports(
            int[][] ranges) throws QueryException;


    /**
     * Modifies class interfaces.
     * @param cls the fully qualified name of class to be changed
     * @param toAdd the {@link Collection} of fully qualified names of
     * interfaces to be added
     * @param toRemove the {@link Collection} of fully qualified names of
     * interfaces to be removed
     * @throws QueryException in case of failure
     * @throws IllegalArgumentException when cls is not in a file the
     * {@link Updates} instance was created for. The file {@link URL} is
     * passed to the {@link QueriesController} by {@link QueriesController.Context#getURL()}.
     */
    void modifyInterfaces(
            @NonNull String cls,
            @NonNull Collection<? extends String> toAdd,
            @NonNull Collection<? extends String> toRemove) throws QueryException;

    /**
     * Sets class super class.
     * @param cls the fully qualified name of class to be changed
     * @param superCls the fully qualified name of super class
     * @throws QueryException in case of failure
     * @throws IllegalArgumentException when cls is not in a file the
     * {@link Updates} instance was created for. The file {@link URL} is
     * passed to the {@link QueriesController} by {@link QueriesController.Context#getURL()}.
     */
    void setSuperClass(
            @NonNull String cls,
            @NonNull String superCls) throws QueryException;


    /**
     * Renames a field in a class.
     * @param cls the fully qualified name of the class owning the field
     * @param oldName the old field name
     * @param newName the new field name
     * @throws QueryException in case of failure
     * @throws IllegalArgumentException when cls is not in a file the
     * {@link Updates} instance was created for. The file {@link URL} is
     * passed to the {@link QueriesController} by {@link QueriesController.Context#getURL()}.
     */
    void renameField(
            @NonNull String cls,
            @NonNull String oldName,
            @NonNull String newName) throws QueryException;
}
