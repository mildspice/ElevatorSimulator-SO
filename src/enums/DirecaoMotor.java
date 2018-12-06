package enums;

/**
 * Enumeração com as opções para o estado da direção do motor do elevador.
 *
 * @author Grupo4
 * <p>
 * 8170212 </p>
 * <p>
 * 8170282 </p>
 * <p>
 * 8170283 </p>
 */
public enum DirecaoMotor {
    CIMA, BAIXO;
    
    public String message() {
        switch( this ) {
            case CIMA:
                return "*** |^  Going Up  ^| ***";
            case BAIXO:
                return "*** |v Going Down v| ***";
            default:
                return null;
        }
    }
}
