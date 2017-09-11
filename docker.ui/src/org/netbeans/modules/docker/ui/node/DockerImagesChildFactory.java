/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.node;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
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
        Collections.sort(tags, COMPARATOR);
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
