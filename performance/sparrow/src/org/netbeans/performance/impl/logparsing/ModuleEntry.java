/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
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


