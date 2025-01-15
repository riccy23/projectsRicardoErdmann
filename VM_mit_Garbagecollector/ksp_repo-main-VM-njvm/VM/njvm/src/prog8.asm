    pushc   5           // Speichere 5 in die erste globale Variable (Index 0)
    popg    0           // popg 0: Speichert den Wert 5 an Index 0 in der SDA
    
    pushc   10          // Speichere 10 in die zweite globale Variable (Index 1)
    popg    1           // popg 1: Speichert den Wert 10 an Index 1 in der SDA
    
    pushg   0           // Lade den Wert der ersten globalen Variable (5) auf den Stack
    pushc   4           // Lege die Konstante 4 auf den Stack
    mul                 // Multipliziere 5 * 4 = 20
    popg    0           // Speichere das Ergebnis (20) zurück in die erste globale Variable
    
    pushg   1           // Lade den Wert der zweiten globalen Variable (10) auf den Stack
    pushc   2           // Lege die Konstante 2 auf den Stack
    add                 // Addiere 10 + 2 = 12
    popg    1           // Speichere das Ergebnis (12) zurück in die zweite globale Variable
    
    pushg   0           // Lade den aktualisierten Wert der ersten globalen Variable (20) auf den Stack
    pushg   1           // Lade den aktualisierten Wert der zweiten globalen Variable (12) auf den Stack
    add                 // Addiere 20 + 12 = 32
    pushc   3           // Lege die Konstante 3 auf den Stack
    mul                 // Multipliziere das Ergebnis: 32 * 3 = 96
    wrint               // Gib das Ergebnis 96 als Ganzzahl aus
    
    pushc   '\n'        // Lege das Zeichen '\n' (neue Zeile) auf den Stack
    wrchr               // Gib das Zeichen '\n' aus, um die Ausgabe abzuschließen
    halt                // Beende das Programm

