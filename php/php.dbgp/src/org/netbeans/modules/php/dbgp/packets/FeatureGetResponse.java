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
package org.netbeans.modules.php.dbgp.packets;

import org.netbeans.modules.php.dbgp.DebugSession;
import org.w3c.dom.Node;

/**
 * @author ads
 *
 */
public class FeatureGetResponse extends FeatureSetResponse {
    private static final String SUPPORTED = "supported"; // NOI18N

    FeatureGetResponse(Node node) {
        super(node);
    }

    /**
     * This method does NOT mean that the feature is supported, this is encoded
     * in the text child of the response tag. The 'supported' attribute informs
     * whether the feature with 'feature_name' is supported by feature_get in
     * the engine, or when the command with name 'feature_get' is supported by
     * the engine.
     *
     * @return
     */
    public boolean isSupportedFeatureName() {
        String value = getAttribute(getNode(), SUPPORTED);
        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String getDetails() {
        return getNodeValue(getNode());
    }

    @Override
    public void process(DebugSession session, DbgpCommand command) {
    }

}
