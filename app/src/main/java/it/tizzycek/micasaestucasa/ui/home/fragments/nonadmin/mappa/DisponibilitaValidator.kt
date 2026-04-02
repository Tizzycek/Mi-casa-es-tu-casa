package it.tizzycek.micasaestucasa.ui.home.fragments.nonadmin.mappa

import android.os.Parcel
import android.os.Parcelable
import com.google.android.material.datepicker.CalendarConstraints
import it.tizzycek.micasaestucasa.data.proprieta.Disponibilita
import java.time.ZoneOffset

/**
 * A [CalendarConstraints.DateValidator] that marks a date as valid only if it falls
 * within at least one of the provided [Disponibilita] ranges.
 *
 * The ranges are stored as parallel [LongArray]s of UTC-millisecond timestamps so that
 * the validator can be parcelled and restored by the [MaterialDatePicker] framework.
 *
 * Since [MaterialDatePicker] always passes UTC-midnight timestamps, and we store the
 * bounds as UTC-midnight timestamps too, [isValid] can compare longs directly without
 * any [LocalDate] conversion.
 */
class DisponibilitaValidator private constructor(
    private val inizioMs: LongArray,
    private val fineMs: LongArray
) : CalendarConstraints.DateValidator {

    // ── Construction ────────────────────────────────────────────────────────

    constructor(disponibilita: List<Disponibilita>) : this(
        inizioMs = disponibilita
            .map { it.dataInizio.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
            .toLongArray(),
        fineMs = disponibilita
            .map { it.dataFine.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
            .toLongArray()
    )

    // ── DateValidator ───────────────────────────────────────────────────────

    /**
     * Returns `true` when [date] (UTC-midnight ms) falls inside at least one
     * availability window. Both bounds are inclusive.
     */
    override fun isValid(date: Long): Boolean =
        inizioMs.indices.any { i -> date >= inizioMs[i] && date <= fineMs[i] }

    // ── Parcelable ──────────────────────────────────────────────────────────

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLongArray(inizioMs)
        dest.writeLongArray(fineMs)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DisponibilitaValidator> =
            object : Parcelable.Creator<DisponibilitaValidator> {
                override fun createFromParcel(source: Parcel): DisponibilitaValidator {
                    val inizio = source.createLongArray() ?: LongArray(0)
                    val fine = source.createLongArray() ?: LongArray(0)
                    return DisponibilitaValidator(inizio, fine)
                }

                override fun newArray(size: Int): Array<DisponibilitaValidator?> =
                    arrayOfNulls(size)
            }
    }
}
