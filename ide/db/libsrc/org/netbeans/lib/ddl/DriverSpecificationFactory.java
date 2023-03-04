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

package org.netbeans.lib.ddl;

import java.sql.*;
import java.util.Set;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;

/**
* The factory interface used for creating instances of DriverSpecification class.
* DriverSpecificationFactory collects information about available driver
* description files. Then it's able to specify if system can control
* the driver (specified by product name). It also provides a list of supported
* drivers.
*
* @author Radko Najman
*/
public interface DriverSpecificationFactory {

    /** Returns array of driver products supported by system. It returns
    * string array only, not the DriverSpecification array.
    */
    public Set supportedDrivers();

    /** Returns true if driver (specified by driverName) is
    * supported by system. Does not throw exception if it doesn't.
    * @param ddriverName Driver product name as given from DatabaseMetaData
    * @return True if driver product is supported.
    */	
    public boolean isDriverSupported(String driverName);

    /** Creates instance of DriverSpecification class; a driver-specification
    * class. This object knows about used driver.
    * @param driverName Driver name
    * @return DriverSpecification object.
    */
    public DriverSpecification createDriverSpecification(String driverName);

}

/*
* <<Log>>
*/
