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

package org.netbeans.insane.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 *
 * @author nenik
 */
public abstract class Root {

    private Root() {
    }
    
    public abstract String describe();
    public abstract Object getObject();
    
    public static Root createNamed(String name, Object ref) {
        return new Named(name, ref);
    }
    
    public static Root createStatic(Field f, Object ref) {
        return new Static(f, ref);
        
    }
    
    private static class Named extends Root {
        private String name;
        private Object ref;
        
        Named(String name, Object ref) {
            if (ref == null) new Exception().printStackTrace();
            this.ref = ref;
            this.name = name;
        }
        
        public Object getObject() {
            return ref;
        }
        
        public String describe() {
            return name;
        }
    }
    
    private static class Static extends Root {
        private Class cls;
        private int i;
        
        Static(Field f, Object ref) {
            assert ((f.getModifiers() & Modifier.STATIC) != 0);
            
            cls = f.getDeclaringClass();
            i = Arrays.asList(cls.getDeclaredFields()).indexOf(f);
            assert (i >= 0);
            assert cls.getDeclaredFields()[i].equals(f);
        }
        
        public String describe() {
            return getField().toString();
        }

        public Object getObject() {
            try {
                return getField().get(null);
            } catch (Exception e) {
                return null;
            }
        }

        private Field getField() {
            return cls.getDeclaredFields()[i];
        }
    }
    
}
