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
