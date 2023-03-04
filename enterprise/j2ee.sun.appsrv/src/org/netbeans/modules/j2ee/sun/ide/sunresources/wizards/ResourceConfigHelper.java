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
 * ResourceConfigHelper.java
 *
 * Created on October 17, 2002, 12:11 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;


/**
 *
 * @author  shirleyc
 */
public class ResourceConfigHelper {

    private ResourceConfigData datas[] = null;
    private int index;
    private boolean forEdit = false;

    /** Creates a new instance of ResourceConfigHelper */
    public ResourceConfigHelper(int size) {
        this(size, 0);
    }

    public ResourceConfigHelper(int size, int index) {
        datas = new ResourceConfigData[size];
        this.index = index;
    }
    
    public ResourceConfigHelper(ResourceConfigData data, int size, int index) {
        this(size, index);
        datas[index] = data;
    }
    
    public ResourceConfigHelper(ResourceConfigData data) {
        this(data, 1, 0);
    }
    
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    public boolean getForEdit() {
        return forEdit;
    }
    
    public ResourceConfigHelper setForEdit(boolean forEdit) {
        this.forEdit = forEdit;
        return this;
    }    
        
    public ResourceConfigData getData() {
        ResourceConfigData data = datas[index];
        if (data == null) {
            data = new ResourceConfigData();
            datas[index] = data;
        }
        return data;
    }
    
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("index is " + index + "\n");  //NOI18N
        for (int i = 0; i < datas.length; i++) {
            if (datas[i] == null)
                str.append("datas[ " + i + " ] is null"); //NOI18N
            else
                str.append("datas[ " + i + " ] is:\n" + datas[i].toString()); //NOI18N
        }
        return str.toString();
    }
           
}
