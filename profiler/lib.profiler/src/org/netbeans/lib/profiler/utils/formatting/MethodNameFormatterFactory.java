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

package org.netbeans.lib.profiler.utils.formatting;

import org.netbeans.lib.profiler.marker.Mark;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author Jaroslav Bachorik
 */
public class MethodNameFormatterFactory {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static MethodNameFormatterFactory instance;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    // maps the method marks to appropriate formatters
    // @GuardedBy this
    private final Map formatterMap;
    private MethodNameFormatter defaultFormatter;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of MethodNameFormatterFactory
     */
    private MethodNameFormatterFactory() {
        formatterMap = new HashMap();
    }

    private MethodNameFormatterFactory(MethodNameFormatterFactory template) {
        formatterMap = template.formatterMap;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static synchronized MethodNameFormatterFactory getDefault() {
        if (instance == null) {
            instance = new MethodNameFormatterFactory();
            instance.defaultFormatter = new DefaultMethodNameFormatter();
        }

        return instance;
    }

    public static synchronized MethodNameFormatterFactory getDefault(MethodNameFormatter defaultFormatter) {
        MethodNameFormatterFactory factory = new MethodNameFormatterFactory(getDefault());
        factory.defaultFormatter = defaultFormatter;

        return factory;
    }

    public MethodNameFormatter getFormatter() {
        return defaultFormatter;
    }

    public synchronized MethodNameFormatter getFormatter(Mark mark) {
        if ((mark == null) || mark.isDefault()) {
            return defaultFormatter;
        }

        MethodNameFormatter formatter = (MethodNameFormatter) formatterMap.get(mark);

        if (formatter == null) {
            return defaultFormatter;
        }

        return formatter;
    }

    public synchronized void registerFormatter(Mark mark, MethodNameFormatter formatter) {
        formatterMap.put(mark, formatter);
    }
}
