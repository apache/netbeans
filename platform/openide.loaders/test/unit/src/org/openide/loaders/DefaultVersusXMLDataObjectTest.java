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

package org.openide.loaders;

import java.lang.ref.WeakReference;
import org.netbeans.junit.*;
import org.openide.filesystems.*;

/** To check issue 61600
 *
 * @author  Jaroslav Tulach
 */
public final class DefaultVersusXMLDataObjectTest extends NbTestCase {
    /** Creates a new instance of DefaultVersusXMLDataObjectTest */
    public DefaultVersusXMLDataObjectTest(String n) {
        super(n);
    }
    
    public void testCreateFromTemplateResultsInXMLDataObject() throws Exception {
        FileObject fo = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Unknown/EmptyFile");
        DataObject obj = DataObject.find(fo);
        obj.setTemplate(true);
        
        WeakReference ref = new WeakReference(obj);
        obj = null;
        assertGC("obj is gone", ref);
        
        obj = DataObject.find(fo);
        assertEquals ("Right type", DefaultDataObject.class, obj.getClass());
        assertTrue ("Is the template", obj.isTemplate());
        
        FileObject ff = FileUtil.createFolder(FileUtil.getConfigRoot(), "CreateAt");
        DataFolder f = DataFolder.findFolder(ff);
        
        DataObject result = obj.createFromTemplate(f, "my.xml");
        
        if (result instanceof DefaultDataObject) {
            fail("Bad, the object should be of XMLDataObject type: " + result);
        }
        
        assertEquals("it is xml DataObject", XMLDataObject.class, result.getClass());
    }
}
