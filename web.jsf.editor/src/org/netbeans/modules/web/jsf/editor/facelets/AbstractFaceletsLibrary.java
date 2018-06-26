/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.facelets;

import com.sun.faces.facelets.tag.TagLibraryImpl;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.LibraryComponent;
import org.netbeans.modules.web.jsfapi.api.Tag;

public abstract class AbstractFaceletsLibrary extends TagLibraryImpl implements Library {

    protected FaceletsLibrarySupport support;

    public AbstractFaceletsLibrary(FaceletsLibrarySupport support, String namespace) {
        super(namespace);
        this.support = support;
    }

    public abstract Map<String, ? extends NamedComponent> getComponentsMap();

    public abstract URL getLibraryDescriptorSource();

    public abstract LibraryDescriptor getLibraryDescriptor();

    @Override
    public Collection<? extends NamedComponent> getComponents() {
        return getComponentsMap().values();
    }
    
    @Override
    public NamedComponent getComponent(String componentName) {
        return getComponentsMap().get(componentName);
    }

    //since the web-facelettaglibrary_2_0.xsd schema doesn't define any library default prefixes as the TLDs do,
    //we need to define them manually
    @Override
    public String getDefaultPrefix() {
        return DefaultFaceletLibraries.getLibraryDefaultPrefix(getNamespace());
    }

    @Override
    public String getDisplayName() {
        String displayName = DefaultFaceletLibraries.getLibraryDisplayName(getNamespace());
        return displayName != null ? displayName : getNamespace();
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(super.toString())
                .append("(namespace=")
                .append(getNamespace())
                .append(", default prefix=")
                .append(getDefaultPrefix())
                .append(", descriptor=")
                .append(getLibraryDescriptor()).toString(); //NOI18N
    }



    @Override
    public boolean equals(Object obj) {
         if(!(obj instanceof AbstractFaceletsLibrary)) {
             return false;
         }
         AbstractFaceletsLibrary other = (AbstractFaceletsLibrary)obj;
        if (getClass() != obj.getClass()) {
            return false;
        }
        if ((this.getNamespace() == null) ? (other.getNamespace() != null) : !this.getNamespace().equals(other.getNamespace())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.getNamespace() != null ? this.getNamespace().hashCode() : 0);
        return hash;
    }

    public class NamedComponent implements LibraryComponent {

        protected String name;

        protected NamedComponent(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Tag getTag() {
            return getLibraryDescriptor().getTags().get(getName());
        }

        @Override
        public AbstractFaceletsLibrary getLibrary() {
            return AbstractFaceletsLibrary.this;
        }

        @Override
        public String[][] getDescription() {
            return new String[][]{{"name", getName()}}; //NOI18N
        }

        protected String[][] merge(String[][] first, String[][] second) {
            String[][] merged = new String[first.length + second.length][];
            System.arraycopy(first, 0, merged, 0, first.length);
            System.arraycopy(second, 0, merged, first.length, second.length);
            return merged;
        }
    }

    public class BaseComponent extends NamedComponent {

        protected String id;
        protected Class handlerClass;

        private BaseComponent(String name, String id, Class handlerClass) {
            super(name);
            this.id = id;
            this.handlerClass = handlerClass;
        }

        public Class getHandlerClass() {
            return handlerClass;
        }

        public String getId() {
            return id;
        }

        @Override
        public String[][] getDescription() {
            String[][] myDescr = new String[][]{{"id", getId()}, //NOI18N
                                  {"handler class", getHandlerClass() == null ? "N/A" : getHandlerClass().getName()}}; //NOI18N
            return merge(super.getDescription(), myDescr);
        }


    }

    public class Converter extends BaseComponent {

        protected Converter(String name, String id, Class handlerClass) {
            super(name, id, handlerClass);
        }

        @Override
        public String[][] getDescription() {
               String[][] myDescr = new String[][]{{"type", "converter"}}; //NOI18N
            return merge(super.getDescription(), myDescr);
        }


    }

    public class Validator extends BaseComponent {

        protected Validator(String name, String id, Class handlerClass) {
            super(name, id, handlerClass);
        }

        @Override
        public String[][] getDescription() {
               String[][] myDescr = new String[][]{{"type", "validator"}}; //NOI18N
            return merge(super.getDescription(), myDescr);
        }
    }

    public class Behavior extends BaseComponent {

        protected Behavior(String name, String id, Class handlerClass) {
            super(name, id, handlerClass);
        }

        @Override
        public String[][] getDescription() {
               String[][] myDescr = new String[][]{{"type", "behavior"}}; //NOI18N
            return merge(super.getDescription(), myDescr);
        }
    }

    public class TagHandler extends NamedComponent {

        protected Class type;

        protected TagHandler(String name, Class type) {
            super(name);
            this.type = type;
        }

        public Class getType() {
            return type;
        }

        @Override
        public String[][] getDescription() {
               String[][] myDescr = new String[][]{{"type", "tag handler"}, //NOI18N
               {"class type", getType() == null ? "N/A" : getType().getName()}}; //NOI18N
            return merge(super.getDescription(), myDescr); //NOI18N
        }


    }

    public class Component extends NamedComponent {

        protected String componentType;
        protected String rendererType;
        protected Class handlerClass;

        protected Component(String name, String componentType, String rendererType, Class handlerClass) {
            super(name);
            this.componentType = componentType;
            this.rendererType = rendererType;
            this.handlerClass = handlerClass;
        }

        public String getComponentType() {
            return componentType;
        }

        public String getRendererType() {
            return rendererType;
        }

        public Class getHandlerClass() {
            return handlerClass;
        }

           @Override
        public String[][] getDescription() {
               String[][] myDescr = new String[][]{{"type", "component"}, //NOI18N
               {"component type", getComponentType() == null ? "N/A" : getComponentType()}, //NOI18N
               {"renderer type", getRendererType() == null ? "N/A" : getRendererType()}, //NOI18N
               {"handler class", getHandlerClass() == null ? "N/A" : getHandlerClass().getName()}}; //NOI18N
            return merge(super.getDescription(), myDescr);
        }


    }

    public class UserTag extends NamedComponent {

        protected URL source;

        protected UserTag(String name, URL source) {
            super(name);
            this.source = source;
        }

        public URL getUrl() {
            return source;
        }

        @Override
        public String[][] getDescription() {
               String[][] myDescr = new String[][]{{"type", "user tag"}, //NOI18N
               {"URL", getUrl() == null ? "N/A" : getUrl().toExternalForm()}}; //NOI18N
            return merge(super.getDescription(), myDescr);
        }
    }

    public class CompositeComponentTag extends NamedComponent {

        protected String resourceId;

        protected CompositeComponentTag(String name, String resourceId) {
            super(name);
            this.resourceId = resourceId;
        }

        public String getResourceId() {
            return resourceId;
        }

        @Override
        public String[][] getDescription() {
               String[][] myDescr = new String[][]{{"type", "composite component tag"}, //NOI18N
               {"resource id", getResourceId() == null ? "N/A" : getResourceId()}}; //NOI18N
            return merge(super.getDescription(), myDescr);
        }
    }

    public class Function extends NamedComponent {

        protected Method function;

        protected Function(String name, Method function) {
            super(name);
            this.function = function;
        }

        public Method getFunction() {
            return function;
        }

        @Override
        public String[][] getDescription() {
               String[][] myDescr = new String[][]{{"type", "function"}, //NOI18N
               {"function name", getFunction() == null ? "N/A" : getFunction().toGenericString()}}; //NOI18N
            return merge(super.getDescription(), myDescr);
        }


    }

}
