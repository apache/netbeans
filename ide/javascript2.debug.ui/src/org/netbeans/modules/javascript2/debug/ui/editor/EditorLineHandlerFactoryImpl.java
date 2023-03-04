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
package org.netbeans.modules.javascript2.debug.ui.editor;

import java.net.URL;
import org.netbeans.modules.javascript2.debug.EditorLineHandler;
import org.netbeans.modules.javascript2.debug.EditorLineHandlerFactory;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Martin
 */
@ServiceProvider(service = EditorLineHandlerFactory.class)
public class EditorLineHandlerFactoryImpl extends EditorLineHandlerFactory {

    @Override
    public EditorLineHandler get(FileObject fo, int lineNumber) {
        try {
            DataObject dobj = DataObject.find(fo);
            LineCookie lineCookie = dobj.getLookup().lookup(LineCookie.class);
            if (lineCookie == null) {
                return null;
            }
            try {
                Line line = lineCookie.getLineSet().getCurrent(lineNumber - 1);
                return new LineDelegate(line);
            } catch (IndexOutOfBoundsException ioobex) {
                // The line is gone.
                return null;
            }
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
    }

    @Override
    public EditorLineHandler get(URL url, int line) {
        return new LineDelegate(new FutureLine(url, line - 1));
    }

}
