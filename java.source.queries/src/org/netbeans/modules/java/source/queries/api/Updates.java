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
package org.netbeans.modules.java.source.queries.api;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.queries.SPIAccessor;
import org.netbeans.modules.java.source.queries.spi.QueriesController;
import org.openide.util.Parameters;

/**
 * Support for modifications
 * @author Tomas Zezula
 */
public final class Updates extends Queries {

    Updates(@NonNull final URL forURL) {
        super(forURL);
    }

    /**
     * Modifies class interfaces.
     * @param cls the fully qualified name of class to be changed
     * @param toAdd the {@link Collection} of fully qualified names of
     * interfaces to be added
     * @param toRemove the {@link Collection} of fully qualified names of
     * interfaces to be removed
     * @throws QueryException in case of failure
     * @throws IllegalArgumentException when cls is not in a file the
     * {@link Updates} instance was created for.
     */
    public void modifyInterfaces(
            @NonNull String cls,
            @NonNull Collection<? extends String> toAdd,
            @NonNull Collection<? extends String> toRemove) throws QueryException {
        Parameters.notNull("cls", cls);     //NOI18N
        Parameters.notNull("toAdd", toAdd);     //NOI18N
        Parameters.notNull("toRemove", toRemove);     //NOI18N
        impl.modifyInterfaces(cls,toAdd,toRemove);
    }

    /**
     * Sets class super class.
     * @param cls the fully qualified name of class to be changed
     * @param superCls the fully qualified name of super class
     * @throws QueryException in case of failure
     * @throws IllegalArgumentException when cls is not in a file the
     * {@link Updates} instance was created for.
     */
    public void setSuperClass(
            @NonNull String cls,
            @NonNull String superCls) throws QueryException {
        Parameters.notNull("cls", cls);     //NOI18N
        Parameters.notNull("superCls", superCls);   //NOI18N
        impl.setSuperClass(cls, superCls);
    }

    /**
     * Adds required imports into the parts of java source specified by given ranges.
     * @param ranges the source ranges where the fully qualified named should be
     * changed into simple names and the fully qualified names should be imported
     * @throws QueryException in case of failure
     */
    public void fixImports(int[][] ranges) throws QueryException {
        Parameters.notNull("requiredFQNs", ranges);     //NOI18N
        int[][] dcopy = new int[ranges.length][];
        for (int i=0; i< ranges.length; i++) {
            if (ranges[i].length != 2) {
                throw new IllegalArgumentException("Wrong range length, expected 2");   //NOI18N
            }
            dcopy[i] = Arrays.copyOf(ranges[i], 2);
        }
        impl.fixImports(dcopy);
    }

    /**
     * Renames a field in a class.
     * @param cls the fully qualified name of the class owning the field
     * @param oldName the old field name
     * @param newName the new field name
     * @throws QueryException in case of failure
     * @throws IllegalArgumentException when cls is not in a file the
     * {@link Updates} instance was created for.
     */
    public void renameField(
            @NonNull final String cls,
            @NonNull final String oldName,
            @NonNull final String newName) throws QueryException {
        Parameters.notNull("oldName", oldName);     //NOI18N
        Parameters.notNull("newName", newName);     //NOI18N
        impl.renameField(cls,oldName,newName);
    }


    /**
     * Performs the update.
     * @param forURL the URL of the file to perform query on
     * @param updateFnc  the update function. If the function returns true
     * the changes are applied otherwise the changes are rolled back.
     * If the updateFnc throws an exception the changes are rolled back.
     * @return the result of the query function, true means changes
     * were applied, false changes were rolled back.
     * @throws QueryException in case of exception.
     */
    public static boolean update(
            @NonNull final URL forURL,
            @NonNull final Function<Updates,Boolean> updateFnc) throws QueryException {
        Parameters.notNull("forURL", forURL);     //NOI18N
        Parameters.notNull("updateFnc", updateFnc);   //NOI18N
        if (ctl == null) {
            throw new IllegalStateException("No QueriesController found in the Lookup");    //NOI18N
        }
        final Updates u = new Updates(forURL);
        final QueriesController.Context<Boolean> ctx =
                SPIAccessor.getInstance().createContext(updateFnc, u);
        return ctl.runUpdate(ctx);
    }

}
