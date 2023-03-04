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
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>
    <xsl:template match="/">
        <html>
            <style>
                body { background:#ffffee; }
                table { font-size:10.0pt; }
                h2 { font-size:11.5pt; color:#EE6D06; }
                th { background:#aaaadd; font-weight:bold; padding:0pt 10pt 0pt 10pt; }
                td { padding:0pt 10pt 0pt 10pt; }
                tr.start { background:#b5fdb7; }
                tr.start_measured { background:#b5fdb7; font-weight:bold; }
                tr.paint { background:#ffd0d0; }
                tr.paint_measured { background:#ffb4b4; font-weight:bold; }
                tr.user_action { background:#b5ffb0; }
                tr.user_action_measured { background:#9cff95; font-weight:bold; }
                tr.app_message { background:#eeeedd; }
                tr.app_message_ignored { background:#eeeedd;color:#909090; }
                tr.app_message_measured { background:#fffd6d; font-weight:bold; }
                tr.config_message { background:#ffffcc; }
                tr.trace_message { background:#d9d9ff; }
                tr.unknown {}
            </style>
            <head><title>Actions/Events list</title></head>
            <body><xsl:apply-templates select="action-tracking"/></body>
        </html>
    </xsl:template>
    
    <xsl:template match="action-tracking">
        <xsl:apply-templates select="event-list"/>
    </xsl:template>
    
    <xsl:template match="event-list">
        <h2><xsl:value-of select="@name"/></h2>
        <table border="1" cellspacing="1" cellpadding="0" width="100%">
            <tr>
                <th width="10%" align="right">Time since start</th>
                <th width="60%" align="left">Name</th>
                <th width="10%" align="left">Type</th>
                <th width="10%" align="center">Time</th>
            </tr>
            <xsl:apply-templates select="event"/>
        </table>
    </xsl:template>

    <xsl:template match="event">
        <xsl:variable name="cName">
            <xsl:value-of select="@type"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test='@measured'>
                <tr class='{$cName}_measured'>
                    <td align="right"><xsl:value-of select="@diff"/></td>
                    <td align="left"><xsl:value-of select="@name"/></td>
                    <td align="left"><xsl:value-of select="@type"/></td>
                    <td align="center"><xsl:value-of select="@time"/></td>
                </tr>
            </xsl:when>
            <xsl:when test='contains(@name,"IGNORED")'>
                <tr class='{$cName}_ignored'>
                    <td align="right"><xsl:value-of select="@diff"/></td>
                    <td align="left"><xsl:value-of select="@name"/></td>
                    <td align="left"><xsl:value-of select="@type"/></td>
                    <td align="center"><xsl:value-of select="@time"/></td>
                </tr>
            </xsl:when>
            <xsl:otherwise>
                <tr class='{$cName}'>
                    <td align="right"><xsl:value-of select="@diff"/></td>
                    <td align="left"><xsl:value-of select="@name"/></td>
                    <td align="left"><xsl:value-of select="@type"/></td>
                    <td align="center"><xsl:value-of select="@time"/></td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
