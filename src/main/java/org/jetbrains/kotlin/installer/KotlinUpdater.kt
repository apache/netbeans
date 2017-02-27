/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.installer

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import org.jetbrains.kotlin.log.KotlinLogger
import java.io.InputStream
import java.io.BufferedInputStream
import java.nio.charset.Charset
import org.dom4j.io.SAXReader
import java.io.StringReader
import org.openide.NotifyDescriptor
import org.openide.DialogDisplayer
import java.net.URLEncoder
import com.intellij.openapi.util.SystemInfo
import java.util.Random
import org.openide.modules.Modules
import org.openide.util.NbPreferences

object KotlinUpdater {

    val KOTLIN_PLUGIN_VERSION = "0.1.2-beta"
    val USER_ID = "kotlin.userId"
    val pluginSite = "https://plugins.jetbrains.com/netbeans-plugins/kotlin/update?"

    var updated = true
        private set

    @Synchronized fun checkUpdates() {
        if (updated) return

        val params = getRequestParams()
        val url = URL("$pluginSite$params")

        val connection = url.openConnection()
        if (connection is HttpURLConnection) {
            try {
                connection.setInstanceFollowRedirects(false)
                connection.connect()
                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    val redirect = connection.getHeaderField("Location")
                    handleRedirect(redirect)
                } else {
                    KotlinLogger.INSTANCE.logInfo("Response code: $responseCode")
                }

            } catch (e: IOException) {
                KotlinLogger.INSTANCE.logInfo("Couldn't connect to $pluginSite")
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun handleRedirect(redirect: String) {
        KotlinLogger.INSTANCE.logInfo("Redirect: $redirect")
        val url = URL("${redirect}/version.xml")
        val connection = url.openConnection()

        if (connection is HttpURLConnection) {
            try {
                connection.connect()
                val responseCode = connection.responseCode

                if (responseCode == 200) {
                    updated = true

                    val latestVersion = getLatestVersion(connection.inputStream)
                    KotlinLogger.INSTANCE.logInfo("Latest version: $latestVersion. Installed version: $KOTLIN_PLUGIN_VERSION")

                    if (latestVersion != KOTLIN_PLUGIN_VERSION) {
                        showNotification(latestVersion)
                    }
                }
            } catch (e: IOException) {
                KotlinLogger.INSTANCE.logInfo("Couldn't connect to $redirect")
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun showNotification(latestVersion: String) {
        val message = "A new version ($latestVersion) of the Kotlin plugin is available"
        val notifyDescriptor = NotifyDescriptor.Message(message,
                NotifyDescriptor.INFORMATION_MESSAGE)
        DialogDisplayer.getDefault().notifyLater(notifyDescriptor)
    }

    private fun getLatestVersion(inputStream: InputStream): String {
        val text = BufferedInputStream(inputStream).bufferedReader(Charset.defaultCharset()).readText()
        val document = SAXReader().read(StringReader(text))

        return document.rootElement.text
    }

    private fun getRequestParams(): String {
        val os = URLEncoder.encode("${SystemInfo.OS_NAME} ${SystemInfo.OS_VERSION}", "UTF-8")
        val userId = getUserID()
        val netbeansVersion = getNetBeansVersion()

        return "build=$netbeansVersion&pluginVersion=$KOTLIN_PLUGIN_VERSION&os=$os&uuid=$userId"
    }

    private fun getUserID(): Long {
        val userId = NbPreferences.root().getLong(USER_ID, 0L)
        if (userId == 0L) {
            val generated = Math.abs(Random().nextLong())
            NbPreferences.root().putLong(USER_ID, generated)
            return generated
        }

        return userId
    }

    private fun getNetBeansVersion(): String {
        val info = Modules.getDefault().ownerOf(Modules::class.java)

        return info?.buildVersion ?: "-1"
    }

}