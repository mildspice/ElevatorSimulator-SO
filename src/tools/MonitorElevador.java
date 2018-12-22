package tools;

import enums.EstadosMotor;
import enums.EstadosPortas;
import javax.swing.JFrame;
import java.io.*;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * <b>Shared Object</b>
 * Objeto partilhado entre todas as threads que contém todas as variáveis e
 * métodos convenientes.
 */
public class MonitorElevador {

    //tempo de espera entre as execuções
    public final int MOVEMENT_WAITING_TIME = 1000;
    //public final int DOOR_EXECUTE_TIME = 1000;

    //variáveis gerais do funcionamento do elevador
    private boolean flagFloorReached = true;
    private boolean flagFuncionamento = false;
    public final int NUM_PISOS;
    private int pisoAtual = 0;
    protected Semaphore exclusaoMutua;

    //variáveis relacionadas ao estado do motor e das portas
    private EstadosMotor direcaoMotor;
    private EstadosPortas estadoPortas = EstadosPortas.ABERTO;

    //variáveis do log
    private double tempoExec;

    //variâveis relacionadas ao funcionamento da Botoneira
    private final String[] botoesPisos;
    private final String[] botoesPortas = {"A", "F"};
    private boolean chave = false;
    private String input = "";

    public MonitorElevador(Semaphore exclusaoMutua) {

        setDefinitions();
        this.exclusaoMutua = exclusaoMutua;
    }

    public synchronized void acorda() {
        this.notify();
    }

    public synchronized void acordaTodas() {
        this.notifyAll();
    }

    public synchronized void espera() {
        try {
            while (!this.flagFuncionamento) {
                this.wait();
            }

        } catch (InterruptedException ie) {
        }
    }

    /**
     * Atualiza a posição do elevador.
     *
     * @param piso numero do piso onde se encontra o elevador
     */
    public synchronized void setPisoAtual(int piso) {
        this.pisoAtual = piso;
    }

    /**
     * Retorna a posição atual do elevador
     *
     * @return numero do piso onde se encontra atualmente o elevador
     */
    public synchronized int getPisoAtual() {
        return this.pisoAtual;
    }

    public int getNUM_PISOS() {
        return NUM_PISOS;
    }

    /**
     * Retorna a identificação dos botões referentes à botoneira do elevador
     *
     * @return array de String com o nome dos botões dos pisos
     */
    public synchronized String[] getBotoesPisos() {
        return this.botoesPisos;
    }

    /**
     * Retorna a identificação dos botões referentes à botoneira do elevador
     *
     * @return array de String com o nome dos botões relativos ao estado das
     * portas
     */
    public synchronized String[] getBotoesPortas() {
        return this.botoesPortas;
    }

    /**
     * (Este método só vai ser utilizado pelo botão do JFrame)
     *
     * @param state estado boleano da chave
     */
    public synchronized void setChave(boolean state) {
        this.chave = state;
    }

    /**
     * Retorna o estado da chave.
     *
     * @return estado boleano da chave
     */
    public synchronized boolean isChaveAcionada() {
        return this.chave;
    }

    /**
     * Guarda o input feito pela botoneira. (Método utilizado só pelo JFrame)
     *
     * @param input string com o input
     */
    public synchronized void setInput(String input) {
        this.input = input;
    }

    /**
     * Retorna o input feito pelo utilizador (na botoneira)
     *
     * @return string com o input
     */
    public synchronized String getInput() {
        return this.input;
    }

    /**
     * Altera a direção do motor para se deslocar para cima ou para baixo.
     *
     * @param estado enum constant referente à direção
     */
    public synchronized void setDirecaoMotor(EstadosMotor estado) {
        this.direcaoMotor = estado;
    }

    /**
     * Retorna o estado direcional do motor.
     *
     * @return enum constant sobre o estado atual do motor
     */
    public synchronized EstadosMotor getDirecaoMotor() {
        return this.direcaoMotor;
    }

    /**
     * Retorna o estado atual das portas.
     *
     * @param estado enum constant referente ao estado das portas
     */
    public synchronized void setEstadoPortas(EstadosPortas estado) {
        this.estadoPortas = estado;
    }

    /**
     * Retorna o estado atual das portas.
     *
     * @return enum constant sobre o estado das portas
     */
    public synchronized EstadosPortas getEstadoPortas() {
        return this.estadoPortas;
    }

    /**
     * Altera a sinalização para se o elevador se encontra no piso em questão
     *
     * @param flag
     */
    public synchronized void setFloorReachedFlag(boolean flag) {
        this.flagFloorReached = flag;
    }

    /**
     * Retorna a sinalização sobre se o elevador se encontra no piso em questão
     *
     * @return retorna se o elevador se encontra num piso ou não
     */
    public synchronized boolean isFloorReached() {
        return this.flagFloorReached;
    }

    /**
     * Altera o estado funcional do elevador.
     *
     * @param flag booleano para o andamento do elevador
     */
    public synchronized void setFlagFuncionamento(boolean flag) {
        this.flagFuncionamento = flag;
    }

    /**
     * Retorna o estado funcional do elevador.
     *
     * @return retorna se o elevador está ou não em funcionamento
     */
    public synchronized boolean isEmFuncionamento() {
        return this.flagFuncionamento;
    }
    
    public JFrame criarJanelaPrincipal() {
        /* FAZER JANELA PRINCIPAL DO ELEVADOR */
        JFrame guiFrame = new JFrame();
        
        return guiFrame;
    }

    public double getTempoExec() {
        return tempoExec;
    }

    public void setTempoExec(double tempoExec) {
        this.tempoExec = tempoExec;
    }

    public synchronized void writeLog(Thread thread, int pisoInicial, int pisoFinal) {

        Logger logger = Logger.getLogger("ElevatorLog");
        FileHandler fh;

        try {

            fh = new FileHandler("ElevatorLog.log");
            logger.addHandler(fh);
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fh.setFormatter(simpleFormatter);
            logger.info(thread.getName() + " Piso Inicial: " + pisoInicial + " Piso Final: " + pisoFinal +
                    " Peso transportado: " + carga + " Tempo de trabalho: " + getTempoExec());

        } catch (IOException ex) {

        }

    }

    public synchronized void setDefinitions() {

        File deffile = new File("definicoes.properties");
        boolean checker = false;

        try {
            FileReader fr = new FileReader(deffile);
            checker = true;
        } catch (FileNotFoundException fio) {
            System.out.print("File not found");

        }

        if (checker == true) {
            Properties p = new Properties();
            try {
                p.load(new FileInputStream(deffile));
            } catch (IOException e) {
                e.printStackTrace();
            }
            NUM_PISOS = Integer.parseInt(p.getProperty("pisos"));
            this.botoesPisos = new String[NUM_PISOS];
            for (int i = 0; i < this.botoesPisos.length; i++) {
                this.botoesPisos[i] = "PISO" + i;
            }
            carga=Integer.parseInt(p.getProperty("carga"));
        }
      /*  try {
            FileReader fileReader = new FileReader(deffile);
            BufferedReader br = new BufferedReader(fileReader);
            String line;
            while((line=br.readLine())!=null){



            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } */


    }

}
