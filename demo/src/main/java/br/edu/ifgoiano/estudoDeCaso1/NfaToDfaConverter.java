package br.edu.ifgoiano.estudoDeCaso1;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.*;

public class NfaToDfaConverter {

    static class NFA {
        public List<Integer> alpha;
        public List<String> state;
        public String initial_state;
        public List<String> end_state;
        public Map<String, Map<String, List<String>>> transition;
    }

    static class DFA {
        public List<Integer> alpha;
        public List<String> state;
        public String initial_state;
        public List<String> end_state;
        public Map<String, Map<String, String>> transition;
    }

    // Helper method to compute epsilon-closure
    private static Set<String> epsilonClosure(String state, Map<String, Map<String, List<String>>> transitions) {
        Set<String> closure = new HashSet<>();
        Stack<String> stack = new Stack<>();
        stack.push(state);
        closure.add(state);

        while (!stack.isEmpty()) {
            String current = stack.pop();

            // Verifica se há transições para o estado atual
            Map<String, List<String>> currentTransitions = transitions.get(current);
            if (currentTransitions == null) {
                continue; // Se não houver transições para esse estado, continua
            }

            List<String> epsilonMoves = currentTransitions.get("\u03B5");
            if (epsilonMoves != null) {
                for (String nextState : epsilonMoves) {
                    if (!closure.contains(nextState)) {
                        closure.add(nextState);
                        stack.push(nextState);
                    }
                }
            }
        }

        return closure;
    }

    // Convert NFA to DFA
    public static DFA convertNfaToDfa(NFA nfa) {
        DFA dfa = new DFA();
        dfa.alpha = nfa.alpha;
        dfa.transition = new HashMap<>();
        dfa.end_state = new ArrayList<>();
        dfa.state = new ArrayList<>();

        Map<Set<String>, String> dfaStates = new HashMap<>(); // Maps NFA state sets to DFA state names
        Queue<Set<String>> queue = new LinkedList<>();

        // Initial DFA state from epsilon-closure of the NFA's initial state
        Set<String> initialClosure = epsilonClosure(nfa.initial_state, nfa.transition);
        String initialState = initialClosure.toString();
        dfaStates.put(initialClosure, initialState);
        dfa.state.add(initialState);
        dfa.initial_state = initialState;
        queue.add(initialClosure);

        // Process all DFA states
        while (!queue.isEmpty()) {
            Set<String> currentSet = queue.poll();
            String currentState = dfaStates.get(currentSet);

            // Initialize DFA transitions for the current DFA state
            dfa.transition.put(currentState, new HashMap<>());

            // For each symbol in the alphabet
            for (Integer symbol : nfa.alpha) {
                Set<String> newStateSet = new HashSet<>();

                // For each state in the current NFA state set
                for (String nfaState : currentSet) {
                    // Verifica se o estado tem transições
                    Map<String, List<String>> currentTransitions = nfa.transition.get(nfaState);
                    if (currentTransitions == null) {
                        continue; // Se não houver transições, continuar para o próximo estado
                    }

                    List<String> transitions = currentTransitions.get(symbol.toString());
                    if (transitions != null) {
                        for (String state : transitions) {
                            newStateSet.addAll(epsilonClosure(state, nfa.transition));
                        }
                    }
                }

                if (!newStateSet.isEmpty()) {
                    String newState = newStateSet.toString();
                    if (!dfaStates.containsKey(newStateSet)) {
                        dfaStates.put(newStateSet, newState);
                        dfa.state.add(newState);
                        queue.add(newStateSet);
                    }
                    dfa.transition.get(currentState).put(symbol.toString(), newState);
                }
            }
        }

        // Determine DFA end states
        for (Set<String> nfaStateSet : dfaStates.keySet()) {
            for (String endState : nfa.end_state) {
                if (nfaStateSet.contains(endState)) {
                    dfa.end_state.add(dfaStates.get(nfaStateSet));
                    break;
                }
            }
        }

        return dfa;
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Read the NFA from a JSON file
        NFA nfa = mapper.readValue(new File(
                "/workspaces/linguagens-formais-automatos/demo/src/main/java/br/edu/ifgoiano/laboratorio2/input/input.json"),
                NFA.class);

        // Convert the NFA to a DFA
        DFA dfa = convertNfaToDfa(nfa);

        // Write the DFA to a JSON file
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("/workspaces/linguagens-formais-automatos/demo/src/main/java/br/edu/ifgoiano/laboratorio2/output/dfa.json"), dfa);
    }
}
