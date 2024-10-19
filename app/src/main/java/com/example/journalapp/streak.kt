package com.example.journalapp

import android.os.Parcel
import android.os.Parcelable

// Page created to store the streak page.
// Creator: Ana Goncalves.
data class Streak(var count: Int, var lastDate: Long) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(count)
        parcel.writeLong(lastDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Streak> {
        override fun createFromParcel(parcel: Parcel): Streak {
            return Streak(parcel)
        }

        override fun newArray(size: Int): Array<Streak?> {
            return arrayOfNulls(size)
        }
    }
}