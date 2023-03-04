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

import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.utils.*;


/**
 *
 * @author Jaroslav Bachorik
 */
public class DefaultMethodNameFormatter implements MethodNameFormatter {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static final int VERBOSITY_CLASS = 1;
    public static final int VERBOSITY_METHOD = 2;
    public static final int VERBOSITY_CLASSMETHOD = 3;
    public static final int VERBOSITY_FULLMETHOD = 4;
    public static final int VERBOSITY_FULLCLASSMETHOD = 5;
    private static final int VERBOSITY_MIN = 1;
    private static final int VERBOSITY_MAX = 5;
    private static final int VERBOSITY_DEFAULT = VERBOSITY_CLASSMETHOD;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private int verbosity;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of DefaultMethodNameFormatter
     * @param verbosity verbosity level: 1 = getFormattedClass(), 2 = getFormattedMethod(), 3 = getFormattedClassAndMethod(),
     *                                   4 = getFullFormattedMethod(), 5 = getFullFormatedClassAndMethod()
     */
    public DefaultMethodNameFormatter(int verbosity) {
        this.verbosity = ((verbosity >= VERBOSITY_MIN) && (verbosity <= VERBOSITY_MAX)) ? verbosity : VERBOSITY_DEFAULT;
    }

    public DefaultMethodNameFormatter() {
        this.verbosity = VERBOSITY_DEFAULT;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Formattable formatMethodName(String className, String methodName, String signature) {
        return new PlainFormattableMethodName(className, methodName, signature, verbosity);
    }

    public Formattable formatMethodName(ClientUtils.SourceCodeSelection method) {
        return new PlainFormattableMethodName(method.getClassName(), method.getMethodName(), method.getMethodSignature(),
                                              verbosity);
    }
}
