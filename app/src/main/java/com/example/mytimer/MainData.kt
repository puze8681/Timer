package com.example.mytimer

import android.os.Parcel
import android.os.Parcelable

data class MainData(var type: Int, var name: String?, var time: Long) : Parcelable {
    constructor(source: Parcel) : this(
        source.readInt(),
        source.readString(),
        source.readLong()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(type)
        writeString(name)
        writeLong(time)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<MainData> = object : Parcelable.Creator<MainData> {
            override fun createFromParcel(source: Parcel): MainData = MainData(source)
            override fun newArray(size: Int): Array<MainData?> = arrayOfNulls(size)
        }
    }
}