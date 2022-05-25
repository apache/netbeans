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
package org.netbeans.modules.groovy.debug;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.SmartSteppingCallback;

/**
 * Stepping in Groovy, steps through the language implementation.
 */
@SmartSteppingCallback.Registration(path="netbeans-JPDASession")
public class GroovySmartStepping extends SmartSteppingCallback {

    private static final String[] GROOVY_PACKAGES = { "org.codehaus.groovy.", "org.apache.groovy.", "groovy.", "groovyjar" };   // NOI18N
    private static final Set<String> PATTERNS_SKIP_IN_GROOVY = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("java.", "jdk.internal.", "sun.", "com.sun."))); // NOI18N
    private static final Set<String> FILTERS_SKIP_IN_GROOVY = PATTERNS_SKIP_IN_GROOVY.stream().map(pattern -> pattern + '*').collect(Collectors.toSet());

    private static final Logger logger = Logger.getLogger(GroovySmartStepping.class.getName());

    private final Map<SmartSteppingFilter, Boolean> steppingInGroovy = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public void initFilter (SmartSteppingFilter filter) {
    }

    @Override
    public boolean stopHere (ContextProvider lookupProvider, JPDAThread thread, SmartSteppingFilter filter) {
        return true;
    }

    @Override
    public StopOrStep stopAt(ContextProvider lookupProvider, CallStackFrame frame, SmartSteppingFilter filter) {
        String className = frame.getClassName();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("GroovySmartStepping.stopAt("+className+")");
        }
        JPDAThread thread = frame.getThread();
        boolean inGroovy = false;
        for (String gp : GROOVY_PACKAGES) {
            if (className.startsWith(gp)) {
                inGroovy = true;
                steppingInGroovy.put(filter, true);
                break;
            }
        }
        if (inGroovy) {
            // We need to step through the Groovy packages
            logger.fine(" => In Groovy: Step In");
            return StopOrStep.step(0, JPDAStep.STEP_INTO);
        }
        if (Boolean.TRUE.equals(steppingInGroovy.get(filter))) {
            if (isPackageToSkip(className)) {
                logger.fine(" => Was In Groovy: Step In");
                filter.addExclusionPatterns(FILTERS_SKIP_IN_GROOVY);
                return StopOrStep.step(0, JPDAStep.STEP_INTO);
            } else {
                logger.fine(" => FINISHED, stop");
                return StopOrStep.stop();
            }
        } else {
            logger.fine(" => FINISHED (no Groovy), stop");
            return StopOrStep.stop();
        }
    }

    private static boolean isPackageToSkip(String className) {
        for (String pattern : PATTERNS_SKIP_IN_GROOVY) {
            if (className.startsWith(pattern)) {
                return true;
            }
        }
        return false;
    }

}
