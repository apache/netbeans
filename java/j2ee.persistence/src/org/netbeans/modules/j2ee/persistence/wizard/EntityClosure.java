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

package org.netbeans.modules.j2ee.persistence.wizard;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper;
import org.netbeans.modules.j2ee.persistence.util.MetadataModelReadHelper.State;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Pavel Buzek
 */
public class EntityClosure {
    private static final Logger LOG = Logger.getLogger(EntityClosure.class.getName());

    // XXX this class needs a complete rewrite: the computing of the available 
    // entity classes and of the referenced classes need to be moved away.
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    private Set<Entity> availableEntityInstances = new HashSet<>();
    private Set<String> availableEntities = new HashSet<>();
    private Set<String> wantedEntities = new HashSet<>();
    private Set<String> selectedEntities = new HashSet<>();
    private Set<String> referencedEntities = new HashSet<>();
    private HashMap<String,Entity> fqnEntityMap = new HashMap<>();
    private HashMap<String,Boolean> fqnIdExistMap = new HashMap<>();
    private boolean modelReady;
    private boolean ejbModuleInvolved = false;
    private boolean closureEnabled = true;
    private Project project;
    
    private final MetadataModelReadHelper<EntityMappingsMetadata, List<Entity>> readHelper;
    
    private final MetadataModel<EntityMappingsMetadata> model;
    
    public static EntityClosure create(EntityClassScope entityClassScope, Project project) {
        EntityClosure ec = new EntityClosure(entityClassScope, project);
        ec.initialize();
        return ec;
    }

    public static ComboBoxModel getAsComboModel(EntityClosure ec) {
        return new EntityClosureComboBoxModel(ec);
    }
    
    private EntityClosure(EntityClassScope entityClassScope, Project project) {
        this.model = entityClassScope.getEntityMappingsModel(true);
        this.project = project;
        readHelper = MetadataModelReadHelper.create(model, (EntityMappingsMetadata metadata) -> 
                Arrays.<Entity>asList(metadata.getRoot().getEntity()) );
    }
    
    private void initialize() {
        readHelper.addChangeListener( (ChangeEvent e) -> {
            if (readHelper.getState() == State.FINISHED) {
                SwingUtilities.invokeLater( () -> {
                    try {
                        addAvaliableEntities(new HashSet<Entity>(readHelper.getResult()));
                        modelReady = true;
                        changeSupport.fireChange();
                    } catch (ExecutionException e1) {
                        Exceptions.printStackTrace(e1);
                    }
                });
            }
        });
        readHelper.start();
    }
    
    public void addAvaliableEntities(final Set<Entity> entities) {
        availableEntityInstances.addAll(entities);
        
        JavaSource source = null;
        try {
            source = model.runReadAction( (EntityMappingsMetadata metadata) -> metadata.createJavaSource() );
        } catch (MetadataModelException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        if( source!=null ) {
            try {
                ClassPath classPath = source.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                final Set<Project> ejbProjects = getEjbModulesOfClasspath(classPath);
                source.runUserActionTask( (CompilationController parameter) -> {
                    for (Entity en : entities) {
                        availableEntities.add(en.getClass2());
                        fqnEntityMap.put(en.getClass2(), en);
                        PersistentObject po = (PersistentObject) en;
                        ElementHandle<TypeElement> teh = po.getTypeElementHandle();
                        TypeElement te = teh.resolve(parameter);
                        fqnIdExistMap.put(en.getClass2(), JpaControllerUtil.haveId(te));
                        
                        // issue #219565 - troubles of EJB's enities CP visibility
                        FileObject file = SourceUtils.getFile(teh, parameter.getClasspathInfo());
                        if (isEjbProjectEntity(ejbProjects, file)) {
                            ejbModuleInvolved = true;
                            LOG.log(Level.INFO, "Entity came from EJB module and mustn''t be visible on CP, entity={0}", en.getClass2());
                        }
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        availableEntities.removeAll(selectedEntities);
        changeSupport.fireChange();
    }

    private boolean isEjbProjectEntity(Set<Project> ejbProjects, FileObject fo) {
        for (Project relatedProject : ejbProjects) {
            if (fo.getPath().startsWith(relatedProject.getProjectDirectory().getPath())) {
                return true;
            }
        }
        return false;
    }
    
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    public Set<String> getAvailableEntities() {
        return availableEntities;
    }

    public Set<Entity> getAvailableEntityInstances() {
        return availableEntityInstances;
    }
    
    public Set<String> getWantedEntities() {
        return wantedEntities;
    }
    
    public Set<String> getSelectedEntities() {
        return selectedEntities;
    }

    public boolean isEjbModuleInvolved() {
        return ejbModuleInvolved;
    }
    
    public void addEntities(Set<String> entities) {
        if (isClosureEnabled()) {
            if (wantedEntities.addAll(entities)) {
                try{
                    Set<String> refEntities = getReferencedEntitiesTransitively(entities);
                    Set<String> addedEntities = new HashSet<>(entities);
                    addedEntities.addAll(refEntities);
                    
                    selectedEntities.addAll(addedEntities);
                    referencedEntities.addAll(refEntities);
                    availableEntities.removeAll(addedEntities);
                    
                    changeSupport.fireChange();
                } catch (IOException ioe){
                    Exceptions.printStackTrace(ioe);
                }
            }
        } else {
            wantedEntities.addAll(entities);
            selectedEntities.addAll(entities);
            availableEntities.removeAll(entities);
            
            changeSupport.fireChange();
        }
    }
    
    public void removeEntities(Set<String> entities) {
        if (isClosureEnabled()) {
            if (wantedEntities.removeAll(entities)) {
                redoClosure();
                
                changeSupport.fireChange();
            }
        } else {
            wantedEntities.removeAll(entities);
            selectedEntities.removeAll(entities);
            availableEntities.addAll(entities);
            
            changeSupport.fireChange();
        }
    }
    
    public void addAllEntities() {
        wantedEntities.addAll(availableEntities);
        
        if (isClosureEnabled()) {
            redoClosure();
            
            changeSupport.fireChange();
        } else {
            selectedEntities.addAll(wantedEntities);
            availableEntities.clear();
            
            changeSupport.fireChange();
        }
    }
    
    public void removeAllEntities() {
        availableEntities.addAll(selectedEntities);
        wantedEntities.clear();
        selectedEntities.clear();
        referencedEntities.clear();
        
        changeSupport.fireChange();
    }
    
    
    /**
     * Returns the tables transitively referenced by the contents of the tables parameter
     * (not including tables passed in this parameter). If a table references itself,
     * it is not added to the result.
     */
    private Set<String> getReferencedEntitiesTransitively(Set<String> entities) throws IOException {
        
        Queue<String> entityQueue = new Queue<>(entities);
        Set<String> refEntities = new HashSet<>();
        
        while (!entityQueue.isEmpty()) {
            String entity = entityQueue.poll();
            
            Set<String> referenced = getReferencedEntities(entity);
            for (String refEntity : referenced) {
                
                if (!refEntity.equals(entity)) {
                    refEntities.add(refEntity);
                }
                entityQueue.offer(refEntity);
            }
        }
        
        return refEntities;
    }
    
    private Set<String> getReferencedEntities(final String entityClass) throws IOException {
        
        if (readHelper.getState() != State.FINISHED) {
            return Collections.emptySet();
        }

        JavaSource source = model.runReadAction( (EntityMappingsMetadata metadata) -> metadata.createJavaSource() );
                
        final Set<String> result = new HashSet<>();

        source.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController parameter) throws Exception {
                List<Entity> entities = readHelper.getResult();
                Set<String> entitiesFqn = new HashSet<>();
                for( Entity entity : entities){
                    entitiesFqn.add( entity.getClass2());  
                }
                
                TypeElement entity = parameter.getElements().getTypeElement(entityClass);
                for (Element element : parameter.getElements().getAllMembers(entity)){
                    if (ElementKind.METHOD == element.getKind()) {
                        ExecutableType methodType = (ExecutableType)parameter.getTypes().
                                asMemberOf((DeclaredType)entity.asType(), element);
                         TypeMirror returnType = methodType.getReturnType();
                         addTypeMirror(result, parameter, returnType, entitiesFqn);
                         List<? extends TypeMirror> parameterTypes = 
                             methodType.getParameterTypes();
                         for (TypeMirror paramType : parameterTypes) {
                             addTypeMirror(result, parameter, paramType, entitiesFqn);
                        }
                    }
                    else if  (ElementKind.FIELD == element.getKind()) {
                        TypeMirror typeMirror = parameter.getTypes().
                                asMemberOf((DeclaredType)entity.asType(), element);
                        addTypeMirror(result, parameter, typeMirror, entitiesFqn );
                    }
                }
            }

            private void addTypeMirror( final Set<String> result,
                    CompilationController parameter, TypeMirror typeMirror , 
                    Set<String> allEntitiesFqn ) throws Exception
            {
                Element typeElement = parameter.getTypes().asElement(
                        typeMirror );
                if ( typeElement instanceof TypeElement ){
                    String fqn = ((TypeElement)typeElement).getQualifiedName().toString();
                    if ( allEntitiesFqn.contains( fqn )){
                        result.add( fqn );
                    }
                }
            }
        }, true);
        
        return result;
    }

    private void redoClosure() {
        Set<String> allEntities = new HashSet<>(availableEntities);
        allEntities.addAll(selectedEntities);
        
        referencedEntities.clear();
        try{
            referencedEntities.addAll(getReferencedEntitiesTransitively(wantedEntities));
        }catch (IOException ioe){
            Exceptions.printStackTrace(ioe);
        }
        
        
        selectedEntities.clear();
        selectedEntities.addAll(wantedEntities);
        selectedEntities.addAll(referencedEntities);
        
        availableEntities.clear();
        availableEntities.addAll(allEntities);
        availableEntities.removeAll(selectedEntities);
    }
    
    public boolean isClosureEnabled() {
        return closureEnabled;
    }
    
    public void setClosureEnabled(boolean closureEnabled) {
        if (this.closureEnabled == closureEnabled) {
            return;
        }
        this.closureEnabled = closureEnabled;
        if (closureEnabled) {
            redoClosure();
        } else {
            Set<String> allEntities = new HashSet<>(availableEntities);
            allEntities.addAll(selectedEntities);
            
            referencedEntities.clear();
            
            selectedEntities.clear();
            selectedEntities.addAll(wantedEntities);
            
            availableEntities.clear();
            availableEntities.addAll(allEntities);
            availableEntities.removeAll(selectedEntities);
        }
        changeSupport.fireChange();
    }
    
    public boolean isModelReady() {
        return modelReady;
    }
    
    void waitModelIsReady(){
        try {
            Future result = model.runReadActionWhenReady( (EntityMappingsMetadata metadata) -> true );
            result.get();
        } catch (InterruptedException | ExecutionException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }
    
    private boolean isFieldAccess(final String entity) throws MetadataModelException, IOException {
        Boolean result = model.runReadAction( (EntityMappingsMetadata metadata) -> {
            for (Entity e : metadata.getRoot().getEntity()){
                if (e.getClass2().equals(entity)){
                    return e.getAccess().equals(Entity.FIELD_ACCESS);
                }
            }
            return false;
        });
        
        return result;
    }

    Boolean haveId(String entityFqn) {
        return fqnIdExistMap.get(entityFqn);
    }

    Entity getEntity(String entityFqn){
        return fqnEntityMap.get(entityFqn);
    }

    private Set<Project> getEjbModulesOfClasspath(ClassPath classPath) {
        Set<Project> ejbProjects = new HashSet<>();
        for (FileObject fileObject : classPath.getRoots()) {
            Project rootOwner = FileOwnerQuery.getOwner(fileObject);
            if (rootOwner != null) {
                if (Util.isEjbModule(rootOwner)) {
                    ejbProjects.add(rootOwner);
                }
            }
        }
        return ejbProjects;
    }
    
    /**
     * A simple queue. An object can only be added once, even
     * if it has already been removed from the queue. This class could implement
     * the {@link java.util.Queue} interface, but it doesn't because that
     * interface has too many unneeded methods. Not private because of the tests.
     */
    static final class Queue<T> {
        
        /**
         * The queue. Implemented as ArrayList since will be iterated using get().
         */
        private final List<T> queue;
        
        /**
         * The contents of the queue, needed in order to quickly (ideally
         * in a constant time) tell if a table has been already added.
         */
        private final Set<T> contents;
        
        /**
         * The position in the queue.
         */
        private int currentIndex;
        
        /**
         * Creates a queue with an initial contents.
         */
        public Queue(Set<T> initialContents) {
            assert !initialContents.contains(null);
            
            queue = new ArrayList(initialContents);
            contents = new HashSet(initialContents);
        }
        
        /**
         * Adds an elements to the queue if it hasn't been already added.
         */
        public void offer(T element) {
            assert element != null;
            
            if (!contents.contains(element)) {
                contents.add(element);
                queue.add(element);
            }
        }
        
        /**
         * Returns the element at the top of the queue without removing it or null
         * if the queue is empty.
         */
        public boolean isEmpty() {
            return currentIndex >= queue.size();
        }
        
        /**
         * Returns and removes the elements at the top of the queue or null if
         * the queue is empty.
         */
        public T poll() {
            T result = null;
            if (!isEmpty()) {
                result = queue.get(currentIndex);
                currentIndex++;
            }
            return result;
        }
    }

    private static class EntityClosureComboBoxModel extends DefaultComboBoxModel implements ChangeListener {

        private EntityClosure entityClosure;
        private List<String> entities = new ArrayList<>();

        EntityClosureComboBoxModel(EntityClosure entityClosure) {
            this.entityClosure = entityClosure;
            entityClosure.addChangeListener(this);
            refresh();
        }

        @Override
        public int getSize() {
            return entities.size();
        }

        @Override
        public Object getElementAt(int index) {
            return entities.get(index);
        }

        /**
         * @return the fully qualified names of the entities in this model.
         */
        public List<String> getEntityClasses() {
            return entities;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh();
        }

        private void refresh() {
            int oldSize = getSize();
            entities = new ArrayList<String>(entityClosure.getAvailableEntities());
            Collections.sort(entities);
            fireContentsChanged(this, 0, Math.max(oldSize, getSize()));
        }
    }


}
