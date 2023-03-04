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
package org.netbeans.modules.javascript2.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.spi.FunctionInterceptor;
import org.netbeans.modules.javascript2.model.spi.ModelInterceptor;
import org.netbeans.modules.javascript2.model.spi.ObjectInterceptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.javascript2.model.spi.TypeNameConvertor;

/**
 *
 * @author Petr Hejl, Petr Pisl
 */
public final class ModelExtender {

    public static final String MODEL_INTERCEPTORS_PATH = "JavaScript/Model/ModelInterceptors";                      //NOI18N

    public static final String FUNCTION_INTERCEPTORS_PATH = "JavaScript/Model/FunctionInterceptors";                //NOI18N

    public static final String OBJECT_INTERCEPTORS_PATH = "JavaScript/Model/ObjectInterceptors";                    //NOI18N

    public static final String TYPE_NAME_CONVERTORS_PATH = "JavaScript/Model/TypeNameConvertors";    //NOI18N

    private static final Lookup.Result<ModelInterceptor> MODEL_INTERCEPTORS =
            Lookups.forPath(MODEL_INTERCEPTORS_PATH).lookupResult(ModelInterceptor.class);

    private static final Lookup.Result<FunctionInterceptor> FUNCTION_INTERCEPTORS =
            Lookups.forPath(FUNCTION_INTERCEPTORS_PATH).lookupResult(FunctionInterceptor.class);

    private static final Lookup.Result<ObjectInterceptor> OBJECT_INTERCEPTORS =
            Lookups.forPath(OBJECT_INTERCEPTORS_PATH).lookupResult(ObjectInterceptor.class);

    private static final Lookup.Result<TypeNameConvertor> TYPE_DISPLAY_NAME_CONVERTORS =
            Lookups.forPath(TYPE_NAME_CONVERTORS_PATH).lookupResult(TypeNameConvertor.class);

    private static ModelExtender instance;

    private List<JsObject> extendingObjects;

    private ModelExtender() {
        super();
    }

    public static synchronized ModelExtender getDefault() {
        if (instance == null) {
            instance = new ModelExtender();
            MODEL_INTERCEPTORS.addLookupListener((LookupEvent ev) -> {
                synchronized (instance) {
                    instance.extendingObjects = null;
                }
            });
        }
        return instance;
    }

    /**
     * Get all registered {@link MethodCallProcessor}s.
     *
     * @return a list of all registered {@link MethodCallProcessor}s; never
     * null.
     */
    public List<FunctionInterceptor> getFunctionInterceptors() {
        return new ArrayList<>(FUNCTION_INTERCEPTORS.allInstances());
    }

    /**
     * Get all registered {@link ObjectCallProcessor}s.
     *
     * @return a list of all registered {@link ObjectCallProcessor}s; never
     * null.
     */
    public List<ObjectInterceptor> getObjectInterceptors() {
        return new ArrayList<>(OBJECT_INTERCEPTORS.allInstances());
    }

    public List<TypeNameConvertor> getTypeNameConvertors() {
        return new ArrayList<>(TYPE_DISPLAY_NAME_CONVERTORS.allInstances());
    }

    public synchronized List<? extends JsObject> getExtendingGlobalObjects(FileObject fo) {
        if (extendingObjects == null) {
            Collection<? extends ModelInterceptor> interceptors = MODEL_INTERCEPTORS.allInstances();
            extendingObjects = new ArrayList<>(interceptors.size());
            for (ModelInterceptor interceptor : interceptors) {
                extendingObjects.addAll(interceptor.interceptGlobal(
                        ModelElementFactoryAccessor.getDefault().createModelElementFactory(), fo));
            }
        }
        return extendingObjects;
    }
}
