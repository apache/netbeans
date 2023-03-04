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

import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;

/**
 *
 * @author Petr Hejl
 */
public abstract class ModelElementFactoryAccessor {

    private static volatile ModelElementFactoryAccessor DEFAULT;

    public static ModelElementFactoryAccessor getDefault() {
        ModelElementFactoryAccessor a = DEFAULT;
        if (a != null) {
            return a;
        }

        // invokes static initializer of ModelElementFactory.class
        // that will assign value to the DEFAULT field above
        Class c = ModelElementFactory.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
        return DEFAULT;
    }

    public static void setDefault(ModelElementFactoryAccessor accessor) {
        if (DEFAULT != null) {
            throw new IllegalStateException();
        }

        DEFAULT = accessor;
    }

    public abstract ModelElementFactory createModelElementFactory();
}
