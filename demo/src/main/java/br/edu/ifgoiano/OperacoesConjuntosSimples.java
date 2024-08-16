package br.edu.ifgoiano;

import java.util.*;

public class OperacoesConjuntosSimples {

    public static void main(String[] args) {
        Set<Integer> A = new HashSet<>(Arrays.asList(2, 4));
        Set<Integer> B = new HashSet<>(Arrays.asList(4, 5, 6));
        Set<Integer> U = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));

        // União
        Set<Integer> uniao = new HashSet<>(A);
        uniao.addAll(B);
        System.out.println("União: " + uniao);

        // Interseção
        Set<Integer> intersecao = new HashSet<>(A);
        intersecao.retainAll(B);
        System.out.println("Interseção: " + intersecao);

        // Diferença A - B
        Set<Integer> diferenca = new HashSet<>(A);
        diferenca.removeAll(B);
        System.out.println("Diferença (A - B): " + diferenca);

        // Complemento de A em U
        Set<Integer> complemento = new HashSet<>(U);
        complemento.removeAll(A);
        System.out.println("Complemento de A em U: " + complemento);

        // Conjunto das Partes de A
        Set<Set<Integer>> conjuntoPartes = conjuntoDasPartes(A);
        System.out.println("Conjunto das Partes de A: " + conjuntoPartes);

        // Produto Cartesiano A x B
        Set<List<Integer>> produtoCartesiano = produtoCartesiano(A, B);
        System.out.println("Produto Cartesiano A x B: " + produtoCartesiano);
    }

    public static Set<Set<Integer>> conjuntoDasPartes(Set<Integer> conjunto) {
        Set<Set<Integer>> partes = new HashSet<>();
        partes.add(new HashSet<>()); // Adicionando o conjunto vazio

        for (Integer elemento : conjunto) {
            Set<Set<Integer>> novasPartes = new HashSet<>();
            for (Set<Integer> subset : partes) {
                Set<Integer> novoSubset = new HashSet<>(subset);
                novoSubset.add(elemento);
                novasPartes.add(novoSubset);
            }
            partes.addAll(novasPartes);
        }

        return partes;
    }

    public static Set<List<Integer>> produtoCartesiano(Set<Integer> A, Set<Integer> B) {
        Set<List<Integer>> produto = new HashSet<>();
        for (Integer a : A) {
            for (Integer b : B) {
                produto.add(Arrays.asList(a, b));
            }
        }
        return produto;
    }
}
