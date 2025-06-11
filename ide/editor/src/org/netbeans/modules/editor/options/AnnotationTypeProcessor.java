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

package org.netbeans.modules.editor.options;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.editor.AnnotationType;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Exceptions;
import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

/** Processor of the XML file. The result of parsing is instance of AnnotationType
 * class.
 *
 * @author  David Konecny, Petr Nejedly
 * @since 07/2001
*/
public class AnnotationTypeProcessor implements XMLDataObject.Processor, InstanceCookie {
    static final String DTD_PUBLIC_ID = "-//NetBeans//DTD annotation type 1.0//EN"; // NOI18N
    static final String DTD_SYSTEM_ID = "http://www.netbeans.org/dtds/annotation-type-1_0.dtd"; // NOI18N
    static final String DTD_PUBLIC_ID11 = "-//NetBeans//DTD annotation type 1.1//EN"; // NOI18N
    static final String DTD_SYSTEM_ID11 = "http://www.netbeans.org/dtds/annotation-type-1_1.dtd"; // NOI18N
    
    static final String TAG_TYPE = "type"; //NOI18N
    static final String ATTR_TYPE_NAME = "name"; // NOI18N
    static final String ATTR_TYPE_LOCALIZING_BUNDLE = "localizing_bundle"; // NOI18N
    static final String ATTR_TYPE_DESCRIPTION_KEY = "description_key"; // NOI18N
    static final String ATTR_TYPE_VISIBLE = "visible"; // NOI18N
    static final String ATTR_TYPE_GLYPH = "glyph"; // NOI18N
    static final String ATTR_TYPE_HIGHLIGHT = "highlight"; // NOI18N
    static final String ATTR_TYPE_FOREGROUND = "foreground"; // NOI18N
    static final String ATTR_TYPE_WAVEUNDERLINE = "waveunderline"; // NOI18N
    static final String ATTR_TYPE_TYPE = "type"; // NOI18N
    static final String ATTR_TYPE_CONTENTTYPE = "contenttype"; // NOI18N
    static final String ATTR_TYPE_ACTIONS = "actions"; // NOI18N
    static final String ATTR_ACTION_NAME = "name"; // NOI18N
    static final String TAG_COMBINATION  = "combination"; // NOI18N
    static final String ATTR_COMBINATION_TIPTEXT_KEY  = "tiptext_key"; // NOI18N
    static final String ATTR_COMBINATION_ORDER = "order"; // NOI18N
    static final String ATTR_COMBINATION_MIN_OPTIONALS = "min_optionals"; // NOI18N
    static final String TAG_COMBINE  = "combine"; // NOI18N
    static final String ATTR_COMBINE_ANNOTATIONTYPE  = "annotationtype"; // NOI18N
    static final String ATTR_COMBINE_ABSORBALL  = "absorb_all"; // NOI18N
    static final String ATTR_COMBINE_OPTIONAL  = "optional"; // NOI18N
    static final String ATTR_COMBINE_MIN  = "min"; // NOI18N
    static final String ATTR_USE_HIHGLIGHT_COLOR = "use_highlight_color"; //NOI18N
    static final String ATTR_USE_WAVE_UNDERLINE_COLOR = "use_wave_underline_color"; //NOI18N
    static final String ATTR_INHERIT_FOREGROUND_COLOR = "inherit_foreground_color"; //NOI18N
    static final String ATTR_USE_CUSTOM_SIDEBAR_COLOR = "use_custom_sidebar_color"; //NOI18N
    static final String ATTR_CUSTOM_SIDEBAR_COLOR = "custom_sidebar_color"; //NOI18N
    static final String ATTR_SEVERITY = "severity"; //NOI18N
    static final String ATTR_BROWSEABLE = "browseable"; //NOI18N
    static final String ATTR_PRIORITY = "priority"; //NOI18N
    
    private static final Logger LOG = Logger.getLogger(AnnotationTypeProcessor.class.getName());
    
    /** XML data object. */
    private FileObject xmlDataObject;
    
    /**
     * Annotation type created from XML file.
     */
    private AnnotationType  annotationType;

    /** When the XMLDataObject creates new instance of the processor,
     * it uses this method to attach the processor to the data object.
     *
     * @param xmlDO XMLDataObject
     */
    public void attachTo(XMLDataObject xmlDO) {
        xmlDataObject = xmlDO.getPrimaryFile();
    }

    //testeability:
    public void attachTo(FileObject xmlDO) {
        xmlDataObject = xmlDO;
    }

    /** Create an instance.
     * @return the instance of type {@link #instanceClass}
     * @exception java.io.IOException if an I/O error occured
     * @exception ClassNotFoundException if a class was not found
     */
    public Object instanceCreate() throws java.io.IOException, ClassNotFoundException {
        annotationType = null;
        parse();
        return annotationType;
    }
    
    /** The representation type that may be created as instances.
     * Can be used to test whether the instance is of an appropriate
     * class without actually creating it.
     *
     * @return the representation class of the instance
     */
    public Class instanceClass() {
        return AnnotationType.class;
    }
    
    /** The bean name for the instance.
     * @return the name
     */
    public String instanceName() {
        return instanceClass().getName();
    }

    private synchronized AnnotationType parse() {
        if (annotationType == null) {
            AnnotationType at = new AnnotationType();
            Handler h = new Handler(at);

            try {
		Parser xp;
                SAXParserFactory factory = SAXParserFactory.newInstance ();
                factory.setValidating (false);
                factory.setNamespaceAware(false);
                xp = factory.newSAXParser ().getParser ();
                xp.setEntityResolver(h);
                xp.setDocumentHandler(h);
                xp.setErrorHandler(h);
                xp.parse(new InputSource(xmlDataObject.getInputStream()));
                at.putProp(AnnotationType.PROP_FILE, xmlDataObject);
                annotationType = at;
            } catch (Exception e) { 
                LOG.warning("Corrupted xmlDataObject " + xmlDataObject.getPath());
                Exceptions.printStackTrace(e);
            }

        }
        return annotationType;
    }
    
    private static class Handler extends HandlerBase {
        private AnnotationType at;
        private int depth = 0;
        private List combinations;
        
        Handler(AnnotationType at) {
            this.at=at;
        }

	private void rethrow(Exception e) throws SAXException {
            throw new SAXException(e);
	}

        public void startElement(String name, AttributeList amap) throws SAXException {
            switch (depth++) {
                case 0:
                    if (! TAG_TYPE.equals(name)) {
                        throw new SAXException("malformed AnnotationType xml file"); // NOI18N
                    }
                    // basic properties
                    at.setName(amap.getValue(ATTR_TYPE_NAME));
                    if (amap.getValue(ATTR_TYPE_TYPE) == null)
                        at.setWholeLine(true);
                    else
                        at.setWholeLine("line".equals(amap.getValue(ATTR_TYPE_TYPE))); // NOI18N

                    // localization stuff
                    if (amap.getValue(ATTR_TYPE_VISIBLE) == null)
                        at.setVisible(true);
                    else
                        at.setVisible(amap.getValue(ATTR_TYPE_VISIBLE));
                    if (at.isVisible()) {
                        String localizer = amap.getValue(ATTR_TYPE_LOCALIZING_BUNDLE);
                        String key = amap.getValue(ATTR_TYPE_DESCRIPTION_KEY);
                        at.putProp(AnnotationType.PROP_LOCALIZING_BUNDLE, localizer);
                        at.putProp(AnnotationType.PROP_DESCRIPTION_KEY, key);
                    }

                    // boolean checkboxes
                    String useHighlightString = amap.getValue(ATTR_USE_HIHGLIGHT_COLOR);
                    String useWaveUnderlineString = amap.getValue(ATTR_USE_WAVE_UNDERLINE_COLOR);
                    String inheritForeString = amap.getValue(ATTR_INHERIT_FOREGROUND_COLOR);
                    String useCustomSidebarColor = amap.getValue(ATTR_USE_CUSTOM_SIDEBAR_COLOR);
                    at.setUseHighlightColor(Boolean.valueOf(useHighlightString).booleanValue());
                    at.setUseWaveUnderlineColor(Boolean.valueOf(useWaveUnderlineString).booleanValue());
                    at.setInheritForegroundColor(Boolean.valueOf(inheritForeString).booleanValue());
                    at.setUseCustomSidebarColor(Boolean.valueOf(useCustomSidebarColor).booleanValue());
                    
                    // colors
                    try {
                        String color = amap.getValue(ATTR_TYPE_HIGHLIGHT);
                        if (color != null) {
                            at.setHighlight(Color.decode(color));
                            if (useHighlightString == null) at.setUseHighlightColor(true);
                        } else {
                            if (useHighlightString == null) at.setUseHighlightColor(false);
                        }

                        color = amap.getValue(ATTR_TYPE_FOREGROUND);
                        if (color != null) {
                            at.setForegroundColor(Color.decode(color));
                            if (inheritForeString == null) at.setInheritForegroundColor(false);
                        } else {
                            if (inheritForeString == null) at.setInheritForegroundColor(true);
                        }

                        color = amap.getValue(ATTR_TYPE_WAVEUNDERLINE);
                        if (color != null) {
                            at.setWaveUnderlineColor(Color.decode(color));
                            if (useWaveUnderlineString == null) at.setUseWaveUnderlineColor(true);
                        } else {
                            if (useWaveUnderlineString == null) at.setUseWaveUnderlineColor(false);
                        }
                        color = amap.getValue(ATTR_CUSTOM_SIDEBAR_COLOR);
                        if (color != null) {
                            at.setCustomSidebarColor(Color.decode(color));
                            if (useCustomSidebarColor == null) at.setUseCustomSidebarColor(true);
                        } else {
                            if (useCustomSidebarColor  == null) at.setUseCustomSidebarColor(false);
                        }
                    } catch (NumberFormatException ex) {
                        rethrow(ex);
                    }

                    // glyph
                    try {
                        String uri = amap.getValue(ATTR_TYPE_GLYPH);
                        if (uri != null) {
                            at.setGlyph(new URL(uri));
                        }
                    } catch (MalformedURLException ex) {
                        rethrow(ex);
                    }

                    // actions
                    String actions = amap.getValue(ATTR_TYPE_ACTIONS);
                    if (actions != null) {
                        AnnotationTypeActionsFolder.readActions(at, actions);
                        at.putProp(AnnotationType.PROP_ACTIONS_FOLDER, actions);
                    }
                    
                    //extended properties:
                    at.setSeverity(AnnotationType.Severity.valueOf(amap.getValue(ATTR_SEVERITY)));
                    at.setBrowseable(Boolean.valueOf(amap.getValue(ATTR_BROWSEABLE)).booleanValue());
                    
                    String priorityString = amap.getValue(ATTR_PRIORITY);
                    int priority = 0;
                    
                    if (priorityString != null) {
                        try {
                            priority = Integer.parseInt(priorityString);
                        } catch (NumberFormatException e) {
                            Logger.getLogger("global").log(Level.INFO,null, e);
                        }
                    }
                    
                    at.setPriority(priority);
                    
                    break;
                    
                case 1: // <combination ...
                   if (! TAG_COMBINATION.equals(name)) {
                        throw new SAXException("malformed AnnotationType xml file"); // NOI18N
                   }
                   
                   combinations = new ArrayList();
                   
                   String key = amap.getValue(ATTR_COMBINATION_TIPTEXT_KEY);
                   if (key != null) {
                       at.putProp(AnnotationType.PROP_COMBINATION_TOOLTIP_TEXT_KEY, key);
                   }
                   
                   String order = amap.getValue(ATTR_COMBINATION_ORDER);
                   if (order != null)
                       at.setCombinationOrder(order);

                   String min = amap.getValue(ATTR_COMBINATION_MIN_OPTIONALS);
                   if (min != null)
                       at.setMinimumOptionals(min);

                   break;
                   
                 case 2: // <combine ...
                     combinations.add(new AnnotationType.CombinationMember(
                        amap.getValue(ATTR_COMBINE_ANNOTATIONTYPE),
                        amap.getValue(ATTR_COMBINE_ABSORBALL) == null ? false : "true".equals(amap.getValue(ATTR_COMBINE_ABSORBALL)) ? true : false, // NOI18N
                        amap.getValue(ATTR_COMBINE_OPTIONAL) == null ? false : "true".equals(amap.getValue(ATTR_COMBINE_OPTIONAL)) ? true : false, // NOI18N
                        amap.getValue(ATTR_COMBINE_MIN)
                     ));

                     break;
                default:
                    throw new SAXException("malformed AnnotationType xml file"); // NOI18N
            }
        }
        
        public void endElement(String name) throws SAXException {
            if (--depth == 1) {
                AnnotationType.CombinationMember[] combs = new AnnotationType.CombinationMember[combinations.size()];
                combinations.toArray(combs);
                at.setCombinations(combs);
            };
        }
        
        public InputSource resolveEntity(java.lang.String pid,java.lang.String sid) throws SAXException   {
            if (DTD_PUBLIC_ID.equals(pid)) {
                return new InputSource(new ByteArrayInputStream(new byte[0]));
            }            
            if (DTD_PUBLIC_ID11.equals(pid)) {
                return new InputSource(new ByteArrayInputStream(new byte[0]));
            }            
            return new InputSource (sid);            
        }

    }
    
}
