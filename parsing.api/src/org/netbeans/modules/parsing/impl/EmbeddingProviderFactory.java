/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class EmbeddingProviderFactory extends TaskFactory {

    public static final String ATTR_TARGET_MIME_TYPE = "targetMimeType";   //NOI18N
    public static final String ATTR_PROVIDER = "provider";                 //NOI18N
    
    private final Map<String,Object> params;
    private final String targetMimeType;

    private EmbeddingProviderFactory(@NonNull final Map<String,Object> params) {
        Parameters.notNull("definition", params);   //NOI18N
        this.params = params;
        this.targetMimeType = (String) params.get(ATTR_TARGET_MIME_TYPE);
        if (this.targetMimeType == null) {
            throw new IllegalArgumentException(
                String.format(
                    "The definition file has no %s attribute.", //NOI18N
                    ATTR_TARGET_MIME_TYPE));
        }
    }

    public String getTargetMimeType() {
        return this.targetMimeType;
    }

    @NonNull
    @Override
    public Collection<? extends SchedulerTask> create (@NonNull final Snapshot snapshot) {
        final Object delegate = params.get(ATTR_PROVIDER);
        return (delegate instanceof EmbeddingProvider) ?
                Collections.singleton((EmbeddingProvider)delegate) :
                Collections.<EmbeddingProvider>emptySet();
    }

    public static TaskFactory create(@NonNull final Map<String,Object> params) {
        return new EmbeddingProviderFactory(params);
    }
}
