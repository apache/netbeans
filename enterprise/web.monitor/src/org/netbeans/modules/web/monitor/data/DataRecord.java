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

package org.netbeans.modules.web.monitor.data;


/**
 * DataRecord.java
 *
 *
 * Created: Wed Mar 20 19:15:16 2002
 *
 * @author Ana von Klopp
 * @version
 */

public interface DataRecord  {


    public void setAttributeValue(String attr, String value);
    public String getAttributeValue(String attr);

    public void setClientData(ClientData value);

    public ClientData getClientData();

    public void setSessionData(SessionData value);

    public SessionData getSessionData();

    public void setCookiesData(CookiesData value);

    public CookiesData getCookiesData();

    public void setDispatches(Dispatches value);

    public Dispatches getDispatches();

    public void setRequestData(RequestData value);

    public RequestData getRequestData();

    public void setServletData(ServletData value);

    public ServletData getServletData();

    public void setEngineData(EngineData value);

    public EngineData getEngineData();

    public void setContextData(ContextData value);

    public ContextData getContextData();

} // DataRecord
