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

package org.netbeans.modules.beans;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import static javax.lang.model.element.Modifier.*;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.openide.util.Exceptions;

import static org.netbeans.modules.beans.BeanUtils.*;
import org.netbeans.modules.beans.TmpPattern.Property;
import org.netbeans.modules.beans.TmpPattern.IdxProperty;
import org.netbeans.modules.beans.TmpPattern.EventSet;
import org.openide.filesystems.FileObject;

/** Analyses the ClassElement trying to find source code patterns i.e.
 * properties or event sets;
 * 
 * @author Petr Hrebejk
 */

public final class PatternAnalyser {

    private BeanPanelUI ui;

    private FileObject fileObject;
    
    // Enclosing class
    private ElementHandle<TypeElement> classElementHandle;
   
    // Classes
    private ArrayList<ClassPattern> currentClassesPatterns = new ArrayList<ClassPattern>();
    // Properties
    private ArrayList<PropertyPattern> currentPropertyPatterns = new ArrayList<PropertyPattern>();
    // Indexed properties
    private ArrayList<IdxPropertyPattern> currentIdxPropertyPatterns = new ArrayList<IdxPropertyPattern>();
    // Event sets
    private ArrayList<EventSetPattern> currentEventSetPatterns =  new ArrayList<EventSetPattern>();

    final AtomicBoolean canceled = new AtomicBoolean();
    /** deep introspection analyzes also super classes; useful for bean info*/
    private boolean deepIntrospection = false;

    static final Logger LOG = Logger.getLogger(PatternAnalyser.class.getName());

    public PatternAnalyser( FileObject fileObject, BeanPanelUI ui ) {
        this(fileObject, ui, false);
    }

    public PatternAnalyser( FileObject fileObject, BeanPanelUI ui, boolean deepIntrospection ) {
        this.fileObject = fileObject;
        this.ui = ui;
        this.deepIntrospection = deepIntrospection;
    }
    
    public PatternAnalyser(FileObject fileObject, BeanPanelUI ui, Collection<ClassPattern> classes) {
        this(fileObject, ui);
        this.currentClassesPatterns.addAll( classes );
    }

    public List <PropertyPattern> getPropertyPatterns() {
        return currentPropertyPatterns;
    }

    public List<IdxPropertyPattern> getIdxPropertyPatterns() {
        return currentIdxPropertyPatterns;
    }

    public List<EventSetPattern> getEventSetPatterns() {
        return currentEventSetPatterns;
    }

    // XXX Sorting
    public List<Pattern> getPatterns() {
        List<Pattern> patterns = new ArrayList<Pattern>( 
            currentClassesPatterns.size() +     
            currentEventSetPatterns.size() + 
            currentIdxPropertyPatterns.size() +
            currentIdxPropertyPatterns.size() );

       patterns.addAll(currentPropertyPatterns);     
       patterns.addAll(currentIdxPropertyPatterns);     
       patterns.addAll(currentEventSetPatterns);
       patterns.addAll(currentClassesPatterns);     
       
       return patterns;
    }

    /** Gets the classelemnt of this pattern analyser */
    public ElementHandle<TypeElement> getClassElementHandle() {
        return classElementHandle;
    }

    public BeanPanelUI getUI() {
        return ui;
    }

    public FileObject getFileObject() {
        return fileObject;
    }
    
    /** Analyzes given element and returns the PatterAnalyzer containing the
     * result.
     * 
     * XXX Make it really cancelable.
     * 
     */
    public void analyzeAll(CompilationInfo ci, TypeElement element) {

        if ( isCanceled() ) {
            return;
        }
        
        Parameters p = new Parameters( ci, element );
        this.classElementHandle = ElementHandle.create(element);
        
        // Analyse patterns
        resolveMethods( p );
        resolveFields( p );
        
        // Now analyze innerclasses
        resolveTypes( p );

        // Create the real patterns // Compare old and new patterns to resolve changes
        
        try {
            resolveChangesOfProperties(p);
            resolveChangesOfIdxProperties(p);
            resolveChangesOfEventSets(p);
        }
        catch (IntrospectionException e ) {
            Exceptions.printStackTrace(e);
        }

    }
    
    private boolean isCanceled() {
        synchronized ( canceled ) {
            return canceled.get();
        }
    }

    public void cancel() {
        synchronized ( canceled ) {
            canceled.set( true );
        }
    }

    // Private methods ---------------------------------------------------------

    
    private void resolveTypes(Parameters p) {
        
        List<TypeElement> types = ElementFilter.typesIn(p.element.getEnclosedElements());
        
        for (TypeElement typeElement : types) {
            if ( typeElement.getKind() == ElementKind.CLASS ||
                 typeElement.getKind() == ElementKind.INTERFACE ) {
                PatternAnalyser pa = new PatternAnalyser( p.ci.getFileObject(), ui );
                pa.analyzeAll(p.ci, typeElement);
                ClassPattern cp = new ClassPattern(pa, typeElement.asType(), 
                                                   BeanUtils.nameAsString(typeElement));
                currentClassesPatterns.add(cp);
            }
        }

        
    }

    /** This method analyses the ClassElement for "property patterns".
    * The method is analogous to JavaBean Introspector methods for classes
    * without a BeanInfo.
    */
    private void resolveMethods(Parameters p) {

        // First get all methods in classElement
        List<? extends ExecutableElement> methods = methodsIn(p.element, p.ci);
                
        // Temporary structures for analysing EventSets
        Map<String,ExecutableElement> adds = new HashMap<String, ExecutableElement>();
        Map<String,ExecutableElement> removes = new HashMap<String, ExecutableElement>();

        // Analyze each method
        for ( ExecutableElement method : methods ) {            
            if ( !method.getModifiers().contains(Modifier.PUBLIC) ) {
                continue;
            }

            String name = nameAsString(method);
            int len = name.length();

            if ( (name.startsWith(GET_PREFIX) && len>GET_PREFIX.length())
              || (name.startsWith(SET_PREFIX) && len>SET_PREFIX.length())
              || (name.startsWith(IS_PREFIX) && len>IS_PREFIX.length()) ) {
                Property pp = analyseMethodForProperties( p, method );
                if ( pp != null )
                    addProperty( p, pp );
            }
            if ( (name.startsWith(ADD_PREFIX) && len>ADD_PREFIX.length()) 
              || (name.startsWith(REMOVE_PREFIX) && len>REMOVE_PREFIX.length()) )  {
                analyseMethodForEventSets( p, method, adds, removes );
            }
        }
        // Resolve the temporay structures of event sets

        // Now look for matching addFooListener+removeFooListener pairs.
        for( String compound : adds.keySet() ) {
            // Skip any "add" which doesn't have a matching remove // NOI18N
            if (removes.get(compound) == null ) {
                continue;
            }
            // Method name has to end in Listener
            if (compound.indexOf( "Listener:" ) <= 0 ) { // NOI18N
                continue;
            }

            ExecutableElement addMethod = adds.get(compound);
            ExecutableElement removeMethod = removes.get(compound);
            
            List<? extends VariableElement> params = addMethod.getParameters();            
            TypeMirror argType = params.get(0).asType();

            // Check if the argument is a subtype of EventListener
            if ( argType.getKind() != TypeKind.DECLARED || // filter out primitive types + arrays
                 !p.ci.getTypes().isSubtype(argType, p.ci.getElements().getTypeElement("java.util.EventListener").asType() ) ) {
                continue;
            }
            EventSet esp = new EventSet( p.ci, addMethod, removeMethod );
            addEventSet( p, esp );
        }
    }

    private List<? extends ExecutableElement> methodsIn(TypeElement clazz, CompilationInfo javac) {
        return deepIntrospection
                ? BeanUtils.methodsIn(clazz, javac)
                : ElementFilter.methodsIn(clazz.getEnclosedElements());
    }

    private void resolveFields(Parameters p) {
        
        // Analyze fields
        List<VariableElement> fields = ElementFilter.fieldsIn(p.element.getEnclosedElements());
        
        String propertyStyle = "this."; //NOI18N
        
        for ( VariableElement field : fields ) {
            
            if ( field.getModifiers().contains(STATIC)) {
                continue;
            }
            
            //System.out.println("Property style " + propertyStyle);   
            String fieldName = nameAsString(field);
            //System.out.println("Field name1 " + fieldName);
            if( fieldName.startsWith(propertyStyle) ){
                fieldName = fieldName.substring(1);
                //System.out.println("Field name2 " + fieldName);
            }
            
            Property pp = p.propertyPatterns.get( fieldName );
            if ( pp == null )
                pp = p.idxPropertyPatterns.get( fieldName );
            if ( pp == null )
                continue;
            TypeMirror ppType = pp.type;
            if ( ppType != null && ppType.equals( field.asType() ) )
                pp.estimatedField = field;
        }
    }

    /** Analyses one method for property charcteristics */
    Property analyseMethodForProperties( Parameters p, ExecutableElement method ) {
        // Skip static methods as Introspector does.
        
        if( method.getModifiers().contains(STATIC) ) {
            return null;
        }
        
        String name = nameAsString(method);
        VariableElement[] params = method.getParameters().toArray(new VariableElement[0]);
        TypeMirror returnType = method.getReturnType();

        Property pp = null;

        try {
            if ( params.length == 0 ) {
                if (name.startsWith( GET_PREFIX )) {
                    // SimpleGetter
                    pp = new Property( p.ci, method, null );
                }
                else if ( returnType.getKind() == TypeKind.BOOLEAN && name.startsWith( IS_PREFIX )) {
                    // Boolean getter
                    pp = new Property( p.ci, method, null );
                }
            }
            else if ( params.length == 1 ) {
                if ( params[0].asType().getKind() == TypeKind.INT) {
                    if(name.startsWith( GET_PREFIX )) {
                        pp = new IdxProperty( p.ci, null, null, method, null );
                    } else if ( returnType.getKind() == TypeKind.BOOLEAN && name.startsWith( IS_PREFIX )) {
                        pp = new IdxProperty( p.ci, null, null, method, null );
                    } else if (returnType.getKind() == TypeKind.VOID && name.startsWith(SET_PREFIX)) {
                        pp = new Property(p.ci, null, method);
                        // PENDING vetoable => constrained
                    }
                }
                else if ( returnType.getKind() == TypeKind.VOID && name.startsWith( SET_PREFIX )) {
                    pp = new Property( p.ci, null, method );
                    // PENDING vetoable => constrained
                }
            }
            else if ( params.length == 2 ) {
                if ( params[0].asType().getKind() == TypeKind.INT && name.startsWith( SET_PREFIX )) {
                    pp = new IdxProperty( p.ci, null, null, null, method );
                    // PENDING vetoable => constrained
                }
            }
        }
        catch (IntrospectionException ex) {
            // PropertyPattern constructor found some differencies from design patterns.
            LOG.log(Level.INFO, ex.getMessage(), ex);
            pp = null;
        }

        return pp;
    }

    /** Method analyses class methods for EventSetPatterns
     */
    void analyseMethodForEventSets( Parameters p, ExecutableElement method, 
                                    Map<String,ExecutableElement> adds, 
                                    Map<String,ExecutableElement> removes ) {
        // Skip static methods
        if( method.getModifiers().contains(STATIC) ) {        
            return;
        }

        String name = nameAsString(method);
        VariableElement params[] = method.getParameters().toArray(new VariableElement[0]);
        TypeMirror returnType = method.getReturnType();

        if ( params.length == 1 && returnType.getKind() == TypeKind.VOID ) {
            TypeMirror paramType = params[0].asType();
            if (paramType.getKind() == TypeKind.DECLARED ) {
                Element lsnrType = p.ci.getTypes().asElement(paramType);
                String lsnrTypeName = nameAsString(lsnrType);
                if (name.startsWith(ADD_PREFIX) && name.substring(3).equals(lsnrTypeName)) {
                    String compound = name.substring(3) + ":" + nameAsString(lsnrType); // NOI18N
                    adds.put( compound, method );
                } else if (name.startsWith(REMOVE_PREFIX) && name.substring(6).equals(lsnrTypeName)) {
                    String compound = name.substring(6) + ":" + nameAsString(lsnrType); // NOI18N
                    removes.put( compound, method );
                }
            }
        }

    }
    // Utility methods --------------------------------------------------------------------

    /** Adds the new property. Or generates composite property if property
     *  of that name already exists. It puts the property in the right HashMep
     * according to type of property idx || not idx
     */
    @SuppressWarnings("unchecked")
    private void addProperty( Parameters p, Property property ) {
        boolean isIndexed = property instanceof IdxProperty;
        HashMap hm = isIndexed ? p.idxPropertyPatterns : p.propertyPatterns;
        String name = property.name;

        Property old = p.propertyPatterns.get(name);
        if ( old == null )
            old = p.idxPropertyPatterns.get(name);

        if (old == null) {  // There is no other property of that name
            hm.put(name, property);
            return;
        }

        // If the property type has changed, use new property pattern
        TypeMirror opt = old.type;
        TypeMirror npt = property.type;
        if (  opt != null && npt != null && !p.ci.getTypes().isSameType(opt, npt) ) {
            hm.put( name, property );
            return;
        }

        boolean isOldIndexed = old instanceof IdxProperty;

        if  (isIndexed || isOldIndexed ) {
            if ( isIndexed && !isOldIndexed ) {
                p.propertyPatterns.remove( old.name ); // Remove old from not indexed
            }
            else if ( !isIndexed && isOldIndexed ) {
                p.idxPropertyPatterns.remove( old.name ); // Remove old from indexed
            }
            IdxProperty composite = new IdxProperty( p.ci, old, property );
            p.idxPropertyPatterns.put( name, composite );
        }
        else {
            Property composite = new Property( p.ci, old, property );
            p.propertyPatterns.put( name, composite );
        }

        // PENDING : Resolve types of getters and setters to pair correctly
        // methods with equalNames.
        /*
        MethodElement getter = pp.getGetterMethod() == null ?
          old.getGetterMethod() : pp.getGetterMethod();
        MethodElement setter = pp.getSetterMethod() == null ?
          old.getSetterMethod() : pp.getSetterMethod();

        PropertyPattern composite = isIndexed ?
          new IdxPropertyPattern ( getter, setter ) :
          new PropertyPattern( getter, setter );
        hm.put( pp.getName(), composite );
        */

    }

    /** adds an eventSetPattern */

    private void addEventSet( Parameters p, EventSet eventSet ) {
        String key = eventSet.name + p.ci.getTypes().asElement(eventSet.type).getSimpleName();
        EventSet old = p.eventSetPatterns.get( key );

        if ( old == null ) {
            p.eventSetPatterns.put( key, eventSet);
            return;
        }

        EventSet composite = new EventSet( old, eventSet );
        p.eventSetPatterns.put( key, composite );
    }

    private static class Parameters {
        
        private CompilationInfo ci;
        private TypeElement element;
        private HashMap<String, Property> propertyPatterns;
        private HashMap<String, IdxProperty> idxPropertyPatterns;
        private HashMap<String, EventSet> eventSetPatterns;
        
        Parameters( CompilationInfo ci, TypeElement element ) {
            this.ci = ci;
            this.element = element;
            propertyPatterns = new HashMap<String, Property>();
            idxPropertyPatterns = new HashMap<String, IdxProperty>();
            eventSetPatterns = new HashMap<String, EventSet>();
        }
        
    }
    
    
    
//    // XXX can be replaced with ClassDefinition.isSubTypeOf()
//    static boolean isSubclass(ClassDefinition a, ClassDefinition b) {
//
//        if (a == null || b == null) {
//            return false;
//        }
//        assert JMIUtils.isInsideTrans();
//        
//        return a.isSubTypeOf(b);
//
//    }

    private void resolveChangesOfProperties( Parameters p ) throws IntrospectionException {
        currentPropertyPatterns = new ArrayList<PropertyPattern>( p.propertyPatterns.size() );
        for( Property property: p.propertyPatterns.values() ) {
            currentPropertyPatterns.add(property.createPattern(this));
        }
        currentPropertyPatterns.sort(Pattern.NAME_COMPARATOR);
        // currentPropertyPatterns = resolveChanges( currentPropertyPatterns, propertyPatterns, LevelComparator.PROPERTY );
    }

    private void resolveChangesOfIdxProperties( Parameters p ) throws IntrospectionException {
        currentIdxPropertyPatterns = new ArrayList<IdxPropertyPattern>( p.idxPropertyPatterns.size() );
        for( IdxProperty property: p.idxPropertyPatterns.values() ) {
            currentIdxPropertyPatterns.add(property.createPattern(this));
        }
        currentIdxPropertyPatterns.sort(Pattern.NAME_COMPARATOR);
        // currentIdxPropertyPatterns = resolveChanges( currentIdxPropertyPatterns, idxPropertyPatterns, LevelComparator.IDX_PROPERTY );
    }

    private void resolveChangesOfEventSets( Parameters p ) {
        currentEventSetPatterns = new ArrayList<EventSetPattern>( p.eventSetPatterns.size() );
        for( EventSet property: p.eventSetPatterns.values() ) {
            currentEventSetPatterns.add(property.createPattern(this));
        }
        currentEventSetPatterns.sort(Pattern.NAME_COMPARATOR);
        // currentEventSetPatterns = resolveChanges( currentEventSetPatterns, eventSetPatterns, LevelComparator.EVENT_SET );
    }
//
//
//    static ArrayList resolveChanges( Collection current, Map created, LevelComparator comparator ) throws JmiException {
//        JMIUtils.isInsideTrans();
//        ArrayList old = new ArrayList( current );
//        ArrayList cre = new ArrayList( created.size() );
//        cre.addAll( created.values() );
//        ArrayList result = new ArrayList( created.size() + 5 );
//
//
//        for ( int level = 0; level <= comparator.getLevels(); level ++ ) {
//            Iterator itCre = cre.iterator();
//            while ( itCre.hasNext() ) {
//                Pattern crePattern = (Pattern) itCre.next();
//                Iterator itOld = old.iterator();
//                while ( itOld.hasNext() ) {
//                    Pattern oldPattern = (Pattern) itOld.next();
//                    if ( comparator.compare( level, oldPattern, crePattern ) ) {
//                        itOld.remove( );
//                        itCre.remove( );
//                        comparator.copyProperties(oldPattern, crePattern );
//                        result.add( oldPattern );
//                        break;
//                    }
//                }
//            }
//        }
//        result.addAll( cre );
//        return result;
//    }
    
    // Inner Classes --- comparators for patterns -------------------------------------------------


//    abstract static class LevelComparator {
//
//        abstract boolean compare( int level, Pattern p1, Pattern p2 );
//        abstract int getLevels();
//        abstract void copyProperties( Pattern p1, Pattern p2 );
//
//        static LevelComparator PROPERTY = new LevelComparator.Property();
//        static LevelComparator IDX_PROPERTY = new LevelComparator.IdxProperty();
//        static LevelComparator EVENT_SET = new LevelComparator.EventSet();
//
//        static class Property extends LevelComparator {
//
//            boolean compare( int level, Pattern p1, Pattern p2 ) {
//
//                switch ( level ) {
//                case 0:
//                    return ((PropertyPattern)p1).getGetterMethod() == ((PropertyPattern)p2).getGetterMethod() &&
//                           ((PropertyPattern)p1).getSetterMethod() == ((PropertyPattern)p2).getSetterMethod() ;
//                case 1:
//                    return ((PropertyPattern)p1).getGetterMethod() == ((PropertyPattern)p2).getGetterMethod();
//                case 2:
//                    return ((PropertyPattern)p1).getSetterMethod() == ((PropertyPattern)p2).getSetterMethod();
//                default:
//                    return false;
//                }
//            }
//
//            int getLevels() {
//                return 2;
//            }
//
//            void copyProperties( Pattern p1, Pattern p2 ) {
//                ((PropertyPattern) p1).copyProperties( (PropertyPattern)p2 );
//            }
//        }
//
//        static class IdxProperty extends LevelComparator {
//
//            boolean compare( int level, Pattern p1, Pattern p2 ) {
//
//                switch ( level ) {
//                case 0:
//                    return ((IdxPropertyPattern)p1).getIndexedGetterMethod() == ((IdxPropertyPattern)p2).getIndexedGetterMethod() &&
//                           ((IdxPropertyPattern)p1).getIndexedSetterMethod() == ((IdxPropertyPattern)p2).getIndexedSetterMethod() ;
//                case 1:
//                    return ((IdxPropertyPattern)p1).getIndexedGetterMethod() == ((IdxPropertyPattern)p2).getIndexedGetterMethod();
//                case 2:
//                    return ((IdxPropertyPattern)p1).getIndexedSetterMethod() == ((IdxPropertyPattern)p2).getIndexedSetterMethod();
//                default:
//                    return false;
//                }
//            }
//
//            int getLevels() {
//                return 2;
//            }
//
//            void copyProperties( Pattern p1, Pattern p2 ) {
//                ((IdxPropertyPattern) p1).copyProperties( (IdxPropertyPattern)p2 );
//            }
//        }
//
//        static class EventSet extends LevelComparator {
//
//            boolean compare( int level, Pattern p1, Pattern p2 ) {
//
//                switch ( level ) {
//                case 0:
//                    return ((EventSetPattern)p1).getAddListenerMethod() == ((EventSetPattern)p2).getAddListenerMethod() ||
//                           ((EventSetPattern)p1).getRemoveListenerMethod() == ((EventSetPattern)p2).getRemoveListenerMethod() ;
//                    /*
//                    case 1:  
//                      return ((EventSetPattern)p1).getAddListenerMethod() == ((EventSetPattern)p2).getAddListenerMethod();
//                    case 2: 
//                      return ((EventSetPattern)p1).getRemoveListenerMethod() == ((EventSetPattern)p2).getRemoveListenerMethod();
//                    */
//                default:
//                    return false;
//                }
//            }
//
//            int getLevels() {
//                return 0;
//            }
//
//            void copyProperties( Pattern p1, Pattern p2 ) {
//                ((EventSetPattern) p1).copyProperties( (EventSetPattern)p2 );
//            }
//        }
//    }
    
//    public static FileObject fileObjectForElement( Element element ) {
//        return JavaMetamodel.getManager().getFileObject(element.getResource());
//    }
//    
//    public static JavaClass findClassElement( String name, Pattern pattern ) {
//        return pattern.patternAnalyser.findClassElement( name );
//    }
//
//
//    public FileObject findFileObject () {
//        return fileObjectForElement( referenceClassElement != null ? referenceClassElement : classElementHandle );
//    }
//
//    JavaClass findClassElement( String name ) {
//        Type t = findType(name);
//        if (t instanceof JavaClass) {
//            return (JavaClass) t;
//        } else {
//            return null;
//        }
//    }
//    
//    Type findType(String name) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        JavaClass jc = referenceClassElement != null? referenceClassElement: classElementHandle;
//        Type t = null;
//        if (jc.isValid()) {
//            t = JavaMetamodel.getManager().getJavaExtent(jc).getType().resolve(name);
//        }
//
//        return t;
//    }
//
   
}
