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
/*
 * ModuleEntry.java
 *
 * Created on October 8, 2002, 4:01 PM
 */

package org.netbeans.performance.impl.logparsing;
import org.netbeans.performance.spi.*;
import java.util.*;
/**Wrapper class for a module entry line in a NetBeans log.
 *
 * @author  Tim Boudreau
 */
public class ModuleEntry extends ValueLogElement {
    String qualifiedName;
    String simpleName;
    int specversion;
    Date builddate;

    public ModuleEntry(String s) {
        super(s);
    }

    protected void parse() throws ParseException {
        int verindex = line.indexOf(' ');
        if (verindex > 0) {
            qualifiedName = line.substring(0, verindex-2);
            int p = qualifiedName.lastIndexOf('.') +1;
            if (p == -1) p=0;
            int e = qualifiedName.indexOf('/');
            if (e == -1) e = qualifiedName.length();
            simpleName = qualifiedName.substring(p,e);
            //XXX TODO - wrap build date and version info
        } else {
            throw new ParseException(line, "Cannot find module info in \"" + line + "\"");
        }
        
    }
    
    public String toString() {
        checkParsed();
        return simpleName;
    }
    
    public int hashCode() {
        return line.hashCode() ^ 37;
    }
    
    public boolean equals(Object o) {
        return (o instanceof ModuleEntry) && (((ModuleEntry)o).line.equals(line));
    }
}


