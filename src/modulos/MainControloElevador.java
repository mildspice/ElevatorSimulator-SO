package modulos;

import enums.DirecaoMotor;
import enums.EstadoPortas;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import tools.MonitorElevador;

public class MainControloElevador implements Runnable {

    //variáveis da classe (elevador)
    //os pisos estão mais ligados à botoneira.
    //a informação sobre os pisos está no MonitorElevador porque é/pode ser usada
    //por várias threads (este é o tipo de coisas que temos que dizer no relatório
    //e na defesa do projeto :V)
    //Objeto principal com as flags e os waits e notifies
    protected MonitorElevador monitor;
    //Objeto / Thread relacionado ao motor...
    protected Motor motor;
    protected Portas portas;

    //semaforo relacionado ao funcionamento do motor
    protected Semaphore semaforoMotor;
    protected Semaphore semaforoPortas;

    public MainControloElevador(Semaphore semaforoMotor, Semaphore semaforoPortas,
            MonitorElevador monitor, Motor motor, Portas portas) {
        this.semaforoMotor = semaforoMotor;
        this.semaforoPortas = semaforoPortas;

        this.motor = motor;
        this.portas = portas;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            //eu coloquei o motor como estando sempre ativo, assim não é preciso estar
            //sempre a criar threads diferentes em cada utilização do motor
            //depois para para-lo basta fazer 'motor.interrupt()' uma vez que no 'run'
            //está um ciclo while(!Thread.interrupted())' (o mesmo para as portas)
            motor.start();
            portas.start();
            //exemplo funcional do motor (sem a botoneira ...)
            //(os system.out são só para debug)
            int test = 0;
            while (test < 2) {
                //isto é só um exemplo de introdução dos dados na botoneira ...
                DirecaoMotor[] direcao
                        = new DirecaoMotor[]{DirecaoMotor.CIMA, DirecaoMotor.BAIXO};

                //define a direcao do movimento do elevador
                //isto depois é verificado aqui consoante o que é introduzido na botoneira
                monitor.setDirecaoMotor(direcao[test]);

                System.out.println("Portas: " + monitor.getEstadoPortas().toString());

                //cria a thread relacionada ao tempo de andamento do elevador...
                //aqui vai ser preciso fazer o calculo para quantos pisos o elevador
                //se vai deslocar ...
                MainMovimentoElevador workingElevator = new MainMovimentoElevador(this.monitor, this.motor, 3);
                workingElevator.start();
                //poe o elevador em andamento (sinaliza) (o while serve para esperar
                //que o código do "MainMovimentoElevador" corra para evitar confusao
                //e possível malfunction ...)
                while (this.monitor.isFloorReached()) {
                }
                this.monitor.setFlagFuncionamento(true);
                //da sinal ao motor para começar a trabalhar...
                semaforoMotor.release();
                System.out.println("SemaforoMotor_Permicoes_release: "
                        + this.semaforoMotor.availablePermits());
                //sinaliza as portas ...
                semaforoPortas.release();
                //para dar tempo para o codigo correr ...
                Thread.sleep(10);
                System.out.println("Portas: " + monitor.getEstadoPortas().toString());

                //fica à espera do sinal dado pela thread do andamento do elevador
                //na chegada ao piso em questão
                workingElevator.join();
                //chegou ao destino, sinaliza a paragem do elevador...
                this.monitor.setFlagFuncionamento(false);
                //sinaliza as portas ...
                semaforoPortas.release();
                //para dar tempo para o codigo correr ...
                Thread.sleep(10);
                System.out.println("Portas: " + monitor.getEstadoPortas().toString());

                //...
                Thread.sleep(1000);
                System.out.println("\n<!-- Next passenger, please... -->\n");
                test++;

            }

            //desliga o motor...
            motor.setShowInterruptedMessage(false);
            motor.interrupt();
            portas.interrupt();
        } catch (InterruptedException ex) {
            Logger.getLogger(MainControloElevador.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalThreadStateException ex) {
            Logger.getLogger(MainControloElevador.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        //Isto é só para exemplificar ... é capaz de ser melhor usar mais alguns semaforos
        Semaphore semaforoMotor = new Semaphore(0);
        Semaphore semaforoPortas = new Semaphore(1);

        //sharedobject e threads secundárias
        MonitorElevador monitor = new MonitorElevador();
        Portas portas = new Portas(semaforoPortas, monitor);
        portas.setName("[Thread_PortasElevador]");
        //motor extends Thread (e assim já nao é preciso fazer uma instância de 'Thread'
        Motor motor = new Motor(semaforoMotor, monitor);
        motor.setName("[Thread_MotorElevador]");

        //thread principal
        Thread controloElevador = new Thread(
                new MainControloElevador(semaforoMotor, semaforoPortas,
                        monitor, motor, portas), "[Thread_ControloElevador]");
        controloElevador.start();

        try {
            controloElevador.join();
            System.out.println("\t* Elevador Desativado! *");
        } catch (InterruptedException ex) {
            Logger.getLogger(MainControloElevador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
