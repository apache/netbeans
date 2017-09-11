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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.api.search.provider.impl;

import java.io.File;
import java.net.URI;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.api.queries.SharabilityQuery.Sharability;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.util.Utilities;

/**
 * Primitive implementation of {@link SharabilityQuery}.
 *
 * @author  MarianPetras
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.SharabilityQueryImplementation2.class)
public class SharabilityQueryImpl implements SharabilityQueryImplementation2 {

    private static final String SHARABLE_SUFFIX = "_sharable";
    private static final String NON_SHARABLE_SUFFIX = "_unsharable";

    @Override
    public Sharability getSharability(URI uri) {
        final File file = Utilities.toFile(uri);
        if (file == null) {
            return Sharability.NOT_SHARABLE;
        }
        final String simpleName = getSimpleName(file);
        if (simpleName.endsWith(SHARABLE_SUFFIX)) {
            return Sharability.SHARABLE;
        } else if (simpleName.endsWith(NON_SHARABLE_SUFFIX)) {
            return Sharability.NOT_SHARABLE;
        } else {
            return file.isDirectory() ? Sharability.MIXED
                                      : Sharability.SHARABLE;
        }
    }

    private static String getSimpleName(File file) {
        String name = file.getName();
        if (file.isDirectory()) {
            return name;
        } else {
            int lastDotIndex = name.lastIndexOf('.');
            return (lastDotIndex != -1) ? name.substring(0, lastDotIndex)
                                        : name;
        }
    }

}
