package sn.codesamb.exception;

/**
 * Exception métier de base pour la gestion de la bibliothèque.
 */
public class BibliothequeException extends RuntimeException {

    public BibliothequeException(String message) {
        super(message);
    }

    public BibliothequeException(String message, Throwable cause) {
        super(message, cause);
    }
}
