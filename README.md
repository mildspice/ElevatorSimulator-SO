# ProjetoSO - Elevator Simulator

The elevator your condominium needs, but doesn't deserve.

NOTA: 
  Depois da defesa do projeto descobriu-se um pequeno problema com a lógica relativa à forma como as portas são fechadas ou abertas automaticamente.
  Resumidamente, o problema existe uma vez que o algoritmo não é "robusto" no sentido em que depende bastante do tempo de execução de todas as threads, isto é as ações executam ao mesmo tempo sem haver o cuidado de se verificar o tempo de execução de cada uma, correndo o risco de umas executarem antes do tempo devido. Isto poderá resultar em comportamentos indevidos como a não abertura das portas quando o elevador chega num piso.
  A melhor resolução passa por rever completamente o algoritmo e utilizar uma técnica de comunicação entre threads diferente da utilizada.
