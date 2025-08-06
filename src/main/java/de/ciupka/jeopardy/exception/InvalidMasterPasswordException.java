package de.ciupka.jeopardy.exception;

public class InvalidMasterPasswordException extends Exception {
    
    @Override
    public String getMessage() {
        return "Das eingebene Master-Passwort ist nicht korrekt!";
    }

}
