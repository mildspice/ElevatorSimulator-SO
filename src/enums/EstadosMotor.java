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
public enum EstadosMotor {
    //stopped foi adicionado só por conveniência
    CIMA, BAIXO, STOPPED;
    
    public String message() {
        switch( this ) {
            case CIMA:
                return "*** |^  Going Up  ^| ***";
            case BAIXO:
                return "*** |v Going Down v| ***";
            case STOPPED:
                return "*** Engine Stopped ***";
            default:
                return "*** Direction Not Yet Taken ***";
        }
    }
}
