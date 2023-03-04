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
package org.netbeans.modules.xml.schema.completion.spi;

import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.editor.BaseDocument;
import org.openide.filesystems.FileObject;

/**
 * Represents the code completion context at the current cursor location. <br/><br/>
 * <b>Examples:</b>
 * <table border>
 *     <tr>
 *         <th>Completion Point</th>
 *         <th>Completion Type</th>
 *         <th>Typed Characters</th>
 *     </tr>
 *     <tr>
 *         <td><pre>&lt;my:myEl|ement...</pre></td> 
 *         <td>{@link CompletionContext.CompletionType#COMPLETION_TYPE_ELEMENT}</td> 
 *         <td><pre>my:myEl</pre></td>
 *     </tr>
 *     <tr>
 *         <td><pre>&lt;myElement&gt;myElementCont|ent&lt;/myElement&gt;</pre></td> 
 *         <td>{@link CompletionContext.CompletionType#COMPLETION_TYPE_ELEMENT_VALUE}</td>
 *         <td><pre>myElementCont</pre></td>
 *     </tr>
 *     <tr>
 *         <td><pre>&lt;myElement myAttri|bute...</pre></td>
 *         <td>{@link CompletionContext.CompletionType#COMPLETION_TYPE_ATTRIBUTE}</td> 
 *         <td><pre>myAttribute</pre></td>
 *     </tr>
 *     <tr>
 *         <td><pre>&lt;myElement myAttribute="myVal|ue...</pre></td> 
 *         <td>{@link CompletionContext.CompletionType#COMPLETION_TYPE_ATTRIBUTE_VALUE}</td>
 *         <td><pre>myValue</pre></td>
 *     </tr>
 * </table>
 * @author Samaresh (Samaresh@Netbeans.Org)
 */
public abstract class CompletionContext {
    
    /**
     * Returns the default namespace for the document.
     * @return the document's default namespace URI.
     */
    public abstract String getDefaultNamespace();
    
    /**
     * Returns the type of completion requested.
     * @return the completion type requested.
     */
    public abstract CompletionType getCompletionType();
        
    /**
     * Returns the path from root element to the cursor location.
     * @return the {@link QName qualified names} comprising the path from the
     * root element to the node requiring completion.
     */
    public abstract List<QName> getPathFromRoot();
    
    /**
     * Returns the FileObject for the document.
     * @return the document's FileObject.
     */
    public abstract FileObject getPrimaryFile();
    
    /**
     * Returns the BaseDocument for the document.
     * @return the Swing document requiring code completion.
     */
    public abstract BaseDocument getBaseDocument();
    
    /**
     * Returns all the namespaces declared in the document as a HashMap. 
     * @return the declared namespace prefixes and their respective URIs.<br/>
     * e.g.<br/> <pre>[""="http://maven.apache.org/POM/4.0.0" ,
     *  "xsi"="http://www.w3.org/2001/XMLSchema-instance"]</pre>
     */
    public abstract HashMap<String, String> getDeclaredNamespaces();
    
    /**
     * Returns the typed characters during completion.
     * @return the characters typed during code completion.
     */
    public abstract String getTypedChars();
            
    /**
     * The types of code completion that can be requested.
     */
    public static enum CompletionType {
        /**
         * Completion in a place whose type cannot be determined by the context.
         */
        COMPLETION_TYPE_UNKNOWN,
        /**
         * Completion for an <a href="http://www.w3.org/TR/xml/#NT-Attribute">
         * attribute</a>.<br/>
         * e.g.
         */
        COMPLETION_TYPE_ATTRIBUTE,
        /**
         * Completion for an 
         * <a href="http://www.w3.org/TR/xml/#NT-AttValue">attribute value</a>.
         */
        COMPLETION_TYPE_ATTRIBUTE_VALUE,
        /**
         * Completion for an 
         * <a href="http://www.w3.org/TR/xml/#NT-element">element</a>'s 
         * <a href="http://www.w3.org/TR/xml/#NT-STag">start tag</a> or 
         * <a href="http://www.w3.org/TR/xml/#NT-ETag">end tag</a>.
         */
        COMPLETION_TYPE_ELEMENT,
        /**
         * Completion for an 
         * <a href="http://www.w3.org/TR/xml/#NT-content">element's content</a>.
         */
        COMPLETION_TYPE_ELEMENT_VALUE,
        /**
         * Completion for an 
         * <a href="http://www.w3.org/TR/xml/#NT-EntityRef">entity reference</a>.
         */
        COMPLETION_TYPE_ENTITY,
        /**
         * Completion for a
         * <a href="http://www.w3.org/TR/xml/#NT-NotationDecl">NOTATION declaration</a>.
         */
        COMPLETION_TYPE_NOTATION,
        /**
         * Completion for a
         * <a href="http://www.w3.org/TR/xml/#NT-doctypedecl">DOCTYPE declaration</a>.
         */
        COMPLETION_TYPE_DTD
    }
}
