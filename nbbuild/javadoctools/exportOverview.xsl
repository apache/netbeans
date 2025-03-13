<?xml version="1.0" encoding="UTF-8" ?>
<!--

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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:import href="../antsrc/org/netbeans/nbbuild/Arch.xsl" />
    <xsl:import href="export-apichanges.xsl" />
    <xsl:output method="html" />

    <xsl:param name="arch.stylesheet"/>
    <xsl:param name="arch.overviewlink"/>
    <xsl:param name="arch.footer"/>
    <xsl:param name="arch.target"/>

    <xsl:template match="/">
        <xsl:variable name="interfaces" select="//api[@type='export']" />

        <html>
         <head>
          <title>Overview</title><!-- note this is ignored -->
         </head>
         <body>
          <h1>Overview of <xsl:value-of select="api-answers/@module" /><xsl:text> module</xsl:text></h1>
          <xsl:apply-templates select="api-answers/answer[@id='arch-overall']/node()" mode="description"/>
          

          <h2>What is New (see <a href="apichanges.html">all changes</a>)?</h2>

          <ul>
              <xsl:call-template name="api-changes" >
                  <xsl:with-param name="changes-since-url" select="'apichanges.html'" />
                  <xsl:with-param name="changes-since-amount" select="'5'" />
              </xsl:call-template>
          </ul>
          
          <h2>Use Cases</h2>
          
          <xsl:apply-templates select="//answer[@id='arch-usecases']/node()" mode="description" />
          
          <h2>Exported Interfaces</h2>
          
                This table lists all of the module exported APIs 
                with 
                defined stability classifications. It is generated
                based on answers to questions about the architecture 
                of the module. <a href="architecture-summary.html">Read them all</a>...

                <!-- imported from Arch.xsl -->
                <xsl:call-template name="generate-api-table" >
                    <xsl:with-param name="target" select="'api-group'" />
                    <xsl:with-param name="generate-import" select="'false'" />
                </xsl:call-template>
        
            <h2>Implementation Details</h2>

            <xsl:if test="api-answers/answer[@id='arch-where']/node()" >
                <h3>Where are the sources for the module?</h3>
                <xsl:apply-templates select="api-answers/answer[@id='arch-where']/node()" mode="description"/>
            </xsl:if>
            
            <xsl:if test="api-answers/answer[@id='deploy-dependencies']/node()" >
                <h3>What do other modules need to do to declare a dependency on this one, in addition to or instead of a plain module dependency?</h3>
                <xsl:apply-templates select="api-answers/answer[@id='deploy-dependencies']/node()" mode="description"/>
            </xsl:if>

            
            <p>
                Read more about the implementation in the <a href="architecture-summary.html">answers to 
                architecture questions</a>.
            </p>
            
            <!--
                <xsl:call-template name="generate-api-table" >
                    <xsl:with-param name="target" select="'api-group'" />
                    <xsl:with-param name="generate-export" select="'false'" />
                    <xsl:with-param name="generate-group" select="'java'" />
                </xsl:call-template>
            -->
         </body>
        </html>
         
         
        <!--
        <module name="{api-answers/@module}"
                target="{$arch.target}"
                stylesheet="{$arch.stylesheet}"
                overviewlink="{$arch.overviewlink}"
                footer="{$arch.footer}">
            
            <xsl:variable name="deploy-dependencies" select="api-answers/answer[@id='deploy-dependencies']"/>
            <xsl:if test="$deploy-dependencies">
                <deploy-dependencies>
                    <xsl:apply-templates select="$deploy-dependencies/node()"/>
                </deploy-dependencies>
            </xsl:if>
            
            <xsl:variable name="arch-usecases" select="api-answers/answer[@id='arch-usecases']"/>
            <xsl:if test="$arch-usecases">
                <arch-usecases>
                    <xsl:apply-templates select="$arch-usecases/node()"/>
                </arch-usecases>
            </xsl:if>            


        </module>
        -->
    </xsl:template>

    <xsl:template match="api" mode="description">
        <xsl:param name="group" />
        <xsl:param name="type" />
    
        <xsl:variable name="name" select="@name" />
        <xsl:variable name="category" select="@category" />
        <xsl:variable name="url" select="@url" />

        <xsl:choose> 
          <xsl:when test="string-length($url)>0">
            <a>
              <xsl:attribute name="href"><xsl:value-of select="$url" /></xsl:attribute>
              <xsl:value-of select="$name" />
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$name" />
          </xsl:otherwise>
        </xsl:choose>

        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="usecase" mode="description" >
        <h3><xsl:value-of select="@name" /></h3>
        <xsl:apply-templates select="./node()" mode="description" />
    </xsl:template>
    
    <xsl:template match="@*|node()" mode="description" >
       <xsl:copy  >
          <xsl:apply-templates select="@*|node()" mode="description" />
       </xsl:copy>
    </xsl:template>
     
    <!-- Format random HTML elements as is: -->
    <xsl:template match="@*|node()">
       <xsl:copy  >
          <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
    </xsl:template>

    <!-- special html 5 rewrite -->
    <xsl:template match="a/@shape" />
    <xsl:template match="a/@shape" mode="description" />
    <xsl:template match="pre/@space" />
    <xsl:template match="pre/@space" mode="description"/>
    
    
    <!-- format the API table -->
    <xsl:template name="export-api">
        <xsl:param name="arch.target" />
        <xsl:variable name="name" select="@name" />
        <xsl:variable name="type" select="@type" />
        <xsl:variable name="group" select="@group" />
        <xsl:variable name="category" select="@category" />
        <xsl:variable name="url" select="@url" />
        <xsl:variable name="description" select="node()" />

        <tr class="tabler">
            <td>
                <a>
                    <xsl:attribute name="href" >
                        <xsl:value-of select="$arch.target" />
                        <xsl:text>#</xsl:text>
                        <xsl:value-of select="$group"/>
                        <xsl:text>-</xsl:text>
                        <xsl:value-of select="$name"/>
                    </xsl:attribute>
                    <xsl:value-of select="$name" />
                </a>
            </td>
            <!--
            <td>
                <xsl:choose>
                    <xsl:when test="$type='import'">Imported</xsl:when>
                    <xsl:when test="$type='export'">Exported</xsl:when>
                    <xsl:otherwise>WARNING: <xsl:value-of select="$type" /></xsl:otherwise>
                </xsl:choose>
            </td> -->
            <td> <!-- stability category -->
                <a>
                    <xsl:attribute name="href">
                        <xsl:text>https://netbeans.apache.org/wiki/API_Stability#</xsl:text>
                        <xsl:choose>
                            <xsl:when test="$category='official'">official</xsl:when>
                            <xsl:when test="$category='stable'">stable</xsl:when>
                            <xsl:when test="$category='devel'">devel</xsl:when>
                            <xsl:when test="$category='third'">third_party</xsl:when>
                            <xsl:when test="$category='standard'">standard</xsl:when>
                            <xsl:when test="$category='friend'">friend</xsl:when>
                            <xsl:when test="$category='private'">private</xsl:when>
                            <xsl:when test="$category='deprecated'">deprecated</xsl:when>
                            <xsl:otherwise>
                                <xsl:message>
                                    WARNING: <xsl:value-of select="$category"/>
                                </xsl:message>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="$category='official'">Official</xsl:when>
                        <xsl:when test="$category='stable'">Stable</xsl:when>
                        <xsl:when test="$category='devel'">Under Development</xsl:when>
                        <xsl:when test="$category='third'">Third party</xsl:when>
                        <xsl:when test="$category='standard'">Standard</xsl:when>
                        <xsl:when test="$category='friend'">Friend</xsl:when>
                        <xsl:when test="$category='private'">Private</xsl:when>
                        <xsl:when test="$category='deprecated'">Deprecated</xsl:when>
                        <xsl:otherwise>
                            <xsl:message>
                                WARNING: <xsl:value-of select="$category"/>
                            </xsl:message>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>  
            </td>

            <td> <!-- url -->
                <a href="{$url}"><xsl:value-of select="$url"/></a>
                <xsl:if test="$description" >
                    <p>
                        <xsl:apply-templates select="$description" />
                    </p>
                </xsl:if>
            </td>
        </tr>

    </xsl:template>
    
    <xsl:template name="print-change" >
        <li>
            <xsl:choose>
                <xsl:when test="date/@month=1">Jan</xsl:when>
                <xsl:when test="date/@month=2">Feb</xsl:when>
                <xsl:when test="date/@month=3">Mar</xsl:when>
                <xsl:when test="date/@month=4">Apr</xsl:when>
                <xsl:when test="date/@month=5">May</xsl:when>
                <xsl:when test="date/@month=6">Jun</xsl:when>
                <xsl:when test="date/@month=7">Jul</xsl:when>
                <xsl:when test="date/@month=8">Aug</xsl:when>
                <xsl:when test="date/@month=9">Sep</xsl:when>
                <xsl:when test="date/@month=10">Oct</xsl:when>
                <xsl:when test="date/@month=11">Nov</xsl:when>
                <xsl:when test="date/@month=12">Dec</xsl:when>
            </xsl:choose><xsl:text> </xsl:text>
            <xsl:value-of select="date/@day"/> '<xsl:value-of select="substring(date/@year, 3, 2)"/>
            <xsl:text> </xsl:text>
            <a><xsl:attribute name="href">apichanges.html#<xsl:call-template name="change-id"/></xsl:attribute>
                <xsl:apply-templates select="summary/node()"/>
            </a>
            <!--<p> -->
                <xsl:apply-templates select="description/node()" mode="description" />
            <!-- </p> -->
        </li>
    </xsl:template>
    
</xsl:stylesheet> 
