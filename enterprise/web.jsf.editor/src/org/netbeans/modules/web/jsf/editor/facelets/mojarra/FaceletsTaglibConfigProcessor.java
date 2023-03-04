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
package org.netbeans.modules.web.jsf.editor.facelets.mojarra;

import com.sun.faces.config.ConfigurationException;
import org.netbeans.modules.web.jsf.editor.facelets.*;
import com.sun.faces.config.DocumentInfo;
import com.sun.faces.config.processor.AbstractConfigProcessor;
import com.sun.faces.facelets.tag.TagLibraryImpl;
import com.sun.faces.facelets.tag.jsf.CompositeComponentTagLibrary;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.facelets.util.ReflectionUtil;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrarySupport.Compiler;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.MessageFormat;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.Method;

import javax.faces.FacesException;
import javax.servlet.ServletContext;
import org.w3c.dom.Document;

/**
 * <p>
 *  This <code>ConfigProcessor</code> handles all elements defined under
 *  <code>/faces-taglib</code>.
 * </p>
 */
public class FaceletsTaglibConfigProcessor extends AbstractConfigProcessor {

    private static final Logger LOGGER = FacesLogger.CONFIG.getLogger();
    /**
     * <p>
     * /facelet-taglib/library-class
     * </p>
     */
    private static final String LIBRARY_CLASS = "library-class";
    /**
     * <p>
     * /facelet-taglib/namespace
     * </p>
     */
    private static final String TAGLIB_NAMESPACE = "namespace";
    /**
     * <p>
     * /facelet-taglib/tag
     * </p>
     */
    private static final String TAG = "tag";
    /**
     * <p>
     * /facelet-taglib/function
     * </p>
     */
    private static final String FUNCTION = "function";
    /**
     * <p>
     * /facelet-taglib/tag/tag-name
     * </p>
     */
    private static final String TAG_NAME = "tag-name";
    /**
     * <p>
     * /facelet-taglib/tag/component
     * </p>
     */
    private static final String COMPONENT = "component";
    /**
     * <p>
     * /facelet-taglib/tag/validator
     * </p>
     */
    private static final String VALIDATOR = "validator";
    /**
     * <p>
     * /facelet-taglib/tag/converter
     * </p>
     */
    private static final String CONVERTER = "converter";
    /**
     * <p>
     * /facelet-taglib/tag/behavior
     * </p>
     */
    private static final String BEHAVIOR = "behavior";
    /**
     * <p>
     * /facelet-taglib/tag/source
     * </p>
     */
    private static final String SOURCE = "source";
    /**
     * <p>
     *   <ul>
     *     <li>/facelet-taglib/tag/tag-handler</li>
     *     <li>/facelet-taglib/tag/converter/handler-class</li>
     *     <li>/facelet-taglib/tag/validator/handler-class</li>
     *     <li>/facelet-taglib/tag/component/handler-class</li>
     *   </ul>
     * </p>
     */
    private static final String HANDLER_CLASS = "handler-class";
    /**
     * <p>
     *  /facelet-taglib/tag/validator/validator-id
     * </p>
     */
    private static final String VALIDATOR_ID = "validator-id";
    /**
     * <p>
     *  /facelet-taglib/tag/converter/converter-id
     * </p>
     */
    private static final String CONVERTER_ID = "converter-id";
    /**
     * <p>
     *  /facelet-taglib/tag/behavior/behavior-id
     * </p>
     */
    private static final String BEHAVIOR_ID = "behavior-id";
    /**
     * <p>
     * /facelet-taglib/tag/resource-id
     * </p>
     */
    private static final String RESOURCE_ID = "resource-id";
    /**
     * <p>
     *  /facelet-taglib/tag/component/component-type
     * </p>
     */
    private static final String COMPONENT_TYPE = "component-type";
    /**
     * <p>
     *  /facelet-taglib/tag/component/renderer-type
     * </p>
     */
    private static final String RENDERER_TYPE = "renderer-type";
    /**
     * <p>
     *  /facelet-taglib/tag/function/function-name
     * </p>
     */
    private static final String FUNCTION_NAME = "function-name";
    /**
     * <p>
     *  /facelet-taglib/tag/function/function-class
     * </p>
     */
    private static final String FUNCTION_CLASS = "function-class";
    /**
     * <p>
     *  /facelet-taglib/tag/function/function-signature
     * </p>
     */
    private static final String FUNCTION_SIGNATURE = "function-signature";
    /**
     * <p>
     *  /facelet-taglib/composite-library-name
     * </p>
     */
    private static final String COMPOSITE_LIBRARY_NAME = "composite-library-name";

    private static final String ATTRIBUTE_NAME = "attribute";

    //fake compiler, same name, different class, just not to change this class much
    public Compiler compiler = new Compiler();
    private FaceletsLibrarySupport support;

    public FaceletsTaglibConfigProcessor(FaceletsLibrarySupport support) {
        this.support = support;
    }

    @Override
    protected Class<?> loadClass(ServletContext sc, String className, Object fallback, Class<?> expectedType) {
        try {
            // when the class is not found it throws internally FacesException since JSF2.2
            Class.forName(className, true, Thread.currentThread().getContextClassLoader());
            return super.loadClass(sc, className, fallback, expectedType);
        } catch (ClassNotFoundException ignore) {
        } catch (NoClassDefFoundError ignore) {
        }

        return null;

    }

    // -------------------------------------------- Methods from ConfigProcessor

    @Override
    public void process(ServletContext sc, DocumentInfo[] documentInfos) {
        for (int i = 0, length = documentInfos.length; i < length; i++) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE,
                           MessageFormat.format(
                                 "Processing facelet-taglibrary document: ''{0}''",
                                 documentInfos[i].getSourceURI()));
            }
            Document document = documentInfos[i].getDocument();
            // #243750 - skip incomplete tag libraries files
            if (document == null) {
                return;
            }
            String namespace = document.getDocumentElement().getNamespaceURI();
            Element documentElement = document.getDocumentElement();
            NodeList libraryClass = documentElement.getElementsByTagNameNS(namespace, LIBRARY_CLASS);
            if (libraryClass != null && libraryClass.getLength() > 0) {
                processTaglibraryClass(sc, libraryClass, compiler);
            } else {
                processTagLibrary(sc, documentInfos[i], documentElement, namespace, compiler);
            }
        }

//        try {
//            invokeNext(sc, documentInfos);
//        } catch (Exception e) {
//            //ignore
//        }
    }

    // --------------------------------------------------------- Private Methods


    private void processTaglibraryClass(ServletContext sc, NodeList libraryClass,
                                        Compiler compiler) {

//        Node n = libraryClass.item(0);
//        String className = getNodeText(n);
//        TagLibrary taglib = (TagLibrary) createInstance(sc, className, n);
//        compiler.addTagLibrary(taglib);

        //PENDING: enable this

    }

    private void processTagLibrary(ServletContext sc, DocumentInfo info, Element documentElement,
            String namespace,
            Compiler compiler) {

        NodeList children = documentElement.getChildNodes();
        if (children != null && children.getLength() > 0) {
            String taglibNamespace = null;
            String compositeLibraryName = null;
            for (int i = 0, ilen = children.getLength(); i < ilen; i++) {
                Node n = children.item(i);
                if (TAGLIB_NAMESPACE.equals(n.getLocalName())) {
                    taglibNamespace = getNodeText(n);
                } else if (COMPOSITE_LIBRARY_NAME.equals(n.getLocalName())) {
                    compositeLibraryName = getNodeText(n);
                }
            }

            URL sourceUrl;
            try {
                sourceUrl = info.getSourceURI().toURL();
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.INFO, null, ex);
                return;
            }

            NodeList tags = documentElement.getElementsByTagNameNS(namespace, TAG);
            NodeList functions = documentElement.getElementsByTagNameNS(namespace, FUNCTION);
            FaceletsLibrary taglibrary = compositeLibraryName != null
                    ? new CompositeComponentLibrary(support, compositeLibraryName, taglibNamespace, sourceUrl)
                    : new FaceletsLibrary(support, taglibNamespace, sourceUrl);

            processTags(sc, documentElement, tags, taglibrary);
            processFunctions(sc, functions, taglibrary);
            compiler.addTagLibrary(taglibrary);
        }
    }

    private void processTags(ServletContext sc, Element documentElement,
                             NodeList tags,
                             TagLibraryImpl taglibrary) {

        if (tags != null && tags.getLength() > 0) {
            for (int i = 0, ilen = tags.getLength(); i < ilen; i++) {
                Node tagNode = tags.item(i);
                NodeList children = tagNode.getChildNodes();
                String tagName = null;
                NodeList component = null;
                NodeList converter = null;
                NodeList validator = null;
                NodeList behavior = null;
                Node source = null;
                Node handlerClass = null;
                for (int j = 0, jlen = children.getLength(); j < jlen; j++) {
                    Node n = children.item(j);

                    // process the nodes to see what children we have
                    if (TAG_NAME.equals(n.getLocalName())) {
                        tagName = getNodeText(n);
                    } else if (COMPONENT.equals(n.getLocalName())) {
                        component = n.getChildNodes();
                    } else if (CONVERTER.equals(n.getLocalName())) {
                        converter = n.getChildNodes();
                    } else if (VALIDATOR.equals(n.getLocalName())) {
                        validator = n.getChildNodes();
                    } else if (BEHAVIOR.equals(n.getLocalName())) {
                        behavior = n.getChildNodes();
                    } else if (SOURCE.equals(n.getLocalName())) {
                        source = n;
                    } else if (HANDLER_CLASS.equals(n.getLocalName())) {
                        handlerClass = n;
                    }
                }
                if (component != null) {
                    processComponent(sc, documentElement, component, taglibrary, tagName);
                } else if (converter != null) {
                    processConverter(sc, converter, taglibrary, tagName);
                } else if (validator != null) {
                    processValidator(sc, validator, taglibrary, tagName);
                } else if (behavior != null) {
                    processBehavior(sc, behavior, taglibrary, tagName);
                } else if (source != null) {
                    processSource(documentElement, source, taglibrary, tagName);
                } else if (handlerClass != null) {
                    processHandlerClass(sc, handlerClass, taglibrary, tagName);
                }
            }
        }

    }

    private void processBehavior(ServletContext sc, NodeList behavior, TagLibraryImpl taglibrary,
			String tagName) {
        if (behavior != null && behavior.getLength() > 0) {
            String behaviorId = null;
            String handlerClass = null;
            for (int i = 0, ilen = behavior.getLength(); i < ilen; i++) {
                Node n = behavior.item(i);
                if (BEHAVIOR_ID.equals(n.getLocalName())) {
                    behaviorId = getNodeText(n);
                } else if (HANDLER_CLASS.equals(n.getLocalName())) {
                    handlerClass = getNodeText(n);
                }

            }
            if (handlerClass != null) {
                Class<?> clazz = loadClass(sc, handlerClass, this, null);
                taglibrary.putBehavior(tagName, behaviorId, clazz);
            } else {
                taglibrary.putBehavior(tagName, behaviorId);
            }
        }

    }


    private void processHandlerClass(ServletContext sc, Node handlerClass,
                                     TagLibraryImpl taglibrary,
                                     String name) {

        String className = getNodeText(handlerClass);
        //mfukala: (issue #184097) fix possible NPE from the code below if <handler-class> element is empty and the getNodeText() returns null
        if(className == null) {
            // fix for incomplete TAGs
            LOGGER.log(Level.FINE, "HandlerClass name not specified for tag={0}", name);
            taglibrary.putTagHandler(name, loadClass(sc, "com.sun.faces.facelets.tag.TagHandlerImpl", this, null)); //NOI18N
            return ; //just ignre that entry
        }
        Class<?> clazz = loadClass(sc, className, this, null);
        if (clazz != null) {
            taglibrary.putTagHandler(name, clazz);
        } else {
            // fix for missing HandlerClass
            LOGGER.log(Level.FINE, "HandlerClass {0} not found for tag={1}", new Object[]{className, name});
            taglibrary.putTagHandler(name, loadClass(sc, "com.sun.faces.facelets.tag.TagHandlerImpl", this, null)); //NOI18N
        }
    }

    private void processSource(Element documentElement,
                               Node source,
                               TagLibraryImpl taglibrary,
                               String name) {

        String docURI = documentElement.getOwnerDocument().getDocumentURI();
        String s = getNodeText(source);
        //mfukala: fix possible NPE from the code below if <source> element is empty and the getNodeText() returns null
        if(s == null) {
            return ; //just ignre that entry
        }
        try {
            URL url = new URL(new URL(docURI), s);
            taglibrary.putUserTag(name, url);
        } catch (MalformedURLException e) {
            throw new FacesException(e);
        }
    }

    private void processResourceId(Element documentElement,
                               Node compositeSource,
                               TagLibraryImpl taglibrary,
                               String name) {

        String resourceId = getNodeText(compositeSource);
        taglibrary.putCompositeComponentTag(name, resourceId);

    }

    private void processValidator(ServletContext sc, NodeList validator,
                                  TagLibraryImpl taglibrary,
                                  String name) {

        if (validator != null && validator.getLength() > 0) {
            String validatorId = null;
            String handlerClass = null;
            for (int i = 0, ilen = validator.getLength(); i < ilen; i++) {
                Node n = validator.item(i);
                if (VALIDATOR_ID.equals(n.getLocalName())) {
                    validatorId = getNodeText(n);
                } else if (HANDLER_CLASS.equals(n.getLocalName())) {
                    handlerClass = getNodeText(n);
                }

            }
            if (handlerClass != null) {
                    Class<?> clazz = loadClass(sc, handlerClass, this, null);
                    taglibrary.putValidator(name, validatorId, clazz);

            } else {
                taglibrary.putValidator(name, validatorId);
            }
        }

    }


    private void processConverter(ServletContext sc, NodeList converter,
                                  TagLibraryImpl taglibrary,
                                  String name) {

        if (converter != null && converter.getLength() > 0) {
            String converterId = null;
            String handlerClass = null;
            for (int i = 0, ilen = converter.getLength(); i < ilen; i++) {
                Node n = converter.item(i);

                if (CONVERTER_ID.equals(n.getLocalName())) {
                    converterId = getNodeText(n);
                } else if (HANDLER_CLASS.equals(n.getLocalName())) {
                    handlerClass = getNodeText(n);
                }

            }
            if (handlerClass != null) {
                    Class<?> clazz = loadClass(sc, handlerClass, this, null);
                    taglibrary.putConverter(name, converterId, clazz);

            } else {
                taglibrary.putConverter(name, converterId);
            }
        }

    }


    private void processComponent(ServletContext sc, Element documentElement, NodeList component,
                                  TagLibraryImpl taglibrary,
                                  String name) {

        if (component != null && component.getLength() > 0) {
            String componentType = null;
            String rendererType = null;
            String handlerClass = null;
            Node resourceId = null;
            for (int i = 0, ilen = component.getLength(); i < ilen; i++) {
                Node n = component.item(i);
                if (COMPONENT_TYPE.equals(n.getLocalName())) {
                    componentType = getNodeText(n);
                } else if (RENDERER_TYPE.equals(n.getLocalName())) {
                    rendererType = getNodeText(n);
                } else if (HANDLER_CLASS.equals(n.getLocalName())) {
                    handlerClass = getNodeText(n);
                }  else if (RESOURCE_ID.equals(n.getLocalName())) {
                    resourceId = n;
                }
            }
            if (handlerClass != null) {
                Class<?> clazz = loadClass(sc, handlerClass, this, null);
                    taglibrary.putComponent(name,
                                            componentType,
                                            rendererType,
                                            clazz);
            } else if (resourceId != null) {
                processResourceId(documentElement, resourceId, taglibrary, name);
            } else {
                taglibrary.putComponent(name, componentType, rendererType);
            }

        }

    }


    private void processFunctions(ServletContext sc, NodeList functions, TagLibraryImpl taglibrary) {
        if (functions != null && functions.getLength() > 0) {
            for (int i = 0, ilen = functions.getLength(); i < ilen; i++) {
                NodeList children = functions.item(i).getChildNodes();
                String functionName = null;
                String functionClass = null;
                String functionSignature = null;
                for (int j = 0, jlen = children.getLength(); j < jlen; j++) {
                    Node n = children.item(j);

                    if (FUNCTION_NAME.equals(n.getLocalName())) {
                        functionName = getNodeText(n);
                    } else if (FUNCTION_CLASS.equals(n.getLocalName())) {
                        functionClass = getNodeText(n);
                    } else if (FUNCTION_SIGNATURE.equals(n.getLocalName())) {
                        functionSignature = getNodeText(n);
                    }
                }
                Method m = null;
                try {
                    Class<?> clazz = loadClass(sc, functionClass, this, null);
                    m = createMethod(clazz, functionSignature);
                } catch (Exception | Error throwable) {
//                    throw new ConfigurationException(e);
                    //ignore
                }

                //add the function even if method is null
                taglibrary.putFunction(functionName, m);
            }
        }

    }


    private static Method createMethod(Class type, String signatureParam) throws Exception {
        // formatted XML might cause \n\t characters - make sure we only have space characters left
        String signature = signatureParam.replaceAll("\\s+", " ");
        int pos = signature.indexOf(' ');
        if (pos == -1) {
            throw new Exception("Must Provide Return Type: " + signature);
        } else {
            int pos2 = signature.indexOf('(', pos + 1);
            if (pos2 == -1) {
                throw new Exception(
                      "Must provide a method name, followed by '(': "
                      + signature);
            } else {
                String mn = signature.substring(pos + 1, pos2).trim();
                pos = signature.indexOf(')', pos2 + 1);
                if (pos == -1) {
                    throw new Exception("Must close parentheses, ')' missing: "
                                        + signature);
                } else {
                    String[] ps = signature.substring(pos2 + 1, pos).trim().split(",");
                    Class[] pc;
                    if (ps.length == 1 && "".equals(ps[0])) {
                        pc = new Class[0];
                    } else {
                        pc = new Class[ps.length];
                        for (int i = 0; i < pc.length; i++) {
                            pc[i] = ReflectionUtil.forName(ps[i].trim());
                        }
                    }
                    try {
                        return type.getMethod(mn, pc);
                    } catch (NoSuchMethodException e) {
                        throw new Exception("No Function Found on type: "
                                            + type.getName()
                                            + " with signature: "
                                            + signature);
                    }

                }
            }
        }

    }
}
