package com.example.medicinetracker.data.local

import androidx.room.TypeConverter
import com.example.medicinetracker.data.model.DurationUnit
import com.example.medicinetracker.data.model.FrequencyType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.format(dateFormatter)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, dateFormatter) }
    }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.format(timeFormatter)
    }

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it, timeFormatter) }
    }

    @TypeConverter
    fun fromLocalTimeList(value: List<LocalTime>?): String? {
        return value?.joinToString(",") { it.format(timeFormatter) }
    }

    @TypeConverter
    fun toLocalTimeList(value: String?): List<LocalTime>? {
        return value?.split(",")?.filter { it.isNotEmpty() }?.map { LocalTime.parse(it, timeFormatter) }
    }

    @TypeConverter
    fun fromDayOfWeekList(value: List<DayOfWeek>?): String? {
        return value?.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toDayOfWeekList(value: String?): List<DayOfWeek>? {
        return value?.split(",")?.filter { it.isNotEmpty() }?.map { DayOfWeek.valueOf(it) }
    }

    @TypeConverter
    fun fromFrequencyType(value: FrequencyType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toFrequencyType(value: String?): FrequencyType? {
        return value?.let { FrequencyType.valueOf(it) }
    }

    @TypeConverter
    fun fromDurationUnit(value: DurationUnit?): String? {
        return value?.name
    }

    @TypeConverter
    fun toDurationUnit(value: String?): DurationUnit? {
        return value?.let { DurationUnit.valueOf(it) }
    }
}
