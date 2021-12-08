plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
}
dependencies {
    implementation("com.android.tools.build:gradle:7.0.3")
    implementation("com.android.tools:sdk-common:30.0.3")
}

repositories {
    mavenCentral()
    google()
}

gradlePlugin {
    plugins {
        create("variant-v1-basis") {
            id = "variant-v1-basis"
            implementationClass ="VariantV1BasisPlugin"
        }
        create("variant-v1-advanced") {
            id = "variant-v1-advanced"
            implementationClass ="VariantV1AdvancedPlugin"
        }
        create("variant-v2-basis") {
            id = "variant-v2-basis"
            implementationClass ="VariantV2BasisPlugin"
        }
        create("variant-v2-advanced") {
            id = "variant-v2-advanced"
            implementationClass ="VariantV2AdvancedPlugin"
        }
        create("variant-v2-polyfill") {
            id = "variant-v2-polyfill"
            implementationClass ="VariantV2PolyfillPlugin"
        }
    }
}