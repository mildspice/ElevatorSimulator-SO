# ProjetoSO - Elevator Simulator

The elevator your condominium needs, but doesn't deserve.

NOTA:\
  Depois da defesa do projeto descobriu-se um pequeno problema com a lógica relativa à forma como as portas são fechadas ou abertas automaticamente.\
  Resumidamente, o problema existe uma vez que o algoritmo não é "robusto" no sentido em que depende bastante do tempo de execução de todas as threads, isto é as ações executam ao mesmo tempo sem haver o cuidado de se verificar o tempo de execução de cada uma, correndo o risco de umas executarem antes do tempo devido. Isto poderá resultar em comportamentos indevidos como a não abertura das portas quando o elevador chega num piso.\
  A melhor resolução passa por rever completamente o algoritmo e utilizar uma técnica de comunicação entre threads diferente da utilizada.\
  Nota do PROJETO - 18
  
NOTA 2:\
  O ficheiro 'definicoes.properties' existe em duplicado propositadamente, uma vez que a localização predefinida desse ficheiro quando utilizado pela aplicação difere dependendo do compilador utilizado, por exemplo utilizando um IDE (o ficheiro deverá estar no 'root' do projeto) ou java compile (javac) na linha de comandos (o ficheiro deverá estar dentro da pasta 'src').
