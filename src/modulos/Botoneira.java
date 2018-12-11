package modulos;

public class Botoneira implements Runnable {
    //não precisa de ser enum ...

    /* protected int currentFloor;
     protected Semaphore sem;
     private int nextfloor; */
    private int valuebtn;
    private String actionbtn;
    private String input;

    public int getValuebtn() {
        return valuebtn;
    }

    public void setValuebtn(int valuebtn) {
        this.valuebtn = valuebtn;
    }

    public String getActionbtn() {
        return actionbtn;
    }

    public void setActionbtn(String actionbtn) {
        this.actionbtn = actionbtn;
    }

    private enum Botoes {
        PISO1, PISO2, PISO3, PISO4, A, F, S, K;

        public String outputButoes() {

            switch (this) {
                case PISO1:
                    return "1";
                case PISO2:
                    return "2";
                case PISO3:
                    return "3";
                case PISO4:
                    return "4";
                case A:
                    return "A";
                case F:
                    return "F";
                case S:
                    return "S";
                case K:
                    return "K";
                default:
                    return null;
            }

        }

    }

    ;

    //se 1, definido o próximo andar. se 0, guarda operação a executar. se -1, informa da falha;

    public int Outvalue() {

        Botoes btn;
        boolean check = false;

        try {
            btn = Botoes.valueOf(getInput());
            check = true;
         } catch (IllegalArgumentException e) {
            System.err.println("ERRO; NAHSEI SE EXISTE OPCAO");
        }finally {
            if (check) {
                btn = Botoes.valueOf(getInput());
                try {
                    setValuebtn(Integer.parseInt(btn.outputButoes()));
                    return 1;
                } catch (NumberFormatException e) {
                    setActionbtn(btn.outputButoes());
                    return 0;
                }
            }
            return -1;
        }
    }

   /* Construtor descartada. Ver depois o que fazer;
   public Botoneira(int currentfloor, Semaphore sem){

        this.currentFloor=currentfloor;
        this.sem=sem;

    } */

    public Botoneira() {

    }

    public Botoneira(String input) {
        this.input = input;
    }

    public String getInput() {
        return this.input;
    }

    @Override
    public void run() {


    }
}
