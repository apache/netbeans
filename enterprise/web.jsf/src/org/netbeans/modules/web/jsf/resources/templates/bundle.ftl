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

  This is Bundle.properties template for 'JSF Pages From Entity Beans' action. Templating
  is performed using FreeMaker (http://freemarker.org/) - see its documentation
  for full syntax. Variables available for templating are:

    entities - list of beans with following properites:
        entityClassName - controller class name (type: String)
        entityDescriptors - list of beans describing individual entities. Bean has following properties:
            label - part of bundle key name for label (type: String)
            title - part of bundle key name for title (type: String)
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
  be found in category JavaServer Faces->JSF from Entity.

</#if>
PersistenceErrorOccured=A persistence error occurred.
Previous=Previous
Next=Next

<#list entities as entity>
${entity.entityClassName}Created=${entity.entityClassName} was successfully created.
${entity.entityClassName}Updated=${entity.entityClassName} was successfully updated.
${entity.entityClassName}Deleted=${entity.entityClassName} was successfully deleted.
Create${entity.entityClassName}Title=Create New ${entity.entityClassName}
Create${entity.entityClassName}SaveLink=Save
Create${entity.entityClassName}ShowAllLink=Show All ${entity.entityClassName} Items
Create${entity.entityClassName}IndexLink=Index
    <#list entity.entityDescriptors as entityDescriptor>
Create${entity.entityClassName}Label_${entityDescriptor.id?replace(".","_")}=${entityDescriptor.label}:
<#if entityDescriptor.required>Create${entity.entityClassName}RequiredMessage_${entityDescriptor.id?replace(".","_")}=The ${entityDescriptor.label} field is required.
</#if>Create${entity.entityClassName}Title_${entityDescriptor.id?replace(".","_")}=${entityDescriptor.label}
    </#list>
Edit${entity.entityClassName}Title=Edit ${entity.entityClassName}
Edit${entity.entityClassName}SaveLink=Save
Edit${entity.entityClassName}ViewLink=View
Edit${entity.entityClassName}ShowAllLink=Show All ${entity.entityClassName} Items
Edit${entity.entityClassName}IndexLink=Index
    <#list entity.entityDescriptors as entityDescriptor>
Edit${entity.entityClassName}Label_${entityDescriptor.id?replace(".","_")}=${entityDescriptor.label}:
<#if entityDescriptor.required>Edit${entity.entityClassName}RequiredMessage_${entityDescriptor.id?replace(".","_")}=The ${entityDescriptor.label} field is required.
</#if>Edit${entity.entityClassName}Title_${entityDescriptor.id?replace(".","_")}=${entityDescriptor.label}
    </#list>
View${entity.entityClassName}Title=View
View${entity.entityClassName}DestroyLink=Destroy
View${entity.entityClassName}EditLink=Edit
View${entity.entityClassName}CreateLink=Create New ${entity.entityClassName}
View${entity.entityClassName}ShowAllLink=Show All ${entity.entityClassName} Items
View${entity.entityClassName}IndexLink=Index
    <#list entity.entityDescriptors as entityDescriptor>
View${entity.entityClassName}Label_${entityDescriptor.id?replace(".","_")}=${entityDescriptor.label}:
View${entity.entityClassName}Title_${entityDescriptor.id?replace(".","_")}=${entityDescriptor.label}
    </#list>
List${entity.entityClassName}Title=List
List${entity.entityClassName}Empty=(No ${entity.entityClassName} Items Found)
List${entity.entityClassName}DestroyLink=Destroy
List${entity.entityClassName}EditLink=Edit
List${entity.entityClassName}ViewLink=View
List${entity.entityClassName}CreateLink=Create New ${entity.entityClassName}
List${entity.entityClassName}IndexLink=Index
    <#list entity.entityDescriptors as entityDescriptor>
List${entity.entityClassName}Title_${entityDescriptor.id?replace(".","_")}=${entityDescriptor.label}
    </#list>
</#list>
