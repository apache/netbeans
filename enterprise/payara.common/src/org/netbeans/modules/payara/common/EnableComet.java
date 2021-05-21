/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.payara.common;

import org.netbeans.modules.payara.tooling.admin.CommandSetProperty;
import org.netbeans.modules.payara.tooling.admin.ResultMap;
import org.netbeans.modules.payara.tooling.admin.CommandGetProperty;
import org.netbeans.modules.payara.tooling.admin.ResultString;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.spi.PayaraModule;

/**
 *
 * @author vkraemer
 */
public class EnableComet implements Runnable {
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = PayaraLogger.get(CommonServerSupport.class);

    
    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

     /** Payara server instance to be modified. */
    private final PayaraInstance instance;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Comet support enable handler.
     * @param instance Payara server instance to be modified.
     */
    public EnableComet(PayaraInstance instance) {
        this.instance = instance;
    }

    /**
     * Thread execution method.
     */
    @Override
    public void run() {
        String propertiesPattern = "*.comet-support-enabled";
        try {
            ResultMap<String, String> result = CommandGetProperty.getProperties(
                    instance, propertiesPattern,
                    CommonServerSupport.PROPERTIES_FETCH_TIMEOUT);
            if (result.getState() == TaskState.COMPLETED) {
                String newValue
                        = instance.getProperty(PayaraModule.COMET_FLAG);
                if (null == newValue || newValue.trim().length() < 1) {
                    newValue = "false"; // NOI18N
                }
                for (Entry<String, String> entry
                        : result.getValue().entrySet()) {
                    String key = entry.getKey();
                    // do not update the admin listener....
                    if (null != key && !key.contains("admin-listener")) {
                        CommandSetProperty command
                                = PayaraInstanceProvider.getProvider()
                                .getCommandFactory().getSetPropertyCommand(
                                key, newValue);
                        ResultString setResult = CommandSetProperty.setProperty(
                                instance, command,
                                CommonServerSupport.PROPERTIES_FETCH_TIMEOUT);  
                    }
                }
                
            }
        } catch (PayaraIdeException gfie) {
            LOGGER.log(Level.INFO,
                    "Could not get comment-support-enabeld value.", gfie);
        }
    }

}
