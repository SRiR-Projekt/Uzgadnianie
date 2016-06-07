package org.srir.byzantine;

public enum StepState {
	FORMS_POOL, //ten krok to akceptowanie wiadomości oraz nie osiągnięcie minimalnej liczby to przejścia stanu
	WAITS_FOR_TIME_OUT, //minimalna liczba wiadomości została osiągnięta ale krok czeka na wolnejsze wiadomości aż przyjdą
	READY //krok ten nie przyjmuje więcej wiadomości i jest gotowy do wykonania procedury
}
