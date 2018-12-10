package modulos;

import enums.DirecaoMotor;
import enums.EstadoPortas;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import tools.MonitorElevador;
import tools.ElevatorRunTime;

public class Main implements Runnable {

    //variáveis da classe (elevador)
    //os pisos estão mais ligados à botoneira.
    //a informação sobre os pisos está no MonitorElevador porque é/pode ser usada
    //por várias threads (este é o tipo de coisas que temos que dizer no relatório
    //e na defesa do projeto :V)
    //não sei se é melhor fazer as portas de outra maneira ...
    protected Portas estadoPortas;
    //Objeto principal com as flags e os waits e notifies
    protected MonitorElevador monitor;
    //Objeto / Thread relacionado ao motor...
    protected Motor motor;

    //semaforo relacionado ao funcionamento do motor
    protected Semaphore semaforoMotor;

    public Main(Semaphore semaforoMotor, MonitorElevador monitor, Motor motor, Portas portas) {
        this.semaforoMotor = semaforoMotor;
        this.motor = motor;

        this.monitor = monitor;
        this.estadoPortas = portas;
    }

    @Override
    public void run() {
        //exemplo funcional do motor (sem a botoneira ...)
        //(os system.out são só para debug)
        int test = 0;
        //eu coloquei o motor como estando sempre ativo, assim não é preciso estar
        //sempre a criar threads diferentes em cada utilização do motor
        //depois para para-lo basta fazer 'motor.interrupt()' uma vez que no 'run'
        //está um ciclo while(!Thread.interrupted())'
        motor.start();
        while (test < 2) {
            //isto é só um exemplo de introdução dos dados na botoneira ...
            DirecaoMotor[] direcao
                    = new DirecaoMotor[]{DirecaoMotor.CIMA, DirecaoMotor.BAIXO};

            //define a direcao do movimento do elevador
            motor.setEstado(direcao[test]);

            System.out.println("Portas: " + estadoPortas.getEstado().toString());
            //verificar as portas (depois vai-se verificar outras coisas ...)
            if (estadoPortas.getEstado() == EstadoPortas.ABERTO) {
                //fecha as portas
                estadoPortas.setFechado();
            }
            System.out.println("Portas: " + estadoPortas.getEstado().toString());

            //da sinal ao motor para começar a trabalhar...
            semaforoMotor.release();
            System.out.println("SemaforoMotor_Permicoes_release: "
                    + this.semaforoMotor.availablePermits());

            //cria a thread relacionada ao tempo de andamento do elevador...
            ElevatorRunTime workingElevator = new ElevatorRunTime(this.monitor, this.motor, 3);
            workingElevator.start();
            //fica à espera do sinal da thread do andamento do elevador, ou seja
            //sinaliza quando o elevador chegar ao destino
            this.monitor.espera();
            //pára o elevador...
            this.monitor.setFlagFuncionamento(false);
            //abre as portas
            this.estadoPortas.setAberto();
            System.out.println("Portas: " + estadoPortas.getEstado().toString());

            try {
                //...
                Thread.sleep(1000);
                System.out.println("\n<!-- Next passenger, please... -->\n");
                test++;
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //desliga o motor...
        motor.setShowInterruptedMessage(false);
        motor.interrupt();
    }

    public static void main(String[] args) {
        //Isto é só para exemplificar ... é capaz de ser melhor usar mais alguns semaforos
        Semaphore semaforoMotor = new Semaphore(0);

        //sharedobjects e threads secundárias
        MonitorElevador monitor = new MonitorElevador();
        Portas portas = new Portas();
        //motor extends Thread (e assim já nao é preciso fazer uma instância de 'Thread'
        Motor motor = new Motor(semaforoMotor, monitor);
        motor.setName("[Thread_MotorElevador]");

        //thread principal
        Thread controloElevador = new Thread(
                new Main(semaforoMotor, monitor, motor, portas), "[Thread_ControloElevador]");
        controloElevador.start();

        try {
            controloElevador.join();
            System.out.println("\t* Elevador Desativado! *");
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
