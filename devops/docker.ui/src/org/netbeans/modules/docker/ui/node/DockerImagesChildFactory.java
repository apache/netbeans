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
package org.netbeans.modules.docker.ui.node;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.docker.api.DockerImage;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerTag;
import org.netbeans.modules.docker.api.DockerEvent;
import org.netbeans.modules.docker.api.DockerAction;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class DockerImagesChildFactory extends NodeClosingFactory<DockerTag> implements Refreshable, Closeable {

    private static final Logger LOGGER = Logger.getLogger(DockerImagesChildFactory.class.getName());

    private static final Comparator<DockerTag> COMPARATOR = new Comparator<DockerTag>() {

        @Override
        public int compare(DockerTag o1, DockerTag o2) {
            return o1.getTag().compareTo(o2.getTag());
        }
    };

    private final RequestProcessor requestProcessor = new RequestProcessor(DockerImagesChildFactory.class);

    private final DockerInstance instance;

    private final RequestProcessor.Task refreshTask;

    private final DockerEvent.Listener listener;

    public DockerImagesChildFactory(DockerInstance instance) {
        this.instance = instance;
        this.refreshTask = requestProcessor.create(new Runnable() {
            @Override
            public void run() {
                LOGGER.log(Level.FINE, "Refreshing images");
                refresh();
            }
        });
        this.listener = new DockerEvent.Listener() {
            @Override
            public void onEvent(DockerEvent event) {
                if (DockerEvent.Status.PUSH != event.getStatus()) {
                    refreshTask.schedule(200);
                }
            }
        };
        instance.addImageListener(listener);
    }

    @Override
    protected Node createNodeForKey(DockerTag key) {
        return new DockerTagNode(key);
    }

    @Override
    protected boolean createKeys(List<DockerTag> toPopulate) {
        DockerAction facade = new DockerAction(instance);
        List<DockerTag> tags = new ArrayList<>();
        for (DockerImage image : facade.getImages()) {
            tags.addAll(image.getTags());
        }
        tags.sort(COMPARATOR);
        toPopulate.addAll(tags);
        return true;
    }

    @Override
    public final void refresh() {
        refresh(false);
    }

    @Override
    public void close() {
        instance.removeImageListener(listener);
    }

}
