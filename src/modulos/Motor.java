package modulos;

import java.util.concurrent.Semaphore;
import tools.MonitorElevador;

public class Motor extends Thread {

    private boolean showInterruptedMessage = true;
    protected MonitorElevador monitor;
    protected Semaphore semaforoMotor;

    /**
     * Construtor para a thread.
     *
     * @param semaforoMotor semaforo relacionado ao funcionamento do elevador.
     * @param monitor
     */
    public Motor(Semaphore semaforoMotor, MonitorElevador monitor) {
        this.semaforoMotor = semaforoMotor;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                //Quando o main lança a thread do motor, este fica à espera de um sinal
                //(através do uso do semáforo) para que comece a funcionar.

                //repara que só fazemos um acquire(), nao se faz um release() aqui
                //para que na proxima iteração o semaforo esteja com 0 "estafetas" outravez
                this.semaforoMotor.acquire();
                System.out.println("SemaforoMotor_Permicoes_DepoisAcquire: "
                        + this.semaforoMotor.availablePermits());
                System.out.println("DirecaoMotor: " + this.monitor.getDirecaoMotor().toString());

                //A função main vai alterar o valor do funcionamento para quebrar este ciclo
                while (this.monitor.isEmFuncionamento()) {
                    Thread.sleep(500);
                    switch (this.monitor.getDirecaoMotor()) {
                        case BAIXO:
                            System.out.println(this.monitor.getDirecaoMotor().message());
                            break;
                        case CIMA:
                            System.out.println(this.monitor.getDirecaoMotor().message());
                            break;
                        default:
                            //não sabia o que por aqui, até pode nem se por nada.
                            System.out.println("* Erro na direção de deslocamento!! *");
                            this.monitor.setFlagFuncionamento(false);
                            break;
                    }
                    Thread.sleep(500);
                }
            }
        } catch (InterruptedException ex) {
            //esta exceção pode estar diretamente relacionada ao botao S!!
            if (this.showInterruptedMessage == true) {
                System.out.println("\t* Motor Interrompido! *\n");
            }
        }
    }

    public void setShowInterruptedMessage(boolean flag) {
        this.showInterruptedMessage = flag;
    }
}
