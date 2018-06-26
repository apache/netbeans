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
            result = result.concat(val.substring(i+1, val.length()));
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
