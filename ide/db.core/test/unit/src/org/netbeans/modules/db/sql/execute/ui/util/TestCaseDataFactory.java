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

package org.netbeans.modules.db.sql.execute.ui.util;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.netbeans.junit.Manager;

/**
 *
 * @author luke
 */
public class TestCaseDataFactory {
   
    public static  String DB_SQLCREATE="dbcreate.sql";
    public static String DB_SQLSELECT="dbselect.sql";
    public static  String DB_DATA= "dbdata.properties";
    public static  String DB_PROP= "dbprop.properties";
    public static String DB_SQLDEL="dbdel.sql";
    public static String DB_JARS="jar";
    public static String[] FILES={DB_SQLCREATE,DB_PROP,DB_SQLDEL,DB_SQLSELECT,DB_DATA};
    private List list=new ArrayList();
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
    
    private File getDataDir() {
       
        
        String className = getClass().getName();
        URL url = this.getClass().getResource(className.substring(className.lastIndexOf('.')+1)+".class"); // NOI18N
        File dataDir = new File(url.getFile()).getParentFile();
        int index = 0;
        while((index = className.indexOf('.', index)+1) > 0) {
                dataDir = dataDir.getParentFile();
        }
        dataDir = new File(dataDir.getParentFile(), "data"); //NOI18N
        return Manager.normalizeFile(dataDir);
        
    }
    
    private void process() throws Exception{
       File data_dir=getDataDir();
       HashMap map=new HashMap();
       String[] dir=data_dir.list();
       for(int i=0;i<dir.length;i++){
           String dir_name=dir[i];
           String path=data_dir.getAbsolutePath()+File.separator+dir[i];
           if(new File(path).isDirectory()){
                
                for(int index=0;index<FILES.length;index++){
                    File f=new File(path+File.separator+FILES[index]);
                    if(!f.exists())
                        throw new RuntimeException("File called "+FILES[index] +"in directory "+dir_name+"doesn't exist");
                    map.put(FILES[index],f);
                    
                }
                String[] s=new File(path).list(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                         return  name.endsWith(".jar") || name.endsWith(".zip") ? true : false;
                    }
                });
                    
                for(int iii=0;iii<s.length;iii++){
                    System.out.println(s[iii]);
                }
            //    if(s.length>1)
             //       throw new RuntimeException("one jar or zip file must existed in directory "+dir_name);
                if(s.length==0)
                    throw new RuntimeException("the driver doesn't  extist for test case called: "+dir_name);
                ArrayList drivers=new ArrayList();
                for(int myint=0;myint<s.length;myint++){
                   File file=new File(path+File.separator+s[myint]);
                   drivers.add(file);
                   
                }
                map.put(DB_JARS,drivers.toArray(new File[0]));
                  
                TestCaseContext context=new TestCaseContext(map,dir_name);
                list.add(context);
                
           }
       }
    }
    
    public Object[] getTestCaseContext(){
           return list.toArray();
    }
    
}
