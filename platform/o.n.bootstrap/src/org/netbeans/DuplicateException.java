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

package org.netbeans;

/** Exception indicating that a module with a given code name base
 * is already being managed, and that it is not permitted to add
 * another with the same name.
 * @author Jesse Glick
 */
public final class DuplicateException extends Exception {

    private transient Module old, nue;

    DuplicateException(Module old, Module nue) {
        // XXX if nue.jarFile == old.jarFile, produce special message
        super(getInfo(nue) + " is a duplicate of " + getInfo(old)); // NOI18N
        this.old = old;
        this.nue = nue;
    }
    private static String getInfo(Module m) {
        if (m.getHistory() != null) {
            return m.getHistory().toString();
        } else if (m.getJarFile() != null) {
            return m.getJarFile().getAbsolutePath();
        } else {
            return m.getCodeNameBase();
        }
    }
    
    /** Get the module which is already known to exist.
     */
    public Module getOldModule() {
        return old;
    }
    
    /** Get the module whose creation was attempted.
     * <strong>Warning:</strong> this module will be invalid,
     * so do not attempt to do anything with it beyond asking
     * it for its version and things like that.
     */
    public Module getNewModule() {
        return nue;
    }
    
}
