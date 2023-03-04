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
package org.netbeans.modules.jumpto.type;

import java.util.Collection;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import static org.netbeans.spi.jumpto.type.TypeProvider.*;

/**
 * Accessor class.
 * 
 * @author Pavel Flaska
 */
public abstract class TypeProviderAccessor {

    public static TypeProviderAccessor DEFAULT;

    static {
        try {
            Class.forName(Context.class.getName(), true, Context.class.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public abstract Context createContext(Project p, String text, SearchType t);

    @NonNull
    public abstract Result createResult(@NonNull Collection<? super TypeDescriptor> result, @NonNull String[] message, @NonNull Context context);

    public abstract int getRetry(Result result);

    @NonNull
    public abstract String getHighlightText(@NonNull TypeDescriptor td);

    public abstract void setHighlightText(@NonNull TypeDescriptor td, @NonNull String text);
}
