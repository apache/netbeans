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
* System is not able to locate appropriate resources to create DatabaseSpecification object
* (object describing the database). It means that database product is not
* supported by system. You can use generic database system or write your
* own description file. If you are sure that it is, please check location
* of description files.
*
* @author Slavek Psenicka
*/
public class DatabaseProductNotFoundException extends Exception
{
    /** Database product name */
    private String sname;

    static final long serialVersionUID =-1108211224066947350L;
    /** Creates new exception
    * @param desc The text describing the exception
    */
    public DatabaseProductNotFoundException (String spec) {
        super ();
        sname = spec;
    }

    /** Creates new exception with text specified string.
    * @param spec Database product name
    * @param desc The text describing the exception
    */
    public DatabaseProductNotFoundException (String spec, String desc) {
        super (desc);
        sname = spec;
    }

    /** Returns database product name.
    * This database is not supported by system. You can use generic database 
    * system or write your own description file.
    */
    public String getDatabaseProductName()
    {
        return sname;
    }
}

/*
 * <<Log>>
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         9/10/99  Slavek Psenicka 
 *  4    Gandalf   1.3         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
 * $
 */
