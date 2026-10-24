package com.file.material.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.file.material.data.model.SortBy
import com.file.material.data.model.SortOrder
import com.file.material.data.model.ViewMode

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var lastDirectoryPath: String?
        get() = prefs.getString(KEY_LAST_DIR, null)
        set(value) = prefs.edit().putString(KEY_LAST_DIR, value).apply()

    var dynamicColor: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        set(value) = prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, value).apply()

    var darkTheme: Boolean?
        get() = when (prefs.getString(KEY_DARK_THEME, "SYSTEM")) {
            "DARK" -> true
            "LIGHT" -> false
            else -> null
        }
        set(value) {
            val str = when (value) {
                true -> "DARK"
                false -> "LIGHT"
                null -> "SYSTEM"
            }
            prefs.edit().putString(KEY_DARK_THEME, str).apply()
        }

    var amoledDark: Boolean
        get() = prefs.getBoolean(KEY_AMOLED_DARK, false)
        set(value) = prefs.edit().putBoolean(KEY_AMOLED_DARK, value).apply()

    var compactView: Boolean
        get() = prefs.getBoolean(KEY_COMPACT_VIEW, false)
        set(value) = prefs.edit().putBoolean(KEY_COMPACT_VIEW, value).apply()

    var showFileExtensions: Boolean
        get() = prefs.getBoolean(KEY_SHOW_EXTENSIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_EXTENSIONS, value).apply()

    var confirmDelete: Boolean
        get() = prefs.getBoolean(KEY_CONFIRM_DELETE, true)
        set(value) = prefs.edit().putBoolean(KEY_CONFIRM_DELETE, value).apply()

    var showHidden: Boolean
        get() = prefs.getBoolean(KEY_SHOW_HIDDEN, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_HIDDEN, value).apply()

    var showFullPaths: Boolean
        get() = prefs.getBoolean(KEY_SHOW_FULL_PATHS, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_FULL_PATHS, value).apply()

    var viewMode: ViewMode
        get() {
            val name = prefs.getString(KEY_VIEW_MODE, ViewMode.LIST.name) ?: ViewMode.LIST.name
            return try {
                ViewMode.valueOf(name)
            } catch (e: Exception) {
                ViewMode.LIST
            }
        }
        set(value) = prefs.edit().putString(KEY_VIEW_MODE, value.name).apply()

    var sortBy: SortBy
        get() {
            val name = prefs.getString(KEY_SORT_BY, SortBy.NAME.name) ?: SortBy.NAME.name
            return try {
                SortBy.valueOf(name)
            } catch (e: Exception) {
                SortBy.NAME
            }
        }
        set(value) = prefs.edit().putString(KEY_SORT_BY, value.name).apply()

    var sortOrder: SortOrder
        get() {
            val name = prefs.getString(KEY_SORT_ORDER, SortOrder.ASCENDING.name) ?: SortOrder.ASCENDING.name
            return try {
                SortOrder.valueOf(name)
            } catch (e: Exception) {
                SortOrder.ASCENDING
            }
        }
        set(value) = prefs.edit().putString(KEY_SORT_ORDER, value.name).apply()

    var predictiveBackEnabled: Boolean
        get() = prefs.getBoolean(KEY_PREDICTIVE_BACK, true)
        set(value) = prefs.edit().putBoolean(KEY_PREDICTIVE_BACK, value).apply()

    fun resetAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "file_material_prefs"
        private const val KEY_LAST_DIR = "last_directory_path"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_AMOLED_DARK = "amoled_dark"
        private const val KEY_COMPACT_VIEW = "compact_view"
        private const val KEY_SHOW_EXTENSIONS = "show_file_extensions"
        private const val KEY_CONFIRM_DELETE = "confirm_delete"
        private const val KEY_SHOW_HIDDEN = "show_hidden_files"
        private const val KEY_SHOW_FULL_PATHS = "show_full_paths"
        private const val KEY_VIEW_MODE = "view_mode"
        private const val KEY_SORT_BY = "sort_by"
        private const val KEY_SORT_ORDER = "sort_order"
        private const val KEY_PREDICTIVE_BACK = "predictive_back_enabled"
    }
}
