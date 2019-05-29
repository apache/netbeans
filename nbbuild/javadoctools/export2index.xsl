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
    <xsl:template match="/apis" >
        <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
        <head>
            <title>NetBeans API Index</title>
            <link type="application/atom+xml" rel="alternate" href="apichanges.atom"/>
        </head>
        <frameset cols="20%,80%" title="" onLoad="top.loadFrames()">
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
        </frameset>
        
    </xsl:template>
    
</xsl:stylesheet>