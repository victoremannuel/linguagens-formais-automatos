const fs = require('fs');

// Classe para o NFA (Autômato Não Determinístico)
class Automaton {
    constructor(alpha, state, initial_state, end_state, transition) {
        this.alpha = alpha;
        this.state = state;
        this.initialState = initial_state;
        this.endState = end_state;
        this.transition = transition;
    }
}

// Classe para o DFA (Autômato Determinístico)
class DFA {
    constructor(alpha, state, initial_state, end_state, transition) {
        this.alpha = alpha;
        this.state = state;
        this.initialState = initial_state;
        this.endState = end_state;
        this.transition = transition;
    }
}

// Fecho-ε (epsilon closure) para um conjunto de estados
function epsilonClosure(states, transitions) {
    let stack = [...states];
    let closure = new Set(states);

    while (stack.length > 0) {
        let state = stack.pop();
        if (transitions[state] && transitions[state]['ε']) {
            for (let nextState of transitions[state]['ε']) {
                if (!closure.has(nextState)) {
                    closure.add(nextState);
                    stack.push(nextState);
                }
            }
        }
    }

    return closure;
}

// Converte o NFA para DFA
function convertNFAToDFA(nfa) {
    let nfaTransitions = nfa.transition;

    // Nomear estados no DFA
    let dfaStatesMapping = new Map();
    let dfaStateList = [];
    let queue = [];
    let dfaTransitions = {};
    let dfaFinalStates = [];

    // Iniciar o DFA com o fecho-ε do estado inicial
    let initialClosure = epsilonClosure([nfa.initialState], nfaTransitions);
    let initialStateName = 'A';
    dfaStatesMapping.set(initialClosure, initialStateName);
    dfaStateList.push(initialStateName);
    queue.push(initialClosure);

    if (Array.from(initialClosure).some(state => nfa.endState.includes(state))) {
        dfaFinalStates.push(initialStateName);
    }

    let stateCount = 1;

    while (queue.length > 0) {
        let currentSet = queue.shift();
        let currentStateName = dfaStatesMapping.get(currentSet);
        dfaTransitions[currentStateName] = {};

        // Processar transições para cada símbolo do alfabeto
        for (let symbol of nfa.alpha) {
            let moveSet = new Set();

            for (let state of currentSet) {
                if (nfaTransitions[state] && nfaTransitions[state][symbol]) {
                    for (let moveState of nfaTransitions[state][symbol]) {
                        moveSet.add(moveState);
                    }
                }
            }

            // Fecho-ε dos estados alcançados
            let closure = epsilonClosure([...moveSet], nfaTransitions);

            if (!dfaStatesMapping.has(closure)) {
                let newStateName = String.fromCharCode(65 + stateCount++);
                dfaStatesMapping.set(closure, newStateName);
                dfaStateList.push(newStateName);
                queue.push(closure);

                if (Array.from(closure).some(state => nfa.endState.includes(state))) {
                    dfaFinalStates.push(newStateName);
                }
            }

            // Adicionar transição para o estado atual no DFA
            dfaTransitions[currentStateName][symbol] = dfaStatesMapping.get(closure);
        }
    }

    // Criar o DFA final
    let dfa = new DFA(nfa.alpha, dfaStateList, 'A', dfaFinalStates, dfaTransitions);
    return dfa;
}

// Função para carregar o NFA a partir de um arquivo JSON
function loadNFA(filePath) {
    let rawData = fs.readFileSync(filePath);
    let nfaData = JSON.parse(rawData);
    return new Automaton(nfaData.alpha, nfaData.state, nfaData.initial_state, nfaData.end_state, nfaData.transition);
}

// Função para salvar o DFA em um arquivo JSON
function saveDFA(filePath, dfa) {
    let jsonData = JSON.stringify(dfa, null, 2);
    fs.writeFileSync(filePath, jsonData, 'utf8');
    console.log("Conversão concluída! DFA salvo em:", filePath);
}

// Função principal
function main() {
    const inputFilePath = '/workspaces/linguagens-formais-automatos/js/estudoDeCaso1/input/input.json';
    const outputFilePath = '/workspaces/linguagens-formais-automatos/js/estudoDeCaso1/output/dfa.json';

    // Ler o NFA do arquivo JSON
    let nfa = loadNFA(inputFilePath);

    // Converter o NFA para DFA
    let dfa = convertNFAToDFA(nfa);

    // Escrever o DFA para o arquivo JSON
    saveDFA(outputFilePath, dfa);
}

// Executar o programa
main();
