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
        returnType - fully qualified data type of the field (type: String)
        id - field id name (type: String)
        required - is field optional and nullable or it is not? (type: boolean)
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

    <ui:composition template="/template.xhtml">


        <ui:define name="title">
            <h:outputText value="${r"#{"}${bundle}.List${entityName}Title${r"}"}"></h:outputText>
        </ui:define>

        <ui:define name="body">
                <h:form id="${entityName}ListForm">
                    <p:panel header="${r"#{"}${bundle}.List${entityName}Title${r"}"}">
                        <p:dataTable id="datalist" value="${r"#{"}${managedBeanProperty}${r"}"}" var="${item}"
                            selectionMode="single" selection="${r"#{"}${managedBean}${r".selected}"}"
                            paginator="true"
                            rowKey="${r"#{"}${item}.${entityIdField}${r"}"}"
                            rows="10"
                            rowsPerPageTemplate="10,20,30,40,50"
                            >

                            <p:ajax event="rowSelect"   update="createButton viewButton editButton deleteButton"/>
                            <p:ajax event="rowUnselect" update="createButton viewButton editButton deleteButton"/>

<#list entityDescriptors as entityDescriptor>
                            <p:column>
                                <f:facet name="header">
                                    <h:outputText value="${r"#{"}${bundle}.List${entityName}Title_${entityDescriptor.id?replace(".","_")}${r"}"}"/>
                                </f:facet>
    <#if entityDescriptor.dateTimeFormat?? && entityDescriptor.dateTimeFormat != "">
                                <h:outputText value="${r"#{"}${entityDescriptor.name}${r"}"}">
                                    <f:convertDateTime pattern="${entityDescriptor.dateTimeFormat}" />
                                </h:outputText>
    <#elseif entityDescriptor.returnType?matches(".*[Bb]+oolean")>
                                <p:selectBooleanCheckbox value="${r"#{"}${entityDescriptor.name}${r"}"}" disabled="true"/>
    <#else>
                                <h:outputText value="${r"#{"}${entityDescriptor.name}${r"}"}"/>
    </#if>
                            </p:column>
</#list>
                            <f:facet name="footer">
                                <p:commandButton id="createButton" icon="ui-icon-plus"   value="${r"#{"}${bundle}.Create${r"}"}" actionListener="${r"#{"}${managedBean}.prepareCreate${r"}"}" update=":${entityName}CreateForm" oncomplete="PF('${entityName}CreateDialog').show()"/>
                                <p:commandButton id="viewButton"   icon="ui-icon-search" value="${r"#{"}${bundle}.View${r"}"}" update=":${entityName}ViewForm" oncomplete="PF('${entityName}ViewDialog').show()" disabled="${r"#{"}empty ${managedBean}.selected${r"}"}"/>
                                <p:commandButton id="editButton"   icon="ui-icon-pencil" value="${r"#{"}${bundle}.Edit${r"}"}" update=":${entityName}EditForm" oncomplete="PF('${entityName}EditDialog').show()" disabled="${r"#{"}empty ${managedBean}.selected${r"}"}"/>
                                <p:commandButton id="deleteButton" icon="ui-icon-trash"  value="${r"#{"}${bundle}.Delete${r"}"}" actionListener="${r"#{"}${managedBean}${r".destroy}"}" update=":growl,datalist" disabled="${r"#{"}empty ${managedBean}.selected${r"}"}"/>
                            </f:facet>
                        </p:dataTable>
                    </p:panel>
                </h:form>

            <ui:include src="Create.xhtml"/>
            <ui:include src="Edit.xhtml"/>
            <ui:include src="View.xhtml"/>
        </ui:define>
    </ui:composition>

</html>
