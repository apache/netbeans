/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.beans.PropertyEditor;

import org.netbeans.modules.j2ee.sun.api.restricted.ResourceUtils;
import org.openide.util.Utilities;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.BeanNode;
import org.openide.nodes.PropertySupport;

import org.openide.filesystems.FileObject;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePairsPropertyEditor;
import org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader.SunResourceDataObject;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;

/** A node to represent this object.
 *
 * @author nityad
 */
public class DataSourceBeanDataNode extends BaseResourceNode implements java.beans.PropertyChangeListener{
    private DataSourceBean resource = null;
    
    public DataSourceBeanDataNode(SunResourceDataObject obj, DataSourceBean key) {
        super(obj);
        resource = key;
        setIconBaseWithExtension("org/netbeans/modules/j2ee/sun/share/resources/ResNodeNodeIcon.gif"); //NOI18N
        setShortDescription (NbBundle.getMessage (DataSourceBeanDataNode.class, "DSC_DataSourceNode"));//NOI18N
        
        key.addPropertyChangeListener(this);
        
        Class<?> clazz = key.getClass ();
        try{
            createProperties(key, Utilities.getBeanInfo(clazz));
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    protected DataSourceBeanDataNode getDataSourceBeanDataNode(){
        return this;
    }
    
    protected DataSourceBean getDataSourceBean(){
        return resource;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        FileObject resFile = getDataSourceBeanDataNode().getDataObject().getPrimaryFile();
        ResourceUtils.saveNodeToXml(resFile, resource.getGraph());
    }
    
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx ("AS_Res_DataSource");//NOI18N
    }
    
    public Resources getBeanGraph(){
        return resource.getGraph();
    }
    
    protected void createProperties(Object bean, java.beans.BeanInfo info) {
        BeanNode.Descriptor d = BeanNode.computeProperties(bean, info);
        Node.Property p = new PropertySupport.ReadWrite(
                "extraParams", DataSourceBeanDataNode.class, //NOI18N
                NbBundle.getMessage(DataSourceBeanDataNode.class,"LBL_ExtParams"), //NOI18N
                NbBundle.getMessage(DataSourceBeanDataNode.class,"DSC_ExtParams") //NOI18N
                ) {
            public Object getValue() {
                return resource.getExtraParams();
            }
            
            public void setValue(Object val){
                if (val instanceof Object[])
                    resource.setExtraParams((Object[])val);
            }
            
            public PropertyEditor getPropertyEditor(){
                return new NameValuePairsPropertyEditor(resource.getExtraParams());
            }
        };
        
        Sheet sets = getSheet();
        Sheet.Set pset = Sheet.createPropertiesSet();
        pset.put(d.property);
        pset.put(p);
        //pset.setValue("propertiesHelpID", "AS_Res_DataSource_Props"); //NOI18N
        sets.put(pset);
    }
    
}
