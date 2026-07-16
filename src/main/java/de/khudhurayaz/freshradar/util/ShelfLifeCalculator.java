package de.khudhurayaz.freshradar.util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public class ShelfLifeCalculator {

    public enum ProductCategory {
        // Konstruktor: Map mit Regeln, gefolgt von beliebig vielen verbotenen Lagerorten (Varargs)
        FLEISCH(Map.of("KÜHLSCHRANK", 2, "GEFRIERFACH", 180), "TROCKENLAGER"),
        FISCH(Map.of("KÜHLSCHRANK", 1, "GEFRIERFACH", 90), "TROCKENLAGER"),
        MILCHPRODUKTE(Map.of("KÜHLSCHRANK", 4, "GEFRIERFACH", 90), "TROCKENLAGER"),
        BACKWAREN(Map.of("TROCKENLAGER", 3, "GEFRIERFACH", 180, "KÜHLSCHRANK", 7));

        private final Map<String, Integer> rules;
        private final Set<String> forbiddenStorage;

        ProductCategory(Map<String, Integer> rules, String... forbidden) {
            this.rules = rules;
            this.forbiddenStorage = Set.of(forbidden);
        }

        public int getDaysForStorage(String storage) {
            return rules.getOrDefault(storage, 7);
        }

        public boolean isStorageForbidden(String storage) {
            return forbiddenStorage.contains(storage);
        }
    }

    /**
     * Berechnet das neue Ablaufdatum nach dem Öffnen.
     * @param category Die Kategorie des Produkts
     * @param storage Der gewählte Lagerort
     * @param originalMhdTimestamp Das ursprüngliche Haltbarkeitsdatum
     * @return Das berechnete neue Ablaufdatum

     */
    public static Timestamp calculateAsTimestamp(ProductCategory category, String storage, Timestamp originalMhdTimestamp) {
        System.out.println("CalculateAsTimestamp Methode");
        if (category.isStorageForbidden(storage)) {
            throw new IllegalArgumentException("Sicherheitswarnung: " + category + " darf nicht in " + storage + " gelagert werden!");
        }

        LocalDate originalMhd = originalMhdTimestamp.toLocalDateTime().toLocalDate();
        LocalDate today = LocalDate.now();
        LocalDate storageBasedExpiry = today.plusDays(category.getDaysForStorage(storage));

        LocalDate finalDate;

        if ("GEFRIERFACH".equals(storage)) {
            finalDate = storageBasedExpiry;

        } else {
            int getDaysForStorage = category.getDaysForStorage(storage);
            LocalDate localDate = today.plusDays(getDaysForStorage);
            finalDate = originalMhd.isBefore(localDate) ? originalMhd : localDate;

        }
        return Timestamp.valueOf(finalDate.atStartOfDay());
    }
}