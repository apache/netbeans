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

package org.netbeans.modules.dbschema.migration.archiver;

import org.netbeans.modules.dbschema.migration.archiver.serializer.XMLGraphSerializer;

import java.io.OutputStream;

/**
 *
 * @author  Administrator
 * @version
 */
public class XMLOutputStream extends java.io.DataOutputStream implements java.io.ObjectOutput
{

    private OutputStream outStream;

    /** Creates new XMLOutputStream */
    public XMLOutputStream(OutputStream out)
    {
        super(out);
        this.outStream = out;
    }

    public void close() throws java.io.IOException
    {
        this.outStream.close();
    }

    public void writeObject(Object o) throws java.io.IOException
    {

        XMLGraphSerializer lSerial = new XMLGraphSerializer(this.outStream);
        lSerial.writeObject(o);
        
    }
}
