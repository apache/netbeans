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
    private static final ConvertorResolver DEFAULT = new ConvertorResolver();

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
            return (!(attrib instanceof String))? null: (String) attrib;
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
        return (!(attrb instanceof Convertor))? null: (Convertor) attrb;
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
