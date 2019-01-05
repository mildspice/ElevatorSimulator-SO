package modulos;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Semaphore;
import javax.swing.JFrame;
import enums.EstadosMotor;
import java.io.IOException;
import javax.swing.JOptionPane;
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

    private JFrame janelaPrincipal;
    //Objeto partilhado com as flags, 'waits' e 'notifies', ...
    protected MonitorElevador monitor;
    //instâncias das threads
    protected Motor motor;
    protected Portas portas;
    protected Botoneira botoneira;

    //semaforos relacionados ao funcionamento das threads
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
     * Método acoplado que cria uma janela que disponibiliza o input de um
     * inteiro relacionado à carga presente atualmente no elevador.
     */
    private int getInputCarga() {
        String input = JOptionPane.showInputDialog(
                "Peso presente no elevador atualmente: ", "0");
        try {
            return Integer.parseUnsignedInt(input);
        } catch (NumberFormatException exc) {
            JOptionPane.showMessageDialog(null,
                    "Devera introduzir um numero inteiro positivo!",
                    "Parsing Error", JOptionPane.ERROR_MESSAGE);
            return getInputCarga();
        }
    }

    /**
     * <b>Método responsável pelo funcionamento da thread.</b>
     * <p>
     * DEVELOPER NOTE: estão espalhados ao longo do código vários comentários
     * importantes, no entanto alguns vão ser colocados aqui por conveniência.
     * </p>
     * <p>
     * - as threads estão colocadas como estando sempre ativas, assim não é
     * preciso estar sempre a, por exemplo, criar threads diferentes em cada
     * utilização do motor. Para parar as threads basta fazer
     * 'instância_thread.interrupt()' uma vez que no 'run' está um ciclo
     * 'while(!Thread.interrupted())'. (isto é óbvio mas ajuda estar
     * documentado)
     * </p>
     */
    @Override
    public void run() {
        try {
            this.janelaPrincipal = monitor.criarJanelaPrincipal();

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
                            monitor.printWarning("Ja se encontra no piso selecionado!", true);
                            monitor.removeFloorReached();
                            monitor.displayQueue();
                        } else {
                            if (monitor.getPisoAtual() > piso) {
                                pisosADeslocar = monitor.getPisoAtual() - piso;
                                monitor.setDirecaoMotor(EstadosMotor.BAIXO);
                            } else {
                                pisosADeslocar = piso - monitor.getPisoAtual();
                                monitor.setDirecaoMotor(EstadosMotor.CIMA);
                            }

                            //questiona sobre a carga atual e verifica se pode prosseguir
                            while (!monitor.setCargaAtual(getInputCarga())) {
                                monitor.printWarning("Peso dentro do elevador "
                                        + "acima do limite!!", true);
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
                            //esperar pelo motor
                            monitor.espera();
                            Thread.sleep(monitor.MOVEMENT_WAITING_TIME / 2);
                            //sinalizar as portas novamente
                            semaforoPortas.release();
                            Thread.sleep(monitor.MOVEMENT_WAITING_TIME);
                        }
                    }
                }

            }
            /* NOTA:
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
        //semáforo de gestão de áreas críticas (como a escrita dos logs)
        Semaphore exclusaoMutua = new Semaphore(1);
        //semáforo para sinalização do funcionamento do motor 
        //( release em [Thread_ControloElevador] )
        Semaphore semaforoMotor = new Semaphore(0);
        //semáforo de sinalização do funcionamento das portas
        //( release em [Thread_ControloElevador] e [Thread_Botoneira] )
        Semaphore semaforoPortas = new Semaphore(0);
        //semáforo que sinaliza o início do funcionamento do elevador
        //( release realizada pelo uso dos botões em [Thread_Botoneira] )
        Semaphore semaforoBotoneira = new Semaphore(0);

        try {
            //objeto partilhado entre todas as threads
            MonitorElevador monitor = new MonitorElevador(exclusaoMutua);
            Thread[] threads = new Thread[3];
            //threads ...
            Portas portas = new Portas(semaforoPortas, monitor);
            portas.setName("[Thread_PortasElevador]");
            threads[0] = portas;
            Motor motor = new Motor(semaforoMotor, monitor);
            motor.setName("[Thread_MotorElevador]");
            threads[1] = motor;
            Botoneira botoneira = new Botoneira(semaforoBotoneira, monitor, semaforoPortas);
            botoneira.setName("[Thread_Botoneira]");
            threads[2] = botoneira;

            //thread principal
            Thread controloElevador = new Thread(
                    new MainControloElevador(semaforoMotor, semaforoPortas, semaforoBotoneira,
                            monitor, motor, portas, botoneira), "[Thread_ControloElevador]");
            controloElevador.start();

            //variável para os logs
            double startTime = System.currentTimeMillis();

            try {
                controloElevador.join();
                for (Thread th : threads) {
                    th.join();
                }

                //variável para os logs
                double endTime = System.currentTimeMillis();
                monitor.reportGeneration(endTime - startTime);
                System.out.println("\n\t* Elevador Desativado! *");

                /* interromper todas as threads que não tenham sido tratadas.
                (ex.: a thread [Thread_RunningElevator] pode não ser corretamente
                finalizada se o programa fôr parado de forma abrupta (enquanto o 
                elevador estiver a funcionar [funcionar implica estar a decorrer
                a deslocação entre dois pisos])*/
                Thread[] tarray = new Thread[Thread.activeCount()];
                Thread.enumerate(tarray);
                for (Thread th : tarray) {
                    th.interrupt();
                }
            } catch (InterruptedException ex) {
            }
        } catch (IOException ex) {
            System.err.println("Erro na leitura do ficheiro de configurações!!");
            Logger.getLogger(MainControloElevador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
