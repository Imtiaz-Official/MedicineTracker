package com.example.medicinetracker.util

import com.example.medicinetracker.data.model.GenericInfo
import com.example.medicinetracker.data.model.MedicineBrand
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import android.util.Log

object MedexCrawler {
    private const val BASE_URL = "https://medex.com.bd"
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"

    suspend fun searchBrandsFromMedex(query: String): List<MedicineBrand> {
        return try {
            val searchUrl = "$BASE_URL/search?search=${query.replace(" ", "+")}"
            val searchDoc = Jsoup.connect(searchUrl)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .get()
            
            val results = mutableListOf<MedicineBrand>()
            
            // Targeted selectors for Medex results
            val searchItems = searchDoc.select("div.search-result-row")
            for (item in searchItems) {
                val brandLink = item.select("div.search-result-title a").firstOrNull()
                
                if (brandLink != null) {
                    val fullBrandNameWithForm = brandLink.text().trim()
                    
                    // Regex to extract Brand Name & Strength, and Dosage Form in brackets
                    // Example: "Napa 500 mg (Tablet)"
                    val brandFormRegex = Regex("""(.+?)\s*\((.+?)\)$""")
                    val formMatch = brandFormRegex.find(fullBrandNameWithForm)
                    
                    var brandNameWithStrength = if (formMatch != null) formMatch.groupValues[1].trim() else fullBrandNameWithForm
                    val dosageForm = if (formMatch != null) formMatch.groupValues[2].trim() else ""
                    
                    // Regex to separate Name and Strength
                    // Example: "Napa 500 mg" -> "Napa", "500 mg"
                    val strengthRegex = Regex("""(.+?)\s+(\d+(\.\d+)?\s*(mg|ml|mcg|gm|unit|IU|%))""", RegexOption.IGNORE_CASE)
                    val strengthMatch = strengthRegex.find(brandNameWithStrength)
                    
                    val brandName = if (strengthMatch != null) strengthMatch.groupValues[1].trim() else brandNameWithStrength
                    val strength = if (strengthMatch != null) strengthMatch.groupValues[2].trim() else ""

                    val pTag = item.select("p").firstOrNull()
                    var generic = ""
                    var manufacturer = ""
                    
                    if (pTag != null) {
                        generic = pTag.select("i").text().trim().removeSurrounding("(", ")")
                        val pText = pTag.text()
                        if (pText.contains("is manufactured by ")) {
                            manufacturer = pText.substringAfter("is manufactured by ").trim()
                        }
                    }

                    if (brandName.isNotEmpty()) {
                        results.add(MedicineBrand(
                            id = System.currentTimeMillis() + results.size,
                            name = brandName,
                            generic = generic,
                            strength = strength,
                            manufacturer = manufacturer,
                            dosageForm = dosageForm,
                            genericId = null
                        ))
                    }
                }
            }
            results
        } catch (e: Exception) {
            Log.e("MedexCrawler", "Error searching Medex for $query", e)
            emptyList()
        }
    }

    suspend fun fetchMonographFromMedex(brandName: String, genericName: String): GenericInfo? {
        return try {
            // 1. Search for the brand
            val searchUrl = "$BASE_URL/search?search=${brandName.replace(" ", "+")}"
            val searchDoc = Jsoup.connect(searchUrl)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .get()
            
            // 2. Find the first brand link in search-result-row
            val brandLink = searchDoc.select("div.search-result-row div.search-result-title a").firstOrNull()
            
            val brandUrl = if (brandLink != null) {
                val href = brandLink.attr("href")
                if (href.startsWith("http")) href else BASE_URL + href
            } else null

            if (brandUrl == null) return null

            // 3. Load the brand page
            val brandDoc = Jsoup.connect(brandUrl)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .get()
            
            // 4. Find the generic link
            val genericLink = brandDoc.select("a[href*=\"/generics/\"]").firstOrNull()
            
            val genericUrl = if (genericLink != null) {
                val href = genericLink.attr("href")
                if (href.startsWith("http")) href else BASE_URL + href
            } else null

            if (genericUrl == null) return null

            // 5. Load the generic page
            val genericDoc = Jsoup.connect(genericUrl)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .get()
            
            return parseGenericMonograph(genericDoc, genericName)
        } catch (e: Exception) {
            Log.e("MedexCrawler", "Error crawling Medex for $brandName", e)
            null
        }
    }

    suspend fun fetchAlternateBrandsFromMedex(brandName: String): List<MedicineBrand> {
        return try {
            // 1. Search for the brand to get its page
            val searchUrl = "$BASE_URL/search?search=${brandName.replace(" ", "+")}"
            val searchDoc = Jsoup.connect(searchUrl)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .get()
            
            val brandLink = searchDoc.select("div.search-result-row div.search-result-title a").firstOrNull()
            val brandUrl = if (brandLink != null) {
                val href = brandLink.attr("href")
                if (href.startsWith("http")) href else BASE_URL + href
            } else null

            if (brandUrl == null) return emptyList()

            // 2. Load brand page
            val brandDoc = Jsoup.connect(brandUrl)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .get()
            
            // 3. Find the "Other Brands" or "Available Brands" link/section
            // Usually Medex has a link like "Other Brands" on the generic page
            val genericLink = brandDoc.select("a[href*=\"/generics/\"]").firstOrNull()
            val genericUrl = if (genericLink != null) {
                val href = genericLink.attr("href")
                if (href.startsWith("http")) href else BASE_URL + href
            } else null

            if (genericUrl == null) return emptyList()

            // 4. Load the generic page which contains the list of ALL brands for that generic
            val genericDoc = Jsoup.connect(genericUrl)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .get()
            
            val results = mutableListOf<MedicineBrand>()
            
            // Medex generic pages list brands in rows
            val brandItems = genericDoc.select("div.hoverable-block, div.search-result-row")
            for (item in brandItems) {
                val nameElement = item.select("div.brand-name, div.search-result-title a").firstOrNull()
                val name = nameElement?.text()?.trim() ?: ""
                
                // Skip the brand we are currently looking at
                if (name.contains(brandName, ignoreCase = true)) continue

                if (name.isNotEmpty()) {
                    val manufacturer = item.select("div.company-name, p").firstOrNull()?.text()?.replace(Regex(".*manufactured by ", RegexOption.IGNORE_CASE), "")?.trim() ?: ""
                    
                    results.add(MedicineBrand(
                        id = System.currentTimeMillis() + results.size,
                        name = name,
                        generic = "", // We already know the generic from the page context
                        strength = item.select("div.strength").text().trim(),
                        manufacturer = manufacturer,
                        dosageForm = item.select("div.dosage-form").text().trim(),
                        genericId = null
                    ))
                }
            }
            results.distinctBy { it.name }.take(15)
        } catch (e: Exception) {
            Log.e("MedexCrawler", "Error fetching alternates for $brandName", e)
            emptyList()
        }
    }

    private fun parseGenericMonograph(doc: Document, genericName: String): GenericInfo {
        fun getSection(title: String): String? {
            // Medex now uses h3 titles with class ac-header
            val header = doc.select("h3.ac-header:contains($title)").firstOrNull()
            // The content is usually in the next sibling of the header's parent div
            val body = header?.parent()?.nextElementSibling()?.takeIf { it.hasClass("ac-body") }
                ?: header?.nextElementSibling()?.takeIf { it.hasClass("ac-body") }
            return body?.html()
        }

        return GenericInfo(
            id = 0,
            name = genericName,
            indication = getSection("Indications"),
            therapeuticClass = getSection("Therapeutic Class"),
            pharmacology = getSection("Pharmacology"),
            dosage = getSection("Dosage"),
            administration = getSection("Administration"),
            interaction = getSection("Interaction"),
            contraindications = getSection("Contraindications"),
            sideEffects = getSection("Side Effects"),
            pregnancyLactation = getSection("Pregnancy & Lactation"),
            precautions = getSection("Precautions"),
            storage = getSection("Storage Conditions")
        )
    }
}
