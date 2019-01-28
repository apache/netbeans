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
package org.netbeans.modules.java.classfile;

import java.net.URL;
import javax.swing.JComponent;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.SideBarFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class SideBarFactoryImpl implements SideBarFactory {

    @Override
    public JComponent createSideBar(JTextComponent target) {
        Document doc = target.getDocument();
        FileObject originFile;
        Object origin = doc.getProperty(Document.StreamDescriptionProperty);

        if (origin instanceof DataObject) {
            originFile = ((DataObject) origin).getPrimaryFile();
        } else if (origin instanceof FileObject) {
            originFile = (FileObject) origin;
        } else {
            originFile = null;
        }

        Object classFileRoot;
        Object binaryName;
        if (originFile != null) {
            classFileRoot = originFile.getAttribute(CodeGenerator.CLASSFILE_ROOT);
            binaryName = originFile.getAttribute(CodeGenerator.CLASSFILE_BINNAME);
        } else {
            classFileRoot = binaryName = null;
        }

        if (classFileRoot instanceof URL && binaryName instanceof String) {
            return new AttachSourcePanel(
                (URL) classFileRoot,
                originFile.toURL(),
                (String) binaryName);
        }
        return null;
    }

}
