/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
            MODEL_INTERCEPTORS.addLookupListener(new LookupListener() {

                @Override
                public void resultChanged(LookupEvent ev) {
                    synchronized (instance) {
                        instance.extendingObjects = null;
                    }
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
        return new ArrayList<FunctionInterceptor>(FUNCTION_INTERCEPTORS.allInstances());
    }
    
    /**
     * Get all registered {@link ObjectCallProcessor}s.
     *
     * @return a list of all registered {@link ObjectCallProcessor}s; never
     * null.
     */
    public List<ObjectInterceptor> getObjectInterceptors() {
        return new ArrayList<ObjectInterceptor>(OBJECT_INTERCEPTORS.allInstances());
    }

    public List<TypeNameConvertor> getTypeNameConvertors() {
        return new ArrayList<TypeNameConvertor>(TYPE_DISPLAY_NAME_CONVERTORS.allInstances());
    }
    
    public synchronized List<? extends JsObject> getExtendingGlobalObjects(FileObject fo) {
        if (extendingObjects == null) {
            Collection<? extends ModelInterceptor> interceptors = MODEL_INTERCEPTORS.allInstances();
            extendingObjects = new ArrayList<JsObject>(interceptors.size());
            for (ModelInterceptor interceptor : interceptors) {
                extendingObjects.addAll(interceptor.interceptGlobal(
                        ModelElementFactoryAccessor.getDefault().createModelElementFactory(), fo));
            }
        }
        return extendingObjects;
    }
}
