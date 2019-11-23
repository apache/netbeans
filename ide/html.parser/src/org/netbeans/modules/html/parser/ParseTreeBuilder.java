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
package org.netbeans.modules.html.parser;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Stack;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import nu.validator.htmlparser.annotation.Local;
import nu.validator.htmlparser.annotation.NsUri;
import nu.validator.htmlparser.common.TransitionHandler;
import nu.validator.htmlparser.impl.CoalescingTreeBuilder;
import nu.validator.htmlparser.impl.ElementName;
import nu.validator.htmlparser.impl.HtmlAttributes;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Named;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.openide.util.Lookup;
import static org.netbeans.modules.html.parser.ElementsFactory.*;
import static nu.validator.htmlparser.impl.Tokenizer.*;
import nu.validator.htmlparser.impl.TreeBuilder;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.xml.sax.SAXException;

/**
 * An implementation of {@link TreeBuilder} building a tree of {@link Node}s
 *
 * In contrary to the default implementations of the {@link TreeBuilder} this
 * builder also puts end tags to the tree of nodes.
 *
 * @author marekfukala
 */
public class ParseTreeBuilder extends CoalescingTreeBuilder<Named> implements TransitionHandler {

    //use NodeTreeBuilder.setLoggerLevel(Level.FINE);
    static final Logger LOGGER = Logger.getLogger(ParseTreeBuilder.class.getName());
    static boolean LOG, LOG_FINER;

    static {
        initLogLevels();
    }

    private static void initLogLevels() {
        LOG = LOGGER.isLoggable(Level.FINE);
        LOG_FINER = LOGGER.isLoggable(Level.FINER);
    }

    private final ElementsFactory factory;
    private Root root;
    //element's internall offsets
    private int offset;
    private int tag_lt_offset;
    private int data_section_start = -1;
    private boolean self_closing_starttag;
    //stack of opened tags
    private Stack<ModifiableOpenTag> stack = new Stack<ModifiableOpenTag>();
    //stack of encountered end tags
    LinkedList<ModifiableCloseTag> physicalEndTagsQueue = new LinkedList<ModifiableCloseTag>();
    private ElementName startTag;
    //holds found attributes of an open tag
    private Stack<AttrInfo> attrs = new Stack<AttrInfo>();

    private ModifiableOpenTag currentOpenTag;
    private ModifiableCloseTag currentCloseTag;
    
    private final CharSequence sourceCode;

    private boolean ADD_TEXT_NODES = false;
    
    public ParseTreeBuilder(CharSequence sourceCode, Lookup lookup) {
        this.sourceCode = sourceCode;
        factory = new ElementsFactory(sourceCode);
        root = factory.createRoot();
        
        //want text nodes?
        Properties properties = lookup.lookup(Properties.class);
        if(properties != null) {
            ADD_TEXT_NODES = Boolean.parseBoolean(properties.getProperty("add_text_nodes"));//NOI18N
        }
    }

    public Node getRoot() {
        return root;
    }

    public Node getCurrentNode() {
        return stack.peek();
    }

    int getOffset() {
        return offset;
    }
    
    private boolean isVirtual(Named named) {
        return named instanceof VirtualOpenTag;
    }
    
    @Override
    protected void elementPopped(String namespace, String name, Named t) throws SAXException {
        if (LOG) {
            LOGGER.fine(String.format("- %s %s; stack: %s", t, isVirtual(t) ? "[virtual]" : "", dumpStack())); //NOI18N
        }
        
        ModifiableOpenTag openTag = (ModifiableOpenTag)t;

        //normally the stack.pop() == t, but under some circumstances when the code is broken
        //this doesn't need to be true. In such case drop all the nodes from top
        //of the stack until we find t node.
        ModifiableOpenTag top = null;
        Stack<ModifiableOpenTag> removedFromStack = new Stack<ModifiableOpenTag>();
        while(!stack.isEmpty()) {
            top = stack.pop();
            removedFromStack.push(top);
            if(top == t) {
                break;
            }
        }
        if(t != top) {
            //weird, there doesn't seem to be the 't' node pushed
            //better put all the removed nodes back to the stack
            LOGGER.info(String.format("The node %s has been popped but not previously pushed!", t));
            while(!removedFromStack.isEmpty()) {
                stack.push(removedFromStack.pop());
            }
        }

        assert !stack.isEmpty();

        ModifiableCloseTag match = null;
        for (ModifiableCloseTag n : physicalEndTagsQueue) {
            if (LexerUtils.equals(n.name(), t.name(), true, false)) {
                match = n;
                break;
            }
        }

        if (match != null) {
            //remove all until the found element
            List<ModifiableCloseTag> toremove = physicalEndTagsQueue.subList(0, physicalEndTagsQueue.indexOf(match) + 1);

            if (toremove.size() > 1) {
                //there are some stray end tags, add them to the current open tag node
                for (ModifiableCloseTag n : toremove.subList(0, toremove.size() - 1)) {
                    openTag.addChild(n);
                }
            }
            //and remove all the end tags
            toremove.clear();

            //add the end tag node to its parent
            if (!stack.isEmpty()) {
                stack.peek().addChild(match);
            }

            //set matching node
            openTag.setMatchingCloseTag(match);
            match.setMatchingOpenTag(openTag);

            //set logical end of the paired open tag
            int match_end = match.to();
            if(match_end == -1) {
                //the close delimiter not yet lexed so no tag end offset set yet!
                //woraround - compute the end offset
                match_end = match.from() + match.name().length() + 2 /* "/>".length() */;
            }
            openTag.setSemanticEndOffset(match_end);
        } else {
            //no match found, the open tag node's logical range should be set to something meaningful -
            //to the latest end tag found likely causing this element to be popped
            CloseTag latestEndTag = physicalEndTagsQueue.peek();
            if(latestEndTag != null) {
                if(latestEndTag.from() != -1) {
                    openTag.setSemanticEndOffset(latestEndTag.from());
                }
            } else if(startTag != null) {
                if(tag_lt_offset != -1) {
                    //or to an open tag which implies this tag to be closed
                    openTag.setSemanticEndOffset(tag_lt_offset);
                }
            } else {
                //the rest - simply current token offset
                openTag.setSemanticEndOffset(offset);
            }

        }

        if (stack.size() == 1 /* only root tag in the stack */ && !physicalEndTagsQueue.isEmpty()) {
            //there are no nodes on the stack, but there are some physical endtags left
            if (LOG) {
                LOGGER.fine(String.format("LEFT in stack of end tags: %s", dumpEndTags()));//NOI18N
            }
            //attach all the stray end tags to the currently popped node
            for (ListIterator<ModifiableCloseTag> leftEndTags = physicalEndTagsQueue.listIterator(); leftEndTags.hasNext();) {
                ModifiableCloseTag left = leftEndTags.next();
                openTag.addChild(left);
                leftEndTags.remove();
            }


        }

        super.elementPopped(namespace, name, t);
    }

    @Override
    protected void elementPushed(String namespace, String name, Named t) throws SAXException {

        if (LOG) {
            LOGGER.fine(String.format("+ %s %s; stack: %s", t, isVirtual(t) ? "[virtual]" : "", dumpStack())); //NOI18N
        }
        
        ModifiableOpenTag openTag = (ModifiableOpenTag)t;

        stack.push(openTag);

        //stray end tags - add them to the current node
        if (!isVirtual(t)) {
            ModifiableCloseTag head;
            while ((head = physicalEndTagsQueue.poll()) != null) {
                stack.peek().addChild(head);
            }
        }

        super.elementPushed(namespace, name, t);
    }

    private String dumpStack() {
        return collectionOfNodesToString(stack);
    }

    private String dumpEndTags() {
        return collectionOfNodesToString(physicalEndTagsQueue);
    }

    private String collectionOfNodesToString(Collection<? extends Named> col) {
        StringBuilder b = new StringBuilder();
        b.append('[');
        for (Iterator<? extends Named> i = col.iterator(); i.hasNext();) {
            Named en = i.next();
            b.append(en.name());
            b.append(", ");
        }
        b.append(']');
        return b.toString();

    }

    @Override
    public void transition(int from, int to, boolean reconsume, int offset) throws SAXException {
        if (LOG_FINER) {
            LOGGER.finer(String.format("%s -> %s at %s", Util.TOKENIZER_STATE_NAMES[from], Util.TOKENIZER_STATE_NAMES[to], offset));//NOI18N
        }
        this.offset = offset;
        int tag_gt_offset = -1;
        
        if(ADD_TEXT_NODES) {
            switch(from) {
                case DATA:
                case RCDATA:
                case SCRIPT_DATA:
                        if(data_section_start != -1) {
                            stack.peek().addChild(factory.createText(data_section_start, offset));
                            data_section_start = -1;
                        }
                    break;
            }
        }
        
        switch (to) {
            case SELF_CLOSING_START_TAG:
                if (LOG_FINER) {
                    LOGGER.finer("Set self closing start tag flag.");//NOI18N
                }
                self_closing_starttag = true;
                break;
            case TAG_OPEN:
                tag_lt_offset = offset;
                break;

            case NON_DATA_END_TAG_NAME:
                if(from == RAWTEXT_RCDATA_LESS_THAN_SIGN
                        || from == SCRIPT_DATA_LESS_THAN_SIGN) {
                    //end tag in RAW text (like <title> content)
                    tag_lt_offset = offset - 1; //-1 is here because we are already at the tag name just after the &lt; char
                }
                break;
                
            case RAWTEXT:
                //strange transition happening at the closing > char at the tag end:
                //<style type=\"text/css\"> 
                switch(from) {
                    case ATTRIBUTE_VALUE_UNQUOTED:
                    case AFTER_ATTRIBUTE_VALUE_QUOTED:
                    case BEFORE_ATTRIBUTE_NAME:
                    case TAG_NAME:
                        tag_gt_offset = offset + 1;
                        break;
                }                
                break;

            case RCDATA:
            case SCRIPT_DATA:
            case DATA:
                switch (from) {
                    case ATTRIBUTE_NAME:
                    case AFTER_ATTRIBUTE_VALUE_QUOTED:
                    case AFTER_ATTRIBUTE_NAME:
                    case TAG_NAME:
                    case BEFORE_ATTRIBUTE_NAME:
                    case BEFORE_ATTRIBUTE_VALUE:
                    case ATTRIBUTE_VALUE_UNQUOTED:
                    case NON_DATA_END_TAG_NAME:
                    case SELF_CLOSING_START_TAG:
                        //+1 ... add the > char itself
                        tag_gt_offset = offset + 1;
                        break;

                }
                break;

            case ATTRIBUTE_NAME:
                switch (from) {
                    case BEFORE_ATTRIBUTE_NAME:
                    case AFTER_ATTRIBUTE_NAME: //empty attributes w/o value
                        //switching to attribute name
                        AttrInfo ainfo = new AttrInfo();
                        attrs.push(ainfo);
                        ainfo.nameOffset = offset;
                        
                         if (LOG_FINER) {
                            LOGGER.log(Level.FINER, String.format("pushed attribute %s", ainfo));
                         }
                        break;
                }
                break;
                
            case BEFORE_ATTRIBUTE_VALUE:
                switch (from) {
                    case ATTRIBUTE_NAME:
                        attrs.peek().equalSignOffset = offset;
                        break;
                }
                break;

            case ATTRIBUTE_VALUE_DOUBLE_QUOTED:
                if(from == BEFORE_ATTRIBUTE_VALUE) {
                    attrs.peek().valueQuotationType = AttrInfo.ValueQuotation.DOUBLE;
                    attrs.peek().valueOffset = offset;
                }
                break;
            case ATTRIBUTE_VALUE_SINGLE_QUOTED:
                if(from == BEFORE_ATTRIBUTE_VALUE) {
                    attrs.peek().valueQuotationType = AttrInfo.ValueQuotation.SINGLE;
                    attrs.peek().valueOffset = offset;
                }
                break;
            case ATTRIBUTE_VALUE_UNQUOTED:
                if(from == BEFORE_ATTRIBUTE_VALUE) {
                    attrs.peek().valueQuotationType = AttrInfo.ValueQuotation.NONE;
                    attrs.peek().valueOffset = offset;
                }
                break;
                

        }

        data_section_start = tag_gt_offset;
        
        //set the current tag end offset:
        //the transition for the closing tag symbol are done AFTER the element for the tag is created,
        //so it needs to be additionaly set to the latest element
        if(tag_gt_offset != -1) {
            
            if(currentOpenTag != null) {
                currentOpenTag.setEndOffset(tag_gt_offset);
                currentOpenTag.setSemanticEndOffset(tag_gt_offset);
            }
            
            if(currentCloseTag != null) {
                currentCloseTag.setEndOffset(tag_gt_offset);
                //refresh the matching open tag's logical end offset
                OpenTag match = currentCloseTag.matchingOpenTag();
                if(match != null) {
                    ((ModifiableOpenTag)match).setSemanticEndOffset(tag_gt_offset);
                }
            }

            //always remove both 
            currentOpenTag = null;
            currentCloseTag = null;
        }

    }

    @Override
    public void startTag(ElementName en, HtmlAttributes ha, boolean bln) throws SAXException {
        if (LOG) {
            LOGGER.fine(String.format("open tag %s at %s", en.getName(), tag_lt_offset));//NOI18N
        }
        
        startTag = en;
        super.startTag(en, ha, bln);
        startTag = null;
    }

    @Override
    public void endTag(ElementName en) throws SAXException {
        if (LOG) {
            LOGGER.fine(String.format("close tag %s at %s", en.getName(), tag_lt_offset));//NOI18N
        }

        ModifiableCloseTag closeTag = factory.createCloseTag(tag_lt_offset, -1, (byte)en.getName().length());
        currentCloseTag = closeTag;
        physicalEndTagsQueue.add(closeTag);

        if (LOG) {
            LOGGER.fine(String.format("end tags: %s", dumpEndTags()));//NOI18N
        }

        super.endTag(en);
    }

    private void resetInternalPositions() {
        tag_lt_offset = -1;
        self_closing_starttag = false;
        attrs.clear();
        if (LOG) {
            LOGGER.fine("Internal state reset.");//NOI18N
        }
    }

    @Override
    protected void appendCharacters(Named t, String string) throws SAXException {
        //no-op
    }

    @Override
    protected void appendComment(Named t, String string) throws SAXException {
        //no-op
    }

    @Override
    protected void appendCommentToDocument(String string) throws SAXException {
        //no-op
    }

    @Override
    protected void insertFosterParentedCharacters(String string, Named t, Named t1) throws SAXException {
        //???????
    }

    @Override
    protected Named createAndInsertFosterParentedElement(String ns, String name,
            HtmlAttributes attributes, Named table, Named stackParent) throws SAXException {
        if (LOG) {
            LOGGER.fine(String.format("createElement(%s)", name));//NOI18N
        }

        ModifiableOpenTag node;
        if (startTag != null && startTag.getName().equals(name)) {

            if (self_closing_starttag) {
                currentOpenTag = node = factory.createEmptyOpenTag(tag_lt_offset, -1, (byte) name.length());
            } else {
                currentOpenTag = node = factory.createOpenTag(tag_lt_offset, -1, (byte) name.length());
            }
            addAttributesToElement(node, attributes);
            resetInternalPositions();

        } else {
            //virtual element
            node = factory.createVirtualOpenTag(name);
            addAttributesToElement(node, attributes);
        }

        ((ModifiableOpenTag) stackParent).addChild(node);

        return node;
    }

    //create open tag element
    @Override
    protected Named createElement(String namespace, String name, HtmlAttributes attributes, Named t) throws SAXException {
        if(LOG) {
            LOGGER.fine(String.format("createElement(%s)", name));//NOI18N
        }

        ModifiableOpenTag node;
        if (startTag != null && startTag.getName().equals(name)) {
            
            if(self_closing_starttag) {
                currentOpenTag = node = factory.createEmptyOpenTag(tag_lt_offset, -1, (byte)name.length());
            } else {
                currentOpenTag = node = factory.createOpenTag(tag_lt_offset, -1, (byte)name.length());
            }
            addAttributesToElement(node, attributes);
            resetInternalPositions();

        } else {
            //virtual element
            node = factory.createVirtualOpenTag(name);
            addAttributesToElement(node, attributes);
        }

        return node;
    }

    @Override
    protected Named createHtmlElementSetAsRoot(HtmlAttributes attributes) throws SAXException {
        if (LOG) {
            LOGGER.fine("createHtmlElementSetAsRoot()");//NOI18N
        }

        Named rootTag = createElement("http://www.w3.org/1999/xhtml", "html", attributes, null);//NOI18N
        stack.push(root);

        root.addChild(rootTag);

        return rootTag;
    }

    @Override
    protected void detachFromParent(Named node) throws SAXException {
        ((ModifiableElement)node).detachFromParent();
    }

    @Override
    protected boolean hasChildren(Named node) throws SAXException {
        OpenTag ot = (OpenTag)node;
        return !ot.children().isEmpty();
    }

    @Override
    protected void appendElement(Named child, Named parent) throws SAXException {
        ((ModifiableOpenTag)parent).addChild(child);
    }

    @Override
    //move node's children to another node
    protected void appendChildrenToNewParent(Named from, Named to) throws SAXException {
        Collection<Element> children = ((OpenTag)from).children();
        ((ModifiableOpenTag)from).removeChildren(children);
        ((ModifiableOpenTag)to).addChildren(children);
    }

    //http://www.whatwg.org/specs/web-apps/current-work/multipage/tokenization.html#foster-parenting
    @Override
    protected void insertFosterParentedChild(Named child, Named table, Named stackParent) throws SAXException {
        Node parent = table.parent();
        if (parent != null) { // always an element if not null
            ((ModifiableOpenTag)parent).insertChildBefore(child, table);
        } else {
            ((ModifiableOpenTag)stackParent).addChild(child);
        }
    }

    @Override
    protected void addAttributesToElement(Named node, HtmlAttributes attributes) throws SAXException {
        ModifiableOpenTag mot = (ModifiableOpenTag)node;
        //there are situations (when the code is corrupted) when 
        //the attributes recorded during lexing (lexical states switching)
        //do not contain all the attrs from HtmlAttributes.
        int attrs_count = Math.min(attributes.getLength(), attrs.size());

        for (int i = 0; i < attrs_count; i++) {
            //XXX I assume the attributes order is the same as in the source code
            AttrInfo attrInfo = attrs.elementAt(i);
            
            //check the name offset
            if(attrInfo.nameOffset == -1 || attrInfo.nameOffset < node.from()) {
                continue; //bad name offset
            }
            
            //note: attrInfo.equalSignOffset and attrInfo.valueOffset can be null
            //for attribute w/o a value
            
            String attributeName = attributes.getLocalName(i);
            int attributeNameLength = attributeName.length();
            
            //check attribute name length
            int attrNameEndOffset = attrInfo.nameOffset + attributeNameLength;
            if(attrNameEndOffset > sourceCode.length()) {
                continue; //bad attribute
            }
            
            Attribute attr;
            if(attrInfo.valueOffset == -1) {
                //no value
                attr = factory.createAttribute(attrInfo.nameOffset, (short)attributeNameLength);
            } else {
                int attributeValueLength = attributes.getValue(i).length() 
                        + (attrInfo.valueQuotationType == AttrInfo.ValueQuotation.NONE ? 0 : 2);
                attr = factory.createAttribute(
                    attrInfo.nameOffset,
                    attrInfo.valueOffset,
                    (short)attributeNameLength,
                    attributeValueLength);
            }
            mot.setAttribute(attr);
        }
    }
   
    //for unit tests
    static void setLoggerLevel(Level level) {
        LOGGER.setLevel(level);
        LOGGER.addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                System.out.println(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }

        });
        initLogLevels();
    }

    private static class AttrInfo {

        public int nameOffset = -1;
        public int equalSignOffset = -1;
        public int valueOffset = -1;
        
        public ValueQuotation valueQuotationType;

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("AttrInfo[")
                    .append("nameOffset=")
                    .append(nameOffset)
                    .append(",equalSignOffset=")
                    .append(equalSignOffset)
                    .append(",valueOffset=")
                    .append(valueOffset)
                    .append("]")
                    .toString();
        }

        private enum ValueQuotation {

            NONE, SINGLE, DOUBLE;
        }
    }
}
