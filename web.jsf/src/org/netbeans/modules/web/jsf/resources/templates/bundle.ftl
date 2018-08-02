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
