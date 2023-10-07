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

package org.netbeans.modules.maven.execute.model.io.jdom;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jdom2.Content;
import org.jdom2.DefaultJDOMFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;

/**
 * Class NetbeansBuildActionJDOMWriter.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings({"unchecked", "deprecated", "rawtypes"}) //a generated class
public class NetbeansBuildActionJDOMWriter {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field factory.
     */
    private DefaultJDOMFactory factory;

    /**
     * Field lineSeparator.
     */
    private String lineSeparator;


      //----------------/
     //- Constructors -/
    //----------------/

    public NetbeansBuildActionJDOMWriter() {
        factory = new DefaultJDOMFactory();
        lineSeparator = "\n";
    } //-- org.netbeans.modules.maven.execute.model.io.jdom.NetbeansBuildActionJDOMWriter()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method findAndReplaceProperties.
     * 
     * @param counter
     * @param props
     * @param name
     * @param parent
     * @return Element
     */
    protected Element findAndReplaceProperties(Counter counter, Element parent, String name, Map props)
    {
        boolean shouldExist = props != null && ! props.isEmpty();
        Element element = updateElement(counter, parent, name, shouldExist);
        if (shouldExist) {
            Iterator it = props.keySet().iterator();
            Counter innerCounter = new Counter(counter.getDepth() + 1);
            while (it.hasNext()) {
                String key = (String) it.next();
                findAndReplaceSimpleElement(innerCounter, element, key, (String)props.get(key), null);
                }
            ArrayList lst = new ArrayList(props.keySet());
            it = element.getChildren().iterator();
            while (it.hasNext()) {
                Element elem = (Element) it.next();
                String key = elem.getName();
                if (!lst.contains(key)) {
                    it.remove();
                }
            }
        }
        return element;
    } //-- Element findAndReplaceProperties(Counter, Element, String, Map) 

    /**
     * Method findAndReplaceSimpleElement.
     * 
     * @param counter
     * @param defaultValue
     * @param text
     * @param name
     * @param parent
     * @return Element
     */
    protected Element findAndReplaceSimpleElement(Counter counter, Element parent, String name, String text, String defaultValue)
    {
        if (defaultValue != null && text != null && defaultValue.equals(text)) {
            Element element =  parent.getChild(name, parent.getNamespace());
            // if exist and is default value or if doesn't exist.. just keep the way it is..
            if ((element != null && defaultValue.equals(element.getText())) || element == null) {
                return element;
            }
        }
        boolean shouldExist = text != null; // && text.trim().length() > 0;
        Element element = updateElement(counter, parent, name, shouldExist);
        if (shouldExist) {
            element.setText(text);
        }
        return element;
    } //-- Element findAndReplaceSimpleElement(Counter, Element, String, String, String) 

    /**
     * Method findAndReplaceSimpleLists.
     * 
     * @param counter
     * @param childName
     * @param parentName
     * @param list
     * @param parent
     * @return Element
     */
    protected Element findAndReplaceSimpleLists(Counter counter, Element parent, java.util.Collection list, String parentName, String childName)
    {
        boolean shouldExist = list != null && list.size() > 0;
        Element element = updateElement(counter, parent, parentName, shouldExist);
        if (shouldExist) {
            Iterator it = list.iterator();
            Iterator elIt = element.getChildren(childName, element.getNamespace()).iterator();
            if (! elIt.hasNext()) elIt = null;
            Counter innerCount = new Counter(counter.getDepth() + 1);
            while (it.hasNext()) {
                String value = (String) it.next();
                Element el;
                if (elIt != null && elIt.hasNext()) {
                    el = (Element) elIt.next();
                    if (! elIt.hasNext()) elIt = null;
                } else {
                    el = factory.element(childName, element.getNamespace());
                    insertAtPreferredLocation(element, el, innerCount);
                }
                el.setText(value);
                innerCount.increaseCount();
            }
            if (elIt != null) {
                while (elIt.hasNext()) {
                    elIt.next();
                    elIt.remove();
                }
            }
        }
        return element;
    } //-- Element findAndReplaceSimpleLists(Counter, Element, java.util.Collection, String, String) 

    /**
     * Method findAndReplaceXpp3DOM.
     * 
     * @param counter
     * @param dom
     * @param name
     * @param parent
     * @return Element
     */
    protected Element findAndReplaceXpp3DOM(Counter counter, Element parent, String name, Xpp3Dom dom)
    {
        boolean shouldExist = dom != null && (dom.getChildCount() > 0 || dom.getValue() != null);
        Element element = updateElement(counter, parent, name, shouldExist);
        if (shouldExist) {
            replaceXpp3DOM(element, dom, new Counter(counter.getDepth() + 1));
        }
        return element;
    } //-- Element findAndReplaceXpp3DOM(Counter, Element, String, Xpp3Dom) 

    /**
     * Method insertAtPreferredLocation.
     * 
     * @param parent
     * @param counter
     * @param child
     */
    protected void insertAtPreferredLocation(Element parent, Element child, Counter counter)
    {
        int contentIndex = 0;
        int elementCounter = 0;
        Iterator it = parent.getContent().iterator();
        Text lastText = null;
        int offset = 0;
        while (it.hasNext() && elementCounter /* http://jira.codehaus.org/browse/MODELLO-257 */< counter.getCurrentIndex()) {
            Object next = it.next();
            offset = offset + 1;
            if (next instanceof Element) {
                elementCounter = elementCounter + 1;
                contentIndex = contentIndex + offset;
                offset = 0;
            }
            if (next instanceof Text && it.hasNext()) {
                lastText = (Text)next;
            }
        }
        if (lastText != null && lastText.getTextTrim().length() == 0) {
            lastText = (Text)lastText.clone();
        } else {
            String starter = lineSeparator;
            for (int i = 0; i < counter.getDepth(); i++) {
                starter = starter + "    "; //TODO make settable?
            }
            lastText = factory.text(starter);
        }
        if (parent.getContentSize() == 0) {
            Text finalText = (Text)lastText.clone();
            finalText.setText(finalText.getText().substring(0, finalText.getText().length() - "    ".length()));
            parent.addContent(contentIndex, finalText);
        }
        parent.addContent(contentIndex, child);
        parent.addContent(contentIndex, lastText);
    } //-- void insertAtPreferredLocation(Element, Element, Counter) 

    /**
     * Method iterate2NetbeansActionMapping.
     * 
     * @param counter
     * @param childTag
     * @param list
     * @param parent
     */
    protected void iterate2NetbeansActionMapping(Counter counter, Element parent, java.util.Collection list, java.lang.String childTag)
    {
        Iterator it = list.iterator();
        Iterator elIt = parent.getChildren(childTag, parent.getNamespace()).iterator();
        if (!elIt.hasNext()) elIt = null;
        Counter innerCount = new Counter(counter.getDepth() + 1);
        while (it.hasNext()) {
            NetbeansActionMapping value = (NetbeansActionMapping) it.next();
            Element el;
            if (elIt != null && elIt.hasNext()) {
                el = (Element) elIt.next();
                if (! elIt.hasNext()) elIt = null;
            } else {
                el = factory.element(childTag, parent.getNamespace());
                insertAtPreferredLocation(parent, el, innerCount);
            }
            updateNetbeansActionMapping(value, childTag, innerCount, el);
            innerCount.increaseCount();
        }
        if (elIt != null) {
            while (elIt.hasNext()) {
                elIt.next();
                elIt.remove();
            }
        }
    } //-- void iterate2NetbeansActionMapping(Counter, Element, java.util.Collection, java.lang.String) 

    /**
     * Method replaceXpp3DOM.
     * 
     * @param parent
     * @param counter
     * @param parentDom
     */
    protected void replaceXpp3DOM(Element parent, Xpp3Dom parentDom, Counter counter)
    {
        if (parentDom.getChildCount() > 0) {
            Xpp3Dom[] childs = parentDom.getChildren();
            Collection domChilds = new ArrayList();
            domChilds.addAll(Arrays.asList(childs));
            int domIndex = 0;
            ListIterator it = parent.getChildren().listIterator();
            while (it.hasNext()) {
                Element elem = (Element) it.next();
                Iterator it2 = domChilds.iterator();
                Xpp3Dom corrDom = null;
                while (it2.hasNext()) {
                    Xpp3Dom dm = (Xpp3Dom)it2.next();
                    if (dm.getName().equals(elem.getName())) {
                        corrDom = dm;
                        break;
                    }
                }
                if (corrDom != null) {
                    domChilds.remove(corrDom);
                    replaceXpp3DOM(elem, corrDom, new Counter(counter.getDepth() + 1));
                    counter.increaseCount();
                } else {
                    parent.removeContent(elem);
                }
            }
            Iterator it2 = domChilds.iterator();
            while (it2.hasNext()) {
                Xpp3Dom dm = (Xpp3Dom) it2.next();
                Element elem = factory.element(dm.getName(), parent.getNamespace());
                insertAtPreferredLocation(parent, elem, counter);
                counter.increaseCount();
                replaceXpp3DOM(elem, dm, new Counter(counter.getDepth() + 1));
            }
        } else if (parentDom.getValue() != null) {
            parent.setText(parentDom.getValue());
        }
    } //-- void replaceXpp3DOM(Element, Xpp3Dom, Counter) 

    /**
     * Method updateActionToGoalMapping.
     * 
     * @param value
     * @param element
     * @param counter
     * @param xmlTag
     */
    @SuppressWarnings("deprecation")
    protected void updateActionToGoalMapping(ActionToGoalMapping value, String xmlTag, Counter counter, Element element)
    {
        Element root = element;
        Counter innerCount = new Counter(counter.getDepth() + 1);
        findAndReplaceSimpleElement(innerCount, root,  "packaging", value.getPackaging(), null);
        iterate2NetbeansActionMapping(innerCount, root, value.getActions(),"action");
    } //-- void updateActionToGoalMapping(ActionToGoalMapping, String, Counter, Element) 

    /**
     * Method updateElement.
     * 
     * @param counter
     * @param shouldExist
     * @param name
     * @param parent
     * @return Element
     */
    protected Element updateElement(Counter counter, Element parent, String name, boolean shouldExist)
    {
        Element element =  parent.getChild(name, parent.getNamespace());
        if (element != null && shouldExist) {
            counter.increaseCount();
        }
        if (element == null && shouldExist) {
            element = factory.element(name, parent.getNamespace());
            insertAtPreferredLocation(parent, element, counter);
            counter.increaseCount();
        }
        if (!shouldExist && element != null) {
            int index = parent.indexOf(element);
            if (index > 0) {
                Content previous = parent.getContent(index - 1);
                if (previous instanceof Text) {
                    Text txt = (Text)previous;
                    if (txt.getTextTrim().length() == 0) {
                        parent.removeContent(txt);
                    }
                }
            }
            parent.removeContent(element);
        }
        return element;
    } //-- Element updateElement(Counter, Element, String, boolean) 

    /**
     * Method updateNetbeansActionMapping.
     * 
     * @param value
     * @param element
     * @param counter
     * @param xmlTag
     */
    protected void updateNetbeansActionMapping(NetbeansActionMapping value, String xmlTag, Counter counter, Element element)
    {
        Element root = element;
        Counter innerCount = new Counter(counter.getDepth() + 1);
        findAndReplaceSimpleElement(innerCount, root,  "actionName", value.getActionName(), null);
        findAndReplaceSimpleElement(innerCount, root,  "displayName", value.getDisplayName(), null);
        findAndReplaceSimpleElement(innerCount, root,  "basedir", value.getBasedir(), null);
        findAndReplaceSimpleElement(innerCount, root,  "reactor", value.getReactor(), null);
        findAndReplaceSimpleElement(innerCount, root,  "preAction", value.getPreAction(), null);
        findAndReplaceSimpleElement(innerCount, root,  "recursive", value.isRecursive() == true ? null : String.valueOf( value.isRecursive() ), "true");
        findAndReplaceSimpleLists(innerCount, root, value.getPackagings(), "packagings", "packaging");
        findAndReplaceSimpleLists(innerCount, root, value.getGoals(), "goals", "goal");
        findAndReplaceProperties(innerCount, root,  "properties", value.getProperties());
        findAndReplaceSimpleLists(innerCount, root, value.getActivatedProfiles(), "activatedProfiles", "activatedProfile");
    } //-- void updateNetbeansActionMapping(NetbeansActionMapping, String, Counter, Element) 


    /**
     * Method write.
     * 
     * @param actions
     * @param writer
     * @param document
     * @throws java.io.IOException
     */
    public void write(ActionToGoalMapping actions, Document document, OutputStreamWriter writer)
        throws java.io.IOException
    {
        Format format = Format.getRawFormat()
        .setEncoding(writer.getEncoding())
        .setLineSeparator(System.getProperty("line.separator"));
        write(actions, document, writer, format);
    } //-- void write(ActionToGoalMapping, Document, OutputStreamWriter) 

    /**
     * Method write.
     * 
     * @param actions
     * @param jdomFormat
     * @param writer
     * @param document
     * @throws java.io.IOException
     */
    public void write(ActionToGoalMapping actions, Document document, Writer writer, Format jdomFormat)
        throws java.io.IOException
    {
        updateActionToGoalMapping(actions, "actions", new Counter(0), document.getRootElement());
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(jdomFormat);
        outputter.output(document, writer);
    } //-- void write(ActionToGoalMapping, Document, Writer, Format) 


      //-----------------/
     //- Inner Classes -/
    //-----------------/

    /**
     * Class Counter.
     * 
     * @version $Revision$ $Date$
     */
    public class Counter {


          //--------------------------/
         //- Class/Member Variables -/
        //--------------------------/

        /**
         * Field currentIndex.
         */
        private int currentIndex = 0;

        /**
         * Field level.
         */
        private int level;


          //----------------/
         //- Constructors -/
        //----------------/

        public Counter(int depthLevel) {
            level = depthLevel;
        } //-- org.netbeans.modules.maven.execute.model.io.jdom.Counter(int)


          //-----------/
         //- Methods -/
        //-----------/

        /**
         * Method getCurrentIndex.
         * 
         * @return int
         */
        public int getCurrentIndex()
        {
            return currentIndex;
        } //-- int getCurrentIndex() 

        /**
         * Method getDepth.
         * 
         * @return int
         */
        public int getDepth()
        {
            return level;
        } //-- int getDepth() 

        /**
         * Method increaseCount.
         */
        public void increaseCount()
        {
            currentIndex = currentIndex + 1;
        } //-- void increaseCount() 


}


}
