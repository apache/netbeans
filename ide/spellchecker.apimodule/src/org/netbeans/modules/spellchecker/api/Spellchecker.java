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

package org.netbeans.modules.spellchecker.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.text.JTextComponent;

import org.openide.util.Exceptions;
import org.openide.util.Lookup;


/**
 *
 * @author hanz
 * @since 1.3
 */
public class Spellchecker {

    public static void register (
        JTextComponent          textComponent
    ) {
        ClassLoader systemClassLoader = Lookup.getDefault ().lookup (ClassLoader.class);
        try {
            Class componentPeerClass = systemClassLoader.loadClass ("org.netbeans.modules.spellchecker.ComponentPeer");
            Method assureInstalledMethod = componentPeerClass.getMethod ("assureInstalled", Class.forName ("javax.swing.text.JTextComponent"));
            assureInstalledMethod.invoke (null, textComponent);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace (ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace (ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace (ex);
        } catch (ClassNotFoundException ex) {
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace (ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace (ex);
        }
    }
}
