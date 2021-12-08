import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.core.MergedFlavor
import java.io.File

class VariantV1BasisPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val android = project.extensions.findByType(AppExtension::class.java)!!
        android.applicationVariants.configureEach {
            val variant: ApplicationVariant = this
            val variantCapitalizedName = variant.name.capitalize()

            // Configurations
            // Reflect the DSL models
            project.logger.lifecycle("variant name: ${variant.name}")
            project.logger.lifecycle("variant.applicationId: ${variant.applicationId}")
            project.logger.lifecycle("variant.versionCode: ${variant.versionCode}")
            project.logger.lifecycle("variant.mergedFlavor: ${variant.mergedFlavor.name}")

            // Task Providers
            val beforeAssemble = project.tasks.register(
                "before${variantCapitalizedName}Assemble"
            ) {
                doFirst { project.logger.lifecycle("${this.name} is running...") }
            }
            variant.assembleProvider.configure {
                dependsOn(beforeAssemble)
            }


            // Advanced Variant APIs, to update merged flavors config
            if (variant.name.contains("release", true)
                && variant.name.contains("production", true)
            ) {
                // (variant.mergedFlavor as MergedFlavor).setSigningConfig(...)
            }

        }
    }


}