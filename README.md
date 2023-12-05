# asmetadigitaltwin

Il progetto consiste nell'estendere il simulatore di ASMETA per associare l'esecuzione di un modello ASM a un gemello digitale di Azure Digital Twins.

Prerequisiti per l'esecuzione del programma:
- una sottoscrizione di Azure con un'istanza di Azure Digital Twins;
- un twin nell'istanza di Azure Digital Twins;
- un modello ASM.

Quindi eseguire il programma asmetadt.jar settando i seguenti parametri:
  -t: id del twin
  -e: endpoint dell'istanza di Azure Digital Twins
  -a: percorso del modello ASM
  -p: periodo di sincronizzazione in millisecondi

java -jar asmetadt.jar -t \{twinId\} -e \{endpoint\} -a {asmPath} -p {period}
