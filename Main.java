import Classes.*;
import Utils.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {

        String formula = "";
        String filepath = "input.txt";
        try {
         formula = InputReader.readFormulaFromFile(filepath);
        } catch (IOException e ){
         e.printStackTrace();
        }   
        System.out.println(formula);

        Set<String> F = FormulaParser.FormulatoConjuncts(formula);
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
//
        } 
        else {
            System.out.println(solver);
            System.out.println("UNSAT");
        }

    }
}
