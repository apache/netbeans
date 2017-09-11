/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.csl.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.ErrorFilter;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.util.Lookup;

/**
 * Clients can use this class to filter out some of the parser errors returned by
 * {@link ParserResult.getDiagnostics()}. See the {@link ErrorFilter}
 * documentation. 
 *
 * @author marekfukala
 */
public class ErrorFilterQuery {
    private static final Logger UPDATER_BACKDOOR = Logger.getLogger("org.netbeans.modules.parsing.impl.indexing.LogContext.backdoor");

    public static List<? extends Error> getFilteredErrors(ParserResult parserResult, String featureName) {
        Collection<? extends ErrorFilter.Factory> factories = Lookup.getDefault().lookupAll(ErrorFilter.Factory.class);
        List<Error> filtered = null;
        for(ErrorFilter.Factory factory : factories) {
            ErrorFilter filter = factory.createErrorFilter(featureName);
            String fn = "TLIndexer:" + filterName(factory, filter);
            if (filter == null) {
                continue;
            }
            try {
                LogRecord lr = new LogRecord(Level.INFO, "INDEXER_START");
                lr.setParameters(new Object[] { fn });
                UPDATER_BACKDOOR.log(lr);
                List<? extends Error> result = filter.filter(parserResult);
                if(result != null) {
                    if (filtered == null) {
                        filtered = new ArrayList<Error>(result);
                    } else {
                        filtered.addAll(result); 
                    }
                }
            } finally {
                LogRecord lr = new LogRecord(Level.INFO, "INDEXER_END");
                lr.setParameters(new Object[] { fn });
                UPDATER_BACKDOOR.log(lr);
            }
        }
        return filtered;
    }

    static String filterName(ErrorFilter.Factory fact, ErrorFilter f) {
        Class c;
        
        if (f == null) {
            c = fact.getClass();
        } else {
            c = f.getClass();
        }
        
        String n = c.getName();
        int idx = n.indexOf(".modules.");
        int last = n.lastIndexOf('.');
        if (idx > -1) {
            int n2 = n.indexOf('.', idx + 9);
            return n.substring(idx + 9, n2 + 1) + n.substring(last + 1);
        } else {
           return n.substring(last + 1);
        }
    }
}
