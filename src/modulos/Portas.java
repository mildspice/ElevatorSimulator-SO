package modulos;

import enums.EstadoPortas;

/**
 * <b>
 * Módulo "Portas".
 * </b>
 *
 * @author Grupo4
 * <p>
 * 8170212 </p>
 * <p>
 * 8170282 </p>
 * <p>
 * 8170283 </p>
 * 
 * ISTO VAI SER PARA ALTERAR, NAO FAZ SENTIDO FICAR ASSIM.
 * 
 * E AINDA NAO SEI SE DEVERIA FAZER UMA THREAD COM ISTO
 */
public class Portas{

    private EstadoPortas estado = EstadoPortas.ABERTO;
    
    /**
     * Abre as portas. (altera o estado atual das portas para "aberto")
     */
    public void setAberto() {
        this.estado = EstadoPortas.ABERTO;
    }

    /**
     * Fecha as portas. (altera o estado atual das portas para "fechado")
     */
    public void setFechado() {
        this.estado = EstadoPortas.FECHADO;
    }

    /**
     * Retorna o estado atual das portas.
     *
     * @return enum constant sobre o estado das portas
     */
    public EstadoPortas getEstado() {
        return this.estado;
    }
}
