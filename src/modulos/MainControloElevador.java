package modulos;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Semaphore;
import javax.swing.JFrame;
import enums.EstadosMotor;
import java.io.IOException;
import tools.MonitorElevador;

/**
 * <h1>Módulo Principal</h1>
 * <b>- thread que vai simular todas as operações de controlo do elevador.</b>
 * <p>
 * <b>São iniciadas e devidamente terminadas todas as outras threads relativas
 * aos sub-modulos. Esta thread é também responsável pelo processamento e
 * decisões principais decorrentes do funcionamento do elevador.</b>
 * </p>
 *
 * @author Grupo21-8170212_8170282_8170283
 */
public class MainControloElevador implements Runnable {

    /*
     * NOTA (antiga, mas serve como 'memo'):
     * os pisos estão mais ligados à botoneira.
     * A informação sobre os pisos está no MonitorElevador porque
     * é/pode ser usada por várias threads (este é o tipo de coisas que temos
     * que dizer no relatório e na defesa do projeto :V)
     */
    //Objeto partilhado com as flags, 'waits' e 'notifies', ...
    protected MonitorElevador monitor;
    private JFrame janelaPrincipal;
    //threads
    protected Motor motor;
    protected Portas portas;
    protected Botoneira botoneira;

    //semaforos relacionados às threads
    protected Semaphore semaforoMotor;
    protected Semaphore semaforoPortas;
    protected Semaphore semaforoBotoneira;

    /**
     * Construtor para a thread.
     *
     * @param semaforoMotor semaforo relacionado ao funcionamento do elevador.
     * @param semaforoPortas semaforo relacionado ao funcionamento das portas.
     * @param semaforoBotoneira semaforo relacionado a funções especiais da
     * botoneira
     * @param monitor objeto partilhado
     * @param motor instância da thread relativa ao motor
     * @param portas instância da thread relativa às portas
     * @param botoneira instância da thread relativa à botoneira
     */
    public MainControloElevador(
            Semaphore semaforoMotor, Semaphore semaforoPortas, Semaphore semaforoBotoneira,
            MonitorElevador monitor, Motor motor, Portas portas, Botoneira botoneira) {
        this.semaforoMotor = semaforoMotor;
        this.semaforoPortas = semaforoPortas;
        this.semaforoBotoneira = semaforoBotoneira;

        this.motor = motor;
        this.portas = portas;
        this.botoneira = botoneira;
        this.monitor = monitor;
    }

    /**
     * <b>Método responsável pelo funcionamento da thread.</b>
     * <p>
     * NOTAS (estão espalhados ao longo do código vários comentários
     * importantes, no entanto alguns vão ser colocados aqui por conveniência do
     * javadoc.):
     * </p>
     * <p>
     * - as threads estão colocadas como estando sempre ativas, assim não é
     * preciso estar sempre a, por exemplo, criar threads diferentes em cada
     * utilização do motor. Para parar as threds basta fazer
     * 'instância_thread.interrupt()' uma vez que no 'run' está um ciclo
     * while(!Thread.interrupted())'.
     * </p>
     */
    @Override
    public void run() {
        try {
            this.janelaPrincipal = monitor.criarJanelaPrincipal();
            /**
             * as threads estão colocadas como estando sempre ativas, assim não
             * é preciso estar sempre a, por exemplo, criar threads diferentes
             * em cada utilização do motor. Para parar as threds basta fazer
             * 'instância_thread.interrupt()' uma vez que no 'run' está um ciclo
             * while(!Thread.interrupted())'
             */
            motor.start();
            portas.start();
            botoneira.start();

            while (!Thread.interrupted()) {
                /*
                fica à espera do input na botoneira
                (a permição é dada quando o elevador se encontra em estado IDLE
                uma vez que depois de avançar para além deste semáforo vai entrar
                dentro de um ciclo que itera enquanto existirem pisos em fila.)
                 */
                semaforoBotoneira.acquire();
                /*
                tanto os botoes dos pisos como a chave fazem release de permits
                de funcionamento (para evitar problemas na falta de permits, pois
                levaria a ter que se reiniciar completamente o processo).
                Então, logo após a thread começar a funcionar drena os permits todos.
                 */
                semaforoBotoneira.drainPermits();

                if (monitor.isChaveAcionada()) {
                    monitor.printWarning("Chave Acionada!\n"
                            + "Deslocacao do elevador impedida.", true);
                } else {
                    while (!monitor.getFloorQueue().isEmpty()) {
                        //calcular a distância e sentido
                        int piso = 0, pisosADeslocar;
                        while (!monitor.getFloorQueue().get(0).equals(monitor.getBotoesPisos()[piso])) {
                            piso++;
                        }
                        piso++; //piso = indice array + 1;
                        if (monitor.getPisoAtual() == piso) {
                            monitor.printWarning("Ja se encontra no piso!", true);
                            monitor.removeFloorReached();
                        } else {
                            if (monitor.getPisoAtual() > piso) {
                                pisosADeslocar = monitor.getPisoAtual() - piso;
                                monitor.setDirecaoMotor(EstadosMotor.BAIXO);
                            } else {
                                pisosADeslocar = piso - monitor.getPisoAtual();
                                monitor.setDirecaoMotor(EstadosMotor.CIMA);
                            }

                            //iniciar o movimento
                            MainMovimentoElevador workingElevator
                                    = new MainMovimentoElevador(monitor, pisosADeslocar);
                            workingElevator.start();
                            //sinalizar o motor
                            semaforoMotor.release();
                            //espera pela "preparação" do motor
                            monitor.espera();
                            //sinalizar as portas
                            semaforoPortas.release();
                            //esperar pela chegada ao destino
                            workingElevator.join();
                            //(esperar pelo código)
                            while (monitor.isEmFuncionamento()) {
                            }
                            Thread.sleep(monitor.MOVEMENT_WAITING_TIME);
                            //sinalizar as portas novamente
                            semaforoPortas.release();
                        }
                    }
                }

            }
            /*
            A thread só acaba quando for interrompida.
            No entanto, o código tanto pode finalizar na catch clause como depois
            do ciclo while (aqui), dependendo do estado "funcional" da thread quando
            é interrompida (ex.: Se a thread estiver "à espera num semáforo", ativa
            a exception; se estiver a realizar alguma operação, vai simplesmente
            continuar até que saia do ciclo, ou chegue a alguma operação que invoque
            a exceção). Por isso, o código de interromper as outras
            thread está repetido. O mesmo acontece nas outras threads ...
            */
            System.out.println();
            System.out.println("\t* Interrompendo as threads *\n\t\t...");
            monitor.printWarning("GoOdByE!", false);
            Thread.sleep(1000 * 3);
            motor.interrupt();
            portas.interrupt();
            botoneira.interrupt();
            this.janelaPrincipal.dispose();
            
        } catch (IllegalThreadStateException ex) {
            /*
            esta catch clause acontece quando é feito "<thread>.start()" numa
            thread que já se encontra a funcionar. (isto aconteceu-me bastante,
            na minha inocente distração, equanto fazia algum código...)
            Já não serve de nada, mas fica aqui na mesma.
            */
            Thread.currentThread().interrupt();
            Logger.getLogger(MainControloElevador.class.getName()).log(Level.SEVERE, null, ex);
            
        } catch (InterruptedException ex) {
            System.out.println();
            System.out.println("\t* Interrompendo as threads *\n\t\t...\n");
            monitor.printWarning("GoOdByE!", false);
            motor.interrupt();
            portas.interrupt();
            botoneira.interrupt();
            this.janelaPrincipal.dispose();
        }
    }

    public static void main(String[] args) {
        /*
        Este semáforo vai servir para quando fizermos operações de escrever 
        os logs no ficheiro e assim.
        Um exemplo: O módulo Main é constituido por várias classes (diferentes threads).
          Imaginemos, temos uma thread relacionada ao movimento do elevador
              (já temos essa thread, "MainMovimentoElevador") e outra que por alguma razão
              analisa quantas vezes as portas abriram e fecharam.
          Basicamente, faríamos um método para escrever no ficheiro (provavelmente
              na classe "MonitorElevador") e no código de cada thread, cada vez que
              esse método fosse chamado ficaria do tipo:
                ...
                exclusaoMutua.acquire();
                escreverFicheiro();
                exclusaoMutua.release();
        Pronto.
        Isto no fundo serve para evitar que o código se faça simultaneamente.
         */
        Semaphore exclusaoMutua = new Semaphore(1);
        /**
         * semaforos relacionados ao funcionamento dos sub-modulos
         */
        //0 permições iniciais para correr só quando "mandado"
        Semaphore semaforoMotor = new Semaphore(0);
        //1 permição inicial para verificar as portas logo na primeira iteração
        Semaphore semaforoPortas = new Semaphore(0);
        Semaphore semaforoBotoneira = new Semaphore(0);

        try {
            //objeto partilhado entre todas as threads
            MonitorElevador monitor = new MonitorElevador(exclusaoMutua);

            //threads ...
            Portas portas = new Portas(semaforoPortas, monitor);
            portas.setName("[Thread_PortasElevador]");
            Motor motor = new Motor(semaforoMotor, monitor);
            motor.setName("[Thread_MotorElevador]");
            Botoneira botoneira = new Botoneira(semaforoBotoneira, monitor, semaforoPortas);
            motor.setName("[Thread_Botoneira]");

            //thread principal
            Thread controloElevador = new Thread(
                    new MainControloElevador(semaforoMotor, semaforoPortas, semaforoBotoneira,
                            monitor, motor, portas, botoneira), "[Thread_ControloElevador]");
            controloElevador.start();

            try {
                controloElevador.join();

                System.out.println("\n\t* Elevador Desativado! *");
            } catch (InterruptedException ex) {
            }
        } catch (IOException ex) {
            System.err.println("Erro na leitura do ficheiro de configurações!!");
            Logger.getLogger(MainControloElevador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
