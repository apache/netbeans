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

package add;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.WebResult;
import add.foo.FooException;

/**
 *
 * @author mkuchtiak
 */
@WebService()
public class AddNumbers {

    /**
     * add Method
     * @param x first number
     * @param y second number
     * @return SUM of 2 numbers
     */    
    @WebMethod()
    @WebResult(name="sum", targetNamespace = "http://www.netbeans.org/sum")
    public int add(@WebParam(name = "x", targetNamespace = "http://www.netbeans.org/sum/x")
                   int x,
                   @WebParam(name = "y", targetNamespace = "http://www.netbeans.org/sum/y")
                   int y) {
        // TODO write your implementation code here:
        return 0;
    }

    /**
     * Web service operation
     * @return echo text
     */
    @WebMethod(operationName="echo-operation")
    public String echo() throws FooException {
        // TODO write your implementation code here:
        return "hello";
    }
    
    /**
     * Non Web service operation
     */    
    public String echo1() {
        // TODO write your implementation code here:
        return "hello";
    }

    @WebMethod
    @Oneway
    public void send(@WebParam(name = "message")
                     String str) {
    }
    
}
