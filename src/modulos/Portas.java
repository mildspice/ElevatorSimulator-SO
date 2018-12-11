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
 */
public class Portas {

    private EstadoPortas estado;

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

    /*
    * Define os estados da porta
    *
    * @param input String de dados referente ao input do utlizador
     */

    public void acaoPortas(String input){
        Botoneira botoneira = new Botoneira(input);

         if(botoneira.Outvalue()==0) {
            if(botoneira.getActionbtn().equals("A")){
                setAberto();
            }else if(botoneira.getActionbtn().equals("F")){
                setFechado();
            }

         }
    }
}
