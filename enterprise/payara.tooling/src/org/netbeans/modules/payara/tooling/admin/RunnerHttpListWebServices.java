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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

/**
 * Command runner for retrieving list of web services from server.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerHttpListWebServices extends RunnerHttp {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Payara administration command result containing server resources.
     * <p/>
     * Result instance life cycle is started with submitting task into
     * <code>ExecutorService</code>'s queue. method <code>call()</code>
     * is responsible for correct <code>TaskState</code> and receiveResult value
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
    public RunnerHttpListWebServices(final PayaraServer server,
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
     * Extracts result value from internal <code>Manifest</code> object.
     * Value of <i>message</i> attribute in <code>Manifest</code> object is
     * stored as <i>value</i> into <code>ResultString</code> result object.
     * <p/>
     * @return true if result was extracted correctly. <code>null</code>
     *         <i>message</i>value is considered as failure.
     */
    @Override
    protected boolean processResponse() {
            if(manifest == null) {
                return false;
            }
            result.value = new ArrayList<String>();
            Map <String, String> filter = new HashMap<String, String>();
            Iterator<String> keyIterator = manifest.getEntries().keySet().iterator();
            while (keyIterator.hasNext()) {
                String k = keyIterator.next();
                if (!k.contains("address:/")) // NOI18N
                    continue;
                if (k.contains("address:/wsat-wsat")) // NOI18N
                    continue;
                if (k.contains("address:/__wstx-services")) // NOI18N
                    continue;
                String a = k.replaceFirst(".* address:/", "").replaceFirst("\\. .*", ""); // NOI18N
                if (filter.containsKey(a))
                    continue;
                filter.put(a,a);
                result.value.add(a);
            }
        return true;
    }

}
