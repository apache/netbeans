<?xml version="1.0" encoding="UTF-8" ?>
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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    <xsl:template match="/" >
        <xsl:choose>
            <xsl:when test="descendant::api[@category='stable' and @group='java' and @type='export']"><![CDATA[
stability.color=#ffffff
stability.title=Stable
stability.definition.url=https://netbeans.apache.org/wiki/API_Stability#stable
]]></xsl:when>
            <xsl:when test="descendant::api[@category='official' and @group='java' and @type='export']"><![CDATA[
stability.color=#ffffff
stability.title=Official
stability.definition.url=https://netbeans.apache.org/wiki/API_Stability#official
]]></xsl:when>
            <xsl:when test="descendant::api[@category='devel' and @group='java' and @type='export']"><![CDATA[
stability.color=#ddcc80
stability.image=resources/stability-devel.png
stability.title=Under Development
stability.definition.url=https://netbeans.apache.org/wiki/API_Stability#devel
]]></xsl:when>
            <xsl:when test="descendant::api[@category='deprecated' and @group='java' and @type='export']"><![CDATA[
stability.color=#afafaf
stability.image=resources/stability-deprecated.png
stability.title=Deprecated
stability.definition.url=https://netbeans.apache.org/wiki/API_Stability#deprecated
]]></xsl:when>
            <xsl:otherwise><![CDATA[
stability.color=#e0a0a0
stability.image=resources/stability-friend.png
stability.title=Friend, Private or Third Party
stability.definition.url=https://netbeans.apache.org/wiki/API_Stability#friend
]]></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
