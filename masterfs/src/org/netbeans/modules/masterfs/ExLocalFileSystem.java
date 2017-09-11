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
        public synchronized Enumeration attributes(String name) {
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
