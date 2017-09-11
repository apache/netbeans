/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.api;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.parsing.implspi.EnvironmentFactory;
import org.netbeans.modules.parsing.implspi.SchedulerControl;
import org.netbeans.modules.parsing.implspi.SourceControl;
import org.netbeans.modules.parsing.implspi.SourceEnvironment;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Simplified testing environment. Does not react on document changes or 
 * EditorRegistry changes.
 * 
 * @author sdedic
 */
@ServiceProvider(service = EnvironmentFactory.class, position = 50000)
public class TestEnvironmentFactory implements EnvironmentFactory {
    private static Map<String,Reference<Parser>> cachedParsers = new HashMap<String,Reference<Parser>>();
    
    @Override
    public Class<? extends Scheduler> findStandardScheduler(String schedulerName) {
        return null;
    }

    @Override
    public SourceEnvironment createEnvironment(Source src, SourceControl control) {
        return new Env(control);
    }

    @Override
    public <T> T runPriorityIO(Callable<T> r) throws Exception {
        return r.call();
    }

    @Override
    public Lookup getContextLookup() {
        return Lookup.getDefault();
    }

    @Override
    public synchronized Parser findMimeParser(Lookup context, String mimeType) {
        Reference<Parser> rp = cachedParsers.get(mimeType);
        Parser p = null;
        if (rp != null) {
            p = rp.get();
        }
        if (p != null) {
            return p;
        }
        ParserFactory f = MimeLookup.getLookup(mimeType).lookup(ParserFactory.class);
        if (f != null) {
            p = f.createParser(Collections.<Snapshot>emptyList());
        } else {
            p = MimeLookup.getDefault().lookup(ParserFactory.class).createParser(Collections.<Snapshot>emptyList());
        }
        cachedParsers.put(mimeType, new WeakReference<>(p));
        return p;
    }

    @Override
    public Collection<? extends Scheduler> getSchedulers(Lookup context) {
        return getContextLookup().lookupAll(Scheduler.class);
    }

    static class Env extends SourceEnvironment {

        public Env(SourceControl ctrl) {
            super(ctrl);
        }

        @Override
        public boolean isReparseBlocked() {
            return false;
        }

        @Override
        public Document readDocument(FileObject f, boolean forceOpen) throws IOException {
            DataObject d = DataObject.find(f);
            EditorCookie cake = d.getCookie(EditorCookie.class);
            if (!forceOpen) {
                return cake.getDocument();
            } else {
                return cake.openDocument();
            }
        }

        @Override
        public void attachScheduler(SchedulerControl s, boolean attach) {
            // FIXME - schedulers will not react if the source file changes
            // because of rename etc.
        }

        @Override
        public void activate() {
            listenOnFileChanges();
            listenOnParser();
        }
    }
}
