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

package org.netbeans.modules.websvc.api.webservices;

/** Note: this class is not final, so as to allow implementors to derive from
 *  it to add additional implementation dependent properties such as wscompile
 *  features.
 *
 * @author Peter Williams, Martin Grebac
 */
public class StubDescriptor {

    /** Key to represent services generated from interface, e.g. SEI is under
     *  source control and the WSDL file is generated via wscompile.
     */
    public static final String SEI_SERVICE_STUB = "sei_service";

    /** Key to represent services generated from a preexisting WSDL file, e.g.
     *  wsdl file is under source control and the interface files are generated
     *  from it.
     */
    public static final String WSDL_SERVICE_STUB = "wsdl_service";
    
    
    // Private data
    private final String name;
    private final String displayName;

    public StubDescriptor(final String name, final String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String toString() {
        return displayName;
    }
}
