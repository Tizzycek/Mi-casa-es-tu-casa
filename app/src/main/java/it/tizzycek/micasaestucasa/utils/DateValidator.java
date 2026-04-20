package it.tizzycek.micasaestucasa.utils;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import it.tizzycek.micasaestucasa.R;
import it.tizzycek.micasaestucasa.data.proprieta.Disponibilita;

public class DateValidator {
    public static boolean isValidBirthDate(LocalDate data) {
        LocalDate oggi = LocalDate.now();
        return oggi.getYear() - data.getYear() < 18;
    }

    // se la data è già passata va bene (da fare al contrario)
    public static boolean isBeforeToday(LocalDate data) {
        LocalDate oggi = LocalDate.now();
        return data.isBefore(oggi);
    }

    // se l'inizio è dopo la fine non va bene
    public static boolean startIsAfterEnd(LocalDate dataInizio, LocalDate dataFine) {
        return dataInizio.isAfter(dataFine);
    }

    public static boolean hasOverlaps(@NotNull List<Disponibilita> periodi) {
        periodi.sort(Comparator.comparing(Disponibilita::getDataInizio));

        for (int i = 0; i < periodi.size() - 1; i++) {
            Disponibilita current = periodi.get(i);
            Disponibilita next = periodi.get(i + 1);

            if (!current.getDataFine().isBefore(next.getDataInizio())) {
                return true;
            }
        }
        return false;
    }

    public static String formatDate(Instant daFormattare) {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault()).format(daFormattare);
    }
}
