package it.tizzycek.micasaestucasa.data;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

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

    public static boolean hasOverlaps(List<Disponibilita> periodi) {
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
}
