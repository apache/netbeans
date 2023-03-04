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
   
    public static final String DB_SQLCREATE="dbcreate.sql";
    public static final String DB_SQLINSERT="dbinsert.sql";
    public static final String DB_SQLSELECT="dbselect.sql";
    public static final String DB_SQLUPDATE="dbupdate.sql";
    public static final String DB_DATA= "dbdata.properties";
    public static final String DB_PROP= "dbprop.properties";
    public static final String DB_SQLDEL="dbdel.sql";
    public static final String DB_JARS="jar";
    public static final String[] FILES={DB_SQLCREATE,DB_SQLINSERT,DB_SQLUPDATE,DB_PROP,DB_SQLDEL,DB_SQLSELECT,DB_DATA};
    private final List<TestCaseContext> list=new ArrayList<>();
    private static  TestCaseDataFactory factory;
    
    public static TestCaseDataFactory getTestCaseFactory() throws Exception {
        if (factory == null) {
            factory = new TestCaseDataFactory();
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
        HashMap<String, Object> map = new HashMap<>();
        File etcDir = new File(data_dir, "etc");
        for (int index = 0; index < FILES.length; index++) {
            File f = new File(etcDir, FILES[index]);
            if (!f.exists()) {
                throw new RuntimeException("File called " + FILES[index] + " in directory " + etcDir + " doesn't exist");
            }
            map.put(FILES[index], f);
        }
        File[] drivers = new File(data_dir, "../../../../external").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar") || name.endsWith(".zip");
            }
        });
        if (drivers == null || drivers.length == 0) {
            throw new RuntimeException("the driver doesn't exist in folder: " + etcDir);
        }
        map.put(DB_JARS, drivers);

        TestCaseContext context = new TestCaseContext(map, etcDir.getName());
        list.add(context);
    }
    
    public Object[] getTestCaseContext(){
           return list.toArray();
    }
    
}
