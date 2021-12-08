import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.tasks.ProcessApplicationManifest
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import java.io.File

class VariantV1AdvancedPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val android = project.extensions.getByType(AppExtension::class.java)
        android.applicationVariants.configureEach {
            val variant: ApplicationVariant = this
            val variantCapitalizedName = variant.name.capitalize()
            variant.javaCompileProvider
            // Example 1
            variant.outputs.forEach { output ->
                val file = output.outputFile
                println("output: " + output.outputType)
                println("output: " + file.name)
                if (file.extension == "apk") {
                    // 1. Below api is not valid any more.
                    // output.outputFileName = "custom-" + file.name
                    // 2. Use below code for apk rename in legacy(v1) variant api
                    val out = File(file.parentFile, "custom-${variant.versionName}")
                    val renameApkTask = project.tasks.register(
                        "rename${variantCapitalizedName}Apk",
                        RenameApkFile::class.java
                    ) {
                        inputApk = file
                        outputApk = out
                        dependsOn(variant.packageApplicationProvider)
                    }

                    // Example 3
                    val releaseNoteFile = File(file.parentFile, "release-note.txt")
                    val apkSizeObtainTask = project.tasks.register(
                        "apkSizeObtain$variantCapitalizedName",
                        ApkSizeObtainTask::class.java
                    ) {
                        apk = file
                        releaseNote = releaseNoteFile
                        dependsOn(renameApkTask)
                    }
                    val notificationTask = project.tasks.register(
                        "notify${variantCapitalizedName}Build",
                        NotificationTask::class.java
                    ) {
                        title = "${project.name} apk is built successfully."
                        releaseNote = releaseNoteFile
                        dependsOn(apkSizeObtainTask)
                    }
                    assembleProvider.configure { dependsOn(notificationTask) }
                }
            }


            // Example 2
            // output.processManifestProvider can provide same result
            val processManifestTask = project.tasks
                .withType(ProcessApplicationManifest::class.java).first {
                    it.name.contains(variant.name, true)
                }
            val postUpdateManifestTask = project.tasks
                .register(
                    "postUpdate${variantCapitalizedName}Manifest",
                    ManifestAfterMergeTask::class.java
                ) {
                    mergedManifest = processManifestTask.mergedManifest
                        .get()
                        .asFile
                }
            processManifestTask.finalizedBy(postUpdateManifestTask) // Abuse of finalizedBy()


        }
    }

    abstract class RenameApkFile : DefaultTask() {

        @get:InputFile
        lateinit var inputApk: File

        @get:OutputFile
        lateinit var outputApk: File

        @TaskAction
        fun taskAction() {
            inputApk.copyTo(outputApk)
        }
    }


    abstract class ManifestAfterMergeTask : DefaultTask() {

        @get:InputFile
        lateinit var mergedManifest: File

        @TaskAction
        fun afterMerge() {
            val modifiedManifest = mergedManifest.readText()
                .replace("allowBackup=\"true\"", "allowBackup=\"false\"")
            mergedManifest.writeText(modifiedManifest)
        }

    }


    abstract class ApkSizeObtainTask : DefaultTask() {

        @get:InputFile
        lateinit var apk: File

        @get:OutputFile
        lateinit var releaseNote: File

        @TaskAction
        fun taskAction() {
            val size = apk.length() / 1024.0 / 1024.0
            releaseNote.writeText("Apk - $size MB")
        }

    }

    // Let's assume NotificationTask is provided by a 3rd party SDK,
    // it's not open-sourced library and hard to change its implementation.
    abstract class NotificationTask : DefaultTask() {

        @get:Input
        lateinit var title: String

        @get:InputFile
        lateinit var releaseNote: File

        @TaskAction
        fun taskAction() {
            val msg = "$title\n${releaseNote.readText()}"
            val channel = "123456789"
            NotificationClient().send(msg, channel)
        }

    }

    class NotificationClient {
        fun send(message: String, channel: String) {
            println("NotificationTask: sending \"$message\" to channel $channel")
        }
    }
}


