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

package org.netbeans.installer.wizard;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.installer.utils.helper.PropertyContainer;
import org.netbeans.installer.utils.helper.UiMode;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.helper.FinishHandler;
import org.netbeans.installer.utils.helper.Context;
import org.netbeans.installer.wizard.containers.SilentContainer;
import org.netbeans.installer.wizard.containers.WizardContainer;
import org.netbeans.installer.wizard.containers.SwingFrameContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This is the main class of the NBI Wizard framework. It represents the wizard as
 * a whole.
 *
 * <p>
 * The wizard serves a the main source of the data input by the user. It is
 * organized as a series of pages, each either informing the user about something
 * or displaying an input field. The input validation also happens here.
 *
 * <p>
 * The wizard is a singleton in some sense, i.e. only one instance of {@link Wizard}
 * can be created directly, via the {@link #getInstance()} method. All other
 * instances that may be created during the course of wizard's workflow will be the
 * so-called child wizards.
 *
 * <p>
 * The wizard operates over a list of {@link WizardComponent}s. In order to add
 * "depth", the user needs to create sub-wizards which would have their own
 * sequences of components, but will appear as a single component to the parent
 * wizard. This effect can be obtained via the
 * {@link org.netbeans.installer.wizard.components.WizardSequence} component.
 *
 * <p>
 * Context. Each wizard can have an associated context. A {@link Context} is a
 * simple collection of objects which can be fetched by their class. The context is
 * assigned to a wizard at runtime, thus it is possible to switch contexts basing
 * on some conditions. The context is intended to be read-only, though it is
 * possible to add new objects to it. The most common usecase for a context would
 * be passing an instance of an object to all the components of a wizard.
 *
 * <p>
 * Property container. Since the princial use case for a wizard is collecting some
 * user input, the values entered by the user need to be stored somewhere. For this
 * purpose a wizard has an associated {@link PropertyContainer}. It serves as a
 * storage for the user-entered strings.
 *
 * <p>
 * The root wizard instance will load its list of components from an URI defined in
 * the system property {@link #COMPONENTS_INSTANCE_URI_PROPERTY}, if the property is
 * not set, then it falls back to the {@link #DEFAULT_COMPONENTS_INSTANCE_URI}. For
 * child wizards, created with one of the
 * {@link #createSubWizard(List,int)} methods, expect that their lists of components
 * will be passed in directly. The list of components can be constructed using the
 * {@link #loadWizardComponents(String)} method.
 *
 * @author Kirill Sorokin
 * @since 1.0
 */
public class Wizard {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    /**
     * The instance of the root wizard.
     */
    private static Wizard instance;
    
    /**
     * URI which points to the list of components for the root wizard instance.
     */
    private static String componentsInstanceUri;
    
    /**
     * URI which points to the XML Schema which will be used to validate the list
     * of wizard components stored in XML format.
     */
    private static String componentsSchemaUri;
    
    /**
     * Returns the instance of the root {@link Wizard}. If the instance does not
     * exist - it is created and all system properties that make sense for the
     * {@link Wizard} are parsed: {@link #COMPONENTS_INSTANCE_URI_PROPERTY} and
     * {@link #COMPONENTS_SCHEMA_URI_PROPERTY}.
     *
     * @return The instance of the root {@link Wizard}.
     */
    public static synchronized Wizard getInstance() {
        if (instance == null) {
            // initialize uri for root wizard's components list
            if (System.getProperty(COMPONENTS_INSTANCE_URI_PROPERTY) != null) {
                componentsInstanceUri =
                        System.getProperty(COMPONENTS_INSTANCE_URI_PROPERTY);
            } else {
                componentsInstanceUri = DEFAULT_COMPONENTS_INSTANCE_URI;
            }
            
            // initialize uri for components list xml schema
            if (System.getProperty(COMPONENTS_SCHEMA_URI_PROPERTY) != null) {
                componentsSchemaUri =
                        System.getProperty(COMPONENTS_SCHEMA_URI_PROPERTY);
            } else {
                componentsSchemaUri = DEFAULT_COMPONENTS_SCHEMA_URI;
            }
            
            // create the root wizard and load its components
            instance = new Wizard();
            try {
                instance.components = loadWizardComponents(componentsInstanceUri);
            } catch (InitializationException e) {
                ErrorManager.notifyCritical(ResourceUtils.getString(
                        Wizard.class, RESOURCE_FAILED_TO_CREATE_INSTANCE), e);
            }
        }
        
        return instance;
    }
    
    /**
     * Loads the list of {@link WizardComponent} from an XML file identified by its
     * URI. The URI can be of any scheme supported by the
     * {@link org.netbeans.installer.downloader.DownloadManager}.
     *
     * @param componentsUri URI of the XML file which contains the list of
     *      {@link WizardComponent}s.
     * @return The list of {@link WizardComponent} defined in the XML file.
     * @throws org.netbeans.installer.utils.exceptions.InitializationException If an
     *      error occurs during loading of the list.
     */
    public static List<WizardComponent> loadWizardComponents(
            final String componentsUri) throws InitializationException {
        return loadWizardComponents(componentsUri, Wizard.class.getClassLoader());
    }
    
    /**
     * Loads the list of {@link WizardComponent} from an XML file identified by its
     * URI and using the specified {@link ClassLoader} to load the components'
     * classes. The URI can be of any scheme supported by the
     * {@link org.netbeans.installer.downloader.DownloadManager}.
     *
     * @param componentsUri URI of the XML file which contains the list of
     *      {@link WizardComponent}.
     * @param classLoader Instance of {@link ClassLoader} which should be used for
     *      loading the components' classes.
     * @return The list of {@link WizardComponent} defined in the XML file.
     * @throws org.netbeans.installer.utils.exceptions.InitializationException If an
     *      error occurs during loading of the list.
     */
    public static List<WizardComponent> loadWizardComponents(
            final String componentsUri,
            final ClassLoader classLoader) throws InitializationException {
        try {
            final File schemaFile = FileProxy.getInstance().getFile(
                    componentsSchemaUri,
                    classLoader, true);
            final File componentsFile = FileProxy.getInstance().getFile(
                    componentsUri,
                    classLoader, true);
            
            final Schema schema = SchemaFactory.
                    newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).
                    newSchema(schemaFile);
            
            final DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            try {
                factory.setSchema(schema);
            } catch (UnsupportedOperationException e) {
                // if the parser does not support schemas, let it be -- we can do
                // without it anyway -- just log it and proceed
                ErrorManager.notifyDebug(
                        ResourceUtils.getString(Wizard.class,
                        RESOURCE_PARSER_UNSUPPORTS_SCHEMAS,factory.getClass()),
                        e);
            }
            factory.setNamespaceAware(true);
            
            final Document document = factory.newDocumentBuilder().
                    parse(componentsFile);
            
            return loadWizardComponents(document.getDocumentElement(), classLoader);
        } catch (DownloadException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Wizard.class,
                    RESOURCE_FAILED_TO_LOAD_COMPONENTS,
                    componentsUri,
                    classLoader), e);
        } catch (ParserConfigurationException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Wizard.class,
                    RESOURCE_FAILED_TO_LOAD_COMPONENTS,
                    componentsUri,
                    classLoader), e);
        } catch (SAXException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Wizard.class,
                    RESOURCE_FAILED_TO_LOAD_COMPONENTS,
                    componentsUri,
                    classLoader), e);
        } catch (IOException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Wizard.class,
                    RESOURCE_FAILED_TO_LOAD_COMPONENTS,
                    componentsUri,
                    classLoader), e);
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    /**
     * Loads the list of {@link WizardComponent} from the DOM tree identified by its
     * root {@link Element} using the specified {@link ClassLoader} to load the
     * components' classes.
     *
     * @param element Root of the DOM tree from which to load the list.
     * @param classLoader Instance of {@link ClassLoader} which should be used for
     *      loading the components' classes.
     * @return The list of {@link WizardComponent} defined in the DOM tree.
     * @throws org.netbeans.installer.utils.exceptions.InitializationException If an
     *      error occurs during loading of the list.
     */
    private static List<WizardComponent> loadWizardComponents(
            final Element element,
            final ClassLoader classLoader) throws InitializationException {
        final List<WizardComponent> components = new LinkedList<WizardComponent>();
        
        for (Element child: XMLUtils.getChildren(element, TAG_COMPONENT)) {
            components.add(loadWizardComponent(child, classLoader));
        }
        
        return components;
    }
    
    /**
     * Loads a single {@link WizardComponent} from a DOM {@link Element} using the
     * specified {@link ClassLoader} to load the component's class.
     *
     * @param element DOM {@link Element} from which the component's data should be
     *      loaded.
     * @param classLoader Instance of {@link ClassLoader} which should be used for
     *      loading the component's class.
     * @return The loaded {@link WizardComponent}.
     * @throws org.netbeans.installer.utils.exceptions.InitializationException If an
     *      error occurs during loading of the component.
     */
    private static WizardComponent loadWizardComponent(
            final Element element,
            final ClassLoader classLoader) throws InitializationException {
        final WizardComponent component;
        
        try {
            component = (WizardComponent) classLoader.loadClass(
                    element.getAttribute(ATTRIBUTE_CLASS)).newInstance();
            
            Element child = XMLUtils.getChild(element, TAG_COMPONENTS);
            if (child != null) {
                component.addChildren(loadWizardComponents(child, classLoader));
            }
            
            child = XMLUtils.getChild(element, TAG_PROPERTIES);
            if (child != null) {
                component.getProperties().putAll(XMLUtils.parseNbiProperties(child));
            }
        } catch (ParseException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Wizard.class,
                    RESOURCE_FAILED_TO_LOAD_COMPONENT,
                    element,
                    classLoader), e);
        } catch (ClassNotFoundException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Wizard.class,
                    RESOURCE_FAILED_TO_LOAD_COMPONENT,
                    element,
                    classLoader), e);
        } catch (IllegalAccessException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Wizard.class,
                    RESOURCE_FAILED_TO_LOAD_COMPONENT,
                    element,
                    classLoader), e);
        } catch (InstantiationException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Wizard.class,
                    RESOURCE_FAILED_TO_LOAD_COMPONENT,
                    element,
                    classLoader), e);
        }
        
        return component;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * List of the {@link WizardComponent} over which the {@link Wizard} should
     * iterate.
     */
    private List<WizardComponent> components;
    
    /**
     * Container which should display the UI of the wizard.
     */
    private WizardContainer container;
    
    /**
     * Storage for the user input in the panels of the wizard.
     */
    private PropertyContainer propertyContainer;
    
    /**
     * {@link Wizard}'s {@link Context}.
     */
    private Context context;
    
    /**
     * {@link ClassLoader} that should be used by the wizard. E.g. for loading
     * resources.
     */
    private ClassLoader classLoader;
    
    /**
     * Index of the currently active component.
     */
    private int index;
    
    /**
     * Parent of the {@link Wizard}. It is <code>null</code> if the current
     * {@link Wizard} is the root one.
     */
    private Wizard parent;
    
    /**
     * Handler of the <code>cancel</code> and <code>finish</code> requests which
     * happen either by user request or as part of the normal wizard work flow.
     */
    private FinishHandler finishHandler;
    
    /**
     * Specifies whether the wizard is opened in blocking mode. If it is, the
     * opening method {@link #openBlocking()} will not return intil the wizard is
     * closed from another thread.
     */
    private boolean blocking;
    
    // constructors /////////////////////////////////////////////////////////////////
    /**
     * Default constructor. Performs initialization of the basic properties, which,
     * however, is not enough for normal operation - the list of
     * {@link WizardComponent} is not initialized.
     */
    public Wizard() {
        this.index = -1;
        this.context = new Context();
        this.classLoader = getClass().getClassLoader();
        
        this.blocking = false;
    }
    
    /**
     * Constructs a new instance of {@link Wizard} with the given parent. Most of
     * the properties of the new instance are inherited from the parent.
     *
     * @param parent Parent {@link Wizard}.
     */
    public Wizard(
            final Wizard parent) {
        this();
        
        this.parent = parent;
        if (this.parent != null) {
            this.container = parent.container;
            
            this.propertyContainer = parent.propertyContainer;
            this.context = new Context(parent.context);
            this.classLoader = parent.classLoader;
            
            this.finishHandler = parent.finishHandler;
        }
    }
    
    /**
     * Constructs a new instance of {@link Wizard} with the given parent, list of
     * {@link WizardComponent} and inital index of the active component.
     *
     * @param parent Parent {@link Wizard}.
     * @param components List of {@link WizardComponent}s over which the wizard
     *      should iterate.
     * @param index Initial index of the active component.
     */
    public Wizard(
            final Wizard parent,
            final List<WizardComponent> components,
            int index) {
        this(parent);
        
        this.components = components;
        this.index = index;
    }
    
    /**
     * Constructs a new instance of {@link Wizard} with the given parent, list of
     * {@link WizardComponent}, inital index of the active component, properties
     * container and classloader.
     *
     * @param parent Parent {@link Wizard}.
     * @param components List of {@link WizardComponent} over which the wizard
     *      should iterate.
     * @param index Initial index of the active component.
     * @param propertyContainer {@link PropertyContainer} which should be used by
     *      the wizard.
     * @param classLoader {@link ClassLoader} which should be used by the wizard.
     */
    public Wizard(
            final Wizard parent,
            final List<WizardComponent> components,
            final int index,
            final PropertyContainer propertyContainer,
            final ClassLoader classLoader) {
        this(parent, components, index);
        
        this.propertyContainer = propertyContainer;
        this.classLoader = classLoader;
    }
    
    private WizardContainer newWizardContainer() {
        // then create the container according to the current UI mode
        switch (UiMode.getCurrentUiMode()) {
            case SWING:
                // init the look and feel
                // we have to do it here, not in SwingFrameContainer because 
                // we must to initialize L&F before calling JFrame constructor because of
                // using JFrame.setDefaultLookAndFeelDecorated
                try {
                    UiUtils.initializeLookAndFeel();
                } catch (InitializationException e) {
                    // error in UI init, no UI is available, no warning dialog can't be opened
                    UiMode.setCurrentUiMode(UiMode.SILENT);
                    ErrorManager.notifyCritical(e.getMessage(), e.getCause());
                }
                return new SwingFrameContainer();
            case SILENT:
                return new SilentContainer();
            default:
                ErrorManager.notifyCritical(ResourceUtils.getString(
                        Wizard.class,
                        RESOURCE_UNKNOWN_UI_MODE,
                        UiMode.getCurrentUiMode()));
                return null;
        }
    }
    
    // wizard lifecycle control methods /////////////////////////////////////////////
    /**
     * Opens the wizard. Depending on the current UI mode, an appropriate
     * {@link WizardContainer} is chosen, initialized and set to be visible. No
     * wizard container is created if the UI Mode is {@link UiMode#SILENT}.
     *
     * <p>
     * If the current wizard is not the root one - the parent's {@link #open()}
     * method is called.
     */
    public void open() {
        // if a parent exists, ask it - it knows better
        if (parent != null) {
            parent.open();
            return;
        }
        
        // then create the container according to the current UI mode
        container = newWizardContainer();        
        
        if (container!=null) {
            container.open();
        }
        next();
    }
    
    /**
     * Opens the wizard in a blocking mode. As opposed to {@link #open()}, this
     * method will not return the wizard is closed from another thread.
     *
     * @see #open()
     */
    public void openBlocking() {
        this.blocking = true;
        
        open();
        
        while (blocking) {
            try {
                wait();
            } catch (InterruptedException e) {
                ErrorManager.notifyDebug(ResourceUtils.getString(
                        Wizard.class, RESOURCE_INTERRUPTED), e);
            }
        }
    }
    
    /**
     * Closes the wizard. The current {@link WizardContainer} is hidden and
     * deinitialized. No real action is taken if the UI mode is
     * {@link UiMode#SILENT}.
     *
     * <p>
     * If the current wizard is not the ro  ot one - the parent's {@link #close()}
     * method is called.
     */
    public void close() {
        // if a parent exists, ask it - it knows better
        if (parent != null) {
            parent.close();
            return;
        }
        
        // if the container has not yet been initialized -- we do not need to
        // do anything with it
        if (container!=null) {
            container.close();
        }
        
        if (blocking) {
            blocking = false;
            notifyAll();
        }
    }
    
    // component flow control methods ///////////////////////////////////////////////
    /**
     * Proceeds to the next element in the wizard. If the next element is not
     * available and the current wizard instance is the root one, then the wizard is
     * considered to be at the last element and the wizard's
     * {@link FinishHandler#finish()} is called.
     *
     * <p>
     * If the current wizard is not the root instance and there is no next component,
     * then the parent wizard's {@link #next()} method is called.
     */
    public void next() {
        final WizardComponent component = getNext();
        
        // if there is no next component in the current wizard, try to delegate
        // the call to the parent wizard, and if there is no parent wizard... finish
        // the sequence, and call the finish handler
        if (component != null) {
            index = components.indexOf(component);
            
            component.setWizard(this);
            component.initialize();
            if (container!=null) {
                container.updateWizardUi(component.getWizardUi());
            }
            System.setProperty(
                    CURRENT_COMPONENT_CLASSNAME_PROPERTY,
                    component.getClass().getName());
            
            component.executeForward();
        } else if (parent != null) {
            parent.next();
        } else {
            finishHandler.finish();
        }
    }
    
    /**
     * Moves to the previous element in the wizard. If the previous component is not
     * available and the current wizard instance is the root one a critical error is
     * shown and the application terminates.
     *
     * <p>
     * If the current wizard is not the root one and there is no previous component
     * in the current sequence, the parent's {@link #previous()} method is called.
     */
    public void previous() {
        final WizardComponent component = getPrevious();
        
        // if there is no previous component in the current wizard, try to delegate
        // the call to the parent wizard, and if there is no parent wizard... we
        // should be here in the first place
        if (component != null) {
            index = components.indexOf(component);
            
            component.setWizard(this);
            component.initialize();
            if (container!=null) {
                container.updateWizardUi(component.getWizardUi());
            }
            
            System.setProperty(
                    CURRENT_COMPONENT_CLASSNAME_PROPERTY,
                    component.getClass().getName());
            
            component.executeBackward();
        } else if (parent != null) {
            parent.previous();
        } else {
            ErrorManager.notifyCritical(ResourceUtils.getString(
                    Wizard.class,
                    RESOURCE_CANNOT_MOVE_BACKWARD_AT_FIRST));
        }
    }
    
    // informational methods ////////////////////////////////////////////////////////
    /**
     * Checks whether there is exists a component which can be reached using the
     * {@link #next()} method. This method checks both the components of the
     * current wizard and the parent one.
     *
     * @return <code>true</code> is there is a next element, <code>false</code>
     *      otherwise.
     */
    public boolean hasNext() {
        // if there is no next component in the current wizard, we should check the
        // parent wizard if it has one
        return (getNext() != null) || ((parent != null) && parent.hasNext());
    }
    
    /**
     * Checks whether there is exists a component which can be reached using the
     * {@link #previous()} method. This method checks both the components of the
     * current wizard and the parent one.
     *
     * @return <code>true</code> is there is a previous element, <code>false</code>
     *      otherwise.
     */
    public boolean hasPrevious() {
        // if current component is a point of no return - we cannot move backwards,
        // i.e. there is no previous component
        if ((getCurrent() != null) && getCurrent().isPointOfNoReturn()) {
            return false;
        }
        
        for (int i = index - 1; i > -1; i--) {
            final WizardComponent component = components.get(i);
            
            // if the component can be executed backward it is the previous one
            if (component.canExecuteBackward()) {
                return true;
            }
            
            // if the currently examined component is a point of no return and it
            // cannot be executed (since we passed the previous statement) - we have
            // no previous component
            if (component.isPointOfNoReturn()) {
                return false;
            }
        }
        
        // if we got this far, there is not previous component in the current wizard,
        // but no points of no return we encountered either. thus we should ask the
        // parent wizard if it has a previous component
        return (parent != null) && parent.hasPrevious();
    }
    
    // getters/setters //////////////////////////////////////////////////////////////
    /**
     * Returns the index of the currently active component.
     *
     * @return Index of the currently active component.
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Returns the {@link WizardContainer} which is used by this {@link Wizard}
     * instance.
     *
     * @return {@link WizardContainer} which is used by this {@link Wizard}
     *      instance.
     */
    public WizardContainer getContainer() {
        return container;
    }
    
    /**
     * Gets the value of the property with the given name. This method in turn calls
     * the {@link PropertyContainer#getProperty(String)} method on the property
     * container used by this wizard instance.
     *
     * @param name Name of the property whose value should be obtained.
     * @return Value of the specified property, or <code>null</code> is the property
     *      with the given name does not exist.
     */
    public String getProperty(final String name) {
        return propertyContainer.getProperty(name);
    }
    
    /**
     * Sets the value of the property with the given name to the given value. This
     * method in turn calls the {@link PropertyContainer#setProperty(String,String)}
     * method on the property container used by this wizard instance.
     *
     * @param name Name of the property whose value should be set.
     * @param value New value for the property.
     */
    public void setProperty(final String name, final String value) {
        propertyContainer.setProperty(name, value);
    }
    
    /**
     * Returns the {@link Context} of this {@link Wizard} instance.
     *
     * @return {@link Context} of this {@link Wizard} instance.
     */
    public Context getContext() {
        return context;
    }
    
    /**
     * Returns the {@link ClassLoader} used by this {@link Wizard} instance.
     *
     * @return {@link ClassLoader} used by this {@link Wizard} instance.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    /**
     * Returns the {@link FinishHandler} used by this {@link Wizard} instance.
     *
     * @return {@link FinishHandler} used by this {@link Wizard} instance.
     */
    public FinishHandler getFinishHandler() {
        return finishHandler;
    }
    
    /**
     * Sets the {@link FinishHandler} which should be used by this {@link Wizard}
     * instance.
     *
     *
     * @param finishHandler {@link FinishHandler} which should be used by this
     *      {@link Wizard} instance.
     */
    public void setFinishHandler(final FinishHandler finishHandler) {
        this.finishHandler = finishHandler;
    }
    
    // factory methods for children /////////////////////////////////////////////////
    /**
     * Creates a new child (or sub-) wizard with the given list of
     * {@link WizardComponent}s and the initial index of the active component.
     *
     * @param components List of {@link WizardComponent}s over which the child
     *      wizard should iterate.
     * @param index Initial index of the active component.
     * @return New child (sub-) wizard.
     */
    public Wizard createSubWizard(
            final List<WizardComponent> components,
            final int index) {
        return new Wizard(this, components, index);
    }
    
    /**
     * Creates a new child (or sub-) wizard with the given list of
     * {@link WizardComponent}s, initial index of the active component, property
     * container and class loader.
     *
     * @param components List of {@link WizardComponent}s over which the child
     *      wizard should iterate.
     * @param index Initial index of the active component.
     * @param propertyContainer {@link PropertyContainer} which should be used by
     *      the child wizard.
     * @param classLoader {@link ClassLoader} which should be used by the child
     *      wizard.
     * @return New child (sub-) wizard.
     */
    public Wizard createSubWizard(
            final List<WizardComponent> components,
            final int index,
            final PropertyContainer propertyContainer,
            final ClassLoader classLoader) {
        return new Wizard(this, components, index, propertyContainer, classLoader);
    }
    
    // private //////////////////////////////////////////////////////////////////////
    /**
     * Returns the currently active {@link WizardComponent}.
     *
     * @return Currently active {@link WizardComponent}, or <code>null</code> if the
     *      index of the active component is outside the list size.
     */
    private WizardComponent getCurrent() {
        if ((index > -1) && (index < components.size())) {
            return components.get(index);
        } else {
            return null;
        }
    }
    
    /**
     * Returns the previous component in the <b>current</b> wizard's components
     * list.
     *
     * @return Previous {@link WizardComponent} or <code>null</code> if there is no
     *      previous component.
     */
    private WizardComponent getPrevious() {
        // if current component is a point of no return - we cannot move backwards,
        // i.e. there is no previous component
        if ((getCurrent() != null) && getCurrent().isPointOfNoReturn()) {
            return null;
        }
        
        for (int i = index - 1; i > -1; i--) {
            final WizardComponent component = components.get(i);
            
            // if the component can be executed backward it is the previous one
            if (component.canExecuteBackward()) {
                return component;
            }
            
            // if the currently examined component is a point of no return and it
            // cannot be executed (since we passed the previous statement) - we have
            // no previous component
            if (component.isPointOfNoReturn()) {
                return null;
            }
        }
        
        // if we reached the before-first index and yet could not find a previous
        // component, then there is no previous component
        return null;
    }
    
    /**
     * Returns the next component in the <b>current</b> wizard's components
     * list.
     *
     * @return Next {@link WizardComponent} or <code>null</code> if there is no
     *      next component.
     */
    private WizardComponent getNext() {
        for (int i = index + 1; i < components.size(); i++) {
            final WizardComponent component = components.get(i);
            
            // if the component can be executed forward it is the next one
            if (component.canExecuteForward()) {
                return component;
            }
        }
        
        // if we reached the after-last index and yet could not find a next
        // component, then there is no next component
        return null;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * Name of the system property which is expected to contain the URI which points
     * to the XML file with the list of components for the root wizard.
     */
    public static final String COMPONENTS_INSTANCE_URI_PROPERTY =
            "nbi.wizard.components.instance.uri"; // NOI18N
    
    /**
     * Default URI which points to the XML file with the list of components for the
     * root wizard.
     */
    public static final String DEFAULT_COMPONENTS_INSTANCE_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX +
            "org/netbeans/installer/wizard/wizard-components.xml"; // NOI18N
    
    /**
     * Name of the system property which is expected to contain the URI which points
     * to the XML schema which defines the format for the serialized list of wizard
     * components.
     */
    public static final String COMPONENTS_SCHEMA_URI_PROPERTY =
            "nbi.wizard.components.schema.uri"; // NOI18N
    
    /**
     * Default URI which points to the XML schema which defines the format for the
     * serialized list of wizard components.
     */
    public static final String DEFAULT_COMPONENTS_SCHEMA_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX +
            "org/netbeans/installer/wizard/wizard-components.xsd"; // NOI18N
    
    /**
     * Name of the XML tag which describes a list of components.
     */
    public static final String TAG_COMPONENTS =
            "components"; // NOI18N
    
    /**
     * Name of the XML tag which describes an individual component.
     */
    public static final String TAG_COMPONENT =
            "component"; // NOI18N
    
    /**
     * Name of the XMl tag which describes the properties of a component.
     */
    public static final String TAG_PROPERTIES =
            "properties"; // NOI18N
    
    /**
     * Name of the XML attribute which contains the classname of a component.
     */
    public static final String ATTRIBUTE_CLASS =
            "class"; // NOI18N
    
    /**
     * Name of the system property which will be set when a component executes. Its
     * value will be the fully qualified class name of the component.
     */
    public static final String CURRENT_COMPONENT_CLASSNAME_PROPERTY =
            "nbi.wizard.current.component.classname"; // NOI18N
    
    // private //////////////////////////////////////////////////////////////////////
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_FAILED_TO_CREATE_INSTANCE =
            "W.error.failed.to.create.instance"; // NOI18N
    
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_FAILED_TO_LOAD_COMPONENTS =
            "W.error.failed.to.load.components"; // NOI18N
    
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_FAILED_TO_LOAD_COMPONENT =
            "W.error.failed.to.load.component"; // NOI18N
    
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_UNKNOWN_UI_MODE =
            "W.error.unknown.ui.mode"; // NOI18N
    
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_CANNOT_MOVE_BACKWARD_AT_FIRST =
            "W.error.cannot.move.backward.at.first"; // NOI18N
    
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_PARSER_UNSUPPORTS_SCHEMAS =
            "W.error.parser.unsupports.schemas";//NOI18N
    
    /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_INTERRUPTED =
            "W.error.interrupted";//NOI18N
}
