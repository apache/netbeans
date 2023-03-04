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

package org.netbeans.modules.db.explorer;

import java.util.*;
import java.io.*;
import org.netbeans.api.db.explorer.JDBCDriver;

// XXX this class is completely unuseful and should be removed

/**
* xxx
*
* @author Slavek Psenicka
*/
public class DatabaseDriver extends Object implements Externalizable
{
    private String name;
    private String url;
    private String prefix;
    private String adaptor;
    private transient JDBCDriver jdbcDriver;

    static final long serialVersionUID =7937512184160164098L;
    public DatabaseDriver()
    {
    }

    public DatabaseDriver(String dname, String durl)
    {
        name = dname;
        url = durl;
    }

    public DatabaseDriver(String dname, String durl, String dprefix)
    {
        name = dname;
        url = durl;
        prefix = dprefix;
    }
    
    public DatabaseDriver(String dname, String durl, String dprefix, JDBCDriver djdbcDriver)
    {
        name = dname;
        url = durl;
        prefix = dprefix;
        jdbcDriver = djdbcDriver;
    }

    public DatabaseDriver(String dname, String durl, String dprefix, String dbadap)
    {
        name = dname;
        url = durl;
        prefix = dprefix;
        adaptor = dbadap;
    }

    public String getName()
    {
        if (name != null) return name;
        return url;
    }

    public void setName(String nname)
    {
        name = nname;
    }

    public String getURL()
    {
        return url;
    }

    public void setURL(String nurl)
    {
        url = nurl;
    }

    public String getDatabasePrefix()
    {
        return prefix;
    }

    public void setDatabasePrefix(String pref)
    {
        prefix = pref;
    }

    public String getDatabaseAdaptor()
    {
        return adaptor;
    }

    public void setDatabaseAdaptor(String name)
    {
        if (name == null || name.length() == 0) adaptor = null;
        else if (name.startsWith("Database.Adaptors.")) adaptor = name; //NOI18N
        else adaptor = "Database.Adaptors."+name; //NOI18N
        //		System.out.println("Metadata adaptor class set = "+adaptor);
    }
    
    public JDBCDriver getJDBCDriver() {
        return jdbcDriver;
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof String) return obj.equals(url);
        boolean c1 = ((DatabaseDriver)obj).getURL().equals(url);
        boolean c2 = ((DatabaseDriver)obj).getName().equals(name);
        return c1 && c2;
    }

    public String toString()
    {
        return getName();
    }

    /** Writes data
    * @param out ObjectOutputStream
    */
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeObject(name);
        out.writeObject(url);
        out.writeObject(prefix);
        out.writeObject(adaptor);
    }

    /** Reads data
    * @param in ObjectInputStream
    */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        name = (String)in.readObject();
        url = (String)in.readObject();
        prefix = (String)in.readObject();
        adaptor = (String)in.readObject();
    }
}
