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

package samples;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author lukas
 */
@WebService(serviceName = "SampleWebService")
public class SampleWs {

    /**
     * Web service operation
     */
    @WebMethod(operationName = "voidOperation")
    @Oneway
    public void voidOperation() {
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "sayHi")
    public String sayHi(
            @WebParam(name = "s") String s) {
        return "Hello " + s + "!";
    }
}
