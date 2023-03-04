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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;

/**
 *
 * @author jawed
 */
public class TestCaseContext {
    private Properties prop=new Properties();
    private String sql_create;
    private String sql_insert;
    private String sql_select;
    private String sql_update;
    private String sql_del;
    private Properties data=new Properties();
    private File[] jars;
    private String name;
    
    public TestCaseContext(HashMap<String,Object> map,String name)  throws Exception{
        this.name=name;
        setProperties((File)map.get(TestCaseDataFactory.DB_PROP));
        setJars((File[])map.get(TestCaseDataFactory.DB_JARS));
        setSqlCreate((File)map.get(TestCaseDataFactory.DB_SQLCREATE));
        setSqlInsert((File)map.get(TestCaseDataFactory.DB_SQLINSERT));
        setSqlUpdate((File)map.get(TestCaseDataFactory.DB_SQLUPDATE));
        setSqlDel((File)map.get(TestCaseDataFactory.DB_SQLDEL));
        setSqlSelect((File)map.get(TestCaseDataFactory.DB_SQLSELECT));
        setData((File)map.get(TestCaseDataFactory.DB_DATA));
    
    }
    
    
    public Properties getProperties(){
        return prop;
    }
    private void setProperties(File f) throws Exception{
      prop.load(new FileInputStream(f.getAbsolutePath()));        
    }
    
    
    public String getSqlCreate(){
        return sql_create;
    }
    
    
    private void setSqlCreate(File f) throws Exception{
        sql_create=getContent(f);
    }
    
    public String getSqlInsert() {
        return sql_insert;
    }

    private void setSqlInsert(File f) throws Exception {
        sql_insert = getContent(f);
    }
    
    public String getSqlUpdate() {
        return sql_update;
    }

    private void setSqlUpdate(File f) throws Exception {
        sql_update = getContent(f);
    }
    
    public String getSqlSelect(){
        return sql_select;
    }
    
    private void setSqlSelect(File f) throws Exception{
        sql_select=getContent(f);
    }
    
    public String getSqlDel(){
        return sql_del;
    }
    
    private void setSqlDel(File f) throws Exception{
        sql_del=getContent(f);
    }
    
    public Properties getData(){
        return data;
    }
    
    private void setData(File f) throws Exception{
        data.load(new FileInputStream(f.getAbsolutePath()));
    }
    
    public File[] getJars(){
        return jars;
    }
    
    private void setJars(File[] f){
        jars=f;
    }
    
    private  String getContent(File f) throws Exception{
        BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(f.getAbsolutePath())));
        StringBuilder sb=new StringBuilder();
        String s;
        while((s=br.readLine())!=null){
          sb.append(s);
          sb.append("\n");
        }
        if(sb.length()==0) {
            throw new RuntimeException(name+": File called "+f.getName()+" doesn't contain the data.");
        }
        return sb.toString().trim();
    }
    
    @Override
    public String toString(){
        return name;
    }
    
}
