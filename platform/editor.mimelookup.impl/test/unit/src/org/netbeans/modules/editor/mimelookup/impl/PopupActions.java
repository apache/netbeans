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


package org.netbeans.modules.editor.mimelookup.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Martin Roskanin
 */
@MimeLocation(subfolderName="Popup", instanceProviderClass=PopupActions.class)
public class PopupActions implements InstanceProvider{
    static final Object LOCK = new JPanel().getTreeLock();

    List ordered;

    public PopupActions(){
        synchronized (LOCK) {
            // just try to hold a lock
        }
    }

    public PopupActions(List ordered){
        this.ordered = ordered;
        synchronized (LOCK) {
            // just try to hold a lock
        }
    }

    public List getPopupActions(){
        List retList = new ArrayList();
        for (int i = 0; i<ordered.size(); i++){
            FileObject fo = (FileObject) ordered.get(i);
            DataObject dob;
            
            try {
                dob = DataObject.find(fo);
            } catch (DataObjectNotFoundException dnfe) {
                // ignore
                continue;
            }
            
            InstanceCookie ic = (InstanceCookie)dob.getCookie(InstanceCookie.class);
            if (ic!=null){
                try{
                    if (String.class.isAssignableFrom(ic.instanceClass()) ||
                        Action.class.isAssignableFrom(ic.instanceClass()) ||
                        SystemAction.class.isAssignableFrom(ic.instanceClass()) ||
                        JSeparator.class.isAssignableFrom(ic.instanceClass())){
                        Object instance = ic.instanceCreate();
                        retList.add(instance);
                    }
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }catch(ClassNotFoundException cnfe){
                    cnfe.printStackTrace();
                }
            } else{
                retList.add(dob.getName());
            }
        }
        return retList;
    }

    public Object createInstance(List ordered) {
        return new PopupActions(ordered);
    }
}
