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
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
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
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.jpql.ManagedTypeProvider;

/**
 * Executes JPQL query.
 */
public class JPQLExecutor {

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
            PersistenceUnit pu,
            final PersistenceEnvironment pe,
            HashMap<String, String> props,
            Provider provider,
            int maxRowCount,
            ProgressHandle ph,
            boolean execute) {
        JPQLResult result = new JPQLResult();
        jpql = jpql.trim();
        EntityManagerFactory emf = null;
        EntityManager em = null;
        EntityTransaction transaction = null;
        try {
            ph.progress(60);

            Class pClass = Thread.currentThread().getContextClassLoader().loadClass("javax.persistence.Persistence");//NOI18N
            javax.persistence.Persistence p = (javax.persistence.Persistence) pClass.getDeclaredConstructor().newInstance();
            
            emf = p.createEntityManagerFactory(pu.getName(), props);

            em = emf.createEntityManager();

            Logger.getLogger("org.hibernate.hql.internal.ast.ErrorCounter").setFilter( (LogRecord record) -> { //NOI18N
                //workaround to avoid exception dialog from nb for logged exception
                if (record.getLevel().intValue() > Level.INFO.intValue()) {
                    record.setLevel(Level.INFO);
                }
                return true;
            });
            Query query = em.createQuery(jpql);
            String queryStr = null;
            if (provider.equals(ProviderUtil.ECLIPSELINK_PROVIDER3_1) 
                    || provider.equals(ProviderUtil.ECLIPSELINK_PROVIDER2_0)
                    || provider.equals(ProviderUtil.ECLIPSELINK_PROVIDER2_1)
                    || provider.equals(ProviderUtil.ECLIPSELINK_PROVIDER2_2)) {
                Class qClass = Thread.currentThread().getContextClassLoader().loadClass(ECLIPSELINK_QUERY);
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
            } else if (provider.equals(ProviderUtil.HIBERNATE_PROVIDER2_0) 
                    || provider.equals(ProviderUtil.HIBERNATE_PROVIDER2_1)
                    || provider.equals(ProviderUtil.HIBERNATE_PROVIDER2_2)) {
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
            } else if (provider.equals(ProviderUtil.OPENJPA_PROVIDER2_0) 
                    || provider.equals(ProviderUtil.OPENJPA_PROVIDER2_1)
                    || provider.equals(ProviderUtil.OPENJPA_PROVIDER2_2)) {
                Class qClass = Thread.currentThread().getContextClassLoader().loadClass(OPENJPA_QUERY);
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
                transaction = em.getTransaction();
                transaction.begin();
                query.setMaxResults(maxRowCount);

                
                String jpql0 = jpql.toUpperCase();

                if (jpql0.startsWith("UPDATE") || jpql0.startsWith("DELETE")) { //NOI18N
                    result.setUpdateOrDeleteResult(query.executeUpdate());
                } else {
                    result.setQueryResults(query.getResultList());
                }
                transaction.commit();
            }
        } catch (Exception e) {
            result.getExceptions().add(e);
            final Project project = pe.getProject();
            SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            JavaSource js = JavaSource.create(ClasspathInfo.create(sourceGroups[0].getRootFolder()));
            final List<JPQLQueryProblem> problems = new ArrayList<>();
            final String jpql0 = jpql;
            try {
                js.runUserActionTask( (CompilationController controller) -> {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    EntityClassScopeProvider provider1 = (EntityClassScopeProvider) project.getLookup().lookup(EntityClassScopeProvider.class);
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
                            //////////////////////
                            DefaultJPQLQueryHelper  helper = new DefaultJPQLQueryHelper (DefaultJPQLGrammar.instance());
                            helper.setQuery(new org.netbeans.modules.j2ee.persistence.spi.jpql.Query(null, jpql0, mtp));
                            
                            try {
                                problems.addAll(helper.validate());
                            } catch (Exception ex) {
                            }
                            /////////////////////
                            return null;
                        });
                    }
                }, false);
            } catch (IOException ex) {
            } finally {
                if(transaction != null){
                    transaction.isActive();
                    transaction.rollback();
                }
                if(em != null) {
                    em.clear();
                    em.close();
                }
                if(emf != null) {
                    emf.close();
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
        return result;
    }
}
