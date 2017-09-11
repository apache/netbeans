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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.docker.api.DockerContainer;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerEvent;
import org.netbeans.modules.docker.api.DockerAction;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class DockerContainersChildFactory extends NodeClosingFactory<StatefulDockerContainer> implements Refreshable, Closeable {

    private static final Logger LOGGER = Logger.getLogger(DockerContainersChildFactory.class.getName());

    private static final Comparator<DockerContainer> COMPARATOR = new Comparator<DockerContainer>() {

        @Override
        public int compare(DockerContainer o1, DockerContainer o2) {
            return o1.getImage().compareTo(o2.getImage());
        }
    };

    private static final Set<DockerEvent.Status> CHANGE_EVENTS = new HashSet<>();

    static {
        // rename is here because it may reorder nodes
        Collections.addAll(CHANGE_EVENTS, DockerEvent.Status.COPY, DockerEvent.Status.CREATE,
                DockerEvent.Status.DESTROY, DockerEvent.Status.RENAME);
    }

    private final Map<DockerContainer, WeakReference<StatefulDockerContainer>> cache = new WeakHashMap<>();

    private final RequestProcessor requestProcessor = new RequestProcessor(DockerContainersChildFactory.class);

    private final DockerInstance instance;

    private final RequestProcessor.Task refreshTask;

    private final DockerEvent.Listener listener;

    public DockerContainersChildFactory(DockerInstance instance) {
        this.instance = instance;
        this.refreshTask = requestProcessor.create(new Runnable() {
            @Override
            public void run() {
                LOGGER.log(Level.FINE, "Refreshing containers");
                refresh();
            }
        });
        this.listener = new DockerEvent.Listener() {
            @Override
            public void onEvent(DockerEvent event) {
                if (CHANGE_EVENTS.contains(event.getStatus())) {
                    refreshTask.schedule(200);
                }
            }
        };
        instance.addContainerListener(listener);
    }

    @Override
    protected Node createNodeForKey(StatefulDockerContainer key) {
        return new DockerContainerNode(key);
    }

    @Override
    protected boolean createKeys(List<StatefulDockerContainer> toPopulate) {
        DockerAction facade = new DockerAction(instance);
        List<DockerContainer> containers = new ArrayList<>(facade.getContainers());
        Collections.sort(containers, COMPARATOR);
        synchronized (cache) {
            List<StatefulDockerContainer> fresh = new ArrayList<>(containers.size());
            for (DockerContainer c : containers) {
                StatefulDockerContainer cached = null;
                WeakReference<StatefulDockerContainer> ref = cache.get(c);
                if (ref != null) {
                    cached = ref.get();
                }
                if (cached == null) {
                    cached = new StatefulDockerContainer(c);
                    cache.put(c, new WeakReference<>(cached));
                } else {
                    cached.attach();
                    cached.refresh();
                }
                fresh.add(cached);
            }
            // we add it all at once to prevent remove-add cycle for existing
            // containers
            toPopulate.addAll(fresh);
        }
        return true;
    }

    @Override
    public final void refresh() {
        refresh(false);
    }

    @Override
    public void close() {
        instance.removeContainerListener(listener);
        synchronized (cache) {
            for (WeakReference<StatefulDockerContainer> r : cache.values()) {
                StatefulDockerContainer c  = r.get();
                if (c != null) {
                    c.close();
                }
            }
            cache.clear();
        }
    }

}
