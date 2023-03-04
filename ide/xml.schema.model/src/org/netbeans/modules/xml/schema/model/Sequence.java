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

package org.netbeans.modules.xml.schema.model;

/**
 * This interface represents a sequence definition.
 * @author Chris Webster
 */
public interface Sequence extends SequenceDefinition,
    ComplexExtensionDefinition, ComplexTypeDefinition, LocalGroupDefinition,
    SchemaComponent  {

    public static final String MIN_OCCURS_PROPERTY = "minOccurs"; //NOI18N
    public static final String MAX_OCCURS_PROPERTY = "maxOccurs"; //NOI18N
    public static final String CONTENT_PROPERTY ="content"; //NOI18N

    java.util.List<SequenceDefinition> getContent();
    void addContent(SequenceDefinition definition, int position);
    void appendContent(SequenceDefinition definition);
    void removeContent(SequenceDefinition definition);
    
    /**
     * return ability to set min and max occurs if appropriate, null
     * otherwise. This method
     * should only be used after insertion into the model.
     */
    public Cardinality getCardinality();
}
