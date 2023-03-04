<?xml version="1.0"?>
<!--

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


<!-- XML Schema ==> HTML form corresponding the Schema -->



<xsl:stylesheet

 xmlns:xsl = 'http://www.w3.org/1999/XSL/Transform'

 xmlns:xs  = "http://www.w3.org/2001/XMLSchema" 

 xmlns:html= "http://www.w3.org/1999/xhtml"

 version = "1.0" >



<xsl:output method="xml" encoding="UTF-8" />



<xsl:template match = '/' >

<xsl:processing-instruction name="xml-stylesheet">

  href="html.css" type="text/css"

</xsl:processing-instruction>

<html:html>

 <xsl:apply-templates select="//xs:schema/xs:annotation/xs:documentation" />

 <xsl:apply-templates select="//xs:schema/xs:element" />

</html:html>

</xsl:template>



<!-- Top-level element within XML Schema -->

<xsl:template match = 'xs:schema/xs:element' >

 <html:form action="action.htm" method="get" >

  <!-- specify action within XML Schema ?? -->

  <html:fieldset>

   <xsl:attribute name="class">

    <xsl:value-of select="@name" />

   </xsl:attribute>

   <html:legend> <xsl:value-of select="@name" /> </html:legend>



   <xsl:variable name="typename"> <xsl:value-of select="substring-after(@type,':')"/> </xsl:variable>



   <xsl:apply-templates 

    select="xs:complexType | //xs:schema/xs:complexType[@name=$typename]" mode="recur" />

  </html:fieldset>

  <html:input type="submit" /> <html:input type="reset"/>

 </html:form>

</xsl:template>



<xsl:template match = 'text()' >

</xsl:template>



<!-- Types don't appear in XML instances, so neither in forms -->

<xsl:template match = 'xs:complexType' mode="recur" >

   <!--

 DBG:xsl:template match = 'xs:complexType' mode="recur"; 

   -->

 <xsl:variable name="baseTypename"> <xsl:value-of select="substring-after(xs:complexContent/xs:extension/@base,':')"/> </xsl:variable>

   <xsl:apply-templates 

	   select="xs:complexType | //xs:schema/xs:complexType[@name=$baseTypename]" mode="recur" />

   

 <xsl:apply-templates select='xs:all/xs:element | xs:choice | xs:complexContent/xs:extension ' mode="recur" />

 <xsl:apply-templates select='xs:annotation/xs:documentation' />

</xsl:template>



<!-- Elements: this is the heart of this tranform -->

<xsl:template match = 'xs:element' mode="recur" >

 <xsl:variable name="typename">

   <xsl:value-of select="substring-after(@type,':')"/>

 </xsl:variable>

   <!--

   DBG:xsl:value-of select="$typename" =  <xsl:value-of select="$typename" />;

   DBG:xsl:value-of select="@name" =  <xsl:value-of select="@name" />;

   -->

 <html:fieldset>

  <xsl:attribute name="class">

   <xsl:value-of select="@name" />

  </xsl:attribute>

  <html:legend> <xsl:value-of select="@name" /> </html:legend>



  <xsl:choose>

   <!-- Non-embeded type -->

   <xsl:when test="$typename != '' ">

   <!--

    DBG:xsl:when test="$typename"

   -->

    <xsl:apply-templates select='

      //xs:complexType[@name=$typename] |

      //xs:simpleType [@name=$typename]

                                ' mode="recur" /> 

   </xsl:when>

   <!-- Embeded type -->

   <xsl:otherwise>

   <!--

   DBG:xsl:otherwise

   -->

    <xsl:apply-templates select='xs:complexType |

                                 xs:simpleType

                                ' mode="recur" />

   </xsl:otherwise>

  </xsl:choose>

 </html:fieldset>

   <!-- Caution, works for internal types only, we should fetch 

   the imported schemas with document()

   -->

</xsl:template>



<xsl:template match = 'xs:documentation' >

 <html:label>

   <xsl:copy-of select="."/>

 </html:label>

</xsl:template>



<xsl:template match='xs:element [@type="string" or

                                 @type="PCDATA"  or

                                 @type="NMTOKEN"  or

                                 @type="NMTOKENS"

]'

   mode="recur" >

  <html:label>

  <xsl:value-of select="@name" />: 

  <html:input type="text" >

  <xsl:attribute name="name">

   <xsl:value-of select="@name" />

  </xsl:attribute>

  </html:input>

  </html:label>

<!--  | (not xs:complexType and not @type) -->

</xsl:template>



<xsl:template match='xs:complexType [@type="string" or @type="PCDATA"]' 

   mode="recur" >

  <html:label>

  <xsl:value-of select="@name" />: 

  <html:input type="text" >

   <xsl:attribute name="name">

    <xsl:value-of select="@name" />

   </xsl:attribute>

  </html:input>

  </html:label>

</xsl:template>



<xsl:template match='xs:simpleType' mode="recur" >

 <html:label>

 <xsl:value-of select="@name" />: 

 <xsl:choose>

   <xsl:when test="xs:enumeration">

     <html:select>

      <xsl:apply-templates select='xs:enumeration'/>

     </html:select>

   </xsl:when>

  <xsl:otherwise>

  <!-- TODO: make named template, do not paste!! -->

  <html:input type="text" >

   <xsl:attribute name="name">

    <xsl:value-of select="@name" />

   </xsl:attribute>

  </html:input>

  </xsl:otherwise>

 </xsl:choose>

  </html:label>

</xsl:template>



<xsl:template match='xs:simpleType/xs:enumeration'>

 <html:option>

  <xsl:value-of select="@value"/>

 </html:option>

</xsl:template>



<xsl:template match = 'xs:choice' mode="recur" >

 IN CONSTRUCTION: xmlsch:choice

 <xsl:apply-templates select='xs:element' mode="choiceRadio" />

 <xsl:apply-templates select='xs:element|xs:choice' mode="recur" />

 <xsl:apply-templates select='xs:annotation/xs:documentation' />

</xsl:template>



 <xsl:template match = 'xs:element' mode="choiceRadio" >

  <html:input type="radio" >

   <xsl:attribute name="name">choice</xsl:attribute><!-- BETTER TO DO ... -->

   <xsl:attribute name="onClick">alert('choice Radio');//?????????????</xsl:attribute>

   <xsl:attribute name="value">

    <xsl:value-of select="@name" />

   </xsl:attribute>

  </html:input>

 </xsl:template>



</xsl:stylesheet>



