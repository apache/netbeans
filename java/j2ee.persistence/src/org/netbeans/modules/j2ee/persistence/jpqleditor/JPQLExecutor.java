/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.j2ee.persistence.jpqleditor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.lang.model.util.Elements;
import org.eclipse.persistence.jpa.jpql.JPQLQueryProblem;
import org.eclipse.persistence.jpa.jpql.JPQLQueryProblemResourceBundle;
import org.eclipse.persistence.jpa.jpql.parser.DefaultJPQLGrammar;
import org.eclipse.persistence.jpa.jpql.tools.DefaultJPQLQueryHelper;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceEnvironment;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.jpql.ManagedTypeProvider;

/**
 * Executes JPQL query.
 */
public class JPQLExecutor {

    private static final Logger LOG = Logger.getLogger(JPQLExecutor.class.getName());

    private static final String ECLIPSELINK_QUERY = "org.eclipse.persistence.jpa.JpaQuery";//NOI18N
    private static final String ECLIPSELINK_QUERY_SQL0 = "getDatabaseQuery";//NOI18N
    private static final String ECLIPSELINK_QUERY_SQL1 = "getSQLString";//NOI18N
    private static final String HIBERNATE_QUERY = "org.hibernate.ejb.HibernateQuery";//NOI18N
    private static final String HIBERNATE_QUERY_SQL0 = "getSessionFactory";//NOI18N
    private static final String HIBERNATE_QUERY_SQL1 = "getQueryPlanCache";//NOI18N
    private static final String HIBERNATE_QUERY_SQL2 = "getHQLQueryPlan";//NOI18N
    private static final String HIBERNATE_QUERY_SQL3 = "getTranslators";//NOI18N
    private static final String HIBERNATE_QUERY_SQL4 = "getSQLString";//NOI18N
    private static final String OPENJPA_QUERY = "org.apache.openjpa.persistence.QueryImpl";//NOI18N
    private static final String OPENJPA_QUERY_SQL = "getQueryString";//NOI18N

    /**
     * Executes given JPQL query and returns the result.
     *
     * @param jpql the query
     * @param execute execute query (true) or just try to get SQL (false)
     * @return JPQLResult containing the execution result (including any
     * errors).
     */
    public JPQLResult execute(String jpql,
            Persistence persistence,
            PersistenceUnit persistenceUnit,
            final PersistenceEnvironment pe,
            HashMap<String, String> props,
            Provider provider,
            int maxRowCount,
            ProgressHandle ph,
            boolean execute) {
        JPQLResult result = new JPQLResult();
        jpql = jpql.trim();
        try {
            Object emf = null;
            Object em = null;
            Object transaction = null;
            try {
                ph.progress(60);

                Class<?> pClass;
                if (isJakarta(persistence.getVersion())) {
                    pClass = Thread.currentThread().getContextClassLoader().loadClass("jakarta.persistence.Persistence");//NOI18N
                } else {
                    pClass = Thread.currentThread().getContextClassLoader().loadClass("javax.persistence.Persistence");//NOI18N
                }
                Object persistenceManager = pClass.getDeclaredConstructor().newInstance();

                emf = invokeReflectively(persistenceManager,
                        "createEntityManagerFactory",
                        List.of(String.class, Map.class),
                        List.of(persistenceUnit.getName(), props));

                em = invokeReflectively(emf, "createEntityManager", List.of(), List.of());

                Logger.getLogger("org.hibernate.hql.internal.ast.ErrorCounter").setFilter( (LogRecord record) -> { //NOI18N
                    //workaround to avoid exception dialog from nb for logged exception
                    if (record.getLevel().intValue() > Level.INFO.intValue()) {
                        record.setLevel(Level.INFO);
                    }
                    return true;
                });
                Object query = invokeReflectively(em, "createQuery", List.of(String.class), List.of(jpql));
                String queryStr = null;
                if ("org.eclipse.persistence.jpa.PersistenceProvider".equals(provider.getProviderClass())) {
                    Class<?> qClass = Thread.currentThread().getContextClassLoader().loadClass(ECLIPSELINK_QUERY);
                    if (qClass != null) {
                        Method method = qClass.getMethod(ECLIPSELINK_QUERY_SQL0);
                        if (method != null) {
                            Object dqOject = method.invoke(query);
                            Method method2 = (dqOject != null ? dqOject.getClass().getMethod(ECLIPSELINK_QUERY_SQL1) : null);
                            if (method2 != null) {
                                queryStr = (String) method2.invoke(dqOject);
                            }
                        }
                    }
                } else if ("org.hibernate.jpa.HibernatePersistenceProvider".equals(provider.getProviderClass())) {
                    Method method = emf.getClass().getMethod(HIBERNATE_QUERY_SQL0);
                    Object sessionFactoryImpl = method.invoke(emf);
                    Method method2 = sessionFactoryImpl.getClass().getMethod(HIBERNATE_QUERY_SQL1);
                    Object qPlanCache = method2.invoke(sessionFactoryImpl);
                    Method method3 = qPlanCache.getClass().getMethod(HIBERNATE_QUERY_SQL2, String.class, boolean.class, Map.class);
                    Object cache = method3.invoke(qPlanCache, jpql, true, Collections.emptyMap());
                    Method method4 = cache.getClass().getMethod(HIBERNATE_QUERY_SQL3);
                    Object[] translators = (Object[]) method4.invoke(cache);
                    StringBuilder stringBuff = new StringBuilder();
                    if (translators != null && translators.length > 0) {
                        Method method5 = translators[0].getClass().getMethod(HIBERNATE_QUERY_SQL4);
                        for (Object translator : translators) {
                            stringBuff.append(method5.invoke(translator)).append("\n");
                        }
                    }
                    queryStr = stringBuff.toString();
                } else if ("org.apache.openjpa.persistence.PersistenceProviderImpl".equals(provider.getProviderClass())) {
                    Class<?> qClass = Thread.currentThread().getContextClassLoader().loadClass(OPENJPA_QUERY);
                    if(qClass !=null) {
                        Method method = qClass.getMethod(OPENJPA_QUERY_SQL);
                        if(method != null){
                            queryStr = (String) method.invoke(query);
                        }
                    }
                }
                result.setSqlQuery(queryStr);
                //
                ph.progress(70);
                if (execute) {
                    transaction = invokeReflectively(em, "getTransaction", List.of(), List.of());
                    invokeReflectively(transaction, "begin", List.of(), List.of());
                    invokeReflectively(query, "setMaxResults", List.of(int.class), List.of(maxRowCount));


                    String jpql0 = jpql.toUpperCase();

                    if (jpql0.startsWith("UPDATE") || jpql0.startsWith("DELETE")) { //NOI18N
                        result.setUpdateOrDeleteResult((int) invokeReflectively(query, "executeUpdate", List.of(), List.of()));
                    } else {
                        result.setQueryResults((List) invokeReflectively(query, "getResultList", List.of(), List.of()));
                    }
                    invokeReflectively(transaction, "commit", List.of(), List.of());
                }
            } catch (ReflectiveOperationException | RuntimeException e) {
                result.getExceptions().add(e);
                final Project project = pe.getProject();
                SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                JavaSource js = JavaSource.create(ClasspathInfo.create(sourceGroups[0].getRootFolder()));
                final List<JPQLQueryProblem> problems = new ArrayList<>();
                final String jpql0 = jpql;
                try {
                    js.runUserActionTask( (CompilationController controller) -> {
                        controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        EntityClassScopeProvider provider1 = project.getLookup().lookup(EntityClassScopeProvider.class);
                        EntityClassScope ecs = null;
                        if (provider1 != null) {
                            ecs = provider1.findEntityClassScope(pe.getLocation().getFileObject("persistence.xml"));
                        }
                        EntityClassScope scope = ecs;
                        MetadataModel<EntityMappingsMetadata> entityMappingsModel = null;
                        if (scope != null) {
                            entityMappingsModel = scope.getEntityMappingsModel(false); // false since I guess you only want the entity classes defined in the project
                        }
                        if (entityMappingsModel != null) {
                            final Elements elms = controller.getElements();
                            entityMappingsModel.runReadAction( (EntityMappingsMetadata metadata) -> {
                                ManagedTypeProvider mtp = new ManagedTypeProvider(project, metadata, elms);
                                DefaultJPQLQueryHelper  helper = new DefaultJPQLQueryHelper (DefaultJPQLGrammar.instance());
                                helper.setQuery(new org.netbeans.modules.j2ee.persistence.spi.jpql.Query(null, jpql0, mtp));

                                try {
                                    problems.addAll(helper.validate());
                                } catch (Exception ex) {
                                }
                                return null;
                            });
                        }
                    }, false);
                } catch (IOException ex) {
                } finally {
                    if(transaction != null){
                        invokeReflectively(transaction, "isActive");
                        invokeReflectively(transaction, "rollback");
                    }
                    if(em != null) {
                        invokeReflectively(em, "clear");
                        invokeReflectively(em, "close");
                    }
                    if(emf != null) {
                        invokeReflectively(emf, "close");
                    }
                }
                if (!problems.isEmpty()) {
                    //use parsed result for errors
                    StringBuilder message = new StringBuilder();
                    for (int i = 0; i < problems.size(); i++) {
                        ListResourceBundle msgBundle;
                        try {
                            msgBundle = (ListResourceBundle) ResourceBundle.getBundle(JPQLQueryProblemResourceBundle.class.getName());//NOI18N
                        } catch (MissingResourceException ex) {//default en
                            msgBundle = (ListResourceBundle) ResourceBundle.getBundle(JPQLQueryProblemResourceBundle.class.getName(), Locale.ENGLISH);//NOI18N
                        }
                        message.append(java.text.MessageFormat.format(msgBundle.getString(problems.get(i).getMessageKey()), (Object[]) problems.get(i).getMessageArguments())).append("\n");
                    }
                    result.setQueryProblems(message.toString());
                }
            }
        } catch (SecurityException | ReflectiveOperationException ex) {
            // These catches only cover the transaction, entity manager and
            // entity manager factory cleanups.
            LOG.log(Level.INFO, "Failed to execute JPA query", ex);
        }
        return result;
    }

    private Object invokeReflectively(Object thisObj, String methodName) throws SecurityException, ReflectiveOperationException {
        return invokeReflectively(thisObj, methodName, List.of(), List.of());
    }

    private Object invokeReflectively(Object thisObj, String methodName, List<?> parameterClass, List<Object> args) throws SecurityException, ReflectiveOperationException {
        Class<?> clazz = thisObj.getClass();
        Method targetMethod = clazz.getMethod(methodName, parameterClass.toArray(Class<?>[]::new));
        return targetMethod.invoke(thisObj, args.toArray(Object[]::new));
    }

    private boolean isJakarta(String versionString) {
        String[] versionParts = versionString.split("\\.");
        Integer version = null;
        try {
            version = Integer.valueOf(versionParts[0]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            LOG.log(Level.INFO, "Failed to parse persistence version: " + versionString, ex);
        }
        return version == null || version >= 3;
    }
}
