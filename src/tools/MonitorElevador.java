package tools;

import enums.EstadosMotor;
import enums.EstadosPortas;
import javax.swing.JFrame;

/**
 * <b>Shared Object</b>
 * Objeto partilhado entre todas as threads que contém todas as variáveis e
 * métodos convenientes.
 */
public class MonitorElevador {

    //tempo de espera entre as execuções
    public final int MOVEMENT_WAITING_TIME = 1000;
    //public final int DOOR_EXECUTE_TIME = 1000;

    //variáveis gerais do funcionamento do elevador
    private boolean flagFloorReached = true;
    private boolean flagFuncionamento = false;
    public final int NUM_PISOS;
    private int pisoAtual = 0;

    //variáveis relacionadas ao estado do motor e das portas
    private EstadosMotor direcaoMotor;
    private EstadosPortas estadoPortas = EstadosPortas.ABERTO;

    //variâveis relacionadas ao funcionamento da Botoneira
    private final String[] botoesPisos;
    private final String[] botoesPortas = {"A", "F"};
    private boolean chave = false;
    private String input = "";

    public MonitorElevador(int numPisos) {
        this.NUM_PISOS = numPisos;
        this.botoesPisos = new String[numPisos];
        for (int i = 0; i < this.botoesPisos.length; i++) {
            this.botoesPisos[i] = "PISO" + i;
        }
    }

    public synchronized void acorda() {
        this.notify();
    }

    public synchronized void acordaTodas() {
        this.notifyAll();
    }

    public synchronized void espera() {
        try {
            while (!this.flagFuncionamento) {
                this.wait();
            }

        } catch (InterruptedException ie) {
        }
    }

    /**
     * Atualiza a posição do elevador.
     *
     * @param piso numero do piso onde se encontra o elevador
     */
    public synchronized void setPisoAtual(int piso) {
        this.pisoAtual = piso;
    }

    /**
     * Retorna a posição atual do elevador
     *
     * @return numero do piso onde se encontra atualmente o elevador
     */
    public synchronized int getPisoAtual() {
        return this.pisoAtual;
    }

    /**
     * Retorna a identificação dos botões referentes à botoneira do elevador
     *
     * @return array de String com o nome dos botões dos pisos
     */
    public synchronized String[] getBotoesPisos() {
        return this.botoesPisos;
    }

    /**
     * Retorna a identificação dos botões referentes à botoneira do elevador
     *
     * @return array de String com o nome dos botões relativos ao estado das
     * portas
     */
    public synchronized String[] getBotoesPortas() {
        return this.botoesPortas;
    }

    /**
     * (Este método só vai ser utilizado pelo botão do JFrame)
     *
     * @param state estado boleano da chave
     */
    public synchronized void setChave(boolean state) {
        this.chave = state;
    }

    /**
     * Retorna o estado da chave.
     *
     * @return estado boleano da chave
     */
    public synchronized boolean isChaveAcionada() {
        return this.chave;
    }

    /**
     * Guarda o input feito pela botoneira. (Método utilizado só pelo JFrame)
     *
     * @param input string com o input
     */
    public synchronized void setInput(String input) {
        this.input = input;
    }

    /**
     * Retorna o input feito pelo utilizador (na botoneira)
     *
     * @return string com o input
     */
    public synchronized String getInput() {
        return this.input;
    }

    /**
     * Altera a direção do motor para se deslocar para cima ou para baixo.
     *
     * @param estado enum constant referente à direção
     */
    public synchronized void setDirecaoMotor(EstadosMotor estado) {
        this.direcaoMotor = estado;
    }

    /**
     * Retorna o estado direcional do motor.
     *
     * @return enum constant sobre o estado atual do motor
     */
    public synchronized EstadosMotor getDirecaoMotor() {
        return this.direcaoMotor;
    }

    /**
     * Retorna o estado atual das portas.
     *
     * @param estado enum constant referente ao estado das portas
     */
    public synchronized void setEstadoPortas(EstadosPortas estado) {
        this.estadoPortas = estado;
    }

    /**
     * Retorna o estado atual das portas.
     *
     * @return enum constant sobre o estado das portas
     */
    public synchronized EstadosPortas getEstadoPortas() {
        return this.estadoPortas;
    }

    /**
     * Altera a sinalização para se o elevador se encontra no piso em questão
     *
     * @param flag
     */
    public synchronized void setFloorReachedFlag(boolean flag) {
        this.flagFloorReached = flag;
    }

    /**
     * Retorna a sinalização sobre se o elevador se encontra no piso em questão
     *
     * @return retorna se o elevador se encontra num piso ou não
     */
    public synchronized boolean isFloorReached() {
        return this.flagFloorReached;
    }

    /**
     * Altera o estado funcional do elevador.
     *
     * @param flag booleano para o andamento do elevador
     */
    public synchronized void setFlagFuncionamento(boolean flag) {
        this.flagFuncionamento = flag;
    }

    /**
     * Retorna o estado funcional do elevador.
     *
     * @return retorna se o elevador está ou não em funcionamento
     */
    public synchronized boolean isEmFuncionamento() {
        return this.flagFuncionamento;
    }
    
    public JFrame criarJanelaPrincipal() {
        /* FAZER JANELA PRINCIPAL DO ELEVADOR */
        JFrame guiFrame = new JFrame();
        
        return guiFrame;
    }
}
