package modulos;

import enums.DirecaoMotor;
import java.util.concurrent.Semaphore;

public class Main implements Runnable {

    //variáveis da classe (elevador)
    //Ver como vamos fazer com os pisos ...
    private boolean emFuncionamento;

    //variaveis para as threads e funcionamento
    Semaphore mutualExclusion;
    Semaphore semaforoMotor;
    Semaphore semaforoPortas;

    public Main(Semaphore mutex, Semaphore semaforoMotor, Semaphore semaforoPortas) {
        this.mutualExclusion = mutex;
        this.semaforoMotor = semaforoMotor;
        this.semaforoPortas = semaforoPortas;
    }

    /**
     * Altera o estado funcional do elevador.
     *
     * @param is booleano para o andamento do elevador
     */
    public void setEmFuncionamento(boolean is) {
        this.emFuncionamento = is;
    }

    /**
     * Retorna o estado funcional do elevador.
     *
     * @return retorna se o elevador está ou não em funcionamento
     */
    public boolean isEmFuncionamento() {
        return this.emFuncionamento;
    }

    @Override
    public void run() {
        //exemplo ....
        Thread threadMotor = new Thread(new Motor(semaforoMotor, DirecaoMotor.BAIXO));
        //depois de validar algumas coisas (portas por exemplo) 
        //fazer semaforoMotor.realease() , para dar o sinal para o motor funcionar
        
        /*
        Todo o conjunto de operações do elevador ...
        (validar condições para ativar o motor, por exemplo)
        Criar e iniciar as outras threads ...
        
        secalhar faz-se tudo isto dentro de um ciclo while que finaliza com
        algum input durante a execução da simulação do elevador ...
        OU, faz while(true) e para acabar faz-se ctrl+c :V
         */
    }

    public static void main(String[] args) {
        //Isto é só para exemplificar ...
        Semaphore mutualExclusion = new Semaphore(1);
        Semaphore semaforoMotor = new Semaphore(0);
        Semaphore semaforoPortas = new Semaphore(0);

        Thread controloElevador = new Thread(new Main(
                mutualExclusion, semaforoMotor, semaforoPortas));
    }

}
