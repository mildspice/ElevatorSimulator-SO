package modulos;

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

    private final int FlOOR_WAIT_TIME_MS = 5; //em segundos
    private final int numOfFloors;

    protected MonitorElevador monitor;

    /**
     * Construtor para a thread.
     *
     * @param monitor objeto partilhado
     * @param num numero de pisos a deslocar entre o priso atual e o piso final
     */
    public MainMovimentoElevador(MonitorElevador monitor, int num) {
        this.monitor = monitor;
        this.numOfFloors = num;
    }

    /**
     * <b>Método responsável pelo funcionamento da thread.</b>
     * <p>
     * DEVELOPER NOTE: estão espalhados ao longo do código vários comentários
     * importantes, no entanto alguns vão ser colocados aqui por conveniência.
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
        String threadName = "[Thread_RunningElevator]";
        Thread.currentThread().setName(threadName);
        //variável para o cálculo do tempo de execução (log)
        double tempoInicial = System.currentTimeMillis();
        int pisoInicial = monitor.getPisoAtual();

        try {
            //sinalização sobre a chegada ao destino
            this.monitor.setFloorReachedFlag(false);
            //espera pelo motor
            monitor.espera();

            /* ( DEPRECATED ) NOTA: Secalhar era melhor fazer o escalonamento de
             * pisos. Do tipo, para escolher qual o próximo piso a se deslocar.
             * Para isso faz-se outro algoritmo de escalonamento (por agora está
             * o FIFO). 
             */
            for (int i = 0; i < this.numOfFloors; i++) {
                for (int j = 0; j <= this.FlOOR_WAIT_TIME_MS; j++) {
                    Thread.sleep(monitor.MOVEMENT_WAITING_TIME);
                    
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

            //remover o piso do escalonamento
            if (!monitor.getFloorQueue().isEmpty()) {
                monitor.removeFloorReached();
            }
            monitor.displayQueue();
            monitor.displayPisoAtual();

            //LOG
            monitor.counterExecucao();
            monitor.counterPesoTotal();
            monitor.writeLog(Thread.currentThread(), pisoInicial, monitor.getPisoAtual(),
                    monitor.getDirecaoMotor(), monitor.getCargaAtual(), 
                    System.currentTimeMillis() - tempoInicial);
            //sinalização sobre a chegada ao destino
            this.monitor.setFloorReachedFlag(true);
        } catch (InterruptedException ex) {
            System.out.println("\n" + threadName + "\nDeslocacaoo parada "
                    + "abruptamente!!");
        }
    }

}
