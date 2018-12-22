package modulos;

import java.util.logging.Level;
import java.util.logging.Logger;
import tools.MonitorElevador;

/**
 * <b>'Sub-thread' do módulo principal</b>
 * Aqui faz-se a monitorização do desenvolvimento funcional do elevador enquanto
 * faz o percurso até ao próximo piso.
 */
public class MainMovimentoElevador extends Thread {

    //em segundos
    private final int FlOOR_WAIT_TIME_MS = 5;
    //número de pisos a deslocar ...
    private final int numOfFloors;
    //objeto partilhado
    protected MonitorElevador monitor;

    /**
     * Construtor para a thread.
     * 
     * @param monitor objeto partilhado
     * @param num numero de pisos entre o priso atual e o piso final
     */
    public MainMovimentoElevador(MonitorElevador monitor, int num) {
        this.monitor = monitor;
        this.numOfFloors = num;
    }

    @Override
    public void run() {

        //nome da thread (caso dê jeito usar, senao pode-se tirar ...)
        Thread.currentThread().setName("[Thread_RunningElevator]");
        try {
            //sinalização sobre a chegada ao destino
            this.monitor.setFloorReachedFlag(false);
            //tempo de espera até que o elevador comece a andar
            Thread.sleep(monitor.MOVEMENT_WAITING_TIME);

            /**
             * cada iteração do ciclo representa o movimento do elevador entre
             * cada piso
             * 
             * NOTA:
             * Secalhar era melhor fazer o escalonamento de pisos.
             * Do tipo, dar a possibilidade para carregar em vários pisos.
             * Para isso faz-se um algoritmo de escalonamento (até pode ser
             * o FIFO, assim era mais facil).
             */
            for (int i = 0; i < this.numOfFloors; i++) {
                /**
                 * este ciclo representa o tempo que o elevador vai demorar a
                 * deslocar-se entre pisos (e vai verificando a cada segundo se o botao
                 * de paragem foi acionado)
                 */
                for (int j = 0; j <= this.FlOOR_WAIT_TIME_MS; j++) {
                    Thread.sleep(1000);
                    /**
                     * este "if" está diretamente relacionado ao uso do botao de
                     * stop (depois quando voltar a andar ao carregar outravez no
                     * botão, faz (monitor.acordaTodas();)
                     */
                    if (!monitor.isEmFuncionamento()) {
                        monitor.espera();
                    }
                }

                if (this.monitor.getDirecaoMotor().toString().equals("CIMA")) {
                    this.monitor.setPisoAtual(this.monitor.getPisoAtual() + 1);
                } else {
                    this.monitor.setPisoAtual(this.monitor.getPisoAtual() - 1);
                }
            }
            //sinalização

            this.monitor.setFloorReachedFlag(true);
        } catch (InterruptedException ex) {
            Logger.getLogger(MainMovimentoElevador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
