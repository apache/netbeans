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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        setData((File)map.get(TestCaseDataFactory.DB_TEXT));
    
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
        }
        if(sb.length()==0) {
            throw new RuntimeException(name+": File called "+f.getName()+" doesn't contain the data.");
        }
        return sb.toString();
    }
    
    @Override
    public String toString(){
        return name;
    }
    
}
