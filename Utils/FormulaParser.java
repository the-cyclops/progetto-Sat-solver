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
    
    //this method handles the input formula, branches the store and returns a set of formulas with no store
    //we then will call our solvers for each of these formula in the main, 
    //we need to provide and empty set of String in input as to_return
    public static Set<String> FormulaArraysToSetnoStore(String formula, Set<String> to_return) {
        if (formula.contains("store")) {
            String[] split = formula.split("AND");
            for(int i=0; i< split.length; i++) {
                if (split[i].contains("store")) {
                    int index = split[i].indexOf("store");

                    //String term_for_F1 = 
                }
            }
            
        }
        else {
            to_return.add(formula);
            return to_return;
        }

        return null;    
    }
    
    //we use AND for logical operators
    //this works only if formula in input is conjunction of literals, we need to have done DNF before and take one of disjuncts
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

        //handling relation by considering them as function equal to TRUE    (R(t1,t2..,tn -> R(t1,t2..,tn)=TRUE)
        //we do this even for all R that have no equality in the formula (R -> R=TRUE)
        //here we handle the not (!) since it is only applcable to the relations
        for (String s : F) {
            if(! s.contains("=")) {
                if (s.contains("!")) {
                    //System.out.println("found !");
                    int index = s.indexOf("!");
                    String a = s.substring(index+1, s.length());
                    //System.out.println("a is " + a);
                    a+= "!=TRUE";
                    //System.out.println("a is " + a);
                    toRemove.add(s);
                    
                    toAdd.add(a);
                    
                }
                else {
                    //System.out.println("found R");
                    String a = s + "=TRUE";
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
