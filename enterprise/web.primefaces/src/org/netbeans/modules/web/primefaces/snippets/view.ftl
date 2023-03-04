<#--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->
<#if comment>

  TEMPLATE DESCRIPTION:

  This is XHTML template for 'JSF ReadOnly Form From Entity' action. Templating
  is performed using FreeMaker (http://freemarker.org/) - see its documentation
  for full syntax. Variables available for templating are:

    prefixResolver - helps resolve prefix for given template (call prefixForNS(namespace, fallbackPrefix) method)
    entityName - name of entity being modified (type: String)
    managedBean - name of managed choosen in UI (type: String)
    managedBeanProperty - name of managed bean property choosen in UI (type: String)
    item - name of property used for dataTable iteration (type: String)
    comment - always set to "false" (type: Boolean)
    entityDescriptors - list of beans describing individual entities. Bean has following properties:
        label - field label (type: String)
        name - field property name (type: String)
        dateTimeFormat - date/time/datetime formatting (type: String)
        blob - does field represents a large block of text? (type: boolean)
        relationshipOne - does field represent one to one or many to one relationship (type: boolean)
        relationshipMany - does field represent one to many relationship (type: boolean)
        id - field id name (type: String)
        required - is field optional and nullable or it is not? (type: boolean)
        valuesGetter - if item is of type 1:1 or 1:many relationship then use this
            getter to populate <h:selectOneMenu> or <h:selectManyMenu>

  This template is accessible via top level menu Tools->Templates and can
  be found in category JavaServer Faces->JSF Data/Form from Entity.

</#if>

<#assign htmlTagPrefix=prefixResolver.getPrefixForNS("http://xmlns.jcp.org/jsf/html", "h")>
<#assign coreTagPrefix=prefixResolver.getPrefixForNS("http://xmlns.jcp.org/jsf/core", "f")>
<#assign pfTagPrefix=prefixResolver.getPrefixForNS("http://primefaces.org/ui", "p")>

<${htmlTagPrefix}:form>
    <h1><${htmlTagPrefix}:outputText value="View"/></h1>
    <${pfTagPrefix}:panelGrid columns="2">
<#list entityDescriptors as entityDescriptor>
        <${htmlTagPrefix}:outputText value="${entityDescriptor.label}:"/>
<#if entityDescriptor.dateTimeFormat?? && entityDescriptor.dateTimeFormat != "">
        <${htmlTagPrefix}:outputText value="${r"#{"}${entityDescriptor.name}${r"}"}" title="${entityDescriptor.label}">
            <${coreTagPrefix}:convertDateTime pattern="${entityDescriptor.dateTimeFormat}" />
        </${htmlTagPrefix}:outputText>
<#else>
        <${htmlTagPrefix}:outputText value="${r"#{"}${entityDescriptor.name}${r"}"}" title="${entityDescriptor.label}"/>
</#if>
</#list>
    </${pfTagPrefix}:panelGrid>
</${htmlTagPrefix}:form>
