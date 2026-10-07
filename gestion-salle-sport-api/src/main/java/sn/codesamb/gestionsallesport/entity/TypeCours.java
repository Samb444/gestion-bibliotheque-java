package sn.codesamb.gestionsallesport.entity;

/**
 * Énumération définissant les différentes disciplines de cours proposées par la salle de sport.
 * <p>
 * Les valeurs sont stockées sous forme textuelle ({@code EnumType.STRING}) en base de données
 * pour garantir la lisibilité et l'évolutivité.
 */
public enum TypeCours {
    YOGA,
    CARDIO,
    MUSCULATION,
    HIIT,
    CIRCUIT
}
