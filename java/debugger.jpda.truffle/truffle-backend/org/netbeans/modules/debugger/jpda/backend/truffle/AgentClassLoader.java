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
package org.netbeans.modules.debugger.jpda.backend.truffle;

/**
 * The classes of Truffle debugging backend are loaded in this class loader,
 * to be isolated from the guest application.
 * <p>
 * This class loader is exported to Truffle
 * (by com.oracle.truffle.polyglot.LanguageCache$Loader.exportTruffle())
 * to be able to access Truffle module code on JDK 9+.
 */
public final class AgentClassLoader extends ClassLoader {

    public AgentClassLoader() throws ClassNotFoundException {
        super(getTruffleClassLoader());
    }

    private static ClassLoader getTruffleClassLoader() throws ClassNotFoundException {
        Class truffleClass = Class.forName("com.oracle.truffle.api.Truffle");
        return truffleClass.getClassLoader();
    }

}
