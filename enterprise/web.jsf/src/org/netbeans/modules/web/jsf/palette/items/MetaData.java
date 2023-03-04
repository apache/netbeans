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

package org.netbeans.modules.web.jsf.palette.items;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.jsfapi.spi.InputTextTagValueProvider;
import org.netbeans.modules.web.jsf.api.palette.PaletteItem;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexey Butenko
 * @author mfukala@netbeans.org
 */
public class MetaData implements ActiveEditorDrop, PaletteItem {

    private HashMap<String, String> properties = new HashMap<String, String>();

    private JsfLibrariesSupport jsfLibrariesSupport;

    @Override
    public boolean handleTransfer(JTextComponent targetComponent) {
        properties.clear();
        jsfLibrariesSupport = PaletteUtils.getJsfLibrariesSupport(targetComponent);
        if (jsfLibrariesSupport == null) {
            return false;
        }
        findProperties(targetComponent);

        MetaDataCustomizer customizer = new MetaDataCustomizer(this, targetComponent);
        boolean accept = customizer.showDialog();
        if (accept) {
            try {
                String body = createBody(targetComponent);
                JSFPaletteUtilities.insert(body, targetComponent);
                jsfLibrariesSupport.importLibraries(DefaultLibraryInfo.JSF_CORE);
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
                accept = false;
            }

        }
        return accept;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(MetaData.class, "NAME_jsp-JsfMetadata"); //NOI18N
    }

    @Override
    public void insert(JTextComponent component) {
        handleTransfer(component);
    }

    private String createBody(JTextComponent targetComponent) {
        final StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append('<').append(jsfLibrariesSupport.getLibraryPrefix(DefaultLibraryInfo.JSF_CORE)).append(":metadata>\n");    //NOI18N
        Set<Entry<String,String>> set = properties.entrySet();
        for (Entry<String, String> entry : set) {
            stringBuffer.append("   <").append(jsfLibrariesSupport.getLibraryPrefix(DefaultLibraryInfo.JSF_CORE)).append(":viewParam name=\"").append(entry.getKey()).append("\" value=\"").append(entry.getValue()).append("\"/>\n");    //NOI18N
        }
        stringBuffer.append("</").append(jsfLibrariesSupport.getLibraryPrefix(DefaultLibraryInfo.JSF_CORE)).append(":metadata>\n");    //NOI18N
        return stringBuffer.toString();
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }
    public String addProperty(String name, String value) {
        return properties.put(name, value);
    }

    public String removeProperty(String key) {
        return properties.remove(key);
    }
    
    private void findProperties(JTextComponent target) {
        BaseDocument doc = (BaseDocument) target.getDocument();
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        if (dobj != null) {
            FileObject fobj = (FileObject) NbEditorUtilities.getDataObject(doc).getPrimaryFile();
            //finds an instance of the values provider and asks him for the result map
            properties.putAll(InputTextTagValueProvider.Query.getInputTextValuesMap(fobj));

        }

    }
   
}
