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
    <xsl:output method="html"/>

    <!-- unique key over all groups of apis -->
    <xsl:key match="//api" name="apiGroups" use="@group" />
    <!-- unique key over all names of apis -->
    <xsl:key match="//api" name="apiNames" use="@name" />

    <xsl:param name="arch.stylesheet"/>
    <xsl:param name="arch.overviewlink"/>
    <xsl:param name="arch.footer"/>
    <xsl:param name="arch.answers.date"/>
    <xsl:param name="arch.when"/>
    <xsl:param name="javadoc-header" />
    <xsl:param name="deprecated" />
    <xsl:template match="/">
        <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
        <html lang="en">
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
                <title><xsl:value-of select="api-answers/@module" /> - NetBeans Architecture Questions</title>
                <xsl:if test="$arch.stylesheet">
                    <link rel="stylesheet" type="text/css" href="{$arch.stylesheet}"/>
                </xsl:if>
                <xsl:if test="$javadoc-header">
                 <link rel="stylesheet" type="text/css" href="resource-files/javadoc.css" title="Style"/>
                 <script type="text/javascript" src="script-files/script.js"></script>
                 <script type="text/javascript" src="script-files/jquery-3.7.1.min.js"></script>
                 <script type="text/javascript" src="script-files/jquery-ui.min.js"></script>
                </xsl:if>
            </head>
            <body>
             <script type="text/javascript">var pathtoroot = "./";
              loadScripts(document, 'script');</script>
             <noscript>
              <div>JavaScript is disabled on your browser.</div>
             </noscript>
              <header role="banner">
               <nav role="navigation">
                <!-- ========= START OF TOP NAVBAR ======= -->
                <div class="top-nav" id="navbar-top">
                 <div class="nav-content">
                  <div class="nav-menu-button">
                   <button id="navbar-toggle-button" aria-controls="navbar-top" aria-expanded="false" aria-label="Toggle navigation links">
                    <span class="nav-bar-toggle-icon">
                     <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
                    </span>
                    <span class="nav-bar-toggle-icon">
                     <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
                    </span>
                    <span class="nav-bar-toggle-icon">
                     <xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
                    </span>
                   </button>
                  </div>
                  <div class="skip-nav">
                   <a href="#skip-navbar-top" title="Skip navigation links">Skip navigation links</a>
                  </div>

                  <ul id="navbar-top-firstrow" class="nav-list" title="Navigation">
                   <li>
                    <a href="apichanges.html">API Changes</a>
                   </li>
                   <li class="nav-bar-cell1-rev">Architecture Summary
                   </li>
                   <li>
                    <a href="index.html">Overview</a>
                   </li>
                   <li>
                    <a href="overview-tree.html">Tree</a>
                   </li>
                   <xsl:if test="$deprecated='true'">
                       <li>
                           <a href="deprecated-list.html">Deprecated</a>
                       </li>   
                   </xsl:if>
                   <li>
                    <a href="index-files/index-1.html">Index</a>
                   </li>
                   <li><a href="search.html">Search</a></li>
                   <li>
                    <a href="help-doc.html#overview">Help</a>
                   </li>
                  </ul>

                  <div class="about-language">
                   <xsl:value-of select="$javadoc-header"/>
                  </div>
                 </div>
                </div>
                <div class="sub-nav">
                 <div class="nav-content">
                  <ol class="sub-nav-list"></ol>
                  <div class="nav-list-search">
                   <input type="text" id="search-input" disabled="disabled" placeholder="Search" aria-label="Search in documentation" autocomplete="off"/>
                   <input type="reset" id="reset-search" disabled="disabled" value="Reset"/>
                  </div>
                 </div>
                </div>
                <!-- ========= END OF TOP NAVBAR ========= -->
                <span class="skip-nav" id="skip-navbar-top"></span>
               </nav>
              </header>
              <div class="main-grid">
               <main role="main">
                <h1>NetBeans Architecture Answers for <xsl:value-of select="api-answers/@module" /><xsl:text> module</xsl:text></h1>

                <xsl:variable name="qver" select="api-answers/api-questions/@version"/>
                <xsl:variable name="afor" select="api-answers/@question-version" />

                <ul>
                <li><b>Author:</b><xsl:text> </xsl:text><xsl:value-of select="api-answers/@author" /></li>
                <li><b>Answers as of:</b><xsl:text> </xsl:text><xsl:value-of select="$arch.answers.date"/></li>
                <li><b>Answers for questions version:</b><xsl:text> </xsl:text><xsl:value-of select="$afor" /></li>
                <li><b>Latest available version of questions:</b><xsl:text> </xsl:text><xsl:value-of select="$qver" /></li>
                </ul>

                <xsl:if test="not($qver=$afor)">
                    <strong>
                        WARNING: answering questions version <xsl:value-of select="$afor"/>
                        rather than the current <xsl:value-of select="$qver"/>.
                    </strong>
                </xsl:if>

                <hr/>
                <h2>Interfaces table</h2>

                <xsl:call-template name="generate-api-table">
                    <xsl:with-param name="target" >api-group</xsl:with-param>
                </xsl:call-template>


                <xsl:variable name="all_interfaces" select="//api" />
                <xsl:if test="not($all_interfaces)" >
                    <b> WARNING: No imported or exported interfaces! </b>
                </xsl:if>

                <xsl:apply-templates />

                <xsl:if test="$arch.footer">
                    <hr/>
                    <p><xsl:value-of select="$arch.footer"/></p>
                </xsl:if>
               </main>
              </div>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="category">
        <hr/>
        <h2>
            <xsl:value-of select="@name" />
        </h2>
        <ul>
            <xsl:for-each select="question">
                <xsl:call-template name="answer" />
            </xsl:for-each>
        </ul>
    </xsl:template>


    <xsl:template name="answer">
        <xsl:variable name="value" select="@id" />

        <p/>
        <font color="gray" >
        <b><a name="answer-{@id}">Question (<xsl:value-of select="@id"/>)</a>:</b> <em><xsl:apply-templates select="./node()" /></em>
        </font>
        <p/>

        <xsl:choose>
            <xsl:when test="count(//answer[@id=$value])" >
                <b>Answer:</b> <!-- <xsl:value-of select="//answer[@id=$value]" /> -->
                <xsl:apply-templates select="//answer[@id=$value]/node()" />
            </xsl:when>
            <xsl:when test="string-length($arch.when)=0 or contains($arch.when,@when)" >
                <b>WARNING:</b>
                <xsl:text> Question with id="</xsl:text>
                <i>
                <xsl:value-of select="@id" />
                </i>
                <xsl:text>" has not been answered!</xsl:text>
             </xsl:when>
             <xsl:otherwise>
                 <i>Needs to be yet answered in <xsl:value-of select="@when" /> phase.</i>
              </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="api">
        <!-- generates link to given API -->
        <xsl:variable name="name" select="@name" />
        <xsl:variable name="group" select="@group" />

        <a>
            <xsl:attribute name="href" >
                <xsl:text>#</xsl:text><xsl:value-of select="$group" /><xsl:text>-</xsl:text><xsl:value-of select="$name" />
            </xsl:attribute>
            <xsl:value-of select="$name" />
        </a>
        <!-- put "- and description" there only if there are some child nodes -->
        <xsl:if test="child::node()" >
            - <xsl:apply-templates />
        </xsl:if>
    </xsl:template>

    <xsl:template match="usecase">
        <h4><xsl:value-of select="@name" /></h4>
        <xsl:apply-templates select="./node()" />
    </xsl:template>

    <!-- Format random HTML elements as is: -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>


    <xsl:template match="answer">
        <!-- ignore direct answers -->
    </xsl:template>
    <xsl:template match="hint">
        <!-- ignore direct answers -->
    </xsl:template>

    <!-- enumerates all groups of APIs and calls given template
      on each of them
    -->
    <xsl:template name="generate-api-table" >
        <xsl:param name="target" />
        <xsl:param name="generate-export" select="'true'" />
        <xsl:param name="generate-import" select="'true'" />
        <xsl:param name="generate-group" select="''" />

        <xsl:for-each select="//api[
            generate-id() = generate-id(key('apiGroups', @group))
            and
            ($generate-group = '' or @group = $generate-group)
        ]">
            <xsl:call-template name="jump-to-target">
                <xsl:with-param name="group" select="@group" />
                <xsl:with-param name="target" select="$target" />
                <xsl:with-param name="generate-export" select="$generate-export" />
                <xsl:with-param name="generate-import" select="$generate-import" />
            </xsl:call-template>
        </xsl:for-each>

    </xsl:template>
    <xsl:template name="jump-to-target" >
        <xsl:param name="target" />
        <xsl:param name="group" />
        <xsl:param name="generate-export" />
        <xsl:param name="generate-import" />

        <xsl:choose>
            <xsl:when test="$target='api-group'" >
                <xsl:call-template name="api-group">
                    <xsl:with-param name="group" select="$group" />
                    <xsl:with-param name="generate-export" select="$generate-export" />
                    <xsl:with-param name="generate-import" select="$generate-import" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message>
                    WRONG TARGET: <xsl:value-of select="$target"/>
                </xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <!-- displays group of APIs -->

    <xsl:template name="api-group" >
        <xsl:param name="group" />
        <xsl:param name="generate-export" />
        <xsl:param name="generate-import" />

        <xsl:element name="h3">
            <xsl:attribute name="id" >
                <xsl:text>group-</xsl:text><xsl:value-of select="$group" />
            </xsl:attribute>
            Group of <xsl:value-of select="$group"/> interfaces
        </xsl:element>

        <xsl:variable
            name="all_interfaces"
            select="//api[@group=$group and
                          generate-id() = generate-id(key('apiNames', @name)) and
                          (
                            ($generate-export = 'true' and @type = 'export')
                            or
                            ($generate-import = 'true' and @type = 'import')
                          )
                  ]"
        />
        <!--<table class="tablebg"><tr><td>-->
        <table class="tableapigroup">
            <caption>
                Group of <xsl:value-of select="$group"/> interfaces
            </caption>
            <thead>
            <tr class="tablersh">
                <th><span class="titlectable">Interface Name</span></th>
                <th><span class="titlectable">In/Out</span></th>
                <th><span class="titlectable">Stability</span></th>
                <th><span class="titlectable">Specified in What Document?</span></th>
            </tr>
            </thead>
            <tbody>
            <xsl:for-each select="$all_interfaces ">
                <xsl:call-template name="api-group-name" >
                    <xsl:with-param name="name" select="@name" />
                    <xsl:with-param name="group" select="$group" />
                    <xsl:with-param name="category" select="@category" />
                    <xsl:with-param name="type" select="@type" />
                </xsl:call-template>
            </xsl:for-each>
            </tbody>
          </table>
        <!--</td></tr></table>-->
    </xsl:template>

    <!-- the template to convert an instances of API into an HTML line in a table
      describing the API -->

    <xsl:template name="api-group-name" >
       <xsl:param name="name" />
       <xsl:param name="group" />
       <xsl:param name="category" />
       <xsl:param name="type" />

        <tr class="tabler">
            <td>
                <xsl:value-of select="$name"/>
            </td>
            <xsl:if test="$type" >
                <td> <!-- imported/exported -->
                    <xsl:choose>
                        <xsl:when test="$type='import'">Imported</xsl:when>
                        <xsl:when test="$type='export'">Exported</xsl:when>
                        <xsl:otherwise>
                            <xsl:message>
                                WARNING: <xsl:value-of select="$type"/>
                            </xsl:message>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </xsl:if>
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

            <td> <!-- description -->
                <!-- Put anchor here, since name is centered, and we want hyperlinks to scroll to top of table row: -->
                <a>
                    <xsl:attribute name="id">
                        <xsl:value-of select="$group" /><xsl:text>-</xsl:text><xsl:value-of select="$name"/>
                    </xsl:attribute>
                </a>
                    <xsl:call-template name="describe">
                        <xsl:with-param name="name" select="$name"/>
                        <xsl:with-param name="group" select="$group"/>
                    </xsl:call-template>

            </td>
        </tr>
    </xsl:template>
    <xsl:template name="describe">
       <xsl:param name="name" />
       <xsl:param name="group" />

       <xsl:variable name="all_definitions" select="//api[@group=$group and @name=$name]" />
       <xsl:for-each select="$all_definitions" >
            <xsl:variable name="describe.node" select="./node()" />

            <xsl:variable name="before-hash-sign" select="substring-before(@url,'#')" />

            <xsl:if test="@url" >
                <a>
                    <xsl:attribute name="href" >
                        <xsl:value-of select="@url" />
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="$before-hash-sign and string-length($before-hash-sign) > 40" >
                            .../<xsl:value-of select="substring-after(substring($before-hash-sign, string-length(@before-hash-sign) - 40),'/')" />
                        </xsl:when>
                        <xsl:when test="$before-hash-sign" >
                            <xsl:value-of select="$before-hash-sign" />
                        </xsl:when>

                        <xsl:when test="string-length(@url) > 40">
                            .../<xsl:value-of select="substring-after(substring(@url, string-length(@url) - 40),'/')" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="@url" />
                        </xsl:otherwise>
                    </xsl:choose>

                </a>
            </xsl:if>

            <xsl:if test="$describe.node" >
                <xsl:apply-templates select="$describe.node" />
            </xsl:if>
       </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
