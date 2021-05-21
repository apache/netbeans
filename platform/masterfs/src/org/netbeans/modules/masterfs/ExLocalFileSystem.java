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


package org.netbeans.modules.masterfs;

import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.AbstractFileSystem;
import org.openide.filesystems.DefaultAttributes;
import org.netbeans.modules.masterfs.providers.Attributes;

import java.io.IOException;
import java.io.File;
import java.util.Enumeration;
import java.beans.PropertyVetoException;
import org.netbeans.modules.masterfs.filebasedfs.utils.Utils;

public class ExLocalFileSystem extends LocalFileSystem {
    public static ExLocalFileSystem getInstance (File root) throws PropertyVetoException, IOException {
        ExLocalFileSystem retVal = new ExLocalFileSystem ();
        if (Utils.equals(root, Attributes.getRootForAttributes())) {
            retVal.attr = new OneFileAttributeAttachedToRoot(retVal.info, retVal.change, retVal.list);
        } else {
            retVal.attr = new Attributes(root, retVal.info, retVal.change, retVal.list);
        }
        retVal.setRootDirectory(root);
        
        return retVal;
    }
    
    public DefaultAttributes getAttributes () {
        return (DefaultAttributes)attr;
    }
    
    private static class OneFileAttributeAttachedToRoot extends DefaultAttributes {        

        public OneFileAttributeAttachedToRoot(
                AbstractFileSystem.Info info,
                AbstractFileSystem.Change change,
                AbstractFileSystem.List list
                ) {
            
            super(info, change, list, Attributes.ATTRNAME); //NOI18N
        }
        
        
        public String[] children(String f) {
            return super.children(f);
        }

        /* Get the file attribute with the specified name.
        * @param name the file
        * @param attrName name of the attribute
        * @return appropriate (serializable) value or <CODE>null</CODE> if the attribute is unset (or could not be properly restored for some reason)
        */
        public Object readAttribute(String name, String attrName) {
            return super.readAttribute(transformName (name), attrName);
        }

        /* Set the file attribute with the specified name.
        * @param name the file
        * @param attrName name of the attribute
        * @param value new value or <code>null</code> to clear the attribute. Must be serializable, although particular filesystems may or may not use serialization to store attribute values.
        * @exception IOException if the attribute cannot be set. If serialization is used to store it, this may in fact be a subclass such as {@link NotSerializableException}.
        */
        public void writeAttribute(String name, String attrName, Object value)
                throws IOException {
            super.writeAttribute(transformName (name), attrName, value);
        }

        /* Get all file attribute names for the file.
        * @param name the file
        * @return enumeration of keys (as strings)
        */
        public synchronized Enumeration<String> attributes(String name) {
            return super.attributes(transformName (name));
        }

        /* Called when a file is renamed, to appropriatelly update its attributes.
        * <p>
        * @param oldName old name of the file
        * @param newName new name of the file
        */
        public synchronized void renameAttributes(String oldName, String newName) {
            super.renameAttributes(transformName (oldName), transformName (newName));
        }

        /* Called when a file is deleted to also delete its attributes.
        *
        * @param name name of the file
        */
        public synchronized void deleteAttributes(String name) {
            super.deleteAttributes(transformName (name));
        }
        
        private String transformName (String name) {
            char replaceChar = '|';//NOI18N       
            if (name.indexOf(replaceChar) != -1 ) {
                StringBuffer transformed = new StringBuffer(name.length() + 50);
                for (int i = 0; i < name.length(); i++) {
                    transformed.append(name.charAt(i));                        
                    if (name.charAt(i) == replaceChar) 
                        transformed.append(replaceChar);                                                                    
                }
                name = transformed.toString();
            }
            return name.replace('/',replaceChar);//NOI18N
        }        
    }    
}
