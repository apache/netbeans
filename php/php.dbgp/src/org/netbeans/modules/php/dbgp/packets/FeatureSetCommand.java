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

/**
 * @author ads
 *
 */
public class FeatureSetCommand extends FeatureGetCommand {
    static final String FEATURE_SET = "feature_set"; // NOI18N
    private static final String VALUE_ARG = "-v "; // NOI18N
    private String myValue;

    public FeatureSetCommand(String transactionId) {
        super(FEATURE_SET, transactionId);
    }

    @Override
    public boolean wantAcknowledgment() {
        return true;
    }

    public void setValue(String value) {
        myValue = value;
    }

    @Override
    protected String getArguments() {
        StringBuilder builder = new StringBuilder(super.getArguments());
        builder.append(BrkpntSetCommand.SPACE);
        builder.append(VALUE_ARG);
        builder.append(myValue);
        return builder.toString();
    }

}
