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

package org.netbeans.modules.db.dataview.util;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.netbeans.junit.Manager;
import org.openide.util.Utilities;

/**
 *
 * @author jawed
 */
public class TestCaseDataFactory {
   
    public static  String DB_SQLCREATE="dbcreate.sql";
    public static  String DB_SQLINSERT="dbinsert.sql";
    public static String DB_SQLSELECT="dbselect.sql";
    public static String DB_SQLUPDATE="dbupdate.sql";
    public static  String DB_TEXT= "dbdata.txt";
    public static  String DB_PROP= "dbprop.properties";
    public static String DB_SQLDEL="dbdel.sql";
    public static String DB_JARS="jar";
    public static String[] FILES={DB_SQLCREATE,DB_SQLINSERT,DB_SQLUPDATE,DB_PROP,DB_SQLDEL,DB_SQLSELECT,DB_TEXT};
    private List<TestCaseContext> list=new ArrayList<TestCaseContext>();
    private static  TestCaseDataFactory factory;
    
    public static TestCaseDataFactory  getTestCaseFactory() throws Exception{
        
        if(factory==null){
          
          factory=new TestCaseDataFactory();
          factory.process();

        }  
        return factory;
    }
    
    private TestCaseDataFactory() throws Exception {
    }
    
    private File getDataDir() throws URISyntaxException {
       
        
        String className = getClass().getName();
        URL url = this.getClass().getResource(className.substring(className.lastIndexOf('.')+1)+".class"); // NOI18N
        File dataDir = Utilities.toFile(url.toURI()).getParentFile();
        int index = 0;
        while((index = className.indexOf('.', index)+1) > 0) {
                dataDir = dataDir.getParentFile();
        }
        dataDir = new File(dataDir.getParentFile(), "data"); //NOI18N
        return Manager.normalizeFile(dataDir);
        
    }
    
    private void process() throws Exception{
        File data_dir = getDataDir();
        HashMap<String, Object> map = new HashMap<String, Object>();
        File etcDir = new File(data_dir, "etc");
        for (int index = 0; index < FILES.length; index++) {
            File f = new File(etcDir, FILES[index]);
            if (!f.exists()) {
                throw new RuntimeException("File called " + FILES[index] + " in directory " + etcDir + " doesn't exist");
            }
            map.put(FILES[index], f);
        }
        String[] s = etcDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar") || name.endsWith(".zip") ? true : false;
            }
        });
        if (s.length == 0) {
            throw new RuntimeException("the driver doesn't exist in folder: " + etcDir);
        }
        ArrayList<File> drivers = new ArrayList<File>();
        for (int myint = 0; myint < s.length; myint++) {
            File file = new File(etcDir, s[myint]);
            drivers.add(file);

        }
        map.put(DB_JARS, drivers.toArray(new File[0]));

        TestCaseContext context = new TestCaseContext(map, etcDir.getName());
        list.add(context);
    }
    
    public Object[] getTestCaseContext(){
           return list.toArray();
    }
    
}
