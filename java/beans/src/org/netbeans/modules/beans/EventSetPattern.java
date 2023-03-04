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

package org.netbeans.modules.beans;

import java.awt.Image;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;

/** EventSetPattern: This class holds the information about used event set pattern
 * in code.
 * @author Petr Hrebejk
 *
 * 
 *  PENDING: Add Pattern class hierarchy (abstract classes || interfaces )
 */
public final class EventSetPattern extends Pattern {

    protected ElementHandle<ExecutableElement> addListenerMethod;
    protected ElementHandle<ExecutableElement> removeListenerMethod;
    protected boolean isUnicast;
    
    // Constructors ------------------------------------------------------------
    
    public EventSetPattern(PatternAnalyser patternAnalyser,
                           ExecutableElement addListenerMethod, 
                           ExecutableElement removeListenerMethod, 
                           String name,
                           TypeMirror type, 
                           boolean isUnicast ) {
        super( patternAnalyser, Pattern.Kind.EVENT_SOURCE, name, TypeMirrorHandle.create(type) );
        this.addListenerMethod = addListenerMethod == null ? null : ElementHandle.create(addListenerMethod);
        this.removeListenerMethod = removeListenerMethod == null ? null : ElementHandle.create(removeListenerMethod);
        this.isUnicast = isUnicast;
    }
    
//    EventSetPattern( PatternAnalyser analyser ) {
//        super( analyser );
//    }
    
    // Getters and setters -----------------------------------------------------
    
    @Override
    public Image getIcon() {        
        return isUnicast() ? EVENT_SET_UNICAST : EVENT_SET_MULTICAST;
    }

    @Override
    public String getHtmlDisplayName() {
        return null;
    }
 
    /** Returns the getter method */
    public ElementHandle<ExecutableElement> getAddListenerMethod() {
        return addListenerMethod;
    }

    /** Returns the setter method */
    public ElementHandle<ExecutableElement> getRemoveListenerMethod() {
        return removeListenerMethod;
    }

    
    /** Returns the mode of the property READ_WRITE, READ_ONLY or WRITE_ONLY */
    public boolean isUnicast() {
        return isUnicast;
    }
    
  /** Sets the name of PropertyPattern */
    public void setName( String name ) /* throws IllegalArgumentException */ {
          throw new UnsupportedOperationException();  
        
//        if ( !Utilities.isJavaIdentifier( name ) || name.indexOf( "Listener" ) <= 0 ) // NOI18N
//            throw new IllegalArgumentException( "Invalid event source name" ); // NOI18N
//
//        name = capitalizeFirstLetter( name );
//
//        String addMethodID = "add" + name; // NOI18N
//        String removeMethodID = "remove" + name; // NOI18N
//
//        JMIUtils.beginTrans(true);
//        boolean rollback = true;
//        try {
//            if (addListenerMethod.isValid() && removeListenerMethod.isValid()) {
//                addListenerMethod.setName( addMethodID );
//                removeListenerMethod.setName( removeMethodID );
//                this.name = Introspector.decapitalize( name );
//            }
//            rollback = false;
//        } finally {
//            JMIUtils.endTrans(rollback);
//        }
    }

  
    

//
//    /** Creates new pattern from result of dialog */
//    static EventSetPattern create( PatternAnalyser patternAnalyser,
//                                   String type,
//                                   int implementation,
//                                   boolean fire,
//                                   boolean passEvent,
//                                   boolean isUnicast ) {
//        
//        assert JMIUtils.isInsideTrans();
//        EventSetPattern esp = new EventSetPattern( patternAnalyser );
//        
//        esp.type = patternAnalyser.findType(type);
//
//        if ( esp.type == null || !(esp.type instanceof JavaClass)) {
//            return null;
//        }
//
//        //System.out.println( "Type " + esp.type.toString() ); // NOI18N
//
//
//        esp.name = Introspector.decapitalize( ((JavaClass) esp.type).getSimpleName() );
//        esp.isUnicast = isUnicast;
//
//        String listenerList = null;
//
//        if ( implementation == 1 ) {
//            if ( isUnicast )
//                BeanPatternGenerator.unicastListenerField( esp.getDeclaringClass(), esp.type, true);
//            else
//                BeanPatternGenerator.listenersArrayListField( esp.getDeclaringClass(), esp.type, true );
//        }
//        else if ( implementation == 2 && !isUnicast ) {
//            listenerList = BeanPatternGenerator.eventListenerListField( esp.getDeclaringClass(), esp.type, true).getName();
//        }
//
//
//        if ( isUnicast ) {
//            esp.generateAddListenerMethod( BeanPatternGenerator.ucAddBody( esp.type, implementation ), true );
//            esp.generateRemoveListenerMethod( BeanPatternGenerator.ucRemoveBody( esp.type, implementation ), true );
//        }
//        else {
//            esp.generateAddListenerMethod( BeanPatternGenerator.mcAddBody( esp.type, implementation, listenerList ), true );
//            esp.generateRemoveListenerMethod( BeanPatternGenerator.mcRemoveBody( esp.type, implementation, listenerList ), true );
//        }
//
//        if ( fire ) {
//            JavaClass listener = (JavaClass) esp.type;
//
//            List/*<Method>*/ methods = JMIUtils.getMethods(listener);
//            boolean isInterface = listener.isInterface();
//            for (Iterator it = methods.iterator(); it.hasNext();) {
//                Method method = (Method) it.next();
//                if ( ((method.getModifiers() & Modifier.PUBLIC) != 0 ) ||
//                     (isInterface && (method.getModifiers() & (Modifier.PROTECTED | Modifier.PRIVATE)) == 0)
//                   ) {
//                    if ( isUnicast )
//                        BeanPatternGenerator.unicastFireMethod( esp.getDeclaringClass(), esp.type,
//                                                                method, implementation, passEvent );
//                    else
//                        BeanPatternGenerator.fireMethod( esp.getDeclaringClass(), esp.type,
//                                                         method, implementation, listenerList, passEvent );
//                }
//            }
//        }
//
//
//        return esp;
//    }
//    
//    private Field getEstimatedListenerField() {
//        Field f;
//        if ( isUnicast )
//                f = BeanPatternGenerator.unicastListenerField( getDeclaringClass(), getType(), false);
//            else {
//                f = BeanPatternGenerator.listenersArrayListField( getDeclaringClass(), getType(), false);
//                if (f==null) {
//                    f = BeanPatternGenerator.eventListenerListField( getDeclaringClass(), getType(), false);
//                }
//            }
//        return f;
//    }
//
//    /** Test if the name is valid for given pattern */
//    protected static boolean isValidName( String str ) {
//        if ( Utilities.isJavaIdentifier(str) == false )
//            return false;
//
//        if (str.indexOf( "Listener" ) <= 0 ) // NOI18N
//            return false;
//
//        return true;
//    }
//    
//
//    
//    /** Sets the property to be unicast or multicast */
//    public void setIsUnicast( boolean b ) throws JmiException {
//        if ( b == isUnicast) {
//            return;
//        }
//        
//        JMIUtils.beginTrans(true);
//        boolean rollback = true;
//        try {
//            if (!addListenerMethod.isValid()) {
//                return; 
//            }
//            List/*<MultipartId>*/ exs = addListenerMethod.getExceptionNames();
//
//            if (b) {
//                JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(addListenerMethod);
//                MultipartId tooManyId = jmodel.getMultipartId().
//                        createMultipartId("java.util.TooManyListenersException", null, null); // NOI18N
//                exs.add(tooManyId);
//            } else {
//                JavaClass tooMany = patternAnalyser.findClassElement("java.util.TooManyListenersException"); // NOI18N
//                assert tooMany != null;
//                List/*<MultipartId>*/ remove = new LinkedList/*<MultipartId>*/();
//                for (Iterator it = exs.iterator(); it.hasNext();) {
//                    MultipartId exId = (MultipartId) it.next();
//                    JavaClass ex = (JavaClass) exId.getElement();
//                    if (tooMany.isSubTypeOf(ex)) {
//                        remove.add(exId);
//                    }
//                }
//                exs.removeAll(remove);
//            }
//            
//            this.isUnicast = b;
//            rollback = false;
//        } finally {
//            JMIUtils.endTrans(rollback);
//        }
//    }
//
//    
//
//    /** Sets the type of property */
//    public void setType( Type newType ) throws JmiException {
//        int state = 0;
//        JMIUtils.beginTrans(true);
//        boolean rollback = true;
//        try {
//            if (this.type.equals(newType) || !newType.isValid())
//                return;
//
//            if (!(newType instanceof JavaClass) ||
//                    !PatternAnalyser.isSubclass((JavaClass) newType,
//                            patternAnalyser.findClassElement("java.util.EventListener"))) { // NOI18N
//                
//                state = 1;
//            } else {
//                JavaModelPackage jmodel = (JavaModelPackage) addListenerMethod.refImmediatePackage();
//                
//                String newTypeName = ((JavaClass) newType).getSimpleName();
//                List/*<Parameter>*/  params = addListenerMethod.getParameters();
//                params.clear();
//                Parameter newParameter = jmodel.getParameter().createParameter();
//                newParameter.setName("listener"); // NOI18N
//                newParameter.setType(newType);
//                params.add(newParameter);
//
//                params = removeListenerMethod.getParameters();
//                params.clear();
//                newParameter = jmodel.getParameter().createParameter();
//                newParameter.setName("listener"); // NOI18N
//                newParameter.setType(newType);
//                params.add(newParameter);
//
//                // Ask if we have to change the bame of the methods
//                String msg = MessageFormat.format(PatternNode.getString("FMT_ChangeEventSourceName"), // NOI18N
//                        new Object[]{capitalizeFirstLetter(newTypeName)});
//                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
//                if (DialogDisplayer.getDefault().notify(nd).equals(NotifyDescriptor.YES_OPTION)) {
//                    setName(newTypeName);
//                }
//
//                this.type = newType;
//            }
//
//            rollback = false;
//        } finally {
//            JMIUtils.endTrans(rollback);
//        }
//        switch(state) {
//            case 1:
//                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(PatternNode.getString("MSG_InvalidListenerInterface"), // NOI18N
//                        NotifyDescriptor.ERROR_MESSAGE));
//                break;
//                
//        }
//    }
//
//    /** Gets the cookie of the first available method */
//    public Node.Cookie getCookie( Class cookieType ) {
//        return super.getCookie(cookieType);
//    }
//
//    public void destroy() throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        if ( addListenerMethod != null && addListenerMethod.isValid() ) {
//            addListenerMethod.refDelete();
//        }
//
//        if ( removeListenerMethod != null && removeListenerMethod.isValid() ) {
//            removeListenerMethod.refDelete();
//        }
//        
//        //** BOB - Matula
//        
//        // delete associated "fire" methods
//        JavaClass declaringClass = getDeclaringClass();
//        JavaClass listener = (JavaClass) type;
//        boolean canDelete = false;
//
//        if ( listener != null ) {
//            List methods = JMIUtils.getMethods(listener);
//            List sourceMethods = JMIUtils.getMethods(declaringClass);
//            String method;
//            String typeName = listener.getSimpleName();
//            
//            for (Iterator it = methods.iterator(); it.hasNext();) {
//                Method lsnrMethod = (Method) it.next();
//                method = "fire" + // NOI18N
//                        Pattern.capitalizeFirstLetter(typeName) +
//                        Pattern.capitalizeFirstLetter(lsnrMethod.getName());
//                if (Modifier.isPublic(lsnrMethod.getModifiers())) {
//                    for (Iterator it2 = sourceMethods.iterator(); it2.hasNext();) {
//                        Method srcMethod = (Method) it2.next();
//                        if (srcMethod.isValid() && srcMethod.getName().equals(method)) {
//                            if (!canDelete) {
//                                // Ask, if the fire methods can be deleted
//                                String mssg = MessageFormat.format( PatternNode.getString( "FMT_DeleteFire" ),
//                                                                    new Object[0] );
//                                NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
//                                if ( DialogDisplayer.getDefault().notify( nd ).equals( NotifyDescriptor.NO_OPTION ) ) {
//                                    return;
//                                } else {
//                                    canDelete = true;
//                                }
//                            }
//                            srcMethod.refDelete();
//                        }
//                    }
//                }
//            }
//        }
//        //** EOB - Matula
//        Field field = getEstimatedListenerField();
//        if ( field != null && field.isValid() && field.getReferences().isEmpty()) {
//            field.refDelete();
//        }
//    }
//
//    // Utility methods --------------------------------------------------------------------
//
//    
//
//    void generateAddListenerMethod ( String body, boolean javadoc ) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        JavaClass declaringClass = getDeclaringClass();
//        if ( declaringClass == null )
//            throw new IllegalStateException("Missing declaring class"); // NOI18N
//        
//        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(declaringClass);
//        Method newMethod = jmodel.getMethod().createMethod();
//        int modifiers = Modifier.PUBLIC | Modifier.SYNCHRONIZED;
//        Parameter newParameter = jmodel.getParameter().createParameter();
//        newParameter.setName("listener"); // NOI18N
//        newParameter.setType(type);
//
//        newMethod.setName( "add" + capitalizeFirstLetter( getName() ) ); // NOI18N
//        newMethod.setTypeName(jmodel.getMultipartId().createMultipartId("void", null, null)); // NOI18N
//        List/*<Parameter>*/ params = newMethod.getParameters();
//        params.add(newParameter);
//
//        if ( declaringClass.isInterface() ) {
//            modifiers &= ~Modifier.SYNCHRONIZED;    // synchronized modifier is not allowed in interface
//        } else if ( body != null )
//            newMethod.setBodyText( body );
//        newMethod.setModifiers( modifiers );
//        if ( isUnicast ) {
//            MultipartId tooManyLsnrs = jmodel.getMultipartId().
//                    createMultipartId("java.util.TooManyListenersException", null, null); // NOI18N
//            newMethod.getExceptionNames().add(tooManyLsnrs);
//        }
//        if ( javadoc ) {
//            String comment = MessageFormat.format( PatternNode.getString( "COMMENT_AddListenerMethod" ),
//                    new Object[] { ((JavaClass) type).getSimpleName() } );
//            newMethod.setJavadocText( comment );
//        }
//
//        declaringClass.getContents().add(newMethod);
//        addListenerMethod = newMethod;
//    }
//
//    void generateRemoveListenerMethod( String body, boolean javadoc ) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        JavaClass declaringClass = getDeclaringClass();
//        if ( declaringClass == null )
//            throw new IllegalStateException("Missing declaring class"); // NOI18N
//        
//        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(declaringClass);
//        Method newMethod = jmodel.getMethod().createMethod();
//        int modifiers = Modifier.PUBLIC | Modifier.SYNCHRONIZED;
//        Parameter newParameter = jmodel.getParameter().createParameter();
//        newParameter.setName("listener"); // NOI18N
//        newParameter.setType(type);
//
//        newMethod.setName( "remove" + capitalizeFirstLetter( getName() ) ); // NOI18N
//        newMethod.setTypeName(jmodel.getMultipartId().createMultipartId("void", null, null)); // NOI18N
//        List/*<Parameter>*/ params = newMethod.getParameters();
//        params.add(newParameter);
//        
//        if ( declaringClass.isInterface() ) {
//            modifiers &= ~Modifier.SYNCHRONIZED;    // synchronized modifier is not allowed in interface
//        } else if ( body != null )
//            newMethod.setBodyText( body );
//        newMethod.setModifiers( modifiers );
//        if ( javadoc ) {
//            String comment = MessageFormat.format( PatternNode.getString( "COMMENT_RemoveListenerMethod" ),
//                    new Object[] { ((JavaClass) type).getSimpleName() } );
//            newMethod.setJavadocText( comment );
//        }
//
//        declaringClass.getContents().add(newMethod);
//        removeListenerMethod = newMethod;
//    }
//
//    // Property change support -------------------------------------------------------------------------
//
//    void copyProperties( EventSetPattern src ) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        boolean changed = !src.getType().equals( getType() ) ||
//                          !src.getName().equals( getName() ) ||
//                          !(src.isUnicast() == isUnicast());
//
//        if ( src.getAddListenerMethod() != addListenerMethod )
//            addListenerMethod = src.getAddListenerMethod();
//        if ( src.getRemoveListenerMethod() != removeListenerMethod )
//            removeListenerMethod = src.getRemoveListenerMethod();
//
//        if ( changed ) {
//
//            isUnicast = testUnicast();
//
//            findEventSetType();
//            isUnicast = testUnicast();
//            name = findEventSetName();
//            
//            // XXX cannot be fired inside mdr transaction; post to dedicated thread or redesigne somehow
//            firePropertyChange( new java.beans.PropertyChangeEvent( this, null, null, null ) );
//        }
//
//    }

}
