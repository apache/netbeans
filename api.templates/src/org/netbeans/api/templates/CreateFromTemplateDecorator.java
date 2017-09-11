/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.api.templates;

import java.io.IOException;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;

/**
 * Decorator for templating. The decorator will pre- or post-process the main file creation.
 * Decorators are called by the infrastructure, in the declaration order, before the template
 * file is processed and after it is processed, depending on {@link #isBeforeCreation()}
 * and {@link #isAfterCreation()} return values. A decorator may perform both pre- and post- 
 * processing.
 * <p/>
 * First the decorator is asked to {@link #accept} the creation process; if it does not,
 * it will not be invoked at all. Before/after main file creation the {@link #decorate}
 * method is called to perform its magic. Any reported additional files will be returned
 * as part of {@link FileBuilder#build()} result.
 * @since 1.9
 * @author sdedic
 */
public interface CreateFromTemplateDecorator {
    /**
     * Determines if the decorator should be called before template processing. Pre-decorators
     * can setup the environment before the main file creation takes place.
     * @return true, if the decorator should be called before template processing
     */
    public boolean  isBeforeCreation();
    
    /**
     * Determines if the decorator should be called after template processing. Post-decorators
     * can make additional (e.g. settings, registrations) adjustments.
     * @return true, if the decorator should be called after template processing
     */
    public boolean  isAfterCreation();
    
    /** 
     * Determines whether the decorator is willing to participate in creation.
     * If the decorator returns {@code false}, its {@link #decorate} will not be called. It
     * @param desc describes the request that is about to be performed
     * @return true if this decorator wants to participate in files creation
     */
    public abstract boolean accept(CreateDescriptor desc);

    /**
     * Extends the creation process. The decorator may alter the created file (it is the first in the 
     * returned list) or create additional files. In case it creates files, it must return list of the
     * added files.
     * <p/>
     * If the decorator is not interested, it should return simply {@code null}.
     * 
     * @param desc command objects that describes the file creation request
     * @param createdFiles  files created so far as part of the template creation
     * @return the newly create file(s)
     * @throws IOException if something goes wrong with I/O
     */
    public @CheckForNull List<FileObject> decorate(
            @NonNull CreateDescriptor    desc,
            @NonNull List<FileObject>    createdFiles
    ) throws IOException;
}
