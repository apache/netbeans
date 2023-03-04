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

package org.netbeans.modules.java.api.common.impl;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.api.common.Roots;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public abstract class RootsAccessor {

    private static volatile RootsAccessor instance;


    public static synchronized RootsAccessor getInstance() {
        if (instance == null) {
            try {
                Class.forName(Roots.class.getName(),true,RootsAccessor.class.getClassLoader());
                assert instance != null;
            } catch (ClassNotFoundException cnf) {
                Exceptions.printStackTrace(cnf);
            }
        }
        return instance;
    }

    public static void setInstance(final @NonNull RootsAccessor theInstance) {
        assert theInstance != null;
        instance = theInstance;
    }

    public abstract boolean isSourceRoot(Roots roots);

    public abstract boolean supportIncludes(Roots roots);

    public abstract String getType(Roots roots);

    public abstract String getHint(Roots roots);

    public abstract String[] getRootPathProperties(Roots roots);
}
