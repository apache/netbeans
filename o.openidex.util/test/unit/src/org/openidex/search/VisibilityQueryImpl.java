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

package org.openidex.search;

import javax.swing.event.ChangeListener;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 * Primitive implementation of {@link VisibilityQuery}.
 *
 * @author  Marian Petras
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.VisibilityQueryImplementation.class)
public class VisibilityQueryImpl implements VisibilityQueryImplementation {

    private static final String INVISIBLE_SUFFIX = "_invisible";

    public boolean isVisible(FileObject file) {
        final String name = file.getName();
        return !name.endsWith(INVISIBLE_SUFFIX);
    }

    public void addChangeListener(ChangeListener l) {
        /*
         * Does nothing - the visibility never changes so there is need
         * to register listeners.
         */
    }

    public void removeChangeListener(ChangeListener l) {
        /*
         * Does nothing - the visibility never changes so there is need
         * to register listeners.
         */
    }

}
