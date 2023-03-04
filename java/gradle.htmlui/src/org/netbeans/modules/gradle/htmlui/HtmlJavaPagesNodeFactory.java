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

package org.netbeans.modules.gradle.htmlui;

import java.awt.Image;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.nodes.AbstractGradleNodeList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "MSG_HtmlJavaPages=Frontend UI Pages"
})
@NodeFactory.Registration(projectType = NbGradleProject.GRADLE_PROJECT_TYPE, position = 77)
public final class HtmlJavaPagesNodeFactory implements NodeFactory {

    @Override
    public NodeList<?> createNodes(Project p) {
        return new HtmlJavaPagesList(p);
    }

    private static class HtmlJavaPagesList extends AbstractGradleNodeList<FileObject> implements PropertyChangeListener {

        final Project project;

        HtmlJavaPagesList(Project project) {
            this.project = project;
        }

        @Override
        public List<FileObject> keys() {
            FileObject pages = project.getProjectDirectory().getFileObject("src/main/webapp/pages"); // NOI18N
            return pages != null ? Collections.singletonList(pages) : Collections.<FileObject>emptyList();
        }

        @Override
        public Node node(FileObject key) {
            DataFolder df = DataFolder.findFolder(key);
            FilterNode fn = new FilterNode(df.getNodeDelegate().cloneNode()) {
                @Override
                public Image getIcon(int type) {
                    return ImageUtilities.loadImage("org/netbeans/modules/gradle/htmlui/DukeHTML.png"); // NOI18N
                }

                @Override
                public Image getOpenedIcon(int type) {
                    return getIcon(type);
                }

                @Override
                public String getName() {
                    return "pages"; // NOI18N
                }

                @Override
                public String getDisplayName() {
                    return Bundle.MSG_HtmlJavaPages();
                }
            };
            return fn;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                fireChange();
            }
        }

        @Override
        public void removeChangeListener(ChangeListener list) {
            NbGradleProject.removePropertyChangeListener(project, this);
        }

        @Override
        public void addChangeListener(ChangeListener list) {
            NbGradleProject.addPropertyChangeListener(project, this);
        }

    }
}
