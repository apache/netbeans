/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
