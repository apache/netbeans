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
package org.netbeans.modules.groovy.editor.api.elements;

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.openide.filesystems.FileObject;

/**
 * A logical program element, such as a Class, Method, Attribute, etc.
 * These are either backed by a Groovy AST node, or data from the persistent index.
 *
 * @author Tor Norbye
 */
public abstract class GroovyElement implements ElementHandle {

    /**
     * Name of the element where this Groovy element lies. This might be package
     * name in case of class element, class name in case of field element, etc.
     */
    protected String in;

    /** Name of this element */
    protected String name;

    /** Signature of the element*/
    protected String signature;

    /** Offset range of the element */
    protected OffsetRange offsetRange;

    public GroovyElement() {
    }

    public GroovyElement(String in) {
        this(in, null);
    }

    public GroovyElement(String in, String name) {
        this.in = in;
        this.name = name;
    }

    @Override
    public abstract ElementKind getKind();

    @Override
    public boolean signatureEquals(ElementHandle handle) {
        if (getIn().equals(handle.getIn()) &&
            getName().equals(handle.getName()) &&
            getKind().equals(handle.getKind())) {

            return true;
        }
        return false;
    }

    @Override
    public String getIn() {
        return in;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns signature of this element.
     * <p>
     * The signature is formed as {@code in.name}.
     * </p>
     * @return signature of this element
     */
    public String getSignature() {
        if (signature == null) {
            StringBuilder sb = new StringBuilder();
            String clz = getIn();
            if (clz != null && clz.length() > 0) {
                sb.append(clz);
                sb.append("."); // NOI18N
            }
            sb.append(getName());
            signature = sb.toString();
        }

        return signature;
    }

    @Override
    public String getMimeType() {
        return GroovyTokenId.GROOVY_MIME_TYPE;
    }

    @Override
    public FileObject getFileObject() {
        return null;
    }
    
    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        return this.offsetRange == null ? OffsetRange.NONE : offsetRange;
    }

    public void setOffsetRange(OffsetRange offsetRange) {
        this.offsetRange = offsetRange;
    }
   
}
