package com.example.data

import org.json.JSONArray
import org.json.JSONObject

enum class WidgetType {
    EXTENSION_HTML,
    EXTENSION_ACTIONS,
    MEDIA,
    CUSTOM
}

enum class WidgetLayout {
    HORIZONTAL,
    VERTICAL,
    COMPACT_GRID
}

data class WidgetMiniAction(
    val id: String,
    val name: String = "",
    val icon: String = "extension",
    val type: String = "EXTENSION",
    val value: String = "",
    val badge: String = ""
)

data class WidgetConfig(
    val widgetType: WidgetType = WidgetType.EXTENSION_ACTIONS,
    val layout: WidgetLayout = WidgetLayout.HORIZONTAL,
    val showValueDisplay: Boolean = false,
    val valueDisplayLabel: String = "",
    val statusText: String = "",
    val actions: List<WidgetMiniAction> = emptyList()
) {
    fun toJson(): String {
        val obj = JSONObject()
        obj.put("widgetType", widgetType.name)
        obj.put("layout", layout.name)
        obj.put("showValueDisplay", showValueDisplay)
        obj.put("valueDisplayLabel", valueDisplayLabel)
        obj.put("statusText", statusText)

        val actionsArray = JSONArray()
        actions.forEach { act ->
            actionsArray.put(JSONObject().apply {
                put("id", act.id)
                put("name", act.name)
                put("icon", act.icon)
                put("type", act.type)
                put("value", act.value)
                put("badge", act.badge)
            })
        }
        obj.put("actions", actionsArray)
        return obj.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): WidgetConfig {
            if (jsonStr.isBlank() || jsonStr == "[]" || jsonStr == "{}") return WidgetConfig()
            return try {
                val obj = JSONObject(jsonStr)
                val type = try {
                    WidgetType.valueOf(obj.optString("widgetType", WidgetType.EXTENSION_ACTIONS.name))
                } catch (_: Exception) {
                    WidgetType.EXTENSION_ACTIONS
                }
                val layout = try {
                    WidgetLayout.valueOf(obj.optString("layout", WidgetLayout.HORIZONTAL.name))
                } catch (_: Exception) {
                    WidgetLayout.HORIZONTAL
                }

                val actionsList = mutableListOf<WidgetMiniAction>()
                obj.optJSONArray("actions")?.let { array ->
                    for (i in 0 until array.length()) {
                        val actObj = array.optJSONObject(i) ?: continue
                        actionsList.add(
                            WidgetMiniAction(
                                id = actObj.optString("id", ""),
                                name = actObj.optString("name", ""),
                                icon = actObj.optString("icon", "extension"),
                                type = actObj.optString("type", "EXTENSION"),
                                value = actObj.optString("value", ""),
                                badge = actObj.optString("badge", "")
                            )
                        )
                    }
                }

                WidgetConfig(
                    widgetType = type,
                    layout = layout,
                    showValueDisplay = obj.optBoolean("showValueDisplay", false),
                    valueDisplayLabel = obj.optString("valueDisplayLabel", ""),
                    statusText = obj.optString("statusText", ""),
                    actions = actionsList
                )
            } catch (_: Exception) {
                WidgetConfig()
            }
        }
    }
}
