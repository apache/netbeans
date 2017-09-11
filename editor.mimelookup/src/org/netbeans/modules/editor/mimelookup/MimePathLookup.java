/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.editor.mimelookup;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.editor.mimelookup.MimeLookupInitializer;
import org.openide.util.*;
import org.openide.util.Lookup.Template;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


/**
 *
 * @author vita
 */
@SuppressWarnings("deprecation")
public final class MimePathLookup extends ProxyLookup implements LookupListener {

    private static final Logger LOG = Logger.getLogger(MimePathLookup.class.getName());
    private static final RequestProcessor WORKER = new RequestProcessor("MimePathLookupFiring", 1);

    private final MimePath mimePath;
    private final boolean mimePathBanned;
    private final Lookup.Result<MimeDataProvider> dataProviders;
    private final Lookup.Result<MimeLookupInitializer> mimeInitializers; // This is supported for backwards compatibility only.
    private boolean initialized = false;

    /** Creates a new instance of MimePathLookup */
    public MimePathLookup(MimePath mimePath) {
        super();

        if (mimePath == null) {
            throw new NullPointerException("Mime path can't be null."); //NOI18N
        }

        this.mimePath = mimePath;
        this.mimePathBanned = mimePath.size() > 0 && mimePath.getMimeType(0).contains("text/base"); //NOI18N
        
        class R implements Runnable {
            Lookup.Result<MimeDataProvider> dataProviders;
            Lookup.Result<MimeLookupInitializer> mimeInitializers;
            
            public void run() {
                dataProviders = Lookup.getDefault().lookup(new Lookup.Template<MimeDataProvider>(MimeDataProvider.class));
                dataProviders.addLookupListener(WeakListeners.create(LookupListener.class, MimePathLookup.this, dataProviders));

                mimeInitializers = Lookup.getDefault().lookup(new Lookup.Template<MimeLookupInitializer>(MimeLookupInitializer.class));
                mimeInitializers.addLookupListener(WeakListeners.create(LookupListener.class, MimePathLookup.this, mimeInitializers));
            }
        }
        R r = new R();
        Lookups.executeWith(null, r);
        this.dataProviders = r.dataProviders;
        this.mimeInitializers = r.mimeInitializers;
    }

    @Override
    protected void beforeLookup(Template<?> template) {
        synchronized (this) {
            if (!initialized) {
                initialized = true;
                rebuild();
            }
        }
    }


    public MimePath getMimePath() {
        return mimePath;
    }

    private void rebuild() {
        ArrayList<Lookup> lookups = new ArrayList<Lookup>();

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Rebuilding MimeLookup for '" + mimePath.getPath() + "'..."); //NOI18N
        }

        // Add lookups from MimeDataProviders
        for (MimeDataProvider provider : dataProviders.allInstances()) {
            if (mimePathBanned && !isDefaultProvider(provider)) {
                continue;
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("- Querying MimeDataProvider(" + mimePath.getPath() + "): " + provider); //NOI18N
            }
            Lookup mimePathLookup = provider.getLookup(mimePath);
            if (mimePathLookup != null) {
                lookups.add(mimePathLookup);
            }
        }

        // XXX: This hack here is to make GSF and Schliemann frameworks work.
        // Basically we should somehow enforce the composition of lookups
        // for MimeDataProviders too. But some providers such as the one from
        // editor/mimelookup/impl do the composition in their own way. So we
        // will probably have to extend the SPI somehow to accomodate both simple
        // providers and the composing ones.
        // See also http://www.netbeans.org/issues/show_bug.cgi?id=118099

        // Add lookups from deprecated MimeLookupInitializers
        List<MimePath> paths = mimePath.getIncludedPaths();
        for(MimePath mp : paths) {
            Collection<? extends MimeLookupInitializer> initializers = mimeInitializers.allInstances();

            for(int i = 0; i < mp.size(); i++) {
                Collection<MimeLookupInitializer> children = new ArrayList<MimeLookupInitializer>(initializers.size());

                for(MimeLookupInitializer mli : initializers) {
                    children.addAll(mli.child(mp.getMimeType(i)).allInstances());
                }

                initializers = children;
            }

            for(MimeLookupInitializer mli : initializers) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("- Querying MimeLookupInitializer(" + mp.getPath() + "): " + mli); //NOI18N
                }
                Lookup mimePathLookup = mli.lookup();
                if (mimePathLookup != null) {
                    lookups.add(mimePathLookup);
                }
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("MimeLookup for '" + mimePath.getPath() + "' rebuilt."); //NOI18N
        }

        setLookups(WORKER, lookups.toArray(new Lookup[lookups.size()])); // NOI18N
    }

    private boolean isDefaultProvider(MimeDataProvider provider) {
        return provider.getClass().getName().equals("org.netbeans.modules.editor.mimelookup.impl.DefaultMimeDataProvider"); //NOI18N
    }

    //-------------------------------------------------------------
    // LookupListener implementation
    //-------------------------------------------------------------

    public synchronized void resultChanged(LookupEvent ev) {
        rebuild();
    }

}
