# Elevator Simulator - Projeto Sistemas Operativos - 2018/2019

**The elevator your condominium needs, but doesn't deserve!**
Um sistema constituído por um conjunto total de quatro *threads*, em java, com a finalidade de simular o funcionamento de um elevador.

Cada uma das *threads* tem um fim em específico, nomeadamente o controlo das portas, o funcionamento do motor, o controlo dos botões e um *hub* de controlo geral.
  
## Configuração do Elevador
 
Para definição da capacidade máxima e o número de pisos do elevador, podem ser modificados os valores presentes no ficheiro de propriedades 'definicoes.properties' dentro do *source* do projeto.

Contudo, é de notar que o ficheiro existe em duplicado, propositadamente. Isto deve-se a uma inconveniência causada pela localização predefinida do ficheiro, que depende do compilador utilizado. Isto é: 
- Utilizando um IDE (como o NetBeans) o ficheiro deverá estar na pasta *root* do projeto;
- Utilizando o compilador java na consola, java compile (javac), o ficheiro deverá estar dentro da pasta 'src'.

## Notas pós defesa do projeto
- Após a defesa do projeto foi detetado um pequeno problema com a lógica relativa à forma como as portas são fechadas ou abertas automaticamente.
- Resumidamente, o problema existe uma vez que o algoritmo não é "robusto" no sentido em que depende bastante do tempo de execução de todas as threads, isto é as ações executam ao mesmo tempo sem haver o cuidado de se verificar o tempo de execução de cada uma, correndo o risco de umas executarem antes do tempo devido. Isto poderá resultar em comportamentos indevidos como a não abertura das portas quando o elevador chega num piso.
- A melhor resolução passa por rever completamente o algoritmo e utilizar uma técnica de comunicação entre threads diferente da utilizada.
