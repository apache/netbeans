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
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.payara.tooling.utils.JavaUtils;
import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Command runner for retrieving list of components from server.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpListComponents extends RunnerHttpTarget {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Payara administration command result containing server components.
     * <p/>
     * Result instance life cycle is started with submitting task into
     * <code>ExecutorService</code>'s queue. method <code>call()</code>
     * is responsible for correct <code>TaskState</code> and receiveResult value
     * handling.
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultMap<String, List<String>> result;

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
    public RunnerHttpListComponents(final PayaraServer server,
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
    protected ResultMap<String, List<String>> createResult() {
        return result = new ResultMap<String, List<String>>();
    }

   /**
     * Extracts result value from internal <code>Manifest</code> object.
     * Value of <i>message</i> attribute in <code>Manifest</code> object is
     * stored as <i>value</i> into <code>ResultString</code> result object.
     * <p/>
     * @return true if result was extracted correctly. <code>null</code>
     *         <i>message</i>value is considered as failure.
     */
    @Override
    protected boolean processResponse() {
        String resourcesAttr = manifest.getMainAttributes()
                .getValue("children");
        String[] components = resourcesAttr != null
                ? resourcesAttr.split(ServerUtils.MANIFEST_COMPONENTS_SEPARATOR)
                : null;
        result.value = new HashMap<String, List<String>>();
        if (components != null) {
            for (String component : components) {
                String decodedComponent;
                try {
                    decodedComponent = URLDecoder.decode(
                            component, JavaUtils.UTF_8.name());
                } catch (UnsupportedEncodingException ex) {
                    decodedComponent = component;
                }
                ServerUtils.addComponentToMap(result.value, decodedComponent);
            }
        }
        return true;
    }

}
