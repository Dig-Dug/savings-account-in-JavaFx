package de.cbw.jav.account.fx;

import java.util.Random;

/** 
 * Ein Sparbuch mit grundlegenden Kontofunktionen
 * @author JB
 */
public class Sparbuch {
	static private final Random RANDOMIZER = new Random();
	
	private long kontonummer;
	private String inhaber;
	private long guthaben;
	private double zinssatz;
	private long zuletztModifiziert;

	/**
	 * Constructor, initializes a new instance.
	 * @param inhaber der Kontoinhaber
	 * @param zinssatz der Zinssatz im Interval [0 bis 1]
	 */
	public Sparbuch(double zinssatz) {
		super();
		this.kontonummer = RANDOMIZER.nextLong();
		this.zinssatz = zinssatz;
	}


	public long getKontonummer() {  // === Funktion 5 ===
		return this.kontonummer; 
	}

	public void setKontonummer(long kontonummer) { 
		this.kontonummer = kontonummer;
	}


	public String getInhaber() {
		return this.inhaber;
	}

	public void setInhaber(String inhaber) {
		this.inhaber = inhaber;
	}


	public long getGuthaben () { // === Funktion 6 ===
		return this.guthaben;
	}

	public void setGuthaben (long guthaben) {
		this.guthaben = guthaben;
	}


	public double getZinssatz () {
		return this.zinssatz;		
	}

	public void setZinssatz (double zinssatz) {
		this.zinssatz = zinssatz;
	}


	public long getZuletztModifiziert () {
		return zuletztModifiziert;
	}

	public void setZuletztModifiziert (long zuletztModifiziert) {
		this.zuletztModifiziert = zuletztModifiziert;
	}


	// ====== 1 EINZAHLEN ======
	public long zahleEin (long veraenderung) {
		return this.guthaben += veraenderung;
	}

	// ====== 2 AUSZAHLEN ======
	public long hebeAb (long veraenderung) {
		return this.guthaben -= veraenderung;
	}

	// ====== 3 PROGNOSE ZINS ======
	public long getErtrag (int laufzeit) { // Das ist eine Prognose, d.h. guthaben wird nicht verändert
		return this.guthaben + Math.round(this.guthaben * this.zinssatz * laufzeit / 365.0); // Laufzeitangabe in Tagen
	}
	
	// ====== 4 VERZINSUNG ======
	 public long verzinse () { // Verzinsung auf 1 Jahr (Summe Guthaben + Zins)
		return this.guthaben += Math.round(this.guthaben * this.zinssatz);
	}
}