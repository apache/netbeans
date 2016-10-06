/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package org.jetbrains.kotlin.log;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.openide.util.Exceptions;


public class KotlinLogger {
    
    public final static KotlinLogger INSTANCE = new KotlinLogger();
    private final static Logger LOGGER = 
            Logger.getLogger(KotlinLogger.class.getName());
    
    private KotlinLogger() {
        System.setProperty(KotlinLogger.class.getName(), "100");
        try {
            LogManager.getLogManager().readConfiguration();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void logException(String message, Throwable thrown) {
        LOGGER.log(Level.SEVERE, message, thrown);
    }
    
    public void logWarning(String message) {
        LOGGER.warning(message);
    }
    
    public void logInfo(String message) {
        LOGGER.log(Level.INFO, message);
    }
    
}
