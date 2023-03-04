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
public class PropertyValueCommand extends PropertyGetCommand {
    static final String PROPERTY_VALUE = "property_value"; // NOI18N
    private int myPropAddress;

    public PropertyValueCommand(String transactionId) {
        super(PROPERTY_VALUE, transactionId);
        myPropAddress = -1;
    }

    public void setAddress(int address) {
        myPropAddress = address;
    }

    @Override
    protected String getArguments() {
        StringBuilder builder = new StringBuilder(super.getArguments());
        if (myPropAddress != -1) {
            builder.append(BrkpntSetCommand.SPACE);
            builder.append(PropertySetCommand.ADDRESS_ARG);
            builder.append(myPropAddress);
        }
        return builder.toString();
    }

}
