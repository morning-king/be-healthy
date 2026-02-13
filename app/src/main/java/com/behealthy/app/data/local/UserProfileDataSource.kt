package com.behealthy.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

@Singleton
class UserProfileDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        val NICKNAME = stringPreferencesKey("nickname")
        val BIRTHDAY = stringPreferencesKey("birthday")
        val NOTE = stringPreferencesKey("note")
        val NOTE_IMAGE_URI = stringPreferencesKey("note_image_uri")
        val AVATAR_URI = stringPreferencesKey("avatar_uri")
        val TOTAL_WORKOUT_DAYS = stringPreferencesKey("total_workout_days")
        val TOTAL_MOOD_RECORDS = stringPreferencesKey("total_mood_records")
        val CURRENT_STREAK = stringPreferencesKey("current_streak")
        val THEME_STYLE = stringPreferencesKey("theme_style")
        val BACKGROUND_ALPHA = floatPreferencesKey("background_alpha")
        val PAGE_TRANSITION = stringPreferencesKey("page_transition")
        
        // New Theme Settings
        val ZEN_ROTATION_ENABLED = booleanPreferencesKey("zen_rotation_enabled")
        val ZEN_ROTATION_SPEED = floatPreferencesKey("zen_rotation_speed")
        val ZEN_ROTATION_DIRECTION = stringPreferencesKey("zen_rotation_direction")
        val TECH_INTENSITY = stringPreferencesKey("tech_intensity")
        val FONT_COLOR_MODE = stringPreferencesKey("font_color_mode")
    }
    
    val nickname: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[NICKNAME]
        }
    
    val birthday: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[BIRTHDAY]
        }
    
    val note: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[NOTE]
        }

    val noteImageUri: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[NOTE_IMAGE_URI]
        }
    
    val avatarUri: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[AVATAR_URI]
        }
    
    val totalWorkoutDays: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[TOTAL_WORKOUT_DAYS]?.toIntOrNull() ?: 0
        }
    
    val totalMoodRecords: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[TOTAL_MOOD_RECORDS]?.toIntOrNull() ?: 0
        }
    
    val currentStreak: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[CURRENT_STREAK]?.toIntOrNull() ?: 0
        }

    val themeStyle: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_STYLE] ?: "Default"
        }
    
    val backgroundAlpha: Flow<Float?> = context.dataStore.data
        .map { preferences ->
            preferences[BACKGROUND_ALPHA]
        }

    val pageTransition: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PAGE_TRANSITION]
        }
        
    val zenRotationEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ZEN_ROTATION_ENABLED] ?: true
        }
        
    val zenRotationSpeed: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[ZEN_ROTATION_SPEED] ?: 5f
        }
        
    val zenRotationDirection: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[ZEN_ROTATION_DIRECTION] ?: "Clockwise"
        }
        
    val techIntensity: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[TECH_INTENSITY] ?: "Standard"
        }
        
    val fontColorMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[FONT_COLOR_MODE] ?: "Auto"
        }
    
    suspend fun updateNickname(nickname: String) {
        context.dataStore.edit { preferences ->
            preferences[NICKNAME] = nickname
        }
    }
    
    suspend fun updateBirthday(birthday: String) {
        context.dataStore.edit { preferences ->
            preferences[BIRTHDAY] = birthday
        }
    }
    
    suspend fun updateNote(note: String) {
        context.dataStore.edit { preferences ->
            preferences[NOTE] = note
        }
    }
    
    suspend fun updateNoteImageUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[NOTE_IMAGE_URI] = uri
        }
    }
    
    suspend fun updateAvatarUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[AVATAR_URI] = uri
        }
    }
    
    suspend fun updateStats(totalWorkoutDays: Int, totalMoodRecords: Int, currentStreak: Int) {
        context.dataStore.edit { preferences ->
            preferences[TOTAL_WORKOUT_DAYS] = totalWorkoutDays.toString()
            preferences[TOTAL_MOOD_RECORDS] = totalMoodRecords.toString()
            preferences[CURRENT_STREAK] = currentStreak.toString()
        }
    }

    suspend fun updateThemeStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_STYLE] = style
        }
    }
    
    suspend fun updateBackgroundAlpha(alpha: Float) {
        context.dataStore.edit { preferences ->
            preferences[BACKGROUND_ALPHA] = alpha
        }
    }

    suspend fun updatePageTransition(transition: String) {
        context.dataStore.edit { preferences ->
            preferences[PAGE_TRANSITION] = transition
        }
    }
    
    suspend fun updateZenRotationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ZEN_ROTATION_ENABLED] = enabled
        }
    }
    
    suspend fun updateZenRotationSpeed(speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[ZEN_ROTATION_SPEED] = speed
        }
    }
    
    suspend fun updateZenRotationDirection(direction: String) {
        context.dataStore.edit { preferences ->
            preferences[ZEN_ROTATION_DIRECTION] = direction
        }
    }
    
    suspend fun updateTechIntensity(intensity: String) {
        context.dataStore.edit { preferences ->
            preferences[TECH_INTENSITY] = intensity
        }
    }
    
    suspend fun updateFontColorMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[FONT_COLOR_MODE] = mode
        }
    }
}