package com.nickcoblentz.montoya

import burp.api.montoya.MontoyaApi
import burp.api.montoya.BurpExtension
import burp.api.montoya.http.HttpService
import burp.api.montoya.http.message.requests.HttpRequest
import com.nickcoblentz.montoya.settings.*
import java.io.File;
import com.nickcoblentz.montoya.ui.UIHelper

class ImportRequestsToRepeater : BurpExtension {
    private lateinit var Logger: MontoyaLogger
    private val PluginName: String = "Import Requests to Repeater"
    private lateinit var Api: MontoyaApi


    override fun initialize(api: MontoyaApi?) {
        if (api == null) {
            return
        }

        Api = api
        Logger = MontoyaLogger(api, LogLevel.DEBUG)

        Logger.debugLog( "Plugin Starting...")

        api.extension().setName(PluginName)
        val uihelper = UIHelper()
        val requestTemplateSetting = ListStringExtensionSetting(
            api,
            "Request Template",
            "importrequeststorepeater.requesttemplate",
            mutableListOf("GET / HTTP/1.1"),
            ExtensionSettingSaveLocation.PROJECT
        )
        val useUTF8CharsetSetting = BooleanExtensionSetting(
            api,
            "Use UTF-8 (if not will use ISO_8859_1)?",
            "importrequeststorepeater.charset",
            true,
            ExtensionSettingSaveLocation.PROJECT
        )

        val lastImportedFiles = ListStringExtensionSetting(
            api,
            "Files Paths to Import",
            "importrequeststorepeater.importrequeststorepeater",
            mutableListOf("c:\\example.txt","c:\\example2.txt"),
            ExtensionSettingSaveLocation.PROJECT
        )

        val formGenerator = GenericExtensionSettingsFormGenerator(listOf(requestTemplateSetting,useUTF8CharsetSetting,lastImportedFiles),PluginName)
        val settingsFormBuilder = formGenerator.getSettingsFormBuilder()
        formGenerator.addSaveCallback { formElement, form ->
            Logger.debugLog("Starting save callback")
            val progressDialog = uihelper.uibooster.showProgressDialog("Loading Files in Repeater","Import Progress",0,lastImportedFiles.currentValue.size)
            var finished = 0
            lastImportedFiles.currentValue.forEachIndexed({ index, filePath ->
                Logger.debugLog("filePath: $filePath")
                Thread.startVirtualThread(
                    Runnable {
                        val f = File(filePath)
                        if (f.exists()) {
                            Logger.debugLog("file exists")
                            val tabname = f.nameWithoutExtension
                            val fileContent = if (useUTF8CharsetSetting.currentValue) f.readText(Charsets.UTF_8) else f.readText(Charsets.ISO_8859_1)
                            //Logger.debugLog("contents: $fileContent")
                            var httpRequest =
                                HttpRequest.httpRequest("${requestTemplateSetting.currentValue.joinToString("\r\n").trim()}\r\n\r\n$fileContent")
                            if(httpRequest.hasHeader("Host"))
                                httpRequest = httpRequest.withService(HttpService.httpService(httpRequest.headerValue("Host"),true))
                            api.repeater().sendToRepeater(httpRequest, f.nameWithoutExtension)
                        }
                        else
                            Logger.errorLog("couldn't find file: $filePath")
                        finished++
                        progressDialog.setProgress(finished)
                    })

            })
            progressDialog.setMessage("Done!")
            progressDialog.close()
            Logger.debugLog("End save callback")

        }

        val settingsForm = settingsFormBuilder.run()
        api.userInterface().registerContextMenuItemsProvider(ExtensionSettingsContextMenuProvider(api, settingsForm))

        Logger.debugLog("Finished")

    }


}