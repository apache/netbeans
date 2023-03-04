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

package org.netbeans.modules.java.source;

import com.sun.tools.javac.api.JavacTaskImpl;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ElementHandle;

/**
 *
 * @author Tomas Zezula
 */
public abstract class ElementHandleAccessor {

    private static volatile ElementHandleAccessor INSTANCE;

    public static ElementHandleAccessor getInstance() {
        ElementHandleAccessor result = INSTANCE;
        
        if (result == null) {
            synchronized (ElementHandleAccessor.class) {
                if (INSTANCE == null) {
                    Class c = ElementHandle.class;
                    try {
                        Class.forName(c.getName(), true, c.getClassLoader());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    
                    assert INSTANCE != null;
                }
                
                return INSTANCE;
            }
        }
        
        return result;
    }
    
    public static void setInstance(ElementHandleAccessor instance) {
        assert instance != null;
        INSTANCE = instance;
    }

    /** Creates a new instance of ElementHandleAccessor */
    protected ElementHandleAccessor() {
    }
    
    
    public abstract ElementHandle create (ElementKind kind, String... descriptors);
    
    public abstract <T extends Element> T resolve (ElementHandle<T> handle, JavacTaskImpl jti);

    @NonNull
    public abstract String[] getJVMSignature(@NonNull ElementHandle<?> handle);

}
