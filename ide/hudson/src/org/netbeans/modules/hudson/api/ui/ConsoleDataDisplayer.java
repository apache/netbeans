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
package org.netbeans.modules.hudson.api.ui;

import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.spi.BuilderConnector.ConsoleDataProvider;
import org.netbeans.modules.hudson.spi.ConsoleDataDisplayerImpl;

/**
 * Displayer of console data. Insances of this class will be passed to
 * {@link ConsoleDataProvider#showConsole(HudsonJobBuild, ConsoleDataDisplayer)}
 * and
 * {@link ConsoleDataProvider#showConsole(HudsonMavenModuleBuild, ConsoleDataDisplayer)}.
 *
 * Do not implement this interface in your classes. Use
 * {@link ConsoleDataDisplayerImpl} instead.
 *
 * @author jhavlin
 */
public interface ConsoleDataDisplayer {

    /**
     * Prepare the console displayer for writing, initilize needed resources.
     */
    void open();

    /**
     * Write one line to the console.
     *
     * @param line Line of console output.
     * @return True if writing was successfull, false otherwise.
     */
    boolean writeLine(String line);

    /**
     * Finish writing to the console displayer, close all resources.
     */
    void close();
}
