package dev.nilswitt.webmap.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.nilswitt.webmap.entities.converter.EnumConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonSerialize;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TacticalIcon {

    @Enumerated(EnumType.STRING)
    private GrundzeichenId grundzeichen = GrundzeichenId.OHNE;

    @JsonGetter("grundzeichen")
    public String getGrundzeichenId() {
        return grundzeichen.getId();
    }

    @Enumerated(EnumType.STRING)
    private OrganisationId organisation = OrganisationId.OHNE;

    @JsonGetter("organisation")
    public String getOrganisationId() {
        return organisation.getId();
    }

    @Enumerated(EnumType.STRING)
    private FachaufgabeId fachaufgabe = FachaufgabeId.OHNE;

    @JsonGetter("fachaufgabe")
    public String getFachaufgabeId() {
        return fachaufgabe.getId();
    }

    @Enumerated(EnumType.STRING)
    private EinheitId einheit = EinheitId.OHNE;

    @JsonGetter("einheit")
    public String getEinheitId() {
        return einheit.getId();
    }

    @Enumerated(EnumType.STRING)
    private VerwaltungsstufeId verwaltungsstufe = VerwaltungsstufeId.OHNE;

    @JsonGetter("verwaltungsstufe")
    public String getVerwaltungsstufeId() {
        return verwaltungsstufe.getId();
    }

    @Enumerated(EnumType.STRING)
    private FunktionId funktion = FunktionId.OHNE;

    @JsonGetter("funktion")
    public String getFunktionId() {
        return funktion.getId();
    }

    @Enumerated(EnumType.STRING)
    private SymbolId symbol = SymbolId.OHNE;

    @JsonGetter("symbol")
    public String getSymbolId() {
        return symbol.getId();
    }

    @Column(name = "tactical_icon_text")
    private String text = "";
    @Column(name = "tactical_icon_typ")
    private String typ = "";

    @Column(name = "tactical_icon_name")
    private String name = "";

    @Column(name = "tactical_icon_organisation_name")
    private String organisationName = "";


    public enum GrundzeichenId {
        OHNE("ohne"),
        TAKTISCHE_FORMATION("taktische-formation"),
        BEFEHLSSTELLE("befehlsstelle"),
        STELLE("stelle"),
        ORTSFESTE_STELLE("ortsfeste-stelle"),
        PERSON("person"),
        GEBAEUDE("gebaeude"),
        FAHRZEUG("fahrzeug"),
        KRAFTFAHRZEUG_LANDGEBUNDEN("kraftfahrzeug-landgebunden"),
        KRAFTFAHRZEUG_GELAENDEGAENGIG("kraftfahrzeug-gelaendegaengig"),
        KRAFTFAHRZEUG_GELAENDEGAENGIG_KATEGORIE3("kraftfahrzeug-gelaendegaengig-kategorie3"),
        AMPHIBIENFAHRZEUG("amphibienfahrzeug"),
        WECHSELLADER("wechsellader"),
        WECHSELLADER_WECHSELBEHAELTER("wechsellader-wechselbehaelter"),
        ABROLLBEHAELTER("abrollbehaelter"),
        WECHSELBEHAELTER("wechselbehaelter"),
        WECHSELBRUECKE("wechselbruecke"),
        ROLLCONTAINER("rollcontainer"),
        ANHAENGER("anhaenger"),
        ANHAENGER_ABROLLBEHAELTER("anhaenger-abrollbehaelter"),
        ANHAENGER_WECHSELBEHAELTER("anhaenger-wechselbehaelter"),
        ANHAENGER_PKW("anhaenger-pkw"),
        ANHAENGER_LKW("anhaenger-lkw"),
        SCHIENENFAHRZEUG("schienenfahrzeug"),
        KETTENFAHRZEUG("kettenfahrzeug"),
        FAHRRAD("fahrrad"),
        KRAFTRAD("kraftrad"),
        ZWEIRAD("zweirad"),
        WASSERFAHRZEUG("wasserfahrzeug"),
        FLUGZEUG("flugzeug"),
        HUBSCHRAUBER("hubschrauber"),
        MASSNAHME("massnahme"),
        ANLASS("anlass"),
        GEFAHR("gefahr"),
        GEFAHR_VERMUTET("gefahr-vermutet"),
        GEFAHR_AKUT("gefahr-akut");

        private final String id;

        GrundzeichenId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static GrundzeichenId fromId(String id) {
            for (GrundzeichenId value : GrundzeichenId.values()) {
                if (value.id.equals(id)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown id: " + id);
        }
    }

    public enum OrganisationId {
        OHNE(""),
        FEUERWEHR("feuerwehr"),
        THW("thw"),
        FUEHRUNG("fuehrung"),
        POLIZEI("polizei"),
        GEFAHRENABWEHR("gefahrenabwehr"),
        HILFSORGANISATION("hilfsorganisation"),
        BUNDESWEHR("bundeswehr"),
        ZIVIL("zivil");

        private final String id;

        OrganisationId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static OrganisationId fromId(String id) {
            for (OrganisationId value : OrganisationId.values()) {
                if (value.id.equals(id)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown id: " + id);
        }
    }

    public enum FachaufgabeId {
        OHNE(""),
        BRANDBEKAEMPFUNG("brandbekaempfung"),
        HOEHENRETTUNG("hoehenrettung"),
        WASSERVERSORGUNG("wasserversorgung"),
        TECHNISCHE_HILFELEISTUNG("technische-hilfeleistung"),
        HEBEN("heben"),
        BERGUNG("bergung"),
        RAEUMEN("raeumen"),
        ENTSCHAERFEN("entschaerfen"),
        SPRENGEN("sprengen"),
        BELEUCHTUNG("beleuchtung"),
        TRANSPORT("transport"),
        ABC("abc"),
        MESSEN("messen"),
        DEKONTAMINATION("dekontamination"),
        DEKONTAMINATION_PERSONEN("dekontamination-personen"),
        DEKONTAMINATION_GERAETE("dekontamination-geraete"),
        UMWELTSCHAEDEN_GEWAESSER("umweltschaeden-gewaesser"),
        RETTUNGSWESEN("rettungswesen"),
        AERZTLICHE_VERSORGUNG("aerztliche-versorgung"),
        KRANKENHAUS("krankenhaus"),
        EINSATZEINHEIT("einsatzeinheit"),
        BETREUUNG("betreuung"),
        SEELSORGE("seelsorge"),
        UNTERBRINGUNG("unterbringung"),
        LOGISTIK("logistik"),
        VERPFLEGUNG("verpflegung"),
        VERBRAUCHSGUETER("verbrauchsgueter"),
        VERSORGUNG_TRINKWASSER("versorgung-trinkwasser"),
        VERSORGUNG_BRAUCHWASSER("versorgung-brauchwasser"),
        VERSORGUNG_ELEKTRIZITAET("versorgung-elektrizitaet"),
        INSTANDHALTUNG("instandhaltung"),
        FUEHRUNG("fuehrung"),
        IUK("iuk"),
        ERKUNDUNG("erkundung"),
        VETERINAERWESEN("veterinaerwesen"),
        SCHLACHTEN("schlachten"),
        WASSERRETTUNG("wasserrettung"),
        WASSERFAHRZEUGE("wasserfahrzeuge"),
        RETTUNGSHUNDE("rettungshunde"),
        PUMPEN("pumpen"),
        ABWEHR_WASSERGEFAHREN("abwehr-wassergefahren"),
        WARNEN("warnen");

        private final String id;

        FachaufgabeId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static FachaufgabeId fromId(String id) {
            for (FachaufgabeId value : FachaufgabeId.values()) {
                if (value.id.equals(id)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown id: " + id);
        }
    }

    public enum EinheitId {
        OHNE(""),
        TRUPP("trupp"),
        STAFFEL("staffel"),
        GRUPPE("gruppe"),
        ZUG("zug"),
        ZUGTRUPP("zugtrupp"),
        BEREITSCHAFT("bereitschaft"),
        ABTEILUNG("abteilung"),
        GROSSVERBAND("grossverband");

        private final String id;

        EinheitId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static EinheitId fromId(String id) {
            for (EinheitId value : EinheitId.values()) {
                if (value.id.equals(id)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown id: " + id);
        }
    }

    public enum VerwaltungsstufeId {
        OHNE(""),
        GEMEINDE("gemeinde"),
        KREIS("kreis"),
        BEZIRK("bezirk"),
        LAND("land"),
        BRD("brd"),
        EU("eu");

        private final String id;

        VerwaltungsstufeId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static VerwaltungsstufeId fromId(String id) {
            for (VerwaltungsstufeId value : VerwaltungsstufeId.values()) {
                if (value.id.equals(id)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown id: " + id);
        }
    }

    public enum SymbolId {
        OHNE(""),
        DREHLEITER("drehleiter"),
        HEBEGERAET("hebegeraet"),
        BAGGER("bagger"),
        RAEUMGERAET("raeumgeraet"),
        BRUECKE("bruecke"),
        SPRENGMITTEL("sprengmittel"),
        BELEUCHTUNG("beleuchtung"),
        BETT("bett"),
        VERPFLEGUNG("verpflegung"),
        VERBRAUCHSGUETER("verbrauchsgueter"),
        TRINKWASSER("trinkwasser"),
        BRAUCHWASSER("brauchwasser"),
        ELEKTRIZITAET("elektrizitaet"),
        GERAETE("geraete"),
        SPRENGUNG("sprengung"),
        BERGUNG("bergung"),
        TRANSPORT("transport"),
        FAHRZEUG("fahrzeug"),
        FAHRRAD("fahrrad"),
        KRAFTRAD("kraftrad"),
        ZWEIRAD("zweirad"),
        FLUGZEUG("flugzeug"),
        HUBSCHRAUBER("hubschrauber"),
        ENTSTEHUNGSBRAND("entstehungsbrand"),
        FORTENTWICKELTER_BRAND("fortentwickelter-brand"),
        VOLLBRAND("vollbrand"),
        SIRENE("sirene"),
        LAUTSPRECHER("lautsprecher"),
        WARNUNG("warnung"),
        ZELT("zelt"),
        SICHTEN("sichten"),
        SAMMELN("sammeln"),
        SAMMELPLATZ_BETROFFENE("sammelplatz-betroffene"),
        VETERINAERWESEN("veterinaerwesen"),
        SCHLACHTEN("schlachten"),
        TIER_VERLETZT("tier-verletzt"),
        TIER_TOT("tier-tot"),
        PERSON("person"),
        PERSON_VERLETZT("person-verletzt"),
        PERSON_TOT("person-tot"),
        PERSON_VERMISST("person-vermisst"),
        PERSON_VERSCHUETTET("person-verschuettet"),
        PERSON_GERETTET("person-gerettet"),
        PERSON_ZU_TRANSPORTIEREN("person-zu-transportieren"),
        PERSON_TRANSPORTIERT("person-transportiert"),
        BESCHAEDIGT("beschaedigt"),
        TEILZERSTOERT("teilzerstoert"),
        ZERSTOERT("zerstoert"),
        TEILBLOCKIERT("teilblockiert"),
        BLOCKIERT("blockiert"),
        TENDENZ_STEIGEND("tendenz-steigend"),
        TENDENZ_FALLEND("tendenz-fallend"),
        TENDENZ_UNVERAENDERT("tendenz-unveraendert"),
        AUSFALL_25("ausfall-25"),
        AUSFALL_50("ausfall-50"),
        AUSFALL_75("ausfall-75"),
        AUSFALL_100("ausfall-100"),
        ABC("abc"),
        DEKONTAMINATION("dekontamination"),
        DEKONTAMINATION_PERSONEN("dekontamination-personen"),
        DEKONTAMINATION_GERAETE("dekontamination-geraete"),
        WASSER("wasser"),
        WASSERFAHRZEUG("wasserfahrzeug"),
        PUMPE("pumpe"),
        BILDUEBERTRAGUNG("bilduebertragung"),
        BILDUEBERTRAGUNG_FUNK("bilduebertragung-funk"),
        DATENUEBERTRAGUNG("datenuebertragung"),
        DATENUEBERTRAGUNG_FUNK("datenuebertragung-funk"),
        FAX("fax"),
        FAX_FUNK("fax-funk"),
        FERNSPRECHEN("fernsprechen"),
        FERNSPRECHEN_FUNK("fernsprechen-funk"),
        FERNSCHREIBEN("fernschreiben"),
        FERNSCHREIBEN_FUNK("fernschreiben-funk"),
        FESTBILDUEBERTRAGUNG("festbilduebertragung"),
        FESTBILDUEBERTRAGUNG_FUNK("festbilduebertragung-funk"),
        RELAISFUNKBETRIEB("relaisfunkbetrieb"),
        RICHTBETRIEB("richtbetrieb"),
        KABELBAU("kabelbau"),
        VERMUTUNG("vermutung"),
        AKUT("akut"),
        TECHNISCHE_HILFELEISTUNG("technische-hilfeleistung"),
        SEELSORGE("seelsorge"),
        DROHNE("drohne");

        private final String id;

        SymbolId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static SymbolId fromId(String id) {
            for (SymbolId value : SymbolId.values()) {
                if (value.id.equals(id)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown id: " + id);
        }
    }

    public enum FunktionId {
        OHNE(""),
        FUEHRUNGSKRAFT("fuehrungskraft"),
        SONDERFUNKTION("sonderfunktion");

        private final String id;

        FunktionId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static FunktionId fromId(String id) {
            for (FunktionId value : FunktionId.values()) {
                if (value.id.equals(id)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown id: " + id);
        }
    }
}
