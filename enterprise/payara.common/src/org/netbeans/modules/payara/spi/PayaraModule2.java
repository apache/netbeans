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

package org.netbeans.modules.payara.spi;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.netbeans.modules.payara.tooling.TaskStateListener;
import org.netbeans.modules.payara.tooling.admin.ResultString;

/**
 * Extended version of PayaraModule supporting deployment of standalone
 * EE module with libraries they require.
 * 
 * @since org.netbeans.modules.payara.common/0 1.0
 */
public interface PayaraModule2 extends PayaraModule {

    /**
     * @param libraries array of jar files on which standalone EE module depends
     *  and which need to be part of deployment
     */
    Future<ResultString> deploy(final TaskStateListener stateListener,
            final File application, final String name, final String contextRoot,
            final Map<String,String> properties, final File[] libraries);

    /**
     * Redeploy the named application onto the server.The application must have
     * previously been directory deployed.If not, use deploy().
     *
     * @param stateListener listener to listen message describing the redeploy
     * process as it progresses. Can be null.
     * @param name name this application is deployed under.
     * @param contextRoot
     * @param libraries array of jar files on which standalone EE module depends
     *  and which need to be part of deployment
     * @param resourcesChanged Is application resource file modified.
     * @param metadataChanged Is application metadata files modified.
     * @param sourcesChanged the list of source file modified.
     * @return
     */
    Future<ResultString> redeploy(final TaskStateListener stateListener, 
            final String name, final String contextRoot, File[] libraries,
            boolean resourcesChanged, boolean metadataChanged, List<String> sourcesChanged);
    
}
