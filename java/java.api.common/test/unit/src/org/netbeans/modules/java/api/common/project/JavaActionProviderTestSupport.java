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
package org.netbeans.modules.java.api.common.project;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 *
 * @author Tomas Zezula
 */
public final class JavaActionProviderTestSupport {

    private JavaActionProviderTestSupport() {
        throw new IllegalStateException("No instance allowed");
    }

    public static void setUnitTesFixClasses(@NullAllowed final String classes) {
        JavaActionProvider.unitTestingSupport_fixClasses = classes;
    }

    @CheckForNull
    public static JavaActionProvider getDelegate(@NonNull final BaseActionProvider ap) {
        try {
            final Method m = BaseActionProvider.class.getDeclaredMethod("getDelegate");
            m.setAccessible(true);
            return (JavaActionProvider) m.invoke(ap);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    public static boolean setUserPropertiesPolicy(
            @NonNull JavaActionProvider jap,
            @NullAllowed ActionProviderSupport.UserPropertiesPolicy policy) {
        try {
            final Field f = JavaActionProvider.class.getDeclaredField("userPropertiesPolicy");
            f.setAccessible(true);
            f.set(jap, policy);
            return true;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
