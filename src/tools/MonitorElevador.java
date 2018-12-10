package tools;

public class MonitorElevador {

    private boolean flagFloorReached = false;
    private boolean flagFuncionamento = false;
    private final int NUM_PISOS = 4;
    private int pisoAtual = 0;

    public synchronized void acorda() {
        this.notify();
    }

    public synchronized void acordaTodas() {
        this.notifyAll();
    }

    public synchronized void espera() {
        try {
            //tive que adicionar a alteração da flag aqui porque o ElevatorRunTime
            //demorava muito tempo a faze-lo e este método não fazia o wait() ...
            //(estes algoritmos so dao trabalho)
            this.flagFloorReached = false;
            while (!flagFloorReached) {
                this.wait();
            }

        } catch (InterruptedException ie) {
        }
    }

    public synchronized void setPisoAtual(int piso) {
        this.pisoAtual = piso;
    }

    public synchronized int getPisoAtual() {
        return this.pisoAtual;
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
        return flagFloorReached;
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

}
