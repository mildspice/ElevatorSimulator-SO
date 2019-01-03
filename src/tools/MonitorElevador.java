package tools;

import enums.EstadosMotor;
import enums.EstadosPortas;
import java.io.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
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
    //valor a ser colocado através do ficheiro "propriedades"
    private int NUM_PISOS;
    private int pisoAtual = 1;
    //valor a ser colocado através do ficheiro "propriedades"
    private int CARGA_TOTAL;
    private int cargaAtual = 0;
    private int cargaTransportadatotal=0;
    private double startime;
    private double endtime;
    //variâveis relacionadas ao funcionamento da Botoneira
    /*
    secalhar há uma forma melhor de fazer isto mas assim também funciona:
    -> piso = indice do array + 1,
        ex. botoesPisos[2] = "PISO3", botoesPisos[0] = "PISO1"
     */
    private String[] botoesPisos;
    //acabei por colocar as portas como tu (Rodrigo) tinhas inicialmente
    //(como boolean) true - A, false - F.
    private boolean doorButton;
    //para mais informações sobre o ItemEvent, ler a api do JToggleButton ItemListener
    private boolean chave;
    //esta é a fila para a introdução dos pisos de destino. Para o escalonamento
    //vai ser usado o FIFO em princípio.
    private final ArrayList<String> floorQueue = new ArrayList<>();
    //variáveis relacionadas ao estado do motor e das portas
    private EstadosMotor direcaoMotor;
    private EstadosPortas estadoPortas = EstadosPortas.ABERTO;

    //variáveis do log
    private Logger logger;
    private double tempoExec;
    private int vezesExecutado = 0;

    /**
     * Construtor do objeto partilhado.
     *
     * @param exclusaoMutua semaforo para o controlo da realização de operações
     * "sensíveis"
     * @throws java.io.IOException sinaliza se o ficheiro de configurações não
     * foi lido
     */
    public MonitorElevador(Semaphore exclusaoMutua) throws IOException {
        logger = Logger.getLogger("ElevatorLog");
      //  startime=System.currentTimeMillis();
        setDefinitions();
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
    public String[] getBotoesPisos() {
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
    public ArrayList<String> getFloorQueue() {
        return this.floorQueue;
    }

    /**
     * Este método serve para que o estado do JToggleButton relativo à chave que
     * está na botoneira seja "visto" por todas as outras threads. (ver api
     * sobre JToggleButton ItemListener) (Este método só vai ser utilizado pelo
     * botão do JFrame)
     *
     * @param state estado boleano da chave
     */
    public synchronized void setEstadoChave(boolean state) {
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
     * Retorna o estado atual das portas. Sensível ao estado da chave!
     *
     * @param estado enum constant referente ao estado das portas
     */
    public synchronized void setEstadoPortas(EstadosPortas estado) {
        if (chave) {
            printWarning("Chave Acionada!\n"
                    + "Movimento das Portas Impedido.", true);
        } else {
            this.estadoPortas = estado;
        }
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
     * Altera o estado funcional do elevador. Sensível ao estado da chave!
     *
     * @param flag booleano para o andamento do elevador
     */
    public synchronized void setFlagFuncionamento(boolean flag) {
        if (chave) {
            printWarning("Chave Acionada!\n"
                    + "Deslocacao do elevador impedida.", true);
        } else {
            this.flagFuncionamento = flag;
        }
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
     * Atualiza o peso que se econtra dentro do elevador atualmente
     *
     * @param carga peso total presente no elevador
     * @return true - peso não excede a carga, false - no contrário
     */
    public synchronized boolean setCargaAtual(int carga) {
        if (carga > CARGA_TOTAL) {
            return false;
        } else {
            this.cargaAtual = carga;
            return true;
        }
    }

    /**
     * Retorna a posição atual do elevador
     *
     * @return peso total que se encontra atualmente dentro o elevador
     */
    public synchronized int getCargaAtual() {
        return this.cargaAtual;
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
                = "_______|" + "==[" + this.getPisoAtual() + "]==" + "|______\n"
                + "|_____________________|\n"
                + "|__|=======|=======|__|\n"
                + "|__|=======|=======|__|\n"
                + "|__|=======|=======|__|\n"
                + " [ ElEvAtOr SiMuLaToR ]";
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
            this.displayAvisos.setText(message
                    + "\n******************************");
        } else {
            this.displayAvisos.append("\n" + message
                    + "\n******************************");
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
        this.janela = new JFrame();

        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setTitle("[ Display Principal Elevador ]");
        /**
         * Design Note: An alternative option for setting the size of the window
         * is to call the pack() method of the JFrame class. This method
         * calculates the size of the window based on the graphical components
         * it contains. Because this sample application doesn't need to change
         * its window size, we'll just use ​the setSize() method.
         */
        janela.setLocationByPlatform(true);

        /**
         * AQUI FAZ DISABLE AO FECHO DO JFRAME NORMAL, OBRIGA A CLICAR NO EXIT!!!!!
         */
        janela.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new GridLayout(1, 3));
        JLabel prettyLabel = new JLabel("[Current Floor Display]");
        prettyLabel.setPreferredSize(new Dimension(50, 100));
        this.displayFloor = new JTextField();
        displayFloor.setEditable(false);
        displayFloor.setPreferredSize(new Dimension(75, 100));
        this.displayFloorPretty = new JTextArea();
        displayFloorPretty.setEditable(false);
        displayFloorPretty.setPreferredSize(new Dimension(175, 100));
        displayFloorPretty.setFont(new Font("monospaced", Font.PLAIN, 12));
        displayFloorPretty.add(prettyLabel);
        displayPanel.add(prettyLabel);
        displayPanel.add(displayFloor);
        displayPanel.add(displayFloorPretty);

        JPanel floorPanel = new JPanel();
        floorPanel.setLayout(new GridLayout(1, 2));
        this.displayFloorQueue = new JTextArea();
        displayFloorQueue.setEditable(false);
        displayFloorQueue.setLineWrap(true);
        displayFloorQueue.setWrapStyleWord(true);
        displayFloorQueue.setText("(janela para impressao da fila de espera "
                + "dos pisos inseridos)");
        JScrollPane queueScrollPane = new JScrollPane(displayFloorQueue);
        queueScrollPane.setPreferredSize(new Dimension(200, 150));
        floorPanel.add(displayFloorQueue);
        JButton apagarAvisos = new JButton("Apagar");
        apagarAvisos.setToolTipText("Limpar a area de texto. A janela de novas "
                + "mensagens de aviso ficara vazia.");
        apagarAvisos.setPreferredSize(new Dimension(100, 125));
        apagarAvisos.addActionListener(
                (ActionEvent e) -> {
                    displayAvisosLast.setText(displayAvisos.getText());
                    displayAvisos.setText("");
                });
        floorPanel.add(apagarAvisos);
        //mostra erros eventuais e outros avisos 
        //(isto secalhar é melhor ficar na janela principal)
        JPanel avisosPanel = new JPanel();
        avisosPanel.setLayout(new GridLayout(2, 1));
        //----
        this.displayAvisosLast = new JTextArea(10, 30);
        displayAvisosLast.setEditable(false);
        displayAvisosLast.setLineWrap(true);
        displayAvisosLast.setWrapStyleWord(true);
        displayAvisosLast.setFont(new Font("monospaced", Font.PLAIN, 12));
        displayAvisosLast.setText("(janela para guardar avisos anteriores)");
        JScrollPane avisosLastScrollPane = new JScrollPane(displayAvisosLast);
        avisosLastScrollPane.setPreferredSize(new Dimension(200, 150));
        this.displayAvisos = new JTextArea(10, 30);
        displayAvisos.setEditable(false);
        displayAvisos.setText("(janela para impressao de avisos)");
        displayAvisos.setLineWrap(true);
        displayAvisos.setWrapStyleWord(true);
        displayAvisos.setFont(new Font("monospaced", Font.PLAIN, 12));
        JScrollPane avisosScrollPane = new JScrollPane(displayAvisos);
        avisosScrollPane.setPreferredSize(new Dimension(200, 150));
        avisosPanel.add(avisosLastScrollPane);
        avisosPanel.add(avisosScrollPane);

        janela.add(displayPanel, BorderLayout.NORTH);
        janela.add(floorPanel, BorderLayout.CENTER);
        janela.add(avisosPanel, BorderLayout.SOUTH);

        janela.pack();
        janela.setVisible(true);

        this.displayPisoAtual();
        return janela;
    }

    public double getTempoExec() {
        return tempoExec;
    }

    public void setTempoExec(double tempoExec) {
        this.tempoExec = tempoExec;
    }

    /**
     * Escreve para um ficheiro de Log. Este tipos de ficheiros é mesmo do Java
     * e escreverá logo de uma maneira predefinida.
     *
     * @param thread passa para o ficheiro a Thread que criou o log
     * @param pisoInicial passa para o ficheiro o piso inicial do deslocamento
     * @param pisoFinal passa para o ficheiro o destino do deslocamento.
     */
    public synchronized void writeLog(Thread thread, int pisoInicial, int pisoFinal) {

        FileHandler fh;

        try {

            fh = new FileHandler("ElevatorLog.log", true);
            logger.addHandler(fh);
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fh.setFormatter(simpleFormatter);
            //o netbeans "disse-me" que ficava melhor assim, se nao quiseres está
            //aqui no comentário como estava antes
            /*logger.info(thread.getName() + " Piso Inicial: " + pisoInicial
                    + " Piso Final: " + pisoFinal + "Peso transportado: " + CARGA_KG
                    + " Tempo de trabalho: " + getTempoExec());
             */
            this.semaforoExclusaoMutua.acquire();
            logger.log(Level.INFO, "{0} Piso Inicial: {1} Piso Final: "
                    + "{2} Peso transportado: {3} Tempo de trabalho: {4}",
                    new Object[]{thread.getName(), pisoInicial, pisoFinal,
                        cargaAtual, getTempoExec()});
            fh.close();
            this.semaforoExclusaoMutua.release();

        } catch (IOException ex) {

        } catch (InterruptedException e) {

        }

    }

    /**
     * Incrementa na varíavel o número de vezes que foi executado
     */
    public void usingCounter() {
        vezesExecutado++;
    }

    /**
     * Calcula o peso total que vai sendo utilizado no elevador.
     */
    public void pesoTotal(){ cargaTransportadatotal=cargaTransportadatotal+cargaAtual; }

    /**
     * Cria um relatório geral quando se fecha o programa
     *
     * (tu estás a criar 2 ficheiros de log diferentes!?)
     */
    public void reportGeneration() {

        Logger logger = Logger.getLogger("GeneralExecutionLog");
        FileHandler fh;

        try {

            fh = new FileHandler("GeneralExecutionLog.log", true);
            logger.addHandler(fh);
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fh.setFormatter(simpleFormatter);
            //Que mais colocar aqui? LOL
            /*
            tu estás a guardar uma variável de tempo de execução, poe aqui tambem.
            podes guardar o número de vezes que as portas foram usadas.
            
            podes por coisas mais engraçadas tipo, em que direcao o motor se deslocou
            mais vezes, quantas vezes o utilizador se tentou suicidar (experimenta
            por o elevador a funcionar, pará-lo manualmente e abre as portas. lê a mensagem
            que vai aparecer na janela principal...)
            
            o problema disto é que secalhar vai ser preciso criar muitas variaveis.
            mas acho que nao é problema. temos isto muito bem organizado.
             */
            //logger.info("Número de Vezes Executado nesta sessão: " + vezesExecutado);

            logger.log(Level.INFO,  "Número de vezes executado: {0} Total de peso transportado: "
                            + "{1} Tempo total de execução: {2}",
                    new Object[]{vezesExecutado, cargaAtual, getFinalTime()});
            fh.close();

        } catch (IOException ex) {

        }

    }

    /**
     * Lê o ficheiro de configurações, procura pelas propriedades definidas,
     * nomeadamente a carga e o número de pisos, e coloca nas variáveis.
     */
    private void setDefinitions() throws IOException {

        /*
        Alterei a forma como as exceptions são tratadas, não acho que a forma como
        estava fosse a melhor. Assim evita logo no inicio que código corra se não
        conseguir ler o ficheiro!
        
        Eu parti do principio que este método já está finalizado! Se apaguei alguma
        coisa que não devia, sorry. podes sempre ir ver os commits no git.
         */
        File deffile = new File("definicoes.properties");

        Properties p = new Properties();
        p.load(new FileInputStream(deffile));

        NUM_PISOS = Integer.parseInt(p.getProperty("pisos"));
        botoesPisos = new String[NUM_PISOS];
        for (int i = 0; i < this.botoesPisos.length; i++) {
            this.botoesPisos[i] = "PISO" + (i + 1);
        }
        CARGA_TOTAL = Integer.parseInt(p.getProperty("carga"));
    }

    public double getFinalTime() {
        return endtime-startime;
    }

    public void setEndtime(double endtime) {
        this.endtime = endtime;
    }

    public void setStartime(double startime) {
        this.startime = startime;
    }
}
