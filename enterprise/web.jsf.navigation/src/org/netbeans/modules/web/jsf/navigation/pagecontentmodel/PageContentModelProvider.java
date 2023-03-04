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

package org.netbeans.modules.web.jsf.navigation.pagecontentmodel;

import org.openide.filesystems.FileObject;

/**
 *
 * @author joelle lam
 */
public interface PageContentModelProvider {
    /**
     * Returns the Page Content Model
     * @param fileObject 
     * @return PageContentModel for a given fileobject, null if none exists.
     */
    public PageContentModel getPageContentModel(FileObject fileObject);
    /**
     * This method is tricky and sort of a hack.
     * Given a new page or modification, does there now exist a model
     * unlike before.  This method was primarily completed for VWP 
     * functionality which sometimes needs to know about a new JAVA 
     * file being updated.
     * @param fileObject of the new page 
     * @return FileObject of the model that should be updated 
     **/
    public FileObject isNewPageContentModel(FileObject fileObject);
}
