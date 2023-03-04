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
 * Util.java
 *
 * Created on February 12, 2004, 10:52 AM
 */
package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import java.awt.Toolkit;
import java.util.ResourceBundle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.glassfish.spi.Utils;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.util.Lookup;


/**
 *
 * @author  nityad
 */
public class Util {

    /** Creates a new instance of Util */
    public Util() {
    }

    ///Numeric Document
    public static NumericDocument getNumericDocument(){
        return new NumericDocument();
    }
    public static class NumericDocument extends PlainDocument {
        private Toolkit toolkit = Toolkit.getDefaultToolkit();
        
        @Override
        public void insertString(int offs, String str, AttributeSet a)
        throws BadLocationException {
            char[] s = str.toCharArray();
            char[] r = new char[s.length];
            int j = 0;
            for (int i = 0; i < r.length; i++) {
                if (Character.isDigit(s[i])) {
                    r[j++] = s[i];
                } else {
                    toolkit.beep();
                }
            }
            super.insertString(offs, new String(r, 0, j), a);
        }
    } // class NumericDocument
    
    public static String getCorrectedLabel(ResourceBundle bundle, String key){
        String val = bundle.getString("LBL_" + key); //NOI18N
        int i = val.indexOf("&"); // NOI18N
        String result;
        // some locales do not have mnemonics?
        if (i > -1) {
            result = val.substring(0, i);
            result = result.concat(val.substring(i+1));
        } else {
            result = val;
        }
        return result;
    }

    static String getBaseName(Project project) {
            String baseName;
            Lookup lookup = project.getLookup();
            J2eeModuleProvider provider = (J2eeModuleProvider) lookup.lookup(J2eeModuleProvider.class);
            String id = provider.getServerInstanceID();
            if (Utils.useGlassfishPrefix(id)) // NOI18N
                baseName = "glassfish-resources"; // NOI18N
            else
                baseName = "sun-resources"; // NOI18N
            return baseName;

    }
        }
