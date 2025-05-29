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
    <xsl:import href="jsonhelp.xsl" />
    <xsl:import href="export2allmodules.xsl" />
    <xsl:output method="html"/>
    <xsl:param name="date" />
    <xsl:param name="maturity" />
    <xsl:param name="version" />
    <xsl:param name="releaseinfo" />
    <xsl:template match="/" >
        <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
        <html>
            <xsl:call-template name="htmlheader" >
                <xsl:with-param name="title" >APIs Usecases list</xsl:with-param>
                <xsl:with-param name="maturity" select="$maturity" />
                <xsl:with-param name="version" select="$version"/>
            </xsl:call-template>

            <body>
                <xsl:call-template name="htmlmainmenu" >
                    <xsl:with-param name="title" >APIs Usecases list</xsl:with-param>
                    <xsl:with-param name="maturity" select="$maturity" />
                    <xsl:with-param name="version" select="$version"/>
                    <xsl:with-param name="releaseinfo" select="$releaseinfo"/>
                    <xsl:with-param name="menukey" >usecases</xsl:with-param>
                </xsl:call-template>
                <div class="apidocmaincontent">
                    <xsl:call-template name="build-docmenu" >
                        <xsl:with-param name="menukey" >usecases</xsl:with-param>
                        <xsl:with-param name="date" select="$date"/>
                    </xsl:call-template>

                    <div class="innercontent">
                        <div class="abstract">
                            This page contains extracted usecases for some of the NetBeans modules
                            that <a href="index.html">offer an API</a>.
                        </div>

                        <xsl:for-each select="//module/arch-usecases[not(../@name='_no module_') and not(.='No answer')]" >
                            <hr/>
                            <h2>
                                <xsl:variable name="idreplaced" select="../@name"/>
                                <xsl:attribute name="id">
                                        <xsl:text>usecase-</xsl:text>
                                        <xsl:value-of select="translate($idreplaced,' ','-')"/>
                                </xsl:attribute>
                                <xsl:text>How to use </xsl:text>
                                <a>
                                    <xsl:attribute name="href" >
                                        <xsl:text>index.html#def-api-</xsl:text>
                                        <xsl:value-of select="translate($idreplaced,' ','-')"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="../@name"/>
                                ?</a>
                            </h2>
                            <p>
                            <xsl:apply-templates select="../description/node()" />
                            </p>
                            <p/>
                            <xsl:apply-templates />
                        </xsl:for-each>
                    </div>

                </div>
                <div class="apidocleft">
                    <xsl:call-template name="listallmodules" />
                </div>
                <xsl:call-template name="htmlfooter" />
            </body>
        </html>
    </xsl:template>

    <xsl:template match="api-ref">
        <!-- simply bold the name, it link will likely be visible bellow -->
        <b>
            <xsl:value-of select="@name" />
        </b>
    </xsl:template>

    <xsl:template match="usecase">
        <h4><xsl:value-of select="@name" /></h4>
        <xsl:apply-templates select="./node()" />
    </xsl:template>

<!--
    <xsl:template match="a[@href]">
        <xsl:variable name="target" select="ancestor::module/@target"/>
        <xsl:variable name="top" select="substring-before($target,'/')" />

              <xsl:call-template name="print-url" >
                <xsl:with-param name="url" select="@href" />
                <xsl:with-param name="base" select="$target" />
                <xsl:with-param name="top" select="$top" />
              </xsl:call-template>
        </xsl:template>
    -->
    <xsl:template name="print-url" >
        <xsl:param name="url" />
        <xsl:param name="base" />
        <xsl:param name="top" />

        <xsl:choose>
            <xsl:when  test="contains(@href,'@TOP@')" >
                <xsl:comment>URL contains @TOP@</xsl:comment>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="$top" />
                        <xsl:text>/</xsl:text>
                        <xsl:value-of select="substring-after($url,'@TOP@')" />
                    </xsl:attribute>
                    <xsl:apply-templates />
                </a>
            </xsl:when>
            <xsl:when test="contains($url,'//')" >
                <xsl:comment>This is very likely URL with protocol, if not see nbbuild/javadoctools/export2usecases.xsl</xsl:comment>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="$url" />
                    </xsl:attribute>
                    <xsl:apply-templates />
                </a>
            </xsl:when>
            <xsl:when test="starts-with($url, '#')" >
                <xsl:comment>Probably reference in the same target document</xsl:comment>
                <a href="{$base}{$url}" >
                    <xsl:apply-templates />
                </a>
            </xsl:when>
            <xsl:otherwise>
                <xsl:comment>This must be a reference releative to the arch page, if not see nbbuild/javadoctools/export2usecases.xsl</xsl:comment>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="$base" />
                        <xsl:text>/../</xsl:text>
                        <xsl:value-of select="$url" />
                    </xsl:attribute>
                    <xsl:apply-templates />
                </a>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy  >
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="api">
     <xsl:param name="group" />
     <xsl:param name="type" />
    
     <xsl:variable name="name" select="@name" />
     <xsl:variable name="category" select="@category" />
     <xsl:variable name="url" select="@url" />

     <xsl:choose> 
      <xsl:when test="string-length($url)>0">
       <a>
        <xsl:attribute name="href">
         <xsl:value-of select="$url" />
        </xsl:attribute>
        <xsl:value-of select="$name" />
       </a>
      </xsl:when>
      <xsl:otherwise>
       <xsl:value-of select="$name" />
      </xsl:otherwise>
     </xsl:choose>

     <xsl:apply-templates />
    </xsl:template>
    <!-- special html 5 rewrite -->
    <xsl:template match="a/@shape" />
    <xsl:template match="pre/@space" />
    <xsl:template match="pre/@xml:space" />
</xsl:stylesheet>


