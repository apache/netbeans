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
package org.netbeans.modules.web.common.cssprep;

import java.util.List;
import org.netbeans.modules.web.common.api.CssPreprocessor;
import org.netbeans.modules.web.common.api.CssPreprocessors;

public abstract class CssPreprocessorsAccessor {

    private static volatile CssPreprocessorsAccessor accessor;


    public static synchronized CssPreprocessorsAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        }
        Class<?> c = CssPreprocessors.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
        assert accessor != null;
        return accessor;
    }

    public static void setDefault(CssPreprocessorsAccessor accessor) {
        if (CssPreprocessorsAccessor.accessor != null) {
            throw new IllegalStateException("Already initialized accessor");
        }
        CssPreprocessorsAccessor.accessor = accessor;
    }

    public abstract List<CssPreprocessor> getPreprocessors();

}
