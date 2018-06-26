/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
