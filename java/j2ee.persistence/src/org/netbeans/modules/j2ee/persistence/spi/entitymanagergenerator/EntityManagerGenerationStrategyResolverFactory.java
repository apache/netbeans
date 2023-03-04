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

package org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator;

import java.io.IOException;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.injection.api.InjectionTargetQuery;
import org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Factory class providing default implementation of EntityManagerGenerationStrategyResolver.
 * Any project type which need to use {@link EntityManagerGenerationStrategyResolver} and
 * want to reuse this default implementation should put the instance either to the project
 * lookup or better register it using {@link org.netbeans.spi.project.ProjectServiceProvider}.
 *
 * @see EntityManagerGenerationStrategyResolver
 * @author Martin Janicek
 *
 * @since 1.39
 */
public final class EntityManagerGenerationStrategyResolverFactory {

    private EntityManagerGenerationStrategyResolverFactory() {
    }

    /**
     * For the given project creates default implementation of the {@link EntityManagerGenerationStrategyResolver}.
     *
     * @param project project for which we need to create default implementation
     * @return default implementation
     *
     * @since 1.39
     */
    public static EntityManagerGenerationStrategyResolver createInstance(Project project) {
        return new EMGenStrategyResolverImpl(project);
    }

    private static class EMGenStrategyResolverImpl implements EntityManagerGenerationStrategyResolver {

        private Project project;


        private EMGenStrategyResolverImpl(Project project) {
            this.project = project;
        }

        @Override
        public Class<? extends EntityManagerGenerationStrategy> resolveStrategy(final FileObject target) {

            PersistenceUnit persistenceUnit = getPersistenceUnit(target);
            String jtaDataSource = persistenceUnit.getJtaDataSource();
            String transactionType = persistenceUnit.getTransactionType();
            boolean isInjectionTarget = isInjectionTarget(target);
            boolean isJTA = (transactionType == null || transactionType.equals("JTA")); // JTA is default value for transaction type in non-J2SE projects
            boolean isContainerManaged = (jtaDataSource != null && !jtaDataSource.isEmpty()) && isJTA; //NO18N

            ContainerClassPathModifier modifier = project.getLookup().lookup(ContainerClassPathModifier.class);
            if (modifier != null) {
                modifier.extendClasspath(target,
                    new String[] {
                        ContainerClassPathModifier.API_ANNOTATION,
                        ContainerClassPathModifier.API_PERSISTENCE,
                        ContainerClassPathModifier.API_TRANSACTION
                    });

            }

            if (isContainerManaged) { // Container-managed persistence context
                if (isInjectionTarget) { // servlet, JSF managed bean ...
                    return ContainerManagedJTAInjectableInEJB.class;
                } else { // other classes
                    return ContainerManagedJTANonInjectableInWeb.class;
                }
            } else if (!isJTA) { // Application-managed persistence context (Resource-transaction)
                if (isInjectionTarget) { // servlet, JSF managed bean ...
                    return ApplicationManagedResourceTransactionInjectableInWeb.class;
                } else { // other classes
                    return ApplicationManagedResourceTransactionNonInjectableInWeb.class;
                }
            }

            return null;
        }

        private boolean isInjectionTarget(FileObject target) {
            final boolean[] result = new boolean[1];
            JavaSource source = JavaSource.forFileObject(target);
            if (source == null) {
                return false;
            }
            try {
                source.runModificationTask( (WorkingCopy parameter) -> {
                    parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = SourceUtils.getPublicTopLevelElement(parameter);
                    result[0] = InjectionTargetQuery.isInjectionTarget(parameter, typeElement);
                });
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
            return result[0];
        }


        private PersistenceUnit getPersistenceUnit(FileObject target) {
            PersistenceScope persistenceScope = PersistenceScope.getPersistenceScope(target);
            if (persistenceScope == null) {
                return null;
            }

            try {
                // TODO: fix ASAP! 1st PU is taken, needs to find the one which realy owns given file
                Persistence persistence = PersistenceMetadata.getDefault().getRoot(persistenceScope.getPersistenceXml());
                if (persistence != null) {
                    return persistence.getPersistenceUnit(0);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
    }
}
