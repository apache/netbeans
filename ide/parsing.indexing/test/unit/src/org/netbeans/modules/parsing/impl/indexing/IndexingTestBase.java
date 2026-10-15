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
package org.netbeans.modules.parsing.impl.indexing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.spi.indexing.support.EmbeddedPathRecognizer;
import org.netbeans.spi.editor.document.DocumentFactory;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sdedic
 */
public class IndexingTestBase extends NbTestCase {

    public IndexingTestBase(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        List<Class> services = new ArrayList<Class>();
        getAdditionalServices(services);
        List<Class> classes = new ArrayList<Class>(4);
        classes.addAll(services);
        Class[] services2 = getMockServices();
        if (services2 != null) {
            classes.addAll(Arrays.asList(services2));
        }
        MockServices.setServices(classes.toArray(new Class[0]));
            MockMimeLookup.setInstances(
            MimePath.EMPTY,
            new DocumentFactory() {
                @Override
                public Document createDocument(String mimeType) {
                    return new BaseDocument(false, mimeType);
                }

                @Override
                public Document getDocument(FileObject file) {
                    try {
                        final DataObject dobj = DataObject.find(file);
                        final EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
                        return ec == null ?
                            null :
                            ec.openDocument();
                    } catch (DataObjectNotFoundException e) {
                        return null;
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                        return null;
                    }
                }

                @Override
                public FileObject getFileObject(Document document) {
                    Object sdp = document.getProperty(Document.StreamDescriptionProperty);
                    if (sdp instanceof FileObject) {
                        return (FileObject)sdp;
                    }
                    if (sdp instanceof DataObject) {
                        return ((DataObject)sdp).getPrimaryFile();
                    }
                    return null;
            }
        });
    }
    
    protected Class[] getMockServices() {
        return new Class[] { MockMimeLookup.class, TestEnvironmentFactory.class, TestEditorMimeTypesImpl.class, FooPathRecognizer.class, EmbeddedPathRecognizer.class };
    }
    
    protected void getAdditionalServices(List<Class> clazz) {
    }
    
    protected Lookup createTestServices() {
        return Lookups.fixed(new TestEnvironmentFactory(), new TestEditorMimeTypesImpl());
    }

}
