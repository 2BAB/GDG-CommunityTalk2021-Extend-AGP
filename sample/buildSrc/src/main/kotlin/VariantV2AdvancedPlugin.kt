import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.*
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.getByType
import java.io.File

class VariantV2AdvancedPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val androidExtension =
            project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

        androidExtension.onVariants { variant ->
            val mainOutput: VariantOutput = variant.outputs.single {
                it.outputType == VariantOutputConfiguration.OutputType.SINGLE
            }
            val variantCapitalizedName = variant.name.capitalize()

            // Example 1
            val renameApkTask = project.tasks.register(
                "rename${variantCapitalizedName}Apk",
                RenameApkFile::class.java
            ) {
                val apkFolderProvider = variant.artifacts.get(SingleArtifact.APK)
                this.outApk.set(
                    File(apkFolderProvider.get().asFile, "custom-${mainOutput.versionName}")
                )
                this.apkFolder.set(apkFolderProvider)
                this.builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
            }

            // Example 2
            val postUpdateTask = project.tasks.register(
                "postUpdate${variantCapitalizedName}Manifest",
                ManifestAfterMergeTask::class.java
            )
            variant.artifacts
                .use(postUpdateTask)
                .wiredWithFiles(
                    ManifestAfterMergeTask::mergedManifest,
                    ManifestAfterMergeTask::updatedManifest
                )
                .toTransform(SingleArtifact.MERGED_MANIFEST)


            // Example 3
            val notificationTaskProvider: TaskProvider<NotificationTask>
                = project.tasks.register(
                "notify${variantCapitalizedName}Build",
                NotificationTask::class.java
            ) {
                title.set("${project.name} apk is built successfully.")
                releaseNote.set(renameApkTask.map {
                    val size = it.outApk.get().asFile.length() / 1024.0 / 1024.0
                    "Apk - $size MB"
                })
            }
        }
    }

    abstract class ManifestAfterMergeTask : DefaultTask() {

        @get:InputFile
        abstract val mergedManifest: RegularFileProperty

        @get:OutputFile
        abstract val updatedManifest: RegularFileProperty

        @TaskAction
        fun afterMerge() {
            val modifiedManifest = mergedManifest.get().asFile.readText()
                .replace("allowBackup=\"true\"", "allowBackup=\"false\"")
            updatedManifest.get().asFile.writeText(modifiedManifest)
        }

    }

    abstract class RenameApkFile : DefaultTask() {

        @get:InputFiles
        abstract val apkFolder: DirectoryProperty

        @get:Internal
        abstract val builtArtifactsLoader: Property<BuiltArtifactsLoader>

        @get:OutputFile
        abstract val outApk: RegularFileProperty

        @TaskAction
        fun taskAction() {
            val builtArtifacts = builtArtifactsLoader.get().load(apkFolder.get())
                ?: throw RuntimeException("Cannot load APKs")
            File(builtArtifacts.elements.single().outputFile)
                .copyTo(outApk.get().asFile)
        }
    }

    abstract class NotificationTask : DefaultTask() {

        @get:Input
        abstract val title: Property<String>

        @get:Input
        abstract val releaseNote: Property<String>

        @TaskAction
        fun taskAction() {
            val message = "${title.get()}\n${releaseNote.get()}"
            val channel = "123456789"
            NotificationClient().send(message, channel)
        }

    }


    class NotificationClient {
        fun send(message: String, channel: String) {
            println("NotificationTask: sending \"$message\" to channel $channel")
        }
    }
}