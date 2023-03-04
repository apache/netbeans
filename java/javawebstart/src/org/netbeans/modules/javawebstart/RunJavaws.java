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

package org.netbeans.modules.javawebstart;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * Offers action to run {@code javaws} on a JNLP file.
 */
@ServiceProvider(service=ActionProvider.class)
public class RunJavaws implements ActionProvider {

    private static final RequestProcessor RP = new RequestProcessor(RunJavaws.class.getName(), Integer.MAX_VALUE);

    @Override public String[] getSupportedActions() {
        return new String[] {COMMAND_RUN_SINGLE};
    }

    @Override public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        Collection<? extends DataObject> ds = context.lookupAll(DataObject.class);
        return ds.size() == 1 && ds.iterator().next().getPrimaryFile().getMIMEType().equals(JnlpDataLoader.REQUIRED_MIME) &&
                JavaPlatform.getDefault().findTool("javaws") != null;
    }

    @Override public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        final ActionProgress listener = ActionProgress.start(context);
        try {
            final Process p =
            new ProcessBuilder(FileUtil.toFile(JavaPlatform.getDefault().findTool("javaws")).getAbsolutePath(),
                    context.lookup(DataObject.class).getPrimaryFile().toURL().toString()).start();
            RP.post(new Runnable() {
                @Override public void run() {
                    try {
                        listener.finished(p.waitFor() == 0);
                    } catch (InterruptedException x) {
                        listener.finished(false);
                    }
                }
            });
        } catch (IOException x) {
            Exceptions.printStackTrace(x);
            listener.finished(false);
        }
    }

}
