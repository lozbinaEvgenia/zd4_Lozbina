package com.example.crimeintent

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "crime")
data class Crime(
    @PrimaryKey var id: UUID = UUID.randomUUID(),
    @ColumnInfo(name = "title") var title: String = "",
    @ColumnInfo(name = "date") var date: Date = Date(),
    @ColumnInfo(name = "is_solved") var isSolved: Boolean = false,
    @ColumnInfo(name = "suspect") var suspect: String = ""
) {
    @Ignore
    constructor(title: String, date: Date, isSolved: Boolean, suspect: String) : this(
        UUID.randomUUID(),
        title,
        date,
        isSolved,
        suspect
    )
}
