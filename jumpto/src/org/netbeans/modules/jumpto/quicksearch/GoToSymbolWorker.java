/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */


package org.netbeans.modules.jumpto.quicksearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.ListModel;
import org.netbeans.modules.jumpto.symbol.SymbolComparator;
import org.netbeans.modules.jumpto.symbol.SymbolProviderAccessor;
import org.netbeans.modules.jumpto.common.Models;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.util.Lookup;

/**
 * Copy/paste from GoToSymbolAction
 * @author  Jan Becicka
 */
public class GoToSymbolWorker implements Runnable {

    private volatile boolean isCanceled = false;
    private volatile SymbolProvider current;
    private final String text;
    private final long createTime;
    private Logger LOGGER = Logger.getLogger(GoToSymbolWorker.class.getName());
    private Collection<? extends SymbolProvider> typeProviders;


    public GoToSymbolWorker(String text) {
        this.text = text;
        this.createTime = System.currentTimeMillis();
        //LOGGER.fine("Worker for " + text + " - created after " + (System.currentTimeMillis() - panel.time) + " ms.");
    }

    private List<? extends SymbolDescriptor> types;

    public List<? extends SymbolDescriptor> getTypes() {
        return types==null?Collections.<SymbolDescriptor>emptyList():types;
    }

    public void run() {

        LOGGER.fine("Worker for " + text + " - started " + (System.currentTimeMillis() - createTime) + " ms.");

        types = getSymbolNames(text);
        if (isCanceled) {
            LOGGER.fine("Worker for " + text + " exited after cancel " + (System.currentTimeMillis() - createTime) + " ms.");
            return;
        }
        final ListModel fmodel = Models.fromList(types, null, null);
        if (isCanceled) {
            LOGGER.fine("Worker for " + text + " exited after cancel " + (System.currentTimeMillis() - createTime) + " ms.");
            return;
        }

//        if (!isCanceled && fmodel != null) {
//            LOGGER.fine("Worker for text " + text + " finished after " + (System.currentTimeMillis() - createTime) + " ms.");
//            SwingUtilities.invokeLater(new Runnable() {
//
//                public void run() {
//                    panel.setModel(fmodel);
//                    if (okButton != null && !types.isEmpty()) {
//                        okButton.setEnabled(true);
//                    }
//                }
//            });
//        }


    }

    public void cancel() {
//        if (panel.time != -1) {
//            LOGGER.fine("Worker for text " + text + " canceled after " + (System.currentTimeMillis() - createTime) + " ms.");
//        }
        SymbolProvider _provider;
        synchronized (this) {
            isCanceled = true;
            _provider = current;
        }
        if (_provider != null) {
            _provider.cancel();
        }
    }

    @SuppressWarnings("unchecked")
    private List<? extends SymbolDescriptor> getSymbolNames(String text) {
        // TODO: Search twice, first for current project, then for all projects
        List<SymbolDescriptor> items;
        // Multiple providers: merge results
        items = new ArrayList<SymbolDescriptor>(128);
        String[] message = new String[1];
        SymbolProvider.Context context = SymbolProviderAccessor.DEFAULT.createContext(null, text, SearchType.CASE_INSENSITIVE_PREFIX);
        for (SymbolProvider provider : getTypeProviders()) {
            SymbolProvider.Result result = SymbolProviderAccessor.DEFAULT.createResult(items, message, context, provider);
            current = provider;
            if (isCanceled) {
                return null;
            }
            LOGGER.fine("Calling SymbolProvider: " + provider);
            provider.computeSymbolNames(context, result);
            current = null;
        }
        if (!isCanceled) {
            //time = System.currentTimeMillis();
            Collections.sort(items, new SymbolComparator());
            //panel.setWarning(message[0]);
            //sort += System.currentTimeMillis() - time;
            //LOGGER.fine("PERF - " + " GSS:  " + gss + " GSB " + gsb + " CP: " + cp + " SFB: " + sfb + " GTN: " + gtn + "  ADD: " + add + "  SORT: " + sort );
            return items;
        } else {
            return null;
        }
    }
    private Collection<? extends SymbolProvider> getTypeProviders() {
        if (typeProviders==null) {
            typeProviders = Arrays.asList(Lookup.getDefault().lookupAll(SymbolProvider.class).toArray(new SymbolProvider[0]));
        }
        return typeProviders;
    }

}

