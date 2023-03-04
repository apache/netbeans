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

package org.netbeans.modules.schema2beans;

import java.io.*;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author cliffwd
 * An org.xml.sax.EntityResolver that does nothing.  Good for those times
 * when you want 0 validation done, and to make sure it's not going to the
 * network for a DTD lookup.
 */
public class NullEntityResolver implements EntityResolver {
    //protected String dummyDTD = "<!ELEMENT dummy EMPTY >\n";
    protected String dummyDTD = "";
    protected byte[] buf = dummyDTD.getBytes();
    protected static NullEntityResolver theResolver = null;

    private NullEntityResolver() {
    }

    public static NullEntityResolver newInstance() {
        if (theResolver == null)
            theResolver = new NullEntityResolver();
        return theResolver;
    }
    
    public InputSource resolveEntity(String publicId, String systemId)
    {
        //System.out.println("resolveEntity: publicId="+publicId+" systemId="+systemId);
        ByteArrayInputStream bin = new ByteArrayInputStream(buf);
        return new InputSource(bin);
    }
}
