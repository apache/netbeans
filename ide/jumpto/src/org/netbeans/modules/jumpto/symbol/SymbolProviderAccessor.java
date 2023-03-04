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

package org.netbeans.modules.jumpto.symbol;

import java.util.Collection;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.util.Exceptions;
import static org.netbeans.spi.jumpto.symbol.SymbolProvider.*;

/**
 *
 * @author Tomas Zezula
 */
public abstract class SymbolProviderAccessor {

    public static SymbolProviderAccessor DEFAULT;

    static {
        try {
            Class.forName(SymbolProvider.Context.class.getName(), true, SymbolProviderAccessor.class.getClassLoader());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public abstract SymbolProvider.Context createContext(Project p, String text, SearchType t);

    @NonNull
    public abstract SymbolProvider.Result createResult(
            @NonNull Collection<? super SymbolDescriptor> result,
            @NonNull String[] message,
            @NonNull Context context,
            @NonNull SymbolProvider provider);

    public abstract int getRetry(Result result);

    @NonNull
    public abstract String getHighlightText(@NonNull SymbolDescriptor desc);

    public abstract void setHighlightText(@NonNull SymbolDescriptor desc, @NonNull String text);

    @CheckForNull
    public abstract SymbolProvider getSymbolProvider(@NonNull SymbolDescriptor desc);

    public abstract void setSymbolProvider(@NonNull SymbolDescriptor desc, @NonNull SymbolProvider provider);
}
