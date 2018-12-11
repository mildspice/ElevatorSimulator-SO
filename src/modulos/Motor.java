package modulos;

import enums.DirecaoMotor;
import java.util.concurrent.Semaphore;

public class Motor implements Runnable {

    private DirecaoMotor estado;
    protected Main elevador;
    Semaphore semaforoMotor;
    private int currentfloor;
    private int floordistance;
    private boolean ascendente;

    /**
     * Construtor para a thread sem pre-introdução da direção.
     *
     * @param semaforoMotor semaforo relacionado ao funcionamento do elevador.
     */
    public Motor(Semaphore semaforoMotor) {
        this.semaforoMotor = semaforoMotor;
    }

    /**
     * Construtor para a thread.
     *
     * @param semaforoMotor semaforo relacionado ao funcionamento do elevador.
     * @param estado enum constant referente à direção
     */
    public Motor(Semaphore semaforoMotor, DirecaoMotor estado) {
        this.semaforoMotor = semaforoMotor;
        this.estado = estado;
    }

    @Override
    public void run() {
        //Quando o main lança a thread do motor, este fica à espera de um sinal
        //(através do uso do semáforo) para que comece a funcionar.
        try {
            //repara que só fazemos um acquire(), nao se faz um release() aqui
            //para que na proxima iteração o semaforo esteja com 0 "estafetas" outravez
            this.semaforoMotor.acquire();
            elevador.setEmFuncionamento(true);

            //A função main vai alterar o valor do funcionamento para quebrar este ciclo
            while (elevador.isEmFuncionamento()) {
                Thread.sleep(1000);
                switch (this.getEstado()) {
                    case BAIXO:
                        System.out.println(this.getEstado().message());
                        break;
                    case CIMA:
                        System.out.println(this.getEstado().message());
                        break;
                    default:
                        //não sabia o que por aqui, até pode nem se por nada.
                        System.out.println("* Erro na direção de deslocamento!! *");
                        elevador.setEmFuncionamento(false);
                        break;
                }
            }

        } catch (InterruptedException ex) {
            //esta exceção pode estar diretamente relacionada ao botao S!!
            System.out.println("\t* Motor Interrompido! *\n");
        }
    }

    /**
     * Altera a direção do motor para se deslocar para cima ou para baixo.
     *
     * @param estado enum constant referente à direção
     */
    private void setEstado(DirecaoMotor estado) {
        this.estado = estado;
    }

    /**
     * Retorna o estado direcional do motor.
     *
     * @return enum constant sobre o estado atual do motor
     */
    public DirecaoMotor getEstado() {
        return this.estado;
    }

    public int getFloordistance(){ return this.floordistance; }

    public void setFloordistance(int currentfloor, String nextfloor){
        Botoneira botoneira = new Botoneira(nextfloor);
        if(botoneira.Outvalue()==1) {
          if(botoneira.getValuebtn()>currentfloor) {
              this.floordistance = botoneira.getValuebtn()-currentfloor;
              setAscendente(true);
          }else if(botoneira.getValuebtn()<currentfloor){
              this.floordistance= currentfloor-botoneira.getValuebtn();
              setAscendente(false);
          }
        }
    }

    public boolean isAscendente() {
        return ascendente;
    }

    public void setAscendente(boolean ascendente) {
        this.ascendente = ascendente;
    }
}
