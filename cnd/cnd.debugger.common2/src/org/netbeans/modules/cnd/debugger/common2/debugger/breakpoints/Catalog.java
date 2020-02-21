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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

import java.text.MessageFormat;

import java.awt.Component;

import org.openide.util.NbBundle;

class Catalog {
    public static String get(String key) {
	return NbBundle.getMessage(Catalog.class, key);
    }

    public static String format(String formatKey, Object... args) {
        return MessageFormat.format(get(formatKey), args);
    }

    public static void setAccessibleDescription(Component component, String key) {
	component.getAccessibleContext().
	    setAccessibleDescription(get(key));
    }

    public static void setAccessibleName(Component component, String key) {
	component.getAccessibleContext().
	    setAccessibleName(get(key));
    }

    public static char getMnemonic(String key) {
        return get(key).charAt(0);
    }

    public static int getMnemonicIndex(String key) {
        return Integer.parseInt(get(key));
    }
}
