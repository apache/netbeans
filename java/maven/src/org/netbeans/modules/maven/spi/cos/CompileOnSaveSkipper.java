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
package org.netbeans.modules.maven.spi.cos;

import org.netbeans.modules.maven.api.execute.RunConfig;

/**
 * class testing if fast execution via compile on save (aka JavaRunner) is to be performed
 * or some state in the project's files prohibits such execution.
 * implementations to reside in global lookup.
 * @author mkleint
 * @since 2.61
 */
public interface CompileOnSaveSkipper {
    /**
     * 
     * @param config configuration to be executed
     * @param includingTests if true, care both about tests and main sources
     * @param timeStamp the last modified timestamp that we should check against
     * @return true if the javaRunner execution is to be skipped and regular maven build should be run.
     */
    boolean skip(RunConfig config, boolean includingTests, long timeStamp);
}
