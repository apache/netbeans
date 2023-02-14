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

package org.netbeans.modules.debugger.jpda.ui.options;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.netbeans.api.debugger.Properties.Initializer;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 * Initializer of Java debugger options.
 * 
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(types={org.netbeans.api.debugger.Properties.Initializer.class})
public class OptionsInitializer implements Initializer {

    private static final String CLASS_FILTERS_ALL       = "debugger.sources.class_filters.all"; // NOI18N
    private static final String CLASS_FILTERS_ENABLED   = "debugger.sources.class_filters.enabled"; // NOI18N


    public String[] getSupportedPropertyNames() {
        return new String[] {
            CLASS_FILTERS_ALL,
            CLASS_FILTERS_ENABLED,
        };
    }

    public Object getDefaultPropertyValue(String propertyName) {
        if (CLASS_FILTERS_ALL.equals(propertyName)) {
            Set allFilters = new LinkedHashSet<String>();
            fillClassFilters(allFilters, false);
            return allFilters;
        }
        if (CLASS_FILTERS_ENABLED.equals(propertyName)) {
            Set<String> enabled = new HashSet<>();
            fillClassFilters(enabled, true);
            return enabled;
        }
        return null;
    }

    private static void fillClassFilters(Set filters, boolean enabled) {
        filters.add(ClassLoader.class.getName());
        filters.add(StringBuffer.class.getName());
        filters.add(StringBuilder.class.getName());
        filters.add("java.lang.AbstractStringBuilder");
        filters.add("java.lang.String");
        filters.add("java.lang.invoke.ConstantCallSite");   // JDK 8 Lambda
        filters.add("java.lang.invoke.Invokers");           // JDK 8 Lambda
        filters.add("java.lang.invoke.LambdaForm*");        // JDK 8 Lambda
        filters.add("REPL.*");                              // JDK 9 JShell generated classes
        if (!enabled) {
            filters.add("sun.*");
            filters.add("sunw.*");
        }
    }

}
