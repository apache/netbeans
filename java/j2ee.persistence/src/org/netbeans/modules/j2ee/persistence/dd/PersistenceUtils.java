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

package org.netbeans.modules.j2ee.persistence.dd;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappings;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Marek Fukala, Andrei Badea
 */
public class PersistenceUtils {

    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.j2ee.persistence"); // NOI18N
    private static final Logger LOG = Logger.getLogger(PersistenceUtils.class.getName());

    public static EntityMappings getEntityMappings(FileObject documentFO) {
        Project project = FileOwnerQuery.getOwner(documentFO);
        if (project == null) {
            return null;
        }
        EntityClassScope entityClassScope = EntityClassScope.getEntityClassScope(project.getProjectDirectory());
        if(entityClassScope == null){
            return null;
        }
        MetadataModel<EntityMappingsMetadata> model = entityClassScope.getEntityMappingsModel(true);
        EntityMappings mappings = null;
        try {
            mappings = model.runReadAction( (EntityMappingsMetadata metadata) -> metadata.getRoot() );
        } catch (MetadataModelException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return mappings;
    }
    
    // TODO multiple mapping files
    
    private PersistenceUtils() {
    }
    
    /**
     * Returns the persistence unit(s) the given entity class belongs to. Since
     * an entity class can belong to any persistence unit, this returns all
     * persistence units in all persistence.xml files in the project which owns
     * the given entity class.
     *
     * @return an array of PersistenceUnit's; never null.
     * @throws NullPointerException if <code>sourceFile</code> is null.
     */
    public static PersistenceUnit[] getPersistenceUnits(FileObject sourceFile) throws IOException {
        if (sourceFile == null) {
            throw new NullPointerException("The sourceFile parameter cannot be null"); // NOI18N
        }
        
        Project project = FileOwnerQuery.getOwner(sourceFile);
        if (project == null) {
            return new PersistenceUnit[0];
        }
        
        List<PersistenceUnit> result = new ArrayList<>();
        ClassPath cp = ClassPath.getClassPath(sourceFile, ClassPath.SOURCE);
        for (PersistenceScope persistenceScope : getPersistenceScopes(project, cp != null ? cp.findOwnerRoot(sourceFile) : null)) {
            Persistence persistence = null;
            try{
                persistence = PersistenceMetadata.getDefault().getRoot(persistenceScope.getPersistenceXml());
            } catch (RuntimeException ex) {// must catch RTE (thrown by schema2beans when document is not valid)
                LOG.log(Level.INFO, null, ex);
            }
            if(persistence != null) {
                result.addAll(Arrays.asList(persistence.getPersistenceUnit()));
            }
        }
        
        return result.toArray(new PersistenceUnit[0]);
    }
    
    /**
     * Searches the given entity mappings for the specified entity class.
     *
     * @param  className the Java class to search for.
     * @param  entityMappings the entity mappings to be searched.
     * @return the entity class or null if it could not be found.
     * @throws NullPointerException if <code>className</code> or
     *         <code>entityMappings</code> were null.
     */
    public static Entity getEntity(String className, EntityMappings entityMappings) {
        if (className == null) {
            throw new NullPointerException("The javaClass parameter cannot be null"); // NOI18N
        }
        if (entityMappings == null) {
            throw new NullPointerException("The entityMappings parameter cannot be null"); // NOI18N
        }
        
        for (Entity entity : entityMappings.getEntity()) {
            if (className.equals(entity.getClass2())) {
                return entity;
            }
        }
        return null;
    }
    
    /**
     * Returns an array containing all persistence scopes provided by the
     * given project. This is just an utility method which does:
     *
     * <pre>
     * PersistenceScopes.getPersistenceScopes(project).getPersistenceScopes();
     * </pre>
     *
     * <p>with all the necessary checks for null (returning an empty
     * array in this case).</p>
     *
     * @param  project the project to retrieve the persistence scopes from.
     * @return the list of persistence scopes provided by <code>project</code>;
     *         or an empty array if the project provides no persistence
     *         scopes; never null.
     * @throws NullPointerException if <code>project</code> was null.
     */
    public static PersistenceScope[] getPersistenceScopes(Project project) {
        return getPersistenceScopes(project, null);
    }

    /**
     * Returns an array containing all persistence scopes provided by the
     * given project associated with the given FileObject. This is just an utility method which does:
     *
     * <pre>
     * PersistenceScopes.getPersistenceScopes(project, fo).getPersistenceScopes();
     * </pre>
     *
     * <p>with all the necessary checks for null (returning an empty
     * array in this case).</p>
     *
     * @param  project the project to retrieve the persistence scopes from.
     * @param  fo the FileObject.
     * @return the list of persistence scopes provided by <code>project</code>;
     *         or an empty array if the project provides no persistence
     *         scopes for the given FileObject; never null.
     * @throws NullPointerException if <code>project</code> was null.
     * @since 1.55
     */
    public static PersistenceScope[] getPersistenceScopes(Project project, FileObject fo) {
        if (project == null) {
            throw new NullPointerException("The project parameter cannot be null"); // NOI18N
        }
        
        PersistenceScopes persistenceScopes = PersistenceScopes.getPersistenceScopes(project, fo);
        if (persistenceScopes != null) {
            return persistenceScopes.getPersistenceScopes();
        }
        return new PersistenceScope[0];
    }

    /**
     * method check target compile classpath for presence of persitence classes of certain version
     * returns max supported specification
     * @param project
     * @return
     */
    public static String getJPAVersion(Project target)
    {
        String version=null;
        Sources sources=ProjectUtils.getSources(target);
        SourceGroup groups[]=sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        SourceGroup firstGroup=groups[0];
        FileObject fo=firstGroup.getRootFolder();
        ClassPath compile=ClassPath.getClassPath(fo, ClassPath.COMPILE);
        if(compile.findResource("jakarta/persistence/criteria/CriteriaSelect.class")!=null) {
            version=Persistence.VERSION_3_2;
        } else if(compile.findResource("jakarta/persistence/spi/TransformerException.class")!=null) {
            version=Persistence.VERSION_3_1;
        } else if(compile.findResource("jakarta/persistence/Entity.class")!=null) {
            version=Persistence.VERSION_3_0;
        } else if(compile.findResource("javax/persistence/TableGenerators.class")!=null) {
            version=Persistence.VERSION_2_2;
        } else if(compile.findResource("javax/persistence/criteria/CriteriaUpdate.class")!=null) {
            version=Persistence.VERSION_2_1;
        } else if(compile.findResource("javax/persistence/criteria/JoinType.class")!=null) {
            version=Persistence.VERSION_2_0;
        } else if(compile.findResource("javax/persistence/Entity.class")!=null) {
            version=Persistence.VERSION_1_0;
        }
        return version;
    }

    public static String getJPAVersion(Library lib) {
        List<URL> roots=lib.getContent("classpath");
        ClassPath cp = ClassPathSupport.createClassPath(roots.toArray(new URL[0]));
        String version=null;
        if(cp.findResource("jakarta/persistence/criteria/CriteriaSelect.class")!=null) {
            version=Persistence.VERSION_3_2;
        } else if(cp.findResource("jakarta/persistence/spi/TransformerException.class")!=null) {
            version=Persistence.VERSION_3_1;
        } else if(cp.findResource("jakarta/persistence/Entity.class")!=null) {
            version=Persistence.VERSION_3_0;
        } else if(cp.findResource("javax/persistence/TableGenerators.class")!=null) {
            version=Persistence.VERSION_2_2;
        } else if(cp.findResource("javax/persistence/criteria/CriteriaUpdate.class")!=null) {
            version=Persistence.VERSION_2_1;
        } else if(cp.findResource("javax/persistence/criteria/JoinType.class")!=null) {
            version=Persistence.VERSION_2_0;
        } else if(cp.findResource("javax/persistence/Entity.class")!=null) {
            version=Persistence.VERSION_1_0;
        }
        return version;
    }

        /**
     * Logs feature usage.
     *
     * @param srcClass source class
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUsage(Class srcClass, String message, Object[] params) {
        Parameters.notNull("message", message);

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(USG_LOGGER.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        if (params != null) {
            logRecord.setParameters(params);
        }
        USG_LOGGER.log(logRecord);
    }
}
