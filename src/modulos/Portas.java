package modulos;

import enums.EstadoPortas;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import tools.MonitorElevador;

/**
 * <b>
 * Módulo "Portas".
 * </b>
 *
 * @author Grupo4
 * <p>
 * 8170212 </p>
 * <p>
 * 8170282 </p>
 * <p>
 * 8170283 </p>
 *
 * ISTO VAI SER PARA ALTERAR, NAO FAZ SENTIDO FICAR ASSIM.
 *
 * E AINDA NAO SEI SE DEVERIA FAZER UMA THREAD COM ISTO
 */
public class Portas extends Thread {

    protected MonitorElevador monitor;
    protected Semaphore semaforoPortas;

    public Portas(Semaphore semaforo, MonitorElevador monitor) {
        this.semaforoPortas = semaforo;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                //este semaforo serve para que o ciclo nao esteja a correr constantemente
                //e evita puxar muito pelo processador ...
                semaforoPortas.acquire();
                //depois colocar a validação relacionada ao botao de abrir as portas
                //na botoneira
                if (monitor.isFloorReached() && !monitor.isEmFuncionamento()) {
                    monitor.setEstadoPortas(EstadoPortas.ABERTO);

                } else {
                    monitor.setEstadoPortas(EstadoPortas.FECHADO);
                }

            }
        } catch (InterruptedException ex) {
        }
    }

}
