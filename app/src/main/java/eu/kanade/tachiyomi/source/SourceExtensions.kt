package eu.kanade.tachiyomi.source

import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.tachiyomi.extension.ExtensionManager
import tachiyomi.domain.source.model.StubSource
import tachiyomi.source.local.isLocal
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

fun Source.getNameForMangaInfo(
    // SY -->
    mergeSources: List<Source>?,
    enabledLanguages: List<String> = Injekt.get<SourcePreferences>().enabledLanguages().get()
        .filterNot { it in listOf("all", "other") },
    // SY <--
): String {
    val hasOneActiveLanguages = enabledLanguages.size == 1
    val isInEnabledLanguages = lang in enabledLanguages
    return when {
        // SY -->
        !mergeSources.isNullOrEmpty() -> getMergedSourcesString(
            mergeSources,
            enabledLanguages,
            hasOneActiveLanguages,
        )
        // SY <--
        // For edge cases where user disables a source they got manga of in their library.
        hasOneActiveLanguages && !isInEnabledLanguages -> toString()
        // Hide the language tag when only one language is used.
        hasOneActiveLanguages && isInEnabledLanguages -> name
        else -> toString()
    }
}

// SY -->
private fun getMergedSourcesString(
    mergeSources: List<Source>,
    enabledLangs: List<String>,
    onlyName: Boolean,
): String {
    return if (onlyName) {
        mergeSources.joinToString { source ->
            if (source.lang !in enabledLangs) {
                source.toString()
            } else {
                source.name
            }
        }
    } else {
        mergeSources.joinToString()
    }
}
// SY <--

fun Source.isLocalOrStub(): Boolean = isLocal() || this is StubSource

fun Source?.isNsfw(): Boolean {
    if (this == null || this.isLocalOrStub()) return false
    val sourceUsed = Injekt.get<ExtensionManager>().installedExtensionsFlow.value
        .find { ext -> ext.sources.any { it.id == this.id } }!!
    return sourceUsed.isNsfw
}

fun Source.isIncognitoModeEnabled(): Boolean {
    val extensionPackage = Injekt.get<ExtensionManager>().getExtensionPackage(id)
    return extensionPackage in Injekt.get<SourcePreferences>().incognitoExtensions().get()
}
