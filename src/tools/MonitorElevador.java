package tools;

import enums.EstadosMotor;
import enums.EstadosPortas;
import java.io.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * <h1>Divisão do módulo Main</h1>
 * <b>- Shared Object</b>
 * <p>
 * <b>Objeto a ser partilhado entre todas as threads que contém todas as
 * variáveis e métodos convenientes.</b>
 * </p>
 *
 * @author Grupo21-8170212_8170282_8170283
 */
public class MonitorElevador {

    //tempo de espera predefinido entre as execuções
    public final int MOVEMENT_WAITING_TIME = 1000;
    //variáveis da janela principal
    private JFrame janela;
    private JTextField displayFloor;
    private JTextArea displayFloorQueue;
    private JTextArea displayFloorPretty;
    private JTextArea displayAvisos;
    private JTextArea displayAvisosLast;

    //variáveis gerais do funcionamento do elevador
    protected Semaphore semaforoExclusaoMutua;
    private boolean flagFloorReached = true;
    private boolean flagFuncionamento = false;
    private int NUM_PISOS = 4;
    private int CARGA_KG;
    private int pisoAtual = 1;
    //variâveis relacionadas ao funcionamento da Botoneira
    /*
    secalhar há uma forma melhor de fazer isto mas assim também funciona:
    -> piso = indice do array + 1,
        ex. botoesPisos[2] = "PISO3", botoesPisos[0] = "PISO1"
     */
    private final String[] botoesPisos = new String[NUM_PISOS];
    //acabei por colocar as portas como tu (Rodrigo) tinhas inicialmente
    //(como boolean) true - A, false - F.
    private boolean doorButton;
    private boolean chave = false;
    //esta é a fila para a introdução dos pisos de destino. Para o escalonamento
    //vai ser usado o FIFO em princípio.
    private ArrayList<String> floorQueue = new ArrayList<>();
    //variáveis relacionadas ao estado do motor e das portas
    private EstadosMotor direcaoMotor;
    private EstadosPortas estadoPortas = EstadosPortas.ABERTO;

    //variáveis do log
    private double tempoExec;

    /**
     * Construtor do objeto partilhado.
     *
     * @param exclusaoMutua semaforo para o controlo da realização de operações
     * "sensíveis"
     */
    public MonitorElevador(Semaphore exclusaoMutua) {
        //isto está aqui porque estive a testar o programa
        //depois escreve em comentários como se escreve e onde se pôe o file properties
        for (int i = 0; i < this.botoesPisos.length; i++) {
            this.botoesPisos[i] = "PISO" + (i + 1);
        }
        //setDefinitions();
        this.semaforoExclusaoMutua = exclusaoMutua;
    }

    /**
     * Realiza um notify único para uma thread que esteja "adormecida".
     */
    public synchronized void acorda() {
        this.notify();
    }

    /**
     * "Acorda" todas as threads que estejam "adormecidas" no momento.
     */
    public synchronized void acordaTodas() {
        this.notifyAll();
    }

    /**
     * "Adormece" a thread que chama este método, ficando nesse estado até que
     * outra thread a "acorde"
     */
    public synchronized void espera() {
        try {
            while (!this.flagFuncionamento) {
                this.wait();
            }

        } catch (InterruptedException ie) {
        }
    }

    /**
     * Retorna o número total de pisos existentes
     *
     * @return numero total de pisos
     */
    public int getNumPisos() {
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
     * Seleciona o botão relativo ao estado das portas.
     *
     * @param botao boolean referente ao botão manual das portas (true - A,
     * false - F)
     */
    public synchronized void setBotaoPortas(boolean botao) {
        this.doorButton = botao;
    }

    /**
     * Retorna a identificação dos botões referentes à botoneira do elevador em
     * boolean. Aberto (botão 'A') - true, Fechado (botão 'F') - false.
     *
     * @return boolean referente ao botão das portas
     */
    public synchronized boolean getBotaoPortas() {
        return this.doorButton;
    }

    /**
     * Retorna o conjunto de pisos introduzidos pelo utilizador postos em fila.
     *
     * @return array list com a fila de todo os pisos introduzidos
     */
    public synchronized ArrayList<String> getFloorQueue() {
        return this.floorQueue;
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
     * @param flag boolean para a sinalização de chagada ao destino
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

    /**
     * Atualiza a posição do elevador.
     *
     * @param piso numero do piso onde se encontra o elevador
     */
    public synchronized void setPisoAtual(int piso) {
        if (piso > NUM_PISOS) {
            printWarning("Problemas na organizacao dos pisos !!!!", false);
        } else {
            this.pisoAtual = piso;
        }
    }

    /**
     * Retorna a posição atual do elevador
     *
     * @return numero do piso onde se encontra atualmente o elevador
     */
    public synchronized int getPisoAtual() {
        return this.pisoAtual;
    }

    /**
     * Este método junto com os métodos "diplayFloor()" e "removeFloorReached()"
     * existem para que o acesso às operações feitas por eles sejam syncronized,
     * evintando problemas com o acesso mútuo à 'floor queue'.
     *
     * @param floor identificação do piso
     */
    public synchronized void addFloorToQueue(String floor) {
        this.floorQueue.add(floor);
    }

    /**
     * Este método junto com os métodos "addFloorToQueue()" e "displayFloor()"
     * existem para que o acesso às operações feitas por eles sejam
     * synchronized, evintando problemas com o acesso mútuo à 'floor queue'.
     */
    public synchronized void removeFloorReached() {
        this.floorQueue.remove(0);
    }

    /**
     * Este método junto com os métodos "addFloorToQueue()" e
     * "removeFloorReached()" existem para que o acesso às operações feitas por
     * eles sejam syncronized, evintando problemas com o acesso mútuo às
     * instâncias com a 'queue' e a janela de impressão.
     */
    public synchronized void displayQueue() {
        if (this.floorQueue.isEmpty()) {
            this.displayFloorQueue.setText("Sem pisos em fila de espera.\n");
        } else {
            int i = 1;
            this.displayFloorQueue.setText("-> [" + this.floorQueue.get(0) + "] ");
            while (i < this.floorQueue.size()) {
                this.displayFloorQueue.append("-> [" + this.floorQueue.get(i) + "] ");
                i++;
            }
        }
    }

    /**
     * Just for the sake of being synchronized e para que as variáveis também
     * estejam em private.
     */
    public synchronized void displayPisoAtual() {
        String prettyFloorDisplay
                = "______|" + "==[" + this.getPisoAtual() + "]==" + "|______\n"
                + "|___________________|\n"
                + "|__|=======|=======|__|\n"
                + "|__|=======|=======|__|\n"
                + "|__|=======|=======|__|\n"
                + "[ ElEvAtOr SiMuLaToR ]";
        this.displayFloor.setText("Piso Atual: " + this.getPisoAtual());
        this.displayFloorPretty.setText(prettyFloorDisplay);
    }

    /**
     * Imprime uma mensagem na janela de avisos e mensagens de erro.
     *
     * @param message string a imprimir.
     * @param append true - print em modo append, false - apaga o texto anterior
     */
    public synchronized void printWarning(String message, boolean append) {
        if (!append) {
            this.displayAvisosLast.setText(this.displayAvisos.getText());
            this.displayAvisos.setText(message);
        } else {
            this.displayAvisos.append("\n" + message);
        }
    }

    /**
     * Cria o JFrame relativo ao módulo MAIN onde serão imprimidas as mensagens
     * mais gerais do funcionamento do elevador e informação relativa aos pisos.
     *
     * @return instância do JFrame
     */
    public JFrame criarJanelaPrincipal() {
        /* FAZER JANELA PRINCIPAL DO ELEVADOR */
        janela = new JFrame();

        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setTitle("[ Elevator Display ]");
        /**
         * Design Note: An alternative option for setting the size of the window
         * is to call the pack() method of the JFrame class. This method
         * calculates the size of the window based on the graphical components
         * it contains. Because this sample application doesn't need to change
         * its window size, we'll just use ​the setSize() method.
         */
        janela.setLocationByPlatform(true);

        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new GridLayout(1, 2));
        displayFloor = new JTextField();
        displayFloor.setEditable(false);
        displayFloor.setPreferredSize(new Dimension(100, 100));
        displayFloorQueue = new JTextArea();
        displayFloorQueue.setEditable(false);
        displayFloorQueue.setPreferredSize(new Dimension(200, 100));
        displayFloorQueue.setLineWrap(true);
        displayFloorQueue.setWrapStyleWord(true);
        displayFloorQueue.setText("(janela para impressao da fila de espera dos pisos inseridos)");
        JScrollPane queueScrollPane = new JScrollPane(displayFloorQueue);
        displayPanel.add(displayFloor);
        displayPanel.add(queueScrollPane);

        JPanel prettyPanel = new JPanel();
        prettyPanel.setLayout(new GridLayout(1, 3));
        JLabel prettyLabel = new JLabel("{Display}");
        prettyLabel.setPreferredSize(new Dimension(50, 100));
        displayFloorPretty = new JTextArea();
        displayFloorPretty.setEditable(false);
        displayFloorPretty.setPreferredSize(new Dimension(150, 100));
        prettyPanel.add(prettyLabel);
        prettyPanel.add(displayFloorPretty);
        JButton apagarAvisos = new JButton("Apagar");
        apagarAvisos.addActionListener(
                (ActionEvent e) -> {
                    displayAvisosLast.setText("");
                });
        prettyPanel.add(apagarAvisos);

        //mostra erros eventuais e outros avisos 
        //(isto secalhar é melhor ficar na janela principal)
        JPanel avisosPanel = new JPanel();
        avisosPanel.setLayout(new GridLayout(2, 1));
        //----
        displayAvisosLast = new JTextArea(10, 30);
        displayAvisosLast.setEditable(false);
        displayAvisosLast.setPreferredSize(new Dimension(200, 100));
        displayAvisosLast.setLineWrap(true);
        displayAvisosLast.setWrapStyleWord(true);
        displayAvisosLast.setText("(janela para guardar avisos anteriores)");
        JScrollPane avisosLastScrollPane = new JScrollPane(displayAvisosLast);
        //----
        displayAvisos = new JTextArea(10, 30);
        displayAvisos.setEditable(false);
        displayAvisos.setPreferredSize(new Dimension(200, 100));
        displayAvisos.setText("(janela para impressao de avisos)");
        displayAvisos.setLineWrap(true);
        displayAvisos.setWrapStyleWord(true);
        JScrollPane avisosScrollPane = new JScrollPane(displayAvisos);
        avisosPanel.add(avisosLastScrollPane);
        avisosPanel.add(avisosScrollPane);

        janela.add(displayPanel, BorderLayout.NORTH);
        janela.add(prettyPanel, BorderLayout.CENTER);
        janela.add(avisosPanel, BorderLayout.SOUTH);

        janela.pack();
        janela.setVisible(true);

        this.displayPisoAtual();
        return janela;
    }

    //-------------------------------------------------------------------
    //RODRIGO TIME
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
            //o netbeans "disse-me" que ficava melhor assim, se nao quiseres está
            //aqui no comentário como estava antes
            /*logger.info(thread.getName() + " Piso Inicial: " + pisoInicial
                    + " Piso Final: " + pisoFinal + "Peso transportado: " + CARGA_KG
                    + " Tempo de trabalho: " + getTempoExec());
             */
            logger.log(Level.INFO, "{0} Piso Inicial: {1} Piso Final: "
                    + "{2}Peso transportado: {3} Tempo de trabalho: {4}",
                    new Object[]{thread.getName(), pisoInicial, pisoFinal,
                        CARGA_KG, getTempoExec()});

        } catch (IOException ex) {

        }

    }

    public synchronized void setDefinitions() {

        /**
         * EU ESTIVE A TESTAR ESTE MÉTODO E AINDA NAO FUNCIONA DIREITO!
         *
         * até posso ter sido eu que criei o ficheiro mal.
         *
         * uma lembraça: POR FAVOR, começa a comentar o código que fazes.
         */
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
            CARGA_KG = Integer.parseInt(p.getProperty("carga"));
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
