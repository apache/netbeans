/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.htmlui;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import org.netbeans.api.htmlui.HTMLDialog.OnSubmit;
import org.netbeans.spi.htmlui.HTMLViewerSpi;
import org.openide.util.Lookup;

public abstract class ContextAccessor {
    private static ContextAccessor DEFAULT;

    protected ContextAccessor() {
        synchronized (ContextAccessor.class) {
            if (DEFAULT != null) {
                throw new IllegalStateException();
            }
            DEFAULT = this;
        }
    }

    public static ContextAccessor getDefault() {
        if (DEFAULT == null) {
            try {
                final Class<HTMLViewerSpi.Context> clazz = HTMLViewerSpi.Context.class;
                Class.forName(clazz.getName(), true, clazz.getClassLoader());
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return DEFAULT;
    }

    public abstract HTMLViewerSpi.Context newContext(
        ClassLoader loader, URL url, String[] resources, String[] techIds,
        OnSubmit onSubmit, Consumer<String> lifeCycleCallback, Callable<Lookup> onPageLoad,
        Class<?> component
    );
}
