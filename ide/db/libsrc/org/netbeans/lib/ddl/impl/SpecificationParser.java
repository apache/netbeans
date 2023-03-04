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

package org.netbeans.lib.ddl.impl;

import java.io.*;
import java.util.*;
import java.text.ParseException;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.util.PListReader;

/**
* SpecificationParser extends PListReader. It should be removed (it's functionality
* seems to be zero), but it's prepared here for future nonstandard implementations
* among the PListReader and it's use here.
*
* @author Slavek Psenicka
*/
public class SpecificationParser extends PListReader {

    /** Constructor */
    public SpecificationParser(String file)
    throws FileNotFoundException, ParseException, IOException
    {
        super(file);
    }

    /** Constructor */
    public SpecificationParser(InputStream stream)
    throws FileNotFoundException, ParseException, IOException
    {
        super(stream);
    }
}

/*
* <<Log>>
*/	
