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
package org.netbeans.modules.htmlui.impl;

import javafx.embed.swing.JFXPanel;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.SkipException;

public final class EnsureJavaFXPresent {
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
    
    public static void checkAndThrow() {
        if (initError != null) {
            throw new SkipException("Cannot initialize JavaFX", initError);
        }
    }
    
    public static boolean check() {
        return initError == null;
    }
}
