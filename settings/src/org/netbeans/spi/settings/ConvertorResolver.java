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

package org.netbeans.spi.settings;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** Look up proper settings convertor registrations.
 *
 * @author  Jan Pokorsky
 */
final class ConvertorResolver {
    private static final String LOOKUP_PREFIX = "/xml/lookups"; // NOI18N
    private final static ConvertorResolver DEFAULT = new ConvertorResolver();

    /** Creates a new instance of ConvertorResolver */
    private ConvertorResolver() {
    }
    
    protected static ConvertorResolver getDefault() {
        return DEFAULT;
    }
    
    /** look up a convertor registered under xml/memory; used by the storing operation;
     * can return <code>null</code>
     */
    protected Convertor getConvertor(Class clazz) {
        try {
            FileObject fo = org.netbeans.modules.settings.Env.findProvider(clazz);
            if (fo == null) {
                fo = org.netbeans.modules.settings.Env.findProvider(Object.class);
            }
            return getConvertor(fo);
        } catch (IOException ex) {
            Logger.getLogger(ConvertorResolver.class.getName()).log(Level.WARNING, null, ex);
            return null;
        }
    }
    
    String getPublicID(Class clazz) {
        try {
            FileObject fo = org.netbeans.modules.settings.Env.findProvider(clazz);
            if (fo == null) {
                fo = org.netbeans.modules.settings.Env.findProvider(Object.class);
            }
            
            fo = org.netbeans.modules.settings.Env.findEntityRegistration(fo);
            Object attrib = fo.getAttribute(org.netbeans.modules.settings.Env.EA_PUBLICID);
            return (attrib == null || !(attrib instanceof String))? null: (String) attrib;
        } catch (IOException ex) {
            Logger.getLogger(ConvertorResolver.class.getName()).log(Level.WARNING, null, ex);
            return null;
        }
    }

    /** look up a convertor registered under xml/lookups; used by reading operation;
     * can return <code>null</code>
     */
    protected Convertor getConvertor(String publicID) {
        StringBuffer sb = new StringBuffer(200);
        sb.append(LOOKUP_PREFIX);
        sb.append(convertPublicId(publicID));
        // at least for now
        sb.append (".instance"); // NOI18N 

        FileObject fo = FileUtil.getConfigFile(sb.toString());
        return (fo == null)? null: getConvertor(fo);
    }
    
    /** extract convertor from file attributes */
    private Convertor getConvertor(FileObject fo) {
        Object attrb = fo.getAttribute(org.netbeans.modules.settings.Env.EA_CONVERTOR);
        return (attrb == null || !(attrb instanceof Convertor))? null: (Convertor) attrb;
    }
    
    /** Converts the publicID into filesystem friendly name.
     * <p>
     * It expects that PUBLIC has at maximum three "//" parts 
     * (standard // vendor // entity name // language). It is basically
     * converted to "vendor/entity_name" resource name.
     *
     * @see EntityCatalog
     */
    private static String convertPublicId (String publicID) {
        char[] arr = publicID.toCharArray ();


        int numberofslashes = 0;
        int state = 0;
        int write = 0;
        OUT: for (int i = 0; i < arr.length; i++) {
            char ch = arr[i];

            switch (state) {
            case 0:
                // initial state 
                if (ch == '+' || ch == '-' || ch == 'I' || ch == 'S' || ch == 'O') {
                    // do not write that char
                    continue;
                }
                // switch to regular state
                state = 1;
                // fallthru
            case 1:
                // regular state expecting any character
                if (ch == '/') {
                    state = 2;
                    if (++numberofslashes == 3) {
                        // last part of the ID, exit
                        break OUT;
                    }
                    arr[write++] = '/';
                    continue;
                }
                break;
            case 2:
                // previous character was /
                if (ch == '/') {
                    // ignore second / and write nothing
                    continue;
                }
                state = 1;
                break;
            }

            // write the char into the array
            if (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9') {
                arr[write++] = ch;
            } else {
                arr[write++] = '_';
            }
        }

        return new String (arr, 0, write);
    }
    
}
