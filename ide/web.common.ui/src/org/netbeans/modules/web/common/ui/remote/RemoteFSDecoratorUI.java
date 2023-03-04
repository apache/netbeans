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
package org.netbeans.modules.web.common.ui.remote;

import java.awt.Image;
import java.util.Set;
import org.netbeans.modules.web.common.spi.RemoteFSDecorator;
import org.openide.filesystems.*;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Martin
 */
@ServiceProvider(service = RemoteFSDecorator.class)
public class RemoteFSDecoratorUI implements RemoteFSDecorator, ImageDecorator {
    
    private StatusDecorator defaultDecorator;

    @Override
    public void setDefaultDecorator(StatusDecorator decorator) {
        this.defaultDecorator = decorator;
    }

    @Override
    public String annotateName(String name, Set<? extends FileObject> files) {
        return defaultDecorator.annotateName(name, files);
    }

    @Override
    public String annotateNameHtml(String name, Set<? extends FileObject> files) {
        return defaultDecorator.annotateNameHtml(name, files);
    }

    @Override
    public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
        int n = files.size();
        if (n == 1) {
            FileObject fo = files.iterator().next();
            if (fo.isRoot()) {
                return ImageUtilities.loadImage(
                        "org/netbeans/modules/web/clientproject/ui/resources/remotefiles.png"); //NOI18N
            }
        }
        return icon;
    }
    
}
