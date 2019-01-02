package modulos;

import java.util.logging.Level;
import java.util.logging.Logger;
import tools.MonitorElevador;

/**
 * <h1>'Sub-thread' do módulo principal</h1>
 * <p>
 * <b>Aqui faz-se a monitorização do desenvolvimento funcional do elevador
 * enquanto faz o percurso até ao próximo piso.</b>
 *
 * @author Grupo21-8170212_8170282_8170283
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

    /**
     * <b>Método responsável pelo funcionamento da thread.</b>
     * <p>
     * NOTAS (estão espalhados ao longo do código vários comentários
     * importantes, no entanto alguns vão ser colocados aqui por conveniência do
     * javadoc.):
     * </p>
     * <p>
     * {@code for (int i = 0; i < this.numOfFloors; i++) ...} - Cada iteração do
     * ciclo representa o movimento do elevador entre cada piso.
     * </p>
     * <p>
     * {@code for (int j = 0; j <= this.FlOOR_WAIT_TIME_MS; j++) {
     * Thread.sleep(1000);
     * ...}} - este ciclo representa o tempo que o elevador vai demorar a
     * deslocar-se entre pisos (e vai verificando a cada segundo se o botao de
     * paragem foi acionado)
     * </p>
     * <p>
     * {@code if (!monitor.isEmFuncionamento()) {
     * monitor.espera();
     * }} - este "if" está diretamente relacionado ao uso do botao de stop
     * (depois quando voltar a andar ao carregar outravez no botão, faz
     * (monitor.acordaTodas();)
     * </p>
     */
    @Override
    public void run() {
        //variável para o cálculo do tempo de execução
        double tempoInicial= System.currentTimeMillis();
        String threadName = "[Thread_RunningElevator]";
        Thread.currentThread().setName(threadName);
        try {
            //sinalização sobre a chegada ao destino
            this.monitor.setFloorReachedFlag(false);
            //tempo de espera até que o elevador comece a andar
            //Thread.sleep(monitor.MOVEMENT_WAITING_TIME); 
            //agora fica só a esperar no motor.
            monitor.espera();
            //variável para o log
            int pisoInicial=monitor.getPisoAtual();
            /**
             * cada iteração do ciclo representa o movimento do elevador entre
             * cada piso
             *
             * NOTA (ja esta feito, fica aqui na mesma como "MEMO"): Secalhar
             * era melhor fazer o escalonamento de pisos. Do tipo, dar a
             * possibilidade para carregar em vários pisos. Para isso faz-se um
             * algoritmo de escalonamento (até pode ser o FIFO, assim era mais
             * facil).
             */
            for (int i = 0; i < this.numOfFloors; i++) {
                /**
                 * este ciclo representa o tempo que o elevador vai demorar a
                 * deslocar-se entre pisos (e vai verificando a cada segundo se
                 * o botao de paragem foi acionado)
                 */
                for (int j = 0; j <= this.FlOOR_WAIT_TIME_MS; j++) {
                    Thread.sleep(1000);
                    /**
                     * este "if" está diretamente relacionado ao uso do botao de
                     * stop (depois quando voltar a andar ao carregar outravez
                     * no botão, faz (monitor.acordaTodas();)
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

                monitor.displayPisoAtual();
            }

            //impressão para a janela principal ...
            if (!monitor.getFloorQueue().isEmpty()) {
                monitor.removeFloorReached();
            }
            monitor.setTempoExec(System.currentTimeMillis()-tempoInicial);
            monitor.displayQueue();
            monitor.displayPisoAtual();
            monitor.usingCounter();
            monitor.writeLog(Thread.currentThread(),pisoInicial,monitor.getPisoAtual());
            //sinalização sobre a chegada ao destino
            this.monitor.setFloorReachedFlag(true);
        } catch (InterruptedException ex) {
            System.err.println("Problemas na thread: " + threadName + "\nLOG: ");
            Logger.getLogger(MainMovimentoElevador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
