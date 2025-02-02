import Classes.*;
import Utils.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        //![[A]AND[B]]
        //"![[[A]OR[C]]AND[B]]
        //String formula = "[A]<->[B]";
        //Set<String> clauses = FormulaParser.FormulatoDNF(formula);
        //System.out.println(clauses);
        //System.out.println("FINE TEST NNF\n----------------------------------------------");
        String formula = "";
        String filepath = "input.txt";
        try {
         formula = InputReader.readFormulaFromFile(filepath);
        } catch (IOException e ){
         e.printStackTrace();
        }   
        System.out.println(formula);
        //here we should transform the formula with DNF into a set of conjuncts
        //then we do as follow for each conjunct (A AND B AND C AND D) OR (F AND G AND H) 
        Set<String> cubes = FormulaParser.FormulatoDNF(formula);
        
        for ( String s : cubes) {
            Set<String> Formulas_without_store = FormulaParser.FormulaArraysToSetnoStore(s);
            cubes.addAll(Formulas_without_store);
        }
        System.out.println("--------------");
        Boolean unsat_flag = true;
        for (String cube : cubes) {
            System.out.println("working on cube = " + cube);
            Set<String> F = FormulaParser.FormulatoConjuncts(cube);
            System.out.println("--------------");
            System.out.println(F);  
            Set<String> Sf = FormulaParser.ConjunctstoTerms(F);
            System.out.println("--------------");
            System.out.println(Sf);
            Set<List<String>> forbidden_set = FormulaParser.get_forbiddenset(F);
            //System.out.println("--------------");
            //System.out.println(forbidden_set);
            Set<List<String>> equalities = FormulaParser.get_equalities(F);
            //System.out.println("--------------");
            //System.out.println("equalities are" + equalities);
            //System.out.println("--------------");
            Dagsolver solver = FormulaParser.TermstoDAG(Sf, forbidden_set, equalities);
            //System.out.println("id equalities = "+ solver.equalities);
            //System.out.println("--------------");
//  
            System.out.println(solver);
            System.out.println("--------------");
            System.out.println(solver.getEqualities());
            System.out.println("--------------");
            System.out.println("inequalities are = " + solver.getInequalities());
            if(solver.solve()) {
                System.out.println(solver);
                System.out.println("SAT");
                unsat_flag = false;
                break;
            } 
            else {
                System.out.println(solver);
                cube = cube.replace(" ", "");
                System.out.println(cube + " is unsat, proceeding with next cube");
            }
        }
        if(unsat_flag) {
            System.out.println("--------------");
            System.out.println("\n Whole formula is UNSAT since all cubes are unsat");
        }
        
    }
}
