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

package org.netbeans.modules.db.sql.execute.ui.util;



import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.*;
import org.netbeans.junit.Manager;

/**
 *
 * @author luke
 */


public class DbUtil {
    
    public static  String DRIVER_CLASS_NAME="driver_class_name";
    public static String URL="url";
    public static String USER="user";
    public static String PASSWORD="password";
    
    public static Connection createConnection(Properties p,File[] f) throws Exception{
        String driver_name=p.getProperty(DRIVER_CLASS_NAME);
        String url=p.getProperty(URL);
        String user=p.getProperty(USER);
        String passwd=p.getProperty(PASSWORD);
        ArrayList list=new java.util.ArrayList();
        for(int i=0;i<f.length;i++){
            list.add(f[i].toURI().toURL());
        }
        URL[] driverURLs=(URL[])list.toArray(new URL[0]);
        URLClassLoader l = new URLClassLoader(driverURLs);
        Class c = Class.forName(driver_name, true, l);
        Driver driver=(Driver)c.newInstance();
        Connection con=driver.connect(url,p);
        return con;
    }
    
    
    
    
}

