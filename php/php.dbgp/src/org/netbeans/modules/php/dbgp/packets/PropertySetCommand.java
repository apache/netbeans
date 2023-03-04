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

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @author ads
 *
 */
public class PropertySetCommand extends PropertyCommand {
    static final String PROPERTY_SET = "property_set"; // NOI18N
    private static final String TYPE_ARG = "-t "; // NOI18N
    static final String ADDRESS_ARG = "-a "; // NOI18N
    private static final String LENGTH_ARG = "-l "; // NOI18N
    private String myDataType;
    private int myPropAddress;
    private String myData;

    public PropertySetCommand(String transactionId) {
        super(PROPERTY_SET, transactionId);
        myPropAddress = -1;
    }

    @Override
    public boolean wantAcknowledgment() {
        return true;
    }

    public void setDataType(String type) {
        myDataType = type;
    }

    public void setAddress(int address) {
        myPropAddress = address;
    }

    public void setData(String data) {
        myData = data;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    protected String getData() {
        return myData;
    }

    @Override
    protected String getArguments() {
        StringBuilder builder = new StringBuilder(super.getArguments());
        if (myDataType != null) {
            builder.append(BrkpntSetCommand.SPACE);
            builder.append(TYPE_ARG);
            builder.append(myDataType);
        }
        if (myPropAddress != -1) {
            builder.append(BrkpntSetCommand.SPACE);
            builder.append(ADDRESS_ARG);
            builder.append(myPropAddress);
        }
        if (getData() != null && getData().length() > 0) {
            try {
                int size = Base64.getEncoder().encodeToString(getData().getBytes(DbgpMessage.ISO_CHARSET)).length();
                builder.append(BrkpntSetCommand.SPACE);
                builder.append(LENGTH_ARG);
                builder.append(size);
            } catch (UnsupportedEncodingException e) {
                assert false;
            }
        }

        return builder.toString();
    }

}
