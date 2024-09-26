package br.edu.ifgoiano.estudoDeCaso1;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class NFAtoDFAConverter {

    // Classe para o NFA (Autômato Não Determinístico)
    static class Automaton {
        @JsonProperty("alpha")
        List<Integer> alpha;

        @JsonProperty("state")
        List<String> state;

        @JsonProperty("initial_state")
        String initialState;

        @JsonProperty("end_state")
        List<String> endState;

        @JsonProperty("transition")
        Map<String, Map<String, List<String>>> transition;
    }

    // Classe para o DFA (Autômato Determinístico)
    static class DFA {
        @JsonProperty("alpha")
        List<Integer> alpha;

        @JsonProperty("state")
        List<String> state;

        @JsonProperty("initial_state")
        String initialState;

        @JsonProperty("end_state")
        List<String> endState;

        @JsonProperty("transition")
        Map<String, Map<String, String>> transition;
    }

    // Fecho-ε (epsilon closure) para um conjunto de estados
    private static Set<String> epsilonClosure(Set<String> states, Map<String, Map<String, List<String>>> transitions) {
        Stack<String> stack = new Stack<>();
        Set<String> closure = new HashSet<>(states);

        for (String state : states) {
            stack.push(state);
        }

        while (!stack.isEmpty()) {
            String state = stack.pop();
            if (transitions.containsKey(state) && transitions.get(state).containsKey("ε")) {
                for (String next : transitions.get(state).get("ε")) {
                    if (!closure.contains(next)) {
                        closure.add(next);
                        stack.push(next);
                    }
                }
            }
        }

        return closure;
    }

    // Converte o NFA para DFA
    private static DFA convertNFAToDFA(Automaton nfa) {
        Map<String, Map<String, List<String>>> nfaTransitions = nfa.transition;

        // Nomear estados no DFA
        Map<Set<String>, String> dfaStatesMapping = new HashMap<>();
        List<String> dfaStateList = new ArrayList<>();
        Queue<Set<String>> queue = new LinkedList<>();
        Map<String, Map<String, String>> dfaTransitions = new HashMap<>();
        List<String> dfaFinalStates = new ArrayList<>();

        // Iniciar o DFA com o fecho-ε do estado inicial
        Set<String> initialClosure = epsilonClosure(Set.of(nfa.initialState), nfaTransitions);
        String initialStateName = "A";
        dfaStatesMapping.put(initialClosure, initialStateName);
        dfaStateList.add(initialStateName);
        queue.add(initialClosure);

        if (initialClosure.stream().anyMatch(nfa.endState::contains)) {
            dfaFinalStates.add(initialStateName);
        }

        int stateCount = 1;

        while (!queue.isEmpty()) {
            Set<String> currentSet = queue.poll();
            String currentStateName = dfaStatesMapping.get(currentSet);
            dfaTransitions.put(currentStateName, new HashMap<>());

            // Processar transições para cada símbolo do alfabeto
            for (Integer symbol : nfa.alpha) {
                Set<String> moveSet = new HashSet<>();
                for (String state : currentSet) {
                    if (nfaTransitions.containsKey(state) && nfaTransitions.get(state).containsKey(symbol.toString())) {
                        moveSet.addAll(nfaTransitions.get(state).get(symbol.toString()));
                    }
                }

                // Fecho-ε dos estados alcançados
                Set<String> closure = epsilonClosure(moveSet, nfaTransitions);

                if (!dfaStatesMapping.containsKey(closure)) {
                    String newStateName = String.valueOf((char) ('A' + stateCount++));
                    dfaStatesMapping.put(closure, newStateName);
                    dfaStateList.add(newStateName);
                    queue.add(closure);

                    if (closure.stream().anyMatch(nfa.endState::contains)) {
                        dfaFinalStates.add(newStateName);
                    }
                }

                // Adicionar transição para o estado atual no DFA
                dfaTransitions.get(currentStateName).put(symbol.toString(), dfaStatesMapping.get(closure));
            }
        }

        // Criar o DFA final
        DFA dfa = new DFA();
        dfa.alpha = nfa.alpha;
        dfa.state = dfaStateList;
        dfa.initialState = "A";
        dfa.endState = dfaFinalStates;
        dfa.transition = dfaTransitions;

        return dfa;
    }

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // Ler o NFA do arquivo JSON
        File inputFile = new File("/workspaces/linguagens-formais-automatos/demo/src/main/java/br/edu/ifgoiano/estudoDeCaso1/input/input.json");
        Automaton nfa = mapper.readValue(inputFile, Automaton.class);

        // Converter o NFA para DFA
        DFA dfa = convertNFAToDFA(nfa);

        // Escrever o DFA para o arquivo JSON
        File outputFile = new File("/workspaces/linguagens-formais-automatos/demo/src/main/java/br/edu/ifgoiano/estudoDeCaso1/output/dfa.json");
        mapper.writeValue(outputFile, dfa);

        System.out.println("Conversão concluída! DFA salvo em: output/dfa.json");
    }
}
