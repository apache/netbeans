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

//package org.netbeans.modules.jakarta.web.beans;
//
//import java.io.IOException;
//import org.openide.filesystems.FileObject;
//import org.openide.loaders.DataLoader;
//import org.openide.loaders.DataObject;
//import org.openide.util.NbBundle;
//
///**
// */
//public class BeansDataLoader extends DataLoader {
//
//    public static final String REQUIRED_MIME = "text/x-beans-jakarta+xml";
//
//    public BeansDataLoader() {
//        super(BeansDataObject.class.getName());
//    }
//
//    @Override
//    protected void initialize() {
//        super.initialize();
//    }
//
//    @Override
//    protected String defaultDisplayName() {
//        return NbBundle.getMessage(BeansDataLoader.class, "LBL_loaderName"); // NOI18N
//    }
//
//    @Override
//    protected String actionsContext() {
//        return "Loaders/" + REQUIRED_MIME + "/Actions";
//    }
//
//    @Override
//    protected DataObject handleFindDataObject(FileObject fo, RecognizedFiles recognized) throws IOException {
//        return new BeansDataObject(fo, this);
//    }
//}
