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
package org.netbeans.lib.profiler.classfile;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A representation of a binary Java class, which bytecode is loaded lazily.
 * Superclass and interfaces need to be specified in constructor, since they are
 * needed very early. Without it bytecode will be read for all classes and there
 * will be no laziness.
 *
 * @author Tomas Hurka
 */
public class LazyDynamicClassInfo extends DynamicClassInfo {
    private boolean isInitilaized;
    private boolean isInterface;
    
    public LazyDynamicClassInfo(String className, int loaderId, String classFileLocation,
            String superClassName, String[] interfaceNames) throws IOException {
        super(className, loaderId, classFileLocation, false);
        superName = superClassName;
        interfaces = interfaceNames;
    }
    
    public int getMethodIndex(String name, String sig) {
        if (initializeClassFile()) {
            return super.getMethodIndex(name, sig);
        }
        return -1;
    }

    public String[] getMethodNames() {
        if (initializeClassFile()) {
            return super.getMethodNames();
        }
        return new String[0];
    }

    public void preloadBytecode() {
        super.preloadBytecode();
        if (!isInitilaized) {
            ClassFileCache.getDefault().preloadBytecode(getName(), getClassFileLocation());
        }
    }

    public boolean isInterface() {
        if (!isInitilaized) {
            return isInterface;
        }
        return super.isInterface();
    }
    
    public void setInterface() {
        isInterface = true;
    }
    
    private boolean initializeClassFile() {
        if (!isInitilaized) {
            isInitilaized = true;
            try {
                parseClassFile(getName());
                return true;
            } catch (ClassFormatError ex) {
                Logger.getLogger(LazyDynamicClassInfo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(LazyDynamicClassInfo.class.getName()).log(Level.INFO, null, ex);
            }
            return false;
        }
        return true;
    }
}
