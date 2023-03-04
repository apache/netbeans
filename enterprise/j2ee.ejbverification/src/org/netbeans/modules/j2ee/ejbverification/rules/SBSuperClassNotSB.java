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
package org.netbeans.modules.j2ee.ejbverification.rules;

import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.modules.j2ee.ejbverification.JavaUtils;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 * A session bean must not extend another session bean
 *
 * @author Tomasz.Slota@Sun.COM, Martin Fousek <marfous@netbeans.org>
 */
@Hint(displayName = "#SBSuperClassNotSB.display.name",
        description = "#SBSuperClassNotSB.err",
        id = "o.n.m.j2ee.ejbverification.SBSuperClassNotSB",
        category = "javaee/ejb",
        enabled = true,
        suppressWarnings = "SBSuperClassNotSB")
@NbBundle.Messages({
    "SBSuperClassNotSB.display.name=Inheritance of session beans",
    "SBSuperClassNotSB.err=A session bean must not extend another session bean."
})
public final class SBSuperClassNotSB {

    private static final Logger LOG = Logger.getLogger(SBSuperClassNotSB.class.getName());

    private SBSuperClassNotSB() {
    }

    @TriggerTreeKind(Tree.Kind.CLASS)
    public static Collection<ErrorDescription> run(final HintContext hintContext) {
        final List<ErrorDescription> problems = new ArrayList<>();
        final EJBProblemContext ctx = HintsUtils.getOrCacheContext(hintContext);
        if (ctx != null && ctx.getEjb() instanceof Session) {
            TypeMirror parentType = ctx.getClazz().getSuperclass();
            final String parentClassName = JavaUtils.extractClassNameFromType(parentType);

            if (parentClassName != null) {
                try {
                    ctx.getEjbModule().getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
                        @Override
                        public Void run(EjbJarMetadata metadata) throws Exception {
                            Ejb parentEJB = metadata.findByEjbClass(parentClassName);
                            if (parentEJB instanceof Session) {
                                ErrorDescription err = HintsUtils.createProblem(
                                        ctx.getClazz(),
                                        hintContext.getInfo(),
                                        Bundle.SBSuperClassNotSB_err());
                                problems.add(err);
                            }
                            return null;
                        }
                    });
                } catch (MetadataModelException ex) {
                    LOG.log(Level.WARNING, ex.getMessage(), ex);
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        }
        return problems;
    }
}
