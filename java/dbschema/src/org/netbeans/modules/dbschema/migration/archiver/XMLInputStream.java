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

import org.netbeans.modules.dbschema.migration.archiver.deserializer.XMLGraphDeserializer;

import org.xml.sax.*;

import java.io.InputStream;

/**
 *
 * @author  Administrator
 * @version
 */
public class XMLInputStream extends java.io.DataInputStream implements java.io.ObjectInput
{

    private InputStream inStream;
    private ClassLoader classLoader;

    //@lars: added classloader-constructor
    /** Creates new XMLInputStream with the given classloader*/
    public XMLInputStream(InputStream in,
                          ClassLoader cl)
    {
        super(in);
        this.inStream = in;
        this.classLoader = cl;
    }

    /** Creates new XMLInputStream */
    public XMLInputStream(InputStream in)
    {
        this (in, null);
    }

    public java.lang.Object readObject() throws java.lang.ClassNotFoundException, java.io.IOException
    {

        try
        {

            XMLGraphDeserializer lSerializer = new XMLGraphDeserializer(this.classLoader);
            lSerializer.Begin();
            InputSource input = new InputSource(this.inStream);
            input.setSystemId("archiverNoID");

            lSerializer.setSource(input);

            return lSerializer.XlateObject();
        }
        catch (SAXException lError)
        {
            lError.printStackTrace();
            java.io.IOException lNewError = new java.io.IOException(lError.getMessage());
            throw lNewError;
        }
    }

}
