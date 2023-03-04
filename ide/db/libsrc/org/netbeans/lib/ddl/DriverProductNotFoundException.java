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

/**
* System is not able to locate appropriate resources to create DriverSpecification object
* (object describing the driver). It means that driver product is not supported by system.
* You can write your own description file. If you are sure that it is, please check
* location of description files.
*
* @author Radko Najman
*/
public class DriverProductNotFoundException extends Exception
{
    static final long serialVersionUID =-1108211224066947350L;

    /** Driver name */
    private String drvName;

    /** Creates new exception
    * @param desc The text describing the exception
    */
    public DriverProductNotFoundException(String spec) {
        super ();
        drvName = spec;
    }

    /** Creates new exception with text specified string.
    * @param spec Driver name
    * @param desc The text describing the exception
    */
    public DriverProductNotFoundException(String spec, String desc) {
        super (desc);
        drvName = spec;
    }

    /** Returns driver name.
    * This driver is not supported by system. You can write your own description file.
    */
    public String getDriverName()
    {
        return drvName;
    }
}

/*
 * <<Log>>
 *  1    Gandalf   1.0         12/15/99 Radko Najman    
 * $
 */
