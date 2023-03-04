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
package org.netbeans.modules.templatesui;

import javafx.embed.swing.JFXPanel;
import static org.junit.Assert.assertNotNull;
import org.junit.Assume;
import org.junit.AssumptionViolatedException;

class EnsureJavaFXPresent {
    private static final Throwable initError;
    static {
        Throwable t;
        try {
            JFXPanel p = new JFXPanel();
            assertNotNull("Allocated", p);
            t = null;
        } catch (RuntimeException | LinkageError err) {
            t = err;
        }
        initError = t;
    }
    
    private EnsureJavaFXPresent() {
    }
    
    static void checkAndThrow() {
        if (initError != null) {
            throw new AssumptionViolatedException("Cannot initialize JavaFX: " + initError.getMessage(), initError);
        }
    }
    
    static boolean check() {
        return initError == null;
    }
}
