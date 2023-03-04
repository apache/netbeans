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
package org.netbeans.modules.refactoring.java.api;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/**
 * Replaces the type usages in a project with those
 * of the super type, where applicable
 * @author Bharath Ravi Kumar
 */
public final class UseSuperTypeRefactoring extends AbstractRefactoring{
    
    private final TreePathHandle javaClassHandle;
    private ElementHandle<TypeElement> superType;
    
    //Forced to create an array since the ComboBoxModel (for the panel)
    //takes only a vector or an array.
    private ElementHandle<TypeElement>[] candidateSuperTypes;
    
    /**
     * Creates a new instance of UseSuperTypeRefactoring
     * @param javaClassHandle  The class whose occurences must be replaced by
     * that of it's supertype
     */
    public UseSuperTypeRefactoring(TreePathHandle javaClassHandle) {
        super(Lookups.fixed(javaClassHandle));
        this.javaClassHandle = javaClassHandle;
        deriveSuperTypes(javaClassHandle);
    }
    
    /**
     * Returns the type whose occurence must be replaced by that of it's supertype.
     * @return The array of elements to be safely deleted
     */
    public TreePathHandle getTypeElement(){
        return javaClassHandle;
    }
    
    /**
     * Sets the SuperType to be used by this refactoring
     * @param superClass The SuperType to be used by this refactoring
     */
    public void setTargetSuperType(ElementHandle<TypeElement> superClass) {
        this.superType = superClass;
    }
    
    /**
     * Returns the SuperType used by this refactoring
     * @return superClass The SuperType used by this refactoring
     */
    public ElementHandle<TypeElement> getTargetSuperType() {
        return this.superType;
    }
    
    /**
     * Returns the possible SuperTypes that could be used for the initial Type
     * @return The list of possible SuperTypes for the current type
     */
    public ElementHandle<TypeElement>[] getCandidateSuperTypes(){
        return candidateSuperTypes;
    }
    
    //private helper methods follow
    
    private void deriveSuperTypes(final TreePathHandle javaClassHandle) {
        
        
        JavaSource javaSrc = JavaSource.forFileObject(javaClassHandle.
                getFileObject());
        try{
            javaSrc.runUserActionTask(new CancellableTask<CompilationController>() {
                
                @Override
                public void cancel() {
                }
                
                @Override
                public void run(CompilationController complController) throws IOException {
                    
                    complController.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement javaClassElement = (TypeElement) 
                            javaClassHandle.resolveElement(complController);
                    candidateSuperTypes = deduceSuperTypes(javaClassElement, 
                            complController);
                }
            }, false);
        }catch(IOException ioex){
            ioex.printStackTrace();
        }
    }
    
    //    --private helper methods follow--
    
    private ElementHandle[] deduceSuperTypes(TypeElement subTypeElement, 
            CompilationController compCtlr){

        TypeMirror subtypeMirror = subTypeElement.asType();
        Types types = compCtlr.getTypes();
        Comparator<TypeMirror> comparator = new TypeMirrorComparator();
        //TODO:The working set (workingTypeMirrors) doesn't have to be a TreeSet. 
        //Why unnecessarily do the additional work of ordering in an intermediate Set?
        TreeSet<TypeMirror> finalSuperTypeMirrors = new TreeSet<TypeMirror>(comparator);
        TreeSet<TypeMirror> workingTypeMirrors = new TreeSet<TypeMirror>(comparator);
        workingTypeMirrors.add(subtypeMirror);
        getAllSuperIFs(subtypeMirror, workingTypeMirrors, finalSuperTypeMirrors,
                compCtlr);
        ElementHandle[] superTypeHandles = new ElementHandle[finalSuperTypeMirrors.size()];
        int index = 0;
        for (Iterator<TypeMirror> it = finalSuperTypeMirrors.iterator(); it.hasNext();) {
            TypeMirror typeMirror = it.next();
            superTypeHandles[index++] = ElementHandle.create(types.asElement(typeMirror));
        }
        return superTypeHandles;

    }    

    private void getAllSuperIFs(TypeMirror subTypeMirror,
            Collection<TypeMirror> uniqueIFs, Collection<TypeMirror> finalIFCollection,
            CompilationController compCtlr){
        Types types = compCtlr.getTypes();
        Iterator<? extends TypeMirror> subTypeIFs = types.directSupertypes(subTypeMirror).
                iterator();
        while(subTypeIFs.hasNext()){
            TypeMirror superType = subTypeIFs.next();
            finalIFCollection.add(superType);
            if(!uniqueIFs.contains(superType)){
                getAllSuperIFs(superType, uniqueIFs, finalIFCollection, compCtlr);
            }
        }
    }

    //Compares two types alphabetically based on their fully qualified name
    private static class TypeMirrorComparator implements Comparator<TypeMirror>{

        @Override
        public int compare(TypeMirror type1, TypeMirror type2) {
            return type1.toString().compareTo(type2.toString());
        }
        
    }
    
}
