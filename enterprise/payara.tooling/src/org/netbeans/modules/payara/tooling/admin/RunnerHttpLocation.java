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
package org.netbeans.modules.payara.tooling.admin;

import java.util.HashMap;
import java.util.jar.Attributes;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpLocation extends RunnerHttp {

    /** Returned value is map where locations are stored under keys specified in
     * CommandLocation class.
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultMap<String, String> result;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using
     * HTTP interface.
     * <p/>
     * @param server  Payara server entity object.
     * @param command Payara server administration command entity.
     */
    public RunnerHttpLocation(final PayaraServer server,
            final Command command) {
        super(server, command);
    }

    @Override
    protected Result createResult() {
        return result = new ResultMap<String, String>();
    }

    @Override
    protected boolean processResponse() {
        if (manifest == null)
            return false;
        
        result.value = new HashMap<String, String>();
        Attributes mainAttrs = manifest.getMainAttributes();
            if(mainAttrs != null) {
                result.value.put("Base-Root_value", mainAttrs.getValue("Base-Root_value"));
                result.value.put("Domain-Root_value", mainAttrs.getValue("Domain-Root_value"));
                result.value.put("message", mainAttrs.getValue("message"));
            }

        return true;
    }
    
}
