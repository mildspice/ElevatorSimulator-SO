package enums;

/**
 * Enumeração com as opções para o estado das portas do elevador.
 *
 * @author Grupo4
 * <p>
 * 8170212 </p>
 * <p>
 * 8170282 </p>
 * <p>
 * 8170283 </p>
 */
public enum EstadoPortas {
    ABERTO, FECHADO;
    
    public String message() {
        switch( this ) {
            case ABERTO:
                return "**** <-  OPEN  -> ****";
            case FECHADO:
                return "**** -> CLOSED <- ****";
            default:
                return null;
        }
    }
}
