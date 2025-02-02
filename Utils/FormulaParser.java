package Utils;
import Classes.Node;
import Classes.Dagsolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;
import java.util.HashMap;


public class FormulaParser {
    // Reduction to dnf, the result will be the set of each cube
    // USE [] FOR NORMAL PARENTESIS OF ORDER OF CONNECTIVES, USE () ONLY FOR FUNCTION/RELATIONS : [A] AND [B], [F(A)]->[B]
    // we consider  input made with parenthesis for each connective: [R(a)] <-> [[a=b]AND[R(b)]]
    // even for negation: [![A]]<->[B]
    // A AND B OR C AND D -->  {A AND B,
    //                          C AND D  }
    public static Set<String> FormulatoDNF(String formula) {
        Set<String> to_return = new HashSet<>();
        //reduction to NNF
        String formula_NNF = FormulatoNNF(formula);
        //handling AND over OR    [A]AND[[B]OR[C]] --> [A]AND[B]OR[A]AND[C]
        //[[B]OR[C]]AND[A]  --> [A]AND[B]OR[A]AND[C]
        int curr_lvl = 0;
        int lvl_and = 0;
        int index_and = 0;
        boolean changed = true;
        if (! formula_NNF.contains("OR")) changed = false;
        while (changed){
            String iter = formula_NNF;
            //System.out.println("aaaaaaaa");
            for (int j = 0; j<=iter.length(); j++) {
                //System.out.println(formula_NNF);
                if (j == iter.length()) {
                    changed = false;
                    break;
                }
                if (formula_NNF.charAt(j) == '[') {
                    curr_lvl++;
                    continue;
                }
                if (formula_NNF.charAt(j) == ']') {
                    curr_lvl--;
                    continue;
                }
                if (formula_NNF.charAt(j) == 'A') {
                    if (formula_NNF.charAt(j+1) == 'N' && formula_NNF.charAt(j+2) == 'D') {
                        lvl_and = curr_lvl;
                        index_and = j;
                        int index_open_parenthesis = 0;
                        int index_close_parenthesis = 0;
                        
                        //right to left, encountering [ decrease the lvl, less deep
                        for (int i = index_and; curr_lvl >= lvl_and && i>=0 ; i--) {
                            if (formula_NNF.charAt(i) == '[') {
                                curr_lvl--;
                            }
                            if (formula_NNF.charAt(i) == ']') {
                                curr_lvl++;
                            }
                            index_open_parenthesis = i;
                        }
                        if(index_open_parenthesis>0 || curr_lvl< lvl_and) {
                            index_open_parenthesis++;
                        }
                        curr_lvl = lvl_and;
                        //left to right, encoutering [ increase the lvl, deeper
                        for (int i = index_and+3; curr_lvl >= lvl_and && i<formula_NNF.length(); i++) {
                            if (formula_NNF.charAt(i) == '[') {
                                curr_lvl++;
                            }
                            if (formula_NNF.charAt(i) == ']') {
                                curr_lvl--;
                            }
                            index_close_parenthesis = i;
                        }
                        if(index_close_parenthesis == formula_NNF.length()-1) {
                            index_close_parenthesis++;
                        }
                        String to_be_replaced = formula_NNF.substring(index_open_parenthesis, index_close_parenthesis);
                        String term_1 = formula_NNF.substring(index_open_parenthesis, index_and);
                        String term_2 = formula_NNF.substring(index_and+3, index_close_parenthesis);
                        //ensure we don't have extra [ 
                        int openCount = term_2.length() - term_2.replaceAll("\\[", "").length();
                        int closeCount = term_2.length() - term_2.replaceAll("\\]", "").length();
                        int many_to_delete = closeCount - openCount;
                        //System.out.println("\n t2 before " + term_2);
                        //System.out.println("\n to be replaced before " + to_be_replaced);
                        
                        if (many_to_delete>0) {
                            //System.out.println("number of ] to delete = "+many_to_delete);
                            term_2 = term_2.substring(0, term_2.length()-many_to_delete);
                            to_be_replaced = to_be_replaced.substring(0, to_be_replaced.length()-many_to_delete);
                        }
                        //System.out.println (to_be_replaced + "\n term_1 " + term_1 + "\n t2 " + term_2);
                        //System.out.println(formula_NNF);
                        //check term_1
                        if (term_1.contains("OR"))  {
                            int t1_lvl = 0;
                            int t1_open_parenthesis = 0;
                            int t1_close_parenthesis = 0;
                            int or_index = -1;
                            for (int i = 0; i<term_1.length() ; i++) {
                                if (term_1.charAt(i) == '[') {
                                    t1_lvl++;
                                }
                                if (term_1.charAt(i) == ']') {
                                    t1_lvl--;
                                }
                                //complete from here
                                if (t1_lvl == 1 && term_1.charAt(i) == 'O'){
                                    if (term_1.charAt(i+1) == 'R') {
                                        or_index = i;
                                        break;
                                    }
                                }
                            }
                            
                            if (or_index == -1) continue; 
                                // Find the boundaries of the two terms
                                // For term1_1, search backwards from OR until we find matching bracket at level 1
                                t1_lvl = 1;  // Start at level 1 since OR is at level 1
                                for (int i = or_index - 1; i >= 0; i--) {
                                    if (term_1.charAt(i) == ']') {
                                        t1_lvl++;
                                    } else if (term_1.charAt(i) == '[') {
                                        t1_lvl--;
                                        if (t1_lvl == 0) {  // Found the matching opening bracket
                                            t1_open_parenthesis = i;
                                            break;
                                        }
                                    }
                                }
                                
                                // For term1_2, search forwards from OR until we find matching bracket at level 1
                                t1_lvl = 1;  // Reset level
                                for (int i = or_index + 2; i < term_1.length(); i++) {
                                    if (term_1.charAt(i) == '[') {
                                        t1_lvl++;
                                    } else if (term_1.charAt(i) == ']') {
                                        t1_lvl--;
                                        if (t1_lvl == 0) {  // Found the matching closing bracket
                                            t1_close_parenthesis = i;
                                            break;
                                        }
                                    }
                                }
                                // Extract the two terms
                                String term1_1 = term_1.substring(t1_open_parenthesis, or_index);
                                String term1_2 = term_1.substring(or_index + 2, t1_close_parenthesis);
                                
                                // Create the new formula by replacing term_1 with the separated terms
                                //[[A]AND[B]]OR[[A]AND[C]]   [[B]AND[A]]OR[[C]AND[A]]
                                String new_formula =  term1_1 + "AND" + term_2 + "]OR[" + 
                                                   term1_2 + "AND" + term_2 + "]";
                                //System.out.println("substitution on left = " + new_formula);
                                //System.out.println("formula to be replaced " + to_be_replaced);
                                formula_NNF = formula_NNF.replace(to_be_replaced,new_formula);
                                //System.out.println("formula after replace " + formula_NNF);
                                break;
                        }
                        //check term_2
                        if (term_2.contains("OR")) {
                            int t2_lvl = 0;
                            int t2_open_parenthesis = 0;
                            int t2_close_parenthesis = 0;
                            int or_index = -1;
                            
                            // Find OR at level 1
                            for (int i = 0; i < term_2.length(); i++) {
                                if (term_2.charAt(i) == '[') {
                                    t2_lvl++;
                                }
                                if (term_2.charAt(i) == ']') {
                                    t2_lvl--;
                                }
                                if (t2_lvl == 1 && term_2.charAt(i) == 'O') {
                                    if (term_2.charAt(i+1) == 'R') {
                                        or_index = i;
                                        break;
                                    }
                                }
                            }
                            
                            if (or_index == -1) continue;
                                // Find the boundaries of the two terms
                                // For term2_1, search backwards from OR
                                t2_lvl = 1;  // Start at level 1 since OR is at level 1
                                for (int i = or_index - 1; i >= 0; i--) {
                                    if (term_2.charAt(i) == ']') {
                                        t2_lvl++;
                                    } else if (term_2.charAt(i) == '[') {
                                        t2_lvl--;
                                        if (t2_lvl == 0) {  // Found the matching opening bracket
                                            t2_open_parenthesis = i;
                                            break;
                                        }
                                    }
                                }
                                
                                // For term2_2, search forwards from OR
                                t2_lvl = 1;  // Reset level
                                for (int i = or_index + 2; i < term_2.length(); i++) {
                                    if (term_2.charAt(i) == '[') {
                                        t2_lvl++;
                                    } else if (term_2.charAt(i) == ']') {
                                        t2_lvl--;
                                        if (t2_lvl == 0) {  // Found the matching closing bracket
                                            t2_close_parenthesis = i;
                                            break;
                                        }
                                    }
                                }
                                
                                // Extract the two terms
                                String term2_1 = term_2.substring(t2_open_parenthesis+1, or_index);
                                String term2_2 = term_2.substring(or_index + 2, t2_close_parenthesis);
                                
                                // Create the new formula by replacing term_2 with the separated terms
                                String new_formula ="["+ term_1 + "AND" + term2_1 + "]OR[" + 
                                                   term_1 + "AND" + term2_2 + "]";
                                //System.out.println("substitution on right = " + new_formula);
                                formula_NNF = formula_NNF.replace(to_be_replaced, new_formula);
                                break;
                            
                        }
                        
                        changed= false;
                        
                    }
                    
                }
               
                
            }
        }
        System.out.println("formula final before split = " + formula_NNF +" \n");
        Set<String> remove_parenthesis = new HashSet<>(Arrays.asList(formula_NNF.split("OR")));
        //remove all [ and ] from the set
        for (String s : remove_parenthesis) {
            s = s.replaceAll("\\[", "").replaceAll("\\]", "");
            to_return.add(s);
        }
        return to_return;
    }
    
    // -> <->    R(a)<->R(b)    ![a=b]
    public static String FormulatoNNF(String formula) {
        formula = formula.replaceAll(" ", "");
        //handling <->
        while (formula.contains("<->")) {
            int index_biconditional = formula.indexOf("<->");
            int lvl_biconditional = 0;
            //left to right, encoutering ( increase the lvl, deeper
            for (int i =0 ; i<index_biconditional ; i++) {
                if (formula.charAt(i) == '[') {
                    lvl_biconditional++;
                }
                if (formula.charAt(i) == ']') {
                    lvl_biconditional--;
                }
            }
            // [R(a)] <-> [R(b)] -----> [[R(a)] -> [R(b)]] AND [[R(b)] -> [R(a)]]
            int index_open_parenthesis = 0;
            int index_close_parenthesis = 0;
            int curr_lvl = lvl_biconditional;

            //right to left, encountering [ decrease the lvl, less deep
            for (int i = index_biconditional; curr_lvl >= lvl_biconditional && i>=0 ; i--) {
                if (formula.charAt(i) == '[') {
                    curr_lvl--;
                }
                if (formula.charAt(i) == ']') {
                    curr_lvl++;
                }
                index_open_parenthesis = i;
            }
            if(index_open_parenthesis>0 || curr_lvl< lvl_biconditional) {
				index_open_parenthesis++;
			}
            curr_lvl = lvl_biconditional;
            //left to right, encoutering [ increase the lvl, deeper
            for (int i = index_biconditional+3; curr_lvl >= lvl_biconditional && i<formula.length(); i++) {
                if (formula.charAt(i) == '[') {
                    curr_lvl++;
                }
                if (formula.charAt(i) == ']') {
                    curr_lvl--;
                }
                index_close_parenthesis = i;
            }
            if(index_close_parenthesis == formula.length()-1) {
                index_close_parenthesis++;
            }
            String to_be_replaced = formula.substring(index_open_parenthesis, index_close_parenthesis);
            String term_1 = formula.substring(index_open_parenthesis, index_biconditional);
            String term_2 = formula.substring(index_biconditional+3, index_close_parenthesis);
            //ensure we don't have extra [ 
			int openCount = term_2.length() - term_2.replaceAll("\\[", "").length();
			int closeCount = term_2.length() - term_2.replaceAll("\\]", "").length();
			int many_to_delete = closeCount - openCount;
			System.out.println("\n t2 before " + term_2);
			System.out.println("\n to be replaced before " + to_be_replaced);
			
			if (many_to_delete>0) {
                System.out.println("number of ] to delete = "+many_to_delete);
			    term_2 = term_2.substring(0, term_2.length()-many_to_delete);
			    to_be_replaced = to_be_replaced.substring(0, to_be_replaced.length()-many_to_delete);
			}
            System.out.println (to_be_replaced + "\n t1 " + term_1 + "\n t2 " + term_2);
            String replace_with = "[" +term_1+"->" + term_2 + "]AND[" + term_2 + "->" + term_1 + "]";
            System.out.println(replace_with);
            formula = formula.replace(to_be_replaced, replace_with);
            System.out.println(formula);
        }

        //handling ->
        while (formula.contains("->")) {
            int index_implication = formula.indexOf("->");
            int lvl_implication = 0;
            //left to right, encoutering [ increase the lvl, deeper
            for (int i =0 ; i<index_implication ; i++) {
                if (formula.charAt(i) == '[') {
                    lvl_implication++;
                }
                if (formula.charAt(i) == ']') {
                    lvl_implication--;
                }
            }
            // (R(a) <-> R(b)) -----> (R(a) -> R(b)) AND (R(b) -> R(a))
            int index_open_parenthesis = 0;
            int index_close_parenthesis = 0;
            int curr_lvl = lvl_implication;

            //right to left, encountering ( decrease the lvl, less deep
            for (int i = index_implication; curr_lvl >= lvl_implication && i>=0 ; i--) {
                if (formula.charAt(i) == '[') {
                    curr_lvl--;
                }
                if (formula.charAt(i) == ']') {
                    curr_lvl++;
                }
                index_open_parenthesis = i;
            }
            if(index_open_parenthesis>0 || curr_lvl< lvl_implication) {
				index_open_parenthesis++;
			}
            curr_lvl = lvl_implication;
            //left to right, encoutering ( increase the lvl, deeper
            for (int i = index_implication+2; curr_lvl >= lvl_implication && i<formula.length(); i++) {
                if (formula.charAt(i) == '[') {
                    curr_lvl++;
                }
                if (formula.charAt(i) == ']') {
                    curr_lvl--;
                }
                index_close_parenthesis = i;
            }
            if(index_close_parenthesis == formula.length()-1) {
                index_close_parenthesis++; 
            }
            String to_be_replaced = formula.substring(index_open_parenthesis, index_close_parenthesis);
            String term_1 = formula.substring(index_open_parenthesis, index_implication);
            String term_2 = formula.substring(index_implication+2, index_close_parenthesis);
            //ensure we don't have extra ( 
			int openCount = term_2.length() - term_2.replaceAll("\\[", "").length();
			int closeCount = term_2.length() - term_2.replaceAll("\\]", "").length();
			int many_to_delete = closeCount - openCount;
			System.out.println("\n t2 before " + term_2);
			System.out.println("\n to be replaced before " + to_be_replaced);
			
			if (many_to_delete>0) {
			    term_2 = term_2.substring(0, term_2.length()-many_to_delete);
			    to_be_replaced = to_be_replaced.substring(0, to_be_replaced.length()-many_to_delete);
			}
            System.out.println (to_be_replaced + "\n t1 " + term_1 + "\n t2 " + term_2);
            String replace_with = "[!" +term_1+"]OR" + term_2;
            System.out.println(replace_with);
            formula = formula.replace(to_be_replaced, replace_with);
            System.out.println(formula);
        }

        //handling   ![![[A]and[b]]] --> [A]and[b]  ![![atom]]    ![[c]AND[[![R]]OR[C]]]
        while (formula.contains("![!")) {
            int index_double_negation = formula.indexOf("![!");
            int lvl_double_negation = 0;
            //left to right, encoutering [ increase the lvl, deeper
            for (int i =0 ; i<index_double_negation ; i++) {
                if (formula.charAt(i) == '[') {
                    lvl_double_negation++;
                }
                if (formula.charAt(i) == ']') {
                    lvl_double_negation--;
                }
            }
            int index_close_parenthesis = 0;
            int curr_lvl = lvl_double_negation + 1;
            for (int i = index_double_negation+2; curr_lvl > lvl_double_negation && i<formula.length(); i++) {
                if (formula.charAt(i) == '[') {
                    curr_lvl++;
                }
                if (formula.charAt(i) == ']') {
                    curr_lvl--;
                }
                index_close_parenthesis = i;
            }
            index_close_parenthesis++;
            String to_be_replaced = formula.substring(index_double_negation, index_close_parenthesis);
            String replace_with = formula.substring(index_double_negation+4, index_close_parenthesis-2);
            System.out.println("to_be_replaced  "+to_be_replaced);
            System.out.println("replcae with "+replace_with);
            formula = formula.replace(to_be_replaced, replace_with);
            System.out.println("final is "+formula);
        }  
        //handling ![[A]AND[B]] --> [![A]]OR[![B]]
        //handling ![[A]OR[B]] --> [![A]]AND[![B]]
        while (formula.contains("![[")) {
            int index_negation_to_distribute = formula.indexOf("![[");
            int lvl_negation_to_distribute = 0;
            //left to right, encoutering [ increase the lvl, deeper
            for (int i =0 ; i<index_negation_to_distribute ; i++) {
                if (formula.charAt(i) == '[') {
                    lvl_negation_to_distribute++;
                }
                if (formula.charAt(i) == ']') {
                    lvl_negation_to_distribute--;
                }
            }
            int index_close_parenthesis = 0;
            int curr_lvl = lvl_negation_to_distribute + 1;
            for (int i = index_negation_to_distribute+2; curr_lvl > lvl_negation_to_distribute && i<formula.length(); i++) {
                if (formula.charAt(i) == '[') {
                    curr_lvl++;
                }
                if (formula.charAt(i) == ']') {
                    curr_lvl--;
                }
                index_close_parenthesis = i;
            }
            index_close_parenthesis++;
            String to_be_replaced = formula.substring(index_negation_to_distribute, index_close_parenthesis);
            //check what is the connective to distribute the negation over
            int index_and = 0;
            int index_or = 0;
            curr_lvl = lvl_negation_to_distribute + 1;
            for (int i = index_negation_to_distribute+2; curr_lvl > lvl_negation_to_distribute && i<formula.length(); i++) {
                if (formula.charAt(i) == '[') {
                    curr_lvl++;
                }
                if (formula.charAt(i) == ']') {
                    curr_lvl--;
                }
                if (curr_lvl == (lvl_negation_to_distribute+1) && formula.charAt(i) == 'A') {
                    if (formula.charAt(i+1) == 'N' && formula.charAt(i+2) == 'D') {
                        index_and = i;
                    }
                }
                if (curr_lvl == (lvl_negation_to_distribute+1) && formula.charAt(i) == 'O') {
                    if (formula.charAt(i+1) == 'R') {
                        index_or = i;
                    }
                }
            }
            //we have a AND
            String replace_with = new String();
            if (index_and>0) {
                replace_with = "[!" + formula.substring(index_negation_to_distribute+2, index_and-1) + "]]OR[!" + formula.substring(index_and+3, index_close_parenthesis-1) + "]";
                
            }
            //we have a OR
            if (index_or>0) {
                replace_with = "[!" + formula.substring(index_negation_to_distribute+2, index_or-1) + "]]AND[!" + formula.substring(index_or+2, index_close_parenthesis-1) + "]";
            }
            
            System.out.println("to_be_replaced  "+to_be_replaced);
            System.out.println("replcae with "+replace_with);
            formula = formula.replace(to_be_replaced, replace_with);
            System.out.println("final is "+formula);
        }
        while (formula.contains("![!")) {
            int index_double_negation = formula.indexOf("![!");
            int lvl_double_negation = 0;
            //left to right, encoutering [ increase the lvl, deeper
            for (int i =0 ; i<index_double_negation ; i++) {
                if (formula.charAt(i) == '[') {
                    lvl_double_negation++;
                }
                if (formula.charAt(i) == ']') {
                    lvl_double_negation--;
                }
            }
            int index_close_parenthesis = 0;
            int curr_lvl = lvl_double_negation + 1;
            for (int i = index_double_negation+2; curr_lvl > lvl_double_negation && i<formula.length(); i++) {
                if (formula.charAt(i) == '[') {
                    curr_lvl++;
                }
                if (formula.charAt(i) == ']') {
                    curr_lvl--;
                }
                index_close_parenthesis = i;
            }
            index_close_parenthesis++;
            String to_be_replaced = formula.substring(index_double_negation, index_close_parenthesis);
            String replace_with = formula.substring(index_double_negation+4, index_close_parenthesis-2);
            //System.out.println("to_be_replaced  "+to_be_replaced);
            //System.out.println("replcae with "+replace_with);
            formula = formula.replace(to_be_replaced, replace_with);
            //System.out.println("final is "+formula);
        }
        return formula;
    }

    //this method handles the input formula, branches the store and returns a set of formulas with no store
    //we then will call our solvers for each of these formula in the main, 
    //we need to provide and empty set of String in input as to_return
    //select(store(array,i,v),j) = a AND f(f(a,b),b) = a --> {f(f(a,b),b) AND v = a AND i=j,
    //                                                       f(f(a,b),b) AND select(array,j)=v AND i!=j}
    public static Set<String> FormulaArraysToSetnoStore(String formula) {
        //since we are in T-array without extensionality, every occurence of store must be inside of a select
        //select(store(a,i,v),j)
        Set<String> to_return = new HashSet<>();
        if (formula.contains("store")) {
            String F1 = new String(formula);
            String F2 = new String(formula);
            String[] split = formula.split("AND");
            for(int i=0; i< split.length; i++) {
                split[i] = split[i].replaceAll(" ", "");
            }
            F1 = F1.replaceAll(" ", "");
            F2 = F2.replaceAll(" ", "");
            for(int i=0; i< split.length; i++) {
                String to_be_replaced = new String();
                if (split[i].contains("store")) {
                    //s is select(store(array,i,v),j)=a
                    String s = split[i];
                    String[] no_equalities = s.split("=|!=");
                    for (String term : no_equalities){
                        if (term.contains("store")) {
                            to_be_replaced = term;
                            int index_v = term.length()-1;
                            int index_j = 0;
                            int index_i = 0;
                            int index_parentesis_afterv = 0;
                            while(term.charAt(index_v) != ',') {
                                index_v--;
                            }
                            index_j = index_v + 1;
                            //index_v is now the index of the comma after v),
                            index_v--;
                            index_parentesis_afterv = index_v;
                            while(term.charAt(index_v) != ',') {
                                index_v--;
                            }
                            int index_secondcomma = index_v;
                            index_v++;
                            int index_firstcomma = index_secondcomma - 1;
                            while(term.charAt(index_firstcomma) != ',') {
                                index_firstcomma--;
                            }
                            index_i = index_firstcomma + 1;
                            String first_index = term.substring(index_i, index_secondcomma);
                            String second_index = term.substring(index_j, term.length()-1);
                            int index_first_parentesis = term.indexOf("store(") + 5;
                            String array_forf2 = term.substring(index_first_parentesis, index_firstcomma);

                            String v = term.substring(index_v, index_parentesis_afterv);
                            F1 = F1.replace(to_be_replaced, v);
                            F1 += " AND " + first_index + "=" + second_index;
                            to_return.addAll(FormulaArraysToSetnoStore(F1));
                            String select_forf2 = "select" + array_forf2 + "," + second_index + ")";
                            F2 = F2.replace(to_be_replaced, select_forf2);
                            F2 += " AND " + first_index + "!=" + second_index;
                            to_return.addAll(FormulaArraysToSetnoStore(F2));
                        }

                    }
                }
            }
            
        }
        else {
            to_return.add(formula);
            return to_return;
        }

        return to_return;    
    }
    
    //we use AND for logical operators
    //this works only if formula in input is conjunction of literals, we need to have done DNF before and take one of disjuncts
    //this method requires no occurence of store (handled with FormulaArraysToSetnoStore)
    //this method requires no additional parenthesis outside for functions and relations
    //f(a,b) = a AND f(f(a,b),b) = a -> F = {f(a,b)=a, f(f(a,b),b)=a}
    public static Set<String> FormulatoConjuncts(String formula){
        String[] split = formula.split("AND");

        for(int i=0; i< split.length; i++) {
            split[i] = split[i].replaceAll(" ", "");
        }
        Set<String> F = new HashSet<>(Arrays.asList(split));
        
        Set<String> toAdd = new HashSet<>();
        Set<String> toRemove = new HashSet<>();

        //handling !atom for T-list
        for (String s : F) {
            if (s.contains("!atom(")) {
                //System.out.println("found !atom");
                int index = s.indexOf("(");
                //System.out.println("found (");
                String a = s.substring(index+1, s.length()-1);
                //System.out.println("a is " + a);
                a+= "=cons("+a+"_1,"+ a + "_2)";
                //System.out.println("a is " + a);
                toRemove.add(s);
                //System.out.println("fatto remove");
                toAdd.add(a);
                //System.out.println("fatto add");
            }
        }

        //handling relation by considering them as function equal to TRUE    (R(t1,t2..,tn -> f_R(t1,t2..,tn)=TRUE)
        //we do this even for all R that have no equality in the formula (R -> R=TRUE)
        //here we handle the not (!) since it is only applcable to the relations
        for (String s : F) {
            if(! s.contains("=")) {
                if (s.contains("atom")) continue ;
                if (s.contains("!")) {
                    //System.out.println("found !");
                    int index = s.indexOf("!");
                    String a = "f_";
                    a += s.substring(index+1, s.length());
                    //System.out.println("a is " + a);
                    a+= "!=TRUE";
                    //System.out.println("a is " + a);
                    toRemove.add(s);
                    
                    toAdd.add(a);
                    
                }
                else {
                    //System.out.println("found R");
                    String a = "f_";
                    a += s + "=TRUE";
                    //System.out.println("a is " + a);
                    toRemove.add(s);
                    
                    toAdd.add(a);
                    
                }
            }
        } 

        //handling select for T-array without extensionality considering that there are no store calls
        // we can do this because we handle store in the function above
        for (String s : F) {
 
            if (s.contains("select(")) {
                //flag to check if we have to add "=" or "!=" between the two parts of the split
                Boolean not_flag = false;
                if (s.contains("!")) {
                    not_flag = true;
                }
                String[] s_split = s.split("=");
                for(int i=0; i< s_split.length; i++) {
                    String part = s_split[i];
                    if (part.contains("select(")) {
                        //System.out.println("found select");
                        int index_parenthesis = part.indexOf("(");
                        int index_comma = part.indexOf(",");
                        //System.out.println("found (");
                        String a ="f_" + part.substring(index_parenthesis+1, index_comma);
                        index_parenthesis = part.indexOf(")");
                        //System.out.println("a is " + a);
                        a+="(" +  part.substring(index_comma +1, index_parenthesis+1) ;
                        s_split[i] = a;
                        }
                    else if (part.contains("!")) {
                        int index = part.indexOf("!");
                        String a = part.substring(0, index);
                        s_split[i] = a;
                    }
                }
                String a = new String();
                if (not_flag) {
                    a =  s_split[0] + "!=" + s_split[1];
                }
                else {
                    a = s_split[0] + "=" + s_split[1];
                }
   
                toRemove.add(s);
                //System.out.println("fatto remove");
                toAdd.add(a);
                //System.out.println("fatto add");
            }
        }
        F.removeAll(toRemove);
        F.addAll(toAdd);
        return F;
    }
    //F = {f(a,b)=a, f(f(a,b),b)=a} -> only F- as a set of pairs
    public static Set<List<String>> get_forbiddenset(Set<String> F){
        Set<List<String>> F_forbidden = new HashSet<List<String>>();
        for(String s : F)  {
            if (s.contains("!=")) {
                String[] split = s.split("!=");
                List<String> pair = new ArrayList<>();
                pair.add(split[0]);
                pair.add(split[1]);
                F_forbidden.add(pair);
            }
        }
        return F_forbidden;
    }
    //F = {f(a,b)=a, f(f(a,b),b)=a} -> only F+ as a set of pairs
    public static Set<List<String>> get_equalities(Set<String> F){
        Set<List<String>> F_equalities = new HashSet<List<String>>();
        for(String s : F)  {
            if (s.contains("!=")) {continue;}
            if (s.contains("=")) {
                String[] split = s.split("=");
                List<String> pair = new ArrayList<>();
                pair.add(split[0]);
                pair.add(split[1]);
                F_equalities.add(pair);
            }
        }
        return F_equalities;
    }
    
    //F = {f(f(a,b),b)=a, f(a,b)=a} -> Sf {a ,b, f(ab), f((fab, b))}
    public static Set<String> ConjunctstoTerms(Set<String> F){
        //System.out.println("--------------");
        //System.out.println("chiamo con" + F);

        Set<String> Sf = new HashSet<>();
        
        Iterator<String> value = F.iterator();
        while(value.hasNext()) {
            String s = value.next();
            String[] split = s.split("=|!=");

            for(int i=0; i< split.length; i++){
                String now = split[i];
                String term = "";
                //System.out.println("now: " +  now);

                for (int c=0; c<now.length(); c++ ) {
                    //if we find a (, we know there is a function / relation to handle
                    if (now.charAt(c) == '(' ) {
                        
                        int l = now.length()-1;
                        while(now.charAt(l) != ')') {
                            l--;
                        }
                        String k = now.substring(c+1,l);
                        
                        Set<String> k_split = new HashSet<>();
                        int nested_count = 0;
                        int j=0;
                        Set<Integer> indexes = new HashSet<>();
                        while (j<k.length()) {
                            if (k.charAt(j) == '(') nested_count++;
                            if (k.charAt(j) == ')') nested_count--;
                            if (nested_count == 0 && k.charAt(j) == ',') {
                                indexes.add(j);
                            }
                            j++;
                        }
                        //System.out.println("indexes are "+ indexes);
                        Iterator<Integer> index = indexes.iterator();
                        int last_comma = 0;
                        while(index.hasNext()) {
                            j = index.next();
                            //System.out.println("j is "+j);
                            k_split.add(k.substring(last_comma,j));
                            last_comma = j+1;
                        }
                        if (j+1 < k.length()) {
                            k_split.add(k.substring(j+1,k.length()));
                        }
                        if(last_comma == 0) {
                            k_split.add(k);
                        }
                        Sf.addAll(ConjunctstoTerms(k_split));
                        term += now.substring(c, l);
                        c = l - 1;
                    }
                    else {term = term + now.charAt(c);}
                    
                }
                Sf.add(term);
                //System.out.println("ora sf è " + Sf);
            }
        }
        //System.out.println("fine con " + F);
        return Sf;
    }

    //this method builds the DAG from the set of terms Sf, the set of forbidden pairs F_forbidden and the set of equalities F_equalities
    public static Dagsolver TermstoDAG(Set<String> Sf, Set<List<String>> F_forbidden, Set<List<String>> F_equalities){ 
        //node_map is a map that contains all symbols in Sf as keys and the corresponding nodes as values
        
        Map <String, Node> node_map = new HashMap<>();
        List<Node> DAG = new ArrayList<>();
        List<String> Sf_list = new ArrayList<>(Sf);
        Collections.sort(Sf_list, new MyComparator());
        System.out.println("Sf_list is " + Sf_list);
        int id = 0;
        for (String s : Sf_list) {
            //handling constants and variables
            if(s.indexOf("(") == -1) {
                Node n = new Node(id,s);
                node_map.put(s,n);
                DAG.add(n);
            }
            else {
                int first_parentesis = s.indexOf("(");
                //String[] split = s.split("\\(");
                String fn = s.substring(0, first_parentesis);
                String split = s.substring(first_parentesis+1, s.length()-1);

                List<String> args = new ArrayList<>();
                int nested_count = 0;
                int j=0;
                Set<Integer> indexes = new HashSet<>();
                while (j<split.length()) {
                    if (split.charAt(j) == '(') nested_count++;
                    if (split.charAt(j) == ')') nested_count--;
                    if (nested_count == 0 && split.charAt(j) == ',') {
                        indexes.add(j);
                    }
                    j++;
                }
                //System.out.println("indexes are "+ indexes);
                Iterator<Integer> index = indexes.iterator();
                int last_comma = 0;
                while(index.hasNext()) {
                    j = index.next();
                    //System.out.println("j is "+j);
                    args.add(split.substring(last_comma,j));
                    last_comma = j+1;
                }
                if (j+1 < split.length()) {
                    args.add(split.substring(j+1,split.length()));
                }
                if(last_comma == 0) {
                    args.add(split);
                }
                System.out.println("args are " + args);
                List<Integer> id_args = new ArrayList<>();

                for (String arg : args) {
                    if (node_map.containsKey(arg)) {
                        id_args.add(node_map.get(arg).getId());
                    }
                }
                Node n = new Node(id,fn,id_args);
                node_map.put(s,n);
                DAG.add(n);
            }
            //System.out.println("------------------");
            id++;
        }
        //setting the ccpar for each node
        for (int i = DAG.size() - 1 ; i >= 0 ; i--) {
            List<Integer>args = DAG.get(i).getArgs();
            if (args != null) {
                for(Node n : DAG) {
                    if (args.contains(n.getId())) {
                        n.setccpar(DAG.get(i).getId());
                    }
                }
            }
        }
        //setting forbidden set of nodes and saving all pair inequalities (pairs of Id for convinience)
        Set<List<Integer>> Id_inequalities = new HashSet<>();
        for (List<String> pair : F_forbidden) {
            Node n1 = node_map.get(pair.get(0));
            Node n2 = node_map.get(pair.get(1));
            for(Node n : DAG) {
                if (n.getId() == (n1.getId())) {
                    n.setForbidden_set(n2.getId());
                }
                if (n.getId() == (n2.getId())) {
                    n.setForbidden_set(n1.getId());
                }
            }
            List<Integer> id_pair = new ArrayList<>();
            id_pair.add(n1.getId());
            id_pair.add(n2.getId());
            Id_inequalities.add(id_pair);
        }
        //saving all pair equalities (pairs of Id for convinience)
        Set<List<Integer>> Id_equalities = new HashSet<>();
        for (List<String> pair : F_equalities) {
            Node n1 = node_map.get(pair.get(0));
            Node n2 = node_map.get(pair.get(1));
            List<Integer> id_pair = new ArrayList<>();
            id_pair.add(n1.getId());
            id_pair.add(n2.getId());
            Id_equalities.add(id_pair);
        }
        

        //add the car/cdr nodes for all the cons
        Set<List<Integer>> toMerge = new HashSet<>();
        List<Node> toAdd = new ArrayList<>();
        for (Node n : DAG) {
            if (n.getFn().equals("cons")) {
                List<Integer> cons = new ArrayList<>();
                cons.add(n.getId());
                Node car = new Node(id, "car", cons);
                //add to list of ToAdd the car node and the first argument of cons
                Integer[] add_car = new Integer[] {n.getArgs().get(0), car.getId()};
                n.setccpar(id);
                id++;
                Node cdr = new Node(id, "cdr", cons);
                //add to list of ToAdd the cdr node and the second argument of cons
                Integer[] add_cdr = new Integer[] {n.getArgs().get(1), cdr.getId()};
                n.setccpar(id);
                id++;
                //DAG.add(car);
                //DAG.add(cdr);
                toAdd.add(car);
                toAdd.add(cdr);
                toMerge.add(Arrays.asList(add_car));
                toMerge.add(Arrays.asList(add_cdr));
            }
        }
        DAG.addAll(toAdd);
        Dagsolver Dag = new Dagsolver(DAG, Id_equalities, Id_inequalities);
        // merge car/cdr with arguments of cons
        for (List<Integer> pair : toMerge) {
            Dag.Merge(pair.get(0), pair.get(1));
        }
        return Dag;
    }
}
