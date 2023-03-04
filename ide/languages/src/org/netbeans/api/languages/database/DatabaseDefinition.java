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
package org.netbeans.api.languages.database;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DatabaseDefinition extends DatabaseItem {

    private String                  name;
    private String                  type;
    private List<DatabaseUsage>     usages;
    private URL                     sourceFileUrl;

    public DatabaseDefinition (
        String name,
        String type,
        int offset, 
        int endOffset
    ) {
        super (offset, endOffset);
        this.name = name;
        this.type = type;
    }

    public String getName () {
        return name;
    }

    public String getType () {
        return type;
    }
    
    public void addUsage (DatabaseUsage usage) {
        if (usages == null) usages = new ArrayList<DatabaseUsage> ();
        usages.add (usage);
    }
    
    public List<DatabaseUsage> getUsages () {
        if (usages == null)
            return Collections.<DatabaseUsage>emptyList ();
        return usages;
    }
    
    public void setSourceFileUrl(URL url) {
        this.sourceFileUrl = url;
    }
    
    public URL getSourceFileUrl() {
        return sourceFileUrl;
    }
    
    public String toString () {
        return "Definition " + getName () + " (" + getType () + ")";
    }
    
    public static DatabaseDefinition load (DataInputStream is) throws IOException {
        return new DatabaseDefinition (is.readUTF (), is.readUTF (), is.readInt (), is.readInt ());
    }
    
    public void save (DataOutputStream os) throws IOException {
        os.writeUTF (name);
        os.writeUTF (type);
        os.writeInt (getOffset ());
        os.writeInt (getEndOffset ());
    }
}
