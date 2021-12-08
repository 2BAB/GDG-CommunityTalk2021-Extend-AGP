import com.android.build.api.variant.*
import com.android.build.api.artifact.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.*

class VariantV2BasisPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val androidExtension = project.extensions
            .findByType(ApplicationAndroidComponentsExtension::class.java)!!

        androidExtension.finalizeDsl { appExt ->
            appExt.buildFeatures.dataBinding = false
            appExt.buildFeatures.viewBinding = true
        }

        androidExtension.beforeVariants { variantBuilder ->
            variantBuilder.minSdk = 30
        }

        androidExtension.beforeVariants(
            androidExtension.selector()
                .withName("productionDebug")
        ) { variantBuilder ->
            variantBuilder.enabled = false
        }

        androidExtension.onVariants { variant ->
            // Configurations
            // Reflect the DSL models.
            val mainOutput: VariantOutput = variant.outputs.single {
                it.outputType == VariantOutputConfiguration.OutputType.SINGLE
            }
            project.logger.lifecycle("variant name: ${variant.name}")
            project.logger.lifecycle("variant.applicationId: ${variant.namespace.get()}")
            project.logger.lifecycle("variant.versionCode: ${mainOutput.versionCode.get()}")
            project.logger.lifecycle("variant.productFlavors: ${variant.productFlavors.size}")


            // Task Providers are removed from new variant APIs.
        }


        // Advanced Variant APIs, to update merged flavors config
        androidExtension.onVariants(
            androidExtension
                .selector()
                .withBuildType("release")
                .withFlavor(Pair("server", "production"))
        ) { variant ->
            val mainOutput: VariantOutput = variant.outputs.single {
                it.outputType == VariantOutputConfiguration.OutputType.SINGLE
            }
            mainOutput.versionName.set("1.1.0")
            variant.androidResources.aaptAdditionalParameters.add("-v")
//             variant.signingConfig?.setConfig(...)
        }

    }

}