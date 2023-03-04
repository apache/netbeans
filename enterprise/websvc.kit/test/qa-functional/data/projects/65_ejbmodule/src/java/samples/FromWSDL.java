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

import javax.ejb.Stateless;
import javax.jws.WebService;
import org.example.duke.AddNumbersFault_Exception;
import org.example.duke.AddNumbersPortType;

/**
 *
 * @author lukas
 */
@WebService(serviceName = "AddNumbersService", portName = "AddNumbersPort", endpointInterface = "org.example.duke.AddNumbersPortType", targetNamespace = "http://duke.example.org", wsdlLocation = "META-INF/wsdl/FromWSDL/AddNumbers.wsdl")
@Stateless
public class FromWSDL implements AddNumbersPortType {

    public int addNumbers(int arg0, int arg1) throws AddNumbersFault_Exception {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void oneWayInt(int arg0) {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
