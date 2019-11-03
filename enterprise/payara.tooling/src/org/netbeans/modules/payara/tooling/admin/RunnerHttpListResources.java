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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.jar.Attributes;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Command runner for retrieving resources from server.
 * <p>
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpListResources extends RunnerHttpTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Payara administration command result containing server resources.
     * <p/>
     * Result instance life cycle is started with submitting task into
     * <code>ExecutorService</code>'s queue. method
     * <code>call()</code>
     * is responsible for correct
     * <code>TaskState</code> and receiveResult value
     * handling.
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultList<String> result;

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
    public RunnerHttpListResources(final PayaraServer server,
            final Command command) {
        super(server, command);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create <code>ResultList</code> object corresponding
     * to server log command execution value to be returned.
     */
    @Override
    protected ResultList<String> createResult() {
        return result = new ResultList<String>();
    }

    /**
     * Extracts result value from internal
     * <code>Manifest</code> object.
     * Value of <i>message</i> attribute in
     * <code>Manifest</code> object is
     * stored as <i>value</i> into
     * <code>ResultString</code> result object.
     * <p/>
     * @return true if result was extracted correctly. <code>null</code>
     *         <i>message</i>value is considered as failure.
     */
    @Override
    protected boolean processResponse() {
        String resourcesAttr = manifest.getMainAttributes()
                .getValue("children");
        String[] resources = resourcesAttr != null
                ? resourcesAttr.split(ServerUtils.MANIFEST_RESOURCES_SEPARATOR)
                : null;
        int resoucesCount = resources != null ? resources.length : 0;
        result.value = new ArrayList<String>(resoucesCount);
        if (resources != null) {
            for (String resource : resources) {
                Attributes resourceAttr = manifest.getAttributes(resource);
                String resourceMsg = resourceAttr.getValue("message");
                String name;
                try {
                    if (resourceMsg != null) {
                        name = URLDecoder.decode(resourceMsg, "UTF-8");
                    } else {
                        name = null;
                    }
                    if (name == null || name.length() <= 0) {
                        name = URLDecoder.decode(resource.trim(), "UTF-8");
                    }
                } catch (UnsupportedEncodingException uee) {
                    throw new CommandException(
                            CommandException.HTTP_RESP_UNS_ENC_EXCEPTION, uee);
                }
                result.value.add(name);
            }
        }
        return true;
    }
}
