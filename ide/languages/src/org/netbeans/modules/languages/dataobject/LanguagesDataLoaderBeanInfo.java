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

package org.netbeans.modules.languages.dataobject;

import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyEditorSupport;
import org.openide.loaders.MultiFileLoader;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;


public class LanguagesDataLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo() {
        try {
            return new BeanInfo[] {Introspector.getBeanInfo(MultiFileLoader.class)};
        } catch (IntrospectionException e) {
            throw new AssertionError(e);
        }
    }

    /** @param type Desired type of the icon
    * @return returns the Image loader's icon
    */
    public Image getIcon (final int type) {
        return ImageUtilities.loadImage ("org/netbeans/modules/languages/resources/defaultIcon.png"); // NOI18N
    }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        System.out.println("LanguagesDataLoaderBeanInfo.getPropertyDescriptors");
        try {
            PropertyDescriptor[] pds = new PropertyDescriptor[] {
                new PropertyDescriptor ("NBSFiles", LanguagesDataLoader.class, "getNBSFiles", null),
            };
            pds [0].setDisplayName ("GLF Files");
            pds [0].setBound (true);
            pds [0].setPropertyEditorClass (ActionsEditor.class);
            return pds;
        } catch (IntrospectionException ie) {
            ie.printStackTrace();
            return new PropertyDescriptor[0];
        }
    }

    public static class ActionsEditor extends PropertyEditorSupport {

        public boolean supportsCustomEditor () {
            return true;
        }

        public Component getCustomEditor () {
            return new GLFFilesCustomEditor ();
        }
        
        public String getAsText () {
            return "NBS files.";
        }
        
        public void setAsText (String text) throws IllegalArgumentException {
            throw new IllegalArgumentException ();
        }
    }
}
