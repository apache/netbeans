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
/*
 * BookDataLoader.java
 *
 * Created on March 9, 2005, 4:11 PM
 */

package org.netbeans.modules.xml.multiview.test;

import org.openide.filesystems.*;
import org.openide.loaders.*;
/**
 *
 * @author mkuchtiak
 */
public class BookDataLoader extends UniFileLoader {
    
    public BookDataLoader() {
        super(BookDataObject.class.getName());
    }
    protected void initialize() {
        super.initialize();
        getExtensions().addExtension("book");
    }
    protected String displayName() {
        return "Book";
    }
    protected MultiDataObject createMultiObject(FileObject pf) throws java.io.IOException {
        return new BookDataObject(pf, this);
    }
    
}
