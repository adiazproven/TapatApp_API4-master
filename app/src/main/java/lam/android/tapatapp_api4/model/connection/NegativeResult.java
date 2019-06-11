package lam.android.tapatapp_api4.model.connection;

public class NegativeResult {

    private int code;
    private String message;

    public NegativeResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public NegativeResult(int code) {
        this.code = code;
        this.message = "Pongase en contacto con el desarrollador.";
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
