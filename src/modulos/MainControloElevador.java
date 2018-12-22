package modulos;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Semaphore;
import javax.swing.JFrame;
import enums.EstadosMotor;
import tools.MonitorElevador;

/**
 * <b>Módulo Principal - thread que vai simular todas as operações de controlo
 * do elevador.</b>
 * São iniciadas e devidamente terminadas todas as outras threads relativas aos
 * sub-modulos. (mais coisas para escrever aqui no enunciado ...)
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

            /* exemplo de utilização */
            int i = 1;
            while (!Thread.interrupted()) {
                EstadosMotor[] direcao = {EstadosMotor.CIMA, EstadosMotor.BAIXO};

                monitor.setDirecaoMotor(direcao[i]);
                MainMovimentoElevador workingElevator
                        = new MainMovimentoElevador(this.monitor, 3);
                workingElevator.start();

                semaforoMotor.release();
                semaforoPortas.release();

                workingElevator.join();

                if (i == 0) {
                    i++;
                } else {
                    i--;
                }
                Thread.sleep(1000);
            }

            //a thread só acaba quando for interrompida
            //logo a ultima parte do codigo faz-se no catch
            //no entanto, fica aqui tambem em caso de haver problemas ...
            System.out.println();
            System.out.println("\t* Interrompendo as threads *\n\t\t...");
            motor.interrupt();
            portas.interrupt();
            botoneira.interrupt();
            this.janelaPrincipal.dispose();

        } catch (IllegalThreadStateException ex) {
            Thread.currentThread().interrupt();
            Logger.getLogger(MainControloElevador.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);

        } catch (InterruptedException ex) {
            System.out.println();
            System.out.println("\t* Interrompendo as threads *\n\t\t...\n");
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
        Semaphore semaforoPortas = new Semaphore(1);
        Semaphore semaforoBotoneira = new Semaphore(0);

        //sharedobject e threads secundárias
        MonitorElevador monitor = new MonitorElevador(exclusaoMutua);
        Portas portas = new Portas(semaforoPortas, monitor);
        portas.setName("[Thread_PortasElevador]");
        Motor motor = new Motor(semaforoMotor, monitor);
        motor.setName("[Thread_MotorElevador]");
        Botoneira botoneira = new Botoneira(semaforoBotoneira, monitor);
        motor.setName("[Thread_Botoneira]");

        //thread principal
        Thread controloElevador = new Thread(
                new MainControloElevador(semaforoMotor, semaforoPortas, semaforoBotoneira,
                        monitor, motor, portas, botoneira), "[Thread_ControloElevador]");
        controloElevador.start();

        try {
            controloElevador.join();
            System.out.println("\t* Elevador Desativado! *");
        } catch (InterruptedException ex) {
            Logger.getLogger(MainControloElevador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
