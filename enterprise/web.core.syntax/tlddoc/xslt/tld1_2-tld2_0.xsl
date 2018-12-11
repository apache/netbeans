<?xml version="1.0" encoding="UTF-8" ?>

<!--
  - <license>
  - Copyright (c) 2010, Oracle.
  - All rights reserved.
  -
  - Redistribution and use in source and binary forms, with or without
  - modification, are permitted provided that the following conditions are met:
  -
  -     * Redistributions of source code must retain the above copyright
  -       notice, this list of conditions and the following disclaimer.
  -     * Redistributions in binary form must reproduce the above copyright
  -       notice, this list of conditions and the following disclaimer in the
  -       documentation and/or other materials provided with the distribution.
  -     * Neither the name of Oracle nor the names of its
  -       contributors may be used to endorse or promote products derived from
  -       this software without specific prior written permission.
  -
  - THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  - "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  - TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
  - PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  - CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  - EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  - ROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  - PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  - LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  - NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  - SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  - </license>
  -->

<!--

  Translates a JSP 1.2 TLD into a JSP 2.0 TLD, using the following 
  conversion rules:

  1. Change the <taglib> element to read as follows:
     <taglib xmlns="http://java.sun.com/xml/ns/j2ee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee
         http://java.sun.com/xml/ns/j2ee/web-jsptaglibrary_2_0.xsd">
  2. Remove all id attributes from all elements.
  3. Remove <jsp-version> element and add an attribute to <taglib>
     called 'version' with the value "2.0".
  4. Under <taglib>, if a <description> element exists, move it to
     the top.
  5. Under <taglib>, if a <display-name> element exists, move it
     to the top, after the <description> element.
  6. Under <taglib>, if a <small-icon> and/or <large-icon> element
     exists, wrap them in an <icon> element and move it to the
     top, after the <display-name> element.
  7. For each taglib/validator element, if a <description> element
     exists, move it to the top, under <validator>.
  8. For each taglib/validator/init-param element, if a
     <description> element exists, move it to the top, under
     <init-param>.
  9. For each taglib/tag element, if a <description> element
     exists, move it to the top, under <tag>.
 10. For each taglib/tag element, if a <display-name> element
     exists, move it to the top, after the <description> element,
     under <tag>.
 11. For each taglib/tag element, if a <small-icon> and/or
     <large-icon> element exists, wrap them in an <icon> element
     and move it to the top, after the <display-name> element,
     under <tag>.
 12. For each taglib/tag element, if no <body-content> element 
     exists, supply a default value of "JSP"
 13. For each taglib/tag/variable element, if a <description>
     element exists, move it to the top, under <variable>.
 14. For each taglib/tag/attribute element, if a <description>
     element exists, move it to the top, under <attribute>.

  Author: Mark Roth

-->

<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="/taglib">
    <xsl:element name="taglib" namespace="http://java.sun.com/xml/ns/j2ee">
      <xsl:attribute name="xsi:schemaLocation" namespace="http://www.w3.org/2001/XMLSchema-instance">http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-jsptaglibrary_2_0.xsd</xsl:attribute>
      <xsl:attribute name="version">2.0</xsl:attribute>
      <xsl:apply-templates select="description"/>
      <xsl:apply-templates select="display-name"/>
      <xsl:if test="small-icon|large-icon">
        <icon xmlns="http://java.sun.com/xml/ns/j2ee">
	  <xsl:apply-templates select="small-icon"/>
	  <xsl:apply-templates select="large-icon"/>
        </icon>
      </xsl:if>
      <xsl:apply-templates select="tlib-version"/>
      <xsl:apply-templates select="short-name"/>
      <xsl:apply-templates select="uri"/>
      <xsl:apply-templates select="validator"/>
      <xsl:apply-templates select="listener"/>
      <xsl:apply-templates select="tag"/>
    </xsl:element>
  </xsl:template>

  <!-- Strip the id attribute: -->
  <xsl:template match="description">
    <description xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates/>
    </description>
  </xsl:template>

  <!-- Strip the id attribute: -->
  <xsl:template match="display-name">
    <display-name xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates/>
    </display-name>
  </xsl:template>

  <!-- Strip the id attribute: -->
  <xsl:template match="tlib-version">
    <tlib-version xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates/>
    </tlib-version>
  </xsl:template>

  <!-- Strip the id attribute: -->
  <xsl:template match="short-name">
    <short-name xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates/>
    </short-name>
  </xsl:template>

  <!-- Strip the id attribute: -->
  <xsl:template match="uri">
    <uri xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates/>
    </uri>
  </xsl:template>

  <xsl:template match="validator">
    <validator xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates select="description"/>
      <xsl:apply-templates select="validator-class"/>
      <xsl:apply-templates select="init-param"/>
    </validator>
  </xsl:template>

  <xsl:template match="init-param">
    <init-param xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates select="description"/>
      <xsl:apply-templates select="param-name"/>
      <xsl:apply-templates select="param-value"/>
    </init-param>
  </xsl:template>

  <!-- Strip the id attribute: -->
  <xsl:template match="param-name">
    <param-name xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates/>
    </param-name>
  </xsl:template>

  <!-- Strip the id attribute: -->
  <xsl:template match="param-value">
    <param-value xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates/>
    </param-value>
  </xsl:template>

  <!-- Strip the id attribute: -->
  <xsl:template match="listener">
    <listener xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates select="listener-class"/>
    </listener>
  </xsl:template>

  <!-- Strip the id attribute: -->
  <xsl:template match="listener-class">
    <listener-class xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates/>
    </listener-class>
  </xsl:template>

  <xsl:template match="tag">
    <tag xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates select="description"/>
      <xsl:apply-templates select="display-name"/>
      <xsl:if test="small-icon|large-icon">
        <icon xmlns="http://java.sun.com/xml/ns/j2ee">
	  <xsl:apply-templates select="small-icon"/>
	  <xsl:apply-templates select="large-icon"/>
        </icon>
      </xsl:if>
      <xsl:apply-templates select="name"/>
      <xsl:apply-templates select="tag-class"/>
      <xsl:apply-templates select="tei-class"/>
      <xsl:choose>
	<xsl:when test="body-content">
          <xsl:apply-templates select="body-content"/>
	</xsl:when>
	<xsl:otherwise>
	  <!-- 
	    - Explicitly Insert the default body-content since this is 
	    - now a required element
	    -->
	  <body-content xmlns="http://java.sun.com/xml/ns/j2ee">JSP</body-content>
	</xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="variable"/>
      <xsl:apply-templates select="attribute"/>
      <xsl:apply-templates select="example"/>
    </tag>
  </xsl:template>

  <!-- Strip the id attribute: -->
  <xsl:template match="name">
    <name xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates/>
    </name>
  </xsl:template>

  <!-- Strip the id attribute: -->
  <xsl:template match="tag-class">
    <tag-class xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates/>
    </tag-class>
  </xsl:template>

  <!-- Strip the id attribute: -->
  <xsl:template match="tei-class">
    <tei-class xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates/>
    </tei-class>
  </xsl:template>

  <!-- Strip the id attribute: -->
  <xsl:template match="body-content">
    <body-content xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates/>
    </body-content>
  </xsl:template>

  <xsl:template match="variable">
    <variable xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates select="description"/>
      <xsl:apply-templates select="name-given"/>
      <xsl:apply-templates select="name-from-attribute"/>
      <xsl:apply-templates select="variable-class"/>
      <xsl:apply-templates select="declare"/>
      <xsl:apply-templates select="scope"/>
    </variable>
  </xsl:template>

  <xsl:template match="attribute">
    <attribute xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates select="description"/>
      <xsl:apply-templates select="name"/>
      <xsl:apply-templates select="required"/>
      <xsl:apply-templates select="rtexprvalue"/>
      <xsl:apply-templates select="type"/>
    </attribute>
  </xsl:template>

  <!-- Strip the id attribute: -->
  <xsl:template match="required">
    <required xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates/>
    </required>
  </xsl:template>

  <!-- Strip the id attribute: -->
  <xsl:template match="rtexprvalue">
    <rtexprvalue xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates/>
    </rtexprvalue>
  </xsl:template>

  <!-- Strip the id attribute: -->
  <xsl:template match="example">
    <example xmlns="http://java.sun.com/xml/ns/j2ee">
      <xsl:apply-templates/>
    </example>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
