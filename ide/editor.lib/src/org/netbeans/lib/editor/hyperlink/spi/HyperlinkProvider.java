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

package org.netbeans.lib.editor.hyperlink.spi;

import javax.swing.text.Document;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * This interface should be implemented by anyone who whats to provide hyperlinking
 * functionality in the source code.
 * <br>
 * There should be one provider instance per mime-type.
 * Its methods are called for all the opened editors of the given mime-type
 * where the hyperlinking functionality gets requested.
 *
 * <p>
 * The providers need to be registered.
 * For NetBeans IDE, the default approach is to use System FileSystem.
 * <br>
 * The HyperlinkProvider(s) should be registered as ".instance" objects under
 * <code>Editors/&lt;mime-type&gt;/HyperlinkProviders</code> directory.
 * </p>
 * 
 * <p>
 * Please see {@link org.netbeans.lib.editor.hyperlink.HyperlinkProviderManager}
 * for more details.
 * </p>
 *
 * <p>
 * Note: there is no assurance on the order of calling of the methods in this class.
 * The callers may call the methods in any order and even do not call some of these methods
 * at all.
 * </p>
 * 
 * <p><b>Related documentation</b>
 * 
 * <p><a href="http://platform.netbeans.org/tutorials/60/nbm-hyperlink.html">NetBeans Hyperlink Navigation Tutorial</a>
 *
 * @author Jan Lahoda
 * @since 1.0
 */
@MimeLocation(subfolderName="HyperlinkProviders")
public interface HyperlinkProvider {
    
    /**
     * Should determine whether there should be a hyperlink on the given offset
     * in the given document. May be called any number of times for given parameters.
     * <br>
     * This method is called from event dispatch thread.
     * It should run very fast as it is called very often.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     * @return true if the provided offset should be in a hyperlink
     *         false otherwise
     */
    boolean isHyperlinkPoint(Document doc, int offset);
    
    /**
     * Should determine the span of hyperlink on given offset. Generally, if
     * isHyperlinkPoint returns true for a given parameters, this class should
     * return a valid span, but it is not strictly required.
     * <br>
     * This method is called from event dispatch thread.
     * This method should run very fast as it is called very often.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     * @return a two member array which contains starting and ending offset of a hyperlink
     *         that should be on a given offset
     */
    int[] getHyperlinkSpan(Document doc, int offset);
    
    /**
     * The implementor should perform an action
     * corresponding to clicking on the hyperlink on the given offset. The
     * nature of the action is given by the nature of given hyperlink, but
     * generally should open some resource or move cursor
     * to certain place in the current document.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     */
    void performClickAction(Document doc, int offset);
    
}
