<#--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

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
<#if nsLocation?starts_with("http://")>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:ui="${nsLocation}/jsf/facelets"
      xmlns:h="${nsLocation}/jsf/html"
      xmlns:f="${nsLocation}/jsf/core">
<#else>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:ui="${nsLocation}.facelets"
      xmlns:h="${nsLocation}.html"
      xmlns:f="${nsLocation}.core">
</#if>

    <ui:composition template="/template.xhtml">
        <ui:define name="title">
            <h:outputText value="${r"#{"}${bundle}.List${entityName}Title${r"}"}"></h:outputText>
        </ui:define>
        <ui:define name="body">
        <h:form styleClass="jsfcrud_list_form">
            <h:panelGroup id="messagePanel" layout="block">
                <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            </h:panelGroup>
            <h:outputText escape="false" value="${r"#{"}${bundle}.List${entityName}Empty${r"}"}" rendered="${r"#{"}${managedBean}${r".items.rowCount == 0}"}"/>
            <h:panelGroup rendered="${r"#{"}${managedBean}${r".items.rowCount > 0}"}">
                <h:outputText value="${r"#{"}${managedBean}${r".pagination.pageFirstItem + 1}"}..${r"#{"}${managedBean}${r".pagination.pageLastItem + 1}"}/${r"#{"}${managedBean}${r".pagination.itemsCount}"}"/>&nbsp;
                <h:commandLink action="${r"#{"}${managedBean}${r".previous}"}" value="${r"#{"}${bundle}.Previous${r"}"} ${r"#{"}${managedBean}${r".pagination.pageSize}"}" rendered="${r"#{"}${managedBean}${r".pagination.hasPreviousPage}"}"/>&nbsp;
                <h:commandLink action="${r"#{"}${managedBean}${r".next}"}" value="${r"#{"}${bundle}.Next${r"}"} ${r"#{"}${managedBean}${r".pagination.pageSize}"}" rendered="${r"#{"}${managedBean}${r".pagination.hasNextPage}"}"/>&nbsp;
                <h:dataTable value="${r"#{"}${managedBeanProperty}${r"}"}" var="${item}" border="0" cellpadding="2" cellspacing="0" rowClasses="jsfcrud_odd_row,jsfcrud_even_row" rules="all" style="border:solid 1px">
<#list entityDescriptors as entityDescriptor>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="${r"#{"}${bundle}.List${entityName}Title_${entityDescriptor.id?replace(".","_")}${r"}"}"/>
                        </f:facet>
    <#if entityDescriptor.dateTimeFormat?? && entityDescriptor.dateTimeFormat != "">
                        <h:outputText value="${r"#{"}${entityDescriptor.name}${r"}"}">
                            <f:convertDateTime pattern="${entityDescriptor.dateTimeFormat}" />
                        </h:outputText>
    <#else>
                        <h:outputText value="${r"#{"}${entityDescriptor.name}${r"}"}"/>
    </#if>
                    </h:column>
</#list>
                    <h:column>
                        <f:facet name="header">
                            <h:outputText value="&nbsp;"/>
                        </f:facet>
                        <h:commandLink action="${r"#{"}${managedBean}${r".prepareView}"}" value="${r"#{"}${bundle}.List${entityName}ViewLink${r"}"}"/>
                        <h:outputText value=" "/>
                        <h:commandLink action="${r"#{"}${managedBean}${r".prepareEdit}"}" value="${r"#{"}${bundle}.List${entityName}EditLink${r"}"}"/>
                        <h:outputText value=" "/>
                        <h:commandLink action="${r"#{"}${managedBean}${r".destroy}"}" value="${r"#{"}${bundle}.List${entityName}DestroyLink${r"}"}"/>
                    </h:column>
                </h:dataTable>
            </h:panelGroup>
            <br />
            <h:commandLink action="${r"#{"}${managedBean}${r".prepareCreate}"}" value="${r"#{"}${bundle}.List${entityName}CreateLink${r"}"}"/>
            <br />
            <br />
            <h:link outcome="/index" value="${r"#{"}${bundle}.List${entityName}IndexLink${r"}"}"/>
        </h:form>
        </ui:define>
    </ui:composition>

</html>
