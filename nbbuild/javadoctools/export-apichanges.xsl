<?xml version="1.0" encoding="UTF-8"?>
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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:import href="apichanges.xsl" />

    <xsl:output method="xml" omit-xml-declaration="yes"/>

    <!-- Sep 1, 1997 is the start of the NetBeans epoch  -->
    <xsl:param name="changes-since-year" select="'1997'" />
    <xsl:param name="changes-since-month" select="'09'" />
    <xsl:param name="changes-since-day" select="'01'" />
    <!-- relative path to the api changes document -->
    <xsl:param name="changes-since-url" select="'.'" />
    <!-- amount of changes to print -->
    <xsl:param name="changes-since-amount" select="'65535'" />

    <!-- Main document structure: -->
    <xsl:template match="/" name="api-changes" >
        <!-- amount of changes to print -->
        <xsl:param name="changes-since-amount" select="$changes-since-amount" />
         <xsl:text>

         
</xsl:text>
        <xsl:comment>Search for dates that are later or equal to <xsl:value-of select="$changes-since-year" 
          />-<xsl:value-of select="$changes-since-month" />-<xsl:value-of select="$changes-since-day" /> in
          <xsl:value-of select="$changes-since-url" />
        </xsl:comment>
        <xsl:apply-templates select="//change" mode="changes-since" >
            <xsl:with-param name="changes-since-amount" select="$changes-since-amount" />
            <xsl:sort data-type="number" order="descending" select="date/@year"/>
            <xsl:sort data-type="number" order="descending" select="date/@month"/>
            <xsl:sort data-type="number" order="descending" select="date/@day"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- Summarizing links to changes: -->
    <xsl:template match="change" mode="changes-since" >
        <xsl:param name="changes-since-amount" select="$changes-since-amount" />
        
        <xsl:variable name="day" select="date/@day" />
        <xsl:variable name="month" select="date/@month" />
        <xsl:variable name="year" select="date/@year" />

        <xsl:variable name="number-of-newer" select="count(
            //change[ 
              (number(date/@year) > number($year)) or
              (number(date/@year) = number($year) and number(date/@month) > number($month)) or
              (number(date/@year) = number($year) and number(date/@month) = number($month) and number(date/@day) > number($day))
            ]
        )" />
         
         <xsl:text>
</xsl:text>
        <xsl:comment>Checking date <xsl:value-of select="$year" 
          />-<xsl:value-of select="$month" />-<xsl:value-of select="$day" 
          /> with count of newer <xsl:value-of select="$number-of-newer" />
        </xsl:comment>
       <xsl:choose>
            <xsl:when test="number($number-of-newer) >= number($changes-since-amount)" >
                <xsl:comment>Skipped as the amount of changes is too big</xsl:comment>
            </xsl:when>
            <xsl:when test="number(date/@year) > number($changes-since-year)">
                <xsl:comment>year ok</xsl:comment>
                <xsl:call-template name="print-change" />
            </xsl:when>
            <xsl:when test="number($changes-since-year) = number(date/@year)">
                <xsl:comment>year equal</xsl:comment>
                <xsl:choose>
                    <xsl:when test="number(date/@month) > number($changes-since-month)">
                        <xsl:comment>month ok</xsl:comment>
                        <xsl:call-template name="print-change" />
                    </xsl:when>
                    <xsl:when test="number($changes-since-month) = number(date/@month)">
                        <xsl:comment>month equal</xsl:comment>
                        <xsl:if test="number(date/@day) >= number($changes-since-day) ">
                            <xsl:comment>day ok</xsl:comment>
                            <xsl:call-template name="print-change" />
                        </xsl:if>
                    </xsl:when>
                 </xsl:choose>
            </xsl:when>
         </xsl:choose>
         
    </xsl:template>
        
    <xsl:template name="print-change" >
        <xsl:text>
</xsl:text>
        <change>
            <xsl:attribute name="id"><xsl:call-template name="change-id"/></xsl:attribute>
            <xsl:attribute name="url"><xsl:value-of select="$changes-since-url" /></xsl:attribute>
            <xsl:copy-of select="*" />
        </change>
    </xsl:template>

</xsl:stylesheet>
