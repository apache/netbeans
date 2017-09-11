/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.masterfs.watcher.nio2;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import org.netbeans.modules.masterfs.providers.Notifier;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Egor Ushakov <gorrus@netbeans.org>
 */
@ServiceProvider(service=Notifier.class, position=1010)
public class NioNotifier extends Notifier<WatchKey> {
    private final WatchService watcher;

    public NioNotifier() throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
    }
    
    @Override
    protected WatchKey addWatch(String pathStr) throws IOException {
        Path path = Paths.get(pathStr);
        try {
            WatchKey key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            return key;
        } catch (ClosedWatchServiceException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected void removeWatch(WatchKey key) throws IOException {
        try {
            key.cancel();
        } catch (ClosedWatchServiceException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected String nextEvent() throws IOException, InterruptedException {
        WatchKey key;
        try {
            key = watcher.take();
        } catch (ClosedWatchServiceException cwse) { // #238261
            @SuppressWarnings({"ThrowableInstanceNotThrown"})
            InterruptedException ie = new InterruptedException();
            throw (InterruptedException) ie.initCause(cwse);
        }
        Path dir = (Path)key.watchable();
               
        String res = dir.toAbsolutePath().toString();
        for (WatchEvent<?> event: key.pollEvents()) {
            if (event.kind() == OVERFLOW) {
                // full rescan
                res = null;
            }
        }
        key.reset();
        return res;
    }

    @Override
    protected void start() throws IOException {
    }

    @Override
    protected void stop() throws IOException {
        watcher.close();
    }
}
