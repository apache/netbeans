<#--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.

The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Oracle in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):
-->
<#if comment>

  TEMPLATE DESCRIPTION:

  This is XHTML template for 'JSF Editable Form From Entity' action. Templating
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

<${prefixResolver.getPrefixForNS("http://xmlns.jcp.org/jsf/html", "h")}:form>
    <h1><${htmlTagPrefix}:outputText value="Create/Edit"/></h1>
    <${htmlTagPrefix}:panelGrid columns="2">
<#list entityDescriptors as entityDescriptor>
        <${htmlTagPrefix}:outputLabel value="${entityDescriptor.label}:" for="${entityDescriptor.id}" />
<#if entityDescriptor.dateTimeFormat?? && entityDescriptor.dateTimeFormat != "">
        <${htmlTagPrefix}:inputText id="${entityDescriptor.id}" value="${r"#{"}${entityDescriptor.name}${r"}"}" title="${entityDescriptor.label}" <#if entityDescriptor.required>required="true" requiredMessage="The ${entityDescriptor.label} field is required."</#if>>
            <${coreTagPrefix}:convertDateTime pattern="${entityDescriptor.dateTimeFormat}" />
        </${htmlTagPrefix}:inputText>
<#elseif entityDescriptor.blob>
        <${htmlTagPrefix}:inputTextarea rows="4" cols="30" id="${entityDescriptor.id}" value="${r"#{"}${entityDescriptor.name}${r"}"}" title="${entityDescriptor.label}" <#if entityDescriptor.required>required="true" requiredMessage="The ${entityDescriptor.label} field is required."</#if>/>
<#elseif entityDescriptor.relationshipOne>
        <${htmlTagPrefix}:selectOneMenu id="${entityDescriptor.id}" value="${r"#{"}${entityDescriptor.name}${r"}"}" title="${entityDescriptor.label}" <#if entityDescriptor.required>required="true" requiredMessage="The ${entityDescriptor.label} field is required."</#if>>
            <!-- TODO: update below reference to list of available items-->
            <${coreTagPrefix}:selectItems value="${r"#{"}fixme${r"}"}"/>
        </${htmlTagPrefix}:selectOneMenu>
<#elseif entityDescriptor.relationshipMany>
        <${htmlTagPrefix}:selectManyMenu id="${entityDescriptor.id}" value="${r"#{"}${entityDescriptor.name}${r"}"}" title="${entityDescriptor.label}" <#if entityDescriptor.required>required="true" requiredMessage="The ${entityDescriptor.label} field is required."</#if>>
            <!-- TODO: update below reference to list of available items-->
            <${coreTagPrefix}:selectItems value="${r"#{"}fixme${r"}"}"/>
        </${htmlTagPrefix}:selectManyMenu>
<#else>
        <${htmlTagPrefix}:inputText id="${entityDescriptor.id}" value="${r"#{"}${entityDescriptor.name}${r"}"}" title="${entityDescriptor.label}" <#if entityDescriptor.required>required="true" requiredMessage="The ${entityDescriptor.label} field is required."</#if>/>
</#if>
</#list>
    </${htmlTagPrefix}:panelGrid>
</${htmlTagPrefix}:form>
