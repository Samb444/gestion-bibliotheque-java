package sn.codesamb.exception;

/**
 * Exception levée lorsqu'un membre tente d'avoir plus de 3 emprunts simultanés.
 */
public class QuotaEmpruntDepasseException extends BibliothequeException {

    private static final String MESSAGE_DEFAUT = "Le membre ne peut pas avoir plus de 3 emprunts simultanés.";

    public QuotaEmpruntDepasseException() {
        super(MESSAGE_DEFAUT);
    }

    public QuotaEmpruntDepasseException(String message) {
        super(message);
    }
}
