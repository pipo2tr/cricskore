package com.pipo2tr.cricskore.app.utils

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory


data class Item(var team1: Pair<String, String>, var team2: Pair<String, String>, var id: String)


fun liveScoreParser(id: String, xmlData: String): List<Item> {
    val inputStream = xmlData.byteInputStream()
    val factory = XmlPullParserFactory.newInstance()
    factory.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
    val parser = factory.newPullParser()
    parser.setInput(inputStream, null)
    var eventType = parser.eventType
    var currentItem: Item? = null
    var itemsList = mutableListOf<Item>()
    var inItem = false
    while (eventType != XmlPullParser.END_DOCUMENT) {
        val tagName = parser.name
        when (eventType) {
            XmlPullParser.START_TAG -> {
                if (!tagName.isNullOrEmpty() && tagName.equals("item")) {
                    inItem = true
                    currentItem = Item(Pair("", ""), Pair("", ""), "")
                }
            }

            XmlPullParser.TEXT -> {
                if (inItem && parser.text.isNotBlank()) {
                    if (parser.text.contains("http")) {
                        currentItem?.id = extractMatchIdFromUrl(parser.text)
                    } else {
                        val teamsArr = parser.text.split(" v ").sortedBy {
                            !it.contains("*")
                        }
                        currentItem?.team1 = separateTeamAndScore(teamsArr[0])
                        currentItem?.team2 = separateTeamAndScore(teamsArr[1])
                    }
                }
            }

            XmlPullParser.END_TAG -> {
                if (!tagName.isNullOrEmpty() && tagName.equals("item")) {
                    inItem = false
                    currentItem?.let { itemsList.add(it) }
                }
            }
        }
        eventType = parser.next()
    }

    itemsList = itemsList.sortedBy {
        !topICCNations.contains(it.team1.first)
    } as MutableList<Item>

    if (id.isNotEmpty()) {
        return itemsList.sortedBy {
            it.id != id
        }
    }
    return itemsList
}

fun separateTeamAndScore(input: String): Pair<String, String> {
    if (!input.any { it.isDigit() }) {
        return Pair(formatTeamName(input), "")
    }

    val regexPattern = "^(\\D+)\\s+(.+)\$"
    val regex = Regex(regexPattern)
    val matchResult = regex.find(input)

    if (matchResult != null && matchResult.groupValues.size == 3) {
        val teamName = formatTeamName(matchResult.groupValues[1].trim())
        var score = matchResult.groupValues[2].trim().replace(" *", "")
        if (!score.contains("/")) {
            score += "/0"
        }
        return Pair(teamName, score)
    }

    return Pair(input.trim(), "")
}

fun formatTeamName(name: String): String {
    return name.replace("Women", "W").replace("Men", "M")
}

fun extractMatchIdFromUrl(url: String): String {
    val regex = Regex("""/(\d+)\.html(?:\?.*)?$""")
    val matchResult = regex.find(url)
    return matchResult?.groupValues?.getOrNull(1).orEmpty()
}

val topICCNations = arrayOf(
    "England",
    "India",
    "New Zealand",
    "Australia",
    "South Africa",
    "Pakistan",
    "Bangladesh",
    "West Indies",
    "Sri Lanka",
    "Afghanistan",
    "Ireland",
    "Zimbabwe"
)