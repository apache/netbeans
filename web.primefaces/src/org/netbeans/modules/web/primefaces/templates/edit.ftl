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

  This is XHTML template for 'JSF Pages From Entity Beans' action. Templating
  is performed using FreeMaker (http://freemarker.org/) - see its documentation
  for full syntax. Variables available for templating are:

    entityName - name of entity being modified (type: String)
    managedBean - name of managed choosen in UI (type: String)
    managedBeanProperty - name of managed bean property choosen in UI (type: String)
    item - name of property used for dataTable iteration (type: String)
    comment - always set to "false" (type: Boolean)
    nsLocation - which namespace location to use (http://xmlns.jcp.org in case of JSF2.2, http://java.sun.com otherwise)
    entityDescriptors - list of beans describing individual entities. Bean has following properties:
        label - field label (type: String)
        name - field property name (type: String)
        dateTimeFormat - date/time/datetime formatting (type: String)
        blob - does field represents a large block of text? (type: boolean)
        relationshipOne - does field represent one to one or many to one relationship (type: boolean)
        relationshipMany - does field represent one to many relationship (type: boolean)
        id - field id name (type: String)
        required - is field optional and nullable or it is not? (type: boolean)
        returnType - fully qualified data type of the field
        valuesGetter - if item is of type 1:1 or 1:many relationship then use this
            getter to populate <h:selectOneMenu> or <h:selectManyMenu>
    bundle - name of the variable defined in the JSF config file for the resource bundle (type: String)

  This template is accessible via top level menu Tools->Templates and can
  be found in category JavaServer Faces->JSF from Entity.

</#if>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:ui="${nsLocation}/jsf/facelets"
      xmlns:h="${nsLocation}/jsf/html"
      xmlns:f="${nsLocation}/jsf/core"
      xmlns:p="http://primefaces.org/ui">

    <ui:composition>

        <p:dialog id="${entityName}EditDlg" widgetVar="${entityName}EditDialog" modal="true" resizable="false" appendTo="@(body)" header="${r"#{"}${bundle}.Edit${entityName}Title${r"}"}">
            <h:form id="${entityName}EditForm">
                <h:panelGroup id="display">
                    <p:panelGrid columns="2" rendered="${r"#{"}${managedBeanProperty} != null${r"}"}">
<#list entityDescriptors as entityDescriptor>
                        <p:outputLabel value="${r"#{"}${bundle}.Edit${entityName}Label_${entityDescriptor.id?replace(".","_")}${r"}"}" for="${entityDescriptor.id?replace(".","_")}" />
    <#if entityDescriptor.dateTimeFormat?? && entityDescriptor.dateTimeFormat != "">
                        <p:calendar id="${entityDescriptor.id?replace(".","_")}" pattern="${entityDescriptor.dateTimeFormat}" value="${r"#{"}${entityDescriptor.name}${r"}"}" title="${r"#{"}${bundle}.Edit${entityName}Title_${entityDescriptor.id?replace(".","_")}${r"}"}" <#if entityDescriptor.required>required="true" requiredMessage="${r"#{"}${bundle}.Edit${entityName}RequiredMessage_${entityDescriptor.id?replace(".","_")}${r"}"}"</#if> showOn="button"/>
    <#elseif entityDescriptor.returnType?matches(".*[Bb]+oolean")>
                        <p:selectBooleanCheckbox id="${entityDescriptor.id?replace(".","_")}" value="${r"#{"}${entityDescriptor.name}${r"}"}" <#if entityDescriptor.required>required="true" requiredMessage="${r"#{"}${bundle}.Edit${entityName}RequiredMessage_${entityDescriptor.id?replace(".","_")}${r"}"}"</#if>/>
    <#elseif entityDescriptor.blob>
                        <p:inputTextarea rows="4" cols="30" id="${entityDescriptor.id?replace(".","_")}" value="${r"#{"}${entityDescriptor.name}${r"}"}" title="${r"#{"}${bundle}.Edit${entityName}Title_${entityDescriptor.id?replace(".","_")}${r"}"}" <#if entityDescriptor.required>required="true" requiredMessage="${r"#{"}${bundle}.Edit${entityName}RequiredMessage_${entityDescriptor.id?replace(".","_")}${r"}"}"</#if>/>
    <#elseif entityDescriptor.relationshipOne>
                        <p:selectOneMenu id="${entityDescriptor.id?replace(".","_")}" value="${r"#{"}${entityDescriptor.name}${r"}"}" <#if entityDescriptor.required>required="true" requiredMessage="${r"#{"}${bundle}.Edit${entityName}RequiredMessage_${entityDescriptor.id?replace(".","_")}${r"}"}"</#if>>
                            <f:selectItem itemLabel="${r"#{"}${bundle}.SelectOneMessage${r"}"}"/>
                            <f:selectItems value="${r"#{"}${entityDescriptor.valuesGetter}${r"}"}"
                                           var="${entityDescriptor.id?replace(".","_")}Item"
                                           itemValue="${r"#{"}${entityDescriptor.id?replace(".","_")}Item${r"}"}"/>
                        </p:selectOneMenu>
    <#elseif entityDescriptor.relationshipMany>
                        <p:selectManyMenu id="${entityDescriptor.id?replace(".","_")}" value="${r"#{"}${entityDescriptor.name}${r"}"}" <#if entityDescriptor.required>required="true" requiredMessage="${r"#{"}${bundle}.Edit${entityName}RequiredMessage_${entityDescriptor.id?replace(".","_")}${r"}"}"</#if>>
                            <f:selectItems value="${r"#{"}${entityDescriptor.valuesGetter}${r"}"}"
                                           var="${entityDescriptor.id?replace(".","_")}Item"
                                           itemValue="${r"#{"}${entityDescriptor.id?replace(".","_")}Item${r"}"}"/>
                        </p:selectManyMenu>
    <#else>
                        <p:inputText id="${entityDescriptor.id?replace(".","_")}" value="${r"#{"}${entityDescriptor.name}${r"}"}" title="${r"#{"}${bundle}.Edit${entityName}Title_${entityDescriptor.id?replace(".","_")}${r"}"}" <#if entityDescriptor.required>required="true" requiredMessage="${r"#{"}${bundle}.Edit${entityName}RequiredMessage_${entityDescriptor.id?replace(".","_")}${r"}"}"</#if>/>
    </#if>
</#list>
                    </p:panelGrid>
                    <p:commandButton actionListener="${r"#{"}${managedBean}${r".update}"}" value="${r"#{"}${bundle}.Save${r"}"}" update="display,:${entityName}ListForm:datalist,:growl" oncomplete="handleSubmit(args, '${entityName}EditDialog');"/>
                    <p:commandButton value="${r"#{"}${bundle}.Cancel${r"}"}" onclick="${entityName}EditDialog.hide()"/>
                </h:panelGroup>
            </h:form>
        </p:dialog>

    </ui:composition>
</html>
