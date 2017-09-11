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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.openide.loaders;

import java.io.IOException;
import org.openide.filesystems.FileObject;


/**
 * Save document under a different file name and/or extension.
 * 
 * The default implementation is available in {@link org.openide.text.DataEditorSupport}. So if your
 * editor support inherits from <code>DataEditorSupport</code> you can implement "Save As" feature
 * for your documents by adding the following lines into your {@link DataObject}'s constructor:
 * 
<code><pre>
        getCookieSet().assign(SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs(FileObject folder, String fileName) throws IOException {
                getDataEditorSupport().saveAs( folder, fileName );
            }
        });
</pre></code>
 *
 * If you have {@link Node}, you may use the following code:
 * 
<code><pre>
    public class MyNode extends AbstractNode {
        
        public MyNode() {
            getCookieSet().assign(SaveAsCapable.class, new MySaveAsCapable());
            ...
        }

        private class MySaveAsCapable implements SaveAsCapable {
            public void saveAs(FileObject folder, String fileName) throws IOException {
                FileObject newFile = folder.getFileObject(fileName);

                if (newFile == null) {
                    newFile = FileUtil.createData(folder, fileName);
                }
                OutputStream output = newFile.getOutputStream();
                InputStream input = ... // get your input stream
                
                try {
                    byte[] buffer = new byte[4096];

                    while (input.available() > 0) {
                        output.write(buffer, 0, input.read(buffer));
                    }
                }
                finally {
                    if (input != null) {
                        input.close();
                    }
                    if (output != null) {
                        output.close();
                    }
                }
            }
        }
    }
</pre></code>
 *
 * @since 6.3
 * @author S. Aubrecht
 */
public interface SaveAsCapable {
    /** 
     * Invoke the save operation.
     * @param folder Folder to save to.
     * @param name New file name to save to.
     * @throws IOException if the object could not be saved
     */
    void saveAs( FileObject folder, String name ) throws IOException;
}
