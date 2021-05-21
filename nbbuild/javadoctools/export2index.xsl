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
    <xsl:param name="download" select="'true'"/>
    <xsl:param name="maturity" />
    <xsl:param name="version" />
    <xsl:param name="releaseinfo" />
    <!-- unique key over all groups of apis -->
    <xsl:key match="//api[@type='export']" name="apiGroups" use="@group" />
    <!-- unique key over all names of apis -->
    <xsl:key match="//api" name="apiNames" use="@name" />
    <xsl:template match="/" >
        <!-- <frameset cols="20%,80%" title="" onLoad="top.loadFrames()">
            <frameset rows="30%,70%" title="" onLoad="top.loadFrames()">
                <frame src="overview-frame.html" name="packageListFrame" title="All Modules"/>
                <frame src="allclasses-frame.html" name="packageFrame" title="All classes"/>
            </frameset>
            <frame src="overview-summary.html" name="classFrame" title="Module, package, class and interface descriptions" scrolling="yes"/>
            <noframes>
                <h2>Frame Alert</h2>
                <p>
                    This document is designed to be viewed using the frames feature. If you see this message, you are using a non-frame-capable web client.
                    Link to <a href="overview-summary.html">Non-frame version.</a>
                </p>
            </noframes>
        </frameset>-->
        <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
        <html>
            <xsl:call-template name="htmlheader" >
                <xsl:with-param name="title" >APIs Overview</xsl:with-param>
                <xsl:with-param name="maturity" select="$maturity" />
                <xsl:with-param name="version" select="$version"/>
            </xsl:call-template>
        
            <body>
                <xsl:call-template name="htmlmainmenu" >
                    <xsl:with-param name="title" >APIs Overview</xsl:with-param>
                    <xsl:with-param name="maturity" select="$maturity" />
                    <xsl:with-param name="version" select="$version"/> 
                    <xsl:with-param name="releaseinfo" select="$releaseinfo"/>
                    <xsl:with-param name="menukey" >index</xsl:with-param>
                </xsl:call-template>
    
                <div class="apidocmaincontent">
                    <xsl:call-template name="build-docmenu" >
                        <xsl:with-param name="menukey" >index</xsl:with-param>
                        <xsl:with-param name="date" select="$date"/>
                    </xsl:call-template>
                
                    <div class="innercontent">
                        <div class="abstract">
                            This document provides a list of <em>NetBeans APIs</em> with a short description
                            of what they are used for, and a table describing different types of interfaces
                            (see <a href="http://wiki.netbeans.org/API_Design">What is
                                an API?</a> to understand why we list DTDs, file formats, etc.) and with
                            a stability category (<span style="background:#ffffff">stable and official</span>,
                            <span style="background:#ddcc80">under development</span>,
                            <span style="background:#afafaf;text-decoration:line-through">deprecated</span>,
                            <span style="background:#e0c0c0">friend or private</span>;
                            see <a href="http://wiki.netbeans.org/API_Stability">API stability</a> for more info).
                            The aim is to provide as detailed a definition of NetBeans module 
                            external interfaces as possible and give other developers a chance to decide
                            whether they want to depend on a particular API or not.
                            <p/>
                            Some of these APIs are part of the 
                            <a href="http://platform.netbeans.org/">NetBeans Platform</a>
                            (for example lookup,
                            loaders, utilities, nodes, explorer, window systems, multiview, etc.), some of them
                            are specific to 
                            <a href="http://www.netbeans.org/products/ide/index.html">NetBeans IDE</a> 
                            (projects, javacore, diff, etc.) and some
                            are not included in the release at all and are just provided for download
                            (usually via autoupdate). Basically when building an application based on
                            <em>NetBeans</em> one is free to choose the set of modules and their APIs 
                            to satisfy one's needs.
                            <!--<p>
                                This is a list of APIs for 
                                <xsl:call-template name="apachenetbeansversion" >
                                    <xsl:with-param name="maturity">
                                        <xsl:value-of select="$maturity"/>                
                                    </xsl:with-param>
                                    <xsl:with-param name="version">
                                        <xsl:value-of select="$version"/>                
                                    </xsl:with-param>
                                </xsl:call-template>
                            </p>-->
                            <p>To get the API of your module listed here, see the documentation for the Javadoc building <a href="http://wiki.netbeans.org/APIDevelopment">infrastructure</a>.</p>
                        
                            <p>Can't find what you're looking for? Try the <a href="https://netbeans.apache.org" target="_top">Apache NetBeans website</a>.</p>
                        </div>
                        <hr/>
                        <xsl:call-template name="list-modules" />
                        <hr/>
                        <xsl:apply-templates select="/alldata/apis" />
                    </div>
                </div>
                <div class="apidocleft">
                    <xsl:call-template name="listallmodules" />
                </div>
                
                <xsl:call-template name="htmlfooter" />
                
            </body>
        </html>
    </xsl:template>
    <xsl:template name="list-modules">
        <ul>
            <xsl:for-each select="/alldata/apis/module" >
                <xsl:sort select="@name" />
                <xsl:choose>
                    <xsl:when test="api" >
                        <li>
                            <span>
                                <xsl:attribute name="style">
                                    <xsl:choose>
                                        <xsl:when test="descendant::api[@category='stable' and @group='java']">background:#ffffff</xsl:when>
                                        <xsl:when test="descendant::api[@category='official' and @group='java']">background:#ffffff</xsl:when>
                                        <xsl:when test="descendant::api[@category='devel' and @group='java']">background:#ddcc80</xsl:when>
                                        <xsl:when test="descendant::api[@category='deprecated' and @group='java']">text-decoration: line-through</xsl:when>
                                        <xsl:otherwise>background:#e0c0c0</xsl:otherwise>
                                    </xsl:choose>
                                </xsl:attribute>
                                <a>
                                    <xsl:attribute name="href">
                                        <xsl:value-of select="substring-before(@target,'/')" />/overview-summary.html</xsl:attribute>
                                    <xsl:value-of select="@name"/>
                                </a> -
                                <!-- XXX the following is crap; e.g. messes up descs of Dialogs API, I/O API, ... -->
                                <!-- Should use e.g.:
                                <answer id="arch-what">
                                    <span class="summary">This API does such-and-such.</span>
                                    It also does some other less important stuff.
                                </answer>
                                -->
                                <xsl:comment>Begin of first sentenece</xsl:comment>
                                <xsl:apply-templates mode="first-sentence" select="description" />
                                <xsl:comment>End of first sentenece</xsl:comment>.
                            </span>
                        </li>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- will be covered later -->
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            <xsl:for-each select="/alldata/apis/module" >
                <xsl:sort select="api" order="descending" />
                <xsl:sort select="@name" />
                <xsl:choose>
                    <xsl:when test="api" >
                        <!-- covered before -->
                    </xsl:when>
                    <xsl:otherwise>
                        <li>
                            <xsl:variable name="where" select="substring-before(@target, '/')"/>
                            <b>
                                <a href="{$where}/overview-summary.html">
                                    <xsl:value-of select="$where"/>
                                </a>
                            </b>
                            - no API description provided
                            (see <a href="http://wiki.netbeans.org/APIDevelopment">how to do it</a>)
                        </li>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </ul>
    </xsl:template>
    <xsl:template match="module">
        <xsl:variable name="interfaces" select="descendant::api[@type='export' and generate-id() = generate-id(key('apiNames', @name))]" />
        <xsl:variable name="module.name" select="@name" />
        <xsl:variable name="arch.stylesheet" select="@stylesheet" />
        <xsl:variable name="arch.overviewlink" select="@overviewlink" />
        <xsl:variable name="arch.footer" select="@footer" />
        <xsl:variable name="arch.target" select="@target" />

        <xsl:if test="$interfaces">
            <div>
            <xsl:element name="h3">
                <xsl:attribute name="id">
                    <xsl:text>def-api-</xsl:text><xsl:value-of select="$module.name"/>
                </xsl:attribute>
                <xsl:value-of select="$module.name"/>
            </xsl:element>            
            <a>
                <xsl:attribute name="href">
                    <xsl:call-template name="filedirapi" >
                        <xsl:with-param name="arch.target" select="$arch.target" />
                    </xsl:call-template>
                    <xsl:text>/overview-summary.html</xsl:text>
                </xsl:attribute>
                <xsl:text>javadoc</xsl:text>
            </a>
            <xsl:if test="$download = 'true'"> | <a>
                    <xsl:attribute name="href">
                        <xsl:call-template name="filedirapi" >
                            <xsl:with-param name="arch.target" select="$arch.target" />
                        </xsl:call-template>
                        <xsl:text>.zip</xsl:text>
                    </xsl:attribute>
                    <xsl:text>download</xsl:text>
                </a>
            </xsl:if>
            | <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="$arch.target" />
                </xsl:attribute>
                <xsl:text>architecture</xsl:text>
            </a> 
            <xsl:if test="//module[@name=$module.name]/arch-usecases" >
                | <a>
                    <xsl:attribute name="href">
                        <xsl:text>usecases.html#usecase-</xsl:text>
                        <xsl:value-of select="$module.name" />
                    </xsl:attribute>
                    <xsl:text>usecases</xsl:text>
                </a>
            </xsl:if>
            <p/>

            <div>
                <xsl:apply-templates select="description"/>
            </div>

            <xsl:if test="deploy-dependencies">
                <div>
                    <p>
                        <b>Usage:</b>
                    </p>
                    <xsl:apply-templates select="deploy-dependencies"/>
                </div>
            </xsl:if>

            <p/>
            <table cellpadding="1" cellspacing="0" border="0" class="tablebg" width="100%">
                <tr>
                    <td>
                        <table border="0" cellpadding="3" cellspacing="1" width="100%">
                            <!--                    <tr><td COLSPAN="5" class="tablecbg" ALIGN="CENTER"><font CLASS="titlectable">Do not duplicate any files</font></td></tr> -->
                            <tr class="tablersh">
                                <td align="CENTER" width="30%">
                                    <span class="titlectable">Interface Name</span>
                                </td>
                                <td align="CENTER" width="15%">
                                    <span class="titlectable">Stability Classification</span>
                                </td>
                                <td align="CENTER" >
                                    <span class="titlectable">Specified in What Document?</span>
                                </td>
                            </tr>

                            <xsl:for-each select="$interfaces">
                                <xsl:if test="@group='java'" >
                                    <xsl:call-template name="api" >
                                        <xsl:with-param name="arch.target" select="$arch.target" />
                                    </xsl:call-template>
                                </xsl:if>
                            </xsl:for-each>

                            <xsl:for-each select="//api[generate-id() = generate-id(key('apiGroups', @group))]">
                                <xsl:variable name="grp" select="@group" />
                                <xsl:if test="$grp!='java'" >
                                    <xsl:variable name="apis" select="/apis" />
                                    <xsl:variable name="module" select="$apis/module[@name=$module.name]" />

                                    <xsl:variable name="allOfTheGroup" select="$module/api[@group=$grp]" />
                                    <xsl:if test="$allOfTheGroup">
                                        <tr class="tabler">
                                            <td>Set of <xsl:value-of select="$grp"/> APIs</td>
                                            <td>Individual</td>
                                            <td>
                                                <a href="{$arch.target}#group-{$grp}">table with definitions</a>
                                            </td>
                                        </tr>
                                    </xsl:if>
                                </xsl:if>
                            </xsl:for-each>

                        </table>
                    </td>
                </tr>
            </table>
            </div>
        </xsl:if>




    </xsl:template>

    <xsl:template name="api">
        <xsl:param name="arch.target" />
        <xsl:variable name="name" select="@name" />
        <xsl:variable name="type" select="@type" />
        <xsl:variable name="category" select="@category" />
        <xsl:variable name="url" select="@url" />
        <xsl:variable name="description" select="node()" />

        <tr class="tabler">
            <td>
                <a>
                    <xsl:attribute name="href" >
                        <xsl:value-of select="$arch.target" />
                        <xsl:text>#java-</xsl:text>
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
                        <xsl:text>http://wiki.netbeans.org/API_Stability#</xsl:text>
                        <xsl:value-of select="$category" />
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="$category='official'">Official</xsl:when>
                        <xsl:when test="$category='stable'">Stable</xsl:when>
                        <xsl:when test="$category='devel'">Devel</xsl:when>
                        <xsl:when test="$category='third'">Third_Party</xsl:when>
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
                <a href="{$url}">
                    <xsl:value-of select="$url"/>
                </a>
                <xsl:if test="$description" >
                    <p>
                        <xsl:apply-templates select="$description" />
                    </p>
                </xsl:if>
            </td>
        </tr>

    </xsl:template>

    <xsl:template match="api-ref">
        <!-- simply bold the name, it link will likely be visible bellow -->
        <b>
            <xsl:value-of select="@name" />
        </b>
    </xsl:template>

    <!-- extracts first part before slash from LoadersAPI/bleble.html -->

    <xsl:template name="filedirapi" >
        <xsl:param name="arch.target" />
    
        <xsl:if test="substring-before($arch.target,'/')">
            <xsl:value-of select="substring-before($arch.target,'/')" />
        </xsl:if>
    </xsl:template>


    <!-- Format random HTML elements as is: -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Gets the first sentence with HTML tags -->
    
    <xsl:template mode="first-sentence" match="api-ref">
        <b>
            <xsl:value-of select="@name" />
        </b>
        <xsl:text> </xsl:text>
    </xsl:template>

    <xsl:template mode="first-sentence" match="node()">
        <xsl:choose>
            <xsl:when test="count(child::*) = 0" >
                <xsl:variable name="first-sentence" select="substring-before(normalize-space(), '. ')" />
                <xsl:variable name="first-dot" select="substring-before(normalize-space(), '.')" />
                <xsl:choose>
                    <xsl:when test="$first-sentence" >
                        <xsl:value-of select="$first-sentence" />
                        <!-- this trick starts comment which disables output produces after 
                        Which means comments out everything after the .
                        -->
                        <xsl:text disable-output-escaping="yes">&lt;!--</xsl:text>
                    </xsl:when>
                    <xsl:when test="$first-dot" >
                        <xsl:value-of select="$first-dot" />
                        <!-- this trick starts comment which disables output produces after 
                        Which means comments out everything after the .
                        -->
                        <xsl:text disable-output-escaping="yes">&lt;!--</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="." />
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:apply-templates mode="first-sentence" select="child::*"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates mode="first-sentence" select="node()"/>
            </xsl:otherwise>
        </xsl:choose>
        
    </xsl:template>
</xsl:stylesheet>