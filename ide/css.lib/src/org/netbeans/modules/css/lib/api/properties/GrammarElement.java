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
package org.netbeans.modules.css.lib.api.properties;

import java.util.LinkedList;
import java.util.List;

/**
 * An element of the parsed css property value grammar.
 * 
 * Note: the object is in fact immutable since the setMinimum/MaximumOccurrences is
 * called only just after its creation.
 * 
 * @author mfukala@netbeans.org
 */
public abstract class GrammarElement {

    public static final char INVISIBLE_PROPERTY_PREFIX = '@';
    
    public static boolean isArtificialElementName(CharSequence name) {
        if(name.length() == 0) {
            return false;
        }
        return name.charAt(0) == INVISIBLE_PROPERTY_PREFIX;
    }
    
    private GroupGrammarElement parent;
    private String path;
    private String name;

    public GrammarElement(GroupGrammarElement parent, String elementName) {
        this.parent = parent;
        this.name = elementName;
    }
    
    /**
     * Return name of the element if it is named, null otherwise.
     */
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public abstract void accept(GrammarElementVisitor visitor);
    
    private int minimum_occurances = 1;
    private int maximum_occurances = 1;

    public void setMinimumOccurances(int i) {
        this.minimum_occurances = i;
    }

    public void setMaximumOccurances(int i) {
        maximum_occurances = i;
    }

    public int getMaximumOccurances() {
        return maximum_occurances;
    }

    public int getMinimumOccurances() {
        return minimum_occurances;
    }

    public boolean isOptional() {
        return getMinimumOccurances() == 0;
    }

    public GroupGrammarElement parent() {
        return parent;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GrammarElement)) {
            return false;
        }
        GrammarElement e = (GrammarElement) o;
        return path().equalsIgnoreCase(e.path());
    }

    @Override
    public int hashCode() {
        return path().hashCode();
    }

    /**
     * returns a name of the property from which this element comes from
     */
    public String origin() {
        return origin(true);
    }

    public String getVisibleOrigin() {
        return origin(false);
    }
    
    private String origin(boolean allowNonVisibleElements) {
        GroupGrammarElement p = parent;
        while (p != null) {
            if (p.getName() != null) {
                boolean visible = !isArtificialElementName(p.getName());
                if (visible || allowNonVisibleElements) {
                    return p.getName();
                }
            }
            p = p.parent();
        }
        return null;
    }

    public String path() {
        if (path == null) {
            StringBuilder sb = new StringBuilder();
            if (parent() != null) {
                sb.append(parent().path());
                sb.append('/');
            }
            sb.append(toString());
            path = sb.toString();
        }
        return path;
    }

    public List<GrammarElement> elementsPath() {
        List<GrammarElement> elementsPath = new LinkedList<>();

        GrammarElement element = this;
        do {
            elementsPath.add(0, element);
        } while((element = element.parent()) != null);

        return elementsPath;
    }

    @Override
    public String toString() {
        if (getMinimumOccurances() != 1 || getMaximumOccurances() != 1) {
            return "{" + getMinimumOccurances() + "," + (getMaximumOccurances() == Integer.MAX_VALUE ? "inf" : getMaximumOccurances()) + "}"; //NOI18N
        } else {
            return ""; //NOI18N
        }
    }

}
