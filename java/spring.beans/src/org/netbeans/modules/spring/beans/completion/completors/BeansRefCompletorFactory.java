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
package org.netbeans.modules.spring.beans.completion.completors;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.spring.beans.completion.Completor;
import org.netbeans.modules.spring.beans.completion.CompletorFactory;
import org.openide.util.Exceptions;


/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class BeansRefCompletorFactory implements CompletorFactory {

    private boolean includeGlobal;
    private final Class<? extends BeansRefCompletor> clazz;

    public BeansRefCompletorFactory(boolean includeGlobal, Class<? extends BeansRefCompletor> clazz) {
        this.includeGlobal = includeGlobal;
        this.clazz = clazz;
    }

    public Completor createCompletor(int invocationOffset) {
        try {
            Constructor<? extends BeansRefCompletor> constructor = clazz.getConstructor(boolean.class, int.class);
            return constructor.newInstance(includeGlobal, invocationOffset);
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return null;
    }
}
